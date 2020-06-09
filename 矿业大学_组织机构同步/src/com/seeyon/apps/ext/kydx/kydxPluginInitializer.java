package com.seeyon.apps.ext.kydx;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class kydxPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载矿业大学组织机构同步插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁矿业大学组织机构同步插件");
    }
}
