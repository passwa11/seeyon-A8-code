/**
 * Author: wangchw
 * Rev:WorkFlowAppExtendInvokeManager.java
 * Date: 20122012-11-6下午06:12:19
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
*/
package com.seeyon.ctp.workflow.manager;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.workflow.wapi.WorkflowFormDataMapManager;

/**
 * <p>Title: T4工作流</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * <p>Author: wangchw</p>
 * @since CTP2.0
*/
public class WorkflowFormDataMapInvokeManager {
    
    /**
     * 日志记录
     */
    public static final Logger logger = Logger.getLogger(WorkflowFormDataMapInvokeManager.class);
    
    /**
     * 初始化标记
     */
    private static boolean isInit= false;
    
    /**
     * 调用外部催办manager的map属性。
     */
    private static Map<String, WorkflowFormDataMapManager> hastenMap;
    
    
    /**
     * 应用催办管理器初始化。
     */
    public static void init(){
        if(isInit){
            return;
        }
        isInit= true;
        final Map<String, WorkflowFormDataMapManager> initsMap = AppContext.getBeansOfType(WorkflowFormDataMapManager.class);
        if(initsMap==null || initsMap.isEmpty()){
            return;
        }
        hastenMap = new HashMap<String, WorkflowFormDataMapManager>();
        for(Map.Entry<String, WorkflowFormDataMapManager> entry:initsMap.entrySet()){
            if(entry==null){
                continue;
            }
            String name = entry.getKey();
            WorkflowFormDataMapManager hastenManager = entry.getValue();
            if(hastenManager!=null && hastenManager.getAppName()!=null){
                hastenMap.put(hastenManager.getAppName(), hastenManager);
            } else {
                logger.info("id或name="+name+"的WorkFlowAppExtendInvokeManager初始化失败，getAppName的返回值=null");
            }
        }
    }
    
    /**
     * 获得WorkFlowAppExtendManager
     * @param appName
     * @return
     */
    public static WorkflowFormDataMapManager getAppManager(String appName){
        if(ApplicationCategoryEnum.edocRec.name().equals(appName)
                || ApplicationCategoryEnum.edocSend.name().equals(appName)
                || ApplicationCategoryEnum.edocSign.name().equals(appName)
                || "sendEdoc".equals(appName) 
                || "recEdoc".equals(appName)
                || "signReport".equals(appName)){//公文种类较多，兼容处理下
            appName= "edoc";
        }
        if("sendInfo".equals(appName)){
            appName= "info";
        }
        WorkflowFormDataMapManager hastenManager = null;
        if(hastenMap!=null){
            hastenManager = hastenMap.get(appName);
        }
        return hastenManager;
    }
}
