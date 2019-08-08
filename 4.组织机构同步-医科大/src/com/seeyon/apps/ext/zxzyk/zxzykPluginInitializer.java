package com.seeyon.apps.ext.zxzyk;

import com.seeyon.apps.ext.zxzyk.quartz.SyncDeptTask;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.timer.TimerHolder;

public class zxzykPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
//        SyncDeptTask deptTask=new SyncDeptTask();
//        TimerHolder.newTimer(deptTask,50000);
        System.out.println("启动xzyk插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁xzyk插件");
    }
}
