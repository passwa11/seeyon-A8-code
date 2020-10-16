package com.seeyon.ctp.login.server.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class UrlFilter implements Filter {

    private String contextPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        contextPath = filterConfig.getServletContext().getContextPath();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(true);
        /** basePath路径的保存 */
        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
        request.setAttribute("basePath", basePath);
        /** 请求路径 */
        String url = request.getServletPath();
        if (url.equals("")) {
            url += "/";
        }
        request.setCharacterEncoding("utf-8");
        String loginName = (String) session.getAttribute("loginName");
        /** 无需验证的 */
        String[] strs = {"/css/", "/js/", "themes", ".css", ".jpg", ".png", ".svg", ".gif", ".icon"}; // 路径中包含这些字符串的,可以不用用检查
        //特殊用途的路径可以直接访问
        if (strs != null && strs.length > 0) {
            for (String s : strs) {
                if (url.indexOf(s) >= 0) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        /**
         * 使用下面的方法打印出所有参数和参数值，会使中文请求出现乱码，解决办法:在上面加入request.setCharacterEncoding(
         * ) 函数
         */
        Enumeration<?> enu=request.getParameterNames();
        Map<String,String> parameterMap=new HashMap<>();
        while(enu.hasMoreElements()){
            String paramName=(String) enu.nextElement();
            parameterMap.put(paramName,request.getParameter(paramName));
        }
        /** 响应计时*/
        Long startMillis=System.currentTimeMillis();
        request.setAttribute("test","tttttttt");
        filterChain.doFilter(request,response);

    }

    @Override
    public void destroy() {

    }
}
