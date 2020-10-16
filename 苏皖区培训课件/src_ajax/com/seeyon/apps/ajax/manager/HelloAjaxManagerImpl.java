package com.seeyon.apps.ajax.manager;

import java.util.Map;

import com.seeyon.ctp.util.annotation.AjaxAccess;

public class HelloAjaxManagerImpl implements HelloAjaxManager {

	/**
	 * ajax方法不要抛出异常，返回给前台的一定是提示消息，后台记录错误日志
	 * 如果是controller层调用，那么manager、dao都不要处理异常，统一抛给controller层进行异常处理
	 * rest接口也一样，不要抛出异常，统一返回消息
	 */
	@Override
	@AjaxAccess
	public Map<String, Object> hello(int key, Map<String, Object> param) {
		param.put("key", ++key);
		return param;
	}

}
