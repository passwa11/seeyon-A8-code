package com.seeyon.ctp.rest.resources;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.office.*;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.util.*;
import com.seeyon.v3x.edoc.constants.RecRelationAfterSendParam;
import com.seeyon.v3x.edoc.domain.*;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.manager.*;
import com.seeyon.v3x.edoc.util.DataUtil;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.manager.SendEdocManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.vo.PermissionMiniVO;
import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.manager.OfficeBakFileManager;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.phrase.manager.CommonPhraseManager;
import com.seeyon.ctp.common.phrase.po.CommonPhrase;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.util.EdocOpenFromUtil.EdocSummaryType;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryBO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryCountVO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.exchange.manager.EdocExchangeManager;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;

import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import org.springframework.web.servlet.ModelAndView;
import www.seeyon.com.utils.json.JSONUtils;

@Path("edocResource")
@Produces({MediaType.APPLICATION_JSON})
public class EdocResource extends BaseResource {

    private static Log LOGGER = CtpLogFactory.getLog(EdocResource.class);
    private RecieveEdocManager recieveEdocManager = (RecieveEdocManager) AppContext.getBean("recieveEdocManager");
    private IndexManager indexManager = (IndexManager) AppContext.getBean("indexManager");
    private SuperviseManager superviseManager = (SuperviseManager) AppContext.getBean("superviseManager");
    private OfficeLockManager officeLockManager = (OfficeLockManager) AppContext.getBean("officeLockManager");
    private HandWriteManager handWriteManager = (HandWriteManager) AppContext.getBean("handWriteManager");
    private EdocLockManager edocLockManager = (EdocLockManager) AppContext.getBean("edocLockManager");
    private EdocSummaryRelationManager edocSummaryRelationManager = (EdocSummaryRelationManager) AppContext.getBean("edocSummaryRelationManager");
    private TemplateManager templeteManager = (TemplateManager) AppContext.getBean("templateManager");
    private EdocMarkDefinitionManager edocMarkDefinitionManager = (EdocMarkDefinitionManager) AppContext.getBean("edocMarkDefinitionManager");
    private EdocMarkLockManager edocMarkLockManager = (EdocMarkLockManager) AppContext.getBean("edocMarkLockManager");
    private EdocMarkHistoryManager edocMarkHistoryManager = (EdocMarkHistoryManager) AppContext.getBean("edocMarkHistoryManager");
    private SendEdocManager sendEdocManager = (SendEdocManager) AppContext.getBean("sendEdocManager");
    private AppLogManager appLogManager = (AppLogManager) AppContext.getBean("appLogManager");


    private AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
    private CtpTrackMemberManager trackManager = (CtpTrackMemberManager) AppContext.getBean("trackManager");
    private CustomizeManager customizeManager = (CustomizeManager) AppContext.getBean("customizeManager");
    private EdocListManager edocListManager = (EdocListManager) AppContext.getBean("edocListManager");
    private EdocExchangeManager edocExchangeManager = (EdocExchangeManager) AppContext.getBean("edocExchangeManager");
    private EdocH5Manager edocH5Manager = (EdocH5Manager) AppContext.getBean("edocH5Manager");
    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private CommonPhraseManager phraseManager = (CommonPhraseManager) AppContext.getBean("phraseManager");
    private EdocManager edocManager = (EdocManager) AppContext.getBean("edocManager");
    private PermissionManager permissionManager = (PermissionManager) AppContext.getBean("permissionManager");
    private EdocFormManager edocFormManager = (EdocFormManager) AppContext.getBean("edocFormManager");
    private WorkflowApiManager wapi = (WorkflowApiManager) AppContext.getBean("wapi");
    private V3xHtmDocumentSignatManager htmSignetManager = (V3xHtmDocumentSignatManager) AppContext.getBean("htmSignetManager");
    private EdocSummaryManager edocSummaryManager = (EdocSummaryManager) AppContext.getBean("edocSummaryManager");
    private MSignatureManager mSignatureManager = (MSignatureManager) AppContext.getBean("mSignatureManagerforM3");
    private OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
    private EdocRegisterManager edocRegisterManager = (EdocRegisterManager) AppContext.getBean("edocRegisterManager");
    private EdocIndexEnableImpl edocIndexEnable = (EdocIndexEnableImpl) AppContext.getBean("edocIndexEnable");
    private OfficeBakFileManager officeBakFileManager = (OfficeBakFileManager) AppContext.getBean("officeBakFileManager");
    private AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
    ;
    private UserMessageManager userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
    private EdocStatManager edocStatManager = (EdocStatManager) AppContext.getBean("edocStatManager");
    private EdocMarkManager edocMarkManager = (EdocMarkManager) AppContext.getBean("edocMarkManager");

    /**
     * 公文事项设置是否跟踪
     * url: edocResource/setTrack
     *
     * @param 类型 名称                    必填        备注
     *           Long  affairId    Y    事项ID
     * @return 1设置跟踪，0设置不跟踪
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("setTrack")
    public Response setTrack(@QueryParam("affairId") Long affairId) throws BusinessException {

        CtpAffair affair = null;
        try {
            affair = affairManager.get(affairId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
            return fail("find affair is null");
        }
        boolean isTrack = Integer.valueOf(TrackEnum.all.ordinal()).equals(affair.getTrack())
                || Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack());

        int trackValue = isTrack ? TrackEnum.no.ordinal() : TrackEnum.all.ordinal();
        try {

            if (Integer.valueOf(TrackEnum.part.ordinal()).equals(affair.getTrack())) {
                trackManager.deleteTrackMembers(affair.getObjectId(), affairId);
            }

            Map<String, Object> m = new HashMap<String, Object>();
            m.put("track", trackValue);
            affairManager.update(affairId, m);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }

        return ok(trackValue);
    }

    /**
     * 公文事项是否设置了跟踪
     * url: edocResource/trackValue
     *
     * @param 类型 名称                    必填        备注
     *           Long   affairId   Y    事项ID
     * @return 1设置了跟踪，0未设置跟踪
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("trackValue")
    public Response trackValue(@QueryParam("affairId") Long affairId) throws BusinessException {
        User user = AppContext.getCurrentUser();
        CtpAffair affair = null;
        Integer trackValue = TrackEnum.no.ordinal();
        try {
            affair = affairManager.get(affairId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        if (affair == null) {
            return ok(trackValue);
        }
        String _trackValue = customizeManager.getCustomizeValue(user.getId(), CustomizeConstants.TRACK_PROCESS);
        if ("true".equals(_trackValue) && Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack()) &&
                !Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()).equals(affair.getSubState())) {
            trackValue = TrackEnum.all.ordinal();
        } else {
            trackValue = affair.getTrack();
        }
        return ok(trackValue);
    }


    /**
     * 获取待办发文
     * url: edocResource/getAllPending
     *
     * @return FlipInfo 待办公文信息
     * @throws BusinessException
     * @Params params 获取待办发文参数
     * 类型             名称               必填        备注
     * Integer pageNo    Y 分页信息，第几页
     * Integer pageSize  Y 分页信息，每页多少条数据
     * 以下是查询条件，查询条件为空时查询全部
     * String  startMemberName N  发起人
     * String  subject         N  标题
     * String  createDate      N  发起时间
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getAllPending")
    public Response getAllPending(Map<String, String> params) throws BusinessException {
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo.setNeedTotal(true);

        Pagination.setNeedCount(true);
        int nPageNo = Integer.parseInt(params.get("pageNo"));
        nPageNo = nPageNo < 1 ? 1 : nPageNo;
        Integer pageSize = Integer.parseInt(params.get("pageSize"));
        Pagination.setMaxResults(pageSize);
        Pagination.setFirstResult((nPageNo - 1) * pageSize);

        User user = AppContext.getCurrentUser();

        List<EdocSummaryModel> summarys = null;
        if (flipInfo != null && Strings.isNotBlank(user.getName())) {
            Map<String, Object> condition = createCondition(user, null, StateEnum.col_pending.key(), null);

            condition.putAll(params);
            String edocType = params.get("edocType");
            if (Strings.isNotBlank(edocType)) {
                condition.put("edocType", Integer.valueOf(edocType));
            }

            try {
                summarys = edocListManager.findEdocPendingList(10, condition);
            } catch (BusinessException e) {
                LOGGER.error("获得待办发文数据报错!", e);
            }
            //处理名称
            dealReturnInfo(summarys);

            flipInfo.setTotal(Pagination.getRowCount());
            flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
        }

        return ok(flipInfo);
    }

    private void dealReturnInfo(List<EdocSummaryModel> summarys) {
        for (EdocSummaryModel summary : summarys) {
            //加签、知会、当前会签  ps:回退优先，如果这条记录也被回退过，则优先显示回退图标，加签图标不显示
            if (summary.getAffair().getFromId() != null && summary.getAffair().getBackFromId() == null) {
                summary.setFromName(ResourceUtil.getString("edoc.pending.addOrJointly.label", Functions.showMemberName(summary.getAffair().getFromId())));
            }
            //回退、指定回退
            if (summary.getAffair().getBackFromId() != null
                    && !Integer.valueOf(SubStateEnum.col_pending_specialBackCenter.getKey()).equals(summary.getAffair().getSubState())) {
                summary.setBackFromName(ResourceUtil.getString("edoc.pending.stepBack.label", Functions.showMemberName(summary.getAffair().getBackFromId())));
            }
        }
    }

    /**
     * 获取所有列表记录数
     * url: edocResource/getListSizeByEdocType
     *
     * @return Object[]
     * <pre>
     * 		成功：[ {
     * 				  "listPendingSize" : 30,
     * 				  "listZcdbSize" : 15,
     * 				  "listSentSize" : 167,
     * 				  "listWaitSize" : 13,
     * 				  "listDoneAllSize" : 142,
     * 				  "edocType" : 0
     *                }, {
     * 				  "listPendingSize" : 9,
     * 				  "listZcdbSize" : 0,
     * 				  "listSentSize" : 15,
     * 				  "listWaitSize" : 1,
     * 				  "listDoneAllSize" : 4,
     * 				  "edocType" : 1
     *                }, {
     * 				  "listPendingSize" : 1,
     * 				  "listZcdbSize" : 0,
     * 				  "listSentSize" : 0,
     * 				  "listWaitSize" : 0,
     * 				  "listDoneAllSize" : 0,
     * 				  "edocType" : 2
     *                } ]
     * </pre>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getListSizeByEdocType")
    public Response getListSizeByEdocType() {
        Object[] objects = new Object[3];
        try {
            List<EdocSummaryCountVO> list = edocListManager.getCountGroupByEdocType();
            objects[0] = list.get(0);
            objects[1] = list.get(1);
            objects[2] = list.get(2);
        } catch (BusinessException e) {
            LOGGER.error("获得待办发文数据报错!", e);
        }
        return ok(objects);
    }

    /**
     * 按照公文类型和列表类型获取公文列表
     * url: edocResource/getSummaryListByEdocTypeAndListType
     *
     * @param params 获取公文列表的参数
     *               <pre>
     *               类型             名称                   必填           备注
     *               int	 edocType     N       公文类型
     *                  <pre>
     *                     0 发文
     *                     1 收文
     *                     2 签报
     *                  </pre>
     *               String listType   Y 打开来源
     *                  <pre>
     *                     listPending  待办
     *                     listSent     已发
     *                     listWaitSend 待发
     *                     listDoneAll  已办
     *                  </pre>
     *               String conditionKey  Y 搜索条件
     *               String textfield     Y 搜索传值
     *               String textfield1    N 搜索传值
     *               String pageNo        Y 第几页（大于1的整数）
     *               String pageSize      Y 每页多少条数据(大于1的整数)
     *               </pre>
     * @return FlipInfo
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("getSummaryListByEdocTypeAndListType")
    public Response getSummaryListByEdocTypeAndListType(Map<String, String> params) {
        int edocType = ParamUtil.getInt(params, "edocType");
        String listType = ParamUtil.getString(params, "listType");
        String conditionKey = ParamUtil.getString(params, "conditionKey");
        String textfield = ParamUtil.getString(params, "textfield");
        String textfield1 = ParamUtil.getString(params, "textfield1");
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo.setNeedTotal(true);

        Pagination.setNeedCount(true);
        int nPageNo = Integer.parseInt(params.get("pageNo"));
        nPageNo = nPageNo < 1 ? 1 : nPageNo;
        Integer pageSize = Integer.parseInt(params.get("pageSize"));
        Pagination.setMaxResults(pageSize);
        Pagination.setFirstResult((nPageNo - 1) * pageSize);

        List<EdocSummaryModel> summarys = new ArrayList<EdocSummaryModel>();
        User user = AppContext.getCurrentUser();
        if (flipInfo != null && Strings.isNotBlank(user.getName())) {
            Map<String, Object> condition = createNewCondition(user, edocType, StateEnum.col_pending.key(), listType, conditionKey, textfield, textfield1);
            try {
                int listTypeInt = EdocNavigationEnum.LIST_TYPE_PENDING;
                if ("listSent".equals(listType)) {
                    listTypeInt = EdocNavigationEnum.LIST_TYPE_SENT;
                }
                if ("listWaitSend".equals(listType)) {
                    listTypeInt = EdocNavigationEnum.LIST_TYPE_WAIT_SEND;
                }
                if ("listDoneAll".equals(listType)) {
                    listTypeInt = EdocNavigationEnum.LIST_TYPE_DONE;
                }
                summarys = edocListManager.findEdocPendingList(listTypeInt, condition);
                //处理名称
                dealReturnInfo(summarys);
                flipInfo.setData(EdocSummaryListVO.valueOf(summarys));
                flipInfo.setTotal(Pagination.getRowCount());
            } catch (BusinessException e) {
                LOGGER.error("获得待办发文数据报错!", e);
            }
        }
        return ok(flipInfo);
    }

    /**
     * 公文详情
     * url: edocResource/edocSummary
     *
     * @param params 公文详情的参数
     *               <pre>
     *               类型             名称                       必填           备注
     *               Long    affairId     Y     事项ID
     *               Long    summaryId    Y     协同ID
     *               String  openFrom     N     打开来源（默认值：listDoneAll）
     *                  <pre>
     *                     listPending  待办
     *                     listSent     已发
     *                     listWaitSend 待发
     *                     listDoneAll  已办
     *                     glwd         关联文档
     *                     docLib       文档中心
     *                     lenPotent    借阅
     *                  </pre>
     *               String baseObjectId  N 关联文档属于的数据ID
     *               String baseApp  N 关联文档属于的数据所在的模块
     *               String docResId  N 文档ID，用于权限验证
     *               </pre>
     * @return EdocSummaryBO
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("edocSummary")
    public Response edocSummary(Map<String, Object> params) {

        User user = AppContext.getCurrentUser();

        Long affairId = ParamUtil.getLong(params, "affairId", -1L);
        Long summaryId = ParamUtil.getLong(params, "summaryId", -1L);
        String openFrom = ParamUtil.getString(params, "openFrom", "listDoneAll");
        String isfromIndexSearch = ParamUtil.getString(params, "isTODO", "");
        if ("todo".equals(isfromIndexSearch) && affairId == -1l) {
            Map<String, Object> findSourceInfo;
            try {
                findSourceInfo = edocIndexEnable.findSourceInfo(summaryId);
                affairId = (Long) findSourceInfo.get("sourceId");
                CtpAffair affair = affairManager.get(affairId);
                if (affair != null) {
                    if (StateEnum.col_pending.key() == affair.getState()) {
                        openFrom = "listPending";
                    } else if (StateEnum.col_sent.key() == affair.getState()) {
                        openFrom = "listSent";
                    } else if (StateEnum.col_waitSend.key() == affair.getState()) {
                        openFrom = "listWaitSend";
                    } else if (StateEnum.col_done.key() == affair.getState()) {
                        openFrom = "listDoneAll";
                    }
                }
            } catch (BusinessException e) {
                LOGGER.info("全文检索穿透报错" + e.getMessage());
            }
        }

        EdocSummaryBO edocSummaryVo = new EdocSummaryBO();
        try {
            edocSummaryVo = edocH5Manager.getEdocSummaryBO(summaryId, affairId, openFrom, params);

            if (edocSummaryVo.getErrorRet().size() > 0) {
                return ok(edocSummaryVo.getErrorRet());
            }

            //当前登录信息
            edocSummaryVo.setCurrentUser(user);

            //待办数据
            if (EdocSummaryType.listPending.name().equals(edocSummaryVo.getListType())) {
                CtpAffair affair = edocSummaryVo.getAffairObj();
                EdocSummary summary = edocSummaryVo.getSummaryObj();

                //Office正文M3端缓存设置
                if (Strings.isNotEmpty(summary.getEdocBodies())) {
                    EdocBody b = summary.getEdocBodies().iterator().next();
                    if (!com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML.equals(b.getContentType())
                            && Strings.isNotBlank(b.getContent())) {
                        V3XFile f = fileManager.getV3XFile(Long.valueOf(b.getContent()));
                        if (f != null) {
                            edocSummaryVo.setBodyLastModify(DateUtil.formatDateTime(f.getUpdateDate()));
                        }
                    }
                }


                /*************************************M3文单签批开始******************************************/
                //当前节点如果有文单签批权限
                if (edocSummaryVo.getActions().contains("HtmlSign") && affairId != -1L) {
                    V3xHtmDocumentSignature hsSignature = htmSignetManager.getBySummaryIdAffairIdAndType(summary.getId(), affairId, V3xHtmSignatureEnum.HTML_SIGNATURE_DOCUMENT.key());//.getByAffairId(affairId);
                    String fildValue = "";
                    if (hsSignature != null) {
                        try {
                            fildValue = MSignaturePicHandler.encodeSignatureDataForStandard(hsSignature.getFieldValue());
                        } catch (IOException e) {
                            LOGGER.error("文单签批解密出错：", e);
                        }
                    }
                    edocSummaryVo.setFiledValue(fildValue);
                }
                String[] nodePolicyFromWorkflow = null;
                if (affair != null && affair.getActivityId() != null) {
                    nodePolicyFromWorkflow = wapi.getNodePolicyIdAndName(ApplicationCategoryEnum.edoc.name(), edocSummaryVo.getSummaryObj().getProcessId(), String.valueOf(edocSummaryVo.getAffairObj().getActivityId()));
                }
                String nodePermissionPolicy = "shenpi";
                //得到当前处理权限录入意见的显示位置
                if (nodePolicyFromWorkflow != null) {
                    nodePermissionPolicy = nodePolicyFromWorkflow[0];
                    //流程取过来的权限名，替换特殊空格[160 -> 32]
                    if (nodePermissionPolicy != null) {
                        nodePermissionPolicy = nodePermissionPolicy.replaceAll(new String(new char[]{(char) 160}), " ");
                    }
                }
                String disPosition = edocFormManager.getOpinionLocation(summary.getFormId(), EdocHelper.getFlowPermAccountId(summary, user.getLoginAccount())).get(nodePermissionPolicy);
                if (disPosition != null) {
                    String[] dis = disPosition.split("[_]");
                    disPosition = dis[0];
                }
                edocSummaryVo.setDisPosition(disPosition);

                edocSummaryVo.setNodePolicy(affair.getNodePolicy());
            }

            /*************************************M3文单签批结束******************************************/
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        //同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), affairId);

        return ok(edocSummaryVo);
    }

    /**
     * 获取公文的节点权限列表
     * url: edocResource/permissions
     *
     * @param 获取公文的节点权限列表参数 类型             名称                              必填           备注
     *                      String  type        		Y    类型
     *                      String  policyName  		Y    节点名称
     *                      Long    orgAccountId   	Y    单位Id
     *                      <pre>
     *                         类型 如 ：协同（collaboration）、表单(form)、发文(sendEdoc||edocSend)、收文(recEdoc||edocRec)、签报(edocSign||signReport)、信息报送(sendInfo)
     *                      </pre>
     * @return List<Permission>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("permissions")
    public Response permissions(@DefaultValue("sendEdoc") @QueryParam("type") String type
            , @QueryParam("policyName") String policyName, @QueryParam("orgAccountId") Long orgAccountId) throws BusinessException {

        User user = AppContext.getCurrentUser();

        String appName = type;
        Long accountId = user.getLoginAccount();
        if (null != orgAccountId) {
            accountId = orgAccountId;
        }
        boolean isTemplate = false;

        List<Permission> result = permissionManager.getPermissions4WFNodeProperties(appName, policyName, accountId, isTemplate);

        return ok(PermissionMiniVO.valueOf(result));
    }

    /**
     * 获取常用语
     * url : edocResource/phrase
     *
     * @return List<String>
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("phrase")
    public Response getPhrase() {
        List<CommonPhrase> phrases = new ArrayList<CommonPhrase>();
        try {
            phrases = phraseManager.getAllPhrases();
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        List<String> phraseNames = new ArrayList<String>();
        for (CommonPhrase commonPhrase : phrases) {
            phraseNames.add(commonPhrase.getContent());
        }
        return getResponse(phraseNames);
    }

    /**
     * 处理
     * url: edocResource/submit
     *
     * @param param 处理参数
     *              <pre>
     *              	类型                 名称                                 必填           备注
     *              	long     summaryId        Y 		协同ID
     *              	Long     affairId         Y 		事项ID
     *              	String   opinionContent   Y 		处理意见
     *              	Integer  opinionAttibute  Y 		处理态度
     *                 				-1 （没有录入态度时的默认值）
     *                 				 1  已阅
     *                				 2  同意
     *                 				 3  不同意
     *              	String    disPosition   	 N 		当前节点   转收文使用，当"report".equals(disPosition) 为下级单位公文向上级汇报意见
     *              	Integer   isTrack      	 Y 		跟踪
     *                 				 1  跟踪
     *                 				 2  不跟踪
     *              	boolean   isNewImg             Y 是否有文单签批
     *              	Map       workflow_definition  Y 流程相关数据
     *              	String    fileJson             N 附件信息串
     *              	String    oldOpinionIdStr      Y 之前的意见记录
     *              </pre>
     * @return Boolean true:处理成功 ，false:处理失败
     */
    @SuppressWarnings("unchecked")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("submit")
    public Response submit(Map<String, Object> param) {
        User user = AppContext.getCurrentUser();
        long summaryId = ParamUtil.getLong(param, "summaryId", -1L);
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if (Strings.isNotBlank(opinionContent)) {
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute", -1);
        String disPosition = ParamUtil.getString(param, "disPosition", "");
        String optionWay = ParamUtil.getString(param, "optionWay", "");
        Integer isTrack = ParamUtil.getInt(param, "isTrack");
        boolean isNewImg = checkisNewImg(param);
        if (isNewImg) {//是否有文单签批
            saveQianpiData(param, affairId, isNewImg);//文单签批
        }
        //流程相关数据
        Map<String, String> wfParamMap = (Map<String, String>) param.get("workflow_definition");
        wfParamMap.put("fileJson", ParamUtil.getString(param, "fileJson", "[]"));

        String processChangeMessage = ParamUtil.getString(wfParamMap, "processChangeMessage", "");

        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setOldOpinionIdStr(ParamUtil.getString(param, "oldOpinionIdStr", ""));

        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setIdIfNew();

        signOpinion.isPipeonhole = false;
        if (isTrack == 0) {
            signOpinion.affairIsTrack = false;
        }
        if ("report".equals(disPosition)) {
            signOpinion.setIsReportToSupAccount(Boolean.TRUE);
        }
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setAffairTrack(isTrack);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setOptionWay(optionWay);

        Boolean returnValue = true;
        try {
            edocH5Manager.transDealEdoc(edocManagerModel, wfParamMap);
        } catch (BusinessException e) {
            returnValue = false;
            LOGGER.error(e);
        } finally {
            edocManager.unlockEdocAll(summaryId, null);
        }
        return getResponse(returnValue);
    }

    private boolean checkisNewImg(Map<String, Object> param) {
        String _isNewImg = ParamUtil.getString(param, "isNewImg");
        boolean isNewImg = false;
        if (Strings.isNotBlank(_isNewImg)) {
            isNewImg = Boolean.valueOf(_isNewImg);
        }
        return isNewImg;
    }

    //保存签批数据
    private void saveQianpiData(Map<String, Object> param, Long affairId, boolean isNewImg) {
        String qianpiData = ParamUtil.getString(param, "qianpiData");
        try {
            mSignatureManager.transSaveSignatureAndHistory(qianpiData, String.valueOf(affairId), String.valueOf(isNewImg));
        } catch (BusinessException e) {
            LOGGER.error(e);
        }
    }

    /**
     * 暂存待办
     * url: edocResource/zcdb
     *
     * @param param 暂存待办参数
     *              <pre>
     *              	类型                 名称                                 	      必填           备注
     *               long      summaryId   			Y 	协同ID
     *               Long      affairId    			Y 	事项ID
     *               String    opinionContent    	Y 	处理意见
     *               Integer   opinionAttibute   	Y 	处理态度
     *                			 -1 （没有录入态度时的默认值）
     *                			  1  已阅
     *                 			  2  同意
     *                 			  3  不同意
     *               Integer   isTrack      		    Y 	跟踪
     *                 			  1  跟踪
     *                    	      2  不跟踪
     *               boolean   isNewImg              Y 	是否有文单签批
     *               String    processChangeMessage  Y 	流程改变信息
     *               Map       workflow_definition   Y 	流程相关数据
     *               String    fileJson              N 	附件信息串
     *               String    oldOpinionIdStr       Y  	之前的意见记录
     *              </pre>
     * @return Boolean true:暂存待办成功 ，false:暂存待办失败
     */
    @SuppressWarnings("unchecked")
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("zcdb")
    public Response zcdb(Map<String, Object> param) {
        User user = AppContext.getCurrentUser();
        long summaryId = ParamUtil.getLong(param, "summaryId", -1L);
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if (Strings.isNotBlank(opinionContent)) {
            opinionContent = Strings.removeEmoji(opinionContent);
        }

        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute", -1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack");

        String processChangeMessage = ParamUtil.getString(param, "processChangeMessage");

        boolean isNewImg = checkisNewImg(param);
        if (isNewImg) {//是否有文单签批
            saveQianpiData(param, affairId, isNewImg);//文单签批
        }

        //流程相关数据
        Map<String, String> wfParamMap = (Map<String, String>) param.get("workflow_definition");
        wfParamMap.put("fileJson", ParamUtil.getString(param, "fileJson", "[]"));

        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setOldOpinionIdStr(ParamUtil.getString(param, "oldOpinionIdStr", ""));

        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setIdIfNew();
        signOpinion.isPipeonhole = false;
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setAffairTrack(isTrack);
        Boolean returnValue = true;
        try {
            returnValue = edocH5Manager.transDoZCDB(edocManagerModel, user, wfParamMap);
        } catch (BusinessException e) {
            returnValue = false;
            LOGGER.error("", e);
        } finally {
            edocManager.unlockEdocAll(summaryId, null);
        }
        return getResponse(returnValue);
    }

    /**
     * 回退
     * url: edocResource/stepback
     *
     * @param param 回退参数
     *              <pre>
     *              	类型                  		名称                                 	 必填           备注
     *               long 			summaryId   		 Y 		协同ID
     *               Long 			affairId    		 Y 		事项ID
     *               String 			opinionContent   	 Y 		处理意见
     *               Integer 		opinionAttibute  	 Y 		处理态度
     *                					-1 （没有录入态度时的默认值）
     *                 					1  已阅
     *                				 	2  同意
     *                 					3  不同意
     *               Integer         isTrack      		 N 		跟踪
     *                 					1  跟踪
     *                 					2  不跟踪
     *               boolean         isNewImg             Y      是否有文单签批
     *               String        processChangeMessage   Y 		流程改变信息
     *               String             policy            N      节点权限
     *              </pre>
     * @return String true:回退成功 ，false:回退失败
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("stepback")
    public Response stepback(Map<String, Object> param) {
        User user = AppContext.getCurrentUser();
        Long summaryId = ParamUtil.getLong(param, "summaryId", -1L);
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if (Strings.isNotBlank(opinionContent)) {
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute", -1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack", TrackEnum.no.ordinal());
        String processChangeMessage = ParamUtil.getString(param, "processChangeMessage");
        boolean isNewImg = checkisNewImg(param);
        if (isNewImg) {//是否有文单签批
            saveQianpiData(param, affairId, isNewImg);//文单签批
        }
        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setCreateUserId(user.getId());
        signOpinion.setIdIfNew();
        signOpinion.isPipeonhole = false;
        signOpinion.setPolicy(ParamUtil.getString(param, "policy"));

        Map<String, Object> returnMap = new HashMap<String, Object>();
        String returnValue = "true";
        String errMsg = "";
        try {
            CtpAffair affair = affairManager.get(affairId);
            if (affair != null) {
                signOpinion.setNodeId(affair.getActivityId());
                signOpinion.setPolicy(affair.getNodePolicy());
            }
            if (affair == null) {
                returnMap.put("returnValue", "false");
                returnMap.put("errMsg", "affair is null,can`t do this operation!");
                return getResponse(returnMap);
            }
            EdocSummary summary = edocSummaryManager.findById(summaryId);
            if (summary.getFinished()) {
                Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, summary.getOrgAccountId());
                String[] result = edocManager.edocCanStepBack(String.valueOf(affair.getSubObjectId()), String.valueOf(summary.getProcessId()), String.valueOf(affair.getActivityId()), String.valueOf(summary.getCaseId()), String.valueOf(flowPermAccountId), EdocEnum.getEdocAppName(summary.getEdocType()));
                if (!"true".equals(result[0])) {
                    errMsg = ResourceUtil.getString("edoc.stepback.cannot", "《" + summary.getSubject() + "》");
                    returnMap.put("returnValue", "false");
                    returnMap.put("errMsg", errMsg);
                    return getResponse(returnMap);
                }
            }
        } catch (BusinessException e1) {
            LOGGER.error("H5公文回退异常", e1);
        }

        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setAffairTrack(isTrack);

        //保存附件
        String fileJson = ParamUtil.getString(param, "fileJson", "[]");
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("fileJson", fileJson);
        try {
            returnValue = edocH5Manager.transStepback(edocManagerModel, paramMap);
        } catch (BusinessException e) {
            returnValue = "false";
            LOGGER.error("H5公文回退异常", e);
        } finally {
            edocManager.unlockEdocAll(summaryId, null);
        }
        returnMap.put("returnValue", returnValue);
        return getResponse(returnMap);
    }

    /**
     * 是否能进行回退
     * url: edocResource/canStepBack
     *
     * @param param 回退参数
     *              <pre>
     *              	类型                  		名称                                 	 必填           备注
     *               Long 			affairId    		 Y 		事项ID
     *              </pre>
     * @return Map
     * map.canStepBack true:可以回退 ，false:不可以回退
     * map.error_msg    不可以回退是返回不可回退的信息，可以回退时返回空
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("canStepBack")
    public Response canStepBack(Map<String, Object> param) {
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("canStepBack", "true");
        try {
            CtpAffair affair = affairManager.get(affairId);
            EdocSummary summary = edocSummaryManager.findById(affair.getObjectId());
            Long flowPermAccountId = EdocHelper.getFlowPermAccountId(summary, summary.getOrgAccountId());
            String[] result = edocManager.edocCanStepBack(String.valueOf(affair.getSubObjectId()), String.valueOf(summary.getProcessId()), String.valueOf(affair.getActivityId()), String.valueOf(summary.getCaseId()), String.valueOf(flowPermAccountId), EdocEnum.getEdocAppName(summary.getEdocType()));
            if (!"true".equals(result[0])) {
                returnMap.put("canStepBack", "false");
                returnMap.put("error_msg", result[1]);
                return getResponse(returnMap);
            }
        } catch (BusinessException e1) {
            LOGGER.error("H5验证公文是否可以回退异常", e1);
            returnMap.put("canStepBack", "false");
            returnMap.put("error_msg", "affair error!");
        }
        return getResponse(returnMap);
    }

    /**
     * 撤销
     * url: edocResource/cancel
     *
     * @param param 撤销参数
     *              <pre>
     *              	类型                  		名称                                 	 必填           备注
     *               long            summaryId  			Y 		协同ID
     *               Long            affairId    		Y 		事项ID
     *               String          opinionContent    	Y 		处理意见
     *               Integer         opinionAttibute  	Y	 	处理态度
     *                 				-1 （没有录入态度时的默认值）
     *                 				1  已阅
     *                 				2  同意
     *                 				3  不同意
     *               Integer         isTrack      		N 		跟踪
     *                 				1  跟踪
     *                			    2  不跟踪
     *               boolean         isNewImg     		Y		 是否有文单签批
     *               String      processChangeMessage    Y 		流程改变信息
     *               String          policy      		N 		节点权限
     *              </pre>
     * @return map
     * <pre>
     * 				成功{returnValue : true}
     * 				失败{returnValue : false}
     * 			</pre>
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("cancel")
    public Response cancel(Map<String, Object> param) {
        User user = AppContext.getCurrentUser();
        Long summaryId = ParamUtil.getLong(param, "summaryId", -1L);
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if (Strings.isNotBlank(opinionContent)) {
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute", -1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack", TrackEnum.no.ordinal());
        String processChangeMessage = ParamUtil.getString(param, "processChangeMessage");

        String returnValue = "true";
        String errMsg = "";
        try {
            EdocSummary summary = edocManager.getEdocSummaryById(summaryId, false);
            if (summary != null && summary.getFinished()) {
                errMsg = ResourceUtil.getString("edoc.cancel.cannot");
                Map<String, Object> returnMap = new HashMap<String, Object>();
                returnMap.put("returnValue", "false");
                returnMap.put("errMsg", errMsg);
                return getResponse(returnMap);
            }
        } catch (BusinessException e2) {
            LOGGER.error("H5获取主表信息异常", e2);
        }

        boolean isNewImg = checkisNewImg(param);
        if (isNewImg) {//是否有文单签批
            saveQianpiData(param, affairId, isNewImg);//文单签批
        }
        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setAffairId(affairId);
        signOpinion.setContent(opinionContent);
        signOpinion.setAttribute(opinionAttibute);
        signOpinion.setCreateUserId(user.getId());
        signOpinion.setIdIfNew();
        signOpinion.isPipeonhole = false;
        signOpinion.setPolicy(ParamUtil.getString(param, "policy"));
        try {
            CtpAffair affair = affairManager.get(affairId);
            if (affair != null) {
                if (affair.getActivityId() != null) {
                    signOpinion.setNodeId(affair.getActivityId());
                }
                signOpinion.setPolicy(affair.getNodePolicy());
            }
        } catch (BusinessException e1) {
            LOGGER.error("H5公文回退异常", e1);
        }

        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setSignOpinion(signOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setProcessChangeMessage(processChangeMessage);
        edocManagerModel.setAffairTrack(isTrack);

        //保存附件
        String fileJson = ParamUtil.getString(param, "fileJson", "[]");
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("fileJson", fileJson);
        try {
            returnValue = edocH5Manager.transRepeal(edocManagerModel, paramMap);
            if ("isNull".equals(returnValue)) {
                returnValue = "false";
            } else if ("isDelete".equals(returnValue)) {
                returnValue = "false";
            } else if ("isBack".equals(returnValue)) {
                returnValue = "false";
            } else if ("isStop".equals(returnValue)) {
                returnValue = "false";
            } else if ("isDone".equals(returnValue)) {
                returnValue = "false";
            }
        } catch (BusinessException e) {
            returnValue = "false";
            LOGGER.error("", e);
        } finally {
            edocManager.unlockEdocAll(summaryId, null);
        }
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("returnValue", returnValue);
        return getResponse(returnMap);
    }

    /**
     * 取回
     * <p>
     * url: edocResource/takeBack
     *
     * @param param 取回参数
     *              <pre>
     *              	类型                  		名称                                 	 必填           备注
     *               long            summaryId  			Y 		协同ID
     *               Long            affairId    		Y 		事项ID
     *              </pre>
     * @return String 0:取回成功，15:取回失败
     * @throws BusinessException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("takeBack")
    public Response takeBack(Map<String, Object> params) throws BusinessException {
        String message = "0";

        Long affairId = ParamUtil.getLong(params, "affairId", 0l);
        Long summaryId = ParamUtil.getLong(params, "summaryId", 0l);
        String processId = "";
        boolean isBackOk = true;
        EdocSummary summary = null;
        try {
            if (affairId != 0) {
                summary = edocManager.getEdocSummaryById(summaryId, false);
                processId = summary.getProcessId();
                CtpAffair affair = affairManager.get(affairId);

                //0-12
                message = edocManager.canTakeBack(ApplicationCategoryEnum.edoc.name(), String.valueOf(processId),
                        String.valueOf(affair.getActivityId()), String.valueOf(affair.getSubObjectId()), String.valueOf(affairId));
                //只有0的时候才能取回
                if ("0".equals(message)) {
                    isBackOk = edocManager.takeBack(affairId);

                    //取回后，更新当前待办人
                    summary.setFinished(false);//被取回设置结束为false
                    summary.setCompleteTime(null);
                    //跟新当前待办人
                    EdocHelper.updateCurrentNodesInfo(summary, true);

                    //取回失败
                    if (!isBackOk && affair != null) {
                        message = "15";
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("公文取回时抛出异常：", e);
        } finally {
            edocManager.unlockEdocAll(summaryId, summary);
        }
        return ok(message);
    }

    /**
     * 终止
     * url: edocResource/stepStop
     *
     * @param param 取回参数
     *              <pre>
     *              	类型                  		名称                                 	 必填           备注
     *               long            summaryId  			Y 		协同ID
     *               Long            affairId    		Y 		事项ID
     *               String			opinionContent      Y 		处理意见
     *               String          afterSign           Y 		处理态度
     *                 				-1 （没有录入态度时的默认值）
     *                 				1  已阅
     *                 				2  同意
     *                 				3  不同意
     *               String          isHidden          	N 		是否隐藏
     *               String          policy      		N 		节点权限
     *              </pre>
     * @return true:终止成功
     * null:终止失败
     * @throws BusinessException
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("stepStop")
    public Response stepStop(Map<String, Object> params) throws BusinessException {
        User user = AppContext.getCurrentUser();
        EdocSummary summary = null;

        Long affairId = ParamUtil.getLong(params, "affairId", 0l);
        Long summaryId = ParamUtil.getLong(params, "summaryId", 0l);
        String content = ParamUtil.getString(params, "opinionContent", "");
        if (Strings.isNotBlank(content)) {
            content = Strings.removeEmoji(content);
        }
        //态度
        String afterSign = ParamUtil.getString(params, "afterSign", "");
        String isHidden = ParamUtil.getString(params, "isHidden", null);
        //Long currentNodeId = ParamUtil.getLong(params, "currentNodeId", null);
        try {

            CtpAffair _affair = affairManager.get(affairId);
            //当公文不是待办/在办的状态时，不能终止操作
            if (_affair.getState() != StateEnum.col_pending.key()) {
                String msg = EdocHelper.getErrorMsgByAffair(_affair);
                return ok(msg);
            }
            boolean isNewImg = checkisNewImg(params);
            if (isNewImg) {//是否有文单签批
                saveQianpiData(params, affairId, isNewImg);//文单签批
            }
            summary = edocManager.getEdocSummaryById(summaryId, true);
            //保存终止时的意见,附件
            EdocOpinion signOpinion = new EdocOpinion();
            //TODO 2016-6-7 bind(request, signOpinion);
            signOpinion.setContent(content);
            //设置态度显示终止
            signOpinion.setAttribute(OpinionType.stopOpinion.ordinal());
            signOpinion.isDeleteImmediate = "delete".equals(afterSign);
            signOpinion.affairIsTrack = "track".equals(afterSign);

            signOpinion.setAffairId(affairId);
            signOpinion.setPolicy(ParamUtil.getString(params, "policy"));
            signOpinion.setIsHidden(isHidden != null);
            signOpinion.setIdIfNew();

            if (_affair != null) {
                signOpinion.setNodeId(_affair.getActivityId());
                signOpinion.setPolicy(_affair.getNodePolicy());
            }

            //设置代理人信息
            if (user.getId().longValue() != _affair.getMemberId().longValue()) {
                signOpinion.setProxyName(user.getName());
            }
            signOpinion.setCreateUserId(_affair.getMemberId());

	        /*TODO //修改公文附件
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if(editHelper.hasEditAtt()){//是否修改附件
            	attachmentManager.deleteByReference(summaryId, summaryId);//删除公文附件
            }
            //保存公文附件及回执附件，create方法中前台subReference传值为空，默认从java中传过去， 因为公文附件subReference从前台js传值 过来了，而回执附件没有传subReference，所以这里传回执的id
            this.attachmentManager.create(ApplicationCategoryEnum.edoc, summaryId, signOpinion.getId(), request);
            if(editHelper.hasEditAtt()){//是否修改附件
            	//设置summary附件标识,修改affair附件标识，设置附件元素，修改附件日志
                EdocHelper.updateAttIdentifier(summary, editHelper.parseProcessLog(Long.parseLong(processId), currentNodeId), true, "stepStop");
            } */
            //保存附件
            String relateInfo = ParamUtil.getString(params, "fileJson", "[]");//ParamUtil.getString(wfParamMap, "fileJson", "[]");
            List<Map> files = JSONUtil.parseJSONString(relateInfo, List.class);
            try {
                List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.edoc, summaryId, signOpinion.getId(), files);
                if (!attList.isEmpty()) {
                    attachmentManager.create(attList);
                    signOpinion.setHasAtt(true);
                    signOpinion.setOpinionAttachments(attList);
                }
            } catch (Exception ex) {
                LOGGER.error("", ex);
                throw new BusinessException(ex);
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("edocOpinion", signOpinion);
            map.put("summaryId", summaryId);

            edocManager.transStepStop(affairId, map);

            return ok(true);
        } catch (Exception e) {
            LOGGER.error("公文终止时抛出异常：", e);
        } finally {
            edocManager.unlockEdocAll(summaryId, summary);
        }
        return null;
    }

    /**
     * 处理时验证，非查看校验
     * url: edocResource/checkAffairValid
     *
     * @param 处理时验证，非查看校验参数 <pre>
     *                      		类型                  		名称                                 	 必填           备注
     *                       	String            affairId    		Y 		事项ID
     *                           String 			  pageNodePolicy    Y       页面的节点权限id
     *                       </pre>
     * @return Map<String, String>
     * <pre>
     * 							正常：返回值 {isOnlyView:false}
     * 							异常：返回值{isOnlyView:false,
     * 									  error_msg:  事项已被删除}
     * 							备注：isOnlyView始终是false,含义是一个入口的标识，代表从这个接口进入的
     * 							Map中是否含有等于error_msg的key，value的值是 事项已被删除，撤销，终止；表示affairs异常
     * 						</pre>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkAffairValid")
    public Response checkAffairValid(@QueryParam("affairId") String affairId, @QueryParam("pageNodePolicy") String pageNodePolicy) throws BusinessException {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("isOnlyView", "false");
        if (Strings.isBlank(affairId)) {
            affairId = "-1";
        }
        edocH5Manager.isValidAffair(Long.parseLong(affairId), pageNodePolicy, ret);
        return ok(ret);
    }

    /**
     * 验证公文是否已交换
     * <p>
     * url: edocResource/checkCanTakeBack
     *
     * @param 验证公文是否已交换参数 <pre>
     *                    		类型                  		    名称                                 	 必填           备注
     *                     	String            summaryId    		 Y 		协同ID
     *                     </pre>
     * @return boolean true:未交换，false:已交换
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkCanTakeBack")
    public Response checkIsExchanged(@QueryParam("summaryId") String summaryId) throws BusinessException {
        boolean ret = true;
        if (Strings.isNotBlank(summaryId)) {
            ret = !edocManager.isBeSended(Long.parseLong(summaryId));
        }
        return ok(ret);
    }

    /**
     * 校验公文文号
     * <p>
     * url: edocResource/checkEdocMarkIsUsed
     *
     * @param 校验公文文号参数 <pre>
     *                 		类型                  		    名称                                 	 必填           备注
     *                  	String            summaryId    		 Y 		协同ID
     *                      String			  docMark            Y      公文文号
     *                      String			  orgAccountId       Y      单位ID
     *                  </pre>
     * @return boolean true:成功，false:失败
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkEdocMarkIsUsed")
    public Response checkEdocMarkIsUsed(@QueryParam("summaryId") String summaryId, @QueryParam("docMark") String docMark, @QueryParam("orgAccountId") String orgAccountId) throws BusinessException {
        boolean ret = edocH5Manager.checkEdocMarkisUsed(docMark, summaryId, orgAccountId);
        return ok(ret);
    }

    /**
     * 校验公文能否取回
     * url: edocResource/checkTakeBack
     *
     * @param 校验公文能否取回参数 <pre>
     *                   		类型                  		    名称                                 	 必填           备注
     *                    	String            affairId    		 Y 		事项ID
     *                    </pre>
     * @return String
     * <pre>
     * 						-1  :  表示程序或数据发生异常,不可以取回
     * 						0  :   可以取回
     * 						1  :   当前流程已经结束,不可以取回
     * 						2  :   后面节点任务事项已处理完成,不可以取回
     * 						3  :   当前节点触发的子流程已经结束,不可以取回
     * 						4  :   当前节点触发的子流程中已核定通过,不可以取回
     *                      5  :   当前节点是知会节点,不可以取回
     *                      6  :   当前节点为核定节点,不可以取回
     * 						8  :   后面节点任务事项处于指定回退状态，不可以取回
     * 						11 :   公文已经被撤销，不能取回
     * 						12 :   回退公文已经被回退，不能取回
     * 					</pre>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("checkTakeBack")
    public Response checkTakeBack(@QueryParam("affairId") String affairId) throws BusinessException {
        String message = "0";
        if (Strings.isNotBlank(affairId) && !"0".equals(affairId)) {
            String processId = "";
            Long currentAffairId = Long.parseLong(affairId);
            try {
                CtpAffair affair = affairManager.get(currentAffairId);
                if (affair == null) {
                    return ok("-1");
                }
                EdocSummary summary = edocManager.getEdocSummaryById(affair.getObjectId(), false);
                if (summary != null) {
                    processId = summary.getProcessId();
                }
                message = edocManager.canTakeBack(ApplicationCategoryEnum.edoc.name(), String.valueOf(processId), String.valueOf(affair.getActivityId()), String.valueOf(affair.getSubObjectId()), String.valueOf(affairId));
            } catch (Exception e) {
                LOGGER.error("公文取回时抛出异常：", e);
            }
        }
        return ok(message);
    }

    /**
     * 解全部锁
     * url: edocResource/unlockEdocAll
     *
     * @param 解全部锁参数 <pre>
     *               		类型                  		    名称                                 	 必填           备注
     *                	String            summaryId    		 Y 		协同ID
     *                </pre>
     * @return String true:成功解全部锁
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("unlockEdocAll")
    public Response unlockEdocAll(@QueryParam("summaryId") Long summaryId) throws BusinessException {
        edocManager.unlockEdocAll(summaryId, null);
        return ok("true");
    }

    /**
     * @param user
     * @param edocType
     * @param state
     * @param listType
     * @return
     */
    private Map<String, Object> createCondition(User user, Integer edocType, int state, String listType) {
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("track", -1);
        condition.put("state", state);
        if (edocType != null) {
            condition.put("edocType", edocType);
        }
        condition.put("subEdocType", -1L);
        condition.put("type", 10);
        condition.put("user", user);
        condition.put("userId", user.getId());
        condition.put("accountId", user.getLoginAccount());
        condition.put("listType", listType);
        condition.put("isOnlyPendingSurplusTime", true);
        return condition;
    }

    /**
     * @param user
     * @param edocType
     * @param state
     * @param listType
     * @param conditionKey 搜索条件
     * @param textfield    搜索传值
     * @param textfield1
     * @return
     */
    private Map<String, Object> createNewCondition(User user, int edocType, int state, String listType, String conditionKey, String textfield, String textfield1) {
        Map<String, Object> condition = createCondition(user, edocType, state, listType);
        condition.put("conditionKey", conditionKey);
        condition.put("textfield", textfield);
        condition.put("textfield1", textfield1);
        return condition;
    }

    /**
     * 公文待签收数据列表
     * url: edocResource/signed
     *
     * @param 公文待签收数据列表参数 <pre>
     *                    		类型                  		    名称                                 	 必填           备注
     *                     	Long            memberId    		 Y 		人员ID
     *                     </pre>
     * @return List
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("signed")
    @RestInterfaceAnnotation
    public Response signed(@QueryParam("memberId") Long memberId) throws BusinessException {
        Map<String, Object> condition = new HashMap<String, Object>();
        V3xOrgMember memberById = orgManager.getMemberById(memberId);
        User user = new User();
        user.setId(memberId);
        String listType = "listExchangeToRecieve";
        int type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
        int state = Integer.parseInt(EdocNavigationEnum.EdocV5ListTypeEnum.getStateName(listType));
        condition.put("modelType", "toReceive");
        condition.put("state", state);
        condition.put("type", type);
        condition.put("user", user);
        condition.put("listType", listType);
        List findEdocExchangeRecordList = edocExchangeManager.findEdocExchangeRecordList(type, condition);
        return ok(findEdocExchangeRecordList);
    }

    /**
     * 公文待登记数据列表
     * url: edocResource/registered
     *
     * @param 公文待登记数据列表参数 <pre>
     *                    		类型                  		    名称                                 	 必填           备注
     *                     	Long            memberId    		 Y 		人员ID
     *                     </pre>
     * @return List<EdocRegister>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("registered")
    @RestInterfaceAnnotation
    public Response registered(@QueryParam("memberId") Long memberId) {
        String condition = "";
        String[] value = new String[2];
        String listType = "listV5Register";
        User user = new User();
        user.setId(memberId);
        user.setLoginAccount(AppContext.getCurrentUser().getLoginAccount());
        List<EdocRegister> findRegisterByState = edocRegisterManager.findRegisterByState(condition, value, EdocNavigationEnum.RegisterState.Registed.ordinal(), user);
        return ok(findRegisterByState);
    }

    /**
     * @param object
     * @return
     */
    private Response getResponse(Object object) {
        String jsonStr = "";
        if (null != object) {
            jsonStr = JSONUtils.toJSON(object);
        }
        return ok(jsonStr);
    }

    /**
     * 当前人员的菜单权限（公文）
     * <p>
     * url: edocResource/edoc/user/privMenu
     *
     * @return Map<String, Object>
     * <pre>
     * 							haveEdocSend : false  没有发文管理权限  /  true 有发文权限
     * 							haveEdocSignReport : false 没有签报权限  / true 有签报权限
     * 							haveEdocRec :  false 没有收文权限   / true有收文权限
     * 						</pre>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("edoc/user/privMenu")
    public Response edocUserPeivMenu() throws BusinessException {

        Map<String, Object> params = new HashMap<String, Object>();
        User user = AppContext.getCurrentUser();

        boolean haveEdocSend = user.hasResourceCode("F07_sendManager");  //发文管理
        boolean haveEdocSignReport = user.hasResourceCode("F07_signReport");   //签报管理
        boolean haveEdocRec = user.hasResourceCode("F07_recManager");   //收文管理

        params.put("haveEdocSend", haveEdocSend);
        params.put("haveEdocSignReport", haveEdocSignReport);
        params.put("haveEdocRec", haveEdocRec);

        return ok(params);
    }

    /**
     * 文单签批--加锁
     * url: edocResource/qianpiLock
     *
     * @param 文单签批--加锁参数 <pre>
     *                   		类型                  		    名称                                 	 必填           备注
     *                    	String            summaryId    		 Y 		协同ID
     *                    </pre>
     * @return UserUpdateObject
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("qianpiLock")
    public Response qianpiLock(@QueryParam("summaryId") String summaryId) throws BusinessException {
        UserUpdateObject os = edocSummaryManager.editObjectState(summaryId);
        return ok(os);
    }

    private static final String EXPORT_EDOC_ALL = "0";
    private static final String EXPORT_EDOC_FORM = "1";
    private static final String EXPORT_EDOC_BODY = "2";

    /**
     * 根据公文id及导出类型导出公文到指定目录
     *
     * @param params Map<String, Object> | 必填  | 其他参数
     *               <pre>
     *               summaryId     String  |  必填       |  公文ID
     *               folder        String  |  必填       |  输出的目录
     *               exportType    String  |  非必输   |  导出的类型
     *                             0-全部；1-文单；2-正文(含花脸)
     *                             不输入默认导出全部
     *               </pre>
     *               <pre>
     *               @return Map<String, String>
     *               			<pre>
     *               				success: false 失败，true 成功
     *               				msg:结果描述
     *               			</pre>
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("exportFile")
    @RestInterfaceAnnotation
    public Response exportEdocFile(Map<String, Object> param) throws BusinessException {
        // 判断rest用户是否是单位公文收发员或者部门公文收发员
        Map<String, Object> map = new HashMap<String, Object>();
        if (EdocRoleHelper.isAccountExchange() || EdocRoleHelper.isDepartmentExchange()) {
            return ok(this.exportFile(param));
        } else {
            map.put("success", false);
            map.put("msg", ResourceUtil.getString("edoc.alert.no.export.label"));//"用户不是[单位公文收发员]或者[部门公文收发员]，不允许导出公文相关信息"
            return ok(map);
        }
    }

    /**
     * 根据公文id及导出类型导出公文到指定目录
     *
     * @param param summaryId     String  |  必填       |  公文ID
     *              folder        String  |  必填       |  输出的目录
     *              exportType    String  |  非必输   |  导出的类型
     *              0-全部；1-文单；2-正文(含花脸)
     *              不输入默认导出全部
     * @return
     */
    private Map<String, Object> exportFile(Map<String, Object> param) {
        String summaryId = (String) param.get("summaryid");
        String folder = (String) param.get("folder");
        String exportType = (String) param.get("exportType");
        List<Long> bodyIds = new ArrayList<Long>();
        List<V3XFile> v3xList = new ArrayList<V3XFile>();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            if ((null != summaryId && !"".equals(summaryId)) && (null != folder && !"".equals(folder))) {
                if (null == exportType) {
                    exportType = EXPORT_EDOC_ALL;
                }
                List<Long> fileList = new ArrayList<Long>();
                EdocSummary es = edocManager.getEdocSummaryById(Long.parseLong(summaryId), true);
                if (null != es) {
                    Set<EdocBody> esb = es.getEdocBodies(); // 公文的正文不一定是一个，所以这里用循环取正文的花脸
                    for (EdocBody edocBody : esb) {
                        long ecid = Long.parseLong(edocBody.getContent());
                        bodyIds.add(ecid);
                        List<Long> obfList = officeBakFileManager.getOfficeBakFileIds(ecid);
                        if (obfList.size() > 0) {
                            fileList.addAll(obfList);
                        }
                        Long[] fids = new Long[obfList.size()];
                        for (int i = 0; i < obfList.size(); i++) {
                            fids[i] = obfList.get(i);
                        }
                        try {
                            v3xList = fileManager.getV3XFile(fids);
                            if (null != v3xList && !v3xList.isEmpty() && v3xList.size() > 1) {
                                Collections.sort(v3xList, new Comparator<V3XFile>() {
                                    @Override
                                    public int compare(V3XFile file1, V3XFile file2) {
                                        if (file1.getCreateDate().compareTo(file2.getCreateDate()) > 0) {
                                            return -1;
                                        } else if (file1.getCreateDate().compareTo(file2.getCreateDate()) < 0) {
                                            return 1;
                                        } else {
                                            return 0;
                                        }
                                    }
                                });
                            }
                        } catch (BusinessException e1) {
                            throw new BusinessException(ResourceUtil.getString("edoc.alert.no.file.exception.label") + e1.getMessage());//获取文件异常：
                        }
                    }
                    String root = folder.replaceAll("\\\\", "/").replaceAll("//", "/");
                    if (!root.endsWith("/")) {
                        root += File.separator;
                    }
                    String summaryRoot = root + summaryId;
                    String edocformRoot = summaryRoot + File.separator + "edocform"; // 文单输出目录
                    String edocbodyRoot = summaryRoot + File.separator + "edocbody"; // 正文(含花脸)输出目录
                    File sf = new File(summaryRoot);
                    if (sf.exists()) {
                        try {
                            FileUtils.deleteDirectory(sf);
                        } catch (IOException e) {
                            map.put("success", false);
                            map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());//文件操作失败！原因：
                        }
                    }
                    // 根据导出类型导出文单、正文(含花脸)
                    if (EXPORT_EDOC_ALL.equals(exportType)) {
                        map = this.write2File(Long.parseLong(summaryId), bodyIds, v3xList, edocbodyRoot);
                        edocFormManager.writeForm2File(Long.parseLong(summaryId), edocformRoot);
                    } else if (EXPORT_EDOC_FORM.equals(exportType)) {
                        edocFormManager.writeForm2File(Long.parseLong(summaryId), edocformRoot);
                    } else if (EXPORT_EDOC_BODY.equals(exportType)) {
                        map = this.write2File(Long.parseLong(summaryId), bodyIds, v3xList, edocbodyRoot);
                    }
                    if (map.isEmpty()) {
                        map.put("success", true);
                        map.put("msg", ResourceUtil.getString("edoc.alert.file.output.address.label") + summaryRoot);//文件输出地址为：
                    }
                    return map;
                } else {
                    throw new BusinessException(ResourceUtil.getString("edoc.alert.no.edoc.label"));//公文ID(summaryid)查询不到对应公文
                }
            } else {
                throw new BusinessException(ResourceUtil.getString("edoc.alert.summaryid.folder.null.label"));//公文ID(summaryid)及输出目录(folder)均不能为空
            }
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());
            return map;
        }
    }

    /**
     * 输出文件
     *
     * @param summaryId   公文ID
     * @param fileIds     正文及花脸ID集合
     * @param summaryRoot 输出的文件夹目录
     * @return
     */
    private Map<String, Object> write2File(Long summaryId, List<Long> bodyList, List<V3XFile> v3xList,
                                           String summaryRoot) {
        Map<String, Object> map = new HashMap<String, Object>();
        File sf = new File(summaryRoot);
        if (sf.mkdirs()) {
            String tempPath = sf + File.separator;
            String srcFile = "";
            String destFile = "";
            try {
                // 处理正文
                for (int i = 0; i < bodyList.size(); i++) {
                    V3XFile v3xfile = fileManager.getV3XFile(bodyList.get(i));
                    // 获取解密后的文档
                    File file = fileManager.getFile(bodyList.get(i), v3xfile.getCreateDate());
                    // 拷贝文件到目录
                    FileUtils.copyFileToDirectory(file, sf);
                    // 转换为可使用的文件
                    srcFile = tempPath + file.getName();
                    destFile = tempPath + "(正文" + (i + 1) + ")" + bodyList.get(i) + getOfficeSuffix(v3xfile);
                    map = file2StandardOffice(srcFile, destFile);
                }

                // 处理花脸
                if (null != v3xList && !v3xList.isEmpty()) {
                    for (int i = 0; i < v3xList.size(); i++) {
                        V3XFile v3xfile = v3xList.get(i);
                        // 获取解密后的文档
                        File file = fileManager.getFile(v3xfile.getId(), v3xfile.getCreateDate());
                        // 拷贝文件到目录
                        FileUtils.copyFileToDirectory(file, sf);
                        // 转换为可使用的文件
                        srcFile = tempPath + file.getName();
                        destFile = tempPath + "(花脸" + (i + 1) + ")" + v3xfile.getId() + getOfficeSuffix(v3xfile);
                        map = file2StandardOffice(srcFile, destFile);
                    }
                }
            } catch (BusinessException e) {
                map.put("success", false);
                map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());
            } catch (IOException e) {
                map.put("success", false);
                map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + e.getMessage());
            }
        }
        return map;
    }

    /**
     * 转换为可使用的文件
     *
     * @param srcFile  源文件
     * @param destFile 转换后的文件
     * @return
     */
    private Map<String, Object> file2StandardOffice(String srcFile, String destFile) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (Util.jinge2StandardOffice(srcFile, destFile)) {
            File stdTempFIle = new File(srcFile);
            if (stdTempFIle.exists() && stdTempFIle.isFile()) {
                // 删除文件
                try {
                    stdTempFIle.delete();
                } catch (SecurityException e) {
                    map.put("success", false);
                    map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + ResourceUtil.getString("edoc.alert.delete.file.failed.label"));//文件操作失败！原因：删除文件失败！
                }
            }
        } else {
            map.put("success", false);
            map.put("msg", ResourceUtil.getString("edoc.alert.file.operation.failed.label") + ResourceUtil.getString("edoc.alert.conversion.file.failed.label"));//文件操作失败！原因：转换文件失败！
        }
        return map;
    }

    /**
     * 转换文件后缀
     *
     * @param file
     * @return
     * @throws BusinessException
     */
    private String getOfficeSuffix(V3XFile file) throws BusinessException {
        String mimeType = "";
        String extension = null;
        if (file != null) {
            mimeType = file.getMimeType();
            if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType))
                extension = ".docx";
            else if ("application/msword".equals(mimeType))
                extension = ".doc";
            else if ("application/vnd.ms-excel".equals(mimeType))
                extension = ".xls";
            else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType))
                extension = ".xlsx";
            else if ("application/kswps".equals(mimeType))
                extension = ".wps";
            else if ("application/kset".equals(mimeType))
                extension = ".et";
            else if ("msoffice".equals(mimeType)) {
                if (file.getFilename() != null && file.getFilename().toLowerCase().endsWith(".doc")
                        || file.getFilename().toLowerCase().endsWith(".docx")) {
                    extension = ".doc";
                } else {
                    extension = "";
                }
            }
        }
        return extension;
    }

    /**
     * 移交功能
     *
     * @param params Map<String,Object> | 必填 | 其它参数
     * @return response
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transfer")
    public Response transfer(Map<String, Object> param) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long transferMemberId = ParamUtil.getLong(param, "transferMemberId", -1L);
        Long summaryId = ParamUtil.getLong(param, "summaryId", -1L);
        Long affairId = ParamUtil.getLong(param, "affairId", -1L);
        String opinionContent = ParamUtil.getString(param, "opinionContent");
        if (Strings.isNotBlank(opinionContent)) {
            opinionContent = Strings.removeEmoji(opinionContent);
        }
        Integer opinionAttibute = ParamUtil.getInt(param, "opinionAttibute", -1);
        Integer isTrack = ParamUtil.getInt(param, "isTrack", TrackEnum.no.ordinal());
        boolean isNewImg = checkisNewImg(param);
        if (isNewImg) {//是否有文单签批
            saveQianpiData(param, affairId, isNewImg);//文单签批
        }
        //移交的态度
        EdocOpinion transferOpinion = new EdocOpinion();
        transferOpinion.setAffairId(affairId);
        transferOpinion.setContent(opinionContent);
        transferOpinion.setAttribute(opinionAttibute);
        transferOpinion.setCreateUserId(user.getId());
        transferOpinion.setIdIfNew();
        transferOpinion.isPipeonhole = false;
        transferOpinion.setPolicy(ParamUtil.getString(param, "policy"));
        transferOpinion.setOpinionType(EdocOpinion.OpinionType.transferOpinion.ordinal());
        String returnValue = "true";
        String errMsg = "";
        CtpAffair affair = affairManager.get(affairId);
        if (affair != null) {
            transferOpinion.setNodeId(affair.getActivityId());
            transferOpinion.setPolicy(affair.getNodePolicy());
        }
//		EdocSummary summary = edocSummaryManager.findById(summaryId);
        EdocManagerModel edocManagerModel = new EdocManagerModel();
        edocManagerModel.setAffairId(affairId);
        edocManagerModel.setSignOpinion(transferOpinion);
        edocManagerModel.setSummaryId(summaryId);
        edocManagerModel.setUser(user);
        edocManagerModel.setAffairTrack(isTrack);

        //保存附件
        String fileJson = ParamUtil.getString(param, "fileJson", "[]");
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("fileJson", fileJson);
        try {
            errMsg = edocH5Manager.transEdocTransfer(edocManagerModel, transferMemberId, paramMap);
        } catch (BusinessException e) {
            returnValue = "false";
            errMsg = ResourceUtil.getString("edoc.transfer.error");
            LOGGER.error("H5公文移交异常", e);
        } finally {
            edocManager.unlockEdocAll(summaryId, null);
        }
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (Strings.isNotBlank(errMsg)) {
            returnValue = "false";
        }
        returnMap.put("returnValue", returnValue);
        returnMap.put("errMsg", errMsg);
        return getResponse(returnMap);

    }

    /**
     * 指定回退功能
     *
     * @param params Map<String,Object> | 必填 | 其它参数
     * @return response
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("specifiesReturn")
    public Response specifiesReturn(Map<String, Object> param) throws BusinessException {
        User user = AppContext.getCurrentUser();

        Long summaryId = ParamUtil.getLong(param, "summaryId", -1L);
        String content = ParamUtil.getString(param, "opinionContent", "");
        Long currentAffairId = ParamUtil.getLong(param, "affairId", -1L);
        String theStepBackNodeId = ParamUtil.getString(param, "theStepBackNodeId");
        String appName = ParamUtil.getString(param, "appName");
        String policy = ParamUtil.getString(param, "policy");
        String submitStyle = ParamUtil.getString(param, "submitStyle");
        EdocSummary summary = edocManager.getEdocSummaryById(summaryId, true);
        CtpAffair affair = affairManager.get(currentAffairId);

        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setCurrentWorkitemId(affair.getSubObjectId());
        context.setCaseId(affair.getCaseId());
        context.setProcessId(affair.getProcessId());
        context.setCurrentUserId(String.valueOf(user.getId()));
        context.setCurrentUserName(user.getName());
        context.setCurrentAccountId(String.valueOf(user.getLoginAccount()));
        context.setCurrentActivityId(String.valueOf(affair.getActivityId()));
        context.setBusinessData("EDOC_CONTENT_OP", content);
        context.setAppName(ApplicationCategoryEnum.edoc.name());
        context.setSelectTargetNodeId(theStepBackNodeId);// 被退回节点id
        context.setSubmitStyleAfterStepBack(submitStyle);// 退回方式 1直接提交给我 2流程重走
        context.setAppObject(summary);

        Integer attitude = ParamUtil.getInt(param, "opinionAttibute", com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL);
        EdocOpinion signOpinion = new EdocOpinion();
        signOpinion.setIdIfNew();
        signOpinion.setAttribute(attitude);
        signOpinion.setContent(content);
        signOpinion.isDeleteImmediate = false;// "delete".equals(afterSign);
        signOpinion.affairIsTrack = false;// "track".equals(afterSign);
        signOpinion.setNodeId(affair.getActivityId());
        signOpinion.setPolicy(affair.getNodePolicy());
        signOpinion.setAffairId(currentAffairId);
        signOpinion.setIsHidden(Strings.isNotBlank(ParamUtil.getString(param, "isHidden")));
        signOpinion.setOpinionType(EdocOpinion.OpinionType.backOpinion.ordinal());
        signOpinion.setEdocSummary(summary);

        Map<String, Object> tempMap = new HashMap<String, Object>();
        tempMap.put("summaryId", summaryId);
        tempMap.put("appName", appName);
        tempMap.put("submitStyle", submitStyle);
        tempMap.put("currentAffairId", currentAffairId);
        tempMap.put("selectTargetNodeId", theStepBackNodeId);
        tempMap.put("policy", policy);
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
                        DocApi docApi = (DocApi) AppContext.getBean("docApi");
                        if (null != docApi) {
                            docApi.deleteDocResources(user.getId(), ids);
                        }
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

        String errMsg = "";
        String returnValue = "true";

        try {
            edocManager.appointStepBack(tempMap);
        } catch (Exception e) {
            errMsg = "指定回退异常";
            LOGGER.error("specifiesReturn error:", e);

        } finally {
            edocManager.unlockEdocAll(summaryId, null);
        }

        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (Strings.isNotBlank(errMsg)) {
            returnValue = "false";
        }
        returnMap.put("returnValue", returnValue);
        returnMap.put("errMsg", errMsg);
        return getResponse(returnMap);

    }

    /**
     * [客开-徐矿公文撤销] 作者：shiZC
     * @param param
     * @return
     * @throws Exception
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("repealExternal")
    public Response repealExternal(Map<String, Object> param) throws Exception {
        param.put("docBack", "cancelColl");
        param.put("trackWorkflowType", "0");
        Map<String, Object> returnMap = new HashMap<String, Object>();
        User user = AppContext.getCurrentUser();
        String[] _summaryIds = new String[]{};
        String page = ParamUtil.getString(param, "page", "");
        StringBuilder info = new StringBuilder();
        /****/
        //String _trackWorkflowType = request.getParameter("trackWorkflowType");
        String _trackWorkflowType = ParamUtil.getString(param, "trackWorkflowType", "");

        //String _affairId = request.getParameter("affairId");
        String _affairId = ParamUtil.getString(param, "affairId", "");
        //String repealComment = request.getParameter("repealComment"); // 撤销附言
        String repealComment = ParamUtil.getString(param, "repealComment", ""); // 撤销附言
        // lijl添加,为了区分是撤销流程还是取回
        //String docBack = request.getParameter("docBack");// docBack取回/cancelColl撤销
        String docBack = ParamUtil.getString(param, "docBack", "");// docBack取回/cancelColl撤销
        // GOV-4082 发文流程撤销确定后没有反应。另发现撤销和退回拟稿人功能重复
        if (Strings.isBlank(docBack)) {
            docBack = "";
        }

        if ("workflowManager".equals(page)) {
            //String[] summaryIdArr = { request.getParameter("summaryId") };
            String[] summaryIdArr = {ParamUtil.getString(param, "summaryId", "")};
            _summaryIds = summaryIdArr;
        } else {
            //_summaryIds = request.getParameterValues("id");
            //id已，拼接
            _summaryIds = ParamUtil.getString(param, "id", "").split(",");
            if ("dealrepeal".equals(page)) {
                //repealComment = request.getParameter("content");
                repealComment = ParamUtil.getString(param, "content", "");
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
                                //rendJavaScript(response, sb.toString());
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
                                    //String policy = request.getParameter("policy");
                                    String policy = ParamUtil.getString(param, "policy", "");
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
					/*out.println("<script>");
					out.println("<!--");
					out.println("alert('" + alertStr + "');");
					out.println("//-->");
					out.println("</script>");
					return null;*/
                    returnMap.put("returnValue", false);
                    returnMap.put("errMsg", alertStr);
                    return getResponse(returnMap);
                }
            } else {
                String alertStr = ResourceBundleUtil.getString("com.seeyon.v3x.edoc.resources.i18n.EdocResource",
                        "edoc.back.flow.error");
				/*out.println("<script>");
				out.println("<!--");
				out.println("alert('" + alertStr + "');");
				out.println("//-->");
				out.println("</script>");
				return null;*/
                returnMap.put("returnValue", false);
                returnMap.put("errMsg", alertStr);
                return getResponse(returnMap);
            }

            //super.printV3XJS(out);
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
					/*out.println("<script>");
					out.println("<!--");
					out.println("alert(\"" + StringEscapeUtils.escapeJavaScript(alertStr) + "\");");
					out.println("//-->");
					out.println("</script>");*/
                    returnMap.put("returnValue", false);
                    returnMap.put("errMsg", StringEscapeUtils.escapeJavaScript(alertStr));
                    return getResponse(returnMap);
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
        //if ("workflowManager".equals(page)) {
			/*out.println("<script>");
			out.println("if(window.dialogArguments){"); // 弹出
			out.println("  window.returnValue = \"true\";");
			out.println("  window.close();");
			out.println("}else{");
			out.println("  parent.ok();");
			out.println("}");
			out.println("</script>");
			out.close();
			return null;*/
        returnMap.put("returnValue", true);
        returnMap.put("errMsg", StringEscapeUtils.escapeJavaScript(page));
        return getResponse(returnMap);
        //} else {
			/*out.println("<script>");
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
			return null;*/
			/*returnMap.put("returnValue", false);
			returnMap.put("errMsg","处理待办公文时点击 撤销 报错" );
			return getResponse(returnMap);
		}*/
    }

    /**
     * 解锁，公文提交或者暂存待办的时候进行解锁,与Ajax解锁一起，构成两次解锁，避免解锁失败，节点无法修改的问题出现
     *
     * @param userId
     * @param
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
     * [客开-徐矿集团] 公文发送接口 作者：shiZC
     * @param param
     * @return
     * @throws BusinessException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("sendExternal")
    public Response sendExternal(Map<String, Object> param) throws BusinessException {
        param.put("__ActionToken", "SEEYON_A8");
        param.put("caseId", "-1");
        param.put("deadlineSelect", "0");
        param.put("edocExchangeType", "0");
        param.put("edocType", "0");
        param.put("edocType_mark", "0");
        param.put("exchangeDeptType", "Creater");
        param.put("fromSend", "true");
        param.put("hasArchive", "false");
        param.put("isModifyAtt", "0");
        param.put("isModifyContent", "0");
        param.put("isModifyForm", "0");
        param.put("isTrack", "1");
        param.put("isUniteSend", "false");
        param.put("my:create_person", "test1");
        param.put("my:secret_level", "3");
        param.put("sVisorsFromTemplate", "false");
        param.put("standardDuration", "0");
        param.put("taohongriqiSwitch", "true");
        param.put("trackRange", "1");
        param.put("workflow_last_input", "false");
        User user = AppContext.getCurrentUser();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        //String processTypeStr = request.getParameter("processType");
        String processTypeStr = ParamUtil.getString(param, "page", "");
        //String workflowNodePeoplesInput = request.getParameter("workflow_node_peoples_input");
        String workflowNodePeoplesInput = ParamUtil.getString(param, "workflow_node_peoples_input", "");
        //String workflowNodeConditionInput = request.getParameter("workflow_node_condition_input");
        String workflowNodeConditionInput = ParamUtil.getString(param, "workflow_node_condition_input", "");
        Long processType = 0L;
        if (processTypeStr != null && !"".equals(processTypeStr)) {
            processType = Long.parseLong(processTypeStr);
        }
        /**
         * 表单ID
         */
        //String etable = request.getParameter("edoctable");
        String etable = ParamUtil.getString(param, "edoctable", "");
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
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", sb.toString());
            return getResponse(returnMap);
        }

        // 取得公文发送人的信息
        String comm = ParamUtil.getString(param, "comm", "");
        // 来文登记,更新登记时间，给签收人发送消息
		/*long registerId = Strings.isBlank(request.getParameter("registerId")) ? -1
				: Long.parseLong(request.getParameter("registerId"));*/
        long registerId = Strings.isBlank(ParamUtil.getString(param, "registerId", "")) ? -1
                : Long.parseLong(ParamUtil.getString(param, "registerId", ""));
        Long agentToId = null;
        EdocRegister edocRegister = null;
        //String backBoxToEdit = request.getParameter("backBoxToEdit");
        String backBoxToEdit = ParamUtil.getString(param, "backBoxToEdit", "");
        //String recieveIdStr = request.getParameter("recieveId");
        String recieveIdStr = ParamUtil.getString(param, "recieveId", "");
        //String edocType = request.getParameter("edocType");
        String edocType = ParamUtil.getString(param, "edocType", "");

        EdocSummary edocSummary = new EdocSummary();
        edocSummary.setSubject(ParamUtil.getString(param, "subject", ""));
        edocSummary.setText1(ParamUtil.getString(param, "my:text1", ""));
        edocSummary.setSendUnit(ParamUtil.getString(param, "my:send_unit", ""));
        edocSummary.setSendUnitId(ParamUtil.getString(param, "my:send_unit_id", ""));
        edocSummary.setDocMark(ParamUtil.getString(param, "my:doc_mark", ""));
        edocSummary.setCreatePerson(ParamUtil.getString(param, "my:create_person", ""));//创建人
        edocSummary.setSendDepartmentId(ParamUtil.getString(param, "my:send_department_id", ""));
        edocSummary.setSendDepartment(ParamUtil.getString(param, "my:send_department", ""));
        edocSummary.setIdIfNew();
        //bind(request, edocSummary);

        if ("1".equals(edocType)) {
            if (registerId != -1) {
                edocRegister = edocRegisterManager.findRegisterById(registerId);
            } else if (Strings.isNotBlank(recieveIdStr)) {
                edocRegister = edocRegisterManager.findRegisterByRecieveId(Long.parseLong(recieveIdStr));
            }

            if (edocRegister != null) {
                edocSummary.setSendTo(edocRegister.getSendTo());
            }


        }

        // 因为DataUtil中requestToSummary方法很多地方都在调用，公文发送性能优化时在requestToSummary内部将获取公文单的公文元素保存进了ThreadLocal中
        // 需要在这里向ThreadLocal中设置一个开关，当该方法调用requestToSummary时才将公文元素保存进了ThreadLocal中
        SharedWithThreadLocal.setCanUse();
        //DataUtil.requestToSummary(request, edocSummary, formId);

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
			/*int edocExchangeType = request.getParameter("edocExchangeType") == null ? -1
					: Integer.parseInt(request.getParameter("edocExchangeType"));*/
            int edocExchangeType = ParamUtil.getString(param, "edocExchangeType", "") == null ? -1
                    : Integer.parseInt(ParamUtil.getString(param, "edocExchangeType", ""));
            //String edocMangerID = request.getParameter("memberList");
            String edocMangerID = ParamUtil.getString(param, "memberList", "");
            String quickSendPigholeInfo = ""; // 快速发文归档成功后的提示
            // ***快速发文相关变量 end***

            //String deadlineDatetime = (String) request.getParameter("deadLineDateTime");
            String deadlineDatetime = ParamUtil.getString(param, "deadLineDateTime", "");
            if (!isQuickSend && Strings.isNotBlank(deadlineDatetime)) {
                edocSummary.setDeadlineDatetime(DateUtil.parse(deadlineDatetime, "yyyy-MM-dd HH:mm"));
            }

            // 新建公文页面流程期限这里加一个隐藏域，后台保存的是这里的值，因为如果从模板加载设了流程期限的话，就disabled了，后台就取不到值了
            //String deadline2 = request.getParameter("deadline2");
            String deadline2 = ParamUtil.getString(param, "deadline2", "");

            // OA-20265 调用格式模板，发送后报错。
            if (!isQuickSend && Strings.isNotBlank(deadline2)) {
                edocSummary.setDeadline(Long.parseLong(deadline2));
            }
            //String advanceRemind2 = request.getParameter("advanceRemind2");
            String advanceRemind2 = ParamUtil.getString(param, "advanceRemind2", "");
            if (!isQuickSend && Strings.isNotBlank(advanceRemind2)) {
                edocSummary.setAdvanceRemind(Long.parseLong(advanceRemind2));
            }

            // 设置公文类型,党务还是政务的
            //String edocGovType = request.getParameter("edocGovType");
            String edocGovType = ParamUtil.getString(param, "edocGovType", "");
            if ("party".equals(edocGovType)) {
                //String party = request.getParameter("my:party");
                String party = ParamUtil.getString(param, "my:party", "");
                edocSummary.setParty(party);
            } else if ("administrative".equals(edocGovType)) {
                //String administrative = request.getParameter("my:administrative");
                String administrative = ParamUtil.getString(param, "my:administrative", "");
                edocSummary.setAdministrative(administrative);
            }

            /***** puyc**区分 分发和拟文 分发，收文的summaryId *****/
            //String recSummaryIdStr = request.getParameter("recSummaryIdVal");
            String recSummaryIdStr = ParamUtil.getString(param, "recSummaryIdVal", "");
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
			/*if (Strings.isBlank(request.getParameter("my:send_unit"))) {
				edocSummary = setEdocDefaultSendInfo(edocSummary, user, "0");
			}*/
            if (Strings.isBlank(ParamUtil.getString(param, "my:send_unit", ""))) {
                edocSummary = setEdocDefaultSendInfo(edocSummary, user, "0");
            }
            //if (Strings.isBlank(request.getParameter("my:send_department"))) {
            if (Strings.isBlank(ParamUtil.getString(param, "my:send_department", ""))) {
                edocSummary = setEdocDefaultSendInfo(edocSummary, user, "1");
            }

            // OA-32875 系统管理员-枚举管理，公文类型、行文类型枚举引用之后显示为否（注意查看一下OA-29865中的修改方法）
            // 更新系统枚举引用
            //EdocHelper.updateEnumItemRef(request);

            // OA-17655 拟文时设置了跟踪，发送后被回退，在待发中编辑，进入到拟文页面，跟踪被取消了。应该保留原来的设置。
            // 设置summary中的跟踪，因为DataUtil.requestToSummary方法中取跟踪值有点问题，但该方法在处理回退时也被调用了，修改害怕引起其他问题
            //String isTrack = request.getParameter("isTrack");
            String isTrack = ParamUtil.getString(param, "isTrack", "");
            if (Strings.isNotBlank(isTrack)) {
                edocSummary.setCanTrack(Integer.parseInt(isTrack));
            }

            //String templeteId = request.getParameter("templeteId");
            String templeteId = ParamUtil.getString(param, "templeteId", "");
            if (Strings.isNotBlank(templeteId)) {
                CtpTemplate _curTemplate = new CtpTemplate();
                try {
                    _curTemplate = templeteManager.getCtpTemplate(Long.parseLong(templeteId));
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                    returnMap.put("returnValue", false);
                    returnMap.put("errMsg", str.toString());
                    return getResponse(returnMap);
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
                    returnMap.put("returnValue", false);
                    returnMap.put("errMsg", str.toString());
                    return getResponse(returnMap);
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
                    returnMap.put("returnValue", false);
                    returnMap.put("errMsg", str.toString());
                    return getResponse(returnMap);
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
                returnMap.put("returnValue", false);
                returnMap.put("errMsg", str.toString());
                return getResponse(returnMap);
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
                returnMap.put("returnValue", false);
                returnMap.put("errMsg", errMsg);
                return getResponse(returnMap);
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
            //if (null != request.getParameter("resend") && !"".equals(request.getParameter("resend"))) {
            if (null != ParamUtil.getString(param, "resend", "") && !"".equals(ParamUtil.getString(param, "resend", ""))) {
                sendType = EdocEnum.SendType.resend;
            }

            // 是否转发
            //if (null != request.getParameter("forward") && !"".equals(request.getParameter("forward"))) {
            if (null != ParamUtil.getString(param, "forward", "") && !"".equals(ParamUtil.getString(param, "forward", ""))) {
                sendType = EdocEnum.SendType.forward;
                // 是否转发意见
                //boolean isForwardOpinion = "true".equals(request.getParameter("isForwardOpinion"));
                boolean isForwardOpinion = "true".equals(ParamUtil.getString(param, "isForwardOpinion", ""));
                // 转发人附言
                //String additionalComment = request.getParameter("additionalComment");
                String additionalComment = ParamUtil.getString(param, "additionalComment", "");
                // 转发人追加的附件

                options.put("isForwardOpinion", isForwardOpinion);
                options.put("additionalComment", additionalComment);
            }

            //String note = request.getParameter("note");// 发起人附言
            String note = ParamUtil.getString(param, "note", "");// 发起人附言
            EdocOpinion senderOninion = new EdocOpinion();
            senderOninion.setContent(note);
            senderOninion.setIdIfNew();
            //String trackMode = request.getParameter("isTrack");
            String trackMode = ParamUtil.getString(param, "isTrack", "");
            boolean track = (!isQuickSend && "1".equals(trackMode)) ? true : false;
            // 跟踪
            //String trackMembers = request.getParameter("trackMembers");
            String trackMembers = ParamUtil.getString(param, "trackMembers", "");
            //String trackRange = request.getParameter("trackRange");
            String trackRange = ParamUtil.getString(param, "trackRange", "");
            // 如果设置了跟踪指定人，但是指定人为空，则把跟踪设置为false
            if (Strings.isNotBlank(trackRange) && "0".endsWith(trackRange) && Strings.isBlank(trackMembers)) {
                track = false;
            }
            senderOninion.affairIsTrack = track;
            senderOninion.setAttribute(1);
            senderOninion.setIsHidden(false);
            senderOninion.setCreateUserId(user.getId());
            senderOninion.setCreateTime(new Timestamp(System.currentTimeMillis()));
            //senderOninion.setPolicy(request.getParameter("policy"));
            senderOninion.setPolicy(ParamUtil.getString(param, "policy", ""));
            senderOninion.setOpinionType(EdocOpinion.OpinionType.senderOpinion.ordinal());
            senderOninion.setNodeId(0);

            EdocBody body = new EdocBody();
            //bind(request, body);
            body.setId(UUIDLong.longUUID());
            //String tempStr = request.getParameter("bodyType");
            String tempStr = ParamUtil.getString(param, "bodyType", "");
            body.setContentType(tempStr);
            //Date bodyCreateDate = Datetimes.parseDatetime(request.getParameter("bodyCreateDate"));
            Date bodyCreateDate = Datetimes.parseDatetime(ParamUtil.getString(param, "bodyCreateDate", ""));
            if (bodyCreateDate != null) {
                body.setCreateTime(new Timestamp(bodyCreateDate.getTime()));
            }
            // -------性能优化，该方法在新建公文单发送，编辑发送，退件箱编辑后发送都会调用，希望在新建公文发送时就不执行删除附件操作了
            //String hasSummaryId = request.getParameter("newSummaryId");
            String hasSummaryId = ParamUtil.getString(param, "newSummaryId", "");
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
                    //if (request.getParameter("my:send_unit") == null) {
                    if (ParamUtil.getString(param, "my:send_unit", "") == null || ParamUtil.getString(param, "my:send_unit", "") == "") {
                        String sendUnit = sendEdoc.getSendUnit();
                        String sendUnit2 = sendEdoc.getSendUnit2();
                        if (Strings.isNotBlank(sendUnit2) && !sendUnit2.equals(sendUnit)) {
                            sendUnit += "," + sendUnit2;
                        }
                        edocSummary.setSendUnit(sendUnit);
                    }
                }
            }

            //String waitRegister_recieveId = request.getParameter("waitRegister_recieveId");
            String waitRegister_recieveId = ParamUtil.getString(param, "waitRegister_recieveId", "");
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
                //String deadlineTime = request.getParameter("deadlineTime"); // 如果选的是流程期限具体时间，在这里进行运算，会更精确。
                String deadlineTime = ParamUtil.getString(param, "deadlineTime", ""); // 如果选的是流程期限具体时间，在这里进行运算，会更精确。
                Long deadlineValue = getMinValueByDeadlineTime(deadlineTime, edocSummary.getCreateTime());
                if (deadlineValue != -1L) {
                    edocSummary.setDeadline(deadlineValue);
                }
            }

            if (edocSummary.getStartTime() == null) {
                edocSummary.setStartTime(new Timestamp(System.currentTimeMillis()));
            }
            edocSummary.setStartUserId(agentToId == null ? user.getId() : agentToId);
            //edocSummary.setFormId(Long.parseLong(request.getParameter("edoctable")));
            edocSummary.setFormId(Long.parseLong(ParamUtil.getString(param, "edoctable", "")));
            V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, edocSummary.getStartUserId());
            edocSummary.setStartMember(member);
            // 如果公文单无登记人，自动赋上登记节为发起人。yangzd
            //if (request.getParameter("my:create_person") == null) {
            if (ParamUtil.getString(param, "my:create_person", "") == null || ParamUtil.getString(param, "my:create_person", "") == "") {
                edocSummary.setCreatePerson(user.getName());
            }
            // yangzd
            if (edocSummary.getOrgAccountId() == null) {
                edocSummary.setOrgAccountId(user.getLoginAccount());
            }
            edocSummary.setOrgDepartmentId(getEdocOwnerDepartmentId(edocSummary.getOrgAccountId(), agentToId));
            // OA-40757 收文登记簿查询，没有把公文的发文单位查询出来
            //if (edocSummary.getEdocType() == 1 && request.getParameter("my:send_unit") != null) {
            if (edocSummary.getEdocType() == 1 && ParamUtil.getString(param, "my:send_unit", "") != null) {
                // 当是纸质登记时，如果收文单中有发文单位的公文元素，需要在收文summary中设置发文单位
                //edocSummary.setSendUnit(request.getParameter("my:send_unit"));
                edocSummary.setSendUnit(ParamUtil.getString(param, "my:send_unit", ""));
            }
            // BUG_普通_V5_V5.1sp1_发文管理的快速发文，主送单位为机构组（含两个部门）。收文管理时调用模板，主送单位元素显示的是机构组名称，发送出去后再看显示的是单位名称了_20150123006723_20150127
            //if (edocSummary.getEdocType() == 1 && request.getParameter("my:send_to") != null) {
            if (edocSummary.getEdocType() == 1 && ParamUtil.getString(param, "my:send_to", "") != null) {
                //edocSummary.setSendTo(request.getParameter("my:send_to"));
                edocSummary.setSendTo(ParamUtil.getString(param, "my:send_to", ""));
            }

            body.setIdIfNew();
            if (body.getCreateTime() == null) {
                body.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
            body.setLastUpdate(new Timestamp(System.currentTimeMillis()));
            // test code end
            // 保存附件hassavedattachment
			/*String attaFlag = attachmentManager.create(ApplicationCategoryEnum.edoc, edocSummary.getId(),
					edocSummary.getId(), request);*/
			/*String attaFlag =attachmentManager.create(ApplicationCategoryEnum.edoc,edocSummary.getId(),edocSummary.getId());
			if (com.seeyon.ctp.common.filemanager.Constants.isUploadLocaleFile(attaFlag)) {
				edocSummary.setHasAttachments(true);

				// 拟文发送的时候,附件只保存附件,不保存关联文档
				*//*String[] filenames = request
						.getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_filename);
				String[] fileTypes = request
						.getParameterValues(com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_type);*//*
               String fileName=ParamUtil.getString(param,com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_filename,"");
				String[] filenames={};
               if(fileName!=null && fileName!=""){
				   filenames=fileName.split(",");
				}
				String fileType=ParamUtil.getString(param,com.seeyon.ctp.common.filemanager.Constants.FILEUPLOAD_INPUT_NAME_type,"");
				String[] fileTypes={};
				if(fileType!=null && fileType!=""){
					fileTypes=fileType.split(",");
				}
				edocSummary.setAttachments(EdocHelper.getAttachments(filenames, fileTypes));
			}*/
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
			/*superviseMap.put("supervisorIds", request.getParameter("supervisorId"));
			superviseMap.put("supervisorNames", request.getParameter("supervisors"));
			superviseMap.put("awakeDate", request.getParameter("awakeDate"));*/
            superviseMap.put("supervisorIds", ParamUtil.getString(param, "supervisorId", ""));
            superviseMap.put("supervisorNames", ParamUtil.getString(param, "supervisors", ""));
            superviseMap.put("awakeDate", ParamUtil.getString(param, "awakeDate", ""));
            //String title = request.getParameter("title");
            String title = ParamUtil.getString(param, "title", "");
            if (Strings.isBlank(title)) {
                //title = request.getParameter("superviseTitle");
                title = ParamUtil.getString(param, "superviseTitle", "");
            }
            superviseMap.put("title", title);
            edocSummary.setSuperviseMap(superviseMap);

            //String process_xml = request.getParameter("process_xml");
            String process_xml = ParamUtil.getString(param, "process_xml", "");
            //String templeteProcessId = request.getParameter("templeteProcessId");
            String templeteProcessId = ParamUtil.getString(param, "templeteProcessId", "");
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
                RecRelationAfterSendParam param1 = new RecRelationAfterSendParam();
                param1.setSummary(edocSummary);
                param1.setRegister(edocRegister);
                param1.setUser(user);
                param1.setProcessType(processType);
                param1.setRecieveId(recieveIdStr);
                param1.setWaitRegister_recieveId(waitRegister_recieveId);
                /**
                 * 保存收文summary数据后续处理(更新签收，登记数据状态)
                 */
                RecRelationHandlerFactory.getHandler().transAfterSendRec(param1);
            }

            /* puyc 关联收文 */
            Long sendSummaryId = edocSummary.getId();// 发文Id
			/*String relationRecIdStr = request.getParameter("relationRecId"); // 在分发的时候没有值
			String relationRec = request.getParameter("relationRecd");
			//// 待登记关联发文时，关联id用签收id
			String recieveId = request.getParameter("recieveId");
			String forwordType = request.getParameter("forwordType");
			String forwordtosend_recAffairId = request.getParameter("forwordtosend_recAffairId");*/
            String relationRecIdStr = ParamUtil.getString(param, "relationRecId", ""); // 在分发的时候没有值
            String relationRec = ParamUtil.getString(param, "relationRecd", "");
            //// 待登记关联发文时，关联id用签收id
            String recieveId = ParamUtil.getString(param, "recieveId", "");
            String forwordType = ParamUtil.getString(param, "forwordType", "");
            String forwordtosend_recAffairId = ParamUtil.getString(param, "forwordtosend_recAffairId", "");

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
            //String relationSend = request.getParameter("relationSend");
            String relationSend = ParamUtil.getString(param, "relationSend", "");
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
                            //edocMangerID = request.getParameter("returnDeptId");
                            edocMangerID = ParamUtil.getString(param, "returnDeptId", "");

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

            //String pageview = request.getParameter("pageview");
            String pageview = ParamUtil.getString(param, "pageview", "");
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
                //String openFrom = request.getParameter("openFrom");
                String openFrom = ParamUtil.getString(param, "openFrom", "");
                //if ("agent".equals(request.getParameter("app")) && edocRegister != null
                if ("agent".equals(ParamUtil.getString(param, "app", "")) && edocRegister != null
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
                    //String from = Strings.isBlank(request.getParameter("from")) ? "listSent" : request.getParameter("from");
                    String from = Strings.isBlank(ParamUtil.getString(param, "from", "")) ? "listSent" : ParamUtil.getString(param, "from", "");
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
            //rendJavaScript(response, sb.toString());
            returnMap.put("returnValue", true);
            returnMap.put("id", edocSummary.getId());
            returnMap.put("affairId", affairId);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            edocLockManager.unlock(edocSummary.getSubject(), user.getId());
            if (lockDocMarkSubject == null) {
                edocMarkLockManager.unlock(lockDocMark);
            }
        }
        // long endtime = System.currentTimeMillis();
        return getResponse(returnMap);
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

    private String registDocMark(Long summaryId, String markStr, int markNum, int edocType, boolean checkId,
                                 int markType) throws EdocMarkHistoryExistException {
        return newRegistDocMark(summaryId, markStr, markNum, edocType, checkId, markType, "");
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

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("insertCtpAttachment")
    public Response insertCtpAttachment(Map<String, Object> params) throws BusinessException {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("returnValue", true);
        Connection connection = null;
        PreparedStatement ps = null;
        //String reference = ParamUtil.getString(params, "reference");
        Long reference = ParamUtil.getLong(params, "reference", -1L);
        if (isStringNull(params.get("reference") + "")) {
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", "reference来源单据ID不能为空");
            return getResponse(returnMap);
        }
        String filename = ParamUtil.getString(params, "filename");
        if (isStringNull(params.get("filename") + "")) {
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", "filename文件名不能为空");
            return getResponse(returnMap);
        }
        Long file_url = ParamUtil.getLong(params, "file_url", -1L);
        if (isStringNull(params.get("file_url") + "")) {
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", "file_url文件id不能为空(ctp_file这个表的)");
            return getResponse(returnMap);
        }
        String mime_type = ParamUtil.getString(params, "mime_type");
        if (isStringNull(params.get("mime_type") + "")) {
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", "mime_type不能为空(ctp_file这个表里有)");
            return getResponse(returnMap);
        }
        Integer attachment_size = ParamUtil.getInt(params, "attachment_size");
        if (isStringNull(params.get("attachment_size") + "")) {
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", "attachment_size不能为空(ctp_file这个表里有)");
            return getResponse(returnMap);
        }
        Integer sort = ParamUtil.getInt(params, "sort");
        String insertsql = "insert into ctp_attachment(id, reference, sub_reference, category, type, filename, file_url," +
                " mime_type, createdate, attachment_size, sort) values(?,?,?,?,?,?,?,?,?,?,?)";
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(insertsql);
            ps.setLong(1, UUIDLong.longUUID());
            ps.setLong(2, reference);
            ps.setLong(3, reference);
            ps.setInt(4, 4);
            ps.setInt(5, 0);
            ps.setString(6, filename);
            ps.setLong(7, file_url);
            ps.setString(8, mime_type);
            ps.setTimestamp(9, new Timestamp(new Date().getTime()));
            ps.setInt(10, attachment_size);
            ps.setInt(11, sort);
            ps.executeUpdate();
        } catch (Exception e) {
            returnMap.put("returnValue", false);
            returnMap.put("errMsg", e.getMessage());
        } finally {
            closeUtil(connection, ps, null);
        }
        return getResponse(returnMap);

    }

    public static long nextLong() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmsss");
        Date dt1 = new Date();
        String ss = "";
        try {
            Math.random();
            ss = df.format(dt1);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Random r = new Random(10);
        int ran1 = r.nextInt(10000);
        return Long.parseLong(ss + ran1);
    }

    public void closeUtil(Connection connection, PreparedStatement ps, ResultSet rs) {
        try {
            if (null != ps) {
                ps.close();
            }
            if (null != rs) {
                rs.close();
            }
            if (null != connection) {
                connection.close();
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private Boolean isStringNull(String s) {
        if (s == null || s.trim().equals("") || s.trim().equals("null"))
            return true;
        return false;
    }

}
