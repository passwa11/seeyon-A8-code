package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CommentExtAtt1Enum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.DetailAttitude;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.portal.portlet.bo.CollaborationInfo;
import com.seeyon.ctp.portal.portlet.manager.DeskCollaborationProcessManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.ctp.workflow.wapi.PopResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

public class DeskCollaborationProcessManagerImpl implements DeskCollaborationProcessManager {
	private static Log LOG = CtpLogFactory.getLog(DeskCollaborationProcessManagerImpl.class);
	private String app = null;
	private AffairManager affairManager;
	private TemplateManager              templateManager;
	private ColManager colManager ;
	private CommentManager ctpCommentManager;
	private PermissionManager            permissionManager;
    private AttachmentManager             attachmentManager;
	public CustomizeManager customizeManager;
    private WorkflowApiManager       wapi;
    private ColLockManager colLockManager;
    private CAPFormManager capFormManager;
    
    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }
    
    public void setColLockManager(ColLockManager colLockManager) {
        this.colLockManager = colLockManager;
    }

	public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}


	public WorkflowApiManager getWapi() {
		return wapi;
	}
	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}


	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}


	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}
	
	public void setCtpCommentManager(CommentManager ctpCommentManager) {
        this.ctpCommentManager = ctpCommentManager;
    }


	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	

	@Override
	public void setApp(String app) {
		this.app = app;
	}
	@Override
	public String getApp() {
		return this.app;
	}
	@Override
	public void finishWorkitemQuick(Map<String,String> param) throws BusinessException{
	    
		String affairId = param.get("affairId");
		String comment = param.get("comment");
		String attitude = param.get("attitude");
		
		LOG.info("finish Collaboration by desk interface, affairId='" + affairId + "'");
		
		if(affairId == null){
            throw new BusinessException("affairId is null");
		}
        CtpAffair affair = affairManager.get(Long.valueOf(affairId));
        ColSummary summary = colManager.getSummaryById(affair.getObjectId());
        
        boolean isLock = colLockManager.canGetLock(affair.getId());
        
        if(!isLock){
            LOG.error(AppContext.currentAccountName()+"不能获取到map缓存锁，不能执行操作finishWorkItem,affairId"+affairId);
            throw new BusinessException("invalid post, the same post is running");
        }
        
        try {
            //判断并发锁,0为true或false, 1为msg
    		String[] ret = wapi.checkWorkFlowProcessLock(String.valueOf(summary.getProcessId()),String.valueOf(AppContext.currentUserId()));
            if(ret!=null && ret.length == 3){
            	if("false".equals(ret[0])){
            		throw new BusinessException(ret[1]);
            	}
            }
            
            //意见必填
            Permission  p = getPermission(comment, affair, summary);
            NodePolicy nodePolicy = p.getNodePolicy();
            Integer one = Integer.valueOf(1);
            boolean isCommontBlank = Strings.isBlank(comment);
            if(p!=null){
            	boolean isOpionionMustWrite = one.equals(nodePolicy.getOpinionPolicy());
            	if(isOpionionMustWrite && isCommontBlank){
            		throw new BusinessException(ResourceUtil.getString("collaboration.common.deafult.dealCommentNotNull"));//意见不能为空！
            	}
            }
            Comment c = new Comment();
            c.setAffairId(Long.valueOf(affairId));
            c.setId(UUIDLong.longUUID());
            c.setContent(comment);
            c.setCtype(Comment.CommentType.comment.getKey());
            c.setClevel(1);
            c.setCreateDate(new Date());
            c.setCreateId(AppContext.currentUserId());
            c.setModuleId(summary.getId());
            c.setModuleType(ModuleType.collaboration.getKey());
            c.setHidden(false);
            c.setPid(0l);
            if(attitude!=null){
            	/**
            	 * 0 是已阅
            	 * 1是同意
            	 * 2是不同意
            	 * 传入其他的取节点权限默认的态度
            	 */
            	DetailAttitude detailAttitude = nodePolicy.getDatailAttitude();
            	String attitudeCode = nodePolicy.getDefaultAttitude();
            	String showAttitudeName = "";
            	if("0".endsWith(attitude) || CommentExtAtt1Enum.haveRead.name().equals(attitudeCode)) {
            		attitudeCode = CommentExtAtt1Enum.haveRead.name();
                	showAttitudeName = detailAttitude.getHaveRead();
            	}else if("1".endsWith(attitude) || CommentExtAtt1Enum.agree.name().equals(attitudeCode)) {
            		attitudeCode = CommentExtAtt1Enum.agree.name();
            		showAttitudeName = detailAttitude.getAgree();
            	}else if("2".endsWith(attitude) || CommentExtAtt1Enum.disagree.name().equals(attitudeCode)) {
            		attitudeCode = CommentExtAtt1Enum.disagree.name();
            		showAttitudeName = detailAttitude.getDisagree();
            	}
            	 
    			if(CommentExtAtt1Enum.disagree.name().equals(attitudeCode)){
    				if(one.equals(nodePolicy.getOpinionPolicy()) && isCommontBlank){//不同意时意见必填
    			        throw new BusinessException(ResourceUtil.getString("collaboration.common.deafult.dealCommentNotNull"));//意见不能为空！
    			    }
    			}
    			c.setExtAtt1(showAttitudeName);
    			c.setExtAtt4(attitudeCode);
            }
            
            
          //预提交
            WorkflowBpmContext context = null;
            Long templateId = summary.getTempleteId();
            boolean isTemplate = !(templateId == null || templateId == 0 || templateId == -1);
            CtpTemplate template = null;
            //模板才执行事件
            if(isTemplate && summary != null){
                
                context = new WorkflowBpmContext();
                
                Long formRecordid = summary.getFormRecordid();
                if(formRecordid != null){
                    context.setFormData(formRecordid.toString());
                }else{
                    context.setFormData("");
                }
                context.setMastrid(context.getFormData());
                
                
                template = templateManager.getCtpTemplate(summary.getTempleteId());
                if(template != null && template.getWorkflowId() != null){
                    context.setProcessTemplateId(template.getWorkflowId().toString());
                }
                context.setProcessId(summary.getProcessId());
                context.setCurrentActivityId(affair.getActivityId().toString());
                context.setBussinessId(summary.getId().toString());
                context.setAffairId(affair.getId().toString());
                context.setAppName(ApplicationCategoryEnum.collaboration.name());
                context.setCurrentWorkitemId(affair.getSubObjectId());
                context.setCurrentUserId(String.valueOf(AppContext.currentUserId()));
                Long formAppId = summary.getFormAppid();
                if(formAppId != null){
                    context.setFormAppId(formAppId.toString());
                }else{
                    context.setFormAppId("");
                }
                
                String formViewOperation = affair.getMultiViewStr();
                if(formViewOperation != null){
                    context.setFormViewOperation(formViewOperation);
                }
                
                //context["matchRequestToken"] = matchRequestToken;
                //context["processXml"] = processXml;
            }
            
            if(summary != null && context != null){
                
                context.setProcessTemplateId("");
                context.setCaseId(summary.getCaseId());
                context.setCurrentWorkitemId(affair.getSubObjectId());
                
                context.setCurrentAccountId(String.valueOf(AppContext.currentAccountId()));
                context.setDebugMode(false);
                //"currentWorkItemIsInSpecial" : false,
                context.setIsValidate(true);
                context.setUseNowExpirationTime("true");
            }
            
            String conditionMatchResult = "";
            if(context != null){
                //设置缓存key
                context.setMatchRequestToken(String.valueOf(UUIDLong.longUUID()));
                PopResult pr = wapi.isPop(context);
                if("true".equals(pr.getPopResult())){
                    throw new BusinessException("流程处理需要进行分支选择， 暂时不支持该方式处理.");
                }else{
                    //分支选择
                    conditionMatchResult = pr.getConditionsOfNodes();
                }
            }
            
            
            Map<String, Object> _json_params = new HashMap<String, Object>();
            
            //构建工作流数据
            Map<String, String> workflowParam = new HashMap<String, String>();
            workflowParam.put("process_desc_by",  "xml");
            workflowParam.put("process_xml",  "");
            workflowParam.put("readyObjectJSON",  "");
            workflowParam.put("workflow_data_flag",  "WORKFLOW_SEEYON");
            workflowParam.put("process_info",  "");
            workflowParam.put("process_info_selectvalue",  "");
            workflowParam.put("process_subsetting",  "");
            workflowParam.put("moduleType",  "1");
            workflowParam.put("workflow_newflow_input",  "");
            workflowParam.put("process_rulecontent",  "");
            workflowParam.put("workflow_node_peoples_input",  "");
            workflowParam.put("workflow_node_condition_input", conditionMatchResult);
            workflowParam.put("processId",  summary.getProcessId());
            workflowParam.put("caseId",  summary.getCaseId().toString());
            workflowParam.put("subObjectId",  affair.getSubObjectId().toString());
            workflowParam.put("currentNodeId",  affair.getActivityId().toString());
            workflowParam.put("process_message_data",  "");
            workflowParam.put("processChangeMessage",  "");
            workflowParam.put("process_event",  "");
            workflowParam.put("toReGo",  "false");
            workflowParam.put("dynamicFormMasterIds",  "");
            
            _json_params.put("workflow_definition", workflowParam);
            _json_params.put("trackDiv_detail", new HashMap<String, String>());
            _json_params.put("superviseDiv",  new HashMap<String, String>());
            _json_params.put("attFileDomain", Collections.emptyList());
            _json_params.put("assDocDomain", Collections.emptyList());
            _json_params.put("attActionLogDomain", Collections.emptyList());
            _json_params.put("mainbodyDataDiv_0", Collections.emptyList());
            _json_params.put("attachmentInputs", Collections.emptyList());
            putThreadContext(_json_params);
            
          //初始化表单默认值
            if(isTemplate && MainbodyType.FORM.getValue().equals(summary.getBodyType())){
                
                String rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
                capFormManager.procDefaultValue(summary.getFormAppid(), summary.getFormRecordid(), rightId, summary.getId(),null);
            }
            
            Map<String,Object> params = new HashMap<String,Object>();
            if(template != null){
            	params.put("templateColSubject", template.getColSubject());
                params.put("templateWorkflowId", template.getWorkflowId());
            }
        
			colManager.transFinishWorkItemPublic(affair,summary,c,ColHandleType.finish, params) ;
		} catch (BusinessException e) {
			LOG.error("", e);
			throw e;
		} catch (Exception e) {
            LOG.error("通过接口处理协同异常", e);
            throw new BusinessException(e);
        }finally{
		    if(isLock){
                colLockManager.unlock(affair.getId());
            }
            if (summary != null) {
                colManager.colDelLock(summary, affair,true);
            }
            removeThreadContext();
        }
	}
	
	/**
     *  将 _json_params 设置到 线程中
     * 
     * @param jsonStr
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月6日下午7:35:16
     *
     */
    private void putThreadContext(Map<String, Object> jsonData){
        
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY, JSONUtil.toJSONString(jsonData));
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY, jsonData);
    }
    
    /**
     * 清理缓存数据
     * 
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月5日下午4:05:17
     *
     */
    private void removeThreadContext(){
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY);
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY);
    }
	
	private Permission getPermission(String comment, CtpAffair affair,
			ColSummary summary) throws BusinessException {
		Long templateOrgAccountId = null;
		if(summary.getTempleteId()!= null){
			 CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
			 if(template!=null){
				 templateOrgAccountId = template.getOrgAccountId();
			 }
		}
		Long accountId =  ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary.getOrgAccountId(),templateOrgAccountId );
		String configItem = ColUtil.getPolicyByAffair(affair).getId();
        String category = EnumNameEnum.col_flow_perm_policy.name();
        Permission permission = null;
        try{
            permission = permissionManager.getPermission(category, configItem, accountId);
        }catch(Throwable e){
            LOG.error("category:"+category +" caonfigItem:"+configItem +" accountId:"+accountId, e);
            throw new BusinessException(e);
        }
        return permission;
	}
	

   
	@Override
	public CollaborationInfo getCollaboration(Map<String,String> param) throws BusinessException{
		String affairId = param.get("affairId");
		String summaryId = param.get("summaryId");
		//模板ID,用来区分是否是自由流程
		String templateId = param.get("templateId");
		CollaborationInfo info = new CollaborationInfo();
		if(Strings.isBlank(summaryId)){
			return info;
		} 
		CtpAffair affair = affairManager.get(Long.valueOf(affairId));
		ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
		Comment comentDraft = ctpCommentManager.getDraftOpinion(affair.getId());
		if(comentDraft != null){
			info.setComment(comentDraft.getContent());
			String attitude= comentDraft.getExtAtt1();
			info.setAttitude(attitude);
		}
		
		
		
        //显示附件数量
        List<Attachment> atts = attachmentManager.getByReference(summary.getId(), summary.getId());
        int fileAtt = 0;
        int docAtt = 0;
        for (Attachment att : atts) {
            //附件
            if (att.getType() == 0) {
                fileAtt += 1;
            } else { //关联文档
                docAtt += 1;
            }
        }
        info.setAttDocSize(docAtt);
        info.setAttSize(fileAtt);
        info.setId(affair.getId());
		info.setSubject(affair.getSubject());
		info.setStartMember(affair.getSenderId());
		info.setModuleType(affair.getApp());
		info.setFormViewOperation(affair.getMultiViewStr());
		
		//设置处理页面
		String clickUrl = "/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId="+affair.getId();
		info.setClickUrl(clickUrl);
		//设置快捷显示内容地址
		String url = "/collaboration/collaboration.do?method=componentPage&affairId="+affair.getId()+"&rightId="+affair.getMultiViewStr()+"&canFavorite=false&readonly=true&openFrom=";
		info.setContentUrl(url);
		
		boolean isPop = isPop(affair, summary.getProcessId(), summary.getCaseId(), summary.getStartMemberId(), null, null, "collaboartion");
		
		boolean inInSpecialSB= false;
		if(Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())){
    		if(Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(affair.getSubState()) ||
    				Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(affair.getSubState())){//15 16 17
    			inInSpecialSB =  true;
    		}
    	}
		boolean  formTransSendFlag =!summary.getCanEdit() && (null != summary.getParentformSummaryid());
		//自由流程可以快速提交
		if(summary.getTempleteId()== null && !isPop && !inInSpecialSB && !formTransSendFlag){
	        //设置意见处理区
	        List<Map<String,String>> attitudes = new ArrayList<Map<String,String>>();
	        //获取当前节点权限
	        Permission permission = getPermission(null, affair, summary);
	        
	        String nodeattitude = permission.getNodePolicy().getAttitude();
	        if (Strings.isNotBlank(nodeattitude)) {
	        	Map<String,String> attitude0 = new HashMap<String,String>();
	        	String[] attitudeArr = nodeattitude.split(",");
	        	for (String attitude : attitudeArr) {
	        		attitude0.put(attitude, ResourceUtil.getString(attitude));
	        	}
	        	attitudes.add(attitude0);
	        }
	        
	        info.setAttitudes(attitudes);
	        //设置允许提交恢复意见
	        info.setShowComment(true); 
	        //设置提交按钮显示文本
	        info.setShowSubmitBtn(true);
	        info.setSubmitBtnText(ResourceUtil.getString("common.button.submit.label")); //提交
		}else{
			//设置允许提交恢复意见
	        info.setShowComment(false); 
	        //设置提交按钮
	        info.setShowSubmitBtn(false);
		}
		//设置已读状态
        colManager.updateAffairStateWhenClick(affair);
		return info;
	}
	

    /**
     * @param affair
     * @param processId
     * @param edocSummary
     * @return
     * @throws BPMException
     */
    private Boolean isPop(CtpAffair affair, String processId, Long caseId,Long startUserId,String formAppId,String materId,String appName) throws BPMException {
        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setProcessId(processId);
        wfContext.setCaseId(caseId);
        wfContext.setCurrentActivityId(String.valueOf(affair.getActivityId()));
        wfContext.setCurrentWorkitemId(affair.getSubObjectId());
        wfContext.setFormData(formAppId);
        wfContext.setMastrid(materId);
        wfContext.setAppName(appName);
        wfContext.setStartUserId(String.valueOf(startUserId));
        wfContext.setCurrentUserId(String.valueOf(AppContext.getCurrentUser().getId()));
        CPMatchResultVO crvo = wapi.transBeforeInvokeWorkFlow(wfContext, new CPMatchResultVO());
        if( null!= crvo.getInvalidateActivityMap() && crvo.getInvalidateActivityMap().size()>0 ){
            crvo.setPop(true); 
        }
        return crvo.isPop() || crvo.isBackgroundPop();
    }
}
