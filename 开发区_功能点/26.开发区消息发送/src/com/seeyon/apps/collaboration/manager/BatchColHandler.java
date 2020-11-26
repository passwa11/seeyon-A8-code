package com.seeyon.apps.collaboration.manager;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.dee.api.CollaborationFormBindEventListener;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.batch.bo.BatchCheckResult;
import com.seeyon.ctp.common.batch.bo.BatchResult;
import com.seeyon.ctp.common.batch.bo.BatchState;
import com.seeyon.ctp.common.batch.bo.FinishResult;
import com.seeyon.ctp.common.batch.exception.BatchException;
import com.seeyon.ctp.common.batch.manager.BatchAppHandler;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BatchColHandler extends BatchAppHandler {
	private static Log LOG = CtpLogFactory.getLog(BatchColHandler.class);
	private ColManager colManager;
	private AffairManager affairManager;
	private ColPubManager colPubManager;
	private OrgManager orgManager;
	private PermissionManager permissionManager;
	private CollaborationFormBindEventListener collaborationFormBindEventListener;
    private MainbodyManager              ctpMainbodyManager;
    private TemplateManager              templateManager;
    private ColLockManager       colLockManager;
    private CAPFormManager capFormManager;

    public ColLockManager getColLockManager() {
		return colLockManager;
	}
	public CAPFormManager getCapFormManager() {
        return capFormManager;
    }


    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }


    public CollaborationFormBindEventListener getCollaborationFormBindEventListener() {
        return collaborationFormBindEventListener;
    }


    public void setColLockManager(ColLockManager colLockManager) {
		this.colLockManager = colLockManager;
	}


	public TemplateManager getTemplateManager() {
		return templateManager;
	}


	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}


	public void setCollaborationFormBindEventListener(
			CollaborationFormBindEventListener collaborationFormBindEventListener) {
		this.collaborationFormBindEventListener = collaborationFormBindEventListener;
	}
	
	
	public OrgManager getOrgManager() {
		return orgManager;
	}

	public MainbodyManager getCtpMainbodyManager() {
		return ctpMainbodyManager;
	}


	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
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

	public ColPubManager getColPubManager() {
		return colPubManager;
	}

	public void setColPubManager(ColPubManager colPubManager) {
		this.colPubManager = colPubManager;
	}

	public ColManager getColManager() {
		return colManager;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	@Override
	public Object getComment(String attitude, String opinionContent, long affairId, long moduleId)
			throws BusinessException {
		Comment opinion = new Comment();
		opinion.setAffairId(affairId);
		opinion.setCtype(Comment.CommentType.comment.getKey());// 批处理的时候设置类型为评论
		opinion.setClevel(1);
		opinion.setPid(0L);
		opinion.setModuleType(1);
		opinion.setModuleId(moduleId);
		opinion.setRelateInfo("[]");
		opinion.setExtAtt1(attitude);
		opinion.setContent(opinionContent);
		return opinion;
	}
	@Override
	public Object getComment(String attitude, String opinionContent, long affairId, long moduleId, String attitudeCode)
			throws BusinessException {
		Comment opinion = new Comment();
		opinion.setAffairId(affairId);
		opinion.setCtype(Comment.CommentType.comment.getKey());// 批处理的时候设置类型为评论
		opinion.setClevel(1);
		opinion.setPid(0L);
		opinion.setModuleType(1);
		opinion.setModuleId(moduleId);
		opinion.setRelateInfo("[]");
		opinion.setExtAtt1(attitude);
		opinion.setExtAtt4(attitudeCode);
		
		opinion.setContent(opinionContent);
		return opinion;
	}
	
	/**
	 * 态度改造后,int类型已经被废弃
	 */
	@Override
	@Deprecated
	public Object getComment(Integer attitude, String opinionContent, long affairId, long moduleId)
			throws BusinessException {
		Comment opinion = new Comment();
		opinion.setAffairId(affairId);
		opinion.setCtype(Comment.CommentType.comment.getKey());// 批处理的时候设置类型为评论
		opinion.setClevel(1);
		opinion.setPid(0L);
		opinion.setModuleType(1);
		opinion.setModuleId(moduleId);
		opinion.setRelateInfo("[]");
		if (attitude != null) {
			switch (attitude.intValue()) {
			case 1:
				opinion.setExtAtt1("collaboration.dealAttitude.haveRead");
				break;
			case 2:
				opinion.setExtAtt1("collaboration.dealAttitude.agree");
				break;
			case 3:
				opinion.setExtAtt1("collaboration.dealAttitude.disagree");
				break;
			default:
				break;
			}
		}

		opinion.setContent(opinionContent);
		return opinion;
	}

	@Override
	public ApplicationCategoryEnum getAppEnum() throws BusinessException {
		return ApplicationCategoryEnum.collaboration;
	}
	
	
	public FinishResult transFinishWorkItem(long affairId, long summaryId, Object comment, User user,Map<String,Object> param) throws BatchException {
		Object formData = param.get("_formData");
		int code = BatchState.Normal.getCode();
		String msg = "";
		FinishResult finishResult = new FinishResult();
		
		//协同重复提交锁
        boolean isLock = false;
        
		try {
			
			ColSummary summary = (ColSummary)param.get("COL_SUMMARY_OBJ");
			if(summary == null){
				summary  = colManager.getColSummaryById(summaryId);
			}
			
			finishResult.setSubject(summary.getSubject());
			
			//检查流程锁
			String[] checkProcessRet = checkProcess(summary.getProcessId(), user);
			if(!"true".equals(checkProcessRet[0])){
				finishResult.setErrorMsg(checkProcessRet[1]);
			    return finishResult;
			}
			isLock = colLockManager.canGetLock(affairId);
			if (!isLock) {
				finishResult.setErrorMsg(ResourceUtil.getString("collaboration.summary.notDuplicateSub"));
				LOG.error(AppContext.currentAccountName() + " 不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
				return finishResult;
			}
			CtpAffair affair = affairManager.get(affairId);
			
			
			if(!affairManager.isAffairValid(affair,true)){
				String errorMsg = ColUtil.getErrorMsgByAffair(affair);
				finishResult.setErrorMsg(errorMsg);
				return finishResult;
			}
			
			
			Comment opinion = (Comment)comment;
			
			// 不同意意见必填
			if(CommentExtAtt1Enum.disagree.getI18nLabel().equals(opinion.getExtAtt1())
			        && Strings.isBlank(opinion.getContent())){
			    
			    NodePolicy nodePolicy = getNodePolicy(affair, summary);
			    Integer disAgredOpinionPlicy = nodePolicy.getDisAgreeOpinionPolicy();
			    boolean _disAgredOpinionPlicy = Integer.valueOf(1).equals(disAgredOpinionPlicy);
			    if(_disAgredOpinionPlicy){
			        finishResult.setErrorMsg(ResourceUtil.getString("collaboration.batch.comment.isnull"));
			        return finishResult;
			    }
			}
			
			
            
			//对表单数据对象附上初始值 
			if (ColUtil.isForm(affair.getBodyType())) {

				
			    String currentNodeLast = (String) param.get("currentNodeLast");
				
				//V50_SP2_NC业务集成插件_001_表单开发高级
		        if(collaborationFormBindEventListener != null){
		        	
		            
		            Map<String,String> ret = collaborationFormBindEventListener.checkBindEventBatch(affair.getId(),affair.getFormAppId(), affair.getMultiViewStr(), summary.getId()
                            ,ColHandleType.finish,opinion.getExtAtt1(),opinion.getContent(),currentNodeLast);
		        	if(!"true".equals(ret.get("success"))){
		        		code = BatchState.fromAdvanced.getCode();
			        	msg = ret.get("msg");
		        	}
		        }
		        
		        
		        // 处理前事件
                if (Strings.isBlank(msg)) {
                    msg = ColUtil.executeWorkflowBeforeEvent(affair, "BeforeFinishWorkitem", currentNodeLast,opinion);
                    if (Strings.isNotBlank(msg)) {
                        LOG.error("BeforeFinishWorkitem - affair.id:" + affair.getId() + ",currentNodeLast：" + currentNodeLast + ",msg:" + msg);
                        code = BatchState.allOther.getCode();
                    }
                }
		        
		        //流程结束前事件
		        if(Strings.isBlank(msg)){
    		        if("true".equals((String)param.get("isLast"))){
                        String eventMsg =  ColUtil.executeWorkflowBeforeEvent(affair,"BeforeProcessFinished", currentNodeLast,opinion);
                        if (Strings.isNotBlank(eventMsg)) {
                            LOG.error("BeforeProcessFinished - affair.id:"+affair.getId()+msg);
                            code = BatchState.allOther.getCode();
                            msg = eventMsg;
                        }
                    }
		        }
			}
			
			
			
			if(code ==  BatchState.Normal.getCode()){
			   
			    // 更新表单初始值,只会节点权限 & 加签只读不赋值表单初始值
		        if (ColUtil.isForm(affair.getBodyType()) && !AffairUtil.isFormReadonly(affair) && !"inform".equals(affair.getNodePolicy())) {
		          
			        String rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
			        capFormManager.procDefaultValue(summary.getFormAppid(), summary.getFormRecordid(), rightId, summaryId,formData);
			        LOG.info("表单初始值赋值：formAppId:"+summary.getFormAppid()+"getFormRecordid:"+summary.getFormRecordid()+",rightId:"+rightId
	                        +",summary.getId():"+summary.getId()+",affairid:"+affair.getId());
			    }
		        
		        if(null!= summary.getTempleteId()){
                    CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
                    if (ctpTemplate != null){
                        //模板标题
                        param.put("templateColSubject",ctpTemplate.getColSubject()); 
                        //模板工作流ID
                        param.put("templateWorkflowId",ctpTemplate.getWorkflowId()); 
                    }
                }
				colManager.transFinishWorkItemPublic(affair,summary,opinion,ColHandleType.finish, param);
			}
			
			
		}
		catch (Exception e) {
		    LOG.error("批处理异常", e);
			code = BatchState.Error.getCode();
			msg = e.getMessage();
			throw new BatchException(code,msg);
		}finally {
		    
		    if(isLock){
                colLockManager.unlock(affairId);
            }
		    
			try {
				colManager.colDelLock(affairId);
			} catch (BusinessException e1) {
				LOG.error("批处理提交解锁失败", e1);
			}
		}
		
		
		if(code != BatchState.Normal.getCode()){
			throw new BatchException(code,msg);
		}
		
		return finishResult;
	}
	
	public Map<String, String> checkAppPolicy(CtpAffair affair,Object object) throws BatchException{
		
	    ColSummary summary = (ColSummary)object;
		
		NodePolicy policy = getNodePolicy(affair, summary);
      
		return checkPolicy(policy);
	}
	
	
	/**
	 * 获取意见权限
	 * 
	 * @param affair
	 * @param summary
	 * @return
	 * @throws BatchException 
	 */
    private NodePolicy getNodePolicy(CtpAffair affair, ColSummary summary) throws BatchException {

        NodePolicy policy = null;
        try {
            V3xOrgMember sender = orgManager.getMemberById(affair.getSenderId());

            Long flowPermAccountId = ColUtil.getFlowPermAccountId(sender.getOrgAccountId(), summary);

            String nodePermissionPolicy = "collaboration";
            // nodePermissionPolicy = activity.getSeeyonPolicy().getId();
            nodePermissionPolicy = affair.getNodePolicy();

            Permission permission = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(),
                    nodePermissionPolicy, flowPermAccountId);
            policy = permission.getNodePolicy();
        } catch (BusinessException e) {
            throw new BatchException(BatchState.Error.ordinal(),
                    ResourceUtil.getString("collaboration.batch.alert.notdeal.20"));
        }

        return policy;
    }
	
	@Override
    public BatchCheckResult checkForm(CtpAffair affair, Object object,User user) {

        BatchCheckResult result = new BatchCheckResult();
        result.setAllowed(true);

        ColSummary summary = (ColSummary) object;
		Object formData = null;
        // 校验表单必填项
        String bindFormOperation = "";
        try {
            bindFormOperation = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, null);
        } catch (BusinessException e) {
            LOG.error("", e);
        }
        boolean isAffairReadOnly = AffairUtil.isFormReadonly(affair);

        if (!"inform".equals(affair.getNodePolicy()) && !isAffairReadOnly) {
            
            
            Set<String> fields = capFormManager.getNotNullableFields(summary.getFormAppid(), bindFormOperation);
            if (Strings.isNotEmpty(fields)) {
                LOG.info("[批处理getNotNullableFields]: Not Empty,传入参数：formRightId：" + bindFormOperation + ", formAppID:" + summary.getFormAppid() + "fields:" + Strings.join(fields, ","));
                result.setAllowed(false);
                result.setMsg(ResourceUtil.getString("collaboration.batch.alert.notdeal.16"));
                return result;
            }
            else {
                LOG.info("[批处理getNotNullableFields]: Empty,传入参数：formRightId：" + bindFormOperation + ", formAppID:" + summary.getFormAppid());
            }

            // 校验表单校验规则
            Map<String, Object> ret = capFormManager.checkRule(summary.getId(), summary.getFormAppid(), summary.getFormRecordid(), bindFormOperation);
			formData = ret.get("formData");
			result.setFormData(formData);
			String isPass = (String)ret.get("isPass");
            if ("false".equals(isPass)) {
                LOG.info("[checkRule]批处理：判断表单是否能批处理: FALSE,传入参数：formMasterDataId：" + summary.getFormRecordid() + ", formAppID:" + summary.getFormAppid() + ",formRightId:" + bindFormOperation
                        + ",summaryId:" + summary.getId());
                result.setAllowed(false);
                result.setMsg(ResourceUtil.getString("collaboration.batch.alert.notdeal.31"));
                return result;
            }
            else {
                LOG.info("[checkRule]批处理：判断表单是否能批处理: True,传入参数：formMasterDataId：" + summary.getFormRecordid() + ", formAppID:" + summary.getFormAppid() + ",formRightId:" + bindFormOperation
                        + ",summaryId:" + summary.getId());
            }

        }

        boolean isForm = ColUtil.isForm(affair.getBodyType());
        if (isForm) {
            List<CtpContentAll> content = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, affair.getObjectId());
            if (Strings.isNotEmpty(content)) {
                String s = content.get(0).getContent();
                if (Strings.isNotBlank(s) && Strings.isDigits(s)) {
                    result.setAllowed(false);
                    result.setMsg(ResourceUtil.getString("collaboration.batch.alert.notdeal.27"));
                    return result;
                }
            }

            boolean  isFormNeedAddLock  = colManager.isFormNeedAddLock(affair, bindFormOperation);
            
            if(isFormNeedAddLock) {
            	String from = Constants.login_sign.stringValueOf(user.getLoginSign());
            	boolean isLock =  capFormManager.lockFormData(affair.getFormRecordid(),user.getId(), from);
            	if(!isLock){
            		Lock lock = capFormManager.getLock(affair.getFormRecordid());
            		String lockName = Functions.showMemberName(lock.getOwner());
            		String alertLockMsg = ResourceUtil.getString("collaboration.common.flag.editingForm",lockName,lock.getFrom());
            		result.setAllowed(false);
            		result.setMsg(alertLockMsg);
            		return result;
            	}
            }
        }
		result.setFormData(formData);
        return result;
    }


	@Override
	public Map<String, String> disagreeStepBack(Map pMap) throws Exception {
		
		Map<String,String> resonMap = new HashMap<String,String>();
    	
    	Map<String,Object> tempMap=new HashMap<String, Object>();
    	String affairId =String.valueOf(pMap.get("affairId"));
    	String summaryId = String.valueOf(pMap.get("summaryId"));
    	tempMap.put("affairId", affairId);
    	tempMap.put("summaryId", summaryId);
    	tempMap.put("targetNodeId", "");//TODO 暂时不支持回退到指定节点
    	boolean isLock  = false;
    	try{   		
    		isLock  = colLockManager.canGetLock(Long.valueOf(affairId));
    		if (!isLock) {
    			resonMap.put("error",AppContext.currentAccountName()+ResourceUtil.getString("coll.summary.lock.tip")+"stepStop,affairId"+affairId);
    			return resonMap;
    		}
    		
    		CtpAffair affair = affairManager.get(Long.valueOf(affairId));
    		
    		Comment comment = new Comment();
    		String content = (String)pMap.get("content");
    		comment.setContent(content == null ? "" : null);
    		comment.setExtAtt1(CommentExtAtt1Enum.disagree.getI18nLabel());
    		comment.setExtAtt4(CommentExtAtt1Enum.disagree.name());
    		tempMap.put("comment", comment);
    		
    		BatchCheckResult result = excuteDee(affair,ColHandleType.stepBack,comment);
    		if(!result.isAllowed()){
    		    resonMap.put("error",result.getMsg());
                return resonMap;
    		}
    		
    		String msg = colManager.transStepBack(tempMap);
    		if (Strings.isNotBlank(msg)) {
    			resonMap.put("error", msg);
                 return resonMap;
            }
    	}
    	finally{
    		if(isLock){
            	colLockManager.unlock(Long.valueOf(affairId));
            }
    		colManager.colDelLock(Long.valueOf(affairId));
    	}
    	
		return resonMap;
	}


	@Override
	public Map<String, String> disagreeStepStop(Map pMap) throws Exception {

		Map<String,String> resonMap = new HashMap<String,String>();
		
        String affairId= (String)pMap.get("affairId");
        String summaryId= (String)pMap.get("summaryId");
        Map<String,Object> tempMap=new HashMap<String, Object>();
        tempMap.put("affairId", affairId);
        String content = (String) pMap.get("collaboration.dealAttitude.termination");
        if(Strings.isNotBlank(content)){
        	tempMap.put("collaboration.dealAttitude.termination",content);
        }else{
        	tempMap.put("collaboration.dealAttitude.termination","");
        }
        boolean isLock = false; 
        try{
        	isLock =  colLockManager.canGetLock(Long.valueOf(affairId));
        	if (!isLock) {
        		resonMap.put("error",AppContext.currentAccountName()+ResourceUtil.getString("coll.summary.lock.tip")+"stepStop,affairId"+affairId);
        		return null;
        	}
        	//跟踪参数
//            Map<String, String> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
//            tempMap.put("trackParam", trackPara);
            
            //取出模板信息
        	ColSummary summaryById = colManager.getSummaryById(Long.valueOf(summaryId));
        	if(null != summaryById && null != summaryById.getTempleteId()){
        		CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summaryById.getTempleteId());
        		if(null != ctpTemplate){
        			tempMap.put("templateColSubject", ctpTemplate.getColSubject());
                    tempMap.put("templateWorkflowId", ctpTemplate.getWorkflowId());
        		}
        	}
            
        	   CtpAffair affair = affairManager.get(Long.valueOf(affairId));
               
               Comment comment = new Comment();
               comment.setContent(content);
               comment.setExtAtt1(CommentExtAtt1Enum.disagree.getI18nLabel());
               comment.setExtAtt4(CommentExtAtt1Enum.disagree.name());
               tempMap.put("comment", comment);
               
               BatchCheckResult result = excuteDee(affair,ColHandleType.stepStop,comment);
               if(!result.isAllowed()){
                   resonMap.put("error",result.getMsg());
                   return resonMap;
               }
               
               if(Strings.isNotEmpty(affairId)){
                   String msg = ColUtil.executeWorkflowBeforeEvent( affair, "BeforeStop","",comment);
                   if(Strings.isNotBlank(msg)){
                	   LOG.info("BeforeStop,affairId:"+affair.getId()+",msg:"+msg);
                       resonMap.put("error", msg);
                       return resonMap;                    
                   }
               }
               
        	colManager.transStepStop(tempMap);
        }
        finally{
    	    if(isLock){
           		colLockManager.unlock(Long.valueOf(affairId));
            }
        	colManager.colDelLock(Long.valueOf(affairId));
        }
        return resonMap;
    
	}

	@Override
	public BatchResult checkApp(CtpAffair affair, Object object) throws BatchException {
	    
	   BatchResult result = new BatchResult();
	   result.setResultCode(BatchState.Normal.ordinal());
	   return result;
        
	}
    


	@Override
	public Map<String, String> disagreeRepeal(Map pMap) throws Exception {
		
		Map<String,String> resonMap = new HashMap<String,String>();
        String summaryId = String.valueOf(pMap.get("summaryId"));
        String affairId= String.valueOf(pMap.get("affairId"));
        String trackWorkflowType = (String)pMap.get("trackWorkflowType");
        Map<String,Object> tempMap=new HashMap<String, Object>();
        tempMap.put("summaryId", summaryId);
        tempMap.put("affairId", affairId);
        //tempMap.put("repealComment", request.getParameter("repealComment"));
        String content = (String)pMap.get("collaboration.dealAttitude.cancelProcess");
        if(Strings.isBlank(content)){
        	content = "";
        }
        tempMap.put("extAtt1",CommentExtAtt1Enum.disagree.getI18nLabel() );
      //  DateSharedWithWorkflowEngineThreadLocal.addRepalAtt("collaboration.dealAttitude.cancelProcess", content);
        tempMap.put("isWFTrace", trackWorkflowType);
        Long laffairId = Long.valueOf(affairId);
        try{
            CtpAffair affair = affairManager.get(Long.valueOf(affairId));
            
            Comment comment = new Comment();
            comment.setContent(content == null ? "" : content);
            comment.setExtAtt1(CommentExtAtt1Enum.disagree.getI18nLabel());
            comment.setExtAtt4(CommentExtAtt1Enum.disagree.name());
           
            tempMap.put("comment", comment);
            
            BatchCheckResult result = excuteDee(affair,ColHandleType.repeal,comment);
            if(!result.isAllowed()){
                resonMap.put("error",result.getMsg());
                return resonMap;
            }
            
            if(laffairId != null){
                String msg = ColUtil.executeWorkflowBeforeEvent(affair,"BeforeCancel", "",comment);
                if(Strings.isNotBlank(msg)){
                	LOG.info("BeforeCancel,affairId:"+affair.getId()+",msg:"+msg);
                    resonMap.put("error", msg);
                    return resonMap;                    
                }
            }
            tempMap.put("repealComment", content);
        	colManager.transRepal(tempMap);
        }
        finally{
        	colManager.colDelLock(laffairId,true);
        }
        
        return resonMap;
	}

    
    private BatchCheckResult excuteDee(CtpAffair affair, ColHandleType handleType, Comment comment) throws BusinessException {

        BatchCheckResult result = new BatchCheckResult();
        result.setAllowed(true);
        if (!String.valueOf(MainbodyType.FORM.getKey()).equals(affair.getBodyType())) {
            return result;
        }
        if (collaborationFormBindEventListener != null) {

            String attitude = "";
            String content = "";
            if (comment != null) {
                attitude = comment.getExtAtt1();
                content = comment.getContent();
            }
            Map<String, String> ret = collaborationFormBindEventListener.checkBindEventBatch(affair.getId(), affair.getFormAppId(), affair.getMultiViewStr(), affair.getObjectId(),
                    handleType, attitude, content,"");

            if (!"true".equals(ret.get("success"))) {
                String msg = ret.get("msg");
                result.setAllowed(false);
                result.setMsg(msg);
                LOG.info("DEE执行失败 - affair.id:" + affair.getId() + ",msg:" + msg);
            }
        }
        return result;
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
