<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<%@include file="../common/INC/noCache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<%@include file="header.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
<script type="text/javascript">

   var nameList = new Array();

   var onlyLoginAccount_per = false;
   var onlyLoginAccount_second = false;

   <c:if test="${!isGroup&&spaceType!=18&&spaceType!=17}">
   var onlyLoginAccount_per = true;
   var onlyLoginAccount_second = true;
</c:if>

   <c:forEach var="tname" items="${typeNameList}">
         nameList.push("${v3x:escapeJavascript(tname)}");
   </c:forEach>

   function isSameName(){
       var theNewName = document.getElementById("typename");
       for(var j = 0;j<nameList.length;j++ ){
           if(theNewName.value.trim() == nameList[j].trim()){
              alert(v3x.getMessage("InquiryLang.inquiry_isTheSame_alert"));
              theNewName.value = theNewName.value.trim();
              theNewName.focus();
              return false;
           }
       }

	    if(document.getElementById("selectpersonTag").style.display=="block"){
	       if(document.mainForm.peopleIdSecond.value==""){
	         alert(v3x.getMessage("InquiryLang.inquiry_choose_checker_alert"));
	         return false;
	       }
	     }
	     return true;
   }
   function isSub(){
     if(document.getElementById("censordesc1").checked){
	     var auditUser= document.getElementById("peopleIdSecond")==null?"":document.getElementById("peopleIdSecond").value;
	     if(auditUser==null || auditUser==''){
	       alert(v3x.getMessage("InquiryLang.inquiry_has_not_null"));
	       return false;
	     }
     }
     return true;
   }
   function disableButton() {
   		document.getElementById("B1").disabled = true;
   		return true;
   }
</script>
<script type="text/javascript">
<!--
var includeElements_per = "${v3x:parseElementsOfTypeAndId(entity)}";
var includeElements_second = "${v3x:parseElementsOfTypeAndId(entity)}";
//-->
</script>
<v3x:selectPeople id="per" panels="Department,Post,Level,Team"
	selectType="Member"
	departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
	jsFunction="setPeopleFields(elements)" maxSize="50" />
<v3x:selectPeople id="second" panels="Department,Post,Level,Team"
	selectType="Member"
	departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
	jsFunction="setPeopleFieldsSecond(elements)" maxSize="1" />
</head>
<body style="text-align:center">
<form name="mainForm" method="post" action="${detailURL}?method=create_Type&spaceType=${v3x:toHTML(param.spaceType)}&spaceId=${v3x:toHTML(param.spaceId)}${ctp:csrfSuffix()}" onsubmit="return (checkForm(this) && isSameName() && isSub()&& disableButton())" >

<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center" class="">
    <tr>
        <td>

            <div id="docLibBody">

            <table border="0" cellpadding="0" cellspacing="0" align="center">
                <tr>
                    <td height="10%" colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <fmt:message key="inquiry.addCategoryName.label" var="categoryName" />
                    <td class="bg-gray" align="right"> <font color="red">*</font>&nbsp;<fmt:message key='inquiry.categoryName.label' />:</td>
                    <td class="new-column"><input name="typename" type="text" id="typename"
                        class="input-300px" deaultValue="${categoryName}"
                        inputName="<fmt:message key='inquiry.categoryName.label' />"
                        validate="isDeaultValue,notNull,maxLength" maxSize="30"
                        value="<c:out value='${categoryName}' escapeXml='true' default='${categoryName}' />"
                        onfocus='checkDefSubject(this, true)'
                        onblur="checkDefSubject(this, false)">
                    </td>
                </tr>
                <tr>
                    <fmt:message key="common.default.selectPeople.value" bundle="${v3xCommonI18N}" var="dfSubject" />
                    <td class="bg-gray" align="right"> <font color="red">*</font>&nbsp;<fmt:message key='inquiry.manager.label' />:</td>
                    <td class="new-column"><input name="T5" type="text" id="peopleValue"
                        class="input-300px cursor-hand" deaultValue="${dfSubject}"
                        inputName="<fmt:message key='inquiry.manager.label' />"
                        validate="isDeaultValue,notNull"
                        value="<c:out value="${dfSubject}" escapeXml="true" default='${dfSubject}' />"
                        onclick="selectPeopleFun_per()"
                        ${v3x:outConditionExpression(readOnly, 'readonly', '')}
                   onfocus='checkDefSubject(this, true)'
                        onblur="checkDefSubject(this, false)" readonly> <input
                        type="hidden" id="peopleId" name="peopleId"></td>
                </tr>
                <tr>
                    <td class="bg-gray" align="right"><fmt:message key='inquiry.audit.label' />:</td>
                    <td class="new-column">
                    <label for="censordesc1">
                        <input type="radio" ${ isGroup ? 'checked' : '' } value="0" id="censordesc1" name="censordesc"
                        onclick="selectPersonA()"><fmt:message key='common.true'
                        bundle='${v3xCommonI18N}' /></label>
                        <label for="censordesc2">
                        <input type="radio" value="1" id="censordesc2"
                        name="censordesc" ${ isGroup ? '' : 'checked' } onclick="selectPersonB()"><fmt:message
                        key='common.false' bundle='${v3xCommonI18N}' /></label>
                    </td>
                </tr>

                <tr class="bg-gray" id="selectpersonTag" style="display:${ isGroup ? '' : 'none' }">
                    <td align="right"><fmt:message key='inquiry.auditor.label' />: </td>
                    <td class="new-column"><input type="text" id="peopleValueSecond"
                        onfocus='checkSubject(this, true)'
                        onblur="checkSubject(this, false)" deaultvalue="${dfSubject}"
                        value="${dfSubject}"
                        inputName="<fmt:message key='inquiry.auditor.label' />" name="T6"
                        readonly size="20" onclick="selectPeopleFun_second()"
                        class="input-300px cursor-hand">
                        <input type="hidden" value="" id="peopleIdSecond" name="peopleIdSecond"></td>
                </tr>



                <tr>
                    <td class="bg-gray" align="right"><fmt:message key="inquiry.allow.anonymous.vote"/>:</td>
                    <td class="new-column">
                        <label for="anonymous_true">
                            <input type="radio" name="anonymousFlag" value="0" id="anonymous_true"  checked/><fmt:message key="common.true" bundle="${v3xCommonI18N}"/>
                        </label>
                        <label for="anonymous_false">
                            <input type="radio" name="anonymousFlag" value="1" id="anonymous_false"> <fmt:message key="common.false" bundle="${v3xCommonI18N}"/>
                        </label>
                    </td>
                </tr>
                <tr>
                    <td class="bg-gray" align="right" valign="top" rowspan="2"><fmt:message key='common.description.label' bundle="${v3xCommonI18N}"/>:</td>
                    <td class="new-column"><textarea rows="4" name="surveydesc" cols="60" class="input-300px" id="surveydesc" inputName="<fmt:message key='common.description.label'  bundle="${v3xCommonI18N}" />" validate="maxLength" maxSize="120"></textarea><br>
                    </td>
                </tr>
            </table>

            </div>


        </td>
    </tr>
    <tr>
        <td height="50" align="center" class="bg-advance-bottom button_container">

            <input type="submit"
                    value="<fmt:message key='common.button.ok.label' bundle='${v3xCommonI18N}'/>"
                    id="B1" name="B1" class="button-default-2 button-default_emphasize">&nbsp;&nbsp;
            <input type="button"
                    value="<fmt:message key='common.button.cancel.label' bundle='${v3xCommonI18N}' />"
                    name="B2" class="button-default-2" onclick="parent.parent.document.location.reload();">

        </td>
    </tr>
</table>

<script type="text/javascript">
	bindOnresize('docLibBody', 0, 70);

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
