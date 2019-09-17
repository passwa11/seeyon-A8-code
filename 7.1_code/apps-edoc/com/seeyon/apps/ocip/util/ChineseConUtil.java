package com.seeyon.apps.ocip.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**汉字转拼音
 * @author lemingchun
 *
 */
public class ChineseConUtil {
	/**
	 * 获取汉字串拼音首字母，英文字符不变
	 * 
	 * @param chinese
	 *            汉字串
	 * @return 汉语拼音首字母
	 */
	public static String cn2FirstSpell(String chinese) {
		StringBuilder pybf = new StringBuilder();
		char[] arr = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > 128) {
				try {
					String[] _t = PinyinHelper.toHanyuPinyinStringArray(arr[i],
							defaultFormat);
					if (_t != null) {
						pybf.append(_t[0].charAt(0));
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					return "";
				}
			} else {
				pybf.append(arr[i]);
			}
		}
		return pybf.toString().replaceAll("\\W", "").trim();
	}

	/**
	 * 获取汉字串拼音，英文字符不变
	 * 
	 * @param chinese
	 *            汉字串
	 * @return 汉语拼音
	 */
	public static String cn2Spell(String chinese) {
		if (chinese == null || "".equals(chinese)) {
			return "";
		} else {
			StringBuilder pybf = new StringBuilder();
			char[] arr = chinese.toCharArray();
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] > 128) {
					try {
						String[] str = PinyinHelper.toHanyuPinyinStringArray(
								arr[i], defaultFormat);
						if (str == null || str.length == 0) {
							break;
						}
						pybf.append(PinyinHelper.toHanyuPinyinStringArray(
								arr[i], defaultFormat)[0]);
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						break;
					}
				} else {
					pybf.append(arr[i]);
				}
			}
			return pybf.toString();
		}
	}

	/**
	 * 测试程序
	 */
	public static void main(String[] args) {
		String cnStr = "中华1人q民2共和国acb";
		System.out.println("汉字串首字母：" + cn2Spell(cnStr));
		System.out.println("汉字串拼音：" + cn2FirstSpell(cnStr));
	}
}
