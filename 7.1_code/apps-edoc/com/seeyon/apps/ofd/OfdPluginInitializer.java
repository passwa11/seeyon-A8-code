package com.seeyon.apps.ofd;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.plugin.PluginInitializer;

public class OfdPluginInitializer implements PluginInitializer {

	@Override
	public boolean isAllowStartup(PluginDefinition arg0, Logger arg1) {
		return "true".equals(arg0.getPluginProperty("ofd.enable"));
	}
	
}
