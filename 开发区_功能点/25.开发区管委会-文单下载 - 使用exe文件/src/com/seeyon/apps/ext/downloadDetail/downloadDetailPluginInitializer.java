package com.seeyon.apps.ext.downloadDetail;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class downloadDetailPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("reload downloadDetail");
    }

    @Override
    public void destroy() {
        System.out.println("destroy downloadDetail");
    }
}
