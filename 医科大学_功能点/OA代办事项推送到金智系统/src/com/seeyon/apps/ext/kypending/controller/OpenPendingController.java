package com.seeyon.apps.ext.kypending.controller;

import com.seeyon.ctp.common.authenticate.sso.SSOTicketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class OpenPendingController {

    private static Logger log = LoggerFactory.getLogger(OpenPendingController.class);

    public static void formSend(HttpServletRequest request, HttpServletResponse response) {
        /**
         * 1:是协同
         * 4:公文
         * 6：会议
         */
        try {
            String ticket = request.getParameter("ticket");
            String affairId = request.getParameter("affairId");
            String objectId = request.getParameter("objectId");
            String app = request.getParameter("app");
            SSOTicketManager.getInstance().newTicketInfo(ticket, ticket, "xkdxSso");
            String url = "";
            if (Integer.parseInt(app) == 1) {
                url = "/seeyon/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=" + affairId;
            } else if (Integer.parseInt(app) == 4) {
                url = "/seeyon/govdoc/govdoc.do?method=summary&isFromHome=true&openFrom=listPending&affairId=" + affairId + "&app=4&summaryId=" + objectId + "";
            } else if (Integer.parseInt(app) == 6) {
                url = "/seeyon/mtMeeting.do?method=mydetail&id=" + objectId + "&affairId=" + affairId + "&state=10";
            }
            String path = "/seeyon/main.do?method=login&ticket=" + ticket + "&login.destination=" + URLEncoder.encode(url.substring(url.indexOf("seeyon") - 1));
            response.sendRedirect(path);
        } catch (IOException e) {
            log.error("医科系统打开Oa代办事项出错了，错误信息：" + e.getMessage());
        }
    }


}
