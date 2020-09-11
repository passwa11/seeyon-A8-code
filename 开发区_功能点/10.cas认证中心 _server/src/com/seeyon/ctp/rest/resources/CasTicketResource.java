package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.login.server.listener.GlobalSessions;
import com.seeyon.ctp.login.server.util.Constant;
import com.seeyon.ctp.login.server.util.StringUtilSso;
import com.seeyon.ctp.login.server.util.TicketUtil;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("casplus")
@Produces({MediaType.APPLICATION_JSON})
public class CasTicketResource extends BaseResource {
    private static Log LOGGER = CtpLogFactory.getLog(CasTicketResource.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("verify")
    public Response verify(Map data) {
        Map<String, Object> map = new HashMap<>();
        String account = TicketUtil.get(data.get("ticket").toString());
        TicketUtil.remove(data.get("ticket").toString());
        if (StringUtilSso.isUnEmpty(account)) {
            String globalSessionId = data.get("globalSessionId").toString();
            HttpSession session = GlobalSessions.get(globalSessionId);
            Map<String, String> loginOutMap = null;
            if (session.getAttribute("loginOutMap") != null) {
                loginOutMap = (Map<String, String>) session.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<应用退出接口，应用服务器会话id>
            } else {
                loginOutMap = new HashMap<>();
                session.setAttribute("loginOutMap", loginOutMap);
            }
            String localSessionId = data.get("localSessionId").toString();
            String localLoginOutUrl = data.get("localLoginOutUrl").toString();
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

