package com.seeyon.apps.ext.pulldata;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class pulldataPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("����pulldata���");
}

@Override
public void destroy() {
	System.out.println("����pulldata���");
}
}