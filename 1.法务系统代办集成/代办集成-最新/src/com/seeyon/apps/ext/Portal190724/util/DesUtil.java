package com.seeyon.apps.ext.Portal190724.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;


public class DesUtil {
    private static Key key;

    /**
     * 加密String明文输入,String密文输出
     *
     * @param strMing 待加密文本
     * @param strKey  秘钥
     */
    public static String getEncString(String strMing, String strKey) {
        if (strKey == null || "".equals(strKey)) {
            strKey = "daorigin";
        }
        getKey(strKey);
        String strMi = "";
        try {
            return byte2hex(getEncCode(strMing.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strMi;
    }

    /**
     * 解密 以String密文输入,String明文输出
     *
     * @param strMi  待解密文本
     * @param strKey 秘钥
     */
    public static String getDesString(String strMi, String strKey) {
        if (strKey == null || "".equals(strKey)) {
            strKey = "daorigin";
        }
        getKey(strKey);
        String strMing = "";
        try {
            return new String(getDesCode(hex2byte(strMi.getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strMing;
    }

    /**
     * 根据参数生成KEY
     */
    private static void getKey(String strKey) {
        try {
            KeyGenerator _generator = KeyGenerator.getInstance("DES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(strKey.getBytes());
            _generator.init(secureRandom);
            key = _generator.generateKey();
            _generator = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加密以byte[]明文输入,byte[]密文输出
     */
    private static byte[] getEncCode(byte[] byteS) {
        byte[] byteFina = null;
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byteFina = cipher.doFinal(byteS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    /**
     * 解密以byte[]密文输入,以byte[]明文输出
     */
    private static byte[] getDesCode(byte[] byteD) {
        Cipher cipher;
        byte[] byteFina = null;
        try {
            cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byteFina = cipher.doFinal(byteD);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    /**
     * 二进制转字符串
     */
    private static String byte2hex(byte[] b) { // 一个字节的数，
        // 转成16进制字符串
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            // 整数转成十六进制表示
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs.toUpperCase(); // 转成大写
    }

    private static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("长度不是偶数");
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }
}

