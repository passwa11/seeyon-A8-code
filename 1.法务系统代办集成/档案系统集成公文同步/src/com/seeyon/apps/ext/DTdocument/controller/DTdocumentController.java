package com.seeyon.apps.ext.DTdocument.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.DTdocument.manager.DTdocumentManager;
import com.seeyon.apps.ext.DTdocument.manager.DTdocumentManagerImpl;
import com.seeyon.apps.ext.DTdocument.manager.SyncOrgData;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.apps.ext.DTdocument.util.RenderUtil;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

public class DTdocumentController extends BaseController {

    private DTdocumentManager manager = new DTdocumentManagerImpl();

    public ModelAndView syncdata(HttpServletRequest request, HttpServletResponse response) {
        SyncOrgData.getInstance().getSummary();
//        SyncOrgData.getInstance().syncOrgUnit();
//        SyncOrgData.getInstance().syncOrgMember();
        /**
         * 同步公文
         */
        SyncOrgData.getInstance().syncSummary();
//
//        SyncOrgData.getInstance().copyEdoc();
//        SyncOrgData.getInstance().copyAttachment();

        return null;
    }


    /**
     * 跳转到账户设置页面
     */
    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        Long currentUserID = CurrentUser.get().getId();
        UserPas userPas = manager.selectDocAccount(Long.toString(currentUserID));
        request.setAttribute("userPas", userPas);
        modelAndView.setViewName("apps/ext/DTdocument/documentSetAccount");
        return modelAndView;
    }

    public ModelAndView setResult(HttpServletRequest req, HttpServletResponse resp, UserPas userPas) throws Exception {
        Long currentUserID = CurrentUser.get().getId();
        userPas.setId(currentUserID + "");
        Map<String, Object> map = new HashMap<>();
        try {
            manager.setDocAccount(userPas);
            req.setAttribute("law", "ok");
            map.put("code", 0);
            map.put("msg", "success");
        } catch (Exception e) {
            map.put("code", -1);
            map.put("msg", "error");
            e.printStackTrace();
        }

        JSONObject json = new JSONObject(map);
        RenderUtil.render(resp, json.toJSONString());
        return null;

    }

    //单点登录档案
    public ModelAndView loginDocument(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long currentUserId = CurrentUser.get().getId();
        UserPas userPas = manager.selectDocAccount(Long.toString(currentUserId));
        if (null != userPas) {
            request.setAttribute("userID", userPas.getRecord_user());
            request.setAttribute("password", userPas.getRecord_pas());
            request.setAttribute("ouserID", Long.toString(currentUserId));
            request.setAttribute("documentAuthority", "0");
            manager.updateDocState("RECORD_STATE", currentUserId + "");//修改登录状态
        } else {
            request.setAttribute("documentAuthority", "1");//未配置档案系统用户名、密码
        }
        return new ModelAndView("apps/ext/Portal190724/documentlogin");
    }
}
