package com.seeyon.v3x.services.flow.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationStartEvent;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.quartz.CollaborationJob;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.doc.manager.DocFilingManager;
import com.seeyon.apps.doc.manager.DocHierarchyManager;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.cap4.form.service.CAP4FormCacheManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyService;
import com.seeyon.ctp.common.content.mainbody.MainbodyStatus;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.dao.AttachmentDAO;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.CollaborationTemplateManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataMasterBean;
import com.seeyon.ctp.form.bean.FormDataSubBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.form.bean.FormTableBean;
import com.seeyon.ctp.form.service.FormCacheManager;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.form.util.Enums.FieldAccessType;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.form.util.Enums.SubTableField;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.services.ErrorServiceMessage;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.vo.BPMSeeyonPolicyVO;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.oainterface.common.OAInterfaceException;
import com.seeyon.oainterface.common.PropertyList;
import com.seeyon.v3x.project.manager.ProjectManager;
import com.seeyon.v3x.services.flow.FlowFactory;
import com.seeyon.v3x.services.flow.FlowUtil;
import com.seeyon.v3x.services.flow.bean.AttachmentExport;
import com.seeyon.v3x.services.flow.bean.FlowExport;
import com.seeyon.v3x.services.flow.bean.TextAttachmentExport;
import com.seeyon.v3x.services.flow.bean.TextHtmlExport;
import com.seeyon.v3x.services.form.bean.DefinitionExport;
import com.seeyon.v3x.services.form.bean.FormExport;
import com.seeyon.v3x.services.form.bean.RecordExport;
import com.seeyon.v3x.services.form.bean.SubordinateFormExport;
import com.seeyon.v3x.services.form.bean.ValueExport;
import com.seeyon.v3x.services.util.SaveFormToXml;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import www.seeyon.com.utils.UUIDUtil;

/**
 * @author zhangyong
 */
public class FlowFactoryImpl implements FlowFactory {
    private final static Log logger = LogFactory.getLog(FlowFactoryImpl.class);
    private TemplateManager templateManager = null;
    private OrgManager orgManager = null;
    private AffairManager affairManager = null;
    private ColManager colManager = null;
    private FormManager formManager = null;
    private WorkTimeManager workTimeManager = null;
    private CollaborationTemplateManager collaborationTemplateManager = null;
    private IndexManager indexManager = null;
    private AppLogManager appLogManager = null;
    private ProcessLogManager processLogManager = null;
    private FormCacheManager formCacheManager = null;
    private WorkflowApiManager wapi = null;
    private DocHierarchyManager docHierarchyManager = null;
    private ProjectManager projectManager = null;
    private AttachmentManager attachmentManager = null;
    private static FileManager fileManager = null;
    private CommentManager commentManager = null;
    private AttachmentDAO attachmentDAO = null;
    private DocFilingManager docFilingManager = null;
    private SuperviseManager superviseManager = null;
    private UserMessageManager userMessageManager = null;
    private DocApi docApi = null;

    public AttachmentManager getAttachmentManager() {
        if (attachmentManager == null) {
            attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
        }
        return attachmentManager;
    }

    public TemplateManager getTemplateManager() {
        if (templateManager == null) {
            templateManager = (TemplateManager) AppContext.getBean("templateManager");
        }
        return templateManager;
    }

    public AffairManager getAffairManager() {
        if (affairManager == null) {
            affairManager = (AffairManager) AppContext.getBean("affairManager");
        }
        return affairManager;
    }

    public ColManager getColManager() {
        if (colManager == null) {
            colManager = (ColManager) AppContext.getBean("colManager");
        }
        return colManager;
    }

    public OrgManager getOrgManager() {
        if (orgManager == null) {
            orgManager = (OrgManager) AppContext.getBean("orgManager");
        }
        return orgManager;
    }

    public FormManager getFormManager() {
        if (formManager == null) {
            formManager = (FormManager) AppContext.getBean("formManager");
        }
        return formManager;
    }

    public WorkTimeManager getWorkTimeManager() {
        if (workTimeManager == null) {
            workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
        }
        return workTimeManager;
    }

    public CollaborationTemplateManager getCollaborationTemplateManager() {
        if (collaborationTemplateManager == null) {
            collaborationTemplateManager = (CollaborationTemplateManager) AppContext.getBean("collaborationTemplateManager");
        }
        return collaborationTemplateManager;
    }

    public IndexManager getIndexManager() {
        if (indexManager == null) {
            indexManager = (IndexManager) AppContext.getBean("indexManager");
        }
        return indexManager;
    }

    public AppLogManager getAppLogManager() {
        if (appLogManager == null) {
            appLogManager = (AppLogManager) AppContext.getBean("appLogManager");
        }
        return appLogManager;
    }

    public ProcessLogManager getProcessLogManager() {
        if (processLogManager == null) {
            processLogManager = (ProcessLogManager) AppContext.getBean("processLogManager");
        }
        return processLogManager;
    }

    public FormCacheManager getFormCacheManager() {
        if (formCacheManager == null) {
            formCacheManager = (FormCacheManager) AppContext.getBean("formCacheManager");
        }
        return formCacheManager;
    }

    public WorkflowApiManager getWorkflowApiManager() {
        if (wapi == null) {
            wapi = (WorkflowApiManager) AppContext.getBean("wapi");
        }
        return wapi;
    }

    public DocHierarchyManager getDocHierarchyManager() {
        if (docHierarchyManager == null) {
            docHierarchyManager = (DocHierarchyManager) AppContext.getBean("docHierarchyManager");
        }
        return docHierarchyManager;
    }

    public ProjectManager getProjectManager() {
        if (projectManager == null) {
            projectManager = (ProjectManager) AppContext.getBean("projectManager");
        }
        return projectManager;
    }

    public static FileManager getFileManager() {
        if (fileManager == null) {
            fileManager = (FileManager) AppContext.getBean("fileManager");
        }
        return fileManager;
    }

    public CommentManager getCommentManager() {
        if (commentManager == null) {
            commentManager = (CommentManager) AppContext.getBean("ctpCommentManager");
        }
        return commentManager;
    }

    public AttachmentDAO getAttachmentDAO() {
        if (attachmentDAO == null) {
            attachmentDAO = (AttachmentDAO) AppContext.getBean("attachmentDAO");
        }
        return attachmentDAO;
    }

    public DocFilingManager getDocFilingManager() {
        if (docFilingManager == null) {
            docFilingManager = (DocFilingManager) AppContext.getBean("docFilingManager");
        }
        return docFilingManager;
    }

    public SuperviseManager getSuperviseManager() {
        if (superviseManager == null) {
            superviseManager = (SuperviseManager) AppContext.getBean("superviseManager");
        }
        return superviseManager;
    }

    public UserMessageManager getUserMessageManager() {
        if (userMessageManager == null) {
            userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
        }
        return userMessageManager;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    private CAP4FormManager cap4FormManager = null;

    public CAP4FormManager getCap4FormManager() {
        if (cap4FormManager == null) {
            cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
        }
        return cap4FormManager;
    }

    private CAP4FormCacheManager cap4FormCacheManager;

    public CAP4FormCacheManager getCAP4FormCacheManager() {
        if (cap4FormCacheManager == null) {
            cap4FormCacheManager = (CAP4FormCacheManager) AppContext.getBean("cap4FormCacheManager");
        }
        return cap4FormCacheManager;
    }

    @Override
    public long sendCollaboration(String senderLoginName, String templateCode, String subject, Object data1,
                                  Long[] attachments, String parameter, String relateDoc) throws BusinessException, ServiceException {
        Map<String, Object> relevantParam = new HashMap();
        Long summaryId = sendCollaboration(senderLoginName, templateCode, subject, data1, attachments, parameter, relateDoc, relevantParam);
        return summaryId;
    }

    @Override
    public long sendCollaboration(String senderLoginName, String templateCode, String subject, Object data1,
                                  Long[] attachments, String parameter, String relateDoc, Map<String, Object> relevantParam) throws BusinessException, ServiceException {
        CtpTemplate template = getTemplateManager().getTempleteByTemplateNumber(templateCode);

        if (template == null) {
            throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(), ErrorServiceMessage.flowTempleExist.getValue() + ":" + templateCode);
        }
        //获取模板对应的关联协同与附件(协同公文获取summaryID，会议文档直接获取affairID)
        List<Attachment> attachmentList = getAttachmentManager().getByReference(template.getId());
        StringBuffer attBuffer = new StringBuffer();
        StringBuffer colBuffer = new StringBuffer();
        StringBuffer edocBuffer = new StringBuffer();
        List<Attachment> meetingAndDoc = new ArrayList<Attachment>();

        colBuffer.append("col|");
        edocBuffer.append("edoc|");

        List<Long> attList = new ArrayList<Long>();
        if (attachmentList != null) {
            for (int i = 0; i < attachmentList.size(); i++) {
                Attachment attInfo = attachmentList.get(i);
                if (attInfo != null && attInfo.getType() == 0) {
                    attList.add(attInfo.getFileUrl());
                } else if (attInfo != null && attInfo.getType() == 2 && "collaboration".equals(attInfo.getMimeType())) {
                    colBuffer.append(getColManager().getAffairById(attInfo.getFileUrl()).getObjectId().toString() + ",");
                } else if (attInfo != null && attInfo.getType() == 2 && "edoc".equals(attInfo.getMimeType())) {
                    edocBuffer.append(getColManager().getAffairById(attInfo.getFileUrl()).getObjectId().toString() + ",");
                } else if (attInfo != null && attInfo.getType() == 2 && "km".equals(attInfo.getMimeType())) {
                    meetingAndDoc.add(attInfo);
                } else if (attInfo != null && attInfo.getType() == 2 && "meeting".equals(attInfo.getMimeType())) {
                    meetingAndDoc.add(attInfo);
                }
            }
        }

        Long fileIds[] = getAttachments(attList, attachments);
        if (relateDoc == null && (colBuffer.toString().split("\\|").length >= 2 || edocBuffer.toString().split("\\|").length >= 2)) {
            attBuffer.append(colBuffer.deleteCharAt(colBuffer.length() - 1).toString() + ";");
            attBuffer.append(edocBuffer.deleteCharAt(edocBuffer.length() - 1).toString() + ";");
            relateDoc = attBuffer.toString();
        }

        FormExport data = null;
        FormBean formBean = null;
        com.seeyon.cap4.form.bean.FormBean formBean4 = null;

        boolean isForm = template.getModuleType() == 1 && Integer.valueOf(template.getBodyType()) == 20;
        if (isForm) {
            data = (FormExport) data1;
            formBean = getFormManager().getFormByFormCode(template);
            formBean4 = getCap4FormManager().getFormByFormCode(template);
        }
        V3xOrgMember member = null;
        try {
            member = getOrgManager().getMemberByLoginName(senderLoginName);
        } catch (Exception e2) {
            logger.error("获取人员信息异常！");
            throw new BusinessException(e2);
        }
        if (member == null) {
            throw new ServiceException(ErrorServiceMessage.formImportServiceMember.getErroCode(), ErrorServiceMessage.formImportServiceMember.getValue() + ":" + senderLoginName);
        }
        long sender = member.getId();
        User user = new User();
        user.setId(sender);
        user.setName(member.getName());
        if (relevantParam.size() > 0 && relevantParam.get("accountId") != null) {
            user.setLoginAccount((Long) relevantParam.get("accountId"));
            user.setAccountId(user.getLoginAccount());
        } else {
            user.setLoginAccount(member.getOrgAccountId());
            user.setAccountId(user.getLoginAccount());
        }
        user.setLocale(LocaleContext.getAllLocales().get(0));
        AppContext.putThreadContext(com.seeyon.ctp.common.GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
        if (AppContext.getRawSession() != null) {
            Object raw = AppContext.getSessionContext(com.seeyon.ctp.common.constants.Constants.SESSION_CURRENT_USER);
            if (raw != null) {
                User u = (User) raw;
                logger.error("操作被阻止，试图将会话中的当前用户" + u.getName() + "替换为" + user.getName());
            } else {
                AppContext.putSessionContext(com.seeyon.ctp.common.constants.Constants.SESSION_CURRENT_USER, user);
            }
        }
        if (isForm) {
            if (formBean4 != null) {
                checkIsExist(data, senderLoginName, templateCode, template, formBean4, member);
            } else {
                checkIsExist(data, senderLoginName, templateCode, template, formBean, member);
            }
        }
        //正文类型HTML&Form
        //10HTML20FORM
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String bodyType = template.getBodyType();
        Long templateId = template.getId();
        String sub = Strings.nobreakSpaceToSpace(subject);

        if (StringUtils.isBlank(sub)) {
            sub = template.getSubject() + "(" + member.getName() + " " + DateUtil.formatDateTime(now) + ")";
        }
        ColSummary summary = createColSummary(template);
        summary.setSubject(sub);
        summary.setProjectId(template.getProjectId());
        //       ColSummary summary=new ColSummary();
        if (template != null) {
            summary.setPermissionAccountId(template.getOrgAccountId());
        }

        long masterId = -1L;
        if (isForm) {
            //表单数据
            try {
                if (formBean4 != null) {
                    masterId = saveMasterAndSubForm(template, data, formBean4, user);
                } else {
                    masterId = saveMasterAndSubForm(template, data, formBean, user);
                }
            } catch (SQLException e1) {
                throw new ServiceException(ErrorServiceMessage.formExportServiceDateInfoError.getErroCode(), ErrorServiceMessage.formExportServiceDateInfoError.getValue() + ":" + e1.getMessage());
                //throw new BusinessException(e1);
            }
        }
        //为1则保存待发，其他则发送
        boolean saveDraft = "1".equals(parameter);
        //没有待发条件的要判断一次是否后续节点是否需要选人，以及是否有分支条件。如果有则进入发起人的待发
        //1.流程中有分支2.有节点需要选人
        CPMatchResultVO resultVo = null;
        boolean addFirstNode = false;
        if (!saveDraft) {
            resultVo = isSelectPersonCondition(isForm, template, member, sender, masterId, user);
            if (resultVo.isPop()) {
                //saveDraft = true;
                addFirstNode = true;
            }
        }
        //保存正文
        MainbodyService mainBodyService = MainbodyService.getInstance();

        CtpContentAllBean content = new CtpContentAllBean();

        content.setModuleType(ModuleType.collaboration.getKey());
        content.setCreateId(sender);
        content.setCreateDate(now);
        content.setSort(0);
        content.setId(UUIDUtil.getUUIDLong());
        content.setStatus(MainbodyStatus.STATUS_POST_SAVE);
        content.setContentType(Integer.parseInt(bodyType));
        content.setTitle(summary.getSubject());
        content.setContentTemplateId(0L);
        content.setContentDataId(null);

        if (MainbodyType.FORM.getKey() == content.getContentType()) {
            content.setContentType(MainbodyType.FORM.getKey());
            if (formBean4 != null) {
                content.setContentTemplateId(formBean4.getId());
            } else {
                content.setContentTemplateId(formBean.getId());
            }
            content.setContentDataId(masterId);

            summary.setFormAppid(content.getContentTemplateId());//表单ID
            summary.setFormRecordid(content.getContentDataId());//form主数据ID
            //            summary.setFormid(formBean.getAuthViewBeanById(Long.parseLong(content.getContent().getRightId())).getFormViewId());//视图ID
        } else {
            content.setContent("<p>" + data1 + "</p>");
        }
        content.setModuleTemplateId(templateId);
        content.setModuleId(summary.getId());
        content.setModifyDate(now);
        content.setModifyId(sender);
//        content.setContent("<p>ddddddddddddd</P>");
        mainBodyService.saveOrUpdateContentAll(content.toContentAll());

        summary.setOrgAccountId(member.getOrgAccountId());
        summary.setOrgDepartmentId(member.getOrgDepartmentId());
        summary.setStartMemberId(sender);
        summary.setCreateDate(now);
        summary.setStartDate(now);
        summary.setState(CollaborationEnum.flowState.run.ordinal());
        summary.setSubState(CollaborationEnum.SubState.Normal.ordinal());
        summary.setTempleteId(template.getId());
        //保存上传的附件
        boolean attaFlag = false;
        if (fileIds != null && fileIds.length != 0) {
            try {
                String attaFlagStr = getAttachmentManager().create(fileIds, ApplicationCategoryEnum.collaboration, summary.getId(), summary.getId());
                attaFlag = Constants.isUploadLocaleFile(attaFlagStr);
            } catch (Exception e) {
                throw new BusinessException(e);
            }
        }

        /*获取表单正文附件ID**/
        //保存上传的附件 如果为true，则执行后面更新attachmentManager表sub_reference 操作
        Object formContentAttIds = (Object) relevantParam.get("formContentAtt");
        Long[] contentAttIds = (Long[]) formContentAttIds;
        if (null != contentAttIds && contentAttIds.length > 0) {
            try {
                String formContentAtta = getAttachmentManager().create(contentAttIds, ApplicationCategoryEnum.collaboration, summary.getId(), summary.getId());

                List<Attachment> formAttList = getAttachmentManager().getByReference(summary.getId(), summary.getId());

                int count = 0;
                for (Attachment att : formAttList) {
                    for (int i = 0; i < contentAttIds.length; i++) {
                        if (att.getFileUrl().equals(contentAttIds[i]) && count < contentAttIds.length) {
                            att.setSubReference(contentAttIds[i]);
                            getAttachmentManager().update(att);
                            logger.info("表单正文附件ID上传：" + att.getFilename());
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceException(ErrorServiceMessage.formExportServiceDateInfoError.getErroCode(), ErrorServiceMessage.formExportServiceDateInfoError.getValue() + ":" + e.getMessage());
            }
        }
        //保存关联文档
        //relateDoc格式："col|123,456;doc|321,654"
        if (Strings.isNotBlank(relateDoc)) {
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
            if (summaryIds != null) {
                loopAttachmentList(summary, summaryIds, "col");
            }
            if (summaryEdocIds != null) {
                loopAttachmentList(summary, summaryEdocIds, "edoc");
            }
            if (meetingAndDoc != null) {
                for (Attachment attInfo : meetingAndDoc) {
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
                    getAttachmentDAO().save(atta);
                }
            }
        }

        if (attaFlag) {
            ColUtil.setHasAttachments(summary, attaFlag);
        }


        summary.setCanDueReminder(false);
        summary.setAudited(false);
        summary.setVouch(CollaborationEnum.vouchState.defaultValue.ordinal());
        summary.setBodyType(String.valueOf(content.getContentType()));
        try {
            summary.setSubject(ColUtil.makeSubject(template, summary, null));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        CtpAffair affair = new CtpAffair();

        affair.setIdIfNew();
        affair.setCreateDate(now);
        affair.setApp(ApplicationCategoryEnum.collaboration.key());
        affair.setSubApp(ApplicationSubCategoryEnum.collaboration_tempate.key());
        affair.setSubject(summary.getSubject());
        affair.setReceiveTime(now);
        affair.setMemberId(sender);
        //affair对象的ObjectID关联协同的ID
        affair.setObjectId(summary.getId());
        affair.setSubObjectId(null);
        //当前用户（发送者的）
        affair.setSenderId(sender);
        affair.setState(StateEnum.col_sent.key());
        affair.setSubState(SubStateEnum.col_normal.key());
        if (saveDraft) {
            affair.setState(StateEnum.col_waitSend.key());
            affair.setSubState(SubStateEnum.col_waitSend_draft.key());
        }
        affair.setTempleteId(summary.getTempleteId());
        affair.setBodyType(bodyType);
        affair.setImportantLevel(summary.getImportantLevel());
        affair.setResentTime(summary.getResentTime());
        affair.setForwardMember(summary.getForwardMember());
        affair.setNodePolicy("collaboration");//协同发起人节点权限默认为协同
        /*int track = 0;
        if(summary.getCanTrack()){
            track=1;
        }*/
        affair.setTrack(1);
        affair.setDelete(false);
        affair.setArchiveId(null);
//        affair.setIdentifier("0");
        //三个Boolean类型初始值，解决PostgreSQL插入记录异常问题
        affair.setFinish(false);
        affair.setCoverTime(false);
        affair.setDueRemind(false);
        affair.setOrgAccountId(member.getOrgAccountId());
        if (summary.getDeadline() != null && summary.getDeadline() > 0) {
            AffairUtil.addExtProperty(affair, AffairExtPropEnums.processPeriod, summary.getDeadline());
        }
        if (saveDraft) {
            affair.setIdentifier(IdentifierUtil.newIdentifier(affair.getIdentifier(), 20, '0'));
        }
        /**获取FORM_OPERATION_ID***/
        BPMSeeyonPolicyVO startPolicy = wapi.getStartNodeFormPolicy(template.getWorkflowId());
        String formViewOperation = null;
        if (null != startPolicy && Strings.isNotBlank(startPolicy.getFormViewOperation())) {
            formViewOperation = startPolicy.getFormViewOperation();
        }
        affair.setMultiViewStr(formViewOperation);
        affair.setFormAppId(template.getFormAppId());
        affair.setFormRecordid(content.getContentDataId());

        getAffairManager().save(affair);

        AffairData affairData = ColUtil.getAffairData(summary);

        Long flowPermAccountId = ColUtil.getFlowPermAccountId(member.getOrgAccountId(), summary);
        affairData.addBusinessData(ColConstant.FlowPermAccountId, flowPermAccountId);
        if (MainbodyType.FORM.getKey() == content.getContentType()) {
            //只有表单正文的时候才有这两个表单参数，否则工作流分支判断会出错
            affairData.setFormAppId(content.getContentTemplateId());//表单ID
            affairData.setFormRecordId(content.getContentDataId());//form主数据ID
        }
        String[] caseProcessIds = null;
        Map<String, Object> globalMap = new HashMap<String, Object>();
        if (!saveDraft) {
            caseProcessIds = runWorkFlow(addFirstNode, template, member, resultVo, summary, user, affairData, affair, globalMap);

            summary.setProcessId(caseProcessIds[1]);
            summary.setCaseId(Long.valueOf(caseProcessIds[0]));
        } else {
            summary.setStartDate(null);
            summary.setState(null);
            summary.setProcessId("");
            summary.setCaseId(null);
        }
        ColUtil.updateCurrentNodesInfo(summary);
        getColManager().saveColSummary(summary);
        //预归档
        if (summary.getArchiveId() != null) {
//            	getDocFilingManager().pigeonholeAsLinkWithoutAcl(ApplicationCategoryEnum.collaboration.key(), 
//              affair.getId(), ColUtil.isHasAttachments(summary), summary.getArchiveId(), sender, null);

            preArchiving(summary, affair, user, isForm, template);
        }
        getCollaborationTemplateManager().updateTempleteHistory(summary.getTempleteId(), sender);

        if (saveDraft) {
            //调用表单万能方法,更新状态，触发子流程等
            if (String.valueOf(MainbodyType.FORM.getKey()).equals(summary.getBodyType())) {
                try {
                    List<Comment> commentList = getCommentManager().getCommentAllByModuleId(ModuleType.collaboration, summary.getId());
                    if (formBean4 != null) {
                        getCap4FormManager().updateDataState(summary, affair, ColHandleType.save, commentList);
                    } else {
                        getFormManager().updateDataState(summary, affair, ColHandleType.save, commentList);
                    }
                } catch (Exception e) {
                    throw new BusinessException("更新表单相关信息异常", e);
                }
            }
        } else {
            //保存督办
//                colManager.saveColSupervise(summary, true, SuperviseEnum.superviseState.supervising.ordinal(), true);
            //superviseManager.addSuperviseFromTemplate(summary, member.getId(), member.getOrgAccountId());
            getSuperviseManager().saveSuperviseByCopyTemplete(member.getId(), summary, template.getId());
            // 督办消息发送
            if (subject == null) {
                subject = summary.getSubject();
            }
            //V6.0会有通过接口发起表单模板，督办人收到两条督办消息，这里取消接口发送消息的设置
            //	sendSuperviseMessage(subject, member, user, templateId, summary);
            //记录流程日志
            //设置下一个节点的节点名和节点权限
            String[] nextNodeNames = caseProcessIds[2].split("[,]");
            String toMmembers = Strings.join(",", nextNodeNames);
            getAppLogManager().insertLog(user, AppLogAction.Coll_New, user.getName(), summary.getSubject());
            getProcessLogManager().insertLog(user, Long.parseLong(summary.getProcessId()),
                    -1l, ProcessLogAction.sendForm, toMmembers);
            //定时任务超期提醒和提前提醒
            CollaborationJob.createQuartzJobOfSummary(summary, getWorkTimeManager());
            //触发协同立方事件
            //V6.0不需要通过这种方式触发协同立方
            //EventDispatcher.fireEvent(colevent);

            //调用表单万能方法,更新状态，触发子流程等
            if (String.valueOf(MainbodyType.FORM.getKey()).equals(summary.getBodyType())) {
                try {
                    List<Comment> commentList = getCommentManager().getCommentAllByModuleId(ModuleType.collaboration, summary.getId());
                    if (formBean4 != null) {
                        getCap4FormManager().updateDataState(summary, affair, ColHandleType.send, commentList);
                    } else {
                        getFormManager().updateDataState(summary, affair, ColHandleType.send, commentList);
                    }
                } catch (Exception e) {
                    throw new BusinessException("更新表单相关信息异常", e);
                }
            }
            //DateSharedWithWorkflowEngineThreadLocal.setColSummary(summary);
            //事件通知
            CollaborationStartEvent event = new CollaborationStartEvent(this);
            event.setSummaryId(summary.getId());
            event.setFrom("pc");
            event.setAffair(affair);
            EventDispatcher.fireEvent(event);
        }
        if (AppContext.hasPlugin("index")) {
            if (String.valueOf(MainbodyType.FORM.getKey()).equals(summary.getBodyType())) {
                getIndexManager().add(summary.getId(), ApplicationCategoryEnum.form.getKey());
            } else {
                getIndexManager().add(summary.getId(), ApplicationCategoryEnum.collaboration.getKey());
            }
        }
        return summary.getId();
    }

    private void preArchiving(ColSummary summary, CtpAffair affair, User sender, boolean isForm, CtpTemplate template) {
        String archiveKeyword = "";

        //归档
        int app = ApplicationCategoryEnum.collaboration.key();
        if (isForm) {
            app = ApplicationCategoryEnum.form.key();
        }

        if (AppContext.hasPlugin("doc")) {
            if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {
                try {
                    JSONObject jo = new JSONObject(summary.getAdvancePigeonhole());
                    archiveKeyword = jo.optString("archiveKeyword", "");
                    String advancePigeonhole = jo.toString();
                    summary.setAdvancePigeonhole(advancePigeonhole);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {//高级归档
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
                    if (Strings.isNotBlank(archiveField)) {
                        archiveFieldValue = formManager.getMasterFieldValue(summary.getFormAppid(), summary.getFormRecordid(), archiveField, true).toString();
                        if (Strings.isNotBlank(archiveFieldValue)) {
                            archiveRealId = docApi.getPigeonholeFolder(destFolderId, archiveFieldValue, isCreateFloder);//真实归档的路径
                        }
                        if (archiveRealId == null) {
                            archiveFieldValue = "Temp";
                            //归档到Temp下面
                            archiveRealId = docApi.getPigeonholeFolder(destFolderId, archiveFieldValue, true);//真实归档的路径
                        }
                    } else {
                        //普通归档
                        archiveRealId = destFolderId;
                    }

                    String keyword = null;
                    if (Strings.isNotBlank(archiveKeyword)) {
                        keyword = formManager.getCollSubjuet(summary.getFormAppid(), archiveKeyword, summary.getFormRecordid(), false);
                        //LOG.info("预先归档："+summary.getId()+"|keyword:"+keyword+",|summary.getFormAppid():"+summary.getFormAppid()+",|archiveKeyword:"+archiveKeyword+",|summary.getFormRecordid():"+summary.getFormRecordid());
                    }
                    if (keyword != null && keyword.length() > 85) {//关键字截取
                        keyword = keyword.substring(0, 85);
                        logger.info("预先归档：" + summary.getId() + "|keyword:" + keyword);
                    }
                    String _archiveText = jo.optString("archiveText", "");
                    boolean hasAttachments = ColUtil.isHasAttachments(summary);
                    if (null != destFolderId && !"true".equals(_archiveText)) {
                        if (Strings.isBlank(StrArchiveFolder)) {//OA-122843
                            docApi.pigeonholeWithoutAcl(sender.getId(), app, affair.getId(), hasAttachments, archiveRealId, PigeonholeType.edoc_account.ordinal(), keyword);
                            jo.put(ColConstant.COL_ARCHIVEFIELDVALUE, archiveFieldValue);
                            summary.setAdvancePigeonhole(jo.toString());
                            updateJson = false;
                        } else if (!archiveFieldValue.equals(StrArchiveFolder)) {//OA-122843同一个单子，指定回退后再次发起
                            docApi.updatePigehole(AppContext.getCurrentUser().getId(), affair.getId(), ApplicationCategoryEnum.form.key());
                            docApi.moveWithoutAcl(AppContext.getCurrentUser().getId(), affair.getId(), archiveRealId);
                        }
                    }

                    if (updateJson) {
                        //归档路径如果没有创建目录， 显示值和实际归档路径不一致
                        jo.put(ColConstant.COL_ARCHIVEFIELDVALUE, archiveFieldValue);
                        summary.setAdvancePigeonhole(jo.toString());
                    }
                } catch (Exception e) {
                    logger.error("FlowFactoryImpl.getAdvancePigeonhole():" + summary.getAdvancePigeonhole(), e);
                }
            } else if (null != summary.getArchiveId()) {
                if (Strings.isNotBlank(summary.getSubject())) {
                    DocResourceBO _exist = null;
                    try {
                        _exist = docApi.getDocResource(summary.getArchiveId());
                    } catch (BusinessException e) {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                        logger.error("DocResourceBO _exist获取异常！");
                    }
                    if (null != _exist) {

                        String summaryTem = template.getSummary();
                        ColSummary summaryTemBean = (ColSummary) XMLCoder.decoder(summaryTem);
                        boolean hasPrePath = null != summaryTemBean.getArchiveId() || Strings.isNotBlank(summaryTemBean.getAdvancePigeonhole());

                        Integer pigholeType = hasPrePath ? PigeonholeType.edoc_account.ordinal() : PigeonholeType.edoc_dept.ordinal();

                        try {
                            docApi.pigeonholeWithoutAcl(sender.getId(), app, affair.getId(),
                                    ColUtil.isHasAttachments(summary), summary.getArchiveId(), pigholeType, null);
                        } catch (BusinessException e) {
                            // TODO Auto-generated catch block
                            //e.printStackTrace();
                            logger.error("FlowFactoryImpl发起流程预归档异常！");
                        }
                    }
                } else {
                    logger.info("Id为" + summary.getId() + "的协同，由于标题为空不允许归档！");
                }
            }
        } else {
            logger.info("没有DOC插件无法预归档！");
        }
    }

    /**
     * @param summary
     * @param summaryIds
     * @throws BusinessException
     */

    private void loopAttachmentList(ColSummary summary, String[] summaryIds, String Type) throws BusinessException {
        for (String summaryId : summaryIds) {
            List<CtpAffair> affairs = getAffairManager().getAffairs(Long.valueOf(summaryId), StateEnum.col_sent);
            if (affairs != null && affairs.size() == 1) {
                CtpAffair affair = affairs.get(0);
                Attachment atta = new Attachment();
                atta.setIdIfNew();
                atta.setReference(summary.getId());
                atta.setSubReference(summary.getId());
                atta.setCategory("col".equals(Type) ? ApplicationCategoryEnum.collaboration.ordinal() : ApplicationCategoryEnum.edoc.ordinal());
                atta.setType(2);
                atta.setSize(0L);
                atta.setFilename(affair.getSubject());
                atta.setFileUrl(affair.getId());
                atta.setMimeType("col".equals(Type) ? "collaboration" : "edoc");
                atta.setCreatedate(affair.getCreateDate());
                atta.setDescription(affair.getId() + "");
                atta.setGenesisId(affair.getId());
                getAttachmentDAO().save(atta);
            }
        }
    }

    /**
     * 模板设置的附件和第三方发来的附件合并
     *
     * @param attList
     * @param fileIds
     * @return
     */
    private Long[] getAttachments(List<Long> attList, Long[] fileIds) {
        if (CollectionUtils.isNotEmpty(attList)) {
            int attacSizeThirdParty = fileIds != null ? fileIds.length : 0;
            Long[] fileIdTemp = new Long[attacSizeThirdParty + attList.size()];
            for (int i = 0; i < attacSizeThirdParty; i++) {
                fileIdTemp[i] = fileIds[i];
            }
            for (int i = 0; i < attList.size(); i++) {
                fileIdTemp[attacSizeThirdParty + i] = attList.get(i);
            }
            fileIds = fileIdTemp;
        }
        return fileIds;
    }

    /**
     * 发送督办消息
     *
     * @param subject
     * @param member
     * @param user
     * @param templateId
     * @param summary
     * @throws BusinessException
     */
    private void sendSuperviseMessage(String subject, V3xOrgMember member,
                                      User user, Long templateId, ColSummary summary)
            throws BusinessException {
        Set<Long> templateIdSets = getSuperviseManager().parseTemplateSupervisorIds(
                templateId, member.getId());
        CtpSupervisor ctpPeop = null;
        List<CtpSupervisor> surpeople = new ArrayList<CtpSupervisor>();
        CtpSuperviseDetail moduleDetail = new CtpSuperviseDetail();
        moduleDetail.setIdIfNew();

        if (Strings.isNotEmpty(templateIdSets)) {
            for (Long mid : templateIdSets) {
                ctpPeop = new CtpSupervisor();
                ctpPeop.setSupervisorId(mid);
                ctpPeop.setIdIfNew();
                ctpPeop.setSuperviseId(moduleDetail.getId());
                surpeople.add(ctpPeop);
            }
        }

        String hastenType = "col.supervise.hasten";
        String linkType = "message.link.col.supervise";
        int forwardMemberFlag = 0;
        String forwardMember = null;
        int ImportantLevel = 1;
        ApplicationCategoryEnum app = ApplicationCategoryEnum.collaboration;

        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        MessageReceiver receiver = null;
        List<Long> members = new ArrayList<Long>();
        if (surpeople != null) {
            for (CtpSupervisor colSupervisor : surpeople) {
                if (!user.getId().equals(colSupervisor.getSupervisorId())
                        && !members.contains(colSupervisor.getSupervisorId())) {
                    receiver = new MessageReceiver(moduleDetail.getId(),
                            colSupervisor.getSupervisorId(), linkType,
                            summary.getId());
                    receivers.add(receiver);
                    members.add(colSupervisor.getSupervisorId());
                }
            }
        }
        if (receivers != null) {
            getUserMessageManager().sendSystemMessage(
                    new MessageContent(hastenType, subject, member.getName(),
                            forwardMemberFlag, forwardMember)
                            .setImportantLevel(ImportantLevel), app, user
                            .getId(), receivers, 7);
        }
    }

    private ColSummary createColSummary(CtpTemplate template) {
        String strXml = template.getSummary();
        ColSummary summary = (ColSummary) XMLCoder.decoder(strXml);
        summary.setIdIfNew();
        return summary;
    }

    /**
     * 权限检查（人员，表单，模板权限）
     *
     * @param templateCode
     * @param template
     * @param formBean
     * @param member
     * @param templateManager
     * @throws ServiceException
     */
    private void checkIsExist(FormExport data, String loginName, String templateCode, CtpTemplate template, FormBean formBean, V3xOrgMember member) throws ServiceException {
        if (template.getWorkflowId() == null) {
            throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(), ErrorServiceMessage.flowTempleExist.getValue() + ":" + templateCode);
        }

        if (formBean == null) {
            //表单模板不存在
            throw new ServiceException(ErrorServiceMessage.formImportServiceIsexit.getErroCode(), ErrorServiceMessage.formImportServiceIsexit.getValue() + ":" + templateCode);
        }
        if (formBean.getFormType() == FormType.baseInfo.getKey() || formBean.getFormType() == FormType.manageInfo.getKey()) {
            throw new ServiceException(ErrorServiceMessage.flowImportException.getErroCode(),
                    ErrorServiceMessage.flowImportException.getValue());
        }
        if (member == null) {
            throw new ServiceException(ErrorServiceMessage.formImportServiceMember.getErroCode(), ErrorServiceMessage.formImportServiceMember.getValue() + ":" + loginName);
        }
        //null退出，非表单正文退出
        String bodyType = template.getBodyType();
        if (!String.valueOf(MainbodyType.FORM.getKey()).equals(bodyType)) {
            throw new ServiceException(ErrorServiceMessage.flowValidContent.getErroCode(), ErrorServiceMessage.flowValidContent.getValue());
        }
        try {
            boolean auth = getTemplateManager().isTemplateEnabled(template.getId(), member.getId());
            if (!auth) {
                throw new ServiceException(ErrorServiceMessage.flowTempleAuth.getErroCode(), ErrorServiceMessage.flowTempleAuth.getValue() + ":" + templateCode + " " + member.getName());
            }
        } catch (BusinessException e) {
            throw new ServiceException(ErrorServiceMessage.flowTempleAuth.getErroCode(), ErrorServiceMessage.flowTempleAuth.getValue() + ":" + templateCode + " " + member.getName() + e.getMessage());
        }
        //校验数据
        //主表
        Map<String, String> masterNameValues = new HashMap<String, String>();
        for (ValueExport value : data.getValues()) {
            masterNameValues.put(value.getDisplayName(), value.getValue());
        }
        //从表
        List<Map<String, String>> slaveNameValues = new ArrayList<Map<String, String>>();
        for (SubordinateFormExport subFormExport : data.getSubordinateForms()) {

            for (RecordExport recordExport : subFormExport.getValues()) {
                Map<String, String> subDataMap = new HashMap<String, String>();
                for (ValueExport v : recordExport.getRecord()) {
                    subDataMap.put(v.getDisplayName(), v.getValue());
                }
                slaveNameValues.add(subDataMap);
            }
        }
        try {
            String viewAndOperation = getWorkflowApiManager().getNodeFormOperationName(template.getWorkflowId(), null);
            //工作流改造：试图ID.权限ID_试图ID.权限ID
            String operstionId = viewAndOperation.split("[_]")[0].split("[.]")[1];
            FormService.checkInputData(formBean.getId(), -1L, Long.valueOf(operstionId), masterNameValues, slaveNameValues, -1L);
        } catch (Exception e2) {
            throw new ServiceException(ErrorServiceMessage.formExportServiceDateInfoError.getErroCode(), ErrorServiceMessage.formExportServiceDateInfoError.getValue() + ":" + e2.getMessage());
            //throw new ServiceException("校验数据出错:"+e2.getMessage());
        }
    }

    private void checkIsExist(FormExport data, String loginName, String templateCode, CtpTemplate template, com.seeyon.cap4.form.bean.FormBean formBean, V3xOrgMember member) throws ServiceException {
        if (template.getWorkflowId() == null) {
            throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(), ErrorServiceMessage.flowTempleExist.getValue() + ":" + templateCode);
        }

        if (formBean == null) {
            //表单模板不存在
            throw new ServiceException(ErrorServiceMessage.formImportServiceIsexit.getErroCode(), ErrorServiceMessage.formImportServiceIsexit.getValue() + ":" + templateCode);
        }
        if (formBean.getFormType() == FormType.baseInfo.getKey() || formBean.getFormType() == FormType.manageInfo.getKey()) {
            throw new ServiceException(ErrorServiceMessage.flowImportException.getErroCode(),
                    ErrorServiceMessage.flowImportException.getValue());
        }
        if (member == null) {
            throw new ServiceException(ErrorServiceMessage.formImportServiceMember.getErroCode(), ErrorServiceMessage.formImportServiceMember.getValue() + ":" + loginName);
        }
        //null退出，非表单正文退出
        String bodyType = template.getBodyType();
        if (!String.valueOf(MainbodyType.FORM.getKey()).equals(bodyType)) {
            throw new ServiceException(ErrorServiceMessage.flowValidContent.getErroCode(), ErrorServiceMessage.flowValidContent.getValue());
        }
        try {
            boolean auth = getTemplateManager().isTemplateEnabled(template.getId(), member.getId());
            if (!auth) {
                throw new ServiceException(ErrorServiceMessage.flowTempleAuth.getErroCode(), ErrorServiceMessage.flowTempleAuth.getValue() + ":" + templateCode + " " + member.getName());
            }
        } catch (BusinessException e) {
            throw new ServiceException(ErrorServiceMessage.flowTempleAuth.getErroCode(), ErrorServiceMessage.flowTempleAuth.getValue() + ":" + templateCode + " " + member.getName() + e.getMessage());
        }
        //校验数据
        //主表
        Map<String, String> masterNameValues = new HashMap<String, String>();
        for (ValueExport value : data.getValues()) {
            masterNameValues.put(value.getDisplayName(), value.getValue());
        }
        //从表
        List<Map<String, String>> slaveNameValues = new ArrayList<Map<String, String>>();
        for (SubordinateFormExport subFormExport : data.getSubordinateForms()) {

            for (RecordExport recordExport : subFormExport.getValues()) {
                Map<String, String> subDataMap = new HashMap<String, String>();
                for (ValueExport v : recordExport.getRecord()) {
                    subDataMap.put(v.getDisplayName(), v.getValue());
                }
                slaveNameValues.add(subDataMap);
            }
        }
        try {
            String viewAndOperation = getWorkflowApiManager().getNodeFormOperationName(template.getWorkflowId(), null);
            //工作流改造：试图ID.权限ID_试图ID.权限ID
            String operstionId = viewAndOperation.split("[_]")[0].split("[.]")[1];
            getCap4FormManager().checkInputData(formBean.getId(), -1L, Long.valueOf(operstionId), masterNameValues, slaveNameValues, -1L);
        } catch (Exception e2) {
            throw new ServiceException(ErrorServiceMessage.formExportServiceDateInfoError.getErroCode(), ErrorServiceMessage.formExportServiceDateInfoError.getValue() + ":" + e2.getMessage());
            //throw new ServiceException("校验数据出错:"+e2.getMessage());
        }
    }

    private String[] runWorkFlow(boolean isAddFirstNode, CtpTemplate template, V3xOrgMember member, CPMatchResultVO resultVo, ColSummary summary,
                                 User user, AffairData affairData, CtpAffair affair, Map<String, Object> globalMap) throws BPMException {
        WorkflowBpmContext context = new WorkflowBpmContext();
        //zhou:调用bpm发起接口会多一个节点  现在改成false不添加节点
        isAddFirstNode=false;
        context.setAddFirstNode(isAddFirstNode);
        context.setAppName(ModuleType.collaboration.name());
        context.setStartUserId(String.valueOf(member.getId()));
        context.setStartUserName(member.getName());
        /**适应根据发起者设置单位ID流转不同分支**/
        //context.setStartAccountId(String.valueOf(member.getOrgAccountId()));
        context.setStartAccountId(String.valueOf(user.getLoginAccount()));
        context.setStartAccountName("seeyon");
        context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
        /****适应根据发起者设置单位ID流转不同分支end******/
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData("bizObject", summary);
        context.setBusinessData("ColSummary", summary);
        context.setBussinessId(String.valueOf(summary.getId()));
        context.setAppObject(summary);
        context.setBusinessData("operationType", affairData.getBusinessData().get("operationType"));
        context.setProcessTemplateId(String.valueOf(template.getWorkflowId()));
        String conditon_Str = getConditonStr(resultVo);
        logger.info("conditon_Str:=" + conditon_Str);
        context.setConditionsOfNodes(conditon_Str.toString());
        context.setVersion("2.0");
        if (affairData.getFormRecordId() != null && affairData.getFormRecordId() != -1) {
            context.setMastrid("" + affairData.getFormRecordId());
            context.setFormData("" + affairData.getFormAppId());
        }
        //防止当第一个处理节点为空节点
        context.setBusinessData("CtpAffair", affair);
        context.setBusinessData(WorkFlowEventListener.WF_APP_GLOBAL, globalMap);
        return getWorkflowApiManager().transRunCaseFromTemplate(context);
    }

    private String getConditonStr(CPMatchResultVO resultVo) {
        StringBuilder conditon_Str = new StringBuilder();
        Set<String> allSelectNodes = resultVo.getAllSelectNodes();
        conditon_Str.append("{\"condition\":[");
        for (String value : allSelectNodes) {
            conditon_Str.append("{\"nodeId\":\"" + value + "\",");
            conditon_Str.append("\"isDelete\":\"false\"},");
        }
        Set<String> allNotSelectNodes = resultVo.getAllNotSelectNodes();
        for (String value : allNotSelectNodes) {
            conditon_Str.append("{\"nodeId\":\"" + value + "\",");
            conditon_Str.append("\"isDelete\":\"true\"},");
        }
        String vauleString = StringUtils.removeEnd(conditon_Str.toString(), ",");
        return vauleString + "]}";
    }

    /**
     * @param template
     * @param member
     * @param sender
     * @return
     * @throws BPMException
     */
    private CPMatchResultVO isSelectPersonCondition(boolean isForm, CtpTemplate template, V3xOrgMember member, long sender, long masterId, User user) throws BPMException {
        //选分支,选人的判断,isPop 为true则保存待发
        WorkflowBpmContext wfContext = new WorkflowBpmContext();
        wfContext.setProcessId(null);
        wfContext.setCaseId(-1L);
        wfContext.setCurrentActivityId(null);
        wfContext.setCurrentWorkitemId(-1L);
        if (isForm) {
            wfContext.setMastrid(String.valueOf(masterId));
            wfContext.setFormData(String.valueOf(masterId));
        }
        wfContext.setStartUserId(String.valueOf(member.getId()));
        wfContext.setCurrentUserId(String.valueOf(sender));
        wfContext.setProcessTemplateId(String.valueOf(template.getWorkflowId()));
        wfContext.setAppName(ModuleType.collaboration.name());
//      wfContext.setStartAccountId(String.valueOf(member.getOrgAccountId()));
//      wfContext.setCurrentAccountId(String.valueOf(member.getOrgAccountId()));
        wfContext.setStartAccountId(String.valueOf(user.getLoginAccount()));
        wfContext.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
        CPMatchResultVO result = getWorkflowApiManager().transBeforeInvokeWorkFlow(wfContext, new CPMatchResultVO());

        return result;
    }

    private long saveMasterAndSubForm(CtpTemplate template, FormExport data,
                                      com.seeyon.cap4.form.bean.FormBean formBean, User user) throws BusinessException, SQLException {
        String operationId = getWorkflowApiManager().getNodeFormOperationName(template.getWorkflowId(), null);
        //工作流改造：试图ID.权限ID_试图ID.权限ID
        operationId = operationId.split("[_]")[0].split("[.]")[1];
        com.seeyon.cap4.form.bean.FormAuthViewBean viewBean = getCAP4FormCacheManager().getAuth(Long.parseLong(operationId));

        long masterid = 0;

        com.seeyon.cap4.form.bean.FormTableBean masterTableBean = formBean.getMasterTableBean();
        //处理主表数据
        Map<String, Object> mainDataMap = new LinkedHashMap<String, Object>();

        //webservice接口BEAN
        for (ValueExport value : data.getValues()) {
            if (StringUtils.isNotBlank(value.getValue())) {
                com.seeyon.cap4.form.bean.FormFieldBean fieldBean = masterTableBean.getFieldBeanByDisplay(value.getDisplayName());
                if (fieldBean != null) {
                    mainDataMap.put(fieldBean.getName(), value.getValue());
                }
            }
        }
        com.seeyon.cap4.form.bean.FormDataMasterBean masterData = com.seeyon.cap4.form.bean.FormDataMasterBean.newInstance(formBean);
        masterData.addFieldValue(mainDataMap);
        masterData.putExtraAttr("needProduceValue", "true");
        masterid = masterData.getId();
        List<com.seeyon.cap4.form.bean.FormTableBean> tableList = formBean.getSubTableBean();

        //循环该表单下所有重复表(子表)
        for (com.seeyon.cap4.form.bean.FormTableBean table : tableList) {
            String tableName = table.getTableName();
            //处理外部数据为A8bean，如果是重复项表，分区分组提交
            //            List<Map<String, Object>> subTableData = new ArrayList<Map<String, Object>>();
            for (SubordinateFormExport subFormExport : data.getSubordinateForms()) {

                for (RecordExport recordExport : subFormExport.getValues()) {
                    Map<String, Object> subDataMap = null;
                    for (ValueExport v : recordExport.getRecord()) {
                        com.seeyon.cap4.form.bean.FormFieldBean fieldBean = table.getFieldBeanByDisplay(v.getDisplayName());
                        if (fieldBean == null) {
                            continue;
                        }
//                        if (StringUtils.isNotBlank(v.getValue())) {
                        com.seeyon.cap4.form.bean.FormAuthViewFieldBean filedBean = viewBean.getFormAuthorizationField(fieldBean.getName());
                        if (subDataMap == null) {
                            subDataMap = new LinkedHashMap<String, Object>();
                        }
                        if (filedBean.getAccess().equals(FieldAccessType.add.name()) || filedBean.getAccess().equals(FieldAccessType.edit.name())) {
                            subDataMap.put(fieldBean.getName(), v.getValue());
                        }
//                        }
                    }
                    //                    subTableData.add(subDataMap);
                    if (subDataMap != null) {
                        subDataMap.put(SubTableField.formmain_id.getKey(), masterid);
                        List<com.seeyon.cap4.form.bean.FormDataSubBean> subDatas = masterData.getSubData(table.getTableName());
                        if (subDatas != null && subDatas.size() == 1 && subDatas.get(0).isEmpty()) {
                            subDatas.get(0).addFieldValue(subDataMap);
                        } else {
                            com.seeyon.cap4.form.bean.FormDataSubBean subData = new com.seeyon.cap4.form.bean.FormDataSubBean(subDataMap, table, masterData, true);
                            masterData.addSubData(tableName, subData);
                        }
                    }

                }
            }

        }
        getCap4FormManager().putSessioMasterDataBean(formBean, masterData);
        getCap4FormManager().getCap4FormDataManager().calcAll(formBean, masterData, viewBean, false, false, true, true);
        //getFormManager().calcAll(formBean, masterData, viewBean, false, false, true);
        getCap4FormManager().saveOrUpdateFormData(masterData, formBean.getId(), true);
        return masterid;
    }

    private long saveMasterAndSubForm(CtpTemplate template, FormExport data,
                                      FormBean formBean, User user) throws BusinessException, SQLException {
        String operationId = getWorkflowApiManager().getNodeFormOperationName(template.getWorkflowId(), null);
        //工作流改造：试图ID.权限ID_试图ID.权限ID
        operationId = operationId.split("[_]")[0].split("[.]")[1];
        FormAuthViewBean viewBean = getFormCacheManager().getAuth(Long.parseLong(operationId));

        long masterid = 0;

        FormTableBean masterTableBean = formBean.getMasterTableBean();
        //处理主表数据
        Map<String, Object> mainDataMap = new LinkedHashMap<String, Object>();

        //webservice接口BEAN
        for (ValueExport value : data.getValues()) {
            if (StringUtils.isNotBlank(value.getValue())) {
                FormFieldBean fieldBean = masterTableBean.getFieldBeanByDisplay(value.getDisplayName());
                if (fieldBean != null) {
                    mainDataMap.put(fieldBean.getName(), value.getValue());
                }
            }
        }
        FormDataMasterBean masterData = FormDataMasterBean.newInstance(formBean, viewBean);
        masterData.addFieldValue(mainDataMap);
        masterData.putExtraAttr("needProduceValue", "true");
        masterid = masterData.getId();
        List<FormTableBean> tableList = formBean.getSubTableBean();

        //循环该表单下所有重复表(子表)
        for (FormTableBean table : tableList) {
            String tableName = table.getTableName();
            //处理外部数据为A8bean，如果是重复项表，分区分组提交
            //            List<Map<String, Object>> subTableData = new ArrayList<Map<String, Object>>();
            for (SubordinateFormExport subFormExport : data.getSubordinateForms()) {

                for (RecordExport recordExport : subFormExport.getValues()) {
                    Map<String, Object> subDataMap = null;
                    for (ValueExport v : recordExport.getRecord()) {
                        FormFieldBean fieldBean = table.getFieldBeanByDisplay(v.getDisplayName());
                        if (fieldBean == null) {
                            continue;
                        }
//                        if (StringUtils.isNotBlank(v.getValue())) {
                        FormAuthViewFieldBean filedBean = viewBean.getFormAuthorizationField(fieldBean.getName());
                        if (subDataMap == null) {
                            subDataMap = new LinkedHashMap<String, Object>();
                        }
                        if (filedBean.getAccess().equals(FieldAccessType.add.name()) || filedBean.getAccess().equals(FieldAccessType.edit.name())) {
                            subDataMap.put(fieldBean.getName(), v.getValue());
                        }
//                        }
                    }
                    //                    subTableData.add(subDataMap);
                    if (subDataMap != null) {
                        subDataMap.put(SubTableField.formmain_id.getKey(), masterid);
                        List<FormDataSubBean> subDatas = masterData.getSubData(table.getTableName());
                        if (subDatas != null && subDatas.size() == 1 && subDatas.get(0).isEmpty()) {
                            subDatas.get(0).addFieldValue(subDataMap);
                        } else {
                            FormDataSubBean subData = new FormDataSubBean(subDataMap, table, masterData, true);
                            masterData.addSubData(tableName, subData);
                        }
                    }

                }
            }

        }
        getFormManager().putSessioMasterDataBean(formBean, masterData);
        getFormManager().calcAll(formBean, masterData, viewBean, false, false, true);
        FormService.saveOrUpdateFormData(masterData, formBean.getId());
        return masterid;
    }

    @Override
    public long getFlowState(String token, long flowId) throws ServiceException {
        ColSummary summary;
        try {
            summary = getColManager().getColSummaryById(flowId);
        } catch (BusinessException e) {
            throw new ServiceException(ErrorServiceMessage.documentExportFlowExist.getErroCode(),
                    ErrorServiceMessage.documentExportFlowExist.getValue());
        }
        if (summary == null) {
            throw new ServiceException(ErrorServiceMessage.documentExportFlowExist.getErroCode(),
                    ErrorServiceMessage.documentExportFlowExist.getValue());
        }
        int state = FlowUtil.getFlowState(getAffairManager(), summary);

        return NumberUtils.toLong(String.valueOf(state));
    }

    @Override
    public String[] getTemplateDefinition(String token, String templateCode) throws ServiceException {
        CtpTemplate template = getTemplateManager().getTempleteByTemplateNumber(templateCode);

        if (template == null || template.getWorkflowId() == null) {
            throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(), ErrorServiceMessage.flowTempleExist.getValue() + ":" + templateCode);
        }

        String[] result = new String[2];
        FlowExport flowExport = new FlowExport();
        ColSummary summary = createColSummary(template);

        flowExport.setFlowTitle(template.getSubject());
        flowExport.setImportantLevel(summary.getImportantLevel());
        flowExport.setFlowFolder(FlowUtil.getFolder(getDocHierarchyManager(), summary));
        flowExport.setFlowProject(FlowUtil.getProject(getProjectManager(), summary));

        String bodyType = template.getBodyType();
        //MainbodyService mainBodyService = MainbodyService.getInstance();
        if (bodyType.equals(String.valueOf(MainbodyType.HTML.getKey())) || bodyType.equals(String.valueOf(MainbodyType.TXT.getKey()))) {
            TextHtmlExport te = new TextHtmlExport();

            CtpContentAll ctpContentAll = getContentAll(template);
            te.setContext(ctpContentAll.getContent());
            flowExport.setFlowContent(te);
        } else if (bodyType.equals(String.valueOf(MainbodyType.FORM.getKey()))) {
            FormExport export = new FormExport();

            com.seeyon.cap4.form.bean.FormBean formBean4 = null;
            try {
                formBean4 = getCap4FormManager().getFormByFormCode(template);
            } catch (BusinessException e1) {
                logger.info("not in cap4", e1);
            }
            if (formBean4 != null) {
                List<DefinitionExport> define = new ArrayList<DefinitionExport>();
                List<SubordinateFormExport> subordinateFormExport = new ArrayList<SubordinateFormExport>();
                export.setSubordinateForms(subordinateFormExport);
                com.seeyon.cap4.form.bean.FormTableBean masterTableBean = formBean4.getMasterTableBean();

//                export.setFormId(summary.getFormAppid());
                export.setFormName(masterTableBean.getTableName());
                export.setDefinitions(define);

                List<com.seeyon.cap4.form.bean.FormFieldBean> fieldBean = masterTableBean.getFields();
                for (com.seeyon.cap4.form.bean.FormFieldBean field : fieldBean) {
                    FlowUtil.getDefinition(field, define);
                    logger.debug(field.getDisplay() + " " + field.getFieldType() + " ");
                }

                //获取所有子表
                List<com.seeyon.cap4.form.bean.FormTableBean> subtalbes = formBean4.getSubTableBean();
                for (com.seeyon.cap4.form.bean.FormTableBean formTableBean : subtalbes) {
                    //一个子表的定义
                    SubordinateFormExport sub = new SubordinateFormExport();
                    //子表总字段定义
                    List<DefinitionExport> subDefine = new ArrayList<DefinitionExport>();
                    //子表的一条数据记录
                    sub.setDefinitions(subDefine);
                    subordinateFormExport.add(sub);
                    List<com.seeyon.cap4.form.bean.FormFieldBean> subfieldBeans = formTableBean.getFields();
                    for (com.seeyon.cap4.form.bean.FormFieldBean subFieldValue : subfieldBeans) {
                        FlowUtil.getDefinition(subFieldValue, subDefine);
                        logger.debug(subFieldValue.getDisplay() + " " + subFieldValue.getFieldType() + " ");
                    }
                }

                flowExport.setFlowContent(export);
                try {
                    result[1] = toString(export.saveToPropertyList());
                } catch (OAInterfaceException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                FormBean formBean;
                try {
                    formBean = getFormManager().getFormByFormCode(template);
                } catch (BusinessException e) {
                    throw new ServiceException(-1, e.getLocalizedMessage());
                }
                List<DefinitionExport> define = new ArrayList<DefinitionExport>();
                List<SubordinateFormExport> subordinateFormExport = new ArrayList<SubordinateFormExport>();
                export.setSubordinateForms(subordinateFormExport);
                FormTableBean masterTableBean = formBean.getMasterTableBean();

//                export.setFormId(summary.getFormAppid());
                export.setFormName(masterTableBean.getTableName());
                export.setDefinitions(define);

                List<FormFieldBean> fieldBean = masterTableBean.getFields();
                for (FormFieldBean field : fieldBean) {
                    FlowUtil.getDefinition(field, define);
                    logger.debug(field.getDisplay() + " " + field.getFieldType() + " ");
                }

                //获取所有子表
                List<FormTableBean> subtalbes = formBean.getSubTableBean();
                for (FormTableBean formTableBean : subtalbes) {
                    //一个子表的定义
                    SubordinateFormExport sub = new SubordinateFormExport();
                    //子表总字段定义
                    List<DefinitionExport> subDefine = new ArrayList<DefinitionExport>();
                    //子表的一条数据记录
                    sub.setDefinitions(subDefine);
                    subordinateFormExport.add(sub);
                    List<FormFieldBean> subfieldBeans = formTableBean.getFields();
                    for (FormFieldBean subFieldValue : subfieldBeans) {
                        FlowUtil.getDefinition(subFieldValue, subDefine);
                        logger.debug(subFieldValue.getDisplay() + " " + subFieldValue.getFieldType() + " ");
                    }
                }

                flowExport.setFlowContent(export);
                try {
                    result[1] = toString(export.saveToPropertyList());
                } catch (OAInterfaceException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        } else {
            CtpContentAll ctpContentAll = getContentAll(template);
//            te.setContext(ctpContentAll.getContent());
            TextAttachmentExport te = convertOfficeBody(bodyType,
                    ctpContentAll.getContent(), ctpContentAll.getCreateDate());
            flowExport.setFlowContent(te);
        }


        flowExport.setFlowFolder(FlowUtil.getFolder(getDocHierarchyManager(), summary));
        flowExport.setFlowProject(FlowUtil.getProject(getProjectManager(), summary));
        List<AttachmentExport> ret = FlowUtil.createAttachment(getFileManager(), getAttachmentManager(), template.getId());
        flowExport.setAttachmentList(ret);
        try {
            result[0] = toString(flowExport.saveToPropertyList());
        } catch (OAInterfaceException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private CtpContentAll getContentAll(CtpTemplate template) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("ID", template.getBody());
        List<CtpContentAll> contentAllList = DBAgent.find("from CtpContentAll c  where c.id=:ID", param);
        CtpContentAll ctpContentAll = contentAllList.get(0);
        return ctpContentAll;
    }

    /**
     * 取得office类型协同的对象
     */
    public static TextAttachmentExport convertOfficeBody(String bodyType, String content, Date createDate) {
        TextAttachmentExport te = new TextAttachmentExport();
        try {
            File file = getFileManager().getStandardOffice(Long.valueOf(content), createDate);
            te.setId(Long.valueOf(content));
            te.setType(convertBodyType(bodyType));
            te.setDownloadpath("");// TODO 
            if (file != null) {
                te.setAbsolutepath(file.getAbsolutePath());
                String name = file.getName() == null ? "" : file.getName();
                int loc = name.lastIndexOf('.');
                if (loc > -1 && loc != name.length())
                    te.setFilesuffix(name.substring(loc));
                else
                    te.setFilesuffix("");

            } else {
                te.setFilesuffix("");
                logger.info("文件id为：【" + content + "】，创建日期为【" + createDate + "】的文件未找到");
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return te;
    }

    /**
     * 取得对应正文类型常量
     */
    public static int convertBodyType(String bodyType) {
        int ret = MainbodyType.OfficeExcel.getKey();
        if (bodyType.equals(MainbodyType.OfficeExcel.getKey() + "")) {
            ret = TextAttachmentExport.C_Text_XSL;
        } else if (bodyType.equals(MainbodyType.OfficeWord.getKey() + "")) {
            ret = TextAttachmentExport.C_Text_DOC;
        } else if (bodyType.equals(MainbodyType.WpsExcel.getKey() + "")) {
            ret = TextAttachmentExport.C_Text_ET;
        } else if (bodyType.equals(MainbodyType.WpsWord.getKey() + "")) {
            ret = TextAttachmentExport.C_Text_WPS;
        } else if (bodyType.equals(MainbodyType.Pdf.getKey() + "")) {
            ret = 5;
        }

        return ret;
    }

    private String toString(PropertyList props) {
        StringWriter writer = new StringWriter();
        try {
            props.saveXMLToStream(writer);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return writer.toString();

    }

    @Override
    public String getTemplateXml(String templateCode) throws ServiceException {
        CtpTemplate template = getTemplateManager().getTempleteByTemplateNumber(templateCode);
        String result = "";
        if (template == null || template.getWorkflowId() == null) {
            throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(), ErrorServiceMessage.flowTempleExist.getValue() + ":" + templateCode);
        }
        String bodyType = template.getBodyType();
        if (bodyType.equals(String.valueOf(MainbodyType.FORM.getKey()))) {
            FormExport export = new FormExport();

            FormBean formBean;
            com.seeyon.cap4.form.bean.FormBean formBean4 = null;
            try {
                formBean4 = getCap4FormManager().getFormByFormCode(template);
            } catch (BusinessException e1) {
                logger.info("not in cap4", e1);
            }
            if (formBean4 != null) {
                List<DefinitionExport> define = new ArrayList<DefinitionExport>();
                List<SubordinateFormExport> subordinateFormExport = new ArrayList<SubordinateFormExport>();
                export.setSubordinateForms(subordinateFormExport);
                com.seeyon.cap4.form.bean.FormTableBean masterTableBean = formBean4.getMasterTableBean();

                export.setFormName(masterTableBean.getTableName());
                export.setDefinitions(define);

                List<com.seeyon.cap4.form.bean.FormFieldBean> fieldBean = masterTableBean.getFields();
                for (com.seeyon.cap4.form.bean.FormFieldBean field : fieldBean) {
                    FlowUtil.getDefinition(field, define);
                    logger.debug(field.getDisplay() + " " + field.getFieldType() + " ");
                }

                //获取所有子表
                List<com.seeyon.cap4.form.bean.FormTableBean> subtalbes = formBean4.getSubTableBean();
                for (com.seeyon.cap4.form.bean.FormTableBean formTableBean : subtalbes) {
                    //一个子表的定义
                    SubordinateFormExport sub = new SubordinateFormExport();
                    //子表总字段定义
                    List<DefinitionExport> subDefine = new ArrayList<DefinitionExport>();
                    //子表的一条数据记录
                    sub.setDefinitions(subDefine);
                    subordinateFormExport.add(sub);
                    List<com.seeyon.cap4.form.bean.FormFieldBean> subfieldBeans = formTableBean.getFields();
                    for (com.seeyon.cap4.form.bean.FormFieldBean subFieldValue : subfieldBeans) {
                        FlowUtil.getDefinition(subFieldValue, subDefine);
                        logger.debug(subFieldValue.getDisplay() + " " + subFieldValue.getFieldType() + " ");
                    }
                }
            } else {
                try {
                    formBean = getFormManager().getFormByFormCode(template);
                } catch (BusinessException e) {
                    throw new ServiceException(-1, e.getLocalizedMessage());
                }
                List<DefinitionExport> define = new ArrayList<DefinitionExport>();
                List<SubordinateFormExport> subordinateFormExport = new ArrayList<SubordinateFormExport>();
                export.setSubordinateForms(subordinateFormExport);
                FormTableBean masterTableBean = formBean.getMasterTableBean();

                export.setFormName(masterTableBean.getTableName());
                export.setDefinitions(define);

                List<FormFieldBean> fieldBean = masterTableBean.getFields();
                for (FormFieldBean field : fieldBean) {
                    FlowUtil.getDefinition(field, define);
                    logger.debug(field.getDisplay() + " " + field.getFieldType() + " ");
                }

                //获取所有子表
                List<FormTableBean> subtalbes = formBean.getSubTableBean();
                for (FormTableBean formTableBean : subtalbes) {
                    //一个子表的定义
                    SubordinateFormExport sub = new SubordinateFormExport();
                    //子表总字段定义
                    List<DefinitionExport> subDefine = new ArrayList<DefinitionExport>();
                    //子表的一条数据记录
                    sub.setDefinitions(subDefine);
                    subordinateFormExport.add(sub);
                    List<FormFieldBean> subfieldBeans = formTableBean.getFields();
                    for (FormFieldBean subFieldValue : subfieldBeans) {
                        FlowUtil.getDefinition(subFieldValue, subDefine);
                        logger.debug(subFieldValue.getDisplay() + " " + subFieldValue.getFieldType() + " ");
                    }
                }
            }
            result = toXml(export);
        }
        return result;
    }

    private String toXml(FormExport export) {
        StringWriter writer = new StringWriter();
        try {
            SaveFormToXml.getInstance().saveXMLToStream(writer, export);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return writer.toString();

    }
}
