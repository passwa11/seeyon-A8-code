package com.seeyon.apps.meeting.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManager;
import com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManagerImpl;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkOaMapper;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkUsers;
import com.seeyon.apps.ext.welinkMenu.util.WelinkUtil;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.apps.meeting.bo.MeetingBO;
import com.seeyon.apps.meeting.constants.MeetingConstant;
import com.seeyon.apps.meeting.constants.MeetingConstant.*;
import com.seeyon.apps.meeting.dao.MeetingResourcesDao;
import com.seeyon.apps.meeting.dao.MeetingTemplateDao;
import com.seeyon.apps.meeting.event.MeetingDeleteEvent;
import com.seeyon.apps.meeting.event.MeetingUpdateEvent;
import com.seeyon.apps.meeting.po.MeetingPeriodicity;
import com.seeyon.apps.meeting.po.MeetingResources;
import com.seeyon.apps.meeting.po.MeetingSummary;
import com.seeyon.apps.meeting.po.MeetingTemplate;
import com.seeyon.apps.meeting.quartz.MeetingQuartzJobManager;
import com.seeyon.apps.meeting.util.MeetingHelper;
import com.seeyon.apps.meeting.util.MeetingNewHelper;
import com.seeyon.apps.meeting.util.MeetingTransHelper;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meeting.vo.ConfereesConflictVO;
import com.seeyon.apps.meeting.vo.MeetingNewVO;
import com.seeyon.apps.meeting.vo.MeetingOptionListVO;
import com.seeyon.apps.meetingroom.manager.MeetingRoomManager;
import com.seeyon.apps.meetingroom.po.MeetingRoomApp;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.apps.project.api.ProjectApi;
import com.seeyon.apps.project.bo.ProjectBO;
import com.seeyon.apps.project.po.ProjectPhaseEvent;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.*;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.meeting.domain.MtMeeting;
import com.seeyon.v3x.meeting.domain.MtReply;
import com.seeyon.v3x.meeting.util.DataTransUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 唐桂林
 */
public class MeetingNewManagerImpl implements MeetingNewManager {

    private static final Log LOGGER = LogFactory.getLog(MeetingNewManagerImpl.class);

    private MeetingManager meetingManager;
    private MeetingTypeManager meetingTypeManager;
    private MeetingTypeRecordManager meetingTypeRecordManager;
    private MeetingRoomManager meetingRoomManager;
    private MeetingExtManager meetingExtManager;
    private MeetingResourcesManager meetingResourcesManager;
    private MeetingQuartzJobManager meetingQuartzJobManager;
    private MeetingPeriodicityManager meetingPeriodicityManager;
    private MeetingMessageManager meetingMessageManager;
    private MeetingSummaryManager meetingSummaryManager;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private AppLogManager appLogManager;
    private IndexApi indexApi;
    private MeetingTemplateDao meetingTemplateDao;
    private ConfereesConflictManager confereesConflictManager;
    private ProjectApi projectApi;
    private MeetingValidationManager meetingValidationManager;
    private MeetingReplyManager meetingReplyManager;
    private MeetingApplicationHandler meetingApplicationHandler;
    private MeetingBarCodeManager meetingBarCodeManager;
    private FileManager fileManager;
    private MeetingResourcesDao meetingResourcesDao;

    private com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManager welinkMenuManager = new welinkMenuManagerImpl();

    /****************************** 会议新建 **********************************/
    /**
     * 新建/编辑会议
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean newMeeting(MeetingNewVO newVo) throws BusinessException {
        User currentUser = newVo.getCurrentUser();
        MtMeeting meeting = new MtMeeting();
        newVo.setMeeting(meeting);

        /************** 新建会议 **************/
        if (newVo.isNew()) {
            //设置会议默认属性
            MeetingNewHelper.setNewMeetingDefaultValue(newVo);

            if (!MeetingUtil.isIdNull(meeting.getRoom())) {
                newVo.setMeetingRoom(meetingRoomManager.getRoomById(meeting.getRoom()));
            }
        }
        /************** 编辑会议 **************/
        else {
            meeting = meetingManager.getMeetingById(newVo.getMeetingId());
            newVo.setMeeting(meeting);

            if (!MeetingHelper.isPeriodicity(meeting.getCategory())) {
                newVo.setIsBatch(false);
            }

            //设置正文信息
            meetingExtManager.setContent(newVo);

            //视频会议相关信息
            if (MeetingUtil.hasMeetingVideoPlugin()) {
                try {
                    String ext4 = meeting.getVideoMeetingId();
                    if (Strings.isNotBlank(ext4)) {
                        Map<String, Object> ext4Map = JSONUtil.parseJSONString(ext4, Map.class);
                        if (meeting.getEmceeId().equals(currentUser.getId())) {
                            if (null != ext4Map.get("pcode1")) {
                                newVo.getMeeting().setMeetingPassword(String.valueOf(ext4Map.get("pcode1")));
                            }
                        } else {
                            if (null != ext4Map.get("pcode2")) {
                                newVo.getMeeting().setMeetingPassword(String.valueOf(ext4Map.get("pcode2")));
                            }
                        }
                        if (null != ext4Map.get("videoRoomId")) {
                            String videoRoomName = (String) ext4Map.get("videoRoomName");

                            String videoRoomAppBeginStr = (String) ext4Map.get("videoRoomAppBeginDate");
                            String videoRoomAppEndDateStr = (String) ext4Map.get("videoRoomAppEndDate");

                            newVo.setVideoRoomId((String) ext4Map.get("videoRoomId"));
                            newVo.setVideoRoomName(videoRoomName);
                            newVo.setVideoRoomAppId((String) ext4Map.get("videoRoomAppId"));
                            if (Strings.isNotBlank(videoRoomAppEndDateStr) && Strings.isNotBlank(videoRoomAppBeginStr)) {
                                try {
                                    Date videoRoomAppBeginDate = DateUtil.parse(videoRoomAppBeginStr, Datetimes.datetimeAllStyle);
                                    Date videoRoomAppEndDate = DateUtil.parse(videoRoomAppEndDateStr, Datetimes.datetimeAllStyle);
                                    newVo.setVideoRoomAppBeginDate(videoRoomAppBeginDate);
                                    newVo.setVideoRoomAppEndDate(videoRoomAppEndDate);
                                    newVo.setShowVideoRoom(videoRoomName + "(" + Datetimes.format(videoRoomAppBeginDate, Datetimes.datetimeWithoutSecondStyle) + "-" + Datetimes.format(videoRoomAppEndDate, Datetimes.datetimeWithoutSecondStyle) + ")");
                                } catch (ParseException e) {
                                    LOGGER.error("", e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("转换videoMeetingId视频会议参数JSON格式异常", e);
                }
            }
        }
        //设置会议室选项
        if (newVo.isRoomAppNew()) {
            newVo.setMeetingroomAppedNameList(meetingRoomManager.getRoomAppedNameListByUserId(currentUser.getId()));
        } else {
            List<MeetingOptionListVO> roomAppList = meetingRoomManager.getRoomAppedNameByRoomAppId(newVo.getRoomAppId());
            if (Strings.isNotEmpty(roomAppList)) {
                MeetingOptionListVO vo = roomAppList.get(0);
                newVo.setMyMeetingroomAppedName(vo);
                newVo.getMeeting().setBeginDate(vo.getBeginDate());
                newVo.getMeeting().setEndDate(vo.getEndDate());
                //用于判断会议室时间与会议时间是否一致
                MeetingRoomApp mtApp = new MeetingRoomApp();
                mtApp.setStartDatetime(vo.getBeginDate());
                mtApp.setEndDatetime(vo.getEndDate());
                newVo.setMeetingRoomApp(mtApp);
            }
        }
        if (!MeetingUtil.isIdNull(meeting.getRoom())) {
            newVo.setRoomId(meeting.getRoom());

            MeetingRoomApp roomApp = meetingRoomManager.getRoomAppByRoomAndMeetingId(meeting.getRoom(), meeting.getId());
            if (roomApp != null) {
                newVo.setMeetingRoomApp(roomApp);

                if (!MeetingHelper.isWait(meeting.getState())) {
                    List<MeetingOptionListVO> roomAppList = meetingRoomManager.getRoomAppedNameByRoomAppId(roomApp.getId());
                    if (Strings.isNotEmpty(roomAppList)) {
                        newVo.setMyMeetingroomAppedName(roomAppList.get(0));
                    }
                }
            }
        }

        boolean meetingflag = true;
        String openFrom = ParamUtil.getString(newVo.getParameterMap(), "openFrom");
        /************** 调用模板 **************/
        if ("chooseTemplate".equals(openFrom)) {
            meetingExtManager.setMeetingByTemplate(newVo);
            meetingflag = false;
        }

        /************** 格式模板 **************/
        if ("chooseContent".equals(openFrom)) {
            MeetingNewHelper.setMeetingByVo(newVo);
            meetingExtManager.setMeetingByContentTemplate(newVo);
            meetingflag = false;
        }

        /************** 转发会议 **************/
        if (meetingflag && newVo.isFromApp()) {
            meetingExtManager.setMeetingByApp(newVo);
        }

        /** 获取周期会议设置 */
        String periodicityType = newVo.getParameterMap().get("periodicityType");
        if (!MeetingUtil.isIdNull(meeting.getPeriodicityId())) {
            newVo.setPeriodicity(meetingPeriodicityManager.getPeriodicityById(meeting.getPeriodicityId()));
            newVo.setPeriodicityId(meeting.getPeriodicityId());
        } else if (Strings.isNotBlank(periodicityType)) {
            MeetingPeriodicity periodicity = new MeetingPeriodicity();
            periodicity.setPeriodicityType(Integer.valueOf(periodicityType));
            periodicity.setScope(newVo.getParameterMap().get("periodicityScope"));
            periodicity.setStartDate(Datetimes.parse(newVo.getParameterMap().get("periodicityStartDate")));
            periodicity.setEndDate(Datetimes.parse(newVo.getParameterMap().get("periodicityEndDate")));
            newVo.setPeriodicity(periodicity);
        }

        /** 会议方式列表(普通会议，红杉树视频会议) */
        meetingExtManager.setNatureNameList(newVo);

        /** 设置会议分类列表 */
        meetingExtManager.setMeetingTypeNameList(newVo);

        /** 会议资源列表 */
        meetingExtManager.setResourceNameList(newVo);

        /** 会议格式模板列表 */
        meetingExtManager.setContentTemplateNameList(newVo);

        /** 会议提前时间枚举 */
        meetingExtManager.setMeetingRemindTimeEnum(newVo);

        /** 设置关联项目参数 */
        meetingExtManager.setProjectNameList(newVo);

        /** 会议参会人员 */
        meetingExtManager.setConfereesNameList(newVo);

        /** 设置视频会议参数 */
        meetingExtManager.setVideoMeetingParameter(newVo);

        //会议附件回填
        meetingExtManager.setMeetingAttachment(newVo);

        //是否显示申请会议室按钮
        boolean isShowRoom = currentUser.hasResourceCode("F09_meetingRoomApp") && currentUser.hasResourceCode("F09_meetingRoom");
        newVo.setIsShowRoom(isShowRoom);
        return true;
    }

    /**
     * 设置会议周期
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean newPeriodicity(MeetingNewVO newVo) throws BusinessException {
        this.meetingPeriodicityManager.setNewPeriodicity(newVo);
        return true;
    }

    /**
     * 发送/保存会议
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transSend(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = null;
        boolean welink = true;
        if (newVo.isNew()) {
            meeting = new MtMeeting();
        } else {
            meeting = meetingManager.getMeetingById(newVo.getMeetingId());
            newVo.setOldMeeting(MeetingTransHelper.cloneMeeting(meeting));
            welink = false;
        }
        newVo.setMeeting(meeting);

        MeetingNewHelper.setMeetingDefaultValueByVo(newVo);
        MeetingNewHelper.setMeetingByVo(newVo);

        ReplaceBase64Result base64Result = fileManager.replaceBase64Image(meeting.getContent());
        meeting.setContent(base64Result.getHtml());
        /**********************zhou:开始 调用welink接口**************************/
        Long currentUserID = CurrentUser.get().getId();
        WeLinkUsers weLinkUsers = welinkMenuManager.selectByCurrentUserId(currentUserID + "");
        String token = WelinkUtil.getInstance().getToken(weLinkUsers.getWeLinkLoginname(), weLinkUsers.getWeLinkPwd());

        //初始化welink请求体
        String subject = meeting.getTitle();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Date beginTime = meeting.getBeginDate();
        LocalDateTime dateTime = beginTime.toInstant().atOffset(ZoneOffset.of("+0")).toLocalDateTime();
        String beginDate = dateTime.format(dtf);
        System.out.println(beginDate);
        Map<String, Object> _cMap = new ConcurrentHashMap<>();
        _cMap.put("mediaTypes", "Video,Voice");
        _cMap.put("conferenceType", 0);//普通会议
        _cMap.put("startTime", beginDate);
        _cMap.put("subject", subject);

        Long s = meeting.getBeginDate().getTime();
        Long e = meeting.getEndDate().getTime();
        long length = (e - s) / 60000;
        Integer len = Integer.parseInt(length + "");
        _cMap.put("length", len);
        List<String> stringList = new ArrayList<>();
        if (meeting.getConferees().length() > 0) {
            if (meeting.getConferees().indexOf(",") != -1) {
                String[] userIDs = meeting.getConferees().split(",");
                String[] _s = null;
                for (String s1 : userIDs) {
                    _s = s1.split("\\|");
                    if (_s[0].equals("Department")) {
                        List<String> dlist = welinkMenuManager.selectUserIDByOrgId(_s[1]);
                        stringList.addAll(dlist);
                    } else if (_s[0].equals("Member")) {
                        stringList.add(_s[1]);
                    }
                }
            } else {
                String[] _s = meeting.getConferees().split("\\|");
                if (_s[0].equals("Department")) {
                    List<String> dlist = welinkMenuManager.selectUserIDByOrgId(_s[1]);
                    stringList.addAll(dlist);
                } else if (_s[0].equals("Member")) {
                    stringList.add(_s[1]);
                }
            }
        }
        List<Map<String, String>> mapList = new ArrayList<>();
        List<WeLinkUsers> weLinkUsersList = welinkMenuManager.selectListByOauserIdOfUnion(stringList);
        Map<String, String> u1 = null;
        if (null != weLinkUsersList && weLinkUsersList.size() > 0) {
            for (WeLinkUsers weLinkUsers1 : weLinkUsersList) {
                u1 = new ConcurrentHashMap<>();
                u1.put("accountId", weLinkUsers1.getWeLinkLoginname());
                u1.put("name", weLinkUsers1.getOaUserName());
                u1.put("role", "0");
                u1.put("phone", weLinkUsers1.getOaPhone());
                u1.put("isMute", "0");
                mapList.add(u1);
            }
        }

        if (mapList.size() > 0) {
            _cMap.put("attendees", mapList);
        }

        if (welink) {
//			新建
            String create = WelinkUtil.getInstance().welinkCreateMeeting(token, _cMap);
            if (create != null && create.length() > 0) {
                WeLinkOaMapper weLinkOaMapper = new WeLinkOaMapper();
                weLinkOaMapper.setOaMeetingId(meeting.getId() + "");
                JSONArray jsonArray = JSONArray.parseArray(create);
                String info = jsonArray.get(0).toString();
                JSONObject jsonObject = JSONObject.parseObject(info);
                weLinkOaMapper.setWelinkMeetingId((String) jsonObject.get("conferenceID"));
                welinkMenuManager.insertWlAndOaMapper(weLinkOaMapper);
            }
        } else {
//			修改
            Long meetingId = newVo.getMeetingId();
            WeLinkOaMapper oaMapper = welinkMenuManager.selectByOaMeetingId(meetingId + "");
            if (null != oaMapper) {
                String update = WelinkUtil.getInstance().welinkUpdateMeeting(token, oaMapper.getWelinkMeetingId(), _cMap);
            }
        }

        /**********************zhou:结束**************************/
        this.meetingExtManager.checkVideoMeetingData(newVo);

        /** 周期会议 */
        if (newVo.isCategoryPeriodicity()) {
            if (newVo.isNew()) {
                newVo.setIsBatch(Boolean.TRUE);
            }
            if (newVo.getIsBatch()) {
                meetingPeriodicityManager.transSavePeriodicity(newVo);
                meeting.setPeriodicityId(newVo.getPeriodicity().getId());
                meeting.setTemplateId(newVo.getPeriodicityTemplateId());

                if (Strings.isNotEmpty(newVo.getPeriodicityDatesList())) {
                    meeting.setBeginDate(newVo.getPeriodicityDatesList().get(0)[0]);
                    meeting.setEndDate(newVo.getPeriodicityDatesList().get(0)[1]);
                }
                //为周期会议写入一套附件信息，触发下一条会议时作为模板的一部分使用
                meetingExtManager.saveMeetingAttachment(newVo.isNew(), meeting.getPeriodicityId(), -1l);
                if (!Constants.EDITOR_TYPE_HTML.equals(meeting.getDataFormat())) {
                    //克隆一份相同的文件
                    meetingExtManager.cloneOfficeFile(Long.valueOf(meeting.getContent()), meeting.getPeriodicityId(), meeting.getCreateDate());
                }
            } else if (!newVo.isNew()) {
                newVo.setIsSingleEdit(Boolean.TRUE);
                meeting.setCategory(MeetingCategoryEnum.single.key());
                newVo.setCategory(MeetingCategoryEnum.single.key());
                meeting.setPeriodicityId(null);
            }
        }
        /** 保存会议资源 */
        if (newVo.isCategoryPeriodicity()) {
            saveTemplateResources(newVo.getTemplate());
        } else {
            meetingResourcesManager.saveMeetingResources(meeting);
        }
        /** 保存会议类型使用记录 */
        meetingTypeRecordManager.saveMeetingTypeRecord(meeting);
        //对比会议
        if (!newVo.isNew() && newVo.getOldMeeting() != null && !MeetingHelper.isWait(newVo.getOldMeeting().getState())) {
            newVo.setOldMtReplyList(meetingReplyManager.getReplyByMeetingId(newVo.getMeeting().getId()));
        }
        //保存附件
        saveMeetingAttachment(newVo.getIsM3(), newVo.isNew(), meeting.getId(), newVo.getIsBatch() ? meeting.getPeriodicityId() : -1l);

        if ((!newVo.isRoomAppNew() || !newVo.isRoomNew()) && !MeetingNewVO.NewMeeting_selectRoomType_MtPlace.equals(newVo.getSelectRoomType())) {
            //修改后-会议室申请不为空
            MeetingRoomAppVO appVo = new MeetingRoomAppVO();
            appVo.setCategory(newVo.getCategory());
            appVo.setIsBatch(newVo.getIsBatch());
            appVo.setIsSingleEdit(newVo.getIsSingleEdit());
            appVo.setSystemNowDatetime(newVo.getSystemNowDatetime());
            appVo.setRoomAppId(newVo.getRoomAppId());
            appVo.setOldRoomAppId(ParamUtil.getLong(newVo.getParameterMap(), "oldRoomAppId"));
            appVo.setOldRoomId(ParamUtil.getLong(newVo.getParameterMap(), "oldRoomId"));
            appVo.setRoomId(newVo.getRoomId());
            appVo.setMeetingId(meeting.getId());
            appVo.setMeeting(meeting);
            appVo.setIsNewMeetingPeriodicity(newVo.getIsNewMeetingPeriodicity());
            appVo.setAction(newVo.getAction());
            newVo.setMeetingRoomAppVO(appVo);

            if (!newVo.isRoomNew()) {
                appVo = MeetingNewHelper.getMeetingRoomAppVOByParameterMap(newVo);
            }
            //表单触发会议选择会议室时，判断会议室是否会被占用、是否过期
            boolean roomCanUse = true;
            if ("sendForm".equals(newVo.getAction())) {
                String roomState = checkMeetingRoomCanUse(appVo);
                //会议室使用时间异常
                if (!"true".equals(roomState) && !String.valueOf(appVo.getMeeting().getId()).equals(roomState)) {
                    roomCanUse = false;
                    newVo.setRoomState(roomState);
                }
            }
            if (roomCanUse) {
                //会议室冲突抛异常
                if (!meetingValidationManager.checkRoomUsed(appVo.getRoomId(), appVo.getStartDatetime(), appVo.getEndDatetime(), appVo.getMeetingId(), appVo.getRoomAppId(), appVo.getPeriodicityId())) {
                    newVo.setErrorMsg(ResourceUtil.getString("mr.alert.cannotapp"));
                    throw new BusinessException(ResourceUtil.getString("mr.alert.cannotapp"));
                }

                meetingRoomManager.transAppInMeeting(appVo);

                meeting.setRoomState(appVo.getMeetingRoomApp().getStatus());
                meeting.setRoom(appVo.getMeetingRoom().getId());
                meeting.setMeetPlace("");
                newVo.setRoomId(appVo.getMeetingRoom().getId());
                appVo.setRoomAppId(appVo.getMeetingRoomApp().getId());
            } else if ("sendForm".equals(newVo.getAction())) {//清空会议室
                meeting.setRoom(null);
                newVo.setRoomId(null);
                meeting.setRoomState(RoomAppStateEnum.pass.key());
            }

        } else {
            meeting.setRoomState(RoomAppStateEnum.pass.key());
            meeting.setRoom(Constants.GLOBAL_NULL_ID);
            newVo.setRoomId(Constants.GLOBAL_NULL_ID);

            if (!newVo.isNew() && !MeetingUtil.isIdNull(newVo.getOldMeeting().getRoom())) {
                Map<String, Object> cancelAppMap = new HashMap<String, Object>();
                cancelAppMap.put("currentUser", newVo.getCurrentUser());
                cancelAppMap.put("meetingId", meeting.getId());
                cancelAppMap.put("isBatch", newVo.getIsBatch());
                cancelAppMap.put("periodicity", newVo.getPeriodicity());
                meetingRoomManager.transCancelRoomApp(cancelAppMap);
            }
        }

        /**
         * 1.新建会议直接发布时创建关联二维码
         * 2.保存待发的会议进行发布时创建关联二维码
         */
        if (newVo.isNew() || MeetingHelper.isWait(newVo.getMeeting().getState())) {
            meetingBarCodeManager.createMeetingBarCode(meeting);
            meetingBarCodeManager.createMeetingInviteBarCode(meeting);
        }

        /**
         * 会议室申请信息发生变动时
         * 删除回执信息和签到信息
         * 删除之前的会议日程
         */
        boolean isRoomChanged = (newVo.getMeetingRoomAppVO() != null && null != newVo.getMeetingRoomAppVO().getRoomAppId() && !newVo.getMeetingRoomAppVO().getRoomAppId().equals(newVo.getMeetingRoomAppVO().getOldRoomAppId())) && "1".equals(newVo.getRoomNeedApp());
        if (isRoomChanged) {
            meetingReplyManager.deletePhysicalByMeetingId(newVo.getMeeting().getId());
            meetingBarCodeManager.deleteSign(newVo.getMeeting().getId());
            //为日程触发事件
            if (AppContext.hasPlugin("timeView")) {
                MeetingBO bo = new MeetingBO();
                bo.setId(meeting.getId());
                bo.setTitle(meeting.getTitle());
                MeetingDeleteEvent event = new MeetingDeleteEvent(this, bo, meeting.getCreateUser(), new Date());
                EventDispatcher.fireEventAfterCommit(event);
            }
        }

        if (MeetingHelper.isRoomPass(meeting.getRoomState())) {
            boolean result = this.transPublishMeeting(newVo);
            if (!result) {
                return false;
            }
        } else {
            //会议室为待审核状态，会议不能召开
            meeting.setState(MeetingStateEnum.send.key());

            //先发布再编辑申请会议室，再审核不通过，会议数据也发布出去了
            if (!newVo.isNew()) {
                affairManager.deletePhysicalByObjectId(newVo.getMeeting().getId());
                meetingQuartzJobManager.clearQuartzJob(newVo.getMeeting());
            }

            //如果当前是修改会议的情况。则需要给对应的人员发送取消的消息：
            if (!newVo.isNew() && newVo.getOldMeeting() != null && newVo.getOldMeeting().getRoomState() == RoomAppStateEnum.pass.key()) {
                //发给修改后撤销的与会人消息
                meetingMessageManager.sendMeetingCancelMessage(newVo);
            }

            /**
             * 生成回执信息
             */
            meetingManager.createReplies(meeting);
            /**
             * 发送视频会议
             */
            dealVideoMeeting(newVo);

            this.meetingManager.saveOrUpdate(meeting);
        }

        if (newVo.getIsSingleEdit()) {//单次修改
            newVo.setMeetingRoomAppVO(null);
            newVo.setRoomId(-1L);
            boolean result = this.transGenerateNextMeeting(newVo);
            if (!result) {
                return false;
            }
        } else {
            int i = 0;
            while (i < 300 && newVo.getMeeting() != null && MeetingHelper.isFinished(newVo.getMeeting().getState())) {
                i++;
                this.transFinishMeeting(newVo);
            }
        }
        return true;
    }

    /**
     * 处理视频会议相关
     */
    private boolean dealVideoMeeting(MeetingNewVO newVo) throws BusinessException {
        if (MeetingUtil.hasMeetingVideoPlugin() && MeetingHelper.isPending(newVo.getMeeting().getState())) {
            if (MeetingNatureEnum.video.key().equals(newVo.getMeeting().getMeetingType())) {
                boolean result = this.meetingExtManager.sendVideoMeeting(newVo);
                if (!result) {
                    return false;
                }
            } else if (MeetingNatureEnum.normal.key().equals(newVo.getMeeting().getMeetingType())) {
                if (newVo.getOldMeeting() != null && Strings.isNotBlank(newVo.getOldMeeting().getVideoMeetingId())) {
                    try {
                        meetingApplicationHandler.deleteVideoMeeting(newVo.getOldMeeting());
                    } catch (Exception e) {
                        LOGGER.error("删除视频会议出错：", e);
                        newVo.setErrorMsg(ResourceUtil.getString("meeting.video.send.error"));
                        if (MeetingConstant.VIDEO_MEEING_START_CODE.equals(e.getMessage())) {
                            newVo.setErrorMsg(ResourceUtil.getString("meeting.page.msg.videoMeetingStart"));
                        }
                        throw new BusinessException(e.getMessage());
                    }
                }
                newVo.getMeeting().setVideoMeetingId(null);
            }
        }
        return true;
    }

    private String checkMeetingRoomCanUse(MeetingRoomAppVO appVo) throws BusinessException {
        String state = "true";

        Long startDatetime = appVo.getStartDatetime().getTime();
        Long endDatetime = appVo.getEndDatetime().getTime();
        Long now = appVo.getSystemNowDatetime().getTime();
        String roomId = String.valueOf(appVo.getRoomId());

        if (now > startDatetime) {//会议室使用时间小于当前时间
            return "timeIsPast";
        }
        state = meetingValidationManager.checkRoomUsed(roomId, null, null, null, startDatetime, endDatetime, "", "", "", "", "", "", "");
        return state;
    }

    /**
     * 会议保存待发
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transSave(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = null;
        if (newVo.isNew()) {
            meeting = new MtMeeting();
        } else {
            meeting = meetingManager.getMeetingById(newVo.getMeetingId());
            newVo.setOldMeeting(MeetingTransHelper.cloneMeeting(meeting));
        }
        newVo.setMeeting(meeting);

        MeetingNewHelper.setMeetingByVo(newVo);
        MeetingNewHelper.setMeetingDefaultValueByVo(newVo);

        ReplaceBase64Result base64Result = fileManager.replaceBase64Image(meeting.getContent());
        meeting.setContent(base64Result.getHtml());

        /** 保存会议资源 */
        meetingResourcesManager.saveMeetingResources(meeting);

        /** 保存会议类型使用记录 */
        meetingTypeRecordManager.saveMeetingTypeRecord(meeting);

        /** 周期会议 */
        if (newVo.isCategoryPeriodicity()) {
            if (newVo.isNew()) {
                newVo.setIsBatch(Boolean.TRUE);
            }
            if (newVo.getIsBatch()) {
                if (!MeetingUtil.isIdNull(meeting.getPeriodicityId())) {
                    newVo.setPeriodicity(meetingPeriodicityManager.getPeriodicityById(meeting.getPeriodicityId()));
                }
                meetingPeriodicityManager.transSavePeriodicity(newVo);
                meeting.setPeriodicityId(newVo.getPeriodicity().getId());
                meeting.setTemplateId(newVo.getTemplate().getId());

                if (Strings.isNotEmpty(newVo.getPeriodicityDatesList())) {
                    meeting.setBeginDate(newVo.getPeriodicityDatesList().get(0)[0]);
                    meeting.setEndDate(newVo.getPeriodicityDatesList().get(0)[1]);
                }
                //为周期会议写入一套附件信息，触发下一条会议时作为模板的一部分使用
                meetingExtManager.saveMeetingAttachment(newVo.isNew(), meeting.getPeriodicityId(), -1l);
                if (!Constants.EDITOR_TYPE_HTML.equals(meeting.getDataFormat())) {
                    //克隆一份相同的文件
                    meetingExtManager.cloneOfficeFile(Long.valueOf(meeting.getContent()), meeting.getPeriodicityId(), meeting.getCreateDate());
                }
            } else if (!newVo.isNew()) {
                newVo.setIsSingleEdit(Boolean.TRUE);

                meeting.setCategory(MeetingCategoryEnum.single.key());
            }
        }

        //保存附件
        saveMeetingAttachment(newVo.getIsM3(), newVo.isNew(), meeting.getId(), newVo.getIsBatch() ? (meeting.getPeriodicityId() != null ? meeting.getPeriodicityId() : -1L) : -1l);

        meeting.setState(MeetingStateEnum.save.key());
        meeting.setRoom(Constants.GLOBAL_NULL_ID);
        meeting.setRoomState(RoomAppStateEnum.pass.key());
        //保存待发不保存视频会议室信息
        if (Strings.isNotBlank(meeting.getVideoMeetingId())) {
            meeting.setVideoMeetingId(null);
            meeting.setMeetingType(MeetingNatureEnum.normal.key());
        }

        this.meetingManager.saveOrUpdate(meeting);
        this.meetingExtManager.checkVideoMeetingData(newVo);


        //创建回执信息
        meetingManager.createReplies(meeting);

        //会议日志记录
        User actionUser = newVo.getCurrentUser();
        //修改管理员审核通过，后台日志里记录的会议发起人是会议室管理员
        if ((null == actionUser && null != meeting && null != meeting.getCreateUser())
                || (null != actionUser && !actionUser.getId().equals(meeting.getCreateUser()))) {
            V3xOrgMember memberById = orgManager.getMemberById(meeting.getCreateUser());
            actionUser = new User();
            actionUser.setId(memberById.getId());
            actionUser.setName(memberById.getName());
            actionUser.setLoginAccount(memberById.getOrgAccountId());
        }
        //定时任务没有登陆人员
        if (newVo.getCurrentUser() == null) {
            newVo.setCurrentUser(actionUser);
        }
        if (actionUser != null) {
            AppLogAction logAction = AppLogAction.Meeting_notice_new;
            if (!newVo.isNew()) {
                logAction = AppLogAction.Meeting_notice_update;
            }
            appLogManager.insertLog(actionUser, logAction, actionUser.getName(), newVo.getMeeting().getTitle());
        }

        return true;
    }

    //保存附件
    private void saveMeetingAttachment(boolean isM3, boolean isNew, Long meetingId, Long periodicityId) throws BusinessException {
        if (isM3) {
            meetingExtManager.saveMeetingAttachmentM3(isNew, meetingId);
        } else {
            meetingExtManager.saveMeetingAttachment(isNew, meetingId, periodicityId);
        }
    }

    /**
     * 发布会议
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transPublishMeeting(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = newVo.getMeeting();

        boolean isSaveToSend = false;
        if (newVo.getMeeting() != null && newVo.getMeeting().getState() != null && newVo.getMeeting().getState().equals(MeetingStateEnum.save.key())) {
            isSaveToSend = true;
        }
        boolean isRoomPerm = false;

        if (newVo.getOldMeeting() != null && MeetingHelper.isRoomWait(newVo.getOldMeeting().getRoomState())
                && newVo.getMeeting() != null && MeetingHelper.isRoomPass(newVo.getMeeting().getRoomState())) {
            isRoomPerm = true;
        }
        //通过会议时间设置会议状态
        MeetingNewHelper.setMeetingState(newVo);

        //创建回执信息
        List<MtReply> replies = meetingManager.createReplies(meeting);

        //创建affair
        List<CtpAffair> affairs = meetingManager.createAffairs(meeting);
        newVo.setAffairList(affairs);

        //处理会议参会人数量
        calMembers(meeting);

        //会议未结束
        if (MeetingStateEnum.finish.key() != meeting.getState()) {
            meetingQuartzJobManager.createMeetingQuartzJob(newVo);
        }

        /**
         * 发送视频会议
         */
        dealVideoMeeting(newVo);

        //更新会议
        this.meetingManager.saveOrUpdate(newVo.getMeeting());

        //记录项目的关联事项
        if (AppContext.hasPlugin("project")) {
            Long projectId = newVo.getMeeting().getProjectId();
            if (projectApi != null && !MeetingUtil.isIdNull(projectId)) {
                ProjectBO project = projectApi.getProject(projectId);
                if (project != null && project.getPhaseId() != 1) {
                    projectApi.saveProjectPhaseEvent(new ProjectPhaseEvent(ApplicationCategoryEnum.meeting.key(), meeting.getId(), project.getPhaseId()));
                }
            }
        }

        //会议日志记录
        User actionUser = newVo.getCurrentUser();
        //修改管理员审核通过，后台日志里记录的会议发起人是会议室管理员
        if ((null == actionUser && null != meeting && null != meeting.getCreateUser())
                || (null != actionUser && !actionUser.getId().equals(meeting.getCreateUser()))) {
            V3xOrgMember memberById = orgManager.getMemberById(meeting.getCreateUser());
            actionUser = new User();
            actionUser.setId(memberById.getId());
            actionUser.setName(memberById.getName());
            actionUser.setLoginAccount(memberById.getOrgAccountId());
        }
        //定时任务没有登陆人员
        if (newVo.getCurrentUser() == null) {
            newVo.setCurrentUser(actionUser);
        }

        if (actionUser != null) {
            AppLogAction logAction = AppLogAction.Meeting_notice_new;
            if (!newVo.isNew()) {
                logAction = AppLogAction.Meeting_notice_update;
            }
            appLogManager.insertLog(actionUser, logAction, actionUser.getName(), newVo.getMeeting().getTitle());
        }

        if (AppContext.hasPlugin("index")) {
            indexApi.update(meeting.getId(), ApplicationCategoryEnum.meeting.getKey());
        }

        //消息发送
        if (Strings.isNotEmpty(newVo.getAffairList())) {
            meetingMessageManager.sendMeetingMessage(newVo);
        }
        //判断会议是否冲突，当冲突的时候发送会议冲突提醒消息
        sendconfereesConflictMessage(newVo);

        //获取会议中没有电话号码的人员信息
        getNoPhoneNumberNames(newVo);

        //为日程触发事件
        if (AppContext.hasPlugin("timeView")) {
            MeetingBO mVo = DataTransUtil.trans2MeetingBO(newVo.getMeeting());
            List<Long> memberIds = new ArrayList<Long>();
            for (MtReply reply : replies) {
                if (MeetingConstant.MeetingReplyFeedbackFlagEnum.unattend.key() != reply.getFeedbackFlag()) {
                    memberIds.add(reply.getUserId());
                }
            }
            /**
             * OA-172151 代理人代理的会议 在会议主题空间日程安排里面显示，在个人空间日程安排里面不显示
             * 原因:待开会议查询时考虑了代理人的会议，因此在此处发布会议时逐个遍历与会人员找到其代理人并插入时间视图消息
             */
            List<Long> agentIds = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(memberIds)) {
                for (Long memberId : memberIds) {
                    List<AgentModel> agentToList = MemberAgentBean.getInstance().getAgentModelToList(memberId);
                    if (CollectionUtils.isNotEmpty(agentToList)) {
                        for (AgentModel am : agentToList) {
                            Long agentId = am.getAgentId();
                            if (!memberIds.contains(agentId)) {
                                agentIds.add(agentId);
                            }
                        }
                    }
                }
            }
            memberIds.removeAll(agentIds);
            memberIds.addAll(agentIds);
            newVo.setJoinAndPendingMember(memberIds);

            mVo.setJoinAndPendingMember(newVo.getJoinAndPendingMember());

            /*过期会议的发布不在此触发日程,后续在transFinishMeeting触发一次即可*/
            boolean isPassMeet = newVo.isNew() && "send".equals(newVo.getAction()) && newVo.getMeeting() != null && MeetingHelper.isFinished(newVo.getMeeting().getState());

            if (!isPassMeet && (newVo.isNew() || isSaveToSend || isRoomPerm || ("send".equals(newVo.getAction()) && !newVo.isNew()))) {
                MeetingUpdateEvent event = new MeetingUpdateEvent(this, mVo, meeting.getCreateUser(), new Date());
                EventDispatcher.fireEventAfterCommit(event);
            }
        }

        return true;
    }

    /**
     * 给冲突人员发送消息
     *
     * @param newVo
     * @throws BusinessException
     */
    private void sendconfereesConflictMessage(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = newVo.getMeeting();
        List<ConfereesConflictVO> confereesConflictVoList = confereesConflictManager.findConflictVOListForMessage(meeting);

        for (ConfereesConflictVO vo : confereesConflictVoList) {
            Map<String, Object> messageMap = new HashMap<String, Object>();
            messageMap.put("createUser", meeting.getCreateUser());
            messageMap.put("meetingId", meeting.getId());
            messageMap.put("title", meeting.getTitle());
            messageMap.put("title1", vo.getMtTitle());
            messageMap.put("memberId", vo.getId());
            meetingMessageManager.sendconfereesConflictMessage(messageMap);
        }
    }

    /**
     * 结束会议
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transFinishMeeting(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = newVo.getMeeting();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", StateEnum.col_done.key());

        affairManager.update(params, new Object[][]{{"objectId", meeting.getId()}});

        if (meeting.getState().intValue() == MeetingStateEnum.finish.key()
                || meeting.getState().intValue() == MeetingStateEnum.finish_advance.key()) {
            this.meetingManager.saveOrUpdate(meeting);
        }

        //更新待办状态为已办
        meetingReplyManager.updateState2Done(meeting.getId());

        //为日程触发事件
        if (AppContext.hasPlugin("timeView")) {
            List<Long> joinAndPendingMember = meetingManager.getJoinMeetingReplyMapExtImpart(meeting.getId(), meeting);
            MeetingBO mVo = DataTransUtil.trans2MeetingBO(meeting);
            mVo.setJoinAndPendingMember(joinAndPendingMember);
            MeetingUpdateEvent event = new MeetingUpdateEvent(this, mVo, meeting.getCreateUser(), new Date());
            EventDispatcher.fireEventAfterCommit(event);
        }

        //周期会议
        if (MeetingHelper.isPeriodicity(meeting.getCategory())) {
            this.transGenerateNextMeeting(newVo);
        } else {
            newVo.setMeeting(null);
        }

        return true;
    }

    /**
     * 生成新的会议(用于周期会议)
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transGenerateNextMeeting(MeetingNewVO newVo) throws BusinessException {
        MtMeeting nextMeeting = meetingPeriodicityManager.getNextMeetingByPeriodicity(newVo);
        newVo.setMeeting(nextMeeting);

        if (nextMeeting != null) {
            nextMeeting.setCategory(MeetingCategoryEnum.periodicity.key());
            nextMeeting.setCreateDate(newVo.getSystemNowDatetime());
            nextMeeting.setIdIfNew();
            nextMeeting.setRecordState(RecordStateEnum.no.key());

            /** 激活会议时，修改会议室申请记录 */
            MeetingRoomAppVO appVo = newVo.getMeetingRoomAppVO();
            if (appVo == null) {
                appVo = new MeetingRoomAppVO();
            }
            appVo.setMeeting(nextMeeting);
            appVo.setCurrentUser(newVo.getCurrentUser());

            this.meetingRoomManager.transGenerateNextRoomAppInMeeting(appVo);
            newVo.getMeeting().setRoomState(appVo.getStatus());

            MeetingNewHelper.setMeetingState(newVo);

            /** 保存会议类型使用记录 */
            meetingTypeRecordManager.saveMeetingTypeRecord(nextMeeting);

            nextMeeting.setRoom(appVo.getRoomId());
            nextMeeting.setRoomState(appVo.getStatus());

            //判断周期会议是否存在附件
            if (meetingExtManager.hasAttachment(nextMeeting.getPeriodicityId())) {
                nextMeeting.setHasAttachments(true);
                newVo.getMeeting().setHasAttachments(true);
                //生成附件信息
                meetingExtManager.saveMeetingAttachment(true, nextMeeting.getId(), nextMeeting.getPeriodicityId());
            }

            /**
             * 生成签到二维码和邀请二维码
             */
            meetingBarCodeManager.createMeetingBarCode(nextMeeting);
            meetingBarCodeManager.createMeetingInviteBarCode(nextMeeting);

            if (MeetingHelper.isRoomPass(nextMeeting.getRoomState())) {
                boolean result = transPublishMeeting(newVo);
                if (!result) {
                    return false;
                }
            } else {
                this.meetingManager.saveOrUpdate(nextMeeting);
            }
        }
        return true;
    }


    /**
     * 处理会议参会人的情况
     *
     * @param meeting
     * @throws BusinessException
     */
    private void calMembers(MtMeeting meeting) throws BusinessException {
        List<MtReply> allReplyList = meetingReplyManager.getReplyByMeetingId(meeting.getId());
        //计算会议各回执状态人数
        int allCount = 0, joinCount = 0, unjoinCount = 0, pendingCount = 0;
        for (MtReply reply : allReplyList) {
            allCount++;
            if (reply.getFeedbackFlag() == null) {
                continue;
            }
            if (reply.getFeedbackFlag() == MeetingConstant.MeetingReplyFeedbackFlagEnum.attend.key()) {
                joinCount++;
            } else if (reply.getFeedbackFlag() == MeetingConstant.MeetingReplyFeedbackFlagEnum.unattend.key()) {
                unjoinCount++;
            } else if (reply.getFeedbackFlag() == MeetingConstant.MeetingReplyFeedbackFlagEnum.pending.key()) {
                pendingCount++;
            }
        }
        meeting.setAllCount(allCount);
        meeting.setJoinCount(joinCount);
        meeting.setUnjoinCount(unjoinCount);
        meeting.setPendingCount(pendingCount);
    }

    /**
     * 保存会议模板
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transSaveAsTemplate(MeetingNewVO newVo) throws BusinessException {
        User currentUser = newVo.getCurrentUser();
        boolean isNew = true;
        MeetingNewHelper.setMeetingByVo(newVo);
        //判断模板是否存在
        String templateName = newVo.getMeeting().getTitle();
        Long userId = currentUser.getId();
        //获取会议模板，过滤掉周期会议生成的模板
        List<MeetingTemplate> result = meetingTemplateDao.getTemplateByName(templateName, userId);
        //模板存在时，为template赋值
        if (Strings.isNotEmpty(result) && result.size() > 0) {
            newVo.setTemplate(result.get(0));
            isNew = false;
        }

        MeetingNewHelper.setTemplateByMeeting(newVo);

        MeetingTemplate template = newVo.getTemplate();

        ReplaceBase64Result base64Result = fileManager.replaceBase64Image(template.getContent());
        template.setContent(base64Result.getHtml());

        template.setCreateUser(currentUser.getId());
        template.setAccountId(currentUser.getAccountId());
        template.setCreateDate(newVo.getSystemNowDatetime());
        template.setUpdateDate(newVo.getSystemNowDatetime());
        template.setTemplateId(newVo.getContentTemplateId());
        template.setIdIfNew();
        //周期会议过滤掉周期会议生成的模板时，模板不存在时，新生成模板保存。（不然会覆盖掉周期会议模板）
        if (Strings.isEmpty(result) && result.size() == 0) {
            template.setId(UUIDLong.longUUID());
        }
        template.setPeriodicityId(null);

        //保存会议用品
        saveTemplateResources(template);

        this.meetingExtManager.saveOrUpdateTemplate(template);

        //保存附件
        saveMeetingAttachment(false, isNew, template.getId(), -1l);
        return true;
    }

    /**
     * 保存会议模版的会议用品
     *
     * @param template
     * @throws BusinessException
     */
    public void saveTemplateResources(MeetingTemplate template) throws BusinessException {
        meetingResourcesDao.deleteMeetingResources(template.getId());
        String resourceIds = template.getResourcesId();
        if (Strings.isNotBlank(resourceIds)) {
            String[] ids = resourceIds.split(",");
            List<MeetingResources> resourcesList = new ArrayList<MeetingResources>();
            for (String id : ids) {
                MeetingResources res = new MeetingResources();
                res.setNewId();
                res.setBeginDate(template.getBeginDate());
                res.setEndDate(template.getEndDate());
                res.setMeetingId(template.getId());
                res.setResourceId(Long.valueOf(id));
                res.setReserveFlag(false);
                res.setUserId(template.getCreateUser());
                resourcesList.add(res);
            }
            meetingResourcesDao.saveMeetingResources(resourcesList);
        }
    }

    /**
     * 新建/编辑会议纪要
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean newSummary(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = null;
        MeetingSummary summary = new MeetingSummary();
        if (!newVo.isSummaryNew()) {
            summary = meetingSummaryManager.getSummaryById(newVo.getSummaryId());
            if (summary == null) {
                return false;
            }
            meeting = meetingManager.getMeetingById(summary.getMeetingId());
        } else if (!newVo.isNew()) {
            meeting = meetingManager.getMeetingById(newVo.getMeetingId());
            if (!MeetingUtil.isIdNull(meeting.getRecordId())) {
                summary = meetingSummaryManager.getSummaryById(meeting.getRecordId());
            } else {
                summary.setDataFormat(Constants.EDITOR_TYPE_HTML);
            }
        }

        if (meeting == null) {
            return false;
        }

        newVo.setSummary(summary);
        newVo.setMeeting(meeting);

        User currentUser = newVo.getCurrentUser();

        Long recorderId = meeting.getRecorderId();//记录人id
        Long emceeId = meeting.getEmceeId();//主持人id
        Long curruntUserId = currentUser.getId();//当前登录人id
        //如果有记录人，则只有记录人可以记录会议纪要
        if (null != recorderId && !Long.valueOf(-1).equals(recorderId)) {
            if (!curruntUserId.equals(recorderId)) {
                return false;
            }
        } else {//没有记录人，主持人可以记录会议纪要；
            if (!curruntUserId.equals(emceeId)) {
                return false;
            }
        }

        Set<Long> userIdList = new HashSet<Long>();

        MeetingNewHelper.addToList(userIdList, newVo.getCreateUserIdList(), meeting.getCreateUser());
        MeetingNewHelper.addToList(userIdList, newVo.getEmceeIdList(), meeting.getEmceeId());
        MeetingNewHelper.addToList(userIdList, newVo.getRecorderIdList(), meeting.getRecorderId());
        newVo.getEmceeList().add(orgManager.getMemberById(meeting.getEmceeId()));


        if (!MeetingUtil.isIdNull(meeting.getRecorderId())) {
            newVo.getRecorderList().add(orgManager.getMemberById(meeting.getRecorderId()));
        }
        if (Strings.isNotBlank(meeting.getConferees())) {
            newVo.getConfereeList().addAll(orgManager.getEntities(meeting.getConferees()));
        }
        if (Strings.isNotBlank(meeting.getImpart())) {
            newVo.getImpartList().addAll(orgManager.getEntities(meeting.getImpart()));
        }

        if (summary.isNew()) {
            newVo.getScopeList().addAll(meetingSummaryManager.findRealConfereesList(meeting.getId()));

            StringBuilder buffer = new StringBuilder();
            if (Strings.isNotEmpty(newVo.getScopeList())) {
                for (V3xOrgEntity entity : newVo.getScopeList()) {
                    if (Strings.isNotBlank(buffer.toString())) {
                        buffer.append(",");
                    }
                    buffer.append(entity.getEntityType());
                    buffer.append("|");
                    buffer.append(entity.getId());
                }
            }
            summary.setConferees(buffer.toString());

        } else {
            newVo.getScopeList().addAll(orgManager.getEntities(summary.getConferees()));
        }
        //实际与会人过滤掉主持人和记录人
        newVo.getScopeList().remove(orgManager.getMemberById(meeting.getEmceeId()));
        if (!MeetingUtil.isIdNull(meeting.getRecorderId())) {
            newVo.getScopeList().remove(orgManager.getMemberById(meeting.getRecorderId()));
        }
        meeting.setRoomName(meetingRoomManager.getRoomNameById(meeting.getRoom(), meeting.getMeetPlace()));
        meeting.setMeetingTypeName(meetingTypeManager.getMeetingTypeNameById(meeting.getMeetingTypeId()));

        return true;
    }

    /**
     * 发送会议纪要
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transSendSummary(MeetingNewVO newVo) throws BusinessException {

        transSaveSummary(newVo);

        User currentUser = newVo.getCurrentUser();

        MeetingSummary summary = newVo.getSummary();

        meetingSummaryManager.saveMeetingSummaryScope(newVo);

        List<Long> memberIdList = meetingSummaryManager.getScopePeople(newVo.getMeeting(), summary.getId());

        Boolean idEdit = false;
        //编辑会议纪要
        if ("listMeetingSummary".equals(newVo.getParameterMap().get("listType"))) {
            idEdit = true;
        }
        Map<String, Object> messageMap = new HashMap<String, Object>();
        messageMap.put("memberIdList", memberIdList);
        messageMap.put("createUser", currentUser.getId());
        messageMap.put("summaryId", summary.getId());
        messageMap.put("meetingId", newVo.getMeeting().getId());
        messageMap.put("title", summary.getMtName());
        messageMap.put("isEdit", idEdit);
        meetingMessageManager.sendSummaryMessage(messageMap);
        //日志
        AppLogAction logAction = AppLogAction.Meeting_summary_new;
        if (!newVo.isSummaryNew()) {
            logAction = AppLogAction.Meeting_summary_modify;
        }
        appLogManager.insertLog(currentUser, logAction, currentUser.getName(), summary.getMtName());

        //更新全文检索，吧纪要的正文检索进会议里
        if (AppContext.hasPlugin("index")) {
            indexApi.update(summary.getMeetingId(), ApplicationCategoryEnum.meeting.getKey());
        }

        return true;
    }

    /**
     * 保存会议纪要
     *
     * @param newVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transSaveSummary(MeetingNewVO newVo) throws BusinessException {
        MtMeeting meeting = null;
        MeetingSummary summary = new MeetingSummary();
        if (!newVo.isSummaryNew()) {
            MeetingSummary oldSummary = meetingSummaryManager.getSummaryById(newVo.getSummaryId());
            if (oldSummary == null) {
                return false;
            }
            summary = oldSummary;
            meeting = meetingManager.getMeetingById(summary.getMeetingId());
        } else if (!newVo.isNew()) {
            meeting = meetingManager.getMeetingById(newVo.getMeetingId());
            if (!MeetingUtil.isIdNull(meeting.getRecordId())) {
                summary = meetingSummaryManager.getSummaryById(meeting.getRecordId());
            }
        }

        newVo.setSummary(summary);
        newVo.setMeeting(meeting);

        User currentUser = newVo.getCurrentUser();

        MeetingNewHelper.setSummaryByVo(newVo);
        MeetingNewHelper.setSummaryByMeeting(newVo);
        MeetingNewHelper.setSummaryState(newVo);

        summary.setCreateUser(currentUser.getId());
        summary.setUpdateUser(currentUser.getId());
        summary.setCreateDate(newVo.getSystemNowDatetime());
        summary.setUpdateDate(newVo.getSystemNowDatetime());
        summary.setDataFormat(newVo.getSummary().getDataFormat());
        summary.setAccountId(currentUser.getAccountId());
        summary.setConferees(newVo.getSummary().getConferees());
        summary.setMtTypeName(newVo.getSummary().getMtTypeName());
        summary.setIdIfNew();

        //老G6功能，现已放弃
        summary.setIsAudit(false);
        summary.setAuditors("");
        summary.setAuditNum(0);

        if ("save".equals(newVo.getAction())) {
            summary.setState(SummaryStateEnum.draft.key());
        } else {
            summary.setState(SummaryStateEnum.publish.key());
        }

        ReplaceBase64Result base64result = fileManager.replaceBase64Image(summary.getContent());
        summary.setContent(base64result.getHtml());

        meetingSummaryManager.saveOrUpdate(summary);

        if (meeting != null) {
            if ("send".equals(newVo.getAction())) {
                meeting.setRecordState(MeetingRecordStateEnum.yes.key());
            } else {
                meeting.setRecordState(MeetingRecordStateEnum.no.key());
            }
            meeting.setRecordId(summary.getId());
        }
        this.meetingManager.saveOrUpdate(meeting);

        this.meetingExtManager.saveMeetingAttachment(newVo.isSummaryNew(), summary.getId(), -1l);

        return true;
    }

    private void getNoPhoneNumberNames(MeetingNewVO newVo) throws BusinessException {
        if ("1".equals(String.valueOf(newVo.getMeeting().getIsSendTextMessages()))) {
            newVo.setNoPhoneNumberNames(meetingValidationManager.checkMemberPhoneNumber(newVo.getMeeting().getId()));
        }
    }

    /****************************** 依赖注入 **********************************/
    public void setMeetingManager(MeetingManager meetingManager) {
        this.meetingManager = meetingManager;
    }

    public void setMeetingRoomManager(MeetingRoomManager meetingRoomManager) {
        this.meetingRoomManager = meetingRoomManager;
    }

    public void setMeetingExtManager(MeetingExtManager meetingExtManager) {
        this.meetingExtManager = meetingExtManager;
    }

    public void setMeetingQuartzJobManager(MeetingQuartzJobManager meetingQuartzJobManager) {
        this.meetingQuartzJobManager = meetingQuartzJobManager;
    }

    public void setMeetingMessageManager(MeetingMessageManager meetingMessageManager) {
        this.meetingMessageManager = meetingMessageManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setMeetingResourcesManager(MeetingResourcesManager meetingResourcesManager) {
        this.meetingResourcesManager = meetingResourcesManager;
    }

    public void setMeetingPeriodicityManager(MeetingPeriodicityManager meetingPeriodicityManager) {
        this.meetingPeriodicityManager = meetingPeriodicityManager;
    }

    public void setMeetingSummaryManager(MeetingSummaryManager meetingSummaryManager) {
        this.meetingSummaryManager = meetingSummaryManager;
    }

    public void setMeetingTypeManager(MeetingTypeManager meetingTypeManager) {
        this.meetingTypeManager = meetingTypeManager;
    }

    public void setMeetingTypeRecordManager(MeetingTypeRecordManager meetingTypeRecordManager) {
        this.meetingTypeRecordManager = meetingTypeRecordManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }

    public void setMeetingTemplateDao(MeetingTemplateDao meetingTemplateDao) {
        this.meetingTemplateDao = meetingTemplateDao;
    }

    public ConfereesConflictManager getConfereesConflictManager() {
        return confereesConflictManager;
    }

    public void setConfereesConflictManager(ConfereesConflictManager confereesConflictManager) {
        this.confereesConflictManager = confereesConflictManager;
    }

    public ProjectApi getProjectApi() {
        return projectApi;
    }

    public void setProjectApi(ProjectApi projectApi) {
        this.projectApi = projectApi;
    }

    public void setMeetingValidationManager(MeetingValidationManager meetingValidationManager) {
        this.meetingValidationManager = meetingValidationManager;
    }

    public void setMeetingReplyManager(MeetingReplyManager meetingReplyManager) {
        this.meetingReplyManager = meetingReplyManager;
    }

    public void setMeetingApplicationHandler(MeetingApplicationHandler meetingApplicationHandler) {
        this.meetingApplicationHandler = meetingApplicationHandler;
    }

    public void setMeetingBarCodeManager(MeetingBarCodeManager meetingBarCodeManager) {
        this.meetingBarCodeManager = meetingBarCodeManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setMeetingResourcesDao(MeetingResourcesDao meetingResourcesDao) {
        this.meetingResourcesDao = meetingResourcesDao;
    }
}
