/*
 * $Revision: 7242 $
 * $Date: 2014-04-04 17:03:38 +0800 (周五, 04 四月 2014) $
 * $Id: MHttpServletRequest.java 7242 2014-04-04 09:03:38Z wangx $
 * ====================================================================
 * Copyright © 2012 Beijing seeyon software Co..Ltd..All rights reserved.
 *
 * This software is the proprietary information of Beijing seeyon software Co..Ltd.
 * Use is subject to license terms.
 */
package com.seeyon.apps.trustdo.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 可以设置参数的HttpServletRequest类
 * @author wangx
 * @since JDK 1.5
 * @version 1.0
 */
public class M3HttpServletRequest extends HttpServletRequestWrapper {
    private Map<String, Object> parameters = new HashMap<String, Object>(20);
    
    /**
     * 构造一个自定义的Http请求对象
     * @param request Servlet提供的request对象
     */
    public M3HttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    /**
     * 设置HttpServletRequest请求中的参数
     * 
     * @param name
     *            参数名称
     * @param value
     *            参数值
     */
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    public void setParameterMap(Map<String, Object> params) {
        parameters.putAll(params);
    }
    
    @Override
    public String getParameter(String name) {
    	
        if (parameters.containsKey(name)) {
        	// TODO 修复BUG [OA-99518] Pu Weibin
        	if (parameters.get(name) != null) {
        		if (parameters.get(name) instanceof String[]) {
        			Object[] obj = (Object[])parameters.get(name);
        			return (String)obj[0];
        		}
        	}
        	return String.valueOf(parameters.get(name));
        }
        return super.getParameter(name);
    }
    
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    @Override
    public Map getParameterMap() {
        Map<String, String[]> params = super.getParameterMap();
        parameters.putAll(params);
        return parameters;
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {
        String [] result = null;
        if(parameters.containsKey(name)) {
            result = (String[])parameters.get(name);
        } else {
            result = super.getParameterValues(name);
        }
        return result;
    }
}