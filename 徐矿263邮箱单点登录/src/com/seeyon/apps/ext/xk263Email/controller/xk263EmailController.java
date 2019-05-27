package com.seeyon.apps.ext.xk263Email.controller;

import com.seeyon.ctp.common.controller.BaseController;
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
        String userId = "123";
        StringBuffer sb = new StringBuffer("http://pcc.263.net/PCC/263mail.do?");
        sb.append("cid=");
        sb.append(SSO_CID);
        sb.append("&domain=");
        sb.append(MAIL_DOMAIN);
        sb.append("&uid=");
        sb.append(userId);
        sb.append("&sign=");
        sb.append(sign("cid=" + SSO_CID, "&domain=" + MAIL_DOMAIN, "&uid=" + userId, "&key=" + SSO_KEY));


        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("");
    }

    /**
     * 生成调用API时的sign
     * 参数根据sign不同会有不同的意义，此处不说明
     * @param args
     * @return
     */
    public static String sign(String... args) {
//        if (args != null && args.length > 0) {
//            StringBuffer signsb = new StringBuffer("");
//            for (String arg : args) {
//                signsb.append(arg);
//            }
//            String sign = SSOMD5.createEncrypPassword(signsb.toString());
//            return sign.toLowerCase();
//        }
        return null;
    }

}
