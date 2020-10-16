package cn.com.test;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

public class Demo1 {
    public static void main(String[] args) {

        String token = TokenUtil.getToken();
        System.out.println(token);
        String url = "http://localhost:80/seeyon/rest/flow/bpm0916";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost hpost = new HttpPost(url);
            HttpResponse hResponse = null;
            hpost.setHeader("Content-Type", "application/json;charset=utf-8");
            hpost.addHeader("token", token);

            Map<String, Object> data1 = new HashMap<String, Object>();
            data1.put("templateCode", "bpm0916");
            data1.put("senderLoginName", "yanyi");
            data1.put("subject", "bpm集成测试六六");
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("name", "11111111");
            dataMap.put("address", "2222222");
            data1.put("data", com.alibaba.fastjson.JSONObject.toJSONString(dataMap));
            data1.put("param", "0");
            data1.put("transfertype", "json");
            String requestParam = JSONObject.toJSONString(data1);

            StringEntity entity = new StringEntity(requestParam, "UTF-8");
            hpost.setEntity(entity);
            hResponse = client.execute(hpost);
            if (hResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = hResponse.getEntity();
                String result = EntityUtils.toString(httpEntity, "UTF-8");
                System.out.println(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
