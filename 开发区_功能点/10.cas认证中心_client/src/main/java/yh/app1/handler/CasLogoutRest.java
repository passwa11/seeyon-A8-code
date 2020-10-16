package yh.app1.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import yh.app1.filter.SSOClientFilter;
import yh.app1.util.TokenUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class CasLogoutRest {

    public static void toLogout(HttpServletResponse response, String serverUrl, String globalSessionId) {
        String url="";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(serverUrl + "/rest/casplus/logout");
        Map<String, Object> map = new HashMap<>();
        map.put("globalSessionId", globalSessionId);
        String pairs = JSON.toJSONString(map);
        StringEntity formEntity = new StringEntity(pairs, "UTF-8");
        post.setHeader("Content-Type", "application/json;charset=utf-8");
        //设置post请求头
        String tokenu = TokenUtil.getToken(serverUrl + "/rest/token");
        post.addHeader("token", tokenu);
        post.setEntity(formEntity);
        try {
            CloseableHttpResponse closeresponse = client.execute(post);
            closeresponse.setHeader("Cache-Control", "no-cache");
            String resultString = "";
            if (closeresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Cookie cookie = new Cookie("sso", "");
                cookie.setMaxAge(0);// 删除
                cookie.setPath("/");// 和创建时同一个作用域
                response.addCookie(cookie);
                resultString = EntityUtils.toString(closeresponse.getEntity(), "utf-8").replaceAll(" ", "");
                Map<String, Object> objectMap = (Map<String, Object>) JSONObject.parse(resultString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
