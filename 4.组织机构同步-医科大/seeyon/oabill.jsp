<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" buffer="none" %>
<%@page import="java.util.Date" %>
<%@ page import="com.seeyon.apps.ext.zxzyk.ssologin.SsoLogin" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <%
        SsoLogin.oaBill(request, response);
    %>
</head>
</html>
