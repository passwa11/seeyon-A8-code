package com.seeyon.apps.ext.zMember;

import com.seeyon.ctp.common.AbstractSystemInitializer;

/**
 * 周刘成   2019/6/20
 */
public class zMemberPluginInitializer extends AbstractSystemInitializer {
    @Override
    public void initialize() {
        System.out.println("周：加载人员插件");
    }

    @Override
    public void destroy() {
        System.out.println("周：销毁人员插件");
    }
}
