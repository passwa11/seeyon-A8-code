package yh.app1.servlet;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version 1.0
 * @Title:MainServlet
 * @Description:代表需要登录授权才能访问的页面
 */
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 3615122544373006252L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id=request.getParameter("id");
        String no=request.getParameter("VenderNO");
        String venderName=request.getParameter("venderName");
        String address=request.getParameter("address");
        String token = TokenUtil.getToken();
        System.out.println(token);
//        String template="gysmb001";
        String template="test001";
        String url = "http://localhost:81/seeyon/rest/flow/"+template+"/"+id;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost hpost = new HttpPost(url);
            HttpResponse hResponse = null;
            hpost.setHeader("Content-Type", "application/json;charset=utf-8");
            hpost.addHeader("token", token);

            Map<String, Object> data1 = new HashMap<String, Object>();
            data1.put("templateCode", "test001");
            data1.put("senderLoginName", "liyongzhi");
            data1.put("subject", no);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("VenderNO", no);
            dataMap.put("venderName", venderName);
            dataMap.put("address", address);
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
        request.getRequestDispatcher("/WEB-INF/jsp/main.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }


}