package com.seeyon.ctp.login.server.filter;


import com.seeyon.ctp.login.client.constant.AuthConst;
import com.seeyon.ctp.login.server.storage.SessionStorage;
import com.seeyon.ctp.login.util.EncryptString;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 客户端登录filter
 */
public class LoginFilter implements Filter {
    private FilterConfig config;

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession();
		String uri = request.getRequestURI();
        // 已经登录，放行
        if (session.getAttribute(AuthConst.IS_LOGIN) != null) {
            chain.doFilter(req, res);
            return;
        }
        // 从认证中心回跳的带有token的请求，有效则放行
        String token = request.getParameter(AuthConst.TOKEN);
        if (token != null) {
            session.setAttribute(AuthConst.IS_LOGIN, true);
            session.setAttribute(AuthConst.TOKEN, token);
            // 存储，用于注销
            SessionStorage.INSTANCE.set(token, session);
            String username = request.getParameter("username");
            CasHttpServletRequestWrapper requestWrapper = new CasHttpServletRequestWrapper(request, username);
            chain.doFilter(requestWrapper, res);
            return;
        } else {
            // 重定向至登录页面，并附带当前请求地址
            if (session.getAttribute(AuthConst.IS_LOGIN) == null) {
                response.sendRedirect(config.getInitParameter(AuthConst.LOGIN_URL) + "?flag=1&" + AuthConst.CLIENT_URL + "=" + request.getRequestURL());
            }

        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = filterConfig;
    }

    final class CasHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private String myprincipal;

        public CasHttpServletRequestWrapper(HttpServletRequest request, String principal) {
            super(request);
            myprincipal = principal;
        }

        @Override
        public String getRemoteUser() {
            return EncryptString.decode(getMyprincipal());
        }

        public String getMyprincipal() {
            return myprincipal;
        }

        public void setMyprincipal(String myprincipal) {
            this.myprincipal = myprincipal;
        }
    }

}