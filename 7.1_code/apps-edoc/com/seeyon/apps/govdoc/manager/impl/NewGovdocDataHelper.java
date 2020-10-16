package com.seeyon.apps.govdoc.manager.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.seeyon.apps.edoc.api.NewGovdocDataHandler;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;

/**
 * @Author      : caihl
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 转公文，或者新建公文初始化数据获取类</p>
 */
public class NewGovdocDataHelper {
	
	private static Log LOG = CtpLogFactory.getLog(NewGovdocDataHelper.class);
    
    private Map<String, NewGovdocDataHandler> dataHandlers = new ConcurrentHashMap<String, NewGovdocDataHandler>();
    private static NewGovdocDataHelper helper = new NewGovdocDataHelper();

    /**
     * 获取具体的处理类
     * @return
     */
    public static NewGovdocDataHandler getHandler(String key){
        
        return helper.getDataHandler(key);
    }
    
    private NewGovdocDataHandler getDataHandler(String key){
        return dataHandlers.get(key);
    }
    
    /**
     * 初始化
     */
    private void init(){
    
        Map<String, NewGovdocDataHandler> handlers = AppContext.getBeansOfType(NewGovdocDataHandler.class);
        for (Map.Entry<String, NewGovdocDataHandler> entry : handlers.entrySet()) {

        	NewGovdocDataHandler handler = entry.getValue();
            String handlerName = handler.getHandlerName();
            if(Strings.isBlank(handlerName)){
                LOG.error("加载转公文实例错误, Spring Bean ID = " + entry.getKey());
                continue;
            }
            dataHandlers.put(handlerName, handler);
        }
    }
    
    private NewGovdocDataHelper(){
        init();
    }
}
