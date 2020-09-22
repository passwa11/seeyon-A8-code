package com.seeyon.apps.ext.kypending.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.PropertiesUtil;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主要用于调用 http接口 https接口
 *
 */
public class HttpUtilTool {
    private static HttpUtilTool httpUtilTool;

    public static HttpUtilTool getInstance() {
        if (null == httpUtilTool) {
            httpUtilTool = new HttpUtilTool();
        }
        return httpUtilTool;
    }

//    public static String doHttpsPostByPostMedthod(){
//        HttpClient httpclient = HttpClients.createDefault();
//
//        try {
//            String result = "";
//            PostMethod post = new PostMethod(PropertiesUtil.getUrl());
//            post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "gbk");
//            post.addParameter("SpCode", PropertiesUtil.getSpCode());
//            post.addParameter("LoginName", PropertiesUtil.getLoginName());
//            post.addParameter("Password", PropertiesUtil.getPassword());
//            post.addParameter("MessageContent", String.format(PropertiesUtil.getTemplate(), content));
////            post.addParameter("MessageContent", String.format(PropertiesUtil.getTemplate(), ((int)((Math.random()*9+1)*100000))+""));
//            post.addParameter("UserNumber", destPhone);
//            post.addParameter("SerialNumber", "");
//            post.addParameter("ScheduleTime", "");
//            post.addParameter("ExtendAccessNum", "");
//            post.addParameter("f", PropertiesUtil.getF());
//            httpclient.execute(post);
//            result = new String(post.getResponseBody(), "gbk");
//            if (null != result) {
//                String[] arr = result.split("&");
//                String code = arr[0].split("=")[1];
//                if (code.equals("0")) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
//    }


    public String toHttpsGet(String url, Map<String, String> headers) {
        HttpGet httpGet = null;
        HttpResponse httpResponse = null;
        String result = null;
        try (CloseableHttpClient closeableHttpClient = this.createHttpClient()) {
            httpGet = new HttpGet(url);
            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpResponse = closeableHttpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String toHttpsDelete(String url, Map<String, String> headers) {
        String encode = "UTF-8";
        HttpDelete httpDelete = null;
        HttpResponse httpResponse = null;
        String result = null;
        try (CloseableHttpClient closeableHttpClient = createHttpClient()) {
            httpDelete = new HttpDelete(url);
            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpDelete.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpResponse = closeableHttpClient.execute(httpDelete);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity, encode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String toHttpsPut(String url, String json, Map<String, String> headers) {
        String result = "";
        String encode = "UTF-8";
        try (CloseableHttpClient closeableHttpClient = createHttpClient()) {
            HttpPut httpPut = new HttpPut(url);

            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPut.setHeader(entry.getKey(), entry.getValue());
                }
            }
            StringEntity stringEntity = new StringEntity(json, encode);
            stringEntity.setContentType("application/json");
            httpPut.setEntity(stringEntity);
            try (CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPut)) {
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == 500) {
                    System.out.println("error");
                }
                if (code == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(httpResponse.getEntity(), encode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String toHttpsPost(String url, String json, Map<String, String> headers) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        String encode = "UTF-8";
        String result = null;
        try (CloseableHttpClient closeableHttpClient = createHttpClient();) {
            HttpPost httpPost = new HttpPost(url);

            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            StringEntity stringEntity = new StringEntity(json, "UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            try (CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPost)) {
                int code = httpResponse.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(httpResponse.getEntity(), encode);
                }
            }
        }
        return result;
    }

    //避免需要证书
    private CloseableHttpClient createHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authtyep) -> true).build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, null, null, new NoopHostnameVerifier());
        return HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    }

    public JSONObject toDelete(String url, Map<String, String> headers) {
        String encode = "UTF-8";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = null;
        HttpResponse httpResponse = null;
        JSONObject jsonObject = null;
        try {
            httpDelete = new HttpDelete(url);
            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpDelete.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpResponse = httpClient.execute(httpDelete);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String result = EntityUtils.toString(httpEntity, encode);
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public JSONObject toPut(String url, String jsonStr, Map<String, String> headers) throws IOException {
        String encode = "UTF-8";
        try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
            HttpPut httpPut = null;
            HttpResponse httpResponse = null;
            JSONObject jsonObject = null;
            try {
                httpPut = new HttpPut(url);
                httpPut.setHeader("Content-type", "application/json");
                if (null != headers && headers.size() > 0) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        httpPut.setHeader(entry.getKey(), entry.getValue());
                    }
                }
                StringEntity stringEntity = new StringEntity(jsonStr, encode);
                httpPut.setEntity(stringEntity);
                httpResponse = httpClient.execute(httpPut);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    String result = EntityUtils.toString(httpEntity, encode);
                    jsonObject = JSONObject.parseObject(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return jsonObject;
        }
    }


    public JSONObject toPost(String url, Map<String, Object> map, Map<String, String> headers, String encode) {
        if (null == encode) {
            encode = "UTF-8";
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse httpResponse = null;
        JSONObject jsonObject = null;
        try {
            httpPost = new HttpPost(url);
            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            if (null != map) {
                List<NameValuePair> pairList = new ArrayList<>();
                for (String key : map.keySet()) {
                    pairList.add(new BasicNameValuePair(key, (String) map.get(key)));
                }
                UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(pairList, encode);
                httpPost.setEntity(encodedFormEntity);
            }
            httpResponse = httpClient.execute(httpPost);
            httpResponse.setHeader("Cache-Control", "no-cache");
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), encode);
                jsonObject = JSONObject.parseObject(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public JSONObject toGet(String url, Map<String, String> headers, String encode) {
        if (null == encode) {
            encode = "UTF-8";
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = null;
        HttpResponse httpResponse = null;
        JSONObject jsonObject = null;
        try {
            httpGet = new HttpGet(url);
            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String result = EntityUtils.toString(httpEntity, encode);
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public JSONObject toPostByJsonString(String url, String jsonStr, Map<String, String> headers) {
        String encode = "UTF-8";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse httpResponse = null;
        JSONObject jsonObject = null;
        try {
            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");
            if (null != headers && headers.size() > 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            StringEntity stringEntity = new StringEntity(jsonStr, encode);
            httpPost.setEntity(stringEntity);
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String result = EntityUtils.toString(httpEntity, encode);
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }


}
