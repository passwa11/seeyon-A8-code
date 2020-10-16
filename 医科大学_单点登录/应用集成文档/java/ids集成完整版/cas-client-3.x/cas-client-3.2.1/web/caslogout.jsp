<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%
	session.invalidate();
	String casLogoutURL = application.getInitParameter("casServerLogoutUrl");
	String redirectURL = casLogoutURL+"?service="+URLEncoder.encode("http://ssodemo.test.com:8080/cas/caslogin.jsp");
	response.sendRedirect(redirectURL);
%>