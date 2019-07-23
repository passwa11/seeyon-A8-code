package com.seeyon.apps.ext.copyFile;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class copyFilePluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载copyFile插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁copyFile插件");
    }
}