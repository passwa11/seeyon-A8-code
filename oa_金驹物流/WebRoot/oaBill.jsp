<%@page import="com.seeyon.apps.jjwl.JjwlLogin"%>
<%@ page contentType="text/html; charset=utf-8" isELIgnored="false" buffer="none"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<%
		JjwlLogin.oabill(request,response);
	%>
	</head>
</html>