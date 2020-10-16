package com.seeyon.apps.testBPM.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.controller.BaseController;

public class CAP4TriggerController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("testBPM/trigger/planinfo");
        return mav;
    }
}
