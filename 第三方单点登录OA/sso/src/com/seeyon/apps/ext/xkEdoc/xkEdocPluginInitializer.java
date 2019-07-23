package com.seeyon.apps.ext.xkEdoc;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class xkEdocPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载徐矿公文插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁徐矿公文插件");
    }
}