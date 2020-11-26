/**
\ * $Author$	wuwl
 * $Rev$
 * $Date::  2012-08-29               $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.template.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.ai.api.AIApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.apps.seeyonreport.api.SeeyonreportApi;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.ContentSaveOrUpdateRet;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.shareMap.V3xShareMap;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.Approve;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.enums.TemplateTypeEnums;
import com.seeyon.ctp.common.template.event.TemplateSaveEvent;
import com.seeyon.ctp.common.template.quartz.TemplateQuartz;
import com.seeyon.ctp.common.template.util.CtpTemplateUtil;
import com.seeyon.ctp.common.template.utils.TemplateContentUtil;
import com.seeyon.ctp.common.template.vo.TemplateBO;
import com.seeyon.ctp.common.template.vo.TemplateCategory;
import com.seeyon.ctp.common.template.vo.TemplateCategoryComparator;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.messageRule.bo.MessageRuleVO;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.ctp.workflow.messageRule.ruleEnum.MessageRuleEnum.MessageRuleType;
import com.seeyon.ctp.workflow.messageRule.ruleEnum.MessageRuleEnum.RemindMomentType;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
/**
 * <p>Title: Collaboration Template Interface Implements Class.</p>
 * <p>Description: Collaboration Template Modular CRUD Operation.</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 */
public class CollaborationTemplateManagerImpl implements CollaborationTemplateManager {

	private static final Log LOGGER = LogFactory.getLog(CollaborationTemplateManagerImpl.class);

    private TemplateManager     templateManager;


    private AttachmentManager   attachmentManager;

    private AppLogManager       appLogManager;

    private SuperviseManager    superviseManager;
    private TemplateCategoryManager templateCategoryManager;
    private MainbodyManager     ctpMainbodyManager;

    private OrgManager          orgManager;

    private OrgManagerDirect    orgManagerDirect;

    private PermissionManager            permissionManager;
    private RoleManager         roleManager;
    private CAPFormManager capFormManager;
    private ProjectApi   projectApi;
    private DocApi docApi;
    private EdocApi edocApi;
    private WorkflowApiManager           wapi;
    private AIApi aiApi;
    private EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
//    private SeeyonReportTemplateManager seeyonReportTemplateManager;
    private SeeyonreportApi seeyonreportApi;
    private PortalApi portalApi;
    private CustomizeManager	customizeManager;
	private EnumManager enumManagerNew;
	private TemplateApproveManager templateApproveManager;
	private AffairManager affairManager;
	private UserMessageManager userMessageManager;
	
	private MessageRuleManager messageRuleManager;


	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}

	public AffairManager getAffairManager() {
		return affairManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public TemplateApproveManager getTemplateApproveManager() {
		return templateApproveManager;
	}

	public void setTemplateApproveManager(TemplateApproveManager templateApproveManager) {
		this.templateApproveManager = templateApproveManager;
	}

	public EnumManager getEnumManagerNew() {
		return enumManagerNew;
	}

	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}

	public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

	public AIApi getAiApi() {
		return aiApi;
	}

	public void setAiApi(AIApi aiApi) {
		this.aiApi = aiApi;
	}

	public CAPFormManager getCapFormManager() {
        return capFormManager;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}
    
    public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	public TemplateCategoryManager getTemplateCategoryManager() {
        return templateCategoryManager;
    }

    public void setTemplateCategoryManager(TemplateCategoryManager templateCategoryManager) {
        this.templateCategoryManager = templateCategoryManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public EdocApi getEdocApi() {
		return edocApi;
	}

	public void setEdocApi(EdocApi edocApi) {
		this.edocApi = edocApi;
	}

	public DocApi getDocApi() {
		return docApi;
	}

	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}

	public ProjectApi getProjectApi() {
		return projectApi;
	}

	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}

	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}

	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}

	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	
//	public void setSeeyonReportTemplateManager(
//			SeeyonReportTemplateManager seeyonReportTemplateManager) {
//		this.seeyonReportTemplateManager = seeyonReportTemplateManager;
//	}
	public void setSeeyonreportApi(SeeyonreportApi seeyonreportApi) {
        this.seeyonreportApi = seeyonreportApi;
    }
	
	public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
		this.messageRuleManager = messageRuleManager;
	}

	/**
	  * 新建协同模版页面并且数据回显
	  *
	  * @param 	modelAndView	ModelAndView
	  * @param 	user 			当前用户
	  * @param 	from
	  * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
	  */
	public void newCollaborationTemplate(ModelAndView modelAndView, User user, String from) throws BusinessException {
		if (user != null) {
			ColSummary summary = new ColSummary();
			summary.setCanForward(true);
			summary.setCanArchive(true);
			summary.setCanDueReminder(true);
			summary.setCanModify(true);
			summary.setCanEditAttachment(true);
			summary.setCanTrack(true);
			summary.setCanEdit(true);
			newOrUpdateCollTemplateCommon(modelAndView, from, user.getLoginAccount(), user.getId(), summary,null);
			//加入关联项目
			List<ProjectBO> projectList = null;
			if(AppContext.hasPlugin("project")){
				if(user.isAdministrator()){
					projectList = projectApi.findProjectsByAccountId(user.getAccountId());
				}else{
					projectList = projectApi.findProjectsByMemberId(user.getId());
				}
			}
			modelAndView.addObject("relevancyProject", projectList);
		}
	}

	/**
	  * 修改协同模版时数据回显
	  *
	  * @param 	user 			当前用户
	  * @param 	modelAndView	ModelAndView
	  * @param 	from 			数与模版区别标识
	  * @param 	templeteId 		模版Id
	  * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
	  * @return Template
	  */
	public String updateCollaborationTemplate(User user, ModelAndView modelAndView, String from, String templeteId) throws BusinessException {
		ColSummary summary = new ColSummary();
		CtpTemplate template = templateManager.getCtpTemplate(Long.parseLong(templeteId));
		String[] templateAuthInfo = getTemplateAuth(template);
		if(null != templateAuthInfo[1]){
			modelAndView.addObject("templateAuthInfo",templateAuthInfo[1]);
		}
		if(template == null) {
		    return "<script>" + ResourceUtil.getString("col_template_deleted") + "</script>";
		}
		templateManager.addOrgIntoTempalte(template);
        modelAndView.addObject("template", template);
        modelAndView.addObject("categoryId",template.getCategoryId());
        // 暂时注释
        /*appLogManager.insertLog(user, AppLogAction.Coll_Template_Edit, user.getName(), template.getSubject());*/
        newOrUpdateCollTemplateCommon(modelAndView, from, user.getLoginAccount(), user.getId(), summary,template);
        //加入关联项目
        List<ProjectBO> projectList = null;
        if (AppContext.hasPlugin("project")) {
        	if(user.isAdministrator()){
				projectList = projectApi.findProjectsByAccountId(user.getAccountId());
			}else{
				projectList = projectApi.findProjectsByMemberId(user.getId());
			}
    		modelAndView.addObject("relevancyProject", projectList);
		}
        
		superviseManager.parseTemplateSupervise(template.getId());

        return "success"+ template.getType();
	}

	/**
	  * 保存协同模版
	  *
	  * @param 	user 			当前用户
	  * @param 	colBody 		模版正文
	  * @param 	colSummary 		模版属性
	  * @return Template
	 * @throws Exception
	  */
	@SuppressWarnings("unchecked")
	@CheckRoleAccess(roleTypes = { Role_NAME.TtempletManager,Role_NAME.AccountAdministrator})
	public CtpTemplate saveCollaborationTemplate() throws Exception {

		User user = AppContext.getCurrentUser();
		Map para = ParamUtil.getJsonDomain("templateMainData");
		Map summaryMap = ParamUtil.getJsonDomain("advanceHTML");
		
		if(summaryMap.get("canDeleteNode") == null) { // canDeleteNode 后面设置了默认值true,如果前端没值代表去掉了勾选，处理一下，不然总是默认值 
		    summaryMap.put("canDeleteNode", false);
		}
		
		String type = (String) para.get("type");
		CtpTemplate template = new CtpTemplate();
		template.setId(Long.valueOf((String)para.get("id")));
		template.setType(type);
		String subject = (String) para.get("subject");
		template.setSubject(subject== null ? null : subject.replaceAll(new String(new char[]{(char)160}), " "));
        
		template.setSubject((String) para.get("subject"));
		template.setCategoryId(Long.parseLong((String)para.get("categoryId")));
		template.setCreateDate(new Timestamp(System.currentTimeMillis()));
		template.setModifyDate(new Timestamp(System.currentTimeMillis()));
		template.setMemberId(user.getId());
		template.setSystem(true);
		template.setOrgAccountId(user.getLoginAccount());
		template.setStandardDuration(Long.parseLong(summaryMap.get("referenceTime").toString()));
		template.setModuleType(ModuleType.collaboration.ordinal());
		template.setState(TemplateEnum.State.normal.ordinal());
		template.setDelete(false);
		template.setCanSupervise(true);
		template.setCanTrackWorkflow(0);//自动追溯设置0
		template.setVersion(1);
		template.setSubstate(Approve.ApproveType.haveReleased.key());
		
		String templateIdStr = (String) para.get("templateId");
		//同类下同命模板覆盖操作
		if( null != para.get("changeFlag") && "changeTrue".equals(para.get("changeFlag"))){
			/**
			 * 1.根据name和分类ID查询出以前的模板
			 * 2.删除模板以及正文
			 * 3.删除可能存在的权限信息，避免产生垃圾数据
			 */
			CtpTemplate oldTem = templateManager.getTemplateByNameAndCategoryId(template.getSubject(), template.getCategoryId(), Boolean.FALSE);
			if(null != oldTem){
				templateManager.deleteCtpTemplate(oldTem.getId());
				ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.collaboration, oldTem.getId());
				templateManager.deleteAuthsByModuleId(oldTem.getId());
			}
		}
		String isNewBusiness =(String)para.get("newBusiness");
		boolean isSave = "1".equals(isNewBusiness)?true:false;
		if (Strings.isNotBlank(templateIdStr)) {
			isSave = false;
			template.setId(Long.parseLong(templateIdStr));
		}
		//正文
		ContentSaveOrUpdateRet content = null;
		//summary
		String summary = null;
		ColSummary colSummary = null;
		colSummary = (ColSummary) ParamUtil.mapToBean(summaryMap,new ColSummary(),false);
		//设置重要程度
		colSummary.setImportantLevel(Integer.parseInt((String)para.get("importantLevel")));
		if(null !=para.get("projectId") && Strings.isNotBlank((String)para.get("projectId"))){
			template.setProjectId(Long.valueOf((String)para.get("projectId")));
			colSummary.setProjectId(template.getProjectId());
		}
		if((!TemplateTypeEnums.template.name().equals(template.getType()) && !TemplateTypeEnums.templete.name().equals(template.getType())) || summaryMap.get("processTermTypeCheckBox")==null){
			colSummary.setProcessTermType(null);
		}
		if((!TemplateTypeEnums.template.name().equals(template.getType()) && !TemplateTypeEnums.templete.name().equals(template.getType())) || summaryMap.get("remindIntervalCheckBox")==null){
			colSummary.setRemindInterval(null);
		}
		//归档
		if(null != para.get("archiveId") && Strings.isNotBlank((String)para.get("archiveId"))){
			colSummary.setArchiveId(Long.valueOf((String)para.get("archiveId")));
		}
		//附件归档
		if(null != para.get("attachmentArchiveId") && Strings.isNotBlank((String)para.get("attachmentArchiveId"))){
			colSummary.setAttachmentArchiveId(Long.valueOf((String)para.get("attachmentArchiveId")));
		}
		/*if(null != template.getId()){//存在ID的话视为修改，则先去删除一次数据
			ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.collaboration,template.getId());
		}*/
		
		//保存合并处理策略
        Map<String,String> mergeDealType = new HashMap<String,String>();
        String canStartMerge= (String)summaryMap.get("canStartMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
     	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
        }
        String canPreDealMerge= (String)summaryMap.get("canPreDealMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
        }
        String canAnyDealMerge= (String)summaryMap.get("canAnyDealMerge");
        if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
        }
        colSummary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));
        
        
        //
        String messageRuleId = (String) summaryMap.get("messageRuleId");
        colSummary.setMessageRuleId(messageRuleId);
        if(Strings.isNotBlank(messageRuleId)){
        	messageRuleManager.updateMessageRuleReferce(messageRuleId, "-1");
        }
        //流程期限更新枚举使用状态
        String deadline = (String) summaryMap.get("deadline");
        if(Strings.isNotBlank(deadline)) {
            enumManagerNew.updateEnumItemRef(EnumNameEnum.collaboration_deadline.name(), deadline);
        }
		/**
		 * 协同模板：将把协同所有的信息作为模板保存，调用者不可修改流程，其他信心可以修改。
		 * 格式模板：将把协同的正文模板保存，调用者只引用正文。
		 * 流程模板：将把协同流程作为模板保存，调用者只能引用流程，不允许修改流程。
		 */
		if (TemplateEnum.Type.template.name().equals(type)) {
			summary = XMLCoder.encoder(colSummary);
			//其他模板修改存为协同模板的时候，这里需要将ID 设置成正文的moduleID。。。。
			content= TemplateContentUtil.
					contentSaveOrUpdate(TemplateContentUtil.OperationType.template, new AffairData(),template.getSubject(),false);
			template.setWorkflowId(Long.parseLong(content.getProcessId()));
			template.setBody(content.getContent().getId());
		}else if (TemplateEnum.Type.workflow.name().equals(type)) {// 存流程，流程模板不保存正文
			Long processId = TemplateContentUtil.workflowTemplate(template.getSubject());
			template.setWorkflowId(processId);
			//流程模版出了流程其他均为不能修改不勾选的
			colSummary.setCanArchive(false);
			colSummary.setCanAutostopflow(false);
			colSummary.setCanForward(false);
			colSummary.setCanEdit(false);
			colSummary.setCanEditAttachment(false);
			colSummary.setProcessId(null);
			colSummary.setDeadline(null);
			colSummary.setAdvanceRemind(null);
			summary = XMLCoder.encoder(colSummary);
			//如果是从协同模板修改保存为流程模板的，为了出现垃圾数据这里应该去删除一次正文的信息。
			ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.collaboration,template.getId());
		}else if(TemplateEnum.Type.text.name().equals(type)){// 格式（正文）协同模板，存正文，不存流程
			content= TemplateContentUtil.
					contentSaveOrUpdate(TemplateContentUtil.OperationType.noworkflow,new AffairData(),template.getSubject(),false);
			template.setWorkflowId(null);
			colSummary.setCanModify(false);
			colSummary.setDeadline(null);//bug 9885 预期格式模板不保存关联项目，流程期限，提醒，督办设置信息
			colSummary.setAdvanceRemind(null);
			colSummary.setSuperviseTitle(null);
			colSummary.setSupervisors(null);
			colSummary.setSupervisorsId(null);
			summary = XMLCoder.encoder(colSummary);
			template.setBody(content.getContent().getId());
			superviseManager.deleteAllInfoByTemplateId(template.getId());
		}

		template.setSummary(summary);
		if(null != content){
			template.setBodyType(String.valueOf(content.getContent().getContentType()));
		}else{
			template.setBodyType(String.valueOf(MainbodyType.HTML.getKey()));
		}

		//外部系统调用流程发起扩展属性(模板编号)
        String templateNumber = (String)summaryMap.get("templeteNumber");
        if(Strings.isNotBlank(templateNumber)){
            template.setTempleteNumber(templateNumber);
        }

        Long templateId = template.getId();
		//将当前模板推送到首页-我的模板
        List<Long> authMemberIdsList = new ArrayList<Long>();

        if(!isSave){
			// 删除原有附件
			this.attachmentManager.deleteByReference(templateId);
			//删除原有权限信心
            templateManager.deleteCtpTemplateAuths(templateId,ModuleType.collaboration.getKey());
		}
		// 保存附件
        if(TemplateEnum.Type.template.name().equals(type)){
        	String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.collaboration,templateId,templateId);
        	if(com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)){
        		CtpTemplateUtil.setHasAttachments(template, true);
        	}
        }

		//授权信息
		String authInfo = (String)para.get("authInfo");
		String[][] authInfos = Strings.getSelectPeopleElements(authInfo);
		List<CtpTemplateAuth> auths = new ArrayList<CtpTemplateAuth>();
		if(authInfos != null){
			int i = 0;

			for (String[] strings : authInfos) {
				CtpTemplateAuth auth = new CtpTemplateAuth();

				auth.setIdIfNew();
				auth.setAuthType(strings[0]);
				auth.setAuthId(Long.parseLong(strings[1]));
				auth.setSort(i++);
				auth.setModuleId(templateId);
				auth.setModuleType(ModuleType.collaboration.ordinal());
				auths.add(auth);
				//存入权限信息
				//templateDao.saveTemplateAuth(auth);
				//需要推送模板的人员，包括兼职
				Set<Long> memberIdsSet = Functions.getAllMembersId(auth.getAuthType(), auth.getAuthId());
				if(memberIdsSet != null && !memberIdsSet.isEmpty()){
					authMemberIdsList.addAll(memberIdsSet);
				}
			}
			if(Strings.isNotEmpty(auths)){
			    templateManager.saveCtpTemplateAuths(auths);
			}
		}

		//获取督办的MAP域
		if(TemplateEnum.Type.template.name().equals(type)){//协同模板存督办信息
			superviseManager.saveOrUpdateSupervise4Template(templateId);
		} else if(TemplateEnum.Type.text.name().equals(type) || TemplateEnum.Type.workflow.name().equals(type)){ //格式模版流程模版不保存督办信息
			superviseManager.deleteAllInfoByTemplateId(templateId);
		}
		
		//从缓存取高级信息的几个属性
		Object reserved = V3xShareMap.getReserved("templateData"+AppContext.currentUserId() + templateId);
		boolean isModifyProcessDes = false;
		if(null != reserved){
			CtpTemplate sessionTemplate = (CtpTemplate)V3xShareMap.get("templateData"+AppContext.currentUserId() + templateId);
			template.setBelongOrg(sessionTemplate.getBelongOrg());
			template.setPublishTime(sessionTemplate.getPublishTime());
			template.setProcessLevel(sessionTemplate.getProcessLevel());
			template.setCoreUseOrg(sessionTemplate.getCoreUseOrg());
			template.setResponsible(sessionTemplate.getResponsible());
			template.setAuditor(sessionTemplate.getAuditor());
			template.setConsultant(sessionTemplate.getConsultant());
			template.setInform(sessionTemplate.getInform());
			Object extraAttr = sessionTemplate.getExtraAttr("updateProcessDesFlag");
            if(null != extraAttr && "1".equals(extraAttr)){
            	isModifyProcessDes = true;
            }
		}else{//没有点击流程编辑页面的时候
			template.setCoreUseOrg((String)para.get("coreUseOrg"));
			template.setResponsible((String)para.get("responsible"));
			template.setAuditor((String)para.get("auditor"));
			template.setConsultant((String)para.get("consultant"));
			template.setInform((String)para.get("inform"));
			String belongOrg = (String)para.get("belongOrg");
			if(Strings.isNotBlank(belongOrg)){
				template.setBelongOrg(Long.valueOf(belongOrg));
			}
			String processLevel = (String)para.get("processLevel");
			if(Strings.isNotBlank(processLevel)){
				template.setProcessLevel(Integer.valueOf((String)processLevel));
			}
			String publishTime = (String)para.get("publishTime");
			if(Strings.isNotBlank(publishTime)){
				Date parse = Datetimes.parse(publishTime, Datetimes.datetimeStyle);
				template.setPublishTime(parse);
			}
		}
		template = TemplateQuartz.createTemplatePublishJob(template);
		if (isSave) { //新建
			templateManager.saveCtpTemplate(template);
			templateManager.saveCtpTemplateOrgs(templateManager.bulidCtpTemplateOrgList(template));
			appLogManager.insertLog(user, AppLogAction.Coll_Template_Create, user.getName(), template.getSubject());
			if(null != template.getProcessLevel()){
				em.updateEnumItemRef("cap_process_leavel", template.getProcessLevel().toString());
			}
		} else { // 修改
			CtpTemplate ttold = templateManager.getCtpTemplate(template.getId());
			if(null != ttold){
				template.setMemberId(ttold.getMemberId());
			}
			templateManager.updateCtpTemplate(template);
			templateManager.deleteCtpTemplateOrgByTemplateId(template.getId());
			templateManager.saveCtpTemplateOrgs(templateManager.bulidCtpTemplateOrgList(template));
			appLogManager.insertLog(user, AppLogAction.Coll_Template_Edit, user.getName(), template.getSubject());
			//修改高级信息记录日志
            if(isModifyProcessDes || templateManager.needRecordAppLog(template, ttold) ){
            	appLogManager.insertLog(AppContext.getCurrentUser(), 108, AppContext.currentUserName(),template.getSubject());
            }
		}
//		if(auths == null || auths.size() == 0){
//			ArrayList<Long> list = new ArrayList<Long>();
//			list.add(template.getId());
//			templateCacheManager.deleteCacheTemplateAuthByTemplateIds(list);//这样是删除
//		}else{
//			templateCacheManager.synchronizeTemplateAuthCache(template,auths);//新建和更新修改缓存
//		}
		templateManager.synchronizeTemplateAuthCache(template,auths);//新建和更新修改缓存
		TemplateSaveEvent templateSaveEvent = new TemplateSaveEvent(template);
        EventDispatcher.fireEventAfterCommit(templateSaveEvent);
		// 将当前模板推送到首页-我的模板
		return template;
	}




	/**
	  * 新建、修改协同模版时跳转页面公共代码部分抽取
	  *
	  * @param 	modelAndView 	modelAndView
	  * @param 	from
	  * @param 	orgAccountId 	单位Id
	  * @param 	memberId 		用户Id
	  * @param 	summary 		模版属性实体类
	  * @param 	body 			模版正文实体类
	  * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
	  */
	private void newOrUpdateCollTemplateCommon(ModelAndView modelAndView, String from, Long orgAccountId, long memberId, ColSummary summary,CtpTemplate template) throws BusinessException {
		String categoryHTML = "";
		List<Long> categoryTypes = new ArrayList<Long>();
		List<ModuleType> intList = new ArrayList<ModuleType>();
		intList.add(ModuleType.collaboration);
		intList.add(ModuleType.form);
		List<CtpTemplateCategory> templeteCategories = this.templateManager.getCategorys(orgAccountId,intList);
		Collections.sort(templeteCategories, new TemplateCategoryComparator());
		categoryTypes.add(Long.valueOf(ModuleType.collaboration.ordinal()));
		categoryTypes.add(Long.valueOf(ModuleType.form.ordinal()));

		categoryHTML  = category2HTML(templeteCategories, "", categoryTypes, 1);

		modelAndView.addObject("templateCategories", templeteCategories);
		modelAndView.addObject("categoryHTML", categoryHTML);
		boolean canDeleteNode = true;
		if(null != template){
			//summary 信息
			String sumXml = template.getSummary();
			summary = XMLCoder.decoder(sumXml,ColSummary.class);
			//权限信息
			List<CtpTemplateAuth> list = templateManager.getCtpTemplateAuths(template.getId(),ModuleType.collaboration.ordinal());
			String authInfo ="";
			for(CtpTemplateAuth auth:list){
				authInfo += auth.getAuthType() +"|"+ auth.getAuthId()+",";
			}
			if(Strings.isNotBlank(authInfo)){
				authInfo =  authInfo.substring(0, authInfo.length() - 1);
			}
			modelAndView.addObject("authInfo",authInfo);
			if(Strings.isNotBlank(authInfo)){
				PageContext p = null;
				modelAndView.addObject("authInfoShowStr",Functions.showOrgEntities(authInfo.toString(), p));
			}
			//取附件信息
	        String attListJSON = attachmentManager.getAttListJSON(template.getId());
	        modelAndView.addObject("attListJSON",attListJSON);
	        modelAndView.addObject("summary",summary);
	        //预归档
			Long archiveId = null;
	        String archiveName = "";
	        if(AppContext.hasPlugin("doc")){
	        	if(summary.getArchiveId() != null){
	        		archiveId = summary.getArchiveId();
	        		archiveName = docApi.getDocResourceName(archiveId);
	        	}
	        	String attachmentArchiveName = "";
	        	if(summary.getAttachmentArchiveId() != null){//附件归档的完整目录
	        		DocResourceBO docResourceBO = docApi.getDocResource(summary.getAttachmentArchiveId());
	        		if(docResourceBO != null){
	        			attachmentArchiveName = docApi.getPhysicalPath(docResourceBO.getLogicalPath(), "\\", false, 0);
	        		}
	        	}
	        	modelAndView.addObject("attachmentArchiveName", attachmentArchiveName);
	        }
	        modelAndView.addObject("archiveName", archiveName);
	        canDeleteNode = summary.getCanDeleteNode();
		}
		modelAndView.addObject("canDeleteNode", canDeleteNode);
        modelAndView.addObject("summary", summary);
        
        //合并处理设置
        boolean canAnyDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE,summary);
        boolean canPreDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE,summary);
        boolean canStartMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE,summary);
        modelAndView.addObject("canAnyDealMerge", canAnyDealMerge);
        modelAndView.addObject("canPreDealMerge", canPreDealMerge);
        modelAndView.addObject("canStartMerge", canStartMerge);
        
        String messageRuleId = summary.getMessageRuleId();
	     if(Strings.isBlank(messageRuleId)){
	    	messageRuleId = getMessageRuleIds(summary);
	         
	     }
	     
	     modelAndView.addObject("messageRuleId", messageRuleId);

	}
	
	private String getMessageRuleIds(ColSummary summary){
    	User user = AppContext.getCurrentUser();
    	List<MessageRuleVO> result = new ArrayList<MessageRuleVO>();
    	Long dealLine = summary.getDeadline();
    	if(dealLine==null || dealLine<=0){
    		return "";
    	}
    	String remind = String.valueOf(null != summary.getAdvanceRemind() ? summary.getAdvanceRemind().intValue() : 0);
        String remindInterval = String.valueOf(null != summary.getRemindInterval() ? summary.getRemindInterval().intValue() : 0);
        if(Strings.isNotBlank(remind)&&(!"-1".equals(remind) && !"0".equals(remind))){
			List<MessageRuleVO> remindMessageRule = messageRuleManager.getMessageRuleByRemindTime(user.getAccountId(), true, Integer.valueOf(remind), MessageRuleType.overtimeNotice,RemindMomentType.before);
			result.addAll(remindMessageRule);
		}
		
		if(Strings.isNotBlank(remindInterval)&&(!"-1".equals(remindInterval) && !"0".equals(remindInterval))){
			List<MessageRuleVO> cycleRemindMessageRule = messageRuleManager.getMessageRuleByRemindTime(user.getAccountId(), true, Integer.valueOf(remindInterval), MessageRuleType.overtimeNotice,RemindMomentType.after);
			result.addAll(cycleRemindMessageRule);
		}else{
			List<MessageRuleVO> cycleRemindMessageRule = messageRuleManager.getMessageRuleByRemindTime(user.getAccountId(), true, 0, MessageRuleType.overtimeNotice,RemindMomentType.before);
			result.addAll(cycleRemindMessageRule);
		}
		List<Long> messageRuleIdArray = new ArrayList<Long>(); 
		for (MessageRuleVO rule : result) {
			messageRuleIdArray.add(rule.getId());
		}
    	return Strings.join(messageRuleIdArray, ",");
    }
	
    private String category2HTML(List<CtpTemplateCategory> categories, String categoryHTMLStr,
            List<Long> currentNode, int level) throws BusinessException {
		StringBuilder categoryHTML =new StringBuilder();
        for (CtpTemplateCategory category : categories) {
            Long parentId = category.getParentId();
            if (currentNode.contains(parentId)) {
                if (AppContext.getCurrentUser().isAdministrator()
                        || templateManager.isTemplateCategoryManager(AppContext.currentUserId(),
                        AppContext.currentAccountId(), findRootParent(category))) {
                    categoryHTML.append("<option value='" + category.getId() + "'>");
                    for (int i = 0; i < level; i++) {
                        categoryHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                    }
                    categoryHTML.append(Strings.toHTML(category.getName().trim()) + "</option>\n");
                    List<Long> categoryTypes = new ArrayList<Long>();
                    categoryTypes.add(category.getId());
					categoryHTML.append( category2HTML(categories, "", categoryTypes, level + 1));
                }
            }
        }
		categoryHTML.append(categoryHTMLStr);
        return categoryHTML.toString();
    }

    /**
      * 根据单位ID查询模版分类
      *
      * @param accountId 单位Id.
      * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
      * @return List<TemplateCategory>
      */
    public List<CtpTemplateCategory> getCategory(Long accountId) throws BusinessException {
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        List<CtpTemplateCategory> templateCategorys = templateManager.getCategorys(accountId,ModuleType.collaboration);
        if (templateCategorys != null) {
            for (CtpTemplateCategory ctpTemplateCategory : templateCategorys) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                    result.add(ctpTemplateCategory);
                }
            }
        }
        return result;
    }


    

    
   
    public boolean isAccountAdmin() throws BusinessException {
        return AppContext.getCurrentUser().isAdministrator();
    }

    /**
     * 根据单位ID查询模版分类
     *
     * @param accountId 单位Id.
     * @param type 模版类型
     * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
     * @return List<TemplateCategory>
     */
    public List<CtpTemplateCategory> getCategory(Long accountId, int type) throws BusinessException {
    	List<ModuleType> _type = new ArrayList<ModuleType>();
    	_type.add(ModuleType.getEnumByKey(type));
        List<CtpTemplateCategory> templateCategorys = templateManager.getCategorys(accountId, _type);
        //Collections.sort(templateCategorys);
        return templateCategorys;
    }




    

    /**
	  * 查询可管理模版分类
	  *
	  * @param 	userId			用户Id
	  * @param 	accountId 		单位Id
	  * @param 	templateCategories	模版分类
	  * @throws com.seeyon.ctp.common.exceptions.BusinessException If an error occurs.
	  * @return List<Long> 模版分类id
	  */
    public List<Long> getCanManager(Long userId,Long accountId,List<CtpTemplateCategory> templateCategories) throws BusinessException{
        if(templateCategories == null || templateCategories.isEmpty())
            return null;
        List<Long> canManagerList = new ArrayList<Long>();
        for(CtpTemplateCategory category:templateCategories){
            if(templateManager.isTemplateCategoryManager(userId.longValue(), accountId.longValue(),category)){
                canManagerList.add(category.getId());
            }
        }
        return canManagerList;
    }

    /* (non-Javadoc)
     * @see com.seeyon.apps.template.manager.CollaborationTemplateManager#saveCategory(com.seeyon.apps.template.vo.TemplateCategory)
     */
    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.TtempletManager})
    public TemplateCategory saveCategory(TemplateCategory category) throws BusinessException {
        category.setCreateDate(new Date());
        category.setModifyDate(new Date());
        category.setNewId();
        category.setDelete(false);
        templateManager.saveCtpTemplateCategory(category.toPO());
        String memberIds = category.getAuth();
        if (!StringUtil.checkNull(memberIds)) {
            String[] m1 = memberIds.split(",");
            List<CtpTemplateAuth> ctpTemplateAuth = new ArrayList<CtpTemplateAuth>();
            CtpTemplateAuth auth = null;
            for (String s : m1) {
                Long memberId = Long.valueOf(s.split("[|]")[1]);
                auth = new CtpTemplateAuth();
                auth.setNewId();
                auth.setModuleType(-1);
                auth.setModuleId(category.getId());
                auth.setAuthType(V3xOrgEntity.ORGENT_TYPE_MEMBER);
                auth.setAuthId(memberId);
                auth.setSort(1);
                auth.setCreateDate(new Date());
                ctpTemplateAuth.add(auth);
            }
            templateManager.saveCtpTemplateAuths(ctpTemplateAuth);
            templateManager.updatePrivAuth(memberIds,null);
        }
        return category;
    }

   

    @Override
    @CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.TtempletManager})
    public TemplateCategory updateCategory(TemplateCategory category) throws BusinessException {
        category.setModifyMember(AppContext.getCurrentUser().getId());
        category.setModifyDate(new Date());
        category.setDelete(false);
        CtpTemplateCategory old = getCategoryById(category.getId());
        if(old!=null){
            category.setCreateDate(old.getCreateDate());
            category.setCreateMember(old.getCreateMember());
            if(null != old.getType()){
            	category.setType(old.getType());
            }
        }
        CtpTemplateCategory pocategory = category.toPO();
        templateManager.updateCtpTemplateCategory(pocategory);
        //得到该模板的授权信息
        List<CtpTemplateAuth> ctpTemplateAuths = templateManager.getCtpTemplateAuths(category.getId(), -1);
        // 先删除之前的授权
        templateManager.deleteCtpTemplateAuths(category.getId(), null);
        String memberIds = category.getAuth();
        List<String> oldAuthInfoList = new ArrayList<String>();//以前该模板的授权信息
        List<String> deleteIdsList = new ArrayList<String>();//存放此次该模板删除的

        List<String> curAuthIds = (List<String>)Arrays.asList(memberIds.split(","));

        for(int a = 0 ; a < ctpTemplateAuths.size(); a ++){
        	CtpTemplateAuth ctpTemplateAuth = ctpTemplateAuths.get(a);
        	oldAuthInfoList.add("Member|"+ctpTemplateAuth.getAuthId());
        }
    	for(int n = 0 ; n <oldAuthInfoList.size(); n ++){
    		if(!curAuthIds.contains(oldAuthInfoList.get(n))){
    			deleteIdsList.add(oldAuthInfoList.get(n));
    		}
    	}
        if (!StringUtil.checkNull(memberIds)) {
            String[] m1 = memberIds.split(",");
            List<CtpTemplateAuth> ctpTemplateAuth = new ArrayList<CtpTemplateAuth>();
            CtpTemplateAuth auth = null;
            for (String s : m1) {
                Long memberId = Long.valueOf(s.split("[|]")[1]);
                auth = new CtpTemplateAuth();
                auth.setNewId();
                auth.setModuleType(-1);
                auth.setModuleId(category.getId());
                auth.setAuthType(V3xOrgEntity.ORGENT_TYPE_MEMBER);
                auth.setAuthId(memberId);
                auth.setSort(1);
                auth.setCreateDate(new Date());
                ctpTemplateAuth.add(auth);
            }
            templateManager.saveCtpTemplateAuths(ctpTemplateAuth);
        }
        templateManager.updatePrivAuth(memberIds,deleteIdsList);
        return category;
    }



    @Override
    public String getCategory2HTML(Long accountId, Long parentCategoryId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        StringBuilder categoryHTML = new StringBuilder();
        List<Long> categoryTypes = new ArrayList<Long>();
        categoryTypes.add(Long.valueOf(ModuleType.collaboration.ordinal()));
        categoryTypes.add(Long.valueOf(ModuleType.form.ordinal()));
        List<ModuleType> intList = new ArrayList<ModuleType>();
        intList.add(ModuleType.collaboration);
        intList.add(ModuleType.form);
        List<CtpTemplateCategory> templeteCategories = this.templateManager.getCategorys(accountId, intList);
        List<CtpTemplateCategory> notNeedCategorys = templateManager.getSubCategorys(accountId, parentCategoryId);
        notNeedCategorys.add(templateManager.getCtpTemplateCategory(parentCategoryId));
        templeteCategories.removeAll(notNeedCategorys);
        List<CtpTemplateCategory> result  = checkCategoryAuth(accountId, templeteCategories, user.getId());
        if(AppContext.getCurrentUser().isAdministrator()){
            categoryHTML.append("<option value=\"1\">").append(ResourceUtil.getString("collaboration.template.category.type.0")).append("</option>");
        }
        return category2HTML(result, categoryHTML.toString(), categoryTypes, 1);
    }
   
    @Override
    public CtpTemplateCategory getCategoryById(Long id) throws BusinessException{
        return templateManager.getCtpTemplateCategory(id);
    }

    @Override
    public String[] getCategoryAuths(CtpTemplateCategory c) throws BusinessException{
        String[] result = new String[2];
        Set<CtpTemplateAuth> auths = templateManager.getCategoryAuths(c);
        if(auths != null){
            StringBuilder sbValue = new StringBuilder();
            StringBuilder sbTxt = new StringBuilder();
            int i = 0;
            for (CtpTemplateAuth ctpTemplateAuth : auths) {
                if(i != 0){
                    sbValue.append(",");
                    sbTxt.append(",");
                }
                sbValue.append(ctpTemplateAuth.getAuthType()).append("|").append(ctpTemplateAuth.getAuthId());
                V3xOrgMember member = orgManager.getMemberById(ctpTemplateAuth.getAuthId());
                if(null != c.getOrgAccountId() && c.getOrgAccountId().equals(member.getOrgAccountId())  ){
                	sbTxt.append(member.getName()+"("+orgManager.getAccountById(member.getOrgAccountId()).getShortName()+")");
                }else{
                	sbTxt.append(member.getName());
                }
                i++;
            }
            result[0] = sbValue.toString();
            result[1] = sbTxt.toString();
        }
        return result;
    }

	


    public class ComparatorTemplateBO implements Comparator<TemplateBO> {

        public String sortType = "asc";

        public ComparatorTemplateBO() {
            super();
        }

        public ComparatorTemplateBO(String sortType) {
            this.sortType = sortType;
        }

        public int compare(TemplateBO arg0, TemplateBO arg1) {
            TemplateBO template0 = (TemplateBO) arg0;
            TemplateBO template1 = (TemplateBO) arg1;
            int flag = template0.getAuth().compareTo(template1.getAuth());
            return "asc".equals(sortType) ? flag : -1 * flag;
        }
    }





   /* @Override
	public List<CtpTemplate> getCtpTemplate(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
        return this.templateDao.getCtpTemplate(flipInfo, params);
    }*/


	/*@Override
    public void deletePersonalTempleteConfig(Long[] ids) throws BusinessException{
        templateManager.deleteCtpTemplateConfig(ids, AppContext.currentUserId());
    }*/

    @Override
	public void updateTempleteConfigSort(Long[] ids) throws BusinessException {
		if (ids == null)
			return;

		JDBCAgent dba = new JDBCAgent();
		try {
			dba.batch1Prepare("update CTP_TEMPLATE_CONFIG set SORT = ? where TEMPLETE_ID = ? and MEMBER_ID = ? ");

			List<Object> dataList = null;

			for (int sort = 0; sort < ids.length; sort++) {

				dataList = new ArrayList<Object>();
				dataList.add(sort);
				dataList.add(ids[sort]);
				dataList.add(AppContext.currentUserId());
				dba.batch2Add(dataList);

			}
			dba.batch3Execute();
		} catch (Throwable e) {
			LOGGER.error("", e);
		} finally {
			dba.close();
		}

	}

    @Override
    public void updateTempleteHistory(Long id) throws BusinessException {
       updateTempleteHistory(id,AppContext.currentUserId());
    }

    public void updateTempleteHistory(Long id,Long memberId) throws BusinessException {
        templateManager.saveTemplateRecent(id, memberId);
    }
    


    /**
     * @param orgManager the orgManager to set
     */
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    /**
     * @param roleManager the roleManager to set
     */
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    /**
     * @param orgManagerDirect the orgManagerDirect to set
     */
    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
    }
    @Override
    @SuppressWarnings("unchecked")
	public String saveTemplate() throws BusinessException {
		Map para = ParamUtil.getJsonDomain("colMainData");
		User user = AppContext.getCurrentUser();

		String type = (String)para.get("type");
		Long overId = -1L;
		Long formparentid = null;
		String bodytype = (String)para.get("tembodyType");
		if(Strings.isNotBlank((String)para.get("overId"))){//overId即为要覆盖的老模板的ID
			overId = Long.parseLong((String)para.get("overId"));
			templateManager.deleteCtpTemplate(overId);//删除模板
			if(!("0".equals((String)para.get("contentIdUseDelete")))){
			  ctpMainbodyManager.onlyDeleteContentById(Long.valueOf((String)para.get("contentIdUseDelete")));
			}
		}

		Timestamp createDate = new Timestamp(System.currentTimeMillis());

		CtpTemplate template = null;
		//设置模板的一些信息
		template = (CtpTemplate) ParamUtil.mapToBean(para, new CtpTemplate(),false);
		if( null!=para.get("personTid") && Strings.isNotBlank((String)para.get("personTid"))){
			template.setId(Long.valueOf((String)para.get("personTid")));
		}
		if(null != bodytype && Strings.isNotBlank(bodytype)){
			template.setBodyType(bodytype);
		}else{
			template.setBodyType(String.valueOf(MainbodyType.HTML.getKey()));//默认为10HTML类型
		}
		try{
			template.setCanTrackWorkflow(Integer.parseInt((String)para.get("canTrackWorkFlow")));
		}catch(Exception e){
			template.setCanTrackWorkflow(0);
		}
		String temCanSuperviseString = (String)para.get("temCanSupervise");
		template.setCanSupervise(Strings.isBlank(temCanSuperviseString) ? true: Boolean.valueOf(temCanSuperviseString));
		template.setSubject((String)para.get("saveAsTempleteSubject"));
		String invokeTemplateId = (String)para.get("tId");
		CtpTemplate parentTemplate = null;
		if(StringUtils.isNotBlank(invokeTemplateId)){
			 parentTemplate = templateManager.getCtpTemplate(Long.parseLong(invokeTemplateId));
			 //表单个人模版的父id，当调用表单模版时该id为空，调用表单模版另存个人模版后再次另存页面没有刷新该id为空，当调用表单个人模版是该id不为空。
			 String temformParentId = (String)para.get("temformParentId");
			if(Strings.isNotBlank(temformParentId)){
				formparentid = Long.parseLong(temformParentId);
				template.setFormParentid(formparentid);
			}else {
				//如果是表单需要保存parent，或者是系统模板用于督办
				if(String.valueOf(MainbodyType.FORM.getKey()).equalsIgnoreCase(bodytype) || parentTemplate.isSystem())
					template.setFormParentid(Long.parseLong(invokeTemplateId));
			}
		}
		//正文
		ContentSaveOrUpdateRet content = null;
		ColSummary colSummary  = (ColSummary) ParamUtil.mapToBean(para, new ColSummary(),false);
		colSummary.setId(null);
		colSummary.setResentTime(0);
		String summary="";
		/**
		 * 协同模板：将把协同所有的信息作为模板保存，调用者不可修改流程，其他信心可以修改。
		 * 格式模板：将把协同的正文模板保存，调用者只引用正文。
		 * 流程模板：将把协同流程作为模板保存，调用者只能引用流程，不允许修改流程。
		 */
		if (TemplateEnum.Type.template.name().equals(type)) {
			if(para.get("processTermTypeCheck")==null){
				colSummary.setProcessTermType(null);
			}
			if(para.get("remindIntervalCheckBox")==null){
				colSummary.setRemindInterval(null);
			}
			//保存合并处理策略
	        Map<String,String> mergeDealType = new HashMap<String,String>();
	        String canStartMerge= (String)para.get("canStartMerge");
	        if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
	     	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
	        }
	        String canPreDealMerge= (String)para.get("canPreDealMerge");
	        if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
	        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
	        }
	        String canAnyDealMerge= (String)para.get("canAnyDealMerge");
	        if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
	        	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
	        }
	        colSummary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));
			summary = XMLCoder.encoder(colSummary);
			//其他模板修改存为协同模板的时候，这里需要将ID 设置成正文的moduleID。。。。
			 AppContext.putThreadContext("_perTemModuleId",template.getId());
			content= TemplateContentUtil.
					contentSaveOrUpdate(TemplateContentUtil.OperationType.personalTemplate, new AffairData(),template.getSubject(),false);
			template.setWorkflowId(Long.parseLong(content.getProcessId()));
			Long _contentSaveId = Long.valueOf((String)para.get("contentSaveId"));
			template.setBody(_contentSaveId);
			
			
			 updatePermissionRef(user);
		     
			//删除表单模板产生的动态数据，该数据存在ctpContentBean.content字段中，不需要在表单动态表中产生数据
			 /*if(Integer.valueOf((String)para.get("bodyType")).intValue() == MainbodyType.FORM.getKey()){
				//1.获取从content对象(不能直接使用content.getContent()，无法更新)；
			  Long _contentDataId = Long.valueOf((String)para.get("contentDataId"));
				List<CtpContentAll> contentAllList = ctpMainbodyManager.getContentListByContentDataIdAndModuleType(ModuleType.collaboration.getKey(), _contentDataId);
				CtpContentAll contentAll = contentAllList.get(0);
				//使用该变量表示是否需要删除数据：如果新建协同调用模板保存待发后再另存为模板，此时一条formdata数据就对应两条contentAllBean数据了
				//如果通过ContentDataId查出有多条数据的话，保存模板后不删除数据，否则其他协同打开时也找不到数据了。
				boolean needDelFormData = true;
				if(contentAllList.size()>1){
					needDelFormData = false;
					for(CtpContentAll c:contentAllList ){
						if(c.getId().longValue()==_contentSaveId.longValue()){
							contentAll = c;
						}
					}
				}
				//2.获取表单数据Json数据；
				FormManager formManager = (FormManager) AppContext.getBean("formManager");
            	String _content=formManager.getSessioMasterDataBean(_contentDataId).getDataJsonString();
            	//3.将JSON对象设置为content的content值；
            	contentAll.setContent(_content);
            	//4. 更新content对象；
            	ctpMainbodyManager.saveOrUpdateContentAll(contentAll);
            	//5.删除对应的表单数据；
            	if(needDelFormData){
            		try {   
            			Long _contentTemplateId = Long.valueOf(Strings.isBlank((String)para.get("contentTemplateId"))? "0" : (String)para.get("contentTemplateId"));
                        FormService.deleteFormData(_contentDataId,_contentTemplateId);
                        FormService.deleteFlowAtts(template.getId());
                    } catch (SQLException e) {
                        LOGGER.error("删除表单数据异常", e);
                        throw new BusinessException(e);
                    }
            	}
            	//formManager.removeSessionMasterDataBean(content.getContent().getContentDataId());
			}*/
		}else if (TemplateEnum.Type.workflow.name().equals(type)) {// 存流程，流程模板不保存正文
			if(para.get("processTermTypeCheck")==null){
				colSummary.setProcessTermType(null);
			}
			Long processId = TemplateContentUtil.workflowTemplate(template.getSubject());
			template.setWorkflowId(processId);
			template.setProjectId(null);
			template.setStandardDuration(null);
			colSummary = setSummaryInfo(colSummary,"workflow");
			summary = XMLCoder.encoder(colSummary);
			updatePermissionRef(user);
			ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.collaboration,overId);
		}else if(TemplateEnum.Type.text.name().equals(type)){// 格式模板，存正文，不存流程

			template.setWorkflowId(null);
			template.setStandardDuration(null);
			template.setProjectId(null);
			colSummary = setSummaryInfo(colSummary,"text");
			summary = XMLCoder.encoder(colSummary);
			Long _contentSaveId = Long.valueOf((String)para.get("contentSaveId"));
			template.setBody(_contentSaveId);
		}

		boolean isSave =false;
		if(overId == -1L) {
			isSave = true;
		}
		if(String.valueOf(MainbodyType.FORM.getKey()).equalsIgnoreCase(bodytype) && overId == -1L){
			isSave = true;
		}
		template.setIdIfNew();
		long templateId = template.getId();
		template.setSummary(summary);
		template.setCreateDate(createDate);
		template.setModifyDate(createDate);
		template.setMemberId(user.getId());
		template.setSystem(false);
		template.setDelete(false);
		template.setState(0);
		template.setOrgAccountId(user.getLoginAccount());

		if(!isSave && TemplateEnum.Type.template.name().equals(type)){
			// 删除原有附件
			this.attachmentManager.deleteByReference(templateId);
		}
		if(TemplateEnum.Type.template.name().equals(type)){// 保存附件
			//String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.collaboration,templateId,templateId);
			String attaFlag = this.saveAttachmentFromDomain(ApplicationCategoryEnum.collaboration,templateId);
			if(com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)){
	        	CtpTemplateUtil.setHasAttachments(template, true);
	        }
		}
        if(template.getFormParentid() !=null){//另存表单个人模版时先判断父模版id是否存在
        	
        	if(parentTemplate != null){
        		  template.setOrgAccountId(parentTemplate.getOrgAccountId());
            	  template.setFormAppId(parentTemplate.getFormAppId());
        	}
        	
        	if("".equals(invokeTemplateId) || "null".equals(invokeTemplateId) || invokeTemplateId == null)
        		parentTemplate = templateManager.getCtpTemplate(template.getFormParentid());
        	if(parentTemplate ==null){
        		return "templete_notsavePersonalSuccess";//该个人模版所引用的模版已被删除,不能进行另存
        	}else
        		template.setOrgAccountId(parentTemplate.getOrgAccountId());
        }
        
        if(template.getModuleType() == null){
            template.setModuleType(ModuleType.collaboration.ordinal());
        }
        
		//if (isSave) { //新建
			templateManager.saveCtpTemplate(template);
			//TODO templeteConfigManager.pushThisTempleteToMain4Member(templete.getMemberId(), templeteId, -1);//将当前模板推送到首页
		//}else { // 修改
			//templateManager.updateCtpTemplate(template);
		//}
		if(TemplateEnum.Type.template.name().equals(type)) {

		    Map superviseMap = ParamUtil.getJsonDomain("colMainData");
	        SuperviseSetVO ssvo = (SuperviseSetVO)ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
			superviseManager.saveOrUpdateSupervise4Template(template.getId(),ssvo);
		}
		// 将当前模板推送到首页-我的模板
		List<Long> authMemberIdsList = new ArrayList<Long>();
		authMemberIdsList.add(AppContext.currentUserId());
        templateManager.updateTempleteConfig(templateId, authMemberIdsList);
		return "templete_savePersonalSuccess"; //成功保存个人模板
	}

	private void updatePermissionRef(User user) throws BPMException, BusinessException {
		Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
		 String processXml = wfdef.get("process_xml");
		 List<String> list = wapi.getWorkflowUsedPolicyIds("collaboration",processXml,null,null);
		 for(String strPname:list){
			 permissionManager.updatePermissionRef(EnumNameEnum.col_flow_perm_policy.name(),strPname,user.getLoginAccount());
		 }
	}

	private ColSummary setSummaryInfo(ColSummary colSummary,String type){
		colSummary.setCanArchive(false);//允许操作
		colSummary.setCanAutostopflow(false);
		colSummary.setCanEdit(false);
		colSummary.setCanEditAttachment(false);
		colSummary.setCanForward(false);
		if(!"workflow".equals( type)){
			colSummary.setCanModify(false);
		}else{
			colSummary.setProcessId(null);
		}
		colSummary.setImportantLevel(1);//重要程度
		colSummary.setDeadline(null);//流程期限
		colSummary.setAdvanceRemind(null);//提前提醒
		colSummary.setArchiveId(null);//预归档
		colSummary.setProjectId(null);//关联项目

		return colSummary;
	}





	@Override
	@CheckRoleAccess(roleTypes={Role_NAME.AccountAdministrator,Role_NAME.TtempletManager})
	public String checkTargethasDupName(String[] ids, String id)
			throws BusinessException {
		StringBuilder sbBuffer = new StringBuilder();
		if(null != ids && Strings.isNotBlank(ids[0]) &&  null !=id){
			CtpTemplateCategory categorybyId = templateManager.getCtpTemplateCategory(Long.valueOf(id));//目标分类
			CtpTemplate ctpTemplate= null;
			Map  halMap = null;
			for(int count = ids.length, a = 0;a<count; a++){
				ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(ids[a]));
				halMap = new HashMap();
				halMap.put("subject",ctpTemplate.getSubject());
				halMap.put("categoryId",categorybyId.getId());
				halMap.put("d",Boolean.FALSE);
				String hql = "from CtpTemplate c where 1=1 and c.subject=:subject and c.categoryId =:categoryId" +
						" and c.delete=:d";
				List find = DBAgent.find(hql, halMap);
				if(null != find && find.size() > 0){
					//sbBuffer.append("目标分类下存在同名模板&lt;"+ctpTemplate.getSubject()+"&gt;,请修改模板名称后再移动.");
					sbBuffer.append(ResourceUtil.getString("coll.summary.validate.lable29",ctpTemplate.getSubject()));
					return sbBuffer.toString();
				}
			}
		}
		return sbBuffer.toString();
	}
	
	/**
     * 获取前台页面的附件
     * @return
     * @throws BusinessException 
     */
    @SuppressWarnings("unchecked")
    @Override
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
            LOGGER.error("", e);
            throw new BusinessException("创建附件出错");
        }
        
        return attachmentManager.create(result);
    }
    public String getMoreTemplateCategorys(String category, String fragmentId, String ordinal) {
		if (StringUtils.isNotBlank(fragmentId)) {
            Map<String, String> preference = portalApi.getPropertys(Long.parseLong(fragmentId),
                    ordinal);
            String panel = SectionUtils.getPanel("all", preference);
            if (!"all".equals(panel)) {
                String tempStr = preference.get(panel + "_value");
                category = tempStr.replaceAll("C_", "");
                StringBuilder sb=new StringBuilder();
                if(Strings.isBlank(category)){
                    sb.append("-1,32");
                    if(AppContext.hasPlugin("collaboration")){
                        sb.append(",1,2");
                    }
                    if(AppContext.hasPlugin("edoc")){
                        sb.append(",4,19,20,21");
                    }
                    category=sb.toString();
                }
            }
        }
		return category;
	}
    
    @AjaxAccess
	public void saveCustomViewType(String value) {
		if(org.apache.commons.lang3.StringUtils.isNotBlank(value)){
			customizeManager.saveOrUpdateCustomize(AppContext.currentUserId(), "template_view_type", value);
		}
	}


    private List<CtpTemplateCategory> getCategorysByAuth(Long accountId, List<ModuleType> types, Long memberId) throws BusinessException{
    	if(accountId == null || types == null){
    		return new ArrayList<CtpTemplateCategory>();
    	}
    	List<ModuleType> _type = new ArrayList<ModuleType>();
    	for(ModuleType t : types){
    		_type.add(t);
    	}
    	
    	List<CtpTemplateCategory> templateCategorys = templateManager.getCategorys(accountId,_type);
       
    	return checkCategoryAuth(accountId, templateCategorys, memberId);
    }
    
    private List<CtpTemplateCategory> checkCategoryAuth(Long accountId, List<CtpTemplateCategory> templateCategorys, Long memberId)
            throws BusinessException {
        List<CtpTemplateCategory> result = new ArrayList<CtpTemplateCategory>();
        if (templateCategorys != null) {
            CtpTemplateCategory temp = null;
            for (CtpTemplateCategory ctpTemplateCategory : templateCategorys) {
                if (ctpTemplateCategory.isDelete() == null || !ctpTemplateCategory.isDelete()) {
                    // 单位管理员可访问所有
                    if (orgManager.isAdministratorById(memberId, accountId)
                            || templateManager.isTemplateCategoryManager(memberId, accountId,
                                    findRootParent(ctpTemplateCategory))){
						try {
							// 返回clone对象
							temp = (CtpTemplateCategory) ctpTemplateCategory.clone();
							temp.setId(ctpTemplateCategory.getId());
							result.add(temp);
						} catch (CloneNotSupportedException e) {
							LOGGER.error("", e);
						}
					}

                }
            }
        }
        return result;
    }


    private String[] getTemplateAuth(CtpTemplate ctpTemplate) throws BusinessException {
        String[] result = new String[2];
        if (ctpTemplate != null) {
            List<CtpTemplateAuth> auths = templateManager.getCtpTemplateAuths(ctpTemplate.getId(), null);

            result[0] = Functions.showOrgEntities(auths, "authId", "authType", null);
            result[1] = Functions.parseElements(auths, "authId", "authType");
        }
        return result;
    }

	
    private CtpTemplateCategory findRootParent(CtpTemplateCategory ctpTemplateCategory) throws BusinessException{
        if (ctpTemplateCategory == null)
            return null;
        if (ctpTemplateCategory.getParentId() == null
                || (ctpTemplateCategory.getParentId() > 0 && ctpTemplateCategory.getParentId() < 100)) {
            return ctpTemplateCategory;
        }
        return findRootParent(templateManager.getCtpTemplateCategory(ctpTemplateCategory.getParentId()));
    }

	

	/*@Override
	public FlipInfo selectTempletes(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}
*//*
	@Override
	public List<CtpTemplateCategory> getSubCategorys(Long accountId, Long id) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}*/

	

}
