package com.seeyon.apps.govdoc.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.manager.GovdocElementManager;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.po.FormOptionExtend;
import com.seeyon.apps.govdoc.po.FormOptionSort;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionInscriberSetEnum;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.webmodel.FormBoundPerm;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;

public class GovdocFormController extends BaseController {

	private GovdocFormManager govdocFormManager;
    //private FormOptionSortManager formOptionSortManager;
    //private FormOptionExtendManager formOptionExtendManager;
    
	/**
     * 意见元素设置界面
     * @author cx
     */
    public ModelAndView opinionSet(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView mav = new ModelAndView("govdoc/form/design/formOpinionSet");
        FormBean formBean = govdocFormManager.getEditingForm();
       
        FormOptionExtend FormExtend = govdocFormManager.findByFormId(formBean.getId());
        List<FormFieldBean> formFieldBeans = formBean.getAllFieldBeans();
        List<FormFieldBean> list  = new ArrayList<FormFieldBean>();
        for (FormFieldBean formFieldBean : formFieldBeans) {
        	//如果是意见类型，加入到list中
        	if("edocflowdealoption".equals(formFieldBean.getInputType())){
						list.add(formFieldBean);
        	}
		}
        List<FormBoundPerm> processList = getProcessOpinionByEdocFormId(list, formBean.getId(), formBean.getGovDocFormType(), AppContext.currentAccountId(), false);
        mav.addObject("processList", processList);
        // 用于提交选择参数
        String listStr = "";
        for(FormBoundPerm perm : processList) {
            listStr += perm.getPermItem();
            listStr += ",";
        }
        if(!"".equals(listStr) && listStr.length() > 1) {
            listStr = listStr.substring(0, listStr.length() - 1);
        }
        mav.addObject("listStr", listStr);
        //
        mav.addObject("formConfigJSON",FormOpinionConfig.getDefualtConfig());
        if(null!=FormExtend){
        	if(FormExtend.getAccountId().longValue() == AppContext.currentAccountId()) {
                //bean.setWebOpinionSet(info.getOptionFormatSet());//公文单配置，JSON字符串
                //公文配置详细，和新建保持一致
                String defualtConfig = FormExtend.getOptionFormatSet();
                mav.addObject("formConfigJSON", defualtConfig);
                mav.addObject("edocFormStatusId", FormExtend.getId());
            }
        }
        FormOpinionConfig displayConfig = null;
		// 公文单显示格式
		if(null!=FormExtend){
    	    displayConfig = JSONUtil.parseJSONString(FormExtend.getOptionFormatSet(), FormOpinionConfig.class);
		}
		if(displayConfig == null){
		    displayConfig = new FormOpinionConfig();
		}
		mav.addObject("opinionType",displayConfig.getOpinionType());
        return mav;
    }
    
	public List<FormBoundPerm> getProcessOpinionByEdocFormId(List<FormFieldBean> fieldList, long formId, int edocType,long accountId, boolean isUpload) throws Exception{
		GovdocElementManager govdocElementManager = (GovdocElementManager)AppContext.getBean("govdocElementManager");		
		
		List<FormBoundPerm> boundPermList = new ArrayList<FormBoundPerm>();

		String value = "";
		String processItemName = "";
		
		for(FormFieldBean fieldName: fieldList){
				value = fieldName.getName();
				FormBoundPerm formBoundPerm = new FormBoundPerm();
				formBoundPerm.setPermItem(value);
				formBoundPerm.setPermName(fieldName.getDisplay());
				formBoundPerm.setPermItemName(processItemName);
				formBoundPerm.setProcessItemName(processItemName);
				boundPermList.add(formBoundPerm);	 
		}
			for(FormBoundPerm perm : boundPermList){
				//此处判断当前文单是否属于本单位的文单
				//boolean flag=edocFormManager.getFormAccountEdoc(accountId, edocFormId);
				//if(flag){
				List<FormOptionSort> list = govdocFormManager.findBoundByFormId(formId, perm.getPermItem(),accountId);
				String str_temp = "";
				String str_temp_b = "";
				for(FormOptionSort bound : list){
					str_temp += bound.getFlowPermNameLabel();
					str_temp += ",";
					EdocElement element = null;
					element = govdocElementManager.getByFieldName(bound.getFlowPermName());
					if(null!=element && element.getIsSystem()){//查找元素，如果非空证明是系统预置的fuhe,shenpi....节点权限和系统预置的处理意见
						str_temp_b += bound.getFlowPermName();
					}else if(element == null){//如果element为空，那么
						element = govdocElementManager.getByFieldName(bound.getProcessName());//在用processName查
						if(null!=element && element.getIsSystem()){//节点权限为自定义但处理意见元素为系统的
							str_temp_b += bound.getFlowPermName();
						}else if(null!=element && (element.getIsSystem()==false)){//节点权限为自定义而且处理意见元素也是扩展的
							str_temp_b += bound.getFlowPermName();
						}
					}
					str_temp_b += ",";
					perm.setSortType(bound.getSortType());
				}
				if(str_temp.endsWith(",") && str_temp_b.endsWith(",")){
					str_temp = str_temp.substring(0, str_temp.length()-1);
					str_temp_b = str_temp_b.substring(0, str_temp_b.length()-1);
				}
				if(!"".equals(str_temp)){
					perm.setPermItemName(str_temp);
				}
				if(!"".equals(str_temp_b)){
					perm.setPermItemList(str_temp_b);
				}
				
			}			
		return boundPermList;
	}
	
	public void saveOpinionSet(HttpServletRequest request,HttpServletResponse response) throws IOException{
    	PrintWriter out = response.getWriter();
    	FormBean formBean = govdocFormManager.getEditingForm();
    	//文单设置设置保存
    	try{
	        FormOpinionConfig formConfig = parseConfigData(request);
	        String optionFormatSet = JSONUtil.toJSONString(formConfig);//将配置转换
	        FormOptionExtend formExtend = new FormOptionExtend();
	        formExtend.setAccountId(AppContext.currentAccountId());
	        formExtend.setFormId(formBean.getId());
	        formExtend.setOptionFormatSet(optionFormatSet);
	        formExtend.setId(UUIDLong.longUUID());
	        govdocFormManager.saveOrUpdate(formExtend);
	        String tempS = request.getParameter("listStr");
            if(null != tempS && !"".equals(tempS)) {
                String[] tempArray = tempS.split(",");
                List<FormOptionSort> list = new ArrayList<FormOptionSort>();
                for(String process_name : tempArray) {
                    String sortType = request.getParameter("sortType_" + process_name);
                    FormOptionSort FormOpinionSort = new FormOptionSort();
                    FormOpinionSort.setId(UUIDLong.longUUID());
                    FormOpinionSort.setIdIfNew();
                    FormOpinionSort.setFormId(formBean.getId());
                    FormOpinionSort.setProcessName(process_name);
                    FormOpinionSort.setSortType(sortType);
                    FormOpinionSort.setDomainId(AppContext.currentAccountId());
          			list.add(FormOpinionSort);
                }
                govdocFormManager.saveOrUpdateList(list);
            }
	        out.print("1");
    	}catch(Exception e){
    		logger.error("保存意见元素设置错误",e);
    		out.print("0");
    	}finally{
    		out.close();
    	}
    	
    }
	
	/**
     * 解析前端传入的文单意见配置信息
     * @Author      : xuqiangwei
     * @Date        : 2014年11月17日下午3:49:58
     * @param request
     * @return
     */
    private FormOpinionConfig parseConfigData(HttpServletRequest request){
      //文单设置设置保存
        FormOpinionConfig formConfig = new FormOpinionConfig();
        
        String showLastOptionOnly = request.getParameter("optionType");
        if(Strings.isNotBlank(showLastOptionOnly)){//意见显示设置
            formConfig.setOpinionType(showLastOptionOnly);
        }
        
        //系统落款设置  start
        String[] depts = request.getParameterValues("showOrgnDept");
        formConfig.setInscriberNewLine(false);
        formConfig.setShowDept(false);
        formConfig.setHideInscriber(false);
        formConfig.setShowName(false);
        formConfig.setShowUnit(false);
        String showAtt = request.getParameter("showAtt");
        if("0".equals(showAtt)){
        	formConfig.setShowAtt(false);
        }else {
        	formConfig.setShowAtt(true);
		}
        if(depts != null && depts.length > 0){
            for(String s : depts){
                
                if(s.equals(OpinionInscriberSetEnum.UNIT.getValue())){
                    formConfig.setShowUnit(true);
                }else if(s.equals(OpinionInscriberSetEnum.DEPART.getValue())){
                    formConfig.setShowDept(true);
                }else if(s.equals(OpinionInscriberSetEnum.NAME.getValue())){
                    formConfig.setShowName(true);
                }else if(s.equals(OpinionInscriberSetEnum.INSCRIBER.getValue())){
                    formConfig.setHideInscriber(true);
                }else if(s.equals(OpinionInscriberSetEnum.INSCRIBER_NEW_LINE.getValue())){
                    formConfig.setInscriberNewLine(true);
                }
            }
        }
        
        //处理时间格式化
        String dealTimeFmt = request.getParameter("dealTimeFormt");
        if(Strings.isNotBlank(dealTimeFmt)){
            formConfig.setShowDateType(dealTimeFmt);
        }
        
        //签名方式显示方式设置
        String nameShowType = request.getParameter("nameShowTypeItem");
        if(Strings.isNotBlank(nameShowType)){
            formConfig.setShowNameType(nameShowType);
        }

      //处理时间
        String dealTimeModel = request.getParameter("dealTimeModel");
        if(Strings.isNotBlank(dealTimeModel)){
            formConfig.setShowDateModel(dealTimeModel);
        }
        return formConfig;
    }
    
	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}
	
}
