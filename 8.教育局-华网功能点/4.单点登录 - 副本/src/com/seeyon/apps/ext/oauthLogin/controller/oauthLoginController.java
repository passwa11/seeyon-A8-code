package com.seeyon.apps.ext.oauthLogin.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.util.XXEUtil;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import org.springframework.web.servlet.ModelAndView;

public class oauthLoginController extends BaseController {

    @NeedlessCheckLogin
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        XXEUtil.prevent();
        return new ModelAndView("apps/ext/oauthLogin/index");
    }


}