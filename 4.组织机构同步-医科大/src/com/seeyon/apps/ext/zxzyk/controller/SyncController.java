package com.seeyon.apps.ext.zxzyk.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SyncController extends BaseController {

    public ModelAndView syncPage(HttpServletRequest request, HttpServletResponse response) throws Exception{

        return new ModelAndView("apps/ext/zxzyk/index");

    }
}
