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


    public ModelAndView getLinkid(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String linkId = ConfigInfo.getNeiLinkId();
        Map map = new HashMap();
        map.put("link", linkId);
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String linkId = request.getParameter("linkId");
        //访问oa的真实ip
        String ipAddress = request.getServerName();
        Long ipNum = transferIp(ipAddress);

        Long neiWangNum = transferIp(ConfigInfo.getIpAddress());
        Long yuanwaiNum = transferIp(ConfigInfo.getYuanwaiNet());
        Long gongwangNum = transferIp(ConfigInfo.getGongwangNet());

        Map map = new HashMap();
        String neilinkIds = ConfigInfo.getNeiLinkId();
        String yuanwailinkIds = ConfigInfo.getYuanLinkId();
        String gonglinkIds = ConfigInfo.getGongLinkId();
        String clicktype_ = "";
        if (neilinkIds.indexOf(linkId) != -1) {
            clicktype_ = "nei";
        }
        if (yuanwailinkIds.indexOf(linkId) != -1) {
            clicktype_ = "yuan";
        }

        if (gonglinkIds.indexOf(linkId) != -1) {
            clicktype_ = "gong";
        }

        String linkIds = "";
        if (ipNum.longValue() == neiWangNum.longValue()) {
            linkIds = neilinkIds;
            map = judge(linkIds, linkId, "nei", clicktype_);
        } else if (ipNum.longValue() == yuanwaiNum.longValue()) {
            linkIds = yuanwailinkIds;
            map = judge(linkIds, linkId, "yuan", clicktype_);
        } else if (ipNum.longValue() == gongwangNum.longValue()) {
            linkIds = gonglinkIds;
            map = judge(linkIds, linkId, "gong", clicktype_);
        } else {
            map.put("code", -1);
            map.put("flag", "");
            map.put("linkIds", "linkId is null");
            map.put("msg", "你访问的地址不合法！");
        }

        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public Map judge(String linkIds, String linkId, String type, String clicktype) {
        Map map = new HashMap();
        if (linkIds != null && !linkIds.equals("")) {
            map.put("linkIds", linkIds);
            if (type.equals(clicktype)) {
                map.put("code", 0);
                map.put("msg", "");
            } else {
                map.put("code", -1);
                if(clicktype.equals("nei")){
                    map.put("msg", "请在内网打开！");
                }
                if(clicktype.equals("yuan")){
                    map.put("msg", "请在院外打开！");
                }
                if(clicktype.equals("gong")){
                    map.put("msg", "请在公网打开！");
                }
            }
        } else {
            map.put("linkIds", "linkId is null");
            map.put("msg", "访问地址出错了！");
        }
        map.put("flag", type);
        return map;
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
