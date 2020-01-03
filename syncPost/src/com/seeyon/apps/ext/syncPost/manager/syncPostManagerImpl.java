package com.seeyon.apps.ext.syncPost.manager;

import com.seeyon.apps.ext.syncPost.dao.syncPostDaoImpl;
import com.seeyon.apps.ext.syncPost.po.SyncOrgPost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.syncPost.dao.syncPostDao;
import com.seeyon.ctp.common.AppContext;

import java.util.List;


public class syncPostManagerImpl implements syncPostManager {
    private static final Log log = LogFactory.getLog(syncPostManagerImpl.class);

    private syncPostDao postDao = new syncPostDaoImpl();

    @Override
    public void insertPost(List<SyncOrgPost> list) {
        postDao.insertPost(list);
    }

    @Override
    public List<SyncOrgPost> queryChangePost() {
        return postDao.queryChangePost();
    }

    @Override
    public List<SyncOrgPost> queryNotExitPost() {
        return postDao.queryNotExitPost();
    }

    @Override
    public void updatePost(List<SyncOrgPost> list) {
        postDao.updatePost(list);
    }
}
