package com.seeyon.apps.govdoc.manager;

import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.config.ConfigItem;

/**
 * 新公文开关接口
 * @author 唐桂林
 *
 */
public interface GovdocOpenManager {
	
	/**
	 * 公文开关列表
	 * @param configCategory
	 * @param accountId
	 * @return
	 */
	public List<ConfigItem> listAllEdocSwitchByCategory(String configCategory, Long accountId) throws BusinessException;
	
	/**
	 * 
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public ConfigItem getGovdocViewConfig(Long userId, Long accountId) throws BusinessException;
	
	/**
	 * 
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public String getGovdocViewValue(Long userId, Long accountId) throws BusinessException;
	
	/**
	 * 
	 * @param userId
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public void saveOwnerGovdocView(Long userId, Long accountId, String configValue, boolean isDefault) throws BusinessException;
	
	/**
	 * 删除公文开关
	 * @param configCategory
	 * @param configItem
	 * @param configDescription
	 * @param configValue
	 * @param accountId
	 * @return
	 */
	public void deleteConfigItem(String configCategory, Long accountId) throws BusinessException;
	public void deleteConfigItem(String configCategory, String configItem) throws BusinessException;
	
	/**
	 * 修改公文开关
	 * @param item
	 * @throws BusinessException
	 */
	public void updateEdocSwitch(ConfigItem item) throws BusinessException;
	/**
	 * 判断某开发是否打开
	 * @param configvalue
	 * @return
	 */
	public boolean isSwitchOpen(String configvalue);
	
	/**
	 * 获取某公文开关
	 * @param configCategory
	 * @param configItem
	 * @param accountId
	 * @return
	 * @throws BusinessException
	 */
	public ConfigItem getEdocSwitch(String configCategory, String configItem, Long accountId) throws BusinessException;
	public String getEdocSwitchValue(String configCategory, String configItem, Long accountId) throws BusinessException;
	
	/**
	 * 获取公文默认开关(供开关内部使用)
	 * @param configCategory
	 * @param configItem
	 * @param configDescription
	 * @param configValue
	 * @param accountId
	 * @return
	 */
	public ConfigItem getNewConfigItem(String configCategory, String configItem, String configDescription, String configValue, Long accountId);
	public ConfigItem getNewConfigItem(String configCategory, String configItem, String configDescription, String configValue, Long accountId, int sort);
	
	/**
	 * 是否允许在文单内编辑意见(现在改成了意见填写位置：右侧/文单)
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAllowCommentInForm() throws BusinessException;
	
	/**
	 * 是否允许拟文人修改附件
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAllowUpdateAttachment() throws BusinessException;
	
	/**
	 * 获取允许修改意见的人员
	 * @param item
	 * @return
	 * @throws BusinessException
	 */
	public String getEditAuthMembers(ConfigItem item) throws BusinessException;
	
	/**
	 * 是否允许自建流程
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocSelfFlow() throws BusinessException;
	public boolean isEdocSelfFlow(Long accountId) throws BusinessException;
	
	/**
	 * 是否允许手工输入文号
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocMarkHandInput() throws BusinessException;
	public boolean isEdocMarkHandInput(Long accountId) throws BusinessException;
	
	/**
	 * 当前人员是否允许修改意见
	 * @param userId
	 * @param accountId
	 * @return
	 */
	public boolean hasEditAuth(Long userId, Long accountId) throws BusinessException;
	
	/**
	 * 是否使用公文新布局
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocNewView() throws BusinessException;
	
	/**
	 * 是否启用公文文号(机构代字、年份、流水号)分段选择
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocMarkFenduan() throws BusinessException;
	
	/**
	 * 是否启用内部文号(机构代字、年份、流水号)分段选择
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocInnerMarkFenduan() throws BusinessException;
	
	/**
	 * 是否启用收文"见办"公文应用
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocJianban() throws BusinessException;
	
	/**
	 * 是否启用-公文文号按最大值流水
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocMarkByMax() throws BusinessException;
	
	/**
	 * 是否启用-内部文号按最大值流水
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocInnerMarkByMax() throws BusinessException;
	
	/**
	 * 转发文默认设置
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocZhuanfawen() throws BusinessException;
	
	/**
	 * 转发文默认设置数据
	 * @return
	 * @throws BusinessException
	 */
	public String checkEdocZhuanfawen() throws BusinessException;
	
	/**
	 * 是否启用-转发文自动办结
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocFawenZidongBanjie() throws BusinessException;
	
	/**
	 * 是否启用-收文节点转发文策略
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocZhuanfawenTactics() throws BusinessException;
	
	/**
	 * 是否启用-处室承办
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocChushichengban() throws BusinessException;
	
	/**
	 * 是否启用-启用节点超期
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocZznodeTimeExceeds() throws BusinessException;
	
	/**
	 * 是否启用-控制主送、抄送、抄报单位是否可手写
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocSendToUnitEdit() throws BusinessException;
	public boolean isEdocSendToUnitEdit(Long accountId) throws BusinessException;
	
	/**
	 * 正文套红签发日期显示
	 * @return
	 * @throws BusinessException
	 */
	public boolean isTaohongriqiSwitch() throws BusinessException;
	public boolean isTaohongriqiSwitch(Long accountId) throws BusinessException;
	
	/**
	 * 是否开启下载到本地打印
	 * @param item
	 * @return
	 * @throws BusinessException
	 */
	public boolean isLocalPrint() throws BusinessException;
	
	/**
	 * 是否启用右侧公文元素
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEnableRightView() throws BusinessException;
	
	/**
	 * 是否启用短信提醒
	 * @return
	 * @throws BusinessException
	 */
	public boolean isDuanxintixing() throws BusinessException;
	
	/**
	 * 是否允许修改意见
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAllowEditInForm();
	
}
