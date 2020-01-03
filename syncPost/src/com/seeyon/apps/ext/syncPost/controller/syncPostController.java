package com.seeyon.apps.ext.syncPost.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.syncPost.manager.syncPostManager;
import com.seeyon.apps.ext.syncPost.manager.syncPostManagerImpl;
import com.seeyon.apps.ext.syncPost.po.SyncOrgPost;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

public class syncPostController extends BaseController {

    private syncPostManager syncPostManager = new syncPostManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //创建
        List<SyncOrgPost> list = syncPostManager.queryNotExitPost();
        syncPostManager.insertPost(list);
        //update
        List<SyncOrgPost> updateList = syncPostManager.queryChangePost();
        syncPostManager.updatePost(updateList);
        return null;
    }
}
