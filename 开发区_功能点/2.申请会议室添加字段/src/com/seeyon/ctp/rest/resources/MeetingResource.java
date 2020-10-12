package com.seeyon.ctp.rest.resources;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.seeyon.apps.meeting.constants.MeetingRoleEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.common.manager.MeetingLockManager;
import com.seeyon.apps.meeting.api.MeetingApi;
import com.seeyon.apps.meeting.api.MeetingVideoManager;
import com.seeyon.apps.meeting.bo.MeetingBO;
import com.seeyon.apps.meeting.bo.MtReplyBO;
import com.seeyon.apps.meeting.bo.MtSummaryBO;
import com.seeyon.apps.meeting.constants.MeetingBarCodeConstant;
import com.seeyon.apps.meeting.constants.MeetingConstant;
import com.seeyon.apps.meeting.constants.MeetingConstant.DateFormatEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingActionEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingCategoryEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingNatureEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingTypeCategoryEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomAppStateEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomAttEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.SummaryBusinessTypeEnum;
import com.seeyon.apps.meeting.constants.MeetingListConstant.ListTypeEnum;
import com.seeyon.apps.meeting.constants.MeetingListConstant.MeetingListTypeEnum;
import com.seeyon.apps.meeting.constants.MeetingListType4XiaoZhiEnum;
import com.seeyon.apps.meeting.constants.MeetingUserAgent;
import com.seeyon.apps.meeting.constants.MeetingUserType;
import com.seeyon.apps.meeting.event.MeetingReplyEvent;
import com.seeyon.apps.meeting.manager.ConfereesConflictManager;
import com.seeyon.apps.meeting.manager.MeetingApplicationHandler;
import com.seeyon.apps.meeting.manager.MeetingBarCodeManager;
import com.seeyon.apps.meeting.manager.MeetingListManager;
import com.seeyon.apps.meeting.manager.MeetingManager;
import com.seeyon.apps.meeting.manager.MeetingNewManager;
import com.seeyon.apps.meeting.manager.MeetingReplyManager;
import com.seeyon.apps.meeting.manager.MeetingResourcesManager;
import com.seeyon.apps.meeting.manager.MeetingSettingManager;
import com.seeyon.apps.meeting.manager.MeetingSummaryManager;
import com.seeyon.apps.meeting.manager.MeetingTypeManager;
import com.seeyon.apps.meeting.manager.MeetingValidationManager;
import com.seeyon.apps.meeting.outer.MeetingM3Manager;
import com.seeyon.apps.meeting.outer.MeetingRoomM3Manager;
import com.seeyon.apps.meeting.po.MeetingQrcodeSign;
import com.seeyon.apps.meeting.po.MeetingResources;
import com.seeyon.apps.meeting.po.MeetingScreenSet;
import com.seeyon.apps.meeting.po.MeetingSummary;
import com.seeyon.apps.meeting.po.MeetingType;
import com.seeyon.apps.meeting.util.MeetingHelper;
import com.seeyon.apps.meeting.util.MeetingOrgHelper;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meeting.vo.MeetingListVO;
import com.seeyon.apps.meeting.vo.MeetingMemberVO;
import com.seeyon.apps.meeting.vo.MeetingNewVO;
import com.seeyon.apps.meetingroom.manager.MeetingRoomAppManager;
import com.seeyon.apps.meetingroom.manager.MeetingRoomListManager;
import com.seeyon.apps.meetingroom.manager.MeetingRoomManager;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.apps.meetingroom.po.MeetingRoomApp;
import com.seeyon.apps.meetingroom.po.MeetingRoomPerm;
import com.seeyon.apps.meetingroom.util.MeetingRoomRoleUtil;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomListVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomOccupancyVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomVO;
import com.seeyon.apps.taskmanage.api.TaskmanageApi;
import com.seeyon.apps.taskmanage.util.MenuPurviewUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.mainbody.handler.impl.HtmlMainbodyHandler;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.FileSecurityManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.office.trans.util.OfficeTransHelper;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.common.publicqrcode.manager.PublicQrCodeManager;
import com.seeyon.ctp.common.publicqrcode.po.PublicQrCodePO;
import com.seeyon.ctp.common.screenshot.manager.ScreenShotManager;
import com.seeyon.ctp.common.screenshot.manager.ScreenShotManager.HtmlRenderType;
import com.seeyon.ctp.common.screenshot.manager.ScreenShotManager.ScreenShotParam;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.usermessage.UserMessageUtil;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgVisitor;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.BeanUtils;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation.External;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.common.security.SecurityCheck;
import com.seeyon.v3x.common.security.SecurityCheckParam;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.meeting.contants.MeetingMessageTypeEnum;
import com.seeyon.v3x.meeting.domain.MtComment;
import com.seeyon.v3x.meeting.domain.MtMeeting;
import com.seeyon.v3x.meeting.domain.MtReply;
import com.seeyon.v3x.meeting.domain.MtReplyWithAgentInfo;
import com.seeyon.v3x.meeting.manager.MtMeetingManager;
import com.seeyon.v3x.meeting.manager.MtReplyManager;
import com.seeyon.v3x.meeting.util.Constants;
import com.seeyon.v3x.meeting.util.DataTransUtil;
import com.seeyon.v3x.meeting.util.MeetingEnum;
import com.seeyon.v3x.meeting.util.MeetingMsgHelper;

/**
 * REST 会议资源
 *
 * @author Zhangc
 */
@Path("meeting")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces(MediaType.APPLICATION_JSON)
public class MeetingResource extends BaseResource {
    private static final Log LOGGER = LogFactory.getLog(MeetingResource.class);
    private MtMeetingManager mtMeetingManager = (MtMeetingManager) AppContext.getBean("mtMeetingManager");
    private MeetingManager meetingManager = (MeetingManager) AppContext.getBean("meetingManager");
    private MeetingM3Manager meetingM3Manager = (MeetingM3Manager) AppContext.getBean("meetingM3Manager");
    private MeetingRoomM3Manager meetingRoomM3Manager = (MeetingRoomM3Manager) AppContext.getBean("meetingRoomM3Manager");
    private OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
    private AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");
    private UserMessageManager userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
    ;
    private AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private MeetingApi meetingApi = (MeetingApi) AppContext.getBean("meetingApi");
    private MenuPurviewUtil menuPurviewUtil = (MenuPurviewUtil) AppContext.getBean("menuPurviewUtil");
    private MeetingRoomManager meetingRoomManager = (MeetingRoomManager) AppContext.getBean("meetingRoomManager");
    private MeetingNewManager meetingNewManager = (MeetingNewManager) AppContext.getBean("meetingNewManager");
    private MeetingResourcesManager meetingResourcesManager = (MeetingResourcesManager) AppContext.getBean("meetingResourcesManager");
    private MeetingApplicationHandler meetingApplicationHandler = (MeetingApplicationHandler) AppContext.getBean("meetingApplicationHandler");
    private MeetingRoomListManager meetingRoomListManager = (MeetingRoomListManager) AppContext.getBean("meetingRoomListManager");
    private MeetingSummaryManager meetingSummaryManager = (MeetingSummaryManager) AppContext.getBean("meetingSummaryManager");
    private MeetingValidationManager meetingValidationManager = (MeetingValidationManager) AppContext.getBean("meetingValidationManager");
    private ConfereesConflictManager confereesConflictManager = (ConfereesConflictManager) AppContext.getBean("confereesConflictManager");
    private MeetingLockManager meetingLockManager = (MeetingLockManager) AppContext.getBean("meetingLockManager");
    private MeetingReplyManager meetingReplyManager = (MeetingReplyManager) AppContext.getBean("meetingReplyManager");
    private TaskmanageApi taskmanageApi = (TaskmanageApi) AppContext.getBean("taskmanageApi");
    private MtReplyManager replyManager = (MtReplyManager) AppContext.getBean("replyManager");
    private MeetingTypeManager meetingTypeManager = (MeetingTypeManager) AppContext.getBean("meetingTypeManager");
    private MeetingListManager meetingListManager = (MeetingListManager) AppContext.getBean("meetingListManager");
    private MeetingSettingManager meetingSettingManager = (MeetingSettingManager) AppContext.getBean("meetingSettingManager");
    private MeetingBarCodeManager meetingBarCodeManager = (MeetingBarCodeManager) AppContext.getBean("meetingBarCodeManager");
    private FileSecurityManager fileSecurityManager = (FileSecurityManager) AppContext.getBean("fileSecurityManager");
    private ConfigManager configManager = (ConfigManager) AppContext.getBean("configManager");
    private PublicQrCodeManager publicQrCodeManager = (PublicQrCodeManager) AppContext.getBean("publicQrCodeManager");
    private MeetingRoomAppManager meetingRoomAppManager = (MeetingRoomAppManager) AppContext.getBean("meetingRoomAppManager");
    private AppLogManager appLogManager = (AppLogManager) AppContext.getBean("appLogManager");
    private ScreenShotManager screenShotManager = (ScreenShotManager) AppContext.getBean("screenShotManager");


    private static final String RETURN_ERROR_MESSGAE = "errorMsg";
    private static final String RETURN_DATA = "data";
    private static final String SUCCESS_KEY = "success";

    private static final Lock LOCK = new ReentrantLock();

    /*************************************  会议列表Rest接口 start ***********************************************/

    /**
     * 获取会议待办列表<BR>
     *
     * @param pageMap 分页信息
     *                <pre>
     *                       类型    名称             必填     备注
     *                       String   pageNo     Y     页数(1,2,3...)
     *                       String   pageSize   Y     每页显示条数
     *                 </pre>
     * @return <pre>
     * {
     * 	code: 0, //0表示成功
     * 	message: '',
     * 	data: {
     * 		page:"",
     * 		size:"",
     * 		data:{id,title,createUser,beginDate,endDate...} //更多字段可参考com.seeyon.ctp.rest.resources.MeetingListRestVO
     *        }
     *    }
     *
     * </pre>
     * @throws BusinessException
     */
    @POST
    @Path("findPendingMeetings")
    @RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.1")
    public Response findPendingMeetings(Map<String, String> pageMap) throws BusinessException {
        String listType = ListTypeEnum.listPendingMeeting.name();
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo = meetingM3Manager.findM3MeetingList(listType, flipInfo, pageMap);
        List<MeetingListVO> meetingList = (List<MeetingListVO>) flipInfo.getData();
        List<MeetingListRestVO> meetingVOList = new ArrayList<MeetingListRestVO>();
        for (MeetingListVO meeting : meetingList) {
            MeetingListRestVO meetingListRestVO = new MeetingListRestVO();
            BeanUtils.convert(meetingListRestVO, meeting);

            if (meeting.getProxyId() != -1) {
                meetingListRestVO.setProxyName(orgManager.getMemberById(meeting.getProxyId()).getName());
            }

            //6.1之前的版本可能同时存在room于meet_place，兼容如果存在room则清空meet_place
            if (Strings.isNotEmpty(meetingListRestVO.getRoomName())) {
                meetingListRestVO.setMeetPlace("");
            }

            //告知人不显示会议纪要图标
            if (meeting.getIsImpart()) {
                meetingListRestVO.setBusinessType(SummaryBusinessTypeEnum.impart.key());
            }

            meetingVOList.add(meetingListRestVO);
        }

        flipInfo.setData(meetingVOList);
        return success(flipInfo);
    }

    /**
     * 获取会议已办列表<BR>
     *
     * @param pageMap 分页信息
     *                <pre>
     *                       类型    名称           必填     备注
     *                       String   pageNo    Y 	页数(1,2,3...)
     *                       String   pageSize  Y 	每页显示条数
     *                </pre>
     * @return <pre>
     * {
     * 	page:"",
     * 	size:"",
     * 	data:{id,title,createUser,beginDate,endDate...} //更多字段可参考com.seeyon.ctp.rest.resources.MeetingListRestVO
     * }
     * </pre>
     * @throws BusinessException
     */
    @POST
    @Path("findDoneMeetings")
    @RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.1")
    public Response findDoneMeetings(Map<String, String> pageMap) throws BusinessException {
        String listType = ListTypeEnum.listDoneMeeting.name();

        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo = meetingM3Manager.findM3MeetingList(listType, flipInfo, pageMap);
        List<MeetingListVO> meetingList = (List<MeetingListVO>) flipInfo.getData();

        List<MeetingListRestVO> meetingVOList = new ArrayList<MeetingListRestVO>();
        for (MeetingListVO meeting : meetingList) {
            MeetingListRestVO meetingListRestVO = new MeetingListRestVO();
            BeanUtils.convert(meetingListRestVO, meeting);
            //by 吴晓菊、 OA-113121M3：会议已开列表，代理的数据，代理人显示成了null
            if (meeting.getProxyId() != null && meeting.getProxyId() != -1) {
                meetingListRestVO.setProxyName(orgManager.getMemberById(meeting.getProxyId()).getName());
            }

            //6.1之前的版本可能同时存在room于meet_place，兼容如果存在room则清空meet_place
            if (Strings.isNotEmpty(meetingListRestVO.getRoomName())) {
                meetingListRestVO.setMeetPlace("");
            }

            //告知人不显示会议纪要图标
            if (meeting.getIsImpart()) {
                meetingListRestVO.setBusinessType(SummaryBusinessTypeEnum.impart.key());
            }

            meetingVOList.add(meetingListRestVO);
        }

        flipInfo.setData(meetingVOList);

        return ok(flipInfo);
    }

    /**
     * 查询已发会议
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               String	|	pageNo	|	Y	|	页数
     *               String	|	pageSize|	Y	|	每页显示条数
     *               </per>
     *               @return
     *               <pre>
     *               {
     *                code: 0, //0表示成功
     *                message: '',
     *                data: {
     *               		page:"",
     *               		size:"",
     *               		data:{id,title,createUser,beginDate,endDate...} //更多字段可参考com.seeyon.ctp.rest.resources.MeetingListRestVO
     *                    }
     *               }
     *               </pre>
     * @throws BusinessException
     * @author wulin v6.1 2017-01-06
     */
    @POST
    @Path("findSentMeetings")
    @RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.1")
    public Response findSentMeetings(Map<String, String> pageMap) throws BusinessException {
        User user = AppContext.getCurrentUser();
        String listType = ListTypeEnum.listSendMeeting.name();

        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo = meetingM3Manager.findM3MeetingList(listType, flipInfo, pageMap);
        List<MeetingListVO> meetingList = (List<MeetingListVO>) flipInfo.getData();
        List<MeetingListRestVO> meetingVOList = new ArrayList<MeetingListRestVO>();
        for (MeetingListVO meeting : meetingList) {
            MeetingListRestVO meetingListRestVO = new MeetingListRestVO();
            BeanUtils.convert(meetingListRestVO, meeting);
            meetingListRestVO.setCreateUserName(user.getName());

            //6.1之前的版本可能同时存在room于meet_place，兼容如果存在room则清空meet_place
            if (Strings.isNotEmpty(meetingListRestVO.getRoomName())) {
                meetingListRestVO.setMeetPlace("");
            }
            meetingVOList.add(meetingListRestVO);
        }

        flipInfo.setData(meetingVOList);
        return success(flipInfo);
    }

    /**
     * 查询待发会议
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               String	|	pageNo	|	Y	|	页数
     *               String	|	pageSize|	Y	|	每页显示条数
     *               </per>
     *               @return
     *               <pre>
     *               FlipInfo对象
     *               {
     *                code: 0,
     *                message: '',
     *                data: {
     *               		page:"",
     *               		size:"",
     *               		data:{id,title,createUser,beginDate,endDate...} //更多字段可参考com.seeyon.ctp.rest.resources.MeetingListRestVO
     *                    }
     *               }
     *               </pre>
     * @throws BusinessException
     * @author wulin v6.1 2017-01-06
     */
    @POST
    @Path("findWaitSentMeetings")
    @RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.1")
    public Response findWaitSentMeetings(Map<String, String> pageMap) throws BusinessException {
        User user = AppContext.getCurrentUser();
        String listType = ListTypeEnum.listWaitSendMeeting.name();

        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo = meetingM3Manager.findM3MeetingList(listType, flipInfo, pageMap);
        List<MeetingListVO> meetingList = (List<MeetingListVO>) flipInfo.getData();
        List<MeetingListRestVO> meetingVOList = new ArrayList<MeetingListRestVO>();
        for (MeetingListVO meeting : meetingList) {
            MeetingListRestVO meetingListRestVO = new MeetingListRestVO();
            BeanUtils.convert(meetingListRestVO, meeting);
            meetingListRestVO.setCreateUserName(user.getName());

            //6.1之前的版本可能同时存在room于meet_place，兼容如果存在room则清空meet_place
            if (Strings.isNotEmpty(meetingListRestVO.getRoomName())) {
                meetingListRestVO.setMeetPlace("");
            }
            meetingVOList.add(meetingListRestVO);
        }

        flipInfo.setData(meetingVOList);
        return success(flipInfo);
    }

    /**
     * 小致会议列表分类查询
     *
     * @param params 传入参数
     *               <pre>
     *               类型 		|	名称			|  必填	|	备注
     *               String	|	pageNo		|	Y	|	页数
     *               String	|	pageSize	|	Y	|	每页显示条数
     *               String	|	status		|	Y	|	会议状态(多个状态逗号分隔，例如pending,sent)
     *               String	|	createUser	|	N	|	会议发起人名称
     *               </per>
     *               @return
     *               <pre>
     *               {
     *               	    "code": 200,
     *               	    "data": {
     *               	        "total": 5,
     *               	        "data": [
     *                            {
     *               	                "data": [],
     *               	                "title": "待开会议",
     *               	                "status": "pending"
     *                            },
     *                            {
     *               	                "data": [],
     *               	                "title": "已发会议",
     *               	                "status": "sent"
     *                            }
     *               	        ]
     *                    },
     *               	    "message": "为你找到5条会议数据，您可以点击进行穿透查看"
     *                }
     *               </pre>
     * @throws BusinessException
     * @author fengshb V7.1SP1 2019-05-05
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("findMeetings4XiaoZhi")
    @RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.1SP1")
    public Response findMeetings4XiaoZhi(Map<String, String> params) throws BusinessException {

        params.put("queryFrom", "xiaozhi");
        params.put("pageSize", ParamUtil.getString(params, "pageSize", "5"));
        params.put("pageNo", ParamUtil.getString(params, "pageNo", "1"));
        String listType[] = ParamUtil.getString(params, "status", "").split(",");
        int total = 0;
        Map<String, Object> resultMap = null;
        List<Map<String, Object>> mtList = Lists.newArrayList();

        List<MeetingType> mtTypeList = meetingTypeManager.getMeetingTypeList(AppContext.currentAccountId());
        Map<Long, String> allMeetingTypeMap = new HashMap<Long, String>();
        if (CollectionUtils.isNotEmpty(mtTypeList)) {
            for (MeetingType type : mtTypeList) {
                allMeetingTypeMap.put(type.getId(), ResourceUtil.getString(type.getName()));
            }
        }

        for (String type : listType) {
            Map<String, Object> thisTypeMap = new HashMap<String, Object>();
            List<Map<String, Object>> resultList = Lists.newArrayList();
            thisTypeMap.put("title", MeetingListType4XiaoZhiEnum.getEnumByKey(type).getName());
            thisTypeMap.put("status", type);

            FlipInfo flipInfo = super.getFlipInfo();
            flipInfo = meetingM3Manager.findM3MeetingList(MeetingListType4XiaoZhiEnum.getEnumByKey(type).getValue(), flipInfo, params);
            List<MeetingListVO> meetingList = (List<MeetingListVO>) flipInfo.getData();

            Map<String, Object> meeting = null;
            Map<String, Object> gotoParamsMap = null;
            Map<String, Object> queryParams = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(meetingList)) {
                for (MeetingListVO vo : meetingList) {
                    meeting = new HashMap<String, Object>();
                    meeting.put("title", vo.getTitle());
                    meeting.put("createUser", vo.getCreateUser());
                    meeting.put("createUserName", OrgHelper.showMemberName(vo.getCreateUser()));
                    meeting.put("createDate", vo.getCreateDate().getTime());
                    meeting.put("beginDate", vo.getBeginDate().getTime());
                    meeting.put("endDate", vo.getEndDate().getTime());
                    meeting.put("meetPlace", Strings.isBlank(vo.getRoomName()) ? vo.getMeetPlace() : vo.getRoomName());
                    meeting.put("joinCount", vo.getJoinCount());
                    meeting.put("unjoinCount", vo.getUnjoinCount());
                    meeting.put("pendingCount", vo.getPendingCount());
                    meeting.put("hasAttachments", vo.getHasAttachments());
                    meeting.put("feedbackFlag", vo.getFeedbackFlag());
                    gotoParamsMap = Maps.newHashMap();
                    gotoParamsMap.put("meetingId", vo.getId());
                    gotoParamsMap.put("appId", ApplicationCategoryEnum.meeting.key() + "");
                    meeting.put("gotoParams", gotoParamsMap);
                    meeting.put("icon", vo.getCreateUser());
                    resultList.add(meeting);
                }
            }
            /**
             * 将查询条件返回给小致，方便其穿透时传递给M3会议列表页面作为查询条件
             */
            for (Map.Entry<String, String> param : params.entrySet()) {
                String pKey = param.getKey();
                if (!"queryFrom".equals(pKey) && !"pageSize".equals(pKey) && !"pageNo".equals(pKey)) {
                    queryParams.put(pKey, param.getValue());
                }
            }
            queryParams.put("status", type);
            queryParams.put("sourceType", ApplicationCategoryEnum.xiaoz.key());
            thisTypeMap.put("queryParams", queryParams);
            thisTypeMap.put("data", resultList);
            total = total + resultList.size();
            mtList.add(thisTypeMap);
        }
        String message = null;
        if (total > 0) {
            resultMap = new HashMap<String, Object>();
            resultMap.put("total", total);
            resultMap.put("data", mtList);
            message = ResourceUtil.getString("meeting.find.num.data");
        } else {
            message = ResourceUtil.getString("meeting.no.found.data");
        }
        return success(resultMap, message, 200);
    }

    /**
     * 小致根据关联ID查询单个会议信息
     *
     * @param params 传入参数
     *               <pre>
     *               类型 		|	名称			|  必填	|	备注
     *               Integer	|	sourceType	|	Y	|	应用类型（见ApplicationCategoryEnum）
     *               Long		|	sourceId	|	Y	|	关联ID
     *               </per>
     *               @return
     *               <pre>
     *               FlipInfo对象
     *               {
     *               "code": 200,
     *               "data": [
     *                  {
     *                     "sourceId": 883740,
     *                     "beginDate": 1556985600000,
     *                     "createUserId": 2456314066674470016,
     *                     "endDate": 1557036000000,
     *                     "showTime": "05-05 00:00-14:00",
     *                     "createUserName": "张三",
     *                     "meetingId": -6677196071911929330,
     *                     "title": "xnjdw基地51404"
     *                  }
     *                ],
     *               "message": "好的,已创建好了会议。"
     *               }
     *               </pre>
     * @throws BusinessException
     * @author fengshb V7.1SP1 2019-05-05
     */
    @POST
    @Path("findMeetingsByRelaId")
    @RestInterfaceAnnotation(OpenExternal = External.YES, StartVersion = "V7.1SP1")
    public Response findMeetingsByRelaId(Map<String, String> params) throws BusinessException {
        Integer sourceType = ParamUtil.getInt(params, "sourceType", -1);
        Long sourceId = ParamUtil.getLong(params, "sourceId", -1L);
        List<MtMeeting> meetings = meetingManager.findMeetingsByRelaId(sourceType, sourceId);
        List<Map<String, Object>> mtData = null;
        Integer mtState = null;
        if (CollectionUtils.isNotEmpty(meetings)) {
            mtData = Lists.newArrayList();
            MtMeeting mt = meetings.get(0);
            mtState = mt.getState();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", mt.getTitle());
            map.put("createUserId", mt.getCreateUser());
            map.put("createUserName", mt.getCreateUserName());
            map.put("beginDate", mt.getBeginDate().getTime());
            map.put("endDate", mt.getEndDate().getTime());
            map.put("showTime", formatShowTime4XiaoZhi(mt.getBeginDate(), mt.getEndDate()));
            map.put("sourceId", mt.getSourceId());
            map.put("meetingId", mt.getId());
            map.put("state", mt.getState());
            mtData.add(map);
        }
        String message = null;
        if (mtData != null && mtData.size() > 0) {
            if (MeetingConstant.MeetingStateEnum.save.key() == mtState) {
                message = ResourceUtil.getString("meeting.msg.xiaoz.savetips");
            } else {
                message = ResourceUtil.getString("meeting.msg.xiaoz.sendtips");
            }
        } else {
            message = ResourceUtil.getString("meeting.no.found.data");
        }
        return success(mtData, message, 200);
    }


    /**
     * 获取会议待办列表<BR>
     * method:POST<BR>
     * Path("pendings/{personid}")<BR>
     *
     * @param personId 人员ID 非必填  当personId为空时，获取当前登录人员的待办列表
     * @param pageMap  参数map对象
     *                 <pre>
     *                 类型               名称                     必填                        备注
     *                 Object   pageNo      N           当前第几页  如果分页信息不传递，取默认
     *                 Object   pageSize    N           每页显示多少条数据 如果分页信息不传递，取默认
     *                 </pre>
     * @return <pre>
     *  会议待办列表
     * <pre>
     */
    @POST
    @Path("pendings/{personid}")
    @RestInterfaceAnnotation
    public Response getPendingMeetings(@PathParam("personid") Long personId, Map<String, Object> pageMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            String listType = ListTypeEnum.listPendingMeeting.name();
            Integer pageSize = pageMap == null || pageMap.get("pageSize") == null ? 20 : (Integer) pageMap.get("pageSize");
            Integer pageNo = pageMap == null || pageMap.get("pageNo") == null ? 1 : (Integer) pageMap.get("pageNo");
            resultMap = meetingM3Manager.findM3MeetingList(listType, personId, pageSize, pageNo);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(resultMap);
    }

    /**
     * 获取会议待办列表<BR>
     * method:GET<BR>
     * <pre>
     * Long personId 人员ID            非必填    当personId为空时，获取当前登录人员的待办列表
     * String pageNo   当前第几页                   非必填   如果分页信息不传递，取默认
     * String pageSize 每页显示多少条数据    非必填   如果分页信息不传递，取默认
     * </pre>
     *
     * @return <pre>
     *  会议待办列表
     * <pre>
     */
    @GET
    @Path("pendings")
    @RestInterfaceAnnotation
    public Response getPendingMeetings(@QueryParam("personid") Long personId, @QueryParam("pageNo") String pageNo, @QueryParam("pageSize") String pageSize) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            pageSize = pageSize == null ? "20" : pageSize;
            pageNo = pageNo == null ? "1" : pageNo;
            String listType = ListTypeEnum.listPendingMeeting.name();
            resultMap = meetingM3Manager.findM3MeetingList(listType, personId, Integer.parseInt(pageSize), Integer.parseInt(pageNo));

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(resultMap);
    }

    /**
     * 获取会议已发列表<BR>
     *
     * @param personId 人员Id 非必填    当personId为空时，获取当前登录人员的会议已发数量
     * @param pageMap  <pre>
     *                        类型    名称           必填     备注
     *                        Object   pageNo    N 	页数(1,2,3...),如果分页信息不传递，取默认
     *                        Object   pageSize  N 	每页显示条数,如果分页信息不传递，取默认
     *                 </pre>
     * @return <pre>
     * 	 已发会议列表数据
     * </pre>
     */
    @POST
    @Path("sends/{personid}")
    @RestInterfaceAnnotation
    public Response getSendMeetings(@PathParam("personid") Long personId, Map<String, Object> pageMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {
            String listType = ListTypeEnum.listSendMeeting.name();
            Integer pageSize = pageMap.get("pageSize") == null ? 20 : (Integer) pageMap.get("pageSize");
            Integer pageNo = pageMap.get("pageNo") == null ? 1 : (Integer) pageMap.get("pageNo");
            resultMap = meetingM3Manager.findM3MeetingList(listType, personId, pageSize, pageNo);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(resultMap);
    }

    /**
     * 获取会议待发列表<BR>
     *
     * @param personId 人员Id 非必填    当personId为空时，获取当前登录人员的会议待发列表
     * @param pageMap  <pre>
     *                        类型    名称           必填     备注
     *                        Object   pageNo    N 	页数(1,2,3...),如果分页信息不传递，取默认
     *                        Object   pageSize  N 	每页显示条数,如果分页信息不传递，取默认
     *                 </pre>
     * @return <pre>
     * 	待发会议列表数据
     * </pre>
     */
    @POST
    @Path("waitsends/{personid}")
    @RestInterfaceAnnotation
    public Response getWaitsendMeetings(@PathParam("personid") Long personId, Map<String, Object> pageMap) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            String listType = ListTypeEnum.listWaitSendMeeting.name();
            Integer pageSize = pageMap.get("pageSize") == null ? 20 : (Integer) pageMap.get("pageSize");
            Integer pageNo = pageMap.get("pageNo") == null ? 1 : (Integer) pageMap.get("pageNo");
            resultMap = meetingM3Manager.findM3MeetingList(listType, personId, pageSize, pageNo);

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(resultMap);
    }

    /**
     * 获取会议待办数量<BR>
     *
     * @param Long personId 人员Id 非必填    当personId为空时，获取当前登录人员的会议待办数量
     *             method:GET<BR>
     * @return <pre>
     *  会议待办数量
     * <pre>
     */
    @GET
    @Path("pendingCount")
    public Response getPendingMeetingCount(@QueryParam("personid") Long personId) {
        String listType = ListTypeEnum.listPendingMeeting.name();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            resultMap = meetingM3Manager.findM3MeetingListCount(listType);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(resultMap);
    }

    /*************************************  会议列表Rest接口 end ***********************************************/

    /**
     * 判断当前人员具有的会议与会议室相关的菜单权限
     *
     * @return Map
     * data 是否是该单位会议室管理员<br />
     * haveMeetingDoneRole 是否有已办列表的权限<br />
     * haveMeetingPendingRole 是否有待办列表的权限<br />
     * haveMeetingArrangeRole 是否有会议安排的权限<br />
     * haveMeetingRoomApp 是否有会议室申请的权限<br />
     * haveMeetingRoomPerm 是否有会议室审核的权限<br />
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("meeting/user/privMenu")
    @SuppressWarnings("static-access")
    public Response meetingUserPeivMenu() throws BusinessException {

        Map<String, Object> params = new HashMap<String, Object>();
        User user = AppContext.getCurrentUser();

        boolean isMeetingRoomAdminRole = MeetingRoomRoleUtil.isMeetingRoomAdminRole();
        //是否是该单位会议室管理员
        params.put(RETURN_DATA, isMeetingRoomAdminRole);
        //已开会议
        params.put("haveMeetingDoneRole", menuPurviewUtil.isHaveMeetingDone(user));
        //待开会议
        params.put("haveMeetingPendingRole", menuPurviewUtil.isHaveMeetingPending(user));
        //会议安排
        params.put("haveMeetingArrangeRole", menuPurviewUtil.isHaveMeetingArrange(user));
        //会议室申请
        params.put("haveMeetingRoomApp", menuPurviewUtil.isHaveMeetingRoomApp(user));
        //会议室审核
        params.put("haveMeetingRoomPerm", isMeetingRoomAdminRole);

        return ok(params);
    }


    /**
     * 提交震荡回复
     *
     * @param params <pre>
     *                类型	                  名称			必填	                   备注
     *                String           meetingId       Y               会议Id
     *                String           replyId         Y               回执Id
     *                String           content         Y               内容
     *                String           memberId        Y               人员Id(如果是代理情况，传入被代理人ID)
     *                String           hiddencomment   Y               是否隐藏意见(true：是； false：否)
     *                String           hidden2creator  Y               是否对发起人隐藏(true：是； false：否)
     *                String           sendMsg         Y               是否发送消息(true：是； false：否)
     *               </pre>
     * @return <pre>
     * 		MtComment 意见对象
     * </pre>
     * @throws BusinessException
     */
    @POST
    @Path("comment")
    public Response comment(Map<String, Object> params) throws BusinessException {
        //前台获取各种参数
        Long meetingId = ParamUtil.getLong(params, "meetingId");//会议ID
        Long replyId = ParamUtil.getLong(params, "replyId");//回执的ID
        String content = ParamUtil.getString(params, "content");//内容
        Long memberId = ParamUtil.getLong(params, "memberId");
        String hiddencomment = ParamUtil.getString(params, "hiddencomment");
        String hidden2creator = ParamUtil.getString(params, "hidden2creator");
        String pSendMsg = ParamUtil.getString(params, "sendMsg");
        Long proxyId = 0l;

        if (meetingId == null || replyId == null || content == null
                || hiddencomment == null || hidden2creator == null || pSendMsg == null
                || memberId == null) {
            return ok(errorParams());
        }

        User user = CurrentUser.get();
        if (!memberId.equals(user.getId())) {
            proxyId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.meeting.key(), memberId);
            if (!user.getId().equals(proxyId)) {
                return ok(errorParams());
            }
        }

        boolean ishidden = Boolean.parseBoolean(hiddencomment.toLowerCase());
        boolean hiddenCreator = Boolean.parseBoolean(hidden2creator.toLowerCase());
        boolean sendMsg = Boolean.parseBoolean(pSendMsg.toLowerCase());

        //参数准备
        Long creatorId = AppContext.currentUserId();
        String creatorName = AppContext.currentUserName();
        MtMeeting meeting = meetingManager.getMeetingById(meetingId);
        //构造回复
        MtComment mc = new MtComment();
        mc.setIdIfNew();
        mc.setContent(content);
        mc.setCreateUserId(creatorId);
        mc.putExtraAttr("creator", creatorName);
        mc.setIsHidden(ishidden);
        mc.setReplyId(replyId);
        mc.setMeetingId(meetingId);
        mc.setCreateDate(new Timestamp(new Date().getTime()));
        if (proxyId.longValue() != 0l) {
            mc.setProxyId(memberId.toString());
        }

        MtReply reply = replyManager.getById(replyId);
        if (ishidden) {
            String showToId = reply.getUserId().toString();
            if (!hiddenCreator) {
                showToId += "," + meeting.getCreateUser().toString();
            }
            mc.setShowToId(showToId);
        }
        //重复提交校验
        Long submitKey = AppContext.getCurrentUser().getId();
        //保存回复
        boolean isLocked = meetingLockManager.isLock(submitKey);
        try {
            if (isLocked) {
                return null;
            }
            mtMeetingManager.saveComment(mc);
        } finally {
            if (!isLocked) {
                meetingLockManager.unLock(submitKey);
            }
        }
        //发送消息
        if (sendMsg) {
            MeetingMsgHelper.sendMessage("", mc, reply, meeting, AppContext.getCurrentUser());
        }
        //保存附件
        String relateInfo = ParamUtil.getString(params, "fileJson", "[]");
        List<Map> files = JSONUtil.parseJSONString(relateInfo, List.class);
        try {
            List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.meeting, meetingId, mc.getId(), files);
            if (!attList.isEmpty()) {
                attachmentManager.create(attList);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new BusinessException(ex);
        }
        return ok(mc);
    }


    /**
     * 会议回执 <BR>
     *
     * @param map <pre>
     *            	类型	                  名称			必填	                   备注
     *            String            meetingId      Y                会议ID
     *            String            content        Y                内容
     *            String            memberId       Y                人员Id
     *            String            feedbackFlag   Y                回执态度  （参加 1  不参加  0    待定 -1   告知人员传递3）
     *            String            fileJson       N                附件json数据
     *            </pre>
     * @return <pre>
     * 	MtReply 会议回执对象。
     * </pre>
     */
    @POST
    @Path("reply")
    @RestInterfaceAnnotation
    public Response reply(Map<String, Object> params) throws BusinessException {
        Long meetingId = ParamUtil.getLong(params, "meetingId");
        String content = ParamUtil.getString(params, "content", "");
        Long memberId = ParamUtil.getLong(params, "memberId");
        Long proxyId = 0L;

        User user = CurrentUser.get();
        if (user == null && memberId != null) {
            Long mId = Long.valueOf(memberId);
            setCurrentUser(mId);
            user = (User) AppContext.getThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY);
        }
        if (!memberId.equals(user.getId())) {
            proxyId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.meeting.key(), memberId);
            if (!user.getId().equals(proxyId)) {
                Map<String, String> retMap = new HashMap<String, String>();
                retMap.put("errorMsg", ResourceUtil.getString("meeting.error.noReply"));
                LOGGER.info("回执传入会议人员信息非法!传入人员id:" + memberId + ",当前获取登录人员Id:" + user.getId() + ",获取代理人Id:" + proxyId);
                return ok(retMap);
            }
        }

        MeetingBO meeting = meetingApi.getMeeting(meetingId);
        if (meeting.getState() == Constants.DATA_STATE_FINISH) {//如果会议已结束
            Map<String, String> retMap = new HashMap<String, String>();
            retMap.put("errorMsg", ResourceUtil.getString("meeting.status.finish"));
            return ok(retMap);
        }

        if (params.get("feedbackFlag") == null) {
            return ok(errorParams());
        }

        int option = ParamUtil.getInt(params, "feedbackFlag");
        MtMeeting mtMeeting = meetingManager.getMeetingById(meetingId);
        //当与会人选择的部门或者集团的时候，不能使用meeting.getConferees()直接与memberId相比较
        MeetingMemberVO vo = meetingManager.getAllTypeMember(meetingId, mtMeeting);
        List<Long> confereeMemberList = vo.getConferees();
        if (mtMeeting.getImpart() != null && !confereeMemberList.contains(memberId) && mtMeeting.getImpart().contains(String.valueOf(memberId)) && option != Constants.FEEDBACKFLAG_IMPART) {
            return ok(errorParams());
        }

        if (mtMeeting.getConferees() != null && confereeMemberList.contains(memberId) && option != Constants.FEEDBACKFLAG_ATTEND
                && option != Constants.FEEDBACKFLAG_PENDING && option != Constants.FEEDBACKFLAG_UNATTEND) {
            return ok(errorParams());
        }

        MtReplyBO relyBO = null;
        if (proxyId.longValue() == 0L) {
            List<MtReplyBO> myReplyList = meetingApi.findReplyByMeetingIdAndUserId(meetingId, user.getId());
            if (Strings.isEmpty(myReplyList)) {
                relyBO = new MtReplyBO();
                relyBO.setMeetingId(meetingId);
                relyBO.setUserId(user.getId());
            } else {
                //如果之前已经有回执了，就不必更新为已读了，否则会把之前的回执意见覆盖掉
                relyBO = myReplyList.get(0);
                //删除之前的附件关联
                attachmentManager.deleteByReference(meetingId, relyBO.getId());
                if (option == Constants.FEEDBACKFLAG_NOREPLY) {
                    return ok(errorParams());
                }
            }
            relyBO.setOldFeedbackFlag(relyBO.getFeedbackFlag());
            relyBO.setUserName(user.getName());
            relyBO.setFeedback(content);
            relyBO.setFeedbackFlag(option);
            relyBO.setLookState(1);
            relyBO.setLookTime(new Timestamp(System.currentTimeMillis()));
            relyBO.setExt1(Constants.Not_Agent);
            relyBO.setExt2(Constants.Not_Agent);
        } else {
            List<MtReplyBO> myReplyList = meetingApi.findReplyByMeetingIdAndUserId(meetingId, memberId);
            if (Strings.isEmpty(myReplyList)) {
                relyBO = new MtReplyBO();
                relyBO.setMeetingId(meetingId);
            } else {
                relyBO = myReplyList.get(0);
                //删除之前的附件关联
                attachmentManager.deleteByReference(meetingId, relyBO.getId());
                if (option == Constants.FEEDBACKFLAG_NOREPLY) {
                    return ok(errorParams());
                }
            }
            relyBO.setOldFeedbackFlag(relyBO.getFeedbackFlag());
            relyBO.setFeedback(content);
            relyBO.setFeedbackFlag(option);
            relyBO.setLookState(1);
            relyBO.setLookTime(new Timestamp(System.currentTimeMillis()));
            //如果是代理的情况下  设置创建人为代理人，代理人为创建人
            relyBO.setExt1(Constants.PASSIVE_AGENT_FLAG);
            relyBO.setExt2(user.getName());
            relyBO.setUserId(memberId);
            relyBO.setUserName(orgManager.getMemberById(memberId).getName());
        }
        MtReply reply = new MtReply();
        BeanUtils.convert(reply, relyBO);
        //修改回执时间
        reply.setReadDate(new Timestamp(System.currentTimeMillis()));
        meetingManager.updateReplyCount(relyBO, mtMeeting);
        //重复提交校验
        Long submitKey = AppContext.getCurrentUser().getId();
        boolean isLocked = meetingLockManager.isLock(submitKey);
        try {
            if (isLocked) {
                return null;
            }
            reply = replyManager.save(reply);
            replyManager.updateAffair4Reply(reply);
        } finally {
            if (!isLocked) {
                meetingLockManager.unLock(submitKey);
            }
        }
        //如果等于-100的话，是从未查看状态变为已查看状态，不需要发送消息；其他的回执需要发送消息
        if (null != relyBO.getFeedbackFlag() && relyBO.getFeedbackFlag().intValue() != Constants.FEEDBACKFLAG_NOREPLY) {
            List<Long> listId = new ArrayList<Long>();
            listId.add(meeting.getCreateUser());
            Collection<MessageReceiver> receivers = MessageReceiver.getReceivers(meetingId, listId, "message.link.mt.reply", meeting.getId().toString(), reply.getId().toString());
            for (MessageReceiver rec : receivers) {
                rec.setReply(true);
            }
            String feedback = UserMessageUtil.getComment4Message(relyBO.getFeedback());
            int contentType = Strings.isBlank(feedback) ? -1 : 1;
            int proxyType = 0;
            userMessageManager.sendSystemMessage(MessageContent.get("meeting.message.reply", meeting.getTitle(), user.getName(), relyBO.getFeedbackFlag(), contentType, feedback, proxyType, user.getName()), ApplicationCategoryEnum.meeting, user.getId(), receivers, MeetingMessageTypeEnum.Meeting_Reply.key());
        }

        /**
         * 给@的人发消息
         */
        List<Map<String, Object>> atMembers = (List<Map<String, Object>>) params.get("atMembers");
        if (atMembers != null && atMembers.size() > 0) {
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            for (Map<String, Object> member : atMembers) {
                MessageReceiver receiver = MessageReceiver.get(meetingId, Long.parseLong(member.get("id").toString()), "message.link.mt.reply", meetingId.toString(), reply.getId().toString());
                receiver.setAt(true);
                receivers.add(receiver);
            }
            String feedback = UserMessageUtil.getComment4Message(relyBO.getFeedback());
            int contentType = Strings.isBlank(feedback) ? -1 : 1;
            int proxyType = 0;
            userMessageManager.sendSystemMessage(MessageContent.get("meeting.message.reply", meeting.getTitle(), user.getName(), relyBO.getFeedbackFlag(), contentType, feedback, proxyType, user.getName()), ApplicationCategoryEnum.meeting, user.getId(), receivers, MeetingMessageTypeEnum.Meeting_Reply.key());
        }

        //为日程触发事件
        if (AppContext.hasPlugin("timeView")) {
            List<Long> joinAndPendingMember = meetingManager.getJoinMeetingReplyMapExtImpart(meeting.getId(), mtMeeting);
            MeetingBO mVo = DataTransUtil.trans2MeetingBO(mtMeeting);
            mVo.setJoinAndPendingMember(joinAndPendingMember);
            MeetingReplyEvent event = new MeetingReplyEvent(this, mVo, user.getId(), new Date(), relyBO.getFeedbackFlag().intValue());
            EventDispatcher.fireEventAfterCommit(event);
        }

        //保存附件
        String relateInfo = ParamUtil.getString(params, "fileJson", "[]");
        List<Map> files = JSONUtil.parseJSONString(relateInfo, List.class);
        try {
            List<Attachment> attList = attachmentManager.getAttachmentsFromAttachList(ApplicationCategoryEnum.meeting, meetingId, reply.getId(), files);
            if (!attList.isEmpty()) {
                attachmentManager.create(attList);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new BusinessException(ex);
        }
        return ok(reply);
    }

    /**
     * 会议详情api
     * Long affairId  事项Id 非必填
     * Long meetingId 会议Id 必填
     * Long proxyId 发起代理的人Id 非必填
     *
     * @param String openFrom  来源       非必填  当 openFrom=（关联文档：glwd 或 文档中心：docLib）时，不校验会议是否撤销
     * @return <pre>
     * 	会议详情map结构
     * </pre>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("detail")
    public Response detail(@QueryParam("affairId") Long affairId, @QueryParam("meetingId") Long meetingId, @QueryParam("openFrom") String openFrom, @QueryParam("proxyId") Long proxyId) throws BusinessException {
        Map<String, Object> map = new HashMap<String, Object>();

        if (meetingId == null) {
            return ok(errorParams());
        }

        Integer zero = 0;
        int attCount = 0; //计算附件关联文档的个数

        MeetingBO meetingBO = new MeetingBO();
        if (meetingApi != null) {
            meetingBO = meetingApi.getMeetingByAffairId(affairId, AppContext.currentUserId(), meetingId, openFrom);//会议
        }

        /**
         * 是否是代理的其他人的会议
         */
        Long currentUserId = AppContext.currentUserId();
        if (!MeetingUtil.isIdNull(proxyId)) {
            currentUserId = proxyId;
            meetingBO.setProxyId(proxyId);
        }

        if (meetingBO.getErrorRet().size() > 0) {
            return ok(meetingBO.getErrorRet());
        }

        meetingBO.setCreateUserName(Functions.showMemberName(meetingBO.getCreateUser()));
        String emcName = Functions.showMemberName(meetingBO.getEmceeId());
        if (Strings.isNotBlank(emcName) && emcName.length() > 18) {
            emcName = emcName.substring(0, 18) + "...";
        }
        meetingBO.setEmceeName(emcName);
        String recName = Functions.showMemberName(meetingBO.getRecorderId());
        if (Strings.isNotBlank(recName) && recName.length() > 18) {
            recName = recName.subSequence(0, 18) + "...";
        }
        meetingBO.setRecorderName(recName);
        if (!"html".equalsIgnoreCase(meetingBO.getBodyType())) {
            V3XFile file = fileManager.getV3XFile(Long.valueOf(meetingBO.getContent()));
            if (file != null) {
                meetingBO.setLastModified(DateUtil.formatDateTime(file.getUpdateDate()));
                //是否允许office转换
                map.put("allowTrans", OfficeTransHelper.allowTrans(file));
            }
        } else {//HTML 正文需要被正文组件重新解析，才能正常显示其中的关联和附件
            String htmlContentString = meetingBO.getContent();
            meetingBO.setContent(HtmlMainbodyHandler.replaceInlineAttachment(htmlContentString));
        }
        //附件,关联文档
        List<Attachment> attachments = null;
        if (meetingBO != null) {
            attachments = attachmentManager.getByReference(meetingBO.getId(), meetingBO.getId());
        }
        attCount = 0;
        for (Attachment att : attachments) {
            if (zero.equals(att.getType())) {
                attCount++;
            }
        }

        MeetingMemberVO memberVo = meetingManager.getAllTypeMember(meetingId, null);
        List<Long> conferees = memberVo.getConferees();
        List<Long> impart = memberVo.getImpart();
        List<Long> leader = memberVo.getLeader();

        //全部人员Id
        List<Long> replyMemberIds = new ArrayList<Long>();

        //主持人
        for (Long memberId : memberVo.getEmcee()) {
            replyMemberIds.add(memberId);
        }

        //记录人
        for (Long memberId : memberVo.getRecorder()) {
            replyMemberIds.add(memberId);
        }

        //参会领导
        List<String> leaderNames = new ArrayList<String>();
        for (Long memberId : leader) {
            String memberName = Functions.showMemberName(memberId);
            leaderNames.add(memberName);
            replyMemberIds.add(memberId);
        }
        meetingBO.setLeaderNames(leaderNames.toString().substring(1, leaderNames.toString().length() - 1));

        List<String> confereesNames = new ArrayList<String>();
        for (Long memberId : conferees) {
            String memberName = Functions.showMemberName(memberId);
            if (!Strings.isNotBlank(meetingBO.getLeader()) || !meetingBO.getLeader().contains(String.valueOf(memberId))) {
                confereesNames.add(memberName);
            }

            replyMemberIds.add(memberId);
        }
        //设置详细页面显示的与会人
        List<String> showConfereesNames = new ArrayList<String>();
        if (confereesNames.size() > 10) {
            showConfereesNames = confereesNames.subList(0, 10);
        } else {
            showConfereesNames = confereesNames;
        }
        meetingBO.setConfereesNames(showConfereesNames.toString().substring(1, showConfereesNames.toString().length() - 1));


        List<String> impartNames = new ArrayList<String>();
        for (Long memberId : impart) {
            impartNames.add(Functions.showMemberName(memberId));
            replyMemberIds.add(memberId);
        }

        //设置详细页面显示的告知人
        List<String> showImpartNames = new ArrayList<String>();
        if (impartNames.size() > 10) {
            showImpartNames = impartNames.subList(0, 10);
        } else {
            showImpartNames = impartNames;
        }
        meetingBO.setImpartNames(showImpartNames.toString().substring(1, showImpartNames.toString().length() - 1));

        MtReplyBO myReplyBo = meetingReplyManager.getReplyByMeetingIdAndUserId(meetingId, currentUserId);
        if ("pending".equals(openFrom)) {
            //查看时需要回执一条数据,控制图标
            //未查看的就设置未已查看
            if (myReplyBo != null && (myReplyBo.getLookState() == null || myReplyBo.getLookState() == 0)) {
                MtReply reply = MeetingHelper.coverReplyBOToReply(myReplyBo);
                reply.setLookState(1);
                reply.setLookTime(new Timestamp(System.currentTimeMillis()));
                replyManager.save(reply);

                CtpAffair affair = affairManager.get(meetingBO.getAffairId());
                if (affair != null) {
                    affair.setSubState(SubStateEnum.col_pending_read.key());
                    affairManager.updateAffair(affair);
                }
            }
        }

        /**
         * 视频会议相关
         */
        boolean showVideoEntrance = false;
        if (MeetingUtil.isVideoPluginAvailable() && MeetingNatureEnum.video.key().equals(meetingBO.getMeetingType())) {
            MtMeeting meeting = meetingManager.getMeetingById(meetingId);
            Long userId = AppContext.currentUserId();
            String videoMeetingDetail = meeting.getVideoMeetingId();
            Map<String, Object> ext4Map = new HashMap<String, Object>(16);
            try {
                if (Strings.isNotBlank(videoMeetingDetail)) {
                    ext4Map = JSONUtil.parseJSONString(videoMeetingDetail, Map.class);
                }
                String videoRoomName = (String) ext4Map.get("videoRoomName");
                String meetingVideoId = (String) ext4Map.get("bmId");
                String videoRoomId = (String) ext4Map.get("videoRoomId");//视频会议室Id
                if (Strings.isNotBlank(meetingVideoId)) {
                    videoRoomName += "(" + ResourceUtil.getString("meeting.video.number.name") + meetingVideoId + ")";
                }
                if (Strings.isNotBlank(videoRoomName)) {
                    map.put("showVideoRoom", true);
                    map.put("videoRoomName", videoRoomName);
                    map.put("password", meeting.getMeetingPassword());
                    map.put("videoRoomId", videoRoomId);
                }
            } catch (Exception e) {
                LOGGER.error("转换videoMeetingId视频会议参数JSON格式异常", e);
            }
            if (!impart.contains(userId) && MeetingHelper.isPending(meeting.getState()) && MeetingHelper.isRoomPass(meeting.getRoomState())) {
                MeetingVideoManager meetingVideoManager;
                try {
                    meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
                    showVideoEntrance = true;
                    String v_url = meetingVideoManager.getJoinImportUrlM3();
                    map.put("v_url", v_url);
                    if (Strings.isNotBlank(videoMeetingDetail)) {
                        String videoURL;
                        if (meeting.getEmceeId() != null && userId.equals(meeting.getEmceeId())) {
                            videoURL = (String) ext4Map.get("bmCompereUrl");
                        } else {
                            videoURL = (String) ext4Map.get("bmUrl");
                        }
                        map.put("video_url", videoURL);
                    }

                    if ("html".equalsIgnoreCase(meetingBO.getBodyType())) {
                        meetingBO.setContent(meetingBO.getContent() + meetingVideoManager.getVideoUrlContent(meeting.getVideoMeetingId()));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }

        map.put("v_entrance", showVideoEntrance);

        //回复
        List<MtReplyBO> replyList = meetingApi.findReplyList(meetingId);
        map.put("feedbackFlag", myReplyBo != null ? myReplyBo.getFeedbackFlag() : "");

        // 未回执的
        Map<String, List<Long>> ReplyUsers = meetingApi.findMtReplyUsers(meetingId);
        List<Long> noFeedbackList = ReplyUsers.get("noFeedback");
        String noFeedBackMemberIds = "";
        for (Long memberid : noFeedbackList) {
            noFeedBackMemberIds += memberid.toString() + ",";
        }

        //会议类型
        MeetingType mt = meetingTypeManager.getMeetingTypeById(meetingManager.getMeetingById(meetingId).getMeetingTypeId());
        map.put("meetingTypeName", mt == null ? "" : ResourceUtil.getString(mt.getName()));
        map.put("noFeedBackMemberIds", noFeedBackMemberIds);
        map.put("currentUser", AppContext.getCurrentUser());
        map.put("currentUserId", AppContext.currentUserId());
        map.put("meeting", meetingBO);
        map.put("attachmentList", attachments);
        map.put("attCount", attCount);
        map.put("meetingIsEnd", "1");
        if (meetingBO.getState() == MeetingConstant.MeetingStateEnum.start.key() || meetingBO.getState() == MeetingConstant.MeetingStateEnum.send.key()) {
            map.put("meetingIsEnd", "0");
        }
        map.put("replyList", replyList);
        map.put("replySize", replyList.size());
        map.put("listType", openFrom);

        //会议任务
        Boolean canNewTask = AppContext.getCurrentUser().hasResourceCode("F02_taskPage");
        Boolean hasTaskPlugin = AppContext.hasPlugin("taskmanage");
        Boolean canRecord = memberVo.getRecorder().contains(AppContext.currentUserId()) || (memberVo.getRecorder().isEmpty() && memberVo.getEmcee().contains(AppContext.currentUserId()));
        map.put("canNewTask", hasTaskPlugin);
        map.put("canRecord", canNewTask && canRecord);
        map.put("isImpart", impart.contains(AppContext.currentUserId()));
        //获取任务总数，在会议纪要页面，前端使用
        int meetingTaskNum = 0;
        if (AppContext.hasPlugin("taskmanage") && taskmanageApi != null) {
            meetingTaskNum = taskmanageApi.countTaskSource(ApplicationCategoryEnum.meeting.getKey(), meetingId, null, null);
        }
        map.put("meetingTaskNum", meetingTaskNum);

        /**
         * 会议@的人员范围
         */
        List<Map<String, Object>> allMembers = memberVo.getAllMemberObj();
        map.put("atScope", allMembers);

        //控制详情页面各按钮状态
        this.showOrHideButton(map, meetingBO, openFrom, replyMemberIds);
        //计算人员回执类型数量情况
        this.calcMemberCount(meetingId, memberVo, map);
        //计算二维码签到人数
        List<MeetingQrcodeSign> qrcodeSignList = meetingBarCodeManager.findMeetingQrcodeSignByMeetingId(meetingId, meetingBO.getAccountId());
        map.put("signNumber", qrcodeSignList.size());

        /**
         * 签到二维码显示需满足4个条件:
         * 1.会议创建时开启二维码签到标记
         * 2.会议状态不是待发状态
         * 3.会议室审核状态为通过
         * 4.当前人员为会议发起人或主持人(页面JS已有判断)
         **/
        MeetingScreenSet set = null;
        if (meetingBO.getQrCodeSign() != null && meetingBO.getQrCodeSign() == MeetingBarCodeConstant.QrCodeSign.enable.key() && !MeetingHelper.isWait(meetingBO.getState())
                && MeetingHelper.isRoomPass(meetingBO.getRoomState())) {
            //会议二维码提前生效分钟数
            set = meetingSettingManager.getMeetingScreenSet(meetingBO.getAccountId());
            map.put("qrCodeCheckIn", meetingBO.getQrCodeCheckIn());
            map.put("preMinutes", set == null ? 30 : set.getPreMinutes());
        }
        map.put("qrCodeSign", meetingBO.getQrCodeSign() != null && meetingBO.getQrCodeSign() == MeetingBarCodeConstant.QrCodeSign.enable.key());

        /**
         * 会议邀请二维码
         * 1.是外部会议
         * 2.未开始和进行中
         * 3.会议室审核通过
         * 4.当前人员是发起人或主持人
         */
        boolean isPublic = meetingBO.getIsPublic() != null && meetingBO.getIsPublic() == MeetingConstant.MeetingPublicType.isPublic.ordinal();
        boolean isEnableVisitor = orgManager.accessedByVisitor(ApplicationCategoryEnum.meeting.name(), meetingBO.getAccountId());
        boolean isSenderOrEmcee = currentUserId == meetingBO.getCreateUser() || AppContext.currentUserId() == meetingBO.getEmceeId();
        if (isPublic && isEnableVisitor && MeetingHelper.isPending(meetingBO.getState()) && MeetingHelper.isRoomPass(meetingBO.getRoomState()) && isSenderOrEmcee) {
            //邀请二维码文件id
            map.put("qrCodeInvite", meetingBO.getQrCodeInvite());
            String meetingTimeDisplay = Datetimes.format(meetingBO.getBeginDate(), "yyyy/MM/dd HH:mm") + " - " + Datetimes.format(meetingBO.getEndDate(), "yyyy/MM/dd HH:mm");
            map.put("meetingTimeDisplay", meetingTimeDisplay);
            /**
             * 获取会议室展示屏设置那里的logo
             */
            if (set == null) {
                set = meetingSettingManager.getMeetingScreenSet(meetingBO.getAccountId());
            }
            List<Attachment> logoAttachments = attachmentManager.getByReference(set != null ? set.getId() : -1L, set != null ? set.getId() : -1L);
            if (logoAttachments.size() > 0) {
                map.put("qrCodeInviteLogo", logoAttachments.get(0));
            } else {
                V3xOrgAccount account = orgManager.getAccountById(meetingBO.getAccountId());
                map.put("accountName", account.getShortName() != null ? account.getShortName() : account.getName());
            }
            /**
             * 获取分享所需参数
             * 1.v5Domain oa外网地址
             * 2.v5AccountId 二维码单位id
             * 3.M3QRCode 二维码数据id
             */
            //获取oa外网地址
            String v5Domain = "";
            ConfigItem configItem2 = configManager.getConfigItem("wechat_switch", "oa_url");
            if (configItem2 != null) {
                v5Domain = configItem2.getConfigValue();
            }
            if (Strings.isBlank(v5Domain)) {
                v5Domain = "";
            }
            map.put("v5Domain", v5Domain);

            //会议所属单位id
            Long v5AccountId = meetingBO.getAccountId();
            map.put("v5AccountId", v5AccountId);

            //二维码数据id
            Long M3QRCode = null;
            List<PublicQrCodePO> pos = publicQrCodeManager.getPublcQrCodeByObjectId(meetingId);
            for (PublicQrCodePO po : pos) {
                if (Strings.isNotBlank(po.getLinkParams())) {
                    Map<String, String> linkParams = JSONUtil.parseJSONString(po.getLinkParams(), Map.class);
                    if (linkParams.get(MeetingConstant.QR_CODE_PARAM_TYPE).equals(MeetingBarCodeConstant.QrCodeType.meetingInvite.name())) {
                        M3QRCode = po.getId();
                        break;
                    }
                }
            }
            map.put("M3QRCode", M3QRCode);
        }

        //同步消息
        userMessageManager.updateSystemMessageStateByUserAndReference(AppContext.currentUserId(), meetingId);

        return success(map);
    }

    private void calcMemberCount(Long meetingId, MeetingMemberVO memberVo, Map<String, Object> map) throws BusinessException {
        //与会人
        List<Long> conferees = memberVo.getConferees();
        //告知人
        List<Long> impart = memberVo.getImpart();
        //领导
        List<Long> leader = memberVo.getLeader();

        List<Long> replyConferees = new ArrayList<Long>();
        replyConferees.addAll(conferees);

        List<Long> replyImpart = new ArrayList<Long>();
        replyImpart.addAll(impart);

        List<Long> replyLeader = new ArrayList<Long>();
        replyLeader.addAll(leader);

        //与会人数量情况
        int con_attend = 0, con_pending = 0, con_noAttend = 0, con_noReply = 0;
        //领导数量情况
        int lea_attend = 0, lea_pending = 0, lea_noAttend = 0, lea_noReply = 0;
        //告知人数量情况
        int imp_reply = 0, imp_noReply = 0;

        List<MtReply> replys = meetingReplyManager.getReplyByMeetingId(meetingId);
        for (MtReply reply : replys) {
            /**
             * 会议外部人员按与会人计算并展示
             */
            boolean isInternal = MeetingUserType.isExternalOrOuter(reply.getUserType());
            if (conferees.contains(reply.getUserId()) || isInternal) {
                if (MeetingConstant.MeetingReplyFeedbackFlagEnum.attend.key() == reply.getFeedbackFlag()) {
                    con_attend++;
                } else if (MeetingConstant.MeetingReplyFeedbackFlagEnum.pending.key() == reply.getFeedbackFlag()) {
                    con_pending++;
                } else if (MeetingConstant.MeetingReplyFeedbackFlagEnum.unattend.key() == reply.getFeedbackFlag()) {
                    con_noAttend++;
                } else {
                    con_noReply++;
                }
                replyConferees.remove(reply.getUserId());
            } else if (impart.contains(reply.getUserId())) {
                if (reply.getFeedbackFlag() == 3) {
                    imp_reply++;
                } else {
                    imp_noReply++;
                }
                replyImpart.remove(reply.getUserId());
            } else if (leader.contains(reply.getUserId())) {
                if (MeetingConstant.MeetingReplyFeedbackFlagEnum.attend.key() == reply.getFeedbackFlag()) {
                    lea_attend++;
                } else if (MeetingConstant.MeetingReplyFeedbackFlagEnum.pending.key() == reply.getFeedbackFlag()) {
                    lea_pending++;
                } else if (MeetingConstant.MeetingReplyFeedbackFlagEnum.unattend.key() == reply.getFeedbackFlag()) {
                    lea_noAttend++;
                } else {
                    lea_noReply++;
                }
                replyLeader.remove(reply.getUserId());
            }
        }
        //兼容老版本数据:未回执时,reply表中不存在数据
        if (Strings.isNotEmpty(replyConferees)) {
            con_noReply += replyConferees.size();
        }
        if (Strings.isNotEmpty(replyImpart)) {
            imp_noReply += replyImpart.size();
        }
        if (Strings.isNotEmpty(replyLeader)) {
            lea_noReply += replyLeader.size();
        }

        map.put("con_attend", con_attend);
        map.put("con_pending", con_pending);
        map.put("con_noAttend", con_noAttend);
        map.put("con_noReply", con_noReply);
        map.put("lea_attend", lea_attend);
        map.put("lea_pending", lea_pending);
        map.put("lea_noAttend", lea_noAttend);
        map.put("lea_noReply", lea_noReply);
        map.put("imp_reply", imp_reply);
        map.put("imp_noReply", imp_noReply);
    }

    /*
     * 控制详情页面各按钮状态
     */
    private void showOrHideButton(Map<String, Object> map, MeetingBO meetingBO, String openFrom, List<Long> replyMemberIds) throws BusinessException {
        boolean canReply = true;         //回执
        boolean canQuickReply = true;    //快速回执
        boolean canCancel = false;       //撤销
        boolean canModify = false;       //编辑
        boolean canAdvanceOver = false;  //提前结束
        boolean canSummary = false;       //纪要
        boolean canSeeTask = true;      //会议任务

        //当前为知会的时候隐藏纪要
        if (meetingBO.getRecordId() != null && !Integer.valueOf(9).equals(meetingBO.getBusinessType())) {
            canSummary = true;
        }

        //待开和待发的会议不显示会议任务
        if (MeetingHelper.isWait(meetingBO.getState()) || MeetingHelper.isSend(meetingBO.getState()))
            canSeeTask = false;

        //顶部区域
        map.put("canSummary", canSummary);
        map.put("canSeeTask", canSeeTask);

        //当前会议结束和关联文档 、文档中心、会议结束的不显示按钮区域
        if (MeetingHelper.isWait(meetingBO.getState()) || MeetingHelper.isFinished(meetingBO.getState()) ||
                (null != meetingBO.getAffairState() && meetingBO.getAffairState().intValue() == 4) || "glwd".equals(openFrom) || "docLib".equals(openFrom)) {

            map.put("showButton", false);
            return;
        }

        Long currentUserId = AppContext.currentUserId();
        Long createUserId = meetingBO.getCreateUser();
        if (!MeetingUtil.isIdNull(meetingBO.getProxyId())) {
            currentUserId = meetingBO.getProxyId();
        }

        //当前人员是发起人
        if (currentUserId.equals(createUserId)) {
            canReply = false;
        }

        //当前为知会的时候隐藏快速回执  || 当前人员是发起人或者被代理人是发起人隐藏快速回执
        if ((null != meetingBO.getBusinessType() && meetingBO.getBusinessType().intValue() == 9) || currentUserId.equals(createUserId)) {
            canQuickReply = false;
        }

        //当前人员是发起人，会议处于已发送或已开始状态
        if (currentUserId.equals(createUserId) && MeetingHelper.isPending(meetingBO.getState())) {
            canCancel = true;
        }

        //当前人员是发起人，会议处于已发送
        if (currentUserId.equals(createUserId) && MeetingHelper.isSend(meetingBO.getState())) {
            canModify = true;
        }

        //当前人员是发起人，会议处于已开始状态
        if (currentUserId.equals(createUserId) && MeetingHelper.isStart(meetingBO.getState())) {
            canAdvanceOver = true;
        }

        //按钮区域
        map.put("canReply", canReply);
        map.put("canQuickReply", canQuickReply);
        map.put("canCancel", canCancel);
        map.put("canModify", canModify);
        map.put("canAdvanceOver", canAdvanceOver);

        map.put("showButton", canReply || canQuickReply || canCancel || canModify || canAdvanceOver);
    }

    /**
     * 查看具体会议人员
     * 返回指定人员类型（operate）的回执情况和签到情况
     * <pre>
     * Long affairId  事项Id 非必填
     * Long meetingId 会议Id 必填
     * String operate 人员类型  必填 【conferee：与会人、leader：参会领导、impart：告知人】
     * String listType 来自页 非必填 【"join", "noJoin", "pending", "noFeedback","impart"】
     * </pre>
     *
     * @return FlipInfo
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("meetingMembers")
    @RestInterfaceAnnotation
    public Response showMeetingMembers(@QueryParam("affairId") Long affairId, @QueryParam("meetingId") Long meetingId, @QueryParam("operate") String operate, @QueryParam("listType") String listType) throws BusinessException {
        List<MeetingReplyMemberVO> memberList = new ArrayList<MeetingReplyMemberVO>();
        List<MeetingReplyMemberVO> signMemberList = new ArrayList<MeetingReplyMemberVO>();
        if (meetingId == null) {
            return ok(errorParams());
        }

        List<V3xOrgMember> meetingMembers = null;

        /**
         * 会议所有相关人员
         */
        MeetingMemberVO memberVo = meetingManager.getAllTypeMember(meetingId, null);

        /**
         * 会议所有人的回执
         */
        Map<Long, MtReply> allReply = replyManager.findAllByMeetingId(meetingId);
        Map<Long, MtReply> allMemberReplyMap = new HashMap<Long, MtReply>(16);
        for (Map.Entry<Long, MtReply> entry : allReply.entrySet()) {
            MtReply reply = entry.getValue();
            allMemberReplyMap.put(reply.getUserId(), reply);
        }
        /**
         * 会议所有人的签到
         */
        List<MeetingQrcodeSign> qrcodeSignList = meetingBarCodeManager.findQrSignByMeetingId(meetingId);
        Map<Long, MeetingQrcodeSign> allMemberSignMap = new HashMap<Long, MeetingQrcodeSign>(16);
        for (MeetingQrcodeSign qrcodeSign : qrcodeSignList) {
            allMemberSignMap.put(qrcodeSign.getMemberId(), qrcodeSign);
            MtReply reply = allMemberReplyMap.get(qrcodeSign.getMemberId());

            MeetingReplyMemberVO vo = new MeetingReplyMemberVO();
            vo.setMemberId(qrcodeSign.getMemberId());
            vo.setMemberName(OrgHelper.showMemberName(qrcodeSign.getMemberId()));
            /**
             * 系统外访客显示单位
             */
            if (MeetingUserType.isOuter(qrcodeSign.getUserType())) {
                V3xOrgVisitor visitor = orgManager.getVisitorById(qrcodeSign.getMemberId());
                vo.setMemberPost(visitor.getAccount_name());
            } else {
                vo.setMemberPost(OrgHelper.showOrgPostNameByMemberid(qrcodeSign.getMemberId()));
            }
            if (reply != null) {
                vo.setReplyState(reply.getFeedbackFlag() == null ? "-100" : reply.getFeedbackFlag().toString());
                vo.setLook(reply.getLookState() == null ? "0" : reply.getLookState().toString());
            }
            vo.setSignDate(qrcodeSign.getSignDate());
            vo.setUserType(qrcodeSign.getUserType());
            signMemberList.add(vo);
        }

        /**
         * 参会人员数据
         */
        if ("conferee".equals(operate)) {
            Set<V3xOrgMember> memberSet = new LinkedHashSet<V3xOrgMember>(16);
            memberSet.addAll(memberVo.getSenderMembers());
            memberSet.addAll(memberVo.getEmceeMembers());
            memberSet.addAll(memberVo.getRecorderMembers());
            memberSet.addAll(memberVo.getConfereesMembers());
            memberSet.addAll(memberVo.getExternalMembers());
            meetingMembers = new ArrayList<V3xOrgMember>(memberSet);
        } else if ("leader".equals(operate)) {
            meetingMembers = memberVo.getLeaderMembers();
        } else if ("impart".equals(operate)) {
            meetingMembers = memberVo.getImpartMembers();
        }

        for (V3xOrgMember member : meetingMembers) {
            MeetingReplyMemberVO vo = new MeetingReplyMemberVO();
            MtReply reply = allMemberReplyMap.get(member.getId());
            //排除掉只是签到的人
            if (reply == null) {
                continue;
            }
            MeetingQrcodeSign sign = allMemberSignMap.get(member.getId());
            vo.setMemberId(member.getId());
            vo.setMemberName(member.getName());
            if (member.isVisitor()) {
                V3xOrgVisitor visitor = orgManager.getVisitorById(member.getId());
                vo.setMemberPost(visitor.getAccount_name());
            } else {
                vo.setMemberPost(OrgHelper.showOrgPostNameByMemberid(member.getId()));
            }

            vo.setReplyState(reply.getFeedbackFlag() == null ? "-100" : reply.getFeedbackFlag().toString());
            vo.setLook(reply.getLookState() == null ? "0" : reply.getLookState().toString());
            vo.setUserType(reply.getUserType());
            vo.setSignDate(sign == null ? null : sign.getSignDate());
            memberList.add(vo);
        }

        Map<String, Object> retMap = new HashMap<String, Object>(16);
        retMap.put("meetingMembers", memberList);
        retMap.put("signMembers", signMemberList);

        return success(retMap);
    }

    /**
     * 查看会议纪要中的实际与会人员
     * <pre>
     * @param recordId 会议纪要ID 必填
     * </pre>
     *
     * @return FlipInfo
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("showMeetingSummaryMembers")
    public Response showMeetingSummaryMembers(@QueryParam("recordId") Long recordId) throws BusinessException {
        FlipInfo flipInfo = getFlipInfo();
        MeetingSummary mtSummary = null;
        List<MeetingReplyMemberVO> memberList = new ArrayList<MeetingReplyMemberVO>();

        if (recordId == null) {
            return ok(errorParams());
        }
        mtSummary = meetingSummaryManager.getSummaryById(recordId);
        if (null != mtSummary) {
            List<Long> memlist = new ArrayList<Long>();
            MtMeeting meeting = meetingManager.getMeetingById(mtSummary.getMeetingId());
            // 实际与会人
            List<V3xOrgMember> scopesList = MeetingOrgHelper.getMembersByTypeAndId(mtSummary.getConferees(),
                    orgManager);
            for (V3xOrgMember scopes : scopesList) {
                memlist.add(scopes.getId());
            }

            if (!Strings.isEmpty(memlist)) {
                for (int i = 0; i < memlist.size(); i++) {
                    if (memlist.get(i).equals(meeting.getRecorderId()) || memlist.get(i).equals(meeting.getEmceeId())) {
                        continue;
                    }
                    MeetingReplyMemberVO memberVo = new MeetingReplyMemberVO();
                    memberVo.setMemberId(memlist.get(i));
                    String name = Functions.showMemberNameOnly(memlist.get(i));
                    memberVo.setMemberName(name);
                    // 当前人员职位
                    memberVo.setMemberPost(OrgHelper.showOrgPostNameByMemberid(memlist.get(i)));
                    memberList.add(memberVo);
                }

            }

            DBAgent.memoryPaging(memberList, flipInfo);
        }

        return ok(flipInfo);
    }

    /**
     * 获取会议纪要列表
     *
     * @param params 传入参数
     *               <pre>
     *                      类型    		名称             	是否必填     	 	备注
     *                      String   pageNo     Y     页数(1,2,3...)
     *                      String   pageSize   Y     每页显示条数
     *                      String   condition  N     查询条件类型(查询时选择的类型 time/roomName)
     *                      String   roomName   N     会议室名称
     *                      String   startDate  N     会议开始时间(yyyy-MM-dd HH:mm)
     *                      String   endDate    N     会议结束时间(yyyy-MM-dd HH:mm)
     *                </pre>
     * @return com.seeyon.ctp.util.FlipInfo
     * @throws BusinessException
     * @throws ParseException
     */
    @POST
    @Path("summary/getMeetingSummarys")
    public Response getMeetingSummarys(Map<String, Object> params) throws BusinessException, ParseException {
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo.setPage(ParamUtil.getInt(params, "pageNo", 1));
        flipInfo.setSize(ParamUtil.getInt(params, "pageSize", 20));
        String condition = ParamUtil.getString(params, "condition");
        User user = AppContext.getCurrentUser();
        Map<String, Object> hqlParams = new HashMap<String, Object>();
        hqlParams.put("memberId", user.getId());
        hqlParams.put("accountId", user.getAccountId());
        if ("time".equals(condition)) {
            String startDate = ParamUtil.getString(params, "startDate");
            String endDate = ParamUtil.getString(params, "endDate");
            if (Strings.isNotBlank(startDate)) {
                hqlParams.put("startDate", DateUtil.parse(startDate, DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
            }
            if (Strings.isNotBlank(endDate)) {
                hqlParams.put("endDate", DateUtil.parse(endDate, DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN));
            }
        } else {
            String meetingName = ParamUtil.getString(params, "meetingName");
            if (Strings.isNotBlank(meetingName)) {
                hqlParams.put("meetingName", meetingName);
            }
        }
        MtSummaryBO sb = null;
        List<MtSummaryBO> summaryList = new ArrayList<MtSummaryBO>();
        List<Map<String, Object>> meetingSummarys = meetingSummaryManager.findMeetingSummaryByParams(hqlParams, flipInfo);
        UniqueList<Long> ukList = new UniqueList<Long>();
        if (meetingSummarys != null) {
            for (Map mtSummary : meetingSummarys) {
                sb = new MtSummaryBO();
                Long summaryId = MapUtils.getLong(mtSummary, "id");
                if (ukList.contains(summaryId)) {
                    continue;
                } else {
                    ukList.add(summaryId);
                }
                sb.setId(summaryId);
                sb.setMeetingId(MapUtils.getLong(mtSummary, "meetingId"));
                sb.setMtName(MapUtils.getString(mtSummary, "mtName"));
                sb.setMeetingPlace(MapUtils.getString(mtSummary, "mtRoomName"));
                String mtBeginDate = Datetimes.format((Date) mtSummary.get("mtBeginDate"), Datetimes.datetimeStyle);
                String mtEndDate = Datetimes.format((Date) mtSummary.get("mtEndDate"), Datetimes.datetimeStyle);
                if (mtEndDate.substring(0, 10).equals(mtEndDate.substring(0, 10))) {
                    mtEndDate = mtEndDate.substring(11);
                }
                sb.setMeetingTime(mtBeginDate + "-" + mtEndDate);
                sb.setCreateUserName(Functions.showMemberNameOnly(MapUtils.getLong(mtSummary, "createUser")));
                sb.setCreateDateFormat(this.formatShowTime((Date) mtSummary.get("createDate")));
                List<Attachment> attachments = attachmentManager.getByReference(sb.getId(), sb.getId());
                sb.setSummaryAttCount(attachments == null ? 0 : attachments.size());
                summaryList.add(sb);
            }
        }
        flipInfo.setData(summaryList);
        return ok(flipInfo);
    }

    /**
     * 获取会议纪要
     *
     * @param recordId 会议纪要ID
     * @return <pre>
     * MtSummary对象
     * </pre>
     * @throws BusinessException
     */
    @GET
    @Path("summary/{recordId}")
    @RestInterfaceAnnotation
    public Response getMeetingSummary(@PathParam("recordId") Long recordId) throws BusinessException {
        Map<String, Object> retMap = new HashMap<String, Object>();
        String errorMsg = "";
        MeetingSummary mtSummary = null;
        MtSummaryBO sb = null;
        int memberNumber = 0;
        if (recordId == null) {
            errorMsg = ResourceUtil.getString("meeting.params.error");
        } else {
            try {
                User user = AppContext.getCurrentUser();

                //安全校验
                SecurityCheckParam param = new SecurityCheckParam(ApplicationCategoryEnum.meeting, user, recordId);
                SecurityCheck.isLicit(param);
                if (!param.getCheckRet()) {
                    errorMsg = param.getCheckMsg();
                    if (Strings.isBlank(errorMsg)) {
                        errorMsg = ResourceUtil.getString("meeting.view.noAccess");
                    }
                } else {
                    mtSummary = meetingSummaryManager.getSummaryById(recordId);
                    if (null != mtSummary) {
                        //会议纪要已撤销的情况
                        if (mtSummary.getState() == MeetingConstant.SummaryStateEnum.cancel.key()) {
                            retMap.put(RETURN_ERROR_MESSGAE, ResourceUtil.getString("meeting.summary.canceled"));
                            return success(retMap);
                        }

                        sb = new MtSummaryBO();
                        //ID 内容
                        sb.setId(mtSummary.getId());
                        //记录人
                        sb.setCreateUserName(Functions.showMemberNameOnly(Long.valueOf(mtSummary.getCreateUser())));
                        if (!"html".equalsIgnoreCase(mtSummary.getDataFormat())) {
                            V3XFile file = fileManager.getV3XFile(Long.valueOf(mtSummary.getContent()));
                            if (file != null) {
                                sb.setLastModified(DateUtil.formatDateTime(file.getUpdateDate()));
                                retMap.put("allowTrans", OfficeTransHelper.allowTrans(file));
                            }
                            sb.setContent(mtSummary.getContent());
                        } else {//HTML 正文需要被正文组件重新解析，才能正常显示其中的关联和附件
                            String htmlContentString = mtSummary.getContent();
                            sb.setContent(HtmlMainbodyHandler.replaceInlineAttachment(htmlContentString));
                        }
                        sb.setBodyType(mtSummary.getDataFormat());
                        sb.setCreateDateFormat(this.formatShowTime(mtSummary.getCreateDate()));

                        MtMeeting meeting = meetingManager.getMeetingById(mtSummary.getMeetingId());
                        MeetingBO meetingBO = meetingManager.getMeetingBO(meeting);
                        retMap.put("meeting", meetingBO);
                        //获取任务总数，在会议纪要页面，前端使用
                        int meetingTaskNum = 0;
                        if (AppContext.hasPlugin("taskmanage") && taskmanageApi != null) {
                            meetingTaskNum = taskmanageApi.countTaskSource(ApplicationCategoryEnum.meeting.getKey(), mtSummary.getMeetingId(), null, null);
                        }
                        retMap.put("meetingTaskNum", meetingTaskNum);
                        List<Long> memlist = new ArrayList<Long>();
                        //实际与会人
                        List<V3xOrgMember> scopesList = MeetingOrgHelper.getMembersByTypeAndId(mtSummary.getConferees(), orgManager);
                        for (V3xOrgMember scopes : scopesList) {
                            memlist.add(scopes.getId());
                        }

                        List<String> showNames = new ArrayList<String>();
                        if (!Strings.isEmpty(memlist)) {
                            for (int i = 0; i < memlist.size(); i++) {
                                if (memlist.get(i).equals(meeting.getRecorderId()) || memlist.get(i).equals(meeting.getEmceeId())) {
                                    continue;
                                }
                                String name = Functions.showMemberNameOnly(memlist.get(i));
	                        	/*MeetingReplyMemberVO memberVo = new MeetingReplyMemberVO();
	                        	memberVo.setMemberId(memlist.get(i));
	                        	memberVo.setMemberName(name);
	                        	//当前人员职位
	                        	memberVo.setMemberPost(OrgHelper.showOrgPostNameByMemberid(memlist.get(i)));
	                        	*/
                                showNames.add(name);
                                memberNumber++;
                            }
                            List<String> showActualName = new ArrayList<String>();
                            if (showNames.size() > 10) {
                                showActualName = showNames.subList(0, 10);
                            } else {
                                showActualName = showNames;
                            }
                            sb.setActualShowName(showActualName.toString().substring(1, showActualName.toString().length() - 1));
                        }
                        //附件和关联文档
                        List<Attachment> byReference = attachmentManager.getByReference(sb.getId(), sb.getId());
                        sb.setSummaryAttmentList(byReference);
                        int attCount = 0;
                        if (!Strings.isEmpty(byReference)) {
                            for (Attachment a : byReference) {
                                if (null != a.getType() && a.getType().intValue() == 0) {
                                    attCount++;
                                }
                            }
                            sb.setSummaryAttCount(attCount);
                        }
                    }
                }
            } catch (Throwable e) {
                errorMsg = ResourceUtil.getString("meeting.resource.dataError");
                retMap.put(RETURN_ERROR_MESSGAE, errorMsg);
                LOGGER.error("", e);
            }
        }
        retMap.put(RETURN_DATA, sb);
        retMap.put("memberNumber", memberNumber); //实际与会人数量
        return ok(retMap);
    }

    //格式化M3展示时间
    private String formatShowTime(Date d) {
        String paten = "HH:mm";
        String retuDate = "";
        if (d != null) {
            if (DateUtil.getYear(d) != DateUtil.getYear(new Date())) {//跨年
                paten = "yyyy-MM-dd " + paten;
                retuDate = DateUtil.format(d, paten);
            } else if (DateUtil.getMonth(new Date()) != DateUtil.getMonth(d)) {//跨月
                paten = "MM-dd " + paten;
                retuDate = DateUtil.format(d, paten);
            } else if (DateUtil.getDay(new Date()) != DateUtil.getDay(d)) {//跨日
                paten = "MM-dd " + paten;
                retuDate = DateUtil.format(d, paten);
            } else {//今天的会议
                retuDate = ResourceUtil.getString("meeting.list.date.today") + " " + DateUtil.format(d, paten);
            }
            return retuDate;
        }
        return "";
    }

    //小致页面时间展示格式化
    private String formatShowTime4XiaoZhi(Date d1, Date d2) {
        String paten1 = "HH:mm";
        String paten2 = "HH:mm";
        String retuDate = "";
        if (d1 != null && d2 != null) {
            if (DateUtil.getYear(d1) != DateUtil.getYear(d2)) {//跨年
                paten1 = "yyyy-MM-dd " + paten1;
                paten2 = "yyyy-MM-dd " + paten2;
                retuDate = DateUtil.format(d1, paten1) + "-" + DateUtil.format(d2, paten2);
            } else if (DateUtil.getMonth(d1) != DateUtil.getMonth(d2)) {//跨月
                paten1 = "MM-dd " + paten1;
                paten2 = "MM-dd " + paten2;
                retuDate = DateUtil.format(d1, paten1) + "-" + DateUtil.format(d2, paten2);
            } else if (DateUtil.getDay(d1) != DateUtil.getDay(d2)) {//跨日
                paten1 = "MM-dd " + paten1;
                paten2 = "MM-dd " + paten2;
                retuDate = DateUtil.format(d1, paten1) + "-" + DateUtil.format(d2, paten2);
            } else {
                if (DateUtil.getDay(new Date()) == DateUtil.getDay(d1)) {
                    retuDate = ResourceUtil.getString("meeting.list.date.today") + " " + DateUtil.format(d1, paten1) + "-" + DateUtil.format(d2, paten2);
                } else {
                    paten1 = "MM-dd " + paten1;
                    retuDate = DateUtil.format(d1, paten1) + "-" + DateUtil.format(d2, paten2);
                }
            }
            return retuDate;
        }
        return "";
    }

    /**
     * 获取邀请时选人界面过滤的人员
     *
     * @param Long meetingId 会议Id 必填
     * @return <pre>
     *  参会人员所有信息,如：[{id:181818,name:"杨海",type:"member",disable:true}]
     * </pre>
     * @throws BusinessException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("removeInvitePer")
    public Response removeInvitePer(@QueryParam("meetingId") Long meetingId) throws BusinessException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        MtMeeting bean = meetingManager.getMeetingById(Long.valueOf(meetingId));

        Map<Long, MtReply> list_allReply = replyManager.findAllByMeetingId(bean.getId());
        MeetingMemberVO meetingMember = meetingManager.getAllTypeMember(bean.getId(), bean);

        //发起人的回执
        List<MtReplyWithAgentInfo> senderExList = MeetingHelper.getReplyWithAgent(meetingMember.getSenderMembers(), list_allReply);

        //主持人的回执
        List<MtReplyWithAgentInfo> emceeExList = MeetingHelper.getReplyWithAgent(meetingMember.getEmceeMembers(), list_allReply);

        //记录人的回执
        List<MtReplyWithAgentInfo> recorderExList = MeetingHelper.getReplyWithAgent(meetingMember.getRecorderMembers(), list_allReply);

        //参会领导的回执
        List<MtReplyWithAgentInfo> replyLeaderExList = MeetingHelper.getReplyWithAgent(meetingMember.getLeaderMembers(), list_allReply);

        //与会人的回执
        List<MtReplyWithAgentInfo> replyExList = MeetingHelper.getReplyWithAgent(meetingMember.getConfereesMembers(), list_allReply);

        //告知人的回执
        List<MtReplyWithAgentInfo> impartExList = MeetingHelper.getReplyWithAgent(meetingMember.getImpartMembers(), list_allReply);

        // 邀请时选人界面过滤人员
        List<MtReplyWithAgentInfo> excludeReplyExList = new ArrayList<MtReplyWithAgentInfo>();
        if (senderExList.isEmpty()) {
            MtReplyWithAgentInfo senderEx = new MtReplyWithAgentInfo();
            senderEx.setReplyUserId(bean.getCreateUser());
            excludeReplyExList.add(senderEx);
        } else {
            excludeReplyExList.addAll(senderExList);
        }
        excludeReplyExList.addAll(emceeExList);
        excludeReplyExList.addAll(recorderExList);
        excludeReplyExList.addAll(replyExList);
        excludeReplyExList.addAll(replyLeaderExList);
        excludeReplyExList.addAll(impartExList);

        //封装数据
        for (MtReplyWithAgentInfo excludeReplyEx : excludeReplyExList) {
            Map<String, Object> retMap = new HashMap<String, Object>();
            retMap.put("id", excludeReplyEx.getReplyUserId());
            retMap.put("name", excludeReplyEx.getReplyUserName());
            retMap.put("type", "member");
            retMap.put("display", "none");
            list.add(retMap);
        }
        return ok(list);
    }

    /**
     * 获取参会人员是否有会议冲突详情
     *
     * @param params 参数map对象
     *               <pre>
     *                 类型                           名称                           必填                 备注
     *               String   meetingId      N    会议Id
     *               Long     beginDatetime  Y    会议开始时间
     *               Long     endDatetime    Y    会议结束时间
     *               String   emceeId        N    主持人ID(直接人员ID）
     *               String   recorderId     N    记录人ID(直接人员ID）
     *               String   conferees      Y    选择的会议参会人员ID（格式：Member|1212223,Member|1212223)
     *               </pre>
     * @return <pre>
     * boolean   state   会议冲突状态
     * List<ConfereesConflictVO>  message 会议冲突人员详细信息
     * </pre>
     * @throws BusinessException
     */
    @POST
    @Path("checkConfereesConflict")
    public Response checkConfereesConflict(Map<String, Object> params) throws BusinessException {
        Map<String, Object> result = new HashMap<String, Object>();
        boolean conflict = false;

        String meetingId = ParamUtil.getString(params, "meetingId");
        Long beginDatetime = ParamUtil.getLong(params, "beginDatetime");
        Long endDatetime = ParamUtil.getLong(params, "endDatetime");
        String recorderId = ParamUtil.getString(params, "recorderId");
        String emceeId = ParamUtil.getString(params, "emceeId");
        String conferees = ParamUtil.getString(params, "conferees");

        MtMeeting meeting = new MtMeeting();
        meeting.setMeetingId(Strings.isBlank(meetingId) ? -1l : Long.parseLong(meetingId));
        meeting.setCreateUser(AppContext.currentUserId());
        meeting.setEmceeId(Strings.isBlank(emceeId) ? -1l : Long.parseLong(emceeId));
        meeting.setRecorderId(Strings.isBlank(recorderId) ? -1l : Long.parseLong(recorderId));
        meeting.setConferees(conferees);
        meeting.setBeginDate(new Date(beginDatetime));
        meeting.setEndDate(new Date(endDatetime));

        try {
            conflict = confereesConflictManager.checkConfereesConflict(meeting);
        } catch (Exception e) {
            LOGGER.error("获取参会人员是否有会议冲突详情失败！" + e);
        }

        result.put("conflict", conflict); //返回是否冲突标志

        return ok(result);
    }

    @POST
    @Path("checkConfereesConflict4XiaoZhi")
    public Response checkConfereesConflict4XiaoZhi(Map<String, Object> params) throws BusinessException {
        boolean conflict = false;
        String meetingId = ParamUtil.getString(params, "meetingId");
        Integer sourceType = ParamUtil.getInt(params, "sourceType", -1);
        Long beginDate = ParamUtil.getLong(params, "beginDate");
        Long endDate = ParamUtil.getLong(params, "endDate");
        List<Object> memberList = (List<Object>) params.get("conferees");//与会人员
        StringBuilder confereesBuilder = new StringBuilder();
        if (memberList != null) {
            for (int i = 0; i < memberList.size(); i++) {
                Map member = (Map) memberList.get(i);
                confereesBuilder.append(member.get("type") + "|" + member.get("id") + ",");
            }
            if (confereesBuilder != null && confereesBuilder.toString().endsWith(",")) {
                confereesBuilder.deleteCharAt(confereesBuilder.length() - 1);
            }
        }

        MtMeeting meeting = new MtMeeting();
        meeting.setSourceType(sourceType);
        meeting.setBeginDate(new Date(beginDate));
        meeting.setEndDate(new Date(endDate));
        meeting.setMeetingId(Strings.isBlank(meetingId) ? -1l : Long.parseLong(meetingId));
        meeting.setCreateUser(AppContext.currentUserId());
        meeting.setConferees(confereesBuilder.toString());
        meeting.setEmceeId(AppContext.currentUserId());//主持人
        meeting.setRecorderId(AppContext.currentUserId());//记录人

        try {
            conflict = confereesConflictManager.checkConfereesConflict(meeting);
        } catch (Exception e) {
            LOGGER.error("获取参会人员是否有会议冲突详情失败！" + e);
        }

        if (conflict) {
            return success(null, ResourceUtil.getString("meeting.msg.xiaoz.conflictWarm"), 700001);
        } else {
            return success("success", "", 200);
        }
    }


    /**
     * 执行会议邀请
     *
     * @param meetingId Long Y 会议ID
     * @param conferees String Y 邀请人员信息(格式：Member|1212223,Member|1212223);注:某些环境下:|只能识别成%7C,因此需要对url转义
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("transInviteConferees")
    public Response transInviteConferees(@QueryParam("meetingId") Long meetingId, @QueryParam("conferees") String conferees) throws BusinessException {
        Map<String, Object> result = new HashMap<String, Object>();
        if (MeetingUtil.isIdNull(meetingId) || Strings.isBlank(conferees)) {
            return ok(errorParams());
        }
        String res = meetingManager.transInviteConferees(meetingId, conferees);
        result.put(SUCCESS_KEY, res);
        return ok(result);
    }

    /**
     * 获取会议目标为除otherMemberIds 的全体与会人员ID集合，未对代理人进行处理
     *
     * @param currentMemberIds Type|Ids
     * @param otherMemberIds
     * @return
     */
    private List<MtReplyWithAgentInfo> getImpartMemberIds(String currentMemberIds, List<Long> conferees, Map<String, String> otherMemberIds) {

        List<MtReplyWithAgentInfo> memberIds = new ArrayList<MtReplyWithAgentInfo>();
        if (Strings.isNotBlank(currentMemberIds)) {
            Set<V3xOrgMember> orgMember = null;
            try {
                orgMember = this.orgManager.getMembersByTypeAndIds(currentMemberIds);
                if (orgMember != null && orgMember.size() > 0) {
                    for (V3xOrgMember member : orgMember) {
                        Long memberId = member.getId();
                        if (memberId != null && otherMemberIds.containsValue(String.valueOf(memberId)) && conferees.contains(memberId)) {
                            continue;
                        }
                        MtReplyWithAgentInfo exMr = new MtReplyWithAgentInfo();
                        exMr.setReplyUserId(member.getId());
                        exMr.setReplyUserName(member.getName());
                        memberIds.add(exMr);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("获取会议目标为除otherMemberIds 的全体与会人员ID集合，未对代理人进行处理", e);
            }
        }
        return memberIds;
    }

    /**
     * 会议处理
     * POST
     * Path("mydetail/{meetingid}")
     * @param params 参数map对象
     * <pre>
     * 类型         名称                  必填                 备注
     * String   meetingId    Y           会议Id
     * String   userId       Y           当前用户
     * String   feedbackFlag Y           回执态度（未回执 -100 参加 1 不参加  0   待定 -1 ）
     * String   feedback     Y           回执内容
     * </pre>
     * @return
     * <pre>
     *    {result:msg} 结果数据
     * </pre>
     * @throws BusinessException
     */
	/*@POST
	@Path("handleMeeting")
	public Response handleMeeting(Map<String, Object> params) throws BusinessException{
		String flag = "";
		try {
			flag = mtMeetingManager.savemeetingReply(params);
		} catch (Exception e) {
			LOGGER.info("", e);
		}
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("result", flag);
		return ok(result);
	}*/


    /**
     * 获取单条会议数据<BR>
     *
     * @param long meetingId 会议Id 必填
     * @return <pre>
     * 	MtMeeting对象
     * </pre>
     */
    @GET
    @Path("{id}")
    @RestInterfaceAnnotation
    public Response getMeeting(@PathParam("id") long meetingId) throws BusinessException {
        return this.getMeeting(meetingId, new HashMap<String, Object>());
    }


    /**
     * 获取单条会议数据<BR>
     *
     * @param meetingId 会议id  必填
     * @param map       参数Map
     *                  <pre>
     *                  	类型	                  名称			必填	                   备注
     *                   Long             proxyId           N              被代理人Id
     *                   Long             currentUserId     N              当前用户Id
     *                  </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("{id}")
    public Response getMeeting(@PathParam("id") long meetingId, Map<String, Object> map) throws BusinessException {
        Long proxyId = ParamUtil.getLong(map, "proxyId", 0l);
        Long currentUserId = ParamUtil.getLong(map, "currentUserId", 0l);
        MtMeeting meeting = null;
        try {
            // SECURITY 访问安全检查
            if (AppContext.getCurrentUser() != null) {
                if (!SecurityCheck.isLicit(AppContext.getRawRequest(), AppContext.getRawResponse(), ApplicationCategoryEnum.meeting, AppContext.getCurrentUser(), meetingId, null, null)) {
                    BusinessException bexception = new BusinessException(ResourceUtil.getString("meeting.cmp.detail.tip.can.not.read"));
                    bexception.setCode("2003");
                    throw bexception;
                }
            }
            meeting = mtMeetingManager.getMtMeetingById(meetingId, proxyId, true, currentUserId);
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        if (proxyId != null && proxyId != 0l && proxyId != -1l) {
            currentUserId = proxyId;
        }

        List<CtpAffair> affairList = affairManager.getAffairs(ApplicationCategoryEnum.meeting, meetingId, currentUserId);
        if (Strings.isNotEmpty(affairList)) {
            boolean isLook = false;
            List<CtpAffair> updateAffairs = new ArrayList<CtpAffair>();
            for (CtpAffair affair : affairList) {
                if (affair.getSubState() == null || affair.getSubState() == SubStateEnum.col_pending_unRead.key()) {
                    affair.setSubState(SubStateEnum.col_pending_read.key());
                    updateAffairs.add(affair);
                    isLook = true;
                }
            }
            if (updateAffairs.size() > 0) {
                affairManager.updateAffairs(updateAffairs);
            }

            if (isLook && meeting != null) {
                List<MtReply> mList = replyManager.findByMeetingIdAndUserId(meeting.getId(), currentUserId);
                if (mList != null && mList.size() == 0) {
                    MtReply mtReply = new MtReply();
                    mtReply.setMeetingId(meeting.getId());
                    mtReply.setUserId(currentUserId);
                    if (meeting.getCreateUser().equals(Long.valueOf(currentUserId))) {
                        mtReply.setFeedbackFlag(1);
                        mtReply.setLookState(1);
                    } else {
                        mtReply.setFeedbackFlag(-100);
                        mtReply.setLookState(2);
                    }
                    mtReply.setLookTime(new Timestamp(System.currentTimeMillis()));
                    replyManager.save(mtReply);
                }
            }
        }
        return ok(meeting);
    }

    /**
     * 撤销会议
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               String	|	meetingId	|	Y	|	当前会议Id
     *               String	|	isBatch		|	N	|	周期性会议是否批量撤销（true，false）
     *               String	|	content		|	Y	| 	撤销附言
     *               String	|	sendSMS		|	N	|	是否发送短信
     *               </per>
     *               @return Map<String, String>
     *               @throws BusinessException
     */
    @POST
    @Path("cancelMeeting")
    public Response cancelMeeting(Map<String, String> params) throws BusinessException {

        Long meetingId = ParamUtil.getLong(params, "meetingId");
        String isBatch = ParamUtil.getString(params, "isBatch", "false");
        String content = ParamUtil.getString(params, "content");
        String sendSMS = ParamUtil.getString(params, "sendSMS", "false");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("meetingId", meetingId);
        parameterMap.put("isBatch", "false".equals(isBatch) ? false : true);
        parameterMap.put("currentUser", AppContext.getCurrentUser());
        parameterMap.put("content", content);
        parameterMap.put("sendSMS", "false".equals(sendSMS) ? false : true);
        boolean is_success = meetingManager.transCancelMeeting(parameterMap);

        Map<String, String> returnMessage = new HashMap<String, String>();

        String message = SUCCESS_KEY;
        if (!is_success) {
            message = RETURN_ERROR_MESSGAE;
        }
        returnMessage.put("message", message);
        return ok(returnMessage);
    }

    /**
     * 提前结束会议
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               String	|	meetingId	|	Y	|	当前会议Id
     *               </per>
     *               @return Map<String, String>
     *               @throws BusinessException
     */
    @POST
    @Path("advanceMeeting")
    public Response advanceMeeting(Map<String, String> params) throws BusinessException {
        Long meetingId = ParamUtil.getLong(params, "meetingId");
        String content = ParamUtil.getString(params, "content");
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("meetingId", meetingId);
        parameterMap.put("currentUser", AppContext.getCurrentUser());
        parameterMap.put("content", content);
        parameterMap.put("action", MeetingActionEnum.finishMeeting.name());
        parameterMap.put("endDatetime", DateUtil.currentDate());
        boolean is_success = meetingManager.transFinishAdvanceMeeting(parameterMap);

        Map<String, String> returnMessage = new HashMap<String, String>();

        String message = SUCCESS_KEY;
        if (!is_success) {
            message = RETURN_ERROR_MESSGAE;
        }
        returnMessage.put("message", message);
        return ok(returnMessage);
    }

    /**
     * 获取会议的震荡回复意见<BR>
     *
     * @param long meetingId  会议Id 必填
     * @return <pre>
     * 	List<MtReply> 意见列表
     * </pre>
     */
    @POST
    @Path("comments/{meetingid}")
    @RestInterfaceAnnotation
    public Response getMeetingComments(@PathParam("meetingid") long meetingId) {
        List<MtReply> commentList = null;
        try {
            commentList = mtMeetingManager.getAllReplysAndComments(meetingId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return ok(commentList);
    }

    /**
     * 错误参数提示
     *
     * @return
     */
    private Map<String, String> errorParams() {
        Map<String, String> retMap = new HashMap<String, String>();
        retMap.put("errorMsg", ResourceUtil.getString("meeting.error.errorParams"));
        return retMap;
    }


    /**
     * 获取已申请的会议室
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               String	|	pageNo	|	N	|	页数
     *               String	|	pageSize|	N	|	每页显示条数
     *               </per>
     *               @return flipInfo List<com.seeyon.ctp.rest.resources.MeetingRoomRestVO>
     *               @throws BusinessException
     */
    @POST
    @Path("getApplyMeemtingRooms")
    public Response getApplyMeemtingRooms(Map<String, String> params) throws BusinessException {
        User currentUser = AppContext.getCurrentUser();
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo.setPage(ParamUtil.getInt(params, "pageNo", 1));
        flipInfo.setSize(ParamUtil.getInt(params, "pageSize", 20));
        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("userId", currentUser.getId());

        String type = params.get("openFrom");
        if (Strings.isNotBlank(type) && "meetingCreate".equals(type)) {
            conditionMap.put("condition", "4");
            conditionMap.put("status", RoomAppStateEnum.pass.key());
        }

        //M3会议室列表请求来源
        if ("meetingRoomList".equals(type)) {
            String condition = ParamUtil.getString(params, "condition");
            if ("roomName".equals(condition)) { //根据会议室名称查询
                conditionMap.put("condition", "1");
                conditionMap.put("textfield", ParamUtil.getString(params, "roomName"));
            } else {//根据会议室申请时间查询
                conditionMap.put("condition", "2");
                conditionMap.put("textfield", ParamUtil.getString(params, "startDate"));
                conditionMap.put("textfield1", ParamUtil.getString(params, "endDate"));
            }
        }

        List<MeetingRoomListVO> meetingRoomListVO = meetingRoomListManager.findMyRoomAppList(conditionMap, flipInfo);

        List<MeetingRoomRestVO> meetingRoomList = new ArrayList<MeetingRoomRestVO>();
        for (MeetingRoomListVO roomVo : meetingRoomListVO) {
            MeetingRoomRestVO meetingroom = new MeetingRoomRestVO();
            BeanUtils.convert(meetingroom, roomVo);
            meetingRoomList.add(meetingroom);
        }
        flipInfo.setData(meetingRoomList);
        return ok(flipInfo);
    }

    /**
     * 获取待审核的会议室
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               String	|	pageNo	|	N	|	页数
     *               String	|	pageSize|	N	|	每页显示条数
     *               </per>
     *               @return flipInfo List<com.seeyon.ctp.rest.resources.MeetingRoomRestVO>
     *               @throws BusinessException
     */
    @POST
    @Path("getMeetingRoomAudits")
    public Response getMeetingRoomAudits(Map<String, String> params) throws BusinessException {
        User currentUser = AppContext.getCurrentUser();
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo.setPage(ParamUtil.getInt(params, "pageNo", 1));
        flipInfo.setSize(ParamUtil.getInt(params, "pageSize", 20));
        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("userId", currentUser.getId());

        //M3会议室列表请求来源
        String type = params.get("openFrom");
        if ("meetingRoomList".equals(type)) {
            String condition = ParamUtil.getString(params, "condition");
            if ("roomName".equals(condition)) { //根据会议室名称查询
                conditionMap.put("condition", "1");
                conditionMap.put("textfield", ParamUtil.getString(params, "roomName"));
            } else {//根据会议室申请时间查询
                conditionMap.put("condition", "4");
                conditionMap.put("textfield", ParamUtil.getString(params, "startDate"));
                conditionMap.put("textfield1", ParamUtil.getString(params, "endDate"));
            }
        }
        // TODO
        List<MeetingRoomListVO> meetingRoomListVO = meetingRoomListManager.findRoomPermList(conditionMap, flipInfo);

        List<MeetingRoomRestVO> meetingRoomList = new ArrayList<MeetingRoomRestVO>();
        for (MeetingRoomListVO roomVo : meetingRoomListVO) {
            MeetingRoomRestVO meetingroom = new MeetingRoomRestVO();
            BeanUtils.convert(meetingroom, roomVo);

            V3xOrgMember member = orgManager.getMemberById(roomVo.getAppPerId());
            //其他单位人员显示简称
            StringBuilder accountName = new StringBuilder();
            if (!currentUser.getLoginAccount().equals(member.getOrgAccountId())) {
                V3xOrgAccount account = orgManager.getAccountById(member.getOrgAccountId());
                accountName.append("(").append(account.getShortName()).append(")");
            }
            meetingroom.setAppPerName(member.getName() + accountName);
            meetingRoomList.add(meetingroom);
        }
        flipInfo.setData(meetingRoomList);
        return ok(flipInfo);
    }

    /**
     * 获取申请的会议室信息
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               Long	|	roomAppId	|	Y	|	会议室申请Id
     *               </per>
     *               @return MeetingRoomRestVO
     *               @throws BusinessException
     */
    @POST
    @Path("getMeetingRoomApp")
    public Response getMeetingRoomApp(Map<String, String> params) throws BusinessException {
        MeetingRoomRestVO mrRestVO = new MeetingRoomRestVO();

        User currentUser = AppContext.getCurrentUser();
        Long roomAppId = Long.valueOf(params.get("roomAppId"));
        String openFrom = String.valueOf(params.get("openFrom"));

        MeetingRoomApp mrapp = meetingRoomManager.getRoomAppById(roomAppId);

        if (mrapp == null) {
            Map<String, String> retMap = new HashMap<String, String>();
            retMap.put("errorMsg", ResourceUtil.getString("meeting.room.app.cancel"));
            return ok(retMap);
        }

        MeetingRoom room = this.meetingRoomManager.getRoomById(mrapp.getRoomId());
        mrRestVO.setRoomId(room.getId());
        //会议室申请状态
        mrRestVO.setAppStatus(mrapp.getStatus());
        mrRestVO.setMeetingId(mrapp.getMeetingId());
        mrRestVO.setRoomAppId(mrapp.getId());
        mrRestVO.setRoomName(room.getName());
        mrRestVO.setStartDatetime(String.valueOf(mrapp.getStartDatetime()).substring(0, 16));
        mrRestVO.setEndDatetime(String.valueOf(mrapp.getEndDatetime()).substring(0, 16));
        mrRestVO.setAdminLab(room.getAdminLab());

        //1、会议室审核状态为待审核、审核不通过，管理员是能在会议室使用时间内撤销  2、申请人只要会议室未过结束时间都能够撤销
        if ((("mrAuditList".equals(openFrom) && !mrapp.getStatus().equals(RoomAppStateEnum.pass.key())) || ("mrApproveList".equals(openFrom))) && mrapp.getEndDatetime().getTime() > DateUtil.currentDate().getTime()) {
            mrRestVO.setCancelMRApp(true);
        } else {
            mrRestVO.setCancelMRApp(false);
        }
        //wuxiaoju 提前结束
        String[] usedstatusArr = MeetingHelper.getRoomAppUsedStateName(mrapp.getStatus(), mrapp.getUsedStatus(), DateUtil.currentDate(), mrapp.getStartDatetime(), mrapp.getEndDatetime());
        int usedstatus = Integer.parseInt(usedstatusArr[1]);
        if (usedstatus != 0 && usedstatus != 2 && usedstatus != 3) {//0:未使用,2:已使用,3:提前结束
            mrRestVO.setFinishMRApp(true);
        } else {
            mrRestVO.setFinishMRApp(false);
        }
        //wuxiaoju 当会议室拥有会议时，查询出会议名称
        if (mrapp.getMeetingId() != null) {
            List<Long> meetingIdList = new ArrayList<Long>();
            meetingIdList.add(mrapp.getMeetingId());
            Map<Long, String> meetingNameMap = this.meetingManager.getMeetingNameMap(meetingIdList, null);
            mrRestVO.setMeetingName(meetingNameMap.get(mrapp.getMeetingId()));
        }
        String permDescription = "";
        //审核状态不是未审核的时候。查询出审核意见
        if (mrapp.getStatus() != 0) {
            MeetingRoomPerm roomPerm = this.meetingRoomManager.getRoomPermByAppId(roomAppId);
            if (roomPerm != null) {
                permDescription = roomPerm.getDescription();
            }
        }
        mrRestVO.setPermDescription(Strings.toHTML(permDescription));
        mrRestVO.setDescription(Strings.toHTML(mrapp.getDescription()));
        V3xOrgMember member = orgManager.getMemberById(mrapp.getPerId());
        //其他单位人员显示简称
        StringBuilder accountName = new StringBuilder();
        if (!currentUser.getLoginAccount().equals(member.getOrgAccountId())) {
            V3xOrgAccount account = orgManager.getAccountById(member.getOrgAccountId());
            accountName.append("(").append(account.getShortName()).append(")");
        }
        mrRestVO.setAppPerName(member.getName() + accountName);
        mrRestVO.setAppPerId(member.getId());
        Long meetingId = mrapp.getMeetingId();
        String resourcesNames = new String();
        if (null != meetingId) {
            resourcesNames = meetingResourcesManager.getResourceNamesByMeetingId(meetingId);
        }
        mrRestVO.setMeetingResources(resourcesNames);
        return ok(mrRestVO);
    }

    /**
     * 执行会议室审核
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               Long	|	roomAppId	|	Y	|	会议室申请Id
     *               String	|	description	|	N	|	审核意见
     *               Integer	|	permStatus	|	Y	|	审核态度（1：同意、2：不同意）
     *               </per>
     *               @return boolean true/false
     *               @throws BusinessException
     */
    @POST
    @Path("finishAuditMeetingRoom")
    public Response finishAuditMeetingRoom(Map<String, String> params) throws Exception {
        User currentUser = AppContext.getCurrentUser();
        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
        appVo.setRoomAppId(Long.valueOf(params.get("roomAppId")));
        appVo.setDescription(params.get("description"));
        appVo.setStatus(Integer.valueOf(params.get("permStatus")));
        appVo.setSystemNowDatetime(DateUtil.currentDate());
        appVo.setCurrentUser(currentUser);
        boolean result = false;
        if (LOCK.tryLock()) {
            try {
                result = this.meetingRoomManager.transPerm(appVo);
            } catch (Exception e) {
                LOGGER.error("执行会议室审核报错", e);
                throw new BusinessException(e.getMessage(), e);
            } finally {
                LOCK.unlock();
            }
        }
        return ok(result);
    }

    /**
     * 执行会议室提前结束
     *
     * @param params 传入参数
     *               <pre>
     *               类型                   |       名称                                | 必填  | 备注
     *               Long    |   roomAppId       | Y  | 会议室申请Id
     *               String  |  isContainMeeting | Y  | 会议室是否绑定会议（"false"：未绑定、"true"：绑定会议室）
     *               </per>
     *               @return Map<String, Object>
     *               @throws BusinessException
     */
    @SuppressWarnings("finally")
    @POST
    @Path("finishMeetingRoom")
    public Response finishMeetingRoom(Map<String, String> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        parameterMap.put("roomAppId", Long.valueOf(params.get("roomAppId")));
        parameterMap.put("currentUser", AppContext.getCurrentUser());
        parameterMap.put("endDatetime", DateUtil.currentDate());
        parameterMap.put("action", MeetingActionEnum.finishRoomApp.name());
        parameterMap.put("isContainMeeting", params.get("isContainMeeting"));
        boolean result = false;
        try {
            result = this.meetingRoomManager.transFinishAdvanceRoomApp(parameterMap);
        } catch (Exception e) {
            LOGGER.error("执行会议室提前结束报错", e);
            result = false;
        } finally {
            r_map.put(SUCCESS_KEY, result);
            return ok(r_map);
        }
    }

    /**
     * 撤销会议室
     *
     * @param params 传入参数
     *               <pre>
     *               类型 	|	名称	| 	必填	|	备注
     *               Long	|	roomAppId	|	Y	|	会议室申请Id
     *               String |	cancelContent	| Y	| 撤销附言
     *               </per>
     *               @return boolean true/false
     *               @throws BusinessException
     */
    @SuppressWarnings("finally")
    @POST
    @Path("cancelMeetRoomApp")
    public Response cancelMeetRoomApp(Map<String, String> params) throws BusinessException {
        User currentUser = AppContext.getCurrentUser();

        Long roomAppId = ParamUtil.getLong(params, "roomAppId");
        String cancelContent = ParamUtil.getString(params, "cancelContent");

        if (roomAppId == null || cancelContent == null) {
            return ok(errorParams());
        }

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("roomAppIdList", MeetingUtil.getIdList(params.get("roomAppId")));
        parameterMap.put("currentUser", currentUser);
        parameterMap.put("cancelContent", params.get("cancelContent"));
        parameterMap.put("action", MeetingActionEnum.cancelRoomApp.name());
        boolean result = false;
        try {
            result = this.meetingRoomManager.transCancelRoomApp(parameterMap);
        } catch (Exception e) {
            LOGGER.error("撤销会议室申请出错", e);
            result = false;
        } finally {
            return ok(result);
        }
    }


    /**
     * 执行会议室申请操作
     *
     * @param params 传入参数
     *               <pre>
     *                      类型    名称             必填     备注
     *                      String   roomId            Y     会议室ID
     *                      String   description       N     用途
     *                      String   startDatetime     Y     开始时间
     *                      String   endDatetime       Y     结束时间
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     * @throws ParseException
     */
    @POST
    @Path("execApp")
    public Response execApp(Map<String, Object> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();

        User user = AppContext.getCurrentUser();
        String roomId = ParamUtil.getString(params, "roomId");

        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("roomId", roomId);
        parameterMap.put("perId", String.valueOf(user.getId()));
        parameterMap.put("departmentId", String.valueOf(user.getDepartmentId()));
        parameterMap.put("description", ParamUtil.getString(params, "description"));
        parameterMap.put("startDatetime", ParamUtil.getString(params, "startDatetime"));
        parameterMap.put("endDatetime", ParamUtil.getString(params, "endDatetime"));
        //zhou:【开发区手机端添加字段】 开始
		parameterMap.put("sqrdh", ParamUtil.getString(params, "sqrdh"));
		parameterMap.put("sfygwhldcj", ParamUtil.getString(params, "sfygwhldcj"));
		parameterMap.put("hcyq", ParamUtil.getString(params, "hcyq"));
		parameterMap.put("ldid", ParamUtil.getString(params, "ldid"));
		parameterMap.put("ldname", ParamUtil.getString(params, "ldname"));
        //zhou:【开发区手机端添加字段】 结束

        //rest接口用   防止测试数据不合法
        List<MeetingRoom> meetingRooms = meetingRoomManager.getMyCanAppRoomList(user, -1, "", null);
        if (Strings.isEmpty(meetingRooms)) {
            r_map.put("errorMsg", ResourceUtil.getString("meeting.rest.hasNoCanApply"));
            return ok(r_map);
        }
        boolean canNotApply = true;
        for (MeetingRoom meetingRoom : meetingRooms) {
            if (roomId.equals(String.valueOf(meetingRoom.getId()))) {
                canNotApply = false;
                break;
            }
        }
        if (canNotApply) {
            r_map.put("errorMsg", ResourceUtil.getString("meeting.rest.canNotApply"));
            return ok(r_map);
        }

        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
        appVo.setAction("apply");
        appVo.setParameterMap(parameterMap);
        appVo.setSystemNowDatetime(DateUtil.currentDate());
        appVo.setCurrentUser(AppContext.getCurrentUser());

        //重复提交校验
        Long submitKey = AppContext.getCurrentUser().getId();
        boolean isLocked = meetingLockManager.isLock(submitKey);
        try {
            if (isLocked) {
                return null;
            }
            if (!meetingRoomManager.transApp(appVo)) {
                r_map.put("errorMsg", appVo.getMsg());
            } else {
                //返回会议室申请状态
                r_map.put("roomAppState", appVo.getMeetingRoomApp().getStatus());
            }
        } catch (Exception e) {
            r_map.put("errorMsg", e.getMessage());
            LOGGER.error("", e);
        } finally {
            if (!isLocked) {
                meetingLockManager.unLock(submitKey);
            }
        }

        return ok(r_map);
    }

    /**
     * 获取会议室列表
     *
     * @param params 传入参数
     *               <pre>
     *                      类型    名称             必填     备注
     *                      String   pageNo     Y     页数(1,2,3...)
     *                      String   pageSize   Y     每页显示条数
     *                      String   condition  N     查询条件类型(查询时选择的类型 time/roomName)
     *                      String   roomName   N     会议室名称
     *                      String   startDate  N     开始时间(yyyy-MM-dd HH:mm)
     *                      String   endDate    N     结束时间(yyyy-MM-dd HH:mm)
     *                </pre>
     * @return com.seeyon.ctp.util.FlipInfo
     * @throws BusinessException
     * @throws ParseException
     */
    @POST
    @Path("getMeetingRooms")
    public Response getMeetingRooms(Map<String, Object> params) throws BusinessException, ParseException {
        FlipInfo flipInfo = super.getFlipInfo();
        flipInfo.setPage(ParamUtil.getInt(params, "pageNo", 1));
        flipInfo.setSize(ParamUtil.getInt(params, "pageSize", 20));
        String openFrom = ParamUtil.getString(params, "openFrom");
        if ("createVideoMeeting".equals(openFrom)) {
            List<Map<String, Object>> meetingRoomVos;
            try {
                meetingRoomVos = meetingApplicationHandler.getVideoMeetingRoom(params);
                flipInfo.setData(MeetingUtil.pagenate(meetingRoomVos));
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        } else {
            List<MeetingRoomListVO> meetingRoomVos = meetingRoomM3Manager.getMeetingRooms(params, flipInfo);
            flipInfo.setData(meetingRoomVos);
        }

        return ok(flipInfo);
    }

    /**
     * 获取会议室详情
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   roomId     Y     会议室ID
     *                </pre>
     * @return com.seeyon.apps.meetingroom.vo.MeetingRoomVO
     * @throws BusinessException
     * @throws ParseException
     */
    @POST
    @Path("getMeetingRoom")
    public Response getMeetingRoom(Map<String, Object> params) throws BusinessException, ParseException {
        Long roomId = ParamUtil.getLong(params, "roomId");

        MeetingRoom meetingRoom = meetingRoomManager.getRoomById(roomId);
        Map<String, Object> r_map = new HashMap<String, Object>();
        if (meetingRoom == null) {
            r_map.put(RETURN_ERROR_MESSGAE, ResourceUtil.getString("meeting.meetingRoomDetail.noMeetingRoom"));
            return ok(r_map);
        }

        //处理管理员姓名
        String[] meetingRoomAdmins = meetingRoom.getAdmin().split(",");
        StringBuilder sbAdminNames = new StringBuilder();
        for (String meetingRoomAdmin : meetingRoomAdmins) {
            if (Strings.isNotBlank(meetingRoomAdmin)) {
                V3xOrgMember member = orgManager.getMemberById(Long.valueOf(meetingRoomAdmin));
                sbAdminNames.append(member.getName() + ",");
            }
        }
        String adminNames = sbAdminNames.toString();
        if (Strings.isNotBlank(adminNames) && adminNames.endsWith(",")) {
            adminNames = adminNames.substring(0, adminNames.length() - 1);
        }

        MeetingRoomVO meetingRoomVo = new MeetingRoomVO();
        meetingRoomVo.setMeetingRoom(meetingRoom);
        meetingRoomVo.setAdminNames(adminNames);

        //图片处理
        List<Attachment> att = attachmentManager.getByReference(meetingRoom.getId(), RoomAttEnum.image.key());
        meetingRoomVo.setAttatchImage(att);

        return ok(meetingRoomVo);
    }

    /**
     * 获取会议室预订所有天的集合
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   roomId     Y     会议室ID
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     * @throws ParseException
     */
    @POST
    @Path("getOrderDate")
    public Response getOrderDate(Map<String, Object> params) throws BusinessException, ParseException {
        Long roomId = ParamUtil.getLong(params, "roomId");

        if (roomId == null) {
            return ok(errorParams());
        }

        Map<String, Object> queryParam = new HashMap<String, Object>(16);
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(RoomAppStateEnum.wait.key());
        statusList.add(RoomAppStateEnum.pass.key());
        queryParam.put("roomId", roomId);
        queryParam.put("status", statusList);
        List<MeetingRoomApp> list = meetingRoomAppManager.findMeetingRoomApps(queryParam);
        Set<String> set = new HashSet<String>();

        //计数，循环超365则直接返回
        int count = 0;
        String startDatetime = "";
        String endDatetime = "";
        Date tempDate;

        for (MeetingRoomApp app : list) {
            startDatetime = Datetimes.format(app.getStartDatetime(), Datetimes.dateStyle);
            endDatetime = Datetimes.format(app.getEndDatetime(), Datetimes.dateStyle);

            set.add(startDatetime);

            if (startDatetime.equals(endDatetime)) {
                continue;
            }

            count = 0;
            while (!startDatetime.equals(endDatetime)) {
                //开始时间加一天
                tempDate = Datetimes.parse(startDatetime, Datetimes.dateStyle);
                tempDate = new Date(tempDate.getTime() + (24 * 60 * 60 * 1000));
                startDatetime = Datetimes.format(tempDate, Datetimes.dateStyle);
                set.add(startDatetime);
                //循环365次退出
                if (count++ > 365) {
                    break;
                }
            }
        }
        List<String> r_list = new ArrayList<String>();
        r_list.addAll(set);
        return ok(r_list);
    }

    /**
     * 获取会议室申请信息
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   roomId     Y     会议室ID
     *                      String   qDate             Y     查询日期 (yyyy-MM-dd)
     *                </pre>
     * @return List<com.seeyon.apps.meetingroom.po.MeetingRoomApp>
     * @throws BusinessException
     * @throws ParseException
     */
    @POST
    @Path("getMeetingRoomApps")
    public Response getMeetingRoomApps(Map<String, Object> params) throws BusinessException, ParseException {
        String roomId = ParamUtil.getString(params, "roomId");
        String qDate = ParamUtil.getString(params, "qDate");

        String sStartDate = qDate + " 00:00:00";
        String sEndDate = qDate + " 00:00:00";

        Date startDate = Datetimes.parse(sStartDate, Datetimes.datetimeStyle);
        Date endDate = DateUtil.addDay(Datetimes.parse(sEndDate, Datetimes.datetimeStyle), 1);

        List<Long> roomIdList = new ArrayList<Long>();
        roomIdList.add(Long.valueOf(roomId));

        List<MeetingRoomApp> meetingRoomApps = meetingRoomManager.getUsedRoomAppListByDate(startDate, endDate, roomIdList, true);

        List<MeetingRoomOccupancyVO> meetingRoomOccupancys = copyAppToOccupancyVO(meetingRoomApps);

        return ok(meetingRoomOccupancys);
    }

    /**
     * 复制申请信息至占用情况VO
     *
     * @return
     * @throws BusinessException
     */
    private List<MeetingRoomOccupancyVO> copyAppToOccupancyVO(List<MeetingRoomApp> meetingRoomApps) throws BusinessException {
        List<MeetingRoomOccupancyVO> occupancyVos = new ArrayList<MeetingRoomOccupancyVO>();
        for (MeetingRoomApp app : meetingRoomApps) {
            MeetingRoomOccupancyVO vo = new MeetingRoomOccupancyVO();
            vo.setStartDatetime(app.getStartDatetime());
            vo.setEndDatetime(app.getEndDatetime());
            vo.setDescription(Strings.toHTML(app.getDescription()));
            vo.setAppPerName(orgManager.getMemberById(app.getPerId()).getName());
            vo.setStatus(app.getStatus());
            vo.setAppId(app.getId());
            vo.setFinish(app.getEndDatetime().before(new Date()));
            occupancyVos.add(vo);
        }
        return occupancyVos;
    }

    /**
     * 获取会议室申请信息
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   appId     Y     申请ID
     *                </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("getMeetingRoomAppDetail")
    public Response getMeetingRoomAppDetail(Map<String, Object> params) throws BusinessException {
        Long appId = ParamUtil.getLong(params, "appId");
        MeetingRoomApp app = meetingRoomManager.getRoomAppById(appId);

        MeetingRoom room = meetingRoomManager.getRoomById(app.getRoomId());
        V3xOrgMember member = orgManager.getMemberById(app.getPerId());
        V3xOrgDepartment department = orgManager.getDepartmentById(app.getDepartmentId());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("startDatetime", Datetimes.format(app.getStartDatetime(), Datetimes.datetimeWithoutSecondStyle));
        map.put("endDatetime", Datetimes.format(app.getEndDatetime(), Datetimes.datetimeWithoutSecondStyle));
        map.put("roomName", room.getName());
        map.put("appPerName", member.getName());
        map.put("appDepartment", department.getName());
        map.put("description", Strings.toHTML(app.getDescription()));
        map.put("roomId", room.getId());
        map.put("perId", app.getPerId());

        return ok(map);
    }

    /**
     * 新建会议初始化页面
     *
     * @return com.seeyon.ctp.common.authenticate.domain.User
     * @throws BusinessException
     */
    @POST
    @Path("create")
    public Response create() throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();
        User user = AppContext.getCurrentUser();
        r_map.put("userId", String.valueOf(user.getId()));
        r_map.put("userName", user.getName());
        List<MeetingType> meetingTypeList = meetingTypeManager.getMeetingTypeList(user.getLoginAccount());
        for (MeetingType meetingType : meetingTypeList) {
            meetingType.setShowName(ResourceUtil.getString(meetingType.getName()));
        }
        r_map.put("meetingType", meetingTypeList);
        if (MeetingUtil.isVideoPluginAvailable()) {
            try {
                MeetingVideoManager meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
                if (meetingVideoManager != null && meetingVideoManager.isMeetingVideoEnable() && meetingVideoManager.canSendMeeting(AppContext.currentUserId())) {
                    if (meetingVideoManager.canChooseVideoMeetingRoom()) {
                        r_map.put("isShowVideoRoom", true);
                    } else {
                        r_map.put("isShowMeetingNature", meetingVideoManager.isSupportCreateMobileMeeting());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        /**
         * 会议是否可以手动输入会议地址
         */
        boolean isMeetingPlaceInputAble = meetingSettingManager.isMeetingPlaceInputAble();
        r_map.put("isMeetingPlaceInputAble", isMeetingPlaceInputAble);

        //外部会议选项
        boolean enablePublicMeeting = orgManager.accessedByVisitor(ApplicationCategoryEnum.meeting.name(), AppContext.currentAccountId());
        r_map.put("enablePublicMeeting", enablePublicMeeting);

        //会议室权限
        r_map.put("haveMeetingRoomApp", menuPurviewUtil.isHaveMeetingRoomApp(user));
        return ok(JSONUtil.toJSONString(r_map));
    }

    /**
     * 会议发送
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   title        Y     会议名称
     *                      String   beginDate    Y     开始时间 (yyyy-MM-dd hh:mm:ss)
     *                      String   endDate      Y     结束时间 (yyyy-MM-dd hh:mm:ss)
     *                      String   conferees    Y     与会人(Member|id1,Member|id2 ……)
     *                      String   emceeId      Y     主持人
     *                      String   recorderId   N     记录人
     *                      String   impart       N     告知人(Member|id1,Member|id2 ……)
     *                      String   beforeTime   Y     提前提醒(0：无；5：5分钟；10：10分钟；15：15分钟；
     *                      30：30分钟；60：1小时；120：2小时；180：3小时；240：4小时；480：8小时；720：0.5天；
     *                      1440：1天；2880：2天；4320：3天；10080：1周)
     *                      String   content      N     正文内容
     *                      String   selectRoomType Y   会议室选择类型(applied：已申请；apply：申请会议室；mtPlace：手动输入)
     *                      String   meetingPlace N     会议地点
     *                      String   isHasAtt     Y     是否存在附件("true"/"false")
     *                      Long     roomId       N     会议室ID
     *                      String   roomAppBeginDate N 会议室申请开始时间(yyyy-MM-dd hh:mm:ss)
     *                      String   roomAppEndDate   N 会议室申请结束时间(yyyy-MM-dd hh:mm:ss)
     *                      Long     roomAppId    N     会议室申请ID
     *                      String   type         Y     执行动作(send/save)
     *                      String   sendType     N     是否来自小致语音发布的会议("speechRobot"表示小致语音会议发布)
     *                      Long     meetingId    N     会议ID
     *                      String   meetingNature Y    会议方式（1：普通；2：视频）
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Path("send")
    public Response send(Map<String, Object> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();

        String impart = ParamUtil.getString(params, "impart");

        impart = removeRepeat(impart);//去重

        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("title", ParamUtil.getString(params, "title"));
        parameterMap.put("impart", impart);
        parameterMap.put("content", ParamUtil.getString(params, "content"));
        parameterMap.put("meetingPlace", ParamUtil.getString(params, "meetingPlace", ""));
        parameterMap.put("bodyType", "HTML");
        parameterMap.put("projectId", "-1");
        parameterMap.put("meetingPassword", ParamUtil.getString(params, "password"));
        //设置会议分类
        parameterMap.put("meetingTypeId", ParamUtil.getString(params, "meetingTypeId"));
        //roomapp对象需要的数据
        parameterMap.put("roomId", ParamUtil.getString(params, "roomId"));
        parameterMap.put("roomAppId", ParamUtil.getString(params, "roomAppId"));
        parameterMap.put("roomAppBeginDate", ParamUtil.getString(params, "roomAppBeginDate"));
        parameterMap.put("roomAppEndDate", ParamUtil.getString(params, "roomAppEndDate"));
        if (ParamUtil.getLong(params, "roomId") != null) {
            MeetingRoom meetingRoom = meetingRoomManager.getRoomById(ParamUtil.getLong(params, "roomId"));
            parameterMap.put("roomNeedApp", String.valueOf(meetingRoom.getNeedApp()));
        }
        //设置视频会议室相关参数
        parameterMap.put("videoRoomId", ParamUtil.getString(params, "videoRoomId"));
        parameterMap.put("oldVideoRoomAppId", ParamUtil.getString(params, "oldVideoRoomAppId"));
        parameterMap.put("videoRoomName", ParamUtil.getString(params, "videoRoomName"));
        parameterMap.put("videoRoomAppBeginDate", ParamUtil.getString(params, "videoRoomAppBeginDate"));
        parameterMap.put("videoRoomAppEndDate", ParamUtil.getString(params, "videoRoomAppEndDate"));

        //签到二维码
        parameterMap.put("qrCodeSign", ParamUtil.getString(params, "qrCodeSign", "0"));
        //外部会议
        parameterMap.put("isPublic", ParamUtil.getString(params, "isPublic", "0"));

        Long meetingId = ParamUtil.getLong(params, "meetingId", -1L);

        Integer sourceType = ParamUtil.getInt(params, "sourceType", -1); //会议数据来源
        String type = ParamUtil.getString(params, "type"); //区分会议发布还是会议保存待发
        String sendType = ParamUtil.getString(params, "sendType"); //区分是否来自小致语音发布的会议

        MeetingNewVO newVo = new MeetingNewVO();
        if (ApplicationCategoryEnum.xiaoz.key() == sourceType && "send".equals(type)) {

            /**
             * 小致语音发布会议需要塞默认值
             */
            if ("speechRobot".equals(sendType)) {
                parameterMap.put("emceeId", AppContext.getCurrentUser().getId() + "");//主持人
                parameterMap.put("recorderId", AppContext.getCurrentUser().getId() + "");//记录人
                parameterMap.put("isHasAtt", "false");  //无附件
                parameterMap.put("selectRoomType", "apply"); //申请会议室
                parameterMap.put("meetingNature", "1"); //普通会议
                parameterMap.put("beforeTime", "0");
            } else {
                /**
                 * 小致在H5页面点击会议发布
                 */
                parameterMap.put("emceeId", ParamUtil.getString(params, "emceeId"));
                parameterMap.put("recorderId", ParamUtil.getString(params, "recorderId"));
                parameterMap.put("beforeTime", ParamUtil.getString(params, "beforeTime"));
                parameterMap.put("isHasAtt", ParamUtil.getString(params, "isHasAtt"));
                parameterMap.put("selectRoomType", ParamUtil.getString(params, "selectRoomType"));
                parameterMap.put("meetingNature", ParamUtil.getString(params, "meetingNature"));
            }
            long beginDate = ParamUtil.getLong(params, "beginDate", -1L);//开始时间
            long endDate = ParamUtil.getLong(params, "endDate", -1L);//结束时间
            /**
             * 如果开始时间和结束时间同时传入则校验开始时间必须早于结束时间
             */
            if (beginDate != -1 && endDate != -1) {
                long timeFrame = endDate - beginDate;
                if (timeFrame < 0) {
                    r_map.put("code", 500);
                    r_map.put("message", ResourceUtil.getString("mt.common.error.message1"));
                    return ok(r_map);
                }
            }
            parameterMap.put("beginDate", DateUtil.format(new Date(beginDate), Datetimes.datetimeStyle));
            if (endDate == -1) { //结束时间未传时默认为开始时间之后60分钟
                parameterMap.put("endDate", DateUtil.format(new Date(beginDate + 60 * 60 * 1000), Datetimes.datetimeStyle));
            } else {
                parameterMap.put("endDate", DateUtil.format(new Timestamp(endDate), Datetimes.datetimeStyle));
            }
            List<Object> memberList = (List<Object>) params.get("conferees");//与会人员
            StringBuilder confereesBuilder = new StringBuilder();
            if (memberList != null) {
                for (int i = 0; i < memberList.size(); i++) {
                    Map member = (Map) memberList.get(i);
                    Long memId = MapUtils.getLong(member, "id");
                    if (AppContext.currentUserId() != memId.longValue()) {
                        confereesBuilder.append(member.get("type") + "|" + memId + ",");
                    }
                }
                if (confereesBuilder != null && confereesBuilder.toString().endsWith(",")) {
                    confereesBuilder.deleteCharAt(confereesBuilder.length() - 1);
                }
            }
            parameterMap.put("conferees", confereesBuilder.toString());
            parameterMap.put("sourceType", ApplicationCategoryEnum.xiaoz.key() + "");
            parameterMap.put("sourceId", ParamUtil.getString(params, "sourceId"));
        } else {
            parameterMap.put("emceeId", ParamUtil.getString(params, "emceeId"));
            parameterMap.put("recorderId", ParamUtil.getString(params, "recorderId"));
            String conferees = ParamUtil.getString(params, "conferees");
            conferees = removeRepeat(conferees);//去重
            parameterMap.put("conferees", conferees);
            parameterMap.put("beforeTime", ParamUtil.getString(params, "beforeTime"));
            parameterMap.put("beginDate", ParamUtil.getString(params, "beginDate"));
            parameterMap.put("endDate", ParamUtil.getString(params, "endDate"));
            parameterMap.put("selectRoomType", ParamUtil.getString(params, "selectRoomType"));
            parameterMap.put("isHasAtt", ParamUtil.getString(params, "isHasAtt"));
            parameterMap.put("meetingNature", ParamUtil.getString(params, "meetingNature"));
            parameterMap.put("sourceType", ParamUtil.getString(params, "sourceType"));
            parameterMap.put("sourceId", ParamUtil.getString(params, "sourceId"));
        }

        newVo.setAction(type);
        newVo.setMeetingId(meetingId);
        newVo.setRoomId(ParamUtil.getLong(params, "roomId", -1L));
        newVo.setRoomAppId(ParamUtil.getLong(params, "roomAppId", -1L));
        newVo.setCategory(MeetingCategoryEnum.single.key());
        newVo.setIsBatch(false);
        newVo.setSelectRoomType(ParamUtil.getString(params, "selectRoomType"));
        newVo.setCurrentUser(AppContext.getCurrentUser());
        if (meetingId == -1) {
            newVo.setParameterMap(parameterMap);
        } else {
            MtMeeting meeting = meetingManager.getMeetingById(meetingId);
            parameterMap.put("bodyType", meeting.getDataFormat());//覆盖一次
            parameterMap.put("SendTextMessages", meeting.getIsSendMessage() ? "1" : "0");
            parameterMap.put("projectId", null != meeting.getProjectId() ? String.valueOf(meeting.getProjectId()) : "-1");

            List<MeetingResources> meetingResources = meetingResourcesManager.getMeetingResourceListByMeetingId(meetingId);
            StringBuilder sb = new StringBuilder();
            for (MeetingResources meetingResource : meetingResources) {
                sb.append(meetingResource.getResourceId() + ",");
            }
            parameterMap.put("resourcesId", Strings.isNotBlank(sb.toString()) ? sb.toString().substring(0, sb.toString().length() - 1) : "");

            parameterMap.put("mtTitle", meeting.getMtTitle());
            parameterMap.put("leader", meeting.getLeader());
            parameterMap.put("attender", meeting.getAttender());
            parameterMap.put("tel", meeting.getTel());
            parameterMap.put("notice", meeting.getNotice());
            parameterMap.put("plan", meeting.getPlan());

            newVo.setParameterMap(parameterMap);
        }
        newVo.setSystemNowDatetime(DateUtil.currentDate());
        newVo.setIsM3(true);
        AppContext.putThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY, ParamUtil.getString(params, "_json_params"));

        //重复提交校验
        Long submitKey = AppContext.getCurrentUser().getId();
        boolean isLocked = meetingLockManager.isLock(submitKey);
        try {
            if (isLocked) {
                return null;
            }

            if ("send".equals(type)) {
                boolean result = meetingNewManager.transSend(newVo);
                if (!result) {
                    r_map.put("errorMsg", newVo.getErrorMsg());
                }
                //返回会议室申请状态
                if (newVo.getMeeting() != null) {
                    r_map.put("roomAppState", newVo.getMeeting().getRoomState());
                }
                //适配小致语音格式
                if (ApplicationCategoryEnum.xiaoz.key() == sourceType) {
                    if (!result) {
                        r_map.put("code", 500);
                        r_map.put("message", newVo.getErrorMsg());
                    } else {
                        r_map.put("code", 200);
                        List<Map<String, Object>> resultMap = Lists.newArrayList();
                        Long sourceId = ParamUtil.getLong(params, "sourceId", -1L);
                        ;
                        List<MtMeeting> meetings = meetingManager.findMeetingsByRelaId(sourceType, sourceId);
                        if (CollectionUtils.isNotEmpty(meetings)) {
                            for (MtMeeting mt : meetings) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put("title", mt.getTitle());
                                map.put("createUserId", mt.getCreateUser());
                                map.put("createUserName", mt.getCreateUserName());
                                map.put("beginDate", mt.getBeginDate().getTime());
                                map.put("endDate", mt.getEndDate().getTime());
                                map.put("showTime", formatShowTime4XiaoZhi(mt.getBeginDate(), mt.getEndDate()));
                                map.put("sourceId", mt.getSourceId());
                                map.put("meetingId", mt.getId());
                                resultMap.add(map);
                            }
                        }
                        r_map.put("data", resultMap);
                        r_map.put("message", ResourceUtil.getString("mt.resource.send.message"));
                    }
                }
            } else if ("save".equals(type)) {
                if (!meetingNewManager.transSave(newVo)) {
                    r_map.put("errorMsg", newVo.getErrorMsg());
                }
            }
            r_map.put("type", type);
        } catch (Exception e) {
            if (ApplicationCategoryEnum.xiaoz.key() != sourceType) {
                r_map.put("errorMsg", e.getMessage());
            } else {
                r_map.put("code", 500);
                r_map.put("message", e.getMessage());
            }
            LOGGER.error("", e);
        } finally {
            if (!isLocked) {
                meetingLockManager.unLock(submitKey);
            }
            removeThreadContext();
            MeetingRoomAppVO appVo = newVo.getMeetingRoomAppVO();
            meetingValidationManager.clearMeetingRoomAppCache(appVo);
        }
        return ok(r_map);
    }

    /**
     * 会议室申请时间是否被占用
     * 注：此校验仅支持非周期会议，如果之后支持周期会议，需要增加传参以及修改调用方法
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   beginDate    Y     开始时间 (yyyy-MM-dd hh:mm:ss)
     *                      String   endDate      Y     结束时间 (yyyy-MM-dd hh:mm:ss)
     *                      Long     roomId       Y     会议室ID
     *                      Long     meetingId    N     会议ID
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Path("checkMeetingRoomConflict")
    public Response checkMeetingRoomConflict(Map<String, Object> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();

        Date beginDate = DateUtil.toDate(ParamUtil.getString(params, "beginDate"));
        Date endDate = DateUtil.toDate(ParamUtil.getString(params, "endDate"));
        Long roomId = ParamUtil.getLong(params, "roomId", -1l);
        Long meetingId = ParamUtil.getLong(params, "meetingId", -1l);

        if (MeetingUtil.isIdNull(roomId) || beginDate == null || endDate == null) {
            return ok(errorParams());
        }

        boolean isRepeat = false;
        try {
            isRepeat = meetingValidationManager.checkRoomUsed(roomId, beginDate, endDate, meetingId, null);
            if (!isRepeat) {
                r_map.put("errorMsg", ResourceUtil.getString("mr.alert.cannotapp"));
            }
            r_map.put(SUCCESS_KEY, "true");
        } catch (Exception e) {
            r_map.put("errorMsg", e.getMessage());
            LOGGER.error(e);
        }

        return ok(r_map);
    }

    /**
     * 去除人员与部门之间重复数据
     *
     * @throws BusinessException
     */
    private String removeRepeat(String input) throws BusinessException {
        if (Strings.isBlank(input)) {
            return "";
        }
        String[] allData = input.split(",");

        List<V3xOrgMember> list_member = new ArrayList<V3xOrgMember>();
        String authType = "";
        Long authId;
        //获取所有人员信息
        for (String auth : allData) {
            authType = auth.split("\\|")[0];
            authId = Long.valueOf(auth.split("\\|")[1]);
            if (V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(authType)) {
                List<V3xOrgMember> members = orgManager.getAllMembersByAccountId(authId, 1, true, true, null, null, null);
                list_member.addAll(members);
            } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(authType)) {
                List<V3xOrgMember> members = orgManager.getAllMembersByDepartmentId(authId, true, 1, null, true, null, null, null);
                list_member.addAll(members);
            }/*  需要单独剔除人员类别
			else if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(authType)){
				V3xOrgMember v3xOrgMember = orgManager.getMemberById(authId);
				list_member.add(v3xOrgMember);
			}*/ else if (V3xOrgEntity.ORGENT_TYPE_TEAM.equals(authType)) {
                List<V3xOrgMember> members = orgManager.getMembersByTeam(authId);
                list_member.addAll(members);
            } else if (V3xOrgEntity.ORGENT_TYPE_POST.equals(authType)) {
                List<V3xOrgMember> members = orgManager.getMembersByPost(authId);
                list_member.addAll(members);
            } else if (V3xOrgEntity.ORGENT_TYPE_LEVEL.equals(authType)) {
                List<V3xOrgMember> members = orgManager.getMembersByLevel(authId);
                list_member.addAll(members);
            }
        }
        //去重
        HashSet<V3xOrgMember> set = new HashSet<V3xOrgMember>(list_member);
        list_member.clear();
        list_member.addAll(set);

        //获取所有人员的ID
        List<Long> allMemberIds = new ArrayList<Long>();
        for (V3xOrgMember member : list_member) {
            allMemberIds.add(member.getId());
        }

        StringBuilder output = new StringBuilder();
        for (String auth : allData) {
            if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(auth.split("\\|")[0])) {
                Long memberId = Long.valueOf(auth.split("\\|")[1]);
                if (!allMemberIds.contains(memberId)) {
                    output.append(auth + ",");
                }
            } else {
                output.append(auth + ",");
            }
        }
        return output.toString().substring(0, output.toString().length() - 1);
    }

    /**
     * 获取会议修改所需元素
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   meetingId     Y     会议ID
     *                </pre>
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("getMeetingModifyElement")
    public Response getMeetingModifyElement(Map<String, Object> params) throws BusinessException {
        Long meetingId = ParamUtil.getLong(params, "meetingId");
        MtMeeting meeting = meetingManager.getMeetingById(meetingId);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("meetingName", meeting.getTitle());
        map.put("startDate", Datetimes.format(meeting.getBeginDate(), Datetimes.datetimeWithoutSecondStyle));
        map.put("endDate", Datetimes.format(meeting.getEndDate(), Datetimes.datetimeWithoutSecondStyle));
        map.put("conferees", meeting.getConfereesNames());
        map.put("conferees_value", meeting.getConferees());
        map.put("host", meeting.getEmceeName());
        map.put("host_value", meeting.getEmceeId());
        map.put("recoder", meeting.getRecorderName());
        map.put("recoder_value", meeting.getRecorderId());
        map.put("notify", meeting.getImpartNames());
        map.put("notify_value", meeting.getImpart());
        map.put("reminder", meeting.getBeforeTime());
        map.put("content", meeting.getContent());
        map.put("data_format", meeting.getDataFormat());
        map.put("meetingNature_value", meeting.getMeetingType());
        map.put("meeting_password", meeting.getMeetingPassword());
        map.put("qrCodeSign", meeting.getQrCodeSign());
        map.put("isPublic", meeting.getIsPublic());

        if (!"html".equalsIgnoreCase(meeting.getDataFormat())) {
            V3XFile file = fileManager.getV3XFile(Long.valueOf(meeting.getContent()));
            if (file != null) {
                map.put("lastModified", DateUtil.formatDateTime(file.getUpdateDate()));
                //是否允许office转换
                map.put("allowTrans", OfficeTransHelper.allowTrans(file));
            }
        } else {//HTML 正文需要被正文组件重新解析，才能正常显示其中的关联和附件
            String htmlContentString = meeting.getContent();
            map.put("content", HtmlMainbodyHandler.replaceInlineAttachment(htmlContentString));
        }

        Long roomId = meeting.getRoom();
        if (roomId != null && roomId != -1) {
            MeetingRoom room = meetingRoomManager.getRoomById(roomId);
            map.put("meetingPlace", room.getName());
            map.put("meetingPlace_value", room.getId());
            map.put("meetingPlace_type", "applied");

            MeetingRoomApp meetingRoomApp = meetingRoomManager.getRoomAppByMeetingId(meetingId);
            map.put("roomAppBeginDate", Datetimes.format(meetingRoomApp.getStartDatetime(), Datetimes.datetimeWithoutSecondStyle));
            map.put("roomAppEndDate", Datetimes.format(meetingRoomApp.getEndDatetime(), Datetimes.datetimeWithoutSecondStyle));
            map.put("meetingPlace_value1", meetingRoomApp.getId());
        } else {
            map.put("meetingPlace", meeting.getMeetPlace());
            map.put("meetingPlace_type", "mtPlace");
        }

        // 会议类型
        MeetingType mt = meetingTypeManager.getMeetingTypeById(meeting.getMeetingTypeId());
        map.put("meetingTypeName", mt == null ? "" : ResourceUtil.getString(mt.getName()));
        map.put("meetingTypeId", meeting.getMeetingTypeId());
        if (mt != null && MeetingTypeCategoryEnum.form.key() == mt.getType()) {
            map.put("isFormTrigger", "1");
        } else {
            map.put("isFormTrigger", "0");
        }
        //缓存数据
        map.put("conferees_fill", this.analyze(meeting.getConferees()));
        map.put("notify_fill", this.analyze(meeting.getImpart()));
        if (Strings.isNotBlank(meeting.getEmceeName())) {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            Map<String, String> map1 = new HashMap<String, String>();
            map1.put("id", String.valueOf(meeting.getEmceeId()));
            map1.put("name", meeting.getEmceeName());
            map1.put("type", V3xOrgEntity.ORGENT_TYPE_MEMBER);
            list.add(map1);
            map.put("host_fill", list);
        }
        if (Strings.isNotBlank(meeting.getRecorderName())) {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            Map<String, String> map1 = new HashMap<String, String>();
            map1.put("id", String.valueOf(meeting.getRecorderId()));
            map1.put("name", meeting.getRecorderName());
            map1.put("type", V3xOrgEntity.ORGENT_TYPE_MEMBER);
            list.add(map1);
            map.put("recoder_fill", list);
        }

        //附件
        List<Attachment> attachments = attachmentManager.getByReference(meetingId, meetingId);
        map.put("attachments", attachments);

        //视频会议室
        String videoMeetingId = meeting.getVideoMeetingId();
        if (Strings.isNotBlank(videoMeetingId)) {
            try {
                Map<String, String> ext4Map = new HashMap<String, String>();
                ext4Map.putAll(JSONUtil.parseJSONString(meeting.getVideoMeetingId(), Map.class));
                String videoRoomId = (String) ext4Map.get("videoRoomId");
                if (Strings.isNotBlank(videoRoomId)) {
                    map.put("videoRoomId", videoRoomId);
                    map.put("videoRoomAppId", (String) ext4Map.get("videoRoomAppId"));
                    String videoRoomName = (String) ext4Map.get("videoRoomName");
                    map.put("videoRoomName", videoRoomName);
                    String beginDate = (String) ext4Map.get("videoRoomAppBeginDate");
                    String endDate = (String) ext4Map.get("videoRoomAppEndDate");
                    if (Strings.isNotBlank(beginDate) && Strings.isNotBlank(endDate)) {
                        map.put("videoRoomStartDate", Datetimes.format(DateUtil.parse(beginDate, DateFormatEnum.yyyyMMddHHmm2.key()), Datetimes.datetimeWithoutSecondStyle));
                        map.put("videoRoomEndDate", Datetimes.format(DateUtil.parse(endDate, DateFormatEnum.yyyyMMddHHmm2.key()), Datetimes.datetimeWithoutSecondStyle));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("转换videoMeetingId视频会议参数JSON格式异常", e);
            }
        }

        if (MeetingUtil.hasMeetingVideoPlugin()) {
            try {
                MeetingVideoManager meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
                if (meetingVideoManager != null && meetingVideoManager.isMeetingVideoEnable() && meetingVideoManager.canSendMeeting(AppContext.currentUserId())) {
                    if (meetingVideoManager.canChooseVideoMeetingRoom()) {
                        map.put("isShowVideoRoom", true);
                    } else {
                        map.put("isShowMeetingNature", meetingVideoManager.isSupportCreateMobileMeeting());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        /**
         * 会议是否可以手动输入会议地址
         */
        boolean isMeetingPlaceInputAble = meetingSettingManager.isMeetingPlaceInputAble();
        map.put("isMeetingPlaceInputAble", isMeetingPlaceInputAble);

        //外部会议选项
        boolean enablePublicMeeting = orgManager.accessedByVisitor(ApplicationCategoryEnum.meeting.name(), AppContext.currentAccountId());
        map.put("enablePublicMeeting", enablePublicMeeting);

        //会议室权限
        map.put("haveMeetingRoomApp", menuPurviewUtil.isHaveMeetingRoomApp(AppContext.getCurrentUser()));

        return ok(map);
    }

    //解析人员，返回人员组建所需数据
    private List<Map<String, String>> analyze(String input) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (Strings.isEmpty(input)) {
            return list;
        }

        Map<String, String> map;

        String[] allData = input.split(",");
        String authType = "";
        Long authId;
        for (String auth : allData) {
            map = new HashMap<String, String>();
            authType = auth.split("\\|")[0];
            authId = Long.valueOf(auth.split("\\|")[1]);
            map.put("id", auth.split("\\|")[1]);
            map.put("type", authType);
            if (V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(authType)) {
                map.put("name", Functions.showOrgAccountName(authId));
            } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(authType)) {
                map.put("name", Functions.showDepartmentName(authId));
            } else if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(authType)) {
                map.put("name", Functions.showMemberName(authId));
            } else if (V3xOrgEntity.ORGENT_TYPE_TEAM.equals(authType)) {
                map.put("name", Functions.getTeamName(authId));
            } else if (V3xOrgEntity.ORGENT_TYPE_POST.equals(authType)) {
                map.put("name", Functions.showOrgPostName(authId));
            } else if (V3xOrgEntity.ORGENT_TYPE_LEVEL.equals(authType)) {
                map.put("name", Functions.showOrgLeaveName(Functions.getLeave(authId)));
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 会议删除
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   id        Y     会议ID
     *                      String   listType      Y     将要删除会议的来源
     *                         <pre>
     *                         	listWaitSendMeeting   待发
     *                         	listDoneMeeting       已开
     *                         	listSendMeeting       已发
     *                         </pre>
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Path("removeMeeting")
    public Response removeMeeting(Map<String, Object> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();

        String listType = ParamUtil.getString(params, "listType");
        int type = MeetingListTypeEnum.getTypeName(listType);

        String idStr = ParamUtil.getString(params, "id");
        if (Strings.isNotBlank(idStr)) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            List<Long> idList = MeetingUtil.getIdList(idStr);
            parameterMap.put("idList", idList);
            parameterMap.put("type", type);
            parameterMap.put("currentUser", AppContext.getCurrentUser());

            meetingManager.transDeleteMeeting(parameterMap);
            r_map.put(SUCCESS_KEY, ResourceUtil.getString("meeting.deal.delete.success"));
        }
        return ok(r_map);
    }


    /**
     * 获取视频会议插件参数
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   meetingId        Y     会议ID
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Path("videoMeetingParams")
    public Response videoMeetingParams(Map<String, Object> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();

        Long meetingId = ParamUtil.getLong(params, "meetingId");

        if (MeetingUtil.isIdNull(meetingId)) {
            return ok(errorParams());
        }

        if (MeetingUtil.hasMeetingVideoPlugin()) {
            MtMeeting meeting = meetingManager.getMeetingById(meetingId);
            List<Long> list = OrgHelper.getMemberIdsByTypeAndId(meeting.getImpart(), orgManager);
            long userId = AppContext.currentUserId();

            if (MeetingNatureEnum.video.key().equals(meeting.getMeetingType()) && !list.contains(userId)) {
                try {
                    MeetingVideoManager meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
                    if (meetingVideoManager != null) {
                        String v_methodName = meetingVideoManager.getJoinButtonClkFunNameM3();

                        Map<String, Object> v_params = new HashMap<String, Object>();

                        String ext4 = meeting.getVideoMeetingId();
                        Map<String, Object> ext4Map = JSONUtil.parseJSONString(ext4, Map.class);
                        v_params.put("userName", AppContext.currentUserName());
                        v_params.put("userId", userId);
                        if (meeting.getEmceeId().equals(userId)) {
                            v_params.put("pCode", ext4Map.get("pcode1"));
                            v_params.put("v_isEmcee", true);
                        } else {
                            v_params.put("pCode", ext4Map.get("pcode2"));
                            v_params.put("v_isEmcee", false);
                        }
                        v_params.put("conferenceId", ext4Map.get("conferenceId"));

                        r_map.put("v_methodName", v_methodName);
                        r_map.put("v_params", meetingVideoManager.getJoinButtonClkFunParmasM3(v_params));
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
        return ok(r_map);
    }

    /**
     * 获取视频会议插件参数
     *
     * @param params <pre>
     *                      类型    名称             必填     备注
     *                      String   meetingId        Y     会议ID
     *                </pre>
     * @return Map<String, Object>
     * @throws BusinessException
     */
    @POST
    @Path("getVideoMeetingZPK")
    public Response getVideoMeetingZPK(Map<String, Object> params) throws BusinessException {
        Map<String, Object> r_map = new HashMap<String, Object>();

        String videoRoomId = ParamUtil.getString(params, "videoRoomId");
        Long meetingId = ParamUtil.getLong(params, "meetingId");


        if (Strings.isBlank(videoRoomId) && MeetingUtil.isIdNull(meetingId)) {
            return ok(errorParams());
        }

        if (MeetingUtil.hasMeetingVideoPlugin()) {
            MtMeeting meeting = meetingManager.getMeetingById(meetingId);
            List<Long> list = OrgHelper.getMemberIdsByTypeAndId(meeting.getImpart(), orgManager);
            long userId = AppContext.currentUserId();

            if (MeetingNatureEnum.video.key().equals(meeting.getMeetingType()) && !list.contains(userId)) {
                try {
                    String[] videoZpk = meetingApplicationHandler.getVideoMeetingZPK(videoRoomId);
                    r_map.put("videoZpk", videoZpk[0]);
                } catch (Exception e) {
                    r_map.put("errorMsg", e.getMessage());
                    LOGGER.error("", e);
                }
            }
        }
        return ok(r_map);
    }

    /**
     * 清理缓存数据
     *
     * @Since A8-V5 6.1
     * @Author : xuqw
     * @Date : 2017年4月5日下午4:05:17
     */
    private void removeThreadContext() {
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONSTR_KEY);
        AppContext.removeThreadContext(GlobalNames.THREAD_CONTEXT_JSONOBJ_KEY);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("detail/sendMessage")
    public Response sendRemindersMeetingReceiptMessage(@QueryParam("meetingId") String meetingId,
                                                       @QueryParam("senderId") String senderId, @QueryParam("receiverIds") String receiverIds)
            throws BusinessException {
        Map<String, Object> messageMap = new HashMap<String, Object>();
        messageMap.put("senderId", Long.parseLong(senderId));
        String[] receiverIdsArr = receiverIds.substring(0, receiverIds.length() - 1).split(",");
        List<Long> receiverIdsList = new ArrayList<Long>();
        for (String receiverId : receiverIdsArr) {
            receiverIdsList.add(Long.parseLong(receiverId));
        }
        messageMap.put("receiverIds", receiverIdsList);
        messageMap.put("meetingId", Long.parseLong(meetingId));
        messageMap.put("mtmeeting", mtMeetingManager.getMtMeetingById(Long.parseLong(meetingId), null));
        messageMap.put("sendTerminal", "M3");
        messageMap.put("user", AppContext.getCurrentUser());

        Map<String, String> retMap = new HashMap<String, String>();
        try {
            meetingManager.sendMeetingReceiptMessage(messageMap);
            retMap.put("success", ResourceUtil.getString("meeting.deal.urge.success"));
        } catch (BusinessException e) {
            LOGGER.error("", e);
            retMap.put("failer", ResourceUtil.getString("meeting.deal.urge.failed"));
        }
        return ok(retMap);
    }

    /**
     * 获取会议分类列表
     *
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("summary/getTypeList")
    @RestInterfaceAnnotation
    public Response getMeetingSummaryTypeList() throws BusinessException {
        List<MeetingType> meetingTypeList;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            Long accountId = AppContext.getCurrentUser().getAccountId();
            meetingTypeList = meetingTypeManager.getMeetingTypeList(accountId);
            for (MeetingType meetingType : meetingTypeList) {
                meetingType.setShowName(ResourceUtil.getString(meetingType.getName()));
            }
            retMap.put("success", meetingTypeList);
        } catch (BusinessException e) {
            LOGGER.error("获取会议纪要分类出错：", e);
            retMap.put("errorMsg", e.getMessage());
        }
        return ok(retMap);
    }

    /**
     * 会议签到条件
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("meetingSignCondition")
    @RestInterfaceAnnotation
    public Response meetingSignCondition(Map<String, Object> param) throws BusinessException {
        Long meetingId = ParamUtil.getLong(param, "meetingId", -1L);
        Map<String, Object> condition = meetingBarCodeManager.meetingQrCodeSignCondition(meetingId);
        return success(condition);
    }

    /**
     * 会议签到
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("meetingSign")
    @RestInterfaceAnnotation
    public Response meetingSign(Map<String, Object> param) throws BusinessException {
        meetingBarCodeManager.meetingQrCodeSign(param);
        return success(null);
    }

    /**
     * 会议室占用情况页面
     *
     * @param params
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("meetingRoomState")
    @RestInterfaceAnnotation
    public Response meetingRoomState(Map<String, Object> params) throws BusinessException, ParseException {
        Long roomId = ParamUtil.getLong(params, "meetingRoomId", -1L);
        String startDate = ParamUtil.getString(params, "startDate");
        String endDate = ParamUtil.getString(params, "endDate");
        MeetingRoomListVO roomListVO = meetingRoomM3Manager.getMeetingRoomListVo(roomId, DateUtil.parse(startDate), DateUtil.parse(endDate));
        return success(roomListVO);
    }

    /**
     * 扫码申请会议室的基本情况
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("meetingRoomApplyCondition")
    @RestInterfaceAnnotation
    public Response meetingRoomApplyCondition(Map<String, Object> param) throws BusinessException {
        Long meetingRoomId = ParamUtil.getLong(param, "meetingRoomId", -1L);
        Map<String, Object> condition = meetingBarCodeManager.meetingRoomApplyCondition(meetingRoomId);
        return success(condition);
    }

    /**
     * 会议邀请界面内容
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("meetingInviteDetail")
    @RestInterfaceAnnotation
    public Response meetingInviteDetail(Map<String, Object> param) throws BusinessException {
        Long meetingId = ParamUtil.getLong(param, "meetingId", -1L);
        User currentUser = AppContext.getCurrentUser();
        V3xOrgMember user = orgManager.getMemberById(AppContext.currentUserId());
        MeetingBO meetingBO = meetingApi.getMeeting(meetingId);

        Map<String, Object> data = new HashMap<String, Object>(16);
        data.put("meetingName", meetingBO.getTitle());

        /**
         * 会议室审核状态
         */
        boolean isAudited = meetingBO.getRoomState() == MeetingEnum.MeetingRoomStateEnum.MeetingRoomState_App_Yes.key();
        data.put("isAudited", isAudited);

        /**
         * 会议撤销
         */
        boolean isCanceled = meetingBO.getState() == MeetingConstant.MeetingStateEnum.save.key();
        data.put("isCanceled", isCanceled);

        /**
         * 会议结束
         */
        boolean isFinished = MeetingHelper.isFinished(meetingBO.getState());
        data.put("isFinished", isFinished);

        /**
         * 是否开启外部会议
         */
        boolean isPublic = meetingBO.getIsPublic() != null && meetingBO.getIsPublic() == MeetingConstant.MeetingPublicType.isPublic.ordinal();
        boolean isEnableVisitor = orgManager.accessedByVisitor(ApplicationCategoryEnum.meeting.name(), meetingBO.getAccountId());
        data.put("isPublic", isPublic && isEnableVisitor);

        /**
         * 是否回执参加
         */
        List<MtReply> mtReplys = replyManager.findByMeetingIdAndUserId(meetingId, AppContext.currentUserId());
        boolean isAttend = mtReplys != null && !mtReplys.isEmpty() && mtReplys.get(0).getFeedbackFlag() == Constants.FEEDBACKFLAG_ATTEND;
        data.put("isAttend", isAttend);

        /**
         * 会议状态
         */
        String meetingStateHtml = "";
        if (meetingBO.getState() == MeetingConstant.MeetingStateEnum.send.key()) {
            meetingStateHtml = MeetingConstant.MeetingPanelStateEnum.waiting.text();
        } else if (meetingBO.getState() == MeetingConstant.MeetingStateEnum.start.key()) {
            meetingStateHtml = MeetingConstant.MeetingPanelStateEnum.going.text();
        } else if (isFinished) {
            meetingStateHtml = MeetingConstant.MeetingPanelStateEnum.end.text();
        }
        data.put("meetingState", meetingBO.getState());
        data.put("meetingStateHtml", meetingStateHtml);

        /**
         * 会议时间
         */
        MeetingListRestVO restVO = new MeetingListRestVO();
        restVO.setBeginDate(meetingBO.getBeginDate());
        restVO.setEndDate(meetingBO.getEndDate());
        data.put("meetingTime", restVO.getShowTime());

        /**
         * 地点
         */
        data.put("meetingPlace", meetingBO.getMeetPlace());

        /**
         * 附件
         */
        List<Attachment> fileAtts = new ArrayList<Attachment>();
        List<Attachment> allAtts = attachmentManager.getByReference(meetingId, meetingId);
        for (Attachment att : allAtts) {
            if (att.getType() == 0) {
                fileAtts.add(att);
            }
        }
        data.put("fileAttachments", fileAtts);

        /**
         * 正文
         */
        data.put("bodyType", meetingBO.getBodyType());
        if (!"html".equalsIgnoreCase(meetingBO.getBodyType())) {
            V3XFile file = fileManager.getV3XFile(Long.valueOf(meetingBO.getContent()));
            if (file != null) {
                data.put("lastModified", DateUtil.formatDateTime(file.getUpdateDate()));
                data.put("content", meetingBO.getContent());
                //是否允许office转换
                data.put("allowTrans", OfficeTransHelper.allowTrans(file));
            }
        } else {//HTML 正文需要被正文组件重新解析，才能正常显示其中的关联和附件
            String htmlContentString = meetingBO.getContent();
            data.put("content", HtmlMainbodyHandler.replaceInlineAttachment(htmlContentString));
        }

        /**
         * 记录访客浏览日志
         */
        if (user.isVisitor()) {
            V3xOrgVisitor visitor = orgManager.getVisitorById(currentUser.getId());
            String userName = visitor.getName() + "(" + visitor.getMobile() + ")";
            appLogManager.insertLog(currentUser, 2267, userName, meetingBO.getTitle());
        }

        return success(data);
    }

    /**
     * 参加会议
     *
     * @param param
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("attendMeeting")
    @RestInterfaceAnnotation
    public Response attendMeeting(Map<String, Object> param) throws BusinessException {
        Long meetingId = ParamUtil.getLong(param, "meetingId", -1L);
        User currentUser = AppContext.getCurrentUser();
        V3xOrgMember user = orgManager.getMemberById(AppContext.currentUserId());
        List<MtReply> replies = replyManager.findByMeetingIdAndUserId(meetingId, user.getId());
        MtMeeting meeting = meetingManager.getMeetingById(meetingId);

        if (replies == null || replies.isEmpty()) {
            /**
             * 访客
             */
            MtReply reply = new MtReply();
            reply.setIdIfNew();
            reply.setMeetingId(meetingId);
            reply.setUserId(user.getId());
            reply.setFeedbackFlag(Constants.FEEDBACKFLAG_ATTEND);
            reply.setLookState(1);
            reply.setReadDate(new Date());
            reply.setSource(MeetingUserAgent.miniprogram.getKey());
            reply.setUserType(user.isVisitor() ? MeetingUserType.outer.ordinal() : MeetingUserType.external.ordinal());
            reply.setRole(MeetingRoleEnum.external.getKey());
            replyManager.add(reply);

            /**
             * 更新参会人数
             */
            meeting.setAllCount(meeting.getAllCount() + 1);
            meeting.setJoinCount(meeting.getJoinCount() + 1);
            meetingManager.saveOrUpdate(meeting);

            /**
             * 会议外系统内人员触发日程事件
             */
            if (!user.isVisitor() && AppContext.hasPlugin("timeView")) {
                List<Long> joinAndPendingMember = meetingManager.getJoinMeetingReplyMapExtImpart(meeting.getId(), meeting);
                MeetingBO mVo = DataTransUtil.trans2MeetingBO(meeting);
                mVo.setJoinAndPendingMember(joinAndPendingMember);
                MeetingReplyEvent event = new MeetingReplyEvent(this, mVo, user.getId(), new Date(), reply.getFeedbackFlag().intValue());
                EventDispatcher.fireEventAfterCommit(event);
            }
        } else {
            /**
             * 会议内人员
             */
            Map<String, Object> conditions = new HashMap<String, Object>(16);
            conditions.put("app", ApplicationCategoryEnum.meeting.key());
            conditions.put("objectId", meetingId);
            conditions.put("memberId", user.getId());
            List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
            boolean isImpart = !affairList.isEmpty() && ("inform").equals(affairList.get(0).getNodePolicy());

            MtReply reply = replies.get(0);
            reply.setFeedbackFlag(isImpart ? Constants.FEEDBACKFLAG_IMPART : Constants.FEEDBACKFLAG_ATTEND);
            reply.setLookState(1);
            reply.setReadDate(new Date());
            reply.setSource(MeetingUserAgent.miniprogram.getKey());
            replyManager.update(reply);
            replyManager.updateAffair4Reply(reply);

            /**
             * 更新参会人数
             */
            if (!isImpart) {
                meeting.setJoinCount(meeting.getJoinCount() + 1);
                meetingManager.saveOrUpdate(meeting);
            }

            /**
             * 发消息
             */
            List<Long> listId = new ArrayList<Long>();
            listId.add(meeting.getCreateUser());
            Collection<MessageReceiver> receivers = MessageReceiver.getReceivers(meetingId, listId, "message.link.mt.reply", meeting.getId().toString(), reply.getId().toString());
            for (MessageReceiver rec : receivers) {
                rec.setReply(true);
            }
            String feedback = UserMessageUtil.getComment4Message(reply.getFeedback());
            int contentType = Strings.isBlank(feedback) ? -1 : 1;
            int proxyType = 0;
            userMessageManager.sendSystemMessage(MessageContent.get("meeting.message.reply", meeting.getTitle(), user.getName(), reply.getFeedbackFlag(), contentType, feedback, proxyType, user.getName()), ApplicationCategoryEnum.meeting, user.getId(), receivers, MeetingMessageTypeEnum.Meeting_Reply.key());

            /**
             * 触发日程事件
             */
            if (AppContext.hasPlugin("timeView")) {
                List<Long> joinAndPendingMember = meetingManager.getJoinMeetingReplyMapExtImpart(meeting.getId(), meeting);
                MeetingBO mVo = DataTransUtil.trans2MeetingBO(meeting);
                mVo.setJoinAndPendingMember(joinAndPendingMember);
                MeetingReplyEvent event = new MeetingReplyEvent(this, mVo, user.getId(), new Date(), reply.getFeedbackFlag().intValue());
                EventDispatcher.fireEventAfterCommit(event);
            }
        }
        /**
         * 记录访客回执日志
         */
        if (user.isVisitor()) {
            V3xOrgVisitor visitor = orgManager.getVisitorById(currentUser.getId());
            String userName = visitor.getName() + "(" + visitor.getMobile() + ")";
            appLogManager.insertLog(currentUser, 2265, userName, meeting.getTitle());
        }
        return success(null);
    }

    /**
     * 会议邀请卡片
     *
     * @param meetingId
     * @return
     * @throws BusinessException
     */
    @GET
    @Path("meetingInviteCard")
    @RestInterfaceAnnotation
    public Response meetingInviteCard(@QueryParam("meetingId") String meetingId) throws BusinessException {
        Map<String, Object> map = new HashMap<String, Object>(16);
        MeetingBO meetingBO = meetingApi.getMeeting(Long.parseLong(meetingId));

        map.put("meeting", meetingBO);
        //邀请二维码文件id
        map.put("qrCodeInvite", meetingBO.getQrCodeInvite());
        if (!fileSecurityManager.isNeedlessLogin(meetingBO.getQrCodeInvite())) {
            fileSecurityManager.addNeedlessLogin(meetingBO.getQrCodeInvite());
        }
        String meetingTimeDisplay = Datetimes.format(meetingBO.getBeginDate(), "yyyy/MM/dd HH:mm") + " - " + Datetimes.format(meetingBO.getEndDate(), "yyyy/MM/dd HH:mm");
        map.put("meetingTimeDisplay", meetingTimeDisplay);
        /**
         * 获取会议室展示屏设置那里的logo
         */
        MeetingScreenSet set = meetingSettingManager.getMeetingScreenSet(meetingBO.getAccountId());
        List<Attachment> logoAttachments = attachmentManager.getByReference(set != null ? set.getId() : -1L, set != null ? set.getId() : -1L);
        if (logoAttachments.size() > 0) {
            if (!fileSecurityManager.isNeedlessLogin(logoAttachments.get(0).getFileUrl())) {
                fileSecurityManager.addNeedlessLogin(logoAttachments.get(0).getFileUrl());
            }
            map.put("qrCodeInviteLogo", logoAttachments.get(0));
        } else {
            V3xOrgAccount account = orgManager.getAccountById(meetingBO.getAccountId());
            map.put("accountName", account.getShortName() != null ? account.getShortName() : account.getName());
        }
        return success(map);
    }


    /**
     * 截图
     *
     * @param params
     * @return
     * @throws BusinessException
     */
    @POST
    @Path("screenSlot")
    public Response screenSlot(Map<String, Object> params) throws BusinessException {
        V3XFile file = null;
        String meetingId = (String) params.get("meetingId");
        String oper = (String) params.get("oper");
        if (oper.startsWith("only")) {//会议正文或会议纪要
            String url = "/mtMeeting.do?method=detail&id=" + meetingId + "&oper=" + oper + "&proxyId=0&isQuote=&baseObjectId=&baseApp=&fromPigeonhole=false&isImpart=false";
            file = screenShotManager.htmlToPdf(url, null, ApplicationCategoryEnum.meeting);
        } else if ("showBarCode".equals(oper)) {//会议二维码
            ScreenShotParam paramObj = new ScreenShotParam();
            paramObj.setHeight(754);
            paramObj.setWidth(475);
            String url = "/mtMeeting.do?method=showBarCodePrint&id=" + meetingId + "&viewModel=screenSlot";
            file = screenShotManager.htmlToImage(url, paramObj, ApplicationCategoryEnum.meeting);
        }
        return success(file);
    }
}
