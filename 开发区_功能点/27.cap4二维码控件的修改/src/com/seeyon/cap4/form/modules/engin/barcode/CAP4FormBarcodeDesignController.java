package com.seeyon.cap4.form.modules.engin.barcode;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormBindAuthBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCtrl;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldUtil;
import com.seeyon.cap4.form.modules.business.BizConfigBean;
import com.seeyon.cap4.form.modules.business.BusinessManager;
import com.seeyon.cap4.form.modules.business.BusinessSourceTypeManager;
import com.seeyon.cap4.form.modules.business.FormAppBO;
import com.seeyon.cap4.form.modules.engin.design.CAP4FormDesignManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.BarcodeEnum;
import com.seeyon.cap4.form.util.BizUtil;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 二维码控件设置
 *
 * @author wangh
 * @create 2018-01-29 11:00
 **/
@CheckRoleAccess(roleTypes={OrgConstants.Role_NAME.BusinessDesigner})
public class CAP4FormBarcodeDesignController extends BaseController {
    private CAP4FormDesignManager cap4FormDesignManager;
    private BusinessManager businessManager4;
    private CAP4FormManager cap4FormManager;
    private OrgManager orgManager;

    /**
     * 二维码设置的入口
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView barcodeSet(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("capctrlbarcode/barcodeSet");
        return modelAndView;
    }

    /**
     * 文本类型二维码的内容组成设置
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView contentSet(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView modelAndView = new ModelAndView("capctrlbarcode/contentSet");
        Long formId = ReqUtil.getLong(request, "formId", 0L);
        Long bizConfigId = ReqUtil.getLong(request, "bizConfigId", 0L);
        String fieldName = ReqUtil.getString(request, "fieldName");
        FormBean formBean = cap4FormDesignManager.getEditingForm(true, formId);
        //所属应用，如果为null，说明是单表
        BizConfigBean bizConfigBean = businessManager4.findBizConfigById(bizConfigId);
        FormFieldBean currentFieldBean = formBean.getFieldBeanByName(fieldName);
        boolean isMasterField = currentFieldBean.isMasterField();
        //找到表单对应的字段列表
        List<FormFieldBean> tempList = formBean.getAllFieldBeans();
        //最终能参与设置的字段
        List<FormFieldBean> finalField = new ArrayList<FormFieldBean>();
        for (FormFieldBean formFieldBean : tempList) {
            FormFieldCtrl formFieldCtrl = FormFieldUtil.getFormFieldCtrl(formFieldBean.getInputType());
            if (!formFieldCtrl.barcodeContent()) {
                continue;
            }
            //大文本不能参与
            if (Enums.FieldType.LONGTEXT.getKey().equals(formFieldBean.getFieldType())) {
                continue;
            }
            if (formFieldBean.isMasterField()) {
                finalField.add(formFieldBean);
            } else if (!isMasterField && formFieldBean.getOwnerTableName().equals(currentFieldBean.getOwnerTableName())) {
                //重复表字段需要判断是否为当前字段所在的重复表
                finalField.add(formFieldBean);
            }
        }
        modelAndView.addObject("fieldList", finalField);
        //找模版
        List<FormAppBO> templateList = new ArrayList<FormAppBO>();
        if (bizConfigBean != null) {
            //找整个应用下面的所有模版
            List<BusinessSourceTypeManager> sourceTypes = BizUtil.getSubSourceTypeByBaseTypeId("formApp");
            for (BusinessSourceTypeManager sourceType : sourceTypes) {
                templateList.addAll(sourceType.getFormAppBOList(AppContext.getCurrentUser(), null, null, false, bizConfigBean.getId()));
            }
        } else {
            //找单个表单的
            getSingleFormTemplate(formBean, templateList);
        }
        modelAndView.addObject("templateList", templateList);
        //二维码类型
        List<BarcodeEnum.BarcodeType> barcodeType = BarcodeEnum.BarcodeType.getAllEnum();
        modelAndView.addObject("barcodeType", barcodeType);
        //二维码纠错项
        List<BarcodeEnum.BarcodeCorrectionOption> optionList = BarcodeEnum.BarcodeCorrectionOption.getAllItem();
        modelAndView.addObject("optionDefault", BarcodeEnum.BarcodeCorrectionOption.middle.getKey());
        modelAndView.addObject("optionList", optionList);
        //图片大小
        List<String> sizeList = new ArrayList<String>();
        for (int i = 1; i <= 10; i++) {
            sizeList.add(String.valueOf(i));
        }
        modelAndView.addObject("sizeList", sizeList);
        return modelAndView;
    }

    /**
     * 找单个表单下的所有模版
     *
     * @param formBean
     * @param templateList
     */
    private void getSingleFormTemplate(FormBean formBean, List<FormAppBO> templateList) throws BusinessException {
        if (Enums.FormType.processesForm.getKey() == formBean.getFormType()) {
            //流程表单
            List<CtpTemplate> templates = formBean.getBind().getFlowTemplateList();
            if (Strings.isEmpty(templates)) {
                //如果为空，则直接查询，说明当前表单没有编辑过应用绑定
                templates = cap4FormManager.getFormSystemTemplate(formBean.getId());
            }
            for (CtpTemplate ctpTemplate : templates) {
                FormAppBO bo = new FormAppBO();
                bo.setId(ctpTemplate.getId().toString());
                bo.setSourceValue(ctpTemplate.getId());
                bo.setFormAppmainId(formBean.getId());
                bo.setName(ctpTemplate.getSubject());
                bo.setTitle(ctpTemplate.getSubject() + "(" + formBean.getFormName() + ")");
                V3xOrgMember orgMember = orgManager.getMemberById(formBean.getOwnerId());
                if(orgMember != null){
                    bo.setFormCreator(orgMember.getName());
                }
                bo.setSourceType(Enums.SourceType.SOURCE_TYPE_FLOWTEMPLATE.getKey());
                templateList.add(bo);
            }
        } else {
            //无流程
            Map<String, FormBindAuthBean> unTemplates = formBean.getBind().getUnFlowTemplateMap();
            for (Map.Entry<String, FormBindAuthBean> entry : unTemplates.entrySet()) {
                FormBindAuthBean template = entry.getValue();
                FormAppBO bo = new FormAppBO();
                bo.setId(template.getId().toString());
                bo.setSourceValue(template.getId());
                bo.setFormAppmainId(formBean.getId());
                bo.setName(template.getName());
                bo.setTitle(template.getName() + "(" + formBean.getFormName() + ")");
                V3xOrgMember orgMember = orgManager.getMemberById(formBean.getOwnerId());
                if(orgMember != null){
                    bo.setFormCreator(orgMember.getName());
                }
                bo.setSourceType(Enums.SourceType.SOURCE_TYPE_INFOMANAGE.getKey());
                templateList.add(bo);
            }
        }
    }

    public void setCap4FormDesignManager(CAP4FormDesignManager cap4FormDesignManager) {
        this.cap4FormDesignManager = cap4FormDesignManager;
    }

    public void setBusinessManager4(BusinessManager businessManager4) {
        this.businessManager4 = businessManager4;
    }

    public void setCap4FormManager(CAP4FormManager cap4FormManager) {
        this.cap4FormManager = cap4FormManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
}
