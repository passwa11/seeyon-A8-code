package com.seeyon.apps.ext.Sso0715.thread;

/**
 * 周刘成   2019/7/16
 */
public class Test {
    public static void main(String[] args) {
        Thread thread = new Thread(new Task());
        thread.start();
    }
}
