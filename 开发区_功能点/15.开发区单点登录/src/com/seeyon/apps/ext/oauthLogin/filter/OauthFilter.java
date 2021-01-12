package com.seeyon.apps.ext.oauthLogin.filter;

import com.seeyon.apps.ext.oauthLogin.util.MapCacheUtil;
import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

public class OauthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        User user = AppContext.getCurrentUser();
        if (null != user) {
            RestTemplate rest = new RestTemplate();
            MultiValueMap map = new LinkedMultiValueMap();
            String t = MapCacheUtil.cache.get(user.getLoginName());
            if (null != t && !"".equals(t)) {
                map.add(user.getLoginName(), MapCacheUtil.cache.get(user.getLoginName()));
                String token = MapCacheUtil.cache.get(user.getLoginName());
                ResponseEntity<String> responseEntity = rest.getForEntity("http://localhost:8080/server/unity/check_user_status?access_token=" + token, String.class);
                String obj = responseEntity.getBody();
                if ("true".equals(obj)) {
                    filterChain.doFilter(request, response);
                } else {
                    String m = request.getParameter("method");
                    if (m.equals("logout") || m.equals("index")) {
                        filterChain.doFilter(request, response);
                        return;
                    } else {
                        String servername = request.getServerName();
                        PropUtils p = new PropUtils();
                        String ssoLogout = p.getSSO_Logout();
                        response.sendRedirect(ssoLogout + "?returnUrl=" + URLEncoder.encode("http://" + servername + "/seeyon/main.do?method=logout"));
                    }
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }


    }

    /**
     * 白名单
     */
    public String[] whitelist() {
        String[] arr = {"main.do"};
        return arr;
    }

    @Override
    public void destroy() {

    }
}
