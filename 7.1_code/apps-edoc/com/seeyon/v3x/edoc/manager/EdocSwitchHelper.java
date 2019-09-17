package com.seeyon.v3x.edoc.manager;


import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

public class EdocSwitchHelper {
	
	public final static String EDOC_SWITCH_allowUpdate="allowUpdate";
	public final static String EDOC_SWITCH_createNumber="createNumber";
	public final static String EDOC_SWITCH_selfFlow="selfFlow";
	public final static String EDOC_SWITCH_handInputEdoc="handInputEdoc";
	public final static String EDOC_SWITCH_defaultExchangeType="defaultExchangeType";
	public final static String EDOC_SWITCH_timeSort="timesort";
	public final static String EDOC_SWITCH_pdfEnable = "pdfEnable";
	public final static String EDOC_SWITCH_allowShowEdocInSend = "allowShowEdocInSend";
	public final static String EDOC_SWITCH_allowShowEdocInRec = "allowShowEdocInRec";
	public final static String EDOC_SWITCH_taohongriqi = "taohongriqi";
	public final static String EDOC_SWITCH_openRegister = "openRegister";
	public final static String EDOC_SWITCH_banwenYuewen = "banwenYuewen";//区分办文阅文
	
	/** 自动登记开关 - 已废弃 **/
	public final static String Edoc_SWITCH_allowAutoRegister = "allowAutoRegister";
	
	/** 部门交换默认选择 **/
	public final static String EDOC_SWITCH_defualtExchangeDeptType = "defualtExchangeDeptType";
	
	
	//开关设置 - 部门交换默认选择 - 取值
	/** 拟稿人部门  **/
	public final static String SWITCH_DEFUALT_EXCHANGE_DEPT_TYPE_CREATER = "Creater";
	
	/** 封发人部门 **/
	public final static String SWITCH_DEFUALT_EXCHANGE_DEPT_TYPE_DISPATCHER = "Dispatcher";
	
	
	
	private static ConfigManager configManager = (ConfigManager)AppContext.getBean("configManager");
	
	/**
	 * 时间顺序的修改
	 * @return
	 */
	public static boolean timesortUpdate()
	{
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();		
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_timeSort, accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_timeSort, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return true;}
		return "yes".equals(configItem.getConfigValue());
	}
	
	/**
	 * 外来文登记，是否允许修改
	 * @return
	 */
	public static boolean canUpdateAtOutRegist()
	{
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();	
		return canUpdateAtOutRegist(accountId);
	}
	
	/**
     * 外来文登记，是否允许修改
     * @return
     */
    public static boolean canUpdateAtOutRegist(long accountId)
    {   
        ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_allowUpdate, accountId);
        if(configItem==null)
        {
            configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_allowUpdate, ConfigItem.Default_Account_Id);
        }
        if(configItem==null){return true;}
        return "yes".equals(configItem.getConfigValue());
    }
	/**
	 * w是否为公文单生成自动编号
	 * @return
	 */
	public static boolean isCreateOutoNumber()
	{
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_createNumber,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_createNumber, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return true;}
		return "yes".equals(configItem.getConfigValue());
	}
	/**
	 * 公文发起人可否自建流程
	 */
	public static boolean canSelfCreateFlow()
	{
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_selfFlow,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_selfFlow, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return true;}
		return "yes".equals(configItem.getConfigValue());
	}
	/**
	 * 是否允许拟文人修改附件
	 */
	public static boolean allowUpdateAttachment()
	{
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		return allowUpdateAttachment(accountId);
	}
	/**
	 * 某单位下，是否允许拟文人修改附件
	 */
	public static boolean allowUpdateAttachment(long accountId)
	{
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.Allow_Update_Attachment,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, IConfigPublicKey.Allow_Update_Attachment, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return false;}
		return "yes".equals(configItem.getConfigValue());
	}
	/**
	 * 是否开启收文自动登记功能 --xiangfan
	 */
	public static boolean allowAutoRegister(){
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, Edoc_SWITCH_allowAutoRegister,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, Edoc_SWITCH_allowAutoRegister, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return false;}
		return "yes".equals(configItem.getConfigValue());
	}
	
	/**
	 * G6是否开启登记功能
	 */
	/*public static boolean isOpenRegister(){
		if(!EdocHelper.hasEdocRegister())
			return false;
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		return isOpenRegister(accountId);
	}*/
	
	/**
	 * G6是否开启登记功能
	 */
	/*public static boolean isOpenRegister(long accountId){
		if(!EdocHelper.hasEdocRegister())
			return false;
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_openRegister,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_openRegister, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return false;}
		return "yes".equals(configItem.getConfigValue());
	}*/
	
	public static String isOpenRegister() {
		if(!EdocHelper.hasEdocRegister()) {
			return "2";
		}
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		return isOpenRegister(accountId);
	}
	
	/**
	 * 2015年8月28日 修改isOpenRegister判断  简化公文交换环节，以前开关只有两个选项，是boolean类型，现在有三个选项，修改返回值 xiex
	 * G6是否开启登记功能
	 * V57新公文，没有该开关  默认取老公文开关
	 */
	public static String isOpenRegister(long accountId) {
		if(!EdocHelper.hasEdocRegister()) {
			return "2";
		}
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_openRegister,accountId);
		if(configItem==null) {
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_openRegister, ConfigItem.Default_Account_Id);
		}
		if(configItem==null || configItem.getConfigValue() == null){return "2";}
		return configItem.getConfigValue();
	}

	/**
	 * 是否允许手工输入文号
	 * @return
	 */
	public static boolean canInputEdocWordNum()
	{
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
		return canInputEdocWordNum(accountId);
	}
	
	/**
	 * OA-45359 兼职单位公文开关设置不允许手工输入文号，兼职人员登陆主岗单位还可以手动输入文号
     * 是否允许手工输入文号
     * edocAccountId 不能是当前登录单位，而是公文所属单位
     * @return
     */
    public static boolean canInputEdocWordNum(long edocAccountId)
    {
        ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_handInputEdoc,edocAccountId);
        if(configItem==null)
        {
            configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_handInputEdoc, ConfigItem.Default_Account_Id);
        }
        if(configItem==null){return true;}
        return "yes".equals(configItem.getConfigValue());
    }
	
	
	public static int getDefaultExchangeType(){
		User user=AppContext.getCurrentUser();
		long accountId=user.getLoginAccount();
        return getDefaultExchangeType(accountId);
	}
	
	//根据单位id来获取交换设置的开关
	public static int getDefaultExchangeType(long accountId){

		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_defaultExchangeType,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_defaultExchangeType, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return EdocSendRecord.Exchange_Send_iExchangeType_Dept;}
		if("depart".equals(configItem.getConfigValue()))
		{
			return EdocSendRecord.Exchange_Send_iExchangeType_Dept;
		}
		else
		{
			return EdocSendRecord.Exchange_Send_iExchangeType_Org;
		}
	}

	/**
	 * 获取 部门交换默认选择 开关设置
	 * @Author      : xuqiangwei
	 * @Date        : 2015年1月16日下午6:12:27
	 * @param accountId
	 * @return
	 */
	public static String getDefaultExchangeDeptType(long accountId){
	    
	    String ret = null;
	    
	    ConfigItem configItem = configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_defualtExchangeDeptType,accountId);
	    if(configItem == null){
	        ret = EdocSwitchHelper.SWITCH_DEFUALT_EXCHANGE_DEPT_TYPE_CREATER;
	    }else {
	        ret = configItem.getConfigValue();
        }
	    return ret;
	}
	
	/**
	 * 发送正文时是否转换为PDF格式正文
	 * 
	 * @return
	 */
//	public static boolean canEnablePdfDocChange() {
//		User user = AppContext.getCurrentUser();
//		long accountId = user.getLoginAccount();
//		ConfigItem configItem = systemConfig.getConfigItem(
//				IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_pdfEnable,
//				accountId);
//		if (configItem == null) {
//			configItem = systemConfig.getConfigItem(
//					IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_pdfEnable,
//					ConfigItem.Default_Account_Id);
//		}
//		if (configItem == null) {
//			return true;
//		}
//		return "yes".equals(configItem.getConfigValue());
//	}

	
	/**
	 * 送文单是否显示查看公文按钮
	 * @return
	 */
	public static boolean allowShowEdocInSend(long accountId){
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_allowShowEdocInSend,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_allowShowEdocInSend, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return true;}
		return "yes".equals(configItem.getConfigValue());
	}
	
	
	
	/**
	 * 签收单是否显示查看公文按钮
	 * @return
	 */
	public static boolean allowShowEdocInRec(long accountId){
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_allowShowEdocInRec,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_allowShowEdocInRec, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return true;}
		return "yes".equals(configItem.getConfigValue());
	}
	
	/**
	 * 正文套红日期开关
	 */
	public static boolean taohongriqiSwitch(long accountId)
	{
		ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, EDOC_SWITCH_taohongriqi,accountId);
		if(configItem==null)
		{
			configItem=configManager.getConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, EDOC_SWITCH_taohongriqi, ConfigItem.Default_Account_Id);
		}
		if(configItem==null){return true;}
		return "yes".equals(configItem.getConfigValue());
	}
	
	/**
     * 收文区分办文、阅文
     * @return
     */
    public static boolean showBanwenYuewen(Long accountId)
    {   
    	//需求变更，非G6版本先屏蔽阅文办文开关
    	if(!EdocHelper.isG6Version()){
    		return false;
    	}
        ConfigItem configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_banwenYuewen, accountId);
        if(configItem==null)
        {
            configItem=configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, EDOC_SWITCH_banwenYuewen, ConfigItem.Default_Account_Id);
        }
        if(configItem==null){return true;}
        return "yes".equals(configItem.getConfigValue());
    }

}
