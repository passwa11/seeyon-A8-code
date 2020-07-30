package com.seeyon.apps.ext.meetingInfoTip;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class meetingInfoTipPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载meetingInfoTip插件");
}

@Override
public void destroy() {
	System.out.println("销毁meetingInfoTip插件");
}
}