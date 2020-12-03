package com.seeyon.cap4.form.bean.button;


import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormSaveAsBean;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.form.util.FormDesignUtil;
import com.seeyon.cap4.form.vo.ResultInfoVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;

import java.util.List;
import java.util.Map;

/**
 * 标签打印按钮实现类
 * Created by weijh on 2019-7-8.
 */
public class LabelPrintBtn  extends CommonBtn {
    @Override
    public String getKey() {
        return "9169710638743518422";
    }

    @Override
    public boolean canUse(Enums.FormType formType){
        return true;
    }

    @Override
    public String getNameSpace() {
        return "customBtn" + this.getKey();
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.btn.labelPrintBtn.text");
    }

    @Override
    public String getPCInjectionInfo() {
        return "{\"path\":\"apps_res/cap/customCtrlResources/labelPrintBtnResources/\",\"jsUri\":\"js/labelPrintingBtn.js\",\"initMethod\":\"init\",\"nameSpace\":\"button_1567049206956\"}";
    }

    @Override
    public String getMBInjectionInfo() {
        return null;
    }

    @Override
    public void init() {
        this.setPluginId("labelPrintBtn");
        this.setIcon("cap-icon-custom-button");
        BtnParamDefinition targetFormInfoParam = new BtnParamDefinition();
        targetFormInfoParam.setDialogUrl("apps_res/cap/customCtrlResources/labelPrintBtnResources/html/setLabelInfo.html");
        targetFormInfoParam.setDisplay("com.cap.btn.newFormDataBtn.param1.display");
        targetFormInfoParam.setName("labelPrintInfo");
        targetFormInfoParam.setParamType(Enums.BtnParamType.button);
        targetFormInfoParam.setDialogWidth("567");
        targetFormInfoParam.setDialogHeight("366");
        addDefinition(targetFormInfoParam);
    }

    /**
     * 导出的扩展接口，应用绑定自定义按钮用，有需要的重写该方法
     *
     * @param formBean         当前表单
     * @param customParam
     * @param businessDataBean 导出中间对象，如果有附件，可以放到对象中的unifiedExportAttachment中
     * @param resultMap        按鈕json
     */
    @Override
    public void getJson4Export(FormBean formBean, String customParam, BusinessDataBean businessDataBean, Map<String, Object> resultMap) {
        if(Strings.isNotEmpty(customParam)){
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            resultMap.putAll(customParamMap);
        }
    }

    /**
     * 在业务导入完之后，应用绑定自定义按钮的处理接口
     *
     * @param formBean
     * @param customParam
     * @param businessDataBean
     * @param btnInfoMap
     */
    @Override
    public void importInfoAfterBizImport(FormBean formBean, String customParam, BusinessDataBean businessDataBean, Map<String, Object> btnInfoMap) {
        if(Strings.isNotEmpty(customParam)){
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            btnInfoMap.putAll(customParamMap);
        }
    }

    @Override
    public void otherSave(FormSaveAsBean formSaveAsBean, FormBean formBean, Map<String, Object> btnInfoMap){

    }

    /**
     * <P>设计器中修改字段、删除字段或者运维中心修改字段类型的时候，校验自定义按钮是否已经使用该字段</P>
     * <li>1.设计器操作的时候，如果有参与并且不能修改或者删除的，需要设置success属性和msg属性</li>
     * <li>2.运维中心修改字段类型，如果不允许修改，需要设置success位false，并且调用addTipsInfo方法，添加提示信息</li>
     * @param resultInfoVO 校验结果对象
     * @param customMap 自定义按钮设置json字符串
     * @param formBean     当前表单
     * @param param        校验需要的参数对象
     *   <pre>
     *      <li>1.包含字段信息：name,inputType,fieldType 等；</li>
     *      <li>2.handleType：delete 表示删除字段；modify：表示修改字段</li>
     *      <li>3.from:该校验的来源；design:设计中心；maintenance:运维中心；</li>
     *   </pre>
     */
    @Override
    public void onFieldInfoChange(ResultInfoVO resultInfoVO,Map<String,Object> customMap, FormBean formBean, Map<String, Object> param) throws BusinessException {
        String customParam = customMap.get("customParam") == null?"":customMap.get("customParam").toString();
        if(Strings.isNotBlank(customParam)){
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            if(customParamMap != null){
                Map<String,Object> infoMap = (Map<String,Object>)customParamMap.get("labelPrintInfo");
                Map<String,Object> info = (Map<String,Object>)infoMap.get("labelPrintInfo");
                List<Map<String,Object>> dataList = (List<Map<String,Object>>)info.get("data");
                String from = ParamUtil.getString(param, FormConstant.FROM);
                String handleType = ParamUtil.getString(param, FormConstant.HANDLE_TYPE);
                boolean deleteFlag = FormConstant.DELETE.equals(handleType);
                String handleTips = deleteFlag ? ResourceUtil.getString("form.datamatch.del.label") : ResourceUtil.getString("form.oper.update.label");
                String bindName = ParamUtil.getString(param,"bindName","");
                for(Map<String,Object> map:dataList){
                    Object obj = map.get("fieldId");
                    if(obj != null){
                        String fieldName = ParamUtil.getString(param, "name");
                        if(fieldName.equals(obj.toString())){
                            resultInfoVO.setSuccess(false);
                            if (FormConstant.MAINTENANCE.equals(from)) {
                                resultInfoVO.addTipsInfo(FormDesignUtil.setTipsInfoObject(formBean.getFormName(),formBean.getId().toString(),String.valueOf(formBean.getFormType()),ResourceUtil.getString("cap.formDesign.app.appBinding"),bindName,ResourceUtil.getString("com.cap.btn.labelPrintBtn.check.item.label"),formBean.getId().toString(), Enums.CheckCategory.UNFLOW_BIND.getKey()));
                            }else{
                                resultInfoVO.setMsg(ResourceUtil.getString("com.cap.btn.labelPrintBtn.change.tips")+handleTips);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
