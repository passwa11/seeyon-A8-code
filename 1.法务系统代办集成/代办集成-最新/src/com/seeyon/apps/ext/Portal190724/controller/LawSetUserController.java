package com.seeyon.apps.ext.Portal190724.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.apps.ext.Portal190724.util.RenderUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 周刘成   2019/7/26
 */
public class LawSetUserController extends BaseController {

    private Portal190724Manager manager = new Portal190724ManagerImpl();

    /**
     * 跳转到账户设置页面
     */
    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        Long currentUserID = CurrentUser.get().getId();
        Map<String, Object> map = manager.select(Long.toString(currentUserID));
        UserPas userPas = new UserPas();
        if (null != map && map.size() > 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                userPas.setLaw_user(entry.getKey());
                userPas.setLaw_pas((String) entry.getValue());
            }
        }
        request.setAttribute("userPas", userPas);
        modelAndView.setViewName("apps/ext/Portal190724/setAccount");
        return modelAndView;
    }

    public ModelAndView setResult(HttpServletRequest req, HttpServletResponse resp, UserPas userPas) throws Exception {
        Long currentUserID = CurrentUser.get().getId();
        userPas.setId(currentUserID + "");
        int save = manager.setAddAccount(userPas);
        Map<String, Object> map = new HashMap<>();
        if (save == 0) {
            req.setAttribute("law", "ok");
            map.put("code", 0);
            map.put("msg", "success");
        } else {
            map.put("code", -1);
            map.put("msg", "error");
        }
        JSONObject json = new JSONObject(map);
        RenderUtil.render(resp, json.toJSONString());
        return null;

    }
}
