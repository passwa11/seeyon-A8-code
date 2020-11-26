package com.seeyon.apps.ext.kypending.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;


public class kypendingController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        KyPendingManager kyPendingManager = new KyPendingManager();
//        kyPendingManager.getPending(user.getId());
//        kyPendingManager.eachMemberToSendData();
        return null;
    }
}