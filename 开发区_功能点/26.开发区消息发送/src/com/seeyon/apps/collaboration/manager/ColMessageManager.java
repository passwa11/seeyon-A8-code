/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.bo.MessageCommentParam;
import com.seeyon.apps.collaboration.enums.ColMessageFilterEnum;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.workflow.messageRule.bo.MessageRuleVO;

import net.joinwork.bpm.engine.wapi.WorkItem;

/**
 * @author mujun
 *
 */
public interface ColMessageManager {

    /**
     * 工作项完成消息提醒
     * @param affair
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    public  Boolean workitemFinishedMessage(Comment c,CtpAffair affair, Long summaryId,Map<String,Object> OverTimecondtiontMap) throws BusinessException;
    /**
     * 撤销流程发送消息
     * @param affairs
     * @throws BusinessException 
     */
    public  void sendMessage4Repeal(List<CtpAffair> affairs,CtpAffair affair,String repealComment,boolean traceFlag,Comment comment,Map<String,Object> businessData) throws BusinessException;
    
    /**
     * 暂存待办消息提醒
     * @param zcdbAffair
     * @param opinion
     * @return
     * @throws BusinessException
     */
    public Boolean sendMessage4Zcdb(CtpAffair zcdbAffair, Comment comment) throws BusinessException ;
    
    /**
     * 协同转办发送消息
     * @param user 当前用户
     * @param summary
     * @param affairs 移交后的事项
     * @param oldAffairMember 移交前的事项人员
     * @return
     * @throws BusinessException
     */
    public boolean sendMessage4Transfer(User user, ColSummary summary, List<CtpAffair> affairs,V3xOrgMember oldAffairMember, Comment opinion) throws BusinessException;
    
    /**
     * 指定回退提交回退这自动转化为流程重走的时候发送消息
     * @param affair
     * @return
     */
    public Boolean reMeToRegoMessage(CtpAffair affair,Map<Long, Long> canceledAffairMap) ;
    /**
     * 给在竞争执行中被取消的affair发送消息提醒
     * @param workitem
     * @param affairs
     * @return
     */
    public Boolean transCompetitionCancel(WorkItem workitem, List<CtpAffair> affairs,CtpAffair affair)  throws BusinessException;
    /**
     * 取回消息提醒
     * @param pendingAffairList
     * @param affair
     * @param summaryId
     * @return
     */
    public  Boolean transTakeBackMessage(List<CtpAffair> pendingAffairList, CtpAffair affair, Long summaryId,Map<Long,Long> canceledMidToAidMap) ;
    
    /**
     * 给在终止操作中被取消的affair发送消息提醒
     * @param affairManager
     * @param orgManager
     * @param userMessageManager
     * @param workitem
     * @param currentAffair
     * @param trackingAndPendingAffairs
     * @return
     */
    public Boolean terminateCancel(WorkItem workitem, CtpAffair currentAffair, List<CtpAffair> trackingAndPendingAffairs,MessageCommentParam mc,List<Long[]> pushMsgMemberList);
    
    /**
     * 给在督办中被删除的affair发送消息提醒
     * FIXME 协同XX被XX删除的提示在这里，有问题待查
     * @param affairManager
     * @param orgManager
     * @param userMessageManager
     * @param workitem
     * @param affairs
     * @return
     */
    public Boolean superviseDelete(WorkItem workitem, List<CtpAffair> affairs);
    
    /**
     * 回退消息提醒
     * @param affairManager
     * @param orgManager
     * @param userMessageManager
     * @param allTrackAffairLists
     * @param affair
     * @param summaryId
     * @param signOpinion
     * @param msg2sender 是否给发起人发送消息
     * @return
     */
    public Boolean stepBackMessage(CtpAffair affair, Long summaryId,Comment signOpinion,boolean traceFlag, 
            boolean msg2sender,Map<Long,Long> canceledMIdToAIdMap,Map<String,Object> businessData);

    /**
     * 如果是子流程且主流程受约束，则给下节点待办发消息可处理
     * @param mainCaseId  流程实例ID
     * @param nodeIds
     */
    public void sendNextPendingNodeMessage(Long mainCaseId,List<Long> nodeIds);
    /**
     * 
     * @param messageDataList   
     */
    /**
     * 加签、减签、知会、会签 发送消息
     * @param messageDataListJSON 消息集合的JSON串,由工作流返回
     * @param summary
     * @param affair
     */
    public Date sendOperationTypeMessage(boolean isImmediateReceipt,String messageDataListJSON,ColSummary summary,CtpAffair affair,Comment comment);
    
    /**
     * 
     * @Title: insertOperationTypeProcessLog   
     * @Description: 加签,减签，知会会签记录日志
     * @param messageDataListJSON
     * @param summary
     * @param affair
     * @param comment
     * @return      
     * @return: Date  
     * @date:   2019年1月10日 上午9:53:48
     * @author: xusx
     * @since   V7.1	       
     * @throws
     */
    public Date insertOperationTypeProcessLog(String messageDataListJSON,ColSummary summary,CtpAffair affair, Comment comment);
    /**
     * 撤销流程时，给督办人发送消息
     * @param superviseId
     * @param app
     * @param summarySubject
     * @param messageKey
     * @param userId
     * @param userName
     * @param repealComment
     * @param forwardMemberIdStr
     */
    public void sendMessage2Supervisor(Long superviseId,ApplicationCategoryEnum app,String summarySubject,
            String messageKey,long userId,String userName, String repealComment, String forwardMemberIdStr, Long templateId) ;
    /**
     * 发起人附言
     * @param comment
     * @throws BusinessException
     */
    public void doCommentPushMessage4SenderNote(Comment comment) throws BusinessException ;
    
    /**
     * 回复意见
     * @param comment
     * @throws BusinessException
     */
    public void doCommentPushMessage4Reply(Comment comment) throws BusinessException ;
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
    public void sendMessage(AffairData affairData,List<MessageReceiver> receivers, List<MessageReceiver> receivers1,Date bodyCreateDate) throws BusinessException;

    /**
     * 修改流程发送消息 
     * @param caseId
     */
    public void sendSupervisorMsgAndRecordAppLogCol(String caseId);
    
    /**
     * 修改正文或者附件发送消息 modifyBodyOrUpdateAttMessage
     * @param affairManager
     * @param orgManager
     * @param summary
     * @param type 0 附件 1 正文 2 附件和正文
     */
    public Boolean sendMessage4ModifyBodyOrAtt(ColSummary summary, Long memberId,int type) throws BusinessException;
    /**
     * 修改正文或者附件发送消息 modifyBodyOrUpdateAttMessage
     * @param affairManager
     * @param orgManager
     * @param summary
     * @param affair
     * @param type 0 附件 1 正文 2 附件和正文
     */
    public Boolean sendMessage4ModifyBodyOrAtt(ColSummary summary, Long memberId,int type,CtpAffair affair) throws BusinessException;
    
    /**
     * 回退到发起者选择提交回退者，重新发起时需要给待办等发送消息
     * @param colSummary
     * @param nodeNameStr 回退者的名称
     * @throws BusinessException
     */
    public void transColPendingSpecialBackedMsg(ColSummary colSummary, String nodeNameStr) throws BusinessException;
    
    /**
     * 指定回退时发送消息
     * @param summary
     * @param affairs
     * @param submitStyle 指定回退选择的处理提交方式
     * @param selectTargetNodeId 回退到的目标节点ID
     * @param currentAffair 做回退操作的affair
     * @throws BusinessException
     */
    public void appointStepBackSendMsg(ColSummary summary, List<CtpAffair> affairs, String submitStyle,
            String selectTargetNodeId,String selectTargetNodeName, CtpAffair currentAffair,Comment comment,Map<String,Object> businessData) throws BusinessException;
    
    /**
     * 指定回退流程重走中被取消的节点发消息
     * @param affair
     * @param cancelAffairs
     */
    public void transSendMsg4SpecialBackReRunCanceled(CtpAffair affair,List<CtpAffair> cancelAffairs);
    
    
    //指定回退，提交的时候发消息
    public void transSendSubmitMessage4SepicalBacked(ColSummary summary,String submit2NodeName,CtpAffair currentAffair,Comment comment,Map<String,Object> businessData) throws BusinessException;
    
    public void transSendMsg4ProcessOverTime(CtpAffair affair,List<MessageReceiver> receivers, List<MessageReceiver> agentReceivers);
    
    /**
     * 指定回退给发起人
     * @param summary
     */
    public void sendSupervisor(ColSummary summary,CtpAffair affari) throws BusinessException;

    public void getReceiver(CtpAffair affair, int app, List<MessageReceiver> receivers,List<MessageReceiver> receivers1) throws BusinessException;
    
    /**
     * 
     * @Title: sendMessageByMessageRule   
     * @Description: 通过消息规则发送消息
     * @param ruleVO 消息规则
     * @param summary 当前summary
     * @param affair 当前需要发消息的affair
     * @param allAffairList 所有的affair 判断消息是否可以穿透
     * @param messageFilterEnum 消息过滤器
     * @param messageSenderId 消息发送人
     * @throws BusinessException      
     * @return: void  
     * @date:   2019年3月16日 下午4:37:20
     * @author: xusx
     * @since   V7.1	       
     * @throws
     */
    public void sendMessageByMessageRule(MessageRuleVO ruleVO, ColSummary summary,CtpAffair affair,List<CtpAffair> allAffairList,ColMessageFilterEnum messageFilterEnum,Long messageSenderId) throws BusinessException;

    /**
     * 给督办人员和当前节点所有人发消息
     * @param summaryId   协同ID
     * @param memberId    人员ID
     * @param activityId  节点ID
     * @param message     消息内容
     * @throws BusinessException
     */
    public void sendSubmitConfirmMessage(Long summaryId, Long memberId, Long activityId, String message) throws  BusinessException;
    
    /**
     * 异常上报，给表单管理员、督办人发消息
     * @param bugDataMap
     */
    public void sendMsgForWorkflowBugReport(Map<String, Object> bugDataMap);
    
    
    /**
     * 跳过节点
     * @param ctpAffair
     * @param messageLink
     * @param processId
     * @param appEnum
     * @param currentMmber
     * @param key
     */
    public void sendMessageForSkipNode(List<CtpAffair> receiverAffairs) ;
    
    /**
     * 
     * @Title: sendMessage4ReplaceNode   
     * @Description: 节点替换发送消息 
     * @param user 当前处理人
     * @param affairs 新生产的事项
     * @param oldAffair 旧的事项
     * @param dealUserName 当前处理人的名称
     * @return
     * @throws BusinessException      
     * @return: boolean  
     * @date:   2019年6月11日 下午3:34:23
     * @author: xusx
     * @since   V7.1SP1	       
     * @throws
     */
    public boolean sendMessage4ReplaceNode(User user,  CtpAffair newAffair,V3xOrgMember oldMember,String dealUserName) throws BusinessException;
}