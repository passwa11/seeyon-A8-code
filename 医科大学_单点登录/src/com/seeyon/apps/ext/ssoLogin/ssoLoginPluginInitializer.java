package com.seeyon.apps.ext.ssoLogin;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class ssoLoginPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载ssoLogin插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁ssoLogin插件");
    }
}
