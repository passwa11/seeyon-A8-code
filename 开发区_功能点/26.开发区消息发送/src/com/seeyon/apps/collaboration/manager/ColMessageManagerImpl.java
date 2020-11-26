/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 * <p>
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 * <p>
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.springframework.util.CollectionUtils;

import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.bo.MessageCommentParam;
import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.collaboration.enums.ColOpenFrom;
import com.seeyon.apps.collaboration.listener.WorkFlowEventListener;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.util.FormUtil;
import com.seeyon.ctp.cap.api.manager.CAPFormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.po.supervise.CtpSupervisor;
import com.seeyon.ctp.common.processlog.ProcessLogAction;
import com.seeyon.ctp.common.processlog.manager.ProcessLogManager;
import com.seeyon.ctp.common.processlog.po.ProcessLogDetail;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.trace.enums.WFTraceConstants;
import com.seeyon.ctp.common.track.enums.TrackEnum;
import com.seeyon.ctp.common.track.manager.CtpTrackMemberManager;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.common.usermessage.Constants.LinkOpenType;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.common.usermessage.UserMessageUtil;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.constants.WorkFlowConstants;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.messageRule.bo.MessageRuleVO;
import com.seeyon.ctp.workflow.messageRule.manager.MessageRuleManager;
import com.seeyon.ctp.workflow.messageRule.ruleEnum.MessageRuleEnum.MessageRuleType;
import com.seeyon.ctp.workflow.util.WorkflowMatchLogMessageConstants;
import com.seeyon.v3x.common.exceptions.MessageException;
import com.seeyon.v3x.common.web.login.CurrentUser;

import net.joinwork.bpm.engine.wapi.WorkItem;

/**
 * 协同发送消息的类，协同模块发送消息的代码都放在这里，便于统一整理
 * @author mujun
 */
public class ColMessageManagerImpl implements ColMessageManager {
    private static Log LOG = CtpLogFactory.getLog(ColMessageManagerImpl.class);
    private UserMessageManager userMessageManager;
    private OrgManager orgManager;
    private AffairManager affairManager;
    private CtpTrackMemberManager trackManager;
    private ColManager colManager;
    private ProcessLogManager processLogManager;
    private SuperviseManager superviseManager;
    private AppLogManager appLogManager;
    private CommentManager ctpCommentManager;
    private CAPFormManager capFormManager;
    private MessageRuleManager messageRuleManager;

    public void setCtpCommentManager(CommentManager ctpCommentManager) {
        this.ctpCommentManager = ctpCommentManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public void setTrackManager(CtpTrackMemberManager trackManager) {
        this.trackManager = trackManager;
    }

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    public void setProcessLogManager(ProcessLogManager processLogManager) {
        this.processLogManager = processLogManager;
    }

    public void setSuperviseManager(SuperviseManager superviseManager) {
        this.superviseManager = superviseManager;
    }

    public void setCapFormManager(CAPFormManager capFormManager) {
        this.capFormManager = capFormManager;
    }

    public void setMessageRuleManager(MessageRuleManager messageRuleManager) {
        this.messageRuleManager = messageRuleManager;
    }

    /**
     * 去除没有跟踪当前事项的人(去除跟踪列表中为‘部分跟踪且不包含当前被跟踪人员’) by zhengchao at 2019年5月29日18点35分
     * @param trackMembers
     * @return
     */
    private List<CtpTrackMember> excludePartTrackAndNotCurrentMember(List<CtpTrackMember> trackMembers, Long currentMemberId) {
        for (Iterator<CtpTrackMember> it = trackMembers.iterator(); it.hasNext(); ) {
            CtpTrackMember ctpTrackMember = it.next();
            if (ctpTrackMember == null) {
                it.remove();
            }
            boolean trackAll = true;
            if (ctpTrackMember.getTrackAll() != null) {
                trackAll = ctpTrackMember.getTrackAll();
            }
            boolean equalCurrentMember = currentMemberId != null ? currentMemberId.equals(ctpTrackMember.getTrackMemberId()) : false;
            if (!trackAll && !equalCurrentMember) {
                it.remove();
            }
        }
        return trackMembers;
    }

    /**
     * 	新增发起人附言的时候给流程中的人发消息
     *  version 1.0   给流程中的人发消息
     *  version 2.0   如果流程中的人有代理人 则也给代理人发消息
     *  update by libing
     */
    public void doCommentPushMessage4SenderNote(Comment comment) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long summaryId = comment.getModuleId();
        List<CtpAffair> affairList = affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, summaryId);
        if (affairList == null || affairList.size() < 1) {
            return;
        }
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<MessageReceiver> receiversAgent = new HashSet<MessageReceiver>();//代理人
        Set<Long> filterMemberIds = new HashSet<Long>();
        for (CtpAffair affair : affairList) {
            Long memberId = affair.getMemberId();
            Long senderId = affair.getSenderId();
            if (memberId.intValue() == senderId.intValue()) {
                continue;
            }

            if (filterMemberIds.contains(memberId)) {
                continue;
            } else {
                filterMemberIds.add(memberId);
            }

            if (affair.getState() == StateEnum.col_pending.key()) {
                receivers.add(new MessageReceiver(affair.getId(), memberId, "message.link.col.pending", affair.getId(), comment.getId()));
            } else {
                receivers.add(new MessageReceiver(affair.getId(), memberId, "message.link.col.done.detail", affair.getId(), comment.getId()));
            }

            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair.getMemberId());
            if (null != agentId && !affair.getMemberId().equals(agentId)) {
                V3xOrgMember memberAgent = getMemberById(orgManager, affair.getMemberId());
                if (null != memberAgent) {
                    if (affair.getState() == StateEnum.col_pending.key()) {
                        receiversAgent.add(new MessageReceiver(affair.getId(), agentId, "message.link.col.pending", affair.getId(), comment.getId()));
                    } else {
                        receiversAgent.add(new MessageReceiver(affair.getId(), agentId, "message.link.col.done.detail", affair.getId(), comment.getId()));
                    }
                }
            }


        }
        Integer importantLevel = ColUtil.getImportantLevel(affairList.get(0));
        String forwardMemberId = affairList.get(0).getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(comment.getContent()));
        try {
            MessageContent content = new MessageContent("col.addnote", comment.getTitle(), user.getName(), forwardMemberFlag, forwardMember, opinionContent).setImportantLevel(affairList.get(0).getImportantLevel());
            if (null != affairList.get(0).getTempleteId()) {
                content.setTemplateId(affairList.get(0).getTempleteId());
            }
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            if (!receiversAgent.isEmpty()) {
                MessageContent agentContent = new MessageContent("col.addnote", comment.getTitle(), user.getName(), forwardMemberFlag, forwardMember, opinionContent).add("col.agent").setImportantLevel(affairList.get(0).getImportantLevel());
                if (null != affairList.get(0).getTempleteId()) {
                    agentContent.setTemplateId(affairList.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, user.getId(), receiversAgent, importantLevel);
            }
        } catch (MessageException e) {
            LOG.error("发起人增加附言消息提醒失败", e);
            throw new BusinessException("saveOpinion sendMessage Failed");
        }
    }

    public void doCommentPushMessage4Reply(Comment comment) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Long summaryId = comment.getModuleId();
        CtpCommentAll comAll = ctpCommentManager.getById(comment.getPid());
        String memberName = orgManager.getMemberById(comAll.getCreateId()).getName();
        String commentContent = UserMessageUtil.getComment4Message(Strings.toText(comment.getContent()));
        int commentType = Strings.isTrue(comment.isHidden()) ? 0 : Strings.isBlank(commentContent) ? -1 : 1;

        ColSummary summary = colManager.getColSummaryById(summaryId);
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
        trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, user.getId());
        List<Long[]> pushMemberIds = comment.getPushMessageToMembersList();

        if (!(comment.isPushMessage() != null && comment.isPushMessage())) {
            pushMemberIds = new ArrayList<Long[]>();
        }

        //不用给任何人提供消息.
        if (pushMemberIds.isEmpty() && trackMembers.isEmpty() && trackMembers.size() <= 0) {
            return;
        }
        //取得消息的接受者。
        Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
        Set<Long> members = new HashSet<Long>();

        String subject = comment.getTitle();

        String messageUrl = "";
        //定义一个Affair,主要取转发相关的信息.
        Integer importantLevel = ColUtil.getImportantLevel(summary);
        String forwardMemberId = summary.getForwardMember();
        for (CtpTrackMember trackMember : trackMembers) {
            if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(trackMember.getAffairState())) {
                messageUrl = "message.link.col.pending";
            } else {
                messageUrl = "message.link.col.done.detail";
            }
            Long affairId = trackMember.getAffairId();
            Long recieverMemberId = trackMember.getMemberId();
            Long transactorId = trackMember.getTransactorId();
            if (transactorId != null && isProxy(recieverMemberId, transactorId))
                recieverMemberId = transactorId;
            if (!recieverMemberId.equals(user.getId()) && !members.contains(recieverMemberId)) {
                members.add(recieverMemberId);
                MessageReceiver trackReceiver = new MessageReceiver(affairId, recieverMemberId, messageUrl, affairId, comment.getId());
                trackReceiver.setTrack(true);
                if (comAll.getCreateId().equals(recieverMemberId)) {
                    trackReceiver.setReply(true);
                }
                receiversMap.put(recieverMemberId, trackReceiver);
            }
        }
        for (Long[] push : pushMemberIds) {
            if (!members.contains(push[1]) && !push[1].equals(user.getId())) {
                MessageReceiver pushReceiver = getMessageReceiver(push, "message.link.col.pending", comment.getId().toString(), summaryId);
                if (pushReceiver != null) {
                    if (!comAll.getCreateId().equals(push[1])) {
                        pushReceiver.setAt(true);
                    } else {
                        pushReceiver.setReply(true);
                    }
                }
                receiversMap.put(push[1], pushReceiver);
                members.add(push[1]);
            } else if (members.contains(push[1])) {
                MessageReceiver existReceiver = receiversMap.get(push[1]);
                if (null != existReceiver) {
                    if (!comAll.getCreateId().equals(push[1])) {
                        existReceiver.setAt(true);
                    } else {
                        existReceiver.setReply(true);
                    }
                    receiversMap.put(push[1], existReceiver);
                }
            }
        }

        if (receiversMap.isEmpty())
            return;

        //取得消息的接受者。
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        receivers.addAll(receiversMap.values());

        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        if (Strings.isNotBlank(comment.getExtAtt2())) {
            V3xOrgMember theMember = null;
            try {
                theMember = orgManager.getEntityById(V3xOrgMember.class, comment.getCreateId());
            } catch (BusinessException e) {
                LOG.error("协同回复意见,获取接收人异常", e);
            }
            String proxyName = "";
            if (theMember != null) {
                proxyName = theMember.getName();
            }
            MessageContent content = new MessageContent("col.reply", subject, proxyName, memberName, forwardMemberFlag, forwardMember,
                    commentType, commentContent).add("col.agent.reply", user.getName()).setImportantLevel(summary.getImportantLevel());
            if (null != summary.getTempleteId()) {
                content.setTemplateId(summary.getTempleteId());
            }
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, comment.getCreateId(), receivers, importantLevel);

        } else {
            MessageContent content = new MessageContent("col.reply", subject, user.getName(), memberName,
                    forwardMemberFlag, forwardMember, commentType, commentContent).setImportantLevel(summary.getImportantLevel());
            if (null != summary.getTempleteId()) {
                content.setTemplateId(summary.getTempleteId());
            }
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
        }

    }

    //工作项完成消息提醒
    /* (non-Javadoc)
     * @see com.seeyon.apps.collaboration.manager.ColMessageManager#workitemFinishedMessage(com.seeyon.ctp.common.po.affair.CtpAffair, java.lang.Long)
     */
    public Boolean workitemFinishedMessage(Comment comment, CtpAffair affair, Long summaryId, Map<String, Object> overTimecondtiontMap) throws BusinessException {

        String messageRule = (String) overTimecondtiontMap.get("messageRule");

        //设置了消息规则就按照消息规则发送消息
        if (Strings.isNotBlank(messageRule)) {
            String currentNodeLast = (String) overTimecondtiontMap.get("currentNodeLast");
            List<MessageRuleVO> messageRuleVOs = messageRuleManager.getMeesageByIds(messageRule, MessageRuleType.dealNotice, null);
            if (Strings.isNotEmpty(messageRuleVOs)) {
                List<StateEnum> states = new ArrayList<StateEnum>();
                states.add(StateEnum.col_sent);
                states.add(StateEnum.col_pending);
                states.add(StateEnum.col_done);
                List<CtpAffair> affairList = affairManager.getAffairs(summaryId, states);
                ColSummary summary = colManager.getSummaryById(summaryId);
                boolean hasDefault = false;
                //是否是当前节点的最后一个处理人
                boolean currentNodeLastBoolean = "true".equals(currentNodeLast);
                if (!currentNodeLastBoolean && affair.getTempleteId() == null) {
                    List<Long> nodeIds = new ArrayList<Long>();
                    nodeIds.add(affair.getActivityId());
                    List<CtpAffair> activityAffair = affairManager.getPendingAffairs(summaryId, nodeIds);
                    if (Strings.isEmpty(activityAffair)) {
                        currentNodeLastBoolean = true;
                    } else if (activityAffair.size() == 1) {
                        CtpAffair pendingAffair = activityAffair.get(0);
                        if (pendingAffair.getId().equals(affair.getId())) {
                            currentNodeLastBoolean = true;
                        }
                    }
                }
                for (MessageRuleVO messageRuleVO : messageRuleVOs) {
                    //节点最后一个处理人才发消息
                    if (Strings.isNotBlank(messageRuleVO.getMessageMode()) && currentNodeLastBoolean) {
                        sendMessageByMessageRule(messageRuleVO, summary, affair, affairList, null, affair.getMemberId());
                    }
                }
            }
        }

        //默认给跟踪和督办人发消息

        Integer importantLevel = ColUtil.getImportantLevel(affair);
        Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
        User user = AppContext.getCurrentUser();
        V3xOrgMember theMember = null;
        theMember = getMemberById(orgManager, affair.getMemberId());
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
        trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, affair.getMemberId());
        List<Long[]> pushMemberIds = comment.getPushMessageToMembersList();
        String opinionId = String.valueOf(comment.getId());//String.valueOf(DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionId());
        String opinionContentAll = comment.getContent();//DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinion();


        //给督办人发送消息
        List<Long> filterMembers = new ArrayList<Long>();

        String messageKey = "collaboration.opinion.deal";//正常情况已处理
        Boolean OverTimeflag = false;
        if (overTimecondtiontMap != null) {
            OverTimeflag = (Boolean) overTimecondtiontMap.get("overFlag") == null ? false : (Boolean) overTimecondtiontMap.get("overFlag");
        }
        if (OverTimeflag) {
            messageKey = "node.affair.overTerm.autoruncase3";//超期处理-显示处理人和已超期
        }
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        MessageContent mc = null;


        String forwardMemberId = trackMembers.isEmpty() ? null : trackMembers.get(0).getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        try {

            int opinionAttitude = 0;
            if ("collaboration.dealAttitude.haveRead".equals(comment.getExtAtt1())) {
                opinionAttitude = 1;
            } else if ("collaboration.dealAttitude.agree".equals(comment.getExtAtt1())) {
                opinionAttitude = 2;
            } else if ("collaboration.dealAttitude.disagree".equals(comment.getExtAtt1())) {
                opinionAttitude = 3;
            }
            // int opinionAttitude = DateSharedWithWorkflowEngineThreadLocal.getFinishWorkitemOpinionAttitude();
            // 0:意见被隐藏; -1：意见内容为空; 1: 无态度有内容（内容前面加“意见：”）; 2: 有态度有内容
            int opinionType = comment.isHidden() ? 0 : Strings.isBlank(opinionContentAll) ? -1 : opinionAttitude == -1 ? 1 : 2;
            // -1:无附件或者意见被隐藏; 1: 无态度且无内容（内容前面加“意见：”）; 2:有态度或有内容，有附件
            String relateAttr = String.valueOf(comment.getRelateInfo());
            boolean relate = false;
            if (Strings.isEmpty(relateAttr) || "[]".equals(relateAttr) || "null".equals(relateAttr)) {
                relate = true;
            }
            int opinionAtt = (relate || opinionType == 0) ? -1 : (Strings.isBlank(opinionContentAll) && opinionAttitude == -1 ? 1 : 2);

            //有内容，有附件：减少4个字节
            int deviation = opinionAtt == 2 ? -4 : 0;

            String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(opinionContentAll), deviation);


            //TODO 如果是发起者，意见对发起者隐藏要做单独处理
            for (CtpTrackMember trackMember : trackMembers) {
                //当前用户设置了跟踪是不会发消息的
                if (user.getId() != null && user.getId().equals(trackMember.getMemberId())) {
                    continue;
                }
                Long affairId = trackMember.getAffairId();
                Long recieverMemberId = trackMember.getMemberId();
                Long transactorId = trackMember.getTransactorId();
                if (transactorId != null && isColProxy(recieverMemberId, transactorId)) {
                    recieverMemberId = transactorId;
                }
                String messageUrl = "message.link.col.pending";
                if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackMember.getAffairState())) {
                    messageUrl = "message.link.col.waiSend";
                }
                if (!filterMembers.contains(recieverMemberId) && !Long.valueOf(AppContext.currentUserId()).equals(recieverMemberId)) {
                    filterMembers.add(recieverMemberId);
                    MessageReceiver receiver = new MessageReceiver(affairId, recieverMemberId, messageUrl, affairId, opinionId);
                    receiver.setTrack(true);
                    //receiver.setReply(true);
                    receiversMap.put(recieverMemberId, receiver);
                }
            }

            for (Long[] push : pushMemberIds) {
                if (!filterMembers.contains(push[1]) && !Long.valueOf(AppContext.currentUserId()).equals(push[1])) {
                    filterMembers.add(push[1]);
                    MessageReceiver receiver = getMessageReceiver(push, "message.link.col.pending", opinionId, summaryId);
                    if (receiver != null) {
                        receiver.setAt(true);
                        //receiver.setReply(true);
                    }
                    receiversMap.put(push[1], receiver);
                } else if (filterMembers.contains(push[1])) {
                    MessageReceiver existReceiver = receiversMap.get(push[1]);
                    if (null != existReceiver) {
                        existReceiver.setAt(true);
                        //existReceiver.setReply(true);
                        receiversMap.put(push[1], existReceiver);
                    }
                }
            }


            mc = new MessageContent(messageKey, theMember.getName(), affair.getSubject(), forwardMemberFlag, forwardMember, opinionType, opinionContent, opinionAttitude,
                    opinionAtt, (comment.getPraiseToSummary() != null && comment.getPraiseToSummary()) ? 1 : 0);
            mc.setImportantLevel(affair.getImportantLevel());
            if (null != affair.getTempleteId()) {
                mc.setTemplateId(affair.getTempleteId());
            }
            if (opinionType != 0) {
                mc.setBody(opinionContentAll, Constants.EDITOR_TYPE_HTML, new Date());
            }
            if (!affair.getMemberId().equals(user.getId()) && !filterMembers.contains(affair.getMemberId())) {
                filterMembers.add(affair.getMemberId());
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.done.detail", affair.getId(), opinionId);
                receiver.setReply(true);
                receiversMap.put(affair.getMemberId(), receiver);
            }
            //代理人处理
            if (!affair.getMemberId().equals(user.getId())) {
                String proxyName = user.getName();
                mc.add("col.agent.deal", proxyName);
            }


            receivers.addAll(receiversMap.values());

        } catch (Exception e) {
            LOG.error("发送消息异常", e);
        }


        userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration, theMember.getId(), receivers, importantLevel);

        if (OverTimeflag) {
            //督办发消息
            List<MessageReceiver> superviseReceivers = new ArrayList<MessageReceiver>();
            CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
            if (superviseDetail != null) {
                List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
                for (CtpSupervisor colSupervisor : colSupervisorSet) {
                    Long colSupervisMemberId = colSupervisor.getSupervisorId();
                    if (!filterMembers.contains(colSupervisMemberId)) {
                        //流程之外的督办人需要通过督办查询
                        superviseReceivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId, "message.link.col.supervise", summaryId));

                        filterMembers.add(colSupervisMemberId);
                    }
                }
            }


            if (Strings.isNotEmpty(superviseReceivers)) {
                userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration, theMember.getId(), superviseReceivers, ColMessageFilterEnum.supervise.key);
            }
        }
        return true;
    }

    /**
     * 判断是否给代理人发送消息.可能已经取消代理，或者代理过期了，这种情况就不发消息了
     * @param affairMemberId  : affair的memberID
     * @param affairTransactorId : affair.TransactorId affair的代理人的ID
     * @return
     */
    public boolean isColProxy(Long affairMemberId, Long affairTransactorId) {
        //我设置了XX给我干活，返回他的Id
        Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.ordinal(), affairMemberId);

        if (agentId != null && agentId.equals(affairTransactorId)) {
            return true;
        }
        return false;
    }

    //给在竞争执行中被取消的affair发送消息提醒
    public Boolean transCompetitionCancel(WorkItem workitem, List<CtpAffair> affairs, CtpAffair ctpAffair) {
        CtpAffair affair = affairs.get(0);
        Integer importantLevel = ColUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        String userName = "";
        Long userId = null;
        if (user != null) {
            userName = user.getName();
            userId = user.getId();
        }
        if (userId == null || Strings.isBlank(userName)) {
            return false;
        }
        String forwardMemberId = affair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        try {
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }
            //节点中的人员
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            //代理人集合
            Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();

            List<Long> memberIds = new ArrayList<Long>();
            for (CtpAffair affair2 : affairs) {

                if (Integer.valueOf(StateEnum.col_done.getKey()).equals(affair2.getState())) {
                    // 竞争执行， 一般的affair不再发送消息， 主要是为了节点按比例执行
                    continue;
                }

                //不用删除定时任务了，定时任务里面已经做了判断，被竞争掉的不执行了
                //获取代理人，发消息时给代理发
                Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair2.getMemberId());
                if (agentId != null && !userId.equals(agentId)) {
                    //代理人不在节点中，则发送消息的时候添加代理人
                    //OA-181982：竞争执行节点中共没有设置代理的节点处理：代理人收到消息竞争执行消息 ，被代理人没有收到消息。 应该拿代理人id做逻辑处理 by zhengchao at 2019年6月6日17点04分
                    if (!memberIds.contains(agentId)) {
                        receivers1.add(new MessageReceiver(affair2.getId(), agentId));
                        memberIds.add(agentId);
                    }
                }
                if (!affair2.getMemberId().equals(userId) && !memberIds.contains(affair2.getMemberId())) {
                    receivers.add(new MessageReceiver(affair2.getId(), affair2.getMemberId()));
                    memberIds.add(affair2.getMemberId());
                }
            }

            V3xOrgMember member = null;
            //是否是代理人
            boolean hasProxy = false;
            //判断是否是代理人
            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), ctpAffair.getMemberId());
            if (!userId.equals(ctpAffair.getMemberId()) && userId.equals(agentId)) {
                hasProxy = true;
                member = getMemberById(orgManager, ctpAffair.getMemberId());
            }
            //代理人处理发送消息
            if (hasProxy) {
                String proxyName = member.getName();
                MessageContent content = new MessageContent("col.competition", affair.getSubject(),
                        proxyName, forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, member.getId(), receivers, importantLevel);
                //给代理人发消息
                if (receivers1 != null && receivers1.size() != 0) {
                    MessageContent agentContent = new MessageContent("col.competition", affair.getSubject(),
                            proxyName, forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName())
                            .add("col.agent").setImportantLevel(ctpAffair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        agentContent.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, member.getId(), receivers1, importantLevel);
                }
            } else {
                MessageContent content = new MessageContent("col.competition", affair.getSubject(),
                        userName, forwardMemberFlag, forwardMember).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers, importantLevel);
                //给代理人发消息
                if (receivers1 != null && receivers1.size() != 0) {
                    MessageContent agentContent = new MessageContent("col.competition", affair.getSubject(),
                            userName, forwardMemberFlag, forwardMember)
                            .add("col.agent").setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        agentContent.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers1, importantLevel);
                }
            }
        } catch (Exception e) {
            LOG.error("发送消息异常", e);
        }
        return true;
    }

    //给在督办中被删除的affair发送消息提醒
    public Boolean superviseDelete(WorkItem workitem, List<CtpAffair> affairs) {
        if (affairs != null && !affairs.isEmpty()) {
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();

            Set<MessageReceiver> agentReceivers = new HashSet<MessageReceiver>();
            List<Long> members = new ArrayList<Long>();

            CtpAffair firstAffair = affairs.get(0);
            Integer importantLevel = WFComponentUtil.getImportantLevel(firstAffair);
            User user = AppContext.getCurrentUser();

            String userName = ColUtil.getAccountName();

            String forwardMemberId = firstAffair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            try {
                if (Strings.isNotBlank(forwardMemberId)) {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                }
                for (CtpAffair affair : affairs) {
                    //代理人
                    Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair.getMemberId());
                    //如果当前用户是代理人
                    if (user.getId().equals(agentId) && agentId != null && !members.contains(affair.getMemberId())) {
                        agentReceivers.add(new MessageReceiver(affair.getId(), affair.getMemberId()));
                        members.add(affair.getMemberId());
                        V3xOrgMember member = null;
                        member = getMemberById(orgManager, affair.getMemberId());
                        String proxyName = member.getName();
                        if (!agentReceivers.isEmpty()) {
                            MessageContent content = new MessageContent("col.delete", firstAffair.getSubject(), proxyName, forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName())
                                    .setImportantLevel(firstAffair.getImportantLevel());
                            if (null != firstAffair.getTempleteId()) {
                                content.setTemplateId(firstAffair.getTempleteId());
                            }
                            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), agentReceivers, importantLevel);
                        }
                    } else if (!members.contains(affair.getMemberId())) {  //如果当然人其他非代理人
                        receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId()));
                        members.add(affair.getMemberId());
                    }
                }
                if (!receivers.isEmpty()) {
                    MessageContent agentContent = new MessageContent("col.delete", firstAffair.getSubject(),
                            userName, forwardMemberFlag, forwardMember).setImportantLevel(firstAffair.getImportantLevel());
                    if (null != firstAffair.getTempleteId()) {
                        agentContent.setTemplateId(firstAffair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
                }
            } catch (Exception e) {
                LOG.error("发送消息异常", e);
            }
        }
        return true;
    }

    //给在终止操作中被取消的affair发送消息提醒
    public Boolean terminateCancel(WorkItem workitem, CtpAffair currentAffair, List<CtpAffair> trackingAndPendingAffairs, MessageCommentParam mc, List<Long[]> pushMsgMemberList) {
        if (currentAffair == null) {
            return false;
        }
        if (currentAffair.getSenderId().equals(currentAffair.getMemberId())
                && (currentAffair.getSubObjectId() == null || currentAffair.getSubObjectId().longValue() == -1)) {//从已发中终止，作为系统自动终止的一个标志
            LOG.debug("协同自动终止逾期自由流程，不需要再发终止消息!");
        } else {
            Integer importantLevel = ColUtil.getImportantLevel(currentAffair);
            User user = AppContext.getCurrentUser();
            String forwardMemberId = currentAffair.getForwardMember();
            String opinionId = String.valueOf(mc.getOpinionId());
            String opinionContent = UserMessageUtil.getComment4Message(mc.getOpinion());
            int opinionType = mc.getIsHidden() ? 0 : Strings.isBlank(opinionContent) ? -1 : 1;
            int forwardMemberFlag = 0;
            String forwardMember = null;
            try {
                if (Strings.isNotBlank(forwardMemberId)) {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                }
                Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
                List<Long> members = new ArrayList<Long>();
                for (CtpAffair affair : trackingAndPendingAffairs) {
                    //当前处理者不需要收到终止消息提醒
                    if (!user.getId().equals(affair.getMemberId()) && affair.getSenderId().longValue() != affair.getMemberId().longValue() && !affair.isDelete()) {
                        MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.done.detail", affair.getId(), opinionId);
                        if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
                            receiver.setTrack(true);
                        }
                        receiversMap.put(affair.getMemberId(), receiver);
                        members.add(affair.getMemberId());
                    }
                }
                CtpAffair senderAffair = affairManager.getSenderAffair(currentAffair.getObjectId());
                if (!members.contains(senderAffair.getMemberId())) {
                    receiversMap.put(senderAffair.getMemberId(), new MessageReceiver(senderAffair.getId(), senderAffair.getMemberId(), "message.link.col.done.detail", senderAffair.getId(), opinionId));
                    members.add(senderAffair.getMemberId());
                }

                // 督办者发送消息
                Map<Long, MessageReceiver> onlySuperviseRecvsMap = new HashMap<Long, MessageReceiver>();
                CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(currentAffair.getObjectId());
                if (superviseDetail != null) {
                    List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
                    for (CtpSupervisor colSupervisor : colSupervisorSet) {
                        Long colSupervisMemberId = colSupervisor.getSupervisorId();
                        if (!members.contains(colSupervisMemberId) && !colSupervisMemberId.equals(user.getId())) {
//                        	receivers.add(new MessageReceiver(currentAffair.getId(), colSupervisMemberId));
//                        	members.add(colSupervisMemberId);
                            onlySuperviseRecvsMap.put(colSupervisMemberId, new MessageReceiver(currentAffair.getId(), colSupervisMemberId));
                            members.add(colSupervisMemberId);
                        }
                    }
                }
                List<Long[]> pushMemberIds = pushMsgMemberList;
                for (Long[] push : pushMemberIds) {
                    if (!members.contains(push[1])) {
                        MessageReceiver receiver = new MessageReceiver(push[0], push[1], "message.link.col.done", push[0], opinionId);
                        receiver.setAt(true);
                        receiversMap.put(push[1], receiver);
                        members.add(push[1]);
                    } else {
                        MessageReceiver existReceiver = receiversMap.get(push[1]);
                        if (null != existReceiver) {
                            existReceiver.setAt(true);
                            receiversMap.put(push[1], existReceiver);
                        } else {
                            existReceiver = onlySuperviseRecvsMap.get(push[1]);
                            if (null != existReceiver) {
                                existReceiver.setAt(true);
                                onlySuperviseRecvsMap.put(push[1], existReceiver);
                            }
                        }

                    }
                }
                Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                receivers.addAll(receiversMap.values());

                Set<MessageReceiver> onlySuperviseRecvs = new HashSet<MessageReceiver>();
                onlySuperviseRecvs.addAll(onlySuperviseRecvsMap.values());
                V3xOrgMember m = orgManager.getMemberById(currentAffair.getMemberId());

                String memberName = ColUtil.getAccountName();

                if (!user.isAdmin() && !user.getId().equals(currentAffair.getMemberId())) {  //由代理人终止
                    V3xOrgMember member = getMemberById(orgManager, currentAffair.getMemberId());
                    MessageContent content = new MessageContent("col.terminate", currentAffair.getSubject(),
                            member.getName(), forwardMemberFlag, forwardMember, opinionType, Strings.toText(opinionContent)).add("col.agent.deal", user.getName()).setImportantLevel(currentAffair.getImportantLevel());
                    if (null != currentAffair.getTempleteId()) {
                        content.setTemplateId(currentAffair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, m.getId(), receivers, importantLevel);
                    if (onlySuperviseRecvs.size() > 0) {
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, m.getId(), onlySuperviseRecvs, ColMessageFilterEnum.supervise.key);
                    }
                } else {
                    MessageContent content = new MessageContent("col.terminate", currentAffair.getSubject(),
                            memberName, forwardMemberFlag, forwardMember, opinionType, Strings.toText(opinionContent)).setImportantLevel(currentAffair.getImportantLevel());
                    if (null != currentAffair.getTempleteId()) {
                        content.setTemplateId(currentAffair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
                    if (onlySuperviseRecvs.size() > 0) {
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), onlySuperviseRecvs, ColMessageFilterEnum.supervise.key);
                    }
                }
            } catch (Exception e) {
                LOG.error("发送消息异常", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean sendMessage4Transfer(User user, ColSummary summary, List<CtpAffair> affairs, V3xOrgMember oldAffairMember, Comment opinion) throws BusinessException {
        try {
            Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
            List<String> names = new ArrayList<String>();
            Map<Long, MessageReceiver> receivers1Map = new HashMap<Long, MessageReceiver>();
            //@发送消息需要排除的人员集合
            Set<Long> members = new HashSet<Long>();
            for (CtpAffair c : affairs) {
                names.add(orgManager.getMemberById(c.getMemberId()).getName());
                receiversMap.put(c.getMemberId(), new MessageReceiver(c.getId(), c.getMemberId(), "message.link.col.pending", c.getId(), ""));
                members.add(c.getMemberId());
                //代理人
                Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), c.getMemberId());
                if (agentId != null) {
                    receivers1Map.put(agentId, new MessageReceiver(c.getId(), agentId, "message.link.col.pending", c.getId(), ""));
                    members.add(agentId);
                }
            }

            String bodyType = summary.getBodyType();
            Date bodyCreateDate = summary.getStartDate();
            Integer important = summary.getImportantLevel();
            String forwardMemberId = summary.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }

            V3xOrgMember sender = orgManager.getMemberById(summary.getStartMemberId());

            Object[] transObjs = new Object[]{summary.getSubject(), sender.getName(), forwardMemberFlag, forwardMember, 1,
                    oldAffairMember.getName(), ChangeType.Transfer.getKey()};
            //跟踪人
            Map<Long, MessageReceiver> trackRecieversMap = getTrackReceivers(user, summary.getId());

            Iterator<Map.Entry<Long, MessageReceiver>> receiverIterator = trackRecieversMap.entrySet().iterator();
            while (receiverIterator.hasNext()) {
                Map.Entry<Long, MessageReceiver> receiver = receiverIterator.next();
                Long key = receiver.getKey();
                //过滤移交的人的多余消息
                if (affairs.get(0).getMemberId().equals(key)) {
                    receiverIterator.remove();
                }
                members.add(key);
            }

            Set<MessageReceiver> pushRecievers = new HashSet<MessageReceiver>();
            //获取@人员列表
            List<Long[]> pushMemberIds = opinion.getPushMessageToMembersList();
            for (Long[] push : pushMemberIds) {
                if (!user.getId().equals(push[1]) && !members.contains(push[1])) {
                    MessageReceiver reviever = getMessageReceiver(push, "message.link.col.pending", opinion.getId().toString(), summary.getId());
                    if (reviever != null) {
                        reviever.setAt(true);
                    }
                    pushRecievers.add(reviever);
                } else if (members.contains(push[1])) {
                    MessageReceiver existReceiver = receiversMap.get(push[1]);
                    if (null != existReceiver) {
                        existReceiver.setAt(true);
                        receiversMap.put(push[1], existReceiver);
                    } else {
                        existReceiver = receivers1Map.get(push[1]);
                        if (null != existReceiver) {
                            existReceiver.setAt(true);
                            receivers1Map.put(push[1], existReceiver);
                        } else {
                            existReceiver = trackRecieversMap.get(push[1]);
                            if (null != existReceiver) {
                                existReceiver.setAt(true);
                                trackRecieversMap.put(push[1], existReceiver);
                            }
                        }
                    }
                }
            }
            String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(opinion.getContent()), 0);
            Integer praiseType = 0;
            if (null == opinion.getPraiseToSummary()) {
                praiseType = 0;
            } else {
                praiseType = opinion.getPraiseToSummary() ? 1 : 0;
            }
            if (Strings.isNotBlank(opinionContent)) {
                opinionContent = ResourceUtil.getString("collaboration.node.affair.transfer", opinionContent, praiseType);
            }
            List<Map> list_attach = opinion.getAttachList();
            if (Strings.isNotEmpty(list_attach) && list_attach.size() >= 1) {
                opinionContent = ResourceUtil.getString("collaboration.node.affair.transfer1", opinionContent);
            }

            //消息接收人处理
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());
            Set<MessageReceiver> trackRecievers = new HashSet<MessageReceiver>();
            trackRecievers.addAll(trackRecieversMap.values());
            Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();
            receivers1.addAll(receivers1Map.values());
            //代理人处理时   发送消息
            if (!user.getId().equals(oldAffairMember.getId())) {
                MessageContent content = new MessageContent("col.send", transObjs).add("col.agent.deal", user.getName()).setBody("", bodyType, bodyCreateDate).setImportantLevel(important);
                if (null != summary && null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, ColUtil.getImportantLevel(summary));
                if (receivers1.size() > 0) {
                    MessageContent content1 = new MessageContent("col.send", transObjs).add("col.agent.deal", user.getName()).add("col.agent").setBody("", bodyType, bodyCreateDate).setImportantLevel(important);
                    if (null != summary && null != summary.getTempleteId()) {
                        content.setTemplateId(summary.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.collaboration, user.getId(), receivers1, ColUtil.getImportantLevel(summary));
                }
                MessageContent trackContent = new MessageContent("node.affair.transfer2", summary.getSubject(), oldAffairMember.getName(),
                        StringUtils.join(names.iterator(), ","), opinionContent).add("col.agent.deal", user.getName()).setImportantLevel(summary.getImportantLevel());
                if (null != summary && null != summary.getTempleteId()) {
                    trackContent.setTemplateId(summary.getTempleteId());
                }
                if (Strings.isNotEmpty(trackRecievers)) {
                    userMessageManager.sendSystemMessage(trackContent, ApplicationCategoryEnum.collaboration, user.getId(), trackRecievers, summary.getImportantLevel());
                }
                if (pushRecievers.size() > 0) {
                    userMessageManager.sendSystemMessage(trackContent, ApplicationCategoryEnum.collaboration, user.getId(), pushRecievers, summary.getImportantLevel());
                }
            } else {
                MessageContent content = new MessageContent("col.send", transObjs).setBody("", bodyType, bodyCreateDate).setImportantLevel(important);
                if (null != summary && null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, ColUtil.getImportantLevel(summary));
                if (receivers1.size() > 0) {
                    MessageContent agentContent = new MessageContent("col.send", transObjs).add("col.agent").setBody("", bodyType, bodyCreateDate)
                            .setImportantLevel(important);
                    if (null != summary && null != summary.getTempleteId()) {
                        agentContent.setTemplateId(summary.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, user.getId(), receivers1, ColUtil.getImportantLevel(summary));
                }
                if (Strings.isNotEmpty(trackRecievers) || Strings.isNotEmpty(pushRecievers)) {
                    MessageContent trackContent = new MessageContent("node.affair.transfer2", summary.getSubject(), user.getName(),
                            StringUtils.join(names.iterator(), ","), opinionContent).setImportantLevel(summary.getImportantLevel());
                    if (null != summary && null != summary.getTempleteId()) {
                        trackContent.setTemplateId(summary.getTempleteId());
                    }
                    if (Strings.isNotEmpty(trackRecievers)) {
                        userMessageManager.sendSystemMessage(trackContent, ApplicationCategoryEnum.collaboration, user.getId(), trackRecievers, summary.getImportantLevel());
                    }
                    if (Strings.isNotEmpty(pushRecievers)) {
                        userMessageManager.sendSystemMessage(trackContent, ApplicationCategoryEnum.collaboration, user.getId(), pushRecievers, summary.getImportantLevel());
                    }
                }
            }
        } catch (BusinessException e) {
            LOG.error("转办发送消息报错！", e);
        }
        return true;
    }

    /**
     * 获取跟踪人消息接收人员
     * @param user
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    private Map<Long, MessageReceiver> getTrackReceivers(User user, Long summaryId) throws BusinessException {

        Map<Long, MessageReceiver> receivers = new HashMap<Long, MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        List<CtpTrackMember> trackingMemberList = getTrackingMemberList(members, summaryId, true);
        for (CtpTrackMember trackingMember : trackingMemberList) {
            //过滤掉当前登陆人,不给当前人发送消息
            if (user.getId().equals(trackingMember.getMemberId())) {
                continue;
            }
            String messageUrl = "message.link.col.done.detail";
            if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackingMember.getAffairState())) {
                messageUrl = "message.link.col.waiSend";
            }
            MessageReceiver receiver = new MessageReceiver(trackingMember.getAffairId(), trackingMember.getMemberId(), messageUrl, trackingMember.getAffairId().toString());
            receiver.setTrack(true);
            receivers.put(trackingMember.getMemberId(), receiver);
        }
        return receivers;
    }

    //加签消息提醒
    public Boolean insertPeopleMessage(AffairManager affairManager, UserMessageManager userMessageManager,
                                       OrgManager orgManager, List<String> partyNames, ColSummary summary, CtpAffair affair) {
        if (summary == null || affair == null) {
            return false;
        }
        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);

        User user = AppContext.getCurrentUser();
        try {
            Map<Long, MessageReceiver> receiversMap = getTrackReceivers(user, summary.getId());
            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                try {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                } catch (Exception e) {
                    LOG.error("", e);
                    return false;
                }
            }
            //督办人, 消息清理不给督办人发送消息
            /*SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
            CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summary.getId());
            if(superviseDetail != null){
                List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
                for(CtpSupervisor colSupervisor : colSupervisorSet){
                    Long colSupervisMemberId = colSupervisor.getSupervisorId();
                    if (!members.contains(colSupervisMemberId)) {
                        receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId,"message.link.col.supervise", summary.getId()));
                    }
                }
            }*/
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());
            if (!user.getId().equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                MessageContent content = new MessageContent("col.addAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember).add("col.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
            } else {
                MessageContent agentContent = new MessageContent("col.addAssign", summary.getSubject(), user.getName(),
                        StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    agentContent.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }


    //减签消息提醒
    public Boolean deletePeopleMessage(AffairManager affairManager, OrgManager orgManager,
                                       UserMessageManager userMessageManager, List<String> partyNames, ColSummary summary, CtpAffair affair) {
        if (summary == null || affair == null) {
            return false;
        }
        Integer importantLevel = ColUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        try {
            Map<Long, MessageReceiver> receiversMap = getTrackReceivers(user, summary.getId());
            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                try {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                } catch (Exception e) {
                    LOG.error("", e);
                    return false;
                }
            }
            //督办人
//            SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
//            CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summary.getId());
//            if(superviseDetail != null){
//                List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
//                for(CtpSupervisor colSupervisor : colSupervisorSet){
//                    Long colSupervisMemberId = colSupervisor.getSupervisorId();
//                    if (!members.contains(colSupervisMemberId)) {
//                        receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId,"message.link.col.supervise", summary.getId()));
//                    }
//                }
//            }
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());

            if (!user.getId().equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                MessageContent content = new MessageContent("col.decreaseAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .add("col.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
            } else {
                MessageContent agentContent = new MessageContent("col.decreaseAssign", summary.getSubject(),
                        user.getName(), StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    agentContent.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }


    //回退消息提醒
    public Boolean reMeToRegoMessage(CtpAffair affair, Map<Long, Long> canceledAffairMap) {
        try {
            Integer msgFilter = WFComponentUtil.getImportantLevel(affair);
            User user = AppContext.getCurrentUser();
            Long userId = null;
            String userName = null;
            if (user != null) {
                userId = user.getId();
                userName = user.getName();
            }
            if (userId == null) {
                return false;
            }
           /* String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if(Strings.isNotBlank(forwardMemberId)){
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }*/

            Set<Long> filterMember = new HashSet<Long>();
            //给所有待办事项发起协同被回退消息提醒
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            //代理人集合
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();

            //回退的时候其他影响的节点，比如兄弟节点。
            for (Long key : canceledAffairMap.keySet()) {
                //过滤掉当前登陆人,不给当前人发送消息
                if (userId.equals(key)) {
                    continue;
                }
                //不给已发的人重复发
                if (!filterMember.contains(key) && !userId.equals(key)) {
                    Long affairId = canceledAffairMap.get(key);
                    receivers.add(new MessageReceiver(affairId, key));
                    filterMember.add(key);
                    //代理
                    Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), key);
                    if (agentId != null && !filterMember.contains(key) && !userId.equals(agentId)) {
                        receivers1.add(new MessageReceiver(affairId, agentId));
                        filterMember.add(agentId);
                    }
                }
            }

            Integer optType = 1;//处理
            if (Integer.valueOf(StateEnum.col_sent.ordinal()).equals(affair.getState()) || Integer.valueOf(StateEnum.col_sent.ordinal()).equals(affair.getState())) {
                optType = 0;
            }

            //代理人处理时   发送消息
            if (!userId.equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                //String proxyName = member.getName();
                MessageContent content = new MessageContent("collaboration.message.remetorego", userName, optType, affair.getSubject()).add("col.agent.deal", userName).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, msgFilter);
                //给代理人发送消息
                if (receivers1 != null && receivers1.size() > 0) {
                    MessageContent agentContent = new MessageContent("collaboration.message.remetorego", userName, optType, affair.getSubject()).add("col.agent.deal", userName).add("col.agent").setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        agentContent.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers1, msgFilter);
                }
            } else {

                MessageContent content = new MessageContent("collaboration.message.remetorego", userName, optType, affair.getSubject()).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                //当前用户处理自己的事项时    发送消息
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, receivers, msgFilter);
                //给代理人发送消息
                if (receivers1 != null && receivers1.size() > 0) {
                    MessageContent agentContent = new MessageContent("collaboration.message.remetorego", userName, optType, affair.getSubject(), userName).add("col.agent").setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        agentContent.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, userId, receivers1, msgFilter);
                }
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }

    //回退消息提醒
    public Boolean stepBackMessage(CtpAffair affair, Long summaryId, Comment signOpinion, boolean traceFlag,
                                   boolean msg2Sender, Map<Long, Long> canceledMIdToAIdMap, Map<String, Object> businessData) {
        try {
            List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
            trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, affair.getMemberId());
            Integer importantLevel = ColUtil.getImportantLevel(affair);
            User user = AppContext.getCurrentUser();
            Long userId = null;
            String userName = null;
            if (user != null) {
                userId = user.getId();
                userName = user.getName();
            }
            if (userId == null) {
                return false;
            }
            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }
            //给所有待办事项发起协同被回退消息提醒
            Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
            //代理人集合
            Map<Long, MessageReceiver> receivers1Map = new HashMap<Long, MessageReceiver>();
            Set<Long> filterMember = new HashSet<Long>();

            //1.回退以后的代办节点，基本上是当前节点父节点。
            List<CtpAffair> assignedAffairs = (List<CtpAffair>) businessData.get(WorkFlowEventListener.ASSIGNED_AFFAIRS);
            if (Strings.isNotEmpty(assignedAffairs)) {
                for (CtpAffair assignedAffair : assignedAffairs) {
                    Long assignedMemberId = assignedAffair.getMemberId();
                    //过滤掉当前登陆人,不给当前人发送消息
                    if (userId.equals(assignedMemberId)) {
                        continue;
                    }
                    Long affId = assignedAffair.getId();
                    if (!filterMember.contains(assignedMemberId)) {
                        receiversMap.put(assignedMemberId, new MessageReceiver(affId, assignedMemberId, "message.link.col.pending", affId.toString(), signOpinion.getId()));
                        filterMember.add(assignedMemberId);
                    }

                    Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), assignedMemberId);
                    if (agentId != null && !filterMember.contains(agentId) && !userId.equals(agentId)) {
                        receivers1Map.put(agentId, new MessageReceiver(affId, agentId, "message.link.col.pending", affId, signOpinion.getId()));
                        filterMember.add(agentId);
                    }
                }
            }
            /**
             * 2.发起者 :待发或者已发 ，待发/已发的消息构造    ！！！必须 ！！！  放在生成被回退者消息（步骤1）之后，有固定的顺序关系，不要随意改动   。
             * 解决问题,eg.   a->a1->a1->a2类似这种，a2回退以后，a1的消息链接需要是待办，如果把步骤2的代码放在步骤1前面去，则消息连接是已发。
             */
            for (CtpTrackMember waitOrSentTrackMember : trackMembers) {
                //过滤掉当前登陆人,不给当前人发送消息
                if (userId.equals(waitOrSentTrackMember.getMemberId())) {
                    continue;
                }
                if (waitOrSentTrackMember.getAffairState() == StateEnum.col_waitSend.key() && !filterMember.contains(waitOrSentTrackMember.getSenderId())) {
                    MessageReceiver receiver = new MessageReceiver(waitOrSentTrackMember.getAffairId(), waitOrSentTrackMember.getSenderId(), "message.link.col.waiSend", waitOrSentTrackMember.getAffairId().toString(), signOpinion.getId());
                    receiver.setTrack(true);
                    receiversMap.put(waitOrSentTrackMember.getSenderId(), receiver);
                    filterMember.add(waitOrSentTrackMember.getSenderId());
                    break;
                } else if (waitOrSentTrackMember.getAffairState() == StateEnum.col_sent.key() && !filterMember.contains(waitOrSentTrackMember.getSenderId())) {
                    MessageReceiver receiver = new MessageReceiver(waitOrSentTrackMember.getAffairId(), waitOrSentTrackMember.getSenderId(), "message.link.col.done.detail", waitOrSentTrackMember.getAffairId().toString(), signOpinion.getId());
                    receiver.setTrack(true);
                    receiversMap.put(waitOrSentTrackMember.getSenderId(), receiver);
                    filterMember.add(waitOrSentTrackMember.getSenderId());
                    break;
                }
            }
            //3、跟踪事项
            for (CtpTrackMember _trackMember : trackMembers) {
                //过滤掉当前登陆人,不给当前人发送消息
                if (userId.equals(_trackMember.getMemberId())) {
                    continue;
                }
                if (!filterMember.contains(_trackMember.getMemberId())) {
                    MessageReceiver receiver = new MessageReceiver(_trackMember.getAffairId(), _trackMember.getMemberId());
                    receiver.setTrack(true);
                    receiversMap.put(_trackMember.getMemberId(), receiver);
                    filterMember.add(_trackMember.getMemberId());
                }
                //给代理人 发送消息
                Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), _trackMember.getMemberId());
                if (agentId != null && !filterMember.contains(agentId) && !userId.equals(agentId)) {
                    MessageReceiver receiver = new MessageReceiver(_trackMember.getAffairId(), agentId);
                    receiver.setTrack(true);
                    //判断当前affair的代理人是否仍然是代理人，可能已经被取消了代理人设置。
                    receivers1Map.put(agentId, receiver);
                    filterMember.add(agentId);
                }
            }

            //回退的时候其他影响的节点，比如兄弟节点。
            if (null != canceledMIdToAIdMap && canceledMIdToAIdMap.size() > 0) {
                for (Long key : canceledMIdToAIdMap.keySet()) {
                    //过滤掉当前登陆人,不给当前人发送消息
                    if (userId.equals(key)) {
                        continue;
                    }
                    //不给已发的人重复发
                    if (!filterMember.contains(key)) {
                        Long affairId = canceledMIdToAIdMap.get(key);
                        receiversMap.put(key, new MessageReceiver(affairId, key));
                        filterMember.add(key);
                        //代理
                        Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), key);
                        if (agentId != null && !filterMember.contains(key) && !userId.equals(agentId)) {
                            receivers1Map.put(agentId, new MessageReceiver(affairId, agentId));
                            filterMember.add(agentId);
                        }
                    }
                }
            }
            //督办人
            Set<MessageReceiver> onlySuperviseRecvs = new HashSet<MessageReceiver>();
            CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
            if (superviseDetail != null) {
                List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
                for (CtpSupervisor colSupervisor : colSupervisorSet) {
                    Long colSupervisMemberId = colSupervisor.getSupervisorId();
                    //if (!filterMember.contains(colSupervisMemberId)) {
//                        receivers.add(new MessageReceiver(affair.getId(), colSupervisMemberId,"message.link.col.supervise", summaryId));
//                        filterMember.add(colSupervisMemberId);
                    onlySuperviseRecvs.add(new MessageReceiver(affair.getId(),
                            colSupervisMemberId, "message.link.col.supervise", summaryId));
                    //}
                }
            }

            //给消息推送的发送消息
            List<Long[]> pushMemberIds = signOpinion.getPushMessageToMembersList();
            for (Long[] push : pushMemberIds) {
                if (!user.getId().equals(push[1]) && !filterMember.contains(push[1])) {
                    MessageReceiver receiver = getMessageReceiver(push, "message.link.col.done.detail", signOpinion.getId().toString(), summaryId);
                    if (receiver != null) {
                        receiver.setAt(true);
                    }
                    receiversMap.put(push[1], receiver);
                    filterMember.add(push[1]);
                } else if (filterMember.contains(push[1])) {
                    MessageReceiver existReceiver = receiversMap.get(push[1]);
                    if (null != existReceiver) {
                        existReceiver.setAt(true);
                        receiversMap.put(push[1], existReceiver);
                    } else {
                        existReceiver = receivers1Map.get(push[1]);
                        if (null != existReceiver) {
                            existReceiver.setAt(true);
                            receivers1Map.put(push[1], existReceiver);
                        }
                    }
                }
            }

            //发起人
            long senderId = affair.getSenderId();
            if (msg2Sender) {

                if (!user.getId().equals(senderId) && !filterMember.contains(senderId)) {
                    CtpAffair senderAffair = affairManager.getSenderAffair(summaryId);
                    receiversMap.put(senderId, new MessageReceiver(senderAffair.getId(), senderId, "message.link.col.waiSend", senderAffair.getId(), signOpinion.getId()));
                    filterMember.add(senderId);
                }
            }

            //0-意见隐藏，-1内容为空，1有内容
            int opinionType = Strings.isTrue(signOpinion.isHidden()) ? 0 : Strings.isBlank(signOpinion.getContent()) ? -1 : 1;
            // -1:无附件或意见被隐藏; 1: 无内容（内容前面加“意见：”）; 2:有内容，有附件

            //TODO   附件。暂时提交的时候没有保存附件。
            int opinionAtt = ("[]".equals(signOpinion.getRelateInfo()) || opinionType == 0) ? -1 : (Strings.isBlank(signOpinion.getContent()) ? 1 : 2);

            //有内容，有附件：减少4个字节
            int deviation = opinionAtt == 2 ? -4 : 0;

            String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(signOpinion.getContent()), deviation);

            //给所有待办事项发起协同被回退消息提醒
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());
            //发起人单独处理
            Set<MessageReceiver> sendReceivers = new HashSet<MessageReceiver>();
            sendReceivers.add(receiversMap.get(senderId));

            //代理人集合
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
            receivers1.addAll(receivers1Map.values());

            //代理人处理时   发送消息
            if (!userId.equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                MessageContent content = new MessageContent("collaboration.message.stepBack", affair.getSubject(), proxyName, forwardMemberFlag,
                        forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).add("col.agent.deal", userName).setImportantLevel(affair.getImportantLevel());
                if (null != affair.getTempleteId()) {
                    content.setTemplateId(affair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
                if (onlySuperviseRecvs != null && onlySuperviseRecvs.size() > 0) {
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), onlySuperviseRecvs, ColMessageFilterEnum.supervise.key);
                }
                //给代理人发送消息
                if (receivers1 != null && receivers1.size() > 0) {
                    MessageContent agentContent = new MessageContent("collaboration.message.stepBack", affair.getSubject(), proxyName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).add("col.agent.deal", userName).add("col.agent").setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        agentContent.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers1, importantLevel);
                }
            } else {
                if (traceFlag) {
                    List<Long> _hasTraceDataAffair = (List<Long>) businessData.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
                    if (_hasTraceDataAffair == null) {
                        _hasTraceDataAffair = Collections.emptyList();
                    }

                    Long aId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID);
                    Integer type = (Integer) businessData.get(WFTraceConstants.WFTRACE_TYPE);
                    Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
                    Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();


                    Iterator<MessageReceiver> it = receivers.iterator();
                    while (it.hasNext()) {
                        MessageReceiver mr = it.next();
                        if (_hasTraceDataAffair.contains(mr.getReferenceId())) {
                            if (Strings.isBlank(mr.getLinkType())) {
                                if (null != aId) {
                                    MessageReceiver new_receiver = new MessageReceiver(aId, mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.stepBackRecord.name(), aId.toString(), type + "", signOpinion.getId());
                                    new_receiver.setTrack(true);
                                    new_receivers.add(new_receiver);//回退导致撤销的
                                } else {
                                    MessageReceiver new_receiver = new MessageReceiver(mr.getReferenceId(), mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.stepBackRecord.name(), mr.getReferenceId().toString(), type + "", signOpinion.getId());
                                    new_receiver.setTrack(true);
                                    new_receivers.add(new_receiver);
                                }
                            } else {
                                old_receivers.add(mr);
                            }
                        } else {
                            old_receivers.add(mr);
                        }
                    }


                    ColSummary nowSummary = (ColSummary) businessData.get("ColSummary");
                    String messageSubject = "";
                    if (nowSummary != null) {
                        messageSubject = nowSummary.getSubject();
                    }
                    if (Strings.isEmpty(messageSubject)) {
                        messageSubject = affair.getSubject();
                    }

                    //发起人单独现在发起，后面给其它人发送
                    MessageContent Sendcontent = new MessageContent("collaboration.message.stepBack", messageSubject, userName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).setImportantLevel(affair.getImportantLevel());

                    //以前的流程追溯 是其它地方查询的，现在没有流程追溯的逻辑都只要是回退都可以在已办事项中查看
                    MessageContent content = new MessageContent("collaboration.message.stepBack", messageSubject, userName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).setImportantLevel(affair.getImportantLevel()).add("collaboration.summary.cancel.traceview");
                    if (null != affair.getTempleteId()) {
                        content.setTemplateId(affair.getTempleteId());
                        Sendcontent.setTemplateId(affair.getTempleteId());
                    }
                    //回退发起人不一样
                    if (sendReceivers.size() > 0) {
                        userMessageManager.sendSystemMessage(Sendcontent, ApplicationCategoryEnum.collaboration, userId, sendReceivers, importantLevel);
                        old_receivers.removeAll(sendReceivers);
                    }
                    if (old_receivers.size() > 0) {
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, old_receivers, importantLevel);
                    }
                    //以前的流程追溯 发送去掉
                    MessageContent content1 = new MessageContent("collaboration.message.stepBack", affair.getSubject(), userName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).setImportantLevel(affair.getImportantLevel()).add("collaboration.summary.cancel.traceview");
                    if (null != affair.getTempleteId()) {
                        content1.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.collaboration, userId, new_receivers, importantLevel);

                } else {
                    MessageContent content = new MessageContent("collaboration.message.stepBack", affair.getSubject(), userName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        content.setTemplateId(affair.getTempleteId());
                    }
                    //当前用户处理自己的事项时    发送消息
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, receivers, importantLevel);
                }

                if (onlySuperviseRecvs != null && onlySuperviseRecvs.size() > 0) {
                    MessageContent superviseContent = new MessageContent("collaboration.message.stepBack", affair.getSubject(), userName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        superviseContent.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(superviseContent, ApplicationCategoryEnum.collaboration, userId, onlySuperviseRecvs, ColMessageFilterEnum.supervise.key);
                }

                //给代理人发送消息
                if (receivers1 != null && receivers1.size() > 0) {
                    MessageContent content1 = new MessageContent("collaboration.message.stepBack", affair.getSubject(), userName, forwardMemberFlag,
                            forwardMember, opinionContent, opinionType, opinionAtt, Strings.toText(signOpinion.getContent())).add("col.agent").setImportantLevel(affair.getImportantLevel());
                    if (null != affair.getTempleteId()) {
                        content1.setTemplateId(affair.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.collaboration, userId, receivers1, importantLevel);
                }
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }

    //取回消息提醒
    public Boolean transTakeBackMessage(List<CtpAffair> pendingAffairList, CtpAffair affair, Long summaryId, Map<Long, Long> canceledMidToAidMap) {
        Integer importantLevel = ColUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        String userName = "";
        Long userId = null;
        if (user != null) {
            userName = user.getName();
            userId = user.getId();
            try {
                Set<MessageReceiver> receivers1 = new HashSet<MessageReceiver>();
                V3xOrgMember member = null;
                Long memberId = null;
                Set<Long> utilList = new HashSet<Long>();

                if (canceledMidToAidMap != null) {
                    for (Long key : canceledMidToAidMap.keySet()) {
                        memberId = key;
                        if (user.getId().equals(memberId)) {
                            continue;
                        }
                        member = orgManager.getMemberById(memberId);
                        if (!utilList.contains(memberId)) {
                            utilList.add(memberId);
                            receivers1.add(new MessageReceiver(canceledMidToAidMap.get(key), memberId));
                        }
                    }
                }
                String forwardMemberId = affair.getForwardMember();
                int forwardMemberFlag = 0;
                String forwardMember = null;
                if (Strings.isNotBlank(forwardMemberId)) {
                    try {
                        forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                        forwardMemberFlag = 1;
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                }
                if (affair != null) {
                    if (!userId.equals(affair.getMemberId())) {
                        member = getMemberById(orgManager, affair.getMemberId());
                        String proxyName = member.getName();
                        MessageContent content = new MessageContent("col.takeback", affair.getSubject(), proxyName, forwardMemberFlag, forwardMember)
                                .add("col.agent.deal", userName).setImportantLevel(affair.getImportantLevel());
                        if (null != affair.getTempleteId()) {
                            content.setTemplateId(affair.getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers1, importantLevel);
                    } else {
                        MessageContent content = new MessageContent("col.takeback", affair.getSubject(), userName, forwardMemberFlag, forwardMember)
                                .setImportantLevel(affair.getImportantLevel());
                        if (null != affair.getTempleteId()) {
                            content.setTemplateId(affair.getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, receivers1, importantLevel);
                    }
                    Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                    //优化跟踪表的查询
                    List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
                    trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, affair.getMemberId());
                    for (CtpTrackMember trackMember : trackMembers) {
                        //当前用户设置了跟踪是不会发消息的
                        if (userId != null && userId.equals(trackMember.getMemberId())) {
                            continue;
                        }
                        String messageUrl = "message.link.col.done.detail";
                        if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackMember.getAffairState())) {
                            messageUrl = "message.link.col.waiSend";
                        }
                        if (!utilList.contains(trackMember.getMemberId())) {
                            MessageReceiver receiver = new MessageReceiver(trackMember.getAffairId(), trackMember.getMemberId(), messageUrl, trackMember.getId().toString(), "");
                            receiver.setTrack(true);
                            receivers.add(receiver);
                        }
                    }

                    if (userId != null && !userId.equals(affair.getMemberId())) {
                        member = getMemberById(orgManager, affair.getMemberId());
                        String proxyName = member.getName();
                        MessageContent content = new MessageContent("col.takeback", affair.getSubject(), proxyName, forwardMemberFlag, forwardMember)
                                .add("col.agent.deal", userName).setImportantLevel(affair.getImportantLevel());
                        if (null != affair.getTempleteId()) {
                            content.setTemplateId(affair.getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
                    } else {
                        MessageContent content = new MessageContent("col.takeback", affair.getSubject(), userName, forwardMemberFlag, forwardMember)
                                .setImportantLevel(affair.getImportantLevel());
                        if (null != affair.getTempleteId()) {
                            content.setTemplateId(affair.getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, receivers, importantLevel);
                    }
                }
            } catch (Exception e) {
                LOG.error("send message failed", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 撤销流程发送消息
     * @param affairs
     * @throws BusinessException
     */
    public void sendMessage4Repeal(List<CtpAffair> affairs, CtpAffair currentAffair,
                                   String repealComment, boolean trackFlag, Comment comment, Map<String, Object> businessData) throws BusinessException {
        try {
            User user = AppContext.getCurrentUser();
            if (user == null) {
                return;
            }

            Long userId = user.getId();
            if (userId == null) {
                return;
            }
            String name = ColUtil.getAccountName();
            Integer importantLevel = WFComponentUtil.getImportantLevel(affairs.get(0));
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            //代理人集合
            List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();
            Set<Long> receiverIds = new HashSet<Long>(); //同一个人在这个流程中出现多次，只收一条消息就够了
            for (CtpAffair affair1 : affairs) {
                Long agentMemberId = null;
                if (affair1.isDelete() || receiverIds.contains(affair1.getMemberId())) {
                    continue;
                }
                //如果当前用户是撤销人时，不发送消息
                if (AppContext.getCurrentUser().getId().equals(affair1.getMemberId())) {
                    continue;
                }
                if (affair1.getState() == StateEnum.col_waitSend.key()) {
                    receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId(), "message.link.col.waiSend", affair1.getId().toString(), comment.getId()));
                } else {
                    receivers.add(new MessageReceiver(affair1.getId(), affair1.getMemberId()));
                    //给代理人发送消息
                    agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair1.getMemberId());
                    //如果是代理人处理时 不给代理人发消息
                    if (AppContext.getCurrentUser().getId().equals(agentMemberId)) {
                        continue;
                    }
                    if (agentMemberId != null) {
                        receivers1.add(new MessageReceiver(affair1.getId(), agentMemberId));
                    }
                }
                receiverIds.add(affair1.getMemberId());
            }
            String forwardMemberId = affairs.get(0).getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }
            //获取督办人，给督办人发送消息(不给督办人的代理人发送消息)
            Set<MessageReceiver> onlySuperviseRecvs = new HashSet<MessageReceiver>();
            //Map<Long, MessageReceiver> _superviseBySummaryId = getSuperviseBySummaryId(currentAffair.getObjectId(), currentAffair.getId(), receiverIds);
            //receivers.addAll(_superviseBySummaryId.values());
            Map<Long, MessageReceiver> _superviseBySummaryId = getSuperviseBySummaryId(currentAffair.getObjectId(), currentAffair.getId(), new HashSet<Long>());

            Set<Long> supervisekeySet = _superviseBySummaryId.keySet();
            List needRemove = new ArrayList();
            if (!Strings.isEmpty(supervisekeySet) && !Strings.isEmpty(receiverIds)) {
                Iterator<Long> iterator = supervisekeySet.iterator();
                while (iterator.hasNext()) {
                    Long next = iterator.next();
                    if (receiverIds.contains(next)) {
                        needRemove.add(next);
                    }
                }
            }
            if (!Strings.isEmpty(needRemove)) {
                for (int a = 0; a < needRemove.size(); a++) {
                    _superviseBySummaryId.remove(needRemove.get(a));
                }
            }

            onlySuperviseRecvs.addAll(_superviseBySummaryId.values());
            //如果没有附言或者
            int messageFlag = 0;
            if (Strings.isNotBlank(repealComment)) {
                messageFlag = 1;
            }
            //判断当前是什么时候的撤销，有待办、已发、发起者，如果是待办则显示“意见”，如果是其他则显示“附言”
            String messageLink = "collaboration.summary.cancel";
            if (currentAffair != null && currentAffair.getState().intValue() == StateEnum.col_pending.getKey()) {
                messageLink = "collaboration.summary.cancelPending";
            }

            boolean hasTemplateId = false;
            Long templateId = null;
            if (null != currentAffair) {
                templateId = currentAffair.getTempleteId();
                if (null != templateId) {
                    hasTemplateId = true;
                }
            }
            Long curAgentIDLong = MemberAgentBean.getInstance().getAgentMemberId(
                    ApplicationCategoryEnum.collaboration.ordinal(), currentAffair.getMemberId());
            //代理人处理时发消息
            if (userId != null && !user.isAdmin() && !userId.equals(currentAffair.getMemberId()) && userId.equals(curAgentIDLong)) {

                V3xOrgMember member = getMemberById(orgManager, currentAffair.getMemberId());
                if (null != member) {
                    String proxyName = member.getName();


                    if (!trackFlag) {
                        MessageContent content = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                                .add("col.agent.deal", name).setImportantLevel(currentAffair.getImportantLevel());
                        if (hasTemplateId) {
                            content.setTemplateId(templateId);
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, receivers, importantLevel);
                        if (onlySuperviseRecvs.size() > 0) {
                            MessageContent superviseContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                                    .add("col.agent.deal", name).setImportantLevel(currentAffair.getImportantLevel());
                            if (hasTemplateId) {
                                superviseContent.setTemplateId(templateId);
                            }
                            userMessageManager.sendSystemMessage(superviseContent, ApplicationCategoryEnum.collaboration, userId, onlySuperviseRecvs, 7);
                        }
                    } else {
                        List<Long> traeDataAffair = (List<Long>) businessData.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
                        if (traeDataAffair == null) {
                            traeDataAffair = Collections.emptyList();
                        }

                        Long sId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID);
                        Long aId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID);
                        Integer type = (Integer) businessData.get(WFTraceConstants.WFTRACE_TYPE);
                        Iterator<MessageReceiver> _it = receivers.iterator();
                        Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
                        Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();
                        CtpAffair cloneAffair = affairManager.get(aId);
                        while (_it.hasNext()) {
                            MessageReceiver mr = _it.next();
                            Long referenceId = mr.getReferenceId();
                            if (traeDataAffair.contains(referenceId) && !supervisekeySet.contains(mr.getReceiverId())) {
                                MessageReceiver new_receiver = new MessageReceiver(cloneAffair.getId(), mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.repealRecord.name(), cloneAffair.getId().toString(), type.intValue() + "", comment.getId());
                                new_receiver.setTrack(true);
                                new_receivers.add(new_receiver);
                            } else {
                                old_receivers.add(mr);
                            }
                        }
                        MessageContent content = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag).add("col.agent.deal", name)
                                .setImportantLevel(currentAffair.getImportantLevel());
                        if (hasTemplateId) {
                            content.setTemplateId(templateId);
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, old_receivers, importantLevel);

                        if (onlySuperviseRecvs.size() > 0) {
                            MessageContent superviseContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag).add("col.agent.deal", name)
                                    .setImportantLevel(currentAffair.getImportantLevel());
                            if (hasTemplateId) {
                                superviseContent.setTemplateId(templateId);
                            }
                            userMessageManager.sendSystemMessage(superviseContent, ApplicationCategoryEnum.collaboration, userId, onlySuperviseRecvs, 7);
                        }

                        MessageContent traceContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                                .add("collaboration.summary.cancel.traceview").add("col.agent.deal", name).setImportantLevel(currentAffair.getImportantLevel());
                        if (hasTemplateId) {
                            traceContent.setTemplateId(templateId);
                        }
                        userMessageManager.sendSystemMessage(traceContent, ApplicationCategoryEnum.collaboration, userId, new_receivers, importantLevel);
                    }
                    //给代理人发消息
                    if (receivers1 != null && receivers1.size() != 0) {
                        MessageContent agentContent = new MessageContent(messageLink, affairs.get(0).getSubject(), proxyName, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                                .add("col.agent.deal", name).add("col.agent").setImportantLevel(currentAffair.getImportantLevel());
                        if (hasTemplateId) {
                            agentContent.setTemplateId(templateId);
                        }
                        userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, userId, receivers1, importantLevel);
                    }
                }
            } else {
                //当前人 处理时 发消息
                if (!trackFlag) {
                    MessageContent content = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                            .setImportantLevel(currentAffair.getImportantLevel());
                    if (hasTemplateId) {
                        content.setTemplateId(templateId);
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, receivers, importantLevel);
                    if (onlySuperviseRecvs.size() > 0) {
                        MessageContent superviseContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag).setImportantLevel(currentAffair.getImportantLevel());
                        if (hasTemplateId) {
                            superviseContent.setTemplateId(templateId);
                        }
                        userMessageManager.sendSystemMessage(superviseContent, ApplicationCategoryEnum.collaboration, userId, onlySuperviseRecvs, 7);
                    }
                } else {
                    List<Long> traeDataAffair = (List<Long>) businessData.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
                    if (traeDataAffair == null) {
                        traeDataAffair = Collections.emptyList();
                    }

                    Long sId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_SUMMARYID);
                    Long aId = (Long) businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID);
                    Integer type = (Integer) businessData.get(WFTraceConstants.WFTRACE_TYPE);
                    Iterator<MessageReceiver> _it = receivers.iterator();
                    Set<MessageReceiver> old_receivers = new HashSet<MessageReceiver>();
                    Set<MessageReceiver> new_receivers = new HashSet<MessageReceiver>();
                    Set<MessageReceiver> send_revice = new HashSet<MessageReceiver>();//发起人消息
                    CtpAffair cloneAffair = affairManager.get(aId);
                    while (_it.hasNext()) {
                        MessageReceiver mr = _it.next();
                        Long referenceId = mr.getReferenceId();
                        if (traeDataAffair.contains(referenceId) && !supervisekeySet.contains(mr.getReceiverId())) {
                            MessageReceiver receiver = new MessageReceiver(cloneAffair.getId(), mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.repealRecord.name(), cloneAffair.getId().toString(), type.intValue() + "", comment.getId());
                            receiver.setTrack(true);
                            if (cloneAffair.getSenderId() != null && cloneAffair.getSenderId().equals(mr.getReceiverId())) {
                                send_revice.add(receiver);
                            } else {
                                new_receivers.add(receiver);
                            }


                        } else {
                            old_receivers.add(mr);
                        }
                    }
                    MessageContent content = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                            .add("collaboration.summary.cancel.traceview").setImportantLevel(currentAffair.getImportantLevel());
                    if (hasTemplateId) {
                        content.setTemplateId(templateId);
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, userId, old_receivers, importantLevel);

                    if (onlySuperviseRecvs.size() > 0) {
                        MessageContent superviseContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                                .setImportantLevel(currentAffair.getImportantLevel());
                        if (hasTemplateId) {
                            superviseContent.setTemplateId(templateId);
                        }
                        userMessageManager.sendSystemMessage(superviseContent, ApplicationCategoryEnum.collaboration, userId, onlySuperviseRecvs, 7);
                    }
                    //处理人撤销发送消息-带上已办中查询，如果是发起人的话那么不显示 在已办中查询
                    MessageContent sendContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag).setImportantLevel(currentAffair.getImportantLevel());


                    MessageContent content1 = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                            .add("collaboration.summary.cancel.traceview").setImportantLevel(currentAffair.getImportantLevel());
                    if (hasTemplateId) {
                        content1.setTemplateId(templateId);
                        sendContent.setTemplateId(templateId);
                    }
                    //给发起人发送消息
                    userMessageManager.sendSystemMessage(sendContent, ApplicationCategoryEnum.collaboration, userId, send_revice, importantLevel);
                    //流程中其它人发送消息
                    userMessageManager.sendSystemMessage(content1, ApplicationCategoryEnum.collaboration, userId, new_receivers, importantLevel);
                }
                //给代理人发消息
                if (receivers1 != null && receivers1.size() != 0) {
                    MessageContent agentContent = new MessageContent(messageLink, affairs.get(0).getSubject(), name, repealComment, forwardMemberFlag, forwardMember, messageFlag)
                            .add("col.agent").setImportantLevel(currentAffair.getImportantLevel());
                    if (hasTemplateId) {
                        agentContent.setTemplateId(templateId);
                    }
                    userMessageManager.sendSystemMessage(agentContent,
                            ApplicationCategoryEnum.collaboration, userId, receivers1, importantLevel);
                }
            }

        } catch (Exception e) {
            LOG.error("撤销协同发送提醒消息异常", e);
            throw new BusinessException("send message failed");
        }
    }

    /**
     * 获取修改正文和附件的时候需要发送的人
     * @param user
     * @param affairManager
     * @param summary
     * @param receivers1 代理人集合
     * @return
     * @throws BusinessException
     */
    private Set<MessageReceiver> getMessageReceiver(User user, AffairManager affairManager, Long summaryId) throws BusinessException {
        return getMessageReceiver(user, affairManager, summaryId, null);
    }

    private Set<MessageReceiver> getMessageReceiver(User user, AffairManager affairManager, Long summaryId, CtpAffair _affair) throws BusinessException {
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        //获取当前事项的affair集合
        List<StateEnum> states = new ArrayList<StateEnum>();
        states.add(StateEnum.col_pending);
        states.add(StateEnum.col_done);
        states.add(StateEnum.col_sent);
        List<CtpAffair> affairs = new ArrayList<CtpAffair>();
        if (_affair != null) {
            affairs = affairManager.getAffairs(_affair, states);
        } else {
            affairs = affairManager.getAffairs(summaryId, states);
        }
        //节点人和跟踪节点需要发送消息的人员集合
        Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
        //代理人发消息集合
        List<MessageReceiver> receivers1 = new ArrayList<MessageReceiver>();

        Long sendAffairId = null;
        for (CtpAffair affair : affairs) {
            if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())) {
                sendAffairId = affair.getId();
            }
            if (affair.isDelete()) continue;
            //获取代理人
            Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair.getMemberId());
            //如果当然用户是代理人，则不给当然代理人发送消息，且需要给被代理人发送消息
            if (user.getId().equals(agentMemberId)) {
                if (!receiversMap.containsKey(affair.getMemberId())) {
                    receiversMap.put(affair.getMemberId(), new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending", affair.getId().toString(), ""));
                }
                continue;
            }
            //不给当前操作用户发送消息,如果当前用户有代理人则给代理人发送消息
            if (user.getId().equals(affair.getMemberId())) {
                //给代理人发送消息
                if (agentMemberId != null && !receiversMap.containsKey(agentMemberId)) {
                    receivers1.add(new MessageReceiver(affair.getId(), agentMemberId));
                }
                continue;
            }
            String messageUrl = "";
            if (Integer.valueOf(StateEnum.col_pending.getKey()).equals(affair.getState())) {
                messageUrl = "message.link.col.pending";
            } else if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())) {
                messageUrl = "message.link.col.waiSend";
            } else {
                messageUrl = "message.link.col.done.detail";
            }
            //给已发节点发送消息
            Long sendId = affair.getSenderId();
            if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState()) && !receiversMap.containsKey(affair.getSenderId())) {
                receiversMap.put(sendId, new MessageReceiver(affair.getId(), sendId, messageUrl, affair.getId().toString(), ""));
            } else if (!receiversMap.containsKey(affair.getMemberId())) {
                receiversMap.put(affair.getMemberId(), new MessageReceiver(affair.getId(), affair.getMemberId(), messageUrl,
                        affair.getId().toString()));
                //给代理人发送消息
                if (agentMemberId != null && !receiversMap.containsKey(agentMemberId)) {
                    receivers1.add(new MessageReceiver(affair.getId(), agentMemberId));
                }
            }
        }
        //督办人
        //2016-1消息改造，不给督办人发消息
        //Map<Long, MessageReceiver> receiversSupervise = (getSuperviseBySummaryId(summaryId, sendAffairId, receiversMap.keySet()));
        //receiversMap.putAll(receiversSupervise);
        receivers.addAll(receiversMap.values());
        return receivers;
    }

    /**
     * 根据当前事项查询出督办人并添加到消息对象中
     * @param summaryId
     * @param affairId 给当前的
     * @param receiverIds
     * @return
     * @throws BusinessException
     */
    public Map<Long, MessageReceiver> getSuperviseBySummaryId(Long summaryId, Long affairId, Set<Long> receiverIds) throws BusinessException {
        //督办人
        Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();

        SuperviseManager superviseManager = (SuperviseManager) AppContext.getBean("superviseManager");
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
        if (superviseDetail != null) {
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for (CtpSupervisor colSupervisor : colSupervisorSet) {
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                //只给督办人发送消息，不给督办人的代理人发送消息。（见测试用例）
                if (!receiverIds.contains(colSupervisMemberId) && !receiversMap.containsKey(colSupervisMemberId)) {
                    receiversMap.put(colSupervisMemberId, new MessageReceiver(affairId, colSupervisMemberId));
                }
            }
        }
        return receiversMap;
    }


    /**
     * @param affairManager
     * @param orgManager
     * @param summary
     * @param affair
     * @param type 0 附件 1 正文 2 附件和正文
     * @return
     * @throws BusinessException
     */
    @Override
    public Boolean sendMessage4ModifyBodyOrAtt(ColSummary summary, Long memberId, int type) throws BusinessException {
        return sendMessage4ModifyBodyOrAtt(summary, memberId, type, null);
    }

    public Boolean sendMessage4ModifyBodyOrAtt(ColSummary summary, Long memberId, int type, CtpAffair affair) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Integer importantLevel = ColUtil.getImportantLevel(summary);
        Set<MessageReceiver> receivers = getMessageReceiver(user, affairManager, summary.getId(), affair);
        String forwardMemberId = summary.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        try {
            //代理人处理时发消息
            if (user.getId() != null && !user.isAdmin() && !user.getId().equals(memberId)) {
                V3xOrgMember member = getMemberById(orgManager, memberId);
                String proxyName = member.getName();
                MessageContent content = new MessageContent("col.modifyBodyOrAtt", summary.getSubject(), proxyName, forwardMemberFlag, forwardMember, type)
                        .add("col.agent.deal", user.getName()).setImportantLevel(summary.getImportantLevel());
                if (null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                //给节点中的人员和督办人员发送消息
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            } else {
                MessageContent content = new MessageContent("col.modifyBodyOrAtt", summary.getSubject(), user.getName(), forwardMemberFlag, forwardMember, type)
                        .setImportantLevel(summary.getImportantLevel());
                if (null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                //当前人 处理时 发消息
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            }
        } catch (Exception e) {
            LOG.error("修改协同消息提醒失败", e);
        }
        return true;
    }

    //暂存待办消息提醒
    public Boolean sendMessage4Zcdb(CtpAffair zcdbAffair, Comment opinion) throws BusinessException {
        Integer importantLevel = ColUtil.getImportantLevel(zcdbAffair);
        User user = AppContext.getCurrentUser();
        //获取跟踪人
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(zcdbAffair.getObjectId(), null);
        trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, zcdbAffair.getMemberId());
        List<Long[]> pushMemberIds = opinion.getPushMessageToMembersList();
        Map<Long, MessageReceiver> trackReceiversMap = new HashMap<Long, MessageReceiver>();
        Map<Long, MessageReceiver> pushReceiversMap = new HashMap<Long, MessageReceiver>();
        Set<Long> members = new HashSet<Long>();
        for (CtpTrackMember _trackMember : trackMembers) {
            if (!user.getId().equals(_trackMember.getMemberId())) {
                Long memberId = _trackMember.getMemberId();
                String messageUrl = "message.link.col.pending";
                if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(_trackMember.getAffairState())) {
                    messageUrl = "message.link.col.waiSend";
                }
                if (!members.contains(_trackMember.getMemberId())) {
                    MessageReceiver receiver = new MessageReceiver(_trackMember.getAffairId(), memberId, messageUrl, _trackMember.getAffairId().toString(), opinion.getId());
                    receiver.setTrack(true);
                    //receiver.setReply(true);
                    trackReceiversMap.put(_trackMember.getMemberId(), receiver);
                    members.add(_trackMember.getMemberId());
                }
            }

        }
        for (Long[] push : pushMemberIds) {
            if (!user.getId().equals(push[1]) && !members.contains(push[1])) {
                MessageReceiver receiver = getMessageReceiver(push, "message.link.col.pending", opinion.getId().toString(), opinion.getModuleId());
                if (receiver != null) {
                    receiver.setAt(true);
                    //receiver.setReply(true);
                }
                pushReceiversMap.put(push[1], receiver);
                members.add(push[1]);
            } else if (members.contains(push[1])) {
                MessageReceiver existReceiver = trackReceiversMap.get(push[1]);
                if (null != existReceiver) {
                    existReceiver.setAt(true);
                    trackReceiversMap.put(push[1], existReceiver);
                }
            }
        }

        Set<MessageReceiver> trackReceivers = new HashSet<MessageReceiver>();
        trackReceivers.addAll(trackReceiversMap.values());
        Set<MessageReceiver> pushReceivers = new HashSet<MessageReceiver>();
        pushReceivers.addAll(pushReceiversMap.values());

        String forwardMemberId = zcdbAffair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        //0-意见隐藏，-1内容为空，1有内容
        int opinionType = Strings.isTrue(opinion.isHidden()) ? 0 : Strings.isBlank(opinion.getContent()) ? -1 : 1;
        // -1:无附件或意见被隐藏; 1: 无内容（内容前面加“意见：”）; 2:有内容，有附件

        //TODO   附件。暂时提交的时候没有保存附件。
        int opinionAtt = ("[]".equals(opinion.getRelateInfo()) || opinionType == 0) ? -1 : (Strings.isBlank(opinion.getContent()) ? 1 : 2);

        //有内容，有附件：减少4个字节
        int deviation = opinionAtt == 2 ? -4 : 0;

        String opinionContent = UserMessageUtil.getComment4Message(Strings.toText(opinion.getContent()), deviation);

        /*Long startMemberId = zcdbAffair.getSenderId();
        if(!user.getId().equals(startMemberId) && !members.contains(startMemberId)){
            receivers.add(new MessageReceiver(zcdbAffair.getId(), startMemberId
                ,"message.link.col.done.detail",zcdbAffair.getId().toString(), opinion.getId()));
        }*/
        Integer praiseType = 0;
        if (null == opinion.getPraiseToSummary()) {
            praiseType = 0;
        } else {
            praiseType = opinion.getPraiseToSummary() ? 1 : 0;
        }
        if (!user.getId().equals(zcdbAffair.getMemberId())) {
            V3xOrgMember member = null;
            member = getMemberById(orgManager, zcdbAffair.getMemberId());
            String proxyName = member.getName();

            MessageContent content = new MessageContent("collaboration.opinion.zcdb", zcdbAffair.getSubject(), proxyName, forwardMemberFlag, forwardMember, opinionType, opinionContent, opinionAtt, praiseType)
                    .setImportantLevel(zcdbAffair.getImportantLevel()).add("col.agent.deal", user.getName());
            Long templateId = zcdbAffair.getTempleteId();
            if (null != templateId) {
                content.setTemplateId(templateId);
            }
            if (trackReceivers.size() > 0) {
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, zcdbAffair.getMemberId(), trackReceivers, importantLevel);
            }
            if (pushReceivers.size() > 0) {
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, zcdbAffair.getMemberId(), pushReceivers, importantLevel);
            }

        } else {
            MessageContent content = new MessageContent("collaboration.opinion.zcdb", zcdbAffair.getSubject(), user.getName(), forwardMemberFlag, forwardMember, opinionType, opinionContent, opinionAtt, praiseType)
                    .setImportantLevel(zcdbAffair.getImportantLevel());
            Long templateId = zcdbAffair.getTempleteId();
            if (null != templateId) {
                content.setTemplateId(templateId);
            }
            if (trackReceivers.size() > 0) {
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), trackReceivers, importantLevel);
            }
            if (pushReceivers.size() > 0) {
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), pushReceivers, importantLevel);
            }
        }

        return true;
    }

    //会签消息提醒
    public Boolean colAssignMessage(UserMessageManager userMessageManager, AffairManager affairManager,
                                    OrgManager orgManager, List<String> partyNames, ColSummary summary, CtpAffair affair) {
        if (summary == null || affair == null) {
            return false;
        }
        Integer importantLevel = WFComponentUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        try {
            //跟踪的人
            Map<Long, MessageReceiver> receiversMap = getTrackReceivers(user, summary.getId());
            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                try {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                } catch (Exception e) {
                    LOG.error("", e);
                    return false;
                }
            }
            //跟踪的人
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());

            if (!user.getId().equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                MessageContent proxyContent = new MessageContent("col.colAssign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .add("col.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel());
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    proxyContent.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(proxyContent, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
            } else {
                MessageContent content = new MessageContent("col.colAssign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .setImportantLevel(affair.getImportantLevel());
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    content.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }

    //知会消息提醒
    public Boolean addMoreSignMessage(UserMessageManager userMessageManager, AffairManager affairManager,
                                      OrgManager orgManager, List<String> partyNames, ColSummary summary, CtpAffair affair) {
        if (summary == null || affair == null) {
            return false;
        }
        Integer importantLevel = ColUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        try {
            Map<Long, MessageReceiver> receiversMap = getTrackReceivers(user, summary.getId());

            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                try {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                } catch (Exception e) {
                    LOG.error("", e);
                    return false;
                }
            }

            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());

            if (!user.getId().equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                MessageContent content = new MessageContent("col.addMoreSign", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .add("col.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel());
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    content.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
            } else {
                MessageContent content = new MessageContent("col.addMoreSign", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .setImportantLevel(affair.getImportantLevel());
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    content.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }

    //知会消息提醒
    public Boolean addInformMessage(UserMessageManager userMessageManager, AffairManager affairManager,
                                    OrgManager orgManager, List<String> partyNames, ColSummary summary, CtpAffair affair) {
        if (summary == null || affair == null) {
            return false;
        }
        Integer importantLevel = ColUtil.getImportantLevel(affair);
        User user = AppContext.getCurrentUser();
        try {
            Map<Long, MessageReceiver> receiversMap = getTrackReceivers(user, summary.getId());

            String forwardMemberId = affair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                try {
                    forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                    forwardMemberFlag = 1;
                } catch (Exception e) {
                    LOG.error("", e);
                    return false;
                }
            }

            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            receivers.addAll(receiversMap.values());

            if (!user.getId().equals(affair.getMemberId())) {
                V3xOrgMember member = null;
                member = getMemberById(orgManager, affair.getMemberId());
                String proxyName = member.getName();
                MessageContent content = new MessageContent("col.addInform", summary.getSubject(), proxyName, StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .add("col.agent.deal", user.getName()).setImportantLevel(affair.getImportantLevel());
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    content.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getMemberId(), receivers, importantLevel);
            } else {
                MessageContent content = new MessageContent("col.addInform", summary.getSubject(), user.getName(), StringUtils.join(partyNames.iterator(), ","), forwardMemberFlag, forwardMember)
                        .setImportantLevel(affair.getImportantLevel());
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    content.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, importantLevel);
            }
        } catch (Exception e) {
            LOG.error("send message failed", e);
            return false;
        }
        return true;
    }

    public V3xOrgMember getMemberById(OrgManager orgManager, Long memberId) {
        V3xOrgMember member = null;
        try {
            member = orgManager.getEntityById(V3xOrgMember.class, memberId);
        } catch (BusinessException e) {
            LOG.error("获取协同消息提醒对应人员失败", e);
            return null;
        }
        return member;
    }

    @Override
    public void sendNextPendingNodeMessage(Long mainCaseId, List<Long> nodeIds) {
        try {
            ColSummary summary = (ColSummary) colManager.getSummaryByCaseId(mainCaseId);
            //主流程触发子流程 子流程结束以后主流程更新当前代办人
            ColUtil.updateCurrentNodesInfo(summary);
            colManager.updateColSummary(summary);
            if (null != summary) {
                List<CtpAffair> affairList = affairManager.getPendingAffairs(summary.getId(), nodeIds);
                if (affairList != null && !affairList.isEmpty()) {
                    Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
                    for (CtpAffair affair : affairList) {
                        receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending", affair.getId(), ""));
                    }
                    Integer importantLevel = ColUtil.getImportantLevel(summary);
                    MessageContent content = new MessageContent("collaboration.mainflow.canProcess", summary.getSubject()).setImportantLevel(summary.getImportantLevel());
                    Long templateId = summary.getTempleteId();
                    if (null != templateId) {
                        content.setTemplateId(templateId);
                    }
                    userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, CurrentUser.get().getId(), receivers, importantLevel);
                }
            }
        } catch (Throwable e) {
            LOG.error("send message failed", e);
        }
    }

    @Override
    public Date sendOperationTypeMessage(boolean isImmediateReceipt, String messageDataListJSON, ColSummary summary, CtpAffair affair, Comment comment) {
        return sendOperationTypeMessage(isImmediateReceipt, messageDataListJSON, summary, affair, comment, true);
    }

    public Date insertOperationTypeProcessLog(String messageDataListJSON, ColSummary summary, CtpAffair affair, Comment comment) {
        return sendOperationTypeMessage(false, messageDataListJSON, summary, affair, comment, false);
    }


    private Date sendOperationTypeMessage(boolean isImmediateReceipt, String messageDataListJSON, ColSummary summary, CtpAffair affair, Comment comment, Boolean needSendMessage) {
        List<Map<String, Object>> newMessageDataList = null;
        if (null == messageDataListJSON || "".equals(messageDataListJSON.trim())) {
            newMessageDataList = new ArrayList<Map<String, Object>>();
        } else {
            newMessageDataList = (List<Map<String, Object>>) JSONUtil.parseJSONString(messageDataListJSON);
        }
        Long commentId;
        if (comment == null || comment.getId() == null) {
            commentId = -1L;
        } else {
            commentId = comment.getId();
        }
        Long activityId = affair.getActivityId();
        if (activityId == null) {
            activityId = -1L;
        }
        User user = AppContext.getCurrentUser();
        Date date = DateUtil.newDate();
        boolean hasWfOperation = false;
        for (Map<String, Object> map : newMessageDataList) {
            if (user.getId().toString().equals(map.get("handlerId").toString())) {
                String operationType = map.get("operationType").toString();
                String partyNames = map.get("partyNames").toString();
                List<String> partyNameList = Arrays.asList(partyNames.split("[,]"));
                String processLogParam = map.get("processLogParam").toString();
                boolean showDetail = false;
                String detailInfo = "";
                if (summary.getBodyType() != null && ColUtil.isForm(summary.getBodyType()) && map.get("formOperationPolicy") != null) {
                    String formOperationPolicy = map.get("formOperationPolicy").toString();
                    if ("0".equals(formOperationPolicy)) {
                        detailInfo = ResourceUtil.getString("collaboration.same.node");
                    } else if ("1".equals(formOperationPolicy)) {
                        detailInfo = ResourceUtil.getString("common.readonly");
                    }
                    showDetail = true;
                }

                date = Datetimes.addSecond(date, 1);
                List<ProcessLogDetail> processLogDetails = getProcessLogDetails(map, user);
                if ("insertPeople".equals(operationType)) {//加签
                    if (showDetail) {
                        List<String> processlogNameList = Arrays.asList(processLogParam.split("[,]"));
                        StringBuilder sb = new StringBuilder();
                        for (String str : processlogNameList) {
                            sb.append(str.substring(0, str.length() - 1));
                            sb.append("|").append(detailInfo).append(")");
                            sb.append(",");
                        }
                        String processlogName = sb.toString();
                        processLogParam = processlogName.substring(0, processlogName.length() - 1);
                    }
                    if (needSendMessage) {
                        this.insertPeopleMessage(affairManager, userMessageManager, orgManager, partyNameList, summary, affair);
                    }
                    if (isImmediateReceipt) {//加签并立即收到
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), activityId, ProcessLogAction.insertPeople, commentId, date, processLogDetails, processLogParam, "1");
                    } else {
                        processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), activityId, ProcessLogAction.insertPeople, commentId, date, processLogDetails, processLogParam, "0");
                    }
                    hasWfOperation = true;
                } else if ("deletePeople".equals(operationType)) {
                    if (needSendMessage) {
                        this.deletePeopleMessage(affairManager, orgManager, userMessageManager, partyNameList, summary, affair);
                    }
                    processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), activityId, ProcessLogAction.deletePeople, commentId, date, processLogParam);
                    hasWfOperation = true;
                } else if ("colAssign".equals(operationType)) {
                    this.colAssignMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                    processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), activityId, ProcessLogAction.colAssign, commentId, date, processLogDetails, processLogParam);
                    hasWfOperation = true;
                } else if ("addInform".equals(operationType)) {
                    if (needSendMessage) {
                        this.addInformMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                    }
                    processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), activityId, ProcessLogAction.inform, commentId, date, processLogParam);
                    hasWfOperation = true;
                } else if ("addMoreSign".equals(operationType)) {//多级会签
                    if (needSendMessage) {
                        this.addMoreSignMessage(userMessageManager, affairManager, orgManager, partyNameList, summary, affair);
                    }
                    processLogManager.insertLog(user, Long.parseLong(summary.getProcessId()), activityId, ProcessLogAction.addMoreSign, commentId, date, processLogParam);
                    hasWfOperation = true;
                }
            }
        }

        if (comment != null && hasWfOperation) { // 有操作日志才设置，不然会覆盖到正文和附件的修改日志
            comment.setHasWfOperation(hasWfOperation);
        }

        return date;
    }

    private List<ProcessLogDetail> getProcessLogDetails(Map<String, Object> map, User user) {
        String allMemberNames = (String) map.get("allMemberNames");
        String selectName = (String) map.get("selectMemberName");
        String noMemberMsg = "<font color='red'>" + ResourceUtil.getString("common.none") + ".</font>";
        if (Strings.isBlank(allMemberNames) || Strings.isBlank(selectName) || noMemberMsg.equals(allMemberNames)) {
            return null;
        }
        Map<String, List<String>> logDetailMsg = new HashMap<String, List<String>>();
        List<String> step0 = new ArrayList<String>();
        step0.add("人员匹配结果为:" + allMemberNames);
        logDetailMsg.put(WorkflowMatchLogMessageConstants.step0, step0);

        List<String> step2 = new ArrayList<String>();
        step2.add(ResourceUtil.getString("workflow.match.log.selectPeople", user.getName(), selectName));
        logDetailMsg.put(WorkflowMatchLogMessageConstants.step2, step2);

        ProcessLogDetail detail = new ProcessLogDetail();
        detail.setNodeMsg(JSONUtil.toJSONString(logDetailMsg));
        detail.setNodeName("当前节点");
        detail.setNodeType("加签/当前会签选择范围");
        detail.setMatchSate(1);
        detail.setNewId();

        List<ProcessLogDetail> details = new ArrayList<ProcessLogDetail>();
        details.add(detail);
        return details;
    }

    public void sendMessage2Supervisor(Long superviseId, ApplicationCategoryEnum app, String summarySubject,
                                       String messageKey, long userId, String userName, String repealComment, String forwardMemberIdStr, Long templateId) {
        try {
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            List<MessageReceiver> onlySuperviseRecvs = new ArrayList<MessageReceiver>();
            List<CtpSupervisor> colSupervisors = superviseManager.getSupervisors(superviseId);
            for (CtpSupervisor colSupervisor : colSupervisors) {
                MessageReceiver receiver = new MessageReceiver(superviseId, colSupervisor.getSupervisorId());
                //receivers.add(receiver);
                onlySuperviseRecvs.add(receiver);
            }
            MessageContent msgContent = null;
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberIdStr)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberIdStr)).getName();
                forwardMemberFlag = 1;
            }
            if ("collaboration.summary.cancel".equals(messageKey)) {
                int messageFlag = 0;
                if (Strings.isNotBlank(repealComment)) {
                    messageFlag = 1;
                }
                if (repealComment == null) {
                    repealComment = "";
                }
                msgContent = new MessageContent(messageKey, summarySubject, userName, repealComment, forwardMemberFlag, forwardMember, messageFlag);
            } else if ("col.stepback".equals(messageKey)) {
                int messageFlag = 0;
                if (Strings.isNotBlank(repealComment)) {
                    messageFlag = 1;
                }
                msgContent = new MessageContent(messageKey, summarySubject, userName, forwardMemberFlag, forwardMember, repealComment, messageFlag);
            } else {
                msgContent = new MessageContent(messageKey, summarySubject, userName, repealComment);
            }
            if (null != templateId) {
                msgContent.setTemplateId(templateId);
            }
            userMessageManager.sendSystemMessage(msgContent, app, userId, receivers);
            if (onlySuperviseRecvs.size() > 0) {
                userMessageManager.sendSystemMessage(msgContent, app, userId, onlySuperviseRecvs, ColMessageFilterEnum.supervise.key);
            }
        } catch (BusinessException e) {
            LOG.error("", e);
        }
    }

    /**
     * 判断是否给代理人发送消息.可能已经取消代理，或者代理过期了，这种情况就不发消息了
     * @param affairMemberId  : affair的memberID
     * @param affairTransactorId : affair.TransactorId affair的代理人的ID
     * @return
     */
    private boolean isProxy(Long affairMemberId, Long affairTransactorId) {
        //我设置了XX给我干活，返回他的Id
        /*Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.ordinal(),
                affairMemberId);
        if (agentId.equals(affairTransactorId))
            return true;
        return false;*/
        return true;
    }

    /**
     * 生成待办事项发送消息
     * @param app
     * @param receivers
     * @param receivers1
     * @param sender
     * @param subjects
     * @param importantLevel
     * @param bodyContent
     * @param bodyType
     * @param bodyCreateDate
     * @param userInfoData
     */
    public void sendMessage(AffairData affairData, List<MessageReceiver> receivers, List<MessageReceiver> receivers1, Date bodyCreateDate)
            throws BusinessException {
        List<CtpAffair> affairList = affairData.getAffairList();
        if (Strings.isEmpty(affairList)) {
            return;
        }
        List<CtpAffair> oldSendMessageAffairs = new ArrayList<CtpAffair>();
        List<StateEnum> states = new ArrayList<StateEnum>();
        states.add(StateEnum.col_sent);
        states.add(StateEnum.col_pending);
        states.add(StateEnum.col_done);
        List<CtpAffair> affairAllList = null;
        ColSummary summary = null;
        Map<Long, MessageReceiver> receiversMap = new HashMap<Long, MessageReceiver>();
        Map<Long, MessageReceiver> receivers1Map = new HashMap<Long, MessageReceiver>();
        for (MessageReceiver receiver : receivers) {
            receiversMap.put(receiver.getReferenceId(), receiver);
        }
        if (Strings.isNotEmpty(receivers1)) {
            for (MessageReceiver receiver : receivers1) {
                receivers1Map.put(receiver.getReferenceId(), receiver);
            }
        }
        Long currentAffairMember = affairData.getCurrentMemberId();
        receivers = new ArrayList<MessageReceiver>();
        receivers1 = new ArrayList<MessageReceiver>();
        //已经发送了消息的节点id+消息id
        Set<String> activityAndRuleIds = new HashSet<String>();
        for (CtpAffair ctpAffair : affairList) {

            String messageRuleId = ctpAffair.getMessageRuleId();
            //消息规则为空的时候按照以前发送消息的逻辑
            if (Strings.isBlank(messageRuleId)) {
                oldSendMessageAffairs.add(ctpAffair);
                //系统预置发消息接收人设置
                if (receiversMap.get(ctpAffair.getId()) != null) {
                    receivers.add(receiversMap.get(ctpAffair.getId()));
                }
                if (receivers1Map.get(ctpAffair.getId()) != null) {
                    receivers1.add(receivers1Map.get(ctpAffair.getId()));
                }
                continue;
            }
            List<MessageRuleVO> messageRuleVOs = messageRuleManager.getMeesageByIds(messageRuleId, MessageRuleType.sendToNotice, null);


            for (MessageRuleVO messageRuleVO : messageRuleVOs) {

                if (affairAllList == null) {
                    affairAllList = affairManager.getAffairs(affairData.getModuleId(), states);
                }
                if (summary == null) {
                    summary = colManager.getSummaryById(affairData.getModuleId());
                }

                String activityIdAndRuleId = ctpAffair.getActivityId() + "_" + messageRuleVO.getId();
                if (Long.valueOf("1").equals(messageRuleVO.getCreater())) {
                    oldSendMessageAffairs.add(ctpAffair);
                    //系统预置发消息接收人设置
                    if (receiversMap.get(ctpAffair.getId()) != null) {
                        receivers.add(receiversMap.get(ctpAffair.getId()));
                    }
                    if (receivers1Map.get(ctpAffair.getId()) != null) {
                        receivers1.add(receivers1Map.get(ctpAffair.getId()));
                    }
                    continue;
                }
                if (Strings.isNotBlank(messageRuleVO.getMessageMode()) && !activityAndRuleIds.contains(activityIdAndRuleId)) {
                    sendMessageByMessageRule(messageRuleVO, summary, ctpAffair, affairAllList, null, currentAffairMember);
                    //记录节点是否发送了自定义消息
                    activityAndRuleIds.add(activityIdAndRuleId);
                }
            }

        }

        if (Strings.isEmpty(oldSendMessageAffairs)) {
            return;
        }

        Long senderId = affairData.getSender();
        String subject = affairData.getSubject();
        String forwardMemberId = affairData.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
            forwardMemberFlag = 1;
        }
        CtpAffair aff = affairList.get(0);
        Integer importantLevel = WFComponentUtil.getImportantLevel(aff);
        String bodyContent = affairData.getBodyContent();
        String bodyType = affairData.getContentType();
        int app = aff.getApp();
        // CtpAffair senderAffair =
        // affairManager.getSenderAffair(aff.getObjectId());
        // Long[] userInfoData = Long.valueOf[2];
        /*
         * if(senderAffair!=null){ userInfoData[0] = senderAffair.getMemberId();
         * userInfoData[1] = senderAffair.getTransactorId(); }
         */
        V3xOrgMember sender = null;
        sender = orgManager.getMemberById(senderId);
        // {1}发起协同:《{0}{2,choice,0|#1# (由{3}原发)}》
        Object[] subjects = new Object[]{subject, sender.getName(), forwardMemberFlag, forwardMember, 0, "", 0};

        String subjectis = affairData.getSubject();
        if (!subjectis.contains("自动发起")) {
            if (app == ApplicationCategoryEnum.collaboration.key()) {

                Map<Long, CtpAffair> transAffairs = new HashMap<Long, CtpAffair>();
                for (CtpAffair a : oldSendMessageAffairs) {
                    if (a.getFromId() != null && a.getFromType() != null) {
                        transAffairs.put(a.getId(), a);
                    }
                }

                /*
                 * {1}发起协同:《{0}{2,choice,0#|1#
                 * (由{3}原发)}》{4,choice,0#|1#（由{5}{6,choice,0#|1#加签|2#知会|3#当前会签|4#减签|
                 * 5#多级会签|6#传阅|7#转办}给你）}
                 */

                for (MessageReceiver r : receivers) {
                    MessageContent mContent = null;
                    CtpAffair affair = transAffairs.get(r.getReferenceId());
                    if (affair != null) {
                        V3xOrgMember member = orgManager.getMemberById(affair.getFromId());
                        Object[] transObjs = new Object[]{subject, sender.getName(), forwardMemberFlag, forwardMember, 1,
                                member.getName(), affair.getFromType()};
                        mContent = MessageContent.get("col.send", transObjs);
                    } else {
                        mContent = MessageContent.get("col.send", subjects);
                    }
                    try {
                        if (null != aff.getTempleteId()) {
                            mContent.setTemplateId(aff.getTempleteId());
                        }
                        mContent.setBody(bodyContent, bodyType, bodyCreateDate);
                        mContent.setImportantLevel(aff.getImportantLevel());
                        userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.collaboration, sender.getId(), r, importantLevel);
                    } catch (Exception e) {
                        LOG.error("发起协同消息提醒失败!", e);
                    }
                }

                if (receivers1 != null) {
                    for (MessageReceiver r : receivers1) {

                        MessageContent mContent = null;
                        CtpAffair affair = transAffairs.get(r.getReferenceId());
                        if (affair != null) {
                            V3xOrgMember member = orgManager.getMemberById(affair.getFromId());
                            Object[] transObjs = new Object[]{subject, sender.getName(), forwardMemberFlag, forwardMember,
                                    1, member.getName(), affair.getFromType()};
                            mContent = MessageContent.get("col.send", transObjs);
                        } else {
                            subjects[1] = sender.getName();
                            mContent = MessageContent.get("col.send", subjects);
                        }

                        try {
                            mContent.setBody(bodyContent, bodyType, bodyCreateDate).add("col.agent");
                            mContent.setImportantLevel(aff.getImportantLevel());
                            if (null != aff.getTempleteId()) {
                                mContent.setTemplateId(aff.getTempleteId());
                            }
                            userMessageManager.sendSystemMessage(mContent, ApplicationCategoryEnum.collaboration, sender.getId(), r, importantLevel);
                        } catch (Exception e) {
                            LOG.error("发起协同消息提醒失败!", e);
                        }
                    }
                }

            }
        }

    }

    /**
     * 修改流程发送消息
     * @param caseId
     */
    public void sendSupervisorMsgAndRecordAppLogCol(String caseId) {
        //给相关人员发送消息
        ColSummary summary;
        User user = AppContext.getCurrentUser();
        boolean isNeedSendMsg = false; //是否需要发消息，发起人自己修改不给自己发消息
        try {
            V3xOrgMember member = orgManager.getMemberById(user.getId());
            String memname = ColUtil.getAccountName();
            summary = colManager.getSummaryByCaseId(Long.parseLong(caseId));
            if (summary == null) {
                return;
            }
            Integer importantLevel = ColUtil.getImportantLevel(summary);
            CtpAffair affair = affairManager.getSenderAffair(summary.getId());
            if (affair != null && !affair.getMemberId().equals(member.getId())) { //非已发的
                isNeedSendMsg = true;
            }
            List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summary.getId(), null);
            trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, affair.getMemberId());
            if (isNeedSendMsg) {
                List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
                List<Long> members = new ArrayList<Long>();
                //给跟踪人的发送消息
                for (CtpTrackMember trackMember : trackMembers) {
                    if (members.contains(trackMember.getMemberId())) {
                        continue;
                    }
                    String messageUrl = "message.link.col.done.detail";
                    if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(trackMember.getAffairState())) {
                        messageUrl = "message.link.col.waiSend";
                    }
                    Long affairId = trackMember.getAffairId();
                    Long recieverMemberId = trackMember.getMemberId();
                    Long transactorId = trackMember.getTransactorId();
                    if (transactorId != null && isColProxy(recieverMemberId, transactorId))
                        recieverMemberId = transactorId;
                    if (!recieverMemberId.equals(user.getId())) {
                        MessageReceiver reciever = new MessageReceiver(affairId, recieverMemberId, messageUrl, affairId, "");
                        reciever.setTrack(true);
                        receivers.add(reciever);
                        members.add(recieverMemberId);
                    }
                }
                String forwardMember = "";
                int forwardMemberFlag = 0;
                if (affair != null && !members.contains(affair.getMemberId())) {
                    receivers.add(new MessageReceiver(affair.getId(), Long.valueOf(summary.getStartMemberId()), "message.link.col.waiSend", affair.getId(), ""));
                    members.add(summary.getStartMemberId());
                    String forwardMemberId = affair.getForwardMember();
                    if (Strings.isNotBlank(forwardMemberId)) {
                        try {
                            forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                            forwardMemberFlag = 1;
                        } catch (Exception e) {
                            LOG.error("", e);
                        }
                    }
                } else if (!members.contains(summary.getStartMemberId())) {
                    receivers.add(new MessageReceiver(summary.getId(), Long.valueOf(summary.getStartMemberId())));
                    members.add(summary.getStartMemberId());
                }
                //col.supervise.workflow.update
                MessageContent content = new MessageContent("collaboration.msg.supervise.workflow.update", summary.getSubject(), memname, forwardMemberFlag, forwardMember).
                        setImportantLevel(summary.getImportantLevel());
                if (null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, member.getId(), receivers, importantLevel);
            }
            appLogManager.insertLog(user, AppLogAction.Coll_Flow_Modify, user.getName(), summary.getSubject());
        } catch (Throwable e1) {
            LOG.error("", e1);
        }
    }

    @Override
    public void transColPendingSpecialBackedMsg(ColSummary colSummary, String nodeNameStr) throws BusinessException {
        if (colSummary == null)
            return;
        Long sendAffairId = null;
        Long summaryId = colSummary.getId();
        Set<Long> memberFilters = new HashSet<Long>();
        List<CtpTrackMember> trackingMemberList = getTrackingMemberList(memberFilters, summaryId, true);
        // 待办，已办
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objectId", colSummary.getId());
        List<CtpAffair> allTrackAffairLists = affairManager.getValidAffairs(new FlipInfo(), map);
        if (!CollectionUtils.isEmpty(allTrackAffairLists)) {
            Set<Long> receiversIds = new HashSet<Long>();
            Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
            V3xOrgMember member = null;
            for (CtpAffair affair : allTrackAffairLists) {
                if (affair.getState() != StateEnum.col_sent.key()) {
                    // 待办的情况,要过滤掉指定回退者
                    if (affair.getState() == StateEnum.col_pending.key()) {
                        member = orgManager.getMemberById(affair.getMemberId());
                        if (member != null && nodeNameStr != null && nodeNameStr.indexOf(member.getName()) != -1) {
                            continue;
                        }
                    }
                    // 已办的情况,要过滤掉没有设置跟踪的情况
                    if ((affair.getState() == StateEnum.col_done.key() || affair.getState() == StateEnum.col_pending
                            .key()) && notSetTrack(trackingMemberList, affair.getId())) {
                        continue;
                    }
                    String messageUrl = "message.link.col.pending";
                    if (Integer.valueOf(StateEnum.col_sent.getKey()).equals(affair.getState())) {
                        messageUrl = "message.link.col.waiSend";
                    }
                    if (!receiversIds.contains(affair.getMemberId())) {
                        receiversIds.add(affair.getMemberId());
                        MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), messageUrl, affair.getId(), null);
                        receiver.setTrack(true);
                        receivers.add(receiver);
                    }
                } else {
                    sendAffairId = affair.getId();
                }
            }
            String content = ResourceUtil.getString("collaboration.appointStepBack.resend",
                    AppContext.currentUserName(), colSummary.getSubject(), nodeNameStr);
            MessageContent messageContentSent = new MessageContent(content).setImportantLevel(colSummary.getImportantLevel());
            if (null != colSummary.getTempleteId()) {
                messageContentSent.setTemplateId(colSummary.getTempleteId());
            }
            userMessageManager.sendSystemMessage(messageContentSent, ApplicationCategoryEnum.collaboration, AppContext
                    .getCurrentUser().getId(), receivers, ColUtil.getImportantLevel(colSummary));
        }
        // 督办者
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(colSummary.getId());
        if (superviseDetail != null) {
            Set<MessageReceiver> receivers4Sup = new HashSet<MessageReceiver>();
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for (CtpSupervisor colSupervisor : colSupervisorSet) {
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                receivers4Sup.add(new MessageReceiver(sendAffairId, colSupervisMemberId, "message.link.col.supervise",
                        colSummary.getId()));
            }
            MessageContent msgContent = new MessageContent("collaboration.appointStepBack.resend", AppContext.currentUserName(),
                    colSummary.getSubject(), nodeNameStr).setImportantLevel(colSummary.getImportantLevel());
            if (null != colSummary.getTempleteId()) {
                msgContent.setTemplateId(colSummary.getTempleteId());
            }
            userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.collaboration, AppContext
                    .getCurrentUser().getId(), receivers4Sup, ColMessageFilterEnum.supervise.key);
        }
    }

    /**
     * 没有设置跟踪的判断   代替“ !trackingAffairList.contains(affair) ”
     * @param trackingMemberList
     * @param affairId
     * @return
     */
    private Boolean notSetTrack(List<CtpTrackMember> trackingMemberList, Long affairId) {
        for (CtpTrackMember ctpTrackMember : trackingMemberList) {
            if (ctpTrackMember.getAffairId().equals(affairId)) {
                return false;
            }
        }
        return true;
    }

    private MessageReceiver transMessageReceiver(CtpTrackMember ctpTrackMember, String messageLink) {
        MessageReceiver receiver = new MessageReceiver(ctpTrackMember.getAffairId(), ctpTrackMember.getMemberId(), messageLink, ctpTrackMember.getAffairId(), "");
        receiver.setTrack(true);
        return receiver;
    }

    //指定回退，提交的时候发消息
    public void transSendSubmitMessage4SepicalBacked(ColSummary summary, String submit2NodeName, CtpAffair currentAffair, Comment comment, Map<String, Object> businessData) throws BusinessException {
        Long summaryId = summary.getId();

        String messageLinkDone = "message.link.col.done.detail";
        V3xOrgMember m = orgManager.getMemberById(currentAffair.getMemberId());


        Set<Long> filterMemberIds = new HashSet<Long>();
        filterMemberIds.add(AppContext.currentUserId());
        //跟踪节点：
        Object[] os = new Object[]{"collaboration.speciallback.submit.2", m.getName(), comment, submit2NodeName};
        List<CtpTrackMember> members = getTrackingMemberList(filterMemberIds, summaryId, true);
        sendMsg4Affairs(summary, currentAffair, os, messageLinkDone, members, filterMemberIds, comment, businessData);

        sendMsg4Receivers(summary, currentAffair, os, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds, comment, "", "superviseMsg", businessData);
    }

    /**
     * 获取消息内容
     * @param i18nKey
     * @param affair
     * @param userName
     * @param comment
     * @return
     * @throws NumberFormatException
     * @throws BusinessException
     */
    private MessageContent getMessageContentFromOption(String i18nKey, CtpAffair affair,
                                                       String userName, Comment comment, String toNodes) throws NumberFormatException, BusinessException {
        String opinionContent = "";
        int opinionType = -1;
        String forwardMemberId = affair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
            forwardMemberFlag = 1;
        }
        if (comment != null) {
            // 0-意见隐藏，-1内容为空，1有内容
            opinionType = Strings.isTrue(comment.isHidden()) ? 0 : Strings.isBlank(comment.getContent()) ? -1 : 1;

            // 附件
            int opinionAtt = ("[]".equals(comment.getRelateInfo()) || opinionType == 0) ? -1
                    : (Strings.isBlank(comment.getContent()) ? 1 : 2);

            // 有内容，有附件：减少4个字节
            int deviation = opinionAtt == 2 ? -4 : 0;

            opinionContent = UserMessageUtil.getComment4Message(Strings.toText(comment.getContent()), deviation);
        }
        String messageSubject = "";
        ColSummary summary = colManager.getColSummaryById(affair.getObjectId());
        if (summary != null) {
            messageSubject = summary.getSubject();
        }
        if (Strings.isEmpty(messageSubject)) {
            messageSubject = affair.getSubject();
        }
        MessageContent content = new MessageContent(i18nKey, messageSubject, userName, forwardMemberFlag, forwardMember,
                opinionContent, opinionType, toNodes);
        Long templateId = affair.getTempleteId();
        if (null != templateId) {
            content.setTemplateId(templateId);
        }
        return content;
    }

    @Override
    public void appointStepBackSendMsg(ColSummary summary, List<CtpAffair> allAvailableAffairs, String submitStyle, String selectTargetNodeId, String selectTargetNodeName,
                                       CtpAffair currentAffair, Comment comment, Map<String, Object> businessData) throws BusinessException {
        if (summary == null || StringUtil.checkNull(selectTargetNodeId)) return;
        try {
            User user = AppContext.getCurrentUser();
            Long summaryId = summary.getId();
            V3xOrgMember m = orgManager.getMemberById(currentAffair.getMemberId());

            String messageLinkSent = "message.link.col.waiSend";
            String messageLinkDone = "message.link.col.done.detail";
            String messageLinkPending = "message.link.col.pending";
            //给督办人发消息的连接
            String messageSuperviseLink = "message.link.col.supervise";
            //{0}回退协同《{1}》{2,choice,0#|1#意见:{3}}
            String msg2Send = "collaboration.appointStepBack.msg2Send";

            String backMsg2Send = "collaboration.appointStepBack.backSendermsgToSend";

            //{0}回退协同《{1}》，此协同将从你的已办中取消
            String msg2Done = "collaboration.appointStepBack.msgToDone";


            //{0}回退協同《{1}》，此協同將從你的待辦中取消
            String msg2Peeding = "collaboration.appointStepBack.msgToPending";

            //{0}回退协同《{1}》至{4}  {2,choice,0#|1#意见:{3}}
            String selectTargetNodeMemberName = selectTargetNodeName;
            if ("start".equals(selectTargetNodeId)) {
                V3xOrgMember sm = orgManager.getMemberById(summary.getStartMemberId());
                selectTargetNodeMemberName = sm.getName();
            }
            boolean isAdminStepBack = (Boolean) businessData.get("isAdminStepBack");
            String stepBackMemberName = m != null ? m.getName() : user.getName();
            if (user.isAdmin()) {
                stepBackMemberName = user.getName();
            } else if (isAdminStepBack) {
                stepBackMemberName = ResourceUtil.getString("sys.role.rolename.FormAdmin") + "(" + user.getName() + ")";
            }
            Object[] back2SendOs = new Object[]{backMsg2Send, stepBackMemberName, comment, selectTargetNodeMemberName};
            Object[] msg2SendOs = new Object[]{msg2Send, stepBackMemberName, comment, ""};
            Object[] msg2DoneOs = new Object[]{msg2Done, stepBackMemberName, comment, ""};
            Object[] msg2PendingOs = new Object[]{msg2Peeding, stepBackMemberName, comment, ""};

            List<CtpAffair> sentAffairs = new ArrayList<CtpAffair>();
            List<CtpAffair> doneAffairs = new ArrayList<CtpAffair>();
            List<CtpAffair> pendingAffairs = new ArrayList<CtpAffair>();
            boolean needRemoveCurrentUser = !isAdminStepBack;

            //回退给指定节点:流程重走
            Set<Long> filterMemberIds = new HashSet<Long>();
            if ("start".equals(selectTargetNodeId)) {//选中的开始节点
                if ("0".equals(submitStyle)) { //流程重走
                    if (Strings.isNotEmpty(allAvailableAffairs)) {
                        for (CtpAffair ctpAffair : allAvailableAffairs) {
                            switch (StateEnum.valueOf(ctpAffair.getState())) {
                                case col_sent:
                                case col_waitSend:
                                    filterMemberIds.add(ctpAffair.getMemberId());
                                    sentAffairs.add(ctpAffair);
                                    break;
                                case col_done:
                                    if (!filterMemberIds.contains(ctpAffair.getMemberId()) && !ctpAffair.getMemberId().equals(ctpAffair.getSenderId())) {
                                        filterMemberIds.add(ctpAffair.getMemberId());
                                        doneAffairs.add(ctpAffair);
                                    }
                                    break;
                                // 若是直接提交给我待办节点暂存待办下若跟踪可以收到消息提醒
                                // 流程重走待办需要收到消息
                                case col_pending:
                                    if (!filterMemberIds.contains(ctpAffair.getMemberId()) && !ctpAffair.getMemberId().equals(ctpAffair.getSenderId())) {
                                        filterMemberIds.add(ctpAffair.getMemberId());
                                        pendingAffairs.add(ctpAffair);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    //如果是流程重走-发起人就不走
                    businessData.put("retraceProcess", false);
                    sendMsg4Affairs(summary, currentAffair, msg2DoneOs, messageLinkDone, getTrackMembersByAffairs(doneAffairs), filterMemberIds, comment, ColOpenFrom.repealRecord.name(), needRemoveCurrentUser, businessData);
                    if (isAdminStepBack) {
                        Boolean retraceProcess = (Boolean) businessData.get("retraceProcess");
                        businessData.put("retraceProcess", true);
                        sendMsg4Affairs(summary, currentAffair, msg2PendingOs, "", getTrackMembersByAffairs(pendingAffairs), filterMemberIds, comment, ColOpenFrom.repealRecord.name(), needRemoveCurrentUser, businessData);//OA-40673 取消消息连接
                        businessData.put("retraceProcess", retraceProcess);
                    } else {
                        sendMsg4Affairs(summary, currentAffair, msg2SendOs, "", getTrackMembersByAffairs(pendingAffairs), filterMemberIds, comment, ColOpenFrom.repealRecord.name(), needRemoveCurrentUser, businessData);//OA-40673 取消消息连接
                    }

                } else if ("1".equals(submitStyle)) {//提交回退者
                    messageLinkDone = "message.link.col.done.detail";
                    //发起者
                    if (Strings.isNotEmpty(allAvailableAffairs)) {
                        for (CtpAffair ctpAffair : allAvailableAffairs) {
                            switch (StateEnum.valueOf(ctpAffair.getState())) {
                                case col_sent:
                                case col_waitSend:
                                    filterMemberIds.add(ctpAffair.getMemberId());
                                    sentAffairs.add(ctpAffair);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    //跟踪
                    List<CtpTrackMember> trackMembers = getTrackingMemberList(filterMemberIds, summaryId, !isAdminStepBack);

                    sendMsg4Affairs(summary, currentAffair, back2SendOs, messageLinkPending, trackMembers, filterMemberIds, comment, businessData);
                    //给消息推送的发送消息
                    List<Long[]> pushMemberIds = comment != null ? comment.getPushMessageToMembersList() : null;
                    if (Strings.isNotEmpty(pushMemberIds)) {
                        List<MessageReceiver> pendings = new ArrayList<MessageReceiver>();
                        for (Long[] push : pushMemberIds) {
                            if (!user.getId().equals(push[1]) && !filterMemberIds.contains(push[1])) {
                                MessageReceiver receiver = getMessageReceiver(push, messageLinkPending, comment.getId().toString(), summaryId);
                                if (receiver != null) {
                                    receiver.setAt(true);
                                    pendings.add(receiver);
                                    filterMemberIds.add(push[1]);
                                }
                            }
                        }
                        if (Strings.isNotEmpty(pendings)) {
                            sendMsg4Receivers(summary, currentAffair, back2SendOs, pendings, filterMemberIds, comment, businessData);
                        }
                    }
                }
                //发起人
                sendMsg4Affairs(summary, currentAffair, msg2SendOs, messageLinkSent, getTrackMembersByAffairs(sentAffairs), filterMemberIds, comment, "", needRemoveCurrentUser, businessData);

                //督办
                sendMsg4Receivers(summary, currentAffair, back2SendOs, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds, comment, "", "superviseMsg", businessData);
                if (!isAdminStepBack) {
                    //当前人的代理人或者被代理人发送消息
                    sendCurrentAndAgent(summary, currentAffair, messageLinkDone, filterMemberIds);
                } else {
                    appointStepBackSendMsg4CurrentAffair(summary, submitStyle, stepBackMemberName, currentAffair, false);
                }
            } else {
                //这个地方重新查询了一下，目的是让例如被回退的节点的消息连接是可用的。而不是无效的。
                allAvailableAffairs = affairManager.getValidAffairs(ApplicationCategoryEnum.collaboration, summaryId);
                //给被回退节点发消息
                List<CtpAffair> targetAffair = new ArrayList<CtpAffair>();
                for (CtpAffair ctpAffair : allAvailableAffairs) {
                    if (selectTargetNodeId.equals(String.valueOf(ctpAffair.getActivityId()))) {
                        filterMemberIds.add(ctpAffair.getMemberId());
                        targetAffair.add(ctpAffair);
                    }
                }
                businessData.put("retraceProcess", true);
                //被回退节点发送消息
                sendMsg4Affairs(summary, currentAffair, msg2SendOs, messageLinkPending, getTrackMembersByAffairs(targetAffair), filterMemberIds, comment, "", needRemoveCurrentUser, businessData);
                businessData.put("retraceProcess", false);
                //跟踪节点：
                List<CtpTrackMember> members = getTrackingMemberList(filterMemberIds, summaryId, !isAdminStepBack);

                sendMsg4Affairs(summary, currentAffair, back2SendOs, messageLinkPending, members, filterMemberIds, comment, "", needRemoveCurrentUser, businessData);

                //给消息推送的发送消息
                List<Long[]> pushMemberIds = comment != null ? comment.getPushMessageToMembersList() : null;
                if (Strings.isNotEmpty(pushMemberIds)) {
                    List<MessageReceiver> pendings = new ArrayList<MessageReceiver>();
                    for (Long[] push : pushMemberIds) {
                        if (!user.getId().equals(push[1]) && !filterMemberIds.contains(push[1])) {
                            MessageReceiver receiver = new MessageReceiver(push[0], push[1], messageLinkPending, push[0], comment.getId());
                            receiver.setAt(true);
                            pendings.add(receiver);
                            filterMemberIds.add(push[1]);
                        }
                    }
                    if (Strings.isNotEmpty(pendings)) {
                        sendMsg4Receivers(summary, currentAffair, back2SendOs, pendings, filterMemberIds, comment, businessData);
                    }
                }


                //督办
                sendMsg4Receivers(summary, currentAffair, back2SendOs, getSuperviseReceiver(summaryId, filterMemberIds, currentAffair), filterMemberIds, comment, "", "superviseMsg", businessData);


                if ("1".equals(submitStyle)) {
                    //当前人的代理人或者被代理人发送消息
                    messageLinkDone = "message.link.col.done.detail";
                    sendCurrentAndAgent(summary, currentAffair, messageLinkDone, filterMemberIds);
                }
                if ("0".equals(submitStyle)) {
                    //流程重走
                    List<MessageReceiver> dones = new ArrayList<MessageReceiver>();
                    List<MessageReceiver> pendings = new ArrayList<MessageReceiver>();

                    Map<Long, Long[]> map = (Map<Long, Long[]>) businessData.get(WorkFlowEventListener.CANCELED_MIDTOARRAY_MAP);
                    for (Long memberId : map.keySet()) {
                        if (!filterMemberIds.contains(memberId)) {
                            Long[] value = map.get(memberId);
                            if (value != null && value.length == 2) {
                                Long affairId = value[0];
                                if (Long.valueOf(StateEnum.col_pending.getKey()).equals(value[1]) || Long.valueOf(StateEnum.col_done.getKey()).equals(value[1])) {
                                    pendings.add(new MessageReceiver(affairId, memberId, "", affairId, null));//OA-40673 取消消息连接
                                }
                            }
                        }
                    }

                    sendMsg4Receivers(summary, currentAffair, msg2SendOs, pendings, filterMemberIds, comment, businessData);
                    sendMsg4Receivers(summary, currentAffair, msg2DoneOs, dones, filterMemberIds, comment, businessData);
                }
            }
        } catch (BusinessException e) {
            LOG.error("指定回退发送消息异常", e);
        }
    }

    private List<CtpTrackMember> getTrackMembersByAffairs(List<CtpAffair> affairs) throws BusinessException {
        List<CtpTrackMember> trackMembers = new ArrayList<CtpTrackMember>();
        for (CtpAffair affair : affairs) {
//			trackMembers.addAll(trackManager.getTrackMembers(affair.getId()));
            if (!affair.isDelete()) {
                CtpTrackMember member = new CtpTrackMember();
                member.setAffairId(affair.getId());
                member.setMemberId(affair.getMemberId());
                member.setSenderId(affair.getSenderId());
                trackMembers.add(member);
            }
        }
        return trackMembers;
    }

    private List<MessageReceiver> getSuperviseReceiver(Long summaryId, Set<Long> filterMembers, CtpAffair senderAffair) throws BusinessException {
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summaryId);
        if (superviseDetail != null) {
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for (CtpSupervisor colSupervisor : colSupervisorSet) {
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                if (!filterMembers.contains(colSupervisMemberId)) {
                    filterMembers.add(colSupervisMemberId);
                    receivers.add(new MessageReceiver(senderAffair.getId(), colSupervisMemberId, "message.link.col.supervise", summaryId));
                }
            }
        }
        return receivers;
    }

    /**
     *
     * @param summary
     * @param currentAffair
     * @param mcInfos 消息内容 mcInfos = Object[]{国际化key, 发送人名，意见，指定回退到的节点名称}
     * @param receivers
     * @param filterMemberIds
     * @throws BusinessException
     */
    private void sendMsg4Receivers(ColSummary summary, CtpAffair currentAffair, Object[] mcInfos,
                                   List<MessageReceiver> receivers, Set<Long> filterMemberIds, Comment comment, Map<String, Object> businessData) throws BusinessException {
        this.sendMsg4Receivers(summary, currentAffair, mcInfos, receivers, filterMemberIds, comment, "", "", businessData);
    }

    private void sendMsg4Receivers(ColSummary summary, CtpAffair currentAffair, Object[] mcInfos,
                                   List<MessageReceiver> receivers, Set<Long> filterMemberIds, Comment comment, String openFrom, String infoType, Map<String, Object> businessData) throws BusinessException {
        this.sendMsg4Receivers(summary, currentAffair, mcInfos,
                receivers, filterMemberIds, comment, openFrom, infoType, true, businessData);
    }

    private void sendMsg4Receivers(ColSummary summary, CtpAffair currentAffair, Object[] mcInfos,
                                   List<MessageReceiver> receivers, Set<Long> filterMemberIds, Comment comment, String openFrom, String infoType,
                                   boolean needRemoveCurrentUser, Map<String, Object> businessData) throws BusinessException {
        User user = AppContext.getCurrentUser();
        boolean isAdminStepBack = false;
        if (businessData != null && businessData.get("isAdminStepBack") != null) {
            isAdminStepBack = (Boolean) businessData.get("isAdminStepBack");
        }
        List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
        Long commentId = comment != null ? comment.getId() : null;
        for (Iterator<MessageReceiver> it = receivers.iterator(); it.hasNext(); ) {
            MessageReceiver r = it.next();
            //收消息的代理人
            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), r.getReceiverId());
            if (agentId != null && !user.getId().equals(agentId) && !filterMemberIds.contains(agentId) && !isAdminStepBack) {
                MessageReceiver ra = new MessageReceiver(r.getReferenceId(), agentId, r.getLinkType());
                ra.setLinkParam(r.getLinkParam());
                agentReceivers.add(ra);
            }
            if (needRemoveCurrentUser && user.getId().equals(r.getReceiverId())) {
                it.remove();
            }
        }
        MessageContent mc = getMessageContentFromOption(String.valueOf(mcInfos[0]), currentAffair, String.valueOf(mcInfos[1]), (Comment) mcInfos[2], String.valueOf(mcInfos[3]));
        //如果是指定回退-并且不是发起者   businessData.put("retraceProcess",true);
        if (businessData.get("retraceProcess") != null && (Boolean) businessData.get("retraceProcess") == true) {
            mc.add("collaboration.summary.cancel.traceview");
        }

        MessageContent mc4agent = getMessageContentFromOption(String.valueOf(mcInfos[0]), currentAffair, String.valueOf(mcInfos[1]), (Comment) mcInfos[2], String.valueOf(mcInfos[3]));
        MessageContent mcTrace = getMessageContentFromOption(String.valueOf(mcInfos[0]), currentAffair, String.valueOf(mcInfos[1]), (Comment) mcInfos[2], String.valueOf(mcInfos[3]));
        MessageContent mc4agentTrace = getMessageContentFromOption(String.valueOf(mcInfos[0]), currentAffair, String.valueOf(mcInfos[1]), (Comment) mcInfos[2], String.valueOf(mcInfos[3]));
        mc.setImportantLevel(summary.getImportantLevel());
        mc4agent.setImportantLevel(summary.getImportantLevel());
        mcTrace.setImportantLevel(summary.getImportantLevel());
        mc4agentTrace.setImportantLevel(summary.getImportantLevel());
        Long templateId = summary.getTempleteId();
        if (null != templateId) {
            mc.setTemplateId(templateId);
            mc4agent.setTemplateId(templateId);
            mcTrace.setTemplateId(templateId);
            mc4agentTrace.setTemplateId(templateId);
        }

        List<MessageReceiver> receivers_notTrace = receivers;
        List<MessageReceiver> receivers_Trace = new ArrayList<MessageReceiver>();
        List<MessageReceiver> agentReceivers_notTrace = agentReceivers;
        List<MessageReceiver> agentReceivers_Trace = new ArrayList<MessageReceiver>();

        List<Long> _hasTraceDataAffair = (List<Long>) businessData.get(WFTraceConstants.WFTRACE_AFFAIRIDS);
        if (Strings.isNotEmpty(_hasTraceDataAffair) && (Strings.isNotEmpty(receivers) || Strings.isNotEmpty(agentReceivers))) {
            //回退的时候其他影响的节点，比如兄弟节点。
            Map<Long, Long> allStepBackAffectAffairMap = (Map<Long, Long>) businessData.get(WorkFlowEventListener.CANCELED_MIDTOAID_MAP);
            Integer type = (Integer) businessData.get(WFTraceConstants.WFTRACE_TYPE);
            for (Long key : allStepBackAffectAffairMap.keySet()) {
                Long affairId = allStepBackAffectAffairMap.get(key);
                if (!_hasTraceDataAffair.contains(affairId)) {
                    continue;
                }
                for (Iterator<MessageReceiver> it = receivers_notTrace.iterator(); it.hasNext(); ) {
                    MessageReceiver mr = it.next();
                    if (key.equals(mr.getReceiverId()) && _hasTraceDataAffair.contains(mr.getReferenceId())) {
                        if (Strings.isNotBlank(openFrom)) {
                            MessageReceiver receiver = new MessageReceiver(mr.getReferenceId(), mr.getReceiverId(), "message.link.col.traceRecord", openFrom, businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID), type + "", commentId);
                            receiver.setTrack(true);
                            receivers_Trace.add(receiver);
                        } else {
                            MessageReceiver receiver = new MessageReceiver(mr.getReferenceId(), mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.stepBackRecord.name(), mr.getReferenceId(), type + "", commentId);
                            receiver.setTrack(true);
                            receivers_Trace.add(receiver);
                        }
                        it.remove();
                    }
                }
                for (Iterator<MessageReceiver> it = agentReceivers_notTrace.iterator(); it.hasNext(); ) {
                    MessageReceiver mr = it.next();
                    if (key.equals(mr.getReceiverId()) && _hasTraceDataAffair.contains(mr.getReferenceId())) {
                        if (Strings.isNotBlank(openFrom)) {
                            MessageReceiver agentReceiver = new MessageReceiver(mr.getReferenceId(), mr.getReceiverId(), "message.link.col.traceRecord", openFrom, businessData.get(WFTraceConstants.WFTRACE_CLONENEW_AFFAIRID), type + "", comment.getId());
                            agentReceiver.setTrack(true);
                            agentReceivers_Trace.add(agentReceiver);
                        } else {
                            MessageReceiver agentReceiver = new MessageReceiver(mr.getReferenceId(), mr.getReceiverId(), "message.link.col.traceRecord", ColOpenFrom.stepBackRecord.name(), mr.getReferenceId(), type + "", commentId);
                            agentReceiver.setTrack(true);
                            agentReceivers_Trace.add(agentReceiver);
                        }
                        it.remove();
                    }
                }
            }
        }

        Integer filterType = ColUtil.getImportantLevel(summary);

        if (Strings.isNotBlank(infoType) && "superviseMsg".equals(infoType)) {
            filterType = ColMessageFilterEnum.supervise.key;
        }

        if (!user.getId().equals(currentAffair.getMemberId()) && !isAdminStepBack) {
            mc.add("col.agent.deal", user.getName());
            mc4agent.add("col.agent.deal", user.getName());
            mcTrace.add("col.agent.deal", user.getName());
            mc4agentTrace.add("col.agent.deal", user.getName());
            userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), receivers_notTrace, filterType);
            userMessageManager.sendSystemMessage(mcTrace.add("collaboration.summary.cancel.traceview"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), receivers_Trace, filterType);
            if (Strings.isNotEmpty(agentReceivers)) {
                userMessageManager.sendSystemMessage(mc4agent.add("col.agent"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), agentReceivers_notTrace, filterType);
                userMessageManager.sendSystemMessage(mc4agentTrace.add("col.agent").add("collaboration.summary.cancel.traceview"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), agentReceivers_Trace, filterType);
            }
        } else {
            Long senderId = currentAffair.getMemberId();
            if (isAdminStepBack) {
                senderId = user.getId();
            }
            userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration, senderId, receivers_notTrace, filterType);
            userMessageManager.sendSystemMessage(mcTrace.add("collaboration.summary.cancel.traceview"), ApplicationCategoryEnum.collaboration, senderId, receivers_Trace, filterType);
            if (Strings.isNotEmpty(agentReceivers)) {
                userMessageManager.sendSystemMessage(mc4agent.add("col.agent"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), agentReceivers_notTrace, filterType);
                userMessageManager.sendSystemMessage(mc4agentTrace.add("col.agent").add("collaboration.summary.cancel.traceview"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), agentReceivers_Trace, filterType);
            }
        }
    }

    /**
     * @param summary
     * @param currentAffair
     * @param user
     * @param importantLevel
     * @param messageLinkSent
     * @param receiversSent
     * @param ctpAffair
     * @return
     * @throws BusinessException
     */
    private void sendMsg4Affairs(ColSummary summary, CtpAffair currentAffair, Object[] mcInfos, String messageLinkSent, List<CtpTrackMember> members, Set<Long>
            filterMemberIds, Comment comment, Map<String, Object> businessData) throws BusinessException {

        sendMsg4Affairs(summary, currentAffair, mcInfos, messageLinkSent, members, filterMemberIds, comment, "", true, businessData);

    }

    private void sendMsg4Affairs(ColSummary summary, CtpAffair currentAffair, Object[] mcInfos, String messageLinkSent, List<CtpTrackMember> members,
                                 Set<Long> filterMemberIds, Comment comment, String openFrom, boolean needRemoveCurrentUser, Map<String, Object> businessData) throws BusinessException {
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        for (CtpTrackMember a : members) {
            receivers.add(transMessageReceiver(a, messageLinkSent));
        }
        sendMsg4Receivers(summary, currentAffair, mcInfos, receivers, filterMemberIds, comment, openFrom, "", needRemoveCurrentUser, businessData);
    }

    private List<CtpTrackMember> getTrackingMemberList(Set<Long> filterMemberIds, Long summaryId, boolean excludeCurrentUser) throws BusinessException {
        // 设置了获取跟踪的trackMember
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
        if (excludeCurrentUser) {
            trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, AppContext.currentUserId());
        }
        for (Iterator<CtpTrackMember> it = trackMembers.iterator(); it.hasNext(); ) {
            CtpTrackMember a = it.next();
            if (!filterMemberIds.contains(a.getMemberId())) {
                filterMemberIds.add(a.getMemberId());
            } else {
                it.remove();
            }
        }

        return trackMembers;
    }

    /**
     * 指定回退流程重走中被取消的节点发消息
     * @param affair
     * @param cancelAffairs
     */
    public void transSendMsg4SpecialBackReRunCanceled(CtpAffair affair, List<CtpAffair> cancelAffairs) {
        User user = AppContext.getCurrentUser();
        String forwardMemberId = affair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
            } catch (BusinessException e) {
                LOG.error("", e);
            }
            forwardMemberFlag = 1;
        }
        Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
        Set<Long> filterMember = new HashSet<Long>();
        for (CtpAffair cancelAffair : cancelAffairs) {
            Long mid = cancelAffair.getMemberId();
            if (!filterMember.contains(mid)) {
                receivers.add(new MessageReceiver(affair.getId(), mid, "message.link.col.done.detail", affair.getId(), ""));
                filterMember.add(mid);
            }
        }
        try {
            MessageContent content = new MessageContent("col.stepback", affair.getSubject(), user.getName(), forwardMemberFlag,
                    forwardMember, "", 0).setImportantLevel(affair.getImportantLevel());
            Long templateId = affair.getTempleteId();
            if (null != templateId) {
                content.setTemplateId(templateId);
            }
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), receivers, WFComponentUtil.getImportantLevel(affair));
        } catch (BusinessException e) {
            LOG.error("", e);
        }

    }

    public void transSendMsg4ProcessOverTime(CtpAffair affair, List<MessageReceiver> receivers, List<MessageReceiver> agentReceivers) {

        String forwardMemberId = affair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        String msgKey = "process.summary.overTerm";
        String title = affair.getSubject();
        Integer importantLevel = affair.getImportantLevel();
        Long senderId = affair.getSenderId();
        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.collaboration;
        if (Strings.isNotEmpty(receivers)) {
            try {
                MessageContent content = MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel);
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    content.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(content, appEnum, senderId, receivers, ColMessageFilterEnum.overTime.key);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }

        if (Strings.isNotEmpty(agentReceivers)) {
            try {
                MessageContent agentContent = MessageContent.get(msgKey, title, forwardMemberFlag, forwardMember).setImportantLevel(importantLevel).add("col.agent");
                Long templateId = affair.getTempleteId();
                if (null != templateId) {
                    agentContent.setTemplateId(templateId);
                }
                userMessageManager.sendSystemMessage(agentContent, appEnum, senderId, agentReceivers, ColMessageFilterEnum.overTime.key);
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * 给当前人的代理人或者被代理人发送消息
     * @param summary
     * @param affair
     * @param messageLinkDone
     * @param filterMemberIds
     */
    private void sendCurrentAndAgent(ColSummary summary, CtpAffair affair, String messageLinkDone, Set<Long> filterMemberIds) throws BusinessException {
        User user = AppContext.getCurrentUser();
        //给被代理人发送消息
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        //给代理人发消息
        List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
        //判断是否是代理人
        boolean hasProxy = false;
        Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), affair.getMemberId());
        V3xOrgMember member = null;
        if (!user.getId().equals(affair.getMemberId()) && !filterMemberIds.contains(affair.getMemberId()) && agentId != null) {
            hasProxy = true;
            member = getMemberById(orgManager, affair.getMemberId());
            receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), messageLinkDone, affair.getId(), ""));
            filterMemberIds.add(affair.getMemberId());
        }
        String forwardMemberId = affair.getForwardMember();
        int forwardMemberFlag = 0;
        String forwardMember = null;
        if (Strings.isNotBlank(forwardMemberId)) {
            try {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        Long templateId = null;
        if (null != summary) {
            templateId = summary.getTempleteId();
        }
        //代理人处理,给被代理人发送消息
        if (hasProxy) {
            MessageContent agentContent = new MessageContent("collaboration.appointStepBack.msgToSend", member.getName()
                    , affair.getSubject(), forwardMemberFlag, forwardMember)
                    .add("col.agent.reply", user.getName()).setImportantLevel(affair.getImportantLevel());
            if (null != templateId) {
                agentContent.setTemplateId(templateId);
            }
            userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers, ColUtil.getImportantLevel(summary));
        } else if (agentId != null && !filterMemberIds.contains(agentId)) { //给当前人的代理人发送消息
            agentReceivers.add(new MessageReceiver(affair.getId(), agentId, messageLinkDone, affair.getId(), null));
            filterMemberIds.add(agentId);
            MessageContent content = new MessageContent("collaboration.appointStepBack.msgToSend", user.getName()
                    , affair.getSubject(), forwardMemberFlag, forwardMember)
                    .add("col.agent").setImportantLevel(affair.getImportantLevel());
            if (null != templateId) {
                content.setTemplateId(templateId);
            }
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getSenderId(), agentReceivers, ColUtil.getImportantLevel(summary));

        }
    }

    @Override
    public void sendSupervisor(ColSummary summary, CtpAffair affair) throws BusinessException {
        // 督办者
        CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summary.getId());
        if (superviseDetail != null) {
            Set<MessageReceiver> receivers4Sup = new HashSet<MessageReceiver>();
            List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
            for (CtpSupervisor colSupervisor : colSupervisorSet) {
                Long colSupervisMemberId = colSupervisor.getSupervisorId();
                receivers4Sup.add(new MessageReceiver(affair.getId(), colSupervisMemberId, "message.link.col.supervise",
                        summary.getId()));
            }

            Long senderId = affair.getSenderId();
            String forwardMember = summary.getForwardMember();
            int forwardMemberFlag = 0;
            if (Strings.isNotBlank(forwardMember)) {
                forwardMemberFlag = 1;
            }
            V3xOrgMember sender = orgManager.getMemberById(senderId);
            MessageContent content = new MessageContent("col.supervise.hasten", summary.getSubject(), sender.getName(), forwardMemberFlag, forwardMember).
                    setImportantLevel(affair.getImportantLevel());
            Long templateId = affair.getTempleteId();
            if (null != templateId) {
                content.setTemplateId(templateId);
            }
            //{1}发起协同:《{0}{2,choice,0|#1# (由{3}原发)}》
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, affair.getSenderId(), receivers4Sup, 7);
        }
    }


    public void getReceiver(CtpAffair affair, int app, List<MessageReceiver> receivers, List<MessageReceiver> receivers1) throws BusinessException {
        Long theMemberId = affair.getMemberId();
        boolean isTrack = false;
        if (null != affair.getTrack() && !Integer.valueOf(TrackEnum.no.ordinal()).equals(affair.getTrack())) {
            isTrack = true;
        }
        if (app == ApplicationCategoryEnum.collaboration.key()) {
            //判断当前的代理人是否有效
            Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.getKey(), theMemberId, affair.getTempleteId(), affair.getSenderId());
            if (agentMemberId != null) {
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending", affair.getId().toString(), "");
                if (isTrack) {
                    receiver.setTrack(true);
                }
                receivers.add(receiver);
                MessageReceiver agentReceiver = new MessageReceiver(affair.getId(), agentMemberId, "message.link.col.pending", affair.getId().toString(), "");
                if (isTrack) {
                    agentReceiver.setTrack(true);
                }
                receivers1.add(agentReceiver);
            } else {
                MessageReceiver receiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.col.pending", affair.getId().toString(), "");
                if (isTrack) {
                    receiver.setTrack(true);
                }
                receivers.add(receiver);
            }
        } else if (app == ApplicationCategoryEnum.edocSend.key() || app == ApplicationCategoryEnum.edocRec.key()
                || app == ApplicationCategoryEnum.edocSign.key()) {
            Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(),
                    theMemberId);
            if (agentMemberId != null) {
                MessageReceiver agentReceiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString());
                if (isTrack) {
                    agentReceiver.setTrack(true);
                }
                receivers.add(agentReceiver);
                MessageReceiver agentReceiver1 = new MessageReceiver(affair.getId(), agentMemberId, "message.link.edoc.pending", affair.getId().toString());
                if (isTrack) {
                    agentReceiver1.setTrack(true);
                }
                receivers1.add(agentReceiver1);
            } else {
                MessageReceiver agentReceiver = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.edoc.pending", affair.getId().toString());
                if (isTrack) {
                    agentReceiver.setTrack(true);
                }
                receivers.add(agentReceiver);
            }
        }
    }

    private MessageReceiver getMessageReceiver(Long[] push, String messageLink, String commentId, Long summaryId) {
        //只传入格式为:affairId时
        if (push.length == 2) {
            return new MessageReceiver(push[0], push[1], messageLink, push[0], commentId);
        } else if (push.length == 3 && summaryId != null) {
            //当前会签时@当前会签的人员(流程未提交)传入格式为:activityId|-5925334929727995298
            try {
                List<CtpAffair> affairList = affairManager.getAffairsByObjectIdAndNodeId(summaryId, push[0]);
                if (Strings.isNotEmpty(affairList)) {
                    for (CtpAffair affair : affairList) {
                        return new MessageReceiver(affair.getId(), push[1], messageLink, affair.getId(), commentId);
                    }
                }
            } catch (BusinessException e) {
                LOG.error("", e);
            }
        }
        return null;
    }


    public void sendMessageByMessageRule(MessageRuleVO ruleVO, ColSummary summary, CtpAffair affair, List<CtpAffair> allAffairList, ColMessageFilterEnum messageFilterEnum, Long messageSenderId) throws BusinessException {
        boolean canSendMessage = true;

        if (messageSenderId == null) {
            messageSenderId = summary.getStartMemberId();
        }
        putMessageRuleUserToSession(messageSenderId);
        try {
            //消息规则前提
            if (Strings.isNotBlank(ruleVO.getMessageCondition()) && ColUtil.isForm(summary.getBodyType())) {
                canSendMessage = capFormManager.isMatchFilterCondition(summary.getFormAppid(), summary.getFormRecordid(), ruleVO.getMessageCondition());
            }
        } catch (SQLException e2) {
            LOG.error("", e2);
        }

        if (!canSendMessage) {
            LOG.info("消息前提不满足，不能发送消息" + ruleVO.getMessageSubject());
            return;
        }
        Map<Long, MessageReceiver> receiverMap = new HashMap<Long, MessageReceiver>();
        List<Long> receiversList = new ArrayList<Long>();
        String receivers = ruleVO.getReceivers();
        String[] split = receivers.split(",");


        Integer messageFilterParam = messageFilterEnum == null ? null : messageFilterEnum.key;

        String messageSentLink = "message.link.col.done.detail";
        String messagePendLink = "message.link.col.pending";
        String superRerviseLink = "message.link.col.supervise";
        ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.collaboration;

        Integer importantLevel = summary.getImportantLevel();
        Set<Long> superviseReceiverIds = new HashSet<Long>();
        for (int a = 0; a < split.length; a++) {
            String typeAndId = split[a];
            if (typeAndId.indexOf("FormField") > -1) {//表单控件
                String[] fieldMember = FormUtil.getFieldName(typeAndId);
                if (fieldMember.length > 0) {
                    Object obj = null;
                    try {
                        obj = capFormManager.getMasterFieldValue(summary.getFormAppid(), summary.getFormRecordid(), fieldMember[1], false, true);
                    } catch (SQLException e) {
                        LOG.error("", e);
                    }
                    LOG.info("消息设置的表单控件" + fieldMember[1] + ":" + obj + ",接收人表单控件:" + fieldMember);
                    String elements = (String) obj;
                    if (Strings.isBlank(elements)) {
                        LOG.info("表单控件值为空");
                        continue;
                    }

                    String role = fieldMember[3];
                    if (Strings.isBlank(role)) {
                        List<V3xOrgMember> memberList = OrgHelper.getMembersByElements(elements);
                        for (V3xOrgMember v3xOrgMember : memberList) {
                            if (!receiversList.contains(v3xOrgMember.getId())) {
                                receiversList.add(v3xOrgMember.getId());
                            }
                        }
                    } else {

                        //表单控件相对角色下面的人员
                        String[] realOrgType = fieldMember[0].split("[|]");
                        List<V3xOrgMember> memberRoleList = new ArrayList<V3xOrgMember>();
                        //人员控件获取人员所在的部门
                        if (V3xOrgEntity.ORGENT_TYPE_MEMBER.equalsIgnoreCase(realOrgType[1]) || FormFieldComEnum.EXTEND_MULTI_MEMBER.getKey().equalsIgnoreCase(realOrgType[1])) {
                            List<V3xOrgMember> memberList = OrgHelper.getMembersByElements(elements);
                            for (V3xOrgMember v3xOrgMember : memberList) {

                                //汇报人
                                if (role.contains("ReciprocalRoleReporter")) {
                                    V3xOrgMember reporter = orgManager.getMemberById(v3xOrgMember.getReporter());
                                    if (reporter != null) {
                                        memberRoleList.add(reporter);
                                    }
                                    //人员基础信息
                                } else if (role.contains("MemberMetadataRole")) {
                                    String roleIdTemp = role.substring(WorkFlowConstants.MemberMetadataRole.length());
                                    String memberMetadataRoleMemberId = (String) orgManager.getMemberInfoByAttribute(v3xOrgMember.getId(), roleIdTemp);
                                    if (Strings.isNotBlank(memberMetadataRoleMemberId) && Strings.isDigits(memberMetadataRoleMemberId)) {
                                        V3xOrgMember memberMetadataRoleMember = orgManager.getMemberById(Long.valueOf(memberMetadataRoleMemberId));
                                        if (memberMetadataRoleMember != null) {
                                            memberRoleList.add(memberMetadataRoleMember);
                                        }
                                    }
                                } else {
                                    if (role.startsWith(WorkFlowConstants.VJOIN)) {
                                        String roleIdTemp = role.substring(WorkFlowConstants.VJOIN.length());
                                        if (Strings.isDigits(roleIdTemp)) {
                                            role = roleIdTemp;
                                        }
                                    }
                                    V3xOrgRole v3xRole = null;
                                    if (Strings.isDigits(role)) {
                                        //数字类角色有可能是因为传入的是code，而code是数字，因此需要先查询一下，如果能查询到，则证明是非数字类code角色
                                        Long roleId = Long.valueOf(role);
                                        v3xRole = orgManager.getRoleById(roleId);
                                    } else {
                                        //传入的是角色的code，根据code去找角色，然后再根据ID去找
                                        List<V3xOrgRole> v3xRoles = orgManager.getRoleByCode(role, v3xOrgMember.getOrgAccountId());
                                        if (Strings.isNotEmpty(v3xRoles)) {
                                            v3xRole = v3xRoles.get(0);
                                        }
                                    }

                                    if (v3xRole != null) {
                                        if (v3xRole.getBond() == OrgConstants.ROLE_BOND.ACCOUNT.ordinal()) {
                                            memberRoleList.addAll(orgManager.getMembersByRole(v3xOrgMember.getOrgAccountId(), v3xRole.getId()));
                                        } else if (v3xRole.getBond() == OrgConstants.ROLE_BOND.DEPARTMENT.ordinal()) {
                                            memberRoleList.addAll(orgManager.getMembersByRole(v3xOrgMember.getOrgDepartmentId(), v3xRole.getId()));
                                        }

                                        List<V3xOrgDepartment> businessDepartment = orgManager.getBusinessDeptsByMemberId(v3xOrgMember.getId(), v3xRole.getOrgAccountId(), true);
                                        if (Strings.isNotEmpty(businessDepartment)) {
                                            for (V3xOrgDepartment v3xOrgDepartment : businessDepartment) {
                                                memberRoleList.addAll(orgManager.getMembersByDepartmentRole(v3xOrgDepartment.getId(), role));
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equalsIgnoreCase(realOrgType[1]) || FormFieldComEnum.EXTEND_MULTI_DEPARTMENT.getKey().equalsIgnoreCase(realOrgType[1])) {
                            //部门控件就使用表单的值
                            List<V3xOrgEntity> entitys = orgManager.getEntities(elements);
                            for (V3xOrgEntity v3xOrgEntity : entitys) {
                                memberRoleList.addAll(orgManager.getMembersByDepartmentRole(v3xOrgEntity.getId(), role));
                            }

                        } else if (V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equalsIgnoreCase(realOrgType[1]) || FormFieldComEnum.EXTEND_MULTI_ACCOUNT.getKey().equalsIgnoreCase(realOrgType[1])) {
                            List<V3xOrgEntity> entitys = orgManager.getEntities(elements);
                            for (V3xOrgEntity v3xOrgEntity : entitys) {
                                memberRoleList.addAll(orgManager.getMembersByAccountRoleOfUp(v3xOrgEntity.getOrgAccountId(), role));
                            }
                        }
                        for (V3xOrgMember v3xOrgMember1 : memberRoleList) {
                            if (!receiversList.contains(v3xOrgMember1.getId())) {
                                receiversList.add(v3xOrgMember1.getId());
                            }
                        }

                    }
                }
            } else if (ruleVO.isDefault()) {
                String ruleReceivers = ruleVO.getReceivers();
                if ("currentNode".equals(ruleReceivers)) {
                    if (affair != null && !receiversList.contains(affair.getMemberId())) {
                        receiversList.add(affair.getMemberId());
                    }
                } else if ("defaultMember".equals(ruleReceivers)) {
                    for (int j = 0; j < allAffairList.size(); j++) {
                        CtpAffair ctpAffair = allAffairList.get(j);
                        if (StateEnum.col_pending.getKey() == ctpAffair.getState() && !receiversList.contains(ctpAffair.getMemberId())) {
                            receiversList.add(ctpAffair.getMemberId());
                        }
                    }
                    //督办发消息

                    CtpSuperviseDetail superviseDetail = superviseManager.getSupervise(summary.getId());
                    if (superviseDetail != null) {
                        List<CtpSupervisor> colSupervisorSet = superviseManager.getSupervisors(superviseDetail.getId());
                        for (CtpSupervisor colSupervisor : colSupervisorSet) {
                            Long colSupervisMemberId = colSupervisor.getSupervisorId();
                            if (!receiversList.contains(colSupervisMemberId)) {
                                //流程之外的督办人需要通过督办查询
                                superviseReceiverIds.add(colSupervisMemberId);
                                receiversList.add(colSupervisMemberId);
                            }
                        }
                    }
                }
            } else {
                try {
                    List<V3xOrgMember> memberSet = OrgHelper.getMembersByElements(typeAndId);
                    Iterator<V3xOrgMember> it = memberSet.iterator();
                    while (it.hasNext()) {
                        V3xOrgMember member = it.next();
                        if (!receiversList.contains(member.getId())) {
                            receiversList.add(member.getId());
                        }
                    }
                } catch (BusinessException e1) {
                    LOG.error("超期提醒发起消息失败：", e1);
                }

            }
        }
        LOG.info("消息接收人:" + Strings.join(receiversList, ","));
        for (int i = 0; i < receiversList.size(); i++) {
            boolean inProcess = false;
            for (int j = 0; j < allAffairList.size(); j++) {
                CtpAffair ctpAffair = allAffairList.get(j);
                if (receiversList.get(i).equals(ctpAffair.getMemberId())) {
                    String messageLink = messageSentLink;
                    if (StateEnum.col_pending.getKey() == ctpAffair.getState()) {
                        messageLink = messagePendLink;
                    }
                    if (superviseReceiverIds.contains(ctpAffair.getMemberId())) {
                        messageLink = superRerviseLink;
                    }
                    receiverMap.put(receiversList.get(i), new MessageReceiver(ctpAffair.getId(), receiversList.get(i), messageLink, ctpAffair.getId().toString()));
                    inProcess = true;
                }
            }
            if (!inProcess) {//接收人不在流程中
                receiverMap.put(receiversList.get(i), new MessageReceiver(summary.getId(), receiversList.get(i)));
            }
        }

        List<String> pipelines = new ArrayList<String>();
        String[] messageModes = ruleVO.getMessageMode().split(",");
        for (int i = 0; i < messageModes.length; i++) {
            if (Strings.isNotBlank(messageModes[i])) {
                pipelines.add(messageModes[i]);
            }
        }
        Set<MessageReceiver> rs = new HashSet<MessageReceiver>(receiverMap.values());

        String subject = getMessageContents(summary, ruleVO, affair);
        LOG.info("消息内容:" + subject);
        MessageContent content = new MessageContent(subject);
        content.setImportantLevel(importantLevel);
        content.setPipelines(pipelines);
        userMessageManager.sendSystemMessage(content, appEnum, messageSenderId, rs, messageFilterParam);

    }


    private void putMessageRuleUserToSession(Long userId) throws BusinessException {

        User userOld = (User) AppContext.getThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY);
        if (userOld != null && userOld.getId().equals(userId)) {
            return;
        }
        LOG.info("userId; " + userId);

        V3xOrgMember member = orgManager.getMemberById(userId);
        User user = new User();
        if (member == null) {
            user.setId(userId);
            AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
            return;
        }
        user.setId(member.getId());
        user.setAccountId(member.getOrgAccountId());
        user.setDepartmentId(member.getOrgDepartmentId());
        user.setLevelId(member.getOrgLevelId());
        user.setLoginAccount(member.getOrgAccountId());
        user.setPostId(member.getOrgPostId());
        user.setName(member.getName());
        user.setLoginName(member.getLoginName());
        user.setUserAgentFrom(Constants.login_useragent_from.pc.name());
        if (member.getOrgAccountId() != null) {
            V3xOrgAccount account = orgManager.getAccountById(member.getOrgAccountId());
            if (account != null) {
                user.setLoginAccount(account.getId());
                user.setLoginAccountName(account.getName());
                user.setLoginAccountShortName(account.getShortName());
            }
        }
        user.setLocale(AppContext.getLocale());
        user.setRemoteAddr(AppContext.getRemoteAddr());
        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, user);
    }

    @Override
    public void sendMsgForWorkflowBugReport(Map<String, Object> bugDataMap) {
        List<MessageReceiver> receiversList = new ArrayList<MessageReceiver>();
        Long templateId = (Long) bugDataMap.get("templateId");
        Long summaryId = (Long) bugDataMap.get("summaryId");
        String messageLink = "message.link.col.bug.message";
        LinkOpenType openType = LinkOpenType.href;

        List<Long> memberList = (List<Long>) bugDataMap.get("memberList");
        if (Strings.isNotEmpty(memberList)) {
            for (Long memberId : memberList) {
                receiversList.add(new MessageReceiver(summaryId, memberId, messageLink, openType));
            }
        }

        if (Strings.isNotEmpty(receiversList)) {
            String subject = (String) bugDataMap.get("subject");
            String bugDesc = (String) bugDataMap.get("bugDesc");
            Long bugMemberId = (Long) bugDataMap.get("bugMemberId");
            String messageContent = ResourceUtil.getString("col.workflow.problem.report.message", Functions.showMemberName(bugMemberId), subject);
            if (Strings.isNotBlank(bugDesc)) {
                messageContent = ResourceUtil.getString("col.workflow.problem.report.message.postscript", Functions.showMemberName(bugMemberId), subject, bugDesc);
            }
            MessageContent content = new MessageContent(messageContent);
            try {
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, bugMemberId, receiversList);
            } catch (Exception e) {
                LOG.error("发送消息异常", e);
            }

        }

    }

    private String getMessageContents(ColSummary summary, MessageRuleVO ruleVO, CtpAffair currentAffair) throws BusinessException {
        String messageContent = ruleVO.getMessageTemplate();
        V3xOrgMember senderMember = orgManager.getMemberById(summary.getStartMemberId());
        String senderName = senderMember == null ? "" : senderMember.getName();
        if (Strings.isBlank(messageContent)) {
            MessageRuleType ruleType = MessageRuleType.valueOf(ruleVO.getMessageType());
            switch (ruleType) {
                case sendToNotice:
                    // {1}发起协同:《{0}{2,choice,0|#1# (由{3}原发)}》
                    Object[] dealMessageParam = new Object[]{summary.getSubject(), senderName, 0, "", 0, "", 0};
                    messageContent = ResourceUtil.getStringByParams("col.send", dealMessageParam);
                    break;
                case dealNotice:
                    V3xOrgMember currentMember = orgManager.getMemberById(currentAffair.getMemberId());
                    String currentMemberName = currentMember == null ? "" : currentMember.getName();
                    Object[] sendMessageParam = new Object[]{currentMemberName, summary.getSubject(), 0, -1, -1, -1, -1, -1, 0};
                    messageContent = ResourceUtil.getStringByParams("collaboration.opinion.deal", sendMessageParam);
                    break;
                case overtimeNotice:
                    messageContent = ResourceUtil.getStringByParams("node.affair.overTerm", summary.getSubject());
                    break;
                default:
                    messageContent = "";
                    break;
            }
        } else {
            messageContent = capFormManager.getCollSubjuet(summary.getFormAppid(), ruleVO.getMessageTemplate(), summary.getFormRecordid(), false);
            //转移换行符
            messageContent = Strings.toText(messageContent);
            if (Strings.isBlank(messageContent)) {
                messageContent = ruleVO.getMessageTemplate();
            }
        }
        return messageContent;
    }


    /**
     * 给督办人员和当前节点所有人发消息
     * @param summaryId
     * @param memberId
     * @param activityId
     * @param message
     * @throws BusinessException
     */
    public void sendSubmitConfirmMessage(Long summaryId, Long memberId, Long activityId, String message) throws BusinessException {
        Long userId = AppContext.currentUserId();

        //督办人
        List<CtpTrackMember> trackMembers = trackManager.getTrackMembers(summaryId, null);
        trackMembers = excludePartTrackAndNotCurrentMember(trackMembers, memberId);
        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        Set<Long> receiveMemberIds = new HashSet<Long>();
        if (Strings.isNotEmpty(trackMembers)) {
            for (CtpTrackMember trackMember : trackMembers) {
                if (receiveMemberIds.contains(trackMember.getMemberId())) {
                    continue;
                }
                receivers.add(new MessageReceiver(trackMember.getAffairId(), trackMember.getMemberId(), ""));
                receiveMemberIds.add(trackMember.getMemberId());
            }
        }

        //当前节点所有人
        List<CtpAffair> currentNodeMmebers = affairManager.getAffairsByObjectIdAndNodeId(summaryId, activityId);
        if (Strings.isNotEmpty(currentNodeMmebers)) {
            for (CtpAffair affair : currentNodeMmebers) {
                if (receiveMemberIds.contains(affair.getMemberId())) {
                    continue;
                }
                receivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), ""));
                receiveMemberIds.add(affair.getMemberId());
            }
        }

        if (Strings.isNotEmpty(receivers)) {
            userMessageManager.sendSystemMessage(new MessageContent(message), ApplicationCategoryEnum.collaboration, userId, receivers);
        }
    }


    /**
     * 跳过节点
     * @param ctpAffair
     * @param messageLink
     * @param processId
     * @param appEnum
     * @param currentMmber
     * @param key
     */
    public void sendMessageForSkipNode(List<CtpAffair> receiverAffairs) {

        try {

            String messageLink = "message.link.col.done.detail";

            String messageKey = "collaboration.affair.skipnode.msg";

            String userName = Functions.showMemberNameOnly(AppContext.currentUserId());


            List<MessageReceiver> msgReceivers = new ArrayList<MessageReceiver>();
            List<MessageReceiver> msgReceiversAgent = new ArrayList<MessageReceiver>();//代理人id

            MessageContent msgContent = null;
            MessageContent msgContentAgent = null;

            CtpAffair randomAffair = null;
            if (Strings.isNotEmpty(receiverAffairs)) {

                randomAffair = receiverAffairs.get(0);

                msgContent = new MessageContent(messageKey, userName, randomAffair.getSubject()).setImportantLevel(randomAffair.getImportantLevel());
                ;

                msgContentAgent = new MessageContent(messageKey, userName, randomAffair.getSubject()).setImportantLevel(randomAffair.getImportantLevel()).add("col.agent");

                for (CtpAffair affair : receiverAffairs) {

                    msgReceivers.add(new MessageReceiver(affair.getId(), affair.getMemberId(), messageLink, affair.getId().toString()));

                    Long agentId = ColUtil.getAgentMemberId(affair.getTempleteId(), affair.getMemberId(), affair.getReceiveTime());

                    if (null != agentId) {

                        V3xOrgMember currentMemberAgent = orgManager.getMemberById(agentId);

                        if (null != currentMemberAgent && currentMemberAgent.isValid()) {//代理人员可用
                            msgReceiversAgent.add(new MessageReceiver(affair.getId(), currentMemberAgent.getId(), messageLink, affair.getId().toString()));
                        }
                    }
                }
            }


            if (Strings.isNotEmpty(msgReceivers)) {
                userMessageManager.sendSystemMessage(msgContent, ApplicationCategoryEnum.collaboration, AppContext.currentUserId(), msgReceivers, ColUtil.getImportantLevel(randomAffair));
            }

            if (Strings.isNotEmpty(msgReceiversAgent)) {
                userMessageManager.sendSystemMessage(msgContentAgent, ApplicationCategoryEnum.collaboration, AppContext.currentUserId(), msgReceiversAgent, ColUtil.getImportantLevel(randomAffair));
            }
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        } finally {

        }
    }


    private void appointStepBackSendMsg4CurrentAffair(ColSummary summary, String submitStyle, String stepBackMemberName, CtpAffair currentAffair, boolean needSendAgent) {

        try {
            User user = AppContext.getCurrentUser();
            List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();

            String messageLinkPending = "message.link.col.traceRecord";
            MessageReceiver receiver = new MessageReceiver(currentAffair.getId(), currentAffair.getMemberId(), messageLinkPending, ColOpenFrom.stepBackRecord.name(), currentAffair.getId().toString());
            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), receiver.getReceiverId());
            if (agentId != null && !user.getId().equals(agentId) && !user.isAdmin()) {
                MessageReceiver ra = new MessageReceiver(currentAffair.getId(), agentId, messageLinkPending, ColOpenFrom.stepBackRecord.name(), currentAffair.getId().toString());
                ra.setLinkParam(receiver.getLinkParam());
                agentReceivers.add(ra);
            }

            //{0}回退協同《{1}》，此協同將從你的待辦中取消
            MessageContent mc = new MessageContent("collaboration.appointStepBack.msgToPending", stepBackMemberName, currentAffair.getSubject());
            mc.add("collaboration.summary.cancel.traceview");

            MessageContent mc4agent = mc;
            mc.setImportantLevel(summary.getImportantLevel());
            mc4agent.setImportantLevel(summary.getImportantLevel());
            Long templateId = summary.getTempleteId();
            if (null != templateId) {
                mc.setTemplateId(templateId);
                mc4agent.setTemplateId(templateId);
            }


            Integer filterType = ColUtil.getImportantLevel(summary);

            Long senderId = user.getId();
            if (!user.getId().equals(currentAffair.getMemberId()) && needSendAgent) {
                mc.add("col.agent.deal", user.getName());
                mc4agent.add("col.agent.deal", user.getName());
                userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration, senderId, receiver, filterType);
                if (Strings.isNotEmpty(agentReceivers)) {
                    userMessageManager.sendSystemMessage(mc4agent.add("col.agent"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), agentReceivers, filterType);
                }
            } else {
                userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration, senderId, receiver, filterType);
                if (Strings.isNotEmpty(agentReceivers)) {
                    userMessageManager.sendSystemMessage(mc4agent.add("col.agent"), ApplicationCategoryEnum.collaboration, currentAffair.getMemberId(), agentReceivers, filterType);
                }
            }
        } catch (BusinessException e) {
            LOG.error("指定回退发送消息异常", e);
        }


    }

    @Override
    public boolean sendMessage4ReplaceNode(User user, CtpAffair newAffair, V3xOrgMember oldMember, String handlerMemberName)
            throws BusinessException {

        try {
            //新生成事项的接收人
            String bodyType = newAffair.getBodyType();
            Date bodyCreateDate = newAffair.getCreateDate();
            Integer important = newAffair.getImportantLevel();
            String forwardMemberId = newAffair.getForwardMember();
            int forwardMemberFlag = 0;
            String forwardMember = null;
            if (Strings.isNotBlank(forwardMemberId)) {
                forwardMember = orgManager.getMemberById(Long.parseLong(forwardMemberId)).getName();
                forwardMemberFlag = 1;
            }

            V3xOrgMember sender = orgManager.getMemberById(newAffair.getSenderId());

            Object[] transObjs = new Object[]{newAffair.getSubject(), sender.getName(), forwardMemberFlag, forwardMember, 0, "", 0};
            //跟踪人


            //给新生成的事项发送消息
            MessageReceiver newReceiver = new MessageReceiver(newAffair.getId(), newAffair.getMemberId(), "message.link.col.pending", newAffair.getId(), "");
            MessageContent content = new MessageContent("col.send", transObjs).setBody("", bodyType, bodyCreateDate).setImportantLevel(important);
            content.setTemplateId(newAffair.getTempleteId());
            userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.collaboration, user.getId(), newReceiver, ColUtil.getImportantLevel(newAffair));


            //给新生产的事项的代理人发送消息
            Long agentId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.collaboration.key(), newAffair.getMemberId());
            if (agentId != null) {
                MessageReceiver newAgentReceiver = new MessageReceiver(newAffair.getId(), agentId, "message.link.col.pending", newAffair.getId(), "");
                MessageContent agentContent = new MessageContent("col.send", transObjs).add("col.agent").setBody("", bodyType, bodyCreateDate)
                        .setImportantLevel(important);
                agentContent.setTemplateId(newAffair.getTempleteId());

                userMessageManager.sendSystemMessage(agentContent, ApplicationCategoryEnum.collaboration, user.getId(), newAgentReceiver, ColUtil.getImportantLevel(newAffair));

            }

            //获取跟踪人员
            Map<Long, MessageReceiver> trackRecieversMap = getTrackReceivers(user, newAffair.getObjectId());
            Iterator<Map.Entry<Long, MessageReceiver>> receiverIterator = trackRecieversMap.entrySet().iterator();
            while (receiverIterator.hasNext()) {
                Map.Entry<Long, MessageReceiver> receiver = receiverIterator.next();
                Long key = receiver.getKey();
                //过滤移交的人的多余消息
                if (newAffair.getMemberId().equals(key)) {
                    receiverIterator.remove();
                }
            }
            //给新生产事项的跟踪人发送消息
            Set<MessageReceiver> trackRecievers = new HashSet<MessageReceiver>();
            trackRecievers.addAll(trackRecieversMap.values());
            if (Strings.isNotEmpty(trackRecievers)) {
                MessageContent trackContent = new MessageContent("node.affair.batch.transfer", newAffair.getSubject(), handlerMemberName, oldMember.getName(), OrgHelper.showMemberName(newAffair.getMemberId()))
                        .setImportantLevel(newAffair.getImportantLevel());
                trackContent.setTemplateId(newAffair.getTempleteId());

                if (Strings.isNotEmpty(trackRecievers)) {
                    userMessageManager.sendSystemMessage(trackContent, ApplicationCategoryEnum.collaboration, user.getId(), trackRecievers, newAffair.getImportantLevel());
                }
            }

            //给替换前的人员发送消息
            MessageReceiver oldMemberReceiver = new MessageReceiver(newAffair.getId(), oldMember.getId(), "message.link.col.pending", newAffair.getId(), "");
            MessageContent oldContent = new MessageContent("collaboration.replace.sendMessage.pending", handlerMemberName, newAffair.getSubject()).setBody("", bodyType, bodyCreateDate).setImportantLevel(important);
            content.setTemplateId(newAffair.getTempleteId());
            userMessageManager.sendSystemMessage(oldContent, ApplicationCategoryEnum.collaboration, user.getId(), oldMemberReceiver, ColUtil.getImportantLevel(newAffair));


        } catch (BusinessException e) {
            LOG.error("转办发送消息报错！", e);
        }
        return true;

    }
}
