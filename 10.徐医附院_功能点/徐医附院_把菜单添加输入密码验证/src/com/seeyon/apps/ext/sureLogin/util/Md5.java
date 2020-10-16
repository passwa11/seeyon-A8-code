package com.seeyon.apps.ext.sureLogin.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 周刘成   2019-12-12
 */
public class Md5 {

    public static String encodeStr(String msg) {
        String info = "";
        try {
            BASE64Encoder encoder = new BASE64Encoder();
            info = encoder.encode(msg.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static String decodeStr(String msg) {
        String info = "";
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            info = new String(decoder.decodeBuffer(msg), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    // MD5转换
    public static String encryptToMD5(String password) {
        byte[] digesta = null;
        String result = null;
        try {

            // 得到一个MD5的消息摘要
            MessageDigest mdi = MessageDigest.getInstance("MD5");
            // 添加要进行计算摘要的信息
            mdi.update(password.getBytes("utf-8"));
            // 得到该摘要
            digesta = mdi.digest();
            result = byteToHex(digesta);
        } catch (NoSuchAlgorithmException e) {

        } catch (UnsupportedEncodingException e) {
            // TODO 自动生成
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将二进制转化为16进制字符串
     */
    public static String byteToHex(byte[] pwd) {
        StringBuilder hs = new StringBuilder("");
        String temp = "";
        for (int i = 0; i < pwd.length; i++) {
            temp = Integer.toHexString(pwd[i] & 0XFF);
            if (temp.length() == 1) {
                hs.append("0").append(temp);
            } else {
                hs.append(temp);
            }
        }
        return hs.toString().toLowerCase();
    }
}
