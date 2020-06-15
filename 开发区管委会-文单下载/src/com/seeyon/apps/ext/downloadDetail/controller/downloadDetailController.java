package com.seeyon.apps.ext.downloadDetail.controller;

import javax.servlet.http.HttpServletRequest;
import com.seeyon.ctp.common.controller.BaseController;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class downloadDetailController extends BaseController{

public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
/*JSP file path:ApacheJetspeed\webapps\seeyon\WEB-INF\jsp\apps\ext\downloadDetail\downloadDetail.jsp*/
return new ModelAndView("apps/ext/downloadDetail/index");
}
}