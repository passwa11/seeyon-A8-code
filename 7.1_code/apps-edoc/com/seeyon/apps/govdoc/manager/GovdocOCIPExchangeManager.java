package com.seeyon.apps.govdoc.manager;


import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.workflow.event.EventDataContext;
import com.seeyon.ocip.exchange.model.col.Affair;
import com.seeyon.ocip.exchange.model.col.ColOperation;
import com.seeyon.ocip.exchange.model.edoc.EdocOCIPSummary;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public interface GovdocOCIPExchangeManager {


	void handCollaborationAffair(AffairData data, ColOperation operation,EdocSummary edocSummary);
	/**
	 * 带意见
	 * @param data
	 * @param operation
	 * @param colSummary
	 * @param opinionContent
	 */
	void handCollaborationAffair(AffairData data, ColOperation operation,EdocSummary edocSummary,String opinionContent);
	/**
	 * 推送待办并发送消息
	 * @param affair
	 * @param edocSummary
	 * @param receiver
	 */
	public void sendEdocAndMeg(Affair affair,EdocOCIPSummary edocSummary,MessageReceiver receiver,String sendName,int opinionType,String opinionContent,String sendSysCode);
	
	/**
	 * 催办
	 * @param affair
	 * @param colSummary
	 * @param msgContent
	 */
	public void sendHasten(Affair affair,EdocOCIPSummary edocSummary,String msgContent,boolean sendMsg);
	/**
	 * 回退
	 * @param affair
	 * @param colSummary
	 * @param msgContent
	 * @param isSelf 发起者是否和当前处理人为同一人
	 */
	public void sendStepBack(Affair affair,EdocOCIPSummary edocSummary,String sendName,ColOperation operation,String opinionContent,boolean isSelf,String systemCode);
	/**
	 * 竞争执行
	 * @param affair
	 * @param colSummary
	 */
	public void competitionDone(Affair affair,EdocOCIPSummary edocSummary);
	/**
	 * 已办
	 * @param affair
	 * @param colSummary
	 * @param msgContent
	 */	public void sendDone(Affair affair,EdocOCIPSummary edocSummary,String sendSysCode);

	/**
	 * 终止
	 * @param affair
	 * @param colSummary
	 * @param msgContent
	 */
	public void sendStop(Affair affair,EdocOCIPSummary edocSummary,String sendSysCode,String sendName,String opinionContent,boolean isSelf);
	/**
	 * 新增附言
	 * @param affair
	 * @param colSummary
	 * @param sendName
	 */
	public void sendComments(Affair affair,EdocOCIPSummary edocSummary,String sendName);
	/**
	 * 超期提醒
	 * @param affair
	 * @param colSummary
	 * @param sendName
	 */
	public void sendOvertime(Affair affair,EdocOCIPSummary edocSummary,String sendName);
	/**
	 * 修改正文提醒
	 * @param affair
	 * @param colSummary
	 * @param sendName
	 */
	public void sendModifyBody(Affair affair,EdocOCIPSummary edocSummary,String sendName);
	/**
	 * 意见回复提醒
	 * @param affair
	 * @param colSummary
	 * @param sendName
	 */
	public void sendCommentsReply(Affair affair,EdocOCIPSummary edocSummary,String sendName);
	/**
	 * 收藏
	 * @param affair
	 * @param colSummary
	 * @param sendName
	 */
	public void sendFavorite(Affair affair,EdocOCIPSummary edocSummary,String sendName);
	/**
	 * 转发
	 * @param affair
	 * @param colSummary
	 * @param sendName
	 */
	public void forward(Affair affair,EdocOCIPSummary edocSummary,String sendName, String sendSysCode);
	/**
	 * 暂存待办
	 * @param affair
	 * @param colSummary
	 */
	public void sendZcdb(Affair affair,EdocOCIPSummary edocSummary);
	
	public void onWorkflowAssigned(AffairData affairData,EdocSummary summary);
	
	public void onWorkitemFinished(EventDataContext context);
	
	public void onProcessFinished(EventDataContext context);
	
	public void onProcessCanceled(EventDataContext context);
	
	public void onWorkitemTakeBack(EventDataContext context);
	
	public void onWorkitemCanceled(EventDataContext context);
	
}
