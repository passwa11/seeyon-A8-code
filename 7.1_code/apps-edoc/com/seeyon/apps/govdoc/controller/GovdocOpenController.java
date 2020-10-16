package com.seeyon.apps.govdoc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.govdoc.constant.GovdocAppLogAction;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.manager.GovdocLogManager;
import com.seeyon.apps.govdoc.manager.GovdocOpenManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.PropertiesConfiger;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.util.Strings;

/**
 * 新公文开关控制器
 * @author 唐桂林
 *
 */
public class GovdocOpenController extends BaseController {
	
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocOpenController.class);

	private GovdocOpenManager govdocOpenManager;
	private GovdocLogManager govdocLogManager;

	
	/**
	 * 公文开关设置页面
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView listSwitch(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("govdoc/database/switch/switch");
        try {
	        Long accountId = AppContext.getCurrentUser().getLoginAccount();
	        //开关集合
	        List<ConfigItem> configItemsTemp = null;
	        List<ConfigItem> zhuanfawenConfig = new ArrayList<ConfigItem>(); //转发文开关
	        List<ConfigItem> edocMarkConfig = new ArrayList<ConfigItem>();	  //公文文号开关
	        
	        //如果是企业版，直接取集团的开关数据
	        if((Boolean)SysFlag.sys_isEnterpriseVer.getFlag()) {
	        	configItemsTemp = govdocOpenManager.listAllEdocSwitchByCategory(IConfigPublicKey.GOVDOC_SWITCH_KEY, accountId);
	        	if(Strings.isEmpty(configItemsTemp)){
	        		configItemsTemp = govdocOpenManager.listAllEdocSwitchByCategory(IConfigPublicKey.GOVDOC_SWITCH_KEY, ConfigItem.Default_Account_Id);
	        	}
	        } else {
	        	configItemsTemp = govdocOpenManager.listAllEdocSwitchByCategory(IConfigPublicKey.GOVDOC_SWITCH_KEY, accountId);
	        }
	        
	    	String memberIds = "";
	    	PluginDefinition pd=SystemEnvironment.getPluginDefinition("govdoc");
	    	for(int i=configItemsTemp.size()-1;i>=0;i--) {
	    		if("allowEditInForm".equals(configItemsTemp.get(i).getConfigItem())) {
	    			if("yes".equals(configItemsTemp.get(i).getConfigValue())) {
	    				memberIds = configItemsTemp.get(i).getExtConfigValue();
	    			}
	                if(pd!=null && 
	                "false".equals(pd.getPluginProperty("govdoc.allowEditInForm"))){
	    				configItemsTemp.remove(configItemsTemp.get(i));
	    			}    			
	    			break;
	    		}
	    		if(!"true".equals(PropertiesConfiger.getInstance().getProperty("system.gz.edition"))){
	    			if("localPrint".equals(configItemsTemp.get(i).getConfigItem())){
	    				configItemsTemp.remove(configItemsTemp.get(i));
	    			}
	    		}
	    	}
	    	for(int i=0;i<configItemsTemp.size();i++) {
	    		//为了保证前台页面显示顺序，依次获取转发文默认设置、转发文自动办结、转发文策略
	    		if("zhuanfawenTactics".equals(configItemsTemp.get(i).getConfigItem())
	    				||"zfwzidongbanjie".equals(configItemsTemp.get(i).getConfigItem())
	    				||"zhuanfawen".equals(configItemsTemp.get(i).getConfigItem())){
	    			zhuanfawenConfig.add(configItemsTemp.get(i));
	    		}
	    		if(("edocDocMark".equals(configItemsTemp.get(i).getConfigItem()))||("handInputEdoc".equals(configItemsTemp.get(i).getConfigItem()))||
	    				("edocInnerMark".equals(configItemsTemp.get(i).getConfigItem()))||("edocInnerMarkJB".equals(configItemsTemp.get(i).getConfigItem()))
	    				||("innerMarkByMax".equals(configItemsTemp.get(i).getConfigItem()))||("docMarkByMax".equals(configItemsTemp.get(i).getConfigItem()))){
	    			edocMarkConfig.add(configItemsTemp.get(i));
	    		}
	    	}
	    	
	    	Iterator<ConfigItem> it = configItemsTemp.iterator();
	    	while(it.hasNext()){
	    		ConfigItem config = it.next();
	    		if("edocLibManager".equals(config.getConfigItem())&&!"true".equals(PropertiesConfiger.getInstance().getProperty("system.gz.edition"))){
	    			it.remove();//非贵州专版移除公文库开关
				}
	    		if(("zhuanfawenTactics".equals(config.getConfigItem()))||
	    				("zfwzidongbanjie".equals(config.getConfigItem()))||
	    					("zhuanfawen".equals(config.getConfigItem()))){   			
	    			it.remove();//移除常规开关中的转发文开关
	    		}
	    		if(("edocDocMark".equals(config.getConfigItem()))||("handInputEdoc".equals(config.getConfigItem()))||
	    				("edocInnerMark".equals(config.getConfigItem()))||("edocInnerMarkJB".equals(config.getConfigItem()))
	    				||("innerMarkByMax".equals(config.getConfigItem()))||("docMarkByMax".equals(config.getConfigItem()))){
	    			it.remove();//移除常规开关中的转发文开关
	    		}
	    	}
	    	
	    	//确保任何地方看到的顺序都是一致的。
	    	Collections.sort(configItemsTemp,new Comparator<ConfigItem>() {
				@Override
				public int compare(ConfigItem o1, ConfigItem o2) {
					if(o1.getSort()!=null && o2.getSort()!=null) {
						return o1.getSort().intValue() - o2.getSort().intValue();
					} else {
						return o1.getConfigDescription().compareTo(o2.getConfigDescription());
					}
				}
			});
	    	
	    	//由于转发文开关值有可能是默认值，没有保存在数据库中，顺序号都是1，为了保证看到的顺序一致进行排序
	    	Collections.sort(zhuanfawenConfig,new Comparator<ConfigItem>() {
				@Override
				public int compare(ConfigItem o1, ConfigItem o2) {
					return o1.getConfigItem().compareTo(o2.getConfigItem());
				}
			});
			mav.addObject("identifyAuth", memberIds);
	        mav.addObject("configItems", configItemsTemp);       
	        mav.addObject("isG6", GovdocHelper.isG6Version());
	        mav.addObject("zhuanfawenConfig",zhuanfawenConfig);
	        mav.addObject("edocMarkConfig",edocMarkConfig);
        } catch(Exception e) {
        	LOGGER.error("公文开关设置页面出错", e);
        }
        return mav;
    }

	/**
	 * 公文开关设置保存
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ModelAndView saveEdocOpenSet(HttpServletRequest request, HttpServletResponse response) {
    	try {
	    	String allowEditInForm = request.getParameter("allowEditInForm");
	    	String enableRightView = request.getParameter("enableRightView");
	    	String merberIds = request.getParameter("merberIds");
	    	/*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-17 [修改功能：公文库-管理员]start*/
	    	String edocLibManagerIds = request.getParameter("edocLibManagerIds");
	    	/*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-17 [修改功能：公文库-管理员]start*/
	        User user = AppContext.getCurrentUser();
	        Long accountId = AppContext.getCurrentUser().getLoginAccount();
	        List<ConfigItem> configItems = govdocOpenManager.listAllEdocSwitchByCategory(IConfigPublicKey.GOVDOC_SWITCH_KEY, accountId);
	        Collections.sort(configItems);
	
	        String itemValue = null;
	        for(int i=0; i<configItems.size(); i++) {
	        	ConfigItem configItem = configItems.get(i);
	            itemValue = request.getParameter(configItem.getConfigItem());
	            if(itemValue != null) {
	                configItem.setConfigValue(itemValue);
	                
	                //保存意见修改框权限
	                if("allowEditInForm".equals(configItem.getConfigItem())) {
	                	if(Strings.isNotBlank(allowEditInForm)){
	                    	if("yes".equals(allowEditInForm)) {
	                    		configItem.setExtConfigValue(merberIds);
	                    	} else {
	                    		configItem.setExtConfigValue("");
	                        }
	                    }
	                }
	                // 保存是否启用右侧公文元素配置
	                if("govdocview".equals(configItem.getConfigItem())) {
	                	if("true".equals(PropertiesConfiger.getInstance().getProperty("system.gz.edition"))){
		                	if("enable".equals(enableRightView)) {
		                		configItem.setExtConfigValue("1");
		                	} else {
		                		configItem.setExtConfigValue("");
		                    }
	                	}else{
	                		configItem.setExtConfigValue("");
	                	}
	                }
	                /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-17 [修改功能：公文库-管理员]start*/
	                else if("edocLibManager".equals(configItem.getConfigItem())){
	                	if(Strings.isNotBlank(edocLibManagerIds)&&!edocLibManagerIds.equals("null")){
	                		configItem.setExtConfigValue(edocLibManagerIds);
	                	}else{
	                		configItem.setExtConfigValue("");
	                	}
	                }
	                if(configItem.getSort() == null) {
	                	configItem.setSort(i+1);
	                }
	                /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-17 [修改功能：公文库-管理员]end*/
	                govdocOpenManager.updateEdocSwitch(configItem);
	            }
	        }
	        
	        // 记录应日志
	        govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OPENSETAUTHORIZE.key(), user.getName());
	        response.getWriter().write("<script type=\"text/javascript\">parent.callbackt();</script>");
	        response.getWriter().flush();
	        response.getWriter().close();
    	} catch(Exception e) {
    		LOGGER.error("公文开关设置保存出错", e);
    	}
        return  null;
    }

    /**
     * 公文开关默认设置保存
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView defaultEdocOpenSet(HttpServletRequest request, HttpServletResponse response) {
    	try {
	        Long accountId = AppContext.getCurrentUser().getLoginAccount();
	        /*Long groupAccountId = new Long(1L);
	        List<ConfigItem> configItems = govdocOpenManager.listAllEdocSwitchByCategory(IConfigPublicKey.GOVDOC_SWITCH_KEY, accountId);
	        Collections.sort(configItems);
	        String itemValue = null;
	        //做个防护，历史原因，升级程序有可能丢了这个预置数据——自动登记
	        for(ConfigItem configItem : configItems) {
	        	ConfigItem configItemDefault= govdocOpenManager.getEdocSwitch(IConfigPublicKey.GOVDOC_SWITCH_KEY, configItem.getConfigItem(), groupAccountId);
	        	if(configItemDefault==null){
	        		continue;
	        	}
	            itemValue =configItemDefault.getConfigValue();
	            if(itemValue != null) {
	                configItem.setConfigValue(itemValue);
	                govdocOpenManager.updateEdocSwitch(configItem);
	            }
	        }*/
	        //恢复默认其实是将当前的所有公文开关配置删除，刷新当前页面
	        govdocOpenManager.deleteConfigItem(IConfigPublicKey.GOVDOC_SWITCH_KEY, accountId);
	        
	        // 记录应用日志
	        User user = AppContext.getCurrentUser();
	        govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OPEN_SETDEFAULT.key(), user.getName());
	        
	        super.rendJavaScript(response, "parent.location.reload(true);");
    	} catch(Exception e) {
    		LOGGER.error("公文开关默认设置保存出错", e);
    	}
        return null;
    }
    
    /**
	 * 公文布局设置页面
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView govdocview(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("govdoc/database/switch/govdocview");
		try {
			User user = AppContext.getCurrentUser();
			ConfigItem configItem = govdocOpenManager.getGovdocViewConfig(user.getId(), user.getLoginAccount());
			mav.addObject("configItem", configItem);
		} catch(Exception e) {
			LOGGER.error("公文布局设置出错", e);
		}
		return mav;
	}
	
	/**
	 * 公文布局设置保存
	 * @param request
	 * @param response
	 * @return
	 */
	public ModelAndView saveOwnerGovdocview(HttpServletRequest request, HttpServletResponse response) {
		String configValue = request.getParameter("configValue");
		boolean isDefault = "true".equals(request.getParameter("isDefault"));
		try {
			User user = AppContext.getCurrentUser();
			govdocOpenManager.saveOwnerGovdocView(user.getId(), user.getLoginAccount(), configValue, isDefault);
			
			// 记录应日志
	        govdocLogManager.insertAppLog(user, GovdocAppLogAction.EDOC_OPENSETAUTHORIZE.key(), user.getName());
	        response.getWriter().write("<script type=\"text/javascript\">parent.callbackt();</script>");
	        response.getWriter().flush();
	        response.getWriter().close();
	        
		} catch(Exception e) {
			LOGGER.error("公文布局设置保存出错", e);
		}
		return null;
	}
	
	public void setGovdocOpenManager(GovdocOpenManager govdocOpenManager) {
		this.govdocOpenManager = govdocOpenManager;
	}

	public void setGovdocLogManager(GovdocLogManager govdocLogManager) {
		this.govdocLogManager = govdocLogManager;
	}

}
