package com.seeyon.apps.meetingroom.manager;

import com.seeyon.apps.meeting.constants.MeetingConstant.*;
import com.seeyon.apps.meeting.event.MeetingAffairsAssignedEvent;
import com.seeyon.apps.meeting.manager.MeetingManager;
import com.seeyon.apps.meeting.manager.MeetingMessageManager;
import com.seeyon.apps.meeting.manager.MeetingPeriodicityManager;
import com.seeyon.apps.meeting.manager.MeetingValidationManager;
import com.seeyon.apps.meeting.po.MeetingPeriodicity;
import com.seeyon.apps.meeting.util.MeetingHelper;
import com.seeyon.apps.meeting.util.MeetingTransHelper;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meeting.vo.MeetingNewVO;
import com.seeyon.apps.meeting.vo.MeetingOptionListVO;
import com.seeyon.apps.meetingroom.dao.MeetingRoomAppDao;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.apps.meetingroom.po.MeetingRoomApp;
import com.seeyon.apps.meetingroom.po.MeetingRoomPerm;
import com.seeyon.apps.meetingroom.util.MeetingRoomAdminUtil;
import com.seeyon.apps.meetingroom.util.MeetingRoomHelper;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.meeting.domain.MtMeeting;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 唐桂林
 */
public class MeetingRoomAppManagerImpl implements MeetingRoomAppManager {

    private MeetingRoomAppDao meetingRoomAppDao;
    private MeetingRoomRecordManager meetingRoomRecordManager;
    private MeetingRoomManager meetingRoomManager;
    private MeetingRoomPermManager meetingRoomPermManager;
    private MeetingPeriodicityManager meetingPeriodicityManager;
    private MeetingManager meetingManager;
    private MeetingMessageManager meetingMessageManager;
    private MeetingValidationManager meetingValidationManager;
    private AffairManager affairManager;
    private AppLogManager appLogManager;

    /**
     * 申请会议室
     *
     * @param appVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transApp(MeetingRoomAppVO appVo) throws BusinessException {
        /** 1、处理参数(将时间等格式转换正确) */
        MeetingRoomHelper.dealParameters(appVo);

        /** 2、获取会议室 */
        MeetingRoom room = meetingRoomManager.getRoomById(appVo.getRoomId());
        appVo.setMeetingRoom(room);

        /** 3、校验会议室状态 */
        if (!meetingValidationManager.checkRoom(appVo.getMeetingRoom(), appVo.getMsg())) {
            return false;
        }

        /** 4、校验会议室时间内段是否已有会议室申请 */
        if (!meetingValidationManager.checkRoomUsed(appVo.getRoomId(), appVo.getStartDatetime(), appVo.getEndDatetime(), appVo.getMeetingId(), appVo.getRoomAppId())) {
            appVo.setMsg(ResourceUtil.getString("mr.alert.cannotapp"));
            return false;
        }

        /** 5、获取参数 */
        User currentUser = appVo.getCurrentUser();
        Integer appStatus = MeetingHelper.getRoomAppStatus(room, currentUser.getId());
        Long auditingId = MeetingHelper.getRoomAppAuditingId(appStatus, room.getNeedApp(), currentUser.getId());

        /** 6、生成会议室申请数据 */
        MeetingRoomApp roomApp = new MeetingRoomApp();
        roomApp.setRoomId(appVo.getRoomId());
        roomApp.setStartDatetime(appVo.getStartDatetime());
        roomApp.setEndDatetime(appVo.getEndDatetime());
        roomApp.setAppDatetime(appVo.getSystemNowDatetime());
        roomApp.setPerId(appVo.getPerId());
        roomApp.setDepartmentId(appVo.getDepartmentId());
        roomApp.setAccountId(appVo.getAccountId());
        roomApp.setDescription(appVo.getDescription());
        roomApp.setStatus(appStatus);
        roomApp.setUsedStatus(RoomAppUsedStateEnum.normal.key());
        roomApp.setAuditingId(auditingId);
        roomApp.setIdIfNew();
//		zhou
        roomApp.setSqrdh(null == appVo.getParameterMap().get("sqrdh") ? "" : appVo.getParameterMap().get("sqrdh"));
        String is = appVo.getParameterMap().get("sfygwhldcj");
        if (null != is && !"".equals(is)) {
            roomApp.setSfygwhldcj(Integer.parseInt(is));
        } else {
//            roomApp.setSfygwhldcj(null);
        }

        roomApp.setHcyq(null == appVo.getParameterMap().get("hcyq") ? "" : appVo.getParameterMap().get("hcyq"));
        roomApp.setLdid(null == appVo.getParameterMap().get("ldid") ? "" : appVo.getParameterMap().get("ldid"));
        roomApp.setLdname(null == appVo.getParameterMap().get("ldname") ? "" : appVo.getParameterMap().get("ldname"));
//		zhou

        if (roomApp.getPerId() == null) {
            roomApp.setPerId(currentUser.getId());
            roomApp.setDepartmentId(currentUser.getDepartmentId());
        }

        appVo.setMeetingRoomApp(roomApp);

        /** 7、保存会议室申请记录  */
        this.saveOrUpdate(appVo.getMeetingRoomApp());

        /** 8、会议申请通过，生成申请记录 */
        if (MeetingHelper.isRoomPass(appStatus)) {
            this.meetingRoomRecordManager.saveOrUpdate(appVo.getMeetingRoomApp());
        }

        /** 9、生成会议室审核数据  */
        if (room.getNeedApp().equals(RoomNeedAppEnum.yes.key())) {
            MeetingRoomPerm roomPerm = new MeetingRoomPerm();
            roomPerm.setIdIfNew();
            roomPerm.setRoomId(roomApp.getRoomId());
            roomPerm.setRoomAppId(roomApp.getId());
            roomPerm.setIsAllowed(roomApp.getStatus());
            roomPerm.setDelFlag(RoomPermDeleteEnum.no.key());
            if (MeetingHelper.isRoomPass(appStatus)) {
                roomPerm.setProDatetime(appVo.getSystemNowDatetime());
                roomPerm.setAuditingId(currentUser.getId());
                /** 13、申请成功提示：您提交的会议室申请已通过！*/
                appVo.setMsg(ResourceUtil.getString("mr.alert.success_p"));
            } else {//申请者不是会议室拥有者
                /** 13、申请成功提示：你申请的会议室已经提交成功，请等待会议管理人员的审核！*/
                appVo.setMsg(ResourceUtil.getString("mr.alert.success_w"));
            }
            appVo.setMeetingRoomPerm(roomPerm);

            /** 10、保存会议室审核记录  */
            meetingRoomPermManager.saveOrUpdate(appVo.getMeetingRoomPerm());

            /** 11、生成会议室审核待办数据  */
            createAffairs(appVo);

            /** 12、给会议室审核人发送申请消息  */
            meetingMessageManager.sendRoomAppMessage(appVo);
        } else {
            /** 13、申请成功提示：您提交的会议室申请已通过！*/
            appVo.setMsg(ResourceUtil.getString("mr.alert.success_p"));
            if (room.getNeedApp().equals(RoomNeedAppEnum.no_but_need_msg.key())) {
                meetingMessageManager.sendNoticeMessage(appVo);
            }
        }

        /** 14、会议室申请应用日志 */
        appLogManager.insertLog(currentUser, AppLogAction.MeetingRoom_app_new, currentUser.getName(), room.getName());

        return true;
    }

    /**
     * 单次会议-申请会议室
     *
     * @param appVo
     * @throws BusinessException
     */
    private void transAppInMeetingForOnce(MeetingRoomAppVO appVo) throws BusinessException {
        User currentUser = appVo.getCurrentUser();

        if (!appVo.isNew()) {
            /**
             * 会议室申请有变动（修改为已申请好的会议室)
             */
            boolean isRoomAppChanged = appVo.getOldRoomAppId() != null && !appVo.getOldRoomAppId().equals(appVo.getRoomAppId());
            if (isRoomAppChanged) {
                String roomName = meetingRoomManager.getRoomNameById(appVo.getOldRoomId(), "");
                this.deleteRoomApp(appVo.getOldRoomAppId());
                affairManager.deletePhysicalByObjectId(appVo.getOldRoomAppId());
                this.saveRoomApp(appVo.getMeetingRoomApp());

                String[] appLogParams = new String[2];
                appLogParams[0] = currentUser.getName();
                appLogParams[1] = roomName;
                appLogManager.insertLog(currentUser, AppLogAction.MeetingRoom_app_repeal, appLogParams);
            }

            /**
             * 会议室审核未通过
             */
            boolean isRoomAppNotPassed = appVo.getMeetingRoomApp().getStatus() == RoomAppStateEnum.notpass.key();
            if (isRoomAppNotPassed) {
                deleteRoomApp(appVo.getOldRoomAppId());
                MeetingRoomApp app = MeetingRoomHelper.getMeetingRoomAppByVO(appVo);
                appVo.setMeetingRoomApp(app);
                saveRoomApp(app);
                if (appVo.getMeetingRoom().getNeedApp() == RoomNeedAppEnum.yes.key()) {
                    meetingRoomPermManager.removeByAppId(appVo.getOldRoomAppId());
                    MeetingRoomPerm perm = MeetingTransHelper.getRoomPerm(app);
                    appVo.setMeetingRoomPerm(perm);
                    meetingRoomPermManager.saveOrUpdate(perm);
                    createAffairs(appVo);
                    meetingMessageManager.sendRoomAppMessage(appVo);
                }
            }


        } else {
            if (!appVo.isRoomNew()) {
                //保存会议室申请数据(包括会议室记录)
                this.saveRoomApp(appVo.getMeetingRoomApp());

                //保存会议室审核数据
                if (appVo.getMeetingRoomPerm() != null) {
                    meetingRoomPermManager.saveOrUpdate(appVo.getMeetingRoomPerm());

                    createAffairs(appVo);

                    meetingMessageManager.sendRoomAppMessage(appVo);
                }

                if (appVo.getMeetingRoomApp() != null) {
                    String[] appLogParams = new String[2];
                    appLogParams[0] = currentUser.getName();
                    appLogParams[1] = appVo.getMeetingRoom().getName();
                    appLogManager.insertLog(currentUser, AppLogAction.MeetingRoom_app_new, appLogParams);
                }
            }

            if (appVo.getRoomOldApp() != null) {
                // 由于OA-111492，不判断“本次会议修改-无会议室申请||会议室申请有变动”。只要有roomOldApp都删除掉之前的会议室申请
                this.deleteRoomApp(appVo.getRoomOldApp().getId());
                affairManager.deletePhysicalByObjectId(appVo.getRoomOldApp().getId());

                String[] appLogParams = new String[2];
                appLogParams[0] = currentUser.getName();
                appLogParams[1] = appVo.getMeetingRoom().getName();
                appLogManager.insertLog(currentUser, AppLogAction.MeetingRoom_app_repeal, appLogParams);
            }
        }

    }

    /**
     * 周期会议-申请会议室
     *
     * @param appVo
     * @throws BusinessException
     */
    private void transAppInMeetingForPeriodicity(MeetingRoomAppVO appVo) throws BusinessException {
        User currentUser = appVo.getCurrentUser();

        //已有会议室申请数据&&周期规律没变
        if (!appVo.isNew() && appVo.getIsNewMeetingPeriodicity()) {

            //周期会议-单次修改，取消会议室申请的周期ID
            if (appVo.getIsSingleEdit()) {

                meetingRoomAppDao.deleteRoomApp(appVo.getMeetingRoomApp().getId());

                //周期会议-单条编辑，当前会议室申请的ID改变，将Affair数据转给下一条周期会议室申请
                MeetingRoomApp cloneRoomApp = MeetingTransHelper.cloneRoomApp(appVo.getMeetingRoomApp());
                cloneRoomApp.setTemplateId(null);
                cloneRoomApp.setPeriodicityId(null);
                cloneRoomApp.setNewId();
                appVo.setMeetingRoomApp(cloneRoomApp);

                if (appVo.getMeetingRoomPerm() != null) {
                    appVo.getMeetingRoomPerm().setIsPeriodicity(MeetingCategoryEnum.single.key());
                    appVo.getMeetingRoomPerm().setRoomAppId(appVo.getMeetingRoomApp().getId());
                    meetingRoomPermManager.saveOrUpdate(appVo.getMeetingRoomPerm());

                    createAffairs(appVo);
                }

                transAppInMeetingForOnce(appVo);

            } else if (appVo.isCategoryPeriodicity()) { //周期会议批量修改,修改全部的会议室申请
                Long periodicityId = appVo.getMeeting().getPeriodicityId();
                List<MeetingRoomApp> updateMeetingRoomAppList = this.meetingRoomAppDao.getRoomAppIdByTemplateId(periodicityId);
                for (MeetingRoomApp updateMeetingRoomApp : updateMeetingRoomAppList) {
                    updateMeetingRoomApp.setDescription(appVo.getMeeting().getTitle());
                    //修改周期会议会议室申请
                    this.saveRoomApp(updateMeetingRoomApp);
                }

            }

        }

        //会议室申请界面-申请会议室
        else {

            //周期会议-单次修改
            if (!appVo.getIsBatch()) {

                //周期会议单次编辑时，会议室审核为单次
                if (appVo.getIsSingleEdit()) {
                    if (appVo.getMeetingRoomPerm() != null) {
                        appVo.getMeetingRoomPerm().setIsPeriodicity(MeetingCategoryEnum.single.key());
                    }
                }

                transAppInMeetingForOnce(appVo);

            } else {
                if (appVo.getRoomOldApp() == null && !MeetingUtil.isIdNull(appVo.getPeriodicityId())) {//兼容周期会议第一条会议室申请被撤销了
                    List<MeetingRoomApp> roomAppList = meetingRoomAppDao.getRoomAppListByPeriodicityId(appVo.getPeriodicityId());
                    if (Strings.isNotEmpty(roomAppList)) {
                        appVo.setRoomOldApp(roomAppList.get(0));
                    }
                }
                //要先删除再新增会议室申请数据。删除的时候是根据PeriodicityId查询然后删除的。如果删除再新增后面，会导致新增的数据也被删除了。
                if (appVo.getRoomOldApp() != null) {
                    // 由于OA-111492，不判断“本次会议修改-无会议室申请||会议室申请有变动”。只要有roomOldApp都删除掉之前的会议室申请
                    List<Long> deleteAppIdList = getDeleteRoomAppIdList(appVo);

                    // 编辑会议时，原修改的会议室申请撤销
                    if (Strings.isNotEmpty(deleteAppIdList)) {

                        this.deleteRoomApp(deleteAppIdList);

                        List<CtpAffair> affairList = affairManager.getAffairs(appVo.getRoomOldApp().getId());
                        for (CtpAffair affair : affairList) {
                            appVo.getAuditingIdList().add(affair.getMemberId());
                        }

                        String oldRoomName = meetingRoomManager.getRoomNameById(appVo.getRoomOldApp().getRoomId(), "");
                        Map<String, Object> messageMap = new HashMap<String, Object>();
                        messageMap.put("createUser", appVo.getRoomOldApp().getPerId());
                        messageMap.put("roomAppId", appVo.getRoomOldApp().getId());
                        messageMap.put("roomName", oldRoomName);
                        messageMap.put("memberIdList", appVo.getAuditingIdList());
                        meetingMessageManager.sendRoomAppCancelMessage(messageMap);

                        String[] delLogs = new String[2];
                        delLogs[0] = currentUser.getName();
                        delLogs[1] = oldRoomName;
                        appLogManager.insertLog(currentUser, AppLogAction.MeetingRoom_app_repeal, delLogs);

                        boolean isDeleteAffair = true;
                        if (appVo.getIsSingleEdit()) {
                            isDeleteAffair = false;
                        }
                        this.meetingRoomPermManager.deleteRoomPermByAppId(deleteAppIdList, isDeleteAffair);
                    }

                }
                if (!appVo.isRoomNew()) {
                    List<MeetingRoomPerm> saveRoomPermList = new ArrayList<MeetingRoomPerm>();
                    List<MeetingRoomApp> saveRoomAppList = new ArrayList<MeetingRoomApp>();

                    List<Date[]> periodicityDatesList = appVo.getPeriodicityDatesList();
                    if (Strings.isNotEmpty(periodicityDatesList)) {
                        Integer status = MeetingHelper.getRoomAppStatus(appVo.getMeetingRoom(), appVo.getCurrentUser().getId());
                        Long auditingId = null;
                        if (MeetingHelper.isRoomPass(status) && appVo.getRoomNeedApp()) {
                            auditingId = currentUser.getId();
                        }
                        for (int i = 0; i < periodicityDatesList.size(); i++) {
                            Long meetingId = null;
                            //除当有会议室生成meetingId外，其它的都不生成meetingId
                            if (i == 0) {
                                meetingId = appVo.getMeetingRoomApp().getMeetingId();
                            }
                            MeetingRoomApp roomApp = MeetingTransHelper.generateRoomApp(appVo.getMeetingRoomApp(), meetingId, appVo.getPeriodicityDatesList().get(i));
                            roomApp.setStatus(status);
                            roomApp.setAuditingId(auditingId);
                            saveRoomAppList.add(roomApp);
                        }
                        //因周期性会议生成时，id都重新设置了，所以vo里的app的id需要更新
                        appVo.setMeetingRoomApp(saveRoomAppList.get(0));
                    }

                    //新建会议时，申请了周期会议室
                    if (Strings.isNotEmpty(saveRoomAppList)) {
                        this.saveRoomApp(saveRoomAppList);

                        MeetingRoomPerm roomPerm = appVo.getMeetingRoomPerm();
                        if (roomPerm != null) {
                            for (MeetingRoomApp bean : saveRoomAppList) {
                                saveRoomPermList.add(MeetingTransHelper.getRoomPerm(bean));
                            }
                        }
                    }

                    //新建会议时，申请的周期性会议室需要审核
                    if (Strings.isNotEmpty(saveRoomPermList)) {
                        this.meetingRoomPermManager.saveRoomPerm(saveRoomPermList);

                        appVo.setRoomPermList(saveRoomPermList);
                        appVo.setMeetingRoomPerm(saveRoomPermList.get(0));

                        createAffairs(appVo);

                        meetingMessageManager.sendRoomAppMessage(appVo);
                    }

                    if (appVo.getMeetingRoomApp() != null) {
                        String[] appLogParams = new String[2];
                        appLogParams[0] = currentUser.getName();
                        appLogParams[1] = appVo.getMeetingRoom().getName();
                        appLogManager.insertLog(currentUser, AppLogAction.MeetingRoom_app_new, appLogParams);
                    }
                }

            }
        }
    }

    /**
     * 新建会议时申请会议室
     *
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transAppInMeeting(MeetingRoomAppVO appVo) throws BusinessException {
        if (!appVo.isNew()) {
            MeetingRoomApp updateRoomApp = this.getRoomAppById(appVo.getRoomAppId());
            updateRoomApp.setMeetingId(appVo.getMeetingId());
            updateRoomApp.setDescription(appVo.getMeeting().getTitle());
            appVo.setMeetingRoomApp(updateRoomApp);

            appVo.setRoomOldApp(MeetingTransHelper.cloneRoomApp(updateRoomApp));

            //有会议室则设置会议室对象
            appVo.setRoomId(updateRoomApp.getRoomId());
            appVo.setMeetingRoom(meetingRoomManager.getRoomById(appVo.getRoomId()));

            if (appVo.getMeetingRoom().getNeedApp() == RoomNeedAppEnum.yes.key()) {
                MeetingRoomPerm roomPerm = meetingRoomPermManager.getRoomPermByAppId(updateRoomApp.getId());
                if (roomPerm != null) {
                    roomPerm.setMeetingId(appVo.getMeetingId());
                    appVo.setMeetingRoomPerm(roomPerm);
                }
            }
        } else {
            //有会议室则设置会议室对象
            appVo.setMeetingRoom(meetingRoomManager.getRoomById(appVo.getRoomId()));

            MeetingRoomApp addRoomApp = this.getRoomAppByMeetingId(appVo.getMeetingId());
            appVo.setRoomOldApp(MeetingTransHelper.cloneRoomApp(addRoomApp));

            addRoomApp = MeetingRoomHelper.getMeetingRoomAppByVO(appVo);
            appVo.setMeetingRoomApp(addRoomApp);

            if (appVo.getRoomNeedApp()) {
                appVo.setMeetingRoomPerm(MeetingTransHelper.getRoomPerm(addRoomApp));
            }
        }
        //会议室申请有变动(单次编辑需要修改会议室申请的周期ID\周期规则有变动)
        if (appVo.isNew() || !appVo.getIsBatch() || appVo.getCheckRoomChangeFlag() || (appVo.getRoomOldApp() != null && appVo.getMeetingRoomApp() != null && !appVo.getMeetingRoomApp().getId().equals(appVo.getRoomOldApp().getId()))) {
            if (!appVo.isCategoryPeriodicity()) {
                transAppInMeetingForOnce(appVo);
            } else {
                transAppInMeetingForPeriodicity(appVo);
            }
            MeetingRoom room = appVo.getMeetingRoom();
            if (room.getNeedApp().equals(RoomNeedAppEnum.no_but_need_msg.key()) && appVo.isNew()) {
                meetingMessageManager.sendNoticeMessage(appVo);
            }
        }
        return true;
    }

    /**
     * 添加会议室申请用途
     *
     * @param appVo
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transAddRoomAppDesc(MeetingRoomAppVO appVo) throws BusinessException {
        if (!appVo.isNew()) {
            MeetingRoomApp roomApp = this.getRoomAppById(appVo.getRoomAppId());
            if (roomApp != null) {
                roomApp.setDescription(appVo.getDescription());
                this.meetingRoomAppDao.updateRoomApp(roomApp);
            }
        }
        return true;
    }

    /**
     * 撤销会议室申请
     *
     * @param parameterMap
     * @return
     * @throws BusinessException
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean transCancelRoomApp(Map<String, Object> parameterMap) throws BusinessException {
        boolean isBatch = parameterMap.get("isBatch") == null ? false : (Boolean) parameterMap.get("isBatch");
        User currentUser = (User) parameterMap.get("currentUser");
        List<Long> roomAppIdList = parameterMap.get("roomAppIdList") == null ? null : (List<Long>) parameterMap.get("roomAppIdList");
        MtMeeting meeting = (MtMeeting) parameterMap.get("meeting");

        List<MeetingRoomApp> roomAppList = null;
        List<Long> roomIdList = new ArrayList<Long>();
        List<Long> meetingIdList = new ArrayList<Long>();

        Long createUser = currentUser.getId();
        Long roomAppId = null;
        String roomName = "";

        if (isBatch) {//周期会议批量撤销
            MeetingPeriodicity periodicity = (MeetingPeriodicity) parameterMap.get("periodicity");
            if (periodicity != null) {
                roomAppList = meetingRoomAppDao.getRoomAppListByPeriodicityId(periodicity.getId());
                if (Strings.isNotEmpty(roomAppList)) {
                    roomAppIdList = new ArrayList<Long>();
                    for (MeetingRoomApp bean : roomAppList) {
                        roomIdList.add(bean.getRoomId());
                        roomAppIdList.add(bean.getId());
                    }
                }
            }
        } else {
            if (Strings.isNotEmpty(roomAppIdList)) {
                roomAppList = meetingRoomAppDao.getRoomAppListById(roomAppIdList);
            } else {
                roomAppList = new ArrayList<MeetingRoomApp>();
                roomAppIdList = new ArrayList<Long>();
                Long meetingId = (Long) parameterMap.get("meetingId");
                MeetingRoomApp thisRoomApp = meetingRoomAppDao.getRoomAppByMeetingId(meetingId);
                if (thisRoomApp != null) {
                    roomAppId = thisRoomApp.getId();

                    roomAppList.add(thisRoomApp);
                    roomAppIdList.add(thisRoomApp.getId());
                    roomIdList.add(thisRoomApp.getRoomId());

                    MeetingNewVO newVo = new MeetingNewVO();
                    newVo.setIsSingleEdit(true);

                    if (meeting != null) {
                        newVo.setOldMeeting(meeting);
                    } else {
                        newVo.setOldMeeting(meetingManager.getMeetingById(meetingId));
                    }

                    MtMeeting nextMeeting = meetingPeriodicityManager.getNextMeetingByPeriodicity(newVo);
                    if (nextMeeting != null) {
                        MeetingRoomAppVO appVo = new MeetingRoomAppVO();
                        appVo.setMeeting(nextMeeting);
                        appVo.setCurrentUser(currentUser);
                        MeetingRoomApp nextRoomApp = this.getNextRoomAppByPeriodicityId(nextMeeting.getPeriodicityId(), nextMeeting.getBeginDate());

                        if (nextRoomApp != null) {
                            meetingManager.updateAffairObjectId(nextRoomApp.getId(), roomAppId);

                            roomAppId = nextRoomApp.getId();

                            //周期会议单条编辑后，激活新的会议室申请，若该周期会议还有别的未审核的会议室申请，再发一条待审核消息
                            List<MeetingRoomApp> lastList = this.meetingRoomAppDao.getRoomAppListByPeriodicityId(nextMeeting.getPeriodicityId());
                            boolean isNeedSendMsg = false;
                            if (Strings.isNotEmpty(lastList)) {
                                for (MeetingRoomApp bean : lastList) {
                                    if (MeetingHelper.isRoomWait(bean.getStatus())) {
                                        isNeedSendMsg = true;
                                        break;
                                    }
                                }
                            }
                            if (isNeedSendMsg) {
                                appVo.setMeetingRoomApp(nextRoomApp);
                                appVo.setMeetingRoom(meetingRoomManager.getRoomById(nextRoomApp.getRoomId()));
                                appVo.getAuditingIdList().addAll(meetingManager.getAffairMemberIdList(nextRoomApp.getId()));
                                appVo.setStartDatetime(nextRoomApp.getStartDatetime());
                                appVo.setEndDatetime(nextRoomApp.getEndDatetime());

                                meetingMessageManager.sendRoomAppMessage(appVo);
                            }
                        }
                    }
                }
            }
        }

        if (Strings.isNotEmpty(roomAppList)) {

            if (Strings.isEmpty(roomIdList)) {
                for (MeetingRoomApp bean : roomAppList) {
                    roomIdList.add(bean.getRoomId());
                }
            }

            Map<Long, MeetingRoom> roomMap = meetingRoomManager.getRoomMap(roomIdList);

            List<String[]> delLogList = new ArrayList<String[]>(); // 批量写入日志用到的参数
            for (MeetingRoomApp bean : roomAppList) {
                String[] delLogs = new String[2];
                delLogs[0] = currentUser.getName();
                MeetingRoom room = roomMap.get(bean.getRoomId());
                delLogs[1] = room == null ? "" : room.getName();
                delLogList.add(delLogs);

                if (bean.getMeetingId() != null) {
                    meetingIdList.add(bean.getMeetingId());
                }

                createUser = bean.getPerId();
                roomName = room == null ? "" : room.getName();
            }
//zhou   这里执行批量撤销的时源码中只取了集合中的第一个元素来发送通知，不知道为啥这样做。不懂，在此加for循环遍历发送
            for (int i = 0; i < roomAppList.size(); i++) {
//                if (roomAppId == null) {
                roomAppId = roomAppList.get(i).getId();
//                }
                //给会议室管理员发送撤销消息
                List<Long> memberIdList = new ArrayList<Long>();
                List<CtpAffair> affairList = affairManager.getAffairs(roomAppId);
                for (CtpAffair affair : affairList) {
                    memberIdList.add(affair.getMemberId());
                }
                if (Strings.isNotEmpty(memberIdList)) {
                    Map<String, Object> messageMap = new HashMap<String, Object>();
                    messageMap.put("roomAppId", roomAppId);
                    messageMap.put("roomName", roomName);
                    messageMap.put("createUser", createUser);
                    messageMap.put("cancelContent", parameterMap.get("cancelContent"));
                    messageMap.put("memberIdList", memberIdList);
                    //        zhou 开发区撤销会议通知在消息体重添加会议具体时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    messageMap.put("startTime", sdf.format(roomAppList.get(i).getStartDatetime()));
                    messageMap.put("endTime", sdf.format(roomAppList.get(i).getEndDatetime()));
//                   zhou添加会议时间 end
                    if (createUser.longValue() != currentUser.getId().longValue()) {
                        if (memberIdList.contains(currentUser.getId())) {
                            memberIdList.add(createUser);
                            messageMap.put("toCreateUser", createUser);
                            messageMap.put("createUser", currentUser.getId());
                            messageMap.put("memberIdList", memberIdList);
                        }
                    }
                    meetingMessageManager.sendRoomAppCancelMessage(messageMap);
                }
                //删除管理员的待办
                affairManager.deletePhysicalByObjectId(roomAppId);
            }


            if (Strings.isNotEmpty(roomAppIdList)) {
                meetingRoomAppDao.deleteRoomApp(roomAppIdList);
                meetingRoomRecordManager.deleteByRoomAppId(roomAppIdList);
                meetingRoomPermManager.deleteRoomPermByAppId(roomAppIdList, true);
            }

            if (MeetingActionEnum.cancelRoomApp.name().equals(parameterMap.get("action"))) {
                if (Strings.isNotEmpty(meetingIdList)) {
                    meetingManager.transPassRoom(meetingIdList);
                }
            }

            appLogManager.insertLogs(currentUser, AppLogAction.MeetingRoom_app_repeal, delLogList);
        }
        return true;
    }

    /**
     * 提前结束会议室申请
     *
     * @param parameterMap
     * @return
     * @throws BusinessException
     */
    @Override
    public boolean transFinishAdvanceRoomApp(Map<String, Object> parameterMap) throws BusinessException {
        boolean isOkFinish = false;

        Date endDatetime = (Date) parameterMap.get("endDatetime");

        Long finishMeetingId = null;

        boolean finishRoomApp = true;
        MeetingRoomApp roomApp = null;

        if (MeetingActionEnum.finishRoomApp.name().equals(parameterMap.get("action"))) {
            Long roomAppId = (Long) parameterMap.get("roomAppId");
            roomApp = meetingRoomAppDao.getRoomAppById(roomAppId);
        } else {
            roomApp = meetingRoomAppDao.getRoomAppByMeetingId((Long) parameterMap.get("meetingId"));
        }

        //审核不通过、和待审核的不能提前结束会议室申请
        if (roomApp.getStatus() == RoomAppStateEnum.notpass.key() || roomApp.getStatus() == RoomAppStateEnum.wait.key()) {
            isOkFinish = false;
        } else {
            //未使用
            if (roomApp.getStartDatetime().after(endDatetime)) {
                finishRoomApp = false;
                this.transCancelRoomApp(parameterMap);
            }
            //已使用
            else if (roomApp.getEndDatetime().before(endDatetime)) {
                finishRoomApp = false;
            }

            if (finishRoomApp) {
                roomApp.setUsedStatus(RoomAppUsedStateEnum.finished_advance.key());
                roomApp.setEndDatetime(endDatetime);

                if (MeetingHelper.isRoomPass(roomApp.getStatus())) {
                    finishMeetingId = roomApp.getMeetingId();
                }
                this.saveOrUpdate(roomApp);

                isOkFinish = true;
            }

            // lib 提前结束会议室申请 2253
            if (roomApp.getRoomId() != null) {
                String roomName = meetingRoomManager.getRoomNameById(roomApp.getRoomId(), "");
                if (Strings.isNotBlank(roomName)) {
                    User currentUser = (User) parameterMap.get("currentUser");
                    appLogManager.insertLog(currentUser, 2253, currentUser.getName(), roomName);
                }
            }
            if (MeetingActionEnum.finishRoomApp.name().equals(parameterMap.get("action"))) {
                if (finishMeetingId != null) {
                    Boolean isContainMeeting = MeetingUtil.getBoolean(parameterMap, "isContainMeeting", Boolean.FALSE);
                    if (isContainMeeting) {
                        parameterMap.put("meetingId", finishMeetingId);
                        meetingManager.transFinishAdvanceMeeting(parameterMap);
                        isOkFinish = true;
                    }
                }
            }
        }

        return isOkFinish;
    }

    /**
     * 激活会议时修改会议室申请数据
     *
     * @return
     * @throws BusinessException
     */
    @Override
    public MeetingRoomApp transGenerateNextRoomAppInMeeting(MeetingRoomAppVO appVo) throws BusinessException {
        Long periodicityId = appVo.getMeeting().getPeriodicityId();
        Long meetingId = appVo.getMeeting().getId();
        Date beginDate = appVo.getMeeting().getBeginDate();

        MeetingRoomApp nextRoomApp = this.getNextRoomAppByPeriodicityId(periodicityId, beginDate);
        if (nextRoomApp != null) {
            nextRoomApp.setMeetingId(meetingId);
            this.saveOrUpdate(nextRoomApp);

            this.meetingRoomRecordManager.saveOrUpdate(nextRoomApp);

            //会议单次修改
            if (appVo.getIsSingleEdit()) {
                if (appVo.getRoomOldApp() != null) {
                    meetingManager.updateAffairObjectId(nextRoomApp.getId(), appVo.getRoomOldApp().getId());
                }
                //周期会议单条编辑后，激活新的会议室申请，若该周期会议还有别的未审核的会议室申请，再发一条待审核消息
                List<MeetingRoomApp> lastList = this.meetingRoomAppDao.getRoomAppListByPeriodicityId(periodicityId);
                boolean isNeedSendMsg = false;
                if (Strings.isNotEmpty(lastList)) {
                    for (MeetingRoomApp bean : lastList) {
                        if (MeetingHelper.isRoomWait(bean.getStatus())) {
                            isNeedSendMsg = true;
                            break;
                        }
                    }
                }
                if (isNeedSendMsg) {
                    appVo.setMeetingRoomApp(nextRoomApp);
                    appVo.setMeetingRoom(meetingRoomManager.getRoomById(nextRoomApp.getRoomId()));
                    appVo.getAuditingIdList().addAll(meetingManager.getAffairMemberIdList(nextRoomApp.getId()));

                    meetingMessageManager.sendRoomAppMessage(appVo);
                }
            }

            appVo.setRoomId(nextRoomApp.getRoomId());
            appVo.setStatus(nextRoomApp.getStatus());
        } else {
            appVo.setStatus(RoomAppStateEnum.pass.key());
        }
        return nextRoomApp;
    }

    /**
     * 保存会议室申请
     *
     * @param po
     * @throws BusinessException
     */
    @Override
    public void saveOrUpdate(MeetingRoomApp po) throws BusinessException {
        //本次会议耗时(单位分钟)
        Long timeDiff = (po.getEndDatetime().getTime() - po.getStartDatetime().getTime()) / 1000 / 60;
        po.setTimeDiff(timeDiff.intValue());
        this.meetingRoomAppDao.saveOrUpdate(po);
    }

    /**
     * 修改会议室申请
     *
     * @param po
     * @throws BusinessException
     */
    public void updateRoomApp(MeetingRoomApp po) throws BusinessException {
        //本次会议耗时(单位分钟)
        Long timeDiff = (po.getEndDatetime().getTime() - po.getStartDatetime().getTime()) / 1000 / 60;
        po.setTimeDiff(timeDiff.intValue());
        this.meetingRoomAppDao.updateRoomApp(po);
    }

    /**
     * 申请会议室(包括会议室申请记录)
     *
     * @throws BusinessException
     */
    private void saveRoomApp(MeetingRoomApp roomApp) throws BusinessException {
        if (roomApp != null) {
            if (!meetingValidationManager.checkRoomUsed(roomApp.getRoomId(), roomApp.getStartDatetime(), roomApp.getEndDatetime(), roomApp.getMeetingId(), roomApp.getId())) {
                throw new BusinessException(ResourceUtil.getString("mr.alert.cannotapp"));
            }

            this.saveOrUpdate(roomApp);

            if (MeetingHelper.isRoomPass(roomApp.getStatus())) {
                meetingRoomRecordManager.saveOrUpdate(roomApp);
            }
        }
    }

    /**
     * 批量申请会议室(包括会议室申请记录)
     *
     * @param poList
     * @throws BusinessException
     */
    private void saveRoomApp(List<MeetingRoomApp> poList) throws BusinessException {
        if (Strings.isNotEmpty(poList)) {
            for (MeetingRoomApp po : poList) {
                //本次会议耗时(单位分钟)
                Long timeDiff = (po.getEndDatetime().getTime() - po.getStartDatetime().getTime()) / 1000 / 60;
                po.setTimeDiff(timeDiff.intValue());
            }
            meetingRoomAppDao.saveRoomApp(poList);

            meetingRoomRecordManager.saveRoomRecord(poList);
        }
    }

    /**
     * 批量修改会议室申请
     *
     * @param poList
     * @throws BusinessException
     */
    @Override
    public void updateRoomApp(List<MeetingRoomApp> poList) throws BusinessException {
        if (Strings.isNotEmpty(poList)) {
            for (MeetingRoomApp po : poList) {
                //本次会议耗时(单位分钟)
                Long timeDiff = (po.getEndDatetime().getTime() - po.getStartDatetime().getTime()) / 1000 / 60;
                po.setTimeDiff(timeDiff.intValue());
            }
            meetingRoomAppDao.updateRoomApp(poList);
        }
    }

    /**
     * 批量删除会议室申请
     *
     * @throws BusinessException
     */
    private void deleteRoomApp(Long roomAppId) throws BusinessException {
        if (roomAppId != null) {
            meetingRoomAppDao.deleteRoomApp(roomAppId);

            meetingRoomRecordManager.deleteByRoomAppId(roomAppId);
        }
    }

    /**
     * 批量删除会议室申请
     *
     * @param idList
     * @throws BusinessException
     */
    private void deleteRoomApp(List<Long> idList) throws BusinessException {
        if (Strings.isNotEmpty(idList)) {
            meetingRoomAppDao.deleteRoomApp(idList);

            meetingRoomRecordManager.deleteByRoomAppId(idList);
        }
    }

    private List<Long> getDeleteRoomAppIdList(MeetingRoomAppVO appVo) throws BusinessException {
        List<Long> deleteAppIdList = new ArrayList<Long>();

        //删除原来被取消的会议室申请
        deleteAppIdList.add(appVo.getRoomOldApp().getId());

        //周期性会议，批量修改，还需要撤销所有之后的会议室申请
        if (appVo.getIsBatch() && appVo.getRoomOldApp().getPeriodicityId() != null) {
            List<MeetingRoomApp> periodictityRoomAppList = this.meetingRoomAppDao.getRoomAppListByPeriodicityId(appVo.getRoomOldApp().getPeriodicityId());
            if (Strings.isNotEmpty(periodictityRoomAppList)) {
                for (MeetingRoomApp bean : periodictityRoomAppList) {
                    deleteAppIdList.add(bean.getId());
                }
            }
        }

        return deleteAppIdList;
    }

    /**
     * 给会议室审批添加待办数据
     *
     * @throws BusinessException
     */
    private void createAffairs(MeetingRoomAppVO appVo) throws BusinessException {
        List<Long> auditingIdList = new ArrayList<Long>();

        //会议室申请就已审核，审核人生成已办数据
        if (!MeetingUtil.isIdNull(appVo.getMeetingRoomPerm().getAuditingId())) {
            auditingIdList.add(appVo.getMeetingRoomPerm().getAuditingId());
        }
        //审核人为会议室所有管理员
        else {
            auditingIdList.addAll(MeetingRoomAdminUtil.getRoomAdminIdList(appVo.getMeetingRoom()));
        }

        //给会议室审核生成待办数据
        if (Strings.isNotEmpty(auditingIdList)) {

            List<CtpAffair> affairList = new ArrayList<CtpAffair>();
            if (appVo.getMeetingRoomPerm() != null) {
                appVo.setRoomAppId(appVo.getMeetingRoomPerm().getRoomAppId());

                for (Long auditingId : auditingIdList) {
                    appVo.setAuditingId(auditingId);
                    affairList.add(MeetingRoomHelper.createNewAffair(appVo));
                }
            }

            if (Strings.isNotEmpty(affairList)) {
                affairManager.saveAffairs(affairList);
                if (affairList.get(0).getState().intValue() == StateEnum.col_pending.key()) {
                    appVo.setAuditingIdList(auditingIdList);
                }

                MeetingAffairsAssignedEvent event = new MeetingAffairsAssignedEvent(this);
                event.setAffairs(affairList);
                EventDispatcher.fireEventAfterCommit(event);
            }
        }
    }

    /**
     * 获取某人已申请的会议室申请
     *
     * @param userId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingOptionListVO> getRoomAppedNameListByUserId(Long userId) throws BusinessException {
        List<MeetingOptionListVO> appedNameList = new ArrayList<MeetingOptionListVO>();

        List<MeetingRoomApp> appedList = meetingRoomAppDao.getRoomAppedListByUserId(userId);
        if (Strings.isNotEmpty(appedList)) {
            Map<Long, MeetingRoom> roomMap = new HashMap<Long, MeetingRoom>();

            List<Long> roomIdList = new ArrayList<Long>();
            for (MeetingRoomApp bean : appedList) {
                if (!roomIdList.contains(bean.getRoomId())) {
                    roomIdList.add(bean.getRoomId());
                }
            }

            if (Strings.isNotEmpty(roomIdList)) {
                roomMap = this.meetingRoomManager.getRoomMap(roomIdList);
            }

            for (MeetingRoomApp bean : appedList) {
                MeetingRoom room = roomMap.get(bean.getRoomId());
                if (room != null) {
                    appedNameList.add(getMeetingOptionListVO(bean, room.getName()));
                }
            }
        }
        return appedNameList;
    }

    /**
     * 获取某会议室申请
     *
     * @param roomAppId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingOptionListVO> getRoomAppedNameByRoomAppId(Long roomAppId) throws BusinessException {
        List<MeetingOptionListVO> appedNameList = new ArrayList<MeetingOptionListVO>();
        MeetingRoomApp roomApp = this.meetingRoomAppDao.getRoomAppById(roomAppId);
        if (roomApp != null) {
            MeetingRoom room = meetingRoomManager.getRoomById(roomApp.getRoomId());
            if (room != null) {
                appedNameList.add(getMeetingOptionListVO(roomApp, room.getName()));
            }
        }
        return appedNameList;
    }

    /**
     * 获取多个会议室申请
     *
     * @param roomAppIdList
     * @return
     * @throws BusinessException
     */
    @Override
    public Map<Long, MeetingRoomApp> getRoomAppMap(List<Long> roomAppIdList) throws BusinessException {
        Map<Long, MeetingRoomApp> roomAppMap = new HashMap<Long, MeetingRoomApp>();
        if (Strings.isNotEmpty(roomAppIdList)) {
            List<MeetingRoomApp> list = this.meetingRoomAppDao.getRoomAppListById(roomAppIdList);
            for (MeetingRoomApp po : list) {
                roomAppMap.put(po.getId(), po);
            }
        }
        return roomAppMap;
    }

    /**
     * 获取多个会议室申请
     *
     * @param roomAppIdList
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getRoomAppListById(List<Long> roomAppIdList) throws BusinessException {
        if (Strings.isNotEmpty(roomAppIdList)) {
            return this.meetingRoomAppDao.getRoomAppListById(roomAppIdList);
        }
        return null;
    }

    /**
     * 获取单个会议室申请
     *
     * @param roomAppId
     * @return
     * @throws BusinessException
     */
    @Override
    public MeetingRoomApp getRoomAppById(Long roomAppId) throws BusinessException {
        return meetingRoomAppDao.getRoomAppById(roomAppId);
    }

    /**
     * 通过会议ID获取会议室申请
     *
     * @param meetingId
     * @return
     * @throws BusinessException
     */
    @Override
    public MeetingRoomApp getRoomAppByMeetingId(Long meetingId) throws BusinessException {
        return meetingRoomAppDao.getRoomAppByMeetingId(meetingId);
    }

    /**
     * 通会议室ID及会议ID获取会议室申请
     *
     * @param roomId
     * @param meetingId
     * @return
     * @throws BusinessException
     */
    @Override
    public MeetingRoomApp getRoomAppByRoomAndMeetingId(Long roomId, Long meetingId) throws BusinessException {
        return meetingRoomAppDao.getRoomAppByRoomAndMeetingId(roomId, meetingId);
    }

    /**
     * 获取某会议室审核的会议室申请
     *
     * @param roomId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getRoomAppWaitList(Long roomId) throws BusinessException {
        return this.meetingRoomAppDao.getRoomAppWaitList(roomId);
    }

    /**
     * 获取某会议室已使用的会议室申请
     *
     * @param roomIdList
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getUsedRoomAppList(List<Long> roomIdList) throws BusinessException {
        return meetingRoomAppDao.getUsedRoomAppList(roomIdList);
    }

    /**
     * 通过周期ID获取会议室申请
     *
     * @param periodicityId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getRoomAppListByPeriodicityId(Long periodicityId) throws BusinessException {
        return meetingRoomAppDao.getRoomAppListByPeriodicityId(periodicityId);
    }

    /**
     * 通过周期ID获取待审核的会议室申请
     *
     * @param periodicityId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getWaitRoomAppListByPeriodicityId(Long periodicityId) throws BusinessException {
        return meetingRoomAppDao.getWaitRoomAppListByPeriodicityId(periodicityId);
    }

    /**
     * 通过周期ID获取下一条会议室申请
     *
     * @param periodicityId
     * @param beginDate
     * @return
     * @throws BusinessException
     */
    @Override
    public MeetingRoomApp getNextRoomAppByPeriodicityId(Long periodicityId, Date beginDate) throws BusinessException {
        return meetingRoomAppDao.getNextRoomAppByPeriodicityId(periodicityId, beginDate);
    }

    /**
     * 获取某时间段占用的会议室申请
     *
     * @param datetime
     * @param roomIdList
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getUsedRoomAppListByDate(Date datetime, List<Long> roomIdList) throws BusinessException {
        return meetingRoomAppDao.getUsedRoomAppListByDate(datetime, roomIdList);
    }

    /**
     * 获取某时间段占用的会议室申请
     *
     * @param roomIdList
     * @param include    是否包含临界值
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getUsedRoomAppListByDate(Date startDate, Date endDate, List<Long> roomIdList, boolean include) throws BusinessException {
        return meetingRoomAppDao.getUsedRoomAppListByDate(startDate, endDate, roomIdList, include);
    }


    private MeetingOptionListVO getMeetingOptionListVO(MeetingRoomApp bean, String roomName) throws BusinessException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(Strings.toHTML(roomName));
        buffer.append("(");
        buffer.append(DateUtil.format(bean.getStartDatetime(), DateFormatEnum.yyyyMMddHHmm.key()));
        buffer.append(" -- ");
        buffer.append(DateUtil.format(bean.getEndDatetime(), DateFormatEnum.yyyyMMddHHmm.key()));
        buffer.append(")");

        MeetingOptionListVO listVo = new MeetingOptionListVO();
        listVo.setOptionId(bean.getRoomId());
        listVo.setOptionName(buffer.toString());
        listVo.setOption2Id(bean.getId());
        listVo.setBeginDate(bean.getStartDatetime());
        listVo.setEndDate(bean.getEndDatetime());
        return listVo;
    }

    /**
     * 通过会议室ID获取所有会议室申请
     *
     * @param roomId
     * @return
     * @throws BusinessException
     */
    @Override
    public List<MeetingRoomApp> getAllRoomAppByRoomId(Long roomId) throws BusinessException {
        return meetingRoomAppDao.getAllRoomAppByRoomId(roomId);
    }

    @Override
    public void findRoomAppByParam(Map<String, Object> params, FlipInfo flipInfo) throws BusinessException {
        meetingRoomAppDao.findRoomAppByParam(params, flipInfo);
    }

    @Override
    public List<MeetingRoomApp> findMeetingRoomApps(Map<String, Object> params) throws BusinessException {
        return meetingRoomAppDao.findMeetingRoomApps(params);
    }

    @Override
    public List<MeetingRoomApp> getPeriodicityExceptApps(MeetingRoomApp app) throws BusinessException {
        if (MeetingUtil.isIdNull(app.getPeriodicityId())) {
            return new ArrayList<MeetingRoomApp>();
        }
        return meetingRoomAppDao.getPeriodicityExceptApps(app.getId(), app.getPeriodicityId());
    }

    /****************************** 依赖注入 **********************************/
    public void setMeetingRoomAppDao(MeetingRoomAppDao meetingRoomAppDao) {
        this.meetingRoomAppDao = meetingRoomAppDao;
    }

    public void setMeetingRoomRecordManager(MeetingRoomRecordManager meetingRoomRecordManager) {
        this.meetingRoomRecordManager = meetingRoomRecordManager;
    }

    public void setMeetingRoomManager(MeetingRoomManager meetingRoomManager) {
        this.meetingRoomManager = meetingRoomManager;
    }

    public void setMeetingRoomPermManager(MeetingRoomPermManager meetingRoomPermManager) {
        this.meetingRoomPermManager = meetingRoomPermManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setMeetingMessageManager(MeetingMessageManager meetingMessageManager) {
        this.meetingMessageManager = meetingMessageManager;
    }

    public void setMeetingValidationManager(MeetingValidationManager meetingValidationManager) {
        this.meetingValidationManager = meetingValidationManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setMeetingManager(MeetingManager meetingManager) {
        this.meetingManager = meetingManager;
    }

    public MeetingPeriodicityManager getMeetingPeriodicityManager() {
        return meetingPeriodicityManager;
    }

    public void setMeetingPeriodicityManager(MeetingPeriodicityManager meetingPeriodicityManager) {
        this.meetingPeriodicityManager = meetingPeriodicityManager;
    }
}
