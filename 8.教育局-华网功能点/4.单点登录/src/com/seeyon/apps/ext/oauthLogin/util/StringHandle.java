package com.seeyon.apps.ext.oauthLogin.util;

public class StringHandle {

    public static String encode(String info) {
        byte[] b = info.getBytes();
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs;
    }

    public static String decode(String str) {
        if (str == null)
            return null;
        str = str.trim();
        int len = str.length();
        if (len == 0 || len % 2 == 1)
            return null;

        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < str.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0x" + str.substring(i, i + 2)).intValue();
            }
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        String code = "999992016999";
        String e = encode(code);
        System.out.println(e);
    }
}
