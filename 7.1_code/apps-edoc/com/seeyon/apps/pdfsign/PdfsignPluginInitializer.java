package com.seeyon.apps.pdfsign;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.plugin.PluginInitializer;

/**
 * 枭哥取的插件名
 * @author tanggl
 *
 */
public class PdfsignPluginInitializer implements PluginInitializer {
	
	@Override
	public boolean isAllowStartup(PluginDefinition arg0, Logger arg1) {
		return "true".equals(arg0.getPluginProperty("pdfsign.enabled")) || "1".equals(arg0.getPluginProperty("pdfsign.enabled"));
	}

}
