package com.seeyon.apps.govdoc.service;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocWorkflowTypeEnum;
import com.seeyon.apps.govdoc.listener.GovdocAbWorkflowEventListener;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.Strings;

/**
 * 当前单子显示所属模块数据
 * @author 唐桂林
 *
 */
public class GovdocApplicationHandler extends AbstractSystemInitializer {

	private final Map<String, GovdocAbWorkflowEventListener> listenerHandlerMap  = new HashMap<String, GovdocAbWorkflowEventListener>();
	
	@Override
	public void initialize() {
		Map<String, GovdocAbWorkflowEventListener> contentHandlers = AppContext.getBeansOfType(GovdocAbWorkflowEventListener.class);
        for (String key : contentHandlers.keySet()) {
        	GovdocAbWorkflowEventListener handler = contentHandlers.get(key);
        	listenerHandlerMap.put(handler.getSubAppName(), handler);
        }
	}
	
	public GovdocAbWorkflowEventListener getWorkflowListenerHandler(String subAppName) throws BusinessException {
		if(Strings.isBlank(subAppName)) {
			subAppName = GovdocWorkflowTypeEnum.formedoc.name(); 
		}
		GovdocAbWorkflowEventListener handler = listenerHandlerMap.get(subAppName);
        if (handler == null)
            throw new BusinessException("不支持的公文流程类型：" + subAppName);
        return handler;
	}
	
}
