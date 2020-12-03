package com.seeyon.cap4.form.bean.fieldCtrl;

import com.seeyon.cap4.form.bean.FormAuthViewFieldBean;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormSaveAsBean;
import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.modules.importandexport.BusinessEnums;
import com.seeyon.cap4.form.modules.importandexport.BusinessExportConstant;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-12-28.
 * 新建表单数据按钮控件实现类
 */
public class NewFormDataCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(NewFormDataCtrl.class);
    private static final String targetFormInfo = "targetFormInfo";
    private static final String formId = "formId";
    private static final String bindId = "bindId";

    @Override
    public void init() {
        this.setPluginId("newFormDataCtrl");
        this.setIcon("cap-icon-new-form");
        this.setButtonType(Enums.ButtonType.SeeyonBtn);
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition templateIdParam = new ParamDefinition();
        templateIdParam.setDialogUrl("apps_res/cap/customCtrlResources/newFormDataCtrlResources/html/setTargetFormInfo.html");
        templateIdParam.setDisplay("form.ctrl.newformdata.param.display");
        templateIdParam.setName(targetFormInfo);
        templateIdParam.setParamType(Enums.ParamType.button);
        addDefinition(templateIdParam);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    /**
     * 新建表单是一个按钮类控件
     *
     * @return
     */
    @Override
    public boolean isButton() {
        return true;
    }

    @Override
    public String getKey() {
        return "4742819235729708174";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("form.ctrl.newformdata.ctrlname");
    }

    @Override
    public boolean canBathUpdate() {
        return false;
    }

    @Override
    public List<String[]> getListShowDefaultVal(Integer externalType) {
        return null;
    }

    @Override
    public String[] getDefaultVal(String defaultValue) {
        return new String[0];
    }

    @Override
    public String getPCInjectionInfo() {
        return "{path:'apps_res/cap/customCtrlResources/newFormDataCtrlResources/',jsUri:'js/runtime.js',initMethod:'create',nameSpace:'seeyon-cap-inform-cwidget-new-form'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://cwidgetnewform.v5.cmp/v1.0.0/',weixinpath:'cwidgetnewform',jsUri:'js/runtime.js',initMethod:'create',nameSpace:'seeyon-cap-inform-cwidget-new-form'}";
    }

    @Override
    public String getFieldLength() {
        return "20";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getJson4Export(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean, Map<String, Object> resultMap) {
        String customParam = formFieldBean.getCustomParam();
        if (!StringUtil.checkNull(customParam)) {
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            if (customParamMap.containsKey(targetFormInfo)) {
                Map<String, Object> targetFormInfoMap = (Map<String, Object>) customParamMap.get(targetFormInfo);
                Map<String, Object> targetFormMap = (Map<String, Object>) targetFormInfoMap.get("targetForm");
                String formIdStr = (String) targetFormMap.get(formId);
                String bindIdStr = (String) targetFormMap.get(bindId);
                if(Strings.isNotBlank(formIdStr) && Strings.isNotEmpty(bindIdStr)){
                    targetFormMap.put(formId, businessDataBean.getRealId4Export(Long.valueOf(formIdStr)).toString());
                    targetFormMap.put(bindId, businessDataBean.getRealId4Export(Long.valueOf(bindIdStr)).toString());
                }else{
                    LOGGER.info(formBean.getFormName()+"表单导出的时候，自定义按钮绑定信息找不到了或者表单已经被删除了，"+formFieldBean.getDisplay());
                    resultMap.put(BusinessExportConstant.CUSTOMPARAM, "");
                    return;
                }
            }
            resultMap.put(BusinessExportConstant.CUSTOMPARAM, JSONUtil.toJSONString(customParamMap));
        } else {
            resultMap.put(BusinessExportConstant.CUSTOMPARAM, "");
        }
    }

    /**
     * 业务包导入控件定义信息重定向
     */
    @SuppressWarnings("unchecked")
    @Override
    public void importInfoAfterBizImport(FormBean formBean, FormFieldBean formFieldBean, BusinessDataBean businessDataBean) {
        String customParam = formFieldBean.getCustomParam();
        if (!StringUtil.checkNull(customParam)) {
            boolean isBizImport = businessDataBean.getExportType().getKey().equals(BusinessEnums.ExportType.APPLICATION.getKey());
            if (isBizImport) {//业务包导入
                Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
                if (customParamMap.containsKey(targetFormInfo)) {
                    Map<String, Object> targetFormInfoMap = (Map<String, Object>) customParamMap.get(targetFormInfo);
                    Map<String, Object> targetFormInfo = (Map<String, Object>) targetFormInfoMap.get("targetForm");
                    String formIdStr = String.valueOf(targetFormInfo.get(formId));
                    String bindIdStr = String.valueOf(targetFormInfo.get(bindId));
                    int targetType = Integer.valueOf(String.valueOf(targetFormInfo.get("targetType")));
                    if (targetType == 0) {//业务内表单新建
                        Long oldFormId = Long.parseLong(formIdStr);
                        Long oldBindId = Long.parseLong(bindIdStr);
                        if (businessDataBean.isUpgrade()) {
                            oldFormId = businessDataBean.getRealId4Export(oldFormId);
                            oldBindId = businessDataBean.getRealId4Export(oldBindId);
                        }
                        Long newFormId = businessDataBean.getNewIdByOldId(oldFormId);
                        Long newBindId = businessDataBean.getNewIdByOldId(oldBindId);
                        if (newFormId == null || newBindId == null) {
                            // 表单或者应用绑定找不到，直接清空，这种情况属于先设置按钮，然后删除目标表或者应用绑定，再导出业务包，这个时候云上的和本地的都没有，就认为是删除了，这里就会是null
                            formFieldBean.setCustomParam("");
                            LOGGER.info("应用安装时因找不到表单，所以该自定义控件设置清空，当前表单ID：" + formBean.getId() + ">>按钮设置表单formId=" + oldFormId);
                        } else {
                            targetFormInfo.put(formId, newFormId);
                            targetFormInfo.put(bindId, newBindId);
                            formFieldBean.setCustomParam(JSONUtil.toJSONString(customParamMap));
                        }
                    } else {//跨业务表单新建
                        formFieldBean.setCustomParam("");
                        LOGGER.info("跨应用表单新建，清空控件设置");
                    }
                } else {
                    formFieldBean.setCustomParam("");
                    LOGGER.info("目标表单信息不存在，清空控件设置");
                }
            } else {//单表导入
                formFieldBean.setCustomParam("");
                LOGGER.info("表单导入清空控件设置");
            }
        }
    }

    /**
     * 表单另存为的时候，如果控件需要单独实现另存的逻辑，需要重写此接口实现控件自身的另存逻辑
     *
     * @param fieldBean
     * @param formBean
     */
    public void otherSave(FormFieldBean fieldBean, FormBean formBean, FormSaveAsBean formSaveAsBean) {
        Long saveToBizId = formSaveAsBean.getSaveToBizId();
        Long oldBizId = formSaveAsBean.getOldBizId();
        //另存为选择为空说明是存为单表
        if (null == saveToBizId) {
            //原来是应用中的表，清空
            if (null != oldBizId) {
                fieldBean.setCustomParam("");
            }
        } else {//存为应用中的表单
            if (null == oldBizId) {//原来是单表
                fieldBean.setCustomParam("");
            } else {//原来是应用中的表单
                if (!saveToBizId.equals(oldBizId)) {//跨应用另存，清空
                    fieldBean.setCustomParam("");
                }
            }
        }
    }

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        return false;
    }
}
