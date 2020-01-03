package com.seeyon.apps.ext.syncPost.dao;

import com.seeyon.apps.ext.syncPost.po.SyncOrgPost;

import java.util.List;

public interface syncPostDao {

    List<SyncOrgPost> queryNotExitPost();

    List<SyncOrgPost> queryChangePost();

    //创建岗位
    void insertPost(List<SyncOrgPost> list);

    void updatePost(List<SyncOrgPost> list);

}
