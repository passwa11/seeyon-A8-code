<%@page import="java.net.URLEncoder" %>
<%@page import="java.net.URLDecoder" %>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>

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
            var documentAuthority = '<%=request.getAttribute("documentAuthority")%>';
            if (documentAuthority != '1') {
                var userID = '<%=request.getAttribute("userID")%>';
                var password = '<%=request.getAttribute("password")%>';
                var ouserID = '<%=request.getAttribute("ouserID")%>';
                window.open('Http://172.16.0.92/integration/page/oaTolaw.jsp?userID='+userID+'&Password='+password+'&ouserID='+ouserID);
                return true;
            } else if (login == '1') {
                alert("请先设置法律系统用户名、密码 ");
                return false;
            }

        }
    </script>
</head>

<body onload="return  login();">

</body>
</html>
