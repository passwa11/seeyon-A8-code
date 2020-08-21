<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title></title>
<%@ include file="header.jsp" %>

<v3x:selectPeople id="per" panels="Department" selectType="Member" jsFunction="setBulPeopleFields(elements);" minSize="1" maxSize="1" />

<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery.js${v3x:resSuffix()}" />"></script>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
<script type="text/javascript">

var list = "${v3x:escapeJavascript(param.list)}";
var flag = "${v3x:escapeJavascript(flag)}";
if(flag != null) {
	list = flag;
}
showCtpLocation("F09_meetingRoom");

$(function() {
	setTimeout(function() {
		if($("#headIDlistTable") != null) {
			$("#headIDlistTable").find("th").eq(0).find("div").attr("title", v3x.getMessage("meetingLang.meeting_choose_all"));
		}
	}, 100);
});

function showPerm(id, roomId, proxy, proxyId) {
	var url = "meetingroom.do?method=createPerm&id="+id;
	if(typeof(proxy)!='undefined') {
		url += "&proxy="+proxy+"&proxyId="+proxyId;
	}
	parent.detailFrame.location = url + CsrfGuard.getUrlSurffix();
}

function doClear() {
	var checkedBoxs = getCheckedbox(document.listForm);
	if(!checkedBoxs || checkedBoxs.length==0) {
		alert("<fmt:message key='mr.alert.pleaseselecttoclear'/>！");
		return false;
	}
	
	var currentDate = new Date();
	currentDate = currentDate.format("yyyy-MM-dd HH:mm");
	var currentDatetime = Date.parse(currentDate.replace(/\-/g,"/"));
	
	var roomName = "";
	
	for(var i=0; i<checkedBoxs.length; i++) {
		var checkedBox = checkedBoxs[i];
		
		var isAllowed = checkedBox.getAttribute("isAllowed"); 
		
		var roomAppStartDate = checkedBox.getAttribute("startDatetime");
		var roomAppStartDatetime = Date.parse(roomAppStartDate.replace(/\-/g,"/"));
		
		var roomAppEndDate = checkedBox.getAttribute("endDatetime");
		var roomAppEndDatetime = Date.parse(roomAppEndDate.replace(/\-/g,"/"));
		
		if(roomName != "") {
			roomName += "、";
		}
		if(checkedBox.getAttribute("roomName") != "") {
			roomName += "《" + checkedBox.getAttribute("roomName") + "》";
		}
		
		if(isAllowed == "0") {
			alert("<fmt:message key='mr.alert.cannotclear'/>！");
			return false;
		}
	
		/*if(currentDatetime >= roomAppStartDatetime && currentDatetime <= roomAppEndDatetime) {
			alert("${ctp:i18n('meeting.alert.meetingRoom')}"+ roomName +"${ctp:i18n('meeting.alert.mrUsedNoDelect')}");//"会议室："+ roomName +"正在使用中，暂时无法删除！"
			return ;
		} else if(currentDatetime < roomAppStartDatetime) {
			alert("${ctp:i18n('meeting.alert.meetingRoom')}"+ roomName +"${ctp:i18n('meeting.alert.mrNoUsedNoDelect')}");//"会议室："+ roomName +"未被使用，暂时无法删除！"
			return ;
		}*/
	}
	
	if(confirm("${ctp:i18n('meeting.alert.rmSureToDelect')}")) {//该操作不能恢复，是否进行删除操作?
		document.listForm.action = "meetingroom.do?method=execClearPerm";
		document.listForm.submit();
	}
}
	
function ChangedEvent(obj) {
	var beginDate = document.getElementById("fromDate");
	var endDate = document.getElementById("toDate");
	if(beginDate != null && endDate != null){
		beginDate.value = "";
		endDate.value = "";
	}
	showNextCondition(obj);
}
	
function search_result() {
	var beginDate = document.getElementById("fromDate").value;
	var endDate = document.getElementById("toDate").value;
	if(compareDate(beginDate,endDate)>0){
		alert("${ctp:i18n('meeting.alert.startTimeOverEndTime')}");//开始时间不能大于结束时间！
		return ;
	}
	doSearch();
}

function doCancel() {
	var checkedBoxs = getCheckedbox(document.listForm);
	if(!checkedBoxs || checkedBoxs.length==0) {
		alert("<fmt:message key='mr.alert.pleaseselecttocancel'/>！");
		return false;
	}
	if(checkedBoxs.length > 1) {
		alert("${ctp:i18n('meeting.alert.chooseOnlyOneToCancle')}");//只能选择一条进行撤销！
		return false;
	}	
	var checkedBox = checkedBoxs[0];
	var meetingName = checkedBox.getAttribute("meetingName");
	var isAllowed = checkedBox.getAttribute("isAllowed"); 

	var currentDate = new Date();
	currentDate = currentDate.format("yyyy-MM-dd HH:mm");
	var currentDatetime = Date.parse(currentDate.replace(/\-/g,"/"));
	
	var roomAppStartDate = checkedBox.getAttribute("startDatetime");
	var roomAppStartDatetime = Date.parse(roomAppStartDate.replace(/\-/g,"/"));
	
	var roomAppEndDate = checkedBox.getAttribute("endDatetime");
	var roomAppEndDatetime = Date.parse(roomAppEndDate.replace(/\-/g,"/"));
	
	//会议室审核状态为待审核、审核不通过，管理员是能在会议室使用时间内撤销
	if(currentDatetime > roomAppStartDatetime && currentDatetime < roomAppEndDatetime && isAllowed == "1") {
		alert("${ctp:i18n('meeting.alert.meetingRoomNoCancle1')}");
		return ;
	} else if(currentDatetime > roomAppEndDatetime) {
		alert("${ctp:i18n('meeting.alert.meetingRoomNoCancle2')}");
		return ;
	}
	
	if(meetingName != "") {
		meetingName = "《" + meetingName + "》";
	}
	
	var cancelMsg = "<fmt:message key='mr.alert.confirmcancel'/>";
	if(meetingName != "") {
		cancelMsg = "<fmt:message key='mr.alert.meetingRoomCancle1'/>"+meetingName+"<fmt:message key='mr.alert.meetingRoomCancle2'/>";
	}
	if(confirm(cancelMsg)) {
		var url = "meeting.do?method=openCancelDialog";
		openMeetingDialog(url, v3x.getMessage("meetingLang.meeting_action_meetingRoom_cancel"), 450, 260, function(rv, canSendSMS) {
			cancelMeetingCallback(rv, canSendSMS);
		});	
	}
}
function cancelMeetingCallback(rv, canSendSMS) {
	document.listForm["cancelContent"].value = rv;
	document.listForm.action = "meetingroom.do?method=execCancel${ctp:csrfSuffix()}";
	document.listForm.submit();
}

function getCheckedbox(formNode) {
	var ns = formNode.getElementsByTagName("input");
	var ids = [];
	var j = 0;
	for(var i = 0; i < ns.length; i++) {
		if(ns[i].type == "checkbox" && ns[i].name == "id" && ns[i].checked) {
			ids[j++] = ns[i];
		}
	}
	return ids;
}

function _submitCallback(msgType, msg) {
 	if(msgType == "success") {
 		alert("${ctp:i18n('meeting.alert.success')}");//操作成功!
 		var href = parent.location.href;
 		parent.location.href = href;
 	} else {//同名
 		alert("${ctp:i18n('meeting.alert.failed')}");//操作失败！
 	}
}

</script>
</head>

<body srcoll="no" style="overflow: hidden" style="padding: 0px" onUnLoad="UnLoad_detailFrameDown()">
	
<div class="main_div_row2">
  		
<div class="right_div_row2">
				
<div class="top_div_row2">
					
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">

<tr>
	<td class="webfx-menu-bar" style="height:30px;">
		<script type="text/javascript">
			var myBar = new WebFXMenuBar("${pageContext.request.contextPath}","gray");
			myBar.add(new WebFXMenuButton("create", "<fmt:message key='common.toolbar.cancel.label' bundle='${v3xCommonI18N}'/>", "doCancel()", [3,8]));
			if(list=='passperm'||list=='noReview'||list=='all'){
				myBar.add(new WebFXMenuButton("modify", "<fmt:message key='mr.button.del'/>", "doClear()", [1,3]));
			}
			document.write(myBar);
		    document.close();
		</script>
 	</td>
	<td class="webfx-menu-bar" style="height:30px;">
		<form id="searchForm" name="searchForm" method="post" onsubmit="return false" action="meetingroomList.do?method=listPerm&flag=all&list=waitReview${ctp:csrfSuffix()}">
			<input type="hidden" name="flag" value="${v3x:toHTML(flag)}"/>
			<input type="hidden" name="method" value="listPerm" />
			<input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}" />
 			<div class="div-float-right condition-search-div">
 				<div class="div-float">
 					<select name="condition" class="condition" id="condition" onChange="ChangedEvent(this)" inputName="<fmt:message key='mr.label.querycondition'/>" validate="notNull">
	  					<option value="">--<fmt:message key='mr.label.querycondition'/>--</option>
	  					<option value="1"><fmt:message key='mr.label.meetingroom'/></option>
	  					<option value="2"><fmt:message key='mr.label.appPerson'/></option>
	  					<option value="3"><fmt:message key='mr.label.appDept'/></option>
	  					<option value="4"><fmt:message key='meeting.panel.label.holdingTime'/></option>
	  					<option value="5"><fmt:message key='mr.label.checkStatus'/></option>
 					</select>
 				</div>
 				
 				<div id="4Div" class="div-float hidden">
					<input type="text" name="textfield" id="fromDate" class="input-date cursor-hand" onclick="whenstart('${pageContext.request.contextPath}',this,575,140);" readonly /> 
					- 
					<input type="text" name="textfield1" id="toDate" class="input-date cursor-hand" onclick="whenstart('${pageContext.request.contextPath}',this,575,140);" readonly />
				</div>

 				<div id="1Div" class="div-float hidden">
 					<input type='text' name='textfield' id='textfield' validate='maxLength' maxLength='50' style='width:100px; padding:0px;' autoComplete='off' />
 				</div>
 				
 				<div id="2Div" class="div-float hidden">
 					<input type='text' name='textfield' id='textfield' validate='maxLength' maxLength='50' style='width:100px; padding:0px;' autoComplete='off'/>
 				</div>
 				
 				<div id="3Div" class="div-float hidden">
 					<input type='text' name='textfield' id='textfield' validate='maxLength' maxLength='50' style='width:100px; padding:0px;' autoComplete='off' />
 				</div>

 				<div id="5Div" class="div-float hidden">
 					<select name="textfield">
						<option value="0,1,2"><fmt:message key="meeting.options.check.status0"></fmt:message></option>
						<option value="0"><fmt:message key="meeting.room.app.status.0"/></option>
						<option value="1"><fmt:message key="meeting.room.app.status.1"/></option>
						<option value="2"><fmt:message key="meeting.room.app.status.2"/></option>
					</select>
 				</div>
 				
 				<div onclick="search_result();" class="condition-search-button">&nbsp;</div>
 			</div>
 		</form>
 	</td>
</tr>

</table>

</div><!-- top_div_row2 -->

<div class="center_div_row2" id="scrollListDiv">

<form name="listForm" id="listForm" method="post" style="margin: 0px" target="hiddenIframe">
<input type="hidden" id="isContainMeeting" name="isContainMeeting" value="false" />
<input type="hidden" id="cancelContent" name="cancelContent" value="false" />

<v3x:table htmlId="listTable" data="list" var="bean" pageSize="${pageSize }" size="${size }" showHeader="true" showPager="true" isChangeTRColor="true">
	<c:set var="onClick" value="showPerm('${bean.roomAppId}', '${bean.roomId }', '', '');"/>
	<c:set var="onDBClick" value="showPerm('${bean.roomAppId}', '${bean.roomId }');"/>
		
	<v3x:column width="4%" align="center" label="<input type='checkbox' onclick='selectAll(this, \"id\")'/>">
		<input type='checkbox' name='id' value="${bean.roomAppId }" roomId="${bean.roomId }" isAllowed="${bean.appStatus }" startDatetime="${bean.startDatetime }" endDatetime="${bean.endDatetime }" roomName="${v3x:toHTML(bean.roomName)}" meetingName="${bean.meetingName }" />
	</v3x:column>
	
	<v3x:column width="13%" type="String" onClick="${onClick }" label="mr.label.meetingroomname" className="cursor-hand sort mxtgrid_black proxy-" alt="${createAccountName}${v3x:toHTML(bean.roomName)}">
		${v3x:toHTML(createAccountName)}${v3x:toHTML(bean.roomName)}
	</v3x:column>
	
	<v3x:column width="9%" type="String" onClick="${onClick }" label="mr.label.appPerson" className="cursor-hand sort" alt="">
		${v3x:toHTML(v3x:showMemberName(bean.appPerId))}
	</v3x:column>
	
	<v3x:column width="10%" type="String" onClick="${onClick }" label="mr.label.appDept" className="cursor-hand sort" alt="">
		${v3x:toHTML(v3x:getDepartment(v3x:getMember(bean.appPerId).orgDepartmentId).name)}
	</v3x:column>
	
	<v3x:column width="20%" type="String" onClick="${onClick }" label="mr.label.meetName" className="cursor-hand sort" alt="${bean.meetingName}">
		<c:out value="${bean.meetingName }" />
	</v3x:column>
	
	<v3x:column width="10%" type="String" onClick="${onClick }" label="mr.label.appTime" className="cursor-hand sort" alt="">
		<c:out value="${bean.appDatetime }" />
	</v3x:column>
	
	<v3x:column width="10%" type="String" onClick="${onClick }" label="mr.label.startDatetime" className="cursor-hand sort" alt="">
		<c:out value="${bean.startDatetime }" />
	</v3x:column>
	
	<v3x:column width="10%" type="String" onClick="${onClick }" label="mr.label.endDatetime" className="cursor-hand sort" alt="">
		<c:out value="${bean.endDatetime }" />
	</v3x:column>
	
	<v3x:column width="7%" type="String" onClick="${onClick }" label="mr.label.checkStatus" className="cursor-hand sort" alt="" nowarp="nowarp">
		<c:out value="${bean.appStatusName }" />
	</v3x:column>
	
	<v3x:column width="7%" type="String" onClick="${onClick }" label="meeting.room.used.status" className="cursor-hand sort" alt="" nowarp="nowarp">
		<c:out value="${bean.usedStatusName }" />
	</v3x:column>
		
</v3x:table>
</form>
</div><!-- center_div_row2 -->

</div><!-- right_div_row2 -->

</div><!-- main_div_row2 -->

<iframe name="hiddenIframe" style="display:none"></iframe>

<script type="text/javascript">
showCondition("${v3x:escapeJavascript(param.condition)}", "${v3x:escapeJavascript(param.textfield)}", "${v3x:escapeJavascript(param.textfield1)}");
initIpadScroll("scrollListDiv",550,870);
showDetailPageBaseInfo("detailFrame", "<fmt:message key='mr.tab.review'/>", [2,3], pageQueryMap.get('count'), _("officeLang.detail_info_meetingroom_pass"));
</script>

</body>
</html>
	