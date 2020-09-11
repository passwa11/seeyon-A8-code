package yh.app1.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yh.app1.util.LocalSessions;
import yh.app1.util.StringUtil;
/**
 * @Title:ExitServlet
 * @Description:退出接口，应用服务器需要实现类似的退出接口，为了简易这里使用servlet
 * @author 张颖辉
 * @date 2017年9月8日上午10:01:56
 * @version 1.0
 */
public class ExitServlet extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletContext context = this.getServletContext();// 容器
		String ssoServer = context.getInitParameter("ssoServerUrl");
		HttpSession session = request.getSession();
		// 本地会话id，SSO回调退出时需要的参数
		String localSessionId = request.getParameter("localSessionId");
		if (StringUtil.isEmpty(localSessionId)) {// 用户点击退出
			String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath()+"/";
			// 重定向到SSO的退出接口
			response.sendRedirect(ssoServer + "/auth/loginOut?server=" + basePath);
		} else {// SSO回调退出
			logger.info("认证中心回调退出");
			// 销毁本地seesion
			HttpSession localSession = LocalSessions.get(localSessionId);
			if (localSession != null) {
				localSession.invalidate();// 销毁
				LocalSessions.remove(localSessionId);
			} else {
				logger.info("已经退出，无需重复退出！");
			}

		}
	}

}
