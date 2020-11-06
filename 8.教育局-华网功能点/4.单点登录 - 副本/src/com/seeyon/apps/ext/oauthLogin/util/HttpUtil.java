package com.seeyon.apps.ext.oauthLogin.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HttpUtil {
	public static String sendGet(String url) throws Exception {
        return send(url, "GET", null, null);
    }

    public static String sendPost(String url, String param) throws Exception {
        return send(url, "POST", param, null);
    }

    public static String send(String url, String method, String param, Map<String, String> headers) throws Exception {
        String result = null;
        HttpURLConnection conn = getConnection(url, method, param, headers);
        String charset = conn.getHeaderField("Content-Type");
        charset = detectCharset(charset);
        InputStream input = getInputStream(conn);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count;
        byte[] buffer = new byte[4096];
        while ((count = input.read(buffer, 0, buffer.length)) > 0) {
            output.write(buffer, 0, count);
        }
        input.close();
        // 若已通过请求头得到charset，则不需要去html里面继续查找
        if (charset == null || charset.equals("")) {
            charset = detectCharset(output.toString());
            // 若在html里面还是未找到charset，则设置默认编码为utf-8
            if (charset == null || charset.equals("")) {
                charset = "utf-8";
            }
        }
        
        result = output.toString(charset);
        output.close();

        // result = output.toString(charset);
        // BufferedReader bufferReader = new BufferedReader(new
        // InputStreamReader(input, charset));
        // String line;
        // while ((line = bufferReader.readLine()) != null) {
        // if (result == null)
        // bufferReader.mark(1);
        // result += line;
        // }
        // bufferReader.close();

        return result;
    }

    private static String detectCharset(String input) {
        Pattern pattern = Pattern.compile("charset=\"?([\\w\\d-]+)\"?;?", Pattern.CASE_INSENSITIVE);
        if (input != null && !input.equals("")) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private static InputStream getInputStream(HttpURLConnection conn) throws Exception {
        String ContentEncoding = conn.getHeaderField("Content-Encoding");
        if (ContentEncoding != null) {
            ContentEncoding = ContentEncoding.toLowerCase();
            if (ContentEncoding.indexOf("gzip") != 1)
                return new GZIPInputStream(conn.getInputStream());
            else if (ContentEncoding.indexOf("deflate") != 1)
                return new DeflaterInputStream(conn.getInputStream());
        }

        return conn.getInputStream();
    }
    
   
    public static String http(String url, Map<String, String> params) {
		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (Entry<String, String> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
			}
			sb.substring(0, sb.length() - 1);
		}
//		System.out.println("send_url:" + url);
//		System.out.println("send_data:" + sb.toString());
		// 尝试发送请求
		try {
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			//// POST 只能为大写，严格限制，post会不识别
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			osw.write(sb.toString());
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		// 读取返回内容
		StringBuffer buffer = new StringBuffer();
		try {
			//一定要有返回值，否则无法把请求发送给server端。
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}
    
    public static String GetServiceHttp(String url, Map<String, String> params) {
		URL u = null;
		HttpURLConnection con = null;
		// 构建请求参数
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (Entry<String, String> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
			}
			sb.substring(0, sb.length() - 1);
		}
//		System.out.println("send_url:" + url);
//		System.out.println("send_data:" + sb.toString());
		// 尝试发送请求
		try {
			url = url + "?" + sb.toString();
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			//// POST 只能为大写，严格限制，post会不识别
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			osw.write(sb.toString());
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		// 读取返回内容
		StringBuffer buffer = new StringBuffer();
		try {
			//一定要有返回值，否则无法把请求发送给server端。
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}

    static HttpURLConnection getConnection(String url, String method, String param, Map<String, String> header) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setRequestMethod(method);

        // 设置通用的请求属性
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
        conn.setRequestProperty("Accept-Encoding", "gzip,deflate");

        String ContentEncoding = null;
        if (header != null) {
            for (Entry<String, String> entry : header.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("Content-Encoding"))
                    ContentEncoding = entry.getValue();
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (method == "POST") {
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (param != null && !param.equals("")) {
                OutputStream output = conn.getOutputStream();
                if (ContentEncoding != null) {
                    if (ContentEncoding.indexOf("gzip") > 0) {
                        output=new GZIPOutputStream(output);
                    }
                    else if(ContentEncoding.indexOf("deflate") > 0) {
                        output=new DeflaterOutputStream(output);
                    }
                }
                output.write(param.getBytes());
            }
        }
        // 建立实际的连接
        conn.connect();
        return conn;
    }
}

