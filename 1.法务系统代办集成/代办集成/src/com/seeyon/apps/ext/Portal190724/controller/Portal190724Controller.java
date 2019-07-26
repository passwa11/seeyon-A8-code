package com.seeyon.apps.ext.Portal190724.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.apps.ext.Portal190724.util.JsonResolveTools;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public class Portal190724Controller extends BaseController {

    private Portal190724Manager portal190724Manager = new Portal190724ManagerImpl();

    private JsonResolveTools jsonResolveTools = new JsonResolveTools();

    /**
     * 代办列表
     */
    public ModelAndView todoList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type=request.getParameter("type");
        Long currentUserId = CurrentUser.get().getId();
//        Map<String, Object> accountMap = portal190724Manager.select(String.valueOf(CurrentUser.get().getId()));
//        String username = accountMap.keySet().iterator().next();
//        String password = (String) accountMap.get(username);
//        boolean b = jsonResolveTools.savelaws(currentUserId, username, password);
        StringBuffer sb = new StringBuffer();
        List<Contract> contracts = null;
//        GetTokenTool tokenTool = new GetTokenTool();
//        Map<String, Object> getMap = tokenTool.checkToken();
//        for (Map.Entry<String, Object> entry : getMap.entrySet()) {
//            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
//        }
        ModelAndView mav = new ModelAndView();
//        if (b) {
        if (type.equals("limit")) {
            mav.setViewName("apps/ext/Portal190724/index");
            contracts = portal190724Manager.getLimitLaw(currentUserId);
        } else {
            mav.setViewName("apps/ext/Portal190724/list_more");
//            contracts = portal190724Manager.getAllLaw(currentUserId);
        }

//        }
        mav.addObject("contracts", contracts);
        mav.addObject("param", sb.toString());
        return mav;
    }


    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/Portal190724/setAccount");
        return modelAndView;
    }

}
