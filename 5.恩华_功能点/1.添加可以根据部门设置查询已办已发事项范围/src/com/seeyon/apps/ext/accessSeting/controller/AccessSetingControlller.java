package com.seeyon.apps.ext.accessSeting.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManagerImpl;
import com.seeyon.apps.ext.accessSeting.manager.LeaveSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.LeaveSetingManagerImpl;
import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.LeaveSeting;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.PropertiesUtil;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AccessSetingControlller extends BaseController {

    private AccessSetingManager manager = new AccessSetingManagerImpl();

    private LeaveSetingManager leaveSetingManager = new LeaveSetingManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long accountId = user.getAccountId();
        List<Map<String, Object>> list = manager.queryAllUnit(accountId);
        request.setAttribute("list", JSON.toJSONString(list));
        return new ModelAndView("apps/ext/accessSeting/index");
    }

    public ModelAndView leaveSeting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<LeaveSeting> all = leaveSetingManager.findAll();
        if (all.size() > 0) {
            LeaveSeting leaveSeting = all.get(0);
            request.setAttribute("leave", leaveSeting);
        } else {
//            request.setAttribute("leave", null);
        }
        return new ModelAndView("apps/ext/accessSeting/leave");
    }

    public ModelAndView saveLeaveSeting(HttpServletRequest request, HttpServletResponse response, LeaveSeting leaveSeting) {
        leaveSeting.setId(System.currentTimeMillis());

        List<LeaveSeting> all = leaveSetingManager.findAll();
        Map<String, Object> map = new HashMap<>();
        try {
            if (all.size() > 0) {
                LeaveSeting s = all.get(0);
                s.setIsEnable(leaveSeting.getIsEnable());
                leaveSetingManager.updateLeaveSeting(s);
            } else {
                leaveSetingManager.saveLeaveSeting(leaveSeting);
            }
            map.put("code", 0);
        } catch (Exception e) {
            map.put("code", -1);
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView getDepartmentRange(HttpServletRequest request, HttpServletResponse response, String departmentId) {
        String id = request.getParameter("departmentId");
        Map<String, Object> params = new HashMap<>();
        params.put("deptmentId", Long.parseLong(id));
        List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(params);
        Map<String, Object> map = new HashMap<>();
        if (null != list && list.size() > 0) {
            map.put("data", list.get(0));
        } else {
            map.put("data", null);
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView saveDepartmentViewTimeRange(HttpServletRequest request, HttpServletResponse response, DepartmentViewTimeRange range) {
        range.setId(System.currentTimeMillis());
        Map<String, Object> map2 = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("deptmentId", range.getDeptmentId());
            List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(params);
            if (list.size() > 0) {
                DepartmentViewTimeRange upRange = list.get(0);
                upRange.setDeptmentId(range.getDeptmentId());
                upRange.setDepartmentName(range.getDepartmentName());
                upRange.setStartTime(range.getStartTime());
                upRange.setEndTime(range.getEndTime());
                manager.updateDepartmentViewTimeRange(upRange);
            } else {
                manager.saveDepartmentViewTimeRange(range);
            }
            map2.put("code", 0);
        } catch (Exception e) {
            map2.put("code", -1);
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


}
