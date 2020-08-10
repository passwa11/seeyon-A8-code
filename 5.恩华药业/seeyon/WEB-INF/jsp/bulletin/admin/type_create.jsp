<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../include/taglib.jsp" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/css/layout.css${v3x:resSuffix()}" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/portal/management/css/iconfont.css${v3x:resSuffix()}" />">
    <style type="text/css">
        .new-column .em_write_text {
            position: absolute;
            background: black;
            top: -21px;
            left: 255px;
            color: white;
            width: 200px;
            border-radius: 3px;
            padding-left: 5px;
            padding-top: 5px;
            padding-bottom: 5px;
            filter: alpha(opacity=70); /*IE滤镜，透明度50%*/
            -moz-opacity: 0.7; /*Firefox私有，透明度50%*/
            opacity: 0.7; /*其他，透明度50%*/
        }
    </style>
    <%@ include file="../include/header.jsp" %>
    <title>
        <c:if test="${bean.id!=null}"><fmt:message key='common.toolbar.edit.label' bundle="${v3xCommonI18N}"/></c:if>
        <c:if test="${bean.id==null}"><fmt:message key='common.toolbar.new.label' bundle="${v3xCommonI18N}"/></c:if>
        <fmt:message key="bul.type"/>
    </title>
    <script type="text/javascript">
        <c:if test="${!isGroup&&spaceType!=18&&spaceType!=17}">
        var onlyLoginAccount_manager = true;
        var onlyLoginAccount_auditUser = true;
        </c:if>
        var nameList = new Array();
        <c:forEach var="tname" items="${typeNameList}">
        nameList.push("${v3x:escapeJavascript(tname.typeName)}");
        </c:forEach>
        // <!--
        function isSameName() {
            var theNewName = document.getElementById("typeName");
            for (var j = 0; j < nameList.length; j++) {
                if (theNewName.value.trim() == nameList[j].trim()) {
                    alert(v3x.getMessage("bulletin.bulletin_notAlreay_exists"));
                    theNewName.focus();
                    return false;
                }
            }
            return true;
        }

        function myCheckForm(myform) {
            if (!checkForm(myform)) {
                return false;
            }
            if ($('auditFlag1').checked) {
                var auditUserName = $("auditUserName") == null ? "" : $("auditUserName").value;
                var auditUser = $("auditUser") == null ? "" : $("auditUser").value;
                if (auditUserName == null || auditUserName == '' || auditUserName.indexOf("<") == 0) {
                    alert('<fmt:message key="bul.error.audituser.notblank" />');
                    return false;
                }
                if (auditUser == "${bean.auditUser}" && "${isAlert}" == "true") {
                    alert("审核员不在空间访问范围内，请重新选择!");
                    return false;
                }
            }
            return isSameName();
        }

        function disableSub() {
            document.getElementById("submit").disabled = true;
            return true;
        }

        <c:if test="${isAlert}">
        alert("审核员不在空间访问范围内，请重新选择!");
        </c:if>
        //-->

        function cancle() {
            parent.document.location.reload();
        }

        //恩华药业 zhou Start
        //二开
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
    <base>
</head>
<body style="text-align:center">
<form action="${bulTypeURL}?method=save${ctp:csrfSuffix()}" method="post" target="_parent" onsubmit="return (myCheckForm(this) && disableSub());">
    <input type="hidden" name="spaceId" value="${ctp:toHTML(param.spaceId)}"/>
    <input type="hidden" name="spacetype" value="${ctp:toHTML(param.spaceType)}"/>
    <input type="hidden" name="id" value="${bean.id}"/>
    <input type="hidden" name="needTransfer2NewAuditor" value="${needTransfer2NewAuditor}"/>
    <input type="hidden" name="oldAuditorId" value="${oldAuditorId}"/>
    <c:set var="ro" value="${v3x:outConditionExpression(readOnly, 'readonly', '')}"/>
    <c:set var="dis" value="${v3x:outConditionExpression(readOnly, 'disabled', '')}"/>
    <%--恩华药业:start--%>
    <c:set value="${v3x:parseElementsOfTypeAndId(DEPARTMENTissueArea)}" var="org"/>
    <c:set var="issueAreaName" value="${v3x:showOrgEntitiesOfTypeAndId(DEPARTMENTissueArea, pageContext)}"/>
    <v3x:selectPeople id="spGroup" originalElements="${v3x:escapeJavascript(org)}"
                      panels="Account,Department,Team,Post,Level,JoinOrganization,JoinAccountTag,JoinPost,Guest,BusinessDepartment"
                      selectType="Member,Department,Account,Post,Level,Team,JoinAccountTag,Guest,BusinessAccount,BusinessDepartment"
                      departmentId="" jsFunction="setIssueAreaPeopleFields(elements)"/>
    <%--恩华药业:end--%>
    <table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center" class="">
        <tr align="center">
            <td height="8" class="">
                <script type="text/javascript">
                    getDetailPageBreak();
                </script>
            </td>
        </tr>
        <tr>
            <td valign="top">

                <div id="cenner" style="width: 100%; height: 210px; overflow: auto;">

                    <table border="0" cellpadding="0" cellspacing="0" align="center">
                        <tr>
                            <td class="bg-gray" width="25%" nowrap><font color="red">*</font>&nbsp;<fmt:message key="bul.type.typeName"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%">
                                <fmt:message key="bul.type.typeName" var="_myLabel"/>
                                <fmt:message key="label.please.input" var="_myLabelDefault">
                                    <fmt:param value="${_myLabel}"/>
                                </fmt:message>
                                <c:set var="_value" value="${bean.typeName}"/>
                                <c:if test="${empty bean.typeName}">
                                    <c:set var="_value" value="${_myLabelDefault}"/>
                                </c:if>
                                <input type="text" class="input-100per" name="typeName" id="typeName"
                                       value="${v3x:toHTML(_value)}"
                                       title="${v3x:toHTML(_value)}" defaultValue="${_myLabelDefault}"
                                       onfocus="checkDefSubject(this, true)" onblur="checkDefSubject(this, false)"
                                       inputName="${_myLabel}" validate="notNull,isDefaultValue"
                                       maxSize="30" ${v3x:outConditionExpression(readOnly, 'disabled', '')}/>
                            </td>
                        </tr>
                        <tr class="hidden">
                            <td class="bg-gray" width="25%" nowrap><fmt:message key="bul.type.spaceType"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%">
                                <c:if test="${isGroup || bean.spaceType=='3'}">
                                    <label for="spaceType0">
                                        <input type="radio" name="spaceType" id="spaceType0" value="3" ${dis}/><fmt:message key="bul.type.spaceType.0"/>
                                    </label>
                                </c:if>
                                <label for="spaceType1">
                                    <input type="radio" name="spaceType" id="spaceType1" value="2" ${dis }/><fmt:message key="bul.type.spaceType.1"/>
                                </label>
                                <label for="spaceType2">
                                    <input type="radio" name="spaceType" id="spaceType2" value="1" ${dis }/><fmt:message key="bul.type.spaceType.2"/>
                                </label>
                                <c:if test="${bean.id==null}">
                                    <script type="text/javascript">
                                        if ("${isGroup}" == "true") {
                                            $('spaceType0').checked = true;
                                            $('spaceType1').disable();
                                        } else {
                                            $('spaceType1').checked = true;
                                        }
                                        $('spaceType2').disable();
                                    </script>
                                </c:if>
                                <c:if test="${bean.id!=null}">
                                    <script type="text/javascript">
                                        if ("${bean.spaceType}" == "2") {
                                            $('spaceType1').checked = true;
                                        } else if ("${bean.spaceType}" == "3") {
                                            $('spaceType0').checked = true;
                                            $('spaceType1').disable();
                                        }
                                        $('spaceType2').disable();
                                    </script>
                                </c:if>
                            </td>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap><font color="red">*</font>&nbsp;<fmt:message key="bul.type.managerUsers"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%" ${dis }>
                                <c:set value="${v3x:showOrgEntitiesOfIds(bean.managerUserIds, 'Member', pageContext)}" var="publishScopeValue"/>
                                <c:set value="${v3x:parseElementsOfIds(bean.managerUserIds, 'Member')}" var="org"/>
                                <script type="text/javascript">
                                    <!--
                                    var includeElements_manager = "${v3x:parseElementsOfTypeAndId(entity)}";
                                    //-->
                                </script>

                                <v3x:selectPeople id="manager" originalElements="${org}" panels="Department,Post,Level,Team" selectType="Member" jsFunction="setBulPeopleFields(elements,'managerUserIds','managerUserNames')" maxSize="50"/>

                                <input type="hidden" id="manager_includeElements" name="manager_includeElements" value="${entity}">
                                <fmt:message key="bul.type.managerUsers" var="_myLabel"/>
                                <fmt:message key="label.please.select" var="_myLabelDefault"><fmt:param value="${_myLabel}"/></fmt:message>
                                <input type="hidden" id="managerUserIds" name="managerUserIds" value="${bean.managerUserIds}"/>
                                <input type="text" class="cursor-hand input-100per" id="managerUserNames" name="managerUserNames" readonly="true"
                                       value="<c:out value="${publishScopeValue}" default="${_myLabelDefault}" escapeXml="true" />"
                                       defaultValue="${_myLabelDefault}" onfocus="checkDefSubject(this, true)"
                                       onblur="checkDefSubject(this, false)" inputName="${_myLabel}"
                                       validate="notNull,isDefaultValue" ${v3x:outConditionExpression(readOnly, 'disabled', '')}
                                       onclick="selectPeople('manager','managerUserIds','managerUserNames');"/>
                            </td>
                        </tr>
                        <%--恩华药业 zhou 添加发布范围 start--%>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap>
                                发布范围:
                            </td>
                            <td class="new-column" width="75%">
                                <input type="hidden" id="issueArea" name="sendArrangeId" value="${bean.sendArrangeId}">
                                <input type="text" readonly="true" id="issueAreaName" name="sendArrangeName" deaultValue="${defScope}" class="cursor-hand input-250px"
                                       value="<c:out value="${bean.sendArrangeName}" escapeXml="true" default="${defScope}" />"
                                       onclick="selectIssueArea()"
                                       <c:if test="${param.isDetail=='readOnly' }">disabled</c:if> placeholder="<点击选择发布范围>"/>
                            </td>
                        </tr>
                        <%--恩华药业 zhou 添加发布范围 end--%>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap><fmt:message key="bul.type.auditFlag"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%" ${dis }>
                                <label for="auditFlag1">
                                    <input type="radio" id="auditFlag1" name="auditFlag" value="1"
                                           onclick="if(this.checked) {document.getElementById('auditUserName').disabled=false;document.getElementById('isAuditorModify1').disabled=false;document.getElementById('isAuditorModify0').disabled=false;document.getElementById('finalPublishSel').disabled=false;if(document.getElementById('cenner').parentNode.clientHeight - document.getElementById('cenner').clientHeight > 50){document.getElementById('cenner').style.marginTop = -90 + 'px'}}"/><fmt:message
                                        key="label.audit"/>
                                </label>
                                <label for="auditFlag0">
                                    <input type="radio" id="auditFlag0" name="auditFlag" value="0"
                                           onclick="if(this.checked) {document.getElementById('auditUserName').disabled=true;document.getElementById('isAuditorModify1').disabled=true;document.getElementById('isAuditorModify0').disabled=false;document.getElementById('finalPublishSel').disabled=true;if(document.getElementById('cenner').parentNode.clientHeight - document.getElementById('cenner').clientHeight > 50){document.getElementById('cenner').style.marginTop = -90 + 'px'}}"/><fmt:message
                                        key="label.noaudit"/>
                                </label>
                                <i class="iconfont1 icon-help" style="margin: 5px;font-size: 14px;color:#ff3544;position: relative" title="<fmt:message key='bulletin.manager.help.audit' bundle='${v3xCommonI18N}' />"></i>
                                <script type="text/javascript">
                                    setRadioValue("auditFlag", "<c:if test="${bean.auditFlag}">1</c:if><c:if test="${not bean.auditFlag}">0</c:if>");
                                    if (${readOnly}) {
                                        document.getElementById('auditFlag1').disabled = true;
                                        document.getElementById('auditFlag0').disabled = true;
                                    }
                                </script>
                                <c:if test="${(hasPending || needTransfer2NewAuditor || isAlert)}">
                                    <script type="text/javascript">
                                        $('auditFlag0').disable();
                                    </script>
                                </c:if>
                            </td>
                        </tr>
                        <tr id="auditUser0">
                            <td class="bg-gray" width="25%" nowrap><span><fmt:message key="bul.type.auditUser"/><fmt:message key="bulletin.label.colon"/></span></td>
                            <td class="new-column" width="75%" ${dis }>
                                <c:if test="${hasPending and bean.auditUser!= null}">
                                    <c:set value="noEditAuditUserAlert()" var="clickEvent"/>
                                </c:if>
                                <c:if test="${!hasPending || bean.auditUser == null}">
                                    <c:set value="selectPeople('auditUser','auditUser','auditUserName')" var="clickEvent"/>
                                </c:if>
                                <span id="auditUser1">
                            <fmt:message key="bul.type.auditUser" var="_myLabel"/>
                            <fmt:message key="label.please.select" var="_myLabelDefault"><fmt:param value="${_myLabel}"/></fmt:message>
                            <%-- 三元分离使得此处审核员默认显示为"审计管理员"，需要作一下调整 --%>
                            <c:set value="${bean.auditUser ==null || bean.auditUser == 1 || bean.auditUser == 0}" var="auditDefault"/>
                            <c:set value="${auditDefault=='true' ? _myLabelDefault : v3x:showMemberName(bean.auditUser)}" var="publishScopeValue"/>
                            <c:set value="${auditDefault=='true' ? '' : v3x:parseElementsOfIds(bean.auditUser, 'Member')}" var="org"/>
                            <c:set var="maxSize" value="1"/>
                            <c:if test="${maxSize==null}">
                                <c:set var="maxSize" value=""/>
                            </c:if>
                            <script type="text/javascript">
                            <!--
                            var includeElements_auditUser = "${v3x:parseElementsOfTypeAndId(entity)}";
                            //-->
                            </script>

							<v3x:selectPeople id="auditUser" originalElements="${org}" panels="Department,Post,Level,Team" maxSize="${maxSize}" selectType="Member" jsFunction="setBulPeopleFields(elements,'auditUser','auditUserName')"/>

                            <input type="hidden" name="auditUser" id="auditUser" value="${bean.auditUser}"/>
                            <input type="text" class="cursor-hand input-100per" name="auditUserName" id="auditUserName" readonly="true"
                                   value="<c:out value="${publishScopeValue}" default="${_myLabelDefault}" escapeXml="true" />"
                                   defaultValue="${_myLabelDefault}" onfocus="checkDefSubject(this, true)"
                                   onblur="checkDefSubject(this, false)" inputName="${_myLabel}" validate=""
                            ${v3x:outConditionExpression(readOnly, 'disabled', '')} onclick="${clickEvent}"/>
                        </span>
                            </td>
                        </tr>

                        <tr id="isAuditorModifyTrId">
                            <td class="bg-gray" width="25%" nowrap><span><fmt:message key="bul.auditor.is.modify"/></span><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%" ${dis }>
                                <label for="isAuditorModify1">
                                    <input type="radio" id="isAuditorModify1" ${dis} name="isAuditorModify" value="1"/><fmt:message key="bul.yes"/>
                                </label>
                                <label for="isAuditorModify0">
                                    <input type="radio" id="isAuditorModify0" ${dis} name="isAuditorModify" value="0"/><fmt:message key="bul.no"/>
                                </label>
                            </td>
                        </tr>

                        <script type="text/javascript">
                            if ('${bean.isAuditorModify}' == 'true') {
                                document.getElementById('isAuditorModify1').setAttribute("checked", "checked");
                            } else {
                                document.getElementById('isAuditorModify0').setAttribute("checked", "checked");
                            }
                            <c:if test="${not bean.auditFlag}">
                            document.getElementById('auditUserName').disabled = true;
                            document.getElementById('isAuditorModify1').disabled = true;
                            document.getElementById('isAuditorModify0').disabled = true;
                            </c:if>
                        </script>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap><fmt:message key="bulletin.typeSet.publishSet"/><fmt:message key="bulletin.label.colon"/></td>
                            <%--发布人设置 --%>
                            <td class="new-column" width="75%">
                                <label>
                                    <input type="hidden" id="defaultPublishHidden" name="defaultPublish" value="${bean.defaultPublish?1:0}">
                                    <input type="checkbox" id="defaultPublish" value="0" class="radio_com" ${dis}
                                           onclick="if(this.checked) {document.getElementById('defaultPublishHidden').value =1;}else{document.getElementById('defaultPublishHidden').value =0;}" ${bean.defaultPublish ? 'checked' : ''}>
                                </label>
                                <label for="defaultPublish" class="margin_r_10 hand">
                                    <fmt:message key="bulletin.typeSet.defaultPubAll"/><%--默认勾选【允许显示发布人】 --%>
                                </label>
                            </td>
                            <script type="text/javascript">
                                document.getElementById("defaultPublish").checked = false;
                                var df = "${bean.defaultPublish}";
                                if (df == "true") {
                                    document.getElementById("defaultPublish").checked = true;
                                }
                                if (${readOnly}) {
                                    document.getElementById('defaultPublish').parentNode.parentNode.disabled = true;
                                }
                            </script>
                        </tr>
                        <tr id="finalPublishFi">
                            <td class="bg-gray" width="25%" nowrap></td>
                            <td class="new-column" width="75%">
                                <label class="margin_r_10 hand" style="margin-left: 22px;">
                                    <fmt:message key="bulletin.typeSet.finalPublish"/><fmt:message key="bulletin.label.colon"/><%-- 发布人是 --%>
                                </label>
                                <select id="finalPublishSel" name="finalPublish" class="condition" style="height: 23px; width: 120px;padding-top:0px;" ${dis}>
                                    <option value="0"
                                            <c:if test="${bean.finalPublish==0}">selected</c:if>><fmt:message key="bulletin.typeSet.realPublish"/></option>
                                    <option value="1"
                                            <c:if test="${bean.finalPublish==1}">selected</c:if>><fmt:message key="bulletin.createMember"/></option>
                                    <option value="2"
                                            <c:if test="${bean.finalPublish==2}">selected</c:if>><fmt:message key="bulletin.auditMember"/></option>
                                </select>
                                </br>
                            </td>
                            <script type="text/javascript">
                                if (${readOnly}) {
                                    document.getElementById('finalPublishSel').disabled = true;
                                }
                                <c:if test="${not bean.auditFlag}">
                                document.getElementById('finalPublishSel').disabled = true;
                                </c:if>
                            </script>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap></td>
                            <td class="new-column" width="75%" style="position: relative;">
                                <label style="margin-left: 83px">
                                    <input type="hidden" id="writePermitHidden" name="writePermit" value="${bean.writePermit?1:0}">
                                    <input type="checkbox" id="writePermit" class="radio_com" ${dis}
                                           onclick="if(this.checked) {document.getElementById('writePermitHidden').value =1;}else{document.getElementById('writePermitHidden').value =0;}" ${bean.writePermit ? 'checked' : ''}>
                                </label>
                                <label for="writePermit" class="margin_r_10 hand">
                                    <fmt:message key="bulletin.typeSet.writePermit"/><%--允许选择或输入人员 --%>
                                </label>
                                <em id="write_help" class="ico16 help_16"
                                    onmouseover='document.getElementById("write_help_text").style.display="block";'
                                    onmouseout='document.getElementById("write_help_text").style.display="none";'>
                                </em>
                                <em class="em_title em_title_content em_write_text" style="display: none;" id="write_help_text">
                                    <fmt:message key="bulletin.publishChoose.tips"/>
                                </em>
                            </td>
                            <script type="text/javascript">
                                document.getElementById("writePermit").checked = false;
                                var wp = "${bean.writePermit}";
                                if (wp == "true") {
                                    document.getElementById("writePermit").checked = true;
                                }
                                if (${readOnly}) {
                                    document.getElementById('writePermit').parentNode.parentNode.disabled = true;
                                }
                            </script>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap valign="top"><fmt:message key="bul.mode"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%" ${dis }>
                                <table>
                                    <tr>
                                        <td>
                                            <img src="<c:url value='/apps_res/bulletin/images/standard1.png' />" alt='<fmt:message key="bul.standard"/>' width="84" height="102" class="${dis=='disabled'?' ':'cursor-hand'}" ${dis }
                                                 onClick="javascript:chooseStyle('a');">
                                        </td>
                                        <td>
                                            <img src="<c:url value='/apps_res/bulletin/images/standard2.png' />" alt='<fmt:message key="bul.standard1"/>' width="84" height="102" class="${dis=='disabled'?' ':'cursor-hand'}" ${dis }
                                                 onClick="javascript:chooseStyle('d');">
                                        </td>
                                        <td>
                                            <img src="<c:url value='/apps_res/bulletin/images/formal1.png' />" alt='<fmt:message key="bul.formal"/>' width="84" height="102" class="${dis=='disabled'?' ':'cursor-hand'}" ${dis }
                                                 onClick="javascript:chooseStyle('b');">
                                        </td>
                                        <td>
                                            <img src="<c:url value='/apps_res/bulletin/images/formal2.png' />" alt='<fmt:message key="bul.formal2"/>' width="84" height="102" class="${dis=='disabled'?' ':'cursor-hand'}" ${dis }
                                                 onClick="javascript:chooseStyle('c');">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <input type="hidden" name="readOnly" id="readOnly" value="${dis}"/>
                                            <label for="a">
                                                <input type="radio"
                                                       <c:if test="${bean.ext1=='0'}">checked</c:if> ${dis} name="ext1" id="a" value="0"/><fmt:message key="bul.standard"/>
                                            </label>
                                        </td>
                                        <td>
                                            <label for="d">
                                                <input type="radio"
                                                       <c:if test="${bean.ext1=='3'}">checked</c:if> ${dis} name="ext1" id="d" value="3"/><fmt:message key="bul.standard1"/>
                                            </label>
                                        </td>
                                        <td>
                                            <label for="b">
                                                <input type="radio"
                                                       <c:if test="${bean.ext1=='1'}">checked</c:if> ${dis} name="ext1" id="b" value="1"/><fmt:message key="bul.formal"/>
                                            </label>
                                        </td>
                                        <td>
                                            <label for="c">
                                                <input type="radio"
                                                       <c:if test="${bean.ext1=='2'}">checked</c:if> ${dis} name="ext1" id="c" value="2"/><fmt:message key="bul.formal2"/>
                                            </label>
                                        </td>
                                    </tr>
                                </table>
                                <c:if test="${bean.id==null}">
                                    <script type="text/javascript">
                                        $('a').checked = true;
                                    </script>
                                </c:if>
                                <c:if test="${bean.ext1!='0' && bean.ext1!='1' && bean.ext1!='2' && bean.ext1!='3'}">
                                    <script type="text/javascript">
                                        $('a').checked = true;
                                    </script>
                                </c:if>
                            </td>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap><fmt:message key="bulletin.typeSet.printSet"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%">
                                <label>
                                    <input type="hidden" id="printFlagHidden" name="printFlag" value="${bean.printFlag?1:0}">
                                    <input type="checkbox" id="printFlag" class="radio_com" ${dis} onclick="allowPrint(this)" ${bean.printFlag ? 'checked' : ''}>
                                </label>
                                <label for="printFlag" class="margin_r_10 hand">
                                    <fmt:message key="bul.type.printFlag"/><%--允许打印 --%>
                                </label>
                            </td>
                            <script type="text/javascript">
                                if (${readOnly}) {
                                    document.getElementById('printFlag').parentNode.parentNode.disabled = true;
                                }
                            </script>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap></td>
                            <td class="new-column" width="75%">
                                <label>
                                    <input type="hidden" id="printDefaultHidden" name="printDefault" value="${bean.printDefault?1:0}">
                                    <input type="checkbox" id="printDefault" value="1" class="radio_com" onclick="allPrintDefault(this);" ${bean.printDefault ? 'checked' : ''}>
                                </label>
                                <label for="printDefault" class="margin_r_10 hand">
                                    <fmt:message key="bulletin.typeSet.defaultPrintAll"/><%--默认勾选【允许打印】 --%>
                                </label>
                            </td>
                            <script type="text/javascript">
                                if (document.getElementById("printDefault") != null) {
                                    document.getElementById("printDefault").checked = false;
                                    var pd = "${bean.printDefault}";
                                    if (pd == "true") {
                                        document.getElementById("printDefault").checked = true;
                                    }
                                }
                                var pf = "${bean.printFlag}";
                                if (pf != "" && pf == "false" || ${readOnly}) {
                                    document.getElementById('printDefault').parentNode.parentNode.disabled = true;
                                    document.getElementById('printDefault').disabled = true;
                                    document.getElementById('printDefault').parentNode.parentNode.setAttribute('style', 'color:gray;');
                                }

                                function allowPrint(obj) {
                                    if (obj.checked) {
                                        document.getElementById('printFlagHidden').setAttribute('value', '1');
                                        document.getElementById('printDefault').parentNode.parentNode.removeAttribute('disabled');
                                        document.getElementById('printDefault').parentNode.parentNode.removeAttribute('style');
                                        document.getElementById('printDefault').disabled = false;
                                        if (document.getElementById('cenner').parentNode.clientHeight - document.getElementById('cenner').clientHeight > 50) {
                                            document.getElementById('cenner').style.marginTop = -90 + 'px'
                                        }
                                    } else {
                                        document.getElementById('printFlagHidden').setAttribute('value', '0');
                                        document.getElementById('printDefault').parentNode.parentNode.setAttribute('disabled', 'true');
                                        document.getElementById('printDefault').parentNode.parentNode.setAttribute('style', 'color:gray;');
                                        document.getElementById('printDefault').disabled = true;
                                        if (document.getElementById('cenner').parentNode.clientHeight - document.getElementById('cenner').clientHeight > 50) {
                                            document.getElementById('cenner').style.marginTop = -90 + 'px'
                                        }
                                    }
                                }

                                function allPrintDefault(thisobj) {
                                    if (thisobj.checked) {
                                        document.getElementById('printDefaultHidden').setAttribute('value', '1');
                                    } else {
                                        document.getElementById('printDefaultHidden').setAttribute('value', '0');
                                    }
                                }
                            </script>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap><fmt:message key="bul.type.topCount"/><fmt:message key="bulletin.label.colon"/></td>
                            <input type="hidden" name="oldTopCount" value="${bean.topCount}"/>
                            <td class="new-column" width="75%">
                                <select name="topCount" ${dis }>
                                    <v3x:metadataItem metadata="${topCountMetaData}"
                                                      showType="option" name="topCount" selected="${bean.topCount}" switchType="input"/>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap valign="top"><fmt:message key='common.description.label' bundle="${v3xCommonI18N}"/><fmt:message key="bulletin.label.colon"/></td>
                            <td class="new-column" width="75%">
                                <textarea name="description" rows="5" cols="60"  ${v3x:outConditionExpression(readOnly, 'readonly', '')} maxSize="80" validate="maxLength"
                                          inputName="<fmt:message key='common.description.label' bundle="${v3xCommonI18N}"/>">${bean.description}</textarea>
                            </td>
                        </tr>
                        <tr>
                            <td class="bg-gray" width="25%" nowrap></td>
                            <td class="new-column" width="75%">
                            </td>
                        </tr>
                    </table>

                </div>


            </td>
        </tr>
        <c:if test="${!readOnly}">
            <tr>
                <td height="50" align="center" class="bg-advance-bottom button_container">

                    <input type="submit" id="submit" class="button-default_emphasize common_button_emphasize" value="<fmt:message key='common.button.ok.label' bundle="${v3xCommonI18N}" />"/>&nbsp;
                    <input type="button" class="button-default-2" value="<fmt:message key='common.button.cancel.label' bundle="${v3xCommonI18N}" />" onclick="cancle();"/>

                </td>
            </tr>
        </c:if>
    </table>


    <!-- 滚动条方法 START -->
    <script type="text/javascript">
        var isread = '${readOnly}';
        if ('true' == isread) {
            //var RTEEditorDivHeight = document.documentElement.clientHeight;
            //document.getElementById('cenner').style.height= RTEEditorDivHeight +'px';
            bindOnresize('cenner', 0, 10);
        } else {
            bindOnresize('cenner', 0, 64);
        }
    </script>
    <!-- 滚动条方法 END -->

</form>
</body>
</html>
