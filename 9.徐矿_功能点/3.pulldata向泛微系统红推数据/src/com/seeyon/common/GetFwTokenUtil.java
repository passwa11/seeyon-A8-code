package com.seeyon.common;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.common.collect.HppcMaps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetFwTokenUtil {
    /**
     * 模拟缓存服务
     */
    public static Map<String, String> SYSTEM_CACHE = new HashMap<>();

    /**
     * ecology系统发放的授权许可证(appid)
     */
//    private static final String APPID = "EEAA5436-7577-4BE0-8C6C-89E9D88805EA";
    public static void main(String[] args) {
        String address = "http://10.11.100.64:8888";
//        String token = testGetoken(address);
//        System.out.println(token);
    }

    public static String getOaToken() {
        ProptiesUtil prop = new ProptiesUtil();
        String token = "";
//        String url = "http://127.0.0.1:80/seeyon/rest/token";
        String url = prop.getOaUrl() + "/seeyon/rest/token";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = null;
        HttpResponse response = null;
        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
//        String requestParams = "{\"userName\":\"rest\",\"password\":\"rest111111\"}";
        String requestParams = "{\"userName\":\"qsbg\",\"password\":\"xkjt2020\",\"loginName\":\"admin02\"}";
        StringEntity postingString = new StringEntity(requestParams, "utf-8");
        httpPost.setEntity(postingString);
        try {
            response = client.execute(httpPost);
            response.setHeader("Cache-Control", "no-cache");
            System.out.println(response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String resultString = EntityUtils.toString(response.getEntity(), "utf-8").replaceAll(" ", "");
                JSONObject jsonObject = JSON.parseObject(resultString);
                token = (String) jsonObject.get("id");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }


    /**
     * 第一步：
     * <p>
     * 调用ecology注册接口,根据appid进行注册,将返回服务端公钥和Secret信息
     */
    public static Map<String, Object> testRegist(String address) {

        //获取当前系统RSA加密的公钥
        RSA rsa = new RSA();
        String publicKey = rsa.getPublicKeyBase64();
        String privateKey = rsa.getPrivateKeyBase64();

        // 客户端RSA私钥
//        SYSTEM_CACHE.put("LOCAL_PRIVATE_KEY", privateKey);
        // 客户端RSA公钥
//        SYSTEM_CACHE.put("LOCAL_PUBLIC_KEY", publicKey);

        //调用ECOLOGY系统接口进行注册
        String data = HttpRequest.post(address + "/api/ec/dev/auth/regist")
                .header("appid", new ProptiesUtil().getProperties().getProperty("fw.appid"))
                .header("cpk", publicKey)
                .timeout(2000)
                .execute().body();

        // 打印ECOLOGY响应信息
//        System.out.println("testRegist()：" + data);
        Map<String, Object> datas = JSONUtil.parseObj(data);

        //ECOLOGY返回的系统公钥
//        SYSTEM_CACHE.put("SERVER_PUBLIC_KEY", StrUtil.nullToEmpty((String) datas.get("spk")));
        //ECOLOGY返回的系统密钥
//        SYSTEM_CACHE.put("SERVER_SECRET", StrUtil.nullToEmpty((String) datas.get("secrit")));
        return datas;
    }


    /**
     * 第二步：
     * <p>
     * 通过第一步中注册系统返回信息进行获取token信息
     */
    public static String testGetoken(Map<String,Object> mp) {
        // 从系统缓存或者数据库中获取ECOLOGY系统公钥和Secret信息
//        String secret = SYSTEM_CACHE.get("SERVER_SECRET");
//        String spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");

        // 如果为空,说明还未进行注册,调用注册接口进行注册认证与数据更新
//        if (Objects.isNull(secret) || Objects.isNull(spk)) {
//        Map<String, Object> mp = testRegist(address);
        // 重新获取最新ECOLOGY系统公钥和Secret信息
//            secret = SYSTEM_CACHE.get("SERVER_SECRET");
//            spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");
//        }
        String spk = StrUtil.nullToEmpty((String) mp.get("spk"));
        String secret = StrUtil.nullToEmpty((String) mp.get("secrit"));
        // 公钥加密,所以RSA对象私钥为null
        RSA rsa = new RSA(null, spk);
        //对秘钥进行加密传输，防止篡改数据
        String encryptSecret = rsa.encryptBase64(secret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);

        ProptiesUtil proptiesUtil = new ProptiesUtil();
        //调用ECOLOGY系统接口进行注册
        String data = HttpRequest.post(proptiesUtil.getServerUrl() + "/api/ec/dev/auth/applytoken")
                .header("appid", proptiesUtil.getAppId())
                .header("secret", encryptSecret)
                .header("time", "3600")
                .execute().body();

        Map<String, Object> datas = JSONUtil.parseObj(data);

        //ECOLOGY返回的token
        // TODO 为Token缓存设置过期时间
//        SYSTEM_CACHE.put("SERVER_TOKEN", StrUtil.nullToEmpty((String) datas.get("token")));

        return StrUtil.nullToEmpty((String) datas.get("token"));
    }

}
