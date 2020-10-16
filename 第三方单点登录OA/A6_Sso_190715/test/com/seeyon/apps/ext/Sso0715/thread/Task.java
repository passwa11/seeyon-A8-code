package com.seeyon.apps.ext.Sso0715.thread;

/**
 * 周刘成   2019/7/16
 */
public class Task implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("hello!");
        }
    }
}
