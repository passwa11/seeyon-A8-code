package com.seeyon.apps.trustdo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.apps.trustdo.model.NewBindResult;
import com.seeyon.apps.trustdo.model.XRDPhoneResult;
import com.seeyon.apps.trustdo.model.XRDScanResult;
import com.seeyon.apps.trustdo.model.XRDTokenResult;
import com.seeyon.apps.trustdo.model.sdk.AccountData;
import com.seeyon.apps.trustdo.model.sdk.Result;
import com.seeyon.apps.trustdo.model.sdk.SDKLoginEventData;

/**
 * 手机盾http请求工具类
 * @author zhaopeng
 *
 */
public class XRDHttpUtils {
	
	private static final Log LOGGER = CtpLogFactory.getLog(XRDHttpUtils.class);
	
	public static String CHARSET = "UTF-8";
  
    private static CloseableHttpClient getHttpClient() {  
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();  
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();  
        registryBuilder.register("http", plainSF);  
        try {  
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());  
            //信任任何链接  
            TrustStrategy trustStrategy = new TrustStrategy() {  
                @Override  
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {  
                    return true;  
                }  
            };  
            SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, trustStrategy).build();  
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
            registryBuilder.register("https", sslSF);  
        } catch (KeyStoreException e) {  
            throw new RuntimeException(e);  
        } catch (KeyManagementException e) {  
            throw new RuntimeException(e);  
        } catch (NoSuchAlgorithmException e) {  
            throw new RuntimeException(e);  
        }  
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();  
        //设置连接管理器  
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);  
        connManager.setMaxTotal(1000);// 整个连接池最大连接数  
        //构建客户端  
        return HttpClientBuilder.create().setConnectionManager(connManager).build();  
    }
	
	/**
	 * 请求手机盾二维码
	 * @param data
	 * @param url
	 * @return
	 */
	public static XRDPhoneResult connectMobileShieldServer(NameValuePair[] data , String url){
		XRDPhoneResult phoneResult = null;
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		// 设置参数编码为utf-8
		postMethod.getParams().setParameter(
				HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		// 把参数值放入postMethod中
		postMethod.setRequestBody(data);
		// 执行
		try {
			client.executeMethod(postMethod);
			// 打印结果页面
			String response1 = getHttpMethodResult(postMethod);
			phoneResult = JSONUtil.parseJSONString(response1, XRDPhoneResult.class);
		} catch (HttpException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return phoneResult;
		} catch (IOException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return phoneResult;
		} finally {
			postMethod.releaseConnection(); 
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return phoneResult;
	}
	
	public static Result<?> sdkConnectMobileShieldServer(NameValuePair[] data, String url,  Class<?> T){  
		CloseableHttpClient httpClient = null;  
        HttpPost httpPost = null;  
        Result<?> result = new Result<Object>(T.getClass());
        try{  
            httpClient = getHttpClient();  
            httpPost = new HttpPost(url);  
            List<org.apache.http.NameValuePair> list = new ArrayList<org.apache.http.NameValuePair>();
            if (data != null && data.length > 0){
                for (NameValuePair nameValuePair : data) {
                	list.add(new BasicNameValuePair(nameValuePair.getName(), nameValuePair.getValue()));
                }
            }
            if(list.size() > 0){  
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, XRDHttpUtils.CHARSET);  
                httpPost.setEntity(entity);  
            }  
            HttpResponse response = httpClient.execute(httpPost);  
            if(response != null){  
                HttpEntity resEntity = response.getEntity();  
                if(resEntity != null){  
                    result = JSONUtil.parseJSONString(EntityUtils.toString(resEntity, XRDHttpUtils.CHARSET), result.getClass());  
                }  
            }  
        } catch(Exception ex){  
        	LOGGER.error(ex);
        	//ex.printStackTrace();  
        } finally {
        	if (httpPost != null) {
        		httpPost.releaseConnection();  
        	}
        	if (httpClient != null) {
        		try {
					httpClient.close();
				} catch (IOException e) {
					LOGGER.error(e);
					//e.printStackTrace();
				}
        	}
        }
        return result;  
    }  
	
	/**
	 * 获取token
	 * @param url
	 * @param data
	 * @return
	 */
	public static XRDTokenResult getToken(String url,NameValuePair[] data){
		XRDTokenResult token = null;
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		// 设置参数编码为utf-8
		postMethod.getParams().setParameter(
				HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		// 把参数值放入postMethod中
		postMethod.setRequestBody(data);
		// 执行
		try {
			client.executeMethod(postMethod);
			// 打印结果页面
			String response1 = getHttpMethodResult(postMethod);
			token = JSONUtil.parseJSONString(response1,XRDTokenResult.class);
		} catch (HttpException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return token;
		} catch (IOException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return token;
		} finally {
			postMethod.releaseConnection();  
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return token;
	}
	
	/**
	 * 请求手机盾通用接口
	 * @param data
	 * @param url
	 * @return
	 */
	public static XRDScanResult repMobileShieldServer(NameValuePair[] data , String url){
		XRDScanResult scanResult = null;
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		// 设置参数编码为utf-8
		postMethod.getParams().setParameter(
				HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		// 把参数值放入postMethod中
		postMethod.setRequestBody(data);
		// 执行
		try {
			client.executeMethod(postMethod);
			// 打印结果页面
			String response1 = getHttpMethodResult(postMethod);
			scanResult = JSONUtil.parseJSONString(response1, XRDScanResult.class);
		} catch (HttpException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return scanResult;
		} catch (IOException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return scanResult;
		} finally {
			postMethod.releaseConnection();  
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return scanResult;
	}
	
	/**
	 * 请求手机盾通用接口
	 * @param data
	 * @param url
	 * @return
	 */
	public static XRDPhoneResult repMobileShieldServer2(NameValuePair[] data , String url){
		XRDPhoneResult phoneResult = null;
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		// 设置参数编码为utf-8
		postMethod.getParams().setParameter(
				HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		// 把参数值放入postMethod中
		postMethod.setRequestBody(data);
		// 执行
		try {
			client.executeMethod(postMethod);
			// 打印结果页面
			String response1 = getHttpMethodResult(postMethod);
            phoneResult = JSONUtil.parseJSONString(response1, XRDPhoneResult.class);
		} catch (HttpException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return phoneResult;
		} catch (IOException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return phoneResult;
		} finally {
			postMethod.releaseConnection();  
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return phoneResult;
	}
	
	public static NewBindResult repMobileShieldServer3(NameValuePair[] data , String url){
		NewBindResult bindResult = null;
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		// 设置参数编码为utf-8
		postMethod.getParams().setParameter(
				HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		// 把参数值放入postMethod中
		postMethod.setRequestBody(data);
		// 执行
		try {
			client.executeMethod(postMethod);
			// 打印结果页面
			String response1 = getHttpMethodResult(postMethod);
            bindResult = JSONUtil.parseJSONString(response1, NewBindResult.class);
		} catch (HttpException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return bindResult;
		} catch (IOException e) {
			LOGGER.error(e);
			//e.printStackTrace();
			return bindResult;
		} finally {
			postMethod.releaseConnection();  
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		return bindResult;
	}
	/**
	 * 二维码接口返回参数拼接
	 * @param code
	 * @param url
	 * @param eventId
	 * @return
	 */
	public static String resultData(int code ,String msg ,String url, String eventId,String resetEvent){
		String result = "";
		if (200 == code) {
			result = ajaxReturn(String.valueOf(code),msg,url,eventId,resetEvent);
		}else {
			result = ajaxReturn(String.valueOf(code),msg,"","","");
		}
		return result;
	}
	
	public static String pollDate(int code ,String msg, String signValue, String imageDate,String appUid){
		String result = "";
		if (200 == code) {
			result = pollReturn(String.valueOf(code),msg,signValue,imageDate,appUid);
		}else {
			result = pollReturn(String.valueOf(code),msg,signValue,imageDate,appUid);
		}
		return result;
	}
	
	private static String getHttpMethodResult(PostMethod postMethod) throws IOException {
		InputStream inputStream = postMethod.getResponseBodyAsStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuffer = new StringBuilder();
		String str = "";
		while ((str = br.readLine()) != null) {
			stringBuffer.append(str);
		}
		return stringBuffer.toString();
	}
	
	/**
	 * 标准结构返回体
	 * @param success
	 * @param message
	 * @return
	 */
	public static String ajaxReturn(String code,String msg,String data,String eventId,String resetEvent){
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("code", code);
		map.put("msg", msg);
		map.put("data", data);
		map.put("eventId", eventId);
		map.put("resetEvent", resetEvent);
		return JSONUtil.toJSONString(map);		
	}
	public static String pollReturn(String code,String msg ,String signValue,String imageDate,String account){
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("code", code);
		map.put("msg", msg);
		map.put("signValue", signValue);
		map.put("imageDate", imageDate);
		map.put("account", account);
		return JSONUtil.toJSONString(map);	
	}
	public static String pollLogin(){
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("isLogin", true);
		return JSONUtil.toJSONString(map);	
	}
	
	public static void trans(String str, Class<?> T){
		try {
			Result result = new Result(T.getClass());
			result = JSONUtil.parseJSONString(str, result.getClass());
			LOGGER.debug(result);
		} catch (Exception e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
	}
}
