package com.seeyon.apps.ext.loginCheck.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class loginCheckController extends BaseController {


    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String ipAddress = request.getServerName();
        Long ipNum = transferIp(ipAddress);

//        String oaIp = "172.18.98.96";
        String oaIp = ConfigInfo.getIpAddress();
        Long oaipNum = transferIp(oaIp);

        Map map = new HashMap();
        if (ipNum.longValue() == oaipNum.longValue()) {
            map.put("code", 0);
        } else {
            map.put("code", -1);
        }

        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public Long transferIp(String ipAddress) {
        String[] ips = ipAddress.split("\\.");
        long a = Integer.parseInt(ips[0]);
        long b = Integer.parseInt(ips[1]);
        long c = Integer.parseInt(ips[2]);
        long d = Integer.parseInt(ips[3]);

        long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
        return ipNum;
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
