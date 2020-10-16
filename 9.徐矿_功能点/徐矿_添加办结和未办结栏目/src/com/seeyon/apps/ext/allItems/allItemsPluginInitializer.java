package com.seeyon.apps.ext.allItems;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class allItemsPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载allItems插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁allItems插件");
    }
}
