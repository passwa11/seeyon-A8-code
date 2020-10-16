package com.seeyon.apps.wpstrans;

import org.apache.log4j.Logger;

import com.seeyon.apps.wpstrans.util.WpsTransConstant;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.plugin.PluginInitializer;

public class WpsTransPluginInitializer implements PluginInitializer {

	@Override
	public boolean isAllowStartup(PluginDefinition arg0, Logger arg1) {
		WpsTransConstant.WPSTRANS_ENABLE = "true".equals(arg0.getPluginProperty("wpstrans.enable"));
		if (!WpsTransConstant.WPSTRANS_ENABLE) {
			return false;
		}

		WpsTransConstant.WPSTRANS_FILE_SERVICE_URL = arg0.getPluginProperty("wpstrans.file.service.url");
		WpsTransConstant.WPSTRANS_FILE_SERVICE_UPLOAD = arg0
				.getPluginProperty("wpstrans.file.service.operation.upload");
		WpsTransConstant.WPSTRANS_FILE_SERVICE_DOWNLOAD = arg0
				.getPluginProperty("wpstrans.file.service.operation.download");
		WpsTransConstant.WPSTRANS_FILE_SERVICE_HANDSHAKE = arg0
				.getPluginProperty("wpstrans.file.service.operation.handshake");

		WpsTransConstant.WPSTRANS_SERVICE_IP = arg0.getPluginProperty("wpstrans.trans.service.ip");
		WpsTransConstant.WPSTRANS_SERVICE_PORT = arg0.getPluginProperty("wpstrans.trans.service.port");
		WpsTransConstant.WPSTRANS_SERVICE_PATH = arg0.getPluginProperty("wpstrans.trans.service.path");
		WpsTransConstant.WPSTRANS_SERVICE_CRON = arg0.getPluginProperty("wpstrans.trans.service.cron");

		WpsTransConstant.WPSTRANS_FOLDER_PATH = SystemEnvironment.getSystemTempFolder() + "/wpstrans";
		return true;
	}

}
