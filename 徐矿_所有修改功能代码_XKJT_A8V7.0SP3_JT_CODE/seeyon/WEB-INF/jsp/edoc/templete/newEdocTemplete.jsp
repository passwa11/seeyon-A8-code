<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@page import="com.seeyon.ctp.common.constants.ApplicationCategoryEnum" %>
<%@page import="com.seeyon.ctp.common.constants.Constants" %>
<%@ taglib uri="http://v3x.seeyon.com/bridges/spring-portlet-html" prefix="html"%>
<%@ taglib prefix="ctp" uri="http://www.seeyon.com/ctp"%>
<html:link renderURL="/doc.do" var="pigeonholeDetailURL" />
<html xmlns="http://www.w3.org/1999/xhtml" class="h100b w100b over_hidden">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insert title here</title>
<%@ include file="../edocHeader.jsp" %>

<script type="text/javascript">

<%-- 请注意，这个js代码必须在 unitId.jsp 前面 --%>

var pigeonholeURL = "${pigeonholeDetailURL}";
var isTempleteEditor = true;
var checkPDFIsNull=true;
var selfCreateFlow=true;
var logoURL = "${logoURL}";
var edocTemplateSaveUrl="${edocTempleteURL}?method=systemSaveTemplete";
var actorId=-1;//建立模版的权限为全部可以操作
var processing=false;
var hasDiagram = <c:out value="${hasWorkflow}" default="false" />;        
var caseProcessXML = '${process_xml}';
var caseLogXML = "";
var caseWorkItemLogXML = "";
var showMode = 1;
var currentNodeId = null;
var showHastenButton = "";
var appName="${appName}";
var isTemplete = true;
var defaultNodeLable="${defaultNodeLable}";
var defaultNodeName = "${defaultNodeName}";
var hiddenColAssignRadio_wf = true;
var editWorkFlowFlag = "true"
var _roleSender="${ctp:i18n('sys.role.rolename.Sender')}";
var _roleSenderDM = "${ctp:i18n('sys.role.rolename.SenderDepManager')}";
</script>

<c:choose>
	<c:when test="${templete.id != null}">
		<c:set value="${templete.categoryId}" var="categoryId" />
	</c:when>
	<c:otherwise>
		<c:set value="${param.categoryId}" var="categoryId" />
	</c:otherwise>
</c:choose>
<%-- 
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery.js${v3x:resSuffix()}" />"></script>
--%>
<!-- 覆盖SeeyonForm.css内的相关样式 -->
<link rel="stylesheet" type="text/css" href="<c:url value="/apps_res/edoc/css/edocDisplay.css${v3x:resSuffix()}" />"/>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery-ui.custom.min.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.plugin.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery-ui.custom.js${v3x:resSuffix()}" />"></script>


<script type="text/javascript">

// 快速需求(FastDemand):发文、签报支持PDF正文
var supportPdfMenu=true;
// 设置收文登记页面支持PDF正文。
// var supportPdfMenu=('${appType}'=='20'?true:false);

hasWorkflow = <c:out value='${hasWorkflow}' default='false' />;
var selectedElements = null;
//设置office正文能进行本地保存。主要是为了后续进行复制粘贴控制的时候不将正文设置成只读了。
var officecanSaveLocal="true";
formOperation = "aa";
var hiddenPostOfDepartment_auth = true;
//分支  开始
var branchs = new Array();
var keys = new Array();
var hasKeys = true;
var policys = null;
var nodes = null;
<c:if test="${branchs != null}">
	<c:forEach items="${branchs}" var="branch" varStatus="status">
		var branch = new ColBranch();
		branch.id = ${branch.id};
		branch.conditionType = "${branch.conditionType}";
		branch.formCondition = "${v3x:escapeJavascript(branch.formCondition)}";
		branch.conditionTitle = "${v3x:escapeJavascript(branch.conditionTitle)}";
		branch.conditionDesc = "${v3x:escapeJavascript(branch.conditionDesc)}";
		branch.isForce = "${branch.isForce}";
		branch.conditionBase = "${branch.conditionBase}";
		eval("branchs["+${branch.linkId}+"]=branch");
		keys[${status.index}] = ${branch.linkId};
	</c:forEach>
</c:if>
//分支  结束
//OA-37938应用检查：模板中绑定的文单被停用之后，在模板的修改页面查看调用的公文单与实际显示的不一致 名字还是原来的名字，单其实文单不一样。
if('${setDefaultForm}'=='true'){
  alert(_("edocLang.edoc_template_form_Disable",'${formName}'));
}
var clickFlag = true;
//OA-39867 单位管理员liud02编辑公文模板时，鼠标点击节点权限等，没有提示直接切换
var _t;
var t_num = 0;//标记是否是提交
var categoryType = "${categoryType}";
var processTemplateId = "${processTemplateId}";
var currentUserId = "${currentUserId}";
var currentUserName = "${ctp:escapeJavascript(currentUserName)}";
var currentUserAccountName = "${currentUserAccountName}";
var currentUserAccountId = "${currentUserAccountId}";
var paramTempleteId = "${param.templeteId}";
var paramCategoryId = "${v3x:escapeJavascript(param.categoryId)}";
</script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/edoc/js/newEdocTemplete.js${v3x:resSuffix()}" />"></script>
</head>
<body class="w100b h100b" scroll="no" onLoad="clearWorkflowResource(window.parent.parent, '${currentUserId}');edocFormDisplay();onload();" onbeforeunload="return onbefore()">
<input id="edocType" value="${edocType}" type="hidden"/>
<fmt:message key='common.default.selectPeople.value' bundle="${v3xCommonI18N}" var="superviseDefaultValue" />
<input id="taohongriqiSwitch" value="${taohongriqiSwitch}" type="hidden"/>
<iframe name="toXmlFrame" scrolling="no" frameborder="0" marginheight="0" marginwidth="0" height="0" width="0"></iframe>
<form style="width: 100%;height: 100%" name="sendForm" id="sendForm" method="post" action="${edocTempleteURL}?method=systemSaveTemplete">

<%@ include file="../unitId.jsp" %>
<%--5.0工作流 --%>
<input id="process_desc_by" name="process_desc_by" type="hidden"/>
<input id="process_xml" name="process_xml" type="hidden"/>
<input id="process_event" name="process_event" type="hidden">
<input id="process_rulecontent" name="process_rulecontent" type="hidden"/>
<input id="processId" name="processTemplateId" type="hidden" value="${processTemplateId }"/>

<input id="readyObjectJSON" name="readyObjectJSON" type="hidden"/>
<input id="process_subsetting" name="process_subsetting" type="hidden"/>

<input id="moduleType" name="moduleType" type="hidden"/>
<input id="workflow_newflow_input" name="workflow_newflow_input" type="hidden"/>
<input id="workflow_node_peoples_input" name="workflow_node_peoples_input" type="hidden"/>
<input id="workflow_node_condition_input" name="workflow_node_condition_input" type="hidden"/>
<input id="caseId" name="caseId" type="hidden"/>
<input id="subObjectId" name="subObjectId" type="hidden"/>
<input id="currentNodeId" name="currentNodeId" type="hidden"/>

<input type="hidden" name="templeteId" id="templeteId" value="${param.templeteId}" />
<input type="hidden" id="process_info_bak" name="process_info_bak" value="" />
<input type="hidden" id="process_xml_bak" name="process_xml_bak" value="" />

<!-- 分枝 开始 -->
<div id="branchDiv"></div>
<!-- 分枝 开始 -->
<input type="hidden" name="appName" value="<%=ApplicationCategoryEnum.edoc.getKey()%>"/>
<input type="hidden" name="id" value="${templete.id}"/>
<input type="hidden" name="openWinId" id="openWinId" value=""/>
<input type="hidden" name="process_desc_by" value="${process_desc_by}" />
<input type="hidden" name="process_xml" value="" />
<input type="hidden" name="edocType" value="${summary.edocType}"/>
<input type="hidden" name="categoryType" value="${categoryType}" />
<input type="hidden" name="actorId" value="-1"/>
<input type="hidden" name="from" value="${v3x:toHTML(param.from)}"/>
<input type="hidden" id="archiveId" name="archiveId" value="${summary.archiveId}" />
<input type="hidden" id="prevArchiveId" name="prevArchiveId" value="${summary.archiveId}" />
<input type="hidden" name="supervisorId" id="supervisorId" value="${colSupervisors }"/>
<input type="hidden" name="supervisors" id="supervisors" value="${colSupervise.supervisors }"/>
<input type="hidden" name="awakeDate" id="awakeDate" value="${colSupervise.templateDateTerminal}"/>
<input type="hidden" name="superviseDefaultValue" id="superviseDefaultValue" value="${superviseDefaultValue }" />
<input type="hidden" name="superviseTitle" id="superviseTitle" value="${colSupervise.title }"/>
<input type="hidden" name="superviseRole" id="superviseRole" value="${colSuperviseRole }"/>
<input type="hidden" name="loginAccountId" id="loginAccountId" value="${v3x:currentUser().loginAccount}" />
<input type="hidden" name="orgAccountId" id="orgAccountId" value="${v3x:currentUser().loginAccount}" />
<input type="hidden" name="docMarkValue" id="docMarkValue" value ="${formModel.edocSummary.docMark}"/>
<input type="hidden" name="docMarkValue2" id="docMarkValue2" value ="${formModel.edocSummary.docMark2}"/>
<input type="hidden" name="docInmarkValue" id="docInmarkValue" value ="${formModel.edocSummary.serialNo}"/>

<%--是否是公文模板制作或者修改页面，主要用来传递传参,切换公文单的时候区别是前台调用还是后台管理员调用---%>
<input type="hidden" name="isEdocTempletePage" value ="true"/>

<!-- 所属机构 核心使用机构 流程界别  RACI 发布时间 -->
        <input type="hidden" id="belongOrg" name="belongOrg" value="${templete.belongOrg }" />
        <input type="hidden" id="coreUseOrg" name="coreUseOrg" value="${templete.coreUseOrg }" />
        <input type="hidden" id="processLevel" name="processLevel" value="${templete.processLevel }" />
        <input type="hidden" id="responsible" name="responsible" value="${templete.responsible }" />
        <input type="hidden" id="auditor" name="auditor" value="${templete.auditor }" />
        <input type="hidden" id="consultant" name="consultant" value="${templete.consultant }" />
        <input type="hidden" id="inform" name="inform" value="${templete.inform }" />
        <input type="hidden" id="publishTime" name="publishTime" value="${templete.publishTime }" />

<c:set value="${v3x:parseElements(templeteAuths, 'authId', 'authType')}" var="authInfo" />
<input type="hidden" name="authInfo" id="auth" value="${authInfo}" />
<c:set value="${v3x:showOrgEntities(templeteAuths, 'authId','authType',pageContext)}" var="authInfoShow" />
<input type="hidden" name="authInfoShow" id="authInfoShow" value="${authInfoShow}" />

<v3x:selectPeople id="auth" panels="Account,Department,Team,Post,Level" selectType="Department,Team,Post,Level,Outworker,Account,Member" jsFunction="doAuth(elements)"
 originalElements="${authInfo}"  minSize="0"/>
 
 <v3x:selectPeople id="authNotAccount" panels="Account,Department,Team,Post,Level" selectType="Department,Team,Post,Level,Outworker,Account,Member" jsFunction="doAuth(elements)"
 originalElements="${authInfo}"  minSize="0"  />
 <%--OA-39007 列表授权是放开了的 ，那么新建也要放开授权  --%>
<script>onlyLoginAccount_wf=true;onlyLoginAccount_auth=false;onlyLoginAccount_authNotAccount=false;</script>
<script>isNewOfficeFilePage=true;</script>
<script>isNeedCheckLevelScope_auth=false;</script>
<script>showOriginalElement_wf=false;</script>
<script>showAccountShortname_wf="yes";</script>
<div name="edocContentDiv" id="edocContentDiv" style="display:block;width:2px;height:2px;position: absolute;overflow: hidden;">
<v3x:editor htmlId="content" content="${body.content=='null'?'':body.content}" type="${body.contentType}" createDate="${body.createTime}" originalNeedClone="${cloneOriginalAtts}" category="<%=ApplicationCategoryEnum.edoc.getKey()%>" loadOfficeImmediate="false"/>
</div>
<span id="people" style="display:none;">
<c:out value="${peopleFields}" escapeXml="false" />
</span>

<table border="0" cellpadding="0" cellspacing="0" style="background-color:#ededed;width: 100%;height: 100%">
  <tr class="config_tr">
    <td colspan="7" height="22" valign="top">
		<script type="text/javascript">
		var myBar = new WebFXMenuBar("${pageContext.request.contextPath}",'gray');
		
		var save = new WebFXMenu;
		save.add(new WebFXMenuItem("saveAsText", "<fmt:message key='templete.text.label' />", "saveTemplete('text')", "<c:url value='/apps_res/collaboration/images/text.gif'/>"));
		save.add(new WebFXMenuItem("saveAsWorkflow", "<fmt:message key='templete.workflow.label' />", "saveTemplete('workflow')", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>"));
		save.add(new WebFXMenuItem("saveAsTemplete", "<fmt:message key='templete.category.type.${param.categoryType}' bundle='${colI18N}'/>", "saveTemplete('templete')", "<c:url value='/apps_res/collaboration/images/text_wf.gif'/>"));
		
		var saveAs = new WebFXMenu;
		saveAs.add(new WebFXMenuItem("saveAsText", "<fmt:message key='templete.text.label' />", "saveAsTemplete('text')", "<c:url value='/apps_res/collaboration/images/text.gif'/>"));
		saveAs.add(new WebFXMenuItem("saveAsWorkflow", "<fmt:message key='templete.workflow.label' />", "saveAsTemplete('workflow')", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>"));
		saveAs.add(new WebFXMenuItem("saveAsTemplete", "<fmt:message key='templete.category.type.0' />", "saveAsTemplete('templete')", "<c:url value='/apps_res/collaboration/images/text_wf.gif'/>"));
		
		var insert = new WebFXMenu;
		insert.add(new WebFXMenuItem("", "<fmt:message key='common.toolbar.insert.localfile.label' bundle='${v3xCommonI18N}' />", "insertAttachment(null, null, '_insertTempAttCallback', 'false')"));
		<%--
		//insert.add(new WebFXMenuItem("", "<fmt:message key='common.toolbar.insert.mydocument.label' bundle='${v3xCommonI18N}' />", "quoteDocument()"));
		
		/*openSupervise
		var workflow = new WebFXMenu;
		//workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.no.label' />", "doWorkFlow('no')"));
		workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.new.label' />", "doWorkFlow('new')"));
		workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.edit.label' />", "designWorkFlow('detailIframe')"));
		*/		
		//myBar.add(new WebFXMenuButton("save", "<fmt:message key='common.toolbar.save.label' bundle='${v3xCommonI18N}' />", "", "<c:url value='/common/images/toolbar/save.gif'/>", "", save));		
		--%>
		myBar.add(new WebFXMenuButton("save", "<fmt:message key='common.toolbar.save.label' bundle='${v3xCommonI18N}' />","saveTemplete();", [1,5], "", null));
		myBar.add(new WebFXMenuButton("auth", "<fmt:message key='common.toolbar.auth.label' bundle='${v3xCommonI18N}' />", "openAuth();", [2,2]));
		//myBar.add(new WebFXMenuButton("workflow", "<fmt:message key='workflow.label' />", "", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>", "", workflow));
		myBar.add(new WebFXMenuButton("workflow", "<fmt:message key='common.design.workflow.label' bundle='${v3xCommonI18N}'/>", "createEdocWFPersonal()",[3,6], "", null));
		myBar.add(new WebFXMenuButton("insert", "<fmt:message key='common.toolbar.insert.label' bundle='${v3xCommonI18N}' />", null, [1,6], "", insert));
		//myBar.add(new WebFXMenuButton("bodyTypeSelector", "<fmt:message key='common.body.type.label' bundle='${v3xCommonI18N}' />", null, "<c:url value='/common/images/toolbar/bodyTypeSelector.gif'/>", "", bodyTypeSelector));
		
		myBar.add(${v3x:bodyTypeSelector("v3x")});
		myBar.add(new WebFXMenuButton("content", "<fmt:message key='common.toolbar.content.label' bundle='${v3xCommonI18N}' />","openNewEditWin();",[8,10], "", null));
		
		myBar.add(new WebFXMenuButton("superviseSetup", "<fmt:message key='common.toolbar.supervise.label' bundle='${v3xCommonI18N}' />", "openSuperviseWindowForTemplate();",[3,10], "", null));
		if(appName=="sendEdoc"){
			myBar.add(new WebFXMenuButton("taohong", "<fmt:message key='edoc.action.form.template' />","taohongWhenTemplate('edoc');", [18,4], "", null));
		}
		document.write(myBar.toString());
		document.close();
		</script></td>
  </tr>
  <tr class="bg-summary config_tr">
  	<%--模板名称 --%>
   	<td nowrap="nowrap" width="87px" height="24" class="bg-gray"><fmt:message key="edoc.doctemplate.name"/>:</td>
    <td nowrap="nowrap"  width="25%" colspan="1">
        <input name="templatename" id="templatename" maxlength='80' class="input-100per " value="${templete.subject==null?'':ctp:toHTMLWithoutSpace(templete.subject)}" />
    </td> 
    <%--公文单--%>
    <td nowrap="nowrap" width="100px" height="24" class="bg-gray"><fmt:message key="edocTable.label" />:</td>
    <td nowrap="nowrap" width="25%">
    	<select name="edoctable" id="edoctable" class="input-100per" onChange="javascript:changeEdocForm(this, ${edocFormId});">
	        <c:forEach var="edocForm" items="${edocForms}">
	    		<option value="<c:out value="${edocForm.id}"/>" title="<c:out value="${edocForm.name}"/>" <c:if test="${edocForm.id==edocFormId}">selected</c:if>>
	    			<c:out value="${edocForm.name}"/>
	    		</option>
	   		</c:forEach>
    	</select>
    </td>
     <%--模板类型--%>
    <td nowrap="nowrap" width="65px" class="bg-gray"><fmt:message key="templete.type.label" bundle="${colI18N}"/>:</td>
    <td nowrap="nowrap" width="25%">
	    <select id="template_type" name="type" class="input-100per"	onChange="alertChangeType(this)">
			<option value="templete" selected>
				<fmt:message key='templete.category.type.${categoryType}' />
			</option>
			<option value="text" <c:if test="${templateType=='text'}"> selected</c:if>>
				<fmt:message key='templete.text.label' bundle="${colI18N}" />
			</option>
			<option value="workflow" <c:if test="${templateType=='workflow'}"> selected</c:if>>
				<fmt:message key='templete.workflow.label' bundle="${colI18N}" />
			</option>
		</select>
	</td>
    <td>&nbsp;</td>
  </tr>
  <tr class="bg-summary config_tr">
  	<%--流程 --%>
  	<td height="29" class="bg-gray"><fmt:message key="workflow.label" />:</td>
   	<td>
   	   <table  class="w100b" border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
   	      <tr>
   	        <td style="width: 100%">
   	        <fmt:message key='default.workflowInfo.value' var="dfwf" />
        <input id="process_info" style="width:100%;" name="workflowInfo" class="input-100per cursor-hand" readonly value="<c:out value="${workflowNodesInfo}" default="${dfwf}" />" onClick="createEdocWFPersonal()" ${isFromTemplate == true ? 'disabled' : ''}/>
   	        </td>
   	        <td>
   	        <!--input  onclick="selectSourceTemplate()" ${templateType=='text' ? 'disabled':''} type="button" id="copyFlow" style="padding-right:0px;padding-left:0px;width:100%;" class="common_button" value="${ctp:i18n('template.systemNewTem.copyForm')}"/-->
            </td>
   	      </tr>
   	   </table>
    </td>
    <%--预归档到--%>
    <c:if test="${ctp:hasPlugin('doc')}">
    <td class="bg-gray"><fmt:message key="prep-pigeonhole.label.to" />:</td>     
    <td>
	    <select id="selectPigeonholePath" class="input-100per" onchange="pigeonholeEvent(this,'<%=ApplicationCategoryEnum.edoc.key()%>','templete',this.form)">
	    	<option id="defaultOption"  value="1"><fmt:message key="common.default" bundle="${v3xCommonI18N}"/></option>   
	    	<option id="modifyOption" value="2">${v3x:_(pageContext, 'click.choice')}</option>
	    	<c:if test="${archiveName ne null && archiveName ne ''}" >
	    		<option value="3" selected>${archiveName}</option>
	    	</c:if>
	    </select>
    </td>
    </c:if>
     <c:if test="${!ctp:hasPlugin('doc')}">
    <td class="bg-gray"><fmt:message key="trace.label.traceornot" />:</td>     
    <td>
	    <select id="canTrackWorkFlow" name="canTrackWorkFlow" class="input-100per">
	    	<option  <c:if test="${templete.canTrackWorkflow eq 0}">selected</c:if> value="0"><fmt:message key="trace.label.designby" /></option>   
	    	<option  <c:if test="${templete.canTrackWorkflow eq 1}">selected</c:if> value="1"><fmt:message key="trace.label.zhuisuo" /></option>
	    	<option  <c:if test="${templete.canTrackWorkflow eq 2}">selected</c:if> value="2"><fmt:message key="trace.label.buzhuisuo" /></option>
	    </select>
    </td>
    </c:if>
    <%-- 跟踪 --%>
    <td nowrap="nowrap" class="bg-gray"><fmt:message key="track.label" />:</td>
    <td>
		<select name="track" id="track" class="input-100per"> 
	    	<option value=1 <c:if test="${summary.canTrack==1}">selected</c:if>>
	    		<fmt:message key="edoc.form.yes" />
	    	</option>
	    	<option value=0 <c:if test="${summary.canTrack==0}">selected</c:if>>
	    		<fmt:message key="edoc.form.no" />
	    	</option>
	    </select>
	    
	    <div style="visibility:hidden;display: none;">
            <select name="categoryId" id="categoryId" class="input-100per">
                <c:if test="${param.from == 'SYS'}">
                    <option value="${categoryType}"><fmt:message key="templete.category.type.${categoryType}" bundle="${colI18N}"/></option>
                </c:if>
                ${categoryHTML}
            </select>
            <script type="text/javascript">setSelectValue('categoryId', "${ctp:escapeJavascript(categoryId)}");</script>
        </div>
	</td>
    <td width="50px" nowrap="nowrap" align="right">
		<a id="show_more" class="clearfix">${ctp:i18n('collaboration.newcoll.show')}<span class="ico16 arrow_2_b"></span></a>
	</td>
  </tr>
  <%--更多第一行 --%>
  <tr id="show_more_tr1" class="newinfo_more bg-summary config_tr" style="display: none">
  	<%-- 流程期限 --%>
	<td nowrap="nowrap" class="bg-gray"><fmt:message key="process.cycle.label"/>:</td>     
    <td colspan="1">
    	<select name="deadline" id="deadline" class="input-100per" onChange="javascript:compareTime(this)">
    		<v3x:metadataItem metadata="${deadlineMetadata}" showType="option" name="deadline" selected="${summary.deadline}" bundle="${colI18N}"/>
    	</select>    
    </td>
    <%-- 提醒 --%>
    <td class="bg-gray"><fmt:message key="common.remind.time.label" bundle='${v3xCommonI18N}' />:</td>
    <td>
    	<select name="advanceRemind" id="advanceRemind" class="input-100per" onChange="javascript:compareTime(this)">
    		<v3x:metadataItem metadata="${remindMetadata}" showType="option" name="deadline" selected="${summary.advanceRemind}"  bundle="${v3xCommonI18N}"/>
    	</select>
    </td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <%--更多第二行 --%>
  <tr id="show_more_tr2" class="newinfo_more bg-summary config_tr" style="display: none">
  	<%-- 基准时长 --%>
    <td align="right" style="width:185px">
    	<fmt:message key="common.reference.time.label" bundle='${v3xCommonI18N}'/>:&nbsp;&nbsp;
    </td>
    <td align="left" colspan="1">
    	<select name="referenceTime" id="referenceTime" class="input-100per" onChange="javascript:compareTime(this)" style="width: 100%;">
	    	<v3x:metadataItem metadata="${deadlineMetadata}" showType="option" name="referenceTime1" selected="${templete.standardDuration}" bundle="${colI18N}"/>
	    </select>
    </td>
    <%--是否追溯流程--%>
  <c:if test="${ctp:hasPlugin('doc')}">
    <td class="bg-gray"><fmt:message key="trace.label.traceornot" />:</td>     
    <td>
	    <select id="canTrackWorkFlow" name="canTrackWorkFlow" class="input-100per">
	    	<option  <c:if test="${templete.canTrackWorkflow eq 0}">selected</c:if> value="0"><fmt:message key="trace.label.designby" /></option>   
	    	<option  <c:if test="${templete.canTrackWorkflow eq 1}">selected</c:if> value="1"><fmt:message key="trace.label.zhuisuo" /></option>
	    	<option  <c:if test="${templete.canTrackWorkflow eq 2}">selected</c:if> value="2"><fmt:message key="trace.label.buzhuisuo" /></option>
	    </select>
    </td>
    </c:if>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <%--附件展现 --%>
  <tr id="attachmentTR" class="bg-summary config_tr" style="display:none;">
      <td nowrap="nowrap" height="18" class="bg-gray" valign="top"><fmt:message key="common.attachment.label" bundle="${v3xCommonI18N}" />:</td>
      <td colspan="6" valign="top"><div class="div-float">(<span id="attachmentNumberDiv"></span><fmt:message key="edoc.unit" bundle="${v3xCommonI18N}" />)</div>
		<v3x:fileUpload attachments="${attachments}" canDeleteOriginalAtts="${canDeleteOriginalAtts}" originalAttsNeedClone="${cloneOriginalAtts}" />      </td>
  </tr>
  <tr class="config_tr">
  	<td colspan="7" height="6" class="bg-b"></td>
  </tr>
  <tr valign="top">
	<td colspan="7" style="border-top:1px solid #d8d8d8;"><table id="formTableHeight" width="100%" height="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:white;">
		<tr valign="top">
			<td>
				<div id="formAreaDiv" class="scrollList">
					<div style="display:none">
						<textarea id="xml" cols="" rows="">${formModel.xml}</textarea>
		         	</div>
		         	<div style="display:none">
				   		<textarea id="xslt" cols="" rows="">${formModel.xslt}</textarea>
				    </div>
				 	<div id="html" name="html" style="border:1px solid;border-color:#FFFFFF;height:0px;"></div>
				 	<div id="img" name="img" style="height:0px;"></div>	 
					<div style="display: none">
						<textarea name="submitstr" id="submitstr" cols="" rows=""></textarea>
					</div>
				</div>
			</td>
		</tr>
	</table></td>
  </tr>
</table>
<div style="display:none" id="processModeSelectorContainer">
    <%@include file="../processModeSelector.jsp" %>
</div>
</form>

<iframe name="personalTempleteIframe" scrolling="no" frameborder="0" height="0" width="0"></iframe>
<script type="text/javascript">
initProcessXml();
hiddenNoteArea();
<c:if test="${isFromTemplate}" >
isFromTemplate = true;
</c:if>
<c:if test="${not isFromTemplate}" >       
isFromTemplate = false;
</c:if>

if(typeof(officeParams)!="undefined"){
	officeParams.canEdit = "true";
}
</script>
</body>
</html>