package com.controller;

import com.alibaba.fastjson.JSONObject;
import com.pojo.Member;
import com.pojo.Pocket;
import com.util.PocketUtil;
import com.util.StringHandlerUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 周刘成   2019/7/18
 */
public class ProxyServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("进来了");
        request.setCharacterEncoding("UTF-8");
        String code = request.getParameter("code");
        String did = request.getParameter("did");
        String appid = request.getParameter("appid");
        String token = PocketUtil.getToken();
        //获取用户简略信息
//        https://api.kd77.cn/cgi-bin/roster/user/get?access_token=1234adfb64&code=23832afd8&detail=0
        //获取用户详细信息
        String url = "https://211.103.127.211:6802/cgi-bin/roster/user/get?access_token=" + token + "&code=" + code + "&detail=1";
        String resultStr = PocketUtil.getJson(url);
        Pocket pocket = JSONObject.parseObject(resultStr, Pocket.class);
        Member member = pocket.getUser();
        String account = member.getAccount();
        String ticket = StringHandlerUtil.encode(account);
        response.setContentType("text/html;charset=UTF-8");
        String ssoUrl = "http://127.0.0.1:8888/seeyon/login/sso?ticket=" + ticket + "&from=sample";
        response.sendRedirect(ssoUrl);
    }
}
