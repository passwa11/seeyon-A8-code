package com.seeyon.apps.collaboration.controller;

import com.seeyon.apps.ai.event.AIRemindEvent;
import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.api.NewCollDataHandlerInterface;
import com.seeyon.apps.collaboration.bo.ColInfo;
import com.seeyon.apps.collaboration.bo.FormLockParam;
import com.seeyon.apps.collaboration.bo.LockObject;
import com.seeyon.apps.collaboration.constants.ColConstant;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.enums.ColQueryCondition;
import com.seeyon.apps.collaboration.event.CollaborationAffairPrintEvent;
import com.seeyon.apps.collaboration.manager.ColLockManager;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.NewCollDataHelper;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.quartz.CollProcessBackgroundManager;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.collaboration.vo.ColSummaryVO;
import com.seeyon.apps.collaboration.vo.NewCollTranVO;
import com.seeyon.apps.collaboration.vo.NodePolicyVO;
import com.seeyon.apps.collaboration.vo.SeeyonPolicy;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.constants.DocConstants.PigeonholeType;
import com.seeyon.apps.edoc.api.EdocApi;
import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.apps.ext.kypending.util.ReadConfigTools;
import com.seeyon.apps.taskmanage.util.MenuPurviewUtil;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.PerformanceConfig;
import com.seeyon.ctp.common.config.SystemConfig;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.ContentConfig;
import com.seeyon.ctp.common.content.ContentUtil;
import com.seeyon.ctp.common.content.ContentViewRet;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.mainbody.CtpContentAllBean;
import com.seeyon.ctp.common.content.mainbody.MainbodyManager;
import com.seeyon.ctp.common.content.mainbody.MainbodyType;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.datarelation.api.DataRelationApi;
import com.seeyon.ctp.common.dump.vo.DumpDataVO;
import com.seeyon.ctp.common.excel.DataCell;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.htmltopdf.manager.HtmlToPdfManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionLayoutManager;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.shareMap.V3xShareMap;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.template.enums.TemplateEnum;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.util.TemplateUtil;
import com.seeyon.ctp.common.trace.api.TraceWorkflowManager;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.handover.api.HandoverManager;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgIndexManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.webmodel.WebEntity4QuickIndex;
import com.seeyon.ctp.plugins.resources.CollaborationPluginUtils;
import com.seeyon.ctp.plugins.resources.PluginResourceLocation;
import com.seeyon.ctp.plugins.resources.PluginResourceScope;
import com.seeyon.ctp.report.engine.api.ReportConstants.UserConModel;
import com.seeyon.ctp.report.engine.api.manager.ReportResultApi;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.HttpSessionUtil;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.ReqUtil;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.XMLCoder;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.BPMSeeyonPolicySetting;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.security.AccessControlBean;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.peoplerelate.manager.PeopleRelateManager;
import net.joinwork.bpm.definition.BPMProcess;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CollaborationController extends BaseController {
    private static final Log LOG = CtpLogFactory.getLog(CollaborationController.class);
    private ColManager colManager;
    private AffairManager affairManger;
    private WorkflowApiManager wapi;
    private CustomizeManager customizeManager;
    private TemplateManager templateManager;
    private OrgManager orgManager;
    private AttachmentManager attachmentManager;
    private FileToExcelManager fileToExcelManager;
    private FileManager fileManager;
    private EdocApi edocApi;
    private EnumManager enumManagerNew;
    private MainbodyManager ctpMainbodyManager;
    private CollaborationApi collaborationApi;
    private ColLockManager colLockManager;
    private PermissionManager permissionManager;
    private OrgIndexManager orgIndexManager;
    private PeopleRelateManager peopleRelateManager;
    private ProcessLogManager processLogManager;
    private SystemConfig systemConfig;
    private TraceWorkflowManager traceWorkflowManager;
    private DocApi docApi;
    private CAPFormManager capFormManager;
    private CollProcessBackgroundManager collProcessBackgroundManager;
    private PermissionLayoutManager permissionLayoutManager;
    private FormApi4Cap3 formApi4Cap3;
    private FormApi4Cap4 formApi4Cap4;
    private HtmlToPdfManager htmlToPdfManager;
    private MenuPurviewUtil menuPurviewUtil;
    private HandoverManager handoverManager;

    private ReportResultApi reportResultApi;
    private DataRelationApi dataRelationApi;

    public MenuPurviewUtil getMenuPurviewUtil() {
        return menuPurviewUtil;
    }

    public void setMenuPurviewUtil(MenuPurviewUtil menuPurviewUtil) {
        this.menuPurviewUtil = menuPurviewUtil;
    }

    public void setHtmlToPdfManager(HtmlToPdfManager htmlToPdfManager) {
        this.htmlToPdfManager = htmlToPdfManager;
    }

    public FormApi4Cap4 getFormApi4Cap4() {
        return formApi4Cap4;
    }

    public void setFormApi4Cap4(FormApi4Cap4 formApi4Cap4) {
        this.formApi4Cap4 = formApi4Cap4;
    }

    public FormApi4Cap3 getFormApi4Cap3() {
        return formApi4Cap3;
    }

    public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

    public CollProcessBackgroundManager getCollProcessBackgroundManager() {
        return collProcessBackgroundManager;
    }

    public void setCollProcessBackgroundManager(CollProcessBackgroundManager collProcessBackgroundManager) {
        this.collProcessBackgroundManager = collProcessBackgroundManager;
    }

    public void setPermissionLayoutManager(PermissionLayoutManager permissionLayoutManager) {
        this.permissionLayoutManager = permissionLayoutManager;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public DocApi getDocApi() {
        return docApi;
    }

    public void setDocApi(DocApi docApi) {
        this.docApi = docApi;
    }

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }


    public OrgIndexManager getOrgIndexManager() {
        return orgIndexManager;
    }

    public void setOrgIndexManager(OrgIndexManager orgIndexManager) {
        this.orgIndexManager = orgIndexManager;
    }

    public PeopleRelateManager getPeopleRelateManager() {
        return peopleRelateManager;
    }

    public void setPeopleRelateManager(PeopleRelateManager peopleRelateManager) {
        this.peopleRelateManager = peopleRelateManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public ColLockManager getColLockManager() {
        return colLockManager;
    }

    public void setColLockManager(ColLockManager colLockManager) {
        this.colLockManager = colLockManager;
    }

    public ColManager getColManager() {
        return colManager;
    }

    public CollaborationApi getCollaborationApi() {
        return collaborationApi;
    }

    public void setCollaborationApi(CollaborationApi collaborationApi) {
        this.collaborationApi = collaborationApi;
    }

    public void setEnumManagerNew(EnumManager enumManager) {
        this.enumManagerNew = enumManager;
    }

    public AffairManager getAffairManger() {
        return affairManger;
    }

    public void setAffairManger(AffairManager affairManger) {
        this.affairManger = affairManger;
    }

    public EdocApi getEdocApi() {
        return edocApi;
    }

    public void setEdocApi(EdocApi edocApi) {
        this.edocApi = edocApi;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public TraceWorkflowManager getTraceWorkflowManager() {
        return traceWorkflowManager;
    }

    public void setTraceWorkflowManager(TraceWorkflowManager traceWorkflowManager) {
        this.traceWorkflowManager = traceWorkflowManager;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }


    public CustomizeManager getCustomizeManager() {
        return customizeManager;
    }

    public void setCustomizeManager(CustomizeManager customizeManager) {
        this.customizeManager = customizeManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    private CtpTrackMemberManager trackManager;

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    private AffairManager affairManager;

    public CtpTrackMemberManager getTrackManager() {
        return trackManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setHandoverManager(HandoverManager handoverManager) {
        this.handoverManager = handoverManager;
    }

    public void setReportResultApi(ReportResultApi reportResultApi) {
        this.reportResultApi = reportResultApi;
    }

    public void setDataRelationApi(DataRelationApi dataRelationApi) {
        this.dataRelationApi = dataRelationApi;
    }
/*	@Deprecated
    public ModelAndView aiTest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String affairId = request.getParameter("affairId");
        LOG.info(AppContext.currentUserName()+"AI----------------,affairId:" + affairId);
        if (Strings.isDigits(affairId)) {
            collProcessBackgroundManager.transFinishWorkItem(Long.valueOf(affairId), BackgroundDealType.AI);
        }
        ModelAndView mv =null;
        return mv;

    }*/

    /**
     * 新建协同页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @author libing
     */
    public ModelAndView newColl(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("text/html;charset=UTF-8");
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/newCollaboration");
        modelAndView.addObject("summarySate", -1);//默认值newCollaboration.jsp
        User user = AppContext.getCurrentUser();

        NewCollTranVO vobj = new NewCollTranVO();
        vobj.setCreateDate(new Date());
        String from = request.getParameter("from");
        String summaryId = request.getParameter("summaryId");
        String templateId = request.getParameter("templateId");
        String affairId = request.getParameter("affairId");


        // 获取tplId(templateId)进行权限判定，不能直接覆盖templateId,会对后面的分支参数影响
        CtpAffair affair = null;
        String tplId = templateId;
        if (Strings.isNotBlank(affairId) && Strings.isBlank(templateId) && "waitSend".equals(from)) {
            affair = affairManager.get(Long.valueOf(affairId));
            if (affair != null) {
                tplId = affair.getTempleteId() == null ? null : affair.getTempleteId().toString();
            }
        }
        if (!hasNewCollAuth(user, tplId, from)) {
            ColUtil.webAlertAndClose(response, ResourceUtil.getString("collaboration.listWaitSend.noNewCol"));
            return null;
        }

        boolean isNewCollFlag = false;
        modelAndView.addObject("isNewCollFlag", isNewCollFlag);
        String projectID = request.getParameter("projectId");
        boolean relateProjectFlag = false;
        if (Strings.isNotBlank(projectID) && !"-1".equals(projectID)) {
            relateProjectFlag = true;
        }

        ColSummary summary = null;
        boolean canEditColPigeonhole = true;
        CtpTemplate template = null;

        vobj.setFrom(from);
        vobj.setSummaryId(Strings.isBlank(summaryId) ? String.valueOf(UUIDLong.longUUID()) : summaryId);
        vobj.setTempleteId(templateId);
        vobj.setProjectId(Strings.isNotBlank(projectID) ? Long.parseLong(projectID) : null);
        vobj.setAffairId(affairId);
        vobj.setUser(user);
        vobj.setCanDeleteOriginalAtts(true);
        vobj.setCloneOriginalAtts(false);
        vobj.setArchiveName("");
        vobj.setNewBusiness("1");

        int i = 0;

        boolean showTraceWorkflows = false;  //是否显示流程追溯面板
        //调用模板
        String branch = "";
        if (Strings.isNotBlank(templateId)) {
            branch = "template";
            vobj.setSummaryId(String.valueOf(UUIDLong.longUUID()));
            try {
                template = templateManager.getCtpTemplate(Long.valueOf(templateId));
                boolean isEnable = templateManager.isTemplateEnabled(template, user.getId());
                if (!user.hasResourceCode("F01_newColl") && isEnable) {
                    if (null != template && !TemplateUtil.isSystemTemplate(template)) {
                        isEnable = false;
                    }
                }
                if (!isEnable) {
                    if ("templateNewColl".equals(from)) {// 新建页面打开
                        newCollAlert(response, StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("collaboration.send.fromSend.templeteDelete")));//模板已经被删除，或者您已经没有该模板的使用权限
                    } else {// 首页栏目打开
                        PrintWriter out = response.getWriter();
                        out.println("<script>");
                        out.println("alert('" + Strings.escapeJavascript(ResourceUtil.getString("collaboration.send.fromSend.templeteDelete")) + "');");//模板已经被删除，或者您已经没有该模板的使用权限
                        out.print("parent.window.close();");
                        out.println("</script>");
                        out.flush();
                    }

                    return null;
                }

                vobj.setTemplate(template);
                vobj = colManager.transferTemplate(vobj);

                // colManager.transferTemplate方法里面会进行一次转化，如果是个人模板的话，会将template转化为对应的父模板，所以这个地方需要重新设置一下。
                template = vobj.getTemplate();

                modelAndView.addObject("zwContentType", template.getBodyType());

                AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.collaboration, vobj.getSummaryId(), user.getId());
            } catch (Throwable e) {
                LOG.info("", e);
                // 给出提示模板已经被删除，或者您已经没有该模板的使用权限
                newCollAlert(response, StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("collaboration.send.fromSend.templeteDelete")));//模板已经被删除，或者您已经没有该模板的使用权限
                return null;
            }

            canEditColPigeonhole = vobj.isCanEditColPigeonhole();
        } else if ("resend".equals(from)) {//已发列表的重复发起
            branch = "resend";
            vobj = colManager.transResend(vobj);

            vobj.setSummaryId(String.valueOf(UUIDLong.longUUID()));
            modelAndView.addObject("parentSummaryId", vobj.getSummary().getId());
            Long parentSummaryId = vobj.getSummary().getId();
            ColSummary parentSummary = colManager.getSummaryById(parentSummaryId);
            vobj.getSummary().setId(Long.valueOf(vobj.getSummaryId()));
            Long parentTemplateId = parentSummary.getTempleteId();
            CtpTemplate ctpTemplate = null;
            if (parentTemplateId != null) {
                ctpTemplate = (CtpTemplate) templateManager.getCtpTemplate(parentTemplateId);
                if (ctpTemplate != null) {
                    ColSummary tSummary = XMLCoder.decoder(ctpTemplate.getSummary(), ColSummary.class);
                    vobj.getSummary().setDeadlineTemplate(tSummary.getDeadlineTemplate());
                    vobj.setTempleteHasDeadline(tSummary.getDeadline() != null && tSummary.getDeadline() != 0);
                    vobj.setTempleteHasRemind(tSummary.getAdvanceRemind() != null && (tSummary.getAdvanceRemind() != 0 && tSummary.getAdvanceRemind() != -1));
                    vobj.setCanEditColPigeonhole(!(tSummary.getArchiveId() == null));
                    vobj.setTemplateHasProcessTermType(tSummary.getProcessTermType() != null);
                    vobj.setTemplateHasRemindInterval(tSummary.getRemindInterval() != null && tSummary.getRemindInterval() > 0);

                    vobj.setParentWrokFlowTemplete(colManager.isParentWrokFlowTemplete(ctpTemplate.getFormParentid()));
                    vobj.setParentTextTemplete(colManager.isParentTextTemplete(ctpTemplate.getFormParentid()));
                    vobj.setParentColTemplete(colManager.isParentColTemplete(ctpTemplate.getFormParentid()));
                    vobj.setFromSystemTemplete(ctpTemplate.isSystem());//最原始的是不是系统模板


                    String scanCodeInput = "0";
                	/*if (template != null){
                    	if(null != template.getScanCodeInput() && template.getScanCodeInput()){
                    		scanCodeInput = "1";
                    	}
                	}*/

                    //附件归档
                    if (tSummary.getAttachmentArchiveId() != null && AppContext.hasPlugin("doc")) {
                        boolean docResourceExist = docApi.isDocResourceExisted(tSummary.getAttachmentArchiveId());
                        if (docResourceExist) {
                            //vobj.setArchiveId(summary.getArchiveId());
                            vobj.setAttachmentArchiveId(tSummary.getAttachmentArchiveId());
                            if (null != vobj.getSummary()) {
                                vobj.getSummary().setCanArchive(true);
                            }
                        }
                    }

                    modelAndView.addObject("scanCodeInput", scanCodeInput);
                }
            }
            getTrackInfo(modelAndView, vobj, vobj.getSummaryId());

            // 重置一些数据
            ColSummary resentSummary = vobj.getSummary();
            resentSummary.setReplyCounts(0);
            resentSummary.setCurrentNodesInfo("");
            // 如果是null,某些国产化环境${vobj.summary.resentTime + 1}执行出来是0，做一下处理
            if (resentSummary.getResentTime() == null) {
                resentSummary.setResentTime(0);
            }

            modelAndView.addObject("isResend", "1");
        } else if (vobj.getSummaryId() != null && "waitSend".equals(from)) {//来自待发
            branch = "waitSend";
            modelAndView.addObject("summarySate", StateEnum.col_waitSend.key());//待发
            vobj.setNewBusiness("0");

            // 是否展示流程追溯显示面板逻辑
            if (null != affair && !affair.getMemberId().equals(user.getId())) {
                newCollAlert(response, StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("collaboration.error.common.permission.no")));//您无权查看该主题!
                return null;
            }

            try {
                vobj = colManager.transComeFromWaitSend(vobj);

                // 指定回退回退到发起者且处理后直接提交回退者的情况
                String subState = ReqUtil.getString(request, "subState", "");
                String oldSubState = ReqUtil.getString(request, "oldSubState", "");
                if (Strings.isNotBlank(oldSubState) && "1".equals(oldSubState)) {
                    subState = oldSubState;
                }
                CtpAffair vaffair = vobj.getAffair();
                if (Strings.isBlank(subState)) {
                    subState = null != vaffair ? String.valueOf(vaffair.getSubState().intValue()) : "1";
                }
                modelAndView.addObject("subState", subState);
                //获取是否展示流程追溯面板标志
                showTraceWorkflows = showTraceWorkflows(subState, vaffair);

                template = vobj.getTemplate();

                getTrackInfo(modelAndView, vobj, vobj.getSummaryId());
            } catch (Exception e) {
                LOG.error("异常了：", e);
                newCollAlert(response, StringEscapeUtils.escapeJavaScript(ResourceUtil.getString("collaboration.send.fromSend.templeteDelete")));//模板已经被删除，或者您已经没有该模板的使用权限
                return null;
            }

            canEditColPigeonhole = vobj.isCanEditColPigeonhole();
            modelAndView.addObject("alertSuperviseSet", true);
            String formTitleText = request.getParameter("formTitleText");
            if (Strings.isNotBlank(formTitleText)) {
                //String stiitle = URLDecoder.decode(formTitleText,"UTF-8");
                modelAndView.addObject("_formTitleText", formTitleText);
                vobj.setCollSubject(formTitleText);
                if (null != vobj.getSummary()) {
                    vobj.getSummary().setSubject(formTitleText);
                }
            }
            if ("bizconfig".equals(request.getParameter("reqFrom"))) {
                vobj.setFrom("bizconfig");
            }

            vobj.setAttachmentArchiveId(vobj.getSummary().getAttachmentArchiveId());
            //ContentUtil.contentViewForDetail_col(ModuleType.collaboration,Long.valueOf(vobj.getSummaryId()),Long.valueOf(affairId),1,"0",false);
        } else if ("relatePeople".equals(from)) {
            branch = "relatePeople";
            vobj.setNewBusiness("1");
        } else if ("a8genius".equals(from)) {//精灵
            branch = "a8genius";
            //这里为了发送协同时删除原有文件，设置一下关联实体id
            Long referenceId = Long.valueOf(UUIDLong.longUUID());

            //设置附件
            String[] attachids = request.getParameterValues("attachid");
            //Office软件点击“转发协同”功能带过来的参数
            if (attachids != null && attachids.length > 0) {
                Long[] attId = new Long[attachids.length];
                for (int count = 0; count < attachids.length; count++) {
                    attId[count] = Long.valueOf(attachids[count]);
                }
                if (attId.length > 0) {
                    attachmentManager.create(attId, ApplicationCategoryEnum.collaboration, referenceId, referenceId);
                    String attListJSON = attachmentManager.getAttListJSON(referenceId);
                    vobj.setAttListJSON(attListJSON);
                }
            }

            modelAndView.addObject("source", request.getParameter("source"));
            modelAndView.addObject("from", from);
            modelAndView.addObject("referenceId", referenceId);
        }

        //是否显示流程追溯
        modelAndView.addObject("showTraceWorkflows", showTraceWorkflows);

        //后面公用的代码开始
        if (vobj.getSummary() == null) {
            summary = new ColSummary();
            vobj.setSummary(summary);
            summary.setCanForward(true);
            summary.setCanArchive(true);
            summary.setCanDueReminder(true);
            summary.setCanEditAttachment(true);
            summary.setCanModify(true);
            summary.setCanTrack(true);
            summary.setCanEdit(true);
            summary.setAdvanceRemind(-1l);
        }

        // 流程自动填充人员
        String memberId = request.getParameter("memberId");
        setWorkFlowMember(memberId, user, modelAndView);

        //转协同数据处理
        initNewCollTranVO(vobj, summary, modelAndView, user, request);

        boolean isSpecialSteped = vobj.getAffair() != null && vobj.getAffair().getSubState() == SubStateEnum.col_pending_specialBacked.key();
        BPMProcess process = null;
        if (template != null && template.getWorkflowId() != null && !isSpecialSteped && !"resend".equals(from)) {
            process = wapi.getTemplateProcess(template.getWorkflowId());
        } else {
            process = wapi.getBPMProcess(vobj.getSummary().getProcessId());
        }

        /**
         * 1：来自关联项目
         * 2：原始协同模板喊关联项目的置灰
         */
        if ((vobj.isParentColTemplete() && null != vobj.getTemplate() && null != vobj.getTemplate().getProjectId())) {
            modelAndView.addObject("disabledProjectId", "1");
        }
        if (!relateProjectFlag) {
            Long projectId = vobj.getSummary().getProjectId();
            vobj.setProjectId(projectId);
        }

        ContentViewRet context;
        ContentConfig config = ContentConfig.getConfig(ModuleType.collaboration);
        modelAndView.addObject("contentCfg", config);
        //正文
        if (summaryId == null && Strings.isBlank(templateId) || (Strings.isNotBlank(templateId) && TemplateEnum.Type.workflow.name().equals(template.getType()))) {
            //新建协同  && 调用流程模板
            context = setWorkflowParam(null, ModuleType.collaboration);
        } else {
            Long originalContentId = null;
            String rightId = null;
            Long formAppId = null;
            if (summaryId != null) {
                originalContentId = Long.parseLong(summaryId);
            } else if (Strings.isNotBlank(templateId)) {
                originalContentId = Long.valueOf(templateId);
            }
            if (template != null && ColUtil.isForm(template.getBodyType())) {
                rightId = wapi.getNodeFormViewAndOperationName(process, null);
                formAppId = template.getFormAppId();
                colManager.addFormRightIdToFormCache(rightId, formAppId);
            }
            //设置转发后的表单不能修改正文
            ColSummary fromToSummary = vobj.getSummary();
            int viewState = CtpContentAllBean.viewState__editable;
            if (fromToSummary.getParentformSummaryid() != null && !fromToSummary.getCanEdit()) {
                ColSummary parentSummary = colManager.getSummaryById(fromToSummary.getParentformSummaryid());
                if (parentSummary != null && ColUtil.isForm(parentSummary.getBodyType())) {
                    viewState = CtpContentAllBean.viewState_readOnly;
                }
            }
            modelAndView.addObject("contentViewState", viewState);
            modelAndView.addObject("uuidlong", UUIDLong.longUUID());
            modelAndView.addObject("zwModuleId", originalContentId);

            modelAndView.addObject("zwRightId", rightId);

            modelAndView.addObject("zwIsnew", "false");
            modelAndView.addObject("zwViewState", viewState);
            context = setWorkflowParam(originalContentId, ModuleType.collaboration);
            context.setCanReply(false);
            //附言
            if ("waitSend".equals(branch) || "resend".equals(branch)) { //某些情况下不需要进这里面，比如调用模板，应该取模板的附言
                ContentUtil.findSenderCommentLists(request, config, ModuleType.collaboration, originalContentId, null);
            }
        }

        //流程相关信息
        if (context != null) {
    	 /*
    	   自由流程
    	   1.新建时：传processXML
    	   2.指定回退发起人：传processId
    	   3.重走发起人/撤销：传processXml
    	   4.处理提交时：传入processId
    	  模板流程：
    	   1.新建时：processTemplateId
    	   2.指定回退发起人：传processId
    	   3.重走发起人/撤销：processTemplateId
    	   4.处理提交时：传入processId
    	  */


            EnumManager em = (EnumManager) AppContext.getBean("enumManagerNew");
            Map<String, CtpEnumBean> ems = em.getEnumsMap(ApplicationCategoryEnum.collaboration);
            CtpEnumBean nodePermissionPolicy = ems.get(EnumNameEnum.col_flow_perm_policy.name());
            String xml = "";


            CtpTemplate t = vobj.getTemplate();

            context.setWfProcessId(vobj.getSummary().getProcessId());
            context.setWfCaseId(vobj.getCaseId() == null ? -1l : vobj.getCaseId());

            String processId = vobj.getSummary().getProcessId();
            if (t != null && t.getWorkflowId() != null) { //系统模板 & 个人模板

                if (!isSpecialSteped && !"resend".equals(from)) {
                    if (TemplateUtil.isSystemTemplate(vobj.getTemplate())) { //系统模板
                        context.setProcessTemplateId(String.valueOf(vobj.getTemplate().getWorkflowId()));
                        context.setWfProcessId("");
                    } else { //个人模板
                        modelAndView.addObject("ordinalTemplateIsSys", "no");
                        xml = wapi.selectWrokFlowTemplateXml(t.getWorkflowId().toString());
                    }

                } else if ("resend".equals(from)) {
                    xml = wapi.selectWrokFlowXml(processId);
                    context.setWfProcessId("");
                }

            } else { //自由协同
                if (!isSpecialSteped) {
                    xml = wapi.selectWrokFlowXml(processId);
                }
                if ("resend".equals(from)) {
                    context.setWfProcessId("");
                }
            }
            String[] workflowNodesInfo = wapi.getWorkflowInfos(process, ModuleType.collaboration.name(), nodePermissionPolicy);
            context.setWorkflowNodesInfo(workflowNodesInfo[0]);
            modelAndView.addObject("DR", workflowNodesInfo[1]);
            vobj.setWfXMLInfo(Strings.escapeJavascript(xml));
            modelAndView.addObject("contentContext", context);

        }

        //权限验证
        if (vobj.getSummaryId() != null) {
            AccessControlBean.getInstance().addAccessControl(ApplicationCategoryEnum.collaboration, vobj.getSummaryId(), user.getId());
        }


        if (null != vobj.getTemplate() && !TemplateEnum.Type.text.name().equals(vobj.getTemplate().getType())) {//除正文外都不能修改流程
            modelAndView.addObject("onlyViewWF", true);
        }

        modelAndView.addObject("postName", Functions.showOrgPostName(user.getPostId()));
        V3xOrgDepartment department = Functions.getDepartment(user.getDepartmentId());
        if (department != null) {
            modelAndView.addObject("departName", Functions.getDepartment(user.getDepartmentId()).getName());
        }
        // 当前登录用户名如果包含特殊字符，在前端会报js错误
        modelAndView.addObject("currentUserName", Strings.toHTML(AppContext.currentUserName()));

        //加载页面可能有的专业签章数据
        AppContext.putRequestContext("moduleId", vobj.getSummaryId());
        AppContext.putRequestContext("canDeleteISigntureHtml", true);
        if (vobj.getSummary().getDeadlineDatetime() != null) {
            vobj.setDeadLineDateTimeHidden(Datetimes.formatDatetimeWithoutSecond(vobj.getSummary().getDeadlineDatetime()));
        }

        LOG.info("vobj.processId=" + vobj.getProcessId());

        //CAP4与cap3分别
        boolean isCAP4 = false;
        if (vobj.getSummary().getFormAppid() != null) {
            isCAP4 = capFormManager.isCAP4Form(vobj.getSummary().getFormAppid());
        }
        modelAndView.addObject("isCAP4", isCAP4);

        modelAndView.addObject("vobj", vobj);
        boolean processTremTypeHasValue = false;
        if (vobj.getSummary().getProcessTermType() != null || vobj.isTemplateHasProcessTermType()) {
            processTremTypeHasValue = true;
        }
        modelAndView.addObject("processTremTypeHasValue", processTremTypeHasValue);

        boolean remindIntervalHasValue = false;
        if (vobj.getSummary().getRemindInterval() != null && vobj.getSummary().getRemindInterval() >= 0 || vobj.isTemplateHasRemindInterval()) {
            remindIntervalHasValue = true;
        }
        modelAndView.addObject("remindIntervalHasValue", remindIntervalHasValue);
        String trackValue = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_SEND);
        if (Strings.isBlank(trackValue)) {
            modelAndView.addObject("customSetTrack", "true");
        } else {
            modelAndView.addObject("customSetTrack", trackValue);
        }
        String officeOcxUploadMaxSize = SystemProperties.getInstance().getProperty("officeFile.maxSize");
        modelAndView.addObject("officeOcxUploadMaxSize", Strings.isBlank(officeOcxUploadMaxSize) ? "8192" : officeOcxUploadMaxSize);
        modelAndView.addObject("canEditColPigeonhole", canEditColPigeonhole);

        //新建节点权限设置
        Map<String, Object> baseListBtns = colManager.getNewColBaseListBtns(user.getLoginAccount(), 1);//1 == pc端
        NodePolicyVO newColNodePolicy = colManager.getNewColNodePolicy(user.getLoginAccount());
        modelAndView.addObject("basicButs", baseListBtns.get("basicButs"));
        modelAndView.addObject("newColNodePolicy", Strings.escapeJson(JSONUtil.toJSONString(newColNodePolicy)));
        modelAndView.addObject("newColNodePolicyVO", newColNodePolicy);


        //最近联系人
        String recentPeoplesStr = orgIndexManager.getRecentDataStr(user.getId(), null);
        List<WebEntity4QuickIndex> list = JSONUtil.parseJSONString(recentPeoplesStr, List.class);
        modelAndView.addObject("recentPeoples", list);

        modelAndView.addObject("recentPeoplesLength", list.size());
      /* modelAndView.addObject("relativeMembers",oml);
       modelAndView.addObject("relativeMembersLength",oml.size());*/
        //默认节点权限
        PermissionVO permission = permissionManager.getDefaultPermissionByConfigCategory(EnumNameEnum.col_flow_perm_policy.name(), user.getLoginAccount());
        modelAndView.addObject("defaultNodeName", permission.getName());
        modelAndView.addObject("defaultNodeLable", permission.getLabel());
        Map<String, Object> jval = new HashMap<String, Object>();
        jval.put("hasProjectPlugin", AppContext.hasPlugin("project"));
        jval.put("hasDocPlugin", (AppContext.hasPlugin("doc") && newColNodePolicy.isPigeonhole()));
        jval.put("hasBarCodePlug", AppContext.hasPlugin("barCode"));
        modelAndView.addObject("jval", Strings.escapeJson(JSONUtil.toJSONString(jval)));


        List<CtpEnumItem> commonImportances = enumManagerNew.getEnumItems(EnumNameEnum.common_importance);
        List<String[]> deadlines = new ArrayList<String[]>();
        List<CtpEnumItem> collaborationDeadlines = enumManagerNew.getEnumItems(EnumNameEnum.collaboration_deadline, true);
        if (Strings.isNotEmpty(collaborationDeadlines)) {
            for (CtpEnumItem item : collaborationDeadlines) {
                String[] deadline = new String[2];
                deadline[0] = item.getValue();
                deadline[1] = ResourceUtil.getString(item.getLabel());
                deadlines.add(deadline);
            }
        }

        if (template != null
                && template.getFormAppId() != null
                && Strings.isNotBlank(vobj.getSummary().getDeadlineTemplate())
                && vobj.getSummary().getDeadlineTemplate().startsWith("field")) {

            String formFieldDisplay = colManager.getFormFieldDisplay(template.getFormAppId(), vobj.getSummary().getDeadlineTemplate());
            String[] deadline = new String[2];
            deadline[0] = vobj.getSummary().getDeadlineTemplate();
            deadline[1] = formFieldDisplay;
            deadlines.add(deadline);
        }
        modelAndView.addObject("deadlines", deadlines);

        if (vobj.getSummary().getDeadline() != null) {
            vobj.getSummary().setDeadlineTemplate(String.valueOf(vobj.getSummary().getDeadline()));
        }

        List<CtpEnumItem> commonRemindTimes = enumManagerNew.getEnumItems(EnumNameEnum.common_remind_time);
        modelAndView.addObject("comImportanceMetadata", commonImportances);
        modelAndView.addObject("collaborationDeadlines", collaborationDeadlines);
        modelAndView.addObject("commonRemindTimes", commonRemindTimes);

        Map<Long, List<String>> logDescMap = new HashMap();

        //人员操作日志
        String jsonString = JSONUtil.toJSONString(logDescMap);
        modelAndView.addObject("logDescMap", jsonString);

        Boolean canPraise = true;
        Boolean isFormTemplete = false;
        if (template != null) {
            canPraise = template.getCanPraise();
            if (ColUtil.isForm(template.getBodyType())) {
                isFormTemplete = true;
            }
        }
        modelAndView.addObject("isFormTemplete", isFormTemplete);
        modelAndView.addObject("canPraise", canPraise);

        //合并处理设置

        boolean canAnyDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE, vobj.getSummary());
        boolean canPreDealMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE, vobj.getSummary());
        boolean canStartMerge = ColUtil.canMergeDealByType(BPMSeeyonPolicySetting.MergeDealType.START_MERGE, vobj.getSummary());
        modelAndView.addObject("canAnyDealMerge", canAnyDealMerge);
        modelAndView.addObject("canPreDealMerge", canPreDealMerge);
        modelAndView.addObject("canStartMerge", canStartMerge);
        //是否有表单套红模板
        boolean isFormOffice = false;
        if (ColUtil.isForm(vobj.getSummary().getBodyType())) {
            isFormOffice = true;
        }
        modelAndView.addObject("hasBarCodePlug", AppContext.hasPlugin("barCode"));
        modelAndView.addObject("isFormOffice", isFormOffice);

        AppContext.putRequestContext("isSubFlow", Integer.valueOf(ColConstant.NewflowType.child.ordinal()).equals(vobj.getSummary().getNewflowType()));


        PluginResourceLocation locationParam = new PluginResourceLocation();
        locationParam.setAffair(affair);
        locationParam.setTemplate(template);
        locationParam.setLocation(PluginResourceScope.COLL_PC_SEND);
        locationParam.putExtParams("summary", summary);

        Map<String, List<String>> pluginResources = CollaborationPluginUtils.getPluginRersources(locationParam);

        AppContext.putRequestContext("pluginJsFiles", pluginResources.get("pluginJsFiles"));
        AppContext.putRequestContext("pluginCssFiles", pluginResources.get("pluginCssFiles"));

        return modelAndView;
    }

    /**
     * <pre>
     *    判定是否有新建的权限
     *    目前规则：
     *           按钮没有新建事项时，不可以发起自由协同，但是可以通过模板发起
     * </pre>
     *
     * @param user       用户信息
     * @param templateId 模板ID
     * @param from       请求来源
     * @return boolean 是否具有权限，true:有，false：没有
     */
    private boolean hasNewCollAuth(User user, String templateId, String from) {

        // 判定逻辑
        // 1. 如果有新建事项按钮权限，返回为true
        // 2. 否则没有新建事项按钮权限
        //   a: 有templateId,返回为true
        // 其他情况返回为false

        return MenuPurviewUtil.isHaveNewColl(user) || Strings.isNotBlank(templateId);
    }


    /**
     * 转协同数据处理
     *
     * @param vobj
     * @param summary
     * @param modelAndView
     * @param user
     * @param request
     * @throws BusinessException
     * @Author : xuqw
     * @Date : 2015年9月6日下午3:57:37
     */
    private void initNewCollTranVO(NewCollTranVO vobj, ColSummary summary,
                                   ModelAndView modelAndView, User user, HttpServletRequest request)
            throws BusinessException {
        String cashId = request.getParameter("cashId");
        Object object = V3xShareMap.get(cashId);
        if (object == null) {
            return;
        }
        Map<String, String> map = (Map) object;
        String subject = map.get("subject") == null ? "" : map.get("subject");
        String manual = map.get("manual") == null ? "" : map.get("manual");
        String handlerName = map.get("handlerName") == null ? "" : map.get("handlerName");
        String sourceId = map.get("sourceId") == null ? "" : map.get("sourceId");
        String extendInfo = map.get("ext") == null ? "" : map.get("ext");
        String bodyTypes = map.get("bodyType") == null ? "" : map.get("bodyType");
        String bodyContent = map.get("bodyContent") == null ? "" : map.get("bodyContent");
        String personId = map.get("personId") == null ? "" : map.get("personId");
        String from = map.get("from") == null ? "" : map.get("from");

        summary.setSubject(subject);
        NewCollDataHandlerInterface handler = NewCollDataHelper.getHandler(handlerName);

        Map<String, Object> params = null;
        if (handler != null) {
            params = handler.getParams(sourceId, extendInfo);
        }

        if ("true".equalsIgnoreCase(manual) && handler != null && params != null) {// 从后端代码获取参数
            if (Strings.isBlank(subject)) {
                summary.setSubject(handler.getSubject(params));
            }
            bodyTypes = String.valueOf(handler.getBodyType(params));
            bodyContent = handler.getBodyContent(params);
        }

        int bodyType = MainbodyType.HTML.getKey();
        if (Strings.isNotBlank(bodyTypes)) {
            bodyType = Integer.parseInt(bodyTypes);
        }

        //HTML正文或者表单正文
        if (MainbodyType.HTML.getKey() == bodyType || ColUtil.isForm(bodyType)) {
            StringBuilder buf = new StringBuilder();
            buf.append(bodyContent == null ? "" : Strings.toHTML(bodyContent.replace("\t", "").replace("\n", ""), false));
            bodyContent = buf.toString();
        } else {
            modelAndView.addObject("zwContentType", bodyType);
            modelAndView.addObject("transOfficeId", bodyContent);
        }

        summary.setBodyType(String.valueOf(bodyType));
        modelAndView.addObject("contentTextData", bodyContent);
        modelAndView.addObject("transtoColl", "true");

        if (handler != null && params != null) {
            List<Attachment> atts = handler.getAttachments(params);
            // 附件处理
            vobj.setAtts(atts);
            if (Strings.isNotEmpty(atts)) {
                String attListJSON = attachmentManager.getAttListJSON(atts);
                vobj.setAttListJSON(attListJSON);
            }
            vobj.setCloneOriginalAtts(true);
        }

        // 人员卡ID
        setWorkFlowMember(personId, user, modelAndView);

        // from参数设置
        modelAndView.addObject("from", from);
    }

    /**
     * 新建协同，
     *
     * @param memberId
     * @throws BusinessException
     * @throws
     * @Author : xuqw
     * @Date : 2015年9月7日下午6:02:04
     */
    private void setWorkFlowMember(String memberId, User user, ModelAndView modelAndView) throws BusinessException {

        if (Strings.isNotBlank(memberId)) {
            V3xOrgMember sender = orgManager.getMemberById(Long.valueOf(memberId));
            V3xOrgAccount account = orgManager.getAccountById(sender.getOrgAccountId());
            modelAndView.addObject("accountObj", account);
            modelAndView.addObject("isSameAccount", String.valueOf(sender.getOrgAccountId().equals(user.getLoginAccount())));
            modelAndView.addObject("peopeleCardInfo", sender);
        }
    }

    private ContentViewRet setWorkflowParam(Long moduleId, ModuleType moduleType) {

        ContentViewRet context = new ContentViewRet();
        context.setModuleId(moduleId);
        context.setModuleType(moduleType.getKey());
        context.setCommentMaxPath("00");
        return context;

    }

    private void getTrackInfo(ModelAndView modelAndView, NewCollTranVO vobj, String smmaryId) throws BusinessException {
        CtpAffair affairSent = affairManager.getSenderAffair(Long.valueOf(smmaryId));
        if ("waitSend".equals(vobj.getFrom()) && Strings.isNotBlank(vobj.getAffairId()) && !"null".equals(vobj.getAffairId())) {
            affairSent = affairManager.get(Long.valueOf(vobj.getAffairId()));
        }
        String showTrackName = "";
        String showTrackId = "";
        if (affairSent != null) {
            Integer trackType = affairSent.getTrack().intValue();
            modelAndView.addObject("trackType", trackType);
            List<CtpTrackMember> tList = trackManager.getTrackMembers(affairSent.getId());
            StringBuilder trackNames = new StringBuilder();
            StringBuilder trackIds = new StringBuilder();
            if (tList.size() > 0) {
                for (CtpTrackMember ctpT : tList) {
                    if (ctpT.getTrackMemberId() != null) {
                        trackNames.append("Member|")
                                .append(ctpT.getTrackMemberId())
                                .append(",");
                        trackIds.append(ctpT.getTrackMemberId() + ",");
                    }
                }
                if (trackNames.length() > 0) {
                    showTrackName = trackNames.substring(0, trackNames.length() - 1);
                    showTrackId = trackIds.substring(0, trackIds.length() - 1);
                }
            }
        }
        vobj.setForGZShow(showTrackName);
        modelAndView.addObject("forGZIds", showTrackId);
    }

    /**
     * 批量下载：文件
     *
     * @throws BusinessException
     */
    public ModelAndView checkFile(HttpServletRequest request, HttpServletResponse response) throws IOException,
            BusinessException {
        String userId = request.getParameter("userId");
        String docId = request.getParameter("docId");
        String isBorrow = request.getParameter("isBorrow");
        String vForDocDownload = request.getParameter("v");

        if (Strings.isBlank(userId) || !userId.equals(String.valueOf(AppContext.currentUserId()))) {
            PrintWriter out = response.getWriter();
            out.print("1");
            out.close();
            return null;
        }

        // 有权限
        String result = null;
        String context = SystemEnvironment.getContextPath();
        V3XFile vf = fileManager.getV3XFile(Long.valueOf(docId));
        result = "0#" + context + "/fileDownload.do?method=doDownload&viewMode=download&fileId=" + vf.getId() + "&filename=" + java.net.URLEncoder.encode(vf.getFilename(), "UTF-8") + "&createDate=" + Datetimes.formatDate(vf.getCreateDate()) + "&v=" + Strings.escapeJavascript(vForDocDownload);
        PrintWriter out = response.getWriter();
        out.print(result);
        out.close();
        return null;
    }

    private void newCollAlert(HttpServletResponse response, String msg) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("alert('" + msg + "');");
        out.print("parent.window.history.back();");
        out.println("</script>");
        out.flush();
    }


    public void setWapi(WorkflowApiManager wapi) {
        this.wapi = wapi;
    }

    /**
     * 存为草稿
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveDraft(HttpServletRequest request,
                                  HttpServletResponse response) throws BusinessException {
        ColInfo info = new ColInfo();
        User user = AppContext.getCurrentUser();

        Map para = ParamUtil.getJsonDomain("colMainData");
        info.setDR((String) para.get("DR"));
        ColSummary summary = (ColSummary) ParamUtil.mapToBean(para, new ColSummary(), false);

        String clientDeadTime = (String) para.get("deadLineDateTime");
        if (Strings.isNotBlank(clientDeadTime)) {
            Date serviceDeadTime = Datetimes.parse(clientDeadTime);
            summary.setDeadlineDatetime(serviceDeadTime);
        }

        String deadLine = (String) para.get("deadlineTemplate");
        if (Strings.isNotBlank(deadLine)) {
            summary.setDeadlineTemplate(deadLine);
            if (Strings.isDigits(deadLine)) {
                summary.setDeadline(Long.parseLong(deadLine));
            }
        }


        // 获取关联项目的ID
        String selectProjectId = ParamUtil.getString(para, "selectProjectId", "-1");
        if ("-1".equals(selectProjectId)) {
            summary.setProjectId(null);
        } else {
            summary.setProjectId(Long.valueOf(selectProjectId));
        }

        boolean isCap4Forward = false;
        if (Strings.isNotBlank((String) para.get("isCap4Forward"))) {
            isCap4Forward = "1".equals((String) para.get("isCap4Forward"));
        }
        if (!ColUtil.isForm(summary.getBodyType())) {
            summary.setFormRecordid(null);
            if (!isCap4Forward) {//转发cap4表单该属性不能去掉。
                summary.setFormAppid(null);
            }
        }
        //TODO  校验模板是否存在

        Map para1 = ParamUtil.getJsonDomain("senderOpinion");
        Comment comment = (Comment) ParamUtil.mapToBean(para1, new Comment(), false);
        boolean saveProcessFlag = true;
        CtpTemplate ct = null;
        if (Strings.isNotBlank((String) para.get("tId"))) {
            Long templateIdLong = Long.valueOf((String) para.get("tId"));
            info.settId(templateIdLong);
            ct = templateManager.getCtpTemplate(templateIdLong);
            if (!"text".equals(ct.getType())) {
                saveProcessFlag = false;//流程模板和协同模板的 保存待发 才存入processId
            }
        }
        if (Strings.isNotBlank((String) para.get("curTemId"))) {
            info.setCurTemId(Long.valueOf((String) para.get("curTemId")));
        }
        if (para.get("processTermTypeCheck") == null) {
            summary.setProcessTermType(null);
        }
        if (para.get("remindIntervalCheckBox") == null) {
            summary.setRemindInterval(null);
        }
        //保存合并处理策略
        Map<String, String> mergeDealType = new HashMap<String, String>();
        String canStartMerge = (String) para.get("canStartMerge");
        if ((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)) {
            mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
        }
        String canPreDealMerge = (String) para.get("canPreDealMerge");
        if ((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)) {
            mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
        }
        String canAnyDealMerge = (String) para.get("canAnyDealMerge");
        if ((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)) {
            mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
        }
        summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));
        String subjectForCopy = (String) para.get("subjectForCopy");
        info.setSubjectForCopy(subjectForCopy);
        String isNewBusiness = (String) para.get("newBusiness");
        info.setNewBusiness("1".equals(isNewBusiness) ? true : false);
        info.setSummary(summary);
        info.setCurrentUser(user);
        //关于跟踪的代码
        Object canTrack = para.get("canTrack");
        int track = 0;
        if (null != canTrack) {
            track = 1;//affair的track为1的时候为全部跟踪，0时为不跟踪，2时为跟踪指定人
            if (null != para.get("radiopart")) {
                track = 2;
            }
            info.getSummary().setCanTrack(true);
        } else {
            //如果没勾选跟踪，这里讲值设置为false
            info.getSummary().setCanTrack(false);
        }
        info.setTrackType(track);

        String newSubject = "";
        if (ct != null && "template".equals(ct.getType())) {
            ColSummary summary1 = XMLCoder.decoder(ct.getSummary(), ColSummary.class);
            //判断动态更新是否勾选
            if (summary1 != null && Boolean.TRUE.equals(summary1.getUpdateSubject())) {
                newSubject = ColUtil.makeSubject(ct, summary, user);
                if (Strings.isBlank(newSubject)) {
                    newSubject = "{" + ResourceUtil.getString("collaboration.subject.default") + "}";
                }
                info.getSummary().setSubject(newSubject);
            }
        }

        //得到跟踪人员的ID
        String trackMemberId = (String) para.get("zdgzry");
        info.setTrackMemberId(trackMemberId);
        String contentSaveId = (String) para.get("contentSaveId");
        info.setContentSaveId(contentSaveId);
        Map map = colManager.saveDraft(info, saveProcessFlag, para);
        /**
         OA-60206 流程表单中设置了数据关联--关联表单的附件字段，调用时保存待发两次，关联的附件就丢失了
         reason:   表单保存代发一次后清掉了seesion缓存
         修改方法： 表单保存后模拟从待发列表到新建页面的重新编辑过程
         */
        try {
            String retJs = "parent.endSaveDraft('" + map.get("summaryId").toString() + "','" + map.get("contentId").toString() + "','" + map.get("affairId").toString() + "')";
            if (Strings.isNotBlank(newSubject)) {
                retJs = "parent.endSaveDraft('" + map.get("summaryId").toString() + "','" + map.get("contentId").toString() + "','" + map.get("affairId").toString() + "','" + newSubject + "')";
            }
            super.rendJavaScript(response, retJs);
            //super.rendJavaScript(response, "parent.endSaveDraft('" + map.get("summaryId").toString() + "','"+map.get("contentId").toString()+"','"+map.get("affairId").toString()+"')");
        } catch (Exception e) {
            LOG.error("调用js报错！", e);
        }
        //return redirectModelAndView("collaboration.do?method=newColl&summaryId="+map.get("summaryId").toString()+"&from=waitSend");
        return null;
    }

    /**
     * 发送协同
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView send(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //前段参数完整性校验
        if (!checkHttpParamValid(request, response)) {
            return null;
        }
        //构建协同VO对象
        Map para = ParamUtil.getJsonDomain("colMainData");
        //协同信息
        ColSummary summary = (ColSummary) ParamUtil.mapToBean(para, new ColSummary(), false);
        //新建发送设置summary新ID
        String clientDeadTime = (String) para.get("deadLineDateTime");
        if (Strings.isNotBlank(clientDeadTime)) {
            Date serviceDeadTime = Datetimes.parse(clientDeadTime);
            summary.setDeadlineDatetime(serviceDeadTime);
        }

        String deadLine = (String) para.get("deadlineTemplate");
        if (Strings.isNotBlank(deadLine)) {
            summary.setDeadlineTemplate(deadLine);
        }

        summary.setSubject(Strings.nobreakSpaceToSpace(summary.getSubject()));
        ColInfo info = new ColInfo();
        info.setDR((String) para.get("DR"));
        if (null != para.get("phaseId") && Strings.isNotBlank((String) para.get("phaseId"))) {
            info.setPhaseId((String) para.get("phaseId"));
        }
        if (Strings.isNotBlank((String) para.get("tId"))) {
            info.settId(Long.valueOf((String) para.get("tId")));
        }
        if (Strings.isNotBlank((String) para.get("curTemId"))) {
            info.setCurTemId(Long.valueOf((String) para.get("curTemId")));
        }
        if (Strings.isNotBlank((String) para.get("parentSummaryId"))) {
            summary.setParentformSummaryid(Long.valueOf((String) para.get("parentSummaryId")));
        }
        boolean isCap4Forward = false;
        if (Strings.isNotBlank((String) para.get("isCap4Forward"))) {
            isCap4Forward = "1".equals((String) para.get("isCap4Forward"));
        }
        if (!ColUtil.isForm(summary.getBodyType())) {
            summary.setFormRecordid(null);
            if (!isCap4Forward) {//转发cap4表单该属性不能去掉。
                summary.setFormAppid(null);
            }
        }
        if (para.get("processTermTypeCheck") == null) {
            summary.setProcessTermType(null);
        }
        if (para.get("remindIntervalCheckBox") == null) {
            summary.setRemindInterval(null);
        }

        //保存合并处理策略
        Map<String, String> mergeDealType = new HashMap<String, String>();
        String canStartMerge = (String) para.get("canStartMerge");
        if ((BPMSeeyonPolicySetting.MergeDealType.START_MERGE.getValue()).equals(canStartMerge)) {
            mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.START_MERGE.name(), canStartMerge);
        }
        String canPreDealMerge = (String) para.get("canPreDealMerge");
        if ((BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.getValue()).equals(canPreDealMerge)) {
            mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.PRE_DEAL_MERGE.name(), canPreDealMerge);
        }
        String canAnyDealMerge = (String) para.get("canAnyDealMerge");
        if ((BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.getValue()).equals(canAnyDealMerge)) {
            mergeDealType.put(BPMSeeyonPolicySetting.MergeDealType.DEAL_MERGE.name(), canAnyDealMerge);
        }
        summary.setMergeDealType(JSONUtil.toJSONString(mergeDealType));

        String isNewBusiness = (String) para.get("newBusiness");
        info.setNewBusiness("1".equals(isNewBusiness) ? true : false);
        info.setSummary(summary);
        ColConstant.SendType sendType = ColConstant.SendType.normal;
        User user = AppContext.getCurrentUser();
        info.setCurrentUser(user);
        //跟踪的相关逻辑代码(根据Id来去值) 并且判断出跟踪的类型指定人还是全部人
        Object canTrack = para.get("canTrack");
        int track = 0;
        if (null != canTrack) {
            track = 1;//affair的track为1的时候为全部跟踪，0时为不跟踪，2时为跟踪指定人
            if (null != para.get("radiopart")) {
                track = 2;
            }
            info.getSummary().setCanTrack(true);
        } else {
            //如果没勾选跟踪，这里讲值设置为false
            info.getSummary().setCanTrack(false);
        }
        info.setTrackType(track);
        //得到跟踪人员的ID
        info.setTrackMemberId((String) para.get("zdgzry"));
        //
        String caseId = (String) para.get("caseId");
        info.setCaseId(StringUtil.checkNull(caseId) ? null : Long.parseLong(caseId));
        String currentaffairId = (String) para.get("currentaffairId");
        info.setCurrentAffairId(StringUtil.checkNull(currentaffairId) ? null : Long.parseLong(currentaffairId));
        String currentProcessId = (String) para.get("oldProcessId");
        LOG.info("老协同的currentProcessId=" + currentProcessId);
        info.setCurrentProcessId(StringUtil.checkNull(currentProcessId) ? null : Long.parseLong(currentProcessId));
        info.setTemplateHasPigeonholePath(String.valueOf(Boolean.TRUE).equals(para.get("isTemplateHasPigeonholePath")));

        String formViewOperation = (String) para.get("formViewOperation");
        info.setFormViewOperation(formViewOperation);

        int bodyType = 0;
        try {
            bodyType = Integer.parseInt(summary.getBodyType());
        } catch (Exception e) {

        }
        if (bodyType > 40 && bodyType < 46) {
            List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
            if (Strings.isEmpty(contents)) {
                ColUtil.webAlertAndClose(response, ResourceUtil.getString("coll.save.content.lable.tip"));
                return null;
            }
        }
        boolean isLock = false;
        String snInfos = null;

        try {

            //前段参数完整性校验
            if (!checkHttpParamValid(request, response)) {
                return null;
            }


            //colLockManager加锁后，必须必须确保在finally里面解锁
            isLock = colLockManager.canGetLock(summary.getId());
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作-send,affairId" + summary.getId());
                return null;
            }


            Map<String, String> sendRet = colManager.transSend(info, sendType);


            snInfos = sendRet.get("snInfos");

        } catch (Exception e) {

            LOG.error("", e);


            String msg = e.getMessage();
            if (Strings.isBlank(msg)) {
                /* "网络异常，发送协同失败，请稍后重试"*/
                msg = ResourceUtil.getString("collaboration.send.error.label.js");
            }
            msg = Strings.escapeJavascript(msg);

            StringBuilder successJson = new StringBuilder();

            successJson.append("{");
            successJson.append("'code':1");
            successJson.append(",'message':'" + msg + "'");
            successJson.append("}");

            ColUtil.webWriteMsg(response, successJson.toString());
            /*ColUtil.webAlert(response,ResourceUtil.getString("collaboration.send.error.label.js"));*/
            return null;
        } finally {
            if (isLock) {
                colLockManager.unlock(summary.getId());
            }
        }

        //如果是精灵则直接关闭窗口，否则按常规返回页面
        if ("a8genius".equals(request.getParameter("from"))) {
            String script = "\r\n " +
                    "var userAgent = navigator.userAgent;" +
                    "if(userAgent.indexOf(\"Chrome\") > -1) {\r\n" +
                    "	top.location=\"collaboration.do?method=showDealSuccessfully\";" +
                    "} else {" +
                    "	try {" +
                    "		parent.parent.parent.closeWindow();" +
                    "	} catch(e) {" +
                    "		window.close()" +
                    "	}" +
                    "}";
            super.rendJavaScript(response, script);

            return null;
        }
        Map<String, Object> lshmap = (Map) request.getAttribute("lshMap");
        StringBuilder lshsb = new StringBuilder();
        if (null != lshmap) {
            response.setContentType("text/html;charset=UTF-8");
            for (Map.Entry entry : lshmap.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
//            	lshsb.append("已在{" +key+"}项上生成流水号:"+ value+"\n");
                lshsb.append(ResourceUtil.getString("collaboration.send.error.label.js", key) + value + "\n");
            }
            String tslshString = lshsb.toString();
            PrintWriter out = response.getWriter();
            out.println("<script>");
            out.println("alert('" + StringEscapeUtils.escapeJavaScript(tslshString) + "');");
            out.println("window.location.href = 'collaboration.do?method=listSent';");
            out.println("</script>");
            out.flush();
            return null;
        }

        if ("true".equals(para.get("isOpenWindow"))) {

            StringBuilder successJson = new StringBuilder();
            successJson.append("{");
            successJson.append("'code':0");

            if (Strings.isNotBlank(snInfos)) {
                successJson.append(",'snInfos':'" + snInfos + "'");
            }
            successJson.append("}");

            ColUtil.webWriteMsg(response, successJson.toString());

            //super.rendJavaScript(response, "window.close();");
            return null;
        }
        return redirectModelAndView("collaboration.do?method=listSent");
    }

    /**
     * <pre>
     * 	处理成功的页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     * @return: ModelAndView
     * @date: 2019年8月6日
     * @author: yaodj
     * @since v7.1 sp1
     * </pre>
     */
    public ModelAndView showDealSuccessfully(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/dealSuccessfully");
        return mav;
    }

    /**
     * 列表中立即发送
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView sendImmediate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> ret = new HashMap<String, Object>();
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String workflowDataFlag = wfdef.get("workflow_data_flag");
        if (Strings.isBlank(workflowDataFlag) || "undefined".equals(workflowDataFlag.trim()) || "null".equals(workflowDataFlag.trim())) {
            LOG.info("来自立即发送sendImmediate");
        }
        Map params = ParamUtil.getJsonParams();
        boolean bpmFlag = "1".equals((String) params.get("bpmMenuFlag")) ? true : false;
        String summaryIds = (String) params.get("summaryId");
        String affairIds = (String) params.get("affairId");
        if (summaryIds == null || affairIds == null) {
            return null;
        }
        boolean sentFlag = false;
        String workflowNodePeoplesInput = "";
        String workflowNodeConditionInput = "";
        String workflowNewflowInput = "";
        String toReGo = "";
        if (null != params.get("workflow_node_peoples_input")) {
            workflowNodePeoplesInput = (String) params.get("workflow_node_peoples_input");
        }
        if (null != params.get("workflow_node_condition_input")) {
            workflowNodeConditionInput = (String) params.get("workflow_node_condition_input");
        }
        if (null != params.get("workflow_newflow_input")) {
            workflowNewflowInput = (String) params.get("workflow_newflow_input");
        }
        if (null != params.get("toReGo")) {
            toReGo = (String) params.get("toReGo");
        }
        int bodyType = 0;
        ColSummary summary = null;
        try {
            summary = colManager.getColSummaryById(Long.valueOf(summaryIds));
            if (null != summary) {
                bodyType = Integer.parseInt(summary.getBodyType());
            } else {
                if (bpmFlag) {
                    LOG.info("协同已经被删除，不能发送该协同！");
                    ret.put("resourceCode", "F01_listWaitSend");
                    ColUtil.webWriteMsg(response, JSONUtil.toJSONString(ret));
                    return null;
                } else {
                    return redirectModelAndView("collaboration.do?method=listWaitSend" + Functions.csrfSuffix());
                }


            }
        } catch (Exception e) {

        }
        if (bodyType > 40 && bodyType < 46 && summary != null) {
            List<CtpContentAll> contents = ctpMainbodyManager.getContentListByModuleIdAndModuleType(ModuleType.collaboration, summary.getId());
            if (Strings.isEmpty(contents)) {
                if (bpmFlag) {
                    LOG.info("正文不存在不能立即 发送，请重新编辑后发送!");
                    ret.put("resourceCode", "F01_listSent");
                    ColUtil.webWriteMsg(response, JSONUtil.toJSONString(ret));
                    return null;
                } else {
                    return redirectModelAndView("collaboration.do?method=listSent" + Functions.csrfSuffix());
                }

            }
        }

        boolean isLock = false;
        if (summary != null) {
            try {
                //colLockManager加锁后，必须必须确保在finally里面解锁
                isLock = colLockManager.canGetLock(summary.getId());
                if (!isLock) {
                    LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作sendImmediate,affairId" + summary.getId());
                    return null;
                }

                CtpAffair sendAffair = affairManager.getSenderAffair(summary.getId());
                if (sendAffair == null || StateEnum.col_waitSend.getKey() != sendAffair.getState().intValue()) {
                    return redirectModelAndView("collaboration/collaboration.do?method=listWaitSend" + Functions.csrfSuffix());
                }
                colManager.transSendImmediate(summaryIds, sendAffair, sentFlag, workflowNodePeoplesInput, workflowNodeConditionInput, workflowNewflowInput, toReGo);
            } finally {
                if (isLock) {
                    colLockManager.unlock(summary.getId());
                }
            }
        }
        if (bpmFlag) {
            ret.put("resourceCode", "F01_listSent");
            ColUtil.webWriteMsg(response, JSONUtil.toJSONString(ret));
            return null;
        } else {
            return redirectModelAndView("collaboration.do?method=listSent" + Functions.csrfSuffix());
        }
    }

    /**
     * 已发协同列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listSent(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/listSent");
        FlipInfo fi = new FlipInfo();
        Map<String, String> param = getWebQueryCondition(fi, request);
        request.setAttribute("fflistSent", colManager.getSentList(fi, param));
        NodePolicyVO newColNodePolicy = colManager.getNewColNodePolicy(AppContext.currentAccountId());
        boolean isHaveNewColl = MenuPurviewUtil.isHaveNewColl(AppContext.getCurrentUser());

        modelAndView.addObject("newColNodePolicy", Strings.escapeJson(JSONUtil.toJSONString(newColNodePolicy)));
        modelAndView.addObject("isHaveNewColl", isHaveNewColl);
        modelAndView.addObject("paramMap", param);
        modelAndView.addObject("hasDumpData", DumpDataVO.isHasDumpData());
        String isShowTotal = PerformanceConfig.getInstance().getConfig("col.grid.total");
        modelAndView.addObject("isShowTotal", isShowTotal);
        modelAndView.addObject("showHandoverButton", true/*handoverManager.hasHandoverConfig(AppContext.currentUserId())*/);
        return modelAndView;
    }

    /**
     * yangwulin 列出我的待办、已办、已发，并根据是否允许转发进行权限过滤，用在协同用引用
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView list4Quote(HttpServletRequest request,
                                   HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/list4Quote");
        FlipInfo fi = new FlipInfo();
        Map<String, String> param = getWebQueryCondition(fi, request);
        // request.setAttribute("fflistSend", this.colManager.getSentlist4Quote(fi,param));
        String isShowTotal = PerformanceConfig.getInstance().getConfig("col.grid.total");
        modelAndView.addObject("isShowTotal", isShowTotal);
        modelAndView.addObject("hasDumpData", DumpDataVO.isHasDumpData());
        modelAndView.addObject("showHandoverButton", true/*handoverManager.hasHandoverConfig(AppContext.currentUserId())*/);
        return modelAndView;
    }

    /**
     *
     */
    private Map<String, String> getWebQueryCondition(FlipInfo fi, HttpServletRequest request) {
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        Map<String, String> query = new HashMap<String, String>();
        if (Strings.isNotBlank(condition) && Strings.isNotBlank(textfield)) {
            query.put(condition, textfield);
            fi.setParams(query);
        }
        return query;
    }

    /**
     * 已办协同列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listDone(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/listDone");
        FlipInfo fi = new FlipInfo();
        Map<String, String> param = getWebQueryCondition(fi, request);
        String openFrom = request.getParameter("openFrom");
        if ("aiProcess".equals(openFrom)) {
            String beginTime = request.getParameter("beginTime");
            String endTime = request.getParameter("endTime");
            String dealDate = Datetimes.formatDateStr(beginTime, "yyyy-MM-dd HH:mm:ss") + "#" + Datetimes.formatDateStr(endTime, "yyyy-MM-dd HH:mm:ss");
            param.put(ColQueryCondition.dealDate.name(), dealDate);
            param.put(ColQueryCondition.aiProcessing.name(), "true");
            param.put("openFrom", openFrom);
            modelAndView.addObject("showAIProcessing", "true");
            modelAndView.addObject("beginTime", Datetimes.formatDateStr(beginTime, "yyyy-MM-dd"));
            modelAndView.addObject("endTime", Datetimes.formatDateStr(endTime, "yyyy-MM-dd"));
        }
        request.setAttribute("fflistDone", colManager.getDoneList(fi, param));
        modelAndView.addObject("paramMap", param);
        modelAndView.addObject("hasDumpData", DumpDataVO.isHasDumpData());
        modelAndView.addObject("hasAIPlugin", AppContext.hasPlugin("ai"));
        String isShowTotal = PerformanceConfig.getInstance().getConfig("col.grid.total");
        modelAndView.addObject("isShowTotal", isShowTotal);
        modelAndView.addObject("showHandoverButton", true/*handoverManager.hasHandoverConfig(AppContext.currentUserId())*/);
        return modelAndView;
    }

    /**
     * 待办协同列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listPending(HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/listPending");
        String bpmType = request.getParameter("bpmMenu");
        modelAndView.addObject("bpmMenu", "1".equalsIgnoreCase(bpmType));
        FlipInfo fi = new FlipInfo();
        Map<String, String> param = getWebQueryCondition(fi, request);
        if (AppContext.hasPlugin("ai")) {
            String aiSortJSON = customizeManager.getCustomizeValue(AppContext.currentUserId(), CustomizeConstants.PENDING_CENTER_AI_SORT);
            Map<String, String> aiSortMap = JSONUtil.parseJSONString(aiSortJSON, Map.class);
            if (aiSortMap != null && "true".equals(aiSortMap.get("listPending"))) {
                param.put(ColQueryCondition.aiSort.name(), "true");
                modelAndView.addObject("aiSortValue", "true");
            }
            modelAndView.addObject("hasAIPlugin", "true");
        }
        String isShowStatisticalToolBar = PerformanceConfig.getInstance().getConfig("col.todo.statistical");
        modelAndView.addObject("isShowStatisticalToolBar", isShowStatisticalToolBar);
        String isShowTotal = PerformanceConfig.getInstance().getConfig("col.grid.total");
        modelAndView.addObject("isShowTotal", isShowTotal);
        request.setAttribute("fflistPending", colManager.getPendingList(fi, param));
        modelAndView.addObject("paramMap", param);
        return modelAndView;
    }

    /**
     * 待发协同列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listWaitSend(HttpServletRequest request,
                                     HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/listWaitSend");
        FlipInfo fi = new FlipInfo();
        Map<String, String> param = getWebQueryCondition(fi, request);
        request.setAttribute("fflistWaitSend", colManager.getWaitSendList(fi, param));
        NodePolicyVO newColNodePolicy = colManager.getNewColNodePolicy(AppContext.currentAccountId());
        modelAndView.addObject("newColNodePolicy", Strings.escapeJson(JSONUtil.toJSONString(newColNodePolicy)));
        modelAndView.addObject("paramMap", param);
        String isShowTotal = PerformanceConfig.getInstance().getConfig("col.grid.total");
        modelAndView.addObject("isShowTotal", isShowTotal);
        return modelAndView;
    }

    /**
     * 协同处理页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView summary(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        ModelAndView mav = new ModelAndView("apps/collaboration/summary");
        ColSummaryVO summaryVO = new ColSummaryVO();
        User user = AppContext.getCurrentUser();

        String affairId = request.getParameter("affairId");
        String summaryId = request.getParameter("summaryId");
        String processId = request.getParameter("processId");
        String canExePrediction = request.getParameter("canExePrediction");

        String operationId = request.getParameter("operationId");
        String formMutilOprationIds = request.getParameter("formMutilOprationIds");

        String openFrom = request.getParameter("openFrom");
        String type = request.getParameter("type");
        String contentAnchor = request.getParameter("contentAnchor");
        String pigeonholeType = request.getParameter("pigeonholeType");
        String sessionId = HttpSessionUtil.getSessionId(request);
        String trackTypeRecord = request.getParameter("trackTypeRecord");
        mav.addObject("trackTypeRecord", trackTypeRecord);
        String dumpData = request.getParameter("dumpData");
        boolean isHistoryFlag = "1".equals(dumpData);
        summaryVO.setHistoryFlag(isHistoryFlag);
        //校验传入的参数是否非法
        if ((Strings.isNotBlank(affairId) && !org.apache.commons.lang.math.NumberUtils.isNumber(affairId))
                || (Strings.isNotBlank(summaryId) && !org.apache.commons.lang.math.NumberUtils.isNumber(summaryId))
                || (Strings.isNotBlank(processId) && !org.apache.commons.lang.math.NumberUtils.isNumber(processId))) {
            ColUtil.webAlertAndClose(response, ResourceUtil.getString("coll.summary.lable.tip1"));
            return null;
        }
        if (Strings.isBlank(affairId) && Strings.isBlank(summaryId) && Strings.isBlank(processId)) {
            ColUtil.webAlertAndClose(response, ResourceUtil.getString("coll.summary.lable.tip2"));
            LOG.info("无法访问该协同，请求参数中必须有affairId,summaryId,processId 三个参数中的一个！");
            return null;
        }


        summaryVO.setProcessId(processId);
        summaryVO.setSummaryId(summaryId);
        if (ColOpenFrom.subFlow.name().equals(openFrom)) {
            summaryVO.setFormViewOperation(formMutilOprationIds);
        } else {
            summaryVO.setFormViewOperation(operationId);
        }
        summaryVO.setAffairId(Strings.isBlank(affairId) ? null : Long.parseLong(affairId));
        summaryVO.setOpenFrom(openFrom);
        summaryVO.setType(type);
        summaryVO.setCurrentUser(user);
        summaryVO.setLenPotent(request.getParameter("lenPotent"));

        boolean isBlank = Strings.isBlank(pigeonholeType) || "null".equals(pigeonholeType) || "undefined".equals(pigeonholeType);
        summaryVO.setPigeonholeType(isBlank ? PigeonholeType.edoc_dept.ordinal() : Integer.valueOf(pigeonholeType));


        try {
            summaryVO = colManager.transShowSummary(summaryVO);
            if (summaryVO == null) {
                return null;
            }
        } catch (Exception e) {
            LOG.error("summary方法中summaryVO为空", e);
            ColUtil.webAlertAndClose(response, e.getMessage());
            return null;
        }
        //todo zhou :金智代办中心已处理数据还存在问题解决【开始】

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        Long aLong = Long.parseLong(affairId);
        CtpAffair Affair_ = affairManager.get(aLong);
        List<Map<String, Object>> mapList = new ArrayList<>();

        if (null != Affair_) {
            Map<String, Object> map2 = new HashMap<>();
            map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
            map2.put("task_id", Affair_.getObjectId().longValue() + "");
            map2.put("task_delete_flag", 1);
            map2.put("process_instance_id", Affair_.getProcessId());
            map2.put("process_delete_flag", 1);
            mapList.add(map2);
        }

        //todo zhou :金智代办中心已处理数据还存在问题解决【结束】

        if (Strings.isNotBlank(summaryVO.getErrorMsg())) {
            ColUtil.webAlertAndClose(response, summaryVO.getErrorMsg());
            //todo zhou :金智代办中心已处理数据还存在问题解决【开始】
            if (null != Affair_) {
                KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
            }
            //todo zhou :金智代办中心已处理数据还存在问题解决【结束】
            return null;
        }
        if(null != Affair_){
            if (Affair_.getState().intValue() == 4 || Affair_.getState().intValue() == 5 || Affair_.getState().intValue() == 6 ||
                    Affair_.getState().intValue() == 7 || Affair_.getState().intValue() == 8 || Affair_.getState().intValue() == 15) {
                //todo zhou:[医科大学]在金智代办中已经处理的数据还显示问题修改 【开始】
                KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
                //todo zhou:[医科大学]在金智代办中已经处理的数据还显示问题修改 【结束】
            }
        }


        mav.addObject("forwardEventSubject", summaryVO.getSubject());
        summaryVO.setSubject(Strings.toHTML(Strings.toText(summaryVO.getSubject())));
        summaryVO.getSummary().setSubject(Strings.toHTML(summaryVO.getSummary().getSubject()));
        mav.addObject("summaryVO", summaryVO);

        // 当前登录用户名如果包含特殊字符，在前端会报js错误
        mav.addObject("currentUserName", Strings.toHTML(AppContext.currentUserName()));
        //锚点，用于消息打开的时候倒叙去查询谁处理的消息
        String messsageAnchor = "";
        if (Strings.isNotBlank(contentAnchor)) {
            messsageAnchor = contentAnchor;
        }

        CtpAffair affair = summaryVO.getAffair();

        String policyName = permissionManager.getPermissionName(EnumNameEnum.col_flow_perm_policy.name(), affair.getNodePolicy(), affair.getOrgAccountId());
        mav.addObject("nodePolicyName", policyName);

        int superNodestatus = 0;
        if (AffairUtil.isSuperNode(affair)) {
            if (null != summaryVO.getActivityId() && summaryVO.getProcessId() != null) {
                superNodestatus = wapi.getSuperNodeStatus(summaryVO.getProcessId(), String.valueOf(summaryVO.getActivityId()));
            }
        }
        if (superNodestatus != 0) {
            //超级节点不要态度
            mav.addObject("nodeattitude", "3");
            summaryVO.setCanDeleteORarchive(false);
            summaryVO.setCancelOpinionPolicy(0);
            summaryVO.setDisAgreeOpinionPolicy(0);
        }

        // 查看子流程， 根据父流程来判断是否允许流程预测
        if ("true".equals(canExePrediction) && !summaryVO.isCanExePrediction()) {
            summaryVO.setCanExePrediction(true);
        }

        mav.addObject("moduleId", summaryVO.getSummary().getId());
        mav.addObject("sessionId", sessionId);
        mav.addObject("moduleType", ModuleType.collaboration.getKey());
        mav.addObject("MainbodyType", summaryVO.getAffair().getBodyType());
        mav.addObject("superNodestatus", superNodestatus);
        mav.addObject("contentAnchor", messsageAnchor);
        mav.addObject("nodeDesc", (String) request.getAttribute("nodeDesc"));//节点描述
        mav.addObject("signetProtectInput", request.getAttribute("signetProtectInput"));//签章默认保护全字段
        mav.addObject("summaryArchiveId", summaryVO.getSummary().getArchiveId());
        // mav.addObject("summaryCanEdit",summaryVO.getSummary().getCanEdit());


        //判断是否有新建会议权限
        boolean canCreateMeeting = user.hasResourceCode("F09_meetingArrange");
        mav.addObject("canCreateMeeting", canCreateMeeting);

        //判断是否存在流程追溯数据
        boolean showTraceWorkflows = false;
        //回退或者撤销记录不显示相关数据不查追溯
        boolean openFromRealStepRecord = ColOpenFrom.repealRecord.name().equals(openFrom) || ColOpenFrom.stepBackRecord.name().equals(openFrom);
        if (!openFromRealStepRecord) {
			/*	List<CtpAffair> traceWorkflows = traceWorkflowManager.getShowDataByParams(affair.getObjectId(), affair.getActivityId(), affair.getMemberId());
				if(Strings.isNotEmpty(traceWorkflows)){*/
            showTraceWorkflows = true;
            //}
        }
        mav.addObject("showTraceWorkflows", showTraceWorkflows);

        //判断是否有所
        boolean hasFormLock = false;
        //表单从待办列表打开需要加锁(后台加锁)
        if (ColUtil.isForm(summaryVO.getBodyType()) &&
                Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState()) &&
                ColOpenFrom.listPending.name().equals(openFrom)) {
            FormLockParam lockParam = new FormLockParam();
            lockParam.setAffairId(Long.valueOf(affairId));
            lockParam.setAffairState(affair.getState());
            lockParam.setFormAppId(affair.getFormAppId());
            lockParam.setFormRecordId(affair.getFormRecordid());
            lockParam.setNodePolicy(affair.getNodePolicy());
            lockParam.setRightId(summaryVO.getRightId());
            lockParam.setAffairReadOnly(AffairUtil.isFormReadonly(affair));
            LockObject lockObject = colManager.formAddLock(lockParam);
            if (LockObject.CANNOTSUBMIT.equals(lockObject.getCanSubmit())) {
                String alertLockMsg = ResourceUtil.getString("collaboration.common.flag.editingForm", lockObject.getLoginName(), lockObject.getFrom());
                mav.addObject("alertLockMsg", alertLockMsg);
                hasFormLock = true;
            }
            mav.addObject("realFormLock", lockObject.getRealLockForm());
            mav.addObject("isReadOnly", lockObject.getIsReadOnly());
        }
        mav.addObject("hasBarCodePlug", AppContext.hasPlugin("barCode"));
        mav.addObject("hasFormLock", hasFormLock);
        //记录查看次数
        if (AppContext.hasPlugin("ai")) {
            AIRemindEvent remindEvt = new AIRemindEvent(this);
            remindEvt.setAffairId(affair.getId());
            remindEvt.setObjectId(affair.getObjectId());
            remindEvt.setState(affair.getState());
            remindEvt.setSubject(affair.getSubject());
            remindEvt.setOpenFrom(openFrom);
            remindEvt.setTrack(affair.getTrack());
            remindEvt.setMemberId(affair.getMemberId());
            EventDispatcher.fireEventAfterCommit(remindEvt);
        }
//		AppContext.putRequestContext("LazyoutType",2);
        if (2 == Integer.parseInt(String.valueOf(AppContext.getRequestContext("LazyoutType")))) {
            mav.setViewName("apps/collaboration/briefSummary");
        }

//	mav.setViewName("apps/collaboration/briefSummary");
        mav.addObject("accountId", user.getLoginAccount());
        if (user.isAdministrator()) {
            mav.addObject("adminType", "accountAdmin");
        } else {
            mav.addObject("adminType", "formAdmin");
        }
        mav.addObject("formId", affair.getFormId());

        String officeOcxUploadMaxSize = SystemProperties.getInstance().getProperty("officeFile.maxSize");
        mav.addObject("officeOcxUploadMaxSize", Strings.isBlank(officeOcxUploadMaxSize) ? "8192" : officeOcxUploadMaxSize);

        return mav;
    }

    //再次确认态度
    public ModelAndView againNotarizeAttitude(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/attitudeDialog");
        return mav;
    }

    public ModelAndView repealDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/repealDialog");
        String affairId = request.getParameter("affairId");
        String objectId = request.getParameter("objectId");
        if (Strings.isNotBlank(affairId)) {
            CtpAffair ctpAffair = affairManager.get(Long.valueOf(affairId));
            if (null != ctpAffair && null != ctpAffair.getTempleteId()) {
                CtpTemplate ctpTemplate = templateManager.getCtpTemplate(ctpAffair.getTempleteId());
                mav.addObject("template", ctpTemplate.getCanTrackWorkflow());
            }
        }
        mav.addObject("affairId", affairId);
        mav.addObject("objectId", objectId);
        mav.addObject("fromBatch", request.getParameter("fromBatch"));
        return mav;
    }

    @SuppressWarnings("unchecked")
    private boolean checkHttpParamValid(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, String> wfdef = ParamUtil.getJsonDomain("workflow_definition");
        String workflowDataFlag = wfdef.get("workflow_data_flag");
        if (Strings.isBlank(workflowDataFlag) || "undefined".equals(workflowDataFlag.trim()) || "null".equals(workflowDataFlag.trim())) {
            PrintWriter out = null;
            try {
                out = response.getWriter();
                out.println("<script>");
                out.println("alert('" + StringEscapeUtils.escapeJavaScript("网络不稳定，从页面获取数据失败，请稍后重试！") + "');");
                out.println(" window.close();");
                out.println("</script>");
            } catch (Exception e) {
                LOG.error("", e);
            }

            Enumeration es = request.getHeaderNames();
            StringBuilder stringBuilder = new StringBuilder();
            if (es != null) {
                while (es.hasMoreElements()) {
                    Object name = es.nextElement();
                    String header = request.getHeader(name.toString());
                    stringBuilder.append(Strings.escapeJavascript(name + "") + ":=" + Strings.escapeJavascript(header) + ",");
                }
                LOG.warn("request header---" + stringBuilder.toString());
            }

            return false;
        }
        return true;
    }


    /**
     * 正常处理协同
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView finishWorkItem(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String viewAffairId = request.getParameter("affairId");
        Long affairId = Strings.isBlank(viewAffairId) ? 0l : Long.parseLong(viewAffairId);
        ColSummary summary = null;
        boolean isLock = false;
        CtpAffair affair = null;

        affair = affairManager.get(affairId);
        if (affair != null) {
            summary = colManager.getSummaryById(affair.getObjectId());
        }

        LOG.info(AppContext.currentUserName() + "处理协同,affairId:" + affairId);

        try {
            //前段参数完整性校验
            if (!checkHttpParamValid(request, response)) {
                return null;
            }

            //自由协同后台加流程提交锁，防止提交时前端js报错，未加锁
            Long templateId = null;
            if (summary != null) {
                templateId = summary.getTempleteId();
            } else if (affair != null) {
                templateId = affair.getTempleteId();
            }

            if (templateId == null || Long.valueOf(0).equals(templateId) || Long.valueOf(-1).equals(templateId)) {
                String[] result = wapi.lockWorkflowProcess(summary.getProcessId(), String.valueOf(AppContext.currentUserId()), 14, "", true);
                String checkLockMsg = "";
                if (null == result) {
                    checkLockMsg = ResourceUtil.getString("workflow.wapi.exception.msg002");
                } else if (String.valueOf(Boolean.FALSE).equals(result[0])) {
                    checkLockMsg = result[1];
                }

                if (Strings.isNotBlank(checkLockMsg)) {

                    LOG.info("自由协同获取锁报错:" + checkLockMsg + ",summary.getProcessId():" + summary.getProcessId());


                    Map<String, Object> ret = new HashMap<String, Object>();
                    ret.put("code", 1);
                    ret.put("message", Strings.escapeJavascript(checkLockMsg));
                    ret.put("closePage", "false");


                    ColUtil.webWriteMsg(response, JSONUtil.toJSONString(ret));
                    return null;
                }
            }

            isLock = colLockManager.canGetLock(affairId);
            if (!isLock) {
                LOG.error(AppContext.currentUserName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
                return null;
            }
            //待办校验
            if (affair == null || affair.getState() != StateEnum.col_pending.key()) {
                String msg = ColUtil.getErrorMsgByAffair(affair);
                if (Strings.isNotBlank(msg)) {
                    ColUtil.webAlertAndClose(response, msg);
                    return null;
                }
            }

            //检查代理，避免不是处理人也能处理了。
            boolean canDeal = ColUtil.checkAgent(affair, summary, true);
            if (!canDeal) {
                return null;
            }

            //处理参数
            Map<String, Object> params = new HashMap<String, Object>();

            //跟踪参数
            Map<String, String> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
            params.put("trackParam", trackPara);

            //取出模板信息
            Map<String, Object> templateMap = (Map<String, Object>) ParamUtil.getJsonDomain("colSummaryData");
            params.put("templateColSubject", templateMap.get("templateColSubject"));
            params.put("templateWorkflowId", templateMap.get("templateWorkflowId"));

            colManager.transFinishWorkItem(summary, affair, params);


            StringBuilder successJson = new StringBuilder();

            successJson.append("{");
            successJson.append("'code':0");
            successJson.append(",'message':'finishworkitem success!'");
            successJson.append("}");

            ColUtil.webWriteMsg(response, successJson.toString());

        } catch (BusinessException e) {

            LOG.error("协同提交报错", e);

            StringBuilder errorJson = new StringBuilder();

            errorJson.append("{");
            errorJson.append("'code':1");
            errorJson.append(",'message':'" + Strings.escapeJavascript(e.getMessage()) + "'");
            errorJson.append(",'closePage':'true'");
            errorJson.append("}");

            ColUtil.webWriteMsg(response, errorJson.toString());
            return null;

        } finally {

            if (isLock) {
                colLockManager.unlock(affairId);
            }
            if (summary != null) {
                colManager.colDelLock(summary, affair, true);
            }
        }
        return null;
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
        response.setContentType("text/html;charset=UTF-8");
        String viewAffairId = request.getParameter("affairId");
        Long affairId = Long.parseLong(viewAffairId);
        CtpAffair affair = affairManager.get(affairId);
        if (affair == null || affair.getState() != StateEnum.col_pending.key()) {
            String msg = ColUtil.getErrorMsgByAffair(affair);
            if (Strings.isNotBlank(msg)) {
                PrintWriter out = response.getWriter();
                out.println("<script>");
                out.println("alert('" + StringEscapeUtils.escapeJavaScript(msg) + "');");
                out.println(" window.close();");
                out.println("</script>");
                return null;
            }
        }

        if (affair != null) {
            ColSummary summary = colManager.getColSummaryById(affair.getObjectId());

            //检查代理，避免不是处理人也能处理了。
            boolean canDeal = ColUtil.checkAgent(affair, summary, true);
            if (!canDeal) {
                return null;
            }

            //处理参数
            Map<String, Object> params = new HashMap<String, Object>();

            //跟踪参数
            Map<String, String> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
            params.put("trackParam", trackPara);

            //取出模板信息
            Map<String, Object> templateMap = (Map<String, Object>) ParamUtil.getJsonDomain("colSummaryData");
            params.put("templateColSubject", templateMap.get("templateColSubject"));
            params.put("templateWorkflowId", templateMap.get("templateWorkflowId"));

            boolean isLock = false;
            try {
                isLock = colLockManager.canGetLock(affairId);
                if (!isLock) {
                    LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作doZCDB,affairId" + affairId);
                    return null;
                }
                colManager.transDoZcdb(summary, affair, params);
            } finally {
                if (isLock) {
                    colLockManager.unlock(affairId);
                }
                colManager.colDelLock(summary, affair);
            }
        }

        return null;
    }

    public ModelAndView doForward(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Map para = ParamUtil.getJsonDomain("MainData");

        String data = (String) para.get("data");

        String[] ds = data.split("[,]");
        for (String d1 : ds) {
            if (Strings.isBlank(d1)) {
                continue;
            }

            String[] d1s = d1.split("[_]");

            long summaryId = Long.parseLong(d1s[0]);
            long affairId = Long.parseLong(d1s[1]);

            colManager.transDoForward(user, summaryId, affairId, para);
        }

        return null;
    }

    public ModelAndView chooseOperation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView("apps/collaboration/isignaturehtml/chooseOperation");
        return mv;
    }

    public ModelAndView showForward(HttpServletRequest request, HttpServletResponse response) throws Exception {
        NodePolicyVO newColNodePolicy = colManager.getNewColNodePolicy(AppContext.currentAccountId());
        request.setAttribute("newColNodePolicy", newColNodePolicy);
        return new ModelAndView("apps/collaboration/forward");

    }

    /**
     * 终止流程
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView stepStop(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String affairId = request.getParameter("affairId");
        Map<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("affairId", affairId);

        boolean isLock = false;
        try {
            isLock = colLockManager.canGetLock(Long.valueOf(affairId));
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作stepStop,affairId" + affairId);
                return null;
            }
            //跟踪参数
            Map<String, String> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
            tempMap.put("trackParam", trackPara);

            //取出模板信息
            Map<String, Object> templateMap = (Map<String, Object>) ParamUtil.getJsonDomain("colSummaryData");
            tempMap.put("templateColSubject", templateMap.get("templateColSubject"));
            tempMap.put("templateWorkflowId", templateMap.get("templateWorkflowId"));
            colManager.transStepStop(tempMap);
        } finally {
            if (isLock) {
                colLockManager.unlock(Long.valueOf(affairId));
            }
            colManager.colDelLock(Long.valueOf(affairId));
        }
       /* PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("window.parent.$('#summary').attr('src','');"); //刷新iframe的父页面
        out.println("window.parent.$('.slideDownBtn').trigger('click');");
        out.println("window.parent.$('#listPending').ajaxgridLoad();");
        out.println("</script>");
        out.close();*/
        return null;
    }

    /**
     * 回退流程
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView stepBack(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String affairId = request.getParameter("affairId");
        String summaryId = request.getParameter("summaryId");
        Map<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("affairId", affairId);
        tempMap.put("summaryId", summaryId);
        tempMap.put("targetNodeId", "");//TODO 暂时不支持回退到指定节点
        //取出模板信息
        Map<String, Object> templateMap = (Map<String, Object>) ParamUtil.getJsonDomain("colSummaryData");
        tempMap.put("templateColSubject", templateMap.get("templateColSubject"));
        tempMap.put("templateWorkflowId", templateMap.get("templateWorkflowId"));
        boolean isLock = false;
        try {
            isLock = colLockManager.canGetLock(Long.valueOf(affairId));
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作stepBack,affairId:" + affairId);
                return null;
            }
            String msg = colManager.transStepBack(tempMap);
            if (Strings.isNotBlank(msg)) {
                ColUtil.webAlertAndClose(response, msg);
                return null;
            }
        } finally {
            if (isLock) {
                colLockManager.unlock(Long.valueOf(affairId));
            }
            colManager.colDelLock(Long.valueOf(affairId));
        }
    	/*PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("window.parent.$('#summary').attr('src','');"); //刷新iframe的父页面
        out.println("window.parent.$('.slideDownBtn').trigger('click');");
        out.println("window.parent.$('#listPending').ajaxgridLoad();");
        out.println("</script>");
        out.close();*/
        return null;
    }

    /**
     * 指定回退
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView updateAppointStepBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String workitemId = request.getParameter("workitemId");
        String processId = request.getParameter("processId");
        String caseId = request.getParameter("caseId");
        String activityId = request.getParameter("activityId");
        String theStepBackNodeId = request.getParameter("theStepBackNodeId");
        String submitStyle = request.getParameter("submitStyle");
        String summaryId = request.getParameter("summaryId");
        String affairId = request.getParameter("affairId");
        String isCircleBack = request.getParameter("isCircleBack");

        Map<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("workitemId", workitemId);
        tempMap.put("processId", processId);
        tempMap.put("caseId", caseId);
        tempMap.put("activityId", activityId);
        tempMap.put("theStepBackNodeId", theStepBackNodeId);
        tempMap.put("submitStyle", submitStyle);
        tempMap.put("affairId", affairId);
        tempMap.put("summaryId", summaryId);
        tempMap.put("isCircleBack", isCircleBack);


        User user = AppContext.getCurrentUser();
        boolean isLock = false;

        Long affairIdLong = Long.parseLong(affairId);
        ColSummary summary = null;
        try {
            isLock = colLockManager.canGetLock(affairIdLong);
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
                return null;
            }


            CtpAffair currentAffair = affairManager.get(Long.parseLong(affairId));
            //待办校验
            if (currentAffair == null || currentAffair.getState() != StateEnum.col_pending.key()) {
                String msg = ColUtil.getErrorMsgByAffair(currentAffair);
                if (Strings.isNotBlank(msg)) {
                    ColUtil.webAlertAndClose(response, msg);
                    return null;
                }
            }
            //是否已经指定回退了
            if (Integer.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(currentAffair.getSubState())
                    || Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(currentAffair.getSubState())) {
                LOG.error(AppContext.currentAccountName() + "重复指定回退,affairId" + affairId);
                return null;
            }

            summary = colManager.getColSummaryById(Long.parseLong(summaryId));

            // 处理意见
            Comment comment = ContentUtil.getCommnetFromRequest(ContentUtil.OperationType.appointStepBack, currentAffair.getMemberId(),
                    currentAffair.getObjectId());
            comment.setModuleId(summary.getId());
            comment.setCreateDate(new Timestamp(System.currentTimeMillis()));
            if (!user.getId().equals(currentAffair.getMemberId())) {
                comment.setExtAtt2(user.getName());
            }
            comment.setCreateId(currentAffair.getMemberId());
            comment.setExtAtt3("collaboration.dealAttitude.rollback");
            comment.setModuleType(ModuleType.collaboration.getKey());
            comment.setPid(0L);


            tempMap.put("affair", currentAffair);
            tempMap.put("summary", summary);
            tempMap.put("comment", comment);
            tempMap.put("user", user);

            //跟踪参数
            Map<String, String> trackPara = ParamUtil.getJsonDomain("trackDiv_detail");
            tempMap.put("trackParam", trackPara);

            //取出模板信息
            Map<String, Object> templateMap = (Map<String, Object>) ParamUtil.getJsonDomain("colSummaryData");
            tempMap.put("templateColSubject", templateMap.get("templateColSubject"));
            tempMap.put("templateWorkflowId", templateMap.get("templateWorkflowId"));

            colManager.updateAppointStepBack(tempMap);

          /*  PrintWriter out = response.getWriter();
            out.println("<script>");
            out.println("window.parent.$('#summary').attr('src','');"); //刷新iframe的父页面
            out.println("window.parent.$('.slideDownBtn').trigger('click');");
            out.println("window.parent.$('#listPending').ajaxgridLoad();");
            out.println("</script>");
            out.close();*/
        } finally {

            if (isLock) {
                colLockManager.unlock(affairIdLong);
            }
            if (summary != null) {
                wapi.releaseWorkFlowProcessLock(summary.getProcessId(), user.getId().toString(), 14);// 解除回退锁
            }
        }

        return null;
    }

    /**
     * 撤销回退记录列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listRecord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("common/supervise/superviseDetail/recordDetailList");
        String recordType = request.getParameter("record");
        String showPigonHoleBtn = request.getParameter("showPigonHoleBtn");
        String hasDumpData = request.getParameter("hasDumpData");
        String srcFrom = request.getParameter("srcFrom");
        modelAndView.addObject("recordType", recordType);
        modelAndView.addObject("showPigonHoleBtn", showPigonHoleBtn);
        modelAndView.addObject("hasDumpData", hasDumpData);
        modelAndView.addObject("openFrom", "listDone");
        modelAndView.addObject("srcFrom", srcFrom);
        modelAndView.addObject("hasAIPlugin", AppContext.hasPlugin("ai"));
        modelAndView.addObject("paramTemplateIds", request.getParameter("paramTemplateIds"));
        return modelAndView;
    }

    /**
     * 撤销流程
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView repeal(HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        String summaryId = request.getParameter("summaryId");
        String affairId = request.getParameter("affairId");
        Map<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("summaryId", summaryId);
        tempMap.put("affairId", affairId);
        tempMap.put("repealComment", request.getParameter("repealComment"));
        tempMap.put("isWFTrace", 1);
        Long laffairId = Long.valueOf(affairId);
        try {
            colManager.transRepal(tempMap);
        } finally {
            colManager.colDelLock(laffairId, true);
        }
      /*  PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("window.parent.$('#summary').attr('src','');"); //刷新iframe的父页面
        out.println("window.parent.$('.slideDownBtn').trigger('click');");
        out.println("window.parent.$('#listPending').ajaxgridLoad();");
        out.println("</script>");
        out.close();*/
        return null;
    }


    /**
     * 查看属性设置
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ModelAndView getAttributeSettingInfo(HttpServletRequest request,
                                                HttpServletResponse response) throws Exception {
        //定义跳转目标路径
        ModelAndView mav = new ModelAndView("apps/collaboration/showAttributeSetting");
        String affairId = request.getParameter("affairId");
        String isHistoryFlag = request.getParameter("isHistoryFlag");
        Map args = new HashMap();
        args.put("affairId", affairId);
        args.put("isHistoryFlag", isHistoryFlag);
        Map map = colManager.getAttributeSettingInfo(args);

        mav.addObject("remindIntervalTitle", map.get("remindIntervalTitle"));
        //OA-91845要求显示全路径
        map.put("archiveName", map.get("archiveAllName"));
        request.setAttribute("ffattribute", map);
        mav.addObject("processDeadlineIconTipLabel", map.get("processDeadlineIconTipLabel"));

        //协同归档全路径
        mav.addObject("archiveAllName", map.get("archiveAllName"));
        //附件归档全路径
        mav.addObject("attachmentArchiveName", map.get("attachmentArchiveName"));
        //督办属性设置 标志位
        mav.addObject("supervise", map.get("supervise"));
        //流程超期时显示
        mav.addObject("processTermTypeName", map.get("processTermTypeName"));
        //消息规则
        mav.addObject("messageRuleList", map.get("messageRule"));

        mav.addObject("canDueReminder", map.get("canDueReminder"));

        mav.addObject("openFrom", request.getParameter("openFrom"));

        mav.addObject("hasWorkflowAdvanced", AppContext.hasPlugin("workflowAdvanced"));

        return mav;
    }

    /**
     * 显示取回确认页面
     */
    public ModelAndView showTakebackConfirm(HttpServletRequest request,
                                            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/takebackConfirm");
        return mav;
    }

    /**
     * 显示撤销流程确认页面
     */
    public ModelAndView showRepealCommentDialog(HttpServletRequest request,
                                                HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("common/workflowmanage/repealCommentDialog");
        return mav;
    }


    /**
     * 流程分类
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showPortalCatagory(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/showPortalCatagory");
        request.setAttribute("openFrom", request.getParameter("openFrom"));
        String category = ReqUtil.getString(request, "category", "");
        if (Strings.isNotBlank((category))) {
            mav = new ModelAndView("apps/collaboration/showPortalCatagory4MyTemplate");
        }
        return mav;
    }

    /**
     * portal显示重要程度的页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showPortalImportLevel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/showPortalImportLevel");
        List<CtpEnumItem> secretLevelItems = enumManagerNew.getEnumItems(EnumNameEnum.edoc_urgent_level);

        ColUtil.putImportantI18n2Session();


        mav.addObject("itemCount", secretLevelItems.size());
        return mav;
    }


    /**
     * 协同待办列表、Portal待办栏目->更多：点击“批处理”的页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
//    public ModelAndView batchDeal(HttpServletRequest request,HttpServletResponse response) throws Exception{
//        ModelAndView mav = new ModelAndView("apps/collaboration/batchDeal");
//        return mav;
//    }

    /**
     * 处理协同，选择了不同意
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView disagreeDeal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/disagreeDeal");
        return mav;
    }

    /*
     * 协同转换为邮件
     */
    public ModelAndView forwordMail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map query = new HashMap();
        query.put("summaryId", Long.parseLong(request.getParameter("summaryId")));
        query.put("affairId", Long.parseLong(request.getParameter("affairId")));
        query.put("formContent", String.valueOf(request.getParameter("formContent")));
        ModelAndView mv = this.colManager.getforwordMail(query);
        return mv;
    }

    /**
     * 存为个人模板的跳转页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveAsTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/saveAsTemplate");
        String hasWorkflow = request.getParameter("hasWorkflow");
        String subject = request.getParameter("subject");
        String tembodyType = request.getParameter("tembodyType");
        String formtitle = request.getParameter("formtitle");
        String defaultValue = request.getParameter("defaultValue");
        String ctype = request.getParameter("ctype");
        String temType = request.getParameter("temType");
        if ("hasnotTemplate".equals(temType)) {
            mav.addObject("canSelectType", "all");
        } else if ("template".equals(temType)) {
            mav.addObject("canSelectType", "template");
        } else if ("workflow".equals(temType)) {
            mav.addObject("canSelectType", "workflow");
        } else if ("text".equals(temType)) {
            mav.addObject("canSelectType", "text");
        }
        if (Strings.isNotBlank(ctype)) {
            int n = Integer.parseInt(ctype);
            if (n == 20) {
                mav.addObject("onlyTemplate", Boolean.TRUE);
            }
        }
        mav.addObject("hasWorkflow", hasWorkflow);
        mav.addObject("subject", subject);
        mav.addObject("tembodyType", tembodyType);
        mav.addObject("formtitle", formtitle);
        mav.addObject("defaultValue", defaultValue);
        return mav;
    }

    /**
     * 归档协同查看页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return this.summary(request, response);
    }


    public ModelAndView updateContentPage(HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/updateContentPage");
        String summaryId = request.getParameter("summaryId");
        ContentViewRet context = ContentUtil.contentView(ModuleType.collaboration, Long.parseLong(summaryId), null,
                CtpContentAllBean.viewState__editable, null);
        return mav;

    }


    public MainbodyManager getCtpMainbodyManager() {
        return ctpMainbodyManager;
    }

    public void setCtpMainbodyManager(MainbodyManager ctpMainbodyManager) {
        this.ctpMainbodyManager = ctpMainbodyManager;
    }

    /**
     * yangwulin 2012-12-21 Sprint5 处理协同 - 存为草稿
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView doDraftOpinion(HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        long summaryId = Long.parseLong(request.getParameter("summaryId"));
        long affairId = Long.valueOf(request.getParameter("affairId"));
        //先保存意见
        this.colManager.saveOpinionDraft(affairId, summaryId);
        return null;
    }

    /**
     * 统计查询协同部分的穿透，列表+详细列表
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView statisticSearch(HttpServletRequest request,
                                        HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/colStatisticSearch");

        String bodyType = request.getParameter("bodyType");
        String collType = request.getParameter("CollType");
        String templateId = request.getParameter("templateId");
        String startTime = request.getParameter("start_time");
        String endTime = request.getParameter("end_time");
        String status = request.getParameter("state");
        String userId = request.getParameter("user_id");
        String coverTime = request.getParameter("coverTime");
        String isGroup = request.getParameter("isGroup");

        FlipInfo fi = new FlipInfo();
        Map<String, String> param = getStatisticSearchCondition(fi, request);
        //判断是否团队报表，如果是团队报表时，只能查看，不能操作协同
        mav.addObject("isTeamReport", param.get("isTeamReport"));
        request.setAttribute("fflistStatistic", colManager.getStatisticSearchCols(fi, param));

        mav.addObject("bodyType", bodyType);
        mav.addObject("CollType", collType);
        mav.addObject("templateId", templateId);
        mav.addObject("start_time", startTime);
        mav.addObject("end_time", endTime);
        mav.addObject("state", status);
        mav.addObject("user_id", userId);
        mav.addObject("coverTime", coverTime);
        mav.addObject("isGroup", isGroup);

        return mav;
    }

    /**
     * 列表界面-详细界面打开跟踪设置窗口
     */
    public ModelAndView openTrackDetail(HttpServletRequest request,
                                        HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/trackDetail");
        String objectId = request.getParameter("objectId");
        String affairId = request.getParameter("affairId");

        ColSummary summary = colManager.getColSummaryById(Long.valueOf(objectId));
        CtpAffair affair = affairManager.get(Long.valueOf(affairId));
        int trackType = affair.getTrack();//跟踪类型
        Long startMemberId = summary.getStartMemberId();//发起者ID
        int state = summary.getState();//事务状态
        if (trackType == 2) {//指定跟踪人的时候,查询回显数据
            List<CtpTrackMember> trackList = trackManager.getTrackMembers(Long.valueOf(affairId));
            String zdgzrStr = "";
            StringBuilder sb = new StringBuilder();
            for (int a = 0, j = trackList.size(); a < j; a++) {
                CtpTrackMember cm = trackList.get(a);
                sb.append("Member|" + cm.getTrackMemberId() + ",");
            }
            zdgzrStr = sb.toString();
            if (Strings.isNotBlank(zdgzrStr)) {
                mav.addObject("zdgzrStr", zdgzrStr.substring(0, zdgzrStr.length() - 1));
            }
        }
        mav.addObject("objectId", objectId);
        mav.addObject("affairId", affairId);
        mav.addObject("trackType", trackType);
        mav.addObject("state", state);
        mav.addObject("startMemberId", startMemberId);
        return mav;

    }

    /**
     * 绩效管理-统计查询-显示协同列表
     * 待办：协同接收时间
     * 已办：处理时间
     * 已发：发起时间
     * 已归档：归档时间
     * 暂存待办：更新时间
     *
     * @param user_id    查询人员id，如果传递为空，则查询当前登录人员id
     * @param bodyType   应用类型：协同:10、表单:20、协同和表单时不传值
     * @param collType   协同 类型时：Templete(模板协同),Self(自由协同)，任意时不传值
     * @param templateId 表单类型时：表单模板id,多个模板时逗号分隔
     * @param state      0:暂存待办；1:已归档；2:已发；3:待办；4:已办；（多种状态时以逗号分隔）
     * @param coverTime  0：未超期；1:超期；全部时不传值
     * @param start_time 指定期限的开始时间
     * @param end_time   指定期限的结束时间
     * @return
     */
    private Map<String, String> getStatisticSearchCondition(FlipInfo fi, HttpServletRequest request) {
        Map<String, String> query = new HashMap<String, String>();
        User user = AppContext.getCurrentUser();
        if (user == null) {
            return query;
        }
        //类型：协同:10、表单:20
        String bodyType = request.getParameter("bodyType");
        if (Strings.isNotBlank(bodyType)) {
            query.put(ColQueryCondition.bodyType.name(), bodyType);
        }
        //协同 模板：自由流程、协同模板-Templete(模板协同),Self(自由协同)
        String collType = request.getParameter("CollType");
        if (Strings.isNotBlank(collType)) {
            query.put(ColQueryCondition.CollType.name(), collType);
        }
        //模板ID
        String templateId = request.getParameter("templateId");
        if (Strings.isNotBlank(templateId) && !"null".equals(templateId)) {
            query.put(ColQueryCondition.templeteIds.name(), templateId);
        }
        //状态
        String state = request.getParameter("state");
        List<Integer> states = new ArrayList<Integer>();
        if (Strings.isNotBlank(state) && !"null".equals(state)) {
            String[] stateStrs = state.split(",");
            for (String s : stateStrs) {
                states.add(Integer.valueOf(s));
            }
            //已归档
            if (states.contains(1)) {
                query.put(ColQueryCondition.archiveId.name(), "archived");//"archiveId" 传入值不为空即可
                states.remove(states.indexOf(1));
            }
            //暂存待办
            if (states.contains(0)) {
                query.put(ColQueryCondition.subState.name(), String.valueOf(SubStateEnum.col_pending_ZCDB.getKey()));//协同-待办-暂存待办
                states.remove(states.indexOf(0));
            }
            if (states.size() > 0) {
                state = Functions.join(states, ",");
                query.put(ColQueryCondition.state.name(), state);
            }
        }
        //时间：时间段
        String startTime = request.getParameter("start_time");
        String endTime = request.getParameter("end_time");
        String queryTime = "";
        if (Strings.isEmpty(startTime) && Strings.isEmpty(endTime)) {
            queryTime = null;
        } else {
            queryTime = startTime + "#" + endTime;
        }
        if (Strings.isNotBlank(queryTime)) {
            if (states.size() == 1) {
                //如果是已发 ，则按照按创建日期查询
                if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(states.get(0))) {
                    query.put(ColQueryCondition.createDate.name(), queryTime);
                } else if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(states.get(0))) {//待办 按照接收时间
                    query.put(ColQueryCondition.receiveDate.name(), queryTime);
                } else if (Integer.valueOf(StateEnum.col_done.getKey()).equals(states.get(0))) {//已办按完成时间查询
                    query.put(ColQueryCondition.completeDate.name(), queryTime);
                }
            } else if (String.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(query.get(ColQueryCondition.subState.name()))) {
                //如果是暂存待办 则按照修改时间 查询
                query.put(ColQueryCondition.updateDate.name(), queryTime);
            }
            //如果是归档 ，传人的时间时不在dao层方法用，只是用来在返回的结果集中进行过滤，因为dao层方法为公共方法，没有管理文档中心的表进行联查
            //如果是多种状态时，时间不统一，(例如：已发按照创建日期、待办按照接收时间),单独处理
            if ("archived".equals(query.get(ColQueryCondition.archiveId.name())) || states.size() > 1) {
                query.put("statisticDate", queryTime);
            }
        }
        //是否超期：超期、未超期 1:超期；0：未超期；2：全部
        String coverTime = request.getParameter("coverTime");
        if (Strings.isNotBlank(coverTime)) {
            query.put(ColQueryCondition.coverTime.name(), coverTime);
        }
        //人员范围：管理范围内选择
        String userId = request.getParameter("user_id");
        if (Strings.isNotBlank(userId)) {
            query.put(ColQueryCondition.currentUser.name(), userId);
        }

        query.put("statistic", "true");//统计标示
        //判断是否团队报表   "1"表示团队报表
        String isGroup = request.getParameter("isGroup");
        if (Strings.isNotBlank(isGroup)) {
            query.put("isTeamReport", isGroup);
        }
        fi.setParams(query);
        return query;
    }

    /**
     * 根据summaryId查询附件列表信息
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView findAttachmentListBuSummaryId(HttpServletRequest request,
                                                      HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView("apps/collaboration/attachmentList");
        String summaryId = request.getParameter("summaryId");
        String affairId = request.getParameter("affairId");

        CtpAffair affair = affairManager.get(Long.valueOf(affairId));
        if (affair == null) {
            affair = affairManager.getByHis(Long.valueOf(affairId));
        }
        ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
        User user = AppContext.getCurrentUser();
        String memberId = String.valueOf(affair.getMemberId());


        if (!SecurityCheck.isLicit(AppContext.getRawRequest(), AppContext.getRawResponse(), ApplicationCategoryEnum.collaboration,
                user, affair.getId(), affair, summary.getArchiveId(), false)) {
            //记录非法访问日志及
            SecurityCheck.printInbreakTrace(AppContext.getRawRequest(), AppContext.getRawResponse(),
                    user, ApplicationCategoryEnum.collaboration);
            return null;
        }


        //附件
        String openFrom = request.getParameter("openFromList");
        if (!ColOpenFrom.supervise.name().equals(openFrom) && Strings.isNotBlank(memberId)) {
            AppContext.putThreadContext(Comment.THREAD_CTX_DOCUMENT_AFFAIR_MEMBER_ID, Long.valueOf(memberId));
        }
        List<AttachmentVO> attachmentVOs = colManager.getAttachmentListBySummaryId(Long.valueOf(summaryId), Long.valueOf(memberId));
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
        mv.addObject("isHistoryFlag", request.getParameter("isHistoryFlag"));
        return mv;
    }

    /**
     * 绩效报表穿透查询列表导出excel
     *
     * @param request
     * @param response
     * @return ModelAndView mav
     * @author
     */
    public ModelAndView exportColSummaryExcel(HttpServletRequest request, HttpServletResponse response) {
        List<ColSummaryVO> colList = new ArrayList<ColSummaryVO>();
        FlipInfo fi = new FlipInfo();

        try {
            Map<String, String> param = getStatisticSearchCondition(fi, request);
            colList = this.colManager.exportDetaileExcel(null, param);
            //标题
            String subject = ResourceUtil.getString("common.subject.label");
            //发起人
            String sendUser = ResourceUtil.getString("cannel.display.column.sendUser.label");
            //发起时间
            String sendtime = ResourceUtil.getString("common.date.sendtime.label");
            //接收时间
            String receiveTime = ResourceUtil.getString("cannel.display.column.receiveTime.label");
            //处理时间
            String donedate = ResourceUtil.getString("common.date.donedate.label");
            //处理期限
            String deadlineDate = ResourceUtil.getString("pending.deadlineDate.label");
            //跟踪状态
            String track = ResourceUtil.getString("collaboration.track.state");
            //流程日志(暂时不导出Excel出来)
            //String process = ResourceUtil.getString("processLog.list.title.label") ;
            String[] columnName = {subject, sendUser, sendtime, receiveTime, donedate, deadlineDate, track};

            DataRecord dataRecord = new DataRecord();
            // 设置Excel标题名(绩效报表穿透查询列表)
            String excelName = ResourceUtil.getString("report.onelinework.through");
            dataRecord.setTitle(excelName);
            dataRecord.setSheetName(excelName);
            dataRecord.setColumnName(columnName);

            for (int i = 0; i < colList.size(); i++) {
                ColSummaryVO data = colList.get(i);
                DataRow dataRow = new DataRow();
                dataRow.addDataCell(data.getSubject(), DataCell.DATA_TYPE_TEXT);
                dataRow.addDataCell(data.getStartMemberName(), DataCell.DATA_TYPE_TEXT);
                dataRow.addDataCell(data.getStartDate() != null ? Datetimes.format(data.getStartDate(), Datetimes.datetimeWithoutSecondStyle).toString() : "-", DataCell.DATA_TYPE_DATE);
                dataRow.addDataCell(data.getReceiveTime() != null ? Datetimes.format(data.getReceiveTime(), Datetimes.datetimeWithoutSecondStyle).toString() : "-", DataCell.DATA_TYPE_DATE);
                dataRow.addDataCell(data.getDealTime() != null ? Datetimes.format(data.getDealTime(), Datetimes.datetimeWithoutSecondStyle).toString() : "-", DataCell.DATA_TYPE_DATE);
                dataRow.addDataCell(data.getDeadLineDateName(), DataCell.DATA_TYPE_TEXT);
                dataRow.addDataCell(data.getTrack() == 1 ? ResourceUtil.getString("message.yes.js") : ResourceUtil.getString("message.no.js"), DataCell.DATA_TYPE_TEXT);
                dataRecord.addDataRow(dataRow);
            }
            fileToExcelManager.save(response, dataRecord.getTitle(), dataRecord);
        } catch (Exception e) {
            LOG.error("为用户绩效报表穿透查询列表时出现异常:", e);
        }
        return null;
    }

    /**
     * 跳转到组合查询页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView combinedQuery(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/col_com_query");
        if ("templeteAll".equals(request.getParameter("condition")) && "all".equals(request.getParameter("textfield"))) {
            modelAndView.addObject("condition1", "1");
        }
        if ("bizcofnig".equals(request.getParameter("srcFrom"))) {
            modelAndView.addObject("condition2", "1");
        }
        if ("1".equals(request.getParameter("bisnissMap"))) {
            modelAndView.addObject("condition3", "1");
        }
        if ("templeteCategorys".equals(request.getParameter("condition"))) {
            modelAndView.addObject("condition4", "1");
        }
        modelAndView.addObject("aiProcessing", request.getParameter("aiProcessing"));
        modelAndView.addObject("aiSort", request.getParameter("aiSort"));
        modelAndView.addObject("openForm", request.getParameter("openForm"));
        modelAndView.addObject("dataType", request.getParameter("dataType"));
        modelAndView.addObject("hasDoc", AppContext.hasPlugin("doc"));
        return modelAndView;
    }

    /**
     * 转办
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView colTansfer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        Map<String, String> params = new HashMap<String, String>();
        String affairId = request.getParameter("affairId");
        params.put("affairId", affairId);
        params.put("transferMemberId", request.getParameter("transferMemberId"));
        boolean isLock = false;
        try {
            isLock = colLockManager.canGetLock(Long.valueOf(affairId));
            if (!isLock) {
                LOG.error(AppContext.currentAccountName() + "不能获取到map缓存锁，不能执行操作finishWorkItem,affairId" + affairId);
                return null;
            }

            String message = this.colManager.transColTransfer(params);
            modelAndView.addObject("message", message);
        } finally {
            if (isLock) {
                colLockManager.unlock(Long.valueOf(affairId));
            }

        }

        return null; //没有view，暂时返回空，不然404
    }

    /**
     * office页签
     */
    public ModelAndView tabOffice(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/tabOffice");
        //获取国际化js文件名
        Locale locale = AppContext.getLocale();
        String localeStr = "zh-cn";
        if (locale.equals(Locale.ENGLISH)) {
            localeStr = "en";
        } else if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
            localeStr = "zh-tw";
        }
        modelAndView.addObject("localeStr", localeStr);
        return modelAndView;
    }


    public ModelAndView showNodeMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        ModelAndView mav = new ModelAndView("apps/collaboration/showNodeMembers");


        String nodeId = request.getParameter("nodeId");
        String summaryId = request.getParameter("summaryId");

        List<CtpAffair> affairs = affairManager.getAffairsByObjectIdAndNodeId(Long.valueOf(summaryId), Long.valueOf(nodeId));

        List<Object[]> node2Affairs = new ArrayList<Object[]>();
        if (Strings.isNotEmpty(affairs)) {
            for (Iterator<CtpAffair> it = affairs.iterator(); it.hasNext(); ) {
                CtpAffair affair = it.next();
                if (!affairManager.isAffairValid(affair, true)) {
                    it.remove();
                }
            }

            for (CtpAffair a : affairs) {
                if (a.getActivityId() == null ||
                        (a.getState() != StateEnum.col_done.getKey()
                                && a.getState() != StateEnum.col_pending.getKey())) {
                    continue;
                }

                Object[] o = new Object[5];
                o[0] = a.getMemberId();
                o[1] = Functions.showMemberName(a.getMemberId());
                o[2] = a.getState();
                o[3] = a.getSubState();
                o[4] = a.getBackFromId();
                node2Affairs.add(o);
            }
        }

        mav.addObject("commentPushMessageToMembersList", JSONUtil.toJSONString(node2Affairs));
        mav.addObject("readSwitch", systemConfig.get(IConfigPublicKey.READ_STATE_ENABLE));
        return mav;
    }

    /**
     * 缓存大数据
     * 由于V3xShareMap缓存方式，get后数据就被清空，所以不用判断是否存在，直接存储
     */
    public ModelAndView cashTransData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String cashId = String.valueOf(UUIDLong.longUUID());

        Map<String, String> paramMap = new HashMap<String, String>();

        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            paramMap.put(name, request.getParameter(name));
        }
        V3xShareMap.put(cashId, paramMap);

        response.getWriter().write(cashId);
        return null;
    }

    /**
     * 缓存相关数据的条件到报表
     * Long drPoId, Long affairId, Long recordId,
     */
    public ModelAndView cacheUserCondition2Report(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 拼装接收到的参数
        Long drPoId = ReqUtil.getLong(request, "drPoId");
        Long affairId = ReqUtil.getLong(request, "affairId");
        String recordStrId = ReqUtil.getString(request, "recordId");
        String dataTypeName = ReqUtil.getString(request, "dataTypeName");
        Long recordId = null;
        if (Strings.isNotBlank(recordStrId)) {
            recordId = Long.parseLong(recordStrId);
        }
        String pageCondJSON = request.getParameter("pageCondJSON");
        List<Map<String, Object>> userConditionList = dataRelationApi.findFormMoreCond(drPoId, affairId, recordId, dataTypeName, pageCondJSON).getLeft();

        //将相关数据的查询条件缓存到报表,根据conditionId报表更多页面取数据回填
        String conditionId = reportResultApi.setUserCondition(UserConModel.ADVANCE, userConditionList).toString();
        response.getWriter().write(conditionId);
        return null;
    }

    public ModelAndView collaborationSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        ModelAndView modelAndView = new ModelAndView("apps/collaboration/collaborationSet");
        String anyReply = systemConfig.get("anyReply_enable");
        if (Strings.isBlank(anyReply)) {
            anyReply = "disable";
        }
        modelAndView.addObject("anyReply", anyReply);
        return modelAndView;
    }


    public ModelAndView updateCollaborationSwitch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map para = ParamUtil.getJsonDomain("submitform");
        String anyReply = (String) para.get("anyReply");
        systemConfig.update("anyReply_enable", anyReply);
//    	response.setContentType("text/html;charset=UTF-8");
//        PrintWriter out = response.getWriter();
//        out.println("<script type='text/javascript'>");
//        out.println("$.alert('"+ResourceUtil.getString("coll.switch.lable.setSuccess")+"')");
//        out.println("</script>");
//        out.flush();
        return null;
    }

    /**
     * 是否查询追溯数据并返回是否存在追溯数据
     *
     * @param subState 状态
     * @param affair   事项对象
     * @return
     * @throws BusinessException
     */
    private boolean showTraceWorkflows(String subState, CtpAffair affair) throws BusinessException {
        if (Strings.isEmpty(subState) || affair == null) {
            return false;
        }
        //被回退，被撤销，被指定回退且选择流程重走，查询是否存在追溯数据，返回前台控制是否展示追溯数据区域
        int intSubState = Integer.parseInt(subState);
        if (SubStateEnum.col_waitSend_stepBack.key() == intSubState || SubStateEnum.col_waitSend_cancel.key() == intSubState ||
                SubStateEnum.col_pending_specialBackToSenderCancel.key() == intSubState) {
            List<CtpAffair> traceWorkflows = traceWorkflowManager.getShowDataByParams(affair.getObjectId(), affair.getActivityId(), affair.getMemberId());
            if (Strings.isNotEmpty(traceWorkflows)) {
                return true;
            }
        }
        return false;
    }

    /**
     * BPM回收站
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listRecycle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/listRecycle");
        FlipInfo flipInfo = new FlipInfo();
        request.setAttribute("fflistRecycle", affairManager.getAffairByConditions(flipInfo, new HashMap<String, Object>()));
        return mav;
    }

    /**
     * 真正的打印界面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView batchPrintPdf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String affairIdsStr = request.getParameter("affairIds");
        String type = request.getParameter("type");
        List<CtpAffair> ctpAffairList = null;
        if (Strings.isNotBlank(affairIdsStr)) {
            String[] affairIds = affairIdsStr.split(",");
            List<Long> listAffair = new ArrayList<Long>();
            if (affairIds.length > 0) {
                for (String str : affairIds) {
                    listAffair.add(Long.valueOf(str));
                }
                Map<String, Object> conditions = new HashMap<String, Object>();
                conditions.put("affairIds", listAffair);
                ctpAffairList = affairManager.get(listAffair);

            }
        }
        Map<String, String> sourcePathMap = new HashMap<String, String>();
        if (Strings.isNotEmpty(ctpAffairList)) {

            List<CtpAffair> noPrintList = new ArrayList<CtpAffair>();
            //获取新建节点权限
            NodePolicyVO newColNodePolicy = colManager.getNewColNodePolicy(AppContext.currentAccountId());
            if ("listSent".equals(type)) {
                if (!newColNodePolicy.isPrint()) {
                    noPrintList.addAll(ctpAffairList);
                }
            }
            for (CtpAffair ctpAffair : ctpAffairList) {

                //判断每条事项是否存在打印权限====================

                //如果不是已发-就是待办和已办那么通过节点权限判断是否有打印权限
                if (!"listSent".equals(type)) {
                    //取打印权限
                    SeeyonPolicy affairNodePolicy = ColUtil.getPolicyByAffair(ctpAffair);
                    NodePolicyVO nodePolicy = null;
                    try {
                        Permission permission = permissionManager.getPermission(EnumNameEnum.col_flow_perm_policy.name(), affairNodePolicy.getName(), AppContext.currentAccountId());
                        nodePolicy = new NodePolicyVO(permission);
                    } catch (BusinessException e) {
                        LOG.error("", e);
                    }
                    if (nodePolicy.isPrint() == false) {
                        noPrintList.add(ctpAffair);
                    }
                }

                //CAP4与cap3分别
                boolean isCAP4 = false;
                if (ctpAffair.getFormAppId() != null) {
                    isCAP4 = capFormManager.isCAP4Form(ctpAffair.getFormAppId());
                }
                StringBuffer sbFormPath = new StringBuffer();
                if (isCAP4) {
                    String rightId = ContentUtil.findRightIdbyAffairIdOrTemplateId(ctpAffair, ctpAffair.getTempleteId());
                    sbFormPath.append(Strings.getBaseContext(request)).append("/common/cap4/template/display/pc/form/dist/index.html?_r=").append((new Date()).getTime())
                            .append("#/browse?rightId=").append(rightId).append("&isSubFlow=false&moduleId=")
                            .append(ctpAffair.getObjectId()).append("&moduleType=1&printFlag=true&screenCapture=true");

                    colManager.addFormRightIdToFormCache(rightId, ctpAffair.getFormAppId());
                    SecurityCheck.isLicit(request, response, ApplicationCategoryEnum.collaboration, AppContext.getCurrentUser(), ctpAffair.getObjectId(), ctpAffair, null);
                } else {
                    sbFormPath.append(Strings.getBaseContext(request)).append("/content/content.do?method=index&moduleId=").append(ctpAffair.getObjectId()).append("&moduleType=1&isFullPage=true");
                }
                sourcePathMap.put(ctpAffair.getId().toString(), sbFormPath.toString());
            }

            if (Strings.isNotEmpty(noPrintList)) {
                //跳转到这个界面
                ModelAndView mav = new ModelAndView("apps/collaboration/batchPrintError");
                mav.addObject("noPrintList", noPrintList);
                return mav;
            }
            //已经打印了派发一个事件来处理
            CollaborationAffairPrintEvent event = new CollaborationAffairPrintEvent(this);
            event.setAffairs(ctpAffairList);
            EventDispatcher.fireEventAfterCommit(event);
        }
        if (htmlToPdfManager == null) {
            htmlToPdfManager = (HtmlToPdfManager) AppContext.getBean("htmltopdfManager");
        }
        String pdfNames = htmlToPdfManager.batchHtmlToPdf(sourcePathMap, null, null);
        if (Strings.isNotBlank(pdfNames)) {
            response.sendRedirect(request.getContextPath() + "/print/web/viewer.html?file=" + pdfNames);
        }
        return null;
    }
}
