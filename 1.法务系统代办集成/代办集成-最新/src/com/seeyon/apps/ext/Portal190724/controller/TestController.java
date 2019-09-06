package com.seeyon.apps.ext.Portal190724.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 周刘成   2019-8-10
 */
public class TestController extends BaseController {

    public ModelAndView test(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/Portal190724/test");
        return modelAndView;
    }
}
