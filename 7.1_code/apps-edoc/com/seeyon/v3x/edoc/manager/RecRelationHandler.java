package com.seeyon.v3x.edoc.manager;

import com.seeyon.v3x.edoc.constants.RecRelationAfterSendParam;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;


/**
 * 收文不同版本的相关处理
 * 以前不同版本的处理都放在一个地方，逻辑上太混乱了，现在将A8和G6的分类进行处理
 * 
 */
public interface RecRelationHandler {

	/**
	 * 收文保存待发后
	 */
	public void transAfterSaveRec(EdocSummary summary,EdocRegister register,String recieveId,String comm);
	
	/**
	 * 收文发送之后
	 */
	public void transAfterSendRec(RecRelationAfterSendParam param);
	
	/**
	 * A8在发送收文之前，获得正确的签收id(这个方法只用于A8)
	 * @param recieveIdStr
	 * @param waitRegister_recieveId
	 * @param isNewSent
	 * @return
	 */
	public String getRecieveIdBeforeSendRec(EdocSummary summary,String recieveIdStr,String waitRegister_recieveId,boolean isNewSent);
}
