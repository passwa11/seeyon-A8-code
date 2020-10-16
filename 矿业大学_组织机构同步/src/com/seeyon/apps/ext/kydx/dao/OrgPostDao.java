package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.OrgPost;

import java.util.List;

public interface OrgPostDao {

    List<OrgPost> queryInsertPost();

    void insertPost(List<OrgPost> list);

    List<OrgPost> queryUpdatePost();

    void updatePost(List<OrgPost> list);

    List<OrgPost> queryDeletePost();

    void deletePost(List<OrgPost> list);


}
