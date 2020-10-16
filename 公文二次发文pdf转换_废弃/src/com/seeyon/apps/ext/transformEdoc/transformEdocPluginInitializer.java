package com.seeyon.apps.ext.transformEdoc;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class transformEdocPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载transformEdoc插件");
}

@Override
public void destroy() {
	System.out.println("销毁transformEdoc插件");
}
}