/*
 * Author : xuqw
 * Date : 2019年5月20日 下午6:24:38
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.plugins.resources.CollaborationPlugin;
import com.seeyon.ctp.plugins.resources.CollaborationPluginManager;
import com.seeyon.ctp.plugins.resources.PluginResource;
import com.seeyon.ctp.plugins.resources.PluginResourceLocation;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 * <p>@Since A8-V5 7.1SP1</p>
 */
public class CollaborationPluginManagerImpl implements CollaborationPluginManager{

    
    private static Log logger = CtpLogFactory.getLog(CollaborationPluginManagerImpl.class);
    
 // 是否初始化标志
    private static boolean initFlag = false;
    
    private static final List<CollaborationPlugin> collaborationPlugins = new ArrayList<CollaborationPlugin>();

    @Override
    public void init() {
        
        if(initFlag) {
            return;
        }
     
        Map<String, CollaborationPlugin> pluginBeans = AppContext.getBeansOfType(CollaborationPlugin.class);
        
        Set<String> keys = pluginBeans.keySet();
        
        for (String beanName : keys) {

            CollaborationPlugin pluginBean = pluginBeans.get(beanName);
            
            collaborationPlugins.add(pluginBean);
        }
        
        logger.info("加载到协同插件， 共加载 " + collaborationPlugins.size() + " 个插件.");
        
        initFlag = true;
    }

	

	//@Override
	public List<PluginResource> getPluginResources(PluginResourceLocation locationParam) {
		// TODO Auto-generated method stub
		
		List<PluginResource> resources = new ArrayList<PluginResource>();
        
        for(CollaborationPlugin plugin : collaborationPlugins) {
        	
        	List<PluginResource> tempResources = plugin.loadPluginResource(locationParam);
            
			if(Strings.isNotEmpty(tempResources)) {
			    resources.addAll(tempResources);
			}
        }
        return resources;
	}



}
