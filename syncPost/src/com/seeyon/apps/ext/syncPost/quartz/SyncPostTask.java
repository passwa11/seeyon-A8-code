package com.seeyon.apps.ext.syncPost.quartz;

import com.seeyon.apps.ext.syncPost.manager.syncPostManager;
import com.seeyon.apps.ext.syncPost.manager.syncPostManagerImpl;
import com.seeyon.apps.ext.syncPost.po.SyncOrgPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 周刘成   2019-12-24
 */
public class SyncPostTask extends Thread {
    private Logger logger = LoggerFactory.getLogger(SyncPostTask.class);

    private syncPostManager manager = new syncPostManagerImpl();

    @Override
    public void run() {
        //创建
        List<SyncOrgPost> list = manager.queryNotExitPost();
        manager.insertPost(list);
        //update
        List<SyncOrgPost> updateList = manager.queryChangePost();
        manager.updatePost(updateList);
    }

    public syncPostManager getManager() {
        return manager;
    }
}
