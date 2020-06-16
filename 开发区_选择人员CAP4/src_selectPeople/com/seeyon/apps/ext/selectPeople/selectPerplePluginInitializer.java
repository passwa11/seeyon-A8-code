package com.seeyon.apps.ext.selectPeople;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class selectPerplePluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("加载人员选择selectPeople插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁人员选择selectPeople插件");
    }
}