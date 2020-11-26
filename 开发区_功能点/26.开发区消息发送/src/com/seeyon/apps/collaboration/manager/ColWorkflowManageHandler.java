package com.seeyon.apps.collaboration.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.dao.ColDao;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLog;
import com.seeyon.ctp.common.workflowmanage.handler.WorkflowManageHandler;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

public class ColWorkflowManageHandler implements WorkflowManageHandler {
    private static Log LOG = CtpLogFactory.getLog(ColWorkflowManageHandler.class);

	private OrgManager            orgManager;
	private ColManager colManager;
	private ColDao colDao;
	private CAPFormManager capFormManager;
	private AffairManager affairManager;
	private ColLockManager colLockManager;
	private ProcessLogManager processLogManager;
	private AppLogManager appLogManager;
	private ColMessageManager colMessageManager;

	public ColDao getColDao() {
		return colDao;
	}

	public void setColDao(ColDao colDao) {
		this.colDao = colDao;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public ColManager getColManager() {
		return colManager;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	public void setColLockManager(ColLockManager colLockManager) {
		this.colLockManager = colLockManager;
	}
	public void setColMessageManager(ColMessageManager colMessageManager) {
		this.colMessageManager = colMessageManager;
	}
	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.collaboration;
	}

	@SuppressWarnings("rawtypes")
	public List<WorkflowData> buildResult(List result, int flowstate)
			throws BusinessException {
		List<WorkflowData> models = new ArrayList<WorkflowData>();
		 User user=AppContext.getCurrentUser();
		if (result != null) {
           for (int i = 0; i < result.size(); i++) {
               ColSummary summary=(ColSummary) result.get(i);
               //开始组装最后返回的结果
               WorkflowData model = new WorkflowData();
               model.setVouch(summary.getVouch());
               model.setSummaryId(summary.getId().toString());
               String subject = ColUtil.showSubjectOfSummary(summary,false,-1,null);
               model.setSubject(subject);
               model.setImportantLevel(summary.getImportantLevel());
               model.setSendTime((Timestamp)summary.getStartDate());
               model.setProcessId(summary.getProcessId());
               model.setCaseId(summary.getCaseId());
               model.setDeadlineDateime(summary.getDeadlineDatetime());
               //设置流程期限显示内容
               model.setDeadlineDatetimeName(ColUtil.getDeadLineName(summary.getDeadlineDatetime()));
               model.setAdvanceRemind(summary.getAdvanceRemind());
               model.setNewflowType(summary.getNewflowType());  
               model.setResentTime(summary.getResentTime());  
               model.setForwardMember(summary.getForwardMember());  
               //设置当前处理人信息
               model.setCurrentNodesInfo(ColUtil.parseCurrentNodesInfo(summary));
               
               model.setFormAppId(summary.getFormAppid());
               model.setFormRecordId(summary.getFormRecordid());
               Map<String,String> defaultNodeMap =colManager.getColDefaultNode(summary.getOrgAccountId());
               model.setDefaultNodeName(defaultNodeMap.get("defaultNodeName"));
               model.setDefaultNodeLable(defaultNodeMap.get("defaultNodeLable"));
               Long templeteId = summary.getTempleteId();
               Long startMemberId = summary.getStartMemberId();
               String bodyType = summary.getBodyType();

               if(templeteId!= null){
               	model.setIsFromTemplete(true) ;
               	model.setTempleteId(templeteId);
               }
               if(summary.getState() == 1 || summary.getState() == 3){
               	model.setEndFlag(0); //0 - 结束
               }
               String appTypeName = "";
               if(ColUtil.isForm(bodyType)){
                   appTypeName = ResourceUtil.getString("application."+ApplicationCategoryEnum.form.key()+".label");
                   model.setNps("form");
                   model.setAppEnum(2);
               }else{
               	appTypeName = ResourceUtil.getString("application."+ApplicationCategoryEnum.collaboration.key()+".label");
                   model.setNps("default");
                   model.setAppEnum(1);
               }
               model.setAppType(appTypeName);
               model.setAppEnumStr(ApplicationCategoryEnum.collaboration.toString());
               
               String accName = "";
               String depName = "";
               V3xOrgMember member = null;
               try {
                   member =orgManager.getMemberById(startMemberId);
                   if(member != null){
                       V3xOrgDepartment dep = orgManager.getDepartmentById(member.getOrgDepartmentId());
                       if(dep != null){
                           depName = dep.getName();
                       }
                       V3xOrgAccount acc =null;
                       if(dep!=null){
                    	   acc =orgManager.getAccountById(dep.getOrgAccountId());
                       }else{
                    	   acc =orgManager.getAccountById(member.getOrgAccountId());
                       }
                       if(acc != null){
                           accName = acc.getShortName();
                           model.setAccountId(acc.getId());
                       }
                       if(!member.getOrgAccountId().equals(user.getAccountId())) {
                    	   model.setInitiator(member.getName() + "("+accName+")");
                       } else {
                    	   model.setInitiator(member.getName());
                       }
                   }
               } catch (Exception e) {
                   throw new BusinessException(e);
               }
               if(user.isGroupAdmin() || (member !=null && member.getOrgAccountId()!=null && !member.getOrgAccountId().equals(user.getAccountId()))){
                   model.setDepName(depName+"("+accName+")");
               }else{
                   model.setDepName(depName);
               }
               models.add(model);
           }
       }
		return models;
	}

	  
	@Override
	public String transRepal(Map<String,Object> map) throws BusinessException {
		return colManager.transRepal(map);
	}
	
	@Override
	public String transStepStop(Map<String, Object> map) throws BusinessException {
		return colManager.transStepStop(map);
	}

	@Override
	public FlipInfo selectPageWorkflowDataByCondition(Map<String,Object> conditionParam, FlipInfo fi) throws BusinessException {
		List result = null;
		String currentNodesInfo = (String) conditionParam.get("currentNodesInfo");
    	if(Strings.isNotBlank(currentNodesInfo)) {
    		List<Long> objectIds =  (List<Long>) conditionParam.get("objectIds");
    		result = colManager.findColSummarysByIds(objectIds);
    	}else {
    		result=colDao.selectPageWorkflowDataByCondition(conditionParam, fi);
    	}
		
		String flow = (String)conditionParam.get("flowstate");
        int flowstate = 0;
        if(Strings.isNotBlank(flow)){
            flowstate = Integer.parseInt(flow);
        }
		List<WorkflowData> models = buildResult(result, flowstate);
		fi.setData(models);
		return fi;
	}

	
	@Override
	public List<WorkflowData> selectWorkflowDataByCondition(Map<String,Object> conditionParam) throws BusinessException {
		List result=colDao.selectWorkflowDataByCondition(conditionParam);
		String flow = (String)conditionParam.get("flowstate");
        int flowstate = 0;
        if(Strings.isNotBlank(flow)){
            flowstate = Integer.parseInt(flow);
        }
		List<WorkflowData> models = buildResult(result, flowstate);
		return models;
	}

	@Override
	public String validateReliveProcess(Map<String, Object> map) throws BusinessException {
		//正常的和终止状态的流程 1
		String result = "";
		String summaryId = String.valueOf(map.get("summaryId"));
		ColSummary colSummary = null;
		if(Strings.isNotEmpty(summaryId)){
			colSummary = colDao.getColSummaryById(Long.parseLong(summaryId));
//			if(CollaborationEnum.flowState.terminate.equals(colSummary.getState()) || CollaborationEnum.flowState.finish.equals(colSummary.getState()) ){
				//--接下来验证流程的核定节点和 -表单的回调
				if(ColUtil.isForm(colSummary.getBodyType())){
				    if(capFormManager.validateFormFlowCanRelive(colSummary.getFormAppid())){
                        result = "true";
                    }else{
                        result =  ResourceUtil.getString("supervise.process.resurrection.from.check");
                    }
				}else{
				    result = "true";
				}
//			}
		}else{
			result = "true";
		}
		return result;
	}
	
	@Override
	public void onChangeProcessState(String processId) throws BusinessException {
	    
	    ColSummary summary= colManager.getColSummaryByProcessId(Long.valueOf(processId));
	    
	    ColUtil.updateCurrentNodesInfo(summary);
	    
	    colManager.updateColSummary(summary);
	}
	
	@Override
	public String getMyManagerTemplateCount() throws BusinessException{
	    String result=colDao.getMyManagerTemplateCount();
	    return result;
	}

	@Override
	public String updateAppointStepBack(Long currentAffairId, String stepBackNodeId, String submitType, String comment)
			throws BusinessException {
		Map<String, Object> param = new HashMap<String,Object>();
		User user = AppContext.getCurrentUser();
		CtpAffair affair  = affairManager.get(currentAffairId);
		 param.put("summaryId", affair.getObjectId().toString());
		 param.put("workitemId",affair.getSubObjectId().toString());
		 param.put("caseId",affair.getCaseId().toString());
		 param.put("processId", affair.getProcessId());
	     param.put("theStepBackNodeId", stepBackNodeId);
	     param.put("submitStyle", submitType);
	     param.put("activityId",affair.getActivityId().toString());
	     param.put("currentAffairId" ,currentAffairId.toString());
	     param.put("isWfTrace", "1");//默认追溯
	     param.put("isCircleBack","false");
         param.put("affair", affair);
         param.put("user", user);
         param.put("affairId", affair.getId().toString());
         param.put("isAdminStepBack", "true");
		colManager.updateAppointStepBack(param);
		return null;
	}

	@Override
	public String replaceWorkitem(CtpAffair affair, V3xOrgMember newMember, User currentUser)
			throws BusinessException {
		
    	Long affairId = affair.getId();
    	Long oldMemberId = affair.getMemberId();
    	V3xOrgMember oldMember = orgManager.getMemberById(oldMemberId);
    	boolean isLock = false;
    	try {
        	isLock = colLockManager.canGetLock(affairId);
        	if (!isLock) {
        		LOG.error(AppContext.currentAccountName()+"不能获取到map缓存锁，不能执行操作finishWorkItem,affairId"+affairId);
        		return null;
        	}
        	
        	//执行替换的操作
        	Map<String, Object> param = colManager.repalceWorkitem(affair, affair.getNodePolicy(), currentUser, newMember, false);
        	String errorMsg = (String) param.get("errorMsg");
        	if(Strings.isNotBlank(errorMsg)) {
        		return errorMsg;
        	}
        	
        	//获取操作人姓名
        	String handlerMemberName = currentUser.getName();
        	if(!currentUser.isAdmin()) {
        		handlerMemberName = ResourceUtil.getString("sys.role.rolename.FormAdmin")+"("+currentUser.getName()+")";//表单管理员(张三)
    		}
        	
        	List<CtpAffair> newAffairs = (List<CtpAffair>) param.get("newAffairs");
        	if(Strings.isNotEmpty(newAffairs)) { 
        		CtpAffair newCtpAffair = newAffairs.get(0);
        		//写流程日志和应用日志
        		ProcessLog pLog = new ProcessLog();
        		pLog.setProcessId(Long.parseLong(newCtpAffair.getProcessId()));
        		pLog.setActivityId(newCtpAffair.getActivityId());
        		pLog.setActionId(ProcessLogAction.batchHandover.getKey());
        		pLog.setActionUserId(currentUser.getId());
        		String actionDesc = ResourceUtil.getString("workflow.processLog.action.desc5", handlerMemberName, oldMember.getName(), newMember.getName());
        		pLog.setDesc(actionDesc);
        		processLogManager.insertLog(pLog);
        		//{0}将协同中的人员《{1}》替换成{2}
        		appLogManager.insertLog(currentUser, 176,handlerMemberName, newCtpAffair.getSubject(),oldMember.getName(),newMember.getName());
        		//发送消息
        		colMessageManager.sendMessage4ReplaceNode(currentUser, newCtpAffair, oldMember, handlerMemberName);
        	}
        	
		} finally {
			if(isLock){
				colLockManager.unlock(affairId);
			}
		    
		}
		
		
		return null;
	}
}
