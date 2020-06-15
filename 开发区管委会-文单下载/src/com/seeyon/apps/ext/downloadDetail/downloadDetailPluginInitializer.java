package com.seeyon.apps.ext.downloadDetail;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class downloadDetailPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载downloadDetail插件");
}

@Override
public void destroy() {
	System.out.println("销毁downloadDetail插件");
}
}