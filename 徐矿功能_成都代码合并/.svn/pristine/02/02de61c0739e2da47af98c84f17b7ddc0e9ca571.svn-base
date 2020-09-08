<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@page import="com.seeyon.ctp.common.constants.ApplicationCategoryEnum" %>
<%@page import="com.seeyon.ctp.common.constants.Constants" %>
<!DOCTYPE html>
<html>

<head>
    <%@ include file="/WEB-INF/jsp/edoc/edocHeader.jsp" %>
    <meta name="Generator" content="EditPlus">
    <meta name="Author" content="">
    <meta name="Keywords" content="">
    <meta name="Description" content="">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/pdf/js/pdf.js" />"></script>
    <%
        String ctxPath =request.getContextPath(),  ctxServer = request.getScheme()+"://" + request.getServerName() + ":"
            + request.getServerPort() + ctxPath;
    %>
    <script>
    $(function(){
		window.setTimeout(function() {
// 			$(window.parent.document).find("#showPdfView_main").parent().css("display", "none");
    		pdfFullSize();
		}, 1000);
    });
    </script>
</head>

<body scroll="no" style="overflow: hidden">
	<v3x:showContent type="Pdf" content="${xkjtFileId }" createDate="${xkjtCreateDate }" viewMode="true"/>
</body>

</html>