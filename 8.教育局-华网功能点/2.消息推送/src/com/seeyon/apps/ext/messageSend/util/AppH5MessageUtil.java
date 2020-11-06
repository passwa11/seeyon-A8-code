package com.seeyon.apps.ext.messageSend.util;

import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.usermessage.pipeline.Message;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AppH5MessageUtil {

    private static Log log = LogFactory.getLog(AppH5MessageUtil.class);

    public static String getMessageJson(Message message, String platform, String corpid) {
        String url = "";
        try {
            url = getMessage(message, "link", platform, corpid);
        } catch (Exception var7) {
            log.error("push Ding msg Error!", var7);
        }
        return url;
    }

    public static String getMessage(Message message, String messageType, String platform, String corpid) {
        int category = message.getCategory();
        String remoteUrl = message.getRemoteURL();
        String content = message.getContent();
        String linkType = message.getLinkType();
        Long id = message.getReferenceId();
        if (id != null && id == -1L && message.getLinkParams() != null && message.getLinkParams().length > 0) {
            id = NumberUtils.toLong(message.getLinkParams()[0], -1L);
        }
        StringBuilder sb;
        String registerCode;
        Long affairId;

        String isWeixinLink="";
        if ("link".equals(messageType) && Strings.isNotBlank(linkType)) {
            if (category == ApplicationCategoryEnum.templateApprove.key()) {
                isWeixinLink = "";
                isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/templateapprove/html/dealWithTemplateApprove.html?state=" + message.getLinkParams()[0] + "&objectId=" + message.getLinkParams()[1] + "&templateId=" + message.getLinkParams()[2] + "&affairId=" + message.getLinkParams()[3] + "&formId=" + message.getLinkParams()[4];
            } else if (category == ApplicationCategoryEnum.collaboration.key()) {
                isWeixinLink = "";
                if (!"message.link.col.supervise".equals(linkType) && !"message.link.col.traceRecord".equals(linkType) && !"message.link.formtrigger.msg.flow".equals(linkType) && !"message.link.formtrigger.msg.unflow".equals(linkType) && !"message.link.formtrigger.cap4.msg.unflow".equals(linkType)) {
                    isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/collaboration/html/details/summary.html?affairId=" + id + "&openFrom=listPending&summaryId=-1&proxyId=0";
                } else {
                    content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                    remoteUrl = "";
                }
            } else if (category != ApplicationCategoryEnum.edoc.key() && category != ApplicationCategoryEnum.edocSend.key() && category != ApplicationCategoryEnum.edocRec.key() && category != ApplicationCategoryEnum.edocSign.key() && category != ApplicationCategoryEnum.exSend.key() && category != ApplicationCategoryEnum.exSign.key() && category != ApplicationCategoryEnum.edocRegister.key() && category != ApplicationCategoryEnum.edocRecDistribute.key() && category != ApplicationCategoryEnum.exchange.key()) {
                if (category == ApplicationCategoryEnum.meeting.key()) {
                    isWeixinLink = "";
                    if (!"message.link.mt.room_perm".equals(linkType) && !"message.link.office.meetingroom".equals(linkType)) {
                        if (!"message.link.mt.summary".equals(linkType) && !"message.link.mt.summary_send".equals(linkType) && !"message.link.mt.send.scope".equals(linkType)) {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/meeting/html/meetingDetail.html?meetingId=" + id + "&openFrom=pending&affairId=-1&proxyId=0&proxy=false";
                        } else {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/meeting/html/meetingSummary.html?meetingSummaryId=" + id + "&openFrom=message&affairId=-1&proxyId=0&proxy=false";
                        }
                    } else {
                        content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                        remoteUrl = "";
                    }
                } else if (category == ApplicationCategoryEnum.news.key()) {
                    isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/news/html/newsView.html?newsId=" + id + "&comeFrom=0";
                    if ("message.link.news.writedetail".equals(linkType)) {
                        content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                        remoteUrl = "";
                    } else {
                    }
                } else if (category == ApplicationCategoryEnum.bulletin.key()) {
                    isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/bulletin/html/bulView.html?bulId=" + id + "&comeFrom=0";
                    if ("message.link.bul.writedetail".equals(linkType)) {
                        content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                        remoteUrl = "";
                    } else {
                    }
                } else if (category == ApplicationCategoryEnum.bbs.key()) {
                    isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/bbs/html/bbsView.html?bbsId=" + id + "&from=message";
                } else if (category == ApplicationCategoryEnum.inquiry.key()) {
                    isWeixinLink = "";
                    if (!"message.link.inq.alreadyauditing.pass".equals(linkType) && !"message.link.inq.alreadyauditing.nopass".equals(linkType)) {
                        if ("message.link.inq.auditing".equals(linkType)) {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/inquiry/html/inquiryView.html?inquiryId=" + id + "&comeFrom=2&affairState=4";
                        } else {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/inquiry/html/inquiryView.html?inquiryId=" + id + "&comeFrom=0";
                        }
                    } else {
                        content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                        remoteUrl = "";
                    }
                } else {
                    String thirdId;
                    if (category == ApplicationCategoryEnum.vreport.key()) {
                        if ("message.link.vreport.schstats.report".equals(linkType)) {
                            isWeixinLink = message.getLinkParams()[2];
                            thirdId = message.getLinkParams()[4];
                            if ("true".equals(thirdId)) {
                                StringBuilder path = null;
                                if (ApplicationCategoryEnum.cap4biz.name().equals(isWeixinLink)) {
                                    path = new StringBuilder(SystemEnvironment.getContextPath() + "/m3/apps/v5/vreport/html/capReportBridge.html?");
                                    path.append("designId=").append(message.getLinkParams()[0]);
                                    path.append("&schlogId=").append(message.getLinkParams()[1]);
                                    path.append("&category=").append(message.getLinkParams()[2]);
                                    path.append("&categoryId=").append(message.getLinkParams()[3]);
                                    path.append("&success=").append(message.getLinkParams()[4]);
                                } else if (ApplicationCategoryEnum.global.name().equals(isWeixinLink)) {
                                    path = new StringBuilder(SystemEnvironment.getContextPath() + "/m3/apps/v5/vreport/html/reportIndex.html?");
                                    path.append("designId=").append(message.getLinkParams()[0]);
                                    path.append("&schlogId=").append(message.getLinkParams()[1]);
                                    path.append("&category=").append(message.getLinkParams()[2]);
                                    path.append("&categoryId=").append(message.getLinkParams()[3]);
                                    path.append("&success=").append(message.getLinkParams()[4]);
                                }

                                if (path != null) {
                                    remoteUrl = "";
                                }
                            }
                        }
                    } else if (category == ApplicationCategoryEnum.show.key()) {
                        isWeixinLink = message.getLinkParams()[0];
                        StringBuilder path = new StringBuilder(SystemEnvironment.getContextPath() + "/m3/apps/v5/show/html/showbarDetail.html?id=");
                        path.append(isWeixinLink);
                        if ("message.link.show.showReply".equals(linkType)) {
                            content = content.replaceAll("\"", "");
                            path.append("&showpostId=").append(message.getLinkParams()[1]);
                            path.append("&commentId=").append(message.getLinkParams()[2]);
                            path.append("&from=").append(message.getLinkParams()[3]);
                        } else if ("message.link.show.newshowbar".equals(linkType)) {
                            path.append("&from=").append(message.getLinkParams()[1]);
                        } else if ("message.link.show.showpost.settop".equals(linkType) || "message.link.show.showPraise".equals(linkType)) {
                            path.append("&showpostId=").append(message.getLinkParams()[1]);
                            path.append("&from=").append(message.getLinkParams()[2]);
                        }

                        path.append("&openType=").append("wechat");
                    } else if (category == ApplicationCategoryEnum.attendance.key()) {
                        isWeixinLink = "";
                        if ("message.link.attendance.view".equals(linkType)) {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/attendance/html/attendanceIndex.html?from=message";
                        } else {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/attendance/html/attendanceDetail.html?attendanceId=" + id;
                        }

                    } else {
                        StringBuilder path;
                        if (category == ApplicationCategoryEnum.taskManage.key()) {
                            path = new StringBuilder(SystemEnvironment.getContextPath() + "/m3/apps/v5/taskmanage/html/");
                            if ("message.link.taskmanage.viewfeedback".equals(linkType)) {
                                path.append("taskFeedbackList.html?openType=wechat&taskId=").append(message.getLinkParams()[0]);
                            } else if ("message.link.taskmanage.status".equals(linkType)) {
                                String[] linkParams = message.getLinkParams();
                                path.append("task_index.html?openType=wechat&listType=" + linkParams[0] + "&status=overdue");
                            } else {
                                path.append("taskEditor.html?openType=wechat&taskId=").append(message.getLinkParams()[0]);
                            }

                        } else if (category == ApplicationCategoryEnum.calendar.key()) {
                            path = new StringBuilder(SystemEnvironment.getContextPath() + "/m3/apps/v5/calendar/html/newCalEvent.html?");
                            path.append("id=").append(message.getLinkParams()[0]);
                        } else if (category == ApplicationCategoryEnum.office.key()) {
                            affairId = -1L;
                            if (message.getLinkParams() != null && message.getLinkParams().length > 0) {
                                affairId = NumberUtils.toLong(message.getLinkParams()[0], -1L);
                            }

                            thirdId = SystemEnvironment.getContextPath() + "/m3/apps/v5/office/html/";
                            if ("message.link.office.autoN.audit".equals(linkType)) {
                                thirdId = thirdId + "audit_auto.html?affairId=" + affairId;
                            } else if ("message.link.office.stockN.audit".equals(linkType)) {
                                thirdId = thirdId + "audit_stock.html?affairId=" + affairId;
                            } else if ("message.link.office.assetN.audit".equals(linkType)) {
                                thirdId = thirdId + "audit_asset.html?affairId=" + affairId;
                            } else if ("message.link.office.bookN.audit".equals(linkType)) {
                                thirdId = thirdId + "audit_book.html?applyId=" + id;
                            } else {
                                content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                                remoteUrl = "";
                            }
                        } else if (category > 2000) {
                            if (message.getLinkParams() != null && message.getLinkParams().length > 0) {
                                isWeixinLink = message.getLinkParams()[5];
                                if (Strings.isBlank(isWeixinLink)) {
                                    isWeixinLink = message.getLinkParams()[4];
                                }

                                if ("1".equals(isWeixinLink)) {
                                    thirdId = message.getLinkParams()[1];
                                    registerCode = message.getLinkParams()[2];
                                    if (StringUtils.isBlank(registerCode)) {
                                        remoteUrl = "";
                                    } else if (registerCode.startsWith("http")) {
                                        remoteUrl = registerCode + (registerCode.contains("?") ? "&weixinMessage=true" : "?weixinMessage=true");
                                    } else {
                                        sb = new StringBuilder(SystemEnvironment.getContextPath() + "/m3/apps/v5/cip/html/cipWeixinMessage.html?thirdpartyMessageId=");
                                        sb.append(thirdId);
                                        sb.append("&registerCode=");
                                        sb.append(registerCode);
                                    }
                                } else {
                                    remoteUrl = "";
                                }
                            } else {
                                remoteUrl = "";
                            }
                        } else if (category == ApplicationCategoryEnum.ai.key()) {
                            isWeixinLink = "";
                            if ("message.link.col.pending".equals(linkType)) {
                                isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/collaboration/html/details/summary.html?affairId=" + id + "&openFrom=listPending&summaryId=-1&proxyId=0";
                            }
                        }
                    }
                }
            } else {
                isWeixinLink = "";
                if (!"message.link.edoc.supervise.detail".equals(linkType) && !"message.link.edoc.supervise.main".equals(linkType) && !"message.link.exchange.distribute".equals(linkType) && !"message.link.exchange.receive".equals(linkType) && !"message.link.exchange.receive.hasten".equals(linkType) && !"message.link.exchange.register.govpending".equals(linkType) && !"message.link.exchange.register.pending".equals(linkType) && !"message.link.exchange.register.receive".equals(linkType) && !"message.link.exchange.registered".equals(linkType) && !"message.link.exchange.send".equals(linkType) && !"message.link.exchange.sent".equals(linkType) && !"message.link.edoc.traceRecord".equals(linkType) && !"message.link.govdoc.traceRecord".equals(linkType) && !"message.link.edoc.supervise".equals(linkType)) {
                    if (!Strings.isNotBlank(linkType) || !linkType.contains("govdoc") && !linkType.contains("ocip")) {
                        id = Long.valueOf(message.getLinkParams()[0]);
                        isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/edoc/html/edocSummary.html?affairId=" + id + "&openFrom=listPending&summaryId=-1&proxyId=0";
                    } else {
                        id = Long.valueOf(message.getLinkParams()[0]);
                        isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/edoc/html/details/summary.html?affairId=" + id + "&openFrom=listPending&summaryId=-1&proxyId=0";
                        if ("message.link.govdoc.supervise".equals(linkType)) {
                            isWeixinLink = SystemEnvironment.getContextPath() + "/m3/apps/v5/edoc/html/details/summary.html?affairId=-1&openFrom=edocStatistics&summaryId=" + id + "&proxyId=0";
                        }
                    }
                } else {
                    content = Strings.isBlank(content) ? ResourceUtil.getString("weixin.login.processing.please") : content + " " + ResourceUtil.getString("weixin.login.processing.please");
                    remoteUrl = "";
                }
            }
        } else {
            remoteUrl = "";
        }
        return isWeixinLink;
    }


}
