package com.seeyon.apps.ext.Portal190724.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.apps.ext.Portal190724.util.GetTokenTool;
import com.seeyon.apps.ext.Portal190724.util.JsonResolveTools;
import com.seeyon.apps.ext.Portal190724.util.ReadConfigTools;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portal190724Controller extends BaseController {

    private Portal190724Manager portal190724Manager = new Portal190724ManagerImpl();

    private JsonResolveTools jsonResolveTools = new JsonResolveTools();

    /**
     * 向页面返回url请求参数
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView sendTokenToUrl(HttpServletRequest request, HttpServletResponse response) {
        try {
            String appKey = ReadConfigTools.getInstance().getString("portlet.appKey");
            String token = GetTokenTool.getInstance().getToken();
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("appKey", appKey);
            dataMap.put("token", token);

            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("data", dataMap);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 代办列表
     */
    public ModelAndView todoList(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        String type = request.getParameter("type");
        Long currentUserId = CurrentUser.get().getId();
        Map<String, Object> accountMap = portal190724Manager.select(String.valueOf(CurrentUser.get().getId()));
        if (accountMap.size() != 0) {
            String username = accountMap.keySet().iterator().next();
            String password = (String) accountMap.get(username);
            boolean b = jsonResolveTools.savelaws(currentUserId, username, password);
            StringBuffer sb = new StringBuffer();
            List<Contract> contracts = null;
            GetTokenTool tokenTool = new GetTokenTool();
            Map<String, Object> getMap = tokenTool.checkToken();
            for (Map.Entry<String, Object> entry : getMap.entrySet()) {
                if (!entry.getKey().equals("Timespan")) {
                    sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            if (type.equals("limit")) {
                mav.setViewName("apps/ext/Portal190724/index");
                contracts = portal190724Manager.getLimitLaw(currentUserId);
            } else {
                mav.setViewName("apps/ext/Portal190724/list_more");
                contracts = portal190724Manager.getAllLaw(currentUserId);
            }

            mav.addObject("contracts", contracts);
            mav.addObject("detailParam", sb.toString());
        } else {
            if (type.equals("limit")) {
                mav.setViewName("apps/ext/Portal190724/index");
            } else {
                mav.setViewName("apps/ext/Portal190724/list_more");
            }
            mav.addObject("contracts", "");
            mav.addObject("detailParam", "");
        }

        return mav;
    }


    /**
     * 跳转到设置人员账户界面
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/Portal190724/setAccount");
        return modelAndView;
    }


    /**
     * 给前台渲染json数据
     *
     * @param response
     * @param text
     */
    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
