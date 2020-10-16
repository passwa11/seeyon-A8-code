package com.ncbi.medhub.http;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HttpApiService {

    private static CloseableHttpClient httpClient;
    @Autowired
    private RequestConfig config;

    static {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authtyep) -> true).build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, null, null, new NoopHostnameVerifier());
            httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 不带参数的get请求，如果状态码为200，则返回body，如果不为200，则返回null
     *
     * @param url
     * @throws Exception
     **/
    public String doHttpsGet(String url) throws Exception {
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        try {
            httpGet = new HttpGet(url);
            httpGet.setConfig(config);
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 带参数的get请求，如果状态码为200，则返回body，如果不为200，则返回null
     *
     * @param url
     * @throws Exception
     **/
    public String doHttpsGet(String url, Map<String, Object> map) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (null != map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        return this.doHttpsGet(uriBuilder.build().toString());
    }

    /**
     * 带参数的post请求
     *
     * @param url @param map @return @throws Exception */
    public String doPost(String url, Map<String, Object> map) throws Exception {
        // 声明httpPost请求
        HttpPost httpPost = new HttpPost(url);
        // 加入配置信息
        httpPost.setConfig(config);

        // 判断map是否为空，不为空则进行遍历，封装from表单对象
        if (map != null) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            // 构造from表单对象
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, "UTF-8");

            // 把表单放到post里
            httpPost.setEntity(urlEncodedFormEntity);
        }
        // 发起请求
        CloseableHttpResponse response = this.httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
        return null;
    }

    /**
     * 不带参数post请求
     *
     * @param url
     * @return
     * @throws Exception
     */
    public String doPost(String url) throws Exception {
        return this.doPost(url, null);
    }


}
