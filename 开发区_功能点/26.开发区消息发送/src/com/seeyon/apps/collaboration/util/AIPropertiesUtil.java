package com.seeyon.apps.collaboration.util;

import java.io.File;
import java.util.Properties;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.config.PropertiesLoader;

public class AIPropertiesUtil {
	//配置文件的路径
	private static final String FILE_PATH = AppContext.getCfgHome().getPath() + "/plugin/ai/ai_processing.properties";
	
	public static Properties loadAIProperties() {
		File file = new File(FILE_PATH);
		return PropertiesLoader.load(file);
	}

}
