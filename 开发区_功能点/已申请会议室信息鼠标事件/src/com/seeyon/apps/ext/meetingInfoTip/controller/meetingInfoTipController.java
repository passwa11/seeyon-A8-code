package com.seeyon.apps.ext.meetingInfoTip.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.util.JDBCAgent;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class meetingInfoTipController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        Connection connection = JDBCAgent.getRawConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select (select name from org_member o where o.id=m.perid ) username,(select name from org_unit u where u.id=m.departmentid) deptname,startdatetime,enddatetime,description,sqrdh from meeting_room_app m where id=?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            rs = ps.executeQuery();
            Map<String, String> m = new HashMap<>(16);
            while (rs.next()) {
                m.put("sqr", rs.getString("username"));
                m.put("deptname", rs.getString("deptname"));
                String s = rs.getString("startdatetime");
                String start = s.split(" ")[1].substring(0, s.lastIndexOf(":"));
                String e = rs.getString("enddatetime");
                String end = e.split(" ")[1].substring(0, e.lastIndexOf(":"));
                m.put("time", start + "-" + end);
                m.put("description", rs.getString("description"));
                m.put("sqrdh", rs.getString("sqrdh"));
            }
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("data", m);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
