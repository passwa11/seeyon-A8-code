package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Set;

import net.joinwork.bpm.engine.wapi.WorkItem;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public interface EdocMessagerManager {
    /**
     * 加签、减签、知会、会签 发送消息
     * @param messageDataListJSON 消息集合的JSON串,由工作流返回
     * @param summary
     * @param affair
     */
    public void sendOperationTypeMessage(String messageDataListJSON,EdocSummary summary,CtpAffair affair);
    public void sendOperationTypeMessage(String messageDataListJSON,EdocSummary summary,CtpAffair affair,Long commentId);
    /**
     * 督办/管理员 替换节点时，删除原有事项时，发送提醒消息接口
     * @param workitem
     * @param affairs
     * @return
     */
    public Boolean superviseDelete(WorkItem workitem, List<CtpAffair> affairs);
    public void sendMsg4Receivers(EdocSummary summary, 
            CtpAffair currentAffair ,
            List<Object> m, 
            String messageLinkSent, 
            List<MessageReceiver> receivers,
            Set<Long> filterMemberIds
            ) throws BusinessException ;
    public List<MessageReceiver> getSuperviseReceiver(Long summaryId,Set<Long> filterMembers,CtpAffair senderAffair);
    public void transSendMsg4ProcessOverTime(CtpAffair affair,List<MessageReceiver> receivers, List<MessageReceiver> agentReceivers);
}
