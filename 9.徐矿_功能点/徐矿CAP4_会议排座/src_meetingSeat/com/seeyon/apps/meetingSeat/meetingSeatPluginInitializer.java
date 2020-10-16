package com.seeyon.apps.meetingSeat;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class meetingSeatPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("========加载会议排座meetingSeat插件========");
    }

    @Override
    public void destroy() {
        System.out.println("========销毁会议排座meetingSeat插件========");
    }
}