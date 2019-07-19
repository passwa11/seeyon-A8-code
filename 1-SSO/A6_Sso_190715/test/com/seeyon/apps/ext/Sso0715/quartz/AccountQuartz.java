package com.seeyon.apps.ext.Sso0715.quartz;

import com.seeyon.ctp.common.quartz.QuartzJob;

import java.util.Map;

/**
 * 周刘成   2019/7/16
 */
public class AccountQuartz implements QuartzJob {

    @Override
    public void execute(Map<String, String> map) {
        Long id = Long.parseLong(map.get("id"));
        System.out.println("定时任务测试id:" + id);
    }
}
