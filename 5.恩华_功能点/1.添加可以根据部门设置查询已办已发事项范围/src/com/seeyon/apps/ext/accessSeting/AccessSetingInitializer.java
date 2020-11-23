package com.seeyon.apps.ext.accessSeting;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class AccessSetingInitializer extends AbstractSystemInitializer {
    @Override
    public void initialize() {
        System.out.println("启动AccessSetingInitializer插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁AccessSetingInitializer插件");
    }
}
