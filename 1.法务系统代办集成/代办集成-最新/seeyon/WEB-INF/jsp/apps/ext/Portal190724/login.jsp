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
            var login = '<%=request.getAttribute("lawAuthority")%>';

            var Token = '<%=request.getAttribute("Token")%>';
            var Timespan = '<%=request.getAttribute("Timespan")%>';
            var AppKey = '<%=request.getAttribute("AppKey")%>';
            var loginName = '<%=request.getAttribute("loginName")%>';

            if (login != '1') {
                window.open('http://172.16.3.108:9595/law/main/login.htm?Token=' + Token + '&Timespan=' + Timespan + '&AppKey=' + AppKey + '&loginName=' + loginName);
                return true;
            } else if (login == '1') {
                alert("请先设置法律系统用户名、密码 ");
                //window.location.href='http:/localhost/seeyon/sysMgr/individual/individualManager';//法律系统主页面
                return false;
            }

        }
    </script>
</head>

<body onload="return  login();">

</body>
</html>
