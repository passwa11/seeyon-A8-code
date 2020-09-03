package com.seeyon.apps.collaboration.manager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.temp.manager.XkjtTempManager;
import com.seeyon.apps.ext.temp.manager.XkjtTempManagerImpl;
import com.seeyon.apps.ext.temp.po.XkjtTemp;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.ctp.rest.util.CtpAffairUntil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.ai.event.AIRemindEvent;
import com.seeyon.apps.collaboration.bo.ColInfo;
import com.seeyon.apps.collaboration.bo.DeleteAjaxTranObj;
import com.seeyon.apps.collaboration.bo.FormLockParam;
import com.seeyon.apps.collaboration.bo.LockObject;
import com.seeyon.apps.collaboration.bo.MessageCommentParam;
import com.seeyon.apps.collaboration.bo.QuerySummaryParam;
import com.seeyon.apps.collaboration.bo.TrackAjaxTranObj;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.dao.ColDao;
import com.seeyon.apps.collaboration.enums.ColHandleType;
import com.seeyon.apps.collaboration.enums.ColListType;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.enums.CommentExtAtt3Enum;
import com.seeyon.apps.collaboration.event.CollaborationAddCommentEvent;
import com.seeyon.apps.collaboration.event.CollaborationAffairsAssignedEvent;
import com.seeyon.apps.collaboration.event.CollaborationAppointStepBackEvent;
import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.collaboration.event.CollaborationDelEvent;
import com.seeyon.apps.collaboration.event.CollaborationProcessEvent;
import com.seeyon.apps.collaboration.event.CollaborationStepBackEvent;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.apps.collaboration.event.CollaborationTakeBackEvent;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.trace.dao.TraceDao;
import com.seeyon.apps.collaboration.trace.enums.WorkflowTraceEnums;
import com.seeyon.apps.collaboration.trace.manager.TraceWorkflowDataManager;
import com.seeyon.apps.collaboration.trace.manager.TraceWorkflowManager;
import com.seeyon.apps.collaboration.util.AttachmentEditUtil;
import com.seeyon.apps.collaboration.util.ColSelfUtil;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.collaboration.vo.ColListSimpleVO;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.apps.collaboration.vo.NewCollTranVO;
import com.seeyon.apps.collaboration.vo.NodePolicyVO;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.collaboration.vo.WebEntity4QuickIndex4Col;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.apps.index.util.Convertor;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.apps.webmail.api.WebmailApi;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentInterface;
import com.seeyon.ctp.common.content.ContentSaveOrUpdateRet;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.affair.AffairDao;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.lock.manager.LockManagerImpl;
import com.seeyon.ctp.common.lock.manager.LockState;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.HandWriteManager;
import com.seeyon.ctp.common.office.OfficeLockManager;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.permission.bo.CustomAction;
import com.seeyon.ctp.common.permission.bo.NodePolicy;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.enums.PermissionAction;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.po.processlog.ProcessLog;
import com.seeyon.ctp.common.po.processlog.ProcessLogDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseModelVO;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.CollaborationTemplateManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.modules.engin.relation.FormRelationManager;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.form.service.FormService;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.webmodel.WebEntity4QuickIndex;
import com.seeyon.ctp.portal.portlet.PortletConstants.PortletCategory;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.peoplerelate.domain.PeopleRelate;
import com.seeyon.v3x.peoplerelate.manager.PeopleRelateManager;
import com.seeyon.v3x.system.signet.manager.SignetManager;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMAbstractNode;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;


public class ColManagerImpl implements ColManager {
    private static Log LOG = CtpLogFactory.getLog(ColManagerImpl.class);
    private CollaborationTemplateManager collaborationTemplateManager;
    private WorkflowApiManager wapi;
    private OrgManager orgManager;
    private PermissionManager permissionManager;
    private AffairManager affairManager;
    private AttachmentManager attachmentManager;
    private CtpTrackMemberManager trackManager;
    private AppLogManager appLogManager;
    private ProcessLogManager processLogManager;
    private SuperviseManager superviseManager;
    private TemplateManager templateManager;
    private WorkTimeManager workTimeManager;
    private FileManager fileManager;
    private CommentManager commentManager;
    private ColDao colDao;
    private AffairDao affairDao;
    private IndexManager indexManager;
    private FormManager formManager;
    private CAPFormManager capFormManager;
    private ColMessageManager colMessageManager;
    private ColPubManager colPubManager;
    private CustomizeManager customizeManager;
    private FormRelationManager formRelationManager;
    private UserMessageManager userMessageManager;
    private MainbodyManager ctpMainbodyManager;
    private ISignatureHtmlManager iSignatureHtmlManager;
    private SignetManager signetManager;
    //处理后不允许被撤销的节点权限列表，XX审核的节点权限
    private List cannotRepealList;
    private TraceWorkflowDataManager colTraceWorkflowManager;
    private TraceWorkflowManager traceWorkflowManager;
    private OfficeLockManager officeLockManager;
    private PeopleRelateManager peopleRelateManager;
    private ColLockManager colLockManager;
    private LockManagerImpl lockManager;
    private ColBatchUpdateAnalysisTimeManager colBatchUpdateAnalysisTimeManager;
    private ColTaskManager colTaskManager;

    private XkjtManager xkjtManager;


    public XkjtManager getXkjtManager() {
        return xkjtManager;
    }

    public void setXkjtManager(XkjtManager xkjtManager) {
        this.xkjtManager = xkjtManager;
    }


    private TraceDao traceDao;

    private ProjectApi projectApi;

    private DocApi docApi;

    private WebmailApi webmailApi;

    private HandWriteManager handWriteManager;

    public void setHandWriteManager(HandWriteManager handWriteManager) {
        this.handWriteManager = handWriteManager;
    }

    public void setWebmailApi(WebmailApi webmailApi) {
        this.webmailApi = webmailApi;
    }

    public PeopleRelateManager getPeopleRelateManager() {
        return peopleRelateManager;
    }

    public void setPeopleRelateManager(PeopleRelateManager peopleRelateManager) {
        this.peopleRelateManager = peopleRelateManager;
    }

    public ColBatchUpdateAnalysisTimeManager getColBatchUpdateAnalysisTimeManager() {
        return colBatchUpdateAnalysisTimeManager;
    }

    public void setColBatchUpdateAnalysisTimeManager(ColBatchUpdateAnalysisTimeManager colBatchUpdateAnalysisTimeManager) {
        this.colBatchUpdateAnalysisTimeManager = colBatchUpdateAnalysisTimeManager;
    }

    public LockManagerImpl getLockManagerImpl() {
        return lockManager;
    }

    public void setLockManagerImpl(LockManagerImpl lockManagerImpl) {
        this.lockManager = lockManagerImpl;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public void setProjectApi(ProjectApi projectApi) {
        this.projectApi = projectApi;
    }

    public void setTraceDao(TraceDao traceDao) {
        this.traceDao = traceDao;
    }

    public TraceWorkflowManager getTraceWorkflowManager() {
        return traceWorkflowManager;
    }

    public void setTraceWorkflowManager(TraceWorkflowManager traceWorkflowManager) {
        this.traceWorkflowManager = traceWorkflowManager;
    }

    public void setColTraceWorkflowManager(TraceWorkflowDataManager colTraceWorkflowManager) {
        this.colTraceWorkflowManager = colTraceWorkflowManager;
    }

    public CustomizeManager getCustomizeManager() {
        return customizeManager;
    }

    public void setSignetManager(SignetManager signetManager) {
        this.signetManager = signetManager;
    }

    public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
        this.iSignatureHtmlManager = iSignatureHtmlManager;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }

    public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
        this.ctpMainbodyManager = ctpMainbodyManager;
    }

    public void setCollaborationTemplateManager(CollaborationTemplateManager collaborationTemplateManager) {
        this.collaborationTemplateManager = collaborationTemplateManager;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }


    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }


    public void setCommentManager(CommentManager commentManager) {
        this.commentManager = commentManager;
    }

    public void setColDao(ColDao colDao) {
        this.colDao = colDao;
    }

    public void setAffairDao(AffairDao affairDao) {
        this.affairDao = affairDao;
    }

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    public void setFormManager(FormManager formManager) {
        this.formManager = formManager;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public void setColMessageManager(ColMessageManager colMessageManager) {
        this.colMessageManager = colMessageManager;
    }

    public void setColPubManager(ColPubManager colPubManager) {
        this.colPubManager = colPubManager;
    }

    public void setFormRelationManager(FormRelationManager formRelationManager) {
        this.formRelationManager = formRelationManager;
    }

    public void setCannotRepealList(List cannotRepealList) {
        this.cannotRepealList = cannotRepealList;
    }

    public void setOfficeLockManager(OfficeLockManager officeLockManager) {
        this.officeLockManager = officeLockManager;
    }

    public void setColLockManager(ColLockManager colLockManager) {
        this.colLockManager = colLockManager;
    }

    public void saveColSummary(ColSummary colSummary) {
        colDao.saveColSummary(colSummary);
    }

    public void setColTaskManager(ColTaskManager colTaskManager) {
        this.colTaskManager = colTaskManager;
    }

    public void deleteColSummary(ColSummary colSummary) {
        colDao.deleteColSummary(colSummary);
    }

    @Override
    public void updateColInfo(ColInfo info) {
        if (null != info.getSummary()) {
            updateColSummary(info.getSummary());
        }
    }

    @Override
    public void updateColSummary(ColSummary colSummary) {
        colDao.updateColSummary(colSummary);
    }

    /**
     * 获取新建节点权限vo对象
     */
    @Override
    public NodePolicyVO getNewColNodePolicy(Long loginAcctountId) {
        NodePolicyVO nodePolicy = null;
        try {
            Permission permission = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), "newCol", loginAcctountId);
            nodePolicy = new NodePolicyVO(permission);
        } catch (BusinessException e) {
            LOG.error("", e);
        }
        return nodePolicy;
    }

    /**
     * @param sendtype 协同发送类型
     * @throws BusinessException
     * @throws SQLException
     * @info 协同信息
     */
    @SuppressWarnings("unchecked")
    public void transSend(ColInfo info, ColConstant.SendType sendType) throws BusinessException {
        boolean isNew = info.getNewBusiness();
        ColSummary summary = info.getSummary();
        if (!isNew) {// 非新建环节，删除原有的

            CtpAffair senderAffair = info.getSenderAffair();
            if (senderAffair == null || StateEnum.col_waitSend.getKey() != senderAffair.getState().intValue()) {
                return;
            }
            int subState = senderAffair.getSubState().intValue();
            if (subState == SubStateEnum.col_pending_specialBacked.getKey() || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {

                deleteAttachment4SpecialBack(summary.getId());

            } else {

                // affairManager.deleteByObjectId(ApplicationCategoryEnum.collaboration, summary.getId());
                //撤销的需要删除   删除催办日志 下面两行是催办日志
                if (subState == SubStateEnum.col_waitSend_cancel.getKey()) {
                    this.superviseManager.deleteLogs(summary.getId());
                    this.superviseManager.deleteSuperviseAllById(summary.getId());
                    //删除意见  附件的referee是协同ID subreference 是ctp_comment_all的ID
                    String forwardMember = summary.getForwardMember();
                    Integer replyCounts = summary.getReplyCounts();
                    if (Strings.isNotBlank(forwardMember)) {
                        //删除非转发的意见
                        List<Comment> comments = commentManager.getCommentAllByModuleId(ModuleType.collaboration, summary.getId());
                        for (Comment c : comments) {
                            if (c.getForwardCount() < 1) {
                                commentManager.deleteComment(ModuleType.collaboration, c.getId());
                                if (replyCounts > 0) {
                                    replyCounts--;
                                }
                            }
                        }
                    } else {
                        commentManager.deleteCommentAllByModuleId(ModuleType.collaboration, summary.getId());
                        replyCounts = 0;
                    }
                    summary.setReplyCounts(replyCounts);
                    //删除附件
                    List<Attachment> attachment = attachmentManager.getByReference(summary.getId());
                    for (Attachment att : attachment) {
                        if (!AttachmentEditUtil.CONTENT_ATTACHMENTSUBRE.equals(att.getSubReference().toString())
                                && !Integer.valueOf(ApplicationCategoryEnum.cap4Form.key()).equals(att.getCategory())
                                && !Integer.valueOf(ModuleType.form.getKey()).equals(att.getCategory())) {
                            this.attachmentManager.deleteById(att.getId());
                        }
                    }
                } else {
                    //回退再发起删除标题区下方的附件
                    attachmentManager.deleteByReference(summary.getId(), summary.getId());
                }
            }
        }

        summary.setTempleteId(info.gettId());

        //保存评论回复
        Comment comment = ContentUtil.getCommnetFromRequest(ContentUtil.OperationType.send, null, summary.getId());
        //如果附言内容是 "附言(500字以内)" 说明是没有设置附言
        if (Strings.isNotBlank(comment.getContent()) && !ResourceUtil.getString("collaboration.newcoll.fywbzyl").equals(comment.getContent().replace("\n", ""))) {
            if (comment.getId() != null && comment.getId() != -1) {
                ContentUtil.deleteCommentAllByModuleIdAndCtype(ModuleType.collaboration, summary.getId());
            }
            comment.setId(UUIDLong.longUUID());
            info.setComment(comment);
        }


        summary.setCanDueReminder(false);
        summary.setAudited(false);
        summary.setVouch(CollaborationEnum.vouchState.defaultValue.ordinal());


        colPubManager.transSendColl(sendType, info);
    }

    public void transSendImmediate(String _summaryIds, String _affairIds, boolean sentFlag,
                                   String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowNewflowInput, String toReGo)
            throws BusinessException {

        CtpAffair affair = affairManager.get(Long.valueOf(_affairIds));
        transSendImmediate(_summaryIds, affair, sentFlag, workflowNodePeoplesInput, workflowNodeConditionInput, workflowNewflowInput, toReGo);

    }

    /* (non-Javadoc)
     * @see com.seeyon.apps.collaboration.manager.ColManager#transSendImmediate(java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void transSendImmediate(String _summaryIds, CtpAffair affair, boolean sentFlag,
                                   String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowNewflowInput, String toReGo)
            throws BusinessException {

        Long summaryId = Long.valueOf(_summaryIds);
        ColSummary colSummary = getColSummaryById(summaryId);


        //多端重复操作, 防止错误数据生成
        if (StateEnum.col_waitSend.getKey() != affair.getState().intValue()) {
            return;
        }

        int subState = affair.getSubState().intValue();

        String processId = colSummary.getProcessId();

        if (subState == SubStateEnum.col_pending_specialBacked.getKey() || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {

        } else {
            if (subState == SubStateEnum.col_waitSend_cancel.getKey()) {
//	            if (Strings.isNotBlank(processId)) {
//	                processLogManager.deleteLog(Long.parseLong(colSummary.getProcessId()));
//	            }
                //删除历史催办日志
                superviseManager.deleteLogs(colSummary.getId());
                String forwardMember = colSummary.getForwardMember();
                Integer replyCounts = colSummary.getReplyCounts();
                if (Strings.isNotBlank(forwardMember)) {
                    //删除非转发的意见
                    List<Comment> comments = commentManager.getCommentAllByModuleId(ModuleType.collaboration, colSummary.getId());
                    for (Comment c : comments) {
                        if (c.getForwardCount() < 1) {
                            commentManager.deleteComment(ModuleType.collaboration, c.getId());
                            if (replyCounts > 0) {
                                replyCounts--;
                            }
                        }
                    }
                } else {
                    //删除意见
                    List<Comment.CommentType> types = new ArrayList<Comment.CommentType>();
                    types.add(Comment.CommentType.reply);
                    types.add(Comment.CommentType.comment);
                    commentManager.deleteCommentAllByModuleIdAndCtypes(ModuleType.collaboration, summaryId, types);
                    //列表立即发送没有删除发起者附言，并且replyCounts没有统计发起者附言，因此可以直接设置为0
                    replyCounts = 0;
                }
                colSummary.setReplyCounts(replyCounts);
            }

            //删除流程中的历史事项
            // affairManager.deleteByObjectId(ApplicationCategoryEnum.collaboration, colSummary.getId());

            if (subState == SubStateEnum.col_waitSend_cancel.getKey()) {
                List<Attachment> l = attachmentManager.getByReference(summaryId);
                List<Comment> comments = commentManager.getCommentList(ModuleType.collaboration, summaryId);
                if (comments.size() > 0) {
                    List<Long> attSend = new ArrayList<Long>();
                    for (Comment com : comments) {
                        if (com.getCtype() == Comment.CommentType.sender.getKey()) {
                            attSend.add(com.getId());
                        }
                    }
                    if (Strings.isNotEmpty(l)) {//TODO 这个地方可能是个性能点，但是这种情况比较少，平台也没相应的接口，就先这样吧。SP1再处理。
                        for (Attachment m : l) {
                            //待发列表中直接发送，不删除附言区域的附件
                            if (!summaryId.equals(m.getSubReference()) && !attSend.contains(m.getSubReference())) {
                                attachmentManager.deleteByReference(summaryId, m.getSubReference());
                            }
                        }
                    }
                }
            }
            //撤销的协同重新计算ReplyCountsOA-154126
            colSummary.setReplyCounts(0);
        }

        ColInfo info = new ColInfo();
        info.setCurrentUser(AppContext.getCurrentUser());
        info.setSenderAffair(affair);
        info.setSummary(colSummary);
        info.setCurrentProcessId(colSummary.getProcessId() == null ? 0L : Long.valueOf(colSummary.getProcessId()));
        info.setWorkflowNewflowInput(workflowNewflowInput);
        info.setWorkflowNodeConditionInput(workflowNodeConditionInput);
        info.setWorkflowNodePeoplesInput(workflowNodePeoplesInput);
        info.setTrackType(affair.getTrack());
        colPubManager.transSendColl(ColConstant.SendType.immediate, info);

    }

    private void deleteAttachment4SpecialBack(Long id) {
        try {
            this.attachmentManager.deleteByReference(id, id);
            List<Long> commentIds = ContentUtil.getSenderCommentIdByModuleIdAndCtype(ModuleType.collaboration, id);
            if (Strings.isNotEmpty(commentIds)) {
                for (Long cid : commentIds) {
                    attachmentManager.deleteByReference(id, cid);
                }
            }
        } catch (BusinessException e) {
            LOG.error("", e);
        }
    }

    /**
     * 获取前台页面的附件
     *
     * @return
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    public String saveAttachmentFromDomain(ApplicationCategoryEnum type, Long module_id) throws BusinessException {

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
            result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.collaboration, module_id, module_id, assDocGroup);
        } catch (Exception e) {
            LOG.error("", e);
            throw new BusinessException(ResourceUtil.getString("collaboration.error.common.attachment.create"));//创建附件出错
        }

        return attachmentManager.create(result);
    }

    /**
     * 完整删除一个协同，包括：
     * 1. ColSummary
     * 2. 正文组件相关内容（正文/表单、意见/回复、流程）
     * 3. 流程日志
     * 4. 所有Affair
     * 5. 所有附件
     *
     * @param summary
     * @throws BusinessException
     */
    private void deleteAllColSummaryById(ColSummary summary) throws BusinessException {
        colDao.deleteColSummaryById(summary.getId());

        ContentUtil.contentDelete(ModuleType.collaboration, summary.getId(), summary.getProcessId());

        if (Strings.isNotBlank(summary.getProcessId())) {
            processLogManager.deleteLog(Long.parseLong(summary.getProcessId()));
        }

        affairManager.deleteByObjectId(ApplicationCategoryEnum.collaboration, summary.getId());

        //删除原有附件
        this.attachmentManager.deleteByReference(summary.getId());
    }

    public CtpAffair getAffairById(long affairId) throws BusinessException {
        return getAffairById(affairId, false);
    }

    private CtpAffair getAffairById(long affairId, boolean isHistoryFlag) throws BusinessException {
        CtpAffair affair = null;
        if (isHistoryFlag) {
            affair = affairManager.getByHis(affairId);
        } else {
            affair = affairManager.get(affairId);
            if (AppContext.hasPlugin("fk") && affair == null) {
                affair = affairManager.getByHis(affairId);
            }
        }
        if (affair == null)
            return null;

        return affair;
    }

    @Override
    public ColSummary getColSummaryById(Long id) {
        try {
            return this.getSummaryById(id);
        } catch (BusinessException e) {
            return null;
        }
    }

    public ColSummary getColSummaryByIdHistory(Long id) {
        ColSummary s = null;
        try {
            ColDao colDaoFK = null;
            if (AppContext.hasPlugin("fk")) {
                colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
            }
            if (colDaoFK != null) {
                s = colDaoFK.getColSummaryByIdHis(id);
            }
        } catch (Exception e) {
            return null;
        }
        return s;
    }

    @Override
    public String getProcessId(String id) throws BusinessException {
        if (Strings.isBlank(id)) {
            return null;
        }

        ColSummary summary = this.getColSummaryById(Long.parseLong(id));
        if (summary != null) {
            return summary.getProcessId();
        }
        return null;
    }

    @Override
    public List<String> getForwardMemberNames(String forwardMemberIds) {
        if (Strings.isNotBlank(forwardMemberIds)) {
            String[] forwardMembers = forwardMemberIds.split(",");
            List<String> forwardMemberNames = new ArrayList<String>(forwardMembers.length);

            for (String m : forwardMembers) {
                long memberId = Long.parseLong(m);
                try {
                    String memberName = Functions.showMemberName(memberId);
                    forwardMemberNames.add(Strings.escapeNULL(memberName, ""));
                } catch (Exception e) {
                    LOG.error("查询人员信息：" + memberId, e);
                }
            }
            return forwardMemberNames;
        }

        return null;
    }

    public List<Attachment> getAttachmentsById(long summaryId) {
        return attachmentManager.getByReference(summaryId);
    }

    public Long getPermissionAccountId(long loginAccount, ColSummary summary) {
        //        Long flowPermAccountId = loginAccount;
        //        Long templeteId = summary.getTempleteId();
        //        if(templeteId != null){
        //            Template templete = templateManager.getById(templeteId);
        //            if(templete != null){
        //                flowPermAccountId = templete.getOrgAccountId();
        //            }
        //        }
        //        else{
        //            if(summary.getOrgAccountId() != null){
        //                flowPermAccountId = summary.getOrgAccountId();
        //            }
        //        }
        return 0l;
    }

    @Override
    /**
     *获取保存到本地和打印的权限
     * @param summary
     * @param nodePermissionPolicy
     * @param lenPotents  三位字符串，1:待定；2:下载;3：打印
     * @param affair
     * @return
     */
    public Map<String, Object> getSaveToLocalOrPrintPolicy(ColSummary summary,
                                                           String nodePermissionPolicy,
                                                           String lenPotents,
                                                           CtpAffair affair,
                                                           String openForm) throws BusinessException {
        Map<String, Object> map = new HashMap<String, Object>();
        boolean officecanPrint = false;
        Boolean officecanSaveLocal = true;
        try {
            Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            Permission fp = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(),
                    nodePermissionPolicy, accountId);
            if (fp != null) {
                String baseAction = fp.getBasicOperation();
                //是否有打印权限，根据v3x_affair表中的state字段来判断
                //节点对文档中心是否有打印权限
                if (Strings.isNotBlank(lenPotents) && ColOpenFrom.docLib.name().equals(openForm)) {
                    officecanPrint = "0".equals(lenPotents.substring(2, 3)) ? false : true;
                    officecanSaveLocal = "0".equals(lenPotents.substring(1, 2)) ? false : true;
                } else {
                    int state = affair.getState().intValue();
                    if (StateEnum.col_waitSend.getKey() == state
                            || StateEnum.col_sent.getKey() == state
                            || baseAction.indexOf("Print") >= 0
                            || ColOpenFrom.supervise.name().equals(openForm)) {
                        officecanPrint = true;
                    }
                    officecanSaveLocal = true;
                }
            }
            map.put("officecanPrint", officecanPrint);
            map.put("officecanSaveLocal", officecanSaveLocal);
        } catch (Exception e) {
            LOG.error("获取协同的打印权限异常,affairId:" + affair.getId(), e);
            throw new BusinessException("获取协同的打印权限异常,affairId:" + affair.getId());
        }
        return map;
    }

    public ColSummary getSummaryByCaseId(Long caseId) throws BusinessException {
        ColSummary s = colDao.getSummaryByCaseId(caseId);
        ColDao colDaoFK = null;
        if (AppContext.hasPlugin("fk")) {
            colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
        }
        if (s == null && colDaoFK != null) {
            s = colDaoFK.getSummaryByCaseId(caseId);
        }
        return s;
    }

    public FlipInfo getSentAffairs(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        //设置用户id
        String userId = query.get(ColQueryCondition.currentUser.name());
        if (Strings.isBlank(userId)) {
            User user = AppContext.getCurrentUser();
            userId = String.valueOf(user.getId());
        }
        query.put(ColQueryCondition.currentUser.name(), userId);
        query.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_sent.key()));

        List<ColSummaryVO> result = null;

        boolean dumpData = Boolean.parseBoolean(query.get("dumpData"));
        if (dumpData) {
            ColDao colDaoFK = (ColDao) AppContext.getBean("colDaoFK");

            //查询转储数据时，要求此处展示待发与已发
            query.put(ColQueryCondition.state.name(), StateEnum.col_sent.getKey() + "," + StateEnum.col_waitSend.getKey());
            if (colDaoFK != null) {
                result = colDaoFK.queryByConditionHis(flipInfo, query);
            }
        } else {
            result = colDao.queryByCondition(flipInfo, query);
        }

        if (flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    public FlipInfo getSentList(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        FlipInfo info = getSentAffairs(flipInfo, query);
        convertColSummaryVO2ListSimpleVO(info);
        /**项目：徐矿集团 【屏蔽按钮】 作者：jiangchenxi 时间：2019年3月12日 start*/
        List<ColListSimpleVO> affairs = info.getData();
        for (ColListSimpleVO colListSimpleVO : affairs) {
            String id = colListSimpleVO.getProcessId();
            long nodePolicyId = 1L;
            if (Strings.isNotBlank(id)) {
                nodePolicyId = Long.parseLong(id);
            }
            boolean open = xkjtManager.isOpen(colListSimpleVO.getAffairId());
            if (!open) {
                colListSimpleVO.setProcessId("");
                colListSimpleVO.setCurrentNodesInfo("");
            }
        }
        /**项目：徐矿集团 【屏蔽按钮】 作者：jiangchenxi 时间：2019年3月12日 end*/
        return info;
    }


    public FlipInfo getSentlist4Quote(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        //设置用户id
        String userId = query.get(ColQueryCondition.currentUser.name());
        if (Strings.isBlank(userId)) {
            User user = AppContext.getCurrentUser();
            userId = String.valueOf(user.getId());
        }
        String state = (String) query.get("state");
        if ("3".equals(state)) {
            state = String.valueOf(StateEnum.col_pending.key());
        } else if ("4".equals(state)) {
            state = String.valueOf(StateEnum.col_done.key());
        } else if ("2".equals(state)) {
            state = String.valueOf(StateEnum.col_sent.key());
        }
        query.put(ColQueryCondition.state.name(), state);
        query.put(ColQueryCondition.list4Quote.name(), String.valueOf(Boolean.TRUE));
        query.put(ColQueryCondition.currentUser.name(), String.valueOf(userId));

        List<ColSummaryVO> result = null;
        boolean dumpData = Boolean.parseBoolean(query.get("dumpData"));
        if (dumpData) {
            ColDao colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
            if (colDaoFK != null) {
                //查询转储数据时，要求此处展示待办、已办、已发
                query.put(ColQueryCondition.state.name(), StateEnum.col_pending.getKey() + "," + StateEnum.col_done.getKey() + "," + StateEnum.col_sent.getKey());
                result = colDaoFK.queryByConditionHis(flipInfo, query);
            }
        } else {
            result = colDao.queryByCondition(flipInfo, query);
        }


        if (flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    public FlipInfo getPendingAffairs(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        //设置用户id
        String userId = query.get(ColQueryCondition.currentUser.name());
        if (Strings.isBlank(userId)) {
            User user = AppContext.getCurrentUser();
            userId = String.valueOf(user.getId());
        }
        query.put(ColQueryCondition.currentUser.name(), userId);
        query.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_pending.key()));
        //需要查询代理时，默认加上该参数
        String hasNeedAgent = query.get("hasNeedAgent");
        if (Strings.isBlank(hasNeedAgent)) {
            hasNeedAgent = "true";
        }
        query.put("hasNeedAgent", hasNeedAgent);
        List<ColSummaryVO> result = colDao.queryByCondition(flipInfo, query);
        for (ColSummaryVO csvo : result) {
            String nodeName = csvo.getNodePolicy();
            ColSummary summary = csvo.getSummary();
            long flowPermAccountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary.getOrgAccountId(), summary.getPermissionAccountId());
            Permission permisson = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), nodeName, flowPermAccountId);
            /**项目：徐矿集团 【屏蔽按钮】 作者：jiangchenxi 时间：2019年3月12日 start*/
            String id = csvo.getProcessId();
            long nodePolicyId = 1L;
            if (Strings.isNotBlank(id)) {
                nodePolicyId = Long.parseLong(id);
            }
            boolean open = xkjtManager.isOpen(csvo.getAffairId());
            if (open) {
                csvo.setProcessId("");
                csvo.setPreApproverName("");
            }
            /**项目：徐矿集团 【屏蔽按钮】 作者：jiangchenxi 时间：2019年3月12日 end*/
            if (permisson != null) {
                NodePolicy nodePolicy = permisson.getNodePolicy();//意见必填, 不允许直接归档和删除处理
                Integer opinion = nodePolicy.getOpinionPolicy();
                boolean canDeleteORarchive = (opinion != null && opinion.intValue() == 1);
                csvo.setCanDeleteORarchive(canDeleteORarchive);
                csvo.setCancelOpinionPolicy(nodePolicy.getCancelOpinionPolicy());
                csvo.setDisAgreeOpinionPolicy(nodePolicy.getDisAgreeOpinionPolicy());
                //节点权限设置不允许删除
                NodePolicyVO nodePolicyVo = new NodePolicyVO(permisson);
                boolean canReMove = nodePolicyVo.isReMove();
                csvo.setcanReMove(canReMove);

            } else {
                csvo.setCanDeleteORarchive(true);
                csvo.setcanReMove(true);
            }
        }
        if (flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    public Map<String, Permission> getPermissonMap(String category, Long accountId) throws BusinessException {
        Map<String, Permission> permissionMap = new HashMap<String, Permission>();
        List<Permission> permissonList = permissionManager.getPermissionsByCategory(category, accountId);
        for (Permission permission : permissonList) {
            permissionMap.put(permission.getName(), permission);
        }
        return permissionMap;
    }


    public FlipInfo getPendingList(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        FlipInfo info = getPendingAffairs(flipInfo, query);
        convertColSummaryVO2ListSimpleVO(info);
        return info;
    }

    private void convertColSummaryVO2ListSimpleVO(FlipInfo info) {
        if (info != null) {
            List<ColSummaryVO> list = info.getData();
            if (Strings.isNotEmpty(list)) {
                List<ColListSimpleVO> csvo = new ArrayList<ColListSimpleVO>();
                for (ColSummaryVO c : list) {
                    ColListSimpleVO vo = new ColListSimpleVO(c);
                    try {
                        vo.setNodeName(ColUtil.getPolicyByAffair(c.getAffair()).getName());
                    } catch (BusinessException e) {
                        LOG.error("", e);
                    }
                    csvo.add(vo);
                }
                info.setData(csvo);
            }
        }
    }

    public FlipInfo getDoneAffairs(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        //设置用户id
        String userId = query.get(ColQueryCondition.currentUser.name());
        if (Strings.isBlank(userId)) {
            User user = AppContext.getCurrentUser();
            userId = String.valueOf(user.getId());
        }
        query.put(ColQueryCondition.currentUser.name(), userId);
        query.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_done.key()));

        //已办默认需要查询代理。
        String hasNeedAgent = query.get("hasNeedAgent");
        if (Strings.isBlank(hasNeedAgent)) {
            hasNeedAgent = "true";
        }
        query.put("hasNeedAgent", hasNeedAgent);
        List<ColSummaryVO> result = null;
        boolean dumpData = Boolean.parseBoolean(query.get("dumpData"));
        if (dumpData) {
            ColDao colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
            if (colDaoFK != null) {
                //查询转储数据时，要求此处展示待办与已办
                query.put(ColQueryCondition.state.name(), StateEnum.col_pending.getKey() + "," + StateEnum.col_done.getKey());
                result = colDaoFK.queryByConditionHis(flipInfo, query);
            }
        } else {
            result = colDao.queryByCondition(flipInfo, query);
        }
        for (ColSummaryVO csvo : result) {
            String nodeName = csvo.getNodePolicy();
            ColSummary summary = csvo.getSummary();
            long flowPermAccountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary.getOrgAccountId(), summary.getPermissionAccountId());
            Permission permisson = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), nodeName, flowPermAccountId);
            if (permisson != null) {
                //节点权限设置不允许删除
                NodePolicyVO nodePolicyVo = new NodePolicyVO(permisson);
                boolean canReMove = nodePolicyVo.isReMove();
                csvo.setcanReMove(canReMove);

            } else {
                csvo.setcanReMove(true);
            }
        }

        if (flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    public FlipInfo getDoneList(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        String deduplication = String.valueOf(query.get("deduplication"));
        if ("null".equals(deduplication)) {
            deduplication = "false";
        }
        query.put(ColQueryCondition.deduplication.name(), deduplication);
        FlipInfo info = getDoneAffairs(flipInfo, query);
        convertColSummaryVO2ListSimpleVO(info);
        /**项目：徐矿集团 【屏蔽按钮】 作者：jiangchenxi 时间：2019年3月12日 start*/
        List<ColListSimpleVO> affairs = info.getData();
        for (ColListSimpleVO colListSimpleVO : affairs) {
            String id = colListSimpleVO.getProcessId();
            long nodePolicyId = 1L;
            if (Strings.isNotBlank(id)) {
                nodePolicyId = Long.parseLong(id);
            }
            boolean open = xkjtManager.isOpen(colListSimpleVO.getAffairId());
            //Long long1 = AppContext.getCurrentUser().getId();
            //boolean open = xkjtManager.issendmessage(colListSimpleVO.getAffairId(), long1);
            if (!open) {
                colListSimpleVO.setProcessId("");
                //colListSimpleVO.setPreApproverName(""); by wxt.xiaohailong 是屏蔽当前代办人 不是上级 愚蠢!!!!
                colListSimpleVO.setCurrentNodesInfo("");
            }
        }
        /**项目：徐矿集团 【屏蔽按钮】 作者：jiangchenxi 时间：2019年3月12日 end*/
        return info;
    }

    public FlipInfo getWaitSendAffairs(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        //设置用户id
        User user = AppContext.getCurrentUser();
        query.put(ColQueryCondition.currentUser.name(), String.valueOf(user.getId()));
        query.put(ColQueryCondition.state.name(), String.valueOf(StateEnum.col_waitSend.key()));
        List<ColSummaryVO> result = colDao.queryByCondition(flipInfo, query);
        if (flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    public FlipInfo getWaitSendList(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        FlipInfo info = getWaitSendAffairs(flipInfo, query);
        convertColSummaryVO2ListSimpleVO(info);
        return info;
    }

    public List<ColSummaryVO> getTrackList4BizConfig(Long memberId, List<Long> tempIds) throws BusinessException {
        return colDao.getTrackList4BizConfig(memberId, tempIds);
    }

//    public FlipInfo findFavoriteAffairs(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
//        query.put(ColQueryCondition.affairFavorite.name(), "true");
//        List<ColSummaryVO> list = colDao.queryByCondition(flipInfo, query);
//        if (list != null) {
//            flipInfo.setData(list);
//        }
//        convertColSummaryVO2ListSimpleVO(flipInfo);
//        return flipInfo;
//    }

    @Override
    public Map saveDraft(ColInfo info, boolean b, Map para) throws BusinessException {
        V3xOrgMember sender = null;
        try {
            sender = this.orgManager.getMemberById(info.getCurrentUser().getId());
        } catch (BusinessException e1) {
            LOG.error("", e1);
        }
        ColSummary summaryFromUE = info.getSummary();
        CtpAffair affair = null;
        boolean isSpecialBacked = false;
        ColSummary summary = info.getSummary();
        if (summary.getId() != null) {
            summary = getColSummaryById(summary.getId());
            if (summary != null) {
                affair = affairManager.getSenderAffair(summary.getId());
                if (affair != null) {
                    int subState = affair.getSubState().intValue();
                    if (subState == SubStateEnum.col_pending_specialBacked.getKey() || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                        isSpecialBacked = true;
                    }
                }
            } else {
                summary = info.getSummary();
            }
        }

        if (summary.getId() != null) { //删除原来的
           /* if(!isSpecialBacked){
                affairManager.deleteByObjectId(ApplicationCategoryEnum.collaboration, summary.getId());
            } */
            attachmentManager.deleteByReference(summary.getId(), summary.getId());
            summary.setSubject(summaryFromUE.getSubject());//标题
            summary.setDeadline(summaryFromUE.getDeadline());
            summary.setDeadlineDatetime(summaryFromUE.getDeadlineDatetime());
            summary.setAdvanceRemind(summaryFromUE.getAdvanceRemind());
            summary.setAwakeDate(summaryFromUE.getAwakeDate());
            summary.setImportantLevel(summaryFromUE.getImportantLevel());
            summary.setArchiveId(summaryFromUE.getArchiveId());
            summary.setAdvancePigeonhole(summaryFromUE.getAdvancePigeonhole());
            summary.setProjectId(summaryFromUE.getProjectId());
            summary.setCanArchive(summaryFromUE.getCanArchive());
            summary.setCanAutostopflow(summaryFromUE.getCanAutostopflow());
            summary.setCanEdit(summaryFromUE.getCanEdit());
            summary.setCanEditAttachment(summaryFromUE.getCanEditAttachment());
            summary.setCanModify(summaryFromUE.getCanModify());
            summary.setCanForward(summaryFromUE.getCanForward());
            if (null == summaryFromUE.getCanMergeDeal()) {
                summary.setCanMergeDeal(false);
            }
            summary.setCanAnyMerge(summaryFromUE.getCanAnyMerge());
        }

        ContentSaveOrUpdateRet content = ContentUtil.contentSave();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (ColUtil.isForm((String) para.get("bodyType"))) {

            summary.setFormAppid(Long.valueOf((String) para.get("contentTemplateId")));//表单ID
            summary.setFormRecordid(Long.valueOf((String) para.get("contentDataId")));//form主数据ID
        }
        boolean needMergeCreateDate = false;
        String subjectForCopy = info.getSubjectForCopy();
        /**
         * 1：隐藏区存的标题和标题区一样 说明没有变过标题  这种需要把标题设置成   题目  + 创建时间的格式，调用的时候判断标题包含的是协同创建时间的话
         * 2：如果不一样标题就不包含创建时间 这种就要用以前的比标题
         */
        if (Strings.isNotBlank(subjectForCopy) && subjectForCopy.equals(summary.getSubject())) {
            needMergeCreateDate = true;
        }

        if (!isSpecialBacked) {
            summary.setCreateDate(now);
            summary.setState(null);
            if (needMergeCreateDate) {
                summary.setSubject(getTemplateSubject(summary.getSubject(), now, true));
            }
        } else {
            summary.setState(CollaborationEnum.flowState.run.ordinal());
        }
        summary.setStartMemberId(info.getCurrentUser().getId());
        summary.setBodyType((String) para.get("bodyType"));//设置正文类型
        summary.setOrgAccountId(info.getCurrentUser().getLoginAccount());
        summary.setTempleteId(info.gettId());

        //保存待发清空接收人
        summary.setProcessNodesInfo("");

        Long permissionAccountId = summary.getOrgAccountId();
        if (summary.getTempleteId() != null) {
            CtpTemplate t = templateManager.getCtpTemplate(summary.getTempleteId());
            if (t != null) {
                permissionAccountId = t.getOrgAccountId();
            }
        }
        summary.setPermissionAccountId(permissionAccountId);


        /**
         * 协同V5.0 OA-38438 查看待发协同时，点击流程最大化，原流程没有显示出来
         *                    TID     curTemId              templateID    processID
         * 1:掉系统模板的时候                  有                          有                    数据库                         有                                无
         * 2:系统模板存的个人模板          有                           有                    数据库                         有                               无
         * 3：自由协同                                无                       无                      数据库                          0            1
         * 4：自由协同存的个人模板         无                       有                      database      0                     1
         */
        summary.setAudited(false);
        summary.setVouch(CollaborationEnum.vouchState.defaultValue.ordinal());

        if (affair == null) {
            affair = new CtpAffair();
        }
        affair.setIdIfNew();
        affair.setApp(ApplicationCategoryEnum.collaboration.key());
        affair.setSubApp(summary.getTempleteId() == null ? ApplicationSubCategoryEnum.collaboration_self.key() : ApplicationSubCategoryEnum.collaboration_tempate.key());
        affair.setSubject(summary.getSubject());
        if (!isSpecialBacked) {
            affair.setCreateDate(now);
            if (needMergeCreateDate) {
                affair.setSubject(summary.getSubject());
            }
        }
        affair.setSummaryState(summary.getState());
        affair.setUpdateDate(now);
        affair.setMemberId(info.getCurrentUser().getId());
        affair.setObjectId(summary.getId());
        affair.setSubObjectId(null);
        affair.setSenderId(info.getCurrentUser().getId());
        affair.setState(StateEnum.col_waitSend.key());
        affair.setFormAppId(summary.getFormAppid());
        String oldProcessId = summary.getProcessId();
        if (!isSpecialBacked) {
            //没有流程ID，并且不是（系统模板流程模板和协同模板）的时候，协调格式模板需要存PROCESSID，保存PROCESSID。
            boolean isTextTemplate = false;
            boolean isSystem = false;
            if (info.gettId() != null) {
                CtpTemplate t = templateManager.getCtpTemplate(info.gettId());
                isTextTemplate = TemplateEnum.Type.text.name().equals(t.getType());
                isSystem = true;
            }
            if (Strings.isBlank(summary.getProcessId()) && (!isSystem || isTextTemplate)) {
                summary.setProcessId(content.getProcessId());
            } else if (info.gettId() == null) {
                //自由协同的 每次都存一下
                summary.setProcessId(content.getProcessId());
            } else {
                summary.setProcessId(null);
            }
            summary.setCaseId(null);
            if (!(Integer.valueOf(SubStateEnum.col_waitSend_cancel.key()).equals(affair.getSubState()))) {
                affair.setSubState(SubStateEnum.col_waitSend_draft.key());
            }
            summary.setStartDate(null);
        }
        String newProcessId = summary.getProcessId();
        if (Strings.isNotBlank(oldProcessId) && Strings.isNotBlank(newProcessId) && !newProcessId.equals(oldProcessId)) {
            processLogManager.updateByHQL(Long.valueOf(newProcessId), Long.valueOf(oldProcessId));
        }

        affair.setDelete(false);
        affair.setTempleteId(summary.getTempleteId());

        affair.setTrack(info.getTrackType());
        affair.setBodyType((String) para.get("bodyType"));//设置正文类型
        AffairUtil.setHasAttachments(affair, ColUtil.isHasAttachments(summary));
        affair.setImportantLevel(summary.getImportantLevel());
        affair.setResentTime(summary.getResentTime());
        affair.setForwardMember(summary.getForwardMember());
        affair.setProcessId(summary.getProcessId());
        affair.setCaseId(summary.getCaseId());
        affair.setOrgAccountId(summary.getOrgAccountId());
        affair.setNodePolicy("newCol");
        if (Strings.isNotBlank(info.getDR())) {
            affair.setRelationDataId(Long.valueOf(info.getDR()));
        }
        //设置流程期限
        if (summary.getDeadlineDatetime() != null) {
            AffairUtil.addExtProperty(affair, AffairExtPropEnums.processPeriod, summary.getDeadlineDatetime());
        }

        //保存附件
        String attaFlag = saveAttachmentFromDomain(ApplicationCategoryEnum.collaboration, summary.getId());
        if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
            ColUtil.setHasAttachments(summary, true);
            AffairUtil.setHasAttachments(affair, true);//设置附件
        } else {//添加else  第一次保存代码 设置了有  如果第2次 删掉在保存待发  就必须要清楚第一次 设置的信息
            ColUtil.setHasAttachments(summary, false);
            AffairUtil.setHasAttachments(affair, false);//设置附件
        }

        colDao.saveColSummary(summary);
        DBAgent.saveOrUpdate(affair);
        //保存督办
        if (!info.isM3Flag()) {
            this.saveColSupervise4NewColl(summary, false);
        }
        if (isSpecialBacked) {
            colPubManager.updateSpecialBackedAffair(summary);
        }

        // 新增模板调用历史记录
        Long _addRecentTemplateId = (null != info.getCurTemId()) ? info.getCurTemId() : summary.getTempleteId();
        if (_addRecentTemplateId != null) {
            this.templateManager.addRecentTemplete(AppContext.currentUserId(), ModuleType.collaboration.getKey(), _addRecentTemplateId, TemplateEnum.RecentUseType.call);
        }

        //加入跟踪信息 当跟踪类型为2的时候(指定人),像跟踪表插入数据
        if (info.getTrackType() == 2) {
            //OA-40840 新建协同时，设置跟踪指定人A后，连续点击3次【保存待发】，在待发中编辑该协同，查看指定人选人框处多出几个已选择人员A
            trackManager.deleteTrackMembers(summary.getId(), affair.getId());
            //跟踪的逻辑
            String trackMemberId = info.getTrackMemberId();
            String[] str = trackMemberId.split(",");
            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
            CtpTrackMember member = null;
            for (int count = 0; count < str.length; count++) {
                member = new CtpTrackMember();
                member.setIdIfNew();
                member.setAffairId(affair.getId());
                member.setObjectId(summary.getId());
                member.setMemberId(sender.getId());
                member.setTrackMemberId(Long.parseLong(str[count]));
                list.add(member);
            }
            trackManager.save(list);
        }
        Map<String, Object> tranMap = new HashMap<String, Object>();
        tranMap.put("summaryId", summary.getId());
        tranMap.put("contentId", info.getContentSaveId());
        tranMap.put("affairId", affair.getId());
        //调用表单万能方法,更新状态，触发子流程等
        if (ColUtil.isForm(summary.getBodyType())) {
            try {
                capFormManager.updateDataState(summary, affair, ColHandleType.save, null);
            } catch (Exception e) {
                LOG.error("更新表单相关信息异常", e);
            }
        }
        return tranMap;
    }


    public List<ColSummaryVO> queryByCondition(FlipInfo flipInfo, Map<String, String> condition)
            throws BusinessException {
        return this.colDao.queryByCondition(flipInfo, condition);
    }

    public int countByCondition(Map<String, String> condition) throws BusinessException {
        return this.colDao.countByCondition(condition);
    }

    /**
     * 如果是指定回退时，有可能改变事项(标题、重要程度,正文类型)，此时更新所有的事项
     *
     * @param summaryId
     */
//    public void updateSpecialBackedAffair(ColSummary summary){
//    	if(summary == null) return;
//    	StringBuffer hql = new StringBuffer();
//		hql.append("update CtpAffair as a set a.subject =:subject ,a.importantLevel =:importantLevel,a.bodyType =:bodyType where a.objectId =:objectId");
//		Map<String,Object> params=new HashMap<String, Object>();
//		params.put("subject", summary.getSubject());
//		params.put("importantLevel", summary.getImportantLevel());
//		params.put("objectId", summary.getId());
//		params.put("bodyType", summary.getBodyType());
//		DBAgent.bulkUpdate(hql.toString(), params);
//    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map getAttributeSettingInfo(Map<String, String> args) throws BusinessException {
        Map map = new HashMap();
        if (args == null) {
            return map;
        }
        //无
        String blank = ResourceUtil.getString("collaboration.project.nothing.label");
        String affairId = args.get("affairId");
        if (Strings.isBlank(affairId)) {
            return map;
        }
        boolean isHistoryFlag = false;
        if (args.get("isHistoryFlag") != null && "true".equals((String) args.get("isHistoryFlag"))) {
            isHistoryFlag = true;
        }
        CtpAffair affair = null;
        if (isHistoryFlag) {
            affair = affairManager.getByHis(Long.parseLong(affairId));
        } else {
            affair = affairManager.get(Long.parseLong(affairId));
        }
        if (affair == null) {
            return map;
        }
        //流程状态
        String state = "";
        switch (StateEnum.valueOf(affair.getState().intValue())) {
            case col_waitSend:
                state = ResourceUtil.getString("collaboration.state.11.waitSend");
                break;
            case col_sent:
                state = ResourceUtil.getString("collaboration.state.12.col_sent");
                break;
            case col_pending:
                state = ResourceUtil.getString("collaboration.state.13.col_pending");
                break;
            case col_done:
                state = ResourceUtil.getString("collaboration.state.14.done");
                break;
        }
        map.put("flowState", state);
        //模版id
        String processId = null;
        Long templateWorkFlowID = null;
        //查看协同时，特有的属性
        if (ApplicationCategoryEnum.collaboration.key() == affair.getApp().intValue()) {
            ColSummary colSummary = this.getColSummaryById(affair.getObjectId());
            //是否超期
            Boolean cOverTime = ColUtil.checkAffairIsOverTime(affair, colSummary);
            map.put("cOverTime", Boolean.TRUE.equals(cOverTime) ? ResourceUtil.getString("pending.overtop.true.label") : ResourceUtil.getString("pending.overtop.false.label"));
            processId = colSummary.getProcessId();
            //待发中没有PROCESSID的时候，读取模板的流程ID。
            if (Strings.isBlank(processId) && Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState())) {
                if (colSummary.getTempleteId() != null) {
                    CtpTemplate t = templateManager.getCtpTemplate(colSummary.getTempleteId());
                    templateWorkFlowID = t.getWorkflowId();
                }
            }
            Long projectId = colSummary.getProjectId();
            //关联项目
            if (projectId == null || projectId == -1) {
                map.put("projectName", blank);//无
            } else {
                if (null != projectApi) {
                    ProjectBO project = projectApi.getProject(projectId);
                    if (null != project) {
                        String projectName = project.getProjectName();
                        if (projectName != null) {
                            map.put("projectName", projectName);
                        } else {
                            map.put("projectName", blank);//无
                        }
                    }
                }
            }
            //归档
            Long archiveId = colSummary.getArchiveId();
            String archiveName = blank;
            String archiveAllName = blank;

            boolean queryArchievePath = true;
            if (Strings.isNotBlank(colSummary.getAdvancePigeonhole())) {
                try {
                    JSONObject jo = new JSONObject(colSummary.getAdvancePigeonhole());
                    String archiveField = jo.optString(ColConstant.COL_ARCHIVEFIELDID, "");
                    if (Strings.isNotBlank(archiveField)) {
                        archiveName = ColUtil.getAdvancePigeonholeName(colSummary.getArchiveId(), colSummary.getAdvancePigeonhole(), "col");
                        if (jo.has(ColConstant.COL_ARCHIVEFIELDVALUE)) {
                            String archiveFieldValue = jo.get(ColConstant.COL_ARCHIVEFIELDVALUE).toString();
                            if (Strings.isNotBlank(archiveFieldValue) && Strings.isNotBlank(ColUtil.getArchiveAllNameById(archiveId))) {
                                archiveAllName = new StringBuilder(ColUtil.getArchiveAllNameById(archiveId)).append("\\").append(archiveFieldValue).toString();
                            }
                        } else {
                            if (jo.has("archiveFieldName") && Strings.isNotBlank(ColUtil.getArchiveAllNameById(archiveId))) {
                                archiveAllName = new StringBuilder(ColUtil.getArchiveAllNameById(archiveId)).append("\\{")
                                        .append(jo.get("archiveFieldName").toString()).append("}").toString();
                            }
                        }
                        //不用再查归档数据了
                        queryArchievePath = false;
                    }
                } catch (JSONException e) {
                    LOG.info("解析归档信息", e);
                }
            }

            //直接获取归档路径
            if (queryArchievePath) {
                if (archiveId != null) {
                    archiveName = ColUtil.getArchiveNameById(archiveId);
                    archiveAllName = ColUtil.getArchiveAllNameById(archiveId);
                    if (Strings.isBlank(archiveName)) {
                        archiveName = blank;
                    }
                    if (Strings.isBlank(archiveAllName)) {
                        archiveAllName = blank;
                    }
                }
            }
            String attachmentArchiveName = blank;
            //附件归档全路径
            if (colSummary.getAttachmentArchiveId() != null) {
                String retArchiveName = ColUtil.getArchiveAllNameById(colSummary.getAttachmentArchiveId());
                if (Strings.isBlank(retArchiveName)) {
                    retArchiveName = blank;
                }
                attachmentArchiveName = retArchiveName;
            }
            //wxj是否允许点赞
            Boolean canPraise = true;
            Boolean isFormTemplete = false;
            if (colSummary.getTempleteId() != null) {
                CtpTemplate t = templateManager.getCtpTemplate(colSummary.getTempleteId());
                canPraise = t.getCanPraise();
                if (ColUtil.isForm(t.getBodyType())) {
                    isFormTemplete = true;
                }
            }
            map.put("isFormTemplete", isFormTemplete);
            map.put("canPraise", canPraise);
            map.put("attachmentArchiveName", attachmentArchiveName);

            map.put("archiveName", archiveName);
            map.put("archiveAllName", archiveAllName);
            //重要程度
            Integer importantLevel = colSummary.getImportantLevel();
            map.put("importantLevel", importantLevel == null ? blank : ColUtil.getImportantLevel(importantLevel
                    .toString()));
            //流程期限
            Date deadlineDatetime = colSummary.getDeadlineDatetime();
            if (deadlineDatetime != null) {
                map.put("deadline", ColUtil.getDeadLineName(deadlineDatetime));
            } else {//兼容老数据，还是按时间段显示
                String oldDeadLine = ColUtil.getDeadLineName(colSummary.getDeadline());
                map.put("deadline", Strings.isNotBlank(oldDeadLine) ? oldDeadLine : blank);
            }

            //能否转发
            Boolean canForward = colSummary.getCanForward();
            map.put("canForward", Boolean.TRUE.equals(canForward) ? "1" : "0");
            //能否修改流程
            Boolean canModify = colSummary.getCanModify();
            map.put("canModify", Boolean.TRUE.equals(canModify) ? "1" : "0");

            //处理人连续为同一人时，合并处理
            boolean canAnyDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE, colSummary);
            boolean canPreDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE, colSummary);
            boolean canStartMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE, colSummary);

            map.put("canStartMerge", canStartMerge);
            map.put("canPreDealMerge", canPreDealMerge);
            map.put("canAnyDealMerge", canAnyDealMerge);
            //能否编辑正文
            Boolean canEdit = colSummary.getCanEdit();
            map.put("canEdit", Boolean.TRUE.equals(canEdit) ? "1" : "0");
            //能否编辑附件
            Boolean canEditAttachment = colSummary.getCanEditAttachment();
            map.put("canEditAttachment", Boolean.TRUE.equals(canEditAttachment) ? "1" : "0");
            //是否归档
            Boolean canArchive = colSummary.getCanArchive();
            map.put("canArchive", Boolean.TRUE.equals(canArchive) ? "1" : "0");
            //流程到期自动终止和撤销
            Integer processTermType = colSummary.getProcessTermType();
            Boolean hasprocessTermType = Boolean.TRUE.equals(colSummary.getCanAutostopflow()) || null != processTermType;
            map.put("processTermType", hasprocessTermType ? "1" : "0");
            if (hasprocessTermType) {
                if (Boolean.TRUE.equals(colSummary.getCanAutostopflow())) {
                    processTermType = 0;
                }
                map.put("processTermTypeName", ResourceUtil.getString("collaboration.auto.show.title" + processTermType));
            } else {
                map.put("processTermTypeName", ResourceUtil.getString("collaboration.auto.show.title"));
            }

            //流程到期重复提醒
            Long remindInterval = colSummary.getRemindInterval();
            if (null != remindInterval && remindInterval > 0) {
                EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
                CtpEnumItem cei = em.getEnumItem(EnumNameEnum.common_remind_time, String.valueOf(remindInterval));
                String enumLabel = ResourceUtil.getString(cei.getLabel());
                String remindIntervalTitle = ResourceUtil.getString("collaboration.worklfow.overtime.title.start") + enumLabel + ResourceUtil.getString("collaboration.worklfow.overtime.title.end");
                map.put("remindIntervalTitle", remindIntervalTitle);
            }
            //提醒
            Long advanceRemind = colSummary.getAdvanceRemind();
            map.put("canDueReminder", advanceRemind == null ? blank : ColUtil.getAdvanceRemind(advanceRemind
                    .toString()));
            //发起时间
            Date startDate = colSummary.getStartDate();
            map.put("startDate", null != startDate ? DateUtil.formatDateTime(startDate) : "");
            //显示督办信息
            CtpSuperviseDetail detail = superviseManager.getSupervise(affair.getObjectId());
            if (detail != null && Strings.isNotBlank(detail.getSupervisors())) {
                map.put("awakeDate", DateUtil.formatDateTime(detail.getAwakeDate()));
                map.put("supervisors", detail.getSupervisors());
                map.put("supervise", "supervise");
            }
        }
        //表单绑定
        if (ColUtil.isForm(affair.getBodyType())) {
            String viewOperation = null;
            //已办和待办时直接从Affair中取值
            if (Integer.valueOf(StateEnum.col_done.getKey()).equals(affair.getState())
                    || Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())) {
                viewOperation = affair.getMultiViewStr();
                if (Strings.isBlank(viewOperation)) {
                    viewOperation = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, affair.getTempleteId());
                }
            } else {
                if (templateWorkFlowID != null) {
                    viewOperation = wapi.getNodeFormOperationName(templateWorkFlowID, null);
                } else {
                    viewOperation = wapi.getNodeFormOperationNameFromRunning(processId, null, isHistoryFlag);
                }
            }

            if (Strings.isNotBlank(viewOperation)) {

                List<String> rightNames = capFormManager.getFormRightName(affair.getFormAppId(), viewOperation);
                if (Strings.isNotEmpty(rightNames)) {
                    map.put("formOperation", rightNames.get(0));
                }
            }

        }
        return map;
    }


    /**
     * 设置跟踪信息
     *
     * @param affair
     * @param params <pre>
     *                {String} [isTrack] 是否跟踪， 1 - 跟踪， 其他-不跟踪
     *                {String} [trackRange_members] 跟踪指定人，在[isTrack]为1的前提下生效 , 0 - 跟踪指定人, 其他-跟踪全部
     *                {String} [trackRange_all] 跟踪全部，在[isTrack]为1的前提下 生效, 值为 1
     *                {String} [zdgzry] 跟踪指定人的ID
     *               </pre>
     * @return
     * @throws BusinessException
     */
    private int saveTrackInfo(CtpAffair affair, Map<String, String> params) throws BusinessException {
        Long affairId = affair.getId();
        Long summaryId = affair.getObjectId();
        int trackType = 0;
        String trackIds = "";
        if (null != params && params.size() > 0) {
            // isTrack为"1"表示跟踪，其他情况表示不跟踪。
            String isTrack = params.get("isTrack");
            if ("1".equals(isTrack)) {
                trackType = 1;

                // 删除一次跟踪的消息，因为如果先暂存代办指定人 在打开的时候 选择全部的话,数据没有被清理掉，为了避免数据错误，这里进行一次删除操作
                if (Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack())) {
                    trackManager.deleteTrackMembers(null, affair.getId());
                }

                if (null != params.get("trackRange_members")) {
                    trackType = 2;
                    trackIds = params.get("zdgzry");
                }
                if (trackType == 2) {
                    String[] str = trackIds.split(",");
                    List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
                    if (Strings.isNotBlank(str[0])) {
                        CtpTrackMember member = null;
                        for (int count = 0; count < str.length; count++) {
                            member = new CtpTrackMember();
                            member.setIdIfNew();
                            member.setAffairId(affairId);
                            member.setObjectId(summaryId);
                            member.setMemberId(affair.getMemberId());
                            member.setTrackMemberId(Long.parseLong(str[count]));
                            list.add(member);
                        }
                        trackManager.save(list);
                    }
                }
            } else if (isTrack == null) {
                // 如果目前有设置跟踪再删除，本身就没设置的话就不需要删除跟踪人员了
                if (!Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                    trackManager.deleteTrackMembers(null, affair.getId());
                }
            }
        }

        affair.setTrack(trackType);// 设置为不跟踪

        return trackType;
    }

    @Override
    public void transFinishWorkItem(ColSummary summary, CtpAffair affair, Map<String, Object> params)
            throws BusinessException {
        // 督办设置
        @SuppressWarnings("unchecked")
        Map<String, Object> superviseMap = (Map<String, Object>) ParamUtil.getJsonDomain("superviseDiv");
        String isModifySupervise = (String) superviseMap.get("isModifySupervise");
        if ("1".equals(isModifySupervise)) {
            SuperviseMessageParam smp = new SuperviseMessageParam(true, summary.getImportantLevel(),
                    summary.getSubject(), summary.getForwardMember(), summary.getStartMemberId());
            this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), SuperviseEnum.EntityType.summary);
        }
        // 从request对象中对象中获取意见
        Comment comment = ContentUtil.getCommnetFromRequest(ContentUtil.OperationType.finish, affair.getMemberId(),
                affair.getObjectId());
        // 调用公用方法
        transFinishWorkItemPublic(affair, summary, comment, ColHandleType.finish, params);
    }

    @Override
    public void transFinishWorkItemPublic(Long affairId, Comment comment, Map<String, Object> params) throws BusinessException {
        CtpAffair affair = affairManager.get(affairId);
        ColSummary summary = getColSummaryById(affair.getObjectId());
        transFinishWorkItemPublic(affair, summary, comment, ColHandleType.finish, params);
    }

    @Override
    public void transFinishWorkItemPublic(CtpAffair affair, ColSummary summary, Comment comment, ColHandleType handleType, Map<String, Object> params) throws BusinessException {

        if (params == null) {
            params = new HashMap<String, Object>();
        }

        //这里提前更新，目的是将summary/affair对象加入session,避免出现同一个session里面有2个对象
        this.updateColSummary(summary);
        affairManager.updateAffair(affair);

        //这个地方是所有协同处理的入口 {处理，删除，归档，批处理}，加上代理校验
        try {
            if (!ColUtil.checkAgent(affair, summary, true)) {
                return;
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
        //优先设置跟踪，因为流程处理的时候先发消息。如果没有设置跟中就先删除
        String isTrack = String.valueOf(params.get("isTrack"));
        if (!"1".equals(isTrack)) {
            // 如果目前有设置跟踪再删除，本身就没设置的话就不需要删除跟踪人员了
            if (!Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                trackManager.deleteTrackMembers(null, affair.getId());
                affair.setTrack(0);
                affairManager.updateAffair(affair);
            }
        }

        //流程处理事件通知
        CollaborationProcessEvent event = new CollaborationProcessEvent(this);
        event.setSummaryId(summary.getId());
        event.setAffair(affair);
        event.setComment(comment);
        event.setSenderId(summary.getStartMemberId());
        event.setUserId(AppContext.currentUserId());
        event.setTemplateId(summary.getTempleteId());
        event.setBodyType(summary.getBodyType());
        EventDispatcher.fireEvent(event);

        if ("vouch".equalsIgnoreCase(affair.getNodePolicy())) {
            summary.setVouch(CollaborationEnum.vouchState.pass.ordinal());
        }
        if ("formaudit".equals(affair.getNodePolicy())) {
            summary.setAudited(true);
        }

        transFinishAndZcdb(affair, summary, comment, handleType, params);
    }

    /**
     * 协同处理提交和暂存待办公用的方法
     *
     * @param affair
     * @param summary
     * @param comment
     * @param handleType
     * @param params     其他参数，例如跟踪，等等
     *                   <pre>
     *                      跟踪相关参数
     *                      {Map<String, String>} [trackParam] 跟踪相关参数，{@link #saveTrackInfo}
     *                   </pre>
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    private XkjtTempManager tempManager=new XkjtTempManagerImpl();
    private void transFinishAndZcdb(CtpAffair affair, ColSummary summary, Comment comment, ColHandleType handleType, Map<String, Object> params) throws BusinessException {

        User user = AppContext.getCurrentUser();
//     获取回退state=6的数据   zhou start
        Map<String, Object> map6 = new HashMap<>();
        map6.put("activityId", affair.getActivityId());
        map6.put("objectId", affair.getObjectId());
        String hql = "from CtpAffair where state=6 and nodePolicy='请假转送' and  activityId=:activityId and objectId=:objectId ";
        List<CtpAffair> list6 = affairManager.findState6(hql, map6);
        XkjtTemp xktemp = null;
        if (list6.size() > 0) {
            for (CtpAffair a : list6) {
                xktemp = new XkjtTemp();
                xktemp.setId(Long.toString(a.getId()));
                xktemp.setSummaryId(Long.toString(a.getObjectId()));
                tempManager.saveXkjtTemp(xktemp);
            }
        }
//     获取回退state=6的数据   zhou end
        //保存附件
        Map<String, String> colSummaryDomian = (Map<String, String>) ParamUtil.getJsonDomain("colSummaryData");
        String _flowPermAccountId = colSummaryDomian.get("flowPermAccountId");

        Long flowPermAccountId = Strings.isBlank(_flowPermAccountId) ? summary.getOrgAccountId() : Long.valueOf(_flowPermAccountId);
        //保存意见
        if (comment != null) {
            //草稿意见先删除原来的的意见.需要从数据库里面查询
            if (comment.getId() != null) {
                CtpCommentAll c = commentManager.getDrfatComment(affair.getId());
                if (c != null) {
                    commentManager.deleteComment(ModuleType.collaboration, c.getId());
                    attachmentManager.deleteByReference(summary.getId(), c.getId());
                }
            }
            //不在这里面发消息，单独发消息
            comment.setPushMessage(false);
            comment.setId(UUIDLong.longUUID());
            if (AffairUtil.isSuperNode(affair)) {  //超级节点始终显示的提交。不显示暂存待办
                comment.setExtAtt3(null);
            }
            commentManager.insertComment(comment, affair);

            CollaborationAddCommentEvent commentEvent = new CollaborationAddCommentEvent(this);
            commentEvent.setCommentId(comment.getId());
            EventDispatcher.fireEvent(commentEvent);
        }

        //修改附件
        saveAttDatas(user, summary, affair, comment.getId());
        AffairUtil.setHasAttachments(affair, ColUtil.isHasAttachments(summary));

        //工作流组件调用
        AffairData affairData = ColUtil.getAffairData(summary);
        affairData.setMemberId(affair.getMemberId());//事项接收人id，用于工作流回调中处理代理
        affairData.addBusinessData(ColConstant.FlowPermAccountId, flowPermAccountId);

        Integer t = WorkFlowEventListener.COMMONDISPOSAL;
        if (handleType.ordinal() == ColHandleType.wait.ordinal()) {
            t = WorkFlowEventListener.ZCDB;
        }
        affairData.addBusinessData(WorkFlowEventListener.OPERATION_TYPE, t);

        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String messageDataListJSON = wfdef.get("process_message_data");

        //加签、减签、知会、会签 发送消息，记录操作日志
        /**项目：徐矿集团  【屏蔽处理意见返回发起人】  作者：jiangchenxi 时间：2019年3月15日 start*/
      /*  boolean isOpen = xkjtManager.issendmessage(affair.getId(), affair.getSenderId());
        if(isOpen){
        *//**项目：徐矿集团  【屏蔽处理意见返回发起人】  作者：jiangchenxi 时间：2019年3月15日 end*//*
        	try {
        		colMessageManager.workitemFinishedMessage(comment,affair, summary.getId(),null);
        	} catch (Exception e) {
        		LOG.error("", e);
        	}
        }
        Date actionTime = new Date();*///.sendOperationTypeMessage(messageDataListJSON, summary, affair,comment.getId());
        Date actionTime = colMessageManager.sendOperationTypeMessage(messageDataListJSON, summary, affair, comment.getId());
        actionTime = Datetimes.addSecond(actionTime, 1);

        //检查协同标题
        checkCollSubject(summary, affair, params);
        affair.setSubject(summary.getSubject());//本次获取已经更新的最新Subject

        Map<String, String> wfAppGlobalRAffs = null;
        //暂存待办
        Boolean isRego = false;
        boolean isSendFinishMsg = false;

        Map<String, Object> businessData = new HashMap<String, Object>();
        if (handleType.ordinal() == ColHandleType.wait.ordinal()) {
            Map<String, Object> wfRetMap = ContentUtil.workflowWait(affairData, affair.getSubObjectId(), summary, params);
            businessData = (Map<String, Object>) wfRetMap.get(WorkFlowEventListener.BUSINESS_DATA);
            //记录暂存待办操作日志
            ProcessLogAction action = ProcessLogAction.zcdb;
            if (AffairUtil.isSuperNode(affair)) {
                action = ProcessLogAction.commit;//超级节点前端即使是暂存待办也显示的是提交按钮。
            }
            processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), affair.getActivityId(), action, comment.getId(), actionTime);
            //暂存待办 发送消息
            //if(isOpen){
            colMessageManager.sendMessage4Zcdb(affair, comment);
            //}
        } else {

            Boolean isSepicalBackedSubmit = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());
            String conditionsOfNodes = (String) params.get("conditionsOfNodes");
            String subState = (String) params.get(WorkFlowEventListener.AFFAIR_SUB_STATE);

            Map<String, Object> wfRetMap = ContentUtil.workflowFinish(comment, affairData, affair.getSubObjectId(), affair, summary, conditionsOfNodes, subState, params);

            String nextMembers = (String) wfRetMap.get("nextMembers");
            isRego = "true".equals((String) wfRetMap.get("isRego"));
            String nextMembersWithoutPolicyInfo = (String) wfRetMap.get("nextMembersWithoutPolicyInfo");
            wfAppGlobalRAffs = (Map<String, String>) wfRetMap.get(WorkFlowEventListener.WF_APP_GLOBAL_REPEAT_AFFMAP);
            businessData = (Map<String, Object>) wfRetMap.get(WorkFlowEventListener.BUSINESS_DATA);

            if (Strings.isNotBlank(nextMembersWithoutPolicyInfo) && isSepicalBackedSubmit) {
                //if(isOpen){
                colMessageManager.transSendSubmitMessage4SepicalBacked(summary, nextMembersWithoutPolicyInfo, affair, comment, businessData);
                //}
            }

            List<ProcessLogDetail> allProcessLogDetailList = wapi.getAllWorkflowMatchLogAndRemoveCache();
            processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()),
                    affair.getActivityId(), ProcessLogAction.commit, comment.getId(), actionTime, allProcessLogDetailList, nextMembers);

            //更新affair的相关属性
            updateAffairAttribute(affair, summary, subState);


            if (!isSepicalBackedSubmit && comment != null) {//指定回退提交在外层发消息
                isSendFinishMsg = true;
            }

        }

        //更新操作时间
        if (affair.getSignleViewPeriod() == null && affair.getFirstViewDate() != null) {
            Date nowTime = new Date();
            long viewTime = workTimeManager.getDealWithTimeValue(affair.getFirstViewDate(), nowTime, affair.getOrgAccountId());
            affair.setSignleViewPeriod(viewTime);
        }

        //第一次操作时间
        if (affair.getFirstResponsePeriod() == null) {
            Date nowTime = new Date();
            long responseTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());
            affair.setFirstResponsePeriod(responseTime);
        }

        if (handleType.ordinal() == ColHandleType.finish.ordinal()) {
            // 处理后归档
            HttpServletRequest request = AppContext.getRawRequest();
            Long pigeonholeValue = null;
            if (request != null) {
                String temp = request.getParameter("pigeonholeValue");
                if (temp != null) {
                    pigeonholeValue = Long.parseLong(temp);
                }
            }
            //M3处理后归档
            String _pigeonholeValue = (String) params.get("archiveValue");
            if (Strings.isNotBlank(_pigeonholeValue) && NumberUtils.isNumber(_pigeonholeValue)) {
                pigeonholeValue = Long.valueOf(_pigeonholeValue);
            }
            if (pigeonholeValue != null && pigeonholeValue != 0l) {
                transPigeonhole(summary, affair, pigeonholeValue, "handle");
            }
        }

        //更新表单状态之前重新从线程变量中中获取一下summary对象这样，如果流程结束的话，取到的summary的state字段为结束状态,表单会用state字段
        //调用表单万能方法,更新状态，触发子流程等
        if (ColUtil.isForm(summary.getBodyType())) {
            List<Comment> commentList = this.commentManager.getCommentAllByModuleId(ModuleType.collaboration, affair.getObjectId());
            try {
                capFormManager.updateDataState(summary, affair, handleType, commentList);
            } catch (Exception e) {
                LOG.error("更新表单相关信息异常", e);
            }
        }

        //1.保存跟踪,流程不是结束状态就保存跟踪信息,流程已经结束了就不保存跟踪信息
        //2.当前节点就是最后一个节点/流程已经结束后面的只会节点：设置为结束状态。
        boolean isFinished = Integer.valueOf(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState())
                || Integer.valueOf(CollaborationEnum.flowState.terminate.ordinal()).equals(summary.getState());
        if (isFinished) {
            affair.setFinish(true);
        } else {
            Map<String, String> trackParam = (Map<String, String>) params.get("trackParam");
            saveTrackInfo(affair, trackParam);
        }

        /**项目：徐矿集团  【屏蔽处理意见返回发起人】  作者：jiangchenxi 时间：2019年3月15日 start*/
        if (isSendFinishMsg) {
            /**项目：徐矿集团  【屏蔽处理意见返回发起人】  作者：jiangchenxi 时间：2019年3月15日 end*/
            try {
                colMessageManager.workitemFinishedMessage(comment, affair, summary.getId(), null);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }

        //先commit，防止affair和summary出现重复id的情况
        DBAgent.commit();

        // 这个一定要放在更新当前待办人前面，顺序不对， 指定回退-直接提交给我， 被回退人提交时当前待办人不正确
        affairManager.updateAffair(affair);

        //更新当前处理人信息
        if (Strings.isNotBlank(affair.getNodePolicy()) && !"inform".equals(affair.getNodePolicy())) {
            String isProcessCompetion = (String) businessData.get("IsProcessCompetion");
            List<CtpAffair> assignedAffairs = (List<CtpAffair>) businessData.get(WorkFlowEventListener.ASSIGNED_AFFAIRS);

            if (isRego || (Strings.isNotBlank(isProcessCompetion) && isProcessCompetion.equals("1"))) { // 指定回退导致流程重走，情况极少，场景复杂，用数据库查询的方式来更新
                if (handleType.ordinal() == ColHandleType.wait.ordinal()) {
                    ColUtil.updateCurrentNodesInfo(summary);
                } else {
                    ColUtil.updateCurrentNodesInfo(summary, affair.getId());
                }
            } else if (handleType.ordinal() == ColHandleType.finish.ordinal()) {
                ColUtil.setCurrentNodesInfoFromCache(summary, affair.getMemberId(), assignedAffairs);
            } else {// 暂存待办的时候更新一下，因为有可能当前会签了。
                ColUtil.setCurrentNodesInfoFromCache(summary, null, assignedAffairs);
            }
        }

        ColUtil.addOneReplyCounts(summary);
        this.updateColSummary(summary);


        //全文检索库 入库
        if (AppContext.hasPlugin("index")) {
            boolean isForm = ColUtil.isForm(summary.getBodyType());
            try {
                if (isForm) {
                    indexManager.update(summary.getId(), ApplicationCategoryEnum.form.getKey());
                } else {
                    indexManager.update(summary.getId(), ApplicationCategoryEnum.collaboration.getKey());
                }
            } catch (Exception e) {
                String errorInfo = "全文检索更新异常,传入参数summaryId:" + summary.getId() + "mnoduletype:" + (isForm ? "2" : "1");
                LOG.error(errorInfo, e);
                throw new BusinessException(errorInfo, e);
            }
        }

        if (affair.getTempleteId() != null) {
            templateManager.addRecentTemplete(affair.getMemberId(), ModuleType.collaboration.getKey(), affair.getTempleteId(), TemplateEnum.RecentUseType.deal);
        }
        //同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), affair.getId());

//      [徐矿竞争执行，更新处理时间问题。] zhou start
        //多账号竞争执行，在此解决处理时间问题（同一流程节点a处理了，b的处理时间为空）
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("activityId", affair.getActivityId().longValue());
        pMap.put("objectId", affair.getObjectId().longValue());
        List<CtpAffair> affairList = affairManager.findBycondition(pMap);
        String hqlz = "update CtpAffair a set a.state=:state ,a.subState=:subState,a.completeTime=:completeTime where id=:id";
        Map<String, Object> tp = new HashMap<>();
        tp.put("summaryId", Long.toString(affair.getObjectId().longValue()));
        List<XkjtTemp> temps = tempManager.findXkjtTemp(tp);
        List<String> stringList = new ArrayList<>();
        List<String> backList = new ArrayList<>();//取回的数据的集合
        if (temps.size() > 0) {
            for (XkjtTemp tx : temps) {
                if (null != tx.getFlag() && !"".equals(tx.getFlag())) {
                    backList.add(tx.getId());
                } else {
                    stringList.add(tx.getId());
                }
            }
        }
        Map<String, Object> phql = null;
        if (backList.size() > 0) {
            for (int i = 0; i < backList.size(); i++) {
                phql=new HashMap<>();
                phql.put("state", 4);
                phql.put("subState", 0);
                phql.put("completeTime", new java.util.Date());
                phql.put("id", Long.parseLong(backList.get(i)));
                try {
                    affairManager.update(hqlz, phql);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else {
            if (affairList.size() > 0) {
                for (CtpAffair af : affairList) {
                    phql = new HashMap<>();
                    if (stringList.size() > 0) {
                        if (!stringList.contains(Long.toString(af.getId()))) {
                            if (af.getState() != 8) {
                                phql.put("state", 4);
                                phql.put("subState", 0);
                                phql.put("completeTime", new java.util.Date());
                                phql.put("id", af.getId());
                                affairManager.update(hqlz, phql);
                            }
                        }
                    } else {
                        if (af.getState() != 8) {
                            phql.put("state", 4);
                            phql.put("subState", 0);
                            phql.put("completeTime", new java.util.Date());
                            phql.put("id", af.getId());
                            affairManager.update(hqlz, phql);
                        }
                    }
                }
            }
        }
//        String hql2 = "update CtpAffair a set a.state=:state ,a.subState=:subState,a.completeTime=:completeTime where  a.activityId=:activityId and a.objectId=:objectId";
//        Map<String, Object> params2 = new HashMap<>();
//        params2.put("state", 4);
//        params2.put("subState", 0);
//        params2.put("completeTime", new Date());
//        params2.put("activityId", affair.getActivityId().longValue());
//        params2.put("objectId", affair.getObjectId().longValue());
//        try {
//            affairManager.update(hql2, params2);
//        } catch (BusinessException e) {
//            e.printStackTrace();
//            System.out.println("取回时修改状态值的hql语句出错了：" + e.getMessage());
//        }
//      多账号竞争执行，在此解决处理时间问题（同一流程节点a处理了，b的处理时间为空）
//      [徐矿竞争执行，更新处理时间问题。] zhou end
    }

    private void updateAffairAttribute(CtpAffair affair, ColSummary summary, String subState) throws BusinessException {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        affair.setCompleteTime(now);
        affair.setUpdateDate(now);
        affair.setState(StateEnum.col_done.key());
        affair.setSummaryState(summary.getState());
        if (Strings.isNotBlank(subState)) {
            affair.setSubState(Integer.valueOf(subState));
        } else {
            affair.setSubState(SubStateEnum.col_normal.key());
        }

        //判断代理人
        if (!affair.getMemberId().equals(AppContext.getCurrentUser().getId())) {
            affair.setTransactorId(AppContext.getCurrentUser().getId());
        }
        //设置运行时长，超时时长等

        ColUtil.setTime2Affair(affair, summary);
    }

    /**
     * 协同发送过程中判断是否更新协同 标题
     *
     * @param summary
     * @param affair
     * @param params
     */
    public void checkCollSubject(ColSummary summary, CtpAffair affair, Map<String, Object> params) {
        //取出模板信息
        String templateColSubject = (String) params.get("templateColSubject");
        String templateWorkflowId = "";
        Object temWorkflowId = params.get("templateWorkflowId");
        if (null != temWorkflowId && temWorkflowId instanceof java.lang.Long) {
            templateWorkflowId = String.valueOf((Long) temWorkflowId);
        } else {
            templateWorkflowId = (String) params.get("templateWorkflowId");
        }

        Long templateId = summary.getTempleteId();
        if (templateId != null && Strings.isNotBlank(templateColSubject) && Strings.isNotBlank(templateWorkflowId)) {
            User user = AppContext.getCurrentUser();
            try {
                String newSubject = ColUtil.makeSubject(templateColSubject, Long.valueOf(templateWorkflowId), summary, user);
                String oldSubject = summary.getSubject();
                //3、用新生成的标题和现有标题比较
                if (!oldSubject.equals(newSubject) && Strings.isNotBlank(newSubject)) {
                    CtpTemplate template = null;
                    if (params != null && params.get("template") != null) {
                        template = (CtpTemplate) params.get("template");
                    } else {
                        template = templateManager.getCtpTemplate(templateId);
                    }

                    if (template != null) {
                        ColSummary summary1 = (ColSummary) XMLCoder.decoder(template.getSummary());
                        //判断动态更新是否勾选
                        if (summary1 != null && summary1.getUpdateSubject() != null && summary1.getUpdateSubject()) {
                            Long summaryId = summary.getId();
                            //4、更新相关内容
                            //a、coll_summary
                            summary.setSubject(newSubject);

                            // 不要在这里设置标题， 这里是PO对象， 如果设置标题已知问题是指定回退到发起节点， 还在待办
                            // affair.setSubject(newSubject);

                            //b、affair
                            affairManager.updateFormCollSubject(summaryId, newSubject);
                            //c、ctp_content_all
                            ctpMainbodyManager.updateTitleByModuleId(newSubject, summaryId);
                            if (docApi != null) {
                                //d、doc_resources
                                docApi.updateDocResourceFRNameByColSummaryId(newSubject, summaryId);
                                //e、doc_metadata
                                docApi.updateDocMetadataAvarchar1ByColSummaryId(newSubject, summaryId);
                            }
                            //f、ctp_workflow_track
                            colTraceWorkflowManager.updateSubjectByModuleId(newSubject, summaryId);
                            //g、ctp_attachment
                            List<Long> affairIdList = affairManager.getAllAffairIdByAppAndObjectId(ApplicationCategoryEnum.collaboration, summaryId);
                            attachmentManager.updateFileNameByAffairIds(newSubject, affairIdList);
                            //h、 ctp_supervise_detail
                            superviseManager.updateSubjectByEntityId(newSubject, summaryId);
                            //i、form_relation_authority
                            formRelationManager.updateSummarySubjectByModuleId(newSubject, summaryId);

                            LOG.info("表单协同标题有变化：summaryId=" + summaryId + "，oldSubject=" + oldSubject + "，newSubject=" + newSubject + "，userId=" + user.getId());
                        }
                    }
                }
            } catch (BusinessException e) {
                LOG.error("更新表单协同标题时发生异常：", e);
            }
        }
    }


    /**
     * 保存附件并发送消息和记录应用日志
     *
     * @param user
     * @param summary
     * @param affair
     * @param commentId
     * @throws BusinessException
     * @Author : xuqw
     * @Date : 2016年5月30日下午8:13:56
     */
    private void saveAttDatas(User user, ColSummary summary, CtpAffair affair, Long commentId) throws BusinessException {
        //保存附件和正文修改
        Map<String, String> colSummaryDomian = (Map<String, String>) ParamUtil.getJsonDomain("colSummaryData");
        if (colSummaryDomian != null) {

            AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
            boolean modifyContent = "1".equals(colSummaryDomian.get("modifyFlag"));
            boolean modifyAtt = attUtil.hasEditAtt();

            if (modifyAtt) {
                saveAttachment(summary, affair, !modifyContent, commentId);
            }
            //XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
            //boolean open =  xkjtManager.issendmessage(affair.getId(), affair.getSenderId());;
            if (modifyContent) {
                //修改正文后记录流程日志
                processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()),
                        affair.getActivityId(), ProcessLogAction.processColl, commentId, String.valueOf(ProcessLogAction.ProcessEdocAction.modifyBody.getKey()));

                //修改正文发送消息
                if (!modifyAtt) {
                    colMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 1);
                }
                //如果修改正文的时候导入了新文件则记录应用日志
                if ("1".equals(colSummaryDomian.get("isLoadNewFile"))) {
                    appLogManager.insertLog(user, AppLogAction.Coll_Content_Edit_LoadNewFile, user.getName(), affair.getSubject());
                }
            }

            //同时修改了正文和附件发送消息
            if (modifyAtt && modifyContent) {
                colMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 2);
            }
        }
    }


    /**
     * 修改附件的保存
     *
     * @param summary
     * @param attaFlag
     * @param affair
     * @param toSendMsg 是否发送消息
     * @return
     * @throws BusinessException
     */
    private String saveAttachment(ColSummary summary, CtpAffair affair, boolean toSendMsg, Long commentId) throws BusinessException {
        String attaFlag = "";
        try {
            //保存附件之前先删除原来到附件

            List attFileGroup = ParamUtil.getJsonDomainGroup("attFileDomain");
            int attFileSize = attFileGroup.size();
            Map attFileMap = ParamUtil.getJsonDomain("attFileDomain");
            if (attFileSize == 0 && attFileMap.size() > 0) {
                attFileGroup.add(attFileMap);
            }
            List assDocGroup = ParamUtil.getJsonDomainGroup("assDocDomain");
            int assDocSize = assDocGroup.size();
            Map assDocMap = ParamUtil.getJsonDomain("assDocDomain");
            if (assDocSize == 0 && assDocMap.size() > 0) {
                assDocGroup.add(assDocMap);
            }
            attFileGroup.addAll(assDocGroup);
            List<Attachment> result = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.collaboration, summary.getId(), summary.getId(), attFileGroup);

            List<Attachment> oldAtts = attachmentManager.getByReference(summary.getId(), summary.getId());
            List<Attachment> needAddAtts = new ArrayList<Attachment>();
            List<Long> newAttFUIds = new ArrayList<Long>();
            Map<Long, Attachment> nm = new HashMap<Long, Attachment>();
            Map<Long, Long> oldFUID = new HashMap<Long, Long>();
            Set<Long> oldAttFUIds = new HashSet<Long>();

            LOG.info("===============当前附件：");
            for (Attachment newAtt : result) {
                if (!newAttFUIds.contains(newAtt.getFileUrl())) {
                    newAttFUIds.add(newAtt.getFileUrl());
                }
                nm.put(newAtt.getFileUrl(), newAtt);
                LOG.info("文件名：" + newAtt.getFilename() + ",ID：" + newAtt.getId() + "创建时间：" + newAtt.getCreatedate());
            }


            LOG.info("原来的附件：");
            for (Attachment oldAtt : oldAtts) {
                oldAttFUIds.add(oldAtt.getFileUrl());
                oldFUID.put(oldAtt.getFileUrl(), oldAtt.getId());
                LOG.info("文件名：" + oldAtt.getFilename() + ",ID：" + oldAtt.getId() + "创建时间：" + oldAtt.getCreatedate());
            }

            for (Long id : newAttFUIds) {
                if (!oldAttFUIds.contains(id)) {
                    if (!needAddAtts.contains(nm.get(id))) {
                        needAddAtts.add(nm.get(id));
                    }
                    LOG.info("添加的附件fileUrl：" + nm.get(id));
                }
            }

            for (Long id : oldAttFUIds) {
                if (!newAttFUIds.contains(id)) {
                    attachmentManager.deleteById(oldFUID.get(id));
                    LOG.info("删除附件 ：" + oldFUID.get(id));
                }
            }

            attaFlag = attachmentManager.create(needAddAtts);
            LOG.info("添加附件成功返回的attaFlag:" + attaFlag);

            AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
            List<ProcessLog> logs = attUtil.parseProcessLog(Long.valueOf(summary.getProcessId()), affair.getActivityId());
            for (ProcessLog log : logs) {
                log.setCommentId(commentId);
            }
            processLogManager.insertLog(logs);


            updateSummaryAttachment(result.size(), summary, affair, toSendMsg);

            //修改附件更新全文检索库
            if (AppContext.hasPlugin("index")) {
                if (ColUtil.isForm(summary.getBodyType())) {
                    indexManager.update(affair.getObjectId(), ApplicationCategoryEnum.form.getKey());
                } else {
                    indexManager.update(affair.getObjectId(), ApplicationCategoryEnum.collaboration.getKey());
                }
            }
        } catch (Exception e) {
            LOG.error("创建附件出错，位于方法ColManagerImpl.saveAttachment", e);
            throw new BusinessException("创建附件出错");
        }
        return attaFlag;
    }

    private void updateSummaryAttachment(int attSize, ColSummary summary, CtpAffair affair, boolean toSendMsg) throws BusinessException {
        Map<String, String> colSummaryDomian = (Map<String, String>) ParamUtil.getJsonDomain("colSummaryData");
        int type = 0;
        boolean needUpdate = false;
        boolean isHasAtt = ColUtil.isHasAttachments(summary);
        if (!isHasAtt && attSize > 0) {
            needUpdate = true;
            //更新summary的附件标志位
            ColUtil.setHasAttachments(summary, true);
        } else if (isHasAtt && attSize == 0) {
            //更新summary的附件标志位
            needUpdate = true;
            ColUtil.setHasAttachments(summary, false);
        }
        if (needUpdate) {
            //更新该流程中所有的待办标志位
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("identifier", summary.getIdentifier());
            this.updateColSummary(summary);
            affairManager.updateAffairs(ApplicationCategoryEnum.collaboration, summary.getId(), parameter);
        }
        if (toSendMsg) {
            colMessageManager.sendMessage4ModifyBodyOrAtt(summary, affair.getMemberId(), 0);
        }
    }

    public Long getSentAffairIdByFormRecordId(Long formRecordId) throws BusinessException {
        ColSummary s = colDao.getColSummaryByFormRecordId(formRecordId);
        if (s != null) {
            CtpAffair aff = affairManager.getSenderAffair(s.getId());
            return aff.getId();
        }
        return 0l;
    }

    @Override
    public List<String> checkForwardPermission(String affairIds) throws BusinessException {
        List<String> noPermissionCols = new ArrayList<String>();
        User user = AppContext.getCurrentUser();
        String[] ids = affairIds.split("[,]");
        for (String id : ids) {
            if (Strings.isBlank(id)) {
                continue;
            }

            long affairId = Long.parseLong(id);

            CtpAffair affair = affairManager.get(affairId);
            ColSummary summary = this.getColSummaryById(affair.getObjectId());

            if (!Strings.isTrue(summary.getCanForward())) {
                noPermissionCols.add("&lt;" + ColUtil.showSubjectOfSummary(summary, false, -1, "") + "&gt;");
                continue;
            }

            String ActivityId = affair.getActivityId() == null ? "start" : String.valueOf(affair.getActivityId());
            Boolean isNewColNode = ((StateEnum.col_sent.getKey() == affair.getState() || StateEnum.col_waitSend.getKey() == affair.getState()));
            String[] nodePolicy = wapi.getNodePolicyIdAndName(ApplicationCategoryEnum.collaboration.name(), summary.getProcessId(), ActivityId);
            if (isNewColNode) {
                NodePolicyVO newColNodePolicy = getNewColNodePolicy(user.getLoginAccount());
                if (!newColNodePolicy.isForward()) {
                    noPermissionCols.add("&lt;" + ColUtil.showSubjectOfSummary(summary, false, -1, "") + "&gt;");
                    continue;
                }
            } else if (nodePolicy != null && nodePolicy.length > 0) {
                Long accountId = ColUtil.getFlowPermAccountId(user.getLoginAccount(), summary);
                List<String> actionList = permissionManager.getActionList(ColConstant.ConfigCategory.col_flow_perm_policy.name(), nodePolicy[0], accountId);

                if (!actionList.contains("Forward")) {
                    noPermissionCols.add("&lt;" + ColUtil.showSubjectOfSummary(summary, false, -1, "") + "&gt;");
                    continue;
                }
            }
        }
        return noPermissionCols;
    }

    public void transDoForward(User user, Long summaryId, Long affairId, Map para) throws BusinessException {
        LOG.info("transDoForward...start summaryId=" + summaryId + "..affairId=" + affairId);
        boolean forwardOriginalNote = "1".equals(para.get("forwardOriginalNote"));
        boolean forwardOriginalopinion = "1".equals(para.get("forwardOriginalopinion"));
        boolean track = "1".equals(para.get("track"));
        String commentContent = (String) para.get("comment");

        ColSummary oldSummary = this.getColSummaryById(summaryId);

        Date now = new Date();

        ColSummary newSummary;
        try {
            newSummary = (ColSummary) oldSummary.clone();
        } catch (Exception e1) {
            LOG.error("", e1);
            throw new BusinessException(e1);
        }
        //转发过来的要清空原来的当前处理人信息
        newSummary.setCurrentNodesInfo("");
        newSummary.setProcessNodesInfo("");
        Long templeteId = newSummary.getTempleteId();
        newSummary.setNewId();
        newSummary.setForwardMember(String.valueOf(oldSummary.getStartMemberId()));
        if (oldSummary.getParentformSummaryid() != null) {
            newSummary.setParentformSummaryid(oldSummary.getParentformSummaryid());
        } else {
            newSummary.setParentformSummaryid(oldSummary.getId());
        }
        newSummary.setTempleteId(null);
        newSummary.setArchiveId(null);
        newSummary.setAudited(false);
        newSummary.setVouch(0);
        newSummary.setCreateDate(now);
        newSummary.setFinishDate(null);
        //newSummary.setFormAppid(null);
        newSummary.setFormRecordid(null);
        newSummary.setProcessId(null);
        newSummary.setCaseId(null);
        newSummary.setSuperviseTitle(null);
        newSummary.setSupervisors(null);
        newSummary.setSupervisorsId(null);
        newSummary.setAwakeDate(null);
        newSummary.setAttachmentArchiveId(null);
        newSummary.setReplyCounts(0);
        newSummary.setProcessTermType(null);
        newSummary.setRemindInterval(null);
        newSummary.setMergeDealType(null);

        //正文
        MainbodyType newBodyType = MainbodyType.HTML;
        String newContent = "";
        Long contentDataId = null;

        List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, oldSummary.getId());
        if (Strings.isNotEmpty(contents)) {
            CtpContentAll oldContentAll = contents.get(0);

            MainbodyType oldBodyType = MainbodyType.getEnumByKey(oldContentAll.getContentType());

            String oldContent = oldContentAll.getContent();
            LOG.info("transDoForward...contents=" + contents.size() + ",oldBodyType" + oldBodyType + ",templeteId=" + templeteId);
            switch (oldBodyType) {
                case OfficeWord:
                case OfficeExcel:
                case WpsWord:
                case WpsExcel:
                case Pdf:
                    V3XFile newFile = null;
                    try {
                        newFile = this.fileManager.clone(Long.valueOf(oldContentAll.getContentDataId()), true);
                        newBodyType = oldBodyType;
                        //作为印章校验的字段
                        newContent = oldContentAll.getContentDataId() == null ? "" : String.valueOf(oldContentAll.getContentDataId());
                        //作为真正的OFFICE文件的ID
                        contentDataId = newFile.getId();
                        signetManager.insertSignet(oldContentAll.getContentDataId(), contentDataId);
                    } catch (Exception e) {
                        LOG.error("", e);
                        // throw new BusinessException(e);
                    }
                    break;
                case FORM:
                    newBodyType = MainbodyType.HTML;
                    newSummary.setCanEdit(false); //转发的表单不能修改正文
                    // newSummary.setParentformSummaryid(oldSummary.getId());
                    CtpAffair aff = affairManager.get(affairId);
                    String formRightId = "-1";
                    // LOG.error("transDoForward...templeteId="+templeteId);
                    if (aff != null) {
                        formRightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(aff, templeteId);
                    }
                    //附件表单中的附件
                    String[] frId = formRightId.split("[_]");
                    String[] bindOperation = frId[0].split("[.]");
                    String operationId = null;
                    if (bindOperation.length > 1) {
                        operationId = bindOperation[1];
                    } else {
                        operationId = bindOperation[0];
                    }
                    boolean cap4Form = capFormManager.isCAP4Form(oldContentAll.getContentTemplateId());
                    if (!cap4Form) {
                        newSummary.setFormAppid(null);
                        transForwardBody(newSummary.getId(), oldContentAll.getModuleId(), Long.valueOf(operationId));
                    }
                    // LOG.error("transDoForward...oldContentModuleType="+oldContentAll.getModuleType()+" ,oldContentModuleId="+oldContentAll.getModuleId()+"  ,formRightId="+formRightId);
                    //将表单正文转换成HTML
                    //newContent = MainbodyService.getInstance().getContentHTML(oldContentAll.getModuleType(), oldContentAll.getModuleId(),formRightId);
                    Map<String, Object> paramMap = new HashMap<String, Object>();

                    paramMap.put("formId", oldContentAll.getContentTemplateId());
                    paramMap.put("moduleType", oldContentAll.getModuleType());
                    paramMap.put("moduleId", oldContentAll.getModuleId());

                    if (null != para.get("forwardRightId")) {
                        String trasFrId = para.get("forwardRightId").toString();
                        formRightId = Strings.isNotBlank(trasFrId) ? trasFrId : formRightId;
                    }

                    paramMap.put("rightId", formRightId);
                    paramMap.put("newSummaryId", newSummary.getId());

                    newContent = capFormManager.getFormDataContentForForward(paramMap);


                    // LOG.error("transDoForward...newContent="+newContent);
                    //套红的表单正文作为附件转发
                    List<CtpContentAll> ctpContentAll = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summaryId);
                    if (ctpContentAll != null && ctpContentAll.size() > 0 && Strings.isNotBlank(ctpContentAll.get(0).getContent())) {

                        Long formID = ctpContentAll.get(0).getContentTemplateId();
                        //判断当前正文是否是编译、隐藏、查看权限，如果是隐藏的则不转发 browse|edit|hide
                        String templateAuth = FormService.getForm(formID).getAuthViewBeanById(Long.valueOf(operationId)).getTemplateAuth();
                        if (Strings.isNotBlank(templateAuth) && !"hide".equals(templateAuth) && Strings.isDecimalExcludePlus(ctpContentAll.get(0).getContent())) {
                            List<Attachment> attachments = attachmentManager.getByReference(ctpContentAll.get(0).getContentDataId(), Long.parseLong(ctpContentAll.get(0).getContent()));
                            if (Strings.isNotEmpty(attachments)) {
                                List<Attachment> _attNews = new ArrayList<Attachment>();
                                for (Attachment att : attachments) {
                                    try {
                                        Attachment attNew = (Attachment) att.clone();
                                        /***********备份物理文件开始*************/
                                        Long newFileId = UUIDLong.absLongUUID();
                                        String filePath = this.fileManager.getFolder(attNew.getCreatedate(), true) + File.separator + newFileId;
                                        File file = fileManager.getFile(attNew.getFileUrl());
                                        try {
                                            if (file.exists()) {
                                                FileUtils.copyFile(file, new File(filePath));
                                            }
                                        } catch (IOException e) {
                                            LOG.error("带正文的表单转发，拷贝文件错误：", e);
                                        }
                                        /***********备份物理文件结束*************/
                                        attNew.setNewId();
                                        attNew.setReference(newSummary.getId());
                                        attNew.setSubReference(newSummary.getId());
                                        attNew.setFilename(newSummary.getSubject() + "[正文].doc");
                                        attNew.setFileUrl(newFileId);
                                        _attNews.add(attNew);
                                    } catch (CloneNotSupportedException e) {
                                        LOG.error("", e);
                                    }
                                }
                                boolean attaFlag = false;
                                String attaFlagStr = attachmentManager.create(_attNews);
                                attaFlag = com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlagStr);
                                if (attaFlag) {
                                    ColUtil.setHasAttachments(newSummary, attaFlag);
                                }
                            }
                        }
                    }
                    iSignatureHtmlManager.save(summaryId, newSummary.getId());
                    break;
                case HTML:
                case TXT:
                default:
                    newBodyType = oldBodyType;
                    newContent = oldContent;
                    //LOG.error("transDoForward..2298..newContent="+newContent);
                    iSignatureHtmlManager.save(summaryId, newSummary.getId());
                    break;
            }
        }

        newSummary.setBodyType(String.valueOf(newBodyType.getKey()));
        //复制原处理意见、振荡回复意见、原附件
        commentManager.transForwardComment(ModuleType.collaboration, oldSummary.getId(), ModuleType.collaboration, newSummary.getId(), forwardOriginalNote, forwardOriginalopinion);
        //复制表单转发中的模版
        if (String.valueOf(MainbodyType.HTML.getKey()).equals(oldSummary.getBodyType())) {
            addToFromHTMLAtt(summaryId, newSummary.getId());
        }

        ColInfo info = new ColInfo();

        info.setTrackType(track ? TrackEnum.all.ordinal() : TrackEnum.no.ordinal());
        info.setSummary(newSummary);
        info.setCurrentUser(user);
        info.setNewBusiness(true);
        //正文
        info.setBody(newBodyType, newContent, contentDataId, now);

        //发起者附言
        if (Strings.isNotBlank(commentContent)) {
            info.setComment(commentContent);
        }

        //fixed OA-61870升级测试：350SP1升级到5.1，转发设置了自动终止的协同，转发协同没有自动终止
        newSummary.setDeadline(0l);
        newSummary.setDeadlineDatetime(null);
        newSummary.setArchiveId(null);
        newSummary.setAdvancePigeonhole(null);
        newSummary.setAdvanceRemind(null);
        newSummary.setCanAutostopflow(false);
        newSummary.setCanMergeDeal(false);
        newSummary.setCanAnyMerge(false);
        this.colPubManager.transSendColl(ColConstant.SendType.forward, info);
    }

    /**
     * 添加是表单转发的正文区域附件
     *
     * @param oldSummaryId
     * @param summaryId
     */
    private void addToFromHTMLAtt(Long oldSummaryId, Long summaryId) {
        List<Attachment> attachment = attachmentManager.getByReference(oldSummaryId);
        List<Attachment> newAtts = new ArrayList<Attachment>();
        for (Attachment att : attachment) {
            if (AttachmentEditUtil.CONTENT_ATTACHMENTSUBRE.equals(att.getSubReference().toString())) {
                try {
                    Attachment newAtt = null;
                    newAtt = (Attachment) att.clone();
                    newAtt.setNewId();
                    //Long newFileId = UUIDLong.longUUID();
                    //this.fileManager.clone(att.getFileUrl(), att.getCreatedate(), newFileId, now);
                    newAtt.setFileUrl(att.getFileUrl());
                    newAtt.setReference(summaryId);
                    newAtt.setSubReference(100L); //表单中的附件
                    newAtts.add(newAtt);
                } catch (Exception e) {
                    LOG.warn("添加表单中的附件报错！", e);
                }
            }
        }
        this.attachmentManager.create(newAtts);
    }


    private static Pattern PATTERN_ATTACHMENT_ID = Pattern.compile("\"v\":\"(\\S+)\"",
            Pattern.CASE_INSENSITIVE);

    public String replaceInlineAttachment(String html) {
        if (Strings.isEmpty(html)) return html;
        String result = html;
        Matcher matcher = PATTERN_ATTACHMENT_ID.matcher(result);
        //当id中存在v的时候才替换
        while (matcher.find()) {
            try {
                String id = matcher.group(1);
                String lodId = id.substring(0, id.indexOf("\""));
                String fileUrlStr = id.substring(id.lastIndexOf("\"fileUrl\":\""));
                String fileUrl = fileUrlStr.substring(11, fileUrlStr.indexOf(",") - 1);
                String v = SecurityHelper.digest(fileUrl);
                if (result.indexOf("\"v\":\"") > -1) {
                    result = result.replaceAll("\"v\":\"" + lodId + "\"", "\"v\":\"" + v + "\"");
                }
            } catch (Throwable e) {
                LOG.error("", e);
            }
        }
        return result;
    }

    @Override
    public void transDoZcdb(ColSummary summary, CtpAffair affair, Map<String, Object> params) throws BusinessException {


        //督办设置
        @SuppressWarnings("unchecked")
        Map<String, Object> superviseMap = (Map<String, Object>) ParamUtil.getJsonDomain("superviseDiv");
        String isModifySupervise = (String) superviseMap.get("isModifySupervise");
        if ("1".equals(isModifySupervise)) {
            SuperviseMessageParam smp = new SuperviseMessageParam(true, summary.getImportantLevel(), summary.getSubject(), summary.getForwardMember(), summary.getStartMemberId());
            smp.setAffairId(affair.getId());
            this.superviseManager.saveOrUpdateSupervise4Process(smp, summary.getId(), SuperviseEnum.EntityType.summary);
        }
        //从request对象中对象中获取意见
        Comment comment = ContentUtil.getCommnetFromRequest(ContentUtil.OperationType.wait, affair.getMemberId(), affair.getObjectId());

        transDoZcdbPublic(affair, summary, comment, ColHandleType.wait, params);
    }

    @Override
    public void transDoZcdbPublic(CtpAffair affair, ColSummary summary,
                                  Comment comment, ColHandleType handType, Map<String, Object> params) throws BusinessException {
        //更新Affair的状态为暂存待办
        affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));

      /*  if(Integer.valueOf(SubStateEnum.col_normal.key()).equals(affair.getSubState())
        		|| Integer.valueOf(SubStateEnum.col_pending_read.key()).equals(affair.getSubState())
        		|| Integer.valueOf(SubStateEnum.col_pending_unRead.key()).equals(affair.getSubState())){
            affair.setSubState(SubStateEnum.col_pending_ZCDB.key());
        }*/
        affair.setSubState(SubStateEnum.col_pending_ZCDB.key());
        affairManager.updateAffair(affair);

        transFinishAndZcdb(affair, summary, comment, ColHandleType.wait, params);
    }

    public void getTrackInfo(TrackAjaxTranObj obj) throws BusinessException {
        String oldTrackType = obj.getOldTrackType();
        String newTrackType = obj.getNewTrackType();
        String affairId = obj.getAffairId();
        String objectId = obj.getObjectId();
        String trackMemberIds = obj.getTrackMemberIds();
        String senderId = obj.getSenderId();
        String[] ids = null;
        if (Strings.isNotBlank(trackMemberIds)) {
            ids = trackMemberIds.split(",");
        }

        //trackType 0为不跟踪 1为全部跟踪 2为部分跟踪
        if ("0".equals(oldTrackType) && "1".equals(newTrackType)) {
            //修改affair表的track类型和summary表的是否跟踪
            CtpAffair affair = new CtpAffair();
            affair = affairManager.get(Long.parseLong(affairId));
            affair.setTrack(Integer.parseInt(newTrackType));
            affairManager.updateAffair(affair);
           /* ColSummary summary = new ColSummary();
            summary = getSummaryById(Long.parseLong(objectId));
            //summary.setCanTrack(true);
            saveColSummary(summary);*/
        }
        if ("0".equals(oldTrackType) && "2".equals(newTrackType)) {
            //向跟踪表里面添加数据
            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
            if (null != ids) {
                CtpTrackMember member = null;
                for (int i = 0; i < ids.length; i++) {
                    member = new CtpTrackMember();
                    member.setIdIfNew();
                    member.setAffairId(Long.parseLong(affairId));
                    member.setObjectId(Long.parseLong(objectId));
                    member.setMemberId(Long.parseLong(senderId));
                    member.setTrackMemberId(Long.parseLong(ids[i]));
                    list.add(member);
                }
                trackManager.save(list);
            }
            //修改affair表的track类型和summary表的是否跟踪
            CtpAffair affair = new CtpAffair();
            affair = affairManager.get(Long.parseLong(affairId));
            affair.setTrack(Integer.parseInt(newTrackType));
            affairManager.updateAffair(affair);
        }
        if ("1".equals(oldTrackType) && "0".equals(newTrackType)) {
            //修改affair表的track类型和summary表的是否跟踪
            CtpAffair affair = new CtpAffair();
            affair = affairManager.get(Long.parseLong(affairId));
            affair.setTrack(Integer.parseInt(newTrackType));
            affairManager.updateAffair(affair);
            /*ColSummary summary = new ColSummary();
            summary = getSummaryById(Long.parseLong(objectId));
            summary.setCanTrack(false);
            saveColSummary(summary);*/
        }
        if ("1".equals(oldTrackType) && "2".equals(newTrackType)) {
            //向跟踪表里面添加数据
            //向跟踪表里面添加数据
            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
            if (null != ids) {
                CtpTrackMember member = null;
                for (int i = 0; i < ids.length; i++) {
                    member = new CtpTrackMember();
                    member.setIdIfNew();
                    member.setAffairId(Long.parseLong(affairId));
                    member.setObjectId(Long.parseLong(objectId));
                    member.setMemberId(Long.parseLong(senderId));
                    member.setTrackMemberId(Long.parseLong(ids[i]));
                    list.add(member);
                }
                trackManager.save(list);
            }
            //修改affair表的track类型和summary表的是否跟踪
            CtpAffair affair = new CtpAffair();
            affair = affairManager.get(Long.parseLong(affairId));
            affair.setTrack(Integer.parseInt(newTrackType));
            affairManager.updateAffair(affair);
        }
        if ("2".equals(oldTrackType) && "0".equals(newTrackType)) {
            //删除跟踪表里面的数据
            trackManager.deleteTrackMembers(Long.parseLong(objectId), Long.parseLong(affairId));

            //修改affair表的track类型和summary表的是否跟踪
            CtpAffair affair = new CtpAffair();
            affair = affairManager.get(Long.parseLong(affairId));
            affair.setTrack(Integer.parseInt(newTrackType));
            affairManager.updateAffair(affair);
           /* ColSummary summary = new ColSummary();
            summary = getSummaryById(Long.parseLong(objectId));
            summary.setCanTrack(false);
            saveColSummary(summary);*/
        }
        if ("2".equals(oldTrackType) && "1".equals(newTrackType)) {
            //删除跟踪表里面的数据

            trackManager.deleteTrackMembers(Long.parseLong(objectId), Long.parseLong(affairId));
            //修改affair表的track类型和summary表的是否跟踪
            CtpAffair affair = new CtpAffair();
            affair = affairManager.get(Long.parseLong(affairId));
            affair.setTrack(Integer.parseInt(newTrackType));
            affairManager.updateAffair(affair);
        }
        if ("2".equals(oldTrackType) && "2".equals(newTrackType)) {
            //先删除老的

            trackManager.deleteTrackMembers(Long.parseLong(objectId), Long.parseLong(affairId));
            //在插入新的
            List<CtpTrackMember> list = new ArrayList<CtpTrackMember>();
            if (null != ids) {
                CtpTrackMember member = null;
                for (int i = 0; i < ids.length; i++) {
                    member = new CtpTrackMember();
                    member.setIdIfNew();
                    member.setAffairId(Long.parseLong(affairId));
                    member.setObjectId(Long.parseLong(objectId));
                    member.setMemberId(Long.parseLong(senderId));
                    member.setTrackMemberId(Long.parseLong(ids[i]));
                    list.add(member);
                }
                trackManager.save(list);
            }
        }
    }

    /**
     * 保存督办
     */
    public void saveColSupervise4NewColl(ColSummary colSummary, boolean sendMessage) throws BusinessException {
        try {
            Map superviseMap = ParamUtil.getJsonDomain("colMainData");
            SuperviseSetVO ssvo = (SuperviseSetVO) ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
            SuperviseMessageParam smp = new SuperviseMessageParam();
            if (sendMessage) {
                smp.setSendMessage(true);
                smp.setMemberId(colSummary.getStartMemberId());
                smp.setImportantLevel(colSummary.getImportantLevel());
                smp.setSubject(colSummary.getSubject());
                smp.setForwardMember(colSummary.getForwardMember());
            }
            smp.setSaveDraft(true);
            superviseManager.saveOrUpdateSupervise4Process(ssvo, smp, colSummary.getId(), SuperviseEnum.EntityType.summary);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public ColSummary getSummaryById(Long summaryId) throws BusinessException {
        ColSummary s = colDao.getColSummaryById(summaryId);

        if (s == null) {
            ColDao colDaoFK = null;
            if (AppContext.hasPlugin("fk")) {
                colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
            }
            if (colDaoFK != null) {
                s = colDaoFK.getColSummaryByIdHis(summaryId);
            }
        }
        return s;
    }

    @Override
    public String checkCanDelete(DeleteAjaxTranObj obj) throws BusinessException {
        String affairIds = obj.getAffairIds();
        String result = "";
        String from = obj.getFromMethod();
        List<CtpAffair> affairList = null;
        if (Strings.isNotBlank(affairIds)) {
            affairList = new ArrayList<CtpAffair>();
            String[] affairs = affairIds.split("[,]");
            for (String affairId : affairs) {
                Long _affairId = Long.valueOf(affairId);
                CtpAffair affair = affairManager.get(_affairId);
                affairList.add(affair);
                int state = affair.getState();
                if (state != StateEnum.col_pending.getKey() && state != StateEnum.col_done.getKey()
                        && state != StateEnum.col_sent.getKey()
                        && (state != StateEnum.col_waitSend.getKey() || "listSent".equals(from))) { //已发里被回退的不能删除
                    result = ColUtil.getErrorMsgByAffair(affair);
                }
            }
        }

        if (!"".equals(result)) {
            return result;
        }
        return "success";
    }

    //删除一个个人事项
    @Override
    public void deleteAffair(String pageType, long affairId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null)
            return;
        ColSummary summary = getColSummaryById(affair.getObjectId());
        //如果是保存待发，删除个人事项的同时删除整个协同
        if (ColListType.draft.name().equals(pageType)) {

            long summaryId = affair.getObjectId();
            List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.collaboration, summaryId);

            boolean deleteCheck = true;

            if (affair.getState().intValue() != StateEnum.col_waitSend.getKey()) {
                // 防止多个点重复操作删除
                deleteCheck = false;
            }

            List<Long> ids = new ArrayList<Long>();
            for (CtpAffair a : affairs) {
                ids.add(a.getId());
                if (!a.getId().equals(affair.getId()) && a.getState().intValue() == StateEnum.col_pending.getKey()) {
                    // 防护异常删除
                    deleteCheck = false;
                }
            }

            if (!deleteCheck) {
                return;
            }

            deleteColSummaryUseHqlById(summaryId);
            affairManager.deletePhysicalByObjectId(summaryId);

            //删除归档文档
            Boolean hasDoc = AppContext.hasPlugin("doc");
            if (hasDoc && docApi != null) {
                docApi.deleteDocResources(user.getId(), ids);
            }


            //删除表单数据
            try {
                if (ColUtil.isForm(affair.getBodyType())) {
                    LOG.info("待发删除表单数据：summayId:" + summaryId);
                    capFormManager.deleteContentAndFormData(ModuleType.collaboration, summaryId, true);
                }
            } catch (Throwable e) {
                LOG.error("删除表单数据异常", e);
                throw new BusinessException(e);
            }


            //删除流程日志
            if (Strings.isNotBlank(summary.getProcessId())) {
                processLogManager.deleteLog(Long.parseLong(summary.getProcessId()));
            }
        } else {
            //如果是待办，删除个人事项的同时finishWorkitem
            if (ColListType.pending.name().equals(pageType)) {

                Comment comment = new Comment();
                comment.setId(UUIDLong.longUUID());
                comment.setAffairId(affairId);
                comment.setModuleId(affair.getObjectId());
                comment.setClevel(1);
                comment.setCtype(0);
                comment.setHidden(false);
                comment.setContent("");
                comment.setModuleType(ApplicationCategoryEnum.collaboration.getKey());
                comment.setPath("001");
                comment.setPid(0l);
                comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
                comment.setCreateId(user.getId());
                comment.setPushMessage(false);

                Map<String, Object> param = new HashMap<String, Object>();
                param.put(WorkFlowEventListener.AFFAIR_SUB_STATE, String.valueOf(SubStateEnum.col_done_delete.getKey()));
                transFinishWorkItemPublic(affair.getId(), comment, param);
            }
            affairManager.deleteAffair(affair.getId());
        }
        appLogManager.insertLog(user, AppLogAction.Coll_Delete, user.getName(), affair.getSubject());
        //删除事项更新全文检索库
        if (AppContext.hasPlugin("index")) {
            if (ColUtil.isForm(affair.getBodyType())) {
                indexManager.update(affair.getObjectId(), ApplicationCategoryEnum.form.getKey());
            } else {
                indexManager.update(affair.getObjectId(), ApplicationCategoryEnum.collaboration.getKey());
            }
        }

        //协同列表删除监听
        CollaborationDelEvent takedel = new CollaborationDelEvent(this);
        takedel.setSummaryId(summary.getId());
        takedel.setAffair(affair);
        EventDispatcher.fireEvent(takedel);

    }


    /**
     * 由当前协同转发过其他协同的情况下，只删除附件对象，不删除file表的数据和物理文件
     *
     * @throws BusinessException
     */
    private void deleteAttachments(ColSummary summary) throws BusinessException {
        Long summaryId = summary.getId();

        boolean isOnlyDeleteAtt = Strings.isNotBlank(summary.getForwardMember());
        if (!isOnlyDeleteAtt) {
            List<ColSummary> summarys = colDao.findColSummarysByParentId(summaryId);
            if (Strings.isNotEmpty(summarys)) {
                isOnlyDeleteAtt = true;
            }
        }
        if (isOnlyDeleteAtt) {//只删除附件，不删除V3xFile/文件

            attachmentManager.deleteOnlyAttByReference(summaryId);
            return;
        }
        this.attachmentManager.removeByReference(summaryId);
    }


    public void deleteColSummaryUseHqlById(Long id) throws BusinessException {
        colDao.deleteColSummaryById(id);
    }


    public void transUpdateCurrentInfo(Map<String, Object> ma) throws BusinessException {
        CtpAffair affair = affairManager.get(Long.valueOf((String) ma.get("affairId")));
        ColSummary summary = getSummaryById(affair.getObjectId());
        ColUtil.updateCurrentNodesInfo(summary);
        updateColSummary(summary);
    }

    /**
     * 协同取回
     *
     * @param workItemId
     * @return
     * @throws BPMException
     */
    public Map<String, Object> colTakeBack(ColSummary summary, CtpAffair affair) throws BPMException {

        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentWorkitemId(affair.getSubObjectId());
        context.setBussinessId(String.valueOf(summary.getId()));
        context.setAppObject(summary);
        context.setBusinessData("operationType", WorkFlowEventListener.TAKE_BACK);
        context.setBusinessData(WorkFlowEventListener.CTPAFFAIR_CONSTANT, affair);
        context.setMastrid(affair.getFormRecordid() == null ? null : String.valueOf(affair.getFormRecordid()));
        context.setFormData(affair.getFormAppId() == null ? null : String.valueOf(affair.getFormAppId()));
        context.setFormAppId(affair.getFormAppId() == null ? null : String.valueOf(affair.getFormAppId()));
        context.setProcessId(affair.getProcessId());
        context.setAppName("collaboration");
        context.setAffairId(String.valueOf(affair.getId()));

        Integer result = wapi.takeBack(context);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("ireturn", result);
        map.put(WorkFlowEventListener.BUSINESS_DATA, context.getBusinessData());

        return map;
    }

    public String transTakeBack(Map<String, Object> ma) throws BusinessException {
        String msg = null;
        String processId = null;
        boolean isLock = false;
        Long affairId = Long.valueOf(String.valueOf(ma.get("affairId")));
        CtpAffair affair = affairManager.get(Long.valueOf(String.valueOf(ma.get("affairId"))));

        try {
            isLock = colLockManager.canGetLock(affairId);
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作transTakeBack,affairId:" + affairId);
                return ResourceUtil.getString("collaboration.summary.notDuplicateSub");
            }


            ColSummary summary = getSummaryById(affair.getObjectId());
            processId = summary.getProcessId();

            Map<String, Object> m = this.colTakeBack(summary, affair);
            int result = (Integer) m.get("ireturn");
            Map<String, Object> businessData = (Map<String, Object>) m.get(WorkFlowEventListener.BUSINESS_DATA);
            Map<Long, Long> canceledMidToAidMap = (Map<Long, Long>) businessData.get(WorkFlowEventListener.CANCELED_MIDTOAID_MAP);

            //0表示取回成功；1表示流程已结束，不能取回；2表示当前及后面节点有子流程已结束，不能取回；-1表示后面节点任务事项已处理完成，不能取回；-2表示当前任务事项所在节点为知会节点，不能取回；
            if (result == 0) {
                if (!(Boolean) ma.get("isSaveOpinion")) {
                    updateOpinion2Draft(affair.getId(), summary);
                }

                affair.setState(StateEnum.col_pending.key());
                affair.setSubState(SubStateEnum.col_pending_takeBack.key());
                affair.setCompleteTime(null);
                affair.setOverTime(null);
                affair.setOverWorktime(null);
                affair.setRunTime(null);
                affair.setRunWorktime(null);
                affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
               /* affair.setFirstResponsePeriod(null);
                affair.setFirstViewDate(null);
                affair.setFirstViewPeriod(null);
                affair.setSignleViewPeriod(null);
               */
                //取回后 将代理人清空
                affair.setTransactorId(null);
                //取回的上一处理人设置成自己
                //affair.setPreApprover(affair.getMemberId());
                //affair.setBackFromId(null);//取回的时候清空 回退人id  OA-77496  存在串发流程A->B->C；C回退给B，B提交后再取回，查看B的待办列表和栏目，都没有显示回退图标。。。。。
                affairManager.updateAffair(affair);

//                String currentNodesInfo = Strings.joinDelNull(";", summary.getCurrentNodesInfo(),String.valueOf(affair.getMemberId()));
//                summary.setCurrentNodesInfo(currentNodesInfo);
                ColUtil.updateCurrentNodesInfo(summary);

                updateColSummary(summary);//更新当前处理人信息
                //流程日志
                processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.takeBack);

                //流程取回事件
                CollaborationTakeBackEvent takeBackEvent = new CollaborationTakeBackEvent(this);
                takeBackEvent.setSummaryId(summary.getId());
                takeBackEvent.setAffair(affair);
                EventDispatcher.fireEvent(takeBackEvent);

                //调用表单万能方法,更新状态，触发子流程等
                if (ColUtil.isForm(summary.getBodyType())) {
                    try {
                        List<Comment> commentList = this.commentManager.getCommentAllByModuleId(ModuleType.collaboration, summary.getId());
                        capFormManager.updateDataState(summary, affair, ColHandleType.takeBack, commentList);
                    } catch (Exception e) {
                        LOG.error("更新表单相关信息异常", e);
                        throw new BusinessException("更新表单相关信息异常", e);
                    }
                }
                /**项目：徐矿集团 【屏蔽回退消息】 作者：jiangchenxi 时间：2019年3月14日 start*/
                //boolean isOpen = xkjtManager.issendmessage(affair.getId(), affair.getSenderId());
                //if(isOpen){
                colMessageManager.transTakeBackMessage(null, affair, summary.getId(), canceledMidToAidMap);
                //}
                /**项目：徐矿集团 【屏蔽回退消息】 作者：jiangchenxi 时间：2019年3月14日 end*/
                //全文检索
                if (AppContext.hasPlugin("index")) {
                    if (ColUtil.isForm(summary.getBodyType())) {
                        indexManager.update(summary.getId(), ApplicationCategoryEnum.form.getKey());
                    } else {
                        indexManager.update(summary.getId(), ApplicationCategoryEnum.collaboration.getKey());
                    }
                }
            } else if (result == 1) {
                msg = "流程已结束，不能取回；";
            } else if (result == 2) {
                msg = "当前及后面节点有子流程已结束，不能取回";
            } else if (result == -1) {
                msg = "后面节点任务事项已处理完成，不能取回";
            } else if (result == -2) {
                msg = "当前任务事项所在节点为知会节点，不能取回";
            }
            //表单解锁，冗余处理，防止窗口关闭时的事件未执行
            boolean isForm = ColUtil.isForm(affair.getBodyType());
            Long formAppId = affair.getFormAppId();
            if (isForm && formAppId != null) {
                //List<Lock> locks = lockManager.getLocks(summary.getFormRecordid());
                //协同V5 OA-76181 all,表单锁，人员A打开了表单后，人员B取回表单后，能在待办中编辑表单    jiesuodaimashanchu
                // formManager.unlockFormData(summary.getFormRecordid());
            }
        } finally {
            if (isLock) {
                colLockManager.unlock(Long.valueOf(affairId));
            }
            this.wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
        }
        //新竞争执行 shenwei
        String pquanxian = affair.getNodePolicy();
        String nquanxian = "";
        Date date=affair.getUpdateDate();
        Long childactivityID = 0L;
        if (pquanxian.equals("请假转送")) {
            nquanxian = "请假集团领导";
        }
        if (nquanxian.equals("请假集团领导")) {

            List<CtpAffair> plist = new ArrayList<CtpAffair>();
            try {
                String hql = "from CtpAffair where state=4 and activityId=:activityId and   nodePolicy = :nodePolicy and objectId = :objectId ";
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("nodePolicy", pquanxian);
                map.put("activityId", affair.getActivityId().longValue());
                map.put("objectId", affair.getObjectId().longValue());

                plist = affairManager.getAffairsByNodePolicyAndState(hql, map);
            } catch (BusinessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (plist.size() > 0) {
                for (CtpAffair ctpAffair : plist) {
//                    zhou:修改
                    String hql = "update CtpAffair a set a.state=:state ,a.subState=:subState,a.completeTime=:completeTime where  a.id=:id";
                    Map<String, Object> params = new HashMap<>();
                    params.put("state", 3);
                    params.put("subState", 6);
                    params.put("completeTime", new Date());
                    params.put("id", ctpAffair.getId().longValue());
                    try {
                        affairManager.update(hql, params);
                    } catch (BusinessException e) {
                        e.printStackTrace();
                        System.out.println("取回时修改状态值的hql语句出错了：" + e.getMessage());
                    }
                }
//                    在这里记录被取回的数据的ctpaffair的id startd
                Map<String, Object> map6 = new HashMap<>();
                map6.put("activityId", affair.getActivityId());
                map6.put("objectId", affair.getObjectId());
                String hql3 = "from CtpAffair where state=3 and nodePolicy='请假转送' and  activityId=:activityId and objectId=:objectId ";
                List<CtpAffair> list3 = affairManager.findState6(hql3, map6);
                XkjtTemp temp = null;
                if (list3.size() > 0) {
                    for (CtpAffair a : list3) {
                        temp = new XkjtTemp();
                        temp.setId(Long.toString(a.getId()));
                        temp.setSummaryId(Long.toString(a.getObjectId()));
                        temp.setFlag("2");//2代表取回的数据
                        tempManager.saveXkjtTemp(temp);
                    }
                }
//                    在这里记录被取回的数据的ctpaffair的id end
            }

        }
        return msg;
    }

    /**
     * 将处理意见变成草稿状态
     *
     * @param id
     * @param summary
     */
    private Long updateOpinion2Draft(Long id, ColSummary summary) throws BusinessException {
        List<CtpCommentAll> allDelOpin = getDealOpinion(id);
        Long commentId = 0l;
        if (!allDelOpin.isEmpty()) {
            CtpCommentAll all = allDelOpin.get(0);
            all.setCtype(Comment.CommentType.draft.getKey());
            Comment comment = new Comment(all);
            commentManager.updateCommentCtype(comment.getId(), Comment.CommentType.draft);
            ColUtil.removeOneReplyCounts(summary);
            List<Comment> list = commentManager.getCommentList(ModuleType.collaboration, summary.getId(), all.getId());
            commentManager.deleteCommentAllByModuleIdAndParentId(ModuleType.collaboration, summary.getId(), all.getId());
            if (Strings.isNotEmpty(list)) {
                for (Comment c : list) {
                    attachmentManager.deleteByReference(summary.getId(), c.getId());
                }
            }
        }
        return commentId;
    }

    private List<CtpCommentAll> getDealOpinion(Long affairId) {
        StringBuffer hql = new StringBuffer();
        hql.append("from CtpCommentAll where affairId=:affairId and ctype=:ctype order by createDate desc");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("affairId", affairId);
        params.put("ctype", Comment.CommentType.comment.getKey());
        return DBAgent.find(hql.toString(), params);
    }

    public Comment getDraftOpinion(Long affairId) {
        StringBuffer hql = new StringBuffer();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append(" from CtpCommentAll where affairId=:affairId and ctype=:ctype ");
        params.put("affairId", affairId);
        params.put("ctype", Comment.CommentType.draft.getKey());
        List<CtpCommentAll> list = (List<CtpCommentAll>) DBAgent.find(hql.toString(), params);
        Comment comment = null;
        if (Strings.isNotEmpty(list)) {
            comment = new Comment(list.get(0));
        }
        return comment;
    }


    @Override
    public NewCollTranVO transComeFromWaitSend(NewCollTranVO vobj) throws BusinessException {
        // 来自待发
        long summaryId = Long.valueOf(vobj.getSummaryId());
        ColSummary summary = getSummaryById(summaryId);

        if (null != summary && null != summary.getResentTime() && summary.getResentTime() != 0) {
            vobj.setReadOnly(Boolean.TRUE);
        }

        CtpAffair affair = affairManager.get(Long.valueOf(vobj.getAffairId()));
        if (affair == null) {
            affair = affairManager.getAffairs(Long.valueOf(summaryId), StateEnum.col_waitSend).get(0);
        }

        boolean isSpecailBackSubmitTo = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());

        // 需要clone一份 不然对数据进行修改的时候 会改到数据库的内容
        ColSummary summaryCloneColSummary = null;
        try {
            summaryCloneColSummary = (ColSummary) summary.clone();
            summaryCloneColSummary.setId(summary.getId());
            summary = summaryCloneColSummary;
            if (null != summaryCloneColSummary.getParentformSummaryid() && !summaryCloneColSummary.getCanEdit()) {
                AppContext.putRequestContext("parentSummaryId", summaryCloneColSummary.getParentformSummaryid());
                AppContext.putRequestContext("_hideContentType", "1");
            }
        } catch (CloneNotSupportedException e1) {
            LOG.error("", e1);
        }
        vobj.setSummary(summary);
        String attListJSON = getSummaryAttachmentJsonsIncludeSender(summaryId);
        vobj.setAttListJSON(attListJSON);
        Long templateId = summary.getTempleteId();
        CtpTemplate template = null;
        if (templateId != null) {
            vobj.setTempleteId(String.valueOf(templateId));
            template = templateManager.getCtpTemplate(templateId);
            if (template != null) {
                AppContext.putRequestContext("curTemplateID", template.getId());
                if (null == template.getFormParentid()) {
                    vobj.setParentWrokFlowTemplete(template.isSystem() && "workflow".equals(template.getType()));
                    vobj.setParentTextTemplete(template.isSystem() && "text".equals(template.getType()));
                    vobj.setParentColTemplete(template.isSystem() && "template".equals(template.getType()));
                } else {
                    vobj.setParentWrokFlowTemplete(isParentWrokFlowTemplete(template.getFormParentid()));
                    vobj.setParentTextTemplete(isParentTextTemplete(template.getFormParentid()));
                    vobj.setParentColTemplete(isParentColTemplete(template.getFormParentid()));
                }
                //这里可以优化
                CtpTemplate pTemplate = getParentSystemTemplete(template.getId());
                Boolean isFromSystem = template.isSystem();
                if (isFromSystem != null && !isFromSystem) {//个人模板
                    isFromSystem = (pTemplate == null || pTemplate.isSystem() == null) ? false : pTemplate.isSystem();
                } else {
                    isFromSystem = true;
                }
                vobj.setFromSystemTemplete(isFromSystem);
                vobj.setTemplate(template);
                vobj.setFromTemplate(template.isSystem());
                if (template.isSystem()) {
                    vobj.setSystemTemplate(true);
                } else {
                    vobj.setSystemTemplate(false);
                }

                ColSummary tSummary = summary;
                if (pTemplate != null) {
                    tSummary = (ColSummary) XMLCoder.decoder(pTemplate.getSummary());

                }
                vobj.setTempleteHasDeadline(tSummary.getDeadline() != null && tSummary.getDeadline() != 0);
                vobj.setTempleteHasRemind(tSummary.getAdvanceRemind() != null && (tSummary.getAdvanceRemind() != 0 && tSummary.getAdvanceRemind() != -1));
                vobj.setCanEditColPigeonhole(tSummary.getArchiveId() != null);
                vobj.setTemplateHasProcessTermType(tSummary.getProcessTermType() != null);
                vobj.setTemplateHasRemindInterval(tSummary.getRemindInterval() != null && tSummary.getRemindInterval() > 0);
                String scanCodeInput = "0";
                if (template != null && null != template.getScanCodeInput() && template.getScanCodeInput()) {
                    scanCodeInput = "1";
                }
                AppContext.putRequestContext("scanCodeInput", scanCodeInput);
                if (null != tSummary.getRemindInterval()) {
                    AppContext.putRequestContext("_RemindInterval", tSummary.getRemindInterval());
                }
                String formtitle = template.getSubject();
                Long templeteFormparnetId = template.getFormParentid();
                if (null != templeteFormparnetId) {
                    vobj.setTemformParentId(String.valueOf(templeteFormparnetId));
                }
                //如果是模板的都重新生成标题
                // colSubject为表单用流程标题规则
                if (Strings.isNotBlank(template.getColSubject())) {
                    vobj.setCollSubjectNotEdit(true);
                    vobj.setCollSubject(template.getColSubject());
                }
                vobj.setFormtitle(formtitle);
                //包含的是协同创建时间就重新设置标题
                String mSubject = getTemplateSubject(formtitle, summary.getCreateDate(), false);
                if (null != summary.getSubject() && summary.getSubject().equals(mSubject)) {
                    summary.setSubject(formtitle);
                    String fsubject = getTemplateSubject(formtitle, new Date(), false);
                    summary.setSubject(fsubject);
                }
                Long standardDuration = template.getStandardDuration();
                if (null != standardDuration) {
                    vobj.setStandardDuration(template.getStandardDuration().toString());
                }
                if (!"text".equals(template.getType())) {
                    String formViewOperation = "";
                    try {
                        Long rightTemplateId = templeteFormparnetId != null ? templeteFormparnetId : template.getId();
                        formViewOperation = ContentUtil.findRightIdbyAffairIdOrTemplateId(null, rightTemplateId);
                    } catch (BusinessException e) {
                        LOG.error("", e);
                        throw new BusinessException(e);
                    }
                    vobj.setFormViewOperation(formViewOperation);
                }
                if (isFromSystem && !isSpecailBackSubmitTo) {

                    //格式模板5个选项前台全部可以编辑，流程模板可以编辑4个，改变流程不能编辑，协同模板全部不能编辑
                    //这个地方重新取模板的原因是：保存待发的流程，没有发出去，重新读取模板的所有设置。
                    if ("template".equals(template.getType())) {
                        summary.setCanArchive(tSummary.getCanArchive());
                        summary.setCanEdit(tSummary.getCanEdit());
                        summary.setCanEditAttachment(tSummary.getCanEditAttachment());
                        summary.setCanModify(tSummary.getCanModify());
                        summary.setCanForward(tSummary.getCanForward());
                    }

                    if ("workflow".equals(template.getType())) {
                        summary.setCanModify(tSummary.getCanModify());
                    }

                    //项目
                    Long projectId = pTemplate.getProjectId();
                    if (null != projectId && null != projectApi) {
                        ProjectBO ps = projectApi.getProject(projectId);
                        if (null != ps && ProjectBO.STATE_DELETE.intValue() != ps.getProjectState()) {
                            summary.setProjectId(projectId);
                        }
                    }
                    // 关联文档  预归档到
                    if (null != tSummary.getArchiveId()) {
                        summary.setArchiveId(tSummary.getArchiveId());
                    }
                    if (Strings.isNotBlank(tSummary.getAdvancePigeonhole())) {
                        summary.setAdvancePigeonhole(tSummary.getAdvancePigeonhole());
                    }
                    if (tSummary.getDeadline() != null && tSummary.getDeadline() != 0) {
                        summary.setDeadline(tSummary.getDeadline());
                    }
                    if (tSummary.getAdvanceRemind() != null && tSummary.getAdvanceRemind() != 0) {
                        summary.setAdvanceRemind(tSummary.getAdvanceRemind());
                    }

                    if (tSummary.getSupervisors() != null) {
                        summary.setSupervisors(tSummary.getSupervisors());
                    }

                }
            } else {
                //如果模板不存在不让发送
                return null;
            }
        }

        if (AppContext.hasPlugin("doc")) {
            if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {//预归档
                if (Strings.isBlank(String.valueOf(summary.getArchiveId()))) {
                    LOG.warn("预归档时，ArchiveId为空，AdvancePigeonhole值为：" + summary.getId());
                    vobj.setAdvancePigeonhole("");
                    vobj.setArchiveName("");
                    AppContext.putRequestContext("setDisabled", true);
                } else {
                    String archiveName = ColUtil.getAdvancePigeonholeName(summary.getArchiveId(), summary.getAdvancePigeonhole(), "template");
                    if ("wendangisdeleted".equals(archiveName)) {
                        vobj.setAdvancePigeonhole("");
                        vobj.setArchiveName("");
                        AppContext.putRequestContext("setDisabled", true);
                    } else {
                        vobj.setAdvancePigeonhole(summary.getAdvancePigeonhole());
                        vobj.setArchiveName(archiveName);
                    }
                }
            } else if (summary.getArchiveId() != null) {
                vobj.setArchiveId(summary.getArchiveId());
                vobj.setArchiveName(docApi.getDocResourceName(summary.getArchiveId()));
            }
        }

        LOG.info("summary.getProcessId()=" + summary.getProcessId() + ",summary.getSubject()=" + summary.getSubject());
        if (summary.getProcessId() != null) {
            vobj.setProcessId(summary.getProcessId());
            vobj.setCaseId(summary.getCaseId());
        }

        Long projectId = summary.getProjectId();
        vobj.setProjectId(projectId);

        if (template != null && Strings.isNotBlank(template.getColSubject())) {
            vobj.setCollSubjectNotEdit(true);
            if (String.valueOf(SubStateEnum.col_pending_specialBacked.key()).equals(affair.getSubState().toString())) {
                vobj.setCollSubject(affair.getSubject());
            } else {
                vobj.setCollSubject(template.getColSubject());
            }
        }
        vobj.setAffair(affair);
        vobj.setAffairId(String.valueOf(affair.getId()));

        superviseManager.parseProcessSupervise(summary.getId(), templateId, summary.getStartMemberId(), SuperviseEnum.EntityType.summary);

        //判断是否是cap4转发表单
        Long formAppid = summary.getFormAppid();
        if (null != formAppid) {
            boolean isCap4Forward = capFormManager.isCAP4Form(formAppid) && String.valueOf(MainbodyType.HTML.getKey()).equals(summary.getBodyType());
            AppContext.putRequestContext("isCap4Forward", isCap4Forward ? "1" : "0");
        }

        return vobj;
    }

    @Override
    public void updateAffairStateWhenClick(CtpAffair affair) throws BusinessException {
        Integer sub_state = affair.getSubState();
        if (sub_state == null || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()) {

            //更新第一次查看时间
            Date nowTime = new Date();
            long firstViewTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());

            Map<String, Object> columnValue = new HashMap<String, Object>();
            columnValue.put("subState", SubStateEnum.col_pending_read.key());
            columnValue.put("firstViewPeriod", firstViewTime);
            columnValue.put("firstViewDate", nowTime);

            affairManager.update(affair.getId(), columnValue);

            //要把已读状态写写进流程
            if (affair.getSubObjectId() != null) {
                try {
                    wapi.readWorkItem(affair.getSubObjectId());
                } catch (BPMException e) {
                    LOG.error("", e);
                    throw new BusinessException(e);
                }
            }
        }

    }

    @Override
    public void transSendImmediate(String _summaryIds, String _affairIds, boolean sentFlag) throws BusinessException {
        this.transSendImmediate(_summaryIds, _affairIds, sentFlag, null, null, null, "");
    }

    @Override
    public NewCollTranVO transResend(NewCollTranVO vobj) throws BusinessException {
        long summaryId = Long.parseLong(vobj.getSummaryId());
        ColSummary summary = getColSummaryById(summaryId);
        vobj.setSummary(summary);
        String attListJSON = getSummaryAttachmentJsonsIncludeSender(summaryId);
        vobj.setAttListJSON(attListJSON);
        if (summary.getProcessId() != null) {
            vobj.setProcessId(summary.getProcessId());
        }
        //预归档
        if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {
            String archiveName = ColUtil.getAdvancePigeonholeName(summary.getArchiveId(), summary.getAdvancePigeonhole(), "col");
            //页面上传原来的字符串，和现实名字，优先显示高级归档
            vobj.setArchiveName(archiveName);
        } else if (null != summary.getArchiveId() && Strings.isNotBlank(String.valueOf(summary.getArchiveId()))) {
            vobj.setArchiveId(summary.getArchiveId());
            vobj.setArchiveName(ColUtil.getArchiveNameById(summary.getArchiveId()));
            vobj.setArchiveAllName(ColUtil.getArchiveAllNameById(summary.getArchiveId()));
        }
        //来自"重新发起" 不允许修改标题
        vobj.setReadOnly(Boolean.TRUE);
        boolean cloneOriginalAtts = true;
        vobj.setCloneOriginalAtts(cloneOriginalAtts);
        if (summary.getTempleteId() != null) {
            CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
            vobj.setTemplate(ctpTemplate);
            vobj.setSystemTemplate(ctpTemplate.isSystem());
            ColSummary colSummary = (ColSummary) XMLCoder.decoder(ctpTemplate.getSummary());
            vobj.setTempleteHasRemind(colSummary.getAdvanceRemind() != null && (colSummary.getAdvanceRemind() != 0 && colSummary.getAdvanceRemind() != -1));
            vobj.setTempleteHasDeadline(colSummary.getDeadlineDatetime() != null);
            vobj.setTempleteId(summary.getTempleteId().toString());
            vobj.setTemplate(templateManager.getCtpTemplate(summary.getTempleteId()));
            //modelAndView.addObject("isFromTemplate", summary.getTempleteId() != null);
            vobj.setFromTemplate(summary.getTempleteId() != null);
            vobj.setParentWrokFlowTemplete(isParentWrokFlowTemplete(summary.getTempleteId()));
            vobj.setParentTextTemplete(isParentTextTemplete(summary.getTempleteId()));
            vobj.setParentColTemplete(isParentColTemplete(summary.getTempleteId()));
            vobj.setFromSystemTemplete(isParentSystemTemplete(summary.getTempleteId()));
        }
        vobj.setProjectId(summary.getProjectId());
        vobj.setResendFlag(true);
        CopySuperviseFromSummary(vobj, summary.getId());
        return vobj;
    }

    /**
     * 或者正文的附件，包括发起人附言的，主要用于重复发起，编辑待发两种情况下，将发起人附言的附件直接显示到正文区域。
     *
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    private String getSummaryAttachmentJsonsIncludeSender(long summaryId) throws BusinessException {
        //取正文的附件和发起人附言的。
        List<Attachment> showAtts = new ArrayList<Attachment>();
        List<Attachment> list = attachmentManager.getByReference(summaryId);
        List<Comment> comments = commentManager.getCommentList(ModuleType.collaboration, summaryId);
        List<Long> showlds = new ArrayList<Long>();
        showlds.add(summaryId);
        if (Strings.isNotEmpty(comments)) {
            for (Comment c : comments) {
                if (Integer.valueOf(Comment.CommentType.sender.getKey()).equals(c.getCtype())) {
                    showlds.add(c.getId());
                }
            }
        }
        if (Strings.isNotEmpty(list)) {
            for (Attachment a : list) {
                if (showlds.contains(a.getSubReference())) {
                    Attachment aclone = null;
                    try {
                        aclone = (Attachment) a.clone();
                        aclone.setSubReference(a.getReference());
                    } catch (CloneNotSupportedException e) {
                        LOG.error("", e);
                    }
                    if (aclone != null) {
                        showAtts.add(aclone);
                    }
                }
            }
        }
        ColSelfUtil.sortAttachmentList(showAtts);
        String attListJSON = attachmentManager.getAttListJSON(showAtts);
        return attListJSON;
    }

    private Map<String, Object> transRepalFront(String summaryId, String repealComment, String affairId, String isWFTrace, Integer operationType, Boolean needSendMessage, String extAtt1) throws BusinessException {
        String processId = null;
        Long recordId = null;
        try {
            int result = 0;
            Long _summaryId = Long.parseLong(summaryId);
            Long _affairId = Long.parseLong(affairId);
            ColSummary summary = getColSummaryById(_summaryId);
            CtpAffair currentAffair = affairManager.get(_affairId);

            Long caseId = summary.getCaseId();
            processId = summary.getProcessId();
            recordId = summary.getFormRecordid();
            if (Strings.isNotBlank(repealComment)) {
                repealComment = repealComment.replaceAll(new String(new char[]{(char) 160}), " ");
            }
            String info = "";
            AffairData affairData = ColUtil.getAffairData(summary);
            Map<String, Object> businessData = new HashMap<String, Object>();
            businessData.put(WorkFlowEventListener.OPERATION_TYPE, WorkFlowEventListener.CANCEL);
            businessData.put(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID, _affairId);
            businessData.put(WorkFlowEventListener.CURRENT_OPERATE_SUMMARY_ID, _summaryId);
            businessData.put(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_CONTENT, repealComment);
            businessData.put(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_EXTATT1, extAtt1);
            businessData.put(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW, isWFTrace);
            businessData.put(WorkFlowEventListener.CURRENT_OPERATE_NEED_SEND_MESSAGE, needSendMessage);
            businessData.put(WorkFlowEventListener.CTPAFFAIR_CONSTANT, currentAffair);

            affairData.setBusinessData(businessData);

            //撤销流程
            if (caseId != null) {
                result = ContentUtil.cancelCase(summary, affairData, caseId);
            } else {
                //caseId为null表示流程已经撤销了，应将affair相关数据状态进行更新：已发进待发，待办更新为回退状态，以免用户看到垃圾数据
                //fix:    OA-116634 性能测试环境，将一个1000并发的岗位节点协同反复撤销发送，最后一次撤销后协同仍显示在已发中
                repairData(_summaryId);
            }
            String colSubject = summary.getSubject();
            if (result == -1) {
                info = "协同《" + colSubject + "》未生成完毕，请稍后进行撤销操作";
            } else if (result == -2) {// 新流程已结束不能撤销，已在manager里提示, 如果该流程触发的新流程已结束，不能撤销
                info = "该协同触发的子流程《" + colSubject + "》已结束，不允许撤销！";
            } else if (result == 1) {
                info = "协同《" + colSubject + "》已经结束,不允许撤销";
            }
            //表单解锁，冗余处理，防止窗口关闭时的事件未执行
            boolean isForm = ColUtil.isForm(summary.getBodyType());
            Long formAppId = currentAffair.getFormAppId();
            if (isForm && formAppId != null) {
                formManager.unlockFormData(summary.getFormRecordid());
                ;
            }
            //更新第一次处理时间
            affairManager.updateSignleViewTime(currentAffair);

            businessData.put("result", info);
            return businessData;
        } catch (Exception e) {
            LOG.error("", e);
            throw new BusinessException("", e);
        } finally {
            this.officeLockManager.unlockAll(recordId);
            //this.wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
            this.colDelLock(Long.valueOf(affairId), true);
        }
    }

    /**
     * 协同已撤销，修复已发和待办数据
     */
    private void repairData(Long _summaryId) throws BusinessException {
        CtpAffair senderAffair = affairManager.getSenderAffair(_summaryId);
        if (null != senderAffair && senderAffair.getState() == StateEnum.col_sent.key()) {//还是已发状态，需要对垃圾数据进行兼容处理
            senderAffair.setState(StateEnum.col_waitSend.key());
            affairManager.updateAffair(senderAffair);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            StringBuilder hql = new StringBuilder();
            hql.append("update CtpAffair set state=:state,subState=:subState,updateDate=:updateDate where id!=:id and objectId=:objectId ");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("state", StateEnum.col_stepBack.key());
            params.put("subState", SubStateEnum.col_normal.key());
            params.put("objectId", senderAffair.getObjectId());
            params.put("updateDate", now);
            params.put("id", senderAffair.getId());
            DBAgent.bulkUpdate(hql.toString(), params);
        }
    }

    @Override
    public void transRepalBackground(Long _summaryId, Long _affairId, String repealComment, String trackWorkflowType,
                                     Integer operationType, Boolean needSendMessage, String extAtt1, Map<String, Object> businessData) throws BusinessException {
        ColSummary summary = getColSummaryById(_summaryId);
        this.transRepalBackground(summary, _affairId, repealComment, trackWorkflowType, operationType, needSendMessage, extAtt1, businessData);
    }

    @Override
    public void transRepalBackground(ColSummary summary, Long _affairId, String repealComment, String trackWorkflowType,
                                     Integer _ioperationType, Boolean needSendMessage, String extAtt1, Map<String, Object> businessData) throws BusinessException {
        //是否是回退操作，回退操作就不需要重复保存意见，发消息，流程日志之类的了。
        boolean isBackOperation = WorkFlowEventListener.WITHDRAW.equals(_ioperationType) || WorkFlowEventListener.SPECIAL_BACK_RERUN.equals(_ioperationType);

        if (Strings.isNotBlank(repealComment)) {
            repealComment = repealComment.replaceAll(new String(new char[]{(char) 160}), " ");
        }
        String colSubject = "";
        Comment comment = new Comment();
        comment.setAffairId(_affairId);
        comment.setCtype(CommentType.comment.getKey());
        String repealCommentTOHTML = repealComment;
        try {
            User user = AppContext.getCurrentUser();
            colSubject = summary.getSubject();
            //将summary的状态改为待发,撤销已生成事项
            List<StateEnum> states = new ArrayList<StateEnum>();
            states.add(StateEnum.col_sent);
            states.add(StateEnum.col_pending);
            states.add(StateEnum.col_done);
            states.add(StateEnum.col_waitSend);
            List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), states);
            CtpAffair currentAffair = affairManager.get(comment.getAffairId());

            //撤销的时候刷新表单标题
            if (summary.getTempleteId() != null && String.valueOf(MainbodyType.FORM.getKey()).equals(summary.getBodyType())) {
                CtpTemplate ctpTemplate = templateManager.getCtpTemplate(summary.getTempleteId());
                if (null != ctpTemplate) {
                    Map<String, Object> tempMap = new HashMap<String, Object>();
                    tempMap.put("templateColSubject", ctpTemplate.getColSubject());
                    tempMap.put("templateWorkflowId", ctpTemplate.getWorkflowId());
                    checkCollSubject(summary, currentAffair, tempMap);
                }
            }

            do4Repeal(user.getId(), comment.getContent(), summary, affairs, true, _ioperationType);


            ColUtil.deleteQuartzJob(summary.getId());
            //删除节点的定时任务
            for (CtpAffair affair : affairs) {
                if (affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0 || affair.getExpectedProcessTime() != null) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("affair", affair);
                    colTaskManager.addTask(data);
                }
            }

            //更新已发为待发事项的状态
            updateSenderAffairStateToWaitSend(affairs, isBackOperation);

            //将待办已办数据全部设置为撤销状态
            updatePendingAndDoneAffairsToCancel(summary);

            summary.setCaseId(null);
            summary.setCoverTime(Boolean.FALSE);
            summary.setCurrentNodesInfo("");
            summary.setState(CollaborationEnum.flowState.cancel.ordinal());
            ColUtil.addOneReplyCounts(summary);

            updateColSummary(summary);


            //删除ISIgnatureHTML专业签章
            iSignatureHtmlManager.deleteAllByDocumentId(summary.getId());

            if (!isBackOperation) {
                //保存撤销意见,附件
                comment = saveComment4Repeal(comment, repealCommentTOHTML, user, summary, currentAffair, extAtt1);
                //正文和附件
                saveAttDatas(user, summary, currentAffair, comment.getId());
                //勾选了追溯流程才进入这里
                Map<String, Object> traceMap = createRepealTraceWfData(summary, affairs, currentAffair, trackWorkflowType);
                businessData.putAll(traceMap);

                if (null == needSendMessage || needSendMessage) {
                    //发送消息 - 撤销
                    colMessageManager.sendMessage4Repeal(affairs, currentAffair, Strings.toText(comment.getContent()),
                            "1".equals(trackWorkflowType) ? true : false, comment, businessData);
                    //判断当前是什么时候的撤销，有待办、已发、发起者，如果是待办则显示“意见”，如果是其他则显示“附言”
                    String messageLink = "collaboration.summary.cancel";
                    if (currentAffair != null && currentAffair.getState().intValue() == StateEnum.col_pending.getKey()) {
                        messageLink = "collaboration.summary.cancelPending";
                    }
                    //发送消息 - 给被撤销的督办人
                    this.colMessageManager.sendMessage2Supervisor(summary.getId(), ApplicationCategoryEnum.collaboration, summary.getSubject(), messageLink, user.getId(), user.getName(), Strings.toText(comment.getContent()), summary.getForwardMember(), summary.getTempleteId());
                    appLogManager.insertLog(user, AppLogAction.Coll_Repeal, user.getName(), colSubject);
                    processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), -1L, ProcessLogAction.cancelColl, comment.getId());
                }
            }
            //调用表单万能方法,更新状态，触发子流程等
            if (ColUtil.isForm(summary.getBodyType())) {
                try {
                    capFormManager.RelationAuthorityBySummaryId(summary.getId(), ModuleType.collaboration.getKey());//撤销时删除表单授权信息
                    capFormManager.updateDataState(summary, currentAffair, ColHandleType.repeal, null);
                } catch (Exception e) {
                    //throw new BusinessException("更新表单相关信息异常",e);
                    LOG.error("更新表单相关信息异常", e);
                }
            }
            //全文检索
            if (AppContext.hasPlugin("index")) {
                if (ColUtil.isForm(summary.getBodyType())) {
                    indexManager.delete(summary.getId(), ApplicationCategoryEnum.form.getKey());
                } else {
                    indexManager.delete(summary.getId(), ApplicationCategoryEnum.collaboration.getKey());
                }
            }

        } catch (Exception e) {
            LOG.error("", e);
            throw new BusinessException("", e);
        }
    }

    private void updateSenderAffairStateToWaitSend(List<CtpAffair> affairs, boolean isBackOperation) throws BusinessException {
        for (CtpAffair affair : affairs) {
            int state = affair.getState().intValue();
            int subState = affair.getSubState().intValue();
            boolean isSent = state == StateEnum.col_sent.key();
            boolean isWaitSend = state == StateEnum.col_waitSend.key() //撤销的时候 不应该处理待发的状态
                    && (subState == SubStateEnum.col_pending_specialBacked.key() || subState == SubStateEnum.col_pending_specialBackToSenderCancel.key());

            if (isSent || isWaitSend) {
                affair.setState(StateEnum.col_waitSend.key());
                if (isBackOperation) {
                    affair.setSubState(SubStateEnum.col_waitSend_stepBack.key());
                } else {
                    affair.setSubState(SubStateEnum.col_waitSend_cancel.key());
                }
                affair.setDelete(false);
                if (ColUtil.isForm(affair.getBodyType())) {
                    AffairUtil.setIsRelationAuthority(affair, false);
                }
                affair.setSummaryState(CollaborationEnum.flowState.cancel.ordinal());
                affairManager.updateAffair(affair);

                //OA-145253
                User user = AppContext.getCurrentUser();
                List<Long> ids = new ArrayList<Long>();
                ids.add(affair.getId());
                //删除归档文档
                Boolean hasDoc = AppContext.hasPlugin("doc");
                if (hasDoc && docApi != null) {
                    docApi.deleteDocResources(user.getId(), ids);
                }

            }
        }
    }

    private void updatePendingAndDoneAffairsToCancel(ColSummary summary) throws BusinessException {
        Map<String, Object> otherMap = new HashMap<String, Object>();
        otherMap.put("state", StateEnum.col_cancel.key());
        otherMap.put("subState", SubStateEnum.col_normal.key());
        otherMap.put("updateDate", new Timestamp(System.currentTimeMillis()));
        otherMap.put("summaryState", CollaborationEnum.flowState.cancel.ordinal());
        List<Integer> cancelStates = new ArrayList<Integer>();
        cancelStates.add(StateEnum.col_pending.key());
        cancelStates.add(StateEnum.col_done.key());
        cancelStates.add(StateEnum.col_pending_repeat_auto_deal.key());

        Object[][] wheres = new Object[2][2];
        wheres[0] = new Object[]{"objectId", summary.getId()};
        wheres[1] = new Object[]{"state", cancelStates};
        affairManager.update(otherMap, wheres);
    }

    /**
     * Ajax已发列表撤销流程,REST/PC都进这一个方法
     */
    @Override
    public String transRepal(Map<String, Object> tempMap) throws BusinessException {
        Map<String, Object> map = this.transRepealPublic(tempMap);
        String ret = (String) map.get("result");
        return ret;
    }


    public Map<String, Object> transRepealPublic(Map<String, Object> tempMap) throws BusinessException {
        String repealComment = Strings.removeEmoji((String) tempMap.get("repealComment"));
        if (Strings.isNotBlank(repealComment)) {
            repealComment = repealComment.replaceAll(new String(new char[]{(char) 160}), " ");
        }
        String reComment = (String) tempMap.get("collaboration.dealAttitude.cancelProcess");
        if (Strings.isNotBlank(reComment)) {
            repealComment = reComment.replaceAll(new String(new char[]{(char) 160}), " ");
        }
        String affairId = (String) tempMap.get("affairId");
        String summaryId = (String) tempMap.get("summaryId");
        String isWFTrace = (String) tempMap.get("isWFTrace");
        Integer operationType = (Integer) tempMap.get("operationType");
        Boolean needSendMessage = (Boolean) tempMap.get("needSendMessage");
        String extAtt1 = (String) tempMap.get("extAtt1");
        if (null == needSendMessage) {
            needSendMessage = true;
        }
        boolean isLock = false;
        Long laffairId = Long.valueOf(affairId);
        try {
            isLock = colLockManager.canGetLock(laffairId);
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作repeal,affairId:" + laffairId);
                return null;
            }
            return this.transRepalFront(summaryId, repealComment, affairId, isWFTrace, operationType, needSendMessage, extAtt1);
        } finally {
            if (isLock) {
                colLockManager.unlock(laffairId);
            }
        }
    }

    private Map<String, Object> createRepealTraceWfData(ColSummary summary, List<CtpAffair> affairs, CtpAffair currentAffair, String trackWorkflowType) throws BusinessException {
        CtpTemplate t = null;
        if (summary.getTempleteId() != null) {
            t = templateManager.getCtpTemplate(summary.getTempleteId());
        }
        //已办
        //暂存代办
        //自己
        List<CtpAffair> traceAffairs = new ArrayList<CtpAffair>();
        for (CtpAffair aff : affairs) {
            if (Integer.valueOf(StateEnum.col_done.key()).equals(aff.getState())
                    || (Integer.valueOf(StateEnum.col_pending.key()).equals(aff.getState()) && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(aff.getSubState()))
                    || aff.getId().equals(currentAffair.getId())
                    || Integer.valueOf(StateEnum.col_sent.key()).equals(aff.getState())
                    || Integer.valueOf(StateEnum.col_waitSend.key()).equals(aff.getState())) {
                traceAffairs.add(aff);
            }
        }
        return colTraceWorkflowManager.createRepealTraceData(summary, currentAffair, traceAffairs, t, trackWorkflowType);
    }

    /**
     * @param comment
     * @param repealCommentTOHTML
     * @param user
     * @param summary
     * @param currentAffair
     * @throws BusinessException
     */
    private Comment saveComment4Repeal(Comment comment, String repealCommentTOHTML, User user, ColSummary summary, CtpAffair currentAffair, String extAtt1) throws BusinessException {
        ParamUtil.getJsonDomainToBean("comment_deal", comment);

        comment.setCtype(CommentType.comment.getKey());
        comment.setAffairId(currentAffair.getId());

        if (user == null || user.getId() == null) {
            return comment;
        }
        Long userId = user.getId();
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        //TODO 将终止流程的当前Affair放入ThreadLocal，便于工作流中发送消息时获取代理信息。
        comment.setModuleId(summary.getId());
        comment.setExtAtt3("collaboration.dealAttitude.cancelProcess");
        //查询当前事项的代理人
        Long curAgentIDLong = MemberAgentBean.getInstance().getAgentMemberId(
                ApplicationCategoryEnum.collaboration.ordinal(), currentAffair.getMemberId());
        //判断是否是单位管理员
        if (!userId.equals(currentAffair.getMemberId()) && user.isAdmin()) {
            comment.setCreateId(userId);
        } else {
            comment.setCreateId(currentAffair.getMemberId());
        }
        //判断当前用户是否该事项的代理人
        if (userId.equals(curAgentIDLong) && currentAffair.getMemberId().longValue() != userId.longValue()) {
            comment.setExtAtt2(user.getName());
        }
        if (Strings.isBlank(comment.getContent())) {
            comment.setContent(repealCommentTOHTML);
        }
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setPid(0L);
        if (Strings.isNotBlank(extAtt1)) {
            comment.setExtAtt1(extAtt1);
        }
        comment = saveOrUpdateComment(comment);
        return comment;
    }

    @Override
    public Boolean getIsVouchByProcessId(Long processId) throws BusinessException {
        //TODO
        return false;
    }

    @Override
    @AjaxAccess
    public String checkAffairValid(String affairId) throws NumberFormatException, BusinessException {
        return checkAffairValid(affairId, true, "");
    }

    public String checkAffairValid(String affairId, String pageNodePolicy) throws NumberFormatException, BusinessException {
        return checkAffairValid(affairId, true, pageNodePolicy);
    }

    public String checkAffairValid(String affairId, boolean isTraceValid, String pageNodePolicy) throws NumberFormatException, BusinessException {

        CtpAffair affair = getSimpleAffair4Check(affairId);
        return checkAffairValid(affair, isTraceValid, pageNodePolicy);
    }

    private CtpAffair getSimpleAffair4Check(String affairId) {
        CtpAffair affair = null;
        if (Strings.isNotBlank(affairId)) {
            try {
                affair = affairManager.getSimpleAffair(Long.valueOf(affairId));
                if (affair == null) {
                    affair = affairManager.getByHis(Long.valueOf(affairId));
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        return affair;
    }

    public String checkAffairValid(CtpAffair affair, boolean isTraceValid, String nodePolicy) {

        String errorMsg = "";

        if (!ColUtil.isAfffairValid(affair)) {
            errorMsg = ColUtil.getErrorMsgByAffair(affair);
            try {
                if (isTraceValid) {
                    if (affair != null && traceDao.getTraceWorkflowsByAffairId(affair.getId())) {
                        return "";
                    }
                }
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        } else {
            try {
                errorMsg = checkNodePolicyChange(affair, nodePolicy);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }

        return errorMsg;
    }


    /**
     * 随机获取人员在协同里面对应的affair
     *
     * @param summaryId
     * @param _isHistoryFlag
     * @param memberId
     * @return
     * @throws BusinessException
     * @Author : xuqw
     * @Date : 2016年2月19日下午4:32:55
     */
    private CtpAffair findMemberAffair(Long summaryId, boolean _isHistoryFlag, Long memberId)
            throws BusinessException {

        CtpAffair ret = null;

        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        if (_isHistoryFlag) {
            affairs = affairManager.getAffairsHis(ApplicationCategoryEnum.collaboration, summaryId, memberId);
        } else {
            affairs = affairManager.getAffairs(ApplicationCategoryEnum.collaboration, summaryId, memberId);
        }
        if (Strings.isNotEmpty(affairs)) {
            for (CtpAffair aff : affairs) {
                if (!aff.isDelete()) {
                    ret = aff;
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean getDisplayData2VO(ColSummaryVO summaryVO, ColSummary summary, CtpAffair affair, boolean isHistoryFlag) throws BusinessException {

        boolean _isHistoryFlag = isHistoryFlag;

        if (summaryVO.getAffairId() == null) {
            if (Strings.isNotBlank(summaryVO.getSummaryId())) {
                if (_isHistoryFlag) {

                    affair = affairManager.getSenderAffairByHis(Long.valueOf(summaryVO.getSummaryId()));
                } else {
                    affair = affairManager.getSenderAffair(Long.valueOf(summaryVO.getSummaryId()));
                    if (affair == null) {
                        affair = affairManager.getSenderAffairByHis(Long.valueOf(summaryVO.getSummaryId()));
                        if (affair != null) {
                            _isHistoryFlag = true;
                        }
                    }
                }
            } else if (Strings.isNotBlank(summaryVO.getProcessId())) {
                if (summary == null) {
                    _isHistoryFlag = true;
                    summaryVO.setHistoryFlag(true);
                    summary = getColSummaryByProcessIdHistory(Long.valueOf(summaryVO.getProcessId()));
                    if (summary == null) {
                        _isHistoryFlag = false;
                        summaryVO.setHistoryFlag(false);
                        summary = getColSummaryByProcessId(Long.valueOf(summaryVO.getProcessId()));
                    }
                }
                if (summary != null) {
                    affair = affairManager.getSenderAffair(summary.getId());
                    if (_isHistoryFlag) {
                        affair = affairManager.getSenderAffairByHis(summary.getId());
                    } else {
                        affair = affairManager.getSenderAffair(summary.getId());
                        if (affair == null) {
                            affair = affairManager.getSenderAffairByHis(summary.getId());
                            if (affair != null) {
                                _isHistoryFlag = true;
                            }
                        }
                    }
                }
            }
            if (affair != null) {
                summaryVO.setAffairId(affair.getId());
            }
        } else {

            Long aId = summaryVO.getAffairId();
            if (aId != null && aId.intValue() != -1) {
                if (_isHistoryFlag) {
                    affair = affairManager.getByHis(summaryVO.getAffairId());
                } else {
                    affair = affairManager.get(summaryVO.getAffairId());
                    if (affair == null) {

                        affair = affairManager.getByHis(summaryVO.getAffairId());

                        if (affair != null) {
                            _isHistoryFlag = true;
                        }
                    }
                }
            }

            if (affair == null) {
                return false;
            }

            //当前节点可能撤销之类， 找到一套没有被撤销
            if (!ColUtil.isAfffairValid(affair)) {
                CtpAffair aff = findMemberAffair(affair.getObjectId(), _isHistoryFlag, affair.getMemberId());
                if (aff != null) {
                    affair = aff;
                    summaryVO.setAffairId(aff.getId());
                }
            }
        }
        summaryVO.setAffair(affair);
        summaryVO.setSummary(summary);
        return _isHistoryFlag;
    }

    @Override
    public ColSummaryVO transShowSummary(ColSummaryVO summaryVO) throws BusinessException {

        boolean isHistoryFlag = summaryVO.isHistoryFlag();
        User user = AppContext.getCurrentUser();
        CtpAffair affair = null;
        ColSummary summary = null;
        //表单查询穿透，需要显示主流程
        if (ColOpenFrom.formQuery.name().equals(summaryVO.getOpenFrom()) || ColOpenFrom.formStatistical.name().equals(summaryVO.getOpenFrom())) {
            summary = getMainSummary4FormQueryAndStatic(summaryVO.getOpenFrom(), summaryVO.getSummaryId());
            if (summary != null) {
                summaryVO.setSummaryId(summary.getId().toString());
            }
        }

        //关联文档就算当前事项无效，只要在流程中有一个事项是当前用户的，都可以打开。
        isHistoryFlag = getDisplayData2VO(summaryVO, summary, affair, isHistoryFlag);
        if (!AppContext.hasPlugin("fk") && isHistoryFlag) {
            summaryVO.setAffair(null);
        }
        summary = summaryVO.getSummary();
        affair = summaryVO.getAffair();
        AppContext.putRequestContext("isHistoryFlag", isHistoryFlag);

        //业务逻辑开始
        if (affair == null) {
            summaryVO.setErrorMsg(ColUtil.getErrorMsgByAffair(affair));
            return summaryVO;
            // OA-134751
        } else if (null != affair && affair.getState().intValue() == StateEnum.col_sent.getKey()
                && ColOpenFrom.listWaitSend.toString().equals(summaryVO.getOpenFrom())) {
            summaryVO.setOpenFrom(ColOpenFrom.listSent.toString());
        }

        if (summary == null) {
            if (isHistoryFlag) {
                summary = getColSummaryByIdHistory(affair.getObjectId());
            } else {
                summary = getColSummaryById(affair.getObjectId());
            }
            if (summary == null) { //转给指定人的时候，affair的objectid=-1,查不出来东西，这个地方防护下。
                summaryVO.setErrorMsg(ColUtil.getErrorMsgByAffair(affair));
                return summaryVO;
            }
        }

        //*******************安全校验相关************************************

        //如果来自督办，先判断督办权限，否则会出现撤销督办权限以后 还能从消息中打开协同。或者会出现“你无权查看”提示消息不对
        if (ColOpenFrom.supervise.name().equals(summaryVO.getOpenFrom())) {
            boolean isSupervisor = superviseManager.isSupervisor(user.getId(), summary.getId());
            if (!isSupervisor) {
                summaryVO.setErrorMsg(ResourceUtil.getString("collaboration.supercise.cancel.acl"));
                return summaryVO;
            }
            //指定回退流程重走，在待发消息不能打开
            if (affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                summaryVO.setErrorMsg(ColUtil.getErrorMsgByAffair(affair));
                return summaryVO;
            }
        }


        //校验事项是否允许打开,事项删除撤销等都从这里校验,isAfffairValid里面对待发进行了处理，所有这里单独处理一下。
        //待发，表单查询统计部分的待发事项可以打开。
        boolean isFormQuery = ColOpenFrom.formQuery.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.capQuery.name().equals(summaryVO.getOpenFrom());
        boolean isFormStatistical = ColOpenFrom.formStatistical.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.capStatistical.name().equals(summaryVO.getOpenFrom());

        //2013-12-11 OA-50051 产品经理罗雪确认表单中的关联文档可以打开事项
        boolean isFormRelation = ColOpenFrom.formRelation.name().equals(summaryVO.getOpenFrom());
        boolean isDocLib = ColOpenFrom.docLib.name().equals(summaryVO.getOpenFrom());
        boolean isFromListWaitSend = ColOpenFrom.listWaitSend.name().equals(summaryVO.getOpenFrom()) && Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState());
        boolean ifFromstepBackRecord = ColOpenFrom.stepBackRecord.name().equalsIgnoreCase(summaryVO.getOpenFrom());
        boolean isFromrepealRecord = ColOpenFrom.repealRecord.name().equals(summaryVO.getOpenFrom());
        boolean isSubFlow = ColOpenFrom.subFlow.name().equals(summaryVO.getOpenFrom());
        if (!ifFromstepBackRecord && !isFromrepealRecord) {//如果来自督办列表的流程追溯这里就不校验
            if (!isFormQuery && !isFormStatistical && !isFormRelation && !isDocLib && !isFromListWaitSend && !isSubFlow) {
                if (!ColUtil.isAfffairValid(affair, false)) {
                    if (traceDao.getTraceWorkflowsByAffairId(affair.getId())) {
                        summaryVO.setOpenFrom("repealRecord");
                    } else {
                        summaryVO.setErrorMsg(ColUtil.getErrorMsgByAffair(affair));
                        return summaryVO;
                    }
                }
            }
        }

        //SECURITY 访问安全检查，不允许随便在这个地方增加绕过安全校验的逻辑，如确有必要，大家一起讨论决定！！！
        if (!SecurityCheck.isLicit(AppContext.getRawRequest(), AppContext.getRawResponse(), ApplicationCategoryEnum.collaboration,
                user, affair.getId(), affair, summary.getArchiveId(), false)) {

            //协同驾驶舱统计， A统计B的数据， A和B在同一个流程里面，提示A无权查看B的事项，这里做一次防护
            //解决一种场景：在流程中，但是打开的不是自己的事项，以前在SecurityControlColImpl中有判断是否在流程中的逻辑，虽然返回true，但是打开的不是自己的事项，存在权限泄漏，
            //放在外边来，可以解决这种情况，始终打开自己的事项
            CtpAffair aff = findMemberAffair(affair.getObjectId(), isHistoryFlag, user.getId());
            if (aff != null) {
                affair = aff;
                summaryVO.setAffair(aff);
                summaryVO.setAffairId(aff.getId());
            } else {
                //记录非法访问日志及
                SecurityCheck.printInbreakTrace(AppContext.getRawRequest(), AppContext.getRawResponse(),
                        user, ApplicationCategoryEnum.collaboration);
                return null;
            }
        }
        //******************安全校验相关结束**************************************

        checkCanOpenAcl(summaryVO, affair, summary, isHistoryFlag);
        if (Strings.isNotBlank(summaryVO.getErrorMsg())) {
            return summaryVO;
        }

        //判断流程是否处于指定回退的状态
        isInSpecialBack(isHistoryFlag, affair, summary);


        //××××××××××××××××××取正文操作权限×××××××××××××××××××××××××××××××××××××××××
        CtpTemplate template = null;
        String wfOperationId = "";
        if (summary.getTempleteId() != null) {
            template = templateManager.getCtpTemplate(summary.getTempleteId());
            if (template != null) {


                /*因为表单模板在待发中，要去读流程模板绑定的表单权限、但是如果模板被删除，ProcessTemplate被物理删除，所以这里防护一下。
                后续如果processTemplate改为逻辑删除，这个IF就可以干掉了*/
            	/*if(Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState()) && Boolean.TRUE.equals(template.isDelete()) && ColUtil.isForm(template.getBodyType())){

            		summaryVO.setErrorMsg(ResourceUtil.getString("workflow.wapi.exception.msg001"));
            		return summaryVO;
            	}*/
                summaryVO.setCanPraise(template.getCanPraise());

                if (Integer.valueOf(StateEnum.col_pending.key()).equals(affair.getState())) {
                    String nodeDesc = wapi.getBPMActivityDesc(template.getWorkflowId(), String.valueOf(affair.getActivityId()));
                    AppContext.putRequestContext("nodeDesc", nodeDesc);
                }
                if (ColUtil.isForm(template.getBodyType())) {
                    AppContext.putRequestContext("signetProtectInput", template.getSignetProtect());
                }
            }
        }
        String DR = affair.getRelationDataId() == null ? "" : String.valueOf(affair.getRelationDataId());
        AppContext.putRequestContext("DR", DR);
        String rightId = "";
        boolean isForm = ColUtil.isForm(summary.getBodyType());

        //CAP4判断
        boolean isCAP4 = false;

        if (isForm) {

            isCAP4 = capFormManager.isCAP4Form(summary.getFormAppid());

            String scanCodeInput = "0";

            if (ColOpenFrom.docLib.name().equals(summaryVO.getOpenFrom())) {
                boolean isAcountPighole = Integer.valueOf(PigeonholeType.edoc_account.ordinal()).equals(summaryVO.getPigeonholeType());
                String docLibrightid = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, template, isAcountPighole, wfOperationId);

                summaryVO.setFormViewOperation(docLibrightid);
            }
            if (Strings.isBlank(summaryVO.getFormViewOperation()) || ColOpenFrom.subFlow.name().equals(summaryVO.getOpenFrom())) {
                rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, template, false, wfOperationId);
            } else {
                rightId = summaryVO.getFormViewOperation();
            }
            if (template != null) {
                if (null != template.getScanCodeInput() && template.getScanCodeInput()
                        && ColOpenFrom.listPending.name().equals(summaryVO.getOpenFrom())
                        && affair.getState() == StateEnum.col_pending.key()) {
                    scanCodeInput = "1";
                }

                //模板标题
                AppContext.putRequestContext("templateColSubject", template.getColSubject());
                //模板工作流ID
                AppContext.putRequestContext("templateWorkflowId", template.getWorkflowId());
            }
            AppContext.putRequestContext("scanCodeInput", scanCodeInput);
        } else if (null != summary.getFormAppid()) {
            isCAP4 = capFormManager.isCAP4Form(summary.getFormAppid());
        }

        rightId = rightId == null ? null : rightId.replaceAll("[|]", "_");
        summaryVO.setRightId(rightId);
        AppContext.putRequestContext("rightId", rightId);
        AppContext.putRequestContext("isCAP4", isCAP4);
        //××××××××××××××××××取正文操作权限结束×××××××××××××××××××××××××××××××××××××××××

        boolean colReadOnly = false;
        if (ColOpenFrom.docLib.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.favorite.name().equals(summaryVO.getOpenFrom())

                || ColOpenFrom.F8Reprot.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.formStatistical.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.task.name().equals(summaryVO.getOpenFrom())

                || ColOpenFrom.formQuery.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.formRelation.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.glwd.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.repealRecord.name().equals(summaryVO.getOpenFrom())
                || isHistoryFlag
                || (Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair.getState()) &&
                SubStateEnum.col_pending_specialBacked.getKey() != affair.getSubState())) {
            colReadOnly = true;
        }
        summaryVO.setReadOnly(colReadOnly);


        //点击更新affair状态为Read
        LOG.info("state跟踪日志：isHistoryFlag=" + !isHistoryFlag + ",affair.state=" + affair.getSubState() + ",affairId=" + affair.getId());
        if (!isHistoryFlag) {
            updateAffairStateWhenClick(affair);
        }


        if (Integer.valueOf(CollaborationEnum.flowState.finish.ordinal()).equals(summary.getState())
                || Integer.valueOf(CollaborationEnum.flowState.terminate.ordinal()).equals(summary.getState())) {
            summaryVO.setReadOnly(true);
        }

        summaryVO.setBodyType(summary.getBodyType());

        String configItem = ColUtil.getPolicyByAffair(affair).getId();
        //如果是调用的模板，节点权限取模板对应的节点权限。
        Long accountId = summary.getOrgAccountId();
        Long startMenberId = summary.getStartMemberId();
        //兼容320升级上来的数据accountId为空的情况
        if (null == accountId && startMenberId != null) {
            V3xOrgMember orgMember = orgManager.getMemberById(startMenberId);
            accountId = orgMember.getOrgAccountId();
        }
        int wfTraceType = 0;
        if (template != null) {
            AppContext.putRequestContext("templateWorkflowId", template.getWorkflowId());
            AppContext.putRequestContext("templateType", template.getType());
            accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), accountId, template.getOrgAccountId());

            //模板设置的追述数据
            if (null != template.getCanTrackWorkflow()) {
                wfTraceType = template.getCanTrackWorkflow();
            }
        }
        AppContext.putRequestContext("wfTraceType", wfTraceType);

        summaryVO.setFlowPermAccountId(accountId);
        String category = EnumNameEnum.col_flow_perm_policy.name();
        Permission permission = null;
        try {
            permission = permissionManager.getPermission(category, configItem, accountId);
            //用于判断当前节点权限是否存在，如果不存在则给知会的提示
            if (permission != null) {
                if (!configItem.equals(permission.getName()) && affair.getState() == StateEnum.col_pending.getKey()) {
                    AppContext.putRequestContext("noFindPermission", true);
                } else {
                    AppContext.putRequestContext("noFindPermission", false);
                }
            }
        } catch (Exception e) {
            LOG.error("获取节点权限报错category:" + category + " caonfigItem:" + configItem + " accountId:" + accountId, e);
        }
        List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
        if (!Strings.isEmpty(basicActionList)) {
            String isHasPraise = basicActionList.contains("Praise") ? "1" : "0";
            AppContext.putRequestContext("isHasPraise", isHasPraise);
        }
        List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
        List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);
        boolean isTemplete = false;
        if (template != null && template.isSystem()) {
            isTemplete = true;
        }
        AppContext.putRequestContext("isTemplete", isTemplete);
        // 控制节点权限的条件
        checkCanAction(affair, summary, basicActionList, commonActionList, advanceActionList, isTemplete);

        //控制不同意设置的
        NodePolicy nodePolicy = permission.getNodePolicy();
        CustomAction customAction = nodePolicy.getCustomAction();
        if (customAction != null) {
            String isOptional = customAction.getIsOptional();
            String optionalAction = customAction.getOptionalAction();
            String defaultAction = customAction.getDefaultAction();
            AppContext.putRequestContext("isOptional", isOptional);
            AppContext.putRequestContext("optionalAction", optionalAction);
            AppContext.putRequestContext("defaultAction", defaultAction);

        }

        // 指定回退再处理的流转方式  show1 重新流转 show2 提交回退者
        boolean show1 = true;
        boolean show2 = true;
        if (permission != null) {
            AppContext.putRequestContext("nodeattitude", permission.getNodePolicy().getAttitude());
            AppContext.putRequestContext("defaultAttitude", permission.getNodePolicy().getDefaultAttitude());
            Integer submitStyle = permission.getNodePolicy().getSubmitStyle();
            if (submitStyle != null) {
                switch (submitStyle) {
                    case 0:
                        show1 = true;
                        show2 = false;
                        break;
                    case 1:
                        show1 = false;
                        show2 = true;
                        break;
                }
            }
        }

        NodePolicyVO newColNodePolicy = getNewColNodePolicy(user.getLoginAccount());
        boolean _canUploadAttachment = basicActionList.contains("UploadAttachment");
        boolean _canUploadRelDoc = basicActionList.contains("UploadRelDoc");
        if ("listSent".equals(summaryVO.getOpenFrom())) {
            //这个分支么看懂有什么用
            _canUploadAttachment = true;
            _canUploadRelDoc = true;
        }

        Boolean isNewColNode = ((StateEnum.col_sent.getKey() == affair.getState()
                || StateEnum.col_waitSend.getKey() == affair.getState())
                && !ColOpenFrom.supervise.name().equals(summaryVO.getOpenFrom()));
        if (isNewColNode) {
            _canUploadAttachment = newColNodePolicy.isUploadAttachment();
            _canUploadRelDoc = newColNodePolicy.isUploadRelDoc();
        }

        //是否是督办打开， 关联BUG OA-85262, OA-90691
        boolean isSuervise = ((StateEnum.col_sent.getKey() == affair.getState()
                || StateEnum.col_waitSend.getKey() == affair.getState())
                && ColOpenFrom.supervise.name().equals(summaryVO.getOpenFrom()));
        if (isSuervise) {
            _canUploadAttachment = true;
            _canUploadRelDoc = true;
        }

        AppContext.putRequestContext("show1", show1);
        AppContext.putRequestContext("show2", show2);
        AppContext.putRequestContext("newColNodePolicy", newColNodePolicy); //新建节点权限
        AppContext.putRequestContext("isNewColNode", isNewColNode); //是否受新建节点权限控制
        AppContext.putRequestContext("nodePerm_baseActionList", JSONUtil.toJSONString(basicActionList));
        AppContext.putRequestContext("nodePerm_commonActionList", JSONUtil.toJSONString(commonActionList));
        AppContext.putRequestContext("nodePerm_advanceActionList", JSONUtil.toJSONString(advanceActionList));
        AppContext.putRequestContext("commonActionList", commonActionList);
        AppContext.putRequestContext("basicActionList", basicActionList);
        AppContext.putRequestContext("advanceActionList", advanceActionList);
        AppContext.putRequestContext("permissionName", permissionManager.getPermissionName(permission));
        AppContext.putRequestContext("nodePolicy", configItem);//节点权限
        AppContext.putRequestContext("canModifyWorkFlow", summary.getCanModify() == null ? false : summary.getCanModify().booleanValue());

        AppContext.putSessionContext("canUploadAttachment", _canUploadAttachment);//是否可以插入附件
        AppContext.putSessionContext("canUploadRelDoc", _canUploadRelDoc);//是否可以插入附件
        AppContext.putSessionContext("canEdit", commonActionList.contains("Edit") || advanceActionList.contains("Edit"));//是否可以修改正文
        //是否可以归档(发起人只受节点权限控制是否可以归档，其他不限制。)

        boolean isContainOperation = (Boolean) basicActionList.contains("Archive");
        //已发、待发事项都是受新建的归档限制
        if (StateEnum.col_sent.getKey() == affair.getState()
                || StateEnum.col_waitSend.getKey() == affair.getState()) {
            isContainOperation = (Boolean) basicActionList.contains("Pigeonhole");
        }
        boolean isSenderOrCanArchive = Boolean.TRUE.equals(summary.getCanArchive());
        boolean hasResourceCode = (AppContext.getCurrentUser().hasResourceCode("F04_docIndex") || AppContext.getCurrentUser().hasResourceCode("F04_myDocLibIndex") || AppContext.getCurrentUser().hasResourceCode("F04_accDocLibIndex")
                || AppContext.getCurrentUser().hasResourceCode("F04_proDocLibIndex") || AppContext.getCurrentUser().hasResourceCode("F04_eDocLibIndex") || AppContext.getCurrentUser().hasResourceCode("F04_docLibsConfig"));
        hasResourceCode = hasResourceCode && AppContext.hasPlugin("doc");
        String systemVer = AppContext.getSystemProperty("system.ProductId");

        if (AffairUtil.isFormReadonly(affair)) {
            AppContext.putRequestContext("deeReadOnly", 1);
        }

        //是否可以收藏，A6\A8各个产品线的区隔判断，doc.collectFlag设置的位置 :productFeature.xml
        String propertyFav = SystemProperties.getInstance().getProperty("doc.collectFlag");
        boolean propertyFavFlag = "true".equals(propertyFav);
        boolean canFavorite = isContainOperation && isSenderOrCanArchive && hasResourceCode && propertyFavFlag
                && !isFormQuery && !isFormStatistical;
        AppContext.putSessionContext("canFavorite", canFavorite);
        boolean canAttFavorite = hasResourceCode && propertyFavFlag;
        AppContext.putSessionContext("canAttFavorite", canAttFavorite);
        //是否能归档和A6和是否能收藏的权限无关
        boolean canArchive = isContainOperation && isSenderOrCanArchive && hasResourceCode;
        AppContext.putSessionContext("canArchive", canArchive);

        if (!canFavorite) {
            LOG.info("協同ID：<" + summary.getId() + ">,没有收藏权限,isContainOperation:" + isContainOperation + " isSenderOrCanArchive:" + isSenderOrCanArchive + " hasResourceCode：" + hasResourceCode + ",propertyFav:" + propertyFav);
        }

        String permissionName = permission == null ? "" : permission.getName();
        AppContext.putRequestContext("isAudit", "formaudit".equals(permissionName));
        AppContext.putRequestContext("isIssus", ("newsaudit".equals(permissionName) || "bulletionaudit".equals(permissionName)));
        AppContext.putRequestContext("isVouch", "vouch".equals(permissionName));
        AppContext.putRequestContext("senderCanFavorite", (hasResourceCode && propertyFavFlag));
        setPara4Wf(summary, affair);
        List<Attachment> allAttachments = new ArrayList<Attachment>();
        List<Attachment> temp = getAttachmentsById(affair.getObjectId());
        if (temp != null) {
            allAttachments.addAll(temp);
        }

        //取打印权限
        String nodePermissionPolicy = ColUtil.getPolicyByAffair(affair).getId();
        //根据affairId得到权限的处理ID
        String lenPotents = summaryVO.getLenPotent();
        //默认office正文可以打印和保存
        boolean officecanPrint = true;
        boolean officecanSaveLocal = true;

        Map<String, Object> map = getSaveToLocalOrPrintPolicy(summary, nodePermissionPolicy, lenPotents, affair, summaryVO.getOpenFrom());
        if (map != null && map.size() != 0) {
            officecanPrint = (Boolean) map.get("officecanPrint");
            officecanSaveLocal = (Boolean) map.get("officecanSaveLocal");
        }
        //督办,督办列表页面因为要显示修改流程按钮，所以需要预先记载督办信息，其他协同展现页面延迟加载，即点督办设置的时候再加载。
        if (ColOpenFrom.supervise.name().equals(summaryVO.getOpenFrom())
                || ColOpenFrom.listDone.name().equals(summaryVO.getOpenFrom())) {
            SuperviseSetVO ssvo = superviseManager.parseProcessSupervise(summary.getId(), summary.getTempleteId(), summary.getStartMemberId(), SuperviseEnum.EntityType.summary);
            if (Strings.isNotBlank(ssvo.getSupervisorIds()) && ssvo.getSupervisorIds().indexOf(user.getId().toString()) != -1) {
                summaryVO.setIsCurrentUserSupervisor(true);
            }
        }

        //设置当前待办所属人的姓名
        V3xOrgMember orgMember = orgManager.getMemberById(affair.getMemberId());
        if (orgMember != null) {
            summaryVO.setAffairMemberName(orgMember.getName());
        }


        summaryVO.setActivityId(affair.getActivityId());
        summaryVO.setWorkitemId(affair.getSubObjectId());
        summaryVO.setOfficecanPrint(officecanPrint);
        summaryVO.setOfficecanSaveLocal(officecanSaveLocal);
        summaryVO.setForwardMemberNames(getForwardMemberNames(summary.getForwardMember()));
        summaryVO.setSummary(summary);
        summaryVO.setIsTrack(AffairUtil.isTrack(affair));
        summaryVO.setAffair(affair);
        summaryVO.setAttachments(allAttachments);
        summaryVO.setShowButton(false);
        summaryVO.setCreateDate(summary.getCreateDate());
        if (summary.getCaseId() != null) {
            summaryVO.setCaseId(summary.getCaseId().toString());
        }
        //处理页面 的标题不显示代理信息
        String subject = ColUtil.showSubjectOfSummary(summary, false, -1, null).replaceAll("\r\n", "").replaceAll("\n", "");
        summaryVO.setSubject(subject);
        Integer newflowType = summary.getNewflowType();
        if (newflowType != null && newflowType.intValue() == ColConstant.NewflowType.main.ordinal()) {//0
            summaryVO.setIsNewflow(false);
        } else if (newflowType != null && newflowType.intValue() == ColConstant.NewflowType.child.ordinal()) {//1
            summaryVO.setIsNewflow(true);
        } else {//-1
            summaryVO.setIsNewflow(false);
        }
        summaryVO.setProcessId(summary.getProcessId());
        //设置流程是否完成
        summaryVO.setFlowFinished(summary.getFinishDate() == null ? false : true);
        V3xOrgMember member = orgManager.getMemberById(summary.getStartMemberId());
        if (member != null) {
            summaryVO.setStartMemberName(Functions.showMemberName(member));
            summaryVO.setStartMemberDepartmentId(member.getOrgDepartmentId());
            if (member.getIsInternal()) {
                summaryVO.setStartMemberPostId(member.getOrgPostId());
                String postName = "";
                if (member.getOrgPostId() != null) {
                    V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
                    if (post != null) {
                        postName = post.getName();
                    }
                }
                summaryVO.setStartMemberPostName(postName);
            } else {//外部人员
                summaryVO.setStartMemberPostId(null);
                summaryVO.setStartMemberPostName(OrgHelper.getExtMemberPriPost(member));
            }
        }
        //得到所有'意见不能为空'的节点权限名称 集合,用于协同待办列表，如果该Affair的节点权限意见不能为空，则不能直接删除和归档.
        if (permission != null) {
            Integer opinion = permission.getNodePolicy().getOpinionPolicy();
            if (opinion != null && opinion.intValue() == 1) {
                summaryVO.setCanDeleteORarchive(true);
            }
            summaryVO.setCancelOpinionPolicy(permission.getNodePolicy().getCancelOpinionPolicy());
            summaryVO.setDisAgreeOpinionPolicy(permission.getNodePolicy().getDisAgreeOpinionPolicy());
        }

        //个人设置-处理界面是否默认自动展开
        String ctp5 = user.getCustomize(CustomizeConstants.HANDLE_EXPAND);
        AppContext.putRequestContext("isDealPageShow", ctp5 == null ? "" : ctp5);

        String _trackValue = user.getCustomize(CustomizeConstants.TRACK_PROCESS);
        AppContext.putRequestContext("customSetTrack", _trackValue);

        String trackIds = "";
        String trackOnlyIds = "";
        String trackNames = "";
        AppContext.putRequestContext("trackType", affair.getTrack());
        if (null != affair.getTrack() && affair.getTrack() == 2) {
            List<CtpTrackMember> trackInfo = trackManager.getTrackMembers(affair.getId());
            if (Strings.isNotEmpty(trackInfo)) {
                for (CtpTrackMember ctpTrackMember : trackInfo) {
                    Long trackMemeberId = ctpTrackMember.getTrackMemberId();
                    V3xOrgMember trackMember = orgManager.getMemberById(trackMemeberId);

                    trackIds += "Member|" + trackMemeberId + ",";
                    trackOnlyIds += trackMember.getId() + ",";
                    trackNames += Functions.showMemberName(trackMember) + ",";//指定跟踪人显示用人名
                }
                if (trackIds.length() > 0) {

                    AppContext.putRequestContext("trackIds", trackIds.substring(0, trackIds.length() - 1));
                    AppContext.putRequestContext("trackOnlyIds", trackOnlyIds.substring(0, trackOnlyIds.length() - 1));
                    AppContext.putRequestContext("trackNames", trackNames.substring(0, trackNames.length() - 1));
                }
            }
        }

        //正文区域附件
        String arrListJSON = "";
        List<Attachment> mainAtt = new ArrayList<Attachment>();
        //显示保存了草稿的意见、附件、意见隐藏不包括人
        comtentDraftAttAndDis(allAttachments, affair);

        if (Strings.isNotEmpty(allAttachments)) {
            for (Attachment att : allAttachments) {
                if (affair.getObjectId().equals(att.getSubReference())
                        && (Integer.valueOf(com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()).equals(att.getType())
                        || Integer.valueOf(com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.DOCUMENT.ordinal()).equals(att.getType()))) {
                    mainAtt.add(att);
                }
            }
        }

        ColSelfUtil.sortAttachmentList(mainAtt);


        arrListJSON = attachmentManager.getAttListJSON(mainAtt);

        //收藏
       /* String collectFlag = SystemProperties.getInstance().getProperty("doc.collectFlag");
        if(AppContext.hasPlugin(PortletCategory.doc.name()) && "true".equals(collectFlag)){
        	boolean isCollect = false;
        	if(AppContext.hasPlugin("doc")){
        		List<Map<String,Long>> collectMap = docApi.findFavorites(user.getId(), CommonTools.newArrayList(affair.getId()));
            	if(Strings.isNotEmpty(collectMap)){
            		isCollect = true;
            	}
        	}
        	AppContext.putRequestContext("isCollect", isCollect);
        }*/
        /**是否包含流程回退/撤销产生的追溯数据开始**/
        /*List<WorkflowTracePO> dataByParams = traceWorkflowManager.getDataByParams(affair.getObjectId(), affair.getActivityId(), affair.getMemberId());
        if(null != dataByParams && dataByParams.size() > 0){
        	if(!(Integer.valueOf(WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey()).equals(dataByParams.get(0).getTrackType())
        			&& null != affair && affair.getState().equals(Integer.valueOf(StateEnum.col_pending.getKey())))){
        		AppContext.putRequestContext("hasWorkflowDataaButton", true);
        	}
        }*/
        /**是否包含流程回退/撤销产生的追溯数据结束**/
        //获取当前系统版本
        String productId = AppContext.getSystemProperty("system.ProductId");
        AppContext.putRequestContext("productId", productId);
        AppContext.putRequestContext("attListJSON", arrListJSON);
        AppContext.putRequestContext("collEnumKey", ApplicationCategoryEnum.collaboration.getKey());


        AppContext.putRequestContext("isSubFlow", Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(summary.getNewflowType()));


        // 解析接收人, 自由协同才进行展现
        if (summary.getTempleteId() == null && summary.getProcessId() != null) {
            List<String[]> nodeInfos = new ArrayList<String[]>();
            if (Strings.isNotEmpty(summary.getProcessNodesInfo())) {
                nodeInfos = parseNodeInfos(summary.getProcessNodesInfo());
            } else {
                nodeInfos = getNodeMemberInfos(summary.getProcessId());
            }
            AppContext.putRequestContext("nodeInfos", nodeInfos);
        }

        //同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), summaryVO.getAffair().getId());

        if (affair.getState().intValue() == StateEnum.col_pending.key() &&
                (affair.getSubState().intValue() == SubStateEnum.col_pending_Back.key() ||
                        affair.getSubState().intValue() == SubStateEnum.col_pending_specialBacked.key())) {
            AppContext.putRequestContext("needPromptedBeBacked", "1");
        }

        return summaryVO;
    }

    private void isInSpecialBack(boolean isHistoryFlag, CtpAffair affair, ColSummary summary) throws BPMException {
        if (StateEnum.col_pending.getKey() == affair.getState()) {
            if (SubStateEnum.col_pending_specialBack.getKey() != affair.getSubState()
                    && SubStateEnum.col_pending_specialBacked.getKey() != affair.getSubState()
                    && SubStateEnum.col_pending_specialBackCenter.getKey() != affair.getSubState()) {

                if (summary.getCaseId() != null) {
                    boolean inInSpecialBack = false;
                    if (Integer.valueOf(CollaborationEnum.SubState.OldData.ordinal()).equals(summary.getSubState())) {
                        inInSpecialBack = !wapi.isInSpecialStepBackStatus(summary.getCaseId(), isHistoryFlag);
                    } else {
                        inInSpecialBack = Integer.valueOf(CollaborationEnum.SubState.SpecialBack.ordinal()).equals(summary.getSubState());
                    }
                    if (inInSpecialBack) {// inInSpecialSB为false 就是 处于指定回退状态
                        AppContext.putRequestContext("inInSpecialSB", true);
                    }
                }
            }
        }
    }


    private List<String[]> parseNodeInfos(String processNodesInfo) {
        List<String[]> nodeInfos = new ArrayList<String[]>();

        if (Strings.isNotBlank(processNodesInfo)) {
            List<Map> nodeList = (List<Map>) JSONUtil.parseJSONString(processNodesInfo);
            for (int a = 0; a < nodeList.size(); a++) {
                Map map = nodeList.get(a);
                String[] info = new String[5];

                String nodeName = (String) map.get("nodeName");
                String accountId = (String) map.get("actorPartyAccountId");

                if (Strings.isNotBlank(accountId) && Strings.isDigits(accountId)) {
                    try {
                        Long _wfAccount = Long.valueOf(accountId);
                        if (!_wfAccount.equals(AppContext.currentAccountId())) {
                            V3xOrgAccount account = orgManager.getAccountById(_wfAccount);
                            if (account != null) {
                                nodeName += "(" + account.getShortName() + ")";
                            }
                        }
                    } catch (Throwable e) {
                        LOG.error("", e);
                    }
                }
                info[0] = nodeName; //名字
                info[1] = (String) map.get("nodeId"); //nodeId
                info[2] = (String) map.get("actorPartyId");  //partyId
                info[3] = (String) map.get("actorTypeId");    //partyTypeid
                info[4] = "true";
                nodeInfos.add(info);

            }
        }
        return nodeInfos;
    }

    private List<String[]> getNodeMemberInfos(String processId/*, Map<String, List<Object[]>> node2AffairMap*/) throws BPMException {
        BPMProcess process = wapi.getBPMProcessForM1(processId);
        if (process == null) {
            process = wapi.getBPMProcessHis(processId);
        }

        List<String[]> nodeInfos = new ArrayList<String[]>();// [NodeName,NodeId,PartType,PartId]

        if (process != null) {
            List<BPMAbstractNode> processes = wapi.getHumenNodeInOrderFromProcess(process);
            List<BPMAbstractNode> nodes = new ArrayList<BPMAbstractNode>(processes.size());
            for (Object _node : processes) {
                BPMAbstractNode node = (BPMAbstractNode) _node;
                if (!"start".equals(node.getId()) && !"end".equals(node.getId()) && !"join".equals(node.getName())
                        && !"split".equals(node.getName())) {
                    if (Strings.isNotEmpty(node.getActorList())) {
                        nodes.add(node);
                    }
                }
            }

            //解析
            List<Map<String, String>> nodeInfoMaps = new ArrayList<Map<String, String>>();// [NodeName,NodeId,PartType,PartId]
            for (BPMAbstractNode node : nodes) {

                BPMActor actor = (BPMActor) node.getActorList().get(0);
                String[] info = new String[5];

                String nodeId = node.getId();
                String type = actor.getType().id;

                info[1] = nodeId;
                info[2] = actor.getParty().getId();
                info[3] = type;

                String nodeName = node.getBPMAbstractNodeName();
                String showNodeName = nodeName;
                String accountId = actor.getParty().getAccountId();
                if (Strings.isNotBlank(accountId) && Strings.isDigits(accountId)) {
                    try {
                        Long _wfAccount = Long.valueOf(accountId);
                        if (!_wfAccount.equals(AppContext.currentAccountId())) {
                            LOG.info("当前待办人显示单位简称processId:" + processId + ",userId:" + AppContext.currentUserId() + ",accountId:" + AppContext.currentAccountId() + ",_wfAccount:" + _wfAccount);
                            V3xOrgAccount account = orgManager.getAccountById(_wfAccount);
                            if (account != null) {
                                showNodeName = nodeName + "(" + account.getShortName() + ")";
                            }
                        }
                    } catch (Throwable e) {
                        LOG.error("", e);
                    }
                }

                info[0] = showNodeName;

                String canClick = "true";
                info[4] = canClick;

                nodeInfos.add(info);


                Map<String, String> map = new HashMap<String, String>();
                map.put("nodeId", nodeId);
                map.put("nodeName", nodeName);
                map.put("actorTypeId", type);
                map.put("actorPartyId", actor.getParty().getId());
                map.put("actorPartyAccountId", accountId);
                nodeInfoMaps.add(map);
            }

            //存一次数据库，避免下次再解析
            if (Strings.isNotEmpty(nodeInfoMaps)) {
                String nodesInfo = JSONUtil.toJSONString(nodeInfoMaps);
                colDao.updateColSummaryProcessNodeInfos(processId, nodesInfo);
            }
        }
        return nodeInfos;
    }

    private void checkCanOpenAcl(ColSummaryVO summaryVO, CtpAffair affair, ColSummary summary, boolean isHistoryFlag) throws BusinessException {
        HttpServletRequest request = AppContext.getRawRequest();
        String isCollCube = request.getParameter("isCollCube");
        String isColl360 = request.getParameter("isColl360");
        AppContext.putRequestContext("summaryId", affair.getObjectId());
        AppContext.putRequestContext("isCollCube", isCollCube);
        AppContext.putRequestContext("isColl360", isColl360);

        if (Strings.isNotBlank(isCollCube) && "1".equals(isCollCube)) {//协同立方过来查看的判断
            if (StateEnum.col_done.getKey() != affair.getState().intValue() || StateEnum.col_sent.getKey() != affair.getState().intValue()) {
                summaryVO.setErrorMsg(ResourceUtil.getString("collaboration.alert.chuantou.label"));
                return;
            }
        }
        if (Strings.isNotBlank(isColl360) && "1".equals(isColl360)) {//协同360过来查看的判断 ps:协同360记录了两种关系一是发送者二是处理者
            if ((StateEnum.col_sent.getKey() == affair.getState().intValue() && affair.isDelete()) ||
                    (StateEnum.col_done.getKey() != affair.getState().intValue() && affair.isDelete())
                    || (StateEnum.col_waitSend.getKey() == affair.getState().intValue())
                    || (StateEnum.col_pending.getKey() == affair.getState().intValue())) {

                summaryVO.setErrorMsg(ResourceUtil.getString("collaboration.alert.chuantou.label"));
                return;

            }
        }

        if (ColOpenFrom.stepBackRecord.name().equals(summaryVO.getOpenFrom())) {
            if (null != summary.getCaseId() && null != affair.getActivityId()) {
                boolean nodeDelete = false;

                if (isHistoryFlag) {
                    nodeDelete = wapi.isNodeDelete(summary.getCaseId(), affair.getActivityId().toString(), isHistoryFlag);
                } else {
                    nodeDelete = wapi.isNodeDelete(summary.getCaseId(), affair.getActivityId().toString());
                }
                boolean effectiveAffair = isEffectiveAffair(summary.getId(), affair.getActivityId(), AppContext.getCurrentUser().getId());
                if (nodeDelete || !effectiveAffair) {
                    summaryVO.setErrorMsg(ResourceUtil.getString("collaboration.alert.notExistInWF"));
                    return;
                }
            }
        }
    }


    private boolean isEffectiveAffair(Long objectId, Long activityId, Long memId) throws BusinessException {
        boolean flag = true;
        List<CtpAffair> affairsByActivityId = affairManager.getAffairsByObjectIdAndNodeId(objectId, activityId);
        List<Long> _memId = new ArrayList<Long>();
        CtpAffair affair;
        for (int a = 0; a < affairsByActivityId.size(); a++) {
            affair = affairsByActivityId.get(a);
            if (ColUtil.isAfffairValid(affair)) {
                _memId.add(affair.getMemberId());
            }
        }
        if (!Strings.isEmpty(_memId) && !_memId.contains(memId)) {
            flag = false;
        }
        return flag;
    }

    /**
     * 显示保存了草稿的意见、附件、意见隐藏不包括人
     *
     * @param attachments
     * @param affair
     */
    private void comtentDraftAttAndDis(List<Attachment> attachments, CtpAffair affair) {
        //处理区域附件
        String handleAttach = "";
        Long commentId = null;
        List<Attachment> dealAtt = new ArrayList<Attachment>();

        //获取草稿
        Comment comentDraft = getDraftOpinion(affair.getId());
        AppContext.putRequestContext("commentDraft", comentDraft);

        //获取意见隐藏的ids
        String displayIds = "";
        //获取意见隐藏的Names
        String displayNames = "";
        try {
            if (affair.getState().equals(StateEnum.col_pending.key())) {
                if (comentDraft != null) {
                    commentId = comentDraft.getId();
                    for (Attachment att : attachments) {
                        if (commentId.equals(att.getSubReference())) {
                            dealAtt.add(att);
                        }
                    }
                    //意见隐藏
                    if (comentDraft.getShowToId() != null) {
                        String[] ids = comentDraft.getShowToId().split(",");
                        if (!"".equals(ids[0])) {
                            for (int i = 0; i < ids.length; i++) {
                                V3xOrgMember mem = orgManager.getMemberById(Long.valueOf(ids[i]));
                                if (i == ids.length - 1) {
                                    displayIds = displayIds + "Member|" + ids[i];
                                    displayNames = displayNames + mem.getName();
                                } else {
                                    displayIds = displayIds + "Member|" + ids[i] + ",";
                                    displayNames = displayNames + mem.getName() + ",";
                                }
                            }
                        }
                    }
                }
            } else {
                //设置一个标识，用于页面的附件区域
                commentId = 20L;
            }
        } catch (Exception e) {
            LOG.error("获取草稿中的附件、意见隐藏人报错！", e);
        }

        AppContext.putRequestContext("displayIds", displayIds);
        AppContext.putRequestContext("displayNames", displayNames);
        handleAttach = attachmentManager.getAttListJSON(dealAtt);
        AppContext.putRequestContext("commentId", commentId);
        AppContext.putRequestContext("handleAttachJSON", handleAttach);
    }

    @Override
    public ColSummary getMainSummary4FormQueryAndStatic(String openFrom, String summaryId) throws BPMException, BusinessException {
        ColSummary summary = null;
        if (ColOpenFrom.formQuery.name().equals(openFrom) || ColOpenFrom.formStatistical.name().equals(openFrom)) {
            if (Strings.isNotBlank(summaryId)) {
                summary = getColSummaryById(Long.valueOf(summaryId));
                if (summary == null) {
                    LOG.info("协同展现，表单穿透，统计，summary为空，入参summaryId：" + summaryId);
                }

                //表单查询和统计， 如果选择的是子流程，只打开父流程
                if (summary != null && Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(summary.getNewflowType())) {
                    String processId = summary.getProcessId();
                    if (Strings.isNotBlank(processId)) {
                        Long parentProcessId = wapi.getMainProcessIdBySubProcessId(Long.valueOf(processId));
                        if (null != parentProcessId) {
                            ColSummary colSummaryById = getColSummaryByProcessId(parentProcessId);
                            if (null != colSummaryById) {
                                summary = colSummaryById;
                            }
                        }
                    }
                }
            }
        }
        return summary;
    }

    /**
     * 根据相关条件判断当前节点是否不能包含以下节点
     *
     * @param summary
     * @param basicActionList
     * @param commonActionList
     * @param advanceActionList
     */
    private void checkCanAction(CtpAffair affair, ColSummary summary, List<String> basicActionList, List<String> commonActionList, List<String> advanceActionList, boolean isTemplete) {
        //判断是否有日程事件的菜单，如果没有的话，则不能有转事件
        if (!ColUtil.checkByReourceCode("F02_eventlist") || !AppContext.hasPlugin("calendar")) {
            commonActionList.remove("Transform");
            advanceActionList.remove("Transform");
        }
        //没有文档中心，就不显示归档
        if (!AppContext.hasPlugin("doc")) {
            basicActionList.remove("Pigeonhole");
        }

        //没有新建协同权限，就不能转发
        if (!ColUtil.checkByReourceCode("F01_newColl")) {
            advanceActionList.remove("Forward");
            commonActionList.remove("Forward");
        }

        //修改正文和修改附件受节点权限和新建权限控制
        if (summary.getCanEdit() != null && !summary.getCanEdit()) {
            if (advanceActionList != null) {
                advanceActionList.remove("Edit");
            }
            if (commonActionList != null) {
                commonActionList.remove("Edit");
            }
        }

        if (summary.getCanEditAttachment() != null && !summary.getCanEditAttachment()) {
            if (advanceActionList != null) {
                advanceActionList.remove("allowUpdateAttachment");
            }
            if (commonActionList != null) {
                commonActionList.remove("allowUpdateAttachment");
            }
        }

        if (summary.getCanForward() != null && !summary.getCanForward()) {
            if (advanceActionList != null) {
                advanceActionList.remove("Forward");
            }
            if (commonActionList != null) {
                commonActionList.remove("Forward");
            }
        }

        if (summary.getCanArchive() != null && !summary.getCanArchive() && basicActionList != null) {
            basicActionList.remove("Archive");
        }
        //不允许改变流程
        if (isTemplete && summary.getCanModify() != null && !summary.getCanModify()) {
            if (advanceActionList != null) {
                advanceActionList.remove("JointSign");
                advanceActionList.remove("RemoveNode");
                advanceActionList.remove("Infom");
                advanceActionList.remove("AddNode");
            }
            if (commonActionList != null) {
                commonActionList.remove("AddNode");
                commonActionList.remove("JointSign");
                commonActionList.remove("RemoveNode");
                commonActionList.remove("Infom");
            }
        }
        //转发的表单不能编辑，不管处理节点是不是发起人
        if (Strings.isNotBlank(summary.getForwardMember()) && summary.getCanEdit() != null && !summary.getCanEdit()) {
            if (advanceActionList.contains("Edit")) {
                advanceActionList.remove("Edit");
            }
            if (commonActionList.contains("Edit")) {
                commonActionList.remove("Edit");
            }
        }

        boolean isFinished = ColUtil.isColSummaryFinished(summary);
        if (isFinished) {
            basicActionList.remove("Track");
        }

        //V-Join屏蔽操作
        User user = AppContext.getCurrentUser();
        if (!user.isV5Member()) {
            commonActionList.remove("SuperviseSet");
            basicActionList.remove("CommonPhrase");
        }
    }

    /**
     * @param summary
     * @param affair
     * @throws BusinessException
     */
    private void setPara4Wf(ColSummary summary, CtpAffair affair) throws BusinessException {
        HttpServletRequest request = (HttpServletRequest) AppContext
                .getThreadContext(GlobalNames.THREAD_CONTEXT_REQUEST_KEY);
        ContentViewRet context = new ContentViewRet();
        context.setModuleId(summary.getId());
        context.setModuleType(ModuleType.collaboration.getKey());
        context.setAffairId(affair.getId());
        context.setWfActivityId(affair.getActivityId());
        if (Strings.isNotBlank(summary.getProcessId())) {
            context.setWfCaseId(summary.getCaseId());
            context.setWfItemId(affair.getSubObjectId());
            context.setWfProcessId(summary.getProcessId());
            AppContext.putRequestContext("scene", 3);//查运行中
        } else if (null != summary.getTempleteId() && Strings.isNotBlank(String.valueOf(summary.getTempleteId()))) {//待发列表模板查看流程图
            CtpTemplate cp = templateManager.getCtpTemplate(summary.getTempleteId());
            if (null != cp && !"text".equals(cp.getType())) {
                context.setWfProcessId(cp.getWorkflowId().toString());
                AppContext.putRequestContext("scene", 2);//查模板的
            }
        }
        context.setContentSenderId(summary.getStartMemberId());
        context.setSubState(affair.getSubState());
        ContentConfig contentCfg = ContentConfig.getConfig(ModuleType.collaboration);
        request.setAttribute("contentCfg", contentCfg);
        request.setAttribute("contentContext", context);
    }

    /**
     * 调用模板时，设置流程的相关信息
     *
     * @param template 模板对象
     * @throws BPMException
     * @throws BusinessException
     */
    private NewCollTranVO setWorkFlowInfo(CtpTemplate template, NewCollTranVO vobj) throws BPMException, BusinessException {
        //回填工作流需要设置的东西
        ContentViewRet contextwf = (ContentViewRet) AppContext.getRequestContext("contentContext");
        CtpTemplate ct = templateManager.getCtpTemplate(template.getId());
        Long workflowId = ct.getWorkflowId();
        if (contextwf != null && null != workflowId) {
            if ((workflowId == -1l || !ct.isSystem())) {//流程为-1 或者是个人模板的
                if (null != ct.getFormParentid() && null != templateManager.getCtpTemplate(ct.getFormParentid())) {
                    ct = templateManager.getCtpTemplate(template.getFormParentid());
                }
            }

            contextwf.setWfProcessId(ct.getWorkflowId().toString());

            if ((!template.isSystem() && null == ct) || (!template.isSystem() && null != ct && !ct.isSystem())) {//本身是个人的 &&
                contextwf.setWfProcessId(null);
            }
            EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
            Map<String, CtpEnumBean> ems = em.getEnumsMap(ApplicationCategoryEnum.collaboration);
            CtpEnumBean nodePermissionPolicy = ems.get(EnumNameEnum.col_flow_perm_policy.name());
            String workflowNodesInfo = wapi.getWorkflowNodesInfo(String.valueOf(ct.getWorkflowId()), ModuleType.collaboration.name(), nodePermissionPolicy);
            contextwf.setWorkflowNodesInfo(workflowNodesInfo);
            String xml = "";
            if (!ct.isSystem()) {//自由协同存个人模板的时候 调用发送报错
                xml = wapi.selectWrokFlowTemplateXml(ct.getWorkflowId().toString());
                vobj.setWfXMLInfo(Strings.escapeJavascript(xml));
            }
            AppContext.putRequestContext("contentContext", contextwf);
        }
        return vobj;
    }

    @Override
    public NewCollTranVO transferTemplate(NewCollTranVO vobj) throws BusinessException {
        String _templateId = vobj.getTempleteId();
        // 调用模板
        vobj.setFromTemplate(true);
        Long templateId = Long.valueOf(_templateId);

        CtpTemplate template = vobj.getTemplate();

        if (template != null) {
            AppContext.putRequestContext("curTemplateID", template.getId());

            Long templeteFormparnetId = template.getFormParentid();
            if (null != templeteFormparnetId && Strings.isNotBlank(String.valueOf(templeteFormparnetId))) {
                vobj.setTemformParentId(String.valueOf(templeteFormparnetId));
            }

            vobj.setFormtitle(template.getSubject());

            //基准时长
            Long standardDuration = template.getStandardDuration();
            vobj.setStandardDuration(ColUtil.getStandardDuration(standardDuration == null ? null : standardDuration.toString()));

            ColSummary summary = (ColSummary) XMLCoder.decoder(template.getSummary());

            //项目
            Long projectId = template.getProjectId();

            if (null != projectId && AppContext.hasPlugin("project")) {
                if (null != projectApi) {
                    ProjectBO ps = projectApi.getProject(projectId);
                    if (null != ps && ProjectBO.STATE_DELETE.intValue() != ps.getProjectState()) {
                        summary.setProjectId(projectId);
                    } else {
                        summary.setProjectId(null);
                        template.setProjectId(null);
                    }
                }
            }

            if (!TemplateEnum.Type.template.name().equals(template.getType())) {
                summary.setCanForward(true);
                summary.setCanArchive(true);
                summary.setCanDueReminder(true);
                summary.setCanEditAttachment(true);
                summary.setCanTrack(true);
                summary.setCanEdit(true);
            }
            if (TemplateEnum.Type.text.name().equals(template.getType())) {
                summary.setCanModify(true);
            }

            summary.setSubject(template.getSubject());


            if (TemplateEnum.Type.template.name().equals(template.getType())) {
                //附件
                String attListJSON = attachmentManager.getAttListJSON(templateId);
                vobj.setAttListJSON(attListJSON);
                vobj.setCloneOriginalAtts(true);

                //预归档
                if (summary.getArchiveId() != null && AppContext.hasPlugin("doc")) {
                    boolean docResourceExist = docApi.isDocResourceExisted(summary.getArchiveId());
                    if (docResourceExist) {
                        JSONObject jo;
                        try {
                            if (Strings.isNotBlank(summary.getAdvancePigeonhole())) {
                                jo = new JSONObject(summary.getAdvancePigeonhole());
                                //String archiveName = ColUtil.getAdvancePigeonholeName(summary.getArchiveId(),summary.getAdvancePigeonhole(),"template");
                                vobj.setAdvancePigeonhole(summary.getAdvancePigeonhole());
                                String archiveFolder = "";
                                String tempFolder = "";
                                if (jo.has("archiveFieldName")) {
                                    archiveFolder = jo.get("archiveFieldName").toString();
                                    tempFolder = ColUtil.getArchiveAllNameById(summary.getArchiveId()) + "\\{" + archiveFolder + "}";
                                } else {
                                    tempFolder = ColUtil.getArchiveAllNameById(summary.getArchiveId());
                                }
                                vobj.setArchiveName(tempFolder);
                                vobj.setArchiveAllName(tempFolder);
                            } else {
                                vobj.setArchiveId(summary.getArchiveId());
                                vobj.setArchiveName(ColUtil.getArchiveNameById(summary.getArchiveId()));
                                vobj.setArchiveAllName(ColUtil.getArchiveAllNameById(summary.getArchiveId()));
                            }
                        } catch (JSONException e) {
                            LOG.error("", e);
                        }
                    } else {
                        summary.setArchiveId(null);
                    }
                }

                //附件归档
                if (summary.getAttachmentArchiveId() != null && AppContext.hasPlugin("doc")) {//chenxd
                    boolean docResourceExist = docApi.isDocResourceExisted(summary.getAttachmentArchiveId());
                    if (docResourceExist) {
                        //vobj.setArchiveId(summary.getArchiveId());
                        vobj.setAttachmentArchiveId(summary.getAttachmentArchiveId());
                        summary.setCanArchive(true);
                    }
                }
                //发起人意见
                List<Comment> commentSenderList = commentManager.getCommentList(ModuleType.collaboration, templateId);//附言
                AppContext.putRequestContext("commentSenderList", commentSenderList);
            }

            List<CtpContentAll> contentAll = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, templateId, false);
            if (null != contentAll && !contentAll.isEmpty()) {
                CtpContentAll content = contentAll.get(0);
                if (content != null) {
                    summary.setFormAppid(content.getContentTemplateId());
                }
            }


            boolean isParentIdNeNull = false;
            if (templeteFormparnetId != null) {
                // 取个人表单内容的
                template = templateManager.getCtpTemplate(templeteFormparnetId);
                if (template == null) {
                    return null;
                }

                isParentIdNeNull = true;
            }

            if (Strings.isNotBlank(template.getColSubject())) {
                vobj.setCollSubjectNotEdit(true);
                vobj.setCollSubject(template.getColSubject());
            }

            if (template.isSystem()) {
                vobj.setSystemTemplate(true);
            } else {
                vobj.setSystemTemplate(false);
            }

            if ("FORM".equals(template.getBodyType())) {
                vobj.setForm(Boolean.TRUE);
            } else {
                vobj.setForm(Boolean.FALSE);
            }
            AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.collaboration, String.valueOf(templateId), AppContext.currentUserId());//OA-66280


            if (TemplateEnum.Type.template.name().equals(template.getType())) {//格式模板不存取督办信息 升级程序 流程模板也不保留
                superviseManager.parseTemplateSupervise4New(templateId, AppContext.currentUserId(), template.getOrgAccountId());
            } else if (TemplateEnum.Type.workflow.name().equals(template.getType()) || TemplateEnum.Type.text.name().equals(template.getType())) {
                template.setStandardDuration(null);
                vobj.setStandardDuration(null);//设置页面上取的基准时长
                summary.setDeadline(null);//流程期限
                summary.setAdvanceRemind(null);//提醒
            }
            //个人模板父模板IDs
            vobj.setTemplate(template);
            Date _date = new Date();
            String fsubject = getTemplateSubject(summary.getSubject(), _date, false);
            summary.setSubject(fsubject);
            vobj.setSubjectForCopy(fsubject);
            vobj.setSummary(summary);

            ColSummary tSummary = summary;
            if (isParentIdNeNull) {
                tSummary = (ColSummary) XMLCoder.decoder(template.getSummary());
                //用系统父模板中的属性覆盖当前个人模板中的属性
                if (!"text".equals(template.getType())) {
                    vobj.getSummary().setCanAnyMerge(tSummary.getCanAnyMerge());
                    vobj.getSummary().setCanMergeDeal(tSummary.getCanMergeDeal());
                }
            }


            String scanCodeInput = "0";
            if (template != null) {
                if (null != template.getScanCodeInput() && template.getScanCodeInput()) {
                    scanCodeInput = "1";
                }
            }
            AppContext.putRequestContext("scanCodeInput", scanCodeInput);

            vobj.setTempleteHasDeadline(tSummary.getDeadline() != null && tSummary.getDeadline() != 0);
            vobj.setTempleteHasRemind(tSummary.getAdvanceRemind() != null && (tSummary.getAdvanceRemind() != 0 && tSummary.getAdvanceRemind() != -1));
            vobj.setCanEditColPigeonhole(tSummary.getArchiveId() != null);
            vobj.setTemplateHasProcessTermType(tSummary.getProcessTermType() != null);
            vobj.setTemplateHasRemindInterval(tSummary.getRemindInterval() != null && tSummary.getRemindInterval() > 0);

            if (null == template.getFormParentid()) {
                vobj.setParentWrokFlowTemplete(template.isSystem() && "workflow".equals(template.getType()));
                vobj.setParentTextTemplete(template.isSystem() && "text".equals(template.getType()));
                vobj.setParentColTemplete(template.isSystem() && "template".equals(template.getType()));
            } else {
                vobj.setParentWrokFlowTemplete(isParentWrokFlowTemplete(template.getFormParentid()));
                vobj.setParentTextTemplete(isParentTextTemplete(template.getFormParentid()));
                vobj.setParentColTemplete(isParentColTemplete(template.getFormParentid()));
            }
            vobj.setFromSystemTemplete(template.isSystem());//最原始的是不是系统模板
        }
        return vobj;
    }

    private String getTemplateSubject(String subject, Object now, boolean needCut) {
        User curUser = AppContext.getCurrentUser();
        String title = subject;
        if (needCut) {
            title = title.substring(0, title.indexOf("("));
        }
        String formatDate = Datetimes.formatDatetimeWithoutSecond((Date) now);
        title = title + "(" + curUser.getName() + " " + formatDate + ")";
        return title;
    }

//    private String changeSubject(String subject,Object now){
//    	User curUser = AppContext.getCurrentUser();
//    	String title =  subject;
//    	title = title.substring(0,title.lastIndexOf("("));
//    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//    	String formatDate = format.format(now);
//    	title =  title +"("+curUser.getName()+" "+formatDate+")";
//    	return title;
//    }
//
//    private String getTemplateSubject(ColSummary summary){
//    	User curUser = AppContext.getCurrentUser();
//    	String title =  summary.getSubject();
//    	Date date = new Date();
//    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//    	String formatDate = format.format(date);
//    	title =  title +"("+curUser.getName()+" "+formatDate+")";
//    	return title;
//    }


    private void CopySuperviseFromSummary(NewCollTranVO vobj, Long summaryId) throws BusinessException {
        SuperviseSetVO ssvo = new SuperviseSetVO();
        CtpSuperviseDetail detail = superviseManager.getSupervise(summaryId);
        if (detail != null) {
            ssvo.setTitle(detail.getTitle());//主题
            Long terminalDate = detail.getTemplateDateTerminal();
            if (null != terminalDate) {
                Date superviseDate = Datetimes.addDate(new Date(), terminalDate.intValue());
                String date = Datetimes.format(superviseDate, Datetimes.datetimeWithoutSecondStyle);
                vobj.setSuperviseDate(date);
                //ssvo.setTemplateDateTerminal(date);
            } else if (detail.getAwakeDate() != null) {
                vobj.setSuperviseDate(Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
                ssvo.setAwakeDate(Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));//日期
            }
            List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
            Set<String> sIdSet = new HashSet<String>();
            for (CtpSupervisor supervisor : supervisors) {
                sIdSet.add(supervisor.getSupervisorId().toString());
            }
            if (!sIdSet.isEmpty()) {
                StringBuffer names = new StringBuffer();
                ;
                StringBuffer ids = new StringBuffer();
                String forshow = "";
                for (String s : sIdSet) {
                    V3xOrgMember mem = orgManager.getMemberById(Long.valueOf(s));
                    if (mem != null) {
                        if (ids.length() > 0) {
                            ids.append(",");
                            names.append(",");
                        }
                        ids.append(mem.getId());
                        names.append(mem.getName());
                        forshow += "Member|" + mem.getId() + ",";
                    }
                }
                if (forshow.length() > 0) {
                    vobj.setForShow(forshow.substring(0, forshow.length() - 1));
                }
                ColSummary byId = getSummaryById(summaryId);
                if (null != byId && null != byId.getTempleteId()) {
                    //vobj.setUnCancelledVisor(ids.toString());
                    Set<Long> _tempSur = superviseManager.parseTemplateSupervisorIds(byId.getTempleteId(), AppContext.getCurrentUser().getId());
                    StringBuffer sb = new StringBuffer();
                    if (null != _tempSur) {
                        for (Long lid : _tempSur) {
                            sb.append(lid + ",");
                        }
                        String sbs = sb.toString();
                        if (sbs.length() > 1) {
                            ssvo.setUnCancelledVisor(sbs.substring(0, sbs.length() - 1));
                        }
                    }
                }
                vobj.setColSupervisors(ids.toString());
                vobj.setColSupervisorNames(names.toString());
                ssvo.setSupervisorNames(names.toString());
            }
            vobj.setColSupervise(detail);
            //兼容下督办的错误数据
            if (Strings.isNotBlank(ssvo.getAwakeDate())
                    && Strings.isNotBlank(ssvo.getSupervisorNames())) {
                AppContext.putRequestContext("_SSVO", ssvo);
            } else {
                vobj.setSuperviseDate(null);
            }
        }
    }

    public boolean isParentTextTemplete(Long templeteId) throws BusinessException {
        CtpTemplate t = getParentSystemTemplete(templeteId);
        if (t == null)
            return false;
        if (TemplateEnum.Type.text.name().equals(t.getType()))
            return true;
        else
            return false;
    }

    public boolean isParentWrokFlowTemplete(Long templeteId) throws BusinessException {
        CtpTemplate t = getParentSystemTemplete(templeteId);
        if (t == null)
            return false;
        if (TemplateEnum.Type.workflow.name().equals(t.getType()))
            return true;
        else
            return false;
    }

    public boolean isParentColTemplete(Long templeteId) throws BusinessException {
        CtpTemplate t = getParentSystemTemplete(templeteId);
        if (t == null)
            return false;
        if (TemplateEnum.Type.template.name().equals(t.getType()))
            return true;
        else
            return false;
    }

    @Override
    public ColSummary getColSummaryByProcessId(Long processId) throws BusinessException {
        ColSummary s = colDao.getColSummaryByProcessId(processId);
        if (s == null && AppContext.hasPlugin("fk")) {
            ColDao colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
            s = colDaoFK.getColSummaryByProcessId(processId);
        }
        return s;
    }

    private List<ColSummary> getColSummarysByFormAppId(Long formAppId) throws BusinessException {
        return colDao.getColSummarysByFormAppId(formAppId);
    }

    public ColSummary getColSummaryByProcessIdHistory(Long processId) throws BusinessException {
        ColSummary s = null;
        ColDao colDaoFK = null;
        if (AppContext.hasPlugin("fk")) {
            colDaoFK = (ColDao) AppContext.getBean("colDaoFK");
        }
        if (colDaoFK != null) {
            s = colDaoFK.getColSummaryByProcessId(processId);
        }
        return s;
    }

    /**
     * 协同流程终止
     *
     * @param caseId
     * @throws BPMException
     */
    private Map<String, Object> colStepStop(Long workItemId, ColSummary summary, MessageCommentParam mc, CtpAffair affair, List<Long[]> pushMessageToMemberList) throws BPMException {

        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentWorkitemId(workItemId);
        Long formRecordId = summary.getFormRecordid();
        Long formAppId = summary.getFormAppid();

        context.setBussinessId(String.valueOf(summary.getId()));
        context.setAppObject(summary);
        context.setAppName("collaboration");
        context.setBusinessData("operationType", 8);
        context.setCurrentUserId(String.valueOf(AppContext.getCurrentUser().getId()));
        context.setMastrid(formRecordId == null ? null : String.valueOf(formRecordId));
        context.setFormData(formAppId == null ? null : String.valueOf(formAppId));
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_MESSAGE, mc);
        context.setAffairId(String.valueOf(affair.getId()));
        context.setBussinessId(String.valueOf(affair.getObjectId()));
        context.setBusinessData("pushMsgMemberList", pushMessageToMemberList);
        wapi.stopCase(context);

        return context.getBusinessData();
    }

    @Override
    public String transStepStop(Map<String, Object> tempMap) throws BusinessException {
        User user = AppContext.getCurrentUser();
        String _affairId = (String) tempMap.get("affairId");
        String repealComment = (String) tempMap.get("repealComment");
        Comment comment = (Comment) tempMap.get("comment");
        if (_affairId == null) {
            return null;
        }

        Long affairId = Long.parseLong(_affairId);
        String processId = null;
        try {
            CtpAffair affair = affairManager.get(affairId);
            if (affair.getState().intValue() != StateEnum.col_pending.key()) {
                String msg = ColUtil.getErrorMsgByAffair(affair);
                return msg;
            }
            ColSummary summary = getColSummaryById(affair.getObjectId());
            if (comment == null) {
                comment = new Comment();
                ParamUtil.getJsonDomainToBean("comment_deal", comment);
            }

            if (Strings.isNotBlank(repealComment)) {
                comment.setContent(repealComment);
            }

            //检查代理，避免不是处理人也能处理了。
            try {
                boolean canDeal = ColUtil.checkAgent(affair, summary, true);
                if (!canDeal) {
                    return null;
                }
            } catch (Exception e1) {
                LOG.error("", e1);
            }


            processId = summary.getProcessId();
            //由代理人终止需要写入处理人ID
            Map<String, Object> columnValue = new HashMap<String, Object>();
            if (affairId != null) {
                //由代理人终止需要写入处理人ID
                if (!user.isAdmin() && !user.getId().equals(affair.getMemberId())) {
                    columnValue.put("transactorId", user.getId());
                    comment.setExtAtt2(user.getName());
                }
                if (!columnValue.isEmpty() && columnValue.size() > 0) {
                    affairManager.update(affairId, columnValue);
                }
            }

            //保存终止时的意见
            comment.setModuleId(summary.getId());
            comment.setCtype(CommentType.comment.getKey());
            comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
            if (user.isAdmin()) {
                comment.setCreateId(user.getId());
            } else {
                comment.setCreateId(affair.getMemberId());
                comment.setAffairId(affairId);
            }
            comment.setExtAtt3("collaboration.dealAttitude.termination");
            if (!user.getId().equals(affair.getMemberId()) && !user.isAdmin()) {
                comment.setExtAtt2(user.getName());
            }
            comment.setModuleType(ModuleType.collaboration.getKey());
            comment.setPid(0L);
            comment.setPushMessage(false);
            if (tempMap.containsKey("collaboration.dealAttitude.termination")) {
                comment.setExtAtt1("collaboration.dealAttitude.disagree");
                comment.setContent((String) tempMap.get("collaboration.dealAttitude.termination"));
            }
            comment = saveOrUpdateComment(comment);

            //保存附件
            saveAttDatas(user, summary, affair, comment.getId());

            Long _workitemId = affair.getSubObjectId();

            //检查协同标题
            checkFormSubject(summary, affair, tempMap);
            //将终止流程的当前summary放入ThreadLocal，便于工作流中发送消息时获取代理信息。
            //添加意见
            //DateSharedWithWorkflowEngineThreadLocal.setFinishWorkitemOpinionId(comment.getId(), false, comment.getContent(), 2,false);
            MessageCommentParam mc = new MessageCommentParam(comment.getId(), false, comment.getContent(), 2, false);

            this.colStepStop(_workitemId, summary, mc, affair, comment.getPushMessageToMembersList());


            //终止 流程结束时更新督办状态
            superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervised, summary.getId(), SuperviseEnum.EntityType.summary);
            //调用表单万能方法,更新状态，触发子流程等
            if (ColUtil.isForm(summary.getBodyType())) {
                try {
                    List<Comment> commentList = this.commentManager.getCommentList(ModuleType.collaboration, affair.getObjectId());
                    capFormManager.updateDataState(summary, affair, ColHandleType.stepStop, commentList);
                } catch (Exception e) {
                    LOG.error("更新表单相关信息异常", e);
                }
            }
            //记录流程日志
            if (user.isAdministrator() || user.isGroupAdmin() || user.isSystemAdmin()) {
                processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), 1l,
                        ProcessLogAction.stepStop, comment.getId());
            } else {
                processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), affair.getActivityId(),
                        ProcessLogAction.stepStop, comment.getId());
            }
            //记录应用日志
            appLogManager.insertLog(user, AppLogAction.Coll_Flow_Stop, user.getName(), summary.getSubject());


            // 若做过指定回退的操作.做过回退发起者则被回退者的状态要从待发改为已发
            CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
            if (sendAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
                    || sendAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                sendAffair.setState(StateEnum.col_sent.getKey());
                //affair更新状态时需要更新此冗余字段
                sendAffair.setSummaryState(summary.getState());
                sendAffair.setUpdateDate(new java.util.Date());
                affairManager.updateAffair(sendAffair);
            }

            //记录操作时间
            affairManager.updateAffairAnalyzeData(affair);
            ColUtil.addOneReplyCounts(summary);
            updateColSummary(summary);

            //流程正常结束通知
            CollaborationStopEvent stopEvent = new CollaborationStopEvent(this);
            stopEvent.setSummaryId(summary.getId());
            stopEvent.setUserId(user.getId());
            stopEvent.setSenderId(summary.getStartMemberId());
            stopEvent.setTemplateId(summary.getTempleteId());
            stopEvent.setBodyType(summary.getBodyType());
            stopEvent.setAffair(affair);
            if (Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(summary.getNewflowType())) {
                if (Strings.isNotBlank(summary.getProcessId())) {
                    Long mainProcessId = wapi.getMainProcessIdBySubProcessId(Long.valueOf(summary.getProcessId()));
                    stopEvent.setMainProcessId(mainProcessId);
                }
            }
            EventDispatcher.fireEvent(stopEvent);
        } catch (Exception e) {
            LOG.error("", e);
            throw new BusinessException(e);
        } finally {
            this.wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
        }

        return null;
    }


    /**
     * 非回复 和 非发起人附言的时候走这个地方，比如撤销，回退，终止，指定回退等。
     *
     * @param c
     * @return
     * @throws BusinessException
     */
    private Comment saveOrUpdateComment(Comment c) throws BusinessException {
        Comment comment = c;
        c.setPushMessage(false);
        if (c.getId() == null) {
            comment = commentManager.insertComment(c);
        } else {
            commentManager.updateComment(c);
        }
        return comment;
    }

    @Override
    public String transStepBack(Map<String, Object> tempMap) throws BusinessException {
        Map<String, String> ret = transStepBackReturnActivityIds(tempMap);
        String msg = null;
        if (null != ret) {
            msg = ret.get("msg");
        }
        return msg;
    }


    /**
     * 回退
     * String[0] 表示本次回退结果：0表示正常回退成功；-1表示流程已结束，不能回退；-2表示不允许回退；1表示回退导致撤销，正常撤销成功；2表示成功回退到指定节点；3表示前面节点有子流程已结束，不能回退；
     * String[1]  表示节点名称（权限名称）
     *
     * @param affairData
     * @param workitemId
     * @param activityId
     * @param targetNodeId
     * @return
     * @throws BPMException
     */
    private Map<String, Object> stepBack(AffairData affairData, ColSummary summary, CtpAffair affair, String targetNodeId) throws BPMException {
        Long activityId = affair.getActivityId();
        Long workitemId = affair.getSubObjectId();
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentActivityId(activityId.toString());
        context.setAppObject(summary);
        context.setAffairId(String.valueOf(affair.getId()));
        context.setBussinessId(String.valueOf(summary.getId()));
        context.setCurrentWorkitemId(workitemId);
        context.setSelectTargetNodeId(targetNodeId);//参数可选
        context.setMastrid(affair.getFormRecordid() == null ? null : String.valueOf(affair.getFormRecordid()));
        context.setProcessId(affair.getProcessId());
        context.setFormData(affair.getFormAppId() == null ? null : String.valueOf(affair.getFormAppId()));
        context.setFormAppId(affair.getFormAppId() == null ? null : String.valueOf(affair.getFormAppId()));
        context.setBusinessData("operationType", 1);
        context.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT, summary);
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData(WorkFlowEventListener.CTPAFFAIR_CONSTANT, affair);
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID, (Long) affairData.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID));
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, affair.getMemberId());
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_SUMMARY_ID, (Long) affairData.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_SUMMARY_ID));
        context.setVersion("2.0");
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW, (String) affairData.getBusinessData(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW));
        context.setBusinessData(WorkFlowEventListener.WF_ALL_VALID_AFFAIRS, (List<CtpAffair>) affairData.getBusinessData(WorkFlowEventListener.WF_ALL_VALID_AFFAIRS));

        String[] arr = wapi.stepBack(context);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("array", arr);
        map.put(WorkFlowEventListener.BUSINESS_DATA, context.getBusinessData());

        return map;
    }

    @Override
    public Map<String, String> transStepBackReturnActivityIds(Map<String, Object> tempMap) throws BusinessException {

        Map<String, String> ret = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        String _summaryId = (String) tempMap.get("summaryId");
        String _affairId = (String) tempMap.get("affairId");
        String targetNodeId = (String) tempMap.get("targetNodeId");
        String isWFTrace = (String) tempMap.get("isWFTrace");
        targetNodeId = "".equals(targetNodeId) ? null : targetNodeId;
        Long summaryId = Long.parseLong(_summaryId);
        Long affairId = Long.parseLong(_affairId);
        Comment comment = new Comment();
        CtpAffair affair = affairManager.get(affairId);
        int state = affair.getState().intValue();
        if (state != StateEnum.col_pending.key()) {
            String msg = "";
            if (state == StateEnum.col_stepBack.key()) {
                msg = "协同《" + affair.getSubject() + "》已经被回退";
            } else if (state == StateEnum.col_takeBack.key()) {
                msg = "协同《" + affair.getSubject() + "》已经被取回";
            } else {
                msg = "协同《" + affair.getSubject() + "》已经被撤销";
            }
            ret.put("msg", msg);
            return ret;
        }
        ColSummary summary = getColSummaryById(summaryId);
        if (summary == null) {
            return null;
        }
        checkCollSubject(summary, affair, tempMap);
        //检查代理，避免不是处理人也能处理了。
        try {
            boolean canDeal = ColUtil.checkAgent(affair, summary, true);
            if (!canDeal) {
                return null;
            }
        } catch (Exception e1) {
            LOG.error("", e1);
        }
        try {

            //先保存意见否则在事件中查不到
            Comment paramComment = (Comment) tempMap.get("comment");
            if (paramComment != null) {
                comment = paramComment;
            } else {
                comment = ContentUtil.getCommnetFromRequest(ContentUtil.OperationType.stepBack, affair.getMemberId(),
                        affair.getObjectId());
            }
            //保存意见
            comment.setCtype(0);
            comment.setModuleId(summary.getId());
            comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
            comment.setCreateId(affair.getMemberId());
            comment.setAffairId(affair.getId());
            comment.setExtAtt3("collaboration.dealAttitude.rollback");
            if (!user.getId().equals(affair.getMemberId())) {
                comment.setExtAtt2(user.getName());
            }
            comment.setModuleType(ModuleType.collaboration.getKey());
            comment.setPid(0L);
            comment.setPushMessage(false);
            comment = saveOrUpdateComment(comment);

            AffairData affairData = ColUtil.getAffairData(summary);
            affairData.getBusinessData().put(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW, isWFTrace);
            affairData.getBusinessData().put(WorkFlowEventListener.COLSUMMARY_CONSTANT, summary);
            affairData.getBusinessData().put(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID, affairId);
            affairData.getBusinessData().put(WorkFlowEventListener.CURRENT_OPERATE_SUMMARY_ID, summaryId);


            List<CtpAffair> aLLAvailabilityAffairList = affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, summary.getId());


            //回退 String[0] 表示本次回退结果：0表示正常回退成功；-1表示流程已结束，不能回退；
            // -2表示不允许回退；1表示回退导致撤销，正常撤销成功；2表示成功回退到指定节点；3表示前面节点有子流程已结束，不能回退；
            //String[1] 表示节点名称（权限名称）
            affairData.getBusinessData().put(WorkFlowEventListener.WF_ALL_VALID_AFFAIRS, aLLAvailabilityAffairList);
            Map<String, Object> map = this.stepBack(affairData, summary, affair, targetNodeId);


            String[] result = (String[]) map.get("array");
            //是否回退到发起节点
            boolean backToSender = "1".equals(result[0]);
            Map<String, Object> businessData = (Map<String, Object>) map.get(WorkFlowEventListener.BUSINESS_DATA);
            List<CtpAffair> assignedAffairs = (List<CtpAffair>) businessData.get(WorkFlowEventListener.ASSIGNED_AFFAIRS);
            Map<Long, Long> canceledMIdToAIdMap = (Map<Long, Long>) businessData.get(WorkFlowEventListener.CANCELED_MIDTOAID_MAP);
            if (backToSender) {
                //重新获取
                List<CtpAffair> affairList = affairManager.getAffairs(summary.getId());
                canceledMIdToAIdMap = new HashMap<Long, Long>();
                for (CtpAffair affair1 : affairList) {
                    if (!user.getId().equals(affair1.getMemberId())) {
                        canceledMIdToAIdMap.put(affair1.getMemberId(), affair1.getId());
                    }
                }
            }
            if ("-2".equals(result[0])) {
                //不允许回退
                StringBuffer info = new StringBuffer();
                info.append("《").append(affair.getSubject()).append("》").append("\n");
                String msg = ResourceUtil.getString("collaboration.takeBack.alert", info.toString());
                ret.put("msg", msg);
                return ret;
            } else if ("-1".equals(result[0])) {
                //表示流程已结束，不能回退
                StringBuffer info = new StringBuffer();
                info.append("《").append(affair.getSubject()).append("》").append("\n");
                String msg = ResourceUtil.getString("collaboration.takeBack.alert.dimission", info.toString());
                ret.put("msg", msg);
                return ret;
            }


            //A发协同给A A直接回退 回复数变成了2   原因：这里增加了一次 回退导致撤销  这个transRepalBackground方法也增加了一次
            if (!backToSender) {
                ColUtil.updateCurrentNodesInfo(summary);  //撤销的情况repealBackGround直接清空了不用计算了。
                ColUtil.addOneReplyCounts(summary);
            }
            updateColSummary(summary);


            //需要撤消流程
            if (backToSender) {
                createRepealData2BeginNode(affair, summary, aLLAvailabilityAffairList, WorkflowTraceEnums.workflowTrackType.step_back_repeal, isWFTrace);
            }

            //保存附件
            saveAttDatas(user, summary, affair, comment.getId());

            //记录操作时间
            affairManager.updateAffairAnalyzeData(affair);

            //流程回退事件
            CollaborationStepBackEvent backEvent = new CollaborationStepBackEvent(this);
            backEvent.setSummaryId(summary.getId());
            backEvent.setTemplateId(summary.getTempleteId());
            backEvent.setSenderId(summary.getStartMemberId());
            backEvent.setBodyType(summary.getBodyType());
            backEvent.setUserId(user.getId());
            EventDispatcher.fireEventAfterCommit(backEvent);

            //发送回退消息
            List<CtpAffair> trackingAffairLists = affairManager.getValidTrackAffairs(summary.getId());
            /**项目：徐矿集团 【屏蔽回退消息】 作者：jiangchenxi 时间：2019年3月14日 start*/
            //boolean open =  xkjtManager.issendmessage(affair.getId(), affair.getSenderId());
            //if(open){
            colMessageManager.stepBackMessage(trackingAffairLists, affair, summaryId, comment, "1".equals(isWFTrace) ? true : false, backToSender, canceledMIdToAIdMap, businessData);
            //}
            /**项目：徐矿集团 【屏蔽回退消息】 作者：jiangchenxi 时间：2019年3月14日 end*/
            //记录流程日志
            processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.stepBack, comment.getId(), result[1]);
            //记录应用日志
            appLogManager.insertLog(user, AppLogAction.Coll_Step_Back, user.getName(), summary.getSubject());

            if (!backToSender) { //撤销的时候repealBackGround会处理。
                //更新全文检索
                if (AppContext.hasPlugin("index")) {
                    if (ColUtil.isForm(summary.getBodyType())) {
                        indexManager.update(summaryId, ApplicationCategoryEnum.form.getKey());
                    } else {
                        indexManager.update(summaryId, ApplicationCategoryEnum.collaboration.getKey());
                    }
                }
                if (ColUtil.isForm(summary.getBodyType())) {
                    try {
                        List<Comment> commentList = this.commentManager.getCommentAllByModuleId(ModuleType.collaboration, affair.getObjectId());
                        capFormManager.updateDataState(summary, affair, "1".equals(result[0]) ? ColHandleType.repeal : ColHandleType.stepBack, commentList);
                    } catch (Exception e) {
                        LOG.error("更新表单相关信息异常", e);
                        throw new BusinessException("更新表单相关信息异常", e);
                    }
                }
            }
            ret.put("msg", null);
            ret.put("targetActivityIds", result[3]);
            return ret;

        } finally {
            this.wapi.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(AppContext.currentUserId()));
        }
    }


    private void createRepealData2BeginNode(CtpAffair affair, ColSummary summary, List<CtpAffair> aLLAvailabilityAffairList, WorkflowTraceEnums.workflowTrackType trackType, String _trackWorkflowType) throws BusinessException {
        List<CtpAffair> traceAffairs = new ArrayList<CtpAffair>();
        for (CtpAffair aff : aLLAvailabilityAffairList) {
            if (Integer.valueOf(StateEnum.col_done.key()).equals(aff.getState())
                    || (Integer.valueOf(StateEnum.col_pending.key()).equals(aff.getState()) && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(aff.getSubState()))
                    || (aff.getId().equals(affair.getId()) &&
                    !(Integer.valueOf(StateEnum.col_sent.key()).equals(aff.getState()) || Integer.valueOf(StateEnum.col_waitSend.key()).equals(aff.getState())))) {
                traceAffairs.add(aff);
            }
        }
        CtpTemplate template = null;
        if (summary.getTempleteId() != null) {
            template = templateManager.getCtpTemplate(summary.getTempleteId());
        }
        colTraceWorkflowManager.createStepBackTrackDataToBegin(summary, affair, traceAffairs, template, trackType, _trackWorkflowType);
    }


    public void transReMeToReGo(WorkflowBpmContext context) throws BusinessException {

        long summaryId = 0l;
        ColSummary summary = (ColSummary) context.getAppObject();
        if (summary != null) {
            summaryId = summary.getId();
        }
        summary.setSubState(CollaborationEnum.SubState.Normal.ordinal());
        // summary不用做操作
        List<CtpAffair> allAvailableAffairs = affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, Long.valueOf(summaryId));

        CtpAffair stepBackAffair = (CtpAffair) context.getBusinessData().get("_ReMeToReGo_stepBackAffair");

        Long currentAffairId = (Long) context.getBusinessData().get(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID);

        CtpAffair currentAffair = null;
        if (allAvailableAffairs != null) {
            for (int i = 0; i < allAvailableAffairs.size(); i++) {
                CtpAffair affair = (CtpAffair) allAvailableAffairs.get(i);

                if (affair.getId().equals(currentAffairId)) {
                    currentAffair = affair;
                }
            }
        }

        if ("start".equals(context.getCurrentActivityId())) {
            // 将summary的状态改为待发,撤销已生成事项
            CtpAffair sendAffair = null;
            if (allAvailableAffairs != null) {
                for (int i = 0; i < allAvailableAffairs.size(); i++) {
                    CtpAffair affair = (CtpAffair) allAvailableAffairs.get(i);
                    if (affair.getState().intValue() == StateEnum.col_sent.key()
                            || affair.getState().intValue() == StateEnum.col_waitSend.key()) {

                        sendAffair = affair;
                    }


                    if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                        QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                        QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
                    }
                    if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                        QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId()); //保证老数据不报错, 6.0后面的版本把这个删除
                        QuartzHolder.deleteQuartzJob("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
                    }
                    //撤销(回退节点删除多次消息提醒任务)
                    ColUtil.deleteCycleRemindQuartzJob(affair, false);
                }
            }


            long userId = Strings.isNotBlank(context.getCurrentUserId()) ? Long.valueOf(context.getCurrentUserId()) : 0l;
            do4Repeal(userId, "", summary, allAvailableAffairs, false, null);


            //调用表单万能方法,更新状态，触发子流程等
            if (ColUtil.isForm(sendAffair.getBodyType())) {
                try {
                    capFormManager.RelationAuthorityBySummaryId(summaryId, ModuleType.collaboration.getKey());//撤销时删除表单授权信息
                    if (null != sendAffair) {//清楚表单授权信息
                        AffairUtil.setIsRelationAuthority(sendAffair, false);
                        DBAgent.update(sendAffair);
                    }
                } catch (Exception e) {
                    LOG.error("更新表单相关信息异常", e);
                }
            }

            //创建流程追述的数据
            WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_repeal;
            createRepealData2BeginNode(stepBackAffair, summary, allAvailableAffairs, trackType, "1");


            //删除ISIgnatureHTML专业签章
            iSignatureHtmlManager.deleteAllByDocumentId(summary.getId());
        }
        Map<Long, Long> canceledAffairMap = this.getCanceledAffairMap(allAvailableAffairs);
        colMessageManager.reMeToRegoMessage(currentAffair, canceledAffairMap);

        // 记录流程日志
        long activityId = "start".equals(context.getCurrentActivityId()) ? -1 : Long.parseLong(context.getCurrentActivityId());
        ProcessLogAction action = "start".equals(context.getCurrentActivityId()) ? ProcessLogAction.colStepBackReMeToReGo4Send : ProcessLogAction.colStepBackReMeToReGo;
        processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(context.getProcessId()),
                activityId, action);


    }

    private Map<Long, Long> getCanceledAffairMap(List<CtpAffair> affairs) throws BusinessException {

        Map<Long, Long> map = new HashMap<Long, Long>();
        for (CtpAffair affair0 : affairs) {
            if ((Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair0.getState()) || Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair0.getState())) && !affair0.isDelete()) {
                continue;
            }
            map.put(affair0.getMemberId(), affair0.getId());
        }
        return map;
    }

    /**
     * @param user
     * @param repealComment
     * @param summary
     * @param aLLAvailabilityAffairList
     * @param isFireEvent               是否触发事件
     * @throws BusinessException
     */
    private void do4Repeal(long userId, String repealComment, ColSummary summary, List<CtpAffair> aLLAvailabilityAffairList,
                           boolean isFireEvent, Integer _ioperationType) throws BusinessException {

        List<Long> trackPartAffairIds = new ArrayList<Long>();
        List<Long> archivedAffairIds = new ArrayList<Long>();
        for (CtpAffair affair0 : aLLAvailabilityAffairList) {
            if ((Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair0.getState()) || Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair0.getState())) && !affair0.isDelete()) {
                continue;
            }


            if (Integer.valueOf(TrackEnum.part.ordinal()).equals(affair0.getTrack())) {
                trackPartAffairIds.add(affair0.getId());
            }

            if (affair0.getArchiveId() != null) {
                archivedAffairIds.add(affair0.getId());
            }
        }

        if (Strings.isNotEmpty(trackPartAffairIds)) {
            trackManager.deleteTrackMembersByAffairIds(trackPartAffairIds);
        }

        Long summaryId = summary.getId();
        if (summary.getArchiveId() != null) {
            CtpAffair sendAffair = affairManager.getSenderAffair(summaryId);
            archivedAffairIds.add(sendAffair.getId());
        }

        //置空归档值
        String advancePigeonhole = summary.getAdvancePigeonhole();
        if (Strings.isNotBlank(advancePigeonhole)) {
            Map<String, String> advancePigeoholeMap = (Map<String, String>) JSONUtil.parseJSONString(advancePigeonhole);
            advancePigeoholeMap.remove("archiveFieldValue");
            summary.setAdvancePigeonhole(JSONUtil.toJSONString(advancePigeoholeMap));
        }

        if (AppContext.hasPlugin("doc") && Strings.isNotEmpty(archivedAffairIds)) {
            docApi.deleteDocResources(userId, archivedAffairIds);
        }
        // 回退到发起节点时，如果有督办则修改督办状态

        this.superviseManager.updateStatus2Cancel(summaryId);

        // 流程撤销到首发，发送流程撤销事件
        if (isFireEvent) {
            CollaborationCancelEvent cancelEvent = new CollaborationCancelEvent(this);
            cancelEvent.setSummaryId(summaryId);
            cancelEvent.setUserId(userId);
            cancelEvent.setSenderId(summary.getStartMemberId());
            cancelEvent.setTemplateId(summary.getTempleteId());
            cancelEvent.setBodyType(summary.getBodyType());
            cancelEvent.setMessage(repealComment);
            cancelEvent.setType(_ioperationType);
            EventDispatcher.fireEventAfterCommit(cancelEvent);
        }


    }

    /**
     * 回退的时候设置 已发-->待办
     * 子状态   正常 ->（协同-待发-被回退）
     *
     * @param userId
     * @param summaryId
     * @param from
     * @return
     * @throws BusinessException
     */
    public int stepBackSummary(long userId, long summaryId, int from) throws BusinessException {

        return 0;
    }

    @Override
    public int getColCount(long memberId, int state, List<Long> templeteIds) throws BusinessException {
        return colDao.getColCount(state, memberId, templeteIds);
    }

    public List<SuperviseModelVO> getColSuperviseModelList(FlipInfo filpInfo, Map<String, String> map) {
        return colDao.getColSuperviseModelList(filpInfo, map);
    }

    public String getTrackInfosToString(TrackAjaxTranObj obj) throws BusinessException {
        String objectId = obj.getObjectId();
        List<CtpTrackMember> boList = trackManager.getTrackMembers(objectId);
        StringBuffer memberInfo = new StringBuffer();
        String mInfo = "";
        if (boList.size() > 0) {
            for (CtpTrackMember member : boList) {
                Long mId = member.getTrackMemberId();
                memberInfo.append("Member|" + mId + ",");
            }
            mInfo = memberInfo.toString();
            if (mInfo.length() > 0) {
                mInfo = mInfo.substring(0, mInfo.length() - 1);
            }
        }
        return mInfo;
    }

    private List<ProjectBO> getProjectList(NewCollTranVO vobj, Long projectId) throws BusinessException {
        if (null != projectApi) {
            List<ProjectBO> projectList = projectApi.findProjectsByMemberId(vobj.getUser().getId());
            boolean flag = true;
            if (projectId != null) {
                ProjectBO p = projectApi.getProject(projectId);
                if (p != null && p.getProjectState() != ProjectBO.STATE_DELETE.intValue()) {
                    if (projectList == null) {
                        projectList = new ArrayList<ProjectBO>();
                        projectList.add(p);
                    } else if (!projectList.contains(p)) {
                        projectList.add(p);
                    }
                } else {
                    projectId = null;
                    flag = false;
                }
            }
            if (null == projectId && null != vobj.getProjectId() &&
                    Strings.isNotBlank(vobj.getProjectId().toString())) {
                projectId = vobj.getProjectId();
                ProjectBO p = projectApi.getProject(vobj.getProjectId());
                if (p != null && p.getProjectState() != ProjectBO.STATE_DELETE.intValue()) {
                    if (projectList == null) {
                        projectList = new ArrayList<ProjectBO>();
                        projectList.add(p);
                    } else if (!projectList.contains(p)) {
                        projectList.add(p);
                    }
                } else {
                    projectId = null;
                }
            }
            if (flag) {
                vobj.setProjectId(projectId);
            } else {
                vobj.setProjectId(null);
                vobj.getTemplate().setProjectId(null);
            }
            vobj.setProjectList(projectList);
            return projectList;
        }
        return null;
    }

    private boolean isParentSystemTemplete(Long templeteId) throws BusinessException {
        CtpTemplate t = getParentSystemTemplete(templeteId);
        if (t == null)
            return false;
        if (t.isSystem())
            return true;
        else
            return false;
    }

    /**
     * 得到父级系统模板
     *
     * @param templeteId
     * @return
     * @throws BusinessException
     */
    private CtpTemplate getParentSystemTemplete(Long templeteId) throws BusinessException {
        if (templeteId == null) {
            return null;
        }
        boolean needQueryParent = true;
        CtpTemplate t = null;
        while (needQueryParent) {
            t = templateManager.getCtpTemplate(templeteId);
            if (t == null) {
                needQueryParent = false;
                return null;
            }
            if (t.isSystem()) {
                needQueryParent = false;
                return t;
            }
            if (t.getFormParentid() == null) {
                needQueryParent = false;
                return null;
            }
            templeteId = t.getFormParentid();
        }
        return t;
    }

    /**
     * 时间轴查询协同
     */
    public List<ColSummaryVO> getMyCollDeadlineNotEmpty(Map<String, Object> tempMap) throws BusinessException {
        List<ColSummaryVO> colList = colDao.getMyCollDeadlineNotEmpty(tempMap);
        return colList;
    }

    @Override
    public void updateTempleteHistory(Long id) throws BusinessException {
        collaborationTemplateManager.updateTempleteHistory(id);
    }

    @Override
    public String getPigeonholeRight(List<String> collIds) throws BusinessException {
        return getPigeonholeRightForM3(collIds, false);
    }

    @Override
    public String getPigeonholeRightForM3(List<String> collIds, boolean fromM3) throws BusinessException {
        StringBuilder result = new StringBuilder();
        CtpAffair affair = null;
        ColSummary summary = null;
        List<String> permissions = null;
        List<String> actions = null;
        int count = 1;
        for (String id : collIds) {
            affair = getAffairById(Long.parseLong(id));
            summary = getSummaryById(affair.getObjectId());
            //允许操作中不勾选归档：
            //(作废)1、归档人是发起人是可以归档的（从已发列表和待办、已办中都可以归档） 2016-02-24改成没权限都不能归档
            //2、归档人是非发起人，不可以归档
            //if(affair.getState().intValue() == StateEnum.col_sent.getKey()) continue;
            long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
            // 判断协同设置是否允许归档
            actions = permissionManager.getActionList(EnumNameEnum.col_flow_perm_policy.name(), affair.getNodePolicy(), accountId);
            // 不允许归档的情况有：
            // 1. 节点动作不包含处理后归档
            // 2. 协同不允许归档
            // 3. 节点权限设置意见不能为空
            if ((actions.contains("Pigeonhole") || actions.contains("Archive")) && (summary.getCanArchive() != null && summary.getCanArchive())) {
                permissions = permissionManager.getRequiredOpinionPermissions(EnumNameEnum.col_flow_perm_policy.name(), accountId);
                // 待办的情况下
                if (affair.getState().intValue() == StateEnum.col_pending.getKey() && permissions.contains(affair.getNodePolicy())) {
                    result.append("以下事项要求意见不能为空，不能直接归档或删除。").append(summary.getSubject()).append("<br>");
                }
            } else {
                if (fromM3) {
                    result.append(count + ".").append(ResourceUtil.getString("collaboration.pigeonhole.notallow.note", summary.getSubject())).append("<br>");
                    count++;
                } else {
                    result.append(ResourceUtil.getString("collaboration.pigeonhole.notallow.note", summary.getSubject())).append("\r\n");
                }
            }
        }
        return result.toString();
    }

    @Override
    public String getIsSamePigeonhole(List<String> collIds, Long destFolderId) throws BusinessException {
        return getIsSamePigeonhole(collIds, destFolderId, true);
    }

    public String getIsSamePigeonhole(List<String> collIds, Long destFolderId, boolean fromPc) throws BusinessException {
        StringBuilder result = new StringBuilder();
        CtpAffair affair = null;
        boolean isFirst = true;
        String separate = "<br>";
        if (fromPc) {
            separate = "\r";
        }
        for (String id : collIds) {
            affair = getAffairById(Long.parseLong(id));
            if (affair != null) {
                List<Long> sourceIds = new ArrayList<Long>();
                sourceIds.add(affair.getObjectId());
                int _key = ApplicationCategoryEnum.collaboration.getKey();
                if (ColUtil.isForm(affair.getBodyType())) {
                    _key = ApplicationCategoryEnum.form.getKey();
                }
                if (docApi.hasSamePigeonhole(destFolderId, sourceIds, _key)) {
                    if (isFirst) {
                        isFirst = false;
                        result.append(ResourceUtil.getString("collaboration.pigeonhole.getIsSamePigeonhole.lable") + separate);
                    }
                    result.append("<" + affair.getSubject() + ">" + separate);
                }
            }
        }
        return result.toString();
    }

    @Override
    public String transPigeonhole(ColSummary summary, CtpAffair affair, Long destFolderId, String type) throws BusinessException {
        boolean hasAttachment = ColUtil.isHasAttachments(summary);
        StringBuilder result = new StringBuilder();
        User user = AppContext.getCurrentUser();
        List<Boolean> hasAttachments = new ArrayList<Boolean>();
        List<Long> collIdLongs = new ArrayList<Long>();
        collIdLongs.add(affair.getId());
        hasAttachments.add(hasAttachment);

        boolean isForm = ColUtil.isForm(summary.getBodyType());
        int app = ApplicationCategoryEnum.collaboration.key();
        if (isForm) {
            app = ApplicationCategoryEnum.form.key();
        }

        List<Long> results = docApi.pigeonhole(
                AppContext.currentUserId(),
                app,
                collIdLongs,
                hasAttachments,
                null,
                destFolderId,
                PigeonholeType.edoc_dept.ordinal());

        if (results != null && results.size() == 1) {
            DocResourceBO res = docApi.getDocResource(results.get(0));
            affair.setArchiveId(res.getParentFrId());
            String forderName = docApi.getDocResourceName(res.getParentFrId());
            appLogManager.insertLog(user, AppLogAction.Coll_Pigeonhole, user.getName(), res.getFrName(), forderName);
            //先commit，防止affair出现重复id的情况
            DBAgent.commit();
            affairDao.update(affair);
        }
        return result.toString();
    }

    @Override
    public String transPigeonhole(Long affairId, Long destFolderId, String type) throws BusinessException {
        User user = AppContext.getCurrentUser();
        List<Boolean> hasAttachments = new ArrayList<Boolean>();
        List<Long> collIdLongs = new ArrayList<Long>();
        collIdLongs.add(affairId);

        CtpAffair affair = getAffairById(affairId);
        ColSummary summary = getSummaryById(affair.getObjectId());
        hasAttachments.add(ColUtil.isHasAttachments(summary));

        boolean isForm = ColUtil.isForm(summary.getBodyType());
        int app = ApplicationCategoryEnum.collaboration.key();
        if (isForm) {
            app = ApplicationCategoryEnum.form.key();
        }
        List<Long> results = docApi.pigeonhole(
                AppContext.currentUserId(),
                app,
                collIdLongs,
                hasAttachments,
                null,
                destFolderId,
                null);


        affair.setArchiveId(results.get(0));

        DocResourceBO res = docApi.getDocResource(results.get(0));
        String forderName = docApi.getDocResourceName(res.getParentFrId());
        appLogManager.insertLog(user, AppLogAction.Coll_Pigeonhole, user.getName(), res.getFrName(), forderName);

        affair.setArchiveId(res.getParentFrId());
        affairDao.update(affair);

        if ("pending".equals(type)) {
            Comment comment = getNullDealComment(affair.getId(), affair.getObjectId());
            comment.setExtAtt3(CommentExtAtt3Enum.pighole_pending_skip.getI18nLabel());


            Map<String, Object> param = new HashMap<String, Object>();
            param.put(WorkFlowEventListener.AFFAIR_SUB_STATE, String.valueOf(SubStateEnum.col_done_pighone.getKey()));
            transFinishWorkItemPublic(affairId, comment, param);

        }

        return "";
    }

    @Override
    public String transPigeonholeDeleteStepBackDoc(List<String> collIds, Long destFolderId) throws BusinessException {
        StringBuilder result = new StringBuilder();
        if (CollectionUtils.isEmpty(collIds))
            return result.toString();
        // 归档时同一协同已被回退的文档删除
        List<Long> needDeleteDocs = new ArrayList<Long>();
        Map<Long, List<Long>> objectDocMap = new HashMap<Long, List<Long>>();

        List<String> appIds = new ArrayList<String>();
        appIds.add(String.valueOf(ApplicationCategoryEnum.collaboration.getKey()));
        List<DocResourceBO> reses = docApi.findDocResourcesByType(destFolderId, appIds);
        if (!CollectionUtils.isEmpty(reses)) {
            CtpAffair affairPig = null;
            List<Long> affairIds = null;
            for (DocResourceBO docResourcePO : reses) {
                affairPig = affairManager.get(docResourcePO.getSourceId());
                if (affairPig != null && affairPig.getState().intValue() == StateEnum.col_stepBack.getKey()) {
                    affairIds = objectDocMap.get(affairPig.getObjectId());
                    if (affairIds == null) {
                        affairIds = new ArrayList<Long>();
                    }
                    affairIds.add(affairPig.getId());
                    objectDocMap.put(affairPig.getObjectId(), affairIds);
                }
            }
        }
        CtpAffair affair = null;
        for (String id : collIds) {
            affair = getAffairById(Long.parseLong(id));
            if (objectDocMap.get(affair.getObjectId()) != null) {
                needDeleteDocs.addAll(objectDocMap.get(affair.getObjectId()));
            }
        }
        if (!CollectionUtils.isEmpty(needDeleteDocs)) {
            docApi.deleteDocResources(AppContext.getCurrentUser().getId(), needDeleteDocs);
        }
        return result.toString();
    }

    //构造一个空的Comment对象
    private Comment getNullDealComment(Long affairId, Long summaryId) {
        Comment comment = new Comment();
        comment.setId(UUIDLong.longUUID());
        comment.setAffairId(affairId);
        comment.setModuleId(summaryId);
        comment.setCreateId(AppContext.currentUserId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setClevel(1);
        comment.setCtype(Comment.CommentType.comment.getKey());
        comment.setHidden(false);
        comment.setContent("");
        comment.setModuleType(ApplicationCategoryEnum.collaboration.getKey());
        comment.setPath("001");
        comment.setPid(0l);
        //TODO 态度，3.5的时候直接归档是没有态度的
        return comment;
    }

    @Override
    public void transCancelTrack(TrackAjaxTranObj obj) throws BusinessException {
        Object obj1 = obj.getAffairId();
        if (obj1 != null) {
            String[] affairIds = obj1.toString().split("[,]");
            for (int i = 0; i < affairIds.length; i++) {
                Map<String, Object> columnValue = new HashMap<String, Object>();
                columnValue.put("track", 0);
                //更新affair事项表状态
                Long affairId = Long.parseLong(affairIds[i]);
                this.affairManager.update(affairId, columnValue);
                trackManager.deleteTrackMembers(null, affairId);
            }
        }
    }

    /**
     * yangwulin 协同转发邮件
     *
     * @param params
     * @return
     * @throws BusinessException
     */
    public ModelAndView getforwordMail(Map params) throws BusinessException {

        User user = AppContext.getCurrentUser();

        try {
            boolean hasDefaultMbc = webmailApi.hasDefaultMbc(user.getId());
            if (!hasDefaultMbc) {
                ModelAndView mav = new ModelAndView("webmail/error");
                mav.addObject("errorMsg", "2");
                mav.addObject("errorUrls", "?method=list&jsp=set");
                return mav;
            }
        } catch (Exception e1) {
            LOG.error("调用邮件接口判断当前用户是否有邮箱设置：", e1);
        }
        Long summaryId = Long.parseLong(String.valueOf(params.get("summaryId")));
        Long affairId = Long.parseLong(String.valueOf(params.get("affairId")));
        ColSummary summary = this.getColSummaryById(summaryId);
        //summary.getF
        String subject = summary.getSubject();

        List<Attachment> atts = attachmentManager.getByReference(summaryId, summaryId);
        //ColBody body = summary.getFirstBody();
        CtpAffair affair = null;
        if (affairId != null) {
            affair = affairManager.get(affairId);
        }
        if (affair == null) {
            affair = affairManager.getSenderAffair(summaryId);
        }
        String _rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(affair, summary.getTempleteId());
        if (Strings.isNotBlank(_rightId) && "-1.-1".equals(_rightId)) {
            _rightId = "-1";
        }
//        List<CtpContentAll> contentList = ctpMainbodyManager.getContentList(ModuleType.collaboration, summary.getId(), _rightId);
        List<CtpContentAll> contentList = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
        CtpContentAll ctpContent = contentList.get(0);
        //attachmentManager.get
        Date createDate = ctpContent.getCreateDate();

        String bodyType = summary.getBodyType();

        String bodyContent = "";

        boolean isFormCap4 = capFormManager.isCAP4Form(ctpContent.getContentTemplateId());

        if (ColUtil.isForm(bodyType) || isFormCap4) {
            //将表单正文转换成HTML
//            bodyContent = MainbodyService.getInstance().getContentHTML(ctpContent.getModuleType(), ctpContent.getModuleId());
            bodyContent = capFormManager.getFormDataHtmlForForward(ctpContent.getContentTemplateId(), ctpContent.getModuleType(), ctpContent.getModuleId(), _rightId);
        } else if (String.valueOf(MainbodyType.HTML.getKey()).equals(bodyType)) {
            bodyContent = ctpContent.getContent();
        } else if (String.valueOf(MainbodyType.WpsWord.getKey()).equals(bodyType)
                || String.valueOf(MainbodyType.OfficeWord.getKey()).equals(bodyType)
                || String.valueOf(MainbodyType.OfficeExcel.getKey()).equals(bodyType)
                || String.valueOf(MainbodyType.WpsExcel.getKey()).equals(bodyType)
                || String.valueOf(MainbodyType.Pdf.getKey()).equals(bodyType)) {
            File file = null;
            try {
                file = this.fileManager.getStandardOffice(CoderFactory.getInstance().decryptFileToTemp(fileManager.getFile(ctpContent.getContentDataId()).getAbsolutePath()));
                V3XFile f = this.fileManager.save(file, ApplicationCategoryEnum.mail, subject + "." + Convertor.getSufficName(bodyType), createDate, false);
                atts.add(new Attachment(f, ApplicationCategoryEnum.mail, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE));
            } catch (Exception e) {
                LOG.error("协同转发为邮件错误 [summaryId = " + summaryId + "]", e);
            }
        }

        ModelAndView mv = webmailApi.forwardMail(summaryId, subject, bodyContent, atts);

        return mv;
    }

    /**
     * @param fileManager the fileManager to set
     */
    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateAffairIdentifierForRelationAuth(Map param) throws BusinessException {
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        List<String> ids = (List<String>) param.get("affairIds");
        Boolean flag = (Boolean) param.get("flag");
        for (String _id : ids) {
            if (!StringUtil.checkNull(_id)) {
                Long affairId = Long.parseLong(_id);
                CtpAffair ctpAffair = getAffairById(affairId);
                AffairUtil.setIsRelationAuthority(ctpAffair, flag);
                affairs.add(ctpAffair);
            }
        }

        DBAgent.updateAll(affairs);
    }

    /**
     * 协同展示附件列表
     * 一、附件来源根据区域划分为：标题区、正文区、附言区、意见区
     */
    @Override
    public List<AttachmentVO> getAttachmentListBySummaryId(Long summaryId, Long memberId)
            throws BusinessException {
        List<Attachment> tempattachments = null;
        List<Attachment> attachments = new ArrayList<Attachment>();
        List<AttachmentVO> attachmentVOs = new ArrayList<AttachmentVO>();
        //收藏时候传入的文件Id
        List<Long> attmentIds = new ArrayList<Long>();
        AttachmentVO vo = null;
        HttpServletRequest request = AppContext.getRawRequest();
        String formAttrId = request.getParameter("formAttrId");
        List formAttrIds = new ArrayList<String>();
        //获取正文组件中自带的附件
        String attmentContent = request.getParameter("attmentList");
        List<String> attmentList = new ArrayList<String>();
        List<String> _attmentList = new ArrayList<String>();
        if (Strings.isNotBlank(attmentContent) && !"null".equals(attmentContent)) {
            _attmentList = Arrays.asList(attmentContent.split(","));
            for (int a = _attmentList.size() - 1; a > -1; a--) {
                if (!attmentList.contains(_attmentList.get(a))) {
                    attmentList.add(_attmentList.get(a));
                }
            }
        }
        if (Strings.isNotBlank(formAttrId)) {
            formAttrIds = Arrays.asList(formAttrId.split(","));
        }
        ColSummary colSummary = getSummaryById(summaryId);
        tempattachments = attachmentManager.getByReference(summaryId);

        //控制隐藏的评论对发起人可见
        AppContext.putThreadContext(Comment.THREAD_CTX_NOT_HIDE_TO_ID_KEY, colSummary.getStartMemberId());

        //添加附件到对象中，附件的type为0，关联文档的type为2（不显示关联文档在附件列表中）
        for (Attachment attachment : tempattachments) {
            if (attachment.getType() == 0) {
                attachments.add(attachment);
            }
        }
        if (attachments != null && attachments.size() > 0) {
            boolean isHistoryFlag = "true".equals(request.getParameter("isHistoryFlag")) ? true : false;
            List<Comment> comments = commentManager.getCommentAllByModuleId(ModuleType.collaboration, summaryId, isHistoryFlag);
            List<Comment> childComments = new ArrayList<Comment>();
            for (Attachment attachment : attachments) {
                vo = new AttachmentVO();
                createAttachmentVO(vo, attachment);
                //创建人
                Long commentId = attachment.getSubReference();
                Comment curComment = null;
                for (Comment comment : comments) {
                    if (comment.getId().longValue() == commentId.longValue()) {
                        curComment = comment;
                        break;
                    }
                }
                /**
                 * 1、发起人上传：标题下方的由发起人上传的附件，如果流程中间的节点跟发起人是同一个人，也算”处理人回复“
                 * 2、处理人回复：意见区域的附件（评论&回复），通过修改附件区域上传的附件
                 * 3、表单控件：表单正文里面的附件
                 * 4、附言补充：发起人附言区域里面的附件
                 */

                if (curComment != null) {
                    if (!curComment.isCanView()) continue;
                    String agentName = curComment.getExtAtt2();
                    Long createId = curComment.getCreateId();
                    if (Strings.isNotBlank(agentName)) {
                        vo.setUserName(agentName);
                    } else {
                        V3xOrgMember member = orgManager.getMemberById(createId);
                        vo.setUserName(member.getName());
                    }

                    String fromType = "";
                    //来源
                    Integer cType = curComment.getCtype();
                    //如果是草稿状态时，不显示附件
                    if (cType != null) {
                        int ct = cType.intValue();
                        if (ct == -2) {
                            continue;
                        }
                        //如果是转发的则显示成正文区
                        if (curComment.getForwardCount() > 0) {
                            fromType = ResourceUtil.getString("collaboration.att.form");
                        } else {
                            if (ct == -1) {
                                fromType = ResourceUtil.getString("collaboration.att.sender");//"附言区";
                            } else if (ct == 1 || ct == 0) {
                                fromType = ResourceUtil.getString("collaboration.att.reply");//"处理区";
                            }
                        }
                    }
                    attmentIds.add(attachment.getFileUrl());
                    vo.setFromType(fromType);
                    attachmentVOs.add(vo);
                } else if (summaryId.equals(attachment.getSubReference())) {
                    //标题区
                    Date attaDate = attachment.getCreatedate();
                    Date colDate = colSummary.getCreateDate();
                    //将协同创建时间加上2秒,以判断是否是发起人上传，因为在发起人上传时，必须保证附件的创建时间小于协同的创建时间
                    //但是在实际情况中由于保存时都是用的new Date()方式存储日期，导致时间有误差(ms级)
                    if (attaDate.getTime() < colDate.getTime() + 2000) {
                        try {
                            V3xOrgMember member = orgManager.getMemberById(colSummary.getStartMemberId());
                            vo.setUserName(member.getName());
                        } catch (Exception e) {
                            LOG.error("", e);
                        }
                        vo.setFromType(ResourceUtil.getString("collaboration.att.titleArea"));//标题区
                    } else {
                        V3XFile file = fileManager.getV3XFile(attachment.getFileUrl());
                        String name = "";
                        if (file != null) {
                            try {
                                V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
                                name = member.getName();
                            } catch (Exception e) {
                                LOG.error("", e);
                            }
                        }
                        vo.setUserName(name);
                        //处理人回复（处理区）
                        vo.setFromType(ResourceUtil.getString("collaboration.att.titleArea"));
                    }
                    attmentIds.add(attachment.getFileUrl());
                    attachmentVOs.add(vo);
                } else if (AttachmentEditUtil.CONTENT_ATTACHMENTSUBRE.equals(attachment.getSubReference().toString())) { //来之转发表单控件的附件
                    V3XFile file = fileManager.getV3XFile(attachment.getFileUrl());
                    String name = "";
                    if (file != null) {
                        try {
                            V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
                            name = member.getName();
                        } catch (Exception e) {
                            LOG.error("", e);
                        }
                    }
                    vo.setUserName(name);
                    //（来自表单转发控件的属于正文区域）
                    vo.setFromType(ResourceUtil.getString("collaboration.att.form"));
                    attmentIds.add(attachment.getFileUrl());
                    attachmentVOs.add(vo);
                }
            }
        }
        //表单中的附件
        if (formAttrIds != null && formAttrIds.size() > 0 && ColUtil.isForm(colSummary.getBodyType())) {
            for (int i = 0; i < formAttrIds.size(); i++) {
                vo = new AttachmentVO();
                try {
                    Attachment attachment = this.attachmentManager.getAttachmentByFileURL(Long.valueOf(formAttrIds.get(i).toString()));
                    createAttachmentVO(vo, attachment);
                    V3XFile file = fileManager.getV3XFile(Long.valueOf(formAttrIds.get(i).toString()));
                    String name = "";
                    if (file != null) {
                        V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
                        name = member.getName();
                    }
                    vo.setUserName(name);
                    //表单控件（来自表单控件的属于正文区域）
                    vo.setFromType(ResourceUtil.getString("collaboration.att.form"));
                    attmentIds.add(attachment.getFileUrl());
                    attachmentVOs.add(vo);
                } catch (Exception e) {
                    LOG.error("获取表单中的附件报错！", e);
                }
            }
        }

        //添加正文区域中的附件
        if (attmentList != null && attmentList.size() > 0) {
            for (int i = 0; i < attmentList.size(); i++) {
                vo = new AttachmentVO();
                Long fileId = Long.parseLong(attmentList.get(i));
                if (fileId != null) {
                    try {
                        Attachment attachment = this.attachmentManager.getAttachmentByFileURL(fileId);
                        createAttachmentVO(vo, attachment);
                        V3XFile file = fileManager.getV3XFile(fileId);
                        V3xOrgMember member = orgManager.getMemberById(file.getCreateMember());
                        String name = member.getName();
                        vo.setUserName(name);
                        //正文区
                        vo.setFromType(ResourceUtil.getString("collaboration.att.form"));
                        attmentIds.add(attachment.getFileUrl());
                        attachmentVOs.add(vo);
                    } catch (Exception e) {
                        LOG.error("获取正文区域中的附件报错！", e);
                    }
                }

            }

        }
        //收藏
        String collectFlag = SystemProperties.getInstance().getProperty("doc.collectFlag");
        if ("true".equals(collectFlag) && AppContext.hasPlugin("doc")) {
            List<Map<String, Long>> collectMap = docApi.findFavorites(AppContext.currentUserId(), attmentIds);
            Map<Long, Long> doc2SourceId = new HashMap<Long, Long>();
            for (Map<String, Long> map : collectMap) {
                doc2SourceId.put(map.get("sourceId"), map.get("id"));
            }
            for (AttachmentVO attachment : attachmentVOs) {
                if (doc2SourceId.get(Long.valueOf(attachment.getFileUrl())) != null) {
                    attachment.setCollect(true);
                }
            }
        }
        //排序
        Collections.sort(attachmentVOs, new AttachmentVO());

        return attachmentVOs;
    }

    /**
     * @param vo
     * @param attachment
     */
    private void createAttachmentVO(AttachmentVO vo, Attachment attachment) {
        vo.setUploadTime(attachment.getCreatedate());
        //转换附件大小显示格式(转为K且省略小数点后位)
        Long size = attachment.getSize().longValue() / 1024 + 1;
        vo.setFileSize(size.toString());
        //附件后缀
        String extension = attachment.getExtension();
        vo.setFileType(extension);
        //附件是否可查看
        if (OfficeTransHelper.isOfficeTran() && OfficeTransHelper.allowTrans(attachment)) {
            vo.setCanLook(true);
        } else {
            vo.setCanLook(false);
        }
        //附件名称去掉后缀
        String fileName = attachment.getFilename();
        if (!StringUtil.checkNull(extension)) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        vo.setFileFullName(fileName);
        vo.setFileName(Strings.getSafeLimitLengthString(fileName, 25, "..."));
        vo.setFileUrl(String.valueOf(attachment.getFileUrl()));
        vo.setV(String.valueOf(attachment.getV()));
        vo.setCreatedate(attachment.getCreatedate());
        vo.setSort(attachment.getSort());
    }

    @Override
    public Map checkTemplateCanUse(String strID) throws BusinessException {
        Map result = new HashMap();
        if (Strings.isNotBlank(strID)) {//模板检查
            Long tId = Long.parseLong(strID);
            User user = AppContext.getCurrentUser();
            boolean outMsg = templateManager.isTemplateEnabled(tId, user.getId());
            if (!outMsg) {
                result.put("flag", "cannot");
                return result;
            }
        }
        result.put("flag", "can");
        return result;
    }

    @AjaxAccess
    public String isTemplateDeleted(String templateId) throws BusinessException {
        if (Strings.isBlank(templateId)) {
            return "false";
        }
        CtpTemplate template = templateManager.getCtpTemplate(Long.valueOf(templateId));
        Boolean isDelete = template.isDelete();
        if (isDelete == null) {
            isDelete = Boolean.FALSE;
        }
        return isDelete ? "1" : "0";
    }

    @Override
    @AjaxAccess
    public Map checkTemplate(Map<String, String> param) throws BusinessException {

        Long templateId = Long.valueOf(param.get("templateId"));
        Long formAppId = Strings.isBlank(param.get("formAppId")) ? null : Long.valueOf(param.get("formAppId"));
        Long formParentId = Strings.isBlank(param.get("formParentId")) ? null : Long.valueOf(param.get("formParentId"));
        Boolean isSystem = Boolean.valueOf(String.valueOf(param.get("isSystem")));
        Map result = new HashMap();

        User user = AppContext.getCurrentUser();
        boolean outMsg = templateManager.isTemplateEnabled(templateId, formAppId, formParentId, user.getId(), isSystem);
        if (!outMsg) {
            result.put("flag", "cannot");
            return result;
        }

        result.put("flag", "can");
        return result;
    }

    @Override
    public String getTemplateId(String tssemplateId) throws BusinessException {
        if (Strings.isNotBlank(tssemplateId)) {
            CtpTemplate template = templateManager.getCtpTemplate(Long.valueOf(tssemplateId));
            User user = AppContext.getCurrentUser();
            boolean outMsg = templateManager.isTemplateEnabled(template, user.getId());
            if (!outMsg) {
                return "{'wflag':'cannot'}";
            }
            if (null != template && !"text".equals(template.getType()) && null != template.getWorkflowId() && !"".equals(template.getWorkflowId().toString())) {
                return "{'wflag':'" + template.getWorkflowId() + "'}";
            } else {//是正文模板
                return "{'wflag':'isTextTemplate'}";
            }
        }
        return "{'wflag':'noworkflow'}";
    }

    public String checkCollTemplate(String tssemplateId) throws BusinessException {
        if (Strings.isNotBlank(tssemplateId) && Strings.isDigits(tssemplateId)) {
            CtpTemplate template = templateManager.getCtpTemplate(Long.valueOf(tssemplateId));
            User user = AppContext.getCurrentUser();
            boolean outMsg = templateManager.isTemplateEnabled(template, user.getId());
            if (!outMsg) {
                return "cannot";
            }
            if (null != template && !"text".equals(template.getType()) && null != template.getWorkflowId() && !"".equals(template.getWorkflowId().toString())) {
                return String.valueOf(template.getWorkflowId());
            } else {//是正文模板
                return "isTextTemplate";
            }
        }
        return "noworkflow";
    }

    @Override
    public String getTrackListByAffairId(TrackAjaxTranObj obj)
            throws BusinessException {
        String affairId = obj.getAffairId();
        List<CtpTrackMember> trackLisByAffairId = trackManager.getTrackMembers(Long.valueOf(affairId));
        String str = "";
        if (trackLisByAffairId.size() > 0) {
            for (int a = 0; a < trackLisByAffairId.size(); a++) {
                str += "Member|" + trackLisByAffairId.get(a).getTrackMemberId() + ",";
            }
            if (str.length() > 0) {
                str = str.substring(0, str.length() - 1);
                return str;
            }
        }
        return str;
    }

    /**
     * 存为草稿：操作者的操作意见被保存在意见框中，包含态度、意见、附件、关联、意见隐藏(2013-12-09 产品经理杨圆确认)；
     * 将处理意见保存为草稿状态
     *
     * @param id
     * @param summary
     */
    public void saveOpinionDraft(Long affairId, Long summaryId) throws BusinessException {
        try {
            Comment comment = new Comment();
            boolean isSave = true;
            ParamUtil.getJsonDomainToBean("comment_deal", comment);
            comment.setCtype(Comment.CommentType.draft.getKey());
            comment.setPushMessage(false);
            Map para = ParamUtil.getJsonDomain("comment_deal");
            if ("1".equals((String) para.get("praiseInput"))) {
                comment.setPraiseToSummary(true);
            }
            if (isSave) {
                Long commentId = comment.getId();
                String draftCommentId = (String) para.get("draftCommentId");
                if (Strings.isNotBlank(draftCommentId)) {
                    commentId = Long.valueOf((String) para.get("draftCommentId"));
                }
                commentManager.deleteComment(ModuleType.collaboration, commentId);
                attachmentManager.deleteByReference(comment.getModuleId(), comment.getId());
                commentManager.insertComment(comment);
            }
        } catch (Exception e) {
            LOG.error("", e);
            throw new BusinessException(e);
        }

    }

    @Override
    public Map checkVouchAudit(Map params) throws BusinessException {
        String summaryId = (String) params.get("summaryId");
        Map<String, Object> result = new HashMap<String, Object>();
        if (summaryId != null) {
            result.put("result", true);
            ColSummary colSummary = getColSummaryById(Long.parseLong(summaryId));
            if (!ColUtil.isForm(colSummary.getBodyType())) {
                result.put("isForm", false);
                return result;
            } else {
                result.put("isForm", true);
            }
            Boolean isAudited = colSummary.isAudited();
            Integer isVouch = colSummary.getVouch();
            result.put("isVouch", isVouch);
            result.put("isAudited", isAudited);

            return result;
        }
        result.put("result", false);
        return result;
    }

    /**
     * 获取当前流程节点的处理说明
     *
     * @param affairId
     * @param templeteId 协同模版Id
     * @param processId  流程ID
     * @return 处理说明String
     */
    public String getDealExplain(Map params) {
        String affairId = (String) params.get("affairId");
        String templeteId = (String) params.get("templeteId");
        String processId = (String) params.get("processId");
        String desc = "";
        if (Strings.isBlank(affairId) || Strings.isBlank(templeteId) || Strings.isBlank(processId)) {
            return desc;
        }
        try {
            CtpAffair affair = affairManager.get(Long.valueOf(affairId));
            BPMProcess process = wapi.getBPMProcessForM1(processId);
            BPMActivity activity = process.getActivityById(affair.getActivityId().toString());
            desc = activity.getDesc();
            desc = desc.replaceAll("\r\n", "<br>").replaceAll("\r", "<br>").replaceAll("\n", "<br>").replaceAll("\\s", "&nbsp;");
        } catch (Exception e) {
            LOG.error("", e);
        }
        return desc;
    }

    @Override
    public void transSetFinishedFlag(ColSummary summary) throws BusinessException {
        this.colDao.transSetFinishedFlag(summary);
    }

    private final Object CheckAndupdateLock = new Object();

    @Override
    public LockObject formAddLock(FormLockParam lockParam) throws BusinessException {
        LockObject obj = new LockObject();
        obj.setCanSubmit(LockObject.CANSUBMIT);  //默认可以提交，避免为null

        if (lockParam != null) {
            /**
             * 1.加签只读不加锁
             * 2.只会节点权限不加锁
             * 3.表单操作权限-操作类型：只读
             * 以上三中情况不加锁
             */
            Long formAppId = lockParam.getFormAppId();
            Long formRecordId = lockParam.getFormRecordId();
            String nodePolicy = lockParam.getNodePolicy();
            Boolean affairReadOnly = lockParam.getAffairReadOnly() == null ? false : lockParam.getAffairReadOnly();  //加签-只读权限
            Integer affairState = lockParam.getAffairState();
            String rightId = lockParam.getRightId();
            Long affairId = lockParam.getAffairId();


            LOG.info("formAppId=" + formAppId + ",nodePolicy=" + nodePolicy + ",affairReadOnly=" + affairReadOnly);
            if (formAppId != null && !"inform".equals(nodePolicy) && !affairReadOnly) {

                StringBuilder paramsLog = new StringBuilder();
                paramsLog.append("Form Lock All Params ：formAppId:").append(formAppId);
                paramsLog.append(",formRecordId:").append(formRecordId);
                paramsLog.append(",nodePolicy:").append(nodePolicy);
                paramsLog.append(",affairReadOnly:").append(affairReadOnly);
                paramsLog.append(",affairState:").append(affairState);
                paramsLog.append(",rightId:").append(rightId);
                paramsLog.append(",affairId:").append(affairId);

                LOG.info("协同表单加锁：" + paramsLog.toString());
                if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affairState)) {

                    boolean isReadOnly = false;
                    if (Strings.isNotBlank(rightId)) {
                        String[] _firstRight = rightId.split("[_]")[0].split("[.]"); //多视图：视图.操作权限|视图.操作权限（第一个编辑，其他只读）
                        String _firstRightId = "";
                        if (_firstRight.length > 1) {
                            _firstRightId = _firstRight[1];
                        } else {
                            _firstRightId = _firstRight[0];
                        }
                        if (NumberUtils.isNumber(_firstRightId)) {
                            isReadOnly = capFormManager.isReadOnlyAuth(formAppId, Long.valueOf(_firstRightId));
                        }
                    }

                    /*if(LOG.isDebugEnabled()){
                    	LOG.debug("表单锁校验 canEdit:"+true+" ID："+affairId+" 参数: formAppID:"+formAppId+",rightId:"+rightId+",isReadOnly:"+isReadOnly);
                    }*/

                    LOG.info("表单锁校验 canEdit:" + true + ",isReadOnly:" + isReadOnly);

                    obj.setIsReadOnly(String.valueOf(isReadOnly));
                    if (!isReadOnly) {
                        Long masterId = formRecordId;
                        Lock lock = capFormManager.getLock(masterId);
                        String from = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
                       /* if(Constants.login_useragent_from.weixin.equals(AppContext.getCurrentUser().getUserAgentFromEnum())){
                            from = "weixin";
                        }*/
                        Long userId = AppContext.currentUserId();
                        boolean isLockTrue = false;
                        boolean hasNullLock = lock == null;
                        if (hasNullLock) {
                            isLockTrue = capFormManager.lockFormData(masterId, AppContext.currentUserId(), from);
                        }
                        if (isLockTrue) {
                            LOG.info(AppContext.currentUserName() + ",加锁成功！！ - lock is null :" + hasNullLock + " ,from=" + from + ",userId= " + userId + ",affairId:" + affairId);
                            obj.setOwner(AppContext.currentUserId());
                            obj.setLoginName(AppContext.currentUserLoginName());
                            obj.setLoginTimestamp(AppContext.getCurrentUser().getLoginTimestamp().getTime());
                            obj.setCanSubmit(LockObject.CANSUBMIT);
                            obj.setRealLockForm("true");
                        } else {
                            obj.setFrom(lock.getFrom());
                            obj.setLoginName(Functions.showMemberName(lock.getOwner()));
                            obj.setOwner(lock.getOwner());
                            obj.setLoginTimestamp(lock.getLoginTime());

                            boolean isOne = lock.getOwner() == AppContext.currentUserId();
                          /*  if(isOne && !from.equals(lock.getFrom())){
                            	LOG.info("SAME AND DIFFERENT,user:"+AppContext.currentUserId()+",form:"+from+",lock.getFrom():"+lock.getFrom()+",affairId:"+affairId);
                            }*/

                            String msg = AppContext.currentUserName() + ",判断锁：isOne=" + isOne + " from=" + from + " lockFrom=" + lock.getFrom() + ",isExsitLockNull：" + hasNullLock;
                            //LOG.info(AppContext.currentUserName()+",判断锁：isOne="+isOne+" from="+from + " lockFrom="+lock.getFrom()+",isExsitLockNull："+isExsitLockNull);

                            if (isOne && from.equals(lock.getFrom())) {
                                obj.setCanSubmit(LockObject.CANSUBMIT);
                                obj.setRealLockForm("true");
                                LOG.info("可以提交！" + msg);
                            } else {
                                obj.setCanSubmit(LockObject.CANNOTSUBMIT);
                                LOG.info("不可以提交 - " + msg + ",affairId:" + affairId + ",lock.owner:" + lock.getOwner() + ",obj.setLoginName：" + obj.getLoginName() + ",lockTime:" + lock.getLockTime());
                            }
                        }
                    }


                }
            }
        }

        return obj;
    }

    @AjaxAccess
    public void activeLockTime(Map<String, String> lockParam) {

        String curUserId = lockParam.get("curUserId");
        String loginPlatform = lockParam.get("loginPlatform");
        String masterId = lockParam.get("formMasterId");
        String affairId = lockParam.get("affairId");
        long _curUserId = Strings.isNotBlank(curUserId) ? new Long(curUserId) : (null != AppContext.getCurrentUser() ? AppContext.getCurrentUser().getId() : 0l);
        /**
         * 获取所有锁 延长时间1分钟 1：表单锁资源ID 是 masterId 2: 流程锁 资源ID 是processId 3：正文锁 TODO
         */
        List<Lock> updates = new ArrayList<Lock>();
        if (Strings.isNotBlank(masterId)) {

            // Lock lock = formManager.getLock(Long.valueOf(masterId));

            List<Lock> formLocks = lockManager.getLocks(Long.valueOf(masterId));
            Lock formLock = null;
            if (formLocks != null && formLocks.size() > 0) {// 被其他在线人员锁住
                formLock = formLocks.get(0);
            }

            if (null != formLock) {
                long owner = formLock.getOwner();
                if (owner == _curUserId && loginPlatform.equals(formLock.getFrom())) {
                    long _curTime = new Timestamp(System.currentTimeMillis()).getTime();
                    formLock.setExpirationTime(_curTime);
                    updates.add(formLock);
                }
            }
        }

        String processId = lockParam.get("processId");
        if (Strings.isNotBlank(processId)) {
            List<Lock> plocks = getWFLockObject(Long.valueOf(processId), String.valueOf(_curUserId));
            if (!Strings.isEmpty(plocks)) {
                for (Lock _plock : plocks) {
                    if (loginPlatform.equals(_plock.getFrom())) {
                        long _curTime = new Timestamp(System.currentTimeMillis()).getTime();
                        _plock.setExpirationTime(_curTime);
                        updates.add(_plock);
                    }
                }
            }
        }


        for (Lock lock : updates) {

            lockManager.updateLockObj(lock.getResourceId(), lock.getAction(), lock.getExpirationTime());

            // 找到提交锁，判断是否需要自动解锁掉

            if (lock.getAction() == 14) {

                long costTime = System.currentTimeMillis() - lock.getLockTime();

                boolean overtime = false;

                if (costTime > Lock.FROM_WXT_OVERTIME) {
                    overtime = true;
                }

                boolean isLock = true;
                if (Strings.isNotBlank(affairId)) {
                    isLock = colLockManager.isLock(Long.valueOf(affairId));
                }

                if (!isLock && overtime) {//能获取到锁（表示当前affair没有处于处理状态） && 超时了。
                    try {
                        LOG.info("activeLockTime 超时解锁失败：userId:" + _curUserId + ",loginPlatform:" + loginPlatform + ",processId：" + processId + ",affairId:" + affairId);
                        wapi.releaseWorkFlowProcessLock(processId, String.valueOf(_curUserId), loginPlatform);
                    } catch (BPMException e) {
                        LOG.info("activeLockTime 超时解锁失败：userId:" + _curUserId + ",loginPlatform:" + loginPlatform + ",processId：" + processId);
                    }
                }
            }

        }
    }

    private List<Lock> getWFLockObject(Long processId, String userId) {
        List<Lock> mylocks = new ArrayList<Lock>();
        List<Lock> locks = lockManager.getLocks(processId);
        if (locks != null && !locks.isEmpty()) {
            for (Lock lk : locks) {
                if (lk != null && lk.getOwner() == Long.parseLong(userId)) {
                    if (LockState.effective_lock.equals(lockManager.isValid(lk))) {
                        mylocks.add(lk);
                    }
                }
            }
        }
        return mylocks;
    }


    @Override
    public void colDelLock(Long affairId) throws BusinessException {
        CtpAffair affair = affairManager.get(affairId);
        //待发列表预览，删除，这个时候物理删除以后，展现页面解锁。查找不到affair了。
        if (affair == null) {
            LOG.info("协同解锁，获取affair为null,可能有问题");
            return;
        }
        Long summaryId = affair.getObjectId();
        ColSummary summary = getSummaryById(summaryId);
        colDelLock(summary, affair);
    }

    @Override
    public void colDelLock(Long affairId, boolean delAll) throws BusinessException {
        if (delAll) {
            CtpAffair affair = affairManager.get(affairId);
            //待发列表预览，删除，这个时候物理删除以后，展现页面解锁。查找不到affair了。
            if (affair == null) {
                LOG.info("协同解锁，获取affair为null,可能有问题");
                return;
            }
            Long summaryId = affair.getObjectId();
            ColSummary summary = getSummaryById(summaryId);
            colDelLock(summary, affair, delAll);
        } else {
            colDelLock(affairId);
        }

    }


    public void colDelLock(ColSummary summary, CtpAffair _affair) throws BusinessException {
        Map<String, String> param = new HashMap<String, String>();

        param.put("summaryId", String.valueOf(summary.getId()));
        param.put("processId", summary.getProcessId());
        param.put("formAppId", summary.getFormAppid() == null ? null : String.valueOf(summary.getFormAppid()));
        param.put("fromRecordId", summary.getFormRecordid() == null ? null : String.valueOf(summary.getFormRecordid()));
        param.put("bodyType", summary.getBodyType());

        ajaxColDelLock(param);
    }

    public void colDelLock(ColSummary summary, CtpAffair _affair, boolean delAll) throws BusinessException {
        if (delAll) {
            Map<String, String> param = new HashMap<String, String>();

            param.put("summaryId", String.valueOf(summary.getId()));
            param.put("processId", summary.getProcessId());
            param.put("formAppId", summary.getFormAppid() == null ? null : String.valueOf(summary.getFormAppid()));
            param.put("fromRecordId", summary.getFormRecordid() == null ? null : String.valueOf(summary.getFormRecordid()));
            param.put("bodyType", summary.getBodyType());
            param.put("delAll", "true");
            ajaxColDelLock(param);
        } else {
            colDelLock(summary, _affair);
        }

    }

    @AjaxAccess
    public void unlock4NewCol(Map<String, String> params) throws BusinessException {

        String masterDataId = params.get("masterDataId");
        if (Strings.isNotEmpty(masterDataId)) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("masterDataId", masterDataId);
            formManager.removeSessionMasterData(m);
        }

        String handWriteKeys = params.get("handWriteKeys");
        if (Strings.isNotEmpty(handWriteKeys)) {
            String[] names = handWriteKeys.split("[,]");
            for (String name : names) {
                handWriteManager.deleteUpdateObj(name);
            }
        }
    }


    @Override
    @AjaxAccess
    public void onDealPageLeave(Map<String, Map<String, String>> params) throws BusinessException {

        //解锁参数
        Map<String, String> delLockMap = params.get("DelLock");
        if (delLockMap != null && delLockMap.size() > 0) {
            try {
                ajaxColDelLock(delLockMap);
            } catch (Exception e) {
                LOG.error("解锁失败", e);
            }
        }

        //记录操作时间
        Map<String, String> viewRecordMap = params.get("ViewRecord");
        if (viewRecordMap != null && viewRecordMap.size() > 0) {
            try {
                colBatchUpdateAnalysisTimeManager.addTask(viewRecordMap);
            } catch (Exception e) {
                LOG.error("记录第一次关闭窗口时间异常", e);
            }
        }
    }

    @Override
    public void ajaxColDelLock(Map<String, String> param) throws BusinessException {

        Long summaryId = Long.valueOf(param.get("summaryId"));
        String processId = Strings.isBlank(param.get("processId")) ? "" : param.get("processId");
        Long formAppId = Strings.isBlank(param.get("formAppId")) ? 0l : Long.valueOf(param.get("formAppId"));
        Long fromRecordId = Strings.isBlank(param.get("fromRecordId")) ? 0l : Long.valueOf(param.get("fromRecordId"));
        String bodyType = param.get("bodyType");
        if (summaryId == null) {
            LOG.info("协同解锁，获取summary为null,可能有问题");
            return;
        }

        // 解除流程锁
        try {

            String loginFrom = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
            String userId = String.valueOf(AppContext.currentUserId());
            boolean isForm = ColUtil.isForm(bodyType);

            StringBuilder unlockLog = new StringBuilder();
            unlockLog.append(AppContext.currentUserName() + "," + AppContext.currentUserLoginName())
                    .append("-协同解锁-ajaxColDelLock-loginFrom:").append(loginFrom)
                    .append(",processId:").append(processId)
                    .append(",userId:").append(userId)
                    .append(",isForm:").append(isForm)
                    .append(",formAppId:").append(formAppId)
                    .append(",summaryId:").append(summaryId)
                    .append(",fromRecordId:").append(fromRecordId);

            LOG.info(unlockLog.toString());

            wapi.releaseWorkFlowProcessLock(processId, userId, loginFrom);
            wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), userId);

            // 解除表单锁
            if (isForm && formAppId != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("AjaxColDelLock协同页面离开，解锁表单锁：summaryid:" + summaryId + ",fromRecordId:" + fromRecordId);
                }
                formManager.removeSessionMasterDataBean(fromRecordId);
            }
            boolean isHtml = String.valueOf(MainbodyType.HTML.getKey()).equals(bodyType);
            //office正文
            if (!isHtml && !isForm) {
                List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summaryId);
                List<String> fileIds = new ArrayList<String>();
                for (CtpContentAll content : contents) {
                    if (null != content.getContentDataId()) {
                        fileIds.add(content.getContentDataId().toString());
                    }
                }
                handWriteManager.deleteUpdateObjs(Strings.join(fileIds, ","));
            }
        } catch (Throwable e) {
            LOG.error("协同解锁失败colDelLock", e);
            throw new BusinessException(e);
        }
    }

    @Override
    public boolean updateAppointStepBack(Map<String, Object> c) throws BusinessException {
        String summaryId = (String) c.get("summaryId");
        String workitemId = (String) c.get("workitemId");
        String caseId = (String) c.get("caseId");
        String processId = (String) c.get("processId");
        String currentUserId = String.valueOf(AppContext.currentUserId());
        String currentAccountId = String.valueOf(AppContext.currentAccountId());
        String selectTargetNodeId = (String) c.get("theStepBackNodeId");
        String submitStyle = (String) c.get("submitStyle");
        String currentActivityId = (String) c.get("activityId");
        String currentAffairId = (String) c.get("affairId");
        String isWfTrace = (String) c.get("isWFTrace");
        String isCircleBack = (String) c.get("isCircleBack");


        //ColSummary summary = (ColSummary)c.get("summary");
        //保证事物，要不然会存在同事务，两个对象的错误
        ColSummary summary = getSummaryById(Long.valueOf(summaryId));

        CtpAffair currentAffair = (CtpAffair) c.get("affair");
        Comment comment = (Comment) c.get("comment");
        User user = (User) c.get("user");


        // summary不用做操作

        List<CtpAffair> allAvailableAffairs = affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, Long.valueOf(summaryId));

        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        saveOrUpdateComment(comment);

        //更行附件和正文, 顺序很关键， 不要放到下面
        saveAttDatas(user, summary, currentAffair, comment.getId());

        AffairData affairData = ColUtil.getAffairData(summary);

        // 工作流回退
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentWorkitemId(Long.parseLong(workitemId));
        context.setCaseId(Long.parseLong(caseId));
        context.setProcessId(processId);
        context.setCurrentUserId(currentUserId);
        context.setCurrentAccountId(currentAccountId);
        context.setCurrentActivityId(currentActivityId);
        context.setSelectTargetNodeId(selectTargetNodeId);
        context.setSubmitStyleAfterStepBack(submitStyle);
        context.setAppObject(summary);
        context.setBussinessId(String.valueOf(summary.getId()));
        context.setBusinessData(WorkFlowEventListener.COLSUMMARY_CONSTANT, summary);
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_SUMMARY_ID, summary.getId());
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_MEMBER_ID, currentAffair.getMemberId());
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_COMMENT_ID, comment.getId());
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_AFFAIR_ID, Long.valueOf(currentAffairId));
        context.setBusinessData("operationType", "1".equals(submitStyle) ? WorkFlowEventListener.SPECIAL_BACK_SUBMITTO : WorkFlowEventListener.SPECIAL_BACK_RERUN);
        context.setBusinessData("isCircleBack", isCircleBack);
        context.setVersion("2.0");
        context.setBusinessData(WorkFlowEventListener.CURRENT_OPERATE_TRACK_FLOW, isWfTrace);
        String[] retValue = wapi.stepBack(context);
        Map<String, Object> businessData = context.getBusinessData();

        String selectTargetNodeName = retValue[2];
        V3xOrgMember member = null;
        boolean isRepeal = false;
        int backStrategy = 169;//169:直接提交给我,168:流程重走
        if ("1".equals(submitStyle)) {
            backStrategy = 168;
        }
        String describe = "appLog.action." + backStrategy;
        if ("start".equals(selectTargetNodeId)) {
            // 直接提交给我
            if ("1".equals(submitStyle)) {
                // 把发起人的状态改为(是代办还是草稿箱什么的)
                CtpAffair affair = affairManager.getSenderAffair(Long.parseLong(summaryId));
                member = orgManager.getMemberById(affair.getMemberId());
                affair.setSubState(SubStateEnum.col_pending_specialBacked.getKey());
                affair.setState(StateEnum.col_waitSend.getKey());
                affair.setUpdateDate(new java.util.Date());
                affair.setDelete(Boolean.FALSE);
                affair.setPreApprover(user.getId());
                checkFormSubject(summary, affair, c);
                this.affairManager.updateAffair(affair);
                // 流程重走
            } else if ("0".equals(submitStyle)) {
                isRepeal = true;
                // 将summary的状态改为待发,撤销已生成事项
                CtpAffair sendAffair = null;
                if (allAvailableAffairs != null) {
                    for (int i = 0; i < allAvailableAffairs.size(); i++) {
                        CtpAffair affair = (CtpAffair) allAvailableAffairs.get(i);
                        if (affair.getState().intValue() == StateEnum.col_sent.key()
                                || affair.getState().intValue() == StateEnum.col_waitSend.key()) {
                            affair.setState(StateEnum.col_waitSend.key());
                            affair.setSubState(SubStateEnum.col_pending_specialBackToSenderCancel.key());
                            affair.setDelete(false);
                            affair.setPreApprover(user.getId());
                            affairManager.updateAffair(affair);
                            member = orgManager.getMemberById(affair.getMemberId());
                            sendAffair = affair;
                        }
                        if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                            QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                            QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
                        }
                        if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                            QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId()); //保证老数据不报错, 6.0后面的版本把这个删除
                            QuartzHolder.deleteQuartzJob("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
                        }
                        checkFormSubject(summary, affair, c);
                        //指定回退(回退节点删除多次消息提醒任务)
                        ColUtil.deleteCycleRemindQuartzJob(affair, false);
                    }
                }
                summary.setCaseId(null);
                summary.setState(CollaborationEnum.flowState.cancel.ordinal());
                updateColSummary(summary);

                WorkflowTraceEnums.workflowTrackType trackType = WorkflowTraceEnums.workflowTrackType.special_step_back_repeal;
                if ("1".equals(isCircleBack)) {
                    trackType = WorkflowTraceEnums.workflowTrackType.circle_step_back_repeal;
                }
                //创建流程追述的数据
                createRepealData2BeginNode(currentAffair, summary, allAvailableAffairs, trackType, isWfTrace);

                //删除ISIgnatureHTML专业签章
                iSignatureHtmlManager.deleteAllByDocumentId(summary.getId());

                //调用表单万能方法,更新状态，触发子流程等
                if (ColUtil.isForm(summary.getBodyType())) {
                    try {
                        capFormManager.RelationAuthorityBySummaryId(summary.getId(), ModuleType.collaboration.getKey());//撤销时删除表单授权信息
                        if (null != sendAffair) {//清楚表单授权信息
                            sendAffair.setSummaryState(summary.getState());
                            AffairUtil.setIsRelationAuthority(sendAffair, false);
                            DBAgent.update(sendAffair);
                        }
                    } catch (Exception e) {
                        LOG.error("更新表单相关信息异常", e);
                    }
                }
            }

            // 记录流程日志
            processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(processId),
                    Long.parseLong(currentActivityId), ProcessLogAction.colStepBackToSender, comment.getId(), member == null ? "" : member.getName(), describe, summary.getSubject(), selectTargetNodeName, "true");

        } else {

            checkFormSubject(summary, currentAffair, c);
            processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(processId), Long
                    .parseLong(currentActivityId), ProcessLogAction.colStepBackToPoint, comment.getId(), "", describe, summary.getSubject(), selectTargetNodeName, "true");
        }
        if ("1".equals(submitStyle)) {
            ColUtil.updateCurrentNodesInfo(summary, Long.valueOf(currentAffairId), "start".equals(selectTargetNodeId));
        } else {
            ColUtil.updateCurrentNodesInfo(summary);
        }

        if (!"0".equals(submitStyle)) {
            ColUtil.addOneReplyCounts(summary);
        }
        updateColSummary(summary);//更新一下当前待办人信息


        //更新全文检索
        if (AppContext.hasPlugin("index")) {
            if (ColUtil.isForm(summary.getBodyType())) {
                indexManager.update(summary.getId(), ApplicationCategoryEnum.form.getKey());
            } else {
                indexManager.update(summary.getId(), ApplicationCategoryEnum.collaboration.getKey());
            }
        }

        // 调用表单万能方法,更新状态，触发子流程等
        if (ColUtil.isForm(summary.getBodyType())) {
            try {
                AppContext.putThreadContext("isRepeal_4_form_use", "start".equals(selectTargetNodeId));
                List<Comment> commentList = this.commentManager.getCommentAllByModuleId(ModuleType.collaboration, summary.getId());
                capFormManager.updateDataState(summary, currentAffair, ColHandleType.specialback, commentList);
                AppContext.removeThreadContext("isRepeal_4_form_use");
            } catch (Exception e) {
                LOG.error("更新表单相关信息异常", e);
            }
        }

        //记录应用日志
        if ("1".equals(submitStyle)) {
            //直接提交给我--168
            appLogManager.insertLog(user, 168, user.getName(), summary.getSubject(), selectTargetNodeName);
        } else if ("0".equals(submitStyle)) {
            //流程重走--169
            appLogManager.insertLog(user, 169, user.getName(), summary.getSubject(), selectTargetNodeName);
        }
        // 暂存待办，记录第一次操作时间
        affairManager.updateAffairAnalyzeData(currentAffair);

        // 指定回退事件
        CollaborationAppointStepBackEvent backEvent = new CollaborationAppointStepBackEvent(this);
        backEvent.setSelectTargetNodeId(selectTargetNodeId);
        backEvent.setBodyType(summary.getBodyType());
        backEvent.setSenderId(summary.getStartMemberId());
        backEvent.setUserId(user.getId());
        backEvent.setTemplateId(summary.getTempleteId());
        backEvent.setSummaryId(summary.getId());
        EventDispatcher.fireEventAfterCommit(backEvent);

        // 发送消息
        colMessageManager.appointStepBackSendMsg(summary, allAvailableAffairs, submitStyle, selectTargetNodeId,
                selectTargetNodeName, currentAffair, comment, businessData);
        return true;
    }

    @Override
    public String getContentComponentType(String moduleId) {
        List<CtpContentAll> list = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, Long.valueOf(moduleId));
        return list.get(0).getContentType().toString();
    }

    public String colCheckAndupdateLock(String processId, Long summaryId, boolean isLock) throws BusinessException {
        //TODO
    	/*CtpAffair senderAffair = this.affairManager.getSenderAffair(summaryId);
    	if(senderAffair == null || senderAffair.getState().intValue() == StateEnum.col_waitSend.key()){
    		return "--NoSuchSummary--";
    	}

    	User user = AppContext.getCurrentUser();
    	String modifyUserName = null;
    	synchronized (CheckAndupdateLock) {
	    	modifyUserName = getModifyUserName(processId, summaryId);
	    	if(modifyUserName == null && isLock){
	    		//加锁
    			ColHelper.updateProcessLock(processId, user.getId()+"");
	    	}

    	}
    	return modifyUserName;*/
        return null;
    }

    private String getModifyUserName(String processId, Long summaryId) throws BusinessException {
        //TODO
    	/*User user = AppContext.getCurrentUser();
    	String modifyUserName = null;
    	if(user == null){
    		return modifyUserName;
    	}
    	String userId = user.getId() + "";
		try {
			String modifyUserId = ColHelper.isModifyProcess(processId, userId, orgManager);
			if(modifyUserId != null && !"".equals(modifyUserId)){
				V3xOrgAccount account = orgManager.getRootAccount();
				V3xOrgMember member = orgManager.getMemberById(Long.parseLong(modifyUserId));
				if(member.getOrgAccountId().equals(account.getId())){
					modifyUserName = Constant.getCommonString("group.name");
				}else{
					modifyUserName = orgManager.getMemberById(Long.parseLong(modifyUserId)).getName();
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return modifyUserName;*/
        return null;
    }

    @Override
    public FlipInfo getStatisticSearchCols(FlipInfo flipInfo,
                                           Map<String, String> query) throws BusinessException {
        User user = AppContext.getCurrentUser();
        List<ColSummaryVO> result = null;
        //已归档时
        boolean setTimeNull = false;
        String stateStr = query.get("state");
        if ("archived".equals(query.get(ColQueryCondition.archiveId.name()))) {
            result = this.getArchiveAffair(flipInfo, query);
        } else {
            result = colDao.queryByCondition(flipInfo, query);
            if (Strings.isNotBlank(stateStr) && ("3,4".equals(stateStr) || "3".equals(stateStr))) {
                setTimeNull = true;
            }
        }

        for (ColSummaryVO csvo : result) {
            ColSummary summary = csvo.getSummary();
            CtpAffair caffair = csvo.getAffair();
            if (caffair != null && null != caffair.getSubState()) {
                int affairState = caffair.getSubState().intValue();
                if (setTimeNull && (SubStateEnum.col_pending_specialBacked.getKey() == affairState || SubStateEnum.col_pending_specialBackCenter.getKey() == affairState
                        || SubStateEnum.col_pending_specialBackToSenderCancel.getKey() == affairState)) {
                    csvo.setDealTime(null);
                }
            }

            Long accountId = ColUtil.getFlowPermAccountId(user.getLoginAccount(), summary);
            String nodeName = csvo.getNodePolicy();
            Map<String, Permission> permissonMap = getPermissonMap(EnumNameEnum.col_flow_perm_policy.name(), accountId);
            Permission permisson = permissonMap.get(nodeName);
            if (permisson != null) {
                NodePolicy nodePolicy = permisson.getNodePolicy();
                Integer opinion = nodePolicy.getOpinionPolicy();
                boolean canDeleteORarchive = (opinion != null && opinion.intValue() == 1);
                csvo.setCanDeleteORarchive(canDeleteORarchive);
                csvo.setCancelOpinionPolicy(nodePolicy.getCancelOpinionPolicy());
                csvo.setDisAgreeOpinionPolicy(nodePolicy.getDisAgreeOpinionPolicy());
                csvo.setCanDeleteORarchive(false);
            } else {
                csvo.setCanDeleteORarchive(true);
            }
        }
        if (flipInfo != null) {
            flipInfo.setData(result);
        }
        return flipInfo;
    }

    @Override
    public Map checkIsCanRepeal(Map params) throws BusinessException {
        String _summaryId = (String) params.get("summaryId");
        Map<String, String> map = new HashMap<String, String>();
        if (Strings.isBlank(_summaryId)) {
            map.put("msg", "程序或者数据异常，不可以撤销！");
            return map;
        }
        Long summaryId = Long.parseLong(_summaryId);
        ColSummary colSummary = getColSummaryById(summaryId);
        if (colSummary == null) {
            map.put("msg", "程序或者数据异常，不可以撤销！");
            return map;
        }
        //判断流程是否结束，结束后不能撤销
        if (CollaborationEnum.flowState.terminate.ordinal() == colSummary.getState().intValue()
                || CollaborationEnum.flowState.finish.ordinal() == colSummary.getState().intValue()) {
            map.put("msg", ResourceUtil.getString("collaboration.cannotRepeal_workflowIsFinished"));
            return map;
        }
        //判断是否核定，如果核定则不能撤销
        if (getSummaryHasVouch(colSummary)) {
            map.put("msg", ResourceUtil.getString("collaboration.cannotRepeal_workflowIsVouched"));
            return map;
        }
        //有审核(表单审核、新闻审核、公告审核) 不能撤销
        if (getSummaryHasAudit(colSummary)) {
            map.put("msg", ResourceUtil.getString("collaboration.cannotRepeal_workflowIsAudited"));
            return map;
        }
        return map;
    }

    @Override
    public boolean getSummaryHasVouch(ColSummary summary) throws BusinessException {
        if (summary != null && summary.getVouch() != null &&
                ColConstant.ColSummaryVouch.vouchPass.getKey() == summary.getVouch().intValue()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean getSummaryHasAudit(ColSummary summary) throws BusinessException {
        boolean hasAudit = false;
        if (summary == null) {
            return hasAudit;
        }
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("objectId", summary.getId());
        conditions.put("state", StateEnum.col_done.key());
        conditions.put("delete", false);
        List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
        if (affairList != null && !affairList.isEmpty()) {
            for (CtpAffair affair : affairList) {
                if (affair.getActivityId() != null) {
                    SeeyonPolicy seeyonPolicy = ColUtil.getPolicyByAffair(affair);
                    if (cannotRepealList.contains(seeyonPolicy.getId())) {
                        hasAudit = true;
                        break;
                    }
                }
            }
        }
        return hasAudit;
    }

    /**
     * @param userMessageManager the userMessageManager to set
     */
    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public int recallNewflowSummary(Long caseId, User user, String operationType) throws BusinessException {
        int result = 0;
        if (user == null) {
            return result;
        }
        ColSummary summary = getSummaryByCaseId(caseId);
        if (summary == null) {
            return 1;
        }
        //将summary的状态改为待发,撤销已生成事项
        Map map = new HashMap();
        map.put("objectId", summary.getId());
        List<CtpAffair> affairs = affairManager.getValidAffairs(null, map);
        //撤销流程
        List<Long> archiveAffairIds = new ArrayList<Long>();
        if (affairs != null) {
            for (int i = 0; i < affairs.size(); i++) {
                CtpAffair affair = (CtpAffair) affairs.get(i);
                if (affair.getState().intValue() == StateEnum.col_sent.key()) {
                    affair.setState(StateEnum.col_waitSend.key());
                    affair.setSubState(SubStateEnum.col_waitSend_cancel.key());
                    affair.setDelete(true);
                    affairManager.updateAffair(affair);
                }

                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getObjectId() + "_" + affair.getActivityId());
                }

                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getObjectId() + "_" + affair.getActivityId());
                }

                //指定回退(回退节点删除多次消息提醒任务)
                ColUtil.deleteCycleRemindQuartzJob(affair, false);
                if (affair.getArchiveId() != null) {
                    archiveAffairIds.add(affair.getId());
                }
            }

            this.affairManager.updateAffairsState2Cancel(summary.getId());
        }

        summary.setCaseId(null);
        DBAgent.update(summary);

        ColUtil.deleteQuartzJobOfSummary(summary);

        //删除子流程的预归档
        if (AppContext.hasPlugin("doc") && Strings.isNotEmpty(archiveAffairIds)) {
            docApi.deleteDocResources(user.getId(), archiveAffairIds);
        }

        String key = "col.newflow.callback";
        String operation = ResourceUtil.getString("collaboration.appointStepBack.ProcessTheHeavy");
        //更明确的消息提示
        if ("takeBack".equals(operationType)) {
            operation = ResourceUtil.getString("collaboration.takeBack.label");
        } else if ("stepBack".equals(operationType)) {
            operation = ResourceUtil.getString("collaboration.stepBack.label");
        } else if ("repeal".equals(operationType)) {
            operation = ResourceUtil.getString("collaboration.repeal.2.label");
        }
        CtpSuperviseDetail detail = this.superviseManager.getSupervise(SuperviseEnum.EntityType.template.ordinal(), summary.getTempleteId());
        if (null != detail) {
            //发送消息 - 给被撤销的督办人
            this.colMessageManager.sendMessage2Supervisor(detail.getId(), ApplicationCategoryEnum.collaboration, summary.getSubject(), key, user.getId(), user.getName(), operation, summary.getForwardMember(), summary.getTempleteId());
        }
        //删除督办信息
        this.superviseManager.updateStatus2Cancel(summary.getId());

        //对发起人以外的所有执行人发消息通知
        try {
            Integer importantLevel = summary.getImportantLevel();
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            if (affairs != null && affairs.size() > 0) {
                for (CtpAffair affair1 : affairs) {
                    if (affair1.isDelete()) {
                        continue;
                    }
                    if (user.getId().equals(affair1.getMemberId())) {
                        continue;
                    }
                    MessageReceiver receiver = new MessageReceiver(affair1.getId(), affair1.getMemberId());
                    if (null != affair1.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair1.getTrack())) {
                        receiver.setTrack(true);
                    }
                    receivers.add(receiver);
                }
                MessageContent content = new MessageContent(key, summary.getSubject(), user.getName(), operation).setImportantLevel(importantLevel);
                if (null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, ColUtil.getImportantLevel(summary));
            }
        } catch (Exception e) {
            LOG.error("召回新流程协同发送提醒消息异常", e);
            throw new BusinessException("send message failed");
        }

        processLogManager.deleteLog(Long.parseLong(summary.getProcessId()));
        //流程撤销事件通知
        CollaborationCancelEvent cancelEvent = new CollaborationCancelEvent(this);
        cancelEvent.setSummaryId(summary.getId());
        cancelEvent.setUserId(user.getId());
        cancelEvent.setSenderId(summary.getStartMemberId());
        cancelEvent.setTemplateId(summary.getTempleteId());
        cancelEvent.setBodyType(summary.getBodyType());
        EventDispatcher.fireEvent(cancelEvent);

        return 0;
    }

    public boolean isTemplateHasPrePigholePath(Long templateId) throws BusinessException {
        if (templateId == null) {
            return false;
        }
        CtpTemplate templete = templateManager.getCtpTemplate(templateId);
        if (templete != null) {
            ColSummary summary = (ColSummary) XMLCoder.decoder(templete.getSummary());
            if (summary != null) {
                Long archiveId = summary.getArchiveId();
                if (archiveId != null)
                    return true;

                String advancePigeonhole = summary.getAdvancePigeonhole();

                if (Strings.isNotBlank(advancePigeonhole)) {

                    JSONObject jo;
                    try {
                        jo = new JSONObject(advancePigeonhole);
                        String archiveTextName = String.valueOf(jo.get("archiveTextName")).trim();
                        String archiveText = String.valueOf(jo.get("archiveText")).trim();
                        String archiveKeyword = String.valueOf(jo.get("archiveKeyword")).trim();
                        if (StringUtil.checkNull(archiveTextName) && StringUtil.checkNull(archiveText) && StringUtil.checkNull(archiveKeyword)) {
                            return false;
                        }
                    } catch (JSONException e) {
                        LOG.info(e.getLocalizedMessage());
                        return false;
                    }

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前事项的所有memberId
     *
     * @param String
     * @return
     */
    public List<Long> getColAllMemberId(String summaryId) {
        List<Long> memberIdList = new ArrayList<Long>();
        //添加已办和待办的。
        List<StateEnum> states = new ArrayList<StateEnum>();
        states.add(StateEnum.col_pending);
        states.add(StateEnum.col_done);
        states.add(StateEnum.col_sent);
        states.add(StateEnum.col_waitSend);
        try {
            List<CtpAffair> ctpAffair = this.affairManager.getAffairs(Long.valueOf(summaryId), states);
            for (int i = 0; i < ctpAffair.size(); i++) {
                if (!memberIdList.contains(ctpAffair.get(i).getMemberId())) {
                    memberIdList.add(ctpAffair.get(i).getMemberId());
                }
            }
        } catch (Exception e) {
            LOG.error("获取当前事项的所有memberId异常", e);
        }
        return memberIdList;

    }

    /**
     * 回复意见 及发起人附言的时候走这个方法，前台Ajax请求
     */
    public Comment insertComment(Comment comment, String openFrom) throws BusinessException {
        Long affairId = comment.getAffairId();
        User user = AppContext.getCurrentUser();
        CtpAffair affair = affairManager.get(affairId);
        if (affair != null) {
            if ("supervise".equals(openFrom)) {
                comment.setCreateId(user.getId());
            } else {
                List<Long> ownerIds = MemberAgentBean.getInstance().getAgentToMemberId(ModuleType.collaboration.getKey(), user.getId());
                boolean isProxy = false;
                if (Strings.isNotEmpty(ownerIds) && ownerIds.contains(affair.getMemberId())) {
                    isProxy = true;
                }
                if (affair != null && !affair.getMemberId().equals(user.getId()) && isProxy) {
                    comment.setExtAtt2(user.getName());
                    comment.setCreateId(affair.getMemberId());
                } else {
                    comment.setCreateId(user.getId());
                    comment.setExtAtt2(null);
                }
            }
        } else {
            comment.setCreateId(user.getId());
        }


        Comment c = commentManager.insertComment(comment);


        //发送评论/回复的消息
        sendMsg(c);


        if (c.getCreateId() != null) {
            c.setCreateName(OrgHelper.showMemberNameOnly(c.getCreateId()));
        }

        CollaborationAddCommentEvent commentEvent = new CollaborationAddCommentEvent(this);
        commentEvent.setCommentId(c.getId());
        EventDispatcher.fireEvent(commentEvent);

        //全文检索库 入库
        try {
            if (AppContext.hasPlugin("index")) {
                if (affairId != null) {
                    if (Integer.valueOf(ModuleType.collaboration.getKey()).equals(comment.getModuleType())) {
                        CtpAffair ctpAffair = affairManager.get(affairId);
                        if (ctpAffair != null) {//添加回复更新全文检索库
                            if (ColUtil.isForm(ctpAffair.getBodyType())) {
                                indexManager.update(ctpAffair.getObjectId(), ApplicationCategoryEnum.form.getKey());
                            } else {
                                indexManager.update(ctpAffair.getObjectId(), ApplicationCategoryEnum.collaboration.getKey());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("全文检索异常", e);
        }
        return c;
    }

    private void sendMsg(Comment comment) throws BusinessException {

        boolean ispush = comment.isPushMessage() != null && comment.isPushMessage();

        // 回复意见的时候不管是否勾选了发送消息，需要给跟踪人发送消息
        if (comment.getCtype() == Comment.CommentType.reply.getKey() && !ispush) {
            ispush = true;
            comment.setPushMessageToMembers("");
        }
        // 评论回复消息推送

        if (ispush) {
            ContentConfig cc = ContentConfig.getConfig(ModuleType.getEnumByKey(comment.getModuleType()));

            ContentInterface ci = cc.getContentInterface();
            // 包含了发起人附言和回复的消息
            if (ci != null) {
                ci.doCommentPushMessage(comment);
            }
        }
    }


    @Override
    public Map checkTemplateCanModifyProcess(String templateId)
            throws BusinessException {
        Map resMap = new HashMap();
        if (!AppContext.getCurrentUser().isV5Member()) {//V-Join人员不允许设置督办
            resMap.put("canSetSupervise", "no");
        }
        if (!Strings.isNotBlank(templateId)) {
            resMap.put("canModify", "yes");
            return resMap;
        }
        CtpTemplate ctpTemplate = templateManager.getCtpTemplate(Long.valueOf(templateId));
        if (null == ctpTemplate) {
            resMap.put("canModify", "yes");
            return resMap;
        } else {
            String summary = ctpTemplate.getSummary();
            Boolean canSupervise = ctpTemplate.getCanSupervise();
            if (!(null != canSupervise && canSupervise)) {
                resMap.put("canSetSupervise", "no");
            }
            ColSummary sum = (ColSummary) XMLCoder.decoder(summary);
            Boolean canModify = sum.getCanModify();
            if (canModify) {
                resMap.put("canModify", "yes");
                return resMap;
            } else {
                resMap.put("canModify", "no");
                return resMap;
            }
        }
    }

    /**
     * 绩效报表穿透查询列表导出excel
     *
     * @param flipInfo
     * @param query
     * @return
     * @throws BusinessException
     */
    public List<ColSummaryVO> exportDetaileExcel(FlipInfo flipInfo, Map<String, String> query) throws BusinessException {
        List<ColSummaryVO> colSumList = new ArrayList<ColSummaryVO>();
        colSumList = this.colDao.queryByCondition(flipInfo, query);
        return colSumList;
    }

    @Override
    public List<ColSummaryVO> getArchiveAffair(FlipInfo flipInfo,
                                               Map<String, String> query) throws BusinessException {
        return this.colDao.getArchiveAffair(flipInfo, query);
    }

    /**
     * 表单转协同的时候获取表单控件中的附件并复制
     *
     * @param fromModuleType
     * @param oldsummaryId
     * @param toModuleType
     * @param toModuleId
     * @throws BusinessException
     */
    private void transForwardBody(Long summaryId, Long moduleId, Long rightId) throws BusinessException {
        List<Attachment> newAtts = new ArrayList<Attachment>();
        FormService formService = new FormService();
        Set<Attachment> atts = formService.getFormAttsByAuth(moduleId, rightId);

        if (atts != null) {
            for (Attachment att : atts) {
                Attachment newAtt = null;
                try {
                    newAtt = (Attachment) att.clone();
                    newAtt.setNewId();
                    if (att.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE.ordinal()
                            || att.getType() == com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FormFILE
                            .ordinal()) {
                        // Long newFileId = UUIDLong.longUUID();
                        // this.fileManager.clone(att.getFileUrl(), att.getCreatedate(), newFileId, now);
                        newAtt.setFileUrl(att.getFileUrl());
                    }
                } catch (Exception e) {
                    LOG.warn("", e);
                    continue;
                }
                newAtt.setReference(summaryId);
                newAtt.setSubReference(100L); //表单中的附件
                newAtts.add(newAtt);
            }
            this.attachmentManager.create(newAtts);
        }
    }

    /**
     * 根据当前流程的summaryId来获取主流程的summaryId
     */
    @Override
    public Long getParentProceeObjectId(Long id) throws BusinessException {
        ColSummary summary = getSummaryById(id);
        if (null == summary) {
            return id;
        }
        String processId = summary.getProcessId();
        if (Strings.isBlank(processId)) {
            return id;
        }
        Long parentProcessId = wapi.getMainProcessIdBySubProcessId(Long.valueOf(processId));
        if (null == parentProcessId) {
            return id;
        }
        ColSummary colSummaryById = getColSummaryByProcessId(parentProcessId);
        if (null != colSummaryById) {
            return colSummaryById.getId();
        }
        return id;
    }

    public List<ColSummary> findColSummarysByIds(List<Long> ids) throws BusinessException {
        return colDao.findColSummarysByIds(ids);
    }

    @Override
    public Map<Long, ColSummary> getColAndEdocSummaryMap(List<Long> collIdList) throws BusinessException {
        //Map<String,Map> map = new HashMap<String,Map>();
        //TODO DEV 2015-08-19 注释掉用于首页更多显示当前待办人功能 根据协同和公文的id获得summary对象Map数据
		/*Map<Long,SimpleEdocSummary> edocSummaryMap = new HashMap<Long,SimpleEdocSummary>();
        if(Strings.isNotEmpty(edocIdList)){
        	EdocSummaryManager edocSummaryManager = (EdocSummaryManager)AppContext.getBean("edocSummaryManager");
        	List<SimpleEdocSummary> edocSummarys = edocSummaryManager.findSimpleEdocSummarysByIds(edocIdList);
        	for(SimpleEdocSummary summary : edocSummarys){
        		edocSummaryMap.put(summary.getId(), summary);
        	}
        }*/
        Map<Long, ColSummary> colSummaryMap = new HashMap<Long, ColSummary>();
        if (Strings.isNotEmpty(collIdList)) {
            List<ColSummary> colSummarys = this.findColSummarysByIds(collIdList);
            for (ColSummary summary : colSummarys) {
                colSummaryMap.put(summary.getId(), summary);
            }
        }
        //map.put("edocSummaryMap", edocSummaryMap);
        //map.put("colSummaryMap", colSummaryMap);
        return colSummaryMap;
    }

    /**
     * 获取指定分钟数后的日期
     *
     * @param minutes
     * @return
     */
    public Long calculateWorkDatetime(Map<String, String> params) throws BusinessException {
        Date newDate = new Date();
        int m = 0;
        String strMinutes = params.get("minutes");
        if (strMinutes != null) {
            m = Integer.valueOf(params.get("minutes"));
        }
        String datetime = params.get("datetime");
        if (Strings.isNotBlank(datetime)) {
            Date fromDate = Datetimes.parseDatetimeWithoutSecond(datetime);
            newDate = workTimeManager.getRemindDate(fromDate, m);
        } else {
            int minutes = m;
            Long currentAccuntId = AppContext.getCurrentUser().getLoginAccount();
            int workDayCount = workTimeManager.getWorkDaysByWeek();
            //如果未设置工作日，按照自然日计算
            if (workDayCount == 0) {
                workDayCount = 7;
            }

            switch (minutes) {
                case 0:
                case 5:
                case 10:
                case 15:
                case 30:
                    newDate = workTimeManager.getCompleteDate4Worktime(new Date(), m, currentAccuntId);
                    break;
                case 60:// 1小时
                case 120:// 2小时
                case 180:// 3小时
                case 240:// 4小时
                case 300:// 5小时
                case 360:// 6小时
                case 420:// 7小时
                case 480:// 8小时
                case 720:// 0.5天
                    long hours = Long.valueOf(m / 60);
                    newDate = workTimeManager.getComputeDate(new Date(), "+", hours, "hour", currentAccuntId);
                    break;
                case 1440:// 1天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 1, "day", currentAccuntId);
                    break;
                case 2880:// 2天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 2, "day", currentAccuntId);
                    break;
                case 4320:// 3天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 3, "day", currentAccuntId);
                    break;
                case 5760:// 4天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 4, "day", currentAccuntId);
                    break;
                case 7200:// 5天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 5, "day", currentAccuntId);
                    break;
                case 8640:// 6天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 6, "day", currentAccuntId);
                    break;
                case 14400:// 10天
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 10, "day", currentAccuntId);
                    break;
                case 10080:// 1周
                    newDate = workTimeManager.getComputeDate(new Date(), "+", workDayCount, "day", currentAccuntId);
                    break;
                case 20160:// 2周
                    newDate = workTimeManager.getComputeDate(new Date(), "+", Long.valueOf(workDayCount * 2L), "day", currentAccuntId);
                    break;
                case 21600:// 半个月
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 15, "day", currentAccuntId);
                    break;
                case 30240:// 3周
                    newDate = workTimeManager.getComputeDate(new Date(), "+", Long.valueOf(workDayCount * 3L), "day", currentAccuntId);
                    break;
                case 43200:// 1个月
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 30, "day", currentAccuntId);
                    break;
                case 86400:// 2个月
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 60, "day", currentAccuntId);
                    break;
                case 129600:// 3个月
                    newDate = workTimeManager.getComputeDate(new Date(), "+", 90, "day", currentAccuntId);
                    break;
                default:
                    newDate = workTimeManager.getComputeDate(new Date(), "+", workDayCount, "day", currentAccuntId);
                    break;
            }
        }
        return newDate.getTime();
    }

    @Override
    public List<ColSummaryVO> getSummaryByTemplateIdAndState(Long templateId, Integer state) throws BusinessException {
        return colDao.getSummaryByTemplateId(templateId, state);
    }

    @Override
    public String[] getProcessIdByColSummaryId(Long id) throws BusinessException {
        ColSummary summary = getColSummaryById(id);
        return new String[]{summary.getProcessId()};
    }

    @Override
    /**
     * ajax调用，查看跟踪人名称
     */
    public String getTrackName(Map params) {
        String userName = "";
        String ids = (String) params.get("userId");
        if (Strings.isNotBlank(ids)) {
            String[] str = ids.split(",");
            try {
                for (int i = 0; i < str.length; i++) {
                    String strId = str[i].replace("|", ",");
                    V3xOrgMember member = orgManager.getMemberById(Long.valueOf(strId.split(",")[1]));
                    userName += member.getName() + ",";
                }
            } catch (NumberFormatException e) {
                LOG.error("通过用户ID获取用户类型转换错误", e);
            } catch (BusinessException e) {
                LOG.error("通过用户ID获取用户对象出错", e);
            }
            return userName.substring(0, userName.length() - 1);
        }
        return userName;
    }


    public String ajaxCheckAgent(Map param) {

        Long affairMemberId = Long.valueOf((String) param.get("affairMemberId"));
        String subject = String.valueOf(param.get("subject"));
        int moduleType = Integer.valueOf((String) param.get("moduleType"));

        ModuleType mt = ModuleType.getEnumByKey(moduleType);

        String result = ColUtil.ajaxCheckAgent(affairMemberId, subject, mt);

        return result;
    }

    public ColSummary getColSummaryByFormRecordId(Long formRecordId) throws BusinessException {
        return colDao.getColSummaryByFormRecordId(formRecordId);
    }

    @Override
    public Map<Long, Long> getColSummaryIdByFormRecordIds(List<Long> formRecords) throws BusinessException {
        return colDao.getColSummaryIdByFormRecordIds(formRecords);
    }


    public Integer getColSummaryCount(Date beginDate, Date endDate, boolean isForm) throws BusinessException {
        return this.colDao.findIndexResumeCount(beginDate, endDate, isForm);
    }

    public List<Long> findIndexResumeIDList(Date starDate, Date endDate, Integer firstRow, Integer pageSize, boolean isForm) throws BusinessException {
        return this.colDao.findIndexResumeIDList(starDate, endDate, firstRow, pageSize, isForm);
    }

    /**
     * 获取协同的默认节点
     *
     * @param orgAccountId
     * @return
     * @throws BusinessException
     */
    public Map<String, String> getColDefaultNode(Long orgAccountId) throws BusinessException {
        Map<String, String> tempMap = new HashMap<String, String>();
        //默认节点权限
        PermissionVO permission = this.permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(), orgAccountId);
        String defaultNodeName = "";
        String defaultNodeLable = "";
        if (permission != null) {
            defaultNodeName = permission.getName();
            defaultNodeLable = permission.getLabel();
        }
        tempMap.put("defaultNodeName", defaultNodeName);
        tempMap.put("defaultNodeLable", defaultNodeLable);
        return tempMap;
    }

    @Override
    public Permission getPermisson(CtpAffair affair, ColSummary summary, List<String> nodePermissions) throws BusinessException {

        Permission permission = getPermisson(affair, summary);

        List<String> basicActionList = permissionManager.getActionList(permission, PermissionAction.basic);
        List<String> commonActionList = permissionManager.getActionList(permission, PermissionAction.common);
        List<String> advanceActionList = permissionManager.getActionList(permission, PermissionAction.advanced);

        nodePermissions.addAll(basicActionList);
        nodePermissions.addAll(commonActionList);
        nodePermissions.addAll(advanceActionList);
        return permission;
    }

    @Override
    public Permission getPermisson(CtpAffair affair, ColSummary summary) throws BusinessException {

        //如果是调用的模板，节点权限取模板对应的节点权限。
        Long permissionAccountId = summary.getPermissionAccountId();
        Long startMenberId = summary.getStartMemberId();
        //兼容320升级上来的数据accountId为空的情况
        if (null == permissionAccountId && startMenberId != null) {
            V3xOrgMember orgMember = orgManager.getMemberById(startMenberId);
            permissionAccountId = orgMember.getOrgAccountId();
        }
        String configItem = ColUtil.getPolicyByAffair(affair).getId();
        String category = EnumNameEnum.col_flow_perm_policy.name();

        if (permissionAccountId == null) {
            LOG.info("permissionAccountId is null ,summaryId:" + summary.getPermissionAccountId());
        }

        return permissionManager.getPermission(category, configItem, permissionAccountId);
    }


    /**
     * 协同转办
     *
     * @param params 必传递参数：<br>
     *               affairId
     * @return
     * @throws BusinessException
     */
    public String transColTransfer(Map<String, String> params) throws BusinessException {
        String message = "";
        User user = AppContext.getCurrentUser();
        String pageNodePolicy = ParamUtil.getString(params, "pageNodePolicy", "");
        Long affairId = Long.valueOf(params.get("affairId"));
        CtpAffair ctpAffair = affairManager.get(affairId);
        String checkAffairValid = checkAffairValid(ctpAffair.getId().toString(), pageNodePolicy);
        if (Strings.isNotBlank(checkAffairValid)) {
            return checkAffairValid;
        }
        if (ctpAffair.isDelete()) {
            return message;
        }
        ColSummary summary = this.getColSummaryById(ctpAffair.getObjectId());
        //1、保存意见、附件

        //从request对象中对象中获取意见
        Comment comment = ContentUtil.getCommnetFromRequest(null, ctpAffair.getMemberId(), ctpAffair.getObjectId());

        //保存意见
        comment.setModuleId(summary.getId());
        comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
        comment.setCreateId(ctpAffair.getMemberId());
        comment.setAffairId(ctpAffair.getId());
        comment.setExtAtt3("collaboration.dealAttitude.transfer");
        if (!user.getId().equals(ctpAffair.getMemberId())) {
            comment.setExtAtt2(user.getName());
        }
        comment.setModuleType(ModuleType.collaboration.getKey());
        comment.setPid(0L);

        /**
         * m3转办过来的意见取得方式和PC不同
         */
        Comment m3Comment = (Comment) AppContext.getRequestContext("m3Comment");
        if (m3Comment != null) {
            AppContext.removeSessionArrribute("m3Comment");
            comment = m3Comment;
            comment.setExtAtt3("collaboration.dealAttitude.transfer");
            if (!user.getId().equals(ctpAffair.getMemberId())) {
                comment.setExtAtt2(user.getName());
            }
            saveOrUpdateComment(comment);
            String modifyFlag = ParamUtil.getString(params, "modifyFlag", "");
            if ("1".equals(modifyFlag)) {
                //修改正文后记录流程日志
                processLogManager.insertLog(AppContext.getCurrentUser(), Long.parseLong(summary.getProcessId()),
                        ctpAffair.getActivityId(), ProcessLogAction.processColl, comment.getId(), String.valueOf(ProcessLogAction.ProcessEdocAction.modifyBody.getKey()));
            }
        } else {
            saveOrUpdateComment(comment);
            //保存附件
            saveAttDatas(user, summary, ctpAffair, comment.getId());
        }

        //2、转办流程
        //转给指定人id
        Long transferMemberId = Long.valueOf(params.get("transferMemberId"));
        String processId = summary.getProcessId();

        try {
            //*****判断当前转给指定人是否有效开始***********
            V3xOrgMember orgMember = orgManager.getMemberById(transferMemberId);
            if (!orgMember.isValid()) {
                message = "转办指定人无效，请重新指定！";
                return message;
            }

            //删除原来的定时任务（超期提醒、提前提醒）
            if (ctpAffair.getRemindDate() != null && ctpAffair.getRemindDate() != 0) {
                QuartzHolder.deleteQuartzJob("Remind" + ctpAffair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                QuartzHolder.deleteQuartzJob("Remind" + ctpAffair.getObjectId() + "_" + ctpAffair.getActivityId());
            }
            if ((ctpAffair.getDeadlineDate() != null && ctpAffair.getDeadlineDate() != 0) || ctpAffair.getExpectedProcessTime() != null) {
                QuartzHolder.deleteQuartzJob("DeadLine" + ctpAffair.getId());//保证老数据不报错, 6.0后面的版本把这个删除
                QuartzHolder.deleteQuartzJob("DeadLine" + ctpAffair.getObjectId() + "_" + ctpAffair.getActivityId());
            }

            List<V3xOrgMember> nextMembers = new ArrayList<V3xOrgMember>();
            //获取当前待办事项
            /*List<CtpAffair> affairs= affairManager.getAffairs(summary.getId(), StateEnum.col_pending);
            //判断当前转给的指定人是否是待办人，且是否是同一个节点(包含了同一个人)
            for(CtpAffair affair : affairs) {
            	if (affair.getMemberId() != transferMemberId && affair.getActivityId() !=  ctpAffair.getActivityId()) {

            	}
            }*/
            nextMembers.add(orgMember);
            //*****判断当前转给指定人是否有效结束***********

            //*****开始修改事项和转给指定人***************
            //判断当前转给的人是否为空
            String newAffairMemeerId = "";
            if (nextMembers.size() > 0) {
                LOG.info("转办参数： 转办人：" + orgMember.getName() + ",id=" + orgMember.getId());
                LOG.info("转办参数：processId：" + processId + ",id=" + orgMember.getId());
                LOG.info("转办参数：affair: memberId=" + ctpAffair.getMemberId() + ",subObjectId="
                        + ctpAffair.getSubObjectId() + "activityId=" + ctpAffair.getActivityId());


                Object[] result = wapi.replaceWorkItemMembers(true, ctpAffair.getMemberId(), processId,
                        ctpAffair.getSubObjectId(), ctpAffair.getActivityId().toString(), nextMembers, true);
                List<WorkItem> workitems = (List<WorkItem>) result[1];
                BPMHumenActivity bpmActivity = (BPMHumenActivity) result[2];
                List<CtpAffair> newAffairs = new ArrayList<CtpAffair>();

                for (int i = 0; i < workitems.size(); i++) {
                    WorkItem workItem = workitems.get(i);
                    CtpAffair newAffair = (CtpAffair) BeanUtils.cloneBean(ctpAffair);
                    //一、替换v3x_affair中的member_id为dealTermUserId

                    newAffairMemeerId = workItem.getPerformer();

                    newAffair.setMemberId(Long.parseLong(newAffairMemeerId));
                    newAffair.setId(UUIDLong.longUUID());
                    newAffair.setSubObjectId(workItem.getId());
                    newAffair.setCoverTime(false);
                    newAffair.setReceiveTime(new Date());
                    newAffair.setUpdateDate(newAffair.getReceiveTime());
                    newAffair.setFirstViewDate(null);
                    newAffair.setUpdateDate(new Date());
                    newAffair.setDelete(false);
                    newAffair.setFromId(user.getId());
                    newAffair.setFromType(ChangeType.Transfer.getKey());
                    newAffair.setBackFromId(null);
                    newAffair.setState(StateEnum.col_pending.getKey());
                    newAffair.setSubState(SubStateEnum.col_pending_unRead.getKey());
                    newAffair.setOverWorktime(null);
                    newAffair.setRunWorktime(null);
                    newAffair.setOverTime(null);
                    newAffair.setRunTime(null);
                    newAffair.setPreApprover(ctpAffair.getMemberId());
                    V3xOrgMember nextMember = nextMembers.get(i);
                    if (newAffair.getDeadlineDate() != null && newAffair.getDeadlineDate() != 0l) {
                        newAffair.setExpectedProcessTime(workTimeManager
                                .getCompleteDate4Nature(new Date(), newAffair.getDeadlineDate(),
                                        nextMember.getOrgAccountId()));
                    }
                    Long accountId = ColUtil.getFlowPermAccountId(AppContext.currentAccountId(), summary);
                    ColUtil.affairExcuteRemind(newAffair, accountId);
                    newAffairs.add(newAffair);
                }
                affairManager.saveAffairs(newAffairs);
                //发送消息和记录日志
                //五、写流程日志和应用日志
                processLogManager.insertLog(user, Long.valueOf(processId), Long.valueOf(bpmActivity.getId()), ProcessLogAction.transfer, comment.getId(), user.getName(), orgMember.getName());
                //{0}转办协同《{1}》给{2}
                appLogManager.insertLog(user, AppLogAction.Coll_Tranfer, user.getName(), summary.getSubject(), orgMember.getName());

                //发送移交消息
                colMessageManager.sendMessage4Transfer(user, summary, newAffairs, ctpAffair, comment);

                CollaborationAffairsAssignedEvent event = new CollaborationAffairsAssignedEvent(this);
                event.setAffairs(newAffairs);
                EventDispatcher.fireEventAfterCommit(event);
            }
            ctpAffair.setDelete(true);
            ctpAffair.setActivityId(-1l);
            ctpAffair.setSubObjectId(-1l);
            ctpAffair.setObjectId(-1l);
            ctpAffair.setTempleteId(-1l);
            Long oldMemberId = ctpAffair.getMemberId();
            ctpAffair.setMemberId(-1l);

            //更新操作时间
            if (ctpAffair.getSignleViewPeriod() == null
                    && ctpAffair.getFirstViewDate() != null) {
                Date nowTime = new Date();
                long viewTime = workTimeManager.getDealWithTimeValue(ctpAffair.getFirstViewDate(), nowTime, ctpAffair.getOrgAccountId());
                ctpAffair.setSignleViewPeriod(viewTime);
            }

            affairManager.updateAffair(ctpAffair);
            //*****结束修改事项和转给指定人***************

            ColUtil.addOneReplyCounts(summary);
            //更新当前待办人
            // ColUtil.updateCurrentNodesInfo(summary);
            //String currentNodesInfo = Strings.join(";", summary.getCurrentNodesInfo(),newAffairMemeerId);
            LOG.info("移交的时候更新当前待办人日志：1=" + summary.getCurrentNodesInfo() + ",2=" + oldMemberId + ",3=" + newAffairMemeerId);
            String currentNodesInfo = summary.getCurrentNodesInfo().replaceFirst(String.valueOf(oldMemberId), String.valueOf(newAffairMemeerId));
            summary.setCurrentNodesInfo(currentNodesInfo);

            this.updateColSummary(summary);
        } catch (Throwable e) {
            LOG.error("转办报错！", e);
        } finally {
            this.colDelLock(summary, ctpAffair);
        }
        return message;
    }

    @Override
    public List<ColSummary> findColSummarys(QuerySummaryParam param, FlipInfo flip) throws BusinessException {
        return colDao.findColSummarys(param, flip);
    }

    @Override
    public List<ColSummaryVO> queryByCondition4DataRelation(FlipInfo flipInfo, Map<String, String> condition)
            throws BusinessException {

        return colDao.queryByCondition4DataRelation(flipInfo, condition);
    }

    @AjaxAccess
    public String getAffairState(String affairId) throws BusinessException {
        CtpAffair affair = null;
        String state = "";
        if (Strings.isNotBlank(affairId)) {
            try {
                affair = affairManager.getSimpleAffair(Long.valueOf(affairId));
                if (affair != null) {
                    state = String.valueOf(affair.getState());
                }
            } catch (Exception e) {
                LOG.error("", e);
            }

        }
        return state;
    }

    @AjaxAccess
    public String getRelativeMembers() {
        // 关联人员
        User user = AppContext.getCurrentUser();
        List<WebEntity4QuickIndex> oml = new ArrayList<WebEntity4QuickIndex>();

        List<PeopleRelate> l = new ArrayList<PeopleRelate>();
        try {
            l = peopleRelateManager.getPeopleRelatedList(user.getId());
        } catch (Exception e) {
            LOG.error("", e);
        }
        Long currentLoginAccId = user.getLoginAccount();
        // 关联人员转换成快速选人的格式
        if (Strings.isNotEmpty(l)) {
            for (PeopleRelate peopleRelate : l) {
                if (oml.size() == 50) {
                    break;
                }
                V3xOrgMember vm = null;
                try {
                    vm = orgManager.getMemberById(Long.valueOf(peopleRelate.getRelateMemberId()));
                } catch (BusinessException e) {
                    LOG.error("", e);
                }
                // 被删除,停用,离职，人员调出不添加
                if (null == vm || !vm.isValid()) {
                    continue;
                }
                WebEntity4QuickIndex data = null;
                V3xOrgDepartment d = null;
                try {
                    d = orgManager.getDepartmentById(vm.getOrgDepartmentId());
                } catch (BusinessException e) {
                    LOG.error("", e);
                }
                if (currentLoginAccId.equals(vm.getOrgAccountId())) {
                    data = new WebEntity4QuickIndex("Member|" + vm.getId() + "|" + vm.getOrgAccountId(), vm.getName(),
                            null == d ? "" : d.getName());
                } else {
                    V3xOrgAccount account = null;
                    try {
                        account = orgManager.getAccountById(vm.getOrgAccountId());
                    } catch (BusinessException e) {
                        LOG.error("", e);
                    }
                    if (null == account || !account.isValid()) {
                        continue;
                    }
                    if (vm.getName().contains("(") && vm.getName().contains("-")) {
                        data = new WebEntity4QuickIndex("Member|" + vm.getId() + "|" + vm.getOrgAccountId(),
                                vm.getName(), null == d ? "" : d.getName());
                    } else {
                        data = new WebEntity4QuickIndex("Member|" + vm.getId() + "|" + vm.getOrgAccountId(),
                                vm.getName() + "(" + account.getShortName() + ")", null == d ? "" : d.getName());
                    }
                }

                WebEntity4QuickIndex4Col c = new WebEntity4QuickIndex4Col();
                c.setD(data.getD());
                c.setK(data.getK());
                c.setS(data.getS());
                int vlength = data.getV().length();
                String _tv = Strings.toHTML(data.getV());
                c.setSn(vlength <= 38 ? _tv : _tv.substring(38) + "...");
                c.setV(Strings.toHTML(data.getV()));
                oml.add(c);
            }
        }
        return JSONUtil.toJSONString(oml);
    }

    @Override
    @AjaxAccess
    public Map<String, String> showMoreBtn(Map<String, String> map) throws BusinessException {
        Map<String, String> r_map = new HashMap<String, String>();

        String checkBtnStr = map.get("checkBtns");

        if (checkBtnStr != null) {
            String[] checkBtns = checkBtnStr.split(",");
            for (String btn : checkBtns) {
                if ("Favorite".equals(btn)) {//收藏
                    String collect = "false";
                    User user = AppContext.getCurrentUser();
                    String affairId = map.get("affairId");
                    //收藏
                    String collectFlag = SystemProperties.getInstance().getProperty("doc.collectFlag");
                    if (AppContext.hasPlugin(PortletCategory.doc.name()) && "true".equals(collectFlag)) {
                        if (AppContext.hasPlugin("doc")) {
                            List<Map<String, Long>> collectMap = docApi.findFavorites(user.getId(), CommonTools.newArrayList(Long.valueOf(affairId)));
                            if (Strings.isNotEmpty(collectMap)) {
                                collect = "true";
                            }
                        }
                    }
                    r_map.put("collect", collect);
                }
            }
        }

        return r_map;
    }

    @AjaxAccess
    public String checkAffairAndLock4NewCol(String summaryId, String isNeedCheckAffair) throws NumberFormatException, BusinessException {

        if (Strings.isEmpty(summaryId)) {
            //"校验参数不合法！";
            return ResourceUtil.getString("collaboration.newcoll.check.param.error");
        }

        if (Strings.isNotBlank(summaryId)) {
            boolean isLock = colLockManager.isLock(Long.valueOf(summaryId));

            if (isLock) {
                return ResourceUtil.getString("collaboration.summary.notDuplicateSub");
            }
        }


        if ("true".equals(isNeedCheckAffair)) {  //性能优化：如果是直接新建就不需要判断affair了。
            Integer state = affairManager.getStartAffairStateByObjectId(Long.valueOf(summaryId));

            if (Integer.valueOf(StateEnum.col_sent.key()).equals(state)) {
                //"事项已经被发送，你不能执行该操作";
                return ResourceUtil.getString("collaboration.newcoll.check.send.error");
            } else if (state == null) {
                //"事项已经被删除，你不能执行该操作";
                return ResourceUtil.getString("collaboration.newcoll.check.affair.error");
            }
        }
        return "";
    }

    @Override
    @AjaxAccess
    public String checkAffairValidAndIsLock(String affairId, String realLockForm, String pageNodePolicy) throws NumberFormatException, BusinessException {


        CtpAffair affair = getSimpleAffair4Check(affairId);

        String errorMsg = checkAffairValid(affair, true, pageNodePolicy);

        if (Strings.isBlank(errorMsg)) {

            if (affair != null && affair.getState() != StateEnum.col_pending.key()) {
                errorMsg = ColUtil.getErrorMsgByAffair(affair);
                if (Strings.isNotBlank(errorMsg)) {
                    return errorMsg;
                }
            }

            boolean isLock = colLockManager.isLock(Long.valueOf(affairId));

            if (isLock) {
                errorMsg = ResourceUtil.getString("collaboration.summary.notDuplicateSub");
            } else {
                //TODO : 自由协同 加签类的锁 | 修改正文类的锁  需要校验有效性

                if (null != affair.getFormRecordid()) {//提交的时候校验表单锁是否有效

                    Lock lock = capFormManager.getLock(affair.getFormRecordid());
                    boolean isRealLock = "true".equals(realLockForm);

                    LOG.info(AppContext.currentUserName() + ",离开页面验证锁 lock 为空!!!!! ：" + (lock == null) + ",isRealLock=" + isRealLock + ",formRecordid:" + affair.getFormRecordid());

                    if (isRealLock) {

                        boolean isLockedByOther = false; //是否被其他人抢占了资源锁

                        if (lock != null) {
                            boolean isOne = lock.getOwner() == AppContext.currentUserId();
                            String from = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
                            if (!isOne || !from.equals(lock.getFrom())) {
                                isLockedByOther = true;
                            }
                        }


                        if (isLockedByOther || lock == null) {
                            errorMsg = ResourceUtil.getString("collaboration.summary.lock.lost");
                        }
                    }
                }
            }
        }

        return errorMsg;
    }


    public void unlockCollAll(Long affairId, CtpAffair affair, ColSummary summary) {
        try {
            if (affairId != null && affairId.longValue() != -1) {
                if (affair == null) {
                    affair = affairManager.get(affairId);
                }
                if (affair != null && !affair.isDelete()) {
                    if (summary == null) {
                        summary = this.getColSummaryById(affair.getObjectId());
                    }
                    if (summary != null) {
                        try {
                            this.colDelLock(summary, affair, true);
                        } catch (BusinessException e) {
                            LOG.error("表单锁解锁失败", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("协同解锁失败", e);
        }
    }

    /**
     * 加载At的人员列表
     *
     * @param params
     * @throws BusinessException
     */
    @AjaxAccess
    @Override
    public List<Map<String, Object>> pushMessageToMembersList(Map<String, String> params) throws BusinessException {
        return pushMessageToMembersList(params, false);
    }

    @Override
    public List<Map<String, Object>> pushMessageToMembersList(Map<String, String> params, boolean needPost) throws BusinessException {
        Long summaryId = Long.valueOf(params.get("summaryId"));

        List<StateEnum> states = new ArrayList<StateEnum>();
        states.add(StateEnum.col_sent);
        states.add(StateEnum.col_done);
        states.add(StateEnum.col_pending);
        states.add(StateEnum.col_waitSend);

        List<CtpAffair> pushMessageList = affairManager.getAffairs(summaryId, states);
        //排序顺序规则，发起人、已办、暂存待办
        Collections.sort(pushMessageList, new Comparator<CtpAffair>() {
            @Override
            public int compare(CtpAffair o1, CtpAffair o2) {
                if (o1.getState() == StateEnum.col_sent.getKey())
                    return -1;
                else if (o2.getState() == StateEnum.col_sent.getKey())
                    return 1;
                else {
                    if (o1.getState() == StateEnum.col_done.key())
                        return -1;
                    else if (o2.getState() == StateEnum.col_done.key())
                        return 1;
                    else
                        return 0;
                }
            }
        });
        //过滤掉自己和重复项
        Map<Long, Boolean> memberIdMap = new HashMap<Long, Boolean>(pushMessageList.size());

        List<Map<String, Object>> rset = new ArrayList<Map<String, Object>>();
        Long currentUserId = AppContext.currentUserId();
        for (CtpAffair r : pushMessageList) {
            //只显示已发、暂存待办和、已办的、回退者
            int subState = r.getSubState();
            int state = r.getState();
            if ((subState == SubStateEnum.col_pending_ZCDB.key() && state == StateEnum.col_pending
                    .key()) || state == StateEnum.col_done.key() || state == StateEnum.col_sent.key()
                    || subState == SubStateEnum.col_pending_specialBack.key()
                    || subState == SubStateEnum.col_pending_specialBacked.key()
                    || subState == SubStateEnum.col_pending_specialBackCenter.key()
                    || state == StateEnum.col_pending.key()) {
                Long memberId = r.getMemberId();
                if (!memberId.equals(currentUserId) && memberIdMap.get(memberId) == null/* && !memberIdList.contains(r.getMemberId())*/) {
                    memberIdMap.put(memberId, Boolean.TRUE);

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("state", state);
                    map.put("subState", subState);
                    map.put("memberId", memberId);
                    map.put("backFromId", r.getBackFromId());
                    map.put("id", r.getId());

                    V3xOrgMember member = orgManager.getMemberById(memberId);
                    if (member == null) {
                        continue;
                    }
                    map.put("name", member.getName());
                    map.put("phone", member.getTelNumber());

                    //加载岗位信息
                    if (needPost) {
                        V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
                        if (post != null) {
                            map.put("postName", post.getName());
                        }
                    }

                    if (state == StateEnum.col_sent.getKey()) {
                        map.put("i18n", ResourceUtil.getString("cannel.display.column.sendUser.label"));
                    } else if (state == StateEnum.col_pending.getKey()) {
                        map.put("i18n", ResourceUtil.getString("collaboration.default.currentToDo"));
                    } else if (state == StateEnum.col_done.getKey()) {
                        map.put("i18n", ResourceUtil.getString("collaboration.default.haveBeenProcessedPe"));
                    } else if (subState == SubStateEnum.col_pending_specialBack.getKey()) {
                        map.put("i18n", ResourceUtil.getString("collaboration.default.stepBack"));
                    } else if (subState == SubStateEnum.col_pending_specialBackCenter.getKey()) {
                        map.put("i18n", ResourceUtil.getString("collaboration.default.specialBacked"));
                    } else if (state == StateEnum.col_waitSend.getKey() && subState == SubStateEnum.col_pending_specialBacked.getKey()) {
                        map.put("i18n", ResourceUtil.getString("cannel.display.column.sendUser.label"));
                    } else {
                        map.put("i18n", ResourceUtil.getString("collaboration.default.stagedToDo"));
                    }

                    rset.add(map);
                }
            }
        }
        return rset;
    }

    @AjaxAccess
    public Map<String, Object> findsSummaryComments(Map<String, String> params) throws BusinessException {
        //是否查询转储数据
        Boolean isHistory = "true".equals(params.get("isHistory"));
        //协同ID
        Long moduleId = Long.valueOf(params.get("moduleId"));
        //页数
        Integer page = Integer.valueOf(params.get("page"));
        //锚点定位的意见Id
        Long anchorCommentId = ParamUtil.getLong(params, "anchorCommentId", null);
        //每页的条数
        Integer pageSize = Integer.valueOf(params.get("pageSize"));
        //查当前意见还是转发意见[ 0|全部，1|转发，2|当前]
        String queryType = params.get("queryType");
        String paramForwardCountKeys = params.get("forwardCountKeys");
        String forwardMember = params.get("forwardMember"); //当前协同是否是转发的协同
        String summaryCommentsCounts = params.get("replyCounts");

        boolean isForwardColl = false;
        int summaryCommentsCount = 0;
        if (Strings.isNotBlank(forwardMember)) {
            isForwardColl = true;
        }
        if (Strings.isNotBlank(summaryCommentsCounts)) {
            summaryCommentsCount = Integer.valueOf(summaryCommentsCounts);
        }


        boolean isQueryAll = "0".equals(queryType);
        //是否需要查询转发意见
        boolean isQueryFoward = false;
        //是否需要查询当前意见
        boolean isQueryCurrent = false;

        //协同ID
        FlipInfo fpi = new FlipInfo();
        fpi.setSize(pageSize);
        fpi.setNeedTotal(false);
        fpi.setPage(page);


        //当前意见列表
        List<Comment> commentList = null;
        //当前发起人附言
        List<Comment> commentSenderList = null;
        //转发的意见  - [转发的次数 ：[意见类型（评论\回复\发起人附言等） : 对应类型的意见列表]]
        Map<Integer, Map<Integer, List<Comment>>> resultMap = new HashMap<Integer, Map<Integer, List<Comment>>>();
        //转发的点赞数[转发的次数：点赞数]
        Map<Integer, Integer> forwardPraise = new HashMap<Integer, Integer>();
        //主协同点赞数
        int praiseToSumNum = 0;
        //意见总数
        Map<Integer, Integer> allCommentCount = new HashMap<Integer, Integer>();


        //总的意见数是否大于一页（包括转发的意见+本身的）
        boolean allMorePageSize = false;
        //当前意见数是否大于一页
        boolean currentMorePageSize = false;
        //转发的意见数是否大于一页
        boolean forwardMorePageSize = false;
        //是否有转发的意见
        List<Integer> forwardCountKeys = new ArrayList<Integer>();
        if (Strings.isNotBlank(paramForwardCountKeys)) {
            String[] arr = paramForwardCountKeys.split("[,]");
            for (String s : arr) {
                forwardCountKeys.add(Integer.valueOf(s));
            }
        }
        Integer allCnt = 0;
        int forwardCnt = 0;
        int currentCnt = 0;
        if (isQueryAll) {
            if (isForwardColl) {
                Map<Integer, Map<Integer, Integer>> cntMap = commentManager.countComments(ModuleType.collaboration, moduleId, new HashMap<String, Object>(), isHistory);
                Collection<Integer> a = cntMap.keySet();

                if (Strings.isNotEmpty(a)) {
                    for (Integer forwardCountKey : a) {
                        Map<Integer, Integer> m = cntMap.get(forwardCountKey);
                        Collection<Integer> ctypes = m.keySet();
                        for (Integer ctype : ctypes) {
                            int count = m.get(ctype) == null ? 0 : m.get(ctype);
                            allCnt += count;
                            //是否有转发意见
                            if (Integer.valueOf(0).equals(forwardCountKey)) {
                                currentCnt += count;
                            } else {
                                forwardCnt += count;
                                forwardCountKeys.add(forwardCountKey);
                            }

                            if (ctype.equals(Comment.CommentType.comment.getKey())) {
                                allCommentCount.put(forwardCountKey, count);
                            }
                        }
                        if (allCommentCount.get(forwardCountKey) == null) {
                            allCommentCount.put(forwardCountKey, 0);
                        }
                    }
                    if (currentCnt > pageSize) {
                        currentMorePageSize = true;
                    }
                    if (allCnt > pageSize) {
                        allMorePageSize = true;
                    }
                    if (forwardCnt > pageSize) {
                        forwardMorePageSize = true;
                    }
                }
            } else {
                allCnt = summaryCommentsCount;
                forwardMorePageSize = false;
                allCommentCount.put(0, summaryCommentsCount);
                if (summaryCommentsCount > pageSize) {
                    currentCnt = pageSize;
                    currentMorePageSize = true;
                    allMorePageSize = true;
                } else {
                    currentCnt = summaryCommentsCount;
                    currentMorePageSize = false;
                    allMorePageSize = false;
                }
            }
        }

        //第一次查询的时候校验一下是否有数据，有数据才查询，只查转发数据的时候，说明是点的页面的下一条，这个时候就代表肯定有数据了。
        if ((isQueryAll && forwardCnt > 0) || "1".equals(queryType)) {
            isQueryFoward = true;
        }
        //第一次查询的时候校验一下是否有数据，有数据才查询，指定queryType查询当前意见，说明是点的页面的下一条，这个时候就代表肯定有数据了。
        if ((isQueryAll && currentCnt > 0) || "2".equals(queryType)) {
            isQueryCurrent = true;
        }
        boolean currentHasNext = false;
        boolean forwardHasNext = false;
        if (!allMorePageSize && isQueryAll) {
            // 全部一次性查出来，包括评论\回复，包括当前的和转发的一起
            resultMap = commentManager.getCommentsWithForward(ModuleType.collaboration, moduleId, isHistory);

            commentList = (List<Comment>) AppContext.getRawRequest().getAttribute("commentList");
            commentSenderList = (List<Comment>) AppContext.getRawRequest().getAttribute("commentSenderList");
            praiseToSumNum = (Integer) AppContext.getRawRequest().getAttribute("praiseToSumNum");
            forwardPraise = (Map<Integer, Integer>) AppContext.getRawRequest().getAttribute("forwardPraise");
        } else {
            if (isQueryAll) {
                Map<String, Object> queryParams = new HashMap<String, Object>();
                queryParams.put("praiseToSummary", 1);

                Map<Integer, Map<Integer, Integer>> cntMap = commentManager.countComments(ModuleType.collaboration, moduleId, queryParams, isHistory);
                //从双层Map的结构中解析得到点赞数
                parsePraiseCntMap(forwardPraise, cntMap);

                praiseToSumNum = forwardPraise.get(0) == null ? 0 : forwardPraise.get(0);
            }

            if (isQueryFoward) {
                List<Comment> comments = new ArrayList<Comment>();
                if (forwardMorePageSize || page > 1 || !"0".equals(queryType)) {
                    Map<String, Object> m = findSummaryCommentsByPage(fpi, isHistory, moduleId, forwardCountKeys, null);
                    forwardHasNext = (Boolean) m.get("Next");
                    comments = (List<Comment>) m.get("CommentList");
                } else {
                    //全部一次性查出来，包括评论\回复

                    Map<String, Object> queryParams = new HashMap<String, Object>();
                    List<Integer> ctypes = new ArrayList<Integer>();
                    ctypes.add(Comment.CommentType.comment.getKey());
                    ctypes.add(Comment.CommentType.sender.getKey());
                    ctypes.add(Comment.CommentType.reply.getKey());
                    queryParams.put("ctypes", ctypes);
                    queryParams.put("forwardCounts", forwardCountKeys);
                    comments = commentManager.findComments(ModuleType.collaboration, moduleId, null, queryParams, isHistory);
                }

                bindFatherSonRelation(comments);

                packageComment(resultMap, comments);
            }


            if (isQueryCurrent) {

                List<Comment> currentComments = new ArrayList<Comment>();
                if (currentMorePageSize || page > 1 || !"0".equals(queryType)) {
                    //1、查一页的评论
                    List<Integer> forwardCount0 = new ArrayList<Integer>();
                    forwardCount0.add(0);
                    Map m = findSummaryCommentsByPage(fpi, isHistory, moduleId, forwardCount0, anchorCommentId);
                    currentHasNext = (Boolean) m.get("Next");
                    currentComments = (List<Comment>) m.get("CommentList");
                    if (null != anchorCommentId) {
                        commentList = currentComments;
                    }
                } else {
                    //全部一次性查出来，包括评论\回复

                    Map<String, Object> queryParams = new HashMap<String, Object>();
                    List<Integer> ctypes = new ArrayList<Integer>();
                    ctypes.add(Comment.CommentType.comment.getKey());
                    ctypes.add(Comment.CommentType.sender.getKey());
                    ctypes.add(Comment.CommentType.reply.getKey());
                    queryParams.put("ctypes", ctypes);
                    List<Integer> forwardCount0 = new ArrayList<Integer>();
                    forwardCount0.add(0);
                    queryParams.put("forwardCounts", forwardCount0);
                    currentComments = commentManager.findComments(ModuleType.collaboration, moduleId, null, queryParams, isHistory);
                }

                bindFatherSonRelation(currentComments);

                packageComment(resultMap, currentComments);
            }

            //组装树
            for (Integer forwardCount : resultMap.keySet()) {
                if (forwardCount == 0) {
                    Map<Integer, List<Comment>> comMap = resultMap.remove(0);
                    for (Integer ctype : comMap.keySet()) {
                        if (ctype == 0) {
                            commentList = comMap.get(ctype);
                        } else if (ctype == -1) {
                            commentSenderList = comMap.get(ctype);
                        }
                    }
                    break;
                }
            }
        }
        boolean isSenderAnchor = false;
        if (anchorCommentId != null && Strings.isNotEmpty(commentSenderList)) {
            for (Comment c : commentSenderList) {
                if (c.getId().equals(anchorCommentId)) {
                    isSenderAnchor = true;
                    break;
                }
            }
        }

        List needDelete = new ArrayList();

        //锚点定位
        if (anchorCommentId != null && !isSenderAnchor) {
            boolean hasFind = false;
            if (Strings.isNotEmpty(commentList) && allMorePageSize) {
                for (Iterator<Comment> it = commentList.iterator(); it.hasNext(); ) {
                    Comment c = it.next();
                    List<Comment> childrens = c.getChildren();
                    boolean isChild = false;
                    if (Strings.isNotEmpty(childrens)) {
                        for (Comment child : childrens) {
                            if (child.getId().equals(anchorCommentId)) {
                                isChild = true;
                                break;
                            }
                        }
                    }
                    if (c.getId().equals(anchorCommentId) || isChild) {
                        hasFind = true;
                    } else {
                        //it.remove();
                        needDelete.add(c);
                    }
                }
            }

            Integer _count = allCommentCount.get(0);
            if (_count != null && _count > 1) {
                currentHasNext = true; //相当于给了一个默认值
            }

            //流程追索穿透的时候 存在锚点参数 但是不会意见定位
            if (hasFind) {
                commentList.removeAll(needDelete);
            }
            if (null == _count) {
                _count = 0;
            }
            if ((!hasFind && _count != null && _count <= pageSize) || !allMorePageSize) {
                currentHasNext = false;
            }

        }

        //summary.REPLY_COUNTS 为空，老数据没有升级的时候，做一次兼容。
        if (summaryCommentsCount == 0 && Strings.isNotEmpty(commentList)) {
            compatibleOldNullData(commentList.size(), moduleId, isHistory, allMorePageSize);
        }


        //Convert转为前台需要的数据格式
        ConvertVO(resultMap, commentList, commentSenderList);

        Map<String, Object> retMap = new HashMap<String, Object>();
        /**项目：徐矿集团  【协同处理意见】 作者：jiangchenxi 时间：2019年3月11日 start*/
        //commentList是意见区具体意见
        // allCommentCount数量
        List<Comment> comments = new ArrayList<Comment>();
        if (commentList != null) {
            /**项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 start*/
            //当前affairId
            String string = params.get("affairId");
            if (Strings.isNotBlank(string)) {
                Long affairId = Long.valueOf(params.get("affairId"));
                List<CtpAffair> affairs = affairManager.getAffairs(moduleId);
                boolean open = xkjtManager.isOpen(affairId);
                if (!open) {
                    comments = xkjtManager.getOpenOpinions1(commentList, affairId, 2);
                    commentList = new ArrayList<Comment>();
                    commentList = comments;
                }

            }
            /**项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 end*/
            allCommentCount.put(0, commentList.size());
        }
        /**项目：徐矿集团  【协同处理意见】 作者：jiangchenxi 时间：2019年3月11日 end*/
        retMap.put("commentList", commentList); //当前意见
        retMap.put("commentSenderList", commentSenderList); //发起人附言
        retMap.put("commentForwardMap", resultMap); //转发的意见
        retMap.put("praiseToSumNum", praiseToSumNum); //当前意见的赞数
        retMap.put("allPraiseCountMap", forwardPraise); //转发意见的赞数
        retMap.put("allCommentCountMap", allCommentCount); //意见总数的计数
        retMap.put("forwardCountKeys", Strings.join(forwardCountKeys, ","));

        //校验是否有下一页的数据

        retMap.put("currentHasNext", String.valueOf(currentHasNext));//当前意见是否有下一页
        retMap.put("forwardHasNext", String.valueOf(forwardHasNext)); //校验转发区是否有下一页


        //TODO 是不是还有更多数据
        return retMap;
    }

    private void compatibleOldNullData(int cnt, Long moduleId, boolean isHistory, boolean allMorePageSize) {
        int _cnt = cnt;
        if (allMorePageSize) {
            try {
                Map<Integer, Map<Integer, List<Comment>>> resultMap =
                        commentManager.getCommentsWithForward(ModuleType.collaboration, moduleId, isHistory);
                List<Comment> commentList = (List<Comment>) AppContext.getRawRequest().getAttribute("commentList");
                _cnt = commentList.size();
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }
        colDao.updateColSummaryReplyCounts(moduleId, _cnt);
    }

    private void parsePraiseCntMap(Map<Integer, Integer> forwardPraise, Map<Integer, Map<Integer, Integer>> cntMap) {
        Collection<Integer> a = cntMap.keySet();
        if (Strings.isNotEmpty(a)) {
            for (Integer forwardCountKey : a) {
                Map<Integer, Integer> m = cntMap.get(forwardCountKey);
                Collection<Integer> ctypes = m.keySet();
                for (Integer ctype : ctypes) {
                    int count = m.get(ctype) == null ? 0 : m.get(ctype);
                    if (ctype.equals(Comment.CommentType.comment.getKey())) {
                        forwardPraise.put(forwardCountKey, count);
                    }

                }
            }
        }
    }

    private void ConvertVO(Map<Integer, Map<Integer, List<Comment>>> resultMap, List<Comment> commentList, List<Comment> commentSenderList) {
        Map<Integer, Map<Integer, List<Comment>>> retVOMap = new HashMap<Integer, Map<Integer, List<Comment>>>();
        Set<Integer> keys = resultMap.keySet();
        if (keys != null) {
            for (Integer forwardCount : keys) {
                Map<Integer, List<Comment>> m = resultMap.get(forwardCount);
                Set<Integer> ctypes = m.keySet();
                for (Integer ctype : ctypes) {
                    List<Comment> list = m.get(ctype);
                    for (Comment c : list) {
                        ConvertCommentAttr(c);
                    }
                }
            }
        }
        if (Strings.isNotEmpty(commentList)) {
            for (Comment c : commentList) {
                ConvertCommentAttr(c);
            }
        }
        if (Strings.isNotEmpty(commentSenderList)) {
            for (Comment c : commentSenderList) {
                ConvertCommentAttr(c);
            }
        }
    }

    private void ConvertCommentAttr(Comment c) {
        repairAttachment(c);
        if (null != c.getCreateId()) {
            c.setAvatarImageUrl(OrgHelper.getAvatarImageUrl(c.getCreateId()));
        }
    }

    private Map<String, Object> findSummaryCommentsByPage(FlipInfo fpi, Boolean isHistory, Long moduleId, List<Integer> forwardCountKeys, Long anchorCommentId) throws BusinessException {

        Map<String, Object> retMap = new HashMap<String, Object>();

        //1、查一页的评论

        Map<String, Object> queryParams = new HashMap<String, Object>();
        List<Integer> ctypes = new ArrayList<Integer>();
        Long replyCommentId = anchorCommentId; //锚点意见对应的评论的意见Id,如果是
        if (null != anchorCommentId) {
            Comment parComment = commentManager.getComment(anchorCommentId);
            if (parComment != null && Integer.valueOf(Comment.CommentType.sender.getKey()).equals(parComment.getCtype())) {
                replyCommentId = null;
            } else {
                if (null != parComment && null != parComment.getPid() && 0l != parComment.getPid().longValue()) {
                    replyCommentId = parComment.getPid();
                }
                ctypes.add(Comment.CommentType.reply.getKey());
            }

        }
        ctypes.add(Comment.CommentType.comment.getKey());
        ctypes.add(Comment.CommentType.sender.getKey());
        queryParams.put("ctypes", ctypes);
        queryParams.put("forwardCounts", forwardCountKeys);
        queryParams.put("qMoreOne", true);
        queryParams.put("anchorReplyCommentId", replyCommentId);
        List<Comment> comments = commentManager.findComments(ModuleType.collaboration, moduleId, fpi, queryParams, isHistory);

        boolean next = comments.size() == fpi.getSize() + 1;
        if (next && null == anchorCommentId) {
            comments.remove(comments.size() - 1);
        }

        //2、查一页的评论对应的回复意见，锚点就不需要再查询一次了，前面SQL一次性查下出来了
        if (anchorCommentId == null) {
            List<Long> parentIds = new ArrayList<Long>();
            if (Strings.isNotEmpty(comments)) {
                for (Comment c : comments) {
                    parentIds.add(c.getId());
                }
            }
            if (Strings.isNotEmpty(parentIds)) {
                Map<Long, List<Comment>> replyMap = commentManager.findCommentReplay(parentIds);

                List<Comment> replys = getReplys(replyMap);
                comments.addAll(replys);
            }

        }


        retMap.put("CommentList", comments);
        retMap.put("Next", next);
        return retMap;
    }

    private void packageComment(Map<Integer, Map<Integer, List<Comment>>> resultMap, List<Comment> comments) {
        if (Strings.isNotEmpty(comments)) {
            for (Comment comment : comments) {
                Integer forwardCount = comment.getForwardCount();

                //默认转发次数为0
                if (forwardCount == null)
                    forwardCount = 0;
                Map<Integer, List<Comment>> curMap = resultMap.get(forwardCount);
                if (curMap == null) {
                    curMap = new HashMap<Integer, List<Comment>>();
                    resultMap.put(forwardCount, curMap);
                }
                Integer ctype = comment.getCtype();
                if (ctype == null) //回复和评论在一个List中，为创建树结构做准备
                    ctype = 0;
                List<Comment> comList = curMap.get(ctype);
                if (comList == null) {
                    comList = new ArrayList<Comment>();
                    curMap.put(ctype, comList);
                }
                comment.setCreateName(Functions.showMemberName(comment.getCreateId()));
                comList.add(comment);
            }
        }
    }

    /**
     * @param replyMap
     * @return
     */
    private List<Comment> getReplys(Map<Long, List<Comment>> replyMap) {
        Collection<List<Comment>> values = replyMap.values();
        List<Comment> ret = new ArrayList<Comment>();
        if (Strings.isNotEmpty(values)) {
            for (List<Comment> c : values) {
                ret.addAll(c);
            }
        }
        return ret;
    }

    private void bindFatherSonRelation(List<Comment> comList) {
        if (comList != null) {
            Map<Long, Comment> allCommentMap = new HashMap<Long, Comment>();
            for (Comment com : comList) {
                allCommentMap.put(com.getId(), com);
            }

            for (Comment c : comList) {
                Long pid = c.getPid();
                if (pid != null && pid != 0) {
                    Comment parent = allCommentMap.get(pid);
                    if (parent != null) {
                        parent.addChild(c);
                    }
                }
            }
        }
    }

    private void repairAttachment(Comment comment) {
        String atts = comment.getRelateInfo();
        if (Strings.isNotBlank(atts) && atts.indexOf(":") != -1) {
            try {
                List list = JSONUtil.parseJSONString(atts, List.class);
                List<Attachment> l = ParamUtil.mapsToBeans(list, Attachment.class, false);
                l = attachmentManager.setOfficeTransformEnable(l);
                comment.setRelateInfo(JSONUtil.toJSONString(l));
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    private void checkFormSubject(ColSummary summary, CtpAffair affair, Map<String, Object> tempMap) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("trackParam", tempMap.get("trackParam"));

        //取出模板信息
        params.put("templateColSubject", tempMap.get("templateColSubject"));
        params.put("templateWorkflowId", tempMap.get("templateWorkflowId"));
        //检查协同标题
        checkCollSubject(summary, affair, params);
    }

    public void updateColSummaryProcessNodeInfos(String processId, String nodeInfos) {
        colDao.updateColSummaryProcessNodeInfos(processId, nodeInfos);
    }

    public LockManagerImpl getLockManager() {
        return lockManager;
    }

    public void setLockManager(LockManagerImpl lockManager) {
        this.lockManager = lockManager;
    }


    public void deleteCollDatasPhysically(Long formAppId) throws BusinessException {

        List<CtpTemplate> templates = templateManager.getCtpTemplates(formAppId);
        if (Strings.isEmpty(templates)) {
            return;
        }

        List<ColSummary> summarys = this.getColSummarysByFormAppId(formAppId);
        for (ColSummary summary : summarys) {
            List<CtpAffair> affairs = affairManager.getAffairs(summary.getId());
            //删除Col_SUMMARY
            this.deleteColSummaryUseHqlById(summary.getId());

            //删除Ctp_Affair
            affairManager.deletePhysicalByObjectId(summary.getId());


            //删除意见
            commentManager.deleteCommentAllByModuleId(ModuleType.collaboration, summary.getId());

            //删除正文
            ctpMainbodyManager.deleteContentAllByModuleId(ModuleType.collaboration, summary.getId());

            //删除运行中流程的相关数据
            if (Strings.isNotBlank(summary.getProcessId()) && Strings.isDigits(summary.getProcessId())) {
                wapi.deleteWorkflowRunningDatasByProcessId(Long.valueOf(summary.getProcessId()), summary.getCaseId());
            }

            //删除督办
            CtpSuperviseDetail detail = superviseManager.getSupervise(SuperviseEnum.EntityType.summary.ordinal(), summary.getId());
            //人员
            if (null != detail) {
                superviseManager.deleteSupervisorsByDetailId(detail.getId());
                DBAgent.delete(detail);
            }

            //删除定时任务
            if (Strings.isNotEmpty(affairs)) {
                ColUtil.deleteQuartzJobForNodes(affairs);
            }

        }

    }

    @Override
    public void showWFCDiagram(Long affairId, String openFrom) throws BusinessException {
        //记录查看次数
        if (AppContext.hasPlugin("ai")) {
            CtpAffair affair = affairManager.get(affairId);
            if (affair != null) {
                AIRemindEvent remindEvt = new AIRemindEvent(this);
                remindEvt.setAffairId(affair.getId());
                remindEvt.setObjectId(affair.getObjectId());
                remindEvt.setState(affair.getState());
                remindEvt.setSubject(affair.getSubject());
                remindEvt.setOpenFrom(openFrom);
                remindEvt.setTrack(affair.getTrack());
                remindEvt.setMemberId(affair.getMemberId());
                EventDispatcher.fireEvent(remindEvt);
            }
        }
    }

    @Override
    @AjaxAccess
    public void logErrorJs(String jsErrorMsg, Long affairId, String subject) throws BusinessException {
        LOG.error(AppContext.currentUserName() + ",affairId:" + affairId + ",subejct:" + subject + "[\"处理时PC端js报错日志]：" + jsErrorMsg);
    }

    @Override
    @AjaxAccess
    public String checkNodePolicyChange(String affairId, String pageNodePolicy) throws BusinessException {
        CtpAffair affair = getSimpleAffair4Check(affairId);
        return checkNodePolicyChange(affair, pageNodePolicy);
    }


    private String checkNodePolicyChange(CtpAffair affair, String pageNodePolicy) throws BusinessException {
        String errorMsg = "";
        if (affair == null
                || affair.getState().equals(StateEnum.col_waitSend.getKey())
                || affair.getState().equals(StateEnum.col_sent.getKey())) {
            return errorMsg;
        }
        SeeyonPolicy affairNodePolicy = ColUtil.getPolicyByAffair(affair);
        if (affair != null && Strings.isNotBlank(pageNodePolicy) && !affairNodePolicy.getId().equals(pageNodePolicy)) {
            errorMsg = ResourceUtil.getString("collaboration.nodePolicy.change");
        }
        return errorMsg;
    }

}
