package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.edoc.bo.EdocSummaryBO;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.batch.bo.BatchCheckResult;
import com.seeyon.ctp.common.batch.bo.BatchResult;
import com.seeyon.ctp.common.batch.bo.BatchState;
import com.seeyon.ctp.common.batch.bo.FinishResult;
import com.seeyon.ctp.common.batch.exception.BatchException;
import com.seeyon.ctp.common.batch.manager.BatchAppHandler;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.HandWriteManager;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;

public class BatchEdocHandler extends BatchAppHandler {
    private static final Log LOGGER =LogFactory.getLog(BatchEdocHandler.class);
    private EdocManager edocManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
	private PermissionManager permissionManager;
	private EdocLockManager edocLockManager;
	private EdocSummaryManager edocSummaryManager;
	private IndexApi indexApi;
	private AppLogManager appLogManager;
	private WorkflowApiManager wapi;
	private HandWriteManager handWriteManager;
	private EdocRegisterManager edocRegisterManager;
	private RecieveEdocManager recieveEdocManager;
	private SuperviseManager superviseManager;
	private EdocFormManager edocFormManager;
	private CollaborationApi collaborationApi;
	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public RecieveEdocManager getRecieveEdocManager() {
		return recieveEdocManager;
	}

	public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
		this.recieveEdocManager = recieveEdocManager;
	}

	public EdocRegisterManager getEdocRegisterManager() {
		return edocRegisterManager;
	}

	public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
		this.edocRegisterManager = edocRegisterManager;
	}

	public HandWriteManager getHandWriteManager() {
		return handWriteManager;
	}

	public void setHandWriteManager(HandWriteManager handWriteManager) {
		this.handWriteManager = handWriteManager;
	}

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public AppLogManager getAppLogManager() {
		return appLogManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }

	public EdocSummaryManager getEdocSummaryManager() {
		return edocSummaryManager;
	}

	public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
		this.edocSummaryManager = edocSummaryManager;
	}

	public EdocLockManager getEdocLockManager() {
		return edocLockManager;
	}

	public void setEdocLockManager(EdocLockManager edocLockManager) {
		this.edocLockManager = edocLockManager;
	}

	public OrgManager getOrgManager() {
		return orgManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
    
    
	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}
	
	public void setEdocFormManager(EdocFormManager edocFormManager) {
		this.edocFormManager = edocFormManager;
	}
	
	public void setCollaborationApi(CollaborationApi collaborationApi) {
		this.collaborationApi = collaborationApi;
	}

	@Override
	public Object getComment(Integer attitude, String opinionContent, long affairId , long moduleId) throws BusinessException {
		EdocOpinion opinion = new EdocOpinion();
		opinion.setIdIfNew();
		opinion.affairIsTrack = false;
		opinion.isDeleteImmediate = false;
		opinion.isPipeonhole = false;
		opinion.setAffairId(affairId);
		
		if(attitude != null){
			opinion.setAttribute(attitude);
		}
		
		opinion.setContent(opinionContent);
		return opinion;
	}

	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.edoc;
	}

	@Override
	public FinishResult transFinishWorkItem(long affairId, long summaryId, Object comment, User user,Map<String,Object> param) throws BatchException {
		EdocSummary summary = null;
		FinishResult finishResult = new FinishResult();
		try {
			summary = edocManager.getEdocSummaryById(summaryId, false);
			
			finishResult.setSubject(summary.getSubject());
			
			//检查流程锁
			String[] checkProcessRet = checkProcess(summary.getProcessId(), user);
			if(!"true".equals(checkProcessRet[0])){
				finishResult.setErrorMsg(checkProcessRet[1]);
			    return finishResult;
			}
            
            CtpAffair affair = affairManager.get(affairId);
			
			if(!affairManager.isAffairValid(affair,true)){
				String errorMsg = AffairUtil.getErrorMsgByAffair(affair);
				finishResult.setErrorMsg(errorMsg);
				return finishResult;
			}
			
			EdocOpinion opinion = (EdocOpinion)comment;
			
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("opinion", opinion);
			params.put("workflow_node_condition_input",param.get("conditionsOfNodes"));
//			edocManager.transFinishWorkItem(affair,params);
		}
		catch (Exception e) {
			throw new BatchException(BatchState.Error.getCode(),e.getMessage());
        }finally {
			//解锁正文文单
    		try {
    			if(summary!=null){
    				wapi.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(AppContext.currentUserId()));
    			}
				wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
			} catch (BPMException e) {
				LOGGER.error("批处理解锁失败", e);
			}
		}
		
		return finishResult;
	}
	
	@Override
	public BatchResult checkApp(CtpAffair affair, Object object) throws BatchException {
		EdocSummaryBO summary = (EdocSummaryBO)object;
		BatchResult result = new BatchResult();
		result.setResultCode(BatchState.Normal.getCode());
		//校验内部文号
		result = this.checkEdocMark(summary);
		if(Integer.valueOf(BatchState.Normal.getCode()).equals(result.getResultCode())){
			result = this.checkEdocFormElementRequired(summary);
		}
		return result;
	}

    private BatchResult checkEdocMark(EdocSummaryBO summary) throws BatchException {
        BatchResult r = new BatchResult();
        r.setResultCode(BatchState.Normal.getCode());

        String serialNo = summary.getSerialNo();
        if (Strings.isBlank(serialNo) || summary.getEdocType() != EdocEnum.edocType.recEdoc.ordinal()) {
            return r;
        }
        // 判断内部文号是否已经存在
        User user = AppContext.getCurrentUser();
        int isExiste = edocSummaryManager.checkSerialNoExsit(String.valueOf(summary.getId()), serialNo, user.getLoginAccount());
        if (isExiste == 1) {
            r.setResultCode(BatchState.edocMarkExists.getCode());
            r.addMessage(ResourceUtil.getString("collaboration.batch.alert.notdeal.34"));
            return r;
        }
        
        return r;
    }
	
    /**
     * 校验公文单必填项
     * @param summary
     * @return
     * @throws BatchException
     */
    private BatchResult checkEdocFormElementRequired(EdocSummaryBO summary) throws BatchException{
    	BatchResult r = new BatchResult();
        r.setResultCode(BatchState.Normal.getCode());

        try {
			EdocSummary edocSummary = edocSummaryManager.getEdocSummaryById(summary.getId(), true, true);
			EdocForm edocForm = edocFormManager.getEdocForm(edocSummary.getFormId());
			Object[] msg = edocFormManager.getEdocFormElementRequiredMsg(edocForm,edocSummary);
			if (!(Boolean) msg[0]) {
				r.setResultCode(BatchState.edocFormElementRequired.getCode());
				r.addMessage(ResourceUtil.getString("collaboration.batch.alert.notdeal.35"));
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
        
        return r;
    }
    
	@Override
	public Map<String, String> checkAppPolicy(CtpAffair affair, Object object) throws BatchException, BusinessException {
		V3xOrgMember sender = orgManager.getMemberById(affair.getSenderId());
		TemplateManager templateManager = (TemplateManager)AppContext.getBean("templateManager");
		
		EdocSummaryBO summary = (EdocSummaryBO)object;
		Long flowPermAccountId = EdocHelper.getFlowPermAccountId(sender.getOrgAccountId(),summary.getTemplateId(),summary.getOrgAccountId(),templateManager);
		
		String nodePermissionPolicy = "shenpi";
		nodePermissionPolicy = affair.getNodePolicy();
		EnumNameEnum edocTypeEnum = EdocUtil.getEdocMetadataNameEnum(summary.getEdocType());
		Permission permission = permissionManager.getPermission(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
		NodePolicy policy = permission.getNodePolicy();
		
		return checkEdocPolicy(policy,edocTypeEnum,flowPermAccountId,nodePermissionPolicy);
	}
	
	private Map<String, String> checkEdocPolicy(NodePolicy policy,EnumNameEnum edocTypeEnum ,long flowPermAccountId,String nodePermissionPolicy) throws BatchException{
		Map<String, String> returnParam = new HashMap<String, String>();
		if(policy.getBatch() == null || policy.getBatch() != 1){//不允许批量处理
			throw new BatchException(BatchState.PolicyNotOpe.getCode());
		}
		//检查公文的节点是否有交换类型,如果有的话此节点也不能批处理.
		try {
			List<String> baseActions  = permissionManager.getBasicActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
			if(baseActions.contains("EdocExchangeType")){
				throw new BatchException(BatchState.PolicyNotOpe1.getCode());//交换类型的节点不允许批处理
			}
		} catch (BusinessException e) {
		    LOGGER.error("", e);
		}
		if(policy.getAttitude() == null){
			returnParam.put("attitude", "");
		}else{
			StringBuffer nodeattitude = new StringBuffer();
			
			String attitude = policy.getAttitude();
			DetailAttitude detailAttitude = policy.getDatailAttitude();
			if (Strings.isNotBlank(attitude)) {
        		String[] attitudeArr = attitude.split(",");
        		for (String att : attitudeArr) {
        			if (Strings.isNotBlank(nodeattitude.toString())) {
        				nodeattitude.append(",");
        			}
        			if ("haveRead".equals(att)) {
        				nodeattitude.append("haveRead|" + detailAttitude.getHaveRead());
        			} else if ("agree".equals(att)) {
        				nodeattitude.append("agree|" + detailAttitude.getAgree());
        			} else if ("disagree".equals(att)) {
        				nodeattitude.append("disagree|" + detailAttitude.getDisagree());
        			} 
        		}
        	}
			returnParam.put("attitude", nodeattitude.toString());
		}
		String baseAction = policy.getBaseAction();
		if(Strings.isNotBlank(baseAction)){
			if(baseAction.indexOf("Opinion") >=0){
				if(policy.getOpinionPolicy()!=null){
					returnParam.put("opinionPolicy", String.valueOf(policy.getOpinionPolicy()));
				}else if(policy.getDisAgreeOpinionPolicy()!=null){
					returnParam.put("opinionPolicy", "3"); 
				}else{
					returnParam.put("opinionPolicy", String.valueOf(0));
				}
			}else{
				returnParam.put("opinionPolicy", String.valueOf(2));
			}
		}
		return returnParam;
	}

	@Override
	public Map<String, String> disagreeStepBack(Map pMap) throws Exception {

		Map<String,String> resonMap  = new HashMap<String,String>();
		User user = AppContext.getCurrentUser();
		String _summaryId = (String)pMap.get("summaryId");
		String _affairId = (String)pMap.get("affairId");
		Long summaryId = Long.parseLong(_summaryId);
		Long affairId = Long.parseLong(_affairId);
		String _trackWorkflowType = (String)pMap.get("trackWorkflowType");
		boolean isRelieveLock = true;
		EdocSummary summary = null;
		Long lockUserId = edocLockManager.canGetLock(affairId,user.getId());
        if (lockUserId != null ) {
        	resonMap.put("error", "获取不到锁");
            return resonMap;
        }
        
		try {
			CtpAffair _affair = affairManager.get(affairId);
			// 补上退回时间
			StringBuilder sb = new StringBuilder();
			String errMsg = "";
			if (_affair.getState() != StateEnum.col_pending.key()) {
				errMsg = EdocHelper.getErrorMsgByAffair(_affair);
			}

			if ("".equals(errMsg)) {
				
				
	        	
				summary = edocSummaryManager.findById(summaryId);

				if (summary.getFinished()) {
					// OA-75460 公文交换类型节点处理后，封发节点后面的节点间回退时提示流程不能回退，应该可以回退
					Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, summary.getOrgAccountId());
					String[] result = edocManager.edocCanStepBack(String.valueOf(_affair.getSubObjectId()),
							String.valueOf(summary.getProcessId()), String.valueOf(_affair.getActivityId()),
							String.valueOf(summary.getCaseId()), String.valueOf(flowPermAccountId),
							EdocEnum.getEdocAppName(summary.getEdocType()));

					if (!"true".equals(result[0])) {
						errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
								"edoc.state.end.stepback.alert", "《" + summary.getSubject() + "》");
					}
				}
			}
			if (!"".equals(errMsg)) {
				// sb.append("<!--");
				resonMap.put("error", errMsg);
				return resonMap;
			}

			// 保存回退时的意见,附件�?
			//TODO affairID和policy没设置正确
			EdocOpinion oldOpinion = null;
			Set<EdocOpinion> opinions = summary.getEdocOpinions();
			if(opinions != null){
			    for(EdocOpinion o : opinions){
			        if(affairId.equals(o.getAffairId())){
			            oldOpinion = o;
			            break;
			        }
			    }
			}else{
			    oldOpinion = edocManager.findBySummaryIdAndAffairId(summaryId, affairId);
			}
			
			EdocOpinion signOpinion;
			if (oldOpinion != null) {
				signOpinion = oldOpinion;
			}else{
				signOpinion = new EdocOpinion();
				signOpinion.setIdIfNew();
			}
			
			String content = (String)pMap.get("collaboration.dealAttitude.disagree");
			if(Strings.isNotBlank(content)){
				signOpinion.setContent(content);
				signOpinion.setAttribute(3);
			}else{
				signOpinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
			}
			
			signOpinion.setAffairId(Long.valueOf(_affairId));
			signOpinion.setPolicy(_affair.getNodePolicy());
			signOpinion.isDeleteImmediate = false;
			signOpinion.affairIsTrack = false;
			signOpinion.setIsHidden(false);

			signOpinion.setNodeId(_affair.getActivityId());
			signOpinion.setCreateUserId(_affair.getMemberId()); 

			Map<String, Object> paramMap = new HashMap<String, Object>();
//			paramMap.put("affairState", request.getParameter("affairState"));
//			paramMap.put("affState", request.getParameter("affState"));
			paramMap.put("optionType", "3");
			paramMap.put("oldOpinion", oldOpinion);
			paramMap.put("trackWorkflowType", _trackWorkflowType);
			paramMap.put("edocSummary", summary);
			// true:成功回退 false:不允许回退
			boolean ok = edocManager.stepBack(summaryId, affairId, signOpinion, paramMap);
			// OA-68355
			EdocSummary summaryNew = edocSummaryManager.findById(summaryId);
			summary.setHasArchive(summaryNew.getHasArchive());
			summary.setDocMark(summaryNew.getDocMark());
			summary.setDocMark2(summaryNew.getDocMark2());
			summary.setState(summaryNew.getState());
			summary.setArchiveId(summaryNew.getArchiveId());
			// 更新当前待办人
			EdocHelper.updateCurrentNodesInfo(summary, true);
			String optionType = "3";
			// 1、3都表是只保留最后一条意见
			if ("1".equals(optionType) || "3".equals(optionType)) {
				signOpinion.setState(2);
				// 退回的时候要把以前是0的情况改为2
				edocManager.update(summaryId, user.getId(), signOpinion.getPolicy(), 2, 0);
			}

			if (AppContext.hasPlugin("index")) {
			    indexApi.update(summaryId, ApplicationCategoryEnum.edoc.getKey());
			}
			// 记录应用日志
			if (ok) {
				appLogManager.insertLog(user, 317, user.getName(), summary.getSubject());
			}

			_affair.setSummaryState(summary.getState());
			// 记录操作时间
			affairManager.updateAffairAnalyzeData(_affair);
			return resonMap;
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
		    
		    edocLockManager.unlock(affairId);
			
		    if (isRelieveLock) {
				this.wapi.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(AppContext.currentUserId()));
				this.wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId),
						String.valueOf(AppContext.currentUserId()));
			}
			try {
				// 解锁正文文单
				unLock(user.getId(), summary);
			} catch (Exception e) {
				LOGGER.error("解锁正文文单抛出异常：", e);
			}

		}

		return resonMap;
	
	}
	
	/**
	 * 解锁，公文提交或者暂存待办的时候进行解锁,与Ajax解锁一起，构成两次解锁，避免解锁失败，节点无法修改的问题出现
	 * 
	 * @param userId
	 * @param SUMMARYID
	 */
	private void unLock(Long userId, EdocSummary summary) {
		if (summary == null)
			return;
		String bodyType = summary.getFirstBody().getContentType();
		long summaryId = summary.getId();

		if (Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(bodyType) || Constants.EDITOR_TYPE_OFFICE_WORD.equals(bodyType)
				|| Constants.EDITOR_TYPE_WPS_EXCEL.equals(bodyType)
				|| Constants.EDITOR_TYPE_WPS_WORD.equals(bodyType)) {
			// 1、解锁office正文
			try {
				String contentId = summary.getFirstBody().getContent();

				handWriteManager.deleteUpdateObj(contentId);
			} catch (Exception e) {
				LOGGER.error("解锁office正文失败 userId:" + userId + " summaryId:" + summary.getId(), e);
			}
		} else {
			// 2、解锁html正文
			try {
				handWriteManager.deleteUpdateObj(String.valueOf(summaryId));
			} catch (Exception e) {
				LOGGER.error("解锁html正文失败 userId:" + userId + " summaryId:" + summaryId, e);
			}
		}
		// 3、解锁公文单
		try {
			edocSummaryManager.deleteUpdateObj(String.valueOf(summaryId), String.valueOf(userId));
		} catch (Exception e) {
			LOGGER.error("解锁公文单失败 userId:" + userId + " summaryId:" + summaryId, e);
		}
	}

	@Override
	public Map<String, String> disagreeStepStop(Map pMap) throws Exception {
		Map<String,String> resonMap = new HashMap<String,String>();
		User user = AppContext.getCurrentUser();
		String _summaryId = (String)pMap.get("summaryId");
		String _affairId = (String)pMap.get("affairId");
		
		Long summaryId = Long.parseLong(_summaryId);
		Long affairId = Long.parseLong(_affairId);
		
		boolean isRelieveLock = true;
		EdocSummary summary = null;
		 Long lockUserId = null;
		 lockUserId = edocLockManager.canGetLock(affairId,user.getId());
		 if (lockUserId != null ) {
	        	resonMap.put("error", "获取不到锁");
	            return resonMap;
	        }
		try {

			CtpAffair _affair = affairManager.get(affairId);
			// 当公文不是待办/在办的状态时，不能终止操作
			if (_affair.getState() != StateEnum.col_pending.key()) {
				String msg = EdocHelper.getErrorMsgByAffair(_affair);
				if (Strings.isNotBlank(msg)) {
					resonMap.put("error", msg);
					return resonMap;
				}
			}
			
			 if(_affair!=null){
                 String msg = collaborationApi.executeWorkflowBeforeEvent(_affair, "BeforeStop", null);
                 if(Strings.isNotBlank(msg)){
                     resonMap.put("error", msg);
                     return resonMap;                    
                 }
             }
			
			summary = edocManager.getEdocSummaryById(summaryId, true);
			// 保存终止时的意见,附件�
			EdocOpinion signOpinion = new EdocOpinion();
			
			String content = (String)pMap.get("collaboration.dealAttitude.disagree");
			if(Strings.isNotBlank(content)){
				signOpinion.setContent(content);
			}
			signOpinion.setAttribute(OpinionType.stopOpinion.ordinal());
			signOpinion.setPolicy(_affair.getNodePolicy());
			signOpinion.setAffairId(affairId);
			signOpinion.isDeleteImmediate = false;
			signOpinion.affairIsTrack = false;

			signOpinion.setIsHidden(false);
			signOpinion.setIdIfNew();

			long nodeId = -1;
			nodeId= _affair.getSubObjectId();
			signOpinion.setNodeId(nodeId);
			if(null != user && user.getId().longValue() != _affair.getMemberId().longValue()){
				signOpinion.setProxyName(user.getName());
			}
			signOpinion.setCreateUserId(_affair.getMemberId());

			
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("summaryId",summaryId);
			params.put("edocOpinion",signOpinion);
			
//			edocManager.transStepStop( affairId,params );

			return resonMap;
		} catch (Exception e) {
			LOGGER.error("公文终止时抛出异常：", e);
		} finally {
    		
		    edocLockManager.unlock(affairId);
    		
			if (isRelieveLock) {
				// 解锁正文文单
				if(summary != null){
					wapi.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(AppContext.currentUserId()));
				}				
				wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
			}
			try {
				unLock(user.getId(), summary);
			} catch (Exception e) {
				LOGGER.error("解锁正文文单抛出异常：", e);
			}
		}
		return resonMap;
	
	}

	@Override
	public Map<String, String> disagreeRepeal(Map pMap) throws Exception {
		
		Map<String,String> resonMap = new HashMap<String,String>();
		User user = AppContext.getCurrentUser();
		String _trackWorkflowType = (String)pMap.get("trackWorkflowType");;

		String _affairId = (String)pMap.get("affairId");
		String _summaryId = (String)pMap.get("summaryId");
		String content = (String)pMap.get("collaboration.dealAttitude.disagree");

		String docBack = "cancelColl";
		
		boolean isRelieveLock = true;
		String processId = "";
		Long summaryIdLong = null;
		EdocSummary summary = null;
		try {

			int result = 0;
			List<CtpAffair> doneList = null;

			CtpAffair _affair = null;
			// affair状态校验需要放到 获取processId之后进行，因为还需要在finally中进行解锁
			if (Strings.isNotBlank(_affairId)) {
				_affair = affairManager.get(Long.parseLong(_affairId));
				// 当公文不是待办/在办的状态时，不能撤销操作
				if (_affair.getState() != StateEnum.col_pending.key()
						|| _affair.getState() != StateEnum.col_sent.key()) {
					String msg = EdocHelper.getErrorMsgByAffair(_affair);
					if (Strings.isNotBlank(msg)) {
						pMap.put("error", msg);
						return pMap;
					}
				}
			}


			Long summaryId = Long.parseLong(_summaryId);
			summary = edocManager.getEdocSummaryById(summaryId, false);
			processId = summary.getProcessId();
			summaryIdLong = summary.getId();

			if (summary.getFinished()) {
				result = 1;
				String rs = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
						"edoc.state.end.alert1");
				resonMap.put("error",rs);
			}else {
				Map<String, Object> conditions = new HashMap<String, Object>();
				conditions.put("objectId", summary.getId());
				conditions.put("app", EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).key());
				List<Integer> states = new ArrayList<Integer>();
				states.add(StateEnum.col_done.key());
				states.add(StateEnum.col_stepStop.key());

				conditions.put("state", states);
				doneList = affairManager.getByConditions(null, conditions);
				if ((doneList != null && doneList.size() > 0) && "docBack".equals(docBack)) {// 处理中不能取回
					result = 3;// 已有人员
				} else {
					boolean isCancel = true;
	                String msg = collaborationApi.executeWorkflowBeforeEvent( _affair,"BeforeCancel", "");
	                if(Strings.isNotBlank(msg)){
	                    resonMap.put("error", msg);
	                    return resonMap;                    
	                }
		            
					// 收文撤销，取回，数据到达待分发。
					if (1 == summary.getEdocType()) {
						Map<String, Object> distributerConditions = new HashMap<String, Object>();
						distributerConditions.put("objectId", summary.getId());
						distributerConditions.put("app", ApplicationCategoryEnum.edocRecDistribute.key());
						List<Integer> distributerStates = new ArrayList<Integer>();
						distributerStates.add(StateEnum.col_done.key());
						distributerConditions.put("state", distributerStates);
						List<CtpAffair> distributerDoneList = affairManager.getByConditions(null,
								distributerConditions);
						for (int k = 0; k < distributerDoneList.size(); k++) {
							distributerDoneList.get(k).setState(StateEnum.col_pending.key());
							affairManager.updateAffair(distributerDoneList.get(k));
						}
						EdocRegister edocRegister = edocRegisterManager
								.findRegisterByDistributeEdocId(summaryId);
						if (edocRegister != null) {
							edocRegister.setDistributeDate(null);
							edocRegister.setDistributeEdocId(summaryId);
							edocRegister.setDistributeState(
							EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());// 将状态设置为"草稿"
							edocRegister.setIsRetreat(0);// 非退回
							edocRegisterManager.update(edocRegister);
							summary.setState(EdocConstant.flowState.deleted.ordinal());

						} else {
							EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(summaryId);
							if (record != null) {
								summary.setState(EdocConstant.flowState.deleted.ordinal());
							}
						}
					}

					EdocOpinion repealOpinion = new EdocOpinion();
					if (Strings.isNotBlank(_affairId)) {
						repealOpinion.setAffairId(Long.parseLong(_affairId));
					}
					String policy = _affair.getNodePolicy();
					repealOpinion.setPolicy(policy);

					repealOpinion.setNeedRepealRecord(_trackWorkflowType);
					result = edocManager.cancelSummary(user.getId(), summaryId, _affair, content,
							docBack, repealOpinion);

					String alertStr = "";

					if (result == 1) {
						// 流程已结束
						alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
								"edoc.state.end.alert", _affair.getSubject());
						resonMap.put("error",alertStr);
					}
				
					return resonMap;
				}
			}

			try {
				// 已发撤销后，需要删除已经发出去的全文检索文件
				if (AppContext.hasPlugin("index")) {
				    indexApi.delete(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
				}
			} catch (Exception e) {
				LOGGER.error("撤销公文流程，更新全文检索异常", e);
			}
			// 撤销流程事件
			CollaborationCancelEvent event = new CollaborationCancelEvent(this);
			event.setSummaryId(summary.getId());
			event.setUserId(user.getId());
			event.setMessage(content);
			EventDispatcher.fireEvent(event);
			// 发送消息给督办人，更新督办状态，并删除督办日志、删除督办记录、删除催办次数

			superviseManager.updateStatus2Cancel(summaryId);
		
			try {
				// 解锁正文文单
				unLock(user.getId(), summary);
			} catch (Exception e) {
				LOGGER.error("解锁正文文单抛出异常：", e);
			}

		} catch (Exception e) {
			LOGGER.error("撤销流程时抛出异常：", e);
		} finally {
			// 目前撤销只能 一次执行一条
			if (isRelieveLock) {
				wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
				wapi.releaseWorkFlowProcessLock(String.valueOf(summaryIdLong),
						String.valueOf(AppContext.currentUserId()));
				try {
					unLock(user.getId(), summary);
				} catch (Exception e) {
					LOGGER.error("解锁正文文单抛出异常：", e);
				}
			}
		}
		return resonMap;
	
	}

	/* (non-Javadoc)
	 * @see com.seeyon.apps.collaboration.batch.manager.BatchHandler#checkProcess(java.lang.String, com.seeyon.ctp.common.authenticate.domain.User)
	 */
	 private String[] checkProcess(String processId,User user) throws BatchException{
		String[] ret = {"true",""};
	    if(Strings.isBlank(processId)){
	        return ret;
	    }
	    WorkflowApiManager wapi = (WorkflowApiManager)AppContext.getBean("wapi");
	    try {
	        String[] re = wapi.checkWorkFlowProcessLock(processId, String.valueOf(user.getId()));
	        return re; 
        }
	    catch (BPMException e) {
            throw new BatchException(BatchState.Error.getCode(), ResourceUtil.getString("collaboration.batch.alert.notdeal.20"));
        }
	}

	
}
