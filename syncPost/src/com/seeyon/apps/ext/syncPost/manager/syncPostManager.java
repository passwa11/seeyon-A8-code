package com.seeyon.apps.ext.syncPost.manager;

import com.seeyon.apps.ext.syncPost.po.SyncOrgPost;

import java.util.List;

public interface syncPostManager {

    List<SyncOrgPost> queryNotExitPost();

    List<SyncOrgPost> queryChangePost();

    void insertPost(List<SyncOrgPost> list);

    void updatePost(List<SyncOrgPost> list);

}
