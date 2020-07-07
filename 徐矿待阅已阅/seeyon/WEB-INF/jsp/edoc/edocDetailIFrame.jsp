<!DOCTYPE HTML>
<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<html class="h100b">
<head>
<%@page import="com.seeyon.ctp.common.flag.*"%>
<meta http-equiv="X-UA-Compatible" content="IE=9">

<%--
 $Author: wuym $
 $Rev: 2 $
 $Date:: 2010-01-29 19:12:44#$:

 Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 This software is the proprietary information of Seeyon, Inc.
 Use is subject to license terms.
--%>
<%@ page isELIgnored="false" import="com.seeyon.ctp.common.AppContext,java.util.Locale"%>
<%@ taglib uri="http://v3x.seeyon.com/taglib/core" prefix="v3x"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="ctp" uri="http://www.seeyon.com/ctp"%>
<%
    response.setHeader("Pragma", "No-cache");
	response.setHeader("Cache-Control", "no-store");
	response.setDateHeader("Expires", 0);
	boolean isDevelop = AppContext.isRunningModeDevelop();
    String ctxPath =request.getContextPath(),  ctxServer = request.getScheme()+"://" + request.getServerName() + ":"
                    + request.getServerPort() + ctxPath;
    Locale locale = AppContext.getLocale();
%>
<c:set var="path" value="${pageContext.request.contextPath}" />
<script type="text/javascript">
  var CSRFTOKEN = '<%=session.getAttribute("CSRFTOKEN")%>';
  var _ctxPath = '<%=ctxPath%>', _ctxServer = '<%=ctxServer%>';
  var _locale = '<%=locale%>',_isDevelop = <%=isDevelop%>,_sessionid = '<%=session.getId()%>',_isModalDialog = false;
  <c:if test="${param._isModalDialog == 'true' || param.isFromModel == 'true'}">
  _isModalDialog = true;
  </c:if>
  var _resourceCode = "${ctp:escapeJavascript(param._resourceCode)}";
  var seeyonProductId="${ctp:getSystemProperty("system.ProductId")}";
</script>
<link rel="stylesheet" href="${path}/common/all-min.css${ctp:resSuffix()}">
<link href="<c:url value="/common/images/${v3x:getSystemProperty('portal.porletSelectorFlag')}/favicon${v3x:getSystemProperty('portal.favicon')}.ico${v3x:resSuffix()}" />" type="image/x-icon" rel="icon"/>
<%@ include file="/WEB-INF/jsp/ctp/portal/common/components_theme.jsp"%>
<script type="text/javascript" src="${path}/i18n_<%=locale%>.js${ctp:resSuffix()}"></script>
<%
    if (isDevelop) {
%>
<script type="text/javascript" src="${path}/common/js/jquery-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/calendar/calendar-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/misc/Moo-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/misc/jsonGateway-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.hotkeys-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.json-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.fillform-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.jsonsubmit-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.autocomplete-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.code-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/common-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/seeyon.ui.core-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.ajaxgridbar-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.base64-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/v3x-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.common-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/searchBox-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.checkform-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.dialog-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.contextmenu-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.grid-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.layout-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.menu-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.progress-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.tab-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.toolbar-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.tree-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.calendar-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.arraylist-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.tooltip-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.focusImage-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.projectList-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.scrollbar-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.contextmenu-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.comLanguage-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.print-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.shortCutSet-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.colorpicker-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.pageMenu-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.peopleCrad-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.peopleSquare-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/ui/seeyon.ui.channel-debug.js"></script>

<script type="text/javascript" src="${path}/common/js/jquery.comp-debug.js"></script>
<script type="text/javascript" src="${path}/common/js/jquery.tree-debug.js"></script>
<%
    } else {
%>
<script type="text/javascript" src="${path}/common/all-min.js${ctp:resSuffix()}"></script>
<%
    }
%>
<script type="text/javascript" src="${path}/common/js/ui/calendar/calendar-language.js${ctp:resSuffix()}"></script>
<c:set var="loginTime" value="${ CurrentUser !=null ? CurrentUser.etagRandom : 0}" />
<script type="text/javascript" src="${path}/main.do?method=headerjs&login=${loginTime}"></script>
<script type="text/javascript">
$.ctx._currentPathId = '${_currentPathId}';
$.ctx.fillmaps = <c:out value="${_FILL_MAP}" default="null" escapeXml="false"/>;
var CSRFTOKEN = '<%=session.getAttribute("CSRFTOKEN")%>';
</script>
<%@ include file="/WEB-INF/jsp/common/content/workflow.jsp"%>
<%@ include file="/WEB-INF/jsp/edoc/lock/edocLock_js.jsp"%>
<link rel="stylesheet" href="${path}/apps_res/edoc/css/edocnew.css">
<link rel="stylesheet" href="${path}/common/image/css/touchTouch.css">
<style type="text/css">
html{height:100%;overflow:hidden;}
</style>
</head>
<script type="text/javascript">
var callbackFunction= window.dialogArguments==undefined?null:window.dialogArguments.callback;
function releaseApplicationButtons(){
		var fnx_edoc;
	    if($.browser.mozilla){
	    	 fnx_edoc =$(window.edocDetailIframe)[0].document.getElementById("detailMainFrame").contentWindow;
	     }else{
	    	 fnx_edoc =window.edocDetailIframe.detailMainFrame;
	     }
	    fnx_edoc.enablePrecessButtonEdoc();
}
</script>
<body class="h100b over_hidden">
<input type="hidden" id="workFlowContent" class="hidden"/>
<iframe id="edocDetailIframe" name="edocDetailIframe" src="edocController.do?method=detail&xkFlag=true&summaryId=${param.summaryId}&affairId=${ctp:toHTML(param.affairId)}&detailType=${ctp:toHTML(param.detailType)}&from=${ctp:toHTML(param.from)}&edocType=${ctp:toHTML(param.edocType)}&edocId=${param.edocId}&list=${param.list}&sendSummaryId=${param.sendSummaryId}&canNotOpen=${param.canNotOpen}&openFrom=${param.openFrom}&openEdocByForward=${param.openEdocByForward}&isColl360=${param.isColl360}&isCollCube=${param.isCollCube}&isTransFrom=${param.isTransFrom}&isWfanalysis=${param.isWfanalysis}${ctp:csrfSuffix()}" FRAMEBORDER="0" marginheight="0" marginwidth="0" width="100%" height="100%" style="border:0px;"></iframe>
</body>
</html>
