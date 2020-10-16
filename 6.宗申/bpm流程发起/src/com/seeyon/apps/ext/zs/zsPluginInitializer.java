package com.seeyon.apps.ext.zs;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class zsPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("初始化");
    }

    @Override
    public void destroy() {
        System.out.println("销毁");
    }
}