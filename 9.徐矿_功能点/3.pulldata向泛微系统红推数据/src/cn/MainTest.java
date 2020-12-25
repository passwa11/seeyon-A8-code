package cn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;


import cn.hutool.json.JSONUtil;

/**
 * Token认证测试
 *
 *  认证过程主要采用RSA非对称加密算法
 *
 * @author tzf 2020/6/9
 */
public class MainTest {
    /**
     * 模拟缓存服务
     */
    private static final Map<String,String> SYSTEM_CACHE = new HashMap <>();

    /**
     * ecology系统发放的授权许可证(appid)
     */
    private static final String APPID = "EEAA5436-7577-4BE0-8C6C-89E9D88805EA";

    public static void main(String[] args) {
    	
        Map<String, Object> data1 = new HashMap<String,Object>();
        
        
        String lcbt ="发文测试流程触发";
        data1.put("requestName",lcbt);
        data1.put("workflowId",24);
        Map<String, Object> wjbt = new HashMap<String,Object>();
        wjbt.put("fieldName", "wjbt");
        wjbt.put("fieldValue", "测试文件标题");
        Map<String, Object> rq = new HashMap<String,Object>();
        rq.put("fieldName", "rq");
        rq.put("fieldValue", "2020-12-22");
        Map<String, Object> lwdw1 = new HashMap<String,Object>();
        lwdw1.put("fieldName", "lwdw1");
        lwdw1.put("fieldValue", "徐矿集团");
        Map<String, Object> lwh = new HashMap<String,Object>();
        lwh.put("fieldName", "lwh");
        lwh.put("fieldValue", "测试【2020】");
        Map<String, Object> wjzw = new HashMap<String,Object>();
        //wjzw.put("fieldName", "wjzw");
        //Map<String, Object> data2 = new HashMap<String,Object>();
       // FileUtil fu = new FileUtil();
        
        
        //data2.put("filePath","F://徐矿//徐矿办92号.doc");
        //data2.put("fileName", "徐矿办92号.doc");
        List<Map<String, Object>> list1 = new ArrayList<Map<String,Object>>();
        //list1.add(data2);
        wjzw.put("fieldValue", list1);
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
        list.add(wjbt);
        list.add(rq);
        list.add(lwdw1);
        list.add(lwh);
        //list.add(wjzw);
        data1.put("mainData", list);
        String ssss =JSONUtil.toJsonStr(list);
        String JSONStr = "requestName=\"发文测试流程触发\"&workflowId=24&mainData="+ssss;
        System.out.println(ssss);
        testRestful("http://10.11.100.64:8888","/api/workflow/paService/doCreateRequest",JSONStr);
    }

    /**
     * 第一步：
     *
     * 调用ecology注册接口,根据appid进行注册,将返回服务端公钥和Secret信息
     */
    public static Map<String,Object> testRegist(String address){

        //获取当前系统RSA加密的公钥
        RSA rsa = new RSA();
        String publicKey = rsa.getPublicKeyBase64();
        String privateKey = rsa.getPrivateKeyBase64();

        // 客户端RSA私钥
        SYSTEM_CACHE.put("LOCAL_PRIVATE_KEY",privateKey);
        // 客户端RSA公钥
        SYSTEM_CACHE.put("LOCAL_PUBLIC_KEY",publicKey);

        //调用ECOLOGY系统接口进行注册
        String data = HttpRequest.post(address + "/api/ec/dev/auth/regist")
                .header("appid",APPID)
                .header("cpk",publicKey)
                .timeout(2000)
                .execute().body();

        // 打印ECOLOGY响应信息
        System.out.println("testRegist()："+data);
        Map<String,Object> datas = JSONUtil.parseObj(data);

        //ECOLOGY返回的系统公钥
        SYSTEM_CACHE.put("SERVER_PUBLIC_KEY",StrUtil.nullToEmpty((String)datas.get("spk")));
        //ECOLOGY返回的系统密钥
        SYSTEM_CACHE.put("SERVER_SECRET",StrUtil.nullToEmpty((String)datas.get("secrit")));
        return datas;
    }



    /**
     * 第二步：
     *
     * 通过第一步中注册系统返回信息进行获取token信息
     */
    public static Map<String,Object> testGetoken(String address){
        // 从系统缓存或者数据库中获取ECOLOGY系统公钥和Secret信息
        String secret = SYSTEM_CACHE.get("SERVER_SECRET");
        String spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");

        // 如果为空,说明还未进行注册,调用注册接口进行注册认证与数据更新
        if (Objects.isNull(secret)||Objects.isNull(spk)){
            testRegist(address);
            // 重新获取最新ECOLOGY系统公钥和Secret信息
            secret = SYSTEM_CACHE.get("SERVER_SECRET");
            spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");
        }

        // 公钥加密,所以RSA对象私钥为null
        RSA rsa = new RSA(null,spk);
        //对秘钥进行加密传输，防止篡改数据
        String encryptSecret = rsa.encryptBase64(secret,CharsetUtil.CHARSET_UTF_8,KeyType.PublicKey);

        //调用ECOLOGY系统接口进行注册
        String data = HttpRequest.post(address+ "/api/ec/dev/auth/applytoken")
                .header("appid",APPID)
                .header("secret",encryptSecret)
                .header("time","3600")
                .execute().body();

        System.out.println("testGetoken()："+data);
        Map<String,Object> datas = JSONUtil.parseObj(data);

        //ECOLOGY返回的token
        // TODO 为Token缓存设置过期时间
        SYSTEM_CACHE.put("SERVER_TOKEN",StrUtil.nullToEmpty((String)datas.get("token")));

        return datas;
    }

    /**
     * 第三步：
     *
     * 调用ecology系统的rest接口，请求头部带上token和用户标识认证信息
     *
     * @param address ecology系统地址
     * @param api rest api 接口地址(该测试代码仅支持GET请求)
     * @param jsonParams 请求参数json串
     *
     * 注意：ECOLOGY系统所有POST接口调用请求头请设置 "Content-Type","application/x-www-form-urlencoded; charset=utf-8"
     */
    public static String testRestful(String address,String api,String jsonParams){

        //ECOLOGY返回的token
        String token= SYSTEM_CACHE.get("SERVER_TOKEN");
        if (StrUtil.isEmpty(token)){
            token = (String) testGetoken(address).get("token");
        }

        String spk = SYSTEM_CACHE.get("SERVER_PUBLIC_KEY");
        //封装请求头参数
        RSA rsa = new RSA(null,spk);
        //对用户信息进行加密传输,暂仅支持传输OA用户ID
        String encryptUserid = rsa.encryptBase64("31",CharsetUtil.CHARSET_UTF_8,KeyType.PublicKey);

        //调用ECOLOGY系统接口
        String data = HttpRequest.post(address + api)
                .header("appid",APPID)
                .header("token",token)
                .header("userid",encryptUserid)
                .header("Content-Type","application/x-www-form-urlencoded; charset=utf-8")
                .body(jsonParams)
                .execute().body();
        System.out.println("testRestful()："+data);
        return data;
    }


}
