package com.seeyon.apps.m3.pending.utils;

import com.seeyon.apps.cip.enums.AccessTypeEnum;
import com.seeyon.apps.cip.manager.AppManager;
import com.seeyon.apps.cip.manager.RegisterManager;
import com.seeyon.apps.cip.po.M3AppVersion;
import com.seeyon.apps.cip.po.RegisterPO;
import com.seeyon.apps.cip.po.ThirdPendingPO;
import com.seeyon.apps.cip.util.CIPUtil;
import com.seeyon.apps.inquiry.api.InquiryApi;
import com.seeyon.apps.inquiry.bo.InquirySurveybasicBO;
import com.seeyon.apps.m3.app.utils.AppUtils;
import com.seeyon.apps.m3.app.vo.AppInfoVO;
import com.seeyon.apps.m3.common.BaseAffairs;
import com.seeyon.apps.m3.common.M3AppContextUtils;
import com.seeyon.apps.m3.common.Pagination;
import com.seeyon.apps.m3.message.vo.MessageEums;
import com.seeyon.apps.m3.pending.vo.AffairListItem;
import com.seeyon.apps.m3.pending.vo.ListItemDisplayValue;
import com.seeyon.apps.meeting.enums.MeetingEnums.MeetingFeedbackFlagEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.portal.section.PendingRow;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class M3PendingConvertUtils {

    private static InquiryApi inquiryApi = (InquiryApi) AppContext.getBean("inquiryApi");

    // 需要过滤的SubCategory
    private static ApplicationSubCategoryEnum[] subAppFilter = {};// subapp枚举
    private static Integer[] appFilter = {ApplicationCategoryEnum.exSend.getKey(),
            /*ApplicationCategoryEnum.exSign.getKey(),*/ ApplicationCategoryEnum.edocRegister.getKey()};

    // 判断m3是否可以显示此条待办
    public static boolean isHasAffair(PendingRow pr) {
        boolean isHas = true;
        if (Arrays.asList(appFilter).contains(pr.getApplicationCategoryKey())) {//代码检查   修改导入util包中的Arrays  2018-03-06 xinpei
            isHas = false;
        }
        if (isHas) {
            B:
            for (ApplicationSubCategoryEnum e : subAppFilter) {
                if (e.getApp().getKey() == pr.getApplicationCategoryKey()) {
                    if (pr.getApplicationSubCategoryKey() != null) {
                        if (e.getKey() == pr.getApplicationSubCategoryKey()) {
                            isHas = false;
                            break B;
                        }
                    }
                }
            }
        }
        return isHas;
    }

    public static Pagination<AffairListItem> convertVO(FlipInfo fi) throws BusinessException {
        Pagination<AffairListItem> page = new Pagination<AffairListItem>();
        List<AffairListItem> list = new ArrayList<AffairListItem>();
        List<PendingRow> prlist = fi.getData();
        for (PendingRow pr : prlist) {
            AffairListItem ai = new AffairListItem();
            if (isHasAffair(pr)) {
                ai.setReadonly(MessageEums.ReadOnlyStatus.none.getStatus());
            } else {
                ai.setReadonly(MessageEums.ReadOnlyStatus.readonly.getStatus());
            }
            int app = pr.getApplicationCategoryKey().intValue();
            ai.setAffairId(pr.getId().toString());
            ai.setAppId(AppUtils.getAppIdByCategory(app));
            ai.setContent(pr.getSubject() == null ? "" : pr.getSubject());
            ai.setTitle("");
            ai.setAffairSubStatus(pr.getSubState() == null ? "" : pr.getSubState().toString());
            ai.setGotoParams(getPendingGotoParams(pr));
            ai.setGrade(BaseAffairs.getGrade(pr.getImportantLevel()).getGrade());
            ai.setCreateTime(null == pr.getReceiveTimeAll() ? Datetimes.format(pr.getCreateDate(), "yyyy-MM-dd HH:mm:ss") : pr.getReceiveTimeAll());
            ai.setSenderName(pr.getCreateMemberName());
            ai.setSenderMark(pr.getCreateMemberId().toString());
            ai.setSenderFaceUrl(getMemberIcon(pr.getCreateMemberId().longValue()));
            ai.setHasAttachment(pr.getHasAttachments().booleanValue());
            ai.setAffairSubStatus(pr.getSubState() == null ? "" : pr.getSubState().toString());
            ai.setServerIdentifier(M3AppContextUtils.getServerIdentifier());
            ai.setSubAppId(pr.getApplicationSubCategoryKey() == null ? "" : pr.getApplicationSubCategoryKey().toString());

            ai.setReceiveTime(pr.getReceiveTime());
            ai.setCompleteTime(pr.getCompleteTime());

            if ((pr.getSubState() != null) && (pr.getSubState().intValue() != 11))
                ai.setStatus(BaseAffairs.AffairStatusEnums.read.getStatus());
            else {
                ai.setStatus(BaseAffairs.AffairStatusEnums.unread.getStatus());
            }
            if (app == 1) {
                if (pr.getSubState() != null && Strings.isNotBlank(pr.getBackFromName())) {
                    ai.setBeBack(true);
                }
                ai.setDisplayValueList(getCollListItemValue(pr));
                ai.setRemainingTime(String.valueOf(pr.getDealTimeout()));
            } else if (AppUtils.getEdocApps().contains(app)) {
                ai.setRemainingTime(String.valueOf(pr.getDealTimeout()));
            } else if (app == 6) {
                List<ListItemDisplayValue> dis = getMeetingListItemValue(pr);
                ai.setDisplayValueList(dis);
                if (dis != null && dis.size() > 3) {
                    ListItemDisplayValue value = dis.get(3);
                    ai.setJoinMeetingState(value.getValue());
                } else {
                    ai.setJoinMeetingState("FEEDBACK_FLAG_HOLD");
                }
                ai.setCreateTime(pr.getReceiveTimeAll());//会议的话给召开时间和pc匹配
            } else if (app == 10) {
                ai.setDisplayValueList(getInquiryListItemValue(pr.getObjectId()));
            }

            Map<String, String> prExtParam = pr.getExtParam();
            if (prExtParam != null) {
                String attitude = ParamUtil.getString(prExtParam, "attitude");
                String canFastProcess=ParamUtil.getString(prExtParam,"canFastProcess");
                if (Strings.isNotBlank(attitude) && ("true".equals(canFastProcess)|| canFastProcess==null)) {
                    ai.putExtParam("attitude", attitude);
                }
            }

            ai.setMeetingImpart(pr.getMeetingImpart() + "");
            //拼接协同处理需要的参数{affairId,summaryId,subState,templateId,canDeleteORarchive,canReMove,workitemId,processId}
            Map<String, Object> handerMap = new HashMap<String, Object>();
            if ("6".equals(ai.getAppId())) {
                //拼接会议处理参数
                handerMap.put("meetingId", pr.getObjectId() + "");
                handerMap.put("affairId", pr.getId() + "");
                handerMap.put("memberId", pr.getMemberId() + "");
                handerMap.put("pagetype", "1");
                User user = AppContext.getCurrentUser();
                if (user != null) {
                    handerMap.put("proxyId", user.getId() + "");
                } else {
                    handerMap.put("proxyId", "");
                }
                if(prExtParam != null){
                    handerMap.put("replyShowFlag",ParamUtil.getString(prExtParam,"replyShowFlag"));
                }
            } else {
                handerMap.put("affairId", pr.getId() + "");
                handerMap.put("subject", pr.getSubject());
                handerMap.put("category", pr.getApplicationCategoryKey() + "");
                handerMap.put("summaryId", pr.getObjectId() + "");
                handerMap.put("subState", pr.getSubState() + "");
                handerMap.put("templateId", pr.getTemplateId());
                handerMap.put("processId", pr.getProcessId());
                if (prExtParam != null) {
                    String canDeleteORarchive = ParamUtil.getString(prExtParam, "canDeleteORarchive");
                    if (Strings.isNotBlank(canDeleteORarchive)) {
                        handerMap.put("canDeleteORarchive", canDeleteORarchive);
                    }
                    String canReMove = ParamUtil.getString(prExtParam, "canReMove");
                    if (Strings.isNotBlank(canReMove)) {
                        handerMap.put("canReMove", canReMove);
                    }
                    String workitemId = ParamUtil.getString(prExtParam, "workitemId");
                    if (Strings.isNotBlank(workitemId)) {
                        handerMap.put("workitemId", workitemId);
                    }
                }
            }
            ai.setHandleParam(handerMap);
            //=====================拼接快捷处理需要的数据==============
            list.add(ai);
        }
        page.setChange("true");
        page.setTotal(fi.getTotal());
        page.setPageNo(fi.getPage());
        page.setPageSize(fi.getSize());
        page.setData(list);
        return page;
    }

    public static Pagination<AffairListItem> convertThirdVO(FlipInfo fi) throws BusinessException {
        Pagination<AffairListItem> page = new Pagination<AffairListItem>();
        List<AffairListItem> list = new ArrayList<AffairListItem>();
        List<ThirdPendingPO> thirds = fi.getData();
        RegisterManager rm = (RegisterManager) AppContext.getBean("registerManager");
        for (ThirdPendingPO t : thirds) {
            AffairListItem ai = new AffairListItem();
            long app = t.getRegisterId();
            ai.setReadonly(MessageEums.ReadOnlyStatus.none.getStatus());
            ai.setAffairId(t.getId().toString());//先放主键有歧义再改为第三方待办ID
            RegisterPO po = rm.getRegisterByCache(app);
            ai.setAppId(po == null ? "" : String.valueOf(po.getAppCode()));
            ai.setAppName(po == null ? "" : po.getAppName());
            if (null != po) {
                if (po.getIconH5() != null && !"".equals(po.getIconH5())) {
                    ai.setIconUrl(M3AppContextUtils.getM3ServerAccessURL() + "/seeyon" + po.getIconH5());
                } else {
                    ai.setIconUrl("");
                }
            }
            ai.setContent(t.getTitle() == null ? "" : t.getTitle());
            ai.setTitle("");
            ai.setAffairSubStatus(t.getSubState() == null ? "" : t.getSubState().toString());
            ai.setGotoParams(getPendingGotoParams(t));
            ai.setGrade(BaseAffairs.getGrade(null).getGrade());
            ai.setCreateTime(Datetimes.format(t.getCreationDate(), "yyyy-MM-dd HH:mm:ss"));
            ai.setSenderName(t.getSenderName());
            ai.setSenderMark(t.getSenderId() == null ? "" : t.getSenderId().toString());
            if (t.getSenderId() != null) {
                ai.setSenderFaceUrl(getMemberIcon(t.getSenderId()));
            }
            ai.setHasAttachment(false);
            ai.setServerIdentifier(M3AppContextUtils.getServerIdentifier());
            if (t.getState() == 0) {
                ai.setStatus(BaseAffairs.AffairStatusEnums.unread.getStatus());
            } else {
                ai.setStatus(BaseAffairs.AffairStatusEnums.read.getStatus());
            }
            list.add(ai);
        }
        page.setChange("true");
        page.setTotal(fi.getTotal());
        page.setPageNo(fi.getPage());
        page.setPageSize(fi.getSize());
        page.setData(list);
        return page;
    }

    public static List<ListItemDisplayValue> getCollListItemValue(PendingRow pr) throws BusinessException {
        List<ListItemDisplayValue> result = new ArrayList<ListItemDisplayValue>(1);
        ListItemDisplayValue data = new ListItemDisplayValue();
//		FlipInfo fpi = new FlipInfo(1,1);
//		ctpCommentManager.findCommentByType(ModuleType.collaboration, moduleID, Comment.CommentType.comment, fpi, true);
        data.setValue(String.valueOf(pr.getReplyCounts()));
        data.setDisplay(ListItemDisplayValue.display_replies);
        result.add(data);
        return result;
    }

    public static List<ListItemDisplayValue> getMeetingListItemValue(PendingRow pr) throws BusinessException {
        List<ListItemDisplayValue> result = new ArrayList<ListItemDisplayValue>(3);
        result.add(new ListItemDisplayValue(pr.getProcessedNumber() == null ? "0" : pr.getProcessedNumber().toString(), ListItemDisplayValue.display_meeting_reply_yes));
        result.add(new ListItemDisplayValue(pr.getUnJoinNumber() == null ? "0" : pr.getUnJoinNumber().toString(), ListItemDisplayValue.display_meeting_reply_no));
        result.add(new ListItemDisplayValue(pr.getPendingNumber() == null ? "0" : pr.getPendingNumber().toString(), ListItemDisplayValue.display_meeting_reply_hold));
        if (pr.getMeetingImpart() != null && !"".equals(pr.getMeetingImpart())) {
            result.add(new ListItemDisplayValue("", ListItemDisplayValue.display_meeting_feedback));
        } else {
            if (pr.getSubState() == SubStateEnum.meeting_pending_join.getKey()) {
                result.add(new ListItemDisplayValue(MeetingFeedbackFlagEnum.FEEDBACK_FLAG_ATTEND.toString(), ListItemDisplayValue.display_meeting_feedback));
            } else if (pr.getSubState() == SubStateEnum.meeting_pending_unJoin.getKey()) {
                result.add(new ListItemDisplayValue(MeetingFeedbackFlagEnum.FEEDBACK_FLAG_NOT_ATTEND.toString(), ListItemDisplayValue.display_meeting_feedback));
            } else if (pr.getSubState() == SubStateEnum.meeting_pending_pause.getKey()) {
                result.add(new ListItemDisplayValue(MeetingFeedbackFlagEnum.FEEDBACK_FLAG_HOLD.toString(), ListItemDisplayValue.display_meeting_feedback));
            } else {
                result.add(new ListItemDisplayValue("", ListItemDisplayValue.display_meeting_feedback));
            }
        }
        return result;
    }

    public static List<ListItemDisplayValue> getInquiryListItemValue(Long inquiryId) throws BusinessException {
        List<ListItemDisplayValue> result = new ArrayList<ListItemDisplayValue>(1);
        String voteCount = "0";
        String noVoteCount = "0";
        if (inquiryApi != null) {
            InquirySurveybasicBO basic = inquiryApi.getSimpleInquiryBasic(inquiryId);
            if (basic != null) {
                voteCount = String.valueOf(basic.getVoteCount());
                noVoteCount = String.valueOf(basic.getTotals() - basic.getVoteCount());
            }
        }
        result.add(new ListItemDisplayValue(voteCount, ListItemDisplayValue.display_inquiry_vote_count));
        result.add(new ListItemDisplayValue(noVoteCount, ListItemDisplayValue.display_inquiry_no_vote_count));
        return result;
    }

    private static String getMemberIcon(long memberId) {
        String m3Address = M3AppContextUtils.getM3ServerAccessURL();
        return m3Address + "/seeyon/rest/orgMember/avatar/" + memberId;
    }

    public static String getPendingGotoParams(PendingRow row) {
        Integer catgory = row.getApplicationCategoryKey();
        Map<String, String> params = new HashMap<String, String>();
        ApplicationCategoryEnum cat = ApplicationCategoryEnum.valueOf(catgory);
        switch (cat) {
            case office:
                params.put("affairId", row.getId().toString());
                params.put("subApp", row.getApplicationSubCategoryKey() == null ? "" : row.getApplicationSubCategoryKey().toString());
                break;
            case collaboration:
            case edoc:
            case edocRec:
            case edocRecDistribute:
            case edocRegister:
            case edocSend:
            case edocSign:
            // 待签收数据 mxs
            case exSign:
                params.put("affairId", row.getId().toString());
                break;
            case meetingroom:
                params.put("openFrom", "mrAuditList");
                params.put("roomAppId", row.getObjectId() == null ? "" : row.getObjectId().toString());
                params.put("subApp", row.getApplicationSubCategoryKey() == null ? "" : row.getApplicationSubCategoryKey().toString());
                break;
            case meeting:
            case mobileAppMgrForHTML5:
            case taskManage:
            case bulletin:
            case news:
            case show:
            case inquiry:
                params.put("affairId", row.getId().toString());
                params.put("id", row.getObjectId().toString());
                params.put("subApp", row.getApplicationSubCategoryKey() == null ? "" : row.getApplicationSubCategoryKey().toString());
                params.put("state", row.getState() == null ? "" : row.getState().toString());
            default:
                break;
        }

        return JSONUtil.toJSONString(params);
    }

    public static String getPendingGotoParams(ThirdPendingPO tird) throws BusinessException {
        Map<String, String> params = new HashMap<String, String>();
        RegisterManager rm = (RegisterManager) AppContext.getBean("registerManager");
        RegisterPO reg = rm.getRegisterByCache(tird.getRegisterId());
        params.put("parameters", "");
        if (reg != null) {
            int type = reg.getAccessMethod();
            if (type == AccessTypeEnum.MobileURL.getKey() || type == AccessTypeEnum.PCAndMobileURL.getKey()) {
                params.put("appType", AppInfoVO.AppTypeEnums.integration_remote_url.getType());
                String loginName = AppContext.currentUserLoginName();
                params.put("entry", CIPUtil.getCIPSSOTicket(loginName, tird.getH5url(), String.valueOf(reg.getAppCode()), "P"));//给h5url
            } else if (type == AccessTypeEnum.LocalH5.getKey() || type == AccessTypeEnum.PCAndLocalH5.getKey()) {
                AppManager appManager = (AppManager) AppContext.getBean("appManager");
                M3AppVersion m = appManager.findNewestVersion(String.valueOf(reg.getAppCode()));
                params.put("appType", AppInfoVO.AppTypeEnums.integration_local_h5.getType());
                if (m != null) {
                    params.put("appId", m.getAppId());//本地h5应用包类型给这么多
                    params.put("bundle_identifier", m.getBundleIdentifier());
                    params.put("bundle_name", m.getBundleName());
                    params.put("team", m.getTeam());
                    params.put("version", m.getVersion());
                }
            } else if (type == AccessTypeEnum.LocalApp.getKey() || type == AccessTypeEnum.PCAndLocalApp.getKey()) {
                AppManager appManager = (AppManager) AppContext.getBean("appManager");
                params.put("appType", AppInfoVO.AppTypeEnums.integration_native.getType());
                params.put("downloadUrl", tird.getContent() != null ? tird.getContent() : "");//原生应用下载地址
                String cmd = tird.getAppParam() == null ? "" : tird.getAppParam();
                String appId = tird.getReceiverId() == null ? "" : tird.getReceiverId().toString();
                params.put("entry", appManager.replaceUserName(cmd, appId, null));//给命令
            }
        }
        return JSONUtil.toJSONString(params);
    }
}
