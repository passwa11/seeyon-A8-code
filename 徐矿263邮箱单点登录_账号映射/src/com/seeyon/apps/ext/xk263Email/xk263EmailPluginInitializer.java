package com.seeyon.apps.ext.xk263Email;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class xk263EmailPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载263邮箱插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁263邮箱插件");
    }
}