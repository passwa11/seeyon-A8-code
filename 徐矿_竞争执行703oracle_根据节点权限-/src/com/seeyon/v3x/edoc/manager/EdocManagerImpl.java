package com.seeyon.v3x.edoc.manager;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.temp.manager.XkjtTempManager;
import com.seeyon.apps.ext.temp.manager.XkjtTempManagerImpl;
import com.seeyon.apps.ext.temp.po.XkjtTemp;
import com.seeyon.v3x.edoc.domain.EdocSummaryExtend;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.condition.HasFreeSpace;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.bulletin.api.BulletinApi;
import com.seeyon.apps.bulletin.bo.BulTypeBO;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.collaboration.trace.enums.WFTraceConstants;
import com.seeyon.apps.collaboration.trace.enums.WorkflowTraceEnums;
import com.seeyon.apps.collaboration.trace.manager.TraceWorkflowDataManager;
import com.seeyon.apps.collaboration.util.AttachmentEditUtil;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.enums.EdocEnum.SendType;
import com.seeyon.apps.edoc.event.EdocAffairsAssignedEvent;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.apps.xkjt.dao.XkjtDao;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentEditHelper;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.HandWriteManager;
import com.seeyon.ctp.common.office.OfficeLockManager;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.processlog.ProcessLog;
import com.seeyon.ctp.common.po.processlog.ProcessLogDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.quartz.MutiQuartzJobNameException;
import com.seeyon.ctp.common.quartz.NoSuchQuartzJobBeanException;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateStateEnums;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.track.dao.CtpTrackMemberDao;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgTeam;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.manager.WorkflowAjaxManager;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.search.manager.SearchManager;
import com.seeyon.v3x.edoc.constants.EdocEventDataContext;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDateFormatSetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDisplaySetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionShowNameTypeEnum;
import com.seeyon.v3x.edoc.dao.EdocBodyDao;
import com.seeyon.v3x.edoc.dao.EdocFormFlowPermBoundDao;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocFormExtendInfo;
import com.seeyon.v3x.edoc.domain.EdocFormFlowPermBound;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.event.EdocFinishEvent;
import com.seeyon.v3x.edoc.event.EdocStartEvent;
import com.seeyon.v3x.edoc.event.EdocStepBackEvent;
import com.seeyon.v3x.edoc.event.EdocStopEvent;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.DateUtil;
import com.seeyon.v3x.edoc.util.EdocInfo;
import com.seeyon.v3x.edoc.util.EdocSuperviseHelper;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.MetaUtil;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.edoc.webmodel.MessageCommentParam;
import com.seeyon.v3x.edoc.webmodel.MoreSignSelectPerson;
import com.seeyon.v3x.edoc.webmodel.SummaryModel;
import com.seeyon.v3x.edoc.workflow.event.EdocWorkflowEventListener;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.manager.EdocExchangeTurnRecManager;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.manager.SendEdocManager;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;
import com.seeyon.v3x.system.signet.dao.DocumentSignatureDao;
import com.seeyon.v3x.system.signet.domain.V3xDocumentSignature;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.domain.V3xSignet;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.SignetManager;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.engine.wapi.WorkItem;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;


public class EdocManagerImpl implements EdocManager {

    private final static Log LOGGER = LogFactory.getLog(EdocManagerImpl.class);
    protected ProcessLogManager processLogManager;
    private AppLogManager appLogManager;
    private EdocExchangeTurnRecManager edocExchangeTurnRecManager;
    protected EdocSummaryDao edocSummaryDao;
    private EdocOpinionDao edocOpinionDao;
    private CtpTrackMemberDao trackdao;
    protected AffairManager affairManager;
    protected UserMessageManager userMessageManager = null;
    private SearchManager searchManager = null;
    private AttachmentManager attachmentManager;
    protected FileManager fileManager;
    private EdocBodyDao edocBodyDao;
    private SendEdocManager sendEdocManager;
    protected EnumManager enumManagerNew;
    protected OrgManager orgManager;
    private EdocStatManager edocStatManager;
    private EdocSuperviseManager edocSuperviseManager;
    private EdocFormManager edocFormManager;
    private PermissionManager permissionManager;
    private TemplateManager templateManager;
    private WorkTimeManager workTimeManager;
    private EdocElementManager edocElementManager;
    private EdocMarkManager edocMarkManager;
    private EdocMarkHistoryManager edocMarkHistoryManager;
    private DocumentSignatureDao documentSignatureDao;
    private ISignatureHtmlManager iSignatureHtmlManager;
    private EdocSummaryManager edocSummaryManager;
    private EdocSummaryExtendManager edocSummaryExtendManager;
    private SignetManager signetManager = null;
    private DocApi docApi;
    private BulletinApi bulletinApi;
    private HandWriteManager handWriteManager = null;
    private RecieveEdocManager recieveEdocManager;
    private OfficeLockManager officeLockManager;
    private EdocFormFlowPermBoundDao edocFormFlowPermBoundDao;
    private CtpTrackMemberManager trackManager;

    private XkjtManager xkjtManager;


    public XkjtManager getXkjtManager() {
        return xkjtManager;
    }

    public void setXkjtManager(XkjtManager xkjtManager) {
        this.xkjtManager = xkjtManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setEdocFormFlowPermBoundDao(EdocFormFlowPermBoundDao edocFormFlowPermBoundDao) {
        this.edocFormFlowPermBoundDao = edocFormFlowPermBoundDao;
    }

    public void setEdocExchangeTurnRecManager(EdocExchangeTurnRecManager edocExchangeTurnRecManager) {
        this.edocExchangeTurnRecManager = edocExchangeTurnRecManager;
    }

    public void setOfficeLockManager(OfficeLockManager officeLockManager) {
        this.officeLockManager = officeLockManager;
    }

    public void setHandWriteManager(HandWriteManager handWriteManager) {
        this.handWriteManager = handWriteManager;
    }

    public void setBulletinApi(BulletinApi bulletinApi) {
        this.bulletinApi = bulletinApi;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public void setSignetManager(SignetManager signetManager) {
        this.signetManager = signetManager;
    }

    public void setDocumentSignatureDao(DocumentSignatureDao documentSignatureDao) {
        this.documentSignatureDao = documentSignatureDao;
    }

    protected SuperviseManager superviseManager;

    private TraceWorkflowDataManager edocTraceWorkflowManager;


    public TraceWorkflowDataManager getEdocTraceWorkflowManager() {
        return edocTraceWorkflowManager;
    }

    public void setEdocTraceWorkflowManager(
            TraceWorkflowDataManager edocTraceWorkflowManager) {
        this.edocTraceWorkflowManager = edocTraceWorkflowManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    private EdocRegisterManager edocRegisterManager;
    private EdocMarkDefinitionManager edocMarkDefinitionManager;
    private V3xHtmDocumentSignatManager htmSignetManager;
    private EdocMessagerManager edocMessagerManager;

    private WorkflowApiManager wapi;

    public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
        this.edocMarkDefinitionManager = edocMarkDefinitionManager;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public void setEdocMessagerManager(EdocMessagerManager edocMessagerManager) {
        this.edocMessagerManager = edocMessagerManager;
    }

    private IndexManager indexManager;

    public IndexManager getIndexManager() {
        return indexManager;
    }

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    public CtpTrackMemberDao getTrackdao() {
        return trackdao;
    }

    public void setTrackdao(CtpTrackMemberDao trackdao) {
        this.trackdao = trackdao;
    }

    private EdocCategoryManager edocCategoryManager;
    public static final String PAGE_TYPE_DRAFT = "draft";
    public static final String PAGE_TYPE_SENT = "sent";
    public static final String PAGE_TYPE_PENDING = "pending";
    public static final String PAGE_TYPE_FINISH = "finish";


    public void setEdocCategoryManager(EdocCategoryManager edocCategoryManager) {
        this.edocCategoryManager = edocCategoryManager;
    }

    public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setEdocFormManager(EdocFormManager edocFormManager) {
        this.edocFormManager = edocFormManager;
    }

    public void setEdocStatManager(EdocStatManager edocStatManager) {
        this.edocStatManager = edocStatManager;
    }

    public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
        this.htmSignetManager = htmSignetManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

    public void setSendEdocManager(SendEdocManager sendEdocManager) {
        this.sendEdocManager = sendEdocManager;
    }

    public void setEdocBodyDao(EdocBodyDao edocBodyDao) {
        this.edocBodyDao = edocBodyDao;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public void setEdocOpinionDao(EdocOpinionDao edocOpinionDao) {
        this.edocOpinionDao = edocOpinionDao;
    }

    public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
        this.edocSummaryDao = edocSummaryDao;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

	/*public void setSignetManager(SignetManager signetManager) {
		this.signetManager = signetManager;
	}*/

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public void setWorkTimeManager(WorkTimeManager workTimeManager) {
        this.workTimeManager = workTimeManager;
    }

    public EdocSummaryExtendManager getEdocSummaryExtendManager() {
        return edocSummaryExtendManager;
    }

    public void setEdocSummaryExtendManager(
            EdocSummaryExtendManager edocSummaryExtendManager) {
        this.edocSummaryExtendManager = edocSummaryExtendManager;
    }


    @Override
    public EdocForm getNewDefaultEdocForm(int iEdocType, Long subType, User user) {
        String isOpen = SystemProperties.getInstance().getProperty("edoc.hasEdocCategory");
        boolean isG6 = EdocHelper.isG6Version();
        subType = (isG6 && Boolean.valueOf(isOpen)) ? subType : -1;
        EdocForm defaultEdocForm = null;
        EdocCategory edocCategory = null;
        if (iEdocType == EdocEnum.edocType.sendEdoc.ordinal()) {
            List<EdocCategory> categoryList = edocCategoryManager.getCategoryByAccount(user.getLoginAccount());
            if (subType == -1) {
                //GOV-3490 【公文管理】-【发文管理】-【拟文】，进入拟文界面时循环弹出提示框'该类型下没找到公文单'
                //依次找每个类型下的文单
                for (int i = 0; i < categoryList.size(); i++) {
                    edocCategory = categoryList.get(i);
                    //subType = edocCategory.getId();
                    defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(), iEdocType, edocCategory.getId());
                    if (defaultEdocForm != null && defaultEdocForm.getIsDefault() && EdocForm.C_iStatus_Published.equals(defaultEdocForm.getStatus())) {
                        break;
                    }
                }
            } else {
                edocCategory = edocCategoryManager.getCategoryById(subType);
                defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(), iEdocType, subType);
                //当选择的文单类型下没有文单时，就要遍历所有文单类型依次查找了
                //GOV-3490 【公文管理】-【发文管理】-【拟文】，进入拟文界面时循环弹出提示框'该类型下没找到公文单'
                if (defaultEdocForm == null) {
                    //依次找每个类型下的文单
                    for (int i = 0; i < categoryList.size(); i++) {
                        edocCategory = categoryList.get(i);
                        subType = edocCategory.getId();
                        defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(), iEdocType, subType);
                        if (defaultEdocForm != null) {
                            break;
                        }
                    }
                }
            }
        }
        if (defaultEdocForm == null) {
            defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(), iEdocType);
        }

        if (defaultEdocForm != null && defaultEdocForm.getIsSystem() && Strings.isBlank(defaultEdocForm.getContent())) {
            edocFormManager.updateFormContentToDBOnly();
            defaultEdocForm = edocFormManager.getEdocForm(defaultEdocForm.getId());
        }
        return defaultEdocForm;
    }

    /**
     *
     */
    public FormOpinionConfig getEdocFormOpinionConfig(EdocSummary summary) {
        Long flowPermAccout = EdocHelper.getFlowPermAccountId(AppContext.getCurrentUser().getLoginAccount(), summary, templateManager);
        FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(), flowPermAccout);
        return displayConfig;
    }

    @Override
    public Hashtable getEdocOpinion(Long edocFormId, LinkedHashMap hsOpinion) throws EdocException {
        User user = AppContext.getCurrentUser();
        return getEdocOpinion(edocFormId, user.getLoginAccount(), hsOpinion);
    }

    @Override
    public Hashtable getEdocOpinion(Long edocFormId, Long aclAccountId, LinkedHashMap hsOpinion) throws EdocException {

        Hashtable locOpin = new Hashtable();
        Hashtable<String, String> locHs = edocFormManager.getOpinionLocation(edocFormId, aclAccountId);
        Iterator en = hsOpinion.keySet().iterator();
        String key = "";
        String value = "";
        String local = "";
        //发起人意见是否绑定
        boolean setSendLoca = false;
        while (en.hasNext()) {
            StringBuilder tempValue = new StringBuilder("");
            key = en.next().toString();
            if ("senderOpinionList".equals(key) || "senderOpinionAttStr".equals(key)) {
                continue;
            }
            value = (String) hsOpinion.get(key);
            if (value == null || "".equals(value)) {
                continue;
            }
            String opinionKey = key.split("_")[0];
            // tempLoacl格式 公文元素名称为value_公文元素的排序方式
            String tempLocal = locHs.get(opinionKey);
            local = locHs.get(opinionKey);
            // 公文元素名称
            if (local != null) local = tempLocal.split("_")[0];
            // 公文元素绑定的排序方式
            //String sortType = tempLocal.split("_")[1];

            //没有设置意见放置位置的，统一放到其它意见，不在按节点权限放置到前台匹配;发起人附言单独处理,如果没有设置绑定就不放入公文单
            if (local == null || "".equals(local)) {
                if ("niwen".equals(opinionKey) || "dengji".equals(opinionKey)) {
                    continue;
                }
                local = "otherOpinion";
            } else {
                //发起人附言已经绑定到公文单，不再显示到公文单下面，从对象中删除
                if ("niwen".equals(opinionKey) || "dengji".equals(opinionKey)) {
                    setSendLoca = true;
                }
            }
            String newValue = locOpin.get(local) != null ? locOpin.get(local).toString() : null;
            if (newValue != null && !"".equals(newValue)) {
                tempValue.append(newValue);
                tempValue.append("<br><br>");
            }
            tempValue.append(value);
            locOpin.put(local, tempValue.toString());
        }
        if (hsOpinion.get("senderOpinionList") != null && setSendLoca == false) {
            locOpin.put("senderOpinionList", hsOpinion.get("senderOpinionList"));
        }
        return locOpin;
    }

    public LinkedHashMap getEdocOpinion(Long summaryId, Long curUser, Long sender, String from) throws EdocException {
        EdocSummary summary = edocSummaryDao.get(summaryId);
        return getEdocOpinion(summary, summary.getOrgAccountId(), curUser, sender, from);
    }

    public LinkedHashMap getEdocOpinion(EdocSummary summary, Long aclAccountId, Long curUser, Long sender, String from) throws EdocException {
        //EdocSummary summary=edocSummaryDao.get(summaryId);
        LinkedHashMap hs = new LinkedHashMap();

        List<EdocOpinion> senderOpinions = new ArrayList<EdocOpinion>();
        StringBuilder sb = new StringBuilder();
        String attitude = "";
        String content = "";
        Object value = null;
        List<Attachment> tempAtts = attachmentManager.getByReference(summary.getId());
        Hashtable<Long, List<Attachment>> attHas = com.seeyon.ctp.common.filemanager.manager.Util.sortBySubreference(tempAtts);
        boolean isHidden = false;
        ResourceBundle r = null;
        V3xOrgMember member = null;
        Map senderAttMap = new HashMap();

        boolean showDate = false;
        boolean showDateTime = false;
        boolean showDept = false;
        boolean showLastOptionOnly = false;
        // 取得公文单的意见元素的绑定关系，key是FlowPermName，value是公文元素名称为value_公文元素的排序方式
        Hashtable<String, String> locHs = edocFormManager
                .getOpinionLocation(summary.getFormId(), aclAccountId);
        // 公文单显示格式
        String optionFormatSet = null;
        EdocForm form = edocFormManager.getEdocForm(summary.getFormId());
        Set<EdocFormExtendInfo> infos = form.getEdocFormExtendInfo();
        for (EdocFormExtendInfo info : infos) {
            if (info.getAccountId().equals(summary.getOrgAccountId())) {
                optionFormatSet = info.getOptionFormatSet();
            }
        }

        if (optionFormatSet == null) {
            optionFormatSet = FormOpinionConfig.getDefualtConfig();
        }

        //OA-33698 wangchw发起签报后，在已发中撤销后再待发查看拟文意见的态度显示撤销，也显示了处理人的部门，但是编辑页面态度显示已阅，且无部门
        FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(), aclAccountId);

        if (Strings.isNotBlank(optionFormatSet)) {

            FormOpinionConfig tempConfig = null;
            try {
                tempConfig = JSONUtil.parseJSONString(optionFormatSet, FormOpinionConfig.class);
            } catch (Exception e) {
                tempConfig = new FormOpinionConfig();
            }
            if (tempConfig == null) {
                tempConfig = new FormOpinionConfig();
            }
            showLastOptionOnly = OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(tempConfig.getOpinionType());

            //OA-33698 wangchw发起签报后，在已发中撤销后再待发查看拟文意见的态度显示撤销，也显示了处理人的部门，但是编辑页面态度显示已阅，且无部门
            if (tempConfig.isShowDept() || displayConfig.isShowDept()) {
                showDept = true;
            }
            showDate = OpinionDateFormatSetEnum.DATE.getValue().equals(tempConfig.getShowDateType());
            if (!showDate) {
                showDateTime = true;
            }
        }


        List<EdocOpinion> tempResult = new ArrayList<EdocOpinion>();   //查询出来的意见
        List<String> boundFlowPerm = new ArrayList<String>();   //绑定的节点权限
        //Map<意见元素名称，List<绑定的节点权限>>因为一个意见元素可以绑定多个节点权限
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        Map<String, String> sortMap = new HashMap<String, String>();
        //绑定部分的意见
        for (Iterator keyName = locHs.keySet().iterator(); keyName.hasNext(); ) {
            String flowPermName = (String) keyName.next();
            if (!boundFlowPerm.contains(flowPermName)) boundFlowPerm.add(flowPermName);
            // tempLoacl格式 公文元素名称为value_公文元素的排序方式
            String tempLocal = locHs.get(flowPermName);
            String elementOpinion = tempLocal.split("_")[0];//公文元素名,例如公文单上的shenpi这个公文元素
            //取到指定公文元素绑定的节点权限列表
            List<String> flowPermsOfSpecialElement = map.get(elementOpinion);
            if (flowPermsOfSpecialElement == null) {
                flowPermsOfSpecialElement = new ArrayList<String>();
            }
            flowPermsOfSpecialElement.add(flowPermName);
            map.put(elementOpinion, flowPermsOfSpecialElement);

            // 公文元素绑定的排序方式
            String sortType = tempLocal.split("_")[1];
            sortMap.put(elementOpinion, sortType);
        }

        boolean hasFeedBack = false;
        List<EdocElement> feedbackList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), EdocOpinion.FEED_BACK);
        if (Strings.isNotEmpty(feedbackList)) {
            hasFeedBack = true;
        }

        if (hasFeedBack) {
            List<String> feedbackPolicy = new ArrayList<String>(1);
            feedbackPolicy.add(EdocOpinion.FEED_BACK);
            map.put(EdocOpinion.FEED_BACK, feedbackPolicy);
            if (!boundFlowPerm.contains(EdocOpinion.FEED_BACK)) {
                boundFlowPerm.add(EdocOpinion.FEED_BACK);
            }
        }

        Set<String> bound = map.keySet(); //绑定的公文元素
        for (String s : bound) {
            tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
                    map.get(s), sortMap.get(s), showLastOptionOnly, true));
        }
        //查询非绑定意见
        tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
                boundFlowPerm, "0", showLastOptionOnly, false));

        // 根据公文单的edoc_id,policy查询出指定节点的公文元素
        //  同一个人的意见，在勾选了只显示一条的时候，只显示一条

        for (int i = 0; i < tempResult.size(); i++) {
            EdocOpinion opinion = tempResult.get(i);

            attitude = null;
            // 公文单不显示暂存待办意见
            if (opinion.getOpinionType() == EdocOpinion.OpinionType.provisionalOpinoin
                    .ordinal()) {
                continue;
            }
            if (opinion.getAttribute() > 0) {
                if (EdocOpinion.OpinionType.backOpinion.ordinal() == opinion.getOpinionType().intValue()) {
                    attitude = "stepBack.label";
                }
                //OA-33698  wangchw发起签报后，在已发中撤销后再待发查看拟文意见的态度显示撤销，也显示了处理人的部门，但是编辑页面态度显示已阅，且无部门  start
                else if (OpinionType.stopOpinion.ordinal() == opinion.getOpinionType().intValue()) {
                    attitude = "stepStop.label";
                } else if (OpinionType.repealOpinion.ordinal() == opinion.getOpinionType().intValue()) {
                    attitude = "edoc.repeal.2.label";
                }
                //OA-33698  wangchw发起签报后，在已发中撤销后再待发查看拟文意见的态度显示撤销，也显示了处理人的部门，但是编辑页面态度显示已阅，且无部门   end
                else {
                    attitude = enumManagerNew.getEnumItemLabel(
                            EnumNameEnum.collaboration_attitude, Integer
                                    .toString(opinion.getAttribute()));
                }
            }
            if (attitude != null && !"".equals(attitude)) {
                attitude = ResourceUtil.getString(attitude);
            } else if (attitude != null
                    && Integer.valueOf(attitude).intValue() == com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL) {
                attitude = null;
            }
            if (opinion.getOpinionType() == EdocOpinion.OpinionType.senderOpinion
                    .ordinal()) {
                opinion.setOpinionAttachments(attHas.get(opinion.getId()));
                senderOpinions.add(opinion);
                attitude = null;
                // continue;
            }
            sb.delete(0, sb.length());
            StringBuilder key = null;
            String tempKey = opinion.getPolicy();
            if (tempKey == null) {
                key = new StringBuilder(summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()
                        ? "dengji" : "niwen");
            } else {
                key = new StringBuilder(tempKey);
            }
            key.append("_");
            key.append(opinion.getId());
            value = hs.get(key);
            if (value != null) {
                sb.append(value.toString());
            }
            if (sb.length() > 0) {
                sb.append("<br>");
            }

            StringBuilder doUserName = new StringBuilder();
            try {
                member = orgManager
                        .getMemberById(opinion.getCreateUserId());
                String tempDoUserName = member.getName();
                if (member.getIsAdmin()) {
                    // 如果是管理员终止，不显示管理员名字及时间
                } else if ("newEdoc".equals(from)) {
                    doUserName.append("<span>")
                            .append(Functions.showMemberNameOnly(opinion.getCreateUserId()))
                            .append("</span>");
                } else {
                    doUserName.append("<span class='link-blue' onclick='javascript:showV3XMemberCard(\"");
                    doUserName.append(opinion.getCreateUserId());
                    doUserName.append("\", 4)'>");
                    doUserName.append(tempDoUserName);
                    doUserName.append("</span>");
                }

                if (!Strings.isBlank(opinion.getProxyName())) {
                    doUserName.append(ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "edoc.opinion.proxy", opinion.getProxyName()));
                }
            } catch (Exception e) {
                throw new EdocException(e);
            }
            isHidden = (opinion.getIsHidden() && opinion.getCreateUserId() != curUser.longValue()
                    && !curUser.equals(sender));
            if (isHidden == false) {
                content = opinion.getContent();
            } else {
                content = ResourceBundleUtil.getString(
                        "com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.opinion.hide.label");
            }
            if (attitude != null) {
                sb.append("【").append(attitude).append("】");
            }
            // 意见排序 ：【态度】 意见 部门 姓名 时间
            sb.append(" ").append(Strings.toHTML(content));
            if (showDept) {
                sb.append(" ").append(opinion.getDepartmentName());
            }
            // 如果是管理员终止，不显示管理员名字及时间
            if (!member.getIsAdmin()) {
                sb.append(" ").append(doUserName);
                if (showDateTime) {
                    sb.append(" ").append(
                            Datetimes.formatDatetimeWithoutSecond(opinion
                                    .getCreateTime()));
                } else if (showDate) {
                    sb.append(" ").append(
                            Datetimes.formatDate(opinion.getCreateTime()));
                }
            }
            if (isHidden == false) {
                // 增加附件
                tempAtts = attHas.get(opinion.getId());
                if (tempAtts != null) {
                    sb.append("<br>");
                    StringBuilder attSb = new StringBuilder();
                    for (Attachment att : tempAtts) {
                        // 不管文件名有多长，显示整体的文件名。yangzd
                        String s = com.seeyon.ctp.common.filemanager.manager.Util
                                .AttachmentToHtmlWithShowAllFileName(att,
                                        true, false);
                        sb.append(s);
                        attSb.append(s);
                    }
                    senderAttMap.put(opinion.getId(), attSb);
                    // sb.append("<br>");
                }
            }
            hs.put(key.toString(), sb.toString());
        }


        hs.put("senderOpinionAttStr", senderAttMap);
        hs.put("senderOpinionList", senderOpinions);
        return hs;
    }


    //lijl添加,为了区分是撤销流程还是取回
    public int cancelSummary(long userId, long summaryId, CtpAffair cancelAffair, String repealComment, String from, EdocOpinion... repealOpinion) throws BusinessException {
        return cancelSummary(userId, summaryId, cancelAffair, StateEnum.col_cancel.key(), repealComment, from, repealOpinion);
    }

    //lijl添加,为了区分是撤销流程还是取回
    public int cancelSummary(long userId, long summaryId, CtpAffair cancelAffair, int from, String repealComment, String docFrom, EdocOpinion... repealOpinion) throws BusinessException {
        return cancelSummary(userId, summaryId, cancelAffair, from, true, repealComment, docFrom, repealOpinion);
    }

    //lijl添加,为了区分是撤销流程还是取回
    public int cancelSummary(long userId, long summaryId, CtpAffair cancelAffair, int from, boolean sendMessage,
                             String repealComment,
                             String docFrom, EdocOpinion... repealOpinions) throws BusinessException {
        boolean isneedrepealRecord = (null != repealOpinions && null != repealOpinions[0] && null != repealOpinions[0].getNeedRepealRecord() && "1".equals(repealOpinions[0].getNeedRepealRecord())) ? true : false;
        int result = 0;
        User user = AppContext.getCurrentUser();
        EdocSummary summary = edocSummaryDao.get(summaryId);
        //GOV-3328 （需求检查）【公文管理】-【收文管理】-【分发】，已分发纸质公文撤销后到待分发列表中了，应该在草稿箱中
        //撤销时，需要将收文的状态设为0,撤销后收文进入草稿箱列表
        if ("cancelColl".equals(docFrom)) {
            summary.setState(CollaborationEnum.flowState.cancel.ordinal());
        }
        Long caseId = summary.getCaseId();
        if (caseId == null)
            return 1;

        ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());

        //将summary的状态改为待�发,撤销已生成事项
        List<CtpAffair> affairs = affairManager.getValidAffairs(app, summary.getId());

        //获得待取回的工作项id
        long cancelWorkitemId = 0L;
        CtpAffair curAffair = null;
        if (repealOpinions != null && repealOpinions.length > 0) {
            curAffair = affairManager.get(repealOpinions[0].getAffairId());
            if (curAffair.getState() != StateEnum.col_sent.getKey()) {
                cancelWorkitemId = curAffair.getSubObjectId();
            }
        } else {
            for (int i = 0; i < affairs.size(); i++) {
                CtpAffair affair = (CtpAffair) affairs.get(i);
                if (affair.getState() == StateEnum.col_pending.key()) {
                    cancelWorkitemId = affair.getSubObjectId();
                    curAffair = affair;
                    break;
                }
            }
        }

        EdocRegister edocRegister = edocRegisterManager.findRegisterByDistributeEdocId(summaryId);
        if (edocRegister != null && !edocRegister.getDistributeState().equals(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal())) {
            edocRegister.setDistributeDate(null);
            edocRegister.setDistributeEdocId(summaryId);
            edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());// 将状态设置为"草稿"
            // 撤销与取回时，登记对象退回状态为：0
            edocRegister.setIsRetreat(0);// 非退回
            edocRegisterManager.update(edocRegister);
        }

        Map<String, Object> traceMap = new HashMap<String, Object>();
        //调用工作流取回接口
        if ("docBack".equals(docFrom)) {
            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setAppName("edoc");
            context.setAppObject(summary);
            context.setCurrentWorkitemId(cancelWorkitemId);
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.CANCEL);
            try {
                result = wapi.takeBack(context);
            } catch (BPMException e) {
                LOGGER.error("公文取回产生异常", e);
            }
            if (result != 0) {
                return result;
            }
        } else {

            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setAppName("edoc");
            context.setCaseId(summary.getCaseId());
            context.setAppObject(summary);
            context.setCurrentWorkitemId(cancelWorkitemId);
            try {
                result = wapi.cancelCase(context);
                this.superviseManager.updateStatus2Cancel(summaryId);
                summary.setCaseId(null);
            } catch (BPMException e) {
                LOGGER.error("公文撤销产生异常", e);
            }
            if (repealOpinions != null && repealOpinions.length > 0) {
                EdocOpinion repealOpinion = repealOpinions[0];
                //OA-19935  客户bug验证：流程是gw1，gw11，m1，串发，m1撤销，gw1在待发直接查看（不是编辑态），文单上丢失了撤销的意见
                repealOpinion.setAttribute(1);//这里必须要设置大于0的值，不然查看意见时 不会显示公文是如何处理的 (比如 撤销)
                repealOpinion.setContent(repealComment);
                repealOpinion.isDeleteImmediate = false;
                repealOpinion.affairIsTrack = false;
                repealOpinion.setIdIfNew();
                if (user.getId().longValue() != curAffair.getMemberId().longValue()) {
                    repealOpinion.setProxyName(user.getName());
                }
                repealOpinion.setCreateUserId(curAffair.getMemberId());
                repealOpinion.setEdocSummary(summary);
                repealOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
                repealOpinion.setOpinionType(EdocOpinion.OpinionType.repealOpinion.ordinal());
                saveOpinion(repealOpinion);
                summary.getEdocOpinions().add(repealOpinion);
                HttpServletRequest _request = AppContext.getRawRequest();
                String page = _request.getParameter("page");

                if ("dealrepeal".equals(page)) {//处理中撤销
                    String oldOpinionIdStr = _request.getParameter("oldOpinionId");
                    if (Strings.isNotBlank(oldOpinionIdStr)) {
                        this.attachmentManager.deleteByReference(summaryId, Long.parseLong(oldOpinionIdStr));
                    }
                    try {
                        this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, repealOpinion.getId(), _request);
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                }

            }


            traceMap = createRepealTraceWfData(summary, affairs, curAffair, isneedrepealRecord ? "1" : "2");

        }

        if (affairs != null) {
            for (int i = 0; i < affairs.size(); i++) {
                CtpAffair affair = (CtpAffair) affairs.get(i);
                //正常的撤销以及指定退回后的撤销都要修改state、subState状态
                if (affair.getState() == StateEnum.col_sent.key() || (affair.getState() == StateEnum.col_waitSend.key() && affair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey())) {
                    affair.setState(StateEnum.col_waitSend.key());
                    //GOV-3328 （需求检查）【公文管理】-【收文管理】-【分发】，已分发纸质公文撤销后到待分发列表中了，应该在草稿箱中
                    if ("cancelColl".equals(docFrom) && (affair.getApp() == ApplicationCategoryEnum.edocRec.getKey() || affair.getApp() == ApplicationCategoryEnum.edocSend.getKey() || affair.getApp() == ApplicationCategoryEnum.edocSign.getKey())) {
                        affair.setSubState(SubStateEnum.col_waitSend_cancel.key());
                    } else {
                        affair.setSubState(SubStateEnum.col_waitSend_draft.key());
                    }

                    affair.setDelete(false);
                    affairManager.updateAffair(affair);

                    //OA-145253
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(affair.getId());
                    //删除归档文档
                    Boolean hasDoc = AppContext.hasPlugin("doc");
                    if (hasDoc && docApi != null) {
                        docApi.deleteDocResources(user.getId(), ids);
                    }

                }

                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                }

                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                }
            }

            this.affairManager.updateAffairsState2Cancel(summaryId);
            this.affairManager.updateAffairSummaryState(summaryId, summary.getState());
        }
        /**
         * 撤销时删除归档路径
         * 原则：
         * 如果不是模板可以置空归档archiveId
         * 如果是模板（模板没有设置预归档路径）可以置空归档archiveId
         */
        Long archiveId = EdocHelper.getTempletePrePigholePath(summary.getTempleteId(), templateManager);
        if (summary.getTempleteId() == null || (summary.getTempleteId() != null && archiveId == null)) {
            summary.setArchiveId(null);
        }

        //summary.setCaseId(null);//不能清空caseId，如果清空了，从待发中直接发送会没有caseId，导致流程报错
        edocSummaryDao.update(summary);
        //wangwei 撤销流程或者退回需要删除督办定时任务
        EdocHelper.deleteQuartzJobOfSummary(summary);
        String key = "edoc.cancel";
        int keyFlag = 1;
        if (Strings.isNotBlank(repealComment)) {
            keyFlag = 2;
        }
        try {
            edocStatManager.deleteEdocStat(summaryId);
//             this.processLogManager.deleteLog(Long.valueOf(summary.getProcessId())) ;--撤销和取回的时候删除了对应的日志
            ProcessLogAction pla = null;//lijl添加,为了区分是撤销流程还是取回
            if ("docBack".equals(docFrom)) {
                pla = ProcessLogAction.takeBack;
                key = "edoc.takeback";
            } else {
                pla = ProcessLogAction.cancelColl;
            }
            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1L, pla);
            this.appLogManager.insertLog(user, AppLogAction.Edoc_Cacel, user.getName(), summary.getSubject());
        } catch (Exception e) {
            LOGGER.error("删除公文统计记录异常", e);
        }

        Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(summary).key;
        //对发起人以外的所有执行人发消息通知
        try {
            String agenName = "";
            String userName = EdocUtil.getAccountName();
            //如果代理人处理，这里要发送（由代理人）处理消息 (判断依据：当前撤销事项的所属人不是撤销操作人)
            if (cancelAffair != null && user.getId().longValue() != cancelAffair.getMemberId().longValue()) {
                key = "edoc.agent.cancel";
                userName = orgManager.getMemberById(cancelAffair.getMemberId()).getName();
                agenName = user.getName();
            }
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
            List<Long> list = new ArrayList<Long>();
            for (CtpAffair affair1 : affairs) {
                Long agentMemberId = null;
                if (affair1.isDelete())
                    continue;
                if (affair1.getMemberId() == userId) {
                    continue;
                }
                if (!list.contains(affair1.getMemberId())) {
                    if (affair1.getState() == StateEnum.col_waitSend.key()) {
                        MessageReceiver receiver = new MessageReceiver(affair1.getId(), affair1.getMemberId(), "message.link.edoc.done", affair1.getId().toString());
                        if (null != affair1.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair1.getTrack())) {
                            receiver.setTrack(true);
                        }
                        receivers.add(receiver);
                    } else {
                        receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId()));
                        agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), affair1.getMemberId());
                        if (agentMemberId != null && !list.contains(agentMemberId)) {
                            MessageReceiver receiver = new MessageReceiver(affair1.getId(), agentMemberId);
                            if (null != affair1.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair1.getTrack())) {
                                receiver.setTrack(true);
                            }
                            receivers1.add(receiver);
                            list.add(agentMemberId);
                        }
                    }
                    list.add(affair1.getMemberId());
                }
            }
            if (repealComment == null) {
                repealComment = "";
            }
            if (receivers != null && receivers.size() != 0) {
                if ("edoc.agent.cancel".equals(key)) {
                    if (AppContext.getCurrentUser().isAdmin()) {//单位管理员撤销
                        key = "edoc.cancel";
                        MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), EdocUtil.getAccountName(), affairs.get(0).getApp(), repealComment, keyFlag).setImportantLevel(summary.getImportantLevel());
                        if (null != affairs.get(0).getTempleteId()) {
                            content.setTemplateId(affairs.get(0).getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content,
                                app,
                                user.getId(),
                                receivers,
                                systemMessageFilterParam);
                    } else {
                        MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, agenName).setImportantLevel(summary.getImportantLevel());
                        if (null != affairs.get(0).getTempleteId()) {
                            content.setTemplateId(affairs.get(0).getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content,
                                app,
                                user.getId(),
                                receivers,
                                systemMessageFilterParam);
                    }
                } else {
                    if (isneedrepealRecord) {//勾选流程追溯发消息
                        List<Long> traeDataAffair = (List<Long>) traceMap.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
                        Long sId = traceMap.get(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID) == null ? null : ((Number) traceMap.get(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID)).longValue();
                        Long aId = traceMap.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID) == null ? null : ((Number) traceMap.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID)).longValue();
                        Integer type = (Integer) traceMap.get(WFTraceConstants.WFTRACE_TYPE);
                        Iterator<MessageReceiver> _it = receivers.iterator();
                        Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
                        Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();
                        CtpAffair cloneAffair = affairManager.get(aId);
                        while (_it.hasNext()) {
                            MessageReceiver mr = _it.next();
                            Long referenceId = mr.getReferenceId();
                            if (traeDataAffair.contains(referenceId) && Strings.isBlank(mr.getLinkType())) {
                                MessageReceiver receiver = new MessageReceiver(cloneAffair.getId(), mr.getReceiverId(), "message.link.edoc.traceRecord", ColOpenFrom.repealRecord.name(), cloneAffair.getId().toString(), sId.toString(), type.intValue() + "");
                                receiver.setTrack(true);
                                new_receivers.add(receiver);
                            } else {
                                old_receivers.add(mr);
                            }
                        }
                        MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).setImportantLevel(summary.getImportantLevel());
                        if (null != affairs.get(0).getTempleteId()) {
                            content.setTemplateId(affairs.get(0).getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content,
                                app,
                                user.getId(),
                                old_receivers,
                                systemMessageFilterParam);
                        MessageContent content1 = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).add("edoc.summary.cancel.traceview").setImportantLevel(summary.getImportantLevel());
                        if (null != affairs.get(0).getTempleteId()) {
                            content1.setTemplateId(affairs.get(0).getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content1,
                                app,
                                user.getId(),
                                new_receivers,
                                systemMessageFilterParam);
                    } else {
                        MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).setImportantLevel(summary.getImportantLevel());
                        if (null != affairs.get(0).getTempleteId()) {
                            content.setTemplateId(affairs.get(0).getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content,
                                app,
                                user.getId(),
                                receivers,
                                systemMessageFilterParam);
                    }
                }
            }
            List<MessageReceiver> superviseReceivers = EdocSuperviseHelper.getRecieverBySummaryId(summaryId);
            if (null != superviseReceivers && superviseReceivers.size() > 0) {
                //过滤自己，不给自己发消息。
                List<MessageReceiver> excludeSuperviseReceivers = new ArrayList<MessageReceiver>();
                for (MessageReceiver messageReceiver : superviseReceivers) {
                    if (messageReceiver.getReceiverId() != userId && !list.contains(messageReceiver.getReceiverId())) {
                        excludeSuperviseReceivers.add(messageReceiver);
                    }
                }
                if (AppContext.getCurrentUser().isAdmin()) {
                    userName = EdocUtil.getAccountName();
                }
                MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).setImportantLevel(summary.getImportantLevel());
                if (null != affairs.get(0).getTempleteId()) {
                    content.setTemplateId(affairs.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(content,
                        app,
                        user.getId(),
                        excludeSuperviseReceivers,
                        systemMessageFilterParam);
            }
            if (receivers1 != null && receivers1.size() != 0) {
                if ("edoc.agent.cancel".equals(key)) {
                    MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, agenName).add("col.agent").setImportantLevel(summary.getImportantLevel());
                    if (null != affairs.get(0).getTempleteId()) {
                        content.setTemplateId(affairs.get(0).getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content,
                            app,
                            user.getId(),
                            receivers1,
                            systemMessageFilterParam);
                } else {
                    MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).add("col.agent").setImportantLevel(summary.getImportantLevel());
                    if (null != affairs.get(0).getTempleteId()) {
                        content.setTemplateId(affairs.get(0).getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content,
                            app,
                            user.getId(),
                            receivers1,
                            systemMessageFilterParam);
                }
            }
            //文号回滚操作:发起人撤销流程后，已经调用的文号（如果是最大号）可以恢复，下次发文时可继续调用
            if (summary.getEdocType() == 0 || summary.getEdocType() == 2) {//发文/签报撤销
                edocMarkManager.edocMarkCategoryRollBack(summary);
            }
            //删除文档中心已归档的公文。
            deleteDocByResources(summary, user);

            //撤销部门归档
            List<Long> cancelAffairIds = new ArrayList<Long>();
            for (CtpAffair affair2 : affairs) {
                if (affair2.getSubObjectId() != null) {
                    cancelAffairIds.add(affair2.getId());
                }
            }
            if (!CollectionUtils.isEmpty(cancelAffairIds) && AppContext.hasPlugin("doc")) {
                docApi.deleteDocResources(user.getId(), cancelAffairIds);
            }

            //记录操作时间
            affairManager.updateAffairAnalyzeData(cancelAffair);


        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
            throw new EdocException(e);
        } finally {
            wapi.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(AppContext.currentUserId()));
            wapi.releaseWorkFlowProcessLock(String.valueOf(summary.getId()), String.valueOf(AppContext.currentUserId()));
        }
        LOGGER.info("summary is cancelled:" + summaryId);
        return 0;
    }

    private Map<String, Object> createRepealTraceWfData(EdocSummary summary,
                                                        List<CtpAffair> affairs, CtpAffair curAffair, String traceFlag)
            throws BusinessException {
        CtpTemplate t = null;
        if (summary.getTempleteId() != null) {
            t = templateManager.getCtpTemplate(summary.getTempleteId());
        }
        List<CtpAffair> traceAffairs = new ArrayList<CtpAffair>();
        for (CtpAffair aff : affairs) {
            if (Integer.valueOf(StateEnum.col_done.key()).equals(aff.getState())
                    || (Integer.valueOf(StateEnum.col_pending.key()).equals(aff.getState()) && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(aff.getSubState()))
                    || aff.getId().equals(curAffair.getId())
                    || Integer.valueOf(StateEnum.col_sent.key()).equals(aff.getState())
                    || Integer.valueOf(StateEnum.col_waitSend.key()).equals(aff.getState())) {
                traceAffairs.add(aff);
            }
        }
        return edocTraceWorkflowManager.createRepealTraceData(summary, curAffair, traceAffairs, t, traceFlag);
    }


    /**
     * Ajax判断文号定义是否被删除，并且判断内部文号是否已经存在（除开公文自己本身占用的文号）
     *
     * @param definitionId 下拉选择传进来的是文号定义ID，断号选择传进来的是edoc_mark表的ID
     * @param serialNo     内部文号
     * @return deleted（0：已删除  1：未删除）  exsit(0:不存在  1：存在)
     */
    public String checkEdocMark(Long definitionId, String serialNo, Integer selectMode, String summaryId) {
        int deleted = 1;
        int exsit = 0;
        //手动输入的时候不判断文号定义。
        if (definitionId != null && definitionId.longValue() != 0) {
            Long id = 0L;
            if (selectMode == 2) {//断号选择
                EdocMark edocMark = edocMarkManager.getEdocMark(definitionId);
                if (edocMark != null) {
                    EdocMarkDefinition definition = edocMark.getEdocMarkDefinition();
                    if (definition != null) {
                        id = definition.getId();
                    }
                }
            } else if (selectMode == 4) {
                EdocMarkDefinition definition = edocMarkDefinitionManager.getMarkDefinition(definitionId);
                if (definition != null) {
                    id = definition.getId();
                }
            } else {
                id = definitionId;
            }
            //判断文号定义是否已经删除
            deleted = edocMarkDefinitionManager.judgeEdocDefinitionExsit(id);
        }
        //判断内部文号是否已经存在
        if (serialNo != null && !"".equals(serialNo)) {
            User user = AppContext.getCurrentUser();
            long accountId = user.getAccountId();
            if (Strings.isNotBlank(summaryId)) {
                EdocSummary summary = edocSummaryManager.findById(Long.valueOf(summaryId));
                if (summary != null) {
                    accountId = summary.getOrgAccountId();
                }
            }
            exsit = edocSummaryManager.checkSerialNoExsit(summaryId, serialNo, Long.valueOf(accountId));
        }

        return deleted + "," + exsit;
    }

    public int edocBackCancelSummary(long userId, long summaryId, String repealComment, String docFrom) throws BusinessException {
        int result = 0;
        User user = AppContext.getCurrentUser();
        EdocSummary summary = edocSummaryDao.get(summaryId);
        summary.setState(CollaborationEnum.flowState.cancel.ordinal());

        Long caseId = summary.getCaseId();

        summary.setCaseId(null);
        edocSummaryDao.update(summary);

        //将summary的状态改为待�发,撤销已生成事项
        Map<String, Object> columns = new HashMap<String, Object>();
        int app = ApplicationCategoryEnum.edocSend.key();
        if (summary.getEdocType() == 1) {
            app = ApplicationCategoryEnum.edocRec.key();
        } else if (summary.getEdocType() == 2) {
            app = ApplicationCategoryEnum.edocSign.key();
        }
        columns.put("objectId", String.valueOf(summary.getId()));
        columns.put("app", app);
        columns.put("state", StateEnum.col_waitSend.key());
        List<Integer> subStateList = new ArrayList<Integer>();
        subStateList.add(SubStateEnum.col_waitSend_sendBack.key());
        subStateList.add(SubStateEnum.col_waitSend_stepBack.key());
        columns.put("subState", subStateList);
        List<CtpAffair> affairs = affairManager.getByConditions(null, columns);

        //对发起人以外的所有执行人发消息通知
        List<CtpAffair> msgAffairs = affairManager.getValidAffairs(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()), summaryId);
        if (caseId != null) {
            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setCaseId(caseId);
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.CANCEL);
            try {
                result = wapi.cancelCase(context);
            } catch (BPMException e) {
                LOGGER.error("", e);
            }
        }

        if (affairs != null) {
            for (int i = 0; i < affairs.size(); i++) {
                CtpAffair affair = (CtpAffair) affairs.get(i);
                if (affair.getState() == StateEnum.col_waitSend.key()) {
                    affair.setState(StateEnum.col_waitSend.key());
                    affair.setSubState(SubStateEnum.col_waitSend_draft.key());
                    affair.setDelete(false);
                    affairManager.updateAffair(affair);
                }

                if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                    QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                }

                if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                    QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                }
            }
            this.affairManager.updateAffairsState2Cancel(summaryId);
            this.affairManager.updateAffairSummaryState(summaryId, summary.getState());
        }

        if (caseId == null)
            return 1;
        if (result == 1) {
            return result;
        }

        String key = "edoc.cancel";
        int keyFlag = 1;
        if (Strings.isNotBlank(repealComment)) {
            keyFlag = 2;
        }
        try {
            edocStatManager.deleteEdocStat(summaryId);
            this.processLogManager.deleteLog(Long.valueOf(summary.getProcessId()));
            ProcessLogAction pla = null;//lijl添加,为了区分是撤销流程还是取回
            if ("docBack".equals(docFrom)) {
                pla = ProcessLogAction.takeBack;
                key = "edoc.takeback";
            } else {
                pla = ProcessLogAction.cancelColl;
            }
            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1L, pla);
            this.appLogManager.insertLog(user, AppLogAction.Edoc_Cacel, user.getName(), summary.getSubject());
        } catch (Exception e) {
            LOGGER.error("删除公文统计记录异常", e);
        }

        Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(summary).key;
        try {
            String userName = "";
            if (user != null) {
                userName = user.getName();
            }
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
            for (CtpAffair affair1 : msgAffairs) {
                Long agentMemberId = null;
                if (affair1.isDelete())
                    continue;
                if (affair1.getMemberId() == userId) {
                    continue;
                }
                if (affair1.getState() == StateEnum.col_waitSend.key()) {//待发
                    receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId(), "message.link.edoc.done", affair1.getId().toString()));
                } else {
                    agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), affair1.getMemberId());
                    if (agentMemberId != null) {
                        receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId()));
                        receivers1.add(new MessageReceiver(affair1.getId(), agentMemberId));
                    } else {
                        receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId()));
                    }
                }
            }
            if (repealComment == null) {
                repealComment = "";
            }
            MessageContent content = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).setImportantLevel(summary.getImportantLevel());
            if (null != affairs.get(0).getTempleteId()) {
                content.setTemplateId(affairs.get(0).getTempleteId());
            }
            userMessageManager.sendSystemMessage(content,
                    app, user.getId(),
                    receivers, systemMessageFilterParam);
            List<MessageReceiver> superviseReceivers = EdocSuperviseHelper.getRecieverBySummaryId(summaryId);
            if (null != superviseReceivers && superviseReceivers.size() > 0) {
                //过滤自己，不给自己发消息。
                List<MessageReceiver> excludeSuperviseReceivers = new ArrayList<MessageReceiver>();
                for (MessageReceiver messageReceiver : superviseReceivers) {
                    if (messageReceiver.getReceiverId() == userId) continue;
                    excludeSuperviseReceivers.add(messageReceiver);
                }
                MessageContent content1 = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).setImportantLevel(summary.getImportantLevel());
                if (null != affairs.get(0).getTempleteId()) {
                    content1.setTemplateId(affairs.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(content1,
                        app,
                        user.getId(),
                        excludeSuperviseReceivers,
                        systemMessageFilterParam);
            }
            if (receivers1 != null && receivers1.size() != 0) {
                MessageContent content1 = new MessageContent(key, affairs.get(0).getSubject(), userName, affairs.get(0).getApp(), repealComment, keyFlag).add("col.agent").setImportantLevel(summary.getImportantLevel());
                if (null != affairs.get(0).getTempleteId()) {
                    content1.setTemplateId(affairs.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(content1,
                        app, user.getId(),
                        receivers1,
                        systemMessageFilterParam);
            }
            //文号回滚操作:发起人撤销流程后，已经调用的文号（如果是最大号）可以恢复，下次发文时可继续调用
            if (summary.getEdocType() == 0 || summary.getEdocType() == 2) {//发文/签报撤销
                edocMarkManager.edocMarkCategoryRollBack(summary);
            }
            //删除文档中心已归档的公文。
            deleteDocByResources(summary, user);
        } catch (BusinessException e) {
            LOGGER.error("send message failed", e);
            throw new EdocException(e);
        }
        LOGGER.info("summary is cancelled:" + summaryId);
        return 0;
    }

    public void claimWorkItem(int workItemId) throws EdocException {

    }

    @Override
    public void deleteAffair(String pageType, long affairId)
            throws Exception {
        User user = AppContext.getCurrentUser();
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null)
            return;

        //如果是待办，删除个人事项的同时finishWorkitem
        if (pageType.equals(PAGE_TYPE_PENDING)) {
            //Long workitemId = affair.getSubObjectId();
            EdocSummary summary = null;
            EdocOpinion nullColOpinion = new EdocOpinion();
            nullColOpinion.setIdIfNew();
            nullColOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            nullColOpinion.setOpinionType(EdocOpinion.OpinionType.signOpinion.ordinal());
            nullColOpinion.setCreateUserId(user.getId());

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("opinion", nullColOpinion);

            transFinishWorkItem(affair, params);
        }
        affairManager.deleteAffair(affair.getId());
        //删除事项更新全文检索库
        if (AppContext.hasPlugin("index")) {
            indexManager.update(affair.getObjectId(), ApplicationCategoryEnum.edoc.getKey());
        }
    }

    public void pigeonholeAffair(String pageType, CtpAffair affair, Long summaryId, Long archiveId) throws EdocException {
        pigeonholeAffair(pageType, affair, summaryId, archiveId, true);
    }

    public void pigeonholeAffair(String pageType, CtpAffair affair,
                                 Long summaryId, Long archiveId,
                                 boolean needcheckFinish) throws EdocException {
        User user = AppContext.getCurrentUser();
        // 下面这个查询可能使用了hibernate的一级缓存，直接从缓存里面取的，所以在lisenter里面需要设置completetime，否则最后一个节点在这里不是结束状态
        EdocSummary summary = this.getEdocSummaryById(summaryId, false);

        if (summary != null) {
            if (archiveId != null) {
                summary.setArchiveId(archiveId);
            }
            // summary.setHasArchive(false);
            if (!summary.getHasArchive()) {// 公文只归档一次，未归档才进行归档
                Map<String, Object> colums = new HashMap<String, Object>();
                colums.put("hasArchive", true);
                colums.put("archiveId", archiveId);
                this.edocSummaryDao.update(summaryId, colums);

                try {
                    edocStatManager.setArchive(summary.getId());
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

                // 公文督办的操作,删除该公文的督办项 -- start --
                // edocSuperviseManager.pigeonhole(summary);
                // -- end --

                try {
                    LOGGER.info("公文id:" + summary.getId() + "title:" + summary.getSubject() + "getArchiveId:" + (summary.getArchiveId()) + "doc:" + AppContext.hasPlugin("doc"));
                    boolean hasAtt = (summary.isHasAttachments());
                    if (AppContext.hasPlugin("doc") && summary.getArchiveId() != null) {
                        docApi.pigeonholeWithoutAcl(user.getId(), ApplicationCategoryEnum.edoc.getKey(),
                                summary.getId(), hasAtt, summary.getArchiveId(), 0, null);
                    }


                    // 发文封发时候的归档直接删除已发已办事项,其他情况的归档都不删除已发已办事项
                    if (("fengfa".equals(affair.getNodePolicy())
                            && affair.getApp() == ApplicationCategoryEnum.edocSend.getKey()) || summary.getFinished()) {
                        LOGGER.info("公文id:" + summary.getId() + "title:" + summary.getSubject() + "getArchiveId:" + (summary.getArchiveId()) + "doc:" + AppContext.hasPlugin("doc"));

                        setArchiveIdToAffairsAndSendMessages(summary, affair, true);

                        try {
                            if (!summary.getIsQuickSend()) { // 快速发文不需要写归档操作到流程日志
                                String params = summary.getSubject();
                                Long activityId = affair.getActivityId();
                                if (activityId == null) {
                                    BPMActivity bPMActivity = EdocHelper.getBPMActivityByAffair(affair);// 当前节点
                                    if (bPMActivity != null)
                                        activityId = Long.valueOf(bPMActivity.getId());
                                }
                                if (activityId != null) {
                                    this.processLogManager.insertLog(user,
                                            Long.valueOf(
                                                    summary.getProcessId() == null ? "-1" : summary.getProcessId()),
                                            activityId.longValue(), ProcessLogAction.processEdoc,
                                            String.valueOf(ProcessLogAction.ProcessEdocAction.pigeonhole.getKey()),
                                            params);
                                } else {
                                    this.processLogManager.insertLog(user,
                                            Long.valueOf(
                                                    summary.getProcessId() == null ? "-1" : summary.getProcessId()),
                                            -1l, ProcessLogAction.processEdoc,
                                            String.valueOf(ProcessLogAction.ProcessEdocAction.pigeonhole.getKey()),
                                            params);
                                }
                            }

                            this.appLogManager.insertLog(user, AppLogAction.Edoc_PingHole, user.getName(),
                                    summary.getSubject());
                        } catch (Exception e) {
                            LOGGER.error("发送消息错误", e);
                            throw new EdocException(e);
                        }
                    }

                } catch (Exception e) {
                    throw new EdocException(e);
                }
            }
        }
    }

    public void setArchiveIdToAffairsAndSendMessages(EdocSummary summary, CtpAffair affair, boolean needSendMessage) {

        Map<String, Object> parameter = new HashMap<String, Object>();
        parameter.put("archiveId", summary.getArchiveId());
        try {
            affairManager.updateAffairs(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()), summary.getId(), parameter);
        } catch (BusinessException e1) {
            LOGGER.error("更新affair状态异常", e1);
        }

        try {
            if (AppContext.hasPlugin("index")) {
                indexManager.delete(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
            }
        } catch (Exception e) {
            LOGGER.error("公文归档删除已发已办事项的时候，删除全文检索异常", e);
        }

        //发送系统消息告诉流程总的节点,"收文|发文|签报《****》流程结束，已经被归档到’公文档案\审计部’下，从已发、已办中删除"
        if (needSendMessage) {
            if (summary.getArchiveId() != null) {
                DocResourceBO doc = null;
                try {
                    if (AppContext.hasPlugin("doc")) {
                        doc = docApi.getDocResource(summary.getArchiveId());
                    }
                } catch (BusinessException e) {
                    LOGGER.error("获取文档异常", e);
                }
                String pigeonholePath = ResourceUtil.getString("doc.center.lable");
                if (doc != null && AppContext.hasPlugin("doc")) {
                    try {
                        pigeonholePath = docApi.getPhysicalPath(doc.getLogicalPath(), java.io.File.separator, false, 0);
                    } catch (BusinessException e) {
                        LOGGER.error("", e);
                    }
                } else {
                    LOGGER.info("公文流程结束，DocResouce为空,标题：" + summary.getSubject() + ",id:" + summary.getId());
                }
                EdocMessageHelper.processFinishedAutoPigeonhole(affairManager,
                        userMessageManager,
                        summary,
                        affair,
                        orgManager,
                        pigeonholePath,
                        processLogManager,
                        appLogManager);
            } else {
                LOGGER.info("公文流程结束，archivedId为空，未发送归档消息，标题：" + summary.getSubject() + ",id:" + summary.getId());
            }
        }
    }

    public void pigeonholeAffair(String pageType, long affairId, Long summaryId, Long archiveId) throws BusinessException {
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null) {
            return;
        }
        pigeonholeAffair(pageType, affair, summaryId, archiveId);
    }

    public void pigeonholeAffair(String pageType, long affairId, Long summaryId) throws BusinessException {
        pigeonholeAffair(pageType, affairId, summaryId, null);
    }

    public void pigeonholeAffair(String pageType, CtpAffair affair, Long summaryId) throws EdocException {
        pigeonholeAffair(pageType, affair, summaryId, null);
    }


    public void updateEdocIndex(Long summaryId) {
        if (summaryId != null) {
            try {
                if (AppContext.hasPlugin("index")) {
                    indexManager.update(summaryId, ApplicationCategoryEnum.edoc.getKey());
                }
            } catch (Exception e) {
                LOGGER.error("公文更新全文检索信息异常", e);
            }
        }
    }

    private void addPdfBodyToCurrentSummary(EdocSummary summary) {
        Set<EdocBody> bodies = summary.getEdocBodies();
        List<EdocBody> saveBody = new ArrayList<EdocBody>();
        List<EdocBody> updateBody = new ArrayList<EdocBody>();
        for (EdocBody body : bodies) {
            if (body.getContentNo() == EdocBody.EDOC_BODY_PDF_ONE ||
                    body.getContentNo() == EdocBody.EDOC_BODY_PDF_TWO) {
                EdocBody e = edocBodyDao.get(body.getId());
                if (e != null) {
                    updateBody.add(body);
                } else {
                    saveBody.add(body);
                }
            }
        }

        if (saveBody.size() > 0) {
            edocBodyDao.savePatchAll(saveBody);
        }
        if (updateBody.size() > 0) {
            edocBodyDao.updatePatchAll(updateBody);
        }
    }

    private String getParams(EdocOpinion signOpinion, String params) {
        if (signOpinion.exchangeType >= 0) {
            if (signOpinion.exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Dept) {
                params = ResourceBundleUtil.getString("com.seeyon.v3x.system.resources.i18n.SysMgrResources", "sys.role.rolename.department_exchange");
            } else if (signOpinion.exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org) {
                params = ResourceBundleUtil.getString("com.seeyon.v3x.system.resources.i18n.SysMgrResources", "sys.role.rolename.account_exchange");
            }
        }
        return params;
    }


    public String getCaseLogXML(long caseId) throws EdocException {
        String xml = "";
        try {
            //	xml=EdocHelper.getCaseLogXML(userId, caseId);
        } catch (Exception e) {
            throw new EdocException(e);
        }
        return xml;
    }

    public String getCaseProcessXML(long caseId) throws EdocException {
        String xml = "";
        return xml;
    }

    public String getCaseWorkItemLogXML(long caseId) throws EdocException {
        String xml = "";
        return xml;
    }

    public EdocSummary getColAllById(long summaryId) throws EdocException {
        EdocSummary summary = getEdocSummaryById(summaryId, false);
        if (summary == null) return null;
        if (summary.getEdocOpinions() != null) summary.getEdocOpinions().size();
        if (summary.getEdocBodies() != null) summary.getEdocBodies().size();
        summary.getFirstBody();
        return summary;
    }

    public EdocSummary getEdocSummaryById(long summaryId, boolean needBody, boolean isLoadExtend) throws EdocException {
        return edocSummaryManager.getEdocSummaryById(summaryId, needBody, isLoadExtend);
    }

    public EdocSummary getEdocSummaryById(long summaryId, boolean needBody) throws EdocException {
        return edocSummaryManager.getEdocSummaryById(summaryId, needBody, true);
    }

    public String isQuickSend(String summaryId) {
        if (summaryId == null) {
            return "false";
        }
        try {

            EdocSummary s = edocSummaryManager.getEdocSummaryById(Long.valueOf(summaryId), false, false);
            Boolean _isQuick = s.getIsQuickSend() == null ? false : s.getIsQuickSend();
            return _isQuick.toString();
        } catch (EdocException e) {
            return "false";
        }

    }

    public String getPolicyByAffair(CtpAffair affair, String processId) throws EdocException {
        if (Strings.isNotBlank(affair.getNodePolicy())) {
            return affair.getNodePolicy();
        }

        SeeyonPolicy p = null;
        try {
            p = EdocHelper.getPolicyByAffair(affair, processId);
        } catch (Exception e) {
            throw new EdocException(e);
        }
        if (p == null)
            return SeeyonPolicy.DEFAULT_POLICY;
        String policy = p.getId();
        return policy;
    }

    public String getPolicyBySummary(EdocSummary summary) throws EdocException {
        summary.setCompleteTime(new Timestamp(System.currentTimeMillis()));
        edocSummaryDao.update(summary);
        return null;
    }

    public String getProcessXML(String processId) throws EdocException {
        String xml = "";
        return xml;
    }

    public EdocSummary getSummaryByCaseId(long caseId) throws EdocException {
        EdocSummary summary = edocSummaryDao.getSummaryByCaseId(caseId);
        try {
            V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
            summary.setStartMember(member);
        } catch (BusinessException e) {
            LOGGER.error("", e);
            throw new EdocException(e);
        }
        return summary;
    }


    public Long getSummaryIdByCaseId(long caseId) throws EdocException {

        List<Object> list = edocSummaryDao.find("select id from EdocSummary where caseId=?", -1, -1, null, caseId);

        if (list != null && !list.isEmpty()) {
            return list.get(0) == null ? null : ((Number) (list.get(0))).longValue();
        }

        return null;
    }

    public EdocSummary getSummaryByWorkItemId(int workItemId)
            throws EdocException {
        return null;
    }

    public void hasten(String processId, String activityId,
                       String additional_remark) throws EdocException {

    }

    private static final String selectSummayExtend =
            "extend1.date1," +
                    "extend1.date2," +
                    "extend1.date3," +
                    "extend1.date4," +
                    "extend1.date5," +
                    "extend1.date6," +
                    "extend1.date7," +
                    "extend1.date8," +
                    "extend1.date9," +
                    "extend1.date10," +
                    "extend1.date11," +
                    "extend1.date12," +
                    "extend1.date13," +
                    "extend1.date14," +
                    "extend1.date15," +
                    "extend1.date16," +
                    "extend1.date17," +
                    "extend1.date18," +
                    "extend1.date19," +
                    "extend1.date20," +
                    "extend1.decimal1," +
                    "extend1.decimal2," +
                    "extend1.decimal3," +
                    "extend1.decimal4," +
                    "extend1.decimal5," +
                    "extend1.decimal6," +
                    "extend1.decimal7," +
                    "extend1.decimal8," +
                    "extend1.decimal9," +
                    "extend1.decimal10," +
                    "extend1.decimal11," +
                    "extend1.decimal12," +
                    "extend1.decimal13," +
                    "extend1.decimal14," +
                    "extend1.decimal15," +
                    "extend1.decimal16," +
                    "extend1.decimal17," +
                    "extend1.decimal18," +
                    "extend1.decimal19," +
                    "extend1.decimal20," +
                    "extend1.integer1," +
                    "extend1.integer2," +
                    "extend1.integer3," +
                    "extend1.integer4," +
                    "extend1.integer5," +
                    "extend1.integer6," +
                    "extend1.integer7," +
                    "extend1.integer8," +
                    "extend1.integer9," +
                    "extend1.integer10," +
                    "extend1.integer11," +
                    "extend1.integer12," +
                    "extend1.integer13," +
                    "extend1.integer14," +
                    "extend1.integer15," +
                    "extend1.integer16," +
                    "extend1.integer17," +
                    "extend1.integer18," +
                    "extend1.integer19," +
                    "extend1.integer20," +
                    "extend1.list1," +
                    "extend1.list2," +
                    "extend1.list3," +
                    "extend1.list4," +
                    "extend1.list5," +
                    "extend1.list6," +
                    "extend1.list7," +
                    "extend1.list8," +
                    "extend1.list9," +
                    "extend1.list10," +
                    "extend1.list11," +
                    "extend1.list12," +
                    "extend1.list13," +
                    "extend1.list14," +
                    "extend1.list15," +
                    "extend1.list16," +
                    "extend1.list17," +
                    "extend1.list18," +
                    "extend1.list19," +
                    "extend1.list20," +
                    "extend1.text1," +
                    "extend1.text2," +
                    "extend1.text3," +
                    "extend1.text4," +
                    "extend1.text5," +
                    "extend1.text6," +
                    "extend1.text7," +
                    "extend1.text8," +
                    "extend1.text9," +
                    "extend1.text10," +
                    "extend1.text11," +
                    "extend1.text12," +
                    "extend1.text13," +
                    "extend1.text14," +
                    "extend1.text15," +
                    "extend1.varchar1," +
                    "extend1.varchar2," +
                    "extend1.varchar3," +
                    "extend1.varchar4," +
                    "extend1.varchar5," +
                    "extend1.varchar6," +
                    "extend1.varchar7," +
                    "extend1.varchar8," +
                    "extend1.varchar9," +
                    "extend1.varchar10," +
                    "extend1.varchar11," +
                    "extend1.varchar12," +
                    "extend1.varchar13," +
                    "extend1.varchar14," +
                    "extend1.varchar15," +
                    "extend1.varchar16," +
                    "extend1.varchar17," +
                    "extend1.varchar18," +
                    "extend1.varchar19," +
                    "extend1.varchar20," +
                    "extend1.varchar21," +
                    "extend1.varchar22," +
                    "extend1.varchar23," +
                    "extend1.varchar24," +
                    "extend1.varchar25," +
                    "extend1.varchar26," +
                    "extend1.varchar27," +
                    "extend1.varchar28," +
                    "extend1.varchar29," +
                    "extend1.varchar30 ";

    private static final String selectSummayExtendSql =
            "extend1.date1," +
                    "extend1.date2," +
                    "extend1.date3," +
                    "extend1.date4," +
                    "extend1.date5," +
                    "extend1.date6," +
                    "extend1.date7," +
                    "extend1.date8," +
                    "extend1.date9," +
                    "extend1.date10," +
                    "extend1.date11," +
                    "extend1.date12," +
                    "extend1.date13," +
                    "extend1.date14," +
                    "extend1.date15," +
                    "extend1.date16," +
                    "extend1.date17," +
                    "extend1.date18," +
                    "extend1.date19," +
                    "extend1.date20," +
                    "extend1.decimal1," +
                    "extend1.decimal2," +
                    "extend1.decimal3," +
                    "extend1.decimal4," +
                    "extend1.decimal5," +
                    "extend1.decimal6," +
                    "extend1.decimal7," +
                    "extend1.decimal8," +
                    "extend1.decimal9," +
                    "extend1.decimal10," +
                    "extend1.decimal11," +
                    "extend1.decimal12," +
                    "extend1.decimal13," +
                    "extend1.decimal14," +
                    "extend1.decimal15," +
                    "extend1.decimal16," +
                    "extend1.decimal17," +
                    "extend1.decimal18," +
                    "extend1.decimal19," +
                    "extend1.decimal20," +
                    "extend1.integer1," +
                    "extend1.integer2," +
                    "extend1.integer3," +
                    "extend1.integer4," +
                    "extend1.integer5," +
                    "extend1.integer6," +
                    "extend1.integer7," +
                    "extend1.integer8," +
                    "extend1.integer9," +
                    "extend1.integer10," +
                    "extend1.integer11," +
                    "extend1.integer12," +
                    "extend1.integer13," +
                    "extend1.integer14," +
                    "extend1.integer15," +
                    "extend1.integer16," +
                    "extend1.integer17," +
                    "extend1.integer18," +
                    "extend1.integer19," +
                    "extend1.integer20," +
                    "extend1.list1," +
                    "extend1.list2," +
                    "extend1.list3," +
                    "extend1.list4," +
                    "extend1.list5," +
                    "extend1.list6," +
                    "extend1.list7," +
                    "extend1.list8," +
                    "extend1.list9," +
                    "extend1.list10," +
                    "extend1.list11," +
                    "extend1.list12," +
                    "extend1.list13," +
                    "extend1.list14," +
                    "extend1.list15," +
                    "extend1.list16," +
                    "extend1.list17," +
                    "extend1.list18," +
                    "extend1.list19," +
                    "extend1.list20," +
                    "extend1.text1," +
                    "extend1.text2," +
                    "extend1.text3," +
                    "extend1.text4," +
                    "extend1.text5," +
                    "extend1.text6," +
                    "extend1.text7," +
                    "extend1.text8," +
                    "extend1.text9," +
                    "extend1.text10," +
                    "extend1.text11," +
                    "extend1.text12," +
                    "extend1.text13," +
                    "extend1.text14," +
                    "extend1.text15," +
                    "extend1.avarchar1," +
                    "extend1.avarchar2," +
                    "extend1.avarchar3," +
                    "extend1.avarchar4," +
                    "extend1.avarchar5," +
                    "extend1.avarchar6," +
                    "extend1.avarchar7," +
                    "extend1.avarchar8," +
                    "extend1.avarchar9," +
                    "extend1.avarchar10," +
                    "extend1.avarchar11," +
                    "extend1.avarchar12," +
                    "extend1.avarchar13," +
                    "extend1.avarchar14," +
                    "extend1.avarchar15," +
                    "extend1.avarchar16," +
                    "extend1.avarchar17," +
                    "extend1.avarchar18," +
                    "extend1.avarchar19," +
                    "extend1.avarchar20," +
                    "extend1.avarchar21," +
                    "extend1.avarchar22," +
                    "extend1.avarchar23," +
                    "extend1.avarchar24," +
                    "extend1.avarchar25," +
                    "extend1.avarchar26," +
                    "extend1.avarchar27," +
                    "extend1.avarchar28," +
                    "extend1.avarchar29," +
                    "extend1.avarchar30 ";

    private static final String selectSummary = "summary.id," +
            "summary.startUserId," +
            "summary.caseId," +
            "summary.completeTime," +
            "summary.subject," +
            "summary.secretLevel," +
            "summary.identifier," +
            "summary.docMark," +
            "summary.serialNo," +
            "summary.createTime," +
            "summary.sendTo," +
            "summary.issuer," +
            "summary.signingDate," +
            "summary.deadline," +
            "summary.deadlineDatetime," +
            "summary.startTime," +
            "summary.copies," +
            "summary.createPerson," +
            "summary.sendUnit," +
            "summary.sendDepartment," +
            "summary.hasArchive," +
            "summary.processId," +
            "summary.caseId," +
            "summary.urgentLevel, " +
            "summary.templeteId, " +
            "summary.state, " +
            "summary.copyTo, " +
            "summary.reportTo," +
            "summary.archiveId," +
            "summary.edocType, " +
            "summary.docMark2," +
            "summary.sendTo2, " +
            "summary.docType," +
            "summary.sendType," +
            "summary.keywords," +
            "summary.isQuickSend," +
            "summary.review, " +
            "summary.currentNodesInfo," +
            "summary.printUnit," +
            "summary.printer," +
            "summary.packTime," +
            "summary.sendUnit2," +
            "summary.copyTo2," +
            "summary.reportTo2," +
            "summary.keepPeriod," +
            "summary.receiptDate," +
            "summary.registrationDate," +
            "summary.auditor," +
            "summary.undertaker," +
            "summary.phone," +
            "summary.copies2," +
            "summary.orgAccountId";

    private static final String selectSummarySql = "summary.id," +
            "summary.start_user_id," +
            "summary.case_id," +
            "summary.complete_time," +
            "summary.subject," +
            "summary.secret_level," +
            "summary.identifier," +
            "summary.doc_mark," +
            "summary.serial_no," +
            "summary.create_time," +
            "summary.send_to," +
            "summary.issuer," +
            "summary.signing_date," +
            "summary.deadline," +
            "summary.deadline_datetime," +
            "summary.start_time," +
            "summary.copies," +
            "summary.create_person," +
            "summary.send_unit," +
            "summary.send_department," +
            "summary.has_archive," +
            "summary.process_id," +
            "summary.case_id as cese_id1," +
            "summary.urgent_level, " +
            "summary.templete_id, " +
            "summary.state, " +
            "summary.copy_to, " +
            "summary.report_to," +
            "summary.archive_id," +
            "summary.edoc_type, " +
            "summary.doc_mark2," +
            "summary.send_to2, " +
            "summary.doc_type," +
            "summary.send_type," +
            "summary.keywords," +
            "summary.is_quick_send," +
            "summary.review, " +
            "summary.current_nodes_info," +
            "summary.print_unit," +
            "summary.printer," +
            "summary.pack_date," +
            "summary.send_unit2," +
            "summary.copy_to2," +
            "summary.report_to2," +
            "summary.keep_period," +
            "summary.receipt_date," +
            "summary.registration_date," +
            "summary.auditor," +
            "summary.undertaker," +
            "summary.phone," +
            "summary.copies2," +
            "summary.org_account_id";

    private static final String selectAffair = selectSummary +
            ",affair.id," +
            "affair.state," +
            "affair.subState," +
            "affair.track," +
            "affair.hastenTimes," +
            "affair.coverTime," +
            "affair.remindDate," +
            "affair.deadlineDate," +
            "affair.receiveTime," +
            "affair.completeTime," +
            "affair.createDate," +
            "affair.memberId," +
            "affair.bodyType," +
            "affair.transactorId," +
            "affair.updateDate," +
            "affair.extProps," +
            "affair.nodePolicy," +
            "affair.subObjectId," +
            "affair.activityId";

    private static void make(Object[] object, EdocSummary summary, CtpAffair affair) {
        int n = 0;
        summary.setId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setStartUserId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setCaseId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setCompleteTime((Timestamp) object[n++]);
        summary.setSubject((String) object[n++]);
        summary.setSecretLevel((String) object[n++]);
        summary.setIdentifier((String) object[n++]);
        summary.setDocMark((String) object[n++]);
        summary.setSerialNo((String) object[n++]);
        summary.setCreateTime((Timestamp) object[n++]);
        summary.setSendTo((String) object[n++]);
        summary.setIssuer((String) object[n++]);
        summary.setSigningDate(trans2SqlDate(object[n++]));
        summary.setDeadline(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setDeadlineDatetime((java.util.Date) object[n++]);
        summary.setStartTime((Timestamp) object[n++]);
        summary.setCopies(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setCreatePerson((String) object[n++]);
        summary.setSendUnit((String) object[n++]);
        summary.setSendDepartment((String) object[n++]);
        summary.setHasArchive((Boolean) object[n++]);
        summary.setProcessId((String) object[n++]);
        summary.setCaseId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setUrgentLevel((String) object[n++]);
        summary.setTempleteId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setState(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setCopyTo((String) object[n++]);
        summary.setReportTo((String) object[n++]);
        summary.setArchiveId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setEdocType(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setDocMark2((String) object[n++]);
        summary.setSendTo2((String) object[n++]);
        summary.setDocType((String) object[n++]);
        summary.setSendType((String) object[n++]);
        summary.setKeywords((String) object[n++]);
        summary.setIsQuickSend((Boolean) object[n++]);
        summary.setReview((String) object[n++]);
        summary.setCurrentNodesInfo((String) object[n++]);
        summary.setPrintUnit((String) object[n++]);
        summary.setPrinter((String) object[n++]);
        summary.setPackTime((Timestamp) object[n++]);
        summary.setSendUnit2((String) object[n++]);
        summary.setCopyTo2((String) object[n++]);
        summary.setReportTo2((String) object[n++]);
        summary.setKeepPeriod(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setReceiptDate(trans2SqlDate(object[n++]));
        summary.setRegistrationDate(trans2SqlDate(object[n++]));
        summary.setAuditor((String) object[n++]);
        summary.setUndertaker((String) object[n++]);
        summary.setPhone((String) object[n++]);
        summary.setCopies2(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setOrgAccountId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;

        affair.setId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setState(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        affair.setSubState(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        affair.setTrack(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        affair.setHastenTimes(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        affair.setCoverTime((Boolean) object[n++]);
        affair.setRemindDate(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setDeadlineDate(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setReceiveTime((Timestamp) object[n++]);
        affair.setUpdateDate(affair.getReceiveTime());
        affair.setCompleteTime((Timestamp) object[n++]);
        affair.setCreateDate((Timestamp) object[n++]);
        affair.setMemberId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setBodyType((String) object[n++]);
        affair.setTransactorId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setUpdateDate((Timestamp) object[n++]);
        affair.setExtProps(((String) object[n++]));
        String nodePolicy = (String) object[n++];
        if (nodePolicy != null) {
            nodePolicy = nodePolicy.replaceAll(new String(new char[]{(char) 160}), " ");
        }
        affair.setNodePolicy(nodePolicy);
        affair.setSubject(summary.getSubject());
        affair.setSubObjectId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        affair.setActivityId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
    }

    private static int make(Object[] object, EdocSummary summary, boolean needExtend) {
        int n = 0;
        summary.setId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setStartUserId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setCaseId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setCompleteTime((Timestamp) object[n++]);
        summary.setSubject((String) object[n++]);
        summary.setSecretLevel((String) object[n++]);
        summary.setIdentifier((String) object[n++]);
        summary.setDocMark((String) object[n++]);
        summary.setSerialNo((String) object[n++]);
        summary.setCreateTime((Timestamp) object[n++]);
        summary.setSendTo((String) object[n++]);
        summary.setIssuer((String) object[n++]);
        summary.setSigningDate(trans2SqlDate(object[n++]));
        summary.setDeadline(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setDeadlineDatetime((java.util.Date) object[n++]);
        summary.setStartTime((Timestamp) object[n++]);
        summary.setCopies(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setCreatePerson((String) object[n++]);
        summary.setSendUnit((String) object[n++]);
        summary.setSendDepartment((String) object[n++]);
        summary.setHasArchive((Boolean) object[n++]);
        summary.setProcessId((String) object[n++]);
        summary.setCaseId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setUrgentLevel((String) object[n++]);
        summary.setTempleteId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setState(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setCopyTo((String) object[n++]);
        summary.setReportTo((String) object[n++]);
        summary.setArchiveId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        summary.setEdocType(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setDocMark2((String) object[n++]);
        summary.setSendTo2((String) object[n++]);
        summary.setDocType((String) object[n++]);
        summary.setSendType((String) object[n++]);
        summary.setKeywords((String) object[n++]);
        summary.setIsQuickSend((Boolean) object[n++]);
        summary.setReview((String) object[n++]);
        summary.setCurrentNodesInfo((String) object[n++]);
        summary.setPrintUnit((String) object[n++]);
        summary.setPrinter((String) object[n++]);
        summary.setPackTime((Timestamp) object[n++]);
        summary.setSendUnit2((String) object[n++]);
        summary.setCopyTo2((String) object[n++]);
        summary.setReportTo2((String) object[n++]);
        summary.setKeepPeriod(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setReceiptDate(trans2SqlDate(object[n++]));
        summary.setRegistrationDate(trans2SqlDate(object[n++]));
        summary.setAuditor((String) object[n++]);
        summary.setUndertaker((String) object[n++]);
        summary.setPhone((String) object[n++]);
        summary.setCopies2(object[n] == null ? null : ((Number) object[n]).intValue());
        n++;
        summary.setOrgAccountId(object[n] == null ? null : ((Number) object[n]).longValue());
        n++;
        if (needExtend) {
            n += 6;
            summary.setDate1(trans2SqlDate(object[n++]));
            summary.setDate2(trans2SqlDate(object[n++]));
            summary.setDate3(trans2SqlDate(object[n++]));
            summary.setDate4(trans2SqlDate(object[n++]));
            summary.setDate5(trans2SqlDate(object[n++]));
            summary.setDate6(trans2SqlDate(object[n++]));
            summary.setDate7(trans2SqlDate(object[n++]));
            summary.setDate8(trans2SqlDate(object[n++]));
            summary.setDate9(trans2SqlDate(object[n++]));
            summary.setDate10(trans2SqlDate(object[n++]));
            summary.setDate11(trans2SqlDate(object[n++]));
            summary.setDate12(trans2SqlDate(object[n++]));
            summary.setDate13(trans2SqlDate(object[n++]));
            summary.setDate14(trans2SqlDate(object[n++]));
            summary.setDate15(trans2SqlDate(object[n++]));
            summary.setDate16(trans2SqlDate(object[n++]));
            summary.setDate17(trans2SqlDate(object[n++]));
            summary.setDate18(trans2SqlDate(object[n++]));
            summary.setDate19(trans2SqlDate(object[n++]));
            summary.setDate20(trans2SqlDate(object[n++]));

            summary.setDecimal1(transSqlDouble(object[n++]));
            summary.setDecimal2(transSqlDouble(object[n++]));
            summary.setDecimal3(transSqlDouble(object[n++]));
            summary.setDecimal4(transSqlDouble(object[n++]));
            summary.setDecimal5(transSqlDouble(object[n++]));
            summary.setDecimal6(transSqlDouble(object[n++]));
            summary.setDecimal7(transSqlDouble(object[n++]));
            summary.setDecimal8(transSqlDouble(object[n++]));
            summary.setDecimal9(transSqlDouble(object[n++]));
            summary.setDecimal10(transSqlDouble(object[n++]));
            summary.setDecimal11(transSqlDouble(object[n++]));
            summary.setDecimal12(transSqlDouble(object[n++]));
            summary.setDecimal13(transSqlDouble(object[n++]));
            summary.setDecimal14(transSqlDouble(object[n++]));
            summary.setDecimal15(transSqlDouble(object[n++]));
            summary.setDecimal16(transSqlDouble(object[n++]));
            summary.setDecimal17(transSqlDouble(object[n++]));
            summary.setDecimal18(transSqlDouble(object[n++]));
            summary.setDecimal19(transSqlDouble(object[n++]));
            summary.setDecimal20(transSqlDouble(object[n++]));

            summary.setInteger1(transSqlInteger(object[n++]));
            summary.setInteger2(transSqlInteger(object[n++]));
            summary.setInteger3(transSqlInteger(object[n++]));
            summary.setInteger4(transSqlInteger(object[n++]));
            summary.setInteger5(transSqlInteger(object[n++]));
            summary.setInteger6(transSqlInteger(object[n++]));
            summary.setInteger7(transSqlInteger(object[n++]));
            summary.setInteger8(transSqlInteger(object[n++]));
            summary.setInteger9(transSqlInteger(object[n++]));
            summary.setInteger10(transSqlInteger(object[n++]));
            summary.setInteger11(transSqlInteger(object[n++]));
            summary.setInteger12(transSqlInteger(object[n++]));
            summary.setInteger13(transSqlInteger(object[n++]));
            summary.setInteger14(transSqlInteger(object[n++]));
            summary.setInteger15(transSqlInteger(object[n++]));
            summary.setInteger16(transSqlInteger(object[n++]));
            summary.setInteger17(transSqlInteger(object[n++]));
            summary.setInteger18(transSqlInteger(object[n++]));
            summary.setInteger19(transSqlInteger(object[n++]));
            summary.setInteger20(transSqlInteger(object[n++]));

            summary.setList1((String) object[n++]);
            summary.setList2((String) object[n++]);
            summary.setList3((String) object[n++]);
            summary.setList4((String) object[n++]);
            summary.setList5((String) object[n++]);
            summary.setList6((String) object[n++]);
            summary.setList7((String) object[n++]);
            summary.setList8((String) object[n++]);
            summary.setList9((String) object[n++]);
            summary.setList10((String) object[n++]);
            summary.setList11((String) object[n++]);
            summary.setList12((String) object[n++]);
            summary.setList13((String) object[n++]);
            summary.setList14((String) object[n++]);
            summary.setList15((String) object[n++]);
            summary.setList16((String) object[n++]);
            summary.setList17((String) object[n++]);
            summary.setList18((String) object[n++]);
            summary.setList19((String) object[n++]);
            summary.setList20((String) object[n++]);

            summary.setText1((String) object[n++]);
            summary.setText2((String) object[n++]);
            summary.setText3((String) object[n++]);
            summary.setText4((String) object[n++]);
            summary.setText5((String) object[n++]);
            summary.setText6((String) object[n++]);
            summary.setText7((String) object[n++]);
            summary.setText8((String) object[n++]);
            summary.setText9((String) object[n++]);
            summary.setText10((String) object[n++]);
            summary.setText11((String) object[n++]);
            summary.setText12((String) object[n++]);
            summary.setText13((String) object[n++]);
            summary.setText14((String) object[n++]);
            summary.setText15((String) object[n++]);

            summary.setVarchar1((String) object[n++]);
            summary.setVarchar2((String) object[n++]);
            summary.setVarchar3((String) object[n++]);
            summary.setVarchar4((String) object[n++]);
            summary.setVarchar5((String) object[n++]);
            summary.setVarchar6((String) object[n++]);
            summary.setVarchar7((String) object[n++]);
            summary.setVarchar8((String) object[n++]);
            summary.setVarchar9((String) object[n++]);
            summary.setVarchar10((String) object[n++]);
            summary.setVarchar11((String) object[n++]);
            summary.setVarchar12((String) object[n++]);
            summary.setVarchar13((String) object[n++]);
            summary.setVarchar14((String) object[n++]);
            summary.setVarchar15((String) object[n++]);
            summary.setVarchar16((String) object[n++]);
            summary.setVarchar17((String) object[n++]);
            summary.setVarchar18((String) object[n++]);
            summary.setVarchar19((String) object[n++]);
            summary.setVarchar20((String) object[n++]);
            summary.setVarchar21((String) object[n++]);
            summary.setVarchar22((String) object[n++]);
            summary.setVarchar23((String) object[n++]);
            summary.setVarchar24((String) object[n++]);
            summary.setVarchar25((String) object[n++]);
            summary.setVarchar26((String) object[n++]);
            summary.setVarchar27((String) object[n++]);
            summary.setVarchar28((String) object[n++]);
            summary.setVarchar29((String) object[n++]);
            summary.setVarchar30((String) object[n++]);
        }
        return n;
    }

    private static Date trans2SqlDate(Object date) {
        Date ret = null;
        if (date != null) {
            if (date instanceof Date) {
                ret = (Date) date;
            } else {
                ret = new Date(((java.util.Date) date).getTime());
            }
        }
        return ret;
    }

    private static Double transSqlDouble(Object data) {
        Double ret = null;
        if (data == null) {
            return ret;
        }
        if (data instanceof Double) {
            ret = (Double) data;
        } else if (data instanceof BigDecimal) {
            ret = ((BigDecimal) data).doubleValue();
        }
        return ret;
    }

    private static Integer transSqlInteger(Object data) {
        Integer ret = null;
        if (data == null) {
            return ret;
        }
        if (data instanceof Integer) {
            ret = (Integer) data;
        } else if (data instanceof Long) {
            ret = ((Long) data).intValue();
        }
        return ret;
    }

    /**
     * 供公文统计使用，仅查询出秘密级别
     *
     * @param ids
     * @return
     */
    public Hashtable<Long, EdocSummary> queryBySummaryIds(List<Long> ids) {
        Hashtable<Long, EdocSummary> hs = new Hashtable<Long, EdocSummary>();
        List result = new ArrayList();
        String hsql = "select id,secretLevel,archiveId,sendUnit from EdocSummary as summary where id in (:ids)";
        List<Long>[] list = Strings.splitList(ids, 1000);
        if (list != null && list.length > 0) {
            for (List<Long> id : list) {
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                parameterMap.put("ids", id);
                result.addAll(edocSummaryDao.find(hsql, -1, -1, parameterMap));
            }
        }
        for (int i = 0; i < result.size(); i++) {
            Object[] object = (Object[]) result.get(i);
            EdocSummary summary = new EdocSummary();
            summary.setId(Long.parseLong(object[0].toString()));
            if (object[1] != null) {
                summary.setSecretLevel(object[1].toString());
            }
            if (object[2] != null) {
                summary.setArchiveId(object[2] == null ? null : ((Number) object[2]).longValue());
            }
            if (object[3] != null) {
                summary.setSendUnit(object[3].toString());
            }
            hs.put(summary.getId(), summary);
        }

        return hs;
    }

    public List<EdocSummaryModel> queryByCondition(int edocType, String condition, String field, String field1, int state, Map<String, Object> params, int... substateArray) {
        return queryByCondition1(edocType, condition, field, field1, -1, state, params, substateArray);
    }

    /**
     * substateArray 0:affair的substate；1：是否归档；2：区分阅文、办文3：用于标识是否等于0中的值，用于收发文待发
     */
    public List<EdocSummaryModel> queryByCondition1(int edocType, String condition, String field, String field1, long subEdocType, int state, Map<String, Object> params, int... substateArray) {
//		yangzd 特殊字符处理
        if (null != field) {//小查询textfeild
//			StringBuilder buffer=new StringBuilder();

            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < field.length(); i++) {
                if (field.charAt(i) == '\'') {
                    buffer.append("\\'");
                } else {
                    buffer.append(field.charAt(i));
                }
            }
            field = SQLWildcardUtil.escape(buffer.toString());
        }
        if (null != field1) {//小查询textfeild1
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < field1.length(); i++) {
                if (field1.charAt(i) == '\'') {
                    buffer.append("\\'");
                } else {
                    buffer.append(field1.charAt(i));
                }
            }
            field1 = SQLWildcardUtil.escape(buffer.toString());
        }
//		yangzd 特殊字符处理
        String exp0 = null;
        String paramName = null;
        String paramValue = null;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        //List<String> paramNameList = new ArrayList<String>();
        User user = AppContext.getCurrentUser();
        long user_id = user.getId();

        //获取代理相关信息
        List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(user_id);
        List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(user_id);
        List<AgentModel> agentModelList = null;
        boolean agentToFlag = false;
        boolean agentFlag = false;
        if (_agentModelList != null && !_agentModelList.isEmpty()) {
            agentModelList = _agentModelList;
            agentFlag = true;
        } else if (_agentModelToList != null && !_agentModelToList.isEmpty()) {
            agentModelList = _agentModelToList;
            agentToFlag = true;
        }

        List<AgentModel> edocAgent = new ArrayList<AgentModel>();
        if (agentModelList != null && !agentModelList.isEmpty()) {
            java.util.Date now = new java.util.Date();
            for (AgentModel agentModel : agentModelList) {
                if (agentModel.isHasEdoc() && agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now)) {
                    edocAgent.add(agentModel);
                }
            }
        }
//    	boolean isProxy = false;
        if (edocAgent != null && !edocAgent.isEmpty()) {
            //isProxy = true;
        } else {
            agentFlag = false;
            agentToFlag = false;
        }

        //在办表加入暂存代办提醒时间列
        String zcdbTimeSelect = "";
        String zcdbTable = "";
        if (substateArray.length > 0) {
            int substate = substateArray[0];//子状态(v3x_affair的sub_state)
            if (state == StateEnum.col_pending.key() && substate == SubStateEnum.col_pending_ZCDB.getKey()) { //待办时，如果子状态大于0表示加入子状态的查询条件
                zcdbTimeSelect = ",edocZcdb.zcdbTime ";
                zcdbTable = " ,EdocZcdb as edocZcdb ";
            }
        }

        String hql = "select " + selectAffair + zcdbTimeSelect + " from CtpAffair as affair,EdocSummary as summary" + zcdbTable
                + " where";


        /**
         * 收文管理  -- 成文单位
         * cy add
         */
        if ("edocUnit".equals(condition) && StringUtils.isNotBlank(field)) {
            hql = "select " + selectAffair + zcdbTimeSelect + " from CtpAffair as affair,EdocSummary as summary,EdocRegister register " + zcdbTable
                    + " where";
        }

        //GOV-4426 公文管理-收文管理-分发页面已发列表，按登记时间,签收时间 进行搜索，输入内容后点搜索，报红三角
        /**
         * 收文管理  -- 签收时间
         * cy add
         */
        if ("recieveDate".equals(condition) && (Strings.isNotBlank(field) || Strings.isNotBlank(field1))) {
            hql = "select " + selectAffair + zcdbTimeSelect + " from CtpAffair as affair,EdocSummary as summary ,EdocRegister register " + zcdbTable
                    + " where";
        }
        /**
         * 收文管理  -- 登记时间
         * cy add
         */
        if ("registerDate".equals(condition) && (Strings.isNotBlank(field) || Strings.isNotBlank(field1))) {
            hql = "select " + selectAffair + zcdbTimeSelect + " from CtpAffair as affair,EdocSummary as summary,EdocRegister register  " + zcdbTable
                    + " where";
        }

        if ("true".equals(params.get("deduplication"))) {
            hql += " affair.objectId = summary.id and " +
                    " affair.id in ( select max(affair.id) from CtpAffair as affair,EdocSummary as summary " + zcdbTable +
                    " where 1=1 and ";
        }

        StringBuilder userAgent = new StringBuilder();
        if (edocAgent != null && !edocAgent.isEmpty()) {
            if (!agentToFlag) {
                userAgent.append("( (affair.memberId=:user_id) ");
                parameterMap.put("user_id", user_id);
                if (state == StateEnum.col_pending.key() || state == StateEnum.col_done.key()) {
                    if (edocAgent != null && !edocAgent.isEmpty()) {
                        userAgent.append("   or (");
                        int i = 0;
                        for (AgentModel agent : edocAgent) {
                            if (i != 0) {
                                userAgent.append(" or ");
                            }
                            userAgent.append(" (affair.memberId=:edocAgentToId")
                                    .append(i)
                                    .append(" and affair.receiveTime>=:proxyCreateDate")
                                    .append(i);
                            parameterMap.put("edocAgentToId" + i, agent.getAgentToId());
                            parameterMap.put("proxyCreateDate" + i, agent.getStartDate());
                            userAgent.append(" )");
                            i++;
                        }
                        userAgent.append("   )");
                    }
                }
                userAgent.append(")");
            } else {
                if (state == StateEnum.col_pending.key()) {
                    userAgent.append(" affair.memberId=:user_id1");
                    parameterMap.put("user_id1", user_id);

                } else {
                    userAgent.append(" affair.memberId=:user_id");
                    parameterMap.put("user_id", user_id);
                }
            }
        } else {
            userAgent.append(" (affair.memberId=:user_id )");
            parameterMap.put("user_id", user_id);
        }
        hql += userAgent.toString();

        if (params != null) {
            if (params.get("track") != null) {
                int track = (Integer) params.get("track");
                if (track != -1) {
                    hql += " and affair.isTrack=:track ";
                    parameterMap.put("track", (Boolean) (track == 1));
                }
            }
        }

        hql += " and summary.state <> " + CollaborationEnum.flowState.deleted.ordinal();//不读取公文删除。
        hql += " and affair.state=:a_state "
                + " and affair.objectId=summary.id ";


        // GOV-4655  公文管理-签报管理，拟文发起后，发起人在已办内删除，下一节点回退，拟文消失。
//                + " and affair.delete=false ";
        //GOV-4848.【公文管理】-【收文管理】，收文分发员在已分发列表将已分发的收文删除，收文待办人处理时退回公文，退回的数据不见了 start
        if ("backBox".equals(params.get("sendListType")) || "retreat".equals(params.get("sendListType"))) {
            hql += " and (affair.delete=false or affair.delete = true) ";
        } else if ("backBox".equals(params.get("listWaitSend"))) {
            hql += " or (affair.delete=false or affair.delete = true) ";
        } else {
            //	hql += " and affair.delete=false ";
        }


        parameterMap.put("a_state", state);

        //区分是收文办文(1)、收文阅文(2)、分发草稿箱(3)魏俊标
        int processType = (params == null || params.get("processType") == null) ? 0 : (Integer) params.get("processType");
        if ((substateArray.length == 3 || (processType != 0)) && edocType == 1) {
            hql += " and summary.processType=:s_processType ";
            if (substateArray.length == 3) {
                parameterMap.put("s_processType", Long.valueOf(substateArray[2]));
            } else {
                parameterMap.put("s_processType", Long.valueOf(processType));
            }
        }
        if (subEdocType != -1) {
            hql += " and summary.subEdocType=:s_subEdocType ";
            parameterMap.put("s_subEdocType", Long.valueOf(subEdocType));
        }

        if (substateArray.length > 0) {
            int substate = substateArray[0];//子状态(v3x_affair的sub_state)

            if (state == StateEnum.col_pending.key()) { //待办时，如果子状态大于0表示加入子状态的查询条件
                if (substate >= 0) {
                    hql += (substate == SubStateEnum.col_pending_ZCDB.getKey() ? " and edocZcdb.affairId =affair.id " : "") + " and affair.subState=:sub_state "; //用于暂存待办，即在办
                    parameterMap.put("sub_state", substate);
//            	}else{
                    //GOV-4012 收文管理-待阅，暂存待办事项后，从待阅列表消失，跑到待办-在办列表中了
                    //changyi  待阅列表要查询在办的，所以这里要排除阅文
                } else if (processType != 2) {
                    hql += " and affair.subState<>:sub_state "; //用于待办,排除在办的
                    parameterMap.put("sub_state", SubStateEnum.col_pending_ZCDB.getKey());
                }
            } else if (state == StateEnum.col_waitSend.getKey()) {  //待发时，substateArray[0]保存subState，substateArray[3]：1，退件箱2，草稿箱
                if (substateArray.length >= 4) {
                    hql += " and affair.subState in(:sub_states)";
                    List<Integer> subList = new ArrayList<Integer>(2);
                    if (substateArray[3] == 1) {
                        subList.add(SubStateEnum.col_waitSend_sendBack.getKey());
                        subList.add(SubStateEnum.col_waitSend_stepBack.getKey());
                    } else if (substateArray[3] == 2) {
                        subList.add(SubStateEnum.col_waitSend_draft.getKey());
                        subList.add(SubStateEnum.col_waitSend_cancel.getKey());
                    } else if (substateArray[3] == 3) {//待发（包括草稿箱和退件箱）
                        subList.add(SubStateEnum.col_waitSend_sendBack.getKey());
                        subList.add(SubStateEnum.col_waitSend_stepBack.getKey());
                        subList.add(SubStateEnum.col_waitSend_draft.getKey());
                        subList.add(SubStateEnum.col_waitSend_cancel.getKey());
                        subList.add(SubStateEnum.col_pending_specialBacked.getKey());
                    }
                    parameterMap.put("sub_states", subList);
                }
            } else if (state == StateEnum.col_done.getKey()) {//已办时，如果子状态大于0表示加入子状态，否则排除子状态
                if (substate >= 0) {

//            		hql+= " and summary.state=:sub_state "; //已办结
//                    parameterMap.put("sub_state", substate);
                    //GOV-3438 公文管理-发文管理-已办页面，全部显示的条数和未办结与已办结总共显示条数不一致
                    //已办要加入  公文处理时直接 终止的   changyi add
                    hql += " and (summary.state=:sub_state or summary.state=:sub_state2) "; //已办结
                    parameterMap.put("sub_state", substate);
                    parameterMap.put("sub_state2", 1);  //终止后edoc_summary中state值为1
                    int hasArchive = substateArray[1];//是否归档
                    if (hasArchive >= 0) { //归档，0未归档，1已归档
                        hql += " and summary.hasArchive=:has_archive ";
                        parameterMap.put("has_archive", (hasArchive == 0 ? false : true));
                    }
                } else if (substateArray.length == 3 && (substateArray[2] == 2 || substateArray[2] == 1)) {
                    //收文已阅不判断是否结束，先这么写，以后重构
                    int hasArchive = substateArray[1];//是否归档
                    if (hasArchive >= 0) { //归档，0未归档，1已归档
                        hql += " and summary.hasArchive=:has_archive ";
                        parameterMap.put("has_archive", (hasArchive == 0 ? false : true));
                    }
                } else {
                    hql += " and (summary.state = :sub_state) "; //未办结
                    parameterMap.put("sub_state", CollaborationEnum.flowState.run.ordinal());
                }
                if ("doneOver".equals(condition)) {
                    hql += " and summary.completeTime !=null "; //已办结
                }
            }

        }


        if (edocType >= 0) {
            hql += " and affair.app=:a_app ";
            parameterMap.put("a_app", EdocUtil.getAppCategoryByEdocType(edocType).getKey());
        } else if (edocType == -1) {
            hql += " and (affair.app =:a_app1 or affair.app =:a_app2 or affair.app =:a_app3)";
            parameterMap.put("a_app1", ApplicationCategoryEnum.edocRec.getKey());
            parameterMap.put("a_app2", ApplicationCategoryEnum.edocSend.getKey());
            parameterMap.put("a_app3", ApplicationCategoryEnum.edocSign.getKey());
        }

        //跟踪
        if ("isTrack".equals(condition)) {
            String expss = " and affair.isTrack = 1";
            hql = hql + expss;
        }

        //关于已经完成的filter
        if ("finishfilter".equals(condition)) {
            String expss = " and summary.finishDate is not null";
            hql = hql + expss;
        }
        //关于未完成的filter
        if ("notfinishfilter".equals(condition)) {
            String expss = " and summary.finishDate is null";
            hql = hql + expss;
        }

        /**
         * 收文管理  -- 成文单位
         * cy add
         */
        if ("edocUnit".equals(condition) && StringUtils.isNotBlank(field)) {
            paramName = "edocUnit";
            exp0 = " and summary.id = register.distributeEdocId " +
                    " and register.edocUnit like :" + paramName + " ";
            paramValue = "%" + field + "%";
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        }

        /**
         * 收文管理  -- 签收时间
         * cy add
         */
        else if ("recieveDate".equals(condition) && (Strings.isNotBlank(field) || Strings.isNotBlank(field1))) {
            exp0 = " and summary.id = register.distributeEdocId ";

            if (StringUtils.isNotBlank(field)) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(field);
                paramName = "timestamp1";
                exp0 = exp0 + " and register.recTime >= :" + paramName;
                parameterMap.put(paramName, stamp);
            }
            if (StringUtils.isNotBlank(field1)) {
                java.util.Date stamp = Datetimes.getTodayLastTime(field1);
                paramName = "timestamp2";
                exp0 = exp0 + " and register.recTime <= :" + paramName;
                parameterMap.put(paramName, stamp);
            }
            hql = hql + exp0;
        }
        /**
         * 收文管理  -- 登记时间
         * cy add
         */
        else if ("registerDate".equals(condition) && (Strings.isNotBlank(field) || Strings.isNotBlank(field1))) {
            exp0 = " and summary.id = register.distributeEdocId ";

            if (StringUtils.isNotBlank(field)) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(field);
                paramName = "timestamp1";
                exp0 = exp0 + " and register.registerDate >= :" + paramName;
                parameterMap.put(paramName, stamp);
            }
            if (StringUtils.isNotBlank(field1)) {
                java.util.Date stamp = Datetimes.getTodayLastTime(field1);
                paramName = "timestamp2";
                exp0 = exp0 + " and register.registerDate <= :" + paramName;
                parameterMap.put(paramName, stamp);
            }
            hql = hql + exp0;
        }


        /**
         * 到达时间
         * cy add
         */
        else if ("receiveTime".equals(condition) && StringUtils.isNotBlank(field)) {

            if (StringUtils.isNotBlank(field)) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(field);

                paramName = "timestamp1";
                hql = hql + " and affair.receiveTime >= :" + paramName;

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

            if (StringUtils.isNotBlank(field1)) {
                java.util.Date stamp = Datetimes.getTodayLastTime(field1);
                paramName = "timestamp2";
                hql = hql + " and affair.receiveTime <= :" + paramName;

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

        }

        /**
         * 主题词
         * cy add
         */
        else if ("keywords".equals(condition) && !"".equals(field)) {
            paramName = "keywords";
            exp0 = " and summary.keywords like :" + paramName + " ";
            paramValue = "%" + field + "%";
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        }

        /**
         * 文件秘级
         * cy add
         */
        else if ("secretLevel".equals(condition) && Strings.isNotBlank(field)) {
            paramName = "secretLevel";
            exp0 = " and summary.secretLevel = :" + paramName + " ";
            paramValue = field;
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        }
        /**
         * 紧急程度
         * cy add
         */
        //
        if ("urgentLevel".equals(condition) && Strings.isNotBlank(field)) {
            paramName = "urgentLevel";
            exp0 = " and summary.urgentLevel = :" + paramName + " ";
            paramValue = field;
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        } else if ("subject".equals(condition)) {
            paramName = "subject";
            exp0 = " and summary.subject like :" + paramName + " ";
            paramValue = "%" + field + "%";
            //paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        } else if ("docMark".equals(condition)) {
            if (Strings.isNotBlank(field)) {
                paramName = "docMark";
                exp0 = " and summary.docMark like :" + paramName + " ";
                paramValue = "%" + field + "%";
                //paramNameList.add(paramName);
                parameterMap.put(paramName, paramValue);
                hql = hql + exp0;
            }

        } else if ("docInMark".equals(condition)) {
            if (Strings.isNotBlank(field)) {
                paramName = "serialNo";
                exp0 = " and summary.serialNo like :" + paramName + " ";
                paramValue = "%" + field + "%";
                //paramNameList.add(paramName);
                parameterMap.put(paramName, paramValue);
                hql = hql + exp0;
            }
            //行文类型
        } else if ("sendType".equals(condition)) {
            if (Strings.isNotBlank(field)) {
                paramName = "sendType";
                exp0 = " and summary.sendType=:" + paramName + " ";
                paramValue = field;
                //paramNameList.add(paramName);
                parameterMap.put(paramName, paramValue);
                hql = hql + exp0;
            }
            //流程节点
        } else if ("nodePolicy".equals(condition)) {
            if (Strings.isNotBlank(field)) {
                paramName = "nodePolicy";
                exp0 = " and affair.nodePolicy=:" + paramName + " ";
                paramValue = field;
                //paramNameList.add(paramName);
                parameterMap.put(paramName, paramValue);
                hql = hql + exp0;
            }

        }
        /**
         * author:wangwei
         * 退回人  start
         */
        else if ("backBoxPeople".equals(condition)) {
            if (Strings.isNotBlank(field)) {
                paramName = "memberId";
                exp0 = " and affair.extProps like :" + paramName + " ";
                paramValue = field;
                parameterMap.put(paramName, "%" + paramValue + "%");
                hql = hql + exp0;
            }
        }
        //退会时间
        else if ("backDate".equals(condition)) {
            if (StringUtils.isNotBlank(field)) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(field);
                paramName = "timestamp1";
                hql = hql + " and affair.updateDate >= :" + paramName;
                parameterMap.put(paramName, stamp);
            }
            if (StringUtils.isNotBlank(field1)) {
                java.util.Date stamp = Datetimes.getTodayLastTime(field1);
                paramName = "timestamp2";
                hql = hql + " and affair.updateDate <= :" + paramName;
                parameterMap.put(paramName, stamp);
            }
        }
        //退回方式
        else if ("backBox".equals(condition)) {
            if (substateArray.length > 0) {
                int substate = substateArray[0];
                exp0 = " and affair.subState=:sub_state";
                parameterMap.put("sub_state", substate);
                hql = hql + exp0;
            }
        }
        /**
         * wangwei end
         */
        //发文类型
        else if ("cusSendType".equals(condition)) {
            if (Strings.isNotBlank(field)) {
                paramName = "subEdocType";
                exp0 = " and summary.subEdocType=:" + paramName + " ";
                paramValue = field;
                parameterMap.put(paramName, Long.parseLong(paramValue));
                hql = hql + exp0;
            }

        } else if ("createDate".equals(condition)) {
            if (StringUtils.isNotBlank(field)) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(field);

                paramName = "timestamp1";
                hql = hql + " and summary.createTime >= :" + paramName;

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

            if (StringUtils.isNotBlank(field1)) {
                java.util.Date stamp = Datetimes.getTodayLastTime(field1);
                paramName = "timestamp2";
                hql = hql + " and summary.createTime <= :" + paramName;

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }
        } else if ("startMemberName".equals(condition)) {
            int index = hql.indexOf("where");
            hql = hql.substring(0, index) + "," + OrgMember.class.getName() + " as mem " + hql.substring(index);
            hql += " and affair.senderId=mem.id"
                    + " and mem.name like :startMemberName";
            paramName = "startMemberName";
            paramValue = "%" + SQLWildcardUtil.escape(field) + "%";
            parameterMap.put(paramName, paramValue);

        } else if ("expectprocesstime".equals(condition)) {
            if (StringUtils.isNotBlank(field)) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(field);
                hql = hql + "  and affair.expectedProcessTime >= :a_createDate1 and affair.expectedProcessTime is not null ";
                parameterMap.put("a_createDate1", stamp);
            }
            if (StringUtils.isNotBlank(field1)) {
                java.util.Date stamp = Datetimes.getTodayLastTime(field1);
                hql = hql + " and affair.expectedProcessTime <= :a_createDate2 and affair.expectedProcessTime is not null ";
                parameterMap.put("a_createDate2", stamp);
            }
        }
        if (edocType >= 0) {
            hql += " and summary.edocType=:s_edocType";
            parameterMap.put("s_edocType", edocType);

        } else if (edocType == -1) {
        	/*hql+=" and (summary.edocType=:edocType1 or summary.edocType =:edocType2 or summary.edocType =:edocType3)";
        	parameterMap.put("edocType1", EdocEnum.edocType.sendEdoc.ordinal());
			parameterMap.put("edocType2", EdocEnum.edocType.recEdoc.ordinal());
			parameterMap.put("edocType3",EdocEnum.edocType.signReport.ordinal());*/
        }

        hql += " and affair.delete = :affairDelete ";
        parameterMap.put("affairDelete", false);

        //同一流程只显示最后一条
        if ("true".equals(params.get("deduplication"))) {
            hql += " group by summary.id ) ";
            hql += " and " + userAgent;
        }


        if (state == StateEnum.col_pending.key()) {
            hql = hql + " order by affair.receiveTime desc";
        } else if (state == StateEnum.col_done.key()) {
            hql = hql + " order by affair.completeTime desc";
        } else {
            hql = hql + " order by affair.createDate desc";
        }
        List result = edocSummaryDao.find(hql.toString(), parameterMap);

        java.util.Date early = null;
        if (edocAgent != null && !edocAgent.isEmpty())
            early = edocAgent.get(0).getStartDate();

        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>(result.size());
        for (int i = 0; i < result.size(); i++) {
            Object[] object = (Object[]) result.get(i);
            CtpAffair affair = new CtpAffair();
            EdocSummary summary = new EdocSummary();
            make(object, summary, affair);

            try {
                V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                summary.setStartMember(member);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
            //开始组装最后返回的结果
            EdocSummaryModel model = new EdocSummaryModel();
            if (state == StateEnum.col_waitSend.key()) {
                model.setWorkitemId(null);
                model.setCaseId(null);
                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setSummary(summary);
                model.setAffairId(affair.getId());
                model.setAffair(affair);

            } else if (state == StateEnum.col_sent.key()) {
                model.setWorkitemId(null);
                model.setCaseId(summary.getCaseId() + "");
                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setSummary(summary);
                model.setAffairId(affair.getId());
                //设置流程是否超期标志
                java.sql.Timestamp startDate = summary.getCreateTime();
                java.sql.Timestamp finishDate = summary.getCompleteTime();
                Date now = new Date(System.currentTimeMillis());

                if (summary.getDeadline() != null && summary.getDeadline() != 0) {
                    Long deadline = summary.getDeadline() * 60000;
                    if (finishDate == null) {
                        if ((now.getTime() - startDate.getTime()) > deadline) {
                            summary.setWorklfowTimeout(true);
                        }
                    } else {
                        Long expendTime = summary.getCompleteTime().getTime() - summary.getCreateTime().getTime();
                        if ((deadline - expendTime) < 0) {
                            summary.setWorklfowTimeout(true);
                        }
                    }
                }
            } else if (state == StateEnum.col_done.key()) {
                model.setWorkitemId(affair.getObjectId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                model.setAffairId(affair.getId());
                model.setAffair(affair);
            } else if (state == StateEnum.col_pending.key()) {
                model.setWorkitemId(affair.getObjectId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                model.setAffairId(affair.getId());
                model.setAffair(affair); //应M1要求，加入setAffair
                // OA-8206  发文管理--待办列表查看流程期限，显示的方式不对，应该是设置了2小时就显示2小时，不是以具体时间方式显示出来   start
                Long deadline = null;
                if (affair.getDeadlineDate() == null) {
                    deadline = 0L;
                } else {
                    deadline = affair.getDeadlineDate() == null ? null : ((Number) affair.getDeadlineDate()).longValue();
                }

                model.setDeadLine(deadline);
                model.setDeadlineDisplay(EdocHelper.getDeadLineName(summary.getDeadlineDatetime()));
                //  OA-8206  发文管理--待办列表查看流程期限，显示的方式不对，应该是设置了2小时就显示2小时，不是以具体时间方式显示出来   end
                //将暂存待办提醒时间加入列数据中
                if (substateArray.length > 0) {
                    int substate = substateArray[0];//子状态(v3x_affair的sub_state)
                    if (substate == SubStateEnum.col_pending_ZCDB.getKey()) { //待办时，如果子状态大于0表示加入子状态的查询条件
                        if (object[object.length - 1] != null) {
                            model.setZcdbTime(new java.sql.Date(((Timestamp) object[object.length - 1]).getTime()));
                        }
                    }
                }
            } else {
                model.setWorkitemId(affair.getObjectId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                model.setAffairId(affair.getId());
            }
            int affairState = affair.getState();
            if (affairState == StateEnum.col_waitSend.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.WaitSend.name());
            } else if (affairState == StateEnum.col_sent.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.Sent.name());
            } else if (affairState == StateEnum.col_done.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.Done.name());
            } else if (affairState == StateEnum.col_pending.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.Pending.name());
            }

            model.setFinshed(summary.getCompleteTime() != null);

            model.setBodyType(affair.getBodyType());

            //公文状态
            Integer sub_state = affair.getSubState();
            if (sub_state != null) {
                model.setState(sub_state.intValue());
            }

            //是否跟踪
            Integer isTrack = affair.getTrack();
            if (isTrack != null) {
                model.setTrack(isTrack.intValue());
            }

            //催办次数
            Integer hastenTimes = affair.getHastenTimes();
            if (hastenTimes != null) {
                model.setHastenTimes(hastenTimes);
            }

            //检查是否有附件
            //TODO(5.0sprint3) model.setHasAttachments(affair.isHasAttachments());

            //是否超期
            Boolean overtopTime = affair.isCoverTime();
            if (overtopTime != null) {
                model.setOvertopTime(overtopTime.booleanValue());
            }

            //提前提醒
            Long advanceRemind = affair.getRemindDate();
            if (advanceRemind == null) {
                advanceRemind = 0L;
            }
            model.setAdvanceRemindTime(advanceRemind);

            //协同处理期限
            Long deadLine = affair.getDeadlineDate();
            if (deadLine == null) {
                deadLine = 0L;
            }
            model.setDeadLine(deadLine);
            model.setDeadlineDisplay(EdocHelper.getDeadLineName(summary.getDeadlineDatetime()));
            V3xOrgMember member = null;
            //是否代理
            if (state == StateEnum.col_done.key()) {
                model.setAffair(affair);
			    /*if(affair.getTransactorId() != null){
					try {
						if(affair.getMemberId().longValue() == user.getId().longValue())
                            member = orgManager.getMemberById(affair.getTransactorId());
						else{
							member = orgManager.getMemberById(affair.getMemberId());
						}
					    model.setProxyName(member.getName());
					    model.setProxy(true);
					} catch (BusinessException e) {
						log.error("", e);
					}
			    }else{
			    	if(affair.getMemberId().longValue() != user.getId().longValue()){
			    		try{
			    			member = orgManager.getMemberById(affair.getMemberId());
			    			model.setProxyName(member.getName());
			    		}catch(BusinessException e){
			    			log.error("", e);
			    		}
			    		model.setAgentDeal(true);
			    		model.setProxy(true);
			    	}
			    }*/
            }

            if (state == StateEnum.col_pending.key() && agentFlag && affair.getMemberId().longValue() != user.getId().longValue()) {
                Long proxyMemberId = affair.getMemberId();
                try {
                    member = orgManager.getMemberById(proxyMemberId);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
                model.setProxyName(member.getName());
                model.setProxy(true);
            } else if (state == StateEnum.col_pending.key() && agentToFlag && early != null && early.before(affair.getReceiveTime())) {
                model.setProxy(true);
            }

            model.setNodePolicy(affair.getNodePolicy());

            if (affair.getCompleteTime() != null) {
                model.setDealTime(new Date(affair.getCompleteTime().getTime()));
            }
            model.setSurplusTime(calculateSurplusTime(affair.getReceiveTime(), deadLine == null ? null : ((Number) deadLine).longValue()));

            //设置标题
            String subject = "";
            if (StateEnum.col_done.getKey() == affair.getState().intValue()) {//已办 图标提示
                subject = EdocUtil.showSubjectOfSummary4Done(affair, -1);
            } else {
                //subject = ColUtil.showSubjectOfSummary(summary, false, -1, "");
                subject = EdocUtil.showSubjectOfEdocSummary(summary, model.isProxy(), -1, model.getProxyName(), false);
            }
            summary.setSubject(subject);
            model.setSummary(summary);
            //设置当前处理人信息
            model.setCurrentNodesInfo(EdocHelper.parseCurrentNodesInfo(summary));
            models.add(model);
        }
        return models;
    }

    public List<EdocSummaryModel> queryByCondition4Quote(ApplicationCategoryEnum appEnum, String condition,
                                                         String field, String field1) {
        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>();
        long user_id = AppContext.getCurrentUser().getId();

        List<Object> objects = new ArrayList<Object>();

        String hql = "select " + selectAffair + " from CtpAffair as affair,EdocSummary as summary"
                + " where (affair.objectId=summary.id) and (affair.memberId=?)"
                //+ " and (affair.state=" + state.key() + ")"
                + " and (affair.state in (" + StateEnum.col_done.getKey() + "," + StateEnum.col_pending.getKey() + "," + StateEnum.col_sent.getKey() + ") )"
                + " and (affair.app=?)"
                + " and affair.delete=false";
        objects.add(user_id);
        objects.add(appEnum.key());

        if (condition != null) {
            if ("subject".equals(condition) && Strings.isNotBlank(field)) {
                hql += " and (summary.subject like ?)";

                objects.add("%" + SQLWildcardUtil.escape(field) + "%");
            } else if ("docMark".equals(condition) && Strings.isNotBlank(field)) {
                hql += " and (summary.docMark like ?)";

                objects.add("%" + SQLWildcardUtil.escape(field) + "%");
            } else if ("docInMark".equals(condition) && Strings.isNotBlank(field)) {
                hql += " and (summary.serialNo like ?)";

                objects.add("%" + SQLWildcardUtil.escape(field) + "%");
            } else if ("startMemberName".equals(condition) && Strings.isNotBlank(field)) {
                hql = "select " + selectAffair + " from CtpAffair as affair," + OrgMember.class.getName() + " as mem,EdocSummary as summary"
                        + " where (affair.senderId=mem.id) and (affair.objectId=summary.id) and (affair.memberId=?)"
                        + " and (affair.state in (" + StateEnum.col_pending.getKey() + "," + StateEnum.col_sent.getKey() + "," + StateEnum.col_done.getKey() + ") )"
                        + " and (affair.app=?)"
                        + " and affair.delete=false"
                        //+ " and affair.archiveId is null"
                        + " and (mem.name like ?)";

                objects.add("%" + SQLWildcardUtil.escape(field) + "%");
            } else if ("createDate".equals(condition)) {
                if (StringUtils.isNotBlank(field)) {
                    hql += " and summary.createTime >= ?";
                    java.util.Date stamp = Datetimes.getTodayFirstTime(field);

                    objects.add(stamp);
                }
                if (StringUtils.isNotBlank(field1)) {
                    hql += " and summary.createTime <= ?";
                    java.util.Date stamp = Datetimes.getTodayLastTime(field1);

                    objects.add(stamp);
                }
            }
        }

        String selectHql = hql + " order by summary.createTime desc";

        List result = edocSummaryDao.find(selectHql, null, objects);

        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object[] object = (Object[]) result.get(i);
                CtpAffair affair = new CtpAffair();
                EdocSummary summary = new EdocSummary();
                make(object, summary, affair);

                try {
                    V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                    summary.setStartMember(member);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }

                //开始组装最后返回的结果�?
                EdocSummaryModel model = new EdocSummaryModel();

                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setWorkitemId(affair.getObjectId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                model.setAffairId(affair.getId());
                model.setBodyType(affair.getBodyType());

                Long deadlineDate = affair.getDeadlineDate();
                if (deadlineDate == null) {
                    deadlineDate = 0L;
                }
                model.setSurplusTime(calculateSurplusTime(affair.getReceiveTime(), deadlineDate));

                models.add(model);
                //协同状�?
                Integer sub_state = affair.getState();
                if (sub_state != null) {
                    model.setState(sub_state.intValue());
                }

                //是否跟踪
                Integer isTrack = affair.getTrack();
                if (isTrack != null) {
                    model.setTrack(isTrack.intValue());
                }


            }
        }

        return models;
    }

    public List<EdocSummaryModel> queryDraftList(int edocType, Map<String, Object> params) throws EdocException {
        return queryDraftList(edocType, -1, params, -1);
    }

    public List<EdocSummaryModel> queryDraftList(int edocType, long subEdocType, Map<String, Object> params, int... substateArray) throws EdocException {
        List<EdocSummaryModel> summaryModelList = queryByCondition1(edocType, "", null, null, subEdocType, StateEnum.col_waitSend.key(), params, substateArray);
        return summaryModelList;
    }


    public List<EdocSummaryModel> queryFinishedList(int edocType, Map<String, Object> params) throws EdocException {
        List<EdocSummaryModel> result = queryByCondition(edocType, "", null, null, StateEnum.col_done.key(), params);
        return result;
    }

    public List<EdocSummaryModel> querySentList(int edocType, Map<String, Object> params) throws EdocException {
        return querySentList(edocType, -1, params);
    }

    public List<EdocSummaryModel> querySentList(int edocType, long subEdocType, Map<String, Object> params) throws EdocException {
        List<EdocSummaryModel> summaryModelList = queryByCondition1(edocType, "", null, null, subEdocType, StateEnum.col_sent.key(), params);
        return summaryModelList;
    }

    public List<EdocSummaryModel> queryTodoList(int edocType, Map<String, Object> params) throws EdocException {
        List<EdocSummaryModel> result = queryByCondition(edocType, "", null, null, StateEnum.col_pending.key(), params);
        return result;
    }

    public List<EdocSummaryModel> queryTrackList(int edocType, Map<String, Object> params) throws EdocException {
        List<EdocSummaryModel> summaryModelList = queryByCondition(edocType, "isTrack", null, null, 0, params);
        return summaryModelList;
    }

    /**
     * 设置是否为联合发文标志
     */
    private void _setIsUnit(EdocSummary summary) {
        Long edocFormId = summary.getFormId();
        if (edocFormId != null) {
            EdocForm ef = edocFormManager.getEdocForm(edocFormId);
            if (ef != null) {
                summary.setIsunit(ef.getIsunit());
            }
        }
    }

    public void delOrUpdateProcessLog(EdocSummary summary, int subState, String oldProcessId) {
        boolean isQuickSend = summary.getIsQuickSend() == null ? false : summary.getIsQuickSend();
        if (!isQuickSend) {
            // 指定回退-提交回退者的情况下不删除，其他的情况下都删除
            if (subState == SubStateEnum.col_pending_specialBacked.getKey() || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()
                    || subState == SubStateEnum.col_waitSend_cancel.getKey() || subState == SubStateEnum.col_waitSend_stepBack.getKey()) {
                if (Strings.isNotBlank(oldProcessId)) {
                    List<ProcessLog> logs = processLogManager.getLogsByProcessId(Long.parseLong(oldProcessId), false);
                    if (!CollectionUtils.isEmpty(logs)) {
                        for (ProcessLog processLog : logs) {
                            if (summary.getCaseId() != null) {
                                processLog.setProcessId(Long.valueOf(summary.getProcessId()));
                            }
                            processLogManager.update(processLog);
                        }
                    }
                }
            } else {
                if (Strings.isNotBlank(summary.getProcessId())) {
                    processLogManager.deleteLog(Long.parseLong(summary.getProcessId()));
                }
            }
        }
    }

    @Override
    public Long runCase(EdocSummary summary,
                        EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType,
                        Map options, String from, Long agentToId, boolean isNewSent, String process_xml, String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowId) throws BusinessException {

        User user = AppContext.getCurrentUser();
        //快速发文的标识
        boolean isQuickSend = summary.getIsQuickSend() == null ? false : summary.getIsQuickSend();
        boolean isFinish = false;
        Long affairId = 0L;
        String processId = summary.getProcessId();
        try {
            V3xOrgMember sender = orgManager.getMemberById(user.getId());
            if (agentToId != null) {
                sender = orgManager.getMemberById(agentToId);
            }

            body.setIdIfNew();
            body.setEdocSummary(summary);
            summary.getEdocBodies().add(body);
            //查找出所有的edocBody对象

            List<EdocBody> allBody = edocBodyDao.getBodyByIdAndNum(summary.getId());

            if (allBody != null) {
                for (EdocBody eb : allBody) {
                    //防止含有两个相同的对象或者相同正文编号（0，1，2）的对象。
                    if (eb.getContentNo() != null
                            && body.getContentNo() != null
                            && eb.getContentNo().intValue() != body.getContentNo().intValue())
                        summary.getEdocBodies().add(eb);
                }
            }


            //生成流程模板对象
            //-----------性能优化，如果是新建公文发送，就不执行查询了，因为还没有记录
            EdocSummary _summary = null;
            if (!isNewSent) {
                _summary = edocSummaryDao.get(summary.getId());
            }
            List<EdocOpinion> opinions = null;
            CtpAffair senderAffair = null;
            String oldprocessId = null;
            int subState = 0;//初始验证指定回退的状态值，用来验证当前流程是否为指定回退流程
            if (_summary != null) {
                senderAffair = affairManager.getSenderAffair(summary.getId());
                subState = senderAffair.getSubState().intValue();
                oldprocessId = senderAffair.getProcessId();
                //当下级文单重新编辑发送后，删除上级文单中的这个下级文单的意见
                delOptionsBySubOpId(_summary.getId());

                if (senderAffair != null && senderAffair.getSubState() != SubStateEnum.col_pending_specialBacked.key()
                        && senderAffair.getSubState() != SubStateEnum.col_pending_specialBackToSenderCancel.key()
                        && subState != SubStateEnum.col_waitSend_stepBack.key()) {

                    edocSummaryDao.delete(_summary);

                    //指定回退到发起人意见保留
                    deleteOpinionBySummaryId(_summary.getId());

                    affairManager.deleteByObjectId(EdocUtil.getAppCategoryByEdocType(_summary.getEdocType()), _summary.getId());
                    //删除处理意见，手写批注
                    htmSignetManager.deleteBySummaryIdAndType(summary.getId(), V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
                }


                /**
                 * 下级单位收文撤销后，再发起时，将该下级单位公文上报的意见删除
                 */
                List<EdocOpinion> els = edocExchangeTurnRecManager.getDelStepBackSupOptions(_summary.getId());
                if (els != null && els.size() > 0) {
                    for (EdocOpinion op : els) {
                        deleteOpinion(op);
                    }
                }
                /**
                 * 上级单位收文撤销后，再发起时，将收文的转收文信息删除
                 */
                edocExchangeTurnRecManager.delTurnRecByEdocId(_summary.getId());

            }
            //保存colsummary、body
            summary.setIdIfNew();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            AffairData affairData = EdocHelper.getAffairData(summary, user);
            CtpTemplate templete = null;
            String workflowTemplateId = "-1";
            if (summary.getTempleteId() != null) {
                templete = templateManager.getCtpTemplate(summary.getTempleteId());
                //这里原来存放templete的workflowId，原因不明。这样会导致流程绩效统计不出结果，因此修改为templeteId
                affairData.setTemplateId(summary.getTempleteId());
                if (null != templete && templete.getWorkflowId() != null && !(templete.isSystem() || templete.getFormParentid() != null)) {
                    //summary.setTempleteId(null);
                    affairData.setTemplateId(null);
                }
                if (templete != null) {
                    workflowTemplateId = String.valueOf(templete.getWorkflowId());
                }
            }

            // 保存督办
            if (!isQuickSend) {// 快速发文--不需要生成工作流数据
                // 删除已有的督办信息
                CtpSuperviseDetail detail = superviseManager.getSupervise(summary.getId());
                if (detail != null) {
                    superviseManager.deleteSupervisorsByDetailId(detail.getId());
                }
            }
            String caseId[] = null;
            if (!isQuickSend) {// 快速发文--不需要生成工作流数据
                WorkflowBpmContext context = new WorkflowBpmContext();
                context.setAppName("edoc");
                context.setDebugMode(false);
                context.setStartUserId(String.valueOf(sender.getId()));
                context.setStartUserName(sender.getName());
                // OA-45651分支条件设置成：当前节点，集团基准岗（按照发起者登录当前来判断），兼职人员在兼职当前中发起流程时判断错误
                context.setStartAccountId(String.valueOf(user.getLoginAccount()));
                context.setSelectedPeoplesOfNodes(workflowNodePeoplesInput);// xuan
                // ren
                context.setConditionsOfNodes(workflowNodeConditionInput);// xuan
                // fen
                // zhi
                context.setCurrentActivityId("start");
                context.setBusinessData("isStart", "true");//新发流程标记
                if (Strings.isNotBlank(process_xml)) {// 编辑过的自由公文流程
                    context.setProcessXml(process_xml);
                }
                Long case_Id = 0l;
                if (_summary != null) {// 保存待发直接发送、撤销的、指定回退给发起者【直接提交给我】,processId肯定有值
                    // OA-48335test01调用公文模板发送流程后，单位管理员会签了多个节点，处理人员撤销或回退公文后，发起人待发中重新调用模板查看流程是模板流程，
                    // 但是发送之后是之前单位管理员修改后的流程
                    // processId在前面已经有值了，这里不再获得了，如果再从_summary中取会有问题
                    // processId = _summary.getProcessId();
                    case_Id = _summary.getCaseId();
                    // if(Strings.isNotBlank(processId)){
                    // context.setProcessId(processId);
                    // }
                }
                V3xOrgAccount account;
                try {
                    account = orgManager.getAccountById(user.getAccountId());
                    context.setStartAccountName(account.getName());
                } catch (BusinessException e1) {
                    LOGGER.error("", e1);
                }

                context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
                context.setAppObject(summary);
                // 首页栏目的扩展字段设置--公文文号、发文单位等--start
                context.setBusinessData("edoc_send_doc_mark", summary.getDocMark());
                context.setBusinessData("edoc_send_send_unit", summary.getSendUnit());
                context.setCurrentUserId(user.getId().toString());
                context.setCurrentUserName(user.getName());
                if (case_Id != null)
                    context.setCaseId(case_Id);
                // 首页栏目的扩展字段设置--公文文号、发文单位等--end
                // context.setBusinessData("bizObject", wfContextBizObject);
                try {
                    if (null != affairData.getTemplateId() && !"".equals(affairData.getTemplateId().toString().trim())
                            && Strings.isNotBlank(workflowId) && Strings.isBlank(processId)) {
                        context.setProcessTemplateId(workflowId);// 工作流xml模板ID
                        context.setProcessId(null);
                        caseId = wapi.transRunCaseFromTemplate(context);
                        Long templeteAccountId = templete != null ? templete.getOrgAccountId() : user.getLoginAccount(); // 获取模板所在的单位id
                        // 该处需要的是表wf_process_templete中的id，而ctp_affair中存的是ctp_templete中的id
                        // OA-75107 拟文时调用模板，发送后查看列表，找不到这条公文了
                        if ("null".equals(workflowTemplateId) || "undefinde".equals(workflowTemplateId)) {
                            workflowTemplateId = null;
                        }
                        EdocUtil.updatePermissinRef(affairData.getModuleType(), process_xml, "-1", workflowTemplateId,
                                templeteAccountId);
                    } else {
                        // 缓存caseId
                        context.setProcessId(processId);
                        context.setProcessTemplateId(null);
                        caseId = wapi.transRunCase(context);
                        EdocUtil.updatePermissinRef(affairData.getModuleType(), process_xml, caseId[1],
                                "-1", user.getLoginAccount());
                    }

                    isFinish = context.isStartFinished();

                } catch (BPMException e) {
                    LOGGER.error("发送公文调用工作流接口出错", e);
                    throw new BusinessException(e.getMessage());
                }

                summary.setProcessId(caseId[1]);
                summary.setCaseId(Long.parseLong(caseId[0]));

            } else {// 快速发文
                summary.setProcessId("0");
                summary.setCaseId(0L);
            }


            if (body.getCreateTime() == null) {
                body.setCreateTime(now);
            }
            if (summary.getStartTime() == null) {
                summary.setStartTime(now);
            }
            summary.setStartUserId(agentToId == null ? user.getId() : agentToId);

            CtpAffair affair = null;
            if (null != senderAffair) {
                affair = senderAffair;
            } else {
                affair = new CtpAffair();
                affair.setIdIfNew();
            }
            affairId = affair.getId();
            _setIsUnit(summary);

            //OA-16122   首页--我的模板，调用公文模板发送后，该模板没有记录为最近使用模板
            if (summary.getTempleteId() != null) {
                templateManager.updateTempleteHistory(user.getId(), summary.getTempleteId());
            }

            boolean isSpecialBacked = false;
            boolean isSpecialBackToSenderCancel = false;
            if (subState == SubStateEnum.col_pending_specialBacked.getKey() || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                isSpecialBacked = true;
                if (subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                    isSpecialBackToSenderCancel = true;
                }
            }
            LOGGER.info("撤销后重新执行的时候需要更新对应的权限内" + summary.getProcessId() + "oldprocessId:" + processId + "subState:" + subState);
            //删除或者更新原来的流程
            delOrUpdateProcessLog(summary, subState, processId);

            if (!isSpecialBacked) {
                affair.setCreateDate(now);
            } else {
                summary.setCreateTime((Timestamp) affair.getCreateDate());
                if (summary.getStartTime() == null) {
                    summary.setStartTime((Timestamp) affair.getCreateDate());
                }
            }


            //待办人在生成事项的时候已经更新了不用再次更新了
//	          EdocHelper.updateCurrentNodesInfo(summary, false);


            if (senderAffair != null && (senderAffair.getSubState() == SubStateEnum.col_pending_specialBacked.key()
                    || senderAffair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()
                    || senderAffair.getSubState() == SubStateEnum.col_waitSend_stepBack.key())) {
                edocSummaryManager.updateEdocSummary(summary, true);
            } else {
                edocSummaryManager.saveEdocSummary(summary, true);
            }

            List<Long> sendOptionsIds = new ArrayList<Long>();
            List<EdocOpinion> listSendOptions = edocOpinionDao.getSenderOpinions(summary.getId());
            for (EdocOpinion option : listSendOptions) {
                sendOptionsIds.add(option.getId());
            }
            if (Strings.isNotEmpty(sendOptionsIds)) {
                edocOpinionDao.deleteOpinionById(sendOptionsIds);
            }

            //附言内容为空，就不记录了
            if (senderOpinion.getContent() != null && !"".equals(senderOpinion.getContent())) {
                senderOpinion.setEdocSummary(summary);
                senderOpinion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
                senderOpinion.setCreateTime(now);
                senderOpinion.setCreateUserId(user.getId());
                summary.getEdocOpinions().add(senderOpinion);

                saveOpinion(senderOpinion);
            }

            /** 被指定退回 **/
            if (senderAffair != null && (senderAffair.getSubState() == SubStateEnum.col_pending_specialBacked.getKey()
                    //OA-44446撤销后的公文被编辑后重新发起，待办还能看到撤销意见，处理后已办理的文单上也有撤销意见
                    /*||senderAffair.getSubState() == SubStateEnum.col_waitSend_draft.getKey()*/)) {
//            	List<EdocOpinion> opinionList = edocOpinionDao.findEdocOpinion(summary.getId(), senderOpinion.getCreateUserId(), senderOpinion.getPolicy());
//            	if(Strings.isNotEmpty(opinionList)) {
//            		EdocOpinion opinion = opinionList.get(opinionList.size()-1);
//        			senderOpinion.setId(opinion.getId());
//        			updateOpinion(senderOpinion);
//            	}
                opinions = edocOpinionDao.findEdocOpinionBySummaryId(summary.getId(), true);
                if (opinions != null) {
                    for (EdocOpinion opinion : opinions) {
                        opinion.setEdocSummary(_summary);
                        if (senderAffair.getSubState() == SubStateEnum.col_pending_specialBacked.key()) {
                            if (senderOpinion.getId().longValue() != opinion.getId().longValue()) {
                                updateOpinion(opinion);
                            }
                        } else {
                            saveOpinion(opinion);
                        }
                    }
                }
            }
            ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
            affair.setApp(app.key());
            // wangjingjing 发文阅文 发文办文 begin

            Integer subApp = null;
            if (ApplicationCategoryEnum.edocRec.equals(app) && null != summary.getProcessType()) {
                if (2 == summary.getProcessType().intValue()) {
                    subApp = ApplicationSubCategoryEnum.edocRecRead.getKey();
                } else if (1 == summary.getProcessType().intValue()) {
                    subApp = ApplicationSubCategoryEnum.edocRecHandle.getKey();
                }
            }
            if (null != subApp) {
                affair.setSubApp(subApp);
            }
            // wangjingjing 发文阅文 发文办文 end
            affair.setSubject(summary.getSubject());
            affair.setReceiveTime(now);
            affair.setUpdateDate(now);
            //收文登记的时候可能是代理人登记的。
            if (agentToId != null) {
                affair.setMemberId(agentToId);
                affair.setSenderId(agentToId);
                affair.setTransactorId(user.getId());
            } else {
                affair.setMemberId(user.getId());
                affair.setSenderId(user.getId());
            }
            affair.setObjectId(summary.getId());
            affair.setSubObjectId(null);
            affair.setState(StateEnum.col_sent.key());
            affair.setSubState(SubStateEnum.col_normal.key());
            affair.setTrack((isQuickSend || senderOpinion.affairIsTrack == false) ? 0 : 1);

            boolean changeBody = !(affair.getBodyType() == null || summary.getFirstBody().getContentType().equals(affair.getBodyType()));

            affair.setBodyType(summary.getFirstBody().getContentType());
            AffairUtil.setHasAttachments(affair, summary.isHasAttachments());
            if (summary.getUrgentLevel() != null && !"".equals(summary.getUrgentLevel())) {
                affair.setImportantLevel(Integer.parseInt(summary.getUrgentLevel()));
            }
            affair.setTempleteId(summary.getTempleteId());
            affair.setDelete(false);
            if (CollaborationEnum.flowState.finish.ordinal() == summary.getState() || CollaborationEnum.flowState.terminate.ordinal() == summary.getState()) {
                affair.setFinish(true);
            } else {
                affair.setFinish(false);
            }
            affair.setSenderId(user.getId());

            affair.setProcessId(summary.getProcessId());
            affair.setCaseId(summary.getCaseId());
            affair.setOrgAccountId(summary.getOrgAccountId());
            affair.setSummaryState(summary.getState());

            //首页栏目的扩展字段设置--公文文号、发文单位、流程期限等--start
            Map<String, Object> extParam = EdocUtil.createExtParam(summary);
            //原来没有指定回退，所以不需要批量更新扩展字段的公文文号
            if (isSpecialBacked) {
                List<CtpAffair> affairs = affairManager.getAffairs(app, summary.getId());
                for (CtpAffair ctpAffair : affairs) {
                    affairSetExtProp(ctpAffair, extParam);
                    affairManager.updateAffair(ctpAffair);
                }
            } else {
                affairSetExtProp(affair, extParam);
            }
            //首页栏目的扩展字段设置--公文文号、发文单位等--end

            if (Strings.isNotBlank(summary.getForwardMember())) {
                affair.setForwardMember(summary.getForwardMember());
            }
            if (null != senderAffair) {

                if (changeBody) {
                    Map<String, Object> columnValue = new HashMap<String, Object>(1);
                    columnValue.put("bodyType", affair.getBodyType());
                    affairManager.update(columnValue, new Object[][]{{"objectId", affair.getObjectId()}});
                }

                affairManager.updateAffair(affair);
            } else {
                affairManager.save(affair);
            }

            // 保存督办，需要在保存affair后才保存督办。
            if (!isQuickSend) {// 快速发文--不需要生成工作流数据
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("contentType", body.getContentType());
                this.saveEdocSupervise4NewEdoc(summary, true, param);
            }

            String params = caseId != null ? caseId[2] : "";
            //EdocHelper.checSecondNodeMembers(bPMProcess,flowData.getCondition()) ;
            List<ProcessLogDetail> allProcessLogDetailList = wapi.getAllWorkflowMatchLogAndRemoveCache(workflowNodeConditionInput);
            if ("transmitSend".equals(from)) {
                if (!isQuickSend) {//快速发文--不需要生成流程日志
                    this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l,
                            ProcessLogAction.processEdoc, allProcessLogDetailList, String.valueOf(ProcessLogAction.ProcessEdocAction.fowardIssuing.getKey()));
                }

                this.appLogManager.insertLog(user, AppLogAction.Edoc_Forward, user.getName(), summary.getSubject());
            } else if ("register".equals(from)) {
                if (!isQuickSend) {//快速发文--不需要生成流程日志
                    this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.register, allProcessLogDetailList, params);
                }

                if (EdocHelper.isG6Version()) {
                    this.appLogManager.insertLog(user, AppLogAction.Edoc_receive_distribute, user.getName(), summary.getSubject());
                }
            } else if ("forwordtosend".equals(from)) {
                this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.drafting, params);
                this.appLogManager.insertLog(user, AppLogAction.Edoc_forword_send, user.getName(), summary.getSubject());
            } else {
                if (summary.getEdocType() == 1) { //收文分发
                    //OA-44190 收文登记：当处理节点指定回退给发起人-提交回退者，发起人重新发送后流程日志中应该记录处理提交而不是登记
                    if (isSpecialBacked) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.commit, allProcessLogDetailList, params);
                    } else if (!isQuickSend) {//快速发文--不需要生成流程日志
                        if (EdocHelper.isG6Version()) {
                            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.distributer, allProcessLogDetailList, params);
                        } else {
                            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.register, allProcessLogDetailList, params);
                        }
                    }
                    String distributeLog = SystemProperties.getInstance().getProperty("edoc.hasDistribute");
                    if ("true".equals(distributeLog)) {
                        this.appLogManager.insertLog(user, AppLogAction.Edoc_receive_distribute, user.getName(), summary.getSubject());
                    }

                } else { //其他，包括发文拟文
                    //OA-41692 公文指定回退给发起人后，发起人再次发送，在流程日志中记录成了拟文，应该记录成提交
                    if (isSpecialBacked) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.commit, allProcessLogDetailList, params);
                    } else if (!isQuickSend) {//快速发文--不需要生成流程日{
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.drafting, allProcessLogDetailList, params);
                    }
                    this.appLogManager.insertLog(user, AppLogAction.Edoc_Send, user.getName(), summary.getSubject());
                }


            }

            //GOV-4775 公文统计，收文统计不出手工登记和二维码登记的公文
            //其实如果这里注释了，电子公文也无法统计出来
            //就是在发文的时候加入到统计表的
            //添加到公文统计表
            edocStatManager.createState(summary, user);


            //如果收文和签报设置了预归档目录，则流程发起后就自动归档，V320工作项
            if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()
                    || summary.getEdocType() == EdocEnum.edocType.signReport.ordinal()
                    || (isFinish && summary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal())) {

                if (summary.getArchiveId() != null && summary.getTempleteId() != null) {  //设置了预先归档的目录
                    pigeonholeAffair("", affair, summary.getId(), summary.getArchiveId());
                    summary.setHasArchive(true);
                }
            }

            try {
                MetaUtil.refMeta(summary);
            } catch (Exception e) {
                LOGGER.error("更改枚举项为引用出现异常 error = ", e);
            }

            //产生流程超期提醒任务， 它和节点的提醒无关
            EdocHelper.createQuartzJobOfSummary(summary, workTimeManager);


            //调用协同立方
            EdocStartEvent event = new EdocStartEvent(this, affair);
            EventDispatcher.fireEvent(event);


            if (isSpecialBacked) {
                //指定回退发起者：流程重走，督办消息发送内容跟提交给回退人是不一样的
                String nodeNameStr = caseId != null ? caseId[2] : "";
                EdocMessageHelper.transEdocPendingSpecialBackedMsg(summary, nodeNameStr, isSpecialBackToSenderCancel);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return affairId;
    }

    @Override
    public Long transRunCase(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, SendType sendType,
                             Map options, String from, Long agentToId, boolean isNewSent, String process_xml,
                             String workflowNodePeoplesInput, String workflowNodeConditionInput, String workflowId)
            throws BusinessException {
        return runCase(summary, body, senderOpinion, sendType, options, from, agentToId, isNewSent, process_xml,
                workflowNodePeoplesInput, workflowNodeConditionInput, workflowId);
    }

    public Long runCase(EdocSummary summary,
                        EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType,
                        Map options, String from, Long agentToId, boolean isNewSent, String process_xml, String workflowNodePeoplesInput, String workflowNodeConditionInput) throws BusinessException {

        return this.runCase(summary, body, senderOpinion, sendType, options, from, agentToId, isNewSent, process_xml, workflowNodePeoplesInput, workflowNodeConditionInput, null);
    }


    public int runCase(String processId, EdocSummary summary, EdocBody body,
                       EdocOpinion senderOpinion) throws EdocException {

        return 0;
    }

    public void saveBody(EdocBody body) throws EdocException {


    }

    public Long saveBackBox(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion) throws EdocException {
        Long affairId = 0L;
        User user = AppContext.getCurrentUser();
        String processId = summary.getProcessId();
        CtpAffair senderAffair = null;
        if (summary.getId() != null) {
            // OA-44223 指定回退--直接提交我/流程重走，待发编辑然后再保存待发，待发页面之前节点的意见丢失 --start
            try {
                senderAffair = affairManager.getSenderAffair(summary.getId());
                if (senderAffair != null) {
                    affairId = senderAffair.getId();
                }
            } catch (BusinessException e) {
                LOGGER.error("获取发起人affair出错 " + e.getMessage());
            }
            /** 删除非指定回退的附言 **/
            if (summary != null) {
                edocOpinionDao.deleteSenderOpinion(summary.getId());
                //// OA-44223 指定回退--直接提交我/流程重走，待发编辑然后再保存待发，待发页面之前节点的意见丢失 --end
                edocSummaryDao.delete(summary.getId());
            }
        }
        summary.setIdIfNew();

        Timestamp now = new Timestamp(System.currentTimeMillis());
        summary.setCreateTime(now);
        summary.setProcessId(processId);
        if (summary.getStartTime() == null) {
            summary.setStartTime(now);
        }
        summary.setStartUserId(user.getId());

        body.setLastUpdate(now);
        body.setEdocSummary(summary);
        summary.getEdocBodies().add(body);

        // 附言内容为空，就不记录了
        if (StringUtils.isNotBlank(senderOpinion.getContent())) {
            senderOpinion.setEdocSummary(summary);
            senderOpinion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
            senderOpinion.setCreateTime(now);
            senderOpinion.setCreateUserId(user.getId());
            summary.getEdocOpinions().add(senderOpinion);
        }

        _setIsUnit(summary);

        edocSummaryManager.saveEdocSummary(summary);
        if (senderOpinion.getContent() != null && !"".equals(senderOpinion.getContent())) {
            saveOpinion(senderOpinion);
        }
        // 返回的affairId对于保存退回公文记录没有任何意义的
        return affairId;
    }

    private void updatePermissionRef4SaveDraft(Long templateId, Long caseId, Integer edocType, String processXml, Long orgAccountId) {

        int moduleType = EdocUtil.getApplicationCategoryEnumKeyByEdocType(edocType);

        String _caseId = "-1";
        String _templateId = "-1";

        if (Strings.isNotBlank(processXml)) {
            if (null != templateId && templateId != -1L && templateId != 0L) {
                _templateId = String.valueOf(templateId);
            } else {
                _caseId = String.valueOf(caseId);
            }
            try {
                EdocUtil.updatePermissinRef(moduleType, processXml, _caseId, _templateId, orgAccountId);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
        }
    }

    public Long saveDraft(EdocSummary summary, EdocBody body, EdocOpinion senderOpinion, String processXml) throws BusinessException {
        User user = AppContext.getCurrentUser();
        String processId = summary.getProcessId();
        Long templateId = summary.getTempleteId();
        try {

            boolean isFormatOrTextTemplate = false;//是否为格式模板, 是否是纯个人模版
            CtpTemplate template = null;

            if (null != templateId && templateId != -1L && templateId != 0L) {

                template = templateManager.getCtpTemplate(templateId);
                if (template != null) {

                    if ("text".equals(template.getType())) {//格式模版
                        isFormatOrTextTemplate = true;
                    } else if (!template.isSystem()) {//个人模版

                        if (template.getFormParentid() != null) {//父模版是否为系统模版判断

                            CtpTemplate parentTemp = templateManager.getCtpTemplate(template.getFormParentid());
                            if (!parentTemp.isSystem()) {
                                isFormatOrTextTemplate = true;
                            }
                        } else {
                            isFormatOrTextTemplate = true;
                        }
                    }
                }
            }

            Long flowPermAccountId = EdocHelper.getFlowPermAccountId(user.getLoginAccount(), summary, template);

            //更新节点权限
            updatePermissionRef4SaveDraft(templateId, summary.getCaseId(), summary.getEdocType(), processXml, flowPermAccountId);

            if (Strings.isBlank(processId) && template != null && !isFormatOrTextTemplate) {//非格式模板保存待发
                summary.setProcessId(null);
            } else if (Strings.isNotBlank(processId) && Strings.isNotBlank(processXml)) {//个人流程多次保存待发
                processId = wapi.saveProcessXmlDraf(processId, processXml, "edoc");
                summary.setProcessId(processId);
            } else if (Strings.isNotBlank(processXml)) {//个人流程第一次保存待发
                processId = wapi.saveProcessXmlDraf(null, processXml, "edoc");
                summary.setProcessId(processId);
            }
        } catch (BPMException e) {
            LOGGER.error("", e);
        }

        if (summary.getId() != null) {
            EdocSummary _summary = edocSummaryDao.get(summary.getId());
            if (_summary != null) {
                deleteOpinionBySummaryId(_summary.getId());
                edocSummaryDao.delete(_summary);
                affairManager.deleteByObjectId(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()), summary.getId());
            }
        }
        summary.setIdIfNew();

        Timestamp now = new Timestamp(System.currentTimeMillis());

        //保存colsummary、body
        summary.setCaseId(null);
        summary.setCreateTime(now);
        if (summary.getStartTime() == null) {
            summary.setStartTime(now);
        }
        //summary.setStartUserId(user.getId());
        body.setLastUpdate(now);
        body.setEdocSummary(summary);
        summary.getEdocBodies().add(body);

        //附言内容为空，就不记录了
        if (StringUtils.isNotBlank(senderOpinion.getContent())) {
            senderOpinion.setEdocSummary(summary);
            senderOpinion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
            senderOpinion.setCreateTime(now);
            senderOpinion.setCreateUserId(user.getId());
            summary.getEdocOpinions().add(senderOpinion);
        }
        CtpAffair affair = new CtpAffair();
        affair.setIdIfNew();
        affair.setApp(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).key());
        affair.setSubject(summary.getSubject());
        affair.setCreateDate(now);
        affair.setUpdateDate(now);
        affair.setMemberId(summary.getStartUserId());
        affair.setObjectId(summary.getId());
        affair.setSubObjectId(null);
        affair.setSenderId(summary.getStartUserId());
        affair.setState(StateEnum.col_waitSend.key());
        affair.setSubState(SubStateEnum.col_waitSend_draft.key());
        affair.setDelete(senderOpinion.isDeleteImmediate);
        affair.setProcessId(summary.getProcessId());
        affair.setCaseId(summary.getCaseId());
//	        affair.setTrack(senderOpinion.affairIsTrack);
        if (senderOpinion.affairIsTrack) {
            affair.setTrack(1);
        } else {
            affair.setTrack(0);
        }

        affair.setProcessId(summary.getProcessId());
        affair.setCaseId(summary.getCaseId());

        affair.setBodyType(summary.getFirstBody().getContentType());

        affair.setOrgAccountId(summary.getOrgAccountId());
        AffairUtil.setHasAttachments(affair, summary.isHasAttachments());

        if (Strings.isNotBlank(summary.getUrgentLevel())) {
            affair.setImportantLevel(Integer.parseInt(summary.getUrgentLevel()));
        }
        _setIsUnit(summary);
        edocSummaryManager.saveEdocSummary(summary, true);
        if (senderOpinion.getContent() != null && !"".equals(senderOpinion.getContent())) {
            saveOpinion(senderOpinion);
        }
        //首页栏目的扩展字段设置--公文文号、发文单位等--start
        Map<String, Object> extParam = EdocUtil.createExtParam(summary);
        affairSetExtProp(affair, extParam);
        //首页栏目的扩展字段设置--公文文号、发文单位等--end
        affairManager.save(affair);
        //OA-43108 调用公文模板保存待发，没有保存到最近调用模板分类中
        if (summary.getTempleteId() != null) {
            templateManager.updateTempleteHistory(user.getId(), summary.getTempleteId());
        }


        return affair.getId();
    }

    public EdocSummary saveForward(Long summaryId,
                                   boolean forwardOriginalNode, boolean foreardOriginalopinion,
                                   EdocOpinion senderOpinion) throws EdocException {
        return null;
    }

    public void saveOpinion(EdocOpinion opinion, boolean isSendMessage) throws BusinessException {
        saveOpinion(opinion, null, isSendMessage, "");
    }

    public void saveOpinion(EdocOpinion opinion, EdocOpinion oldOpinion, boolean isSendMessage) throws BusinessException {
        saveOpinion(opinion, oldOpinion, isSendMessage, "");
    }

    public void saveOpinion(EdocOpinion opinion, EdocOpinion oldOpinion, boolean isSendMessage, String lastOperateState) throws BusinessException {
        if (Strings.isNotBlank(lastOperateState) && Integer.parseInt(lastOperateState) == StateEnum.col_takeBack.key()) {//上一个操作是取回
            if (oldOpinion != null) {
                opinion.setId(oldOpinion.getId());
                updateOpinion(opinion);
            } else {
                saveOpinion(opinion);
            }
        } else {
            saveOpinion(opinion);
        }
        //saveOpinion(opinion);
        if (isSendMessage) {
            User user = AppContext.getCurrentUser();
            Long summaryId = opinion.getEdocSummary().getId();
            EdocSummary summary = opinion.getEdocSummary();
            ApplicationCategoryEnum appEnum = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
            Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(summary).key;
            List<CtpAffair> affairList = affairManager.getValidAffairs(appEnum, summaryId);
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            for (CtpAffair affair : affairList) {
                Long memberId = affair.getMemberId();
                Long senderId = affair.getSenderId();
                if (memberId.intValue() == senderId.intValue())
                    continue;
                MessageReceiver receiver = new MessageReceiver(affair.getId(), memberId, "message.link.edoc.done", affair.getId().toString());
                receiver.setReply(true);
                receivers.add(receiver);
            }
            try {
                MessageContent content = new MessageContent("edoc.addnote", summary.getSubject(), user.getName(), EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).getKey(), opinion.getContent()).setImportantLevel(summary.getImportantLevel());
                if (null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content,
                        appEnum,
                        user.getId(),
                        receivers,
                        systemMessageFilterParam);
            } catch (BusinessException e) {
                LOGGER.error("发起人增加附言消息提醒失败", e);
            }
        }
    }

    public void setFinishedFlag(long summaryId, int summaryState) throws EdocException {
        Map<String, Object> columns = new HashMap<String, Object>();
        columns.put("completeTime", new Timestamp(System.currentTimeMillis()));
        columns.put("state", summaryState);

        edocSummaryDao.update(summaryId, columns);
        //更新公文统计标志
        try {
            edocStatManager.updateFlowState(summaryId, CollaborationEnum.flowState.finish.ordinal());
        } catch (Exception e) {
            LOGGER.error("更新公文统计流程状态错误 summaryId=" + summaryId, e);
        }
    }

    public int stepBackSummary(long userId, EdocSummary summary, int from) throws BusinessException {

        Long summaryId = summary.getId();
        User user = AppContext.getCurrentUser();
        List<CtpAffair> affairs = affairManager.getValidAffairs(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()), summary.getId());
        //获取所有待办事项
        List<CtpAffair> colPendingAffairList = new ArrayList<CtpAffair>();
        for (CtpAffair pendingAffair : affairs) {
            if (pendingAffair.getState() == StateEnum.col_pending.key()) {
                colPendingAffairList.add(pendingAffair);
            }
        }
        summary.setState(CollaborationEnum.flowState.cancel.ordinal());
        /**
         * 撤销时删除归档路径
         * 原则：
         * 如果不是模板可以置空归档archiveId
         * 如果是模板（模板没有设置预归档路径）置空归档archiveId
         */
        Long archiveId = EdocHelper.getTempletePrePigholePath(summary.getTempleteId(), templateManager);
        if (summary.getTempleteId() == null || (summary.getTempleteId() != null && archiveId == null)) {
            summary.setArchiveId(null);
        }
        //将summary的状态改为待发
        summary.setCaseId(null);
        edocSummaryDao.update(summary);

        if (affairs != null) {
            for (int i = 0; i < affairs.size(); i++) {
                CtpAffair affair = (CtpAffair) affairs.get(i);
                if (affair.getState() == StateEnum.col_sent.key()) {
                    affair.setState(StateEnum.col_waitSend.key());
                    affair.setSubState(SubStateEnum.col_waitSend_stepBack.key());
                    //affair.addExtProperty("stepBackName",user.getName());
                    //affair.serialExtProperties();
                    Map<String, Object> extParam = new HashMap<String, Object>();
                    extParam.put("stepBackName", user.getName());
                    affairSetExtProp(affair, extParam);
                    //OA-49486客户bug回测：普通A8BUG_V3.50SP1_江苏徐州港务（集团）有限公司 _登记的收文，发送后在已发中删除，下一节点回退后，发起人的待发中没有，点击回退的系统消息，提示"收文已被删除"...
                    affair.setDelete(false);
                    //OA-49486客户bug回测：普通A8BUG_V3.50SP1_江苏徐州港务（集团）有限公司 _登记的收文，发送后在已发中删除，下一节点回退后，发起人的待发中没有，点击回退的系统消息，提示"收文已被删除"...
                    affair.setUpdateDate(new java.util.Date());
                    affairManager.updateAffair(affair);
                }
            }
            this.affairManager.updateAffairsState2Cancel(summaryId);
            this.affairManager.updateAffairSummaryState(summaryId, summary.getState());
        }
        //撤销后，删除公文统计数据
        try {
            edocStatManager.deleteEdocStat(summaryId);
        } catch (Exception e) {
            throw new EdocException(e);
        }
        LOGGER.info("summary is cancelled:" + summaryId);
        return 0;
    }

    private void affairSetExtProp(CtpAffair affair, Map<String, Object> extParam) {
        if (null != extParam) AffairUtil.setExtProperty(affair, extParam);
    }

    /**
     * 流程回退，指定回退，撤销电子分发的数据，需要同步修改登记的状态
     *
     * @param summary
     * @Author : xuqiangwei
     * @Date : 2015年1月13日下午5:18:43
     */
    private void makeRegisterToDraft(EdocSummary summary) {
        if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()) {//退回到发起节点才会更新register的状态
            //登记改为待登记状态
            EdocRegister register = edocRegisterManager.findRegisterByDistributeEdocId(summary.getId());
            if (register != null) {
                if (register.getState() == EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC) {
                    /**
                     * 当是纸质分发时，会自动插入register数据，其中type为100
                     * 当纸质分发的 第二个节点进行回退时，是回退到分发待发列表中，这里DistributeState值要设为0
                     */
                    register.setDistributeState(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());
                } else {
                    if (EdocHelper.isG6Version()) {
                        /**
                         * G6电子分发的，第二个节点进行回退时，是回退到待分发列表中，这里DistributeState值要设为1(不在分发待发列表中)
                         */
                        register.setDistributeState(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());
                    } else {
                        register.setDistributeState(EdocNavigationEnum.EdocDistributeState.Distribute_Back.ordinal());
                    }
                }
                edocRegisterManager.update(register);
            }
        }
    }

    public boolean stepBack(Long summaryId, Long affairId, EdocOpinion signOpinion, Map<String, Object> paramMap) throws BusinessException {
        User user = AppContext.getCurrentUser();

        EdocSummary summary = null;

        summary = (EdocSummary) paramMap.get("edocSummary");
        if (summary == null) {
            summary = edocSummaryDao.get(summaryId);
        }
        if (summary == null) {
            return false;
        }

        CtpAffair affair = affairManager.get(affairId);
        if (affair == null) {
            return false;
        }


        Long _workitemId = affair.getSubObjectId();
        Long caseId = summary.getCaseId();

        AffairData affairData = EdocHelper.getAffairData(summary, user);
        /**增加是否追溯流程的标记**/
        String _trackWorkflowType = (String) paramMap.get("trackWorkflowType");
        affairData.getBusinessData().put("isNeedTraceWorkflow", _trackWorkflowType);

        String result[] = null;
        Map<String, Object> businessData = new HashMap<String, Object>();
        try {
            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setAppName(ApplicationCategoryEnum.edoc.name());
            context.setCurrentWorkitemId(_workitemId);
            context.setProcessId(summary.getProcessId());
            context.setCaseId(caseId);
            context.setCurrentActivityId(String.valueOf(affair.getActivityId()));
            context.setAppObject(summary);
            context.setCurrentUserId(String.valueOf(user.getId()));
            //首页栏目的扩展字段设置--公文文号、发文单位等--start
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.WITHDRAW);
            context.setBusinessData(EdocEventDataContext.CTP_AFFAIR_EDOC_SEND_DOC_MARK, summary.getDocMark());
            context.setBusinessData(EdocEventDataContext.CTP_AFFAIR_EDOC_SEND_SEND_UNIT, summary.getSendUnit());
            context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
            //首页栏目的扩展字段设置--公文文号、发文单位等--end
            context.setBusinessData("isNeedTraceWorkflow", _trackWorkflowType);
            context.setBusinessData("currentAffairId", affairId);
            result = wapi.stepBack(context);//0代表是否退回成功，1代码回退给谁了，这里要记录日志用到

            businessData = context.getBusinessData();
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new EdocException(e);
        }
        int _result = Integer.parseInt(result[0]);

        //OA-45547yyzw9登录，从待发中编辑一个收文模板流程，发送时提示公文已分发，不允许重复分发，且确定后报js
        if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal() && _result == 1) {//退回到发起节点才会更新register的状态
            makeRegisterToDraft(summary);
        }
        String params = result[1];
        affair = affairManager.get(affairId);
        if (_result == 0 || _result == 1) {//0退回到普通节点 1退回到发起节点，导致撤销
            signOpinion.setEdocSummary(summary);
            signOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            signOpinion.setOpinionType(EdocOpinion.OpinionType.backOpinion.ordinal());
            affair.setTrack(signOpinion.affairIsTrack == false ? 0 : 1);
            if (paramMap.get("oldOpinion") != null) {
                updateOpinion(signOpinion);
            } else {
                saveOpinion(signOpinion);
                Set<EdocOpinion> _edocOpinions = summary.getEdocOpinions();
                if (!_edocOpinions.contains(signOpinion)) {
                    summary.getEdocOpinions().add(signOpinion);
                }
            }
        }
        if (_result == 0) {//0退回到普通节点
            affair.setState(StateEnum.col_stepBack.key());
            affair.setSubState(SubStateEnum.col_normal.key());
            affair.setUpdateDate(new java.util.Date());
            affairManager.updateAffair(affair);
        }
        Integer app = affair.getApp();
        //设置变量发回退消息。
        List<CtpAffair> allAvailabilityAffairList = null;
        if (Integer.valueOf(21).equals(app)) {
            allAvailabilityAffairList = affairManager.getValidAffairs(ApplicationCategoryEnum.edocSign, summary.getId());
        } else if (Integer.valueOf(20).equals(app)) {
            allAvailabilityAffairList = affairManager.getValidAffairs(ApplicationCategoryEnum.edocRec, summary.getId());
        } else if (Integer.valueOf(19).equals(app)) {
            allAvailabilityAffairList = affairManager.getValidAffairs(ApplicationCategoryEnum.edocSend, summary.getId());
        } else {
            allAvailabilityAffairList = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc, summary.getId());
        }


        // 更新当前待办人
        try {
            EdocHelper.updateCurrentNodesInfo(summary, true);
        } catch (Exception e1) {
            LOGGER.error("", e1);
        }


        Map<Long, Long> canceledAffairMap = new HashMap<Long, Long>();
        if (_result == 1) {//需要撤消流程

            Map<String, Object> traceMap = createRepealData2BeginNode(summary, affair, allAvailabilityAffairList, _trackWorkflowType);
            businessData.putAll(traceMap);

            affairManager.updateAffairSummaryState(summaryId, summary.getState());

            stepBackSummary(AppContext.getCurrentUser().getId(), summary, StateEnum.col_stepBack.key());

            try {
                //发送消息给督办人，更新督办状态，并删除督办日志、删除督办记录、删除催办次数
                edocSuperviseManager.updateStatusAndNoticeSupervisor(summary.getId(), SuperviseEnum.EntityType.edoc.ordinal(), ApplicationCategoryEnum.edoc, summary.getSubject(), user.getId(), user.getName(), SuperviseEnum.superviseState.waitSupervise.ordinal(), "edoc.stepback", "", summary.getForwardMember());
            } catch (Exception e) {
                LOGGER.error("删除待发事项相关督办信息异常：", e);
            }
            //删除文档中心已归档的公文。
            deleteDocByResources(summary, user);
            //删除部门归档
            List<Long> cancelAffairIds = new ArrayList<Long>();
            for (CtpAffair affair2 : allAvailabilityAffairList) {
                if (affair2.getSubObjectId() != null) {
                    cancelAffairIds.add(affair2.getId());
                }
            }
            if (!CollectionUtils.isEmpty(cancelAffairIds) && AppContext.hasPlugin("doc")) {
                docApi.deleteDocResources(user.getId(), cancelAffairIds);
            }
            //删除跟踪消息设置
            deleteColTrackMembersByObjectId(summaryId);
            for (CtpAffair affair0 : allAvailabilityAffairList) {
                canceledAffairMap.put(affair0.getMemberId(), affair0.getId());
            }
            if (summary.getEdocType() == 0 || summary.getEdocType() == 2) {//发文/签报撤销
                edocMarkManager.edocMarkCategoryRollBack(summary);
            }
            _result = 0;
        }

        if (_result == 0) {//退回到普通节点
            try {
                List<CtpAffair> trackingAffairLists = affairManager.getValidTrackAffairs(summaryId);
                CtpAffair sentAffair = null;
                for (CtpAffair _sentAffair : allAvailabilityAffairList) {
                    if (_sentAffair.getState() == StateEnum.col_sent.getKey() || _sentAffair.getState() == StateEnum.col_waitSend.getKey()) {
                        sentAffair = _sentAffair;
                        break;
                    }
                }
                if (sentAffair == null) {
                    sentAffair = this.affairManager.getSenderAffair(summary.getId());
                }
                trackingAffairLists.add(sentAffair);
                List<CtpTrackMember> trackMembers = getColTrackMembersByObjectIdAndTrackMemberId(summaryId, null);
                EdocMessageHelper.getTrackAffairExcludePart(trackingAffairLists, trackMembers, affair.getMemberId());
                boolean isTrace = "1".equals(_trackWorkflowType) ? true : false;
                /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
                //XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
                //boolean isOpen;
                //try {
                //isOpen = xkjtManager.isOpenBySender(affair.getId());
                //if(isOpen){
                EdocMessageHelper.stepBackMessage(affairManager, orgManager, userMessageManager, trackingAffairLists, affair,
                        summaryId, signOpinion, isTrace, canceledAffairMap, businessData);
                //}
                //} catch (BusinessException e) {
                // TODO Auto-generated catch block
                //LOGGER.error("获取是否发送消息失败",e);
                //}
                /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
                if (Strings.isNotBlank(params)) {
                    this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.stepBack, params);
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            //触发回退事件
            EdocStepBackEvent event = new EdocStepBackEvent(this, affair);
            EventDispatcher.fireEvent(event);

            //      回退记录  zhou start
            Map<String, Object> map6 = new HashMap<>();
            map6.put("activityId", affair.getActivityId());
            map6.put("objectId", affair.getObjectId());
            String hql = "from CtpAffair where state=6 and nodePolicy='转送' and  activityId=:activityId and objectId=:objectId ";

            List<CtpAffair> list6 = affairManager.findState6(hql,map6);
            XkjtTemp temp = null;
            if (list6.size() > 0) {
                for (CtpAffair a : list6) {
                    temp = new XkjtTemp();
                    temp.setId(Long.toString(a.getId()));
                    temp.setSummaryId(Long.toString(a.getObjectId()));
                    tempManager.saveXkjtTemp(temp);
                }
            }
            //      回退记录  zhou end
            return true;
        } else {
            return false;
        }

    }

    private Map<String, Object> createRepealData2BeginNode(EdocSummary summary, CtpAffair affair, List<CtpAffair> allAvailabilityAffairList, String traceFlag) throws BusinessException {
        CtpTemplate t = null;
        if (null != summary.getTempleteId()) {
            t = templateManager.getCtpTemplate(summary.getTempleteId());
        }
        List<CtpAffair> traceAffairs = new ArrayList<CtpAffair>();
        for (CtpAffair aff : allAvailabilityAffairList) {
            if (Integer.valueOf(StateEnum.col_done.key()).equals(aff.getState())
                    || (Integer.valueOf(StateEnum.col_pending.key()).equals(aff.getState()) && Integer.valueOf(SubStateEnum.col_pending_ZCDB.key()).equals(aff.getSubState()))
                    || (aff.getId().equals(affair.getId()) &&
                    !(Integer.valueOf(StateEnum.col_sent.key()).equals(aff.getState()) || Integer.valueOf(StateEnum.col_waitSend.key()).equals(aff.getState())))) {
                traceAffairs.add(aff);
            }
        }
        return edocTraceWorkflowManager.createStepBackTrackDataToBegin(summary, affair, traceAffairs, t, WorkflowTraceEnums.workflowTrackType.step_back_repeal, traceFlag);
    }

    /**
     * 删除归档的公文
     *
     * @param summary
     * @throws EdocException
     */
    private void deleteDocByResources(EdocSummary summary, User user) throws EdocException {
        try {
            List<Long> ids = new ArrayList<Long>();
            ids.add(summary.getId());
            if (AppContext.hasPlugin("doc")) {
                docApi.deleteDocResources(user.getId(), ids);
            }
            summary.setHasArchive(false);//BUG_OA-66429_普通_V5_V5.0sp2_建信人寿保险有限公司_发文回退后再次提交，无法归档_20140730001904_20140806
            if (summary.getTempleteId() == null) {
                summary.setArchiveId(null);
            }
            edocSummaryDao.saveOrUpdate(summary);
        } catch (Exception e) {
            LOGGER.error("撤销公文流程，删除归档文档,", e);
        }
    }

    public boolean transStepStop(Long affairId, Map<String, Object> params) throws BusinessException {
        EdocOpinion signOpinion = (EdocOpinion) params.get("edocOpinion");
        Long summaryId = (Long) params.get("summaryId");
        List<Long[]> pushMsgMemberList = (List<Long[]>) params.get("pushMsgMemberList");

        EdocSummary summary = edocSummaryDao.get(summaryId);
        BPMActivity bPMActivity = null;
        if (summary == null) {
            return false;
        }

        CtpAffair curerntAffair = null;
        User user = AppContext.getCurrentUser();
        if (affairId != null) {
            curerntAffair = affairManager.get(affairId);
            if (curerntAffair == null) {
                return false;
            }
            if (signOpinion != null && signOpinion.isDeleteImmediate) {
                curerntAffair.setDelete(true);
            }
            //OA-16680 终止公文，在已办中还有自己代理自己的字样。应该去掉。
            if (!user.isAdmin() && curerntAffair.getMemberId().longValue() != user.getId().longValue()) {
                //由代理人终止需要写入处理人ID
                curerntAffair.setTransactorId(user.getId());
            }

            curerntAffair.setState(StateEnum.col_done.key());
            curerntAffair.setSubState(SubStateEnum.col_done_stepStop.key());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            /*curerntAffair.setState(StateEnum.col_done.key());
            curerntAffair.setSubState(SubStateEnum.col_done_stepStop.key());*/
            curerntAffair.setCompleteTime(now);
            curerntAffair.setUpdateDate(now);

            //更新操作时间
            if (curerntAffair.getSignleViewPeriod() == null && curerntAffair.getFirstViewDate() != null) {
                java.util.Date nowTime = new java.util.Date();
                long viewTime = workTimeManager.getDealWithTimeValue(curerntAffair.getFirstViewDate(), nowTime, curerntAffair.getOrgAccountId());
                curerntAffair.setSignleViewPeriod(viewTime);
            }

            //第一次操作时间
            if (curerntAffair.getFirstResponsePeriod() == null) {
                java.util.Date nowTime = new java.util.Date();
                long responseTime = workTimeManager.getDealWithTimeValue(curerntAffair.getReceiveTime(), nowTime, curerntAffair.getOrgAccountId());
                curerntAffair.setFirstResponsePeriod(responseTime);
            }

            affairManager.updateAffair(curerntAffair);
        } else if (affairId == null && (user.isAdministrator() || user.isGroupAdmin())) {
            // 管理员终止，临时这么处理
            List<CtpAffair> curerntAffairs = affairManager.getAffairs(summary.getId());
            if (curerntAffairs != null && !curerntAffairs.isEmpty()) {
                try {
                    curerntAffair = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(curerntAffairs.get(0));
                    curerntAffair.setMemberId(user.getId());
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }

        if (curerntAffair == null) {
            return false;
        }


        /*
        try {
            currentActivity = Long.parseLong(ColHelper.getActvityIdByAffair(curerntAffair));
		}
		catch (Exception e) {
			log.error("", e);
		}

        if (signOpinion.isDeleteImmediate) {
        	curerntAffair.setIsDelete(true);
        }
        if(curerntAffair.getMemberId() != user.getId()){
            //由代理人终止需要写入处理人ID
            curerntAffair.setTransactorId(user.getId());
        }*/

        try {
            bPMActivity = EdocHelper.getBPMActivityByAffair(curerntAffair);
        } catch (Exception e) {
            LOGGER.error("记录流程日志获取当前节点时候出现问题", e);
        }


        summary.setState(CollaborationEnum.flowState.terminate.ordinal());    // 值参考Contant.java 枚举值
        if (signOpinion != null) {
            signOpinion.setEdocSummary(summary);
            signOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            signOpinion.setOpinionType(EdocOpinion.OpinionType.stopOpinion.ordinal());
            if (null == user.getId()) {
                signOpinion.setCreateUserId(user.getId());
            }

            saveOpinion(signOpinion);
        }

        //affairManager.updateAffair(curerntAffair);

        //如果公文已经归档，则终止后不在已办中显示(原3.50的逻辑设计)
        if (summary.getHasArchive()) {
            //因为目前终止操作所有的待办都进入已办，所以将该公文在affair中所有archiveId为null的更新为-1
            affairManager.updateAllPigeonholeInfo(summary.getId(), -1L);
        }
        Long _workitemId = curerntAffair.getSubObjectId();
        try {
            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setCurrentWorkitemId(_workitemId);
            context.setAppObject(summary);
            context.setBusinessData(EdocWorkflowEventListener.CTP_AFFAIR, curerntAffair);
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.STETSTOP);
            context.setBusinessData(EdocWorkflowEventListener.PUSHMSG_MEMBERLIST, pushMsgMemberList);
            if (null == signOpinion) {
                signOpinion = new EdocOpinion();
                signOpinion.setIdIfNew();
            }
            if (signOpinion != null) {
                MessageCommentParam mc = new MessageCommentParam(signOpinion.getId(), signOpinion.getIsHidden(), signOpinion.getContent(), signOpinion.getAttribute(), signOpinion.isHasAtt());
                context.setBusinessData(EdocWorkflowEventListener.COMMENT_MESSAGE, mc);
            }
            wapi.stopCase(context);
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        try {
            if (user.isAdministrator() || user.isGroupAdmin() || user.isSystemAdmin()) {
                this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), -1L, ProcessLogAction.stepStop, "");
            } else {
                if (bPMActivity != null) {
                    this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.stepStop, "");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("", e);
        }

        //协同立方接口调用
        Timestamp now = new Timestamp(System.currentTimeMillis());
        EdocStopEvent event = new EdocStopEvent(this, curerntAffair.getMemberId(), now, curerntAffair);
        EventDispatcher.fireEvent(event);

        return true;
    }

    public boolean takeBack(Long affairId) throws EdocException, BusinessException {
        return this.transTakeBack(affairId);
    }

    public boolean transTakeBack(Long affairId) throws BusinessException {


        User user = AppContext.getCurrentUser();
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null) {
            return false;
        }


        Long summaryId = affair.getObjectId();
        EdocSummary summary = edocSummaryDao.get(summaryId);
        if (summary == null) {
            return false;
        }

        List<CtpAffair> pendingAffairList = affairManager.getAffairs(summaryId);
        Long _workitemId = affair.getSubObjectId();

        int result = -1;
        Map<Long, Long> canceledMemberIdToAffairId = new HashMap<Long, Long>();
        try {
            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setAppName("edoc");
            context.setAppObject(summary);
            context.setCurrentWorkitemId(_workitemId);
            context.setCaseId(summary.getCaseId());
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.TAKE_BACK);
            context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, EdocHelper.getAffairData(summary, user));
            result = wapi.takeBack(context);

            canceledMemberIdToAffairId = (Map<Long, Long>) context.getBusinessData(EdocWorkflowEventListener.WORKITEMCANCELED_MEMBERIDTOAFFAIRID);

        } catch (BPMException e) {
            LOGGER.error("公文取回产生异常", e);
        }
        List<String> baseActions = null;
        if (result == 0) {
            EnumNameEnum edocTypeEnum = EdocUtil.getEdocMetadataNameEnumByApp(affair.getApp());
            String nodePermissionPolicy = getPolicyByAffair(affair, summary.getProcessId());
            try {
                baseActions = permissionManager.getBasicActionList(edocTypeEnum.name(), nodePermissionPolicy, summary.getOrgAccountId());
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        if (result == 0) {

            //设置当前处理人信息R
            String cninfo = summary.getCurrentNodesInfo();
            String cniStr = affair.getMemberId() + "";
            if (Strings.isNotBlank(cninfo)) {//原来有当前处理人信息，追加
                summary.setCurrentNodesInfo(cninfo + ";" + cniStr);
            } else {
                summary.setCurrentNodesInfo(cniStr);
            }
            try {
                this.update(summary);
            } catch (Exception e1) {
                LOGGER.error("更新公文对象报错!", e1);
            }
            // 删除封发时已发文
            if ((!Strings.isBlank(affair.getNodePolicy()) && "fengfa"
                    .equals(affair.getNodePolicy()))
                    || ((baseActions != null) && (baseActions
                    .contains("EdocExchangeType")))) {
                Long sendEdocId = null;
                for (int i = 0; i < pendingAffairList.size(); i++) {
                    CtpAffair tempPendAffair = pendingAffairList.get(i);
                    if (tempPendAffair.getApp() == ApplicationCategoryEnum.exSend
                            .key() && !tempPendAffair.isDelete()) {
                        sendEdocId = tempPendAffair.getSubObjectId();
                        break;
                    }
                }
                try {
                    if (sendEdocId != null) {
                        sendEdocManager.deleteByPhysical(sendEdocId);
                    }
                } catch (Exception e) {
                    LOGGER.error("取回公文时，删除封发后的已发送公文错误。", e);
                    throw new EdocException(e);
                }

                //更新督办到未办结状态
                superviseManager.updateStatusBySummaryIdAndType(SuperviseEnum.superviseState.supervising, summaryId, SuperviseEnum.EntityType.edoc);
            }
        }
        if (result == 0) {

            affair.setState(StateEnum.col_pending.key());
            affair.setSubState(SubStateEnum.col_pending_takeBack.key());
            affair.setCompleteTime(null);
        	/*affair.setFirstResponsePeriod(null);
            affair.setFirstViewDate(null);
            affair.setFirstViewPeriod(null);
            affair.setSignleViewPeriod(null);
        	*/
            Map<String, Object> extMap = AffairUtil.getExtProperty(affair);
            if (extMap == null) extMap = new HashMap<String, Object>();
            extMap.put(AffairExtPropEnums.edoc_lastOperateState.name(), String.valueOf(StateEnum.col_takeBack.key()));
            AffairUtil.setExtProperty(affair, extMap);
            affairManager.updateAffair(affair);
            // 更新封发后取回时，本affair的已发事件的isdelete为true
            if ((!Strings.isBlank(affair.getNodePolicy()) && "fengfa"
                    .equals(affair.getNodePolicy()))
                    || ((baseActions != null) && (baseActions
                    .contains("EdocExchangeType")))) {

                // 批量更新
                Map<String, Object> columns = new Hashtable<String, Object>();
                columns.put("delete", true);
                columns.put("state", StateEnum.col_takeBack.key());
                affairManager.update(columns, new Object[][]{
                        {"app", ApplicationCategoryEnum.exSend.getKey()},
                        {"state", StateEnum.col_pending.key()},
                        {"objectId", summaryId}});
                // 更新summary的state为0，使其不显示流程结束图标
				/*Map<String, Object> summaryColumns = new HashMap<String, Object>();
				summaryColumns.put("state", 0);
				summaryColumns.put("completeTime",null);
				update(summaryId, summaryColumns);*/
                this.edocSummaryDao.bulkUpdate("update EdocSummary set completeTime=null,state=0 where id=?", null, summaryId);
                /*
                 * List<Affair> sentAndPendingAffairList = affairManager
                 * .getSentAndPendingAffairList(summaryId); for (Affair
                 * sentAffair : sentAndPendingAffairList) { if
                 * (sentAffair.getState() == StateEnum.col_pending.key() &&
                 * sentAffair.getApp() == ApplicationCategoryEnum.exSend
                 * .getKey()) { // 待办事项，且是公文交换中的待发送
                 * sentAffair.setIsDelete(true);
                 * sentAffair.setState(StateEnum.col_takeBack.key());//
                 * 设置为已取回，防止发送消息 affairManager.updateAffair(sentAffair); } }
                 */
                // 同时，不给自己发送一般消息，只发送跟踪消息
                int removeIndex = 0;
                boolean isRemove = false;
                for (int i = 0; i < pendingAffairList.size(); i++) {
                    CtpAffair tempPendAffair = pendingAffairList.get(i);
                    if ((tempPendAffair.getMemberId().longValue() == user
                            .getId())
                            && (tempPendAffair.getApp() == ApplicationCategoryEnum.exSend
                            .key())) {
                        removeIndex = i;
                        isRemove = true;
                        break;
                    }
                }
                if (isRemove) {
                    pendingAffairList.remove(removeIndex);
                }
            }
            //封发取回成功的时候要把edoc_mark_history表中的文号给删除了
            if (summary.getCompleteTime() != null) {
                this.edocMarkHistoryManager.deleteMarkIdBySummaryId(summary.getId());
            }

            //更新意见类型，设置为暂存待办状态
            EdocOpinion eo = this.findBySummaryIdAndAffairId(summaryId, affairId);
//			zhou
            if (null != eo) {
                eo.setOpinionType(EdocOpinion.OpinionType.draftOpinion.ordinal());
                this.updateOpinion(eo);
            }
            htmSignetManager.deleteBySummaryIdAffairIdAndType(summary.getId(), affairId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
        }
        if (result == 0) {
            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
            // XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
            //boolean isOpen;
            //try {
            //isOpen = xkjtManager.isOpenBySender(affair.getId());
            //if(isOpen){
            EdocMessageHelper.takeBackMessage(affairManager, orgManager, userMessageManager, pendingAffairList, affair, summaryId, canceledMemberIdToAffairId);
            //}
            //} catch (BusinessException e) {
            // TODO Auto-generated catch block
            //	LOGGER.error("获取是否发送消息失败",e);
            //}
            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/

            try {
                String paramer = "";
                BPMActivity bPMActivity = EdocHelper.getBPMActivityByAffair(affair);//当前节点
                this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.takeBack, paramer);

            } catch (Exception e) {
                LOGGER.error("", e);
            }

            if (AppContext.hasPlugin("index")) {
                indexManager.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
            }
            return true;
        } else {
            return false;
        }
    }

    public void update(Long summaryId, Map<String, Object> columns) {
        edocSummaryDao.update(summaryId, columns);
    }

    public void update(EdocSummary summary) throws Exception {
        this.update(summary, false);
    }

    public void update(EdocSummary summary, boolean isSaveExtend) throws Exception {
        edocSummaryManager.updateEdocSummary(summary, isSaveExtend);
        //更新公文统计数据
        edocStatManager.updateElement(summary);
        try {
            MetaUtil.refMeta(summary);
        } catch (Exception e) {
            LOGGER.error("更改枚举项为引用出现异常 error = " + e);
        }
    }

    public void zcdb(EdocSummary summary, CtpAffair affair, EdocOpinion opinion, String processId, String userId,
                     String process_xml, String readyObjectJSON, String processChangeMessage, boolean isM1, List<Long[]> pushMsgMemberList) throws BusinessException {
        /*//Map<Long, List<MessageData>> messageDataMap = ColHelper.messageDataMap;*/
        //持久化修改后的流程
        User user = AppContext.getCurrentUser();
        try {
            EdocInfo info = new EdocInfo();
            info.setSummary(summary);
            AffairData affairData = EdocHelper.getAffairData(summary, user);
            affairData.setTemplateId(info.getSummary().getTempleteId());//如协同colsummary
            //暂存待办为11，工作流回调监听接口中会使用到
            long workItemId = affair.getSubObjectId();
            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, 11);
            context.setAppName("edoc");
            context.setCurrentWorkitemId(workItemId);
            context.setCurrentUserId(userId);
            context.setAppObject(summary);
            context.setChangeMessageJSON(processChangeMessage);
            context.setMobile(isM1);
            if (Strings.isNotBlank(process_xml)) {
                context.setProcessXml(process_xml);
            }
            if (Strings.isNotBlank(readyObjectJSON)) {
                context.setReadyObjectJson(readyObjectJSON);
            }
            context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
            wapi.temporaryPending(context);

            if (Strings.isNotBlank(process_xml)) {
                if (summary.getTempleteId() == null) {
                    EdocUtil.updatePermissinRef(affairData.getModuleType(), process_xml, String.valueOf(summary.getCaseId()), "-1", summary.getOrgAccountId());
                } else {
                    CtpTemplate template = templateManager.getCtpTemplate(affairData.getTemplateId());
                    if (template != null) {
                        EdocUtil.updatePermissinRef(affairData.getModuleType(), process_xml, "-1", String.valueOf(affairData.getTemplateId()), template.getOrgAccountId());
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("zcdb affair failed", e);
        }

        opinion.setIdIfNew();

        opinion.setEdocSummary(summary);
        opinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
        opinion.setOpinionType(EdocOpinion.OpinionType.provisionalOpinoin.ordinal());

        if (affair.getMemberId().longValue() != user.getId()) {
            List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(user.getId());
            if (agentModelList != null && !agentModelList.isEmpty()) {
                opinion.setCreateUserId(affair.getMemberId());
                opinion.setProxyName(user.getName());
            } else {
                opinion.setCreateUserId(user.getId());
            }
        } else {
            opinion.setCreateUserId(user.getId());
        }

        try {
            saveOpinion(opinion);
            String params = "";
    		/*BPMActivity bPMActivity = EdocHelper.getBPMActivityByAffair(affair);//当前节点
    		params = EdocHelper.checkNextNodeMembers(bPMActivity) ;*/
            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.zcdb, params);
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        Integer sub_state = affair.getSubState();
        if (sub_state == null
                || sub_state.intValue() == SubStateEnum.col_normal.key()
                || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()
                || sub_state.intValue() == SubStateEnum.col_pending_read.key()
                || Integer.valueOf(SubStateEnum.col_pending_takeBack.getKey()).equals(sub_state.intValue())) {

            affair.setSubState(SubStateEnum.col_pending_ZCDB.key());
        }
        /**
         * OA-44628 公文直接暂存待办时不记录处理时间，但是取回后暂存待办就记录了处理时间，且没有处理时长
         * 暂存待办计算处理时长，当前节点没有处理完成，就会取更新时间进行计算，如果暂存待办把当前节点的completeetime赋值后，就会引发自动跳过的问题，
         * 自动跳过时会认为当前节点已处理完成，所以不跳过。
         * 屏蔽affair.setCompleteTime(new Timestamp(System.currentTimeMillis()));
         */
        //affair.setCompleteTime(new Timestamp(System.currentTimeMillis()));
        affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));

        //更新操作时间
        if (affair.getSignleViewPeriod() == null && affair.getFirstViewDate() != null) {
            java.util.Date nowTime = new java.util.Date();
            long viewTime = workTimeManager.getDealWithTimeValue(affair.getFirstViewDate(), nowTime, affair.getOrgAccountId());
            affair.setSignleViewPeriod(viewTime);
        }

        //第一次操作时间
        if (affair.getFirstResponsePeriod() == null) {
            java.util.Date nowTime = new java.util.Date();
            long responseTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());
            affair.setFirstResponsePeriod(responseTime);
        }

        this.affairManager.updateAffair(affair);
        EdocMessageHelper.zcdbMessage(userMessageManager, orgManager, affairManager, affair, opinion.isHasAtt(), pushMsgMemberList);
    }

    public void zcdb(CtpAffair affair, EdocOpinion opinion, String title, String supervisorMemberId, String supervisorNames
            , String superviseDate, EdocSummary summary, String processId, String userId,
                     String process_xml, String readyObjectJSON, String processChangeMessage, List<Long[]> pushMsgMemberList) throws BusinessException {

        this.zcdb(summary, affair, opinion, processId, userId, process_xml, readyObjectJSON, processChangeMessage, false, pushMsgMemberList);
        //督办开始--
        edocSuperviseManager.supervise(title, supervisorMemberId, supervisorNames, superviseDate, summary);
        //督办结束--

    }

    public boolean updateHtmlBody(long bodyId, String content) throws EdocException {
        EdocBody edocBody = edocBodyDao.get(bodyId);
        if (edocBody != null) {
            edocBody.setContent(content);
            edocBody.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            edocBodyDao.update(edocBody);
            //EdocMessageHelper.saveBodyMessage(affairManager, userMessageManager, orgManager, edocBody.getEdocSummary());
            EdocSummary summary = edocBody.getEdocSummary();
            if (null != summary) {
                //operationlogManager.insertOplog(summary.getId(), bodyId,ApplicationCategoryEnum.edoc,
                //		EactionType.LOG_EDOC_UPDATE_CONTENT, EactionType.LOG_EDOC_UPDATE_CONTENT_DESCRIPTION, user.getName(), summary.getSubject());
            }

            return true;
        }
        return false;
    }

    public void deleteEdocOpinion(Long opinionId) throws EdocException {
        EdocOpinion opinion = edocOpinionDao.get(opinionId);
        deleteOpinion(opinion);
    }

    /**
     * 待发列表，点击立即发送
     *
     * @param summary
     * @param map：    调用模版时候，角色匹配选择人员数据
     * @throws BusinessException
     */
    public void sendImmediate(Long affairId, EdocSummary summary) throws BusinessException {
        sendImmediate(affairId, summary, false);
    }

    private void delOptionsBySubOpId(Long summaryId) {
        List<EdocOpinion> opinions = edocOpinionDao.findEdocOpinionBySummaryId(summaryId, true);
        List<Long> subOpinionIds = new ArrayList<Long>();
        for (EdocOpinion op : opinions) {
            Long subOpitionId = op.getId();//下级单位上报意见id
            //获得该意见对应的 在上级文单中显示的意见，并删除
            if (subOpitionId != null) {
                subOpinionIds.add(subOpitionId);
            }

        }
        if (Strings.isNotEmpty(subOpinionIds)) {
            delOptionBySubOpId(subOpinionIds);
        }
    }


    /**
     * 待发列表，点击立即发送
     *
     * @param summary
     * @param 快速发文标识
     * @throws BusinessException
     */
    public void sendImmediate(Long affairId, EdocSummary summary, boolean isQuickSend) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long summaryId = summary.getId();
        String processId = summary.getProcessId();
        //edocManager.clearSummaryOCA(summaryId, false);
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        summary.setCreateTime(now);
        CtpAffair updAffair = affairManager.get(affairId);
        String caseId[] = null;
        //Map<String, Object> summ = new HashMap<String, Object>();

        SuperviseMessageParam sParam = new SuperviseMessageParam();
        sParam.setForwardMember(null);
        sParam.setImportantLevel(Integer.valueOf(1));
        sParam.setMemberId(user.getId());
        sParam.setSaveDraft(false);
        sParam.setSendMessage(true);
        sParam.setSubject(updAffair.getSubject());

        this.superviseManager.updateStatus(summaryId, SuperviseEnum.EntityType.edoc,
                SuperviseEnum.superviseState.supervising, sParam);

        int subState = 0;
        // 指定回退的情况
        boolean isColPendingSpecialBacked = false;
        boolean isSpecialBacked = false;
        if (!isQuickSend) {
            try {
                AffairData affairData = EdocHelper.getAffairData(summary, user);
                affairData.setTemplateId(summary.getTempleteId());

                WorkflowBpmContext context = new WorkflowBpmContext();
                context.setAppName("edoc");
                context.setDebugMode(false);
                context.setStartUserId(String.valueOf(user.getId()));
                context.setStartUserName(user.getName());
                context.setStartAccountId(String.valueOf(user.getAccountId()));
                context.setProcessId(summary.getProcessId());
                //首页栏目的扩展字段设置--公文文号、发文单位等--start
                context.setBusinessData("edoc_send_doc_mark", summary.getDocMark());
                context.setBusinessData("edoc_send_send_unit", summary.getSendUnit());
                //首页栏目的扩展字段设置--公文文号、发文单位等--end
                long _caseId = 0L;
                if (summary.getCaseId() != null) {
                    _caseId = summary.getCaseId();
                }
                context.setCaseId(_caseId);
                context.setCurrentActivityId("start");
                V3xOrgAccount account;
                try {
                    account = orgManager.getAccountById(user.getAccountId());
                    context.setStartAccountName(account.getName());
                } catch (BusinessException e1) {
                    LOGGER.error("", e1);
                }

                context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
                //              context.setBusinessData("bizObject", wfContextBizObject);
                context.setAppObject(summary);
                context.setCurrentUserId(user.getId().toString());
                context.setCurrentUserName(user.getName());
                caseId = wapi.transRunCase(context);
                summary.setCaseId(Long.valueOf(caseId[0]));
                summary.setProcessId(caseId[1]);

                //当前待办人

                edocStatManager.createState(summary, user);

                delOptionsBySubOpId(summaryId);

                if (updAffair.getSubState() != SubStateEnum.col_pending_specialBacked.getKey() && updAffair.getSubState() != SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                    deleteDealOpinion(summaryId);
                }
                /**
                 * 判断并删除对向上单位收文意见
                 */
                List<EdocOpinion> els = edocExchangeTurnRecManager.getDelStepBackSupOptions(summary.getId());
                if (els != null && els.size() > 0) {
                    for (EdocOpinion op : els) {
                        deleteOpinion(op);
                    }
                }
                /**
                 * 上级单位收文撤销后，再发起时，将收文的转收文信息删除
                 */
                edocExchangeTurnRecManager.delTurnRecByEdocId(summary.getId());


                subState = updAffair.getSubState();
                // 指定回退-提交回退者的情况下不删除，其他的情况下都删除
                //OA-35626 weblogic环境：a1新建公文，在已发进行催办流程激活的节点，然后已发撤销或待办节点撤销了公文，这时a1重新编辑又发起了，从a1的已发查看公文的催办日志，保留了以前的日志
                if (subState != SubStateEnum.col_pending_specialBacked.getKey() || subState != SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                    //删除催办日志
                    this.superviseManager.deleteLogs(summary.getId());
                }
                //删除处理意见，手写批注
                htmSignetManager.deleteBySummaryIdAndType(summaryId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());

                if (subState == SubStateEnum.col_pending_specialBacked.getKey()
                        || subState == SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {
                    isSpecialBacked = true;
                    if (subState == SubStateEnum.col_pending_specialBacked.getKey()) {
                        isColPendingSpecialBacked = true;
                    }
                }

                //删除或者更新原来的流程
                delOrUpdateProcessLog(summary, subState, processId);

                if (!isSpecialBacked) {
                    updAffair.setCreateDate(now);
                    summary.setCreateTime(now);
                    if (summary.getStartTime() == null) {
                        summary.setStartTime(now);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
                throw new EdocException(e);
            }
        } else { // 快速发文没有流程
            //summ.put("caseId", 0L);
            //summ.put("processId", null);
            //summ.put("createTime", now);
            // summ.put("startTime", now);
            summary.setCaseId(null);
            summary.setProcessId(null);
            summary.setCreateTime(now);
            if (summary.getStartTime() == null) {
                summary.setStartTime(now);
            }
        }

        ///待发直接发送更新公文的部分信息
        //edocSummaryDao.update(summary.getId(),summ);

//		Map<String, Object> aff = new HashMap<String, Object>();

        //将affair由待发状态改为已发状态
        updAffair.setApp(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).ordinal());
        updAffair.setSubObjectId(null);
        updAffair.setState(StateEnum.col_sent.key());
        updAffair.setSubState(SubStateEnum.col_normal.key());
        updAffair.setReceiveTime(now);
        updAffair.setUpdateDate(now);
        updAffair.setOrgAccountId(summary.getOrgAccountId());
        if (isQuickSend) {
            updAffair.setTrack(0);
        }

        affairManager.updateAffair(updAffair);


        try {
            String params = caseId != null ? caseId[2] : "";
            if (summary.getEdocType() == 1) { //收文分发
                if (isSpecialBacked) {
                    this.processLogManager.insertLog(user, Long.valueOf(caseId[1]), -1l, ProcessLogAction.commit, params);
                } else {
                    if (EdocHelper.isG6Version()) {
                        this.processLogManager.insertLog(user, Long.valueOf(caseId[1]), -1l, ProcessLogAction.distributer, params);
                    } else {
                        this.processLogManager.insertLog(user, Long.valueOf(caseId[1]), -1l, ProcessLogAction.register, params);
                    }
                }
                if (EdocHelper.isG6Version()) {
                    this.appLogManager.insertLog(user, AppLogAction.Edoc_receive_distribute, user.getName(), summary.getSubject());
                } else {
                    this.appLogManager.insertLog(user, AppLogAction.Edoc_RegEdoc, user.getName(), summary.getSubject());
                }
            } else { //其他，包括发文拟文
                if (isSpecialBacked) {
                    this.processLogManager.insertLog(user, Long.valueOf(caseId[1]), -1l, ProcessLogAction.commit, params);
                } else {
                    this.processLogManager.insertLog(user, Long.valueOf(caseId[1]), -1l, ProcessLogAction.drafting, params);
                }
                this.appLogManager.insertLog(user, AppLogAction.Edoc_Send, user.getName(), summary.getSubject());
            }
            if (isSpecialBacked) {
                // 回退到发起者选择提交回退者，重新发起时需要给待办等发送消息
                if (isColPendingSpecialBacked) {
                    String nodeNameStr = caseId[2];
                    EdocMessageHelper.transEdocPendingSpecialBackedMsg(summary, nodeNameStr);
                } else {
                    String nodeNameStr = caseId[2];
                    EdocMessageHelper.transEdocPendingSpecialBackedMsg(summary, nodeNameStr, true);
                }
            }

        } catch (Exception e) {
            LOGGER.error("流程日志写入失败：", e);
        }

        EdocHelper.createQuartzJobOfSummary(summary, workTimeManager);
    }

    public void setEdocSuperviseManager(EdocSuperviseManager edocSuperviseManager) {
        this.edocSuperviseManager = edocSuperviseManager;
    }

    public EdocOpinion findBySummaryIdAndAffairId(long summaryId, long affairId) {
        return edocOpinionDao.findBySummaryIdAndAffairId(summaryId, affairId);
    }

    public List<ResultModel> iSearch(ConditionModel cModel) {
        List<ResultModel> rsms = new ArrayList<ResultModel>();
        String exp0 = null;
        String paramName = null;
        String paramValue = null;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> paramNameList = new ArrayList<String>();
        User user = AppContext.getCurrentUser();
        long user_id = user.getId();

        String hql = "select affair,summary from CtpAffair as affair,EdocSummary as summary"
                + " where";
        hql += " affair.memberId=" + user_id;


        hql += " and affair.objectId=summary.id"
                + " and affair.delete!=true";
        hql += " and affair.app in (" + ApplicationCategoryEnum.edocSend.getKey() + "," + ApplicationCategoryEnum.edocRec.getKey() + "," + ApplicationCategoryEnum.edocSign.getKey() + ")";

        if (cModel.getTitle() != null) {
            paramName = "subject";
            exp0 = " and summary.subject like :" + paramName + " ";
            paramValue = "%" + SQLWildcardUtil.escape(cModel.getTitle()) + "%";
            paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        }
        if (cModel.getKeywords() != null) {
            paramName = "keywords";
            exp0 = " and summary.keywords like :" + paramName + " ";
            paramValue = "%" + SQLWildcardUtil.escape(cModel.getKeywords()) + "%";
            paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql = hql + exp0;
        }
        if (cModel.getFromUserId() != null) {
            paramName = "createUser";
            exp0 = " and summary.startUserId = :" + paramName + " ";
            exp0 += " and affair.state=" + StateEnum.col_sent.getKey();
            paramNameList.add(paramName);
            parameterMap.put(paramName, cModel.getFromUserId());
            hql = hql + exp0;
        } else {//查发给我的
            //paramName = "createUser";
            //exp0 = " and summary.startUserId = :"+paramName+" ";
            exp0 = " and (affair.state=" + StateEnum.col_pending.getKey() + " or affair.state=" + StateEnum.col_done.getKey() + ")";
            //paramNameList.add(paramName);
            //parameterMap.put(paramName, cModel.getFromUserId());
            hql = hql + exp0;
        }

        if (cModel.getBeginDate() != null) {
            paramName = "timestamp1";
            hql = hql + " and summary.startTime >= :" + paramName;

            paramNameList.add(paramName);
            parameterMap.put(paramName, cModel.getBeginDate());
        }
        if (cModel.getEndDate() != null) {
            paramName = "timestamp2";
            hql = hql + " and summary.startTime <= :" + paramName;

            paramNameList.add(paramName);
            parameterMap.put(paramName, cModel.getEndDate());
        }
        //归挡标志
        paramName = "isPigeonholed";
        cModel.getPigeonholedFlag();
        hql = hql + " and summary.hasArchive = :" + paramName;
        paramNameList.add(paramName);
        parameterMap.put(paramName, cModel.getPigeonholedFlag());

        hql = hql + " order by summary.startTime desc";

        parameterMap.put(SearchManager.NAME_LIST, paramNameList);
        List result = searchManager.searchByHql(hql, parameterMap);

        for (int i = 0; i < result.size(); i++) {
            Object[] object = (Object[]) result.get(i);
            CtpAffair affair = (CtpAffair) object[0];
            EdocSummary summary = (EdocSummary) object[1];

            //开始组装最后返回的结果
            String fromUserName = "";
            try {
                fromUserName = orgManager.getMemberById(summary.getStartUserId()).getName();
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            String bodyType = affair.getBodyType();
            boolean hasAttachments = summary.isHasAttachments();
            ResultModel rsm = new ResultModel(summary.getSubject(), fromUserName, summary.getStartTime(), "", "", bodyType, hasAttachments);

            String edocLocationName = EdocUtil.getEdocLocationName(summary.getEdocType(), affair.getState());
            rsm.setLocationPath(edocLocationName);

            if (affair.getState() == StateEnum.col_waitSend.key()) {
                rsm.setOpenLink("/edocController.do?method=detail&from=WaitSend&affairId=" + affair.getId());
            } else if (affair.getState() == StateEnum.col_sent.key()) {
                rsm.setOpenLink("/edocController.do?method=detail&from=sended&affairId=" + affair.getId());
            } else if (affair.getState() == StateEnum.col_pending.key()) {
                rsm.setOpenLink("/edocController.do?method=detail&from=Pending&affairId=" + affair.getId());
            } else {
                rsm.setOpenLink("/edocController.do?method=detail&from=Done&affairId=" + affair.getId());
            }

            rsms.add(rsm);
        }
        return rsms;
    }

    public boolean useMetadataValue(Long domainId, Long metadataId, String value) {
        boolean ret = true;
        String fieldNames = edocElementManager.getRefMetadataFieldName(domainId, metadataId);
        if ("".equals(fieldNames)) {
            ret = false;
            return ret;
        }
        ret = edocSummaryDao.isUseMetadataValue(fieldNames, value);
        return ret;
    }

    public EdocElementManager getEdocElementManager() {
        return edocElementManager;
    }

    public void setEdocElementManager(EdocElementManager edocElementManager) {
        this.edocElementManager = edocElementManager;
    }


    /**
     * 通过processId取到summary
     *
     * @param processId
     * @return
     */
    public EdocSummary getSummaryByProcessId(String processId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(EdocSummary.class);
        criteria.add(Restrictions.eq("processId", processId));
        return (EdocSummary) edocSummaryDao.executeUniqueCriteria(criteria);
    }


    /**
     * 公文管理员操作 发文登记簿查询功能
     *
     * @param em         查询条件
     * @param needByPage 是否需要分页
     * @return
     */
    public List<EdocSummaryModel> queryByDocManager(EdocSearchModel em, boolean needByPage, long accountId, String condition, String field, String field1) {
        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>();

        int edocType = em.getEdocType();
        List<Object> objects = new ArrayList<Object>();
        StringBuilder hql = new StringBuilder();
        User user = AppContext.getCurrentUser();
        if (edocType == 0) {
            //发文登记簿——查询时间条件
            int sendQueryTimeType = em.getSendQueryTimeType();

            //OA-19095 在公文交换--已发送中进行了撤销、补发的操作，发文登记簿统计时字段中的主送单位丢失了
            //加入了获取 sendedTypeIds属性
            //发文登记簿查询日期按 分发时间查询
            hql.append("select ")
                    .append(selectSummary)
                    .append(",affair.state,send.sendedTypeIds,summary.keepPeriod,affair.id ,summary.sendDepartment,send.sendUserId ")
                    .append(" from ")
                    .append(" CtpAffair affair, EdocSummary as summary left join summary.edocSendRecords as send where affair.objectId = summary.id ")
                    .append(" and summary.edocType = ? and summary.orgAccountId = ? and summary.completeTime is not null ");

            if (sendQueryTimeType == 3) {//交换日期
                hql.append(" and ( send.status in(1,2,5) and send.id in (select detail.sendRecordId from EdocSendDetail as detail where send.id=detail.sendRecordId and detail.status in (0,1)))");
            } else {
                hql.append(" and ((send.status is null) or ( send.isBase=1 )) ");
            }

            hql.append(" and affair.app = ? and affair.state = ? ");


            objects.add(em.getEdocType());
            objects.add(accountId);
            objects.add(ApplicationCategoryEnum.edocSend.key());
            objects.add(StateEnum.col_sent.key());


            String sendQueryTimeStr = "send.sendTime"; //交换日期
            if (sendQueryTimeType == 2) { //签发日期
                sendQueryTimeStr = "summary.signingDate";
            } else if (sendQueryTimeType == 1) { //拟文日期
                sendQueryTimeStr = "summary.createTime";
            } else if (sendQueryTimeType == 3) {//交换日期
                sendQueryTimeStr = "send.sendTime";
            }

            if (sendQueryTimeType != 0) {
                if (em.getSigningDateA() != null) {
                    hql.append(" and (");
                    hql.append(sendQueryTimeStr);
                    hql.append(" >= ?)");
                    objects.add(Datetimes.getTodayFirstTime(em.getSigningDateA()));
                }
                if (em.getSigningDateB() != null) {
                    hql.append(" and (");
                    hql.append(sendQueryTimeStr);
                    hql.append(" <= ?)");
                    //Datetimes.getTodayLastTime 日期格式化
                    objects.add(Datetimes.getTodayLastTime(em.getSigningDateB()));
                }
            }

            //需要判断field是否为空，为空就拼sql了，不然导出没有数据了
            if (condition != null && Strings.isNotBlank(field)) {
                if ("subject".equals(condition)) {//标题
                    hql.append(" and (summary.subject like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("docMark".equals(condition)) {//公文文号
                    hql.append(" and (summary.docMark like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("serialNo".equals(condition)) {//内部文号
                    hql.append(" and (summary.serialNo like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("createPerson".equals(condition)) {//发起人
                    hql.append(" and (summary.createPerson like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }

                if ("sendUnit".equals(condition)) {//发文单位
                    hql.append(" and (summary.sendUnit like ? or summary.sendUnit2 like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("sendDepartment".equals(condition) && Strings.isNotBlank(field)) {//发文部门
                    hql.append(" and (summary.sendDepartment like ? or summary.sendDepartment2 like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }

                if ("secretLevel".equals(condition) && Strings.isNotBlank(field)) {//密级
                    hql.append(" and (summary.secretLevel like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("urgentLevel".equals(condition) && Strings.isNotBlank(field)) {//紧急程度
                    hql.append(" and (summary.urgentLevel like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }

            }

            if (condition != null && (Strings.isNotBlank(field) || Strings.isNotBlank(field1))) {
                if ("createDate".equals(condition)) {//发起日期
                    if (StringUtils.isNotBlank(field)) {
                        hql.append(" and (summary.createTime >= ?)");
                        objects.add(Datetimes.getTodayFirstTime(field));
                    }
                    if (StringUtils.isNotBlank(field1)) {
                        hql.append(" and (summary.createTime <= ?)");
                        objects.add(Datetimes.getTodayLastTime(field1));
                    }
                }
            }


            try {
                //OA-41805 部门收发员在发文登记簿中能看到的是整个单位的收发数据，权限放大了
                if (!EdocRoleHelper.isAccountExchange(user.getId())) {
                    String departmentIds = EdocRoleHelper.getUserExchangeDepartmentIds();
                    if (Strings.isNotBlank(departmentIds)) {
                        String[] depIds = departmentIds.split("[,]");
                        if (depIds.length > 0) {
                            hql.append("  and ( (send.status is not null and send.exchangeType=? and send.exchangeOrgId in (");
                            objects.add(EdocSendRecord.Exchange_Send_iExchangeType_Dept);
                            for (int i = 0; i < depIds.length; i++) {
                                if (i == depIds.length - 1) {
                                    hql.append("?");
                                } else {
                                    hql.append("?,");
                                }
                                objects.add(Long.parseLong(depIds[i]));
                            }
                            hql.append(")) or (send.status is null and   summary.orgDepartmentId in (");
                            for (int i = 0; i < depIds.length; i++) {
                                if (i == depIds.length - 1) {
                                    hql.append("?");
                                } else {
                                    hql.append("?,");
                                }
                                objects.add(Long.parseLong(depIds[i]));
                            }
                            hql.append(")))");
                        }
                    }
                }
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }


            if (sendQueryTimeType == 2) { //签发日期
                hql.append(" order by summary.signingDate desc ");
            } else if (sendQueryTimeType == 3) {//交换日期
                hql.append(" order by send.sendTime desc");
            } else {//拟文日期
                hql.append(" order by summary.createTime desc ");
            }
        } else {
            hql = new StringBuilder("select register.subject,register.serialNo,register.secretLevel,register.keepPeriod, register.urgentLevel,register.edocUnit, ");
            hql.append("register.docMark,register.copies,register.sendTo,register.copyTo,register.issuer,register.keywords,summary.undertaker,summary.docType,register.signer, ");
            hql.append("register.registerUserName,register.registerDate,register.distributer,   ");
            hql.append("register.createTime,affair.id,affair.objectId,affair.state,register.sendUnitType from EdocRegister register,CtpAffair affair,EdocSummary summary ");
            hql.append("where register.distributeEdocId = affair.objectId and affair.objectId = summary.id ");
            hql.append("and affair.state = ?  and register.edocType = ? ");
            hql.append("and register.state in( ?,?,?,? ) and register.orgAccountId = ? ");

            objects.add(StateEnum.col_sent.key());
            objects.add(em.getEdocType());
            objects.add(2);//已经登记的
            //GOV-3538 已登记的公文在列表中逻辑删除后，不能在收文登记薄中查询出来
            objects.add(EdocNavigationEnum.RegisterState.deleted.ordinal()); //登记后删除的
            objects.add(10); //分发后删除的
            objects.add(100); //纸质分发
            objects.add(accountId);

            if (em.getRegisterDateB() != null) {
                hql.append(" and (register.registerDate >= ?)");
                objects.add(Datetimes.getTodayFirstTime(em.getRegisterDateB()));
            }
            if (em.getRegisterDateE() != null) {
                hql.append(" and (register.registerDate <= ?)");
                objects.add(Datetimes.getTodayLastTime(em.getRegisterDateE()));
            }
            //OA-51363  收文登记簿列表，查询条件按照来文字号、收文编号查询，发现没输入的时候没有查询出所有记录
            if (condition != null && Strings.isNotBlank(field)) {
                if ("subject".equals(condition)) {//标题
                    hql.append(" and (register.subject like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("serialNo".equals(condition)) {//收文编号
                    hql.append(" and (register.serialNo like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }
                if ("docMark".equals(condition)) {//来字文号
                    hql.append(" and (register.docMark like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }

                if ("sendUnit".equals(condition)) {//收文单位
//                    hql += " and (register.sendUnit like ? or register.sendUnitId like ?)";
//                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
//                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                    //OA-31801 收文登记簿，小查询中，发文单位，其实是来文单位。
                    hql.append(" and summary.sendTo like ? ");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }

                if ("registerUserName".equals(condition)) {//登记人
                    hql.append(" and (register.registerUserName like ?)");
                    objects.add("%" + SQLWildcardUtil.escape(field) + "%");
                }


            }

            if (condition != null && (Strings.isNotBlank(field) || Strings.isNotBlank(field1))) {
                if ("recieveDate".equals(condition)) {//签收时间
                    if (StringUtils.isNotBlank(field)) {
                        hql.append(" and (register.recTime >= ?)");
                        objects.add(Datetimes.getTodayFirstTime(field));
                    }
                    if (StringUtils.isNotBlank(field1)) {
                        hql.append(" and (register.recTime <= ?)");
                        objects.add(Datetimes.getTodayLastTime(field1));
                    }
                }

                if ("registerDate".equals(condition)) {//登记时间
                    if (StringUtils.isNotBlank(field)) {
                        hql.append(" and (register.registerDate >= ?)");
                        objects.add(Datetimes.getTodayFirstTime(field));
                    }
                    if (StringUtils.isNotBlank(field1)) {
                        hql.append(" and (register.registerDate <= ?)");
                        objects.add(Datetimes.getTodayLastTime(field1));
                    }
                }
            }
            try {
                if (!EdocRoleHelper.isAccountExchange(user.getId())) {
                    String departmentIds = EdocRoleHelper.getUserExchangeDepartmentIds();
                    if (Strings.isNotBlank(departmentIds)) {
                        String[] depIds = departmentIds.split("[,]");
                        if (depIds.length > 0) {
                            hql.append(" and summary.orgDepartmentId in (");
                            for (int i = 0; i < depIds.length; i++) {
                                if (i == depIds.length - 1) {
                                    hql.append("?");
                                } else {
                                    hql.append("?,");
                                }
                                objects.add(Long.parseLong(depIds[i]));
                            }
                            hql.append(") ");
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            hql.append(" order by register.createTime desc");
        }
        List result = new ArrayList();


        if (needByPage) { //需要分页
//        	 result = edocSummaryDao.find(selectHql,"summary.id",true, null, objects);
            result = edocSummaryDao.find(hql.toString(), null, objects);
        } else { //不需要分页
            result = edocSummaryDao.find(hql.toString(), -1, -1, null, objects);
        }

        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object[] object = (Object[]) result.get(i);

                int n = 0;
                EdocSummary summary = new EdocSummary();
                if (edocType == 0) {
                    make(object, summary, false);
                } else {
                    summary.setSubject((String) object[n++]);
                    summary.setSerialNo((String) object[n++]);
                    summary.setSecretLevel((String) object[n++]);
                    String kp = (String) object[n++];
                    //OA-29574 收文登记簿--查询报错
                    if (Strings.isBlank(kp) || "null".equals(kp)) {
                        kp = "1";
                    }
                    summary.setKeepPeriod(Integer.parseInt(kp));
                    summary.setUrgentLevel((String) object[n++]);
                    summary.setSendUnit((String) object[n++]);
                    summary.setDocMark((String) object[n++]);
                    summary.setCopies(object[n] == null ? null : ((Number) object[n]).intValue());
                    n++;
                    summary.setSendTo((String) object[n++]);
                    summary.setCopyTo((String) object[n++]);
                    summary.setIssuer((String) object[n++]);
                    summary.setKeywords((String) object[n++]);
                    summary.setUndertaker((String) object[n++]);
                    summary.setDocType((String) object[n++]);
                    try {
                        //TODO__承办单位和承办部门
                    } catch (Exception e) {
                        summary.setUndertakerAccount(null);
                        summary.setUndertakerDep(null);
                    }
                }

                summary.setEdocType(em.getEdocType());

                //开始组装最后返回的结果
                EdocSummaryModel model = new EdocSummaryModel();

//                 model.setStartDate(new Date(summary.getCreateTime().getTime()));
//                 model.setWorkitemId(summary.getId() + "");
//                 model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);

                if (em.getEdocType() == 0) {
                    Long userId = object[object.length - 1] == null ? null : ((Number) object[object.length - 1]).longValue();
                    String userName = "";
                    //修复bug GOV-2915 公文管理-发文管理-分发，发文登记簿，查询，最后2条公文的分发人是审计管理员
                    if (userId != null && userId != 0L) {
                        try {
                            userName = orgManager.getMemberById(userId).getName();
                        } catch (BusinessException e) {
                            LOGGER.error("", e);
                        }
                    }
                    model.setSender(userName);

                    //OA-25915  发文登记簿，查询出来的数据，发文部门全都是a部门。但是通过小查询发文部门，只能查出一条数据。
                    Object depName = object[object.length - 2];
                    if (depName != null) {
                        model.setDepartmentName(String.valueOf(depName));
                    }

                    model.setAffairId(object[object.length - 3] == null ? null : ((Number) object[object.length - 3]).longValue());
//                	 model.setSender((String)object[object.length-1]);

                    Object keepPeriod = object[object.length - 4];
                    if (keepPeriod != null && Strings.isNotBlank(String.valueOf(keepPeriod))) {
                        try {
                            summary.setKeepPeriod(Integer.parseInt(String.valueOf(keepPeriod)));
                        } catch (Exception e) {
                            summary.setKeepPeriod(null);
                        }
                    } else {
                        summary.setKeepPeriod(null);
                    }
                    //当发文时没有选择主送单位的时候，才采用下面的逻辑
                    if (Strings.isBlank(summary.getSendTo())) {
                        //OA-19095 在公文交换--已发送中进行了撤销、补发的操作，发文登记簿统计时字段中的主送单位丢失了 start
                        String sendedTypeIds = String.valueOf(object[object.length - 5]);
                        StringBuilder sendName = new StringBuilder();
                        try {
                            List<V3xOrgEntity> entitys = orgManager.getEntities(sendedTypeIds);
                            if (Strings.isNotBlank(sendedTypeIds) && entitys != null && entitys.size() > 0) {
                                for (int j = 0; j < entitys.size(); j++) {
                                    V3xOrgEntity entity = entitys.get(j);
                                    if (j != entitys.size() - 1) {
                                        sendName.append(entity.getName());
                                        sendName.append(",");
                                    } else {
                                        sendName.append(entity.getName());
                                    }
                                }
                                summary.setSendTo(sendName.toString());
                            }

                        } catch (BusinessException e) {
                            LOGGER.error("获取单位或部门名称错误" + e);
                        }
                    }
                    model.setState((Integer) object[object.length - 6]);
                    //OA-19095 在公文交换--已发送中进行了撤销、补发的操作，发文登记簿统计时字段中的主送单位丢失了 end
                }
                if (em.getEdocType() == 1) {
                    model.setSigner((String) object[n++]);
                    model.setRegisterUserName((String) object[n++]);
                    model.setRegisterDate((java.sql.Date) object[n++]);
                    model.setDistributer((String) object[n++]);
//                	 model.setRecieveDate((java.sql.Timestamp)object[object.length-1]);
                    model.setRecieveDate((java.sql.Timestamp) object[n++]);
                    model.setAffairId(object[n] == null ? null : ((Number) object[n]).longValue());
                    n++;
                    summary.setId(object[n] == null ? null : ((Number) object[n]).longValue());
                    n++;
                    model.setState(object[n] == null ? null : ((Number) object[n]).intValue());
                    n++;
                    model.setSendUnitType(object[n] == null ? null : ((Number) object[n]).intValue());
                    n++;
                }

                String sendToUnit = "";
                if (!Strings.isBlank(summary.getSendTo())) {
                    sendToUnit = summary.getSendTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getCopyTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getCopyTo();
                    }
                } else {
                    sendToUnit = summary.getCopyTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getReportTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getReportTo();
                    }
                } else {
                    sendToUnit = summary.getReportTo();
                }
                model.setSendToUnit(sendToUnit);

                models.add(model);
            }
        }
        return models;
    }


    /**
     * 公文查询
     *
     * @param curUserId  当前用户ID
     * @param em         查询条件
     * @param needByPage 是否需要分页
     * @return
     * @throws BusinessException
     */
    public List<EdocSummaryModel> queryByCondition(long curUserId, EdocSearchModel em, boolean needByPage) throws BusinessException {
        List<EdocSummaryModel> list = new ArrayList<EdocSummaryModel>();
        if (Strings.isNotBlank(em.isGourpBy()) && "true".equals(em.isGourpBy())) {
            list = queryByConditionDumplation(curUserId, em, needByPage);
        } else {
            list = queryByCondition0(curUserId, em, needByPage);
        }
        return list;
    }

    private List<EdocSummaryModel> queryByCondition0(long curUserId, EdocSearchModel em, boolean needByPage) throws BusinessException {
        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        String hql = "";
        String hqlTitle = "select  " + selectSummary + ",affair.state,summary.archiveId,affair.bodyType,affair.id,summary.undertakenoffice,summary.unitLevel,  ";
        hqlTitle += selectSummayExtend;
        if (Strings.isNotBlank(em.isGourpBy()) && "true".equals(em.isGourpBy())) {
            hql = hqlTitle + "from EdocSummary as summary , EdocSummaryExtend as extend1, CtpAffair as affair where affair.objectId=summary.id and summary.id = extend1.summaryId ";
            hql += " and affair.id in ( select max(affair.id) ";
        } else {
            hql = hqlTitle;
        }
        hql += "from EdocSummary as summary , EdocSummaryExtend as extend1, CtpAffair as affair "
                + " where (affair.objectId=summary.id) and (affair.memberId=:curUserId)"
                + "  and summary.id = extend1.summaryId "
                + " and (affair.state in (" + StateEnum.col_done.getKey() + "," + StateEnum.col_pending.getKey() + "," + StateEnum.col_sent.getKey() + ") )"
                + " and summary.state<> " + CollaborationEnum.flowState.deleted.ordinal();//不读取公文归档后删除。
        paramMap.put("curUserId", curUserId);

        if (em.getEdocType() == 3) {//全部
            hql += " and (affair.app in (" + ApplicationCategoryEnum.edocSend.key() + "," + ApplicationCategoryEnum.edocRec.key() + "," + ApplicationCategoryEnum.edocSign.key() + "))";
        } else {
            hql += " and (affair.app=:app)";
            paramMap.put("app", EdocUtil.getAppCategoryByEdocType(em.getEdocType()).key());
        }


        if (!Strings.isBlank(em.getSubject())) {
            hql += " and (summary.subject like :subject)";
            paramMap.put("subject", "%" + SQLWildcardUtil.escape(em.getSubject()) + "%");
        }
        if (!Strings.isBlank(em.getDocMark())) {
            hql += " and (summary.docMark like :docMark or summary.docMark2 like :docMark2)";
            paramMap.put("docMark", "%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
            paramMap.put("docMark2", "%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
        }
        if (!Strings.isBlank(em.getSerialNo())) {
            hql += " and (summary.serialNo like :serialNo)";
            paramMap.put("serialNo", "%" + SQLWildcardUtil.escape(em.getSerialNo()) + "%");
        }
        if (!Strings.isBlank(em.getKeywords())) {
            hql += " and (summary.keywords like :keywords)";
            paramMap.put("keywords", "%" + SQLWildcardUtil.escape(em.getKeywords()) + "%");
        }
        if (!Strings.isBlank(em.getDocType())) {
            hql += " and (summary.docType = :docType)";
            paramMap.put("docType", em.getDocType());
        }
        if (!Strings.isBlank(em.getSendType())) {
            hql += " and (summary.sendType = :sendType)";
            paramMap.put("sendType", em.getSendType());
        }
        if (!Strings.isBlank(em.getCreatePerson())) {
            hql += " and (summary.createPerson like :createPerson)";
            paramMap.put("createPerson", "%" + SQLWildcardUtil.escape(em.getCreatePerson()) + "%");
        }
        if (em.getCreateTimeB() != null) {
            hql += " and (summary.createTime >= :createTimeB)";
            paramMap.put("createTimeB", Datetimes.getTodayFirstTime(em.getCreateTimeB()));
        }
        if (em.getCreateTimeE() != null) {
            hql += " and (summary.createTime <= :createTimeE)";
            paramMap.put("createTimeE", Datetimes.getTodayLastTime(em.getCreateTimeE()));
        }
        if (Strings.isNotBlank(em.getSendToId())) {
            //主送ID查询
            hql += " and ((summary.sendToId like :sendToId or summary.sendToId2 like :sendToId2)";
            paramMap.put("sendToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("sendToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            //主送单位名称
            if (Strings.isNotBlank(em.getSendTo())) {
                hql += " or (summary.sendTo like :sendTo or summary.sendTo2 like :sendTo2)";
                paramMap.put("sendTo", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
                paramMap.put("sendTo2", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
            }
            //抄送
            hql += " or (summary.copyToId like :copyToId or summary.copyToId2 like :copyToId2)";
            paramMap.put("copyToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("copyToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            //抄报
            hql += " or (summary.reportToId like :reportToId or summary.reportToId2 like :reportToId2))";
            paramMap.put("reportToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("reportToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
        } else if (Strings.isNotBlank(em.getSendTo())) {
            hql += " and (summary.sendTo like :sendTo or summary.sendTo2 like :sendTo2)";
            paramMap.put("sendTo", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
            paramMap.put("sendTo2", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
        }
        if (Strings.isNotBlank(em.getSendUnitId())) {
            hql += " and ((summary.sendUnitId like :sendUnitId or summary.sendUnitId2 like :sendUnitId2)";
            paramMap.put("sendUnitId", "%" + SQLWildcardUtil.escape(em.getSendUnitId()) + "%");
            paramMap.put("sendUnitId2", "%" + SQLWildcardUtil.escape(em.getSendUnitId()) + "%");
            if (Strings.isNotBlank(em.getSendUnit())) {
                hql += " or (summary.sendUnit like :sendUnit or summary.sendUnit2 like :sendUnit2))";
                paramMap.put("sendUnit", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
                paramMap.put("sendUnit2", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
            } else {
                hql += ")";
            }
        } else if (Strings.isNotBlank(em.getSendUnit())) {
            hql += " and (summary.sendUnit like :sendUnit or summary.sendUnit2 like :sendUnit2)";
            paramMap.put("sendUnit", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
            paramMap.put("sendUnit2", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
        }
        //发文部门
        if (!Strings.isBlank(em.getSendDepartmentId())) {
            hql += " and (summary.sendDepartmentId like :sendDepartmentId or summary.sendDepartmentId2 like :sendDepartmentId2)";
            paramMap.put("sendDepartmentId", "%" + SQLWildcardUtil.escape(em.getSendDepartmentId()) + "%");
            paramMap.put("sendDepartmentId2", "%" + SQLWildcardUtil.escape(em.getSendDepartmentId()) + "%");
        }
        if (!Strings.isBlank(em.getIssuer())) {
            hql += " and (summary.issuer like :issuer)";
            paramMap.put("issuer", "%" + SQLWildcardUtil.escape(em.getIssuer()) + "%");
        }
        if (em.getSigningDateA() != null) {
            hql += " and (summary.signingDate >= :signingDateA)";
            paramMap.put("signingDateA", Datetimes.getTodayFirstTime(em.getSigningDateA()));
        }
        if (em.getSigningDateB() != null) {
            hql += " and (summary.signingDate <= :signingDateB)";
            paramMap.put("signingDateB", Datetimes.getTodayLastTime(em.getSigningDateB()));
        }
        if (Strings.isNotBlank(em.isGourpBy()) && "true".equals(em.isGourpBy())) {
            hql += " group by summary.id )  ";
        }

        String selectHql = hql + " order by summary.createTime desc";

        List result = new ArrayList();

        if (needByPage) { //需要分页
            result = edocSummaryDao.find(selectHql, "affair.id", true, paramMap);
        } else { //不需要分页
            result = edocSummaryDao.find(selectHql, -1, -1, paramMap);
        }
        List<Long> summaryIds = new ArrayList<Long>();
        List<Long> summaryIdsList = new ArrayList<Long>();
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object[] object = (Object[]) result.get(i);
                EdocSummary summary = new EdocSummary();
                make(object, summary, true);
                summary.setEdocType(em.getEdocType());
                summaryIdsList.add(summary.getId());
                try {
                    V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                    summary.setStartMember(member);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }

                //开始组装最后返回的结果
                EdocSummaryModel model = new EdocSummaryModel();

                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setWorkitemId(summary.getId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                int length = object.length - 125;
                Long archiveId = object[length - 5] == null ? null : ((Number) object[length - 5]).longValue();
                if (archiveId != null) {
                    summaryIds.add(archiveId);
                    model.getSummary().setArchiveId(archiveId);
                }

                model.setAffairId(object[length - 3] == null ? null : ((Number) object[length - 3]).longValue());
                model.setBodyType((String) object[length - 4]);
                model.setState((Integer) object[length - 6]);

                String undertakenoffice = (String) object[length - 2];
                summary.setUndertakenoffice(undertakenoffice);

                String unitLevel = (String) object[length - 1];
                summary.setUnitLevel(unitLevel);

                String sendToUnit = "";
                if (!Strings.isBlank(summary.getSendTo())) {
                    sendToUnit = summary.getSendTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getCopyTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getCopyTo();
                    }
                } else {
                    sendToUnit = summary.getCopyTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getReportTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getReportTo();
                    }
                } else {
                    sendToUnit = summary.getReportTo();
                }
                model.setSendToUnit(sendToUnit);

                models.add(model);
            }
        }
        //备份最开始的分页信息
        int fristResult = Pagination.getFirstResult();
        int maxResult = Pagination.getMaxResults();
        int rowCount = Pagination.getRowCount();
        boolean isNeedCount = Pagination.isNeedCount();


        //查询DocResouce,获取归档路径。
        if (Strings.isNotEmpty(summaryIds) && AppContext.hasPlugin("doc")) {
            List<DocResourceBO> docs = docApi.findDocResources(summaryIds);
            for (EdocSummaryModel model : models) {
                Long summaryId = model.getSummary().getId();
                if (summaryId != null && model.getSummary().getHasArchive()) {
                    for (DocResourceBO doc : docs) {
                        if (doc.getId().equals(model.getSummary().getArchiveId())) {

                            StringBuilder frName = new StringBuilder(ResourceUtil.getString(doc.getFrName()));

                            if (doc.getLogicalPath() != null && doc.getLogicalPath().split("\\.").length > 1) {
                                frName = new StringBuilder(com.seeyon.v3x.edoc.util.Constants.Edoc_PAGE_SHOWPIGEONHOLE_SYMBOL + java.io.File.separator)
                                        .append(frName);
                            }

                            model.setArchiveName(frName.toString());
                            model.setLogicalPath((String) doc.getLogicalPath());
                            break;
                        }
                    }
                }
            }
        }

        // 组装公文 额外的公文元素的数据
        if (null != summaryIdsList && summaryIdsList.size() > 0) {
            List<EdocSummaryExtend> edocSummaryExtends = edocSummaryExtendManager.getEdocSummaryExtendBySummaryIds(summaryIdsList);
            Map<Long, EdocSummaryExtend> edocSummaryExtendsMap = new HashMap<Long, EdocSummaryExtend>();
            for (EdocSummaryExtend edocSummaryExtend : edocSummaryExtends) {
                edocSummaryExtendsMap.put(edocSummaryExtend.getSummaryId(), edocSummaryExtend);
            }
            for (EdocSummaryModel model : models) {
                Long summaryId = model.getSummary().getId();
                if (summaryId != null) {
                    EdocSummaryExtend edocSummaryExtend = edocSummaryExtendsMap.get(summaryId);
                    if (edocSummaryExtend != null) {
                        edocSummaryExtendManager.transSetExtendToSummary(model.getSummary(), edocSummaryExtend);
                    }
                }
            }
        }
        //恢复分页信息
        Pagination.setFirstResult(fristResult);
        Pagination.setMaxResults(maxResult);
        Pagination.setNeedCount(isNeedCount);
        Pagination.setRowCount(rowCount);
        return models;
    }


    /**
     * 同一流程只显示一条时
     *
     * @param curUserId
     * @param em
     * @param needByPage
     * @return
     * @throws BusinessException
     */
    private List<EdocSummaryModel> queryByConditionDumplation(long curUserId, EdocSearchModel em, boolean needByPage) throws BusinessException {
        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        String hql = "";
        hql += "select max(affair1.id) id from edoc_summary  summary1 , ctp_affair  affair1 "
                + " where (affair1.object_id=summary1.id) and (affair1.member_id=:curuserid)"
                + " and (affair1.state in (" + StateEnum.col_done.getKey() + "," + StateEnum.col_pending.getKey() + "," + StateEnum.col_sent.getKey() + ") )"
                //+ " and affair.delete=false"
                + " and summary1.state<> " + CollaborationEnum.flowState.deleted.ordinal();//不读取公文归档后删除。
        paramMap.put("curuserid", curUserId);

        if (em.getEdocType() == 3) {//全部
            hql += " and (affair1.app in (" + ApplicationCategoryEnum.edocSend.key() + "," + ApplicationCategoryEnum.edocRec.key() + "," + ApplicationCategoryEnum.edocSign.key() + "))";
        } else {
            hql += " and (affair1.app=:app)";
            paramMap.put("app", EdocUtil.getAppCategoryByEdocType(em.getEdocType()).key());
        }


        if (!Strings.isBlank(em.getSubject())) {
            hql += " and (summary1.subject like :subject)";
            paramMap.put("subject", "%" + SQLWildcardUtil.escape(em.getSubject()) + "%");
        }
        if (!Strings.isBlank(em.getDocMark())) {
            hql += " and (summary1.doc_Mark like :docMark or summary1.doc_Mark2 like :docMark2)";
            paramMap.put("docMark", "%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
            paramMap.put("docMark2", "%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
        }
        if (!Strings.isBlank(em.getSerialNo())) {
            hql += " and (summary1.serial_No like :serialNo)";
            paramMap.put("serialNo", "%" + SQLWildcardUtil.escape(em.getSerialNo()) + "%");
        }
        if (!Strings.isBlank(em.getKeywords())) {
            hql += " and (summary1.keywords like :keywords)";
            paramMap.put("keywords", "%" + SQLWildcardUtil.escape(em.getKeywords()) + "%");
        }
        if (!Strings.isBlank(em.getDocType())) {
            hql += " and (summary1.doc_Type = :docType)";
            paramMap.put("docType", em.getDocType());
        }
        if (!Strings.isBlank(em.getSendType())) {
            hql += " and (summary1.send_Type = :sendType)";
            paramMap.put("sendType", em.getSendType());
        }
        if (!Strings.isBlank(em.getCreatePerson())) {
            hql += " and (summary1.create_Person like :createPerson)";
            paramMap.put("createPerson", "%" + SQLWildcardUtil.escape(em.getCreatePerson()) + "%");
        }
        if (em.getCreateTimeB() != null) {
            hql += " and (summary1.create_Time >= :createTimeB)";
            paramMap.put("createTimeB", Datetimes.getTodayFirstTime(em.getCreateTimeB()));
        }
        if (em.getCreateTimeE() != null) {
            hql += " and (summary1.create_Time <= :createTimeE)";
            paramMap.put("createTimeE", Datetimes.getTodayLastTime(em.getCreateTimeE()));
        }
        if (Strings.isNotBlank(em.getSendToId())) {
            //主送
            hql += " and ((summary1.send_To_Id like :sendToId or summary1.send_To_Id2 like :sendToId2)";
            paramMap.put("sendToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("sendToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            //单位名称查询
            if (Strings.isNotBlank(em.getSendTo())) {
                hql += " or (summary1.send_To like :sendTo or summary1.send_To2 like :sendTo2)";
                paramMap.put("sendTo", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
                paramMap.put("sendTo2", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
            }
            //抄送
            hql += " or (summary1.copy_To_Id like :copyToId or summary1.copy_To_Id2 like :copyToId2)";
            paramMap.put("copyToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("copyToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            //抄报
            hql += " or (summary1.report_To_Id like :reportToId or summary1.report_To_Id2 like :reportToId2))";
            paramMap.put("reportToId", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            paramMap.put("reportToId2", "%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
        } else if (Strings.isNotBlank(em.getSendTo())) {
            hql += " and (summary1.send_To like :sendTo or summary1.send_To2 like :sendTo2)";
            paramMap.put("sendTo", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
            paramMap.put("sendTo2", "%" + SQLWildcardUtil.escape(em.getSendTo()) + "%");
        }

        if (Strings.isNotBlank(em.getSendUnitId())) {
            hql += " and ((summary1.send_Unit_Id like :sendUnitId or summary1.send_Unit_Id2 like :sendUnitId2)";
            paramMap.put("sendUnitId", "%" + SQLWildcardUtil.escape(em.getSendUnitId()) + "%");
            paramMap.put("sendUnitId2", "%" + SQLWildcardUtil.escape(em.getSendUnitId()) + "%");
            if (Strings.isNotBlank(em.getSendUnit())) {
                hql += " or (summary1.send_Unit like :sendUnit or summary1.send_Unit2 like :sendUnit2))";
                paramMap.put("sendUnit", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
                paramMap.put("sendUnit2", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
            } else {
                hql += ")";
            }
        } else if (Strings.isNotBlank(em.getSendUnit())) {
            hql += " and (summary1.send_Unit like :sendUnit or summary1.send_Unit2 like :sendUnit2)";
            paramMap.put("sendUnit", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
            paramMap.put("sendUnit2", "%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
        }
        //发文部门
        if (!Strings.isBlank(em.getSendDepartmentId())) {
            hql += " and (summary1.send_Department_Id like :sendDepartmentId or summary1.send_Department_Id2 like :sendDepartmentId2)";
            paramMap.put("sendDepartmentId", "%" + SQLWildcardUtil.escape(em.getSendDepartmentId()) + "%");
            paramMap.put("sendDepartmentId2", "%" + SQLWildcardUtil.escape(em.getSendDepartmentId()) + "%");
        }
        if (!Strings.isBlank(em.getIssuer())) {
            hql += " and (summary1.issuer like :issuer)";
            paramMap.put("issuer", "%" + SQLWildcardUtil.escape(em.getIssuer()) + "%");
        }
        if (em.getSigningDateA() != null) {
            hql += " and (summary1.signing_Date >= :signingDateA)";
            paramMap.put("signingDateA", Datetimes.getTodayFirstTime(em.getSigningDateA()));
        }
        if (em.getSigningDateB() != null) {
            hql += " and (summary1.signing_Date <= :signingDateB)";
            paramMap.put("signingDateB", Datetimes.getTodayLastTime(em.getSigningDateB()));
        }
        if (Strings.isNotBlank(em.isGourpBy()) && "true".equals(em.isGourpBy())) {
            hql += " group by summary1.id   ";
        }
        String selectHql = "";
        String hqlTitle = "select  " + selectSummarySql + ",affair.state affair_state,summary.archive_id archive_id2,affair.body_type affair_body_type,affair.id affair_id,summary.undertakenoffice,summary.unit_level unit_level2 ";
        hqlTitle += "," + selectSummayExtendSql;
        if (Strings.isNotBlank(em.isGourpBy()) && "true".equals(em.isGourpBy())) {
            selectHql = hqlTitle + "from edoc_summary  summary , edoc_summary_extend extend1, Ctp_Affair  affair, (" + hql + ")  affair2 where affair.object_Id=summary.id  and affair.id = affair2.id and summary.id = extend1.summary_id ";
        }
        selectHql = selectHql + " order by summary.create_Time desc";

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        JDBCAgent jdbc = new JDBCAgent(true);
        FlipInfo flipInfo = new FlipInfo();
        flipInfo.setNeedTotal(true);
        HttpServletRequest request = WebUtil.getRequest();
        int page = 0;
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception e) {
        }
        flipInfo.setPage(page);
        flipInfo.setSize(Pagination.getMaxResults());
        try {
            if (needByPage) { //需要分页
                jdbc.findNameByPaging(selectHql, paramMap, flipInfo);
            } else { //不需要分页
                flipInfo.setSize(-1);
                jdbc.findNameByPaging(selectHql, paramMap, flipInfo);
            }
            result = flipInfo.getData();
        } catch (Exception e) {
            LOGGER.error("合并同一流程查询错误！", e);
        } finally {
            jdbc.close();
        }
        Pagination.setRowCount(flipInfo.getTotal());
//        if(needByPage){ //需要分页
//        	 result = edocSummaryDao.find(selectHql,"affair.id", true, paramMap);
//        }else { //不需要分页
//        	 result = edocSummaryDao.find(selectHql,-1,-1,paramMap);
//        }
        List<Long> summaryIds = new ArrayList<Long>();
        List<Long> summaryIdsList = new ArrayList<Long>();
        List<Object[]> querySet = new ArrayList<Object[]>();

        List<String> booleanList = new ArrayList<String>();
        booleanList.add("has_archive");
        booleanList.add("is_quick_send");
        booleanList.add("is_cover_time");
        List<String> decimalList = new ArrayList<String>();
        decimalList.add("affair_state");
        decimalList.add("state");
        List<String> timestampList = new ArrayList<String>();
        timestampList.add("signing_date");
        timestampList.add("deadline_datetime");
        timestampList.add("receipt_date");
        timestampList.add("registration_date");
        for (Map<String, Object> map : result) {
            Object[] obj = new Object[map.size()];
            Iterator ite = map.keySet().iterator();
            int j = 0;
            while (ite.hasNext()) {
                String key = (String) ite.next();
                Object a = map.get(key);
                if (a != null && booleanList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
                    a = Integer.valueOf(String.valueOf(a)) == 0 ? false : true;
                }
                if (a != null && decimalList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
                    a = Integer.valueOf(String.valueOf(a));
                }
                if (a != null && timestampList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
                    a = new Date(((Timestamp) a).getTime());
                }
                obj[j] = a;
                j++;
            }
            querySet.add(obj);
        }
        if (result != null) {
            for (int i = 0; i < querySet.size(); i++) {
                Object[] object = (Object[]) querySet.get(i);
                //Affair affair = new Affair();
                EdocSummary summary = new EdocSummary();
                make(object, summary, true);
                summary.setEdocType(em.getEdocType());
                summaryIdsList.add(summary.getId());
                try {
                    V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                    summary.setStartMember(member);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }

                //开始组装最后返回的结果
                EdocSummaryModel model = new EdocSummaryModel();

                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setWorkitemId(summary.getId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                //model.setAffairId(affair.getId());
                //summaryIds.add(summary.getId());
                Long archiveId = object[53] == null ? null : ((Number) object[53]).longValue();
                if (archiveId != null) {
                    summaryIds.add(archiveId);
                    model.getSummary().setArchiveId(archiveId);
                }

                model.setAffairId(object[55] == null ? null : ((Number) object[55]).longValue());
                model.setBodyType((String) object[54]);
                model.setState((Integer) object[52]);

                String undertakenoffice = (String) object[56];
                summary.setUndertakenoffice(undertakenoffice);

                String unitLevel = (String) object[57];
                summary.setUnitLevel(unitLevel);
                String sendToUnit = "";
                if (!Strings.isBlank(summary.getSendTo())) {
                    sendToUnit = summary.getSendTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getCopyTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getCopyTo();
                    }
                } else {
                    sendToUnit = summary.getCopyTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getReportTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getReportTo();
                    }
                } else {
                    sendToUnit = summary.getReportTo();
                }
                model.setSendToUnit(sendToUnit);

                models.add(model);
            }
        }
        //备份最开始的分页信息
        int fristResult = Pagination.getFirstResult();
        int maxResult = Pagination.getMaxResults();
        int rowCount = Pagination.getRowCount();
        boolean isNeedCount = Pagination.isNeedCount();


        //查询DocResouce,获取归档路径。
        if (Strings.isNotEmpty(summaryIds) && AppContext.hasPlugin("doc")) {
            List<DocResourceBO> docs = docApi.findDocResources(summaryIds);
            for (EdocSummaryModel model : models) {
                Long summaryId = model.getSummary().getId();
                if (summaryId != null && model.getSummary().getHasArchive()) {
                    for (DocResourceBO doc : docs) {
                        if (doc.getId().equals(model.getSummary().getArchiveId())) {

                            StringBuilder frName = new StringBuilder(ResourceUtil.getString(doc.getFrName()));

                            if (doc.getLogicalPath() != null && doc.getLogicalPath().split("\\.").length > 1) {
                                frName = new StringBuilder(com.seeyon.v3x.edoc.util.Constants.Edoc_PAGE_SHOWPIGEONHOLE_SYMBOL + java.io.File.separator)
                                        .append(frName);
                            }

                            model.setArchiveName(frName.toString());
                            model.setLogicalPath((String) doc.getLogicalPath());
                            break;
                        }
                    }
                }
            }
        }

        // 组装公文 额外的公文元素的数据
        if (null != summaryIdsList && summaryIdsList.size() > 0) {
            List<EdocSummaryExtend> edocSummaryExtends = edocSummaryExtendManager.getEdocSummaryExtendBySummaryIds(summaryIdsList);
            Map<Long, EdocSummaryExtend> edocSummaryExtendsMap = new HashMap<Long, EdocSummaryExtend>();
            for (EdocSummaryExtend edocSummaryExtend : edocSummaryExtends) {
                edocSummaryExtendsMap.put(edocSummaryExtend.getSummaryId(), edocSummaryExtend);
            }
            for (EdocSummaryModel model : models) {
                Long summaryId = model.getSummary().getId();
                if (summaryId != null) {
                    EdocSummaryExtend edocSummaryExtend = edocSummaryExtendsMap.get(summaryId);
                    if (edocSummaryExtend != null) {
                        edocSummaryExtendManager.transSetExtendToSummary(model.getSummary(), edocSummaryExtend);
                    }
                }
            }
        }
        //恢复分页信息
        Pagination.setFirstResult(fristResult);
        Pagination.setMaxResults(maxResult);
        Pagination.setNeedCount(isNeedCount);
        Pagination.setRowCount(rowCount);
        return models;
    }

    public List<EdocSummaryModel> queryByCondition(long curUserId, EdocSearchModel em) {
        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>();

        List<Object> objects = new ArrayList<Object>();

        String hql = "select distinct " + selectSummary + ",affair.bodyType from EdocSummary as summary, CtpAffair as affair"
                + " where (affair.objectId=summary.id) and (affair.memberId=?)"
                //+ " and (affair.state=" + state.key() + ")"
                + " and (affair.state in (" + StateEnum.col_done.getKey() + "," + StateEnum.col_pending.getKey() + "," + StateEnum.col_sent.getKey() + ") )"
                + " and (affair.app=?)"
                + " and affair.delete=false"
                //310sp2:不根据文件的密级限制查询权限。muj
                // + " and (summary.hasArchive=false or (summary.hasArchive=true and (summary.secretLevel='1' or summary.secretLevel='5' or summary.secretLevel is null)))"//不读取归档密级不是普通的
                + " and summary.state <> ? ";//不读取公文归档后删除。
        //+ " and affair.archiveId is null";
        objects.add(curUserId);
        objects.add(EdocUtil.getAppCategoryByEdocType(em.getEdocType()).key());
        objects.add(CollaborationEnum.flowState.deleted.ordinal());

        if (!Strings.isBlank(em.getSubject())) {
            hql += " and (summary.subject like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getSubject()) + "%");
        }
        if (!Strings.isBlank(em.getDocMark())) {
            hql += " and (summary.docMark like ? or summary.docMark2 like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
            objects.add("%" + SQLWildcardUtil.escape(em.getDocMark()) + "%");
        }
        if (!Strings.isBlank(em.getSerialNo())) {
            hql += " and (summary.serialNo like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getSerialNo()) + "%");
        }
        if (!Strings.isBlank(em.getKeywords())) {
            hql += " and (summary.keywords like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getKeywords()) + "%");
        }
        if (!Strings.isBlank(em.getDocType())) {
            hql += " and (summary.docType = ?)";
            objects.add(em.getDocType());
        }
        if (!Strings.isBlank(em.getSendType())) {
            hql += " and (summary.sendType = ?)";
            objects.add(em.getSendType());
        }
        /*
         * wangwei start
         */
        //党务机关类型
        if (!Strings.isBlank(em.getParty())) {
            hql += " and (summary.party = ?)";
            objects.add(em.getParty());
        }
        // 政务机关类型
        if (!Strings.isBlank(em.getAdministrative())) {
            hql += " and (summary.administrative= ?)";
            objects.add(em.getAdministrative());
        }
        /**
         * wangwei end
         */
        if (!Strings.isBlank(em.getCreatePerson())) {
            hql += " and (summary.createPerson like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getCreatePerson()) + "%");
        }
        if (em.getCreateTimeB() != null) {
            hql += " and (summary.createTime >= ?)";
            objects.add(Datetimes.getTodayFirstTime(em.getCreateTimeB()));
        }
        if (em.getCreateTimeE() != null) {
            hql += " and (summary.createTime <= ?)";
            objects.add(Datetimes.getTodayLastTime(em.getCreateTimeE()));
        }
        if (!Strings.isBlank(em.getSendToId())) {
            hql += " and (summary.sendToId like ? or summary.sendToId2 like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
            objects.add("%" + SQLWildcardUtil.escape(em.getSendToId()) + "%");
        }
        if (!Strings.isBlank(em.getSendUnit())) {
            hql += " and (summary.sendUnit like ? or summary.sendUnit2 like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
            objects.add("%" + SQLWildcardUtil.escape(em.getSendUnit()) + "%");
        }
        if (!Strings.isBlank(em.getIssuer())) {
            hql += " and (summary.issuer like ?)";
            objects.add("%" + SQLWildcardUtil.escape(em.getIssuer()) + "%");
        }
        if (em.getSigningDateA() != null) {
            hql += " and (summary.signingDate >= ?)";
            objects.add(Datetimes.getTodayFirstTime(em.getSigningDateA()));
        }
        if (em.getSigningDateB() != null) {
            hql += " and (summary.signingDate <= ?)";
            objects.add(Datetimes.getTodayLastTime(em.getSigningDateB()));
        }

        String selectHql = hql + " order by summary.createTime desc";

        List result = edocSummaryDao.find(selectHql, "summary.id", true, null, objects);

        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object[] object = (Object[]) result.get(i);
                //Affair affair = new Affair();
                EdocSummary summary = new EdocSummary();
                make(object, summary, false);
                summary.setEdocType(em.getEdocType());

                try {
                    V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                    summary.setStartMember(member);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }

                //开始组装最后返回的结果
                EdocSummaryModel model = new EdocSummaryModel();

                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setWorkitemId(summary.getId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                //model.setAffairId(affair.getId());
                model.setBodyType((String) object[object.length - 1]);

                /*
                //协同状
                Integer sub_state = affair.getState();
                if (sub_state != null) {
                    model.setState(sub_state.intValue());
                }

                //是否跟踪
                Boolean isTrack = affair.getIsTrack();
                if (isTrack != null) {
                    model.setIsTrack(isTrack.booleanValue());
                }
                */
                models.add(model);
            }
        }

        return models;
    }


    //lijl添加此方法,修改重要G6BUG_G6_v1.0_徐州财政局_收文按照岗位加签，报错_20120912012962
    private List<V3xOrgEntity> getDepartmentPost(String typeAndIds)
            throws BusinessException {
        if (Strings.isBlank(typeAndIds)) {
            return null;
        }
        List<V3xOrgEntity> list = new ArrayList<V3xOrgEntity>();
        String[] items = typeAndIds.split(V3xOrgEntity.ORG_ID_DELIMITER);
        for (String item : items) {
            String postId = "";
            Long departId = 0l;
            if (item.contains("Department_Post|") == true) {
                String[] posts = item.split("_");
                if (posts != null && posts.length > 0) {
                    String[] departs = posts[1].split("\\|");
                    if (departs[1] != null) {
                        departId = Long.parseLong(departs[1]);
                    }
                    postId = "Post|" + posts[2];
                }
            } else {
                postId = item;
            }
            V3xOrgEntity entity = orgManager.getEntity(postId);
            V3xOrgEntity VOE = null;
            if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(entity.getEntityType())) {
                VOE = new V3xOrgMember();
            } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(entity.getEntityType())) {
                VOE = new V3xOrgDepartment();
            } else if (V3xOrgEntity.ORGENT_TYPE_TEAM.equals(entity.getEntityType())) {
                VOE = new V3xOrgTeam();
            } else if (V3xOrgEntity.ORGENT_TYPE_POST.equals(entity.getEntityType())) {
                VOE = new V3xOrgPost();
            }
            BeanUtils.copyProperties(entity, VOE);
            if (departId != 0l) {
                V3xOrgDepartment vom = orgManager.getDepartmentById(departId);
                VOE.setName(vom.getName() + "-" + VOE.getName());
            }
            if (entity != null) {
                list.add(VOE);
            }
        }

        return list;
    }

    public List<MoreSignSelectPerson> findMoreSignPersons(String typeAndIds) {
        List<MoreSignSelectPerson> msps = new ArrayList<MoreSignSelectPerson>();
        try {
            StringBuilder buffer = new StringBuilder();
            List<Long> deptIds = new ArrayList<Long>();
            String[] typeIds = typeAndIds.split(",");
            for (int i = 0; i < typeIds.length; i++) {
                String str = "";
                if (typeIds[i].split("\\|").length == 3) {
                    deptIds.add(Long.parseLong((typeIds[i].split("\\|")[1])));
                    str = typeIds[i].substring(0, typeIds[i].length() - 2);
                } else {
                    str = typeIds[i];
                }
                buffer.append(str + ",");
            }
            typeAndIds = buffer.toString();
            if (!"".equals(typeAndIds)) {
                typeAndIds = typeAndIds.substring(0, typeAndIds.length() - 1);
            }

            List<V3xOrgEntity> ents = getDepartmentPost(typeAndIds);

            for (V3xOrgEntity ent : ents) {
                MoreSignSelectPerson msp = new MoreSignSelectPerson();
                msp.setSelObj(ent);
                List<V3xOrgMember> selPersons = new ArrayList<V3xOrgMember>();
                if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(ent.getEntityType())) {
                    selPersons.add((V3xOrgMember) ent);
                } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(ent.getEntityType())) {
                    //GOV-4809.【公文管理-加签】加签一个部门，执行模式本来是存在的。但是在节点权限那里选择知会，执行模式不在了，然后再切换回审批，执行模式还是不存在 start
                    boolean excludeChildDepartment = false;
                    if (deptIds.contains(ent.getId())) {
                        excludeChildDepartment = true;
                    }
                    List<V3xOrgMember> deptMemberList = orgManager.getMembersByDepartment(ent.getId(), excludeChildDepartment);
                    selPersons.addAll(deptMemberList);
                    //GOV-4809.【公文管理-加签】加签一个部门，执行模式本来是存在的。但是在节点权限那里选择知会，执行模式不在了，然后再切换回审批，执行模式还是不存在 end
                } else if (V3xOrgEntity.ORGENT_TYPE_TEAM.equals(ent.getEntityType())) {
                    List<V3xOrgMember> teamMemberList = orgManager.getMembersByTeam(ent.getId());
                    selPersons.addAll(teamMemberList);
                } else if (V3xOrgEntity.ORGENT_TYPE_POST.equals(ent.getEntityType())) {
                    List<V3xOrgMember> postMemberList = orgManager.getMembersByPost(ent.getId());
                    selPersons.addAll(postMemberList);
                }
                msp.setSelPersons(selPersons);
                msps.add(msp);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return msps;

    }

    public void recoidChangeWords(String affairIds, String summaryIds, String changeType, String userId) {
        if (affairIds == null || "".equals(affairIds)
                || summaryIds == null || "".equals(summaryIds))
            return;
        String[] affairId = affairIds.split(",");
        String[] summaryId = summaryIds.split(",");
        try {
            for (int i = 0; i < affairId.length; i++) {
                recoidChangeWord(affairId[i], summaryId[i], changeType, userId);
            }
        } catch (Exception e) {
            LOGGER.error("记录修日志出错:", e);
        }
    }

    public void recoidChangeWord(String affairId, String summaryId, String changeType, String userId) {
        if (affairId == null || "".equals(affairId) ||
                summaryId == null || "".equals(summaryId)
                || changeType == null || "".equals(changeType)) {
            return;
        }
        try {
            User user = AppContext.getCurrentUser();
            if (user == null) {
                user = new User();
                user.setId(Long.valueOf(userId));
            }
            CtpAffair affair = affairManager.get(Long.valueOf(affairId));

            //记录修改正文的日志-------------------changyi add
            appLogManager.insertLog(user, AppLogAction.Edoc_Content_update, user.getName(), affair.getSubject());

            BPMActivity bPMActivity = EdocHelper.getBPMActivityByAffair(affair);
            EdocSummary summary = edocSummaryDao.get(Long.valueOf(summaryId));
            String[] changeWords = null;
            if (changeType.contains(",")) {
                changeWords = changeType.split(",");
            } else {
                changeWords = new String[1];
                changeWords[0] = changeType;
            }
            if (changeWords != null && changeWords.length > 0) {
                for (int i = 0; i < changeWords.length; i++) {
                    if ("contentUpdate".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.modifyBody.getKey()));

                        /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
                        //XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
                        //boolean isOpen;
                        //try {
                        //isOpen = xkjtManager.isOpenBySender(affair.getId());
                        //if(isOpen){
                        EdocMessageHelper.saveBodyMessage(affairManager, userMessageManager, orgManager, summary);
                        //}
                        //} catch (BusinessException e) {
                        // TODO Auto-generated catch block
                        //LOGGER.error("获取是否发送消息失败",e);
                        //}
                        /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 end*/
                    } else if ("taohong".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.Body.getKey()));
                    } else if ("qianzhang".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.signed.getKey()));
                    } else if ("taohongwendan".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.bodyFromRed.getKey()));
                    } else if ("depPinghole".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.depHigeonhole.getKey()));
                    } else if ("duban".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.duban.getKey()));
                    } else if ("wendanqianp".equals(changeWords[i])) {
                        this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.wendanqianp.getKey()));
                    } else if ("isLoadNewFile".equals(changeWords[i])) {//记录修改正文并且导入新的文件的操作的应用日志
                        appLogManager.insertLog(user, AppLogAction.Edoc_Content_Edit_LoadNewFile, user.getName(), affair.getSubject());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("记录修改正文时候出错:", e);
        }
    }


    /**
     * ajax请求：已发中修改公文附件
     */
    public void updateAttachment(EdocSummary edocSummary, CtpAffair cAffair, User user, HttpServletRequest request) throws Exception {
        //添加新的正文附件
        attachmentManager.create(ApplicationCategoryEnum.edoc, edocSummary.getId(), edocSummary.getId());
        //修改公文附件
        AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
        if (editHelper.hasEditAtt()) {//是否修改附件
            //设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
            EdocHelper.updateAttIdentifier(edocSummary, editHelper.parseProcessLog(Long.parseLong(edocSummary.getProcessId()), -1l), true, "updateAtt");
        }
    }

    /* public void saveUpdateAttInfo(int attSize,Long summaryId,List<ProcessLog> logs){
    	try {
			EdocSummary edocSummary = getEdocSummaryById(summaryId, false);
			boolean needUpdate = false;
			boolean hasAtt = attSize !=0;
			if(edocSummary.isHasAttachments() && !hasAtt){
				needUpdate = true;
			}else if(!edocSummary.isHasAttachments() && hasAtt){
				needUpdate = true;
			}
			if(needUpdate){
				edocSummary.setHasAttachments(hasAtt);

				Map<String,Object> p = new HashMap<String,Object>();
				p.put("identifier", edocSummary.getIdentifier());
				update(edocSummary.getId(),p);

				CtpAffair affair = new CtpAffair();
				//affair.setHasAttachments(hasAtt);
				Map<String,Object> parameter = new HashMap<String,Object>();
				parameter.put("identifier", affair.getIdentifier());

				//affairManager.updateAllAvailabilityAffair(EdocUtil.getAppCategoryByEdocType(edocSummary.getEdocType()),
				//		edocSummary.getId(), parameter);
			}
		//	processLogManager.insertLog(logs);
			//EdocMessageHelper.updateAttachmentMessage(affairManager, userMessageManager, orgManager, edocSummary);
		} catch (Exception e) {
			log.error("更新附件信息",e);
		}

    }*/
    public String getShowArchiveNameByArchiveId(Long archiveId) {
        //预归档
        String archiveName = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "prep-pigeonhole.label.no");

        if (archiveId != null) {

            try {
                DocResourceBO doc = null;
                String frName = "";
                if (AppContext.hasPlugin("doc")) {
                    doc = docApi.getDocResource(archiveId);
                    frName = docApi.getDocResourceName(archiveId);
                }
                if (doc != null) {
                    if (doc.getLogicalPath().split("\\.").length > 1) {
                        archiveName = com.seeyon.v3x.edoc.util.Constants.Edoc_PAGE_SHOWPIGEONHOLE_SYMBOL + "\\" + frName;
                    } else {
                        archiveName = frName;
                    }
                }
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }

        }
        return archiveName;
    }

    /**
     * 得到预归档目录名： 根文件夹名/xxx/yyy/最好一个文件夹名
     *
     * @param archiveId 归档路径ID
     * @return
     */
    public String getFullArchiveNameByArchiveId(Long archiveId) {
        //预归档
        String archiveName = "";
        if (archiveId != null) {
            try {
                DocResourceBO dr = null;
                if (AppContext.hasPlugin("doc")) {
                    dr = docApi.getDocResource(archiveId);
                }
                if (dr != null) {
                    archiveName = docApi.getPhysicalPath(dr.getLogicalPath(), "\\", false, 0);
                }
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }

        }
        return archiveName;
    }

    public String checkHasAclNodePolicyOperation(String affairIds, String operationName) throws BusinessException {
        StringBuilder ret = new StringBuilder("ok");
        StringBuilder notArchiveAclAffairIds = new StringBuilder();
        if (affairIds != null) {
            String[] affairId = affairIds.split(",");
            for (int i = 0; i < affairId.length; i++) {
                Long _affairId = Long.parseLong(affairId[i]);
                CtpAffair affair = affairManager.get(_affairId);
                try {

                    EdocSummary summary = getEdocSummaryById(affair.getObjectId(), false);
                    EnumNameEnum edocTypeEnum = EdocUtil.getEdocMetadataNameEnumByApp(affair.getApp());
                    String nodePermissionPolicy = getPolicyByAffair(affair, summary.getProcessId());

                    V3xOrgMember sender = orgManager.getMemberById(summary.getStartUserId());
                    Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, sender.getOrgAccountId());

                    List<String> advancedActions = permissionManager.getAdvaceActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);

                    if (advancedActions != null && advancedActions.contains(operationName)) continue;

                    List<String> commonActions = permissionManager.getCommonActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
                    if (commonActions != null && commonActions.contains(operationName)) continue;

                    List<String> basicActions = permissionManager.getBasicActionList(edocTypeEnum.name(), nodePermissionPolicy, flowPermAccountId);
                    if (basicActions != null && basicActions.contains(operationName)) continue;

                    /* 王为暂时屏蔽该项，直接赋予归档权限 */
                    if ("ok".equals(ret.toString())) {
                        ret = new StringBuilder();
                        ret.append("《").append(summary.getSubject()).append("》");
                    } else {
                        ret.append(",《").append(summary.getSubject()).append("》");
                    }
                    if (notArchiveAclAffairIds.length() > 0) {
                        notArchiveAclAffairIds.append(",").append(_affairId);
                    } else {
                        notArchiveAclAffairIds.append(_affairId);
                    }

                } catch (Exception e) {
                    LOGGER.error("ajax判断归档权限：", e);
                }
            }
        }
        if (!"ok".equals(ret.toString()))
            ret.append("&").append(notArchiveAclAffairIds);
        return ret.toString();
    }

    public String checkIsCanBeRepealed(String summaryId4Check) {
        boolean isFinished = false;
        String result = "";
        StringBuilder info = new StringBuilder();
        try {
            EdocSummary summary = this.getEdocSummaryById(Long
                    .parseLong(summaryId4Check), false);
            isFinished = summary.getFinished();
            if (isFinished) {
                info.append("《").append(summary.getSubject()).append("》");
                result = ResourceBundleUtil.getString(
                        "com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.state.end.alert", info.toString());
            } else {
                result = "Y";
            }
        } catch (EdocException e) {
            LOGGER.error("ajax判断是否流程结束异常：", e);
        }
        return result;
    }

    /**
     * 异步判断已发公文,注意，是已发公文，是否可以被取回
     *
     * @param summaryId4Check 公文id
     * @return msgInfo(edoc.state.end.alert) ；Y 可以撤销
     * @throws BusinessException
     */
    public String checkIsCanBeTakeBack(String summaryId4Check) throws BusinessException {
        boolean isFinished = false;
        String result = "";
        StringBuilder info = new StringBuilder();
        try {
            EdocSummary summary = this.getEdocSummaryById(Long
                    .parseLong(summaryId4Check), false);

            //判断下一结点是否已被看过或者处理过
            List<CtpAffair> affairList = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summary.getId());
            if (affairList != null && affairList.size() > 0) {
                for (int i = 0; i < affairList.size(); i++) {
                    CtpAffair affair = affairList.get(i);
                    int state = (affair.getState() == null ? 0 : affair.getState());
                    int subState = (affair.getSubState() == null ? 0 : affair.getSubState());
                    //不能取回的有：大于等于已办，待办中的大于未读状态。(排除state等于5，这里可以反复的取回和发送)
                    if (state >= StateEnum.col_done.getKey() && state != StateEnum.col_cancel.getKey() ||
                            (state == StateEnum.col_pending.getKey() && subState > SubStateEnum.col_pending_unRead.getKey())) {
                        info.append("《").append(summary.getSubject()).append("》");
                        result = ResourceBundleUtil.getString(
                                "com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "edoc.state.cannottake.alert", info.toString());
                        return result;
                    }
                }

            }

            //判断流程是否结束
            isFinished = summary.getFinished();
            if (isFinished) {
                info.append("《").append(summary.getSubject()).append("》");
                result = ResourceBundleUtil.getString(
                        "com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.state.end.alert", info.toString());
            } else {
                result = "Y";
            }
        } catch (EdocException e) {
            LOGGER.error("ajax判断是否流程结束异常：", e);
        }
        return result;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public EdocMarkManager getEdocMarkManager() {
        return edocMarkManager;
    }

    public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
        this.edocMarkManager = edocMarkManager;
    }

    public EdocMarkHistoryManager getEdocMarkHistoryManager() {
        return edocMarkHistoryManager;
    }

    public void setEdocMarkHistoryManager(
            EdocMarkHistoryManager edocMarkHistoryManager) {
        this.edocMarkHistoryManager = edocMarkHistoryManager;
    }

    public List<CtpTrackMember> getColTrackMembersByObjectIdAndTrackMemberId(Long objectId, Long trackMemberId) throws BusinessException {
        return trackdao.getTrackMembersByObjectIdAndTrackMemberId(objectId, trackMemberId);
    }

    public void deleteColTrackMembersByObjectId(Long objectId) {
        //colTrackMemberDao.deleteColTrackMembersByObjectId(objectId);
    }

    private List<EdocOpinion> getEdocOpinionObjectList(EdocSummary summary, boolean isOnlyShowLastOpinion) {
        // 取得公文单的意见元素的绑定关系，key是FlowPermName，value是公文元素名称为value_公文元素的排序方式
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);
        Hashtable<String, String> locHs = edocFormManager.getOpinionLocation(summary.getFormId(), flowPermAccout);

        List<Attachment> tempAtts = attachmentManager.getByReference(summary.getId());
        Hashtable<Long, List<Attachment>> attHas = com.seeyon.ctp.common.filemanager.manager.Util.sortBySubreference(tempAtts);


        List<EdocOpinion> tempResult = new ArrayList<EdocOpinion>();   //查询出来的意见
        List<String> boundFlowPerm = new ArrayList<String>();   //绑定的节点权限
        //Map<意见元素名称，List<绑定的节点权限>>因为一个意见元素可以绑定多个节点权限
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        Map<String, String> sortMap = new HashMap<String, String>();
        //绑定部分的意见
        for (Iterator keyName = locHs.keySet().iterator(); keyName.hasNext(); ) {
            String flowPermName = (String) keyName.next();
            if (!boundFlowPerm.contains(flowPermName)) boundFlowPerm.add(flowPermName);
            // tempLoacl格式 公文元素名称为value_公文元素的排序方式
            String tempLocal = locHs.get(flowPermName);
            String elementOpinion = tempLocal.split("_")[0];//公文元素名,例如公文单上的shenpi这个公文元素
            //取到指定公文元素绑定的节点权限列表
            List<String> flowPermsOfSpecialElement = map.get(elementOpinion);
            if (flowPermsOfSpecialElement == null) {
                flowPermsOfSpecialElement = new ArrayList<String>();
            }
            flowPermsOfSpecialElement.add(flowPermName);
            map.put(elementOpinion, flowPermsOfSpecialElement);

            // 公文元素绑定的排序方式
            String sortType = tempLocal.split("_")[1];
            sortMap.put(elementOpinion, sortType);
        }

        boolean hasFeedBack = false;
        List<EdocElement> feedbackList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), EdocOpinion.FEED_BACK);
        if (Strings.isNotEmpty(feedbackList)) {
            hasFeedBack = true;
        }

        if (hasFeedBack) {
            List<String> feedbackPolicy = new ArrayList<String>(1);
            feedbackPolicy.add(EdocOpinion.FEED_BACK);
            map.put(EdocOpinion.FEED_BACK, feedbackPolicy);
            if (!boundFlowPerm.contains(EdocOpinion.FEED_BACK)) {
                boundFlowPerm.add(EdocOpinion.FEED_BACK);
            }
        }

        Set<String> bound = map.keySet(); //绑定的公文元素
        for (String s : bound) {
            tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
                    map.get(s), sortMap.get(s), isOnlyShowLastOpinion, true));
        }
        //查询非绑定意见
        tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
                boundFlowPerm, "0", isOnlyShowLastOpinion, false));

        List<EdocOpinion> edocOpionionList = new ArrayList<EdocOpinion>();

        for (EdocOpinion edocOpinion : tempResult) {
            edocOpionionList.add(edocOpinion);

            edocOpinion.setOpinionAttachments(attHas.get(edocOpinion.getId()));
        }
        return edocOpionionList;
    }

    private Map<String, Object> getEdocOpinionObjectList(EdocSummary summary, FormOpinionConfig displayConfig) {
        Map<String, Object> edocOpinionMap = new HashMap<String, Object>();
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);
        Hashtable<String, String> locHs = edocFormManager.getOpinionLocation(summary.getFormId(), flowPermAccout);
        edocOpinionMap.put("locHs", locHs);

        List<Attachment> tempAtts = attachmentManager.getByReference(summary.getId());
        Hashtable<Long, List<Attachment>> attHas = com.seeyon.ctp.common.filemanager.manager.Util.sortBySubreference(tempAtts);

        Long formAccountId = summary.getOrgAccountId();
        if (summary.getTempleteId() != null) {
            try {
                CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                if (template != null && (template.isSystem() || template.getFormParentid() != null) && template.getOrgAccountId() != summary.getOrgAccountId()) {
                    formAccountId = flowPermAccout;
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        List<EdocFormFlowPermBound> bList = edocFormFlowPermBoundDao.findBoundProcessByFormId(summary.getFormId(), formAccountId);
        Set<String> eList = new HashSet<String>();
        for (EdocFormFlowPermBound b : bList) {
            eList.add(b.getProcessName());
        }
        List<EdocOpinion> tempResult = new ArrayList<EdocOpinion>();   //查询出来的意见
        List<String> boundFlowPerm = new ArrayList<String>();   //绑定的节点权限
        //Map<意见元素名称，List<绑定的节点权限>>因为一个意见元素可以绑定多个节点权限
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        Map<String, String> sortMap = new HashMap<String, String>();
        //绑定部分的意见
        for (Iterator keyName = locHs.keySet().iterator(); keyName.hasNext(); ) {
            String flowPermName = (String) keyName.next();
            if (!boundFlowPerm.contains(flowPermName)) boundFlowPerm.add(flowPermName);
            // tempLoacl格式 公文元素名称为value_公文元素的排序方式
            String tempLocal = locHs.get(flowPermName);
            String elementOpinion = tempLocal.split("_")[0];//公文元素名,例如公文单上的shenpi这个公文元素
            //取到指定公文元素绑定的节点权限列表
            List<String> flowPermsOfSpecialElement = map.get(elementOpinion);
            if (flowPermsOfSpecialElement == null) {
                flowPermsOfSpecialElement = new ArrayList<String>();
            }
            flowPermsOfSpecialElement.add(flowPermName);

            for (String e : eList) {
                if (Strings.isNotBlank(e)) {
                    if (e.equals(elementOpinion)) {
                        map.put(elementOpinion, flowPermsOfSpecialElement);
                        break;
                    }
                }
            }
            String sortType = tempLocal.split("_")[1];
            sortMap.put(elementOpinion, sortType);
        }

        //查询文单是否包含其他意见元素
        boolean hasOtherOpinion = false;
        boolean hasFeedBack = false;

        List<EdocElement> elementList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), "otherOpinion");
        if (Strings.isNotEmpty(elementList)) {
            hasOtherOpinion = true;
        }
        List<EdocElement> feedbackList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), EdocOpinion.FEED_BACK);
        if (Strings.isNotEmpty(feedbackList)) {
            hasFeedBack = true;
        }

        if (hasFeedBack) {
            List<String> feedbackPolicy = new ArrayList<String>(1);
            feedbackPolicy.add(EdocOpinion.FEED_BACK);
            map.put(EdocOpinion.FEED_BACK, feedbackPolicy);
            if (!boundFlowPerm.contains(EdocOpinion.FEED_BACK)) {
                boundFlowPerm.add(EdocOpinion.FEED_BACK);
            }
        }

        Set<String> bound = map.keySet(); //绑定的公文元素
        boolean isShowLast = OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(displayConfig.getOpinionType());

        if (eList.size() > 0) {
            for (String s : bound) {
                List<EdocOpinion> opinions = edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(), map.get(s), sortMap.get(s), isShowLast, true, displayConfig.getOpinionType());
                tempResult.addAll(opinions);
            }
        }

        String defaultSortType = sortMap.get("otherOpinion") == null ? "0" : sortMap.get("otherOpinion");
        //未绑定的意见全部显示
        List<EdocOpinion> opinions = edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(), boundFlowPerm, defaultSortType, false, false, displayConfig.getOpinionType());
        //将下级汇报意见取出来
        List<EdocOpinion> feedbacks = new ArrayList<EdocOpinion>();
        List<EdocOpinion> others = new ArrayList<EdocOpinion>();

        for (EdocOpinion op : opinions) {
			/*if(EdocOpinion.FEED_BACK.equals(op.getPolicy())){
				op.setBound(true);
				feedbacks.add(op);
			}else{*/
            op.setBound(hasOtherOpinion || false);//非绑定意见
            others.add(op);
            /*}*/
        }
        List<EdocOpinion> opinions2 = new ArrayList<EdocOpinion>();
        if (Strings.isNotEmpty(others)) {
            opinions2.addAll(others);
        }
        if (Strings.isNotEmpty(feedbacks)) {
            feedbacks = EdocHelper.getFeedBackOptions(feedbacks);
            opinions2.addAll(feedbacks);
        }

        tempResult.addAll(opinions2);
        List<EdocOpinion> edocOpionionList = new ArrayList<EdocOpinion>();
        for (EdocOpinion edocOpinion : tempResult) {
            edocOpionionList.add(edocOpinion);

            edocOpinion.setOpinionAttachments(attHas.get(edocOpinion.getId()));
        }
        /**项目：徐矿集团  【屏蔽公文中的意见】 作者：jiangchenxi 时间：2019年3月12日 start*/
        try {
            //项目：修复公文待办指定公开无法看到意见 zelda 2019年12月21日16:36:53 start
            boolean open = xkjtManager.isOpen(summary.getAffairId());
            if (!open) {
                edocOpionionList = xkjtManager.getOpenOpinions(edocOpionionList, 1);
            }
            //项目：修复公文待办指定公开无法看到意见 zelda 2019年12月21日16:36:53 end
        } catch (Exception e) {
            LOGGER.error("zelda-校验指定公开错误", e);
        }

        /**项目：徐矿集团  【屏蔽公文中的意见】 作者：jiangchenxi 时间：2019年3月12日 start*/
        edocOpinionMap.put("edocOpinionList", edocOpionionList);
        return edocOpinionMap;
    }


    /**
     * lijl重写上边方法
     */
    private Map<String, Object> getEdocOpinionObjectList(EdocSummary summary, boolean isOnlyShowLastOpinion, String optionType) {
        //--------公文查看性能优化
        Map<String, Object> edocOpinionMap = new HashMap<String, Object>();

        // 取得公文单的意见元素的绑定关系，key是FlowPermName，value是公文元素名称为value_公文元素的排序方式
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);
        Hashtable<String, String> locHs = edocFormManager.getOpinionLocation(summary.getFormId(), flowPermAccout);
        edocOpinionMap.put("locHs", locHs);

        List<Attachment> tempAtts = attachmentManager.getByReference(summary.getId());
        Hashtable<Long, List<Attachment>> attHas = com.seeyon.ctp.common.filemanager.manager.Util.sortBySubreference(tempAtts);


        //性能优化代码--start
        //先把意见查出来，获得需要填充的意见栏有哪些。不需要填充的，下面的查询意见及排序完全没必要去查。
        //OA-37231 当文单意见框绑定超过一个节点权限时，处理节点填写意见后提交，意见就不显示
        Long formAccountId = summary.getOrgAccountId();
        if (summary.getTempleteId() != null) {
            try {
                CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                if (template != null && (template.isSystem() || template.getFormParentid() != null) && template.getOrgAccountId() != summary.getOrgAccountId()) {
                    formAccountId = flowPermAccout;
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        List<EdocFormFlowPermBound> bList = edocFormFlowPermBoundDao.findBoundProcessByFormId(
                //这里文单绑定的单位 要取公文summary的单位，而不能取文单的制作单位，因为文单是可以授权给外单位的，外单位有自己的一套文单设置
                summary.getFormId(), formAccountId);
        Set<String> eList = new HashSet<String>();
        for (EdocFormFlowPermBound b : bList) {
            eList.add(b.getProcessName());
        }

        //性能优化代码--end

        List<EdocOpinion> tempResult = new ArrayList<EdocOpinion>();   //查询出来的意见
        List<String> boundFlowPerm = new ArrayList<String>();   //绑定的节点权限
        //Map<意见元素名称，List<绑定的节点权限>>因为一个意见元素可以绑定多个节点权限
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        Map<String, String> sortMap = new HashMap<String, String>();
        //绑定部分的意见
        for (Iterator keyName = locHs.keySet().iterator(); keyName.hasNext(); ) {
            String flowPermName = (String) keyName.next();
            if (!boundFlowPerm.contains(flowPermName)) boundFlowPerm.add(flowPermName);
            // tempLoacl格式 公文元素名称为value_公文元素的排序方式
            String tempLocal = locHs.get(flowPermName);
            String elementOpinion = tempLocal.split("_")[0];//公文元素名,例如公文单上的shenpi这个公文元素
            //取到指定公文元素绑定的节点权限列表
            List<String> flowPermsOfSpecialElement = map.get(elementOpinion);
            if (flowPermsOfSpecialElement == null) {
                flowPermsOfSpecialElement = new ArrayList<String>();
            }
            flowPermsOfSpecialElement.add(flowPermName);

            //性能优化代码--start
            for (String e : eList) {
                if (Strings.isNotBlank(e)) {
                    if (e.equals(elementOpinion)) {
                        map.put(elementOpinion, flowPermsOfSpecialElement);
                        break;
                    }
                }
            }
            //性能优化代码--end

//			map.put(elementOpinion,flowPermsOfSpecialElement);

            // 公文元素绑定的排序方式
            String sortType = tempLocal.split("_")[1];
            sortMap.put(elementOpinion, sortType);
        }

        Set<String> bound = map.keySet(); //绑定的公文元素


        if (eList.size() > 0) {
            for (String s : bound) {
                tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
                        map.get(s), sortMap.get(s), isOnlyShowLastOpinion, true, optionType));
            }


        }

        //查询非绑定意见
        // 非绑定意见一样需要动态设定意见是全部显示，还是显示最后一条 所以需要传参数isOnlyShowLastOpinion
//		tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
//				boundFlowPerm, "0", isOnlyShowLastOpinion,false,optionType));
        //OA-33107 test01既是发起人也是处理时，他处理时增加附言后，再次增加附言，第二次增加的附言会覆盖第一次的，他在已发中增加附言后又会覆盖之前的
        //非绑定意见显示全部意见
        tempResult.addAll(edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(),
                boundFlowPerm, "0", isOnlyShowLastOpinion, false, optionType));


        List<EdocOpinion> edocOpionionList = new ArrayList<EdocOpinion>();

        for (EdocOpinion edocOpinion : tempResult) {
            edocOpionionList.add(edocOpinion);

            edocOpinion.setOpinionAttachments(attHas.get(edocOpinion.getId()));
        }
        edocOpinionMap.put("edocOpinionList", edocOpionionList);
//		return edocOpionionList;
        return edocOpinionMap;
    }

    /*********************************** 唐桂林 公文意见显示 start *************************************/
    /**
     * 公文处理意见回显到公文单,排序
     *
     * @param summary
     * @return
     */
    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary) {
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);
        FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(), flowPermAccout);
        return getEdocOpinion(summary, OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(displayConfig.getOpinionType()));
    }

    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary, boolean isOnlyShowLastOpinion) {
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);
        Hashtable<String, String> opinionLocation = edocFormManager.getOpinionLocation(summary.getFormId(), flowPermAccout);
        /** 意见排序 **/
        List<EdocOpinion> list = getEdocOpinionObjectList(summary, isOnlyShowLastOpinion);
        return getEdocOpinion(list, summary, opinionLocation);
    }

    @SuppressWarnings("unchecked")
    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary, boolean isOnlyShowLastOpinion, String optionType) {
        Map<String, Object> map = getEdocOpinionObjectList(summary, isOnlyShowLastOpinion, optionType);
        Hashtable<String, String> locHs = (Hashtable<String, String>) map.get("locHs");
        SharedWithThreadLocal.setLocHs(locHs);
        List<EdocOpinion> list = (List<EdocOpinion>) map.get("edocOpinionList");
        return getEdocOpinion(list, summary, locHs);
    }

    @SuppressWarnings("unchecked")
    public Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary summary, FormOpinionConfig displayConfig) {
        Map<String, Object> map = getEdocOpinionObjectList(summary, displayConfig);
        Hashtable<String, String> locHs = (Hashtable<String, String>) map.get("locHs");
        SharedWithThreadLocal.setLocHs(locHs);
        List<EdocOpinion> list = (List<EdocOpinion>) map.get("edocOpinionList");
        return getEdocOpinion(list, summary, locHs);
    }

    public Map<String, EdocOpinionModel> getEdocOpinion(List<EdocOpinion> edocOpinions, EdocSummary summary, Map<String, String> opinionLocation) {
        Long summaryId = summary.getId();
        Map<String, EdocOpinionModel> map = new HashMap<String, EdocOpinionModel>();
        Map<String, V3xHtmDocumentSignature> signMap = getEdocOpinionSignatureMap(summaryId);
        //拟文或者登记意见是否被绑定到意见框中显示了。
        for (EdocOpinion edocOpinion : edocOpinions) {
            //节点权限
            String policy = edocOpinion.getPolicy();
            if (policy == null) {
                policy = summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal() ? "dengji" : "niwen";
            }
            //公文元素_排序方式
            String location = opinionLocation.get(policy);

            //没有设置意见放置位置的，统一放到其它意见，不再按节点权限放置到前台匹配;发起人附言单独处理,如果没有设置绑定就不放入公文单
            if (Strings.isBlank(location)) {
                if ("niwen".equals(policy) || "dengji".equals(policy) || edocOpinion.getOpinionType() == EdocOpinion.OpinionType.senderOpinion.ordinal()) {
                    //发起人附言
                    location = "senderOpinion";
                } else if (policy.equals(EdocOpinion.FEED_BACK)) {
                    location = EdocOpinion.FEED_BACK;
                } else {
                    //其他意见。
                    location = "otherOpinion";
                }
            } else {
                location = location.split("[_]")[0];
            }
            EdocOpinionModel model = map.get(location);
            if (model == null) {
                model = new EdocOpinionModel();
            }
            if (model.getOpinions() == null) {
                model.setOpinions(new ArrayList<EdocOpinion>());
            }
            model.getOpinions().add(edocOpinion);

            //签章加载到意见框
            V3xHtmDocumentSignature v3xHtmDocumentSignature = signMap.get(location);
            List<V3xHtmDocumentSignature> signList = new ArrayList<V3xHtmDocumentSignature>();
            signList.add(v3xHtmDocumentSignature);
            model.setV3xHtmDocumentSignature(signList);

            map.put(location, model);
        }
        return map;
    }

    private Map<String, V3xHtmDocumentSignature> getEdocOpinionSignatureMap(Long summaryId) {
        //查找印章数据
        List<V3xHtmDocumentSignature> ls = htmSignetManager.findBySummaryIdAndType(summaryId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
        Map<String, V3xHtmDocumentSignature> signMap = new HashMap<String, V3xHtmDocumentSignature>();
        for (V3xHtmDocumentSignature htmlSign : ls) {
            String fieldName = htmlSign.getFieldName();
            if (Strings.isNotBlank(fieldName)) {
                String[] name = fieldName.split("hw");
                if (name.length > 1) {
                    signMap.put(name[1], htmlSign);
                }
            }
        }
        return signMap;
    }

    /*********************************** 唐桂林 公文意见显示 end *************************************/

    @Override
    public EdocBody getEdocBodyByFileid(long fileid) {
        DetachedCriteria criteria = DetachedCriteria.forClass(EdocBody.class);
        criteria.add(Restrictions.like("content", fileid + ""));
        return (EdocBody) edocOpinionDao.executeUniqueCriteria(criteria);
    }

    public void createZcdbQuartz(long affairId, java.util.Date remindTime) throws EdocException {
        Map<String, String> datamap = new HashMap<String, String>(3);
        datamap.put("objectId", String.valueOf(affairId));

        try {
            QuartzHolder.newQuartzJob("EdocZcdbRemind" + affairId, remindTime, "edocRemindQuartzJob", datamap);
        } catch (MutiQuartzJobNameException e) {
            LOGGER.error("新增公文暂存待办提醒job异常", e);
        } catch (NoSuchQuartzJobBeanException e) {
            LOGGER.error("新增公文暂存待办提醒job异常", e);
        }

    }

    public void deleteZcdbQuartz(long affairId, java.util.Date remindTime) {
        java.util.Date now = new java.util.Date();
        if (now.before(remindTime)) {
            QuartzHolder.deleteQuartzJob("EdocZcdbRemind" + affairId);
        }
    }

    /**
     * 自定义分类-根据时间枚举返回条件数组
     *
     * @param timeEnumId 时间枚举id EdocCustomerTypeTimeEnum
     * @return 数组，{0：textfiled，1：textfiled1}
     * @throws EdocException
     */
    public String[] getTimeTextFiledByTimeEnum(int timeEnumId) {
        String[] condition = new String[2];
        java.util.Date now = new java.util.Date();
        Calendar cd = Calendar.getInstance();

        switch (timeEnumId) {
            case 1: // (EdocCustomerTypeTimeEnum.DAY.getKey())
                condition[0] = Datetimes.formatDate(now);
                condition[1] = condition[0];
                break;
            case 2: // EdocCustomerTypeTimeEnum.YESTERDAY.getKey()
                cd.add(Calendar.DATE, -1);
                condition[0] = Datetimes.formatDate(cd.getTime());
                condition[1] = condition[0];
                break;
            case 3: // EdocCustomerTypeTimeEnum.WEEK.getKey()
                condition[0] = DateUtil.getMondayOFWeek();
                condition[1] = Datetimes.formatDate(now);
                break;
            case 4: // EdocCustomerTypeTimeEnum.LAST_WEEK.getKey()
                condition[0] = DateUtil.getPreviousWeekday();
                condition[1] = DateUtil.getPreviousWeekSunday();
                break;
            case 5: // EdocCustomerTypeTimeEnum.MONTH.getKey()
                condition[0] = DateUtil.getFirstDayOfMonth();
                condition[1] = Datetimes.formatDate(now);
                break;
            case 6: // EdocCustomerTypeTimeEnum.LAST_MONTH.getKey()
                condition[0] = DateUtil.getPreviousMonthFirst();
                condition[1] = DateUtil.getPreviousMonthEnd();
                break;
            case 7: // EdocCustomerTypeTimeEnum.YEAR.getKey()
                condition[0] = DateUtil.getCurrentYearFirst();
                condition[1] = Datetimes.formatDate(now);
                break;
            case 8: // EdocCustomerTypeTimeEnum.LAST_YEAR.getKey()
                condition[0] = DateUtil.getPreviousYearFirst();
                condition[1] = DateUtil.getPreviousYearEnd();
                break;
        }
        return condition;
    }

    //通过公文发文单位id和name查询出单位或部门全名
    public String getSendUnitFullName(int taohongSendUnitType, String sendUnit, String sendUnitId, long accountId) {
        if (sendUnit != null && sendUnitId != null) {
            try {
                String[] units = null;
                String[] unitIds = null;
                String[] unitTypes = null;
                StringBuilder sendUnitName = new StringBuilder();
                StringBuilder sendUnitFullName = new StringBuilder();

                unitIds = sendUnitId.split(",");
                units = sendUnit.split("、");
                for (int i = 0; i < unitIds.length; i++) {
                    unitTypes = unitIds[i].split("\\|");
                    if ("Department".equals(unitTypes[0])) {
                        if (taohongSendUnitType == 1) {
                            V3xOrgDepartment v3xOrgDepartment = orgManager.getDepartmentById(Long.parseLong(unitTypes[1]));
                            long orgAccountId = v3xOrgDepartment.getOrgAccountId();
                            if (accountId != orgAccountId) {
                                V3xOrgAccount v3xOrgAccount = orgManager.getAccountById(orgAccountId);
                                sendUnitName.append(v3xOrgAccount.getShortName() + orgManager.getDepartmentById(Long.parseLong(unitTypes[1])).getName());
                            } else {
                                sendUnitName.append(v3xOrgDepartment.getName());
                            }
                            sendUnitName.append(",");
                        } else {
                            //sendUnitFullName += orgManager.getParentDepartmentFullName(unitTypes[1], accountId);
                        }
                    } else if ("Account".equals(unitTypes[0])) {
                        if (taohongSendUnitType == 1) {
                            sendUnitName.append(units[i])
                                    .append(",");
                        } else {
                            sendUnitFullName.append(units[i])
                                    .append(",");
                        }
                    }
                }
                if (taohongSendUnitType == 1) {
                    if (sendUnitName.length() > 0) {
                        sendUnitName.deleteCharAt(sendUnitName.length() - 1);
                    }
                    return sendUnitName.toString();
                } else {
                    if (sendUnitFullName.length() > 0) {
                        sendUnitFullName.deleteCharAt(sendUnitFullName.length() - 1);
                    }
                    return sendUnitFullName.toString();
                }
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    /**
     * 判断是否签章
     */
    public boolean isHaveHtmlSign(String recordId) {
        try {
            List<V3xDocumentSignature> list = documentSignatureDao.findByRecordId(recordId);
            if (list == null || list.size() == 0) {
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("查找签章异常", e);
            return true;
        }
        return true;
    }

    /**
     * 组合查询
     *
     * @param edocType
     * @param edocSearchModel
     * @param state
     * @return List<EdocSummaryModel>
     */
    public List<EdocSummaryModel> combQueryByCondition(int edocType, EdocSearchModel em, int state, int... substateArray) {
        return combQueryByCondition(edocType, em, state, null, substateArray);
    }

    /**
     * 组合查询
     *
     * @param edocType
     * @param edocSearchModel
     * @param state
     * @return List<EdocSummaryModel>
     */
    public List<EdocSummaryModel> combQueryByCondition(int edocType, EdocSearchModel em, int state, Map<String, Object> params, int... substateArray) {

        StringBuilder exp0 = new StringBuilder();
        String paramName = null;
        String paramValue = null;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        //List<String> paramNameList = new ArrayList<String>();
        User user = AppContext.getCurrentUser();
        long user_id = user.getId();

        //获取代理相关信息
        List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(user_id);
        List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(user_id);
        List<AgentModel> agentModelList = null;
        boolean agentToFlag = false;
        boolean agentFlag = false;
        if (_agentModelList != null && !_agentModelList.isEmpty()) {
            agentModelList = _agentModelList;
            agentFlag = true;
        } else if (_agentModelToList != null && !_agentModelToList.isEmpty()) {
            agentModelList = _agentModelToList;
            agentToFlag = true;
        }
        //Map<Integer, AgentModel> agentModelMap = new HashMap<Integer, AgentModel>();
        List<AgentModel> edocAgent = new ArrayList<AgentModel>();
        if (agentModelList != null && !agentModelList.isEmpty()) {
            java.util.Date now = new java.util.Date();
            for (AgentModel agentModel : agentModelList) {
                if (agentModel.isHasEdoc() && agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now)) {
                    edocAgent.add(agentModel);
                }
            }
        }
        //boolean isProxy = false;
        if (Strings.isNotEmpty(edocAgent)) {
            //isProxy = true;
        } else {
            agentFlag = false;
            agentToFlag = false;
        }

        //在办表加入暂存代办提醒时间列
        String zcdbTimeSelect = "";
        String zcdbTable = "";

        StringBuilder hql = new StringBuilder();
        hql.append("select ");
        hql.append(selectAffair);
        hql.append(zcdbTimeSelect);
        hql.append(" from CtpAffair as affair,EdocSummary as summary");
        hql.append(zcdbTable);
        hql.append(" where ");
        /**
         * 收文管理 -- 成文单位,登记时间 cy add
         */
        if (!Strings.isBlank(em.getSendUnit()) || em.getRegisterDateB() != null || em.getRegisterDateE() != null) {
            hql = new StringBuilder("select ")
                    .append(selectAffair)
                    .append(zcdbTimeSelect)
                    .append(" from CtpAffair as affair,EdocSummary as summary,EdocRegister register ")
                    .append(zcdbTable)
                    .append(" where ");

        }
        /**
         * 收文管理  -- 登记时间
         * cy add
         */
//		if (em.getRegisterDateB()!=null ||em.getRegisterDateE()!=null) {
//			hql = "select "+selectAffair+zcdbTimeSelect+" from Affair as affair,EdocSummary as summary,EdocRegister register "+zcdbTable
//            + " where";
//        }

        /**
         * 收文管理  -- 签收时间
         * cy add
         */
        if (em.getRecieveDateB() != null || em.getRecieveDateE() != null) {
            hql = new StringBuilder("select ")
                    .append(selectAffair)
                    .append(zcdbTimeSelect)
                    .append(" from CtpAffair as affair,EdocSummary as summary,EdocRegister register,EdocRecieveRecord recieve ")
                    .append(zcdbTable)
                    .append(" where ");
        }

        if ("true".equals(params.get("deduplication"))) {
            hql.append(" affair.objectId = summary.id and ");
            hql.append(" affair.id in ( select max(affair.id) from CtpAffair as affair,EdocSummary as summary ");
            hql.append(zcdbTable);
            hql.append(" where 1=1 and ");
        }

        if (Strings.isNotEmpty(edocAgent)) {
            if (!agentToFlag) {
                hql.append("(");
                hql.append(" (affair.memberId=:user_id) ");
                parameterMap.put("user_id", user_id);
                if (state == StateEnum.col_pending.key() || state == StateEnum.col_done.key()) {
                    if (edocAgent != null && !edocAgent.isEmpty()) {
                        hql.append("   or (");
                        int i = 0;
                        for (AgentModel agent : edocAgent) {
                            if (i != 0) {
                                hql.append(" or ");
                            }
                            hql.append(" (affair.memberId=:edocAgentToId");
                            hql.append(i);
                            hql.append(" and affair.receiveTime>=:proxyCreateDate");
                            hql.append(i);
                            parameterMap.put("edocAgentToId" + i, agent.getAgentToId());
                            parameterMap.put("proxyCreateDate" + i, agent.getStartDate());
                            hql.append(" )");
                            i++;
                        }
                        hql.append("   )");
                    }
                }
                hql.append(")");
            } else {
                if (state == StateEnum.col_pending.key()) {
                    hql.append(" affair.memberId=:user_id1");
                    parameterMap.put("user_id1", user_id);
                } else {
                    hql.append(" affair.memberId=:user_id");
                    parameterMap.put("user_id", user_id);
                }
            }
        } else {
            hql.append(" (affair.memberId=:user_id )");
            parameterMap.put("user_id", user_id);
        }

        hql.append(" and affair.state=:a_state ");
        hql.append(" and affair.objectId=summary.id");
        hql.append(" and affair.delete=false ");

        parameterMap.put("a_state", state);

        //区分是收文办文(1)、收文阅文(2)、分发草稿箱(3)魏俊标
        int processType = (params == null || params.get("processType") == null) ? 0 : (Integer) params.get("processType");
        if ((substateArray.length == 3 || (processType != 0)) && edocType == 1) {
            hql.append(" and summary.processType=:s_processType ");
            if (substateArray.length == 3) {
                parameterMap.put("s_processType", Long.valueOf(substateArray[2]));
            } else {
                parameterMap.put("s_processType", Long.valueOf(processType));
            }
        }
        if (substateArray.length > 0) {
            int substate = substateArray[0];//子状态(v3x_affair的sub_state)

            if (state == StateEnum.col_pending.key()) { //待办时，如果子状态大于0表示加入子状态的查询条件
                if (substate >= 0) {

//            		hql+= (substate==SubStateEnum.col_pending_ZCDB.getKey()?" and edocZcdb.affairId =affair.id ":"")+ " and affair.subState=:sub_state "; //用于暂存待办，即在办

                    //GOV-3934  待办里没有数据！点击组合查询选择时间查询，查询出来了数据！ changyi
                    if (substate == SubStateEnum.col_pending_ZCDB.getKey()) {
                        hql.append(" and affair.subState=:sub_state ");//用于暂存待办，即在办
                        parameterMap.put("sub_state", substate);
                    }
                }
                //GOV-5120 【公文管理】-【收文管理】-【待阅】，组合查询时无法查出暂存待办的收文
                else if (processType != 2) {
                    hql.append(" and affair.subState<>:sub_state "); //用于待办,排除在办的
                    parameterMap.put("sub_state", SubStateEnum.col_pending_ZCDB.getKey());
                }
            } else if (state == StateEnum.col_waitSend.getKey()) {  //待发时，substateArray[0]保存subState，substateArray[3]保存等于还是不等于
                if (substateArray.length >= 4) {
                    hql.append(" and affair.subState");
                    hql.append((substateArray[3] == 1 ? "=" : "<>"));
                    hql.append(":sub_state");
                    parameterMap.put("sub_state", substate);
                }
            } else if (state == StateEnum.col_done.getKey()) {//已办时，如果子状态大于0表示加入子状态，否则排除子状态
                if (substate >= 0) {
                    hql.append(" and summary.state=:sub_state "); //已办结
                    parameterMap.put("sub_state", substate);

                    int hasArchive = substateArray[1];//是否归档
                    if (hasArchive >= 0) { //归档，0未归档，1已归档
                        hql.append(" and summary.hasArchive=:has_archive ");
                        parameterMap.put("has_archive", (hasArchive == 0 ? false : true));
                    }
                } else if (substateArray.length == 3 && substateArray[2] == 2) {
                    //收文已阅不判断是否结束，先这么写，以后重构
                    int hasArchive = substateArray[1];//是否归档
                    if (hasArchive >= 0) { //归档，0未归档，1已归档
                        hql.append(" and summary.hasArchive=:has_archive ");
                        parameterMap.put("has_archive", (hasArchive == 0 ? false : true));
                    }
                } else {
                    hql.append(" and summary.state<>:sub_state "); //未办结
                    parameterMap.put("sub_state", CollaborationEnum.flowState.finish.ordinal());
                }
            }

        }
        //GOV-3934  待办里没有数据！点击组合查询选择时间查询，查询出来了数据！ changyi
        String list = "";
        if (params != null && params.get("list") != null) {
            list = (String) params.get("list");
            //待办
            if ("notPending".equals(list)) {
                hql.append(" and affair.subState<>:sub_state "); //用于待办,排除在办的
                parameterMap.put("sub_state", SubStateEnum.col_pending_ZCDB.getKey());
            }
        }

        if (edocType >= 0) {
            hql.append(" and affair.app=:a_app ");
            parameterMap.put("a_app", EdocUtil.getAppCategoryByEdocType(edocType).getKey());
        } else if (edocType == -1) {
            hql.append(" and (affair.app =:a_app1 or affair.app =:a_app2 or affair.app =:a_app3)");
            parameterMap.put("a_app1", ApplicationCategoryEnum.edocRec.getKey());
            parameterMap.put("a_app2", ApplicationCategoryEnum.edocSend.getKey());
            parameterMap.put("a_app3", ApplicationCategoryEnum.edocSign.getKey());
        }


        if (!Strings.isBlank(em.getCreatePerson())) {

            String oldHql = hql.toString();
            int index = oldHql.indexOf("where");

            hql = new StringBuilder(oldHql.substring(0, index))
                    .append(",")
                    .append(OrgMember.class.getName())
                    .append(" as mem ")
                    .append(oldHql.substring(index))
                    .append(" and affair.senderId=mem.id")
                    .append(" and mem.name like :startMemberName");

            paramName = "startMemberName";
            paramValue = "%" + SQLWildcardUtil.escape(em.getCreatePerson()) + "%";
            parameterMap.put(paramName, paramValue);
        }


        /**
         * 收文管理  -- 成文单位
         * cy add
         */
        if (!Strings.isBlank(em.getSendUnit())) {
            paramName = "edocUnit";
            exp0 = new StringBuilder(" and summary.id = register.distributeEdocId ");
            exp0.append(" and register.edocUnit like :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = "%" + specialString(em.getSendUnit()) + "%";
            //paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }
        /**
         * 收文管理  -- 签收时间
         * cy add
         */
        if (em.getRecieveDateB() != null || em.getRecieveDateE() != null) {
            exp0 = new StringBuilder(" and summary.id = register.distributeEdocId ");
            exp0.append(" and recieve.id = register.recieveId ");

            if (em.getRecieveDateB() != null) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(em.getRecieveDateB());
                paramName = "timestamp1";
                exp0.append(" and recieve.recTime >= :");
                exp0.append(paramName);
                parameterMap.put(paramName, stamp);
            }
            if (em.getRecieveDateE() != null) {
                java.util.Date stamp = Datetimes.getTodayLastTime(em.getRecieveDateE());
                paramName = "timestamp2";
                exp0.append(" and recieve.recTime <= :");
                exp0.append(paramName);
                parameterMap.put(paramName, stamp);
            }
            hql.append(exp0);
        }
        /**
         * 收文管理  -- 登记时间
         * cy add
         */
        if (em.getRegisterDateB() != null || em.getRegisterDateE() != null) {
            exp0 = new StringBuilder(" and summary.id = register.distributeEdocId ");

            if (em.getRegisterDateB() != null) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(em.getRegisterDateB());
                paramName = "timestamp1";
                exp0.append(" and register.registerDate >= :");
                exp0.append(paramName);
                parameterMap.put(paramName, stamp);
            }
            if (em.getRegisterDateE() != null) {
                java.util.Date stamp = Datetimes.getTodayLastTime(em.getRegisterDateE());
                paramName = "timestamp2";
                exp0.append(" and register.registerDate <= :");
                exp0.append(paramName);
                parameterMap.put(paramName, stamp);
            }
            hql.append(exp0);
        }


        /**
         * 到达时间
         * cy add
         */
        if (em.getReceiveTimeB() != null || em.getReceiveTimeE() != null) {

            if (em.getReceiveTimeB() != null) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(em.getReceiveTimeB());

                paramName = "timestamp1";
                hql.append(" and affair.receiveTime >= :");
                hql.append(paramName);

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

            if (em.getReceiveTimeE() != null) {
                java.util.Date stamp = Datetimes.getTodayLastTime(em.getReceiveTimeE());
                paramName = "timestamp2";
                hql.append(" and affair.receiveTime <= :");
                hql.append(paramName);

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

        }

        //发起时间
        if (em.getCreateTimeB() != null || em.getCreateTimeE() != null) {
            if (em.getCreateTimeB() != null) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(em.getCreateTimeB());

                paramName = "timestamp1";
                hql.append(" and affair.createDate >= :");
                hql.append(paramName);

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

            if (em.getCreateTimeE() != null) {
                java.util.Date stamp = Datetimes.getTodayLastTime(em.getCreateTimeE());
                paramName = "timestamp2";
                hql.append(" and affair.createDate <= :");
                hql.append(paramName);

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }
        }


        /**
         * 主题词
         * cy add
         */
        if (!Strings.isBlank(em.getKeywords())) {
            paramName = "keywords";
            exp0 = new StringBuilder(" and summary.keywords like :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = new StringBuilder("%").append(specialString(em.getKeywords())).append("%").toString();
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }

        /**
         * 文件秘级
         * cy add
         */
        if (!Strings.isBlank(em.getSecretLevel())) {
            paramName = "secretLevel";
            exp0 = new StringBuilder(" and summary.secretLevel = :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = em.getSecretLevel();
            //paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }

        /**
         * 紧急程度
         * cy add
         */
        if (!Strings.isBlank(em.getUrgentLevel())) {
            paramName = "urgentLevel";
            exp0 = new StringBuilder(" and summary.urgentLevel = :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = em.getUrgentLevel();
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }


        if (!Strings.isBlank(em.getSubject())) {
            paramName = "subject";
            exp0 = new StringBuilder(" and summary.subject like :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = new StringBuilder("%").append(specialString(em.getSubject())).append("%").toString();
            //paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }

        if (!Strings.isBlank(em.getDocMark())) {
            paramName = "docMark";
            exp0 = new StringBuilder(" and summary.docMark like :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = new StringBuilder("%").append(specialString(em.getDocMark())).append("%").toString();
            //paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);

        }

        if (!Strings.isBlank(em.getSerialNo())) {
            paramName = "serialNo";
            exp0 = new StringBuilder(" and summary.serialNo like :");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = new StringBuilder("%").append(specialString(em.getSerialNo())).append("%").toString();
            //paramNameList.add(paramName);
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }
        //行文类型
        if (!Strings.isBlank(em.getSendType())) {
            paramName = "sendType";
            exp0 = new StringBuilder(" and summary.sendType=:");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = em.getSendType();
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);
        }
        //流程节点
        if (!Strings.isBlank(em.getNodePolicy())) {
            paramName = " ";
            exp0 = new StringBuilder(" and affair.nodePolicy=:");
            exp0.append(paramName);
            exp0.append(" ");
            paramValue = em.getNodePolicy();
            parameterMap.put(paramName, paramValue);
            hql.append(exp0);

        }

        if (em.getCreateDateB() != null || em.getCreateDateE() != null) {
            if (em.getCreateDateB() != null) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(em.getCreateDateB());

                paramName = "timestamp1";
                hql.append(" and affair.createDate >= :");
                hql.append(paramName);

                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }

            if (em.getCreateDateE() != null) {
                java.util.Date stamp = Datetimes.getTodayLastTime(em.getCreateDateE());
                paramName = "timestamp2";
                hql.append(" and affair.createDate <= :");
                hql.append(paramName);
                //paramNameList.add(paramName);
                parameterMap.put(paramName, stamp);
            }
        }


        if (edocType >= 0) {
            hql.append(" and summary.edocType=:s_edocType");
            parameterMap.put("s_edocType", edocType);
        } else if (edocType == -1) {
            hql.append(" and (summary.edocType=:edocType1 or summary.edocType =:edocType2 or summary.edocType =:edocType3)");
            parameterMap.put("edocType1", EdocEnum.edocType.sendEdoc.ordinal());
            parameterMap.put("edocType2", EdocEnum.edocType.recEdoc.ordinal());
            parameterMap.put("edocType3", EdocEnum.edocType.signReport.ordinal());
        }

        //同一流程只显示最后一条
        if ("true".equals(params.get("deduplication"))) {
            hql.append(" group by summary.id ) ");
        }

        if (state == StateEnum.col_pending.key()) {
            hql.append(" order by affair.receiveTime desc");
        } else if (state == StateEnum.col_done.key()) {
            hql.append(" order by affair.completeTime desc");
        } else {
            hql.append(" order by affair.createDate desc");
        }
        List result = edocSummaryDao.find(hql.toString(), parameterMap);


        java.util.Date early = null;
        if (edocAgent != null && !edocAgent.isEmpty())
            early = edocAgent.get(0).getStartDate();

        List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>(result.size());
        for (int i = 0; i < result.size(); i++) {
            Object[] object = (Object[]) result.get(i);
            CtpAffair affair = new CtpAffair();
            EdocSummary summary = new EdocSummary();
            make(object, summary, affair);

            try {
                V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                summary.setStartMember(member);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }

            //开始组装最后返回的结果
            EdocSummaryModel model = new EdocSummaryModel();
            if (state == StateEnum.col_waitSend.key()) {
                model.setWorkitemId(null);
                model.setCaseId(null);
                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setSummary(summary);
                model.setAffairId(affair.getId());

            } else if (state == StateEnum.col_sent.key()) {
                model.setWorkitemId(null);
                model.setCaseId(summary.getCaseId() + "");
                model.setStartDate(new Date(summary.getCreateTime().getTime()));
                model.setSummary(summary);
                model.setAffairId(affair.getId());
                //设置流程是否超期标志
                java.sql.Timestamp startDate = summary.getCreateTime();
                java.sql.Timestamp finishDate = summary.getCompleteTime();
                Date now = new Date(System.currentTimeMillis());
                if (summary.getDeadline() != null && summary.getDeadline() != 0) {
                    Long deadline = summary.getDeadline() * 60000;
                    if (finishDate == null) {
                        if ((now.getTime() - startDate.getTime()) > deadline) {
                            summary.setWorklfowTimeout(true);
                        }
                    } else {
                        Long expendTime = summary.getCompleteTime().getTime() - summary.getCreateTime().getTime();
                        if ((deadline - expendTime) < 0) {
                            summary.setWorklfowTimeout(true);
                        }
                    }
                }
            } else {
                model.setWorkitemId(affair.getObjectId() + "");
                model.setCaseId(summary.getCaseId() + "");
                model.setSummary(summary);
                model.setAffairId(affair.getId());
            }
            int affairState = affair.getState();
            if (affairState == StateEnum.col_waitSend.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.WaitSend.name());
            } else if (affairState == StateEnum.col_sent.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.Sent.name());
            } else if (affairState == StateEnum.col_done.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.Done.name());
            } else if (affairState == StateEnum.col_pending.key()) {
                model.setEdocType(EdocSummaryModel.EDOCTYPE.Pending.name());
            }

            model.setFinshed(summary.getCompleteTime() != null);

            model.setBodyType(affair.getBodyType());

            //公文状态
            Integer sub_state = affair.getSubState();
            if (sub_state != null) {
                model.setState(sub_state.intValue());
            }

            //是否跟踪
            Integer isTrack = affair.getTrack();
            if (isTrack != null) {
                model.setTrack(isTrack.intValue());
            }

            //催办次数
            Integer hastenTimes = affair.getHastenTimes();
            if (hastenTimes != null) {
                model.setHastenTimes(hastenTimes);
            }

            //检查是否有附件
            model.setHasAttachments(AffairUtil.isHasAttachments(affair));

            //是否超期
            Boolean overtopTime = affair.isCoverTime();
            if (overtopTime != null) {
                model.setOvertopTime(overtopTime.booleanValue());
            }
            //提前提醒
            Long advanceRemind = affair.getRemindDate();
            if (advanceRemind == null) {
                advanceRemind = 0L;
            }
            model.setAdvanceRemindTime(advanceRemind);

            //协同处理期限
            Long deadLine = affair.getDeadlineDate();
            if (deadLine == null) {
                deadLine = 0L;
            }
            model.setDeadLine(deadLine);
            model.setDeadlineDisplay(EdocHelper.getDeadLineName(summary.getDeadlineDatetime()));
            V3xOrgMember member = null;
            //是否代理
            if (state == StateEnum.col_done.key()) {
                model.setAffair(affair);
            }

            if (state == StateEnum.col_pending.key() && agentFlag && affair.getMemberId() != user.getId()) {
                Long proxyMemberId = affair.getMemberId();
                try {
                    member = orgManager.getMemberById(proxyMemberId);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
                model.setProxyName(member.getName());
                model.setProxy(true);
            } else if (state == StateEnum.col_pending.key() && agentToFlag && early != null && early.before(affair.getReceiveTime())) {
                model.setProxy(true);
            }

            model.setNodePolicy(affair.getNodePolicy());

            if (affair.getCompleteTime() != null) {
                model.setDealTime(new Date(affair.getCompleteTime().getTime()));
            }
            //修复bug GOV-3087 【公文管理】-【发文管理】-【待办】，公文组合查询前'办理剩余时间'显示为已超期或者显示剩余时间段，组合查询后显示为'无'了
            model.setSurplusTime(calculateSurplusTime(affair.getReceiveTime(), affair.getDeadlineDate()));
            //设置当前处理人信息
            model.setCurrentNodesInfo(EdocHelper.parseCurrentNodesInfo(summary));
            models.add(model);
        }
        return models;

    }


    // 特殊字符处理
    private String specialString(String field) {
        // yangzd 特殊字符处理
        if (null != field) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < field.length(); i++) {

                if (field.charAt(i) == '\'') {
                    buffer.append("\\'");
                } else {
                    buffer.append(field.charAt(i));
                }
            }
            field = SQLWildcardUtil.escape(buffer.toString());
        }
        // yangzd 特殊字符处理

        return field;
    }

/*	public void setDocumentSinatureDao(DocumentSignatureDao documentSinatureDao) {
		this.documentSinatureDao = documentSinatureDao;
	}*/


    /**
     * lijl添加,通过edocId,userId,oplicy查询同一用户同一文单的所有意见
     *
     * @param edocId 公文单ID
     * @param userId 用户ID
     * @param oplicy 审批类型,'shenpi,tuihui...'
     * @return List<EdocOpinion>
     */
    public List<EdocOpinion> findEdocOpinion(Long edocId, Long userId, String oplicy) {
        return edocOpinionDao.findEdocOpinion(edocId, userId, oplicy);
    }

    public List<EdocOpinion> findEdocOpinionByAffairId(Long edocId, Long userId, String oplicy, List<Long> affairIds) {
        return edocOpinionDao.findEdocOpinionByAffairId(edocId, userId, oplicy, affairIds);
    }

    /**
     * lijl添加根据对象修改意见的State,0表示正常,1表示不显示,2退回时的状态
     *
     * @param edocOpinion EdcoOpinion对象
     */
    public void update(EdocOpinion edocOpinion) {
        updateOpinion(edocOpinion);
    }

    /**
     * lijl重写上边方法,退回时把以前是0的状态改为2
     *
     * @param
     */
    public void update(Long edocId, Long userid, String oplicy, int newstate, int oldstate) {
        edocOpinionDao.update(edocId, userid, oplicy, newstate, oldstate);
    }

    /**
     * 计算节点处理剩余时间
     *
     * @param date         流程到达时间
     * @param deadlineDate 节点处理期限（分钟）
     * @return 剩余时间（不足一天单位为：小时，反之单位为：天）
     */
    @SuppressWarnings("deprecation")
    private int[] calculateSurplusTime(java.util.Date date, Long deadlineDate) {
        int[] surplusTime = null;

        User user = AppContext.getCurrentUser();
        try {
            if (deadlineDate != null && deadlineDate.longValue() != 0 && date != null) {
                int days = 0;
                int hours = 0;
                int minutes = 0;
                // 获取系统当前时间
                java.util.Date nowTime = new java.util.Date();
                // 得到节点处理的最后时间
                java.util.Date overTime = workTimeManager.getCompleteDate4Nature(date, deadlineDate, user.getAccountId());
                // 未超期
                if (nowTime.before(overTime)) {
                    // 得到剩余处理时间（分钟）
                    int surplusMinu = (int) workTimeManager.getDealWithTimeValue(nowTime, overTime, user.getAccountId()) / (1000 * 60);
                    // 得到当前单位当月的日工作时间（分钟）
                    int dayOfMinu = workTimeManager.getEachDayWorkTime(nowTime.getYear(), user.getAccountId());
                    if (surplusMinu >= dayOfMinu) {
                        // 天数
                        days = surplusMinu / dayOfMinu;
                        int shenyufen = surplusMinu - days * dayOfMinu;
                        if (shenyufen < 60) {
                            minutes = shenyufen;
                        } else {
                            hours = shenyufen / 60;
                            minutes = shenyufen - hours * 60;
                        }
                    } else if (60 <= surplusMinu && surplusMinu < dayOfMinu) {
                        hours = surplusMinu / 60;
                        minutes = surplusMinu - hours * 60;
                    } else {
                        minutes = surplusMinu;
                    }
                }
                surplusTime = new int[3];
                surplusTime[0] = days;
                surplusTime[1] = hours;
                surplusTime[2] = minutes;
            }
        } catch (WorkTimeSetExecption e) {
            LOGGER.error("", e);
        }
        return surplusTime;
    }


    @Override
    public boolean checkTempleteDisabled(Long templeteId) {
        if (null == templeteId) {
            return false;
        }

        try {
            CtpTemplate templete = templateManager.getCtpTemplate(templeteId);
            if (null != templete && templete.getState() == TemplateStateEnums.invalidation.getKey()) {
                return true;
            }
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }

        return false;
    }


    /**
     * 跟踪相关设置
     *
     * @param affairId     : affairiD
     * @param isTrack      :是否设置了跟踪
     * @param trackMembers ：部门跟踪人员的ID串
     * @throws BusinessException
     */
    public void setTrack(Long affairId, boolean isTrack, String trackMembers) {
        List<Long> members = new ArrayList<Long>();
        if (Strings.isNotBlank(trackMembers)) {
            String[] m = trackMembers.split(",");
            for (String s : m) {
                members.add(Long.valueOf(s));
            }
        }

        this.changeTrack(affairId, isTrack, members);

    }

    public void changeTrack(Long affairId, boolean track, List<Long> memberIds) {
        try {
            CtpAffair affair = affairManager.get(affairId);
            if (affair == null) { // 不妥
                return;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("affairId", affair.getId());
            trackdao.deleteTrackInfo(map);
            if (track && memberIds != null && !memberIds.isEmpty()) {
                List<CtpTrackMember> trackMembers = new ArrayList<CtpTrackMember>();
                for (Long trackMemberId : memberIds) {
                    CtpTrackMember coltm = new CtpTrackMember();
                    coltm.setIdIfNew();
                    coltm.setObjectId(affair.getObjectId());
                    coltm.setAffairId(affair.getId());
                    coltm.setMemberId(affair.getMemberId());
                    coltm.setTrackMemberId(trackMemberId);
                    trackMembers.add(coltm);
                }
                trackdao.save(trackMembers);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

    }

    public Long runCase(EdocSummary summary,
                        EdocBody body, EdocOpinion senderOpinion, EdocEnum.SendType sendType,
                        Map options, String from, Long agentToId) throws EdocException {
        //boolean isResend = (ColConstant.SendType.resend.ordinal() == sendType.ordinal());
        //boolean isForward = (ColConstant.SendType.forward.ordinal() == sendType.ordinal());
        User user = AppContext.getCurrentUser();
        Long affairId = 0L;
        try {
            String processId = summary.getProcessId();
            if (processId != null && "".equals(processId.trim()))
                processId = null;


            //根据选人界面传来的people生成流程模板XML


            body.setIdIfNew();
            body.setEdocSummary(summary);
            summary.getEdocBodies().add(body);

            //查找出所有的edocBody对象

            List<EdocBody> allBody = edocBodyDao.getBodyByIdAndNum(summary.getId());

            if (allBody != null) {
                for (EdocBody eb : allBody) {
                    //防止含有两个相同的对象或者相同正文编号（0，1，2）的对象。
                    if (eb.getContentNo() != null
                            && body.getContentNo() != null
                            && eb.getContentNo().intValue() != body.getContentNo().intValue())
                        summary.getEdocBodies().add(eb);
                }
            }

            //生成流程模板对象
            EdocSummary _summary = edocSummaryDao.get(summary.getId());
            if (_summary != null) {
                deleteOpinionBySummaryId(_summary.getId());
                edocSummaryDao.delete(_summary);
                affairManager.deleteByObjectId(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()), summary.getId());
                //删除处理意见，手写批注
                //   htmlSignDao.delete(new String[]{"summaryId"},new Object[]{summary.getId()});
            }
            //保存colsummary、body
            summary.setIdIfNew();
            //if (isResend) {
            //    summary.setResentTime(summary.getResentTime() == null ? 1 : summary.getResentTime() + 1);
            //}


            Timestamp now = new Timestamp(System.currentTimeMillis());

            summary.setCreateTime(now);

            if (body.getCreateTime() == null) {
                body.setCreateTime(now);
            }
            summary.setProcessId(processId);
            if (summary.getStartTime() == null) {
                summary.setStartTime(now);
            }
            summary.setStartUserId(user.getId());

            //附言内容为空，就不记录了
            if (senderOpinion.getContent() != null && !"".equals(senderOpinion.getContent())) {
                senderOpinion.setEdocSummary(summary);
                senderOpinion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
                senderOpinion.setCreateTime(now);
                senderOpinion.setCreateUserId(user.getId());
                summary.getEdocOpinions().add(senderOpinion);
            }

            CtpAffair affair = new CtpAffair();
            affair.setIdIfNew();
            affairId = affair.getId();
            _setIsUnit(summary);

            //先保存公文，在维护公文和附言的关系即存储公文附言 author:zhangg
            edocSummaryDao.save(summary);
            if (senderOpinion.getContent() != null && !"".equals(senderOpinion.getContent())) {
                saveOpinion(senderOpinion);
            }
            affair.setApp(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).getKey());
            affair.setSubject(summary.getSubject());
            affair.setCreateDate(now);
            //收文登记的时候可能是代理人登记的。
            if (agentToId != null) {
                affair.setMemberId(agentToId);
                affair.setTransactorId(user.getId());
            } else {
                affair.setMemberId(user.getId());
            }
            affair.setObjectId(summary.getId());
            affair.setSubObjectId(null);
            affair.setSenderId(user.getId());
            affair.setState(StateEnum.col_sent.key());
            affair.setTrack(senderOpinion.affairIsTrack == false ? 0 : 1);

            affair.setProcessId(summary.getProcessId());
            affair.setCaseId(null);

            Long _deadline = summary.getDeadline();
            if (_deadline != null && _deadline.intValue() > 0) {
                affair.setDeadlineDate(_deadline);
            }
            affair.setBodyType(summary.getFirstBody().getContentType());
            AffairUtil.setHasAttachments(affair, summary.isHasAttachments());
            //affair.serialExtProperties();
            if (summary.getUrgentLevel() != null && !"".equals(summary.getUrgentLevel())) {
                affair.setImportantLevel(Integer.parseInt(summary.getUrgentLevel()));
            }
            affair.setTempleteId(summary.getTempleteId());

            //BPMProcess bPMProcess = EdocHelper.getCaseProcess(processId);

            //添加到公文统计表
            edocStatManager.createState(summary, user);


            //如果收文和签报设置了预归档目录，则流程发起后就自动归档，V320工作项
            if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()
                    || summary.getEdocType() == EdocEnum.edocType.signReport.ordinal()) {

                if (summary.getArchiveId() != null && summary.getTempleteId() != null) {  //设置了预先归档的目录
                    pigeonholeAffair("", affair, summary.getId(), summary.getArchiveId());
                }
            }

            try {
                MetaUtil.refMeta(summary);
            } catch (Exception e) {
                LOGGER.error("更改枚举项为引用出现异常 error = " + e);
            }

            EdocHelper.createQuartzJobOfSummary(summary, workTimeManager);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return affairId;
    }


/*
	@Override
	public String transFinishWorkItem(EdocSummary summary, long affairId,
			EdocOpinion signOpinion,  String title,
			String supervisorMemberId, String supervisorNames,
			String superviseDate, String processId, String userId,
			String edocMangerID,String processXml,String readyObjectJSON,String workflowNodePeoplesInput,String workflowNodeConditionInput, String processChangeMessage) throws Exception {
		//督办开始--
	    edocSuperviseManager.supervise(title, supervisorMemberId, supervisorNames, superviseDate, summary);
    	//督办结束--

    	return transFinishWorkItem(summary, affairId, signOpinion, processId,
    	        userId, edocMangerID,processXml,readyObjectJSON,workflowNodePeoplesInput,workflowNodeConditionInput, processChangeMessage);
	}*/

    @Override
    public void saveUpdateAttInfo(int attSize, Long summaryId,
                                  List<ProcessLog> logs) {
        try {
            EdocSummary edocSummary = getEdocSummaryById(summaryId, false);
            boolean needUpdate = false;
            boolean hasAtt = attSize != 0;
            if ((edocSummary.isHasAttachments() && !hasAtt)
                    || (!edocSummary.isHasAttachments() && hasAtt)) {
                needUpdate = true;
            }
            if (needUpdate) {
                edocSummary.setHasAttachments(hasAtt);

                Map<String, Object> p = new HashMap<String, Object>();
                p.put("identifier", edocSummary.getIdentifier());
                update(edocSummary.getId(), p);

//                Affair affair = new Affair();
//                affair.setHasAttachments(hasAtt);
//                Map<String,Object> parameter = new HashMap<String,Object>();
//                parameter.put("identifier", affair.getIdentifier());
//
//                affairManager.updateAllAvailabilityAffair(EdocUtil.getAppCategoryByEdocType(edocSummary.getEdocType()),
//                        edocSummary.getId(), parameter);
            }
            processLogManager.insertLog(logs);
            EdocMessageHelper.updateAttachmentMessage(affairManager, userMessageManager, orgManager, edocSummary);
        } catch (Exception e) {
            LOGGER.error("更新附件信息", e);
        }
    }


    /**
     * Ajax判断是否有发布公告、新闻的权限
     *
     * @param policyName
     * @return true(有权限) false无权限
     * @throws Exception
     */

    public boolean AjaxjudgeHasPermitIssueNewsOrBull(String policyName) throws Exception {
        User user = AppContext.getCurrentUser();
        List<BulTypeBO> typeList = null;
        if ("bulletionaudit".equals(policyName) && AppContext.hasPlugin("bulletin")) {
            typeList = bulletinApi.findAllBulletinTypesCanIssue(user.getId());
        }
        if (Strings.isEmpty(typeList)) {
            return false;//没有权限
        }
        return true;
    }

    public String getPhysicalPath(String logicalPath, String separator, boolean needSub1, int beginIndex) {
        String ret = "";
        if (AppContext.hasPlugin("doc")) {
            try {
                ret = docApi.getPhysicalPath(logicalPath, separator, needSub1, beginIndex);
            } catch (BusinessException e) {
                LOGGER.error("", e);
            }
        }
        return ret;
    }


    /**
     * 判断当前公文文号是否已经被占用（除开公文自己本身占用的文号）
     *
     * @param edocSummaryId :公文ID
     * @param serialNo      :内部文号
     * @return 0:不存在，1:已存在
     */
    public String checkSerialNoExcludeSelf(String edocSummaryId, String serialNo) {
        User user = AppContext.getCurrentUser();
        if (Strings.isBlank(serialNo)) {
            serialNo = "";
        }
        long accountId = user.getAccountId();
        if (Strings.isNotBlank(edocSummaryId)) {
            EdocSummary summary = edocSummaryManager.findById(Long.valueOf(edocSummaryId));
            if (summary != null) {
                accountId = summary.getOrgAccountId();
            }
        }
        int exsit = edocSummaryManager.checkSerialNoExsit(edocSummaryId, serialNo, Long.valueOf(accountId));
        return String.valueOf(exsit);
    }

    public String checkDocMarkExcludeSelf(String edocSummaryId, String docMark) {
        if (Strings.isBlank(docMark)) {
            EdocSummary summary;
            try {
                summary = this.getEdocSummaryById(Long.parseLong(edocSummaryId), false);
                docMark = summary.getDocMark();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        User user = AppContext.getCurrentUser();
        int exsit = edocSummaryDao.checkDocMarkExsit(edocSummaryId, docMark, user.getLoginAccount());
        return String.valueOf(exsit);
    }

    @Override
    public void pigeonholeAffair(String pageType, long affairId,
                                 Long summaryId, Long archiveId, String pigeonholeType)
            throws EdocException, BusinessException {
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null) {
            LOGGER.info("归档：affair 为空,affairId:" + affairId);
            return;
        }
        User user = AppContext.getCurrentUser();
        // 下面这个查询可能使用了hibernate的一级缓存，直接从缓存里面取的，所以在lisenter里面需要设置completetime，否则最后一个节点在这里不是结束状态
        EdocSummary summary = this.getEdocSummaryById(summaryId, false);
        if (archiveId != null) {
            summary.setArchiveId(archiveId);
        }

        if (summary != null) {//公文只归档一次，未归档才进行归档
            if (null != archiveId) {
                Map<String, Object> colums = new HashMap<String, Object>();
                colums.put("hasArchive", true);
                colums.put("archiveId", archiveId);
                this.edocSummaryDao.update(summaryId, colums);
                /**
                 * 如果是模板预归档，archiveId是已经绑定了目录ID,所以不能变更archiveId，保留预归档绑定的ID
                 * 追加更新方法，发文设置了预归档，已发归档后，需要更新hasArchive字段
                 */
            } else if (summary.getArchiveId() != null) {
                Map<String, Object> colums = new HashMap<String, Object>();
                colums.put("hasArchive", true);
                this.edocSummaryDao.update(summaryId, colums);
            } else {
                // OA-38126  归档的公文撤销流程后再次归档失败！  oracle下，archiveID为null进行update时会出现异常
                /**
                 * OA-45572 发送一个公文模板，处理节点处理时进行了处理后归档，已办中查看属性状态，归档到显示"无",
                 * 此代码逻辑有问题，如果是预归档的的公文，根据summaryId查询，页面上传的archiveId肯定为空，执行更新后，
                 * 就把原来已经归档的变成没有归档的状态了,当archiveId为空时，不需要更新Summary表的数据，除非有新的archiveId归档目录传过来，
                 * 才进行更新，此修改为v5sprint时修改出的问题
                 */
                //this.edocSummaryDao.bulkUpdate("update EdocSummary set archiveId=null,hasArchive=? where id=?", null, true, summaryId);
            }
            //发起者归档跟新affair.ActivityId
            if (affair.getArchiveId() == null) {
                affair.setArchiveId(summary.getArchiveId());
                affairManager.updateAffair(affair);
            }
            try {
                edocStatManager.setArchive(summary.getId());
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            try {
                boolean hasAtt = summary.isHasAttachments();

                LOGGER.info(new StringBuilder("公文执行归档操作：summary.getId()：")
                        .append(summary.getId())
                        .append(",hasAtt:")
                        .append(hasAtt)
                        .append(",summary.getArchiveId():")
                        .append(summary.getArchiveId())
                        .append(",pigeonholeType:")
                        .append(pigeonholeType)
                        .append(",user.getId():")
                        .append(user.getId()).toString());
                if (AppContext.hasPlugin("doc") && summary.getArchiveId() != null) {
                    docApi.pigeonholeWithoutAcl(user.getId(), ApplicationCategoryEnum.edoc.getKey(), summary.getId(), hasAtt, summary.getArchiveId(), Integer.parseInt(pigeonholeType), null);
                }
                //发文封发时候的归档直接删除已发已办事项,其他情况的归档都不删除已发已办事项
                if (("fengfa".equals(affair.getNodePolicy())
                        && affair.getApp() == ApplicationCategoryEnum.edocSend.getKey())
                        || summary.getFinished()) {

                    setArchiveIdToAffairsAndSendMessages(summary, affair, true);

                    try {
                        if (summary.getIsQuickSend() == null || !summary.getIsQuickSend()) { //快速收发文的归档不用写入流程日志
                            String params = summary.getSubject();
                            Long activityId = affair.getActivityId();
                            if (activityId == null) {
                                BPMActivity bPMActivity = EdocHelper.getBPMActivityByAffair(affair);//当前节点
                                if (bPMActivity != null)
                                    activityId = Long.valueOf(bPMActivity.getId());
                            }
                            if (activityId != null) {
                                this.processLogManager.insertLog(user, summary.getProcessId() == null ? 0L : Long.valueOf(summary.getProcessId()), activityId.longValue(), ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.pigeonhole.getKey()), params);
                            } else {
                                this.processLogManager.insertLog(user, summary.getProcessId() == null ? 0L : Long.valueOf(summary.getProcessId()), -1l, ProcessLogAction.processEdoc, String.valueOf(ProcessLogAction.ProcessEdocAction.pigeonhole.getKey()), params);
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.error("发送消息错误", e);
                        throw new EdocException(e);
                    }
                }
                this.appLogManager.insertLog(user, AppLogAction.Edoc_PingHole, user.getName(), summary.getSubject());
            } catch (Exception e) {
                LOGGER.error("", e);
                throw new EdocException(e);
            }
        }

    }


    public String colCheckAndupdateLock(String processId, Long summaryId) throws BusinessException {
        return colCheckAndupdateLock(processId, summaryId, true);
    }

    private final Object CheckAndupdateLock = new Object();

    private String colCheckAndupdateLock(String processId, Long summaryId, boolean isLock) throws BusinessException {
        CtpAffair senderAffair = this.affairManager.getSenderAffair(summaryId);
        if (senderAffair == null || (senderAffair.getState().intValue() == StateEnum.col_waitSend.key() && senderAffair.getSubState().intValue() != SubStateEnum.col_pending_specialBackToSenderCancel.getKey() && senderAffair.getSubState().intValue() != SubStateEnum.col_pending_specialBacked.getKey())) {
            return "--NoSuchSummary--";
        }

        User user = AppContext.getCurrentUser();
        String modifyUserName = null;
        synchronized (CheckAndupdateLock) {
            modifyUserName = getModifyUserName(processId, summaryId);
            if (modifyUserName == null && isLock) {
                //加锁
                EdocHelper.updateProcessLock(processId, user.getId() + "");
            }

        }
        modifyUserName = getModifyUserName(processId, summaryId);
        return modifyUserName;
    }


    private String getModifyUserName(String processId, Long summaryId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        String modifyUserName = null;
        if (user == null) {
            return modifyUserName;
        }
        String userId = user.getId() + "";
        try {
            String modifyUserId = EdocHelper.isModifyProcess(processId, userId, orgManager);
            if (modifyUserId != null && !"".equals(modifyUserId)) {
                V3xOrgAccount account = orgManager.getRootAccount();
                V3xOrgMember member = orgManager.getMemberById(Long.parseLong(modifyUserId));
                if (member.getOrgAccountId().equals(account.getId())) {
                    modifyUserName = ResourceUtil.getString("group.name");
                } else {
                    modifyUserName = orgManager.getMemberById(Long.parseLong(modifyUserId)).getName();
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return modifyUserName;
    }

    public List<EdocSummaryModel> getMyEdocDeadlineNotEmpty(Map<String, Object> tempMap) {
        return edocSummaryDao.getMyEdocDeadlineNotEmpty(tempMap);
    }

  /*  public EdocManagerModel transFinishWorkItem(HttpServletRequest request, EdocManagerModel edocManagerModel,Map<String,Object> params) throws  BusinessException{


    }
    */

    /**
     * 处理公文的方法
     *
     * @param affair 处理的事项对象
     * @param params <pre>
     *                                                                                        {
     *                                                                                           <b>必须的参数:</b>
     *                                                                                           params.userId | Long 当前用户的Id
     *
     *                                                                                           <b>非必要的参数:</b>
     *                                                                                           params.isHidden | String    意见-是否隐藏
     *                                                                                           params.attitude | String    意见-态度
     *                                                                                           params.contentOP | String   意见-内容
     *                                                                                           params.opinion | EdocOpinion对象          意见对象
     *                                                                                           params.optionWay | String  意见-覆盖的方式
     *                                                                                           params.oldOpinionId | String 旧意见ID
     *
     *                                                                                           params.afterSign | String   ?
     *                                                                                           params.trackMembers | String 跟踪人员
     *                                                                                           params.trackRange | String 跟踪范围
     *                                                                                           pushMsgMemberList
     *
     *                                                                                           params.edocExchangeType | String 公文交换的类型
     *                                                                                           params.returnDeptId | String  公文交换  - 部门交换的时候选择的部门Id
     *                                                                                           exchangeMemberId | String 公文交换  - 单位交换的时候选择的具体的公文收发员
     *                                                                                           params.docMark | String 公文文号
     *                                                                                           params.serialNo | String 内部文号
     *                                                                                           params.isConvertPdf | 是否需要Word转PDF
     *                                                                                           params.archiveId | String 归档ID
     *                                                                                           params.optionType | String ？
     *                                                                                           params.supervisorId | String
     *                                                                                           params.awakeDate | String
     *                                                                                           params.supervisors | String
     *                                                                                           params.superviseTitle | String
     *                                                                                           isDeleteSupervisior
     *
     *                                                                                           params.process_xml | String
     *                                                                                           readyObjectJSON | String
     *                                                                                           workflow_node_peoples_input | String
     *                                                                                           workflow_node_condition_input | String
     *                                                                                           process_message_data | String
     *                                                                                           processChangeMessage | String
     *
     *                                                                                           signing_date | String
     *                                                                                           signingDate | String
     *                                                                                     }
     *                                                                                     </pre>
     * @throws Exception
     */
    private XkjtTempManager tempManager = new XkjtTempManagerImpl();

    public XkjtTempManager getTempManager() {
        return tempManager;
    }

    @Override
    public void transFinishWorkItem(CtpAffair affair, Map<String, Object> params) throws Exception {
//     获取回退state=6的数据   zhou start
        Map<String, Object> map6 = new HashMap<>();
        map6.put("activityId", affair.getActivityId());
        map6.put("objectId", affair.getObjectId());
        String hql = "from CtpAffair where state=6 and nodePolicy='转送' and  activityId=:activityId and objectId=:objectId ";
        List<CtpAffair> list6 = affairManager.findState6(hql, map6);
        XkjtTemp temp = null;
        if (list6.size() > 0) {
            for (CtpAffair a : list6) {
                temp = new XkjtTemp();
                temp.setId(Long.toString(a.getId()));
                temp.setSummaryId(Long.toString(a.getObjectId()));
                tempManager.saveXkjtTemp(temp);
            }
        }
//     获取回退state=6的数据   zhou end

        String attitude = (String) params.get("attitude");
        String content = (String) params.get("contentOP");
        String optionWay = (String) params.get("optionWay");
        EdocOpinion edocOpinion = (EdocOpinion) params.get("opinion");
        String isHidden = (String) params.get("isHidden");

        String[] afterSign = (String[]) params.get("afterSign");
        String trackMembers = (String) params.get("trackMembers");
        String trackRange = (String) params.get("trackRange");
        String exchangeType = (String) params.get("edocExchangeType");
        String returnDeptId = (String) params.get("returnDeptId");
        String docMark = (String) params.get("docMark");

        String newPdfId = (String) params.get("newPdfIdFirst");

        String docMark2 = (String) params.get("docMark2");
        String serialNo = (String) params.get("serialNo");
        String isConvertPdf = (String) params.get("isConvertPdf");
        //设置手动选择的归档路径ID
        String archiveId = (String) params.get("archiveId");
        String oldOpinionIdStr = (String) params.get("oldOpinionId");

        String optionType = (String) params.get("optionType");

        String spMemberId = (String) params.get("supervisorId");
        String superviseDate = (String) params.get("awakeDate");
        String supervisorNames = (String) params.get("supervisors");
        String title = (String) params.get("superviseTitle");
        String processXml = (String) params.get("process_xml");
        String readyObjectJSON = (String) params.get("readyObjectJSON");
        String workflowNodePeoplesInput = (String) params.get("workflow_node_peoples_input");
        String workflowNodeConditionInput = (String) params.get("workflow_node_condition_input");


        String signingDate = (String) params.get("signing_date");
        String oldSigningDate = (String) params.get("signingDate");

        String messageDataListJSON = (String) params.get("process_message_data");
        String isDeleteSupervisior = (String) params.get("isDeleteSupervisior");
        List<Long[]> pushMsgMemberList = (List<Long[]>) params.get("pushMsgMemberList");
        String processChangeMessage = (String) params.get("processChangeMessage");
        String exchangeMemberId = (String) params.get("exchangeMemberId");

        Long userId = (Long) params.get("userId");
        if (userId == null) {
            userId = AppContext.currentUserId();
        }

        EdocSummary summary = null;
        EdocOpinion signOpinion = edocOpinion;

        signOpinion.setCreateUserId(affair.getMemberId());

        summary = this.getEdocSummaryById(affair.getObjectId(), true);
        boolean flagDocMark = true;
        if (Strings.isNotBlank(summary.getDocMark())) {
            flagDocMark = false;
        }
        //docMark2
        boolean flagDocTMark = true;
        if (Strings.isNotBlank(summary.getDocMark2())) {
            flagDocTMark = false;
        }

        boolean flagSerialNoMark = true;
        if (Strings.isNotBlank(summary.getSerialNo())) {
            flagSerialNoMark = false;
        }
        //OA-49613wangt发文封发后，收发员在已发送中查看自动填充了签发日期，但是发文单中没有签发人和签发日期的
        String policy = getPolicyByAffair(affair, summary.getProcessId());

        // 历史原因设置
        if ("_from_pc_".equals((String) params.get("_from_pc_"))) {

            if (!Strings.isBlank(attitude)) {
                signOpinion.setAttribute(Integer.valueOf(attitude).intValue());
            } else {
                signOpinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
            }

            Set<String> opts = new HashSet<String>();
            if (afterSign != null) {
                for (String option : afterSign) {
                    opts.add(option);
                }
            }
            //允许处理后归档和跟踪被同时选中，但是他们都不能和删除按钮同时选中
            if (opts.size() > 1 && opts.contains("delete")) {
                opts.remove("delete");
            }
            signOpinion.isDeleteImmediate = opts.contains("delete");
            boolean track = opts.contains("track");
            signOpinion.affairIsTrack = track;
            signOpinion.isPipeonhole = opts.contains("pipeonhole");


            //不跟踪 或者 全部跟踪的时候不向部门跟踪表中添加数据，所以将下面这个参数串设置为空。
            if (!track || "1".equals(trackRange)) {
                trackMembers = "";
            }
            if (Strings.isNotBlank(trackRange)) {
                affair.setTrack(Integer.parseInt(trackRange));
                affairManager.updateAffair(affair);
            }
            setTrack(affair.getId(), track, trackMembers);
            boolean isHiden = isHidden != null;


            signOpinion.setIsHidden(isHiden);
            signOpinion.setIdIfNew();

            //-8222600899131324823|蓉内[2012]第0417号|417|1  (文号是这种形式了，需要处理)
            if (Strings.isNotBlank(docMark)) {
                String[] docMarks = docMark.split("\\|");
                if (docMarks.length > 1) {
                    summary.setDocMark(docMarks[1]);
                } else {
                    summary.setDocMark(docMark);
                }
            } else {
                //公文流转如果设置了文号就一定不能为空了只能且必须有值的往下流转
                if (flagDocMark) {
                    //发送时删除了文号
                    summary.setDocMark(null);
                }

            }
            if (Strings.isNotBlank(docMark2)) {
                String[] docMarks = docMark2.split("\\|");
                if (docMarks.length > 1) {
                    summary.setDocMark2(docMarks[1]);
                } else {
                    summary.setDocMark2(docMark2);
                }
            } else {
                if (flagDocTMark) {
                    //发送时删除了文号2
                    summary.setDocMark2(null);
                }

            }
            if (Strings.isNotBlank(serialNo)) {
                String[] serialNos = serialNo.split("\\|");
                if (serialNos.length > 1) {
                    summary.setSerialNo(serialNos[1]);
                } else {
                    summary.setSerialNo(serialNo);
                }
            } else {
                //发送时删除了内部文号
                if (flagSerialNoMark) {
                    summary.setSerialNo(null);
                }
            }


            //为了保存流程日志中修改附件的记录在处理提交之前，所以将保存附件的操作提前了。bug29527
            //保存附件


            //如果选择的是"退回时办理人选择覆盖方式,其他保留最后意见",将在同一个节点权限域,的其他意见全部设置为1
            if ("3".equals(optionType) && Strings.isBlank(optionWay)) {
                this.update(affair.getObjectId(), userId, edocOpinion.getPolicy(), 1, 0);
            }


            if (null != oldOpinionIdStr && !"".equals(oldOpinionIdStr) && (null != affair.getSubState() && affair.getSubState() != SubStateEnum.col_pending_specialBacked.getKey()))//取回后再次提交要删除原意见（指定回退不能删除提交人的意见）
            {//删除原来意见,上传附件等
                Long oldOpinionId = Long.parseLong(oldOpinionIdStr);
                try {
//                     attachmentManager.deleteByReference(edocManagerModel.getSummaryId(), oldOpinionId);
//                     attachmentManager.deleteByReference(edocManagerModel.getSummaryId());
                    this.deleteEdocOpinion(oldOpinionId);
                } catch (BusinessException e) {
                    LOGGER.error("", e);
                }
            }

            //修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(AppContext.getRawRequest());
            if (editHelper.hasEditAtt()) {//是否修改附件
                //attachmentManager.deleteByReference(edocManagerModel.getSummaryId(), edocManagerModel.getSummaryId());//删除公文附件
                saveAttachment(summary, affair, signOpinion.getId(), AppContext.getRawRequest());
            } else {
                //保存公文附件及回执附件，create方法中前台subReference传值为空，默认从java中传过去， 因为公文附件subReference从前台js传值 过来了，而回执附件没有传subReference，所以这里传回执的id
                this.attachmentManager.create(ApplicationCategoryEnum.edoc, summary.getId(), signOpinion.getId(), AppContext.getRawRequest());
            }

            /**
             * 如果处理时添加了附件
             */
            signOpinion.setHasAtt(EdocHelper.isAddAttachmentByOpinion(AppContext.getRawRequest(), summary.getId()));

            if (editHelper.hasEditAtt()) {//是否修改附件
                //设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(summary.getProcessId()), affair.getActivityId()), true, "finish");
            }


            if ("qianfa".equals(policy)) {
                if (Strings.isNotBlank(signingDate)) {
                    if (signingDate.equals(oldSigningDate)) {
                        summary.setSigningDate(new Date(System.currentTimeMillis()));
                    } else {
                        summary.setSigningDate(new Date(Datetimes.parse(signingDate).getTime()));
                    }
                } else {
                    summary.setSigningDate(new Date(System.currentTimeMillis()));
                }
            }

            //加签，减签，知会提醒
            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
            //XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
            // boolean isOpen;
            //try {
            //isOpen = xkjtManager.isOpenBySender(affair.getId());
            //if(isOpen){
            edocMessagerManager.sendOperationTypeMessage(messageDataListJSON, summary, affair);
            //}
            //} catch (BusinessException e) {
            // TODO Auto-generated catch block
            //	LOGGER.error("获取是否发送消息失败",e);
            //}
            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 end*/
        }


        if (null != content && !"".equals(content)) {
            signOpinion.setContent(content);
        }

        if (exchangeType != null && !"".equals(exchangeType)) {
            signOpinion.exchangeType = Integer.parseInt(exchangeType);
        }
        //String spMemberId = request.getParameter("supervisorId");
        //String superviseDate = request.getParameter("awakeDate");
        //String supervisorNames = request.getParameter("supervisors");
        //String title = request.getParameter("superviseTitle");

        if (Strings.isNotBlank(isConvertPdf)) {
            createPdfBodies(summary, newPdfId);
        }


        if (Strings.isNotBlank(archiveId) && signOpinion.isPipeonhole) {
            //若归档目录变更
            if (summary.getArchiveId() != null && summary.getArchiveId().longValue() != Long.parseLong(archiveId)) {
                //原归档目录撤销归档
                if (summary.getHasArchive()) {
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(summary.getId());
                    if (docApi != null) {
                        docApi.deleteDocResources(AppContext.currentUserId(), ids);
                    }

                    summary.setHasArchive(false);
                    summary.setArchiveId(Long.parseLong(archiveId));
                    edocSummaryDao.saveOrUpdate(summary);
                }
            }

            summary.setArchiveId(Long.parseLong(archiveId));
        }

        //发文、预归档并未归档、且存在交换类型操作，归档
        if (summary.getEdocType() == 0 && summary.getArchiveId() != null && !summary.getHasArchive() && signOpinion.exchangeType >= 0) {
            this.pigeonholeAffair("", affair, summary.getId(), summary.getArchiveId(), false);
            summary.setHasArchive(true);
        }


        if (null != supervisorNames && !"".equals(supervisorNames) && null != spMemberId && !"".equals(spMemberId) && null != superviseDate && !"".equals(superviseDate)) {
            edocSuperviseManager.supervise(title, spMemberId, supervisorNames, superviseDate, summary);
        }

        if ("true".equals(isDeleteSupervisior)) {
            this.edocSuperviseManager.deleteSuperviseDetailAndSupervisors(summary);
        }


        /**
         * wangwei start
         */
        //保存正文
        User user = AppContext.getCurrentUser();
        EdocInfo info = new EdocInfo();
        info.setSummary(summary);
        AffairData affairData = EdocHelper.getAffairData(summary, user);
        affairData.setTemplateId(info.getSummary().getTempleteId());//如协同colsummary
        affairData.setBodyContent(signOpinion.getContent());
        //重置memberId,用于存放当前affair真实的处理人
        affairData.setMemberId(affair.getMemberId());
        if (summary.getDeadline() != null && summary.getDeadline().intValue() > 0) {
            affairData.setProcessDeadline(summary.getDeadline());
        }
        String[] wresult = new String[]{""};
        MessageCommentParam mc = new MessageCommentParam();
        try {
            //if(info.getIsDelAffair()){//删除个人待办事项
//	              ContentUtil.workflowFinish(affairData, affair.getSubObjectId());

            //正常处理为 9

            WorkflowBpmContext context = new WorkflowBpmContext();
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, 9);
            context.setDebugMode(false);
            context.setAppName("edoc");
            context.setCurrentWorkitemId(affair.getSubObjectId());
            context.setCurrentUserId(String.valueOf(user.getId()));
            context.setCurrentUserName(user.getName());
            context.setCurrentAccountId(String.valueOf(user.getAccountId()));
            context.setCurrentAccountName(user.getName());
            context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
            context.setSelectedPeoplesOfNodes(workflowNodePeoplesInput);
            context.setConditionsOfNodes(workflowNodeConditionInput);
            context.setAppObject(summary);
            context.setChangeMessageJSON(processChangeMessage);

            //首页栏目的扩展字段设置--公文文号、发文单位等--start
            context.setBusinessData(EdocEventDataContext.CTP_AFFAIR_EDOC_SEND_DOC_MARK, summary.getDocMark());
            context.setBusinessData(EdocEventDataContext.CTP_AFFAIR_ID, affair.getId());
            context.setBusinessData(EdocEventDataContext.CTP_AFFAIR_EDOC_SEND_SEND_UNIT, summary.getSendUnit());
            context.setBusinessData(EdocEventDataContext.CTP_AFFAIR_EDOC_CONTENT_OP, signOpinion.getContent());
            context.setBusinessData(EdocEventDataContext.CTP_IS_ADDATTACHMENT_BY_OPINION, signOpinion.isHasAtt());//是否意见中上传了附件
            context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, EdocWorkflowEventListener.COMMONDISPOSAL);
            context.setBusinessData(EdocWorkflowEventListener.PUSHMSG_MEMBERLIST, pushMsgMemberList);

            if (processXml != null && !"".equals(processXml.trim())) {
                context.setProcessXml(processXml);
                //OA-35655  weblogic环境：a1新建节点权限，前台使用该权限，后台进行删除，删除成功了，应该是提示引用了不能删除才对
                //加签后的节点权限也要更新引用了
                EdocUtil.updatePermissinRef(affairData.getModuleType(), processXml, String.valueOf(summary.getCaseId()), "-1", user.getLoginAccount());
            }
            if (readyObjectJSON != null && !"".equals(readyObjectJSON.trim())) {
                context.setReadyObjectJson(readyObjectJSON);
            }

            if (signOpinion != null) {
                mc = new MessageCommentParam(signOpinion.getId(), signOpinion.getIsHidden(), signOpinion.getContent(), signOpinion.getAttribute(), signOpinion.isHasAtt());
                context.setBusinessData(EdocWorkflowEventListener.COMMENT_MESSAGE, mc);
            }
            if (Strings.isBlank(context.getFormData())) {
                context.setFormData("-1");
            }
            if (Strings.isBlank(context.getBussinessId())) {
                context.setBussinessId(String.valueOf(summary.getId()));
            }
            wresult = wapi.finishWorkItem(context);


            //重新取一下数据，因为调用工作流接口时，可能数据发生了变化，比如流程结束的，工作流会把isFinish字段更新，但是要注意，中间如果有对affair set值，那需要改
            affair = affairManager.get(affair.getId());


            // }else{//内容新增保存或更新保存
            //   ContentUtil.contentSaveOrUpdate(ContentUtil.OperationType.finish,affairData,info.getSummary(),false);//内容新增保存或更新保存
            //}
        } catch (BusinessException e) {
            LOGGER.error("处理公文调用工作流接口出错", e);
            //工作流内部出现异常，需要立即向外抛出
            throw new BusinessException(e.getMessage(), e);
        }
        //更新Affair状态
        affair.setCompleteTime(new Timestamp(System.currentTimeMillis()));
        affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
        affair.setState(StateEnum.col_done.key());

        //更新操作时间
        if (affair.getSignleViewPeriod() == null && affair.getFirstViewDate() != null) {
            java.util.Date nowTime = new java.util.Date();
            long viewTime = workTimeManager.getDealWithTimeValue(affair.getFirstViewDate(), nowTime, affair.getOrgAccountId());
            affair.setSignleViewPeriod(viewTime);
        }

        //第一次操作时间
        if (affair.getFirstResponsePeriod() == null) {
            java.util.Date nowTime = new java.util.Date();
            long responseTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());
            affair.setFirstResponsePeriod(responseTime);
        }

        if (affair.getMemberId().longValue() != user.getId().longValue()) {//非本人处理的，写入处理人ID(代理人)
            affair.setTransactorId(user.getId());
        } else {
            affair.setTransactorId(null);
        }

        affair.setSummaryState(summary.getState());

        String ret = "";


        if (null != affair.getActivityId()) {
            signOpinion.setNodeId(affair.getActivityId());
        }
        String wfparams = wresult[0];

        //封发节点，日志操作描述记录公文时交换到部门收发员还是单位收发员。
        List<ProcessLogDetail> allProcessLogDetailList = wapi.getAllWorkflowMatchLogAndRemoveCache(workflowNodeConditionInput);
        if ("fengfa".equals(policy)) {
            wfparams = getParams(signOpinion, wfparams);
            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.seal, allProcessLogDetailList, wfparams);
        } else {
            //lijl添加了记录退回时的意见,在param5字段中存储
            this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), affair.getActivityId(), ProcessLogAction.commit, allProcessLogDetailList, wfparams.toString(), null, null, null, null, signOpinion.getContent());
            //this.processLogManager.insertLog(user, Long.valueOf(summary.getProcessId()), Long.valueOf(bPMActivity.getId()), ProcessLogAction.commit, params);
        }
        /** 指定回退消息：被退回人提交后发消息 **/
        String[] nextMemberNodes = wresult;
        Boolean isSepicalBackedSubmit = Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()).equals(affair.getSubState());
        if (nextMemberNodes.length == 2 && Strings.isNotBlank(nextMemberNodes[1]) && isSepicalBackedSubmit) {
            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
            //XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
            //boolean isOpen;
            try {
                //isOpen = xkjtManager.isOpenBySender(affair.getId());
                //if(isOpen){
                EdocMessageHelper.transSendSubmitMessage4SepicalBacked(summary, nextMemberNodes[1], affair, mc, pushMsgMemberList);
                //}
            } catch (BusinessException e) {
                // TODO Auto-generated catch block
                LOGGER.error("获取是否发送消息失败", e);
            }
            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 end*/
        }

        boolean upd = false;
        Map<String, Object> namedParameter = new HashMap<String, Object>();

        //EdocHelper.finishWorkitem(workItemId);
        //String policy=getPolicyByAffair(affair);
        if ("qianfa".equals(policy)) {
            //GOV-4934.公文签发人加签的签发人在公文交换时不显示 start
            String issuerName = user.getName();
            try {
                issuerName = orgManager.getMemberById(affair.getMemberId()).getName();
            } catch (Exception e) {
                LOGGER.error("查找人员错误", e);
            }
            String issuserStr = summary.getIssuer();
            String separator = ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources", "common.separator.label");
            if (Strings.isNotBlank(issuserStr)) {
                String[] arg = issuserStr.split(separator);
                boolean findName = false;
                for (String name : arg) {
                    if (name.equals(issuerName)) {
                        findName = true;
                        break;
                    }
                }
                if (findName) {
                    issuerName = issuserStr;
                } else {
                    issuerName += separator + summary.getIssuer();
                }
            }
            summary.setIssuer(issuerName);
            edocStatManager.updateElement(summary);
            namedParameter.put("issuer", issuerName);
            upd = true;

            //如果有多人签发，则取最后一个签发节点审批的时间为签发时间
            //OA-49132ALL：240环境公文'签发'节点处理报错，无法处理
            //此问题为移动端调用产生的，summary中的signingDate为空了，因为signingDate值是在controller中设置的
            //而移动端是直接调用manager层，所以需要做个防护设个默认值
            if (summary.getSigningDate() == null) {
                summary.setSigningDate(new Date(System.currentTimeMillis()));
            }
            namedParameter.put("signingDate", summary.getSigningDate());
            upd = true;
            //GOV-4934.公文签发人加签的签发人在公文交换时不显示 end
        }
        if ("shenhe".equals(policy)) {
            String auditorName = EdocHelper.getAuditorName(user, affair.getMemberId());
            String auditorStr = summary.getAuditor();
            auditorName = EdocHelper.getCompositionStr(auditorStr, auditorName);
            upd = true;
            summary.setAuditor(auditorName);
            namedParameter.put("auditor", summary.getAuditor());
        }
        if ("fuhe".equals(policy)) {
            String auditorName = EdocHelper.getAuditorName(user, affair.getMemberId());
            String reviewStr = summary.getReview();
            auditorName = EdocHelper.getCompositionStr(reviewStr, auditorName);
            upd = true;
            summary.setReview(auditorName);
            namedParameter.put("review", auditorName);
        }
        if ("chengban".equals(policy)) {
            String auditorName = EdocHelper.getAuditorName(user, affair.getMemberId());
            String reviewStr = summary.getUndertaker();
            auditorName = EdocHelper.getCompositionStr(reviewStr, auditorName);
            upd = true;
            summary.setUndertaker(auditorName);
            namedParameter.put("undertaker", summary.getUndertaker());

            //承办机构
            appendUndertakenoffice(namedParameter, summary, user, affair.getMemberId());
        }

        if ("fengfa".equals(policy) || signOpinion.exchangeType >= 0) {
            summary.setFinished(true);
            summary.setCompleteTime(new Timestamp(System.currentTimeMillis()));
            //  封发的时候进行相关的问号操作，移动到历史表中。tdbug28578 以封发节点完成提交，作为流程结束标志。
            edocMarkHistoryManager.afterSend(summary);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (summary.getPackTime() == null) {
                summary.setPackTime(now);
                namedParameter.put("packTime", now);
            }
            namedParameter.put("completeTime", now);
            namedParameter.put("state", CollaborationEnum.flowState.finish.ordinal());

            upd = true;

            if (summary.getHasArchive() && summary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal())
                setArchiveIdToAffairsAndSendMessages(summary, affair, true);
        }
        if (!("fengfa".equals(policy) || signOpinion.exchangeType >= 0) && summary.getState() == CollaborationEnum.flowState.finish.ordinal()) {
            //流程结束的时候进行相关的文号操作，移动到历史表中。tdbug28578 以封发节点完成提交，作为流程结束标志。
            edocMarkHistoryManager.afterSend(summary);
        }

        if (summary != null) {
            signOpinion.setEdocSummary(summary);
            signOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            signOpinion.setOpinionType(EdocOpinion.OpinionType.signOpinion.ordinal());
            if (Strings.isBlank(signOpinion.getPolicy())) {
                signOpinion.setPolicy(affair.getNodePolicy());
            }
            if (!Integer.valueOf(2).equals(affair.getTrack())) {
                affair.setTrack(signOpinion.affairIsTrack ? 1 : 0);
            }
            if (affair.getMemberId().longValue() != user.getId().longValue()) {
                List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(user.getId());
                if (agentModelList != null && !agentModelList.isEmpty()) {
                    signOpinion.setCreateUserId(affair.getMemberId());
                    signOpinion.setProxyName(user.getName());
                } else {
                    signOpinion.setCreateUserId(user.getId());
                }
                //非本人处理的，写入处理人ID
                affair.setTransactorId(user.getId());
            } else {
                signOpinion.setCreateUserId(user.getId());
            }
            //signOpinion.setCreateUserId(user.getId());
            if (null != signOpinion.getContent()) {
                signOpinion.setContent(signOpinion.getContent().replace("<br/>", "\r\n"));
            }

            saveOpinion(signOpinion);

            //判断是否为下级单位向上级汇报的意见
            if (!signOpinion.getIsReportToSupAccount()
                    && Integer.valueOf(1).equals(summary.getEdocType())) {

                String disPosition = edocFormManager.getOpinionLocation(summary.getFormId(), EdocHelper.getFlowPermAccountId(summary, user.getLoginAccount())).get(policy);
                if (disPosition != null) {
                    String[] dis = disPosition.split("[_]");
                    disPosition = dis[0];
                    if ("report".equals(disPosition)) {
                        signOpinion.setIsReportToSupAccount(true);
                    }
                }
            }
            if (signOpinion.getIsReportToSupAccount() && summary.getEdocType() == 1) {
                Timestamp createTime = signOpinion.getCreateTime();
                //上级收文没有被撤销时 才能向上级填报意见
                if ("false".equals(edocExchangeTurnRecManager.isSupEdocCanceled(String.valueOf(summary.getId())))) {
                    Long supEdocId = edocExchangeTurnRecManager.findSupEdocId(summary.getId());
                    if (supEdocId != null) {
                        List<EdocOpinion> els = edocExchangeTurnRecManager.getDelStepBackSupOptions(summary.getId(), supEdocId);
                        if (els != null && els.size() > 0) {
                            for (EdocOpinion op : els) {
                                //被退回后再提交的意见，更新时间用第一条意见时间，因为上级显示下级意见是以第一次提交为准
                                createTime = op.getCreateTime();
                                deleteOpinion(op);
                            }
                        }
                        EdocOpinion reportOpinion = signOpinion.cloneEdocOpinion();
                        reportOpinion.setOpinionType(EdocOpinion.OpinionType.reportOpinion.ordinal());

                        EdocSummary supSummary = this.getEdocSummaryById(supEdocId, false);

                        reportOpinion.setEdocSummary(supSummary);
                        reportOpinion.setPolicy(EdocOpinion.FEED_BACK);
                        reportOpinion.setSubEdocId(summary.getId());
                        reportOpinion.setCreateTime(createTime);
                        reportOpinion.setSubOpinionId(signOpinion.getId());
                        saveOpinion(reportOpinion);

                        /**
                         * 向上级单位转收文人发送消息
                         */
                        EdocExchangeTurnRec turnRec = edocExchangeTurnRecManager.findEdocExchangeTurnRecByEdocId(supEdocId);
                        //获得转收文人处理的上级收文对应的affair
                        List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edocRec, supEdocId, turnRec.getUserId());
                        if (affairs != null && affairs.size() > 0) {
                            CtpAffair affair1 = affairs.get(0);
                            String key = "";
                            if (signOpinion.getContent().length() > 0) {
                                key = "edoc.report";  // 有意见
                            } else {
                                key = "edoc.report1";  // 无意见
                            }

                            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
                            MessageReceiver receiver = new MessageReceiver(affair1.getId(), affair1.getMemberId(), "message.link.edoc.report", affair1.getId().toString());
                            receiver.setReply(true);
                            receivers.add(receiver);
                            //意见格式：下级单位({0})对公文《{1}》意见为:{2}
                            //当消息太长时，截取下
                            String opinionStr = signOpinion.getContent();
                            if (opinionStr.length() > 30) {
                                opinionStr = opinionStr.substring(0, 30) + "...";
                            }
                            String accountName = AppContext.currentAccountName();

                            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 start*/
                            /**项目：徐州矿物【只允许发送给指定公开的人员 -- 加签】 作者：xiaohailong 时间：2020年5月9日 start**/
                            Set<MessageReceiver> can_receivers = new HashSet<MessageReceiver>();
                            Iterator<MessageReceiver> it1 = receivers.iterator();
                            while (it1.hasNext()) {
                                MessageReceiver mr = it1.next();
                                XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
                                //判断其发送人是否有被公开的权限
                                boolean issendmessage = xkjtManager.issendmessage(affair.getId(), mr.getReceiverId());
                                if (issendmessage) {
                                    can_receivers.add(mr);
                                }
                                //long receiverId = mr.getReceiverId();
                            }
                            //清空之前的回退消息
                            receivers.clear();
                            //加入现在的消息人员
                            receivers.addAll(can_receivers);
                            /**项目：徐州矿物【只允许发送给指定公开的人员 -- 加签】 作者：xiaohailong 时间：2020年5月9日 end**/
                            //XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
                            //boolean isOpen;
                            try {
                                //isOpen = xkjtManager.isOpenBySender(affair.getId());
                                //if(isOpen){
                                MessageContent msg = new MessageContent(key, accountName, supSummary.getSubject(), opinionStr);
                                msg.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
                                if (null != supSummary.getTempleteId()) {
                                    msg.setTemplateId(supSummary.getTempleteId());
                                }
                                userMessageManager.sendSystemMessage(msg,
                                        ApplicationCategoryEnum.edocRec,
                                        user.getId(),
                                        receivers,
                                        EdocMessageHelper.getSystemMessageFilterParam(affair1).key);
                                //}
                            } catch (BusinessException e) {
                                // TODO Auto-generated catch block
                                LOGGER.error("获取是否发送消息失败", e);
                            }
                            /**项目：徐矿集团   【根据权限屏蔽消息】 作者：jiangchenxi 时间：2019年3月15日 end*/
                        }
                    }
                }
            }

            if (signOpinion.isDeleteImmediate) {
                affairManager.deleteAffair(affair.getId());
                //updateEdocIndex(summary.getId());
            }
            boolean UpdateAffairFlag = false;
            if (signOpinion.affairIsTrack) {
                UpdateAffairFlag = true;
//	                affairManager.updateAffair(affair);
            }

//	            if(signOpinion.getIsHidden())

//	          处理页面是否包含交换设置
            if (signOpinion.exchangeType >= 0) {
                Long unitId = -1L;
                String exchangeMemberOrDeptId = null;
                if (signOpinion.exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Dept) {//部门交换
                    if (Strings.isBlank(returnDeptId)) {
                        unitId = orgManager.getMemberById(affair.getSenderId()).getOrgDepartmentId();
                    } else {//发起人存在副岗时，选择交换部门
                        unitId = Long.valueOf(returnDeptId);
                        exchangeMemberOrDeptId = returnDeptId;
                    }
                } else if (signOpinion.exchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org) {//单位交换
                    unitId = summary.getOrgAccountId();
                    exchangeMemberOrDeptId = exchangeMemberId;
                }
                try {
                    sendEdocManager.create(summary, unitId, signOpinion.exchangeType, exchangeMemberOrDeptId, affair, false);
                    edocSuperviseManager.updateBySummaryId(summary.getId());//更新督办结束状态
                    //更新公文统计表为封发
                    edocStatManager.setSeal(summary.getId());
                } catch (Exception e) {
                    LOGGER.error("生成公文统计表错误", e);
                    throw new EdocException(e);
                }
                summary.setFinished(true);
                upd = true;
                namedParameter.put("completeTime", new Timestamp(System.currentTimeMillis()));
            }
            addPdfBodyToCurrentSummary(summary);


            /************处理后修改当前待办人**************/

            try {
                EdocHelper.updateCurrentNodesInfo(summary, false);
            } catch (Exception e1) {
                LOGGER.error("公文提交时更新当前待办人报错!", e1);
            }
            namedParameter.put("currentNodesInfo", summary.getCurrentNodesInfo());

            //承办机构名称显示成最新的名称
            String names = EdocHelper.getEntityNames(summary.getUndertakenofficeId(), "、", orgManager);
            namedParameter.put("undertakenoffice", names);
            summary.setUndertakenoffice(names);

            upd = true;
            if (upd) {
	                /*edocSummaryDao.update(summary);
	                edocSummaryDao.forceCommit();*/
                namedParameter.put("docMark", summary.getDocMark());
                namedParameter.put("docMark2", summary.getDocMark2());
                namedParameter.put("serialNo", summary.getSerialNo());
                edocSummaryDao.update(summary.getId(), namedParameter);
            }

            //归档处理
            if (signOpinion.isPipeonhole && !summary.getHasArchive()) {
                pigeonholeAffair("Pending", affair.getId(), summary.getId(), summary.getArchiveId());  //这里面有session.flush，故必须放在最后
                //更新统计表归档
                try {
                    edocStatManager.setArchive(summary.getId());
                } catch (Exception e) {
                    LOGGER.error("更新归档信息错误，summaryId＝" + summary.getId(), e);
                    throw new EdocException(e);
                }
            }
            if (summary.getHasArchive()
                    && summary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal()
                    && summary.getFinished()) {//发文归档并且流程结束的时候删除后续节点的已办事项

                if (affair.getArchiveId() == null) {
                    affair.setArchiveId(summary.getArchiveId());
                    UpdateAffairFlag = true;
//	                        affairManager.updateAffair(affair);
                }
            }
            if (UpdateAffairFlag) {
                //处理完成后一定更affair
                affairManager.updateAffair(affair);
            }

            //开始：写在这个位置的原因是：当取回后再去转送，显示的当前代办人是查询ctp_affair state=3的数据。zhou_2020-08-05:多账号竞争执行，在此解决处理时间问题（同一流程节点a处理了，b的处理时间为空）
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("activityId", affair.getActivityId().longValue());
            pMap.put("objectId", affair.getObjectId().longValue());
            List<CtpAffair> affairList = affairManager.findBycondition(pMap);
            String hqlz = "update CtpAffair a set a.state=:state ,a.subState=:subState,a.completeTime=:completeTime where id=:id";
            Map<String, Object> tp = new HashMap<>();
            tp.put("summaryId", Long.toString(affair.getObjectId().longValue()));
            List<XkjtTemp> temps = tempManager.findXkjtTemp(tp);
            List<String> stringList = new ArrayList<>();
            if (temps.size() > 0) {
                for (XkjtTemp t : temps) {
                    stringList.add(t.getId());
                }
            }

            if (affairList.size() > 0) {
                Map<String, Object> phql = null;
                for (CtpAffair af : affairList) {
                    phql = new HashMap<>();
                    if (stringList.size() > 0) {
                        if (!stringList.contains(Long.toString(af.getId()))) {
                            phql.put("state", 4);
                            phql.put("subState", 0);
                            phql.put("completeTime", new java.util.Date());
                            phql.put("id", af.getId());
                            affairManager.update(hqlz, phql);
                        }
                    } else {
                        phql.put("state", 4);
                        phql.put("subState", 0);
                        phql.put("completeTime", new java.util.Date());
                        phql.put("id", af.getId());
                        affairManager.update(hqlz, phql);
                    }
                }
            }
            //结束： zhou_2020-08-05:多账号竞争执行，在此解决处理时间问题（同一流程节点a处理了，b的处理时间为空）
        }


        //协同立方接口调用
        Timestamp now = new Timestamp(System.currentTimeMillis());
        EdocFinishEvent event = new EdocFinishEvent(this, affair.getMemberId(), now, affair);
        EventDispatcher.fireEvent(event);


        if (AppContext.hasPlugin("index")) {
            indexManager.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
        }
    }

    /**
     * 修改附件的保存
     *
     * @param summary
     * @param attaFlag
     * @param affair
     * @return
     * @throws BusinessException
     */
    private String saveAttachment(EdocSummary summary, CtpAffair affair, Long opinionId, HttpServletRequest request) throws BusinessException {
        String attaFlag = "";
        try {


            // 全部附件
            List<Attachment> allAtts = attachmentManager.getAttachmentsFromRequest(ApplicationCategoryEnum.edoc, summary.getId(), null, request);

            // 标题区域意见
            List<Attachment> importAtts = new ArrayList<Attachment>();

            // 意见区域意见
            List<Attachment> optionAtts = new ArrayList<Attachment>();

            if (allAtts != null) {

                for (Attachment att : allAtts) {
                    if (summary.getId().equals(att.getSubReference())) {
                        importAtts.add(att);
                    } else {
                        // 意见区附件
                        att.setSubReference(opinionId);
                        optionAtts.add(att);
                    }
                }
            }

            // 保存意见区附件
            if (Strings.isNotEmpty(optionAtts)) {
                attachmentManager.create(optionAtts);
            }

            //传入的附件
            List<Long> importAttIds = new ArrayList<Long>();
            Map<Long, Attachment> importAttsMap = new HashMap<Long, Attachment>();
            //已有的附件
            List<Attachment> oldAtts = attachmentManager.getByReference(summary.getId(), summary.getId());
            List<Long> oldAttIds = new ArrayList<Long>();
            Map<Long, Long> oldIds = new HashMap<Long, Long>();
            //需要新增的附件
            List<Attachment> addAtts = new ArrayList<Attachment>();

            for (Attachment att : importAtts) {
                importAttIds.add(att.getFileUrl());
                importAttsMap.put(att.getFileUrl(), att);
            }
            int maxSort = 0;
            for (Attachment att : oldAtts) {
                oldAttIds.add(att.getFileUrl());
                oldIds.put(att.getFileUrl(), att.getId());
                maxSort = maxSort > att.getSort() ? maxSort : att.getSort();
            }

            for (Long url : importAttIds) {
                if (!oldAttIds.contains(url)) {
                    Attachment addAtt = importAttsMap.get(url);
                    addAtt.setSort(++maxSort);
                    addAtts.add(addAtt);
                }
            }
            for (Long url : oldAttIds) {
                if (!importAttIds.contains(url)) {
                    attachmentManager.deleteById(oldIds.get(url));
                }
            }

            attaFlag = attachmentManager.create(addAtts, false);
            LOGGER.info("添加附件成功返回的attaFlag:" + attaFlag);

            AttachmentEditUtil attUtil = new AttachmentEditUtil("attActionLogDomain");
            List<ProcessLog> logs = attUtil.parseProcessLog(Long.valueOf(summary.getProcessId()), affair.getActivityId());
            processLogManager.insertLog(logs);

            //修改附件更新全文检索库
            if (AppContext.hasPlugin("index")) {
                indexManager.update(affair.getObjectId(), ApplicationCategoryEnum.edoc.getKey());
            }
        } catch (Exception e) {
            LOGGER.error("创建附件出错，位于方法", e);
            throw new BusinessException("创建附件出错");
        }
        return attaFlag;
    }

    /**
     * 联合发文暂时不支持转化为PDF正文
     *
     * @param request
     * @param summary
     */
    public void createPdfBodies(EdocSummary summary, String newPDFId) {
        //WORD转PDF正文
        //1、判断当前公文开关是否允许转PDF操作
        //2、获取前台参数
        //3、组装PDF BODY对象
        //4、设置进EDOCSUMMARY中，传至MANAGER层进行处理
        //boolean canConvert=EdocSwitchHelper.canEnablePdfDocChange();
        //	String isConvertPdf=request.getParameter("isConvertPdf");
        //if(Strings.isNotBlank(isConvertPdf)){
//			String pdfIdSecond=request.getParameter("pdfIdSecond");
        EdocBody pdfBody = new EdocBody();
//		    EdocBody  pdfBodySend=new EdocBody();
//		    if(summary.getIsunit() && summary.getEdocBodies().size()>1){
//		    	if(summary.getBody(EdocBody.EDOC_BODY_PDF_TWO)==null){//当前公文不存在PDF正文
//		        	pdfBodySend.setIdIfNew();
//		        	pdfBodySend.setContentType(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_PDF);
//		        	pdfBodySend.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
//		        	pdfBodySend.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
//		        	pdfBodySend.setContentNo(EdocBody.EDOC_BODY_PDF_TWO);
//		    	}
//		    	else{//覆盖当前PDF正文。取最新的PDF正文
//		    		pdfBodySend=summary.getBody(EdocBody.EDOC_BODY_PDF_TWO);
//		    		pdfBodySend.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
//		    	}
//		    	pdfBodySend.setEdocSummary(summary);
//		    	pdfBodySend.setContent(pdfIdSecond);
//		    	summary.getEdocBodies().add(pdfBodySend);
//		    }
        if (summary.getBody(EdocBody.EDOC_BODY_PDF_ONE) == null) {//当前公文不存在PDF正文
            pdfBody.setIdIfNew();
            pdfBody.setContentType(com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_PDF);
            pdfBody.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
            pdfBody.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
            pdfBody.setContentNo(EdocBody.EDOC_BODY_PDF_ONE);
        } else {//覆盖当前PDF正文。取最新的PDF正文
            pdfBody = summary.getBody(EdocBody.EDOC_BODY_PDF_ONE);
            pdfBody.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        pdfBody.setContent(newPDFId);
        summary.getEdocBodies().add(pdfBody);
        pdfBody.setEdocSummary(summary);
        //this.saveEdocBody(pdfBody);
        //}
    }


    public List<Long> checkSubject4Personal(String type, String subject) {
        User user = AppContext.getCurrentUser();
        List list = edocSummaryDao.checkSubject4Personal(type, subject, user.getId());

        return list;
    }

    public List<Long> checkSubject4System(String type, String subject) {
        User user = AppContext.getCurrentUser();
        List list = edocSummaryDao.checkSubject4System(type, subject, user.getLoginAccount());

        return list;
    }

    public void saveEdocRegisterCondition(EdocRegisterCondition condition) {
        edocSummaryDao.saveEdocRegisterCondition(condition);
    }

    public List<EdocRegisterCondition> getEdocRegisterCondition(long accountId, Map<String, Object> paramMap, User user) {
        return edocSummaryDao.getEdocRegisterCondition(accountId, paramMap, user);
    }

    public int getEdocRegisterConditionTotal(long accountId, int type, String subject) {
        return edocSummaryDao.getEdocRegisterConditionTotal(accountId, type, subject);
    }

    public void delEdocRegisterCondition(long id) {
        edocSummaryDao.delEdocRegisterCondition(id);
    }

    public EdocRegisterCondition getEdocRegisterConditionById(long id) {
        return edocSummaryDao.getEdocRegisterConditionById(id);
    }

    @Override
    public void updateAffairStateWhenClick(CtpAffair affair) throws BusinessException {
        Integer sub_state = affair.getSubState();
        if (sub_state == null || sub_state.intValue() == SubStateEnum.col_pending_unRead.key()) {
            //affair.setSubState(SubStateEnum.col_pending_read.key());
            //affairManager.updateAffair(affair);
            Map<String, Object> map = new HashMap<String, Object>();
            StringBuffer hql = new StringBuffer();
            hql.append("update CtpAffair set subState=:subState where id=:id");
            map.put("id", affair.getId());
            map.put("subState", SubStateEnum.col_pending_read.key());
            affairManager.update(hql.toString(), map);
            //要把已读状态写写进流程
            if (affair.getSubObjectId() != null && affair.getState().intValue() == StateEnum.col_pending.getKey()) {//待办时才更新状态
                try {

                    //更新第一次查看时间
                    java.util.Date nowTime = new java.util.Date();
                    long firstViewTime = workTimeManager.getDealWithTimeValue(affair.getReceiveTime(), nowTime, affair.getOrgAccountId());
                    /*affair.setFirstViewPeriod(firstViewTime);
                    affair.setFirstViewDate(nowTime);

                    affairManager.updateAffair(affair);*/

                    Map<String, Object> newMap = new HashMap<String, Object>();
                    StringBuffer newHql = new StringBuffer();
                    newHql.append("update CtpAffair set firstViewPeriod=:firstViewTime,firstViewDate=:nowTime where id=:id");
                    newMap.put("id", affair.getId());
                    newMap.put("firstViewTime", firstViewTime);
                    newMap.put("nowTime", nowTime);
                    affairManager.update(newHql.toString(), newMap);

                    wapi.readWorkItem(affair.getSubObjectId());
                } catch (BPMException e) {
                    //在这里吃掉了，不往外抛了
                    LOGGER.error("公文调用流程，设置已读状态异常", e);
                }
            }
        }
    }

    /**
     * 判读公文是否已经被发送
     *
     * @param summaryId
     * @return
     */
    public boolean isBeSended(Long summaryId) {
        EdocSummary summary = edocSummaryDao.get(summaryId);
        if (summary != null && 1 == summary.getEdocType()) {
            return false;
        }
        return edocSummaryDao.isBeSended(summaryId);
    }

    private WorkflowAjaxManager WFAjax;

    public void setWFAjax(WorkflowAjaxManager WFAjax) {
        this.WFAjax = WFAjax;
    }

    public String canTakeBack(String appName, String processId, String activityId, String workitemId, String affairId) {
        try {
            /**
             *    -B(处理提交)
             * A--
             *    -C(回退)
             * 当BC并发时，B处理提交，C回退，这时公文会在A的待发列表
             *
             * B处理提交后，到已办列表，开启新会话用C回退，B已办中取回
             * 这时就需要判断 当前B对应的affair的状态了
             * 上面这种情况是 回退后到 发起人的待发列表的情况,affair的state为5，表示取消(撤销)
             *
             * 如果回退后 流程不是到发起人处，也就是A后面 还有一个节点比如E，E之后才是 B和C,不管它们是并发还是串发，这时affair的state为6表示回退
             */
            CtpAffair affair = affairManager.getAffairBySubObjectId(Long.parseLong(workitemId));
            //撤销
            if (affair.getState() == StateEnum.col_cancel.key()) {
                return "11";
            }
            //回退
            else if (affair.getState() == StateEnum.col_stepBack.key()) {
                return "12";
            }
            if (affair.getObjectId() != null) {
                EdocSummary edoc = edocSummaryDao.get(affair.getObjectId());
                if (CollaborationEnum.flowState.finish.ordinal() == edoc.getState()) {
                    return "1";
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        String state = null;
        try {
            String str = WFAjax.canTakeBack(appName, processId, activityId, workitemId);
            Map map = (Map) JSONUtil.parseJSONString(str);
            state = String.valueOf(map.get("state"));
            //String canTakeBack = String.valueOf(map.get("canTakeBack"));

        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return state;
    }

    /**
     * wangwei 督办修改流程回调生成待办
     *
     * @param processId
     * @return
     */

    public EdocSummary getEdocSummaryByProcessId(Long processId) {
        StringBuilder hql = new StringBuilder();
        hql.append("from EdocSummary where processId=:processId");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("processId", processId.toString());
        List<EdocSummary> l = DBAgent.find(hql.toString(), map);
        if (Strings.isNotEmpty(l)) {
            return l.get(0);
        }
        return null;
    }

    /**
     * 是否能进行普通退回
     */
    public String[] edocCanStepBack(String workitemId, String processId, String nodeId, String caseId, String permissionAccount, String configCategory) {
        String[] result = null;
        try {
            result = WFAjax.canStepBack(workitemId, caseId, processId, nodeId, permissionAccount, configCategory);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 是否能够暂存代办
     *
     * @return
     */
    public String[] edocCanTemporaryPending(String workitemId) {
        String[] result = null;
        try {
            result = WFAjax.canTemporaryPending(workitemId);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 是否能够撤销
     */
    public String[] edocCanRepeal(String processId, String nodeId) {
        String[] result = null;
        try {
            result = WFAjax.canRepeal("edoc", processId, nodeId);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * @return
     * @是否能够进行提交
     */
    public String[] edocCanWorkflowCurrentNodeSubmit(String workitemId) {
        String[] result = null;
        try {
            result = WFAjax.canWorkflowCurrentNodeSubmit(workitemId);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    // OA-32768  test01调用公文模板发文后，处理时没有查看处理说明的入口
    public String getDealExplain(String affairId, String templeteId, String processId) {
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
            LOGGER.error("", e);
        }
        return desc;
    }

    public String edocElementsCount() {
        List<EdocElement> elementList = edocElementManager.getEdocElementsByStatus(1, 1, 10000);
        String msg = "";
        if (elementList.size() == 0) {
            msg = ResourceUtil.getString("edoc.no.edit.edoc.elements");
        } else {
            msg = "have";
        }
        return msg;
    }

    @Override
    public void transSetFinishedFlag(EdocSummary summary) throws BusinessException {
        this.edocSummaryDao.transSetFinishedFlag(summary);

        //流程结束的时候进行相关的文号操作，移动到历史表中。tdbug28578 以封发节点完成提交，作为流程结束标志。
        edocMarkHistoryManager.afterSend(summary);
    }

    /**
     * 对事项的状态进行判断，是否是有效的数据，主要是防止同时打开的情况
     */
    public String[] edocCanChangeNode(String workitemId) {
        String[] result = null;
        try {
            result = WFAjax.canChangeNode(workitemId);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public String[] canStopFlow(String workitemId) {
        String[] result = null;
        try {
            result = WFAjax.canStopFlow(workitemId);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }


    public String ajaxCheckNodeHasExchangeType(String edocTypeStr, String configItem, String accountId) throws BusinessException {
        String category = "";
        int edocType = Integer.parseInt(edocTypeStr);
        if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {
            category = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
            category = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (edocType == EdocEnum.edocType.signReport.ordinal()) {
            category = EnumNameEnum.edoc_qianbao_permission_policy.name();
        }
        List<String> baseActions = permissionManager.getBasicActionList(category, configItem, Long.parseLong(accountId));
        if (baseActions != null) {
            if (baseActions.contains("EdocExchangeType")) {
                return "true";
            }
        }
        return "false";
    }

    /**
     * 从待发中编辑，当前文单来源于模板时，判断系统模板是否存在
     *
     * @param templeteId
     * @return
     */
    public String isHaveSystemTemplate(long templeteId) {
        String isExist = "false";
        try {
            CtpTemplate templete = templateManager.getCtpTemplate(templeteId);
            if (!templete.isDelete() && templete.getState() != 1) {
                isExist = "true";
            }
        } catch (BusinessException e) {
            LOGGER.error("根据模板id获取模板出错 ", e);
        }
        return isExist;
    }

    public String isEdocHandle(String affairId) {
        String msg = "canHandle";
        if (Strings.isNotBlank(affairId)) {
            CtpAffair affair = null;
            try {
                affair = affairManager.get(Long.parseLong(affairId));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            if (affair == null || affair.isDelete() ||
                    (affair.getState().intValue() != StateEnum.col_pending.key()
                            && affair.getState().intValue() != StateEnum.col_sent.key()
                            && affair.getState().intValue() != StateEnum.col_waitSend.key()
                            && affair.getState().intValue() != StateEnum.col_done.key())) {
                msg = EdocHelper.getErrorMsgByAffair(affair);
            }
        }
        return msg;
    }

    /**
     * 收文登记时来文文号若有重复需要给出提醒
     *
     * @param docMark
     * @return
     */
    public String checkDocMarkIsUsedByRec(String docMark) {
        String msg = "notUse";
        User user = AppContext.getCurrentUser();
        int count = edocSummaryDao.checkDocMarkIsUsedByRec(1, docMark, user.getLoginAccount());
        if (count > 0) {
            msg = "used";
        }
        return msg;
    }

    /**************** 指定回退 start ******************/
    public void appointStepBack(Map<String, Object> tempMap) throws BusinessException {
        /*** 1、获取参数 **/
        Long summaryId = tempMap.get("summaryId") == null ? null : ((Number) tempMap.get("summaryId")).longValue();
        //String content = (String)tempMap.get("content");
        User user = AppContext.getCurrentUser();
        //String currentUserId = String.valueOf(user.getId());
        //String currentAccountId = String.valueOf(user.getLoginAccount());
        String selectTargetNodeId = (String) tempMap.get("selectTargetNodeId");
        String submitStyle = (String) tempMap.get("submitStyle");
        Long currentAffairId = tempMap.get("currentAffairId") == null ? null : ((Number) tempMap.get("currentAffairId")).longValue();
        EdocSummary summary = (EdocSummary) tempMap.get("summary");
        WorkflowBpmContext context = (WorkflowBpmContext) tempMap.get("context");
        Long workitemId = context.getCurrentWorkitemId();
        //Long caseId = context.getCaseId();
        Long processId = Long.parseLong(context.getProcessId());
        Long currentActivityId = Long.parseLong(context.getCurrentActivityId());

        AffairData affairData = EdocHelper.getAffairData(summary, user);
        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, affairData);
        context.setBusinessData("CRRENTAFFAIRID", currentAffairId);
        context.setBusinessData(AbstractEventListener.OPERATION_TYPE, "1".equals(submitStyle) ? AbstractEventListener.SPECIAL_BACK_SUBMITTO : AbstractEventListener.SPECIAL_BACK_RERUN);

        /*** 2、定义对象 **/
        CtpAffair currentAffair = affairManager.get(currentAffairId);
        CtpAffair sendAffair = affairManager.getSenderAffair(summaryId);
        CtpAffair _affair = affairManager.getAffairBySubObjectId(workitemId);
        ApplicationCategoryEnum app = EdocUtil.getAppCategoryByEdocType(summary.getEdocType());
        List<CtpAffair> allAvailableAffairs = affairManager.getValidAffairs(app, Long.valueOf(summaryId));

        /*** 3、保存公文意见 **/
        EdocOpinion signOpinion = (EdocOpinion) tempMap.get("signOpinion");
        //设置代理人信息
        Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), _affair.getMemberId());
        String agentName = null;
        if (agentMemberId != null) {
            agentName = orgManager.getMemberById(agentMemberId).getName();
            if (user.getId().longValue() != agentMemberId.longValue())
                agentName = null;
        }
        signOpinion.setProxyName(agentName);
        signOpinion.setCreateUserId(_affair.getMemberId());
        signOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
        signOpinion.setIdIfNew();
        EdocOpinion oldOpinion = tempMap.get("oldOpinion") == null ? null : (EdocOpinion) tempMap.get("oldOpinion");
        Map<String, Object> extMap = AffairUtil.getExtProperty(currentAffair);
        Object obj = extMap == null ? null : extMap.get(AffairExtPropEnums.edoc_lastOperateState.name());
        String lastOperateState = obj == null ? "" : (String) obj;
        this.saveOpinion(signOpinion, oldOpinion, false, lastOperateState);


        saveAttachmentsFromDetailDlg(summary, currentActivityId, signOpinion.getId());


        String _caseId[] = null;
        //Long affairId = 0L;
        V3xOrgMember member = null;
        ProcessLogAction logAction = null;
        String stepBackMember = "";
        boolean needUpdateAffair = false;
        /*** 4、指定回退状态变更 **/
        int way = Integer.parseInt(submitStyle);
        if ("start".equals(selectTargetNodeId)) {//指定回退给发起者节点
            logAction = ProcessLogAction.stepBackToSender;
            switch (way) {
                case 1:/** 直接提交给我 **/
                    //affairId = sendAffair.getId();
                    sendAffair.setSubState(SubStateEnum.col_pending_specialBacked.getKey());//16这个地方还要判断如果sate=1和substate=16流程不可编辑。只看查看
                    sendAffair.setState(StateEnum.col_waitSend.getKey());
                    sendAffair.setDelete(false);
                    sendAffair.setUpdateDate(new java.util.Date());
                    sendAffair.setPreApprover(currentAffair.getId());
                    this.affairManager.updateAffair(sendAffair);
                    /** 被回退人 **/
                    member = orgManager.getMemberById(sendAffair.getMemberId());
                    break;

                case 0:/** 流程重走 **/
                    if (Strings.isNotEmpty(allAvailableAffairs)) {
                        // 将summary的状态改为待发,撤销已生成事项
                        sendAffair.setState(StateEnum.col_waitSend.getKey());
                        sendAffair.setSubState(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
                        sendAffair.setDelete(Boolean.FALSE);
                        affairManager.updateAffair(sendAffair);
                        member = orgManager.getMemberById(sendAffair.getMemberId());
                        for (CtpAffair affair : allAvailableAffairs) {
                            if (affair.getRemindDate() != null && affair.getRemindDate() != 0) {
                                QuartzHolder.deleteQuartzJob("Remind" + affair.getId());
                            }
                            if ((affair.getDeadlineDate() != null && affair.getDeadlineDate() != 0) || affair.getExpectedProcessTime() != null) {
                                QuartzHolder.deleteQuartzJob("DeadLine" + affair.getId());
                            }
                        }
                        edocSummaryDao.saveOrUpdate(summary);
                        do4Repeal(user, signOpinion.getContent(), "edoc.summary.cancelPending", summary, allAvailableAffairs);
                        context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, EdocHelper.getAffairData(summary, user));//须更改状态

                        //登记数据处理
                        //从待发中编辑一个收文模板流程，发送时提示公文已分发，不允许重复分发，且确定后报js
                        if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()) {//退回到发起节点才会更新register的状态
                            makeRegisterToDraft(summary);
                        }
                    }
                    break;
            }
            /** 退回到发起者，流程日志中被退回人 **/
            stepBackMember = member == null ? "" : member.getName();
        } else {//指定回退给普通节点
            logAction = ProcessLogAction.colStepBackToPoint;
            switch (way) {
                case 1:/** 直接提交给我 **/
                    //affairId = _affair.getId();
                    _affair.setState(StateEnum.col_pending.key());
                    _affair.setSubState(SubStateEnum.col_pending_specialBack.getKey());
                    _affair.setPreApprover(currentAffair.getId());
                    break;

                case 0:/** 流程重走 **/
                    needUpdateAffair = true;
                    //affairId = _affair.getId();
                    //_affair.setDelete(Boolean.TRUE);
                    _affair.setState(StateEnum.col_stepBack.getKey());
                    _affair.setSubState(SubStateEnum.col_normal.getKey());
                    context.setBusinessData(EventDataContext.CTP_AFFAIR_DATA, EdocHelper.getAffairData(summary, user));//须更改状态
                    break;
            }
        }

        int operationType = way == 1 ? AbstractEventListener.SPECIAL_BACK_SUBMITTO : AbstractEventListener.SPECIAL_BACK_RERUN;
        context.setBusinessData(EventDataContext.CTP_AFFAIR_OPERATION_TYPE, operationType);

        /*** 6、指定回退流程变更 **/
        _caseId = wapi.stepBack(context);

        if ("0".equals(submitStyle)) {
            if (AppContext.hasPlugin("index")) {
                indexManager.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
            }
        }
        String selectTargetNodeName = _caseId[2];
        if (!"start".equals(selectTargetNodeId)) {
            stepBackMember = selectTargetNodeName;
        }

        if (needUpdateAffair) {
            affairManager.updateAffair(_affair);
        }

        int backStrategy = AppLogAction.Edoc_Appoint_Step_Back2.getKey();//357:直接提交给我,358:流程重走
        if ("1".equals(submitStyle)) {
            backStrategy = AppLogAction.Edoc_Appoint_Step_Back1.getKey();
        }
        /*** 7、记录应用日志 **/
        appLogManager.insertLog(user, backStrategy, user.getName(), summary.getSubject(), selectTargetNodeName);
        /*** 8、记录流程日志 **/
        String describe = "appLog.action." + backStrategy;
        if ("start".equals(selectTargetNodeId)) {//指定回退给发起者节点
            processLogManager.insertLog(AppContext.getCurrentUser(), processId,
                    currentActivityId, ProcessLogAction.colStepBackToSender, -1l, member == null ? "" : member.getName(), describe, summary.getSubject(), selectTargetNodeName, "true");
        } else {
            processLogManager.insertLog(AppContext.getCurrentUser(), processId, currentActivityId, ProcessLogAction.colStepBackToPoint, -1l, "", describe, summary.getSubject(), selectTargetNodeName, "true");
        }
        /*** 9、发送消息 **/
        tempMap.put("summary", summary);
        tempMap.put("allAvailableAffairs", allAvailableAffairs);
        tempMap.put("submitStyle", submitStyle);
        tempMap.put("selectTargetNodeId", selectTargetNodeId);
        tempMap.put("selectTargetNodeName", selectTargetNodeName);
        tempMap.put("currentAffair", currentAffair);
        tempMap.put("sendAffair", sendAffair);
        tempMap.put("businessData", context.getBusinessData());
        EdocMessageHelper.appointStepBackMsg(tempMap);

        //暂存待办，记录第一次操作时间
        affairManager.updateAffairAnalyzeData(currentAffair);

        try {
            if (summary != null) {
                EdocHelper.updateCurrentNodesInfo(summary, true);
            }
        } catch (Exception e) {
            LOGGER.error("指定回退更新当前待办人报错!", e);
        }
    }

    /**
     * 保存公文处理节点的附件
     *
     * @param summary
     * @param currentActivityId
     * @param signOpinionId
     */
    private void saveAttachmentsFromDetailDlg(EdocSummary summary, Long currentActivityId, Long signOpinionId) {
        try {

            Long summaryId = summary.getId();
            //修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(AppContext.getRawRequest());
            if (editHelper.hasEditAtt()) {//是否修改附件
                attachmentManager.deleteByReference(summaryId, summaryId);//删除公文附件
            }
            //保存公文附件及回执附件，create方法中前台subReference传值为空，默认从java中传过去， 因为公文附件subReference从前台js传值 过来了，而回执附件没有传subReference，所以这里传回执的id

            this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, signOpinionId, (AppContext.getRawRequest()));

            if (editHelper.hasEditAtt()) {//是否修改附件
                //设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(summary.getProcessId()), currentActivityId), true, "stepBack");
            }

        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private void do4Repeal(User user, String repealComment, String messageLink, EdocSummary summary, List<CtpAffair> aLLAvailabilityAffairList) throws BusinessException {
        List<Long> ids = new ArrayList<Long>();
        for (CtpAffair affair0 : aLLAvailabilityAffairList) {
            if ((Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair0.getState())
                    || Integer.valueOf(StateEnum.col_waitSend.getKey()).equals(affair0.getState()))
                    && !affair0.isDelete()) {
                continue;
            }
            ids.add(affair0.getId());
        }
        trackManager.deleteTrackMembersByAffairIds(ids);
        CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
        ids.add(sendAffair.getId());
        if (AppContext.hasPlugin("doc")) {
            docApi.deleteDocResources(user.getId(), ids);
        }
        //删除ISIgnatureHTML专业签章
        iSignatureHtmlManager.deleteAllByDocumentId(summary.getId());
        //流程撤销到首发，发送流程撤销事件
        CollaborationCancelEvent cancelEvent = new CollaborationCancelEvent(this);
        cancelEvent.setSummaryId(summary.getId());
        cancelEvent.setUserId(user.getId());
        cancelEvent.setMessage(repealComment);
        EventDispatcher.fireEvent(cancelEvent);
        //回退到发起节点时，如果有督办则修改督办状态
        this.superviseManager.updateStatus2Cancel(summary.getId());
    }

    /**
     * 保存督办
     */
    public void saveEdocSupervise4NewEdoc(EdocSummary summary, boolean sendMessage, Map<String, Object> param) throws BusinessException {
        try {
            //Map superviseMap = ParamUtil.getJsonDomain("colMainData");
            Map superviseMap = summary.getSuperviseMap();
            SuperviseSetVO ssvo = (SuperviseSetVO) ParamUtil.mapToBean(superviseMap, new SuperviseSetVO(), false);
            SuperviseMessageParam smp = new SuperviseMessageParam();
            if (sendMessage) {
                smp.setSendMessage(true);
                smp.setMemberId(summary.getStartUserId());
                smp.setImportantLevel(summary.getImportantLevel());
                smp.setSubject(summary.getSubject());
                smp.setForwardMember(summary.getForwardMember());
            }
            superviseManager.saveOrUpdateSupervise4Process(ssvo, smp, summary.getId(), SuperviseEnum.EntityType.edoc, param);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /*****************指定回退 end ********************/

    public String getDeptSenders(String summaryIdStr) throws BusinessException {
        EdocSummary summary = edocSummaryDao.get(Long.parseLong(summaryIdStr));
        Set<Long> deptSenderList = new HashSet<Long>();

        /* xiangfan 添加  修复 GOV-4911 Start */
        deptSenderList = EdocHelper.getDeptSenderList(summary.getStartUserId(), summary.getOrgAccountId());
        Iterator<Long> iterator = deptSenderList.iterator();
        Long deptId;
        StringBuilder deptList = new StringBuilder();
        while (iterator.hasNext()) {
            Object deptObj = iterator.next();
            deptId = deptObj == null ? null : ((Number) deptObj).longValue();
            V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
            if (dept != null) {
                if (deptList.length() > 0) {
                    deptList.append("|");
                }
                deptList.append(dept.getId());
                deptList.append(",");
                deptList.append(dept.getName());
            }
        }

        return deptList.toString();
    }

    /**
     * 获取指定分钟数后的日期
     *
     * @param minutes
     * @return
     */
    public String calculateWorkDatetime(String minutesStr) throws BusinessException {
        java.util.Date newDate = new java.util.Date();
        int minutes = 0;
        if (Strings.isNotBlank(minutesStr)) {
            minutes = Integer.valueOf(minutesStr);
            Long currentAccuntId = AppContext.getCurrentUser().getLoginAccount();
            int workDayCount = workTimeManager.getWorkDaysByWeek();
            switch (minutes) {
                case 5:
                case 10:
                case 15:
                case 30:
                    newDate = workTimeManager.getCompleteDate4Worktime(new java.util.Date(), minutes, currentAccuntId);
                    break;
                case 60://1小时
                case 120://2小时
                case 180://3小时
                case 240://4小时
                case 300://5小时
                case 360://6小时
                case 420://7小时
                case 480://8小时
                case 720://0.5天
                    long hours = minutes / 60;
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", hours, "hour", currentAccuntId);
                    break;
                case 1440://1天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 1, "day", currentAccuntId);
                    break;
                case 2880://2天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 2, "day", currentAccuntId);
                    break;
                case 4320://3天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 3, "day", currentAccuntId);
                    break;
                case 5760://4天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 4, "day", currentAccuntId);
                    break;
                case 7200://5天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 5, "day", currentAccuntId);
                    break;
                case 8640://6天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 6, "day", currentAccuntId);
                    break;
                case 14400://10天
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 10, "day", currentAccuntId);
                    break;
                case 20160://2周
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", workDayCount * 2, "day", currentAccuntId);
                    break;
                case 21600://半个月
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 15, "day", currentAccuntId);
                    break;
                case 30240://3周
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", workDayCount * 3, "day", currentAccuntId);
                    break;
                case 43200://1个月
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 30, "day", currentAccuntId);
                    break;
                case 86400://2个月
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 60, "day", currentAccuntId);
                    break;
                case 129600://3个月
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", 90, "day", currentAccuntId);
                    break;
                case 10080://1周
                default://1周
                    newDate = workTimeManager.getComputeDate(new java.util.Date(), "+", workDayCount, "day", currentAccuntId);
                    break;
            }
        }
        return Datetimes.formatDatetimeWithoutSecond(newDate);
    }

    /**
     * 获取公文的附件和关联文档的个数
     */
    public String getEdocAttSizeAndAttDocSize(long edocId) {
        int attSize = 0;
        int attDocSize = 0;
        List<Attachment> atts = attachmentManager.getByReference(edocId);
        if (Strings.isNotEmpty(atts)) {
            for (Attachment att : atts) {
                if (att.getType() == 0) {
                    attSize++;
                } else if (att.getType() == 2) {
                    attDocSize++;
                }
            }
        }
        return attSize + "," + attDocSize;
    }

    public String checkHasSignaturehtml(long edocId) {
        int isgnatueCount = iSignatureHtmlManager.getISignCount(edocId);
        if (isgnatueCount > 0) {
            return "yes";
        } else {
            return "no";
        }
    }

    public ISignatureHtmlManager getiSignatureHtmlManager() {
        return iSignatureHtmlManager;
    }

    public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
        this.iSignatureHtmlManager = iSignatureHtmlManager;
    }

    @Override
    public String ajaxCalcuteNatureDatetime(String fromDate, Long minutes) {
        java.util.Date newDate = new java.util.Date();
        if (Strings.isNotBlank(fromDate)) {
            java.util.Date toDate = Datetimes.parseDatetimeWithoutSecond(fromDate);
            newDate = workTimeManager.getRemindDate(toDate, minutes);
        }
        return Datetimes.formatDatetimeWithoutSecond(newDate);
    }

    @Override
    public List<AttachmentVO> getAttachmentListBySummaryId(Long summaryId, String parameter)
            throws BusinessException {
        List<Attachment> tempattachments = null;
        List<Attachment> attachments = new ArrayList<Attachment>();
        List<Attachment> tempattachmentsCon = new ArrayList<Attachment>();
        List<AttachmentVO> attachmentVOs = new ArrayList<AttachmentVO>();
        tempattachments = attachmentManager.getByReference(summaryId, summaryId);
        //添加附件到对象中，附件的type为0，关联文档的type为2（不显示关联文档在附件列表中）
        for (Attachment attachment : tempattachments) {
            if (attachment.getType() == 0) {
                attachments.add(attachment);
            }
        }
        List<EdocBody> bodysById = edocBodyDao.getBodysById(summaryId);
        if (null != bodysById && bodysById.size() > 0) {
            EdocBody edocBody = bodysById.get(0);
            String content = edocBody.getContent();

            //Pattern p = Pattern.compile("a\\s+attachmentid\\s*=\\s*('|\")([-\\d]+)('|\")");
            Pattern p = Pattern.compile("attachmentid\\s*=\\s*('|\")([-\\d]+)('|\")");
            if (Strings.isNotBlank(content)) {
                Matcher m = p.matcher(content);
                List<Long> urlLon = new ArrayList<Long>();
                while (m.find()) {
                    String strId = m.group(2);
                    if (Strings.isNotBlank(strId)) {
                        urlLon.add(Long.valueOf(strId));
                    }
                }
                if (urlLon.size() > 0) {
                    List<Attachment> attachmentByFileURLs = attachmentManager.getAttachmentByFileURLs(urlLon);
                    for (Attachment attachment : attachmentByFileURLs) {
                        if (attachment.getType() == 0) {
                            tempattachmentsCon.add(attachment);
                        }
                    }
                }
            }

        }
        AttachmentVO vo = null;
        EdocSummary _edocSummary = getEdocSummaryById(summaryId, false);
        for (Attachment attachment : attachments) {//标题下方区
            vo = new AttachmentVO();
            createAttachmentVO(vo, attachment);
            V3XFile v3xFile = fileManager.getV3XFile(Long.valueOf(vo.getFileUrl()));
            try {
                vo.setUserName(orgManager.getMemberById(v3xFile.getCreateMember()).getName());
            } catch (Exception e) {
                vo.setUserName(_edocSummary.getStartMember().getName());
            }
            vo.setFromType(ResourceUtil.getString("collaboration.att.titleArea"));//标题区
            attachmentVOs.add(vo);
        }
        for (Attachment attachment : tempattachmentsCon) {//正文内容区
            vo = new AttachmentVO();
            createAttachmentVO(vo, attachment);
            V3XFile v3xFile = fileManager.getV3XFile(Long.valueOf(vo.getFileUrl()));
            try {
                vo.setUserName(orgManager.getMemberById(v3xFile.getCreateMember()).getName());
            } catch (Exception e) {
                vo.setUserName(_edocSummary.getStartMember().getName());
            }
            vo.setFromType(ResourceUtil.getString("collaboration.att.form"));//正文区
            attachmentVOs.add(vo);
        }


        if (Strings.isNotBlank(parameter) && !"undefined".equals(parameter)) {
            String[] split = parameter.split(",");

            List<Long> _attId = new ArrayList<Long>();
            for (String s : split) {
                _attId.add(Long.valueOf(s));
            }
            List<Attachment> attachmentByFileURLs = attachmentManager.getAttachmentByFileURLs(_attId);
            for (Attachment att : attachmentByFileURLs) {
                if (att.getType() == 0) {
                    vo = new AttachmentVO();
                    createAttachmentVO(vo, att);
                    if (null != att.getSubReference()) {
                        EdocOpinion edocOpinion = edocOpinionDao.get((att.getSubReference()));
                        if (null != edocOpinion) {
                            if (0l != edocOpinion.getCreateUserId()) {
                                vo.setUserName(orgManager.getMemberById(edocOpinion.getCreateUserId()).getName());
                            }
                            if (Integer.valueOf(EdocOpinion.OpinionType.senderOpinion.ordinal()).equals(edocOpinion.getOpinionType())) {
                                vo.setFromType(ResourceUtil.getString("collaboration.att.sender"));
                            } else {
                                vo.setFromType(ResourceUtil.getString("collaboration.att.opinion"));
                            }
                            attachmentVOs.add(vo);
                        }
                    }
                }
            }
        }
        //排序
        //	Collections.sort(attachmentVOs, new AttachmentVO());
        return attachmentVOs;
    }

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
        if (Strings.isNotBlank(extension)) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        vo.setFileFullName(fileName);
        vo.setFileName(Strings.getSafeLimitLengthString(fileName, 25, "..."));
        vo.setFileUrl(String.valueOf(attachment.getFileUrl()));
        vo.setV(String.valueOf(attachment.getV()));
    }

    public String checkAffairValid(String affairId, String pageNodePolicy) {
        CtpAffair affair = null;
        String errorMsg = "";
        if (Strings.isNotBlank(affairId)) {
            try {
                affair = affairManager.get(Long.valueOf(affairId));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            if (!ColUtil.isAfffairValid(affair)) {
                errorMsg = ColUtil.getErrorMsgByAffair(affair);
            }

            if (Strings.isBlank(errorMsg) && Strings.isNotEmpty(pageNodePolicy)) {
                String affairNodePolicy = affair.getNodePolicy();
                if ("inform".equals(affairNodePolicy)) {
                    affairNodePolicy = "zhihui";
                }
                if ("inform".equals(pageNodePolicy)) {
                    pageNodePolicy = "zhihui";
                }
                if (Strings.isNotEmpty(affairNodePolicy) && !affairNodePolicy.equals(pageNodePolicy)) {
                    errorMsg = ResourceUtil.getString("edoc.NodePolicy.change");
                }
            }
        }
        return errorMsg;
    }

    /**
     * 获取指定跟踪人
     *
     * @param docMark
     * @return
     */
    public String getTrackName(String params) {
        StringBuilder userName = new StringBuilder();
        if (Strings.isNotBlank(params)) {
            String[] str = params.split(",");
            try {
                for (int i = 0; i < str.length; i++) {
                    V3xOrgMember member = orgManager.getMemberById(Long.valueOf(str[i]));
                    userName.append(member.getName());
                    userName.append(",");
                }
            } catch (NumberFormatException e) {
                LOGGER.error("通过用户ID获取用户类型转换错误", e);
            } catch (BusinessException e) {
                LOGGER.error("通过用户ID获取用户对象出错", e);
            }
            return userName.substring(0, userName.length() - 1);
        }
        return userName.toString();
    }

    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }

    @Override
    public FlipInfo getSendRegisterData(FlipInfo flipInfo, Map<String, String> paramMap) throws BusinessException {

        User user = AppContext.getCurrentUser();
        List<SummaryModel> sendRegisterDataList = new ArrayList<SummaryModel>();

        final List<EdocSummaryModel> edocSummaryModels = this.searchSendRegisterData(flipInfo, paramMap, user);

        if (Strings.isNotEmpty(edocSummaryModels)) {
            for (EdocSummaryModel edocModel : edocSummaryModels) {
                SummaryModel summaryModel = new SummaryModel();
                summaryModel.trans2SummaryModel(edocModel);
                sendRegisterDataList.add(summaryModel);
            }
        }
        flipInfo.setData(sendRegisterDataList);
        return flipInfo;
    }

    @Override
    public FlipInfo getRecRegisterData(FlipInfo flipInfo, Map<String, String> paramMap) throws BusinessException {
        User user = AppContext.getCurrentUser();
        List<SummaryModel> sendRegisterDataList = new ArrayList<SummaryModel>();
        final List<EdocSummaryModel> edocSummaryModels = this.searchRecRegisterData(flipInfo, paramMap, user);

        if (Strings.isNotEmpty(edocSummaryModels)) {
            for (EdocSummaryModel edocModel : edocSummaryModels) {
                SummaryModel summaryModel = new SummaryModel();
                summaryModel.trans2SummaryModel(edocModel);
                sendRegisterDataList.add(summaryModel);
            }
        }
        flipInfo.setData(sendRegisterDataList);
        return flipInfo;
    }

    @Override
    public List<SummaryModel> queryRegisterData(int edocType, Map<String, String> queryParams, User user) {

        List<SummaryModel> retList = new ArrayList<SummaryModel>();
        List<EdocSummaryModel> edocSummaryModels = null;
        if (com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC == edocType) {//收文

            edocSummaryModels = this.searchRecRegisterData(null, queryParams, user);

        } else if (com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND == edocType) {//收文

            edocSummaryModels = this.searchSendRegisterData(null, queryParams, user);

        }

        if (Strings.isNotEmpty(edocSummaryModels)) {

            if (Strings.isNotEmpty(edocSummaryModels)) {
                for (EdocSummaryModel edocModel : edocSummaryModels) {
                    SummaryModel summaryModel = new SummaryModel();
                    summaryModel.trans2SummaryModel(edocModel);
                    retList.add(summaryModel);
                }
            }
        }

        return retList;
    }

    /**
     * 收文登记薄查询语句拼接
     *
     * @param paramMap    查询条件map
     * @param hql         查询语句
     * @param queryParams 查询语句 对应的传参
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月16日下午5:53:31
     */
    private String searcRecRegistQuerySet(Map<String, String> paramMap, StringBuffer hql,
                                          Map<String, Object> queryParams) {

        String orderSql = null;// 排序SQL

        String condition = paramMap.get("condition");

        if (Strings.isNotBlank(condition)) {// 值不用Strings, 用户可能输入空格

            if (isNotBlankQuery(paramMap, "subject")) {// 公文标题

                String searchValue = paramMap.get("subject");
                hql.append(" and (summary.subject like :subject) ");
                queryParams.put("subject", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "docMark")) {// 来文文号

                String searchValue = paramMap.get("docMark");
                hql.append(" and (summary.docMark like :docMark) ");
                queryParams.put("docMark", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "serialNo")) {// 收文编号

                String searchValue = paramMap.get("serialNo");
                hql.append(" and (summary.serialNo like :serialNo) ");
                queryParams.put("serialNo", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "sendUnit")) {// 来文单位

                String searchValue = paramMap.get("sendUnit");
                hql.append(" and (summary.sendUnit like :sendUnit) ");
                queryParams.put("sendUnit", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "undertaker")) {//承办人
                String searchValue = paramMap.get("undertaker");
                hql.append(" and (summary.undertaker like :undertaker) ");
                queryParams.put("undertaker", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if (isNotBlankQuery(paramMap, "createTime")) {//G6分发日期， A8登记日期

                String searchValue = paramMap.get("createTime");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {// 开始时间
                    hql.append(" and (summary.createTime >= :createTimeBegin) ");
                    queryParams.put("createTimeBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {// 结束时间
                    hql.append(" and (summary.createTime <= :createTimeEnd) ");
                    queryParams.put("createTimeEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
            }

            if (isNotBlankQuery(paramMap, "distributer")) {//G6分发人， A8登记人

                String searchValue = paramMap.get("distributer");
                hql.append(" and (register.distributer like :distributer) ");
                queryParams.put("distributer", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if (isNotBlankQuery(paramMap, "recieveDate")) {// 签收日期

                String searchValue = paramMap.get("recieveDate");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {// 开始时间
                    hql.append(" and (register.recTime >= :recTimeBegin) ");
                    queryParams.put("recTimeBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {// 结束时间
                    hql.append(" and (register.recTime <= :recTimeEnd) ");
                    queryParams.put("recTimeEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
            }

            if (isNotBlankQuery(paramMap, "recieveUserName")) {//签收人

                String searchValue = paramMap.get("recieveUserName");
                hql.append(" and (register.recieveUserName like :recieveUserName) ");
                queryParams.put("recieveUserName", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if (isNotBlankQuery(paramMap, "undertakenoffice")) {//承办机构

                String searchValue = paramMap.get("undertakenoffice");
                hql.append(" and (summary.undertakenoffice like :undertakenoffice) ");
                queryParams.put("undertakenoffice", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            //G6登记人，登记日期
            if (isNotBlankQuery(paramMap, "registerDate")) {//G6登记人，登记日期

                String searchValue = paramMap.get("registerDate");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {// 开始时间
                    hql.append(" and (register.registerDate >= :registerDateBegin) ");
                    queryParams.put("registerDateBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {// 结束时间
                    hql.append(" and (register.registerDate <= :registerDateEnd) ");
                    queryParams.put("registerDateEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
            }

            if (isNotBlankQuery(paramMap, "registerDateList")) {//G6登记人，登记日期 老数据列表查询，只有升级数据才用到这个查询

                String searchValue = paramMap.get("registerDateList");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {// 开始时间
                    hql.append(" and (register.registerDate >= :registerDateListBegin) ");
                    queryParams.put("registerDateListBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {// 结束时间
                    hql.append(" and (register.registerDate <= :registerDateListEnd) ");
                    queryParams.put("registerDateListEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
            }

            if (isNotBlankQuery(paramMap, "registerUserName")) {//G6 登记人

                String searchValue = paramMap.get("registerUserName");
                hql.append(" and (register.registerUserName like :registerUserName) ");
                queryParams.put("registerUserName", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if (isNotBlankQuery(paramMap, "sendTo")) {//老数据升级  - 收文单位 只有升级数据才有这个查询条件

                String searchValue = paramMap.get("sendTo");
                hql.append(" and (summary.sendTo like :sendTo) ");
                queryParams.put("sendTo", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if ("compQuery".equals(condition)) {// 复合查询采用默认排序
                orderSql = null;
            }
        }

        return orderSql;
    }

    /**
     * 查询收文登记薄数据
     *
     * @param flipInfo
     * @param paramMap
     * @param user
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月9日下午1:31:38
     */
    private List<EdocSummaryModel> searchRecRegisterData(FlipInfo flipInfo, Map<String, String> paramMap, User user) {

        List<EdocSummaryModel> retList = new ArrayList<EdocSummaryModel>();// 返回对象

        StringBuffer hql = new StringBuffer();
        Map<String, Object> queryParams = new HashMap<String, Object>();// 查询条件
        Long accountId = user.getLoginAccount();

        int edocType = com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC;

        hql.append("select ");
        hql.append("summary.subject, summary.serialNo, summary.secretLevel, summary.keepPeriod, summary.urgentLevel ");
        hql.append(",summary.sendUnit, summary.docMark, summary.copies, summary.sendTo, summary.copyTo ");
        hql.append(",summary.issuer, summary.keywords, summary.undertaker, summary.docType, register.signer ");
        hql.append(",register.registerUserName, register.registerDate, register.distributer, summary.registrationDate ");
        hql.append(",summary.id, register.sendUnitType,summary.createTime,summary.identifier ");
        hql.append(",summary.undertakenoffice,register.recTime, register.recieveUserName,summary.unitLevel,register.exchangeSendTime ");
        hql.append(" from ");
        hql.append("EdocRegister register,EdocSummary   summary ");
        hql.append(" where ");
        hql.append(" register.distributeEdocId = summary.id");

        hql.append(" and register.edocType = :edocType ");
        queryParams.put("edocType", edocType);

        hql.append(" and register.state in( :registerState ) ");
        List<Integer> registerStatList = new ArrayList<Integer>();
        registerStatList.add(EdocNavigationEnum.RegisterState.Registed.ordinal());// 已经登记的
        registerStatList.add(EdocNavigationEnum.RegisterState.deleted.ordinal()); // 登记后删除的
        registerStatList.add(10); // 分发后删除的
        registerStatList.add(EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC); // 纸质分发
        queryParams.put("registerState", registerStatList);

        hql.append(" and register.orgAccountId = :orgAccountId ");
        queryParams.put("orgAccountId", accountId);

        // 查询条件处理
        searcRecRegistQuerySet(paramMap, hql, queryParams);

        String dataRight = paramMap.get("dataRight");//推送的权限范围

        try {

            String departmentIds = null;
            if (EdocRoleHelper.isAccountExchange(user.getId())) {

                departmentIds = dataRight;

            } else {

                String myExchangeDept = EdocRoleHelper.getUserExchangeDepartmentIds();

                //部门推送 其他部门收发员查看数据取交集
                if (Strings.isNotBlank(dataRight) && Strings.isNotBlank(myExchangeDept)) {
                    StringBuilder watchDataRight = new StringBuilder();
                    String[] deptArray = myExchangeDept.split(",");
                    for (String dept : deptArray) {
                        if (dataRight.contains(dept)) {

                            if (watchDataRight.length() > 0) {
                                watchDataRight.append(",");
                            }
                            watchDataRight.append(dept);
                        }
                    }
                    departmentIds = watchDataRight.toString();
                } else {
                    departmentIds = myExchangeDept;
                }
            }

            if (Strings.isNotBlank(departmentIds)) {
                String[] depIds = departmentIds.split("[,]");
                if (depIds.length > 0) {
                    hql.append(" and summary.orgDepartmentId in (:orgDepartmentIds)");
                    List<Long> orgDepartmentIdList = new ArrayList<Long>();
                    for (int i = 0; i < depIds.length; i++) {
                        orgDepartmentIdList.add(Long.parseLong(depIds[i]));
                    }
                    queryParams.put("orgDepartmentIds", orgDepartmentIdList);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        hql.append(" order by summary.createTime desc");

        List<Object[]> result = null;

        if (flipInfo == null) {//不分页查询
            result = DBAgent.find(hql.toString(), queryParams);
        } else {
            result = DBAgent.find(hql.toString(), queryParams, flipInfo);
        }

        if (Strings.isNotEmpty(result)) {

            for (int i = 0; i < result.size(); i++) {
                Object[] object = result.get(i);

                int n = 0;

                EdocSummary summary = new EdocSummary();
                summary.setSubject((String) object[n++]);//summary.subject
                summary.setSerialNo((String) object[n++]);//summary.serialNo
                summary.setSecretLevel((String) object[n++]);//summary.secretLevel
                Integer kp = (Integer) object[n++];//summary.keepPeriod
                // OA-29574 收文登记簿--查询报错
                summary.setKeepPeriod(kp);
                summary.setUrgentLevel((String) object[n++]);////summary.urgentLevel
                summary.setSendUnit((String) object[n++]);//summary.sendUnit
                summary.setDocMark((String) object[n++]);//summary.docMark
                summary.setCopies((Integer) object[n++]);//summary.copies
                summary.setSendTo((String) object[n++]);//summary.sendTo
                summary.setCopyTo((String) object[n++]);//summary.copyTo
                summary.setIssuer((String) object[n++]);//summary.issuer
                summary.setKeywords((String) object[n++]);//summary.keywords
                summary.setUndertaker((String) object[n++]);//summary.undertaker
                summary.setDocType((String) object[n++]);//summary.docType


                try {
                    // TODO__承办单位和承办部门
                } catch (Exception e) {
                    summary.setUndertakerAccount(null);
                    summary.setUndertakerDep(null);
                }

                summary.setEdocType(edocType);


                // 开始组装最后返回的结果
                EdocSummaryModel model = new EdocSummaryModel();

                model.setSummary(summary);

                model.setSigner((String) object[n++]);//register.signer
                model.setRegisterUserName((String) object[n++]);//register.registerUserName
                model.setRegisterDate((java.sql.Date) object[n++]);//register.registerDate
                model.setDistributer((String) object[n++]);//register.distributer
                summary.setRegistrationDate((java.sql.Date) object[n++]);//summary.RegistrationDate
//                model.setAffairId(object[n]==null ? null : ((Number) object[n]).longValue());//affair.id
//                n++;
                summary.setId(object[n] == null ? null : ((Number) object[n]).longValue());//affair.objectId
                n++;
                model.setState(StateEnum.col_sent.key());//affair.state
                model.setSendUnitType((Integer) object[n++]);//register.sendUnitType
                summary.setCreateTime((java.sql.Timestamp) object[n++]);//summary.createTime
                summary.setIdentifier((String) object[n++]);//summary.identifier
                summary.setUndertakenoffice((String) object[n++]);//summary.undertakenoffice
                model.setRecTime((Timestamp) object[n++]);//register.recTime
                model.setRecieveUserName((String) object[n++]);//register.recieveUserName
                summary.setUnitLevel((String) object[n++]);
                summary.setSendTime((java.sql.Timestamp) object[n++]);
                /*Integer eMode = (Integer) object[n++];
                if(eMode == null){//新增的字段，默认值为0，兼容老字段
                    eMode = EdocExchangeModeEnum.internal.getKey();
                }
                String exchangeModeName = "";
                // 当exchangeMode  为 0时 ，则是内部公文交换，为1是书生系统交换
                if(eMode == EdocExchangeModeEnum.internal.getKey()){
                	exchangeModeName = ResourceUtil.getString("edoc.exchangeMode.internal");//内部公文交换
                }else if(eMode == EdocExchangeModeEnum.sursen.getKey()){
                	exchangeModeName = ResourceUtil.getString("edoc.exchangeMode.sursen");// 书生系统交换
                }
                model.setExchangeMode(exchangeModeName);*/
                String sendToUnit = "";
                if (!Strings.isBlank(summary.getSendTo())) {
                    sendToUnit = summary.getSendTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getCopyTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getCopyTo();
                    }
                } else {
                    sendToUnit = summary.getCopyTo();
                }
                if (!Strings.isBlank(sendToUnit)) {
                    if (!Strings.isBlank(summary.getReportTo())) {
                        sendToUnit = sendToUnit + "、" + summary.getReportTo();
                    }
                } else {
                    sendToUnit = summary.getReportTo();
                }

                validateEnmus(model);//进行枚举检验，老数据可能存在枚举为-1的情况

                model.setSendToUnit(sendToUnit);

                retList.add(model);
            }
        }
        return retList;
    }

    /**
     * 验证查询条件是否为空
     *
     * @param paramMap
     * @param queryField
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月16日下午2:59:16
     */
    private boolean isNotBlankQuery(Map<String, String> paramMap, String queryField) {

        boolean ret = false;

        String value = paramMap.get(queryField);

        if (value != null && !"".equals(value)) {
            ret = true;
        }

        return ret;
    }

    /**
     * 发文登记薄
     *
     * @param paramMap
     * @param hql
     * @param queryParams
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月16日上午10:41:59
     */
    private String searcSendRegistQuerySet(Map<String, String> paramMap, StringBuffer hql, Map<String, Object> queryParams) {

        String orderSql = null;//排序SQL

        String condition = paramMap.get("condition");

        if (Strings.isNotBlank(condition)) {//值不用Strings, 用户可能输入空格

            if (isNotBlankQuery(paramMap, "subject")) {//公文标题

                String searchValue = paramMap.get("subject");
                hql.append(" and (summary.subject like :subject) ");
                queryParams.put("subject", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "docMark")) {//公文文号

                String searchValue = paramMap.get("docMark");
                hql.append(" and (summary.docMark like :docMark) ");
                queryParams.put("docMark", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "serialNo")) {//内部文号

                String searchValue = paramMap.get("serialNo");
                hql.append(" and (summary.serialNo like :serialNo) ");
                queryParams.put("serialNo", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "issuer")) {//签发人

                String searchValue = paramMap.get("issuer");
                hql.append(" and (summary.issuer like :issuer) ");
                queryParams.put("issuer", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "sendUnit")) {//发文单位

                String searchValue = paramMap.get("sendUnit");

                hql.append(" and (summary.sendUnit like :sendUnit or summary.sendUnit2 like :sendUnit2) ");

                queryParams.put("sendUnit", "%" + SQLWildcardUtil.escape(searchValue) + "%");
                queryParams.put("sendUnit2", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "startTime")) {//拟文日期

                String searchValue = paramMap.get("startTime");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {//开始时间
                    hql.append(" and (summary.startTime >= :startTimeBegin) ");
                    queryParams.put("startTimeBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {//结束时间
                    hql.append(" and (summary.startTime <= :startTimeEnd) ");
                    queryParams.put("startTimeEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
                orderSql = " order by summary.startTime desc ";

            }

            if (isNotBlankQuery(paramMap, "createTime")) {//发起日期，只有升级数据才用到这个查询条件

                String searchValue = paramMap.get("createTime");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {//开始时间
                    hql.append(" and (summary.createTime >= :createTimeBegin) ");
                    queryParams.put("createTimeBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {//结束时间
                    hql.append(" and (summary.createTime <= :createTimeEnd) ");
                    queryParams.put("createTimeEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
            }

            if (isNotBlankQuery(paramMap, "createTimeList")) {//发起日期，只有升级数据的小查询才用到这个查询条件

                String searchValue = paramMap.get("createTimeList");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {//开始时间
                    hql.append(" and (summary.createTime >= :createTimeListBegin) ");
                    queryParams.put("createTimeListBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {//结束时间
                    hql.append(" and (summary.createTime <= :createTimeListEnd) ");
                    queryParams.put("createTimeListEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }
            }

            if (isNotBlankQuery(paramMap, "signingDate")) {//签发日期

                String searchValue = paramMap.get("signingDate");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {//开始时间
                    hql.append(" and (summary.signingDate >= :signingDateBegin) ");
                    queryParams.put("signingDateBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {//结束时间
                    hql.append(" and (summary.signingDate <= :signingDateEnd) ");
                    queryParams.put("signingDateEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }

                orderSql = " order by summary.signingDate desc ";

            }

            if (isNotBlankQuery(paramMap, "sendTime")) {//交换日期

                String searchValue = paramMap.get("sendTime");

                String[] valueArray = searchValue.split(",");
                if (valueArray.length > 0 && Strings.isNotBlank(valueArray[0])) {//开始时间
                    hql.append(" and (send.sendTime >= :sendTimeBegin) ");
                    queryParams.put("sendTimeBegin", Datetimes.getTodayFirstTime(valueArray[0]));
                }

                if (valueArray.length > 1 && Strings.isNotBlank(valueArray[1])) {//结束时间
                    hql.append(" and (send.sendTime <= :sendTimeEnd) ");
                    queryParams.put("sendTimeEnd", Datetimes.getTodayLastTime(valueArray[1]));
                }

                orderSql = " order by send.sendTime desc ";

            }

            if (isNotBlankQuery(paramMap, "departmentName")) {//发文部门

                String searchValue = paramMap.get("departmentName");

                hql.append(" and (summary.sendDepartment like :sendDepartment or summary.sendDepartment2 like :sendDepartment2) ");
                queryParams.put("sendDepartment", "%" + SQLWildcardUtil.escape(searchValue) + "%");
                queryParams.put("sendDepartment2", "%" + SQLWildcardUtil.escape(searchValue) + "%");

            }

            if (isNotBlankQuery(paramMap, "urgentLevel")) {//紧急程度

                String searchValue = paramMap.get("urgentLevel");
                hql.append(" and (summary.urgentLevel like :urgentLevel) ");
                queryParams.put("urgentLevel", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if (isNotBlankQuery(paramMap, "secretLevel")) {//密级

                String searchValue = paramMap.get("secretLevel");
                hql.append(" and (summary.secretLevel like :secretLevel) ");
                queryParams.put("secretLevel", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }

            if (isNotBlankQuery(paramMap, "createPerson")) {//拟稿人

                String searchValue = paramMap.get("createPerson");
                hql.append(" and (summary.createPerson like :createPerson) ");
                queryParams.put("createPerson", "%" + SQLWildcardUtil.escape(searchValue) + "%");
            }
            /*if(isNotBlankQuery(paramMap, "exchangeMode")){//交换方式

                String searchValue = paramMap.get("exchangeMode");
                hql.append(" and (send.exchangeMode = :exchangeMode) ");
                queryParams.put("exchangeMode", Integer.valueOf(searchValue));

            }*/

            //送文人
            if (isNotBlankQuery(paramMap, "sendperson")) {
                String sendpersonValue = paramMap.get("sendperson");
                String queryPersonHql = "select o.id from OrgMember o where o.name like :sendPersonName";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sendPersonName", "%" + SQLWildcardUtil.escape(sendpersonValue) + "%");

                List<Object> results = DBAgent.find(queryPersonHql, params);

                if (Strings.isNotEmpty(results)) {
                    hql.append(" and (");
                    int index = 0;
                    int step = 999;
                    while (index < results.size()) {//分页，Oracle  in查询有1000 限制
                        int toIndex = index + step;
                        if (toIndex > results.size()) {
                            toIndex = results.size();
                        }
                        List<Object> subList = results.subList(index, toIndex);

                        List<Long> idLists = new ArrayList<Long>();
                        for (Object o : subList) {
                            idLists.add(o == null ? null : ((Number) o).longValue());
                        }
                        if (index != 0) {
                            hql.append(" or");
                        }
                        hql.append(" send.sendUserId in (:sendUserId" + index + ") ");
                        queryParams.put("sendUserId" + index, idLists);

                        index = toIndex;
                    }
                    hql.append(")");
                } else {
                    hql.append(" and 1=0 ");//没有数据
                }
            }

            if ("compQuery".equals(condition)) {//复合查询采用默认排序
                orderSql = null;
            }
        }

        return orderSql;
    }

    /**
     * 查找发文登记薄数据
     *
     * @param flipInfo
     * @param paramMap
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月9日上午12:46:36
     */
    private List<EdocSummaryModel> searchSendRegisterData(FlipInfo flipInfo, Map<String, String> paramMap, User user) {

        List<EdocSummaryModel> retList = new ArrayList<EdocSummaryModel>();// 返回对象

        StringBuffer hql = new StringBuffer();
        String orderSql = null;//排序SQL
        Map<String, Object> queryParams = new HashMap<String, Object>();// 查询条件
        Long accountId = user.getLoginAccount();

        hql.append("select ");
        hql.append(selectSummary);
        hql.append(",summary.unitLevel,send.sendTime,send.sendedTypeIds,summary.keepPeriod,summary.sendDepartment,send.sendUserId");
        hql.append(" from ");
        hql.append("EdocSummary as summary left join summary.edocSendRecords as send");
        hql.append(" where ");
        hql.append(" summary.edocType = :edocType ");
        queryParams.put("edocType", com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND);

        hql.append(" and summary.completeTime is not null ");
        hql.append(" and ((send.status is null) or ( send.isBase=" + EdocSendRecord.Exchange_Base_YES + ")) ");

        //查询条件过滤...
        searcSendRegistQuerySet(paramMap, hql, queryParams);

        String dataRight = paramMap.get("dataRight");//推送的权限范围

        try {
            String departmentIds = null;

            // OA-41805 部门收发员在发文登记簿中能看到的是整个单位的收发数据，权限放大了
            if (EdocRoleHelper.isAccountExchange(user.getId())) {

                departmentIds = dataRight;

            } else {
                String myExchangeDept = EdocRoleHelper.getUserExchangeDepartmentIds();

                //部门推送 其他部门收发员查看数据取交集
                if (Strings.isNotBlank(dataRight) && Strings.isNotBlank(myExchangeDept)) {
                    StringBuilder watchDataRight = new StringBuilder();
                    String[] deptArray = myExchangeDept.split(",");
                    for (String dept : deptArray) {
                        if (dataRight.contains(dept)) {

                            if (watchDataRight.length() > 0) {
                                watchDataRight.append(",");
                            }
                            watchDataRight.append(dept);
                        }
                    }
                    departmentIds = watchDataRight.toString();
                } else {
                    departmentIds = myExchangeDept;
                }
            }

            //规则：发文登记簿可以查到交换过的收文，和本单位或本部门未交换但路程结束的数据
            if (Strings.isNotBlank(departmentIds)) {
                String[] depIds = departmentIds.split("[,]");
                if (depIds.length > 0) {
                    hql.append("  and (");
                    hql.append(" (send.status is not null and send.exchangeType=:exchangeType ");
                    queryParams.put("exchangeType", EdocSendRecord.Exchange_Send_iExchangeType_Dept);

                    List<Long> exchangeOrgIdList = new ArrayList<Long>();
                    for (int i = 0; i < depIds.length; i++) {
                        exchangeOrgIdList.add(Long.parseLong(depIds[i]));
                    }
                    hql.append(" and send.exchangeOrgId in (:exchangeOrgIds)");
                    queryParams.put("exchangeOrgIds", exchangeOrgIdList);

                    hql.append(" ) or (");
                    hql.append(" send.status is null and summary.orgDepartmentId in (:orgDepartmentIds))");
                    queryParams.put("orgDepartmentIds", exchangeOrgIdList);
                    hql.append(")");
                }
            } else {//单位收发员查看

                hql.append(" and (");
                hql.append(" (send.status is null and summary.orgAccountId = :orgAccountId) ");
                hql.append(" or ");
                hql.append(" (send.status is not null and send.exchangeAccountId=:exchangeAccountId) ");
                hql.append(") ");

                queryParams.put("orgAccountId", accountId);
                queryParams.put("exchangeAccountId", accountId);
            }
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }

        //排序
        if (Strings.isNotBlank(orderSql)) {
            hql.append(orderSql);
        } else {
            hql.append(" order by summary.createTime desc");
        }

        List<Object[]> results = null;

        if (flipInfo == null) {//不分页查询
            results = DBAgent.find(hql.toString(), queryParams);
        } else {
            results = DBAgent.find(hql.toString(), queryParams, flipInfo);
        }

        if (Strings.isNotEmpty(results)) {

            for (Object[] objects : results) {

                EdocSummary summary = new EdocSummary();
                int n = make(objects, summary, false);// 组装对象
                summary.setUnitLevel((String) objects[n++]);
                summary.setSendTime((Timestamp) objects[n++]);
                summary.setEdocType(com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND);

                // 开始组装最后返回的结果
                EdocSummaryModel model = new EdocSummaryModel();

                model.setSummary(summary);

                Object tempUserId = objects[objects.length - 1];
                Long userId = null;
                if (tempUserId != null) {
                    userId = ((Number) tempUserId).longValue();
                }

                String userName = "";

                // 修复bug GOV-2915 公文管理-发文管理-分发，发文登记簿，查询，最后2条公文的分发人是审计管理员
                if (userId != null && userId.longValue() != 0l) {
                    try {
                        userName = orgManager.getMemberById(userId).getName();
                    } catch (BusinessException e) {
                        LOGGER.error("", e);
                    }
                }

                model.setSender(userName);

                // OA-25915 发文登记簿，查询出来的数据，发文部门全都是a部门。但是通过小查询发文部门，只能查出一条数据。
                Object depName = objects[objects.length - 2];
                if (depName != null) {
                    model.setDepartmentName(String.valueOf(depName));
                }

                //model.setAffairId(objects[objects.length - 3]==null ? null : ((Number) objects[objects.length - 3]).longValue());

                Object keepPeriod = objects[objects.length - 4];
                if (keepPeriod != null && Strings.isNotBlank(String.valueOf(keepPeriod))) {
                    try {
                        summary.setKeepPeriod(Integer.parseInt(String.valueOf(keepPeriod)));
                    } catch (Exception e) {
                        summary.setKeepPeriod(null);
                    }
                } else {
                    summary.setKeepPeriod(null);
                }

                // 当发文时没有选择主送单位的时候，才采用下面的逻辑
                if (Strings.isBlank(summary.getSendTo())) {

                    // OA-19095 在公文交换--已发送中进行了撤销、补发的操作，发文登记簿统计时字段中的主送单位丢失了
                    String sendedTypeIds = String.valueOf(objects[objects.length - 5]);
                    StringBuilder sendName = new StringBuilder();
                    try {
                        if (Strings.isNotBlank(sendedTypeIds) && !"null".equals(sendedTypeIds)) {

                            List<V3xOrgEntity> entitys = orgManager.getEntities(sendedTypeIds);
                            if (entitys != null && entitys.size() > 0) {
                                for (int j = 0; j < entitys.size(); j++) {
                                    V3xOrgEntity entity = entitys.get(j);
                                    sendName.append(entity.getName());
                                    if (j != entitys.size() - 1) {
                                        sendName.append(",");
                                    }
                                }
                                summary.setSendTo(sendName.toString());
                            }
                        }
                    } catch (BusinessException e) {
                        LOGGER.error("获取单位或部门名称错误" + e);
                    }
                }
                model.setState((Integer) objects[objects.length - 6]);
                // OA-19095 在公文交换--已发送中进行了撤销、补发的操作，发文登记簿统计时字段中的主送单位丢失了 end

                StringBuilder sendToUnit = new StringBuilder();
                if (Strings.isNotBlank(summary.getSendTo())) {
                    sendToUnit.append(summary.getSendTo());
                }
                if (Strings.isNotBlank(summary.getCopyTo())) {
                    if (sendToUnit.length() > 0) {
                        sendToUnit.append("、");
                    }
                    sendToUnit.append(summary.getCopyTo());
                }

                if (Strings.isNotBlank(summary.getReportTo())) {
                    if (sendToUnit.length() > 0) {
                        sendToUnit.append("、");
                    }
                    sendToUnit = new StringBuilder(summary.getReportTo());
                }

                validateEnmus(model);//进行枚举检验，老数据可能存在枚举为-1的情况

                model.setSendToUnit(sendToUnit.toString());

                retList.add(model);
            }
        }

        return retList;
    }

    /**
     * 验证Summary里面的有效枚举,进行枚举检验，老数据可能存在枚举为-1的情况
     *
     * @param summary
     * @Author : xuqiangwei
     * @Date : 2014年12月9日上午10:46:08
     */
    private void validateEnmus(EdocSummaryModel model) {

        if (model != null) {

            if (model.getSummary() != null) {

                EdocSummary summary = model.getSummary();

                CtpEnumBean keepPeriod = enumManagerNew.getEnum(EnumNameEnum.edoc_keep_period.name());//保密期限
                CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());//密级
                CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());//紧急程度
                CtpEnumBean unitLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_unit_level.name());//公文级别

                String secretLevelValue = summary.getSecretLevel();
                Integer keepPeriodValue = summary.getKeepPeriod();
                String urgentLevelValue = summary.getUrgentLevel();
                String unitLevelValue = summary.getUnitLevel();

                String keepPeriodStrVal = keepPeriodValue == null ? null : keepPeriodValue.toString();
                if (keepPeriod.getItem(keepPeriodStrVal) == null) {
                    summary.setKeepPeriod(null);
                }
                ;

                if (secretLevel.getItem(secretLevelValue) == null) {
                    summary.setSecretLevel(null);//
                }
                if (urgentLevel.getItem(urgentLevelValue) == null) {
                    summary.setUrgentLevel(null);
                }
                if (unitLevel.getItem(unitLevelValue) == null) {
                    summary.setUnitLevel(null);
                }
            }


            CtpEnumBean sendUnitType = enumManagerNew.getEnum(EnumNameEnum.send_unit_type.name());//来文类别

            Integer sendUnitTypeValue = model.getSendUnitType();

            String sendUnitTypeStrValue = sendUnitTypeValue == null ? null : sendUnitTypeValue.toString();

            if (sendUnitType.getItem(sendUnitTypeStrValue) == null) {
                model.setSendUnitType(null);
            }
        }
    }

    /**
     * @param opinion
     * @throws BusinessException
     * @throws UnsupportedEncodingException
     * @Description : 保存公文单意见
     * @Author : xuqiangwei
     * @Date : 2014年11月14日下午12:32:29
     */
    private void saveOpinion(EdocOpinion opinion) {
        if (null == opinion) {
            return;
        }
        Long userId = opinion.getCreateUserId();
        try {
            if (userId != null && userId != -1L) {
                V3xOrgMember orgMember = orgManager.getMemberById(userId);
                if (null != orgMember) {
                    V3xOrgAccount orgAccount = orgManager.getAccountById(orgMember.getOrgAccountId());
                    V3xOrgDepartment orgDepartment = orgManager.getDepartmentById(orgMember.getOrgDepartmentId());
                    if (null != orgAccount) {
                        opinion.setAccountName(orgAccount.getName());
                    }
                    if (null != orgDepartment) {
                        opinion.setDepartmentName(orgDepartment.getName());
                        opinion.setDepartmentSortId(orgDepartment.getSortId());
                    }
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("保存意见获取部门信息错误：", e);
        }

        edocOpinionDao.save(opinion); //保存意见
        saveEdocPicSign(opinion);//保留签名信息
    }

    /**
     * @param opinion
     * @Description : 删除公文单意见
     * @Author : xuqiangwei
     * @Date : 2014年11月14日下午12:39:46
     */
    private void deleteOpinion(EdocOpinion opinion) {
        edocOpinionDao.delete(opinion);//删除意见
        deleteEdocPicSign(opinion);//删除签名信息
    }

    /**
     * @param opinion
     * @throws BusinessException
     * @Description : 更新公文单意见
     * @Author : xuqiangwei
     * @Date : 2014年11月14日下午12:44:55
     */
    private void updateOpinion(EdocOpinion opinion) {
        if (null == opinion) {
            return;
        }
        Long userId = opinion.getCreateUserId();
        try {
            if (userId != null && userId != -1L) {
                V3xOrgMember orgMember = orgManager.getMemberById(userId);
                if (null != orgMember) {
                    V3xOrgAccount orgAccount = orgManager.getAccountById(orgMember.getOrgAccountId());
                    V3xOrgDepartment orgDepartment = orgManager.getDepartmentById(orgMember.getOrgDepartmentId());
                    if (null != orgAccount) {
                        opinion.setAccountName(orgAccount.getName());
                    }
                    if (null != orgDepartment) {
                        opinion.setDepartmentName(orgDepartment.getName());
                        opinion.setDepartmentSortId(orgDepartment.getSortId());
                    }
                }
            }
        } catch (BusinessException e) {
            LOGGER.error("修改意见获取部门信息错误：", e);
        }
        edocOpinionDao.update(opinion);//修改意见
        saveEdocPicSign(opinion);//保留签名信息
    }

    /**
     * @param summaryId
     * @Description : 删除公文单的意见
     * @Author : xuqiangwei
     * @Date : 2014年11月14日下午1:53:31
     */
    private void deleteOpinionBySummaryId(Long summaryId) {
        //删除意见
        edocOpinionDao.deleteOpinionBySummaryId(summaryId);

        //删除签名  && 删除签批意见
        htmSignetManager.deleteBySummaryId(summaryId);

    }

    /**
     * 删除处理意见，不包括发起人意见
     *
     * @param summaryId
     */
    private void deleteDealOpinion(Long summaryId) {
        List<EdocOpinion> opinions = edocOpinionDao.getDealOpinion(summaryId);
        if (Strings.isNotEmpty(opinions)) {//批量删除电子签名信息
            List<Long> affairIds = new ArrayList<Long>();
            for (EdocOpinion opinion : opinions) {
                affairIds.add(opinion.getAffairId());
            }
            htmSignetManager.deleteBySummaryIdAffairsAndType(summaryId, affairIds, V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());
        }
        edocOpinionDao.deleteDealOpinion(summaryId);//删除意见
    }

    /**
     * @param subOpinionIds
     * @Description : 批量删除下级意见
     * @Author : xuqiangwei
     * @Date : 2014年11月14日下午2:18:24
     */
    private void delOptionBySubOpId(List<Long> subOpinionIds) {
        List<EdocOpinion> opinions = edocOpinionDao.getOptionBySubOpId(subOpinionIds);
        if (Strings.isNotEmpty(opinions)) {//批量删除电子签名信息
            Long summaryId = opinions.get(0).getEdocSummary().getId();
            List<Long> affairIds = new ArrayList<Long>();
            for (EdocOpinion opinion : opinions) {
                affairIds.add(opinion.getAffairId());
            }
            htmSignetManager.deleteBySummaryIdAffairsAndType(summaryId, affairIds, V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());
        }
        edocOpinionDao.delOptionBySubOpId(subOpinionIds);//删除意见
    }

    /**
     * 保存文单审批落款电子签名
     *
     * @param edocOpinion
     * @throws BusinessException
     * @throws UnsupportedEncodingException
     * @Author : xuqiangwei
     * @Date : 2014年11月7日下午3:09:30
     */
    private void saveEdocPicSign(EdocOpinion edocOpinion) {

        User user = AppContext.getCurrentUser();

        EdocSummary summary = edocOpinion.getEdocSummary();
        V3xHtmDocumentSignature hd = htmSignetManager.getBySummaryIdAffairIdAndType(summary.getId(),
                edocOpinion.getAffairId(),
                V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());

        //公文处理意见回显到公文单,排序
        long flowPermAccout = EdocHelper.getFlowPermAccountId(user.getLoginAccount(), edocOpinion.getEdocSummary(), templateManager);

        //查找公文单意见元素显示。
        FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(), flowPermAccout);
        if (OpinionShowNameTypeEnum.SIGN.getValue().equals(displayConfig.getShowNameType())) {//电子签名显示方式

            List<V3xSignet> ls = signetManager.findSignetByMemberId(edocOpinion.getCreateUserId());
            String userName = null;

            try {
                V3xOrgMember affairUser = orgManager.getMemberById(edocOpinion.getCreateUserId());
                userName = affairUser.getName();
            } catch (BusinessException e1) {
                userName = "";
                LOGGER.error("保留签名信息，获取人员名称错误", e1);
            }

            if (Strings.isNotEmpty(ls)) {
                Collections.sort(ls, new Comparator<V3xSignet>() {
                    public int compare(V3xSignet o1, V3xSignet o2) {
                        return o1.getMarkDate().compareTo(o2.getMarkDate());
                    }
                });
                V3xSignet vSign = null;
                Integer signType = Integer.valueOf(0);//0 - 签名， 1 - 印章
                for (V3xSignet sign : ls) {
                    if (signType.equals(sign.getMarkType())) {
                        vSign = sign;//获取第一个签名
                        break;
                    }
                }
                if (vSign != null) {
                    byte[] markBodyByte = vSign.getMarkBodyByte();

                    try {
                        //有异常先绕过 记录下来异常的签章 然后需要客户去重新修改签章数据
                        boolean isUpdate = true;
                        if (hd == null) {
                            isUpdate = false;
                            hd = new V3xHtmDocumentSignature();
                        }

                        hd.setIdIfNew();
                        hd.setAffairId(edocOpinion.getAffairId());//设置affairId
                        hd.setSummaryId(edocOpinion.getEdocSummary().getId());//取得文档编号
                        hd.setFieldName("");//取得签章字段名称, 设置为空
                        hd.setFieldValue(EdocHelper.byte2hex(markBodyByte));//取得签章数据内容, 16进制方式保存
                        hd.setUserName(userName);//取得用户名称
                        hd.setDateTime(new Timestamp(System.currentTimeMillis()));//取得签章日期时间
                        hd.setHostName("");//取得客户端IP，审核不做IP记录
                        hd.setSignetType(V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());


                        if (isUpdate) {
                            htmSignetManager.update(hd);
                        } else {
                            htmSignetManager.save(hd);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } else {//取消签名的情况进行删除
                    deleteEdocPicSign(edocOpinion);//删除签名
                }
            }
        } else if (hd != null) {//文单签名方式改变，删除已有数据
            deleteEdocPicSign(edocOpinion);//删除签名
        }
    }

    /**
     * 物理删除文单审批落款电子签名
     *
     * @param edocOpinion
     * @Author : xuqiangwei
     * @Date : 2014年11月7日下午4:01:22
     */
    private void deleteEdocPicSign(EdocOpinion edocOpinion) {

        htmSignetManager.deleteBySummaryIdAffairIdAndType(edocOpinion.getEdocSummary().getId(),
                edocOpinion.getAffairId(),
                V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());
    }

    /**
     * 追加承办机构
     *
     * @param namedParameter
     * @param summary
     * @param user
     * @throws BusinessException
     * @Author : xuqiangwei
     * @Date : 2014年12月13日下午5:40:34
     */
    private void appendUndertakenoffice(Map<String, Object> namedParameter, EdocSummary summary,
                                        User user, Long memberId) throws BusinessException {

        String undertakenofficeId = summary.getUndertakenofficeId();
        boolean blankFlag = false;

        if (Strings.isBlank(undertakenofficeId)) {
            undertakenofficeId = "";
            blankFlag = true;
        }
        long userDeptId = user.getDepartmentId();
        long userAccountId = user.getAccountId();

        V3xOrgMember member = orgManager.getMemberById(memberId);
        if (member != null) {
            userDeptId = member.getOrgDepartmentId();
            userAccountId = member.getOrgAccountId();
        }

        String deptCode = "Department|" + userDeptId;
        String userAccountCode = "Account|" + userAccountId;

        //去重处理, 部门包含在单位直接不添加
        if (undertakenofficeId.indexOf(deptCode) == -1 && undertakenofficeId.indexOf(userAccountCode) == -1) {

            if (!blankFlag) {
                undertakenofficeId += ",";//ID直接用逗号分隔
            }

            undertakenofficeId += deptCode;
            summary.setUndertakenofficeId(undertakenofficeId);

            namedParameter.put("undertakenofficeId", undertakenofficeId);
        }
    }

    @Override
    public int checkSerialNoExsit(String objectId, String serialNo) {
        long loginAccout = AppContext.currentAccountId();
        return edocSummaryDao.checkRegisterSerialNoExsit(Strings.isBlank(objectId) ? null : Long.parseLong(objectId), serialNo, loginAccout);
    }

    @Override
    public String getBodyType(String edocId) throws BusinessException {
        String bodyType = "";
        if (Strings.isNotBlank(edocId)) {
            Long eId = Long.valueOf(edocId);
            EdocSummary summary = edocSummaryDao.get(eId);
            bodyType = summary.getBody(0).getContentType();
        }
        return bodyType;
    }

    public String getRegisterBodyType(String registerId) throws BusinessException {
        String bodyType = "";
        if (Strings.isNotBlank(registerId)) {
            Long eId = Long.valueOf(registerId);
            EdocRegister register = edocRegisterManager.getEdocRegister(eId);
            EdocSummary summary = edocSummaryDao.get(register.getEdocId());
            bodyType = summary.getBody(0).getContentType();
        }
        return bodyType;
    }

    @SuppressWarnings({"unchecked", "unchecked"})
    @Override
    public Map getAttributeSettingInfo(Map<String, String> args) throws BusinessException {
        Map map = new HashMap();
        if (args == null) {
            return map;
        }
        //无
        String isNull = ResourceUtil.getString("collaboration.project.nothing.label");
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
        //是否超期
        Boolean cOverTime = affair.isCoverTime(); //未超期、已超期
        boolean isComplatedFlag = Integer.valueOf(StateEnum.col_done.key()).equals(affair.getState());
        java.util.Date complateTime = isComplatedFlag ? affair.getCompleteTime() : com.seeyon.ctp.util.DateUtil.currentDate();
        if (affair.getExpectedProcessTime() != null && complateTime.after(affair.getExpectedProcessTime())) {
            cOverTime = true;
        }
        map.put("cOverTime", cOverTime == true ? ResourceUtil.getString("pending.overtop.true.label") : ResourceUtil.getString("pending.overtop.false.label"));
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
        //查看公文时，特有的属性
        EdocSummary edocSummary = this.getEdocSummaryById(affair.getObjectId(), false, false);
        processId = edocSummary.getProcessId();
        //归档
        Long archiveId = edocSummary.getArchiveId();

        String archiveName = isNull;
        String archiveAllName = isNull;
        if (archiveId != null) {
            archiveName = ColUtil.getArchiveNameById(archiveId);
            archiveAllName = ColUtil.getArchiveAllNameById(archiveId);
        }
        map.put("archiveName", archiveName);
        map.put("archiveAllName", archiveAllName);
        //流程期限
        Long deadline = edocSummary.getDeadline();
        if (edocSummary.getDeadlineDatetime() == null) {
            map.put("deadline", (deadline == null || deadline == 0) ? isNull : ColUtil.getDeadLineNameForEdoc(deadline, edocSummary.getCreateTime()));
        } else {
            map.put("deadline", (deadline == null) ? isNull : ColUtil.getDeadLineName(edocSummary.getDeadlineDatetime()));
        }
        //提醒
        Long advanceRemind = edocSummary.getAdvanceRemind();
        map.put("canDueReminder", advanceRemind == null ? isNull : ColUtil.getAdvanceRemind(advanceRemind
                .toString()));
        //发起时间
        Timestamp startDate = edocSummary.getCreateTime();
        map.put("startDate", Datetimes.formatDatetimeWithoutSecond(startDate));
        //显示督办信息
        CtpSuperviseDetail detail = superviseManager.getSupervise(affair.getObjectId());
        if (detail != null) {
            map.put("awakeDate", Datetimes.formatDatetimeWithoutSecond(detail.getAwakeDate()));
            map.put("supervisors", detail.getSupervisors());
            map.put("supervise", "supervise");
        }
        return map;
    }

    @Override
    public void onLeaveDealPage(String jsonParam) {

        if (Strings.isNotBlank(jsonParam)) {

            Map<String, Map<String, String>> params = JSONUtil.parseJSONString(jsonParam, Map.class);

            User user = AppContext.getCurrentUser();

            String userId = user.getId().toString();

            //解锁
            Map<String, String> lockParam = params.get("DelLock");
            if (lockParam != null && !lockParam.isEmpty()) {
                try {
                    String summaryId = lockParam.get("summaryId");
                    String processId = lockParam.get("processId");
                    //文单解锁
                    edocSummaryManager.deleteUpdateObj(summaryId, userId);
                    //正文解锁
                    handWriteManager.deleteUpdateObj(summaryId);
                    //流程解锁
                    WFAjax.releaseWorkflow(summaryId, userId);
                    WFAjax.releaseWorkflow(processId, userId);
                } catch (Exception e) {
                    LOGGER.error("解锁失败", e);
                }
            }

            //记录操作时间
            Map<String, String> viewRecordMap = params.get("ViewRecord");
            if (viewRecordMap != null && viewRecordMap.size() > 0) {
                try {
                    affairManager.updateSignleViewTime(viewRecordMap);
                } catch (Exception e) {
                    LOGGER.error("记录第一次关闭窗口时间异常", e);
                }
            }
        }
    }

    /**
     * 解锁，公文提交或者暂存待办的时候进行解锁,与Ajax解锁一起，构成两次解锁，避免解锁失败，节点无法修改的问题出现
     *
     * @param userId
     * @param summaryId
     */
    public void unLockSummary(Long userId, EdocSummary summary) {
        if (summary == null) return;
        String bodyType = summary.getFirstBody().getContentType();
        long summaryId = summary.getId();
        if (Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(bodyType) ||
                Constants.EDITOR_TYPE_OFFICE_WORD.equals(bodyType) ||
                Constants.EDITOR_TYPE_WPS_EXCEL.equals(bodyType) ||
                Constants.EDITOR_TYPE_WPS_WORD.equals(bodyType)) {
            //1、解锁office正文
            try {
                String contentId = summary.getFirstBody().getContent();
                handWriteManager.deleteUpdateObj(contentId);
            } catch (Exception e) {
                LOGGER.error("解锁office正文失败 userId:" + userId + " summaryId:" + summary.getId(), e);
            }
        } else {
            //2、解锁html正文
            try {
                handWriteManager.deleteUpdateObj(String.valueOf(summaryId));
            } catch (Exception e) {
                LOGGER.error("解锁html正文失败 userId:" + userId + " summaryId:" + summaryId, e);
            }
        }
        //3、解锁公文单
        try {
            edocSummaryManager.deleteUpdateObj(String.valueOf(summaryId), String.valueOf(userId));
        } catch (Exception e) {
            LOGGER.error("解锁公文单失败 userId:" + userId + " summaryId:" + summaryId, e);
        }
    }

    public void h5CancelRegister(EdocSummary summary) throws BusinessException {
        Map<String, Object> distributerConditions = new HashMap<String, Object>();
        distributerConditions.put("objectId", summary.getId());
        distributerConditions.put("app", ApplicationCategoryEnum.edocRecDistribute.key());
        List<Integer> distributerStates = new ArrayList<Integer>();
        distributerStates.add(StateEnum.col_done.key());
        distributerConditions.put("state", distributerStates);
        List<CtpAffair> distributerDoneList = affairManager.getByConditions(null, distributerConditions);
        for (int k = 0; k < distributerDoneList.size(); k++) {
            distributerDoneList.get(k).setState(StateEnum.col_pending.key());
            affairManager.updateAffair(distributerDoneList.get(k));
        }
        EdocRegister edocRegister = edocRegisterManager.findRegisterByDistributeEdocId(summary.getId());
        if (edocRegister != null) {
            edocRegister.setDistributeDate(null);
            edocRegister.setDistributeEdocId(summary.getId());
            edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());//将状态设置为"草稿"
            //撤销与取回时，登记对象退回状态为：0
            edocRegister.setIsRetreat(0);//非退回
            edocRegisterManager.update(edocRegister);

            //删除收文(暂物理删除) 撤销/取回都要删除分发数据
            summary.setState(CollaborationEnum.flowState.deleted.ordinal());

        } else {
            EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(summary.getId());
            if (record != null) {
                //删除收文(暂物理删除) 撤销/取回都要删除分发数据
                summary.setState(CollaborationEnum.flowState.deleted.ordinal());
            }
        }
    }

    public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
        this.recieveEdocManager = recieveEdocManager;
    }

    public void unlockEdocAll(Long summaryId, EdocSummary summary) {
        try {
            if (summary == null || summary.getFirstBody() == null) {
                if (summaryId != null) {
                    summary = this.getEdocSummaryById(summaryId, true);
                }
            }
            if (summary != null) {
                Long userId = AppContext.currentUserId();
                /** 1 流程解锁 */
                if (Strings.isNotBlank(summary.getProcessId())) {
                    wapi.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(userId));
                }
                wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(userId));
                /** 2 公文单解锁 */
                try {
                    edocSummaryManager.deleteUpdateObj(String.valueOf(summaryId), String.valueOf(userId));
                } catch (Exception e) {
                    LOGGER.error("解锁公文单失败 userId:" + userId + " summaryId:" + summaryId, e);
                }
                /** 3 正文解锁(多人同时修改同一正文) */
                String bodyType = summary.getFirstBody().getContentType();
                if (Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(bodyType) ||
                        Constants.EDITOR_TYPE_OFFICE_WORD.equals(bodyType) ||
                        Constants.EDITOR_TYPE_WPS_EXCEL.equals(bodyType) ||
                        Constants.EDITOR_TYPE_WPS_WORD.equals(bodyType)) {
                    try {
                        String contentId = summary.getFirstBody().getContent();
                        handWriteManager.deleteUpdateObj(contentId);
                    } catch (Exception e) {
                        LOGGER.error("解锁office正文失败 userId:" + userId + " summaryId:" + summary.getId(), e);
                    }
                } else {
                    try {
                        handWriteManager.deleteUpdateObj(String.valueOf(summaryId));
                    } catch (Exception e) {
                        LOGGER.error("解锁html正文失败 userId:" + userId + " summaryId:" + summaryId, e);
                    }
                }
                /** 4 解锁文档中的所有office锁 */
                officeLockManager.unlockAll(summary.getId());
            }
        } catch (Exception e) {
            LOGGER.error("公文解锁出错", e);
        }
    }

    @Override
    public void deleteOpinionByWay(CtpAffair affair, String optionWay) {
        //4退回时办理人选择意见覆盖方式,其他情况保留所有意见，被退回人选择保存最后意见
        if ("4_1".equals(optionWay)) {
            List<Long> affairIds = new ArrayList<Long>();
            List<CtpAffair> affairList = affairManager.getAffairsByNodeId(affair.getActivityId());
            for (CtpAffair aff : affairList) {
                affairIds.add(aff.getId());
            }
            List<Long> deleteOpinionIdList = new ArrayList<Long>();
            List<EdocOpinion> edocOpinionList = this.findEdocOpinionByAffairId(affair.getObjectId(), affair.getMemberId(), affair.getNodePolicy(), affairIds);
            for (int i = 0; i < edocOpinionList.size(); i++) {
                deleteOpinionIdList.add(edocOpinionList.get(i).getId());
            }
            if (Strings.isNotEmpty(deleteOpinionIdList)) {
                this.edocOpinionDao.deleteOpinionById(deleteOpinionIdList);
            }
        }
    }

    @Override
    public void updateCurrentNodeUser(Long id, String currentUser)
            throws BusinessException {
        edocSummaryDao.updateCurrentNodeUser(id, currentUser);

    }

    @Override
    public String transferEdoc(User user, long affairId, long summaryId, Long transferMemberId, String content, Boolean isHidden, String attitude, HttpServletRequest request, List<Long[]> pushMsgMemberList) throws BusinessException {
        CtpAffair affair = affairManager.get(affairId);
        EdocSummary summary = this.getEdocSummaryById(summaryId, true);
        EdocOpinion transferOpinion = new EdocOpinion();
        if (Strings.isNotBlank(attitude)) {
            transferOpinion.setAttribute(Integer.valueOf(attitude).intValue());
        } else {
            transferOpinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
        }
        if (null != affair.getActivityId()) {
            transferOpinion.setNodeId(affair.getActivityId());
        }
        transferOpinion.setAffairId(affairId);
        transferOpinion.setCreateUserId(affair.getMemberId());
        transferOpinion.setContent(content);
        transferOpinion.setIsHidden(isHidden);
        transferOpinion.setIdIfNew();
        transferOpinion.setEdocSummary(summary);
        transferOpinion.setCreateTime(new Timestamp(System.currentTimeMillis()));
        // 移交
        transferOpinion.setOpinionType(EdocOpinion.OpinionType.transferOpinion.ordinal());
        if (Strings.isBlank(transferOpinion.getPolicy()))
            transferOpinion.setPolicy(affair.getNodePolicy());
        try {
            // 修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if (editHelper.hasEditAtt()) {// 是否修改附件
                saveAttachment(summary, affair, transferOpinion.getId(), request);
            } else {
                this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, transferOpinion.getId(), request);
            }
            /**
             * 如果处理时添加了附件
             */
            transferOpinion.setHasAtt(EdocHelper.isAddAttachmentByOpinion(request, summaryId));
            if (editHelper.hasEditAtt()) {// 是否修改附件
                // 设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(summary.getProcessId()), affair.getActivityId()), true,
                        "transfer");
            }
        } catch (Exception e) {
            LOGGER.error("公文附件保存出错：", e);
            throw new BusinessException(e.getMessage());
        }
        return transTransferEdoc(user, affair, summary, transferOpinion, transferMemberId, pushMsgMemberList);
    }

    @SuppressWarnings("unchecked")
    public String transTransferEdoc(User user, CtpAffair affair, EdocSummary summary, EdocOpinion transferOpinion, Long transferMemberId, List<Long[]> pushMsgMemberList)
            throws BusinessException {
        String message = "";
        V3xOrgMember orgMember = orgManager.getMemberById(transferMemberId);
        try {
            if (!orgMember.isValid()) {
                message = ResourceUtil.getString("edoc.transfer.no.person");
                return message;
            }
            String processId = summary.getProcessId();
            List<V3xOrgMember> nextMembers = new ArrayList<V3xOrgMember>();
            if (orgMember.getId() != user.getId()) {
                nextMembers.add(orgMember);
            }
            String newAffairMemberId = "";
            if (nextMembers.size() > 0) {
                // 替换节点
                Object[] result = wapi.replaceWorkItemMembers(true, affair.getMemberId(), processId, affair.getSubObjectId(),
                        affair.getActivityId().toString(), nextMembers, true);
                List<WorkItem> workitems = (List<WorkItem>) result[1];
                BPMHumenActivity bpmActivity = (BPMHumenActivity) result[2];
                List<CtpAffair> newAffairs = new ArrayList<CtpAffair>();
                for (int i = 0; i < workitems.size(); i++) {
                    WorkItem workItem = workitems.get(i);
                    CtpAffair newAffair = (CtpAffair) org.apache.commons.beanutils.BeanUtils.cloneBean(affair);
                    // 一、替换v3x_affair中的member_id为dealTermUserId
                    newAffairMemberId = workItem.getPerformer();
                    newAffair.setMemberId(Long.parseLong(newAffairMemberId));
                    newAffair.setId(UUIDLong.longUUID());
                    newAffair.setSubObjectId(workItem.getId());
                    newAffair.setCoverTime(false);
                    newAffair.setReceiveTime(new java.util.Date());
                    newAffair.setUpdateDate(newAffair.getReceiveTime());
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
                    newAffair.setPreApprover(affair.getMemberId());
                    V3xOrgMember nextMember = nextMembers.get(i);
                    if (newAffair.getDeadlineDate() != null && newAffair.getDeadlineDate() != 0l) {
                        newAffair.setExpectedProcessTime(workTimeManager.getCompleteDate4Nature(new java.util.Date(), newAffair.getDeadlineDate(),
                                nextMember.getOrgAccountId()));
                    }
                    newAffairs.add(newAffair);
                }
                affairManager.saveAffairs(newAffairs);
                // 发送移交信息
                EdocMessageHelper.sendMessage4EdocTransfer(affairManager, orgManager, userMessageManager, user, summary, newAffairs, affair, transferOpinion, pushMsgMemberList);
                affair.setDelete(true);
                affair.setActivityId(-1l);
                affair.setSubObjectId(-1l);
                affair.setObjectId(-1l);
                affair.setTempleteId(-1l);
                Long oldMemberId = affair.getMemberId();
                affair.setMemberId(-1l);

                // 更新操作时间
                if (affair.getSignleViewPeriod() == null && affair.getFirstViewDate() != null) {
                    java.util.Date nowTime = new java.util.Date();
                    long viewTime = workTimeManager.getDealWithTimeValue(affair.getFirstViewDate(), nowTime, affair.getOrgAccountId());
                    affair.setSignleViewPeriod(viewTime);
                }
                affairManager.updateAffair(affair);
                // 更新当前待办人
                String currentNodesInfo = summary.getCurrentNodesInfo().replaceFirst(String.valueOf(oldMemberId), String.valueOf(newAffairMemberId));
                summary.setCurrentNodesInfo(currentNodesInfo);
                saveOpinion(transferOpinion);
                this.update(summary);
                // 写流程日志和应用日志
                processLogManager.insertLog(user, Long.valueOf(processId), Long.valueOf(bpmActivity.getId()), ProcessLogAction.transfer, user.getName(),
                        orgMember.getName());
                // {0}移交公文《{1}》给{2}
                appLogManager.insertLog(user, AppLogAction.Edoc_Transfer, user.getName(), summary.getSubject(), orgMember.getName());

                EdocAffairsAssignedEvent event = new EdocAffairsAssignedEvent(this);
                event.setAffairs(newAffairs);
                EventDispatcher.fireEvent(event);

                return message;
            } else {
                message = ResourceUtil.getString("edoc.transfer.person.is.invalid");
                return message;
            }
        } catch (Exception e) {
            LOGGER.error("公文移交出错：", e);
            throw new EdocException(e.getMessage());
        } finally {
            unlockEdocAll(summary.getId(), summary);
        }
    }

    @Override
    @AjaxAccess
    public String checkCreateRoleAuth(String moduleType) throws BusinessException {
        boolean isEdocCreateRole = true;

        if ("19".equals(moduleType)) {
            isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(),
                    AppContext.getCurrentUser().getId(), EdocEnum.edocType.sendEdoc.ordinal());
        } else if ("20".equals(moduleType)) {
            isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(),
                    AppContext.getCurrentUser().getId(), EdocEnum.edocType.recEdoc.ordinal());
        } else if ("21".equals(moduleType)) {
            isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(),
                    AppContext.getCurrentUser().getId(), EdocEnum.edocType.signReport.ordinal());
        }
        if (!isEdocCreateRole) {
            String msgKey = "alert_not_edoccreate";
            if ("20".equals(moduleType)) {
                msgKey = "alert_not_edocregister";
                // G6的登记是分发
                if (EdocHelper.isG6Version()) {
                    msgKey = "alert_not_edoc_fenfa";
                }
            }
            return ResourceUtil.getString(msgKey);
        }
        return "";
    }


    @Override
    public String getAffairIdByEdocId(String edocId) throws BusinessException {
        if (Strings.isBlank(edocId)) {
            return "";
        }
        List<CtpAffair> affairs = affairManager.getAffairs(Long.valueOf(edocId), StateEnum.col_sent);
        if (Strings.isEmpty(affairs)) {
            return "";
        }
        return affairs.get(0).getId().toString();
    }

    @Override
    public String checkAffairIsDone(String affairId) {
        CtpAffair affair = null;
        String errorMsg = "";
        if (Strings.isNotBlank(affairId)) {
            try {
                affair = affairManager.get(Long.valueOf(affairId));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            if (affair == null) {
                errorMsg = ColUtil.getErrorMsgByAffair(affair);
            } else {
                StateEnum state = StateEnum.valueOf(affair.getState());
                if (StateEnum.col_done.equals(state)) {
                    errorMsg = ColUtil.getErrorMsgByAffair(affair);
                }
            }
        }
        return errorMsg;
    }

    /**
     * 项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 start
     */
    private Map<String, Object> getEdocOpinionObjectList1(EdocSummary summary, Long affairId, FormOpinionConfig displayConfig) {
        Map<String, Object> edocOpinionMap = new HashMap<String, Object>();
        long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templateManager);
        Hashtable<String, String> locHs = edocFormManager.getOpinionLocation(summary.getFormId(), flowPermAccout);
        edocOpinionMap.put("locHs", locHs);

        List<Attachment> tempAtts = attachmentManager.getByReference(summary.getId());
        Hashtable<Long, List<Attachment>> attHas = com.seeyon.ctp.common.filemanager.manager.Util.sortBySubreference(tempAtts);

        Long formAccountId = summary.getOrgAccountId();
        if (summary.getTempleteId() != null) {
            try {
                CtpTemplate template = templateManager.getCtpTemplate(summary.getTempleteId());
                if (template != null && (template.isSystem() || template.getFormParentid() != null) && template.getOrgAccountId() != summary.getOrgAccountId()) {
                    formAccountId = flowPermAccout;
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        List<EdocFormFlowPermBound> bList = edocFormFlowPermBoundDao.findBoundProcessByFormId(summary.getFormId(), formAccountId);
        Set<String> eList = new HashSet<String>();
        for (EdocFormFlowPermBound b : bList) {
            eList.add(b.getProcessName());
        }
        List<EdocOpinion> tempResult = new ArrayList<EdocOpinion>();   //查询出来的意见
        List<String> boundFlowPerm = new ArrayList<String>();   //绑定的节点权限
        //Map<意见元素名称，List<绑定的节点权限>>因为一个意见元素可以绑定多个节点权限
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        Map<String, String> sortMap = new HashMap<String, String>();
        //绑定部分的意见
        for (Iterator keyName = locHs.keySet().iterator(); keyName.hasNext(); ) {
            String flowPermName = (String) keyName.next();
            if (!boundFlowPerm.contains(flowPermName)) boundFlowPerm.add(flowPermName);
            // tempLoacl格式 公文元素名称为value_公文元素的排序方式
            String tempLocal = locHs.get(flowPermName);
            String elementOpinion = tempLocal.split("_")[0];//公文元素名,例如公文单上的shenpi这个公文元素
            //取到指定公文元素绑定的节点权限列表
            List<String> flowPermsOfSpecialElement = map.get(elementOpinion);
            if (flowPermsOfSpecialElement == null) {
                flowPermsOfSpecialElement = new ArrayList<String>();
            }
            flowPermsOfSpecialElement.add(flowPermName);

            for (String e : eList) {
                if (Strings.isNotBlank(e)) {
                    if (e.equals(elementOpinion)) {
                        map.put(elementOpinion, flowPermsOfSpecialElement);
                        break;
                    }
                }
            }
            String sortType = tempLocal.split("_")[1];
            sortMap.put(elementOpinion, sortType);
        }

        //查询文单是否包含其他意见元素
        boolean hasOtherOpinion = false;
        boolean hasFeedBack = false;

        List<EdocElement> elementList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), "otherOpinion");
        if (Strings.isNotEmpty(elementList)) {
            hasOtherOpinion = true;
        }
        List<EdocElement> feedbackList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(), EdocOpinion.FEED_BACK);
        if (Strings.isNotEmpty(feedbackList)) {
            hasFeedBack = true;
        }

        if (hasFeedBack) {
            List<String> feedbackPolicy = new ArrayList<String>(1);
            feedbackPolicy.add(EdocOpinion.FEED_BACK);
            map.put(EdocOpinion.FEED_BACK, feedbackPolicy);
            if (!boundFlowPerm.contains(EdocOpinion.FEED_BACK)) {
                boundFlowPerm.add(EdocOpinion.FEED_BACK);
            }
        }

        Set<String> bound = map.keySet(); //绑定的公文元素
        boolean isShowLast = OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(displayConfig.getOpinionType());

        if (eList.size() > 0) {
            for (String s : bound) {
                List<EdocOpinion> opinions = edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(), map.get(s), sortMap.get(s), isShowLast, true, displayConfig.getOpinionType());
                tempResult.addAll(opinions);
            }
        }

        String defaultSortType = sortMap.get("otherOpinion") == null ? "0" : sortMap.get("otherOpinion");
        //未绑定的意见全部显示
        List<EdocOpinion> opinions = edocOpinionDao.findLastSortOpinionBySummaryIdAndPolicy(summary.getId(), boundFlowPerm, defaultSortType, false, false, displayConfig.getOpinionType());
        //将下级汇报意见取出来
        List<EdocOpinion> feedbacks = new ArrayList<EdocOpinion>();
        List<EdocOpinion> others = new ArrayList<EdocOpinion>();

        for (EdocOpinion op : opinions) {
			/*if(EdocOpinion.FEED_BACK.equals(op.getPolicy())){
				op.setBound(true);
				feedbacks.add(op);
			}else{*/
            op.setBound(hasOtherOpinion || false);//非绑定意见
            others.add(op);
            /*}*/
        }
        List<EdocOpinion> opinions2 = new ArrayList<EdocOpinion>();
        if (Strings.isNotEmpty(others)) {
            opinions2.addAll(others);
        }
        if (Strings.isNotEmpty(feedbacks)) {
            feedbacks = EdocHelper.getFeedBackOptions(feedbacks);
            opinions2.addAll(feedbacks);
        }

        tempResult.addAll(opinions2);
        List<EdocOpinion> edocOpionionList = new ArrayList<EdocOpinion>();
        for (EdocOpinion edocOpinion : tempResult) {
            edocOpionionList.add(edocOpinion);
            edocOpinion.setOpinionAttachments(attHas.get(edocOpinion.getId()));
        }
        /**项目：徐矿集团  【屏蔽公文中的意见】 作者：jiangchenxi 时间：2019年3月12日 start*/
        try {
            //项目：修复公文待办指定公开无法看到意见 zelda 2019年12月21日16:36:53 start
            boolean open = xkjtManager.isOpen(summary.getAffairId());
            if (!open) {
                edocOpionionList = xkjtManager.getOpenOpinions1(edocOpionionList, affairId, 1);
            }
            //项目：修复公文待办指定公开无法看到意见 zelda 2019年12月21日16:36:53 end
        } catch (Exception e) {
            LOGGER.error("zelda-校验指定公开错误", e);
        }

        /**项目：徐矿集团  【屏蔽公文中的意见】 作者：jiangchenxi 时间：2019年3月12日 start*/
        edocOpinionMap.put("edocOpinionList", edocOpionionList);
        return edocOpinionMap;
    }

    public Map<String, EdocOpinionModel> getEdocOpinion1(EdocSummary summary, Long affairId, FormOpinionConfig displayConfig) {
        Map<String, Object> map = getEdocOpinionObjectList1(summary, affairId, displayConfig);
        Hashtable<String, String> locHs = (Hashtable<String, String>) map.get("locHs");
        SharedWithThreadLocal.setLocHs(locHs);
        List<EdocOpinion> list = (List<EdocOpinion>) map.get("edocOpinionList");
        return getEdocOpinion(list, summary, locHs);
    }
    /**项目：徐矿集团 【当前节点为待办可以看到所有】 作者：jiangchenxi 时间：2020年8月5日 end*/
}
