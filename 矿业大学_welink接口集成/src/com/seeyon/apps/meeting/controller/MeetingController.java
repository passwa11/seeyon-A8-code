package com.seeyon.apps.meeting.controller;

import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingActionEnum;
import com.seeyon.apps.meeting.constants.MeetingConstant.MeetingCategoryEnum;
import com.seeyon.apps.meeting.constants.MeetingListConstant.ListTypeEnum;
import com.seeyon.apps.meeting.constants.MeetingListConstant.MeetingListTypeEnum;
import com.seeyon.apps.meeting.constants.MeetingPathConstant;
import com.seeyon.apps.meeting.manager.*;
import com.seeyon.apps.meeting.po.MeetingSummary;
import com.seeyon.apps.meeting.po.MeetingTemplate;
import com.seeyon.apps.meeting.po.PublicResource;
import com.seeyon.apps.meeting.util.MeetingParamHelper;
import com.seeyon.apps.meeting.util.MeetingUtil;
import com.seeyon.apps.meeting.vo.ConfereesConflictVO;
import com.seeyon.apps.meeting.vo.MeetingNewVO;
import com.seeyon.apps.meetingroom.vo.MeetingRoomAppVO;
import com.seeyon.apps.performancereport.api.PerformanceReportApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.PendingManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.domain.ReplaceBase64Result;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.util.PortletPropertyContants.PropertyName;
import com.seeyon.ctp.rest.resources.MeetingReplyMemberVO;
import com.seeyon.ctp.util.*;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.meeting.domain.MtMeeting;
import com.seeyon.v3x.meeting.domain.MtReply;
import com.seeyon.v3x.meeting.domain.MtReplyWithAgentInfo;
import com.seeyon.v3x.meeting.manager.MtMeetingManager;
import com.seeyon.v3x.meeting.manager.MtReplyManager;
import com.seeyon.v3x.mobile.message.manager.MobileMessageManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author 唐桂林
 */
public class MeetingController extends BaseController {

    private static final Log LOGGER = LogFactory.getLog(MeetingController.class);

    private MeetingNewManager meetingNewManager;
    private MeetingManager meetingManager;
    private PublicResourceManager publicResourceManager;
    private MeetingTemplateManager meetingTemplateManager;
    private MeetingExtManager meetingExtManager;
    private ConfereesConflictManager confereesConflictManager;
    private PendingManager pendingManager;
    private MtMeetingManager mtMeetingManager;
    private AttachmentManager attachmentManager;
    private MeetingSummaryManager meetingSummaryManager;
    private FileToExcelManager fileToExcelManager;
    private MobileMessageManager mobileMessageManager;
    private OrgManager orgManager;
    private MtReplyManager replyManager;
    private MeetingValidationManager meetingValidationManager;
    private PortalApi portalApi;
    private PerformanceReportApi performanceReportApi;
    private MeetingSettingManager meetingSettingManager;
    private FileManager fileManager;

    public void setMeetingSettingManager(MeetingSettingManager meetingSettingManager) {
        this.meetingSettingManager = meetingSettingManager;
    }
/****************************** 会议数据加载 **********************************/

    /**
     * 创建会议
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_create_frame);
        if (!AppContext.getCurrentUser().hasResourceCode("F09_meetingArrange")) {
            StringBuilder sb = new StringBuilder();
            sb.append("alert('" + ResourceUtil.getString("meeting.permission.no") + "');");
            sb.append("parent.parent.parent.close();");
            super.rendJavaScript(response, sb.toString());
            return null;
        }
        Long id = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        Long projectId = Strings.isBlank(request.getParameter("projectId")) ? -1L : Long.parseLong(request.getParameter("projectId"));
        Long templateId = Strings.isBlank(request.getParameter("templateId")) ? -1L : Long.parseLong(request.getParameter("templateId"));
        Long contentTemplateId = Strings.isBlank(request.getParameter("contentTemplateId")) ? -1L : Long.parseLong(request.getParameter("contentTemplateId"));
        Long roomAppId = Strings.isBlank(request.getParameter("portalRoomAppId")) ? -1L : Long.parseLong(request.getParameter("portalRoomAppId"));
        Long affairId = Strings.isBlank(request.getParameter("affairId")) ? -1L : Long.parseLong(request.getParameter("affairId"));
        Long summaryId = Strings.isBlank(request.getParameter("summaryId")) ? -1L : Long.parseLong(request.getParameter("summaryId"));
        String appFrom = request.getParameter("collaborationFrom");
        String appName = request.getParameter("moduleTypeFlag");
        String from = request.getParameter("from");
        Boolean isBatch = MeetingUtil.getBoolean(request, "isBatch", false);
        String action = Strings.isBlank(request.getParameter("actionName")) ? "create" : request.getParameter("actionName");
        String time = request.getParameter("time");
        //since V7.1  (致信需求)新增URL参数conferee，用于默认回填参会人员，可传递人员id，多个以逗号分割
        String confereeIds = ReqUtil.getString(request, "conferee", null);

        MeetingNewVO newVo = new MeetingNewVO();
        try {
            newVo.setAction(action);
            newVo.setMeetingId(id);
            newVo.setProjectId(projectId);
            newVo.setTemplateId(templateId);
            newVo.setRoomAppId(roomAppId);
            newVo.setContentTemplateId(contentTemplateId);
            newVo.setAppSummaryId(summaryId);
            newVo.setAppAffairId(affairId);
            newVo.setAppFrom(appFrom);
            newVo.setAppName(appName);
            newVo.setCurrentUser(AppContext.getCurrentUser());
            newVo.setAttachmentList(attachmentManager.getAttachmentsFromRequestNotRelition(request));
            newVo.setParameterMap(MeetingParamHelper.getMeetingParameterByRequest(request, newVo.getAction()));
            newVo.setSystemNowDatetime(DateUtil.currentDate());
            newVo.setIsBatch(isBatch);
            //快速需求：如会议创建入口为时间视图新建的会议，创建时间取视图点击的时间
            if (time != null && !"undefined".equals(time) && !"".equals(time)) {
                if (isValidLong(time)) {
                    Date d = new Date();
                    d.setTime(Long.parseLong(time));
                    newVo.setSystemNowDatetime(d);
                } else {
                    newVo.setSystemNowDatetime(DateUtil.toDate(time));
                }
            } else {
                newVo.setSystemNowDatetime(DateUtil.currentDate());
            }
            //给致信提供：增加新建时传入会人员默认值
            if (Strings.isNotBlank(confereeIds)) {
                String[] conferees = confereeIds.split(",");
                for (String string : conferees) {
                    V3xOrgEntity entity = orgManager.getEntity(String.format("Member|%s", string));
                    if (entity != null) {
                        newVo.getConfereeList().add(entity);
                        newVo.getConfereeIdList().add(Long.valueOf(string));
                    }
                }
            }
            meetingNewManager.newMeeting(newVo);
        } catch (Exception e) {
            LOGGER.error("新建会议失败", e);
        }

        //会议地点是否可编辑
        boolean isMeetingPlaceInputAble = meetingSettingManager.isMeetingPlaceInputAble();

        //外部会议选项
        boolean enablePublicMeeting = orgManager.accessedByVisitor(ApplicationCategoryEnum.meeting.name(), AppContext.currentAccountId());
        mav.addObject("enablePublicMeeting", enablePublicMeeting);

        //从栏目中新建会议
        mav.addObject("openFrom", ParamUtil.getString(request.getParameterMap(), "openFrom"));
        mav.addObject("roomParam", ParamUtil.getString(request.getParameterMap(), "roomParam"));

        mav.addObject("newVo", newVo);
        mav.addObject("attachments", newVo.getAttachmentList());
        mav.addObject("portalRoomAppId", roomAppId);
        mav.addObject("from", from);
        mav.addObject("isMeetingPlaceInputAble", isMeetingPlaceInputAble);
        return mav;
    }

    /**
     * 判断字符串是否long类型
     */
    private boolean isValidLong(String str) {
        try {
            long _v = Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 编辑会议
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_create_frame);
        Long id = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        Long projectId = Strings.isBlank(request.getParameter("projectId")) ? -1L : Long.parseLong(request.getParameter("projectId"));
        Long templateId = Strings.isBlank(request.getParameter("templateId")) ? -1L : Long.parseLong(request.getParameter("templateId"));
        Long contentTemplateId = Strings.isBlank(request.getParameter("contentTemplateId")) ? -1L : Long.parseLong(request.getParameter("contentTemplateId"));
        Boolean isBatch = MeetingUtil.getBoolean(request, "isBatch", true);

        MeetingNewVO newVo = new MeetingNewVO();
        try {
            newVo.setAction("edit");
            newVo.setIsBatch(isBatch);
            newVo.setMeetingId(id);
            newVo.setProjectId(projectId);
            newVo.setTemplateId(templateId);
            newVo.setContentTemplateId(contentTemplateId);
            newVo.setCurrentUser(AppContext.getCurrentUser());
            newVo.setParameterMap(MeetingParamHelper.getMeetingParameterByRequest(request, newVo.getAction()));
            newVo.setSystemNowDatetime(DateUtil.currentDate());
            meetingNewManager.newMeeting(newVo);
        } catch (Exception e) {
            LOGGER.error("编辑会议报错", e);
        }
        //会议地点是否可编辑
        boolean isMeetingPlaceInputAble = meetingSettingManager.isMeetingPlaceInputAble();

        //外部会议选项
        boolean enablePublicMeeting = orgManager.accessedByVisitor(ApplicationCategoryEnum.meeting.name(), AppContext.currentAccountId());
        mav.addObject("enablePublicMeeting", enablePublicMeeting);

        mav.addObject("newVo", newVo);
        mav.addObject("isWaitSend", newVo.getMeeting().getState());
        mav.addObject("attachments", newVo.getAttachmentList());
        mav.addObject("isMeetingPlaceInputAble", isMeetingPlaceInputAble);

        return mav;
    }

    /****************************** 会议数据保存 **********************************/
    /**
     * 发送会议
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView send(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /******************************** 1 获取参数，组装bean **********************************/
        Long id = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        Long roomId = Strings.isBlank(request.getParameter("roomId")) ? -1L : Long.parseLong(request.getParameter("roomId"));
        Long roomAppId = Strings.isBlank(request.getParameter("roomAppId")) ? -1L : Long.parseLong(request.getParameter("roomAppId"));
        Integer category = Strings.isBlank(request.getParameter("category")) ? MeetingCategoryEnum.single.key() : Integer.parseInt(request.getParameter("category"));
        Boolean isBatch = MeetingUtil.getBoolean(request, "isBatch", false);

        MeetingNewVO newVo = new MeetingNewVO();
        newVo.setAction("send");
        newVo.setMeetingId(id);
        newVo.setRoomId(roomId);
        newVo.setRoomAppId(roomAppId);
        newVo.setCategory(category);
        newVo.setIsBatch(isBatch);
        newVo.setSelectRoomType(request.getParameter("selectRoomType"));
        newVo.setCurrentUser(AppContext.getCurrentUser());
        newVo.setParameterMap(MeetingParamHelper.getMeetingParameterByRequest(request, newVo.getAction()));
        newVo.setSystemNowDatetime(DateUtil.currentDate());
        String errorMsg = "";
        try {
            boolean result = meetingNewManager.transSend(newVo);
            if (!result) {
                errorMsg = newVo.getErrorMsg();
                super.rendJavaScript(response, "parent.gotoList('send', '" + errorMsg + "');");
                return null;
            }

            //选择发送短信，校验是否存在没电话的人员，需要前台给出提示
            if (Strings.isNotBlank(newVo.getNoPhoneNumberNames())) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>");
                out.println("alert('" + ResourceUtil.getString("meeting.message.calcel_meeting_send_sms_alert_info", Strings.toHTML(newVo.getNoPhoneNumberNames())) + "');");
                out.println("</script>");
                out.flush();
            }

        } catch (Exception e) {
            LOGGER.error("发送会议出错", e);

            errorMsg = newVo.getErrorMsg();
            if (Strings.isBlank(errorMsg)) {
                errorMsg = ResourceUtil.getString("meeting.send.error");
            } else {
                LOGGER.error("发送会议出错：" + errorMsg);
            }
            super.rendJavaScript(response, "parent.gotoList('send', '" + errorMsg + "');");
            return null;
        } finally {
            MeetingRoomAppVO appVo = newVo.getMeetingRoomAppVO();
            meetingValidationManager.clearMeetingRoomAppCache(appVo);
        }

        /******************************** 12 跳转页面 **********************************/
        super.rendJavaScript(response, "parent.gotoList('send');");
        return null;
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
        /******************************** 1 获取参数，组装bean **********************************/
        Long id = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));
        Long roomId = Strings.isBlank(request.getParameter("roomId")) ? -1L : Long.parseLong(request.getParameter("roomId"));
        Long roomAppId = Strings.isBlank(request.getParameter("roomAppId")) ? -1L : Long.parseLong(request.getParameter("roomAppId"));
        Integer category = Strings.isBlank(request.getParameter("category")) ? MeetingCategoryEnum.single.key() : Integer.parseInt(request.getParameter("category"));
        Boolean isBatch = MeetingUtil.getBoolean(request, "isBatch", true);

        MeetingNewVO newVo = new MeetingNewVO();
        newVo.setAction("save");
        newVo.setMeetingId(id);
        newVo.setRoomId(roomId);
        newVo.setRoomAppId(roomAppId);
        newVo.setCategory(category);
        newVo.setIsBatch(isBatch);
        newVo.setSelectRoomType(request.getParameter("selectRoomType"));
        newVo.setCurrentUser(AppContext.getCurrentUser());
        newVo.setParameterMap(MeetingParamHelper.getMeetingParameterByRequest(request, newVo.getAction()));
        newVo.setSystemNowDatetime(DateUtil.currentDate());
        try {
            meetingNewManager.transSave(newVo);
        } catch (Exception e) {
            LOGGER.error("发送会议报错", e);
        }
        super.rendJavaScript(response, "parent.gotoList('save');");
        return null;
    }

    /**
     * 保存会议为个人模板
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveAsTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = Strings.isBlank(request.getParameter("id")) ? -1L : Long.parseLong(request.getParameter("id"));

        MeetingNewVO newVo = new MeetingNewVO();
        newVo.setMeetingId(id);
        newVo.setAction(request.getParameter("operation"));
        newVo.setCurrentUser(AppContext.getCurrentUser());
        newVo.setParameterMap(MeetingParamHelper.getMeetingParameterByRequest(request, newVo.getAction()));
        newVo.setSystemNowDatetime(DateUtil.currentDate());
        try {
            meetingNewManager.transSaveAsTemplate(newVo);
        } catch (Exception e) {
            LOGGER.error("保存会议个人模板报错", e);
        }
        super.rendJavaScript(response, "parent.enableBtnAndPrintMsg();");
        return null;
    }

    /****************************** 打开弹出框 **********************************/
    /**
     * 打开会议周期性设置弹出框
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView openPeriodicityDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_periodicity_dialog);
        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? -1L : Long.parseLong(request.getParameter("meetingId"));
        Long periodicityId = Strings.isBlank(request.getParameter("periodicityId")) ? -1L : Long.parseLong(request.getParameter("periodicityId"));

        MeetingNewVO newVo = new MeetingNewVO();
        newVo.setMeetingId(meetingId);
        newVo.setPeriodicityId(periodicityId);
        newVo.setSystemNowDatetime(DateUtil.currentDate());
        this.meetingNewManager.newPeriodicity(newVo);

        mav.addObject("newVo", newVo);

        //将现在的时间按照客户端的时区转换
        String now = Datetimes.format(new Date(), DateUtil.YEAR_MONTH_DAY_PATTERN);
        mav.addObject("todayDateValue", now);

        return mav;
    }

    /**
     * 打开手动输入会议地址弹出框
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView openAddressDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_address_dialog);
        mav.addObject("meetingPlace", request.getParameter("meetingPlace"));
        return mav;
    }

    /**
     * 打开会议用品选择框
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView openResourceDialog(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_resource_dialog);
        try {
            List<PublicResource> meetingResourceList = publicResourceManager.findResourceList(AppContext.currentAccountId());
            mav.addObject("resourceMap", meetingResourceList);
            String selectedResourceIds = request.getParameter("type");
            if (selectedResourceIds != null && !"".equals(selectedResourceIds.trim())) {
                mav.addObject("oldResourceIds", selectedResourceIds);
            }
        } catch (Exception e) {
            LOGGER.error("打开会议用品出错", e);
        }
        return mav;
    }

    /**
     * 打开会议议程
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView openPlanDialog(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView(MeetingPathConstant.meeting_plan_dialog);
    }

    /**
     * 打开会议注意事项
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView openNoticeDialog(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView(MeetingPathConstant.meeting_notice_dialog);
    }

    /**
     * 打开会议个人模板选择框
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView openTemplateDialog(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_template_dialog);

        try {
            //OA-170563 兼职人员在兼职单位建立的会议模板，调用时无数据，切换到本单位后数据出现
            List<MeetingTemplate> personTemplateList = meetingTemplateManager.getMyTemplateList(AppContext.currentUserId(), null);
            for (MeetingTemplate template : personTemplateList) {
                if (template.getTitle().length() > 10) {
                    template.setTitle(template.getTitle().substring(0, 10) + "...");
                }

                try {
                    // 此处是为了升级历史数据
                    ReplaceBase64Result result = fileManager.replaceBase64Image(template.getContent());
                    if (result.isConvertBase64Img()) {// 替换过正文内容才执行更新
                        template.setContent(result.getHtml());
                        DBAgent.update(template);
                    }
                } catch (Exception e) {// 查看时，如果转换失败就不转换了
                    logger.error("将正文中base64编码图片转为URL时发生异常！", e);
                }
            }
            mav.addObject("personTemplateList", personTemplateList);

        } catch (Exception e) {
            LOGGER.error("打开会议个人模板出错", e);
        }
        return mav;
    }

    /**
     * @param request
     * @param response
     * @return
     */
    public ModelAndView openEditDialog(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_template_dialog);

        return mav;
    }

    public ModelAndView openCancelDialog(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_cancel_dialog);
        try {
            mav.addObject("isCanSendSMS", meetingExtManager.isCanSendSMS());
        } catch (Exception e) {
            LOGGER.error("打开会议撤销页面出错", e);
            mav.addObject("isCanSendSMS", false);
        }
        return mav;
    }

    public ModelAndView openPeriodicityBatchDialog(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView(MeetingPathConstant.periodicity_batch_dialog);
    }

    public ModelAndView openMeetingRoomPromptDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meetingroom_prompt_dialog);
        mav.addObject("hasMeetingArrangeMenu", AppContext.getCurrentUser().hasResourceCode("F09_meetingArrange"));
        return mav;
    }

    public ModelAndView openMeetingConfereesRepeatDialog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.conferees_conflict_dialog);
        return mav;
    }

    public ModelAndView listConfereesConflict(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.conferees_conflict_list_frame);

        Long meetingId = Strings.isBlank(request.getParameter("meetingId")) ? Constants.GLOBAL_NULL_ID : Long.parseLong(request.getParameter("meetingId"));
        Long emceeId = Strings.isBlank(request.getParameter("emceeId")) ? Constants.GLOBAL_NULL_ID : Long.parseLong(request.getParameter("emceeId"));
        Long recorderId = Strings.isBlank(request.getParameter("recorderId")) ? Constants.GLOBAL_NULL_ID : Long.parseLong(request.getParameter("recorderId"));

        MtMeeting meeting = new MtMeeting();
        meeting.setMeetingId(meetingId);
        meeting.setCreateUser(AppContext.currentUserId());
        meeting.setEmceeId(emceeId);
        meeting.setRecorderId(recorderId);
        meeting.setConferees(request.getParameter("conferees"));
        meeting.setLeader(request.getParameter("leader"));
        meeting.setBeginDate(new Date(Long.parseLong(request.getParameter("beginDatetime"))));
        meeting.setEndDate(new Date(Long.parseLong(request.getParameter("endDatetime"))));
        List<ConfereesConflictVO> list = confereesConflictManager.findConflictVOListForShow(meeting);
        mav.addObject("list", list);
        return mav;
    }

    public ModelAndView showReplyCardDetail(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.meeting_reply_detail);
        long meetingId = Long.parseLong(ReqUtil.getString(request, "meetingId"));
        Map<Long, MtReply> replyMap = replyManager.findAllByMeetingId(meetingId);

        List<MeetingReplyMemberVO> joinList = new ArrayList<MeetingReplyMemberVO>();
        List<MeetingReplyMemberVO> notJoinList = new ArrayList<MeetingReplyMemberVO>();
        List<MeetingReplyMemberVO> waitJoinList = new ArrayList<MeetingReplyMemberVO>();
        List<MeetingReplyMemberVO> allList = new ArrayList<MeetingReplyMemberVO>();
        for (Long replyId : replyMap.keySet()) {
            MtReply reply = replyMap.get(replyId);

            MeetingReplyMemberVO replyMemberVO = new MeetingReplyMemberVO();
            replyMemberVO.setMemberId(reply.getUserId());
            replyMemberVO.setReplyState(reply.getFeedbackFlag() == null ? "-100" : reply.getFeedbackFlag().toString());
            replyMemberVO.setLook(reply.getLookState() == null ? "0" : reply.getLookState().toString());
            replyMemberVO.setUserType(reply.getUserType() == null ? 0 : reply.getUserType());

            /**
             * 告知人不计算在内
             */
            if (reply.getFeedbackFlag() == com.seeyon.v3x.meeting.util.Constants.FEEDBACKFLAG_IMPART) {
                continue;
            }

            if (reply.getFeedbackFlag() == com.seeyon.v3x.meeting.util.Constants.FEEDBACKFLAG_ATTEND) {
                joinList.add(replyMemberVO);
            } else if (reply.getFeedbackFlag() == com.seeyon.v3x.meeting.util.Constants.FEEDBACKFLAG_UNATTEND) {
                notJoinList.add(replyMemberVO);
            } else if (reply.getFeedbackFlag() == com.seeyon.v3x.meeting.util.Constants.FEEDBACKFLAG_PENDING) {
                waitJoinList.add(replyMemberVO);
            } else {
                waitJoinList.add(replyMemberVO);
            }
            allList.add(replyMemberVO);
        }
        mav.addObject("joinList", joinList);
        mav.addObject("notJoinList", notJoinList);
        mav.addObject("waitJoinList", waitJoinList);
        mav.addObject("allList", allList);

        return mav;
    }

    /**
     * 已办会议 栏目 【更多】
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView moreMeeting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("meeting/manager/moreMeeting");
        FlipInfo fi = new FlipInfo();
        String fragmentId = request.getParameter("fragmentId");
        String ordinal = request.getParameter("ordinal");
        String rowStr = request.getParameter("rowStr");
        String meeting_category = request.getParameter("meeting_category");
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("fragmentId", fragmentId);
        query.put("ordinal", ordinal);
        query.put("state", StateEnum.col_done.key());
        query.put("isTrack", false);
        query.put("meeting_category", meeting_category);
        fi.setSortField("receiveTime");
        fi.setSortOrder("desc");
        query.put("isFromMore", true);
        this.pendingManager.getMoreList4SectionContion(fi, query);
        modelAndView.addObject("total", fi.getTotal());
        request.setAttribute("ffmoreList", fi);
        fi.setParams(query);
        String columnsName = ResourceUtil.getString("system.menuname.AccomplishedConference");
        if (Strings.isNotBlank(fragmentId)) {
            Long fragmentIdL = Long.parseLong(fragmentId);
            Map<String, String> preference = portalApi.getPropertys(fragmentIdL, ordinal);
            String name = preference.get(PropertyName.columnsName.name());
            if (Strings.isNotBlank(name)) {
                columnsName = name;
            }
        }
        modelAndView.addObject("columnsName", columnsName);
        modelAndView.addObject("params", query);
        modelAndView.addObject("rowStr", rowStr);
        modelAndView.addObject("meeting_category", meeting_category);
        return modelAndView;
    }

    public ModelAndView listMeetingByReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meeting/manager/meetingReport_main_frame");
        return mav;
    }

    /**
     * 绩效报表查询会议详细列表入口 --xiangfan
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listMeetingByReportIframe(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("meeting/user/listMeetingByReport");
        //统计类型（mtReply:会议回执情况,mtRole:会议角色,workStat:日常工作统计,affairStat:事项统计）
        String statType = request.getParameter("statType");
        String reportName = request.getParameter("reportName");
        String time = request.getParameter("time");//0 为统计本日
        Long reportId = Strings.isNotBlank(request.getParameter("reportId")) ? Long.parseLong(request.getParameter("reportId")) : -1L;
        Long userId = Strings.isNotBlank(request.getParameter("userid")) ? Long.parseLong(request.getParameter("userid")) : AppContext.getCurrentUser().getId();

        String begindate = request.getParameter("begindate");
        String beginDateStr = Strings.isNotBlank(begindate) ? begindate.trim() : null;
        Date beginDate = Strings.isNotBlank(beginDateStr) ? Datetimes.getTodayFirstTime(beginDateStr) : null;

        String enddate = request.getParameter("enddate");
        String endDateStr = Strings.isNotBlank(enddate) ? enddate.trim() : null;
        Date endDate = Strings.isNotBlank(endDateStr) ? Datetimes.getTodayFirstTime(endDateStr) : null;


        String personGroupTab = request.getParameter("personGroupTab");//(1 个人报表,2 团队报表)
//	    if("2".equals(personGroupTab)&&!performancereportApi.isAuth4Report(AppContext.currentUserId(), reportId)){
        if ("2".equals(personGroupTab) && !performanceReportApi.checkReport(reportId, AppContext.currentAccountId(), AppContext.currentUserId())) {
            return null;
        }
        if (!"0".equals(time) && ("affairStat".equals(statType) || "mtReply".equals(statType) || "mtRole".equals(statType))) {
            endDate = Strings.isNotBlank(endDateStr) ? Datetimes.getTodayLastTime(endDateStr) : null;
        }
        //字段类型[mtReply:31(参加) 32(不参加) 33(待定) 11,12(未回执)],[mtRole:create(发起人) emcee(主持人) recorder(记录人) conferee(与会人) ]
        String fieldType = request.getParameter("fieldType");
        //日常工作统计 状态字段，其他会议统计该字段为null，已完成:4，待办:3，已发:2，未回执:11 ，已回执:31，已归档:-10
        Integer status = Strings.isNotBlank(request.getParameter("status")) ? Integer.parseInt(request.getParameter("status")) : null;
        List<MtMeeting> mtList = this.mtMeetingManager.getMeetingListByPerformance(userId, beginDate, endDate, statType, fieldType, status, true);
        mav.addObject("list", mtList);
        mav.addObject("userId", userId);
        mav.addObject("statType", statType);
        mav.addObject("time", time);
        mav.addObject("begindate", request.getParameter("begindate"));
        mav.addObject("enddate", request.getParameter("enddate"));
        mav.addObject("fieldType", fieldType);
        mav.addObject("status", status);
        mav.addObject("reportName", reportName);
        mav.addObject("currentUser", AppContext.getCurrentUser().getId());
        return mav;
    }

    /**
     * 绩效穿透查询-打印
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listMeetingExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //统计类型（mtReply:会议回执情况,mtRole:会议角色,workStat:日常工作统计,affairStat:事项统计）
        String statType = request.getParameter("statType");
        String time = request.getParameter("time");//0 为统计本日
        Long userId = Strings.isNotBlank(request.getParameter("userid")) ? Long.parseLong(request.getParameter("userid")) : AppContext.getCurrentUser().getId();

        String begindate = request.getParameter("begindate");
        String beginDateStr = Strings.isNotBlank(begindate) ? begindate.trim() : null;
        Date beginDate = Strings.isNotBlank(beginDateStr) ? Datetimes.getTodayFirstTime(beginDateStr) : null;

        String enddate = request.getParameter("enddate");
        String endDateStr = Strings.isNotBlank(enddate) ? enddate.trim() : null;
        Date endDate = Strings.isNotBlank(endDateStr) ? Datetimes.getTodayFirstTime(endDateStr) : null;

        if (!"0".equals(time) && ("affairStat".equals(statType) || "mtReply".equals(statType) || "mtRole".equals(statType))) {
            endDate = Strings.isNotBlank(endDateStr) ? Datetimes.getTodayLastTime(endDateStr) : null;
        }
        //字段类型[mtReply:31(参加) 32(不参加) 33(待定) 11,12(未回执)],[mtRole:create(发起人) emcee(主持人) recorder(记录人) conferee(与会人) ]
        String fieldType = request.getParameter("fieldType");
        //日常工作统计 状态字段，其他会议统计该字段为null，已完成:4，待办:3，已发:2，未回执:11 ，已回执:31，已归档:-10
        Integer status = Strings.isNotBlank(request.getParameter("status")) ? Integer.parseInt(request.getParameter("status")) : null;
        List<MtMeeting> mtList = this.mtMeetingManager.getMeetingListByPerformance(userId, beginDate, endDate, statType, fieldType, status, false);
        DataRecord dr = new DataRecord();
        String[] colNames = new String[3];
        colNames[0] = ResourceUtil.getString("mt.list.column.mt_name");
        colNames[1] = ResourceUtil.getString("mt.mtMeeting.beginDate");
        colNames[2] = ResourceUtil.getString("mt.mtMeeting.endDate");
        dr.setColumnName(colNames);
        dr.setTitle(ResourceUtil.getString("mt.mtMeeting.stat"));
        dr.setSheetName(ResourceUtil.getString("mt.mtMeeting.stat"));
        if (mtList != null && mtList.size() > 0) {
            DataRow[] datarow = new DataRow[mtList.size()];
            for (int i = 0; i < mtList.size(); i++) {
                datarow[i] = new DataRow();
                datarow[i].addDataCell(mtList.get(i).getTitle(), 1);
                datarow[i].addDataCell(Datetimes.format(mtList.get(i).getBeginDate(), Datetimes.datetimeWithoutSecondStyle), 1);
                datarow[i].addDataCell(Datetimes.format(mtList.get(i).getEndDate(), Datetimes.datetimeWithoutSecondStyle), 1);
            }
            dr.addDataRow(datarow);
        }
        this.fileToExcelManager.save(response, ResourceUtil.getString("mt.mtMeeting.stat"), new DataRecord[]{dr});
        return null;
    }

    public ModelAndView cancelMeeting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String listType = MeetingUtil.getString(request, "listType", ListTypeEnum.listSendMeeting.name());
        int type = MeetingListTypeEnum.getTypeName(listType);
        Boolean isBatch = MeetingUtil.getBoolean(request, "isBatch", false);
        String content = request.getParameter("cancelComment");
        boolean sendSMS = MeetingUtil.getBoolean(request, "canSendSMS", false);
        Long meetingId = MeetingUtil.getLong(request, "id", Constants.GLOBAL_NULL_ID);
        if (!MeetingUtil.isIdNull(meetingId)) {
            try {
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                parameterMap.put("meetingId", meetingId);
                parameterMap.put("type", type);
                parameterMap.put("isBatch", isBatch);
                parameterMap.put("currentUser", AppContext.getCurrentUser());
                parameterMap.put("content", content);
                parameterMap.put("sendSMS", sendSMS);
                meetingManager.transCancelMeeting(parameterMap);
            } catch (Exception e) {
                LOGGER.error("会议撤销出错", e);
            }
        }
        return this.redirectModelAndView("/" + MeetingPathConstant.NAVIGATION_DO + "?method=list&listType=" + Strings.toHTML(listType) + Functions.csrfSuffix());
    }

    public ModelAndView finishMeeting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String listType = MeetingUtil.getString(request, "listType", ListTypeEnum.listSendMeeting.name());
        int type = MeetingListTypeEnum.getTypeName(listType);
        String content = request.getParameter("cancelComment");
        Long meetingId = MeetingUtil.getLong(request, "id", Constants.GLOBAL_NULL_ID);
        if (!MeetingUtil.isIdNull(meetingId)) {
            try {
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                parameterMap.put("meetingId", meetingId);
                parameterMap.put("type", type);
                parameterMap.put("currentUser", AppContext.getCurrentUser());
                parameterMap.put("content", content);
                parameterMap.put("action", MeetingActionEnum.finishMeeting.name());
                parameterMap.put("endDatetime", DateUtil.currentDate());
                meetingManager.transFinishAdvanceMeeting(parameterMap);
            } catch (Exception e) {
                LOGGER.error("会议提前结束出错", e);
            }
        }
        return this.redirectModelAndView("/" + MeetingPathConstant.NAVIGATION_DO + "?method=list&listType=" + listType + Functions.csrfSuffix());
    }

    public ModelAndView deleteMeeting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String listType = MeetingUtil.getString(request, "listType", ListTypeEnum.listSendMeeting.name());
        int type = MeetingListTypeEnum.getTypeName(listType);

        String idStr = request.getParameter("id");
        if (Strings.isNotBlank(idStr)) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            List<Long> idList = MeetingUtil.getIdList(idStr);
            parameterMap.put("idList", idList);
            parameterMap.put("type", type);
            parameterMap.put("currentUser", AppContext.getCurrentUser());

            meetingManager.transDeleteMeeting(parameterMap);
        }

        return this.redirectModelAndView("/" + MeetingPathConstant.NAVIGATION_DO + "?method=list&listType=" + listType + Functions.csrfSuffix());
    }

    /**
     * 会议列表操作:归档
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView pigeonhole(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String ids = request.getParameter("id");
        String folders = request.getParameter("folders");

        if (Strings.isNotBlank(ids)) {
            List<Long> meetingIdList = MeetingUtil.getIdList(ids);
            List<Long> folderIdList = MeetingUtil.getIdList(folders);

            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("meetingIdList", meetingIdList);
            parameterMap.put("folderIdList", folderIdList);
            parameterMap.put("currentUser", AppContext.getCurrentUser());
            meetingManager.transPigeonhole(parameterMap);

        }

        return this.redirectModelAndView("/" + MeetingPathConstant.NAVIGATION_DO + "?method=entryManager&entry=meetingDone" + Functions.csrfSuffix());
    }

    /**
     * @param request
     * @param response
     * @return
     */
    public ModelAndView showContentOfSummaryOffice(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("meeting/user/showContentOfSummaryOffice");
        long summaryId = Long.parseLong(ReqUtil.getString(request, "summaryId"));
        try {
            MeetingSummary summary = meetingSummaryManager.getSummaryById(summaryId);
            mav.addObject("summary", summary);
        } catch (Exception e) {
            logger.error("", e);
        }
        return mav;
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
        String meetingId = request.getParameter("meetingid");
        MtMeeting bean = meetingManager.getMeetingById(Long.parseLong(meetingId));
        List<Long> leaderIdList = new ArrayList<Long>();//存放参会领导ID
        Map<Long, MtReply> list_allReply = replyManager.findAllByMeetingId(bean.getId());
        List<MtReplyWithAgentInfo> replyLeaderExList = mtMeetingManager.getMtReplyInfoByLeader(bean, leaderIdList, list_allReply);
        List<MtReplyWithAgentInfo> replyExList = mtMeetingManager.getMtReplyInfo(bean, leaderIdList, list_allReply);
        if (Strings.isNotEmpty(replyLeaderExList) && Strings.isNotEmpty(replyExList)) {
            List<MtReplyWithAgentInfo> leaderInfoList = new ArrayList<MtReplyWithAgentInfo>();
            for (MtReplyWithAgentInfo mtReplyWithAgentInfo : replyExList) {
                if (mtReplyWithAgentInfo.getReplyUserId() != null && leaderIdList.contains(mtReplyWithAgentInfo.getReplyUserId())) {
                    leaderInfoList.add(mtReplyWithAgentInfo);
                }
            }
            replyExList.removeAll(leaderInfoList);
        }
        Object[] feedbackUsers = mtMeetingManager.getMeetingReplyUsers(Long.parseLong(meetingId), null);

        String type = request.getParameter("type");
        List<Map<String, Object>> members = (List<Map<String, Object>>) feedbackUsers[Integer.parseInt(type)];
        for (Map<String, Object> userInfo : members) {
            V3xOrgMember member = orgManager.getMemberById((Long) userInfo.get("id"));
            userList.add(member);
        }
        mav.addObject("userList", userList);
        return mav;
    }

    /**
     * 会议at窗口
     *
     * @param request
     * @param response
     * @return
     * @throws BusinessException
     */
    public ModelAndView openAtDialog(HttpServletRequest request, HttpServletResponse response) throws BusinessException {
        ModelAndView mav = new ModelAndView(MeetingPathConstant.at_dialog);
        return mav;
    }


    /****************************** 依赖注入 **********************************/
    public void setMeetingNewManager(MeetingNewManager meetingNewManager) {
        this.meetingNewManager = meetingNewManager;
    }

    public void setMeetingManager(MeetingManager meetingManager) {
        this.meetingManager = meetingManager;
    }

    public void setPublicResourceManager(PublicResourceManager publicResourceManager) {
        this.publicResourceManager = publicResourceManager;
    }

    public void setMeetingTemplateManager(MeetingTemplateManager meetingTemplateManager) {
        this.meetingTemplateManager = meetingTemplateManager;
    }

    public void setMeetingExtManager(MeetingExtManager meetingExtManager) {
        this.meetingExtManager = meetingExtManager;
    }

    public void setConfereesConflictManager(ConfereesConflictManager confereesConflictManager) {
        this.confereesConflictManager = confereesConflictManager;
    }

    public void setPendingManager(PendingManager pendingManager) {
        this.pendingManager = pendingManager;
    }

    public void setMeetingSummaryManager(MeetingSummaryManager meetingSummaryManager) {
        this.meetingSummaryManager = meetingSummaryManager;
    }

    public void setMtMeetingManager(MtMeetingManager mtMeetingManager) {
        this.mtMeetingManager = mtMeetingManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setMobileMessageManager(MobileMessageManager mobileMessageManager) {
        this.mobileMessageManager = mobileMessageManager;
    }

    public void setReplyManager(MtReplyManager replyManager) {
        this.replyManager = replyManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setFileToExcelManager(FileToExcelManager fileToExcelManager) {
        this.fileToExcelManager = fileToExcelManager;
    }

    public void setMeetingValidationManager(MeetingValidationManager meetingValidationManager) {
        this.meetingValidationManager = meetingValidationManager;
    }

    public void setPortalApi(PortalApi portalApi) {
        this.portalApi = portalApi;
    }

    public void setPerformanceReportApi(PerformanceReportApi performanceReportApi) {
        this.performanceReportApi = performanceReportApi;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }
}
