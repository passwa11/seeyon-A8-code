package com.seeyon.apps.ext.copyFile.util;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * 附件导出类
 */
public class OAFileUtil {

    private static String getTokenUrl = ConfigProperties.getServerIp().concat("/seeyon/rest/token");
    // 导出服务地址
    private static String downloadServiceUrl = ConfigProperties.getServerIp().concat("/seeyon/rest/edoc/export");
    // 请求token的账号
    private static String restName = ConfigProperties.getRestName();

    private static String restPassword = ConfigProperties.getRestPassword();


    /**
     * 从OA系统下载附件
     *
     * @param fileId   文件的id
     * @param destPath 本地磁盘的路径 eg."d:/账号密码.txt"
     * @return true / false
     */
//    public static boolean fileDownload(String fileId, String destPath, String loginName) {
//
////         String token = getTokenByHttp(loginName);
//        String token = getTokenV6(loginName);
//        System.out.println("token = " + token);
//
//        return exportFile(token, fileId, destPath);
//
//    }

    /**
     * 把系统的文件或附件以二进制流的形式输出到本地。
     */
    private static boolean exportFile(String token, String fileId, String destPath) {

        StringBuffer parameters = new StringBuffer();
        parameters.append("fileId=" + fileId);
        parameters.append("&token=" + token);
        URL preUrl = null;
        URLConnection uc = null;
        try {
            preUrl = new URL(downloadServiceUrl);
            // preUrl = new
            // URL("http://127.0.0.1:8012/seeyon/services/downloadService");
            String s = parameters.toString();
            uc = preUrl.openConnection();
            uc.setDoOutput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            HttpURLConnection hc = (HttpURLConnection) uc;
            hc.setRequestMethod("POST");
            OutputStream os = hc.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeBytes(s);
            dos.flush();
            dos.close();
            FileOutputStream file = new FileOutputStream(destPath);
            InputStream is = hc.getInputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                file.write(ch);
            }
            if (is != null)
                is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public static String getTokenV6(String loginName) {
        String url = ConfigProperties.getServerIp();// OA的登录URL
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(url);
        CTPRestClient client = clientManager.getRestClient();
        client.authenticate(ConfigProperties.getRestName(), ConfigProperties.getRestPassword());// 由致远OA提供
        // 生成token的代码
        String s = client.get(("/token/").concat(ConfigProperties.getRestName()+"/").concat(ConfigProperties.getRestPassword())+"?loginName=" + loginName, String.class);
        com.alibaba.fastjson.JSONObject jsons = com.alibaba.fastjson.JSONObject.parseObject(s);
        String token = (String) jsons.get("id");

        System.out.println("token : " + token);

        return token;
    }

    /**
     * 通过原生的HttpClient获取token
     *
     * @return token
     */
    private static String getTokenByHttp(String loginName) {
        String token = "";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(getTokenUrl);
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("userName", restName);
            jsonParam.put("password", restPassword);
            jsonParam.put("loginName", loginName);
            StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");// 解决中文乱码问题
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            String respContent = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject json = new JSONObject(respContent);
            token = json.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
}
