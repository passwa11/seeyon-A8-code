package com.seeyon.apps.ext.Sso0715;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class Sso0715PluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载Sso0715插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁Sso0715插件");
    }
}