package com.seeyon.apps.ext.modulePortlet;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class modulePortletPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载modulePortlet");
}

@Override
public void destroy() {
	System.out.println("销毁modulePortlet");
}
}