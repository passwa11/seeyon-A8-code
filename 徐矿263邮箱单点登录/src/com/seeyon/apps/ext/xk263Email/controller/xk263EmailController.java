package com.seeyon.apps.ext.xk263Email.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.xk263Email.util.ZCommonUtil;
import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class xk263EmailController extends BaseController {


    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginUrl = ZCommonUtil.get263LoginUrl();
        String count = ZCommonUtil.getUnreadCount();

        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("url", loginUrl);
        map.put("count", count);
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }


    /**
     * 单点到263邮箱
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView login263Email(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginUrl = ZCommonUtil.get263LoginUrl();
        String count = ZCommonUtil.getUnreadCount();

        request.setAttribute("loginUrl", loginUrl);
        request.setAttribute("count", count);
        return new ModelAndView("apps/ext/xk263Email/login");
    }

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
