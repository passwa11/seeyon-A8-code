package com.seeyon.apps.ext.kfqMessage;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class kfqMessagePluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("����kfqMessage���");
}

@Override
public void destroy() {
	System.out.println("����kfqMessage���");
}
}