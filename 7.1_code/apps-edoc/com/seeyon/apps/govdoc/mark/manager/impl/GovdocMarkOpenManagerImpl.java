package com.seeyon.apps.govdoc.mark.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.constant.GovdocConfigKey;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.apps.govdoc.mark.manager.GovdocMarkOpenManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.util.Strings;

public class GovdocMarkOpenManagerImpl implements GovdocMarkOpenManager {

	private GovdocOpenManager govdocOpenManager;
	private GovdocLogManager govdocLogManager;
	private ConfigManager configManager;
	
	/**
	 * 获取所有公文文号开关
	 * @param configCategory
	 * @param accountId
	 * @return
	 */
	@Override
	public List<ConfigItem> findAllMarkSwitch(String configCategory, Long accountId) {
		List<ConfigItem> returnlst = new ArrayList<ConfigItem>();
		
		boolean hasDocMark = false;//公文文号开关
		boolean hasSerialNo = false;//公文文号开关
		boolean hasSignMark = false;//公文文号开关
		boolean hasDocMarkHandInput = false;
		boolean hasSerialNoHandInput = false;
		boolean hasSignMarkHandInput = false;
		boolean hasDocMarkMax = false;
		boolean hasSerialNoMax = false;
		boolean hasSerialNoJianban = false;
		
		String handInputValue = "yes";
		String maxValue = "no";
		
		List<ConfigItem> templst = configManager.listAllConfigByCategory(configCategory, accountId);
		for(ConfigItem cfi : templst) {
			if(cfi.getOrgAccountId().longValue() == accountId.longValue() && configCategory.equals(cfi.getConfigCategory())) {
				String category = cfi.getConfigCategory();
				String item = cfi.getConfigItem();
				if(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK.equals(category)) {//公文文号
					//因老版中已存在这2个文号开关，所以先判断数据库中是否有该开关，再做新增
					if(GovdocConfigKey.DOC_MARK_HANDINPUT.equals(item)) {
						hasDocMarkHandInput = true;
						handInputValue  = cfi.getConfigValue();
					} else if(GovdocConfigKey.DOC_MARK_MAX.equals(item)) {
						hasDocMarkMax = true;
						maxValue = cfi.getConfigValue();
					} else {
						hasDocMark = true;
					}
				} else if(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO.equals(category)) {//内部文号
					//因老版中已存在这1个文号开关，所以先判断数据库中是否有该开关，再做新增
					if(GovdocConfigKey.SERIAL_NO_HANDINPUT.equals(item)) {
						hasSerialNoHandInput = true;
						handInputValue  = cfi.getConfigValue();
					} else if(GovdocConfigKey.SERIAL_NO_MAX.equals(item)) {
						hasSerialNoMax = true;
					} else if(GovdocConfigKey.SERIAL_NO_JIANBAN.equals(item)) {
						hasSerialNoJianban = true;
					} else {
						hasSerialNo = true;
					}
				} else if(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK.equals(category)) {
					if(GovdocConfigKey.SIGN_MARK_HANDINPUT.equals(item)) {
						hasSignMarkHandInput = true;
						handInputValue  = cfi.getConfigValue();
					} else {
						hasSignMark = true;
					}
				}
				returnlst.add(cfi);
			}
		}
		
		if(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK.equals(configCategory)) {//公文文号
			if(!hasDocMarkHandInput) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.DOC_MARK_HANDINPUT, "启用手写文号", "yes", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_HANDINPUT)));
			}
			if(!hasDocMark) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.DOC_MARK_SHOW_CALL, "编辑文号时显示断号、预留文号", "yes", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_SHOW_CALL)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.DOC_MARK_FAWEN, "发文", GovdocConfigKey.DOC_MARK_FAWEN_DEFUALT_1, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_FAWEN)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.DOC_MARK_QIAN, "签报", GovdocConfigKey.DOC_MARK_QIAN_DEFUALT_2, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_QIAN)));
			}
			if(!hasDocMarkMax) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.DOC_MARK_MAX, "公文文号按最大值自增", "no", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_MAX)));
			}
		} else if(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO.equals(configCategory)) {//内部文号
			if(!hasSerialNoHandInput) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SERIAL_NO_HANDINPUT, "启用手写文号", handInputValue, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_HANDINPUT)));
			}
			if(!hasSerialNoJianban) {}
			if(!hasSerialNo) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SERIAL_NO_SHOW_CALL, "编辑文号时显示断号、预留文号", "no", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_SHOW_CALL)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SERIAL_NO_CHECK_CALL, "启用文号使用提醒", "no", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_CHECK_CALL)));
				//returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SERIAL_NO_FAWEN, "发文", GovdocConfigKey.SERIAL_NO_FAWEN_DEFUALT_2, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_FAWEN)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SERIAL_NO_QIAN, "收文、签报", GovdocConfigKey.SERIAL_NO_QIAN_DEFUALT_2, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_QIAN)));
			}
			if(!hasSerialNoMax) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SERIAL_NO_MAX, "公文文号按最大值自增", "no", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_MAX)));
			}
		} else if(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK.equals(configCategory)) {//签收编号
			if(!hasSignMarkHandInput) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SIGN_MARK_HANDINPUT, "启用手写文号", handInputValue, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_HANDINPUT)));
			}
			if(!hasSignMark) {
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SIGN_MARK_SHOW_CALL, "编辑文号时显示断号、预留文号", "no", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_SHOW_CALL)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SIGN_MARK_CHECK_CALL, "启用文号使用提醒", "no", accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_CHECK_CALL)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SIGN_MARK_MAX, "公文文号按最大值自增", maxValue, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_MAX)));
				returnlst.add(govdocOpenManager.getNewConfigItem(configCategory, GovdocConfigKey.SIGN_MARK_QIAN, "签收、登记", GovdocConfigKey.SIGN_MARK_QIAN_DEFUALT_1, accountId, GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_QIAN)));
			}
		}
		//确保任何地方看到的顺序都是一致的。
    	Collections.sort(returnlst,new Comparator<ConfigItem>() {
			@Override
			public int compare(ConfigItem o1, ConfigItem o2) {
				if(o1.getSort() == null) {
					return 1;
				} else if(o2.getSort() == null) {
					return -1;
				}
				return o1.getSort().compareTo(o2.getSort());
			}
		});
		return returnlst;
	}
	
	/**
	 * 保存公文文号开关
	 * @param request
	 * @param configCategory
	 * @param user
	 * @throws BusinessException
	 */
	@Override
	public void saveMarkSwitch(HttpServletRequest request, String configCategory, User user) throws BusinessException {
		List<ConfigItem> configItems = this.findAllMarkSwitch(configCategory, user.getLoginAccount());
		
		govdocOpenManager.deleteConfigItem(configCategory, user.getLoginAccount());
		
        String itemValue = null;
        for(int i=0; i<configItems.size(); i++) {
        	ConfigItem configItem = configItems.get(i);
            itemValue = request.getParameter(configItem.getConfigItem());
            if(itemValue != null) {
                configItem.setConfigValue(itemValue);
                configItem.setSort(GovdocConfigKey.getMarkSort(configItem.getConfigItem()));
                
                govdocOpenManager.updateEdocSwitch(configItem);
            }
        }
        
        // 记录应日志
        govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OPENSETAUTHORIZE.key(), user.getName());
	}
	
	public void saveMarkSwitchToDefault(String configCategory, User user) throws BusinessException {
		govdocOpenManager.deleteConfigItem(configCategory, user.getLoginAccount());
		
		// 记录应用日志
		govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OPEN_SETDEFAULT.key(), user.getName());
	}

	/**
	 * 公文文号是否开启手写
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isMarkHandInput(String markType) throws BusinessException {
		boolean isHandInput = false;
		if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
			isHandInput = this.isDocMarkHandInput();	
		} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
			isHandInput = this.isSerialNoHandInput();
		} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {
			isHandInput = this.isSignMarkHandInput();
		}
		return isHandInput;
	}
	
	/**
	 * 公文文号是否开启选择断号/预留文号
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isMarkShowCall(String markType) throws BusinessException {
		boolean isShowCall = false;
		if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
			isShowCall = this.isDocMarkShowCall();
		} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
			isShowCall = this.isSerialNoShowCall();
		} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {
			isShowCall = this.isSignMarkShowCall();
		}
		return isShowCall;
	}
	
	/**
	 * 公文文号是否开启文号最大值
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isMarkMax(String markType) throws BusinessException {
		boolean isMarkMax = false;
		if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
			isMarkMax = this.isDocMarkMax();
		} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
			isMarkMax = this.isSerialNoMax();
		} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {
			isMarkMax = this.isSignMarkMax();
		}
		return isMarkMax;
	}
	

	/**
	 * 发文/签报
	 * 模式2：发起提交时占用文号，其它文不能再使用
	 * 是否启用文号使用提醒
	 * 备注(拼音首写)：SYTX=使用提醒
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public boolean isMarkCheckCall(String markType, boolean isFawen) throws BusinessException {
		String isCheckCall = "";
		String configValue = "";
		if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
			configValue = getMarkSendValue(markType, isFawen);
			if(Strings.isNotBlank(configValue)) {
				String[] array = configValue.split("[.]");
				if(array.length >= 4) {
					isCheckCall = array[GovdocConfigKey.USED_TYPE_INDEX_CHECK_CALL];
				}
			}
			return "yes".equals(isCheckCall);
		} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
			return isSerialNoCheckCall();
		} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {
			return isSignMarkCheckCall();
		}
		return false;
	}
	
	/**
	 * 发文/收文/签报
	 * 模式1：发起提交时不占用文号，其它文可使用
	 * 是否启用分送后占号
	 * @param markType
	 * @param sendType
	 * @return
	 * @throws BusinessException
	 */
	@Override
	public boolean isUsedByFensong_Fensong(String markType, boolean isFawen) throws BusinessException {
		String isFensong = "";
		String configValue = "";
		if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
			configValue = getMarkSendValue(markType, isFawen);
		} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
			configValue = getMarkSendValue(markType, isFawen);		
		} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {}
		if(Strings.isNotBlank(configValue) && configValue.startsWith(GovdocConfigKey.USED_TYPE_1+"")) {
			String[] array = configValue.split("[.]");
			if(array.length >= 4) {
				isFensong = array[GovdocConfigKey.USED_TYPE_INDEX_FENGSONG];
			}
		}
		return "yes".equals(isFensong);
	}
	
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
	@Override
	public boolean isUsedByFensong_Finish(String markType, boolean isFawen) throws BusinessException {
		String isFinish = "";
		String configValue = "";
		if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
			configValue = getMarkSendValue(markType, isFawen);
		} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
			configValue = getMarkSendValue(markType, isFawen);		
		} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {
			configValue = getMarkSendValue(markType, isFawen);
		}
		if(Strings.isNotBlank(configValue) && configValue.startsWith(GovdocConfigKey.USED_TYPE_1+"")) {
			String[] array = configValue.split("[.]");
			if(array.length >= 4) {
				isFinish = array[GovdocConfigKey.USED_TYPE_INDEX_FINISH];
			}
		}
		return "yes".equals(isFinish);
	}
	
	/**
	 * 是否允许手工输入文号
	 * @return
	 * @throws BusinessException
	 */
	private boolean isDocMarkHandInput() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_HANDINPUT, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_HANDINPUT, "启用手写文号", "yes", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSerialNoHandInput() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_HANDINPUT, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_HANDINPUT, "启用手写文号", "yes", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSignMarkHandInput() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_HANDINPUT, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_HANDINPUT, "启用手写文号", "yes", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
		
	/**
	 * 是否启用文号最大值
	 * @return
	 * @throws BusinessException
	 */
	private boolean isDocMarkMax() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_MAX, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_MAX, "公文文号按最大值自增", "no", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSerialNoMax() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_MAX, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_MAX, "公文文号按最大值自增", "no", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSignMarkMax() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_MAX, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_MAX, "公文文号按最大值自增", "no", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}

	
	
	public String getSendUsedType(String markType, boolean isFawen) throws BusinessException {
		String configValue = "";
		if(isFawen) {
			if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
				configValue = getDocMarkFawenType().getConfigValue();
			} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
				configValue = getDocMarkFawenType().getConfigValue();
			} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {}
		} else {
			if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
				configValue = getDocMarkQianType().getConfigValue();
			} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
				configValue = getSerialNoQianType().getConfigValue();
			} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {
				configValue = getSignMarkQianType().getConfigValue();
			}
		}
		if(Strings.isNotBlank(configValue)) {
			return configValue.substring(0, 1);
		} else {
			return "2";
		}
	}
	
	private String getMarkSendValue(String markType, boolean isFawen) throws BusinessException {
		if(isFawen) {
			String configValue = "1";
			if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
				return getDocMarkFawenType().getConfigValue();
			} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {//发文内部文号同收文
				return getDocMarkFawenType().getConfigValue();
			} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {}
			return configValue;
		} else {
			String configValue = "1";
			if("edocDocMark".equals(markType) || "0".equals(markType) || "doc_mark".equals(markType)) {	
				return getDocMarkQianType().getConfigValue();
			} else if("edocInnerMark".equals(markType) || "1".equals(markType) || "serial_no".equals(markType)) {
				return getSerialNoQianType().getConfigValue();
			} else if("edocSignMark".equals(markType) || "2".equals(markType) || "sign_mark".equals(markType)) {}
			return configValue;
		}
	}
	
	private ConfigItem getDocMarkFawenType() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_FAWEN, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_FAWEN, "发文", GovdocConfigKey.DOC_MARK_FAWEN_DEFUALT_1, AppContext.currentAccountId(), GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_FAWEN));
		}
		return item;
	}
	private ConfigItem getDocMarkQianType() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_QIAN, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_QIAN, "签报", GovdocConfigKey.DOC_MARK_QIAN_DEFUALT_2, AppContext.currentAccountId(), GovdocConfigKey.getMarkSort(GovdocConfigKey.DOC_MARK_QIAN));
		}
		return item;
	}
	private ConfigItem getSerialNoQianType() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_QIAN, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_QIAN, "收文、签报", GovdocConfigKey.SERIAL_NO_QIAN_DEFUALT_2, AppContext.currentAccountId(), GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_QIAN));
		}
		return item;
	}
	private ConfigItem getSignMarkQianType() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_QIAN, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_QIAN, "发文、收文、签报", GovdocConfigKey.SIGN_MARK_QIAN_DEFUALT_1, AppContext.currentAccountId(), GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_QIAN));
		}
		return item;
	}
	/**
	 * 是否启用显示断号
	 * @return
	 * @throws BusinessException
	 */
	private boolean isDocMarkShowCall() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_SHOW_CALL, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_DOC_MARK, GovdocConfigKey.DOC_MARK_SHOW_CALL, "编辑文号时显示断号、预留文号", "yes", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSerialNoShowCall() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_SHOW_CALL, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_SHOW_CALL, "编辑文号时显示断号、预留文号", "no", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSignMarkShowCall() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_SHOW_CALL, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_SHOW_CALL, "编辑文号时显示断号、预留文号", "no", AppContext.currentAccountId());
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSerialNoCheckCall() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_CHECK_CALL, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SERIAL_NO, GovdocConfigKey.SERIAL_NO_CHECK_CALL, "启用文号使用提醒", "no", AppContext.currentAccountId(), GovdocConfigKey.getMarkSort(GovdocConfigKey.SERIAL_NO_CHECK_CALL));
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}
	private boolean isSignMarkCheckCall() throws BusinessException {
		ConfigItem item = configManager.getConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_CHECK_CALL, AppContext.currentAccountId());
		if(item == null) {
			item = govdocOpenManager.getNewConfigItem(GovdocConfigKey.GOVDOC_SWITCH_SIGN_MARK, GovdocConfigKey.SIGN_MARK_CHECK_CALL, "启用文号使用提醒", "no", AppContext.currentAccountId(), GovdocConfigKey.getMarkSort(GovdocConfigKey.SIGN_MARK_CHECK_CALL));
		}
		if("yes".equals(item.getConfigValue())) {
			return true;
		}
		return false;
	}

	public void setGovdocOpenManager(GovdocOpenManager govdocOpenManager) {
		this.govdocOpenManager = govdocOpenManager;
	}
	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}
	
}
