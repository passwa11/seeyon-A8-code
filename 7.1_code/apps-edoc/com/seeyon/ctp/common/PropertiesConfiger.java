package com.seeyon.ctp.common;

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
/**
 * 
 * @author liaoy 
 *
 */
public class PropertiesConfiger extends PropertyPlaceholderConfigurer {
	private Properties props;
	private static PropertiesConfiger propertiesConfiger;

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
			throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		this.props = props;
	}

	public String getProperty(String key) {
		if(this.props == null) {
			this.props = new Properties();
		}
		return this.props.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		if(this.props == null) {
			this.props = new Properties();
		}
		return this.props.getProperty(key, defaultValue);
	}
	
	public static PropertiesConfiger getInstance(){
		if(null==propertiesConfiger){
			synchronized (PropertiesConfiger.class) {
				if(null==propertiesConfiger){
					propertiesConfiger=(PropertiesConfiger) AppContext.getBean("propertiesConfiger");
				}
			}
		}
		if(propertiesConfiger == null) {
			propertiesConfiger = new PropertiesConfiger();
		}
		return propertiesConfiger;
	}
}
