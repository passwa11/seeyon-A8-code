package com.seeyon.apps.ext.welinkMenu;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class welinkMenuPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载welinkMenu");
}

@Override
public void destroy() {
	System.out.println("销毁welinkMenu");
}
}