package com.seeyon.apps.govdoc.manager.impl;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Strings;

/**
 * 新公文开关管理类
 * @author 唐桂林
 *
 */
public class GovdocOpenManagerImpl implements GovdocOpenManager {

	private ConfigManager configManager;
	private OrgManager orgManager;
	
	/**
	 * 公文开关列表
	 * @param configCategory
	 * @param accountId
	 * @return
	 */
	@Override
	public List<ConfigItem> listAllEdocSwitchByCategory(String configCategory, Long accountId) {
		List<ConfigItem> returnlst = new ArrayList<ConfigItem>();
		
		List<ConfigItem> templst = configManager.listAllConfigByCategory(configCategory, accountId);
		
		boolean haszhuanfawen = false;//转发文默认设置
		boolean haszfwzidongbanjie = false;//转发文自动办结
		boolean haszhuanfawenTactics = false;//启用收文节点转发文策略
		boolean hasAllowEditInForm = false;//允许修改意见
		boolean hasAllowCommentInForm = false;//允许在文单内编辑意见
		boolean hasallowUpdateAttachment = false;//允许拟文人修改已发公文的附件
		boolean hasGovdocview = false;//公文左右布局
		boolean hasselfFlow = false;//公文发起人可否自建流程
		boolean hasduanxintixing = false;//公文处理时是否短信提醒下一节点处理人
		boolean hastaohongriqi = false;//正文套红签发日期显示
		boolean hasSendToUnitEdit = false;//控制主送、抄送、抄报单位是否可手写
		
		for(ConfigItem cfi : templst) {
			if(cfi.getOrgAccountId().longValue() == accountId.longValue() && configCategory.equals(cfi.getConfigCategory())) {
				if(IConfigPublicKey.GOVDOC_SWITCH_KEY.equals(configCategory)) {
					if(IConfigPublicKey.TimeLable.equals(cfi.getConfigItem())) {
						continue;
					}
					
					if("govdocview".equals(cfi.getConfigItem())) {
						if("yes".equals(cfi.getConfigValue())) {
							cfi.setConfigValue("1");
						} else if("no".equals(cfi.getConfigValue())) {
							cfi.setConfigValue("0");
						}  
					}
					
					//-s没有该功能
					if(!(Boolean)SysFlag.sys_isG6S.getFlag()) {
						if(!hasGovdocview) { if("govdocview".equals(cfi.getConfigItem())) { hasGovdocview = true; } }
						if(!haszhuanfawen) { if("zhuanfawen".equals(cfi.getConfigItem())) { haszhuanfawen = true; } }
						if(!haszfwzidongbanjie) { if("zfwzidongbanjie".equals(cfi.getConfigItem())) { haszfwzidongbanjie = true; } }
						if(!haszhuanfawenTactics) { if("zhuanfawenTactics".equals(cfi.getConfigItem())) { haszhuanfawenTactics = true; } }						
					}
					if(!hasAllowEditInForm) { if("allowEditInForm".equals(cfi.getConfigItem())) { hasAllowEditInForm = true; } }
					if(!hasAllowCommentInForm) { if("allowCommentInForm".equals(cfi.getConfigItem())) { hasAllowCommentInForm = true; } }
					if(!hasallowUpdateAttachment) { if("allowUpdateAttachment".equals(cfi.getConfigItem())) { hasallowUpdateAttachment = true; } }
					if(!hasselfFlow) { if("selfFlow".equals(cfi.getConfigItem())) { hasselfFlow = true; } }
					if(!hasduanxintixing) { if("duanxintixing".equals(cfi.getConfigItem())) { hasduanxintixing = true; } }
					if(!hastaohongriqi) { if("taohongriqi".equals(cfi.getConfigItem())) { hastaohongriqi = true; } }
					if(!hasSendToUnitEdit) { if("sendToUnitEdit".equals(cfi.getConfigItem())) { hasSendToUnitEdit = true; } }
				}
				returnlst.add(cfi);
			}
		}
		if(!(Boolean)SysFlag.sys_isG6S.getFlag()) {
			if (IConfigPublicKey.GOVDOC_SWITCH_KEY.equals(configCategory)) {
				if(!hasGovdocview) {//公文布局设置
					returnlst.add(getConfigItem_govdocview(accountId, true));					
				}
				if(!haszhuanfawen) {//转发文默认设置
					returnlst.add(getConfigItem_zhuanfawen(accountId, true));
				}
				if(!haszfwzidongbanjie) {//转发文自动办结
					returnlst.add(getConfigItem_zfwzidongbanjie(accountId, true));
				}
				if(!haszhuanfawenTactics) {//启用收文节点转发文策略
					returnlst.add(getConfigItem_zhuanfawenTactics(accountId, true));
				}
				if(!hasselfFlow) {//公文发起人可否自建流程
					returnlst.add(getConfigItem_selfFlow(accountId, true));
				}
				if(!hasduanxintixing) {//公文处理时是否短信提醒下一节点处理人
					returnlst.add(getConfigItem_duanxintixing(accountId, true));
				}
				if(!hasallowUpdateAttachment) {//允许拟文人修改已发公文的附件
					returnlst.add(getConfigItem_allowUpdateAttachment(accountId, true));
				}
				if(!hastaohongriqi) {//正文套红签发日期显示
					returnlst.add(getConfigItem_taohongriqi(accountId, true));
				}
			}	
		}
		if(!hasAllowEditInForm) {//允许修改意见
			returnlst.add(getConfigItem_allowEditInForm(accountId, true));
		}
		if(!hasAllowCommentInForm) {//意见填写位置
			returnlst.add(getConfigItem_allowCommentInForm(accountId, true));
		}
		if(!hasSendToUnitEdit) {//控制主送、抄送、抄报单位是否可手写
			returnlst.add(getConfigItem_sendToUnitEdit(accountId, true));
		}
		//此处18、19、20序号预留
		return returnlst;
	}
	
	@Override
	public ConfigItem getGovdocViewConfig(Long userId, Long accountId) throws BusinessException {
		ConfigItem item = configManager.getConfigItem("govdocview", userId.toString(), accountId);
		if(item == null) {
			item = getConfigItem_govdocview(accountId, false);
		}
		return item;
	}
	
	@Override
	public String getGovdocViewValue(Long userId, Long accountId) throws BusinessException {
		ConfigItem item = getGovdocViewConfig(userId, accountId);
		return item.getConfigValue();
	}
	
	@Override
	public void saveOwnerGovdocView(Long userId, Long accountId, String configValue, boolean isDefault) throws BusinessException {
		ConfigItem item = configManager.getConfigItem("govdocview", userId.toString(), accountId);
		if(isDefault) {
			if(item != null) {
				configManager.deleteConfigItem("govdocview", userId.toString(), accountId);
			}
		} else {
			if(item == null) {
				item = new ConfigItem();
				item.setNewId();
				item.setConfigCategory("govdocview");
				item.setConfigItem(userId.toString());
				item.setConfigDescription("公文布局设置");
				item.setConfigValue(configValue);
				item.setConfigType("owner_config");
				item.setOrgAccountId(accountId);
				item.setCreateDate(DateUtil.currentTimestamp());
				item.setSort(0);
				configManager.addConfigItem(item);
			} else {
				item.setConfigValue(configValue);
				configManager.updateConfigItem(item);
			}
		}
	}
	
	/**
	 * 获取允许修改意见的人员
	 * @param item
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String getEditAuthMembers(ConfigItem item) throws BusinessException {
		String memberNames = "";
		if("yes".equals(item.getConfigValue())) {
			String members = item.getExtConfigValue();
			if(Strings.isNotBlank(members)) {
				for(String member : members.split(",")) {
					Long memberId = Long.parseLong(member.split("[|]")[1]);
					if(Strings.isNotBlank(memberNames)) {
						memberNames += "、";
					}
					memberNames += orgManager.getMemberById(memberId).getName();
				}
			}
		}
		return memberNames;
	}
	
	/**
	 * 当前人员是否允许修改意见
	 * @param userId
	 * @param accountId
	 * @return
	 */
	@Override
	public boolean hasEditAuth(Long userId, Long accountId) throws BusinessException {
		if("false".equals(SystemProperties.getInstance().getProperty("govdocConfig.allowEditInForm"))){
			return false;
		}
		ConfigItem item = getConfigItem_allowEditInForm(accountId, false);
		if("yes".equals(item.getConfigValue())) {
			String members = item.getExtConfigValue();
			if(Strings.isNotBlank(members)) {
				for(String member : members.split(",")) {
					Long memberId = Long.parseLong(member.split("[|]")[1]);
					if(userId.longValue() == memberId.longValue()) {
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	/**
	 * 是否允许在文单内编辑意见(现在改成了意见填写位置：右侧/文单)
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAllowCommentInForm() throws BusinessException {
		ConfigItem item = getConfigItem_allowCommentInForm(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否允许拟文人修改附件
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAllowUpdateAttachment() throws BusinessException {
		ConfigItem item = getConfigItem_allowUpdateAttachment(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否允许自建流程
	 * @return
	 * @throws BusinessException
	 */
	public boolean isEdocSelfFlow() throws BusinessException {
		return isEdocSelfFlow(AppContext.currentAccountId());
	}
	public boolean isEdocSelfFlow(Long accountId) throws BusinessException {
		ConfigItem item = getConfigItem_selfFlow(accountId, false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否允许手工输入文号
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocMarkHandInput() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "handInputEdoc", AppContext.currentAccountId());
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	@Override
	public boolean isEdocMarkHandInput(Long accountId) throws BusinessException {
		ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "handInputEdoc", accountId);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否使用公文新布局
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocNewView() throws BusinessException {
		ConfigItem item = getConfigItem_govdocview(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启动右侧公文元素
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEnableRightView() throws BusinessException {
		ConfigItem item = getConfigItem_govdocview(AppContext.currentAccountId(), false);
		if("1".equals(item.getExtConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用公文文号(机构代字、年份、流水号)分段选择
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocMarkFenduan() throws BusinessException {
		ConfigItem item = getConfigItem_edocDocMark(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用内部文号(机构代字、年份、流水号)分段选择
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocInnerMarkFenduan() throws BusinessException {
		ConfigItem item = getConfigItem_edocInnerMark(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用收文"见办"公文应用
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocJianban() throws BusinessException {
		ConfigItem item = getConfigItem_edocInnerMarkJB(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用-公文文号按最大值流水
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocMarkByMax() throws BusinessException {
		ConfigItem item = getConfigItem_docMarkByMax(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用-内部文号按最大值流水
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocInnerMarkByMax() throws BusinessException {
		ConfigItem item = getConfigItem_innerMarkByMax(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 转发文默认设置
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocZhuanfawen() throws BusinessException {
		ConfigItem item = getConfigItem_zhuanfawen(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	/**
	 * 转发文默认设置信息
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public String checkEdocZhuanfawen() throws BusinessException {
		ConfigItem item = getConfigItem_zhuanfawen(AppContext.currentAccountId(), false);
		return item.getConfigValue();
	}
	/**
	 * 是否启用-转发文自动办结
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocFawenZidongBanjie() throws BusinessException {
		ConfigItem item = getConfigItem_zfwzidongbanjie(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用-收文节点转发文策略
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocZhuanfawenTactics() throws BusinessException {
		ConfigItem item = getConfigItem_zhuanfawenTactics(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用-处室承办
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocChushichengban() throws BusinessException {
		ConfigItem item = getConfigItem_chushichengban(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用-启用节点超期
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocZznodeTimeExceeds() throws BusinessException {
		ConfigItem item = getConfigItem_zznodeTimeExceeds(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否启用-控制主送、抄送、抄报单位是否可手写
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isEdocSendToUnitEdit() throws BusinessException {
		return isEdocSendToUnitEdit(AppContext.currentAccountId());
	}
	@Override
	public boolean isEdocSendToUnitEdit(Long accountId) throws BusinessException {
		ConfigItem item = getConfigItem_sendToUnitEdit(accountId, false);
		if("no".equals(item.getConfigValue())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isTaohongriqiSwitch() throws BusinessException {
		return isTaohongriqiSwitch(AppContext.currentAccountId());
	}
	@Override
	public boolean isTaohongriqiSwitch(Long accountId) throws BusinessException {
		ConfigItem item = getConfigItem_taohongriqi(accountId, false);
		if("no".equals(item.getConfigValue())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isLocalPrint() throws BusinessException {
		ConfigItem item = getConfigItem_localPrint(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDuanxintixing() throws BusinessException {
		ConfigItem item = getConfigItem_duanxintixing(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isSwitchOpen(String configvalue) {
		if((!("".equals(configvalue)))&&(!("no".equals(configvalue)))) {
			return true;
		}
		return false;
	}
	/**
	 * 是否允许修改意见
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAllowEditInForm(){
		ConfigItem item = getConfigItem_allowEditInForm(AppContext.currentAccountId(), false);
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	/**
	 * 获取公文开关
	 * @param item
	 * @throws BusinessException
	 */
	@Override
	public ConfigItem getEdocSwitch(String configCategory, String configItem, Long accountId) throws BusinessException {
		if("allowEditInForm".equals(configItem)) {
			return getConfigItem_allowEditInForm(accountId, false);
		} else if("govdocview".equals(configItem)) {
			return getConfigItem_govdocview(accountId, false);
		} else if("edocDocMark".equals(configItem)) {
			return getConfigItem_edocDocMark(accountId, false);
		} else if("edocInnerMark".equals(configItem)) {
			return getConfigItem_edocInnerMark(accountId, false);
		} else if("edocInnerMarkJB".equals(configItem)) {
			return getConfigItem_edocInnerMarkJB(accountId, false);
		} else if("zhuanfawenTactics".equals(configItem)) {
			return getConfigItem_zhuanfawenTactics(accountId, false);
		} else if("zhuanfawen".equals(configItem)) {
			return getConfigItem_zhuanfawen(accountId, false);
		} else if("zfwzidongbanjie".equals(configItem)) {
			return getConfigItem_zfwzidongbanjie(accountId, false);
		} else if("selfFlow".equals(configItem)) {
			return getConfigItem_selfFlow(accountId, false);
		} else if("zznodeTimeExceeds".equals(configItem)) {
			return getConfigItem_zznodeTimeExceeds(accountId, false);
		} else if("duanxintixin".equals(configItem)) {
			return getConfigItem_duanxintixing(accountId, false);
		} else if("chushichengban".equals(configItem)) {
			return getConfigItem_chushichengban(accountId, false);
		} else if("enabled".equals(configItem)) {
			return getConfigItem_enabled(accountId, false);
		} else if("mainNum".equals(configItem)) {
			return getConfigItem_mainNum(accountId, false);
		} else if("copyNum".equals(configItem)) {
			return getConfigItem_copyNum(accountId, false);
		} else if("relation_print".equals(configItem)) {
			return getConfigItem_relation_print(accountId, false);
		} else if("taohongriqi".equals(configItem)) {
			return getConfigItem_taohongriqi(accountId, false);
		}
		return configManager.getConfigItem(configCategory, configItem, accountId);
	}
	
	@Override
	public String getEdocSwitchValue(String configCategory, String configItem, Long accountId) throws BusinessException {
		ConfigItem item = getEdocSwitch(configCategory, configItem, accountId);
		if(item != null) {
			if("no".equals(item.getConfigValue())) {
				return "0";
			} else if("yes".equals(item.getConfigValue())) {
				return "1";
			}
			return item.getConfigValue();
		}
		return "";
	}
	
	/**
	 * 修改公文开关
	 * @param item
	 * @throws BusinessException
	 */
	@Override
	public void updateEdocSwitch(ConfigItem item) throws BusinessException {
		if(configManager.getConfigItem(item.getId()) != null) {
			configManager.updateConfigItem(item);	
		} else {
			configManager.addConfigItem(item);
		}
	}
	
	private ConfigItem getConfigItem_allowEditInForm(Long accountId, boolean isGetDefault) {
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowEditInForm", "允许修改意见", "no", accountId, 11);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowEditInForm", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowEditInForm", "允许修改意见", "no", accountId, 11);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_allowCommentInForm(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowCommentInForm", "意见填写位置", "no", accountId, 15);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowCommentInForm", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowCommentInForm", "意见填写位置", "no", accountId, 15);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_allowUpdateAttachment(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowUpdateAttachment", "允许拟文人修改已发公文的附件", "no", accountId, 12);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowUpdateAttachment", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "allowUpdateAttachment", "允许拟文人修改已发公文的附件", "no", accountId, 12);
			}
			return item;
		}
	}
	
	public ConfigItem getConfigItem_govdocview(Long accountId, boolean isGetDefault) {
		if(isGetDefault) {
			if(!(Boolean)SysFlag.sys_isG6S.getFlag()) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "govdocview", "公文布局设置", "0", accountId, 30);	
			} else {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "govdocview", "公文布局设置", "1", accountId, 30);
			}
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "govdocview", accountId);
			if(item == null) {
				if(!(Boolean)SysFlag.sys_isG6S.getFlag()) {
					return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "govdocview", "公文布局设置", "0", accountId, 30);	
				} else {
					return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "govdocview", "公文布局设置", "1", accountId, 30);
				}
			} else {
				if("yes".equals(item.getConfigValue())) {
					item.setConfigValue("1");
				} else if("no".equals(item.getConfigValue())) {
					item.setConfigValue("0");
				}  
			}
			return item;	
		}
	}
	
	private ConfigItem getConfigItem_edocDocMark(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocDocMark", "公文文号启用(机构代字、年份、流水号)分段选择", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocDocMark", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocDocMark", "公文文号启用(机构代字、年份、流水号)分段选择", "no", accountId);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_edocInnerMark(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocInnerMark", "内部文号启用(机构代字、年份、流水号)分段选择", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocInnerMark", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocInnerMark", "内部文号启用(机构代字、年份、流水号)分段选择", "no", accountId);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_edocInnerMarkJB(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocInnerMarkJB", "收文启用\"见办\"公文应用", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocInnerMarkJB", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "edocInnerMarkJB", "收文启用\"见办\"公文应用", "no", accountId);
			}
			return item;
		}
	}
	private ConfigItem getConfigItem_localPrint(Long accountId, boolean isGetDefault) {
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "localPrint", "是否启用下载到本地打印", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "localPrint", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "localPrint", "是否启用下载到本地打印", "no", accountId);
			}
			return item;
		}
	}
	private ConfigItem getConfigItem_docMarkByMax(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "docMarkByMax", "公文文号按最大值流水", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "docMarkByMax", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "docMarkByMax", "公文文号按最大值流水", "no", accountId);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_innerMarkByMax(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "innerMarkByMax", "内部文号按最大值流水", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "innerMarkByMax", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "innerMarkByMax", "内部文号按最大值流水", "no", accountId);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_zhuanfawen(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zhuanfawen", "转发文默认设置", "no", accountId, 2);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zhuanfawen", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zhuanfawen", "转发文默认设置", "no", accountId, 2);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_zfwzidongbanjie(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zfwzidongbanjie", "转发文自动办结", "no", accountId, 1);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zfwzidongbanjie", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zfwzidongbanjie", "转发文自动办结", "no", accountId, 1);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_zhuanfawenTactics(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zhuanfawenTactics", "启用收文节点转发文策略", "no", accountId, 3);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zhuanfawenTactics", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zhuanfawenTactics", "启用收文节点转发文策略", "no", accountId, 3);
			}
			return item;
		}
	}	
	
	private ConfigItem getConfigItem_chushichengban(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "chushichengban", "开启处室承办", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "chushichengban", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "chushichengban", "开启处室承办", "no", accountId);
			}
			return item;
		}
	}	
	
	private ConfigItem getConfigItem_zznodeTimeExceeds(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zznodeTimeExceeds", "启用节点超期", "no", accountId);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zznodeTimeExceeds", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "zznodeTimeExceeds", "启用节点超期", "no", accountId);
			}
			return item;
		}
	}	
	
	private ConfigItem getConfigItem_sendToUnitEdit(Long accountId, boolean isGetDefault) {
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "sendToUnitEdit", "控制主送、抄送、抄报单位是否可手写", "yes", accountId, 17);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "sendToUnitEdit", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "sendToUnitEdit", "控制主送、抄送、抄报单位是否可手写", "yes", accountId, 17);
			}
			return item;
		}
	}	
	
	private ConfigItem getConfigItem_taohongriqi(Long accountId, boolean isGetDefault) {
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "taohongriqi", "正文套红签发日期显示", "yes", accountId, 21);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "taohongriqi", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "taohongriqi", "正文套红签发日期显示", "yes", accountId, 21);
			}
			return item;
		}
	}	
	
	private ConfigItem getConfigItem_selfFlow(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "selfFlow", "是否允许自建流程", "yes", accountId, 13);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "selfFlow", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "selfFlow", "是否允许自建流程", "yes", accountId, 13);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_duanxintixing(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "duanxintixing", "是否允许短信提醒下一节点人", "no", accountId, 14);
		} else {
			ConfigItem item = configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "duanxintixing", accountId);
			if(item == null) {
				return getNewConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, "duanxintixing", "是否允许短信提醒下一节点人", "no", accountId, 14);
			}
			return item;
		}
	}
	
	private ConfigItem getConfigItem_enabled(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem("printSet", "enabled", "是否有打印控制", "no", accountId, 21);
		} else {
			ConfigItem item = configManager.getConfigItem("printSet", "enabled", accountId);
			if(item == null) {
				return getNewConfigItem("printSet", "enabled", "是否有打印控制", "no", accountId, 21);
			}
			return item;
		}
	}
	private ConfigItem getConfigItem_mainNum(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem("printSet", "mainNum", "是否有打印控制", "no", accountId, 22);
		} else {
			ConfigItem item = configManager.getConfigItem("printSet", "mainNum", accountId);
			if(item == null) {
				return getNewConfigItem("printSet", "mainNum", "是否有打印控制", "no", accountId, 22);
			}
			return item;
		}
	}
	private ConfigItem getConfigItem_copyNum(Long accountId, boolean isGetDefault) {
		if(isGetDefault) {
			return getNewConfigItem("printSet", "copyNum", "是否有打印控制", "no", accountId, 23);
		} else {
			ConfigItem item = configManager.getConfigItem("printSet", "copyNum", accountId);
			if(item == null) {
				return getNewConfigItem("printSet", "copyNum", "是否有打印控制", "no", accountId, 23);
			}
			return item;
		}
	}
	private ConfigItem getConfigItem_relation_print(Long accountId, boolean isGetDefault) { 
		if(isGetDefault) {
			return getNewConfigItem("system_switch", "relation_print", "是否有打印控制", "no", accountId, 24);
		} else {
			ConfigItem item = configManager.getConfigItem("system_switch", "relation_print", accountId);
			if(item == null) {
				return getNewConfigItem("system_switch", "relation_print", "是否有打印控制", "no", accountId, 24);
			}
			return item;
		}
	}
	
	@Override
	public void deleteConfigItem(String configCategory, Long accountId) throws BusinessException {
		configManager.deleteByConfigCategory(configCategory, accountId);
	}
	@Override
	public void deleteConfigItem(String configCategory, String configItem) throws BusinessException {
		configManager.deleteConfigItem(configCategory, configItem);
	}
	
	public ConfigItem getNewConfigItem(String configCategory, String configItem, String configDescription, String configValue, Long accountId) {
		return getNewConfigItem(configCategory, configItem,  configDescription, configValue, accountId, 1);
	}
	public ConfigItem getNewConfigItem(String configCategory, String configItem, String configDescription, String configValue, Long accountId, int sort) {
		ConfigItem item = new ConfigItem();
		item.setNewId();
		item.setConfigCategory(configCategory);
		item.setConfigItem(configItem);
		item.setConfigDescription(configDescription);
		item.setConfigValue(configValue);
		item.setConfigType("system_config");
		item.setOrgAccountId(accountId);
		item.setCreateDate(DateUtil.currentTimestamp());
		item.setSort(sort);
		return item;
	}
	
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
}
