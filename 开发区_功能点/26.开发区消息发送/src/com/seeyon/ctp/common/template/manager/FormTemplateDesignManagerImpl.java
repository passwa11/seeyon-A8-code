package com.seeyon.ctp.common.template.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.seeyon.apps.ai.api.AIApi;
import com.seeyon.apps.cip.api.CipApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.seeyonreport.api.SeeyonreportApi;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.cdp.CDPAgent;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.datarelation.manager.DataRelationManager;
import com.seeyon.ctp.common.datarelation.po.DataRelationPO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.po.template.CtpTemplateAuth;
import com.seeyon.ctp.common.po.template.CtpTemplateHistory;
import com.seeyon.ctp.common.po.template.CtpTemplateOrg;
import com.seeyon.ctp.common.po.template.TemplateApprovePO;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.template.enums.Approve;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.enums.TemplateEnum.State;
import com.seeyon.ctp.common.template.event.FormTemplateSaveAllParam;
import com.seeyon.ctp.common.template.event.TemplateSaveEvent;
import com.seeyon.ctp.common.template.po.ApproveRecordsPO;
import com.seeyon.ctp.common.template.utils.TemplateApproveUtil;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.cycle.enums.CycleEnum;
import com.seeyon.ctp.cycle.enums.HourEnum;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.po.CtpTemplateRelationAuth;
import com.seeyon.ctp.form.util.FormConstant;
import com.seeyon.ctp.form.util.SelectPersonOperation;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.organization.manager.RoleManager;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.ctp.workflow.po.ProcessTemplete;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMParticipant;
import net.joinwork.bpm.definition.BPMParticipantType;
import net.joinwork.bpm.definition.BPMProcess;

public class FormTemplateDesignManagerImpl implements FormTemplateDesignManager {

	private static final Log LOG = LogFactory.getLog(FormTemplateDesignManagerImpl.class);
    private TemplateManager     templateManager;
    private AttachmentManager   attachmentManager;
    private AppLogManager       appLogManager;
    private SuperviseManager    superviseManager;
    private MainbodyManager     ctpMainbodyManager;
    private OrgManager          orgManager;
    private OrgManagerDirect    orgManagerDirect;
    private PermissionManager            permissionManager;
    private RoleManager         roleManager;
    private CAPFormManager capFormManager;
    private ProjectApi   projectApi;
    private DocApi docApi;
    private FormApi4Cap3 formApi4Cap3;
    private WorkflowApiManager           wapi;
    private AIApi aiApi;
    private EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
    private SeeyonreportApi seeyonreportApi;
    private CustomizeManager	customizeManager;
	private EnumManager enumManagerNew;
	private TemplateApproveManager templateApproveManager;
	private AffairManager affairManager;
	private UserMessageManager userMessageManager;
    private FormApi4Cap4 formApi4Cap4;
    private MessageRuleManager messageRuleManager;
    private DataRelationManager  dataRelationManager;
    private CipApi cipApi;
    
    public void setCipApi(CipApi cipApi) {
        this.cipApi = cipApi;
    }

    public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
        this.formApi4Cap4 = formApi4Cap4;
    }

    public DataRelationManager getDataRelationManager() {
		return dataRelationManager;
	}
	public void setDataRelationManager(DataRelationManager dataRelationManager) {
		this.dataRelationManager = dataRelationManager;
	}
	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }
	public TemplateManager getTemplateManager() {
		return templateManager;
	}
	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}
	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}
	public AppLogManager getAppLogManager() {
		return appLogManager;
	}
	public void setAppLogManager(AppLogManager appLogManager) {
		this.appLogManager = appLogManager;
	}
	public SuperviseManager getSuperviseManager() {
		return superviseManager;
	}
	public void setSuperviseManager(SuperviseManager superviseManager) {
		this.superviseManager = superviseManager;
	}

	public MainbodyManager getCtpMainbodyManager() {
		return ctpMainbodyManager;
	}
	public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
		this.ctpMainbodyManager = ctpMainbodyManager;
	}
	public OrgManager getOrgManager() {
		return orgManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public OrgManagerDirect getOrgManagerDirect() {
		return orgManagerDirect;
	}
	public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
		this.orgManagerDirect = orgManagerDirect;
	}
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public RoleManager getRoleManager() {
		return roleManager;
	}
	public void setRoleManager(RoleManager roleManager) {
		this.roleManager = roleManager;
	}
	public CAPFormManager getCapFormManager() {
		return capFormManager;
	}
	public void setCapFormManager(CAPFormManager capFormManager) {
		this.capFormManager = capFormManager;
	}
	public ProjectApi getProjectApi() {
		return projectApi;
	}
	public void setProjectApi(ProjectApi projectApi) {
		this.projectApi = projectApi;
	}
	public DocApi getDocApi() {
		return docApi;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public WorkflowApiManager getWapi() {
		return wapi;
	}
	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}
	public AIApi getAiApi() {
		return aiApi;
	}
	public void setAiApi(AIApi aiApi) {
		this.aiApi = aiApi;
	}
	public EnumManager getEm() {
		return em;
	}
	public void setEm(EnumManager em) {
		this.em = em;
	}
	public SeeyonreportApi getSeeyonreportApi() {
		return seeyonreportApi;
	}
	public void setSeeyonreportApi(SeeyonreportApi seeyonreportApi) {
		this.seeyonreportApi = seeyonreportApi;
	}
	public CustomizeManager getCustomizeManager() {
		return customizeManager;
	}
	public void setCustomizeManager(CustomizeManager customizeManager) {
		this.customizeManager = customizeManager;
	}
	public EnumManager getEnumManagerNew() {
		return enumManagerNew;
	}
	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	public TemplateApproveManager getTemplateApproveManager() {
		return templateApproveManager;
	}
	public void setTemplateApproveManager(TemplateApproveManager templateApproveManager) {
		this.templateApproveManager = templateApproveManager;
	}
	public AffairManager getAffairManager() {
		return affairManager;
	}
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public UserMessageManager getUserMessageManager() {
		return userMessageManager;
	}
	public void setUserMessageManager(UserMessageManager userMessageManager) {
		this.userMessageManager = userMessageManager;
	}


	@AjaxAccess
	public Map saveTemplate2Cache(Map dataMap)throws BusinessException{

		Map listMap = new HashMap();
		Map baseInfo = (Map)dataMap.get("baseInfo");
		String defId = (String)baseInfo.get("defId");
		boolean cap4Form =  Strings.isNotBlank((String)baseInfo.get("cap4Flag")) && "1".equals((String)baseInfo.get("cap4Flag"));
		if(cap4Form){
            listMap = saveTemplate2cacheCap4(dataMap,baseInfo,defId,listMap);

		}else{
			listMap = saveTemplate2cacheCap3(dataMap,baseInfo,defId,listMap);
		}

       return listMap ;
	}

	private Map saveTemplate2cacheCap3(Map dataMap,Map baseInfo,String defId,Map listMap)
			throws NumberFormatException, BusinessException{
		FormBean fb = capFormManager.getEditingForm();
        /**************************cap3为做版本审批所以特殊处理下****和cap4保持一致*************************/
        Long templateHistoryId = null;
        Long templateId  = null;
        CtpTemplate template = null;
        CtpTemplateHistory old = null ;
        boolean templateIsNew = false;
        if(Strings.isNotEmpty(String.valueOf(baseInfo.get("templateId")))){
        	templateId =   Long.valueOf((String)baseInfo.get("templateId"));
        	template = fb.getBind().getFlowTemplate(templateId);
        }
        if(template == null){
        	template = new CtpTemplate();
        	template.setNewId();//新建的
        	templateIsNew = true;
        }


        String templateHistoryIdString =  String.valueOf(baseInfo.get("templateHistoryId"));
        if(Strings.isNotEmpty(templateHistoryIdString)){
        	templateHistoryId = Long.valueOf(templateHistoryIdString);
        }

        //获取 & 创建 历史表数据
        CtpTemplateHistory c = convertCtpTemplateHistory(baseInfo, templateHistoryId, template);


        /**************************cap3为做版本审批所以特殊处理下*****************************/


        Map processCreate = (Map)dataMap.get("processCreate");
		//TODO 保存流程信息
		int moduleType = ModuleType.collaboration.getKey();//应用类型 java.lang.String 例如协同为collaboration，公文为edoc等。
		String workflowId = (String)baseInfo.get("workflowId");
        String processName = (String)baseInfo.get("subject");
        String processXml = processCreate.get("process_xml") == null || Strings.isBlank((String)processCreate.get("process_xml")) ? "" : (String)processCreate.get("process_xml");//流程定义模版内容 java.lang.String 流程模版xml内容
        String subProcessSetting = processCreate.get("process_subsetting") == null || Strings.isBlank((String)processCreate.get("process_subsetting")) ? "" : (String)processCreate.get("process_subsetting");//流程模版绑定的子流程信息 java.lang.String 流程模版绑定的子流程信息
        //String workflowRule = processCreate.get("process_rulecontent") == null || Strings.isBlank((String)processCreate.get("process_rulecontent")) ? "" : (String)processCreate.get("process_rulecontent");//流程规则说明
        String workflowRule = (String)baseInfo.get("siwfRule");
        String processId = processCreate.get("process_id") == null || Strings.isBlank((String)processCreate.get("process_id")) ? "-1" : (String)processCreate.get("process_id");
        String processEventJson = processCreate.get("process_event") == null || Strings.isBlank((String)processCreate.get("process_event")) ? "" : (String)processCreate.get("process_event");
        String fbatchId = (String)baseInfo.get("fbatchId");

        String canPraise= (String)baseInfo.get("canPraise");
        c.setCanPraise(( Strings.isBlank(canPraise) || "false".equals(canPraise)) ? false : true);

        String canCopy= (String)baseInfo.get("canCopy");
        c.setCanCopy(( Strings.isBlank(canCopy) || "false".equals(canCopy)) ? false : true);

        String canAIProcessing= (String)baseInfo.get("canAIProcessing");
        c.setCanAIProcessing(( Strings.isBlank(canAIProcessing) || "false".equals(canAIProcessing)) ? false : true);

        String canSupervise = (String)baseInfo.get("canSupervise");
        c.setCanSupervise(( Strings.isBlank(canSupervise) || "false".equals(canSupervise)) ? false : true);

        String scanCodeInput = (String)baseInfo.get("scanCodeInput");
        c.setScanCodeInput(( Strings.isBlank(scanCodeInput) || "false".equals(scanCodeInput)) ? false : true);

        String signetProtect = (String)baseInfo.get("signetProtect");
        c.setSignetProtect(( Strings.isBlank(signetProtect) || "false".equals(signetProtect)) ? false : true);

        //TODO 高级信息
        c.setResponsible((String)baseInfo.get("responsible"));
        c.setAuditor((String)baseInfo.get("auditor"));
        c.setConsultant((String)baseInfo.get("consultant"));
        c.setInform((String)baseInfo.get("inform"));
        c.setCoreUseOrg((String)baseInfo.get("coreUseOrg"));

        String belongOrg = (String)baseInfo.get("belongOrg");
        c.setBelongOrg(Strings.isNotBlank(belongOrg)?Long.valueOf(belongOrg):null);

        String publishTime = (String)baseInfo.get("publishTime");
        if(Strings.isNotBlank(publishTime)){
        	c.setPublishTime(Datetimes.parse(publishTime));
        }


       c.setModifyDate(new Date());
       c.setState(State.normal.ordinal());

       c.setProcessLevel(Integer.valueOf((String)baseInfo.get("processLevel")));
       c.setOrgAccountId(AppContext.currentAccountId());
       c.setMemberId(AppContext.currentUserId());
       c.setModuleType(ModuleType.collaboration.getKey());
       c.setBodyType("" + MainbodyType.FORM.getKey());
       c.setCategoryId(fb.getCategoryId());
       c.setDelete(false);
       c.setFormAppId(Long.valueOf(defId));
       c.setSystem(true);
       c.setType("template");
       //归档
       ColSummary summary = new ColSummary();

       String messageRuleId= (String)baseInfo.get("messageRuleId");
       summary.setMessageRuleId(messageRuleId);
       if(Strings.isNotBlank(messageRuleId)) {
    	   messageRuleManager.updateMessageRuleReferce(messageRuleId, "-1");
       }

       String archiveId= (String)baseInfo.get("archiveId");
       summary.setArchiveId(Strings.isBlank(archiveId) ? null : Long.parseLong(archiveId));
       if (summary.getArchiveId() == null) {
           String archive_Id= (String)baseInfo.get("archive_Id");
           summary.setArchiveId( Strings.isBlank(archive_Id) ? null : Long.parseLong(archive_Id));
       }

       //附件归档
       String attachmentArchiveId = (String)baseInfo.get("attachmentArchiveId");
       summary.setAttachmentArchiveId(Strings.isBlank(attachmentArchiveId) ? null : Long.parseLong(attachmentArchiveId));
       if(baseInfo.get("processTermTypeCheck")==null){
    	   summary.setProcessTermType(null);
		}else{
			String processTermType= (String)baseInfo.get("processTermType");
			summary.setProcessTermType(Integer.valueOf(processTermType));
		}
       if(baseInfo.get("remindIntervalCheck")==null){
    	   summary.setRemindInterval(null);
		}else{
			String remindInterval = (String)baseInfo.get("remindInterval");
			summary.setRemindInterval(Long.valueOf(remindInterval));
		}
       summary.setBodyType("" + MainbodyType.FORM.getKey());

       String canArchive= (String)baseInfo.get("canArchive");
       summary.setCanArchive( ( Strings.isBlank(canArchive) || "false".equals(canArchive))? false : true);

       String canForward= (String)baseInfo.get("canForward");
       summary.setCanForward( ( Strings.isBlank(canForward) || "false".equals(canForward)) ? false : true);

       String canEditAttachment= (String)baseInfo.get("canEditAttachment");
       summary.setCanEditAttachment( ( Strings.isBlank(canEditAttachment) || "false".equals(canEditAttachment)) ? false : true);

       String canModify= (String)baseInfo.get("canModify");
       summary.setCanModify( ( Strings.isBlank(canModify) || "false".equals(canModify)) ? false : true);
       
       String canDeleteNode= (String)baseInfo.get("canDeleteNode");
       summary.setCanDeleteNode( ( Strings.isBlank(canDeleteNode) || "false".equals(canDeleteNode)) ? false : true);

       //保存合并处理策略
       Map<String,String> mergeDealType = new HashMap<String,String>();
       String canStartMerge= (String)baseInfo.get("canStartMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
    	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
       }
       String canPreDealMerge= (String)baseInfo.get("canPreDealMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
       	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
       }
       String canAnyDealMerge= (String)baseInfo.get("canAnyDealMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
       	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
       }
       summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));


       String updateSubject= (String)baseInfo.get("updateSubject");
       summary.setUpdateSubject((Strings.isBlank(updateSubject) || "false".equals(updateSubject)) ? false : true);

       String deadline= (String)baseInfo.get("deadline");
       if(Strings.isBlank(deadline)) {
           summary.setDeadlineTemplate(null);
       } else {
           summary.setDeadlineTemplate(deadline);
           enumManagerNew.updateEnumItemRef(EnumNameEnum.collaboration_deadline.name(), deadline);
       }

       String importantLevel= (String)baseInfo.get("importantLevel");
       summary.setImportantLevel( Strings.isBlank(importantLevel) ? null : Integer.parseInt(importantLevel));
       summary.setTempleteId(c.getId());
       summary.setSubject(c.getSubject());

       String advanceremind= (String)baseInfo.get("advanceremind");
       summary.setAdvanceRemind( Strings.isBlank(advanceremind) ? null : Long.parseLong(advanceremind));
       //显示明细
       if (summary.getArchiveId() != null || summary.getAttachmentArchiveId()!=null) {
           StringBuilder archiverFormid = new StringBuilder("");
           for (FormViewBean fvb : fb.getFormViewList()) {
               String viewId = (String)baseInfo.get("view_" + fvb.getId());
               if (Strings.isNotBlank(viewId)) {
                   if(archiverFormid.length() > 0){
                       archiverFormid.append("|");
                   }
                   archiverFormid.append(viewId + "." + baseInfo.get("auth_" + fvb.getId()));
               }
           }
           if (summary.getArchiveId() != null && Strings.isNotBlank(archiverFormid.toString())) {
               summary.putExtraAttr("archiverFormid", archiverFormid.toString());
           }

           //String archiveForm = map.get("archiveForm");//预归档内容 表单
           String archiveText = fb.hasRedTemplete() ? (String)baseInfo.get("archiveText") : "";//预归档内容 正文
           String archiveTextName = fb.hasRedTemplete() ? (String)baseInfo.get("archiveTextName") : "";//归档后正文名称
           String archiveKeyword = (String)baseInfo.get("archiveKeyword");//文档关键字
           String archiveAll = (String)baseInfo.get("archiveAll");//归档所有内容
           summary.putExtraAttr("archiveText", archiveText);
           summary.putExtraAttr("archiveTextName", archiveTextName);
           summary.putExtraAttr("archiveKeyword", archiveKeyword);
           summary.putExtraAttr("archiveAll", archiveAll);
           JSONObject jo = new JSONObject();
           try {
               jo.put("archiveText", archiveText);
               jo.put("archiveAll", archiveAll);
               jo.put("archiveTextName", archiveTextName);
               jo.put("archiveKeyword", archiveKeyword);
           } catch (JSONException e) {
               LOG.error("", e);
           }

           String archiveField = (String)baseInfo.get("archiveFieldName");
           String archiveIsCreate = (String)baseInfo.get("archiveIsCreate");
           if (Strings.isNotBlank(archiveField)) {
               summary.putExtraAttr("archiveField", archiveField);
               summary.putExtraAttr("archiveIsCreate", archiveIsCreate);
               try {
                   jo.put("archiveField", archiveField);
                   jo.put("archiveFieldName", fb.getFieldBeanByName(archiveField).getDisplay());
                   jo.put("archiveIsCreate", archiveIsCreate);
               } catch (JSONException e) {
                   LOG.error("", e);
               }
           }
           summary.setAdvancePigeonhole(jo.toString());
       } else {
           summary.putExtraAttr("archiverFormid", "");
           summary.putExtraAttr("archiveText", "");
           summary.putExtraAttr("archiveAll", "false");
           summary.putExtraAttr("archiveTextName", "");
           summary.putExtraAttr("archiveKeyword", "");
           summary.putExtraAttr("archiveField", "");
           summary.putExtraAttr("archiveIsCreate", "");
           summary.setAdvancePigeonhole(null);
       }

       String cycleState = Strings.isNotBlank((String)baseInfo.get("cycleState")) ? (String)baseInfo.get("cycleState") : "0";
       summary.putExtraAttr("cycleState", cycleState);
       if ("1".equals(cycleState) && Strings.isNotBlank((String)baseInfo.get("cycleSender"))) {
           summary.putExtraAttr("cycleSender", (String)baseInfo.get("cycleSender"));
           summary.putExtraAttr("cycleStartDate", (String)baseInfo.get("cycleStartDate"));
           summary.putExtraAttr("cycleEndDate", (String)baseInfo.get("cycleEndDate"));
           summary.putExtraAttr("cycleType", (String)baseInfo.get("cycleType"));
           summary.putExtraAttr("cycleMonth", (String)baseInfo.get("cycleMonth"));
           summary.putExtraAttr("cycleOrder", (String)baseInfo.get("cycleOrder"));
           summary.putExtraAttr("cycleDay", (String)baseInfo.get("cycleDay"));
           summary.putExtraAttr("cycleWeek", (String)baseInfo.get("cycleWeek"));
           summary.putExtraAttr("cycleHour", (String)baseInfo.get("cycleHour"));
       } else {
           summary.putExtraAttr("cycleSender", "");
           summary.putExtraAttr("cycleStartDate", "");
           summary.putExtraAttr("cycleEndDate", "");
           summary.putExtraAttr("cycleType", "");
           summary.putExtraAttr("cycleMonth", "");
           summary.putExtraAttr("cycleOrder", "");
           summary.putExtraAttr("cycleDay", "");
           summary.putExtraAttr("cycleWeek", "");
           summary.putExtraAttr("cycleHour", "");
           summary.putExtraAttr("cycleState", "0");
       }


       Map<String, String> superviseMap = ParamUtil.getJsonDomain("superviseDiv");
       String superviseStr = XMLCoder.encoder(superviseMap);
       c.putExtraAttr("superviseStr", superviseStr);

       String replaceSubject = summary.getSubject() == null ? null : summary.getSubject().replaceAll(new String(new char[]{(char)160}), " ");
       summary.setSubject(replaceSubject);
       c.setSubject(replaceSubject);
       c.setSummary(XMLCoder.encoder(summary));
       c.putExtraAttr("summary", summary);

       if(AppContext.hasPlugin("ai")){//有AI插件
    	   setProcessMonitorInfo(baseInfo, c);
       }

       //授权
       String auth = (String)baseInfo.get("auth");
       List<CtpTemplateAuth> authList = new ArrayList<CtpTemplateAuth>();
       if (auth != null && Strings.isNotBlank(auth)) {
           String[] authObj = auth.split(",");
           CtpTemplateAuth templateAuth = null;
           int sort = 1;
           for (String a : authObj) {
               templateAuth = new CtpTemplateAuth();
               String[] authSp = a.split("\\|");
               templateAuth.setId(UUIDLong.longUUID());
               templateAuth.setAuthId(Long.parseLong(authSp[1]));
               templateAuth.setAuthType(authSp[0]);
               templateAuth.setModuleId(c.getId());
               templateAuth.setCreateDate(new Date());
               templateAuth.setModuleType(moduleType);
               templateAuth.setSort(sort++);
               authList.add(templateAuth);
           }
       }
       c.putExtraAttr("authList", authList);

       //关联表单授权
       String relationAuth = (String)baseInfo.get("authRelation");
       List<CtpTemplateRelationAuth> relationAuthList = new ArrayList<CtpTemplateRelationAuth>();
       if (relationAuth != null && Strings.isNotBlank(relationAuth)) {
           String[] relationAuthObj = relationAuth.split(",");
           CtpTemplateRelationAuth formRelationAuth = null;
           for (String a : relationAuthObj) {
               formRelationAuth = new CtpTemplateRelationAuth();
               String[] authSp = a.split("\\|");
               formRelationAuth.setIdIfNew();
               formRelationAuth.setTemplateId(c.getId());
               formRelationAuth.setAuthType(SelectPersonOperation.changeType(authSp[0]));
               formRelationAuth.setAuthValue(String.valueOf(authSp[1]));
               formRelationAuth.setCreateDate(new Date());
               relationAuthList.add(formRelationAuth);
           }
       }
       c.putExtraAttr("relationAuthList", relationAuthList);
       /*//附件
       try {
           attachmentManager.deleteByReference(c.getId(), c.getId());
           attachmentManager.create(ApplicationCategoryEnum.collaboration, c.getId(), c.getId());
           setHasAttachments(c, attachmentManager.hasAttachments(c.getId(), c.getId()));
       } catch (Exception e) {
           throw new BusinessException(e);
       }*/


       try {
			List<Attachment> atts  = attachmentManager.getAttachmentsFromRequest(ApplicationCategoryEnum.collaboration, c.getId(),
			           c.getId(), AppContext.getRawRequest());
			if(Strings.isNotEmpty(atts)){
				setHasAttachments(c,true);
			}
			c.putExtraAttr("attachmentList", atts);
	    } catch (Exception e) {
			LOG.error("", e);
		}


       listMap.put("frombindlist",fb.getBind().getFlowTemplateList());

       
       
       Long newWorkflowId = null;

       if (fb.getBind().getExtraAttr("batchId") == null) {
       		LOG.error("流程模板依据 batchId 值为空！模板保存异常！");
       }

		if (templateIsNew) {// 新建-新建一个流程给模板和模板历史使用
			   
				//如果是第一次创建模板，模板id已经存在了不用在生成id了
				if(Strings.isNotBlank(workflowId) && templateIsNew) {
					newWorkflowId = Long.valueOf(workflowId); 
				}else {
					newWorkflowId = UUIDLong.longUUID();
				}
				wapi.insertWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting,
						workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId), Long.parseLong(fbatchId),
						processEventJson,newWorkflowId);
			
			c.setSubstate(Approve.ApproveType.draft.key());

			c.setCreateDate(new Date());
			c.setModifyDate(new Date());
			c.setTemplateId(template.getId());


		} else {

			Long oldProcessId = c.getWorkflowId();

			c.setModifyMember(AppContext.getCurrentUser().getId());

			newWorkflowId = wapi.updateWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting, 
					workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId),
       				Long.parseLong(fbatchId), oldProcessId, processEventJson);
			
			if (c.getExtraAttr("oldProId") == null) {
				c.putExtraAttr("oldProId", oldProcessId);// 用于删除流程模板的
			}

		}
		c.setWorkflowId(newWorkflowId);

        Long orginalTemplateWorkFlowId = (Long) template.getExtraAttr("orginalTemplateWorkFlowId");
        if(orginalTemplateWorkFlowId == null || Long.valueOf(-1).equals(orginalTemplateWorkFlowId) ){ 
        	//为空再添加，确保一次完成的流程编辑保存操作，只添加一次，否则重复添加多次还是有问题。
        	template.putExtraAttr("orginalTemplateWorkFlowId", template.getWorkflowId() == null ? -1 : template.getWorkflowId());
        }
       
        String s = (String)baseInfo.get("updateProcessDesFlag");
        c.putExtraAttr("updateProcessDesFlag", s);

        //因为是cap3所以强制设置为立即生效的数据，所以状态都是是
        /**************************cap3为做版本审批所以特殊处理下*****************************/
        c.setDelete(false);
        c.setSystem(true);
        c.setVersion(1);

        
        Long bakId = template.getId();//id不改变
        c.cloneProperty2ThsTemplate(template);
        template.setId(bakId);

        template.setSystem(true);
        TemplateApprovePO  approve = templateToTemplateApprove (c);


		if(c.getPublishTime() != null && c.getPublishTime().after(new Date())){
			approve.setType(Approve.ApproveType.toBeReleased.key());
			c.setSubstate(Approve.ApproveType.toBeReleased.key());
			c.setState(State.invalidation.ordinal());
		}else{
			approve.setType(Approve.ApproveType.haveReleased.key());
			c.setSubstate(Approve.ApproveType.haveReleased.key());
		}
        approve.setVersion(1);
        approve.setIsDelete(0);

        c.setTemplateId(template.getId());
        c.putExtraAttr("templateApprovePO", approve);
		c.putExtraAttr("cap4Flag",false);
        //最后设置更新了的history
        template.putExtraAttr("templateHistory", c);
//		template.setPublishTime(new Date());
        /**************************cap3为做版本审批所以特殊处理下*****************************/
        
        fb.getBind().addFlowTemplate(template);
        
        //微服务问题：更新缓存
        try {
            capFormManager.addEditForm(fb);
        } catch (CloneNotSupportedException e) {
            LOG.error("", e);
        }
       return listMap;
	}


	private Map saveTemplate2cacheCap4(Map dataMap,Map baseInfo,String defId,Map listMap)
			throws NumberFormatException, BusinessException{

		com.seeyon.cap4.form.bean.FormBean cap4fb = formApi4Cap4.checkAndLoadForm2Session(Long.valueOf(defId), "");
        Long templateHistoryId = null;
        Long templateId  = null;
        CtpTemplate template = null;
        boolean templateIsNew = false;
        //这个就是模板ID
        if(Strings.isNotEmpty(String.valueOf(baseInfo.get("templateId")))){
            templateId =   Long.valueOf((String)baseInfo.get("templateId"));
            template = cap4fb.getBind().getFlowTemplate(templateId);
            if(template == null ){
				template = templateManager.getCtpTemplate(templateId);
			}
        }
        if(template == null){
            template = new CtpTemplate();
            template.setNewId();//新建的
            templateIsNew = true;
        }else{ //修改的数据首先重置为最初的状态
            HashMap<String,String> versionMap = (HashMap<String,String>)template.getExtraMap().get("versionMap");
            if(versionMap != null){
                if(versionMap.containsKey("approveVersion")){
                    template.setApproveVersion(versionMap.get("approveVersion"));
                }else{
                    template.setApproveVersion("");
                }
                if(versionMap.containsKey("toBeReleasedVersion")){
                    template.setToBeReleasedVersion(versionMap.get("toBeReleasedVersion"));
                }else{
                    template.setToBeReleasedVersion("");
                }
                if(versionMap.containsKey("version")){
                    template.setVersion(Integer.parseInt(versionMap.get("version")));
                }else{
                    template.setVersion(null);
                }
            }else{
                template.setVersion(null);
                template.setToBeReleasedVersion("");
                template.setApproveVersion("");
            }
        }


        String templateHistoryIdString =  String.valueOf(baseInfo.get("templateHistoryId"));
        if(Strings.isNotEmpty(templateHistoryIdString)){
        	templateHistoryId = Long.valueOf(templateHistoryIdString);
        }

        //获取 & 创建 历史表数据
        CtpTemplateHistory c = convertCtpTemplateHistory(baseInfo, templateHistoryId, template);

        Map<String,Object> approveInfo = (Map<String, Object>) dataMap.get("approveInfo");

        //版本号递增方案及审批相关的逻辑
        boolean isVersionIncrement = computeTemplateVersion(template, c, approveInfo);

        template.setPublishTime(c.getPublishTime());//时间应该从历史记录中获取
  		//流程信息
		Map processCreate = (Map)dataMap.get("processCreate");
		//TODO 保存流程信息
		int moduleType = ModuleType.collaboration.getKey();//应用类型 java.lang.String 例如协同为collaboration，公文为edoc等。
        String processName = (String)baseInfo.get("subject");
        String workflowId = (String)baseInfo.get("workflowId");
      //这里需要重新获取，-前端或者后端
        String processXml = processCreate.get("process_xml") == null || Strings.isBlank((String)processCreate.get("process_xml")) ? "" : (String)processCreate.get("process_xml");//流程定义模版内容 java.lang.String 流程模版xml内容
        String subProcessSetting = processCreate.get("process_subsetting") == null || Strings.isBlank((String)processCreate.get("process_subsetting")) ? "" : (String)processCreate.get("process_subsetting");//流程模版绑定的子流程信息 java.lang.String 流程模版绑定的子流程信息
        String workflowRule = (String)baseInfo.get("siwfRule");
        String processId = processCreate.get("process_id") == null || Strings.isBlank((String)processCreate.get("process_id")) ? "-1" : (String)processCreate.get("process_id");
        String processEventJson = processCreate.get("process_event") == null || Strings.isBlank((String)processCreate.get("process_event")) ? "" : (String)processCreate.get("process_event");
        String fbatchId = (String)baseInfo.get("fbatchId");

        String canPraise= (String)baseInfo.get("canPraise");
        c.setCanPraise(( Strings.isBlank(canPraise) || "false".equals(canPraise)) ? false : true);

        String canCopy= (String)baseInfo.get("canCopy");
        c.setCanCopy(( Strings.isBlank(canCopy) || "false".equals(canCopy)) ? false : true);


        String canAIProcessing= (String)baseInfo.get("canAIProcessing");
        c.setCanAIProcessing(( Strings.isBlank(canAIProcessing) || "false".equals(canAIProcessing)) ? false : true);

        String canSupervise = (String)baseInfo.get("canSupervise");
        c.setCanSupervise(( Strings.isBlank(canSupervise) || "false".equals(canSupervise)) ? false : true);

        String scanCodeInput = (String)baseInfo.get("scanCodeInput");
        c.setScanCodeInput(( Strings.isBlank(scanCodeInput) || "false".equals(scanCodeInput)) ? false : true);

        String signetProtect = (String)baseInfo.get("signetProtect");
        c.setSignetProtect(( Strings.isBlank(signetProtect) || "false".equals(signetProtect)) ? false : true);
        //TODO 高级信息
        c.setResponsible((String)baseInfo.get("responsible"));
        c.setAuditor((String)baseInfo.get("auditor"));
        c.setConsultant((String)baseInfo.get("consultant"));
        c.setInform((String)baseInfo.get("inform"));
        c.setCoreUseOrg((String)baseInfo.get("coreUseOrg"));

        String belongOrg = (String)baseInfo.get("belongOrg");
        c.setBelongOrg(Strings.isNotBlank(belongOrg)?Long.valueOf(belongOrg):null);
       c.setModifyDate(new Date());
//       c.setState(State.normal.ordinal());
       c.setProcessLevel(Integer.valueOf((String)baseInfo.get("processLevel")));
       c.setOrgAccountId(AppContext.currentAccountId());
       c.setMemberId(AppContext.currentUserId());
       c.setModuleType(ModuleType.collaboration.getKey());
       c.setBodyType("" + MainbodyType.FORM.getKey());
       c.setCategoryId(cap4fb.getCategoryId());
       c.setDelete(false);
       c.setFormAppId(Long.valueOf(defId));
       c.setSystem(true);
       c.setType("template");

       //归档
       ColSummary summary = new ColSummary();
       String messageRuleId= (String)baseInfo.get("messageRuleId");
       summary.setMessageRuleId(messageRuleId);
       if(Strings.isNotBlank(messageRuleId)) {
    	   messageRuleManager.updateMessageRuleReferce(messageRuleId, "-1");
       }
       String archiveId= (String)baseInfo.get("archiveId");
       summary.setArchiveId(Strings.isBlank(archiveId) ? null : Long.parseLong(archiveId));
       if (summary.getArchiveId() == null) {
           String archive_Id= (String)baseInfo.get("archive_Id");
           summary.setArchiveId( Strings.isBlank(archive_Id) ? null : Long.parseLong(archive_Id));
       }

       //附件归档
       String attachmentArchiveId = (String)baseInfo.get("attachmentArchiveId");
       summary.setAttachmentArchiveId(Strings.isBlank(attachmentArchiveId) ? null : Long.parseLong(attachmentArchiveId));
       if(baseInfo.get("processTermTypeCheck")==null){
    	   summary.setProcessTermType(null);
		}else{
			String processTermType= (String)baseInfo.get("processTermType");
			summary.setProcessTermType(Integer.valueOf(processTermType));
		}
       if(baseInfo.get("remindIntervalCheck")==null){
    	   summary.setRemindInterval(null);
		}else{
			String remindInterval = (String)baseInfo.get("remindInterval");
			summary.setRemindInterval(Long.valueOf(remindInterval));
		}
       summary.setBodyType("" + MainbodyType.FORM.getKey());

       String canArchive= (String)baseInfo.get("canArchive");
       summary.setCanArchive( ( Strings.isBlank(canArchive) || "false".equals(canArchive))? false : true);

       String canForward= (String)baseInfo.get("canForward");
       summary.setCanForward( ( Strings.isBlank(canForward) || "false".equals(canForward)) ? false : true);

       String canEditAttachment= (String)baseInfo.get("canEditAttachment");
       summary.setCanEditAttachment( ( Strings.isBlank(canEditAttachment) || "false".equals(canEditAttachment)) ? false : true);

       String canModify= (String)baseInfo.get("canModify");
       summary.setCanModify( ( Strings.isBlank(canModify) || "false".equals(canModify)) ? false : true);

       String canDeleteNode= (String)baseInfo.get("canDeleteNode");
       summary.setCanDeleteNode( ( Strings.isBlank(canDeleteNode) || "false".equals(canDeleteNode)) ? false : true);
       //保存合并处理策略
       Map<String,String> mergeDealType = new HashMap<String,String>();
       String canStartMerge= (String)baseInfo.get("canStartMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)){
    	   mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
       }
       String canPreDealMerge= (String)baseInfo.get("canPreDealMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)){
       	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
       }
       String canAnyDealMerge= (String)baseInfo.get("canAnyDealMerge");
       if((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)){
       	mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
       }
       summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));

       String updateSubject= (String)baseInfo.get("updateSubject");
       summary.setUpdateSubject((Strings.isBlank(updateSubject) || "false".equals(updateSubject)) ? false : true);


       String deadline= (String)baseInfo.get("deadline");
       if(Strings.isBlank(deadline)) {
           summary.setDeadlineTemplate(null);
       } else {
           summary.setDeadlineTemplate(deadline);
           enumManagerNew.updateEnumItemRef(EnumNameEnum.collaboration_deadline.name(), deadline);
       }

       String importantLevel= (String)baseInfo.get("importantLevel");
       summary.setImportantLevel( Strings.isBlank(importantLevel) ? null : Integer.parseInt(importantLevel));
       summary.setTempleteId(c.getId());
       summary.setSubject(c.getSubject());

       String advanceremind= (String)baseInfo.get("advanceremind");
       summary.setAdvanceRemind( Strings.isBlank(advanceremind) ? null : Long.parseLong(advanceremind));
       //显示明细
       if (summary.getArchiveId() != null || summary.getAttachmentArchiveId()!=null) {
		   StringBuilder archiverFormid = new StringBuilder("");
           for (com.seeyon.cap4.form.bean.FormViewBean fvb : cap4fb.getFormViewList()) {
               String viewId = (String)baseInfo.get("view_" + fvb.getId());
               if (Strings.isNotBlank(viewId)) {
                   if(archiverFormid.length() > 0){
                       archiverFormid.append("|");
                   }
                   archiverFormid.append(viewId + "." + baseInfo.get("auth_" + fvb.getId()));
               }
           }
           if (Strings.isNotBlank(archiverFormid.toString())) {
               summary.putExtraAttr("archiverFormid", archiverFormid.toString());
           }

           //String archiveForm = map.get("archiveForm");//预归档内容 表单
           String archiveAll = (String)baseInfo.get("archiveAll");//文档所有内容
           String archiveText = cap4fb.hasRedTemplete() ? (String)baseInfo.get("archiveText") : "";//预归档内容 正文
           String archiveTextName = cap4fb.hasRedTemplete() ? (String)baseInfo.get("archiveTextName") : "";//归档后正文名称
           String multipleArchiveTextName = cap4fb.hasRedTemplete() ? (String)baseInfo.get("multipleArchiveTextName") : "";//归档后正文名称
           String archiveKeyword = (String)baseInfo.get("archiveKeyword");//文档关键字
           summary.putExtraAttr("archiveAll", archiveAll);
           summary.putExtraAttr("archiveText", archiveText);
           summary.putExtraAttr("archiveTextName", archiveTextName);
           summary.putExtraAttr("multipleArchiveTextName", multipleArchiveTextName);
           summary.putExtraAttr("archiveKeyword", archiveKeyword);
           JSONObject jo = new JSONObject();
           try {
               jo.put("archiveText", archiveText);
               jo.put("archiveAll", archiveAll);
               jo.put("archiveTextName", archiveTextName);
               jo.put("multipleArchiveTextName", multipleArchiveTextName);
               jo.put("archiveKeyword", archiveKeyword);
           } catch (JSONException e) {
               LOG.error("", e);
           }

           String archiveField = (String)baseInfo.get("archiveFieldName");
           String archiveIsCreate = (String)baseInfo.get("archiveIsCreate");
           if (Strings.isNotBlank(archiveField)) {
               summary.putExtraAttr("archiveField", archiveField);
               summary.putExtraAttr("archiveIsCreate", archiveIsCreate);
               try {
                   jo.put("archiveField", archiveField);
                   jo.put("archiveFieldName", cap4fb.getFieldBeanByName(archiveField).getDisplay());
                   jo.put("archiveIsCreate", archiveIsCreate);
               } catch (JSONException e) {
                   LOG.error("", e);
               }
           }
           summary.setAdvancePigeonhole(jo.toString());
       } else {
           summary.putExtraAttr("archiverFormid", "");
           summary.putExtraAttr("archiveText", "");
           summary.putExtraAttr("archiveAll", "false");
           summary.putExtraAttr("archiveTextName", "");
           summary.putExtraAttr("multipleArchiveTextName", "");
           summary.putExtraAttr("archiveKeyword", "");
           summary.putExtraAttr("archiveField", "");
           summary.putExtraAttr("archiveIsCreate", "");
           summary.setAdvancePigeonhole(null);
       }

       String cycleState = Strings.isNotBlank((String)baseInfo.get("cycleState")) ? (String)baseInfo.get("cycleState") : "0";
       summary.putExtraAttr("cycleState", cycleState);
       if ("1".equals(cycleState) && Strings.isNotBlank((String)baseInfo.get("cycleSender"))) {
           summary.putExtraAttr("cycleSender", (String)baseInfo.get("cycleSender"));
           summary.putExtraAttr("cycleStartDate", (String)baseInfo.get("cycleStartDate"));
           summary.putExtraAttr("cycleEndDate", (String)baseInfo.get("cycleEndDate"));
           summary.putExtraAttr("cycleType", (String)baseInfo.get("cycleType"));
           summary.putExtraAttr("cycleMonth", (String)baseInfo.get("cycleMonth"));
           summary.putExtraAttr("cycleOrder", (String)baseInfo.get("cycleOrder"));
           summary.putExtraAttr("cycleDay", (String)baseInfo.get("cycleDay"));
           summary.putExtraAttr("cycleWeek", (String)baseInfo.get("cycleWeek"));
           summary.putExtraAttr("cycleHour", (String)baseInfo.get("cycleHour"));
       } else {
           summary.putExtraAttr("cycleSender", "");
           summary.putExtraAttr("cycleStartDate", "");
           summary.putExtraAttr("cycleEndDate", "");
           summary.putExtraAttr("cycleType", "");
           summary.putExtraAttr("cycleMonth", "");
           summary.putExtraAttr("cycleOrder", "");
           summary.putExtraAttr("cycleDay", "");
           summary.putExtraAttr("cycleWeek", "");
           summary.putExtraAttr("cycleHour", "");
           summary.putExtraAttr("cycleState", "0");
       }


       Map<String, String> superviseMap = ParamUtil.getJsonDomain("superviseDiv");
       String superviseStr = XMLCoder.encoder(superviseMap);
       c.putExtraAttr("superviseStr", superviseStr);
       String replaceSubject = summary.getSubject() == null ? null : summary.getSubject().replaceAll(new String(new char[]{(char)160}), " ");
       summary.setSubject(replaceSubject);
       c.setSubject(replaceSubject);
       c.setSummary(XMLCoder.encoder(summary));
       c.putExtraAttr("summary", summary);
	   c.putExtraAttr("cap4Flag",true);


       //授权
       String auth = (String)baseInfo.get("auth");
       List<CtpTemplateAuth> authList = new ArrayList<CtpTemplateAuth>();
       if (auth != null && Strings.isNotBlank(auth)) {
           String[] authObj = auth.split(",");
           CtpTemplateAuth templateAuth = null;
           int sort = 1;
           for (String a : authObj) {
               templateAuth = new CtpTemplateAuth();
               String[] authSp = a.split("\\|");
               templateAuth.setId(UUIDLong.longUUID());
               templateAuth.setAuthId(Long.parseLong(authSp[1]));
               templateAuth.setAuthType(authSp[0]);
               templateAuth.setModuleId(c.getId());
               templateAuth.setCreateDate(new Date());
               templateAuth.setModuleType(moduleType);
               templateAuth.setSort(sort++);
               authList.add(templateAuth);
           }
       }
       c.putExtraAttr("authList", authList);



       try {
   			List<Attachment> atts  = attachmentManager.getAttachmentsFromRequest(ApplicationCategoryEnum.collaboration, c.getId(),
   			           c.getId(), AppContext.getRawRequest());
   			if(Strings.isNotEmpty(atts)){
   				setHasAttachments(c,true);
   			}
   			c.putExtraAttr("attachmentList", atts);
   	    } catch (Exception e) {
   			LOG.error("", e);
   		}

       if(AppContext.hasPlugin("ai")){//有AI插件
    	   setProcessMonitorInfo(baseInfo, c);
       }


        Long newWorkflowId = null;

        if (cap4fb.getBind().getExtraAttr("batchId") == null) {
        	LOG.error("流程模板依据 batchId 值为空！模板保存异常！");
        }

		if (templateIsNew || isVersionIncrement) {// 新建-新建一个流程给模板和模板历史使用

			//如果新增的时候processXml 是空的话就从原来的流程中拷贝
			if(Strings.isEmpty(processXml)){
				ProcessTemplete tt = wapi.selectProcessTemplateById(c.getWorkflowId());
				if(tt != null ){
					processXml = tt.getWorkflow();
				}
			}
			//如果是第一次新建模板id已经生成了，不用再次创建
			if(Strings.isNotBlank(workflowId) && templateIsNew) {
				newWorkflowId = Long.valueOf(workflowId); 
			}else {
				newWorkflowId = UUIDLong.longUUID();
			}
			wapi.insertWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting,
					workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId), Long.parseLong(fbatchId),
					processEventJson,newWorkflowId);
			

			c.setCreateDate(new Date());
			c.setModifyDate(new Date());
			c.setTemplateId(template.getId());


		} else {


			Long oldProcessId = c.getWorkflowId();

			c.setModifyMember(AppContext.getCurrentUser().getId());


			newWorkflowId = wapi.updateWorkflowTemplate("" + moduleType, processName, processXml, subProcessSetting,
					workflowRule, "" + AppContext.currentUserId(), Long.valueOf(defId), Long.parseLong(fbatchId),
					oldProcessId, processEventJson);
			
			if (c.getExtraAttr("oldProId") == null) {
				c.putExtraAttr("oldProId", oldProcessId);// 用于删除流程模板的
			}

		}
		c.setWorkflowId(newWorkflowId);
		c.setDelete(false);
	    c.setSystem(true);
	        
        Long orginalTemplateWorkFlowId = (Long) template.getExtraAttr("orginalTemplateWorkFlowId");
        if(orginalTemplateWorkFlowId == null || Long.valueOf(-1).equals(orginalTemplateWorkFlowId) ){ 
        	//为空再添加，确保一次完成的流程编辑保存操作，只添加一次，否则重复添加多次还是有问题。
        	template.putExtraAttr("orginalTemplateWorkFlowId", template.getWorkflowId() == null ? -1 : template.getWorkflowId());
        }

		String s = (String) baseInfo.get("updateProcessDesFlag");
		c.putExtraAttr("updateProcessDesFlag", s);
		
		

        Integer oldVersion = null;
        HashMap<String,String> versionMap = (HashMap<String,String>)template.getExtraMap().get("versionMap");
        if(versionMap != null) {
            if (versionMap.containsKey("version") && Strings.isNotEmpty(versionMap.get("version"))) {
                oldVersion = Integer.parseInt(versionMap.get("version"));
            }
        }
        
        Long bakId = template.getId();//id不改变
        c.cloneProperty2ThsTemplate(template);
        template.setId(bakId);
        //最后设置更新了的history
        template.putExtraAttr("templateHistory", c);
        if(oldVersion  != null){
            if(oldVersion.equals(template.getVersion())){
                template.setVersion(null);
            }else{
                template.setVersion(oldVersion);
            }
	        if(((Integer)Approve.ApproveType.toBeReleased.key()).equals(c.getSubstate())){
		        template.setToBeReleasedVersion("V"+c.getVersion()+".0");
	        }
	        if(((Integer)Approve.ApproveType.inReview.key()).equals(c.getSubstate())){
		        template.setApproveVersion("V"+c.getVersion()+".0");
	        }
	        if(((Integer)Approve.ApproveType.haveReleased.key()).equals(c.getSubstate())){
		        template.setVersion(c.getVersion());
		        template.setToBeReleasedVersion("");
		        template.setApproveVersion("");
		        
	        }else{
	        	if(versionMap != null  && !template.getApproveVersion().equals( versionMap.get("toBeReleasedVersion")) && ((Integer)Approve.ApproveType.inReview.key()).equals(c.getSubstate())){
			        template.setToBeReleasedVersion(versionMap.get("toBeReleasedVersion"));
		        }
	        }
        }else{
        	if(((Integer)Approve.ApproveType.toBeReleased.key()).equals(c.getSubstate())){
		        template.setToBeReleasedVersion("V"+c.getVersion()+".0");
	        }
	        if(((Integer)Approve.ApproveType.inReview.key()).equals(c.getSubstate())){
		        template.setApproveVersion("V"+c.getVersion()+".0");
	        }
            if(Strings.isNotEmpty(template.getApproveVersion()) || Strings.isNotEmpty(template.getToBeReleasedVersion())){
                template.setVersion(null);
            }
        }
        if(versionMap != null){
	        template.getExtraMap().put("versionMap",versionMap);
        }
        
		cap4fb.getBind().addFlowTemplate(template);

		listMap.put("frombindlist",cap4fb.getBind().getFlowTemplateList());

        return listMap;
	}

	private CtpTemplateHistory convertCtpTemplateHistory(Map baseInfo, Long templateHistoryId, CtpTemplate template)
			throws BusinessException {

		Long historyId = templateHistoryId;

		CtpTemplateHistory exsitHistory = (CtpTemplateHistory) template.getExtraAttr("templateHistory");

        if(exsitHistory != null){
        	exsitHistory.putExtraAttr("isFromCache","true");
        }

        if(exsitHistory == null && templateHistoryId != null){
        	CtpTemplateHistory exsitHistoryDB = templateManager.getCtpTemplateHistory(templateHistoryId);
        	exsitHistory = exsitHistoryDB.clone();
        }

        if(exsitHistory == null){
        	 exsitHistory = new CtpTemplateHistory();
        	 historyId = UUIDLong.longUUID();
        }

        CtpTemplateHistory c = (CtpTemplateHistory)ParamUtil.mapToBean(baseInfo,exsitHistory,false);


        c.setId(historyId);
        c.setTemplateId(template.getId());

    	//c.putExtraAttr("isFromNew","true");

		return c;
	}

	private boolean computeTemplateVersion(CtpTemplate template,CtpTemplateHistory history,
        Map<String, Object> approveInfo) throws BusinessException {
		history.setDelete(false);
		history.setSystem(true);
		String auditor = (String) approveInfo.get("approve_auditors");// 审批人
		String remark = (String) approveInfo.get("approve_remark");// 备注
		Boolean isApprove = "0".equals(approveInfo.get("approve_isApprove")) ? false : true;// 是否审批
		Boolean isNowRelease = "1".equals(approveInfo.get("approve_nowRelease")) ? true : false;// 是否立即发布
		Boolean versionFlag = "1".equals(approveInfo.get("approve_versionFlag")) ? true : false;// 是否新增版本号
		Date publicTime = Datetimes.parse((String) approveInfo.get("approve_releaseTime"));// 审批时间
		if(isNowRelease){
			publicTime = null;//如果立即发布时间为空
		}
        if(history.getId() == null){
            history.setNewId();
        }
		if(history.getVersion() == null){
			history.setVersion(1);
		}
		if(versionFlag){
            history.setNewId();
			history.setVersion(history.getVersion() +1);
		}
		history.setPublishTime(publicTime);

		if (isApprove) {
            
			//3、修改模板有审批人，刷新审批中版本
            template.setApproveVersion("V"+String.valueOf(history.getVersion())+".0");
			history.setSubstate(Approve.ApproveType.inReview.key());
			history.setState(State.invalidation.ordinal());//审批中的就是不可用

		} else {
			if (publicTime == null || publicTime.before(new Date())) {
                
				// 1、修改模板直接发布-无发布时间，刷新当前使用版本
				history.setSubstate(Approve.ApproveType.haveReleased.key());
                template.setVersion(history.getVersion());
				history.setState(State.invalidation.ordinal());//未到时间不可用
			} else {
                
				// 2、修改模板直接发布-有发布时间，刷新待发布版本
				history.setSubstate(Approve.ApproveType.toBeReleased.key());
                template.setToBeReleasedVersion("V"+history.getVersion()+".0");

			}
		}
		TemplateApprovePO approve = templateToTemplateApprove(history);
		approve.setRemark(remark);
		approve.setAuditor(auditor);
		approve.setApprove(isApprove);
		approve.setReleaseTime(publicTime);
		history.putExtraAttr("templateApprovePO", approve);

		return versionFlag;
	}


	public TemplateApprovePO templateToTemplateApprove(CtpTemplateHistory history){

		TemplateApprovePO templateApprovePO = new TemplateApprovePO();

		templateApprovePO.setIsDelete(Approve.ISDelete.invalidData.key());
		templateApprovePO.setTemplateID(history.getId());// 模板ID
		templateApprovePO.setType(history.getSubstate());//
		templateApprovePO.setApproveName(history.getSubject());// 审批模板名称
		templateApprovePO.setProcessID(history.getWorkflowId());// 流程ID
		templateApprovePO.setVersion(history.getVersion());// 版本号
		templateApprovePO.setCreateDate(DateUtil.newDate());
		templateApprovePO.setCreater(AppContext.currentUserId());
		templateApprovePO.setNewId();
		templateApprovePO.setApprove(false);//默认不 审核
		templateApprovePO.setReleaseTime(history.getPublishTime()); // 发布时间

		return templateApprovePO;

	}
	/**
	 * 设置流程监控和自动处理条件
	 * @param baseInfo
	 * @param c
	 */
	private void setProcessMonitorInfo(Map baseInfo, CtpTemplateHistory c) {
		c.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_MONITOR_CAN_SEND_MSG, (String) baseInfo.get("processMonitorInput"));
		c.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_PROCESS_MONITOR, (String)baseInfo.get("monitorArray"));
        c.putExtraAttr(FormConstant.FLOW_TEMPLATE_EXT_AI_PROCESSING_CONDITION,(String)baseInfo.get("autoDealConditionVal"));
	}
    private void setHasAttachments(CtpTemplateHistory template,boolean hasAttachments) {
    	if(Strings.isBlank(template.getIdentifier())){
    		template.setIdentifier(IdentifierUtil.newIdentifier(template.getIdentifier(), 20,
                    '0'));
        }
        template.setIdentifier(IdentifierUtil.update(template.getIdentifier(), TemplateEnum.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1'));
    }



	public void saveFlowTemplate2DB(FormTemplateSaveAllParam saveAllParams) throws BusinessException {
		if (saveAllParams == null) {
			return;
		}
		State state = saveAllParams.getState(); // 表单是否可用
		Long memberId = saveAllParams.getMemberId(); // 表单所属人
		Long ownerAccountId = saveAllParams.getOwnerAccountId(); // 表单所属单位
		Long batchId = saveAllParams.getBatchId();// 是否修改了表单模板
		Long formAppId = (Long) saveAllParams.getFormAppId(); // 表单ID
		String formName = (String) saveAllParams.getFormName(); // 表单名称
		List<CtpTemplate> newTemplateList = saveAllParams.getNewTemplateList();

		List<CtpTemplate> oldTemplateList = formApi4Cap3.getFormSystemTemplate(formAppId);

		Map<Long, CtpTemplate> dbTemplateMap = new HashMap<Long, CtpTemplate>();
		Map<Long, CtpTemplate> newTemplateMap = new HashMap<Long, CtpTemplate>();

		if (Strings.isNotEmpty(oldTemplateList)) {
			for (CtpTemplate t : oldTemplateList) {
				dbTemplateMap.put(t.getId(), t);
			}
		}

		if (Strings.isNotEmpty(newTemplateList)) {
			for (CtpTemplate t : newTemplateList) {
				newTemplateMap.put(t.getId(), t);
			}
		}

		if (batchId == null) { // batchId表示有没有修改过模板，is null表示没有修改过模板
			if (Strings.isNotEmpty(oldTemplateList)) {
				for (CtpTemplate c : oldTemplateList) {
					c.setMemberId(memberId);
					if (ownerAccountId != null) {
						c.setOrgAccountId(ownerAccountId);
					}
					if (state.equals(State.normal)) {// 保存并发布
						if (!(null != c.getPublishTime() && c.getPublishTime().after(new Date()))) {
							c.setState(state.ordinal());
						}
					} else {
						c.setState(state.ordinal());
					}
					templateManager.updateCtpTemplate(c);
				}
			}
			return;
		}

		// 更新表单相关的属性
		if (Strings.isNotEmpty(newTemplateList)) {
			for (CtpTemplate t : newTemplateList) {
				t.setMemberId(memberId);
				if (ownerAccountId != null) {
					t.setOrgAccountId(ownerAccountId);
				}
				if (state.equals(State.normal)) {// 保存并发布
					if (!(null != t.getPublishTime() && t.getPublishTime().after(new Date()))) {
						t.setState(state.ordinal());
					}
				} else {
					t.setState(state.ordinal());
				}
			}
		}

		// 删除模板（加上删除状态）
		List<CtpTemplate> needDelTemplates = new ArrayList<CtpTemplate>();
		// 更新模板（同时更新流程，授权）
		List<CtpTemplate> needUpdateTemplates = new ArrayList<CtpTemplate>();
		// 保存模板
		List<CtpTemplate> needSaveTemplates = new ArrayList<CtpTemplate>();

		for (CtpTemplate oldTemplate : oldTemplateList) {
			DBAgent.evict(oldTemplate);
			CtpTemplate newTemplate = newTemplateMap.get(oldTemplate.getId());
			if (newTemplate != null) {
				newTemplate.putExtraAttr("isTemplateNew", false);
				needUpdateTemplates.add(newTemplate);
			} else {
				needDelTemplates.add(oldTemplate);
			}
		}

		for (CtpTemplate newTemplate : newTemplateList) {
			CtpTemplate dbTemplate = dbTemplateMap.get(newTemplate.getId());
			if (dbTemplate == null) {
				newTemplate.putExtraAttr("isTemplateNew", true);
				needSaveTemplates.add(newTemplate);
			}
		}
		ArrayList<Long> needDelList = new ArrayList<Long>();
		User user = AppContext.getCurrentUser();
		// 更新删除状态
		for (CtpTemplate c : needDelTemplates) {

			c.setDelete(true);
			c.setTempleteNumber(null);
			templateManager.updateCtpTemplate(c);
			// 删除RACI
			templateManager.deleteCtpTemplateOrgByTemplateId(c.getId());
			// 删除自动发起定时任务
			newFormBindQuartzJob(c);
			deleteCtpTempleteHistoryByTemplateId(c.getId());
			// 删除流程智能监控配置
			if (CDPAgent.isEnabled() && AppContext.hasPlugin("ai")) {
				aiApi.delMonitorSetByTemplateId(c.getId());
			}
			// 删除自动处理条件配置
			if (AppContext.hasPlugin("ai")) {
				aiApi.delAutoDealSetByTemplateId(c.getId());
			}
			//删除对应的个人模板信息
			needDelList.add(c.getId());
			
			appLogManager.insertLog(user, 183, OrgHelper.showMemberName(memberId) , c.getSubject());
			
		}
		if(needDelTemplates != null && needDelTemplates.size() >0){
			templateManager.deleteCtpTemplateByTemplateIds(needDelList);
		}

		
		Map<Long,ProcessTemplete> ptMap = new HashMap<Long,ProcessTemplete>();
		if (batchId != null) { // 编辑或者新建了模板

			List<ProcessTemplete> pts = wapi.saveWorkflowTemplates(batchId);

			if (Strings.isNotEmpty(pts)) {
				for (ProcessTemplete pt : pts) {
					
					ptMap.put(pt.getId(), pt);
					updatePermissinRef(ModuleType.collaboration, pt.getWorkflow(), "-1", "-1",
							AppContext.currentAccountId());
					updateMessageRuleRef( pt.getWorkflow(), "-1", "-1");
					updateDealRef( pt.getWorkflow(), "-1", "-1");
				}
			}
		}

		// 触发模板保存的事件 ,特别注意：事件的代码需要放在保存 更新对象之前
		List<CtpTemplate> allEventTemplates = new ArrayList<CtpTemplate>();
		allEventTemplates.addAll(needSaveTemplates);
		allEventTemplates.addAll(needUpdateTemplates);

		if (Strings.isNotEmpty(allEventTemplates)) {

			for (CtpTemplate t : allEventTemplates) {

				CtpTemplateHistory history = (CtpTemplateHistory) t.getExtraAttr("templateHistory");

				if (history != null) { // 应用绑定的部分模板没有修改，就不会进入，故需要判断history
					// 无时间不审核在最后去设置为1，这种在新增和修改都存在

					if (state.equals(State.normal)) {
						history.setState(0);//
					} else {
						history.setState(1);
					}
					//兼容存在模板审批不通过的状态，导致的没有版本号
					if(t.getVersion() == null && history.getVersion() != null){
						t.setVersion(history.getVersion());
					}

					Long oldProId = (Long) history.getExtraAttr("oldProId");
					if (oldProId != null) {
						history.setWorkflowId(oldProId);
					}
					Boolean isTemplateNew = (Boolean) t.getExtraAttr("isTemplateNew");
					if (isTemplateNew) {
						saveTemplateHistory2DB(history, formAppId);
						appLogManager.insertLog(user, 182, OrgHelper.showMemberName(memberId) , t.getSubject());
					} else {
						updateTemplateHistory2DB(history, t, formAppId);
						appLogManager.insertLog(user, 184, OrgHelper.showMemberName(memberId) , t.getSubject());
					}

					// 事件触发
					TemplateSaveEvent templateSaveEvent = new TemplateSaveEvent(t,
							(List<CtpTemplateAuth>) t.getExtraAttr("authList"),ptMap.get(t.getWorkflowId()));
					templateSaveEvent.setHistory(history);
					try {
						EventDispatcher.fireEventWithException(templateSaveEvent);
					} catch (Throwable e) {
						throw new BusinessException(e);
					}

					if (isTemplateNew != null) {
                        saveTemplateApprove(t, dbTemplateMap.get(t.getId()), isTemplateNew);
					}
				}
				
				// 检查超级节点，如果有超级节点调用CIP的接口进行相关业务处理
				if(cipApi != null) {
				    Long processId = t.getWorkflowId();
				    BPMProcess process = wapi.getTemplateProcess(processId);
				    if(process != null) {
				        List<String> superNodeIds = new ArrayList<String>();
				        List<BPMActivity> activities = process.getActivitiesList();
			            for(BPMActivity activity : activities) {
			                List<BPMActor> actorList = activity.getActorList();
			                if(Strings.isEmpty(actorList)) {
			                    continue;
			                }
			                for(BPMActor actor : actorList) {
			                    BPMParticipant participant = actor.getParty();
			                    BPMParticipantType type = participant.getType();
			                    if("WF_SUPER_NODE".equals(type.id)) {
			                        superNodeIds.add(participant.getId());
			                    }
			                }
			            }
			            if(Strings.isNotEmpty(superNodeIds)) {
			                // 调用CPI接口创建业务数据
			                cipApi.createBusinessFlowEvent(t, superNodeIds);
			            }
				    }
				}
			}
		}
		
		// 增加日志
		// if (needSaveTemplates.size() > 0 || needUpdateTemplates.size() > 0) {
		// appLogManager.insertLog(AppContext.getCurrentUser(),
		// AppLogAction.Form_ChangeAuth, AppContext.currentUserName(),formName);
		// }

	}

	private void deleteCtpTempleteHistoryByTemplateId(Long templateId) throws  BusinessException{
		Map<String, String> params = new HashMap<String, String>();
		params.put("templateId",String.valueOf(templateId));
		List<CtpTemplateHistory> list = templateManager.getCtpTemplateHistory(null,params);
		if(Strings.isNotEmpty(list)){
			//删除对应的catAffair事项和Approve 和Appproves记录
			for(CtpTemplateHistory ctpTemplateHistory: list){
				affairManager.deletePhysicalByTemplateId(ctpTemplateHistory.getId());
				templateManager.deleteCtpTempleteHistoryByTemplateId(ctpTemplateHistory.getId());//删除历史表记录
			}

		}
	}

    private void updateTemplateHistory2DB(CtpTemplateHistory c,CtpTemplate template,Long formAppId) throws BusinessException{
        //更新模板表
        saveOrUpdateCptContentAllHistroy(c, false, formAppId);
        //删除再增加RACI
        templateManager.deleteCtpTemplateOrgByTemplateId(c.getId());
        templateManager.saveCtpTemplateOrgs(templateManager.bulidCtpTemplateOrgListHistory(c));

        //督办
        String superviseStr = (String) c.getExtraAttr("superviseStr");
        if (Strings.isNotBlank(superviseStr)) {

        	Map<String, Object> map = new HashMap<String, Object>();
            map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
            savSupervise(map, c.getId(), false, false);
        }

        //更新授权
        if (c.getExtraAttr("authList") != null) {
            templateManager.deleteCtpTemplateAuths(c.getId(), c.getModuleType());
            templateManager.saveCtpTemplateAuths((List<CtpTemplateAuth>) c.getExtraAttr("authList"));
        }

        if (c.getExtraAttr("relationAuthList") != null) {
            //删除关联表单授权
            formApi4Cap3.deleteCtpTemplateRelationAuths(c.getId());
            //更新关联表单授权
            formApi4Cap3.saveCtpTemplateRelationAuths((List<CtpTemplateRelationAuth>) c.getExtraAttr("relationAuthList"));
        }

        if(AppContext.hasPlugin("ai") && aiApi != null){//有AI插件，保存流程监控信息
        	aiApi.saveOrUpdateProcessMonitor(c);
        }

        Object extraAttr = c.getExtraAttr("updateProcessDesFlag");

        List<Attachment> atts =  (List<Attachment>) c.getExtraAttr("attachmentList");
        attachmentManager.deleteOnlyAttByReference(c.getId());
        if(Strings.isNotEmpty(atts)){
    	     attachmentManager.create(atts);
             setHasAttachments(c, true);
        }


        //TODO mujTempalte
      /*  CtpTemplate oldTemplate = dbTemplateMap.get(c.getId());
        boolean isNeed =  templateManager.needRecordAppLog(c, oldTemplate);
        if((null != extraAttr && "1".equals(extraAttr)) || isNeed ){
        	appLogManager.insertLog(AppContext.getCurrentUser(), 108, AppContext.currentUserName(),c.getSubject());
        }*/

    }


    private void saveTemplateHistory2DB(CtpTemplateHistory history,Long formAppId) throws BusinessException{

    	history.setVersion(1);
    	history.setDelete(false);
        templateManager.saveCtpTemplateHistory(history);

        //更新模板表
        saveOrUpdateCptContentAllHistroy(history, true, formAppId);


        if(null != history.getProcessLevel()){
        	enumManagerNew.updateEnumItemRef("cap_process_leavel", history.getProcessLevel().toString());
        }

        templateManager.saveCtpTemplateOrgs(templateManager.bulidCtpTemplateOrgListHistory(history));


        String superviseStr = (String) history.getExtraAttr("superviseStr");
        Map<String, Object> map = new HashMap<String, Object>();
        map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
        savSupervise(map, history.getId(), true, false);

        //更新授权
        List<CtpTemplateAuth> auths = (List<CtpTemplateAuth>) history.getExtraAttr("authList");
        if(Strings.isNotEmpty(auths)){
        	templateManager.saveCtpTemplateAuths((List<CtpTemplateAuth>) history.getExtraAttr("authList"));
        }

        //更新关联表单授权
        if( history.getExtraAttr("relationAuthList") != null ){
            formApi4Cap3.saveCtpTemplateRelationAuths((List<CtpTemplateRelationAuth>) history.getExtraAttr("relationAuthList"));
        }


        List<Attachment> atts =  (List<Attachment>) history.getExtraAttr("attachmentList");
        attachmentManager.deleteOnlyAttByReference(history.getId());
        if(Strings.isNotEmpty(atts)){
        	attachmentManager.create(atts);
             setHasAttachments(history, true);
        }

        if(AppContext.hasPlugin("ai") && aiApi!= null){//有AI插件，保存流程监控信息
        	aiApi.saveOrUpdateProcessMonitor(history);
        }
    }





    /**
     *
     * @param template
     * @param isWholeTemplateNew  整个模板是新增
     * @throws BusinessException
     */
    //发送消息给相关审批人，开始审批
    private void saveTemplateApprove(CtpTemplate template, CtpTemplate dbTemplate, boolean isWholeTemplateNew) throws BusinessException{


    	CtpTemplateHistory history = (CtpTemplateHistory) template.getExtraAttr("templateHistory");

    	TemplateApprovePO  templateApprovePO = (TemplateApprovePO) history.getExtraAttr("templateApprovePO");



		templateApprovePO.setIsDelete(Approve.ISDelete.validDate.key());

		boolean isNeedFreshHistoryToTemplate =  false;

		
		boolean isReleased = Integer.valueOf(Approve.ApproveType.haveReleased.key()).equals(Integer.valueOf(templateApprovePO.getType()));
		
		if (isWholeTemplateNew) {
			templateApproveManager.saveTemplateApprove(templateApprovePO);
			// 保存具体审批人的审批记录并且发消息
			saveTemplateApproveRecords(templateApprovePO);

		} else { // 修改
			templateApproveManager.deleteTemplateApproveByHistoryId(history.getId());
			templateApproveManager.saveOrUpdateTemplateApprove(templateApprovePO);

			
			 //无时间不审核才更新为正式使用状态-前提是表单正常
			if (Integer.valueOf( TemplateEnum.State.normal.ordinal()).equals(history.getState()) && !isReleased) {
				history.setState(TemplateEnum.State.invalidation.ordinal());
			}
			if(isReleased) {
				history.setState(TemplateEnum.State.normal.ordinal());
			}
			
			templateManager.saveOrUpdateTempleteHistory(history);

			//版本相同需要覆盖
			if(dbTemplate != null && dbTemplate.getVersion() != null && dbTemplate.getVersion().equals(history.getVersion())) {
				isNeedFreshHistoryToTemplate = true;
			}

			// 保存具体审批人的审批记录并且发消息
			saveTemplateApproveRecords(templateApprovePO);

		}


	
		boolean isVersion1 = Integer.valueOf(1).equals(Integer.valueOf(history.getVersion()));

		if(!isNeedFreshHistoryToTemplate) {
			isNeedFreshHistoryToTemplate = isReleased || isVersion1 ; //是否需要将history中的数据刷新到Template
		}

		// 已经发布，立即生效。
		if (isNeedFreshHistoryToTemplate) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("templateId",String.valueOf(template.getId()));
			params.put("substate",Approve.ApproveType.haveReleased.getKey()+","+Approve.ApproveType.toBeReleased.getKey());
			params.put("notin_id",String.valueOf(history.getId()));
			List<CtpTemplateHistory> list =  templateManager.getCtpTemplateHistory(null,params);
			List<Long> approveIds = new ArrayList<Long>();
			for(CtpTemplateHistory history1 : list){
				history1.setSubstate(Approve.ApproveType.invalid.getKey());
				templateManager.updateTempleteHistory(history1);
				approveIds.add(history1.getId());
			}
			//查询以前发布的模板审批记录然后更新
			if(approveIds.size() >0){
				HashMap<String,Object> parmas = new HashMap<String, Object>();
				parmas.put("type",Approve.ApproveType.invalid.getKey());
				parmas.put("ids",approveIds);
				templateApproveManager.updateTemplateByParam(parmas);
			}
			//如果立即发布但是在审核中还是不可以使用
			if(templateApprovePO != null && !templateApprovePO.getType().equals(new Integer(Approve.ApproveType.haveReleased.getKey()))){
				history.setState(State.invalidation.ordinal());
			}
			//如果是立即发布但是表单不可以使用就还是不能使用
			if(!capFormManager.isEnabled(history.getFormAppId())){
				template.setState(State.invalidation.ordinal());
				history.setState(State.invalidation.ordinal());
			}
			cloneAndSaveTemplateHistoryToTemplate(template,history,isWholeTemplateNew,true);

		}else{
		    //不是立即发布的话并且版本没有发生改变将原来的模板设置为停用状态
			if(template.getVersion() == null){
				template.setVersion(history.getVersion());
			}
            if(template.getVersion().equals(history.getVersion())){
                template.setState(State.invalidation.ordinal());
                templateManager.updateCtpTemplate(template);
            }
        }

		// 待发布延迟生效
		if (Integer.valueOf(Approve.ApproveType.toBeReleased.key()).equals(Integer.valueOf(templateApprovePO.getType()))) {
			templateManager.createTemplatePublishJob(history);
			//-如果模板存在和自己版本·
			//只要是修改都需要立即修改待发布的数据为-已失效
			Map<String, String> params = new HashMap<String, String>();
			params.put("templateId",String.valueOf(template.getId()));
			params.put("substate",Approve.ApproveType.toBeReleased.getKey()+",");
			params.put("notin_id",String.valueOf(history.getId()));
			List<CtpTemplateHistory> list =  templateManager.getCtpTemplateHistory(null,params);
			List<Long> approveIds = new ArrayList<Long>();
			for(CtpTemplateHistory history1 : list){
				if(Approve.ApproveType.toBeReleased.getKey() == history1.getSubstate() ){
					String _jobName = "TemplatePublishTimeQuartzJob" + history1.getId();
					QuartzHolder.deleteQuartzJob(_jobName);
					approveIds.add(history1.getId());//待发布的都设置为已废弃
				}
				history1.setSubstate(Approve.ApproveType.invalid.getKey());
				templateManager.updateTempleteHistory(history1);
			}
			//查询以前发布的模板审批记录然后更新
			if(approveIds.size() >0){
				HashMap<String,Object> parmas = new HashMap<String, Object>();
				parmas.put("type",Approve.ApproveType.invalid.getKey());
				parmas.put("ids",approveIds);
				templateApproveManager.updateTemplateByParam(parmas);
			}
		}

		if(isReleased){
			newFormBindQuartzJob(template);
		}

   }
	//设置其它的待发布为已失效数据
	private void modifyCtpHistoryTonvalid(CtpTemplateHistory ctpTemplateHistory,CtpTemplate template) throws BusinessException{
		//版本不相等 立即更新其它的待办和已办事项
		if(!ctpTemplateHistory.getVersion().equals(template.getVersion()) ){
			//历史模板生效后如果-存在老的已办和待办都修改为
			HashMap<String,String> parmas = new HashMap<String, String>();
			parmas.put("templateId",String.valueOf(template.getId()));
			parmas.put("substate",String.valueOf(Approve.ApproveType.toBeReleased.getKey()));
			List<CtpTemplateHistory> historyList = templateManager.getCtpTemplateHistory(null,parmas);
			if(historyList != null && historyList.size() >0) {
				for (Iterator<CtpTemplateHistory> iterator = historyList.iterator(); iterator.hasNext();) {
					CtpTemplateHistory history = iterator.next();
					if(history.getId().equals(ctpTemplateHistory.getId())){
						historyList.remove(history);
					}
					//待发布 的删除派发的job
					if (Approve.ApproveType.toBeReleased.getKey() == ctpTemplateHistory.getSubstate()) {
						String _jobName = "TemplatePublishTimeQuartzJob" + ctpTemplateHistory.getId();
						QuartzHolder.deleteQuartzJob(_jobName);
					}
					history.setSubstate(Approve.ApproveType.invalid.getKey());
					templateManager.updateTempleteHistory(history);
				}
			}
		}
	}


	public void cancleTempleteApprove(Long approveId)throws BusinessException{
		TemplateApprovePO po =   templateApproveManager.getById(approveId);
		 affairManager.deleteByObjectId(ApplicationCategoryEnum.templateApprove,po.getId());
    	//撤销的话1-删除CtpAffair；
		templateApproveManager.deleteTemplateApproveByHistoryId(po.getTemplateID());
		//2删除对应的ApproveDetails 和 approve

		//删除history 的关联信息 更新templateHistory 为草稿

	}



	/**
	 * 将模板的历史中的数据更新到模板表中
	 * @param template  模板
	 * @param history   模板历史表
	 * @param isWholeTemplateNew  是否是整个模板新建
	 * @param isTemplateEditState 是否是模板编辑页面保存，编辑页面保存某些数据直接从页面获取即可，否则如定时任务需要从数据库中获取
	 * @throws BusinessException
	 */
    public void cloneAndSaveTemplateHistoryToTemplate(CtpTemplate template,CtpTemplateHistory history,
    		boolean isWholeTemplateNew,boolean isTemplateEditState) throws BusinessException{

    	Long orginalTemplateWorkFlowId = (Long) template.getExtraAttr("orginalTemplateWorkFlowId");
    	if(orginalTemplateWorkFlowId == null || Long.valueOf(-1).equals(orginalTemplateWorkFlowId)){
    		orginalTemplateWorkFlowId  = template.getWorkflowId();
    	}
    	Long templateId = template.getId();
    	BeanUtils.convert(template, history);
    	template.setId(templateId);


        //更新授权
        List<CtpTemplateAuth> templateAuths = (List<CtpTemplateAuth>) history.getExtraAttr("authList");
        if(!isTemplateEditState){
        	templateManager.deleteCtpTemplateOrgByTemplateId(template.getId());
        	templateAuths = templateManager.getCtpTemplateAuths(history.getId(), history.getModuleType());
        }
		cloneAndsaveTemplateAuth2ThsTemplate(template, templateAuths);
        
		// 删除再增加RACI
        List<CtpTemplateOrg> orgs = templateManager.bulidCtpTemplateOrgListHistory(history);
        saveRACI2ThsTemplate(template, orgs);

        if(isWholeTemplateNew){
        	//clone流程模板的数据
        	Long newProcesstTemplateId = wapi.cloneAndSaveProcessTemplate(String.valueOf(history.getWorkflowId()));
        	template.setWorkflowId(history.getWorkflowId());
    		template.setDelete(false);
        	templateManager.saveCtpTemplate(template);
        	
        	history.setWorkflowId(newProcesstTemplateId);
        	templateManager.saveOrUpdateTempleteHistory(history);
        	
    	}else{
    		
			/**
			 * 做一次兼容,避免CTP_TEMPLATE的WorkFlowId和CTP_TEMPLATE_HISTORY表的WORKFLOW_ID值相同。正式发版的时候可以删除
			 * */
			/*if(orginalTemplateWorkFlowId!=null && orginalTemplateWorkFlowId.equals(history.getWorkflowId())) {
				Long newProcesstTemplateId = wapi.cloneAndSaveProcessTemplate(String.valueOf(history.getWorkflowId()));
				history.setWorkflowId(newProcesstTemplateId);
				templateManager.saveOrUpdateTempleteHistory(history);		
			}*/
			/**
			 * 兼容END
			 */
    		
    		template.setWorkflowId(orginalTemplateWorkFlowId);
    		wapi.updateProcessTemplate(String.valueOf(history.getWorkflowId()), String.valueOf(orginalTemplateWorkFlowId));
    		template.setDelete(false);
    		templateManager.updateCtpTemplate(template);
    	}

        //更新模板表
        if(isWholeTemplateNew){
        	 saveOrUpdateCptContentAll(template, true, template.getFormAppId());
        }
        else{
        	 saveOrUpdateCptContentAll(template, false, template.getFormAppId());
        }


        //督办
        if(isTemplateEditState){
        	 String superviseStr = (String) history.getExtraAttr("superviseStr");
             if (Strings.isNotBlank(superviseStr)) {
            	 Map<String, Object> map = new HashMap<String, Object>();
                 map = (Map<String, Object>) XMLCoder.decoder(superviseStr);
                 savSupervise(map, template.getId(), false, false);
             }
        }
        else{
        	superviseManager.deleteAllInfoByTemplateId(templateId);
        	superviseManager.cloneAndSaveAllTemplateSuperviseInfo(history.getId(), template.getId());
        }



        //保存表单关联授权
        if(template.getFormAppId() != null){
        	  boolean isCAP4 = capFormManager.isCAP4Form(template.getFormAppId());
              if(!isCAP4 && isTemplateEditState){
          		List<CtpTemplateRelationAuth> relationAuths = (List<CtpTemplateRelationAuth>) history.getExtraAttr("relationAuthList");
          	    saveFormRelationAuth2ThsTemplate(template, relationAuths);
              }
        }


        //附件
        List<Attachment> atts =  (List<Attachment>) history.getExtraAttr("attachmentList");
        if(!isTemplateEditState){
        	atts = attachmentManager.getByReference(history.getId(),history.getId());
        }
        TemplateApproveUtil.cloneAndSaveAttachment(template, atts, attachmentManager);



        if(AppContext.hasPlugin("ai") && aiApi != null){//有AI插件，保存流程监控信息
        	if(isTemplateEditState){
        		TemplateApproveUtil.cloneTemplateHistoryAiPropertyToTemplate(template, history);
        		aiApi.saveOrUpdateProcessMonitor(template);
        	}
        	else{
        		aiApi.copyProcessMonitor(history.getId(), template.getId(), null, null, null, null);
        	}
        }




		List<DataRelationPO>  pos  =  dataRelationManager.findDataRelationPOsByProcessId(history.getWorkflowId());
		if(Strings.isNotEmpty(pos)){
			List<DataRelationPO> newPos = new ArrayList<DataRelationPO>();
			for(DataRelationPO po : pos){
				po.setProcessId(template.getWorkflowId());
				newPos.add(po);
			}
			dataRelationManager.updateDataRelationPOs(newPos);
		}

    }

	private void saveFormRelationAuth2ThsTemplate(CtpTemplate template, List<CtpTemplateRelationAuth> relationAuths)
			throws BusinessException {
	    formApi4Cap3.deleteCtpTemplateRelationAuths(template.getId());
        if (relationAuths != null) {
            //删除关联表单授权
            //更新关联表单授权
        	List<CtpTemplateRelationAuth> newAuths = new ArrayList<CtpTemplateRelationAuth>();
            if(Strings.isNotEmpty(relationAuths)){
            	for(CtpTemplateRelationAuth auth :relationAuths ){

            		try {
						CtpTemplateRelationAuth newAuth = (CtpTemplateRelationAuth) auth.clone();
                        newAuth.setNewId();
                        newAuth.setTemplateId(template.getId());
						newAuths.add(newAuth);
					} catch (CloneNotSupportedException e) {
						LOG.error("",e);
					}
            	}
            }
            formApi4Cap3.saveCtpTemplateRelationAuths(newAuths);
        }
	}

	private void saveRACI2ThsTemplate(CtpTemplate template, List<CtpTemplateOrg>  orgs) throws BusinessException {
		templateManager.deleteCtpTemplateOrgByTemplateId(template.getId());

        if(Strings.isNotEmpty(orgs)){
        	List<CtpTemplateOrg> newOrgs = new ArrayList<CtpTemplateOrg>();
        	for(CtpTemplateOrg org : orgs){
        		try {
					CtpTemplateOrg newOrg = (CtpTemplateOrg) org.clone();
					newOrg.setTemplateId(template.getId());
					newOrg.setNewId();
	        		newOrgs.add(newOrg);
				} catch (CloneNotSupportedException e) {
					LOG.error("",e);
				}
        	}
        	templateManager.saveCtpTemplateOrgs(newOrgs);
        }
	}

	private void cloneAndsaveTemplateAuth2ThsTemplate(CtpTemplate template, List<CtpTemplateAuth> templateAuths)throws BusinessException {

		templateManager.deleteCtpTemplateAuths(template.getId(),template.getModuleType());

		if (templateAuths != null && templateAuths.size() > 0) {
        	List<CtpTemplateAuth> newAuths = new ArrayList<CtpTemplateAuth>();
            if(Strings.isNotEmpty(templateAuths)){
            	for(CtpTemplateAuth auth :templateAuths){

            		CtpTemplateAuth newAuth = null;
					try {
						newAuth = (CtpTemplateAuth) auth.clone();
						newAuth.setModuleId(template.getId());
	            		newAuth.setNewId();
					} catch (CloneNotSupportedException e) {
						LOG.error("",e);
					}
            		newAuths.add(newAuth);
            	}
            }
            templateManager.saveCtpTemplateAuths(newAuths);
            templateManager.synchronizeTemplateAuthCache(template,newAuths);

        }else{
        	templateManager.synchronizeTemplateAuthCache(template,null);
//			//只是删除权限
//			ArrayList<Long> list = new ArrayList<Long>();
//			list.add(template.getId());
//			templateCacheManager.deleteCacheTemplateAuthByTemplateIds(list);
		}
	}



	private void saveTemplateApproveRecords(TemplateApprovePO templateApprovePO) throws BusinessException {
		if(Integer.valueOf(Approve.ApproveType.inReview.key()).equals(templateApprovePO.getType())){ //需要审核
		 	String handler = templateApprovePO.getAuditor();
		    String[] userArr = null;
		    if(handler.indexOf(",") > -1){
		        userArr = handler.split(",");
		    }else{
		        userArr = new String[]{handler};
		    }
		    ApproveRecordsPO approveRecordsPO = null ;
		    List<ApproveRecordsPO> listApprove = new ArrayList<ApproveRecordsPO>(userArr.length);
		    List<String> userLists = new ArrayList<String>(userArr.length);
		    Long  createUserId = templateApprovePO.getCreater();
		    for(String userId : userArr){
		        approveRecordsPO  = new ApproveRecordsPO();//采用拷贝但是必须是深拷贝
		        //创建审批记录
		        if(Strings.isNotEmpty(userId)){
		            approveRecordsPO.setApprover(Long.valueOf(userId));
		            approveRecordsPO.setTemplateApproveID(templateApprovePO.getId());
		            approveRecordsPO.setIsDelete(0);
		            approveRecordsPO.setNewId();
		            approveRecordsPO.setCreateDate(DateUtil.newDate());
		            approveRecordsPO.setCreater(createUserId);
		            listApprove.add(approveRecordsPO);
		        }
		        userLists.add(userId);
		    }
		    templateApproveManager.saveTemplateApproveList(listApprove);

		    sendTodoCtpAffair(templateApprovePO,listApprove);
		}
	}


    //发起流程审批人员的相关待办事项和消息
    private boolean  sendTodoCtpAffair(TemplateApprovePO to,List<ApproveRecordsPO> pos){
        boolean flag = false;
        List<CtpAffair>  list = getTodoListCtpAffair(to, pos);
        if(Strings.isNotEmpty(list)){
            //保存并发送消息
            try {
            	affairManager.saveAffairs(list);
                //并给处理人发送消息
                SendTempalteApproveMessage(list);
                flag = true;
            }catch (Exception e){
            	LOG.error("", e);
            }


        }

        return flag;
    }
    //获取将要保存的待发事项
    private List<CtpAffair> getTodoListCtpAffair(TemplateApprovePO to,List<ApproveRecordsPO> pos){
        List<CtpAffair >  list = new ArrayList<CtpAffair>();
            CtpAffair todoCtpAffair = null;
            list = new ArrayList<CtpAffair>(pos.size());
            Date createDate = DateUtil.newDate();
            int i = 0;
            for(ApproveRecordsPO approveRecordsPO: pos){
                Long  userId = approveRecordsPO.getApprover();
                todoCtpAffair = new CtpAffair();
                String subject = to.getApproveName()+  Datetimes.format(to.getCreateDate(), Datetimes.datetimeWithoutSecondStyle);
                todoCtpAffair.setSubject(subject);
                todoCtpAffair.setState(StateEnum.col_pending.key());//待办的事项

                todoCtpAffair.setNewId();
                todoCtpAffair.setSubState(SubStateEnum.col_pending_unRead.key());
                todoCtpAffair.setApp(ApplicationCategoryEnum.templateApprove.key());//模板审批
                todoCtpAffair.setSenderId(to.getCreater());
                todoCtpAffair.setMemberId(userId);
//                todoCtpAffair.setDelete(true);//先都是删除数据，生效后修改为有效数据
                todoCtpAffair.setObjectId(to.getId());
                todoCtpAffair.setCreateDate(createDate);
                todoCtpAffair.setSenderId(to.getCreater());
                //这里的模板id应该是历史表记录的Id
                todoCtpAffair.setTempleteId(to.getTemplateID());
                todoCtpAffair.setFromId(pos.get(i).getId());//设置审批记录ID
                todoCtpAffair.setReceiveTime(createDate);
                i ++;
                list.add(todoCtpAffair);
            }
        return  list;
    }
    private void SendTempalteApproveMessage(List<CtpAffair> list)throws BusinessException{
        if(Strings.isNotEmpty(list)){
            Set<MessageReceiver> receivers_new = new HashSet<MessageReceiver>();//
            String messagelink = "message.link.template.approve";//链接地址
            for(CtpAffair affair: list){
                receivers_new.add(new MessageReceiver(affair.getId(), affair.getMemberId(), messagelink,affair.getState(), affair.getObjectId(), affair.getTempleteId(),affair.getId(),affair.getFromId()));

            }
            Long userId = list.get(0).getSenderId();
            Integer importantLevel = 6;
            int forwardMemberFlag = 0;
            String forwardMember = null;//转发人
            int messageFlag = 0; //没有付言
            String repealComment = "";

            MessageContent traceContent = new MessageContent("collaboration.template.approve", list.get(0).getSubject(), forwardMemberFlag, forwardMember,messageFlag)
                    .setImportantLevel(importantLevel);

            userMessageManager.sendSystemMessage(traceContent,ApplicationCategoryEnum.templateApprove, userId, receivers_new, importantLevel);

        }

    }


    /**
     * 表单模板自动发起-定时任务生成
     * @throws BusinessException
     */
    public void newFormBindQuartzJob( CtpTemplate template) throws BusinessException {
        if (template == null) {
            return;
        }

        String formBindJobName = FormConstant.FORMBIND_JOBNAME + template.getId();
        String jobBeanId = FormConstant.FORMBIND_JOBBEANID;
        String jobGroupName = FormConstant.FORMBIND_GROUPNAME;

        boolean isCAP4 = capFormManager.isCAP4Form(template.getFormAppId());
        if(isCAP4){
        	jobBeanId  = com.seeyon.cap4.form.util.FormConstant.FORMBIND_JOBBEANID;
        }

		if (QuartzHolder.hasQuartzJob(jobGroupName, formBindJobName)) {
            QuartzHolder.deleteQuartzJobByGroupAndJobName(jobGroupName, formBindJobName);
        }

        ColSummary summary = null;
        if (template.getSummary() != null) {
			/*summary = edocApi.getColSummaryByEdocSummary(template.getSummary());*/
        	
        	summary = XMLCoder.decoder(template.getSummary(),ColSummary.class);
        }

        if (template.isDelete() != null && template.isDelete() || summary == null) {
            return;
        }

        String cycleState = String.valueOf(summary.getExtraAttr("cycleState"));
        if (!"1".equals(cycleState)) {//自动发起：无
            return;
        }

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("formId", String.valueOf(template.getFormAppId()));
        parameters.put("templateId", String.valueOf(template.getId()));
        parameters.put("cycleSender", String.valueOf(summary.getExtraAttr("cycleSender")));

        String cycleStartDate = String.valueOf(summary.getExtraAttr("cycleStartDate"));
        String cycleEndDate = String.valueOf(summary.getExtraAttr("cycleEndDate"));
        String cycleType = String.valueOf(summary.getExtraAttr("cycleType"));
        String cycleMonth = String.valueOf(summary.getExtraAttr("cycleMonth"));
        String cycleOrder = String.valueOf(summary.getExtraAttr("cycleOrder"));
        String cycleDay = String.valueOf(summary.getExtraAttr("cycleDay"));
        String cycleWeek = String.valueOf(summary.getExtraAttr("cycleWeek"));
        String cycleHour = String.valueOf(summary.getExtraAttr("cycleHour"));

        //BUG_紧急_V5_V5.6SP1_宁波江北威信软件有限公司_（恩莱）表单制作保存的时候报错：出现异常...._20160331018605
        int month = Strings.isNotBlank(cycleMonth) ? Integer.parseInt(cycleMonth) : 0;
        int day = Strings.isNotBlank(cycleDay) ? Integer.parseInt(cycleDay) : 0;
        int week = Strings.isNotBlank(cycleWeek) ? Integer.parseInt(cycleWeek) : 0;

        HourEnum hourEnum = HourEnum.valueOf(Strings.isNotBlank(cycleHour) ? Integer.parseInt(cycleHour) : 0);

        Date currentTime = new Date();
        Date beginTime = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(new Date()) + " " + hourEnum.getText());
        if (!StringUtil.checkNull(cycleStartDate)) {
            Date selectTime = Datetimes.parseDatetimeWithoutSecond(cycleStartDate + " " + hourEnum.getText());
            if (selectTime.compareTo(currentTime) >= 0) {//开始时间小于当前时间，从当天生成定时任务
                beginTime = selectTime;
            }
        }
        Date endTime = null;
        if (!StringUtil.checkNull(cycleEndDate)) {
            endTime = Datetimes.parseDatetimeWithoutSecond(cycleEndDate + " " + hourEnum.getText());
            if (endTime.compareTo(currentTime) < 0) {//结束时间小于当前时间，不生成定时任务
                return;
            }
        }


		if (CycleEnum.DAY.getValue().equals(cycleType)) {//按天
            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addDate(beginTime, 1);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }
			
            QuartzHolder.newQuartzJobPerDay(jobGroupName, formBindJobName, beginTime, endTime, jobBeanId, parameters);

            LOG.info("表单模板自动发起-定时任务开始时间：formBindJobName:"+formBindJobName +","+ cycleType + "，" + Datetimes.formatDatetime(beginTime)+",templateId:"+template.getId());

        } else if (CycleEnum.WEEK.getValue().equals(cycleType)) {//按周
            Date firstDayInWeek = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInWeek(beginTime)) + " " + hourEnum.getText());
            
            Date firstTimeInWeek = Datetimes.getTodayFirstTime(firstDayInWeek);
            Date currentDateFirstTime = Datetimes.getTodayFirstTime(currentTime);
            //如果当前时间就是每周第一天的话，不默认加7天
            if(!firstTimeInWeek.equals(currentDateFirstTime)) {
            	beginTime = Datetimes.addDate(firstDayInWeek, week);
            }

            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addDate(beginTime, 7);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            QuartzHolder.newQuartzJobPerWeek(jobGroupName, formBindJobName, beginTime, endTime, jobBeanId, parameters);
        } else if (CycleEnum.MONTH.getValue().equals(cycleType)) {//按月
            Date firstDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInMonth(beginTime)) + " " + hourEnum.getText());
            Date lastDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getLastDayInMonth(beginTime)) + " " + hourEnum.getText());
            if ("0".equals(cycleOrder)) {//正数
                beginTime = Datetimes.addDate(firstDayInMonth, day - 1);
            } else if ("1".equals(cycleOrder)) {//倒数
                beginTime = Datetimes.addDate(lastDayInMonth, 1 - day);
            }

            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addMonth(beginTime, 1);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            //倒数的时候重新组装任务的时间参数
            if("1".equals(cycleOrder)){
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(beginTime);
                int SECOND = gc.get(GregorianCalendar.SECOND);
                int MINUTE = gc.get(GregorianCalendar.MINUTE);
                int HOUR_OF_DAY = gc.get(GregorianCalendar.HOUR_OF_DAY);
                String cronExpression = SECOND + " " + MINUTE + " " + HOUR_OF_DAY + " " + day + "L * ?";
                QuartzHolder.newCronQuartzJob(jobGroupName, formBindJobName, cronExpression, beginTime, endTime, jobBeanId, parameters);
            }else{
                QuartzHolder.newQuartzJobPerMonth(jobGroupName, formBindJobName, beginTime, endTime, jobBeanId, parameters);
            }
        } else if (CycleEnum.YEAR.getValue().equals(cycleType)) {//按年
            Date firstDayInYear = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInYear(beginTime)) + " " + hourEnum.getText());
            beginTime = Datetimes.addMonth(firstDayInYear, month - 1);
            Date firstDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getFirstDayInMonth(beginTime)) + " " + hourEnum.getText());
            Date lastDayInMonth = Datetimes.parseDatetimeWithoutSecond(Datetimes.formatDate(Datetimes.getLastDayInMonth(beginTime)) + " " + hourEnum.getText());
            if ("0".equals(cycleOrder)) {//正数
                beginTime = Datetimes.addDate(firstDayInMonth, day - 1);
            } else if ("1".equals(cycleOrder)) {//倒数
                beginTime = Datetimes.addDate(lastDayInMonth, 1 - day);
            }

            if (beginTime.compareTo(currentTime) < 0) {//开始时间小于当前时间，取下次
                beginTime = Datetimes.addYear(beginTime, 1);
            }

            if (endTime != null && endTime.compareTo(beginTime) < 0) {//结束时间小于开始时间，不生成定时任务
                return;
            }

            //倒数的时候重新组装任务的时间参数
            if("1".equals(cycleOrder)){
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(beginTime);
                int SECOND = gc.get(GregorianCalendar.SECOND);
                int MINUTE = gc.get(GregorianCalendar.MINUTE);
                int HOUR_OF_DAY = gc.get(GregorianCalendar.HOUR_OF_DAY);
                String cronExpression = SECOND + " " + MINUTE + " " + HOUR_OF_DAY + " " + day + "L * ?";
                QuartzHolder.newCronQuartzJob(jobGroupName, formBindJobName, cronExpression, beginTime, endTime, jobBeanId, parameters);
            }else {
                QuartzHolder.newQuartzJobPerYear(jobGroupName, formBindJobName, beginTime, endTime, jobBeanId, parameters);
            }
        }
        LOG.info("表单模板自动发起-定时任务开始时间：" + cycleType + "，" + Datetimes.formatDatetime(beginTime));
    }
    private  void updatePermissinRef(ModuleType type, String processXml, String processId,String processTemplateId,Long accountId) throws BusinessException {
        //更新节点权限引用状态
        String configCategory = EnumNameEnum.col_flow_perm_policy.name();
        List<String> list = wapi.getWorkflowUsedPolicyIds(type.name(), processXml, processId, processTemplateId);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                permissionManager.updatePermissionRef(configCategory, list.get(i), accountId);
            }
        }
    }

    /**
     *
     * @Title: updateMessageRuleRef
     * @Description: 更新消息规则引用状态
     * @param processXml
     * @param processId
     * @param processTemplateId
     * @throws BusinessException
     * @return: void
     * @date:   2019年2月25日 上午10:19:04
     * @author: xusx
     * @since   V7.1
     * @throws
     */
    private  void updateMessageRuleRef(String processXml, String processId,String processTemplateId) throws BusinessException {
        //更新消息规则引用状态
        String messageRuleId = wapi.getWorkflowUsedMessageRuleIds( processXml, processId, processTemplateId);
        messageRuleManager.updateMessageRuleReferce(messageRuleId, "-1");
    }
    
    /**
     * @Description: 更新节点期限枚举使用状态
     * @param processXml
     * @param processId
     * @param processTemplateId
     * @throws BusinessException
     */
   private  void updateDealRef(String processXml, String processId,String processTemplateId) throws BusinessException {
       //更新枚举使用状态
       String deadline = wapi.getWorkflowUsedDealTermRuleIds(processXml, processId, processTemplateId);
       if(Strings.isBlank(deadline)){
           return;
       }
       String[] rule = deadline.split(",");
       for (String ruleId : rule) {
           if(Strings.isNotBlank(ruleId)){
               enumManagerNew.updateEnumItemRef(EnumNameEnum.collaboration_deadline.name(), ruleId);
           }
       }
   }


    private void saveOrUpdateCptContentAllHistroy(CtpTemplateHistory template, boolean isNew, Long formAppId) throws BusinessException {
        CtpContentAll content;
        if (isNew) {
            content = new CtpContentAll();
            content.setId(UUIDLong.longUUID());
            content.setCreateId(AppContext.currentUserId());
            content.setCreateDate(new Date());
            content.setContentTemplateId(formAppId);
            //找不到枚举值了。。
            content.setContentType(MainbodyType.FORM.getKey());
            content.setModuleId(template.getId());
            content.setModuleType(template.getModuleType());
            content.setModuleTemplateId(-1L);
            content.setSort(1);
        } else {
            List<CtpContentAll> contentList = MainbodyService.getInstance().getContentList(ModuleType.getEnumByKey(template.getModuleType()), template.getId());
            if (contentList == null || contentList.size() < 1) {
            	//因为现在都是新增逻辑-只有草稿是修改所以如果没有就新增
				content = new CtpContentAll();
				content.setId(UUIDLong.longUUID());
				content.setCreateId(AppContext.currentUserId());
				content.setCreateDate(new Date());
				content.setContentTemplateId(formAppId);
				//找不到枚举值了。。
				content.setContentType(MainbodyType.FORM.getKey());
				content.setModuleId(template.getId());
				content.setModuleType(template.getModuleType());
				content.setModuleTemplateId(-1L);
				content.setSort(1);
            }else{
				content = contentList.get(0);
			}

        }
        template.setBody(content.getId());
        content.setTitle(template.getSubject());
        content.setModifyDate(new Date());
        content.setModifyId(AppContext.currentUserId());
        MainbodyService.getInstance().saveOrUpdateContentAll(content);
    }

    /**
     * 保存流程表单正文模板
     */
    private void saveOrUpdateCptContentAll(CtpTemplate template, boolean isNew, Long formAppId) throws BusinessException {
        CtpContentAll content;
        if (isNew) {
            content = new CtpContentAll();
            content.setId(UUIDLong.longUUID());
            content.setCreateId(AppContext.currentUserId());
            content.setCreateDate(new Date());
            content.setContentTemplateId(formAppId);
            //找不到枚举值了。。
            content.setContentType(MainbodyType.FORM.getKey());
            content.setModuleId(template.getId());
            content.setModuleType(template.getModuleType());
            content.setModuleTemplateId(-1L);
            content.setSort(1);
        } else {
            List<CtpContentAll> contentList = MainbodyService.getInstance().getContentList(ModuleType.getEnumByKey(template.getModuleType()), template.getId());
            if (contentList == null || contentList.size() < 1) {
                throw new BusinessException(template.getSubject() + ":修改时找不到正文组件内容!");
            }
            content = contentList.get(0);
        }
        template.setBody(content.getId());
        content.setTitle(template.getSubject());
        content.setModifyDate(new Date());
        content.setModifyId(AppContext.currentUserId());
        MainbodyService.getInstance().saveOrUpdateContentAll(content);
    }
    private void savSupervise(Map map, Long templateId, boolean isNew, boolean sendMessage) throws BusinessException {
        String supervisorId = (String) map.get("supervisorIds");
        String supervisors = (String) map.get("supervisorNames");
        Long templateDateTerminal = 0L;
        if (Strings.isNotBlank((String) map.get("templateDateTerminal"))) {
            templateDateTerminal = ParamUtil.getLong(map, "templateDateTerminal", 0L);
        }
        String role = (String) map.get("role");
        if (Strings.isBlank(supervisors) && Strings.isBlank(role)) {
            superviseManager.deleteAllInfoByTemplateId(templateId);
            return;
        }
        String superviseTitle = (String) map.get("title");
        SuperviseSetVO ssvo = new SuperviseSetVO();
        ssvo.setTitle(superviseTitle);
        ssvo.setSupervisorIds(supervisorId);
        ssvo.setSupervisorNames(supervisors);
        ssvo.setTemplateDateTerminal(templateDateTerminal);
        ssvo.setRole(role);
        //detailid没有实际意义，这里只是用来在接口中判断是保存还是更新
        if (isNew) {
            ssvo.setDetailId(null);
        } else {
            ssvo.setDetailId(1L);
        }
        superviseManager.saveOrUpdateSupervise4Template(templateId, ssvo);
    }

	public MessageRuleManager getMessageRuleManager() {
		return messageRuleManager;
	}

	public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
		this.messageRuleManager = messageRuleManager;
	}
}
