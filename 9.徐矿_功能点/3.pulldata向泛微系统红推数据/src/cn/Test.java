package cn;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        String token = "";
//        String url = "http://127.0.0.1:80/seeyon/rest/token";
        String url = "http://10.11.100.63:8888/seeyon/rest/token";
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
        System.out.println(token);
    }
}
