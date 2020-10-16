package com.seeyon.apps.ext.sureLogin.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.seeyon.apps.ext.sureLogin.manager.sureLoginManager;
import com.seeyon.apps.ext.sureLogin.manager.sureLoginManagerImpl;
import com.seeyon.apps.ext.sureLogin.po.SureLogin;
import com.seeyon.apps.ext.sureLogin.util.Md5;
import com.seeyon.apps.m3.api.M3Api;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.jfree.data.xy.TableXYDataset;
import org.springframework.web.servlet.ModelAndView;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class sureLoginController extends BaseController {

    private sureLoginManager manager = new sureLoginManagerImpl();

    public ModelAndView check(HttpServletRequest request, HttpServletResponse response) {
        String answer = request.getParameter("answer");
        User user = AppContext.getCurrentUser();
        Long userid = user.getId();
        SureLogin sureLogin = manager.selectSureLoginByid(userid);
        Map map = new HashMap();
        if (sureLogin != null) {
            String n = sureLogin.getAnswer1();
            if (n.equals(Md5.encodeStr(answer))) {
                String pwd = Md5.decodeStr(sureLogin.getAnswer3());
                map.put("code",1);
                map.put("pwd", pwd);
            }
        }else{
            map.put("code",-1);
            map.put("pwd", "");
        }
        String info = JSON.toJSONString(map);
        render(response, info);
        return null;
    }

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long userid = user.getId();
        SureLogin sureLogin = manager.selectSureLoginByid(userid);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/sureLogin/index");
        if (sureLogin == null) {
            modelAndView.addObject("first", true);
        } else {
            modelAndView.addObject("first", false);
        }
        return modelAndView;
    }

    public ModelAndView toLogin(HttpServletRequest request, HttpServletResponse response) {
        String pwd = request.getParameter("pwd");
        String md5 = Md5.encryptToMD5(pwd);
        User user = AppContext.getCurrentUser();
        Long userid = user.getId();
        SureLogin sureLogin = null;
        Map map = new HashMap();
        try {
            sureLogin = manager.selectSureLoginByid(userid);

            if (sureLogin != null) {
                if (sureLogin.getPassword().equals(md5)) {
                    map.put("code", 1);
                } else {
                    map.put("code", -1);
                }
            } else {
                map.put("code", -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String s = JSON.toJSONString(map);
        render(response, s);
        return null;
    }

    public ModelAndView doSave(HttpServletRequest request, HttpServletResponse response) {
        String pwd = request.getParameter("pwd");
        String answer = request.getParameter("answer");
        User user = AppContext.getCurrentUser();
        SureLogin sureLogin = new SureLogin();
        sureLogin.setId(user.getId());
        sureLogin.setLoginName(user.getLoginName());
        sureLogin.setPassword(Md5.encryptToMD5(pwd));
        //存入这个字段，用于找回密码
        sureLogin.setAnswer3(Md5.encodeStr(pwd));
        sureLogin.setAnswer1(Md5.encodeStr(answer));
        Map map = new HashMap();

        try {
            manager.insertSureLogin(sureLogin);
            map.put("code", 1);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", -1);
        }

        String info = JSON.toJSONString(map);
        render(response, info);
        return null;

    }

    public void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public ModelAndView sureLoginPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        String url=request.getParameter("url");
//        return new ModelAndView("apps/ext/sureLogin/sureLogin");
//    }
}
