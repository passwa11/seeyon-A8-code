package com.seeyon.apps.ext.fileUploadConfig.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.seeyon.apps.ext.fileUploadConfig.manager.fileUploadConfigManager;
import com.seeyon.apps.ext.fileUploadConfig.manager.fileUploadConfigManagerImpl;
import com.seeyon.apps.ext.fileUploadConfig.po.MiddleTemp;
import com.seeyon.apps.ext.fileUploadConfig.po.Temp;
import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUploadMember;
import com.seeyon.apps.ext.fileUploadConfig.po.ZorgMember;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class fileUploadConfigController extends BaseController {

    private fileUploadConfigManager manager = new fileUploadConfigManagerImpl();

    public ModelAndView getAllUploadMem(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("’‚ «≤‚ ‘£°°£°£°£°£°£°£°£");
    	List<ZOrgUploadMember> list = null;
        Map<String, Object> map = new HashMap<>();
        List<MiddleTemp> tempList = new ArrayList<>();
        MiddleTemp temp = null;
        try {
            list = manager.selectAllUploadMem();
            for (ZOrgUploadMember member : list) {
                temp = new MiddleTemp();
                temp.setUserid(Long.toString(member.getUserid()));
                temp.setDeptid(member.getDeptid());
                temp.setLoginname(member.getLoginname());
                temp.setStatus(member.getStatus());
                tempList.add(temp);
            }
            map.put("list", tempList);
            map.put("code", 0);
        } catch (Exception e) {
            map.put("code", -1);
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView listAll(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        String accountId = Long.toString(user.getAccountId());
        request.setAttribute("accountId", accountId);
        List<Map<String, Object>> list = manager.getUnitByAccountId(Long.parseLong(accountId));
        ModelAndView result = new ModelAndView("apps/ext/fileUploadConfig/configMember");
        request.setAttribute("list", JSON.toJSONString(list));
        return result;
    }

    public ModelAndView getMemberByDepartmentId(HttpServletRequest request, HttpServletResponse response) {
        try {
            String departmentId = request.getParameter("departmentId");
            if (null == departmentId || departmentId.equals("")) {
                departmentId = "";
            }
            Map map = new HashMap<>();
            map.put("departmentId", departmentId);
            map.put("name", request.getParameter("name"));
            List<ZorgMember> list = manager.showPeople(map);
            Map<String, Object> map2 = new HashMap<>();
            map2.put("code", 0);
            map2.put("message", "");
            map2.put("count", list.size());
            map2.put("data", list);
            JSONObject json = new JSONObject(map2);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ModelAndView insertUploadMember(HttpServletRequest request, HttpServletResponse response) {
        String data = request.getParameter("data");
        ArrayList<Temp> temps = JSON.parseObject(data, new TypeReference<ArrayList<Temp>>() {
        });
        List<ZOrgUploadMember> list = new ArrayList<>();
        for (Temp temp : temps) {
            ZOrgUploadMember member = new ZOrgUploadMember();
            member.setUserid(Long.parseLong(temp.getValue()));
            member.setDeptid(temp.getDept());
            member.setLoginname(temp.getText());
            list.add(member);
        }
        Map<String, Object> map2 = new HashMap<>();
        try {
            manager.insertUploadMember(list);
            map2.put("code", 0);
            map2.put("message", "success");
        } catch (Exception e) {
            map2.put("code", -1);
            map2.put("message", "error");
        }
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

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/fileUploadConfig/index");
    }
}
