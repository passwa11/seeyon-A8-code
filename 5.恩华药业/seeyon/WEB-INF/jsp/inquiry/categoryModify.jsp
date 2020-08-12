<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<%@include file="../common/INC/noCache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
<%@include file="header.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
<link rel="stylesheet" type="text/css" href="<c:url value="/portal/management/css/iconfont.css${v3x:resSuffix()}" />">
<script type="text/javascript">
   var nameList = new Array();
   
   <c:forEach var="tname" items="${typeNameList}">
         nameList.push("${v3x:escapeJavascript(tname)}");//初始化调查类型名称列表
   </c:forEach>
   
   var onlyLoginAccount_per = false;
   var onlyLoginAccount_second = false;
   
   <c:if test="${!isGroup&&spaceType!=18&&spaceType!=17}">
    onlyLoginAccount_per = true;
    onlyLoginAccount_second =true;
</c:if>
   
   //判断当前调查类型名称是否存在
   function isSameName(){
       var theNewName = document.getElementById("typename");
       var theOldName = document.getElementById("theOldName");
       for(var j = 0;j<nameList.length;j++ ){
           if(theNewName.value.trim() == nameList[j] && (theNewName.value.trim() != theOldName.value )){
              alert(v3x.getMessage("InquiryLang.inquiry_isTheSame_alert"));
              theNewName.value = theNewName.value.trim();
              theNewName.focus();
              return false;
           }
       }
	   if(document.getElementById("selectpersonTag").style.display=="block"){
	       if(document.mainForm.peopleIdSecond.value==""||document.mainForm.peopleIdSecond.value=='<fmt:message key="common.default.selectPeople.value"  bundle="${v3xCommonI18N}"/>'){
	         alert(v3x.getMessage("InquiryLang.inquiry_choose_checker_alert"));
	         return false;
	       }
	   }
	   return true;
  }
   
  function disableSub(){
	document.getElementById("submit").disabled = true;
	return true;
  }
  function getList(){
      parent.parent.document.location.reload();
  }
  
  function selectChecker(){
  	if("${hasNoCheck}"=='true'){
  		alert(v3x.getMessage("InquiryLang.inquiry_has_noCheck"));
  		return;
  	}
  	selectPeopleFun_second();
  }
  function isSub(){
    if(document.getElementById("censordesc1").checked){
	    var auditUser= document.getElementById("peopleIdSecond")==null?"":document.getElementById("peopleIdSecond").value;
	    if(auditUser==null || auditUser==''){
	      alert(v3x.getMessage("InquiryLang.inquiry_has_not_null"));
	      return false;
	    }
	    if(auditUser=="${surveytype.checker.id}" && "${isAlert}"=="true"){
	      alert($.i18n('inquiry.audit.scope.re.select'));
	      return false;
	    }
    }
    return true;
  }
<c:if test="${isAlert}">
  alert($.i18n('inquiry.audit.scope.re.select'));
</c:if>
</script>
<script type="text/javascript">
<!--
var includeElements_per = "${v3x:parseElementsOfTypeAndId(entity)}";
var includeElements_second = "${v3x:parseElementsOfTypeAndId(entity)}";
//-->
</script>
<c:set value="${v3x:parseElementsOfIds(surveytype.managerIds,'Member')}" var="managers" ></c:set>
<v3x:selectPeople id="per" panels="Department,Post,Level,Team" selectType="Member" originalElements="${managers}"
	departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
	jsFunction="setPeopleFields(elements)" maxSize="50" />
<c:if test="${surveytype.checker.id != null}">
     <c:set var="member_id" value="${v3x:parseElementsOfIds(surveytype.checker.id , 'Member')}"></c:set>
</c:if>
<v3x:selectPeople id="second" panels="Department,Post,Level,Team" selectType="Member" originalElements="${member_id}"
	departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
	jsFunction="setPeopleFieldsSecond(elements)" maxSize="1" />

</head>
<body>
<form name="mainForm" method="post" action="${detailURL}?method=update_Type&spaceId=${param.spaceId}${ctp:csrfSuffix()}" onsubmit="return (checkForm(this) && isSameName())  && isSub() && disableSub() ">
<input type="hidden" name="needTransfer2NewChecker" value="${needTransfer2NewChecker}" />
<input type="hidden" name="oldCheckerId" value="${oldCheckerId}" />
<c:set var="ro" value="${v3x:outConditionExpression(readOnly, 'readonly', '')}" />
<c:set var="dis" value="${v3x:outConditionExpression(readOnly, 'disabled', '')}" />

<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center" class="">
    <tr>
        <td>
           
            <div id="docLibBody">

                <table width="600px" cellpadding="2" cellspacing="0" border="0" align="center">
                    <tr>
                        <td align="right" width="25%" class="bg-gray"><font color="red">*</font> <fmt:message
                            key='inquiry.categoryName.label' />:</td>
                        <td width="75%">
                            <input type="text" id="typename" name="typename" validate="notNull,maxLength" maxSize="30" inputName="<fmt:message key='inquiry.categoryName.label' />"
                                    value="${v3x:toHTML(surveytype.inquirySurveytype.typeName)}" class="input-300px"  ${dis} ${ro} />
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="bg-gray"><font color="red">*</font> <fmt:message
                            key='inquiry.manager.label' />:</td>
                        <td>
                            <input ${dis} ${ surveytype.inquirySurveytype.spaceType eq 1 ? 'disabled' : '' } type="text" id="peopleValue" readonly
                                value="${v3x:showOrgEntitiesOfIds(surveytype.managerIds, 'Member', pageContext)}" validate="notNull"
                                inputName="<fmt:message key='inquiry.manager.label' />" name="T5" onclick="selectPeopleFun_per()" class="input-300px cursor-hand">
                            <input type="hidden" id="peopleId" name="peopleId" value="${v3x:joinWithSpecialSeparator(surveytype.managers, 'id', ',')}">
                        </td>
                    </tr>
                    <tr>
                        <td align="right" class="bg-gray"><fmt:message key='inquiry.audit.label' />:</td>
                        <td>
                        <label for="censordesc1">
                            <c:set value="${surveytype.checker.id!=null && surveytype.checker.id!=0 && surveytype.checker.id!=1 && surveytype.checker.id!=-1}" var="valid" />
                            <input ${dis} type="radio" value="0" id="censordesc1" name="censordesc" onclick="selectPersonA()" <c:if test="${update!='update'}">disabled</c:if>
                            <c:if test="${surveytype.inquirySurveytype.censorDesc==0}">checked</c:if> >
                            <fmt:message key='common.true' bundle='${v3xCommonI18N}' />
                        </label>
                        <label for="censordesc2">
                            <input ${dis} type="radio" value="1" id="censordesc2" name="censordesc" onclick="selectPersonB()" ${ (hasNoCheck || needTransfer2NewChecker || isAlert)  && valid=='true' ? 'disabled' : '' } <c:if test="${update!='update'}"> disabled</c:if>
                            <c:if test="${surveytype.inquirySurveytype.censorDesc==1}">checked</c:if> >
                            <fmt:message key='common.false' bundle='${v3xCommonI18N}' />
                        </label>
                        <i class="iconfont1 icon-help" style="margin: 5px;font-size: 14px;color:#ff3544;position: relative" title="<fmt:message key='inquiry.manager.help.audit' bundle='${v3xCommonI18N}' />"></i>
                        </td>
                    </tr>
          
                    <tr id="selectpersonTag" style="display:${ surveytype.inquirySurveytype.censorDesc==0 ? '' : 'none'}">
                        <td align="right" class="bg-gray">
                            <fmt:message key='inquiry.auditor.label' />:
                        </td>
                        <fmt:message key="common.default.selectPeople.value"  bundle="${v3xCommonI18N}" var="dfSubject"/>
                        <td>                    
                            <c:set value="${valid=='true' ? v3x:showMemberName(surveytype.checker.id) : dfSubject}" var="auditorName" />
                            <input ${dis} type="text" id="peopleValueSecond" value="<c:out value='${auditorName}' default='${dfSubject}'/>"  deaultValue="${dfSubject}"
                            inputName="<fmt:message key='inquiry.auditor.label' />" name="peopleValueSecond"
                            readonly onclick="selectChecker();" class="input-300px cursor-hand"
                            ${v3x:outConditionExpression(readOnly, 'readonly', '')}
                            onfocus="checkDefSubject(this, true)" onblur="checkDefSubject(this, false)">
                            <input type="hidden" id="peopleIdSecond" name="peopleIdSecond" value="${surveytype.checker.id}">
                        </td>
                    </tr>
                    <tr>
	                    <td class="bg-gray" align="right"> <fmt:message key="inquiry.allow.anonymous.vote" />:</td>
	                     <td>
	                        <label for="anonymous_true">
	                            <input type="radio" name="anonymousFlag" value="0" id="anonymous_true"  <c:if test="${surveytype.inquirySurveytype.anonymousFlag == 0 || empty surveytype.inquirySurveytype.anonymousFlag}">checked</c:if> ${dis} ${ro}> <fmt:message key="common.true" bundle="${v3xCommonI18N}"/>
	                        </label>
	                        <label for="anonymous_false">
	                            <input type="radio" name="anonymousFlag" value="1" id="anonymous_false" <c:if test="${surveytype.inquirySurveytype.anonymousFlag == 1}">checked</c:if> ${dis} ${ro}> <fmt:message key="common.false" bundle="${v3xCommonI18N}"/>
	                        </label>
	                    </td>
                    </tr>
                    <tr>
                        <td align="right" valign="top" class="bg-gray"><fmt:message key='common.description.label'  bundle="${v3xCommonI18N}" />:</td>
                        <td>
                            <textarea ${dis} rows="5" name="surveydesc" cols="60" class="input-300px" id="surveydesc" inputName="<fmt:message key='common.description.label'  bundle="${v3xCommonI18N}" />" validate="maxLength" maxSize="120">${surveytype.inquirySurveytype.surveyDesc}</textarea><br>
                        </td>
                    </tr>
                </table>

            </div>
           
           
        </td>
    </tr>
    <c:if test="${update=='update'}">
    <c:if test="${!readOnly}">
    <tr>
        <td height="50" align="center" class="bg-advance-bottom button_container">
                
			<input type="hidden" value="${surveytype.inquirySurveytype.id}" name="id">
			<input type="hidden" value="${v3x:toHTML(surveytype.inquirySurveytype.typeName)}" id="theOldName"/>
			<input type="submit" id="submit" value="<fmt:message key='common.button.ok.label' bundle='${v3xCommonI18N}' />" name="B1" class="button-default-2 button-default_emphasize">&nbsp;&nbsp;
			<input type="button"  value="<fmt:message key='common.button.cancel.label' bundle='${v3xCommonI18N}' />" name="B1" class="button-default-2" onclick="getList()">

        </td>
    </tr>
    </c:if>
    </c:if>
</table>

<script type="text/javascript">
	bindOnresize('docLibBody',0,70);

	function selectPersonA()
	{
		document.getElementById("selectpersonTag").style.display = "";
	}
	function selectPersonB()
	{
		document.getElementById("selectpersonTag").style.display = "none";
	}
</script>
</form>
</body>
</html>