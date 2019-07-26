package com.seeyon.apps.ext.Portal190724.controller;

import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 周刘成   2019/7/26
 */
public class LawSetUserController extends BaseController {

    /**
     * 跳转到账户设置页面
     */
    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/Portal190724/setAccount");
        return modelAndView;
    }

    public ModelAndView setResult(HttpServletRequest req, HttpServletResponse resp, UserPas userPas) throws Exception {
        Long currentUserID = CurrentUser.get().getId();
        System.out.println(userPas);
        userPas.setId(currentUserID + "");
//        int save = lawLoginManager.add(userPas);
//        System.out.println("save=" + save);
//        if (save == 0) {
//            req.setAttribute("law", "ok");
//        }
        return new ModelAndView("plugin/lawcation/setresult");

    }
}
