/**
 * Author : xuqw
 *   Date : 2015年9月6日 下午4:30:46
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.seeyon.apps.collaboration.api.NewCollDataHandlerInterface;
import com.seeyon.ctp.common.BeansOfTypeListener;
import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.api.NewCollDataHandler;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 转协同，或者新建协同初始化数据获取类</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class NewCollDataHelper {
	
	private static Log LOG = CtpLogFactory.getLog(NewCollDataHelper.class);
    
    private Map<String, NewCollDataHandlerInterface> dataHandlers = new ConcurrentHashMap<String, NewCollDataHandlerInterface>();
    private static NewCollDataHelper helper = new NewCollDataHelper();

    /**
     * 获取具体的处理类
     * @Author      : xuqw
     * @Date        : 2015年9月6日下午10:08:44
     * @param key
     * @return
     */
    public static NewCollDataHandlerInterface getHandler(String key){
        
        return helper.getDataHandler(key);
    }
    
    private NewCollDataHandlerInterface getDataHandler(String key){
        return dataHandlers.get(key);
    }
    
    /**
     * 初始化
     * @Author      : xuqw
     * @Date        : 2015年9月6日下午4:37:29
     */
    private void init(){
        initNewCollDataHandlers();
        AppContext.addBeansOfTypeListener(NewCollDataHandlerInterface.class, new BeansOfTypeListener() {
            @Override
            public void onChange(Class clazz) {
                initNewCollDataHandlers();
            }
        });
    }

    private synchronized void initNewCollDataHandlers(){
        dataHandlers.clear();
        Map<String, NewCollDataHandlerInterface> handlers = AppContext.getBeansOfType(NewCollDataHandlerInterface.class);
        for (Map.Entry<String, NewCollDataHandlerInterface> entry : handlers.entrySet()) {

            NewCollDataHandlerInterface handler = entry.getValue();
            String handlerName = handler.getHandlerName();
            if(Strings.isBlank(handlerName)){
                LOG.error("加载转协同实例错误, Spring Bean ID = " + entry.getKey());
                continue;
            }
            dataHandlers.put(handlerName, handler);
        }
    }
    
    private NewCollDataHelper(){
        init();
    }
}
