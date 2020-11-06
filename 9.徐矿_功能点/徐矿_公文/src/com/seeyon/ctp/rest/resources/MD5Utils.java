package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.AppContext;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * @desc 文件MD5码获取工具类
 * @author xyy
 * @project [客开项目：徐矿集团]
 * @date 2019-04-23 10:55:32
 * @reference <a href="https://blog.csdn.net/u012416914/article/details/50395508"></a>
 */
 
public class MD5Utils {
    public static String getFileMD5(File file) {
        AppContext
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream fis = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            while ((len = fis.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
