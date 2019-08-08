package com.seeyon.apps.ext.zxzyk.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.zxzyk.dao.OrgCommon;
import com.seeyon.apps.ext.zxzyk.manager.*;
import com.seeyon.ctp.common.controller.BaseController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class xzykController extends BaseController {

    private OrgDeptManager orgDeptManager = new OrgDeptManagerImpl();

    private OrgLevelManager orgLevelManager = new OrgLevelManagerImpl();

    private OrgMemberManager orgMemberManager = new OrgMemberManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/xzyk/index");
    }

    public ModelAndView syncDept(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OrgCommon common = new OrgCommon();

        Map<String, Object> map = new HashMap<>();
        try {
            //部门
            orgDeptManager.updateOrgDept();
            orgDeptManager.insertOtherDept();


//            //职级
            orgLevelManager.updateOrgLevel();
            orgLevelManager.insertOrgLevel();

//
//            //人员
            orgMemberManager.updateOrgMember();
            orgMemberManager.insertOrgMember();


            orgDeptManager.deleteOrgDept();
            orgMemberManager.deleteOrgMember();
            orgLevelManager.deleteNotExistLevel();
            map.put("code", 0);
            map.put("msg", "success");
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("msg", "error");
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
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
