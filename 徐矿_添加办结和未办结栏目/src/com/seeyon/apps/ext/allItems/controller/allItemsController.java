package com.seeyon.apps.ext.allItems.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.allItems.manager.allItemsManager;
import com.seeyon.apps.ext.allItems.manager.allItemsManagerImpl;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.util.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public class allItemsController extends BaseController {

    private static Log log = LogFactory.getLog(allItemsController.class);

    private allItemsManager allItemsManager = new allItemsManagerImpl();

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
        List<Object> list = null;
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
