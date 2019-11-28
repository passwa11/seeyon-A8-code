<%@page import="java.net.URLEncoder" %>
<%@page import="java.net.URLDecoder" %>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%--<%@ include file="/WEB-INF/jsp/form/formcreate/formHeader.jsp" %>--%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<%--                这个页面没用到            --%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <base href="<%=basePath%>">

    <title></title>

    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="This is my page">
    <script type="text/javascript">

        function login() {
            var login = '<%=request.getAttribute("loginUrl")%>';
            // console.log(login,'1');
            window.open('http://pcc.263.net/PCC/263mail.do?cid=ff80808150fbc5b2015124367cc103ba&domain=xkjt.net&uid=hh&sign=206ebf9f1d8b71aa455e9fee3ae5cd0c');
            // console.log(login,'2');

        }
    </script>
</head>

<body onload="return  login();">

</body>
</html>
