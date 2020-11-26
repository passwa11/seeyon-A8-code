/**
 * 
 */
package com.seeyon.apps.collaboration.listener;

import java.util.Date;
import java.util.List;

import com.seeyon.ctp.common.content.affair.AffairData;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.event.Event;

/** 
* @Description: TODO
* @author muj
* @date 2018年3月22日 下午8:57:14 
*  
*/
public class CollaborationAffairAssignedMsgEvent extends Event{
    /**
     * @param source
     */
    public CollaborationAffairAssignedMsgEvent(Object source) {
        super(source);
    }
    
    private static final long serialVersionUID = 1427462182758555551L;
    private AffairData affairData;
    private List<MessageReceiver> receivers;
    private List<MessageReceiver> receivers1;
    private Date receiveTime;
    public AffairData getAffairData() {
        return affairData;
    }
    public void setAffairData(AffairData affairData) {
        this.affairData = affairData;
    }
    public List<MessageReceiver> getReceivers() {
        return receivers;
    }
    public void setReceivers(List<MessageReceiver> receivers) {
        this.receivers = receivers;
    }
    public List<MessageReceiver> getReceivers1() {
        return receivers1;
    }
    public void setReceivers1(List<MessageReceiver> receivers1) {
        this.receivers1 = receivers1;
    }
    public Date getReceiveTime() {
        return receiveTime;
    }
    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    
}
