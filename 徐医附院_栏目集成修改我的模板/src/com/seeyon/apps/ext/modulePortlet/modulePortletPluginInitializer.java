package com.seeyon.apps.ext.modulePortlet;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class modulePortletPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("����modulePortlet���");
}

@Override
public void destroy() {
	System.out.println("����modulePortlet���");
}
}