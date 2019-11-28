/*********************************

 **********************************/
package com.seeyon.apps.ext.xk263Email.util;

import java.security.MessageDigest;

/**
 * MD5加密
 * 
 * 
 */
public class SSOMD5 {
	/**
	 * 存储十六进制值的数组
	 */
	private final static String[] hexArray = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/** 将string使用MD5方式加密，返回加密后代码 */
	public static String createEncrypPassword(String string) {
		return encrypByMD5(string);
	}

	/**
	 * 比较明文密码和加密后密码是否一致
	 * 
	 * @param password
	 *            加密后
	 * @param string
	 *            明文
	 * @return 加密后一致：true 不一致：false
	 */
	public static boolean verificationPassword(String password, String string) {
		if (password.equals(encrypByMD5(string))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 使用MD5方式加密
	 * 
	 * @param originString
	 * @return
	 */
	private static String encrypByMD5(String originString) {
		if (originString != null) {
			try {
				// 创建具有MD5算法的信息摘要
				MessageDigest md = MessageDigest.getInstance("MD5");
				// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
				byte[] results = md.digest(originString.getBytes());
				// 将得到的字节数组变成字符串返回
				String resultString = byteArrayToHex(results);
				return resultString.toUpperCase();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 将字节数组转换成十六进制字符串
	 * 
	 * @param b
	 * @return
	 */
	private static String byteArrayToHex(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHex(b[i]));
		}
		return resultSb.toString();
	}

	/**
	 * 将字节转换成十六进制字符串
	 * 
	 * @param b
	 * @return
	 */
	private static String byteToHex(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexArray[d1] + hexArray[d2];
	}

	/**
	 * 测试方法
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(SSOMD5.createEncrypPassword("admin"));
	}
}
