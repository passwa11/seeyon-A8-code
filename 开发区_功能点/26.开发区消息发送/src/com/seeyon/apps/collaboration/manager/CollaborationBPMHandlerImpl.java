/**
 * 
 */
package com.seeyon.apps.collaboration.manager;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;

import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationStartEvent;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.quartz.CollaborationJob;
import com.seeyon.apps.collaboration.util.ColSelfUtil;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.ctp.cap.api.constant.CAPFormEnum;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.enums.AffairExtPropEnums;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.dao.AttachmentDAO;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.CollaborationTemplateManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler;
import com.seeyon.ctp.rest.resources.SeeyonBPMHandAddNodeTypeEnum;
import com.seeyon.ctp.rest.resources.SeeyonBPMHandSubmitTypeEnum;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.XXEUtil;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.util.AjaxJsonUtil;
import com.seeyon.ctp.workflow.util.MessageUtil;
import com.seeyon.ctp.workflow.wapi.RunCaseResult;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * 协同+表单的流程rest接口实现类
 * @author wangchw
 *
 */
public class CollaborationBPMHandlerImpl extends SeeyonBPMAppHandler {

	private static final Log LOG = CtpLogFactory.getLog(CollaborationBPMHandlerImpl.class);

	private CAPFormManager capFormManager;
	private AttachmentManager attachmentManager=null;
	private ColManager colManager;
	private TemplateManager templateManager;
	private WorkflowApiManager wapi;
	private AffairManager affairManager;
	private AttachmentDAO attachmentDAO;
	private DocApi docApi;
	private CollaborationTemplateManager collaborationTemplateManager;
	private CommentManager ctpCommentManager;
	private SuperviseManager superviseManager;
	private AppLogManager appLogManager;
	private ProcessLogManager processLogManager;
	private WorkTimeManager workTimeManager;
	private IndexApi indexApi;
	private ColLockManager colLockManager = null;
	private OrgManager orgManager;
	private PermissionManager permissionManager = null;
	
	
	
	
	private static final String FORM_BEAN_ID = "formBeanId";
	
	
	public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }
	
	public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#getApp()
	 */
	@Override
	public ApplicationCategoryEnum getApp() {
		return ApplicationCategoryEnum.collaboration;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#startProcess(java.util.
	 * Map)
	 */
	@Override
	public Map<String, String> startProcess(Map<String, Object> param) throws BusinessException {
		Map<String,String> result= new HashMap<String, String>();
		try{
			User user = AppContext.getCurrentUser();
			String draft= (String)param.get(SeeyonBPMAppHandler.BPM_REQUEST_DRAFT);
			boolean saveDraft= "1".equals(draft);
			
			CtpTemplate template = (CtpTemplate)param.get(BPM_REQUEST_CTPTEMPLATE); 

			Long[] attachments = list2LongArray((List<Long>) param.get("attachments"));
			
			//获取模板对应的关联协同与附件(协同公文获取summaryID，会议文档直接获取affairID)
	        List<Attachment> attachmentList = attachmentManager.getByReference(template.getId());
	        StringBuilder attBuffer = new StringBuilder();
	        StringBuilder colBuffer = new StringBuilder();
	        StringBuilder edocBuffer = new StringBuilder();
	        List<Attachment> meetingAndDoc=new ArrayList<Attachment>();
	        
	        colBuffer.append("col|");
	        edocBuffer.append("edoc|");
	        
	        List<Long> attList = new ArrayList<Long>(); 
	        if (attachmentList != null) {
	            for (int i = 0; i < attachmentList.size(); i++) {
	                Attachment attInfo = attachmentList.get(i);
	                if (attInfo != null &&attInfo.getType() == 0) {
	                    attList.add(attInfo.getFileUrl());
	                } else if (attInfo != null && attInfo.getType() == 2&& "collaboration".equals(attInfo.getMimeType())) {
	                    colBuffer.append(colManager.getAffairById(attInfo.getFileUrl()).getObjectId().toString() + ",");
	                }else if (attInfo != null && attInfo.getType() == 2&& "edoc".equals(attInfo.getMimeType())) {
	                    edocBuffer.append(colManager.getAffairById(attInfo.getFileUrl()).getObjectId().toString() + ",");
	                }else if (attInfo != null && attInfo.getType() == 2&& "km".equals(attInfo.getMimeType())) {
	                    meetingAndDoc.add(attInfo);
	                }else if (attInfo != null && attInfo.getType() == 2&& "meeting".equals(attInfo.getMimeType())) {
	                    meetingAndDoc.add(attInfo);
	                }
	            }
	        }

	        Long fileIds[] = getAttachments(attList, attachments);
	        String relateDoc= null;
	        Object relateDocObj= param.get("relateDoc");
	        if(null!=relateDocObj){
	        	relateDoc= relateDocObj.toString();
	        }
	        if(relateDoc == null && 
	                (colBuffer.toString().split("\\|").length >= 2 || 
	                   edocBuffer.toString().split("\\|").length >= 2)){
	            
	            attBuffer.append(colBuffer.deleteCharAt(colBuffer.length() - 1).toString()+";");
	            attBuffer.append(edocBuffer.deleteCharAt(edocBuffer.length() - 1).toString()+";");
	            relateDoc = attBuffer.toString();
	        }
			
			String subject = (String) param.get("subject");
			//正文类型HTML&Form
	        //10HTML20FORM
            Timestamp now = new Timestamp(System.currentTimeMillis());
            String bodyType = template.getBodyType();
            Long templateId = template.getId();
            String sub = Strings.nobreakSpaceToSpace(subject);
            V3xOrgMember member = (V3xOrgMember) param.get(BPM_REQUEST_LOGINMEMBER);
            Long sender = member.getId();
            if (StringUtils.isBlank(sub)) {
                sub = template.getSubject() + "(" + member.getName() + " " + DateUtil.formatDateTime(now) + ")";
            }
            ColSummary summary = createColSummary(template);
            summary.setSubject(sub);
            summary.setProjectId(template.getProjectId());
			
			//保存正文
	        MainbodyService mainBodyService = MainbodyService.getInstance();
	        
	        CtpContentAllBean content = new CtpContentAllBean();
	        
	        content.setModuleType(ModuleType.collaboration.getKey());
	        content.setCreateId(sender);
	        content.setCreateDate(now);
	        content.setSort(0);
	        content.setId(UUIDLong.longUUID());
	        content.setStatus(MainbodyStatus.STATUS_POST_SAVE);
	        content.setContentType(Integer.parseInt(bodyType));
	        content.setTitle(summary.getSubject());
	        content.setContentTemplateId(0L);
	        content.setContentDataId(null);
	        
	        
	        Long formBeanId= null;
        	Long masterId= null;
	        if (ColUtil.isForm(content.getContentType())) {
	        	formBeanId= (Long)param.get(FORM_BEAN_ID);
	        	masterId= (Long)param.get(BPM_REQUEST_MASTERID);
	            content.setContentType(MainbodyType.FORM.getKey());
	            content.setContentTemplateId(formBeanId);
	            content.setContentDataId(masterId);
	            summary.setFormAppid(content.getContentTemplateId());//表单ID
	            summary.setFormRecordid(content.getContentDataId());//form主数据ID
	        }else{
	        	String data1 = (String) param.get("data");
	        	content.setContent("<p>"+data1+"</p>");
	        }
	        content.setModuleTemplateId(templateId);
	        content.setModuleId(summary.getId());
	        content.setModifyDate(now);
	        content.setModifyId(sender);
	        mainBodyService.saveOrUpdateContentAll(content.toContentAll());

	        summary.setOrgAccountId(member.getOrgAccountId());
	        summary.setOrgDepartmentId(member.getOrgDepartmentId());
	        summary.setStartMemberId(sender);
	        summary.setCreateDate(now);
	        summary.setStartDate(now);
	        summary.setState(CollaborationEnum.flowState.run.ordinal());
	        summary.setTempleteId(template.getId());
	       
			//保存上传的附件
            boolean attaFlag = false;
            if (fileIds != null && fileIds.length != 0) {
                String attaFlagStr = attachmentManager.create(fileIds, ApplicationCategoryEnum.collaboration,
                        summary.getId(), summary.getId());
                attaFlag = Constants.isUploadLocaleFile(attaFlagStr);
            }
			//保存关联文档
			//relateDoc格式："col|123,456;doc|321,654"
			if (Strings.isNotBlank(relateDoc)){
                String[] temp = relateDoc.split(";");
                String[] summaryIds = null;
                String[] summaryEdocIds = null;
                for (String s : temp) {
                    if (s.startsWith("col")) {
                        String[] ss = s.split("\\|");
                        if (ss.length >= 2) {
                            summaryIds = ss[1].split(",");
                        }
                    } else if (s.startsWith("edoc")) {
                        String[] edocInfo = s.split("\\|");
                        if (edocInfo.length >= 2) {
                            summaryEdocIds = edocInfo[1].split(",");
                        }
                    }
                }
		        if (summaryIds != null){
		            loopAttachmentList(summary, summaryIds,"col");
		        }
		       if (summaryEdocIds != null){
		                loopAttachmentList(summary, summaryEdocIds,"edoc");
		        }
		       if(meetingAndDoc!=null){
		           for(Attachment attInfo:meetingAndDoc){
		               Attachment atta = new Attachment();
		                atta.setIdIfNew();
		                atta.setReference(summary.getId());
		                atta.setSubReference(summary.getId());
		                atta.setCategory(attInfo.getCategory());
		                atta.setType(attInfo.getType());
		                atta.setSize(attInfo.getSize());
		                atta.setFilename(attInfo.getFilename());
		                atta.setFileUrl(attInfo.getFileUrl());
		                atta.setMimeType(attInfo.getMimeType());
		                atta.setCreatedate(attInfo.getCreatedate());
		                atta.setDescription(attInfo.getDescription());
		                atta.setGenesisId(attInfo.getGenesisId());
		                attachmentDAO.save(atta);
		           }
		       }
			}
			
	        if(attaFlag){
	            ColUtil.setHasAttachments(summary, attaFlag);
	        }
	        
	        
	        summary.setCanDueReminder(false);
	        summary.setAudited(false);
	        summary.setVouch(CollaborationEnum.vouchState.defaultValue.ordinal());
	        summary.setBodyType(String.valueOf(content.getContentType()));
	        summary.setSubject(ColUtil.makeSubject(template, summary, null));
	        CtpAffair affair = new CtpAffair();
	        
	        affair.setIdIfNew();
	        affair.setCreateDate(now);
	        affair.setApp(ApplicationCategoryEnum.collaboration.key());
	        affair.setSubApp(ApplicationSubCategoryEnum.collaboration_tempate.key());
	        affair.setSubject(summary.getSubject());
	        affair.setReceiveTime(now);
	        affair.setUpdateDate(now);
	        affair.setMemberId(sender);
	        //affair对象的ObjectID关联协同的ID
	        affair.setObjectId(summary.getId());
	        affair.setSubObjectId(null);
	        //当前用户（发送者的）
	        affair.setSenderId(sender);
	        affair.setState(StateEnum.col_sent.key()); 
	        affair.setSubState(SubStateEnum.col_normal.key());
	        if(saveDraft){
	            affair.setState(StateEnum.col_waitSend.key());
	            affair.setSubState(SubStateEnum.col_waitSend_draft.key());
	        }
	        affair.setTempleteId(summary.getTempleteId());
	        affair.setBodyType(bodyType);
	        affair.setImportantLevel(summary.getImportantLevel());
	        affair.setResentTime(summary.getResentTime());
	        affair.setForwardMember(summary.getForwardMember());
	        affair.setNodePolicy("collaboration");//协同发起人节点权限默认为协同
	        affair.setTrack(1);
	        affair.setDelete(false);
	        affair.setArchiveId(null);
	        //affair.setIdentifier("0");
	        //三个Boolean类型初始值，解决PostgreSQL插入记录异常问题
	        affair.setFinish(false);
	        affair.setCoverTime(false);
	        affair.setDueRemind(false);
			affair.setOrgAccountId(member.getOrgAccountId());
			affair.setFormAppId(formBeanId);
			affair.setFormRecordid(masterId);
	        if(summary.getDeadline() != null && summary.getDeadline() > 0){
	            AffairUtil.addExtProperty(affair, AffairExtPropEnums.processPeriod,summary.getDeadline());
	        }
	        if(saveDraft){
	            affair.setIdentifier(IdentifierUtil.newIdentifier(affair.getIdentifier(), 20,'0'));
	        }
	        affairManager.save(affair);

	        AffairData affairData = ColUtil.getAffairData(summary);
	        
	        Long flowPermAccountId = ColUtil.getFlowPermAccountId(member.getOrgAccountId(), summary);
	        affairData.addBusinessData(ColConstant.FlowPermAccountId, flowPermAccountId);
	        if (ColUtil.isForm(content.getContentType())) {
    	        //只有表单正文的时候才有这两个表单参数，否则工作流分支判断会出错
    	        affairData.setFormAppId(content.getContentTemplateId());//表单ID
    	        affairData.setFormRecordId(content.getContentDataId());//form主数据ID
	        }
	        RunCaseResult runCaseResult =null;

	        Map<String,Object> globalMap = new HashMap<String,Object>();
	        if(!saveDraft){
	            
	            //分支条件
	        	String conditon_Str = (String)param.get(BPM_REQUEST_NODECONDITION);
	        	String dynamicFormMasterIds = (String)param.get(BPM_REQUEST_DYNAMICFORMMASTERIDS);
	        	
	        	runCaseResult = runWorkFlow(template, member,conditon_Str, dynamicFormMasterIds, 
	                    summary, user, affairData,affair,globalMap);
	            
	            summary.setProcessId(runCaseResult.getProcessId());
	            summary.setCaseId(Long.valueOf(runCaseResult.getCaseId()));
	            ColUtil.updateCurrentNodesInfo(summary);
	        }else{
	            summary.setStartDate(null);
	            summary.setState(null);
	            summary.setProcessId("");
	            summary.setCaseId(null);
	        }
	        colManager.saveColSummary(summary);
            //预归档
            if (summary.getArchiveId() != null) {
//            	docFilingManager.pigeonholeAsLinkWithoutAcl(ApplicationCategoryEnum.collaboration.key(), 
//                        affair.getId(), ColUtil.isHasAttachments(summary), summary.getArchiveId(), sender, null);
            }
            collaborationTemplateManager.updateTempleteHistory(summary.getTempleteId(),sender);
            
            if(saveDraft){
                //调用表单万能方法,更新状态，触发子流程等
                if(ColUtil.isForm(summary.getBodyType())){
                	List<Comment> commentList =  ctpCommentManager.getCommentAllByModuleId(ModuleType.collaboration,summary.getId());
                	capFormManager.updateDataState(summary,affair,ColHandleType.save,commentList);
                }
            }else{
            	SuperviseMessageParam smp = ColSelfUtil.convertTOSMP(summary);
            	superviseManager.saveSuperviseByCopyTemplete(member.getId(), smp, template.getId());
				// 督办消息发送
				if (subject == null) {
					subject = summary.getSubject();
				}
				//V6.0会有通过接口发起表单模板，督办人收到两条督办消息，这里取消接口发送消息的设置
				//	sendSuperviseMessage(subject, member, user, templateId, summary);
                //记录流程日志
                //设置下一个节点的节点名和节点权限
                String[] nextNodeNames = runCaseResult.getNextMembers().split("[,]");
                String toMmembers = Strings.join(",", nextNodeNames);
                appLogManager.insertLog(user, AppLogAction.Coll_New, user.getName(), summary.getSubject());
                processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),-1l, ProcessLogAction.sendForm, toMmembers);
                //定时任务超期提醒和提前提醒
                CollaborationJob.createQuartzJobOfSummary(summary,workTimeManager);
                //触发协同立方事件
                //V6.0不需要通过这种方式触发协同立方
                //EventDispatcher.fireEvent(colevent);
                
                //调用表单万能方法,更新状态，触发子流程等
                if(ColUtil.isForm(summary.getBodyType())){
                	List<Comment> commentList =  ctpCommentManager.getCommentAllByModuleId(ModuleType.collaboration,summary.getId());
                	capFormManager.updateDataState(summary,affair,ColHandleType.send,commentList);
                }
                //事件通知
                CollaborationStartEvent event = new CollaborationStartEvent(this);
                event.setSummaryId(summary.getId());
                event.setFrom("pc");
                event.setAffair(affair);
                EventDispatcher.fireEventAfterCommit(event);
                
            }
            if (AppContext.hasPlugin("index")) {
                if(ColUtil.isForm(summary.getBodyType())){
                    indexApi.add(summary.getId(), ApplicationCategoryEnum.form.getKey());
                }else{
                    indexApi.add(summary.getId(), ApplicationCategoryEnum.collaboration.getKey());
                }
            }
            result.put(BPM_RESPONSE_PROCESSID, summary.getProcessId());
            result.put(BPM_RESPONSE_SUBJECT, summary.getSubject());
		}catch(Throwable e){
			LOG.error("",e);
			result.put(BPM_RESPONSE_STATUS, BPM_RESPONSE_ERROR);
			result.put(BPM_RESPONSE_ERRORMSG, e.getLocalizedMessage());
		}
		return result;
	}
	
	private RunCaseResult runWorkFlow(CtpTemplate template, V3xOrgMember member, String conditon_Str, String dynamicFormMasterIds, 
	        ColSummary summary, User user, AffairData affairData,CtpAffair affair,Map<String,Object> globalMap)
	                throws BPMException {
	    
	    
        WorkflowBpmContext context = new WorkflowBpmContext();
        
        context.setAppName(ModuleType.collaboration.name());
        context.setStartUserId(String.valueOf(member.getId()));
        context.setStartUserName(member.getName());
        context.setStartAccountId(String.valueOf(member.getOrgAccountId()));
        context.setStartAccountName("seeyon");
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData("bizObject", summary);
        context.setBusinessData("ColSummary", summary);
        context.setBusinessData("operationType", affairData.getBusinessData().get("operationType"));
        context.setProcessTemplateId(String.valueOf(template.getWorkflowId()));
        LOG.info("conditon_Str:="+conditon_Str);
        context.setConditionsOfNodes(conditon_Str.toString());
        context.setDynamicFormMasterIds(dynamicFormMasterIds);
        
        context.setVersion("2.0");
        if(affairData.getFormRecordId() != null && affairData.getFormRecordId() != -1){
            context.setMastrid(""+affairData.getFormRecordId());
            context.setFormData(""+affairData.getFormAppId());
        }
        //防止当第一个处理节点为空节点
        context.setBusinessData("CtpAffair", affair);
        context.setBusinessData(WorkFlowEventListener.WF_APP_GLOBAL,globalMap);
        
        return wapi.transRunCaseFromTemplate(context);
    }
	
	/** 
     * @param summary
     * @param summaryIds
     * @throws BusinessException 
     */
	private void loopAttachmentList(ColSummary summary, String[] summaryIds,String Type) throws BusinessException {
	   for(String summaryId : summaryIds){
	       List<CtpAffair> affairs = affairManager.getAffairs(Long.valueOf(summaryId), StateEnum.col_sent);
	       if (affairs != null && affairs.size() == 1){
	           CtpAffair affair = affairs.get(0);
	           Attachment atta = new Attachment();
	           atta.setIdIfNew();
	           atta.setReference(summary.getId());
	           atta.setSubReference(summary.getId());
	           atta.setCategory("col".equals(Type)?ApplicationCategoryEnum.collaboration.ordinal():ApplicationCategoryEnum.edoc.ordinal());
	           atta.setType(2);
	   		   atta.setSize(0L);
	           atta.setFilename(affair.getSubject());
	           atta.setFileUrl(affair.getId());
	           atta.setMimeType("col".equals(Type)?"collaboration":"edoc");
	           atta.setCreatedate(affair.getCreateDate());
	           atta.setDescription(affair.getId()+"");
	           atta.setGenesisId(affair.getId());
	           attachmentDAO.save(atta);
	       }
	   }
	}

	@Override
	public Map<String, String> transPreStartProcess(Map<String, Object> appData) throws BusinessException {
	    
	    Map<String, String> ret = new HashMap<String, String>();
	    User user = AppContext.getCurrentUser();
	    
	    CtpTemplate template = null;
	    
	    String templateCode = (String)appData.get(SeeyonBPMAppHandler.BPM_REQUEST_TEMPLATECODE);
	    if(Strings.isBlank(templateCode)){
	    	ret.put(SeeyonBPMAppHandler.BPM_RESPONSE_STATUS, SeeyonBPMAppHandler.BPM_RESPONSE_ERROR);
            ret.put(SeeyonBPMAppHandler.BPM_RESPONSE_ERRORMSG, "templateCode is null");
            return ret;
	    }
	        
        template = templateManager.getTempleteByTemplateNumber(templateCode);
        
        if (template == null || template.getWorkflowId()==null) {
            ret.put(SeeyonBPMAppHandler.BPM_RESPONSE_STATUS, SeeyonBPMAppHandler.BPM_RESPONSE_ERROR);
            ret.put(SeeyonBPMAppHandler.BPM_RESPONSE_ERRORMSG, ResourceUtil.getString("coll.template.no.existence", templateCode));
            return ret;
        }
        
        boolean auth= templateManager.isTemplateEnabled(template.getId(), user.getId());
        if(!auth){
        	ret.put(SeeyonBPMAppHandler.BPM_RESPONSE_STATUS, SeeyonBPMAppHandler.BPM_RESPONSE_ERROR);
            ret.put(SeeyonBPMAppHandler.BPM_RESPONSE_ERRORMSG, user.getName()+ResourceUtil.getString("coll.template.is.not.permission")+template.getSubject());
            return ret;
        }
        
        appData.put(SeeyonBPMAppHandler.BPM_REQUEST_CTPTEMPLATE, template);
        appData.put("processTemplateId", template.getWorkflowId());
        
        //表单才执行表单数据保存
        if(ColUtil.isForm(template.getBodyType())){
        	try {
        		String rightId = wapi.getNodeFormViewAndOperationName(template.getWorkflowId(), null);
        		
        		//是否为保存待发的数据
        		String draft= (String)appData.get(SeeyonBPMAppHandler.BPM_REQUEST_DRAFT);
    			boolean saveDraft = "1".equals(draft);
        		int saveType = CAPFormEnum.SaveType.SAVE.getType();
        		if(saveDraft){
        			saveType = CAPFormEnum.SaveType.SAVE_WAIT.getType();
        		}
        		
        		Long dateMasterBeanId =  capFormManager.structureFormDataMasterBeanByDataJson(template.getFormAppId(), rightId, JSONUtil.toJSONString(appData.get("data")),saveType);
        		appData.put(BPM_REQUEST_MASTERID, dateMasterBeanId);
        		appData.put(FORM_BEAN_ID, template.getFormAppId());
        	} catch (Exception e) {
        		LOG.error("", e);
        		ret.put(BPM_RESPONSE_STATUS, BPM_RESPONSE_ERROR);
        		ret.put(BPM_RESPONSE_ERRORMSG, e.getLocalizedMessage());
        		return ret;
        	}
        }
	    ret.put(BPM_RESPONSE_STATUS, BPM_RESPONSE_SUCCESS);
		return ret;
	}
    
    private ColSummary createColSummary(CtpTemplate template) {
        String strXml = template.getSummary();
        ColSummary summary = XMLCoder.decoder(strXml,ColSummary.class);
        summary.setIdIfNew();
        return summary;
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
    private void putThreadContext(String jsonStr){
        
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY, jsonStr);
        
        Object jsonObj = JSONUtil.parseJSONString(Strings.removeEmoji(jsonStr), Map.class);
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY, jsonObj);
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
    
	@Override
	public String[] preFinishAndZcdb(Map<String, Object> appData, Long workitemId) throws BusinessException {
	    
	    CtpAffair affair = affairManager.getAffairBySubObjectId(workitemId);
	    ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
	    
	    appData.put("affair", affair);
	    appData.put("summary", summary);
	    
	    CtpTemplate template = (CtpTemplate)appData.get(BPM_REQUEST_CTPTEMPLATE); 
        if(template == null && summary.getTempleteId() != null){
            template = templateManager.getCtpTemplate(summary.getTempleteId());
            appData.put(BPM_REQUEST_CTPTEMPLATE, template);
        }
	    
	    return new String[]{"0", ""};
	}
	
	
	@Override
	public String[] beforFinishAndZcdb(Map<String, Object> appData) throws BusinessException {
        
        String[] ret = new String[2];
        
        String error = "1", success = "0";
        ret[0] = error;
        
        CtpAffair affair = (CtpAffair) appData.get("affair");
        
        //TODO，表单加锁， 现在表单不能提交数据， 暂时不考虑
        String checkRet = colManager.checkAffairValidAndIsLock(affair.getId().toString(), "false",affair.getNodePolicy());
        
        Map<String, Object> result = AjaxJsonUtil.parseAjaxJson(checkRet);
        String checkMsg = AjaxJsonUtil.checkJsonResult(result);
        
        if(Strings.isNotBlank(checkMsg)){
            ret[1] = checkMsg;
        }else{
            ret[0] = success;
        }
        
        //TODO, 这个点表单预提交
        
        
        //TODO collaborationFormBindEventListener.achieveTaskType
        //事件
        
        return ret;
    }
	
	@Override
	public String[] transFinishAndZcdb(Map<String, Object> appData) throws BusinessException {
	    
	    
	    String[] ret = new String[2];
        
        String error = "1", success = "0";
        ret[0] = error;
        boolean exeStop = false;
	    
	    ColSummary summary = (ColSummary) appData.get("summary");
        CtpAffair affair = (CtpAffair) appData.get("affair");
        String submitTypeString = (String)appData.get("submitType");
        Integer submitType = SeeyonBPMHandSubmitTypeEnum.submit.getKey();
        if("2".equals(submitTypeString)){
        	submitType = SeeyonBPMHandSubmitTypeEnum.zcdb.getKey();
        }
        
      //检查代理，避免不是处理人也能处理了。
        boolean canDeal = true;
        try {
            canDeal = ColUtil.checkAgent(affair, summary, false);
        } catch (Exception e1) {
            LOG.error("", e1);
            ret[1] = ResourceUtil.getString("coll.summary.validate.lable1");
        }
        if (!canDeal) {
            ret[1] = ResourceUtil.getString("coll.summary.validate.lable2");
            return ret;
        }
        
        
        if("bulletionaudit".equals(affair.getNodePolicy()) || "newsaudit".equals(affair.getNodePolicy())){
        	ret[1] = ResourceUtil.getString("coll.summary.validate.lable3");
        	return ret;
        }
        
        Map<String, Object> _json_params = new HashMap<String, Object>();
        
        //构建工作流数据
        Map<String, String> workflowParam = new HashMap<String, String>();
        workflowParam.put("process_desc_by",  "xml");
        workflowParam.put("process_xml",  (String)appData.get("processXML"));
        workflowParam.put("readyObjectJSON",  (String)appData.get("readyObjectJSON"));
        workflowParam.put("workflow_data_flag",  "WORKFLOW_SEEYON");
        workflowParam.put("process_info",  "");
        workflowParam.put("process_info_selectvalue",  "");
        workflowParam.put("process_subsetting",  "");
        workflowParam.put("moduleType",  "1");
        workflowParam.put("workflow_newflow_input",  (String)appData.get(SeeyonBPMAppHandler.BPM_REQUEST_NEWFLOWINPUT));
        workflowParam.put("process_rulecontent",  "");
        workflowParam.put("workflow_node_peoples_input",  "");
        workflowParam.put("workflow_node_condition_input",  (String)appData.get(BPM_REQUEST_NODECONDITION));
        workflowParam.put("dynamicFormMasterIds",  (String)appData.get(BPM_REQUEST_DYNAMICFORMMASTERIDS));
        workflowParam.put("processId",  summary.getProcessId());
        workflowParam.put("caseId",  summary.getCaseId().toString());
        workflowParam.put("subObjectId",  affair.getSubObjectId().toString());
        workflowParam.put("currentNodeId",  affair.getActivityId().toString());
        workflowParam.put("process_message_data",  (String)appData.get("messageDataList"));
        workflowParam.put("processChangeMessage",  (String)appData.get("processChangeMessage"));
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
        
        
        V3xOrgMember currentUser = (V3xOrgMember) appData.get(SeeyonBPMAppHandler.BPM_REQUEST_LOGINMEMBER);
        CtpTemplate template = (CtpTemplate)appData.get(BPM_REQUEST_CTPTEMPLATE); 
        if(template == null && summary.getTempleteId() != null){
            template = templateManager.getCtpTemplate(summary.getTempleteId());
        }
        
        String configItem = ColUtil.getPolicyByAffair(affair).getId();
        Long accountId = summary.getOrgAccountId();
        Long startMenberId = summary.getStartMemberId();
        //兼容320升级上来的数据accountId为空的情况
        if (null == accountId && startMenberId != null) {
            V3xOrgMember orgMember = orgManager.getMemberById(startMenberId);
            accountId = orgMember.getOrgAccountId();
        }
        if(template != null){
            accountId = ColUtil.getFlowPermAccountId(currentUser.getOrgAccountId(), accountId, template.getOrgAccountId());
        }
        
        String category = EnumNameEnum.col_flow_perm_policy.name();
        Permission permission = null;
        try{
            permission = permissionManager.getPermission(category, configItem, accountId);
            //用于判断当前节点权限是否存在，如果不存在则给知会的提示
            if(permission != null) { 
                if (!configItem.equals(permission.getName()) && affair.getState()==StateEnum.col_pending.getKey()) {
                    ret[1] = ResourceUtil.getString("coll.summary.validate.lable4");
                    exeStop = true;
                }
            }
        }catch(Exception e){
            String infoStr = "获取节点权限报错category:"+category +" caonfigItem:"+configItem +" accountId:"+accountId;
            LOG.error(infoStr, e);
            ret[1] = infoStr;
            exeStop = true;
        }
        
        if(!exeStop){
            Map<String, String> colSummaryData = new HashMap<String, String>();
            colSummaryData.put("contentstr", "");
            colSummaryData.put("summaryId", summary.getId().toString());
            colSummaryData.put("isDeleteSupervisior", "false");
            colSummaryData.put("processId", summary.getProcessId());
            colSummaryData.put("subject", summary.getSubject());
            colSummaryData.put("createDate", DateUtil.format(summary.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
            
            if(permission != null){
                //Integer opinion = permission.getNodePolicy().getOpinionPolicy();
                Integer opinion = permission.getNodePolicy().getOpinionPolicy();
                if(opinion != null && opinion.intValue() == 1){
                    colSummaryData.put("canDeleteORarchive", "true");
                }else{
                    colSummaryData.put("canDeleteORarchive", "false");
                }
                colSummaryData.put("cancelOpinionPolicy", String.valueOf(permission.getNodePolicy().getCancelOpinionPolicy()));
                colSummaryData.put("disAgreeOpinionPolicy", String.valueOf(permission.getNodePolicy().getDisAgreeOpinionPolicy()));
            }
            
            colSummaryData.put("bodyType", summary.getBodyType());
            colSummaryData.put("modifyFlag", "0");
            colSummaryData.put("isLoadNewFile", "0");
            colSummaryData.put("attModifyFlag", "0");
            colSummaryData.put("flowPermAccountId", accountId.toString());
            
            if(template != null){
                colSummaryData.put("templateColSubject", template.getColSubject());
                if(template.getWorkflowId() != null){
                    colSummaryData.put("templateWorkflowId", template.getWorkflowId().toString());
                }else {
                    colSummaryData.put("templateWorkflowId", "");
                }
            }
            
            _json_params.put("colSummaryData", colSummaryData);
            
            //TODO 意见必填校验
            Map<String, String> paramComment = (Map<String, String>) appData.get("comment_deal");
            String attitude = paramComment.get("attitude");
            String content = paramComment.get("content");
            Map<String, String> comment_deal = getComment(affair, attitude, content);
            _json_params.put("comment_deal", comment_deal);
            
            putThreadContext(_json_params);
            try {
                
              //处理参数
                Map<String, Object> params = new HashMap<String, Object>();
                
              //跟踪参数
                Map<String, String> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
                params.put("trackParam", trackPara);
                
                //取出模板信息
                Map<String,Object> templateMap = (Map<String,Object>)ParamUtil.getJsonDomain("colSummaryData");
                params.put("templateColSubject", templateMap.get("templateColSubject"));
                params.put("templateWorkflowId", templateMap.get("templateWorkflowId"));
                if(SeeyonBPMHandSubmitTypeEnum.submit.getKey()==submitType){
                	colManager.transFinishWorkItem(summary, affair, params);
                }else{
                	colManager.transDoZcdb(summary, affair, params);
                }
                ret[0] = success;
                
            } finally {
                removeThreadContext();
            }
        }
        
	    return ret;
	}
	

	/* (non-Javadoc)
	 * @see com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#transStepStop(java.util.Map)
	 */
	@Override
	public Map<String, String> transStepStop(Map<String, Object> requestData) throws BusinessException {
		String workitemId = (String) requestData.get(SeeyonBPMAppHandler.BPM_REQUEST_WORKITEMID);

		Map<String, Object> appData = (Map<String, Object>) requestData.get(SeeyonBPMAppHandler.BPM_REQUEST_APPDATA);
		if (Strings.isBlank(workitemId)) {
			LOG.info("CollaborationBPMHandlerImpl.transStepStop 参数workitemId 为空");
		}
		else {
			Map<String, Object> params = new HashMap<String, Object>();
			CtpAffair affair = affairManager.getAffairBySubObjectId(Long.valueOf(workitemId));
			String repealComment = (String) appData.get("stopOpinion");
			params.put("affairId", String.valueOf(affair.getId()));
			params.put("repealComment", repealComment);
			colManager.transStepStop(params);
		}

		return null;
	}
	/* (non-Javadoc)
	 * @see com.seeyon.ctp.rest.resources.SeeyonBPMAppHandler#transRepeal(java.util.Map)
	 */
	@Override
	public Map<String, String> transRepeal(Map<String, Object> requestData) throws BusinessException {
		String workitemId = (String) requestData.get(SeeyonBPMAppHandler.BPM_REQUEST_WORKITEMID);

		Map<String, Object> appData = (Map<String, Object>) requestData.get(SeeyonBPMAppHandler.BPM_REQUEST_APPDATA);
		if (Strings.isBlank(workitemId)) {
			LOG.info("CollaborationBPMHandlerImpl.transStepStop 参数workitemId 为空");
		}
		else {
//			Map<String, Object> params = new HashMap<String, Object>();
			CtpAffair affair = affairManager.getAffairBySubObjectId(Long.valueOf(workitemId));
			String repealComment = (String) appData.get("stopOpinion");
//			params.put("affairId", String.valueOf(affair.getId()));
//			params.put("repealComment", repealComment);
			colManager.transRepalBackground(affair.getObjectId(), affair.getId(), repealComment, 
					TemplateEnum.TrackWorkFlowType.notTrack.name(), WorkFlowEventListener.CANCEL,null,null,new HashMap<String,Object>());
		}

		return null;
	}

	
	/**
	 * 从App data中获取事件数据
	 */
	@Override
    public WorkflowBpmContext getEventContext(final Map<String, Object> appData){
        
	    WorkflowBpmContext context = null;
	    ColSummary summary = (ColSummary) appData.get("summary");
	    CtpAffair affair = (CtpAffair) appData.get("affair");
	    
	    Long templateId = summary.getTempleteId();
	    boolean isTemplate = !(templateId == null || templateId == 0 || templateId == -1);
	    
	    //模板才执行事件

	        context = new WorkflowBpmContext();
	        
	        Long formRecordid = summary.getFormRecordid();
	        if(formRecordid != null){
	            context.setFormData(formRecordid.toString());
	        }else{
	            context.setFormData("");
	        }
	        context.setMastrid(context.getFormData());
	        
	        CtpTemplate template = (CtpTemplate)appData.get(BPM_REQUEST_CTPTEMPLATE); 
	        if(template != null && template.getWorkflowId() != null){
	            context.setProcessTemplateId(template.getWorkflowId().toString());
	        }
	        context.setProcessId(summary.getProcessId());
	        context.setCurrentActivityId(affair.getActivityId().toString());
	        context.setBussinessId(summary.getId().toString());
	        context.setAffairId(affair.getId().toString());
	        context.setAppName(ApplicationCategoryEnum.collaboration.name());
	        context.setCurrentWorkitemId(affair.getSubObjectId());
	        
	        V3xOrgMember currentUser = (V3xOrgMember) appData.get(SeeyonBPMAppHandler.BPM_REQUEST_LOGINMEMBER);
	        context.setCurrentUserId(currentUser.getId().toString());
	        
	        Long formAppId = summary.getFormAppid();
            if(formAppId != null){
                context.setFormAppId(formAppId.toString());
            }else{
                context.setFormAppId("");
            }
            
            //表单操作视图
            context.setFormViewOperation(affair.getMultiViewStr());
            
            //context["matchRequestToken"] = matchRequestToken;
            //context["processXml"] = processXml;

        return context;
    }
	
	@Override
	public WorkflowBpmContext getBeforeInvokeWorkFlowContext(Map<String, Object> appData) {
	    
	    WorkflowBpmContext context = getEventContext(appData);
	    ColSummary summary = (ColSummary) appData.get("summary");
	    CtpAffair affair = (CtpAffair) appData.get("affair");
	    if(summary != null && context != null){
	        
	        context.setProcessTemplateId("");
	        context.setCaseId(summary.getCaseId());
	        context.setCurrentWorkitemId(affair.getSubObjectId());
	        
	        V3xOrgMember currentUser = (V3xOrgMember) appData.get(SeeyonBPMAppHandler.BPM_REQUEST_LOGINMEMBER);
	        context.setCurrentUserId(currentUser.getId().toString());
	        context.setCurrentAccountId(currentUser.getOrgAccountId().toString());
	        context.setDebugMode(false);
	        //"currentWorkItemIsInSpecial" : false,
	        context.setIsValidate(true);
	        context.setUseNowExpirationTime("true");
	    }
	    
	    return context;
	}
	
	@Override
	public String[] preTackBack(Map<String, Object> appData, Long workitemId) throws BusinessException {
	    
	    return preFinishAndZcdb(appData, workitemId);
	}
	
	/**
	 * @see SeeyonBPMAppHandler#tackBackCheck
	 * data.affairId String 必填
	 */
    @Override
    public String[] tackBackCheck(final Map<String, Object> appData) {
        
        String[] ret = new String[2];
        
        String error = "1", success = "0";
        
        ret[0] = error;
        
        CtpAffair affair = (CtpAffair) appData.get("affair");
        String affairId = affair.getId().toString();
        try {
            String state = colManager.getAffairState(affairId);
            
            if(String.valueOf(StateEnum.col_done.getKey()).equals(state)){
               ret[0] = success; 
            }else{
                ret[1] = ResourceUtil.getString("collaboration.listDone.tabkeback.state.js");
            }
        } catch (BusinessException e) {
            LOG.error("", e);
            ret[1] = ResourceUtil.getString("coll.summary.validate.lable5");
        }
        return ret;
    }

    @Override
    public String[] transTackBack(Map<String, Object> appData) throws BusinessException {
        
        String[] ret = new String[2];
        
        String error = "1", success = "0";
        ret[0] = error;
        
        CtpAffair affair = (CtpAffair) appData.get("affair");
        
        String affairId = affair.getId().toString();
        String isSaveOpinion = (String) appData.get("isSaveOpinion");
        boolean saveOption = false;
        if(isSaveOpinion != null){
            saveOption = Boolean.valueOf(isSaveOpinion);
        }
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("affairId", affairId);
        params.put("isSaveOpinion", saveOption);
        
        String msg = colManager.transTakeBack(params);
        
        if(Strings.isNotBlank(msg)){
            ret[1] = msg;
        }else {
            ret[0] = success;
        }
        
        return ret;
    }
    
    @Override
    public String[] preStepBack(Map<String, Object> appData, Long workitemId) throws BusinessException {
        return preFinishAndZcdb(appData, workitemId);
    }
    
    
    @Override
    public String[] stepBackCheck(Map<String, Object> appData) {
        
        String[] ret = new String[2];
        
        String error = "1", success = "0";
        
        ret[0] = error;
        
        CtpAffair affair = (CtpAffair)appData.get("affair");
        String affairId = affair.getId().toString();
        
        try {
            String checkRet = colManager.checkAffairValid(affairId,affair.getNodePolicy());
            if(Strings.isBlank(checkRet)){
                ret[0] = success; 
            }else {
                ret[1] = checkRet;
            }
            
        } catch (BusinessException e) {
            LOG.error("", e);
            ret[1] = ResourceUtil.getString("coll.summary.validate.lable5");
        }
        return ret;
    }

    @Override
    public String[] transStepBack(Map<String, Object> appData) throws BusinessException {
        
        
        String[] ret = new String[2];
        
        String error = "1", success = "0";
        ret[0] = error;
        
        CtpAffair affair = (CtpAffair) appData.get("affair");
        
        Long affairId = affair.getId();
        String summaryId = affair.getObjectId().toString();
        String trackWorkflowType = (String) appData.get("isWFTrace");
            
        Map<String,Object> tempMap=new HashMap<String, Object>();
        tempMap.put("affairId", affairId.toString());
        tempMap.put("summaryId", summaryId);
        tempMap.put("targetNodeId", "");
        tempMap.put("isWFTrace", trackWorkflowType);
        
        boolean isLock  = false;
        try{        
            isLock  = colLockManager.canGetLock(affairId);
            if (!isLock) {
                LOG.error(AppContext.currentAccountName()+"不能获取到map缓存锁，不能执行操作stepBack,affairId:"+affairId);
                ret[1] = ResourceUtil.getString("coll.summary.validate.lable6");
            }else{
                
                Map<String, Object> _json_params = new HashMap<String, Object>();
                
                //TODO 意见必填校验
                Map<String, String> paramComment = (Map<String, String>) appData.get("comment_deal");
                if(null!=paramComment){
                	String attitude = paramComment.get("attitude");
                	String content = paramComment.get("content");
                	Map<String, String> comment_deal = getComment(affair, attitude, content);
                	_json_params.put("comment_deal", comment_deal);
                }
                
                putThreadContext(_json_params);
                
                Map<String,String> returnValue = colManager.transStepBackReturnActivityIds(tempMap);
                String msg = null;
                if(null!=returnValue){
                	msg = returnValue.get("msg");
                }
                if (Strings.isNotBlank(msg)) {
                    ret[1] = msg;
                }else{
                    ret[0] = success;
                    ret[1] = returnValue ==null?"":returnValue.get("targetActivityIds");
                }
            }
        } finally{
            removeThreadContext();
            if(isLock){
                colLockManager.unlock(affairId);
            }
            colManager.colDelLock(affairId);
        }
        
        return ret;
    }
    
    /**
     * 封装意见
     * 
     * @param affair
     * @return
     * @throws BusinessException 
     *
     * @Since A8-V5 6.1SP1
     * @Author      : xuqw
     * @Date        : 2017年7月5日上午10:53:13
     *
     */
    private Map<String, String> getComment(CtpAffair affair, 
            String attitude, String content) throws BusinessException{
        
      //TODO 意见必填校验
        Map<String, String> comment_deal = new HashMap<String, String>();
        comment_deal.put("id", "");
        comment_deal.put("draftCommentId", "");
        
        //找到草稿意见
        CtpCommentAll draftComment = ctpCommentManager.getDrfatComment(affair.getId());
        if(draftComment != null){
            comment_deal.put("id", draftComment.getId().toString());
            comment_deal.put("draftCommentId", draftComment.getId().toString());
        }
        comment_deal.put("pid", "0");
        comment_deal.put("clevel", "1");
        comment_deal.put("path", AppContext.getCurrentUser().getUserAgentFrom()==null?"pc":AppContext.getCurrentUser().getUserAgentFrom());
        comment_deal.put("moduleType", String.valueOf(ApplicationCategoryEnum.collaboration.getKey()));
        comment_deal.put("moduleId", affair.getObjectId().toString());
        comment_deal.put("ctype", "0");
        comment_deal.put("hidden", "false");
        comment_deal.put("showToId", "");//"Member|-8492409909120645741"
        comment_deal.put("affairId", affair.getId().toString());
        comment_deal.put("relateInfo", "[]");
        comment_deal.put("pushMessage", "true");
        comment_deal.put("praiseInput", "0");
        //comment_deal.put("richContent", "");
        
        if("2".equals(attitude)){
            attitude = "collaboration.dealAttitude.agree";
        }else if("3".equals(attitude)){
            attitude = "collaboration.dealAttitude.disagree";
        }else if("1".equals(attitude)){
            attitude = "collaboration.dealAttitude.haveRead";
        }else {
            attitude = "";
        }
        
        comment_deal.put("extAtt1", attitude);
        comment_deal.put("content", content);
        
        return comment_deal;
    }
    
    @Override
    public String[] preSpecifyBack(Map<String, Object> appData, Long workitemId) throws BusinessException {
        return preFinishAndZcdb(appData, workitemId);
    }

    @Override
    public String[] transSpecifyBack(Map<String, Object> appData, String targetNodeId, String stepbackStyle) throws BusinessException {
        

        
        String[] ret = new String[2];
        
        String error = "1", success = "0";
        ret[0] = error;
        
        CtpAffair affair = (CtpAffair) appData.get("affair");
        ColSummary summary = (ColSummary) appData.get("summary");
        
        String workitemId = affair.getSubObjectId().toString();
        String processId = summary.getProcessId();
        String caseId = summary.getCaseId().toString();
        String activityId = affair.getActivityId().toString();
        String theStepBackNodeId = targetNodeId;
        String submitStyle = stepbackStyle;
        String summaryId = affair.getObjectId().toString();
        String affairId = affair.getId().toString();
        String isWfTrace = (String) appData.get("isWFTrace");
        String isCircleBack = "0";//不是环形流程， "1" 是环形流程
        
        if("1".equals(stepbackStyle)){
            isWfTrace = "0";
        }
        
        User user = AppContext.getCurrentUser();
        
        Map<String,Object> tempMap=new HashMap<String, Object>();
        tempMap.put("workitemId", workitemId);
        tempMap.put("processId", processId);
        tempMap.put("caseId", caseId);
        tempMap.put("activityId", activityId);
        tempMap.put("theStepBackNodeId", theStepBackNodeId);
        tempMap.put("submitStyle", submitStyle);
        tempMap.put("affairId", affairId);
        tempMap.put("summaryId", summaryId);
        tempMap.put("isWFTrace", isWfTrace);
        tempMap.put("isCircleBack", isCircleBack);
        
        
        try{
        
            Map<String, Object> _json_params = new HashMap<String, Object>();
            //TODO 意见必填校验
            Map<String, String> paramComment = (Map<String, String>) appData.get("comment_deal");
            if(null!=paramComment){
            	String attitude = paramComment.get("attitude");
            	String content = paramComment.get("content");
            	Map<String, String> comment_deal = getComment(affair, attitude, content);
            	_json_params.put("comment_deal", comment_deal);
            }
            
            putThreadContext(_json_params);
            
            // 处理意见
            Comment comment  = new Comment();
            ParamUtil.getJsonDomainToBean("comment_deal", comment);
            comment.setCreateDate(new Date());
            if(!user.getId().equals(affair.getMemberId())){
                comment.setExtAtt2(user.getName());
            }
            comment.setExtAtt3("collaboration.dealAttitude.rollback");
            
            tempMap.put("affair", affair);
            tempMap.put("summary", summary);
            tempMap.put("comment", comment);
            tempMap.put("user", user);
        
            colManager.updateAppointStepBack(tempMap);
            
            ret[0] = success;
            
        }finally{
            
            removeThreadContext();
            
            wapi.releaseWorkFlowProcessLock(summary.getProcessId(), user.getId().toString(), 14);//解除回退锁
        }
        
        
        return ret;
    }
    
    /**
     * 
     * 应用层校验数据是否可用
     * 
     * @param workitemId 业务自定义参数
     * @return
     * 返回 长度为2的数组，ret[0] - 错误码， ret[1] - 错误信息， 结果正常ret[0] = "0";
     */
    public String[] preReplaceItem(String workitemId,String nextMemeberId) throws BusinessException{
    	String[] ret = new String[2];
        
        String error = "1", success = "0";
        
        ret[0] = error;
        CtpAffair affair = affairManager.getAffairBySubObjectId(Long.valueOf(workitemId));
        if(null==affair){
			String state = ResourceUtil.getString("collaboration.state.9.delete");
			String msg = ResourceUtil.getString("collaboration.state.inexistence.alert", state);
			ret[1] = msg;
		}else{
			String msg = colManager.checkAffairValid(affair, true,affair.getNodePolicy());
			if(Strings.isNotBlank(msg)){
				ret[1] = msg; 
			}else{
				ret[0] = success;
			}
		}
        V3xOrgMember member = orgManager.getMemberById(Long.valueOf(nextMemeberId));
        if(member ==null){
        	ret[0] = error;
        	ret[1] = "Invalid nextMemeberId  " + nextMemeberId;
        }
        return ret;
    }
    
    /**
     * 
     * @param workitemId 业务自定义参数
     * 
     * @param nextMemeberId 转办的人员id
     * @return
     * 返回 长度为2的数组，ret[0] - 错误码， ret[1] - 错误信息， 结果正常ret[0] = "0";
     */
    public String[] transReplaceItem(String workitemId,String nextMemeberId) throws BusinessException{
    	String[] ret = new String[2];
        
        String error = "1", success = "0";
        
        ret[0] = error;
        
    	Map<String,String> param = new HashMap<String,String>();

        //获取对象
        CtpAffair  affair = affairManager.getAffairBySubObjectId(Long.valueOf(workitemId));
        Long affairId = affair.getId();
        param.put("affairId", affairId.toString());
        param.put("transferMemberId", nextMemeberId);
    	boolean isLock = false;
    	String msg = "";
    	try {
        	isLock = colLockManager.canGetLock(affairId);
        	if(!isLock) {
        		msg =  ResourceUtil.getString("collaboration.summary.notDuplicateSub");
        		LOG.error( AppContext.currentUserLoginName()+msg+",affairId"+affairId);
        	}
        	else{
        		msg = this.colManager.transColTransfer(param);
        		
        	}
		} finally {
			if(isLock){
				colLockManager.unlock(Long.valueOf(affairId));
			}
		}
    	if(Strings.isBlank(msg)){
    		ret[0] = success;
    	}else{
    		ret[1] = msg;
    	}
      return ret;
    }
    
    /**
     * 校验节点状态
     * @param activityId 需要替换的节点id
     * 
     * @return
     * 返回 长度为2的数组，ret[0] - 错误码， ret[1] - 错误信息， 结果正常ret[0] = "0";
     */
    public String[] preReplaceNode(BPMCase bpmCase, BPMActivity activity,Map<String,Object> appData) throws BusinessException{
    	String[] ret = new String[]{"0", ""};
    	String id = (String) appData.get("userId");
    	String name = "";
    	String excludeChildDepartment = "false";
    	String accountId = "";
    	String accountName = "";
    	String entityType = "";
		if(Strings.isNotBlank(id)){
			String[] user =  id.split("[|]");
			V3xOrgEntity orgEntity = orgManager.getEntityAnyType(Long.valueOf(user[0]));
			if(null!=orgEntity){
				id = orgEntity.getId().toString();
				entityType = orgEntity.getEntityType();
				if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entityType) && 
						user.length==2 &&"true".equals(user[1])){
					name = orgEntity.getName()+"(" + MessageUtil.getString("common.selectPeople.excludechilddepartment") + ")";
					excludeChildDepartment = user[1];
				}else{
					name = orgEntity.getName();
				}
				Long orgAccountId = orgEntity.getOrgAccountId();
				accountId=orgAccountId.toString();
				accountName=Functions.getAccountShortName(orgAccountId);
			}else{
				ret[0] = "1";
	        	ret[1] = ResourceUtil.getString("coll.summary.validate.lable7")+id;
	        	return ret;
			}
		}else{
			ret[0] = "1";
        	ret[1] = ResourceUtil.getString("coll.summary.validate.lable8");
        	return ret;
		}
		appData.put("userId", id);
		appData.put("name", name);
		appData.put("excludeChildDepartment", excludeChildDepartment);
		appData.put("accountId", accountId);
		appData.put("accountName", accountName);
		appData.put("entityType", entityType);
        return ret;
    }
    
    /**
     * 校验节点状态
     * @param activityId 需要替换的节点id
     * 
     * @return
     * 返回 长度为2的数组，ret[0] - 错误码， ret[1] - 错误信息， 结果正常ret[0] = "0";
     */
    public String[] transReplaceNode(String activityId,Map<String,Object> appData) throws BusinessException{
        
        return new String[]{"0", ""};
    }
    
    /**
     * 校验节点状态，是否可以执行减签
     * @param activityId 执行减签的节点id
     * 
     * @return
     * 返回 长度为2的数组，ret[0] - 错误码， ret[1] - 错误信息， 结果正常ret[0] = "0";
     */
    public String[] preDeleteNode(Map<String, Object> appData, Long workitemId) throws BusinessException{
    	getAffairMessage(appData,workitemId);
	    return new String[]{"0", ""};
    }
    
    /**
     * 校验节点状态，是否可以执行加签
     * @param workitemId 执行加签的节点id
     * 
     * @return
     * 返回 长度为2的数组，ret[0] - 错误码， ret[1] - 错误信息， 结果正常ret[0] = "0";
     */
    public String[] preAddNode(Map<String, Object> appData, Long workitemId,String changeType) throws BusinessException{
    	String[] ret= new String[]{"0", ""} ;
    	getAffairMessage(appData,workitemId);
    	List<String> userId = new ArrayList<String>();
    	List<String> userName = new ArrayList<String>();
    	List<String> userExcludeChildDepartment = new ArrayList<String>();
    	List<String> accountId = new ArrayList<String>();
    	List<String> accountShortName = new ArrayList<String>();
    	List<String> userType = new ArrayList<String>();
    	List<String> node_process_modes = new ArrayList<String>();
    	String policyName = "";
    	String policyId = "";
    	String dealTerm = "";
    	String remindTime = "";
    	Map<Object,Object>  addNodeInfo = (Map<Object, Object>) appData.get("add_node_info");
    	if(null!=addNodeInfo){
    		List<String> userIds = (List<String>)addNodeInfo.get("userId");
    		String node_process_mode = (String)addNodeInfo.get("node_process_mode");
    		if(Strings.isBlank(node_process_mode)){
				node_process_mode = "all";
			}
    		policyId = (String)addNodeInfo.get("policyId");
    		dealTerm = (String) addNodeInfo.get("dealTerm");
    		remindTime = (String) addNodeInfo.get("remindTime");
    		//解析组织架构信息
    		if(Strings.isNotEmpty(userIds)){
    			for(String userMessage:userIds ){
    				if(Strings.isNotBlank(userMessage)){
    					String[] user =  userMessage.split("[|]");
    					V3xOrgEntity orgEntity = orgManager.getEntityAnyType(Long.valueOf(user[0]));
    					if(null!=orgEntity){
    						userId.add(orgEntity.getId().toString());
    						String orgType = orgEntity.getEntityType();
    						//如果是部门不包含子部门需要修改显示信息
    						if(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(orgType) && 
    								user.length==2 &&"true".equals(user[1])){
    							userName.add(orgEntity.getName()+"(" + MessageUtil.getString("common.selectPeople.excludechilddepartment") + ")");
    							userExcludeChildDepartment.add("true");
    						}else{
    							userName.add(orgEntity.getName());
    							userExcludeChildDepartment.add("false");
    						}
    						Long orgAccountId = orgEntity.getOrgAccountId();
    						accountId.add(orgAccountId.toString());
    						accountShortName.add(Functions.getAccountShortName(orgAccountId));
    						userType.add(orgType);
    						node_process_modes.add(node_process_mode);
    					}else{
    						ret[0] = "1";
        		        	ret[1] = ResourceUtil.getString("coll.summary.validate.lable7")+userMessage;
        		        	return ret;
    					}
    				}
    			}
    		}
    	}
    	addNodeInfo.put("userId", userId);
    	addNodeInfo.put("userName", userName);
    	addNodeInfo.put("userExcludeChildDepartment", userExcludeChildDepartment);
    	addNodeInfo.put("accountId", accountId);
    	addNodeInfo.put("accountShortName", accountShortName);
    	addNodeInfo.put("userType", userType);
    	addNodeInfo.put("node_process_mode", node_process_modes);
    	
    	if(SeeyonBPMHandAddNodeTypeEnum.addNode.getKeyString().equals(changeType)){
    		if(Strings.isBlank(policyId) || !Strings.isDigits(policyId)){
    			ret[0] = "1";
    			ret[1] = ResourceUtil.getString("coll.summary.validate.lable9");
    			return ret;
    		}
    		//根据id获取节点权限信息
			PermissionVO permissionVO = permissionManager.getPermission(Long.valueOf(policyId));
			if(null!=permissionVO){
				policyId = permissionVO.getName();
				addNodeInfo.put("policyId", policyId);
			}else{
				ret[0] = "1";
				ret[1] = ResourceUtil.getString("coll.summary.validate.lable4");
				return ret;
			}
    	}
    	//知会默认节点名称为知会
    	if(SeeyonBPMHandAddNodeTypeEnum.info.getKeyString().equals(changeType)){
    		policyId = "inform";
    		addNodeInfo.put("policyId", policyId);
    		addNodeInfo.put("dealTerm", "");
    	}
    	//当前会签默认为当前节点的节点权限
    	CtpAffair affair = (CtpAffair) appData.get("affair");
    	if(SeeyonBPMHandAddNodeTypeEnum.Assign.getKeyString().equals(changeType)){
    		policyId = ColUtil.getPolicyByAffair(affair).getId();
    		addNodeInfo.put("policyId", policyId);
    		addNodeInfo.put("dealTerm", "");
    	}
    	dealTerm = (String) addNodeInfo.get("dealTerm");
		remindTime = (String) addNodeInfo.get("remindTime");
    	//判断提前提醒时间是否在节点期限之前
    	Date dealTermDate = Datetimes.parseDatetime(dealTerm);
    	if(null!=dealTermDate){
    		long shijiancha = dealTermDate.getTime() - new Date().getTime();
    		if(shijiancha<0){
    			ret[0] = "1";
    			ret[1] = ResourceUtil.getString("workflow.nodeProperty.remindTimeLessThanNow");
    			return ret;
    		}
    		if(Strings.isNotBlank(remindTime) && shijiancha/1000/60<Long.valueOf(remindTime)){
    			ret[0] = "1";
    			ret[1] = ResourceUtil.getString("workflow.nodeProperty.remindTimeLessThanDealDeadLine");
    			return ret;
    		}
    	}
    	//节点期限为空，节点期限和提前提醒时间都设置为空
    	if(null==dealTermDate){
    		addNodeInfo.put("dealTerm","");
    		addNodeInfo.put("remindTime","");
    	}
    	String category = EnumNameEnum.col_flow_perm_policy.name();
    	PermissionVO permissionVO = permissionManager.getPermissionVO(category, policyId, AppContext.currentAccountId());
        if(permissionVO==null || !permissionVO.getName().equals(policyId)){
        	ret[0] = "1";
        	ret[1] = ResourceUtil.getString("coll.summary.validate.lable4");
        	return ret;
        }
    	addNodeInfo.put("policyName", permissionVO.getLabel());
    	addNodeInfo.put("affairId", appData.get("affairId"));
    	addNodeInfo.put("summaryId", appData.get("summaryId"));
    	addNodeInfo.put("caseId", affair.getCaseId());
    	addNodeInfo.put("workitemId", workitemId);
	    return ret;
    }
    
    
    private void getAffairMessage(Map<String, Object> appData, Long workitemId)  throws BusinessException{
    	CtpAffair affair = affairManager.getAffairBySubObjectId(workitemId);
    	if(affair!=null){
    		appData.put("affair", affair);
    		appData.put("affairId", affair.getId().toString());
    		ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
    		appData.put("summary", summary);
    		appData.put("summaryId", summary.getId().toString());
    	}
    }
    
    /**
     * 处理XML，判断XML1.0还是XML2.0版本XML2.1版本
     * @param xmlData
     * @return int
     * 
     */
    public static double getXmlVersion(String xmlData) throws RuntimeException{
        Element rootElement = null;
        try {
            Document document = XXEUtil.safeParseText(xmlData); 
            rootElement = document.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("解析XML出错！",e);
        }
        if("formExport".equalsIgnoreCase(rootElement.getName()))
            return 2;
        if("forms".equalsIgnoreCase(rootElement.getName()))
            return 2.1;
        return 1;
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

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public void setColManager(ColManager colManager) {
		this.colManager = colManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}


	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setAttachmentDAO(AttachmentDAO attachmentDAO) {
		this.attachmentDAO = attachmentDAO;
	}

	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}

	public void setCollaborationTemplateManager(CollaborationTemplateManager collaborationTemplateManager) {
		this.collaborationTemplateManager = collaborationTemplateManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}

	public void setProcessLogManager(ProcessLogManager processLogManager) {
		this.processLogManager = processLogManager;
	}

	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}

	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }
	
	public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

	public void setCtpCommentManager(CommentManager ctpCommentManager) {
		this.ctpCommentManager = ctpCommentManager;
	}
	
	public void setColLockManager(ColLockManager colLockManager) {
        this.colLockManager = colLockManager;
    }
	
	
}
