package com.seeyon.apps.ext.pulldata.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public class pulldataController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/pulldata/index");
    }
}