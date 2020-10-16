package com.seeyon.apps.ext.xk263Email.util;

import com.seeyon.apps.ext.xk263Email.axis.xmapi.XmapiImpl;
import com.seeyon.apps.ext.xk263Email.axis.xmapi.XmapiImplServiceLocator;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import www.seeyon.com.utils.Base64Util;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 周刘成   2019/5/29
 */
public class ZCommonUtil {

    private static Logger logger = LoggerFactory.getLogger(ZCommonUtil.class);

    /**
     * 263授权单点登录sid和key
     */
    public static String SSO_CID = "ff80808150fbc5b2015124367cc103ba";
    public static String SSO_KEY = "4fW3H8my2cBeE";
    public static String MAIL_DOMAIN = "xkjt.net";

    public static ExecutorService pool = Executors.newCachedThreadPool();
    public static XmapiImplServiceLocator service = new XmapiImplServiceLocator();
    /**
     * 263授权人员组织account和key
     */
    public static String API_KEY = "R5hy7M2dcv4AK";
    public static String API_ACCOUNT = "xkjt.net";


    //创建263邮箱用户需要的参数
    // 默认密码
    public static String USER_PWD = "123";
    // 密码类型 密码的明文:0 密码的32位MD5小写加密串:4
    public static int USER_CRYPTTYPE_0 = 0;
    // 组ID（空间大小ID），263G-5万封:33 263G-10万封:43
    public static int USER_GID_33 = 33;
    // 角色ID，默认0
    public static int USER_ROLE_ID = 0;
    // 首次登录是否需要修改密码，不能为空，0为不需修改，1为需要修改
    public static int USER_CHANGEPWD_OFF = 0;
    public static int USER_CHANGEPWD_ON = 1;

    public static String get263LoginUrl() {
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

        return sb.toString();
    }

    public static String getUnreadCount() {
        String alias = CurrentUser.get().getLoginName();
        //获取263未读邮件数
        String count = "";
        try {
            XmapiImpl apiImpl = service.getxmapi();
            String sign = SignUtil.sign(alias, MAIL_DOMAIN, API_KEY);
            count = apiImpl.getDirInfo_New(alias, MAIL_DOMAIN, "", 0, API_ACCOUNT, sign);
            try {
                int code = Integer.parseInt(count);
                if (code < 0) {
                    count = "0";
                }
            } catch (Exception e) {
                count = "0";
            }
        } catch (ServiceException e) {
            count = "0";
        } catch (RemoteException e) {
            count = "0";
        }
        return count;
    }

    /**
     * 将字符串转换成gbk编码后用Base64编码
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static String GBKToBase64(String s) throws Exception {
        if (s == null) {
            return "";
        }
        String base64 = Base64Util.encode(s.getBytes("GBK"));
        return base64;
    }

    public static String get263UserList() {
        String info = "";
        try {
            XmapiImpl apiImpl = service.getxmapi();
            String sign = SignUtil.sign(MAIL_DOMAIN, API_ACCOUNT, API_KEY);
            info = apiImpl.getDomainUserlist_New(MAIL_DOMAIN, API_ACCOUNT, sign);
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException re) {
            logger.error(re.getMessage());
        }
        return info;
    }


}
