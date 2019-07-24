package com.seeyon.apps.ext.Portal190724.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.pojo.Contract;
import com.seeyon.apps.ext.Portal190724.util.JsonResolveTools;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

public class Portal190724Controller extends BaseController {

    private Portal190724Manager portal190724Manager = new Portal190724ManagerImpl();

    private JsonResolveTools jsonResolveTools = new JsonResolveTools();

    /**
     * 代办列表
     */
    public ModelAndView todoList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long currentUserId = CurrentUser.get().getId();
        Map<String, Object> accountMap = portal190724Manager.select(String.valueOf(CurrentUser.get().getId()));
        String username = accountMap.keySet().iterator().next();
        String password = (String) accountMap.get(username);
        boolean b = jsonResolveTools.savelaws(currentUserId, username, password);
        List<Contract> contracts = null;
        if (b) {
            contracts = portal190724Manager.getLimitLaw(currentUserId);
        }
        ModelAndView mav = new ModelAndView("apps/ext/Portal190724/index");
        mav.addObject("contracts", contracts);
        return mav;
    }
}
