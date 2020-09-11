package com.seeyon.ctp.login.server.util;


public class StringUtilSso {
	/**
	 * @Title:是否为空
	 * @Description:是否为空，包括空字符串，或者空格
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.trim().length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @Title:是否不为空
	 * @Description:是否包含有效内容的字符串，与isEmpty()相反
	 * @param str
	 * @return
	 */
	public static boolean isUnEmpty(String str) {
		return !isEmpty(str);
	}
}
