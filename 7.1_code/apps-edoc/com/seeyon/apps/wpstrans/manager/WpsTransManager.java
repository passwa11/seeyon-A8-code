package com.seeyon.apps.wpstrans.manager;

import com.seeyon.apps.wpstrans.listener.WpsTransEvent;
import com.seeyon.apps.wpstrans.po.WpsTransRecord;
import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 * Wps后台转版接口
 * @author 唐桂林
 *
 */
public interface WpsTransManager {

	/**
	 * Wps转版
	 * @param event
	 * @throws BusException
	 */
	public boolean transToOfd(WpsTransEvent event);
	/**
	 * 异步转换
	 * @param event
	 */
	public void transToOfdAsyn(WpsTransEvent event);
	/**
	 * Wps转版后从服务器上下载及保存
	 * @param serviceTargetPath
	 * @param record
	 * @throws BusException
	 */
	public void downloadAndSaveFile(String serviceTargetPath, WpsTransRecord record);

	/**
	 * 判断转版服务是否可用
	 * @return
	 * @throws BusinessException
	 */
	public boolean isWpsTransServiceEnable();

}
