package com.seeyon.apps.ext.kypending;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class kypendingPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("����kypending���");
}

@Override
public void destroy() {
	System.out.println("����kypending���");
}
}