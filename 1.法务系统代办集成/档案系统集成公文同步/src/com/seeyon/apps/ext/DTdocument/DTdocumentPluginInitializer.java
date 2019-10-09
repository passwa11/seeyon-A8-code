package com.seeyon.apps.ext.DTdocument;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class DTdocumentPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("加载DTdocument插件");
}

@Override
public void destroy() {
	System.out.println("销毁DTdocument插件");
}
}