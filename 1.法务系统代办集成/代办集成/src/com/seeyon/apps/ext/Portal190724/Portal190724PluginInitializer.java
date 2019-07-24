package com.seeyon.apps.ext.Portal190724;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class Portal190724PluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("正在启动Portal190724插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁Portal190724插件");
    }
}
