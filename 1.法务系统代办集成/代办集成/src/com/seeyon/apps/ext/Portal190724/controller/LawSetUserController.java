package com.seeyon.apps.ext.Portal190724.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 周刘成   2019/7/26
 */
public class LawSetUserController extends BaseController {

    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/Portal190724/setAccount");
        return modelAndView;
    }
}
