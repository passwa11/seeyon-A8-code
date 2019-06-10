package com.seeyon.apps.ext.selectPeople.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.selectPeople.manager.JtldEntityManager;
import com.seeyon.apps.ext.selectPeople.manager.JtldEntityManagerImpl;
import com.seeyon.apps.ext.selectPeople.po.Formson0174;
import com.seeyon.ctp.common.controller.BaseController;
import com.alibaba.fastjson.JSONArray;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class selectPeopleController extends BaseController {

    private JtldEntityManager jtldEntityManager = new JtldEntityManagerImpl();

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("apps/ext/Zselect/index");
    }

    //new 党政办
    public ModelAndView selectDeskWork(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            if (null == name || name.equals("")) {
                name = "";
            }
            List<Map> list = jtldEntityManager.selectDeskWork(name);
            List<Map<String, String>> revoler = new ArrayList<>();
            for (Map map : list) {
                Map<String, String> m = new HashMap<>();
                BigDecimal bigDecimal = (BigDecimal) map.get("id");
                m.put("id", bigDecimal.toString());

                m.put("field0001", (String) map.get("field0001"));
                m.put("field0002", (String) map.get("field0002"));
                revoler.add(m);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", revoler.size());
            map.put("data", revoler);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    //根据科室的id获取科室下的人员
    public ModelAndView selectPeopleByDeskWorkId(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            if (null == id || id.equals("")) {
                id = "";
            }
            List<Map> list = jtldEntityManager.selectPeopleByDeskWorkId(id);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }


    //集团领导
    public ModelAndView selectJtldEntity(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            if (null == name || name.equals("")) {
                name = "";
            }
            List<Map> list = jtldEntityManager.selectJtldEntity(name);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    //党政办
    public ModelAndView selectFormmain0148_policy(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            if (null == name || name.equals("")) {
                name = "";
            }
            List<Map> list = jtldEntityManager.selectFormmain0148(name);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    //机关单位
    public ModelAndView selectFormmain0106_organ(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            if (null == name || name.equals("")) {
                name = "";
            }
            List<Map> list = jtldEntityManager.selectFormmain0106(name);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    //基层单位
    public ModelAndView selectFormmain0087_baseUnits(HttpServletRequest request, HttpServletResponse response) {
        try {
            String name = request.getParameter("name");
            if (null == name || name.equals("")) {
                name = "";
            }
            List<Map> list = jtldEntityManager.selectFormmain0087(name);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", list.size());
            map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
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


    public ModelAndView insertData(HttpServletRequest request, HttpServletResponse response) {
        String info = request.getParameter("info");
        JSONArray jsonArray = JSON.parseArray(info);
        List<Formson0174> list = new ArrayList<>();
        for (Object object : jsonArray) {
            Formson0174 f0174 = new Formson0174();
            JSONObject jsonObject = (JSONObject) object;
            f0174.setField0044(jsonObject.getString("text"));
            f0174.setField0045(jsonObject.getString("value"));
            f0174.setField0046(jsonObject.getString("dept"));
            list.add(f0174);
        }
        jtldEntityManager.insertFormson0174(list);
        return null;
    }


}