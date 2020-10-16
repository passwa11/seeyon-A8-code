package com.seeyon.apps.govdoc.manager;

import java.util.Date;
import java.util.List;

import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocDealVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.supervise.CtpSuperviseDetail;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import net.joinwork.bpm.engine.wapi.WorkItem;

/**
 * 新公文消息接口
 * 
 * @author 唐桂林
 * 
 */
public interface GovdocMessageManager {

	/**
	 * 公文发起消息
	 * @param affairData
	 * @param receivers
	 * @param receivers1
	 * @param receiveTime
	 * @throws BusinessException
	 */
	public void sendMessage(AffairData affairData, List<MessageReceiver> receivers, List<MessageReceiver> receivers1, Date receiveTime) throws BusinessException;

	/**
	 * 发送处理消息
	 * @param c
	 * @param affair
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public Boolean sendFinishMessage(Comment c, CtpAffair affair, Long summaryId) throws BusinessException;

	/**
	 * 发送暂存待办消息
	 * @param zcdbAffair
	 * @param opinion
	 * @return
	 * @throws BusinessException
	 */
	public Boolean sendMessage4Zcdb(CtpAffair zcdbAffair, Comment opinion) throws BusinessException;
	
	/**
	 * 发送移交消息
	 * @param user
	 * @param summary
	 * @param newAffairs
	 * @param ctpAffair
	 * @param comment
	 * @return
	 * @throws BusinessException
	 */
	public boolean sendMessage4Transfer(User user, EdocSummary summary, List<CtpAffair> newAffairs, CtpAffair ctpAffair, Comment comment) throws BusinessException;
	
	/**
	 * 撤销流程发送消息
	 * @param affairs
	 * @param currentAffair
	 * @param repealCommentTOHTML
	 * @param b
	 * @throws BusinessException
	 */
	public void sendMessage4Repeal(List<CtpAffair> affairs, CtpAffair currentAffair, String repealCommentTOHTML, boolean b) throws BusinessException;
	
	/**
	 * 发送回退消息
	 * @param allTrackAffairLists
	 * @param affair
	 * @param summaryId
	 * @param signOpinion
	 * @param traceFlag
	 * @param msg2Sender
	 * @return
	 */
	public boolean sendStepBackMessage(List<CtpAffair> allTrackAffairLists, CtpAffair affair, Long summaryId,Comment signOpinion,boolean traceFlag, boolean msg2Sender) throws BusinessException;
	
	/**
	 * 发送指定回退消息
	 * @param dealVo
	 * @throws BusinessException
	 */
	public void sendAppointStepBackMsg(GovdocDealVO dealVo) throws BusinessException;
	
	/**
	 * 流程终止发送消息
	 * @param paramWorkItem
	 * @param paramCtpAffair
	 * @param paramList
	 * @return
	 */
	public Boolean sendStepstopMsg(WorkItem paramWorkItem, CtpAffair paramCtpAffair, List<CtpAffair> paramList) throws BusinessException;
		
	/**
	 * 发送取回消息
	 * @param pendingAffairList
	 * @param affair
	 * @param summaryId
	 * @return
	 */
	public Boolean sendTakeBackMsg(List<CtpAffair> pendingAffairList, CtpAffair affair, Long summaryId) throws BusinessException;

	 /**
	  * 修改正文/附件消息 
    * @param affairManager
    * @param orgManager
    * @param summary
    * @param affair
    * @param type 0 附件 1 正文 2 附件和正文
    * @return 
    * @throws BusinessException
    */
	public Boolean sendMessage4ModifyBodyOrAtt(EdocSummary summary, Long memberId,int type) throws BusinessException;
	
	/**
	 *  发送超期消息
	 * @param aff
	 * @param receivers
	 * @param receivers1
	 * @throws BusinessException
	 */
	public void sendMsg4ProcessOverTime(CtpAffair aff, List<MessageReceiver> receivers, List<MessageReceiver> receivers1) throws BusinessException;
	
	/**
	 * 给在督办中被删除的affair发送消息提醒
	 * @param workitem
	 * @param affairs
	 * @return
	 */
	public Boolean sendSuperviseDelete(WorkItem workitem, List<CtpAffair> affairs) throws BusinessException;
	
	/**
     * 	新增发起人附言的时候给流程中的人发消息
     *  version 1.0   给流程中的人发消息
     *  version 2.0   如果流程中的人有代理人 则也给代理人发消息
     *  update by libing
     */
	public void sendPushMessage4SenderNote(Comment comment) throws BusinessException;
	
	/**
	 * 公文意见回复消息推送
	 * @param comment
	 * @throws BusinessException
	 */
	public void sendPushMessage4Reply(Comment comment) throws BusinessException;
	
	/**
	 * 发送交换消息
	 * @param receiverId
	 * @param subject
	 * @param messageInfo
	 * @throws BusinessException
	 */
	public void sendMessage4ExchangeFail(Long receiverId,String subject,String messageInfo) throws BusinessException;
	
	/**
	 * 子流程撤销发送消息
	 * @param affairs
	 * @param summary
	 * @param detail
	 * @param repealComment
	 * @return
	 * @throws BusinessException
	 */
	public Boolean sendMessage4SubProcessRepeal(List<CtpAffair> affairs, EdocSummary summary, CtpSuperviseDetail detail, String repealComment) throws BusinessException;
	
	/**
	 * 各种流程操作发送消息
	 * @param messageDataListJSON
	 * @param summary
	 * @param affair
	 * @param commentId
	 */
	public void sendOperationTypeMessage(String messageDataListJSON, EdocSummary summary, CtpAffair affair,Long commentId) throws BusinessException;
	
	/**
	 * 公文点赞消息发送(暂提供给Rest)
	 * @param baseVo
	 * @throws BusinessException
	 */
	public void sendCommentAddPraiseMsg(GovdocBaseVO baseVo) throws BusinessException;

	/**
     * 给督办人员和当前节点所有人发消息
     * @param summaryId   协同ID
     * @param memberId    人员ID
     * @param activityId  节点ID
     * @param message     消息内容
     * @throws BusinessException
     */
    public void sendSubmitConfirmMessage(Long summaryId, Long memberId, Long activityId, String message) throws  BusinessException;
	
}
