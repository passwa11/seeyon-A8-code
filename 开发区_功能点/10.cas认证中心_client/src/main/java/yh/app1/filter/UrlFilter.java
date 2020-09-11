package yh.app1.filter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Title:UrlFilter
 * @Description:跟踪请求的内容和消耗时间
 * @version 1.0
 */
//登陆状态验证控制过滤器
public class UrlFilter implements Filter {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private String contextPath;

	@Override
	public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) sRequest;
		HttpServletResponse response = (HttpServletResponse) sResponse;
		HttpSession session = request.getSession(true);// 若存在会话则返回该会话，否则新建一个会话。
		/**##### basePath路径的保存   #####**/
		String path = request.getContextPath();
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path
				+ "/";
		// logger.info(basePath);
		request.setAttribute("basePath", basePath);
		/**##### 请求路径打印   #####**/
		String url = request.getServletPath();
		if (url.equals(""))
			url += "/";
		// post请求编码,交给spring过滤器
		// request.setCharacterEncoding("utf-8");// 统一编码格式
		String loginName = (String) session.getAttribute("loginName");
		/** 无需验证的 */
		String[] strs = { "/css/", "/js/", "themes", ".css", ".jpg", ".png" }; // 路径中包含这些字符串的,可以不用用检查
		// 特殊用途的路径可以直接访问
		if (strs != null && strs.length > 0) {
			for (String str : strs) {
				if (url.indexOf(str) >= 0) {
					filterChain.doFilter(request, response);
					return;
				}
			}
		}
		/**
		 * 
		 * 使用下面的方法打印出所有参数和参数值，会使中文请求出现乱码，解决办法:在上面加入request.setCharacterEncoding(
		 * ) 函数
		 */
		Enumeration<?> enu = request.getParameterNames();
		Map<String, String> parameterMap = new HashMap<String, String>();
		while (enu.hasMoreElements()) {
			String paraName = (String) enu.nextElement();
			parameterMap.put(paraName, request.getParameter(paraName));
		}
		logger.info("【url日志】 UrlFilter:【" + basePath.substring(0,basePath.lastIndexOf("/"))+url + "】  loginName=" + loginName + " parameterMap="
				+ parameterMap);
		/**********
		 * 避免中文get请求乱码（并且适用于带空格'%20'的getUrl） -zyh （不能和tomcat的设置
		 * URIEncoding="UTF-8" 同时使用）
		 **************/
		// String method=request.getMethod();
		// String json=request.getParameter("json");
		// if
		// ("GET".equals(method.toUpperCase())&&StringHelper.isNotEmpty(json.trim()))
		// {
		// System.out.println("UrlFilter-GET:[原]json="+json);
		// json = json.replaceAll("\n", "&");
		// json=new String(json.getBytes("ISO-8859-1"), "utf-8");
		// request.setAttribute("json4get", json);
		// System.out.println("UrlFilter-GET:[新]json="+json);
		// System.out.println("※ 如果json传递为乱码，请使用
		// request.getAttribute(\"json4get\"); 获取处理后的json。");
		// }
		/** 响应计时 **/
		Long startMillis = System.currentTimeMillis();
		filterChain.doFilter(request, response);
		logger.info("【url日志】UrlFilter【" +  basePath.substring(0,basePath.lastIndexOf("/"))+url + "】  :耗时=" + (System.currentTimeMillis() - startMillis));
	}

	@Override
	public void destroy() {
		System.out.println(contextPath + " UrlFilter：销毁");

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		contextPath = filterConfig.getServletContext().getContextPath();
		System.out.println(contextPath + " UrlFilter：创建");
	}

}