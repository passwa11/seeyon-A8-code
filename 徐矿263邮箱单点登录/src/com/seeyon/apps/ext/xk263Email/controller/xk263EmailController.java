package com.seeyon.apps.ext.xk263Email.controller;

import com.seeyon.apps.ext.xk263Email.util.SSOMD5;
import com.seeyon.apps.ext.xk263Email.util.SignUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class xk263EmailController extends BaseController {

    /**
     * 263授权单点登录sid和key
     */
    public static String SSO_CID = "ff80808150fbc5b2015124367cc103ba";
    public static String SSO_KEY = "4fW3H8my2cBeE";
    public static String MAIL_DOMAIN = "xkjt.net";

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /**
         * 获取单点登录URL
         * 单点登录接口为HTTP链接形式，根据接口要求提供正确参数，
         * 访问如下链接可以直接登录263 Web Mail，显示指定用户的邮箱
         * http://pcc.263.net/PCC/263mail.do?cid=单点登录接口账号&domain=邮箱域名&uid=用户ID&sign=加密标识
         * sign = 32位MD5 （ cid=单点登录接口账号&domain=邮箱域名&uid=用户ID&key=单点登录接口密钥 ）
         * @return
         */
        String alias = CurrentUser.get().getLoginName();

        StringBuffer sb = new StringBuffer("http://pcc.263.net/PCC/263mail.do?");
        sb.append("cid=");
        sb.append(SSO_CID);
        sb.append("&domain=");
        sb.append(MAIL_DOMAIN);
        sb.append("&uid=");
        sb.append(alias);
        sb.append("&sign=");
        sb.append(SignUtil.sign("cid=" + SSO_CID, "&domain=" + MAIL_DOMAIN, "&uid=" + alias, "&key=" + SSO_KEY));

        response.sendRedirect(sb.toString());
        return null;
    }


}
