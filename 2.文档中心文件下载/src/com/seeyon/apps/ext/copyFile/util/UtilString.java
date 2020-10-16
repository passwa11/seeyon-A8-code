package com.seeyon.apps.ext.copyFile.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

final public class UtilString {

	public static String formartString(String src, int length) {
		try {
			String tmp = src.trim();
			int size = tmp.getBytes().length;
			if (size > length) {
				byte[] tmpBytes = new byte[length];
				byte[] srcBytes = tmp.getBytes();
				for (int i = 0; i < length; i++) {
					tmpBytes[i] = srcBytes[i];
				}
				tmp = new String(tmpBytes);
			} else {
				int x = length - size;
				for (int i = 0; i < x; i++) {
					tmp += " ";
				}
			}
			return tmp;
		} catch (Exception e) {
			return "";
		}
	}

	private static class StringParser {
		private String text;

		private String delimiter;

		private boolean returnDelimiterTokens = false;

		private List<String> parsedTokens = new ArrayList<String>();

		private int tokenIndex = 0;

		private StringParser(String text, String delimiter, boolean returnTokens) {
			this(text, delimiter, returnTokens, false);
		}

		private StringParser(String text, String delimiter, boolean returnTokens, boolean ignoreCase) {
			super();
			this.text = text;
			this.delimiter = delimiter;
			this.returnDelimiterTokens = returnTokens;
			parse(ignoreCase);
		}

		private void parse(boolean ignoreCase) {
			String matchText = null;
			String matchDelim = null;
			if (ignoreCase) {
				matchText = text.toUpperCase();
				matchDelim = delimiter.toUpperCase();
			} else {
				matchText = text;
				matchDelim = delimiter;
			}
			int startIndex = 0;
			int endIndex = matchText.indexOf(matchDelim, startIndex);
			while (endIndex != -1) {
				String token = text.substring(startIndex, endIndex);
				parsedTokens.add(token);
				if (returnDelimiterTokens) {
					parsedTokens.add(delimiter);
				}
				startIndex = endIndex + delimiter.length();
				endIndex = matchText.indexOf(matchDelim, startIndex);
			}
			parsedTokens.add(text.substring(startIndex));
		}

		private int countTokens() {
			return parsedTokens.size();
		}

		private String nextToken() {
			return (String) parsedTokens.get(tokenIndex++);
		}
	}

	public static String replace(String str, String findValue, String replaceValue) {
		StringParser tok = new StringParser(str, findValue, false);
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < tok.countTokens() - 1; i++) {
			result.append(tok.nextToken());
			result.append(replaceValue);
		}
		result.append(tok.nextToken());
		return result.toString();
	}

	public static String replace(String str, String findValue, String replaceValue, boolean ignoreCase) {
		StringParser tok = new StringParser(str, findValue, false, ignoreCase);

		StringBuffer result = new StringBuffer();
		for (int i = 0; i < tok.countTokens() - 1; i++) {
			result.append(tok.nextToken()).append(replaceValue);
		}
		result.append(tok.nextToken());

		return result.toString();
	}

	public static String[] tokenize(String str, String findValue) {
		return tokenize(str, findValue, false);
	}

	public static String[] tokenize(String str, String findValue, boolean trim) {
		return tokenize(str, findValue, trim, false);
	}

	public static String[] tokenize(String str, String findValue, boolean trim, boolean ignoreCase) {
		StringParser tok = new StringParser(str, findValue, false, ignoreCase);
		List<String> result = new LinkedList<String>();
		for (int i = 0; i < tok.countTokens(); i++) {
			if (trim) {
				result.add(tok.nextToken().trim());
			} else {
				result.add(tok.nextToken());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public static String[] tokenizeLines(String string) {
		StringTokenizer tok = new StringTokenizer(string, "\n\r", true);
		List<String> result = new LinkedList<String>();
		String previousToken = null;
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (token.equals("\r")) {
				; // Discard
			} else if (token.equals("\n")) {
				if (previousToken != null) {
					result.add(previousToken);
				} else {
					result.add(""); // Add a blank line

				}
				previousToken = null;
			} else {
				previousToken = token;
			}
		}
		// Make sure we get the last line, even if it didn't end
		// with a carriage return
		if (previousToken != null) {
			result.add(previousToken);
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	public static boolean isEmpty(String string) {
		if (string == null) {
			return true;
		}
		if (string.length() == 0) {
			return true;
		}
		if (string.trim().length() == 0) {
			return true;
		}
		return false;
	}

	public static final byte hexCharToByte(char ch) {
		switch (ch) {
		case '0':
			return 0x00;
		case '1':
			return 0x01;
		case '2':
			return 0x02;
		case '3':
			return 0x03;
		case '4':
			return 0x04;
		case '5':
			return 0x05;
		case '6':
			return 0x06;
		case '7':
			return 0x07;
		case '8':
			return 0x08;
		case '9':
			return 0x09;
		case 'a':
			return 0x0A;
		case 'b':
			return 0x0B;
		case 'c':
			return 0x0C;
		case 'd':
			return 0x0D;
		case 'e':
			return 0x0E;
		case 'f':
			return 0x0F;
		}
		return 0x00;
	}

	public static String encodeBase64(String data) {
		return encodeBase64(data.getBytes());
	}

	public static String encodeBase64(byte[] data) {
		int c;
		int len = data.length;
		StringBuffer ret = new StringBuffer(((len / 3) + 1) * 4);
		for (int i = 0; i < len; ++i) {
			c = (data[i] >> 2) & 0x3f;
			ret.append(cvt.charAt(c));
			c = (data[i] << 4) & 0x3f;
			if (++i < len)
				c |= (data[i] >> 4) & 0x0f;

			ret.append(cvt.charAt(c));
			if (i < len) {
				c = (data[i] << 2) & 0x3f;
				if (++i < len)
					c |= (data[i] >> 6) & 0x03;

				ret.append(cvt.charAt(c));
			} else {
				++i;
				ret.append((char) fillchar);
			}

			if (i < len) {
				c = data[i] & 0x3f;
				ret.append(cvt.charAt(c));
			} else {
				ret.append((char) fillchar);
			}
		}
		return ret.toString();
	}

	public static String decodeBase64(String data) {
		return decodeBase64(data.getBytes());
	}

	public static String decodeBase64(byte[] data) {
		int c, c1;
		int len = data.length;
		StringBuffer ret = new StringBuffer((len * 3) / 4);
		for (int i = 0; i < len; ++i) {
			c = cvt.indexOf(data[i]);
			++i;
			c1 = cvt.indexOf(data[i]);
			c = ((c << 2) | ((c1 >> 4) & 0x3));
			ret.append((char) c);
			if (++i < len) {
				c = data[i];
				if (fillchar == c)
					break;

				c = cvt.indexOf((char) c);
				c1 = ((c1 << 4) & 0xf0) | ((c >> 2) & 0xf);
				ret.append((char) c1);
			}

			if (++i < len) {
				c1 = data[i];
				if (fillchar == c1)
					break;

				c1 = cvt.indexOf((char) c1);
				c = ((c << 6) & 0xc0) | c1;
				ret.append((char) c);
			}
		}
		return ret.toString();
	}

	private static final int fillchar = '=';

	private static final String cvt = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	public static String capitalizeFirstLetter(String s) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Character.toUpperCase(s.charAt(0)));
		sBuffer.append(s.substring(1, s.length()));
		return sBuffer.toString();
	}

	public static String lowercaseFirstLetter(String s) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Character.toLowerCase(s.charAt(0)));
		sBuffer.append(s.substring(1, s.length()));
		return sBuffer.toString();
	}

	public static String cutByteByU8(byte[] buf, int j) throws IOException {
		if (j > buf.length) {
			j = buf.length;
		}
		int count = 0;
		int i = 0;
		for (i = j - 1; i >= 0; i--) {
			if (buf[i] < 0)
				count++;
			else
				break;
		}
		String s = null;
		// 因為UTF-8三個字節表示一個漢字
		if (count % 3 == 0)
			s = new String(buf, 0, j, "utf-8");
		else if (count % 3 == 1)
			s = new String(buf, 0, j - 1, "utf-8");
		else if (count % 3 == 2)
			s = new String(buf, 0, j - 2, "utf-8");
		return s;
	}

	public static String cutString(String s, int len) {
		try {
			byte[] buf = s.getBytes("utf-8");
			return cutByteByU8(buf, len);
		} catch (Exception e) {
			return s.substring(len);
		}
	}

	public static int lengthString(String s) {
		try {
			byte[] buf = s.getBytes("utf-8");
			return buf.length;
		} catch (Exception e) {
			return s.length();
		}
	}

	public static void main(String args[]) {
		String ss = "";
		for (int i = 0; i < 100; i++) {
			ss += "好";
		}
		ss = ss == null ? "" : ss.trim();
		System.out.println(ss);
		if (UtilString.lengthString(ss) > 240) {
			String kk = UtilString.cutString(ss, 240);
			System.out.println(kk);
			System.out.println(kk.length());
		}
	}
}