package com.seeyon.apps.ext.accessSeting.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManager;
import com.seeyon.apps.ext.accessSeting.manager.AccessSetingManagerImpl;
import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.ZorgMember;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.PropertiesUtil;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessSetingControlller extends BaseController {

    private AccessSetingManager manager = new AccessSetingManagerImpl();


    /**
     * 根据部门id获取部门人员列表
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getMemberByDepartmentId(HttpServletRequest request, HttpServletResponse response) {
        try {
            String departmentId = request.getParameter("departmentId");
            if (null == departmentId || departmentId.equals("")) {
                departmentId = "";
            }
            Map map = new HashMap<>();
            map.put("departmentId", departmentId);
            map.put("name", request.getParameter("name"));
            List<ZorgMember> list = null;
            list = manager.showPeople(map);
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

    /**
     * 跳转到查询设置页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long accountId = user.getAccountId();
        List<Map<String, Object>> list = manager.queryAllUnit(accountId);
        request.setAttribute("list", JSON.toJSONString(list));
        return new ModelAndView("apps/ext/accessSeting/index");
    }

    /**
     * 弹出设置页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView setting(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/accessSeting/setConfig");
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
        Map<String, Object> map2 = new HashMap<>();
        try {
            String[] memberIds = (range.getIds()).split(",");
            Map<String, Object> params = null;
            for (int i = 0; i < memberIds.length; i++) {
                if (!"".equals(memberIds[i])) {
                    params = new HashMap<>();
                    params.put("memberId", Long.parseLong(memberIds[i]));
                    List<DepartmentViewTimeRange> list = manager.getDepartmentViewTimeRange(params);
                    if (list.size() > 0) {
                        DepartmentViewTimeRange upRange = list.get(0);
                        upRange.setDayNum(range.getDayNum());
                        manager.updateDepartmentViewTimeRange(upRange);
                    } else {
                        range.setId(System.currentTimeMillis());
                        range.setMemberId(Long.parseLong(memberIds[i]));
                        manager.saveDepartmentViewTimeRange(range);
                    }
                }
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
