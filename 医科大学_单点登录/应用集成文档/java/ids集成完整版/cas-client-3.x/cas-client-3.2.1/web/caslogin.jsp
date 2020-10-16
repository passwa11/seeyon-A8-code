<%@page import="java.util.Map" %>
<%@ page
        import="java.security.Principal" %>
<%@ page
        import="org.jasig.cas.client.authentication.AttributePrincipal" %>
<%@ page
        import="java.util.Iterator" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    //    cas-client-3.2.1版本集成
    String uid = request.getRemoteUser();
    String cn = "";
    Principal principal = request.getUserPrincipal();
    if (principal != null && principal instanceof AttributePrincipal) {
        AttributePrincipal aPrincipal = (AttributePrincipal) principal;
        //获取用户信息中公开的Attributes部分
        Map<String, Object> map = aPrincipal.getAttributes();
    			/*Iterator<String> it = map.keySet().iterator();
    			while (it.hasNext()) {
    				String k = it.next();
    				response.getWriter().printf("%s:%s\r\n", k, map.get(k));
    			}*/
        cn = (String) map.get("cn");
    }
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title></title>
</head>

<body>
Hello,<%=uid%>! Welcome <%=cn %> &nbsp;
<a href="caslogout.jsp">登出</a>
</body>
</html>
