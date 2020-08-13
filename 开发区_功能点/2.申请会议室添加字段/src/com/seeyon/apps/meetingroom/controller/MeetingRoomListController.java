package com.seeyon.apps.meetingroom.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.meetingroom.manager.MeetingRoomListManager;
import com.seeyon.apps.meetingroom.manager.MeetingRoomManager;
import com.seeyon.apps.meetingroom.po.MeetingRoom;
import com.seeyon.apps.meetingroom.po.MeetingRoomRecord;
import com.seeyon.apps.meetingroom.util.MeetingRoomRoleUtil;
import com.seeyon.apps.meetingroom.vo.MeetingRoomListVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.CommonTools;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;

/**
 * @author 唐桂林
 */
public class MeetingRoomListController extends BaseController {

    private MeetingRoomListManager meetingRoomListManager;
    private MeetingRoomManager meetingRoomManager;
    private FileToExcelManager fileToExcelManager;

    /**
     * 会议室登记列表页面
     *
     * @param request
     * @param response
     * @return 转到listadd.jsp页面
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.MeetingRoomAdmin, Role_NAME.AccountAdministrator})
    public ModelAndView listAdd(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean isAdmin = MeetingRoomRoleUtil.isMeetingRoomAdminRole();
        boolean isAccountAdmin = MeetingRoomRoleUtil.isAdministrator();
        if (!isAdmin && !isAccountAdmin) {
            return refreshWorkspace();
        }
        // 不同的角色跳转不同的页面
        String viewName = "meetingroom/listadd";
        if (isAccountAdmin) {
            viewName = "meetingroom/listaddaccount";
        }
        ModelAndView mav = new ModelAndView(viewName);
        String selectCondition = request.getParameter("selectCondition");
        String name = request.getParameter("name");
        Integer status = Strings.isBlank(request.getParameter("status")) ? null : Integer.parseInt(request.getParameter("status"));
        String seatCountCondition = request.getParameter("seatCountCondition");
        String seatCountStr = request.getParameter("seatCount");

        User currentUser = AppContext.getCurrentUser();

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("status", status);
        conditionMap.put("selectCondition", selectCondition);
        conditionMap.put("name", name);
        conditionMap.put("seatCountCondition", seatCountCondition);
        conditionMap.put("seatCountStr", seatCountStr);
        conditionMap.put("userId", currentUser.getId());
        conditionMap.put("accountId", currentUser.getLoginAccount());

        // 单位管理员的查询项
        String mngdepIdCondition = request.getParameter("mngdepId");
        if (mngdepIdCondition != null && mngdepIdCondition.length() > 0) {
            String applyrange = request.getParameter("applyrange");
            mav.addObject("conditionValue", applyrange);
            conditionMap.put("mngdepId", mngdepIdCondition);
        }
        String adminname = request.getParameter("adminname");
        List<Long> madIds = MeetingRoomRoleUtil.getMeetingRoomAdminIdList(currentUser.getLoginAccount());
        if (Strings.isNotBlank(adminname) && null != madIds && !madIds.isEmpty()) {
            // 查询本单位下的所有会议室管理员,然后比对姓名
            List<V3xOrgMember> meetingRoomAdmins = MeetingRoomRoleUtil.getMeetingRoomAdminList(currentUser.getLoginAccount());
            String adminIds = "";
            for (V3xOrgMember member : meetingRoomAdmins) {
                if ((member.getName().equals(adminname) || member.getName().contains(adminname))
                        && madIds.contains(member.getId())) {
                    adminIds += member.getId() + ",";
                }
            }
            if (Strings.isNotBlank(adminIds)) {
                conditionMap.put("adminIds", adminIds.substring(0, adminIds.length() - 1));
            } else {
                conditionMap.put("adminIds", "null");
            }
            mav.addObject("conditionValue", adminname);
        }

        List<MeetingRoomListVO> voList = this.meetingRoomListManager.findRoomList(conditionMap);
        mav.addObject("list", CommonTools.pagenate(voList));

        if (Strings.isNotBlank(selectCondition)) {
            mav.addObject("selectCondition", selectCondition);
            if (Strings.isNotBlank(name)) {
                mav.addObject("conditionValue", name);
            } else if (status != null) {
                mav.addObject("conditionValue", status);
            } else if (Strings.isNotBlank(seatCountStr) && Strings.isNotBlank(seatCountCondition)) {
                mav.addObject("conditionValue", new String[]{seatCountStr, seatCountCondition});
            }
        }
        return mav;
    }

    public ModelAndView listMyApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meetingroom/listmyapp");

        // 右侧下拉查询参数
        String condition = request.getParameter("condition");
        //OA-113629在会议资源-预定撤销页面直接点击查询，报js
        if (Strings.isEmpty(condition)) {
            condition = null;
        }
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("userId", AppContext.currentUserId());
        conditionMap.put("condition", condition);
        conditionMap.put("textfield", textfield);
        conditionMap.put("textfield1", textfield1);

        List<MeetingRoomListVO> voList = this.meetingRoomListManager.findMyRoomAppList(conditionMap, null);

        mav.addObject("list", voList);
        mav.addObject("selectCondition", condition);
        mav.addObject("textfield", textfield);
        mav.addObject("textfield1", textfield1);
        mav.addObject("flag", request.getParameter("flag"));
        mav.addObject("isAdmin", MeetingRoomRoleUtil.isMeetingRoomAdminRole());
        mav.addObject("systemNowDatetime", DateUtil.currentDate());

        return mav;
    }


    /**
     * 会议室申请列表页面
     *
     * @param request
     * @param response
     * @return 转到listperm.jsp页面
     * @throws Exception
     */
    @CheckRoleAccess(roleTypes = {Role_NAME.MeetingRoomAdmin})
    public ModelAndView listPerm(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!MeetingRoomRoleUtil.isMeetingRoomAdminRole()) {
            return refreshWorkspace();
        }

        ModelAndView mav = new ModelAndView("meetingroom/listperm");

        // 右侧下拉查询参数
        String condition = request.getParameter("condition");
        String textfield = request.getParameter("textfield");
        String textfield1 = request.getParameter("textfield1");

        User currentUser = AppContext.getCurrentUser();

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("userId", currentUser.getId());
        conditionMap.put("condition", condition);
        conditionMap.put("textfield", textfield);
        conditionMap.put("textfield1", textfield1);

        List<MeetingRoomListVO> voList = this.meetingRoomListManager.findRoomPermList(conditionMap, null);
        mav.addObject("list", voList);

        mav.addObject("selectCondition", condition);
        mav.addObject("textfield", textfield);
        mav.addObject("textfield1", textfield1);

        mav.addObject("flag", request.getParameter("flag"));

        return mav;
    }

    /**
     * 统计列表页面
     *
     * @param request
     * @param response
     * @return 转到listtotal.jsp页面
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @CheckRoleAccess(roleTypes = Role_NAME.MeetingRoomAdmin)
    public ModelAndView listTotal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!MeetingRoomRoleUtil.isMeetingRoomAdminRole()) {
            return refreshWorkspace();
        }

        ModelAndView mav = new ModelAndView("meetingroom/listtotal");

        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("startDatetime", request.getParameter("startDatetime"));
        conditionMap.put("endDatetime", request.getParameter("endDatetime"));
        conditionMap.put("userId", AppContext.currentUserId());

        List list = this.meetingRoomListManager.findMyRoomStatList(conditionMap);

        mav.addObject("startDatetime", conditionMap.get("startDatetime"));
        mav.addObject("endDatetime", conditionMap.get("endDatetime"));
        mav.addObject("list", list);
        return mav;
    }

    /**
     * 统计结果导出
     *
     * @param request
     * @param response
     * @return null, 不跳转页面
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @CheckRoleAccess(roleTypes = {Role_NAME.MeetingRoomAdmin})
    public ModelAndView listTotalExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!MeetingRoomRoleUtil.isMeetingRoomAdminRole()) {
            return refreshWorkspace();
        }
        Map<String, Object> conditionMap = new HashMap<String, Object>();
        conditionMap.put("startDatetime", request.getParameter("startDatetime"));
        conditionMap.put("endDatetime", request.getParameter("endDatetime"));
        conditionMap.put("userId", AppContext.currentUserId());

        List list = this.meetingRoomListManager.findMyRoomStatList(conditionMap);

        String[] colNames = new String[4];
        colNames[0] = ResourceUtil.getString("mr.label.meetingroomname", new Object[0]);
        colNames[1] = ResourceUtil.getString("mr.label.nowmonth", new Object[0]);
        colNames[2] = ResourceUtil.getString("mr.label.total", new Object[0]);
        colNames[3] = ResourceUtil.getString("mr.label.from", new Object[0]) + conditionMap.get("startDatetime") + ResourceUtil.getString("mr.label.to", new Object[0]) + conditionMap.get("endDatetime");

        DataRecord dr = new DataRecord();
        dr.setColumnName(colNames);
        dr.setTitle(ResourceUtil.getString("mr.tab.meetingtotal", new Object[0]));
        dr.setSheetName(ResourceUtil.getString("mr.tab.meetingtotal", new Object[0]));

        if (Strings.isNotEmpty(list)) {
            List<Long> roomIdList = new ArrayList<Long>();
            for (int i = 0; i < list.size(); i++) {
                HashMap h = (HashMap) list.get(i);
                MeetingRoomRecord record = (MeetingRoomRecord) h.get("MeetingRoomRecord");
                roomIdList.add(record.getRoomId());
            }

            Map<Long, MeetingRoom> roomMap = meetingRoomManager.getRoomMap(roomIdList);

            DataRow[] datarow = new DataRow[list.size()];
            for (int i = 0; i < list.size(); i++) {
                HashMap h = (HashMap) list.get(i);
                MeetingRoomRecord record = (MeetingRoomRecord) h.get("MeetingRoomRecord");
                datarow[i] = new DataRow();
                datarow[i].addDataCell(roomMap.get(record.getRoomId()).getName(), 1);
                datarow[i].addDataCell(String.valueOf(h.get("MonthTotal")) + ResourceUtil.getString("mr.label.hour", new Object[0]), 1);
                datarow[i].addDataCell(String.valueOf(h.get("AllTotal")) + ResourceUtil.getString("mr.label.hour", new Object[0]), 1);
                datarow[i].addDataCell(String.valueOf(h.get("SectionTotal")) + ResourceUtil.getString("mr.label.hour", new Object[0]), 1);
            }
            dr.addDataRow(datarow);
        }
        this.fileToExcelManager.save(response, ResourceUtil.getString("mr.tab.meetingtotal", new Object[0]), new DataRecord[]{dr});
        return null;
    }

    /****************************** 依赖注入 **********************************/
    public void setMeetingRoomListManager(MeetingRoomListManager meetingRoomListManager) {
        this.meetingRoomListManager = meetingRoomListManager;
    }

    public void setMeetingRoomManager(MeetingRoomManager meetingRoomManager) {
        this.meetingRoomManager = meetingRoomManager;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

}
