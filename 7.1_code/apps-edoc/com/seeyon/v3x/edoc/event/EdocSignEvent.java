package com.seeyon.v3x.edoc.event;

import com.seeyon.ctp.event.Event;
import com.seeyon.v3x.edoc.domain.EdocSignReceipt;

/**
 * 公文签收事件对象。
 * @author muj
 */
public class EdocSignEvent extends Event{

	private static final long serialVersionUID = 758532119618055891L;
	/**
	 * 事件构造器。
	 * @param source : 触发事件的来源对象。
	 */
	public EdocSignEvent(Object source) {
		super(source);
	}
	/**
	 * 公文签收回执单据对象
	 */
	private EdocSignReceipt EdocSignReceipt;
	
	/**
	 * 公文发文详细ID,EdocSendDetail对象主键。
	 */
	private Long sendDetailId;
	


	public EdocSignReceipt getEdocSignReceipt() {
		return EdocSignReceipt;
	}
	public void setEdocSignReceipt(EdocSignReceipt edocSignReceipt) {
		EdocSignReceipt = edocSignReceipt;
	}
	public Long getSendDetailId() {
		return sendDetailId;
	}
	public void setSendDetailId(Long sendDetailId) {
		this.sendDetailId = sendDetailId;
	}
}
