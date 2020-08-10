<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html style="height:100%;">
<%@ include file="./header.jsp" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="renderer" content="webkit">
<title >
  <c:choose>
    <c:when test="${param.bulId!=''&&param.bulId!=null&&param.bulId!='null'}">
      <fmt:message key="bulletin.modifyBul"/>
    </c:when>
    <c:otherwise>
      <fmt:message key="bulletin.createBul"/>
    </c:otherwise>
  </c:choose>
</title>
<link rel="stylesheet" type="text/css" href="${path}/skin/dist/modules/bulletin.css${v3x:resSuffix()}" />
<script type="text/javascript" src="${path}/apps_res/bulletin/js/bulEdit.js${v3x:resSuffix()}"></script>
<script type="text/javascript">
var _path = '${path}';
var alert_noNull = "<fmt:message key='bul.alert.newsType.is.noNull' />";
var publishScope_noNull = "<fmt:message key='bbs.create.alert3.js' />";
var alert_temp = "<fmt:message key='bulletin.bulTemplLoad' />";
var alert_spaceType_select = "<fmt:message key='bulletin.spaceType_select' />";
var alert_pleWritePublish = "<fmt:message key='bulletin.edit.pleWritePublish' />";
var alert_pleChoosePublish= "<fmt:message key='bulletin.edit.pleChoosePublish' />";
var alert_pleWriteLen1= "<fmt:message key='bulletin.edit.pleWrite.length1' />";
var alert_pleWriteLen2= "<fmt:message key='bulletin.edit.pleWrite.length2' />";
var bulsender = "<fmt:message key='bulletin.createMember' />";
var bulAuditer = "<fmt:message key='bulletin.auditMember' />";
var bulRealer = "<fmt:message key='bulletin.typeSet.realPublish' />";

function writePublishSelect(elements) {
  if (!elements) {
    return;
  }
  document.getElementById('choosePublshId').value=getIdsString(elements,false);
  document.getElementById('writePublishSel').value=getNamesString(elements);
  activeOcx();
}

function setDeptPeopleFields(elements) {
  if (!elements) {
    return;
  }
  document.getElementById('publishDepartmentId').value=getIdsString(elements,false);
  document.getElementById('publishDepartmentName').value=getNamesString(elements);
  activeOcx();
}

function onfocusColor(obj){
   if (obj.style.color == "gray") {
    obj.style.color = '#000000';
   }
}

setInterval(function(){
    if(typeof RTEEditorDiv !== "undefined" && RTEEditorDiv && RTEEditorDiv.style.display === "block"){
        resizeFckeditor();
    }
}, 300);
var myBar = new WebFXMenuBar("${path}");
<c:if test="${param.bulId=='' || bean.state==0 || bean.state==40}">
myBar.add(new WebFXMenuButton("save", "<fmt:message key='common.toolbar.save.label' bundle='${v3xCommonI18N}' />", "saveForm('draft');", [1,5], "", null));
</c:if>
var supportPdfMenu=true;
//正文类型
var supportPdfMenu=true;
myBar.add(${v3x:bodyTypeSelector("v3x")});
if(bodyTypeSelector){
  bodyTypeSelector.disabled("menu_bodytype_${v3x:escapeJavascript(bean.dataFormat)}");
}
//预览
myBar.add(new WebFXMenuButton("preview", "<fmt:message key='common.toolbar.preview.label' bundle='${v3xCommonI18N}'/>", "viewPage()", [7,3],"<fmt:message key='common.toolbar.preview.label' bundle='${v3xCommonI18N}'/>", null));

      <%-- 集团版块选人组件 --%>
      var isNeedCheckLevelScope_spGroup = false;
      var hiddenPostOfDepartment_spGroup = true;
      <%-- 单位版块选人组件 --%>
      var isNeedCheckLevelScope_spAccount = false;
      var hiddenPostOfDepartment_spAccount = true;
      var hiddenOtherMemberOfTeam_spAccount = true;
      <%-- 部门版块选人组件 --%>
      var isNeedCheckLevelScope_spDept = false;
      var hiddenPostOfDepartment_spDept = true;
      var onlyLoginAccount_spDept = true;
      var includeElements_spDept = "${v3x:escapeJavascript(ChildDeptissueArea)}";
      function setPeopleFields(elements){
        if(!elements){
          return;
        }
        document.getElementById("issueArea").value=getIdsString(elements);
        document.getElementById("issueAreaName").value=getNamesString(elements);
		document.getElementById("issueAreaName").style.color = "black";
        document.getElementById("publishScopeId").value=getIdsString(elements);
        hasIssueArea = true;
      }

      function openAjaxNew(typeId){
        var requestCaller;
        requestCaller = new XMLHttpRequestCaller(this, "ajaxBulDataManager", "getBulData", false);
        requestCaller.addParameter(1, "String", typeId);
        var ds = requestCaller.serviceRequest();
        if(ds!=null){
          document.getElementById('typeId').value=ds.instance.typeId;
          document.getElementById('typeName').value=ds.instance.typeName;
          document.getElementById('publishDepartmentId').value=ds.instance.publishDepartmentId;
          document.getElementById('publishDepartmentName').value=ds.instance.publishDepartmentName;
          document.getElementById('issueArea').value=ds.instance.issueArea;
          document.getElementById('issueAreaName').value=ds.instance.issueAreaName;
          var area = ds.instance.issueArea.indexOf(ds.instance.bulTypeId)!=-1 ? ds.instance.issueArea : "nil";
          includeElements_spDept =ds.instance.issueArea.indexOf(ds.instance.bulTypeId)!=-1 ? ds.instance.issueArea : "nil" ;
            document.getElementById("issueArea").value=area;
            document.getElementById("publishScopeId").value=area;
            hasIssueArea = true;
        }
        return ;
      }

      var _isCustom = ${spaceType == '18'||spaceType == '17'||spaceType == '4'};
	  jQuery(function($) {
	    if(typeof(OfficeAPI)!="undefined"){
	    	var office = document.getElementById("dataFormat").value;
	    	// office 控制参数
	    	if(office == "OfficeWord" ||
   			   //office == "OfficeExcel" ||
   			   office == "WpsWord" ||
   			   office == "WpsExcel"){
	    		OfficeAPI.showOffice({
		            "officecanSaveLocal":true,
		            "officecanPrint":true
		        });
	    	}
	    	// pdf 控制参数
	    }
	});
</script>
<style type="text/css">
.hand{
cursor: hand;
}
</style>
</head>
<body scroll='no' style="overflow:hidden;height:100%" class="padding5" onload="" onunload="unlock('${bean.id }');">
<form style="height:100%;" action="${bulDataURL}" name="dataForm" target="_blank" id="dataForm" method="post" onsubmit="return true">
<input type="hidden" name="method" id="method" value="bulEdit" />
<input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}" />
<input type="hidden" name="id" id="bul_Id" value="${bean.id}" />
<input type="hidden" name="custom" id="custom" value="${custom}" />
<input type="hidden" name="spaceId" id="spaceId" value="${v3x:toHTML(param.spaceId)}" />
<input type="hidden" name="form_oper" id="form_oper" value="draft" />
<input type="hidden" name="dataFormat" id="dataFormat" value="${v3x:toHTML(bean.dataFormat)}" />
<input type="hidden" name="spaceType" id="spaceType" value="${spaceType}" />
<input type="hidden" name="ext5" id="ext5" value="${bean.ext5}" />
<input type="hidden" name="bulTypeId" id="bulTypeId" value="${v3x:toHTML(bulTypeId)}" />
<input type="hidden" name="templateId" id="templateId" value="${templateId}" />
<input type="hidden" name="isAuditEdit" id="isAuditEdit" value="${v3x:toHTML(param.isAuditEdit)}" />
<input type="hidden" name="showPublish" id="showPublish" value="${bean.showPublishUserFlag}"/>
<input type="hidden" id="showPublishExi" value="${bean.showPublishUserFlag}"/>
<input type="hidden" id="publishChooseHidden" value="${bean.publishChoose}"/>
<input type="hidden" name="noteCallInfo" id="readMes" value="${bean.ext1}"/>
<input type="hidden" name="openRecordsReadName" id="openRecordsReadId" value="${bean.openRecordRead }"/>
<input type="hidden" id="readMes0" value="${bean.ext1}"/>
<input type="hidden" name="printAllow" id="printFla" value="${bean.ext2}"/>
<input type="hidden" name="printAllow0" id="printFla0" value="${bean.ext2}"/>
<!-- 预览 -->
<input type="hidden" name="createUser" value="${bean.createUser}"/>
<input type="hidden" name="previewTitle" id="previewTitle" value="${v3x:toHTML(bean.title)}"/>
<!-- 审核时修改 -->
<input type="hidden" id="writePermit" name="writePermit" value="${writePermit}"/>
<input type="hidden" id="finalPublish" name="finalPublish" value="${finalPublish}"/>
<input type="hidden" id="bulAuditPublishChoose" name="bulAuditPublishChoose" value="${bulAuditPublishChoose}"/>
<input type="hidden" id="showPublisherName" name="showPublisherName" value="${showPublisherName}"/>
<input type="hidden" name="recordReadName" id="recordReadId" value="1"/>
<input type="hidden" name="openRecordsRead" id="openRecordsRead" value="1"/>
<input type="hidden" name="showRecordReadName" id="showRecordReadId" value="${bean.ext1}"/>
<input type="hidden" name="showOpenRecordsReadName" id="showOpenRecordsReadId" value="${bean.openRecordRead}"/>
<!-- 切换格式为了方便取得附件的ID所以传递AttID -->
<input type="hidden" name="attRefId" id="attRefId" value="${requestScope.attRefId}" />
<input type="hidden" name="openFlag" id="openFlag" value="${requestScope.openFlag}" />
<input type="hidden" name="pdf" id="pdf" value="${empty bean.ext5? 'false':'true'}"/>
<input type="hidden" name="bul_Type" id = "bul_Type_id" value="${v3x:toHTML(boardListMap)}">
<input type="hidden" name="bul_TypeInfo" id = "bul_TypeInfo_id" value="${v3x:toHTML(boardListMapJson)} ">
<input type="hidden" id = "isAuditEditBul" value="${v3x:toHTML(option)} ">
<input type="hidden" id="issueArea" value="${spaceType == '18'||spaceType == '17'||spaceType == '4' ? DEPARTMENTissueArea : ''}"  name="issueArea"><%-- 选人信息 --%>
<input type="hidden" id="publish_scop" value="${v3x:toHTML(bean.publishScope)}">
<input type="hidden" id="spaceType_change" name="spaceType_change" value="">
<input type="hidden" id = "isBulEdit" name="isBulEdit" value="">
    <%--选人 --%>
    <c:set value="${v3x:parseElementsOfTypeAndId(DEPARTMENTissueArea)}" var="org"/>
    <c:set var="issueAreaName" value="${v3x:showOrgEntitiesOfTypeAndId(DEPARTMENTissueArea, pageContext)}" />
    <v3x:selectPeople id="spGroup"  originalElements="${v3x:escapeJavascript(org)}"
      panels="Account,Department,Team,Post,Level,JoinOrganization,JoinAccountTag,JoinPost,Guest,BusinessDepartment"
      selectType="Member,Department,Account,Post,Level,Team,JoinAccountTag,Guest,BusinessAccount,BusinessDepartment"
      departmentId="" jsFunction="setPeopleFields(elements)" />
    <v3x:selectPeople id="spAccount" originalElements="${v3x:escapeJavascript(org)}"
        panels="Department,Team,Post,Level,Outworker,JoinOrganization,JoinAccountTag,JoinPost,Guest,BusinessDepartment"
        selectType="Member,Department,Account,Post,Level,Team,JoinAccountTag,Guest,BusinessAccount,BusinessDepartment"
        departmentId="${v3x:currentUser().departmentId}"
        jsFunction="setPeopleFields(elements)" />
    <v3x:selectPeople id="spDept" originalElements="${v3x:escapeJavascript(org)}"
        panels="Department,Team,Post,Level,Outworker"
        selectType="Member,Department,Account,Post,Level,Team"
        departmentId="${v3x:currentUser().departmentId}"
        jsFunction="setPeopleFields(elements)" />
    <script type="text/javascript">
      <!--
       includeElements_spCustomSpace = "${v3x:parseElementsOfTypeAndId(entity)}";
      //-->
    </script>
    <v3x:selectPeople id="spCustomSpace"
                      originalElements="${v3x:escapeJavascript(org)}"
                      panels="Account,Department,Team,Post,Level,Outworker"
                      selectType="Member,Department,Account,Post,Level,Team"
                      departmentId="${v3x:currentUser().departmentId}"
                      jsFunction="setPeopleFields(elements)" />
    <script type="text/javascript">
    if("18" == "${v3x:escapeJavascript(param.spaceType)}" || "17" == "${v3x:escapeJavascript(param.spaceType)}" || "4" == "${v3x:escapeJavascript(param.spaceType)}") {
        showAllOuterDepartment_spCustomSpace = false;
    } else {
        showAllOuterDepartment_spCustomSpace = true;
    }
    </script>
<style>
#officeFrameDiv{height:100%;}
.input-100per{width: 100%;}
.input-99per{width: 99%;}
.input-98per{width: 98%;}
.sponsor_table tr td input:hover,.sponsor_table tr td select:hover{
  border: 1px solid #42b3e5;
}
</style>
<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center" class="CollTable sponsor_table" id="details_table_height">
    <tr>
        <td height="10">
            <script type="text/javascript">document.write(myBar);</script>
        </td>
    </tr>
    <tr id="bulEdit_head">
        <td id="bulEditTD" height="10" class="bg-summary padding_t_10 padding_r_10">
        <table border="0" cellpadding="2" cellspacing="0" width="100%" align="center">
            <tr>
            <fmt:message key="oper.send" var="oper_send"/>
            <fmt:message key="bulletin.oper.publish" var="oper_publish"/>
            <fmt:message key="bul.save" var="bul_save"/>
            <td rowspan="3" valign="top"><a onclick="javaScript:saveForm('submit');" id='sendId'  class="margin_lr_10 inline-block align_center new_btn">${param.isAuditEdit?bul_save:(isAduit?oper_send:oper_publish)}</a></td>
            <td width="36%" align="left">
                <fmt:message key="common.subject.label" var="_title"/>
                <fmt:message key="bul.data.title" var="_myLabel"/>
                <fmt:message key="label.please.input" var="_myLabelDefault">
                    <fmt:param value="${_myLabel}" />
                </fmt:message>
                <c:set var="_value" value="${v3x:toHTML(bean.title)}" />
                <input class="input-60per" type="text"
                                name="title" id="title"
                                value="<c:out value="${bean.title}" default="${_myLabelDefault}" escapeXml="true" />"
                                title="${_value}"
                                defaultValue="${_myLabelDefault}"
                                onfocus="checkDefSubject(this, true);onfocusColor(this);"
                                onblur="checkDefSubject(this, false);initColor(this);"
                                inputName="${_myLabel}"
                                validate="notNull,isDefaultValue"
                                maxSize="200"
                                ${v3x:outConditionExpression(readOnly, 'disabled', '')}/>
                <input type="hidden" id="isAuditEditSpace" value="${v3x:toHTML(optionSpace)}"/>
                <select class="corSelect choice right input-30per" onchange="changeSelect();" id = "orgType" name="orgType" ${param.isAuditEdit eq 'true'?'disabled':''}><!-- groupAccExch() -->
                    <c:choose>
                        <c:when test="${spaceType==18}"><option value="18" selected><fmt:message key="bulletin.spaceType.18"/></option></c:when>
                        <c:when test="${spaceType==17}"><option value="17" selected><fmt:message key="bulletin.spaceType.17"/></option></c:when>
                        <c:when test="${spaceType==4}"><option value="4" selected><fmt:message key="bulletin.spaceType.4"/></option></c:when>
                        <c:otherwise>
                          <c:if test="${bean.type.spaceType != 1}">
                            <c:if test="${corporationSize eq 'true'}">
                              <option value="2" <c:if test="${spaceType==2}">selected</c:if>><fmt:message key="UnitAnnouncement.label"/></option>
                            </c:if>
                            <c:if test="${groupSize eq 'true'}">
                              <option value="3" <c:if test="${spaceType==3}">selected</c:if>><fmt:message key="GroupAnnouncement.label"/></option>
                            </c:if>
                          </c:if>
                          <c:if test="${bean.type.spaceType == 1}">
                            <option value="1" <c:if test="${spaceType==1}">selected</c:if>><fmt:message key="bbs.departmentBulletin.label"/></option>
                          </c:if>
                        </c:otherwise>
                    </c:choose>
                </select>
            </td>
            <td width="30%" style="padding-left:10px;">
                <input type="hidden" name="typeName" id="typeName" value="${v3x:toHTML(bean.type.typeName)}" />
                <input type="hidden" name="typeId" id="typeId" value="${bean.type.id}" />
                <select id ="typeList_id" class="titleInput input-99per"  onchange="changeCheckBox();" ${param.isAuditEdit eq 'true'?'disabled':''}>
                  <c:if test="${bean.type.spaceType == 1}">
                    <c:forEach items="${deptSpaceModels}" var="deptSpace">
                      <option id="boardOpt_${deptSpace.getEntityId()}"  class="titleInput choice right" value="${deptSpace.getEntityId()}">${v3x:toHTML(deptSpace.getSpacename())}</option>
                    </c:forEach>
                  </c:if>
                </select>
            </td>
            <td width="30%" style="padding-left:6px;">
                <!-- 发布部门 -->
                <c:set value="${v3x:getOrgEntity('Department', bean.publishDepartmentId).name}" var="publishScopeValue"/>
                <c:set value="${v3x:parseElementsOfIds(bean.publishDepartmentId, 'Department')}" var="defauDepar"/>
                <v3x:selectPeople id="dept" originalElements="${defauDepar}" panels="Department" selectType="Department"
                jsFunction="setDeptPeopleFields(elements)" departmentId="${v3x:currentUser().departmentId}" maxSize="1" minSize="1" />
                <fmt:message key="bul.data.publishDepartmentId" var="_myLabel"/>
                <fmt:message key="label.please.select" var="_myLabelDefault">
                  <fmt:param value="${_myLabel}" />
                </fmt:message>
                <input type="hidden" id="publishDepartmentId" name="publishDepartmentId" value="${bean.publishDepartmentId}"/>

                <input class="titleInput input-99per" type="text" placeholder="<fmt:message key='bbs.publish.department.label'/>" id="publishDepartmentName" name="publishDepartmentName" readonly="true"
                    value="<c:out value="${publishScopeValue}" default="${_myLabelDefault}" escapeXml="true" />"
                    defaultValue="${_myLabelDefault}"
                    onfocus="checkDefSubject(this, true)"
                    onblur="checkDefSubject(this, false)"
                    inputName="${_myLabel}"
                    validate="notNull,isDefaultValue"
                    ${v3x:outConditionExpression(readOnly, 'disabled', '')}
                    onclick="selectPeople('dept','publishDepartmentId','publishDepartmentName');"
                    <c:if test="${bean.type.spaceType==1}"> disabled </c:if>
                    />
            </td>
        </tr>

        <tr>

            <td width="32%">
              <fmt:message key='common.issueScope.label' bundle="${v3xCommonI18N}" var="_myLabelScope"/>
              <fmt:message key="label.please.select" var="_myLabelDefaultScope">
              <fmt:param value="${_myLabelScope}" />
              </fmt:message>
                <input type="hidden" id="publishScopeId" name="publishScope" value="${spaceType == '18'||spaceType == '17'||spaceType == '4' ? DEPARTMENTissueArea : bean.publishScope}"/>
                <input type="hidden" id="publishInput" value="${v3x:showOrgEntitiesOfTypeAndId(bean.publishScope, pageContext)}">
<%--                zhou--%>
                <c:choose>
                    <c:when test="${spaceType == '18'||spaceType == '17'||spaceType == '4'}">
                      <input id="issueAreaName" type="text" value="${issueAreaName}"
                        onclick="javascript:selectPeopleFun_spCustomSpace();" readonly="readonly"
                        class="titleInput sendArea"
                        value="<c:out value="${_myLabelDefaultScope}" default="${_myLabelDefaultScope}" escapeXml="true" />"
                        defaultValue="${_myLabelDefaultScope}"
                        onfocus="checkDefSubject(this, true);"
                        onblur="checkDefSubject(this, false);"
                        inputName="${_myLabelScope}"
                        validate="notNull,isDefaultValue"
                        ${v3x:outConditionExpression(readOnly, 'disabled', '')}>
                    </c:when>
                    <c:otherwise>
                        <input id="issueAreaName" type="text" readonly="readonly"
                          class="titleInput sendArea"
                          value="<c:out value="${_myLabelDefaultScope}" default="${_myLabelDefaultScope}" escapeXml="true" />"
                          defaultValue="${_myLabelDefaultScope}"
                          onfocus="checkDefSubject(this, true)"
                          onblur="checkDefSubject(this, false)"
                          inputName="${_myLabelScope}"
                          validate="notNull,isDefaultValue"
                          ${v3x:outConditionExpression(readOnly, 'disabled', '')}>
                    </c:otherwise>
                </c:choose>
            </td>
            <td width="32%" style="padding-left:10px;">
                <select class="corSelect input-99per" id="bulTempl" name="bulTempl" onchange="loadBulTemplate();">
                  <option value=""><fmt:message key="bul.template" /></option>
                </select>
            </td>
        </tr>
        <tr class="row3">
            <td class="col0_2" colspan="4" style="position: relative;">
               <ul class="overflow" id="checkbox">
                  <li class="left" id="isShowPublish">
                    <em id="showPublishUser" class="checkBox ${v3x:outConditionExpression(bean.showPublishUserFlag, 'checked', '')}"></em>
                    <span class="hand"><fmt:message key="bul.dataEdit.showPublishUser"/></span>
                  </li>
                  <li class="left" id="publishChoose" style="display: none">
                    <select  name="publishChoose" onchange="changePublishChoose();" id="publishChooseSelect">
                      <option  value="0" id="publishChooseSelect_0"
                          <c:if test="${bean.publishChoose==0}">selected</c:if>><fmt:message key="bulletin.typeSet.realPublish" /></option>
                      <option  value="1"
                          <c:if test="${bean.publishChoose==1}">selected</c:if>><fmt:message key="bulletin.edit.choosePublish" /></option>
                      <option  value="2"
                          <c:if test="${bean.publishChoose==2}">selected</c:if>><fmt:message key="bulletin.edit.writePublish" /></option>
                    </select>
                  </li>
                  <li class="left em_left" id="writePublishSelLi" style="display: none">
                    <em id="write_help" class="ico16 help_16 write_em"></em>
                    <!-- 手动输入发布人 -->
                    <c:set value="${v3x:parseElementsOfIds(bean.choosePublshId, 'Member')}" var="defauDepar"/>
                    <v3x:selectPeople id="writePub" originalElements="${defauDepar}" panels="Department,Team,Post,Level,Outworker" selectType="Member"
                    jsFunction="writePublishSelect(elements)" maxSize="1" minSize="1" />
                    <fmt:message key="bulletin.publishMember" var="_clickPub"/>
                    <fmt:message key="bulletin.select.people.click" var="_clickPubDefault">
                      <fmt:param value="${_clickPub}" />
                    </fmt:message>
                    <input type="hidden" id="choosePublshId" name="choosePublshId" value="${v3x:toHTML(bean.choosePublshId)}"/>

                    <input class="titleInput input-99per" type="text" placeholder="${_clickPubDefault}" id="writePublishSel" name="writePublishSel" readonly="true"
                        value="<c:out value='${v3x:showMemberName(bean.choosePublshId)}' default="${_clickPubDefault}" escapeXml="true" />"
                        defaultValue="${_clickPubDefault}"
                        onfocus="checkDefSubject(this, true)"
                        onblur="checkDefSubject(this, false)"
                        inputName="${_clickPub}"
                        validate="notNull"
                        ${v3x:outConditionExpression(readOnly, 'disabled', '')}
                        onclick="selectPeople('writePub','choosePublshId','writePublishSel');"
                        />
                  </li>
                  <li class="left em_left" id="writePublishWriLi" style="display: none">
                    <em id="write_help" class="ico16 help_16 write_em"></em>
                    <fmt:message key="bulletin.edit.pleWrite.click" var="pleWritePublish"/>
                    <input class="input-99per" type="text" id="writePublish" name="writePublish" placeholder="${pleWritePublish}" value="${v3x:toHTML(bean.writePublish)}" validate="isWord" inputName="${_clickPub}"/>
                  </li>
                  <li class="left em_write_text" style="display: none;width: 360px;" id="write_help_li">
                    <em class="em_title em_title_content" style="width: 360px;">
                      <fmt:message key="bulletin.publishChoose.tips" />
                    </em>
                  </li>
                  <li class="left" id="recordRead">
                    <em class="checkBox checked" id="recordRead_em"></em>
                    <span class="hand"><fmt:message key="bul.dataEdit.noteCallInfo"/></span>
                  </li>
                  <li class="left" id="openRecordsReadLi" style="display: block;">
			          <em class="checkBox checked" id="openRecordsRead_em"></em>
			          <span class="hand"><fmt:message key="bul.dataOpen.openReadInfo"/></span>
			      </li>
                  <li class="left" id="printPermit">
                    <em class="checkBox" id="printPermit_em"></em>
                    <span class="hand"><fmt:message key="bul.dataEdit.printAllow"/></span>
                  </li>
                  <li id="changePdf" class="left" style="${bean.dataFormat == 'OfficeWord' ? '' : 'display: none;'}">
                    <em id="toPdf" class="checkBox ${empty bean.ext5 ? '' : 'checked'}"></em>
                    <span class="hand"><fmt:message key="common.transmit.pdf" bundle='${v3xCommonI18N}' /></span>
                  </li>
                </ul>
            </td>
        </tr>
        <tr class="row4">
          <td width="1%"></td>
          <td class="col0_2" colspan="3" width="99">
            <ul class="overflow" id="set_mb">
              <li class="left">
                <em class="icon16 file_attachment_16"></em>
                <span class="hand"  onclick="insertAttachment();showEditer();"><fmt:message key='bul.add.attachment' /></span>
              </li>
              <li class="left">
                <em class="icon16 addRelatDoc_16"></em>
                <span class="hand" onclick="insertCorrelationFile('resizeFckeditor');"><fmt:message key='bul.add.correlationfile' /></span>
              </li>
            </ul>
          </td>
        </tr>
        <tr>
          <td width="1%"></td>
          <td width="1%"></td>
          <td colspan="3" width="99%">
          <div id="attachment5Area"></div>
          </td>
        </tr>
        <tr>
          <td width="1%"></td>
          <!-- <td width="1%"></td> -->
          <td colspan="3">
            <div id="attachment2TR" style="display:none;" class="reply_relation margin_b_5">
              <span class="attachment_count left margin_r_10">
                <em class="icon16 relation_file_16"></em>
                <span class="attachment_num">(<span id="attachment2NumberDiv"></span>)</span>
              </span>
              <div id="attachment2Area" style="overflow: auto;"></div>
            </div>
          </td>
        </tr>
        <tr>
          <td width="1%"></td>

          <td colspan="3">
              <div id="attachmentTR" style="display:none;" class="my_reply_attchment margin_b_5">
                  <span class="attachment_count left margin_r_10">
                    <em class="icon16 file_attachment_16"></em>
                    <span class="attachment_num">(<span id="attachmentNumberDiv"></span>)</span>
                  </span>
                  <v3x:fileUpload attachments="${attachments}" canDeleteOriginalAtts="true" />
              </div>
          </td>
        </tr>
        </table>
     </td>
    </tr>
    <tr id="bodyTr">
        <td id="editerDiv_td" valign="top" height="100%">
        <div id="editerDiv" style="display: block;height:100%;">
            <c:if test="${originalNeedClone==null}">
                <c:set var="originalNeedClone" value="false" scope="request" />
            </c:if>
            <v3x:editor htmlId="content" content="${bean.content}" type="${v3x:escapeJavascript(bean.dataFormat)}" createDate="${bean.createDate}" category="7" originalNeedClone="${v3x:escapeJavascript(originalNeedClone)}" contentName="${v3x:escapeJavascript(bean.contentName)}" />
        </div>
        </td>
    </tr>
</table>

</form>
<div id="hideIframe" style="display:none">
<iframe id="templateIframe"></iframe>
</body>
</html>
<script type="text/javascript">
  if(v3x.isFirefox||v3x.isChrome){
    window.onbeforeunload = function(){
        return "";
    }
  }else{
    window.onbeforeunload = function(){
        try {
            window.event.returnValue="";
        } catch (e) {
        }
    }
  }
  OfficeObjExt.setIframeId("officeFrameDiv");
  function selfSet(){
    var div = document.getElementById("webfx-menu-object-1");
    div.style.paddingLeft = "10px";
    div.style.border = "1px solid transparent";
    var a = document.getElementById("sendId");
    a.style.marginLeft = "20px";
    var mt = document.getElementById("checkbox");
    mt.style.marginTop = "3px";
    var mb = document.getElementById("set_mb");
    mb.style.marginBottom = "6px";
  }
  selfSet();

  var space_Type_Account = document.getElementById("orgType").value;
  if(space_Type_Account != '3') onlyLoginAccount_dept = true;
</script>
