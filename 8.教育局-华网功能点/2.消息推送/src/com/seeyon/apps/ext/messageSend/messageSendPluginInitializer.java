package com.seeyon.apps.ext.messageSend;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class messageSendPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("初始化messageSend插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁messageSend插件");
    }
}