package com.seeyon.apps.ext.gcxySso.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public class gcxySsoController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/gcxySso/index");
    }
}