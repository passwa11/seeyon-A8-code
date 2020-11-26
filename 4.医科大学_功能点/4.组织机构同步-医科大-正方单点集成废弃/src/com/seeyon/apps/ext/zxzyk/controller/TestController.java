package com.seeyon.apps.ext.zxzyk.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestController extends BaseController {

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String ticket = request.getParameter("ticket");
        System.out.println();
        return super.index(request, response);
    }
}
