package com.seeyon.apps.ext.DTdocument;

import com.seeyon.ctp.common.AbstractSystemInitializer;
public class DTdocumentPluginInitializer extends AbstractSystemInitializer{

@Override
public void initialize() {
	System.out.println("����DTdocument���");
}

@Override
public void destroy() {
	System.out.println("����DTdocument���");
}
}