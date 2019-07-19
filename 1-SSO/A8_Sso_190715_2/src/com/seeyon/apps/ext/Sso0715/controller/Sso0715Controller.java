package com.seeyon.apps.ext.Sso0715.controller;

import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 周刘成   2019/7/16
 */
public class Sso0715Controller extends BaseController {

    @NeedlessCheckLogin
    public ModelAndView proxyForward(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("apps/hello/index");
        mav.addObject("name", "fangaowei");
        return mav;
    }
}
