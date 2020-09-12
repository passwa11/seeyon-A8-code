package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.login.server.listener.GlobalSessions;
import com.seeyon.ctp.login.server.util.Constant;
import com.seeyon.ctp.login.server.util.StringUtilSso;
import com.seeyon.ctp.login.server.util.TicketUtil;
import org.apache.commons.logging.Log;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("casplus")
@Produces({MediaType.APPLICATION_JSON})
public class CasTicketResource extends BaseResource {
    private static Log LOGGER = CtpLogFactory.getLog(CasTicketResource.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("logout")
    public Response logout(Map data){
        String globalSessionid=data.get("globalSessionId").toString();
        HttpSession httpSession = GlobalSessions.get(globalSessionid);
        Map<String, String> loginOutMap = (Map<String, String>) httpSession.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<局部会话id，应用退出接口>
        Map<String,Object> resultmap=new HashMap<>();
        if (loginOutMap != null) {
            // 登出系统
            // 直接使用map遍历并在遍历中删除元素会报错ConcurrentModificationException，不能在遍历中动态修改集合,解决办法：使用Iterator
            // for (String localSessionId : loginOutMap.keySet()) {
            // 正确的方法
            Iterator<Map.Entry<String, String>> iterator = loginOutMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                Map<String, String> params = new HashMap<>();
                params.put("localSessionId", entry.getKey());
                try {
//                    rg.sso.util.HttpUtil.http(entry.getValue(), params);
                    iterator.remove();// 删除已经退出的APP会话信息。
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("CasTicketResource中的退出方法出错了"+e.getMessage());
                }
            }
            resultmap.put("code",0);
        } else {
//            logger.info("从未登陆过或登出会话异常，重启浏览器");
            resultmap.put("code",-1);
        }

        return Response.ok(resultmap).build();
    }


    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("verify")
    public Response verify(@FormParam("ticket") String ticket,@FormParam("globalSessionId")  String globalSessionId,@FormParam("localSessionId") String localSessionId,@FormParam("localLoginOutUrl")  String localLoginOutUrl) {
        Map<String, Object> map = new HashMap<>();
        String account = TicketUtil.get(ticket);
        TicketUtil.remove(ticket);
        if (StringUtilSso.isUnEmpty(account)) {
            HttpSession session = GlobalSessions.get(globalSessionId);
            Map<String, String> loginOutMap = null;
            if (session.getAttribute("loginOutMap") != null) {
                loginOutMap = (Map<String, String>) session.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<应用退出接口，应用服务器会话id>
            } else {
                loginOutMap = new HashMap<>();
                session.setAttribute("loginOutMap", loginOutMap);
            }
            loginOutMap.put(localSessionId, localLoginOutUrl);
            // 返回数据
            map.put("code", Constant.CODE_SUCCESS);
            map.put("msg", "令牌认证成功!");
            map.put("globalSessionId", globalSessionId);// 应用发送给SSO退出请求时使用(应该无需返回)，之前登录生成令牌回调已经发送了全局会话id
            map.put("account", account);
        } else {
            map.put("code", Constant.CODE_FAIL);
            map.put("msg", "令牌认证失败");
        }
        return Response.ok(map).build();
    }
}

