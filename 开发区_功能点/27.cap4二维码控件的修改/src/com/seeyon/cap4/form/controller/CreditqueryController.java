package com.seeyon.cap4.form.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreditqueryController extends BaseController {

    /**
     * 获取企业征信的页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView getCreditqueryPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("customField/creditquery");
        return mav;
    }

}
