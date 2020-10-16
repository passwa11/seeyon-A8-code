package com.seeyon.v3x.edoc.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import com.seeyon.v3x.edoc.manager.EdocUpgradeManager;

public class EdocUpgradeControllor extends BaseController {
	
	private EdocUpgradeManager upgradeManager = (EdocUpgradeManager)AppContext.getBean("edocUpgradeManager");
	
	@NeedlessCheckLogin
	public ModelAndView upgrade(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int state = upgradeManager.getUpgradState();
		if(state == upgradeManager.UPGRADE_STATE_DOING){
			super.rendJavaScript(response, "alert(\"升级中。。。\");");
            return null;
		}else if(state == upgradeManager.UPGRADE_STATE_DONE){
			super.rendJavaScript(response, "alert(\"已经完成升级。。。\");");
            return null;
		}else if(state == upgradeManager.UPGRADE_STATE_DONE_ERROR){
			super.rendJavaScript(response, "alert(\"升级出错了。。。\");");
            return null;
		}else if(state == upgradeManager.UPGRADE_STATE_NEEDTODO_NO){
			super.rendJavaScript(response, "alert(\"无需升级。。。\");");
            return null;
		}else{
			 try {
				 upgradeManager.setUpgradeState(upgradeManager.UPGRADE_STATE_DOING);
				 HttpSession session = request.getSession(true);
				 session.setMaxInactiveInterval(3600*4);
				 String result = upgradeManager.upgrade();
				if ("-1".equals(result)) {
					upgradeManager.setUpgradeState(upgradeManager.UPGRADE_STATE_DONE);
					logger.info("旧版本升级成功!须重启服务方能正常使用!");
					upgradeManager.getTemplateStr();
					response.setContentType("text/html;charset=UTF-8");
					PrintWriter out = response.getWriter();
					out.println("<script>");
					out.println("alert(\"旧版本升级成功!须重启服务方能正常使用!\");");
					out.println("parent._reflush();");
					out.println("</script>");
					out.flush();
				}
		     } catch (Exception e) {
		    	 upgradeManager.setUpgradeState(upgradeManager.UPGRADE_STATE_DONE_ERROR);
		         super.rendJavaScript(response, "alert(\"旧版本升级失败!\");");
		     }
		}
        return null;
	}

	public EdocUpgradeManager getUpgradeManager() {
		return upgradeManager;
	}

	public void setUpgradeManager(EdocUpgradeManager upgradeManager) {
		this.upgradeManager = upgradeManager;
	}

}
