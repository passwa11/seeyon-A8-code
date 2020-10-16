<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title></title>
<%@ include file="header.jsp" %>
<%@ include file="../meeting/include/meeting_taglib.jsp"%>
<%@ include file="../meeting/include/meeting_header.jsp"%>
<v3x:selectPeople id="per" panels="Department" selectType="Member" jsFunction="setBulPeopleFields(elements);" minSize="1" maxSize="1" />

<fmt:formatDate value="${systemNowDatetime }" pattern="yyyy-MM-dd HH:mm" var="systemNowDatetime"/>

<script type="text/javascript" src="<c:url value="/common/js/jquery-debug.js${v3x:resSuffix()}"/>"></script>
<script type="text/javascript" src="<c:url value="/common/js/jquery.json-debug.js${v3x:resSuffix()}"/>"></script>
<script type="text/javascript" src="<c:url value="/apps_res/meeting/js/meetingAjax.js${v3x:resSuffix()}"/>"></script>
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
//zhou:这是预定撤销列表，添加一个参数kfqRead=true
function showDetail(id) {
	var url = "meetingroom.do?method=createPerm&kfqRead=true&id="+id+"&readOnly=true&openWin=openWin&flag=${v3x:escapeJavascript(param.flag)}&listType=listmyapp&from=yesApp";
	parent.detailFrame.location = url;
}

function checkMoreSelect(formNode) {
	var ns = formNode.getElementsByTagName("input");
	var ids = "";
	for(var i = 0; i < ns.length; i++){
		if(ns[i].type == "checkbox" && ns[i].name == "id" && ns[i].checked){
			ids += ns[i].value+",";
		}
	}
	if(ids != ""){
		ids = ids.substring(0,ids.length-1);
		return ids;
	}

	return "";
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

function doCancel() {
	var checkedBoxs = getCheckedbox(document.listForm);
	if(!checkedBoxs || checkedBoxs.length==0) {
		alert("<fmt:message key='mr.alert.pleaseselecttocancel'/>！");
		return false;
	}

	var currentDate = new Date();
	currentDate = currentDate.format("yyyy-MM-dd HH:mm");
	var currentDatetime = Date.parse(currentDate.replace(/\-/g,"/"));

	var meetingName = "";

	for(var i=0; i<checkedBoxs.length; i++) {
		var checkedBox = checkedBoxs[i];

		var roomAppStartDate = checkedBox.getAttribute("startDatetime");
		var roomAppStartDatetime = Date.parse(roomAppStartDate.replace(/\-/g,"/"));

		var roomAppEndDate = checkedBox.getAttribute("endDatetime");
		var roomAppEndDatetime = Date.parse(roomAppEndDate.replace(/\-/g,"/"));

		if(checkedBox.getAttribute("usedStatusDisplay") == "1") {//使用中
			alert("${ctp:i18n('meeting.alert.meetingRoomNoCancle1')}");//该会议室正在使用中，不允许撤销!
			return ;
		}

		if(currentDatetime > roomAppEndDatetime) {
			alert("${ctp:i18n('meeting.alert.meetingRoomNoCancle2')}");//会议室使用时间已过，不允许撤销会议室！
			return ;
		}

		if(meetingName != "") {
			meetingName += "、";
		}
		if(checkedBox.getAttribute("meetingName") != "") {
			meetingName += "《" + checkedBox.getAttribute("meetingName") + "》";
		}
	}

	var cancelMsg = "<fmt:message key='mr.alert.confirmcancel'/>";
	if(meetingName != "") {
		cancelMsg = "<fmt:message key='mr.alert.meetingRoomCancle1'/>"+meetingName+"<fmt:message key='mr.alert.meetingRoomCancle2'/>";
	}
	if(confirm(cancelMsg)) {
		var url = "meeting.do?method=openCancelDialog";
		openMeetingDialog(url, v3x.getMessage("meetingLang.meeting_action_meetingRoom_cancel"), 450, 260, function(rv, canSendSMS) {
			document.listForm["cancelContent"].value = rv;
			document.listForm.action = "meetingroom.do?method=execCancel${ctp:csrfSuffix()}";
			document.listForm.submit();
		});
	}
}

function doFinish() {
	var checkedBoxs = getCheckedbox(document.listForm);
	if(!checkedBoxs || checkedBoxs.length==0) {
		alert("<fmt:message key='meeting.room.used.finish.select.no'/>");
		return false;
	}
	if(checkedBoxs.length > 1) {
		alert("${ctp:i18n('meeting.alert.chooseOnlyOneToEarlyEnd')}");//只能选择一条进行提前结束！
		return false;
	}
	var checkedBox = checkedBoxs[0];
	var meetingName = checkedBoxs[0].getAttribute("meetingName");
	var id = checkedBoxs[0].value;
	if(checkedBox.getAttribute("usedStatusDisplay") == "0") {//未使用
		alert("${ctp:i18n('meeting.alert.meetingroom.noUseToEnd')}");//该会议室未使用，不允许提前结束！
		return;
	} else if(checkedBox.getAttribute("usedStatusDisplay") == "2") {//已使用
		alert("${ctp:i18n('meeting.alert.meetingroom.UseEndToEnd')}");//该会议室已结束使用，不允许提前结束！
		return;
	} else if(checkedBox.getAttribute("usedStatusDisplay") == "3") {//提前结束
		alert("${ctp:i18n('meeting.alert.meetingroom.UseEndToEnd')}");//该会议室已结束使用，不允许提前结束！
		return;
	}

	if(meetingName != "") {
		var message = "${ctp:i18n('meeting.alert.meetingRoomEnd1')}" + meetingName + "${ctp:i18n('meeting.alert.meetingRoomEnd2')}";
		if(confirm(message)) {//该会议室安排了会议《" + meetingName + "》，您确定提前结束该会议室和会议吗？
            meetingAjax.sendAjax({
                managerName : 'meetingAjaxManager',
                managerMethod : 'finishMeetingRoom',
                data : {
                    id : id,
                    isContainMeeting : true
                },
                success : function (result) {
                    if(result && result.message){
                        alert(result.message);
                    }
                    window.location.reload();
                }
            });
		}
	} else {
		if(confirm("${ctp:i18n('meeting.alert.meetingroom.releaseForEarlyEnd')}")) {//提前结束将释放当前会议室，您确定要提前结束使用吗？
            meetingAjax.sendAjax({
                managerName : 'meetingAjaxManager',
                managerMethod : 'finishMeetingRoom',
                data : {
                    id : id,
                    isContainMeeting : false
                },
                success : function (result) {
                    if(result && result.message){
                        alert(result.message);
                    }
                    window.location.reload();
                }
            });
		}
	}

}

function setBulPeopleFields(elements){
	if(elements.length > 0){
		var element = elements[0];
		document.getElementById("perId").value=element.id;
		document.getElementById("perName").value=element.name;
	}
}
function initSearchCondition(){
	<c:if test="${selectCondition!=null}">
		var selectNode = document.getElementById("selectCondition");
		selectNode.selectedIndex = ${selectCondition};
		showCondition(selectNode);
		<c:choose>
			<c:when test="${selectCondition == 1}">
				document.getElementById("name").value = "${v3x:escapeJavascript(textfield)}";
			</c:when>
			<c:when test="${selectCondition == 2}">
				document.getElementById("name").value = "${v3x:escapeJavascript(textfield)}";
				document.getElementById("name1").value = "${v3x:escapeJavascript(textfield1)}";
			</c:when>
		</c:choose>
	</c:if>
}

function doClear(){
	var id = checkSelect(document.listForm);
	if(id != ""){
		var ns = document.listForm.getElementsByTagName("input");
		for(var i = 0; i < ns.length; i++){
			if(ns[i].type == "checkbox" && ns[i].name == "id" && ns[i].checked){
				var isAllowed = document.getElementById("isAllowed_"+ns[i].value).value;
				if(isAllowed == "${MRConstants.Status_App_Wait}"){
					alert("<fmt:message key='mr.alert.cannotclear'/>！");
					return false;
				}
			}
		}
		if(confirm("<fmt:message key='mr.alert.confirmClear'/>")){
			document.listForm.action = "${mrUrl}?method=execClearPerm";
			document.listForm.submit();
		}
	}else{
		alert("<fmt:message key='mr.alert.pleaseselecttoclear'/>！");
		return false;
	}
}

function search_result(){
    var fromDate = document.getElementById("fromDate");
    var toDate = document.getElementById("toDate");
	var beginDate = null!=fromDate ? fromDate.value : "";
	var endDate = null!=toDate ? toDate.value : "";
	if(compareDate(beginDate,endDate)>0){
		alert(v3x.getMessage("officeLang.meetingRoom_time_alert"));
		return ;
	}

	var theForm = document.getElementsByName("searchForm")[0];
	theForm.action="meetingroomList.do?method=listMyApp";

	doSearch();
}
function ChangeEvent(o){
  	var beginDate = document.getElementById("fromDate");
    var endDate = document.getElementById("toDate");
    if(beginDate != null && endDate != null){
		beginDate.value = "";
    	endDate.value = "";
    }
  	showCondition(o);
}

//isValid 人员是否是有效的，换句话说就是是否已经离职，离职不允许弹出人员卡片
function displayPeopleCard(memberId, isValid){
	if(!isValid || isValid == "false"){
		return ;
	}
    showV3XMemberCardWithOutButton(memberId);
}


function _submitCallback(msgType, msg) {
 	if(msgType == "success") {
 		alert(v3x.getMessage("meetingLang.meeting_action_success"));
 		var href = parent.location.href;
 		parent.location.href = href;
 	} else {//同名
 		alert(v3x.getMessage("meetingLang.meeting_action_failed"));
 	}
}

//==================催办会议室审核======================
function doReminders(){
	var checkedBoxs = getCheckedbox(document.listForm);
	if(!checkedBoxs || checkedBoxs.length==0) {
		alert("<fmt:message key='mr.alert.pleaseselecttoreminders'/>！");
		return false;
	}else if(checkedBoxs.length>1){
		alert("<fmt:message key='mr.alert.pleaseselectonereminders'/>！");
		return false;
	}else{
		var checkedBox=checkedBoxs[0];//只能单条催办

		var currentDate=new Date();
		currentDate=currentDate.format("yyyy-MM-dd HH:mm");
		var currentDatetime=Date.parse(currentDate.replace(/\-/g,"/"));

		var roomAppEndDate=checkedBox.getAttribute("endDatetime");
		var roomAppEndDatetime=Date.parse(roomAppEndDate.replace(/\-/g,"/"));

		var appStatus=checkedBox.getAttribute("status");

		if(currentDatetime>roomAppEndDatetime) {
			alert("${ctp:i18n('meeting.alert.meetingRoomNoReminders1')}");//会议室使用时间已过，不允许催办！
			return;
		}
		if(appStatus!=0){
			alert("${ctp:i18n('meeting.alert.meetingRoomNoReminders2')}");//不是待审核状态的数据，不允许催办！
			return;
		}
		var auditIds=checkedBox.getAttribute("auditingIdList");
		var roomAppId=checkedBox.getAttribute("value");
		meetingRoomRemindersAudit(auditIds,roomAppId);
	}
}
/*
 * 催办会议室审核
 * auditIds:审核人ID的集合
 */
function meetingRoomRemindersAudit(auditIds,roomAppId){
	// TODO 判空
	auditIds=auditIds.substring(1,auditIds.length-1);
	var url="meetingroom.do?method=openRemindersDialog&auditIds="+auditIds;
	var buttons=[
	     {
			 text : "<fmt:message key='common.button.ok.label'/>",
			 handler :  function(){
				 var result=getA8Top().winRA.getReturnValue(); // 页面上的信息
				 var obj=getA8Top().$.parseJSON(result);
				 if(obj==null){
					 return;
				 }
				 if(obj.success==true){
					 submitok(result,roomAppId);
				 }
			 }
		 },
         {
			 text : "<fmt:message key='common.button.cancel.label'/>",
			 handler : function(){
				 getA8Top().winRA.close();
			 }
		 }
		]
	openRemindersDialog(url,"<fmt:message key='remindersPeople.label'/>","400","450",buttons,function(){});
}

function submitok(result,roomAppId){
	try{
		var obj=getA8Top().$.parseJSON(result);
		if(obj==null){
			return;
		}
		if(obj.success==true){
			var memberIdArray=obj.memberIdArray; // 要催办的人员的数组
			var content=obj.content; // 催办的附言
			var sendPhoneMessage=obj.sendPhoneMessage; // 催办是否可以发送短信
			// 用form提交催办请求
			document.listForm["remindContent"].value = content;
			document.listForm.action="meetingroom.do?method=execReminders&memberIdArray="+memberIdArray+"&sendPhoneMessage="+sendPhoneMessage+"&roomAppId="+roomAppId;
			document.listForm.submit();
			getA8Top().$.infor(v3x.getMessage("officeLang.meetingRoom_reminders_successful"));
		}
	}catch(e){
		getA8Top().$.error(v3x.getMessage("officeLang.meetingRoom_reminders_failer"));
	}
	getA8Top().winRA.close();
}
//==================催办会议室审核======================

</script>
</head>

<body style="overflow: auto" style="padding: 0px" onload="">

<div class="main_div_row2">

<div class="right_div_row2">

<div class="top_div_row2">

<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
	<td height="22" class="webfx-menu-bar">
		<script type="text/javascript">
			var list = "${v3x:escapeJavascript(param.list)}";
			var myBar = new WebFXMenuBar("${pageContext.request.contextPath}","gray");
			if(list!='noReview'){
				myBar.add(new WebFXMenuButton("create", "<fmt:message key='common.toolbar.cancel.label' bundle='${v3xCommonI18N}'/>", "doCancel()", [3,8]));
			}
			if(list=='noReview'){
				myBar.add(new WebFXMenuButton("modify", "<fmt:message key='mr.button.del'/>", "doClear()", [1,3]));
			}
			myBar.add(new WebFXMenuButton("finishAdvance", "<fmt:message key='meeting.room.used.status.3' />", "doFinish()", [4,3]));
			myBar.add(new WebFXMenuButton("reminders", "<fmt:message key='meeting.room.reminders' />", "doReminders()", [1,2]));
			document.write(myBar);
			document.close();
		</script>
	</td>

	<td class="webfx-menu-bar">
		<form action="" name="searchForm" id="searchForm" method="post" onsubmit="return false" style="margin: 0px">
			<input type="hidden" name="flag" value="${v3x:toHTML(flag)}"/>
			<input type="hidden" name="method" value="listMyApplication"/>
			<input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}" />

			<div class="div-float-right condition-search-div">
				<div class="div-float">
					<select id="condition" name="condition" onChange="ChangeEvent(this)" class="condition" >
						<option value=""><fmt:message key="common.option.selectCondition.text" bundle="${v3xCommonI18N}" /></option>
						<option value="1"><fmt:message key='mr.label.meetingroomname'/></option>
						<option value="2"><fmt:message key='mr.label.appTime'/></option>
						<option value="3"><fmt:message key='mr.label.checkStatus'/></option>
					</select>
				</div>
				<div id="1Div" class="div-float hidden">
					<input type="text" name="textfield" class="textfield" />
				</div>
				<div id="2Div" class="div-float hidden">
					<input type="text" name="textfield" id="fromDate" class="input-date cursor-hand" onclick="whenstart('${pageContext.request.contextPath}',this,575,140);" readonly />
					-
					<input type="text" name="textfield1" id="toDate" class="input-date cursor-hand" onclick="whenstart('${pageContext.request.contextPath}',this,575,140);" readonly />
				</div>
				<div id="3Div" class="div-float hidden">
					<select name="textfield">
						<option value="0,1,2"><fmt:message key="meeting.options.check.status0"></fmt:message></option>
						<option value="0"><fmt:message key="meeting.room.app.status.0"/></option>
						<option value="1"><fmt:message key="meeting.room.app.status.1"/></option>
						<option value="2"><fmt:message key="meeting.room.app.status.2"/></option>
					</select>
				</div>
				<div onclick="search_result();" class="condition-search-button div-float"></div>
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
<input type="hidden" id="remindContent" name="remindContent" value="false" />

<v3x:table htmlId="listTable" data="list" var="vo" pageSize="${pageSize }" size="${size }" showHeader="true" showPager="true" isChangeTRColor="true">

<v3x:column width="4%" align="center" label="<input type='checkbox' onclick='selectAll(this, \"id\")'/>">
	<input type='checkbox' name='id' value="${vo.roomAppId }" usedStatusDisplay="${vo.usedStatusDisplay }" status='${vo.appStatus}' meetingId="${vo.meetingId }" meetingName="${vo.meetingName }" startDatetime="${vo.startDatetime }" endDatetime="${vo.endDatetime }"  auditingIdList="${vo.auditingIdList }" />
</v3x:column>

<v3x:column width="28%" type="String" onClick="showDetail('${vo.roomAppId }')" label="mr.label.meetingroomname" alt="${v3x:toHTML(vo.roomName)}" className="cursor-hand sort mxtgrid_black">
	<c:out value="${vo.roomName }" />
</v3x:column>

<v3x:column width="10%" type="String" label="mr.label.admin" className="cursor-hand sort" alt="">
	<c:forEach var="admin" items="${vo.auditingIdList}">
		<c:set value="${v3x:getMember(admin).isValid}" var="memberIsValid"></c:set>
		<a class="click-link"  href="javascript:displayPeopleCard('${admin}', '${memberIsValid }')"><c:out value="${v3x:showOrgEntitiesOfIds(admin, 'Member', '')}"/></a>
	</c:forEach>
</v3x:column>

<v3x:column width="12%" type="String" onClick="showDetail('${vo.roomAppId }')" label="mr.label.meetName" className="cursor-hand sort" alt="${vo.meetingName}">
	<c:out value="${vo.meetingName }" />
</v3x:column>

<v3x:column width="10%" type="String" onClick="showDetail('${vo.roomAppId }')" label="mr.label.appTime" className="cursor-hand sort" alt="">
	<c:out value="${vo.appDatetime }" />
</v3x:column>

<v3x:column width="10%" type="String" onClick="showDetail('${vo.roomAppId }')" label="mr.label.startDatetime" className="cursor-hand sort" alt="">
	<c:out value="${vo.startDatetime }" />
</v3x:column>

<v3x:column width="10%" type="String" onClick="showDetail('${vo.roomAppId }')" label="mr.label.endDatetime" className="cursor-hand sort" alt="${vo.endDatetime }">
	<c:out value="${vo.endDatetime }" />
</v3x:column>

<v3x:column width="8%" type="String" onClick="showDetail('${vo.roomAppId }')" label="mr.label.checkStatus" className="cursor-hand sort" alt="${vo.startDatetime }">
	<c:out value="${vo.appStatusName }" />
</v3x:column>

<v3x:column width="8%" type="String" onClick="showDetail('${vo.roomAppId }')" label="meeting.room.used.status" className="cursor-hand sort" alt="${vo.usedStatusName }">
	<c:out value="${vo.usedStatusName }" />
</v3x:column>

</v3x:table>

</form>

</div><!-- center_div_row2 -->

</div><!-- right_div_row2 -->

</div><!-- main_div_row2 -->

<iframe name="hiddenIframe" style="display:none"></iframe>

<script type="text/javascript">
showCondition("${v3x:escapeJavascript(param.condition)}", "<v3x:out value='${param.textfield}' escapeJavaScript='true' />", "<v3x:out value='${param.textfield1}' escapeJavaScript='true' />");
initIpadScroll("scrollListDiv",550,870);
showDetailPageBaseInfo("detailFrame", "<fmt:message key='mr.tab.review'/>", [2,3], pageQueryMap.get('count'), _("officeLang.detail_info_meetingroom_pass"));
</script>

</body>

</html>
