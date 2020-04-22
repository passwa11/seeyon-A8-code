package com.seeyon.apps.ext.welinkMenu.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManager;
import com.seeyon.apps.ext.welinkMenu.manager.welinkMenuManagerImpl;
import com.seeyon.apps.ext.welinkMenu.po.WeLinkUsers;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class welinkMenuController extends BaseController {

    private welinkMenuManager welinkMenuManager = new welinkMenuManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long currentUserID = CurrentUser.get().getId();
        WeLinkUsers weLinkUsers = welinkMenuManager.selectByCurrentUserId(currentUserID+"");
        request.setAttribute("user",weLinkUsers);
        return new ModelAndView("apps/ext/welinkMenu/setAccount");
    }

    public ModelAndView setResult(HttpServletRequest req, HttpServletResponse resp, WeLinkUsers users) throws Exception {
        Long currentUserID = CurrentUser.get().getId();
        User user=CurrentUser.get();
        users.setOaUserId(currentUserID + "");
        users.setOaUserName(CurrentUser.get().getName());
        WeLinkUsers weLinkUsers = welinkMenuManager.selectByCurrentUserId(users.getOaUserId());
        int flag = -1;
        if (null != weLinkUsers) {
            flag = welinkMenuManager.updateWebLinkUsers(users);
        } else {
            flag = welinkMenuManager.insertWebLinkUsers(users);
        }
        Map<String, Object> map = new HashMap<>();
        if (flag == 1) {
            req.setAttribute("wluser", "ok");
            map.put("code", 0);
            map.put("msg", "success");
        } else {
            map.put("code", -1);
            map.put("msg", "error");
        }
        JSONObject json = new JSONObject(map);
        render(resp, json.toJSONString());
        return null;
    }

    public void render(HttpServletResponse response, String text) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setContentLength(text.getBytes("utf-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}