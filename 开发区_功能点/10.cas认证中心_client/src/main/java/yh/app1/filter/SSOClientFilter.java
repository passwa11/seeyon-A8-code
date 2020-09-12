package yh.app1.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import yh.app1.util.StringUtil;
import yh.app1.util.TokenUtil;

/**
 * @version 1.0
 * @Title:SSOClientFilter SSO客户端过滤器
 * @Description:应用服务器必须添加的过滤器
 */
public class SSOClientFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String loginPage;
    private String validateTicketUrl;
    private String localExitUrl;
    private String needLoginUrls;// 无需登录拦截的url，使用逗号分隔
    private String restToken;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        String ticket = request.getParameter("ticket");
        String globalSessionId = request.getParameter("globalSessionId");
        String url = request.getRequestURL().toString();

        String[] needLoginAry = needLoginUrls.split(",");

        // 除了包含needLoginAry的请求，其他的都不拦截
        if (needLoginAry != null && needLoginAry.length > 0) {
            boolean contains = false;
            for (String str : needLoginAry) {
                if (url.contains(str)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                logger.info("{}不包含隐私内容，无需SSO拦截", url);
                filterChain.doFilter(request, response);
                return;
            }
        }
        // 登录拦截

        if (StringUtil.isEmpty(username)) {// 本地未登录
            // 中心已经登录
            if (StringUtil.isUnEmpty(ticket)) {
                if (StringUtil.isUnEmpty(globalSessionId)) {
//                    zhou
                    String tokenu = TokenUtil.getToken(restToken);
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost post = new HttpPost(validateTicketUrl);
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("ticket", ticket);
//                    map.put("globalSessionId", globalSessionId);
                    String basePath = request.getScheme() + "://" + request.getServerName() + ":"
                            + request.getServerPort() + request.getContextPath() + "/";
//                    map.put("localLoginOutUrl", basePath + localExitUrl);
//                    map.put("localSessionId", session.getId());
//                    String pairs = JSON.toJSONString(map);
//                    StringEntity formEntity = new StringEntity(pairs, "UTF-8");
                    List<NameValuePair> pairList=new ArrayList<>();
                    pairList.add(new BasicNameValuePair("ticket", ticket));
                    pairList.add(new BasicNameValuePair("globalSessionId", globalSessionId));
                    pairList.add(new BasicNameValuePair("localLoginOutUrl", basePath + localExitUrl));
                    pairList.add(new BasicNameValuePair("localSessionId", session.getId()));
                    UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairList, "UTF-8");

//                    post.setHeader("Content-Type", "application/json;charset=utf-8");
                    post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                    //设置post请求头

                    post.addHeader("token", tokenu);
                    post.setEntity(formEntity);
                    CloseableHttpResponse closeresponse=null;
                    try {
                        closeresponse = client.execute(post);
                        closeresponse.setHeader("Cache-Control", "no-cache");
                        String resultString = "";
                        if (closeresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            resultString = EntityUtils.toString(closeresponse.getEntity(), "utf-8").replaceAll(" ", "");
                            Map<String, Object> objectMap = (Map<String, Object>) JSONObject.parse(resultString);
                            if (Integer.parseInt(objectMap.get("code").toString()) == 0) {
                                username = (String) objectMap.get("account");
                                session.setAttribute("username", username);
                                session.setAttribute("globalSessionId", globalSessionId);// 等退出全局登录时使用
//							zhou start
                                CasHttpServletRequestWrapper casRequest = new CasHttpServletRequestWrapper(request, username);
                                filterChain.doFilter(casRequest, response);
//							zhou end
                                logger.info("验票成功");
                                return;
                            }else {
                                logger.info("验票失败，重新跳转到sso登录页面");
                                response.sendRedirect(loginPage + "?service=" + url);
                            }

                        } else {
                            logger.info("验票失败，重新跳转到sso登录页面");
                            response.sendRedirect(loginPage + "?service=" + url);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        try{
                            if(null != closeresponse){
                                closeresponse.close();
                            }
                            if(null !=client){
                                client.close();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                } else {
                    logger.info("缺少必须的globalSessionId");
                    throw new RuntimeException("ticket不为空时，globalSessionId不能为空");
                }

            } else {
                logger.info("ticket为空！跳转到认证中心登录页");
                response.sendRedirect(loginPage + "?service=" + url);
            }
        } else {// 已经登录
            logger.info("已经登录，无需拦截,进入系统:" + request.getContextPath());
            filterChain.doFilter(request, response);
            return;
        }

    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        String ssoServer = servletContext.getInitParameter("ssoServerUrl");
        localExitUrl = servletContext.getInitParameter("localExitUrl");
        needLoginUrls = servletContext.getInitParameter("needLoginUrls");

//        loginPage = ssoServer + "/auth/toLogin";
        loginPage = ssoServer + "/main.do";
//        validateTicketUrl = ssoServer + "/ticket/verify";
        validateTicketUrl = ssoServer + "/rest/casplus/verify";
        restToken = ssoServer + "/rest/token";
    }

    final class CasHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private String username;

        public CasHttpServletRequestWrapper(HttpServletRequest request, String name) {
            super(request);
//            Cookie[] cookies=request.getCookies();
//            for(Cookie cookie:cookies){
//                String cookieName =cookie.getName();
//                if("sso".equals(cookieName)){
//                    this.username = cookie.getValue();
//                }
//            }
            this.username = name;
        }

        @Override
        public String getRemoteUser() {
            return getUsername();
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
//            throws IOException, ServletException {
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        HttpSession session = request.getSession();
//        String username = (String) session.getAttribute("username");
//        String ticket = request.getParameter("ticket");
//        String globalSessionId = request.getParameter("globalSessionId");
//        String url = request.getRequestURL().toString();
//
//        String[] needLoginAry = needLoginUrls.split(",");
//
//        // 除了包含needLoginAry的请求，其他的都不拦截
//        if (needLoginAry != null && needLoginAry.length > 0) {
//            boolean contains = false;
//            for (String str : needLoginAry) {
//                if (url.contains(str)) {
//                    contains = true;
//                    break;
//                }
//            }
//            if (!contains) {
//                logger.info("{}不包含隐私内容，无需SSO拦截", url);
//                filterChain.doFilter(request, response);
//                return;
//            }
//        }
//        // 登录拦截
//        if (StringUtil.isEmpty(username)) {// 本地未登录
//            // 中心已经登录
//            if (StringUtil.isUnEmpty(ticket)) {
//                if (StringUtil.isUnEmpty(globalSessionId)) {
//                    // 令牌验证
//                    // 发送请求参数
//                    PostMethod postMethod = new PostMethod(validateTicketUrl);
//                    postMethod.addParameter("ticket", ticket);
//                    postMethod.addParameter("globalSessionId", globalSessionId);//服务端需要它找到全局会话以保存本地会话id和退出接口
//                    // localLoginOutUrl
//                    ServletContext context = request.getServletContext();// 容器
//                    // String localExitUrl =
//                    // context.getInitParameter("localExitUrl");
//                    String basePath = request.getScheme() + "://" + request.getServerName() + ":"
//                            + request.getServerPort() + request.getContextPath() + "/";
//                    postMethod.addParameter("localLoginOutUrl", basePath + localExitUrl);// 退出接口
//                    postMethod.addParameter("localSessionId", session.getId());// 退出接口
//                    // 发送验证请求
//                    HttpClient httpClient = new HttpClient();
//                    try {
//                        httpClient.executeMethod(postMethod);
//                        String json = postMethod.getResponseBodyAsString();
//                        postMethod.releaseConnection();
//                        // 返回
//                        Map<String, Object> map = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
//                        }.getType());
//                        int code = ((Double) map.get("code")).intValue();
//                        if (code == 0) {
//                            username = (String) map.get("account");
//                            session.setAttribute("username", username);
//                            session.setAttribute("globalSessionId", globalSessionId);// 等退出全局登录时使用
////							zhou start
//							CasHttpServletRequestWrapper casRequest=new CasHttpServletRequestWrapper(request,username);
//                            filterChain.doFilter(casRequest, response);
////							zhou end
////                            filterChain.doFilter(request, response);
//                            logger.info("验票成功");
//                            return;
//                        } else {
//                            logger.info("验票失败，重新跳转到sso登录页面");
//                            response.sendRedirect(loginPage + "?service=" + url);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    logger.info("缺少必须的globalSessionId");
//                    throw new RuntimeException("ticket不为空时，globalSessionId不能为空");
//                }
//
//            } else {
//                logger.info("ticket为空！跳转到认证中心登录页");
//                response.sendRedirect(loginPage + "?service=" + url);
//            }
//        } else {// 已经登录
//            logger.info("已经登录，无需拦截,进入系统:" + request.getContextPath());
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//    }

}