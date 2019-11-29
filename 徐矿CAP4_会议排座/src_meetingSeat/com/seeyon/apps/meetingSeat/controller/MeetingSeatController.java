package com.seeyon.apps.meetingSeat.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.meetingSeat.manager.MeetingSeatManager;
import com.seeyon.apps.meetingSeat.manager.MeetingSeatManagerImpl;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;

public class MeetingSeatController extends BaseController{

	private static final Log logger = LogFactory.getLog(MeetingSeatController.class);
	
	
	private MeetingSeatManager meetingSeatManager = new MeetingSeatManagerImpl();
	
	private String msg;
	
	public void initialize() {
        System.out.println("========初始化会议排座插件========");
    }
    
    public void destroy() {
        System.out.println("========销毁会议排座插件========");
    }
    
    
    //返回参数人员信息
    public ModelAndView getMeetingSeatPersonEntity(HttpServletRequest request, HttpServletResponse response) {
    	AppContext.getCurrentUser();
    	try {
            String hybh=request.getParameter("hybh");
            if(null ==hybh || hybh.equals("") ){
            	return null;
            }
 
			//List<Map> list = meetingSeatManager.getMeetingSeatPersonList(hybh);
            Map<String, Object> map = new HashMap<>();
            map.put("code", 0);
            map.put("message", "");
            //map.put("total", list.size());
            //map.put("data", list);
            JSONObject json = new JSONObject(map);
            render(response, json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

	
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("进入ajax方法");
		
		Map<String, Object> res = new HashMap<>();
		
		res.put("success", true);
		System.out.println(AppContext.getSystemProperty("ajaxTest.msg"));
		res.put("msg", AppContext.currentUserLoginName() + ",456456" + msg);
		
		//render(response, JsonKit.toJson(res));
		
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
