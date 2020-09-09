package com.seeyon.ctp.login.server.filter;

import com.seeyon.ctp.login.client.constant.AuthConst;
import com.seeyon.ctp.login.server.storage.ClientStorage;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String uri = request.getRequestURI();
        if ("/logout".equals(uri)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (null != session.getAttribute(AuthConst.IS_LOGIN)) {
            String clientUrl = request.getParameter(AuthConst.CLIENT_URL);
            String token = request.getParameter(AuthConst.TOKEN);
            if (clientUrl != null && !"".equals(clientUrl)) {
                // 存储，用于注销
                ClientStorage.INSTANCE.set(token, clientUrl);
                response.sendRedirect(clientUrl + "?" + AuthConst.TOKEN + "=" + token);
                return;
            }
            if (!"/success".equals(uri)) {
                response.sendRedirect("/success");
                return;
            }
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        // 登录请求，放行
        if ("/".equals(uri) || "/login".equals(uri)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 其他请求，拦截
        response.sendRedirect("/");

    }

    @Override
    public void destroy() {

    }
}
