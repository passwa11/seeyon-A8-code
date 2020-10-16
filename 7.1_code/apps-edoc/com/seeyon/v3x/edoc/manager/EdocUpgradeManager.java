package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;

public interface EdocUpgradeManager {
	
	public static int UPGRADE_STATE_NEEDTODO = 1;//需要执行升级
	public static int UPGRADE_STATE_NEEDTODO_NO = 2;//不需要执行升级
	public static int UPGRADE_STATE_DOING = 3;//升级执行中
	public static int UPGRADE_STATE_DONE_ERROR = 4;//升级报错
	public static int UPGRADE_STATE_DONE = 5;//升级完成

	public String upgrade() throws Exception;
	
	/**
	 * 获取升级状态
	 * @return
	 */
	public int getUpgradState();
	
	public void setUpgradeState(int state);
	
	public String getTemplateStr() throws BusinessException;

	List<String> getTableName(String tables) throws Exception;
	
	public void activeSessionTime(Map<String,String> params) throws BusinessException;
	
}
