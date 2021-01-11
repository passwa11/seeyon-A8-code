package com.seeyon.apps.ext.oauthLogin.filter;

import com.seeyon.apps.ext.oauthLogin.util.MapCacheUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class OauthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        RestTemplate rest = new RestTemplate();
        MultiValueMap map = new LinkedMultiValueMap();
        String t = MapCacheUtil.cache.get("token");
        if (null != t && !"".equals(t)) {
            map.add("token", MapCacheUtil.cache.get("token"));
            String token = MapCacheUtil.cache.get("token");
//            ResponseEntity<Object> result = rest.postForEntity("http://localhost:8080/server/oauth/check_token", map, Object.class);
//            Map<String, Object> user = (Map<String, Object>) result.getBody();
//            System.out.println(user.toString());
            ResponseEntity<String> responseEntity = rest.getForEntity("http://localhost:8080/server/unity/check_user_status?access_token=" + token, String.class);
            String obj = responseEntity.getBody();
            if ("true".equals(obj)) {
                filterChain.doFilter(request, response);
            } else {
                String uri = request.getRequestURI();
                String m = request.getParameter("method");
                if (m.equals("logout")) {
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    response.sendRedirect("/seeyon/main.do?method=logout");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
//        PropUtils prop = new PropUtils();
//        String clientId = prop.getSSO_ClientId();
//        String oauthUrl = prop.getSSO_OAuthAuthorize();
//        String redirect_uri = prop.getApplicationUrl();
//        String loginName = "";
//        if (null == code) {
//            response.sendRedirect(oauthUrl + "?response_type=code&scope=read&client_id=" + clientId + "&redirect_uri=" + redirect_uri);
//        } else {
//            loginName = OauthLoginUtil.togetToken(code, prop);
//        }
//        if (null != loginName && !"".equals(loginName)) {
//            filterChain.doFilter(request, response);
//        }
//        String encodeloginName = StringHandle.encode(loginName);
//        if (null != loginName && !"".equals(loginName)) {
//            try {
//                String servername = request.getServerName();
//                int port = request.getServerPort();
//                String url = "http://" + servername + ":" + port + "/seeyon/";
//                response.sendRedirect(url + "login/sso?from=xkSso&ticket=" + encodeloginName);
//            } catch (IOException e) {
//                System.out.println("单点登录OA系统出错了，错误信息：" + e.getMessage());
//            }
//        }
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
