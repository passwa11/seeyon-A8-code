package com.seeyon.ctp.rest.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.seeyon.apps.customFieldCtrl.constants.FormFieldEnum;
import com.seeyon.apps.customFieldCtrl.kit.CAP4FormKit;
import com.seeyon.apps.customFieldCtrl.kit.DateKit;
import com.seeyon.apps.customFieldCtrl.kit.StrKit;
import com.seeyon.apps.customFieldCtrl.vo.Barcode;
import com.seeyon.cap4.form.bean.FormAuthViewBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.common.taglibs.functions.Functions;

@Path("cap4/customFieldCtrl")
@Consumes({"application/json"})
@Produces({"application/json"})
public class ScanBarcodeResources extends BaseResource {
	
    private static CAP4FormManager cap4FormManager = (CAP4FormManager)AppContext.getBean("cap4FormManager");
    
    private static CAP4FormDataManager cap4FormDataManager =  (CAP4FormDataManager) AppContext.getBean("cap4FormDataManager");
    
    private static Random rand = new Random();
    
    /**
     * 批量打印
     * @param masterId	主表id
     * @param recordId	重复表的明细行id
     * @return
     * @throws BusinessException
     */
    @GET
    @Produces({"application/json"})
    @Path("batchPrint")
    public Response batchPrint(@QueryParam("masterId") String masterId, @QueryParam("subId") String recordId) throws BusinessException {
        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
        Map<String, Object> result = new HashMap<String, Object>();
        if (null == cacheFormData) {
            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
        }
        try {
			List<FormDataSubBean> subs = CAP4FormKit.getSubBeans(cacheFormData);
            List<Map<String, Object>> billSubDatas = new ArrayList<>();
            // 判断是否为空的情况
			for(FormDataSubBean subdata : subs) {
				FormFieldBean tiaoma = CAP4FormKit.getFieldBean(subdata, FormFieldEnum.wuliaobianhao.getText());
				String code = CAP4FormKit.getFieldStrValue(subdata, FormFieldEnum.wuliaobianhao.getText());
				if(StrKit.isNull(code)) {
					code = getBarcode(DateKit.getSimpleDate(new Date()));
				}
				// 设置条码属性
				Barcode barcode = new Barcode();
				barcode.setCode(code);
				// 需要加入一些判断，比如必须填写了名称价格这些才能打印
				barcode.setName(CAP4FormKit.getFieldStrValue(subdata, FormFieldEnum.wuliaomingchen.getText()));
				barcode.setPrice(CAP4FormKit.getFieldStrValue(subdata, FormFieldEnum.price.getText()));
				barcode.setDept(Functions.showDepartmentName(StrKit.toLong(CAP4FormKit.getFieldValue(subdata, FormFieldEnum.zerenbumen.getText()))));
				// 缓存添加值
				subdata.addFieldValue(tiaoma.getName(), code);
				// 获取字段的返回给前台的数据
                Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(code, tiaoma, null);
                // 回填的字段数据  多个字段
                Map<String, Object> filldatas = new HashMap<String, Object>();
                filldatas.put(tiaoma.getName(), tempRes);
                
                Map<String, Object> map = new HashMap<String, Object>();
                // 回填表单的数据
                map.put("tbName", tiaoma.getOwnerTableName());
                map.put("data", filldatas);
                // 用于打印的数据
                map.put("barcode", barcode);
                map.put("recordId", subdata.getId() + "");
                billSubDatas.add(map);
			}
	        result.put("subs", billSubDatas);
		} catch (Exception e) {
			return fail("获取从表信息失败");
		}
    	return success(result);
    }
    
    
	/**
	 * @param formId	表单id
	 * @param masterId	主表id
	 * @param value		条码的值
	 * @return
	 * @throws BusinessException
	 */
	@GET
    @Produces({"application/json"})
    @Path("scan")
    public Response scan(@QueryParam("formId") String formId, @QueryParam("masterId") String masterId, @QueryParam("value") String value) throws BusinessException {
        FormBean formBean = cap4FormManager.getForm(Long.valueOf(formId), false);
        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
        if (null == cacheFormData) {
            return fail("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
        }
        List<FormTableBean> subForms = formBean.getSubTableBean();
        FormTableBean subForm = subForms.get(0);
        List<FormDataSubBean> subs = cacheFormData.getSubData(subForm.getTableName());
        Map<String, Object> result = new HashMap<String, Object>();
        // 获取 扫码区域
   	 	FormFieldBean codeArea = CAP4FormKit.getFieldBean(cacheFormData, FormFieldEnum.saomaquyu.getText());  
   	 	cacheFormData.addFieldValue(codeArea.getName(), "");
	   	Map<String, Object> codeMap = CAPFormUtil.getDisplayValueMap("", codeArea, null);
	    Map<String, Object> codedata = new HashMap<String, Object>();
	    codedata.put(codeArea.getName(), codeMap);
        result.put("maindata", codedata);
        result.put("subTbName", subForm.getTableName());
        FormFieldBean tiaoma = null;
        // 增加一个逻辑：先判断是否含有这个数据，如果有的话，那么直接数量 +1
        final String tmValue = value;
        List<FormDataSubBean> filterList = subs.stream().filter(item-> 
            tmValue.equals((CAP4FormKit.getFieldValue(item, FormFieldEnum.wuliaobianhao.getText()))))
                .collect(Collectors.toList());
        
        // 如果物料编号存在，则累加一个数量
        if(!StrKit.isNull(filterList)) {
        	 FormDataSubBean subdata = filterList.get(0);
        	 int sl = CAP4FormKit.getIntValue(subdata, FormFieldEnum.lingyongshuliang.getText());
    		 sl++;
        	 FormFieldBean slField = CAP4FormKit.getFieldBean(subdata, FormFieldEnum.lingyongshuliang.getText());  
        	 subdata.addFieldValue(slField.getName(), sl);
             List<Map<String, Object>> billSubDatas = new ArrayList<>();
             Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(sl, slField, null);
             Map<String, Object> filldatas = new HashMap<String, Object>();
             filldatas.put(slField.getName(), tempRes);
             Map<String, Object> map = new HashMap<String, Object>();
             // 回填表单的数据
             map.put("tbName", slField.getOwnerTableName());
             map.put("data", filldatas);
             map.put("recordId", subdata.getId() + "");
             billSubDatas.add(map);
             result.put("subs", billSubDatas);
             result.put("add", false);
             return success(result);
        }
        
        FormDataSubBean subdata = null;
        // 赋值bug修改   新增到空白的一行上面
        filterList = subs.stream().filter(item-> 
            StrKit.isNull(CAP4FormKit.getFieldValue(item, FormFieldEnum.wuliaobianhao.getText())))
                .collect(Collectors.toList());
        
        if(StrKit.isNull(filterList)) {
            result.put("add", true);
            return success(result);
        }
        
        subdata = filterList.get(0);
        tiaoma = CAP4FormKit.getFieldBean(subdata, FormFieldEnum.wuliaobianhao.getText());  
        // 设置缓存数据
        subdata.addFieldValue(tiaoma.getName(), value);
        List<Map<String, Object>> billSubDatas = new ArrayList<>();
        Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(value, tiaoma, null);
        Map<String, Object> filldatas = new HashMap<String, Object>();
        // 处理回填数据的问题 只有第一次需要进行此处理
        Set<String> fillBackFields = new HashSet<String>();

        FormAuthViewBean formAuthViewBean = null;
        // 查看视图权限
        if(cacheFormData.getExtraMap().containsKey(FormConstant.viewRight)){
            formAuthViewBean = (FormAuthViewBean) cacheFormData.getExtraAttr(FormConstant.viewRight);
        }
        if(formAuthViewBean == null){
        	List<FormAuthViewBean> auths = formBean.getAllFormAuthViewBeans();
        	for(FormAuthViewBean bean : auths) {
        		if("add".equals(bean.getType())) {
        			formAuthViewBean = bean;
        			break;
        		}
        	}
        }
        
        // 计算表单其他数据的值，一起回填给前台
        cap4FormDataManager.calcAllWithFieldIn(formBean, tiaoma, cacheFormData, subdata, fillBackFields, formAuthViewBean, true);
        
        Map<String, Object> masterMap = cacheFormData.getAllDataMap();
        
        Map<String, Object> mainDatas = new HashMap<String, Object>();
        for(String key : masterMap.keySet()) {
        	if(key.startsWith("field")) {
        		FormFieldBean fieldBean = cacheFormData.getFieldBeanByFieldName(key);
        		Object fieldVal = masterMap.get(key);
        		if(fieldBean.isMasterField() && null != fieldVal && fieldBean.isCalcField()) {
        	        Map<String, Object> mainTemp = CAPFormUtil.getDisplayValueMap(fieldVal, fieldBean, null);
        	        mainDatas.put(key, mainTemp);
        		}
        	}
        }
        mainDatas.put(codeArea.getName(), codeMap);

        result.put("maindata", mainDatas);
        // 处理重复表字段
        masterMap = subdata.getRowData();
        for(String key : masterMap.keySet()) {
        	if(key.startsWith("field")) {
        		FormFieldBean fieldBean = subForm.getFormFieldBeanByFieldName(key);
        		Object fieldVal = masterMap.get(key);
        		if(null != fieldVal) {
        	        Map<String, Object> subTemp = CAPFormUtil.getDisplayValueMap(fieldVal, fieldBean, null);
        	        filldatas.put(key, subTemp);
        		}
        	}
        }
        
        filldatas.put(tiaoma.getName(), tempRes);
        Map<String, Object> map = new HashMap<String, Object>();
        // 回填表单的数据
        map.put("tbName", tiaoma.getOwnerTableName());
        map.put("data", filldatas);
        map.put("recordId", subdata.getId() + "");
        billSubDatas.add(map);
        result.put("subs", billSubDatas);
        result.put("add", false);
        
        return success(result);
    }
	
	/**
	 * 自定义规则
	 * @return
	 */
	private String getBarcode(String barcode) {
		if(null == barcode) {
			barcode = DateKit.getSimpleDate(new Date());
		}
		for (int i = 0; i < 5; i++) {
			barcode += rand.nextInt(9); //生成随机数
		}
		return barcode;
	}
}
