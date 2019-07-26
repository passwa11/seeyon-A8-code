package com.seeyon.apps.ext.Portal190724;

import com.seeyon.apps.ext.quartz.SampleTask;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.timer.TimerHolder;

public class Portal190724PluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
//        SampleTask sampleTask=new SampleTask();
//        TimerHolder.newTimer(sampleTask,5000);
        System.out.println("正在启动Portal190724插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁Portal190724插件");
    }
}
