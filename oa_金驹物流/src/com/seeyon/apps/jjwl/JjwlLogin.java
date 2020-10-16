package com.seeyon.apps.jjwl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;

public class JjwlLogin {
	private static final Log log = LogFactory.getLog(JjwlLogin.class);
	
	// 协同发起
	public static void oabill(HttpServletRequest request,HttpServletResponse response) throws Exception{
		// 操作员
		String ticket = request.getParameter("uid");
		// 操作模板编号
		String entityId = request.getParameter("entityId");
		// 操作模板ID
		String templateId = "";
		TemplateManager manager = (TemplateManager)AppContext.getBean("templateManager");
		CtpTemplate template = manager.getTempleteByTemplateNumber(entityId);
		templateId = template.getId().toString();
		// 协同发起URL
		String url = "/seeyon/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId="+templateId;
		log.info("发起协同URL：["+url+"]");
		// 单点登录操作
		SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "jjwlSSO");
		// 跳转URL
		String reURL = "/seeyon/main.do?method=login&ticket="+ticket+"&login.destination="+java.net.URLEncoder.encode(url);
		// 跳转
		log.info("发起协同跳转URL：["+reURL+"]");
		response.sendRedirect(reURL);
	}
	
	// 代办事项、公告
	public static void gainbill(HttpServletRequest request,HttpServletResponse response) throws Exception {
		log.info("公告URL：[start]");
		// 操作员
		String ticket = request.getParameter("ticket");
		// 事项ID
		String affairId = request.getParameter("affairId");
		// 事项类型
		String openFrom = request.getParameter("openFrom");
		
		String url="/seeyon/collaboration/collaboration.do?method=summary&openFrom="+openFrom+"&affairId="+affairId;
		log.info("公告URL：["+url+"]");
		// 单点登录操作
		SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "jjwlSSO");
		// 跳转URL
		String reURL = "/seeyon/main.do?method=login&ticket="+ticket+"&login.destination="+java.net.URLEncoder.encode(url);
		log.info("公告跳转URL：["+reURL+"]");
		// 跳转
		response.sendRedirect(reURL);
	}
}
