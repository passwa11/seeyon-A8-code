package com.seeyon.apps.ext.accessSeting.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManagerImpl;
import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessSetingControlller extends BaseController {

    private AccessSetingManager manager = new AccessSetingManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/accessSeting/index");
    }

    public ModelAndView getUnitTree(HttpServletRequest request, HttpServletResponse response) {
        List<Map<String, Object>> list = manager.queryAllUnit();
        Map<String, Object> map2 = new HashMap<>();
        map2.put("count", list.size());
        map2.put("data", list);
        JSONObject json = new JSONObject(map2);
        render(response, json.toJSONString());
        return null;
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
