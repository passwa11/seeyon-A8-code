package com.seeyon.apps.exchange.engine.service;

import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * 公文/协同交换接口
 */
public interface ExchangeDataManager {
	
	/**
	 * 标准对象里面的业务类型，通过这个判断走的逻辑
	 * recieve/recieveInfo/signObj
	 * @return
	 */
	public String getType();
	
	/**
	 * 发送业务数据
	 * @param sendData
	 * @return
	 * @throws BusinessException
	 */
	public Object send(Object sendData) throws BusinessException;
	
	/**
	 * 发送数据后的回调
	 * @param backData
	 * @throws BusinessException
	 */
	public void callBack(Object backData) throws BusinessException;

}
