package com.seeyon.apps.ext.ssoLogin.controller;

import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class SendServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String ticket = request.getRemoteUser();
            String templateId = request.getParameter("templateId");
            String type = request.getParameter("type");
            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xkdxSso");

            String url = "";
            if (null != type && !"".equals(type)) {
                if (type.equals("xt")) {
                    url = "/seeyon/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId=" + templateId;
                } else if (type.equals("gw")) {
                    url = "/seeyon/govdoc/govdoc.do?method=newGovdoc&sub_app=1&from=template&templateId=" + templateId;
                } else if (type.equals("self")) {
                    url = "/seeyon/collaboration/collaboration.do?method=newColl&rescode=F01_newColl&_resourceCode=F01_newColl";
                }
            }
            String path = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url.substring(url.indexOf("seeyon") - 1));
            response.sendRedirect(path);
        } catch (IOException e) {
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
