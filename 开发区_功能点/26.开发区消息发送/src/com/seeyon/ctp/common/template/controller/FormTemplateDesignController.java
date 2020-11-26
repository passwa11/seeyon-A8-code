package com.seeyon.ctp.common.template.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.template.manager.FormTemplateDesignManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.Strings;

public class FormTemplateDesignController extends BaseController{

	private static Log LOGGER = LogFactory.getLog(FormTemplateDesignController.class);
    private FormTemplateDesignManager formTemplateDesignManager;
    
    private CAPFormManager	capFormManager;
    private FormApi4Cap4 formApi4Cap4;
    private FormApi4Cap3 formApi4Cap3;

	


	//表单
	@SuppressWarnings("rawtypes")
	public ModelAndView saveTemplate2Cache(HttpServletRequest request,
            HttpServletResponse response) throws BusinessException{
		//基础信息
		Map baseInfo = ParamUtil.getJsonDomain("baseInfo");
		//流程信息
		Map processCreate = ParamUtil.getJsonDomain("processCreate");
		Map approveInfo = ParamUtil.getJsonDomain("approveInfo");
		Map dataMap = new HashMap();
		dataMap.put("baseInfo", baseInfo);
		dataMap.put("processCreate", processCreate);
		dataMap.put("approveInfo", approveInfo);

		Map saveformbind2List = formTemplateDesignManager.saveTemplate2Cache(dataMap);


		return null;
	}
	
	public ModelAndView advancePigeonhole(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/advancePigeonhole");
        Long formId = ReqUtil.getLong(request, "formId");
        boolean isCap4 = capFormManager.isCAP4Form(formId);
        if(isCap4) {
        	FormBean fb = formApi4Cap4.getEditingForm(formId);
        	List<FormFieldBean> tempList = fb.getAllFieldBeans();
        	List<FormFieldBean> fieldList = new ArrayList<FormFieldBean>();
        	
        	for (FormFieldBean f : tempList) {
                if (f.isMasterField() &&
                        (FormFieldComEnum.RADIO.getKey().equals(f.getInputType())
                                || FormFieldComEnum.SELECT.getKey().equals(f.getInputType())
                                || FormFieldComEnum.EXTEND_ACCOUNT.getKey().equals(f.getInputType())
                                || FormFieldComEnum.EXTEND_DEPARTMENT.getKey().equals(f.getInputType()))) {
                    fieldList.add(f);
                }
            }
        	
        	List<FormFieldBean> wordInjectionFormBeans = formApi4Cap4.getAllWordInjectionCtrls(formId);
        	mav.addObject("formBean", fb);
        	mav.addObject("fieldList", fieldList);
        	mav.addObject("redTemplete",Strings.isNotEmpty(wordInjectionFormBeans));
        	mav.addObject("wordInjectionFormBeans", wordInjectionFormBeans);
        }else {
        	com.seeyon.ctp.form.bean.FormBean fb = formApi4Cap3.getEditingForm();
            List<com.seeyon.ctp.form.bean.FormFieldBean> tempList = fb.getAllFieldBeans();
            List<com.seeyon.ctp.form.bean.FormFieldBean> fieldList = new ArrayList<com.seeyon.ctp.form.bean.FormFieldBean>();
            for (com.seeyon.ctp.form.bean.FormFieldBean f : tempList) {
    			if (f.isMasterField()){
                    if(FormFieldComEnum.RADIO.getKey().equals(f.getRealInputType())
    						|| FormFieldComEnum.SELECT.getKey().equals(f.getRealInputType())
    						|| FormFieldComEnum.EXTEND_ACCOUNT.getKey().equals(f.getRealInputType())
    						|| FormFieldComEnum.EXTEND_DEPARTMENT.getKey().equals(f.getRealInputType())
    						|| FormFieldComEnum.EXTEND_PROJECT.getKey().equals(f.getRealInputType())){
    					fieldList.add(f);
    				} else if(FormFieldComEnum.OUTWRITE.getKey().equals(f.getRealInputType())){
                    	com.seeyon.ctp.form.bean.FormFieldBean realFieldBean = f.findRealFieldBean();
    					if(FormFieldComEnum.RADIO.getKey().equals(realFieldBean.getFormatType())
    							|| FormFieldComEnum.SELECT.getKey().equals(realFieldBean.getFormatType())
    							|| FormFieldComEnum.EXTEND_ACCOUNT.getKey().equals(realFieldBean.getFormatType())
    							|| FormFieldComEnum.EXTEND_DEPARTMENT.getKey().equals(realFieldBean.getFormatType())
    							|| FormFieldComEnum.EXTEND_PROJECT.getKey().equals(realFieldBean.getFormatType())){
    						fieldList.add(f);
    					}
    				}
    			}
            }
            mav.addObject("pigeonholeType","fromTempleteManage");
            mav.addObject("formBean", fb);
            mav.addObject("fieldList", fieldList);
    		//预归档高级设置增加是否带正文套红模板参数
    		mav.addObject("redTemplete", fb.hasRedTemplete());
        }
        mav.addObject("isCap4", isCap4);
        return mav;
    }
	
	
	public FormTemplateDesignManager getFormTemplateDesignManager() {
		return formTemplateDesignManager;
	}

	public void setFormTemplateDesignManager(FormTemplateDesignManager formTemplateDesignManager) {
		this.formTemplateDesignManager = formTemplateDesignManager;
	}
	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public CAPFormManager getCapFormManager() {
		return capFormManager;
	}
	public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
		this.formApi4Cap4 = formApi4Cap4;
	}
	
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
		this.formApi4Cap3 = formApi4Cap3;
	}
	
}
