package com.seeyon.apps.ext.quartz;

/**
 * 周刘成   2019/7/26
 */
public class SampleTask implements Runnable {
    @Override
    public void run() {
        System.out.println("我说hello !,你说Hi!");
    }
}
