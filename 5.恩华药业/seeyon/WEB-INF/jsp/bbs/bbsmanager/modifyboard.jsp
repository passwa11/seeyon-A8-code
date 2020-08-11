<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<html>
<head>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<%@ include file="../header.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
<title></title>
<c:set var="isGroup" value="${v3x:currentUser().groupAdmin}" />
<script type="text/javascript">
	//是否选择管理员判断，修改页面肯定有管理员，而且不能取消人员 ，现在由false改true,解决点击修改后，不做人员变动会谈提示。
	var hasIssueArea = true;

	function selectBoardAdmin(){
		selectPeopleFun_wf();
	}

	function setPeopleFields(elements){
		if(!elements){
			return;
		}
		document.fm.bbsBoardAdmin.value=getIdsString(elements, false);
		document.fm.bbsBoardAdminName.value=getNamesString(elements);
		hasIssueArea = true;
	}

	function submitForm(){
		var theForm = document.getElementsByName("fm")[0];
		if (!theForm) {
        	return;
    	}
    	if(document.getElementById("bbsBoardAdminName").value=='' || document.getElementById("bbsBoardAdminName").value=='<fmt:message key="common.default.selectPeople.value" bundle="${v3xCommonI18N}"/>') {
    		alert(v3x.getMessage("BBSLang.bbs_bbsmanage_createboard_choice"));
    		return;
    	}
		theForm.action = "${detailURL}?method=modifyBoard&spaceType=${param.spaceType}&spaceId=${param.spaceId}${ctp:csrfSuffix()}";
		if (checkForm(theForm) && checkSelectWF()) {
		  var nameList = new Array();
	    	//初始化讨论类型名称列表
	      <c:forEach var="tname" items="${nameList}">
	            nameList.push("${v3x:escapeJavascript(tname)}");
	      </c:forEach>
	         var boardName = document.fm.name.value.trim();
	         for(var i=0;i<nameList.length;i++){
	            if(boardName==nameList[i].trim()&&boardName!='${v3x:escapeJavascript(bbsBoard.name)}'.trim()){
	             alert(v3x.getMessage("BBSLang.bbs_bbsmanage_createboard_sameness"));
	             document.fm.name.focus();
	             return false;
	           }
	         }
	         // 快速需求(Fast Demand):增加板块停用状态,对原来是启用状态的修改为停用状态的增加提示
	         var isDisable = document.getElementById("userFlag_false").checked;
	         var oldUserFlag = '${v3x:escapeJavascript(bbsBoard.flag)}';
	         if(isDisable && oldUserFlag==0){
	            if(confirm(v3x.getMessage("BBSLang.bbs_bbsmanage_modifyboard_userflag"))){
	                document.fm.name.value = document.fm.name.value.trim();
	                theForm.submit();
	            }else{
	                return;
	            }
	         }else{
	            document.fm.name.value = document.fm.name.value.trim();
	            theForm.submit();
	         }
   	 	}
	}

	function checkSelectWF() {
	    if (!hasIssueArea) {
	        alert(v3x.getMessage("BBSLang.bbs_bbsmanage_createboard_choice"));
	        selectPeopleFun_wf();
	        return false;
	    }
	    return true;
	}

  <c:if test="${!isGroup&&spaceType!=18&&spaceType!=17}">
   var onlyLoginAccount_wf = true;
  </c:if>
   function doOnclick(){
	      try{
	          getA8Top().headImgCuttingWin = getA8Top().$.dialog({
	                id : "headImgCutDialog",
	                title : v3x.getMessage('BBSLang.bbs_board_cover'),
	                transParams:{'parentWin':window},
	                url: "${pageContext.request.contextPath}/portal/portalController.do?method=headImgCutting&cutImg=/apps_res/bbs/css/images/cover_1.jpg&cutWidth=216&cutHeight=165${ctp:csrfSuffix()}",
	                width: 750,
	                height: 400,
	                isDrag:false
	         });
	      }catch(e){}
	}

	function headImgCuttingCallBack (retValue) {
	    var value_id= retValue.toString().substr(0,retValue.indexOf("&"));
	    getA8Top().headImgCuttingWin.close();
	    if(retValue != undefined){
	        document.getElementById("coverImage").setAttribute("src", "${pageContext.request.contextPath}/fileUpload.do?method=showRTE&fileId=" + retValue + "&type=image${ctp:csrfSuffix()}");
	        document.getElementById("imageId").value=value_id;
	    }
	}
</script>
</head>
<body scroll="no" style="overflow: no">
<form name="fm" method="post" action="" onsubmit="return checkForm(this)">
<input name="id" type="hidden" value="${bbsBoard.id}">
<c:set var="readOnly" value="${param.isDetail eq 'readOnly'}" />
<c:set var="dis" value="${v3x:outConditionExpression(readOnly, 'disabled', '')}" />
<c:set value="${v3x:joinDirectWithSpecialSeparator(bbsBoard.admins, ',')}" var="adminId"/>
<c:set value="${v3x:showOrgEntitiesOfIds(adminId, 'Member', pageContext)}" var="adminName"/>
    <%--恩华药业:start zhou--%>
    <c:set value="${v3x:parseElementsOfTypeAndId(DEPARTMENTissueArea)}" var="org"/>
    <c:set var="issueAreaName" value="${v3x:showOrgEntitiesOfTypeAndId(DEPARTMENTissueArea, pageContext)}"/>
    <v3x:selectPeople id="spGroup" originalElements="${v3x:escapeJavascript(org)}"
                      panels="Account,Department,Team,Post,Level,JoinOrganization,JoinAccountTag,JoinPost,Guest,BusinessDepartment"
                      selectType="Member,Department,Account,Post,Level,Team,JoinAccountTag,Guest,BusinessAccount,BusinessDepartment"
                      departmentId="" jsFunction="setIssueAreaPeopleFields(elements)"/>
    <%--恩华药业:end--%>
<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center" class="">
    <tr>
        <td>

            <div id="connect" class="scrollList">

                <table width="500" border="0" cellspacing="0" cellpadding="0" align="center">
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" width="25%" nowrap>
                            <font color="red">*</font>&nbsp;<fmt:message key="bbs.type.typeName"/>:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" width="75%">
                            <fmt:message key="common.default.name.value" var="defName" bundle="${v3xCommonI18N}" />
                            <input name="name" type="text" id="name" class="input-100per" deaultValue="${defName}" ${dis}
                               inputName="<fmt:message key='bbs.type.typeName'/>" validate="isDeaultValue,notNull,maxLength" maxSize="30"
                               value="<c:out value="${bbsBoard.name}" escapeXml="true" default='${defName}' />"
                               onfocus='checkDefSubject(this, true)' onblur="checkDefSubject(this, false)">
                        </td>
                    </tr>
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                            <font color="red">*</font>&nbsp;<fmt:message key="bbs.admin.label" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap ${dis}>
                        <script type="text/javascript">
                            <!--
                            var includeElements_wf = "${v3x:parseElementsOfTypeAndId(entity)}";
                            //-->
                            </script>

                             <v3x:selectPeople id="wf" panels="Department,Post,Level,Team" selectType="Member"
                                jsFunction="setPeopleFields(elements)" maxSize="50" originalElements="${v3x:parseElementsOfIds(adminId, 'Member')}"/>

                            <fmt:message key="common.default.selectPeople.value" var="defaultSP" bundle="${v3xCommonI18N}"/>
                            <input type="hidden" value="${adminId}" name="bbsBoardAdmin" >
                            <input type="text" name='bbsBoardAdminName' id='bbsBoardAdminName' value="<c:out value='${adminName}' default='${defaultSP}' escapeXml='true' />"
                                readonly class="cursor-hand input-100per" onclick="selectBoardAdmin()" deaultValue="${defaultSP}" ${dis} >
                        </td>
                    </tr>
                    <%--恩华药业  发送范围  zhou--%>
                    <tr>
                        <td class="bg-gray" width="25%" nowrap>
                            发布范围:
                        </td>
                        <td class="new-column" width="75%">
                            <input type="hidden" id="issueArea" name="sendArrangeId" value="<c:out value="${range.rangeId}" /> ">
                            <input type="text" readonly="true" id="issueAreaName" name="sendArrangeName" deaultValue="${defScope}" class="cursor-hand input-250px"
                                   value="<c:out value="${range.rangeName}" escapeXml="true" default="${defScope}" />"
                                   onclick="selectIssueArea()"
                                   <c:if test="${param.isDetail=='readOnly' }">disabled</c:if> placeholder="<点击选择发布范围>"/>
                        </td>
                    </tr>
                    <script type="text/javascript">
                        //恩华药业 zhou Start
                        function selectIssueArea() {
                            selectPeopleFun_spGroup();
                        }
                        function setIssueAreaPeopleFields(elements) {
                            if (!elements) {
                                return;
                            }
                            document.getElementById("issueArea").value = getIdsString(elements);
                            document.getElementById("issueAreaName").value = getNamesString(elements);
                            hasIssueArea = true;
                        }
                        //恩华药业 zhou end
                    </script>
                    <%--恩华药业 zhou 添加发布范围 end--%>

                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                            <fmt:message key="bbs.type.usedFlagState" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap ${dis}>
                            <label for="userFlag_true">
                                <input type="radio"  id="userFlag_true" name="userFlag" value="0"
                                <c:if test="${bbsBoard.flag==0}">checked</c:if> ${dis} /><fmt:message key="bbs.type.usedFlag.enable" bundle="${v3xCommonI18N}" />
                            </label>
                            <label for="userFlag_false">
                                <input type="radio" name="userFlag"  id="userFlag_false" value="2"
                                <c:if test="${bbsBoard.flag==2}">checked</c:if> ${dis} /><fmt:message key="bbs.type.usedFlag.disable" bundle="${v3xCommonI18N}" />
                            </label>
                       </td>
                    </tr>
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <fmt:message key="bbs.sort" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap ${dis}>
                                <select  name="orderFlag"  class="condition" style="height: 23; width: 80" ${dis}>
                            <option  value="0"
                                <c:if test="${bbsBoard.orderFlag==0}">selected</c:if>><fmt:message key="bbs.sort.asc" /></option>
                            <option  value="1"
                                <c:if test="${bbsBoard.orderFlag==1}">selected</c:if>><fmt:message key="bbs.sort.desc"/></option>
                          </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                            <fmt:message key="bbs.allow.anonymous.label" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap ${dis}>
                            <label for="anonymous_true">
                                <input type="radio"  id="anonymous_true" name="anonymousFlag" value="0"
                                    <c:if test="${bbsBoard.anonymousFlag==0}">checked</c:if> ${dis} /><fmt:message key="common.yes" bundle="${v3xCommonI18N}" />
                            </label>
                            <label for="anonymous_false">
                                <input type="radio" name="anonymousFlag"  id="anonymous_false" value="1"
                                    <c:if test="${bbsBoard.anonymousFlag==1}">checked</c:if> ${dis} /><fmt:message key="common.no" bundle="${v3xCommonI18N}" />
                            </label>
                         </td>
                    </tr>
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                            <fmt:message key="bbs.allow.anonymous.reply.label" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap ${dis}>
                            <label for="a">
                                <input type="radio"  id="a" name="anonymousReplyFlag" value="0"
                                    <c:if test="${bbsBoard.anonymousReplyFlag==0}">checked</c:if> ${dis} /><fmt:message key="common.yes" bundle="${v3xCommonI18N}" />
                            </label>
                            <label for="b">
                                <input type="radio" name="anonymousReplyFlag"  id="b" value="1"
                                    <c:if test="${bbsBoard.anonymousReplyFlag==1}">checked</c:if> ${dis} /><fmt:message key="common.no" bundle="${v3xCommonI18N}" />
                            </label>
                         </td>
                    </tr>
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                            <fmt:message key="bbs.topnumber.label" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap ${dis}>
                            <select name="topNumber" class="condition" style="height: 23; width: 80" ${dis}>
                            <option value="0"
                                <c:if test="${bbsBoard.topNumber==0}">selected</c:if>><fmt:message key="bbs.createboard.common.name.zero"/></option>
                            <option value="1"
                                <c:if test="${bbsBoard.topNumber==1}">selected</c:if>><fmt:message key="bbs.createboard.common.name.one" /></option>
                            <option value="2"
                                <c:if test="${bbsBoard.topNumber==2}">selected</c:if>><fmt:message key="bbs.createboard.common.name.two"/></option>
                            <option value="3"
                                <c:if test="${bbsBoard.topNumber==3}">selected</c:if>><fmt:message key="bbs.createboard.common.name.three"/></option>
                            <option value="4"
                                <c:if test="${bbsBoard.topNumber==4}">selected</c:if>><fmt:message key="bbs.createboard.common.name.four"/></option>
                            <option value="5"
                                <c:if test="${bbsBoard.topNumber==5}">selected</c:if>><fmt:message key="bbs.createboard.common.name.five"/></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap valign="top">
                            <fmt:message key="common.description.label"  bundle="${v3xCommonI18N}" />:
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom">
                            <textarea id="description" name="description"  ${dis} style="width: 400px;" cols="60" rows="5" inputName="<fmt:message key='common.description.label'  bundle="${v3xCommonI18N}" />" validate="maxLength" maxSize="120"><c:out value="${bbsBoard.description}" escapeXml="true"/></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                            <fmt:message key="bbs.createboard.common.cover"/>:
                            <input type="hidden" id="imageId" name="imageId" value="">
                        </td>
                        <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                            <c:if test="${bbsBoard.imageId==null}">
                                <div id="coverImgDiv" style="float:left;">
                                  <img id="coverImage" width="216" height="165" name="coverImge" src="${pageContext.request.contextPath}/apps_res/bbs/css/images/${bbs:getBoardImage(bbsBoard.id)}"/>
                                </div>
                            </c:if>
                            <c:if test="${bbsBoard.imageId!=null}">
                                <div id="coverImgDiv" style="float:left;">
                                  <img id="coverImage" width="216" height="165" name="coverImge" src="${pageContext.request.contextPath}/fileUpload.do?method=showRTE&fileId=${bbsBoard.imageId}&type=image"/>
                                </div>
                            </c:if>
                            &nbsp; &nbsp; &nbsp;
                            <input id="cover" name="cover"  type="button" onclick="doOnclick();" value="<fmt:message key='bbs.createboard.upload'/>" style="float:left,width:55px" ${dis}/>
                        </td>
                    </tr>
                </table>

            </div>


        </td>
    </tr>
    <c:if test="${!readOnly}">
    <tr>
        <td height="50" align="center" class="bg-advance-bottom button_container">

			<input type="button" onclick="submitForm()" value="<fmt:message key='common.button.ok.label' bundle="${v3xCommonI18N}" />" class="button-default-2 button-default_emphasize">&nbsp;
			<input type="button" onclick="parent.parent.document.location.reload();" value="<fmt:message key='common.button.cancel.label' bundle="${v3xCommonI18N}" />" class="button-default-2">

        </td>
    </tr>
    </c:if>
</table>


<script type="text/javascript">
	var isread = ${readOnly};
	if(isread){
		document.getElementById('connect').style.height = "240px";
	}
	bindOnresize('connect',0, 50)
</script>
</form>
</body>
