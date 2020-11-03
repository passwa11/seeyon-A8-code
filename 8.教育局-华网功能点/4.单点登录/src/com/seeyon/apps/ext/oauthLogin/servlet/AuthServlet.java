package com.seeyon.apps.ext.oauthLogin.servlet;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.oauthLogin.manager.oauthLoginManager;
import com.seeyon.apps.ext.oauthLogin.manager.oauthLoginManagerImpl;
import com.seeyon.apps.ext.oauthLogin.util.HttpUtil;
import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import com.seeyon.apps.ext.oauthLogin.util.StringHandle;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.MemberManager;
import com.seeyon.ctp.organization.manager.MemberManagerImpl;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthServlet extends HttpServlet {

    private oauthLoginManager oauthLoginManager = new oauthLoginManagerImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PropUtils propUtils = new PropUtils();
        String code = request.getParameter("code");
        if (null != code && !"".equals(code)) {
            System.out.println("code:" + code);
            String returnUrl = request.getParameter("returnUrl");
            Map<String, String> params = new HashMap<String, String>();
            params.put("clientId", propUtils.getSSOClientId());
            params.put("client_secret", propUtils.getSSOClientSecret());
            params.put("code", code);

            try {
                String result = HttpUtil.http(propUtils.getSSOOAuthAccess_token(), params);//得到相应的token
                System.out.println(result);
                Map object = (Map) JSONObject.parse(result);
                if (object.get("success").equals("true")) {
                    String userId = (String) object.get("uid");
                    String access_token = (String) object.get("access_token");
                    String expires_in = (String) object.get("expires_in");

                    String loginName = oauthLoginManager.selectLoginNameByCode(userId);
                    String encodeloginName = StringHandle.encode(loginName);
                    if (null != loginName) {
                        try {
                            String servername = request.getServerName();
                            int port = request.getServerPort();
                            String url = "http://" + servername + ":" + port + "/seeyon/";
                            response.sendRedirect(url + "login/sso?from=jyjSso&ticket=" + encodeloginName);
                        } catch (IOException e) {
                        }
                    }
                } else {
                    response.sendRedirect(propUtils.getSSOErrorPath() + "?msg=" + java.net.URLEncoder.encode("授权码超时!", "utf-8"));
                }
            } catch (Exception e) {
                response.sendRedirect(propUtils.getSSOErrorPath() + "?msg=" + java.net.URLEncoder.encode("远程服务器没有反应！", "utf-8"));//
            }
        } else {
            String returnUrl = request.getParameter("returnUrl");
            response.setCharacterEncoding("UTF-8");
            String url = propUtils.getSSOOAuthAuthorize() + "?clientId=" + propUtils.getSSOClientId() + "&returnUrl=" + java.net.URLEncoder.encode(returnUrl, "utf-8");
            response.sendRedirect(url);
        }

    }

    private String GetUserInfo(String access_token, String uId) {
        PropUtils propUtils = new PropUtils();
        Map<String, String> params = new HashMap<String, String>();
        params.put("Token", access_token);
        params.put("uId", uId);
        String result = HttpUtil.GetServiceHttp(propUtils.getSSOServices() + "API/APIPerson/GetPersonById", params);
        return result;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
