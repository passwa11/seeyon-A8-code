package com.seeyon.apps.collaboration.listdesc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.i18n.ResourceUtil;

public class ListDescController extends BaseController {
	private static final Logger  LOGGER     = Logger.getLogger(ListDescController.class);
   
	 /**
     * 列表描述页面
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView listDesc(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("apps/collaboration/listDesc");
        String type = request.getParameter("type");
        if("listSent".equals(type)){//已发事项
            mav.addObject("desc", ResourceUtil.getString("menu.collaboration.listsent"));
        }else if("listDone".equals(type)){//已办事项
            mav.addObject("desc", ResourceUtil.getString("menu.collaboration.listDone"));
        }else if("listPending".equals(type)){//待办事项
            mav.addObject("desc", ResourceUtil.getString("menu.collaboration.listPending"));
        }else if("listWaitSend".equals(type)){//待发事项
            mav.addObject("desc", ResourceUtil.getString("menu.collaboration.listWaitsend"));
        }else if("listSupervise".equals(type)){//待发事项
            mav.addObject("desc", ResourceUtil.getString("menu.collaboration.supervise"));
        }
        mav.addObject("type",type);
        mav.addObject("size", request.getParameter("size"));
        return mav;
    }
}

