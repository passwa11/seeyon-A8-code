package com.seeyon.apps.ext.zMember.controller;

import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 周刘成   2019/6/20
 */
public class zMemberController extends BaseController {

    /***
     * 周刘成
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listAll(HttpServletRequest request, HttpServletResponse response) throws Exception{
        ModelAndView result = new ModelAndView("apps/organization/member/allMember");
        return result;
    }
}
