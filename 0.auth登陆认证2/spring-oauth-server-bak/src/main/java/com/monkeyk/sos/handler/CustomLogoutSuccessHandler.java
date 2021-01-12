package com.monkeyk.sos.handler;

import com.monkeyk.sos.domain.CheckUserStatus;
import com.monkeyk.sos.domain.shared.security.SOSUserDetails;
import com.monkeyk.sos.service.CheckUserStatusServiceImpl;
import com.monkeyk.sos.service.RedisCheckUserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Component
public class CustomLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements LogoutSuccessHandler {

    @Autowired
    private CheckUserStatusServiceImpl service;

    @Autowired
    private RedisCheckUserStatus redisCheckUserStatus;

    @Autowired
    private ConsumerTokenServices consumerTokenServices;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String returnUrl = request.getParameter("returnUrl");
        if (null != authentication) {
            SOSUserDetails map = (SOSUserDetails) authentication.getPrincipal();
            String username = map.user().username();
            List<CheckUserStatus> statusList = service.findAll(username);
            for (CheckUserStatus checkUserStatus : statusList) {
                consumerTokenServices.revokeToken(checkUserStatus.getToken());
            }
            //数据库
//            service.delete(username);
            //redis
            redisCheckUserStatus.delete(username);
            // 将子系统的cookie删掉
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
        if (null != returnUrl) {
            response.sendRedirect(returnUrl);
        } else {
            super.handle(request, response, authentication);
        }
    }
}
