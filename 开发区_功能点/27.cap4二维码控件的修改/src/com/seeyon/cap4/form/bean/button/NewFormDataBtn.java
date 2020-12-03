package com.seeyon.cap4.form.bean.button;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormSaveAsBean;
import com.seeyon.cap4.form.modules.importandexport.BusinessDataBean;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by weijh on 2018-12-26.
 * 应用绑定新建按钮实现类
 */
public class NewFormDataBtn extends CommonBtn {
    private static final Log logger = CtpLogFactory.getLog(NewFormDataBtn.class);

    @Override
    public void init() {
        this.setPluginId("newFormDataBtn");
        //@todo 图标等ue提供之后再修改icon
        this.setIcon("cap-icon-custom-button");
        BtnParamDefinition targetFormInfoParam = new BtnParamDefinition();
        targetFormInfoParam.setDialogUrl("apps_res/cap/customCtrlResources/newFormDataBtnResources/html/setTargetFormInfo.html");
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
        return "8714694276131171133";
    }

    @Override
    public String getNameSpace() {
        return "customBtn" + this.getKey();
    }

    @Override
    public String getText() {
        return ResourceUtil.getString("com.cap.btn.newFormDataBtn.text");
    }

    @Override
    public String getPCInjectionInfo() {
        return "{\"path\":\"apps_res/cap/customCtrlResources/newFormDataBtnResources/\",\"jsUri\":\"js/" + this.getNameSpace() + ".umd.min.js\",\"initMethod\":\"init\",\"nameSpace\":\"" + this.getNameSpace() + "\"}";
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
            if(customParamMap.size() > 0){
                resultMap.putAll(customParamMap);
                Map<String, Object> targetFormInfo = (Map<String, Object>) customParamMap.get("targetFormInfo");
                Map<String, Object> targetFormMap = (Map<String, Object>) targetFormInfo.get("targetForm");
                String formId = (String) targetFormMap.get("formId");
                targetFormMap.put("formId", businessDataBean.getRealId4Export(Long.valueOf(formId)).toString());
                String bindId = (String) targetFormMap.get("bindId");
                targetFormMap.put("bindId", businessDataBean.getRealId4Export(Long.valueOf(bindId)).toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void importInfoAfterBizImport(FormBean formBean, String customParam, BusinessDataBean businessDataBean, Map<String, Object> btnInfoMap) {
        if (Strings.isNotEmpty(customParam)) {
            Map<String, Object> customParamMap = (Map<String, Object>) JSONUtil.parseJSONString(customParam);
            if(customParamMap != null && customParamMap.size() > 0){
                btnInfoMap.putAll(customParamMap);
                Map<String, Object> targetFormInfo = (Map<String, Object>) customParamMap.get("targetFormInfo");
                Map<String, Object> targetFormMap = (Map<String, Object>) targetFormInfo.get("targetForm");
                int targetType = Integer.parseInt(String.valueOf(targetFormMap.get("targetType")));
                if (targetType == 0) {
                    //业务内表单新建
                    String formId = (String) targetFormMap.get("formId");
                    String bindId = (String) targetFormMap.get("bindId");
                    Long oldFormId = Long.valueOf(formId);
                    Long oldBindId = Long.valueOf(bindId);
                    if (businessDataBean.isUpgrade()) {
                        oldFormId = businessDataBean.getRealId4Export(oldFormId);
                        oldBindId = businessDataBean.getRealId4Export(oldBindId);
                    }
                    Long newFormId = businessDataBean.getNewIdByOldId(oldFormId);
                    Long newBindId = businessDataBean.getNewIdByOldId(oldBindId);
                    if(newFormId != null && newBindId != null){
                        targetFormMap.put("formId", newFormId.toString());
                        targetFormMap.put("bindId", newBindId.toString());
                    }else{
                        logger.info("应用安装时因找不到表单，所以该自定义按钮设置清空，当前表单ID："+formBean.getId()+">>按钮设置表单formId="+formId);
                        btnInfoMap.clear();
                        btnInfoMap.putAll(Collections.<String, Object>emptyMap());
                    }
                } else {
                    //业务外表单新建
                    Long formId = ParamUtil.getLong(targetFormMap,"formId",0L);
                    Long bindId = ParamUtil.getLong(targetFormMap,"bindId",0L);
                    Long newFormID = businessDataBean.getNewIdByOldId(formId);
                    Long newBindId=businessDataBean.getNewIdByOldId(bindId);
                    if(newFormID != null && newBindId != null){
                        targetFormMap.put("formId", String.valueOf(newFormID));
                        targetFormMap.put("bindId",String.valueOf(newBindId));
                    } else{
                        logger.info("应用安装时因找不到表单，所以该自定义按钮设置清空，当前表单ID："+formBean.getId()+">>按钮设置表单formId="+formId);
                        btnInfoMap.clear();
                        btnInfoMap.putAll(Collections.<String, Object>emptyMap());
                    }
                }
            }
        } else {
            btnInfoMap.putAll(new HashMap<String, Object>());
        }
    }

    @Override
    public void otherSave(FormSaveAsBean formSaveAsBean, FormBean formBean, Map<String, Object> btnInfoMap){
        Long saveToBizId = formSaveAsBean.getSaveToBizId();
        Long oldBizId = formSaveAsBean.getOldBizId();
        //另存为选择为空说明是存为单表
        if(null == saveToBizId){
            //原来是应用中的表，清空
            if(null != oldBizId){
                btnInfoMap.put("customParam","");
            }
        }else{//存为应用中的表单
            if(null == oldBizId){//原来是单表
                btnInfoMap.put("customParam","");
            }else{//原来是应用中的表单
                if(!saveToBizId.equals(oldBizId)){//跨应用另存，清空
                    btnInfoMap.put("customParam","");
                }
            }
        }

    }
}
/*
 * targetFormInfo的Json格式
 * {
 *     "targetFormInfo":{
 *         "targetForm":{
 *             "formId":"-6408353118820217732",
 *             "formType":"6",
 *             "bindName":"123456784副本副本",
 *             "bindId":"-3998234970821670250",
 *             "targetType":0
 *         },
 *         "onlyFormson":false,
 *         "mappedFields":[
 *             {
 *                 "src":{"name":"field0001"},
 *                 "dst":{"name":"field0001"}
 *             },{
 *                 "src":{"name":"field0005"},
 *                 "dst":{"name":"field0005"}
 *             },{
 *                 "src":{"name":"field0007"},
 *                 "dst":{"name":"field0008"}
 *             },
 *             ...
 *         ]
 *     }
 * }
 * */