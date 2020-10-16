<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>待阅</title>
<style>
html,body{
	margin:0;
	padding:0;
}
a{
	text-decoration:none;
	color:#000;
}
.active {
	font-weight: bold;
}
td {
	font-family: Arial, "Ping Fang SC", "Microsoft YaHei", Helvetica, sans-serif, "SimSun";
	height:30px;
	line-height:30px;
	white-space: nowrap;
	padding-left: 5px;
	font-size:14px;
}
</style>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	
    <tbody>
	    <c:forEach var="detail" items="${xkjtLeaderDaiYues}">
		    <tr>
	         <%-- 项目：徐州矿物集团【待阅栏目：待阅栏目显示的内容，标题+文号+时间】 作者：wxt.xiangrui 时间：2019-6-3 start --%>
	            <td width="50%" class="active" onclick="xkjtOpen(this,'${detail.id}','${detail.edoc_id}')"><a href="javascript:void(0);">${detail.title}</a></td>
	            <td width="10%" align="center"><span title="${detail.doc_mark}">${detail.doc_mark}</span></td>
	            <%--<td width="10%" align="center"><span title="${detail.senderName}">${detail.senderName}</span></td> --%>
	            <td width="30%" align="center"><span title="<fmt:formatDate value='${detail.send_date}' pattern='yyyy-MM-dd' />"><fmt:formatDate value='${detail.send_date}' pattern='yyyy-MM-dd' /></span></td>
	            <%--<td width="10%" align="center"><span title="待阅">待阅</span></td> --%>
	         <%-- 项目：徐州矿物集团【待阅栏目：待阅栏目显示的内容，标题+文号+时间】 作者：wxt.xiangrui 时间：2019-6-3 end --%>
	        </tr>
		</c:forEach>
    </tbody>
</table>
</body>
<script type="text/javascript">
function xkjtOpen(obj,id,edocId){
	debugger;
	var param = {
			id:id,
			status:12
	};
	
	$(obj).removeClass("active");
	var requestCaller = new XMLHttpRequestCaller(this, "xkjtManager", "updateXkjtLeaderDaiYueByCondition", false);
  	requestCaller.addParameter(1, "String", param.id);  
  	requestCaller.addParameter(2, "String", param.status);
  	var bool = requestCaller.serviceRequest();
	var url = "edocController.do?method=edocDetailInDoc&summaryId="+edocId+"&openFrom=lenPotent&lenPotent=100&isShow=1";
	window.open(url,"_blank");
}
</script>
</html>