package com.seeyon.apps.ext.syncPost;

import com.seeyon.apps.ext.syncPost.quartz.SyncPostTask;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.timer.TimerHolder;

import java.util.Calendar;
import java.util.Date;

public class syncPostPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        final long PERIOD_DAY = 60 * 60 * 1000;
        SyncPostTask deptTask = new SyncPostTask();

        //一小时执行一次
//        TimerHolder.newTimer(deptTask, 20 * 60 * 1000, PERIOD_DAY);
        //test
//        TimerHolder.newTimer(deptTask, 1 * 60 * 1000, 2*60*1000);

        System.out.println("加载syncPost插件");
    }


    @Override
    public void destroy() {
        System.out.println("销毁syncPost插件");
    }
}
