package com.seeyon.apps.ext.oauthLogin.servlet;

import com.seeyon.apps.ext.oauthLogin.util.HttpUtil;
import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GetAuthCode extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PropUtils propUtils=new PropUtils();
        String success = request.getParameter("success");
        if (success.toLowerCase().equals("false")) {
            response.sendRedirect(propUtils.getSSOErrorPath() + "?msg=" + java.net.URLEncoder.encode("授权码超时!", "utf-8"));
            return;
        }
        String code = request.getParameter("code");
        String returnUrl = request.getParameter("returnUrl");

        Map<String, String> params = new HashMap<String, String>();
        params.put("clientId", propUtils.getSSOClientId());
        params.put("client_secret", propUtils.getSSOClientSecret());
        params.put("code", code);


        try {
            String result = HttpUtil.http(propUtils.getSSOOAuthAccess_token(), params);//得到相应的token
            JSONObject object = JSONObject.fromObject(result);
            if (object.get("success").equals("true")) {
                String userId = (String) object.get("uid");
                String access_token = (String) object.get("access_token");
                String expires_in = (String) object.get("expires_in");

                request.getSession().setAttribute(propUtils.getSSOSessionAccessToken(), access_token);
                request.getSession().setAttribute(propUtils.getSSOSessionUserId(), userId);
                request.getSession().setAttribute(propUtils.getSSOSessionExpires_in(), expires_in);

                //下面得到用户数据
                String userResult = this.GetUserInfo(access_token, userId);

                if (!userResult.equals("")) {
                    request.getSession().setAttribute(propUtils.getSSOSessionUser(), userResult);
                    response.sendRedirect(returnUrl);
                } else {
                    response.sendRedirect(propUtils.getSSOErrorPath() + "?msg=" + java.net.URLEncoder.encode("得到用户数据出现错误!", "utf-8"));
                }

            } else {
                response.sendRedirect(propUtils.getSSOErrorPath() + "?msg=" + java.net.URLEncoder.encode("授权码超时!", "utf-8"));
            }
        } catch (Exception e) {
            response.sendRedirect(propUtils.getSSOErrorPath() + "?msg=" + java.net.URLEncoder.encode("远程服务器没有反应！", "utf-8"));//
            return;
        }
    }

    /**
     * 得到用户数据
     *
     * @param access_token
     * @param uId
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private String GetUserInfo(String access_token, String uId) {
        PropUtils propUtils=new PropUtils();
        Map<String, String> params = new HashMap<String, String>();
        params.put("Token", access_token);
        params.put("uId", uId);
        String result = HttpUtil.GetServiceHttp(propUtils.getSSOServices() + "API/APIPerson/GetPersonById", params);
        return result;
    }
}
