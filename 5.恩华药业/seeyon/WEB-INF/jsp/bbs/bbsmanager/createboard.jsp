<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title></title>
<%@ include file="../header.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
<c:set var="isGroup" value="${v3x:currentUser().groupAdmin}" />
<script type="text/javascript">
	var hasIssueArea = false;

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
		theForm.action = "${detailURL}?method=createBoard&spaceType=${param.spaceType}&spaceId=${param.spaceId}${ctp:csrfSuffix()}";
		if (checkForm(theForm)) {
		   var nameList = new Array();
		    //初始化讨论类型名称列表
		   <c:forEach var="tname" items="${nameList}">
		         nameList.push("${v3x:escapeJavascript(tname)}");
		   </c:forEach>
		      var boardName = document.fm.name.value.trim();
		      for(var i=0;i<nameList.length;i++){
		         if(boardName==nameList[i].trim()){
		          alert(v3x.getMessage("BBSLang.bbs_bbsmanage_createboard_sameness"));
		          document.fm.name.focus();
		          return false;
		        }
		      }

		    document.fm.name.value = document.fm.name.value.trim();
            document.getElementById("b1").disabled = true;
            document.getElementById("b2").disabled = true;

        	theForm.submit();
   	 	}
	}

	//管理员是否为空
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
            document.getElementById("coverImage").setAttribute("src", "${pageContext.request.contextPath}/fileUpload.do?method=showRTE&fileId=" + retValue + "&type=image");
            document.getElementById("imageId").value=value_id;
        }
    }
</script>
<script type="text/javascript">
<!--
var includeElements_wf = "${v3x:parseElementsOfTypeAndId(entity)}";
//-->
</script>
<v3x:selectPeople id="wf" panels="Department,Post,Level,Team" selectType="Member" jsFunction="setPeopleFields(elements)" maxSize="50" />

</head>
<body scroll="no" style="overflow: no; text-align:center">
<form name="fm" method="post" action="" onsubmit="return checkForm(this)">
<input type="hidden" value="${newId}" name="newId" />
    <%--恩华药业:start--%>
    <c:set value="${v3x:parseElementsOfTypeAndId(DEPARTMENTissueArea)}" var="org"/>
    <c:set var="issueAreaName" value="${v3x:showOrgEntitiesOfTypeAndId(DEPARTMENTissueArea, pageContext)}"/>
    <v3x:selectPeople id="spGroup" originalElements="${v3x:escapeJavascript(org)}"
                      panels="Account,Department,Team,Post,Level,JoinOrganization,JoinAccountTag,JoinPost,Guest,BusinessDepartment"
                      selectType="Member,Department,Account,Post,Level,Team,JoinAccountTag,Guest,BusinessAccount,BusinessDepartment"
                      departmentId="" jsFunction="setIssueAreaPeopleFields(elements)"/>
    <%--恩华药业:end--%>
<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center" class="">
    <tr>
        <td  valign="top">

            <div id="cenner" style="overflow: auto;">

            <table border="0" cellpadding="0" cellspacing="0" height="100%" align="center">
                <tr>
                    <td class="bg-gray , bbs-tb-padding-topAndBottom" width="25%" nowrap>
                        <font color="red">*</font>&nbsp;<fmt:message key="bbs.type.typeName"/>:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" width="75%" colspan="3">
                        <fmt:message key="common.default.name.value" var="defName" bundle="${v3xCommonI18N}" />
                        <input name="name" type="text" id="name" class="input-100per" deaultValue="${defName}"
                           inputName="<fmt:message key='bbs.type.typeName'/>" validate="isDeaultValue,notNull,maxLength" maxSize="30"
                            value="${defName}"
                            ${v3x:outConditionExpression(readOnly, 'readonly', '')}
                           onfocus='checkDefSubject(this, true)' onblur="checkDefSubject(this, false)">
                    </td>
                </tr>

                <tr>
                    <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <font color="red">*</font>&nbsp;<fmt:message key="bbs.admin.label"/>:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" colspan="3" nowrap>
                        <fmt:message key="common.default.selectPeople.value" var="defaultSP" bundle="${v3xCommonI18N}"/>
                        <fmt:message key="bbs.type.managerUsers" var="_myLabel"/>
                        <input type="hidden" value="" name="bbsBoardAdmin">
                        <input type="text" name='bbsBoardAdminName'
                            value="${defaultSP}" inputName="${_myLabel}" validate="notNull,isDefaultValue"
                            readonly class="cursor-hand input-100per" onclick="selectBoardAdmin()"
                            deaultValue="${defaultSP}">
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
                    <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                        <label for="userFlag_true">
                            <input type="radio" name="userFlag" value="0" id="userFlag_true"  checked/><fmt:message key="bbs.type.usedFlag.enable" bundle="${v3xCommonI18N}" />
                        </label>
                        <label for="userFlag_false">
                            <input type="radio" name="userFlag" value="2" id="userFlag_false"  ><fmt:message key="bbs.type.usedFlag.disable" bundle="${v3xCommonI18N}" />
                        </label>
                    </td>
                </tr>
                <tr>
                    <td class="bg-gray , bbs-tb-padding-topAndBottom" width="25%" nowrap>
                    <fmt:message key="bbs.sort" />:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                            <select  name="orderFlag"  class="condition" style="height: 23; width: 80">
                        <option  value="0" ><fmt:message key="bbs.sort.asc" /></option>
                        <option  value="1" ><fmt:message key="bbs.sort.desc"/></option>
                      </select>
                    </td>
                </tr>
                <tr>
                    <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <fmt:message key="bbs.allow.anonymous.label" />:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                        <label for="anonymous_true">
                            <input type="radio" name="anonymousFlag" value="0" id="anonymous_true"  checked/><fmt:message key="common.yes" bundle="${v3xCommonI18N}" />
                        </label>
                        <label for="anonymous_false">
                            <input type="radio" name="anonymousFlag" value="1" id="anonymous_false"  ><fmt:message key="common.no" bundle="${v3xCommonI18N}" />
                        </label>
                    </td>
                </tr>

                <tr>
                    <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <fmt:message key="bbs.allow.anonymous.reply.label" />:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                        <label for="a">
                            <input type="radio" name="anonymousReplyFlag" value="0" id="a"  checked/><fmt:message key="common.yes" bundle="${v3xCommonI18N}" />
                        </label>
                        <label for="b">
                            <input type="radio" name="anonymousReplyFlag" value="1" id="b" ><fmt:message key="common.no" bundle="${v3xCommonI18N}" />
                        </label>
                    </td>
                </tr>

                <tr>
                    <td class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <fmt:message key="bbs.topnumber.label" />:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                        <select name="topNumber" class="condition" style="height: 23; width: 80">
                            <option value="0"><fmt:message key="bbs.createboard.common.name.zero" /></option>
                            <option value="1"><fmt:message key="bbs.createboard.common.name.one"/></option>
                            <option value="2"><fmt:message key="bbs.createboard.common.name.two" /></option>
                            <option value="3" selected><fmt:message key="bbs.createboard.common.name.three" /></option>
                            <option value="4"><fmt:message key="bbs.createboard.common.name.four" /></option>
                            <option value="5"><fmt:message key="bbs.createboard.common.name.five"  /></option>
                        </select>
                    </td>
                </tr>

                <tr>
                    <td valign="top" class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <fmt:message key="common.description.label"  bundle="${v3xCommonI18N}" />:
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom " colspan="3">
                        <textarea id="description" name="description" class="input-100per"  rows="5" inputName="<fmt:message key='common.description.label'  bundle="${v3xCommonI18N}" />" validate="maxLength" maxSize="120"></textarea>
                    </td>
                </tr>
                <tr>
                    <td valign="top" class="bg-gray , bbs-tb-padding-topAndBottom" nowrap>
                        <fmt:message key="bbs.createboard.common.cover"/>:
                        <input type="hidden" id="imageId" name="imageId" value="">
                    </td>
                    <td class="new-column , bbs-tb-padding-topAndBottom" nowrap>
                       <div id="coverImgDiv" style="float:left;">
                            <img id="coverImage" width="216" height="165" name="coverImge" src="${pageContext.request.contextPath}/apps_res/bbs/css/images/${bbs:getBoardImage(newId)}"/>
                        </div>
                        &nbsp; &nbsp; &nbsp;
                        <input id="cover" name="cover"  type="button" onclick="doOnclick();" value="<fmt:message key='bbs.createboard.upload'/>" style="float:left,width:55px"/>
                    </td>
                </tr>
            </table>

            </div>


        </td>
    </tr>
    <tr>
        <td height="50" align="center" class="bg-advance-bottom button_container">

            <input type="button" id="b1" onclick="submitForm()" value="<fmt:message key='common.button.ok.label' bundle="${v3xCommonI18N}" />" class="button-default-2 button-default_emphasize">&nbsp;
            <input type="button" id="b2" value="<fmt:message key='common.button.cancel.label' bundle="${v3xCommonI18N}" />" class="button-default-2" onclick="parent.parent.document.location.reload();">

        </td>
    </tr>
</table>


<script type="text/javascript">
bindOnresize('cenner', 0, 50);
</script>
</form>
</body>
</html>
