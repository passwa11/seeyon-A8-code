package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.Role_NAME;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.CheckRoleAccess;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;

@CheckRoleAccess(roleTypes = {Role_NAME.AccountAdministrator, Role_NAME.EdocManagement})
public class EdocOpenController extends BaseController {

    private ConfigManager configManager;
    private OrgManager orgManager;
    private AppLogManager appLogManager;
    private OrgManagerDirect orgManagerDirect;

    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
		this.orgManagerDirect = orgManagerDirect;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public AppLogManager getAppLogManager() {
        return appLogManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }
    
    private EdocRegisterManager edocRegisterManager = null;
    
    public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }
    
    public EdocRegisterManager getEdocRegisterManager() {
        return edocRegisterManager;
    }

    public ModelAndView showEdocOpenSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        ModelAndView mav = new ModelAndView("edoc/edocSwitch");
        
        boolean isG6 = EdocHelper.isG6Version();
        
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        List<ConfigItem> configItemsTemp = null;
        
        //如果是企业版，直接取集团的开关数据
        int productId = SystemProperties.getInstance().getIntegerProperty("system.ProductId");
        if(1 == productId){
        	configItemsTemp = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, accountId);
        	if(Strings.isEmpty(configItemsTemp)){
        		configItemsTemp = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, ConfigItem.Default_Account_Id);
        	}
        }else{
            configItemsTemp = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, accountId);
        }
        
        
        // 排序
        Collections.sort(configItemsTemp);
        List<ConfigItem> configItems = new ArrayList<ConfigItem>();
        for(ConfigItem configItem : configItemsTemp) {
            if(!configItem.getConfigItem().equals(IConfigPublicKey.TimeLable)) {
                configItems.add(configItem);
            }
        }
        if(configItems == null || configItems.size() <= 0) {
            configManager.saveInitCmpConfigData(IConfigPublicKey.EDOC_SWITCH_KEY, accountId);
            configItems = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, accountId);
        }
        
        //5.0公文开关屏蔽项标识
//        String isCoverFlag=SystemProperties.getInstance().getProperty("edoc.system.open");
        
        String isCoverFlag = "false";//V5-G6和A8自定义分类开关 不需要
        if("false".equals(isCoverFlag)){
        	List<ConfigItem> configItems_temp = new ArrayList<ConfigItem>();
        	boolean hasEdocRegister = EdocHelper.hasEdocRegister();
            for(ConfigItem configItem : configItems) {
            	
            	
            	//V5不需要  登记开关
            	if(!hasEdocRegister && EdocSwitchHelper.EDOC_SWITCH_openRegister.equals(configItem.getConfigItem())){
                	configItems_temp.add(configItem);
                }
            	
            	//之前的自动登记开关不要，先在这里过滤掉
            	if(EdocSwitchHelper.Edoc_SWITCH_allowAutoRegister.equals(configItem.getConfigItem())){
                	configItems_temp.add(configItem);
                }
            	
            	if(hasEdocRegister && EdocSwitchHelper.EDOC_SWITCH_openRegister.equals(configItem.getConfigItem())){
            		//页面中需要先获得 收文登记开关的 当前初始状态
            		mav.addObject("register_switch_init_value", configItem.getConfigValue());
            		
                    V3xOrgRole role = orgManager.getRoleByName("RegisterEdoc", accountId);
                    if(role!=null){
                    	if("yes".equals(configItem.getConfigValue())){
                			//显示收文登记角色
                			role.setBond(OrgConstants.ROLE_BOND.ACCOUNT.ordinal());
                    	}else{
                			role.setBond(OrgConstants.ROLE_BOND.NULL2.ordinal());
                		}
                    	orgManagerDirect.updateRole(role);
                    }
            		
            		
            	}
            	
                if("sendCustomType".equals(configItem.getConfigItem())||      //收文自定义分类开关屏蔽
                   "recCustomType".equals(configItem.getConfigItem())||    
                   "allowDistributeModifyContent".equals(configItem.getConfigItem())) {  
                	configItems_temp.add(configItem);
                }
            }
            configItems.removeAll(configItems_temp); 
        }
        //需求变更，非G6版本先屏蔽阅文办文开关
        if(!isG6){
            Iterator<ConfigItem> item = configItems.iterator();  
            while(item.hasNext()){  
            	ConfigItem element = item.next();
            	String itemName = element.getConfigItem();
        		if(itemName.equals(EdocSwitchHelper.EDOC_SWITCH_banwenYuewen)){
        			item.remove();
        		}
            }
		}
        
        //确保任何地方看到的顺序都是一致的。
        Collections.sort(configItems, new Comparator<ConfigItem>() {
            public int compare(ConfigItem o1, ConfigItem o2) {
                return o1.getConfigItem().compareTo(o2.getConfigItem());
            }
        });
        mav.addObject("configItems", configItems);
        mav.addObject("isG6", isG6);
        return mav;
    }

    /**
     * 读取公文发起权
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView showEdocSendSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("edoc/edocSendSet");
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        String edocSendCreates = "";
        String edocRecCreates = "";
        String edocSignCreates = "";
        // 唐桂林添加收文分发权展示功能 2011-10-09
        String edocRecDistributeCreates = "";
        // 公文归档修改权限
        String edocArchiveModifyCreates = "";
        ConfigItem edocSendItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_SEND, accountId);
        ConfigItem edocRecItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC, accountId);
        ConfigItem edocSignItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_SIGN, accountId);
        ConfigItem edocRecDistributeItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC_DISTRIBUTE, accountId);
        ConfigItem edocArchiveModifyItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, "v3x_edoc_create_acc_archive_modify", accountId);
        if(edocSendItem != null) {
            edocSendCreates = edocSendItem.getExtConfigValue();
        }
        if(edocRecItem != null) {
            edocRecCreates = edocRecItem.getExtConfigValue();
        }
        if(edocSignItem != null) {
            edocSignCreates = edocSignItem.getExtConfigValue();
        }
        if(edocRecDistributeItem != null) {
            edocRecDistributeCreates = edocRecDistributeItem.getExtConfigValue();
        }
        if(edocArchiveModifyItem != null) {
            edocArchiveModifyCreates = edocArchiveModifyItem.getExtConfigValue();
        }
        mav.addObject("edocSendCreates", edocSendCreates);
        mav.addObject("edocRecCreates", edocRecCreates);
        mav.addObject("edocSignCreates", edocSignCreates);
        mav.addObject("edocRecDistributeCreates", edocRecDistributeCreates);
        mav.addObject("edocArchiveModifyCreates", edocArchiveModifyCreates);
        return mav;
    }

    /**
     * 保存公文发起权
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveEdocSendSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        String edocSendCreates = request.getParameter("edocSendCreates");
        String edocRecCreates = request.getParameter("edocRecCreates");
        String edocSignCreates = request.getParameter("edocSignCreates");
        // 唐桂林公文收文分发权保存功能 2011-10-09
        String edocRecDistributeCreates = request.getParameter("edocRecDistributeCreates");
        String edocArchiveModifyCreates = request.getParameter("edocArchiveModifyCreates");
        ConfigItem edocSendItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_SEND, accountId);
        ConfigItem edocRecItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC, accountId);
        ConfigItem edocSignItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_SIGN, accountId);
        ConfigItem edocRecDistributeItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC_DISTRIBUTE, accountId);
        ConfigItem edocArchiveModifyItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, "v3x_edoc_create_acc_archive_modify", accountId);
        if(edocSendItem == null) {
            edocSendItem = getNewConfigItem(accountId, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_SEND, edocSendCreates);
            configManager.addConfigItem(edocSendItem);
        } else {
            edocSendItem.setExtConfigValue(edocSendCreates);
            configManager.updateConfigItem(edocSendItem);
        }
        if(edocRecItem == null) {
            edocRecItem = getNewConfigItem(accountId, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC, edocRecCreates);
            configManager.addConfigItem(edocRecItem);
        } else {
            edocRecItem.setExtConfigValue(edocRecCreates);
            configManager.updateConfigItem(edocRecItem);
        }
        if(edocSignItem == null) {
            edocSignItem = getNewConfigItem(accountId, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_SIGN, edocSignCreates);
            configManager.addConfigItem(edocSignItem);
        } else {
            edocSignItem.setExtConfigValue(edocSignCreates);
            configManager.updateConfigItem(edocSignItem);
        }
        if(edocRecDistributeItem == null) {
            edocRecDistributeItem = getNewConfigItem(accountId, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC_DISTRIBUTE, edocRecDistributeCreates);
            configManager.addConfigItem(edocRecDistributeItem);
        } else {
            edocRecDistributeItem.setExtConfigValue(edocRecDistributeCreates);
            configManager.updateConfigItem(edocRecDistributeItem);
        }
        if(edocArchiveModifyItem == null) {
            edocArchiveModifyItem = getNewConfigItem(accountId, "v3x_edoc_create_acc_archive_modify", edocArchiveModifyCreates);
            configManager.addConfigItem(edocArchiveModifyItem);
        } else {
            edocArchiveModifyItem.setExtConfigValue(edocArchiveModifyCreates);
            configManager.updateConfigItem(edocArchiveModifyItem);
        }
        // 记录日志
        appLogManager.insertLog(user, AppLogAction.Edoc_SendSetAuthorize, user.getName());
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        super.printV3XJS(out);
        out.println("<script>");
        // out.println("alert('操作成功!');");
        out.println("alert('" + ResourceBundleUtil.getString("www.seeyon.com.v3x.form.resources.i18n.FormResources", "formapp.saveoperok.label") + "')");
        out.println("parent.location.reload(true);");
        out.println("</script>");
        return null;
    }

    private ConfigItem getNewConfigItem(Long accountId, String item, String value) {
        ConfigItem edocSendItem = new ConfigItem();
        edocSendItem.setIdIfNew();
        edocSendItem.setConfigCategory(IConfigPublicKey.EDOC_CREATE_KEY);
        edocSendItem.setConfigItem(item);
        edocSendItem.setExtConfigValue(value);
        edocSendItem.setOrgAccountId(accountId);
        return edocSendItem;
    }

    public ModelAndView saveEdocOpenSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //ModelAndView mav = new ModelAndView("edoc/edocSwitch");
        User user = AppContext.getCurrentUser();
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        //List<ConfigItem> groupconfigItems = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, ConfigItem.Default_Account_Id);
        List<ConfigItem> configItems = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, accountId);
        Collections.sort(configItems);

        String itemValue = null;
        for(Iterator<ConfigItem> it = configItems.iterator(); it.hasNext();) {
            ConfigItem configItem = it.next();
            if(configItem.getConfigItem().equals(IConfigPublicKey.TimeLable))
                it.remove();
            itemValue = request.getParameter(configItem.getConfigItem());
            if(itemValue != null) {
                configItem.setConfigValue(itemValue);
                configManager.updateConfigItem(configItem);
            }
        }
        // 记录应日志
           appLogManager.insertLog(user, AppLogAction.Edoc_OpenSetAuthorize, user.getName());
        //mav.addObject("configItems", configItems);
        //mav.addObject("operateResult", true);
        return  super.redirectModelAndView("/edocOpenController.do?method=showEdocOpenSet");
        //return mav;
    }

    public ModelAndView defaultEdocOpenSet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //ModelAndView mav = new ModelAndView("edoc/edocSwitch");
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        Long groupAccountId = 1L;
        List<ConfigItem> configItems = configManager.listAllConfigByCategory(IConfigPublicKey.EDOC_SWITCH_KEY, accountId);
        Collections.sort(configItems);
        String itemValue = null;
        //做个防护，历史原因，升级程序有可能丢了这个预置数据——自动登记
//        EdocRegisterManager registerManager = (EdocRegisterManager)AppContext.getBean("edocRegisterManager");
//        registerManager.saveAutoRegisterSwitch(1L);
        for(ConfigItem configItem : configItems) {
        	ConfigItem configItemDefault= configManager.getConfigItem(IConfigPublicKey.EDOC_SWITCH_KEY, configItem.getConfigItem(), groupAccountId);
        	if(configItemDefault==null){
        		continue;
        	}
            itemValue =configItemDefault.getConfigValue();
            if(itemValue != null) {
                configItem.setConfigValue(itemValue);
                configManager.updateConfigItem(configItem);
            }
        }
        // 记录应用日志
        User user = AppContext.getCurrentUser();
        appLogManager.insertLog(user, AppLogAction.Edoc_Open_SetDefault, user.getName());
        //mav.addObject("configItems", configItems);
        //mav.addObject("operateResult", true);
        return  super.redirectModelAndView("/edocOpenController.do?method=showEdocOpenSet");
    }

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
