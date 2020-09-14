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
            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xzykSso");
//            String url = "/seeyon/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId=15912627018010";
            String url = "/seeyon/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId="+templateId;
            String urlt = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url.substring(url.indexOf("seeyon") - 1));
            response.sendRedirect(urlt);
        } catch (IOException e) {
            log.error("医科系统打开Oa代办事项出错了，错误信息：" + e.getMessage());
        }
    }

}
