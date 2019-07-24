<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>办结</title>
<style>

html,body{
	margin:0;
	padding:0;
}

a{
	text-decoration:none;
	color:#000;
}

td {
	font-family: Arial, "Ping Fang SC", "Microsoft YaHei", Helvetica, sans-serif, "SimSun";
	height:30px;
	line-height:30px;
	white-space: nowrap;
	padding-left: 5px;
	font-size:12px;
}
</style>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	
    <tbody>
	    <c:forEach var="finish" items="${xkjtLeaderBanJies}">
		    <tr>
	            <td style="font-size:14px" width="60%" onclick="xkjtOpen(this,'${finish.id}','${finish.edoc_type}')"><a href="javascript:void(0);">${finish.subject}</a></td>
	            <td width="10%" align="right"><span title="<fmt:formatDate value='${finish.create_time}' pattern='yyyy-MM-dd' />"><fmt:formatDate value='${finish.create_time}' pattern='yyyy-MM-dd' /></span></td>
	            <td width="10%" align="right"><span title="${finish.send_unit}">${finish.send_unit}</span></td>
	            <td style="color:#505050" width="10%" align="left"><a onclick="javascript:window.top.showMemberCard('${finish.start_user_id}','5')" style="width:5px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;" title="${finish.create_person}">${finish.create_person}</a></td>
	            
	            <c:if test="${finish.edoc_type==0}">
	            <td width="10%" align="right">
	            <a class="paper" style="color:#505050;" href="javascript:void(0)" onclick="javascript:window.top.vPortal.sectionHandler.multiRowVariableColumnTemplete.open_link('/edocController.do?method=entryManager&amp;entry=sendManager&amp;listType=listFinish')">发文<!-- extClasses --></a>
	            </td></c:if>
	            
	            <c:if test="${finish.edoc_type==1}">
		            <td width="10%" align="right">
		             <a class="paper" style="color:#505050;" href="javascript:void(0)" onclick="javascript:window.top.vPortal.sectionHandler.multiRowVariableColumnTemplete.open_link('/edocController.do?method=entryManager&entry=recManager&listType=listFinish&objectId=-7217783385919962978')">收文<!-- extClasses --></a>
		            </td>
	            </c:if>
	        </tr>
		</c:forEach>
    </tbody>
</table>
</body>
<script type="text/javascript">
function xkjtOpen(obj,id,edocId){
	var url = "edocController.do?method=edocDetailInDoc&summaryId="+id+"&openFrom=lenPotent&lenPotent=100";
	window.open(url,"_blank");
}
function(document).ready(function(){
	$(".paper").mouseover(function(){
		this.css('color','#0066FF')
	})
	
	$(".paper").mouseout(function(){
		this.css('color','#505050')
	})
})
</script>
</html>