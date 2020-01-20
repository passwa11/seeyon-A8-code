/**
 * $Author: wangwy $
 * $Rev: 17416 $
 * $Date:: 2015-06-08 16:14:21#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.i18n;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.datai18n.manager.DataI18nManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import java.text.MessageFormat;
import java.util.*;

/**
 * <p>Title: T1开发框架</p>
 * <p>Description: i18n资源获取工具类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public final class ResourceUtil {
    private static final Log LOGGER        = CtpLogFactory.getLog(ResourceUtil.class);
    private static final String DEFAULT_VALUE = "";
    private static String i18nSuffixUpperCase = "";
    private static String i18nSuffixLowerCase = "";
    
    static {
    	String i18nSuffix = "";
    	if(!SystemEnvironment.isRemoteMode()) {
    		i18nSuffix = ProductEditionEnum.getCurrentProductEditionEnum().getI18nSuffix();
    	}
        i18nSuffixUpperCase = i18nSuffix.toUpperCase();
        i18nSuffixLowerCase = i18nSuffix.toLowerCase();
    }
    
    private static DataI18nManager dataI18nManager;
    public static DataI18nManager getDataI18nManager() {
        if (dataI18nManager == null) {
        	dataI18nManager = (DataI18nManager) AppContext.getBean("dataI18nManager");
        }

        return dataI18nManager;
    }
    

    /**
     * 根据指定key获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @return 资源内容
     */
    public static String getString(String key) {
        return getStringByParams(key);
    }
    
    /**
     * 根据指定key获取i18n资源，默认取当前用户所使用的Locale
     * 先按照当前人员登陆语言取
     * 再按照系统默认语言取
     * 如果都没取到，按照语言顺序，优先取第一个。
     *
     * @param dataI18nId 多语言id
     * @return 资源内容
     * @throws BusinessException 
     */
    public static String getString(Long dataI18nId) throws BusinessException {
        
        return getString(dataI18nId, AppContext.getLocale());
    }

    public static String getString(Long dataI18nId, Locale locale) {
        
        return getDataI18nManager().getDataI18nValueWithCompensate(dataI18nId, locale);
    }
    /**
     * 获取用户输入数据的国际化信息， 兼容升级数据
     * 
     * @param dataI18n
     * @return
     *
     * @Since A8-V5 7.1SP1
     * @Author      : xuqw
     * @Date        : 2019年6月22日下午2:52:16
     *
     */
    public static String getDataI18nString(String dataI18n) {
        
        if(!Strings.isLong(dataI18n)) {
            return dataI18n;
        }
        return getDataI18nString(dataI18n, AppContext.getLocale());
    }
    public static String getDataI18nString(String dataI18n,Locale locale) {
        
        if(!Strings.isLong(dataI18n)) {
            return dataI18n;
        }
        return getString(Long.valueOf(dataI18n),locale);
    }

    /**
     * 根据指定key和参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param param1 资源参数
     * @return 格式化后资源内容
     */
    public static String getString(String key, Object param1) {
        return getStringByParams(key, param1);
    }

    /**
     * 根据指定key和参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param param1 资源参数
     * @param param2 资源参数
     * @return 格式化后资源内容
     */
    public static String getString(String key, Object param1, Object param2) {
        return getStringByParams(key, param1, param2);
    }

    /**
     * 根据指定key和参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param param1 资源参数
     * @param param2 资源参数
     * @param param3 资源参数
     * @return 格式化后资源内容
     */
    public static String getString(String key, Object param1, Object param2, Object param3) {
        return getStringByParams(key, param1, param2, param3);
    }

    /**
     * 根据指定key和参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param param1 资源参数
     * @param param2 资源参数
     * @param param3 资源参数
     * @param param4 资源参数
     * @return 格式化后资源内容
     */
    public static String getString(String key, Object param1, Object param2, Object param3, Object param4) {
        return getStringByParams(key, param1, param2, param3, param4);
    }

    /**
     * 根据指定key和参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param param1 资源参数
     * @param param2 资源参数
     * @param param3 资源参数
     * @param param4 资源参数
     * @param param5 资源参数
     * @return 格式化后资源内容
     */
    public static String getString(String key, Object param1, Object param2, Object param3, Object param4, Object param5) {
        return getStringByParams(key, param1, param2, param3, param4, param5);
    }

    /**
     * 根据指定key和动态参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param parameters 动态资源参数
     * @return 格式化后资源内容
     */
    public static String getStringByParams(String key, Object... parameters) {
    	return getStringByParams(AppContext.getLocale(), key, parameters);
    }

	public static String getStringByParams(Locale locale, String key,
			Object... parameters) {
		if( key == null ) {
    		return key;
    	}
		
//        Map<String, String> resources = ResourceLoader.getResources(locale);
//        if(resources==null) return key;
		//使用版本特定后缀的国际化KEY
        String suffixKey = key + i18nSuffixUpperCase;
        String message = ResourceLoader.getResources(locale,suffixKey);
		//如果没有取到，就使用默认的KEY
        if(message==null || "".equals(message.trim())){
        	suffixKey = key + i18nSuffixLowerCase;
        	message = ResourceLoader.getResources(locale,suffixKey);
        }
        if(message==null || "".equals(message.trim())){
        	message = ResourceLoader.getResources(locale,key);
        }
        if (message != null && parameters != null){
            message = format(message, locale, parameters);
        }

        if (message == null){
            //如果是Long类型，取DataI8n里的数据
            if(Strings.isLong(key)) {
                return getString(Long.valueOf(key),locale);
            }
            return key;
        } else{
            return message;
        }
	}

    /**
     * 根据指定key和动态参数获取i18n资源，默认取当前用户所使用的Locale
     *
     * @param key 资源key
     * @param locale 指定locale
     * @param parameters 动态资源参数
     * @return 格式化后资源内容
     */
    public static String getStringByLocaleAndParams(String key,Locale locale,Object... parameters) {
    	if(locale == null){
        	locale= AppContext.getLocale();
        }

//        Map<String, String> resources = ResourceLoader.getResources(locale);
//        if(resources==null) return key;
        String message = ResourceLoader.getResources(locale,key);
        if (message != null && parameters != null){
            message = format(message, locale, parameters);
        }

        if (message == null)
            return key;
        else
            return message;
    }

    private static String format(String message, Locale locale, Object... parameters) {
        try {
            Object[] p = parameters;
            if((p.length==1)&&(p[0] instanceof String[])){
               p = (Object[])p[0];
            }
            MessageFormat formatter = new MessageFormat(message, locale);
            message = formatter.format(p);
        } catch (Exception e) {
            LOGGER.error(message + "\t" + StringUtils.join(parameters, ", "), e);
            return DEFAULT_VALUE;
        }

        return message;
    }
  	/**
  	 * 判断国际化资源名称是否重复
  	 * @param nameList
  	 * @param compairName
  	 * @return
  	 * @throws BusinessException
  	 */
  	public static boolean compairI18nInfoByName(List<String> nameList, String compairName) throws BusinessException{
  		List<String> compairnameList = geti18nInfoByName(compairName);
  		List<String> newNameList = new ArrayList<String>();
  		newNameList.addAll(nameList);
  		//取交集
  		newNameList.retainAll(compairnameList);
  		if (Strings.isNotEmpty(newNameList)) {
  			return true;
  		}
  		return false;
  	}
  	//根据名称获取所有国际化值
  	public static List<String> geti18nInfoByName(String name) throws BusinessException {
  		List<String> resultList = new ArrayList<String>();
  		Map<String, String> i18nMap = new LinkedHashMap<String, String>();
  		if(Strings.isLong(name)){
  			i18nMap = dataI18nManager.getAllDataI18nValueMap(Long.parseLong(name));
  			if (MapUtils.isNotEmpty(i18nMap)) {
  				for (String key : i18nMap.keySet()) {
  					resultList.add(i18nMap.get(key));
  				}
  			} else {
  				resultList.add(ResourceUtil.getString(name));
  			}
  		} else {
  			resultList.add(ResourceUtil.getString(name));
  		}
  		return resultList;
  	}

}
