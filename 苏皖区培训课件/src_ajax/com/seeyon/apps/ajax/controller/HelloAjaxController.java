package com.seeyon.apps.ajax.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.common.kit.JsonKit;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;

public class HelloAjaxController extends BaseController {

	private static final Log logger = LogFactory.getLog(HelloAjaxController.class);
	
	private String msg;
	
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("进入ajax方法");
		
		Map<String, Object> res = new HashMap<>();
		
		res.put("success", true);
		System.out.println(AppContext.getSystemProperty("ajaxTest.msg"));
		res.put("msg", AppContext.currentUserLoginName() + ",456456" + msg);
		
		render(response, JsonKit.toJson(res));
		
		return null;
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
