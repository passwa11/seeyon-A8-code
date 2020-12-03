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
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-12-28.
 * 查看表单记录按钮控件实现类
 */
public class ShowFormDataCtrl extends FormFieldCustomCtrl {
    private static final Log LOGGER = CtpLogFactory.getLog(ShowFormDataCtrl.class);
    private static final String targetFormId = "targetFormId";
    private static final String targetFormInfo = "targetFormInfo";
    private static final String relationId = "relationId";
    private static final String rightId = "rightId";

    @Override
    public void init() {
        this.setPluginId("showFormDataCtrl");
        this.setIcon("cap-icon-form-record");
        this.setButtonType(Enums.ButtonType.SeeyonBtn);
        LOGGER.info("自定义控件" + this.getText() + "init执行开始");
        ParamDefinition templateIdParam = new ParamDefinition();
        templateIdParam.setDialogUrl("apps_res/cap/customCtrlResources/showFormDataCtrlResources/html/setTargetFormInfo.html");
        templateIdParam.setDisplay("form.ctrl.showformdata.param.display");
        templateIdParam.setName(targetFormInfo);
        templateIdParam.setParamType(Enums.ParamType.button);
        addDefinition(templateIdParam);
        LOGGER.info("自定义控件" + this.getText() + "init执行结束，params.size:" + super.params.size());
    }

    @Override
    public boolean isButton() {
        return true;
    }

    @Override
    public String getKey() {
        return "8896390104409123792";
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("form.ctrl.showformdata.name");
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
        return "{path:'apps_res/cap/customCtrlResources/showFormDataCtrlResources/',jsUri:'js/runtime.js',initMethod:'create',nameSpace:'seeyon-cap-inform-cwidget-view-form'}";
    }

    @Override
    public String getMBInjectionInfo() {
        return "{path:'http://cwidgetviewform.v5.cmp/v1.0.0/',weixinpath:'cwidgetviewform',jsUri:'js/runtime.js',initMethod:'create',nameSpace:'seeyon-cap-inform-cwidget-view-form'}";
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
                // 目标表单
                String tFormId = (String) targetFormInfoMap.get(targetFormId);
                if (StringUtil.checkNull(tFormId)) {
                    LOGGER.error("自定义控件目标表单ID为空！数据异常，将整个设置清空" + customParam);
                    resultMap.put(BusinessExportConstant.CUSTOMPARAM, "");
                    return;
                }
                targetFormInfoMap.put(targetFormId, businessDataBean.getRealId4Export(Long.valueOf(tFormId)).toString());
                // 关联关系ID
                String rId = (String) targetFormInfoMap.get(relationId);
                if (StringUtil.checkNull(rId)) {
                    LOGGER.error("自定义控件关联关ID为空！数据异常，将整个设置清空" + customParam);
                    resultMap.put(BusinessExportConstant.CUSTOMPARAM, "");
                    return;
                }
                targetFormInfoMap.put(relationId, businessDataBean.getRealId4Export(Long.valueOf(rId)).toString());
                // 目标表pc端权限
                List<Map<String, Object>> pcRights = (List<Map<String, Object>>) targetFormInfoMap.get("pcRights");
                redirectViewRightInfo(businessDataBean, pcRights, false);
                // 目标表移动端权限
                List<Map<String, Object>> mbRights = (List<Map<String, Object>>) targetFormInfoMap.get("mbRights");
                redirectViewRightInfo(businessDataBean, mbRights, false);
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

                    Long oldTargetFormId = Long.parseLong(String.valueOf(targetFormInfoMap.get(targetFormId)));
                    Long oldRelationId = Long.parseLong(String.valueOf(targetFormInfoMap.get(relationId)));
                    if (businessDataBean.isUpgrade()) {
                        oldTargetFormId = businessDataBean.getRealId4Export(oldTargetFormId);
                        oldRelationId = businessDataBean.getRealId4Export(oldRelationId);
                    }
                    Long newTargetFormId = businessDataBean.getNewIdByOldId(oldTargetFormId);
                    Long newRelationId = businessDataBean.getNewIdByOldId(oldRelationId);
                    if (newTargetFormId == null || newRelationId == null) {
                        // 表单或者应用绑定找不到，直接清空，这种情况属于先设置按钮，然后删除目标表或者应用绑定，再导出业务包，这个时候云上的和本地的都没有，就认为是删除了，这里就会是null
                        formFieldBean.setCustomParam("");
                        LOGGER.info("应用安装时因找不到表单或者关系，所以该自定义控件设置清空，当前表单ID：" + formBean.getId() + ">>按钮设置表单formId=" + oldTargetFormId + "   关系ID=oldRelationId" + oldRelationId);
                        return;
                    }
                    targetFormInfoMap.put(targetFormId, String.valueOf(newTargetFormId));
                    targetFormInfoMap.put(relationId, String.valueOf(newRelationId));

                    List<Map<String, Object>> mbRights = (List<Map<String, Object>>) targetFormInfoMap.get("mbRights");
                    List<Map<String, Object>> pcRights = (List<Map<String, Object>>) targetFormInfoMap.get("pcRights");
                    redirectViewRightInfo(businessDataBean, mbRights, true);
                    redirectViewRightInfo(businessDataBean, pcRights, true);
                }
                formFieldBean.setCustomParam(JSONUtil.toJSONString(customParamMap));
            } else {//单表导入
                formFieldBean.setCustomParam("");
            }
        }
    }

    /**
     * 导入导出过程中，处理按钮中的视图权限ID
     */
    private void redirectViewRightInfo(BusinessDataBean businessDataBean, List<Map<String, Object>> rightMap, boolean isImport) {
        if (null != rightMap) {
            for (Map<String, Object> rightInfoMap : rightMap) {
                if (rightInfoMap.containsKey(rightId)) {
                    String viewRightStr = String.valueOf(rightInfoMap.get(rightId));
                    if (!StringUtil.checkNull(viewRightStr)) {
                        String[] viewAndRight = viewRightStr.split("[.]");
                        if (viewAndRight.length == 2) {
                            String vId = viewAndRight[0];
                            String rId = viewAndRight[1];
                            if (isImport) {
                                Long oldVId = Long.valueOf(vId);
                                Long oldRId = Long.valueOf(rId);
                                if (businessDataBean.isUpgrade()) {
                                    oldVId = businessDataBean.getRealId4Export(oldVId);
                                    oldRId = businessDataBean.getRealId4Export(oldRId);
                                }
                                Long newViewId = businessDataBean.getNewIdByOldId(oldVId);
                                if (newViewId != null) {
                                    //配置的关联，有可能已经被删除了，表单已经不存在，所以导入的时候找不到对应的信息
                                    vId = newViewId.toString();
                                }
                                Long newRightId = businessDataBean.getNewIdByOldId(oldRId);
                                if (newRightId != null) {
                                    rId = newRightId.toString();
                                }
                            } else {
                                vId = businessDataBean.getRealId4Export(Long.valueOf(vId)).toString();
                                rId = businessDataBean.getRealId4Export(Long.valueOf(rId)).toString();
                            }
                            rightInfoMap.put(rightId, vId + "." + rId);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void otherSave(FormFieldBean fieldBean, FormBean formBean, FormSaveAsBean mapping) {
        fieldBean.setCustomParam("");
    }

    @Override
    public boolean authNotNullAndValIsNull(FormDataMasterBean formDataMasterBean, FormFieldBean field, FormAuthViewFieldBean authViewFieldBean, Object val) {
        return false;
    }
}
