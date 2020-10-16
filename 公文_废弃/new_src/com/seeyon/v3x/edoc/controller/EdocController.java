package com.seeyon.v3x.edoc.controller;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.edoc.domain.*;
import com.seeyon.v3x.edoc.manager.*;
import com.seeyon.v3x.edoc.util.*;
import com.seeyon.v3x.webmail.util.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.xfire.client.Client;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.collaboration.po.WorkflowTracePO;
import com.seeyon.apps.collaboration.trace.enums.WorkflowTraceEnums;
import com.seeyon.apps.collaboration.trace.manager.TraceWorkflowManager;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.common.isignaturehtml.manager.ISignatureHtmlManager;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.constants.DocConstants;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.edoc.enums.EdocEnum.MarkCategory;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.authenticate.domain.UserHelper;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentEditHelper;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.flag.BrowserFlag;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.HandWriteManager;
import com.seeyon.ctp.common.office.HtmlHandWriteManager;
import com.seeyon.ctp.common.office.OfficeLockManager;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.enums.PermissionAction;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.phrase.manager.CommonPhraseManager;
import com.seeyon.ctp.common.phrase.po.CommonPhrase;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseTemplateRole;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.supervise.enums.SuperviseEnum;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.supervise.vo.SuperviseMessageParam;
import com.seeyon.ctp.common.supervise.vo.SuperviseSetVO;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.OrgConstants.*;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.HttpSessionUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.ctp.util.annotation.SetContentType;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.exceptions.MessageException;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.edoc.constants.EdocCustomConstant;
import com.seeyon.v3x.edoc.constants.EdocCustomerTypeEnum;
import com.seeyon.v3x.edoc.constants.EdocElementConstants;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum.RegisterRetreatState;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum.RegisterState;
import com.seeyon.v3x.edoc.constants.EdocOrgConstants;
import com.seeyon.v3x.edoc.constants.EdocQueryColConstants;
import com.seeyon.v3x.edoc.constants.PackageColValueInter;
import com.seeyon.v3x.edoc.constants.QueryCol;
import com.seeyon.v3x.edoc.constants.QueryColTemplate;
import com.seeyon.v3x.edoc.constants.RecRelationAfterSendParam;
import com.seeyon.v3x.edoc.controller.newedoc.A8RegisterEdocImpl;
import com.seeyon.v3x.edoc.controller.newedoc.CreateNewEdocImpl;
import com.seeyon.v3x.edoc.controller.newedoc.DistributeToSendEdocImpl;
import com.seeyon.v3x.edoc.controller.newedoc.ForwordtosendEdocImpl;
import com.seeyon.v3x.edoc.controller.newedoc.FromTemplateToSendEdocImpl;
import com.seeyon.v3x.edoc.controller.newedoc.NewEdocHandle;
import com.seeyon.v3x.edoc.controller.newedoc.TransmitSendEdocImpl;
import com.seeyon.v3x.edoc.controller.newedoc.WaitForSendEdocImpl;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.enums.EdocOpenFrom;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.EdocRegisterConditionModel;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.edoc.webmodel.MoreSignSelectPerson;
import com.seeyon.v3x.edoc.webmodel.SummaryModel;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.enums.EdocExchangeMode.EdocExchangeModeEnum;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;
import com.seeyon.v3x.exchange.manager.EdocExchangeTurnRecManager;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.manager.SendEdocManager;
import com.seeyon.v3x.exchange.util.ExchangeUtil;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

import net.joinwork.bpm.definition.BPMSeeyonPolicy;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;

/**
 * 类描述： 创建日期：
 *
 * @author liaoj
 * @version 1.0
 * @since JDK 5.0
 */
public class EdocController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(EdocController.class);
    private V3xHtmDocumentSignatManager htmSignetManager;
    private EnumManager enumManagerNew;
    private WorkflowApiManager wapi;
    private OrgManager orgManager;
    private AttachmentManager attachmentManager;
    private EdocManager edocManager;
    private AffairManager affairManager;
    private EdocFormManager edocFormManager;
    private TemplateManager templeteManager;
    private PermissionManager permissionManager;
    private RecieveEdocManager recieveEdocManager;
    private EdocMarkManager edocMarkManager;
    private EdocMarkHistoryManager edocMarkHistoryManager;
    private EdocMarkDefinitionManager edocMarkDefinitionManager;
    private EdocSummaryManager edocSummaryManager;
    private EdocSuperviseManager edocSuperviseManager;
    private SuperviseManager superviseManager;
    private HandWriteManager handWriteManager;
    private HtmlHandWriteManager htmlHandWriteManager;
    private UserMessageManager userMessageManager;
    private ProcessLogManager processLogManager;
    private FileManager fileManager;
    private DocApi docApi;
    private FileToExcelManager fileToExcelManager;
    private IndexManager indexManager;
    private EdocZcdbManager edocZcdbManager;
    private EdocArchiveModifyLogManager edocArchiveModifyLogManager;
    private EdocCategoryManager edocCategoryManager;
    private AppLogManager appLogManager;
    private EdocElementManager edocElementManager;
    private EdocElementFlowPermAclManager edocElementFlowPermAclManager;
    private EdocMessagerManager edocMessagerManager;
    private SendEdocManager sendEdocManager;
    private CtpTrackMemberManager trackManager;
    private ISignatureHtmlManager iSignatureHtmlManager;
    private EdocStatManager edocStatManager;
    private EdocSummaryQuickManager edocSummaryQuickManager;
    private CustomizeManager customizeManager;
    private OfficeLockManager officeLockManager;
    private ConfigManager configManager;
    private EdocExchangeManager edocExchangeManager;
    private PrivilegeManager privilegeManager;
    private EdocExchangeTurnRecManager edocExchangeTurnRecManager;
    private CommonPhraseManager phraseManager;
    private EdocLockManager edocLockManager;
    private EdocMarkLockManager edocMarkLockManager;

    private CtpPdfSavepathManager ctpPdfSavepathManager = new CtpPdfSavepathManagerImpl();

    public EdocMarkLockManager getEdocMarkLockManager() {
        return edocMarkLockManager;
    }

    public void setEdocMarkLockManager(EdocMarkLockManager edocMarkLockManager) {
        this.edocMarkLockManager = edocMarkLockManager;
    }

    public void setPhraseManager(CommonPhraseManager phraseManager) {
        this.phraseManager = phraseManager;
    }

    public void setEdocExchangeTurnRecManager(EdocExchangeTurnRecManager edocExchangeTurnRecManager) {
        this.edocExchangeTurnRecManager = edocExchangeTurnRecManager;
    }

    public void setPrivilegeManager(PrivilegeManager privilegeManager) {
        this.privilegeManager = privilegeManager;
    }

    public void setEdocExchangeManager(EdocExchangeManager edocExchangeManager) {
        this.edocExchangeManager = edocExchangeManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public EnumManager getEnumManagerNew() {
        return enumManagerNew;
    }

    public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

    public void setHtmSignetManager(V3xHtmDocumentSignatManager htmSignetManager) {
        this.htmSignetManager = htmSignetManager;
    }

    public CustomizeManager getCustomizeManager() {
        return customizeManager;
    }

    public WorkflowApiManager getWapi() {
        return wapi;
    }

    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }

    public void setEdocSummaryQuickManager(EdocSummaryQuickManager edocSummaryQuickManager) {
        this.edocSummaryQuickManager = edocSummaryQuickManager;
    }

    public void setEdocStatManager(EdocStatManager edocStatManager) {
        this.edocStatManager = edocStatManager;
    }

    private TraceWorkflowManager traceWorkflowManager;

    public TraceWorkflowManager getTraceWorkflowManager() {
        return traceWorkflowManager;
    }

    public void setTraceWorkflowManager(TraceWorkflowManager traceWorkflowManager) {
        this.traceWorkflowManager = traceWorkflowManager;
    }

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    public void setiSignatureHtmlManager(ISignatureHtmlManager iSignatureHtmlManager) {
        this.iSignatureHtmlManager = iSignatureHtmlManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setSendEdocManager(SendEdocManager sendEdocManager) {
        this.sendEdocManager = sendEdocManager;
    }

    public void setEdocMessagerManager(EdocMessagerManager edocMessagerManager) {
        this.edocMessagerManager = edocMessagerManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public void setEdocElementManager(EdocElementManager edocElementManager) {
        this.edocElementManager = edocElementManager;
    }

    public void setEdocElementFlowPermAclManager(EdocElementFlowPermAclManager edocElementFlowPermAclManager) {
        this.edocElementFlowPermAclManager = edocElementFlowPermAclManager;
    }

    private EdocSummaryRelationManager edocSummaryRelationManager;

    public void setEdocSummaryRelationManager(EdocSummaryRelationManager edocSummaryRelationManager) {
        this.edocSummaryRelationManager = edocSummaryRelationManager;
    }

    public void setEdocCategoryManager(EdocCategoryManager edocCategoryManager) {
        this.edocCategoryManager = edocCategoryManager;
    }

    public void setEdocArchiveModifyLogManager(EdocArchiveModifyLogManager edocArchiveModifyLogManager) {
        this.edocArchiveModifyLogManager = edocArchiveModifyLogManager;
    }

    private EdocRegisterManager edocRegisterManager;

    public EdocRegisterManager getEdocRegisterManager() {
        return edocRegisterManager;
    }

    private RegisterBodyManager registerBodyManager;

    public void setRegisterBodyManager(RegisterBodyManager registerBodyManager) {
        this.registerBodyManager = registerBodyManager;
    }

    /**
     * cy add
     */
    private EdocCustomerTypeManager edocCustomerTypeManager;

    public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }

    public void setEdocCustomerTypeManager(EdocCustomerTypeManager edocCustomerTypeManager) {
        this.edocCustomerTypeManager = edocCustomerTypeManager;
    }

    public void setEdocZcdbManager(EdocZcdbManager edocZcdbManager) {
        this.edocZcdbManager = edocZcdbManager;
    }

    public void setHandWriteManager(HandWriteManager handWriteManager) {
        this.handWriteManager = handWriteManager;
    }

    public UserMessageManager getUserMessageManager() {
        return userMessageManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public void setHtmlHandWriteManager(HtmlHandWriteManager htmlHandWriteManager) {
        this.htmlHandWriteManager = htmlHandWriteManager;
    }

    public HtmlHandWriteManager getHtmlHandWriteManager() {
        return this.htmlHandWriteManager;
    }

    public RecieveEdocManager getRecieveEdocManager() {
        return recieveEdocManager;
    }

    public EdocMarkManager getEdocMarkManager() {
        return edocMarkManager;
    }

    public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
        this.edocMarkManager = edocMarkManager;
    }

    public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
        this.recieveEdocManager = recieveEdocManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setTemplateManager(TemplateManager templeteManager) {
        this.templeteManager = templeteManager;
    }

    public void setEdocFormManager(EdocFormManager edocFormManager) {
        this.edocFormManager = edocFormManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setEdocManager(EdocManager edocManager) {
        this.edocManager = edocManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setOfficeLockManager(OfficeLockManager officeLockManager) {
        this.officeLockManager = officeLockManager;
    }

    public void setEdocLockManager(EdocLockManager edocLockManager) {
        this.edocLockManager = edocLockManager;
    }

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }

    public ModelAndView entryManager(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocFrameEntry");
        modelAndView.addObject("entry", request.getParameter("entry"));
        modelAndView.addObject("openFrom", request.getParameter("openFrom"));
        String toFrom = request.getParameter("toFrom");
        modelAndView.addObject("registerType", request.getParameter("registerType"));
        if (Strings.isNotBlank(toFrom) && ("newEdoc").equals(toFrom)
                && Strings.isNotBlank(request.getParameter("templeteId"))) {
            int edocType = Strings.isBlank(request.getParameter("edocType")) ? 0
                    : Integer.parseInt(request.getParameter("edocType"));
            // 检查是否有公文发起权
            int roleEdocType = edocType == 1 ? 3 : edocType;
            boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(),
                    AppContext.getCurrentUser().getId(), roleEdocType);
            if (isEdocCreateRole == false && !"agent".equals(request.getParameter("app"))) {// 没有公文发起权不能发送
                StringBuffer sb = new StringBuffer();
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "alert_not_edoccreate");
                if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
                    String msgKey = "alert_not_edocregister";
                    // G6的登记是分发
                    if (EdocHelper.isG6Version()) {
                        msgKey = "alert_not_edoc_fenfa";
                    }
                    errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", msgKey);
                }
                sb.append("alert('" + errMsg + "');");
                sb.append("if(window.dialogArguments) {"); // 弹出
                sb.append("   window.returnValue = \"true\";");
                sb.append("   window.close();");
                sb.append("} else {");
                if (Strings.isNotBlank(request.getParameter("templeteId"))) {
                    if (Strings.isNotBlank(request.getParameter("portalId"))) {
                        sb.append("  parent.refreshCurrentSpace();");
                    } else {
                        sb.append("  history.back();");
                    }
                } else if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
                    sb.append("  location.href='edocController.do?method=entryManager&entry=recManager';");
                } else if (edocType == EdocEnum.edocType.signReport.ordinal()) {
                    sb.append("  location.href='edocController.do?method=entryManager&entry=signReport';");
                } else {
                    sb.append("  location.href='edocController.do?method=entryManager&entry=sendManager';");
                }
                sb.append("}");
                rendJavaScript(response, sb.toString());
                return null;
            }
        } else if (Strings.isNotBlank(toFrom) && ("newEdocRegister").equals(toFrom) && !EdocHelper
                .isG6Version()) {/*
         * xiangfan 添加修复在兼职单位没有登记权限，点击登记的消息框，页面无限跳转的错误
         * GOV-5018 Start
         */
            boolean isEdocRegister = EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(),
                    AppContext.getCurrentUser().getId(), EdocEnum.edocType.recEdoc.ordinal());
            if (!isEdocRegister) {
                modelAndView = new ModelAndView("common/redirect");
                String msgKey = "alert_not_edocregister";
                // G6的登记是分发
                if (EdocHelper.isG6Version()) {
                    msgKey = "alert_not_edoc_fenfa";
                }
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", msgKey);
                rendJavaScript(response, "alert('" + errMsg + "');parent.location.reload();");
                return null;
            }
        } /* xiangfan 添加修复在兼职单位没有登记权限，点击登记的消息框，页面无限跳转的错误 GOV-5018 */

        String comm = request.getParameter("comm");
        if ("register".equals(comm) && "1".equals(request.getParameter("edocType"))) {
            String recieveId = request.getParameter("recieveId");
            if (Strings.isNotBlank(recieveId)) {
                EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveId));
                if (record == null || record.getStatus() == com.seeyon.v3x.exchange.util.Constants.C_iStatus_Retreat ||
                        // 待签收回退是这个状态 3
                        record.getStatus() == com.seeyon.v3x.exchange.util.Constants.C_iStatus_Receive_StepBacked) {// 被退回
                    String szJs = "alert('" + ResourceUtil.getString("edoc.alert.flow.edocStepBack1") + "');";// 公文已经被退回。
                    if (record != null) {
                        szJs = "alert('" + ResourceUtil.getString("edoc.alert.flow.edocStepBack", StringEscapeUtils.escapeJavaScript(record.getSubject()))
                                + "');";// 公文《"+record.getSubject()+"》已经被退回。
                    }
                    szJs += "   history.back();";
                    rendJavaScript(response, szJs);
                    return null;
                }
            }
        }

        return modelAndView;
    }

    // public ModelAndView entryManager(HttpServletRequest request,
    // HttpServletResponse response) throws Exception {
    // ModelAndView modelAndView = new
    // ModelAndView("edoc/edocFrameEntryIframe");
    // return modelAndView;
    // }
    public ModelAndView fullEditor(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/fullEditor");
        User user = AppContext.getCurrentUser();
        // GOV-4516
        // 【公文管理】-【发文管理】-【拟文】，公文起草时正文类型选择标准正文，编辑正文时IE标题栏还是显示的'致远A8-M协同管理软件'
        ConfigItem configItem_login = configManager.getConfigItem("System_Login_Title", "loginTitleName");
        boolean isFromTemplete = Strings.isNotBlank(request.getParameter("isFromTemplete"))
                ? Boolean.valueOf(request.getParameter("isFromTemplete")) : false;
        String title = null;
        if (configItem_login != null) {
            title = configItem_login.getConfigValue();
        }
        boolean flag = false;
        boolean hasQuoteDocumentPermission = true;
        if (Strings.isNotBlank(title)) {
            flag = true;
            modelAndView.addObject("title", title);
            modelAndView.addObject("flag", flag);
        }
        if (isFromTemplete || user.isAdmin()) {
            hasQuoteDocumentPermission = false;
        }
        modelAndView.addObject("hasInsertDocumentPermission", hasQuoteDocumentPermission);
        return modelAndView;

    }

    public ModelAndView newLeave(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/newLeave");
        return modelAndView;

    }

    /**
     * 收文管理
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView recManager(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String toFrom = request.getParameter("toFrom");
        String exchangeId = request.getParameter("exchangeId");
        String recieveId = request.getParameter("recieveId");
        String id = request.getParameter("id");
        String registerId = request.getParameter("registerId");
        String edocId = request.getParameter("edocId");
        String affairId = Strings.isBlank(request.getParameter("affairId")) ? "" : request.getParameter("affairId");
        String track = Strings.isBlank(request.getParameter("track")) ? "" : request.getParameter("track");
        String templeteId = request.getParameter("templeteId");
        String from = Strings.isBlank(request.getParameter("from")) ? "" : request.getParameter("from");
        String openFrom = Strings.isBlank(request.getParameter("openFrom")) ? "" : request.getParameter("openFrom");
        // G6版本签收后，点登记消息所要传递的参数：来文单位id
        String sendUnitId = request.getParameter("sendUnitId");
        // G6版本签收后，点登记消息所要传递的参数:待登记页签选中
        String recListType = request.getParameter("recListType");
        String registerType = request.getParameter("registerType");
        User user = AppContext.getCurrentUser();
        if (Strings.isNotBlank(affairId)) {
            Long affairIdLong = Long.valueOf(affairId);
            CtpAffair affair = affairManager.get(affairIdLong);
            if (null != affair
                    && Integer.valueOf(SubStateEnum.col_pending_unRead.getKey()).equals(affair.getSubState())) {
                affair.setSubState(SubStateEnum.col_pending_read.getKey());
                affairManager.updateAffair(affair);
            }
        }
        // branches_a8_v350_r_gov GOV-2641 唐桂林修改政务收文阅件链接 start
        if (Strings.isNotBlank(request.getParameter("objectId"))) {
            EdocSummary summary = edocSummaryManager.findById(Long.parseLong(request.getParameter("objectId")));
            if (summary != null && summary.getProcessType() != null && summary.getProcessType().longValue() == 2) {// 阅文
                if ("listDoneAll".equals(request.getParameter("listType"))) {// OA-65855首页点击已办数据分类，切换到已阅列表
                    toFrom = "listReaded";
                } else {
                    toFrom = "listReading";
                }
            } else if (summary != null && summary.getProcessType() != null
                    && summary.getProcessType().longValue() == 1) {// 办文
                // 取我的Affair，用于访问控制权限校验
                List<CtpAffair> myAffairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summary.getId(),
                        user.getId());
                CtpAffair affair = null;// 判断是否为暂存待办状态
                if (myAffairs != null) {
                    affair = myAffairs.get(0);
                    for (CtpAffair a : myAffairs) {
                        if (a.getState().intValue() == StateEnum.col_pending.getKey()) {// 待办Affaire
                            affair = a;
                            break;
                        }
                    }
                }
                if (affair != null && affair.getSubState() != null
                        && affair.getSubState().intValue() == SubStateEnum.col_pending_ZCDB.getKey()) {
                    toFrom = "listZcdb";
                }
            }
        }
        // branches_a8_v350_r_gov GOV-2641 唐桂林修改政务收文阅件链接 end
        StringBuilder url = new StringBuilder();
        url.append("/edocController.do?method=listIndex&edocType=" + EdocEnum.edocType.recEdoc.ordinal()).append(Functions.csrfSuffix());
        if (Strings.isNotBlank(toFrom)) {
            if (!"listSent".equals(request.getParameter("from"))) {
                from = toFrom;
            }
            url.append("&toFrom=" + toFrom);
        }
        if (Strings.isNotBlank(track)) {
            url.append("&track=" + track);
        }
        if (Strings.isNotBlank(registerId)) {
            url.append("&registerId=" + registerId);
        }
        if (Strings.isNotBlank(id)) {
            if (Strings.isBlank(registerId)) {
                url.append("&registerId=" + id);
            } else {
                url.append("&id=" + id);
            }
        }
        if (Strings.isNotBlank(recieveId)) {
            url.append("&recieveId=" + recieveId);
        }
        if (Strings.isNotBlank(sendUnitId)) {
            url.append("&sendUnitId=" + sendUnitId);
        }
        if (Strings.isNotBlank(recListType)) {
            url.append("&recListType=" + recListType);
        }
        if (Strings.isNotBlank(exchangeId)) {
            url.append("&exchangeId=" + exchangeId);
        }
        if (Strings.isNotBlank(edocId)) {
            url.append("&edocId=" + edocId);
        }
        if (Strings.isNotBlank(templeteId)) {
            url.append("&templeteId=" + templeteId);
        }
        if (Strings.isNotBlank(affairId)) {
            url.append("&affairId=" + affairId);
        }
        if (Strings.isNotBlank(request.getParameter("app"))) {
            url.append("&app=" + request.getParameter("app"));
        }
        if (Strings.isNotBlank(request.getParameter("summaryId"))) {
            url.append("&summaryId=" + request.getParameter("summaryId"));
        }
        if (Strings.isNotBlank(registerType)) {
            url.append("&registerType=" + registerType);
        }

        String comm = Strings.isBlank(request.getParameter("comm")) ? "" : request.getParameter("comm");
        if ("newEdoc".equals(toFrom) && Strings.isBlank(templeteId)) {// 表示收文分发
            if (Strings.isBlank(comm) && Strings.isBlank(request.getParameter("summaryId"))) {
                comm = "distribute";
            }
        }
        url.append("&comm=" + comm);
        url.append("&from=" + from);
        String listType = Strings.isBlank(request.getParameter("listType")) ? "listPending"
                : request.getParameter("listType");
        url.append("&listType=" + listType);
        url.append("&backBoxToEdit=" + request.getParameter("backBoxToEdit"));
        if (Strings.isNotBlank(openFrom)) {
            url.append("&openFrom=" + openFrom);
        }
        url.append(Functions.csrfSuffix());
        return super.redirectModelAndView(url.toString());
    }

    /**
     * 发文管理
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView sendManager(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String affairId = Strings.isBlank(request.getParameter("affairId")) ? "" : request.getParameter("affairId");
        String track = Strings.isBlank(request.getParameter("track")) ? "" : request.getParameter("track");
        String templeteId = Strings.isNotBlank(request.getParameter("templeteId")) ? request.getParameter("templeteId")
                : "";
        String toFrom = request.getParameter("toFrom");
        String from = Strings.isBlank(request.getParameter("from")) ? "" : request.getParameter("from");
        String openFrom = Strings.isBlank(request.getParameter("openFrom")) ? "" : request.getParameter("openFrom");
        String meetingSummaryId = request.getParameter("meetingSummaryId");
        String comm = request.getParameter("comm");
        String edocId = request.getParameter("edocId");
        String summaryId = Strings.isBlank(request.getParameter("summaryId")) ? ""
                : (request.getParameter("summaryId"));
        String transmitSendNewEdocId = request.getParameter("transmitSendNewEdocId");
        // 来自首页待发的连接地址，需要接收指定回退参数
        String backBoxToEdit = request.getParameter("backBoxToEdit");
        if (Strings.isNotBlank(toFrom)) {
            from = toFrom;
        }
        String modelType = request.getParameter("modelType");
        String listType = Strings.isBlank(request.getParameter("listType")) ? "listPending"
                : request.getParameter("listType");
        if (Strings.isNotBlank(from)) {
            listType = from;
        }
        return super.redirectModelAndView("/edocController.do?method=listIndex&comm=" + comm + "&from=" + from
                + "&toFrom=" + toFrom + "&track=" + track + "&controller=edocController.do&edocType="
                + EdocEnum.edocType.sendEdoc.ordinal() + "&templeteId=" + templeteId + "&affairId=" + affairId
                + (Strings.isNotBlank(meetingSummaryId) ? "&meetingSummaryId=" + meetingSummaryId : "") + "&edocId="
                + edocId + "&transmitSendNewEdocId=" + transmitSendNewEdocId
                + (Strings.isBlank(modelType) ? "" : "&modelType=" + modelType) + "&summaryId=" + summaryId
                + "&listType=" + listType + "&backBoxToEdit=" + backBoxToEdit
                + (Strings.isBlank(openFrom) ? "" : "&openFrom=" + openFrom)
                + Functions.csrfSuffix());
    }

    /**
     * 签报管理
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView signReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String affairId = Strings.isBlank(request.getParameter("affairId")) ? "" : request.getParameter("affairId");
        String track = Strings.isBlank(request.getParameter("track")) ? "" : request.getParameter("track");
        String toFrom = request.getParameter("toFrom");
        String templeteId = Strings.isNotBlank(request.getParameter("templeteId")) ? request.getParameter("templeteId")
                : "";
        String from = Strings.isBlank(request.getParameter("from")) ? "" : request.getParameter("from");
        String summaryId = Strings.isBlank(request.getParameter("summaryId")) ? ""
                : (request.getParameter("summaryId"));
        // 来自首页待发的连接地址，需要接收指定回退参数
        String backBoxToEdit = request.getParameter("backBoxToEdit");
        if (Strings.isNotBlank(toFrom)) {
            from = toFrom;
        }
        String listType = Strings.isBlank(request.getParameter("listType")) ? "listPending"
                : request.getParameter("listType");
        if (Strings.isNotBlank(from)) {
            listType = from;
        }
        String openFrom = Strings.isBlank(request.getParameter("openFrom")) ? "" : request.getParameter("openFrom");
        return super.redirectModelAndView("/edocController.do?method=listIndex&from=" + from + "&toFrom=" + toFrom
                + "&track=" + track + "&controller=edocController.do&edocType=" + EdocEnum.edocType.signReport.ordinal()
                + "&templeteId=" + templeteId + "&affairId=" + affairId + "&summaryId=" + summaryId + "&listType="
                + listType + "&backBoxToEdit=" + backBoxToEdit + "&openFrom=" + openFrom + Functions.csrfSuffix());
    }

    private void setReadEdocIsView(ModelAndView modelAndView) throws Exception {
        // 根据后台开关来控制是否显示办文、阅文按钮
        boolean readFlag = EdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
        modelAndView.addObject("readFlag", String.valueOf(readFlag));
    }

    /**
     * 新建公文
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView newEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("edoc/newEdoc");
        User user = AppContext.getCurrentUser();
        if ("true".equals(request.getParameter("quickView"))) {
            modelAndView = new ModelAndView("edoc/newEdoc_quickview");
        }

        String register = request.getParameter("register");
        String meetingSummaryId = request.getParameter("meetingSummaryId");// 会议纪要转公文
        // 获得会议纪要ID
        String comm = request.getParameter("comm");
        String from = request.getParameter("from");
        String s_summaryId = request.getParameter("summaryId");// 表单ID
        String templeteId = request.getParameter("templeteId");// 表单模板ID
        String oldtempleteId = request.getParameter("oldtempleteId");// 原始模板ID
        String edocType = request.getParameter("edocType");
        boolean hasDoc = AppContext.hasPlugin("doc");// 是否有文档模块

        modelAndView.addObject("hasDoc", hasDoc);
        modelAndView.addObject("pageview", request.getParameter("pageview"));// 已阅、待阅列表中阅转办页面跳转标识
        // wangjingjing
        modelAndView.addObject("source", request.getParameter("source"));

        // 发文、收文、签报各自设置默认节点权限
        String configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        if (String.valueOf(EdocEnum.edocType.sendEdoc.ordinal()).equals(edocType)) {
            configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        } else if (String.valueOf(EdocEnum.edocType.recEdoc.ordinal()).equals(edocType)) {
            configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if (String.valueOf(EdocEnum.edocType.signReport.ordinal()).equals(edocType)) {
            configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
        }
        PermissionVO permission = permissionManager.getDefaultPermissionByConfigCategory(configCategory,
                user.getLoginAccount());
        modelAndView.addObject("defaultNodeName", permission.getName());
        modelAndView.addObject("defaultNodeLable", permission.getLabel());

        String subTypeStr = request.getParameter("subType");
        String registerIdStr = request.getParameter("registerId");

        /**
         * 参数定义
         */
        String checkOption = "";// 判断撰文类型
        String openForm = "";

        // 设置阅读页签的显示权限
        setReadEdocIsView(modelAndView);

        NewEdocHandle handle = null;
        /**
         * 转发
         */
        if ("transmitSend".equals(comm)) {
            handle = new TransmitSendEdocImpl();
        }
        /**
         * 收文分发
         */
        else if ("distribute".equals(comm)) {
            handle = new DistributeToSendEdocImpl();
        }
        /**
         * 调用模板
         */
        else if (StringUtils.isNotEmpty(templeteId)) {
            modelAndView.addObject("fromTemplateFlag", "true");
            CtpTemplate ctpTemplate = templeteManager.getCtpTemplate(Long.valueOf(templeteId));
            if (null != ctpTemplate) {
                if (!ctpTemplate.isSystem()) {
                    modelAndView.addObject("personalTemplateFlag", "1");
                }
                modelAndView.addObject("newTemplateType", ctpTemplate.getType());//模板类型
            }
            handle = new FromTemplateToSendEdocImpl();
        }
        /**
         * 来自待发
         */
        else if (Strings.isNotBlank(s_summaryId)) {
            handle = new WaitForSendEdocImpl();
            openForm = EdocOpenFrom.listWaitSend.name();
        }
        /**
         * 关联发文/收文
         */
        else if ("forwordtosend".equals(comm)) {
            handle = new ForwordtosendEdocImpl();
        }
        /**
         * A8登记
         */
        else if ("register".equals(comm)) {
            handle = new A8RegisterEdocImpl();
        }
        /**
         * 直接新建
         */
        else {
            handle = new CreateNewEdocImpl();
        }

        handle.setParams(register, meetingSummaryId, comm, from, s_summaryId, templeteId, oldtempleteId, edocType,
                subTypeStr, registerIdStr, checkOption, openForm);

        modelAndView = handle.execute(request, response, modelAndView);
        if (modelAndView == null) {
            return null;
        }
        // 不是调用模板和待发编辑时
        if (StringUtils.isEmpty(templeteId) && Strings.isBlank(s_summaryId)) {
            String docMarkByTemplateJs = EdocHelper.exeTemplateMarkJs(null, null, null);
            modelAndView.addObject("docMarkByTemplateJs", docMarkByTemplateJs);
        }
        if (modelAndView != null) {
            modelAndView.addObject("isG6", EdocHelper.isG6Version());
        }
        String _trackValue = customizeManager.getCustomizeValue(AppContext.getCurrentUser().getId(),
                CustomizeConstants.TRACK_SEND);
        if (Strings.isBlank(_trackValue)) {
            modelAndView.addObject("customSetTrack", "true");
        } else {
            modelAndView.addObject("customSetTrack", _trackValue);
        }
        // 需要获得创建公文的单位的开关_正文套红日期
        boolean taohongriqiSwitch = EdocSwitchHelper.taohongriqiSwitch(AppContext.currentAccountId());
        modelAndView.addObject("taohongriqiSwitch", taohongriqiSwitch);

        String affairId = request.getParameter("affairId");
        String subState = request.getParameter("subState");
        if (Strings.isNotBlank(subState)) {
            modelAndView.addObject("subState", subState);
        } else if (Strings.isNotBlank(affairId)) {
            CtpAffair affair = affairManager.get(Long.valueOf(affairId));
            if (affair != null) {
                modelAndView.addObject("subState", affair.getSubState());
            }
        }
        return modelAndView;
    }

    public ModelAndView showTime(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/test");
        Map<String, Object> tempMap = new HashMap<String, Object>();
        String time = "2013-4-28 00:00:00";
        java.util.Date begin = Datetimes.parseDatetime(time);
        String etime = "2013-12-29 00:00:00";
        java.util.Date end = Datetimes.parseDatetime(etime);
        tempMap.put("beginDate", begin);
        tempMap.put("endDate", end);
        tempMap.put("currentUserID", AppContext.getCurrentUser().getId());

        List<EdocSummaryModel> list = edocManager.getMyEdocDeadlineNotEmpty(tempMap);
        mav.addObject("list", list);
        return mav;
    }

    /**
     * 新建登记单页面数据加载 -唐桂林 2011-10-12
     */
    public ModelAndView newEdocRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/newEdocRegister");
        User user = AppContext.getCurrentUser();
        String comm = Strings.isBlank(request.getParameter("comm")) ? "create" : request.getParameter("comm");// 新建
        // /修改
        int edocType = Strings.isBlank(request.getParameter("edocType")) ? 1
                : Integer.parseInt(request.getParameter("edocType"));
        long registerId = Strings.isBlank(request.getParameter("registerId")) ? -1
                : Long.parseLong(request.getParameter("registerId"));
        long recieveId = Strings.isBlank(request.getParameter("recieveId")) ? -1
                : Long.parseLong(request.getParameter("recieveId"));
        long exchangeId = Strings.isBlank(request.getParameter("exchangeId")) ? -1
                : Long.parseLong(request.getParameter("exchangeId"));
        long edocId = Strings.isBlank(request.getParameter("edocId")) ? -1
                : Long.parseLong(request.getParameter("edocId"));
        int registerType = Strings.isBlank(request.getParameter("registerType"))
                ? EdocNavigationEnum.RegisterType.ByAutomatic.ordinal()
                : Integer.parseInt(request.getParameter("registerType"));
        long sendUnitId = Strings.isBlank(request.getParameter("sendUnitId")) ? -1
                : Long.parseLong(request.getParameter("sendUnitId"));
        long affairId = Strings.isBlank(request.getParameter("affairId")) ? -1
                : Long.parseLong(request.getParameter("affairId"));
        if (exchangeId != -1) {
            recieveId = exchangeId;
        }

        /********* 关联发文 * 收文的Id,收文的Type *********/
        String relSends = "haveNot";
        // String relRecs = "haveNot";
        List<EdocSummary> newEdocList = this.edocSummaryRelationManager.findNewEdoc(edocId, user.getId(), 1);
        if (newEdocList != null) {
            relSends = "haveMany";
            modelAndView.addObject("recEdocId", edocId);
            modelAndView.addObject("recType", 1);
            modelAndView.addObject("relSends", relSends);
        }
        /********* 关联 发文的Id 发文的Type **********/

        EdocRegister edocRegister = null;
        RegisterBody registerBody = null;
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        EdocRecieveRecord record = null;
        long agentId = -1;// 代理人
        long agentToId = -1;// 被代理人
        List<EdocMarkModel> serialNoList = new ArrayList<EdocMarkModel>();
        String strTemp = "";
        // 修复bug GOV-3204 公文管理-首页的BUG
        CtpAffair affair = affairManager.get(affairId);
        if ("create".equals(comm)) {// 新建
            edocRegister = new EdocRegister();
            // 签收单
            record = recieveEdocManager.getEdocRecieveRecord(recieveId);
            /**
             * 登记公文，判断当前操作人是否可以登记此公文
             */
            // 指定登记人
            long registerUserId = record == null ? user.getId() : record.getRegisterUserId();
            // 是否有登记权
            // 如果当前用户与登记用户不同，判断是代理人
            Long agentMember = 0L;

            // 当registerUserId为0时，表示登记竞争执行
            if (registerUserId != 0 && registerUserId != user.getId()) {
                Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                        registerUserId);
                if (agentMemberId != null) {
                    agentMember = agentMemberId;
                    agentId = Long.valueOf(agentMemberId);
                    agentToId = registerUserId;
                    if (user.getId() != agentId) {// 如果当前用户与登记人的代理人不同，则表示公文登记人的代理人已转换
                        String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "alert_hasChanged_register");
                        StringBuffer sb = new StringBuffer();
                        sb.append("alert(\"" + errMsg + "\");");
                        sb.append("if(window.dialogArguments){"); // 弹出
                        sb.append("   window.returnValue = \"true\";");
                        sb.append("   window.close();");
                        sb.append("} else {");
                        if ("agent".equals(request.getParameter("app"))) {
                            sb.append(
                                    "	parent.parent.parent.location.href='main.do?method=morePending4App&app=agent';");
                        } else {
                            if (EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal())) {
                                sb.append(
                                        "	parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&edocType="
                                                + edocType + "';");
                            } else {
                                sb.append(
                                        "	parent.parent.location.href='edocController.do?method=listIndex&from=listPending&edocType="
                                                + edocType + "';");
                            }
                        }
                        sb.append("}");
                        rendJavaScript(response, sb.toString());
                        return null;
                    }
                } else {
                    String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "alert_hasChanged_register");
                    StringBuffer sb = new StringBuffer();
                    sb.append("alert(\"" + errMsg + "\");");
                    sb.append("if(window.dialogArguments){"); // 弹出
                    sb.append("   window.returnValue = \"true\";");
                    sb.append("   window.close();");
                    sb.append("} else {");
                    if (EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal())) {
                        sb.append(
                                "	parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&edocType="
                                        + edocType + "';");
                    } else {
                        sb.append(
                                "	parent.parent.location.href='edocController.do?method=listIndex&from=listPending&edocType="
                                        + edocType + "';");
                    }
                    sb.append("}");
                    rendJavaScript(response, sb.toString());
                    return null;
                }
            } else {
                // GOV-4808.安全测试：aqc没有单位公文管理员-收文管理-登记权限，输入地址却能访问对应界面 start
                if (!EdocHelper.isG6Version()
                        && !EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal())) {
                    return refreshWorkspace();
                }
                // GOV-4808.安全测试：aqc没有单位公文管理员-收文管理-登记权限，输入地址却能访问对应界面 end
            }
            // 如果公文回退了
            if (record != null && (record.getStatus() == EdocRecieveRecord.Exchange_iStatus_Torecieve
                    || (record.getIsRetreat() == EdocRecieveRecord.Receive_Retreat_Yes))) {
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "alert_hasBeStepBack_already");
                StringBuffer sb = new StringBuffer();
                sb.append("alert(\"" + errMsg + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("}else{");
                if (EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal())) {
                    sb.append(
                            "	parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&edocType=1&listType=listPending';");
                } else {
                    sb.append(
                            "	parent.parent.location.href='edocController.do?method=listIndex&from=listPending&edocType=1&listType=listPending';");
                }
                sb.append("}");
                sb.append("");
                rendJavaScript(response, sb.toString());
                return null;
            }
            // 修复bug GOV-3204 公文管理-首页的BUG
            // 当公文已经登记后，再待分发中退回时，并没有对签收表中的状态进行改变，而且affair的状态为3,表示待处理的状态，因此首页能够显示出它属于
            // 待登记的状态，因此这里再加上 affair的状态判断，这种情况就可以进行处理了
            boolean hasRegisted = false;
            if (affair != null) {
                // 公文已经登记
                if (affair.getState() != StateEnum.col_pending.key()) {
                    if (record != null && record.getStatus() == EdocRecieveRecord.Exchange_iStatus_Registered) {
                        hasRegisted = true;
                    }
                }
            } else {
                if (record != null && record.getStatus() == EdocRecieveRecord.Exchange_iStatus_Registered) {
                    hasRegisted = true;
                }
            }
            if (hasRegisted) {
                modelAndView = new ModelAndView("common/redirect");
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "alert_has_registe");
                modelAndView.addObject("redirectURL", BaseController.REDIRECT_BACK);
                StringBuffer sb = new StringBuffer();
                sb.append("alert(\"" + errMsg + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("} else {");
                // 如果当前事项的代理人是当前代理人的情况下，就进入代理事项列表
                if (("agent".equals(request.getParameter("app")) || user.getId().equals(agentMember))
                        && user.getId() != record.getRegisterUserId()) {// 代理人跳转到代理事项
                    sb.append(
                            "parent.parent.parent.location.href='collaboration/pending.do?method=morePending&from=Agent';");
                } else {
                    // sb.append("parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&listType=registerDone&edocType="+edocType+"'");
                    sb.append(
                            "parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&listType=listRegister&recListType=registerPending&edocType="
                                    + edocType + "'");
                }
                sb.append("}");
                sb.append("");
                rendJavaScript(response, sb.toString());
                return null;
            }

            // 装载数据到登记表
            edocRegister.setId(-1L);
            /*
             * if(agentToId != -1){ edocRegister.setCreateUserId(agentToId);
             * edocRegister.setCreateUserName(orgManager.getMemberById(agentToId
             * ).getName()); }else{
             */
            edocRegister.setCreateUserId(user.getId());
            edocRegister.setCreateUserName(user.getName());
            // }
            edocRegister.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
            edocRegister.setUpdateTime(new java.sql.Timestamp(new Date().getTime()));

            Long exchangeOrgId = null;
            if (record == null) {
                exchangeOrgId = user.getLoginAccount();
            } else {
                exchangeOrgId = ExchangeUtil.getAccountIdOfRegisterByOrgIdAndOrgType(record.getExchangeOrgId(),
                        record.getExchangeType());
            }

            edocRegister.setOrgAccountId(exchangeOrgId);

            edocRegister.setState(EdocNavigationEnum.RegisterState.WaitRegister.ordinal());
            // 设置登记人
            if (registerUserId == 0) {
                registerUserId = user.getId();
            }

            edocRegister.setRegisterUserId(registerUserId);
            V3xOrgMember v3xOrgMember = orgManager.getMemberById(registerUserId);
            edocRegister.setRegisterUserName(v3xOrgMember == null ? user.getName() : v3xOrgMember.getName());
            // 设置默认分发人
            if (registerUserId != user.getId()) {
                edocRegister.setDistributerId(registerUserId);
                edocRegister.setDistributer(orgManager.getMemberById(registerUserId).getName());
            } else {
                edocRegister.setDistributerId(user.getId());
                edocRegister.setDistributer(user.getName());
            }
            edocRegister.setRegisterType(registerType);
            edocRegister.setRegisterDate(new java.sql.Date(new Date().getTime()));
            // 来文信息
            edocRegister.setEdocId(edocId);
            edocRegister.setRecieveId(recieveId);

            // 从EdocSummary对象中取的数据
            EdocSummary summary = edocManager.getEdocSummaryById(edocId, true);
            if (sendUnitId == -1 && summary != null) {
                sendUnitId = summary.getOrgAccountId();
            }
            V3xOrgAccount account = orgManager.getAccountById(sendUnitId);
            edocRegister.setSendUnit(account == null ? "" : account.getName());
            edocRegister.setSendUnitId(sendUnitId);
            if (record != null) {
                edocRegister.setSendUnitType(record.getSendUnitType());
                edocRegister.setDocType(record.getDocType());
                edocRegister.setSecretLevel(record.getSecretLevel());
                edocRegister.setUrgentLevel(record.getUrgentLevel());
            }
            edocRegister.setEdocType(EdocEnum.edocType.recEdoc.ordinal());
            edocRegister.setSubject(record == null ? "" : record.getSubject());
            edocRegister.setDocMark(record == null ? "" : record.getDocMark());
            // 公文元素基本信息
            edocRegister.setKeepPeriod(summary == null ? record == null ? "" : record.getKeepPeriod()
                    : String.valueOf(summary.getKeepPeriod()));
            if (summary != null) {
                edocRegister.setSendType(summary.getSendType());
            }
            edocRegister.setKeywords(summary == null ? "" : summary.getKeywords());
            edocRegister.setIssuerId(-1l);
            edocRegister.setIssuer(summary == null ? "" : summary.getIssuer());
            edocRegister.setEdocDate(summary == null ? null : summary.getSigningDate());
            edocRegister.setUnitLevel(summary == null ? "" : summary.getUnitLevel());
            // 字段edocDate将做为签发日期保存，字段issueDate暂不用
            /*
             * if(edocRegister.getEdocDate()==null) {//如果没有签发时间，则显示为封发时间
             * edocRegister.setEdocDate(summary==null ? null : new
             * java.sql.Date(summary.getPackTime().getTime())); }
             */
            // lijl添加,GOV-3061.收文管理-登记的时候，怎么没有把主送单位带进来呢？
            // 增加主送单位
            // GOV-4693.公文管理-公文登记时，抄送单位，抄报单位，印发单位等都在主送单位框内显示 start

            String sendToId = null;
            String sendToNames = null;

            // 获取送文单上的主送单位
            if (record != null) {
                if (Strings.isNotBlank(record.getReplyId())) {

                    long sendDetailId = Long.parseLong(record.getReplyId());
                    EdocSendDetail sendDetail = sendEdocManager.getSendRecordDetail(sendDetailId);
                    if (sendDetail != null) {
                        long sendId = sendDetail.getSendRecordId();
                        EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecord(sendId);

                        sendToId = sendRecord.getSendedTypeIds();
                        sendToNames = sendRecord.getSendEntityNames();
                    }
                }
            }

            edocRegister.setSendTo(sendToNames);
            edocRegister.setSendToId(sendToId);

            // GOV-4693.公文管理-公文登记时，抄送单位，抄报单位，印发单位等都在主送单位框内显示 end

            // 如果来文流程中有会签节点，则设置会签节点处理人
            if (summary != null) {
                List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), StateEnum.col_done);
                if (affairs != null && affairs.size() > 0) {
                    for (CtpAffair aff : affairs) {
                        if ("huiqian".equals(aff.getNodePolicy())) {
                            V3xOrgMember member = orgManager.getMemberById(aff.getMemberId());
                            edocRegister.setSigner(member.getName());
                            break;
                        }
                    }
                }
            }

            List<Attachment> attachmentListCopy = new ArrayList<Attachment>();
            // 附件信息
            if (summary != null) {
                attachmentList = attachmentManager.getByReference(summary.getId(), summary.getId());

                // 只要拟文添加和处理修改添加的附件，不要处理时填意见所添加的附件
                for (Attachment att : attachmentList) {
                    // type=0表示附件 (附件元素上不显示关联文档)
                    if (att.getType() == 0 && att.getSubReference().longValue() == summary.getId()) {
                        attachmentListCopy.add(att);
                    }
                }

            }
            edocRegister.setIdentifier("00000000000000000000");
            edocRegister.setAttachmentList(attachmentListCopy);
            edocRegister.setHasAttachments(attachmentListCopy.size() > 0);
            // 装载公文正文
            if (summary != null && record != null) {
                EdocBody edocBody = summary.getBody(record.getContentNo().intValue());
                registerBody = new RegisterBody();
                edocBody = edocBody == null ? summary.getFirstBody() : edocBody;
                if (null != edocBody) {
                    registerBody.setId(-1L);
                    registerBody.setContent(edocBody.getContent());
                    registerBody.setContentNo(edocBody.getContentNo());
                    registerBody.setContentType(edocBody.getContentType());
                    registerBody.setCreateTime(edocBody.getLastUpdate());
                    edocRegister.setRegisterBody(registerBody);
                }
            } else {
                registerBody = new RegisterBody();
                String bodyContentType = Constants.EDITOR_TYPE_OFFICE_WORD;
                if (com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx") == false) {
                    bodyContentType = Constants.EDITOR_TYPE_HTML;
                }
                registerBody.setId(-1L);
                registerBody.setContentType(bodyContentType);
                registerBody.setCreateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
                edocRegister.setRegisterBody(registerBody);
            }
            if (summary != null && !Strings.isBlank(summary.getSendUnit())
                    && !Strings.isBlank(summary.getSendUnitId())) {
                edocRegister.setEdocUnit(summary.getSendUnit());
                edocRegister.setEdocUnitId(summary.getSendUnitId());
            } else {// lijl添加else，如果来文单位为空,则取record里的来文单位
                edocRegister.setEdocUnit(record == null ? "" : record.getSendUnit());
            }

            edocRegister.setSerialNo("");
        } else {// 修改
            // 处理已登记中编辑(这时候可以从已登记列表中 获取登记id)
            edocRegister = edocRegisterManager.findRegisterById(registerId);

            // 处理被分发退回，待登记中 登记(只能通过签收id来获取登记对象)
            if (edocRegister == null) {
                edocRegister = edocRegisterManager.findRegisterByRecieveId(recieveId);
            }
            if (edocRegister == null) {// 该登记不存在
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "alert_edocregister_isnotexsit");
                StringBuffer sb = new StringBuffer();
                sb.append("alert(\"" + errMsg + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("   window.returnValue = \"true\";");
                sb.append("   window.close();");
                sb.append("} else {");
                sb.append("   parent.location.href='edocController.do?method=edocFrame&from=listRegister&edocType="
                        + edocType + "';");
                sb.append("}");
                rendJavaScript(response, sb.toString());
                return null;
            }
            // 公文已经登记
            if (affair != null && affair.getState() != StateEnum.col_pending.key()) {
                modelAndView = new ModelAndView("common/redirect");
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "alert_has_registe");
                modelAndView.addObject("redirectURL", BaseController.REDIRECT_BACK);
                StringBuffer sb = new StringBuffer();
                sb.append("alert(\"" + errMsg + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("} else {");
                // sb.append("parent.parent.location.href='edocController.do?method=listIndex&from=listRegister&listType=listRegister&recListType=registerPending&edocType="+edocType+"'");
                sb.append("history.back();");
                sb.append("}");
                sb.append("");
                rendJavaScript(response, sb.toString());
                return null;
            }
            if (Strings.isNotBlank(edocRegister.getSerialNo())) {
                EdocMarkModel emTemp = null;
                try {
                    emTemp = EdocMarkModel.parse(edocRegister.getSerialNo());
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
                if (emTemp == null) {
                    emTemp = new EdocMarkModel();
                    emTemp.setMark(Strings.toHTML(Strings.toXmlStr(edocRegister.getSerialNo())));
                    // emTemp.setWordNo("0|" +
                    // Strings.toHTML(Strings.toXmlStr(edocRegister.getSerialNo()),false)
                    // + "||" +
                    // com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_NONE);
                    // 文号值改为和显示值一样，不用两端加 | 符号
                    emTemp.setWordNo(Strings.toHTML(Strings.toXmlStr(edocRegister.getSerialNo())));
                } else {
                    strTemp = emTemp.getMarkDefinitionId() + "|" + emTemp.getMark() + "|" + emTemp.getCurrentNo() + "|"
                            + com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW;
                    emTemp.setMark(Strings.toHTML(emTemp.getMark()));
                    emTemp.setWordNo(Strings.toHTML(strTemp));
                }
                serialNoList.add(emTemp);
            }
            registerBody = edocRegisterManager.findRegisterBodyByRegisterId(registerId);
            attachmentList = attachmentManager.getByReference(edocRegister.getId(), edocRegister.getId());
            edocRegister.setAttachmentList(attachmentList);
        }

        if (edocRegister.getRegisterUserId() != null && edocRegister.getRegisterUserId() == 0) {
            edocRegister.setRegisterUserId(user.getId());
            edocRegister.setRegisterUserName(user.getName());
            edocRegister.setDistributerId(user.getId());
            edocRegister.setDistributer(user.getName());
        }

        // Long _orgAccountId=V3xOrgEntity.VIRTUAL_ACCOUNT_ID;
        String deptIds = orgManager.getUserIDDomain(user.getId(), user.getLoginAccount(),
                V3xOrgEntity.ORGENT_TYPE_DEPARTMENT, V3xOrgEntity.ORGENT_TYPE_ACCOUNT);
        EdocMarkModel markModel = null;
        List<EdocMarkModel> list = edocMarkDefinitionManager.getEdocMarkDefinitions(deptIds,
                EdocEnum.MarkType.edocInMark.ordinal());
        if (list != null && list.size() > 0) {
            for (int x = 0; x < list.size(); x++) {
                markModel = (EdocMarkModel) list.get(x);
                if (null != markModel) {
                    Long definitionId = markModel.getMarkDefinitionId();
                    strTemp = definitionId + "|" + markModel.getMark() + "|" + markModel.getCurrentNo() + "|"
                            + com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW;
                    if (strTemp.equals(edocRegister.getSerialNo())) {
                        continue;
                    }
                    markModel.setMark(Strings.toHTML(markModel.getMark()));
                    markModel.setWordNo(Strings.toHTML(strTemp));
                    serialNoList.add(markModel);
                }
            }
        }
        /** 基础信息装载 */
        modelAndView.addObject("comm", comm);
        modelAndView.addObject("affairId", affairId);
        modelAndView.addObject("agentId", agentId);
        modelAndView.addObject("agentToId", agentToId);
        modelAndView.addObject("oldDistributerId", edocRegister.getDistributerId());
        modelAndView.addObject("controller", "edocController.do");
        /** 权限信息装载 */
        boolean canDeleteOriginalAtts = true;// 是否能删除附件
        boolean cloneOriginalAtts = "create".equals(comm) && record != null;// 是否能复制正文
        boolean canUpdateContent = (edocRegister.getRegisterType() != null
                && (edocRegister.getRegisterType() == 2 || edocRegister.getRegisterType() == 3)) ? true
                : EdocSwitchHelper.canUpdateAtOutRegist();
        ;// 是否能修改正文
        modelAndView.addObject("canDeleteOriginalAtts", canDeleteOriginalAtts);
        modelAndView.addObject("cloneOriginalAtts", cloneOriginalAtts);
        modelAndView.addObject("personInput", EdocSwitchHelper.canInputEdocWordNum());
        modelAndView.addObject("canUpdateContent", canUpdateContent);
        /** 枚举信息装载 */
        CtpEnumBean remindMetadata = enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
        CtpEnumBean deadlineMetadata = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name());
        modelAndView.addObject("remindMetadata", remindMetadata);
        modelAndView.addObject("deadlineMetadata", deadlineMetadata);
        modelAndView.addObject("appName", EdocEnum.getEdocAppName(edocType));
        modelAndView.addObject("templeteCategrory", EdocEnum.getTemplateCategory(edocType));

        CtpEnumBean edocTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_type.name());// 公文种类
        CtpEnumBean sendUnitTypeData = enumManagerNew.getEnum(EnumNameEnum.send_unit_type.name());// 来文类别
        CtpEnumBean edocSecretLevelData = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());// 密级程度
        CtpEnumBean edocUrgentLevelData = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());// 紧急程度
        CtpEnumBean edocKeepPeriodData = enumManagerNew.getEnum(EnumNameEnum.edoc_keep_period.name());// 保密期限
        CtpEnumBean edocUnitLevelData = enumManagerNew.getEnum(EnumNameEnum.edoc_unit_level.name());
        modelAndView.addObject("edocTypeMetadata", edocTypeMetadata);
        modelAndView.addObject("sendUnitTypeData", sendUnitTypeData);
        modelAndView.addObject("edocSecretLevelData", edocSecretLevelData);
        modelAndView.addObject("edocUrgentLevelData", edocUrgentLevelData);
        modelAndView.addObject("edocKeepPeriodData", edocKeepPeriodData);
        modelAndView.addObject("edocUnitLevelData", edocUnitLevelData);
        modelAndView.addObject("serialNoList", serialNoList);
        /** 对象装载 */
        modelAndView.addObject("bean", edocRegister);
        modelAndView.addObject("registerBody", registerBody);
        modelAndView.addObject("attachments", edocRegister.getAttachmentList());
        modelAndView.addObject("forwordtosend", "1");// 登记单打开正文的js中用
        // wangjingjing
        // lijl添加appType----------GOV-2225.公文管理-收文管理-登记-待登记页面，打开一条公文进行登记，不能插入关联文档，不正确。
        int iEdocType = -1;
        String edocType1 = request.getParameter("edocType");
        if (edocType1 != null && !"".equals(edocType1)) {
            iEdocType = Integer.parseInt(edocType1);
        }
        modelAndView.addObject("appType", EdocUtil.getAppCategoryByEdocType(iEdocType).getKey());
        return modelAndView;
    }

    /**
     * 保存登记单 -唐桂林2011-10-12
     */
    public ModelAndView saveRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String comm = Strings.isBlank(request.getParameter("comm")) ? "create" : request.getParameter("comm");// 新建
        // /修改
        // String listType =
        // Strings.isBlank(request.getParameter("recListType"))? "listRegister"
        // : request.getParameter("recListType");//新建 /修改
        long registerId = Strings.isBlank(request.getParameter("id")) ? -1L
                : Long.parseLong(request.getParameter("id"));
        long agentId = Strings.isBlank(request.getParameter("agentId")) ? -1
                : Long.parseLong(request.getParameter("agentId"));// 代理人id
        // long agentToId = Strings.isBlank(request.getParameter("agentToId")) ?
        // -1 : Long.parseLong(request.getParameter("agentToId"));//代理人id
        long oldDistributerId = Strings.isBlank(request.getParameter("oldDistributerId")) ? -1
                : Long.parseLong(request.getParameter("oldDistributerId"));// 原来的分发人

        String registerState = request.getParameter("state");
        EdocRegister oldRegister = edocRegisterManager.getEdocRegister(registerId);
        RegisterBody registerBody = null;
        if (oldRegister != null) {// oldRegister为空，查询报错
            registerBody = edocRegisterManager.findRegisterBodyByRegisterId(registerId);
        }

        // 刷新或跳转到待登记待发列表
        StringBuilder refreshDraftScript = new StringBuilder();
        refreshDraftScript.append("if(window.dialogArguments){"); // 弹出
        refreshDraftScript.append("   window.returnValue = \"true\";");
        refreshDraftScript.append("   window.close();");
        refreshDraftScript.append("} else {");
        refreshDraftScript.append(
                " parent.parent.window.location.href='edocController.do?method=listIndex&edocType=1&from=listRegister&listType=listRegister&recListType=registerDraft';");
        refreshDraftScript.append("}");

        // 修改签收单
        String recieveIdStr = request.getParameter("recieveId");
        long recieveId = recieveIdStr == null ? 0 : Long.parseLong(recieveIdStr);
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(recieveId);

        // 权限与状态验证
        String registUserIdStr = request.getParameter("registerUserId");
        long registUserId = registUserIdStr == null ? 0 : Long.parseLong(registUserIdStr);
        if (oldRegister == null && recieveId > 0) {
            oldRegister = edocRegisterManager.findRegisterByRecieveId(recieveId);
        }
        String errorMsg = checkValid(registUserId, oldRegister, edocRecieveRecord);
        if (Strings.isNotBlank(errorMsg)) {
            refreshDraftScript.insert(0, "alert(\"" + errorMsg + "\");");
            rendJavaScript(response, refreshDraftScript.toString());
            return null;
        }

        EdocRegister edocRegister = oldRegister;

        if (registerBody == null && edocRegister != null) {
            registerBody = edocRegisterManager.findRegisterBodyByRegisterId(edocRegister.getId());
        }

        User user = AppContext.getCurrentUser();
        long oldRegisterId = 0;
        long oldBodyId = 0;
        if (edocRegister == null) {
            edocRegister = new EdocRegister();
        } else {
            oldRegisterId = edocRegister.getId();// 保存老的ID，bind的时候会把ID给清掉
            oldBodyId = registerBody.getId();
        }
        /*******************/
        if (registerBody == null) {
            registerBody = new RegisterBody();
        }
        edocRegister.bind(request);
        registerBody.bind(request);

        // 修改签收单
        if (edocRecieveRecord == null) {
            edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(edocRegister.getRecieveId());
        }

        Long oid = edocRegister.getOrgAccountId();
        V3xOrgUnit unit = orgManager.getUnitById(oid);
        if (unit != null) {
            edocRegister.setOrgAccountId(unit.getOrgAccountId());
        }
        // 删除原有附件
        if (oldRegisterId != 0) {// 有原始ID，直接更新
            edocRegister.setId(oldRegisterId);
            registerBody.setId(oldBodyId);
            this.attachmentManager.deleteByReference(edocRegister.getId());
        } else {
            edocRegister.setNewId();
            registerBody.setNewId();
        }
        // 保存附件
        String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.edocRegister, edocRegister.getId(),
                edocRegister.getId(), request);
        if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
            edocRegister.setHasAttachments(true);
        }

        // 你已经不是当前事项的代理人
        // edocRegister.setRegisterUserId(edocRegister.getRegisterUserId()==-1?user.getId():edocRecieveRecord.getRegisterUserId());
        edocRegister.setRecTime(edocRecieveRecord == null ? null : edocRecieveRecord.getRecTime());

        Integer tempState = edocRegister.getState();
        if (tempState != null && tempState == EdocNavigationEnum.RegisterState.Registed.ordinal()) {// 状态为已登记
            edocRegister.setIsRetreat(EdocNavigationEnum.RegisterRetreatState.NotRetreat.ordinal());// 非退回
            edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());
        }

        String serialNo = edocRegister.getSerialNo();
        serialNo = this.registDocMark(edocRegister.getId(), serialNo, 3, 1, false,
                EdocEnum.MarkType.edocInMark.ordinal());
        if (serialNo != null) {
            edocRegister.setSerialNo(serialNo);
        }

        // GOV-4861.公文管理，收文登记节点，标准格式的正文，如果没有内容，查看正文时，显示NULL start
        if (registerBody != null && "HTML".equals(registerBody.getContentType())) {
            if (Strings.isBlank(registerBody.getContent())) {
                registerBody.setContent("");
            }
        }
        // 交换方式设置
        if (edocRecieveRecord != null) {
            edocRegister.setExchangeMode(edocRecieveRecord.getExchangeMode());
        } else {
            edocRegister.setExchangeMode(EdocExchangeModeEnum.internal.getKey());
        }
        // GOV-4861.公文管理，收文登记节点，标准格式的正文，如果没有内容，查看正文时，显示NULL end

        if ("create".equals(comm) && oldRegister == null) {

            registerBody.setEdocRegister(edocRegister);
            edocRegister.setRegisterBody(registerBody);
            edocRegister.setAutoRegister(0);

            // 电子公文 (为1)
            if (edocRegister.getRegisterType() != null
                    && edocRegister.getRegisterType() == EdocNavigationEnum.RegisterType.ByAutomatic.ordinal()) {
                if (edocRecieveRecord != null) {
                    edocRegister.setRecieveUserId(edocRecieveRecord.getRecUserId());
                    // 送文日期
                    edocRegister.setExchangeSendTime(edocRecieveRecord.getCreateTime());
                    String recUserName = "";
                    V3xOrgEntity member = orgManager.getEntity("Member", edocRecieveRecord.getRecUserId());
                    if (member != null) {
                        recUserName = member.getName();
                    }
                    edocRegister.setRecieveUserName(recUserName);
                    if (!"0".equals(registerState)) {
                        recieveEdocManager.registerRecieveEdoc(edocRegister.getRecieveId());
                    }
                }
            }
            edocRegisterManager.createEdocRegister(edocRegister);

        } else {
            registerBody.setEdocRegister(edocRegister);
            edocRegister.setRegisterBody(registerBody);
            edocRegisterManager.updateEdocRegister(edocRegister);
        }

        // 电子公文保存待发，修改affair的状态
        if ("0".equals(registerState) && edocRegister.getRegisterType() != null
                && edocRegister.getRegisterType() == EdocNavigationEnum.RegisterType.ByAutomatic.ordinal()) {
            recieveEdocManager.registerRecieveEdoc(edocRegister.getRecieveId(), 1);
        }

        if (edocRecieveRecord != null) {
            edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
            edocRecieveRecord.setRegisterTime(new java.sql.Timestamp(edocRegister.getRegisterDate().getTime()));
            edocRecieveRecord.setRegisterName(edocRegister.getRegisterUserName());
            if (edocRecieveRecord.getRegisterUserId() == 0) {
                edocRecieveRecord.setRegisterUserId(edocRegister.getRegisterUserId());
            }
            edocRecieveRecord.setIsRetreat(EdocRecieveRecord.Receive_Retreat_No);

            recieveEdocManager.update(edocRecieveRecord);
            if (edocRegister.getState() != null
                    && edocRegister.getState() == EdocNavigationEnum.RegisterState.Registed.ordinal()) {
                // 去掉待登记
                Map<String, Object> params = new HashMap<String, Object>();
                // wangjingjing begin
                params.put("app", ApplicationCategoryEnum.edocRegister.getKey());
                // wangjingjing end
                params.put("objectId", edocRecieveRecord.getEdocId());
                params.put("subObjectId", edocRecieveRecord.getId());
                List<CtpAffair> affairList = affairManager.getByConditions(null, params);
                if (affairList != null) {
                    for (int i = 0; i < affairList.size(); i++) {
                        // 代理人登记
                        if (!user.getId().equals(edocRegister.getRegisterUserId())) {
                            affairList.get(i).setTransactorId(user.getId());
                        }
                        affairList.get(i).setCompleteTime(new Timestamp(new Date().getTime()));
                        if ("0".equals(registerState)) {
                            affairList.get(i).setState(StateEnum.col_waitSend.getKey());
                        } else {
                            affairList.get(i).setState(StateEnum.edoc_exchange_registered.getKey());
                        }
                        affairManager.updateAffair(affairList.get(i));
                    }
                }
            }
        }
        appLogManager.insertLog(user, AppLogAction.Edoc_RegEdoc, user.getName(), edocRegister.getSubject());

        // 发送消息
        if (edocRegister.getState() != null
                && edocRegister.getState() == EdocNavigationEnum.RegisterState.Registed.ordinal()) {
            // -------------待分发列表没有显示word图标bug 修复 changyi update
            sendMessageToRegister(user, agentId, edocRegister, comm, oldDistributerId, registerBody.getContentType());
        }

        // 页面跳转
        StringBuffer sb = new StringBuffer();

        if ("agent".equals(request.getParameter("app")) && !user.getId().equals(edocRegister.getRegisterUserId())) {// 代理人跳转到代理事项
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("   window.returnValue = \"true\";");
            sb.append("   window.close();");
            sb.append("} else {");
            sb.append("	parent.parent.parent.location.href='main.do?method=morePending4App&app=agent';");
            sb.append("}");
        } else {
            if (edocRegister.getState() != null
                    && edocRegister.getState() == EdocNavigationEnum.RegisterState.DraftBox.ordinal()) {// 草稿
                sb.append(refreshDraftScript);
            } else {
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("   window.returnValue = \"true\";");
                sb.append("   window.close();");
                sb.append("} else {");
                sb.append(
                        "	parent.parent.window.location.href='edocController.do?method=listIndex&edocType=1&from=listRegister&listType=listRegister&recListType=registerDone';");
                sb.append("}");
            }
        }
        rendJavaScript(response, sb.toString());
        return null;
    }

    private String checkValid(long registUserId, EdocRegister oldreRegister, EdocRecieveRecord edocRecieveRecord)
            throws IOException {

        User user = AppContext.getCurrentUser();

        // 竞争执行判断，如果该公文已经被签收，给出提示
        boolean hasRegisted = false;
        boolean hasDelete = false;
        String errMsg = null;

        if (oldreRegister != null && oldreRegister.getState() != null) {// 已有登记数据

            if (oldreRegister.getState() == EdocNavigationEnum.RegisterState.deleted.ordinal()) {// 竞争执行看是否被删除了
                hasDelete = true;
            } else if (oldreRegister.getState() == EdocNavigationEnum.RegisterState.Registed.ordinal()) {

                // 公文登记数据已登记且未进行分发可以进行修改
                if (oldreRegister.getDistributeState() == EdocNavigationEnum.EdocDistributeState.Distributed
                        .ordinal()) {
                    hasRegisted = true;
                }
            } else if (oldreRegister.getState() == EdocNavigationEnum.RegisterState.DraftBox.ordinal()) {

                long oldRegisterUserId = oldreRegister.getRegisterUserId();
                if (oldRegisterUserId != 0) {// 非竞争执行
                    if (oldRegisterUserId == registUserId) {// 自己保存待发再次编辑

                        // 代理人处理的情况
                        // List<Long> ownerIds =
                        // MemberAgentBean.getInstance().getAgentToMemberId(ModuleType.edoc.getKey(),user.getId());
                        if (!user.getId().equals(registUserId)) {
                            hasRegisted = true;
                        }
                    } else {// 或者竞争执行第二个人进来
                        hasRegisted = true;
                    }
                }
            }
        }

        if (hasDelete) {
            errMsg = ResourceUtil.getString("edoc.alert.register.hasDelete");
        } else if (hasRegisted) {// 已经登记
            errMsg = ResourceUtil.getString("alert_has_registe");
        } else if (edocRecieveRecord != null) {

            // 验证是否已经被回退了
            if (edocRecieveRecord.getStatus() == EdocRecieveRecord.Exchange_iStatus_Torecieve
                    || edocRecieveRecord.getIsRetreat() == EdocRecieveRecord.Receive_Retreat_Yes) {

                errMsg = ResourceUtil.getString("alert_hasBeStepBack_already");

            } else if (edocRecieveRecord.getRegisterUserId() != 0
                    && registUserId != edocRecieveRecord.getRegisterUserId()) {

                // 如果当前用户与登记人的代理人不同，则表示公文登记人的代理人已转换
                // 已签收列表修改登记人
                if (edocRecieveRecord.getRegisterUserId() != 0
                        && registUserId != edocRecieveRecord.getRegisterUserId()) {
                    errMsg = ResourceUtil.getString("alert_hasChanged_register");
                }
            }
        }

        return errMsg;
    }

    // 登记后发送消息
    private void sendMessageToRegister(User user, long agentToId, EdocRegister edocRegister, String comm,
                                       long oldDistributerId, String... bodyType) throws MessageException {
        try {
            String key = "edoc.registered"; // 成功登记
            String cancel = "edoc.registered.distribute.cancel"; // 撤销
            String modify = "edoc.registered.modify"; // 撤销
            String userName = user.getName();
            long userId = user.getId();
            long registerId = edocRegister.getId();
            Long registerUserId = edocRegister.getRegisterUserId();
            Long distributerId = edocRegister.getDistributerId();
            String subject = edocRegister.getSubject();
            String url = "message.link.exchange.distribute";
            String agentApp = "agent";
            com.seeyon.ctp.common.usermessage.Constants.LinkOpenType linkOpenType = com.seeyon.ctp.common.usermessage.Constants.LinkOpenType.href;
            String agentToName = "";
            String registerName = "";
            V3xOrgMember member = orgManager.getMemberById(edocRegister.getRegisterUserId());
            if (member != null) {
                registerName = member.getName();
            }
            int importantLevel = Strings.isBlank(edocRegister.getUrgentLevel()) ? 0
                    : Integer.parseInt(edocRegister.getUrgentLevel());
            if ("modify".equals(comm)) {
                // 如果修改了分发人
                if (distributerId != oldDistributerId) {
                    // 给分发人发送修改消息
                    distributeAffair(subject, registerUserId, oldDistributerId, edocRegister.getId(),
                            edocRegister.getDistributeEdocId(), "delete", importantLevel);
                    sendRegisterMessage(cancel, subject, registerName, agentToName, userId, registerId,
                            oldDistributerId, "", "", null, linkOpenType, -1L, "");
                    // 给分发人代理发送修改消息
                    Long agentMemberId = MemberAgentBean.getInstance()
                            .getAgentMemberId(ApplicationCategoryEnum.edoc.key(), oldDistributerId);
                    if (agentMemberId != null) {
                        // distributeAffair(subject, registerUserId,
                        // agentMemberId, edocRegister.getId(),
                        // edocRegister.getDistributeEdocId(), "delete");
                        sendRegisterMessage(cancel, subject, registerName, agentToName, userId, registerId,
                                agentMemberId, "col.agent", "", null, linkOpenType, -1L, agentApp);
                    }
                    // 给分发人发消息 [登记人]登记了公文《》，请速进行分发处理！
                    distributeAffair(subject, registerUserId, distributerId, edocRegister.getId(),
                            edocRegister.getDistributeEdocId(), "create", importantLevel);
                    sendRegisterMessage(key, subject, registerName, agentToName, userId, registerId, distributerId, "",
                            "", url, linkOpenType, registerId, "");
                    // 给分发人的代理人发消息
                    agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                            distributerId);
                    if (agentMemberId != null) {
                        // distributeAffair(subject, registerUserId,
                        // agentMemberId, edocRegister.getId(),
                        // edocRegister.getDistributeEdocId(), "create");
                        sendRegisterMessage(key, subject, registerName, agentToName, userId, registerId, agentMemberId,
                                "col.agent", "", url, linkOpenType, registerId, agentApp);
                    }
                } else {
                    // 给分发人发送修改消息
                    distributeAffair(subject, registerUserId, oldDistributerId, edocRegister.getId(),
                            edocRegister.getDistributeEdocId(), "create", importantLevel);
                    sendRegisterMessage(modify, subject, registerName, agentToName, userId, registerId, distributerId,
                            "", "", url, linkOpenType, registerId, "");
                    // 给分发人代理发送修改消息
                    Long agentMemberId = MemberAgentBean.getInstance()
                            .getAgentMemberId(ApplicationCategoryEnum.edoc.key(), distributerId);
                    if (agentMemberId != null) {
                        // distributeAffair(subject, registerUserId,
                        // agentMemberId, edocRegister.getId(),
                        // edocRegister.getDistributeEdocId(), "create");
                        sendRegisterMessage(modify, subject, registerName, agentToName, userId, registerId,
                                agentMemberId, "col.agent", "", url, linkOpenType, registerId, agentApp);
                    }
                }
            } else {
                // 代理人登记
                if (agentToId != -1) {
                    agentToName = userName;
                    // 给分发人发消息 [登记人]成功登记公文《》，请速进行分发处理！(由代理人[]代为处理)
                    distributeAffair(subject, registerUserId, distributerId, edocRegister.getId(),
                            edocRegister.getDistributeEdocId(), "create", importantLevel, bodyType);
                    sendRegisterMessage(key, subject, registerName, agentToName, agentToId, registerId, distributerId,
                            "", "edoc.agent.deal", url, linkOpenType, registerId, "");
                    // 给分发人代理人发消息 [登记人]成功登记公文《》，请速进行分发处理！(由代理人[]代为处理)(代理)
                    Long agentMemberId = MemberAgentBean.getInstance()
                            .getAgentMemberId(ApplicationCategoryEnum.edoc.key(), distributerId);
                    if (agentMemberId != null) {
                        // distributeAffair(subject, registerUserId,
                        // agentMemberId, edocRegister.getId(),
                        // edocRegister.getDistributeEdocId(), "create");
                        sendRegisterMessage(key, subject, registerName, agentToName, agentToId, registerId,
                                agentMemberId, "col.agent", "edoc.agent.deal", url, linkOpenType, registerId, agentApp);
                    }
                    url = "message.link.exchange.registered";
                    // 给登记被代理人发消息 [登记人]登记了公文《》
                    MessageContent msgContent = new MessageContent("exchange.edoc.register", registerName, subject);
                    msgContent.add("edoc.agent.deal", user.getName());
                    msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
                    MessageReceiver receiver = new MessageReceiver(registerId, edocRegister.getRegisterUserId(), url,
                            registerId + "");
                    userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc, agentToId, receiver,
                            EdocMessageFilterParamEnum.recQita.key);
                    Long agentRegisterMemberId = MemberAgentBean.getInstance()
                            .getAgentMemberId(ApplicationCategoryEnum.edoc.key(), edocRegister.getRegisterUserId());
                    if (agentRegisterMemberId != null) {
                        // sendRegisterMessage(register , subject, registerName,
                        // agentToName, agentToId, registerId,
                        // agentRegisterMemberId, "col.agent",
                        // "edoc.agent.deal", url, linkOpenType, registerId,
                        // agentApp,
                        // "com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
                        msgContent = new MessageContent("exchange.edoc.register", registerName, subject);
                        msgContent.add("col.agent");
                        msgContent.add("edoc.agent.deal", user.getName());
                        msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
                        receiver = new MessageReceiver(registerId, agentRegisterMemberId, url, registerId + "");
                        userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.edoc,
                                agentRegisterMemberId, receiver, EdocMessageFilterParamEnum.recQita.key);
                    }

                } else {// 非代理人登记
                    // 给分发人发消息 [登记人]登记了公文《》，请速进行分发处理！
                    distributeAffair(subject, registerUserId, distributerId, edocRegister.getId(),
                            edocRegister.getDistributeEdocId(), "create", importantLevel, bodyType);
                    sendRegisterMessage(key, subject, registerName, agentToName, userId, registerId, distributerId, "",
                            "", url, linkOpenType, registerId, "");
                    // 给分发人的代理人发消息
                    Long agentMemberId = MemberAgentBean.getInstance()
                            .getAgentMemberId(ApplicationCategoryEnum.edoc.key(), edocRegister.getDistributerId());
                    if (agentMemberId != null) {
                        // distributeAffair(subject, registerUserId,
                        // agentMemberId, edocRegister.getId(),
                        // edocRegister.getDistributeEdocId(), "create");
                        sendRegisterMessage(key, subject, registerName, agentToName, userId, registerId, agentMemberId,
                                "col.agent", "", url, linkOpenType, registerId, agentApp);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private void distributeAffair(String subject, long senderId, long memberId, Long objectId, Long subObjectId,
                                  String comm, int importantLevel, String... bodyType) throws BusinessException {
        CtpAffair reAffair = null;
        if ("delete".equals(comm)) {
            affairManager.deleteByObjectId(ApplicationCategoryEnum.edocRegister, objectId);
        } else if ("create".equals(comm)) {// 这里注意，登记ojbectId, subState
            // 分发：subObjectId, state
            List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edocRecDistribute, objectId);
            if (Strings.isNotEmpty(affairs)) {
                reAffair = affairs.get(0);
            } else {
                reAffair = new CtpAffair();
                reAffair.setIdIfNew();
            }

            reAffair.setSubject(subject);
            reAffair.setMemberId(memberId);
            reAffair.setSenderId(senderId);
            reAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
            reAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
            reAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
            reAffair.setObjectId(objectId);// 登记的id
            reAffair.setSubObjectId(subObjectId);// 分发的id
            // wangjingjing begin 发文分发 等于 交换中心的 待发送，所以这里的分发一定是 收文分发
            reAffair.setApp(ApplicationCategoryEnum.edocRecDistribute.getKey());
            // reAffair.setApp(31);
            // wangjingjing end
            reAffair.setState(3);// 分发的状态
            reAffair.setSubState(SubStateEnum.col_normal.getKey());
            // -------------待分发列表没有显示word图标bug 修复 changyi add
            if (bodyType != null && bodyType.length == 1)
                reAffair.setBodyType(bodyType[0]);
            reAffair.setImportantLevel(importantLevel);
            if (Strings.isNotEmpty(affairs)) {
                affairManager.updateAffair(reAffair);
            } else {
                affairManager.save(reAffair);
            }
        }
    }

    /**
     * @param key        消息内容
     * @param subject    消息内容参数1：主题
     * @param userName   消息内容参数2：登记人
     * @param agentName  消息代登记参数1：代理登记人
     * @param fromUserId 由谁发起
     * @param registerId 登记id
     * @param toUserId   发给谁
     * @param colAgent   (代理)接收者是否为代理
     * @param agentDeal  (由..处理) 代理处理消息
     * @param url        链接url
     * @param linkType   打开消息类型
     * @param param      消息内容 参数
     * @param agentApp   代理参数标识 发给代理人：app=agent
     * @throws Exception
     */
    private void sendRegisterMessage(String key, String subject, String userName, String agentName, long fromUserId,
                                     long registerId, long toUserId, String colAgent, String agentDeal, String url,
                                     com.seeyon.ctp.common.usermessage.Constants.LinkOpenType linkType, long param, String agentApp)
            throws Exception {
        MessageReceiver receiver = new MessageReceiver(registerId, toUserId, url, linkType,
                EdocEnum.edocType.recEdoc.ordinal(), String.valueOf(registerId), agentApp);
        MessageContent messageContent = new MessageContent(key, subject, userName);
        messageContent.add(colAgent);
        messageContent.add(agentDeal, agentName);
        userMessageManager.sendSystemMessage(messageContent, ApplicationCategoryEnum.edocRecDistribute, fromUserId,
                receiver, EdocMessageFilterParamEnum.recQita.key);
    }

    /**
     * 保存登记单 -唐桂林2011-10-12
     */
    public ModelAndView deleteRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String listType = request.getParameter("listType");
        String edocType = request.getParameter("edocType");
        String[] ids = request.getParameterValues("registerId");
        String[] registerTypes = request.getParameterValues("registerType");
        User user = AppContext.getCurrentUser();
        if (registerTypes != null) {
            RegisterBody registerBody = null;
            List<EdocRegister> registerList = edocRegisterManager.findList(ids);
            if (registerList != null) {
                for (EdocRegister edocRegister : registerList) {
                    if (edocRegister.getRegisterType() == 2 && edocRegister.getState() == 0) {// 草稿箱中的手工登记则全删
                        attachmentManager.deleteByReference(edocRegister.getId());
                        registerBody = edocRegisterManager.findRegisterBodyByRegisterId(edocRegister.getId());
                        if (registerBody != null) {
                            registerBody.setEdocRegister(edocRegister);
                            edocRegister.setRegisterBody(registerBody);
                        }
                        edocRegisterManager.deleteEdocRegister(edocRegister);
                    } else {// 自动登记，已登记的纸质登记 物理删除
                        edocRegisterManager.updateEdocRegisterState(edocRegister.getId(),
                                EdocNavigationEnum.RegisterState.deleted.ordinal());
                    }
                    appLogManager.insertLog(user, AppLogAction.Edoc_RegEdoc_Delete, user.getName(),
                            edocRegister.getSubject());
                }
            }
        }
        return super.redirectModelAndView("/edocController.do?method=listRegister&listType=" + listType
                + "&recListType=" + listType + "&from=listRegister&edocType=" + edocType);
    }

    /**
     * 保存登记单 -唐桂林2011-10-12
     */
    public ModelAndView cancelRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String listType = request.getParameter("listType");
        String edocType = request.getParameter("edocType");
        String[] ids = request.getParameterValues("registerId");
        String[] registerTypes = request.getParameterValues("registerType");
        if (registerTypes != null) {
            List<EdocRegister> registerList = edocRegisterManager.findList(ids);
            if (registerList != null) {
                List<EdocRegister> list = new ArrayList<EdocRegister>();
                for (EdocRegister edocRegister : registerList) {
                    if (edocRegister.getRegisterType() != 1 && edocRegister.getRecieveId() == -1) {// 纸质/二维码登记公文允许撤销
                        edocRegister.setState(EdocNavigationEnum.RegisterState.DraftBox.ordinal());
                        edocRegister.setIsRetreat(0);
                        list.add(edocRegister);

                        Map<String, Object> columns = new HashMap<String, Object>();
                        columns.put("isDelete", true);
                        Object[][] wheres = new Object[3][2];
                        wheres[0] = new Object[]{"app", ApplicationCategoryEnum.edocRegister.key()};
                        wheres[1] = new Object[]{"objectId", edocRegister.getId()};
                        wheres[2] = new Object[]{"state", StateEnum.col_pending.getKey()};
                        affairManager.update(columns, wheres);
                    }
                }
                edocRegisterManager.updateEdocRegister(list);
            }
        }
        return super.redirectModelAndView("/edocController.do?method=listRegister&listType=" + listType
                + "&from=listRegister&edocType=" + edocType);
    }

    /**
     * 意见回复
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView doComment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long summaryId = Long.valueOf(request.getParameter("summaryId"));
        EdocSummary edocSummary = edocManager.getEdocSummaryById(summaryId, false);

        // 是否发送消息
        boolean isSendMessage = request.getParameterValues("isSendMessage") != null;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // 发起人增加附言

        EdocOpinion senderOpinion = new EdocOpinion();
        senderOpinion.setIdIfNew();

        senderOpinion.setCreateTime(now);
        senderOpinion.setContent(request.getParameter("postscriptContent"));
        senderOpinion.setCreateUserId(user.getId());
        senderOpinion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
        senderOpinion.setEdocSummary(edocSummary);
        senderOpinion.setIsHidden(false);

        this.edocManager.saveOpinion(senderOpinion, isSendMessage);

        this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, senderOpinion.getId(), request);

        super.rendJavaScript(response, "parent.replyCommentOK('" + Datetimes.formateToLocaleDatetime(now) + "')");
        return null;
    }

    public ModelAndView waitSendPreSend(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("collaboration/processModeSelectorMain");
        String _summaryId = request.getParameter("summaryId");
        Long summaryId = Long.parseLong(_summaryId);
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
        String processId = summary.getProcessId();
        if (processId == null || "".equals(processId)) {
            response.getWriter().println("err:noflow");
            return null;
        }

        Long templateId = summary.getTempleteId();

        String _caseId = request.getParameter("caseId");
        if (Strings.isNotBlank(_caseId)) {
        }

        // 分枝 开始
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("currentIsStart", "true");
        if (hash.size() > 0) {
            Set<Map.Entry<String, String>> entry = hash.entrySet();
            List<String> keys = new ArrayList<String>();
            List<String> nodeNames = new ArrayList<String>();
            List<String> conditions = new ArrayList<String>();
            List<String> forces = new ArrayList<String>();
            List<String> links = new ArrayList<String>();
            //List<Integer> conditionTypes = new ArrayList<Integer>();
            String[] temp = null;
            String[] temp1 = null;
            String order = hash.get("order");
            if (order != null && order.indexOf("$") != -1) {
                temp1 = StringUtils.split(order, "$");
            }
            User user = AppContext.getCurrentUser();
            StringBuilder sb = new StringBuilder();
            if (temp1 != null && temp1.length > 0) {
                for (String item : temp1) {
                    String value = hash.get(item);
                    if (value != null && value.indexOf("↗") != -1) {
                        sb.append(item + ":");
                        keys.add(item);
                        links.add(hash.get("linkTo" + item));
                        temp = value.split("↗");
                        if (temp != null) {
                            nodeNames.add(temp[0]);
                            temp[1] = temp[1].replaceAll("Department", String.valueOf(user.getDepartmentId()))
                                    .replaceAll("Post", String.valueOf(user.getPostId()))
                                    .replaceAll("Level", String.valueOf(user.getLevelId()))
                                    .replaceAll("Account", String.valueOf(user.getAccountId()))
                                    .replaceAll("'", "\\\\\'").replaceAll("&#91;", "").replaceAll("&#93;", "");
                            if (temp[1].indexOf("handCondition") != -1) {
                                temp[1] = temp[1].replaceAll("handCondition", "false");
                                //conditionTypes.add(2);
                            } else
                                //conditionTypes.add(0);
                                conditions.add(temp[1]);
                            if (temp.length == 3 && "1".equals(temp[2]))
                                forces.add("true");
                            else
                                forces.add("false");
                        }
                    }
                }
            } else {
                for (Map.Entry<String, String> item : entry) {
                    if (item.getValue() != null && item.getValue().indexOf("↗") != -1) {
                        sb.append(item.getKey() + ":");
                        keys.add(item.getKey());
                        links.add(hash.get("linkTo" + item.getKey()));
                        temp = item.getValue().split("↗");
                        if (temp != null) {
                            nodeNames.add(temp[0]);
                            temp[1] = temp[1].replaceAll("Department", String.valueOf(user.getDepartmentId()))
                                    .replaceAll("Post", String.valueOf(user.getPostId()))
                                    .replaceAll("Level", String.valueOf(user.getLevelId()))
                                    .replaceAll("Account", String.valueOf(user.getAccountId()))
                                    .replaceAll("'", "\\\\\'").replaceAll("&#91;", "").replaceAll("&#93;", "");
                            if (temp[1].indexOf("handCondition") != -1) {
                                temp[1] = temp[1].replaceAll("handCondition", "false");
                                //conditionTypes.add(2);
                            } else
                                //conditionTypes.add(0);
                                conditions.add(temp[1]);
                            if (temp.length == 3)
                                forces.add("true");
                            else
                                forces.add("false");
                        }
                    }
                }
            }
            if (keys.size() > 0 && conditions.size() > 0) {
                mav.addObject("allNodes", sb.toString());
                mav.addObject("keys", keys);
                mav.addObject("names", nodeNames);
                mav.addObject("conditions", conditions);
                mav.addObject("nodeCount", hash.get("nodeCount"));
                mav.addObject("forces", forces);
                mav.addObject("links", links);
                mav.addObject("templateId", templateId);
            }
        }
        // 分枝 结束

        // mav.addObject("processModeSelector",selector);

        return mav;
    }

    private String newRegistDocMark(Long summaryId, String markStr, int markNum, int edocType, boolean checkId,
                                    int markType, String from) throws EdocMarkHistoryExistException {
        if (Strings.isNotBlank(markStr)) {
            markStr = markStr.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
        }

        EdocMarkModel em = EdocMarkModel.parse(markStr);
        if (em != null) {
            Integer t = em.getDocMarkCreateMode();// 0:未选择文号，1：下拉选择的文号，2：选择的断号，3.手工输入
            // 4.预留文号
            String _edocMark = em.getMark(); // 需要保存到数据库中的公文文号
            Long markDefinitionId = em.getMarkDefinitionId();
            Long edocMarkId = em.getMarkId();
            User user = AppContext.getCurrentUser();
            if (markType == EdocEnum.MarkType.edocMark.ordinal()) {// 公文文号
                if (t != 0) {// 等于0的时候没有进行公文文号修改
                    edocMarkManager.disconnectionEdocSummary(summaryId, markNum);
                }
                if (edocType != com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                    if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) { // 选择了一个新的公文文号
                        Integer currentNo = em.getCurrentNo();
                        edocMarkManager.createMark(markDefinitionId, currentNo, _edocMark, summaryId, markNum);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) { // 选择了一个断号
                        edocMarkManager.createMarkByChooseNo(edocMarkId, summaryId, markNum);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_RESERVE) { // 选择了一个预留文号
                        edocMarkManager.createMarkByChooseReserveNo(edocMarkId, summaryId, em.getCurrentNo(), markNum);
                    } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) { // 手工输入一个公文文号
                        edocMarkManager.createMark(_edocMark, summaryId, markNum);
                    }
                } else {// 签报处理
                    if ("save".equals(from)) {//保存待发
                        if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) {
                            Integer currentNo = em.getCurrentNo();
                            edocMarkManager.createMark(markDefinitionId, currentNo, _edocMark, summaryId, markNum);
                        } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) {
                            edocMarkManager.createMarkByChooseNo(edocMarkId, summaryId, markNum);
                        } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) {
                            edocMarkManager.createMark(_edocMark, summaryId, markNum);
                        }
                    } else {
                        if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) {
                            Integer currentNo = em.getCurrentNo();
                            this.edocMarkHistoryManager.save(summaryId, currentNo, _edocMark, markDefinitionId, markNum, user.getId(), user.getId(), checkId, true);
                        } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_OLD) {
                            this.edocMarkHistoryManager.saveMarkHistorySelectOld(edocMarkId, _edocMark, summaryId, user.getId(), checkId);
                        } else if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_INPUT) {
                            this.edocMarkHistoryManager.save(summaryId, _edocMark, markDefinitionId, markNum, user.getId(), user.getId(), checkId, false);
                        }
                    }
                }
            } else if (markType == EdocEnum.MarkType.edocInMark.ordinal()) {// 内部文号
                if (t == com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW) {
                    this.edocMarkDefinitionManager.setEdocMarkCategoryIncrement(markDefinitionId);
                }
            }
            return _edocMark;
        }
        return null;
    }

    /**
     * 登记使用的文号,返回真正的文号串
     *
     * @param markStr:掩码格式文号，详细见EdocMarkModel.parse()方法
     * @param markNum
     */
    private String registDocMark(Long summaryId, String markStr, int markNum, int edocType, boolean checkId,
                                 int markType) throws EdocMarkHistoryExistException {
        return newRegistDocMark(summaryId, markStr, markNum, edocType, checkId, markType, "");
    }

    private String createSendRecEdocErrMsg(String msg, String app) {
        String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", msg);
        StringBuffer sb = new StringBuffer();
        sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(errMsg) + "\");");
        sb.append("if(window.dialogArguments){"); // 弹出
        sb.append("  window.returnValue = \"true\";");
        sb.append("  window.close();");
        sb.append("}else{");
        if ("agent".equals(app)) {
            sb.append("	parent.parent.location.href='main.do?method=morePending4App&app=agent';");
        } else {
            sb.append(
                    "	parent.parent.location.href='edocController.do?method=listIndex&from=listPending&listType=listPending&edocType=1';");
        }
        sb.append("}");
        sb.append("");
        return sb.toString();
    }

    private String canSendRecEdoc(long accountId, long userId, int roleType, String app) throws Exception {
        String errMsg = "";
        if (!EdocRoleHelper.isEdocCreateRole(accountId, userId, roleType)) {
            String msg = "alert_not_edocregister";
            if (EdocHelper.isG6Version()) {
                msg = "edoc.distributeprivileges.label";
            }
            errMsg = createSendRecEdocErrMsg(msg, app);
        }
        return errMsg;
    }

    /**
     * ----------------------收文发起权限校验-----------------------------
     * <p>
     * a: A8纸质登记，G6纸质收文发送时，只需要判断登录人是否有收文发起权
     * <p>
     * b: 如果是电子的，那么收文发起人是指定了的 那么如果当前登录人就是指定人，只需要判断登录人是否有收文发起权，如果没有就不能发送
     * 如果当前登录人不是指定人，是指定人的代理人时，也需要判断指定人是否有发起权
     */
    private Map<String, Object> checkSendRecEdoc(EdocRegister edocRegister, long registerId, String recieveIdStr,
                                                 String app, User user, String backBoxToEdit) throws Exception {
        int roleType = EdocEnum.edocType.recEdoc.ordinal();
        if (EdocHelper.isG6Version()) {
            roleType = EdocEnum.edocType.distributeEdoc.ordinal();
        }

        Map<String, Object> msgMap = new HashMap<String, Object>();
        msgMap.put("errMsg", "");

        // A8纸质登记，G6纸质收文
        if ((registerId == -1 && Strings.isBlank(recieveIdStr)) || (edocRegister != null
                && edocRegister.getRegisterType() == EdocRegister.REGISTER_TYPE_BY_PAPER_REC_EDOC)) {
            String errMsg = canSendRecEdoc(user.getLoginAccount(), user.getId(), roleType, app);
            msgMap.put("errMsg", errMsg);
            return msgMap;
        }
        // 电子收文
        else {
            if (edocRegister != null) {
                Long recordRegisterUserId = edocRegister.getDistributerId();

                boolean isNotAgent = false;
                Long agentToId = null;
                // 获得分发人的代理人
                Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                        recordRegisterUserId);
                if (agentId != null) {
                    // 如果不是代理人登录，则isNotAgent=true
                    if (!Long.valueOf(user.getId()).equals(agentId)) {
                        isNotAgent = true;
                    }
                }
                // GOV-5117 公文分发人在登记处做修改，原来的登记人还是可以在待分发列表处分发这条公文，处理后出现代理的消息提示
                // 如果代理人为空，那么就没有设代理
                else {
                    isNotAgent = true;
                }

                if (recordRegisterUserId.longValue() != user.getId()) {
                    agentToId = edocRegister.getDistributerId();
                    msgMap.put("agentToId", agentToId);
                }
                if (recordRegisterUserId.longValue() != user.getId() && isNotAgent) {
                    // 公文登记人已经转换
                    String msg = "alert_hasChanged_register";
                    if (EdocHelper.isG6Version()) {
                        msg = "alert_hasChanged_distribute";
                    }
                    msg = createSendRecEdocErrMsg(msg, app);
                    msgMap.put("errMsg", msg);
                    return msgMap;
                }

                // 登记公文，判断指定的登记人是否可以登记此公文
                String errorMsg = canSendRecEdoc(edocRegister.getOrgAccountId(), recordRegisterUserId, roleType, app);
                if (Strings.isNotBlank(errorMsg)) {
                    msgMap.put("errMsg", errorMsg);
                    return msgMap;
                }

                if (edocRegister.getIsRetreat() == 1) {
                    // 公文已经回退
                    String sb = createSendRecEdocErrMsg("alert_hasBeStepBack_already", app);
                    msgMap.put("errMsg", sb);
                    return msgMap;
                } else if (edocRegister.getDistributeState() == EdocNavigationEnum.EdocDistributeState.Distributed.ordinal()
                        && !"true".equals(backBoxToEdit)) {// 公文已经分发
                    msgMap.put("errMsg", "redirect");
                    return msgMap;
                }
            }
        }
        return msgMap;
    }

    /**
     * 发送公文,跳转到已�发
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView send(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String processTypeStr = request.getParameter("processType");

        String workflowNodePeoplesInput = request.getParameter("workflow_node_peoples_input");
        String workflowNodeConditionInput = request.getParameter("workflow_node_condition_input");
        Long processType = 0L;
        if (processTypeStr != null && !"".equals(processTypeStr)) {
            processType = Long.parseLong(processTypeStr);
        }
        /**
         * 表单ID
         */
        String etable = request.getParameter("edoctable");
        long formId = 0;
        if (Strings.isNotBlank(etable)) {
            formId = Long.parseLong(etable);
        }
        // 检查公文单是否已经被删除。“当前公文单不存在，可能已经被删除，请检查。”
        boolean isExsit = edocFormManager.isExsit(formId);
        if (!isExsit) {
            StringBuffer sb = new StringBuffer();
            String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                    "alert_edocform_isnotexsit");
            sb.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
            sb.append("history.back();");
            rendJavaScript(response, sb.toString());
            return null;
        }

        // 取得公文发送人的信息
        User user = AppContext.getCurrentUser();
        String comm = request.getParameter("comm");
        // 来文登记,更新登记时间，给签收人发送消息
        long registerId = Strings.isBlank(request.getParameter("registerId")) ? -1
                : Long.parseLong(request.getParameter("registerId"));

        Long agentToId = null;
        EdocRegister edocRegister = null;
        String backBoxToEdit = request.getParameter("backBoxToEdit");
        String recieveIdStr = request.getParameter("recieveId");
        String edocType = request.getParameter("edocType");

        EdocSummary edocSummary = new EdocSummary();
        edocSummary.setIdIfNew();
        bind(request, edocSummary);

        if ("1".equals(edocType)) {
            if (registerId != -1) {
                edocRegister = edocRegisterManager.findRegisterById(registerId);
            } else if (Strings.isNotBlank(recieveIdStr)) {
                edocRegister = edocRegisterManager.findRegisterByRecieveId(Long.parseLong(recieveIdStr));
            }

            if (edocRegister != null) {
                edocSummary.setSendTo(edocRegister.getSendTo());
            }

            Map<String, Object> msgMap = checkSendRecEdoc(edocRegister, registerId, recieveIdStr,
                    request.getParameter("app"), user, backBoxToEdit);
            String errMessage = (String) msgMap.get("errMsg");
            if (Strings.isNotBlank(errMessage)) {
                if ("redirect".equals(errMessage)) {
                    ModelAndView modelAndView = new ModelAndView("common/redirect");
                    String errMsg = ResourceBundleUtil
                            .getString("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource", "edoc.distributed");
                    modelAndView.addObject("redirectURL", BaseController.REDIRECT_BACK);
                    modelAndView.addObject("errMsg", errMsg);
                    return modelAndView;
                } else {
                    rendJavaScript(response, errMessage);
                    return null;
                }
            }
            if (msgMap.containsKey("agentToId")) {
                agentToId = (Long) msgMap.get("agentToId");
            }
        }

        // 因为DataUtil中requestToSummary方法很多地方都在调用，公文发送性能优化时在requestToSummary内部将获取公文单的公文元素保存进了ThreadLocal中
        // 需要在这里向ThreadLocal中设置一个开关，当该方法调用requestToSummary时才将公文元素保存进了ThreadLocal中
        SharedWithThreadLocal.setCanUse();
        DataUtil.requestToSummary(request, edocSummary, formId);

        Long lockUserId = edocLockManager.canGetLock(edocSummary.getSubject(), user.getId());
        if (lockUserId != null) {
            LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,summaryId" + edocSummary.getId() + "subject:" + edocSummary.getSubject() + " userId:" + user.getId());
            return null;
        }

        String lockDocMarkSubject = "";
        String lockDocMark = "";
        try {
            // ***快速发文相关变量 start***
            boolean isQuickSend = false; // 快速发文标识
            isQuickSend = edocSummary.getIsQuickSend() == null ? false : edocSummary.getIsQuickSend();
            // 交换类型
            int edocExchangeType = request.getParameter("edocExchangeType") == null ? -1
                    : Integer.parseInt(request.getParameter("edocExchangeType"));
            String edocMangerID = request.getParameter("memberList");
            String quickSendPigholeInfo = ""; // 快速发文归档成功后的提示
            // ***快速发文相关变量 end***

            String deadlineDatetime = (String) request.getParameter("deadLineDateTime");
            if (!isQuickSend && Strings.isNotBlank(deadlineDatetime)) {
                edocSummary.setDeadlineDatetime(DateUtil.parse(deadlineDatetime, "yyyy-MM-dd HH:mm"));
            }

            // 新建公文页面流程期限这里加一个隐藏域，后台保存的是这里的值，因为如果从模板加载设了流程期限的话，就disabled了，后台就取不到值了
            String deadline2 = request.getParameter("deadline2");

            // OA-20265 调用格式模板，发送后报错。
            if (!isQuickSend && Strings.isNotBlank(deadline2)) {
                edocSummary.setDeadline(Long.parseLong(deadline2));
            }
            String advanceRemind2 = request.getParameter("advanceRemind2");
            if (!isQuickSend && Strings.isNotBlank(advanceRemind2)) {
                edocSummary.setAdvanceRemind(Long.parseLong(advanceRemind2));
            }

            // 设置公文类型,党务还是政务的
            String edocGovType = request.getParameter("edocGovType");
            if ("party".equals(edocGovType)) {
                String party = request.getParameter("my:party");
                edocSummary.setParty(party);
            } else if ("administrative".equals(edocGovType)) {
                String administrative = request.getParameter("my:administrative");
                edocSummary.setAdministrative(administrative);
            }

            /***** puyc**区分 分发和拟文 分发，收文的summaryId *****/
            String recSummaryIdStr = request.getParameter("recSummaryIdVal");
            if (Strings.isNotBlank(recSummaryIdStr) && !"-1".equals(recSummaryIdStr)) {
                List<EdocSummaryRelation> list = this.edocSummaryRelationManager
                        .findEdocSummaryRelation(Long.parseLong(recSummaryIdStr));
                if (list != null) {
                    this.edocSummaryRelationManager.updateEdocSummaryRelation(list, Long.parseLong(recSummaryIdStr),
                            edocSummary.getId());
                }
            }
            /***** puyc**区分 分发和拟文 分发，收文的summaryId end *****/

            // OA-40064一个没有发文单位公文元素的公文进行公文交换后，test02在发文登记簿中按发文单位统计，列表中显示为空，但是发送单和签收单都显示了发文单位
            // 当发文单没有 发文单位 公文元素时，也需要summary中设置发文单位
            // OA-68915 签报拟文时文单上没有发文部门元素，发送后没有保存发文部门
            if (Strings.isBlank(request.getParameter("my:send_unit"))) {
                edocSummary = setEdocDefaultSendInfo(edocSummary, user, "0");
            }
            if (Strings.isBlank(request.getParameter("my:send_department"))) {
                edocSummary = setEdocDefaultSendInfo(edocSummary, user, "1");
            }

            // OA-32875 系统管理员-枚举管理，公文类型、行文类型枚举引用之后显示为否（注意查看一下OA-29865中的修改方法）
            // 更新系统枚举引用
            EdocHelper.updateEnumItemRef(request);

            // OA-17655 拟文时设置了跟踪，发送后被回退，在待发中编辑，进入到拟文页面，跟踪被取消了。应该保留原来的设置。
            // 设置summary中的跟踪，因为DataUtil.requestToSummary方法中取跟踪值有点问题，但该方法在处理回退时也被调用了，修改害怕引起其他问题
            String isTrack = request.getParameter("isTrack");
            if (Strings.isNotBlank(isTrack)) {
                edocSummary.setCanTrack(Integer.parseInt(isTrack));
            }

            String templeteId = request.getParameter("templeteId");
            if (Strings.isNotBlank(templeteId)) {
                CtpTemplate _curTemplate = templeteManager.getCtpTemplate(Long.parseLong(templeteId));
                if (_curTemplate.getFormParentid() != null && !_curTemplate.isSystem()) {
                    edocSummary.setTempleteId(_curTemplate.getFormParentid());
                } else {
                    edocSummary.setTempleteId(Long.parseLong(templeteId));
                }
                if (null != _curTemplate && !_curTemplate.isSystem() && null == _curTemplate.getFormParentid()) {
                    templeteManager.updateTempleteHistory(user.getId(), _curTemplate.getId());
                    edocSummary.setTempleteId(null);
                }
            }

            // 将公文流水号（内部文号）自动增1
            // add by handy,2007-10-16
            // if("new_form".equals(comm))
            // {
            // edocInnerMarkDefinitionManager.getInnerMark(edocSummary.getEdocType(),
            // user.getAccountId(), true);
            // }
            // edocSummary.setSerialNo(serialNo);

            List<Long> markDefinitionIdList = new ArrayList<Long>();
            // 第一个公文文号
            EdocMarkModel em = EdocMarkModel.parse(edocSummary.getDocMark());
            if (em != null) {
                markDefinitionIdList.add(em.getMarkDefinitionId());
            }
            // 第二个公文文号
            em = EdocMarkModel.parse(edocSummary.getDocMark2());
            if (em != null) {
                markDefinitionIdList.add(em.getMarkDefinitionId());
            }
            // 内部文号
            em = EdocMarkModel.parse(edocSummary.getSerialNo());
            if (em != null) {
                markDefinitionIdList.add(em.getMarkDefinitionId());
            }
            if (Strings.isNotEmpty(markDefinitionIdList)) {

                List<EdocMarkDefinition> markDefinitions = edocMarkDefinitionManager
                        .queryMarkDefinitionListById(markDefinitionIdList);

                // ----------性能优化，存入SharedWithThreadLocal
                SharedWithThreadLocal.setMarkDefinition(markDefinitions);
            }

            // 处理公文文号
            // 如果公文文号为空，不做任何处理
            String docMark = edocSummary.getDocMark();
            if (Strings.isNotBlank(edocSummary.getDocMark()) && edocSummary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                lockDocMarkSubject = edocMarkLockManager.canGetLock(edocSummary.getDocMark(), edocSummary.getSubject());
                lockDocMark = edocSummary.getDocMark();
                if (Strings.isNotBlank(lockDocMarkSubject)) {
                    EdocMarkModel emark = EdocMarkModel.parse(docMark);
                    String errMsg = "公文《" + lockDocMarkSubject + "》占用了文号《" + emark.getMark() + "》,请刷新重试";
                    StringBuffer str = new StringBuffer("");
                    str.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
                    str.append("history.back();");
                    rendJavaScript(response, str.toString());
                    return null;
                }
            }
            if (Strings.isNotBlank(edocSummary.getSerialNo()) && edocSummary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                lockDocMark = edocSummary.getSerialNo();
                lockDocMarkSubject = edocMarkLockManager.canGetLock(edocSummary.getSerialNo(), edocSummary.getSubject());
                if (Strings.isNotBlank(lockDocMarkSubject)) {
                    EdocMarkModel serialNo = EdocMarkModel.parse(edocSummary.getSerialNo());
                    String errMsg = "公文《" + lockDocMarkSubject + "》占用了文号《" + serialNo.getMark() + "》,请刷新重试";
                    StringBuffer str = new StringBuffer("");
                    str.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
                    str.append("history.back();");
                    rendJavaScript(response, str.toString());
                    return null;
                }
            }
            if (Strings.isNotBlank(edocSummary.getDocMark2()) && edocSummary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                lockDocMarkSubject = edocMarkLockManager.canGetLock(edocSummary.getDocMark2(), edocSummary.getSubject());
                lockDocMark = edocSummary.getDocMark2();
                if (Strings.isNotBlank(lockDocMarkSubject)) {
                    EdocMarkModel mark2 = EdocMarkModel.parse(edocSummary.getDocMark2());
                    String errMsg = "公文《" + lockDocMarkSubject + "》占用了文号《" + mark2.getMark() + "》,请刷新重试";
                    StringBuffer str = new StringBuffer("");
                    str.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
                    str.append("history.back();");
                    rendJavaScript(response, str.toString());
                    return null;
                }
            }
            try {
                docMark = this.registDocMark(edocSummary.getId(), docMark, 1, edocSummary.getEdocType(), false,
                        EdocEnum.MarkType.edocMark.ordinal());
            } catch (EdocMarkHistoryExistException e) {
                // 签报提交时如果文号存在
                // OA-47875签报拟文时使用发文封发完的公文文号，发送报错
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        e.getMessage());
                StringBuffer str = new StringBuffer("");
                str.append("alert('" + errMsg + "');");
                str.append("history.back();");
                rendJavaScript(response, str.toString());
                return null;
            }
            if (docMark != null) {
                docMark = docMark.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
                edocSummary.setDocMark(docMark);
            }

            // 处理第二个公文文号
            docMark = edocSummary.getDocMark2();
            try {
                docMark = this.registDocMark(edocSummary.getId(), docMark, 2, edocSummary.getEdocType(), false,
                        EdocEnum.MarkType.edocMark.ordinal());
            } catch (EdocMarkHistoryExistException e) {
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        e.getMessage());
                StringBuffer str = new StringBuffer("");
                str.append("alert('" + errMsg + "');");
                str.append("history.back();");
                rendJavaScript(response, str.toString());
                return null;
            }
            if (docMark != null) {
                docMark = docMark.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
                edocSummary.setDocMark2(docMark);
            }

            // 内部文号
            String serialNo = edocSummary.getSerialNo();
            serialNo = this.registDocMark(edocSummary.getId(), serialNo, 3, edocSummary.getEdocType(), false,
                    EdocEnum.MarkType.edocInMark.ordinal());
            if (serialNo != null) {
                serialNo = serialNo.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
                edocSummary.setSerialNo(serialNo);
            }

            Map<String, Object> options = new HashMap<String, Object>();

            EdocEnum.SendType sendType = EdocEnum.SendType.normal;

            // 是否重复发起
            if (null != request.getParameter("resend") && !"".equals(request.getParameter("resend"))) {
                sendType = EdocEnum.SendType.resend;
            }

            // 是否转发
            if (null != request.getParameter("forward") && !"".equals(request.getParameter("forward"))) {
                sendType = EdocEnum.SendType.forward;
                // 是否转发意见
                boolean isForwardOpinion = "true".equals(request.getParameter("isForwardOpinion"));
                // 转发人附言
                String additionalComment = request.getParameter("additionalComment");
                // 转发人追加的附件

                options.put("isForwardOpinion", isForwardOpinion);
                options.put("additionalComment", additionalComment);
            }

            String note = request.getParameter("note");// 发起人附言
            EdocOpinion senderOninion = new EdocOpinion();
            senderOninion.setContent(note);
            senderOninion.setIdIfNew();
            String trackMode = request.getParameter("isTrack");
            boolean track = (!isQuickSend && "1".equals(trackMode)) ? true : false;
            // 跟踪
            String trackMembers = request.getParameter("trackMembers");
            String trackRange = request.getParameter("trackRange");
            // 如果设置了跟踪指定人，但是指定人为空，则把跟踪设置为false
            if (Strings.isNotBlank(trackRange) && "0".endsWith(trackRange) && Strings.isBlank(trackMembers)) {
                track = false;
            }
            senderOninion.affairIsTrack = track;
            senderOninion.setAttribute(1);
            senderOninion.setIsHidden(false);
            senderOninion.setCreateUserId(user.getId());
            senderOninion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            senderOninion.setPolicy(request.getParameter("policy"));
            senderOninion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
            senderOninion.setNodeId(0);

            EdocBody body = new EdocBody();
            bind(request, body);
            body.setId(UUIDLong.longUUID());
            String tempStr = request.getParameter("bodyType");
            body.setContentType(tempStr);
            Date bodyCreateDate = Datetimes.parseDatetime(request.getParameter("bodyCreateDate"));
            if (bodyCreateDate != null) {
                body.setCreateTime(new Timestamp(bodyCreateDate.getTime()));
            }

            // 从request对象取到选人信息
            // FlowData flowData = FlowData.flowdataFromRequest();
            // 角色匹配
            // String[] manualSelectNodeId =
            // request.getParameterValues("manual_select_node_id");
            // Map<String, String[]> map = new HashMap<String, String[]>();
            // if(manualSelectNodeId != null){
            // for(String node : manualSelectNodeId){
            // String[] people = request.getParameterValues("manual_select_node_id"
            // + node);
            //
            // map.put(node, people);
            // }
            // }


            // -------性能优化，该方法在新建公文单发送，编辑发送，退件箱编辑后发送都会调用，希望在新建公文发送时就不执行删除附件操作了
            String hasSummaryId = request.getParameter("newSummaryId");
            boolean isNewSent = false; // 是否新建公文发送
            if (Strings.isBlank(hasSummaryId)) {
                isNewSent = true;
            }

            // 来文登记,更新登记时间，给签收人发送消息
            if (Strings.isNotBlank(recieveIdStr)) {
                EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveIdStr));
                if (record != null) {
                    // 获得登记人
                    long registerUserId = record.getRegisterUserId();
                    // 设置当前登记的 被代理人
                    if (registerUserId != user.getId().longValue()) {
                        agentToId = registerUserId;
                    }
                    long edocId = record.getEdocId();
                    EdocSummary sendEdoc = edocManager.getEdocSummaryById(edocId, false);
                    // BUG-普通-V5-V5.1SP1-2015年8月月度修复包-20150930012635-打开待登记的公文，修改来文单位之后提交流程，查看来文单位还是修改之前的状态-唐桂林-20151012
                    if (request.getParameter("my:send_unit") == null) {
                        String sendUnit = sendEdoc.getSendUnit();
                        String sendUnit2 = sendEdoc.getSendUnit2();
                        if (Strings.isNotBlank(sendUnit2) && !sendUnit2.equals(sendUnit)) {
                            sendUnit += "," + sendUnit2;
                        }
                        edocSummary.setSendUnit(sendUnit);
                    }
                }
            }

            String waitRegister_recieveId = request.getParameter("waitRegister_recieveId");
            /**
             * 获得A8所需要签收id（各种场景：待登记中登记，从待发中编辑登记等）
             */
            recieveIdStr = RecRelationHandlerFactory.getHandler().getRecieveIdBeforeSendRec(edocSummary, recieveIdStr,
                    waitRegister_recieveId, isNewSent);

            String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");

            // 删除原有附件
            if (!isNewSent && !edocSummary.isNew()) {
                this.attachmentManager.deleteByReference(edocSummary.getId(), edocSummary.getId());
            }
            // ---------------------- start ----------------------
            // OA-65581 发起人从已发中撤销后重新发起，发现催办日志没有清空上一轮催办的记录(与协同一致，指定回退的不清)
            CtpAffair sendAffair = affairManager.getSenderAffair(edocSummary.getId());
            if (sendAffair != null) {
                int subState = sendAffair.getSubState().intValue();
                if (subState != SubStateEnum.col_pending_specialBacked.getKey()
                        && subState != SubStateEnum.col_pending_specialBackToSenderCancel.getKey()) {

                    this.superviseManager.deleteLogs(edocSummary.getId());// 删除催办日志
                }
            }
            // ---------------------- end ----------------------
            // test code begin
            if (isQuickSend) {// 快速发文后，summary的状态为结束。
                edocSummary.setState(CollaborationEnum.flowState.finish.ordinal());
                edocSummary.setFinished(true);
                edocSummary.setCompleteTime(new Timestamp(System.currentTimeMillis()));
                edocSummary.setCanTrack(0);
                edocSummary.setDeadline(null);
                edocSummary.setDeadlineDatetime(null);
            } else {
                edocSummary.setState(CollaborationEnum.flowState.run.ordinal());
            }

            edocSummary.setCreateTime(new Timestamp(System.currentTimeMillis()));
            // 流程期限具体时间需要做版本控制

            if (!isQuickSend && "true".equals(isG6)) {
                String deadlineTime = request.getParameter("deadlineTime"); // 如果选的是流程期限具体时间，在这里进行运算，会更精确。
                Long deadlineValue = getMinValueByDeadlineTime(deadlineTime, edocSummary.getCreateTime());
                if (deadlineValue != -1L) {
                    edocSummary.setDeadline(deadlineValue);
                }
            }

            if (edocSummary.getStartTime() == null) {
                edocSummary.setStartTime(new Timestamp(System.currentTimeMillis()));
            }
            edocSummary.setStartUserId(agentToId == null ? user.getId() : agentToId);
            edocSummary.setFormId(Long.parseLong(request.getParameter("edoctable")));
            V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, edocSummary.getStartUserId());
            edocSummary.setStartMember(member);
            // 如果公文单无登记人，自动赋上登记节为发起人。yangzd
            if (request.getParameter("my:create_person") == null) {
                edocSummary.setCreatePerson(user.getName());
            }
            // yangzd
            if (edocSummary.getOrgAccountId() == null) {
                edocSummary.setOrgAccountId(user.getLoginAccount());
            }
            edocSummary.setOrgDepartmentId(getEdocOwnerDepartmentId(edocSummary.getOrgAccountId(), agentToId));
            // OA-40757 收文登记簿查询，没有把公文的发文单位查询出来
            if (edocSummary.getEdocType() == 1 && request.getParameter("my:send_unit") != null) {
                // 当是纸质登记时，如果收文单中有发文单位的公文元素，需要在收文summary中设置发文单位
                edocSummary.setSendUnit(request.getParameter("my:send_unit"));
            }
            // BUG_普通_V5_V5.1sp1_发文管理的快速发文，主送单位为机构组（含两个部门）。收文管理时调用模板，主送单位元素显示的是机构组名称，发送出去后再看显示的是单位名称了_20150123006723_20150127
            if (edocSummary.getEdocType() == 1 && request.getParameter("my:send_to") != null) {
                edocSummary.setSendTo(request.getParameter("my:send_to"));
            }

            body.setIdIfNew();
            if (body.getCreateTime() == null) {
                body.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
            body.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            // test code end
            // 保存附件
            String attaFlag = attachmentManager.create(ApplicationCategoryEnum.edoc, edocSummary.getId(),
                    edocSummary.getId(), request);
            if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
                edocSummary.setHasAttachments(true);

                // 拟文发送的时候,附件只保存附件,不保存关联文档
                String[] filenames = request
                        .getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_filename);
                String[] fileTypes = request
                        .getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_type);

                edocSummary.setAttachments(EdocHelper.getAttachments(filenames, fileTypes));
            }
            boolean isNew = edocSummary.isNew();
            // OA-45558 登记外来公文时，保存待发后，将待发公文发送后，待发中仍存在该公文，已发中也有该公文
            if (!isNew && Strings.isNotBlank(hasSummaryId)) {
                // 待发编辑发送时，这里需要设置为之前的id，不然在manager中就不会更新affair了
                edocSummary.setId(Long.parseLong(hasSummaryId));
            }
            Long affairId = 0L;

            Long detailId = null;
            if (!isNewSent) {
                detailId = edocSummary.getId();
            }
            Map<String, String> superviseMap = new HashMap<String, String>();
            superviseMap.put("detailId", detailId == null ? null : String.valueOf(detailId));
            superviseMap.put("supervisorIds", request.getParameter("supervisorId"));
            superviseMap.put("supervisorNames", request.getParameter("supervisors"));
            superviseMap.put("awakeDate", request.getParameter("awakeDate"));
            String title = request.getParameter("title");
            if (Strings.isBlank(title)) {
                title = request.getParameter("superviseTitle");
            }
            superviseMap.put("title", title);
            edocSummary.setSuperviseMap(superviseMap);

            String process_xml = request.getParameter("process_xml");
            String templeteProcessId = request.getParameter("templeteProcessId");
            //zhou
//            if (isQuickSend == false) {
//                int flag = transitionPdf(edocSummary, body);
//                if (flag == -1) {
//                    StringBuffer sb = new StringBuffer();
//                    sb.append("alert('" + StringEscapeUtils.escapeJavaScript("转换服务出错了，请联系管理员") + "');");
//                    sb.append("history.back();");
//                    rendJavaScript(response, sb.toString());
//                    return null;
//                }
//            }
            try {
                affairId = edocManager.transRunCase(edocSummary, body, senderOninion, sendType, options, comm, agentToId,
                        isNewSent, process_xml, workflowNodePeoplesInput, workflowNodeConditionInput, templeteProcessId);
            } catch (Exception e) {
                LOGGER.error("发起公文流程异常", e);
            }

            // 不跟踪 或者 全部跟踪的时候不向部门跟踪表中添加数据，所以将下面这个参数串设置为空。
            if (!track || "1".equals(trackRange)) {
                trackMembers = "";
            }
            edocManager.setTrack(affairId, track, trackMembers);

            // 全文检索入库
            add2Index(edocSummary.getEdocType(), edocSummary.getId());

            if (edocSummary != null && edocSummary.getEdocType() == 1) {
                RecRelationAfterSendParam param = new RecRelationAfterSendParam();
                param.setSummary(edocSummary);
                param.setRegister(edocRegister);
                param.setUser(user);
                param.setProcessType(processType);
                param.setRecieveId(recieveIdStr);
                param.setWaitRegister_recieveId(waitRegister_recieveId);
                /**
                 * 保存收文summary数据后续处理(更新签收，登记数据状态)
                 */
                RecRelationHandlerFactory.getHandler().transAfterSendRec(param);
            }

            /* puyc 关联收文 */
            Long sendSummaryId = edocSummary.getId();// 发文Id
            String relationRecIdStr = request.getParameter("relationRecId"); // 在分发的时候没有值
            String relationRec = request.getParameter("relationRecd");
            //// 待登记关联发文时，关联id用签收id
            String recieveId = request.getParameter("recieveId");
            String forwordType = request.getParameter("forwordType");
            String forwordtosend_recAffairId = request.getParameter("forwordtosend_recAffairId");

            if (Strings.isNotBlank(relationRec) && "haveYes".equals(relationRec)) {
                EdocSummaryRelation edocSummaryRelation = new EdocSummaryRelation();
                edocSummaryRelation.setIdIfNew();
                edocSummaryRelation.setSummaryId(sendSummaryId);// 发文Id
                edocSummaryRelation.setRelationEdocId(Long.parseLong(relationRecIdStr));// 收文Id
                edocSummaryRelation.setEdocType(0);// 发文Type
                if (Strings.isNotBlank(forwordtosend_recAffairId)) {
                    edocSummaryRelation.setRecAffairId(Long.parseLong(forwordtosend_recAffairId));
                }
                edocSummaryRelation.setMemberId(user.getId());
                this.edocSummaryRelationManager.saveEdocSummaryRelation(edocSummaryRelation);
            }

            /* puyc 关联发文 */
            String relationSend = request.getParameter("relationSend");
            if (Strings.isNotBlank(relationSend) && "haveYes".equals(relationSend)) {
                if (Strings.isNotBlank(recieveId) || Strings.isNotBlank(relationRecIdStr)) {
                    EdocSummaryRelation edocSummaryRelation = new EdocSummaryRelation();
                    edocSummaryRelation.setIdIfNew();

                    if (Strings.isNotBlank(recieveId)) {
                        edocSummaryRelation.setSummaryId(Long.parseLong(recieveId));// 签收Id
                    } else {
                        edocSummaryRelation.setSummaryId(Long.parseLong(relationRecIdStr));// 收文Id
                    }
                    edocSummaryRelation.setRelationEdocId(sendSummaryId);// 发文Id
                    edocSummaryRelation.setEdocType(1);// 收文Type
                    // changyi 加上转发人ID
                    edocSummaryRelation.setMemberId(user.getId());
                    if ("registered".equals(forwordType)) {
                        edocSummaryRelation.setType(1);
                    } else if ("waitSent".equals(forwordType)) {
                        edocSummaryRelation.setType(2);
                    }
                    this.edocSummaryRelationManager.saveEdocSummaryRelation(edocSummaryRelation);
                }
            }
            /* puyc 收文关联发文 end */

            // 是否更新发文关联收文的 recAffairId
            isUpdateRecRelation(edocSummary);

            // ****快速发文start***
            if (isQuickSend) {
                CtpAffair a = affairManager.get(affairId);
                // 发文拟文才有交换
                if (edocSummary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal()) {
                    // 封发的时候进行相关的问号操作，移动到历史表中。tdbug28578 以封发节点完成提交，作为流程结束标志。
                    edocMarkHistoryManager.afterSend(edocSummary);
                    // 这不知道啥玩意，处理时封发有的代码，先挪过来 TODO
                    if (edocSummary.getPackTime() == null) {
                        edocSummary.setPackTime(new Timestamp(System.currentTimeMillis()));
                    }

                    Long unitId = -1L;
                    if (edocExchangeType != -1) {
                        if (edocExchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Dept) {// 部门交换
                            edocMangerID = request.getParameter("returnDeptId");
                            if (Strings.isBlank(edocMangerID)) {
                                unitId = orgManager.getMemberById(user.getId()).getOrgDepartmentId();
                            } else {// 发起人存在副岗时，选择交换部门
                                unitId = Long.valueOf(edocMangerID);
                            }
                        } else if (edocExchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org) {// 单位交换
                            unitId = edocSummary.getOrgAccountId();
                        }
                        try {
                            sendEdocManager.create(edocSummary, unitId, edocExchangeType, edocMangerID, a, false);
                            // 更新公文统计表为封发
                            edocStatManager.setSeal(edocSummary.getId());
                        } catch (Exception e) {
                            LOGGER.error("生成公文统计表错误", e);
                            // throw new EdocException(e);
                        }
                    }
                }
                try {
                    // 快速发文——归档。start
                    if (edocSummary.getEdocType() == 0 || edocSummary.getEdocType() == 1) { // 发文
                        if (edocSummary.getArchiveId() != null && !edocSummary.getHasArchive()) {
                            edocManager.pigeonholeAffair("", a, edocSummary.getId(), edocSummary.getArchiveId(), false);
                            quickSendPigholeInfo = ResourceBundleUtil.getString(
                                    "com.seeyon.v3x.edoc.resources.i18n.EdocResource", "edoc.quickSend.pigholeInfo");
                        }
                    }
                    // 快速发文——归档。end
                } catch (Exception e) {
                    LOGGER.error("快速发文归档错误", e);
                }

            }
            // ****快速发文 end***

            //记录文号的变动日志
            if (Strings.isNotBlank(edocSummary.getDocMark())) {
                appLogManager.insertLog(user, AppLogAction.Edoc_Doc_Mark_Create, user.getName(), edocSummary.getSubject(), edocSummary.getDocMark());
            }
            if (Strings.isNotBlank(edocSummary.getSerialNo())) {
                appLogManager.insertLog(user, AppLogAction.Edoc_Serial_No_Create, user.getName(), edocSummary.getSubject(), edocSummary.getSerialNo());
            }


            String pageview = request.getParameter("pageview");
            StringBuffer sb = new StringBuffer();
            if ("listReaded".equals(pageview)) {
                sb.append("if(window.dialogArguments) {"); // 弹出
                sb.append("  	window.returnValue = \"true\";");
                sb.append("  	window.close();");
                sb.append("} else {");
                sb.append(
                        "	parent.parent.location.href='edocController.do?method=listIndex&controller=edocController.do&from=listReaded&listType=listReaded&edocType="
                                + edocSummary.getEdocType() + Functions.csrfSuffix() + "'");
                sb.append("}");
            } else if ("listReading".equals(pageview)) {
                sb.append("if(window.dialogArguments) {"); // 弹出
                sb.append("  	window.returnValue = \"true\";");
                sb.append("  	window.close();");
                sb.append("} else {");
                sb.append(
                        "	parent.parent.location.href='edocController.do?method=listIndex&controller=edocController.do&from=listReading&listType=listReading&edocType="
                                + edocSummary.getEdocType() + Functions.csrfSuffix() + "'");
                sb.append("}");
            } else {
                String openFrom = request.getParameter("openFrom");
                if ("agent".equals(request.getParameter("app")) && edocRegister != null
                        && !user.getId().equals(edocRegister.getRegisterUserId())) {// 代理人跳转到代理事项
                    sb.append("if(parent.dialogArguments || window.dialogArguments) {");
                    sb.append("  window.returnValue = \"true\";");
                    sb.append("  	window.close();");
                    sb.append("} else {");
                    sb.append(
                            "	parent.parent.parent.location.href='collaboration/pending.do?method=morePending&from=Agent" + Functions.csrfSuffix() + "';");
                    sb.append("}");
                } else if (Strings.isNotBlank(openFrom) && "ucpc".equals(openFrom)) {
                    sb.append("if(typeof(getA8Top)!='undefined') {");
                    sb.append(" getA8Top().window.close();");
                    sb.append("} else {");
                    sb.append("	parent.parent.parent.window.close();");
                    sb.append("}");
                } else {
                    String from = Strings.isBlank(request.getParameter("from")) ? "listSent" : request.getParameter("from");
                    if (!"".equals(quickSendPigholeInfo)) {
                        sb.append("alert('" + StringEscapeUtils.escapeJavaScript(quickSendPigholeInfo) + "');");
                    }
                    sb.append("if(parent.dialogArguments || window.dialogArguments ) {");
                    sb.append("  window.returnValue = \"true\";");
                    sb.append("  	window.close();");
                    sb.append("} else {");
                    sb.append(
                            "	parent.parent.location.href='edocController.do?method=listIndex&controller=edocController.do&from="
                                    + from + "&edocType=" + edocSummary.getEdocType() + "&listType=listSent" + Functions.csrfSuffix() + "'");
                    sb.append("}");
                }
            }

            // 性能优化，删除实例对象
            SharedWithThreadLocal.remove();
            rendJavaScript(response, sb.toString());
            //--------------------------------------Pdf-----------------------------------------------------------------------------


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            edocLockManager.unlock(edocSummary.getSubject(), user.getId());
            if (lockDocMarkSubject == null) {
                edocMarkLockManager.unlock(lockDocMark);
            }
        }
        // long endtime = System.currentTimeMillis();
        return null;
    }

    public int transitionPdf(EdocSummary edocSummary, EdocBody body) {
        int flag = -1;
        try {
            //获取系统路径
            String spath = fileManager.getFolder(new Date(), false);
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

            Long val = edocSummary.getId();
            System.out.println("表单信息ID：" + val);


            // 文单路径(文单模板)
            System.out.println("获取系统路径:" + spath);
//            String sFormFilePath = spath.substring(0, spath.lastIndexOf(String.valueOf(year) + "\\")) + "template" + File.separator + edocSummary.getFormId() + ".pdf";
            int p = spath.lastIndexOf(String.valueOf(year));
            String templateFilePath = (spath.substring(0, p) + "template" + File.separator).replaceAll("\\\\", "/");

            int insertRes = 0;

            // 文单存在，则插入文单域相应的内容
            String wdPdfPath = (spath + File.separator + (edocSummary.getId()).toString() + File.separator).replaceAll("\\\\", "/");
            // -2:表示模板不存在   -1：表示合并pdf失败
            flag = insertFormRegionValue(edocSummary, templateFilePath, wdPdfPath);
            if (flag != -2) {
                // 附件转换成cebx
                // 获取相关的附件列表
                List<Attachment> atts = attachmentManager.getByReference(edocSummary.getId());
                // 合并附件用到的附件参数
                String attFileName = "";
                // 附件后缀名
                String suffix = "";
                // 无后缀的附件路径
                String filePath = "";
                File file;
                for (Attachment att : atts) {
                    suffix = att.getFilename().substring(att.getFilename().lastIndexOf("."), att.getFilename().length());
                    filePath = (spath + File.separator + att.getFileUrl()).replaceAll("\\\\", "/");
                    file = new File(filePath);
                    if (file.exists() && (".doc".equals(suffix) || ".docx".equals(suffix) || ".pdf".equals(suffix) ||
                            ".ppt".equals(suffix) || ".pptx".equals(suffix) || ".wps".equals(suffix)
                            || ".xlsx".equals(suffix) || ".xls".equals(suffix)) || ".cebx".equals(suffix)) {
                        FileUtil.copy(filePath, filePath + suffix);
                        attFileName += (filePath + suffix + "|");
                    }
                }
                // 表示附件是可以合并的附件
                if (!"".equals(attFileName)) {
                    attFileName = attFileName.substring(0, attFileName.length() - 1);
                    System.out.println("合并附件路径：" + attFileName);
                }

                //获取正文文件所在的路径
                String sBodyPath = (spath + File.separator + body.getContent() + ".doc").replaceAll("\\\\", "/");

                // copy 一份正文的doc文件
                FileUtil.copy(new File((spath + File.separator + body.getContent()).replaceAll("\\\\", "/")), new File(sBodyPath));

                //获取正文文件路径
                File bodyFile = new File(sBodyPath);
                //获取文单pdf文件路径
                File formFile = new File(wdPdfPath.concat(edocSummary.getId().toString() + ".doc"));
                if (bodyFile.exists() && formFile.exists()) {
                    System.out.println("合并文件开始！！！");
                    // 2015-07-28 modify 默认发文
                    Boolean isReceive = false;
                    // 2015-07-28 modify 表示是收文
                    if (edocSummary.getEdocType() == 1) {
                        isReceive = true;
                    }
                    //ftp://root:xkjt1234@10.100.1.76:21/2007/word.docx
                    String mergeSavePath = wdPdfPath;
                    String ftpUpload ="z:" + wdPdfPath.substring(wdPdfPath.indexOf("upload") + 6).replaceAll("\\\\", "/");
                    String wendanP = ftpUpload.concat(edocSummary.getId().toString() + ".doc");
                    String zwp = sBodyPath.substring(sBodyPath.indexOf("upload") + 6).replaceAll("\\\\", "/");
                    String zhengwenP = "z:" + zwp;
//                String fujianP = "";
                    StringBuffer fujianP = new StringBuffer();
                    if (!("").equals(attFileName) && attFileName.length() > 0) {
                        String[] arr = attFileName.split("\\|");
                        for (int i = 0; i < arr.length; i++) {
                            fujianP.append( "z:" + arr[i].substring(arr[i].indexOf("upload")+6).replaceAll("\\\\", "/"));
                            fujianP.append("|");
                        }
                    }

                    System.out.println("公文单地址路径：" + wendanP);
                    System.out.println("正文地址路径：" + zhengwenP);
                    System.out.println("附件地址路径：" + fujianP);
                    String mergerpath = "";
                    String formId=Long.toString(edocSummary.getFormId()).trim();
                    if(null != formId && !"".equals(formId)){
//                        合并正文
                        if(formId.equals("-7139423850050401892") || formId.equals("1542089478047025160")){
                            mergerpath = wendanP.concat("|"+zhengwenP) ;
                        }else {
//                            不合并正文
                            mergerpath = wendanP;
                        }
                    } else {
                        mergerpath = wendanP;
                    }
                    if (!("").equals(fujianP.toString())) {
                        mergerpath = mergerpath.concat("|" + fujianP.toString());
                    }

                    //类根路径  F:\Seeyon\A8\ApacheJetspeed\webapps\seeyon\WEB-INF\classes
                    String classPath = this.getClass().getResource("/").getPath();
                    mergeSavePath = (classPath.substring(0, classPath.indexOf("WEB-INF"))).concat("pdf") + File.separator + edocSummary.getId() + File.separator;
                    File f = new File(mergeSavePath);
                    if (!f.exists()) {
                        f.mkdirs();
                    }

//                    flag = mergeFormAndBody(edocSummary, "http://10.11.100.41:8088/convert/webservice/ConvertService?wsdl", mergerpath, mergeSavePath, isReceive);
                    flag = mergeFormAndBody(edocSummary, "http://10.11.100.41:8088/convert/webservice/ConvertService?wsdl", mergerpath, mergeSavePath, isReceive);
                    bodyFile.delete();
                    formFile.delete();
                    if (flag != -1) {
                        String pathPdf="smb://root:xkjt2019,@10.11.100.33/pdf"+File.separator+edocSummary.getId()+File.separator;
                        SmbFile smbFile=new SmbFile(pathPdf);
                        String filename="";
                        if(smbFile.exists()){
                            SmbFile[] smbFiles=smbFile.listFiles();
                            filename=smbFiles[0].getName();
                        }
                        smbGet(pathPdf + filename ,mergeSavePath.concat(filename));
                        String pdfpath = mergeSavePath.concat(edocSummary.getId().toString() + ".pdf");
                        File deleteFile=new File(pdfpath);
                        deleteFile.delete();
                        returnFileName(mergeSavePath, "2", new File(pdfpath));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 0:代表转换成功，-1：转换失败  -2：表示公文单模板不存在
        return flag;
    }

    public void smbGet(String remoteUrl,String pdfOfPdfDir){
        InputStream in=null;
        OutputStream out=null;
        SmbFile remoteFile=null;
        try {
            remoteFile=new SmbFile(remoteUrl);
            if(null == remoteFile){
                System.out.println("共享文件不存在");
                return;
            }
            File localFile = new File(pdfOfPdfDir);
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                out.close();
                in.close();
            }catch (IOException io){
                io.printStackTrace();
            }
            try {
                remoteFile.delete();
            }catch (SmbException smb){
                logger.error("删除共享文件出错："+smb.getMessage());
            }
        }

    }

    public String returnFileName(String path, String type, File file) {
        File wdfile = new File(path);
        File[] filesWd = wdfile.listFiles();
        String fileName = "";
        for (int i = 0; i < filesWd.length; i++) {
            if (filesWd[i].isFile()) {
                if ("1".equals(type)) {
                    fileName = filesWd[i].getName();
                } else {
                    filesWd[i].renameTo(file);
                }
            }
        }
        return fileName;
    }

    /**
     * 合并公文单与正文
     */
    public int mergeFormAndBody(EdocSummary edocSummary, String url, String mergepath, String savepath, boolean flag) {

        int cbCode = -1;

        String reverPath = savepath.replaceAll("\\\\", "/");
        CtpPdfSavepath ctpPdfSavepath = new CtpPdfSavepath(edocSummary.getId(), reverPath);

        try {
            ctpPdfSavepathManager.deleteCtpPdfSavepath(ctpPdfSavepath);
            ctpPdfSavepathManager.insertCtpPdfSavepath(ctpPdfSavepath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Client client;
        String ftpdownload =  "y:" + savepath.substring(savepath.indexOf("pdf") + 3).replaceAll("\\\\", "/");
        System.out.println("合并文件保存路径：" + ftpdownload);
        try {
            URL testUrl=new URL(url);
            HttpURLConnection huc=(HttpURLConnection)testUrl.openConnection();
            huc.setUseCaches(false);
            huc.setConnectTimeout(3000);
            int status=huc.getResponseCode();
            if(200 == status){
                client = new Client(new URL(url));
                Object[] result = client.invoke("ConcatFiles",
                        new Object[]{mergepath, ftpdownload, 0, 5, url, "test"});
                System.out.println(result[0]);
                Document document = DocumentHelper.parseText((String) result[0]);
                Element rootElt = document.getRootElement();
                String ts = rootElt.elementText("result");
                System.out.println(ts);
                cbCode = Integer.parseInt(ts);
            }

        } catch (Exception e) {
            logger.error(e.getMessage() + "，转换服务出问题了 ~v~ 请联系管理员！");
        }
        return cbCode;
    }

    public void pdfView(HttpServletRequest request, HttpServletResponse response) {
        try {
            String summaryId = (request.getParameter("id"));
            String spath = "";
            CtpPdfSavepath ctpPdfSavepath = ctpPdfSavepathManager.selectCtpPdfSavepathBySummaryId(summaryId);
            spath = ctpPdfSavepath.getSavepath();

            //pdf文件名称
            String fileName = summaryId.concat(".pdf");
            //pdf文件在服务器上的完整路径
            String realPath = spath + fileName;

            File f = new File(realPath);
            if (!f.exists()) {
                response.sendError(404, "获取Pdf文档出错了！");
                return;
            }
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            byte[] buf = new byte[1024];
            int len = 0;
            response.reset(); // 非常重要
            // 在线打开方式  
            URL u = new URL("file:///" + realPath);
            response.setContentType(u.openConnection().getContentType());
            response.setHeader("Content-Disposition", "inline; filename=" + new String(fileName.getBytes("utf-8"), "iso-8859-1"));
            // 文件名应该编码成UTF-8  
            OutputStream out = response.getOutputStream();
            while ((len = bis.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            bis.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 插入文单域相应内容
     *
     * @param edocSummary summary对象
     * @param sUrl        转换服务器地址
     * @param sFunction   使用的转换方法
     * @param sFilePath   转换的文件路径
     */
    private int insertFormRegionValue(EdocSummary edocSummary, String templetpath, String sFilePath) throws RemoteException {
        // edocSummary属性域
        StringBuffer sTarget = new StringBuffer();
        // edocSymmary属性域值
        StringBuffer sTargetValue = new StringBuffer();

        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (edocSummary.getSubject() != null && !"".equals(edocSummary.getSubject())) {
            sTarget.append("subject").append("|");
            sTargetValue.append(edocSummary.getSubject()).append("|");
        }
        dataMap.put("subject", edocSummary.getSubject());

        if (edocSummary.getCreatePerson() != null && !"".equals(edocSummary.getCreatePerson())) {
            sTarget.append("create_person").append("|");
            sTargetValue.append(edocSummary.getCreatePerson()).append("|");
        }
        dataMap.put("create_person", edocSummary.getCreatePerson());

        if (edocSummary.getSerialNo() != null && !"".equals(edocSummary.getSerialNo())) {
            sTarget.append("serial_no").append("|");
            sTargetValue.append(edocSummary.getSerialNo()).append("|");
        }
        dataMap.put("serial_no", edocSummary.getSerialNo());

        if (edocSummary.getDocMark() != null && !"".equals(edocSummary.getDocMark())) {
            sTarget.append("doc_mark").append("|");
            sTargetValue.append(edocSummary.getDocMark()).append("|");
        }
        dataMap.put("doc_mark", edocSummary.getDocMark());

        if (edocSummary.getSendUnit() != null && !"".equals(edocSummary.getSendUnit())) {
            sTarget.append("send_unit").append("|");
            sTargetValue.append(edocSummary.getSendUnit()).append("|");
        }
        dataMap.put("send_unit", edocSummary.getSendUnit());

        if (edocSummary.getIssuer() != null && !"".equals(edocSummary.getIssuer())) {
            sTarget.append("issuer").append("|");
            sTargetValue.append(edocSummary.getIssuer()).append("|");
        }
        dataMap.put("issuer", edocSummary.getIssuer());

        if (edocSummary.getSendTo() != null && !"".equals(edocSummary.getSendTo())) {
            sTarget.append("send_to").append("|");
            sTargetValue.append(edocSummary.getSendTo()).append("|");
        }
        dataMap.put("send_to", edocSummary.getSendTo());

        if (edocSummary.getCopyTo() != null && !"".equals(edocSummary.getCopyTo())) {
            sTarget.append("copy_to").append("|");
            sTargetValue.append(edocSummary.getCopyTo()).append("|");

        }
        dataMap.put("copy_to", edocSummary.getCopyTo());

        if (edocSummary.getReportTo() != null && !"".equals(edocSummary.getReportTo())) {
            sTarget.append("report_to").append("|");
            sTargetValue.append(edocSummary.getReportTo()).append("|");

        }
        dataMap.put("report_to", edocSummary.getReportTo());

        if (edocSummary.getKeywords() != null && !"".equals(edocSummary.getKeywords())) {
            sTarget.append("keyword").append("|");
            sTargetValue.append(edocSummary.getKeywords()).append("|");

        }
        dataMap.put("keyword", edocSummary.getKeywords());

        if (edocSummary.getPrintUnit() != null && !"".equals(edocSummary.getPrintUnit())) {
            sTarget.append("print_unit").append("|");
            sTargetValue.append(edocSummary.getPrintUnit()).append("|");

        }
        dataMap.put("print_unit", edocSummary.getPrintUnit());

        if (edocSummary.getPrinter() != null && !"".equals(edocSummary.getPrinter())) {
            sTarget.append("printer").append("|");
            sTargetValue.append(edocSummary.getPrinter()).append("|");

        }
        dataMap.put("printer", edocSummary.getPrinter());

		/*
		if (edocSummary.getDocMark2() != null)
		{
			sTarget.append("doc_mark2").append("|");
			sTargetValue.append(edocSummary.getDocMark2()).append("|");
		}
		if (edocSummary.getSendTo2() != null)
		{
			sTarget.append("send_to2").append("|");
			sTargetValue.append(edocSummary.getSendTo2()).append("|");
		}
		if (edocSummary.getCopyTo2() != null)
		{
			sTarget.append("copy_to2").append("|");
			sTargetValue.append(edocSummary.getCopyTo2()).append("|");
		}
		if (edocSummary.getReportTo2() != null)
		{
			sTarget.append("report_to2").append("|");
			sTargetValue.append(edocSummary.getReportTo2()).append("|");
		}
		if (edocSummary.getSendUnit2() != null)
		{
			sTarget.append("send_unit2").append("|");
			sTargetValue.append(edocSummary.getSendUnit2()).append("|");
		}
		if (edocSummary.getSendDepartment2() != null)
		{
			sTarget.append("send_department2").append("|");
			sTargetValue.append(edocSummary.getSendDepartment2()).append("|");
		}*/

        // 0代表发文，发文需要send_department属性，1代表收文，收文不需要send_department属性
        if (edocSummary.getSendDepartment() != null && !"".equals(edocSummary.getSendDepartment()) && edocSummary.getEdocType() == 0) {
            sTarget.append("send_department").append("|");
            sTargetValue.append(edocSummary.getSendDepartment()).append("|");

        }
        dataMap.put("send_department", edocSummary.getSendDepartment());

        if (edocSummary.getFilesm() != null && !"".equals(edocSummary.getFilesm())) {
            sTarget.append("filesm").append("|");
            sTargetValue.append(edocSummary.getFilesm()).append("|");

        }
        dataMap.put("filesm", edocSummary.getFilesm());

        if (edocSummary.getFilefz() != null && !"".equals(edocSummary.getFilefz())) {
            sTarget.append("filefz").append("|");
            sTargetValue.append(edocSummary.getFilefz()).append("|");

        }
        dataMap.put("filefz", edocSummary.getFilefz());

        if (edocSummary.getPhone() != null && !"".equals(edocSummary.getPhone())) {
            sTarget.append("phone").append("|");
            sTargetValue.append(edocSummary.getPhone()).append("|");

        }
        dataMap.put("phone", edocSummary.getPhone());

        if (edocSummary.getAuditor() != null && !"".equals(edocSummary.getAuditor())) {
            sTarget.append("auditor").append("|");
            sTargetValue.append(edocSummary.getAuditor()).append("|");

        }
        dataMap.put("auditor", edocSummary.getAuditor());

        if (edocSummary.getReview() != null && !"".equals(edocSummary.getReview())) {
            sTarget.append("review").append("|");
            sTargetValue.append(edocSummary.getReview()).append("|");

        }
        dataMap.put("review", edocSummary.getReview());

        if (edocSummary.getUndertaker() != null && !"".equals(edocSummary.getUndertaker())) {
            sTarget.append("undertaker").append("|");
            sTargetValue.append(edocSummary.getUndertaker()).append("|");

        }
        dataMap.put("undertaker", edocSummary.getUndertaker());

        if (edocSummary.getAttachments() != null && !"".equals(edocSummary.getAttachments())) {
            sTarget.append("attachments").append("|");
            sTargetValue.append(edocSummary.getAttachments()).append("|");

        }
        dataMap.put("attachments", edocSummary.getAttachments());

        if (edocSummary.getCopies() != null && !"".equals(edocSummary.getCopies())) {
            sTarget.append("copies").append("|");
            sTargetValue.append(edocSummary.getCopies()).append("|");

        }
        dataMap.put("copies", edocSummary.getCopies());

		/*if (edocSummary.getCopies2() != null)
		{
			sTarget.append("copies2").append("|");
			sTargetValue.append(edocSummary.getCopies2()).append("|");
		}*/
        if (edocSummary.getSigningDate() != null && !"".equals(edocSummary.getSigningDate())) {
            sTarget.append("signing_date").append("|");
            sTargetValue.append(edocSummary.getSigningDate()).append("|");

        }
        dataMap.put("signing_date", edocSummary.getSigningDate());

		/*if (edocSummary.getCreateTime() != null)
		{
			sTarget.append("createdate").append("|");
			sTargetValue.append(edocSummary.getCreateTime()).append("|");
		}*/
        if (edocSummary.getPackTime() != null && !"".equals(edocSummary.getPackTime())) {
            sTarget.append("packdate").append("|");
            sTargetValue.append(edocSummary.getPackTime()).append("|");

        }
        dataMap.put("packdate", edocSummary.getPackTime());

        if (edocSummary.getReceiptDate() != null && !"".equals(edocSummary.getReceiptDate())) {
            sTarget.append("receipt_date").append("|");
            sTargetValue.append(edocSummary.getReceiptDate()).append("|");

        }
        dataMap.put("receipt_date", edocSummary.getReceiptDate());

        if (edocSummary.getRegistrationDate() != null && !"".equals(edocSummary.getRegistrationDate())) {
            sTarget.append("registration_date").append("|");
            sTargetValue.append(edocSummary.getRegistrationDate()).append("|");

        }
        dataMap.put("registration_date", edocSummary.getRegistrationDate());

        if (edocSummary.getDocType() != null && !"".equals(edocSummary.getDocType())) {
            sTarget.append("doc_type").append("|");
            sTargetValue.append(edocSummary.getDocType()).append("|");

        }
        dataMap.put("doc_type", edocSummary.getDocType());

        if (edocSummary.getSendType() != null && !"".equals(edocSummary.getSendType())) {
            sTarget.append("send_type").append("|");
            sTargetValue.append(edocSummary.getSendType()).append("|");

        }
        dataMap.put("send_type", edocSummary.getSendType());

        if (edocSummary.getSecretLevel() != null && !"".equals(edocSummary.getSecretLevel())) {
            sTarget.append("secret_level").append("|");

            if ("1".equals(edocSummary.getSecretLevel())) {
                sTargetValue.append("普通").append("|");
                dataMap.put("secret_level", "普通");

            } else if ("2".equals(edocSummary.getSecretLevel())) {
                sTargetValue.append("秘密").append("|");
                dataMap.put("secret_level", "秘密");

            } else if ("3".equals(edocSummary.getSecretLevel())) {
                sTargetValue.append("机密").append("|");
                dataMap.put("secret_level", "机密");

            } else {
                sTargetValue.append("绝密").append("|");
                dataMap.put("secret_level", "绝密");

            }
        }
        if ("1".equals(edocSummary.getSecretLevel())) {
            sTargetValue.append("普通").append("|");
            dataMap.put("secret_level", "普通");

        } else if ("2".equals(edocSummary.getSecretLevel())) {
            sTargetValue.append("秘密").append("|");
            dataMap.put("secret_level", "秘密");

        } else if ("3".equals(edocSummary.getSecretLevel())) {
            sTargetValue.append("机密").append("|");
            dataMap.put("secret_level", "机密");

        } else {
            sTargetValue.append("绝密").append("|");
            dataMap.put("secret_level", "绝密");

        }
        if (edocSummary.getUrgentLevel() != null && !"".equals(edocSummary.getUrgentLevel())) {
            sTarget.append("urgent_level").append("|");

            if ("1".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("普通").append("|");
                dataMap.put("urgent_level", "普通");

            } else if ("2".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("平急").append("|");
                dataMap.put("urgent_level", "平急");

            } else if ("3".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("加急").append("|");
                dataMap.put("urgent_level", "加急");

            } else if ("4".equals(edocSummary.getUrgentLevel())) {
                sTargetValue.append("特急").append("|");
                dataMap.put("urgent_level", "特急");

            } else {
                sTargetValue.append("特提").append("|");
                dataMap.put("urgent_level", "特提");

            }
        }
        if ("1".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("普通").append("|");
            dataMap.put("urgent_level", "普通");

        } else if ("2".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("平急").append("|");
            dataMap.put("urgent_level", "平急");

        } else if ("3".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("加急").append("|");
            dataMap.put("urgent_level", "加急");

        } else if ("4".equals(edocSummary.getUrgentLevel())) {
            sTargetValue.append("特急").append("|");
            dataMap.put("urgent_level", "特急");

        } else {
            sTargetValue.append("特提").append("|");
            dataMap.put("urgent_level", "特提");

        }
        if (edocSummary.getKeepPeriod() != null && !"".equals(edocSummary.getKeepPeriod())) {
            sTarget.append("keep_period").append("|");
            sTargetValue.append(edocSummary.getKeepPeriod()).append("|");
        }
        dataMap.put("keep_period", edocSummary.getKeepPeriod());

        if (edocSummary.getVarchar1() != null && !"".equals(edocSummary.getVarchar1())) {
            sTarget.append("string1").append("|");
            sTargetValue.append(edocSummary.getVarchar1()).append("|");

        }
        dataMap.put("string1", edocSummary.getVarchar1());

        if (edocSummary.getVarchar2() != null && !"".equals(edocSummary.getVarchar2())) {
            sTarget.append("string2").append("|");
            sTargetValue.append(edocSummary.getVarchar2()).append("|");

        }
        dataMap.put("string2", edocSummary.getVarchar2());

        if (edocSummary.getVarchar3() != null && !"".equals(edocSummary.getVarchar3())) {
            sTarget.append("string3").append("|");
            sTargetValue.append(edocSummary.getVarchar3()).append("|");

        }
        dataMap.put("string3", edocSummary.getVarchar3());

        if (edocSummary.getVarchar4() != null && !"".equals(edocSummary.getVarchar4())) {
            sTarget.append("string4").append("|");
            sTargetValue.append(edocSummary.getVarchar4()).append("|");

        }
        dataMap.put("string4", edocSummary.getVarchar4());

        if (edocSummary.getVarchar5() != null && !"".equals(edocSummary.getVarchar5())) {
            sTarget.append("string5").append("|");
            sTargetValue.append(edocSummary.getVarchar5()).append("|");

        }
        dataMap.put("string5", edocSummary.getVarchar5());

        if (edocSummary.getVarchar6() != null && !"".equals(edocSummary.getVarchar6())) {
            sTarget.append("string6").append("|");
            sTargetValue.append(edocSummary.getVarchar6()).append("|");

        }
        dataMap.put("string6", edocSummary.getVarchar6());

        if (edocSummary.getVarchar7() != null && !"".equals(edocSummary.getVarchar7())) {
            sTarget.append("string7").append("|");
            sTargetValue.append(edocSummary.getVarchar7()).append("|");

        }
        dataMap.put("string7", edocSummary.getVarchar7());

        if (edocSummary.getVarchar8() != null && !"".equals(edocSummary.getVarchar8())) {
            sTarget.append("string8").append("|");
            sTargetValue.append(edocSummary.getVarchar8()).append("|");

        }
        dataMap.put("string8", edocSummary.getVarchar8());

        if (edocSummary.getVarchar9() != null && !"".equals(edocSummary.getVarchar9())) {
            sTarget.append("string9").append("|");
            sTargetValue.append(edocSummary.getVarchar9()).append("|");

        }
        dataMap.put("string9", edocSummary.getVarchar9());

        if (edocSummary.getVarchar10() != null && !"".equals(edocSummary.getVarchar10())) {
            sTarget.append("string10").append("|");
            sTargetValue.append(edocSummary.getVarchar10()).append("|");

        }
        dataMap.put("string10", edocSummary.getVarchar10());

        if (edocSummary.getVarchar11() != null && !"".equals(edocSummary.getVarchar11())) {
            sTarget.append("string11").append("|");
            sTargetValue.append(edocSummary.getVarchar11()).append("|");

        }
        dataMap.put("string11", edocSummary.getVarchar11());

        if (edocSummary.getVarchar12() != null && !"".equals(edocSummary.getVarchar12())) {
            sTarget.append("string12").append("|");
            sTargetValue.append(edocSummary.getVarchar12()).append("|");

        }
        dataMap.put("string12", edocSummary.getVarchar12());

        if (edocSummary.getVarchar13() != null && !"".equals(edocSummary.getVarchar13())) {
            sTarget.append("string13").append("|");
            sTargetValue.append(edocSummary.getVarchar13()).append("|");

        }
        dataMap.put("string13", edocSummary.getVarchar13());

        if (edocSummary.getVarchar14() != null && !"".equals(edocSummary.getVarchar14())) {
            sTarget.append("string14").append("|");
            sTargetValue.append(edocSummary.getVarchar14()).append("|");

        }
        dataMap.put("string14", edocSummary.getVarchar14());

        if (edocSummary.getVarchar15() != null && !"".equals(edocSummary.getVarchar15())) {
            sTarget.append("string15").append("|");
            sTargetValue.append(edocSummary.getVarchar15()).append("|");

        }
        dataMap.put("string15", edocSummary.getVarchar15());

        if (edocSummary.getVarchar16() != null && !"".equals(edocSummary.getVarchar16())) {
            sTarget.append("string16").append("|");
            sTargetValue.append(edocSummary.getVarchar16()).append("|");

        }
        dataMap.put("string16", edocSummary.getVarchar16());

        if (edocSummary.getVarchar17() != null && !"".equals(edocSummary.getVarchar17())) {
            sTarget.append("string17").append("|");
            sTargetValue.append(edocSummary.getVarchar17()).append("|");

        }
        dataMap.put("string17", edocSummary.getVarchar17());

        if (edocSummary.getVarchar18() != null && !"".equals(edocSummary.getVarchar18())) {
            sTarget.append("string18").append("|");
            sTargetValue.append(edocSummary.getVarchar18()).append("|");

        }
        dataMap.put("string18", edocSummary.getVarchar18());

        if (edocSummary.getVarchar19() != null && !"".equals(edocSummary.getVarchar19())) {
            sTarget.append("string19").append("|");
            sTargetValue.append(edocSummary.getVarchar19()).append("|");

        }
        dataMap.put("string19", edocSummary.getVarchar19());

        if (edocSummary.getVarchar20() != null && !"".equals(edocSummary.getVarchar20())) {
            sTarget.append("string20").append("|");
            sTargetValue.append(edocSummary.getVarchar20()).append("|");

        }
        dataMap.put("string20", edocSummary.getVarchar20());

        if (edocSummary.getVarchar21() != null && !"".equals(edocSummary.getVarchar21())) {
            sTarget.append("string21").append("|");
            sTargetValue.append(edocSummary.getVarchar21()).append("|");

        }
        dataMap.put("string21", edocSummary.getVarchar21());

        if (edocSummary.getVarchar22() != null && !"".equals(edocSummary.getVarchar22())) {
            sTarget.append("string22").append("|");
            sTargetValue.append(edocSummary.getVarchar22()).append("|");
        }
        dataMap.put("string22", edocSummary.getVarchar22());

        if (edocSummary.getVarchar23() != null && !"".equals(edocSummary.getVarchar23())) {
            sTarget.append("string23").append("|");
            sTargetValue.append(edocSummary.getVarchar23()).append("|");

        }
        dataMap.put("string23", edocSummary.getVarchar23());

        if (edocSummary.getVarchar24() != null && !"".equals(edocSummary.getVarchar24())) {
            sTarget.append("string24").append("|");
            sTargetValue.append(edocSummary.getVarchar24()).append("|");

        }
        dataMap.put("string24", edocSummary.getVarchar24());

        if (edocSummary.getVarchar25() != null && !"".equals(edocSummary.getVarchar25())) {
            sTarget.append("string25").append("|");
            sTargetValue.append(edocSummary.getVarchar25()).append("|");

        }
        dataMap.put("string25", edocSummary.getVarchar25());

        if (edocSummary.getVarchar26() != null && !"".equals(edocSummary.getVarchar26())) {
            sTarget.append("string26").append("|");
            sTargetValue.append(edocSummary.getVarchar26()).append("|");

        }
        dataMap.put("string26", edocSummary.getVarchar26());

        if (edocSummary.getVarchar27() != null && !"".equals(edocSummary.getVarchar27())) {
            sTarget.append("string27").append("|");
            sTargetValue.append(edocSummary.getVarchar27()).append("|");

        }
        dataMap.put("string27", edocSummary.getVarchar27());

        if (edocSummary.getVarchar28() != null && !"".equals(edocSummary.getVarchar28())) {
            sTarget.append("string28").append("|");
            sTargetValue.append(edocSummary.getVarchar28()).append("|");

        }
        dataMap.put("string28", edocSummary.getVarchar28());

        if (edocSummary.getVarchar29() != null && !"".equals(edocSummary.getVarchar29())) {
            sTarget.append("string29").append("|");
            sTargetValue.append(edocSummary.getVarchar29()).append("|");

        }
        dataMap.put("string29", edocSummary.getVarchar29());

        if (edocSummary.getVarchar30() != null && !"".equals(edocSummary.getVarchar30())) {
            sTarget.append("string30").append("|");
            sTargetValue.append(edocSummary.getVarchar30()).append("|");

        }
        dataMap.put("string30", edocSummary.getVarchar30());

        if (edocSummary.getText1() != null && !"".equals(edocSummary.getText1())) {
            sTarget.append("text1").append("|");
            sTargetValue.append(edocSummary.getText1()).append("|");

        }
        dataMap.put("text1", edocSummary.getText1());

        if (edocSummary.getText2() != null && !"".equals(edocSummary.getText2())) {
            sTarget.append("text2").append("|");
            sTargetValue.append(edocSummary.getText2()).append("|");

        }
        dataMap.put("text2", edocSummary.getText2());

        if (edocSummary.getText3() != null && !"".equals(edocSummary.getText3())) {
            sTarget.append("text3").append("|");
            sTargetValue.append(edocSummary.getText3()).append("|");

        }
        dataMap.put("text3", edocSummary.getText3());

        if (edocSummary.getText4() != null && !"".equals(edocSummary.getText4())) {
            sTarget.append("text4").append("|");
            sTargetValue.append(edocSummary.getText4()).append("|");

        }
        dataMap.put("text4", edocSummary.getText4());

        if (edocSummary.getText5() != null && !"".equals(edocSummary.getText5())) {
            sTarget.append("text5").append("|");
            sTargetValue.append(edocSummary.getText5()).append("|");

        }
        dataMap.put("text5", edocSummary.getText5());

        if (edocSummary.getText6() != null && !"".equals(edocSummary.getText6())) {
            sTarget.append("text6").append("|");
            sTargetValue.append(edocSummary.getText6()).append("|");

        }
        dataMap.put("text6", edocSummary.getText6());

        if (edocSummary.getText7() != null && !"".equals(edocSummary.getText7())) {
            sTarget.append("text7").append("|");
            sTargetValue.append(edocSummary.getText7()).append("|");

        }
        dataMap.put("text7", edocSummary.getText7());

        if (edocSummary.getText8() != null && !"".equals(edocSummary.getText8())) {
            sTarget.append("text8").append("|");
            sTargetValue.append(edocSummary.getText8()).append("|");

        }
        dataMap.put("text8", edocSummary.getText8());

        if (edocSummary.getText9() != null && !"".equals(edocSummary.getText9())) {
            sTarget.append("text9").append("|");
            sTargetValue.append(edocSummary.getText9()).append("|");

        }
        dataMap.put("text9", edocSummary.getText9());

        if (edocSummary.getText10() != null && !"".equals(edocSummary.getText10())) {
            sTarget.append("text10").append("|");
            sTargetValue.append(edocSummary.getText10()).append("|");

        }
        dataMap.put("text10", edocSummary.getText10());

        if (edocSummary.getText11() != null && !"".equals(edocSummary.getText11())) {
            sTarget.append("text11").append("|");
            sTargetValue.append(edocSummary.getText11()).append("|");

        }
        dataMap.put("text11", edocSummary.getText11());

        if (edocSummary.getText12() != null && !"".equals(edocSummary.getText12())) {
            sTarget.append("text12").append("|");
            sTargetValue.append(edocSummary.getText12()).append("|");

        }
        dataMap.put("text12", edocSummary.getText12());

        if (edocSummary.getText13() != null && !"".equals(edocSummary.getText13())) {
            sTarget.append("text13").append("|");
            sTargetValue.append(edocSummary.getText13()).append("|");

        }
        dataMap.put("text13", edocSummary.getText13());

        if (edocSummary.getText14() != null && !"".equals(edocSummary.getText14())) {
            sTarget.append("text14").append("|");
            sTargetValue.append(edocSummary.getText14()).append("|");

        }
        dataMap.put("text14", edocSummary.getText14());

        if (edocSummary.getText15() != null && !"".equals(edocSummary.getText15())) {
            sTarget.append("text15").append("|");
            sTargetValue.append(edocSummary.getText15()).append("|");

        }
        dataMap.put("text15", edocSummary.getText15());

        if (edocSummary.getInteger1() != null && !"".equals(edocSummary.getInteger1())) {
            sTarget.append("integer1").append("|");
            sTargetValue.append(edocSummary.getInteger1()).append("|");

        }
        dataMap.put("integer1", edocSummary.getInteger1());

        if (edocSummary.getInteger2() != null && !"".equals(edocSummary.getInteger2())) {
            sTarget.append("integer2").append("|");
            sTargetValue.append(edocSummary.getInteger2()).append("|");

        }
        dataMap.put("integer2", edocSummary.getInteger2());

        if (edocSummary.getInteger3() != null && !"".equals(edocSummary.getInteger3())) {
            sTarget.append("integer3").append("|");
            sTargetValue.append(edocSummary.getInteger3()).append("|");

        }
        dataMap.put("integer3", edocSummary.getInteger3());

        if (edocSummary.getInteger4() != null && !"".equals(edocSummary.getInteger4())) {
            sTarget.append("integer4").append("|");
            sTargetValue.append(edocSummary.getInteger4()).append("|");

        }
        dataMap.put("integer4", edocSummary.getInteger4());

        if (edocSummary.getInteger5() != null && !"".equals(edocSummary.getInteger5())) {
            sTarget.append("integer5").append("|");
            sTargetValue.append(edocSummary.getInteger5()).append("|");

        }
        dataMap.put("integer5", edocSummary.getInteger5());

        if (edocSummary.getInteger6() != null && !"".equals(edocSummary.getInteger6())) {
            sTarget.append("integer6").append("|");
            sTargetValue.append(edocSummary.getInteger6()).append("|");

        }
        dataMap.put("integer6", edocSummary.getInteger6());

        if (edocSummary.getInteger7() != null && !"".equals(edocSummary.getInteger7())) {
            sTarget.append("integer7").append("|");
            sTargetValue.append(edocSummary.getInteger7()).append("|");

        }
        dataMap.put("integer7", edocSummary.getInteger7());

        if (edocSummary.getInteger8() != null && !"".equals(edocSummary.getInteger8())) {
            sTarget.append("integer8").append("|");
            sTargetValue.append(edocSummary.getInteger8()).append("|");

        }
        dataMap.put("integer8", edocSummary.getInteger8());

        if (edocSummary.getInteger9() != null && !"".equals(edocSummary.getInteger9())) {
            sTarget.append("integer9").append("|");
            sTargetValue.append(edocSummary.getInteger9()).append("|");

        }
        dataMap.put("integer9", edocSummary.getInteger9());

        if (edocSummary.getInteger10() != null && !"".equals(edocSummary.getInteger10())) {
            sTarget.append("integer10").append("|");
            sTargetValue.append(edocSummary.getInteger10()).append("|");

        }
        dataMap.put("integer10", edocSummary.getInteger10());

        if (edocSummary.getInteger11() != null && !"".equals(edocSummary.getInteger11())) {
            sTarget.append("integer11").append("|");
            sTargetValue.append(edocSummary.getInteger11()).append("|");

        }
        dataMap.put("integer11", edocSummary.getInteger11());

        if (edocSummary.getInteger12() != null && !"".equals(edocSummary.getInteger12())) {
            sTarget.append("integer12").append("|");
            sTargetValue.append(edocSummary.getInteger12()).append("|");

        }
        dataMap.put("integer12", edocSummary.getInteger12());

        if (edocSummary.getInteger13() != null && !"".equals(edocSummary.getInteger13())) {
            sTarget.append("integer13").append("|");
            sTargetValue.append(edocSummary.getInteger13()).append("|");

        }
        dataMap.put("integer13", edocSummary.getInteger13());

        if (edocSummary.getInteger14() != null && !"".equals(edocSummary.getInteger14())) {
            sTarget.append("integer14").append("|");
            sTargetValue.append(edocSummary.getInteger14()).append("|");

        }
        dataMap.put("integer14", edocSummary.getInteger14());

        if (edocSummary.getInteger15() != null && !"".equals(edocSummary.getInteger15())) {
            sTarget.append("integer15").append("|");
            sTargetValue.append(edocSummary.getInteger15()).append("|");

        }
        dataMap.put("integer15", edocSummary.getInteger15());

        if (edocSummary.getInteger16() != null && !"".equals(edocSummary.getInteger16())) {
            sTarget.append("integer16").append("|");
            sTargetValue.append(edocSummary.getInteger16()).append("|");

        }
        dataMap.put("integer16", edocSummary.getInteger16());

        if (edocSummary.getInteger17() != null && !"".equals(edocSummary.getInteger17())) {
            sTarget.append("integer17").append("|");
            sTargetValue.append(edocSummary.getInteger17()).append("|");

        }
        dataMap.put("integer17", edocSummary.getInteger17());

        if (edocSummary.getInteger18() != null && !"".equals(edocSummary.getInteger18())) {
            sTarget.append("integer18").append("|");
            sTargetValue.append(edocSummary.getInteger18()).append("|");

        }
        dataMap.put("integer18", edocSummary.getInteger18());

        if (edocSummary.getInteger19() != null && !"".equals(edocSummary.getInteger19())) {
            sTarget.append("integer19").append("|");
            sTargetValue.append(edocSummary.getInteger19()).append("|");

        }
        dataMap.put("integer19", edocSummary.getInteger19());

        if (edocSummary.getInteger20() != null && !"".equals(edocSummary.getInteger20())) {
            sTarget.append("integer20").append("|");
            sTargetValue.append(edocSummary.getInteger20()).append("|");

        }
        dataMap.put("integer20", edocSummary.getInteger20());

        if (edocSummary.getDecimal1() != null && !"".equals(edocSummary.getDecimal1())) {
            sTarget.append("decimal1").append("|");
            sTargetValue.append(edocSummary.getDecimal1()).append("|");

        }
        dataMap.put("decimal1", edocSummary.getDecimal1());

        if (edocSummary.getDecimal2() != null && !"".equals(edocSummary.getDecimal2())) {
            sTarget.append("decimal2").append("|");
            sTargetValue.append(edocSummary.getDecimal2()).append("|");

        }
        dataMap.put("decimal2", edocSummary.getDecimal2());

        if (edocSummary.getDecimal3() != null && !"".equals(edocSummary.getDecimal3())) {
            sTarget.append("decimal3").append("|");
            sTargetValue.append(edocSummary.getDecimal3()).append("|");

        }
        dataMap.put("decimal3", edocSummary.getDecimal3());

        if (edocSummary.getDecimal4() != null && !"".equals(edocSummary.getDecimal4())) {
            sTarget.append("decimal4").append("|");
            sTargetValue.append(edocSummary.getDecimal4()).append("|");

        }
        dataMap.put("decimal4", edocSummary.getDecimal4());

        if (edocSummary.getDecimal5() != null && !"".equals(edocSummary.getDecimal5())) {
            sTarget.append("decimal5").append("|");
            sTargetValue.append(edocSummary.getDecimal5()).append("|");

        }
        dataMap.put("decimal5", edocSummary.getDecimal5());

        if (edocSummary.getDecimal6() != null && !"".equals(edocSummary.getDecimal6())) {
            sTarget.append("decimal6").append("|");
            sTargetValue.append(edocSummary.getDecimal6()).append("|");

        }
        dataMap.put("decimal6", edocSummary.getDecimal6());

        if (edocSummary.getDecimal7() != null && !"".equals(edocSummary.getDecimal7())) {
            sTarget.append("decimal7").append("|");
            sTargetValue.append(edocSummary.getDecimal7()).append("|");

        }
        dataMap.put("decimal7", edocSummary.getDecimal7());

        if (edocSummary.getDecimal8() != null && !"".equals(edocSummary.getDecimal8())) {
            sTarget.append("decimal8").append("|");
            sTargetValue.append(edocSummary.getDecimal8()).append("|");

        }
        dataMap.put("decimal8", edocSummary.getDecimal8());

        if (edocSummary.getDecimal9() != null && !"".equals(edocSummary.getDecimal9())) {
            sTarget.append("decimal9").append("|");
            sTargetValue.append(edocSummary.getDecimal9()).append("|");

        }
        dataMap.put("decimal9", edocSummary.getDecimal9());

        if (edocSummary.getDecimal10() != null && !"".equals(edocSummary.getDecimal10())) {
            sTarget.append("decimal10").append("|");
            sTargetValue.append(edocSummary.getDecimal10()).append("|");

        }
        dataMap.put("decimal10", edocSummary.getDecimal10());

        if (edocSummary.getDecimal11() != null && !"".equals(edocSummary.getDecimal11())) {
            sTarget.append("decimal11").append("|");
            sTargetValue.append(edocSummary.getDecimal11()).append("|");

        }
        dataMap.put("decimal11", edocSummary.getDecimal11());

        if (edocSummary.getDecimal12() != null && !"".equals(edocSummary.getDecimal12())) {
            sTarget.append("decimal12").append("|");
            sTargetValue.append(edocSummary.getDecimal12()).append("|");

        }
        dataMap.put("decimal12", edocSummary.getDecimal12());

        if (edocSummary.getDecimal13() != null && !"".equals(edocSummary.getDecimal13())) {
            sTarget.append("decimal13").append("|");
            sTargetValue.append(edocSummary.getDecimal13()).append("|");

        }
        dataMap.put("decimal13", edocSummary.getDecimal13());

        if (edocSummary.getDecimal14() != null && !"".equals(edocSummary.getDecimal14())) {
            sTarget.append("decimal14").append("|");
            sTargetValue.append(edocSummary.getDecimal14()).append("|");

        }
        dataMap.put("decimal14", edocSummary.getDecimal14());

        if (edocSummary.getDecimal15() != null && !"".equals(edocSummary.getDecimal15())) {
            sTarget.append("decimal15").append("|");
            sTargetValue.append(edocSummary.getDecimal15()).append("|");

        }
        dataMap.put("decimal15", edocSummary.getDecimal15());

        if (edocSummary.getDecimal16() != null && !"".equals(edocSummary.getDecimal16())) {
            sTarget.append("decimal16").append("|");
            sTargetValue.append(edocSummary.getDecimal16()).append("|");

        }
        dataMap.put("decimal16", edocSummary.getDecimal16());

        if (edocSummary.getDecimal17() != null && !"".equals(edocSummary.getDecimal17())) {
            sTarget.append("decimal17").append("|");
            sTargetValue.append(edocSummary.getDecimal17()).append("|");

        }
        dataMap.put("decimal17", edocSummary.getDecimal17());

        if (edocSummary.getDecimal18() != null && !"".equals(edocSummary.getDecimal18())) {
            sTarget.append("decimal18").append("|");
            sTargetValue.append(edocSummary.getDecimal18()).append("|");

        }
        dataMap.put("decimal18", edocSummary.getDecimal18());

        if (edocSummary.getDecimal19() != null && !"".equals(edocSummary.getDecimal19())) {
            sTarget.append("decimal19").append("|");
            sTargetValue.append(edocSummary.getDecimal19()).append("|");

        }
        dataMap.put("decimal19", edocSummary.getDecimal19());

        if (edocSummary.getDecimal20() != null && !"".equals(edocSummary.getDecimal20())) {
            sTarget.append("decimal20").append("|");
            sTargetValue.append(edocSummary.getDecimal20()).append("|");

        }
        dataMap.put("decimal20", edocSummary.getDecimal20());

        if (edocSummary.getDate1() != null && !"".equals(edocSummary.getDate1())) {
            sTarget.append("date1").append("|");
            sTargetValue.append(edocSummary.getDate1()).append("|");

        }
        dataMap.put("date1", edocSummary.getDate1());

        if (edocSummary.getDate2() != null && !"".equals(edocSummary.getDate2())) {
            sTarget.append("date2").append("|");
            sTargetValue.append(edocSummary.getDate2()).append("|");

        }
        dataMap.put("date2", edocSummary.getDate2());

        if (edocSummary.getDate3() != null && !"".equals(edocSummary.getDate3())) {
            sTarget.append("date3").append("|");
            sTargetValue.append(edocSummary.getDate3()).append("|");

        }
        dataMap.put("date3", edocSummary.getDate3());

        if (edocSummary.getDate4() != null && !"".equals(edocSummary.getDate4())) {
            sTarget.append("date4").append("|");
            sTargetValue.append(edocSummary.getDate4()).append("|");

        }
        dataMap.put("date4", edocSummary.getDate4());

        if (edocSummary.getDate5() != null && !"".equals(edocSummary.getDate5())) {
            sTarget.append("date5").append("|");
            sTargetValue.append(edocSummary.getDate5()).append("|");

        }
        dataMap.put("date5", edocSummary.getDate5());

        if (edocSummary.getDate6() != null && !"".equals(edocSummary.getDate6())) {
            sTarget.append("date6").append("|");
            sTargetValue.append(edocSummary.getDate6()).append("|");

        }
        dataMap.put("date6", edocSummary.getDate6());

        if (edocSummary.getDate7() != null && !"".equals(edocSummary.getDate7())) {
            sTarget.append("date7").append("|");
            sTargetValue.append(edocSummary.getDate7()).append("|");

        }
        dataMap.put("date7", edocSummary.getDate7());

        if (edocSummary.getDate8() != null && !"".equals(edocSummary.getDate8())) {
            sTarget.append("date8").append("|");
            sTargetValue.append(edocSummary.getDate8()).append("|");

        }
        dataMap.put("date8", edocSummary.getDate8());

        if (edocSummary.getDate9() != null && !"".equals(edocSummary.getDate9())) {
            sTarget.append("date9").append("|");
            sTargetValue.append(edocSummary.getDate9()).append("|");

        }
        dataMap.put("date9", edocSummary.getDate9());

        if (edocSummary.getDate10() != null && !"".equals(edocSummary.getDate10())) {
            sTarget.append("date10").append("|");
            sTargetValue.append(edocSummary.getDate10()).append("|");

        }
        dataMap.put("date10", edocSummary.getDate10());

        if (edocSummary.getDate11() != null && !"".equals(edocSummary.getDate11())) {
            sTarget.append("date11").append("|");
            sTargetValue.append(edocSummary.getDate11()).append("|");

        }
        dataMap.put("date11", edocSummary.getDate11());

        if (edocSummary.getDate12() != null && !"".equals(edocSummary.getDate12())) {
            sTarget.append("date12").append("|");
            sTargetValue.append(edocSummary.getDate12()).append("|");

        }
        dataMap.put("date12", edocSummary.getDate12());

        if (edocSummary.getDate13() != null && !"".equals(edocSummary.getDate13())) {
            sTarget.append("date13").append("|");
            sTargetValue.append(edocSummary.getDate13()).append("|");

        }
        dataMap.put("date13", edocSummary.getDate13());

        if (edocSummary.getDate14() != null && !"".equals(edocSummary.getDate14())) {
            sTarget.append("date14").append("|");
            sTargetValue.append(edocSummary.getDate14()).append("|");

        }
        dataMap.put("date14", edocSummary.getDate14());

        if (edocSummary.getDate15() != null && !"".equals(edocSummary.getDate15())) {
            sTarget.append("date15").append("|");
            sTargetValue.append(edocSummary.getDate15()).append("|");

        }
        dataMap.put("date15", edocSummary.getDate15());

        if (edocSummary.getDate16() != null && !"".equals(edocSummary.getDate16())) {
            sTarget.append("date16").append("|");
            sTargetValue.append(edocSummary.getDate16()).append("|");

        }
        dataMap.put("date16", edocSummary.getDate16());

        if (edocSummary.getDate17() != null && !"".equals(edocSummary.getDate17())) {
            sTarget.append("date17").append("|");
            sTargetValue.append(edocSummary.getDate17()).append("|");

        }
        dataMap.put("date17", edocSummary.getDate17());

        if (edocSummary.getDate18() != null && !"".equals(edocSummary.getDate18())) {
            sTarget.append("date18").append("|");
            sTargetValue.append(edocSummary.getDate18()).append("|");

        }
        dataMap.put("date18", edocSummary.getDate18());

        if (edocSummary.getDate19() != null && !"".equals(edocSummary.getDate19())) {
            sTarget.append("date19").append("|");
            sTargetValue.append(edocSummary.getDate19()).append("|");

        }
        dataMap.put("date19", edocSummary.getDate19());

        if (edocSummary.getDate20() != null && !"".equals(edocSummary.getDate20())) {
            sTarget.append("date20").append("|");
            sTargetValue.append(edocSummary.getDate20()).append("|");

        }
        dataMap.put("date20", edocSummary.getDate20());

        if (edocSummary.getList1() != null && !"".equals(edocSummary.getList1())) {
            sTarget.append("list1").append("|");
            sTargetValue.append(edocSummary.getList1()).append("|");

        }
        dataMap.put("list1", edocSummary.getList1());

        if (edocSummary.getList2() != null && !"".equals(edocSummary.getList2())) {
            sTarget.append("list2").append("|");
            sTargetValue.append(edocSummary.getList2()).append("|");

        }
        dataMap.put("list2", edocSummary.getList2());

        if (edocSummary.getList3() != null && !"".equals(edocSummary.getList3())) {
            sTarget.append("list3").append("|");
            sTargetValue.append(edocSummary.getList3()).append("|");

        }
        dataMap.put("list3", edocSummary.getList3());

        if (edocSummary.getList4() != null && !"".equals(edocSummary.getList4())) {
            sTarget.append("list4").append("|");
            sTargetValue.append(edocSummary.getList4()).append("|");

        }
        dataMap.put("list4", edocSummary.getList4());

        if (edocSummary.getList5() != null && !"".equals(edocSummary.getList5())) {
            sTarget.append("list5").append("|");
            sTargetValue.append(edocSummary.getList5()).append("|");

        }
        dataMap.put("list5", edocSummary.getList5());

        if (edocSummary.getList6() != null && !"".equals(edocSummary.getList6())) {
            sTarget.append("list6").append("|");
            sTargetValue.append(edocSummary.getList6()).append("|");

        }
        dataMap.put("list6", edocSummary.getList6());

        if (edocSummary.getList7() != null && !"".equals(edocSummary.getList7())) {
            sTarget.append("list7").append("|");
            sTargetValue.append(edocSummary.getList7()).append("|");

        }
        dataMap.put("list7", edocSummary.getList7());

        if (edocSummary.getList8() != null && !"".equals(edocSummary.getList8())) {
            sTarget.append("list8").append("|");
            sTargetValue.append(edocSummary.getList8()).append("|");

        }
        dataMap.put("list8", edocSummary.getList8());

        if (edocSummary.getList9() != null && !"".equals(edocSummary.getList9())) {
            sTarget.append("list9").append("|");
            sTargetValue.append(edocSummary.getList9()).append("|");

        }
        dataMap.put("list9", edocSummary.getList9());

        if (edocSummary.getList10() != null && !"".equals(edocSummary.getList10())) {
            sTarget.append("list10").append("|");
            sTargetValue.append(edocSummary.getList10()).append("|");

        }
        dataMap.put("list10", edocSummary.getList10());

        if (edocSummary.getList11() != null && !"".equals(edocSummary.getList11())) {
            sTarget.append("list11").append("|");
            sTargetValue.append(edocSummary.getList11()).append("|");

        }
        dataMap.put("list11", edocSummary.getList11());

        if (edocSummary.getList12() != null && !"".equals(edocSummary.getList12())) {
            sTarget.append("list12").append("|");
            sTargetValue.append(edocSummary.getList12()).append("|");

        }
        dataMap.put("list12", edocSummary.getList12());

        if (edocSummary.getList13() != null && !"".equals(edocSummary.getList13())) {
            sTarget.append("list13").append("|");
            sTargetValue.append(edocSummary.getList13()).append("|");

        }
        dataMap.put("list13", edocSummary.getList13());

        if (edocSummary.getList14() != null && !"".equals(edocSummary.getList14())) {
            sTarget.append("list14").append("|");
            sTargetValue.append(edocSummary.getList14()).append("|");

        }
        dataMap.put("list14", edocSummary.getList14());

        if (edocSummary.getList15() != null && !"".equals(edocSummary.getList15())) {
            sTarget.append("list15").append("|");
            sTargetValue.append(edocSummary.getList15()).append("|");

        }
        dataMap.put("list15", edocSummary.getList15());

        if (edocSummary.getList16() != null && !"".equals(edocSummary.getList16())) {
            sTarget.append("list16").append("|");
            sTargetValue.append(edocSummary.getList16()).append("|");

        }
        dataMap.put("list16", edocSummary.getList16());

        if (edocSummary.getList17() != null && !"".equals(edocSummary.getList17())) {
            sTarget.append("list17").append("|");
            sTargetValue.append(edocSummary.getList17()).append("|");

        }
        dataMap.put("list17", edocSummary.getList17());

        if (edocSummary.getList18() != null && !"".equals(edocSummary.getList18())) {
            sTarget.append("list18").append("|");
            sTargetValue.append(edocSummary.getList18()).append("|");

        }
        dataMap.put("list18", edocSummary.getList18());

        if (edocSummary.getList19() != null && !"".equals(edocSummary.getList19())) {
            sTarget.append("list19").append("|");
            sTargetValue.append(edocSummary.getList19()).append("|");

        }
        dataMap.put("list19", edocSummary.getList19());

        if (edocSummary.getList20() != null && !"".equals(edocSummary.getList20())) {
            sTarget.append("list20").append("|");
            sTargetValue.append(edocSummary.getList20()).append("|");

        }
        dataMap.put("list20", edocSummary.getList20());


        System.out.println("sTarget::" + sTarget.substring(0, sTarget.lastIndexOf("|")).toString());
        System.out.println("sTargetValue::" + sTargetValue.substring(0, sTargetValue.lastIndexOf("|")).toString());


        int flag = 0;
        /**第二种方法：采用freemark模板 */
        Configuration configuration = new Configuration();
        try {
            configuration.setDefaultEncoding("UTF-8");
            configuration.setDirectoryForTemplateLoading(new File(templetpath));
            String wordpath = sFilePath + edocSummary.getId() + ".doc";
            File outFile = new File(wordpath);
            File fileParent = outFile.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            Template template = configuration.getTemplate(edocSummary.getFormId().toString() + ".ftl", "utf-8");
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"), 10240);
            template.process(dataMap, out);
            out.close();
        } catch (IOException e) {
            flag = -2;
            logger.error(e.getMessage() + ",模板不存在！");
        } catch (Exception e) {
            flag = -2;
            logger.error(e.getMessage() + ",模板不存在！");
        }

        return flag;
    }


    /**
     * 是否更新发文关联收文的 recAffairId
     *
     * @param edocSummary
     * @throws BusinessException
     */
    private void isUpdateRecRelation(EdocSummary edocSummary) throws BusinessException {
        // 当是收文的时候
        if (edocSummary.getEdocType() == 1 && edocSummary.getId() != null) {
            List<EdocSummaryRelation> relationList = edocSummaryRelationManager
                    .findRecEdocByRelationEdocId(edocSummary.getId(), 0);
            // 因为收文撤销后再发送时，会新产生收文已发和待办affair数据，因此转发关联表中的发文关联收文的数据就需要更新，用新的affairId
            if (Strings.isNotEmpty(relationList)) {
                List<CtpAffair> affList = affairManager.getValidAffairs(ApplicationCategoryEnum.edocRec,
                        edocSummary.getId());

                for (EdocSummaryRelation relation : relationList) {
                    long recAffairId = relation.getRecAffairId();
                    // 关联表中 之前关联的收文affair
                    CtpAffair recAffair = affairManager.get(recAffairId);
                    // 再次发送收文，新生成的affair
                    for (CtpAffair newAff : affList) {
                        // 收文被撤销时，之前已发affair状态变为待发
                        if ((recAffair.getState() == StateEnum.col_waitSend.key()
                                && newAff.getState() == StateEnum.col_sent.key())
                                || (recAffair.getState() != StateEnum.col_waitSend.key()
                                && newAff.getState() == StateEnum.col_pending.key())) {
                            relation.setRecAffairId(newAff.getId());
                            break;
                        }
                    }
                }
                edocSummaryRelationManager.updateEdocSummaryRelationList(relationList);
            }

        }
    }

    /**
     * 待分发-阅文批处理
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView readBatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RegisterBody registerBody = null;
        EdocRegister edocRegister = null;
        EdocSummary summary = null;// 定义表单存储对象
        EdocBody edocBody = null;
        User user = AppContext.getCurrentUser();
        EdocForm defaultEdocForm = null; // 获取默认表单
        String note = request.getParameter("comment");// 获取页面附言信息
        String registerIdsStr = request.getParameter("registerStr");// 获取登记表ID
        String[] registerId = registerIdsStr.split("&");
        String process_xml = request.getParameter("processXml");
        defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(), 1);

        StringBuilder notPassMsg = new StringBuilder();
        EdocSummary fromSummary = null;
        if (registerId != null) {// 判断页面是否选择，Js有验证，加一层错误提示保险
            for (int i = 0; i < registerId.length; i++) {// 批量处理需要办理的待发文单
                // 来文信息
                long recieveId = Long.parseLong(registerId[i]);// 遍历登记表ID
                edocRegister = edocRegisterManager.getEdocRegister(recieveId);// 获取登记表信息
                // //
                fromSummary = edocManager.getEdocSummaryById(edocRegister.getEdocId(), true);
                summary = new EdocSummary();// 建立新表单
                summary.bind(edocRegister);// 将登记表值赋给表单信息表
                summary.setSigningDate(edocRegister.getEdocDate());
                summary.setFormId(defaultEdocForm.getId());// 将默认表单ID放入表单对象中
                String docMark = summary.getDocMark();// 获取当前痕迹
                if (docMark != null) {
                    summary.setDocMark(docMark);
                }
                // 处理文号
                docMark = summary.getDocMark2();
                docMark = this.registDocMark(summary.getId(), docMark, 2, summary.getEdocType(), false,
                        EdocEnum.MarkType.edocMark.ordinal());
                if (docMark != null) {
                    summary.setDocMark2(docMark);
                }
                // 内部文号
                String serialNo = summary.getSerialNo();
                if (serialNo == null) {
                    summary.setSerialNo(this.registDocMark(summary.getId(), serialNo, 3, summary.getEdocType(), false,
                            EdocEnum.MarkType.edocInMark.ordinal()));
                }
                summary.setState(CollaborationEnum.flowState.run.ordinal());// 将此流程设为运行状态
                summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
                if (summary.getStartTime() == null) {
                    summary.setStartTime(new Timestamp(System.currentTimeMillis()));
                }
                summary.setStartUserId(user.getId());//
                V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                summary.setStartMember(member);
                if (summary.getOrgAccountId() == null) {
                    summary.setOrgAccountId(user.getLoginAccount());
                }
                summary.setOrgDepartmentId(getEdocOwnerDepartmentId(summary.getOrgAccountId(), null));
                summary.setProcessType(Long.valueOf("2"));

                Object[] objects = edocFormManager.getEdocFormElementRequiredMsg(defaultEdocForm, summary);
                notPassMsg.append(((String) objects[1]));
                if (!(Boolean) objects[0]) {
                    continue;
                }

                // 装载公文正文
                registerBody = edocRegisterManager.findRegisterBodyByRegisterId(recieveId);
                edocBody = new EdocBody();
                edocBody.setIdIfNew();
                if (edocBody.getCreateTime() == null) {
                    edocBody.setCreateTime(new Timestamp(System.currentTimeMillis()));
                }
                edocBody.setContentType(registerBody == null ? "HTML" : registerBody.getContentType());
                edocBody.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                if (summary != null && edocRegister != null && registerBody != null) {
                    // 增加正文文件和v3x_file
                    try {
                        if (registerBody.getContent() != null) {
                            if (!"HTML".equals(registerBody.getContentType())) {
                                // 复制正文及复制v3x_file表数据
                                V3XFile file = null;
                                try {
                                    file = fileManager.clone(Long.parseLong(registerBody.getContent()), true);
                                    file.setFilename(file.getId().toString());
                                    fileManager.update(file);
                                    edocBody.setContent(file.getId().toString());
                                } catch (Exception e) {
                                    LOGGER.error("复制公文登记正文错误！", e);
                                }
                                // 复制印章什么的？
                            }
                        }
                        // 增加附件
                        if (fromSummary != null) {
                            this.attachmentManager.copy(fromSummary.getId(), fromSummary.getId(), summary.getId(),
                                    summary.getId(), ApplicationCategoryEnum.edocRegister.ordinal());// 附件
                        }
                        if (edocRegister != null) {
                            // 登记上传的附件
                            this.attachmentManager.copy(edocRegister.getId(), edocRegister.getId(), summary.getId(),
                                    summary.getId(), ApplicationCategoryEnum.edocRegister.ordinal());
                        }

                        EdocOpinion senderOninion = new EdocOpinion();// 创建意见表对象
                        senderOninion.setContent(note);// 保存页面意见信息
                        senderOninion.setIdIfNew();// 创建新的ID
                        String trackMode = request.getParameter("isTrack");
                        boolean track = "1".equals(trackMode) ? true : false;
                        senderOninion.affairIsTrack = track;
                        senderOninion.setAttribute(1);
                        senderOninion.setIsHidden(false);
                        senderOninion.setCreateUserId(user.getId());
                        senderOninion.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        senderOninion.setPolicy(request.getParameter("policy"));
                        senderOninion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
                        senderOninion.setNodeId(0);
                        String comm = "new_form";
                        Long agentToId = null;
                        Map<String, Object> options = new HashMap<String, Object>();
                        // 获取页面附言
                        String additionalComment = note;
                        options.put("additionalComment", additionalComment);
                        EdocEnum.SendType sendType = EdocEnum.SendType.normal;
                        sendType = EdocEnum.SendType.normal;
                        long affairId = -1;
                        try {
                            if (summary.getDeadline() == -1L) {
                                // GOV-3414
                                // 【公文管理】-【收文管理】-【分发】-【待分发】，阅文批处理处理的收文，流程中人员收到的提示消息有误
                                summary.setDeadline(0L);
                            }

                            affairId = edocManager.runCase(summary, edocBody, senderOninion, sendType, options, comm,
                                    agentToId, false, process_xml, "", "");
                        } catch (Exception e) {
                            LOGGER.error("发起公文流程异常", e);
                        }
                        edocManager.setTrack(affairId, track, "");
                        // edocSummaryManager.saveOrUpdateEdocSummary(summary);
                    } catch (Exception e) {
                        LOGGER.error("//增加正文文件和v3x_file抛异常", e);
                    }
                }
                edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.Distributed.ordinal());// 改成已分发
                edocRegister.setDistributeEdocId(summary.getId());

                Date startTIme = summary.getStartTime();
                if (startTIme == null) {
                    edocRegister.setDistributeDate(new java.sql.Date(System.currentTimeMillis()));
                } else {
                    edocRegister.setDistributeDate(new java.sql.Date(startTIme.getTime()));
                }

                edocRegisterManager.update(edocRegister);// 修改登记表状态

                Object[][] wheres = new Object[3][2];
                wheres[0] = new Object[]{"app", ApplicationCategoryEnum.edocRecDistribute.getKey()};
                wheres[1] = new Object[]{"objectId", edocRegister.getId()};
                wheres[2] = new Object[]{"memberId", edocRegister.getDistributerId()};
                Map<String, Object> columns = new HashMap<String, Object>();
                columns.put("state", StateEnum.col_done.getKey());
                columns.put("subObjectId", summary.getId());
                columns.put("completeTime", new Date());
                columns.put("summaryState", summary.getState());
                affairManager.update(columns, wheres);
            }
        }

        String msg = notPassMsg.toString();
        if (!"".equals(msg)) {
            msg = msg.substring(0, msg.length() - 1);
            msg = ResourceUtil.getString("edoc.alert.form.wrongSet", msg, defaultEdocForm.getName());// 公文"+msg+"不符合公文单《"+defaultEdocForm.getName()+"》的必填项设置，请编辑后再进行分发。
            StringBuffer sb = new StringBuffer();
            sb.append("alert('" + StringEscapeUtils.escapeJavaScript(msg) + "');");
            sb.append(
                    "location.href = 'edocController.do?method=listDistribute&edocType=1&list=aistributining&btnType=2';");
            rendJavaScript(response, sb.toString());
            return null;
        }
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println(
                "window.parent.parent.location.href='edocController.do?method=listIndex&from=listSent&edocType=1&listType=listSent'");
        out.println("</script>");
        out.close();
        return null;
    }

    public ModelAndView readBatchRegist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        RegisterBody registerBody = null;
        EdocRegister edocRegister = null;
        EdocSummary summary = null;// 定义表单存储对象
        EdocBody edocBody = null;
        User user = AppContext.getCurrentUser();
        EdocForm defaultEdocForm = null; // 获取默认表单
        String note = request.getParameter("comment");// 获取页面附言信息
        String registerIdsStr = request.getParameter("registerStr");// 获取登记表ID
        String[] registerId = registerIdsStr.split("&");
        String process_xml = request.getParameter("processXml");
        defaultEdocForm = edocFormManager.getDefaultEdocForm(user.getLoginAccount(), 1);

        //StringBuilder notPassMsg = new StringBuilder();
        // 先生成登记记录**********************************
        EdocSummary fromSummary = null;
        // ##################################
        if (registerId != null) {// 判断页面是否选择，Js有验证，加一层错误提示保险
            for (int i = 0; i < registerId.length; i++) {// 批量处理需要办理的待发文单
                // 来文信息
                long recieveId = Long.parseLong(registerId[i]);// 遍历登记表ID
                edocRegister = createEdocRegister(recieveId);
                fromSummary = edocManager.getEdocSummaryById(edocRegister.getEdocId(), true);
                summary = new EdocSummary();// 建立新表单
                summary.bind(edocRegister);// 将登记表值赋给表单信息表
                summary.setSigningDate(edocRegister.getEdocDate());
                summary.setFormId(defaultEdocForm.getId());// 将默认表单ID放入表单对象中
                String docMark = summary.getDocMark();// 获取当前痕迹
                if (docMark != null) {
                    summary.setDocMark(docMark);
                }
                // 处理文号
                docMark = summary.getDocMark2();
                docMark = this.registDocMark(summary.getId(), docMark, 2, summary.getEdocType(), false,
                        EdocEnum.MarkType.edocMark.ordinal());
                if (docMark != null) {
                    summary.setDocMark2(docMark);
                }
                // 内部文号
                String serialNo = summary.getSerialNo();
                if (serialNo == null) {
                    summary.setSerialNo(this.registDocMark(summary.getId(), serialNo, 3, summary.getEdocType(), false,
                            EdocEnum.MarkType.edocInMark.ordinal()));
                }
                summary.setState(CollaborationEnum.flowState.run.ordinal());// 将此流程设为运行状态
                summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
                if (summary.getStartTime() == null) {
                    summary.setStartTime(new Timestamp(System.currentTimeMillis()));
                }
                summary.setStartUserId(user.getId());//
                V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                summary.setStartMember(member);
                if (summary.getOrgAccountId() == null) {
                    summary.setOrgAccountId(user.getLoginAccount());
                }
                summary.setOrgDepartmentId(getEdocOwnerDepartmentId(summary.getOrgAccountId(), null));
                summary.setProcessType(Long.valueOf("2"));

                Object[] objects = edocFormManager.getEdocFormElementRequiredMsg(defaultEdocForm, summary);
                //notPassMsg.append(((String) objects[1]));
                if (!(Boolean) objects[0]) {
                    continue;
                }

                // 装载公文正文

                registerBody = edocRegister.getRegisterBody();
                ;
                edocBody = new EdocBody();
                edocBody.setIdIfNew();
                if (edocBody.getCreateTime() == null) {
                    edocBody.setCreateTime(new Timestamp(System.currentTimeMillis()));
                }
                edocBody.setContentType(registerBody == null ? "HTML" : registerBody.getContentType());
                edocBody.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                if (summary != null && edocRegister != null && registerBody != null) {
                    // 增加正文文件和v3x_file
                    try {
                        if (registerBody.getContent() != null) {
                            if (!"HTML".equals(registerBody.getContentType())) {
                                // 复制正文及复制v3x_file表数据
                                V3XFile file = null;
                                try {
                                    file = fileManager.clone(Long.parseLong(registerBody.getContent()), true);
                                    file.setFilename(file.getId().toString());
                                    fileManager.update(file);
                                    edocBody.setContent(file.getId().toString());
                                } catch (Exception e) {
                                    LOGGER.error("复制公文登记正文错误！", e);
                                }
                                // 复制印章什么的？
                            }
                        }
                        // 增加附件
                        if (fromSummary != null) {
                            this.attachmentManager.copy(fromSummary.getId(), fromSummary.getId(), summary.getId(),
                                    summary.getId(), ApplicationCategoryEnum.edocRegister.ordinal());// 附件
                        }

                        EdocOpinion senderOninion = new EdocOpinion();// 创建意见表对象
                        senderOninion.setContent(note);// 保存页面意见信息
                        senderOninion.setIdIfNew();// 创建新的ID
                        String trackMode = request.getParameter("isTrack");
                        boolean track = "1".equals(trackMode) ? true : false;
                        senderOninion.affairIsTrack = track;
                        senderOninion.setAttribute(1);
                        senderOninion.setIsHidden(false);
                        senderOninion.setCreateUserId(user.getId());
                        senderOninion.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        senderOninion.setPolicy(request.getParameter("policy"));
                        senderOninion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
                        senderOninion.setNodeId(0);
                        String comm = "new_form";
                        Long agentToId = null;
                        Map<String, Object> options = new HashMap<String, Object>();
                        // 获取页面附言
                        String additionalComment = note;
                        options.put("additionalComment", additionalComment);
                        EdocEnum.SendType sendType = EdocEnum.SendType.normal;
                        sendType = EdocEnum.SendType.normal;
                        long affairId = -1;
                        try {
                            if (summary.getDeadline() == -1L) {
                                // GOV-3414
                                // 【公文管理】-【收文管理】-【分发】-【待分发】，阅文批处理处理的收文，流程中人员收到的提示消息有误
                                summary.setDeadline(0L);
                            }

                            affairId = edocManager.runCase(summary, edocBody, senderOninion, sendType, options, comm,
                                    agentToId, false, process_xml, "", "");
                        } catch (Exception e) {
                            LOGGER.error("发起公文流程异常", e);
                        }
                        edocManager.setTrack(affairId, track, "");
                        // edocSummaryManager.saveOrUpdateEdocSummary(summary);
                    } catch (Exception e) {
                        LOGGER.error("//增加正文文件和v3x_file抛异常", e);
                    }
                }
                edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.Distributed.ordinal());// 改成已分发
                edocRegister.setDistributeEdocId(summary.getId());

                Date startTIme = summary.getStartTime();
                if (startTIme == null) {
                    edocRegister.setDistributeDate(new java.sql.Date(System.currentTimeMillis()));
                } else {
                    edocRegister.setDistributeDate(new java.sql.Date(startTIme.getTime()));
                }

                edocRegister.setState(EdocNavigationEnum.RegisterState.Registed.ordinal());
                edocRegisterManager.update(edocRegister);// 修改登记表状态
                // 更新待登记记录
                recieveEdocManager.registerRecieveEdoc(recieveId, summary.getId());

                Object[][] wheres = new Object[3][2];
                wheres[0] = new Object[]{"app", ApplicationCategoryEnum.edocRecDistribute.getKey()};
                wheres[1] = new Object[]{"objectId", edocRegister.getId()};
                wheres[2] = new Object[]{"memberId", edocRegister.getDistributerId()};
                Map<String, Object> columns = new HashMap<String, Object>();
                columns.put("state", StateEnum.col_done.getKey());
                columns.put("subObjectId", summary.getId());
                columns.put("completeTime", new Date());
                columns.put("summaryState", summary.getState());
                affairManager.update(columns, wheres);
            }
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println(
                "window.parent.parent.location.href='edocController.do?method=listIndex&from=listSent&edocType=1&listType=listSent'");
        out.println("</script>");
        out.close();
        return null;
    }

    /**
     * 生成登记记录
     *
     * @param recieveId
     * @return
     * @throws BusinessException
     */
    private EdocRegister createEdocRegister(long recieveId) throws BusinessException {
        EdocRegister edocRegister = new EdocRegister();
        RegisterBody registerBody = null;
        User user = AppContext.getCurrentUser();
        List<Attachment> attachmentList = new ArrayList<Attachment>();
        EdocRecieveRecord record = null;

        // 签收单
        record = recieveEdocManager.getEdocRecieveRecord(recieveId);
        // 修复bug GOV-3204 公文管理-首页的BUG
        /**
         * 登记公文，判断当前操作人是否可以登记此公文
         */
        // 指定登记人
        long registerUserId = record == null ? user.getId() : record.getRegisterUserId();
        // 修复bug GOV-3204 公文管理-首页的BUG
        // 当公文已经登记后，再待分发中退回时，并没有对签收表中的状态进行改变，而且affair的状态为3,表示待处理的状态，因此首页能够显示出它属于

        // 装载数据到登记表
        edocRegister.setIdIfNew();
        /*
         * if(agentToId != -1){ edocRegister.setCreateUserId(agentToId);
         * edocRegister.setCreateUserName(orgManager.getMemberById(agentToId).
         * getName()); }else{
         */
        edocRegister.setCreateUserId(user.getId());
        edocRegister.setCreateUserName(user.getName());
        // }
        edocRegister.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
        edocRegister.setUpdateTime(new java.sql.Timestamp(new Date().getTime()));
        edocRegister.setOrgAccountId(user.getAccountId());
        edocRegister.setState(EdocNavigationEnum.RegisterState.WaitRegister.ordinal());
        // 设置登记人
        edocRegister.setRegisterUserId(registerUserId);
        V3xOrgMember v3xOrgMember = orgManager.getMemberById(registerUserId);
        edocRegister.setRegisterUserName(v3xOrgMember == null ? user.getName() : v3xOrgMember.getName());
        // 设置默认分发人
        if (registerUserId != user.getId()) {
            edocRegister.setDistributerId(registerUserId);
            edocRegister.setDistributer(orgManager.getMemberById(registerUserId).getName());
        } else {
            edocRegister.setDistributerId(user.getId());
            edocRegister.setDistributer(user.getName());
        }
        edocRegister.setRegisterType(EdocNavigationEnum.RegisterType.ByAutomatic.ordinal());// 电子公文
        edocRegister.setRegisterDate(new java.sql.Date(new Date().getTime()));
        // 来文信息
        edocRegister.setRecieveId(recieveId);
        Long edocId = null;
        if (record != null) {
            record.getSendUnit();
            edocId = record.getEdocId();
            edocRegister.setEdocId(edocId);
        }
        // 从EdocSummary对象中取的数据
        EdocSummary summary = edocManager.getEdocSummaryById(edocId, true);
        Long sendUnitId = -1L;
        if (summary != null) {
            sendUnitId = summary.getOrgAccountId();
        }

        V3xOrgAccount account = orgManager.getAccountById(sendUnitId);
        edocRegister.setSendUnit(account == null ? "" : account.getName());
        edocRegister.setSendUnitId(sendUnitId);
        edocRegister.setSendUnitType(record == null ? 1 : record.getSendUnitType());
        edocRegister.setEdocType(EdocEnum.edocType.recEdoc.ordinal());
        edocRegister.setDocType(record == null ? "1" : record.getDocType());
        edocRegister.setSubject(record == null ? "" : record.getSubject());
        edocRegister.setDocMark(record == null ? "" : record.getDocMark());
        edocRegister.setSecretLevel(record == null ? "1" : record.getSecretLevel());
        edocRegister.setUrgentLevel(record == null ? "1" : record.getUrgentLevel());
        // 公文元素基本信息
        edocRegister.setKeepPeriod(summary == null ? record == null ? "" : record.getKeepPeriod()
                : String.valueOf(summary.getKeepPeriod()));
        edocRegister.setSendType(summary == null ? "1" : summary.getSendType());
        edocRegister.setKeywords(summary == null ? "" : summary.getKeywords());
        edocRegister.setIssuerId(-1l);
        edocRegister.setIssuer(summary == null ? "" : summary.getIssuer());
        edocRegister.setEdocDate(summary == null ? null : summary.getSigningDate());
        // 增加主送单位
        // GOV-4693.公文管理-公文登记时，抄送单位，抄报单位，印发单位等都在主送单位框内显示 start
        String sendTo = summary == null ? "" : summary.getSendTo();
        String recordSendTo = record == null ? "" : record.getSendTo();
        String copyTo = summary == null ? "" : summary.getCopyTo();
        String recordCopyTo = record == null ? "" : record.getCopyTo();
        edocRegister.setSendTo(Strings.isBlank(sendTo) ? recordSendTo : sendTo);
        edocRegister.setSendToId(summary == null ? "" : summary.getSendToId());
        edocRegister.setCopyTo(Strings.isBlank(copyTo) ? recordCopyTo : copyTo);
        edocRegister.setCopyToId(summary == null ? "" : summary.getCopyToId());
        // GOV-4693.公文管理-公文登记时，抄送单位，抄报单位，印发单位等都在主送单位框内显示 end

        // 如果来文流程中有会签节点，则设置会签节点处理人
        if (summary != null) {
            List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), StateEnum.col_done);
            if (affairs != null && affairs.size() > 0) {
                for (CtpAffair aff : affairs) {
                    if ("huiqian".equals(aff.getNodePolicy())) {
                        V3xOrgMember member = orgManager.getMemberById(aff.getMemberId());
                        edocRegister.setSigner(member.getName());
                        break;
                    }
                }
            }
        }

        List<Attachment> attachmentListCopy = new ArrayList<Attachment>();
        // 附件信息
        if (summary != null) {
            attachmentList = attachmentManager.getByReference(summary.getId(), summary.getId());

            // 只要拟文添加和处理修改添加的附件，不要处理时填意见所添加的附件
            for (Attachment att : attachmentList) {
                // type=0表示附件 (附件元素上不显示关联文档)
                if (att.getType() == 0 && att.getSubReference().longValue() == summary.getId()) {
                    attachmentListCopy.add(att);
                }
            }

        }
        edocRegister.setIdentifier("00000000000000000000");
        edocRegister.setAttachmentList(attachmentListCopy);
        edocRegister.setHasAttachments(attachmentListCopy.size() > 0);
        // 装载公文正文
        if (summary != null && record != null) {
            EdocBody edocBody = summary.getBody(record.getContentNo().intValue());
            registerBody = new RegisterBody();
            edocBody = edocBody == null ? summary.getFirstBody() : edocBody;
            if (null != edocBody) {
                registerBody.setId(-1L);
                registerBody.setContent(edocBody.getContent());
                registerBody.setContentNo(edocBody.getContentNo());
                registerBody.setContentType(edocBody.getContentType());
                registerBody.setCreateTime(edocBody.getLastUpdate());
                edocRegister.setRegisterBody(registerBody);
            }
        } else {
            registerBody = new RegisterBody();
            String bodyContentType = Constants.EDITOR_TYPE_OFFICE_WORD;
            if (com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx") == false) {
                bodyContentType = Constants.EDITOR_TYPE_HTML;
            }
            registerBody.setId(-1L);
            registerBody.setContentType(bodyContentType);
            registerBody.setCreateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
            edocRegister.setRegisterBody(registerBody);
        }
        if (summary != null && !Strings.isBlank(summary.getSendUnit()) && !Strings.isBlank(summary.getSendUnitId())) {
            edocRegister.setEdocUnit(summary.getSendUnit());
            edocRegister.setEdocUnitId(summary.getSendUnitId());
        } else {// lijl添加else，如果来文单位为空,则取record里的来文单位
            edocRegister.setEdocUnit(record == null ? "" : record.getSendUnit());
        }

        edocRegister.setSerialNo("");
        // 保存登记对象
        edocRegisterManager.createEdocRegister(edocRegister);
        // 应用日志
        appLogManager.insertLog(user, AppLogAction.Edoc_RegEdoc, user.getName(), edocRegister.getSubject());
        return edocRegister;
    }

    /**
     * 增加全文检索记录
     *
     * @param hasArchive
     * @param affairId
     */
    private void add2Index(int edocType, Long affairId) {

        if (AppContext.hasPlugin("index")) {
            try {
                indexManager.add(affairId, ApplicationCategoryEnum.edoc.getKey());
            } catch (BusinessException e) {
                LOGGER.error("公文增加全文检索抛出异常：", e);
            }
        }
    }

    public ModelAndView sendImmediate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String[] _summaryIds = request.getParameterValues("id");
        String[] _affairIds = request.getParameterValues("affairId");
        String edocType = request.getParameter("edocType");
        // json格式的字符串:选人
        String popNodeSelected = request.getParameter("popNodeSelected");
        // json格式的字符串:条件分支
        String popNodeCondition = request.getParameter("popNodeCondition");
        // json格式的字符串:新流程信息
        String popNodeNewFlow = request.getParameter("popNodeNewFlow");

        // 是否来自于首页二级栏目待发更多列表的直接发送,clickFrom=portal
        String clickFrom = request.getParameter("clickFrom");
        String successUrl = ""; // 处理成功，返回的url
        String failUrl = ""; // 处理失败，发回的url
        if (Strings.isNotBlank(clickFrom) && "portal".equals(clickFrom)) { // 来自首页待发更多
            _summaryIds = new String[]{request.getParameter("summaryId")};
            _affairIds = new String[]{request.getParameter("affairId")};
            String fragmentId = request.getParameter("fragmentId");
            String ordinal = request.getParameter("ordinal");
            String rowStr = request.getParameter("rowStr");
            String app = request.getParameter("app");
            String columnsName = request.getParameter("columnsName");
            // int portalState=StateEnum.col_waitSend.key();
            // boolean portalIsTrack=false;
            String s = URLEncoder.encode(columnsName, "UTF-8");
            successUrl = "location.href ='portalAffair/portalAffairController.do?method=moreWaitSend&fragmentId=" + fragmentId
                    + "&ordinal=" + ordinal + "&rowStr=" + rowStr + "&columnsName=" + s + Functions.csrfSuffix() + "';";
            failUrl = successUrl;
            if ("19".equals(app)) {
                successUrl = "location.href ='edocController.do?method=entryManager&entry=sendManager&listType=listSent" + Functions.csrfSuffix() + "';";
            } else if ("20".equals(app)) {
                successUrl = "location.href ='edocController.do?method=entryManager&entry=recManager&listType=listSent" + Functions.csrfSuffix() + "';";
            } else if ("21".equals(app)) {
                successUrl = "location.href ='edocController.do?method=entryManager&entry=signReport&listType=listSent" + Functions.csrfSuffix() + "';";
            }
        } else { // 公文待发列表
            successUrl = "parent.location.href ='edocController.do?method=listIndex&edocType=" + edocType
                    + "&listType=listSent" + Functions.csrfSuffix() + "';";
            failUrl = "location.href = 'edocController.do?method=edocFrame&edocType=" + edocType
                    + "&listType=listWaitSend" + Functions.csrfSuffix() + "';";
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("popNodeSelected:=" + Strings.toHTML(popNodeSelected));
            LOGGER.info("popNodeCondition:=" + Strings.toHTML(popNodeCondition));
            LOGGER.info("popNodeNewFlow:=" + Strings.toHTML(popNodeNewFlow));
        }
        // String subType =
        // Strings.isBlank(request.getParameter("subType"))?"":request.getParameter("subType");
        StringBuilder message = new StringBuilder();
        // StringBuilder msgErr = new StringBuilder();
        // boolean sentFlag = false;
        StringBuffer sb = new StringBuffer();
        StringBuilder notPassMsg = new StringBuilder();

        for (int i = 0; i < _summaryIds.length; i++) {
            Long summaryId = Long.valueOf(_summaryIds[i]);

            Long lockUserId = null;


            lockUserId = edocLockManager.canGetLock(summaryId, user.getId());
            if (lockUserId != null) {
                LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,summaryId" + summaryId);
                return null;
            }


            EdocSummary edocSummary = edocManager.getEdocSummaryById(summaryId, false);
            if (edocSummary == null) {
                return null;
            } else {
                //防止使用工具重复请求
                if (edocSummary.getState() == CollaborationEnum.flowState.finish.ordinal()) {
                    //快速发文或者发送后就结束的
                    return null;
                }

                CtpAffair affair = affairManager.getSenderAffair(edocSummary.getId());
                if (affair == null || affair.getState() == StateEnum.col_sent.getKey()) {
                    return null;
                }
            }


            try {
                // 直接收文时，自动登记
                if ("1".equals(edocType)) {
                    EdocRegister edocRegister = edocRegisterManager.findRegisterByDistributeEdocId(summaryId);
                    if (edocRegister != null && !EdocHelper.isG6Version()) {

                        // A8收文待办退回到待发时，会修改register数据的IsRetreat为1，表示退回的，直接发送时需要改回0
                        edocRegister.setIsRetreat(EdocNavigationEnum.RegisterRetreatState.NotRetreat.ordinal());
                        edocRegisterManager.update(edocRegister);

                        // A8电子登记收文保存待发后，签收status状态为10(登记保存待发)，直接发送需要将签收状态改为已登记
                        EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(summaryId);
                        if (record != null) {
                            record.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
                            recieveEdocManager.update(record);
                        }
                    }
                }

                int edocTypeRole = edocSummary.getEdocType();
                if (edocTypeRole == EdocEnum.edocType.recEdoc.ordinal() && EdocHelper.isG6Version()) { // 政务版判断收文分发权限
                    edocTypeRole = EdocEnum.edocType.distributeEdoc.ordinal();
                }
                boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                        edocTypeRole);
                // 没有发起权
                if (!isEdocCreateRole) {
                    String errMsg1 = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "alert_not_edocCreatePower", edocSummary.getSubject());
                    message.append(errMsg1).append("\n");
                    continue;
                }

                // 调用了模板
                if (edocSummary.getTempleteId() != null) {
                    String errMsg2 = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "alert_not_edocHaveTemplate", edocSummary.getSubject());
                    message.append(errMsg2).append("\n");
                    continue;
                }

                // 快速发文--start
                boolean isQuickSend = edocSummary.getIsQuickSend() == null ? false : edocSummary.getIsQuickSend();
                EdocSummaryQuick edocSummaryQuick = null;
                if (isQuickSend) {
                    edocSummaryQuick = edocSummaryQuickManager.findBySummaryId(edocSummary.getId());

                }

                //zhou
//                EdocBody body=edocSummary.getBody(0);
//                if (isQuickSend == false) {
//                    int flag = transitionPdf(edocSummary, body);
//                    if (flag == -1) {
//                        StringBuffer sbuffer = new StringBuffer();
//                        sbuffer.append("alert('" + StringEscapeUtils.escapeJavaScript("转换服务出错了，请联系管理员") + "');");
//                        sbuffer.append("history.back();");
//                        rendJavaScript(response, sbuffer.toString());
//                        return null;
//                    }
//                }
                // 快速发文--end

                String processId = edocSummary.getProcessId();

                // 没有流程
                if (!isQuickSend && (processId == null || "".equals(processId.trim()))) {
                    String errMsg3 = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "alert_not_edocHaveWorkFlow", edocSummary.getSubject());
                    message.append(errMsg3).append("\n");
                    continue;
                }

                EdocForm edocForm = edocFormManager.getEdocForm(edocSummary.getFormId());
                Object[] objects = edocFormManager.getEdocFormElementRequiredMsg(edocForm, edocSummary);
                notPassMsg.append(((String) objects[1]));
                if (!(Boolean) objects[0]) {
                    continue;
                }

                if (edocSummary.getOrgAccountId() == null) {
                    edocSummary.setOrgAccountId(user.getLoginAccount());
                }
                edocSummary.setOrgDepartmentId(getEdocOwnerDepartmentId(edocSummary.getOrgAccountId(), null));

                if (isQuickSend) {// 快速发文后，summary的状态为结束。
                    edocSummary.setState(CollaborationEnum.flowState.finish.ordinal());
                    edocSummary.setFinished(true);
                    // edocSummary.setHasArchive(hasArchive) TODO
                    edocSummary.setCompleteTime(new Timestamp(System.currentTimeMillis()));
                    edocSummary.setCanTrack(0);
                    edocSummary.setDeadline(0L);
                    edocSummary.setCaseId(0L);
                    edocSummary.setProcessId("0");
                    edocSummary.setDeadlineDatetime(null);
                } else {
                    edocSummary.setState(CollaborationEnum.flowState.run.ordinal());
                }
                // OA-68915 签报拟文时文单上没有发文部门元素，发送后没有保存发文部门
                setEdocDefaultSendInfo(edocSummary, user, "0,1");
                if (null != edocSummary.getTempleteId()) {
                    CtpTemplate _curTemplate = templeteManager.getCtpTemplate(edocSummary.getTempleteId());
                    if (null != _curTemplate && !_curTemplate.isSystem() && null == _curTemplate.getFormParentid()) {
                        edocSummary.setTempleteId(null);
                    }
                }

                // 收文登记时间更新
                if (edocSummary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC) {// 收文
                    // 设置登记时间(注意：若是指定回退流程重走，则取之前的登记日期)
                    java.sql.Date oldDate = edocSummary.getRegistrationDate();
                    edocSummary.setRegistrationDate(new java.sql.Date(System.currentTimeMillis()));
                    List<CtpAffair> affairs = affairManager.getAffairs(edocSummary.getId(), StateEnum.col_waitSend);
                    if (Strings.isNotEmpty(affairs)) {
                        for (CtpAffair affair : affairs) {
                            if (affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.key()
                                    || affair.getSubState() == SubStateEnum.col_pending_specialBacked.key()) {
                                edocSummary.setRegistrationDate(oldDate);
                                break;
                            }
                        }
                    }
                }

                edocManager.update(edocSummary);

                // (5.0sprint3)-FIXED(changyi)
                edocManager.sendImmediate(Long.parseLong(_affairIds[i]), edocSummary, isQuickSend);
                // sentFlag = true;

                // ****快速发文start***
                if (isQuickSend) {
                    CtpAffair a = affairManager.get(Long.parseLong(_affairIds[i]));
                    // 发文拟文才有交换
                    if (edocSummary.getEdocType() == EdocEnum.edocType.sendEdoc.ordinal()) {
                        int edocExchangeType = edocSummaryQuick.getExchangeType();
                        Long edocMangerID = edocSummaryQuick.getExchangeAccountMemberId();
                        // 封发的时候进行相关的问号操作，移动到历史表中。tdbug28578 以封发节点完成提交，作为流程结束标志。
                        edocMarkHistoryManager.afterSend(edocSummary);
                        // 这不知道啥玩意，处理时封发有的代码，先挪过来 TODO
                        if (edocSummary.getPackTime() == null) {
                            edocSummary.setPackTime(new Timestamp(System.currentTimeMillis()));
                        }

                        Long unitId = -1L;
                        if (edocExchangeType != -1) {
                            if (edocExchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Dept) {// 部门交换
                                if (edocMangerID == null) {
                                    unitId = edocSummary.getOrgDepartmentId();
                                } else {// 发起人存在副岗时，选择交换部门
                                    unitId = edocMangerID;
                                }
                            } else if (edocExchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org) {// 单位交换
                                unitId = edocSummary.getOrgAccountId();
                            }
                            try {
                                sendEdocManager.create(edocSummary, unitId, edocExchangeType,
                                        edocMangerID == null ? null : String.valueOf(edocMangerID), a, false);
                                // 更新公文统计表为封发
                                edocStatManager.setSeal(edocSummary.getId());
                            } catch (Exception e) {
                                LOGGER.error("生成公文统计表错误", e);
                            }
                        }
                    }

                    try {
                        // 快速发文——归档。start
                        if (edocSummary.getEdocType() == 0 || edocSummary.getEdocType() == 1) { // 发文
                            if (edocSummary.getArchiveId() != null && !edocSummary.getHasArchive()) {
                                edocManager.pigeonholeAffair("", a, edocSummary.getId(), edocSummary.getArchiveId(), false);
                            }
                        }
                        // 快速发文——归档。end
                    } catch (Exception e) {
                        LOGGER.error("快速发文归档错误", e);
                    }

                }
                // ****快速发文 end***
                // 全文检索
                add2Index(edocSummary.getEdocType(), edocSummary.getId());

                // 更新当前待办人信息
                EdocHelper.updateCurrentNodesInfo(edocSummary, true);
                // 是否更新发文关联收文的 recAffairId
                isUpdateRecRelation(edocSummary);
            } finally {
                edocLockManager.unlock(summaryId);
            }
        }

        // ModelAndView mv =
        // super.redirectModelAndView("/edocController.do?method=edocFrame&listType=listSent&edocType="+edocType);
        if (message.length() > 0) {
            /*
             * WebUtil.saveAlert("alert.sendImmediate.nowf",
             * message.toString()); mv = super.redirectModelAndView(
             * "/edocController.do?method=edocFrame&listType=listWaitSent&edocType="
             * +edocType);
             */
            sb.append("alert('" + StringEscapeUtils.escapeJavaScript(message.toString()) + "');");
            sb.append(failUrl);

            rendJavaScript(response, sb.toString());
            return null;
        } else {
            String msg = notPassMsg.toString();
            if (!"".equals(msg)) {
                msg = msg.substring(0, msg.length() - 1);
                msg = ResourceUtil.getString("edoc.alert.form.hasRequiredField", msg);// 公文"+msg+"存在必填项，请编辑后发送。
                sb.append("alert('" + StringEscapeUtils.escapeJavaScript(msg) + "');");
                sb.append(failUrl);
                rendJavaScript(response, sb.toString());
                return null;
            } else {
                sb.append(successUrl);
                rendJavaScript(response, sb.toString());
                return null;
            }
        }

    }

    /**
     * 审核公文时，是否修改过文单
     *
     * @param s1
     * @param s2
     * @return
     */
    private boolean isChangeEdocForm(EdocSummary s1, EdocSummary s2) {
        boolean flag = false;
        Method[] methods = EdocSummary.class.getDeclaredMethods();
        for (Method method : methods) {
            Class<?>[] p = method.getParameterTypes();
            int parameterCount = p.length;
            // 只比较get方法，并且其中关于时间的都不比较
            if (method.getName().startsWith("get") && !method.getName().endsWith("Time")
                    && !method.getName().endsWith("Date") && parameterCount == 0) {

                Object value1 = null;
                Object value2 = null;
                try {
                    value1 = method.invoke(s1);
                    value2 = method.invoke(s2);
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

                if (value1 != null && value2 != null && !value1.equals(value2)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /* 修改文单后，保存修改的数据 */
    public ModelAndView updateFormData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Boolean ret = false;
        User user = AppContext.getCurrentUser();
        long summaryId = Long.parseLong(request.getParameter("summaryId"));
        EdocSummary edocSummary = edocManager.getEdocSummaryById(summaryId, false);
        Long affairId = Long.valueOf(request.getParameter("affairId"));
        CtpAffair affair = affairManager.get(affairId);
        long formId = Long.parseLong(request.getParameter("edoctable"));
        try {
            // 克隆一个对象，以和改变后的相比较
            EdocSummary copyEdocSummary = (EdocSummary) edocSummary.clone();

            String mark = request.getParameter("my:doc_mark");
            if (Strings.isNotBlank(mark)) {
                mark = mark.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
                EdocMarkModel em = EdocMarkModel.parse(mark);
                if (em != null) {
                    mark = em.getMark(); // 需要保存到数据库中的公文文号
                }
            }

            String mark2 = request.getParameter("my:doc_mark2");
            if (Strings.isNotBlank(mark2)) {
                mark2 = mark2.replaceAll(String.valueOf((char) 160), String.valueOf((char) 32));
                EdocMarkModel em = EdocMarkModel.parse(mark2);
                if (em != null) {
                    mark2 = em.getMark(); // 需要保存到数据库中的公文文号
                }
            }

            boolean updateDocMark = false;
            if (Strings.isNotBlank(edocSummary.getDocMark()) && mark != null && !edocSummary.getDocMark().equals(mark)) {
                updateDocMark = true;
            }
            boolean updateDocMark2 = false;
            if (Strings.isNotBlank(edocSummary.getDocMark2()) && mark2 != null && !edocSummary.getDocMark2().equals(mark2)) {
                updateDocMark2 = true;
            }

            if (edocSummary.getEdocType() == 2 && (updateDocMark || updateDocMark2)) {
                edocMarkManager.edocMarkCategoryRollBack(edocSummary);
            }

            // OA-17655 拟文时设置了跟踪，发送后被回退，在待发中编辑，进入到拟文页面，跟踪被取消了。应该保留原来的设置。
            int isTrack = edocSummary.getCanTrack();
            DataUtil.requestToSummary(request, edocSummary, formId);
            edocSummary.setCanTrack(isTrack);

            // 如果公文文号为空，不做任何处理
            String docMark = edocSummary.getDocMark();
            if (docMark != null && !"".equals(docMark)) {
                try {
                    docMark = this.registDocMark(edocSummary.getId(), docMark, 1, edocSummary.getEdocType(), true,
                            EdocEnum.MarkType.edocMark.ordinal());
                } catch (EdocMarkHistoryExistException e) {
                    response.setContentType("text/html;charset=UTF-8");
                    String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "edoc.docmark.exist");
                    response.getWriter().print("result=historyMarkExist:" + errMsg);
                    return null;
                }
                if (docMark != null) {
                    edocSummary.setDocMark(docMark);
                }
            }
            // 处理第二个公文文号
            docMark = edocSummary.getDocMark2();
            if (docMark != null && !"".equals(docMark)) {
                docMark = this.registDocMark(edocSummary.getId(), docMark, 2, edocSummary.getEdocType(), true,
                        EdocEnum.MarkType.edocMark.ordinal());
                if (docMark != null) {
                    edocSummary.setDocMark2(docMark);
                }
            }

            // 处理内部文号
            String serialNo = edocSummary.getSerialNo();
            if (serialNo != null && !"".equals(serialNo)) {
                serialNo = this.registDocMark(edocSummary.getId(), serialNo, 3, edocSummary.getEdocType(), true,
                        EdocEnum.MarkType.edocInMark.ordinal());
                if (Strings.isNotBlank(serialNo)) {
                    edocSummary.setSerialNo(serialNo);
                }
            }

            String isOnlyModifyWordNo = request.getParameter("isOnlyModifyWordNo");
            if ("true".equals(isOnlyModifyWordNo)) {
                this.processLogManager.insertLog(user, Long.valueOf(edocSummary.getProcessId()), affair.getActivityId(),
                        ProcessLogAction.processEdoc,
                        String.valueOf(ProcessLogAction.ProcessEdocAction.modifyWordNo.getKey()));
            }
            edocManager.update(edocSummary, true);

            // 判断是否修改了文单
            if (isChangeEdocForm(edocSummary, copyEdocSummary)) {
                // OA-34156
                // 应用检查：待办节点有效果文单的权限，处理后查看日志记录，一直会有：修改文单，很久之前报了，但是关了，需求要求可以挂起这个bug作为再放开修改的bug
                if (!"true".equals(isOnlyModifyWordNo)) {
                    this.processLogManager.insertLog(user, Long.valueOf(edocSummary.getProcessId()),
                            affair.getActivityId(), ProcessLogAction.processEdoc,
                            String.valueOf(ProcessLogAction.ProcessEdocAction.modifyForm.getKey()));
                }
                appLogManager.insertLog(user, AppLogAction.Edoc_Form_update, user.getName(), affair.getSubject());

                //如果修改前后的文号不一致，记录一条应用日志
                if ((Strings.isNotBlank(copyEdocSummary.getDocMark()) && !copyEdocSummary.getDocMark().equals(edocSummary.getDocMark())) ||
                        (Strings.isNotBlank(edocSummary.getDocMark()) && !edocSummary.getDocMark().equals(copyEdocSummary.getDocMark()))) {

                    appLogManager.insertLog(user, AppLogAction.Edoc_Doc_Mark_Update, user.getName(), edocSummary.getSubject(), copyEdocSummary.getDocMark(), edocSummary.getDocMark());
                }
                if ((Strings.isNotBlank(copyEdocSummary.getSerialNo()) && !copyEdocSummary.getSerialNo().equals(edocSummary.getSerialNo())) ||
                        (Strings.isNotBlank(edocSummary.getSerialNo()) && !edocSummary.getSerialNo().equals(copyEdocSummary.getSerialNo()))) {
                    appLogManager.insertLog(user, AppLogAction.Edoc_Serial_No_Update, user.getName(), edocSummary.getSubject(), copyEdocSummary.getSerialNo(), edocSummary.getSerialNo());
                }
            }

            // 暂存待办时更新affair中的标题和紧急程度
            Map<String, Object> columns = new HashMap<String, Object>(2);
            columns.put("subject", edocSummary.getSubject());
            if (!Strings.isEmpty(edocSummary.getUrgentLevel())) {
                columns.put("importantLevel", Integer.parseInt(edocSummary.getUrgentLevel()));
            }
            // 首页栏目的扩展字段设置--公文文号、发文单位等--start
            Map<String, Object> extParam = new HashMap<String, Object>();
            extParam.put(AffairExtPropEnums.edoc_edocMark.name(), edocSummary.getDocMark()); // 公文文号
            extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), edocSummary.getSendUnit());// 发文单位ID
            // OA-43885 首页待办栏目下，待开会议的主持人名字改变后，仍显示之前的名称
            extParam.put(AffairExtPropEnums.edoc_sendAccountId.name(), edocSummary.getSendUnitId());// 发文单位ID
            AffairUtil.setExtProperty(affair, extParam);
            columns.put("extProps", affair.getExtProps());
            // 首页栏目的扩展字段设置--公文文号、发文单位等--end
            Object[][] wheres = {{"objectId", edocSummary.getId()}};
            this.affairManager.update(columns, wheres);
            ret = true;
            if (AppContext.hasPlugin("doc")) {
                docApi.updatePigehole(user.getId(), edocSummary.getId(), ApplicationCategoryEnum.edoc.getKey());
            }
        } catch (Exception e) {
            LOGGER.error("修改文单后，保存修改的数据抛出异常：", e);
        }
        response.getWriter().print("result=" + ret);
        return null;
    }

    /**
     * 当电子收文保存时，文单中没有来文部门时，依然需要保存 主要解决文单中没有来文部门保存后，待发编辑切换到有来文部门的文单时，来文部门显示错了
     *
     * @param summary
     * @param edocRegister
     * @param sendDepartment
     * @throws Exception
     */
    private void saveSendEdocDepartment(EdocSummary summary, EdocRegister edocRegister, String sendDepartment)
            throws Exception {
        if (summary.getEdocType() == 1 && edocRegister != null && edocRegister.getRegisterType() == 1
                && Strings.isBlank(sendDepartment)) {
            EdocSummary sendSummary = edocManager.getEdocSummaryById(edocRegister.getEdocId(), false);
            if (sendSummary != null) {
                summary.setSendDepartment(sendSummary.getSendDepartment());
            }
        }
    }

    /**
     * 保存待发
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String backBoxToEdit = request.getParameter("backBoxToEdit");
        // 代理信息
        boolean isAgent = false;
        Long agentToId = null;
        String _isAgent = request.getParameter("isAgent");
        String _agentToId = request.getParameter("agentToId");
        String _agentToName = request.getParameter("agentToName");
        if (Strings.isNotBlank(_agentToId)) {
            agentToId = Long.valueOf(_agentToId);
        }
        if ((_isAgent != null && "true".equals(_isAgent)) || agentToId != null) {
            isAgent = true;
        }

        // 待登记关联发文时，关联id用签收id
        String recieveId = request.getParameter("recieveId");
        String edocType = request.getParameter("edocType");

        EdocRegister edocRegister = null;
        long registerId = Strings.isBlank(request.getParameter("registerId")) ? -1
                : Long.parseLong(request.getParameter("registerId"));
        String comm = request.getParameter("comm");

        EdocSummary summary = new EdocSummary();
        bind(request, summary);

        if ("1".equals(edocType)) {
            if (registerId != -1) {
                edocRegister = edocRegisterManager.findRegisterById(registerId);
            } else if (Strings.isNotBlank(recieveId)) {
                edocRegister = edocRegisterManager.findRegisterByRecieveId(Long.parseLong(recieveId));
            }

            if (edocRegister != null) {
                summary.setSendTo(edocRegister.getSendTo());
            }

            Map<String, Object> msgMap = checkSendRecEdoc(edocRegister, registerId, recieveId,
                    request.getParameter("app"), user, backBoxToEdit);
            String errMessage = (String) msgMap.get("errMsg");
            if (Strings.isNotBlank(errMessage)) {
                if ("redirect".equals(errMessage)) {
                    ModelAndView modelAndView = new ModelAndView("common/redirect");
                    String errMsg = ResourceBundleUtil
                            .getString("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource", "edoc.distributed");
                    modelAndView.addObject("redirectURL", BaseController.REDIRECT_BACK);
                    modelAndView.addObject("errMsg", errMsg);
                    return modelAndView;
                } else {
                    rendJavaScript(response, errMessage);
                    return null;
                }
            }
            if (msgMap.containsKey("agentToId")) {
                agentToId = (Long) msgMap.get("agentToId");
            }
        }
        String lockDocMarkSubject = "";
        String lockDocMark = "";
        try {
            // ***快速发文相关变量 start***
            boolean isQuickSend = false; // 快速发文标识
            isQuickSend = summary.getIsQuickSend() == null ? false : summary.getIsQuickSend();
            // ***快速发文相关变量 end***

            String deadlineDatetime = (String) request.getParameter("deadLineDateTime");

            if (!isQuickSend && Strings.isNotBlank(deadlineDatetime)) {
                summary.setDeadlineDatetime(DateUtil.parse(deadlineDatetime, "yyyy-MM-dd HH:mm"));
            }

            // 新建公文页面流程期限这里加一个隐藏域，后台保存的是这里的值，因为如果从模板加载设了流程期限的话，就disabled了，后台就取不到值了
            String deadline2 = request.getParameter("deadline2");
            if (!isQuickSend && Strings.isNotBlank(deadline2)) {
                summary.setDeadline(Long.parseLong(deadline2));
            }
            String advanceRemind2 = request.getParameter("advanceRemind2");
            if (!isQuickSend && Strings.isNotBlank(advanceRemind2)) {
                summary.setAdvanceRemind(Long.parseLong(advanceRemind2));
            }

            if (summary.isNew()) {
                summary.setId(Long.parseLong(request.getParameter("summaryId")));
            }
            boolean isNew = summary.isNew();
            summary.setIdIfNew();
            long formId = Long.parseLong(request.getParameter("edoctable"));

            DataUtil.requestToSummary(request, summary, formId);

            // 当电子收文保存时，文单中没有来文部门时，依然需要保存
            saveSendEdocDepartment(summary, edocRegister, request.getParameter("my:send_department"));

            // OA-32875 系统管理员-枚举管理，公文类型、行文类型枚举引用之后显示为否（注意查看一下OA-29865中的修改方法）
            // 更新系统枚举引用
            EdocHelper.updateEnumItemRef(request);

            // 将公文流水号（内部文号）自动增1
            // add by handy,2007-10-16
            // String serialNo =
            // edocInnerMarkDefinitionManager.getInnerMark(summary.getEdocType(),
            // user.getAccountId(), true);
            // summary.setSerialNo(serialNo);

            // 处理公文文号
            // 如果公文文号为空，不做任何处理
            String docMark = summary.getDocMark();
            if (Strings.isNotBlank(summary.getDocMark()) && summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                lockDocMarkSubject = edocMarkLockManager.canGetLock(summary.getDocMark(), summary.getSubject());
                lockDocMark = summary.getDocMark();
                if (Strings.isNotBlank(lockDocMarkSubject)) {
                    EdocMarkModel mark = EdocMarkModel.parse(summary.getDocMark());
                    String errMsg = "公文《" + lockDocMarkSubject + "》占用了文号《" + mark.getMark() + "》,请刷新重试";
                    StringBuffer str = new StringBuffer("");
                    str.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
                    str.append("history.back();");
                    rendJavaScript(response, str.toString());
                    return null;
                }
            }
            if (Strings.isNotBlank(summary.getSerialNo()) && summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                lockDocMarkSubject = edocMarkLockManager.canGetLock(summary.getSerialNo(), summary.getSubject());
                lockDocMark = summary.getSerialNo();
                if (Strings.isNotBlank(lockDocMarkSubject)) {
                    EdocMarkModel serialNo = EdocMarkModel.parse(summary.getSerialNo());
                    String errMsg = "公文《" + lockDocMarkSubject + "》占用了文号《" + serialNo.getMark() + "》,请刷新重试";
                    StringBuffer str = new StringBuffer("");
                    str.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
                    str.append("history.back();");
                    rendJavaScript(response, str.toString());
                    return null;
                }
            }
            if (Strings.isNotBlank(summary.getDocMark2()) && summary.getEdocType() == com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SIGN) {
                lockDocMarkSubject = edocMarkLockManager.canGetLock(summary.getDocMark2(), summary.getSubject());
                lockDocMark = summary.getDocMark2();
                if (Strings.isNotBlank(lockDocMarkSubject)) {
                    EdocMarkModel mark2 = EdocMarkModel.parse(summary.getDocMark2());
                    String errMsg = "公文《" + lockDocMarkSubject + "》占用了文号《" + mark2.getMark() + "》,请刷新重试";
                    StringBuffer str = new StringBuffer("");
                    str.append("alert('" + StringEscapeUtils.escapeJavaScript(errMsg) + "');");
                    str.append("history.back();");
                    rendJavaScript(response, str.toString());
                    return null;
                }
            }
            docMark = this.newRegistDocMark(summary.getId(), docMark, 1, summary.getEdocType(), false,
                    EdocEnum.MarkType.edocMark.ordinal(), "save");
            if (docMark != null) {
                summary.setDocMark(docMark);
            }

            // 处理第二个公文文号
            docMark = summary.getDocMark2();
            docMark = this.newRegistDocMark(summary.getId(), docMark, 2, summary.getEdocType(), false,
                    EdocEnum.MarkType.edocMark.ordinal(), "save");
            if (docMark != null) {
                summary.setDocMark2(docMark);
            }

            // 内部文号
            String serialNo = summary.getSerialNo();
            serialNo = this.registDocMark(summary.getId(), serialNo, 3, summary.getEdocType(), false,
                    EdocEnum.MarkType.edocInMark.ordinal());
            if (serialNo != null) {
                summary.setSerialNo(serialNo);
            }

            String note = request.getParameter("note");// 发起人附言
            EdocOpinion senderOninion = new EdocOpinion();
            senderOninion.setContent(note);
            senderOninion.setIdIfNew();
            String trackMode = request.getParameter("isTrack");
            boolean track = "1".equals(trackMode) ? true : false;
            senderOninion.affairIsTrack = track;
            summary.setCanTrack(track ? 1 : 0);
            senderOninion.setAttribute(1);
            senderOninion.setIsHidden(false);
            senderOninion.setCreateUserId(user.getId());
            senderOninion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            senderOninion.setPolicy(request.getParameter("policy"));
            senderOninion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
            senderOninion.setNodeId(0);

            EdocBody body = new EdocBody();
            bind(request, body);
            String tempStr = request.getParameter("bodyType");
            if (Strings.isBlank(tempStr)) {
                tempStr = "HTML";
            }
            body.setContentType(tempStr);
            Date bodyCreateDate = Datetimes.parseDatetime(request.getParameter("bodyCreateDate"));
            if (bodyCreateDate != null) {
                body.setCreateTime(new Timestamp(bodyCreateDate.getTime()));
            }
            body.setIdIfNew();

            // 删除原有附件
            if (!summary.isNew()) {
                this.attachmentManager.deleteByReference(summary.getId());
            }
            /*
             * FlowData flowData = new FlowData();
             * flowData.setDesc_by(FlowData.DESC_BY_XML); String xml =
             * request.getParameter("process_xml"); flowData.setXml(xml);
             */
            // FlowData flowData = FlowData.flowdataFromRequest();

            // test code begin
            summary.setState(CollaborationEnum.flowState.cancel.ordinal());
            if (summary.getCreateTime() == null) {
                summary.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
            // 流程期限具体时间需要做版本控制
            String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
            if (!isQuickSend && "true".equals(isG6)) {
                String deadlineTime = request.getParameter("deadlineTime"); // 如果选的是流程期限具体时间，在这里进行运算，会更精确。
                Long deadlineValue = getMinValueByDeadlineTime(deadlineTime, summary.getCreateTime());
                if (deadlineValue != -1L) {
                    summary.setDeadline(deadlineValue);
                }
            }

            if (summary.getStartTime() == null) {
                summary.setStartTime(new Timestamp(System.currentTimeMillis()));
            }
            if (isAgent) {
                Long summaryStartUId = agentToId != null ? agentToId
                        : edocRegister == null ? null : edocRegister.getDistributerId();
                if (summaryStartUId == null)
                    throw new EdocException(ResourceUtil.getString("proxy.not.exist"));//获取不到被代理人ID
                summary.setStartUserId(summaryStartUId);
                summary.setCreatePerson(_agentToName);
            } else {
                summary.setStartUserId(user.getId());
            }
            summary.setFormId(Long.parseLong(request.getParameter("edoctable")));
            V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
            summary.setStartMember(member);

            body.setIdIfNew();
            if (body.getCreateTime() == null) {
                body.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
            body.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            // test code end

            // 保存附件
            String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.edoc, summary.getId(), summary.getId(),
                    request);
            if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
                summary.setHasAttachments(true);

                // -------------------changyi
                // 增加公文元素中的附件信息--------------------------------
                // 这里只能获得 拟文人所上传的附件 (需要去看下 getByReference方法内部的实现)
                String[] filenames = request
                        .getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_filename);
                String[] fileTypes = request
                        .getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_type);
                /****
                 * 重载一个方法剥离附件和关联文档 by libing
                 */
                summary.setAttachments(EdocHelper.getAttachments(filenames, fileTypes));
            }

            // OA-46482wangchw登记交换来的公文时调用模板保存待发，再次编辑后保存待发，重新生成一条数据
            String hasSummaryId = request.getParameter("newSummaryId");
            if (!isNew && Strings.isNotBlank(hasSummaryId)) {
                // 待发编辑发送时，这里需要设置为之前的id,不能为新的id
                summary.setId(Long.parseLong(hasSummaryId));
            }

            this.zcdbSupervise(request, response, summary, isNew, SuperviseEnum.superviseState.waitSupervise.ordinal(),
                    false);

            // 添加最近处理模板
            if (summary.getTempleteId() != null) {
                templeteManager.addRecentTemplete(AppContext.currentUserId(), ModuleType.collaboration.getKey(),
                        summary.getTempleteId(), TemplateEnum.RecentUseType.deal);
            }

            String process_xml = request.getParameter("process_xml");

            Long affairId = 0L;
            if ("true".equals(backBoxToEdit)) {
                affairId = edocManager.saveBackBox(summary, body, senderOninion);
            } else {
                affairId = edocManager.saveDraft(summary, body, senderOninion, process_xml);
            }

            // 跟踪
            String trackMembers = request.getParameter("trackMembers");
            String trackRange = request.getParameter("trackRange");
            // 不跟踪 或者 全部跟踪的时候不向部门跟踪表中添加数据，所以将下面这个参数串设置为空。
            if (!track || "1".equals(trackRange))
                trackMembers = "";
            // OA-40773 唐桂林 wf从待发中编辑一个收文流程，跟踪设置中显示的跟踪人员显示2遍。而实际发起这个流程时并没有设置跟踪人员
            edocManager.setTrack(affairId, track, trackMembers);

            // GOV-4483 新建收文中新建的阅文，在待阅中转发文，关联发文、关联收文均勾选
            /* puyc 关联收文 */
            Long sendSummaryId = summary.getId();// 发文Id
            String relationRecIdStr = request.getParameter("relationRecId"); // 在分发的时候没有值
            String relationRec = request.getParameter("relationRecd");

            String forwordType = request.getParameter("forwordType");
            String forwordtosend_recAffairId = request.getParameter("forwordtosend_recAffairId");

            if (Strings.isNotBlank(relationRec) && "haveYes".equals(relationRec)) {
                EdocSummaryRelation edocSummaryRelation = new EdocSummaryRelation();
                edocSummaryRelation.setIdIfNew();
                edocSummaryRelation.setSummaryId(sendSummaryId);// 发文Id
                edocSummaryRelation.setRelationEdocId(Long.parseLong(relationRecIdStr));// 收文Id
                edocSummaryRelation.setEdocType(0);// 发文Type
                if (Strings.isNotBlank(forwordtosend_recAffairId)) {
                    edocSummaryRelation.setRecAffairId(Long.parseLong(forwordtosend_recAffairId));
                }
                this.edocSummaryRelationManager.saveEdocSummaryRelation(edocSummaryRelation);
            }

            /* puyc 关联发文 */
            String relationSend = request.getParameter("relationSend");
            if (Strings.isNotBlank(relationSend) && "haveYes".equals(relationSend) &&
                    // 当收文转发文后，收文撤销后从待发中编辑 保存待发，这个时候就不能再保存关联关系了(因为之前转发文时已经保存了)
                    (Strings.isNotBlank(recieveId) || Strings.isNotBlank(relationRecIdStr))) {
                EdocSummaryRelation edocSummaryRelation = new EdocSummaryRelation();
                edocSummaryRelation.setIdIfNew();
                if (Strings.isNotBlank(recieveId)) {
                    edocSummaryRelation.setSummaryId(Long.parseLong(recieveId));// 签收Id
                } else {
                    edocSummaryRelation.setSummaryId(Long.parseLong(relationRecIdStr));// 收文Id
                }
                edocSummaryRelation.setRelationEdocId(sendSummaryId);// 发文Id
                edocSummaryRelation.setEdocType(1);// 收文Type
                // changyi 加上转发人ID
                edocSummaryRelation.setMemberId(user.getId());
                if ("registered".equals(forwordType)) {
                    edocSummaryRelation.setType(1);
                } else if ("waitSent".equals(forwordType)) {
                    edocSummaryRelation.setType(2);
                }
                this.edocSummaryRelationManager.saveEdocSummaryRelation(edocSummaryRelation);
            }
            /* puyc 收文关联发文 end */

            /*
             * 待登记 选择一个 到分发，保存草稿箱后， 在待发中发送后，待登记没消失 （解决办法，保存草稿箱时，就需要将签收记录中的
             * state设为2，表示已登记了）
             */
            String waitRegister_recieveId = request.getParameter("waitRegister_recieveId");

            RecRelationHandlerFactory.getHandler().transAfterSaveRec(summary, edocRegister, waitRegister_recieveId, comm);

            // ***快速发文，保存相关信息到quick表 start ***
            if (isQuickSend) {
                // 交换类型,部门0，单位1
                int edocExchangeType = request.getParameter("edocExchangeType") == null ? -1
                        : Integer.parseInt(request.getParameter("edocExchangeType"));
                String edocMangerID = request.getParameter("memberList"); // 指定的单位收发员
                String exchangeDeptType = request.getParameter("exchangeDeptType");// 交换类型为部门，部门取发起人或封发人类型
                String taohongTemplateUrl = request.getParameter("fileUrl"); // 套红模板的select选项value
                String kuaisuGuidangSelect = request.getParameter("kuaisuGuidangSelect"); // 快速归档的id
                EdocSummaryQuick eq = new EdocSummaryQuick();
                eq.setIdIfNew();
                eq.setSummaryId(summary.getId());
                eq.setExchangeType(edocExchangeType);
                if (StringUtils.isNotBlank(edocMangerID)) {
                    eq.setExchangeAccountMemberId(Long.parseLong(edocMangerID));
                }

                if (edocExchangeType == EdocSendRecord.Exchange_Send_iExchangeType_Org
                        || Strings.isBlank(exchangeDeptType)) {// 交单位，设置成默认

                    exchangeDeptType = EdocSwitchHelper.getDefaultExchangeDeptType(user.getLoginAccount());
                }
                eq.setExchangeDeptType(exchangeDeptType);

                if (StringUtils.isNotBlank(taohongTemplateUrl)) {
                    taohongTemplateUrl = taohongTemplateUrl.replace("\\", "\\\\");
                    eq.setTaohongTemplateUrl(taohongTemplateUrl);
                }
                if (StringUtils.isNotBlank(kuaisuGuidangSelect)) {
                    eq.setArchiveId(Long.parseLong(kuaisuGuidangSelect));
                }
                edocSummaryQuickManager.deleteBySummaryId(summary.getId());
                edocSummaryQuickManager.saveEdocSummaryQuick(eq);

            }
            // ***快速发文，保存相关信息到quick表 end ***

            // 保存待发时登记外来文
            /*
             * if("register".equals(request.getParameter("comm")) &&
             * exchangeIdStr!=null && !"".equals(exchangeIdStr)) { if
             * (isRegistCanBeSaved) { Long exchangeId =
             * Long.parseLong(exchangeIdStr);
             * recieveEdocManager.registerRecieveEdoc(exchangeId); } }
             */

            // return
            // super.redirectModelAndView("/edocController.do?method=edocFrame&from=listWaitSend&edocType="+summary.getEdocType());
            StringBuffer sb = new StringBuffer();

            // 修复bug GOV-3238 【公文管理】-【发文管理】-【待办】，发文转发时保存至草稿箱后跳转的是空白页面
            // 将下面注释掉，可能是以前转发后的页面还是在弹出的新页面中存在的，现在不是了
            // 转发 点保存到草稿箱，直接关闭弹出的窗口
            // if("transmitSend".equals(request.getParameter("comm"))){
            // out.println("window.close();");
            // }else
            String openFrom = request.getParameter("openFrom");
            if ("true".equals(backBoxToEdit)) {
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("} else {");
                String hasBackBox = SystemProperties.getInstance().getProperty("edoc.hasBackBox");
                if ("false".equals(hasBackBox)) {// A8
                    sb.append(
                            "   parent.parent.location.href='edocController.do?method=listIndex&controller=edocController.do&from=listWaitSend&listType=listWaitSend&list=draftBox&edocType="
                                    + summary.getEdocType() + Functions.csrfSuffix() + "'");
                } else {
                    sb.append(
                            "	parent.parent.location.href='edocController.do?method=listIndex&controller=edocController.do&from=listWaitSend&list=backBox&edocType="
                                    + summary.getEdocType() + Functions.csrfSuffix() + "'");
                }
                sb.append("}");
            } else if (Strings.isNotBlank(openFrom) && "ucpc".equals(openFrom)) {
                sb.append("if(typeof(getA8Top)!='undefined') {");
                sb.append(" getA8Top().window.close();");
                sb.append("} else {");
                sb.append("	parent.parent.parent.window.close();");
                sb.append("}");
            } else {
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("} else {");
                sb.append(
                        "   parent.parent.location.href='edocController.do?method=listIndex&controller=edocController.do&from=listWaitSend&listType=listWaitSend&list=draftBox&edocType="
                                + summary.getEdocType() + Functions.csrfSuffix() + "'");
                sb.append("}");
            }
            rendJavaScript(response, sb.toString());
        } finally {
            if (lockDocMarkSubject == null) {
                edocMarkLockManager.unlock(lockDocMark);
            }
        }
        return null;
    }

    /**
     * 归档后修改保存
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveArchived(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        // boolean updateMark = false;
        long summaryId = Long.parseLong(request.getParameter("summaryId"));
        EdocSummary edocSummary = edocManager.getEdocSummaryById(summaryId, true);
        Long affairId = Long.valueOf(request.getParameter("affairId"));
        CtpAffair affair = affairManager.get(affairId);
        long formId = Long.parseLong(request.getParameter("edoctable"));
        try {
            // String lastEdocSummaryContent=XMLCoder.encoder(edocSummary);
            // //把上一版本数据保存为xml格式（留痕）

            DataUtil.requestToSummary(request, edocSummary, formId);

            // 处理公文文号
            // 如果公文文号为空，不做任何处理
            String docMark = edocSummary.getDocMark();
            if (docMark != null && !"".equals(docMark)) {
                try {
                    docMark = this.registDocMark(edocSummary.getId(), docMark, 1, edocSummary.getEdocType(), true,
                            EdocEnum.MarkType.edocMark.ordinal());
                } catch (EdocMarkHistoryExistException e) {
                    return null;
                }
                if (docMark != null) {
                    edocSummary.setDocMark(docMark);
                }
                // updateMark = true;
            }
            // 处理第二个公文文号
            docMark = edocSummary.getDocMark2();
            if (docMark != null && !"".equals(docMark)) {
                docMark = this.registDocMark(edocSummary.getId(), docMark, 2, edocSummary.getEdocType(), true,
                        EdocEnum.MarkType.edocMark.ordinal());
                if (docMark != null) {
                    edocSummary.setDocMark2(docMark);
                }
                // updateMark = true;
            }

            // 处理内部文号
            String serialNo = edocSummary.getSerialNo();
            if (serialNo != null && !"".equals(serialNo)) {
                serialNo = this.registDocMark(edocSummary.getId(), serialNo, 3, edocSummary.getEdocType(), true,
                        EdocEnum.MarkType.edocInMark.ordinal());
                if (serialNo != null) {
                    edocSummary.setSerialNo(serialNo);
                }
            }

            // 封发的时候进行相关的文号操作，移动到历史表中。
            List<EdocSendRecord> es = sendEdocManager.getEdocSendRecordOnlyByEdocId(edocSummary.getId());
            if (es != null && es.size() > 0) {
                edocMarkHistoryManager.afterSend(edocSummary);
            }

            // 正文
            EdocBody body = edocSummary.getFirstBody();
            /*
             * bind(request, body); String
             * tempStr=request.getParameter("bodyType");
             * body.setContentType(tempStr); Date bodyCreateDate =
             * Datetimes.parseDatetime(request.getParameter("bodyCreateDate"));
             * if (bodyCreateDate != null) { body.setCreateTime(new
             * Timestamp(bodyCreateDate.getTime())); } body.setIdIfNew();
             * if(body.getCreateTime()==null) { body.setCreateTime(new
             * Timestamp(System.currentTimeMillis())); }
             */
            body.setContent(request.getParameter("content"));
            body.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            // edocManager.saveBody(body);
            // 删除原有附件
            if (!edocSummary.isNew()) {
                this.attachmentManager.deleteByReference(edocSummary.getId(), edocSummary.getId());
            }
            // 保存附件
            String attaFlag = this.attachmentManager.create(ApplicationCategoryEnum.edoc, edocSummary.getId(),
                    edocSummary.getId(), request);
            if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {

                edocSummary.setHasAttachments(true);

                // 附件只保存附件,不保存关联文档
                String[] filenames = request
                        .getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_filename);
                String[] fileTypes = request
                        .getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_type);
                edocSummary.setAttachments(EdocHelper.getAttachments(filenames, fileTypes));
            } else {
                edocSummary.setHasAttachments(false);
            }

            // BPMActivity bPMActivity =
            // EdocHelper.getBPMActivityByAffair(affair) ;
            // String
            // isOnlyModifyWordNo=request.getParameter("isOnlyModifyWordNo");
            // if("true".equals(isOnlyModifyWordNo)){
            // this.processLogManager.insertLog(user,
            // Long.valueOf(edocSummary.getProcessId()),Long.valueOf(bPMActivity.getId()),
            // ProcessLogAction.processEdoc,
            // String.valueOf(ProcessLogAction.ProcessEdocAction.modifyWordNo.getKey()))
            // ;
            // }else{
            // this.processLogManager.insertLog(user,
            // Long.valueOf(edocSummary.getProcessId()),Long.valueOf(bPMActivity.getId()),
            // ProcessLogAction.processEdoc,
            // String.valueOf(ProcessLogAction.ProcessEdocAction.modifyForm.getKey()))
            // ;
            // }
            edocManager.update(edocSummary, true);

            // 新增修改历史记录
            String isModifyContent = request.getParameter("isModifyContent");// 是否修改正文
            String isModifyForm = request.getParameter("isModifyForm");// 是否修改文单
            String isModifyAtt = request.getParameter("isModifyAtt");// 是否修改附件
            EdocArchiveModifyLog modifyLog = new EdocArchiveModifyLog();
            modifyLog.setIdIfNew();
            modifyLog.setSummaryId(summaryId);
            modifyLog.setUpdatePerson(user.getName());
            modifyLog.setUserId(user.getId());
            modifyLog.setUpdateTime(new Date());
            modifyLog.setModifyContent(isModifyContent != null ? Integer.parseInt(isModifyContent) : 0);
            modifyLog.setModifyForm(isModifyForm != null ? Integer.parseInt(isModifyForm) : 0);
            modifyLog.setModifyAtt(isModifyAtt != null ? Integer.parseInt(isModifyAtt) : 0);
            // modifyLog.setHistoryContent(lastEdocSummaryContent);

            // GOV-4538 公文发文，已办，已办结公文的修改历史不显示发起者附言的修改
            // 修复附言也算修改文单
            /*
             * if(isModifyNote){ modifyLog.setModifyForm(1); }
             */
            // 必须修改一样，才记录到修改历史中
            if (modifyLog.getModifyContent() == 1 || modifyLog.getModifyForm() == 1 || modifyLog.getModifyAtt() == 1) {
                edocArchiveModifyLogManager.saveEdocArchiveModifyLog(modifyLog);

                // 应用日志
                AppLogAction alog = null;
                int edocType = edocSummary.getEdocType();
                if (edocType == 0) {
                    if (modifyLog.getModifyContent() == 1) {
                        alog = AppLogAction.Edoc_pigeonhole_send_Content_update;
                        appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
                    }

                    if (modifyLog.getModifyForm() == 1) {
                        alog = AppLogAction.Edoc_pigeonhole_send_Form_update;
                        appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
                    }

                    if (modifyLog.getModifyAtt() == 1) {
                        alog = AppLogAction.Edoc_pigeonhole_send_File_update;
                        appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
                    }
                } else {
                    if (modifyLog.getModifyContent() == 1) {
                        alog = AppLogAction.Edoc_pigeonhole_sign_Content_update;
                        appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
                    }

                    if (modifyLog.getModifyForm() == 1) {
                        alog = AppLogAction.Edoc_pigeonhole_sign_Form_update;
                        appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
                    }

                    if (modifyLog.getModifyAtt() == 1) {
                        alog = AppLogAction.Edoc_pigeonhole_sign_File_update;
                        appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
                    }
                }

            }

            if (AppContext.hasPlugin("doc")) {
                // 归档的数据修改（doc_resource,doc_metadata）
                docApi.updatePigehole(user.getId(), edocSummary.getId(), ApplicationCategoryEnum.edoc.getKey());
            }

            // 更新所有相关的affair的值，比如标题之类的--start
            Map<String, Object> columns = new HashMap<String, Object>();
            columns.put("subject", edocSummary.getSubject());// 标题
            if (Strings.isNotBlank(edocSummary.getUrgentLevel())) {
                columns.put("importantLevel", Integer.parseInt(edocSummary.getUrgentLevel()));// 紧急程度
            }
            columns.put("identifier", edocSummary.getIdentifier());// 附件标志位

            Map<String, Object> extParamMap = EdocUtil.createExtParam(edocSummary);
            if (extParamMap != null) {
                columns.put("extProps", XMLCoder.encoder(extParamMap));// 扩展字段--包含发文单位和公文文号
            }
            Object[][] wheres = new Object[1][2];
            wheres[0] = new Object[]{"objectId", edocSummary.getId()};
            affairManager.update(columns, wheres);
            // 更新所有相关的affair的值，比如标题之类的--end

            // 更新交换列表--start
            Map<String, Object> columnsExchange = new HashMap<String, Object>();
            columnsExchange.put("subject", edocSummary.getSubject());// 标题
            columnsExchange.put("docMark", edocSummary.getDocMark());// 发文文号
            columnsExchange.put("secretLevel", edocSummary.getSecretLevel());// 密级
            columnsExchange.put("urgentLevel", edocSummary.getUrgentLevel());// 紧急程度
            columnsExchange.put("copies", edocSummary.getCopies());// 份数
            Object[][] whereExchange = new Object[1][2];
            whereExchange[0] = new Object[]{"edocId", edocSummary.getId()};
            sendEdocManager.update(columnsExchange, whereExchange);
            columnsExchange.remove("copies"); // 签收表没有copies
            recieveEdocManager.update(columnsExchange, whereExchange);
            // 更新交换列表--end

        } catch (Exception e) {
            LOGGER.error("公文发文已办结归档修改异常", e);
        } finally {
            StringBuffer sb = new StringBuffer();
            // out.println("alert('"+processMessage+"');");
            String listType = Strings.isBlank(request.getParameter("listType")) ? "listDoneAll"
                    : request.getParameter("listType");
            sb.append("location.href='edocController.do?method=listDone&edocType=" + edocSummary.getEdocType()
                    + "&listType=" + listType + Functions.csrfSuffix() + "';");
            rendJavaScript(response, sb.toString());
        }

        return null;
    }

    /**
     * 页面框架
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView edocFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocFrame");
        // 会议纪要转公文 获得会议纪要ID
        String meetingSummaryId = request.getParameter("meetingSummaryId");
        String comm = request.getParameter("comm");
        String from = request.getParameter("from");
        String listType = Strings.isBlank(request.getParameter("listType")) ? "" : request.getParameter("listType");
        if (Strings.isNotBlank(from) && Strings.isBlank(listType)) {
            listType = from;
        }
        try {
            String controllerURL = EdocNavigationEnum.EdocV5ListTypeEnum.getControllerName(listType);
            modelAndView.addObject("controller", controllerURL);
        } catch (Exception e) {
            modelAndView.addObject("controller", "edocController.do");
        }
        try {
            String listMethod = EdocNavigationEnum.EdocV5ListTypeEnum.getMethodName(listType);
            modelAndView.addObject("listMethod", listMethod);
        } catch (Exception e) {
            modelAndView.addObject("listMethod", from);
        }
        String edocType = request.getParameter("edocType");
        modelAndView.addObject("edocType", edocType);

        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }
        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType
                || EdocEnum.edocType.distributeEdoc.ordinal() == iEdocType) {// 收文
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);// 公文发起权

        boolean isExchangeRole = EdocRoleHelper.isExchangeRole(); // 公文交换权
        modelAndView.addObject("edocType", edocType);
        modelAndView.addObject("hasRegistButton", hasRegistButton);
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        modelAndView.addObject("meetingSummaryId", meetingSummaryId);
        modelAndView.addObject("registerType", request.getParameter("registerType"));
        modelAndView.addObject("openForm", request.getParameter("openFrom"));
        modelAndView.addObject("comm", comm);

        modelAndView.addObject("listType", listType);

        String nowFrom = request.getParameter("from");
        if (nowFrom != null && !"".equals(nowFrom) && "newEdoc".equals(nowFrom)) {
            modelAndView.addObject("isNewEdoc", true);
        }

        if (from != null && "listFenfa".equals(from)) {
            modelAndView.addObject("isFenfa", true);
        }

        if ("agent".equals(request.getParameter("app"))) {
            long affairId = Strings.isBlank(request.getParameter("affairId")) ? -1
                    : Long.parseLong(request.getParameter("affairId"));
            if (affairId != -1) {
                CtpAffair affair = affairManager.get(affairId);
                if (affair != null && affair.getMemberId().longValue() != AppContext.getCurrentUser().getId()) {
                    if (affair.getApp() == 24) {// 收文登记
                        hasRegistButton = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal());// 公文发起权
                        if (!hasRegistButton) {
                            modelAndView.addObject("isAgent", true);
                        }
                    } else if (affair.getApp() == 34) {// 收文分发
                        if (!isEdocCreateRole) {
                            modelAndView.addObject("isAgent", true);
                        }
                    }
                }
            } else {
                modelAndView.addObject("isAgent", true);
            }
        }

        return modelAndView;
    }

    /**
     * 已办公文列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listDone(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");
        String edocType = request.getParameter("edocType");
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        params.put("processType", 1);
        String list = request.getParameter("list");
        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }
        ModelAndView modelAndView = new ModelAndView("edoc/listDone");
        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getMemberById(user.getId());
        List<EdocSummaryModel> queryList = null;

        // 同一流程只显示最后一条
        String deduplication = request.getParameter("deduplication");
        if ("null".equals(deduplication) || Strings.isBlank(deduplication)) {
            deduplication = "false";
        }
        modelAndView.addObject("isGourpBy", deduplication);
        params.put("deduplication", deduplication);

        if (theMember.getIsAssigned())

        {
            /********** 组合查询组件调用开始 *****************/
            String comb_condition = request.getParameter("comb_condition"); // 组合查询标识
            if (comb_condition != null && "1".equals(comb_condition)) {
                EdocSearchModel em = new EdocSearchModel();
                bind(request, em);
                if ("notFinish".equals(list)) {
                    queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_done.key(), -1);
                } else {
                    queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_done.key(), params,
                            new int[]{});
                }
                modelAndView.addObject("combQueryObj", em); // 设置的查询条件还原到页面
                modelAndView.addObject("combCondition", "1");
            } else
            /*************** 组合查询组件调用结束 ************/
                if (condition != null && !"".equals(condition)
                        && (Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1))) {
                    if ("cusReceiveTime".equals(condition)) { // 如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
                        condition = "receiveTime";
                        String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
                        textfield = textfield_condition[0];
                        textfield1 = textfield_condition[1];
                    }
                    if ("notFinish".equals(list)) {
                        queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                                StateEnum.col_done.key(), params, -1);
                    } else {
                        queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                                StateEnum.col_done.key(), params);
                    }
                }
            if (queryList != null && !"".equals(condition)) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                List<EdocSummaryModel> finishedList = null;
                if (iEdocType == 0) {// 执行发文查询
                    // 未办结
                    if ("notFinish".equals(list)) {
                        finishedList = edocManager.queryByCondition(iEdocType, "", null, null, StateEnum.col_done.key(),
                                params, -1);
                    } else {// 全部，已办结在listFinish方法
                        finishedList = edocManager.queryByCondition(iEdocType, "", null, null, StateEnum.col_done.key(),
                                params);
                    }

                } else {// 执行收文查询
                    if ("notFinish".equals(list)) {
                        finishedList = edocManager.queryByCondition(iEdocType, "", null, null, StateEnum.col_done.key(),
                                params, -1);
                    } else {
                        finishedList = edocManager.queryByCondition(iEdocType, "", null, null, StateEnum.col_done.key(),
                                params);
                    }
                }
                modelAndView.addObject("pendingList", finishedList);
                // 这里逻辑重复，上面已经有condition<>null和==null的逻辑
                /*
                 * if("".equals(condition)){ List<EdocSummaryModel> finishedList
                 * = null; finishedList =
                 * edocManager.queryByCondition(iEdocType,"", null, null,
                 * StateEnum.col_done.key());
                 * modelAndView.addObject("pendingList", finishedList); }else{
                 * List<EdocSummaryModel> finishedList =
                 * edocManager.queryByCondition(iEdocType,"", null, null,
                 * StateEnum.col_done.key(),-1);
                 * modelAndView.addObject("pendingList", finishedList); }
                 */
            }
        } else {
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }
        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        // Metadata comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);

        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);

        /*
         * 元数据中加载 秘密程度 cy add
         */
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        /*
         * 元数据中加载 紧急程度 cy add
         */
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);
        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");

        modelAndView.addObject("edocType", edocType);
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
        } else {
            modelAndView.addObject("newEdoclabel", "edoccolMetadata.new.type.send");
        }
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        // 发文拟文权限
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        // 公文已办取回加锁需要的登录人的userId
        modelAndView.addObject("currentUserId", user.getId());
        // OA-31123 收文：已发、待办、已办小查询条件不要：成文单位，此为原G6的条件
        modelAndView.addObject("isG6", EdocHelper.isG6Version());

        return modelAndView;
    }

    /**
     * 显示自定义类别列表页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView customerTypes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String isAdminSet = request.getParameter("isAdminSet");

        ModelAndView modelAndView = new ModelAndView("edoc/edocCustomerType");
        User user = AppContext.getCurrentUser();
        // 获取单位ID
        long loginAccount = user.getLoginAccount();
        // 根据发文或收文，获取其节点权限列表
        // 获得当前是 发文还是收文
        String edocType = request.getParameter("edocType") == null ? "0" : request.getParameter("edocType");
        int iEdocType = -1;

        List<Permission> nodeList = null;
        // 发文时 节点列表
        if (Integer.parseInt(edocType) == EdocCustomerTypeEnum.SEND_NODE_TYPE.ordinal()) {
            nodeList = permissionManager.getPermissionsByCategory("edoc_send_permission_policy", loginAccount);
        }
        // 收文时 节点列表
        if (Integer.parseInt(edocType) == EdocCustomerTypeEnum.REC_NODE_TYPE.ordinal()) {
            nodeList = permissionManager.getPermissionsByCategory("edoc_rec_permission_policy", loginAccount);
        }

        // 紧急程度
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        modelAndView.addObject(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);
        // 文件密级 完了，
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        modelAndView.addObject(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        // 行文类型
        CtpEnumBean sendType = enumManagerNew.getEnum(EnumNameEnum.edoc_send_type.name());
        modelAndView.addObject(EnumNameEnum.edoc_send_type.toString(), sendType);

        // 还要区分是待办还是已办
        String from = request.getParameter("from");
        if (Strings.isNotBlank(from)) {
            iEdocType = getCustomType(edocType, from);
        }

        // 自定义列表,还是要区分 发文 和 收文
        // 用户已勾选 需要显示的 类别列表
        /* CustomerTypeList 和 流程节点POJO 应该是不一样的，在jsp页面中其实 只需要比较他们的 ID 即可 */
        Map map = edocCustomerTypeManager.getUserCustomerTypeList(user, iEdocType, isAdminSet,
                Integer.parseInt(edocType));
        List customerTypeList = (List) map.get("customerTypeList");
        List timeList = (List) map.get("timeList");
        List sendList = (List) map.get("sendList");
        modelAndView.addObject("nodeList", nodeList);
        modelAndView.addObject("timeList", timeList);
        modelAndView.addObject("sendList", sendList);
        modelAndView.addObject("customerTypeList", customerTypeList);
        // //大类别，页面好处理
        // long bigTypeId = 0;
        // if(customerTypeList.size()>0){
        // bigTypeId =
        // ((EdocCustomerType)customerTypeList.get(0)).getBigTypeId();
        // }
        // modelAndView.addObject("bigTypeId", bigTypeId);
        modelAndView.addObject("edocType", edocType);
        return modelAndView;
    }

    private int getCustomType(String edocType, String from) {
        int iEdocType = -1;
        if ("listPending".equals(from)) {
            if ("0".equals(edocType)) {
                iEdocType = EdocCustomConstant.SEND_PENDING; // 发文待办
            } else {
                iEdocType = EdocCustomConstant.REC_PENDING;
                ; // 收文待办
            }
        } else if ("listDone".equals(from)) {
            if ("0".equals(edocType)) {
                iEdocType = EdocCustomConstant.SEND_DONE; // 发文已办
            } else {
                iEdocType = EdocCustomConstant.REC_DONE; // 收文已办
            }
        } else if ("listReaded".equals(from)) { // 已阅
            iEdocType = EdocCustomConstant.REC_READED;
        }
        if (iEdocType == -1) {
            if ("0".equals(edocType)) {
                iEdocType = EdocCustomConstant.SEND_ADMIN; // 统一设置发文自定义类型
            } else if ("1".equals(edocType)) {
                iEdocType = EdocCustomConstant.REC_ADMIN; // 统一设置收文自定义类型
            }
        }
        return iEdocType;
    }

    /***********************************
     * 唐桂林 公文意见显示 start
     *************************************/
    /**
     * lijl添加,如果是用户选择意见覆盖方式,同一个公文单同一个人,在二条意见以上则弹出该文本框
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView optionSetup(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/optionSetup");
        String optionId = request.getParameter("optionId");
        String optionType = request.getParameter("optionType");
        String affairId = request.getParameter("affairId");
        Long summaryId = Strings.isBlank(request.getParameter("summary_id")) ? 0L
                : Long.parseLong(request.getParameter("summary_id"));
        String policy = request.getParameter("policy");
        modelAndView.addObject("summaryId", summaryId);
        modelAndView.addObject("affairId", affairId);
        modelAndView.addObject("policy", policy);
        modelAndView.addObject("optionId", optionId);
        modelAndView.addObject("optionType", optionType);
        return modelAndView;
    }

    /**
     * lijl添加,如果是用户选择意见覆盖方式,如果用户选择后修改State状态
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView upOptionState(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/optionSetup");
        String policy = request.getParameter("policy");
        String optionType = request.getParameter("optionType");
        String optionWay = request.getParameter("optionWay");
        long affairId = Long.parseLong(request.getParameter("affairId"));
        Long summaryId = Strings.isBlank(request.getParameter("summaryId")) ? 0L
                : Long.parseLong(request.getParameter("summaryId"));
        User user = AppContext.getCurrentUser();
        int state = 0;
        // 3退回时办理人选择意见覆盖方式,其他情况保留最后意见，被退回人选择保存最后意见
        if ((optionType != null) && ("3".equals(optionType)) && ("3_1".equals(optionWay))) {
            List<EdocOpinion> edocOpinionList = edocManager.findEdocOpinion(summaryId, user.getId(), policy);
            for (int i = 0; i < edocOpinionList.size(); i++) {
                EdocOpinion edocOpinion = edocOpinionList.get(i);
                // 修改状态
                edocOpinion.setState(1);
                edocManager.update(edocOpinion);
            }
            state = 3;
        }
        // 3退回时办理人选择意见覆盖方式,其他情况保留最后意见，被退回人选择保存所有意见
        else if ((optionType != null) && ("3".equals(optionType)) && ("3_2".equals(optionWay))) {
            // 如果进入到此方法则设置flag为true表示[办理人选择的是"办理人选择覆盖方式,其他保留所有意见]
            List<EdocOpinion> edocOpinionList = edocManager.findEdocOpinion(summaryId, user.getId(), policy);
            for (int i = 0; i < edocOpinionList.size(); i++) {
                EdocOpinion edocOpinion = edocOpinionList.get(i);
                // 修改状态
                edocOpinion.setState(0);
                edocManager.update(edocOpinion);
            }
            state = 2;
        }
        // 4退回时办理人选择意见覆盖方式,其他情况保留所有意见，被退回人选择保存最后意见
        else if ((optionType != null) && ("4".equals(optionType)) && ("4_1".equals(optionWay))) {
            List<Long> affairIds = new ArrayList<Long>();
            CtpAffair affair = affairManager.get(affairId);
            List<CtpAffair> affairList = affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
            for (CtpAffair aff : affairList) {
                affairIds.add(aff.getId());
            }
            List<EdocOpinion> edocOpinionList = edocManager.findEdocOpinionByAffairId(summaryId, affair.getMemberId(),
                    policy, affairIds);
            for (int i = 0; i < edocOpinionList.size(); i++) {
                EdocOpinion edocOpinion = edocOpinionList.get(i);
                // 修改状态
                edocOpinion.setState(1);
                edocManager.update(edocOpinion);
            }
            state = 3;
        }
        // 4退回时办理人选择意见覆盖方式,其他情况保留所有意见，被退回人选择保存所有意见
        else if ((optionType != null) && ("4".equals(optionType)) && ("4_2".equals(optionWay))) {
            List<Long> affairIds = new ArrayList<Long>();
            CtpAffair affair = affairManager.get(affairId);
            List<CtpAffair> affairList = affairManager.getAffairsByObjectIdAndNodeId(affair.getObjectId(), affair.getActivityId());
            for (CtpAffair aff : affairList) {
                affairIds.add(aff.getId());
            }
            // 保留所有意见
            List<EdocOpinion> edocOpinionList = edocManager.findEdocOpinionByAffairId(summaryId, affair.getMemberId(),
                    policy, affairIds);
            for (int i = 0; i < edocOpinionList.size(); i++) {
                // EdocOpinion edocOpinion=edocOpinionList.get(i);
                // 修改状态
                // edocOpinion.setState(0);
                // edocManager.update(edocOpinion);
            }
            state = 4;
        }
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        pw.write(Strings.toHTML(optionWay));
        pw.flush();
        modelAndView.addObject("state", state);
        return modelAndView;
    }

    /***********************************
     * 唐桂林 公文意见显示 end
     *************************************/

    /**
     * 保存勾选的自定义类别
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveCustomerTypes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String edocType = request.getParameter("edocType");

        String isAdminSet = request.getParameter("isAdminSet");
        int iEdocType = -1;

        // 还要区分是待办还是已办
        String from = request.getParameter("from");
        if (Strings.isNotBlank(from)) {
            iEdocType = getCustomType(edocType, from);
        }

        // 选中的大的分类，比如 节点、时间、元素、自定义等
        long bigTypeId = Long.parseLong(request.getParameter("bigTypeId"));
        String[] typeIdStr = request.getParameterValues("typeId");

        long[] typeIds = null;
        String[] typeName = null;
        String[] typeCode = null;
        if (typeIdStr != null && typeIdStr.length > 0) {
            typeIds = new long[typeIdStr.length];
            typeName = new String[typeIdStr.length];
            typeCode = new String[typeIdStr.length];

            for (int i = 0; i < typeIds.length; i++) {
                String[] nn = typeIdStr[i].split("_");
                // 流程节点 保存进 typeCode
                if (bigTypeId == 0 || bigTypeId == 1) {
                    typeCode[i] = nn[1];
                } else {
                    typeName[i] = nn[1];
                }
                typeIds[i] = Long.parseLong(nn[0]);
                typeName[i] = nn[1];
                typeCode[i] = nn[2] + "_" + nn[3];

            }
        }

        User user = AppContext.getCurrentUser();
        edocCustomerTypeManager.saveUserCustomerType(user, typeIds, typeName, typeCode, bigTypeId, iEdocType,
                isAdminSet, Integer.parseInt(edocType));

        ModelAndView modelAndView = new ModelAndView("edoc/inner");
        modelAndView.addObject("message", ResourceUtil.getString("edoc.msg.saveSuccess"));// 保存成功!
        return modelAndView;
    }

    /**
     * 已办结公文列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listFinish(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");
        String edocType = request.getParameter("edocType");
        String hasArchive = request.getParameter("hasArchive");
        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        params.put("processType", 1);
        ModelAndView modelAndView = new ModelAndView("edoc/listFinish");
        int ihasArchive = -1;
        if (hasArchive != null && !"".equals(hasArchive)) {
            ihasArchive = Integer.parseInt(hasArchive);
            modelAndView.addObject("ihasArchive", ihasArchive);
        }
        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());

        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned())

        {
            /********** 组合查询组件调用开始 *****************/
            String comb_condition = request.getParameter("comb_condition"); // 组合查询标识
            if (comb_condition != null && "1".equals(comb_condition)) {
                EdocSearchModel em = new EdocSearchModel();
                bind(request, em);
                // queryList =
                // edocManager.combQueryByCondition(iEdocType,em,StateEnum.col_done.key(),Constant.flowState.finish.ordinal(),ihasArchive);
                queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_done.key(), params,
                        new int[]{CollaborationEnum.flowState.finish.ordinal(), ihasArchive});
                modelAndView.addObject("combQueryObj", em); // 设置的查询条件还原到页面
                modelAndView.addObject("combCondition", "1");
            } else
            /*************** 组合查询组件调用结束 ************/
                if (condition != null) {
                    if ("cusReceiveTime".equals(condition)) { // 如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
                        condition = "receiveTime";
                        String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
                        textfield = textfield_condition[0];
                        textfield1 = textfield_condition[1];

                    }
                    queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                            StateEnum.col_done.key(), params, CollaborationEnum.flowState.finish.ordinal(), ihasArchive);
                }
            if (queryList != null) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                List<EdocSummaryModel> finishedList = edocManager.queryByCondition(iEdocType, "", null, null,
                        StateEnum.col_done.key(), params, CollaborationEnum.flowState.finish.ordinal(), ihasArchive);
                modelAndView.addObject("pendingList", finishedList);
            }
        } else {
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);

        // Metadata comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);

        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);
        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        int modifyType = EdocEnum.ArchiveModifyType.archiveModifySendEdoc.ordinal();
        switch (iEdocType) {
            case 0:
                modifyType = EdocEnum.ArchiveModifyType.archiveModifySendEdoc.ordinal();
                break;
            case 1:
                modifyType = EdocEnum.ArchiveModifyType.archiveModifyRecEdoc.ordinal();
                break;
            case 2:
                modifyType = EdocEnum.ArchiveModifyType.archiveModifySignEdoc.ordinal();
                break;
            default:
                modifyType = EdocEnum.ArchiveModifyType.archiveModifySendEdoc.ordinal();
                break;
        }
        boolean isArchiveRole = EdocRoleHelper.isEdocArchivedModifyRole(user.getAccountId(), user.getId(), modifyType); // 公文归档角色判断
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        modelAndView.addObject("isArchiveRole", isArchiveRole);
        // 发文拟文权限
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        return modelAndView;
    }

    /**
     * 待办公文列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listPending(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute("transmitSendAtts");
        /*
         * List <V3xOrgMember> mems=EdocRoleHelper.getAccountExchangeUsers()
         * HttpServletResponse response) throws Exception{
         * request.getSession().removeAttribute("transmitSendAtts"); /* List
         * <V3xOrgMember> mems=EdocRoleHelper.getAccountExchangeUsers(); List
         * <V3xOrgMember> memsd=EdocRoleHelper.getDepartMentExchangeUsers();
         * boolean isEdocAdmin=EdocRoleHelper.isAccountExchange();
         * isEdocAdmin=EdocRoleHelper.isDepartmentExchange(); String
         * ids=EdocRoleHelper.getUserExchangeDepartmentIds();
         */

        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");
        String edocType = request.getParameter("edocType");
        String list = request.getParameter("list");// 区分待办、在办
        // GOV-3428 公文管理-收文管理-待办页面，进入页面后默认显示待办公文，不正确
        // 默认进入待办的 全部
        if (Strings.isBlank(list)) {
            list = "listPending";
        }

        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        params.put("processType", 1);
        ModelAndView modelAndView = new ModelAndView("edoc/listPending");

        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());

        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned()) {
            /********** 组合查询组件调用开始 *****************/
            String comb_condition = request.getParameter("comb_condition"); // 组合查询标识
            if (comb_condition != null && "1".equals(comb_condition)) {
                EdocSearchModel em = new EdocSearchModel();
                bind(request, em);
                params.put("list", list);
                // queryList =
                // edocManager.combQueryByCondition(iEdocType,em,StateEnum.col_pending.key());
                queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_pending.key(), params,
                        new int[]{});
                modelAndView.addObject("combQueryObj", em); // 设置的查询条件还原到页面
                modelAndView.addObject("combCondition", "1");
            } else
            /*************** 组合查询组件调用结束 ************/
                if (condition != null && !"".equals(condition)) {
                    if ("cusReceiveTime".equals(condition)) { // 如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
                        condition = "receiveTime";
                        String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
                        textfield = textfield_condition[0];
                        textfield1 = textfield_condition[1];
                    }

                    // queryList = edocManager.queryByCondition(iEdocType,condition,
                    // textfield, textfield1, StateEnum.col_pending.key(), params,
                    // -1,-1,1);
                    // 修复bug GOV-3085 【公文管理】-【发文管理】-【待办】，发文待办【全部】列表里有16条记录，
                    // 但是根据任一查询条件只能查询出14条待办状态的，在办状态的查询不到
                    if (null != list && "notPending".equals(list)) {
                        queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                                StateEnum.col_pending.key(), params, -1, -1, 1);
                    } else {
                        queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                                StateEnum.col_pending.key(), params); // 全部待办数据
                    }

                }
            if (queryList != null) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                if ("".equals(condition) || null == condition) {
                    List<EdocSummaryModel> pendingList = null;
                    if (iEdocType == 1) {
                        params.put("processType", 1);
                        if (null != list && "pending".equals(list)) {
                            pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                    StateEnum.col_pending.key(), params, 1, -1, 1); // 查询已办数据
                        }
                        if (null != list && "listPending".equals(list)) {// 全部
                            pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                    StateEnum.col_pending.key(), params);
                        } else {
                            pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                    StateEnum.col_pending.key(), params, -1, -1, 1); // 全部待办数据
                        }
                    } else {
                        if (null != list && "notPending".equals(list)) {// 发文，待办（不包括在办）
                            pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                    StateEnum.col_pending.key(), params, -1);
                        } else {// 全部待办数据
                            pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                    StateEnum.col_pending.key(), params); // 页签，全部待办数据
                        }

                    }
                    modelAndView.addObject("pendingList", pendingList);
                } else {
                    List<EdocSummaryModel> pendingList = null;
                    if (null != list && "notPending".equals(list)) {
                        pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                StateEnum.col_pending.key(), params, -1, -1, 1);// 左侧列表，除去在办数据
                    } else {

                        pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                                StateEnum.col_pending.key(), params); // 全部待办数据
                    }

                    modelAndView.addObject("pendingList", pendingList);
                }
            }
        } else {
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        // Metadata comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);

        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);

        /*
         * 元数据中加载 秘密程度 cy add
         */
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        /*
         * 元数据中加载 紧急程度 cy add
         */
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);
        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);

        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        boolean hasSubjectWrap = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;

            // xiangfan 添加逻辑：获得当前用户是否设置了 收文-待办 的公文标题多行显示
            hasSubjectWrap = this.edocSummaryManager.hasSubjectWrapRecordByCurrentUser(user.getAccountId(),
                    user.getId(), EdocSubjectWrapRecord.Edoc_Receive_Pending, iEdocType);
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");

            // xiangfan 添加逻辑：获得当前用户是否设置了 发文-待办 的公文标题多行显示
            hasSubjectWrap = this.edocSummaryManager.hasSubjectWrapRecordByCurrentUser(user.getAccountId(),
                    user.getId(), EdocSubjectWrapRecord.Edoc_Send_Pending, iEdocType);
        }
        modelAndView.addObject("hasSubjectWrap", hasSubjectWrap);

        modelAndView.addObject("hasRegistButton", hasRegistButton);
        /********* 转发文 **********/
        modelAndView.addObject("newForwardaRrticle", "edoc.new.type.forwardarticle");
        /********* 转发文 **********/
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        // 发文的拟文权限
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        // OA-31123 收文：已发、待办、已办小查询条件不要：成文单位，此为原G6的条件
        modelAndView.addObject("isG6", EdocHelper.isG6Version());

        return modelAndView;
    }

    /**
     * 弹出公文转起草类型提示框 wangwei
     */
    public ModelAndView forwordOption(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/forwordOption");
        String recEdocId = request.getParameter("recEdocId"); // 发文id,待办、已办等处传递
        String recieveRecordId = request.getParameter("recieveRecordId"); // 签收id，待登记处传递
        boolean disableContent = false; // 缺省可以选择作为正文转发
        EdocBody body = null;
        if (Strings.isNotBlank(recEdocId)) {
            body = this.edocSummaryManager.getBodyByIdAndNum(recEdocId, 0);
        } else if (Strings.isNotBlank(recieveRecordId)) {
            EdocRecieveRecord record = this.recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveRecordId));
            if (record != null) {
                body = this.edocSummaryManager.getBodyByIdAndNum(String.valueOf(record.getEdocId()),
                        record.getContentNo());
            }
        }
        if (body != null)
            disableContent = MainbodyType.Pdf.name().equals(body.getContentType());
        return modelAndView.addObject("disableContent", disableContent);
    }

    /**
     * 弹出阅文批量登记页面
     */
    public ModelAndView readBatchWDRegist(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocReadBatchRegist");
        User user = AppContext.getCurrentUser();
        String openType = request.getParameter("openType");
        modelAndView.addObject("openType", openType);
        modelAndView.addObject("currentUserId", user.getId());
        modelAndView.addObject("currentUserName", user.getName());
        modelAndView.addObject("currentUserAccountName", user.getLoginAccountName());
        modelAndView.addObject("currentUserAccountId", user.getLoginAccount());
        return modelAndView;
    }

    /**
     * 公文督办查询---魏俊标
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listEdocSuperviseController(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/listSupervise");
        User user = AppContext.getCurrentUser();
        int edocType = request.getParameter("edocType") == null ? 0
                : Integer.parseInt(request.getParameter("edocType"));
        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == edocType) {// 收文
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);
        modelAndView.addObject("hasRegistButton", hasRegistButton);
        // 公文发起权与公文交换权
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(edocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        // 收文登记与分发权限
        boolean isEdocCreateRegister = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                EdocEnum.edocType.recEdoc.ordinal());
        boolean isEdocDistribute = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                EdocEnum.edocType.distributeEdoc.ordinal());
        modelAndView.addObject("isEdocCreateRegister", isEdocCreateRegister);
        modelAndView.addObject("isEdocDistribute", isEdocDistribute);
        // 是否显示左侧导航

        if ("false".equals(SystemProperties.getInstance().getProperty("edoc.Supervise"))) {
            modelAndView.addObject("isAgent", true);
        }
        return modelAndView;
    }

    /**
     * 在办公文列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listZcdb(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute("transmitSendAtts");

        // 不明白两变量的作用是什么，后面没有使用
        // List <V3xOrgMember> mems=EdocRoleHelper.getAccountExchangeUsers();
        // List <V3xOrgMember>
        // memsd=EdocRoleHelper.getDepartMentExchangeUsers();

        // boolean isEdocAdmin=EdocRoleHelper.isAccountExchange();
        // isEdocAdmin=EdocRoleHelper.isDepartmentExchange();
        // String ids=EdocRoleHelper.getUserExchangeDepartmentIds();

        String condition = request.getParameter("condition");

        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");

        String edocType = request.getParameter("edocType");
        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        // GOV-4012 收文管理-待阅，暂存待办事项后，从待阅列表消失，跑到待办-在办列表中了
        // changyi 收文在办，需要加上待办的类型
        if ("1".equals(edocType)) {
            params.put("processType", 1);
        }

        ModelAndView modelAndView = new ModelAndView("edoc/listZcdb");

        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());

        /*************** 测试代码开始 *****************/
        // Long metadataId=7971699406983548687L;
        // edocManager.useMetadataValue(user.getLoginAccount(),metadataId,"22");
        /*************** 测试代码结束 *****************/

        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned()) {
            /********** 组合查询组件调用开始 *****************/
            String comb_condition = request.getParameter("comb_condition"); // 组合查询标识
            if (comb_condition != null && "1".equals(comb_condition)) {
                EdocSearchModel em = new EdocSearchModel();
                bind(request, em);
                queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_pending.key(),
                        SubStateEnum.col_pending_ZCDB.getKey());
                modelAndView.addObject("combQueryObj", em); // 设置的查询条件还原到页面
                modelAndView.addObject("combCondition", "1");
            } else
            /*************** 组合查询组件调用结束 ************/
                if (condition != null) {
                    if ("cusReceiveTime".equals(condition)) { // 如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
                        condition = "receiveTime";
                        String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
                        textfield = textfield_condition[0];
                        textfield1 = textfield_condition[1];

                    }
                    queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                            StateEnum.col_pending.key(), params, SubStateEnum.col_pending_ZCDB.getKey());
                }

            if (queryList != null) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                List<EdocSummaryModel> pendingList = edocManager.queryByCondition(iEdocType, "", null, null,
                        StateEnum.col_pending.key(), params, SubStateEnum.col_pending_ZCDB.getKey());
                ;
                modelAndView.addObject("pendingList", pendingList);
            }
        } else {
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        // Metadata comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);

        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);
        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);

        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        modelAndView.addObject("hasRegistButton", hasRegistButton);
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);

        // GOV-4749
        // 收文待办列表，【全部】和【待办】页签下均有"转发文"操作，唯独【在办】页签下没有，而实际上在办收文也是可以转发文的，不符合逻辑。
        /********* 转发文 **********/
        modelAndView.addObject("newForwardaRrticle", "edoc.new.type.forwardarticle");

        // 发文的拟文权限
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        return modelAndView;
    }

    /**
     * 待办登记公文列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listRegisterPending(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // String condition = request.getParameter("condition");
        // String textfield = request.getParameter("textfield");
        // String textfield1 = request.getParameter("textfield1");

        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        ModelAndView mav = new ModelAndView("edoc/listRegisterPending");
        List<EdocRecieveRecord> list = recieveEdocManager.getWaitRegisterEdocRecieveRecords(userId);
        if (list != null) {

            for (EdocRecieveRecord r : list) {
                EdocSummary summary = edocSummaryManager.findById(r.getEdocId());
                r.setCopies(summary == null ? null : summary.getCopies());
            }
        }

        mav.addObject("edocType", EdocEnum.edocType.recEdoc.ordinal());
        mav.addObject("controller", "edocController.do");
        mav.addObject("newEdoclabel", "edoc.new.type.rec");

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);

        mav.addObject("colMetadata", colMetadata);
        mav.addObject("list", pagenate(list));
        return mav;
    }

    private <T> List<T> pagenate(List<T> list) {
        if (null == list || list.size() == 0)
            return new ArrayList<T>();
        Integer first = Pagination.getFirstResult();
        Integer pageSize = Pagination.getMaxResults();
        Pagination.setRowCount(list.size());
        List<T> subList = null;
        if (first + pageSize > list.size()) {
            subList = list.subList(first, list.size());
        } else {
            subList = list.subList(first, first + pageSize);
        }
        return subList;
    }

    /**
     * 已发公文列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listSent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");
        long subEdocType = Strings.isBlank(request.getParameter("subType")) ? -1
                : Long.parseLong(request.getParameter("subType"));
        String edocType = request.getParameter("edocType");
        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        ModelAndView modelAndView = new ModelAndView("edoc/listSent");

        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());
        // 时间分类查询（本日，昨日，本周等）
        String listType = request.getParameter("listType");
        int listTypeInt = 0;
        if (listType != null && !"".equals(listType)) {
            listTypeInt = Integer.parseInt(listType);
            condition = "createDate"; // 收文的创建时间createDate
            String[] dateFields = com.seeyon.v3x.edoc.util.DateUtil.getTimeTextFiledByTimeEnum(listTypeInt);
            textfield = dateFields[0];
            textfield1 = dateFields[1];
        }
        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned()) {
            if (condition != null) {
                queryList = edocManager.queryByCondition1(iEdocType, condition, textfield, textfield1, subEdocType,
                        StateEnum.col_sent.key(), params);
            }
            if (queryList != null) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                List<EdocSummaryModel> finishedList = edocManager.querySentList(iEdocType, subEdocType, params);
                modelAndView.addObject("pendingList", finishedList);
            }
        } else {
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);

        // Metadata comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);

        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);

        /*
         * 元数据中加载 秘密程度 cy add
         */
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        /*
         * 元数据中加载 紧急程度 cy add
         */
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);

        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);

        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.element.receive.distribute");
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);

        // 发文的拟文权限
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        modelAndView.addObject("currentUserId", AppContext.getCurrentUser().getId());
        // OA-31123 收文：已发、待办、已办小查询条件不要：成文单位，此为原G6的条件
        modelAndView.addObject("isG6", EdocHelper.isG6Version());

        return modelAndView;
    }

    /**
     * 公文草稿箱/退件箱/退稿箱
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listWaitSend(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");
        // String subStateStr = request.getParameter("subState");
        long subEdocType = Strings.isBlank(request.getParameter("subType")) ? -1
                : Long.parseLong(request.getParameter("subType"));
        String edocType = request.getParameter("edocType");
        String list = request.getParameter("list");
        int iEdocType = Strings.isBlank(request.getParameter("edocType")) ? -1
                : Integer.parseInt(request.getParameter("edocType"));
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        ModelAndView modelAndView;
        if ("backBox".equals(list) || "retreat".equals(list)) {// backBox发文退稿箱,retreat收文退件箱
            modelAndView = new ModelAndView("edoc/listBackBox");// 跳转到退稿想
        } else {
            modelAndView = new ModelAndView("edoc/listWaitSend");// 跳转到草稿箱
        }
        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());

        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned()) {
            // 1：退件箱2：草稿箱，收文、发文一样
            int type = -1;
            if ("draftBox".equals(list)) {// 草稿箱
                type = 2;
            } else if ("backBox".equals(list) || "retreat".equals(list)) { // backBox发文退稿箱,retreat收文退件箱
                type = 1;
            } else if ("listWaitSend".equals(list)) {// 待发（包括草稿箱和退件箱）
                type = 3;
            } else {
                type = 3;
            }
            if (condition != null && (Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1))) {// 判断查询条件是否为空
                int sub_state = 0;// 默认显示原来的状态
                if (("backBox".equals(list) && "backBox".equals(condition))
                        || "retreat".equals(list) && "retreat".equals(condition)) {// 获取退回方式状态
                    String s_State = request.getParameter("textfield");
                    if (Strings.isNotBlank(s_State)) {// 判断是否为空
                        sub_state = Integer.parseInt(s_State);// 转换
                    }
                }
                if (sub_state != 0) {
                    queryList = edocManager.queryByCondition1(iEdocType, condition, textfield, textfield1, subEdocType,
                            StateEnum.col_waitSend.key(), params, sub_state);
                } else {
                    // GOV-4655 公文管理-签报管理，拟文发起后，发起人在已办内删除，下一节点回退，拟文消失。
                    // 表示是退稿箱列表backBox，在queryDraftList方法中
                    // 需要查询affair表中包含删除的，这样退搞箱中才能显示出在已发中删除的
                    params.put("sendListType", list);
                    queryList = edocManager.queryByCondition1(iEdocType, condition, textfield, textfield1, subEdocType,
                            StateEnum.col_waitSend.key(), params, -1, -1, 1, type);
                }
            }
            if (queryList != null) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                modelAndView.addObject("equal", type);
                // GOV-4655 公文管理-签报管理，拟文发起后，发起人在已办内删除，下一节点回退，拟文消失。
                // 表示是退稿箱列表backBox，在queryDraftList方法中
                // 需要查询affair表中包含删除的，这样退搞箱中才能显示出在已发中删除的
                params.put("sendListType", list);
                List<EdocSummaryModel> finishedList = edocManager.queryDraftList(iEdocType, subEdocType, params, -1, -1,
                        1, type);
                modelAndView.addObject("pendingList", finishedList);
            }
        } else {// 有代理人
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);

        CtpEnumBean comImportanceMetadata = enumManagerNew.getEnum(EnumNameEnum.common_importance.name());

        modelAndView.addObject("comImportanceMetadata", comImportanceMetadata);

        /*
         * 元数据中加载 秘密程度 cy add
         */
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        /*
         * 元数据中加载 紧急程度 cy add
         */
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);
        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.element.receive.distribute");
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        int roleEdocType = iEdocType == 1 ? 3 : iEdocType;
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        return modelAndView;
    }

    public ModelAndView listIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/listIndex");
        // 会议纪要转公文 获得会议纪要ID
        String meetingSummaryId = request.getParameter("meetingSummaryId");
        User user = AppContext.getCurrentUser();
        int edocType = Strings.isBlank(request.getParameter("edocType")) ? 0
                : Integer.parseInt(request.getParameter("edocType"));
        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == edocType) {// 收文
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);
        modelAndView.addObject("openFrom", request.getParameter("openFrom"));
        modelAndView.addObject("hasRegistButton", hasRegistButton);
        modelAndView.addObject("registerType", request.getParameter("registerType"));
        // 公文发起权与公文交换权
        int roleEdocType = edocType == 1 ? 3 : edocType;

        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(roleEdocType);
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();

        String model = request.getParameter("from");
        // GOV-4369 公文人员的发起权限被取消后，点击拟文报网页错误
        // GOV-4722.公文管理-代理人；A设置B为代理人，某公文中A的节点权限是封发（A有全部公文权限，B没有收文相关权限），那么B在代处理该公文时到
        // 收文分发 这一步，在待办事项内点击该待办，页面会一直处在跳转中 start
        if (!isEdocCreateRole) {
            if (!"agent".equals(request.getParameter("app")) && null != request.getParameter("app")) {// 没有公文发起权不能发送
                String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "alert_not_edoccreate");
                StringBuffer sb = new StringBuffer();
                if ("newEdoc".equals(model) && edocType != 1) {
                    sb.append("alert('" + errMsg + "');");
                    sb.append("location.href='edocController.do?method=listIndex&edocType=" + edocType
                            + "&list=listSent&from=listSent" + Functions.csrfSuffix() + "';");
                    rendJavaScript(response, sb.toString());
                    return null;
                } else if ("listDistribute".equals(model) && edocType == 1) {
                    sb.append("alert('" + errMsg + "');");
                    sb.append("location.href='edocController.do?method=listIndex&edocType=" + edocType
                            + "&list=listPending&from=listPending" + Functions.csrfSuffix() + "';");
                    rendJavaScript(response, sb.toString());
                    return null;
                }
            }
        }

        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        // 收文登记与分发权限
        boolean isEdocCreateRegister = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                EdocEnum.edocType.recEdoc.ordinal());
        boolean isEdocDistribute = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                EdocEnum.edocType.distributeEdoc.ordinal());
        modelAndView.addObject("isEdocCreateRegister", isEdocCreateRegister);
        modelAndView.addObject("isEdocDistribute", isEdocDistribute);
        modelAndView.addObject("meetingSummaryId", meetingSummaryId);

        modelAndView.addObject("isOpenRegister", EdocSwitchHelper.isOpenRegister());
        modelAndView.addObject("showBanwenYuewen", EdocSwitchHelper.showBanwenYuewen(user.getLoginAccount()));
        return modelAndView;
    }

    public ModelAndView listLeft(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/listLeft");
        // int edocType = request.getParameter("edocType")==null? 0 :
        // Integer.parseInt(request.getParameter("edocType"));
        User user = AppContext.getCurrentUser();
        // 获取单位ID
        // 根据发文或收文，获取其节点权限列表
        // 获得当前是 发文还是收文

        String from = request.getParameter("from");
        String edocType = request.getParameter("edocType");
        int etype = Integer.parseInt(edocType);
        int iEdocType = -1;
        if ("listPending".equals(from)) {
            if ("0".equals(edocType)) {
                iEdocType = 1; // 发文待办
            } else {
                iEdocType = 3; // 收文待办
            }
        } else if ("listDone".equals(from)) {
            if ("0".equals(edocType)) {
                iEdocType = 2; // 发文已办
            } else {
                iEdocType = 4; // 收文已办
            }
        } else if ("listReaded".equals(from)) { // 已阅
            iEdocType = 5;
        }

        modelAndView.addObject("edocType", etype);

        // 节点权限
        List<Permission> nodeList = null;
        Map<Long, String> nodeMap = new HashMap<Long, String>();

        // 用户已勾选 需要显示的 类别列表
        Map map = edocCustomerTypeManager.getUserCustomerTypeList(user, iEdocType, null, Integer.parseInt(edocType));
        List<EdocCustomerType> customerTypeList = (List<EdocCustomerType>) map.get("customerTypeList");
        List<EdocCustomerType> customerTypeList_new = new ArrayList<EdocCustomerType>();
        int count = 0;
        // 查询条件的set
        if (customerTypeList != null && customerTypeList.size() > 0) {
            for (EdocCustomerType edocCustomerType : customerTypeList) {
                String typeCode = edocCustomerType.getTypeCode();
                String[] conditionArray = typeCode.split("_");
                String condition = conditionArray[0];
                String conditionvalue = conditionArray[1];
                edocCustomerType.setCondition(condition);
                edocCustomerType.setTextfield(conditionvalue);
                // 公文元素名称（紧急程度，密级等）
                if (edocCustomerType.getBigTypeId() == 4) {
                    if ("urgentLevel".equals(condition)) {
                        edocCustomerType.setEdocElementLabel("edoc.element.urgentlevel");
                    } else if ("secretLevel".equals(condition)) {
                        edocCustomerType.setEdocElementLabel("edoc.element.secretlevel");
                    } else if ("sendType".equals(condition)) {
                        edocCustomerType.setEdocElementLabel("edoc.element.sendtype");
                    }
                }
                // GOV-4408 公文管理-发文管理，收文管理，自定义分类，在后台增加节点权限后，在前台设置显示，后台删除，前台依然保留
                // -----start
                // 发文流程节点
                if (edocCustomerType.getBigTypeId() == 0 && count == 0) {
                    count++;
                    nodeList = permissionManager.getPermissionsByCategory("edoc_send_permission_policy",
                            user.getLoginAccount());
                    for (Permission perm : nodeList) {
                        nodeMap.put(perm.getFlowPermId(), "");
                    }
                }
                // 收文流程节点
                if (edocCustomerType.getBigTypeId() == 1 && count == 0) {
                    count++;
                    nodeList = permissionManager.getPermissionsByCategory("edoc_rec_permission_policy",
                            user.getLoginAccount());
                    for (Permission perm : nodeList) {
                        nodeMap.put(perm.getFlowPermId(), "");
                    }
                }
                if (edocCustomerType.getBigTypeId() == 0 || edocCustomerType.getBigTypeId() == 1) {
                    if (nodeMap.get(edocCustomerType.getTypeId()) == null)
                        continue;
                }
                // GOV-4408 公文管理-发文管理，收文管理，自定义分类，在后台增加节点权限后，在前台设置显示，后台删除，前台依然保留
                // -----end

                customerTypeList_new.add(edocCustomerType);
            }
        }
        List<EdocCategory> edocCategoryList = edocCategoryManager.getCategoryByAccount(user.getLoginAccount());
        modelAndView.addObject("edocCategoryList", edocCategoryList);
        modelAndView.addObject("customerTypeList", customerTypeList_new);
        modelAndView.addObject("isEdocAdmin", EdocRoleHelper.isAccountEdocAdmin());
        // modelAndView.addObject("isEdocAdmin", true);
        // 发文草稿箱标志 subState=1
        modelAndView.addObject("sendBackState", SubStateEnum.col_waitSend_draft.getKey());
        // 收文退稿箱标志，目前收文待发分为草稿箱和退件箱，退件箱的subState=4
        modelAndView.addObject("stepBackState", SubStateEnum.col_waitSend_stepBack.getKey());
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(etype);
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        return modelAndView;
    }

    /**
     * 发送登记单显示请求-唐4桂林2011-10-12
     */
    public ModelAndView edocRegisterDetail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ModelAndView modelAndView = new ModelAndView("edoc/edocRegisterDetail");
            Long registerId = null;
            if (!Strings.isBlank(request.getParameter("registerId"))) {
                registerId = Long.parseLong(request.getParameter("registerId"));
            }
            if (registerId == null) {
                // throw new ColException(msg);
            }
            String list = request.getParameter("list");
            if (!Strings.isNotBlank(list)) {
                list = "popup";
            }
            // 有可能公文已经处理完了，但是消息提示框仍然在哪里，所以需要重新查询一下当前事项的状态。
            String from = request.getParameter("from");
            modelAndView.addObject("registerId", registerId);
            modelAndView.addObject("list", list);
            modelAndView.addObject("from", from);
            modelAndView.addObject("controller", "edocController.do");
            return modelAndView;
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(e.getMessage()) + "\")");
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"true\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.getA8Top().reFlesh();");
            sb.append("}");
            sb.append("");
            rendJavaScript(response, sb.toString());
            return null;
        }
    }

    /**
     * 显示登记单信息，包括附件和正文及登记单-唐桂林2011-10-12
     */
    public ModelAndView edocRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ModelAndView modelAndView = new ModelAndView("edoc/edocRegister");
            long registerId = request.getParameter("registerId") == null ? -1L
                    : Long.parseLong(request.getParameter("registerId"));
            EdocRegister edocRegister = edocRegisterManager.getEdocRegister(registerId);
            if (edocRegister == null) {
                StringBuffer sb = new StringBuffer();
                sb.append("alert(\"" + ResourceUtil.getString("edoc.alert.flow.registStepback") + "\");");// 登记已被回退!
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("}else{");
                sb.append("  window.close();");
                sb.append("}");
                sb.append("");
                rendJavaScript(response, sb.toString());
                return null;
            }

            // 已登记和待分发，关联发文id都用登记id
            long relationId = registerId;
            // if(edocRegister.getRegisterType()==1){
            // relationId = edocRegister.getEdocId();
            // }
            int forward = 0;
            String forwardType = request.getParameter("forwardType");
            if ("registered".equals(forwardType)) {
                forward = 1;
            } else if ("waitSent".equals(forwardType)) {
                forward = 2;
            }

            User user = AppContext.getCurrentUser();
            /********* 关联发文 * 收文的Id,收文的Type *********/
            String relSends = "haveNot";
            // String relRecs = "haveNot";
            // List<EdocSummary> newEdocList =
            // this.edocSummaryRelationManager.findNewEdoc(registerId,
            // user.getId(), 1);
            List<EdocSummary> newEdocList = this.edocSummaryRelationManager
                    .findNewEdocByRegisteredOrWaitSent(relationId, user.getId(), 1, forward);// 已登记
            // 如果已登记下还没有关联发文，需要获得待登记下关联的发文 (需要在已登记列表查看时，待分发就不用了)
            if (newEdocList == null && forward == 1) {
                // 获得签收id,如果是纸质公文就不用了
                long recieveId = edocRegister.getRecieveId();
                if (recieveId != -1) {
                    newEdocList = this.edocSummaryRelationManager.findNewEdoc(recieveId, user.getId(), 1);
                }
            }

            if (newEdocList != null) {
                relSends = "haveMany";
                // modelAndView.addObject("recEdocId",registerId);
                modelAndView.addObject("recEdocId", relationId);
                modelAndView.addObject("recType", 1);
                modelAndView.addObject("relSends", relSends);
            }
            /********* 关联 发文的Id 发文的Type **********/

            RegisterBody registerBody = edocRegisterManager.findRegisterBodyByRegisterId(registerId);
            modelAndView.addObject("edocType", edocRegister.getEdocType());
            modelAndView.addObject("edocId", edocRegister.getEdocId());
            modelAndView.addObject("controller", "edocController.do");
            // 是否允许拟文人修改附件。
            boolean allowUpdateAttachment = EdocSwitchHelper.allowUpdateAttachment();
            modelAndView.addObject("allowUpdateAttachment", allowUpdateAttachment);
            modelAndView.addObject("firstPDFId", registerBody != null ? registerBody.getContent() : "");
            // 只查找正文的附件。
            modelAndView.addObject("attachments", attachmentManager.getByReference(registerId, registerId));
            modelAndView.addObject("bean", edocRegister);
            modelAndView.addObject("registerBody", registerBody);
            modelAndView.addObject("curUser", AppContext.getCurrentUser());
            if (registerBody != null) {
                if (registerBody.getCreateTime() == null) {
                    registerBody.setCreateTime(edocRegister.getCreateTime());
                    registerBodyManager.updateReigsterBody(registerBody);
                }
            }

            StringBuffer contentBuffer = registerBodyManager.getRegisterContent(request, registerBody);
            // GOV-4861.公文管理，收文登记节点，标准格式的正文，如果没有内容，查看正文时，显示NULL start
            if (registerBody != null) {
                modelAndView.addObject("content",
                        Strings.isBlank(registerBody.getContent()) ? "" : contentBuffer.toString());
                // GOV-4861.公文管理，收文登记节点，标准格式的正文，如果没有内容，查看正文时，显示NULL end
                if (!"HTML".equals(registerBody.getContentType())) {
                    modelAndView.addObject("contentNo", registerBody.getContent());
                }
            }
            return modelAndView;
        } catch (Exception e) {
            // LOGGER.error("显示登记单信息抛出异常：",e);
            StringBuffer sb = new StringBuffer();
            sb.append("alert(\"" + ResourceUtil.getString("edoc.alert.flow.registStepback") + "\");");// 登记已被回退!
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"true\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.getA8Top().reFlesh();");
            sb.append("}");
            sb.append("");
            rendJavaScript(response, sb.toString());
            return null;
        }
    }

    /**
     * 显示登记单-唐桂林2011-10-12
     */
    public ModelAndView edocRegisterFormDetail(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            User user = AppContext.getCurrentUser();
            ModelAndView mav = new ModelAndView("edoc/edocRegisterFormDetail");
            long registerId = request.getParameter("registerId") == null ? -1L
                    : Long.parseLong(request.getParameter("registerId"));
            EdocRegister edocRegister = edocRegisterManager.getEdocRegister(registerId);

            /********* puyc 关联发文 * 收文的Id,收文的Type *********/
            String relSends = "haveNot";
            List<EdocSummary> newEdocList = this.edocSummaryRelationManager.findNewEdoc(edocRegister.getEdocId(),
                    user.getId(), 1);
            if (newEdocList != null) {
                relSends = "haveMany";
                mav.addObject("recEdocId", edocRegister.getEdocId());
                mav.addObject("recType", 1);
                mav.addObject("relSends", relSends);

            }
            /*********** puyc end ************/

            // lijl添加,修改bug
            // 40928(登记时收文编号与手工录入的编号不一致)-----------------------------Start
            if (edocRegister != null) {
                String serialNo = edocRegister.getSerialNo();
                if (serialNo != null && !"".equals(serialNo)) {
                    try {
                        // -5503164321234926552|内部文号〔2014〕0001号|1|1
                        String serialNo4Display = serialNo;
                        serialNo4Display = serialNo4Display.substring(serialNo4Display.indexOf("|") + 1);
                        serialNo4Display = serialNo4Display.substring(0, serialNo4Display.indexOf("|"));
                        edocRegister.setSerialNo(serialNo4Display);
                    } catch (Exception ex) {
                        edocRegister.setSerialNo(serialNo);
                    }
                }
            }
            // lijl添加,修改bug
            // 40928(登记时收文编号与手工录入的编号不一致)-----------------------------End
            mav.addObject("bean", edocRegister);
            mav.addObject("controller", "edocController.do");
            // RegisterBody body = edocRegister.getRegisterBody();
            mav.addObject("edocType", edocRegister.getEdocType());
            mav.addObject("curUser", AppContext.getCurrentUser());
            CtpEnumBean sendUnitTypeData = enumManagerNew.getEnum(EnumNameEnum.send_unit_type.name());// 来文类别
            CtpEnumBean edocSecretLevelData = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());// 密级程度
            CtpEnumBean edocUrgentLevelData = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());// 紧急程度
            CtpEnumBean edocKeepPeriodData = enumManagerNew.getEnum(EnumNameEnum.edoc_keep_period.name());// 保密期限

            CtpEnumBean edocTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_type.name());// 公文种类
            CtpEnumBean edocUnitLevelData = enumManagerNew.getEnum(EnumNameEnum.edoc_unit_level.name());// 公文级别
            mav.addObject("edocTypeMetadata", edocTypeMetadata);

            List<EdocMarkModel> serialNoList = edocMarkDefinitionManager
                    .getEdocMarkDefinitions(user.getDepartmentId() + "," + user.getAccountId(), 1);
            mav.addObject("sendUnitTypeData", sendUnitTypeData);
            mav.addObject("edocSecretLevelData", edocSecretLevelData);
            mav.addObject("edocUrgentLevelData", edocUrgentLevelData);
            mav.addObject("edocKeepPeriodData", edocKeepPeriodData);
            mav.addObject("serialNoList", serialNoList);
            mav.addObject("edocUnitLevelData", edocUnitLevelData);
            return mav;
        } catch (Exception e) {
            LOGGER.error("显示登记单抛异常：", e);
            StringBuffer sb = new StringBuffer();
            sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(e.getMessage()) + "\")");
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"true\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.getA8Top().reFlesh();");
            sb.append("}");
            sb.append("");
            rendJavaScript(response, sb.toString());
            return null;
        }

    }

    /**
     * 显示登记单-唐桂林2011-10-12
     */
    public ModelAndView selectDistributer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        ModelAndView mav = new ModelAndView("edoc/selectDistributer");
        mav.addObject("controller", "edocController.do");
        mav.addObject("curUser", user);
        return mav;
    }

    /**
     * 批处理-唐桂林2011-10-12
     */
    public ModelAndView batchRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String registerType = request.getParameter("registerType");
        long distributerId = request.getParameter("distributerId") == null ? user.getId()
                : Long.parseLong(request.getParameter("distributerId"));
        String distributer = request.getParameter("distributer") == null ? user.getName()
                : request.getParameter("distributer");
        String[] recieveIds = request.getParameterValues("recieveId");
        RegisterBody registerBody = null;
        EdocRegister edocRegister = null;
        EdocRecieveRecord edocRecieveRecord = null;
        EdocSummary summary = null;
        EdocBody edocBody = null;
        if (recieveIds != null) {
            for (int i = 0; i < recieveIds.length; i++) {
                edocRegister = new EdocRegister();
                // 来文信息
                long recieveId = Long.parseLong(recieveIds[i]);
                edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(recieveId);
                if (edocRecieveRecord == null) {
                    return null;
                }
                edocRegister.setEdocId(edocRecieveRecord.getEdocId());
                edocRegister.setRecieveId(recieveId);
                edocRegister.setRecTime(edocRecieveRecord.getRecTime());
                // 装载数据到登记表
                edocRegister.setNewId();
                edocRegister.setCreateUserId(user.getId());
                edocRegister.setCreateUserName(user.getName());
                edocRegister.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
                edocRegister.setUpdateTime(new java.sql.Timestamp(new Date().getTime()));
                edocRegister.setOrgAccountId(user.getAccountId());
                edocRegister.setState(EdocNavigationEnum.RegisterState.Registed.ordinal());
                // 设置登记人
                edocRegister.setRegisterUserId(user.getId());
                edocRegister.setRegisterUserName(user.getName());
                // 设置默认分发人
                edocRegister.setDistributerId(distributerId);
                edocRegister.setDistributer(distributer);
                edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());
                edocRegister.setRegisterType(Integer.parseInt(registerType));
                edocRegister.setRegisterDate(new java.sql.Date(new Date().getTime()));
                V3xOrgAccount account = orgManager.getAccountById(user.getLoginAccount());
                edocRegister.setSendUnit(account == null ? "" : account.getName());
                edocRegister.setSendUnitId(1l);
                edocRegister.setSendUnitType(edocRecieveRecord.getSendUnitType());
                edocRegister.setEdocType(EdocEnum.edocType.recEdoc.ordinal());

                summary = edocManager.getEdocSummaryById(edocRecieveRecord.getEdocId(), true);

                edocRegister.setDocType(edocRecieveRecord.getDocType());
                edocRegister.setSubject(edocRecieveRecord.getSubject());
                edocRegister.setDocMark(edocRecieveRecord.getDocMark());
                edocRegister.setSecretLevel(edocRecieveRecord.getSecretLevel());
                edocRegister.setUrgentLevel(edocRecieveRecord.getUrgentLevel());
                // 公文元素基本信息
                edocRegister.setSerialNo(null);
                edocRegister.setIdentifier("00000000000000000000");
                edocRegister.setKeepPeriod(summary == null ? "" : String.valueOf(summary.getKeepPeriod()));
                edocRegister.setSendType(summary == null ? "1" : summary.getSendType());
                edocRegister.setKeywords(summary == null ? "" : summary.getKeywords());
                edocRegister.setIssuerId(-1l);
                edocRegister.setIssuer(summary == null ? "" : summary.getIssuer());
                edocRegister.setEdocDate(summary == null ? null : summary.getSigningDate());
                /*
                 * if(edocRegister.getEdocDate()==null) {//如果没有签发时间，则显示为封发时间
                 * edocRegister.setEdocDate(summary==null ? null : new
                 * java.sql.Date(summary.getPackTime().getTime())); }
                 */
                // 附件信息
                List<Attachment> attachmentList = null;
                // 装载公文正文
                if (summary != null && edocRecieveRecord != null) {
                    edocBody = summary.getBody(edocRecieveRecord.getContentNo().intValue());
                    registerBody = new RegisterBody();
                    edocBody = edocBody == null ? summary.getFirstBody() : edocBody;
                    if (null != edocBody) {
                        if ("HTML".equals(edocBody.getContentType())) {
                            registerBody.setContent(edocBody.getContent());
                            registerBody.setContentNo(edocBody.getContentNo());
                            registerBody.setContentType(edocBody.getContentType());
                        } else {
                            registerBody.setContent(Long.toString(UUIDLong.longUUID()));
                            registerBody.setContentType(edocBody.getContentType());
                            registerBody.setCreateTime(new java.sql.Timestamp(new Date().getTime()));
                        }
                        registerBody.setIdIfNew();
                    }
                    // 增加正文文件和v3x_file
                    try {
                        if (!"HTML".equals(edocBody.getContentType())) {
                            // 复制正文及复制v3x_file表数据
                            V3XFile file = fileManager.clone(Long.parseLong(edocBody.getContent()), true);
                            file.setFilename(file.getId().toString());
                            fileManager.update(file);
                            registerBody.setContent(file.getId().toString());
                            // 复制印章什么的？
                        }
                        // 增加附件
                        this.attachmentManager.copy(summary.getId(), summary.getId(), edocRegister.getId(),
                                edocRegister.getId(), ApplicationCategoryEnum.edocRegister.ordinal());// 附件
                        attachmentList = attachmentManager.getByReference(edocRegister.getId(), edocRegister.getId());
                        /*
                         * for(int k=0; k<attachmentList.size(); k++) {
                         * fileManager.clone(attachmentList.get(k).getFileUrl(),
                         * attachmentList.get(k).getCreatedate(),
                         * edocRegister.getId(), new java.util.Date()); }
                         */
                        edocRegister.setHasAttachments(attachmentList.size() > 0);
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }

                }
                if (summary != null && !Strings.isBlank(summary.getSendUnit())
                        && !Strings.isBlank(summary.getSendUnitId())) {
                    edocRegister.setEdocUnit(summary.getSendUnit());
                    edocRegister.setEdocUnitId(summary.getSendUnitId());
                }
                if (registerBody != null) {
                    registerBody.setEdocRegister(edocRegister);
                    edocRegister.setRegisterBody(registerBody);
                }
                edocRegisterManager.createEdocRegister(edocRegister);

                // 修改签收单
                edocRecieveRecord.setRegisterUserId(edocRegister.getRegisterUserId());
                edocRecieveRecord.setRegisterTime(new java.sql.Timestamp(edocRegister.getRegisterDate().getTime()));
                edocRecieveRecord.setRegisterName(edocRegister.getRegisterUserName());
                edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
                recieveEdocManager.update(edocRecieveRecord);
                if (edocRegister.getState() == EdocNavigationEnum.RegisterState.Registed.ordinal()) {
                    // 去掉待登记z
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("app", ApplicationCategoryEnum.edocRegister.getKey());
                    params.put("objectId", edocRecieveRecord.getEdocId());
                    List<CtpAffair> affairList = affairManager.getByConditions(null, params);
                    if (affairList != null) {
                        for (int j = 0; j < affairList.size(); j++) {
                            affairList.get(j).setState(StateEnum.edoc_exchange_registered.getKey());
                            affairList.get(j).setCompleteTime(new Timestamp(new Date().getTime()));
                            affairManager.updateAffair(affairList.get(j));
                        }
                    }
                }

                // 发送消息
                if (edocRegister.getState() == EdocNavigationEnum.RegisterState.Registed.ordinal()) {
                    sendMessageToRegister(user, -1, edocRegister, "create", -1);
                }
            }
        }

        String listType = request.getParameter("listType");
        return super.redirectModelAndView("/edocController.do?method=listRegister&listType=" + listType
                + "&from=listRegister&edocType=" + edocRegister.getEdocType());
    }

    /**
     * 登记列表 --唐桂林20110923
     */
    public ModelAndView listRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
        ModelAndView mav = new ModelAndView("edoc/listRegister");
        User user = AppContext.getCurrentUser();
        int edocType = request.getParameter("edocType") == null ? 1
                : Integer.parseInt(request.getParameter("edocType"));
        // GOV-4808.安全测试：aqc没有单位公文管理员-收文管理-登记权限，输入地址却能访问对应界面 start
        if ((1 != edocType) || (!"true".equals(isG6) && !EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(),
                user.getId(), EdocEnum.edocType.recEdoc.ordinal()))) {
            return refreshWorkspace();
        }
        // GOV-4808.安全测试：aqc没有单位公文管理员-收文管理-登记权限，输入地址却能访问对应界面 end
        String defaultListType = Strings.isNotBlank(request.getParameter("list")) ? request.getParameter("list")
                : EdocNavigationEnum.RegisterListState.registerPending.getKey();
        // String listType = Strings.isBlank(request.getParameter("listType")) ?
        // defaultListType : request.getParameter("listType");

        // G6-V5版本中 将以前G6中的登记待发和已登记从左侧树
        // 改为页签，故需要在listIndex.jsp和edocFrame.jsp中增加recListType参数传递
        String listType = Strings.isBlank(request.getParameter("recListType")) ? defaultListType
                : request.getParameter("recListType");

        // 登记状态 0草稿箱 1未登记 2已登记 3退回到签收 4退件箱 5被删除
        int state = EdocNavigationEnum.RegisterListState.getEnumByKey(listType).getValue();
        // 登记类型 1自动 2纸质 3二维码
        int registerType = EdocNavigationEnum.RegisterListType.getEnumByKey(listType).getValue();

        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("conditionKey", request.getParameter("condition"));
        condition.put("textfield", request.getParameter("textfield"));
        condition.put("textfield1", request.getParameter("textfield1"));
        condition.put("edocType", edocType);
        condition.put("userId", AppContext.getCurrentUser().getId());
        condition.put("orgAccountId", AppContext.getCurrentUser().getLoginAccount());
        condition.put("registerType", registerType);

        List<EdocRegister> list = new ArrayList<EdocRegister>();
        if (state == EdocNavigationEnum.RegisterState.DraftBox.ordinal()
                || state == EdocNavigationEnum.RegisterState.Registed.ordinal()
                || state == EdocNavigationEnum.RegisterState.retreat.ordinal()) {
            list = edocRegisterManager.findEdocRegisterList(state, condition);
        } else {
            // 目前待登记里就显示自动登记
            if (registerType == EdocNavigationEnum.RegisterType.ByAutomatic.ordinal()
                    || registerType == EdocNavigationEnum.RegisterType.All.ordinal()) {
                List<EdocRecieveRecord> recieveList = recieveEdocManager.findWaitEdocRegisterList(state, condition);
                if (recieveList != null) {
                    EdocRegister edocRegister = null;
                    RegisterBody registerBody = null;
                    EdocSummary summary = null;
                    V3xOrgAccount account = null;
                    V3xOrgDepartment dept = null;
                    for (EdocRecieveRecord r : recieveList) {
                        long sendUnitId = r.getExchangeOrgId();
                        if (r.getExchangeType() == 2) {
                            dept = orgManager.getDepartmentById(r.getExchangeOrgId());
                            if (dept != null) {
                                sendUnitId = dept.getOrgAccountId();
                            }
                        }
                        account = orgManager.getAccountById(sendUnitId);
                        edocRegister = new EdocRegister();
                        edocRegister.setId(-1);
                        edocRegister.setExchangeType(r.getExchangeType());
                        edocRegister.setExchangeOrgId(r.getExchangeOrgId());
                        edocRegister.setSendUnitId(sendUnitId);
                        edocRegister.setIsRetreat(r.getIsRetreat());
                        edocRegister.setSendUnit(account == null ? "" : account.getName());
                        edocRegister.setEdocType(com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC);
                        edocRegister.setEdocUnit(r.getSendUnit());
                        edocRegister.setEdocId(r.getEdocId());
                        edocRegister.setRecieveId(r.getId());
                        edocRegister.setSubject(r.getSubject());
                        edocRegister.setDocMark(r.getDocMark());
                        edocRegister.setSecretLevel(r.getSecretLevel());
                        edocRegister.setUrgentLevel(r.getUrgentLevel());
                        edocRegister.setKeepPeriod(r.getKeepPeriod());
                        edocRegister.setCopies(r.getCopies());
                        edocRegister.setRegisterUserName(r.getRegisterName());
                        edocRegister.setRecTime(r.getRecTime());
                        edocRegister.setExchangeMode(r.getExchangeMode());

                        V3xOrgMember member = orgManager.getMemberById(r.getRecUserId());
                        if (member != null) {
                            edocRegister.setRecieveUserName(member.getName());
                        }
                        summary = edocSummaryManager.findById(r.getEdocId());
                        edocRegister.setSendTo(r.getSendTo());
                        // lijl添加if,如果来文单位为空则从summary中获取
                        if (edocRegister.getEdocUnit() == null) {
                            edocRegister.setEdocUnit(summary == null ? "" : summary.getSendUnit());
                        }
                        edocRegister.setKeywords(summary == null ? "" : summary.getKeywords());
                        edocRegister.setIssuer(summary == null ? "" : summary.getIssuer());
                        edocRegister.setIdentifier(summary == null ? "00000000000000000000" : summary.getIdentifier());
                        edocRegister.setState(1);
                        edocRegister.setRegisterUserId(r.getRegisterUserId());
                        edocRegister.setProxy(r.isProxy());
                        edocRegister.setProxyId(r.getProxyUserId() == null ? -1 : r.getProxyUserId());
                        edocRegister.setProxyLabel(r.getProxyLabel());
                        registerBody = new RegisterBody();
                        // GOV-4192.公文管理-收文管理-登记-待登记，正文为pdf的待登记的收文标题后图标还是显示的word图标，应该是pdf
                        // start
                        if (summary != null && summary.getEdocBodies() != null && summary.getEdocBodies().size() > 0) {
                            Iterator<EdocBody> it = summary.getEdocBodies().iterator();
                            EdocBody firstBody = null;
                            EdocBody pdfBody = null;
                            while (it.hasNext()) {
                                EdocBody body = (EdocBody) it.next();
                                if (body.getContentNo() == 0) {
                                    firstBody = body;
                                } else {
                                    pdfBody = body;
                                }
                            }
                            if (firstBody != null) {
                                registerBody.setContentType(
                                        pdfBody == null ? firstBody.getContentType() : pdfBody.getContentType());
                            }
                        } else {
                            registerBody.setContentType("HTML");
                        }
                        // GOV-4192.公文管理-收文管理-登记-待登记，正文为pdf的待登记的收文标题后图标还是显示的word图标，应该是pdf
                        // end
                        edocRegister.setRegisterBody(registerBody);
                        list.add(edocRegister);
                    }
                }
            }
        }
        /* xiangfan 添加 判断当前用户是否设置了收文-登记 的标题多行显示 start */
        if ("registerPending".equals(listType) || "listRegister".equals(listType)) {
            boolean hasSubjectWrap = false;
            hasSubjectWrap = this.edocSummaryManager.hasSubjectWrapRecordByCurrentUser(user.getAccountId(),
                    user.getId(), EdocSubjectWrapRecord.Edoc_Receive_Register, edocType);
            mav.addObject("hasSubjectWrap", hasSubjectWrap);
        }
        // 常用变量
        mav.addObject("state", state);
        mav.addObject("listType", listType);
        mav.addObject("registerType", registerType);
        mav.addObject("edocType", edocType);
        mav.addObject("controller", "edocController.do");
        // 收文登记与分发权限
        boolean canUpdateContent = EdocSwitchHelper.canUpdateAtOutRegist();
        boolean isEdocCreateRegister = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                EdocEnum.edocType.recEdoc.ordinal());
        boolean isEdocDistribute = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(),
                EdocEnum.edocType.distributeEdoc.ordinal());
        mav.addObject("isEdocCreateRegister", isEdocCreateRegister);
        mav.addObject("isEdocDistribute", isEdocDistribute);
        mav.addObject("canUpdateContent", canUpdateContent);
        // 枚举类型
        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        mav.addObject("colMetadata", colMetadata);
        // 加载对象
        // mav.addObject("list", pagenate(list));//lijl注销
        mav.addObject("list", list);// lijl添加,修改GOV-348.公文登记页面，分发页面不能翻页.

        // 发文拟文权限(注意是发文的)
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        mav.addObject("isEdocCreateRole", isEdocCreateRole);

        return mav;
    }

    // wangjingjing 分发待办事项 begin
    public ModelAndView edocRecDistribute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("edoc/edocFrameEntryPending");
    }

    public ModelAndView edocRecDistributeStep2(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        return new ModelAndView("edoc/edocFramePending");
    }
    // wangjingjing 分发待办事项 end

    /**
     * 分发列表
     */
    public ModelAndView listDistribute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();

        String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
        // GOV-4810.安全测试：aqc没有单位公文管理员-收文管理-分发权限，输入地址却能访问对应界面 start
        String edocType = request.getParameter("edocType");

        // G6不跳转到待办，A8跳转到待办，叶方的序曲。
        if (!"1".equals(edocType) || (!"true".equals(isG6) && !EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(),
                user.getId(), EdocEnum.edocType.distributeEdoc.ordinal()))) {
            return refreshWorkspace();
        }
        // GOV-4810.安全测试：aqc没有单位公文管理员-收文管理-分发权限，输入地址却能访问对应界面 end

        Long userId = user.getId();
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");
        String[] values = new String[2];
        values[0] = textfield;
        values[1] = textfield1;
        ModelAndView mav = new ModelAndView("edoc/listDistribute");
        List<EdocRecieveRecord> list = recieveEdocManager.getWaitRegisterEdocRecieveRecords(userId);
        if (list != null)
            for (EdocRecieveRecord r : list) {
                EdocSummary summary = edocSummaryManager.findById(r.getEdocId());
                r.setCopies(summary == null ? null : summary.getCopies());
            }
        // lijl获取待分发数据----------------------------------------------------------------Start
        List<EdocSummaryModel> summaryList = new ArrayList<EdocSummaryModel>();
        EdocSummary edocSummary = null;
        String btnTypeStr = request.getParameter("btnType");
        int btnType = 0;
        if (Strings.isNotBlank(btnTypeStr)) {
            btnType = Integer.parseInt(btnTypeStr);
        } else {
            btnType = 2;// 默认是待分发的数据
        }

        // 时间分类查询（本日，昨日，本周等）
        String listType = request.getParameter("listType");
        int listTypeInt = 0;
        if (listType != null && !"".equals(listType)) {
            // listTypeInt=Integer.parseInt(listType);
            condition = "registerDate";
            String[] dateFields = com.seeyon.v3x.edoc.util.DateUtil.getTimeTextFiledByTimeEnum(listTypeInt);
            values[0] = dateFields[0];
            values[1] = dateFields[1];
        }

        // 带分发的数据
        List<EdocRegister> pendingList = edocRegisterManager.findRegisterByState(condition, values, btnType,
                AppContext.getCurrentUser());

        for (EdocRegister edocRegister : pendingList) {
            String serialNo = edocRegister.getSerialNo();
            // lijl处理内部文号0|内部文号||3------------------------------------Start
            if (serialNo != null && !"".equals(serialNo)) {
                serialNo = serialNo.replace("0|", "");
                serialNo = serialNo.replace("||3", "");
            }
            edocRegister.setSerialNo(serialNo);
            // lijl处理内部文号0|内部文号||3------------------------------------End
            edocSummary = new EdocSummary();
            EdocSummaryModel edocSummaryModel = new EdocSummaryModel();
            if (edocRegister != null) {
                ConvertUtils.register(new SqlTimestampConverter(null), java.sql.Timestamp.class);
                BeanUtils.copyProperties(edocSummary, edocRegister);
                edocSummary.setId(-1L);// 待分时，EdocSummary数据为空，这里赋值为-1，上一个操作把id赋值给了分发对象
                // puyc
                // 修改Id取值错误--这句话还是注释掉了。非常奇怪的代码，居然用edocRegister的id设置edocSummary的id，然后到listDistribute去直接用edocSummary的id，很危险，容易让以后的人误解。

                // edocSummary.setId(edocRegister.getEdocId());
                // //不要轻易修改这个地方，会引起页面的id传递参数错误
            }
            if (btnType == 2) {// 2由前台传入, 已分发按钮,需查询发起人
                V3xOrgMember v3xOrgMember = this.orgManager.getMemberById(edocRegister.getDistributerId());
                edocSummary.setStartMember(v3xOrgMember);
                // 查询该公文是否已归档
                EdocSummary summary = edocSummaryManager.findById(edocRegister.getEdocId());
                if (summary != null) {
                    edocSummary.setHasArchive(summary.getHasArchive());
                }
            }

            // -------------待分发列表没有显示word图标bug 修复 changyi add
            List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, edocRegister.getId());
            if (affairs != null && affairs.size() > 0) {
                edocSummaryModel.setBodyType(affairs.get(0).getBodyType());
            }
            // -------------待分发列表没有显示word图标bug 修复

            // GOV-3338
            // （需求检查）【公文管理】-【收文管理】，待登记、已登记和待分发列表里'转发文'时选择收文关联新发文，但是打开收文后没有关联发文记录
            // 设置公文登记的类型，是纸质的还是电子的
            edocSummaryModel.setRegisterId(edocRegister.getId());
            edocSummaryModel.setRegisterType(edocRegister.getRegisterType());
            edocSummaryModel.setEdocId(edocRegister.getEdocId()); // 设置公文id
            edocSummaryModel
                    .setAutoRegister(edocRegister.getAutoRegister() == null ? 1 : edocRegister.getAutoRegister());
            edocSummaryModel.setRegisterUserName(edocRegister.getRegisterUserName());
            edocSummaryModel.setRegisterDate(edocRegister.getRegisterDate());

            if (edocRegister.getRecieveUserId() != null) {
                edocSummaryModel.setRecUserName(edocRegister.getRecieveUserName());
                edocSummaryModel.setRecieveDate(edocRegister.getRecTime());
            }

            // Long recUserId = edocRegister.getRecUserId();
            // if(recUserId != null){
            // String recUserName =
            // orgManager.getMemberById(recUserId).getName();
            // edocSummaryModel.setRecUserName(recUserName);
            // }
            // String r =
            // "com.seeyon.v3x.collaboration.resources.i18n.CollaborationResource";
            edocSummaryModel.setSummary(edocSummary);

            edocSummaryModel.setProxy(edocRegister.isProxy());

            edocSummaryModel.setProxyName(EdocHelper.getMemberById(edocRegister.getProxyUserId()).getName());
            edocSummaryModel.setExchangeMode(edocRegister.getExchangeMode() + "");// 交换方式
            summaryList.add(edocSummaryModel);

            // lijl添加,GOV-3245.A设置B为代理人，他们的待分发下显示出来了以前设置的代理事项.--------------------------------Start

            /*
             * if(edocRegister.getProxyUserId() != null){//判断是否有代理
             * List<AgentModel> _agentModelList =
             * MemberAgentBean.getInstance().getAgentModelList(user.getId());//
             * 获取被理人 if(_agentModelList!=null){ if(_agentModelList.size()>0){
             * for(int i=0;i<_agentModelList.size();i++){ AgentModel
             * agentModel=_agentModelList.get(i);
             * if(edocRegister.getRegisterUserId()==agentModel.getAgentToId()){/
             * /如果登记人的ID等于被代理人的ID则设置代理信息 Date
             * start=agentModel.getStartDate();//获取代理的开如时间 Date
             * end=agentModel.getEndDate();//获取代理的结整束时间 Date
             * now=edocRegister.getUpdateTime();//获取接收的时间
             * if((start.getTime()<=now.getTime()) &&
             * (now.getTime()<=end.getTime())){//判断该公文接收的时间是否是在这个代理时间之内
             * edocRegister.setProxyUserId(edocRegister.getProxyUserId());
             * edocSummaryModel.setProxy(edocRegister.isProxy());
             *
             * // String label = "("+ResourceBundleUtil.getString(r,
             * "col.proxy")+
             * EdocHelper.getMemberById(edocRegister.getProxyUserId()).getName()
             * +")"; // edocSummaryModel.setProxyName(label);
             *
             * edocSummaryModel.setProxyName(edocRegister.getProxyUserId()!=null
             * ?orgManager.getMemberById(edocRegister.getProxyUserId()).getName(
             * ):""); summaryList.add(edocSummaryModel); }else{ //如果不在不做处理 }
             * }else{ //如果签收人不等于被代理人也不做处理 } } }else{
             * summaryList.add(edocSummaryModel); } }else{
             * summaryList.add(edocSummaryModel); } }else{
             * summaryList.add(edocSummaryModel); }
             */

            // lijl添加,GOV-3245.A设置B为代理人，他们的待分发下显示出来了以前设置的代理事项.--------------------------------End
            // puyc 代理
            // edocSummaryModel.setProxy(edocRegister.isProxy());
            // if(edocRegister.getProxyUserId()!=null){
            // V3xOrgMember proxyMember =
            // this.orgManager.getMemberById(edocRegister.getProxyUserId());
            // edocSummaryModel.setProxyName(proxyMember.getName());
            // }

        }
        /* xiangfan 添加 判断当前用户是否设置了 收文-分发 的标题多行显示 start */
        boolean hasSubjectWrap = false;
        hasSubjectWrap = this.edocSummaryManager.hasSubjectWrapRecordByCurrentUser(user.getAccountId(), user.getId(),
                EdocSubjectWrapRecord.Edoc_Receive_Fenfa, 1);
        mav.addObject("hasSubjectWrap", hasSubjectWrap);
        /* xiangfan 添加 判断当前用户是否设置了 收文-分发 的标题多行显示 end */
        mav.addObject("pendingList", summaryList);
        // lijl获取待分发数据----------------------------------------------------------------End
        mav.addObject("edocType", EdocEnum.edocType.recEdoc.ordinal());
        mav.addObject("controller", "edocController.do");
        mav.addObject("newEdoclabel", "edoc.new.type.rec");
        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        mav.addObject("colMetadata", colMetadata);
        mav.addObject("list", list);
        // 发文拟文权限(注意是发文的)
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        mav.addObject("isEdocCreateRole", isEdocCreateRole);
        mav.addObject("isOpenRegister", EdocSwitchHelper.isOpenRegister());
        mav.addObject("showBanwenYuewen", EdocSwitchHelper.showBanwenYuewen(user.getLoginAccount()));// 是否开启区分阅文、办文开关
        mav.addObject("canSelfCreateFlow", EdocSwitchHelper.canSelfCreateFlow());// 是否允许自建流程
        // 设置阅文页签权限
        setReadEdocIsView(mav);
        return mav;
    }

    /**
     * 纸质收文退回检查
     *
     * @param idStr
     * @return
     */
    public String checkDistributeRetreat(String idStr) {
        StringBuilder passIds = new StringBuilder();
        StringBuilder notPassNames = new StringBuilder();
        if (Strings.isNotBlank(idStr)) {
            EdocSummary summary = null;
            EdocRegister edocRegister = null;
            EdocRecieveRecord record = null;
            String[] ids = idStr.split(",");
            if (ids != null && ids.length > 0) {
                for (int i = 0; i < ids.length; i++) {
                    boolean notPassFlag = false;
                    long id = Long.parseLong(ids[i]);
                    summary = edocSummaryManager.findById(id);
                    edocRegister = edocRegisterManager.findRegisterByDistributeEdocId(id);
                    if (edocRegister == null) {
                        record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(id);
                        if (record != null) {
                            notPassFlag = true;
                        }
                    } else {
                        notPassFlag = true;
                    }
                    if (notPassFlag) {
                        passIds.append(id + ",");
                    } else {
                        notPassNames.append(summary.getSubject() + ",");
                    }
                }
            }
        }
        String ids = passIds.toString();
        String names = notPassNames.toString();
        if (ids.length() > 0) {
            ids = ids.toString().substring(0, ids.length() - 1);
        }
        if (names.length() > 0) {
            names = names.substring(0, names.length() - 1);
        }
        return ids + "|" + names;
    }

    /**
     * 收文分发列表页的退件箱：退回到登记(已发中退回，退件箱中退回)
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView distributeRetreat(HttpServletRequest request, HttpServletResponse response) throws Exception {

        User user = AppContext.getCurrentUser();

        request.getSession().removeAttribute("transmitSendAtts");

        Long id = Strings.isBlank(request.getParameter("id")) ? -1 : Long.parseLong(request.getParameter("id"));
        Long registerId = Strings.isBlank(request.getParameter("registerId")) ? -1
                : Long.parseLong(request.getParameter("registerId"));
        String stepBackInfo = request.getParameter("stepBackInfo");// 退回备注
        String subject = "";// 消息标题
        String distributeName = "";// 接受者名字
        long edocId = 0l;// 公文id
        // long createId = 0l;//接收者id
        long sendId = 0l;// 发送者id
        int distributeState = -1;// 分发状态

        EdocRegister edocRegister = null;

        EdocSummary summary = null;

        Long templateId = null;
        // 收文分发退回到登记
        if (id != -1) {
            summary = edocSummaryManager.findById(id);
            if (summary != null) {
                List<EdocRegister> list = edocRegisterManager.findRegister(summary.getId());
                if (list != null && list.size() > 0) {
                    edocRegister = list.get(0);
                }
                if (null != summary.getTempleteId()) {
                    templateId = summary.getTempleteId();
                }
            }
        }

        if (registerId != -1) {
            if (edocRegister == null) {
                edocRegister = edocRegisterManager.getEdocRegister(registerId);
            }
        }

        if (edocRegister != null) {
            if (summary != null) {
                affairManager.deleteByObjectId(ApplicationCategoryEnum.edocRec, summary.getId());
                // 删除收文(暂逻辑删除)
                edocSummaryManager.updateEdocSummaryState(summary.getId(),
                        CollaborationEnum.flowState.deleted.ordinal());
            }

            distributeState = edocRegister.getDistributeState();

            edocRegister.setIsRetreat(RegisterRetreatState.Retreated.ordinal());
            edocRegister.setState(RegisterState.DraftBox.ordinal());// 电子公文回退更具实际情况在后面重新进行设置
            edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());
            edocRegister.setDistributeEdocId(-1l);
            edocRegister.setDistributeDate(null);
            subject = edocRegister.getSubject();
            distributeName = edocRegister.getDistributer();
            edocId = edocRegister.getEdocId();
            // createId = edocRegister.getCreateUserId();
            sendId = edocRegister.getDistributerId();

            long registerUserId = edocRegister.getRegisterUserId();// 电子公文退回时，下面会用到这个原登记人变量
            String competitionAction = request.getParameter("competitionAction");
            // 当纸质登记后，待分发退回时，需要退回到登记待发竞争执行
            // 需要将原登记人和指定的分发人清除掉
            if ("yes".equals(competitionAction)) {
                edocRegister.setRegisterUserId(0L);
                edocRegister.setRegisterUserName("");
                edocRegister.setDistributerId(0L);
                edocRegister.setDistributer("");
            }

            /**
             * EdocRecieveRecord record =
             * recieveEdocManager.getEdocRecieveRecordByEdocId(edocId);
             * 不能用上面的方式，因为补发的签收数据和原数据 对应的edocId是一致的
             */
            EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(edocRegister.getRecieveId());

            // 删除分发待办
            Map<String, Object> columns = new Hashtable<String, Object>();
            columns.put("delete", true);
            affairManager.update(columns,
                    new Object[][]{{"app", ApplicationCategoryEnum.edocRecDistribute.getKey()},
                            {"state", StateEnum.edoc_exchange_register.key()}});

            // 自动登记的数据 或者当 登记关闭时，分发直接退回到签收
            if ((edocRegister.getAutoRegister() != null
                    && edocRegister.getAutoRegister().intValue() == EdocRegister.AUTO_REGISTER)
                    || !EdocSwitchHelper.isOpenRegister()) {
                edocExchangeManager.stepBackRegisterEdoc(edocRegister, record.getId(), record.getId(), stepBackInfo,
                        competitionAction);
            }
            // 按照以前的回退流程
            else {
                // 给登记人发送退回消息
                if (!"yes".equals(competitionAction)) {
                }
                // 添加登记待办

                V3xOrgMember registerUser = orgManager.getMemberById(Long.valueOf(registerUserId));
                String resource = "F07_recRegister";
                if (EdocHelper.isG6Version()) {
                    resource = "F07_recListRegistering";
                }
                boolean isRegisterOk = privilegeManager.checkByReourceCode(resource, registerUserId,
                        registerUser.getOrgAccountId());
                List<V3xOrgMember> otherRegistAclMembers = new ArrayList<V3xOrgMember>();

                // 发送消息
                if (!isRegisterOk) {
                    otherRegistAclMembers = privilegeManager.getMembersByResource(resource, registerUser.getOrgAccountId());
                    Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                    for (V3xOrgMember member : otherRegistAclMembers) {
                        receivers.add(new MessageReceiver(edocId, member.getId()));
                    }
                    MessageContent content = new MessageContent("exchange.stepback", subject, distributeName, stepBackInfo);
                    if (null != templateId) {
                        content.setTemplateId(templateId);
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edocRegister, sendId, receivers,
                            EdocMessageFilterParamEnum.recQita.key);
                } else {
                    MessageReceiver receiver = new MessageReceiver(edocId, registerUserId);
                    MessageContent content = new MessageContent("exchange.stepback", subject, distributeName, stepBackInfo);
                    if (null != templateId) {
                        content.setTemplateId(templateId);
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.edocRegister, sendId, receiver,
                            EdocMessageFilterParamEnum.recQita.key);
                }

                if (edocRegister.getRegisterType() == 1) {// 电子登记
                    if (edocRegister.getEdocId() != -1 && edocRegister.getRecieveId() != -1) {
                        columns = new Hashtable<String, Object>();
                        columns.put("delete", true);
                        affairManager.update(columns,
                                new Object[][]{{"app", ApplicationCategoryEnum.edocRegister.getKey()},
                                        {"objectId", edocRegister.getEdocId()},
                                        {"subObjectId", edocRegister.getRecieveId()}});
                    }
                    /**
                     * 只有电子登记的，待分发回退时才会生成待登记的affair数据
                     * 纸质登记的，从待分发回退，是回退到登记待发的，不产生affair数据
                     */
                    Map<String, Object> extParam = new HashMap<String, Object>();
                    extParam.put(AffairExtPropEnums.edoc_edocMark.name(), edocRegister.getDocMark()); // 公文文号
                    extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), edocRegister.getSendUnit());// 发文单位
                    extParam.put(AffairExtPropEnums.edoc_edocRegisterRetreat.name(), user.getId());// G6原来set到此字段的值,一起加
                    if (isRegisterOk) {

                        // 回退直接回退到登记待发里面
                        saveAffair(edocRegister, registerUserId, extParam, true);
                    } else {

                        // 现在纸质登记的也可以在待分发中回退了，而纸质登记的record为null
                        if (record != null) {

                            // 将签收状态改为 1(未登记)不然 待登记退回时，就会判断为已登记不能退回了
                            record.setStatus(EdocRecieveRecord.Exchange_iStatus_Recieved);
                            record.setRegisterUserId(0);// 设置为竞争执行

                            recieveEdocManager.update(record);
                        }

                        for (V3xOrgMember member : otherRegistAclMembers) {
                            saveAffair(edocRegister, member.getId(), extParam, true);
                        }
                    }
                } else {// 纸质、二维码登记
                    columns = new Hashtable<String, Object>();
                    columns.put("delete", true);
                    affairManager.update(columns,
                            new Object[][]{{"app", ApplicationCategoryEnum.edocRegister.getKey()},
                                    {"objectId", edocRegister.getId()}});
                }

                // 上一个分支把registered删除了
                edocRegisterManager.updateEdocRegister(edocRegister);// 更新登记数据
            }

            Integer logAction = 340;// 分发待发-回退

            // 更新操作日志
            if (EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal() == distributeState) {// 带分发列表回退

                logAction = 339;// 待分发-回退

            } else if (!EdocHelper.isG6Version() // A8
                    || EdocNavigationEnum.EdocDistributeState.Distributed.ordinal() == distributeState
                    || EdocNavigationEnum.EdocDistributeState.Distribute_Back.ordinal() == distributeState) {// 被回退公文再回退

                logAction = 341;// 待发-回退
            }

            appLogManager.insertLog(user, logAction, user.getName(), edocRegister.getSubject());

        } else {
            if (summary != null) {
                EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(summary.getId());
                if (record != null) {
                    // 删除收文(暂物理删除) 撤销/取回都要删除分发数据
                    summary.setState(CollaborationEnum.flowState.deleted.ordinal());
                } else {
                    String msg = ResourceUtil.getString("edoc.alert.flow.paperReceiptStepback", summary.getSubject());// 纸质收文《"+summary.getSubject()+"》不允许退回操作。
                    StringBuffer sb = new StringBuffer();
                    sb.append("alert('" + StringEscapeUtils.escapeJavaScript(msg) + "');");
                    // out.println("try {parent.getA8Top().endProc();}");
                    // out.println("catch (e) {};");
                    // out.println("parent.doEndSign_pending();");
                    sb.append("location.href='edocController.do?method=listWaitSend&edocType=1&list=retreat';");
                    rendJavaScript(response, sb.toString());
                    return null;
                }
            }
        }

        if (registerId != -1) {// 待分发列表
            // 回退成功后，跳到待分发列表
            // GOV-3546 【公文管理】-【收文管理】-【分发】，待分发收文退回后跳转到退件箱页面去了

            ModelAndView toView = null;

            if (summary != null) {// G6分发待发，A8待发列表
                toView = new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
            } else {
                toView = super.redirectModelAndView(
                        "/edocController.do?method=listDistribute&edocType=1&list=aistributining&btnType=2");
            }
            return toView;
        } else {// 退件箱列表
            return super.redirectModelAndView("/edocController.do?method=listWaitSend&edocType=1&list=retreat");
        }
    }

    /**
     * G6生成待登记数据
     *
     * @param edocRegister   登记数据
     * @param affairMemberId 待处理人
     * @param extParam       affair扩展属性
     * @param toDraf         是否生成登记待发数据
     * @throws BusinessException
     * @Author : xuqiangwei
     * @Date : 2015年1月7日下午1:38:04
     */
    private void saveAffair(EdocRegister edocRegister, Long affairMemberId, Map<String, Object> extParam,
                            boolean toDraf) throws BusinessException {
        // 添加收文分发退回到收文登记标识，EdocSummary表就不用做标识了，Affair的isDelete做标识
        CtpAffair affair = new CtpAffair();
        affair.setIdIfNew();
        affair.setApp(ApplicationCategoryEnum.edocRegister.getKey());
        affair.setSubject(edocRegister.getSubject());
        affair.setMemberId(affairMemberId);
        affair.setFinish(false);
        affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
        affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
        affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
        if (edocRegister.getRegisterType() == 1 && edocRegister.getEdocId() != -1
                && edocRegister.getRecieveId() != -1) {
            affair.setObjectId(edocRegister.getEdocId());
        } else {
            affair.setObjectId(edocRegister.getId());
        }
        affair.setSubObjectId(edocRegister.getRecieveId());
        affair.setSenderId(AppContext.getCurrentUser().getId());

        if (toDraf) {
            affair.setState(StateEnum.col_waitSend.getKey());
            affair.setSubState(SubStateEnum.col_waitSend_draft.getKey());
        } else {
            affair.setState(StateEnum.edoc_exchange_register.key());
        }
        // 首页栏目的扩展字段设置--公文文号、发文单位等--start

        AffairUtil.setExtProperty(affair, extParam);
        // 首页栏目的扩展字段设置--公文文号、发文单位等--end
        affairManager.save(affair);
    }

    /**
     * 收文待办退回功能:从收文的退件箱退回到待分发列表
     */
    public ModelAndView listPendingRetreat(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute("transmitSendAtts");
        User user = AppContext.getCurrentUser();
        String _sumaaryId = request.getParameter("resgisterId");
        String _affairId = request.getParameter("checkAffirId");
        String stepBackInfo = request.getParameter("stepBackInfo");// 退回备注
        String subject = "";// 消息标题
        String distributeName = user.getName();// 发送者名字
        long edocId = 0l;// 公文id
        long distributeNameId = 0l;// 接收者id
        long sendId = user.getId();// 发送者id
        // 删除个人流程。
        long affairId = Long.parseLong(_affairId);
        edocManager.deleteAffair("draft", affairId);
        // 更改待分发状态。
        long summaryId = Long.parseLong(_sumaaryId);
        List<EdocRegister> reg = edocRegisterManager.findRegister(summaryId);
        // 收文待办退回到分发的待分发
        EdocRegister register = null;
        if (reg.size() > 0) {
            register = reg.get(0);
        }
        long id = 0l;
        if (register != null) {
            id = register.getId();
        }
        EdocRegister edocRegister = edocRegisterManager.getEdocRegister(id);
        // 设置待分发为2
        edocRegister.setState(EdocNavigationEnum.RegisterState.Registed.ordinal());
        edocRegister.setDistributeState(EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());// distribute_state是分发状态。
        // 设置为被退回状态
        edocRegister.setIsRetreat(1);
        subject = edocRegister.getSubject();
        edocId = edocRegister.getEdocId();
        distributeNameId = edocRegister.getDistributerId();
        edocRegisterManager.updateEdocRegister(edocRegister);

        // 给登记人发送退回消息
        MessageReceiver receiver = new MessageReceiver(edocId, distributeNameId);
        userMessageManager.sendSystemMessage(
                new MessageContent("exchange.stepback", subject, distributeName, stepBackInfo),
                ApplicationCategoryEnum.edocRegister, sendId, receiver, EdocMessageFilterParamEnum.recQita.key);
        // 回退成功后，跳到待办列表?
        return super.refreshWorkspace();
    }

    /**
     * 待阅列表
     */
    public ModelAndView listReading(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute("transmitSendAtts");
        ModelAndView modelAndView = new ModelAndView("edoc/listReading");
        // 用于已阅列表中阅转办跳转页面的表示 wangjingjing 开始
        String pageview = request.getParameter("pageview");
        if (null == pageview) {
            pageview = "listReading";
        }
        modelAndView.addObject("pageview", pageview);
        // 用于已阅列表中阅转办跳转页面的表示 wangjingjing 结束
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");

        String edocType = request.getParameter("edocType");
        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }

        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("track", track);
        params.put("processType", 2);
        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());

        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned())// 无代理人
        {
            /********** 组合查询组件调用开始 *****************/
            String comb_condition = request.getParameter("comb_condition"); // 组合查询标识
            if (comb_condition != null && "1".equals(comb_condition)) {
                EdocSearchModel em = new EdocSearchModel();
                bind(request, em);
                // GOV-5120 【公文管理】-【收文管理】-【待阅】，组合查询时无法查出暂存待办的收文
                queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_pending.key(), params, -1, -1,
                        2);
                modelAndView.addObject("combQueryObj", em); // 设置的查询条件还原到页面
                modelAndView.addObject("combCondition", "1");
            } else
            /*************** 组合查询组件调用结束 ************/
                if (condition != null) {
                    if ("cusReceiveTime".equals(condition)) { // 如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
                        condition = "receiveTime";
                        String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
                        textfield = textfield_condition[0];
                        textfield1 = textfield_condition[1];

                    }

                    queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                            StateEnum.col_pending.key(), params, -1, -1, 2);
                }
            if (queryList != null) {
                modelAndView.addObject("pendingList", queryList);
            } else {
                List<EdocSummaryModel> pendingList = edocManager.queryTodoList(iEdocType, params);
                modelAndView.addObject("pendingList", pendingList);
            }
        } else {
            queryList = new ArrayList<EdocSummaryModel>();
            modelAndView.addObject("pendingList", queryList);
        }

        /* xiangfan 添加 判断当前用户是否设置了 收文-待阅 的标题多行显示 start */
        boolean hasSubjectWrap = false;
        hasSubjectWrap = this.edocSummaryManager.hasSubjectWrapRecordByCurrentUser(user.getAccountId(), user.getId(),
                EdocSubjectWrapRecord.Edoc_Receive_WaitReading, iEdocType);
        modelAndView.addObject("hasSubjectWrap", hasSubjectWrap);
        /* xiangfan 添加 判断当前用户是否设置了 收文-待阅 的标题多行显示 end */

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude

        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        // CtpEnum comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);

        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);

        /*
         * 元数据中加载 秘密程度 cy add
         */
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        /*
         * 元数据中加载 紧急程度 cy add
         */
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);

        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);

        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        modelAndView.addObject("hasRegistButton", hasRegistButton);

        // 分发权限：阅转办
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.distributeEdoc.ordinal());
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);
        // 发文拟文权限:转发文
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);
        modelAndView.addObject("isG6", EdocHelper.isG6Version());

        return modelAndView;
    }

    /**
     * 已阅列表
     */
    public ModelAndView listReaded(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().removeAttribute("transmitSendAtts");
        ModelAndView modelAndView = new ModelAndView("edoc/listReaded");
        // 用于待阅列表中阅转办跳转页面的表示 wangjingjing 开始
        String pageview = request.getParameter("pageview");
        if (null == pageview) {
            pageview = "listReaded";
        }
        modelAndView.addObject("pageview", pageview);

        // 同一流程只显示最后一条
        String deduplication = String.valueOf(request.getParameter("deduplication"));
        deduplication = Functions.toHTML(deduplication);
        if ("null".equals(deduplication) || Strings.isBlank(deduplication)) {
            deduplication = "false";
        }
        modelAndView.addObject("isGourpBy", deduplication);
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");

        String edocType = request.getParameter("edocType");
        int iEdocType = -1;
        if (edocType != null && !"".equals(edocType)) {
            iEdocType = Integer.parseInt(edocType);
        }

        int hasArchive = -1;
        // lijl添加空值判断
        String hasArchiveStr = request.getParameter("hasArchive");
        if (hasArchiveStr != null && !"".equals(hasArchiveStr)) {
            if (Strings.isNotBlank(hasArchiveStr)) {
                try {
                    hasArchive = Integer.parseInt(hasArchiveStr);
                } catch (Exception ex) {
                    hasArchive = -1;
                }
            }
        }
        int track = Strings.isBlank(request.getParameter("track")) ? -1
                : Integer.parseInt(request.getParameter("track"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("deduplication", deduplication);
        params.put("track", track);
        params.put("processType", 2);
        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = orgManager.getEntityById(V3xOrgMember.class, user.getId());

        /*************** 测试代码开始 *****************/
        // Long metadataId=7971699406983548687L;
        // edocManager.useMetadataValue(user.getLoginAccount(),metadataId,"22");
        /*************** 测试代码结束 *****************/

        List<EdocSummaryModel> queryList = null;
        if (theMember.getIsAssigned())// 无代理人
        {
            /********** 组合查询组件调用开始 *****************/
            String comb_condition = request.getParameter("comb_condition"); // 组合查询标识
            if (comb_condition != null && "1".equals(comb_condition)) {
                EdocSearchModel em = new EdocSearchModel();
                bind(request, em);
                queryList = edocManager.combQueryByCondition(iEdocType, em, StateEnum.col_done.key(), params, -1,
                        hasArchive, 2);
                modelAndView.addObject("combQueryObj", em); // 设置的查询条件还原到页面
                modelAndView.addObject("combCondition", "1");
            } else
            /*************** 组合查询组件调用结束 ************/
                if (condition != null) {
                    if ("cusReceiveTime".equals(condition)) { // 如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
                        condition = "receiveTime";
                        String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
                        textfield = textfield_condition[0];
                        textfield1 = textfield_condition[1];

                    }

                    queryList = edocManager.queryByCondition(iEdocType, condition, textfield, textfield1,
                            StateEnum.col_done.key(), params, -1, hasArchive, 2);
                }
                // wangjingjing 当不是组合查询，也不是自定义查询时执行 begin
                /*
                 * if (queryList != null) { modelAndView.addObject("pendingList",
                 * queryList); } else { List<EdocSummaryModel> pendingList =
                 * edocManager.queryTodoList(iEdocType);
                 * modelAndView.addObject("pendingList", pendingList); }
                 */
                else {
                    queryList = edocManager.queryByCondition(iEdocType, "", null, null, StateEnum.col_done.key(), params,
                            -1, hasArchive, 2);
                }
            // wangjingjing end
        }
        // wangjingjing begin
        /*
         * else { queryList = new ArrayList<EdocSummaryModel>();
         * modelAndView.addObject("pendingList", queryList); }
         */
        modelAndView.addObject("pendingList", queryList);
        // wangjingjing end

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        // CtpEnum comImportanceMetadata =
        // enumManager.getEnum(EnumNameEnum.common_importance);
        // modelAndView.addObject("comImportanceMetadata",
        // comImportanceMetadata);

        /*
         * 元数据中加载 秘密程度 cy add
         */
        CtpEnumBean secretLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        colMetadata.put(EnumNameEnum.edoc_secret_level.toString(), secretLevel);

        /*
         * 元数据中加载 紧急程度 cy add
         */
        CtpEnumBean urgentLevel = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        colMetadata.put(EnumNameEnum.edoc_urgent_level.toString(), urgentLevel);

        modelAndView.addObject("colMetadata", colMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("edocType", edocType);

        // 是否包含“待登记”按钮
        boolean hasRegistButton = false;
        if (EdocEnum.edocType.recEdoc.ordinal() == iEdocType) {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.rec");
            hasRegistButton = true;
        } else {
            modelAndView.addObject("newEdoclabel", "edoc.new.type.send");
        }
        modelAndView.addObject("hasRegistButton", hasRegistButton);
        // 收文分发权限判断，应传入收文分发参数
        boolean isEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.distributeEdoc.ordinal());
        boolean isExchangeRole = EdocRoleHelper.isExchangeRole();
        modelAndView.addObject("isEdocCreateRole", isEdocCreateRole);
        modelAndView.addObject("isExchangeRole", isExchangeRole);

        // 发文拟文权限
        boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
        modelAndView.addObject("isSendEdocCreateRole", isSendEdocCreateRole);

        return modelAndView;
    }

    /**
     * 正常处理公文
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView finishWorkItem(HttpServletRequest request, HttpServletResponse response) throws Exception {


        User user = AppContext.getCurrentUser();
        boolean isRelieveLock = true;
        EdocSummary summary = null;
        Long affairId = null;
        String _affairId = request.getParameter("affairId");
        CtpAffair affair = null;
        try {
            affairId = Long.valueOf(_affairId);
            affair = affairManager.get(affairId);
            // 待办校验
            if (affair == null || affair.getState() != StateEnum.col_pending.key()) {
                String errStr = ResourceUtil.getString("edoc.workitem.state.nopending.lable");
                rendJavaScript(response, "alert('" + errStr + "');window.top.close();");
                return null;
            }

            Long lockUserId = null;
            if (null != affairId) {
                lockUserId = edocLockManager.canGetLock(affairId, user.getId());
                if (lockUserId != null) {
                    LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
                    return null;
                }
            }

            Map<String, Object> params = parseFinishReqParamsToMap(request);

            // 历史原因， 这样设置参数， 公文重构时请 重构 transFinishWorkItem
            params.put("_from_pc_", "_from_pc_");

            edocManager.transFinishWorkItem(affair, params);

            // 添加最近处理模板
            if (affair.getTempleteId() != null) {
                templeteManager.addRecentTemplete(affair.getMemberId(), ModuleType.collaboration.getKey(),
                        affair.getTempleteId(), TemplateEnum.RecentUseType.deal);
            }
            StringBuffer sb = new StringBuffer();

            sb.append("parent.doEndSign_pending('" + affairId + "');");
            rendJavaScript(response, sb.toString());

            return null;
        } catch (Exception e) {
            LOGGER.error("处理公文抛出异常：", e);
        } finally {
            if (null != affairId) {
                edocLockManager.unlock(affairId);
            }

            if (isRelieveLock) {
                // 解锁正文文单
                wapi.releaseWorkFlowProcessLock(affair.getProcessId(), String.valueOf(AppContext.currentUserId()));
                wapi.releaseWorkFlowProcessLock(String.valueOf(affair.getObjectId()), String.valueOf(AppContext.currentUserId()));
            }
            try {
                unLock(user.getId(), summary);
            } catch (Exception e) {
                LOGGER.error("解锁正文文单抛出异常：", e);
            }
        }
        return null;
    }

    private Map<String, Object> parseFinishReqParamsToMap(HttpServletRequest request) {

        Map<String, Object> params = new HashMap<String, Object>();

        String attitude = request.getParameter("attitude");
        String content = request.getParameter("contentOP");
        String[] afterSign = request.getParameterValues("afterSign");
        String trackMembers = request.getParameter("trackMembers");
        String trackRange = request.getParameter("trackRange");
        String exchangeType = request.getParameter("edocExchangeType");
        String returnDeptId = request.getParameter("returnDeptId");
        String docMark = request.getParameter("docMark");

        String newPdfId = request.getParameter("newPdfIdFirst");

        String docMark2 = request.getParameter("docMark2");
        String serialNo = request.getParameter("serialNo");
        String isConvertPdf = request.getParameter("isConvertPdf");
        //设置手动选择的归档路径ID
        String archiveId = request.getParameter("archiveId");
        String oldOpinionIdStr = request.getParameter("oldOpinionId");

        String optionType = request.getParameter("optionType");
        String summaryIdStr = request.getParameter("summary_id");

        String spMemberId = request.getParameter("supervisorId");
        String superviseDate = request.getParameter("awakeDate");
        String supervisorNames = request.getParameter("supervisors");
        String title = request.getParameter("superviseTitle");
        String processXml = request.getParameter("process_xml");
        String readyObjectJSON = request.getParameter("readyObjectJSON");
        String workflowNodePeoplesInput = request.getParameter("workflow_node_peoples_input");
        String workflowNodeConditionInput = request.getParameter("workflow_node_condition_input");
        String optionWay = request.getParameter("optionWay");

        String signingDate = request.getParameter("signing_date");
        String oldSigningDate = request.getParameter("signingDate");

        String messageDataListJSON = request.getParameter("process_message_data");
        String isDeleteSupervisior = request.getParameter("isDeleteSupervisior");


        // 推送消息 affairId,memberId#affairId,memberId#affairId,memberId
        String pushMessageMembers = request.getParameter("pushMessageMemberIds");
        List<Long[]> pushMsgMemberList = parsePushMessageMemberList(pushMessageMembers);
        params.put("pushMsgMemberList", pushMsgMemberList);


        // 选择的特定的单位公文收发员
        String exchangeMemberId = request.getParameter("memberList");


        String isReportToSupAccount = request.getParameter("isReportOpinion");
        String processChangeMessage = request.getParameter("processChangeMessage");


        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setIsReportToSupAccount(Boolean.valueOf(isReportToSupAccount));
        try {
            bind(request, signOpinion);
        } catch (Exception e) {
            LOGGER.error("", e);
        }


        params.put("isHidden", request.getParameterValues("isHidden"));
        params.put("opinion", signOpinion);
        params.put("processChangeMessage", processChangeMessage);

        params.put("attitude", attitude);
        params.put("contentOP", content);
        params.put("afterSign", afterSign);
        params.put("trackMembers", trackMembers);
        params.put("trackRange", trackRange);

        params.put("edocExchangeType", exchangeType);
        params.put("exchangeMemberId", exchangeMemberId);
        params.put("returnDeptId", returnDeptId);

        params.put("docMark", docMark);
        params.put("newPdfIdFirst", newPdfId);
        params.put("docMark2", docMark2);

        params.put("serialNo", serialNo);
        params.put("isConvertPdf", isConvertPdf);
        params.put("archiveId", archiveId);
        params.put("optionType", optionType);
        params.put("summary_id", summaryIdStr);

        params.put("supervisorId", spMemberId);
        params.put("awakeDate", superviseDate);
        params.put("supervisors", supervisorNames);
        params.put("superviseTitle", title);
        params.put("process_xml", processXml);

        params.put("readyObjectJSON", readyObjectJSON);
        params.put("workflow_node_peoples_input", workflowNodePeoplesInput);
        params.put("workflow_node_condition_input", workflowNodeConditionInput);
        params.put("optionWay", optionWay);
        params.put("signing_date", signingDate);

        params.put("signingDate", oldSigningDate);
        params.put("process_message_data", messageDataListJSON);
        params.put("isDeleteSupervisior", isDeleteSupervisior);
        params.put("oldOpinionId", oldOpinionIdStr);


        params.put("userId", AppContext.currentUserId());

        return params;
    }

    public ModelAndView detailIFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocDetailIFrame");
        return modelAndView;
    }

    /**
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocSuperiseDetail");
        String isCollCube = request.getParameter("isCollCube");
        String isColl360 = request.getParameter("isColl360");
        String isWfanalysis = request.getParameter("isWfanalysis");
        String isTransFrom = request.getParameter("isTransFrom");
        modelAndView.addObject("isTransFrom", isTransFrom);
        try {
            User user = AppContext.getCurrentUser();
            Long summaryId = null;
            boolean showType = false;// 关联收文、是否有权限显示公文
            String sendSummaryId = request.getParameter("sendSummaryId"); // 从发文关联收文点击进来的,这个是发文summaryId
            String canNotOpen = request.getParameter("canNotOpen");
            String from = request.getParameter("from");
            String openFrom = request.getParameter("openFrom");
            // OA-35933在处理公文时，从关联文档中打开一个公文，应该只能查看，而这里居然可以处理公文--如果是关联文档打开，则是以已办的形式打开，不允许处理。
            boolean isglwgFlag = "glwd".equals(openFrom);
            if (Strings.isNotBlank(openFrom) && "glwd".equals(openFrom)) {
                from = "Done";
                showType = true;
            }
            if (Strings.isNotBlank(sendSummaryId)) {// 从关联文点击进来的
                // 发文关联收文，在发文流程上的人有权限看收文
                List<CtpAffair> affairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc,
                        Long.parseLong(sendSummaryId));
                for (int i = 0; i < affairs.size(); i++) {
                    if (Strings.equals(user.getId(), affairs.get(i).getMemberId())) {
                        showType = true;
                    }
                }
            }
            if (!Strings.isBlank(request.getParameter("summaryId"))) {
                summaryId = Long.parseLong(request.getParameter("summaryId"));
            }
            CtpAffair affair = null;
            String _affairId = request.getParameter("affairId");
            if ("null".equals(_affairId)) {
                _affairId = null;
            }
            modelAndView.addObject("isCollCube", isCollCube);
            modelAndView.addObject("isColl360", isColl360);
            if (Strings.isNotBlank(isCollCube) && "1".equals(isCollCube) && Strings.isNotBlank(_affairId)) {// 协同立方过来查看的判断
                CtpAffair validAffair = affairManager.get(Long.parseLong(_affairId));
                if (null != validAffair && StateEnum.col_done.getKey() != validAffair.getState().intValue()) {
                    String errStr = ResourceUtil.getString("collaboration.alert.chuantou.label");
                    rendJavaScript(response, "alert('" + errStr
                            + "');try{parent.window.close();parent.window.parentDialogObj.url.closeParam.handler();}catch(e){}");
                    return null;
                }
            }
            if (Strings.isNotBlank(isColl360) && "1".equals(isColl360) && Strings.isNotBlank(_affairId)) {// 协同360过来查看的判断
                // ps:协同360记录了两种关系一是发送者二是处理者
                CtpAffair validAffair = affairManager.get(Long.parseLong(_affairId));
                if (null != validAffair
                        && ((StateEnum.col_sent.getKey() == validAffair.getState().intValue() && validAffair.isDelete())
                        || (StateEnum.col_done.getKey() != validAffair.getState().intValue()
                        && validAffair.isDelete())
                        || (StateEnum.col_waitSend.getKey() == validAffair.getState().intValue()
                        && null != validAffair.getSubState() && SubStateEnum.col_waitSend_cancel
                        .getKey() == validAffair.getSubState().intValue())
                        || (StateEnum.col_pending.getKey() == validAffair.getState().intValue())
                        || StateEnum.col_cancel.getKey() == validAffair.getState().intValue())) {
                    String errStr = ResourceUtil.getString("collaboration.alert.chuantou.label");
                    rendJavaScript(response, "alert('" + errStr
                            + "');try{parent.window.close();parent.window.parentDialogObj.url.closeParam.handler();}catch(e){}");
                    return null;
                }
            }

            if (Strings.isNotBlank(_affairId)) {
                affair = affairManager.get(Long.parseLong(_affairId));
                boolean isWaitSendDelete = Integer.valueOf(StateEnum.col_waitSend.key()).equals(affair.getState()) && affair.isDelete();
                if (!ColUtil.isAfffairValid(affair) || isWaitSendDelete) {
                    List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, affair.getObjectId(), user.getId());
                    if (Strings.isNotEmpty(affairs)) {
                        for (CtpAffair aff : affairs) {
                            boolean isWaitSendDeleteA = Integer.valueOf(StateEnum.col_waitSend.key()).equals(aff.getState()) && aff.isDelete();

                            if (ColUtil.isAfffairValid(affair) && !isWaitSendDeleteA) {
                                affair = aff;
                                break;
                            }
                        }
                    }
                }
                if (!showType && !"transEvent".equals(isTransFrom)) {
                    if (affair != null && !SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.edoc, user, affair.getId(), affair, null, false)) {
                        List<CtpAffair> myAffairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, affair.getObjectId(), user.getId());
                        if (myAffairs != null && !myAffairs.isEmpty()) {
                            //发送待发和已登记的数据是objectID，是一样的， 这个逻辑有问题， 感觉不对
                            if (affair.getApp().equals(myAffairs.get(0).getApp())) {
                                affair = myAffairs.get(0);
                                _affairId = affair.getId().toString();
                                modelAndView.addObject("changeTOSelf", true);
                            }
                        }
                    }
                }
                if (isglwgFlag && null != affair && null != affair.getSubState()
                        && SubStateEnum.col_waitSend_cancel.getKey() == affair.getSubState()) {
                    rendJavaScript(response,
                            "alert('" + ResourceUtil.getString("edoc.alert.already.repeal") + "');window.top.close();");
                    return null;
                }
                if (affair != null && affair.getApp() == ApplicationCategoryEnum.edocRecDistribute.key()) {
                    affair = affairManager.getSenderAffair(affair.getSubObjectId());
                    if (affair != null) {
                        _affairId = affair.getId() + "";
                        modelAndView.addObject("isEdocRecDistribute", true);
                    }
                }

                // GOV-5168.【公文管理】-【发文管理】，并发流程中的某个节点处理时将公文'退回拟稿人'，
                // 另一条线上的用户在待办里仍能看到该公文，处理时提示'公文已被撤销'（详细流程图请见附件）. ---start
                if (affair != null && affair.getState() == StateEnum.col_pending.key()) { //
                    summaryId = affair.getObjectId();
                    // 获得发起人的affair
                    CtpAffair affair_sender = affairManager.getSenderAffair(summaryId);

                    if (affair_sender != null && affair_sender.getSubState() != null
                            && affair_sender.getSubState() == SubStateEnum.col_waitSend_sendBack.key()) {
                        String errMsg = ResourceUtil.getString("edoc.symbol.opening.chevron") + affair_sender.getSubject() + ResourceUtil.getString("edoc.symbol.closing.chevron") + ResourceBundleUtil
                                .getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource", "alert_not_openReturn");
                        rendJavaScript(response, "alert('" + errMsg + "');window.top.close();");
                        return null;
                    }

                    // OA-63298节点到期后转给指定人后，还能通过历史消息点击转之前的公文，弹出空白页面刷新提示null
                    if (summaryId == -1) {
                        String errMsg = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "alert_already_deleted", affair.getSubject());
                        rendJavaScript(response, "alert('" + replaceRN(errMsg) + "');window.top.close();");
                        return null;
                    }

                }
                if (affair != null && Strings.isNotBlank(from)) {
                    if ("Pending".equals(from) || "Done".equals(from)) {// 撤销消息点击查看时是Down
                        // 获得发起人的affair
                        if (affair.getSubState() != null
                                && affair.getSubState() == SubStateEnum.col_waitSend_cancel.key()) {
                            String errMsg = ResourceUtil.getString("edoc.symbol.opening.chevron") + StringEscapeUtils.escapeJavaScript(affair.getSubject()) + ResourceUtil.getString("edoc.symbol.closing.chevron")
                                    + ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                    "edoc.waitSend.stepBack");
                            rendJavaScript(response, "alert('" + errMsg + "');window.top.close();");
                            return null;
                        }
                        // 撤销
                        if (affair.getState() != null && affair.getState() == StateEnum.col_cancel.key()) {
                            String errMsg = ResourceUtil.getString("edoc.symbol.opening.chevron") + StringEscapeUtils.escapeJavaScript(affair.getSubject()) + ResourceUtil.getString("edoc.symbol.closing.chevron")
                                    + ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                    "edoc.waitSend.revocation");
                            rendJavaScript(response, "alert('" + errMsg + "');window.top.close();");
                            return null;
                        }

                        if (affair.getState() != null
                                && Integer.valueOf(StateEnum.col_stepBack.getKey()).equals(affair.getState())) {
                            String errMsg = ResourceUtil.getString("edoc.symbol.opening.chevron") + StringEscapeUtils.escapeJavaScript(affair.getSubject()) + ResourceUtil.getString("edoc.symbol.closing.chevron")
                                    + ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                    "edoc.waitSend.stepBack");
                            rendJavaScript(response, "alert('" + errMsg + "');window.top.close();");
                            return null;
                        }
                    }
                }
                if (affair != null && Strings.isNotBlank(from)) {
                    // 如果是從列表打開已被回退的信息，此處進行驗證
                    // OA-43845 公文待发列表中单击/双击流程下游节点回退的公文，弹出的的界面中提示公文已经被回退
                    // OA-50622
                    // 已经被指定回退给发起人的公文，查看以前的回退的消息还能设置督办，且督办设置生效，上一条消息的链接from的参数是sended
                    if (null != affair.getSubState() && affair.getState().intValue() == StateEnum.col_waitSend.key()) {
                        from = "listWaitSend";
                    }
                    if (!"listWaitSend".equals(from)) {
                        if (null != affair.getSubState()) {
                            /**
                             * 当前数据是否为回退数据 affair!=null 非空验证
                             * affair.getSubState().intValue() ==
                             * SubStateEnum.col_waitSend_stepBack.key()
                             * 当前数据是否为已回退数据 affair.getState().intValue() !=
                             * StateEnum.col_waitSend.key() 当前数据状态为待发状态
                             * 验证逻辑为：affair不等于空，当前数据substate是已回退状态并且state不等于待发，
                             * 才进行提示
                             * OA-48472待发栏目更多页面，查看被人回退回来的发文流程，提示已被回退。应该可以查看的
                             */
                            if (affair != null
                                    && affair.getSubState().intValue() == SubStateEnum.col_waitSend_stepBack.key()
                                    && affair.getState().intValue() != StateEnum.col_waitSend.key()) {
                                String errMsg = ResourceUtil.getString("edoc.symbol.opening.chevron") + affair.getSubject() + ResourceUtil.getString("edoc.symbol.closing.chevron") + ResourceBundleUtil.getString(
                                        "com.seeyon.v3x.edoc.resources.i18n.EdocResource", "edoc.affair.stepBack");
                                rendJavaScript(response, "alert('" + errMsg + "');window.top.close();");
                                return null;
                            }
                        }
                    }
                }
                // GOV-5168.【公文管理】-【发文管理】，并发流程中的某个节点处理时将公文'退回拟稿人'，
                // 另一条线上的用户在待办里仍能看到该公文，处理时提示'公文已被撤销'（详细流程图请见附件）. --end

                // ------------公文查看性能优化，detail方法中查出affair对象后保存进session中，在后面的getContent方法中再从session中取，然后清空session
                // request.getSession().setAttribute(EdocConstant.AFFAIR_TO_SESSEION,affair);
            } else {
                // 取我的Affair，用于访问控制权限校验
                List<CtpAffair> myAffairs = affairManager.getAffairs(ApplicationCategoryEnum.edoc, summaryId,
                        user.getId());
                if (myAffairs != null && !myAffairs.isEmpty()) {
                    affair = myAffairs.get(0);
                } else {
                    affair = affairManager.getSenderAffair(summaryId);
                }
            }
            // OA-18953 待办中进行撤销操作，然后单击当时收到发文的消息提示，是代码形式的
            String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
            if (affair == null || (affair.getState().intValue() != StateEnum.col_pending.key()
                    && affair.getState().intValue() != StateEnum.col_sent.key()
                    && affair.getState().intValue() != StateEnum.col_waitSend.key()
                    && affair.getState().intValue() != StateEnum.col_done
                    .key() /*
             * && user.getId() !=
             * affair.getSenderId().longValue()
             */)) {

                // xiangfan GOV-4867 添加判断条件 如果当前人是公文的发起人，那么即使收到回退的消息也是能打开公文的
                if (("true".equals(isG6) && user.getId() == affair.getSenderId().longValue())
                        || "stepBackRecord".equals(openFrom) || "repealRecord".equals(openFrom)) {
                    // 什么也不做
                } else {
                    // 删除状态
                    // GOV-4837
                    // 公文管理，签报；拟文发起后，后面的节点回退到首节点，首节点编辑后重新发起。系统提示的XX回退签报XX，点这条消息提示NULL
                    // 补上非空的判断，退回的公文再发出时，对应的与原来的affairid对应affair记录会删除，点击回退的系统消息提示，该事项已被删除
                    if (affair != null && affair.isDelete() && affair.getMemberId().longValue() == user.getId()
                            && affair.getState().intValue() == StateEnum.col_waitSend.key()
                            && affair.getSubState().intValue() == SubStateEnum.col_waitSend_stepBack.key()) {
                        affair.setDelete(false);
                        affairManager.updateAffair(affair);
                    } else {
                        String msg = EdocHelper.getErrorMsgByAffair(affair);
                        // 修复bug
                        // GOV-491
                        // a拟文，b/c审核，b不同意，提交给c，c退回，b退回给发起人，a查看，提示的是被撤销，应该是被退回
                        // ---start
                        // 这里有加入了 affair非空的判断，不然这里会产生空指针
                        if (affair != null && affair.getState() == StateEnum.col_cancel.key()) { //

                            if (Strings.isBlank(_affairId)) {

                                summaryId = affair.getObjectId();

                                // 获得发起人的affair
                                List<CtpAffair> waitAffs = affairManager.getAffairs(summaryId, StateEnum.col_waitSend);
                                affair = waitAffs.get(0);

                                if (affair.getSubState() == SubStateEnum.col_waitSend_stepBack.ordinal()) {
                                    String state = ResourceUtil.getString("collaboration.state.6.stepback");
                                    String appName = ResourceUtil
                                            .getString("application." + affair.getApp() + ".label");
                                    msg = ResourceUtil.getString("collaboration.state.invalidation.alert",
                                            affair.getSubject(), state, appName, 0, null);
                                }
                            }
                        }
                        // GOV-491
                        // a拟文，b/c审核，b不同意，提交给c，c退回，b退回给发起人，a查看，提示的是被撤销，应该是被退回
                        // ---end
                        throw new EdocException(msg);
                    }
                }
            }
            if (Strings.isNotBlank(_affairId)) {
                // 标记为已读状态。
                try {
                    edocManager.updateAffairStateWhenClick(affair);
                } catch (Exception e) {
                    LOGGER.error("公文标记为已读状态抛出异常：", e);
                    return null;
                }
            }
            if (summaryId == null) {
                summaryId = affair.getObjectId();
            }

            long relationSummaryId = summaryId;

            String detailType = request.getParameter("detailType");
            modelAndView.addObject("forwardType", detailType);
            // 如果是从已分发列表中查看详细信息

            String edocType = request.getParameter("edocType");
            List<EdocSummary> newEdocList = null;
            if ("1".equals(edocType) && "listSent".equals(detailType)) {
                String edocId = request.getParameter("edocId");
                relationSummaryId = Long.parseLong(edocId);

                List<EdocSummary> allEdocList = new ArrayList<EdocSummary>();

                EdocRegister edocRegister = edocRegisterManager.findRegisterByDistributeEdocId(relationSummaryId);
                if (edocRegister != null) {
                    List<EdocSummary> newEdocList1 = this.edocSummaryRelationManager
                            .findNewEdocByRegisteredOrWaitSent(edocRegister.getId(), user.getId(), 1, 2);
                    if (newEdocList1 != null) {
                        allEdocList.addAll(newEdocList1);
                    }
                }

                List<EdocSummary> newEdocList2 = this.edocSummaryRelationManager
                        .findNewEdocByRegisteredOrWaitSent(relationSummaryId, user.getId(), 1, 2);
                if (newEdocList2 != null) {
                    allEdocList.addAll(newEdocList2);
                }

                newEdocList = allEdocList;
            }

            /********* puyc 关联发文 * 收文的Id,收文的Type *********/
            String relSends = "haveNot";
            String relRecs = "haveNot";
            if (Strings.isBlank(detailType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(relationSummaryId, user.getId(), 1);
            } else if (!"listSent".equals(detailType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(relationSummaryId,
                        user.getId(), 1, 2);
            }
            if (Strings.isEmpty(newEdocList) && "1".equals(edocType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(relationSummaryId, user.getId(), 1);
            }

            // OA-36095
            // wangchw登记了纸质公文，在待办中转发文--收文关联新发文，收文处理节点查看有关联链接，处理时回退该流程，发起人在待发中查看有此链接直接发送后已发待办中也有，但是若在待发中编辑没有此链接，发送后也没此链接
            if (Strings.isEmpty(newEdocList)) {
                EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
                if (summary.getEdocType() == 1) {
                    newEdocList = this.edocSummaryRelationManager.findAllNewEdoc(summaryId, user.getId(), 1);
                }
            }

            // GOV-4481 新建收文中调用系统收文模板，发送之后在已办列表中的文点开之后已有关联发文的链接，点击链接无内容，此文并没有转发文
            if (Strings.isNotEmpty(newEdocList)) {
                relSends = "haveMany";
                modelAndView.addObject("recEdocId", relationSummaryId);
                modelAndView.addObject("recType", 1);
                modelAndView.addObject("relSends", relSends);
            }
            /********* puyc 关联收文 发文的Id 发文的Type **********/
            EdocSummaryRelation edocSummaryRelationR = this.edocSummaryRelationManager.findRecEdoc(summaryId, 0);
            if (edocSummaryRelationR != null) {
                relRecs = "haveMany";
                if ("isYes".equals(canNotOpen) || Strings.isNotBlank(sendSummaryId)) {
                    relRecs = "haveNot";
                }
                modelAndView.addObject("relRecs", relRecs);
            }
            /**
             * 收文转发文，收文关联发文，进行催办按钮屏蔽控制 from不为空时，from值不变化，不影响其它链接的设置
             * canNotOpen参数为收发文关联打开链接参数
             */
            if (Strings.isBlank(from) && "isYes".equals(canNotOpen)) {
                from = "haveNot";
            }
            /********* puyc end **********/
            // SECURITY 访问安全检查
            // 收文关联发文不需要进行验证
            if ("true".equals(request.getParameter("openEdocByForward"))) {
                showType = true;
            }
            if (!showType && !"transEvent".equals(isTransFrom)) {
                if (!SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.edoc, user, summaryId, affair,
                        null)) {
                    if ((Strings.isNotBlank(isCollCube) && "1".equals(isCollCube))
                            || (Strings.isNotBlank(isColl360) && "1".equals(isColl360))
                            || (Strings.isNotBlank(isWfanalysis) && "1".equals(isWfanalysis))) {
                        // String errStr =
                        // ResourceUtil.getString("collaboration.alert.chuantou.label");
                        rendJavaScript(response, "window.top.close();");
                        return null;
                    } else {
                        return null;
                    }
                }
            } else {// BUG_紧急_V5_V5.0sp2_青岛地铁集团有限公司_打开协同中的关联文档报错：您无权限查看该主题_20150213007240_如果跳过安全验证，就要加入安全缓存
                AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.edoc,
                        String.valueOf(summaryId), user.getId());
            }
            // String list = request.getParameter("list");
            // if(!Strings.isNotBlank(list)){
            // list="popup";
            // }
            // 有可能公文已经处理完了，但是消息提示框仍然在哪里，所以需要重新查询一下当前事项的状态。
            // G6BUG_G6_v1.0_中国证券监督管理委员会_自定义IE标题，打开公文单后，IE标题显示为默认的名称，打开其他的事项显示的都是自定义的标题_20120731011964
            String subject = affair.getSubject();
            modelAndView.addObject("affairId", _affairId);
            if (affair.getState().equals(StateEnum.col_done.getKey()) && "Pending".equals(from)) {
                from = "Done";
            }
            modelAndView.addObject("sendSummaryId", sendSummaryId);
            modelAndView.addObject("summaryId", summaryId);
            modelAndView.addObject("bodyType", affair.getBodyType());
            modelAndView.addObject("openFrom", request.getParameter("openFrom"));
            modelAndView.addObject("controller", "edocController.do");
            modelAndView./* addObject("list", list). */addObject("from", from).addObject("summarySubject", subject);
            // modelAndView.addObject("securityChecks", securityChecks);
            return modelAndView;
        } catch (Exception e) {
            LOGGER.error("打开公文报错：", e);
            String errMsg = StringEscapeUtils.escapeJavaScript(e.getMessage());
            rendJavaScript(response, "alert('" + errMsg
                    + "');try{parent.window.close();parent.window.parentDialogObj.url.closeParam.handler();}catch(e){}");
            return null;
        }
    }

    public ModelAndView edocDetailInDoc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/edocSuperiseDetail");
        String summaryId = request.getParameter("summaryId");
        String showHl = request.getParameter("showHl");
        if (!"false".equals(showHl)) {
            showHl = "true";
        }
        CtpAffair affair = null;
        if (Strings.isNotBlank(summaryId)) {
            affair = affairManager.getSenderAffair(Long.valueOf(summaryId));
            if (affair == null) {
                // 获得从已发删除的affair
                DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class)
                        .add(Restrictions.eq("objectId", Long.valueOf(summaryId))).add(Restrictions.eq("delete", true))
                        .add(Restrictions.in("state",
                                new Object[]{StateEnum.col_sent.key(), StateEnum.col_waitSend.key()}));
                List<CtpAffair> list = DBAgent.findByCriteria(criteria);

                if (Strings.isNotEmpty(list)) {
                    affair = list.get(0);
                } else {
                    // 在文档中心打开部门归档的公文，由于部门归档doc_resource表的source_id是affairId，所以这里传的summaryId实际是affairId
                    affair = affairManager.get(Long.valueOf(summaryId));
                    if (null != affair) {
                        if (affair.getObjectId() != null) {
                            summaryId = affair.getObjectId().toString();
                        }
                    }
                }
            }
        } else {
            String archiveModifyId = request.getParameter("archiveModifyId"); // 归档修改日志id
            if (Strings.isNotBlank(archiveModifyId)) {
                mav.addObject("archiveModifyId", archiveModifyId);
            }
        }
        mav.addObject("showHl", showHl);
        mav.addObject("openFrom", request.getParameter("openFrom"));
        mav.addObject("lenPotent", request.getParameter("lenPotent"));
        mav.addObject("docId", request.getParameter("docId"));
        mav.addObject("summaryId", summaryId);
        mav.addObject("isLibOwner", request.getParameter("isLibOwner"));
        mav.addObject("controller", "edocController.do");
        if (affair != null) {
            mav.addObject("affairId", affair.getId());
            mav.addObject("summarySubject", affair.getSubject());
        }
        return mav;
    }

    private String getTrackIds(Long affairId) throws BusinessException {
        StringBuilder ids = new StringBuilder();
        if (affairId != null) {
            List<CtpTrackMember> tracks = null;
            tracks = trackManager.getTrackMembers(Long.valueOf(affairId));
            if (tracks != null) {
                for (CtpTrackMember ctpTrackMember : tracks) {
                    if (ids.length() > 0) {
                        ids.append(",");
                    }
                    ids.append(ctpTrackMember.getTrackMemberId().toString());
                }
            }
        }
        return ids.toString();
    }

    /**
     * 获取一篇公文的全部属性
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView summary(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean flageState = true;
        try {
            User user = AppContext.getCurrentUser();
            ModelAndView mav = new ModelAndView("edoc/edocSummary");
            String sessionId = HttpSessionUtil.getSessionId(request);
            mav.addObject("sessionId", sessionId);

            String _trackValue = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_PROCESS);
            mav.addObject("customSetTrack", _trackValue);

            String s_summaryId = request.getParameter("summaryId");
            long summaryId = 0L;
            if (s_summaryId != null && !s_summaryId.isEmpty()) {
                summaryId = Long.parseLong(s_summaryId);
            }

            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
            if (summary == null) {
                LOGGER.error("公文不存在");
                StringBuffer sb = new StringBuffer();
                // super.printV3XJS(out);
                sb.append("alert(\"" + ResourceUtil.getString("edoc.resourse.notExist") + "\");");
                sb.append("debugger;if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"true\";");
                sb.append("  window.close();");
                sb.append("}else{");
                sb.append("  parent.close();");
                sb.append("}");
                sb.append("");
                rendJavaScript(response, sb.toString());
                return null;
            }

            String hasMeetingPlugin = "false";
            String _from = request.getParameter("from");
            String _openFrom = request.getParameter("openFrom");
            // 待办 || 已办 || 已发 || 督办 || 协同驾驶舱穿透查询（Pending）
            boolean _flag = ("Pending".equals(_from) || "Done".equals(_from) || "listSent".equals(_from)
                    || "supervise".equals(_from) || "supervise".equals(_openFrom) || "sended".equals(_openFrom)
                    || "sended".equals(_from)) && !"glwd".equals(_openFrom) && !"recRegisterResult".equals(_openFrom)// 收文登记薄
                    && !"sendRegisterResult".equals(_openFrom)// 发文登记薄
                    && !"edocPerformanceList".equals(_openFrom);// 绩效考核连接

            boolean _hasResourceCode = user.hasResourceCode("F09_meetingArrange");

            if (AppContext.hasPlugin("meeting") && _flag && _hasResourceCode) {
                hasMeetingPlugin = "true";
            }
            mav.addObject("hasMeetingPlugin", hasMeetingPlugin);
            // 公文处理意见回显到公文单,排序
            long flowPermAccout = EdocHelper.getFlowPermAccountId(user.getLoginAccount(), summary, templeteManager);

            // 验证当前流程是否是指定回退流程
            boolean specialStepBack = (summary.getCaseId() == null || Long.valueOf(0).equals(summary.getCaseId()))
                    ? false : wapi.isInSpecialStepBackStatus(summary.getCaseId());
            mav.addObject("specialStepBack", specialStepBack);
            mav.addObject("summary", summary);
            mav.addObject("contentRecordId", summary.getEdocBodiesJs());
            mav.addObject("archiveName", edocManager.getShowArchiveNameByArchiveId(summary.getArchiveId()));
//			mav.addObject("hasPrePighole", summary.getArchiveId() == null ? false : true);
            mav.addObject("fullArchiveName", edocManager.getFullArchiveNameByArchiveId(summary.getArchiveId()));
            String isCollCube = request.getParameter("isCollCube");
            String isColl360 = request.getParameter("isColl360");
            String isTransFrom = request.getParameter("isTransFrom");
            mav.addObject("isCollCube", isCollCube);
            mav.addObject("isColl360", isColl360);
            mav.addObject("isTransFrom", isTransFrom);
            // 是否允许拟文人修改附件。
            long edocAccountId = user.getLoginAccount();
            if (summary != null) {
                edocAccountId = summary.getOrgAccountId();
            }
            // 需要获得创建公文的单位的开关
            boolean allowUpdateAttachment = EdocSwitchHelper.allowUpdateAttachment(edocAccountId);
            mav.addObject("allowUpdateAttachment", allowUpdateAttachment);

            // 只查找正文的附件。
            List<Attachment> attachments = attachmentManager.getByReference(summaryId, summaryId);
            List<Attachment> attachmentList = new ArrayList<Attachment>();
            String trimFileName = "";
            for (Attachment att : attachments) {
                if (att.getFilename().contains("<br/>")) {
                    trimFileName = att.getFilename().replace("<br/>", "");
                } else {
                    trimFileName = att.getFilename();
                }
                att.setFilename(trimFileName);
                attachmentList.add(att);
            }
            mav.addObject("attachments", attachmentList);

            mav.addObject("controller", "edocController.do");
            String _docId = request.getParameter("docId");
            mav.addObject("openFrom", request.getParameter("openFrom"))
                    .addObject("lenPotent", request.getParameter("lenPotent")).addObject("docId", _docId);
            // 判断是否是部门归档
            Long docResId = null;
            boolean isDeptPigeonhole = false;
            if (AppContext.hasPlugin("doc") && Strings.isNotBlank(_docId)) {
                docResId = Long.parseLong(_docId);
                DocResourceBO doc = docApi.getDocResource(docResId);
                if (doc != null && Integer.valueOf(1).equals(doc.getPigeonholeType())) {
                    isDeptPigeonhole = true;
                }
            }
            mav.addObject("isDeptPigeonhole", isDeptPigeonhole);
            try {
                V3xOrgMember member = this.orgManager.getMemberById(user.getId());
                if (member != null) {
                    mav.addObject("extendConfig", member.getProperty("extendConfig"));
                }
            } catch (Exception e) {
                LOGGER.error("查看公文抛出异常：", e);
            }

            String _affairId = request.getParameter("affairId");
            Long affairId = null;
            CtpAffair affair = null;
            EdocZcdb edocZcdb = null; // 暂存待办提醒时间

            Long currentNodeOwner = user.getId();
            try {
                if (("null".equals(_affairId) || Strings.isBlank(_affairId))
                        && "supervise".equals(request.getParameter("openFrom"))) {
                    CtpAffair sAffair = affairManager.getSenderAffair(summaryId);
                    _affairId = sAffair.getId().toString();
                }

                affairId = Long.valueOf(_affairId);
                affair = affairManager.get(affairId);
                if (affair != null && !affair.getMemberId().equals(currentNodeOwner)) {
                    currentNodeOwner = affair.getMemberId();
                }

                /**
                 * OA-50342督办人员查看处于指定回退的督办公文流程时，无修改流程按钮
                 * 此处验证督办详细是否可以修改流程，需要加上指定回退的验证
                 */
                if (affair != null && affair.getState() == SubStateEnum.col_waitSend_draft.getKey()
                        && affair.getSubState() != SubStateEnum.col_pending_specialBacked.key()) {
                    flageState = false;
                }
                // 如果转发后发送的公文，affair待办中就有了forwardMemberId了，之后流程中所有节点产生的affair事项中都要一直有这个值
                // 需要将该值设置到处理页面的隐藏域中
                String forwardMember = affair.getForwardMember();
                if (Strings.isNotBlank(forwardMember)) {
                    mav.addObject("forwardMember", forwardMember);
                }

                // 暂存待办提醒时间
                edocZcdb = edocZcdbManager.getEdocZcdbByAffairId(affairId);
                if (edocZcdb != null) {
                    mav.addObject("zcdbTime", edocZcdb.getZcdbTime());
                }
                Integer _subState = affair.getSubState();
                mav.addObject("isFlowBack", affair.getBackFromId());// 是否是回退(指定回退)标识
                mav.addObject("affairSubState", _subState);
                mav.addObject("trackStatus", affair.getTrack());
                if (ColOpenFrom.stepBackRecord.name().equals(request.getParameter("openFrom"))) {
                    if (null != summary && null != affair && null != summary.getCaseId()
                            && null != affair.getActivityId()) {
                        boolean nodeDelete = wapi.isNodeDelete(summary.getCaseId(), affair.getActivityId().toString());
                        boolean effectiveAffair = isEffectiveAffair(summary.getId(), affair.getActivityId(),
                                AppContext.getCurrentUser().getId());
                        if (nodeDelete || !effectiveAffair) {
                            rendJavaScript(response,
                                    "alert('" + ResourceUtil.getString("collaboration.alert.notExistInWF") + "');");
                            return null;
                        }
                    }
                    // 获取关联发文信息
                    getRelationSummaryInfo(mav, summaryId);
                }

            } catch (Exception e) {
                LOGGER.error("", e);
            }

            mav.addObject("finished", summary.getFinished());

            String supervise = request.getParameter("supervise");
            if (Strings.isNotBlank(supervise)) {
                mav.addObject("supervisePanel", supervise);
            }

            boolean hasDiagram = false;
            if (summary != null && summary.getCaseId() != null)
                hasDiagram = true;

            mav.addObject("hasDiagram", hasDiagram);
            mav.addObject("hasBody1", summary.getBody(1) != null);
            mav.addObject("hasBody2", summary.getBody(2) != null);
            EdocBody pb = summary.getBody(EdocBody.EDOC_BODY_PDF_ONE);
            mav.addObject("firstPDFId", pb != null ? pb.getContent() : "");
            Long templeteProcessId = 0L;
            /* 获取调用加签，减签工作流接口需要准备的数据 */
            if (affair != null) {
                Long workitemId = affair.getSubObjectId();
                Long activityId = affair.getActivityId();// 节点id
                Long performer = affair.getMemberId(); // 处理人员

                mav.addObject("workitemId", workitemId);
                mav.addObject("activityId", activityId);
                mav.addObject("performer", performer);

                /*
                 * 加签 减签 知会 js 调用修改 需要加 默认节点权限 和 单位id参数 单位id 对于自由流程取 发起人所在单位id
                 * 对于模板流程取 模板所在单位id 所以在调用方法之前，要先判断 当前处理的 是自由流程发起的还是由 模板流程发起
                 */

                //是否是系统模板，并且系统模板是否有归档ID
                boolean hasSysTemplateArchiveId = false;

                Long wOrgAccountId = null;
                Long templeteId = summary.getTempleteId();
                if (templeteId == null) {
                    wOrgAccountId = summary.getOrgAccountId();
                } else {
                    TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");
                    CtpTemplate templete = templateManager.getCtpTemplate(templeteId);
                    if (null == templete) {
                        wOrgAccountId = summary.getOrgAccountId();
                    } else {
                        wOrgAccountId = templete.getOrgAccountId();
                        templeteProcessId = templete.getWorkflowId();

                        if (templete.isSystem()) {
                            EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
                            if (edocSummary.getArchiveId() != null && edocSummary.getArchiveId().longValue() != 0 && edocSummary.getArchiveId().longValue() != -1) {
                                hasSysTemplateArchiveId = true;
                            }
                        }

                        if (templete.getFormParentid() != null) {// 系统模板的个人模板
                            CtpTemplate pTemplate = templeteManager.getCtpTemplate(templete.getFormParentid());
                            // OA-45197保存待发后流程仍然是空的
                            // 系统格式模板存为个人模板之后，调用个人模板存为待发，存待发中打开公文详细
                            // 流程图的流程id要用个人模板的，而不是系统格式模板的(因没有流程)
                            if (!"text".equals(pTemplate.getType())) {
                                templeteProcessId = pTemplate.getWorkflowId();
                            }

                            if (!hasSysTemplateArchiveId && pTemplate.isSystem()) {
                                EdocSummary edocSummary = (EdocSummary) XMLCoder.decoder(templete.getSummary());
                                if (edocSummary.getArchiveId() != null && edocSummary.getArchiveId().longValue() != 0 && edocSummary.getArchiveId().longValue() != -1) {
                                    hasSysTemplateArchiveId = true;
                                }
                            }
                        }
                    }
                }
                //OA-117052
                String archiveName = edocManager.getFullArchiveNameByArchiveId(summary.getArchiveId());
                if (Strings.isBlank(archiveName)) {
                    hasSysTemplateArchiveId = false;
                } else {
                    mav.addObject("hasPrePighole", summary.getArchiveId() == null ? false : true);
                }
                mav.addObject("wOrgAccountId", wOrgAccountId);
                mav.addObject("hasSysTemplateArchiveId", hasSysTemplateArchiveId);
                mav.addObject("bodyType", affair.getBodyType());

                // 传阅还需要 departmentId
                mav.addObject("departmentId", user.getDepartmentId());
            }
            mav.addObject("templeteProcessId", templeteProcessId);
            mav.addObject("processId", summary.getProcessId());
            mav.addObject("scene", 3);// 查看已发出去的流程图
            if (Strings.isBlank(summary.getProcessId()) || "0".equals(summary.getProcessId())
                    || "-1".equals(summary.getProcessId())) {
                mav.addObject("scene", 2);// 查看模板流程图
            } else {
                mav.addObject("scene", 3);
            }

            // caseId是用来标记流程图上的当前节点的，待发列表中以下情况不需要标记：直接保存待发（subState=1）、回退导致待发（subState=2）、从已发中撤销流程（subState=3）、指定回退流程重走（subState=18）
            boolean showCaseId = false;
            if (affair.getState() == StateEnum.col_waitSend.getKey()) {
                showCaseId = affair.getSubState() == SubStateEnum.col_waitSend_draft.getKey() // 直接保存待发（subState=1）
                        || affair.getSubState() == SubStateEnum.col_waitSend_stepBack.getKey() // 回退导致待发（subState=2）
                        || affair.getSubState() == SubStateEnum.col_waitSend_cancel.getKey() // 从已发中撤销流程（subState=3）
                        || affair.getSubState() == SubStateEnum.col_pending_specialBackToSenderCancel.getKey(); // 指定回退流程重走（subState=18）
            }
            if (!showCaseId) {
                mav.addObject("caseId", summary.getCaseId());
            }
            mav.addObject("activityId", affair.getActivityId());
            // OA-34445新建模版，选择的预归档路径较长，单击打开模版查看，这时看的路径是：自建路径全部显示了
            mav.addObject("archiveFullName", edocManager.getShowArchiveNameByArchiveId(summary.getArchiveId()));
            if (summary.getCaseId() == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("edoc CaseId is null: ");
                if (summary != null)
                    sb.append(summary.getId());
                if (affair != null)
                    sb.append(", " + affair.getId());
                sb.append(", " + user.getName());
                LOGGER.info(sb.toString());
            }
            mav.addObject("edocType", summary.getEdocType());
            // 指定回退再处理的流转方式
            // show1 重新流转 show2 提交回退者
            String configItem = "shenpi";
            if (affair.getNodePolicy() != null) {
                configItem = affair.getNodePolicy();
            }
            String category = "";
            if (null != summary) {
                int edocType = summary.getEdocType();
                if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {
                    category = EnumNameEnum.edoc_send_permission_policy.name();
                } else if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
                    category = EnumNameEnum.edoc_rec_permission_policy.name();
                } else if (edocType == EdocEnum.edocType.signReport.ordinal()) {
                    category = "";
                    category = EnumNameEnum.edoc_qianbao_permission_policy.name();
                }
            }

            PermissionVO permissionVO = permissionManager.getPermissionVO(category, configItem, flowPermAccout);
            String _updateNodeFlag = "";// OA-66328 update by libing
            // 用于判断当前节点权限是否存在，如果不存在则给知会的提示，inform是协同的节点权限，处理时做知会操作产生的错误数据
            if (!"inform".equals(configItem) && permissionVO != null && !configItem.equals(permissionVO.getName())
                    && affair.getState() == StateEnum.col_pending.getKey()) {
                mav.addObject("noFindPermission", true);
                if (EnumNameEnum.edoc_send_permission_policy.name().equals(category)
                        || EnumNameEnum.edoc_qianbao_permission_policy.name().equals(category)) {
                    mav.addObject("_fawentishi", "1");
                    _updateNodeFlag = "1";
                } else {
                    mav.addObject("_shouwentishi", "1");
                    _updateNodeFlag = "2";
                }
            } else {
                mav.addObject("noFindPermission", false);
            }
            if (permissionVO != null) {
                Integer submitStyle = permissionVO.getSubmitStyle();
                if (submitStyle != null) {
                    switch (submitStyle) {
                        case 0:
                            AppContext.putRequestContext("show1", true);
                            AppContext.putRequestContext("show2", false);
                            break;
                        case 1:
                            AppContext.putRequestContext("show1", false);
                            AppContext.putRequestContext("show2", true);
                            break;
                        default:
                            AppContext.putRequestContext("show1", true);
                            AppContext.putRequestContext("show2", true);
                            break;
                    }
                }
            }
            // 判断归档文件夹是否存在
            boolean docResourceExist = false;
            if (AppContext.hasPlugin("doc")) {
                docResourceExist = docApi.isDocResourceExisted(summary.getArchiveId());
            }
            mav.addObject("isPresPigeonholeFolderExsit", docResourceExist);
            // 如果归档文件夹被删除了，设置summary为未归档。
            if (!docResourceExist) {
                summary.setArchiveId(null);
                summary.setHasArchive(false);
            }
            mav.addObject("hasArchive", summary.getHasArchive());
            mav.addObject("hasSetPigeonholePath", summary.getArchiveId() == null ? false : true);
            // 转发公文：新生成一个待转发的清除了痕迹的公文,产生一个这样的公文的ID。
            mav.addObject("transmitSendNewEdocId", UUIDLong.longUUID());

            // -------公文查看性能优化，下面查询跟踪表可能会被注释
            mav.addObject("trackIds", getTrackIds(affairId));

            String openFrom = request.getParameter("openFrom");
            String lenPotents = request.getParameter("lenPotent");
            String lenPotent = "0";
            String lenPotentPrint = "0";// 借阅设置的打印权限
            if (lenPotents != null && !"".equals(lenPotents)) {
                lenPotent = lenPotents.substring(0, 1);
                lenPotentPrint = lenPotents.substring(2, 3);
            }

            if (lenPotents != null && !"".equals(lenPotents)) {
                lenPotent = lenPotents.substring(0, 1);
            }

            String officecanPrint = "true";
            String officecanSaveLocal = "true";
            String onlySeeContent = "false";
            if (lenPotents != null && !"".equals(lenPotents)) {
                officecanSaveLocal = "0".equals(lenPotents.substring(1, 2)) ? "false" : "true";
            }
            if (lenPotents != null && !"".equals(lenPotents)) {
                officecanPrint = "0".equals(lenPotents.substring(2, 3)) ? "false" : "true";
            }
            mav.addObject("officecanPrint", officecanPrint);
            mav.addObject("officecanSaveLocal", officecanSaveLocal);

            String from = request.getParameter("from");
            if ("lenPotent".equals(openFrom) && Byte.toString(DocConstants.LENPOTENT_CONTENT).equals(lenPotent)) {
                onlySeeContent = "true";
            }
            mav.addObject("onlySeeContent", onlySeeContent);
            String showHl = request.getParameter("showHl");
            mav.addObject("showHl", showHl);
            boolean affairCanPrint = false; // 事项的节点权限是否包含打印操作
            boolean isSupervisor = false; // 是否督办人
            Map<String, List<String>> actions = new HashMap<String, List<String>>();
            if (!"WaiSend".equals(request.getParameter("from"))) { // 待发不用判断权限
                String nodePermissionPolicy = "shenpi";
                String[] nodePolicyFromWorkflow = null;
                // affair的已发事项没有activityId
                // 从待分发列表打开
                // 登记单，点收文关联发文，出现关联的发文列表，再打开发文时，affair为null,没有传affairId，这里加上非空判断
                if (affair != null && affair.getActivityId() != null)
                    nodePolicyFromWorkflow = wapi.getNodePolicyIdAndName(ApplicationCategoryEnum.edoc.name(),
                            summary.getProcessId(), String.valueOf(affair.getActivityId()));

                if (affair != null) {
                    if ("Pending".equals(request.getParameter("from"))) {
                        // 取回或者暂存待办的需要回写附件和意见
                        if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())
                                && (Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(affair.getSubState())
                                || Integer.valueOf(SubStateEnum.col_pending_takeBack.getKey())
                                .equals(affair.getSubState()))) {

                            EdocOpinion tempOpinion = edocManager.findBySummaryIdAndAffairId(summaryId, affair.getId());
                            mav.addObject("tempOpinion", tempOpinion);
                            if (tempOpinion != null) {
                                mav.addObject("attachmentsOpinion",
                                        attachmentManager.getByReference(summaryId, tempOpinion.getId()));
                            }

                        }
                        // mav.addObject("isMatch", isMatch);
                        // 检查是否有公文发起权----只检查有没有发文的权利
                        boolean isEdocCreateRole = EdocRoleHelper
                                .isEdocCreateRole(EdocEnum.edocType.sendEdoc.ordinal());
                        mav.addObject("isEdocCreateRole", isEdocCreateRole);
                    }
                    mav.addObject("currentNodeId", affair.getActivityId());
                    // 催办按钮只有在已发事项中才能显示

                    if (summary.getState() != CollaborationEnum.flowState.finish.ordinal()
                            && affair.getSubObjectId() == null && affair.getState() == StateEnum.col_sent.getKey()
                            && user.getId().longValue() == affair.getSenderId().longValue()
                            && !("lenPotent").equals(openFrom) && !("haveNot").equals(from)) {
                        mav.addObject("showHastenButton", "true");
                    }

                    // 得到当前处理权限录入意见的显示位置
                    if (nodePolicyFromWorkflow != null) {
                        nodePermissionPolicy = nodePolicyFromWorkflow[0];
                        // 流程取过来的权限名，替换特殊空格[160 -> 32]
                        if (nodePermissionPolicy != null) {
                            nodePermissionPolicy = nodePermissionPolicy
                                    .replaceAll(new String(new char[]{(char) 160}), " ");
                        }
                        // 为了解决bug，将协同的知会英文名 inform改为 公文的zhihui
                        if ("inform".equals(nodePermissionPolicy)) {
                            nodePermissionPolicy = "zhihui";
                        }
                        if ("1".equals(_updateNodeFlag)) {
                            nodePermissionPolicy = "shenpi";
                        } else if ("2".equals(_updateNodeFlag)) {
                            nodePermissionPolicy = "yuedu";
                        }
                    }

                    // -----------公文查看性能优化，前面已将查询过了，从ThreadLocal中获取
                    String disPosition = edocFormManager
                            .getOpinionLocation(summary.getFormId(),
                                    EdocHelper.getFlowPermAccountId(summary, user.getLoginAccount()))
                            .get(nodePermissionPolicy);
                    /*
                     * OA-36989一个流程中的多人执行节点，节点权限审核绑定了文单，但是第一个人处理时意见框显示在文单中，
                     * 第二个人处理时，意见框显示在下面 这个缓存有问题，导致意见的位置出现偶发性变化，需要检查这个缓存
                     * Hashtable<String, String> locHs =
                     * SharedWithThreadLocal.getLocHs(); if(locHs == null){
                     * locHs=edocFormManager.getOpinionLocation(summary.
                     * getFormId(),EdocHelper.getFlowPermAccountId(summary,
                     * user.getLoginAccount(), templeteManager)); }
                     *
                     * String disPosition = locHs.get(nodePermissionPolicy);
                     */

                    // OA-45168
                    // 上传一个文单，审批意见框默认绑定了审批节点权限，当取消审批节点后，处理节点处理时，意见框仍显示在文单中，但是处理后意见显示在文单下面
                    // 当disPosition为null时，表示当前节点权限
                    // 没有绑定意见框，那么意见就应该显示在文单最下面(其他意见)，如果再设置为当前节点节点就有问题了
                    // if(disPosition==null){disPosition=nodePermissionPolicy;}
                    if (disPosition != null) {
                        String[] dis = disPosition.split("[_]");
                        disPosition = dis[0];
                    }
                    mav.addObject("disPosition", disPosition);
                    mav.addObject("affair", affair);
                }
                EnumNameEnum edocTypeEnum = EdocUtil.getEdocMetadataNameEnum(summary.getEdocType());
                CtpEnumItem tempMitem = null;
                // 获取节点权限，如果是自由流程取发起人所在单位的节点权限，模板取模板所在单位节点权限
                // Long
                // accountId=orgManager.getMemberById(summary.getStartUserId()).getOrgAccountId();
                Long senderId = summary.getStartUserId();
                V3xOrgMember sender = orgManager.getMemberById(senderId);
                Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, sender.getOrgAccountId());
                mav.addObject("flowPermAccountId", flowPermAccountId);
                // FlowPerm fpm = new FlowPerm();
                Permission permission = null;
                try {
                    // fpm = flowPermManager.getFlowPerm(edocTypeEnum.name(),
                    // nodePermissionPolicy, flowPermAccountId);

                    permission = permissionManager.getPermission(edocTypeEnum.name(), nodePermissionPolicy,
                            flowPermAccountId);
                    // 获取当前节点名称
                    if (permission != null) {
                        mav.addObject("nodePermissionPolicyName", permission.getLabel());
                    } else {
                        // TODO 现在还不清楚阅转办时 为什么节点权限为空了，以后再查
                        mav.addObject("nodePermissionPolicyName", "阅读");
                    }
                    int docMarkAccess = 1;
                    int docMark2Access = 1;
                    int serialNoAccess = 1;
                    // if(null!=fpm){
                    if (null != permission) {

                        Hashtable<Long, EdocElementFlowPermAcl> actorsAcc = edocElementFlowPermAclManager
                                .getEdocElementFlowPermAclsHs(permission.getFlowPermId());
                        EdocElement docMarkElement = edocElementManager.getEdocElement("004");
                        if (actorsAcc.get(Long.parseLong(docMarkElement.getElementId())) != null) {
                            docMarkAccess = actorsAcc.get(Long.parseLong(docMarkElement.getElementId())).getAccess();
                        }

                        EdocElement docMark2Element = edocElementManager.getEdocElement("021");
                        if (actorsAcc.get(Long.parseLong(docMark2Element.getElementId())) != null) {
                            docMark2Access = actorsAcc.get(Long.parseLong(docMark2Element.getElementId())).getAccess();
                        }

                        EdocElement serialNoElement = edocElementManager.getEdocElement("005");
                        if (actorsAcc.get(Long.parseLong(serialNoElement.getElementId())) != null) {
                            serialNoAccess = actorsAcc.get(Long.parseLong(serialNoElement.getElementId())).getAccess();
                        }

                        mav.addObject("opinionPolicy", permission.getNodePolicy().getOpinionPolicy());
                        mav.addObject("cancelOpinionPolicy", permission.getNodePolicy().getCancelOpinionPolicy());
                        mav.addObject("disAgreeOpinionPolicy", permission.getNodePolicy().getDisAgreeOpinionPolicy());
                        mav.addObject("attitudes", permission.getNodePolicy().getAttitude());
                        if (permission.getType() == 0) { // com.seeyon.v3x.flowperm.util.Constants.F_type_system
                            tempMitem = enumManagerNew.getEnumItem(edocTypeEnum, nodePermissionPolicy);
                        }
                    }

                    mav.addObject("docMarkAccess", docMarkAccess);
                    mav.addObject("docMark2Access", docMark2Access);
                    mav.addObject("serialNoAccess", serialNoAccess);

                } catch (Exception e) {
                    LOGGER.error("查看公文时抛出异常：", e);
                }
                mav.addObject("nodePermissionPolicy", tempMitem);

                mav.addObject("nodePermissionPolicyKey", nodePermissionPolicy);

                Hashtable<String, String> locHs = SharedWithThreadLocal.getLocHs();
                if (locHs == null) {
                    locHs = edocFormManager.getOpinionLocation(summary.getFormId(), flowPermAccountId);
                }

                // ----------------公文查看性能优化，注释掉下面
                String element = locHs.get(nodePermissionPolicy);
                mav.addObject("position", Strings.isBlank(element) ? "" : element.substring(0, element.indexOf("_")));

                List<String> baseActions = permissionManager.getBasicActionList(edocTypeEnum.name(),
                        nodePermissionPolicy, flowPermAccountId);
                if (baseActions == null) {
                    LOGGER.error("edocController.summary : baseActions is null! [edocTypeEnum.name():"
                            + edocTypeEnum.name() + " nodePermissionPolicy:" + nodePermissionPolicy
                            + " flowPermAccountId:" + flowPermAccountId + " summary.id:" + summary.getId());
                }
                // 常用操作
                List<String> commonActions = permissionManager.getCommonActionList(edocTypeEnum.name(),
                        nodePermissionPolicy, flowPermAccountId);

                // 高级操作
                List<String> advancedActions = permissionManager.getAdvaceActionList(edocTypeEnum.name(),
                        nodePermissionPolicy, flowPermAccountId);

                // 公文所属单位的公文收发员
                if (("fengfa".equals(nodePermissionPolicy))
                        || (baseActions != null && baseActions.contains("EdocExchangeType"))) {
                    Map<String, Object> map = EdocHelper.getOrgExchangeMembersAndDeptSenders(summary);// 取被代理人的封发部门
                    mav.addObject("createrExchangeDepts", map.get("deptSenderList"));
                    mav.addObject("memberList", map.get("memberList"));
                    mav.addObject("defaultExchangeType",
                            EdocSwitchHelper.getDefaultExchangeType(summary.getOrgAccountId()));

                    // 封发人部门取所有单位的部门
                    Set<Long> deptSenderList = EdocHelper.getDeptSenderList(affair.getMemberId());
                    StringBuilder deptList = new StringBuilder();
                    // String deptList="";
                    for (Long deptId : deptSenderList) {
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

                    mav.addObject("deptSenderList", deptList);

                    String exchangeDeptTypeSwitchValue = EdocSwitchHelper
                            .getDefaultExchangeDeptType(summary.getOrgAccountId());
                    mav.addObject("exchangeDeptTypeSwitchValue", exchangeDeptTypeSwitchValue);

                }

                // 如果选择不同意则弹出
                boolean isdealStepBackShow = false;
                boolean isdealStepStopShow = false;
                boolean isdealCancelShow = false;
                if ((commonActions != null && commonActions.contains("Return"))
                        || (advancedActions != null && advancedActions.contains("Return"))) {
                    isdealStepBackShow = true;
                }
                if ((commonActions != null && commonActions.contains("Terminate"))
                        || (advancedActions != null && advancedActions.contains("Terminate"))) {
                    isdealStepStopShow = true;
                }
                if ((commonActions != null && commonActions.contains("Cancel"))
                        || (advancedActions != null && advancedActions.contains("Cancel"))) {
                    isdealCancelShow = true;
                }
                mav.addObject("isdealStepBackShow", isdealStepBackShow);
                mav.addObject("isdealStepStopShow", isdealStepStopShow);
                mav.addObject("isdealCancelShow", isdealCancelShow);
                String contentPrint = "false";
                String contentSaveLocal = "false";

                if (((affair.getState().intValue() == StateEnum.col_sent.getKey()) || (affair.getState().intValue() == StateEnum.col_waitSend.getKey()))
                        && !"lenPotent".equals(openFrom) && !"docLib".equals(openFrom)) {
                    contentPrint = "true";
                    contentSaveLocal = "true";
                } else {
                    if ("listSent".equals(from) || "listWaitSend".equals(from) || "sended".equals(from)
                            || "sendRegisterResult".equals(_openFrom) || "recRegisterResult".equals(_openFrom)) {
                        contentPrint = "true";
                        contentSaveLocal = "true";
                    } else {
                        if (Strings.isNotBlank(lenPotents)) {
                            contentSaveLocal = "0".equals(lenPotents.substring(1, 2)) ? "false" : "true";
                            contentPrint = "0".equals(lenPotents.substring(2, 3)) ? "false" : "true";
                        } else {
                            if ((baseActions != null && baseActions.contains("PrintContentAcc"))) {
                                contentPrint = "true";
                            }
                            if (baseActions != null && baseActions.contains("SaveContentAcc")) {
                                contentSaveLocal = "true";
                            }
                        }
                    }
                }
                mav.addObject("contentPrint", contentPrint);
                mav.addObject("contentSaveLocal", contentSaveLocal);
                if (baseActions != null && baseActions.contains("Print")) {
                    affairCanPrint = true;
                }
                boolean docPluginExist = AppContext.hasPlugin(ApplicationCategoryEnum.doc.name());
                if (!docPluginExist && baseActions != null && baseActions.contains("Archive")) {
                    baseActions.remove("Archive");
                }

                if (!docPluginExist && commonActions.contains("DepartPigeonhole")) {
                    commonActions.remove("DepartPigeonhole");
                }
                if (!docPluginExist && advancedActions.contains("DepartPigeonhole")) {
                    advancedActions.remove("DepartPigeonhole");
                }
                // 是否有公告模块
                boolean bulletinPluginExist = AppContext.hasPlugin(ApplicationCategoryEnum.bulletin.name());
                if (!bulletinPluginExist && commonActions.contains("TransmitBulletin")) {
                    commonActions.remove("TransmitBulletin");
                }
                if (!bulletinPluginExist && advancedActions.contains("TransmitBulletin")) {
                    advancedActions.remove("TransmitBulletin");
                }
                // 是否有日常时间模块
                boolean calendarPluginExist = AppContext.hasPlugin(ApplicationCategoryEnum.calendar.name());
                if (!calendarPluginExist && commonActions.contains("Transform")) {
                    commonActions.remove("Transform");
                }
                if (!calendarPluginExist && advancedActions.contains("Transform")) {
                    advancedActions.remove("Transform");
                }
                // 判断是否能进行归档操作：
                boolean canArchive = false;
                if (baseActions != null && baseActions.contains("Archive") && !summary.getHasArchive()) { // 封发及自定义节点可以进行归档操作
                    canArchive = true;
                }
                // 把常用操作和高级操作合并到一个list中，用于控制显示8个后，其余的显示到更多中
                List<String> totalActions = new ArrayList<String>();
                // 修复bug GOV-3039 公文管理-收文管理-待阅，点击一条公文，弹出null
                if (commonActions != null) {
                    totalActions.addAll(commonActions);
                }
                if (advancedActions != null) {
                    totalActions.addAll(advancedActions);
                }
                int roleEdocType = summary.getEdocType() == 1 ? 3 : summary.getEdocType();
                boolean isSendEdocCreateRole = EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(),
                        AppContext.getCurrentUser().getId(), roleEdocType);
                if (summary.getFinished() && baseActions != null) {
                    baseActions.remove("Track");
                }
                mav.addObject("canArchive", canArchive);
                mav.addObject("baseActions", baseActions);
                mav.addObject("advancedActions", advancedActions);
                mav.addObject("commonActions", commonActions);
                mav.addObject("isRemoveForward", !isSendEdocCreateRole);// 是否不显示转发
                AppContext.putSessionContext("canFavorite", false);

                // 1需要转过收文的，才能显示收文信息
                // 2上级单位转过来的，下级单位待办查看时也要显示转收文信息
                EdocExchangeTurnRec turnRec = edocExchangeTurnRecManager
                        .findEdocExchangeTurnRecByEdocId(summary.getId());

                if (turnRec == null) {
                    Long superEdocId = edocExchangeTurnRecManager.findSupEdocId(summary.getId());
                    if (superEdocId != null) {
                        turnRec = edocExchangeTurnRecManager.findEdocExchangeTurnRecByEdocId(superEdocId);
                    }
                }

                if (turnRec != null) {
                    // ****************查看收文明细时，如果当前登录人在收文流程中(生成了待办的)，就可以看到[转收文信息](从关联文档，转发文，文档中心都这么处理)********************
                    if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()) {
                        mav.addObject("isCanViewTurnRecEdoc", "false");
                        List<StateEnum> states = new ArrayList<StateEnum>();
                        states.add(StateEnum.col_sent);
                        states.add(StateEnum.col_pending);
                        states.add(StateEnum.col_done);
                        List<CtpAffair> affairs = affairManager.getAffairs(summary.getId(), states);
                        for (CtpAffair af : affairs) {
                            // 首页需要是收文流程的 affair
                            if (af.getApp().intValue() == ApplicationCategoryEnum.edocRec.key()
                                    && af.getMemberId().longValue() == user.getId().longValue()) {
                                mav.addObject("isCanViewTurnRecEdoc", "true");
                                break;
                            }
                        }
                    }
                }
            }

            Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
            CtpEnumBean comMetadata = enumManagerNew.getEnum(EnumNameEnum.common_remind_time.name());
            CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
            // attitude
            colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
            CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
            // attitude
            colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);

            mav.addObject("comMetadata", comMetadata);
            mav.addObject("colMetadata", colMetadata);
            mav.addObject("summary", summary);
            mav.addObject("isShowButton", false);

            mav.addObject("appTypeName", EdocEnum.getEdocAppName(summary.getEdocType()));

            mav.addObject("curUser", AppContext.getCurrentUser());

            // 得到公文发起人
            long edocSendUserId = summary.getStartUserId();
            V3xOrgMember edocSendMember = orgManager.getMemberById(edocSendUserId);
            // OA-36876
            // weblogic环境：q1在兼职单位gwgw下新建公文，节点人员q1封发节点，gwgw下有收发员，q1待办处理时，能看到单位收发员有人，但是提交的时候却提示请设置单位收发员
            // 单位id要去 发起公文时所在的单位id,因为单位收发员是根据发起公文时所在的单位来取的
            // 因为有两种情况 1 用户切换到兼职单位后发文(发给自己)，封发 2 用户在本单位发文后(发给自己)，再切换到兼职单位封发
            edocSendMember.setOrgAccountId(summary.getOrgAccountId());
            mav.addObject("edocSendMember", edocSendMember);

            mav.addObject("controller", "edocController.do");

            // 督办开始
            Set<String> idSets = new HashSet<String>();
            StringBuilder supervisorId = new StringBuilder(); // supervisorId :
            // all the ids
            // of supervise
            // detail
            StringBuilder tempIds = new StringBuilder(); // tempIds : all the
            // ids of
            // superviseTemplate

            CtpSuperviseDetail detail = this.superviseManager.getSupervise(summaryId);

            if (detail != null) {
                List<CtpSupervisor> supervisors = superviseManager.getSupervisors(detail.getId());
                // Set<CtpSupervisor> supervisors = detail.getColSupervisors();
                Set<Long> userSuper = new HashSet<Long>();
                for (CtpSupervisor supervisor : supervisors) {
                    userSuper.add(supervisor.getSupervisorId().longValue());
                    idSets.add(supervisor.getSupervisorId().toString());
                }
                if (null != summary && null != summary.getTempleteId()) {

                    CtpSuperviseDetail tempDetail = superviseManager.getSupervise(summary.getTempleteId());

                    if (null != tempDetail) {
                        List<CtpSupervisor> tempVisors = superviseManager.getSupervisors(tempDetail.getId());

                        for (CtpSupervisor ts : tempVisors) {
                            if (userSuper.contains(ts.getSupervisorId())) {
                                idSets.add(ts.getSupervisorId().toString());
                                tempIds.append(ts.getSupervisorId().toString() + ",");
                            }
                        }

                        List<CtpSuperviseTemplateRole> roleList = superviseManager
                                .findRoleByTemplateId(summary.getTempleteId());
                        V3xOrgRole orgRole = null;
                        V3xOrgMember starter = orgManager.getMemberById(summary.getStartUserId());
                        if (null != starter) {
                            for (CtpSuperviseTemplateRole role : roleList) {
                                if (null == role.getRole() || "".equals(role.getRole())) {
                                    continue;
                                }
                                if (role.getRole().toLowerCase()
                                        .equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase())) {
                                    if (userSuper.contains(starter.getId())) {
                                        idSets.add(String.valueOf(starter.getId()));
                                        tempIds.append(starter.getId() + ",");
                                    }
                                }
                                if (role.getRole().toLowerCase()
                                        .equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase()
                                                + EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER.toLowerCase())) {
                                    orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_DEPMANAGER,
                                            summary.getOrgAccountId());
                                    if (null != orgRole) {
                                        List<V3xOrgDepartment> depList = orgManager
                                                .getDepartmentsByUser(starter.getId());
                                        for (V3xOrgDepartment dep : depList) {
                                            List<V3xOrgMember> managerList = orgManager.getMembersByRole(dep.getId(),
                                                    orgRole.getId());
                                            for (V3xOrgMember mem : managerList) {
                                                if (userSuper.contains(mem.getId())) {
                                                    idSets.add(mem.getId().toString());
                                                    tempIds.append(mem.getId() + ",");
                                                }
                                            }
                                        }
                                    }
                                }
                                if (role.getRole().toLowerCase()
                                        .equals(EdocOrgConstants.ORGENT_META_KEY_SEDNER.toLowerCase()
                                                + EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER.toLowerCase())) {
                                    orgRole = orgManager.getRoleByName(EdocOrgConstants.ORGENT_META_KEY_SUPERDEPMANAGER,
                                            summary.getOrgAccountId());
                                    if (null != orgRole) {
                                        List<V3xOrgDepartment> depList = orgManager
                                                .getDepartmentsByUser(starter.getId());
                                        for (V3xOrgDepartment dep : depList) {
                                            List<V3xOrgMember> superManagerList = orgManager
                                                    .getMembersByRole(dep.getId(), orgRole.getId());
                                            for (V3xOrgMember mem : superManagerList) {
                                                if (userSuper.contains(mem.getId())) {
                                                    idSets.add(mem.getId().toString());
                                                    tempIds.append(mem.getId() + ",");
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                for (String id : idSets) {
                    supervisorId.append(id + ",");
                }
                if (supervisorId.length() > 0) {
                    mav.addObject("supervisorId", supervisorId.substring(0, supervisorId.length() - 1));
                }
                mav.addObject("superviseId", detail.getId());
                mav.addObject("supervisors", detail.getSupervisors());
                mav.addObject("superviseTitle", detail.getTitle());
                mav.addObject("awakeDate",
                        Datetimes.format(detail.getAwakeDate(), Datetimes.datetimeWithoutSecondStyle));
                mav.addObject("superviseTitle", detail.getTitle());
                mav.addObject("count", idSets.size());
                if (idSets.contains(String.valueOf(user.getId())) && flageState) {
                    mav.addObject("isSupervis", true);
                    isSupervisor = true;
                }

                mav.addObject("openModal", request.getParameter("openModal"));
                mav.addObject("bean", detail);
                if (tempIds.length() > 0) {
                    String unCancelledVisor = tempIds.substring(0, tempIds.length() - 1);
                    mav.addObject("unCancelledVisor", unCancelledVisor);
                    mav.addObject("sVisorsFromTemplate", "true");// 公文调用的督办模板是否设置了督办人
                }
            }

            boolean templateFlag = false;
            if (summary.getTempleteId() != null)
                templateFlag = true;

            // 文单打印权限的控制 -------------
            /**
             * 关于openFrom：从公文档案库传递过来的时候值为edocDocLib,普通文档库（包括共享）为docLib,
             * 借阅的时候为lenPotent
             * 从文档相关消息框弹出来的时候为"null"，暂时只发现这几个值，但是每种情况都会传递参数lenPotent，
             * 所以这里判断文单打印权限的时候使用lenPotent来判断
             */
            String printEdocTable = "";
            if ("true".equals(request.getParameter("isLibOwner"))) { // 文档库相关
                printEdocTable = "true";
            } else if (Strings.isNotBlank(lenPotents)) {
                printEdocTable = "0".equals(lenPotentPrint) ? "false" : "true";
            } else if ("glwd".equals(openFrom)) {// 从关联文档点击进来的，都不可以打印
                printEdocTable = "false";
            } else if ("listWaitSend".equals(from) ||
                    "listSent".equals(from)/*从首页和普通列表打开*/ || "Sent".equals(from)/*首页更多打开*/ || "sendRegisterResult".equals(_openFrom) || "recRegisterResult".equals(_openFrom)) { // 已发送和保存待发
                printEdocTable = "true";
            } else if (isSupervisor && !"Pending".equals(from)) {// 督办节点都能打印
                printEdocTable = "true";
            } else {
                printEdocTable = affairCanPrint ? "true" : "false";
            }
            mav.addObject("printEdocTable", printEdocTable);
            // -----

            Map<String, List<String>> map = parse2WebActionList(actions, "true".equals(printEdocTable) ? true : false);
            mav.addObject("toolBarActions", map.get("toolBar"));
            mav.addObject("moreActions", map.get("more"));
            mav.addObject("affairId", _affairId);
            mav.addObject("templateFlag", templateFlag);
            /**
             * 由于协同督办传递的参数值为openFrom，公文统一全部是from,为了影响改动范围，先调整公文的参数接受方式 wangwei
             */
            String superviseFrom = request.getParameter("from");
            if (Strings.isBlank(superviseFrom)) {
                superviseFrom = request.getParameter("openFrom");
            }
            if ("supervise".equals(superviseFrom)
                    && summary.getState() != CollaborationEnum.flowState.finish.ordinal()) {
                mav.addObject("showHastenButton", "true");
            }
            if ("docLib".equals(superviseFrom) || "Done".equals(superviseFrom)) {
                mav.addObject("showHastenButton", "false");
            }
            String isOpenFrom = request.getParameter("isOpenFrom");

            /** 是否包含流程回退/撤销产生的追溯数据开始 **/
            List<WorkflowTracePO> dataByParams = traceWorkflowManager.getDataByParams(affair.getObjectId(),
                    affair.getActivityId(), affair.getMemberId());
            if (null != dataByParams && dataByParams.size() > 0
                    && ("listPending".equals(superviseFrom) || ("Pending").equals(superviseFrom))) {
                // mav.addObject("hasWorkflowDataaButton", true);
                if (!Integer.valueOf(WorkflowTraceEnums.workflowTrackType.step_back_normal.getKey())
                        .equals(dataByParams.get(0).getTrackType()) && null != affair
                        && affair.getState().equals(Integer.valueOf(StateEnum.col_pending.getKey()))) {
                    mav.addObject("hasWorkflowDataaButton", true);
                }

            }
            /** 是否包含流程回退/撤销产生的追溯数据结束 **/
            V3xOrgMember orgMember = orgManager.getMemberById(affair.getMemberId());
            if (orgMember != null) {
                mav.addObject("affairMemberName", orgMember.getName());
            }
            mav.addObject("affairMemberId", affair.getMemberId());
            mav.addObject("isOpenFrom", isOpenFrom);
            mav.addObject("from", superviseFrom);
            mav.addObject("newPdfIdFirst", UUIDLong.longUUID());
            mav.addObject("newPdfIdSecond", UUIDLong.longUUID());
            if (summary.getFirstBody() != null && "OfficeWord".equals(summary.getFirstBody().getContentType())) {
                mav.addObject("firstBodyContent", summary.getFirstBody().getContent());
            }
            // ------------公文查看性能优化，删除ThreadLocal中保存的
            SharedWithThreadLocal.removeView();
            mav.addObject("isFromSDL", superviseFrom);
            mav.addObject("trackTypeRecord", request.getParameter("trackTypeRecord"));

            //同步消息
            userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), affair.getId());

            return mav;
        } catch (Exception e) {
            LOGGER.error("查看公文时抛出异常：", e);
            StringBuffer sb = new StringBuffer();
            // super.printV3XJS(out);
            sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(e.getMessage()) + "\");");
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"true\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.getA8Top().reFlesh();");
            sb.append("}");
            sb.append("");
            rendJavaScript(response, sb.toString());

            return null;
        }

    }

    private boolean isEffectiveAffair(Long objectId, Long activityId, Long memId) throws BusinessException {
        boolean flag = true;
        List<CtpAffair> affairsByActivityId = affairManager.getAffairsByObjectIdAndNodeId(objectId, activityId);
        List<Long> _memId = new ArrayList<Long>();
        for (int a = 0; a < affairsByActivityId.size(); a++) {
            _memId.add(affairsByActivityId.get(a).getMemberId());
        }
        if (!Strings.isEmpty(affairsByActivityId) && !_memId.contains(memId)) {
            flag = false;
        }
        return flag;
    }

    private void getRelationSummaryInfo(ModelAndView mav, long summaryId) {

        User user = AppContext.getCurrentUser();
        String relSends = AppContext.getRawRequest().getParameter("relSends");
        String relRecs = AppContext.getRawRequest().getParameter("relRecs");
        if (Strings.isBlank(relSends)) {
            relSends = "haveNot";

            long relationSummaryId = summaryId;
            List<EdocSummary> newEdocList = null;
            String edocType = AppContext.getRawRequest().getParameter("edocType");
            String detailType = AppContext.getRawRequest().getParameter("detailType");

            /********* puyc 关联发文 * 收文的Id,收文的Type *********/
            if (Strings.isBlank(detailType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(relationSummaryId, user.getId(), 1);
            } else if (!"listSent".equals(detailType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(relationSummaryId,
                        user.getId(), 1, 2);
            }
            if (Strings.isEmpty(newEdocList) && "1".equals(edocType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(relationSummaryId, user.getId(), 1);
            }

            if (Strings.isNotEmpty(newEdocList)) {
                relSends = "haveMany";
                mav.addObject("recEdocId", relationSummaryId);
                mav.addObject("recType", 1);
                mav.addObject("relSends", relSends);
            }
        }

        if (Strings.isBlank(relRecs)) {
            relRecs = "haveNot";

            String canNotOpen = AppContext.getRawRequest().getParameter("canNotOpen");
            String sendSummaryId = AppContext.getRawRequest().getParameter("sendSummaryId"); // 从发文关联收文点击进来的,这个是发文summaryId

            EdocSummaryRelation edocSummaryRelationR = this.edocSummaryRelationManager.findRecEdoc(summaryId, 0);
            if (edocSummaryRelationR != null) {
                relRecs = "haveMany";
                if ("isYes".equals(canNotOpen) || Strings.isNotBlank(sendSummaryId)) {
                    relRecs = "haveNot";
                }
                mav.addObject("relRecs", relRecs);
            }
        }
    }

    public ModelAndView superviseDiagram(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocDiagramSupervise");
        String s_summaryId = request.getParameter("summaryId");
        long summaryId = Long.parseLong(s_summaryId);
        CtpSuperviseDetail detail = this.superviseManager.getSupervise(summaryId);
        if (detail != null) {
            boolean isSupervis = false;
            String supervisors = detail.getSupervisors();
            String[] supervisorsArry = supervisors.split("、");
            for (String str : supervisorsArry) {
                if (str.equals(AppContext.getCurrentUser().getName())) {
                    isSupervis = true;
                    break;
                }
            }

            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
            modelAndView.addObject("finished", summary.getFinished());
            modelAndView.addObject("isSupervis", isSupervis);
            String title = detail.getTitle();
            if (Strings.isNotBlank(title)) {
                title = title.replace("<br/>", "\n\r");
            }
            detail.setTitle(title);
            modelAndView.addObject("bean", detail);
        }
        modelAndView.addObject("summaryId", s_summaryId);
        return modelAndView;
    }

    private Map<String, List<String>> parse2WebActionList(Map<String, List<String>> actions,
                                                          boolean isEdocFormCanPrint) {

        List<String> advancedActions = actions.get("advanced");
        List<String> commonActions = actions.get("common");
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        int toolBarShowSize = 8;// 页面工具条显示的按钮数
        // 同意排序，工具条显示8个，其余的显示到高级操作中
        // List<String> all = new ArrayList<String>();
        // if(commonActions!=null) all.addAll(commonActions);
        // if(advancedActions!=null) all.addAll(advancedActions);
        // all.add("edocFormPrint");
        // all.add("logDetail");
        // int allSize = all.size();
        // map.put("toolBar", allSize>=toolBarShowSize ?
        // all.subList(0,toolBarShowSize):all.subList(0,allSize));
        // map.put("more", all.size()>toolBarShowSize ?
        // all.subList(toolBarShowSize,allSize):null);
        //
        List<String> more = new ArrayList<String>();
        if (commonActions != null) {
            if (commonActions.size() <= toolBarShowSize) {
                map.put("toolBar", commonActions);
            } else {
                map.put("toolBar", commonActions.subList(0, toolBarShowSize));
                more.addAll(commonActions.subList(toolBarShowSize, commonActions.size()));
            }
        } else {
            map.put("toolBar", null);
        }

        if (advancedActions != null)
            more.addAll(advancedActions);

        more.add("edocFormPrint");
        more.add("logDetail");

        map.put("more", more);
        return map;
    }

    public ModelAndView wendanTaohong(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView("edoc/wendantaohong");
        String s_summaryId = request.getParameter("summaryId");
        long summaryId = Long.parseLong(s_summaryId);
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
        mv.addObject("summary", summary);
        mv.addObject("body", summary.getFirstBody());
        String tempContentType = request.getParameter("tempContentType");
        mv.addObject("tempContentType", tempContentType);
        return mv;
    }

    public ModelAndView wendanTaohongIframe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView("edoc/wendantaohongIframe");
        String s_summaryId = request.getParameter("summaryId");
        mv.addObject("summaryId", s_summaryId);
        String tempContentType = request.getParameter("tempContentType");
        mv.addObject("tempContentType", tempContentType);
        return mv;
    }

    public ModelAndView getContent2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocTopic");
        String s_summaryId = request.getParameter("summaryId");
        long summaryId = Long.parseLong(s_summaryId);
        User user = AppContext.getCurrentUser();
        CtpAffair affair = affairManager.getSenderAffair(summaryId);
        // SECURITY 访问安全检查
        boolean showType = false;
        String sendSummaryId = request.getParameter("sendSummaryId");
        String openFrom = request.getParameter("openFrom");

        if ("lenPotent".equals(openFrom)) {
            showType = true;
        }

        if (Strings.isNotBlank(sendSummaryId)) {// 从关联文点击进来的
            // 发文关联收文，在发文流程上的人有权限看收文
            List<CtpAffair> affairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc,
                    Long.parseLong(sendSummaryId));
            for (int i = 0; i < affairs.size(); i++) {
                if (user.getId().longValue() == affairs.get(i).getMemberId().longValue()) {
                    showType = true;
                }
            }
        }
        if (!showType) {
            if (!SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.edoc, user, summaryId, affair,
                    null)) {
                return null;
            }
        } else {// BUG_紧急_V5_V5.0sp2_青岛地铁集团有限公司_打开协同中的关联文档报错：您无权限查看该主题_20150213007240_如果跳过安全验证，就要加入安全缓存
            AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.edoc, String.valueOf(summaryId),
                    user.getId());
        }

        String lenPotents = request.getParameter("lenPotent");
        String lenPotent = "0";
        if (lenPotents != null && !"".equals(lenPotents)) {
            lenPotent = lenPotents.substring(0, 1);
        }

        String officecanPrint = "true";
        String officecanSaveLocal = "true";
        String onlySeeContent = "false";

        if (lenPotents != null && !"".equals(lenPotents)) {
            officecanSaveLocal = "0".equals(lenPotents.substring(1, 2)) ? "false" : "true";
        }
        if (lenPotents != null && !"".equals(lenPotents)) {
            officecanPrint = "0".equals(lenPotents.substring(2, 3)) ? "false" : "true";
        }

        if (Byte.toString(DocConstants.LENPOTENT_CONTENT).equals(lenPotent)) {
            onlySeeContent = "true";
        }
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
        modelAndView.addObject("summary", summary);

        modelAndView.addObject("body", summary.getFirstBody());

        EdocFormModel fm = new EdocFormModel();
        fm.setEdocSummary(summary);
        fm.setEdocFormId(summary.getFormId());
        fm.setEdocBody(summary.getFirstBody());
        modelAndView.addObject("formModel", fm);

        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("contentRecordId", summary.getEdocBodiesJs());

        // 记录操作日志
        String docResId = request.getParameter("docId");

        /**
         * operationlogManager.insertOplog(docResId, summaryId,
         * ApplicationCategoryEnum.doc, ActionType.LOG_DOC_VIEW,
         * ActionType.LOG_DOC_VIEW+".desc",
         * AppContext.getCurrentUser().getName(), summary.getSubject());
         **/
        if ("glwd".equals(openFrom))// 从关联文档点击进来的，都不可以打印
        {
            officecanPrint = "false";
        }

        modelAndView.addObject("docId", docResId);
        modelAndView.addObject("officecanPrint", officecanPrint);
        modelAndView.addObject("officecanSaveLocal", officecanSaveLocal);
        modelAndView.addObject("onlySeeContent", onlySeeContent);
        modelAndView.addObject("openFrom", openFrom);

        return modelAndView;
    }

    /**
     * 显示转换后的公文
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getOfficeTransContent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String summaryId = request.getParameter("summaryId");
        EdocSummary summary = this.edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
        ModelAndView mav = new ModelAndView("edoc/edocTransContent");
        boolean officeTransformConversion = OfficeTransHelper.isOfficeTran();
        StringBuilder buf = new StringBuilder();
        if (officeTransformConversion) {
            String fileId = summary.getFirstBody().getContent();
            try {
                V3XFile v3XFile = fileManager.getV3XFile(Long.valueOf(fileId));
                if (OfficeTransHelper.allowTrans(v3XFile)) {
                    String htmlURL = OfficeTransHelper.buildCacheUrl(v3XFile, false);
                    buf.append("<iframe id=\"officeTransIframe\" style=\"width:96%;height:99%;margin-left:10px;margin-right:10px;\" src=\"");
                    buf.append(htmlURL);
                    buf.append("\"></iframe>");
                } else {
                    buf.append("<script type='text/javascript'>$.alert('" + ResourceUtil.getString("edoc.alert.content.canNotTrans") + "')</script>");
                }
            } catch (Exception e) {
                LOGGER.error("ID为\"" + fileId + "\"的文件获取失败！", e);
            }
        } else {
            buf.append("<script type='text/javascript'>$.alert('" + ResourceUtil.getString("edoc.alert.content.officeTransDisabled") + "')</script>");
        }
        mav.addObject("htmlContent", buf.toString());
        return mav;
    }

    /**
     * 显示公文内容和处理信息
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getContent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocTopic");
        // 初始化定义subState，防止传递前台为null,导致IE8下js变量取值错误
        int affairSubState = 0;
        String openFrom = request.getParameter("openFrom");
        String lenPotents = request.getParameter("lenPotent");
        String lenPotent = "0";
        if (lenPotents != null && !"".equals(lenPotents)) {
            lenPotent = lenPotents.substring(0, 1);
        }
        String officecanPrint = "true";
        String officecanSaveLocal = "true";
        String isTransFrom = request.getParameter("isTransFrom");
        modelAndView.addObject("isTransFrom", isTransFrom);
        boolean onlySeeContent = false;
        boolean showType = false;
        if ("lenPotent".equals(openFrom) || "glwd".equals(openFrom) || "edocDocLib".equals(openFrom)) {
            showType = true;
            if (Byte.toString(DocConstants.LENPOTENT_CONTENT).equals(lenPotent)) {
                onlySeeContent = true;
                return getContent2(request, response);
            }
        }

        String s_summaryId = request.getParameter("summaryId");
        long summaryId = Long.parseLong(s_summaryId);

        EdocSummary summary = null;
        String docMarkByTemplateJs = EdocHelper.exeTemplateMarkJs(null, null, null);
        String archiveModifyId = request.getParameter("archiveModifyId"); // 归档修改日志id
        if (Strings.isBlank(archiveModifyId)) {
            summary = edocManager.getEdocSummaryById(summaryId, true);
            if (summary.getTempleteId() != null) {
                CtpTemplate template = templeteManager.getCtpTemplate(summary.getTempleteId());
                if (null != template) {
                    EdocSummary templateSummary = (EdocSummary) XMLCoder.decoder(template.getSummary());
                    docMarkByTemplateJs = EdocHelper.exeTemplateMarkJs(templateSummary.getDocMark(),
                            templateSummary.getDocMark2(), templateSummary.getSerialNo());
                }
            }
        }
        modelAndView.addObject("docMarkByTemplateJs", docMarkByTemplateJs);
        modelAndView.addObject("summary", summary);

        modelAndView.addObject("body", summary == null ? null : summary.getFirstBody());

        if (summary == null) {
            LOGGER.error("summary is null ,summaryId:" + summaryId);
            return modelAndView;
        }

        User user = AppContext.getCurrentUser();
        String openFromAffair = "";

        // 根据affairId得到权限的处理ID
        long permId = -1;
        String _affairId = request.getParameter("affairId");
        CtpAffair affair = null;
        String nodePermissionPolicy = "";
        if (Strings.isNotBlank(_affairId)) {
            Long affairId = null;
            try {
                affairId = Long.valueOf(_affairId);
            } catch (Exception ex) {
                LOGGER.error("显示公文内容和处理信息，string类型转换抛出异常：", ex);
            }

            if (null == affairId && "supervise".equals(request.getParameter("openFrom"))) {
                CtpAffair sAffair = affairManager.getSenderAffair(summaryId);
                affairId = sAffair.getId();
            }

            affair = affairManager.get(affairId);

            if (affair.getState() == StateEnum.col_waitSend.getKey()
                    || affair.getState() == StateEnum.col_sent.getKey()) {
                openFromAffair = "waitsendOrSended";
            }

            nodePermissionPolicy = affair.getNodePolicy();
            String catType = EdocUtil.getEdocMetadataNameEnum(summary.getEdocType()).name();
            if ("collaboration".equalsIgnoreCase(nodePermissionPolicy)) {
                nodePermissionPolicy = "niwen";
                if (summary.getEdocType() == EdocEnum.edocType.recEdoc.ordinal()) {
                    nodePermissionPolicy = "dengji";
                }
            }
            try {
                Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, summary.getOrgAccountId());
                if (nodePermissionPolicy != null) {
                    Permission fp = permissionManager.getPermission(catType, nodePermissionPolicy, flowPermAccountId);
                    permId = fp.getFlowPermId();
                    String baseAction = fp.getBasicOperation();
                    if (baseAction.indexOf("PrintContentAcc") < 0) {
                        officecanPrint = "false";
                    }
                    if (baseAction.indexOf("SaveContentAcc") < 0) {
                        officecanSaveLocal = "false";
                    }
                }
            } catch (Exception e) {
                LOGGER.error("显示公文内容和处理信息，节点权限抛出异常：", e);
            }
        }
        if (affair == null) {
            affair = affairManager.getSenderAffair(summaryId);
        }
        /* puyc修改 发文关联收文，发文流程上面的所有人都可以看到关联收文 */
        String sendSummaryId = request.getParameter("sendSummaryId");

        if (Strings.isNotBlank(sendSummaryId)) {// 从关联文点击进来的
            // 发文关联收文，在发文流程上的人有权限看收文
            List<CtpAffair> affairs = affairManager.getValidAffairs(ApplicationCategoryEnum.edoc,
                    Long.parseLong(sendSummaryId));
            for (int i = 0; i < affairs.size(); i++) {
                if (user.getId().longValue() == affairs.get(i).getMemberId()) {
                    showType = true;
                }
            }
        }
        // 收文关联发文不需要进行验证
        if ("true".equals(request.getParameter("openEdocByForward"))) {
            showType = true;
        }
        if (!showType) {
            if (!"transEvent".equals(isTransFrom)) {
                if (!SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.edoc, user, summaryId, affair,
                        null)) {
                    return null;
                }
            }
        } else {// BUG_OA-75228_紧急_V5_V5.0sp2_青岛地铁集团有限公司_打开协同中的关联文档报错：您无权限查看该主题_20150213007240_如果跳过安全验证，就要加入安全缓存
            AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.edoc, String.valueOf(summaryId),
                    user.getId());
        }
        /* puyc修改 完毕 */
        // 自动获取签发日期， 封发日期
        if ("fengfa".equals(nodePermissionPolicy) && summary.getPackTime() == null) {
            summary.setPackTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        if ("qianfa".equals(nodePermissionPolicy) && summary.getSigningDate() == null) {
            summary.setSigningDate(new java.sql.Date(System.currentTimeMillis()));
        }
        if (affair.getSubState() != null) {
            affairSubState = affair.getSubState();
        }
        modelAndView.addObject("subState", affairSubState);
        modelAndView.addObject("affairId", _affairId);
        EdocFormModel fm = edocFormManager.getEdocFormModel(summary.getFormId(), summary, permId, affair);
        fm.setEdocBody(summary.getFirstBody());
        // &符号转义-BUG_普通_V5_V5.1sp1_致远客服部_公文单有"<"，在导入的时候会提示“公文单数据出现异常！错误原因：解析XML失败”_20150626010068.在seeyonForm3.js有对应转换。搜bug编号
        String xsl = fm.getXslt();
        xsl = xsl.replaceAll("&", "&amp;");
        fm.setXslt(xsl);
        modelAndView.addObject("formModel", fm);

        /***********************************
         * 唐桂林 公文意见显示 start
         *************************************/
        // 公文处理意见回显到公文单,排序
        long flowPermAccout = EdocHelper.getFlowPermAccountId(user.getLoginAccount(), summary, templeteManager);
        // 查找公文单意见元素显示。
        FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(),
                flowPermAccout);
        EdocFormExtendInfo edocFormExtendInfo = edocFormManager.getEdocOpinionConfig(summary.getFormId(),
                flowPermAccout);
        Map<String, EdocOpinionModel> map = edocManager.getEdocOpinion(summary, displayConfig);
        List<EdocFormFlowPermBound> policyList = edocFormManager.findBoundByFormId(summary.getFormId(), flowPermAccout,
                nodePermissionPolicy);
        String policy = "";
        if (Strings.isNotEmpty(policyList)) {
            policy = policyList.get(0).getProcessName();
        } else {
            List<EdocElement> elementList = edocFormManager.getEdocFormElementByFormIdAndFieldName(summary.getFormId(),
                    "otherOpinion");
            if (Strings.isNotEmpty(elementList)) {
                policy = elementList.get(0).getFieldName();
            }
        }
        if (Strings.isBlank(policy)) {
            policy = nodePermissionPolicy;
        }

        modelAndView.addObject("policy", policy);

        // 是否是退回的状态
        modelAndView.addObject("affairState", EdocOpinionDisplayUtil.getAffairReturnState(map, policy, affair));
        if (edocFormExtendInfo != null)
            modelAndView.addObject("optionId", edocFormExtendInfo.getId());
        modelAndView.addObject("optionType", displayConfig.getOpinionType());
        boolean isFromPending = "Pending".equals(request.getParameter("from"));
        List<V3xHtmDocumentSignature> signatuers = htmSignetManager.findBySummaryIdAndType(summary.getId(),
                V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.getKey());
        Map<String, Object> strMap = EdocOpinionDisplayUtil.convertOpinionToString(map, displayConfig, affair,
                isFromPending, signatuers);
        modelAndView.addObject("opinionsJs", EdocOpinionDisplayUtil.optionToJs(strMap));
        // 发起人意见
        modelAndView.addObject("senderOpinion", strMap.get("senderOpinionList"));
        modelAndView.addObject("senderOpinionAttStr", strMap.get("senderOpinionAttStr"));
        /***********************************
         * 唐桂林 公文意见显示 end
         *************************************/

        // 公文单手写批注回显
        boolean isSender = Strings.equals(user.getId(), summary.getStartUserId());
        List<String> ols = edocFormManager.getOpinionElementLocationNames(summary.getFormId(), flowPermAccout);
        String hwjs = htmlHandWriteManager.getHandWritesJs(summaryId, user.getName(), ols);
        if (onlySeeContent) {
            hwjs = "<Script language='JavaScript'>hwObjs=new Array();</Script>";
        }
        modelAndView.addObject("isSender", isSender);
        modelAndView.addObject("hwjs", hwjs);

        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("contentRecordId", summary.getEdocBodiesJs());

        // 判断是否是部门归档
        String _docId = request.getParameter("docId");
        Long docResId = null;
        boolean isDeptPigeonhole = false;
        if (AppContext.hasPlugin("doc") && Strings.isNotBlank(_docId)) {
            docResId = Long.parseLong(_docId);
            DocResourceBO doc = docApi.getDocResource(docResId);
            if (doc == null) {
                super.printV3XJS(response.getWriter());
                super.rendJavaScript(response,
                        "alert(getA8Top().dialogArguments.v3x.getMessage('DocLang.doc_alert_source_deleted_doc'));"
                                + "getA8Top().window.returnValue = true;" + "getA8Top().window.close();");
                return null;
            }

            if (Integer.valueOf(1).equals(doc.getPigeonholeType())) {
                isDeptPigeonhole = true;
            }
        }
        modelAndView.addObject("isDeptPigeonhole", isDeptPigeonhole);

        // 公文可以移动以后需要兼容其他的情况
        if (Strings.isNotBlank(lenPotents)) {
            officecanPrint = "false";
            officecanSaveLocal = "false";

            officecanSaveLocal = "0".equals(lenPotents.substring(1, 2)) ? "false" : "true";
            officecanPrint = "0".equals(lenPotents.substring(2, 3)) ? "false" : "true";

            modelAndView.addObject("docId", docResId);
        } else if ("glwd".equals(openFrom)) {// 从关联文档点击进来的，都不可以打印
            officecanPrint = "false";
        } else if ("waitsendOrSended".equals(openFromAffair)) {
            officecanPrint = "true";
            officecanSaveLocal = "true";
        }

        // boolean isUnit=false;//是否是联合发文
        if (null != summary) {
            int edocType = summary.getEdocType();
            String category = "";
            if (edocType == EdocEnum.edocType.sendEdoc.ordinal()) {
                category = "edoc_send_permission_policy";
            } else if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
                category = "edoc_rec_permission_policy";
            } else if (edocType == EdocEnum.edocType.signReport.ordinal()) {
                category = "edoc_qianbao_permission_policy";
            }
            String opins = this.getOpinionName(category, user.getAccountId());
            modelAndView.addObject("opn", opins);
        }

        /********** puyc *************/
        // 关联收文
        String relRecs = request.getParameter("relRecs");
        Long recEdocId = -1L;
        String relationUrl = "";
        if ("haveMany".equals(relRecs)) {
            EdocSummaryRelation edocSummaryRelationR = this.edocSummaryRelationManager.findRecEdoc(summaryId, 0);
            if (edocSummaryRelationR != null) {
                // OA-33377
                // 收文转发文，发文发送后。把收文撤销流程。然后在发文-关联收文中打开收文，查看属性状态，应该是待发，现在是已发。
                recEdocId = edocSummaryRelationR.getRelationEdocId();// 收文Id
                // 收文affairId (从那儿转发的，就是对应收文列表数据的affairId[待登记，已发，待办，已办])
                Long recAffairId = edocSummaryRelationR.getRecAffairId();
                // ***当从待登记转发文时，关联的是签收id,这时不需要recAffairId,因为还没有收文
                if (recAffairId != null) {
                    CtpAffair reAffair = affairManager.get(recAffairId);
                    // 当收文被撤销时，那么就要获得收文待发的affairId
                    if (reAffair.getState() == StateEnum.col_cancel.key()) {
                        CtpAffair waitAffair = affairManager.getAffairs(recEdocId, StateEnum.col_waitSend).get(0);
                        recAffairId = waitAffair.getId();
                    }
                }

                relationUrl = this.relationReceive(recEdocId + "", "1") + "&affairId=" + recAffairId;// 收文已发或待发的affairId
                relationUrl += "&openEdocByForward=true";
                modelAndView.addObject("relationUrl", relationUrl);// 收文的url
                modelAndView.addObject("relRecs", relRecs);
            }
        }
        /********** puyc***end **********/

        modelAndView.addObject("officecanPrint", officecanPrint);
        modelAndView.addObject("officecanSaveLocal", officecanSaveLocal);
        modelAndView.addObject("openFrom", openFrom);
        long edocAccountId = 0L;
        // 模板的公文开关权限应该取模板制作单位的权限控制
        if (summary.getTempleteId() != null) {
            CtpTemplate t = templeteManager.getCtpTemplate(summary.getTempleteId());
            if (null != t) {
                edocAccountId = t.getOrgAccountId();
            } else {
                edocAccountId = summary.getOrgAccountId();
            }
        }
        // 不是模板时，取公文所属单位权限控制
        else {
            edocAccountId = summary.getOrgAccountId();
        }
        modelAndView.addObject("personInput", EdocSwitchHelper.canInputEdocWordNum(edocAccountId));
        modelAndView.addObject("canTransformToPdf", true);
        modelAndView.addObject("isBoundSerialNo", edocMarkDefinitionManager
                .getEdocMarkByTempleteId(summary.getTempleteId(), MarkCategory.serialNo) == null ? false : true);

        String logoURL = EdocHelper.getLogoURL(summary.getOrgAccountId());
        modelAndView.addObject("logoURL", logoURL);

        // 需要获得创建公文的单位的开关_正文套红日期
        boolean taohongriqiSwitch = EdocSwitchHelper.taohongriqiSwitch(summary.getOrgAccountId());
        modelAndView.addObject("taohongriqiSwitch", taohongriqiSwitch);
        return modelAndView;
    }

    // 减签前的人员检查
    public ModelAndView preDeletePeople(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String _summaryId = request.getParameter("summary_id");
        String _affairId = request.getParameter("affairId");
        Long summaryId = Long.parseLong(_summaryId);
        Long affairId = Long.parseLong(_affairId);
        String processId = request.getParameter("processId");

        ModelAndView mv = new ModelAndView("edoc/decreaseNodes");

        CtpAffair affair = affairManager.get(affairId);
        if (affair.getState() != StateEnum.col_pending.key()) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            String msg = EdocHelper.getErrorMsgByAffair(affair);
            super.printV3XJS(out);
            out.println("<script>");
            out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(msg) + "\")");
            out.println("if(window.dialogArguments){"); // 弹出
            out.println("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
            out.println("  window.close();");
            out.println("}else{");
            out.println("  getA8Top().reFlesh();");
            out.println("}");
            out.println("</script>");
            out.close();
            return null;
        }

        mv.addObject("summmaryId", summaryId);
        mv.addObject("affairId", affairId);
        mv.addObject("processId", processId);
        return mv;
    }

    // 多级会签匹配人员
    public ModelAndView preAddMoreSign(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // User user = AppContext.getCurrentUser();
        String selObj = request.getParameter("selObj");
        // String appName = request.getParameter("appName");
        ModelAndView mv = new ModelAndView("edoc/addPeopleSelect");
        if (selObj != null) {
            List<MoreSignSelectPerson> msps = edocManager.findMoreSignPersons(selObj);
            mv.addObject("msps", msps);

            // int flowPermType = 1;
            List<Permission> nodePolicyList = null;
            /*
             * String nodeMetadataName="";
             * if("sendEdoc".equalsIgnoreCase(appName) ||
             * "edocSend".equals(appName)){
             * nodeMetadataName=EnumNameEnum.edoc_send_permission_policy.name();
             * }else if("recEdoc".equalsIgnoreCase(appName) ||
             * "edocRec".equals(appName)){
             * nodeMetadataName=EnumNameEnum.edoc_rec_permission_policy.name();
             * }else if("signReport".equalsIgnoreCase(appName) ||
             * "edocSign".equals(appName)){
             * nodeMetadataName=EnumNameEnum.edoc_qianbao_permission_policy.name
             * (); }
             */
            // 取得节点权限所属单位ID，分为两种情况，1.公文发起人所在的单位ID，2.模板所属单位的ID
            // String _summaryId=request.getParameter("summary_id");
            // Long summaryId = Long.parseLong(_summaryId);
            // EdocSummary summary = edocManager.getEdocSummaryById(summaryId,
            // false);
            // long accountId=EdocHelper.getFlowPermAccountId(summary,
            // user.getLoginAccount(), templeteManager);
            // nodePolicyList =
            // permissionManager.getFlowpermsByStatus(nodeMetadataName,
            // Permission.Node_isActive, false, flowPermType,accountId);
            mv.addObject("nodePolicyList", nodePolicyList);
        }
        return mv;
    }

    // 多级会签
    public ModelAndView addMoreSign(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String _summaryId = request.getParameter("summaryId");
        String _affairId = request.getParameter("affairId");
        Long summaryId = Long.parseLong(_summaryId);
        Long affairId = Long.parseLong(_affairId);
        String processId = request.getParameter("processId");

        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);

        // boolean isRelieveLock = true;
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            CtpAffair affair = affairManager.get(affairId);
            if (affair.getState() != StateEnum.col_pending.key()) {
                // String msg=ColHelper.getErrorMsgByAffair(affair);
                super.printV3XJS(out);
                out.println("<script>");
                // out.println("alert(\"" +
                // StringEscapeUtils.escapeJavaScript(msg) + "\")");
                out.println("if(window.dialogArguments){"); // 弹出
                out.println("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
                out.println("  window.close();");
                out.println("}else{");
                out.println("  getA8Top().reFlesh();");
                out.println("}");
                out.println("</script>");
                out.close();
                return null;
            }

            // 先增加自己
            String caseLogXML = null;
            String caseProcessXML = null;
            String caseWorkItemLogXML = null;

            if (summary != null) {
                if (summary.getCaseId() != null) {
                    long caseId = summary.getCaseId();
                    caseLogXML = edocManager.getCaseLogXML(caseId);
                    caseWorkItemLogXML = edocManager.getCaseWorkItemLogXML(caseId);
                } else if (summary.getProcessId() != null && !"".equals(summary.getProcessId())) {
                    caseProcessXML = edocManager.getProcessXML(processId);
                }
            }

            caseProcessXML = Strings.escapeJavascript(caseProcessXML);
            caseLogXML = Strings.escapeJavascript(caseLogXML);
            caseWorkItemLogXML = Strings.escapeJavascript(caseWorkItemLogXML);

            return null;
        } catch (Exception e) {
            LOGGER.error("多级会签抛出异常：", e);
        } finally {
            /*
             * //if(isRelieveLock) ColLock.getInstance().removeLock(summaryId);
             */
        }
        return null;
    }

    /**
     * 撤消流程，变待发(收文分发取回功能，数据到达待分发)
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView cancelBackEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        User user = AppContext.getCurrentUser();
        String[] _summaryIds = new String[]{};
        String repealComment = request.getParameter("repealComment"); // 撤销附言
        _summaryIds = request.getParameterValues("id");

        boolean isRelieveLock = true;
        try {
            int result = 0;
            StringBuilder info = new StringBuilder();
            // lijl添加空值判断,如果为空则提示"流程撤销错误!"
            if (_summaryIds != null) {
                if (_summaryIds.length > 0) {
                    for (int i = 0; i < _summaryIds.length; i++) {
                        Long summaryId = Long.parseLong(_summaryIds[i]);

                        Long lockUserId = null;
                        lockUserId = edocLockManager.canGetLock(summaryId, user.getId());
                        if (lockUserId != null) {
                            LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,summaryId" + summaryId);
                            return null;
                        }

                        try {

                            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
                            if (summary.getFinished()) {
                                result = 1;
                            }

                            edocManager.edocBackCancelSummary(user.getId(), summaryId, repealComment, "");

                            if (result == 1) {
                                info.append("《").append(summary.getSubject()).append("》");
                            } else {
                                // 收文撤销流程-从退件箱中撤销
                                if (summary != null && summary.getEdocType() == 1) {
                                    EdocRegister edocRegister = edocRegisterManager
                                            .findRegisterByDistributeEdocId(summary.getId());
                                    if (edocRegister != null) {
                                        edocRegister.setDistributeState(
                                                EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());
                                        edocRegisterManager.update(edocRegister);
                                    }
                                }

                                try {
                                    // indexManager.getIndexManager().deleteFromIndex(EdocUtil.getAppCategoryByEdocType(summary.getEdocType()),summary.getId());
                                    if (AppContext.hasPlugin("index")) {
                                        indexManager.delete(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("撤销公文流程，更新全文检索异常", e);
                                }

                            }
                            try {
                                // 解锁正文文单
                                unLock(user.getId(), summary);
                            } catch (Exception e) {
                                LOGGER.error("解锁正文文单抛出异常：", e);
                            }
                        } finally {
                            edocLockManager.unlock(summaryId);
                        }
                    }
                } else {
                    String alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "edoc.back.flow.error");
                    out.println("<script>");
                    out.println("<!--");
                    out.println("alert('" + alertStr + "');");
                    out.println("//-->");
                    out.println("</script>");
                    return null;
                }
            } else {
                String alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.back.flow.error");
                out.println("<script>");
                out.println("<!--");
                out.println("alert('" + alertStr + "');");
                out.println("//-->");
                out.println("</script>");
                return null;
            }

            super.printV3XJS(out);
            if (info.length() > 0) {
                String alertStr = "";
                alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.state.end.alert", info.toString());
                if (alertStr.indexOf("$") > -1) {
                    alertStr = alertStr.replace("$", "");
                }

                if (result == 1) {
                    out.println("<script>");
                    out.println("<!--");
                    out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(alertStr) + "\");");
                    out.println("//-->");
                    out.println("</script>");
                }
                return null;
            }
            out.println("<script>");
            out.println("if(window.dialogArguments){"); // 弹出
            out.println("  window.returnValue = \"true\";");
            out.println("  window.close();");
            out.println("}else{");
            out.println("  parent.location.reload(true);");
            out.println("}");
            out.println("</script>");
            return null;
        } catch (Exception e) {
            LOGGER.error("撤消流程，变待发抛出异常：", e);
        } finally {
            if (isRelieveLock) {
                for (int i = 0; i < _summaryIds.length; i++) {
                    /*
                     * //ColLock.getInstance().removeLock(Long.parseLong(
                     * _summaryIds[i]));
                     */
                }
            }
        }
        return null;
    }

    /**
     * 公文已发撤销时，弹出撤销附言框
     */
    public ModelAndView repealCommentDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/repealCommentDialog");
        String _affairId = request.getParameter("affairId");
        if (Strings.isNotBlank(_affairId)) {
            CtpAffair ctpAffair = affairManager.get(Long.valueOf(_affairId));
            if (null != ctpAffair && null != ctpAffair.getTempleteId()) {
                CtpTemplate ctpTemplate = templeteManager.getCtpTemplate(ctpAffair.getTempleteId());
                mav.addObject("template", ctpTemplate.getCanTrackWorkflow());
            }
        }
        mav.addObject("affairId", _affairId);
        return mav;
    }

    /**
     * 公文处理界面撤销时，弹出撤销附言框
     */
    public ModelAndView repealDetailDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/repealDetailDialog");
        String _affairId = request.getParameter("affairId");
        if (Strings.isNotBlank(_affairId)) {
            CtpAffair ctpAffair = affairManager.get(Long.valueOf(_affairId));
            if (null != ctpAffair && null != ctpAffair.getTempleteId()) {
                CtpTemplate ctpTemplate = templeteManager.getCtpTemplate(ctpAffair.getTempleteId());
                mav.addObject("template", ctpTemplate.getCanTrackWorkflow());
            }
        }
        mav.addObject("affairId", _affairId);
        return mav;
    }

    /**
     * 撤消流程，已发变待发(收文分发取回功能，数据到达待分发)
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView repeal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        User user = AppContext.getCurrentUser();
        String[] _summaryIds = new String[]{};
        String page = request.getParameter("page");
        StringBuilder info = new StringBuilder();
        /****/
        String _trackWorkflowType = request.getParameter("trackWorkflowType");

        String _affairId = request.getParameter("affairId");
        String repealComment = request.getParameter("repealComment"); // 撤销附言
        // lijl添加,为了区分是撤销流程还是取回

        String docBack = request.getParameter("docBack");// docBack取回/cancelColl撤销
        // GOV-4082 发文流程撤销确定后没有反应。另发现撤销和退回拟稿人功能重复
        if (Strings.isBlank(docBack)) {
            docBack = "";
        }

        if ("workflowManager".equals(page)) {
            String[] summaryIdArr = {request.getParameter("summaryId")};
            _summaryIds = summaryIdArr;
        } else {
            _summaryIds = request.getParameterValues("id");
            if ("dealrepeal".equals(page)) {
                repealComment = request.getParameter("content");
            }
        }

        boolean isRelieveLock = true;
        String processId = "";
        Long summaryIdLong = null;
        EdocSummary summary = null;
        try {

            int result = 0;
            List<CtpAffair> doneList = null;
            // lijl添加空值判断,如果为空则提示"流程撤销错误!"
            if (_summaryIds != null) {
                if (_summaryIds.length > 0) {

                    CtpAffair _affair = null;
                    // affair状态校验需要放到 获取processId之后进行，因为还需要在finally中进行解锁
                    if (Strings.isNotBlank(_affairId)) {
                        _affair = affairManager.get(Long.parseLong(_affairId));
                        // 当公文不是待办/在办的状态时，不能撤销操作
                        if (_affair.getState() != StateEnum.col_pending.key()
                                || _affair.getState() != StateEnum.col_sent.key()) {
                            String msg = EdocHelper.getErrorMsgByAffair(_affair);
                            if (Strings.isNotBlank(msg)) {
                                StringBuffer sb = new StringBuffer();
                                sb.append("alert('" + msg + "');");
                                sb.append("parent.doEndSign_dealrepeal('true');");
                                rendJavaScript(response, sb.toString());
                                return null;
                            }
                        }
                    }

                    for (int i = 0; i < _summaryIds.length; i++) {
                        Long summaryId = Long.parseLong(_summaryIds[i]);
                        summary = edocManager.getEdocSummaryById(summaryId, false);
                        processId = summary.getProcessId();
                        summaryIdLong = summary.getId();

                        if (summary.getFinished()) {
                            result = 1;
                        }
                        // else if(summary.getHasArchive()){result=2;}
                        else {
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
                                // 收文撤销，取回，数据到达待分发。
                                if (1 == summary.getEdocType()) {
                                    // affairManager.deleteByObject(ApplicationCategoryEnum.edocRecDistribute,
                                    // summary.getId());
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
                                        // GOV-3328
                                        // （需求检查）【公文管理】-【收文管理】-【分发】，已分发纸质公文撤销后到待分发列表中了，应该在草稿箱中
                                        if ("docBack".equals(docBack)) {// 取回
                                            edocRegister.setDistributeEdocId(-1l);
                                            edocRegister.setDistributeState(
                                                    EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal());// 将状态设置为"未分发"
                                        } else {
                                            // GOV-4848.【公文管理】-【收文管理】，收文分发员在已分发列表将已分发的收文删除，收文待办人处理时退回公文，退回的数据不见了
                                            // start
                                            edocRegister.setDistributeEdocId(summaryId);
                                            edocRegister.setDistributeState(
                                                    EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());// 将状态设置为"草稿"
                                            // GOV-4848.【公文管理】-【收文管理】，收文分发员在已分发列表将已分发的收文删除，收文待办人处理时退回公文，退回的数据不见了
                                            // end
                                        }

                                        // 撤销与取回时，登记对象退回状态为：0
                                        edocRegister.setIsRetreat(0);// 非退回
                                        edocRegisterManager.update(edocRegister);

                                        // 删除收文(暂物理删除) 撤销/取回都要删除分发数据
                                        summary.setState(CollaborationEnum.flowState.deleted.ordinal());

                                    } else {
                                        EdocRecieveRecord record = recieveEdocManager
                                                .getEdocRecieveRecordByReciveEdocId(summaryId);
                                        if (record != null) {
                                            // 删除收文(暂物理删除) 撤销/取回都要删除分发数据
                                            summary.setState(CollaborationEnum.flowState.deleted.ordinal());
                                        } else {
                                            if ("docBack".equals(docBack)) {
                                                result = 4;
                                                isCancel = false;
                                            }
                                        }
                                    }
                                }
                                // 可以取回或撤销
                                if (isCancel) {
                                    // OA-19935
                                    // 客户bug验证：流程是gw1，gw11，m1，串发，m1撤销，gw1在待发直接查看（不是编辑态），文单上丢失了撤销的意见
                                    EdocOpinion repealOpinion = new EdocOpinion();
                                    if (Strings.isNotBlank(_affairId)) {
                                        repealOpinion.setAffairId(Long.parseLong(_affairId));
                                    }
                                    String policy = request.getParameter("policy");
                                    repealOpinion.setPolicy(policy);

                                    repealOpinion.setNeedRepealRecord(_trackWorkflowType);
                                    result = edocManager.cancelSummary(user.getId(), summaryId, _affair, repealComment,
                                            docBack, repealOpinion);
                                }
                            }
                        }
                        if (result == 1 || result == -1
                                || ((result == 3 || result == 4) && "docBack".equals(docBack))) {
                            info.append("《").append(summary.getSubject()).append("》");
                        } else {
                            try {
                                // 已发撤销后，需要删除已经发出去的全文检索文件
                                if (AppContext.hasPlugin("index")) {
                                    indexManager.delete(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
                                }
                            } catch (Exception e) {
                                LOGGER.error("撤销公文流程，更新全文检索异常", e);
                            }
                            // 撤销流程事件
                            CollaborationCancelEvent event = new CollaborationCancelEvent(this);
                            event.setSummaryId(summary.getId());
                            event.setUserId(user.getId());
                            event.setMessage(repealComment);
                            EventDispatcher.fireEvent(event);
                            // 发送消息给督办人，更新督办状态，并删除督办日志、删除督办记录、删除催办次数

                            superviseManager.updateStatus2Cancel(summaryId);
                        }
                        try {
                            // 解锁正文文单
                            unLock(user.getId(), summary);
                        } catch (Exception e) {
                            LOGGER.error("解锁正文文单抛出异常：", e);
                        }
                    }
                } else {
                    String alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                            "edoc.back.flow.error");
                    out.println("<script>");
                    out.println("<!--");
                    out.println("alert('" + alertStr + "');");
                    out.println("//-->");
                    out.println("</script>");
                    return null;
                }
            } else {
                String alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.back.flow.error");
                out.println("<script>");
                out.println("<!--");
                out.println("alert('" + alertStr + "');");
                out.println("//-->");
                out.println("</script>");
                return null;
            }

            super.printV3XJS(out);
            if (info.length() > 0) {
                String alertStr = "";
                // 取回
                if ("docBack".equals(docBack)) {
                    if (result == 1) {
                        // 流程已结束
                        alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "edoc.back.state.end.alert", info.toString());
                    } else if (result == 3) {// 取回
                        // 公文${0}正在处理中，不能进行取回操作。
                        alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "edoc.is.running", info.toString());
                    } else if (result == 4) {// 取回
                        // 纸质收文{0}不允许进行取回操作
                        alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "edoc.rec.notBack", info.toString());
                    } else if (result == -1) {
                        alertStr = ResourceUtil.getString("edoc.alert.flow.cannotTakeback", info.toString());// 公文"+info.toString()+"后面节点任务事项已处理完成，不能取回
                    } else if (result == -2) {
                        alertStr = ResourceUtil.getString("edoc.alert.flow.cannotTakeback1", info.toString());// 公文"+info.toString()+"当前任务事项所在节点为知会节点，不能取回
                    }
                }
                // 撤销
                else {
                    if (result == 1) {
                        // 流程已结束
                        alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                                "edoc.state.end.alert", info.toString());
                    }
                }

                // 很奇怪，调用ResourceBundleUtil.getString之后，会在提示中加入$, 先暂时这样处理
                // changyi add
                if (alertStr.indexOf("$") > -1) {
                    alertStr = alertStr.replace("$", "");
                }

                if (((result == 3 || result == 4) && "docBack".equals(docBack)) || result == 1) {
                    out.println("<script>");
                    out.println("<!--");
                    out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(alertStr) + "\");");
                    out.println("//-->");
                    out.println("</script>");
                }
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("撤销流程时抛出异常：", e);
        } finally {
            // 目前撤销只能 一次执行一条
            if (isRelieveLock) {
                this.officeLockManager.unlockAll(summaryIdLong);
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
        if ("workflowManager".equals(page)) {
            out.println("<script>");
            out.println("if(window.dialogArguments){"); // 弹出
            out.println("  window.returnValue = \"true\";");
            out.println("  window.close();");
            out.println("}else{");
            out.println("  parent.ok();");
            out.println("}");
            out.println("</script>");
            out.close();
            return null;
        } else {
            out.println("<script>");
            out.println("if(window.dialogArguments){"); // 弹出
            out.println("  window.returnValue = \"true\";");
            out.println("  window.close();");
            out.println("}else{");
            if ("dealrepeal".equals(page)) {
                // GOV-2873 【公文管理】-【发文管理】-【待办】，处理待办公文时点击'撤销'报错
                out.println("parent.doEndSign_dealrepeal();");
            } else {
                out.println(" parent.location.href =  parent.location.href; ");
            }
            out.println("}");
            out.println("</script>");
            return null;
        }
    }

    // 回退
    public ModelAndView stepBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String _summaryId = request.getParameter("summary_id");
        String _affairId = request.getParameter("affairId");
        Long summaryId = Long.parseLong(_summaryId);
        Long affairId = Long.parseLong(_affairId);
        String processId = request.getParameter("processId");
        String _trackWorkflowType = request.getParameter("trackWorkflowType");
        boolean isRelieveLock = true;
        EdocSummary summary = null;
        Long lockUserId = edocLockManager.canGetLock(affairId, user.getId());
        if (lockUserId != null) {
            LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
            return null;
        }

        try {
            CtpAffair _affair = affairManager.get(affairId);
            // 补上退回时间
            StringBuffer sb = new StringBuffer();
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
                sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(errMsg) + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
                sb.append("  window.close();");
                sb.append("}else{");
                sb.append("  parent.parent.location.reload(true);");
                sb.append("}");
                // sb.append("//-->");
                rendJavaScript(response, sb.toString());
                return null;
            }

            // 保存回退时的意见,附件�?

            EdocOpinion oldOpinion = null;
            Set<EdocOpinion> opinions = summary.getEdocOpinions();
            if (opinions != null) {
                for (EdocOpinion o : opinions) {
                    if (affairId.equals(o.getAffairId())) {
                        oldOpinion = o;
                        break;
                    }
                }
            } else {
                oldOpinion = edocManager.findBySummaryIdAndAffairId(summaryId, affairId);
            }

            EdocOpinion signOpinion;
            if (oldOpinion != null) {
                signOpinion = oldOpinion;
            } else {
                signOpinion = new EdocOpinion();
                signOpinion.setIdIfNew();
            }
            bind(request, signOpinion);

            String content = request.getParameter("contentOP");
            signOpinion.setContent(content);

            String attitude = request.getParameter("attitude");
            if (!Strings.isBlank(attitude)) {
                signOpinion.setAttribute(Integer.valueOf(attitude).intValue());
            } else {
                signOpinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
            }
            // 回退的时候,选择归档,跟踪无效
            // String afterSign = request.getParameter("afterSign");

            signOpinion.isDeleteImmediate = false;// "delete".equals(afterSign);
            signOpinion.affairIsTrack = false;// "track".equals(afterSign);

            signOpinion.setIsHidden(request.getParameterValues("isHidden") != null);

            long nodeId = -1;
            if (request.getParameter("currentNodeId") != null && !"".equals(request.getParameter("currentNodeId"))) {
                nodeId = Long.parseLong(request.getParameter("currentNodeId"));
            }
            signOpinion.setNodeId(nodeId);

            // //设置代理人信息
            // 公文回退后，从待发编辑，文单中退回意见 多出了代理人名字，是由于回退时 意见保存时多出了代理人名
            if (user.getId().longValue() != _affair.getMemberId().longValue()) {
                signOpinion.setProxyName(user.getName());
            }
            signOpinion.setCreateUserId(_affair.getMemberId());
            signOpinion.setHasAtt(EdocHelper.isAddAttachmentByOpinion(request, summaryId));

            // 修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if (editHelper.hasEditAtt()) {// 是否修改附件
                attachmentManager.deleteByReference(summaryId, summaryId);// 删除公文附件
            }
            // 保存公文附件及回执附件，create方法中前台subReference传值为空，默认从java中传过去，
            // 因为公文附件subReference从前台js传值 过来了，而回执附件没有传subReference，所以这里传回执的id
            this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, signOpinion.getId(), request);
            if (editHelper.hasEditAtt()) {// 是否修改附件
                // 设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(processId), nodeId),
                        true, "stepBack");
            }

            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("affairState", request.getParameter("affairState"));
            paramMap.put("affState", request.getParameter("affState"));
            paramMap.put("optionType", request.getParameter("optionType"));
            paramMap.put("oldOpinion", oldOpinion);
            paramMap.put("trackWorkflowType", _trackWorkflowType);
            paramMap.put("edocSummary", summary);
            // true:成功回退 false:不允许回退
            boolean ok = edocManager.stepBack(summaryId, affairId, signOpinion, paramMap);
            // OA-68355


            // 不允许回退提示待完成

            // lijl添加,在退回时设置Edoc_Option的state的状态为1------------------------State
            String optionType = request.getParameter("optionType");
            // 1、3都表是只保留最后一条意见
            if ("1".equals(optionType) || "3".equals(optionType)) {
                signOpinion.setState(2);
                // 退回的时候要把以前是0的情况改为2
                edocManager.update(summaryId, user.getId(), signOpinion.getPolicy(), 2, 0);
            }

            if (AppContext.hasPlugin("index")) {
                indexManager.update(summaryId, ApplicationCategoryEnum.edoc.getKey());
            }
            // 记录应用日志
            if (ok) {
                appLogManager.insertLog(user, 317, user.getName(), summary.getSubject());
            }

            _affair.setSummaryState(summary.getState());
            // 记录操作时间
            affairManager.updateAffairAnalyzeData(_affair);

            // 回退成功后，跳到待办列表
            sb.append("parent.doEndSign_pending('" + affairId + "');");
            rendJavaScript(response, sb.toString());
            return null;
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {

            edocLockManager.unlock(affairId);

            if (isRelieveLock) {
                this.wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
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

        return super.refreshWorkspace();
    }

    /**
     * 会签
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView colAssign(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sSummaryId = request.getParameter("summaryId");
        String _affairId = request.getParameter("affairId");

        long summaryId = Long.parseLong(sSummaryId);
        long affairId = Long.parseLong(_affairId);
        // boolean isRelieveLock = true;
        try {

            CtpAffair affair = affairManager.get(affairId);
            PrintWriter out = null;
            if (affair.getState() != StateEnum.col_pending.key()) {
                response.setContentType("text/html;charset=UTF-8");
                out = response.getWriter();
                String msg = EdocHelper.getErrorMsgByAffair(affair);
                super.printV3XJS(out);
                out.println("<script>");
                out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(msg) + "\")");
                out.println("if(window.dialogArguments){"); // 弹出
                out.println("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
                out.println("  window.close();");
                out.println("}else{");
                out.println("  getA8Top().reFlesh();");
                out.println("}");
                out.println("</script>");
                out.close();
                return null;
            }

            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
            String caseLogXML = null;
            String caseProcessXML = null;
            String caseWorkItemLogXML = null;
            if (summary != null) {
                if (summary.getCaseId() != null) {
                    long caseId = summary.getCaseId();
                    caseLogXML = edocManager.getCaseLogXML(caseId);
                    caseWorkItemLogXML = edocManager.getCaseWorkItemLogXML(caseId);
                } else if (summary.getProcessId() != null && !"".equals(summary.getProcessId())) {
                    String processId = summary.getProcessId();
                    caseProcessXML = edocManager.getProcessXML(processId);
                }
            }

            caseProcessXML = Strings.escapeJavascript(caseProcessXML);
            caseLogXML = Strings.escapeJavascript(caseLogXML);
            caseWorkItemLogXML = Strings.escapeJavascript(caseWorkItemLogXML);

            return null;
        } catch (Exception e) {
            LOGGER.error("公文会签时抛出异常：", e);
        } finally {
            /*
             * //if(isRelieveLock) ColLock.getInstance().removeLock(summaryId);
             */
        }
        return null;

    }

    // 取回
    public ModelAndView takeBack(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String[] affairIds = request.getParameterValues("affairId");
        String[] summaryIds = request.getParameterValues("summaryId");
        boolean isRelieveLock = true;
        String processId = "";
        List<EdocSummary> summaryList = new ArrayList<EdocSummary>();
        try {
            StringBuilder info = new StringBuilder();
            // StringBuilder info1 = new StringBuilder();
            StringBuilder info2 = new StringBuilder();
            if (affairIds != null) {
                int i = 0;
                for (String affairId : affairIds) {
                    Long _affairId = Long.valueOf(affairId);
                    Long summaryId = Long.parseLong(summaryIds[i]);

                    CtpAffair affair = affairManager.get(_affairId);
                    boolean ok = edocManager.takeBack(_affairId);
                    if (ok == false) {
                        if (affair != null) {
                            info.append("《").append(affair.getSubject()).append("》").append("\n");
                        }
                    }
                    i++;
                    EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
                    processId = summary.getProcessId();
                    // 取回后，更新当前待办人
                    summary.setFinished(false);// 被取回设置结束为false
                    summary.setCompleteTime(null);
                    EdocHelper.updateCurrentNodesInfo(summary, true);

                    summaryList.add(summary);
                }
            }
            if (info.length() > 0) {
                WebUtil.saveAlert(ResourceBundleUtil.getString(
                        "com.seeyon.v3x.collaboration.resources.i18n.CollaborationResource", "takeBack.not.label",
                        info.toString()));
            }
            if (info2.length() > 0) {
                WebUtil.saveAlert(ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.state.end.takeback.archivealert", info2.toString()));
            }
            return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
        } catch (Exception e) {
            LOGGER.error("公文取回时抛出异常：", e);
        } finally {
            // 目前
            if (isRelieveLock) {
                // 解锁正文文单
                wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
                for (int i = 0; i < summaryIds.length; i++) {
                    wapi.releaseWorkFlowProcessLock(summaryIds[i], String.valueOf(AppContext.currentUserId()));
                }
                try {
                    User user = AppContext.getCurrentUser();
                    if (Strings.isNotEmpty(summaryList)) {
                        for (EdocSummary summary : summaryList) {
                            // 解锁正文文单
                            unLock(user.getId(), summary);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("解锁正文文单抛出异常：", e);
                }
            }
        }
        // 更新当前待办人

        return super.refreshWorkspace();
    }

    /**
     * 删除
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String[] affairIds = request.getParameterValues("affairId");
        String pageType = request.getParameter("pageType");
        if (affairIds != null) {
            for (String affairId : affairIds) {
                Long _affairId = Long.valueOf(affairId);
                Long logAffair = Long.valueOf(affairId);
                edocManager.deleteAffair(pageType, _affairId);
                /** puyc 添加日志 **/
                CtpAffair affair = affairManager.get(logAffair);
                int affairApp = affair.getApp();
                AppLogAction alog = null;
                if (affairApp == ApplicationCategoryEnum.edocSend.ordinal()) {
                    alog = AppLogAction.Edoc_Delete_Send;
                } else if (affairApp == ApplicationCategoryEnum.edocRec.ordinal()) {
                    alog = AppLogAction.Edoc_Delete_Rec;
                } else if (affairApp == ApplicationCategoryEnum.edocSign.ordinal()) {
                    alog = AppLogAction.Edoc_Delete_Sign;
                } else {
                    alog = AppLogAction.Edoc_delete;
                }
                User user = AppContext.getCurrentUser();
                appLogManager.insertLog(user, alog, user.getName(), affair.getSubject());
            }
        }
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
    }

    // 终止
    public ModelAndView stepStop(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String _summaryId = request.getParameter("summary_id");
        String _affairId = request.getParameter("affairId");
        String processId = request.getParameter("processId");
        Long summaryId = Long.parseLong(_summaryId);
        Long affairId = Long.parseLong(_affairId);
        boolean isRelieveLock = true;
        EdocSummary summary = null;
        Long lockUserId = null;
        lockUserId = edocLockManager.canGetLock(affairId, user.getId());
        if (lockUserId != null) {
            LOGGER.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
            return null;
        }
        try {

            CtpAffair _affair = affairManager.get(affairId);
            // 当公文不是待办/在办的状态时，不能终止操作
            if (_affair.getState() != StateEnum.col_pending.key()) {
                String msg = EdocHelper.getErrorMsgByAffair(_affair);
                if (Strings.isNotBlank(msg)) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("alert('" + msg + "');");
                    sb.append("parent.doEndSign_pending('" + affairId + "');");
                    rendJavaScript(response, sb.toString());
                    return null;
                }
            }

            summary = edocManager.getEdocSummaryById(summaryId, true);
            // 保存终止时的意见,附件�
            EdocOpinion signOpinion = new EdocOpinion();
            bind(request, signOpinion);
            // GOV-4932 终止后的公文，终止给出的意见不显示
            String content = request.getParameter("contentOP");
            signOpinion.setContent(content);

            // String attitude = request.getParameter("attitude");
            // if(!Strings.isBlank(attitude)){
            // signOpinion.setAttribute(Integer.valueOf(attitude).intValue());
            // }else{
            // signOpinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
            // }
            // OA-18228 待办中进行终止操作，终止后到已办理查看，态度显示仍然是普通的，不是终止
            signOpinion.setAttribute(OpinionType.stopOpinion.ordinal());

            String afterSign = request.getParameter("afterSign");

            signOpinion.isDeleteImmediate = "delete".equals(afterSign);
            signOpinion.affairIsTrack = "track".equals(afterSign);

            signOpinion.setIsHidden(request.getParameterValues("isHidden") != null);
            signOpinion.setIdIfNew();

            long nodeId = -1;
            if (request.getParameter("currentNodeId") != null && !"".equals(request.getParameter("currentNodeId"))) {
                nodeId = Long.parseLong(request.getParameter("currentNodeId"));
            }
            signOpinion.setNodeId(nodeId);

            // //设置代理人信息
            // OA-19210 兼职人员test02处理待办公文后，人员名称后还显示由自己代理
            if (user.getId().longValue() != _affair.getMemberId().longValue()) {
                signOpinion.setProxyName(user.getName());
            }
            signOpinion.setCreateUserId(_affair.getMemberId());

            // 修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if (editHelper.hasEditAtt()) {// 是否修改附件
                attachmentManager.deleteByReference(summaryId, summaryId);// 删除公文附件
            }
            // 保存公文附件及回执附件，create方法中前台subReference传值为空，默认从java中传过去，
            // 因为公文附件subReference从前台js传值 过来了，而回执附件没有传subReference，所以这里传回执的id
            this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, signOpinion.getId(), request);
            if (editHelper.hasEditAtt()) {// 是否修改附件
                // 设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(processId), nodeId),
                        true, "stepStop");
            }

            String pushMessageMembers = request.getParameter("pushMessageMemberIds");
            List<Long[]> pushMsgMemberList = parsePushMessageMemberList(pushMessageMembers);


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("edocOpinion", signOpinion);
            params.put("summaryId", summaryId);
            params.put("pushMsgMemberList", pushMsgMemberList);
            edocManager.transStepStop(affairId, params);

            // 终止成功后，跳到待办列表
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            super.printV3XJS(out);
            out.println("<script>");
            out.println("parent.close()");
            out.println("parent.doEndSign_pending('" + affairId + "');");
            out.println("</script>");
            out.close();
            return null;
        } catch (Exception e) {
            LOGGER.error("公文终止时抛出异常：", e);
        } finally {

            edocLockManager.unlock(affairId);

            if (isRelieveLock) {
                // 解锁正文文单
                wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
                wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
            }
            try {
                unLock(user.getId(), summary);
            } catch (Exception e) {
                LOGGER.error("解锁正文文单抛出异常：", e);
            }
        }
        return null;
    }

    /**
     * 流程管理界面直接终止流程
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author jincm 2008-6-05
     */
    public ModelAndView stopflow(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String _summaryId = request.getParameter("summaryId");
        Long summaryId = Long.parseLong(_summaryId);
        try {

            // 保存终止时的意见,附件�
            EdocOpinion signOpinion = new EdocOpinion();
            bind(request, signOpinion);

            String attitude = request.getParameter("attitude");
            if (!Strings.isBlank(attitude)) {
                signOpinion.setAttribute(Integer.valueOf(attitude).intValue());
            } else {
                signOpinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
            }

            signOpinion.isDeleteImmediate = false;
            signOpinion.affairIsTrack = false;
            signOpinion.setIsHidden(false);
            signOpinion.setIdIfNew();


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("edocOpinion", signOpinion);
            params.put("summaryId", summaryId);

            edocManager.transStepStop(null, params);
            // 终止成功后，跳到待办列表
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            super.printV3XJS(out);
            out.println("<script>");
            out.println("if(window.dialogArguments){"); // 弹出
            out.println("  window.returnValue = \"true\";");
            out.println("  window.close();");
            out.println("}else{");
            out.println("  parent.ok();");
            out.println("}");
            out.println("</script>");
            out.close();
            return null;
        } catch (Exception e) {
            LOGGER.error("公文终止流程时抛出异常：", e);
        } finally {
            /*
             * //if(isRelieveLock) ColLock.getInstance().removeLock(summaryId);
             */
        }
        return null;
    }

    // 公文已发，已办归档
    public ModelAndView pigeonhole(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // ModelAndView modelAndView = new ModelAndView("edoc/listDone");
        String[] affairIds = request.getParameterValues("affairId");
        String pageType = request.getParameter("pageType");
        // String edocType = request.getParameter("edocType");
        String _archiveId = request.getParameter("archiveId");
        String pigeonholeType = request.getParameter("pigeonholeType");
        Long archiveId = null;
        if (Strings.isNotBlank(_archiveId)) {
            archiveId = Long.parseLong(_archiveId);
        }

        StringBuilder sbInfo = new StringBuilder();
        StringBuilder accInfo = new StringBuilder();
        String nodePermissionPolicy;
        EnumNameEnum edocTypeEnum;
        StringBuilder needChoosePigeonholeSubject = new StringBuilder();// 需要选择归档归档的公文。
        StringBuilder needChoosePigeonholeIds = new StringBuilder();
        int successCount = 0;
        Set<String> filterSet = new HashSet<String>();
        if (affairIds != null) {
            for (String affairId : affairIds) {

                if (Strings.isBlank(affairId)) {
                    continue;
                }

                if (filterSet.contains(affairId)) {
                    continue;
                } else {
                    filterSet.add(affairId);
                }

                Long _affairId = Long.valueOf(affairId);
                CtpAffair affair = affairManager.get(_affairId);
                EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(), false);

                if (archiveId != null) {
                    summary.setArchiveId(archiveId);
                }
                boolean docResourceExist = false;
                if (AppContext.hasPlugin("doc")) {
                    docResourceExist = docApi.isDocResourceExisted(summary.getArchiveId());
                }

                if (summary.getArchiveId() == null || !docResourceExist) {
                    needChoosePigeonholeSubject.append("《").append(affair.getSubject()).append("》<br>");
                    needChoosePigeonholeIds.append(affair.getId()).append(",");
                }

                edocTypeEnum = EdocUtil.getEdocMetadataNameEnumByApp(affair.getApp());
                try {
                    nodePermissionPolicy = affair.getNodePolicy();
                    if ("finish".equals(pageType)) {// 已办事项，归档判断处理时，是否有归档权限

                        Long actionId = 0L; // 取节点权限所在单位的id。如果是调用了模板的话，要去取模板所在单位id的节点权限，避免兼职单位取值错误。
                        if (summary != null && summary.getTempleteId() != null) {
                            CtpTemplate t = templeteManager.getCtpTemplate(summary.getTempleteId());
                            actionId = t.getOrgAccountId();
                        } else {
                            actionId = summary.getOrgAccountId();
                        }

                        List<String> baseActions = permissionManager.getActionList(edocTypeEnum.name(),
                                nodePermissionPolicy, actionId, PermissionAction.basic);

                        if (baseActions.contains("Archive") == false) {
                            if (accInfo.length() > 0) {
                                accInfo.append(",");
                            }

                            accInfo.append(affair.getSubject());
                            continue;
                        }
                    }

                    // 是否有没有设置预归档路径的公文
                    if (summary.getArchiveId() != null && docResourceExist) {

                        affair.setNodePolicy(nodePermissionPolicy);
                        edocManager.pigeonholeAffair(pageType, _affairId, affair.getObjectId(), archiveId,
                                pigeonholeType);
                        successCount++; // 统计归档成功的个数。
                    }
                } catch (EdocException e) {
                    LOGGER.error("", e);
                    if (e.getErrNum() == EdocException.errNumEnum.workflow_not_finish.ordinal()) {
                        if (sbInfo.length() > 0) {
                            sbInfo.append(",");
                        }
                        sbInfo.append(e.getMessage());
                    } else {
                        throw e;
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }

        response.setContentType("text/html;charset=UTF-8");
        // PrintWriter out = response.getWriter();
        if (Strings.isNotBlank(needChoosePigeonholeIds.toString())) {// 假设有需要选择归档路径的公文
            String confirmInfo = "";
            if (successCount != 0) {
                confirmInfo = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.pigdoc.alert.list", successCount, needChoosePigeonholeSubject);
            } else {// 没有成功归档的。
                confirmInfo = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.pigdoc.alert.list.needchoose", needChoosePigeonholeSubject);
            }
            String js = "if(confirm('" + confirmInfo.replace("<br>", "\\r\\n") + "')){\r\n" + "var selectIds = '"
                    + needChoosePigeonholeIds.substring(0, needChoosePigeonholeIds.length() - 1) + "';\r\n"
                    + "var ids=document.getElementsByName('id');for(var i=0;i<ids.length;i++){"
                    + "if(selectIds.indexOf(ids[i].affairId)!=-1)ids[i].checked=true;}" + "doPigeonhole('new',"
                    + ApplicationCategoryEnum.edoc.key() + ",'listDone','');\r\n"
                    + "var archiveId = document.getElementById('archiveId').value;\r\n"
                    + "if(archiveId)pigeonholeForEdoc();}";

            WebUtil.saveJavascript(js);
        } else if (sbInfo.length() > 0) {// 有流程未结束的
            String alertInfo = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                    "edoc.pigdoc.alert.list.success", successCount);
            WebUtil.saveAlert(alertInfo);
        } else {// 所有的都设有预归档目录，不需要选择。
            String alertInfo = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                    "edoc.pigdoc.alert.list.success", successCount);
            WebUtil.saveAlert(alertInfo);
        }
        return new ModelAndView("edoc/refreshWindow").addObject("windowObj", "parent");
    }

    public void setEdocMarkHistoryManager(EdocMarkHistoryManager edocMarkHistoryManager) {
        this.edocMarkHistoryManager = edocMarkHistoryManager;
    }

    // 系统管理员主页面
    public ModelAndView sysMain(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/sysMain");
        return modelAndView;
    }

    // 系统管理员主页面
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView sysCompanyMain(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 分版的时候如果没有预置公文相关的数据，进行数据补偿
        EdocHelper.compensate(AppContext.currentAccountId());

        ModelAndView modelAndView = new ModelAndView("edoc/sysComanyMainIframe");
        return modelAndView;
    }

    // 系统管理员主页面外层框架，主要用于模板页面调用外层框架中的5.0的编辑流程控件
    @CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
    public ModelAndView sysCompanyMainIframe(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/sysComanyMain");
        boolean isGroupVer = false;

        isGroupVer = (Boolean) SysFlag.sys_isGroupVer.getFlag();

        modelAndView.addObject("isGroupVer", isGroupVer);
        return modelAndView;
    }

    /**
     * 暂存待办
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView doZCDB(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        long summaryId = Long.parseLong(request.getParameter("summary_id"));
        long affairId = Long.parseLong(request.getParameter("affair_id"));
        String processId = request.getParameter("processId");
        boolean isRelieveLock = true;

        EdocSummary summary = null;

        try {

            EdocOpinion opinion = new EdocOpinion();
            bind(request, opinion);
            opinion.setIdIfNew();
            opinion.setIsHidden(request.getParameterValues("isHidden") != null);
            String attitude = request.getParameter("attitude");
            if (!Strings.isBlank(attitude)) {
                opinion.setAttribute(Integer.valueOf(attitude).intValue());
            } else {
                opinion.setAttribute(com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
            }
            String content = request.getParameter("contentOP");
            opinion.setContent(content == null ? "" : content);
            opinion.setHasAtt(EdocHelper.isAddAttachmentByOpinion(request, summaryId));

            // String afterSign = request.getParameter("afterSign");
            // affair.setIsTrack("track".equals(afterSign));
            String[] afterSign = request.getParameterValues("afterSign");
            Set<String> options = new HashSet<String>();
            if (afterSign != null) {
                for (String option : afterSign) {
                    options.add(option);
                }
            }
            // 允许处理后归档和跟踪被同时选中，但是他们都不能和删除按钮同时选中
            if (options.size() > 1 && options.contains("delete")) {
                options.remove("delete");
            }
            boolean track = options.contains("track");
            // 跟踪
            String trackMembers = request.getParameter("trackMembers");
            String trackRange = request.getParameter("trackRange");
            // 不跟踪 或者 全部跟踪的时候不向部门跟踪表中添加数据，所以将下面这个参数串设置为空。
            if (!track || "1".equals(trackRange))
                trackMembers = "";
            edocManager.setTrack(affairId, track, trackMembers);

            long nodeId = -1;
            if (request.getParameter("currentNodeId") != null && !"".equals(request.getParameter("currentNodeId"))) {
                nodeId = Long.parseLong(request.getParameter("currentNodeId"));
            }
            opinion.setNodeId(nodeId);

            String spMemberId = request.getParameter("supervisorId");
            String superviseDate = request.getParameter("awakeDate");
            String supervisorNames = request.getParameter("supervisors");
            String title = request.getParameter("superviseTitle");

            summary = edocManager.getEdocSummaryById(summaryId, true);

            String deadlineDatetime = (String) request.getParameter("deadLineDateTime");
            if (Strings.isNotBlank(deadlineDatetime)) {
                summary.setDeadlineDatetime(DateUtil.parse(deadlineDatetime, "yyyy-MM-dd HH:mm"));
            }

            // 为了保存流程日志中修改附件的记录在处理提交之前，所以将保存附件的操作提前了。bug29527
            // 保存附件
            String oldOpinionIdStr = request.getParameter("oldOpinionId");
            if (!"".equals(oldOpinionIdStr)) {// 删除原来意见,上传附件等
                Long oldOpinionId = Long.parseLong(oldOpinionIdStr);
                // attachmentManager.deleteByReference(summaryId, oldOpinionId);
                edocManager.deleteEdocOpinion(oldOpinionId);
            }

            // 全部附件
            List<Attachment> allAtts = attachmentManager.getAttachmentsFromRequest(ApplicationCategoryEnum.edoc, summaryId, null, request);

            // 标题区域意见
            List<Attachment> importAtts = new ArrayList<Attachment>();

            // 意见区域意见
            List<Attachment> optionAtts = new ArrayList<Attachment>();

            if (allAtts != null) {

                Long summaryIdLong = Long.valueOf(summaryId);
                for (Attachment att : allAtts) {
                    if (summaryIdLong.equals(att.getSubReference())) {
                        importAtts.add(att);
                    } else {
                        // 意见区附件
                        att.setSubReference(opinion.getId());
                        optionAtts.add(att);
                    }
                }
            }

            // 保存意见区附件
            if (Strings.isNotEmpty(optionAtts)) {
                attachmentManager.create(optionAtts);
            }

            // 修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if (editHelper.hasEditAtt()) {// 是否修改附件

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
                for (Attachment att : oldAtts) {
                    oldAttIds.add(att.getFileUrl());
                    oldIds.put(att.getFileUrl(), att.getId());
                }

                for (Long url : importAttIds) {
                    if (!oldAttIds.contains(url)) {
                        addAtts.add(importAttsMap.get(url));
                    }
                }
                for (Long url : oldAttIds) {
                    if (!importAttIds.contains(url)) {
                        attachmentManager.deleteById(oldIds.get(url));
                    }
                }

                String attaFlag = attachmentManager.create(addAtts);
                LOGGER.info("添加附件成功返回的attaFlag:" + attaFlag);

                //修改附件更新全文检索库
                if (AppContext.hasPlugin("index")) {
                    indexManager.update(summary.getId(), ApplicationCategoryEnum.edoc.getKey());
                }
                // 设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(processId), nodeId),
                        true, "zcdb");
            }

            // 推送消息 affairId,memberId#affairId,memberId#affairId,memberId
            String pushMessageMembers = request.getParameter("pushMessageMemberIds");
            List<Long[]> pushMsgMemberList = parsePushMessageMemberList(pushMessageMembers);

            // 如果当前操作执行了转PDF的操作。
            String isConvertPdf = request.getParameter("isConvertPdf");
            if (Strings.isNotBlank(isConvertPdf)) {
                String newPdfId = request.getParameter("newPdfIdFirst");
                edocManager.createPdfBodies(summary, newPdfId);
            }

            String process_xml = request.getParameter("process_xml");
            String readyObjectJSON = request.getParameter("readyObjectJSON");
            // 流程锁优化导致的加签减签机制修改--周龙君
            String processChangeMessage = request.getParameter("processChangeMessage");

            CtpAffair affair = affairManager.get(affairId);
            affair.setTrack(track ? 1 : 0);
            if ("2".equals(trackRange)) {
                affair.setTrack(2);
            }
            /*
             * 公文暂不要显示由**转发,有问题联系tanmf String forwardMember =
             * request.getParameter("forwardMember");
             * if(Strings.isNotBlank(forwardMember)) {
             * summary.setForwardMember(forwardMember); }
             */

            // 暂存待办提醒时间设置--end
            String messageDataListJSON = request.getParameter("process_message_data");
            edocMessagerManager.sendOperationTypeMessage(messageDataListJSON, summary, affair);

            if (null != supervisorNames && !"".equals(supervisorNames) && null != spMemberId && !"".equals(spMemberId)
                    && null != superviseDate && !"".equals(superviseDate)) {
                this.edocManager.zcdb(affair, opinion, title, spMemberId, supervisorNames, superviseDate, summary,
                        processId, user.getId() + "", process_xml, readyObjectJSON, processChangeMessage, pushMsgMemberList);
            } else {
                this.edocManager.zcdb(summary, affair, opinion, processId, user.getId() + "", process_xml,
                        readyObjectJSON, processChangeMessage, false, pushMsgMemberList);
                if ("true".equals(request.getParameter("isDeleteSupervisior"))) {
                    this.edocSuperviseManager.deleteSuperviseDetailAndSupervisors(summary);
                }
            }
            // 设置新的当前待办人
            EdocHelper.updateCurrentNodesInfo(summary, false);
            edocManager.update(summary);

            String draftOpinionId = request.getParameter("draftOpinionId");
            if (Strings.isNotBlank(draftOpinionId)) { // 修改草稿
                edocManager.deleteEdocOpinion(Long.parseLong(draftOpinionId));
            }
            if (AppContext.hasPlugin("index")) {
                indexManager.update(summaryId, ApplicationCategoryEnum.edoc.getKey());
            }
            // 暂存待办提醒时间设置--start
            String zcdbTimeStr = request.getParameter("zcdbTime");
            EdocZcdb edocZcdb = edocZcdbManager.getEdocZcdbByAffairId(affairId);
            if (zcdbTimeStr != null && !("".equals(zcdbTimeStr))) {
                Date zcdbTime = Datetimes.parse(zcdbTimeStr, Datetimes.datetimeWithoutSecondStyle);
                if (edocZcdb != null) {
                    edocZcdbManager.updateEdocZcdbByAffairId(affairId, zcdbTime);

                    // 新增定时消息
                    edocManager.deleteZcdbQuartz(affairId, zcdbTime);
                    edocManager.createZcdbQuartz(affairId, zcdbTime);
                } else {
                    edocZcdb = new EdocZcdb();
                    edocZcdb.setIdIfNew();
                    edocZcdb.setAffairId(affairId);
                    edocZcdb.setZcdbTime(new Timestamp(zcdbTime.getTime()));
                    edocZcdbManager.saveEdocZcdb(edocZcdb);
                    // 新增定时消息
                    edocManager.createZcdbQuartz(affairId, zcdbTime);
                }

            } else {// 如果没设置，也要保存一条记录，因为查询在办列表需要连接此表
                if (edocZcdb != null) {
                    edocZcdbManager.deleteEdocZcdb(edocZcdb.getId());
                    edocZcdb = new EdocZcdb();
                    edocZcdb.setIdIfNew();
                    edocZcdb.setAffairId(affairId);
                    edocZcdb.setZcdbTime(null);
                    edocZcdbManager.saveEdocZcdb(edocZcdb);
                } else {
                    edocZcdb = new EdocZcdb();
                    edocZcdb.setIdIfNew();
                    edocZcdb.setAffairId(affairId);
                    edocZcdb.setZcdbTime(null);
                    edocZcdbManager.saveEdocZcdb(edocZcdb);
                }
            }
            // //暂存待办提醒时间设置--end
            // String messageDataListJSON =
            // request.getParameter("process_message_data");
            // edocMessagerManager.sendOperationTypeMessage(messageDataListJSON,
            // summary,affair);
            StringBuffer sb = new StringBuffer();
            sb.append("parent.doEndSign_pending('" + affairId + "');");
            rendJavaScript(response, sb.toString());
        } catch (Exception e) {
            LOGGER.error("暂存待办时抛出异常：", e);
        } finally {
            if (isRelieveLock) {
                wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
                wapi.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
            }
            try {
                unLock(user.getId(), summary);
            } catch (Exception e) {
                LOGGER.error("解锁正文文单抛出异常：", e);
            }
        }
        return null;
    }

    private List<Long[]> parsePushMessageMemberList(String pushMessageMembers) {
        // 推送消息 affairId,memberId#affairId,memberId#affairId,memberId
        List<Long[]> pushList = new ArrayList<Long[]>();
        if (Strings.isNotBlank(pushMessageMembers)) {
            String[] pushs = pushMessageMembers.split("#");
            for (String s : pushs) {
                String[] s1 = s.split(",");
                pushList.add(new Long[]{Long.valueOf(s1[0]), Long.valueOf(s1[1])});
            }
        }
        return pushList;
    }

    public ModelAndView superviseList(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView mav = new ModelAndView("edoc/supervise/supervise_list_main");

        return mav;
    }

    public EdocSummaryManager getEdocSummaryManager() {
        return edocSummaryManager;
    }

    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }

    public void setEdocSuperviseManager(EdocSuperviseManager edocSuperviseManager) {
        this.edocSuperviseManager = edocSuperviseManager;
    }

    public ModelAndView showList4QuoteFrame(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/list4QuoteFrame");
        String appType = request.getParameter("appType");
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("appType", appType);
        return modelAndView;
    }

    /**
     * 列出我的待办、已办、已发，并根据是否允许转发进行权限过滤，用在协同用引用场�?
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView list4Quote(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/list4Quote");

        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");

        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.edocSend;
        String coltype = request.getParameter("appType");
        if (Strings.isNotBlank(coltype)) {
            appEnum = ApplicationCategoryEnum.valueOf(Integer.parseInt(coltype));
        }

        List<EdocSummaryModel> queryList = this.edocManager.queryByCondition4Quote(appEnum, condition, textfield,
                textfield1);
        modelAndView.addObject("csList", queryList);

        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        modelAndView.addObject("colMetadata", colMetadata);

        CtpEnumBean comImportanceMetadata = enumManagerNew.getEnum(EnumNameEnum.common_importance.name());

        modelAndView.addObject("comImportanceMetadata", comImportanceMetadata);
        modelAndView.addObject("controller", "edocController.do");
        modelAndView.addObject("appType", coltype);

        return modelAndView;
    }

    private void zcdbSupervise(HttpServletRequest request, HttpServletResponse response, EdocSummary edocSummary,
                               boolean isNew, int state, boolean sendMessage) throws BusinessException {
        String supervisorId = request.getParameter("supervisorId");
        String supervisors = request.getParameter("supervisors");
        String awakeDate = request.getParameter("awakeDate");
        if (supervisorId != null && !"".equals(supervisorId) && awakeDate != null && !"".equals(awakeDate)) {
            // boolean canModifyAwake =
            // "on".equals(request.getParameter("canModifyAwake"))?true:false;
            String superviseTitle = request.getParameter("superviseTitle");

            SuperviseMessageParam smp = new SuperviseMessageParam();
            smp.setImportantLevel(edocSummary.getImportantLevel());
            smp.setSubject(edocSummary.getSubject());
            smp.setMemberId(edocSummary.getStartUserId());
            smp.setSendMessage(state == SuperviseEnum.superviseState.waitSupervise.ordinal() ? false : true);//
            smp.setSaveDraft(state == SuperviseEnum.superviseState.waitSupervise.ordinal() ? true : false);
            if (isNew) {
                // superviseManager.save(smp,
                // superviseTitle,user.getId(),user.getName(), supervisors,
                // idList, date, SuperviseEnum.EntityType.edoc.ordinal(),
                // edocSummary.getId());
            } else {
                // superviseManager.update(smp,
                // superviseTitle,user.getId(),user.getName(), supervisors,
                // idList, date, SuperviseEnum.EntityType.edoc.ordinal(),
                // edocSummary.getId());
            }

            SuperviseSetVO sVO = new SuperviseSetVO();
            // sVO.setTemplateDateTerminal();
            sVO.setAwakeDate(awakeDate);
            // sVO.setRole(role);
            sVO.setSupervisorIds(supervisorId);
            sVO.setSupervisorNames(supervisors);
            sVO.setTitle(superviseTitle);
            sVO.setUnCancelledVisor(null);

            if (!isNew) {
                CtpSuperviseDetail d = superviseManager.getSupervise(edocSummary.getId());
                if (d != null) {
                    sVO.setDetailId(d.getId());
                }
            } else {
            }
            superviseManager.saveOrUpdateSupervise4Process(sVO, smp, edocSummary.getId(),
                    SuperviseEnum.EntityType.edoc);
        }
    }

    // 公文查询模块Controller

    public ModelAndView edocSearchMain(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView mav = new ModelAndView("edoc/docSearch/edocSearchMain");
        // ModelAndView mav = new ModelAndView("edoc/docSearch/searchWhere");
        CtpEnumBean edocTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_type.name());
        CtpEnumBean sendTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_send_type.name());
        CtpEnumBean PTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_party.name());
        CtpEnumBean ATypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_administrative.name());
        // lijl添加(屏闭注入的html脚本),GOV-2942.【公文管理-公文查询】页面按效果图修改。------------------Start
        if (edocTypeMetadata != null) {
            List<CtpEnumItem> etm = edocTypeMetadata.getItems();
            for (CtpEnumItem me : etm) {
                me.setLabel(Functions.toHTML(me.getLabel()));
            }
        }
        if (sendTypeMetadata != null) {
            List<CtpEnumItem> stm = sendTypeMetadata.getItems();
            for (CtpEnumItem meta : stm) {
                meta.setLabel(Functions.toHTML(meta.getLabel()));
            }
        }
        if (PTypeMetadata != null) {
            List<CtpEnumItem> mdi = PTypeMetadata.getItems();
            for (CtpEnumItem mi : mdi) {
                mi.setLabel(Functions.toHTML(mi.getLabel()));
            }
        }
        if (ATypeMetadata != null) {
            List<CtpEnumItem> atm = ATypeMetadata.getItems();
            for (CtpEnumItem mi1 : atm) {
                mi1.setLabel(Functions.toHTML(mi1.getLabel()));
            }
        }
        // lijl添加(屏闭注入的html脚本),GOV-2942.【公文管理-公文查询】页面按效果图修改。------------------End
        mav.addObject("PTypeMetadata", PTypeMetadata);
        mav.addObject("ATypeMetadata", ATypeMetadata);
        mav.addObject("edocTypeMetadata", edocTypeMetadata);
        mav.addObject("sendTypeMetadata", sendTypeMetadata);
        mav.addObject("isGourpBy", "false");
        return mav;
    }

    public ModelAndView edocSearchEntry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/docSearch/edocQueryIndex");
        return mav;
    }

    public ModelAndView edocSearchWhere(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/docSearch/searchWhere");
        CtpEnumBean edocTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_type.name());
        CtpEnumBean sendTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_send_type.name());
        mav.addObject("edocTypeMetadata", edocTypeMetadata);
        mav.addObject("sendTypeMetadata", sendTypeMetadata);
        return mav;
    }

    public ModelAndView listEdocSearchReult(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Locale local = LocaleContext.getLocale(request);
        String resource = "com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources";
        final String edocResource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";
        final String commonTrue = ResourceBundleUtil.getString(resource, local, "common.true");
        final String commonFalse = ResourceBundleUtil.getString(resource, local, "common.false");
        final Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        final CtpEnumBean urgentMeta = enumManagerNew.getEnum(MetadataNameEnum.edoc_urgent_level.name());
        final CtpEnumBean secretMeta = enumManagerNew.getEnum(MetadataNameEnum.edoc_secret_level.name());
        final CtpEnumBean unitLevelMeta = enumManagerNew.getEnum(MetadataNameEnum.edoc_unit_level.name());
        final CtpEnumBean keepPeriodMeta = enumManagerNew.getEnum(MetadataNameEnum.edoc_keep_period.name());
        final CtpEnumBean docTypeMeta = enumManagerNew.getEnum(MetadataNameEnum.edoc_doc_type.name());
        final CtpEnumBean sendTypeMeta = enumManagerNew.getEnum(MetadataNameEnum.edoc_send_type.name());
        ModelAndView mav = new ModelAndView("edoc/docSearch/searchResult");
        long curUserId = AppContext.getCurrentUser().getId();
        final String datePattern = ResourceBundleUtil.getString(resource, local, "common.datetime.pattern");
        // 同一流程只显示最后一条
        String deduplication = String.valueOf(request.getParameter("deduplication"));
        if ("null".equals(deduplication) || Strings.isBlank(deduplication)) {
            deduplication = "false";
        }
        mav.addObject("isGourpBy", deduplication);
        EdocSearchModel em = new EdocSearchModel();
        bind(request, em);
        em.setGourpBy(deduplication);
        final List<EdocSummaryModel> result = edocManager.queryByCondition(curUserId, em, true);
        final int edocType = em.getEdocType();
        // 自定义查询-start
        List<QueryCol> queryColList = new ArrayList<QueryCol>();

        // 选择自定义查询选项后，公文被停用
        String colId = request.getParameter("displayCol");
        String[] colIds = colId.split(",");
        List<String> searchCol = new ArrayList<String>();
        final Map<String, String> listEdocElement = getEdocSetting(request);
        Set<String> listEdocElementSet = listEdocElement.keySet();
        for (int index = 0; index < colIds.length; index++) {
            if (listEdocElementSet.contains(colIds[index])) {
                searchCol.add(colIds[index]);
            }
        }
        colId = Strings.join(searchCol, ",");
        if (colId != null && !"".equals(colId)) {
            QueryColTemplate template = new QueryColTemplate();
            queryColList = template.getQueryCol(colId, listEdocElement, new PackageColValueInter() {
                public void packageValue(String label, List<Object> values) {
                    for (EdocSummaryModel model : result) {
                        if (label.equals(EdocElementConstants.EDOC_ELEMENT_SUBJECT)) {
                            values.add(model.getSummary().getSubject());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DOC_MARK)) {
                            values.add(model.getSummary().getDocMark());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SERIAL_NO)) {
                            values.add(model.getSummary().getSerialNo());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_CREATE_PERSON)) {
                            values.add(model.getSummary().getCreatePerson());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_UNIT)) {
                            values.add(model.getSummary().getSendUnit());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_ISSUER)) {
                            values.add(model.getSummary().getIssuer());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_TO)) {
                            values.add(model.getSummary().getSendTo());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_COPY_TO)) {
                            values.add(model.getSummary().getCopyTo());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_REPORT_TO)) {
                            values.add(model.getSummary().getReportTo());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_PRINT_UNIT)) {
                            values.add(model.getSummary().getPrintUnit());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_PRINTER)) {
                            values.add(model.getSummary().getPrinter());
                        } else if (label.equals(EdocQueryColConstants.ISPIG)) {
                            if (model.getSummary().getHasArchive()) {
                                values.add(commonTrue);
                            } else {
                                values.add(commonFalse);
                            }
                        } else if (label.equals(EdocQueryColConstants.PIGE_PATH)) {
                            values.add(model.getArchiveName());
                        }
                        // else
                        // if(label.equals(EdocQueryColConstants.EDOC_START_DATE)){
                        // values.add(model.getSummary().getCreateTime() !=null
                        // ?
                        // sDateformat1.format(model.getSummary().getCreateTime())
                        // : "");
                        // }
                        else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DOC_MARK2)) {
                            values.add(model.getSummary().getDocMark2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_TO2)) {
                            values.add(model.getSummary().getSendTo2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_COPY_TO2)) {
                            values.add(model.getSummary().getCopyTo2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_REPORT_TO2)) {
                            values.add(model.getSummary().getReportTo2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_UNIT2)) {
                            values.add(model.getSummary().getSendUnit2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_DEPARTMENT)) {
                            values.add(model.getSummary().getSendDepartment());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_DEPARTMENT2)) {
                            values.add(model.getSummary().getSendDepartment2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_PHONE)) {
                            values.add(model.getSummary().getPhone());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_AUDITOR)) {
                            values.add(model.getSummary().getAuditor());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_REVIEW)) {
                            values.add(model.getSummary().getReview());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_UNDERTAKER)) {
                            values.add(model.getSummary().getUndertaker());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_UNDERTAKENOFFICE)) {
                            values.add(model.getSummary().getUndertakenoffice());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_COPIES)) {
                            values.add(model.getSummary().getCopies());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_COPIES2)) {
                            values.add(model.getSummary().getCopies2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SIGNING_DATE)) {
                            values.add(model.getSummary().getSigningDate() != null
                                    ? Datetimes.format(model.getSummary().getSigningDate(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_CREATEDATE)) {
                            if (edocType == 1) {
                                values.add("");
                            } else {
                                values.add(model.getSummary().getCreateTime() != null
                                        ? Datetimes.format(model.getSummary().getCreateTime(), datePattern) : "");
                            }
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_PACKDATE)) {
                            values.add(model.getSummary().getPackTime() != null
                                    ? Datetimes.format(model.getSummary().getPackTime(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_RECEIPT_DATE)) {
                            values.add(model.getSummary().getReceiptDate() != null
                                    ? Datetimes.format(model.getSummary().getCreateTime(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_REGISTRATION_DATE)) {
                            if (edocType == 0) {
                                values.add("");
                            } else {
                                values.add(model.getSummary().getCreateTime() != null
                                        ? Datetimes.format(model.getSummary().getCreateTime(), datePattern) : "");
                            }
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DOC_TYPE)) {
                            String docTypeLabel = docTypeMeta
                                    .getItemLabel(String.valueOf(model.getSummary().getDocType()));
                            String docTypeName = ResourceBundleUtil.getString(edocResource, local, docTypeLabel);
                            values.add(docTypeName);
                            // values.add(null!=model.getSummary().getDocType()
                            // ?
                            // ResourceBundleUtil.getString(colMetadata.get("edoc_doc_type").getResourceBundle(),colMetadata.get("edoc_doc_type").getItemLabel(model.getSummary().getDocType()))
                            // : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SEND_TYPE)) {
                            // values.add(model.getSummary().getSendType());
                            // values.add(null!=model.getSummary().getSendType()
                            // ?
                            // ResourceBundleUtil.getString(colMetadata.get("edoc_send_type").getResourceBundle(),colMetadata.get("edoc_send_type").getItemLabel(model.getSummary().getSendType()))
                            // : "");
                            String sendTypeLabel = sendTypeMeta
                                    .getItemLabel(String.valueOf(model.getSummary().getSendType()));
                            String sendTypeName = ResourceBundleUtil.getString(edocResource, local, sendTypeLabel);
                            values.add(sendTypeName);
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_SECRET_LEVEL)) {
                            // values.add(model.getSummary().getSecretLevel());
                            String secretLabel = secretMeta
                                    .getItemLabel(String.valueOf(model.getSummary().getSecretLevel()));
                            String secretName = ResourceBundleUtil.getString(edocResource, local, secretLabel);
                            values.add(secretName);
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_UNIT_LEVEL)) {// 公文级别
                            String unitLevelLabel = unitLevelMeta
                                    .getItemLabel(String.valueOf(model.getSummary().getUnitLevel()));
                            String unitLevelName = ResourceBundleUtil.getString(edocResource, local, unitLevelLabel);
                            values.add(unitLevelName);
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_URGENT_LEVEL)) {

                            String urgentLabel = urgentMeta
                                    .getItemLabel(String.valueOf(model.getSummary().getUrgentLevel()));
                            String urgentName = ResourceBundleUtil.getString(edocResource, local, urgentLabel);
                            values.add(urgentName);
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_KEEP_PERIOD)) {
                            // values.add(model.getSummary().getKeepPeriod());
                            String keepPeriodLabel = keepPeriodMeta
                                    .getItemLabel(String.valueOf(model.getSummary().getKeepPeriod()));
                            String keepPeriodName = ResourceBundleUtil.getString(edocResource, local, keepPeriodLabel);
                            values.add(keepPeriodName);
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING1)) {
                            values.add(model.getSummary().getVarchar1());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING2)) {
                            values.add(model.getSummary().getVarchar2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING3)) {
                            values.add(model.getSummary().getVarchar3());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING4)) {
                            values.add(model.getSummary().getVarchar4());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING5)) {
                            values.add(model.getSummary().getVarchar5());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING6)) {
                            values.add(model.getSummary().getVarchar6());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING7)) {
                            values.add(model.getSummary().getVarchar7());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING8)) {
                            values.add(model.getSummary().getVarchar8());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING9)) {
                            values.add(model.getSummary().getVarchar9());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING10)) {
                            values.add(model.getSummary().getVarchar10());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING11)) {
                            values.add(model.getSummary().getVarchar11());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING12)) {
                            values.add(model.getSummary().getVarchar12());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING13)) {
                            values.add(model.getSummary().getVarchar13());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING14)) {
                            values.add(model.getSummary().getVarchar14());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING15)) {
                            values.add(model.getSummary().getVarchar15());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING16)) {
                            values.add(model.getSummary().getVarchar16());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING17)) {
                            values.add(model.getSummary().getVarchar17());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING18)) {
                            values.add(model.getSummary().getVarchar18());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING19)) {
                            values.add(model.getSummary().getVarchar19());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING20)) {
                            values.add(model.getSummary().getVarchar20());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING21)) {
                            values.add(model.getSummary().getVarchar21());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING22)) {
                            values.add(model.getSummary().getVarchar22());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING23)) {
                            values.add(model.getSummary().getVarchar23());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING24)) {
                            values.add(model.getSummary().getVarchar24());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING25)) {
                            values.add(model.getSummary().getVarchar25());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING26)) {
                            values.add(model.getSummary().getVarchar26());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING27)) {
                            values.add(model.getSummary().getVarchar27());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING28)) {
                            values.add(model.getSummary().getVarchar28());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING29)) {
                            values.add(model.getSummary().getVarchar29());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_STRING30)) {
                            values.add(model.getSummary().getVarchar30());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT1)) {
                            values.add(model.getSummary().getText1());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT2)) {
                            values.add(model.getSummary().getText2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT3)) {
                            values.add(model.getSummary().getText3());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT4)) {
                            values.add(model.getSummary().getText4());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT5)) {
                            values.add(model.getSummary().getText5());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT6)) {
                            values.add(model.getSummary().getText6());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT7)) {
                            values.add(model.getSummary().getText7());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT8)) {
                            values.add(model.getSummary().getText8());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT9)) {
                            values.add(model.getSummary().getText9());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT10)) {
                            values.add(model.getSummary().getText10());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT11)) {
                            values.add(model.getSummary().getText11());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT12)) {
                            values.add(model.getSummary().getText12());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT13)) {
                            values.add(model.getSummary().getText13());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT14)) {
                            values.add(model.getSummary().getText14());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_TEXT15)) {
                            values.add(model.getSummary().getText15());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER1)) {
                            values.add(model.getSummary().getInteger1());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER2)) {
                            values.add(model.getSummary().getInteger2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER3)) {
                            values.add(model.getSummary().getInteger3());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER4)) {
                            values.add(model.getSummary().getInteger4());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER5)) {
                            values.add(model.getSummary().getInteger5());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER6)) {
                            values.add(model.getSummary().getInteger6());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER7)) {
                            values.add(model.getSummary().getInteger7());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER8)) {
                            values.add(model.getSummary().getInteger8());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER9)) {
                            values.add(model.getSummary().getInteger9());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER10)) {
                            values.add(model.getSummary().getInteger10());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER11)) {
                            values.add(model.getSummary().getInteger11());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER12)) {
                            values.add(model.getSummary().getInteger12());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER13)) {
                            values.add(model.getSummary().getInteger13());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER14)) {
                            values.add(model.getSummary().getInteger14());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER15)) {
                            values.add(model.getSummary().getInteger15());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER16)) {
                            values.add(model.getSummary().getInteger16());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER17)) {
                            values.add(model.getSummary().getInteger17());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER18)) {
                            values.add(model.getSummary().getInteger18());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER19)) {
                            values.add(model.getSummary().getInteger19());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_INTEGER20)) {
                            values.add(model.getSummary().getInteger20());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL1)) {
                            values.add(model.getSummary().getDecimal1());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL2)) {
                            values.add(model.getSummary().getDecimal2());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL3)) {
                            values.add(model.getSummary().getDecimal3());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL4)) {
                            values.add(model.getSummary().getDecimal4());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL5)) {
                            values.add(model.getSummary().getDecimal5());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL6)) {
                            values.add(model.getSummary().getDecimal6());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL7)) {
                            values.add(model.getSummary().getDecimal7());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL8)) {
                            values.add(model.getSummary().getDecimal8());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL9)) {
                            values.add(model.getSummary().getDecimal9());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL10)) {
                            values.add(model.getSummary().getDecimal10());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL11)) {
                            values.add(model.getSummary().getDecimal11());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL12)) {
                            values.add(model.getSummary().getDecimal12());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL13)) {
                            values.add(model.getSummary().getDecimal13());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL14)) {
                            values.add(model.getSummary().getDecimal14());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL15)) {
                            values.add(model.getSummary().getDecimal15());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL16)) {
                            values.add(model.getSummary().getDecimal16());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL17)) {
                            values.add(model.getSummary().getDecimal17());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL18)) {
                            values.add(model.getSummary().getDecimal18());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL19)) {
                            values.add(model.getSummary().getDecimal19());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DECIMAL20)) {
                            values.add(model.getSummary().getDecimal20());
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE1)) {
                            values.add(model.getSummary().getDate1() != null
                                    ? Datetimes.format(model.getSummary().getDate1(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE2)) {
                            values.add(model.getSummary().getDate2() != null
                                    ? Datetimes.format(model.getSummary().getDate2(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE3)) {
                            values.add(model.getSummary().getDate3() != null
                                    ? Datetimes.format(model.getSummary().getDate3(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE4)) {
                            values.add(model.getSummary().getDate4() != null
                                    ? Datetimes.format(model.getSummary().getDate4(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE5)) {
                            values.add(model.getSummary().getDate5() != null
                                    ? Datetimes.format(model.getSummary().getDate5(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE6)) {
                            values.add(model.getSummary().getDate6() != null
                                    ? Datetimes.format(model.getSummary().getDate6(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE7)) {
                            values.add(model.getSummary().getDate7() != null
                                    ? Datetimes.format(model.getSummary().getDate7(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE8)) {
                            values.add(model.getSummary().getDate8() != null
                                    ? Datetimes.format(model.getSummary().getDate8(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE9)) {
                            values.add(model.getSummary().getDate9() != null
                                    ? Datetimes.format(model.getSummary().getDate9(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE10)) {
                            values.add(model.getSummary().getDate10() != null
                                    ? Datetimes.format(model.getSummary().getDate10(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE11)) {
                            values.add(model.getSummary().getDate11() != null
                                    ? Datetimes.format(model.getSummary().getDate11(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE12)) {
                            values.add(model.getSummary().getDate12() != null
                                    ? Datetimes.format(model.getSummary().getDate12(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE13)) {
                            values.add(model.getSummary().getDate13() != null
                                    ? Datetimes.format(model.getSummary().getDate13(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE14)) {
                            values.add(model.getSummary().getDate14() != null
                                    ? Datetimes.format(model.getSummary().getDate14(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE15)) {
                            values.add(model.getSummary().getDate15() != null
                                    ? Datetimes.format(model.getSummary().getDate15(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE16)) {
                            values.add(model.getSummary().getDate16() != null
                                    ? Datetimes.format(model.getSummary().getDate16(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE17)) {
                            values.add(model.getSummary().getDate17() != null
                                    ? Datetimes.format(model.getSummary().getDate17(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE18)) {
                            values.add(model.getSummary().getDate18() != null
                                    ? Datetimes.format(model.getSummary().getDate18(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE19)) {
                            values.add(model.getSummary().getDate19() != null
                                    ? Datetimes.format(model.getSummary().getDate19(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_DATE20)) {
                            values.add(model.getSummary().getDate20() != null
                                    ? Datetimes.format(model.getSummary().getDate20(), datePattern) : "");
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST1)) {
                            String list1 = model.getSummary().getList1();
                            // EdocElementManager edocElementManager =
                            // (EdocElementManager)AppContext.getBean("edocElementManager");
                            // EdocElement listElement =
                            // edocElementManager.getByFieldName("", curUserId);
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST1,
                                            model.getSummary().getOrgAccountId(), list1, local, edocResource));

                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST2)) {
                            String list2 = model.getSummary().getList2();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST2,
                                            model.getSummary().getOrgAccountId(), list2, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST3)) {
                            String list3 = model.getSummary().getList3();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST3,
                                            model.getSummary().getOrgAccountId(), list3, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST4)) {
                            String list4 = model.getSummary().getList4();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST4,
                                            model.getSummary().getOrgAccountId(), list4, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST5)) {
                            String list5 = model.getSummary().getList5();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST5,
                                            model.getSummary().getOrgAccountId(), list5, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST6)) {
                            String list6 = model.getSummary().getList6();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST6,
                                            model.getSummary().getOrgAccountId(), list6, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST7)) {
                            String list7 = model.getSummary().getList7();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST7,
                                            model.getSummary().getOrgAccountId(), list7, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST8)) {
                            String list8 = model.getSummary().getList8();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST8,
                                            model.getSummary().getOrgAccountId(), list8, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST9)) {
                            String list9 = model.getSummary().getList9();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST9,
                                            model.getSummary().getOrgAccountId(), list9, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST10)) {
                            String list10 = model.getSummary().getList10();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST10, model.getSummary().getOrgAccountId(),
                                    list10, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST11)) {
                            String list11 = model.getSummary().getList11();
                            values.add(
                                    EdocHelper.getListElementByElementFiledName(EdocElementConstants.EDOC_ELEMENT_LIST1,
                                            model.getSummary().getOrgAccountId(), list11, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST12)) {
                            String list12 = model.getSummary().getList12();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST12, model.getSummary().getOrgAccountId(),
                                    list12, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST13)) {
                            String list13 = model.getSummary().getList13();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST13, model.getSummary().getOrgAccountId(),
                                    list13, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST14)) {
                            String list14 = model.getSummary().getList14();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST14, model.getSummary().getOrgAccountId(),
                                    list14, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST15)) {
                            String list15 = model.getSummary().getList15();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST15, model.getSummary().getOrgAccountId(),
                                    list15, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST16)) {
                            String list16 = model.getSummary().getList16();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST16, model.getSummary().getOrgAccountId(),
                                    list16, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST17)) {
                            String list17 = model.getSummary().getList17();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST17, model.getSummary().getOrgAccountId(),
                                    list17, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST18)) {
                            String list18 = model.getSummary().getList18();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST18, model.getSummary().getOrgAccountId(),
                                    list18, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST19)) {
                            String list19 = model.getSummary().getList19();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST19, model.getSummary().getOrgAccountId(),
                                    list19, local, edocResource));
                        } else if (label.equals(EdocElementConstants.EDOC_ELEMENT_LIST20)) {
                            String list20 = model.getSummary().getList20();
                            values.add(EdocHelper.getListElementByElementFiledName(
                                    EdocElementConstants.EDOC_ELEMENT_LIST20, model.getSummary().getOrgAccountId(),
                                    list20, local, edocResource));
                        }
                    }
                }
            });
            mav.addObject("queryState", 1);
        } else {
            mav.addObject("queryState", 0);
        }
        mav.addObject("colId", colId);
        mav.addObject("queryColList", queryColList);
        // 自定义查询-end

        mav.addObject("result", result);
        mav.addObject("controller", "edocController.do");
        mav.addObject("edocType", em.getEdocType());
        mav.addObject("rowCount", Pagination.getRowCount(false));
        mav.addObject("colMetadata", colMetadata);

        return mav;
    }

    /**
     * 转到发文登记簿页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    // GOV-4811.安全测试：aqc没有单位/部门公文收发员-公文管理-发文管理-分发权限，输入地址却能访问对应界面 start
    @CheckRoleAccess(roleTypes = {Role_NAME.Accountexchange, Role_NAME.Departmentexchange})
    // GOV-4811.安全测试：aqc没有单位/部门公文收发员-公文管理-发文管理-分发权限，输入地址却能访问对应界面 end
    public ModelAndView sendRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/edocSendRegister");
        Map<String, CtpEnumBean> colMetadata = enumManagerNew.getEnumsMap(ApplicationCategoryEnum.edoc);
        CtpEnumBean attitude = enumManagerNew.getEnum(EnumNameEnum.collaboration_attitude.name()); // 处理意见
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_attitude.toString(), attitude);
        CtpEnumBean deadline = enumManagerNew.getEnum(EnumNameEnum.collaboration_deadline.name()); // 处理期限
        // attitude
        colMetadata.put(EnumNameEnum.collaboration_deadline.toString(), deadline);
        modelAndView.addObject("colMetadata", colMetadata);
        return modelAndView;
    }

    /**
     * 转到收文登记簿页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView recRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // GOV-4811.安全测试：aqc没有单位/部门公文收发员-公文管理-发文管理-分发权限，输入地址却能访问对应界面 start
        String edocType = request.getParameter("edocType");
        if (!"1".equals(edocType)) {
            return refreshWorkspace();
        }
        // GOV-4811.安全测试：aqc没有单位/部门公文收发员-公文管理-发文管理-分发权限，输入地址却能访问对应界面 end
        ModelAndView modelAndView = new ModelAndView("edoc/edocSendRegister");
        return modelAndView;
    }

    // 打开发文收文登记簿字段显示设置窗口
    public ModelAndView openEdocSetting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String edocType = request.getParameter("edocType");
        Map<Integer, String> leftMap = new LinkedHashMap<Integer, String>();

        if ("0".equals(edocType)) {
            leftMap.putAll(EdocQueryColConstants.sendEdocColMap);
        } else {
            leftMap.putAll(EdocQueryColConstants.recEdocColMap);
        }

        Map<Integer, String> targetMap = new LinkedHashMap<Integer, String>();
        String displayCol = request.getParameter("displayCol");
        if (displayCol != null && !"".equals(displayCol)) {
            String[] colIds = displayCol.split(",");
            for (int i = 0; i < colIds.length; i++) {
                String label = EdocQueryColConstants.queryColMap.get(Integer.parseInt(colIds[i]));
                targetMap.put(Integer.parseInt(colIds[i]), label);
                leftMap.remove(Integer.parseInt(colIds[i]));
            }
        }

        // OA-67830 收文后需要移除 登记人、登记日期
        boolean isGov = (Boolean) SysFlag.is_gov_only.getFlag();
        if (isGov && !EdocSwitchHelper.isOpenRegister()) {// 未开起登记功能
            Set<Integer> keySet = leftMap.keySet();
            List<Integer> removeList = new ArrayList<Integer>();
            for (Integer key : keySet) {
                if (leftMap.get(key).equals(EdocQueryColConstants.REG_PERSON)
                        || leftMap.get(key).equals(EdocQueryColConstants.REG_DATE)) {
                    removeList.add(key);
                    if (removeList.size() > 1) {
                        break;
                    }
                }
            }
            for (Integer key : removeList) {
                leftMap.remove(key);
            }
        }

        ModelAndView mav = new ModelAndView("edoc/docCountSetting");
        mav.addObject("targetMap", targetMap);
        mav.addObject("leftMap", leftMap);
        return mav;
    }

    // 打开自定义查询字段显示设置窗口
    public ModelAndView openSearchEdocSetting(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Map<String, String> leftMap = getEdocSetting(request);
        String edocStringText = Strings.join(leftMap.values(), ",");
        String edocStringValue = Strings.join(leftMap.keySet(), ",");
        ModelAndView mav = new ModelAndView("edoc/docSetting");
        mav.addObject("leftMap", leftMap);
        mav.addObject("edocStringText", edocStringText);
        mav.addObject("edocStringValue", edocStringValue);
        return mav;
    }

    /**
     * 获取符合条件的自定义公文元素
     *
     * @param request
     * @return
     */
    private Map<String, String> getEdocSetting(HttpServletRequest request) {
        Map<String, String> leftMap = new LinkedHashMap<String, String>();
        Locale local = LocaleContext.getLocale(request);
        String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";

        List<EdocElement> edocUsedElementList = edocElementManager
                .getEdocElementsByStatus(com.seeyon.v3x.edoc.util.Constants.EDOC_USEED);
        if (null != edocUsedElementList && edocUsedElementList.size() > 0) {
            for (EdocElement edocElement : edocUsedElementList) {
                int edocElementType = edocElement.getType();
                String edocElementFieldName = edocElement.getFieldName();
                boolean isComment = edocElementType == EdocElementConstants.EDOC_ELEMENT_TYPE_COMMENT;
                boolean isImg = edocElementType == EdocElementConstants.EDOC_ELEMENT_TYPE_IMG;
                boolean isKeyWord = EdocElementConstants.EDOC_ELEMENT_KEYWORD.equals(edocElementFieldName);
                boolean isFileSm = EdocElementConstants.EDOC_ELEMENT_FILESM.equals(edocElementFieldName);
                boolean isFileFz = EdocElementConstants.EDOC_ELEMENT_FILEFZ.equals(edocElementFieldName);
                boolean isAttachment = EdocElementConstants.EDOC_ELEMENT_ATTACHMENTS.equals(edocElementFieldName);
                if (!isComment && !isImg && !isKeyWord && !isFileSm && !isFileFz && !isAttachment) {
                    leftMap.put(edocElement.getFieldName(),
                            ResourceBundleUtil.getString(resource, local, edocElement.getName()));

                }
            }
        }
        if (AppContext.hasPlugin("doc")) {
            leftMap.put(EdocQueryColConstants.ISPIG,
                    ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.ISPIG));
            leftMap.put(EdocQueryColConstants.PIGE_PATH,
                    ResourceBundleUtil.getString(resource, local, EdocQueryColConstants.PIGE_PATH));
        }
        return leftMap;
    }

    /**
     * 发文登记簿查询
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.Accountexchange, Role_NAME.Departmentexchange})
    public ModelAndView listSendEdocSearchReultByDocManager(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        User user = AppContext.getCurrentUser();

        // 页面来自 （stat：公文统计页签，section:首页条件连接）
        String listType = request.getParameter("listType");
        if (listType == null) {
            listType = "section";// 来自首页推送
        }

        String conditionId = request.getParameter("conditionId");
        String showColumn = null;
        String initCondition = null;
        String dataRight = null;
        if ("section".equals(listType) && Strings.isNotBlank(conditionId)) {// 带入查询条件和查询列
            Long sId = Long.parseLong(conditionId);
            EdocRegisterCondition register = edocManager.getEdocRegisterConditionById(sId);

            showColumn = register.getQueryCol();
            initCondition = register.getContentExt1();
            dataRight = register.getContentExt2();// 部门收发员推送的数据，进行数据过滤
        }

        if (Strings.isBlank(initCondition)) {// 必须保证这个是个JSON对象
            initCondition = "{}";
        }

        ModelAndView mav = new ModelAndView("edoc/docstat/edocSendRegister");

        // 推送首页的默认名称
        String defalutPushName = EdocRoleHelper.getExchangeOgrNames(user)
                + ResourceUtil.getString("edoc.send.register");// 发文登记簿
        mav.addObject("defalutPushName", defalutPushName);

        mav.addObject("listType", listType);

        mav.addObject("showColumn", showColumn);
        mav.addObject("initCondition", initCondition);
        mav.addObject("dataRight", dataRight);

        if ("stat".equals(listType)) {
            FlipInfo fi = edocManager.getSendRegisterData(new FlipInfo(), new HashMap<String, String>());
            request.setAttribute("ffsendRegisterDataTabel", fi);
        }

        return mav;
    }

    /**
     * 在更多页面里打开一个公文统计详情
     */
    public ModelAndView openEdocRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long id = Long.parseLong(request.getParameter("id"));
        String type = request.getParameter("type");
        EdocRegisterCondition register = edocManager.getEdocRegisterConditionById(id);
        String url = "";

        if ("1".equals(type)) {
            url = "edocController.do?method=listSendEdocSearchReultByDocManager&listType=section&conditionId="
                    + register.getId();
        } else {
            url = "edocController.do?method=listRecEdocSearchReultByDocManager&listType=section&conditionId="
                    + register.getId();
        }
        url += Functions.csrfSuffix();
        return super.redirectModelAndView(url);
    }

    /**
     * 在更多页面删除某一条记录
     */

    public ModelAndView delEdocRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long id = Long.parseLong(request.getParameter("id"));
        String type = request.getParameter("type");
        edocManager.delEdocRegisterCondition(id);
        return super.redirectModelAndView(
                "edocController.do?method=getEdocRegisterConditions&type=" + type + Functions.csrfSuffix());
    }

    /**
     * 二级首页发文登记簿更多页面
     */
    public ModelAndView getEdocRegisterConditions(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String subject = request.getParameter("subject");
        String type = request.getParameter("type");

        String columnsName = null;

        if ("1".equals(type)) {
            columnsName = ResourceUtil.getString("edoc.send.register");
        } else {
            columnsName = ResourceUtil.getString("edoc.rec.register");
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("subject", subject);
        paramMap.put("type", Integer.parseInt(type));
        ModelAndView mav = new ModelAndView("edoc/edocquery/edocRegisterMoreConditions");
        User user = AppContext.getCurrentUser();
        List<EdocRegisterCondition> list = edocManager.getEdocRegisterCondition(user.getLoginAccount(), paramMap, user);
        List<EdocRegisterConditionModel> allList = new ArrayList<EdocRegisterConditionModel>();
        int count = -1;
        for (int i = 0; i < list.size(); i++) {
            EdocRegisterConditionModel model = new EdocRegisterConditionModel();
            model.setRegister(list.get(i));
            allList.add(model);
            count++;
        }
        mav.addObject("list", allList);

        count = edocManager.getEdocRegisterConditionTotal(user.getLoginAccount(), Integer.parseInt(type), subject);
        mav.addObject("count", count);
        mav.addObject("columnsName", columnsName);
        mav.addObject("_type", type);
        return mav;
    }

    /**
     * 插入推送到首页的 登记簿的查询条件
     */
    public ModelAndView addEdocRegisterCondition(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String listType = request.getParameter("listType");
        User user = AppContext.getCurrentUser();

        // 显示的列
        List<Map<String, String>> columnDomainList = ParamUtil.getJsonDomainGroup("columnDomain");
        // 查询条件
        Map<String, String> queryDomainMap = ParamUtil.getJsonDomain("queryDomain");

        String title = queryDomainMap.get("pushConditionName");
        // int edocType =
        // com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND;
        String type = "1";
        if ("sendRegister".equals(listType)) {// 发文登记薄
            // edocType =
            // com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND;
            type = "1";
        } else if ("recRegister".equals(listType)) {
            // edocType = com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC;
            type = "2";
        }

        StringBuilder queryCol = new StringBuilder();

        if (Strings.isEmpty(columnDomainList)) {// 兼容处理，ParamUtil.getJsonDomainGroup只有一条数据的时候不会返回数据
            Map<String, String> column = ParamUtil.getJsonDomain("columnDomain");
            if (column.size() > 0) {// 有数据
                columnDomainList.add(column);
            }
        }

        for (Map<String, String> columnMap : columnDomainList) {
            String name = columnMap.get("name");
            if ("".equals(queryCol.toString())) {
                queryCol.append(name);
            } else {
                queryCol.append("," + name);
            }
        }

        queryDomainMap.remove("pushConditionName");// 移除推送的名字

        String condition = JSONUtil.toJSONString(queryDomainMap);

        String exchangeDepts = null;
        // 不是单位管理员需要保存推送的范围
        if (!EdocRoleHelper.isAccountExchange(user.getId())) {
            String departmentIds = EdocRoleHelper.getUserExchangeDepartmentIds();
            if (Strings.isNotBlank(departmentIds)) {
                String[] depIds = departmentIds.split("[,]");
                if (depIds.length > 0) {
                    exchangeDepts = departmentIds;
                }
            }
        }

        EdocRegisterCondition regCon = new EdocRegisterCondition();
        regCon.setIdIfNew();
        regCon.setUserId(user.getId());
        regCon.setAccountId(user.getLoginAccount());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        regCon.setCreateTime(now);
        regCon.setType(Integer.parseInt(type));
        regCon.setStarttime(null);// 这个属性废弃了
        regCon.setEndtime(null);// 这个属性废弃了
        regCon.setQueryCol(queryCol.toString());
        regCon.setTitle(title);
        regCon.setContentExt1(condition);
        regCon.setContentExt2(exchangeDepts);// 部门权限范围

        edocManager.saveEdocRegisterCondition(regCon);

        return null;
    }

    /**
     * 收文登记簿查询
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.Accountexchange, Role_NAME.Departmentexchange})
    public ModelAndView listRecEdocSearchReultByDocManager(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        User user = AppContext.getCurrentUser();

        // 页面来自 （stat：公文统计页签，section:首页条件连接）
        String listType = request.getParameter("listType");
        if (listType == null) {
            listType = "section";// 来自首页推送
        }

        ModelAndView mav = new ModelAndView("edoc/docstat/edocRecRegister");

        String conditionId = request.getParameter("conditionId");
        String showColumn = null;
        String initCondition = null;
        String dataRight = null;
        if ("section".equals(listType) && Strings.isNotBlank(conditionId)) {// 带入查询条件和查询列
            Long sId = Long.parseLong(conditionId);
            EdocRegisterCondition register = edocManager.getEdocRegisterConditionById(sId);

            showColumn = register.getQueryCol();
            initCondition = register.getContentExt1();// 必须保证这个是个JSON对象
            dataRight = register.getContentExt2();// 部门收发员推送的数据，进行数据过滤
        }

        if (Strings.isBlank(initCondition)) {
            initCondition = "{}";
        }

        String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
        mav.addObject("isG6", isG6);

        // G6登记开关
        boolean registerSwitch = EdocSwitchHelper.isOpenRegister();
        mav.addObject("registerSwitch", registerSwitch);

        // 推送首页默认名称
        String defalutPushName = EdocRoleHelper.getExchangeOgrNames(user) + ResourceUtil.getString("edoc.rec.register");// 收文登记簿
        mav.addObject("defalutPushName", defalutPushName);

        mav.addObject("listType", listType);

        mav.addObject("showColumn", showColumn);
        mav.addObject("initCondition", initCondition);
        mav.addObject("dataRight", dataRight);

        if ("stat".equals(listType)) {
            FlipInfo fi = edocManager.getRecRegisterData(new FlipInfo(), new HashMap<String, String>());
            request.setAttribute("ffrecRegisterDataTabel", fi);
        }

        return mav;
    }

    /**
     * 导出公文查询结果到excel
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView exportQueryToExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // -- 用于输出excel的标题 －－
        // -- start --
        String stat_title = "";

        String type = request.getParameter("type");
        String edocType = request.getParameter("edocType");
        // 从查询页面导出
        if ("query".equals(type)) {
            stat_title = ResourceUtil.getString("edoc.stat.query.label");// 公文查询
        }
        // GOV-4300 【公文管理】-【收文管理】-【登记】-【收文登记薄】，将查询结果导出excel时，excel的名称还是'发文登记薄'
        else if ("0".equals(edocType)) {
            stat_title = ResourceUtil.getString("edoc.send.register");// 发文登记簿
        } else if ("1".equals(edocType)) {
            stat_title = ResourceUtil.getString("edoc.rec.register");// 收文登记簿
        }

        String page = request.getParameter("page");
        String size = request.getParameter("size");
        List<EdocSummaryModel> result = null;

        String exportType = request.getParameter("exportType");
        // 将查询结果组装成页面显示要用的形式
        DataRecord dataRecord = null;
        if ("edocQuery".equals(exportType)) {
            EdocSearchModel em = new EdocSearchModel();
            // bind(request, em);
            edocType = request.getParameter("_oldEdocType");
            String subject = request.getParameter("_oldSubject");
            String keywords = request.getParameter("_oldKeywords");
            String docMark = request.getParameter("_oldDocMark");
            String serialNo = request.getParameter("_oldSerialNo");
            String docType = request.getParameter("_oldDocType");
            String sendType = request.getParameter("_oldSendType");
            String createPerson = request.getParameter("_oldCreatePerson");
            String createTimeB = request.getParameter("_oldCreateTimeB");
            String createTimeE = request.getParameter("_oldCreateTimeE");
            String sendTo = request.getParameter("_oldSendTo");
            String sendToId = request.getParameter("_oldSendToId");
            String sendUnit = request.getParameter("_oldSendUnit");
            String sendDepartment = request.getParameter("_oldSendDepartment");
            String sendDepartmentId = request.getParameter("_oldSendDepartmentId");
            String issuer = request.getParameter("_oldIssuer");
            String signingDateA = request.getParameter("_oldSigningDateA");
            // String show=request.getParameter("show");

            if (edocType != null && !"".equals(edocType))
                em.setEdocType(Integer.parseInt(edocType));
            em.setSubject(subject);
            em.setKeywords(keywords);
            em.setDocMark(docMark);
            em.setSerialNo(serialNo);
            em.setDocType(docType);
            em.setSendType(sendType);
            em.setCreatePerson(createPerson);
            if (createTimeB != null && !"".equals(createTimeB))
                em.setCreateTimeB(Datetimes.parseDate(createTimeB));
            if (createTimeE != null && !"".equals(createTimeE))
                em.setCreateTimeE(Datetimes.parseDate(createTimeE));
            em.setSendTo(sendTo);
            em.setSendToId(sendToId);
            em.setSendUnit(sendUnit);
            em.setSendDepartment(sendDepartment);
            em.setSendDepartmentId(sendDepartmentId);
            em.setIssuer(issuer);
            if (signingDateA != null && !"".equals(signingDateA)) {
                em.setSigningDateA(Datetimes.parseDate(signingDateA));
            }
            // 同一流程只显示最后一条
            em.setGourpBy(request.getParameter("deduplication"));

            FlipInfo f = new FlipInfo();
            f.setPage(Integer.valueOf(page));
            f.setSize(Integer.valueOf(size));
            f.setPagination();

            result = edocManager.queryByCondition(AppContext.getCurrentUser().getId(), em, true);
            dataRecord = EdocHelper.exportQueryToWebModel(request, this.response(result), stat_title,
                    Integer.parseInt(edocType), getEdocSetting(request));
        } else {

            EdocSearchModel em = new EdocSearchModel();
            // bind(request, em);
            String condition = request.getParameter("condition");
            // String textfield = request.getParameter("textfield");
            // String textfield1 = request.getParameter("textfield1");
            String textfield = "";
            if ("createDate".equals(condition) || "recieveDate".equals(condition) || "registerDate".equals(condition)) {
                textfield = request.getParameter(condition + "Div1");
            } else {
                textfield = request.getParameter(condition + "Div");
            }
            String textfield1 = request.getParameter(condition + "Div2");

            if (edocType != null && !"".equals(edocType)) {
                em.setEdocType(Integer.parseInt(edocType));
                if ("0".equals(edocType)) {
                    em.setSendQueryTimeType(request.getParameter("sendQueryTimeType") == null ? 0
                            : Integer.parseInt(request.getParameter("sendQueryTimeType")));
                }
            }
            String signingDateA = request.getParameter("signingDateA");
            String signingDateB = request.getParameter("signingDateB");
            if (signingDateA != null && !"".equals(signingDateA)) {
                em.setSigningDateA(Datetimes.parseDate(signingDateA));
            }
            if (signingDateB != null && !"".equals(signingDateB)) {
                em.setSigningDateB(Datetimes.parseDate(signingDateB));
            }
            String registerDateA = request.getParameter("registerDateB");
            String registerDateB = request.getParameter("registerDateE");
            if (registerDateA != null && !"".equals(registerDateA)) {
                em.setRegisterDateB(Datetimes.parseDate(registerDateA));
            }
            if (registerDateB != null && !"".equals(registerDateB)) {
                em.setRegisterDateE(Datetimes.parseDate(registerDateB));
            }

            result = edocManager.queryByDocManager(em, false, AppContext.getCurrentUser().getLoginAccount(), condition,
                    textfield, textfield1);
            dataRecord = EdocHelper.exportQueryToWebModel2(request, this.response(result), stat_title);
        }

        fileToExcelManager.save(response, stat_title, dataRecord);

        return null;
    }

    /**
     * 收发文登记薄导出数据到Excel
     *
     * @param request
     * @param response
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月17日上午1:19:25
     */
    public ModelAndView registerExport2Excel(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String listType = request.getParameter("listType");
        User user = AppContext.getCurrentUser();

        // 显示的列
        List<Map<String, String>> columnDomainList = ParamUtil.getJsonDomainGroup("columnDomain");
        // 查询条件
        Map<String, String> queryDomainMap = ParamUtil.getJsonDomain("queryDomain");

        int edocType = com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND;
        String stat_title = "";

        if ("sendRegister".equals(listType)) {// 发文登记薄
            edocType = com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_SEND;
            stat_title = ResourceUtil.getString("edoc.send.register");// 发文登记簿
        } else if ("recRegister".equals(listType)) {
            edocType = com.seeyon.v3x.edoc.util.Constants.EDOC_FORM_TYPE_REC;
            stat_title = ResourceUtil.getString("edoc.rec.register");// 收文登记簿
        }

        List<SummaryModel> summaryModels = edocManager.queryRegisterData(edocType, queryDomainMap, user);

        if (Strings.isEmpty(columnDomainList)) {// 兼容处理，ParamUtil.getJsonDomainGroup只有一条数据的时候不会返回数据
            Map<String, String> column = ParamUtil.getJsonDomain("columnDomain");
            if (column.size() > 0) {// 有数据
                columnDomainList.add(column);
            }
        }
        DataRecord dataRecord = EdocHelper.generateExeclData(columnDomainList, summaryModels, stat_title);
        fileToExcelManager.save(response, stat_title, dataRecord);

        return null;
    }

    /**
     * 进行封装的方法，封装成发送到页面的数据
     *
     * @param list
     * @return
     */
    private List<EdocSummaryModel> response(List<EdocSummaryModel> list) throws Exception {
        if (list != null && list.size() != 0) {
            CtpEnumBean secretLeveleMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());// 得到公文密级的枚举
            String secretLevel = "";
            for (EdocSummaryModel summary : list) {
                if (summary.getSummary().getSecretLevel() != null) {
                    secretLevel = this.getLabel(summary.getSummary().getSecretLevel(), secretLeveleMetadata);
                } else {
                    secretLevel = this.getLabel(null, secretLeveleMetadata);
                }
                summary.getSummary().setSecretLevel(secretLevel);
            }
        }
        return list;
    }

    private String getLabel(String itemValue, CtpEnumBean metadata) {
        CtpEnumItem itms = metadata.getItem(itemValue);

        if (itms == null)
            return null;
        String label = null;
        if (itemValue != null) {

            if (Strings.isNotBlank(metadata.getResourceBundle())) { // 在原数据中定义了resourceBundle
                label = ResourceBundleUtil.getString(metadata.getResourceBundle(), itms.getLabel());
            } else {
                label = ResourceUtil.getString(itms.getLabel());
            }

            if (label == null) {
                return itms.getLabel();
            }
        }

        return label;
    }

    /**
     * Ajax判断文号定义是否被删除，并且判断内部文号是否已经存在（除开公文自己本身占用的文号）
     *
     * @param definitionId 下拉选择传进来的是文号定义ID，断号选择传进来的是edoc_mark表的ID
     * @param serialNo     内部文号
     * @return deleted（0：已删除 1：未删除） exsit(0:不存在 1：存在)
     */
    public String checkEdocMark(Long definitionId, String serialNo, Integer selectMode, String summaryId) {
        int deleted = 1;
        int exsit = 0;
        // 手动输入的时候不判断文号定义。
        if (definitionId != null && definitionId.longValue() != 0) {
            Long id = 0L;
            if (selectMode == 2) {// 断号选择
                EdocMark edocMark = edocMarkManager.getEdocMark(definitionId);
                if (edocMark != null) {
                    EdocMarkDefinition definition = edocMark.getEdocMarkDefinition();
                    if (definition != null) {
                        id = definition.getId();
                    }
                }
            } else {
                id = definitionId;
            }
            // 判断文号定义是否已经删除
            deleted = edocMarkDefinitionManager.judgeEdocDefinitionExsit(id);
        }
        // 判断内部文号是否已经存在
        if (serialNo != null && !"".equals(serialNo)) {
            User user = AppContext.getCurrentUser();
            exsit = edocSummaryManager.checkSerialNoExsit(summaryId, serialNo, user.getLoginAccount());
        }

        return deleted + "," + exsit;
    }

    /**
     * 拟文人Ajax更新附件，记录日志，发送消息
     *
     * @return
     */
    public ModelAndView updateAttachment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String id = request.getParameter("edocSummaryId");
        String affairId = request.getParameter("affairId");
        // Long _affairId=0L;
        EdocSummary edocSummary = new EdocSummary();
        CtpAffair affair = new CtpAffair();
        String ret = "SUCCESS";
        if (id != null && !"".equals(id)) {
            edocSummary = edocSummaryManager.findById(Long.parseLong(id));
        }
        if (affairId != null && !"".equals(affairId)) {
            affair = affairManager.get(Long.parseLong(affairId));
        }

        try {
            this.edocManager.updateAttachment(edocSummary, affair, user, request);
        } catch (Exception e) {
            LOGGER.error("修改正文附件异常", e);
            ret = "";
        }
        response.getWriter().write(ret);
        return null;
    }

    /**
     * 暂存待办提醒窗口
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView openZcdbTime(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // String edocType=request.getParameter("edocType"); //公文类型，收文，发文，签报？

        ModelAndView modelAndView = new ModelAndView("edoc/zcdbTime");

        // modelAndView.addObject("edocType", edocType);
        // modelAndView.addObject("edocState", edocState);

        return modelAndView;
    }

    /**
     * 归档修改历史列表窗口
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showArchiveModifyLog(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView modelAndView = new ModelAndView("edoc/showArchiveModifyLog");

        String summaryId = request.getParameter("summaryId");
        List<EdocArchiveModifyLog> list = edocArchiveModifyLogManager.getListBySummaryId(Long.parseLong(summaryId));
        modelAndView.addObject("logList", list);

        return modelAndView;
    }

    // lijl添加,GOV-3864.公文管理-发文管理-已办-已办结-已归档-查看公文修改历史，有多页数据时，点击"下一页"或"末页"提示"被迫下线，原因：与服务器失去连接"
    public ModelAndView showArchiveModifyLog_Iframe(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ModelAndView modelAndView = new ModelAndView("edoc/showArchiveModifyLog_iframe");

        String summaryId = request.getParameter("summaryId");
        List<EdocArchiveModifyLog> list = edocArchiveModifyLogManager.getListBySummaryId(Long.parseLong(summaryId));
        modelAndView.addObject("logList", list);
        modelAndView.addObject("summaryId", summaryId);

        return modelAndView;
    }

    /**
     * 组合查询窗口
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView openCombQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String edocType = request.getParameter("edocType"); // 公文类型，收文，发文
        String edocState = request.getParameter("state"); // 公文状态，1待发，2已发，3待办，4已办

        ModelAndView modelAndView = new ModelAndView("edoc/edocquery/query");

        modelAndView.addObject("edocType", edocType);
        modelAndView.addObject("edocState", edocState);

        CtpEnumBean edocTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_doc_type.name());
        CtpEnumBean sendTypeMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_send_type.name());
        CtpEnumBean secretLevelMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_secret_level.name());
        CtpEnumBean urgentLevelMetadata = enumManagerNew.getEnum(EnumNameEnum.edoc_urgent_level.name());
        modelAndView.addObject("secretLevelMetadata", secretLevelMetadata); // 密级
        modelAndView.addObject("urgentLevelMetadata", urgentLevelMetadata); // 紧急程度
        modelAndView.addObject("edocTypeMetadata", edocTypeMetadata);// 公文种类
        modelAndView.addObject("sendTypeMetadata", sendTypeMetadata); // 行文类型
        return modelAndView;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    private String getOpinionName(String category, long accountId) throws Exception {
        List<Permission> list = permissionManager.getPermissionsByCategory(category, accountId);
        StringBuilder opinions = new StringBuilder("");
        for (Permission perm : list) {
            if (opinions.length() > 0) {
                opinions.append(",");
            }
            opinions.append(perm.getName());
        }

        return opinions.toString();
    }

    /**
     * 得到公文的所属部门的ID 在主单位下，取主部门为公文所属部门，由于系统无法识别他由主岗发文还是由副岗发文，鉴于概率低，就取主岗部门了。
     * 在兼职单位下，取多个兼职部门中的一个（按排序号，兼职序号在前的哪个部门 ）为公文所属部门。
     *
     * @param accoutId    :公文所属单位
     * @param agentUserId ： 被代理人ID， 如果没有被代理人传入null
     * @return 公文所属部门ID
     */
    private Long getEdocOwnerDepartmentId(Long accoutId, Long agentUserId) {

        Long userId = null;
        Long userAccountId = null;
        long currentDeptId = 0;
        if (agentUserId != null) {
            try {
                V3xOrgMember agentMember = orgManager.getMemberById(agentUserId);

                userId = agentMember.getId();
                userAccountId = agentMember.getOrgAccountId();
                currentDeptId = agentMember.getOrgDepartmentId();

            } catch (BusinessException e) {
                // 这个异常不做处理
            }
        }

        if (userId == null) {

            User user = AppContext.getCurrentUser();
            userId = user.getId();
            userAccountId = user.getAccountId();
            currentDeptId = user.getDepartmentId();
        }

        if (!Strings.equals(accoutId, userAccountId)) {

            try {
                Map<Long, List<MemberPost>> map = orgManager.getConcurentPostsByMemberId(accoutId, userId);

                long min = -1;

                for (Long deptId : map.keySet()) {
                    List<MemberPost> list = map.get(deptId);
                    for (MemberPost concurrentPost : list) {
                        if (min == -1)
                            min = concurrentPost.getSortId();
                        if (concurrentPost.getSortId() <= min) {
                            min = concurrentPost.getSortId();
                            currentDeptId = deptId;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("公文所属部门判断异常:", e);
            }
        }
        return currentDeptId;
    }

    /**
     * 解锁，公文提交或者暂存待办的时候进行解锁,与Ajax解锁一起，构成两次解锁，避免解锁失败，节点无法修改的问题出现
     *
     * @param userId
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

    /**
     * lijl添加2011-10-17 分发点击列表数据,显示详细信息
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView edocRegisterInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ModelAndView modelAndView = new ModelAndView("edoc/edocRegister");
            long registerId = request.getParameter("registerId") == null ? -1L
                    : Long.parseLong(request.getParameter("registerId"));
            EdocRegister edocRegister = edocRegisterManager.getEdocRegister(registerId);
            if (edocRegister == null) {
                LOGGER.error("edocRegister is null,registerId:" + registerId);
                return modelAndView;
            }
            RegisterBody registerBody = edocRegister.getRegisterBody();
            modelAndView.addObject("edocType", edocRegister.getEdocType());
            modelAndView.addObject("controller", "edocController.do");
            // 是否允许拟文人修改附件。
            boolean allowUpdateAttachment = EdocSwitchHelper.allowUpdateAttachment();
            modelAndView.addObject("allowUpdateAttachment", allowUpdateAttachment);
            modelAndView.addObject("firstPDFId", registerBody != null ? registerBody.getContent() : "");
            // 只查找正文的附件。
            modelAndView.addObject("attachments", attachmentManager.getByReference(registerId, registerId));
            modelAndView.addObject("bean", edocRegister);
            modelAndView.addObject("registerBody", registerBody);
            modelAndView.addObject("curUser", AppContext.getCurrentUser());
            return modelAndView;
        } catch (Exception e) {
            LOGGER.error("点击分发详细信息抛出异常：", e);
            StringBuffer sb = new StringBuffer();
            // out.println("alert(\"" +
            // StringEscapeUtils.escapeJavaScript(e.getMessage()) + "\")");
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"true\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.getA8Top().reFlesh();");
            sb.append("}");
            sb.append("");
            rendJavaScript(response, sb.toString());
            return null;
        }
    }

    public ProcessLogManager getProcessLogManager() {
        return processLogManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public EdocMarkDefinitionManager getEdocMarkDefinitionManager() {
        return edocMarkDefinitionManager;
    }

    public void setEdocMarkDefinitionManager(EdocMarkDefinitionManager edocMarkDefinitionManager) {
        this.edocMarkDefinitionManager = edocMarkDefinitionManager;
    }

    public FileToExcelManager getFileToExcelManager() {
        return fileToExcelManager;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    /**
     * 进入加签页面（综合原加签、当前会签、多级会签的加签）
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView preInsertPeople(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView mav = null;

        if ((Boolean) BrowserFlag.PageBreak.getFlag(request)) {
            mav = new ModelAndView("edoc/insertPeople");
        } else {// ipad
            mav = new ModelAndView("collaboration/insertPeopleIpad");
        }
        User user = AppContext.getCurrentUser();

        String _summaryId = request.getParameter("summaryId");
        String _affairId = request.getParameter("affairId");
        Long affairId = Long.parseLong(_affairId);
        Long summaryId = Long.parseLong(_summaryId);
        String appName = request.getParameter("appName"); // 相当于edoctype，0发文，1收文，2签报。但是协同=collaboration
        String appTypeName = request.getParameter("appTypeName"); // 多级会签用，判断当前应用是发文、收文、签报，是字符串，从而取到该应用的节点权限。
        String isForm = request.getParameter("isForm");
        String processId = request.getParameter("processId");

        StringBuffer sb = new StringBuffer();
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null || affair.getState() != StateEnum.col_pending.key()) {
            String msg = EdocHelper.getErrorMsgByAffair(affair);
            sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(msg) + "\")");
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.getA8Top().reFlesh();");
            sb.append("}");
            rendJavaScript(response, sb.toString());
            return null;
        }

        List<String> desList = null;
        String nodeMetadataName = "";
        // int flowPermType = 1; //流程权限的类型 0:协同 . 1:公文（默认是公文）
        String bundleName = "com.seeyon.v3x.edoc.resources.i18n.EdocResource"; // 指定国际化资源文件,默认为公文
        String defaultPolicyId = null; // 节点权限值
        if ("0".equalsIgnoreCase(appName)) {
            nodeMetadataName = EnumNameEnum.edoc_send_permission_policy.name(); // 发文节点权限策略
            defaultPolicyId = BPMSeeyonPolicy.EDOC_POLICY_SHENPI.getId();
        } else if ("1".equalsIgnoreCase(appName)) {
            nodeMetadataName = EnumNameEnum.edoc_rec_permission_policy.name();
            defaultPolicyId = BPMSeeyonPolicy.EDOC_POLICY_YUEDU.getId();
        } else if ("2".equalsIgnoreCase(appName)) {
            nodeMetadataName = EnumNameEnum.edoc_qianbao_permission_policy.name();
            defaultPolicyId = BPMSeeyonPolicy.EDOC_POLICY_SHENPI.getId();
        }

        // 获取对应应用类型的所有节点策略
        List<Permission> nodePolicyList = null;
        // 权限策略改动，需要充分验证(Mazc 2009-4-13)
        // 自定义的流程取发起者所在单位（协同的关联单位）的节点权限， 模板的取模板所在单位的节点权限
        Long flowPermAccountId = user.getLoginAccount();
        // ColSummary summary = colManager.getColSummaryById(summaryId, false);
        // if("collaboration".equalsIgnoreCase(appName) && summary != null){
        // flowPermAccountId = ColHelper.getFlowPermAccountId(flowPermAccountId,
        // summary, templeteManager);
        // }
        // else {//公文，包括发文，收文，签报
        EdocSummary edocSummary = edocManager.getEdocSummaryById(summaryId, false);
        if (edocSummary != null) {
            if (edocSummary.getTempleteId() != null) {
                CtpTemplate templete = templeteManager.getCtpTemplate(edocSummary.getTempleteId());
                if (templete != null) {
                    flowPermAccountId = templete.getOrgAccountId();
                }
            } else {
                if (edocSummary.getOrgAccountId() != null) {
                    flowPermAccountId = edocSummary.getOrgAccountId();
                }
            }
        }
        // }
        // TODO-FIXED--修改时减少了参数，留下修改痕迹以便查阅
        // nodePolicyList =
        // flowPermManager.getFlowpermsByStatus(nodeMetadataName,
        // FlowPerm.Node_isActive, false, flowPermType, flowPermAccountId);
        nodePolicyList = permissionManager.getPermissionsByStatus(nodeMetadataName, Permission.Node_isActive,
                flowPermAccountId);
        mav.addObject("nodePolicyList", nodePolicyList);
        if (!"true".equals(isForm)) {
            for (Permission flowPerm : nodePolicyList) {
                if ("formaudit".equals(flowPerm.getName())) {
                    nodePolicyList.remove(flowPerm);
                    break;
                }
            }
        }
        // 获取对应应用下的所有节点策略说明
        desList = new ArrayList<String>();
        for (Permission fp : nodePolicyList) {
            desList.add(fp.getName() + "split" + StringEscapeUtils.escapeJavaScript(fp.getDescription()));
        }

        mav.addObject("bundleName", bundleName);
        mav.addObject("nodePolicyList", nodePolicyList);
        mav.addObject("desList", desList);
        mav.addObject("summaryId", summaryId);
        mav.addObject("affairId", affairId);
        mav.addObject("defaultPolicyId", defaultPolicyId);
        mav.addObject("appName", appName);
        mav.addObject("appTypeName", appTypeName);
        mav.addObject("processId", processId);
        // TODOmav.addObject("isFormReadonly", affair.isFormReadonly());

        // 当前会签流程
        Permission flowPerm = null;
        EdocSummary summary2 = edocManager.getEdocSummaryById(summaryId, true);
        EnumNameEnum edocTypeEnum = EdocUtil.getEdocMetadataNameEnum(summary2.getEdocType());
        Long senderId = summary2.getStartUserId();
        V3xOrgMember sender = orgManager.getMemberById(senderId);
        Long flowPermAccountId2 = EdocHelper.getFlowPermAccountId(summary2, sender.getOrgAccountId());
        flowPerm = permissionManager.getPermission(edocTypeEnum.name(), "huiqian", flowPermAccountId2);

        mav.addObject("nodePolicy", flowPerm);
        mav.addObject("from", "edoc");

        return mav;

    }

    // 加签
    public ModelAndView insertPeople(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // String processMode = request.getParameter("processMode");
        String _summaryId = request.getParameter("summaryId");
        String _affairId = request.getParameter("affairId");
        Long summaryId = Long.parseLong(_summaryId);
        Long affairId = Long.parseLong(_affairId);
        // String policyId = request.getParameter("policyId");
        // String processId = request.getParameter("processId");
        // String policyName = request.getParameter("policyName");
        // String appName = request.getParameter("appName");
        // boolean isFormOperationReadonly =
        // "1".equals(request.getParameter("formOperationPolicy"));

        // 权限策略改动，需要充分验证(Mazc 2009-4-13)
        // 自定义的流程取发起者所在单位（协同的关联单位）的节点权限， 模板的取模板所在单位的节点权限

        EdocSummary edocSummary = null;
        // String fCategoryName = EnumNameEnum.col_flow_perm_policy.name();
        // Long templeteId = 0L;
        // Long summaryAccountId = 0L;
        // Long caseId = null;
        // String summaryProcessId = ""; // 这个变量是否可以去掉，直接取前台传过来的PROCESSid

        edocSummary = edocManager.getEdocSummaryById(summaryId, false);
        if (edocSummary == null)
            return null;
        // templeteId = edocSummary.getTempleteId();
        // summaryAccountId = edocSummary.getOrgAccountId();
        // fCategoryName =
        // EdocHelper.getCategoryName(edocSummary.getEdocType());
        // caseId = edocSummary.getCaseId();
        // summaryProcessId = edocSummary.getProcessId();

        // boolean isRelieveLock = true;
        try {
            StringBuffer sb = new StringBuffer();
            CtpAffair affair = affairManager.get(affairId);
            if (affair.getState() != StateEnum.col_pending.key()) {
                String msg = EdocHelper.getErrorMsgByAffair(affair);
                sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(msg) + "\")");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
                sb.append("  window.close();");
                sb.append("}else{");
                sb.append("  parent.getA8Top().reFlesh();");
                sb.append("}");
                rendJavaScript(response, sb.toString());
                return null;
            }

            String caseLogXML = null;
            String caseProcessXML = null;
            String caseWorkItemLogXML = null;

            caseProcessXML = Strings.escapeJavascript(caseProcessXML);
            caseLogXML = Strings.escapeJavascript(caseLogXML);
            caseWorkItemLogXML = Strings.escapeJavascript(caseWorkItemLogXML);
            return null;
        } catch (Exception e) {
            LOGGER.error("公文加签抛出异常：", e);
        } finally {
            /*
             * //if(isRelieveLock) ColLock.getInstance().removeLock(summaryId);
             */
        }
        return null;
    }

    /**
     * puyc 关联收文 通过edocId和edocType查找，如果在EdocSummary有记录，则返回收文单
     * 如果没有记录，则在EdocRegister 查找，如果有记录，则返回登记单
     * 如果没有记录，则在EdocRecieveRecord查找，如果有记录，则返回签收单 如果没有记录，则提示，资源不存在
     */
    public String relationReceive(String relationEdocIdStr, String edocTypeStr) {
        Long relationEdocId = Strings.isNotBlank(relationEdocIdStr) ? Long.parseLong(relationEdocIdStr) : null;
        int relationEdocType = Strings.isNotBlank(edocTypeStr) ? Integer.parseInt(edocTypeStr) : -1;
        String relationUrl = "";// 收文的路径
        if (relationEdocId != null && relationEdocType != -1) {
            EdocSummary edocSummary = this.edocSummaryRelationManager.findEdocSummary(relationEdocId, relationEdocType);
            if (edocSummary != null) {// 收文单
                relationUrl = "edocController.do?method=detailIFrame&summaryId=" + relationEdocId;
            } else {
                EdocRegister edocRegister = this.edocSummaryRelationManager.findEdocRegister(relationEdocId,
                        relationEdocType);
                if (edocRegister != null) {// 登记单
                    relationUrl = "edocController.do?method=edocRegisterDetail&registerId=" + edocRegister.getId();
                } else {
                    EdocRecieveRecord edocRecieveRecord = this.edocSummaryRelationManager
                            .findEdocRecieveRecord(relationEdocId);
                    if (edocRecieveRecord != null) {// 签收单
                        relationUrl = "exchangeEdoc.do?method=edit&modelType=received&from=tobook&id="
                                + edocRecieveRecord.getId();
                    }
                }
            }

        }
        return relationUrl;
    }

    public ModelAndView relationNewEdocFrame(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/relationNewEdocFrame");
        return modelAndView;
    }

    /*
     * 关联发文
     */
    public ModelAndView relationNewEdoc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/relationNewEdoc");
        String recEdocIdStr = request.getParameter("recEdocId");
        String recTypeStr = request.getParameter("recType");
        String forwardType = request.getParameter("forwardType");
        List<EdocSummary> newEdocList = new ArrayList<EdocSummary>();
        User user = AppContext.getCurrentUser();
        Long createUserId = user.getId();
        if (Strings.isNotBlank(recEdocIdStr) && Strings.isNotBlank(recTypeStr)) {
            if (Strings.isBlank(forwardType)) {
                newEdocList = this.edocSummaryRelationManager.findNewEdoc(Long.parseLong(recEdocIdStr), createUserId,
                        Integer.parseInt(recTypeStr));
            } else if ("registered".equals(forwardType)) {
                List<EdocSummary> newEdocList1 = this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(
                        Long.parseLong(recEdocIdStr), createUserId, Integer.parseInt(recTypeStr), 1);
                if (newEdocList1 != null) {
                    newEdocList.addAll(newEdocList1);
                }

                EdocRegister edocRegister = edocRegisterManager.getEdocRegister(Long.parseLong(recEdocIdStr));

                long recieveId = edocRegister.getRecieveId();
                if (recieveId != -1) {
                    List<EdocSummary> newEdocList2 = this.edocSummaryRelationManager.findNewEdoc(recieveId,
                            user.getId(), 1);
                    if (newEdocList2 != null) {
                        newEdocList.addAll(newEdocList2);
                    }
                }

            } else if ("waitSent".equals(forwardType) || "listSent".equals(forwardType)) {
                // newEdocList =
                // this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(
                // Long.parseLong(recEdocIdStr), createUserId,
                // Integer.parseInt(recTypeStr),2);

                // 待分发

                List<EdocSummary> newEdocList1 = null;
                EdocRegister edocRegister = edocRegisterManager
                        .findRegisterByDistributeEdocId(Long.parseLong(recEdocIdStr));
                if (edocRegister != null) {
                    newEdocList1 = this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(
                            edocRegister.getId(), createUserId, Integer.parseInt(recTypeStr), 2);
                }
                if (newEdocList1 != null) {
                    newEdocList.addAll(newEdocList1);
                }

                List<EdocSummary> newEdocList2 = this.edocSummaryRelationManager.findNewEdocByRegisteredOrWaitSent(
                        Long.parseLong(recEdocIdStr), createUserId, Integer.parseInt(recTypeStr), 2);
                if (newEdocList2 != null) {
                    newEdocList.addAll(newEdocList2);
                }
            }
        }
        // OA-36095
        // wangchw登记了纸质公文，在待办中转发文--收文关联新发文，收文处理节点查看有关联链接，处理时回退该流程，发起人在待发中查看有此链接直接发送后已发待办中也有，但是若在待发中编辑没有此链接，发送后也没此链接
        if (Strings.isEmpty(newEdocList)) {
            EdocSummary summary = edocManager.getEdocSummaryById(Long.parseLong(recEdocIdStr), false);
            if (summary.getEdocType() == 1) {
                newEdocList = this.edocSummaryRelationManager.findAllNewEdoc(Long.parseLong(recEdocIdStr), createUserId,
                        Integer.parseInt(recTypeStr));
            }
        }

        // DAO中并没有分页查询，而是全部查询的，最后调用pagenate方法进行分页的
        modelAndView.addObject("newEdocList", pagenate(newEdocList));
        return modelAndView;
    }

    /**
     * 当前用户设置标题多行显示功能（换行） --xiangfan
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView subjectWrapSetting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // PrintWriter out = response.getWriter();
        int listType = Strings.isBlank(request.getParameter("listType")) ? -1
                : Integer.parseInt(request.getParameter("listType"));
        int edocType = Strings.isBlank(request.getParameter("edocType")) ? -1
                : Integer.parseInt(request.getParameter("edocType"));
        User user = AppContext.getCurrentUser();
        // 标示 true:设置为多行显示，false:取消多行显示
        boolean flag = Strings.isBlank(request.getParameter("flag")) ? false
                : Boolean.parseBoolean(request.getParameter("flag"));
        if (flag) {
            EdocSubjectWrapRecord subjectWrapRecord = new EdocSubjectWrapRecord();
            subjectWrapRecord.setNewId();
            subjectWrapRecord.setAccountId(user.getAccountId());
            subjectWrapRecord.setEdocType(edocType);
            subjectWrapRecord.setListType(listType);
            subjectWrapRecord.setUserId(user.getId());
            this.edocSummaryManager.subjectWrapSetting(subjectWrapRecord);
        } else {
            this.edocSummaryManager.subjectWrapDisabled(user.getAccountId(), user.getId(), listType, edocType);
        }
        // String json = "[{flag:\""+ flag +"\"}]";
        // out.println(json);
        // out.close();
        return null;
    }

    public ModelAndView test(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/test11");
        return modelAndView;
    }

    public ModelAndView superviseWindowForEdocZCDB(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView mav = new ModelAndView("edoc/superviseWindow");

        mav.addObject("title", "edoc.supervise.label");

        mav.addObject("supervisorId", request.getParameter("supervisorId"));
        mav.addObject("supervisors", request.getParameter("supervisors"));
        mav.addObject("superviseTitle", request.getParameter("superviseTitle"));
        mav.addObject("awakeDate", request.getParameter("awakeDate"));
        mav.addObject("canModify", request.getParameter("canModify"));
        mav.addObject("unCancelledVisor", request.getParameter("unCancelledVisor"));
        mav.addObject("sVisorsFromTemplate", request.getParameter("sVisorsFromTemplate"));
        mav.addObject("temformParentId", request.getParameter("temformParentId"));
        return mav;
    }

    public ModelAndView setPolicy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/setEdocPolicy");
        String id = request.getParameter("id");
        mav.addObject("id", id);

        PermissionVO permission = permissionManager.getPermission(Long.parseLong(id));
        List<EdocElementFlowPermAcl> acl_list = edocElementFlowPermAclManager
                .getEdocElementFlowPermAcls(Long.valueOf(id));

        List<EdocElement> elementList = edocElementManager.getEdocElementsByStatus(1, 1, 10000);
        boolean initiate = false;
        if (null != acl_list && acl_list.size() > 0) {
            initiate = true;
        }

        /**
         * 判断是否为新添加的元素
         */
        List<EdocElementFlowPermAcl> final_list = new ArrayList<EdocElementFlowPermAcl>();

        for (EdocElement ele : elementList) {
            // 处理意见,logo图片不出现在设置权限中
            if (ele.getType() >= 6) {
                continue;
            }

            EdocElementFlowPermAcl acl = new EdocElementFlowPermAcl();
            acl.setIdIfNew();
            if (initiate && !"zhihui".equals(permission.getLabel())) {
                for (EdocElementFlowPermAcl fAcl : acl_list) {
                    try {// 由于公文元素生成多套，删除后倒志错误；
                        if (fAcl.getEdocElement().getFieldName().equals(ele.getFieldName())) {
                            acl.setAccess(fAcl.getAccess());
                            break;
                        } else {
                            acl.setAccess(0);
                        }
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
            } else {
                acl.setAccess(0);
            }
            acl.setEdocElement(ele);
            final_list.add(acl);
        }
        // OA-31479 选择一个节点权限修改：公文元素设置，弹出的页面----节点名称存在国际化问题
        // f1将name和label交换了
        mav.addObject("policyName", permission.getLabel());
        // OA-16474 后台公文节点权限，节点权限选择知会，进入公文元素设置页签，查看页面，发现"编辑"列没有置灰
        mav.addObject("policyLabel", permission.getName());
        mav.addObject("aclList", final_list);

        return mav;
    }

    public ModelAndView changePolicy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        List<EdocElement> elementList = edocElementManager.getEdocElementsByStatus(1, 1, 10000);
        List<EdocElementFlowPermAcl> tempList = new ArrayList<EdocElementFlowPermAcl>();
        for (EdocElement ele : elementList) {
            if (ele.getType() == EdocElement.C_iElementType_LogoImg
                    || ele.getType() == EdocElement.C_iElementType_Comment) {
                continue;
            }

            EdocElementFlowPermAcl acl = new EdocElementFlowPermAcl();
            acl.setIdIfNew();
            String access = request.getParameter(String.valueOf(ele.getId()));
            if (null != access && !"".equals(access)) {
                acl.setAccess(Integer.valueOf(access));
            } else {
                acl.setAccess(1);
            }
            acl.setEdocElement(ele);
            acl.setFlowPermId(Long.parseLong(id));
            tempList.add(acl);
        }
        edocElementFlowPermAclManager.deleteEdocElementFlowPermAcl(Long.parseLong(id));// 首先删除该权限下的所有元素,然后重新添加.
        edocElementFlowPermAclManager.saveEdocElementFlowPermAcls(tempList);

        StringBuffer sb = new StringBuffer();
        // out.println("alert('设置成功!')");
        if (AppContext.getCurrentUser().isAdministrator()) {
            sb.append("window.parent.main.edocDialog.close();");
        } else {
            sb.append("window.parent.mainIframe.sysComanyMainIframe.detailIframe.edocDialog.close();");
        }
        rendJavaScript(response, sb.toString());
        return null;
        // return new
        // ModelAndView("edoc/refreshWindow").addObject("windowObj","parent");
    }

    /**
     * 打开设置跟踪窗口 wangwei
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView preChangeTrack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("edoc/preChangeTrack");
        String affairId = request.getParameter("affairId");
        String _isTrack = request.getParameter("trackValue");
        StringBuilder ids = new StringBuilder();
        boolean isTrack = false;
        boolean isWorkFlowFinished = false;

        if (Strings.isNotBlank(affairId)) {

            List<CtpTrackMember> tracks = trackManager.getTrackMembers(Long.valueOf(affairId));

            if (tracks != null) {
                for (CtpTrackMember colTrackMember : tracks) {
                    if (ids.length() > 0) {
                        ids.append(",");
                    }
                    ids.append(colTrackMember.getTrackMemberId());
                }
            }

            CtpAffair affair = affairManager.get(Long.valueOf(affairId));
            modelAndView.addObject("trackStatus", affair.getTrack());
            EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(), false);
            if (summary.getFinished()) {
                isWorkFlowFinished = true;
            }

            isTrack = affair.getTrack() == 0 ? false : true;
        }
        if ("0".equals(_isTrack)) {
            isTrack = false;
        }
        modelAndView.addObject("isWorkFlowFinished", isWorkFlowFinished);
        modelAndView.addObject("isTrack", isTrack);
        modelAndView.addObject("trackIds", ids);

        return modelAndView;
    }

    /**
     * 保存修改跟踪事件 wangwei
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView changeTrack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long affairId = Long.parseLong(request.getParameter("affairId"));
        Integer trackMode = Integer.parseInt((request.getParameter("trackMode")));
        String trackRange = request.getParameter("trackRange");
        boolean track = true;
        if (trackMode == 0) {
            track = false;
        }
        String trackMembers = request.getParameter("trackMembers");
        CtpAffair affair = affairManager.get(affairId);
        /**
         * trackMembers不等于空的时候，证明选择了指定人，同时trackRange不等于全部的时候，trackMode=2,选择了指定人
         */
        if (Strings.isNotBlank(trackMembers) && !"1".equals(trackRange)) {
            trackMode = 2;
        } else {
            trackMembers = ""; // 如果选择全部，就要清空上一次的所选人员
        }
        affair.setTrack(trackMode);
        affairManager.updateAffair(affair);
        edocManager.setTrack(affairId, track, trackMembers);

        StringBuffer sb = new StringBuffer();
        sb.append("if(parent._closeWin) {"); // 弹出
        sb.append("   parent._closeWin();");
        sb.append("}");
        rendJavaScript(response, sb.toString());
        return null;
    }

    /**
     * 常用语列表 wangwei
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listPhrase(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        ModelAndView mv = new ModelAndView("common/phrase/list");
        List<CommonPhrase> phrases = phraseManager.getAllPhrases();
        mv.addObject("phrases", phrases);
        return mv;
    }

    private final Object CheckAndupdateLock = new Object();

    public ModelAndView appointStepBack(HttpServletRequest request, HttpServletResponse response)
            throws BusinessException {
        // boolean isRelieveLock = true;
        User user = AppContext.getCurrentUser();
        Long summaryId = Strings.isBlank(request.getParameter("summaryId")) ? -1
                : Long.parseLong(request.getParameter("summaryId"));
        String content = Strings.isBlank(request.getParameter("contentOP")) ? "" : request.getParameter("contentOP");
        Long currentAffairId = Strings.isBlank(request.getParameter("affairId")) ? -1
                : Long.parseLong(request.getParameter("affairId"));
        String theStepBackNodeId = request.getParameter("theStepBackNodeId");
        String appName = request.getParameter("appName");
        String policy = request.getParameter("policy");
        String nodeName = request.getParameter("nodeName");
        String submitStyle = request.getParameter("submitStyle");
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);

        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentWorkitemId(Long.parseLong(request.getParameter("workitemId")));
        context.setCaseId(Long.parseLong(request.getParameter("caseId")));
        context.setProcessId(request.getParameter("processId"));
        context.setCurrentUserId(String.valueOf(user.getId()));
        context.setCurrentUserName(user.getName());
        context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
        context.setCurrentActivityId(request.getParameter("activityId"));
        context.setBusinessData("EDOC_CONTENT_OP", content);
        context.setAppName(ApplicationCategoryEnum.edoc.name());
        context.setSelectTargetNodeId(request.getParameter("theStepBackNodeId"));// 被退回节点
        context.setSubmitStyleAfterStepBack(submitStyle);// 退回方式 1直接提交给我 2流程重走
        context.setAppObject(summary);

        Integer attitude = Strings.isBlank(request.getParameter("attitude"))
                ? com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL
                : Integer.parseInt(request.getParameter("attitude"));
        long nodeId = Strings.isBlank(request.getParameter("currentNodeId")) ? -1
                : Long.parseLong(request.getParameter("currentNodeId"));
        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setIdIfNew();
        signOpinion.setAttribute(attitude);
        signOpinion.setContent(content);
        signOpinion.isDeleteImmediate = false;// "delete".equals(afterSign);
        signOpinion.affairIsTrack = false;// "track".equals(afterSign);
        signOpinion.setNodeId(nodeId);
        signOpinion.setPolicy(policy);
        signOpinion.setAffairId(currentAffairId);
        signOpinion.setIsHidden(request.getParameterValues("isHidden") != null);
        signOpinion.setOpinionType(EdocOpinion.OpinionType.backOpinion.ordinal());
        signOpinion.setEdocSummary(summary);

        Map<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("summaryId", summaryId);
        tempMap.put("appName", appName);
        tempMap.put("submitStyle", submitStyle);
        tempMap.put("currentAffairId", currentAffairId);
        tempMap.put("selectTargetNodeId", theStepBackNodeId);
        tempMap.put("policy", policy);
        tempMap.put("nodeName", nodeName);
        tempMap.put("content", content);
        tempMap.put("context", context);
        tempMap.put("summary", summary);
        tempMap.put("signOpinion", signOpinion);
        tempMap.put("oldOpinion", edocManager.findBySummaryIdAndAffairId(summaryId, currentAffairId));

        if ("start".equals(theStepBackNodeId)) {
            if ("0".equals(submitStyle)) {
                try {// 删除归档
                    List<Long> ids = new ArrayList<Long>();
                    ids.add(summary.getId());
                    if (AppContext.hasPlugin("doc")) {
                        docApi.deleteDocResources(user.getId(), ids);
                    }
                    summary.setHasArchive(false);
                    //summary.setArchiveId(null);
                    summary.setState(CollaborationEnum.flowState.cancel.ordinal());
                    edocManager.update(summary);
                    // 指定回退到发起者-流程重走撤销统计数据
                    edocStatManager.deleteEdocStat(summary.getId());
                    affairManager.updateAffairSummaryState(summary.getId(), summary.getState());
                    // 流程撤销
                    if (summary.getEdocType() == 0 || summary.getEdocType() == 2) {// 发文/签报撤销
                        edocMarkManager.edocMarkCategoryRollBack(summary);
                    }
                } catch (Exception e) {
                    LOGGER.error("指定回退公文流程，删除归档文档:" + e);
                }
            }
        }
        try {
            edocManager.appointStepBack(tempMap);
        } finally {
            synchronized (CheckAndupdateLock) {
                // 解除流程锁
                wapi.releaseWorkFlowProcessLock(String.valueOf(summary.getProcessId()),
                        String.valueOf(AppContext.currentUserId()));
                wapi.releaseWorkFlowProcessLock(String.valueOf(summary.getId()),
                        String.valueOf(AppContext.currentUserId()));
            }
            try {
                // 解锁正文文单
                unLock(user.getId(), summary);
            } catch (Exception e) {
                LOGGER.error("解锁正文文单抛出异常：", e);
            }
        }
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("parent.doEndSign_pending('" + currentAffairId + "');");
            rendJavaScript(response, sb.toString());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

    public ModelAndView showPushWindow(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/pushMessageList");
        String summaryId = request.getParameter("summaryId");
        String edocType = request.getParameter("edocType");
        // 被回复意见的AffairId.
        // String replyedAffairId = request.getParameter("replyedAffairId");
        // ApplicationCategoryEnum app= ApplicationCategoryEnum.collaboration;
        if (Strings.isNotBlank(edocType) && ("0".equals(edocType) || "1".equals(edocType) || "2".equals(edocType))) {
            // app =
            // EdocUtil.getAppCategoryByEdocType(Integer.parseInt(edocType));
        }

        List<StateEnum> stateList = new ArrayList<StateEnum>();
        stateList.add(StateEnum.col_sent);
        stateList.add(StateEnum.col_done);
        // OA-44645 处理公文时，点击消息推送，消息推送选择框中未显示暂存待办的人员
        stateList.add(StateEnum.col_pending);
        stateList.add(StateEnum.col_waitSend);
        // 过滤掉自己和重复项
        List<Long> memberIdList = new ArrayList<Long>();
        // 显示已发，已办，暂存待办 三种事项
        List<CtpAffair> pushMessageListAffair = new ArrayList<CtpAffair>();
        List<CtpAffair> pushMessageList = affairManager.getAffairs(Long.valueOf(summaryId), stateList);
        for (CtpAffair r : pushMessageList) {
            if (!affairManager.isAffairValid(r, false)) {
                continue;// 过滤已删除事项
            }
            if (r.getState() == StateEnum.col_sent.key() && r.getSubState() == null) {
                r.setSubState(0);
            }
            // 只显示已发、暂存待办和、已办的、回退者
            if (r.getState() == StateEnum.col_pending.key() || r.getState() == StateEnum.col_done.key()
                    || r.getState() == StateEnum.col_sent.key()
                    || r.getSubState() == SubStateEnum.col_pending_specialBack.key()
                    || r.getSubState() == SubStateEnum.col_pending_specialBacked.key()
                    || r.getSubState() == SubStateEnum.col_pending_specialBackCenter.key()) {
                if (!r.getMemberId().equals(AppContext.currentUserId()) && !memberIdList.contains(r.getMemberId())) {
                    memberIdList.add(r.getMemberId());
                    pushMessageListAffair.add(r);
                }
            }
        }
        mav.addObject("affairs", pushMessageListAffair);

        String selected = request.getParameter("sel");
        List<Long> l = new ArrayList<Long>();
        if (Strings.isNotBlank(selected)) {
            String[] s = selected.split("[#]");
            for (String s1 : s) {
                l.add(Long.valueOf(s1.split("[,]")[0]));
            }
        }

        mav.addObject("sels", l);
        return mav;
    }

    // 根据拟文时选择的流程期限具体时间，换算成分钟数
    public Long getMinValueByDeadlineTime(String deadlineTime, Date createTime) {

        Date _createTime = null;
        if (createTime != null) {
            _createTime = new Date(createTime.getTime());
        }
        Long minValue = -1L;
        try {
            if (Strings.isNotBlank(deadlineTime) && _createTime != null) {
                Date deadline = Datetimes.parse(deadlineTime, null, Datetimes.datetimeWithoutSecondStyle);
                String sdate = Datetimes.formatDatetimeWithoutSecond(createTime);
                _createTime = Datetimes.parse(sdate, null, Datetimes.datetimeWithoutSecondStyle);
                long minusDay = deadline.getTime() - _createTime.getTime();
                if (minusDay > 0) {
                    minValue = (long) Math.rint(minusDay / (1000 * 60));
                }
            }
        } catch (Exception e) {
            LOGGER.error("公文拟文转换流程期限具体时间格式化错误。", e);
        } finally {
        }
        return minValue;
    }

    public ModelAndView edocShowNodeExplain(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String affairId = request.getParameter("affairId");
        String templeteId = request.getParameter("templeteId");
        String processId = request.getParameter("processId");
        String msg = edocManager.getDealExplain(affairId, templeteId, processId);
        ModelAndView mv = new ModelAndView("edoc/edocShowNodeExplain");
        mv.addObject("msg", msg);
        return mv;
    }

    public ModelAndView edocContent(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String summaryId = request.getParameter("summaryId");
        EdocSummary summary = this.edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
        ModelAndView mav = new ModelAndView("edoc/edocContent");
        mav.addObject("summary", summary);
        mav.addObject("htmlISignCount", iSignatureHtmlManager.getISignCount(summary.getId()));
        return mav;
    }

    public ModelAndView testOffice(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/testOffice");
        return mav;
    }

    /**
     * 显示意见的电子签名信息
     * <p>
     * 注意 ： NeedlessCheckLogin 设置了这个接口不受登录校验， 所以下面的接口需要做安全校验
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @Author : xuqiangwei
     * @Date : 2014年11月7日下午4:43:44
     */
    @SetContentType
    @NeedlessCheckLogin
    public ModelAndView showInscribeSignetPic(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        User user = AppContext.getCurrentUser();
        boolean accessDenied = false;
        if (user == null) {
            // 如果没有登录,只对来自微信的放行.
            if (UserHelper.isFromMicroCollaboration(request)) {
                accessDenied = false;
            } else {
                accessDenied = true;
            }
        }

        // 没有登录，直接屏蔽
        if (accessDenied) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Long id = Long.valueOf(request.getParameter("id"));
        V3xHtmDocumentSignature signet = htmSignetManager.getById(id);
        OutputStream out = null;
        try {
            if (signet != null) {
                String body = signet.getFieldValue();
                byte[] b = EdocHelper.hex2byte(body);

                // response.setContentType("application/octet-stream;
                // charset=UTF-8");
                response.setContentType("image/jpeg");
                /*
                 * response.setHeader("Content-disposition",
                 * "attachment;filename=\"file.jpg\"");
                 */

                out = response.getOutputStream();
                out.write(b);
            }
        } catch (Exception e) {
            LOGGER.error("显示电子签名异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }

        return null;
    }

    /**
     * 显示prompt页面，可做公共页面
     *
     * @param request
     * @param response
     * @return
     * @Author : xuqiangwei
     * @Date : 2014年12月20日下午11:18:30
     */
    public ModelAndView showPromptPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("edoc/docstat/prompt");
        return mav;
    }

    /**
     * 设置公文的默认发文单位/发文部门(用逗号隔开)
     *
     * @param summary
     * @param user
     * @param setType 0发文单位 1发文部门
     * @return
     */
    private EdocSummary setEdocDefaultSendInfo(EdocSummary summary, User user, String setType) {
        try {
            if (0 == summary.getEdocType() || 2 == summary.getEdocType()) {
                if (setType.contains("0")) {
                    if (Strings.isBlank(summary.getSendUnit())) {
                        long accountId = user.getLoginAccount();
                        summary.setSendUnitId("Account|" + accountId);
                        summary.setSendUnit(orgManager.getAccountById(accountId).getName());
                    }
                }
                if (setType.contains("1")) {
                    if (Strings.isBlank(summary.getSendDepartment())) {
                        V3xOrgDepartment dept = orgManager.getCurrentDepartment();
                        if (dept != null) {
                            summary.setSendDepartmentId("Department|" + String.valueOf(user.getDepartmentId()));
                            summary.setSendDepartment(dept.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("设置EdocSummary发文单位与发文部门出错", e);
        }
        return summary;
    }

    /**
     * 处理公文，选择了不同意
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView disagreeDeal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/disagreeDeal");
        return mav;
    }

    /**
     * 根据summaryId查询附件列表信息
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView findAttachmentListBySummaryId(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ModelAndView mv = new ModelAndView("apps/collaboration/attachmentList");
        String summaryId = request.getParameter("summaryId");
        String memberId = request.getParameter("memberId");
        String parameter = request.getParameter("attmentList");
        List<AttachmentVO> attachmentVOs = edocManager.getAttachmentListBySummaryId(Long.valueOf(summaryId), parameter);
        //控制是否存在操作一列
        boolean canLook = false;
        for (AttachmentVO attachmentVO : attachmentVOs) {
            if (attachmentVO.isCanLook()) {
                canLook = true;
                break;
            }
            if ("jpg".equals(attachmentVO.getFileType()) || "gif".equals(attachmentVO.getFileType()) || "jpeg".equals(attachmentVO.getFileType()) || "png".equals(attachmentVO.getFileType()) || "bmp".equals(attachmentVO.getFileType())
                    || "pdf".equals(attachmentVO.getFileType())) {
                canLook = true;
                break;
            }
        }
        mv.addObject("canLook", canLook);
        mv.addObject("attachmentVOs", attachmentVOs);
        mv.addObject("attSize", attachmentVOs.size());
        return mv;
    }

    /**
     * 查看属性设置
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getAttributeSettingInfo(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // 定义跳转目标路径
        ModelAndView mav = new ModelAndView("apps/collaboration/showAttributeSetting");
        String affairId = request.getParameter("affairId");
        String isHistoryFlag = request.getParameter("isHistoryFlag");
        Map args = new HashMap();
        args.put("affairId", affairId);
        args.put("isHistoryFlag", isHistoryFlag);
        Map map = edocManager.getAttributeSettingInfo(args);
        request.setAttribute("ffattribute", map);
        // 归档全路径
        mav.addObject("archiveAllName", map.get("archiveAllName"));
        // 督办属性设置 标志位
        mav.addObject("supervise", map.get("supervise"));
        mav.addObject("openFrom", request.getParameter("openFrom"));
        return mav;
    }

    /**
     * 公文移交
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView colTransfer(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String sSummaryId = request.getParameter("summary_id");
        String processId = request.getParameter("processId");
        String _affairId = request.getParameter("affairId");
        String sTransferMemberId = request.getParameter("transferMemberId");
        String content = request.getParameter("contentOP");
        Boolean isHidden = request.getParameterValues("isHidden") != null;
        String attitude = request.getParameter("attitude");
        User user = AppContext.getCurrentUser();
        boolean isRelieveLock = true;
        try {
            long affairId = Long.valueOf(_affairId).longValue();
            CtpAffair _affair = affairManager.get(affairId);
            long summaryId = Long.valueOf(sSummaryId).longValue();
            Long transferMemberId = Long.valueOf(sTransferMemberId);
            StringBuffer sb = new StringBuffer();
            String errMsg = "";
            if (_affair.getState() != StateEnum.col_pending.key()) {
                errMsg = EdocHelper.getErrorMsgByAffair(_affair);
            }
            if (Strings.isNotBlank(errMsg)) {
                sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(errMsg) + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
                sb.append("  window.close();");
                sb.append("}else{");
                sb.append("  parent.parent.location.reload(true);");
                sb.append("}");
                rendJavaScript(response, sb.toString());
                return null;
            }
            // 推送消息 affairId,memberId#affairId,memberId#affairId,memberId
            String pushMessageMembers = request.getParameter("pushMessageMemberIds");
            List<Long[]> pushMsgMemberList = parsePushMessageMemberList(pushMessageMembers);

            String result = edocManager.transferEdoc(user, affairId, summaryId, transferMemberId, content, isHidden, attitude, request, pushMsgMemberList);
            if (Strings.isNotBlank(result)) {
                sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(result) + "\");");
                sb.append("if(window.dialogArguments){"); // 弹出
                sb.append("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
                sb.append("  window.close();");
                sb.append("}else{");
                sb.append("  parent.parent.location.reload(true);");
                sb.append("}");
                rendJavaScript(response, sb.toString());
                return null;
            }
            sb.append("parent.doEndSign_pending('" + affairId + "');");
            rendJavaScript(response, sb.toString());
            return null;
        } catch (Exception ex) {
            LOGGER.error("公文移交出错：", ex);
            StringBuffer sb = new StringBuffer();
            sb.append("alert(\"" + StringEscapeUtils.escapeJavaScript(ex.getMessage()) + "\");");
            sb.append("if(window.dialogArguments){"); // 弹出
            sb.append("  window.returnValue = \"" + DATA_NO_EXISTS + "\";");
            sb.append("  window.close();");
            sb.append("}else{");
            sb.append("  parent.parent.location.reload(true);");
            sb.append("}");
            rendJavaScript(response, sb.toString());
            return null;
        } finally {
            if (isRelieveLock) {//解锁
                this.wapi.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
                this.wapi.releaseWorkFlowProcessLock(sSummaryId, String.valueOf(AppContext.currentUserId()));
            }

        }
    }

    private String replaceRN(String str) {
        return str.replaceAll("\r\n|\r|\n|\n\r", "");
    }
}
