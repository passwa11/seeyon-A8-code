package com.seeyon.apps.ext.zMember.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.zMember.manager.impl.zMemberManagerImpl;
import com.seeyon.apps.ext.zMember.manager.zMemberManager;
import com.seeyon.ctp.common.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 周刘成   2019/6/20
 */
public class zMemberController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(zMemberController.class);
    private zMemberManager zMemberManager = new zMemberManagerImpl();

    /***
     * 周刘成
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listAll(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView result = new ModelAndView("apps/organization/member/allMember");
        return result;
    }

    public ModelAndView getPeopleOfUnitCount(HttpServletRequest request, HttpServletResponse response) {
        try {
            int count = zMemberManager.selectUnitPeopleCount();
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            map.put("total", count);
            map.put("data", count);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            logger.info("获取集团总人数出错了："+e.getMessage());
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
}
