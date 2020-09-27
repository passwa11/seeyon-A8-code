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
        String sql = "select ldname,(select name from org_member o where o.id=m.perid ) username,(select name from org_unit u where u.id=m.departmentid) deptname,startdatetime,enddatetime,description,sqrdh,hcyq from meeting_room_app m where id=?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, id);
            rs = ps.executeQuery();
            Map<String, String> m = new HashMap<>();
            while (rs.next()) {
                m.put("sqr", rs.getString("username"));
                m.put("deptname", rs.getString("deptname"));
                String s1 = rs.getString("startdatetime");
                String s = s1.split(" ")[1];
                String start = s.substring(0, s.lastIndexOf(":"));
                String e1 = rs.getString("enddatetime");
                String e = e1.split(" ")[1];
                String end = e.substring(0, e.lastIndexOf(":"));
                m.put("time", start + "-" + end);
                m.put("description", rs.getString("description"));
                m.put("sqrdh", rs.getString("sqrdh"));
                m.put("hcyq", rs.getString("hcyq"));
                m.put("ldname", rs.getString("ldname"));

            }
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("data", m);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != rs) {
                rs.close();
            }
            if (null != ps) {
                ps.close();
            }
            if (null != connection) {
                connection.close();
            }
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
