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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

public class Demo1 {
    public static void main(String[] args) {

        String token = TokenUtil.getToken();
        System.out.println(token);
        String url = "http://localhost:80/seeyon/rest/flow/test";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost hpost = new HttpPost(url);
            HttpResponse hResponse = null;
            hpost.setHeader("Content-Type", "application/json;charset=utf-8");
            hpost.addHeader("token", token);

            Map<String, Object> data1 = new HashMap<String, Object>();
            data1.put("templateCode", "test");
            data1.put("senderLoginName", "admin02");
            data1.put("subject", "bpm集成测试122");
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("dz", "12");
//            dataMap.put("rq", LocalDate.now());
            dataMap.put("name", "2");
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> m = null;
            m = new HashMap<>();
            m.put("l1", "1");
            m.put("l2", "1");
            list.add(m);
            m = new HashMap<>();
            m.put("l1", "1111");
            m.put("l2", "2222");
            list.add(m);
            dataMap.put("sub", list);
            String json = com.alibaba.fastjson.JSONObject.toJSONString(dataMap);
            System.out.println(json);
            data1.put("data", json);
            data1.put("param", "0");
            data1.put("transfertype", "json");
            String requestParam = JSONObject.toJSONString(data1);

            StringEntity entity = new StringEntity(requestParam, "UTF-8");
            hpost.setEntity(entity);
            hResponse = client.execute(hpost);
            System.out.println("status:" + hResponse.getStatusLine().getStatusCode());
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
