package com.monkeyk.sos.web.controller.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**
 * 所谓注销只需将access_token和refresh_token失效即可，
 * 我们模仿org.springframework.security.oauth2.provider.endpoint.TokenEndpoint
 * 写一个使access_token和refresh_token失效的Endpoint:
 */
@FrameworkEndpoint
public class RevokeTokenEndpoint {

    @Autowired
    @Qualifier("consumerTokenServices")
    ConsumerTokenServices consumerTokenServices;

    @Autowired
    private TokenStore tokenStore;

    @DeleteMapping(value = "/remove_token", params = "access_token")
    public void deleteToken(Principal principal, String access_token){
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(access_token);
        if (accessToken != null) {
            // 移除access_token
            tokenStore.removeAccessToken(accessToken);

            // 移除refresh_token
            if (accessToken.getRefreshToken() != null) {
                tokenStore.removeRefreshToken(accessToken.getRefreshToken());
            }

        }

    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/oauth/token")
    @ResponseBody
    public String revokeToken(String access_token) {
        boolean flag = consumerTokenServices.revokeToken(access_token);
        if (flag) {
            return "注销成功";
        } else {
            return "注销失败";
        }
    }

}
