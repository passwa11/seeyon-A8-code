package com.seeyon.apps.ext.zxzyk;

import com.seeyon.apps.ext.zxzyk.quartz.SyncDeptTask;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.timer.TimerHolder;

import java.util.Calendar;
import java.util.Date;

public class zxzykPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        final long PERIOD_DAY = 24 * 60 * 60 * 1000;
        SyncDeptTask deptTask = new SyncDeptTask();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1); //凌晨1点
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        //第一次执行定时任务的时间
        Date date = calendar.getTime();
        if (date.before(new Date())) {
            date = this.addDay(date, 1);
        }
        TimerHolder.newTimer(deptTask, date, PERIOD_DAY);
        System.out.println("启动xzyk插件");
    }

    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    @Override
    public void destroy() {
        System.out.println("销毁xzyk插件");
    }
}
