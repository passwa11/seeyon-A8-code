package com.seeyon.apps.meetingroom.util;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingActionEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomAppUsedStateEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.RoomNeedAppEnum;
import com.seeyon.apps.meeting.util.MeetingHelper;
import com.seeyon.apps.meeting.vo.MeetingJsonVO;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.apps.meetingroom.po.MeetingRoomAcl;
import com.seeyon.apps.meetingroom.po.MeetingRoomApp;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomListVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgUnit;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import net.sf.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 唐桂林
 */
public class MeetingRoomHelper {
    private static final Log LOGGER = LogFactory.getLog(MeetingRoomHelper.class);

    public static String[] getMeetingRoomAcls(List<MeetingRoomAcl> aclList, OrgManager orgManager) throws BusinessException {
        String acl_ids = "";
        String acl_names = "";
        if (Strings.isNotEmpty(aclList)) {
            for (MeetingRoomAcl acl : aclList) {
                V3xOrgUnit unit = orgManager.getUnitById(acl.getEntityId());
                if (unit != null) {
                    if (Strings.isNotBlank(acl_ids)) {
                        acl_ids += ",";
                        acl_names += ",";
                    }
                    acl_ids += unit.getType() + "|" + unit.getId();
                    acl_names += unit.getName();
                }
            }
        }
        return new String[]{acl_ids, acl_names};
    }

    public static MeetingRoomVO convertToVO(MeetingRoomVO vo, MeetingRoom po) throws BusinessException {
        vo.setAccountId(po.getAccountId());
        vo.setAdmin(po.getAdmin());
        vo.setAttachment(po.getAttachment());
        vo.setContent(po.getContent());
        vo.setCreateDatetime(po.getCreateDatetime());
        vo.setDelFlag(po.getDelFlag());
        vo.setDescription(po.getDescription());
        vo.setEqdescription(po.getEqdescription());
        vo.setId(po.getId());
        vo.setImage(po.getImage());
        vo.setMngdepId(po.getMngdepId());
        vo.setModifyDatetime(po.getModifyDatetime());
        vo.setName(po.getName());
        vo.setNeedApp(po.getNeedApp());
        vo.setPerId(po.getPerId());
        vo.setPlace(po.getPlace());
        vo.setSeatCount(po.getSeatCount());
        vo.setStatus(po.getStatus());
        vo.setQrCodeApply(po.getQrCodeApply());
        return vo;
    }

    public static MeetingRoomApp getMeetingRoomAppByVO(MeetingRoomAppVO appVo) throws BusinessException {
        MeetingRoomApp roomApp = new MeetingRoomApp();
        roomApp.setStartDatetime(appVo.getStartDatetime());
        roomApp.setEndDatetime(appVo.getEndDatetime());
        roomApp.setDescription(appVo.getMeeting().getTitle());
        roomApp.setPeriodicityId(appVo.getPeriodicityId());
        roomApp.setTemplateId(appVo.getPeriodicityTemplateId());
        roomApp.setAppDatetime(appVo.getSystemNowDatetime());
        roomApp.setUsedStatus(RoomAppUsedStateEnum.normal.key());
        roomApp.setStatus(MeetingHelper.getRoomAppStatus(appVo.getMeetingRoom(), appVo.getCurrentUser().getId()));

        User currentUser = appVo.getCurrentUser();
        roomApp.setDepartmentId(currentUser.getDepartmentId());
        roomApp.setAccountId(currentUser.getLoginAccount());
        roomApp.setPerId(currentUser.getId());
        //自己申请自己的会议室 ——审核人
        if (MeetingHelper.isRoomPass(roomApp.getStatus()) && appVo.getRoomNeedApp()) {
            roomApp.setAuditingId(currentUser.getId());
        }

        roomApp.setRoomId(appVo.getRoomId());
        roomApp.setMeetingId(appVo.getMeetingId());
        roomApp.setIdIfNew();
        return roomApp;
    }

    public static String meetingroomToJson(List<MeetingRoom> list) throws BusinessException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < list.size(); i++) {
            MeetingRoom bean = list.get(i);
            if (i != 0) {
                buffer.append(",");
            }
            buffer.append("{");
            buffer.append("key:");
            buffer.append("\"" + bean.getId() + "\",");
            buffer.append("label:");
            buffer.append("\"");
            buffer.append("<div class='dhx_matrix_scell_title' style='height:61px;'>");
            buffer.append("<div style='display:inline-block; vertical-align:top; margin-top: -2px; width:10%;'> ");
            buffer.append("<input id='room_" + bean.getId() + "' name='autoSelectRoom' type='checkbox' onclick='selectMtRoom(this);'/></div>");
            buffer.append("<div style='display:inline-block; vertical-align:top; margin-top: -2px; width:90%;'> ");
            buffer.append("<p class='p-name' style='padding-top: 12px;padding-right:0px; line-height:15px;word-break: break-all;overflow:hidden;text-overflow: ellipsis;display: -webkit-box;-webkit-line-clamp: 2;-webkit-box-orient: vertical;'>");
            if (bean.getNeedApp() == RoomNeedAppEnum.yes.key()) {
                buffer.append("<img style='display:inline-block; vertical-align:top;' src='apps_res/plugin/meetingroom/codebase/imgs/clock_small.gif' title='" + ResourceUtil.getString("mt.label.needcheck") + "'/>");
            }
            buffer.append("<a href='#' style='vertical-align:top;word-wrap:break-word;' title='" + Strings.toHTML(bean.getName()) + "' onclick=showMTInfo('" + bean.getId() + "')>" + Strings.toHTML(bean.getName()) + "</a>");
            buffer.append("</p>");
            buffer.append(" <p style='line-height: 12px;padding-top: 6px;' onclick=showMTInfo('" + bean.getId() + "')>");
            buffer.append(ResourceUtil.getString("meeting.room.seatCount", bean.getSeatCount())).append("</p>");
            buffer.append("</div>");
            buffer.append("</div>");
            buffer.append("\"");
            buffer.append("}");
        }
        buffer.append("]");
        if (Strings.isNotEmpty(list)) {
            return buffer.toString();
        } else {
            return "1";
        }
    }

    // 获取已申请的会议室
    public static String meetingroomAppToJson(List<MeetingRoomApp> list, Long meetingId, Long userId, String action) {
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        List<MeetingJsonVO> jsonList = new ArrayList<MeetingJsonVO>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select (select name from org_member o where o.id=m.perid ) username,(select name from org_unit u where u.id=m.departmentid) deptname,startdatetime,enddatetime,description,sqrdh from meeting_room_app m where id=?";
        try {

            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            for (MeetingRoomApp bean : list) {
                MeetingJsonVO mtApp2JSON = new MeetingJsonVO();
                mtApp2JSON.setId(String.valueOf(bean.getId()));
                mtApp2JSON.setMtappid(String.valueOf(bean.getId()));
                mtApp2JSON.setUpmtid(String.valueOf(meetingId));
                mtApp2JSON.setMeetingid(String.valueOf(bean.getId()));
                mtApp2JSON.setSection_id(String.valueOf(bean.getRoomId()));
                Date startDatetime = bean.getStartDatetime();
                Date endDateTime = bean.getEndDatetime();
                mtApp2JSON.setStart_date(Datetimes.formatDatetime(startDatetime));
                mtApp2JSON.setEnd_date(Datetimes.formatDatetime(endDateTime));
                mtApp2JSON.setState(bean.getStatus());
                //mtApp2JSON.setText(mt.getDescription());//去掉会议的描述
                mtApp2JSON.setText_hid(Functions.toHTML(bean.getDescription()));
                mtApp2JSON.setTextColor("#ffffff");// 默认白色
                mtApp2JSON.setDescription(bean.getDescription());
                mtApp2JSON.setSqrdh(bean.getSqrdh());
//                zhou
                ps.setString(1, String.valueOf(bean.getId()));
                rs = ps.executeQuery();
                while (rs.next()) {
                    mtApp2JSON.setSqDeptname(rs.getString("deptname"));
                }
                V3xOrgMember vom = null;
                if (bean.getPerId() != null) {
                    mtApp2JSON.setPerId(bean.getPerId());
                    try {
                        vom = orgManager.getMemberById(bean.getPerId());
                        if (vom != null) {
                            mtApp2JSON.setCreateUserName(vom.getName());
                        }
                    } catch (BusinessException e) {
                        //LOGGER.error("", e);
                    }
                }
                if (userId.longValue() != bean.getPerId().longValue()) {
                    mtApp2JSON.setStatus(1);// 表示不是本人添加的会议
                } else if (userId.longValue() == bean.getPerId().longValue()) {
                    mtApp2JSON.setStatus(2);// 表示是本人添加的会议
                    if (bean.getStartDatetime().getTime() < new Date().getTime()) {//如果开始时间小于当前时间(会议已经开始)
                        mtApp2JSON.setTimeout(2);
                        if (bean.getEndDatetime().getTime() < new Date().getTime()) {//如果结束时间小于当前时间(会议已经结束)
                            mtApp2JSON.setTimeout(3);
                        }
                    } else {
                        mtApp2JSON.setTimeout(1);
                    }
                } else {
                    mtApp2JSON.setStatus(3);// 表示其他状态
                }
                //如果是新增的话,所有已申请的会议室都不显示"删除"图标 TODO OA-41878 二级栏目穿透 action=show
                if (meetingId != null && meetingId.longValue() != -1) {
                    if (action == null || "create".equals(action) || "show".equals(action)) {
                        mtApp2JSON.setStatus(3);// 表示其他状态
                    }
                }
                jsonList.add(mtApp2JSON);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }

                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sq) {
                sq.printStackTrace();
            }
        }

        return JSONArray.fromObject(JSONUtil.toJSONString(jsonList)).toString();
    }

    public static MeetingRoomListVO convertToListVO(MeetingRoomVO vo, MeetingRoom po) throws BusinessException {
		/*vo.setAccountId(po.getAccountId());
		vo.setAdmin(po.getAdmin());
		vo.setAttachment(po.getAttachment());
		vo.setContent(po.getContent());
		vo.setCreateDatetime(po.getCreateDatetime());
		vo.setDelFlag(po.getDelFlag());
		vo.setDescription(po.getDescription());
		vo.setEqdescription(po.getEqdescription());
		vo.setId(po.getId());
		vo.setImage(po.getImage());
		vo.setMngdepId(po.getMngdepId());
		vo.setModifyDatetime(po.getModifyDatetime());
		vo.setName(po.getName());
		vo.setNeedApp(po.getNeedApp());
		vo.setPerId(po.getPerId());
		vo.setPlace(po.getPlace());
		vo.setSeatCount(po.getSeatCount());
		vo.setStatus(po.getStatus());
		return vo;*/
        return null;
    }

    public static CtpAffair createNewAffair(MeetingRoomAppVO appVo) throws BusinessException {
        Integer state = StateEnum.col_pending.key();
        if (appVo.getMeetingRoomPerm() != null && appVo.getMeetingRoomPerm().getProDatetime() != null) {
            state = StateEnum.col_done.key();
        }

        CtpAffair affair = new CtpAffair();
        affair.setIdIfNew();
        affair.setTrack(TrackEnum.no.ordinal());
        affair.setDelete(Boolean.FALSE);
        affair.setState(state);
        affair.setSubState(SubStateEnum.col_normal.key());
        affair.setApp(ApplicationCategoryEnum.meetingroom.key());
        affair.setSubApp(ApplicationSubCategoryEnum.meetingRoomAudit.key());
        affair.setObjectId(appVo.getMeetingRoomPerm().getRoomAppId());
        affair.setSubject(appVo.getMeetingRoom().getName() + ResourceUtil.getString("mr.label.application"));
        affair.setCreateDate(appVo.getMeetingRoomApp().getAppDatetime());
        affair.setReceiveTime(appVo.getStartDatetime());
        affair.setCompleteTime(appVo.getEndDatetime());
        affair.setUpdateDate(appVo.getSystemNowDatetime());
        affair.setSenderId(appVo.getMeetingRoomApp().getPerId());
        affair.setMemberId(appVo.getAuditingId());
        affair.setObjectId(appVo.getRoomAppId());
        /**
         * 处理代理情况
         */
        AgentModel agentModel = MeetingHelper.getMeetingAgent(appVo.getAuditingId());
        Date beginDate = appVo.getStartDatetime();
        if (agentModel != null && agentModel.getEndDate().after(beginDate)) {
            affair.setProxyMemberId(agentModel.getAgentId());
        }
        return affair;
    }

    /**
     * 根据不同动作处理传参
     *
     * @param appVo
     */
    public static void dealParameters(MeetingRoomAppVO appVo) throws BusinessException {
        if (appVo.getParameterMap() == null) {
            return;
        }
        User user = AppContext.getCurrentUser();
        Map<String, String> parameterMap = appVo.getParameterMap();
        //申请会议室
        if (MeetingActionEnum.apply.name().equals(appVo.getAction())) {
            String roomId = parameterMap.get("roomId");
            String perId = parameterMap.get("perId");
            String departmentId = parameterMap.get("departmentId");
            String startDatetime = parameterMap.get("startDatetime");
            String endDatetime = parameterMap.get("endDatetime");

            if (startDatetime.length() < 17) {
                startDatetime += ":00";
            }
            if (endDatetime.length() < 17) {
                endDatetime += ":00";
            }

            appVo.setDescription(parameterMap.get("description"));
            appVo.setRoomId(Strings.isBlank(roomId) ? -1 : Long.parseLong(roomId));
            appVo.setPerId(Strings.isBlank(perId) ? user.getId() : Long.parseLong(perId));
            appVo.setDepartmentId(Strings.isBlank(departmentId) ? MeetingHelper.getMeetingOwnerDepartmentId(user) : Long.parseLong(departmentId));
            appVo.setAccountId(user.getLoginAccount());

            Date serviceStartTime = Datetimes.parse(startDatetime);
            appVo.setStartDatetime(serviceStartTime);
            Date serviceEndTime = Datetimes.parse(endDatetime);
            appVo.setEndDatetime(serviceEndTime);


        }
    }
}
