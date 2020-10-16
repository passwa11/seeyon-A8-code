package com.seeyon.apps.ext.Sso0715.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

//import com.seeyon.apps.common.kit.JsonKit;
//import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;

public class HelloAjaxController extends BaseController {

	private static final Log logger = LogFactory.getLog(HelloAjaxController.class);
	
	private String msg;
	
//	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		logger.info("进入ajax方法");
//
//		Map<String, Object> res = new HashMap<>();
//
//		res.put("success", true);
//		System.out.println("111111" + AppContext.getSystemProperty("ajaxTest.msg"));
//		res.put("msg", AppContext.currentUserLoginName() + "," + msg);
//
//		render(response, JsonKit.toJson(res));
//
//		//String user = AppContext.currentUserLoginName();
//
//
//		//response.sendRedirect("http://www.baidu.com?user="+user+"&sign=xdfsjdaskljdoiqwjdalj");
//		return null;
//	}
	
	@NeedlessCheckLogin
	public ModelAndView test(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("apps/hello/index");
		mav.addObject("name", "fangaowei");
		return mav;
	}


	/**
	 * 给前台渲染json数据
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

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
