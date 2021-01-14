package com.seeyon.apps.ext.oauthLogin.filter;

import com.seeyon.apps.ext.oauthLogin.util.MapCacheUtil;
import com.seeyon.apps.ext.oauthLogin.util.PropUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OauthFilter implements Filter {

    private Log log = LogFactory.getLog(OauthFilter.class);

    private PropUtils propUtils = new PropUtils();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        User user = AppContext.getCurrentUser();
        if (null != user) {
            RestTemplate rest = new RestTemplate();
            MultiValueMap map = new LinkedMultiValueMap();
            String t = MapCacheUtil.cache.get(user.getLoginName());
            if (null != t && !"".equals(t)) {
                String m = request.getParameter("method");
                HttpPost httpPost = null;
                CloseableHttpResponse httpResponse = null;
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    //验证token 是否失效
//                    MultiValueMap pmap = new LinkedMultiValueMap();
//                    pmap.add("token", t);
//                    ResponseEntity<Object> result = rest.postForEntity(propUtils.getCheckToken(), pmap, Object.class);
                    List<NameValuePair> list = new ArrayList<NameValuePair>();
                    list.add(new BasicNameValuePair("token", t));
                    httpPost = new HttpPost(propUtils.getCheckToken());
                    httpPost.setEntity(new UrlEncodedFormEntity(list));
                    try {
                        //认证服务连接出现异常，直接方行，不走认证。
                        httpResponse = client.execute(httpPost);
                    } catch (IOException e) {
                        filterChain.doFilter(request, response);
                    }
                    int StatusCode = httpResponse.getStatusLine().getStatusCode();
                    if (StatusCode != HttpStatus.SC_OK) {//不等于200说明token过期了在服务端不存在了,所以调用接口报错
                        if (m.equals("logout") || m.equals("index")) {
                            filterChain.doFilter(request, response);
                            return;
                        } else {
                            String servername = request.getServerName();
                            response.sendRedirect("http://" + servername + "/seeyon/main.do?method=logout");
                        }
                    } else {
                        map.add(user.getLoginName(), MapCacheUtil.cache.get(user.getLoginName()));
                        String token = MapCacheUtil.cache.get(user.getLoginName());
//                        ResponseEntity<String> responseEntity = rest.getForEntity(propUtils.getCheckUserStatus() + "?access_token=" + token, String.class);
                        HttpGet httpGet = new HttpGet(propUtils.getCheckUserStatus() + "?access_token=" + token);
                        httpResponse = client.execute(httpGet);
                        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            String resultString = EntityUtils.toString(httpResponse.getEntity(), "utf-8").replaceAll(" ", "");
                            if ("true".equals(resultString)) {
                                filterChain.doFilter(request, response);
                            } else {
                                if (m.equals("logout") || m.equals("index")) {
                                    filterChain.doFilter(request, response);
                                    return;
                                } else {
                                    String servername = request.getServerName();
                                    response.sendRedirect("http://" + servername + "/seeyon/main.do?method=logout");
                                }
                            }
                        }

                    }
                } catch (RestClientException e) {
                    log.error("调用验证token 接口出错了：" + e.getMessage());
                } finally {
                    if(null != httpResponse){
                        httpResponse.close();
                    }
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
