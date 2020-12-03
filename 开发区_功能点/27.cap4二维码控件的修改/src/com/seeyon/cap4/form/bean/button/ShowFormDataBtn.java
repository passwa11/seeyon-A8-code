package com.seeyon.cap4.form.bean.button;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormSaveAsBean;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-12-26.
 * 应用绑定修改按钮实现类
 */
public class ShowFormDataBtn extends CommonBtn {
    private static final Log LOGGER = CtpLogFactory.getLog(ShowFormDataBtn.class);

    private static final String targetFormInfo = "targetFormInfo";
    private static final String targetFormId = "targetFormId";
    private static final String relationId = "relationId";
    private static final String rightId = "rightId";

    @Override
    public void init() {
        this.setPluginId("showFormDataBtn");
        //@todo 图标等ue提供之后再修改icon
        this.setIcon("cap-icon-erweima");
        BtnParamDefinition targetFormInfoParam = new BtnParamDefinition();
        targetFormInfoParam.setDialogUrl("apps_res/cap/customCtrlResources/showFormDataBtnResources/html/setTargetFormInfo.html");
        targetFormInfoParam.setDisplay("com.cap.btn.newFormDataBtn.param1.display");
        targetFormInfoParam.setName("targetFormInfo");
        targetFormInfoParam.setParamType(Enums.BtnParamType.button);
        targetFormInfoParam.setDialogWidth("640");
        targetFormInfoParam.setDialogHeight("415");
        addDefinition(targetFormInfoParam);
    }

    @Override
    public boolean canUse(Enums.FormType formType){
        return true;
    }
    @Override
    public String getKey() {
        return "7606653559858518849";
    }

    @Override
    public String getNameSpace() {
        return "customBtn" + this.getKey();
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.btn.showFormDataBtn.text");
    }

    @Override
    public String getPCInjectionInfo() {
        return "{\"path\":\"apps_res/cap/customCtrlResources/showFormDataBtnResources/\",\"jsUri\":\"js/" + this.getNameSpace() + ".umd.min.js\",\"initMethod\":\"init\",\"nameSpace\":\"" + this.getNameSpace() + "\"}";
    }

    @Override
    public String getMBInjectionInfo() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getJson4Export(FormBean formBean, String customParam, BusinessDataBean businessDataBean, Map<String, Object> resultMap) {
        if (Strings.isNotEmpty(customParam)) {
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            if (customParamMap != null && customParamMap.size() > 0) {
                resultMap.putAll(customParamMap);
                Map<String, Object> tFormInfo = (Map<String, Object>) customParamMap.get(targetFormInfo);
                // 目标表单名称
                String tFormId = (String) tFormInfo.get(targetFormId);
                if (StringUtil.checkNull(tFormId)) {
                    LOGGER.error("自定义按钮目标表单ID为空！数据异常，将整个设置清空" + customParam);
                    customParamMap.clear();
                    return;
                }
                tFormInfo.put(targetFormId, businessDataBean.getRealId4Export(Long.valueOf(tFormId)).toString());
                // 关联关系ID
                String rId = (String) tFormInfo.get(relationId);
                if (StringUtil.checkNull(tFormId)) {
                    LOGGER.error("自定义按钮关联关ID为空！数据异常，将整个设置清空" + customParam);
                    customParamMap.clear();
                    return;
                }
                tFormInfo.put(relationId, businessDataBean.getRealId4Export(Long.valueOf(rId)).toString());
                // 目标表pc端权限
                List<Map<String, Object>> pcRights = (List<Map<String, Object>>) tFormInfo.get("pcRights");
                replaceRealRightId(businessDataBean, pcRights, false);
                // 目标表移动端权限
                List<Map<String, Object>> mbRights = (List<Map<String, Object>>) tFormInfo.get("mbRights");
                replaceRealRightId(businessDataBean, mbRights, false);
            }
        }
    }

    /**
     * 处理按钮中的视图权限ID
     */
    private void replaceRealRightId(BusinessDataBean businessDataBean, List<Map<String, Object>> oldRights, boolean isImport) {
        Map<String, Object> realRightMap;
        for (Map<String, Object> rightMap : oldRights) {
            if (rightMap.containsKey(rightId)) {
                String viewRightStr = (String) rightMap.get(rightId);
                if (!StringUtil.checkNull(viewRightStr)) {
                    String[] idArr = viewRightStr.split("[.]");
                    String vId = idArr[0];
                    String rId = idArr[1];
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
                    rightMap.put(rightId, vId + "." + rId);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void importInfoAfterBizImport(FormBean formBean, String customParam, BusinessDataBean businessDataBean, Map<String, Object> btnInfoMap) {
        if (Strings.isNotEmpty(customParam)) {
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            if (customParamMap != null && customParamMap.size() > 0) {
                btnInfoMap.putAll(customParamMap);
                Map<String, Object> tFormInfo = (Map<String, Object>) customParamMap.get(targetFormInfo);

                String tFormId = (String) tFormInfo.get(targetFormId);// 目标表ID
                String rId = (String) tFormInfo.get(relationId);// 关联关系ID
                Long oldTargetFormId = Long.valueOf(tFormId);
                Long oldRelationId = Long.valueOf(rId);
                if (businessDataBean.isUpgrade()) {
                    oldTargetFormId = businessDataBean.getRealId4Export(oldTargetFormId);
                    oldRelationId = businessDataBean.getRealId4Export(oldRelationId);
                }
                Long newTargetFormId = businessDataBean.getNewIdByOldId(oldTargetFormId);
                Long newRelationId = businessDataBean.getNewIdByOldId(oldRelationId);
                if (newTargetFormId == null || newRelationId == null) {
                    // 表单或者应用绑定找不到，直接清空，这种情况属于先设置按钮，然后删除目标表或者应用绑定，再导出业务包，这个时候云上的和本地的都没有，就认为是删除了，这里就会是null
                    btnInfoMap.clear();
                    btnInfoMap.putAll(Collections.<String, Object>emptyMap());
                    LOGGER.info("应用安装时因找不到表单或者关系，所以该自定义按钮设置清空，当前表单ID：" + formBean.getId() + ">>按钮设置表单formId=" + oldTargetFormId + "   关系ID=oldRelationId" + oldRelationId);
                    return;
                }
                tFormInfo.put(targetFormId, newTargetFormId.toString());
                tFormInfo.put(relationId, newRelationId.toString());

                // 目标表pc端权限
                List<Map<String, Object>> pcRights = (List<Map<String, Object>>) tFormInfo.get("pcRights");
                replaceRealRightId(businessDataBean, pcRights, true);
                // 目标表移动端权限
                List<Map<String, Object>> mbRights = (List<Map<String, Object>>) tFormInfo.get("mbRights");
                replaceRealRightId(businessDataBean, mbRights, true);
            }
        }
    }

    @Override
    public void otherSave(FormSaveAsBean formSaveAsBean, FormBean formBean, Map<String, Object> btnInfoMap) {
        //应用绑定查看按钮另存为的时候清空，2019-1-27和产品贾攀确认的结论
        btnInfoMap.put("customParam", "");
    }
}

/*
 * targetFormInfo的Json格式
 * {
 *     "targetFormInfo":{
 *         "targetFormId":"-7910434400142735310",
 *         "targetFormName":"应用绑定设置验证副本232323",
 *         "relationName":"人的鬼地方",
 *         "relationId":"",
 *         "mbRights":[
 *             {
 *                 "rightName":"视图1.显示",
 *                 "rightId":"-4870577323939312650.-8086160475779676743",
 *                 "checked":true
 *             },{
 *                 "rightName":"视图1.显示副本",
 *                 "rightId":"-4870577323939312650.7501907135178623242"
 *             },{
 *                 "rightName":"视图2.显示",
 *                 "rightId":"4446327984963994825.-7712860852113945891"
 *             },{
 *                 "rightName":"视图2.显示副本",
 *                 "rightId":"4446327984963994825.8754783202338228751",
 *                 "checked":true
 *             },
 *             ....
 *         ],
 *         "pcRights":[
 *             {
 *                 "rightName":"视图1.显示",
 *                 "rightId":"-2762448264105868274.5083041261567559781",
 *                 "checked":true
 *             },{
 *                 "rightName":"视图1.显示副本",
 *                 "rightId":"-2762448264105868274.-1946547903106890507"
 *             },{
 *                 "rightName":"视图2.显示",
 *                 "rightId":"-7039226915670898288.-2954516812472302272"
 *             },{
 *                 "rightName":"视图2.显示副本",
 *                 "rightId":"-7039226915670898288.-7349493399914656523",
 *                 "checked":true
 *             },{
 *                 "rightName":"视图3.显示",
 *                 "rightId":"5858461992469744645.-6833381661532139829"
 *             },{
 *                 "rightName":"视图3.显示副本",
 *                 "rightId":"5858461992469744645.-2502778630185203843"
 *             },
 *             ...
 *         ]
 *     }
 * }
 * */
