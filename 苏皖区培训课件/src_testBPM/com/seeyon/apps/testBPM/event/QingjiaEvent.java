package com.seeyon.apps.testBPM.event;
import com.seeyon.apps.testBPM.constant.FormTemplateCode;
import com.seeyon.apps.testBPM.event.HrCommonEvent;
import com.seeyon.ctp.workflow.event.WorkflowEventData;

/**
 * @author Fangaowei
 * <pre>
 * 请假事件
 * </pre>
 * @date 2018年8月24日 下午1:57:14
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class QingjiaEvent extends HrCommonEvent{

    @Override
    public String getId() {
        return FormTemplateCode.qingjia.getCode();
    }

    @Override
    public String getLabel() {
        return "HR-请假流程";
    }

    @Override
    public void onProcessFinished(WorkflowEventData data) {
        invokeHrSystem("请假流程结束");
        super.onProcessFinished(data);
    }
    
    
}
