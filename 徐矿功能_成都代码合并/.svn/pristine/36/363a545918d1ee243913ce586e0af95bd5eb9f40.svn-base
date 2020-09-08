/**
 * $Author: 翟锋$
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.json.JSONObject;

import com.seeyon.apps.collaboration.bo.ColInfo;
import com.seeyon.apps.collaboration.bo.SendCollResult;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.constants.ColConstant.SendType;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationStartEvent;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.quartz.CollaborationJob;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.apps.common.projectphaseevent.manager.ProjectPhaseEventManager;
import com.seeyon.apps.common.projectphaseevent.po.ProjectPhaseEvent;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.processlog.ProcessLogDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.template.NoSuchTemplateException;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.CollaborationTemplateManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.BPMSeeyonPolicyVO;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * @author zhaifeng
 *
 */
public class ColPubManagerImpl implements ColPubManager {
	private static Log LOG = CtpLogFactory.getLog(ColPubManagerImpl.class);
	
    private ColManager               colManager;
    private TemplateManager          templateManager;
    private OrgManager               orgManager;
    private AffairManager            affairManager;
    private AppLogManager            appLogManager;
    private ProcessLogManager        processLogManager;
    private AttachmentManager        attachmentManager;
    private WorkflowApiManager       wapi;
    private SuperviseManager         superviseManager;
    private IndexManager             indexManager;
    private CAPFormManager capFormManager;
    private WorkTimeManager          workTimeManager;
    private CtpTrackMemberManager    trackManager;
    private CommentManager           commentManager;
    private CollaborationTemplateManager collaborationTemplateManager;
    private ColMessageManager colMessageManager;
    private ISignatureHtmlManager        iSignatureHtmlManager;
    private ProjectPhaseEventManager projectPhaseEventManager;
    private ProjectApi projectApi;
    private FileManager fileManager;
	private DocApi docApi;
	private MainbodyManager contentManager = (MainbodyManager) AppContext.getBean("ctpMainbodyManager");
	private CustomizeManager        customizeManager;
    
    public void setProjectPhaseEventManager(
			ProjectPhaseEventManager projectPhaseEventManager) {
		this.projectPhaseEventManager = projectPhaseEventManager;
	}

	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}

	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}

	public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
		this.iSignatureHtmlManager = iSignatureHtmlManager;
	}

    
    
    public void setColMessageManager(ColMessageManager colMessageManager) {
		this.colMessageManager = colMessageManager;
	}

	public void setContentManager(MainbodyManager contentManager) {
		this.contentManager = contentManager;
	}

	public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setCollaborationTemplateManager(CollaborationTemplateManager collaborationTemplateManager) {
        this.collaborationTemplateManager = collaborationTemplateManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }


    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

 
    
    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setCommentManager(CommentManager commentManager) {
        this.commentManager = commentManager;
    }
    
    public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}
    
    @Override
    public  ColSummary transStartNewflowCaseFromHasFlow(Long templateId,Long senderId,Long formMasterId,Long parentSummaryId,String parentNodeId,Long parentAffairId,boolean isRelated)
        throws NoSuchTemplateException,BusinessException{
    	StringBuilder sb = new StringBuilder();
    	sb.append("transStartNewflowCaseFromHasFlow");
    	sb.append("\r\n templateId = "+templateId);
    	sb.append("\r\nsenderId = "+senderId);
    	sb.append("\r\nformMasterId = "+formMasterId);
    	sb.append("\r\n parentSummaryId = "+parentSummaryId);
    	sb.append("\r\n parentNodeId = "+parentNodeId);
    	sb.append("\r\n parentAffairId = "+parentAffairId);
    	sb.append("\r\n isRelated = "+isRelated);
    	LOG.error(sb);
        return this.transSendColl(ColConstant.SendType.auto, templateId, senderId, formMasterId,parentSummaryId).getSummary();
    }

    @Override
    public  ColSummary transStartNewflowCaseFromNoFlow(Long templateId,Long senderId,Long formMasterId,Long parentFormId, Long parentFormMasterId,int formType,boolean isRelated)
        throws NoSuchTemplateException,BusinessException{
    	StringBuilder sb = new StringBuilder();
    	sb.append("transStartNewflowCaseFromNoFlow");
    	sb.append("\r\n templateId = "+templateId);
    	sb.append("\r\nsenderId = "+senderId);
    	sb.append("\r\nformMasterId = "+formMasterId);
    	sb.append("\r\n parentFormId = "+parentFormId);
    	sb.append("\r\n parentFormMasterId = "+parentFormMasterId);
    	sb.append("\r\n formType = "+formType);
    	sb.append("\r\n isRelated = "+isRelated);
    	LOG.error(sb);
    	return this.transSendColl(ColConstant.SendType.auto, templateId, senderId, formMasterId).getSummary();
    }
    
    public SendCollResult transSendColl(SendType sendType, Long templateId,
			Long senderId, Long formMasterId) throws BusinessException {
		return transSendColl(sendType, templateId, senderId, formMasterId, null);
	}
    
    public SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId, Long formMasterId, Long parentSummaryId) throws NoSuchTemplateException,BusinessException {
    	return transSendColl(sendType, templateId, senderId, formMasterId, parentSummaryId, null,new HashMap<String,Object>());
    }
    public SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId,
            Long formMasterId, Long parentSummaryId, Map<String,Object> extParams) throws BusinessException{
        return transSendColl( sendType, templateId,  senderId,  formMasterId, parentSummaryId, null,extParams);
    }
    
    public SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId, 
            Long formMasterId, Long parentSummaryId,Long newSumamryId) throws NoSuchTemplateException,BusinessException {
       return transSendColl( sendType, templateId,  senderId,  formMasterId, parentSummaryId, newSumamryId,new HashMap<String,Object>());
    }
    /**
     * 发起新流程，表单数据请提前保存好, 然后把formMasterId给我
     * 
     * @param templateId 子(新)流程所属模板Id
     * @param senderId 发起者Id
     * @param formMasterId 表单数据记录主键Id值
     * @param parentSummaryId 
     * @param newSumamryId :当前流程的summaryID
     * @throws Exception
     */
    private SendCollResult transSendColl(ColConstant.SendType sendType, Long templateId, Long senderId, 
            Long formMasterId, Long parentSummaryId,Long newSumamryId,
            Map<String,Object> extParams) throws NoSuchTemplateException,BusinessException {
    	
    	
        //1、校验模板是否存在
        CtpTemplate template = templateManager.getCtpTemplate(templateId);
        if(template == null){
            throw new NoSuchTemplateException(templateId);
        }
        if(template.getState().intValue() == TemplateEnum.State.invalidation.ordinal()){
        	return null;
        }
        ColInfo info = new ColInfo();
       
        if(extParams!=null){
            Boolean wfSubPRCSkipFSender  = (Boolean) extParams.get(EventDataContext.CTP_WORKFLOW_SUBPROCESS_SKIP_FSENDER);
            info.setWfSubPRCSkipFSender(wfSubPRCSkipFSender == null ? false : wfSubPRCSkipFSender);
        }
        
        //2、工作流触发新流程
        V3xOrgMember sender = orgManager.getMemberById(senderId);
        if(sender == null){
            throw new BusinessException(ResourceUtil.getString("collaboration.error.common.create.flow.sender") + "senderId=" + senderId);//发起新流程失败，原因：发起者不存在。
        }
        
        //3、构造新的summary对象
        ColSummary newSummary = (ColSummary)XMLCoder.decoder(template.getSummary());
        if(newSumamryId != null){
        	newSummary.setId(newSumamryId);
        }else{
        	newSummary.setIdIfNew();
        }
        //计算流程超期时间。把时间段换成具体时间点
        if(newSummary.getDeadline() != null) {
        	Map<String,String> params = new HashMap<String,String>();
        	params.put("minutes", newSummary.getDeadline() == null ? "0":String.valueOf(newSummary.getDeadline()) );
        	Long deadlineDatetime = colManager.calculateWorkDatetime(params);
        	Date date = new Date(deadlineDatetime);
            String format2 = Datetimes.formatDatetimeWithoutSecond(date);
        	newSummary.setDeadlineDatetime(Datetimes.parseDatetime(String.valueOf(format2)));
        	if(newSummary.getDeadline() == null || 
        			(null != newSummary.getDeadline() && newSummary.getDeadline() == 0l)){
        		newSummary.setDeadlineDatetime(null);
        	}
        }
        newSummary.setAudited(false);
        newSummary.setVouch(CollaborationEnum.vouchState.defaultValue.ordinal());
        newSummary.setTempleteId(templateId);
        newSummary.setFormRecordid(formMasterId);
        
        //项目ID
        if((newSummary.getProjectId() == null || newSummary.getProjectId() == -1)
                && template.getProjectId() != null && template.getProjectId() != -1){
            newSummary.setProjectId(template.getProjectId());
        }
        
        //设置协同的formAppid
//        List<CtpContentAll> contentList = MainbodyService.getInstance().getContentList(ModuleType.collaboration, template.getId());
//        if(!Strings.isEmpty(contentList)){
//            CtpContentAll contentAll = contentList.get(0);
//            newSummary.setFormAppid(contentAll.getContentTemplateId());//表单ID
//        }
        
        //复制模板的附件给当前协同
        if(TemplateUtil.isHasAttachments(template)){
            attachmentManager.copy(template.getId(), template.getId(), newSummary.getId(), newSummary.getId(), ApplicationCategoryEnum.collaboration.key());//附件
            ColUtil.isHasAttachments(newSummary);
        }
        
        //子流程
        if(sendType == ColConstant.SendType.child){ 
            newSummary.setNewflowType(ColConstant.NewflowType.child.ordinal());
        }else if(sendType == ColConstant.SendType.auto){
        	newSummary.setNewflowType(ColConstant.NewflowType.auto.ordinal());
        }else if(newSummary.getNewflowType() == null){
            newSummary.setNewflowType(ColConstant.NewflowType.main.ordinal());
        }
        
        info.setComment(ResourceUtil.getString("collaboration.system.auto.start"));
        
        info.setBody(MainbodyType.getEnumByKey(Integer.valueOf(template.getBodyType())), null, formMasterId, new Date());
        
        User user = new User();
        user.setId(sender.getId());
        user.setName(sender.getName());
        user.setDepartmentId(sender.getOrgDepartmentId());
        user.setLoginAccount(sender.getOrgAccountId());
        user.setAccountId(sender.getOrgAccountId());
        
        info.setCurrentUser(user);
        info.setSummary(newSummary);
        info.setNewBusiness(true);
        String canTrackSend = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_SEND);
		if(Strings.isBlank(canTrackSend) || "true".equals(canTrackSend)){
			info.setTrackType(TrackEnum.all.ordinal());
		}
        //复制签章
        if(parentSummaryId!=null){
        	iSignatureHtmlManager.save(parentSummaryId, newSummary.getId());
        }
        
        return this.transSendColl(sendType, info);
    }
    
    @Override
    public SendCollResult transSendColl(ColConstant.SendType sendtype, ColInfo info) throws BusinessException{
        ColSummary summary = info.getSummary();
        CtpAffair affair = info.getSenderAffair();
        
        //同一个Session里面有2个相同的对象，提前更新以下。拉入hibernate session管理
        if (info.getSenderAffair() != null) {
            this.colManager.updateColSummary(summary);
            this.affairManager.updateAffair(affair);
        }
        
        SendCollResult sendCollResult = new SendCollResult();
        
        User user = info.getCurrentUser();
        if(user == null){
        	user = AppContext.getCurrentUser();
        }
        
        boolean isForm = ColUtil.isForm(summary.getBodyType());
        
        Date now =  new Date();
        Long senderId = user.getId();
        
        V3xOrgMember sender = this.orgManager.getMemberById(senderId);
        
        Long templateId = summary.getTempleteId();
        
        CtpTemplate template = null;
        
        Long permissionAccountId = user.getLoginAccount();
        if(templateId != null){
            template = templateManager.getCtpTemplate(templateId);
            permissionAccountId = template.getOrgAccountId();
        }
        
        // 新增模板调用历史记录
        Long _addRecentTemplateId = (null != info.getCurTemId()) ? info.getCurTemId() : summary.getTempleteId();
        if(_addRecentTemplateId != null) {
        	this.templateManager.addRecentTemplete(senderId,ModuleType.collaboration.getKey(),_addRecentTemplateId,TemplateEnum.RecentUseType.call);
        }
        
        if(isForm && (summary.getFormAppid() == null || info.getFormViewOperation() == null)){
        	BPMSeeyonPolicyVO startPolicy = wapi.getStartNodeFormPolicy(template.getWorkflowId());
            if(null != startPolicy){
                summary.setFormAppid(Long.valueOf(startPolicy.getFormApp()));//表单ID
                info.setFormViewOperation(startPolicy.getFormViewOperation());
                info.setDR(startPolicy.getDR());
            }
        }
        summary.setIdIfNew();
        summary.setCreateDate(now);
        summary.setStartDate(now);
        summary.setState(CollaborationEnum.flowState.run.ordinal());
        summary.setSubState(CollaborationEnum.SubState.Normal.ordinal());
        summary.setStartMemberId(senderId);
        summary.setOrgAccountId(user.getLoginAccount());
        summary.setOrgDepartmentId(user.getDepartmentId());
        summary.setPermissionAccountId(permissionAccountId);
        
        //发送的时候将接收人清空
        summary.setProcessNodesInfo("");
        
        String archiveKeyword = "";
        if(templateId != null){
            if(Strings.isNotBlank(summary.getAdvancePigeonhole())){
        		try {
        		    JSONObject jo = new JSONObject(summary.getAdvancePigeonhole());
        			archiveKeyword = jo.optString("archiveKeyword", "");
					String advancePigeonhole = jo.toString();
	                summary.setAdvancePigeonhole(advancePigeonhole);
        		} catch (Exception e) {
        			LOG.error("",e);
        		}
            }
        }
        //保存上传的附件
        boolean attaFlag = false;
        if(!ColConstant.SendType.auto.equals(sendtype) && !ColConstant.SendType.child.equals(sendtype)){
            try {
            	String attaFlagStr = "";
            	if(sendtype == ColConstant.SendType.forward){
            		HttpServletRequest request=(HttpServletRequest)AppContext.getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
            		if (request != null) {
            			request.removeAttribute("HASSAVEDATTACHMENT");//保存附件之前清空这个标记
            			attaFlagStr = this.attachmentManager.create(ApplicationCategoryEnum.collaboration, summary.getId(), summary.getId());
            		} else {
            			List groups1 = ParamUtil.getJsonDomainGroup("attachmentInputs");
                        int lsize = groups1.size();
                        Map dMap = ParamUtil.getJsonDomain("attachmentInputs");
                        
                        if (lsize == 0 && dMap.size() > 0) {
                            groups1.add(dMap);
                        }
                        List<Attachment> result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.collaboration, 
                        		summary.getId(), summary.getId(), groups1);
                        if (result.size() > 0) {
                        	attaFlagStr = attachmentManager.create(result);
                        }
            		}
            	}else if(isFromNewCollPage(sendtype)){
            		attaFlagStr = saveAttachmentFromDomain(ApplicationCategoryEnum.collaboration,summary.getId());
            	}
                attaFlag = Constants.isUploadLocaleFile(attaFlagStr);
                if(attaFlag){
                    ColUtil.setHasAttachments(summary, attaFlag);
                }
            }
            catch (Exception e) {
               LOG.info("保存上传的附件报错!",e);
            }
        }
        
        //生产最终标题
        String subject = "";
        if(sendtype == ColConstant.SendType.auto 
            ||sendtype == ColConstant.SendType.child 
            || Integer.valueOf(ColConstant.SendType.auto.ordinal()).equals(summary.getNewflowType())
            || Integer.valueOf(ColConstant.SendType.child.ordinal()).equals(summary.getNewflowType())){
                subject = ColUtil.makeSubject4NewWF(template, summary, user);
                summary.setAutoRun(true);
        }else {
            subject = ColUtil.makeSubject(template, summary, user);
        }
        
        if(Strings.isBlank(subject)){
            subject = "{"+ResourceUtil.getString("collaboration.subject.default")+"}";
        }
       
        if(!subject.equals(summary.getSubject()) || info.isM3Flag()){
        	summary.setSubject(subject);
        	if(info.getCtpContentAll()!=null){
        		contentManager.updateContentTitle(info.getCtpContentAll().getId(),summary.getSubject());
        	}else{
        		List<CtpContentAll> cl = contentManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
            	if(Strings.isNotEmpty(cl)){
            		contentManager.updateContentTitle(cl.get(0).getId(),summary.getSubject());
            	}
        	}
        }
        
        //保存已发个人事务表
        boolean isSpecialBackReMe = false;
        boolean isspecialbackrerun = false;
        if(affair == null && info.getCurrentAffairId() != null){
            affair = affairManager.get(info.getCurrentAffairId());
        }
        if(affair != null){
        	if(Integer.valueOf( SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState())){
        		isSpecialBackReMe = true;
        	}
        	if(Integer.valueOf( SubStateEnum.col_pending_specialBackToSenderCancel.getKey()).equals(affair.getSubState())){
        		isspecialbackrerun = true;
        	}
        }
        if(affair == null){
            affair = new CtpAffair();
        }
        if(isSpecialBackReMe || isspecialbackrerun)  {
        	summary.setCreateDate(affair.getCreateDate());
            summary.setStartDate(affair.getCreateDate());
        }else{
        	affair.setCreateDate(now);
        }
        affair.setIdIfNew();
        affair.setApp(ApplicationCategoryEnum.collaboration.key());
        affair.setSubApp(summary.getTempleteId() == null ? ApplicationSubCategoryEnum.collaboration_self.key() : ApplicationSubCategoryEnum.collaboration_tempate.key());
        affair.setSubject(subject);
        affair.setReceiveTime(now);
        affair.setUpdateDate(now);
        affair.setMemberId(senderId);
        affair.setObjectId(summary.getId());
        affair.setSubObjectId(null);
        affair.setSenderId(senderId);
        affair.setState(StateEnum.col_sent.key()); 
        affair.setSubState(SubStateEnum.col_normal.key());
        affair.setTempleteId(summary.getTempleteId());
        affair.setBodyType(summary.getBodyType());
        affair.setImportantLevel(summary.getImportantLevel());
        affair.setResentTime(summary.getResentTime());
        affair.setForwardMember(summary.getForwardMember());
        affair.setNodePolicy("newCol");//协同发起人节点权限默认为协同
        affair.setTrack(info.getTrackType());
        affair.setDelete(false);
        affair.setFinish(false);
        affair.setCoverTime(false);
        affair.setDueRemind(false);
        affair.setArchiveId(null);
        affair.setRelationDataId(Strings.isBlank(info.getDR()) ? null : Long.valueOf(info.getDR()));
        affair.setOrgAccountId(summary.getOrgAccountId());
        if(null != summary.getAutoRun() && summary.getAutoRun()){
        	affair.setAutoRun(summary.getAutoRun());
        }
        
        AffairUtil.setHasAttachments(affair, ColUtil.isHasAttachments(summary));//设置已发事项 附件
        if(summary.getDeadlineDatetime()!= null){
            AffairUtil.addExtProperty(affair, AffairExtPropEnums.processPeriod,summary.getDeadlineDatetime());
        }
        if(isForm){
            affair.setFormAppId(summary.getFormAppid());
            affair.setFormRecordid(summary.getFormRecordid());
            affair.setMultiViewStr(info.getFormViewOperation());
        }
        	
        
        //发起流程
        Map<String,Object> wfRetMap =  new HashMap<String,Object>();
        Map<String,Object> wfAppGlobal =  new HashMap<String,Object>();
        try {
            
            Map<String,Object> wfParams = new HashMap<String,Object>();
            wfParams.put("wfSubPRCSkipFSender", info.getWfSubPRCSkipFSender());
            wfParams.put(WorkFlowEventListener.WF_APP_GLOBAL, wfAppGlobal);
        	
            if(sendtype == ColConstant.SendType.immediate){
        		String processId = summary.getProcessId();
        		//优先取模板的流程
        		if(template!=null && template.getWorkflowId()!=null && template.getWorkflowId().longValue()!=0L
        				&& template.getWorkflowId().longValue()!=-1L && !isSpecialBackReMe){
        			processId = null;
        		}
        		wfRetMap = this.runcase(processId, null, info.getWorkflowNewflowInput(),
        				info.getWorkflowNodePeoplesInput(), info.getWorkflowNodeConditionInput(), user,
        				summary, sendtype, template, 
        				affair,info.getReMeoToReGo(),"",wfParams);
        	}else{
        		wfRetMap = this.runcase(sendtype, user, summary,template, affair,wfParams);
        	}
        }
        catch (Throwable e1) {
            LOG.error("发送流程报错",e1);
            throw new BusinessException(e1);
        }

        boolean isRego = "true".equals(wfRetMap.get("isRego"));
        String caseId = (String) wfRetMap.get("caseId");
        String prcocessId = (String) wfRetMap.get("prcocessId");
        String isTriggerNewFlow = (String) wfRetMap.get("isTriggerNewFlow");
        String RelationDataId = (String) wfRetMap.get("RelationDataId");
        String nextMembers = (String) wfRetMap.get("nextMembers");
        Map<String,Object> businessData = (Map<String, Object>) wfRetMap.get(WorkFlowEventListener.BUSINESS_DATA);
        
        boolean isResend = false;
		if(null != summary.getResentTime()  && summary.getResentTime() > 0){
			isResend = true;
			summary.setCurrentNodesInfo("");
			summary.setReplyCounts(0);
		}
		
        if (isRego || sendtype == ColConstant.SendType.immediate) { // 指定回退导致流程重走，情况极少，场景复杂，用数据库查询的方式来更新
        	summary.setCurrentNodesInfo("");
        	ColUtil.updateCurrentNodesInfo(summary);
		}
        else{
            List<CtpAffair> assigedAffairs = (List<CtpAffair>) businessData.get(WorkFlowEventListener.ASSIGNED_AFFAIRS);
        	
            Long removeSenderId = null;
            if(isSpecialBackReMe){
                removeSenderId = summary.getStartMemberId();
            }
            
            ColUtil.setCurrentNodesInfoFromCache(summary, removeSenderId, assigedAffairs);
        }
        
        summary.setCaseId(Long.valueOf(caseId));
        summary.setProcessId(prcocessId);
        affair.setProcessId(summary.getProcessId());
        affair.setCaseId(summary.getCaseId());
       
        if(Strings.isNotBlank(RelationDataId)){
        	affair.setRelationDataId(Long.valueOf(RelationDataId));
        }
        
        //保存协同信息
        if(null == summary.getDeadlineDatetime() && null == summary.getDeadline()){
          summary.setAdvanceRemind(null);
        }
        
        //保存正文,新建页面再外边保存，后续考虑给他一起合并进来
        if(!isFromNewCollPage(sendtype)){
	        CtpContentAll newContentAll = info.getContent();
	        if(newContentAll != null){
	            newContentAll.setModuleId(summary.getId());
	            newContentAll.setCreateId(senderId);
	            newContentAll.setContentTemplateId(summary.getFormAppid());
	            newContentAll.setModuleTemplateId(templateId);
	            newContentAll.setTitle(summary.getSubject());
	            contentManager.saveOrUpdateContentAll(newContentAll);
	        }
        }
        
        //发起者附言
        Comment comment = info.getComment();
        if(comment != null){
            comment.setModuleId(summary.getId());
            comment.setCreateId(senderId);
            comment.setCreateDate(now);
            this.commentManager.insertComment(comment);
        }

        //加入跟踪信息 当跟踪类型为2的时候(指定人),像跟踪表插入数据
        
        //1.保存跟踪,流程不是结束状态就保存跟踪信息,流程已经结束了就不保存跟踪信息
        //2.当前节点就是最后一个节点/流程已经结束后面的只会节点：设置为结束状态。
        boolean isFinished = Integer.valueOf(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState())
        					 	||Integer.valueOf(CollaborationEnum.flowState.terminate.ordinal()).equals(summary.getState());
        if(isFinished) {
        	affair.setFinish(true);
        } else if (info.getTrackType() == 2 && Strings.isNotBlank(info.getTrackMemberId())) {
            //跟踪的逻辑
            String trackMemberId = info.getTrackMemberId();
            String[] str = trackMemberId.split(",");
            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>(str.length);
        
	        trackManager.deleteTrackMembers(summary.getId());
            /**
             * OA-68807协同跟踪指定人后，回退、撤销给发起人，重新从待发列表编辑发送后，跟踪指定人显示重复
             */
            for (int count = 0; count < str.length; count++) {
                CtpTrackMember member = new CtpTrackMember();
                member.setIdIfNew();
                member.setAffairId(affair.getId());
                member.setObjectId(summary.getId());
                member.setMemberId(senderId);
                member.setTrackMemberId(Long.parseLong(str[count]));
                
                list.add(member);
            }
            
            this.trackManager.save(list);
        }
        
        //如果是项目协同,存入该项目下当前阶段
        if(summary.getProjectId() != null && summary.getProjectId() != 0 && summary.getProjectId() != -1){ 
            ProjectBO projectSummary = projectApi.getProject(summary.getProjectId());
            if(projectSummary != null){
                if(projectSummary.getPhaseId() != 1){
                    ProjectPhaseEvent ppe = new ProjectPhaseEvent(ApplicationCategoryEnum.collaboration.key(),summary.getId(),projectSummary.getPhaseId());
                    projectPhaseEventManager.save(ppe);
                }
            }
        }
        
        
        
        //归档
        int app = ApplicationCategoryEnum.collaboration.key();
        if(isForm){
        	app = ApplicationCategoryEnum.form.key();
        }

        //更新冗余状态
        affair.setSummaryState(summary.getState());
        
        try {
			if (info.getSenderAffair() == null) {
				this.colManager.saveColSummary(summary);
				this.affairManager.save(affair);
			}
			else{
			    this.colManager.updateColSummary(summary);
	            this.affairManager.updateAffair(affair);
			}
			
        } catch (Exception e) {
            LOG.error("保存发起人事项报错！",e);
        }
        
        /**************必须在保存affair数据后才能保存督办信息，督办这边保存的时候要用到affair**************/
        boolean isSendM3Template = info.isM3Flag() && template != null;
        if(isSendM3Template){
        	//M3页面保存督办,没有传递督办信息，需要拷贝模板的督办 
        	//TODO 性能优化，直接把是否新建的参数传递过来，不要再这查询督办信息，影响性能
    		CtpSuperviseDetail supervise = superviseManager.getSupervise(summary.getId());
    		if(supervise != null){
    			superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervising,
    					summary.getId(),SuperviseEnum.EntityType.summary);
    		}else{
    			this.superviseManager.saveSuperviseByCopyTemplete(sender.getId(), summary, template.getId());
    		}
        } else if(sendtype == ColConstant.SendType.auto || sendtype == ColConstant.SendType.child){
        	
        	this.superviseManager.saveSuperviseByCopyTemplete(sender.getId(), summary, template.getId());
        	
        	
        } else if(isFromNewCollPage(sendtype)){
            this.saveColSupervise4NewColl(summary, affair,true);
        } else if(sendtype == ColConstant.SendType.forward){
            //转发不保存督办，先把这个分支留着吧。
        } else if(sendtype ==  ColConstant.SendType.immediate){
            boolean isSendMessage = true;
            if (isSpecialBackReMe || isspecialbackrerun) {
                isSendMessage = false;
            }
            SuperviseMessageParam param = new  SuperviseMessageParam();
            param.setForwardMember(summary.getForwardMember());
            param.setImportantLevel(summary.getImportantLevel());
            param.setMemberId(user.getId());
            param.setSendMessage(isSendMessage);
            param.setSubject(summary.getSubject());
            superviseManager.updateStatus(summary.getId(), 
            		SuperviseEnum.EntityType.summary,  
            		SuperviseEnum.superviseState.supervising, param); 
        }
        
        
        //没有文档中心插件不归档
        if(AppContext.hasPlugin("doc")){
        	if(Strings.isNotBlank(summary.getAdvancePigeonhole())){//高级归档
            	JSONObject jo;
    			try {
    				jo = new JSONObject(summary.getAdvancePigeonhole());
    				
    				Long archiveRealId = null;
    				Long destFolderId = summary.getArchiveId();
    				String isCereateNew = jo.optString(ColConstant.COL_ISCEREATENEW, "");
    				String archiveField = jo.optString(ColConstant.COL_ARCHIVEFIELDID, "");
    				String StrArchiveFolder = jo.optString(ColConstant.COL_ARCHIVEFIELDVALUE, "");
    				boolean isCreateFloder = "true".equals(isCereateNew);
    				String archiveFieldValue = "";
    				boolean updateJson = true;
                    if(Strings.isNotBlank(archiveField)){
                    	archiveFieldValue = capFormManager.getMasterFieldValue(summary.getFormAppid(),summary.getFormRecordid(),archiveField,true).toString();
                        if(Strings.isNotBlank(archiveFieldValue)){
                            archiveRealId = docApi.getPigeonholeFolder(destFolderId, archiveFieldValue, isCreateFloder);//真实归档的路径
                        }
                        if(archiveRealId == null){
                            archiveFieldValue = "Temp";
                            //归档到Temp下面
                            archiveRealId = docApi.getPigeonholeFolder(destFolderId, archiveFieldValue, true);//真实归档的路径
                        }
                    }else {
                        //普通归档
                        archiveRealId = destFolderId;
                    }
    				
    				String keyword = null;
    				if (Strings.isNotBlank(archiveKeyword)) {
    					keyword = capFormManager.getCollSubjuet(summary.getFormAppid(), archiveKeyword, summary.getFormRecordid(), false);
    				}
                    if (keyword != null && keyword.length() > 85) {//关键字截取
                        keyword=keyword.substring(0, 85);
                        LOG.info("预先归档："+summary.getId()+"|keyword:"+keyword);
                    }
    				String _archiveText = jo.optString("archiveText", "");
    				boolean hasAttachments = ColUtil.isHasAttachments(summary);
    				if(null != destFolderId && !"true".equals(_archiveText)){
    					if(Strings.isBlank(StrArchiveFolder) 
    					        || Integer.valueOf(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState())){//OA-122843
    					    
    						docApi.pigeonholeWithoutAcl(sender.getId(), app, affair.getId(), hasAttachments, archiveRealId, PigeonholeType.edoc_account.ordinal(), keyword);
    						jo.put(ColConstant.COL_ARCHIVEFIELDVALUE, archiveFieldValue);
         	                summary.setAdvancePigeonhole(jo.toString());
         	                updateJson = false;
    					}else if(!archiveFieldValue.equals(StrArchiveFolder)){//OA-122843同一个单子，指定回退后再次发起
    						docApi.updatePigehole(AppContext.getCurrentUser().getId(), affair.getId(), ApplicationCategoryEnum.form.key());
    						docApi.moveWithoutAcl(AppContext.getCurrentUser().getId(), affair.getId(), archiveRealId);
    					}
    				}
    				
    				if(updateJson){
      				  //归档路径如果没有创建目录， 显示值和实际归档路径不一致
      	                 jo.put(ColConstant.COL_ARCHIVEFIELDVALUE, archiveFieldValue);
      	                 summary.setAdvancePigeonhole(jo.toString());
      	                 
      	                 //再次跟新数据库
      	                 //this.colManager.updateColSummary(summary);
      				}
    			} catch (Exception e) {
    				LOG.error("summary.getAdvancePigeonhole():"+summary.getAdvancePigeonhole(),e);
    			}
            }else if(null != summary.getArchiveId()){
            	if(Strings.isNotBlank(summary.getSubject())){
            	    DocResourceBO _exist = docApi.getDocResource(summary.getArchiveId());
            	  if(null != _exist){
            	    
            	    if(null != template && sendtype == ColConstant.SendType.auto  || sendtype == ColConstant.SendType.child){
            	        String summaryTem = template.getSummary();
            	        ColSummary summaryTemBean = (ColSummary) XMLCoder.decoder(summaryTem);
            	        boolean hasPrePath = null != summaryTemBean.getArchiveId() || Strings.isNotBlank(summaryTemBean.getAdvancePigeonhole());
            	        info.setTemplateHasPigeonholePath(hasPrePath);
            	    }
            	    
            	    Integer pigholeType = info.isTemplateHasPigeonholePath() ? PigeonholeType.edoc_account.ordinal() : PigeonholeType.edoc_dept.ordinal();
            	    
            	    docApi.pigeonholeWithoutAcl(sender.getId(), app, affair.getId(),  
            	    		ColUtil.isHasAttachments(summary), summary.getArchiveId(), pigholeType, null);
            	  }
            	}else{
            		LOG.info("Id为"+summary.getId()+"的协同，由于标题为空不允许归档！");
            	}
            }
        	
        }
        //调用表单万能方法
        if(isForm){
            try {
            	ColHandleType colHandleType = ColHandleType.send;
            	if(ColConstant.SendType.auto.equals(sendtype)) {
            		colHandleType = ColHandleType.autosend;
            	}
            	
            	//不需要每次都去查询意见，损耗性能
            	List<Comment> commentList = new ArrayList<Comment>();
                if(isSpecialBackReMe || isspecialbackrerun){
            	    commentList = commentManager.getCommentList(ModuleType.collaboration, summary.getId());
                }else{
                	commentList.add(comment);
                }
               
                capFormManager.updateDataState(summary, affair, colHandleType, commentList);
            }catch (Exception e) {
            	LOG.error("更新表单相关信息异常:summaryId:"+summary.getId()+",affairId:"+affair.getId(),e);
                throw new BusinessException("更新表单相关信息异常:summaryId:"+summary.getId()+",affairId:"+affair.getId(),e);
            }
        }
       
        //审计日志,无流程表单表单触发可能为空
        String toMmembers = nextMembers;
        if(user!=null){
        	if(sendtype == ColConstant.SendType.forward){
        		appLogManager.insertLog(user, AppLogAction.Coll_Transmit, user.getName(), summary.getSubject());  
        	}else if(!isSpecialBackReMe && !isspecialbackrerun){
        		appLogManager.insertLog(user, AppLogAction.Coll_New, user.getName(), info.getSummary().getSubject());
        	}
            
            //记录流程日志
            if(isSpecialBackReMe || isspecialbackrerun){
                processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(info.getSummary().getProcessId()),-1l, ProcessLogAction.colStepBackToResend, toMmembers);
            }else {
            	List<ProcessLogDetail> allProcessLogDetailList= null;
            	if(sendtype == ColConstant.SendType.immediate){
            		allProcessLogDetailList= wapi.getAllWorkflowMatchLogAndRemoveCache(info.getWorkflowNodeConditionInput());
            	}else{
            		allProcessLogDetailList= wapi.getAllWorkflowMatchLogAndRemoveCache();
            	}
            	if (isForm){
            		processLogManager.insertLog(user, Long.parseLong(info.getSummary().getProcessId()), -1l, ProcessLogAction.sendForm,allProcessLogDetailList, (String)toMmembers);
            	}else {
            		processLogManager.insertLog(user, Long.parseLong(info.getSummary().getProcessId()), -1l, ProcessLogAction.sendColl,allProcessLogDetailList, (String)toMmembers);
            	}
        	}
        }
       
        
		//所有的流程日志都要保留
		Long processId = info.getCurrentProcessId();
		LOG.info("协同ID="+summary.getId()+","+summary.getSubject()+",processId="+processId+",isResend="+isResend
				+",summary.getProcessId()="+summary.getProcessId());
		if (null != processId && !isResend) {
			processLogManager.updateByHQL(Long.valueOf(summary.getProcessId()), processId);
		}
        
        
        if (isSpecialBackReMe || isspecialbackrerun) {
            this.updateSpecialBackedAffair(summary);            
            if (isSpecialBackReMe) {
                colMessageManager.transColPendingSpecialBackedMsg(info.getSummary(), toMmembers);
            } else if (isspecialbackrerun){
                colMessageManager.sendSupervisor(info.getSummary(),affair);
            }
        }
        
        //全文检索 
        if (AppContext.hasPlugin("index")) {
            if (isForm) {
                this.indexManager.add(summary.getId(), ApplicationCategoryEnum.form.key());
            }
            else{
                this.indexManager.add(summary.getId(), ApplicationCategoryEnum.collaboration.key());
            }
        }
        
        //定时任务超期提醒和提前提醒
        CollaborationJob.createQuartzJobOfSummary(summary, workTimeManager);

        //协同发起事件通知
        CollaborationStartEvent event = new CollaborationStartEvent(this);
        event.setSummaryId(summary.getId());
        event.setFrom(user.isFromM1() ? "m1" : "pc"); 
        event.setAffair(affair);
        EventDispatcher.fireEvent(event);
        
        if(user.isFromM1()){
        	 LOG.info("M1 发起协同：标题："+summary.getSubject()+" ,summaryId:"+summary.getId()+",当前用户："+user.getName()+"|"+user.getId());
        }
       
       
        sendCollResult.setSentAffair(affair);
        sendCollResult.setSummary(summary);
        return sendCollResult;
    }
    /**
     * 获取前台页面的附件
     * @return
     * @throws BusinessException 
     */
    public String saveAttachmentFromDomain(ApplicationCategoryEnum type,Long module_id) throws BusinessException{
        
        List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
        int assDocSize = assDocGroup.size();
        Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
        if (assDocSize == 0 && assDocMap.size() > 0) {
        	assDocGroup.add(assDocMap);
        }
        
        List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
        int attFileSize = attFileGroup.size();
        Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
        if (attFileSize == 0 && attFileMap.size() > 0) {
            attFileGroup.add(attFileMap);
        }
        
        assDocGroup.addAll(attFileGroup);
        
        List result;
		try {
			result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.collaboration, module_id,module_id, assDocGroup);
		} catch (Exception e) {
			throw new BusinessException("创建附件出错");
		}
    	
    	return attachmentManager.create(result);
    }
    private boolean isFromNewCollPage(ColConstant.SendType sendType){
    	if(sendType == ColConstant.SendType.resend
    			||sendType == ColConstant.SendType.normal
    			){
    		return true;
    	}else{
    		return false;
    	}
    }
    /**
    * 保存督办
    */
    public void saveColSupervise4NewColl(ColSummary colSummary,CtpAffair affair,boolean sendMessage)throws BusinessException {
        try {
            Map superviseMap = ParamUtil.getJsonDomain("colMainData");
            SuperviseSetVO ssvo = (SuperviseSetVO)ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
        
            SuperviseMessageParam smp = new SuperviseMessageParam();
        	smp.setSendMessage(true);
        	smp.setMemberId(colSummary.getStartMemberId());
        	smp.setImportantLevel(colSummary.getImportantLevel());
        	smp.setSubject(colSummary.getSubject());
        	smp.setForwardMember(colSummary.getForwardMember());
        	smp.setAffairId(affair.getId());
            
            superviseManager.saveOrUpdateSupervise4Process(ssvo, smp, colSummary.getId(), SuperviseEnum.EntityType.summary);
        }
        catch (Exception e) {
            LOG.error("",e);
        }
    }
    
    private  Map<String,Object> runcase(String process_id,String process_xml,String popNodeSubProcessJson, 
            String selectedPeoplesOfNodes,String conditionsOfNodes, User user, 
            ColSummary summary,ColConstant.SendType sendtype , CtpTemplate template, 
            CtpAffair senderAffair,String reMeToReGo,String dynamicFormMasterIds,
            Map<String,Object> wfParams) throws BusinessException{
    	
         boolean wfSubPRCSkipFSender  = (Boolean) wfParams.get("wfSubPRCSkipFSender");
         Map<String,Object> wfAppGlobal = (Map<String, Object>) wfParams.get(WorkFlowEventListener.WF_APP_GLOBAL);
        
    	 AffairData affairData = ColUtil.getAffairData(summary);
    	 Long flowPermAccountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
         affairData.addBusinessData(ColConstant.FlowPermAccountId, flowPermAccountId);
          
    	 WorkflowBpmContext context = new WorkflowBpmContext();
    	 context.setBusinessData(WorkFlowEventListener.WF_APP_GLOBAL,wfAppGlobal);
         context.setAppName(ModuleType.collaboration.name());
         context.setDebugMode(false);
         if (Strings.isNotBlank(process_id) &&  !"-1".equals(process_id)) {
             context.setProcessId(process_id);
         }
         //设置正文内容，用来发送邮件的时候显示正文内容
         if(String.valueOf(MainbodyType.HTML.getKey()).equals(summary.getBodyType())){
        	 List<CtpContentAll> contentList=contentManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
             if(contentList.size()>0){
            	 CtpContentAll content=contentList.get(0);
            	 affairData.setBodyContent(content.getContent());
             }
         }
         
         context.setProcessXml(process_xml);
         context.setStartUserId(String.valueOf(user.getId()));
         context.setCurrentUserId(String.valueOf(user.getId())); 
         context.setStartUserName(user.getName());
         context.setStartAccountId(String.valueOf(user.getLoginAccount()));
         context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
         context.setStartAccountName("seeyon");
         context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, summary.getStartMemberId());
         context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
         context.setBusinessData("bizObject", summary);
         context.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT,summary);
         context.setBussinessId(String.valueOf(summary.getId()));
         context.setBusinessData(WorkFlowEventListener.CTPAFFAIR_CONSTANT, senderAffair);
         context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID,senderAffair.getId());
         context.setCanSubProcessSkipFSender(wfSubPRCSkipFSender);
         context.setMastrid(""+affairData.getFormRecordId());
         context.setFormData(""+affairData.getFormAppId());
         context.setFormAppId(""+affairData.getFormAppId());
         context.setAppObject(summary);
         boolean toReGo = "true".equals(reMeToReGo) ;
         context.setToReGo(toReGo);;
         context.setDynamicFormMasterIds(dynamicFormMasterIds);
		if (toReGo) {
			CtpAffair stepBackAffair = null;
			List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), StateEnum.col_pending,
					SubStateEnum.col_pending_specialBack);
			if (Strings.isNotEmpty(affairs)) {
				for (CtpAffair _affair : affairs) {
					stepBackAffair = _affair;
					break;
				}
			}
			context.setBusinessData("_ReMeToReGo_operationType", WorkFlowEventListener.SPECIAL_BACK_RERUN);
			context.setBusinessData("_ReMeToReGo_stepBackAffair", stepBackAffair);
			context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW,"1");
		}
         else{
        	 context.setBusinessData("operationType", affairData.getBusinessData().get("operationType"));
         }
         context.setVersion("2.0");
         //触发子流程和新流程, 要让工作流增加一个发起者节点
         if(sendtype == ColConstant.SendType.auto || sendtype == ColConstant.SendType.child){
             //子流程不设置手动选择人员信息
        	 context.setAddFirstNode(true);
         }else{
             context.setSelectedPeoplesOfNodes(selectedPeoplesOfNodes);
         }
         
         context.setPopNodeSubProcessJson(popNodeSubProcessJson);
         context.setConditionsOfNodes(conditionsOfNodes);
         
         
         //指定回退到发起者后发起者再发送
         if (affairData.getCaseId() != null) {
             context.setCurrentActivityId("start");
             context.setCaseId(affairData.getCaseId());
         }
         String[] result;
         if (null != template &&  null != template.getWorkflowId()) {
             
          // 可能异常情况，调用模板流程设置了 process_xml， 导致流程没敢找模板流转， 只做协同兼容
             context.setProcessXml("");
             
             context.setProcessTemplateId(String.valueOf(template.getWorkflowId()));
             result = wapi.transRunCaseFromTemplate(context);
             String[] policyIds = null;
             if(result!=null && result.length == 7){
            	 policyIds = result[6].split(",");
             }
             ContentUtil.updatePermissinRef(ModuleType.collaboration.getKey(), policyIds,template.getOrgAccountId());
         } else {
             //缓存caseId
             result = wapi.transRunCase(context);
             String[] policyIds = null;
             if(result!=null && result.length == 8){
            	 policyIds = result[6].split(",");
             }
             ContentUtil.updatePermissinRef(ModuleType.collaboration.getKey(),policyIds,summary.getOrgAccountId());
         }

         Map<String,Object> wfRetMap =  new HashMap<String,Object>();
         wfRetMap.put("isRego", toReGo ? "true" : "false");
         wfRetMap.put("caseId", result !=null && result.length > 0?result[0]:"");
         wfRetMap.put("prcocessId", result !=null && result.length > 1?result[1]:"");
         wfRetMap.put("nextMembers", result !=null && result.length > 2?result[2]:"");
         wfRetMap.put("isTriggerNewFlow", result !=null && result.length >3?result[3]:""); //是否触发了子流程。
         wfRetMap.put("RelationDataId", result !=null && result.length == 8 && Strings.isDigits(result[7]) ? result[7] : "");
         wfRetMap.put(WorkFlowEventListener.BUSINESS_DATA, context.getBusinessData());
         return wfRetMap;
    }
    /**
     * 运行流程
     * 
     * @param sendtype
     * @param user
     * @param summary
     * @return
     * @throws Exception
     */
    private  Map<String,Object> runcase(ColConstant.SendType sendtype, User user, ColSummary summary, 
            CtpTemplate template, CtpAffair senderAffair,Map<String,Object> wfParams) throws Exception{
    
    	@SuppressWarnings("unchecked")
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String process_xml = wfdef.get("process_xml");
        String process_id = wfdef.get("processId");
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
    	process_xml = WorkflowUtil.getTempProcessXml(process_xml);
    	
       // String caseId = wfdef.get("caseId");
        String popNodeSubProcessJson = wfdef.get("workflow_newflow_input");
        String selectedPeoplesOfNodes = wfdef.get("workflow_node_peoples_input");
        String conditionsOfNodes = wfdef.get("workflow_node_condition_input");
        //Integer moduleType = ParamUtil.getInt(wfdef, "moduleType", ModuleType.collaboration.getKey());
        String reGo = String.valueOf(wfdef.get("toReGo"));
        String dynamicFormMasterIds = wfdef.get("dynamicFormMasterIds");
        return runcase( process_id, process_xml, popNodeSubProcessJson, selectedPeoplesOfNodes, conditionsOfNodes, user, summary, sendtype, template, senderAffair,reGo,dynamicFormMasterIds,wfParams);
       
    }
    public void updateSpecialBackedAffair(ColSummary summary){
    	if(summary == null) return;
    	StringBuilder hql = new StringBuilder();
		hql.append("update CtpAffair as a set a.subject =:subject ,a.importantLevel =:importantLevel,a.bodyType =:bodyType where a.objectId =:objectId");
		Map<String,Object> params=new HashMap<String, Object>();
		params.put("subject", summary.getSubject());
		params.put("importantLevel", summary.getImportantLevel());
		params.put("objectId", summary.getId());
		params.put("bodyType", summary.getBodyType());
		DBAgent.bulkUpdate(hql.toString(), params);
    }
}
