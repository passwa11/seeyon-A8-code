package com.ncbi.medhub.http;

import org.apache.http.conn.HttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdleConnectoinEvictor extends Thread {
    @Autowired
    private HttpClientConnectionManager connectionManager;

    //并发编程
    private volatile boolean shutdown;

    public IdleConnectoinEvictor() {
        super();
        super.start();
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000);
                    // 关闭失效的连接
                    connectionManager.closeExpiredConnections();
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    //关闭清理无效连接的线程
    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}
