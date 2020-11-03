<%@ page import="com.seeyon.apps.ext.oauthLogin.util.PropUtils" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="renderer" content="webkit|ie-stand|ie-comp" />
<title>A8</title>
</head>
<body>
<%
    PropUtils pUtils = new PropUtils();
    if (session.getAttribute(pUtils.getSSOSessionUser()) == null) {
        response.sendRedirect(pUtils.getSSOAuthPath() + "?returnUrl=" + java.net.URLEncoder.encode(pUtils.getSSOClientHomePage(), "utf-8"));//未登录跳转到转向服务器登录页面
    } else {
        response.getWriter().print("用户已登录;<br>Token:" + session.getAttribute(pUtils.getSSOSessionAccessToken()) + "<br>用户ID:" + session.getAttribute(pUtils.getSSOSessionUserId()) + "<br>超时时间:" + session.getAttribute(pUtils.getSSOSessionExpires_in()) + "<br>用户信息是：" + session.getAttribute(pUtils.getSSOSessionUser()));//拿到用户信息进行处理
    }
%>
<%--<jsp:forward page="main.do" />--%>
</body>
</html>