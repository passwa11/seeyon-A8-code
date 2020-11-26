package com.seeyon.ctp.common.web.filter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.DirectoryScanner;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.util.FileUtil;
import com.seeyon.ctp.util.Strings;

/**
 * JSP身份验证。
 *
 * @author wangwenyou
 */
public class JSPAuthenticator extends AbstractAuthenticator {

	private static Log LOG = LogFactory.getLog(JSPAuthenticator.class);
	// 可匿名访问的白名单
	private static Set<String> anonymouswhiteList = new HashSet<String>();

	static {
		anonymouswhiteList.addAll(Arrays.asList("colsso.jsp",
				"gke.jsp",
				"send.jsp",
				"openPending.jsp",
				"gke2a8.jsp",
				"index.jsp",
				"lightweightsso.jsp",
				"pc.jsp",
				"pc2a8.jsp",
				"ssoproxy/jsp/ssoproxy.jsp",
				"thirdpartysso/listVoucherA8Form.jsp"));
	}

	private static boolean inited = false;
	// 允许执行的JSP文件白名单
	private static Map<String, Long> JSP_WHITELIST = new HashMap<String, Long>();

	private static void init() {
		if (inited) {
			return;
		}
		String securityConfDir = SystemEnvironment.getApplicationFolder() + File.separator + "WEB-INF"
				+ File.separator + "cfgHome" + File.separator + "security";
		File dir = new File(securityConfDir);
		DirectoryScanner ds = new DirectoryScanner();
		ds.setBasedir(dir);
		ds.setIncludes(new String[]{"jsp_whitelist_*"});
		ds.scan();
		String[] files = ds.getIncludedFiles();
		for (String whitelistFiles : files) {
			try {
				List<String> rules = FileUtils.readLines(new File(dir, whitelistFiles), "UTF-8");
				for (String rule : rules) {
					rule = rule.trim();
					if (rule.startsWith("#")) {
						continue;
					}
					File f = new File(SystemEnvironment.getApplicationFolder(), rule);
					if (f.exists()) {
						JSP_WHITELIST.put(rule, f.lastModified());
					}
				}
			} catch (IOException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
		inited = true;
	}

	@Override
	public boolean authenticate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uri = request.getRequestURI().substring(8);
//		init();
//		// 判是否被篡改
//		Long timestamp = JSP_WHITELIST.get(uri);
//		Long lastModified = timestamp;
//		if (lastModified==null) {
//			response.sendError(404);
//			onError("拦截到可疑的jsp访问：" + uri + " 来自 " +Strings.getRemoteAddr(request));
//		}else {
//			File f = new File(SystemEnvironment.getApplicationFolder(),uri);
//			if(f.exists() && FileUtil.inDirectory(f, new File(SystemEnvironment.getApplicationFolder()))) {
//				if(f.lastModified()!= lastModified && !SystemEnvironment.isDev()) {
//					response.sendError(401);
//					onError("jsp在运行期被篡改：" + uri + " 拒绝访问。 ");
//				}
//			}
//		}
		// 判是否已登录
		AppContext.initSystemEnvironmentContext(request, response);
		if (anonymouswhiteList.contains(uri)) {
			return true;
		} else {
			if (AppContext.getCurrentUser() != null) {
				return true;
			}
		}
		return false;
	}

	private static void onError(String msg) throws ServletException {
		LOG.error(msg);
		throw new ServletException(msg);
	}
}
