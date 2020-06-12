package com.seeyon.apps.ext.kydx.manager;

import com.seeyon.apps.ext.kydx.dao.OrgPostDao;
import com.seeyon.apps.ext.kydx.dao.OrgPostDaoImpl;
import com.seeyon.apps.ext.kydx.po.OrgPost;

import java.util.List;

public class OrgPostManagerImpl implements OrgPostManager {
    private OrgPostDao postDao = new OrgPostDaoImpl();

    @Override
    public void insertPost() {
        List<OrgPost> list = postDao.queryInsertPost();
        postDao.insertPost(list);
    }

    @Override
    public void updatePost() {
        List<OrgPost> list = postDao.queryUpdatePost();
        postDao.updatePost(list);
    }

    @Override
    public void deletePost() {
        List<OrgPost> list = postDao.queryDeletePost();
        postDao.deletePost(list);
    }
}
