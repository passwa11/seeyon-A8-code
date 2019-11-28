package com.seeyon.apps.ext.xk263Email.util;

/**
 * 周刘成   2019/5/28
 */
public class SignUtil {

    /**
     * 生成调用API时的sign
     * 参数根据sign不同会有不同的意义，此处不说明
     * @param args
     * @return
     */
    public static String sign(String... args) {
        if (args != null && args.length > 0) {
            StringBuffer signsb = new StringBuffer("");
            for (String arg : args) {
                signsb.append(arg);
            }
            String sign = SSOMD5.createEncrypPassword(signsb.toString());
            return sign.toLowerCase();
        }
        return null;
    }
}
