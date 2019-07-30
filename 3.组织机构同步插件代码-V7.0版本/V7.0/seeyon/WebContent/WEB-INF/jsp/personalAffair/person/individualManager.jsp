<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>个人维护</title>
<%@include file="header.jsp"%>
<html:link renderURL='/main.do' var="mainURL"/>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/passwdcheck.css${v3x:resSuffix()}" />">
<script type="text/javascript" src="<c:url value='/common/js/passwdcheck.js${v3x:resSuffix()}'/>"></script>
<script type="text/javascript">
<%--getA8Top().showLocation(801, "<fmt:message key='menu.individual.manager' bundle='${v3xMainI18N}'/>");--%>
<%--验证弹出的信息-输入新密码是否相同-原密码错误--%>
var sameOrNot = "<fmt:message key='manager.vialdateword'/>";
var oldPasswordMsg = "<fmt:message key='manager.oldword'/>";

var weakrule="<fmt:message key='manager.pwdStrength.weakrule'/>";
var mediumrule="<fmt:message key='manager.pwdStrength.mediumrule'/>";
var strongrule="<fmt:message key='manager.pwdStrength.strongrule'/>";
var bestrule="<fmt:message key='manager.pwdStrength.bestrule'/>";

var mediumMsg="<fmt:message key='manager.pwdStrength.medium'/>"+"\r\n"+mediumrule;
var strongMsg="<fmt:message key='manager.pwdStrength.strong'/>"+"\r\n"+strongrule;
var bestMsg="<fmt:message key='manager.pwdStrength.best'/>"+"\r\n"+bestrule;
var notequalMsg="<fmt:message key='manager.pwdModify.notequal'/>";

$(document).ready(function(){
	disabledbutton();
});

function disabledbutton(){
	var forcemodify = '${param.forcemodify}';
	if (forcemodify == "true" || forcemodify == true) {
	document.getElementById("submitOk").style.display="none";
	var submitbutton=document.getElementById("subBut");
	submitbutton.disabled=true;
	
	var canclebutton=document.getElementById("canclebutton");
	canclebutton.disabled=true;
	document.onkeydown = function(event) {
		event=event|window.event;
		if (event.keyCode == 116 || event.keyCode == 9) {
			return false;}
		}
	}
}



function save(){
	var fm = document.getElementById('subBut');
	fm.click();
}
function cancle(){
	window.location.reload();
}

function canSamePWD(){
	var cansame = document.getElementById("pwdmodify_same_enable").value;
	if(cansame){
		if(cansame ==="enable"){
			return true;
		}
	}
	return false;
}

function initPage(){
	var from = parent.document.getElementById('submitbtn');
	if(from != null){
		document.getElementById('submitOk').style.display='none';
	}else{
		document.getElementById('submitOk').style.display='';
	}
	disabledbutton();
}

	function OK() {
		var _form = document.getElementById("postForm");
		if (validateOldPassword1()
				&& checkForm(_form)
				&& validate1()
				&& (canSamePWD() || validatePwdEqual(
						document.getElementById("formerpassword").value,
						document.getElementById("nowpassword").value))
				&& validatePwdStrong('${pwd_strong_require}', document
						.getElementById("nowpassword").value)) {
			if(_form.submit()){
				return "true";
			}
			    
		}
	}
</script>
<script type="text/javascript"
	src="<c:url value='/apps_res/systemmanager/js/individualManager.js${v3x:resSuffix()}'/>">
</script>
	<style>
		.bg-advance-bottom{
			height:30px\9;
		}
		form{
			height:350px\9;
		}
		input:focus{
			border:1px solid #68BAFF;
		}
	</style>
</head>
<body class="padding5" scroll="no" onload="initPage();" style="background-color:#fafafa;">
<form id="postForm" method="post" action="<html:link renderURL='/individualManager.do' />?method=modifyIndividual"
onsubmit="return (validateOldPassword1() && checkForm(this) && validate1() && (canSamePWD() || validatePwdEqual(document.getElementById('formerpassword').value,document.getElementById('nowpassword').value))  && validatePwdStrong('${pwd_strong_require}',document.getElementById('nowpassword').value))">
<input id="individualName" type="hidden" name="individualName" value="${logerName}" />
<input id=pwdmodify_same_enable type="hidden" name="pwdmodify_same_enable" value="${pwdmodify_same_enable}" />
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
	  <%--<tr>
	  	<td class="init-password-border">
	  		<c:if test="${initPage != 'true' }">
	    	<script type="text/javascript">
	    	var myBar = new WebFXMenuBar("${pageContext.request.contextPath}","gray");
			myBar.add(new WebFXMenuButton("editBtn","<fmt:message key='common.toolbar.update.label' bundle="${v3xCommonI18N}" />", "editData();", [1,2], "",null));
	    	document.write(myBar);
	    	document.close();
	    	</script>
	    	</c:if>
	  	</td>
	  </tr> --%>   
	  <tr>
	    <td valign="top" class="tab-body-bg" height="100%" align="center" style="padding:0;border:none;">
			<div style="width: 100%;">
			<fieldset style="border:none;padding:0;margin-top:30px;">
						<table width="100%" border="0" cellspacing="0" cellpadding="0"
							align="center" style="padding-left:20px;padding-right:20px;">
							<c:set var="disabled" value="${param.disabled }" />
							<tr height="40">
								<td class="bg-gray" width="20%" nowrap="nowrap"><label
									for="post.code">  <font color="red">*</font><fmt:message key="manager.formerpassword.notnull" /></label></td>
								<td class="new-column" width="80%">
									<input class="input-100per" style="width:325px;height:28px;border: 1px solid #e4e4e4;" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" type="password" name="formerpassword" id="formerpassword" value="${systemPassword }" maxlength="50" inputName="<fmt:message key="manager.formerpassword.notnull" />" validate="notNull" />
								</td>
							</tr>
							<tr height="40">
								<td class="bg-gray" width="20%" nowrap="nowrap"><label
									for="post.code"> <font color="red">*</font> <fmt:message key="manager.password.notnull" /></label></td>
								<td class="new-column" width="80%">
									<input class="input-100per" style="width:325px;height:28px;border: 1px solid #e4e4e4;" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" type="password" name="nowpassword" id="nowpassword" value="" inputName="<fmt:message key="manager.password.notnull" />" minLength="6" maxLength="50" validate="notNull,minLength,maxLength"
									onKeyUp="EvalPwdStrength(document.forms[0],this.value);"/>
								</td>
							</tr>
							<tr>
								<td>&nbsp;</td>
								<td>
									<div style="padding-left:5px;">
										<span style="color:green;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
										<fmt:message key='common.pwd.pwdStrength.require'/>:
										</span>
										<!-- 弱密碼強度提示 -->
										<c:if test="${pwd_strong_require=='weak'}">
											<span style="color:#f90001;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.value1'/>
											</span>
											</br>
											<span style="color:green;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.weakrule'/>
											</span>
										</c:if>
										<!-- 中密碼強度提示 -->
										<c:if test="${pwd_strong_require=='medium'}">
											<span style="color:#f90001;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.value2'/>
											</span>
											</br>
											<span style="color:green;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.mediumrule'/>
											</span>
										</c:if>
										<!-- 強密碼強度提示 -->
										<c:if test="${pwd_strong_require=='strong'}">
											<span style="color:#f90001;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.value3'/>
											</span>
											</br>
											<span style="color:green;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.strongrule'/>
											</span>
										</c:if>
										<!-- 最好密碼強度提示 -->
										<c:if test="${pwd_strong_require=='best'}">
											<span style="color:#f90001;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.value4'/>
											</span>
											</br>
											<span style="color:green;font-family:'Microsoft YaHei',SimSun, Arial,Helvetica,sans-serif;font-size:12px;">
											<fmt:message key='common.pwd.pwdStrength.bestrule'/>
											</span>
										</c:if>
									</div>
								</td>
							</tr>
							<tr height="40">
							<td class="bg-gray" width="20%" nowrap="nowrap"><label
									for="post.code"><fmt:message key="common.pwd.pwdStrength.label" bundle="${v3xCommonI18N }"/></label></td>
							<td class="new-column" width="80%">
							<%--<table cellpadding="0" cellspacing="0" class="pwdChkTbl2">--%>
								<%--<td>--%>
									<div style="width:100%;margin-top:4px;">
										 <div id="idSM1" style="position:relative;width:77px;height:7px;float:left;" class="pwdChkCon0" align="center">
											 <div id="idSMD1" style="position:absolute;width:28px;height:28px;background-color:#fc5d5c;border-radius:100%;top:-10px;left:24px;line-height:28px;color:white;display:none;">
							 					<span id="idSMT1" style="display:none;"><fmt:message key="common.pwd.pwdStrength.value1" bundle="${v3xCommonI18N }"/></span>
							 				</div>
							 			</div>
										 <div id="idSM2" class="pwdChkCon0" align="center" style="position:relative;width:77px;height:7px;float:left;margin-left:5px;">
							 				<div id="idSMD2" style="position:absolute;width:28px;height:28px;background-color:#fcb04b;border-radius:100%;top:-10px;left:24px;line-height:28px;color:white;display:none;">
											 	<span id="idSMT2" style="display:none;"><fmt:message key="common.pwd.pwdStrength.value2" bundle="${v3xCommonI18N }"/></span>
							 				</div>
							 			</div>
										 <div id="idSM3" class="pwdChkCon0" align="center" style="position:relative;width:77px;height:7px;float:left;margin-left:5px;">
											 <div id="idSMD3" style="position:absolute;width:28px;height:28px;background-color:#70b442;border-radius:100%;top:-10px;left:24px;line-height:28px;color:white;display:none;">
												 <span id="idSMT3" style="display:none;"><fmt:message key="common.pwd.pwdStrength.value3" bundle="${v3xCommonI18N }"/></span>
							 				</div>
							 			</div>
										 <div id="idSM4" class="pwdChkCon0" align="center" style="position:relative;width:77px;height:7px;float:left;margin-left:5px;">
											 <div id="idSMD4" style="position:absolute;width:28px;height:28px;background-color:#378404;border-radius:100%;top:-10px;left:24px;line-height:28px;color:white;display:none;">
											 	<span id="idSMT4" style="display:none;"><fmt:message key="common.pwd.pwdStrength.value4" bundle="${v3xCommonI18N }"/></span>
											 </div>
										 </div>
										 <%--<td id="idSM4" width="25%" class="pwdChkCon0" align="center"--%>
										 <%--style="border-left:solid 1px #fff"><span style="font-size:1px">&nbsp;</span><span--%>
										 <%--id="idSMT4" style="display:none;"><fmt:message key="common.pwd.pwdStrength.value4" bundle="${v3xCommonI18N }"/></span></td>--%>
										 <div style="clear:both;"></div>
							 		</div>
							 <%--</td>--%>
							<%--</table>--%>
							</td>
						</tr>
							<tr>
								<td class="bg-gray" width="20%" nowrap="nowrap"><label
									for="post.code"> <font color="red">*</font><fmt:message key="manager.validate.notnull" /></label></td>
								<td class="new-column" width="80%">
									<input id="validatepass" class="input-100per" style="width:325px;height:28px;border: 1px solid #e4e4e4;" onfocus="this.style.borderColor = '#68BAFF'" onblur="this.style.borderColor = '#e4e4e4'" type="password" name="validatepass" value="" inputName="<fmt:message key="manager.validate.notnull" />"  minLength="6" maxLength="50" validate="notNull" />
								</td>
							</tr>
							<tr height="35">
								<td>&nbsp;</td>
								<td class="description-lable">
									<div style="padding-left:5px;">
										<fmt:message key="manager.vaildate.length1"/>
									</div>
								</td>
							</tr>
							
						</table>
						<table width="95%" border="0" cellspacing="0" cellpadding="0"
							align="center" style="padding-left:20px;padding-right:20px;">
						  <tr>
							<td width="70%">
									<div style="padding-left:5px;color:#F00">
										此处修改的密码仅作为门户系统无法登录时，临时启用协同系统密码登录时使用;
										若要修改协同系统平常登录密码，请到一体化平台进行修改！！
									</div>
								</td>
							</tr>
						</table>
						<br/>			
						</fieldset>				
			</div>
			
		</td>
	</tr>
	<tr id="submitOk" style="display: ">
		<td height="42" align="right" class="tab-body-bg bg-advance-bottom" style="border:none;">
			<input type="submit" id="subBut" value="<fmt:message key='common.button.ok.label' bundle="${v3xCommonI18N}" />" class="button-default-2 button-default_emphasize">
			<input type="button" id="canclebutton" onclick="getA8Top().pwdModify.close();" value="<fmt:message key='common.button.cancel.label' bundle="${v3xCommonI18N}" />" class="button-default-2" style="margin-left:6px;">
		</td>
	</tr>
</table>
</form>
</body>
</html>