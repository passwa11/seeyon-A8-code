package com.seeyon.apps.trustdo.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientInit {

	private static PoolingHttpClientConnectionManager cm;
    private static RequestConfig requestConfig;

    /** 初始化httpClient连接池 */
    private static void init() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(1000);
            cm.setDefaultMaxPerRoute(500);
        }
        if (requestConfig == null) {
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(30000)
                    .setConnectTimeout(30000).setSocketTimeout(30000).build();
        }
    }

    /** 通过连接池获取HttpClient对象 */
    public static CloseableHttpClient getHttpClient() {
        init();
        return HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
    }
}
