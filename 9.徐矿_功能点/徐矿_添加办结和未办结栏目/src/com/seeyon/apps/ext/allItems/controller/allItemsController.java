package com.seeyon.apps.ext.allItems.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.allItems.manager.allItemsManager;
import com.seeyon.apps.ext.allItems.manager.allItemsManagerImpl;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.util.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class allItemsController extends BaseController {

    private static Log log = LogFactory.getLog(allItemsController.class);

    private allItemsManager allItemsManager = new allItemsManagerImpl();

    /**
     * 跳转到协同未办结更多页面
     */
    public ModelAndView toMoreCooprationNoBj(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/coop_list_more_no_banjie");
        String templetIds = String.valueOf(request.getParameter("templetIds"));
        if (Strings.isNotBlank(templetIds) && !"null".equals(templetIds)) {
            modelAndView.addObject("templetIds", templetIds);
        } else {
            modelAndView.addObject("templetIds", "");
        }
        return modelAndView;
    }
    /**
     * 协同所有未办结栏目
     * @param request
     * @param response
     * @return
     */
    public ModelAndView cooprationUnfinishDataSection(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/coopratiion_no_banjie");
        String templetIds = request.getParameter("templetIds");
        List<Map<String, Object>> list = null;
        try {
            list = allItemsManager.findCooprationNobanjie(templetIds);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取所有未办结出错了!", e);
        }
        modelAndView.addObject("list", list);
        return modelAndView;
    }

    /**
     * 跳转到未办结更多页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView toMoreCooprationBanjie(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/coop_list_more_banjie");
        String templetIds = String.valueOf(request.getParameter("templetIds"));
        if (Strings.isNotBlank(templetIds) && !"null".equals(templetIds)) {
            modelAndView.addObject("templetIds", templetIds);
        } else {
            modelAndView.addObject("templetIds", "");
        }
        return modelAndView;
    }

    public ModelAndView getCtpAffairIdBycolSummaryById(HttpServletRequest request, HttpServletResponse response) {
        String summaryId = request.getParameter("id");
        List<Map<String, Object>> mapList = allItemsManager.findCtpAffairIdbySummaryid(summaryId);
        Map<String, Object> map = new HashMap<>();
        Map<String,Object> b=mapList.get(0);
        for (Map.Entry<String,Object> m:b.entrySet()){
            map.put("code", m.getValue()+"");
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

    /**
     * 协同所有已办结
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView cooprationFinishColumnDataSection(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/coopratiion_banjie");
        String templetIds = request.getParameter("templetIds");
        List<Map<String, Object>> list = null;
        try {
            list = allItemsManager.findCoopratiionBanjie(templetIds);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取所有办结出错了!", e);
        }
        modelAndView.addObject("list", list);
        return modelAndView;
    }

    /**
     * 跳转到未办结更多页面
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView toMoreOfNoBj(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/list_more_no_banjie");
        String templetIds = String.valueOf(request.getParameter("templetIds"));
        if (Strings.isNotBlank(templetIds) && !"null".equals(templetIds)) {
            modelAndView.addObject("templetIds", templetIds);
        } else {
            modelAndView.addObject("templetIds", "");
        }
        return modelAndView;
    }

    /**
     * 跳转到办结更多页面
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView toMoreOfBanjie(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/list_more_banjie");
        String templetIds = String.valueOf(request.getParameter("templetIds"));
        if (Strings.isNotBlank(templetIds) && !"null".equals(templetIds)) {
            modelAndView.addObject("templetIds", templetIds);
        } else {
            modelAndView.addObject("templetIds", "");
        }
        return modelAndView;
    }

    /**
     * 未办结
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView noProcessingSection(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/no_banjie");
        String templetIds = request.getParameter("templetIds");
        List<Map<String, Object>> list = null;
        try {
            list = allItemsManager.findXkjtAllNoBanJie(templetIds);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取所有办结出错了!", e);
        }
        modelAndView.addObject("list", list);
        return modelAndView;
    }

    /**
     * 所有已办结
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView finishColumnDataSection(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/allItems/index");
        String templetIds = request.getParameter("templetIds");
        List<Object> list = null;
        try {
            list = allItemsManager.findXkjtAllBanJie(templetIds);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取所有办结出错了!", e);
        }
        modelAndView.addObject("list", list);
        return modelAndView;
    }
}
