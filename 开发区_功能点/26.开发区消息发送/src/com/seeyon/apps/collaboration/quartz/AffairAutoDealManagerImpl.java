package com.seeyon.apps.collaboration.quartz;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMSeeyonPolicy;

public class AffairAutoDealManagerImpl implements AffairAutoDealManager{
	private static Log LOG = CtpLogFactory.getLog(AffairAutoDealManagerImpl.class);
	private WorkflowApiManager wapi;
	private CAPFormManager capFormManager;
	 
	
	public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
	
	@Override
	public void transAutoDeal() throws BusinessException {
		// TODO Auto-generated method stub
		
	}
	/**
     * B节点是核定、新闻审批、公告审批、封发（带交换类型的节点）时，不能跳过。
	 * B节点有必填项时不能自动跳过。
	 * B节点后有手动分支和非强制分支和选人不能跳过。强制分支不需要手动选择的可以跳过。
	 * B节点有子流程不能跳过。
	 * 对于被加签人员与加签人员同一个人时，和下一节点需要选人但又选择了自己的情况，不支持跳过
	 * B节点处理意见必填时不能跳过
	 * @throws BPMException 
     */
    private boolean checkCanAutoDeal(CtpAffair affair,ColSummary summary) throws BPMException{
    	
        boolean canAutoDeal = true;
    	//B节点是核定、新闻审批、公告审批、封发（带交换类型的节点）时，不能跳过。
    	
	    if("vouch".equalsIgnoreCase(affair.getNodePolicy()) || "formaudit".equals(affair.getNodePolicy())){
		    return false;
	    }
	    
        //B节点有必填项时不能自动跳过。
        if(ColUtil.isForm(affair.getBodyType())){//表单类型的数据
            
            boolean isFormMustWrite = capFormManager.hasNotNullableField(summary.getFormAppid(), affair.getMultiViewStr());
            if(isFormMustWrite){
            	return false;
            }
            LOG.info("isFormMustWrite:"+isFormMustWrite +",入参：FormAppid():"+summary.getFormAppid()+",MultiViewStr:"+affair.getMultiViewStr());
            boolean isPreNewFlowFinish =  wapi.hasUnFinishedNewflow(summary.getProcessId(),String.valueOf(affair.getActivityId()));
            if(isPreNewFlowFinish){
            	LOG.info("has unfinished new flow,can't auto deal !");
            	return false;
            }
        }
        
        String processId  = summary.getProcessId();
        BPMProcess bpmProcess= wapi.getBPMProcess(processId);
        BPMActivity bpmActivity= null;
        BPMSeeyonPolicy bpmSeeyonPolicy= null;
        if(bpmProcess != null){
            bpmActivity= bpmProcess.getActivityById(affair.getActivityId().toString());
        }
        if(bpmActivity != null){
            bpmSeeyonPolicy= bpmActivity.getSeeyonPolicy();
        }
        boolean isFromTemplate= false;
		isFromTemplate = summary.getTempleteId() != null && summary.getTempleteId().longValue() != -1;
        boolean hasNewflow = isFromTemplate && bpmSeeyonPolicy != null && "1".equals(bpmSeeyonPolicy.getNF());
        if(hasNewflow){
            LOG.info("该协同待办需要触发新流程或者前面节点触发的新流程还没有结束或者后面节点需要进行分支匹配、选择执行人或人员不可用，不允许执行自动跳过操作。colSummaryId:="+summary.getId()+";  affairId:="+affair.getId());
            return false;
        }
        
        
        
        return true;
    }
}
