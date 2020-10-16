package com.util;

import com.alibaba.fastjson.JSONObject;
import com.pojo.Pocket;
import com.ssl.MyX509TrustManager;
import com.ssl.TrustAnyHostnameVerifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 周刘成   2019/7/17
 */
public class PocketUtil {

//    https://api.kdzl.cn/cgi-bin/oauth/access_token?appid=281474976776194&did=4019295&secret=134c2FA58206427c

    private static final String appid = "281474976776194";
    private static final String did = "4019295";
    private static final String secret = "134c2FA58206427c";

    private static final String url = "https://211.103.127.211:6802/cgi-bin/oauth/access_token";

    private static final String Path = "https://211.103.127.211:6802/cgi-bin/oauth/access_token?appid=281474976776194&did=4019295&secret=134c2FA58206427c";

    public static String getJson(String reqUrl) {
        TrustManager[] tm = {new MyX509TrustManager()};
        SSLContext sslContext = null;
        String vhtml = "";
        try {
            sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL urlx = new URL(reqUrl);
            HttpsURLConnection uc = (HttpsURLConnection) urlx.openConnection();
            uc.setDoInput(true);
            uc.setUseCaches(false);
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            uc.setConnectTimeout(10 * 1000);
            uc.setReadTimeout(10 * 1000);
            uc.setSSLSocketFactory(ssf);
            uc.setHostnameVerifier(new TrustAnyHostnameVerifier());
            uc.setRequestProperty("Connection", "Keep-Alive");
            uc.setRequestProperty(
                    "Accept",
                    "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");
            uc.setRequestProperty("Accept-Language", "zh-cn");

            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36");

            uc.setRequestProperty("Cookie", "");


            System.out.println(uc.getResponseCode());

            String htmltype = "utf-8";

            try {
                java.io.InputStream inputstream = uc.getInputStream();
                BufferedReader bufferedreader = null;
                if (htmltype == null || htmltype.trim().equals("")) {
                    bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
                } else {
                    bufferedreader = new BufferedReader(new InputStreamReader(inputstream, htmltype));
                }
                String s1;
                while ((s1 = bufferedreader.readLine()) != null) {
                    vhtml = vhtml + s1;
                }

            } catch (Exception e) {
                // TODO: handle exception
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vhtml;

    }

    public static String getToken() {
        String t = getJson(Path);
        Pocket p = JSONObject.parseObject(t, Pocket.class);
        return p.getAccess_token();
    }

    public static String getDepartmentList(String url, String department_id, int fetch_child) {
        String token = getToken();
        String department = getJson(url.concat("?access_token=" + token +
                "&department_id=" + department_id + "&fetch_child=" + fetch_child));
        return department;
    }

    public static String getAllMember(String url, String departmentId, String fetchChild) {
//        https://api.kd77.cn/cgi-bin/roster/department/get_member?access_token=234578fe23d4&department_id=3241&fetch_child=0
        String token = getToken();
        String member = getJson(url.concat("?access_token=" + token +
                "&department_id=" + departmentId + "&fetch_child=" + fetchChild));
        return member;
    }


    public static String getAccessToken3() {
        OutputStreamWriter out = null;
        BufferedReader br = null;
        // 获取连接客户端工具
        StringBuilder sb = null;
        try {
            URL url = new URL(Path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            sb = new StringBuilder();
            while ((line = br.readLine()) != null) {// 循环读取流
                sb.append(line);
            }
            br.close();// 关闭流
            conn.disconnect();// 断开连接
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getAccessToken2() {
        String resultString = "";
        // 获取连接客户端工具
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.addParameter("appid", appid);
            uriBuilder.addParameter("did", did);
            uriBuilder.addParameter("secret", secret);


            HttpGet httpGet = new HttpGet(uriBuilder.build());
            // 浏览器表示
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.6)");
            // 传输的类型
            httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");

            // 执行请求
            response = httpClient.execute(httpGet);
            // 获得响应的实体对象
            HttpEntity entity = response.getEntity();
            // 使用Apache提供的工具类进行转换成字符串
            resultString = EntityUtils.toString(entity, "UTF-8");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }
}
