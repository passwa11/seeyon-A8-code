package com.seeyon.apps.testBPM.event;
import com.seeyon.apps.testBPM.constant.FormTemplateCode;
import com.seeyon.ctp.workflow.event.WorkflowEventData;

/**
 * @author Fangaowei
 * <pre>
 * 出差事件
 * </pre>
 * @date 2018年8月24日 下午1:57:14
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class ChuchaiEvent extends HrCommonEvent{

    @Override
    public String getId() {
        return FormTemplateCode.chuchai.getCode();
    }

    @Override
    public String getLabel() {
        return "HR-出差流程-testBPM";
    }

    @Override
    public void onProcessFinished(WorkflowEventData data) {
        invokeHrSystem("出差流程结束");
        super.onProcessFinished(data);
    }
    
}
