package com.seeyon.apps.meetingroom.controller;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.apps.meetingroom.manager.*;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.services.ServiceResponse;
import com.seeyon.ctp.services.UserToken;
import com.seeyon.ctp.util.*;
import com.seeyon.util.ReadConfigTools;
import com.seeyon.v3x.services.AuthorityService;
import com.seeyon.v3x.services.impl.AuthorityServiceImpl;
import com.seeyon.v3x.services.message.MessageService;
import com.seeyon.v3x.services.message.impl.MessageServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.common.manager.MeetingLockManager;
import com.seeyon.apps.meeting.api.MeetingVideoManager;
import com.seeyon.apps.meeting.constants.MeetingConstant.DateFormatEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingActionEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomAppStateEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomAttEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomNeedAppEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomSortByEnum;
import com.seeyon.apps.meeting.constants.MeetingPathConstant;
import com.seeyon.apps.meeting.manager.MeetingApplicationHandler;
import com.seeyon.apps.meeting.manager.MeetingManager;
import com.seeyon.apps.meeting.manager.MeetingResourcesManager;
import com.seeyon.apps.meeting.manager.MeetingSettingManager;
import com.seeyon.apps.meeting.manager.MeetingValidationManager;
import com.seeyon.apps.meeting.po.MeetingScreenSet;
import com.seeyon.apps.meeting.po.MeetingTemplate;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.apps.meetingroom.po.MeetingRoomApp;
import com.seeyon.apps.meetingroom.po.MeetingRoomPerm;
import com.seeyon.apps.meetingroom.util.MeetingRoomAdminUtil;
import com.seeyon.apps.meetingroom.util.MeetingRoomHelper;
import com.seeyon.apps.meetingroom.util.MeetingRoomRoleUtil;
import com.seeyon.apps.meetingroom.util.MeetingRoomUtil;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentEditHelper;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.organization.OrgConstants.MemberPostType;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.meeting.domain.MtMeeting;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;


/**
 * @author 唐桂林
 */
public class MeetingRoomController extends BaseController {

    private static final Log LOGGER = LogFactory.getLog(MeetingRoomController.class);

    private MeetingRoomManager meetingRoomManager;
    private MeetingValidationManager meetingValidationManager;
    private MeetingManager meetingManager;
    private OrgManager orgManager;
    private AttachmentManager attachmentManager;
    private AffairManager affairManager;
    private MobileMessageManager mobileMessageManager;
    private MeetingResourcesManager meetingResourcesManager;
    private MeetingLockManager meetingLockManager;
    private MeetingApplicationHandler meetingApplicationHandler;
    private MeetingSettingManager meetingSettingManager;
    private FileManager fileManager;
    private ConfigManager configManager;


    private final Lock LOCK = new ReentrantLock();

    /**
     * 进入创建会议室
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView createAdd(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean isAdmin = MeetingRoomRoleUtil.isMeetingRoomAdminRole();
        boolean isAccountAdmin = MeetingRoomRoleUtil.isAdministrator();
        if (!isAdmin && !isAccountAdmin) {
            return refreshWorkspace();
        }

        Long roomId = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        String flag = request.getParameter("flag");

        User currentUser = AppContext.getCurrentUser();

        MeetingRoomVO roomVo = new MeetingRoomVO();

        if ("register".equals(flag)) {
            // 单位管理员创建会议室不显示自己，因为单位管理员不是会议室管理员
            if (!isAccountAdmin) {
                roomVo.setAdminIds(String.valueOf(currentUser.getId()));
                roomVo.setAdminNames(currentUser.getName());
                roomVo.setAdminMembers("Member|" + currentUser.getId());
            }
        } else {
            MeetingRoom room = this.meetingRoomManager.getRoomById(roomId);
            if (room == null) {
                return refreshWorkspace();
            }

            //过滤当前会议室管理员
            roomVo = MeetingRoomHelper.convertToVO(roomVo, room);

            //获取当前会议室所属管理员
            String[] admins = MeetingRoomAdminUtil.getRoomAdmins(room);
            roomVo.setAdminIds(admins[0]);
            roomVo.setAdminNames(admins[1]);
            //设置已有的会议室管理员
            roomVo.setAdminMembers(admins[2]);

            //获取当前会议室管理范围
            roomVo.setMngdepId(room.getMngdepId());
            roomVo.setMngdepName(MeetingRoomUtil.getMeetingRoomMngdepNames(room.getMngdepId()));

            //获取当前会议室制度附件
            List<Attachment> attatchmentsC = this.attachmentManager.getByReference(room.getId(), RoomAttEnum.attachment.key());
            if (attatchmentsC.size() > 0) {
                roomVo.setAttatchments(attatchmentsC);
                roomVo.setAttObj(attatchmentsC.get(0));
            }

            //获取当前会议室图片附件
            List<Attachment> attatchmentsI = this.attachmentManager.getByReference(room.getId(), RoomAttEnum.image.key());
            if (attatchmentsI.size() > 0) {
                roomVo.setAttatchImage(attatchmentsI);
                String imageIds = "";
                for (Attachment attachmentImg : attatchmentsI) {
                    imageIds += attachmentImg.getFileUrl().toString().trim() + ",";
                }
                roomVo.setImageIds(imageIds.substring(0, imageIds.length() - 1));
            }

            //会议室短URL
            if (Strings.isNotBlank(room.getShortUrl())) {
                roomVo.setShortUrl(room.getShortUrl());
            }
        }

        roomVo.setRoomAdminList(MeetingRoomRoleUtil.getMeetingRoomAdminList(AppContext.currentAccountId()));

        ModelAndView mav = new ModelAndView("meetingroom/createadd");
        mav.addObject("bean", roomVo);
        MeetingScreenSet set = meetingSettingManager.getMeetingScreenSet(AppContext.currentAccountId());
        mav.addObject("screenEnable", set == null ? 0 : set.getScreenEnable());
        mav.addObject("qrcodeEnable", set == null ? 0 : set.getQrcodeEnable());

        //会议室短URL
        if (Strings.isNotBlank(roomVo.getShortUrl())) {
            //本地服务器地址
            String localUrl = Strings.getBaseContext(request) + "/g/" + roomVo.getShortUrl();
            mav.addObject("localUrl", localUrl);
            mav.addObject("intranetTips", ResourceUtil.getString("mr.label.shorturl.realtips", request.getServerName() + ":" + request.getServerPort()));

            //若微协同配置了外网地址
			/*ConfigItem configItem = configManager.getConfigItem("wechat_switch", "oa_url");
			if (configItem != null && Strings.isNotBlank(configItem.getConfigValue())) {
				mav.addObject("remoteUrl", configItem.getConfigValue() + "/g/" + roomVo.getShortUrl());
			}else if(Strings.isNotBlank(SystemEnvironment.getInternetSiteURL())){
				mav.addObject("remoteUrl", SystemEnvironment.getInternetSiteURL() + "/g/" + roomVo.getShortUrl());
			}*/
        }

        return mav;
    }

    /**
     * 执行新建会议室操作
     *
     * @param request
     * @param response
     * @return null, 刷新add.jsp页面
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.MeetingRoomAdmin, Role_NAME.AccountAdministrator})
    public ModelAndView execAdd(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String msgType = "success";

        User currentUser = AppContext.getCurrentUser();
        Long roomId = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        String roomName = request.getParameter("name");
        Integer needApp = Strings.isBlank(request.getParameter("needApp")) ? RoomNeedAppEnum.no.key() : Integer.parseInt(request.getParameter("needApp"));
        Integer needMsg = Strings.isBlank(request.getParameter("needMsg")) ? RoomNeedAppEnum.no.key() : Integer.parseInt(request.getParameter("needMsg"));

        /**
         * 从页面得到图片的Id串
         */
        String images = request.getParameter("image");

        String _content = request.getParameter("filenameContent");
        boolean hasMeetingRoomApp = Strings.isBlank(request.getParameter("hasMeetingRoomApp")) ? false : Boolean.parseBoolean(request.getParameter("hasMeetingRoomApp"));

        if (!MeetingUtil.isIdNull(roomId)) {

            boolean isAdmin = MeetingRoomRoleUtil.isMeetingRoomAdminRole();
            //若会议室有有效会议室管理员，并且不是本人，且不是单位管理员，则不能修改
            List<Long> roleAdminList = MeetingRoomAdminUtil.getRoomAdminIdList(roomId);
            boolean isAccountAdmin = MeetingRoomRoleUtil.isAdministrator();
            if (Strings.isNotEmpty(roleAdminList) && !roleAdminList.contains(currentUser.getId()) && !isAccountAdmin) {
                isAdmin = false;
            }
            if (!isAdmin && !isAccountAdmin) {
                msgType = "notAdmin";
                buffer.append("parent._submitCallback('" + msgType + "', '" + ResourceUtil.getString("mr.alert.notAdmin") + "');");
                rendJavaScript(response, buffer.toString());
                return null;
            }
        }

        MeetingRoom repeatRoom = this.meetingValidationManager.checkRoomNameRepeat(roomId, roomName);
        if (repeatRoom != null) {
            msgType = "isRepeat";
            String accountName = ResourceUtil.getString("mr.alert.currentAccount");
            if (!repeatRoom.getAccountId().equals(AppContext.currentAccountId())) {
                accountName = orgManager.getUnitById(repeatRoom.getAccountId()).getName();
            }
            buffer.append("parent._submitCallback('" + msgType + "', '" + ResourceUtil.getString("mr.alert.namesame", accountName) + "');");
            rendJavaScript(response, buffer.toString());
            return null;
        }

        MeetingRoomVO roomVo = new MeetingRoomVO();
        roomVo.setId(roomId);
        roomVo.setRoomId(roomId);
        roomVo.setName(roomName);
        roomVo.setContent(request.getParameter("content"));
        roomVo.setDescription(request.getParameter("description"));
        roomVo.setEqdescription(request.getParameter("eqdescription"));
        roomVo.setPlace(request.getParameter("place"));
        roomVo.setSeatCount(Integer.parseInt(request.getParameter("seatCount")));
        roomVo.setStatus(Integer.parseInt(request.getParameter("status")));
        roomVo.setAdmin(request.getParameter("adminIds"));
        roomVo.setMngdepId(request.getParameter("mngdepId"));
        roomVo.setSystemNowDatetime(DateUtil.currentDate());
        roomVo.setCurrentUser(currentUser);
        roomVo.setHasMeetingRoomApp(hasMeetingRoomApp);
        // 增加不用申请也发消息的处理(增加类型2为不用申请也需要发消息给管理员)
        if (RoomNeedAppEnum.no_but_need_msg.key() == needMsg) {
            roomVo.setNeedApp(needMsg);
        } else {
            roomVo.setNeedApp(needApp);
        }


        if (roomVo.isNew()) {
            roomVo.setIdIfNew();
        }

        try {
            // 编辑时，先删除会议图片和会议制度
            this.attachmentManager.deleteByReference(roomVo.getId(), RoomAttEnum.attachment.key());
            this.attachmentManager.deleteByReference(roomVo.getId(), RoomAttEnum.image.key());

            StringBuilder sbImageIds = new StringBuilder();
            List<Attachment> attList = new ArrayList<Attachment>();
            AttachmentEditHelper editHelper = new AttachmentEditHelper(request);
            if (!Strings.isBlank(images)) {
                String[] imageIds = images.split(",");
                List<Attachment> attachments = attachmentManager.getAttachmentsFromRequest(
                        ApplicationCategoryEnum.meetingroom, roomVo.getId(), RoomAttEnum.image.key(), request);
                for (String imageId : imageIds) {
                    if (Strings.isNotBlank(imageId)) {
                        editHelper.setSubReference(Long.parseLong(imageId));
                        // 记录操作日志
                        if (editHelper.hasEditAtt()) {
                            attachmentManager.deleteByReference(editHelper.getReference(),
                                    editHelper.getSubReference());
                        }
                        if (attachments != null && attachments.size() > 0) {
                            for (int i = 0; i < attachments.size(); i++) {
                                if (attachments.get(i).getFileUrl() == Long.parseLong(imageId)) {
                                    sbImageIds.append(imageId).append(",");
                                    Attachment attachment = attachments.get(i);
                                    attachment.setSubReference(RoomAttEnum.image.key());
                                    attList.add(attachment);
                                }
                            }
                        }
                    }
                }
            }

            if (!Strings.isBlank(sbImageIds.toString())) {
                String[] imageIds = sbImageIds.toString().split(",");
                for (int i = 0; i < imageIds.length; i++) {
                    if (Strings.isNotBlank(imageIds[i])) {
                        roomVo.setImage(imageIds[i]);
                        break;
                    }
                }
            } else {
                roomVo.setImage("");
            }

            if (!Strings.isBlank(_content)) {
                editHelper.setSubReference(Long.parseLong(_content));
                //记录操作日志
                if (editHelper.hasEditAtt()) {
                    attachmentManager.deleteByReference(editHelper.getReference(), editHelper.getSubReference());
                }
                List<Attachment> attachments = attachmentManager.getAttachmentsFromRequest(ApplicationCategoryEnum.meetingroom, roomVo.getId(), RoomAttEnum.attachment.key(), request);
                if (attachments != null && attachments.size() > 0) {
                    for (int i = 0; i < attachments.size(); i++) {
                        if (attachments.get(i).getFileUrl() == Long.parseLong(_content)) {
                            Attachment attachment = attachments.get(i);
                            attachment.setSubReference(RoomAttEnum.attachment.key());
                            attList.add(attachment);
                        }
                    }
                }
            }

            if (Strings.isNotEmpty(attList)) {
                attachmentManager.create(attList);
            }

            boolean ok = this.meetingRoomManager.transAdd(roomVo);
            if (!ok) {
                msgType = "failure";
            }
        } catch (Exception e) {
            LOGGER.error("登记会议室出错", e);
            msgType = "failure";
        }
        buffer.append("parent._submitCallback('" + msgType + "');");
        rendJavaScript(response, buffer.toString());

        return null;
    }


    /**
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.MeetingRoomAdmin, Role_NAME.AccountAdministrator})
    public ModelAndView execDelRoom(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] ids = request.getParameterValues("id");

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("idList", MeetingUtil.getIdList(ids));

        try {
            this.meetingRoomManager.transDelRoom(parameterMap);
        } catch (Exception e) {
            LOGGER.error("删除会议类型出错", e);
            parameterMap.put("msgType", "failure");
            parameterMap.put("msg", e.getMessage());
        }
        boolean isAccountAdmin = MeetingRoomRoleUtil.isAdministrator();
        StringBuilder buffer = new StringBuilder();
        if (isAccountAdmin) {
            if ("success".equals(parameterMap.get("msgType").toString())) {
                buffer.append("parent.location=parent.location.href;");
            } else {
                buffer.append("alert('" + parameterMap.get("msg") + "'); parent.location=parent.location.href;");
            }
            //buffer.append("parent._submitCallbackAccount('"+parameterMap.get("msgType")+"', '"+parameterMap.get("msg")+"')");
        } else {
            buffer.append("parent._submitCallback('" + parameterMap.get("msgType") + "', '" + parameterMap.get("msg") + "')");
        }
        rendJavaScript(response, buffer.toString());

        return null;
    }


    /**
     * 申请会议室页面，弹出页面
     *
     * @param request
     * @param response
     * @return 弹出createapp.jsp页面
     * @throws Exception
     */
    public ModelAndView createApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/createapp");

        User currentUser = AppContext.getCurrentUser();

        V3xOrgMember v3xOrgMember = this.orgManager.getMemberById(currentUser.getId());
        Long deptId = null;
        if (currentUser.getLoginAccount().longValue() != currentUser.getAccountId().longValue()) {
            List<MemberPost> postList = this.orgManager.getMemberPosts(currentUser.getLoginAccount().longValue(), currentUser.getId());
            if (Strings.isNotEmpty(postList)) {
                for (MemberPost post : postList) {
                    if (post.getOrgAccountId().longValue() == currentUser.getLoginAccount().longValue() && MemberPostType.Concurrent.name().equals(post.getType().name())) {
                        deptId = post.getDepId();
                        break;
                    }
                }
            }
        } else {
            deptId = v3xOrgMember.getOrgDepartmentId();
        }

        Long roomId = MeetingUtil.getLong(request, "id", Constants.GLOBAL_NULL_ID);
        MeetingRoom room = this.meetingRoomManager.getRoomById(roomId);
        if (room != null) {
            room = new MeetingRoom();
        }

        //从栏目中申请会议室
        mav.addObject("openFrom", ParamUtil.getString(request.getParameterMap(), "openFrom"));
        mav.addObject("roomParam", ParamUtil.getString(request.getParameterMap(), "roomParam"));

        mav.addObject("action", "create");
        mav.addObject("bean", room);
        mav.addObject("user", v3xOrgMember);
        mav.addObject("v3xOrgDepartment", this.orgManager.getDepartmentById(deptId));
        mav.addObject("meetingRoomAdmin", MeetingRoomRoleUtil.isMeetingRoomAdminRole());
        return mav;
    }

    /**
     * 执行会议室申请操作
     *
     * @param request
     * @param response
     * @return null，关闭弹出窗口
     * @throws Exception
     */
    public ModelAndView execApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("roomId", request.getParameter("roomId"));
        parameterMap.put("perId", request.getParameter("perId"));
        parameterMap.put("departmentId", request.getParameter("departmentId"));
        parameterMap.put("description", request.getParameter("description"));
        parameterMap.put("startDatetime", request.getParameter("startDatetime"));
        parameterMap.put("endDatetime", request.getParameter("endDatetime"));
//		zhou:2020-07-14
        parameterMap.put("sqrdh", request.getParameter("sqrdh"));
        parameterMap.put("sfygwhldcj", request.getParameter("sfygwhldcj"));
        parameterMap.put("hcyq", request.getParameter("hcyq"));
        parameterMap.put("ldid", request.getParameter("ldid"));
        parameterMap.put("ldname", request.getParameter("ldname"));

//		zhou:2020-07-14

        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
        appVo.setAction(MeetingActionEnum.apply.name());
        appVo.setParameterMap(parameterMap);
        appVo.setCurrentUser(AppContext.getCurrentUser());
        appVo.setSystemNowDatetime(DateUtil.currentDate());

        if (LOCK.tryLock(2000L, TimeUnit.MILLISECONDS)) {
            try {
                this.meetingRoomManager.transApp(appVo);
            } catch (Exception e) {
                LOGGER.error("会议室申请失败", e);
                appVo.setMsg(ResourceUtil.getString("meeting.meetingroom.apply.failed"));
            } finally {
                LOCK.unlock();
            }
        }

        StringBuilder buffer = new StringBuilder();
        if (Strings.isBlank(request.getParameter("linkSectionId"))) {
            buffer.append("if(parent._submitCallback) {");
            buffer.append("  parent._submitCallback('" + appVo.getMsg() + "');");
            buffer.append("}");
            rendJavaScript(response, buffer.toString());
            return null;
        } else {
            if (appVo.getMeetingRoomApp() != null) {
                buffer.append(appVo.getMeetingRoomApp().getId() + "|" + appVo.getMsg());
            }
            super.rendText(response, buffer.toString());
            return null;
        }
    }

    /**
     * 执行会议室申请操作
     *
     * @param request
     * @param response
     * @return null，关闭弹出窗口
     * @throws Exception
     */
    public ModelAndView addRoomAppDesc(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long roomAppId = Strings.isBlank(request.getParameter("roomAppId")) ? Constants.GLOBAL_NULL_ID : Long.parseLong(request.getParameter("roomAppId"));

        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
        try {
            appVo.setRoomAppId(roomAppId);
            appVo.setDescription(request.getParameter("description"));
            meetingRoomManager.transAddRoomAppDesc(appVo);
        } catch (Exception e) {
            logger.error("会议室申请添加用途失败", e);
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append("if(parent._submitCallback) {");
        buffer.append("  parent._submitCallback('" + appVo.getMsg() + "');");
        buffer.append("}");
        rendJavaScript(response, buffer.toString());
        return null;
    }

    private MeetingRoomAppManager appManager=(MeetingRoomAppManager)AppContext.getBean("meetingRoomAppManager");

    public ModelAndView execCancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String msgType = "success";

        String[] ids = request.getParameterValues("id");
        if (ids != null && ids.length > 0) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("roomAppIdList", MeetingUtil.getIdList(ids));
            parameterMap.put("currentUser", AppContext.getCurrentUser());
            parameterMap.put("cancelContent", request.getParameter("cancelContent"));
            parameterMap.put("action", MeetingActionEnum.cancelRoomApp.name());
            try {
                boolean result = this.meetingRoomManager.transCancelRoomApp(parameterMap);

                if (!result) {
                    msgType = "failure";
                }

            } catch (Exception e) {
                LOGGER.error("撤销会议室申请出错", e);
                msgType = "failure";
            }

            if (Strings.isBlank(request.getParameter("linkSectionId"))) {
                buffer.append("if(parent._submitCallback) {");
                buffer.append("  parent._submitCallback('" + msgType + "');");
                buffer.append("}");
            }
        }

        rendJavaScript(response, buffer.toString());
        return null;
    }

    public ModelAndView execClearPerm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String msgType = "success";

        String[] ids = request.getParameterValues("id");

        if (ids != null && ids.length > 0) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("roomAppIdList", MeetingUtil.getIdList(ids));
            parameterMap.put("currentUser", AppContext.getCurrentUser());
            parameterMap.put("cancelContent", request.getParameter("cancelContent"));
            parameterMap.put("action", MeetingActionEnum.cancelRoomApp.name());
            try {
                boolean result = this.meetingRoomManager.transClearRoomPerm(parameterMap);

                if (!result) {
                    msgType = "failure";
                }
            } catch (Exception e) {
                LOGGER.error("撤销会议室申请出错", e);
                msgType = "failure";
            }

            if (Strings.isBlank(request.getParameter("linkSectionId"))) {
                buffer.append("if(parent._submitCallback) {");
                buffer.append("  parent._submitCallback('" + msgType + "');");
                buffer.append("}");
            }

        }
        rendJavaScript(response, buffer.toString());
        return null;
    }


    public ModelAndView execFinish(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String msgType = "success";

        String[] ids = request.getParameterValues("id");
        if (ids != null && ids.length > 0) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("roomAppId", Long.parseLong(ids[0]));
            parameterMap.put("currentUser", AppContext.getCurrentUser());
            parameterMap.put("endDatetime", DateUtil.currentDate());
            parameterMap.put("action", MeetingActionEnum.finishRoomApp.name());
            parameterMap.put("isContainMeeting", request.getParameter("isContainMeeting"));
            try {
                boolean result = this.meetingRoomManager.transFinishAdvanceRoomApp(parameterMap);

                if (!result) {
                    msgType = "failure";
                }
            } catch (Exception e) {
                LOGGER.error("提前结束会议室申请出错", e);
                msgType = "failure";
            }
        } else {
            msgType = "failure";
        }

        if (Strings.isBlank(request.getParameter("linkSectionId"))) {
            buffer.append("if(parent._submitCallback) {");
            buffer.append("  parent._submitCallback('" + msgType + "');");
            buffer.append("}");
        } else {

        }

        rendJavaScript(response, buffer.toString());
        return null;
    }

    private KfqMeetingRoomManager kfqMeetingRoomManager = new KfqMeetingRoomManagerImpl();

    public KfqMeetingRoomManager getKfqMeetingRoomManager() {
        return kfqMeetingRoomManager;
    }

    /**
     * 会议室审批页面
     *
     * @param request
     * @param response
     * @return 转到createperm.jsp页面或者弹出createpermopen.jsp页面
     * @throws Exception
     */
    public ModelAndView createPerm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long roomAppId = ReqUtil.getLong(request, "id", Constants.GLOBAL_NULL_ID);
        Long affairId = Strings.isBlank(request.getParameter("affairId")) ? -1 : Long.parseLong(request.getParameter("affairId"));
        String proxy = request.getParameter("proxy");

        String openWin = request.getParameter("openWin");
        String view = "meetingroom/createperm";
        if ("1".equals(openWin)) {//1 这里1只是起着标示的作用，没有什么特殊的意义(消息打开连接)
            view = "meetingroom/createpermopen";
        }
        ModelAndView mav = new ModelAndView(view);

        CtpAffair currentAffair = null;

        int confereeCount = 0;//参会人数

        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
        MeetingRoomApp roomApp = null;
        boolean isReadOnly = true;
        try {
            roomApp = this.meetingRoomManager.getRoomAppById(roomAppId);

            if (roomApp == null) {
                StringBuilder buffer = new StringBuilder();
                String msg = ResourceUtil.getString("meeting.room.app.cancel");
                buffer.append("alert('" + msg + "');");
                buffer.append("closeWindow();");
                buffer.append(getCloseWindowFunction());
                super.rendJavaScript(response, buffer.toString());
                return null;
            }

            if (roomApp.getStatus() != null && Integer.valueOf(RoomAppStateEnum.wait.key()).equals(roomApp.getStatus())) {
                isReadOnly = false;
            }
            if ("1".equals(openWin)) {//周期批量处理
                if (roomApp.getPeriodicityId() != null) {
                    List<MeetingRoomApp> periodicityRoomAppList = this.meetingRoomManager.getWaitRoomAppListByPeriodicityId(roomApp.getPeriodicityId());
                    if (Strings.isNotEmpty(periodicityRoomAppList)) {
                        mav.addObject("periodicityRoomAppList", periodicityRoomAppList);
                    }
                    //是否能审核
                    for (MeetingRoomApp app : periodicityRoomAppList) {
                        if (app.getStatus() != null && Integer.valueOf(RoomAppStateEnum.wait.key()).equals(app.getStatus())) {
                            isReadOnly = false;
                            break;
                        }
                    }
                }
            }
            if (roomApp != null) {
                if (roomApp.getMeetingId() != null) {
                    MtMeeting meeting = meetingManager.getMeetingById(roomApp.getMeetingId());
                    if (meeting != null) {
                        appVo.setMeetingName(meeting.getTitle());

                        String conferees = meeting.getConferees();
                        if (Strings.isNotBlank(conferees)) {
                            //通过类型计算人员数量
                            String arrCount[] = conferees.split(",");
                            for (String item : arrCount) {
                                if (Strings.isNotBlank(item)) {
                                    String data[] = item.split("[|]");
                                    List<V3xOrgMember> list = this.orgManager.getMembersByType(data[0], Long.valueOf(data[1]));
                                    if (list != null) {
                                        confereeCount += list.size();
                                    }
                                }
                            }
                        }
                    }
                } else if (roomApp.getTemplateId() != null) {
                    MeetingTemplate template = meetingManager.getTemplateById(roomApp.getTemplateId());
                    if (template != null) {
                        appVo.setMeetingName(template.getTitle());

                        String conferees = template.getConferees();
                        if (Strings.isNotBlank(conferees)) {
                            //通过类型计算人员数量
                            String arrCount[] = conferees.split(",");
                            for (String item : arrCount) {
                                if (Strings.isNotBlank(item)) {
                                    String data[] = item.split("[|]");
                                    List<V3xOrgMember> list = this.orgManager.getMembersByType(data[0], Long.valueOf(data[1]));
                                    if (list != null) {
                                        confereeCount += list.size();
                                    }
                                }
                            }
                        }
                    }
                }

                appVo.setRoomId(roomApp.getRoomId());
                appVo.setRoomAppId(roomApp.getId());
                appVo.setMeetingRoomApp(roomApp);


                MeetingRoomPerm roomPerm = this.meetingRoomManager.getRoomPermByAppId(roomAppId);
                if (roomPerm != null) {
                    appVo.setMeetingRoomPerm(roomPerm);
                    appVo.setRoomPermId(roomPerm.getId());
                }

                List<CtpAffair> list = this.affairManager.getAffairs(ApplicationCategoryEnum.meetingroom, roomApp.getId());
                if (Strings.isNotEmpty(list)) {
                    currentAffair = list.get(0);
                }
                //代理人处理
                if (!MeetingUtil.isIdNull(roomPerm.getProxyId())) {
                    V3xOrgMember member = this.orgManager.getMemberById(roomPerm.getProxyId());
                    if (member != null) {
                        mav.addObject("proxyName", member.getName());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("通过id获取会议室申请对象报错!", e);
        }

        MeetingRoom room = this.meetingRoomManager.getRoomById(appVo.getRoomId());
        appVo.setMeetingRoom(room);

        List<Attachment> attatchmentsC = this.attachmentManager.getByReference(appVo.getRoomId(), RoomAttEnum.attachment.key());
        if (attatchmentsC.size() > 0) {
            mav.addObject("attatchments", attatchmentsC);
            mav.addObject("attObj", attatchmentsC.get(0));
        }
        List<Attachment> attatchmentsI = this.attachmentManager.getByReference(appVo.getRoomId(), RoomAttEnum.image.key());
        if (attatchmentsI.size() > 0) {
            mav.addObject("attatchImage", attatchmentsI);
            //mav.addObject("imageObj",attatchmentsI.get(0));
            List<String> imageIds = new ArrayList<String>();
            for (Attachment attachmentImg : attatchmentsI) {
                imageIds.add(attachmentImg.getFileUrl().toString().trim());
            }
            mav.addObject("imageIds", imageIds);
        }
        if (roomApp != null) {
            V3xOrgDepartment department = this.orgManager.getDepartmentById(roomApp.getDepartmentId());
            if (department != null) {
                mav.addObject("departmentName", department.getName());
            }
        }
        mav.addObject("count", confereeCount);//参会人数
        mav.addObject("proxy", "1".equals(proxy));

        mav.addObject("bean", appVo);
        mav.addObject("affairId", affairId);
        mav.addObject("isReadOnly", isReadOnly);
        // 会议用品
        Long meetingId = null;
        if (roomApp != null) {
            meetingId = roomApp.getMeetingId();
        }
        String resourcesNames = new String();
        if (null != meetingId) {
            resourcesNames = meetingResourcesManager.getResourceNamesByMeetingId(meetingId);
        }
        mav.addObject("resourcesName", resourcesNames);

        // 谁审核的 原逻辑取的是perid,现在的逻辑是在perm表中添加审核人字段，如果审核人为空，取会议室的全部管理员
        if (appVo.getMeetingRoomApp().getAuditingId() != null) {
            mav.addObject("peradmin", appVo.getMeetingRoomApp().getAuditingId());
        } else {
            String adminformat = MeetingRoomAdminUtil.getRoomAdmins(appVo.getMeetingRoom())[2];
            mav.addObject("peradmin", Strings.isNotBlank(adminformat) ? adminformat.replaceAll("Member[|]", "") : adminformat);
        }

        //checkAuditRight = !checkAuditRight ? "1".equals(openWin) : true;//打开窗口和管理员待办内嵌页面都进行权限验证

//      [开发区会议管理]  zhou start
        List<MeetingRoom> rooms = kfqMeetingRoomManager.findAllMeetingRoom();
        mav.addObject("rooms", rooms);
//      [开发区会议管理]  zhou end
        mav.addObject("isPeriodicity", !MeetingUtil.isIdBlank(request.getParameter("periodicityInfoId")));
        return mav;
    }


    /**
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public ModelAndView execPerm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String permStatusStr = request.getParameter("permStatus");
        String description = request.getParameter("description");
        String openWin = request.getParameter("openWin");
        Long periodicityId = Strings.isBlank(request.getParameter("periodicityId")) ? Constants.GLOBAL_NULL_ID : Long.parseLong(request.getParameter("periodicityId"));
        Long roomAppId = Strings.isBlank(request.getParameter("id")) ? -1 : Long.parseLong(request.getParameter("id"));
        Long affairId = Strings.isBlank(request.getParameter("affairId")) ? -1 : Long.parseLong(request.getParameter("affairId"));
        String[] roomAppIds = request.getParameterValues("roomAppId");

        Connection connection = JDBCAgent.getRawConnection();
        //  放在这个位置管理员修改会议室信息，申请人收到的通知就是管理员修改后的会议室信息。            会议管理员在审批的时候可以修改会议室，在此更新会议室申请信息   zhou start
        String updateSql = "update MEETING_ROOM_APP set MEETINGROOMID=? where ID=?";
        String updateRoomPerm = "update MEETING_ROOM_PERM set meeting_room_id=? where appid=?";
        String updateRoomRecord = "update MEETING_ROOM_RECORD set meetingroomid=? where appid=?";
        PreparedStatement psUpdate = null;
        try {
            String roomId = request.getParameter("roomId");
            psUpdate = connection.prepareStatement(updateSql);
            psUpdate.setLong(1, Long.parseLong(roomId));
            psUpdate.setLong(2, roomAppId);
            psUpdate.executeUpdate();

//                  修改会议审批记录中会议室信息
            psUpdate = connection.prepareStatement(updateRoomPerm);
            psUpdate.setLong(1, Long.parseLong(roomId));
            psUpdate.setLong(2, roomAppId);
            psUpdate.executeUpdate();

//                  修改会议室申请记录
            psUpdate = connection.prepareStatement(updateRoomRecord);
            psUpdate.setLong(1, Long.parseLong(roomId));
            psUpdate.setLong(2, roomAppId);
            psUpdate.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != psUpdate) {
                psUpdate.close();
            }
        }
//              会议管理员在审批的时候可以修改会议室，在此更新会议室申请信息   zhou end
        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
        appVo.setRoomAppId(roomAppId);
        appVo.setDescription(request.getParameter("description"));
        appVo.setStatus(Integer.parseInt(request.getParameter("permStatus")));
        appVo.setSystemNowDatetime(DateUtil.currentDate());
        appVo.setCurrentUser(AppContext.getCurrentUser());
        appVo.setAffairId(affairId);
        appVo.setPeriodicityId(periodicityId);
        appVo.setPeriodicityRoomAppIdList(MeetingUtil.getIdList(roomAppIds));

        boolean ok = true;
        if (LOCK.tryLock()) {
            try {
                this.meetingRoomManager.transPerm(appVo);
            } catch (Exception e) {
                LOGGER.error("审核会议室出错", e);
                ok = false;
                rendJavaScript(response, "alert('" + e.getMessage() + "')");
            } finally {
                LOCK.unlock();
            }
        } else {
            ok = false;
            rendJavaScript(response, "alert('" + ResourceUtil.getString("meeting.other.admin.auditing") + "')");
        }
        StringBuilder buffer = new StringBuilder();
        if (ok) {
            if (Strings.isBlank(request.getParameter("linkSectionId"))) {
                buffer.append("if(parent._submitCallback) {");
                buffer.append("  parent._submitCallback('" + appVo.getMsg() + "');");
                buffer.append("}");


                /**
                 * zhou:会议室审核通过，发送消息通知
                 */
                AuthorityService authorityService = new AuthorityServiceImpl();
                String pass = new ReadConfigTools().getString("passwordOfWebservice");
                UserToken userToken = authorityService.authenticate("service-admin", pass);
                String tokenId = userToken.getId();
                MessageService messageService = new MessageServiceImpl();
                User user = AppContext.getCurrentUser();
                String[] urls = {"/seeyon/meetingroom.do?method=createPerm&openWin=1&id=" + request.getParameter("id") + "&affairId="};
                String[] loginNames = new ReadConfigTools().getString("loginNameOfReciver").split(",");

                String sql = "select startdatetime,enddatetime,(select r.name from MEETING_room r where r.id =m.meetingroomid) meetingname,(select name from org_member o where o.id=m.perid ) username,(select name from org_unit u where u.id=m.departmentid) deptname from meeting_room_app m where m.id=?";
                PreparedStatement ps = null;
                ResultSet rs = null;
                StringBuffer sb = new StringBuffer();
                try {
                    ps = connection.prepareStatement(sql);
                    ps.setString(1, request.getParameter("id"));
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        sb.append(rs.getString("username") + "【" + rs.getString("deptname") + "】申请了会议室：" + rs.getString("meetingname") + "，时间为：" + rs.getString("startdatetime") + "至" + rs.getString("enddatetime") + "。请提前安排！");
                    }
                    ServiceResponse serviceResponse = messageService.sendMessageByLoginName(tokenId, loginNames, sb.toString(), urls);
                    serviceResponse.getResult();


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != rs) {
                        rs.close();
                    }
                    if (null != ps) {
                        ps.close();
                    }
                    if (null != connection) {
                        connection.close();
                    }
                }

            } else {
                if (appVo.getMeetingRoomApp() != null) {
                    buffer.append(appVo.getMeetingRoomApp().getId() + "|" + appVo.getMsg());
                }
                super.rendText(response, buffer.toString());
                return null;
            }

            rendJavaScript(response, buffer.toString());
        }

        return null;
    }

    /**
     * 会议室申请
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView mtroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/mtroom");
        int needApp = Strings.isBlank(request.getParameter("needApp")) ? -1 : Integer.parseInt(request.getParameter("needApp"));
        int sortType = Strings.isBlank(request.getParameter("sort")) ? RoomSortByEnum.name.key() : Integer.parseInt(request.getParameter("needApp"));
        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? -1L : Long.parseLong(request.getParameter("meetingId"));
        Long periodicityId = Strings.isBlank(request.getParameter("periodicityId")) ? -1L : Long.parseLong(request.getParameter("periodicityId"));
        String action = request.getParameter("action");//判断是增加还是修改(create新增,edit修改)
        String returnMrStr = request.getParameter("returnMr");//获取返回的记录
        String timepams = Strings.isBlank(request.getParameter("timepams")) ? DateUtil.get19DateAndTime() : request.getParameter("timepams");
        String appType = request.getParameter("appType");
        String meetingBeginDate = request.getParameter("meetingBeginDate");
        String meetingEndDate = request.getParameter("meetingEndDate");

        List<MeetingRoomApp> appedList = new ArrayList<MeetingRoomApp>();

        User currentUser = AppContext.getCurrentUser();

        List<MeetingRoom> list = this.meetingRoomManager.getMyCanAppRoomList(currentUser, sortType, null, null);
        if (Strings.isNotEmpty(list)) {
            List<Long> meetingroomList = new ArrayList<Long>();
            for (MeetingRoom bean : list) {
                meetingroomList.add(bean.getId());
            }
            appedList = this.meetingRoomManager.getUsedRoomAppListByDate(DateUtil.parse(timepams), meetingroomList);
        }
        String mtRoom = MeetingRoomHelper.meetingroomToJson(list);

        String jsonMt = MeetingRoomHelper.meetingroomAppToJson(appedList, meetingId, AppContext.currentUserId(), action);

        String returnMr = "null";
        String oldRoomAppId = "null";
        String[] returnMrs = Strings.isBlank(returnMrStr) ? null : returnMrStr.split(",", -1);//全部被分割成数组
        if (returnMrs != null && returnMrs.length > 3 && Strings.isNotBlank(returnMrs[1]) && Strings.isNotBlank(returnMrs[2])) {
            oldRoomAppId = "\"" + returnMrs[3] + "\"";

            mav.addObject("meetingRoom", returnMrs[0]);
            mav.addObject("startDate", returnMrs[1]);
            mav.addObject("endDate", returnMrs[2]);

            StringBuilder buffer = new StringBuilder();
            buffer.append("[");
            buffer.append("{");
            buffer.append("id: \"" + returnMrs[3] + "\",");
            buffer.append("start_date: \"" + returnMrs[1] + "\",");
            buffer.append("end_date: \"" + returnMrs[2] + "\",");
            buffer.append("section_id: \"" + returnMrs[0] + "\"");
            buffer.append("}");
            buffer.append("]");
            returnMr = buffer.toString();
        }

        mav.addObject("mtRoom", mtRoom);
        mav.addObject("jsonMt", jsonMt);
        mav.addObject("needApp", needApp);
        mav.addObject("returnMr", returnMr);
        mav.addObject("oldRoomAppId", oldRoomAppId);
        mav.addObject("action", action);
        mav.addObject("meetId", meetingId);
        mav.addObject("appType", appType);
        mav.addObject("periodicityId", periodicityId);

        //<会议室ID,名称+是否需要审核>(表单触发会议，选择会议室时，需要获取到会议室名称）
        Map<String, String> meetingRoomNameList = new HashMap<String, String>();
        for (MeetingRoom mt : list) {
            meetingRoomNameList.put(String.valueOf(mt.getId()), mt.getName() + "," + mt.getNeedApp());
        }
        mav.addObject("meetingRoomNameList", JSONUtil.toJSONString(meetingRoomNameList));

        //这个日期字符串，需要在jsp中转为js日期对象(注意这里的格式：yyyy/MM/dd HH:mm，这样js通过 new Date(str)才能转成功)
        mav.addObject("newDate", Datetimes.format(new Date(), DateFormatEnum.yyyyMMddHHmm2.key()));
        //传递会议室看板中开始结束时间
        mav.addObject("meetingBeginDate", meetingBeginDate);
        mav.addObject("meetingEndDate", meetingEndDate);

        return mav;
    }

    public ModelAndView mtroomAjax(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/mtroom");
        String action = Strings.toHTML(request.getParameter("action"));//判断是增加还是修改(create新增,edit修改)
        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? -1L : Long.parseLong(Strings.toHTML(request.getParameter("meetingId")));
        if ("edit".equals(action)) {//如果是修改,则获取会议的ID
            mav.addObject("meetId", meetingId);
        }
        int sortType = Strings.isBlank(request.getParameter("sort")) ? RoomSortByEnum.name.key() : Integer.parseInt(Strings.escapeJavascript(request.getParameter("needApp")));
        String timepams = Strings.isBlank(request.getParameter("timepams")) ? DateUtil.get19DateAndTime() : Strings.escapeJavascript(request.getParameter("timepams"));

        List<MeetingRoomApp> appedList = new ArrayList<MeetingRoomApp>();

        List<MeetingRoom> list = this.meetingRoomManager.getMyCanAppRoomList(AppContext.getCurrentUser(), sortType, null, null);
        if (Strings.isNotEmpty(list)) {
            List<Long> meetingroomList = new ArrayList<Long>();
            for (MeetingRoom bean : list) {
                meetingroomList.add(bean.getId());
            }
            appedList = this.meetingRoomManager.getUsedRoomAppListByDate(DateUtil.parse(timepams), meetingroomList);
        }

        String jsonMt = MeetingRoomHelper.meetingroomAppToJson(appedList, meetingId, AppContext.currentUserId(), action);

        //json字符串中文乱码
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(jsonMt);
        out.close();
        return null;
    }

    public ModelAndView videoMtroom(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/mtroom");
        int needApp = Strings.isBlank(request.getParameter("needApp")) ? -1 : Integer.parseInt(request.getParameter("needApp"));
        //排序方式(RoomSortByEnum {createTime(1),name(2);})
        int sortType = Strings.isBlank(request.getParameter("sort")) ? RoomSortByEnum.name.key() : Integer.parseInt(request.getParameter("needApp"));
        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? -1L : Long.parseLong(request.getParameter("meetingId"));
        String action = request.getParameter("action");//判断是增加还是修改(create新增,edit修改)
        //看板默认显示的日期
        String timepams = Strings.isBlank(request.getParameter("timepams")) ? DateUtil.get19DateAndTime() : request.getParameter("timepams");
        //RoomApp表示入口是【申请会议室】 MtMeeting表示入口是【新建会议通知】
        String appType = request.getParameter("appType");
        String meetingBeginDate = request.getParameter("meetingBeginDate");
        String meetingEndDate = request.getParameter("meetingEndDate");
        String returnMrStr = request.getParameter("returnMr");//获取返回的记录

        MeetingVideoManager meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
        if (meetingVideoManager != null) {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("bmTime", DateUtil.toDate(timepams, DateFormatEnum.yyyyMMdd.key()).getTime());

            List<Map<String, Object>> videoRoomDetail = meetingVideoManager.getVideoInfo(param);

            String mtRoom = meetingApplicationHandler.meetingroomToJson(videoRoomDetail);
            String jsonMt = meetingApplicationHandler.meetingroomAppToJson(videoRoomDetail);

            Map<String, String> meetingRoomNameList = meetingApplicationHandler.getMeetingroomName(videoRoomDetail);

            mav.addObject("mtRoom", mtRoom);
            mav.addObject("jsonMt", jsonMt);
            mav.addObject("meetingRoomNameList", JSONUtil.toJSONString(meetingRoomNameList));
        }

        String returnMr = "null";
        String oldRoomAppId = "null";//视频会议室申请ID
        String[] returnMrs = Strings.isBlank(returnMrStr) ? null : returnMrStr.split(",", -1);//全部被分割成数组
        if (returnMrs != null && returnMrs.length > 3 && Strings.isNotBlank(returnMrs[1]) && Strings.isNotBlank(returnMrs[2])) {
            oldRoomAppId = "\"" + returnMrs[3] + "\"";

            mav.addObject("meetingRoom", returnMrs[0]);
            mav.addObject("startDate", returnMrs[1]);
            mav.addObject("endDate", returnMrs[2]);

            StringBuilder buffer = new StringBuilder();
            buffer.append("[");
            buffer.append("{");
            buffer.append("id: \"" + returnMrs[3] + "\",");
            buffer.append("start_date: \"" + returnMrs[1] + "\",");
            buffer.append("end_date: \"" + returnMrs[2] + "\",");
            buffer.append("section_id: \"" + returnMrs[0] + "\"");
            buffer.append("}");
            buffer.append("]");
            returnMr = buffer.toString();
        }

        mav.addObject("needApp", needApp);
        mav.addObject("action", action);
        mav.addObject("meetId", meetingId);
        mav.addObject("appType", appType);
        mav.addObject("openFrom", "videoRoom");
        mav.addObject("returnMr", returnMr);
        if (Strings.isBlank(oldRoomAppId)) {
            oldRoomAppId = "-1";
        }
        mav.addObject("oldRoomAppId", oldRoomAppId);

        //这个日期字符串，需要在jsp中转为js日期对象(注意这里的格式：yyyy/MM/dd HH:mm，这样js通过 new Date(str)才能转成功)
        mav.addObject("newDate", Datetimes.format(new Date(), DateFormatEnum.yyyyMMddHHmm2.key()));
        //传递会议室看板中开始结束时间
        mav.addObject("meetingBeginDate", meetingBeginDate);
        mav.addObject("meetingEndDate", meetingEndDate);

        return mav;
    }

    public ModelAndView videoMtroomAjax(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/mtroom");
        String action = Strings.toHTML(request.getParameter("action"));//判断是增加还是修改(create新增,edit修改)
        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? -1L : Long.parseLong(Strings.toHTML(request.getParameter("meetingId")));
        if ("edit".equals(action)) {//如果是修改,则获取会议的ID
            mav.addObject("meetId", meetingId);
        }
        int sortType = Strings.isBlank(request.getParameter("sort")) ? RoomSortByEnum.name.key() : Integer.parseInt(Strings.escapeJavascript(request.getParameter("needApp")));
        String timepams = Strings.isBlank(request.getParameter("timepams")) ? DateUtil.get19DateAndTime() : Strings.escapeJavascript(request.getParameter("timepams"));

        String jsonMt = "";
        MeetingVideoManager meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
        if (meetingVideoManager != null) {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("bmTime", DateUtil.toDate(timepams, DateFormatEnum.yyyyMMdd.key()).getTime());

            List<Map<String, Object>> videoRoomDetail = meetingVideoManager.getVideoInfo(param);

            jsonMt = meetingApplicationHandler.meetingroomAppToJson(videoRoomDetail);

        }
        //json字符串中文乱码
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(jsonMt);
        out.close();
        return null;
    }

    public ModelAndView checkVideoRoomUsed(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String flag = "y";

        String roomId = Strings.isBlank(request.getParameter("roomId")) ? "-1" : request.getParameter("roomId");
        String oldRoomAppId = Strings.isBlank(request.getParameter("oldRoomAppId")) ? "-1" : request.getParameter("oldRoomAppId");
        String sDate = request.getParameter("startDate");
        String eDate = request.getParameter("endDate");
        if (Strings.isNotBlank(sDate) && sDate.length() < 17) {
            sDate += ":59";
        }
        if (Strings.isNotBlank(eDate) && eDate.length() < 17) {
            eDate += ":00";
        }

        MeetingVideoManager meetingVideoManager = meetingApplicationHandler.getMeetingVideoHandler();
        if (meetingVideoManager != null) {
            Long startDate = DateUtil.toDate(sDate, DateFormatEnum.yyyyMMddHHmmss.key()).getTime();
            Long endDate = DateUtil.toDate(eDate, DateFormatEnum.yyyyMMddHHmmss.key()).getTime();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("key", oldRoomAppId);
            param.put("bmAccount", roomId);
            param.put("startTime", startDate);
            param.put("endTime", endDate);

            boolean isRepeat = meetingVideoManager.checkTimeStatus(param);
            if (!isRepeat) {
                flag = "n";
            }
        }
        PrintWriter out = response.getWriter();
        out.println(flag);
        out.close();

        return null;
    }

    /**
     * ajax方式获取会议室
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getAllMeetingRoomAjax(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //RoomSortByEnum {createTime(1),name(2);}
        int sortType = Integer.valueOf(Strings.toHTML(request.getParameter("sort")));

        User currentUser = AppContext.getCurrentUser();
        request.setCharacterEncoding("UTF-8");

        String roomName = request.getParameter("roomName");
        List<MeetingRoom> list = this.meetingRoomManager.getMyCanAppRoomList(currentUser, sortType, roomName, null);

        String mtRoom = MeetingRoomHelper.meetingroomToJson(list);

        response.setContentType("application/text;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(mtRoom);
        out.close();

        return null;
    }

    /**
     * 点击会议室获取会议室信息
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getMeetingRoomById(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/mtroominfo");
        Long roomId = Strings.isBlank(request.getParameter("roomId")) ? -1L : Long.parseLong(request.getParameter("roomId"));

        MeetingRoom meetingRoom = meetingRoomManager.getRoomById(roomId);
        List<Attachment> attatchmentsC = attachmentManager.getByReference(meetingRoom.getId(), RoomAttEnum.attachment.key());
        if (attatchmentsC.size() > 0) {
            mav.addObject("attatchments", attatchmentsC);
            mav.addObject("attObj", attatchmentsC.get(0));
        }
        List<Attachment> attatchmentsI = attachmentManager.getByReference(meetingRoom.getId(), RoomAttEnum.image.key());
        if (attatchmentsI.size() > 0) {
            mav.addObject("attatchImage", attatchmentsI);
            // 会议室支持多图片快速需求改造
            mav.addObject("imageObjs", attatchmentsI);
        }
        mav.addObject("meetingRoom", meetingRoom);
        return mav;
    }

    /**
     * 点击确定时将ID值转化成会议室名称
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getMeetingRoomByIdAjax(HttpServletRequest request, HttpServletResponse response) throws Exception {

        Long roomId = Strings.isBlank(request.getParameter("roomId")) ? -1L : Long.parseLong(Strings.toHTML(request.getParameter("roomId")));
        MeetingRoom room = this.meetingRoomManager.getRoomById(roomId);
        String mtRoom = null;
        if (room != null) {
            mtRoom = room.getId() + "," + room.getName() + "," + room.getNeedApp();
        }

        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(mtRoom);
        out.close();

        return null;
    }

    public ModelAndView checkRoomUsed(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String flag = "y";

        Long roomId = Strings.isBlank(request.getParameter("roomId")) ? -1L : Long.parseLong(request.getParameter("roomId"));
        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? -1L : Long.parseLong(request.getParameter("meetingId"));
        Long periodicityId = Strings.isBlank(request.getParameter("periodicityId")) ? -1L : Long.parseLong(request.getParameter("periodicityId"));
        String sDate = request.getParameter("startDate");
        String eDate = request.getParameter("endDate");
        if (Strings.isNotBlank(sDate) && sDate.length() < 17) {
            sDate += ":00";
        }
        if (Strings.isNotBlank(eDate) && eDate.length() < 17) {
            eDate += ":00";
        }
        Date startDate = DateUtil.parse(sDate, DateFormatEnum.yyyyMMddHHmmss.key());
        Date endDate = DateUtil.parse(eDate, DateFormatEnum.yyyyMMddHHmmss.key());

        boolean isRepeat = this.meetingValidationManager.checkRoomUsed(roomId, startDate, endDate, meetingId, null, periodicityId);
        if (!isRepeat) {
            flag = "n";
        }

        PrintWriter out = response.getWriter();
        out.println(flag);
        out.close();

        return null;
    }

    /**
     * 会议室管理主框架页面
     *
     * @param request
     * @param response
     * @return 转到index.jsp页面
     * @throws Exception
     */
    //@CheckRoleAccess(roleTypes={Role_NAME.MeetingRoomAdmin})
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!MeetingRoomRoleUtil.hasRoomResource(AppContext.getCurrentUser())) {
            return refreshWorkspace();
        }
        boolean isAdmin = MeetingRoomRoleUtil.isMeetingRoomAdminRole();

        ModelAndView mav = new ModelAndView("meetingroom/index");
        mav.addObject("isAdmin", isAdmin);
        if (isAdmin) {
            mav.addObject("top", 2);
        } else {
            mav.addObject("top", 1);
        }

        //这一段是给报表的，进入时默认回填值
        Date now = Datetimes.getTodayLastTime();
        Date firstDay = Datetimes.getFirstDayInYear(now);
        String[] defaultValues = new String[]{Datetimes.format(firstDay, Datetimes.datetimeWithoutSecondStyle), Datetimes.format(now, Datetimes.datetimeWithoutSecondStyle)};
        Map<String, Object> defaultMap = new HashMap<String, Object>();
        defaultMap.put("meeting_room_app.startDatetime", defaultValues);
        mav.addObject("reportFilterValue", URLEncoder.encode(JSONUtil.toJSONString(defaultMap), "utf-8"));

        return mav;
    }

    /**
     * 新建会议室主框架页面
     *
     * @param request
     * @param response
     * @return 转到add.jsp页面
     * @throws Exception
     */
    //@CheckRoleAccess(roleTypes={Role_NAME.MeetingRoomAdmin})
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/add");
        return mav;
    }

    /**
     * 会议室管理的左侧页面
     *
     * @return 转到meetListLeft.jsp页面
     * @throws Exception
     */
    //@CheckRoleAccess(roleTypes={Role_NAME.MeetingRoomAdmin})
    public ModelAndView meetListLeft(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/meetListLeft");
        String from = request.getParameter("from");
        mav.addObject("from", from);
        return mav;
    }

    /**
     * 会议室申请主框架页面
     *
     * @param request
     * @param response
     * @return 转到app.jsp页面
     * @throws Exception
     */
    public ModelAndView app(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/app");
        return mav;
    }

    /**
     * 会议室审批主框架页面
     *
     * @param request
     * @param response
     * @return 转到perm.jsp页面
     * @throws Exception
     */
    //@CheckRoleAccess(roleTypes={Role_NAME.MeetingRoomAdmin})
    public ModelAndView perm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/perm");
        return mav;
    }

    /**
     * 会议室预定撤销主框架页面
     *
     * @param request
     * @param response
     * @return 转到cancel.jsp页面
     * @throws Exception
     */
    public ModelAndView cancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String select = request.getParameter("select");
        String flag = request.getParameter("flag");
        ModelAndView mav = new ModelAndView("meetingroom/cancel");
        mav.addObject("select", select);
        mav.addObject("flag", flag);
        return mav;
    }

    /**
     * 会议室统计主框架页面
     *
     * @param request
     * @param response
     * @return 转到total.jsp页面
     * @throws Exception
     */
    public ModelAndView total(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/total");
        return mav;
    }

    /**
     * 会议室使用情况查看主框架页面
     *
     * @param request
     * @param response
     * @return 转到view.jsp页面
     * @throws Exception
     */
    public ModelAndView view(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/view");
        return mav;
    }

    private String getCloseWindowFunction() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("function closeWindow() {");
        buffer.append("  if(window.dialogArguments && window.dialogArguments.callback) {");
        buffer.append("    window.dialogArguments.callback();");
        buffer.append("  } else if(window.dialogArguments && window.dialogArguments.dialogDealColl) {");
        buffer.append("    window.dialogArguments.dialogDealColl.close();");
        buffer.append("    window.dialogArguments.location.reload();");
        buffer.append("  } else if(window.dialogArguments) {");
        buffer.append("    window.dialogArguments.getA8Top().reFlesh();");
        buffer.append("    parent.window.close();");
        buffer.append("  } else {");
        buffer.append("    if(parent.window.listFrame) {");
        buffer.append("       parent.window.listFrame.location.reload();");
        buffer.append("    } else {");
        buffer.append("  	  parent.window.close();");
        buffer.append("	   }");
        buffer.append("  }");
        buffer.append("}");
        return buffer.toString();
    }

    /****************************** 依赖注入 **********************************/
    public void setMeetingRoomManager(MeetingRoomManager meetingRoomManager) {
        this.meetingRoomManager = meetingRoomManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setMeetingValidationManager(MeetingValidationManager meetingValidationManager) {
        this.meetingValidationManager = meetingValidationManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setMeetingManager(MeetingManager meetingManager) {
        this.meetingManager = meetingManager;
    }

    public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
        this.mobileMessageManager = mobileMessageManager;
    }

    public MeetingResourcesManager getMeetingResourcesManager() {
        return meetingResourcesManager;
    }

    public void setMeetingResourcesManager(MeetingResourcesManager meetingResourcesManager) {
        this.meetingResourcesManager = meetingResourcesManager;
    }

    public MeetingLockManager getMeetingLockManager() {
        return meetingLockManager;
    }

    public void setMeetingLockManager(MeetingLockManager meetingLockManager) {
        this.meetingLockManager = meetingLockManager;
    }

    public MeetingApplicationHandler getMeetingApplicationHandler() {
        return meetingApplicationHandler;
    }

    public void setMeetingApplicationHandler(MeetingApplicationHandler meetingApplicationHandler) {
        this.meetingApplicationHandler = meetingApplicationHandler;
    }

    public void setMeetingSettingManager(MeetingSettingManager meetingSettingManager) {
        this.meetingSettingManager = meetingSettingManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 打开催办会议室管理员审核会议室申请操作窗口
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView openRemindersDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.reminders_dialog);
        // 是否可以发送短信
        boolean isCanSendSMS = false;
        if (SystemEnvironment.hasPlugin("sms")) {
            isCanSendSMS = mobileMessageManager.isCanUseSMS();
        }
        mav.addObject("canSendPhone", isCanSendSMS);
        // 要催办的用户集合
        List<V3xOrgMember> userList = new ArrayList<V3xOrgMember>();
        String auditIds = request.getParameter("auditIds");
        String[] admins = auditIds.split(",");
        for (String memberId : admins) {
            V3xOrgMember member = orgManager.getMemberById(Long.valueOf(memberId.trim()));
            if (member.isValid())
                userList.add(member);
        }
        mav.addObject("userList", userList);
        return mav;
    }

    /**
     * 执行会议室申请催办
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView execReminders(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String memberIdArray = request.getParameter("memberIdArray");
        String[] memberIdArrays = memberIdArray.split(",");
        List<Long> memberIds = new ArrayList<Long>();
        for (String memberId : memberIdArrays) {
            memberIds.add(Long.parseLong(memberId));
        }

        String content = request.getParameter("remindContent");
        String sendPhoneMessage = request.getParameter("sendPhoneMessage");
        String roomAppId = request.getParameter("roomAppId");
        User user = AppContext.getCurrentUser();
        // 催办
        try {
            meetingRoomManager.execReminders(memberIds, content, Boolean.getBoolean(sendPhoneMessage), Long.parseLong(roomAppId), user);
        } catch (Exception e) {
            LOGGER.error("会议室申请催办出错", e);
        }
        return null;
    }

    /**
     * 会议室二维码打印
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView showBarCodePrint(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mv = new ModelAndView(MeetingPathConstant.bar_code_print);
        Long roomId = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        MeetingRoom room = this.meetingRoomManager.getRoomById(roomId);
        if (room == null) {
            return refreshWorkspace();
        }
        MeetingRoomVO roomVo = new MeetingRoomVO();
        roomVo = MeetingRoomHelper.convertToVO(roomVo, room);
        if (roomVo.getQrCodeApply() != null) {
            mv.addObject("qrCodeFile", fileManager.getV3XFile(roomVo.getQrCodeApply()));
        }
        MeetingScreenSet screenSet = meetingSettingManager.getMeetingScreenSet(room.getAccountId());
        mv.addObject("logoId", screenSet == null ? null : screenSet.getLogoId());
        mv.addObject("bean", roomVo);
        return mv;
    }
}
