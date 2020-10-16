package com.seeyon.apps.ext.ssoLogin.sso;

import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class SendTest {

    private static Logger log = LoggerFactory.getLogger(SendTest.class);

    public static void formSend(HttpServletRequest request, HttpServletResponse response) {
        try {
            String ticket = request.getParameter("ticket");
            String templateId = request.getParameter("templateId");
            String type = request.getParameter("type");
            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xkdxSso");
            String url = "";
            if (null != type && !"".equals(type)) {
                if (type.equals("xt")) {
                    url = "/seeyon/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId=" + templateId;
                } else if (type.equals("gw")) {
                    url = "/seeyon/govdoc/govdoc.do?method=newGovdoc&sub_app=1&from=template&templateId=" + templateId;
                } else if(type.equals("self")){
                    url ="/seeyon/collaboration/collaboration.do?method=newColl&rescode=F01_newColl&_resourceCode=F01_newColl";
                }
            }
            String path = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url.substring(url.indexOf("seeyon") - 1));
            response.sendRedirect(path);
        } catch (IOException e) {
            log.error("医科系统打开Oa模板出错了，错误信息：" + e.getMessage());
        }
    }

}
