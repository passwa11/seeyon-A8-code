package com.seeyon.apps.ext.Portal190724.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.manager.SyncData;
import com.seeyon.apps.ext.Portal190724.manager.SyncOrgData;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.apps.ext.Portal190724.util.RenderUtil;
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
public class DocumentSetUserController extends BaseController {

    private Portal190724Manager manager = new Portal190724ManagerImpl();

    public ModelAndView syncdata(HttpServletRequest request, HttpServletResponse response){
//        SyncOrgData.getInstance().syncOrgUnit();
//        SyncOrgData.getInstance().syncOrgMember();
        /**
         * 同步公文
         */
//        SyncOrgData.getInstance().syncSummary();
        SyncData syncData=new SyncData();
        syncData.syncSummary();

        return null;
    }
    /**
     * 跳转到账户设置页面
     */
    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        Long currentUserID = CurrentUser.get().getId();
        UserPas userPas  = manager.selectDocAccount(Long.toString(currentUserID));
        request.setAttribute("userPas", userPas);
        modelAndView.setViewName("apps/ext/Portal190724/documentSetAccount");
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
        }catch (Exception e){
            map.put("code", -1);
            map.put("msg", "error");
            e.printStackTrace();
        }

        JSONObject json = new JSONObject(map);
        RenderUtil.render(resp, json.toJSONString());
        return null;

    }
}
