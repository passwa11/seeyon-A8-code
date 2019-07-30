<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="../header.jsp" %>
<%@ include file="../../common/INC/noCache.jsp"%>
<fmt:setBundle basename="com.seeyon.v3x.hr.resource.i18n.HRResources" var="v3xHRI18N"/>
<fmt:setBundle basename="com.seeyon.v3x.localeselector.resources.i18n.LocaleSelectorResources" var="localeI18N"/>
<fmt:setBundle basename="com.seeyon.v3x.organization.resources.i18n.OrganizationResources" var="V3xOrgI18N"/>
<fmt:setBundle basename="com.seeyon.apps.index.resource.i18n.IndexResources" var="indexResources"/>
<fmt:setBundle basename="com.seeyon.v3x.common.resources.i18n.SeeyonCommonResourc" var="commonResources"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/v3xmain/js/shortcut.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/hr/js/string.extend.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/v3xmain/js/personalInfo.js${v3x:resSuffix()}" />"></script>
<title>Insert title here</title>
<script type="text/javascript">
var personalBindURL='${personalBindURL}';
var smsPluginEnable="${smsSetEnable=='true'}";
var mailPluginEnable="${v3x:hasPlugin('webmail') && emailSetEnable=='true'}";

<%--国际化--%>
var bindphonenumMsg="<fmt:message key='personalInfo.track.bindphonenum' bundle='${v3xHRI18N}'/>";
var submitverificationcodeMsg="<fmt:message key='personalInfo.bind.submitverificationcode' bundle='${v3xHRI18N}'/>";
var nosmspluginMsg="<fmt:message key='personalInfo.bind.nosmsplugin' bundle='${v3xHRI18N}'/>";
var cancellabelMsg="<fmt:message key='common.button.cancel.label' bundle='${v3xHRI18N}'/>";
var oklabelMsg="<fmt:message key='common.button.ok.label' bundle='${v3xHRI18N}'/>";
var determinebindMsg="<fmt:message key='personalInfo.bind.determinebind' bundle='${v3xHRI18N}'/>";
var changenumMsg="<fmt:message key='personalInfo.bind.changenum' bundle='${v3xHRI18N}'/>";
var bindemailMsg="<fmt:message key='personalInfo.track.bindemail' bundle='${v3xHRI18N}'/>";
var noemailpluginMsg="<fmt:message key='personalInfo.bind.noemailplugin' bundle='${v3xHRI18N}'/>";
var changeemailMsg="<fmt:message key='personalInfo.bind.changeemail' bundle='${v3xHRI18N}'/>";
var releaseboundMsg="<fmt:message key='personalInfo.bind.releasebound'  bundle='${v3xHRI18N}'/>";
var immediatebindingMsg="<fmt:message key='personalInfo.bind.immediatebinding'  bundle='${v3xHRI18N}'/>";
var numberformaterrorMsg="<fmt:message key='personalInfo.bind.numberformaterror'  bundle='${v3xHRI18N}'/>";
var emailformaterrorMsg="<fmt:message key='personalInfo.bind.emailformaterror'  bundle='${v3xHRI18N}'/>";
var failureMsg="<fmt:message key='personalInfo.bind.failure'  bundle='${v3xHRI18N}'/>";
var supportsdigitalMsg="<fmt:message key='addressbook.fieldset.supportsdigital'  bundle='${v3xHRI18N}'/>";

</script>
<script type="text/javascript">
v3x.loadLanguage("/apps_res/hr/js/i18n");
<%--更新TOP是否播放声音--%>
getA8Top().isEnableMsgSound = ${systemMsgSoundEnable&&(enableMsgSound=='true')};
<%--更新Top消息查看后是否关闭--%>
getA8Top().msgClosedEnable = ${msgClosedEnable!='false'?true:false};
<%--更新TOP人员头像--%>
try {
	if (getA8Top().curUserPhoto) {
		getA8Top().curUserPhoto = "${v3x:avatarImageUrl(CurrentUser.id)}";
	}
	getA8Top().memberImageUrl = "${v3x:avatarImageUrl(CurrentUser.id)}";
	getA8Top().$(".avatar > img").attr("src", getA8Top().memberImageUrl);
} catch(e) {}
function disableSubmitButton() {
	document.getElementById("submitButton").disabled=true;
	return true;
}
function save(){
    document.getElementById("submitButton").click();
}
function cancel(){
    getA8Top().back();
}
//头像设置窗口
function setHead(){
	try{
		var evt = v3x.getEvent();
		var filename = document.getElementById("image1").src;
		getA8Top().setHeadWind = getA8Top().$.dialog({
            title:"<fmt:message key='hr.staffInfo.selfSet.label' bundle='${v3xHRI18N}'/>",
            transParams:{'parentWin':window},
            url: "genericController.do?ViewPage=personalAffair/person/setHead&from=newWindow&filename=" + encodeURIComponent(filename),
            width: 670,
            height: 335,
            isDrag:false
        });
	}catch(e){}
}

/**
 * 设置头像回调函数
 */
function setHeadCollback(returnValue) {
	getA8Top().setHeadWind.close();
	if( returnValue != undefined){
       var returns = returnValue.split(",");
       document.getElementById("filename").value = returns[0];
       document.getElementById("thePicture").innerHTML = "<img class='radius' id='image1' src='" + returns[1] + "' width='95' height='95' onclick='setHead();' style='cursor:pointer;'>";
    }
}

function smsLoginEnableOnclickHandler(_this){
  var checked = $(_this).parent().parent().find("input").attr("value");
  if(checked){
    var telNumber = $("#telNumber").val();
    if(telNumber.replace(/^\s+|\s+$/gm, "") == ""){
      alert(v3x.getMessage("HRLang.systemswitch_telnumberset_prompt_notNull"));
      $("#telNumber").focus();
    } else {
      var requestCaller = new XMLHttpRequestCaller(this, "ajaxPortalManager", "checkTelNumber", false);
      requestCaller.addParameter(1, "String", v3x.getMessage("HRLang.systemswitch_telnumbercheck_prompt"));
      var rs = requestCaller.serviceRequest();
      if(rs == "success"){
        var r = confirm(v3x.getMessage("HRLang.systemswitch_smsloginenable_prompt"));
        if(!r){
          $("#smsLoginEnable").removeClass("checked");
          $("#smsLoginEnableHidden").attr("value","");
        }
      } else {
        $("#smsLoginEnable").removeClass("checked");
        $("#smsLoginEnableHidden").attr("value","");
        alert(rs);
      }
    }
  }
}

function telNumberOnBlurHandler(element){
  var oldValue = $("#telNumber").attr("defValue");
  var telNumber = $("#telNumber").val();
  var checked = $("#smsLoginEnable").attr("checked");
  if(checked && $.trim(telNumber) == ""){
    alert(v3x.getMessage("HRLang.systemswitch_telnumbercheck_prompt") + "," + v3x.getMessage("HRLang.systemswitch_telnumberset_prompt"));
    $("#smsLoginEnable").removeClass("checked");
    $("#smsLoginEnableHidden").attr("value","");
    $("#telNumber").focus();
    return;
  }
  if(isPhoneNumber(element) && maxLength(element)){
    if(checked && oldValue != telNumber){
      var requestCaller = new XMLHttpRequestCaller(this, "ajaxPortalManager", "checkTelNumber", false);
      requestCaller.addParameter(1, "String", v3x.getMessage("HRLang.systemswitch_telnumberModify_prompt"));
      var rs = requestCaller.serviceRequest();
      if(rs == "success"){
        var r = confirm(v3x.getMessage("HRLang.systemswitch_smsloginenable_prompt"));
        if(!r){
          $("#smsLoginEnable").removeClass("checked");
          $("#smsLoginEnableHidden").attr("value","");
        }
      } else {
        $("#smsLoginEnable").removeClass("checked");
        $("#smsLoginEnableHidden").attr("value","");
        alert(rs);
      }
    }
    $("#telNumber").attr("defValue", telNumber);
  } else {
    $("#telNumber").focus();
  }
}

//修改mail和num的disable为false，使得后台能够取得有效的值
function bindenable(){
	 var email= document.getElementById("email");
	 email.disabled=false;
	 var telNumber=document.getElementById("telNumber");
	 telNumber.disabled=false;
	 return true;
}

function validatenum(obj){
	var customerNumValue=obj.value;
	if (customerNumValue != '') {
        var reg = /^([0-9]{0,8})([.][0-9]{1,4})?$/;
        var r = customerNumValue.match(reg);
        if(r==null) {
        	alert(supportsdigitalMsg);
        	obj.value="";
        }
	}
}
	window.onload = function(){
		document.getElementById('portrat_div').style.height = parent.document.getElementById('personalSetContent').clientHeight - 1 + "px";
		document.getElementById('input_div').style.height = parent.document.getElementById('personalSetContent').clientHeight + "px";
		document.getElementById('check_div').style.height = parent.document.getElementById('personalSetContent').clientHeight + "px";
		if(document.body.clientWidth < getA8Top().innerWidth - 60){
			document.getElementById("portrat_div").style.width = "230px";
			document.getElementById("portrat_div").parentNode.style.maxWidth = "1203px";
			document.getElementById("portrat_div").parentNode.style.width = "1203px";
			document.body.style.minWidth = "1204px";
			document.getElementById("check_div").style.width = "346px";
			for(var i = 0;i < $(".msgClosedEnable_right").length;i++){
				$(".msgClosedEnable_right").eq(i).css({"width":"268px"})
			}
		}
		else{
			document.getElementById("portrat_div").style.width = "305px";
			document.getElementById("portrat_div").parentNode.style.maxWidth = "1344px";
			document.getElementById("portrat_div").parentNode.style.width = "1344px";
			document.body.style.minWidth = "1345px";
			document.getElementById("check_div").style.width = "412px";
			for(var i = 0;i < $(".msgClosedEnable_right").length;i++){
				$(".msgClosedEnable_right").eq(i).css({"width":"335px"})
			}
		}

		var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
		var isOpera = userAgent.indexOf("Opera") > -1;
		if (isOpera) {
			return "Opera"
		} //判断是否Opera浏览器
		if (userAgent.indexOf("Firefox") > -1) {
			return "FF";
		} //判断是否Firefox浏览器
		if (userAgent.indexOf("Chrome") > -1){
			return "Chrome";
		}
		if (userAgent.indexOf("Safari") > -1) {
			return "Safari";
		} //判断是否Safari浏览器
		if (userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1 && !isOpera) {
			$("#lag_select").css({"width":"321px"});
			$("#tip_textarae").css({"width":"321px"});
			if(userAgent.indexOf("MSIE 8")){
				$("#lag_select").css({"width":"328px"});
				$("#tip_textarae").css({"width":"321px"});
			}
			if(userAgent.indexOf("MSIE 10")){
				$("#lag_select").css({"width":"321px"});
				$("#tip_textarae").css({"width":"321px"});
			}
			if(userAgent.indexOf("MSIE 9")){
				$("#lag_select").css({"width":"321px"});
				$("#tip_textarae").css({"width":"321px"});
			}
		} //判断是否IE8 9 10浏览器
		if((userAgent.toLowerCase().indexOf("trident") > -1 && userAgent.indexOf("rv") > -1)){
			$("#lag_select").css({"width":"321px"});
			$("#tip_textarae").css({"width":"321px"});
		}//判断是否IE11浏览器
	}

	function checkbox_click(_this){
		if(!$(_this).hasClass("disabled")){
			if($(_this).hasClass("checked")){
				$(_this).removeClass("checked");
				$(_this).parent().parent().find("input").attr("value","");
			}else{
				$(_this).addClass("checked");
				$(_this).parent().parent().find("input").attr("value","true");
			}
		}
	}

	// function checkTelNumber_Checked(){
 //  		if($('input[name="smsLoginEnable"]').val() == "true"){
 //    		var telNumber = $("#telNumber").val();
 //    		if(telNumber == ""){
 //      			alert(v3x.getMessage("HRLang.systemswitch_telnumberset_prompt_notNull"));
 //      			$("#telNumber").focus();
 //      			return false;
 //      		}
 //    	}
 //    	return true;
	// }
$(function(){
	if ($.browser.msie && $.browser.version <= 9) {
		$("#input_div").children(":first").css({"width":"480px"});
    }
})
</script>
<style type="text/css">
.min_button{
	width: 70px;
	height: 25px;
	line-height: 25px;
	background: #6eb0fc;
	text-align: center;
	color: #fff;
	display: inline-block;
	margin-left: 10px;
	margin-top: 2px;
	border-radius:4px;
}
.min_button:hover{
	color: #fff;
}
.select-per{
	width: 79%;
}
.checkbox{
	display: inline-block;
	width: 17px;
	height: 17px;
	border: 1px solid #dfdfdf;
	position: relative;
	top: 5px;
	margin-right: 5px;
}
.checked{
	border: 0;
	width: 19px;
	height: 19px;
	background: url(apps_res/personal/img/choosed.png);
}
.hand{
	margin-top:6px;
}
#input_div{
	width:625px;float:left;overflow-x:hidden;overflow-y:scroll;
}
</style>
</head>
<body style="overflow: hidden;">
<form method="get" action="${personalAffairURL}" onsubmit="return (checkForm(this) && disableSubmitButton() && bindenable());">
	<input type="hidden" name="method" value="updatePersonalInfo">
	<input type="hidden" name="memberId" id="memberId" value="${member.id}">

	<div style="margin:0px;background-color:#fafafa;">
		<div style="margin: 0 auto;">
			<div id="portrat_div" style="width:305px;max-width:305px;min-width:230px;background-color:#eaf5f9;float:left;border-top: 1px #fafafa solid;">
			<div style="width:100%;margin:0 auto;">
			<div id="thePicture" style="width: 100%; text-align: center;margin-top:90px;">
			<c:if test="${isAllowUpdateAvatarEnable}">
			<img class="radius" id="image1" src="${fileName}" width="95" height="95" onclick="setHead()" style="cursor:pointer;"/>
			</c:if>
			<c:if test="${!isAllowUpdateAvatarEnable}">
			<img class="radius" id="image1" src="${fileName}" width="95" height="95" style="cursor:pointer;"/>
			</c:if>
			<c:set value="${selfImage}" var="selfImage" />
			</div>
			</div>
			<div style="width:100%;text-align: center;margin-top:20px;">
			<input type="hidden" id="filename" name="filename" value="${selfImage}">

			<!-- 判断是否允许修改--不允许置灰 -->
			<c:if test="${isAllowUpdateAvatarEnable}">
			<input style="width: 70px;height: 25px;background-color: #6EB0FC;border: none;color: white;cursor:pointer;border-radius:4px;overflow: hidden; white-space: nowrap; text-overflow: ellipsis;" type="button" size="100" value="<fmt:message key="hr.staffInfo.changeself.label" bundle="${v3xHRI18N}" />" onclick="setHead()">
			</c:if>
			<c:if test="${!isAllowUpdateAvatarEnable}">
			<input disabled style="width: 70px;height: 25px;background-color:rgb(238, 238, 238);border: none;color: rgb(177, 177, 177);cursor:pointer;border-radius:4px;overflow: hidden; white-space: nowrap; text-overflow: ellipsis;" type="button" size="100" value="<fmt:message key="hr.staffInfo.changeself.label" bundle="${v3xHRI18N}" />" >
			</c:if>
			</div>
			</div>

			<div id="input_div">
			<div style="width:461px;margin:0 auto;">
			<div style="width:100%;margin-top:25px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.name.label" bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;background-color:#eeeeee;color:#b1b1b1;" type="text" id="n" name="n" disabled size="70" value="${member.name}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'"/>
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.memberno.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;background-color:#eeeeee;color:#b1b1b1;" type="text" id="no" name="no" disabled size="70" value="${member.code}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'"/>
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<c:if test="${v3x:hasPlugin('i18n')}">
				<c:choose>
					<c:when test="${v3x:getSysFlagByName('i18n_onlyCN') == true}">
						<input name="primaryLanguange" type="hidden" value="zh_CN">
					</c:when>
					<c:otherwise>
						<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
						<span><fmt:message key="org.member_form.primaryLanguange.label"  bundle="${V3xOrgI18N}"/></span>
						</div>
						<div style="width:326px;float:left;">
						<select id="lag_select" style="width:326px;height: 28px;border: 1px solid #e4e4e4;" name="primaryLanguange" class="select-per">
						<c:forEach items="${v3x:getAllLocales()}" var="language">
							<c:if test="${v3x:getSysFlagByName('sys_isGovVer') != 'true' || language != 'en'}">
								<option value="${language}" ${orgLocale==language ? "selected" : ""}><fmt:message key="localeselector.locale.${language}" bundle="${localeI18N}"/></option>
							</c:if>
						</c:forEach>
						</select>
						</div>
						<div style="width:75px;float:left;"></div>
						<div style="clear:both;"></div>
					</c:otherwise>
				</c:choose>
			</c:if>
			</div>
			<!--首选时区  -->
			<c:if test="${timeZoneEnable==true || timeZoneEnable=='true'}">
				<div style="width:100%;margin-top:10px;">
					<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
					<span><fmt:message key="timezone.msg.preferred"  bundle="${commonResources}"/></span>
					</div>
					<div style="width:326px;float:left;">
					<select id="lag_select" style="width:322px;height: 28px;border: 1px solid #e4e4e4;" name="timezone" class="select-per">
					<c:forEach items="${timeZoneList}" var="timezone">
							<option value="${timezone.id}" ${currentTimeZone==timezone.id ? "selected" : ""}>${timezone.showvalue}</option>
					</c:forEach>
					</select>
					</div>
					<div style="width:75px;float:left;"></div>
					<div style="clear:both;"></div>
				</div>
			</c:if>

			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.workTelephone.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input disabled style="width:321px;padding-left:5px;height: 28px;background-color:#eeeeee;color:#b1b1b1;" type="text" id="telephone" name="telephone" maxlength="66" inputName="<fmt:message key="hr.staffInfo.workTelephone.label"  bundle="${v3xHRI18N}"/>" validate="maxLength" size="70" value="${v3x:toHTML(member.officeNum)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.mobileTelephone.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input disabled style="width:321px;padding-left:5px;height: 28px;background-color:#eeeeee;color:#b1b1b1;" type="text" id="telNumber" name="telNumber" onblur="if(v3x.isMSIE){telNumberOnBlurHandler(this);}this.style.borderColor = '#e4e4e4'"
			defValue="<c:out value="${v3x:toHTML(member.telNumber)}" escapeXml="true"/>" maxLength="70" size="70"
			inputName="<fmt:message key="hr.staffInfo.mobileTelephone.label"  bundle="${v3xHRI18N}"/>" validate="isPhoneNumber,maxLength" value="<c:out value="${member.telNumber}" escapeXml="true"/>"
			${(smsSetEnable=='true' && bindphonenumberEnable=='true')? 'disabled':''} onfocus="this.style.borderColor = '#68BAFF'" />
			</div>
			<div style="width:75px;float:left;">
				<input type="hidden" name="bindphonenumberEnable" id="bindphonenumberEnable" value="${bindphonenumberEnable}">
				<c:if test="${smsSetEnable=='true'}">
					<a href="javascript:bindphonenumberchange();" class="min_button" id="bindnum">
					<c:choose>
						<c:when test="${bindphonenumberEnable=='true'}">
							<fmt:message key="personalInfo.bind.releasebound"  bundle="${v3xHRI18N}"/>
						</c:when>
						<c:otherwise>
							<fmt:message key="personalInfo.bind.immediatebinding"  bundle="${v3xHRI18N}"/>
						</c:otherwise>
					</c:choose>
					</a>
				</c:if>
			</div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.address.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="address" name="address" maxlength="70" size="70" value="${v3x:toHTML(member.address)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'"/>
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.postalcode.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="postalcode" name="postalcode" maxlength="70" size="70" value="${v3x:toHTML(member.postalcode)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.emailDetail.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="email" name="email" maxlength="70" size="70"
			value="${v3x:toHTML(member.emailAddress)}" inputName="<fmt:message key='hr.staffInfo.emailDetail.label'  bundle='${v3xHRI18N}'/>"
			maxSize="40" maxLength="40" validate="validEmail,maxLength"
			${(v3x:hasPlugin('webmail') && emailSetEnable=='true' && bindemailEnable=='true')? 'disabled':''} onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'"/>
			</div>
			<div style="width:75px;float:left;">
				<input type="hidden" name="bindemailEnable" id="bindemailEnable" value="${bindemailEnable}">
				<c:if test="${v3x:hasPlugin('webmail') && emailSetEnable=='true'}">
					<a href="javascript:bindemailchange();" class="min_button" id="bindemail">
					<c:choose>
						<c:when test="${bindemailEnable=='true'}">
							<fmt:message key="personalInfo.bind.releasebound"  bundle="${v3xHRI18N}"/>
						</c:when>
						<c:otherwise>
							<fmt:message key="personalInfo.bind.immediatebinding"  bundle="${v3xHRI18N}"/>
						</c:otherwise>
					</c:choose>
					</a>
				</c:if>
			</div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.communication.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="communication" name="communication" maxlength="70" size="70" value="${v3x:toHTML(member.postAddress)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.wb.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="wb" name="wb" maxlength="70" size="70" value="${v3x:toHTML(member.weibo)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.wx.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="wx" name="wx" maxlength="70" size="70" value="${v3x:toHTML(member.weixin)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.website.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="website" name="website" maxlength="70" size="70" value="${v3x:toHTML(member.website)}" inputName="<fmt:message key="hr.staffInfo.website.label"  bundle="${v3xHRI18N}"/>" validate="maxLength" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<div style="width:100%;margin-top:10px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.staffInfo.blog.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="blog" name="blog" maxlength="70" size="70" inputName="<fmt:message key="hr.staffInfo.blog.label"  bundle="${v3xHRI18N}"/>" validate="maxLength" value="${v3x:toHTML(member.blog)}" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" />
			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			<c:forEach items="${bean}" var="bean" varStatus="status">
				<!--文本类型  -->
				<c:if test="${bean.type == 0}">
					<div style="width:100%;margin-top:10px;">
					<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
					<span>${v3x:toHTML(bean.label)}</span>
					</div>
					<div style="width:326px;float:left;">
					<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="${bean.id}" name="${bean.id}" maxlength="70" size="70" validate="maxLength" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'"/>
					</div>
					<div style="width:75px;float:left;"></div>
					<div style="clear:both;"></div>
					</div>

					<%--<tr>--%>
					<%--<td class="bg-gray" nowrap="nowrap" >--%>
					<%--${v3x:toHTML(bean.label)}:--%>
					<%--</td>--%>
					<%--<td class="new-column" nowrap="nowrap" >--%>
					<%--<input type="text" id="${bean.id}" name="${bean.id}" size="70" maxLength="70" value="" validate="maxLength"/>--%>
					<%--</td>--%>
					<%--</tr>--%>
				</c:if>

				<!--数字类型  -->
				<c:if test="${bean.type == 1}">
					<div style="width:100%;margin-top:10px;">
					<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
					<span>${v3x:toHTML(bean.label)}</span>
					</div>
					<div style="width:326px;float:left;">
					<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="${bean.id}" name="${bean.id}" maxlength="13" size="70" validate="maxLength" onblur="validatenum(this);this.style.borderColor = '#e4e4e4'" onfocus="this.style.borderColor = '#68BAFF'"/>
					</div>
					<div style="width:75px;float:left;"></div>
					<div style="clear:both;"></div>
					</div>

					<%--<tr>--%>
					<%--<td class="bg-gray" nowrap="nowrap" >--%>
					<%--${v3x:toHTML(bean.label)}:--%>
					<%--</td>--%>
					<%--<td class="new-column" nowrap="nowrap" >--%>
					<%--<input type="text" id="${bean.id}" name="${bean.id}" value="" class="validate font_size12" onblur="validatenum(this);" maxlength="13" size="70" />--%>
					<%--</td>--%>
					<%--</tr>--%>
				</c:if>

				<!--日期类型  -->
				<c:if test="${bean.type == 2}">
					<div style="width:100%;margin-top:10px;">
					<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
					<span>${v3x:toHTML(bean.label)}</span>
					</div>
					<div style="width:326px;float:left;">
					<input style="width:321px;padding-left:5px;height: 28px;" type="text" id="${bean.id}" name="${bean.id}" maxlength="250" onClick="whenstart('${pageContext.request.contextPath}', this, 175, 140);" readonly="readonly"  value="" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'"/>
					</div>
					<div style="width:75px;float:left;"></div>
					<div style="clear:both;"></div>
					</div>

					<%--<tr>--%>
					<%--<td class="bg-gray" nowrap="nowrap" >--%>
					<%--${v3x:toHTML(bean.label)}:--%>
					<%--</td>--%>
					<%--<td class="new-column" nowrap="nowrap">--%>
					<%--<input id="${bean.id}" name="${bean.id}" type="text" maxlength="250" class="select-per" onClick="whenstart('${pageContext.request.contextPath}', this, 175, 140);" readonly="readonly"  value=""    />--%>
					<%--</td>--%>
					<%--</tr>--%>
				</c:if>
			</c:forEach>

			<c:forEach items="${beanValue}" var="beanValue">
				<script type="text/javascript">
				var key='${beanValue.key}';
				var beanValueStr = "${v3x:escapeJavascript(beanValue.value)}";
				$("#"+key).val("" + beanValueStr);
				</script>
			</c:forEach>
			<div style="width:100%;margin-top:10px;margin-bottom:25px;">
			<div style="width:53px;float:left;padding-top:7px;text-align: right;margin-right:7px;">
			<span><fmt:message key="hr.record.remark.label"  bundle="${v3xHRI18N}"/></span>
			</div>
			<div style="width:326px;float:left;">
			<textarea id="tip_textarae" style="width:328px;padding-left:5px;border: 1px solid #e4e4e4;"  maxlength="500" validate="maxLength" maxSize="500" rows="5" cols="68" name="comment" id="comment"  inputName="<fmt:message key="hr.record.remark.label"  bundle="${v3xHRI18N}"/>" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'">${v3x:toHTMLAlt(member.description)}</textarea>

			</div>
			<div style="width:75px;float:left;"></div>
			<div style="clear:both;"></div>
			</div>
			</div>
			</div>

			<div id="check_div" style="float:left;border-left: 1px #EDEDED solid;">
			<div style="margin-left:50px;margin-top:25px;">
			<c:if test="${isCanUseSMS}">
				<div for="smsLoginEnable">
				<div style="width:20px;float:left;">
					<em id="smsLoginEnable" class="checkBox ${smsLoginEnable != 'true'? '':'checked'}" onclick="checkbox_click(this);smsLoginEnableOnclickHandler(this)"></em>
				</div>
				<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
					<span class="hand"><fmt:message key="systemswitch.smsLoginEnable.lable" bundle="${v3xHRI18N}" /></span>
					<input id="smsLoginEnableHidden" name="smsLoginEnable" value="${smsLoginEnable != 'true'? '':'true'}" type="hidden"/>
				</div>
				<div style="clear:both;"></div>
				<%--<input id="smsLoginEnable" name="smsLoginEnable" type="checkbox" value="true" onclick="smsLoginEnableOnclickHandler(this);" ${smsLoginEnable != 'true'? '':'checked'} >--%>
				<%--<fmt:message key="systemswitch.smsLoginEnable.lable" bundle="${v3xHRI18N}" />--%>
				</div>
			</c:if>
			</div>
			<div style="margin-left:50px;margin-top:10px;">
			<div for="msgSoundEnable">
			<div style="width:20px;float:left;">
				<em id="msgSoundEnable" class="checkBox ${systemMsgSoundEnable&&(enableMsgSound=='true')? 'checked':''} ${systemMsgSoundEnable? '':'disabled'}" onclick="checkbox_click(this)"></em>
			</div>
			<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
				<span class="hand"><fmt:message key="systemswitch.MsgHint.lable"/></span>
				<input name="enableMsgSound" value="${systemMsgSoundEnable&&(enableMsgSound=='true')? 'true':''}" type="hidden"/>
			</div>
			<div style="clear:both;"></div>
			<%--<input id="msgSoundEnable" name="enableMsgSound" type="checkbox" value="true" ${systemMsgSoundEnable&&(enableMsgSound=='true')? 'checked':''} ${systemMsgSoundEnable? '':'disabled'}>--%>
			<%--<fmt:message key="systemswitch.MsgHint.lable"/>--%>
			</div>
			</div>
			<div style="margin-left:50px;margin-top:10px;">
			<div for="msgClosedEnable">
			<div style="width:20px;float:left;">
				<em id="msgClosedEnable" class="checkBox ${msgClosedEnable!='false'? 'checked':''}" onclick="checkbox_click(this)"></em>
			</div>
			<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
				<span class="hand"><fmt:message key="systemswitch.MsgAfterLook.lable"/></span>
				<input name="msgClosedEnable" value="${msgClosedEnable!='false'? 'true':''}" type="hidden"/>
			</div>
			<div style="clear:both;"></div>
			<%--<input id="msgClosedEnable" name="msgClosedEnable" type="checkbox" value="true" ${msgClosedEnable!='false'? 'checked':''} >--%>
			<%--<fmt:message key="systemswitch.MsgAfterLook.lable"/>--%>
			</div>
			</div>
			<c:if test="${v3x:hasPlugin('index')}">
				<div style="margin-left:50px;margin-top:10px;">
				<c:if test="${not empty isShowIndexSummary&&isShowIndexSummary=='false'}">
					<c:set value="true" var="setIndexSummary"/>
				</c:if>
				<div for="isShowIndexSummary">
				<div style="width:20px;float:left;">
					<em id="isShowIndexSummary" class="checkBox ${isShowIndexSummary=='false'? 'checked':''}" onclick="checkbox_click(this)"></em>
				</div>
				<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
					<span class="hand"><fmt:message key="index.com.seeyon.v3x.index.showSummary.notshow" bundle="${indexResources }"/></span>
					<input name="isShowIndexSummary" value="${isShowIndexSummary=='false'? 'true':''}" type="hidden"/>
				</div>
				<div style="clear:both;"></div>
				<%--<input id="isShowIndexSummary" name="isShowIndexSummary" type="checkbox" value="true" ${isShowIndexSummary=='false'? 'checked':''} >--%>
				<%--<fmt:message key="index.com.seeyon.v3x.index.showSummary.notshow" bundle="${indexResources }"/>--%>
				</div>
				</div>
			</c:if>
			<div style="margin-left:50px;margin-top:10px;">
			<div for="extendConfig">
			<div style="width:20px;float:left;">
				<em id="extendConfig" class="checkBox ${extendConfig!='false'? 'checked':''}" onclick="checkbox_click(this)"></em>
			</div>
			<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
				<span class="hand"><fmt:message key="personalInfo.extend"/></span>
				<input name="extendConfig" value="${extendConfig!='false'? 'true':''}" type="hidden"/>
			</div>
			<div style="clear:both;"></div>
			<%--<input id="extendConfig" name="extendConfig" type="checkbox" value="true" ${extendConfig!='false'? 'checked':''} >--%>
			<%--<fmt:message key="personalInfo.extend"/>--%>
			</div>
			</div>
			<div style="margin-left:50px;margin-top:10px;">
			<div for="tracksend">
			<div style="width:20px;float:left;">
				<em id="tracksend" class="checkBox ${tracksend!='false'? 'checked':''}" onclick="checkbox_click(this)"></em>
			</div>
			<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
				<span class="hand"><fmt:message key="personalInfo.track.send"/></span>
				<input name="tracksend" value="${tracksend!='false'? 'true':''}" type="hidden"/>
			</div>
			<div style="clear:both;"></div>
			<%--<input id="tracksend" name="tracksend" type="checkbox" value="true" ${tracksend!='false'? 'checked':''} >--%>
			<%--<fmt:message key="personalInfo.track.send"/>--%>
			</div>
			</div>
			<div style="margin-left:50px;margin-top:10px;">
			<div for="trackprocess">
			<div style="width:20px;float:left;">
				<em id="trackprocess" class="checkBox ${trackprocess!='true'? '':'checked'}" onclick="checkbox_click(this)"></em>
			</div>
			<div class="msgClosedEnable_right" style="width:335px;float:right;margin-top:5px;">
				<span class="hand"><fmt:message key="personalInfo.track.process"/></span>
				<input name="trackprocess" value="${trackprocess!='true'? '':'true'}" type="hidden"/>
			</div>
			<div style="clear:both;"></div>
			<%--<input id="trackprocess" name="trackprocess" type="checkbox" value="true" ${trackprocess=='true'? 'checked':''} >--%>
			<%--<fmt:message key="personalInfo.track.process"/>--%>
			</div>
			</div>
			</div>
			<div style="clear:both;"></div>
		</div>
	</div>










	<table style="display:none;" width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
	  <%--<tr>--%>
	    <%--<td width="100%" align="center">--%>
        <%--<div id="scrollListDiv">--%>
                   	<%--<table align="center" width="10%" border="0" cellspacing="0" cellpadding="0">--%>
                   	  <%--<tr><td colspan="2" height="30"><br></td></tr>--%>
                   	  <%--<tr>--%>
						<%--<td class="bg-gray" width="25%" nowrap="nowrap" valign="top"><fmt:message key="hr.staffInfo.self.label"--%>
							<%--bundle="${v3xHRI18N}" /></td>--%>
						<%--<td class="new-column" width="75%" nowrap="nowrap" align="left">--%>
							<%--<table cellpadding="0" cellspacing="0">--%>
								<%--<tr>--%>
									<%--<td>--%>
										<%--<div class="radius" style="border: 1px #CCC solid; width: 104px; height: 104px; text-align: center; background-color: #FFF;">--%>
											<%--<div id="thePicture" style="width: 104px; height: 104px; text-align: center;">--%>
												<%--<img class="radius" id="image1" src="${fileName}" width="104" height="104" />--%>
												<%--<c:set value="${selfImage}" var="selfImage" />--%>
											<%--</div>--%>
										<%--</div>--%>
									<%--</td>--%>
									<%--<td valign="bottom">--%>
										<%--<input type="hidden" id="filename" name="filename" value="${selfImage}">--%>
										<%--&nbsp;&nbsp;--%>
										<%--<input type="button" size="100" value="<fmt:message key="hr.staffInfo.changeself.label" bundle="${v3xHRI18N}" />" onclick="setHead()">--%>
									<%--</td>--%>
								<%--</tr>--%>
							<%--</table>--%>
						<%--</td>--%>
					  <%--</tr>--%>
					  <%--<tr><td colspan="2"><br></td></tr>--%>
			          <%--<tr>--%>
						<%--<td class="bg-gray" width="25%" nowrap="nowrap">--%>
							<%--<fmt:message key="hr.staffInfo.name.label" bundle="${v3xHRI18N}"/>:--%>
						<%--</td>--%>
						<%--<td class="new-column" width="75%" nowrap="nowrap">--%>
							<%--<input type="text" id="n" name="n" disabled size="70" value="${member.name}"/>--%>
						<%--</td>--%>
					  <%--</tr>--%>
			          <%--<tr>--%>
						<%--<td class="bg-gray" width="25%" nowrap="nowrap">--%>
							<%--<fmt:message key="hr.staffInfo.memberno.label"  bundle="${v3xHRI18N}"/>:--%>
						<%--</td>--%>
						<%--<td class="new-column" width="75%" nowrap="nowrap">--%>
							<%--<input type="text" id="no" name="no" disabled size="70" value="${member.code}"/>--%>
						<%--</td>--%>
					<%--</tr>--%>

						<%--<c:if test="${v3x:hasPlugin('i18n')}">--%>
						  <%--<c:choose>--%>
							<%--<c:when test="${v3x:getSysFlagByName('i18n_onlyCN') == true}">--%>
							<%--<input name="primaryLanguange" type="hidden" value="zh_CN">--%>
							<%--</c:when>--%>
							<%--<c:otherwise>--%>
							<%--<tr>--%>
							<%--<td class="bg-gray" width="25%" nowrap="nowrap">--%>
								<%--<fmt:message key="org.member_form.primaryLanguange.label"  bundle="${V3xOrgI18N}"/>:--%>
							<%--</td>--%>
							<%--<td class="new-column" width="75%" nowrap="nowrap">--%>
								<%--<select name="primaryLanguange" class="select-per">--%>
								    <%--<c:forEach items="${v3x:getAllLocales()}" var="language">--%>
	                                    <%--<c:if test="${v3x:getSysFlagByName('sys_isGovVer') != 'true' || language != 'en'}">--%>
								            <%--<option value="${language}" ${orgLocale==language ? "selected" : ""}><fmt:message key="localeselector.locale.${language}" bundle="${localeI18N}"/></option>--%>
	                                    <%--</c:if>--%>
								    <%--</c:forEach>--%>
								<%--</select>--%>
							<%--</td>--%>
						  	<%--</tr>--%>
							<%--</c:otherwise>--%>
						  <%--</c:choose>--%>
				       <%--</c:if>--%>
			          <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                           <%--<fmt:message key="hr.staffInfo.workTelephone.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
                            <%--<input type="text" id="telephone" name="telephone" maxlength="66" inputName="<fmt:message key="hr.staffInfo.workTelephone.label"  bundle="${v3xHRI18N}"/>" validate="maxLength" size="70" value="${v3x:toHTML(member.officeNum)}" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>
			          <%--<tr>--%>
			           <%--<td class="bg-gray" nowrap="nowrap">--%>
			               <%--<fmt:message key="hr.staffInfo.mobileTelephone.label"  bundle="${v3xHRI18N}"/>:--%>
			           <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<input type="text" id="telNumber" name="telNumber" onblur="if(v3x.isMSIE){telNumberOnBlurHandler(this);}"--%>
			               <%--defValue="<c:out value="${v3x:toHTML(member.telNumber)}" escapeXml="true"/>" maxLength="70" size="70"--%>
			               <%--inputName="<fmt:message key="hr.staffInfo.mobileTelephone.label"  bundle="${v3xHRI18N}"/>" validate="isPhoneNumber,maxLength" value="<c:out value="${member.telNumber}" escapeXml="true"/>"--%>
			               <%--${(smsSetEnable=='true' && bindphonenumberEnable=='true')? 'disabled':''}  />--%>
			               <%--<input type="hidden" name="bindphonenumberEnable" id="bindphonenumberEnable" value="${bindphonenumberEnable}">--%>
			               <%--<c:if test="${smsSetEnable=='true'}">--%>
					       	 <%--<a href="javascript:bindphonenumberchange();" class="min_button" id="bindnum">--%>
				               <%--<c:choose>--%>
					               <%--<c:when test="${bindphonenumberEnable=='true'}">--%>
					                  <%--<fmt:message key="personalInfo.bind.releasebound"  bundle="${v3xHRI18N}"/>--%>
					               <%--</c:when>--%>
					               <%--<c:otherwise>--%>
					    			 <%--<fmt:message key="personalInfo.bind.immediatebinding"  bundle="${v3xHRI18N}"/>--%>
					               <%--</c:otherwise>--%>
				               <%--</c:choose>--%>
					           <%--</a>--%>
			               <%--</c:if>--%>
                       <%--</td>--%>
                      <%--</tr>--%>

			          <%--<tr>--%>
			           <%--<td class="bg-gray" width="25%" nowrap="nowrap" align="right">--%>
			               <%--<fmt:message key="hr.staffInfo.address.label"  bundle="${v3xHRI18N}"/>:--%>
			           <%--</td>--%>
			           <%--<td class="new-column" width="75%" nowrap="nowrap">--%>
			               <%--<input type="text" id="address" name="address" maxlength="70" size="70" value="${v3x:toHTML(member.address)}"/>--%>
                       <%--</td>--%>
                      <%--</tr>--%>
			          <%--<tr>--%>

			           <%--<td class="bg-gray" nowrap="nowrap">--%>
			               <%--<fmt:message key="hr.staffInfo.postalcode.label"  bundle="${v3xHRI18N}"/>:--%>
			           <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<input type="text" id="postalcode" name="postalcode" maxlength="70" size="70" value="${v3x:toHTML(member.postalcode)}" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>
			          <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                           <%--<fmt:message key="hr.staffInfo.emailDetail.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<input type="text" id="email" name="email" maxlength="70" size="70"--%>
			               <%--value="${v3x:toHTML(member.emailAddress)}" inputName="<fmt:message key='hr.staffInfo.emailDetail.label'  bundle='${v3xHRI18N}'/>"--%>
			               <%--maxSize="40" maxLength="40" validate="validEmail,maxLength"--%>
			               <%--${(v3x:hasPlugin('webmail') && emailSetEnable=='true' && bindemailEnable=='true')? 'disabled':''}/>--%>
			               <%--<input type="hidden" name="bindemailEnable" id="bindemailEnable" value="${bindemailEnable}">--%>
			               <%--<c:if test="${v3x:hasPlugin('webmail') && emailSetEnable=='true'}">--%>
				               <%--<a href="javascript:bindemailchange();" class="min_button" id="bindemail">--%>
				               <%--<c:choose>--%>
					               <%--<c:when test="${bindemailEnable=='true'}">--%>
					                  <%--<fmt:message key="personalInfo.bind.releasebound"  bundle="${v3xHRI18N}"/>--%>
					               <%--</c:when>--%>
					               <%--<c:otherwise>--%>
					    			 <%--<fmt:message key="personalInfo.bind.immediatebinding"  bundle="${v3xHRI18N}"/>--%>
					               <%--</c:otherwise>--%>
				               <%--</c:choose>--%>
					           <%--</a>--%>
			                <%--</c:if>--%>
                       <%--</td>--%>
                      <%--</tr>--%>
                      <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                           <%--<fmt:message key="hr.staffInfo.communication.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<input type="text" id="communication" name="communication" maxlength="70" size="70" value="${v3x:toHTML(member.postAddress)}" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>
                      <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                         <%--<fmt:message key="hr.staffInfo.wb.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
                       <%--<td class="new-column" nowrap="nowrap">--%>
                           <%--<input type="text" id="wb" name="wb" maxlength="70" size="70" value="${v3x:toHTML(member.weibo)}" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>
                      <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                         <%--<fmt:message key="hr.staffInfo.wx.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
                       <%--<td class="new-column" nowrap="nowrap">--%>
                           <%--<input type="text" id="wx" name="wx" maxlength="70" size="70" value="${v3x:toHTML(member.weixin)}" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>
                      <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                           <%--<fmt:message key="hr.staffInfo.website.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<input type="text" id="website" name="website" maxlength="70" size="70" value="${v3x:toHTML(member.website)}" inputName="<fmt:message key="hr.staffInfo.website.label"  bundle="${v3xHRI18N}"/>" validate="maxLength" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>
                      <%--<tr>--%>
                       <%--<td class="bg-gray" nowrap="nowrap">--%>
                           <%--<fmt:message key="hr.staffInfo.blog.label"  bundle="${v3xHRI18N}"/>:--%>
                       <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<input type="text" id="blog" name="blog" maxlength="70" size="70" inputName="<fmt:message key="hr.staffInfo.blog.label"  bundle="${v3xHRI18N}"/>" validate="maxLength" value="${v3x:toHTML(member.blog)}" />--%>
                       <%--</td>--%>
                      <%--</tr>--%>

                    <%--<c:forEach items="${bean}" var="bean" varStatus="status">--%>
				     <%--<!--文本类型  -->--%>
				 	<%--<c:if test="${bean.type == 0}">--%>
			           <%--<tr>--%>
	                       <%--<td class="bg-gray" nowrap="nowrap" >--%>
	                           <%--${v3x:toHTML(bean.label)}:--%>
	                       <%--</td>--%>
				           <%--<td class="new-column" nowrap="nowrap" >--%>
				               <%--<input type="text" id="${bean.id}" name="${bean.id}" size="70" maxLength="70" value="" validate="maxLength"/>--%>
	                       <%--</td>--%>
                      <%--</tr>--%>
				 	<%--</c:if>--%>

				 	<%--<!--数字类型  -->--%>
 				 	<%--<c:if test="${bean.type == 1}">--%>
				 		 <%--<tr>--%>
				 		 	<%--<td class="bg-gray" nowrap="nowrap" >--%>
	                           <%--${v3x:toHTML(bean.label)}:--%>
	                       <%--</td>--%>
				           <%--<td class="new-column" nowrap="nowrap" >--%>
				               <%--<input type="text" id="${bean.id}" name="${bean.id}" value="" class="validate font_size12" onblur="validatenum(this);" maxlength="13" size="70" />--%>
	                       <%--</td>--%>
			            <%--</tr>--%>
				 	<%--</c:if>--%>

				 	<%--<!--日期类型  -->--%>
 				 	<%--<c:if test="${bean.type == 2}">--%>
				        <%--<tr>--%>
 				          <%--<td class="bg-gray" nowrap="nowrap" >--%>
	                           <%--${v3x:toHTML(bean.label)}:--%>
	                       <%--</td>--%>
 				           <%--<td class="new-column" nowrap="nowrap">--%>
				               <%--<input id="${bean.id}" name="${bean.id}" type="text" maxlength="250" class="select-per" onClick="whenstart('${pageContext.request.contextPath}', this, 175, 140);" readonly="readonly"  value=""    />--%>
	                       <%--</td>--%>
			            <%--</tr>--%>
				 	<%--</c:if>--%>
		  		 <%--</c:forEach>--%>

 		  		 <%--<c:forEach items="${beanValue}" var="beanValue">--%>
		  		        <%--<script type="text/javascript">--%>
		  		        <%--var key='${beanValue.key}';--%>
		  		        <%--$("#"+key).val('${v3x:escapeJavascript(beanValue.value)}');--%>
                     <%--</script>--%>
		  		 <%--</c:forEach>--%>


                      <%--<tr>--%>
	                       <%--<td class="bg-gray" nowrap="nowrap" valign="top">--%>
	                           <%--<fmt:message key="hr.record.remark.label"  bundle="${v3xHRI18N}"/>:--%>
	                       <%--</td>--%>
				           <%--<td class="new-column" nowrap="nowrap">--%>
				              <%--<textarea  maxlength="2000"  rows="5" cols="68" name="comment" id="comment"  inputName="<fmt:message key="hr.record.remark.label"  bundle="${v3xHRI18N}"/>">${v3x:toHTMLAlt(member.description)}</textarea>--%>
	                       <%--</td>--%>
                       <%--</tr>--%>

					   <%--<c:if test="${isCanUseSMS}">--%>
                       <%--<tr>--%>
                           <%--<td class="bg-gray" nowrap="nowrap"></td>--%>
                           <%--<td class="new-column" nowrap="nowrap">--%>
                               <%--<label for="smsLoginEnable"><input id="smsLoginEnable" name="smsLoginEnable" type="checkbox" value="true" onclick="smsLoginEnableOnclickHandler(this);" ${smsLoginEnable != 'true'? '':'checked'} >--%>
                                    <%--<fmt:message key="systemswitch.smsLoginEnable.lable" bundle="${v3xHRI18N}" />--%>
                               <%--</label>--%>
                           <%--</td>--%>
                       <%--</tr>--%>
                       <%--</c:if>--%>
                       <%--<tr>--%>
			           <%--<td class="bg-gray" nowrap="nowrap">--%>

			           <%--</td>--%>
			           <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<label for="msgSoundEnable">--%>
			    				<%--<input id="msgSoundEnable" name="enableMsgSound" type="checkbox" value="true" ${systemMsgSoundEnable&&(enableMsgSound=='true')? 'checked':''} ${systemMsgSoundEnable? '':'disabled'}>--%>
					       		<%--<fmt:message key="systemswitch.MsgHint.lable"/>--%>
					       <%--</label>--%>
                       <%--</td>--%>
                       <%--</tr>--%>
                       <%--<tr>--%>
			           <%--<td class="bg-gray" nowrap="nowrap">--%>

                       <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<label for="msgClosedEnable">--%>
			    				<%--<input id="msgClosedEnable" name="msgClosedEnable" type="checkbox" value="true" ${msgClosedEnable!='false'? 'checked':''} >--%>
					       		<%--<fmt:message key="systemswitch.MsgAfterLook.lable"/>--%>
					       <%--</label>--%>
                       <%--</td>--%>
                        <%--</tr>--%>
                  <%--<c:if test="${v3x:hasPlugin('index')}">--%>
		                <%--<tr>--%>
	                       <%--<td class="bg-gray" nowrap="nowrap">--%>
			               <%--</td>--%>
	                       <%--<td class="new-column" nowrap="nowrap">--%>
	                       <%--<c:if test="${not empty isShowIndexSummary&&isShowIndexSummary=='false'}">--%>
	                          <%--<c:set value="true" var="setIndexSummary"/>--%>
	                       <%--</c:if>--%>
				               <%--<label for="isShowIndexSummary">--%>
				    				<%--<input id="isShowIndexSummary" name="isShowIndexSummary" type="checkbox" value="true" ${isShowIndexSummary=='false'? 'checked':''} >--%>
						       		<%--<fmt:message key="index.com.seeyon.v3x.index.showSummary.notshow" bundle="${indexResources }"/>--%>
						      <%--</label>--%>
	                       	<%--</td>--%>
                    	  <%--</tr>--%>
                    <%--</c:if>--%>
                    <%--<tr>--%>
			           <%--<td class="bg-gray" nowrap="nowrap">--%>

                       <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<label for="extendConfig">--%>
			    				<%--<input id="extendConfig" name="extendConfig" type="checkbox" value="true" ${extendConfig!='false'? 'checked':''} >--%>
					       		<%--<fmt:message key="personalInfo.extend"/>--%>
					       <%--</label>--%>
                       <%--</td>--%>
                        <%--</tr>--%>

                        <%--<tr>--%>
			           <%--<td class="bg-gray" nowrap="nowrap">--%>

                       <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<label for="tracksend">--%>
			    				<%--<input id="tracksend" name="tracksend" type="checkbox" value="true" ${tracksend!='false'? 'checked':''} >--%>
					       		<%--<fmt:message key="personalInfo.track.send"/>--%>
					       <%--</label>--%>
                       <%--</td>--%>
                        <%--</tr>--%>

                        <%--<tr>--%>
			           <%--<td class="bg-gray" nowrap="nowrap">--%>

                       <%--<td class="new-column" nowrap="nowrap">--%>
			               <%--<label for="trackprocess">--%>
			    				<%--<input id="trackprocess" name="trackprocess" type="checkbox" value="false" ${trackprocess=='true'? 'checked':''} >--%>
					       		<%--<fmt:message key="personalInfo.track.process"/>--%>
					       <%--</label>--%>
                       <%--</td>--%>
                        <%--</tr>--%>

		            <%--</table>--%>
	<%--</div>--%>
	<%--</td>--%>
	<%--</tr>--%>
	  <tr style="display:none">
			<td height="42" align="center" class="tab-body-bg bg-advance-bottom">
				<input type="submit" id="submitButton" name="submitButton" value="<fmt:message key='common.button.ok.label' bundle="${v3xCommonI18N}" />" class="button-default-2 button-default_emphasize">&nbsp;
				<input type="button" onclick="getA8Top().back();" value="<fmt:message key='common.button.cancel.label' bundle="${v3xCommonI18N}" />" class="button-default-2">
			</td>
		</tr>
	</table>
</form>
</body>
</html>
<script>
//bindOnresize('scrollListDiv',0,10);
showCtpLocation("F12_perSetting");
</script>