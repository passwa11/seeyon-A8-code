package com.seeyon.apps.ext.edocDetail;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class edocDetailPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载edocDetail插件");
}

@Override
public void destroy() {
	System.out.println("销毁edocDetail插件");
}
}