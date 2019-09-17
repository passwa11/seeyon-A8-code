package com.seeyon.apps.govdoc.manager.impl;

import java.util.List;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.SendType;
import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovdocLogManagerImpl implements GovdocLogManager {
	
	//private static final Log LOGGER = LogFactory.getLog(GovdocManagerImpl.class);
	
	private ProcessLogManager processLogManager;
	private AppLogManager appLogManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	private WorkflowApiManager wapi;
	
	/**
	 * 保存公文发送流程日志及应用日志
	 * @param info
	 * @param sendType
	 * @throws BusinessException
	 */
	@Override
	public void saveSendLogs(GovdocNewVO newVo, SendType sendType) throws BusinessException {
		// 审计日志,无流程表单表单触发可能为空
		if(newVo.getWfInfo() != null) {
			boolean isSpecialBacked = newVo.isSpecialBacked() || newVo.isSpecialBackReturn();
			User user = newVo.getCurrentUser();
			String[] toMmembers = newVo.getWfInfo().getNextNodeNames();
			if (sendType == EdocConstant.SendType.forward) {
				appLogManager.insertLog(user, GovdocAppLogAction.EDOC_FORWARD.key(), user.getName(), newVo.getSummary().getSubject());
			} else if (!isSpecialBacked) {
				appLogManager.insertLog(user, GovdocAppLogAction.EDOC_SEND.key(), user.getName(), newVo.getSummary().getSubject());
			}
			// 记录流程日志
			if (isSpecialBacked) {
				processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(newVo.getSummary().getProcessId()), -1l, ProcessLogAction.colStepBackToResend, toMmembers);
			} else {
				//记录提交操作日志
				List<ProcessLogDetail> allProcessLogDetailList= wapi.getAllWorkflowMatchLogAndRemoveCache();
				List<ProcessLogDetail> processLogDetails= newVo.getProcessLogDetails();
	            if(null!=processLogDetails && !processLogDetails.isEmpty()){
	            	allProcessLogDetailList.addAll(processLogDetails);
	            }
				this.insertProcessLog(AppContext.getCurrentUser(), Long.parseLong(newVo.getSummary().getProcessId()), -1l, ProcessLogAction.sendColl,allProcessLogDetailList,toMmembers);
				//processLogManager.insertLog(user, Long.parseLong(newVo.getSummary().getProcessId()), -1l, ProcessLogAction.sendColl, toMmembers);
			}
		}
	}
	
	/**
	 * 保存公文公送(交换)日志
	 * @param info
	 * @param sendtype
	 * @param user
	 * @param summary
	 * @param isSpecialBackReMe
	 * @param isspecialbackrerun
	 * @param isForm
	 * @param toMmembers
	 */
	@Override
	public void saveSendExchangeLogs(GovdocNewVO info, SendType sendtype, User user, EdocSummary summary, 
			boolean isSpecialBackReMe, boolean isspecialbackrerun, boolean isForm,
			String toMmembers) {
		if(sendtype == EdocConstant.SendType.forward) {
    		appLogManager.insertLog(user, GovdocAppLogAction.EDOC_FORWARD.key(), user.getName(), summary.getSubject());  
    	} else if(!isSpecialBackReMe && !isspecialbackrerun) {
    		appLogManager.insertLog(user, GovdocAppLogAction.EDOC_SEND_EXCHANGE.key(), user.getName(), info.getSummary().getSubject());
    	}
        
        //记录流程日志
        if(isSpecialBackReMe || isspecialbackrerun){
            processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(info.getSummary().getProcessId()),-1l, ProcessLogAction.colStepBackToResend, toMmembers);
        }else {
        	List<ProcessLogDetail> allProcessLogDetailList= null;
        	if(sendtype == EdocConstant.SendType.immediate){
        		allProcessLogDetailList = govdocWorkflowManager.getAllWorkflowMatchLogAndRemoveCache(info.getWorkflowNodeConditionInput());
        	}else{
        		allProcessLogDetailList = govdocWorkflowManager.getAllWorkflowMatchLogAndRemoveCache();
        	}
        	if (isForm){
        		processLogManager.insertLog(user, Long.parseLong(info.getSummary().getProcessId()), -1l, ProcessLogAction.sendForm,allProcessLogDetailList, (String)toMmembers);
        	}else {
        		processLogManager.insertLog(user, Long.parseLong(info.getSummary().getProcessId()), -1l, ProcessLogAction.sendColl,allProcessLogDetailList, (String)toMmembers);
        	}
    	}
	}
	
	/**
	 * 公文回退-记录公文流程及应用日志
	 * @param dealVo
	 * @throws BusinessException
	 */
	@Override
	public void saveStepbackLog(GovdocDealVO dealVo) throws BusinessException {
		// 记录流程日志
		processLogManager.insertLog(dealVo.getCurrentUser(), Long.parseLong(dealVo.getSummary().getProcessId()),  dealVo.getAffair().getActivityId(), ProcessLogAction.stepBack, dealVo.getComment().getId(), dealVo.getNodePolicy());
		
		// 记录应用日志
		appLogManager.insertLog(dealVo.getCurrentUser(), GovdocAppLogAction.EDOC_STEP_BACK.key(), dealVo.getCurrentUser().getName(),  dealVo.getSummary().getSubject());
	}

	/**
	 * 公文指定回退-记录公文流程及应用日志
	 * @param dealVo
	 * @throws BusinessException
	 */
	@Override
	public void saveAppointStepbackLog(GovdocDealVO dealVo) throws BusinessException {
		ProcessLogAction logAction = null;
        if("start".equals(dealVo.getSelectTargetNodeId())) {//指定回退给发起者节点
        	logAction = ProcessLogAction.stepBackToSender;
        } else {
        	logAction = ProcessLogAction.colStepBackToPoint;
        }
        processLogManager.insertLog(dealVo.getCurrentUser(), Long.parseLong(dealVo.getProcessId()), dealVo.getActivityId(), logAction, dealVo.getSelectTargetNodeName());
        
        int backStrategy = 346;//169:直接提交给我,168:流程重走
        if ("1".equals(dealVo.getSubmitStyle())) {//直接提交给我--168
   		 	backStrategy = 345;
   	 	}
        appLogManager.insertLog(dealVo.getCurrentUser(), backStrategy, dealVo.getCurrentUser().getName(), dealVo.getSummary().getSubject(), dealVo.getSelectTargetNodeName());
	}
	
	/**
	 * 公文撤销日志
	 * @param baseVo
	 * @throws BusinessException
	 */
	@Override
	public void saveCancelLog(GovdocBaseVO baseVo) throws BusinessException {
		appLogManager.insertLog(baseVo.getCurrentUser(), GovdocAppLogAction.EDOC_CACEL.key(), baseVo.getCurrentUser().getName(), baseVo.getSummary().getSubject());
	
		processLogManager.insertLog(baseVo.getCurrentUser(), Long.parseLong(baseVo.getSummary().getProcessId()), -1L, ProcessLogAction.cancelColl);
	}
	
	/**
	 * 保存附件修改日志
	 * @param dealVo
	 * @throws BusinessException
	 */
	@Override
	public void saveAttUpdateLog(GovdocBaseVO baseVo) throws BusinessException {
		//修改正文后记录流程日志
	    processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(baseVo.getSummary().getProcessId()), baseVo.getAffair().getActivityId(), ProcessLogAction.processColl, baseVo.getComment().getId(), String.valueOf(ProcessLogAction.ProcessEdocAction.modifyBody.getKey()));
	
	    //如果修改正文的时候导入了新文件则记录应用日志
	    if(null != baseVo.getIsLoadNewFile() && 1 == baseVo.getIsLoadNewFile().intValue()) {
	    //if("1".equals(colSummaryDomian.get("isLoadNewFile"))){
	        appLogManager.insertLog(baseVo.getCurrentUser(), GovdocAppLogAction.EDOC_CONTENT_EDIT_LOADNEWFILE.key(),  baseVo.getCurrentUser().getName(), baseVo.getAffair().getSubject());
	    }
	}
	
	
	/**
	 * 保存流程日志
	 * @param user
	 * @param processeId
	 * @param activityId
	 * @param action
	 * @param params
	 */
	@Override
	public void insertProcessLog(User user, long processeId, long activityId, ProcessLogAction action, String... params) {
		processLogManager.insertLog(user, processeId, activityId, action, params);
	}
	@Override
	public void insertProcessLog(User user, long processeId, long activityId, ProcessLogAction action, Long commentId,String... params) {
		processLogManager.insertLog(user, processeId, activityId, action, commentId, params);
	}
	@Override
	public void insertProcessLog(List<ProcessLog> logs) {
		processLogManager.insertLog(logs);
	}
	@Override
	public void updateByHQL(Long newId, Long oldID) {
		processLogManager.updateByHQL(newId, oldID);
	}
	@Override
	public void insertProcessLog(User user, long processeId, long activityId, ProcessLogAction action,List<ProcessLogDetail> allProcessLogDetailList, String... params) {
		processLogManager.insertLog(AppContext.getCurrentUser(), processeId, activityId, ProcessLogAction.commit,allProcessLogDetailList,params);
	}
	@Override
	public void deleteProcessLog(Long processId) {
		processLogManager.deleteLog(processId);
	}
	@Override
	public List<ProcessLog> getLogsByProcessIdAndActionId(Long processId,List<Integer> actionIds) {
		return processLogManager.getLogsByProcessIdAndActionId(processId, actionIds);
	}
	
	/**
	 * 保存应用日志
	 * @param user
	 * @param actionId
	 * @param params
	 */
	@Override
    public void insertAppLog(User user, Integer actionId, String... params) {
		appLogManager.insertLog(user, actionId, params);
	}
	
	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

}
