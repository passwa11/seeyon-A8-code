package com.seeyon.apps.testBPM.node;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.common.kit.HttpKit;
import com.seeyon.apps.common.kit.JsonKit;
import com.seeyon.apps.testBPM.kit.ParseMasterBean;
import com.seeyon.apps.testBPM.po.Response;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.workflow.supernode.BaseSuperNodeAction;
import com.seeyon.ctp.workflow.supernode.SuperNodeResponse;

/**
 * @author Fangaowei
 * <pre>
 * 
 * </pre>
 * @date 2018年8月24日 下午3:01:42
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class HrNode extends BaseSuperNodeAction {
    
    /**
     * 节点动作撤销。
     */
    public void cancelAction(String token, String activityId) {
        System.out.println("撤销");
    }
    
    /** 
     * Description:
     * <pre>提交事件,人员干预</pre>
     * @param token
     * @param activityId
     * @param params
     * @return
     * @see com.seeyon.ctp.workflow.supernode.BaseSuperNodeAction#confirmAction(java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public SuperNodeResponse confirmAction(String token, String activityId, Map<String, Object> params) {
        return executeAction(token, activityId, params);
    }
    
    
    
    /** 
     * Description:
     * <pre>和confirmAction差不多</pre>
     * @param token
     * @param activityId
     * @param params
     * @return
     * @see com.seeyon.ctp.workflow.supernode.BaseSuperNodeAction#executeAction(java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    public SuperNodeResponse executeAction(String token, String activityId, Map<String, Object> params) {
        SuperNodeResponse response = new SuperNodeResponse();
        Map<String, Object> data = getFormData(params);
        String content = "";
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                FormDataMasterBean formDataBean = (FormDataMasterBean) data.get("formDataBean");
                content = JsonKit.toJson(ParseMasterBean.parse(formDataBean));
            } catch (Exception e) {
                
            }
            map.put("content", content);
            map.put("token", token);
            map.put("affairId", activityId);
            // 调用第三方接口传数据
            String msg = HttpKit.post("http://127.0.0.1:8888/saveId", map);
            //String msg = HttpKit.get("http://127.0.0.1:8888/saveId?token=" + token + "&affairId=" + activityId);
            Response res = JsonKit.parse(msg, Response.class);
            response.setReturnCode(res.getReturnCode());
            response.setReturnMsg(res.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*response.setReturnCode(0);
        response.setReturnMsg("等待第三方处理");*/
        return response;
    }
    

    @Override
    public void cancelAction(String token, String activityId, Map<String, Object> params) {
        
    }

    @Override
    public String getNodeId() {
        return "hrNode";
    }

    @Override
    public String getNodeName() {
        return "HR审批节点";
    }

    @Override
    public int getOrder() {
        return 0;
    }
    
    /**
     * 获取表单数据
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFormData(Map<String, Object> params) {
        return (Map<String, Object>) params.get("CTP_FORM_DATA");
    }
}
