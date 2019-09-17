package com.seeyon.apps.govdoc.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.util.Strings;

/**
 * 新公文参数工具类
 * @author 唐桂林
 *
 */
public class GovdocParamUtil {

	public static String getString(HttpServletRequest request, String name) {
		return request.getParameter(name);
	}
	
	public static String getString(HttpServletRequest request, String name, String defaultValue) {
		if(Strings.isBlank(request.getParameter(name))) {
			return defaultValue;
		}
		if("undefined".equals(request.getParameter(name))) {
			return defaultValue;
		}
		return request.getParameter(name);
	}
	
	public static Long getLong(HttpServletRequest request, String name) {
		if(Strings.isBlank(request.getParameter(name)) || ("null").equals(request.getParameter(name))) {
			return null;
		}
		return Long.parseLong(request.getParameter(name));
	}
	
	public static Long getLong(HttpServletRequest request, String name, Long defaultValue) {
		if(Strings.isBlank(request.getParameter(name))) {
			return defaultValue;
		}
		return Long.parseLong(request.getParameter(name));
	}
	
	public static Integer getInteger(HttpServletRequest request, String name) {
		if(Strings.isBlank(request.getParameter(name)) || "null".equals(request.getParameter(name))) {
			return null;
		}
		return Integer.parseInt(request.getParameter(name));
	}
	
	public static Integer getInteger(HttpServletRequest request, String name, Integer defaultValue) {
		if(Strings.isBlank(request.getParameter(name)) || "null".equals(request.getParameter(name))) {
			return defaultValue;
		}
		return Integer.parseInt(request.getParameter(name));
	}
	
	public static Boolean getBoolean(HttpServletRequest request, String name) {
		if(Strings.isBlank(request.getParameter(name))) {
			return null;
		}
		return Boolean.parseBoolean(request.getParameter(name));
	}
	
	public static Boolean getBoolean(HttpServletRequest request, String name, Boolean defaultValue) {
		if(Strings.isBlank(request.getParameter(name))) {
			return defaultValue;
		}
		return Boolean.parseBoolean(request.getParameter(name));
	}

	public static String getString(Map<String, Object> params, String name) {
		if(params==null || params.get(name)==null) {
			return "";
		}
		return (String)params.get(name);
	}
	
	public static Long getLong(Map<String,Object> params, String name) {
		if(params==null || params.get(name)==null || "".equals(params.get(name))) {
			return null;
		}
		return Long.parseLong(params.get(name).toString());
	}
	
	public static Integer getInteger(Map<String,Object> params, String name) {
		if(params==null || params.get(name)==null || "".equals(params.get(name))) {
			return null;
		}
		return Integer.parseInt(params.get(name).toString());
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean getBoolean(Map params, String name) {
		if(params==null || params.get(name)==null || "".equals(params.get(name))) {
			return false;
		}
		return Boolean.parseBoolean(params.get(name).toString());
	}
	
	public static boolean isNotNull(Long id) {
		if(id != null && id.longValue() !=0 && id.longValue()!=-1) {
			return true;
		}
		return false;
	}
	
	public static boolean isNull(Long id) {
		if(id == null || id.longValue() ==0 || id.longValue()==-1) {
			return true;
		}
		return false;
	}
	
}
