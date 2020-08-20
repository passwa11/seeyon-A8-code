package com.seeyon.apps.ext.KfqInform;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class KfqInformPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("初始化KfqInform");
    }

    @Override
    public void destroy() {
        System.out.println("销毁KfqInform");
    }
}
