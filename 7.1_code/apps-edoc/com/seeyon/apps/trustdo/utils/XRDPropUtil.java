package com.seeyon.apps.trustdo.utils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.log.CtpLogFactory;

/**
 * 配置工具类
 * @author zhaopeng
 *
 */
public class XRDPropUtil {

	private static final Log LOGGER = CtpLogFactory.getLog(XRDPropUtil.class);
	private PropertiesConfiguration config;

	/**
	 * 初始化
	 * @param filePath
	 */
	public XRDPropUtil(String filePath) {
		try {
			this.config = new PropertiesConfiguration(filePath);
		} catch (ConfigurationException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
	}

	/**
	 * 读取配置
	 * @param key
	 * @return
	 */
	public String read(String key) {
		return config.getString(key);
	}

	/**
	 * 修改配置
	 * @param key
	 * @param value
	 */
	public void write(String key, String value) {
		config.setProperty(key, value);
		try {
			config.save();
		} catch (ConfigurationException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
	}
	
	/**
	 * 批量修改配置
	 * @param key
	 * @param value
	 */
	public void write(String[] key, String[] value) {
		
		if (key.length == value.length){
			for (int i=0;i<key.length;i++){
				config.setProperty(key[i], value[i]);
			}
		}
		try {
			config.save();
		} catch (ConfigurationException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
	}
}
