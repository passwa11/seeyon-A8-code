package com.seeyon.apps.govdoc.mark.manager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.config.ConfigItem;

public interface GovdocMarkOpenManager {

	/**
	 * 获取所有公文文号开关
	 * @param configCategory
	 * @param accountId
	 * @return
	 */
	public List<ConfigItem> findAllMarkSwitch(String configCategory, Long accountId);
	
	/**
	 * 保存公文文号开关
	 * @param request
	 * @param configCategory
	 * @param user
	 * @throws BusinessException
	 */
	public void saveMarkSwitch(HttpServletRequest request, String configCategory, User user) throws BusinessException;
	
	/**
	 * 保存公文开关到默认配置
	 * @param request
	 * @param configCategory
	 * @param user
	 * @throws BusinessException
	 */
	public void saveMarkSwitchToDefault(String configCategory, User user) throws BusinessException;
	
	/**
	 * 公文文号是否开启手写
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isMarkHandInput(String markType) throws BusinessException;
	
	/**
	 * 公文文号是否开启选择断号/预留文号
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isMarkShowCall(String markType) throws BusinessException;
	
	/**
	 * 公文文号是否开启文号最大值
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isMarkMax(String markType) throws BusinessException;
	
	/**
	 * 发文/签报
	 * 模式2：发起提交时占用文号，其它文不能再使用
	 * 是否启用文号使用提醒
	 * @param markType
	 * @param sendType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isMarkCheckCall(String markType, boolean isFawen) throws BusinessException;
	
	/**
	 * 发文/收文/签报
	 * 模式1：发起提交时不占用文号，其它文可使用
	 * 是否启用分送后占号
	 * @param markType
	 * @param sendType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isUsedByFensong_Fensong(String markType, boolean isFawen) throws BusinessException;
	
	/**
	 * 发文/收文/签报
	 * 模式1：发起提交时不占用文号，其它文可使用
	 * 是否启用流程结束占号
	 * 备注(拼音首写)：JSZH=结束占号
	 * @param markType
	 * @param sendType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isUsedByFensong_Finish(String markType, boolean isFawen) throws BusinessException;
	
	/**
	 * 获取发文/签报/收文文号占用类型(模式1/2)
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public String getSendUsedType(String markType, boolean isFawen) throws BusinessException;
	
}
