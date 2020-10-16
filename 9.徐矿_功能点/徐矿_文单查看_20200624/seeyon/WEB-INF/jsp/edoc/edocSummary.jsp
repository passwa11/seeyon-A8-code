<%@page import="com.seeyon.ctp.common.i18n.ResourceUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="com.seeyon.v3x.exchange.util.Constants"%>
<%@page import="com.seeyon.v3x.edoc.manager.EdocSwitchHelper"%>

<%@page import="com.seeyon.ctp.common.AppContext"%>
<html style="height: 100%;">
<head>
<%@ include file="../common/INC/noCache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="X-UA-Compatible" content="IE=EmulateIE9">
<%@ include file="edocHeader.jsp"%>
<%-- <%@ include file="../doc/pigeonholeHeader.jsp" %> --%>
<%@ include file="/WEB-INF/jsp/edoc/lock/edocLock_js.jsp"%>
<c:if test="${v3x:hasPlugin('bulletin')}">
<jsp:include page="/WEB-INF/jsp/bulletin/bulletin_issue_edoc_js.jsp"></jsp:include>
</c:if>
<c:if test="${v3x:hasPlugin('calendar')}">
<script type="text/javascript" src="${path}/apps_res/calendar/js/calEvent_Create_addData_js.js"></script>
</c:if>
<title><fmt:message key="common.page.title${v3x:suffix()}" bundle="${v3xCommonI18N}" /></title>
<%@page import="com.seeyon.ctp.common.constants.ApplicationCategoryEnum"%>
<link rel="stylesheet" type="text/css"
	href="<c:url value="/common/jquery/themes/default/easyui.css${v3x:resSuffix()}" />" />
<script type="text/javascript" charset="UTF-8"
	src="<c:url value="/common/js/jquery.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8"
	src="<c:url value="/apps_res/v3xmain/js/phrase.js${v3x:resSuffix()}" />"></script>

<c:set var="param_from"
	value="${param.from eq 'sended'||param.from eq 'listSent'||param.from eq 'Sent'}" />
<c:set var="canEditAtt"
	value="${allowUpdateAttachment &&  param_from=='true'  && !summary.finished && (param.openFrom ne 'recRegisterResult' && param.openFrom ne 'sendRegisterResult') }" />
<c:set value="${v3x:currentUser().id}" var="currentUserId" />
<c:set value="${v3x:currentUser().name}" var="currentUserName" />
<c:set value="${v3x:currentUser().loginAccount}"
	var="currentUserAccount" />
<c:set value="${param.from eq 'Pending' && affair.state eq 3}"
	var="hasSignButton" />
<c:set
	value="${appTypeName == 'sendEdoc'? '19': (appTypeName== 'recEdoc'? '20' :'21')}"
	var="appTypeId" />
<v3x:selectPeople id="flash" panels="Department,Team"
	selectType="Department,Team,Member"
	departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
	jsFunction="monitorFrame.dataToFlash(elements)"
	viewPage="selectNode4Workflow" />

<fmt:message key='common.toolbar.print.label' bundle='${v3xCommonI18N}'
	var="printLabel" />
<fmt:message key="common.attribute.label" bundle="${v3xCommonI18N}"
	var="attributeLabel" />
<fmt:message key='flow.node.excute.detail' bundle="${colI18N}"
	var="excuteDetailLabel" />
<c:set var="isOfficeBodyType"
	value="${'OfficeWord' eq summary.firstBody.contentType or 'OfficeExcel' eq summary.firstBody.contentType}" />
<style>
.multilevel_sign_16 {
	background-position: -256px -96px;
}

.ico16 {
	display: inline-block;
	vertical-align: middle;
	height: 16px;
	width: 16px;
	line-height: 16px;
	background: url(images/icon16.png) no-repeat;
	background-position: 0 0;
	cursor: pointer;
}
</style>
<script>
	/* 客开：徐矿集团【主附件选中，且单选按钮不能点击】 chenqiang 2019年3月27日 start */
	var xkjtAttachmentHasRadio = '${xkjtAttachmentHasRadio}';
	var canClick = '${canClick}';
	var mainAttachmentId = '${mainAttachmentId}';
	/* 客开：徐矿集团【主附件选中，且单选按钮不能点击】 chenqiang 2019年3月27日 end */
    var mids="${trackIds}";
	var _canFavorite='${canFavorite}';
	var trackStatus = '${trackStatus}';
	var customSetTrackFlag = '${customSetTrack}' == 'true';
	var _personalTemplateFlag ='${personalTemplateFlag}' == "1";
	var _affairSubState='${affairSubState}';
	var officecanPrint="${officecanPrint}";
	var isLoadNewFileEdoc=false;//修改正文的时候是否重新导入文件标记
	var officecanSaveLocal="${officecanSaveLocal}";
	var contentPrint = "${contentPrint}";
	var contentSaveLocal = "${contentSaveLocal}";
    var flowPermAccountId= "${flowPermAccountId}";
    var templeteProcessId ="${templeteProcessId}";
	var summaryId = "${summary.id}";
	var wendanId = "${summary.formId}";
	var appTypeName="${appTypeName}";
	var hasDiagram = "${hasDiagram}";
	var currentNodeId = "${currentNodeId}";
	var showMode = 0;
	var showHastenButton = "${showHastenButton}";
	var isNewCollaboration = "${isNewCollaboration}";
	var isTemplete = false;
	var isCheckTemplete = false;
	var templateFlag = "${templateFlag}";
	var transmitSendNewEdocId="${transmitSendNewEdocId}";
	var isEdocCreateRole="${isEdocCreateRole}";
	var affair_id = "${affair.id}";
	var summaryOrgAccountId = "${summary.orgAccountId}";
	var affairFirstViewTime = '${affair.firstViewDate.time}';
	var affairSignleViewPeriod = '${affair.signleViewPeriod}';
	var showHl = '${showHl}';

	var hasPrepigeonholePath="${hasSetPigeonholePath}";
	var sendUserDepartmentId="${edocSendMember.orgDepartmentId}";
	var sendUserAccountId="${edocSendMember.orgAccountId}";
	var caseId = "${caseId }";
	var processId = "${processId }";
	var noConfigItem = "${noFindPermission}";
	var _fawentishi = "${_fawentishi}" == "1";
	var _shouwentishi = "${_shouwentishi}" == "1";
	var showOriginalElement_colAssign=false;
	//异步加载流程信息
	var isLoadProcessXML = false;
	var caseProcessXML = "";
	var caseLogXML = "";
	var caseWorkItemLogXML = "";
	var clickClose = false;

	var divPhraseDisplay = 'none';
	var onlySeeContent ='${onlySeeContent}';
	var phraseURL = '<html:link renderURL="/phrase.do?method=list" />';
	var hasSign=${v3x:containInCollection(commonActions, 'Sign') || v3x:containInCollection(advancedActions, 'Sign')};
	var hasExchangeType = ${v3x:containInCollection(baseActions, 'EdocExchangeType')};
	var permKey="${nodePermissionPolicyKey}";
	var formAction="<html:link renderURL='/edocController.do?method=finishWorkItem' />";
	var finished="${finished}";
	var subject_name="${v3x:toHTML(summary.subject)}";
	var taohongSendUnitType = 1;
	//分支 开始
	//分支
	var branchs = new Array();
	var team = new Array();
	var secondpost = new Array();
	var startTeam = new Array();
	var startSecondpost = new Array();
	<c:if test="${branchs != null}">
		var handworkCondition = _('edocLang.handworkCondition');
		<c:forEach items="${branchs}" var="branch" varStatus="status">
			var branch = new ColBranch();
			branch.id = ${branch.id};
			branch.conditionType = "${branch.conditionType}";
			branch.formCondition = "${v3x:escapeJavascript(branch.formCondition)}";
			branch.conditionTitle = "${v3x:escapeJavascript(branch.conditionTitle)}";
			branch.conditionDesc = "${v3x:escapeJavascript(branch.conditionDesc)}";
			branch.isForce = "${branch.isForce}";
			eval("branchs["+${branch.linkId}+"]=branch");
		</c:forEach>
	</c:if>
	<c:if test="${teams != null}">
		<c:forEach items="${teams}" var="team">
			team["${team.id}"] = ${team.id};
		</c:forEach>
	</c:if>
	<c:if test="${secondPosts != null}">
		<c:forEach items="${secondPosts}" var="secondPost">
			secondpost["${secondPost.depId}_${secondPost.postId}"] = "${secondPost.depId}_${secondPost.postId}";
		</c:forEach>
	</c:if>
	<c:if test="${startTeams != null}">
		<c:forEach items="${startTeams}" var="startTeam">
			startTeam["${startTeam.id}"] = "${startTeam.id}";
		</c:forEach>
	</c:if>
	<c:if test="${startSecondPosts != null}">
		<c:forEach items="${startSecondPosts}" var="startSecondPost">
			startSecondpost["${startSecondPost.depId}_${startSecondPost.postId}"] = "${startSecondPost.depId}_${startSecondPost.postId}";
		</c:forEach>
	</c:if>
	//分支 结束
	var lineMax = 7;
	//office插件使用的JS变量
	${contentRecordId}
	var bodyType="${summary.firstBody.contentType}";
	var resources = <c:out value="${CurrentUser.resourceJsonStr}" default="null" escapeXml="false"/>;

	/*开始*/
	var xmlDoc = null;
	var _sysAdminFlag = "${CurrentUser.systemAdmin}"!="true";
	var _userLocale = "${CurrentUser.locale}";
	var _userLoginName = "${ctp:escapeJavascript(CurrentUser.loginName)}";
	var _sessionId = "${sessionId }";
	var _curUserId = "${CurrentUser.id}";
	/*结束*/

</script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/edoc/js/edocSummary.js${v3x:resSuffix()}" />"></script>
<%@ include file="/WEB-INF/jsp/ctp/portal/common/components_theme.jsp"%>
<style>
.detail-subject {
	padding-top: 5px;
}
</style>
<script>
var isOutOpinions = false;
var timerBrighter;
var timerDarker;
var hasLoadHtmlSign = false;
//修改人：杨帆 2012-2-15———定义显示意见处理输入栏和常用语等变量 --start
var canShowOpinion="${v3x:containInCollection(baseActions, 'Opinion')}";  //允许显示处理意见
var canShowCommonPhrase="${v3x:containInCollection(baseActions, 'CommonPhrase')}"; //允许显示常用语
var canWordNoChange="${v3x:containInCollection(advancedActions, 'WordNoChange') or v3x:containInCollection(commonActions, 'WordNoChange')}"; //允许修改文号
var canUpdateForm="${v3x:containInCollection(advancedActions, 'UpdateForm') or v3x:containInCollection(commonActions, 'UpdateForm')}";   //允许修改表单
var canEditBody="${v3x:containInCollection(advancedActions, 'Edit') or v3x:containInCollection(commonActions, 'Edit')}";     //允许修改正文
//office插件复制粘贴控制 -- 当前正文是否允许编辑
var canEdit="${(v3x:containInCollection(advancedActions, 'Edit') or v3x:containInCollection(commonActions, 'Edit')) and hasSignButton}";
var canUploadRel="${v3x:containInCollection(baseActions, 'UploadRelDoc')}";//允许关联文档
var canUploadAttachment="${v3x:containInCollection(baseActions, 'UploadAttachment')}";//可以上传附件
var canShowAttitude="${attitudes != 3}"; //是否显示态度
var _atts="${attachmentsOpinion ne null && fn:length(attachmentsOpinion) > 0}";//处理意见是否有附件
var isCheckContentEdit=true;
window.onload=init_edocSUmmary;
//获取APPNAME
var edocType = "${summary.edocType}";
var appNameNode ='sendEdoc';
if(edocType=='1'){
	appNameNode ='recEdoc';
}else if(edocType=='2'){
	appNameNode ='signReport';
}
var _trackTitle = "${ctp:i18n('collaboration.newColl.alert.zdgzrNotNull')}";
var clickFlag = true;
var logTitle = "${ctp:i18n('collaboration.sendGrid.findAllLog')}";
var summary_processId = "${summary.processId}";
var currentUserId = "${currentUserId}";
var noFindNodeFawen = "${ctp:i18n("collaboration.summary.noFindNode.fawen")}";
var noFindNodeShouwen = "${ctp:i18n("collaboration.summary.noFindNode.shouwen")}";
var getBrowserFlagByRequest = "${v3x:getBrowserFlagByRequest('HideBrowsers', pageContext.request)}";
var edocErrortypePrint = "<fmt:message key='edoc.errortype.print'/>";
var edocReleaseLock = "<fmt:message key='edoc.release.lock'/>";
var edocErrorDocnum = "<fmt:message key='edoc.error.docnum'/>";
var edocCancelExchangePrivileges1 = "<fmt:message key='edoc.Cancel.exchange.privileges1' />";
var edocCancelExchangePrivileges2 = "<fmt:message key='edoc.Cancel.exchange.privileges2'/>";
var edoc_turn_rec_info = "<fmt:message key='edoc.turn.rec.info' bundle='${exchangeI18N}'/>";
var permissionAdvanceOfficeHtmlSignAuthorLabel = "${ctp:i18n('permission.advanceOffice.htmlSign.author.label')}";
var nodePpolicyShenpi = "${ctp:i18n("node.policy.shenpi")}";
var nodePolicyYuedu = "${ctp:i18n("node.policy.yuedu")}";
var workflowNodePropertyDealExplain = "${ctp:i18n('workflow.nodeProperty.dealExplain')}";
var permissionClose = "${ctp:i18n('permission.close')}";
var permissionOperationTurnRecEdoc = "${ctp:i18n('permission.operation.TurnRecEdoc')}";
var edoc = "${edoc}";
var openModal = "${openModal}";
var canEditAtt = "${canEditAtt}";
var detailURL = "${detailURL}";
var advanceOffice = "${v3x:hasPlugin('advanceOffice')}";
var specialStepBack = "${specialStepBack}";
var activityId = "${activityId}";
var paramFrom = "${ctp:escapeJavascript(param.from)}";
var isHasDealDiv = ${param.from eq 'Pending'};
var workitemId = "${workitemId}";
var processId = "${processId}";
var caseId = "${caseId}";
var wOrgAccountId = "${wOrgAccountId}";
var sessionScopeDepartmentId = "${sessionScope['com.seeyon.current_user'].departmentId}";
var departmentId = sessionScopeDepartmentId;;
var currentUserAccount = "${currentUserAccount}";
var firstBodyContent = "${firstBodyContent}";
var affairHaveBeenProcessing = escapeStringToJavascript(v3x.getMessage("edocLang.affair_have_been_processing",'${ctp:escapeJavascript(summary.subject)}'));
var deptSenderList = "${ctp:escapeJavascript(deptSenderList)}";
var createrExchangeDepts = "${ctp:escapeJavascript(createrExchangeDepts)}";
var isOpenFrom = "${ctp:escapeJavascript(isOpenFrom)}";
var affairSubState = "${affair.subState}";
var scene = "${scene}";
var hasBody1 = "${hasBody1}";
var hasBody2 = "${hasBody2}";
var affairState = "${affair.state}";
var currentUserName = "${ctp:escapeJavascript(currentUserName)}";
var superviseEdocLabel = '<%=ResourceUtil.getString("supervise.edoc.label")%>';
var bodyType = "${bodyType}";
var performer = "${performer}";
var summaryTempleteId = "${summary.templeteId}";
var isFromSDL = "${ctp:escapeJavascript(isFromSDL)}";
var isCollCube = "${isCollCube}";
var isColl360 = "${isColl360}";
var colChooseMessageRecevier = '<fmt:message key='col.choose.message.recevier' bundle="${v3xCommonI18N}"/>';
var edocTemporaryTodoRemind = '<fmt:message key='edoc.Temporary.todo.remind'/>';
var _affairMemberId="${affairMemberId}";//当前待办事项的所属人Id
var affairMemberName="${ctp:escapeJavascript(affairMemberName)}";//当前待办事项的所属人Name
var pagePath='${pageContext.request.contextPath}';
var canShowZCDB="${v3x:containInCollection(baseActions, 'Comment')}";
var canShowJHLX="${v3x:containInCollection(baseActions, 'EdocExchangeType')}";
var canShowGZGD="${v3x:containInCollection(baseActions, 'Track') or v3x:containInCollection(baseActions, 'Archive')}";
var commonActionsStr="${commonActions}";
var advancedActionsStr="${advancedActions}";
var isRemoveForward="${isRemoveForward}";
var isdealStepBackShow="${isdealStepBackShow}";
var isdealStepStopShow="${isdealStepStopShow}";
var isdealCancelShow="${isdealCancelShow}";
var edoc_action_script_template = "<fmt:message key='edoc.action.script.template' />";
var edoc_select_operation = "${ctp:i18n('edoc.select.operation')}";
var openFromUC = "${param.openFrom}" == 'ucpc';
var openFrom= "${param.openFrom}";
</script>
<style type="text/css">

.edocbarmargin {
	color: #ffffff;
}

.edocbarmargin a {
	color: #ffffff;
}

.layout-split-east {
	border-left: 1px solid #cccccc;
	padding-left: 4px;
	background: url("<c:url value='/common/images/deal/split.gif' />") left
		center no-repeat #cccccc;
}

.dealicons-advance,.dealicons {
	display: inline-block;
	margin: 0 4px 0 0;
	vertical-align: middle;
}

.metadataItemDiv label,.metadataItemDiv span {
	vertical-align: middle;
	display: inline-block;
	padding-top: 2px;
	margin: 0;
}

#htmlContentDiv {
	height: 0px;
}
.deal_area .advanceICON a{ overflow:hidden; padding:3px; display:block; /*width:74px;*/overflow: hidden; text-overflow: ellipsis;  word-break: keep-all; white-space: nowrap;}
.deal_area .advanceICON a{color:#111;}
.deal_area .advanceICON a{color:#111;}
.deal_area .advanceICON a:link{color:#111;}
.deal_area .advanceICON a:hover{color:#111;background: #ffffff;}
.processAdvanceDIV .advanceICON a{ overflow:hidden; padding:3px; display:block; /*width:95px; */text-overflow:ellipsis; word-break:keep-all; white-space:nowrap;}
.processAdvanceDIV .advanceICON a{color:#111;}
.processAdvanceDIV .advanceICON a{color:#111;}
.processAdvanceDIV .advanceICON a:link{color:#111;}
.processAdvanceDIV .advanceICON a:hover{color:#fff;background: blue;}
</style>
</head>
<body id="easyui-layout" class="easyui-layout" scroll="no"
	style="height: 100%;background: #ededee;"
	onbeforeunload="unLoad('${summary.processId}', '${param.summaryId}','${currentUserId}')"
	onunload="unLoad('${summary.processId}', '${param.summaryId}','${currentUserId}');">

	<%-- best 待阅和已阅文件打开时默认打开正文 --%>
	<input type="hidden" id="isShow" value="${isShow }"/>
	<%-- best 待阅和已阅文件打开时默认打开正文 --%>
	<%-- best 当前节点是否是最后一节点 --%>
	<input type="hidden" id="isLast" value="${isLast }"/>
	<%-- best 当前节点是否是最后一节点 --%>

	<c:if test="${param.from eq 'sended' || param.from eq 'listSent'}">
		<%--已发修改附件提交 --%>
		<form id="attchmentForm">
			<div id="attachmentInputs"></div>
		</form>
	</c:if>
	<v3x:attachmentDefine attachments="${attachments}" />
	<div region="center" id="center_reagin" border="false"
		style="overflow: hidden; height: 100%;">
		<form id="theform" name="theform"
			action="<html:link renderURL='/edocController.do?method=finishWorkItem${ctp:csrfSuffix()}' />"
			method="post" style='height: 100%; margin: 0px;'
			onSubmit="return false">
			<table border="0" cellpadding="0" cellspacing="0" width="100%"
				height="100%" align="center" class="CollTable">
				<input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}" />
				<%--转发公文发送之后，affair待办中就有了forwardMember了，之后流程中所有节点产生的affair事项中都要一直有这个值 --%>
				<input type="hidden" name="unCancelledVisorSupervise" id="unCancelledVisorSupervise" value="${unCancelledVisor }" />
				<input type="hidden" name="sVisorsFromTemplateSupervise" id="sVisorsFromTemplateSupervise" value="${sVisorsFromTemplate}" />
				<input id="forwardMember" name="forwardMember" type="hidden" value="${forwardMember }" />
				<input type="text" class="hidden" value="${summary.subject}" id="fileNameInput" />
				<input id="subject" type="hidden" value="${summary.subject}">
				<!-- lijl添加,显示意见状态 -->
				<input type="hidden" name="optionType" id="optionType" value="${optionType}">
				<%-- 是否是回退affair --%>
				<input type="hidden" name="isFlowBack" id="isFlowBack" value="${isFlowBack}"/>
				<input type="hidden" name="optionWay" id="optionWay" value="${optionWay }">
				<input type="hidden" name="optionId" id="optionId" value="${optionId}">
				<input type="hidden" name="affairState" id="affairState" value="${affairState}">
				<input type="hidden" name="affState" id="affState" value="${affair.subState}">
				<%--保留上次的签发日期 --%>
				<input type="hidden" name="signingDate" id="signingDate" value="<fmt:formatDate value='${summary.signingDate}' pattern='yyyy-MM-dd'/>">
				<%--待办操作 --%>
				<input type="hidden" name="bodyType" id="bodyType" value="${summary.firstBody.contentType}">
				<input type="hidden" name="contentId" id="contentId" value="${ctp:toHTML(summary.firstBody.content)}">
				<c:if test="${param.from eq 'Pending'}">
					<%--当前会签需要加上该隐藏域 --%>
					<input type="hidden" id="readyObjectJSON" name="readyObjectJSON">
					<input type="hidden" id="docMarkAccess" name="docMarkAccess" value="${docMarkAccess }" />
					<input type="hidden" id="docMark2Access" name="docMark2Access" value="${docMark2Access }" />
					<input type="hidden" id="serialNoAccess" name="serialNoAccess" value="${serialNoAccess }" />
					<input id="workflow_newflow_input" name="workflow_newflow_input" type="hidden" />
					<input id="workflow_node_peoples_input" name="workflow_node_peoples_input" type="hidden" />
					<input id="workflow_node_condition_input" name="workflow_node_condition_input" type="hidden" />
					<input id="process_rulecontent" name="process_rulecontent" type="hidden" />
					<input id="workflow_last_input" name="workflow_last_input" type="hidden" />
					<input id="nodeName" name="nodeName" type="hidden" />
					<input type="hidden" id="process_message_data" name="process_message_data">
					<input id="isReportToSupAccount" type="hidden" />
					<tr>
						<td height="30" class="detail-summary" valign="top">
							<%-- 添加公文文号隐藏域 --%>
							<input type="hidden" name="docMark" id="docMark" />
							<input type="hidden" name="docMark2" id="docMark2" />
							<input type="hidden" name="serialNo" id="serialNo" />
							<input type="hidden" name="signing_date" id="signing_date" />
							<!-- 接收从弹出页面提交过来的数据 -->
							<input type="hidden" name="returnDeptId" id="returnDeptId" value="">
							<%--xiangfan 添加 修复GOV-4911 --%>
							<input type="hidden" name="popJsonId" id="popJsonId" value="">
							<input type="hidden" name="popNodeSelected" id="popNodeSelected" value="">
							<input type="hidden" name="popNodeCondition" id="popNodeCondition" value="">
							<input type="hidden" name="popNodeNewFlow" id="popNodeNewFlow" value="">
							<input type="hidden" name="allNodes" id="allNodes" value="">
							<input type="hidden" name="nodeCount" id="nodeCount" value="">
							<div style="display: none" id="processModeSelectorContainer"></div>
							<input type="hidden" id="ajaxUserId" name="ajaxUserId" value="${currentUserId}" />
							<input type="hidden" id="affair_id" name="affair_id" value="${param.affairId}" />
							<!-- 是否对督办设置进行了修改 -->
							<input type="hidden" id="valiSupervise" name="valiSupervise" value="" />
							<!-- 是否对督办设置进行了修改 -->
							<input type="hidden" id="summary_id" name="summary_id"
								value="${param.summaryId}" /> <input type="hidden"
								name="startMemberId" value="${summary.startMember.id}" />
							<input type="hidden" name="appName" id="appName" value='<%=ApplicationCategoryEnum.edoc.getKey()%>' />
							<input type="hidden" id="policy" name="policy" value="${nodePermissionPolicyKey}" />
							<input type="hidden" id="edocType" name="edocType" value="${summary.edocType}" />
							<input type="hidden" id="archiveId" name="archiveId" value="${summary.archiveId}">
							<input type="hidden" id="prevArchiveId" name="prevArchiveId" value="${summary.archiveId}">
							<input type="hidden" name="supervisorId" id="supervisorId" value="${supervisorId}">
							<input type="hidden" name="isDeleteSupervisior" id="isDeleteSupervisior" value="false">
							<input type="hidden" name="orgSupervisorId" id="orgSupervisorId" value="${supervisorId}">
							<input type="hidden" name="supervisors" id="supervisors" value="${supervisors}">
							<input type="hidden" name="unCancelledVisor" id="unCancelledVisor" value="${unCancelledVisor }">
							<input type="hidden" name="sVisorsFromTemplate" id="sVisorsFromTemplate" value="${sVisorsFromTemplate}">
							<input type="hidden" name="awakeDate" id="awakeDate" value="${awakeDate}">
							<input type="hidden" name="superviseTitle" id="superviseTitle" value="${v3x:toHTML(superviseTitle)}">
							<input type="hidden" name="processId" id="processId" value="${summary.processId}">
							<input type="hidden" name="caseId" id="caseId" value="${summary.caseId }">
							<input type="hidden" name="count" id="count" value="${count}" />
							<input type="hidden" name="disPosition" id="disPosition" value="${disPosition}" />
							<input type="hidden" name="workitemId" id="workitemId" value="${workitemId}" />
							<input type="hidden" name="performer" id="performer" value="${performer}" />
							<%--记录是否进行了文单套红，主要用来记录JS记录日志--%>
							<input type="hidden" name="redForm" id="redForm" value="false">
							<%--记录是否进行了正文套红，主要用来记录JS记录日志--%>
							<input type="hidden" name="redContent" id="redContent" value="false" />
							<input type="hidden" id="currentLoginAccountId" name="currentLoginAccountId" value="${v3x:currentUser().loginAccount}">
							<input type="hidden" id="pushMessageMemberIds" name="pushMessageMemberIds" value="">
							<%--office --%>
							<input type="hidden" name="currContentNum" id="currContentNum" value="0">
							<input type="hidden" name="isUniteSend" id="isUniteSend" value="${summary.isunit}">
							<input type="hidden" name="orgAccountId" id="orgAccountId" value="${summary.orgAccountId}">
							<%--PDF--%>
							<INPUT type="hidden" NAME="isConvertPdf" id="isConvertPdf" value="" />
							<%--WORD转PDF的时候，生成的PDF正文的ID--%>
							<input type="hidden" name="newPdfIdFirst" id="newPdfIdFirst" value="${empty firstPDFId ? newPdfIdFirst : firstPDFId}" />
							<input type="hidden" name="newPdfIdSecond" id="newPdfIdSecond" value="${newPdfIdSecond}" />
							<input type="hidden" id="workflow_node_peoples_input" name="workflow_node_peoples_input" value="" />
							<input type="hidden" id="workflow_node_condition_input" name="workflow_node_condition_input" value="" />
							<input type="hidden" id="process_xml" name="process_xml" value="" />
							<input type="hidden" id="processChangeMessage" name="processChangeMessage" value="" />
							<input type="hidden" name="process_desc_by" id="process_desc_by" value="xml" />
							<input type="hidden" name="currentNodeId" id="currentNodeId" value="${currentNodeId }" />
							<!-- 将isMatch缺省值为true，判断是否最后一个处理人在preSend中 -->
							<input type="hidden" name="isMatch" id="isMatch" value="true" />
							<!-- 暂存待办的意见ID -->
							<input type="hidden" name="oldOpinionId" value="${(affair.state==3 && (affair.subState==13||affair.subState==6) && tempOpinion.opinionType!=3) ? tempOpinion.id : ""}" />
							<input type="hidden" name="__ActionToken" readonly value="SEEYON_A8">
							<%-- post提交的标示，先写死，后续动态 --%>

							<!-- 暂存待办提醒时间-->
							<input type="hidden" name="zcdbTime" id="zcdbTime" value="<fmt:formatDate value='${zcdbTime}' pattern='yyyy-MM-dd HH:mm'/>">

							<input type="hidden" name="hasSysTemplateArchiveId" id="hasSysTemplateArchiveId" value="${hasSysTemplateArchiveId }" />

							<v3x:selectPeople id="wf" panels="Department,Team"
								selectType="Department,Team,Member"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								jsFunction="selectInsertPeople(elements)"
								viewPage="selectNode4Workflow" />
							<script type="text/javascript">var hiddenMultipleRadio_wf = true;</script>
							<v3x:selectPeople id="colAssign"
								panels="Department,Team,Post"
								selectType="Department,Team,Post,Member"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								jsFunction="selectColAssign(elements)" />
							<v3x:selectPeople id="addInform"
								panels="Department,Team,Post"
								selectType="Department,Team,Post,Member"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								jsFunction="selectAddInform(elements)"
								viewPage="selectNode4Workflow" />

							<v3x:selectPeople id="passRead" panels="Department,Team"
								selectType="Account,Department,Team,Member"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								jsFunction="selectPassRead(elements)" />
							<v3x:selectPeople id="addMoreSign" panels="Department"
								selectType="Department,Member"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								jsFunction="addMoreSignResult(elements)" />
							<v3x:selectPeople id="sv" panels="Department"
								selectType="Member"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								jsFunction="sv(elements)" />
							<v3x:selectPeople id="transfer" panels="Department"
								selectType="Member" minSize="1"
								departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
								maxSize="1" showMe="false"
								jsFunction="transferFun(elements)"
								 />
						    <input type = "hidden" name = "transferMemberId" id = "transferMemberId"  />
							<input type="hidden" name="affairId" value="${param.affairId}" />
							<span id="selectPeoplePanel"></span>
							<div oncontextmenu="return false"
								style="position: absolute; right: 20px; top: 120px; width: 260px; height: 60px; z-index: 2; background-color: #ffffff; display: none; overflow: no; border: 1px solid #000000;"
								id="divPhrase" onmouseover="showPhrase()"
								onmouseout="hiddenPhrase()" oncontextmenu="return false">
								<IFRAME width="100%" id="phraseFrame" name="phraseFrame"
									height="100%" frameborder="0" align="middle" scrolling="no"
									marginheight="0" marginwidth="0"></IFRAME>
							</div>
							<script type="text/javascript">
								document.getElementById("process_xml").value = caseProcessXML;
							    var exMems = new Array();
								exMems = exMems.concat(parseElements("${v3x:parseElementsOfIds(supervisorIds, 'Member')}"));
								excludeElements_sv = exMems;
								//传阅知会，不回现选择数据
								showOriginalElement_addInform=false;
								showOriginalElement_passRead=false;
								showOriginalElement_addMoreSign=false;
								showOriginalElement_transfer = false;
								var isConfirmExcludeSubDepartment_colAssign=true;
								var isConfirmExcludeSubDepartment_addInform=true;
								var isConfirmExcludeSubDepartment_passRead=true;
								var isConfirmExcludeSubDepartment_addMoreSign=true;
								onlyLoginAccount_flash=true;
								onlyLoginAccount_wf=true;
								onlyLoginAccount_colAssign=true;
								onlyLoginAccount_sv=true;
								var unallowedSelectEmptyGroup_colAssign = true;
								var unallowedSelectEmptyGroup_addInform = true;
								var hiddenFlowTypeRadio_addInform = true;
								var hiddenRootAccount_addInform = true;

							</script>
							<%--原标题栏 --%>
							<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0" class="deal_toobar_m" style="border: 1px #ffffff solid; border-left: 0px; border-right: 0px;">
								<tr>
									<td style="padding: 3px 0px 0px 0px;">
										<div style="float: left;" id="edoc_deal_deal">
											<table border="0" cellspacing="0" cellpadding="0">
												<tr>
													<%--提交--%>
													<td width="60" align="center">
														<%-- GOV-4683 公文管理-发文管理，人员收到待办公文后，点击进入公文，页面刚刚加载完提交按钮后，快速点击提交，报2个脚本错误
														edocSummary.jsp先加载时将提交按钮置灰，不能点击，当edocTopic.jsp页面加载完后再让提交按钮可以点击 --%>
														<a id="processButton" class="deal_submit validationStepback" disabled onclick="javascript:beforeSubmitButton();edocSubmit('${nodePermissionPolicyName}','${summary.id}')"><br><br>
    														<%-- best 公文在办理流程时如果是最后一个节点默认把提交换为办结 start --%>
															<c:if test="${isLast eq true}">
																办结
															</c:if>
															<c:if test="${isLast eq null || isLast eq false}">
																<fmt:message key="common.button.submit.label" bundle="${v3xCommonI18N}" />
															</c:if>
															<%-- best 公文在办理流程时如果是最后一个节点默认把提交换为办结 end --%>
														</a>
													</td>
													<%--暂存待办--%>
													<c:if test="${v3x:containInCollection(baseActions, 'Comment')}">
														<td>
															<div class="validationStepback validationStepback1">
																<table>
																	<tr>
																		<td align="center" width="55">
																		   <a id="zcdbButton" class="validationStepback validationStepback1 cursor-hand color_black" onclick="beforeSubmitButton();predoZcdb(this,'${param.summaryId}')" href="javascript:void(0)">
																		        <em class='temporary_agency'></em>
                                                                                <fmt:message key="edoc.zancundaiban.label" /><%--暂存待办--%>
																		    </a>
																		</td>
																		<td width="16">
																				<div id="zcdbAdviceDIV" class="processAdvanceDIV" style="display:none;position: absolute;  z-index: 2; background-color: #ffffff; border: 1px #C5C5C5 solid; z-index: 11" id="zcdbAdviceDiv" onMouseOver="zcdbAdviceViews(true)" onMouseOut="zcdbAdviceViews(false)" oncontextmenu="return false">
																					<div class="advanceICON">
																						<a class="padding_5 validationStepback validationStepback1" id="zcdbAdviceA" onclick="beforeSubmitButton();showDatetimeDialog()">
																							<span class="ico16 clock_16"></span>
																							<fmt:message key="stagingAndRemind.label" /><%--暂存并提醒 --%>
																						</a>
																						<input type="hidden"  name="zcdbTime" id="nowzcdbTime">
																					</div>
																				</div>
																				<div id='zcdbAdvice' class="cursor-hand arrow_1_b validationStepback validationStepback1" style="float:left;" onClick="zcdbAdviceViews()"
																						class="cursor-hand arrow_1_b"></div>
																				<div style="border-left: 1px solid #fff; padding-left: 6px;">
																					<iframe id="zcdbAdviceDivIframe" scrolling="no" frameborder="0" style="position: absolute; right: 2px; top: 65; width: 100px; height: 10px; z-index: 2;  display: none; z-index: 10"> </iframe>
																				</div>
																		</td>
																	</tr>
																</table>
															</div>
														</td>
													</c:if>
													<%-- 交换类型 --%>
													<td>
														<c:if test="${v3x:containInCollection(baseActions, 'EdocExchangeType')}">
															<div class="metadataItemDiv">
																<table>
																	<%-- 部门交换--%>
																	<tr>
																		<td>
																			<label for="edocExchangeType_depart">
																				<input type="radio" class="validationStepback" name="edocExchangeType" id="edocExchangeType_depart" onclick="disabledMemberList()" value="<%=Constants.C_iExchangeType_Dept%>"
																				<c:if test="${defaultExchangeType==0}">checked="true"</c:if>>
																				<span>
																					<fmt:message key="edoc.exchangetype.department.label" />
																				</span>
																			</label>
																		</td>
																		<td>
																		  <span id="selectExchangeDeptType" style="display:block;">
													                        <select id="exchangeDeptType" name="exchangeDeptType" <c:if test="${defaultExchangeType != 0}">disabled="disabled"</c:if> class="condition" style="width: 115px">
													                          <option value="Creater" <c:if test="${exchangeDeptTypeSwitchValue=='Creater'}">selected="selected"</c:if>>
													                            <fmt:message key="edoc.label.exchange.dept.creater" bundle="${v3xMainI18N}"/>
													                          </option>
													                          <option value="Dispatcher" <c:if test="${exchangeDeptTypeSwitchValue=='Dispatcher'}">selected="selected"</c:if>>
													                            <fmt:message key="edoc.label.exchange.dept.dispatcher" bundle="${v3xMainI18N}"/>
													                          </option>
													                        </select>
													                      </span>
																		</td>
																	</tr>
																	<%--单位交换--%>
																	<tr>
																		<td>
																			<label for="edocExchangeType_company">
																			<input type="radio" class="validationStepback" name="edocExchangeType" id="edocExchangeType_company" onClick="enabledMemberList()" value="<%=Constants.C_iExchangeType_Org%>"
																				<c:if test="${defaultExchangeType==1}">checked="true"</c:if>>
																				<span>
																					<fmt:message key="edoc.exchangetype.company.label" />
																				</span>
																			</label>

																		</td>
																		<td>
																			<span id="selectMemberList"  style="display:block;">
																				<select id="memberList" name="memberList" <c:if test="${defaultExchangeType!=1}">disabled="disabled"</c:if> class="condition" style="width: 115px">
																					<option value="">
																						<fmt:message key="edoc.category.all.label" /></option>
																					<c:forEach items="${memberList}" var="member">
																						<option value="${member.id}">${v3x:toHTML(member.name)}</option>
																					</c:forEach>
																				</select>
																			</span>
																		</td>
																	</tr>
																</table>
															</div>
														</c:if>
													</td>
													<%--跟踪、归档--%>
													<td>
														<table>
															<%--跟踪 --%>
															<tr>
																<td>
																	<c:if test="${v3x:containInCollection(baseActions, 'Track')}">
																		<c:set var="isTrack" value="${affair.track==0 ? false:true || not empty trackIds}"></c:set>
																		<span id ="trackSpanId" style="display: inline-block; margin-right: 5px; vertical-align: middle; float: left;height:18px;">
																			<input type="checkbox" class="validationStepback"
																			name="afterSign" value="track"
																			onClick="setTrackRadiio(this);" style="border:0px;" id="isTrack"
																			${v3x:outConditionExpression(isTrack, 'checked', '')}>
																			<fmt:message key="track.label" />: <label
																			for="trackRange_all"> <input type="radio"
																				name="trackRange" id="trackRange_all" ${affair.track==0 ? 'DISABLED' : ''}
																				onClick="setTrackCheckboxChecked();" value="1"
																				${isTrack && empty trackIds?'checked':''} /> <fmt:message
																					key="col.track.all" bundle="${v3xCommonI18N}" />
																		</label> <label for="trackRange_part"> <c:set
																					value="${v3x:parseElementsOfIds(trackIds, 'Member')}"
																					var="mids" /> <input type="hidden" value="${trackIds}"
																				name="trackMembers" id="trackMembers" /> <v3x:selectPeople
																					id="track"
																					panels="Department,Team,Post,Outworker,RelatePeople"
																					selectType="Member" jsFunction="setPeople(elements)"
																					originalElements="${mids}" /> <input type="radio"
																				name="trackRange" id="trackRange_part" ${affair.track==0? 'DISABLED' : ''}
																				onClick="selectPeopleFunTrackNewCol()" value="2"
																				${not empty trackIds?'checked':''} /> <fmt:message
																					key="col.track.part" bundle="${v3xCommonI18N}" />
																		</label>
																		<input type="text" id="zdgzryName" style="display:none;" name="zdgzryName" size="10" onclick="selectPeopleFunTrackNewCol()"/>
																		</span>
																	</c:if>
																	<%--只有归档--%>

																	<%--封发 并且设置了预归档路径--%>
																	<c:if test="${!v3x:containInCollection(baseActions, 'Track') and v3x:containInCollection(baseActions, 'Archive')}">
																		<%-- 封发 并且设置了预归档路径 --%>
																		<%--
																			OA-49327 公文发起人在已发中归档后，处理节点处理时处理后归档未勾选且置灰了
																			       		处理页面归档显示修改为：已发归档（单位归档）后，后续节点如果有处理后归档权限，
																			       		处理后归档被选中，不可编辑，并且显示归档后的路径。如果是部门归档保留原状态

																			OA-111336	【V5.6SP1_2月修复包】自动化环境：公文处理时进行部门归档，归档后下一节点回退，文档中心归档的文件没有自动删除
																			改动点如下：
																			系统模板公文：
																			1、若系统模板中已有预归档目录，则不允许处理后归档
																			2、若系统模板中未有预归档目录，则可以勾选处理后归档

																			自由流程公文
																			1、不管有没有做预归档，只要有处理后归档权限，都可以选择归档目录。
																			2、封发节点或有预归档目录的，默认勾选
																			3、若不勾选，且有预归档目录，则本次处理后不归档：
																					a若已单位归档，则不作处理；
																					b若未单位归档，预归档不删除，流程结束后自动归档；
																				若勾选并选择目录不变：
																					a若已单位归档，不作处理；
																					b若未单位归档，则归档到选择目录；
																				若勾选并选择目录改变：
																					a若已单位归档，撤销原归档目录，重新归档到选择目录；
																					b若未单位归档，则归到选择目录；
																			4、回退等操作不引起预归档的变化。
																		 --%>

																		<c:set var="isShowPrepigenholePath" value="${summary.archiveId ne null and isPresPigeonholeFolderExsit}" />
																		<c:set var="archiveIdIsNull" value="${summary.archiveId eq null }" />
																		<c:set var="presPigeonholeFolderNotExsit" value="${summary.archiveId ne null and not isPresPigeonholeFolderExsit}" />
																		<c:set var="isShowSelectpigenholePath" value="${archiveIdIsNull or presPigeonholeFolderNotExsit}" />
																		<c:set var="isCheckboxChecked" value="${((nodePermissionPolicyKey eq 'fengfa') || (!hasSysTemplateArchiveId && !archiveIdIsNull))}" />

																		<span id="pipeSpanId" style="display: inline-block; margin-right: 5px; ">
																			<input type="checkbox" class="validationStepback" name="afterSign" id="pipeonhole" value="pipeonhole"
																			onclick="setPipeonholeCheckboxChecked()" style="margin-top: 1px;" ${isCheckboxChecked ? 'checked':''}
																			<c:if test="${hasSysTemplateArchiveId}">disabled</c:if> />
																			<span style="margin-top: 1px;"><fmt:message key="sign.after.pipeonhole.label" /></span> &nbsp;
																		</span>

																		<%-- 设置了预先归档。则直接显示 --%>
																		<c:if test="${hasSysTemplateArchiveId}">
																			<span id="showPrePigeonhole" title="${archiveFullName}"
																				style="display:${isShowPrepigenholePath?'inline-block':'none'};margin-right:8px; width:130px;">
																				<fmt:message key="pigeonhole.label.to" /> :
																				&nbsp;${v3x:getLimitLengthString(archiveFullName,13,"...")}
																			</span>
																		</c:if>

																		<%--没有设置预归档，需要手动选择 --%>
																		<span id="showSelectPigeonholePath" style="${isCheckboxChecked && !hasSysTemplateArchiveId ? '':'display:none;'}margin-right:5px;">
																			<fmt:message key="pigeonhole.label.to" /> : &nbsp;
																			<select id="selectPigeonholePath" class="margin_r_10" style="width: 100px;" onChange="pigeonholeEvent(this,'<%=ApplicationCategoryEnum.edoc.key()%>','finishWorkItem',this.form)">
																				<option id="defaultOption" value="1"><fmt:message key="common.default" bundle="${v3xCommonI18N}" /></option>
																				<option id="modifyOption" value="2"><fmt:message key="click.choice" bundle="${v3xCommonI18N}" /></option>
																				<c:if test="${hasPrePighole}">
																					<option value="3" selected>${archiveName}</option>
																				</c:if>
																			</select>
																		</span>
																	</c:if>
																</td>
															</tr>
															<%--归档 --%>
															<tr>
																<td>
																	<c:if test="${v3x:containInCollection(baseActions, 'Track') and v3x:containInCollection(baseActions, 'Archive')}">
																		<c:set var="isShowPrepigenholePath" value="${summary.archiveId ne null and isPresPigeonholeFolderExsit}" />
																		<c:set var="archiveIdIsNull" value="${summary.archiveId eq null }" />
																		<c:set var="presPigeonholeFolderNotExsit" value="${summary.archiveId ne null and not isPresPigeonholeFolderExsit}" />
																		<c:set var="isShowSelectpigenholePath" value="${archiveIdIsNull or presPigeonholeFolderNotExsit}" />
																		<c:set var="isCheckboxChecked" value="${((nodePermissionPolicyKey eq 'fengfa') || (!hasSysTemplateArchiveId && !archiveIdIsNull))}" />

																		<span id="pipeSpanId" style="display: inline-block; margin-right: 5px; ">
																			<input type="checkbox" class="validationStepback" name="afterSign" id="pipeonhole" value="pipeonhole"
																			onclick="setPipeonholeCheckboxChecked()" style="border:0px;" ${isCheckboxChecked ? 'checked':''}
																			<c:if test="${hasSysTemplateArchiveId}">disabled</c:if> />
																			<span ><fmt:message key="sign.after.pipeonhole.label" /></span> &nbsp;
																		</span>

																		<%--设置了预先归档。则直接显示 --%>
																		<c:if test="${hasSysTemplateArchiveId}">
																		<span id="showPrePigeonhole" title="${archiveFullName}" style="display:${isShowPrepigenholePath?'inline-block':'none'};margin-right:8px;width:130px; ">
																			<fmt:message key="pigeonhole.label.to" /> :
																			&nbsp;${v3x:getLimitLengthString(archiveFullName,13,"...")}
																		</span>
																		</c:if>

																		<%--没有设置预归档，需要手动选择 --%>
																		<span id="showSelectPigeonholePath" style="${isCheckboxChecked && !hasSysTemplateArchiveId ? '':'display:none;'}margin-right:5px;">
																			<fmt:message key="pigeonhole.label.to" /> : &nbsp;
																			<select id="selectPigeonholePath" class="margin_r_10" style="width: 100px;" onChange="pigeonholeEvent(this,'<%=ApplicationCategoryEnum.edoc.key()%>','finishWorkItem',this.form)">
																				<option id="defaultOption" value="1"><fmt:message key="common.default" bundle="${v3xCommonI18N}" /></option>
																				<option id="modifyOption" value="2"><fmt:message key="click.choice" bundle="${v3xCommonI18N}" /></option>
																				<c:if test="${hasPrePighole}">
																					<option value="3" selected>${archiveName}</option>
																				</c:if>
																			</select>
																		</span>
																	</c:if>
																	<%--如果跟踪和归档只设置了一个，则需要补空格保持样式--%>
																	<c:if test="${!v3x:containInCollection(baseActions, 'Track') or !v3x:containInCollection(baseActions, 'Archive')}">
																		&nbsp;
																	</c:if>
																</td>
															</tr>
														</table>
													</td>
													<%--纵向分割线--%>
													<td width="10" valign="center">
														<div style="height: 45px; border-left: solid 1px #d1d1d1;"></div>
													</td>
													<%--常用--%>
													<td class="deal_area">
														<div id="commonActionsDiv">
														</div>

													</td>
													<%--高级按钮--%>
													<td>
														<%-- 高级隐藏区域--%>
														<div class="processAdvanceDIV" style="display:none;position: absolute;   z-index: 2; background-color: #ffffff; border: 1px #C5C5C5 solid; z-index: 11" id="processAdvanceDIV" onMouseOver="advanceViews(true)" onMouseOut="advanceViews(false)" oncontextmenu="return false">
															<%--高级--%>
															<div id="advancedActionsDiv">
															</div>

															<c:if test="${v3x:getBrowserFlagByRequest('OnlyIpad', pageContext.request)}">
																<div style="clear: both; width: 100%; text-align: right; padding: 0 5px 5px 0">
																	<a href="javascript:closeAdvance()">
																		<fmt:message key="common.button.close.label" bundle="${v3xCommonI18N}" />
																	</a>
																</div>
															</c:if>
														</div>
														<%--高级中没有内容时，就不显示了--%>
														<div id='processAdvance' style="float:left;" onClick="advanceViews()"
															class="cursor-hand arrow_2_b">
														</div>
														<div style="border-left: 1px solid #fff; padding-left: 6px;">
															<c:set var="hasAdvanceButton" value="no" />
															<iframe id="processAdvanceDivIframe" scrolling="no" frameborder="0" style="position: absolute; right: 2px; top: 65; width: 100px; height: 10px; z-index: 2; display: none; z-index: 10"> </iframe>
														</div>
													</td>
												</tr>
											</table>
											<%-- 意见区域内容 begin--%>
											<%--意见框 --%>
											<c:if test="${v3x:containInCollection(baseActions, 'Opinion')}">
												<input type="hidden" id="opinionPolicy" name="opinionPolicy" value="${opinionPolicy}" />
                                                <input type="hidden" id="cancelOpinionPolicy" name="cancelOpinionPolicy" value="${cancelOpinionPolicy}" />
                                                <input type="hidden" id="disAgreeOpinionPolicy" name="disAgreeOpinionPolicy" value="${disAgreeOpinionPolicy}" />
												<textarea id="contentOP" name="contentOP" style="display: none;" validate="maxLength" inputName="<fmt:message key='common.opinion.label' bundle='${v3xCommonI18N}' />" maxSize="4000">${(affair.state==3 && (affair.subState==13||affair.subState==6) && tempOpinion.opinionType!=3) && !(empty tempOpinion.content) ? tempOpinion.content : ""}</textarea>
											</c:if>
											<%--态度--%>
											<c:if test="${attitudes != 3}">
												<div id="processAttitude" style="display: none">
													<c:set var="enclude" value="${attitudes==2?'1':'' }" />
													<c:set var="select" value="${attitudes==2?'2':'1' }" />
													<v3x:metadataItem metadata="${colMetadata['collaboration_attitude']}" showType="radio" name="attitude" selected="${tempOpinion == null ? select : tempOpinion.attribute}" enclude="${enclude }" />
												</div>
											</c:if>
											<%--消息推送 --%>
											<div id="pushMessage" style="display: none;">
												<a class="validationStepback" style="margin-right:5px;font-size:12px;display:inline-block;word-break:normal;word-wrap:normal;white-space:nowrap;float:right;" onClick="javascript:parent.showPushWindowEdoc('${param.summaryId}');">
													<span class="ico16 info_16"></span>
													<fmt:message key="edoc.message.push.label" />
												</a>
											</div>
											<%-- 意见区域内容 end--%>
										</div>

										<%-- 按钮隐藏区域，用来为展现准备数据 --%>
										<div style="display:none;">
											<%--回退--%>
											<div id="divReturn">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback1 validationStepback2" id="stepBackSpan" onclick="stepBack(document.theform)">
														<span class="ico16 toback_16 margin_r_5"></span>
														<fmt:message key="menu.edocNew.return" />
													</a>
												</div>
											</div>
											<%--终止--%>
											<div id="divTerminate">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback3" id="stepStopSpan" onclick="stepStop(document.theform)">
														<span class="ico16 termination_16 margin_r_5"></span>
														<fmt:message key="stepStop.label" bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--转收文 --%>
											<div id="divTurnRecEdoc">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback1" id="divTurnRecEdocButton" onclick="TurnRecEdoc()">
														<span class="ico16 modify_text_16"></span>
														<fmt:message key="permission.operation.TurnRecEdoc" />
													</a>
												</div>
											</div>
											<%--加签 --%>
											<div id="divAddNode">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback1" id="divAddNodeButton" onclick="edocInsertPeople('${workitemId}','${processId}','${activityId}','${performer}','${caseId}')">
														<span class="ico16 signature_16"></span>
														<fmt:message key="insertPeople.label" />
													</a>
												</div>
											</div>
											<%--督办设置 --%>
											<div id="divSuperviseSet">
												<div class=" advanceICON validationStepback">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" id="divSuperviseSetButton" onclick="openSuperviseWindow('${param.summaryId}')">
														<span class="ico16 setting_16"></span>
														<fmt:message key="col.supervise.operation.label" bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--修改正文 --%>
											<div id="divEdit">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" id="divEditButton" onclick="updateContent('${param.summaryId}')">
														<span class="ico16 modify_text_16"></span>
														<fmt:message key="editContent.label" />
													</a>
												</div>
											</div>
											<%--  修改附件--%>
											<div id="divAllowUpdateAttachment">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" id="divAllowUpdateAttachmentButton" class="validationStepback" onclick="updateAtt('${param.summaryId}','${summary.processId}')">
														<span class="ico16 editor_16"></span>
														<fmt:message key="edoc.allowUpdateAttachment" bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--减签--%>
											<div id="divRemoveNode">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback1 validationStepback" id="divRemoveNodeButton" onclick="edocDeletePeople('${workitemId}','${processId}','${activityId}','${performer}','${caseId}','${not empty templeteProcessId and templeteProcessId!=0}')">
														<span class="ico16 signafalse_16"></span>
														<fmt:message key="deletePeople.label" />
													</a>
												</div>
											</div>
											<%--转发--%>
											<div id="divForward">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divForwardButton" onclick="transmitSend('${param.summaryId}','${param.affairId}','${summary.edocType}');">
														<span class="ico16 forwarding_16"></span>
														<fmt:message key='edoc.newEdoc.zf'/>
													</a>
												</div>
											</div>
											<%--修改文号--%>
											<div id="divWordNoChange">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divWordNoChangeButton" class="validationStepback" onclick="contentIframe.WordNoChange()">
														<span class="ico16 number_change_16"></span>
														<fmt:message key="edoc.wordNoChange" bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--文单套红--%>
											<div id="divEdocTemplate">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divEdocTemplateButton" class="validationStepback" onclick="edocTaohong('edoc')">
														<span class="ico16 body_red_16"></span>
														<fmt:message key="edoc.action.form.template" />
													</a>
												</div>
											</div>
											<%--回退--%>
											<div id="divReturn">
												<div class=" advanceICON" id="stepBackSpan">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divReturnButton" onclick="stepBack(document.theform)">
													<span class="ico16 toback_16"></span>
													<fmt:message key="stepBack.label" bundle="${colI18N}" /></a>
												</div>
											</div>
											<%--当前会签--%>
											<div id="divJointSign">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback1" id="divJointSignButton"
														onclick="assignNode('${workitemId}','${processId}','${activityId}','${performer}','${caseId}')">
														<span class="ico16 current_countersigned_16"></span>
														<fmt:message
															key="log.edoc.node.joint" bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--知会--%>
											<div id="divInfom">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divInfomButton" class="validationStepback validationStepback1"
														onclick="addInform('${workitemId}','${processId}','${activityId}','${performer}','${caseId}');">
														<span class="ico16 notification_16"></span> <fmt:message
															key="addInform.label" bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--多级会签 --%>
											<div id="divMoreSign">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divMoreSignButton" class="validationStepback validationStepback1"
														onclick="multistageSign('${param.summaryId}','${summary.processId}','${param.affairId}');">
														<span class="ico16 multilevel_sign_16"></span> <fmt:message
															key="edoc.metadata_item.moreSign"
															bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%--撤销--%>
											<div id="divCancel">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divCancelButton"
														onclick="repealItem('pending','${param.summaryId}','${param.affairId }')">
														<span class="ico16 revoked_process_16"></span> <fmt:message
															key="edoc.repeal.2.label" />
													</a>
												</div>
											</div>
											<%-- 修改文单--%>
											<div id="divUpdateForm">
												<div class=" advanceICON">
													<a id="divUpdateFormButton" class="validationStepback validationStepback2"
														onclick="contentIframe.UpdateEdocForm('${param.summaryId}')">
															<span class="dealicons updateform"></span>
															<fmt:message key="node.policy.updateform.label" />
													</a>
												</div>
											</div>
											<%--指定回退--%>
											<div id="divSpecifiesReturn">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback2"
														id = "spanSpecifiesReturn"
														onclick="stepBackToTargetNode1(window.top,window.top,'${affair.subObjectId}','${summary.processId }','${summary.caseId }','${activityId}','${show1}','${show2}','${templateFlag}');"
														title="<fmt:message key="permission.operation.SpecifiesReturn" />"><span class="ico16 specify_fallback_16"></span> <fmt:message
															key="permission.operation.SpecifiesReturn" />
													</a>
												</div>
											</div>
											<%--签章--%>
											<div id="divSign">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divSignButton" class="validationStepback"
														onclick="openSignature()"
														title="<fmt:message key="comm.sign.introduce.label" bundle="${v3xCommonI18N}"/>">
														<span class="ico16 signa_16"></span> <fmt:message
															key="node.policy.Sign.label"
															bundle="${v3xCommonI18N}" />
													</a>
												</div>
											</div>
											<%--部门归档--%>
											<div id="divDepartPigeonhole">
												<div class=" advanceICON validationStepback">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" id="divDepartPigeonholeButton"
														onclick="DepartPigeonhole(<%=ApplicationCategoryEnum.edoc.getKey()%>,'${param.affairId}')">
														<span class="ico16 filing_16"></span> <fmt:message
															key="edoc.action.DepartPigeonhole.label" />
													</a>
												</div>
											</div>
											<%--指定回退--%>
											<div id="divScriptTemplate">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" id="divScriptTemplateButton"
														onclick="edocTaohong('script')"> <span
														class="ico16 wen_single_red_16"></span> <fmt:message
															key="edoc.action.script.template" />
													</a>
												</div>
											</div>
											<%--传阅--%>
											<div id="divPassRead">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback1" id="divPassReadButton"
														onclick="passRead();"> <span
														class="ico16 circulated_16"></span> <fmt:message
															key="node.policy.chuanyue" bundle="${v3xCommonI18N}" /></a>
												</div>
											</div>
											<%--文单签批--%>
											<div id="divHtmlSign">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" onclick="htmlSign();" id="divHtmlSignButton"
														title="<fmt:message key="edoc.advanceOffice.htmlSign.label"/>">
														<span
														class="dealicons-advance ico16 wen_shan_issuing_16"></span><fmt:message key="edoc.action.htmlSign.label" />
													</a>
												</div>
											</div>
											<%--转公告--%>
											<div id="divTransmitBulletin">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a onclick="TransmitToBulletin();" id="divTransmitBulletinButton"> <span
														class="ico16 transfer_bulletin_16"></span> <fmt:message
															key="edoc.metadata_item.TransmitBulletin"
															bundle="${colI18N}" /></a>
												</div>
											</div>
											<%--Word转PDF--%>
											<div id="divTanstoPDF">
												<div class="advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback" id="divTanstoPDFButton"
														onclick="convertToPdf(this.form);"> <span
														class="dealicons-advance ico16 word_to_pdf_16"></span><fmt:message key="edoc.metadata_item.TanstoPdf"
															bundle="${colI18N}" />
													</a>
												</div>
											</div>
											<%-- 转事件 --%>
											<div id="divTransform">
												<div class="advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a id="divTransformButton" onclick="AddCalEvent('','${param.affairId}','${appTypeId}','','${v3x:escapeJavascript(summary.subject)}','','','','','${pageContext.request.contextPath}');">
														<span class="dealicons-advance ico16 forward_event_16"></span><fmt:message key="edoc.metadata_item.zsj" />
													</a>
												</div>
											</div>
											<%--移交--%>
											<div id="divTransfer">
												<div class=" advanceICON">
													<c:set var="hasAdvanceButton" value="yes" />
													<a class="validationStepback validationStepback1" id="divTransferButton" onclick="transfer();">
														<span class="ico16 transfer_16"></span>
														<fmt:message key='collaboration.dealAttitude.transfer'/>
													</a>
												</div>
											</div>
										</div>
										<%-- 按钮隐藏区域************ --%>

										<%--意见附件和关联文档--%>
										<c:set var="canUploadRel" value="${v3x:containInCollection(baseActions, 'UploadRelDoc')}" />
										<c:set var="canUploadAttachment" value="${v3x:containInCollection(baseActions, 'UploadAttachment')}" />
										<div id="attachmentEditInputs"></div>
										<div id="attachmentInputs"></div>
										<div id="contentIframeAttachmentInputs"></div>
										<div id="processatt1" style="display: none">
											<div id="attachment2Area"></div>
											<v3x:fileUpload attachments="${attachmentsOpinion}" canDeleteOriginalAtts="true" originalAttsNeedClone="false" />
										</div>
									</td>
									<td></td>
									<%--节点权限 --%>
									<td align="right">
										<%-- GOV-4701 打开待办公文时要像协同一样显示当前的节点权限，现在没有显示 --%>
										<fmt:message key='edoc.form.flowperm.name.label' />：${nodePermissionPolicyName}
										<c:choose>
											<c:when test="${!empty summary.templeteId}">
												<%--OA-32768 test01调用公文模板发文后，处理时没有查看处理说明的入口 --%>
												<a onclick="edocShowNodeExplain()">
													<span class="ico16 handling_of_16"></span>
												</a>
											</c:when>
											<c:otherwise>
                                                &nbsp;&nbsp;&nbsp;&nbsp;
                                            </c:otherwise>
										</c:choose>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</c:if>
				<%--公文附件和关联文档 --%>
				<tr id="attachment2Tr" style="display: none">
					<td height="16px" class="detail-summary">
						<table border="0" cellpadding="0" cellspacing="0" width="100%"
							height="100%" align="left">
							<tr id="attachment2TrContent" style="display: none;">
								<td height="18" width="70" nowrap class="bg-gray detail-subject"
									style="margin-left:5px;align:left;width:100px;">
									<span>
										<fmt:message
											key="common.toolbar.insert.mydocument.label"
											bundle="${v3xCommonI18N}" />
									</span>
									 :
									<span class="font-12px" style="margin-top: 8px;">
										(<span id="attachment2NumberDivContent" class="font-12px"></span>)
									</span>
								</td>
								<td valign="middle">
									<div class="div-float" id=att2Div style="padding-top: 5px;">
										<span id="attachmentHtml2Span"> </span>
									</div>
								</td>
							</tr>
							<tr id="attachmentTrContent"
								style="display: ${canEditAtt?'':'none' };padding-buttom:5px;">
								<td height="18" nowrap class="bg-gray detail-subject"
									style="margin-left:5px;align:left;width:100px;">
									<%--如果有权限修改就显示“插入附件”按钮，没有权限就显示"附件"--%> <c:if
										test="${canEditAtt }">
										<span id="uploadAttachmentTR">
										<a onclick="senderEditAtt()"><fmt:message
													key="common.toolbar.updateAttachment.label"
													bundle="${v3xCommonI18N}" /></a></span>
									</c:if> <c:if test="${!canEditAtt }">
										<span id="normalText"><fmt:message
												key="common.attachment.label" bundle="${v3xCommonI18N}" /></span>
									</c:if> :
									<span class="font-12px" style="padding-top: 8px;">
										(<span id="attachmentNumberDivContent" class="font-12px">0</span>)
									</span>
								</td>
								<td>
									<div class=" div-float" id="attDiv"
										style="width: 100%; padding-top: 5px;">
										<span id="attachmentHtml1Span"> <script
												type="text/javascript">
									<!--
									<c:if test="${canEditAtt }">
									document.getElementById("attachmentTrContent").style.display='';
									</c:if>
									-->
									</script>
										</span>

									</div>
								</td>
							</tr>
						</table>
					</td>
				</tr>


				<tr>
					<td height="35" class="top_div_row3_deal_bg deal_toobar_m"
						colspan="2">
						<table cellpadding="0" cellspacing="0" width="100%" height="100%">
							<tr>
								<!-- <td width="16" class="top_div_row3_deal_l">&nbsp;</td> -->
								<td valign="top">

									<table cellpadding="0" cellspacing="0" width="98%" height="35"
										style="margin-left: 10px; margin-right: 10px;">
										<tr>
											<td valign="middle" nowrap="nowrap" class=""
												colspan="${'Pending' eq param.from ?'1':'2'}"><c:if
													test="${!onlySeeContent}">
													<input id="edocform_btn" class="deal_btn_l_sel"
														onclick="showPrecessAreaTd('edocform')" type="button"
														value="<fmt:message key='edocform.label'/>" />
												</c:if> <input id="content_btn" class="deal_btn_m"
												onclick="showPrecessAreaTd('content')" type="button"
												value="<fmt:message key='common.toolbar.content.label'  bundle='${v3xCommonI18N}'/>" />

												<c:if test="${hasBody1}">
													<input id="content1_btn" class="deal_btn_m"
														onclick="showPrecessAreaTd('content1')" type="button"
														value="<fmt:message key='edoc.contentnum1.label' />" />
												</c:if> <c:if test="${hasBody2}">
													<input id="content2_btn" class="deal_btn_m"
														onclick="showPrecessAreaTd('content2')" type="button"
														value="<fmt:message key='edoc.contentnum2.label' />" />
												</c:if> <c:if
													test="${(!summary.isQuickSend) && onlySeeContent=='false'}">
													<!-- 项目：徐矿集团   【屏蔽公文详情中的流程按钮】 作者：jiangchenxi 时间：2019年3月11日 start  -->
													<c:if test="${open==true }">
														<input id="workflow_btn" class="deal_btn_r"
														onclick="showPrecessAreaTd('workflow')" type="button"
														value="<fmt:message key='workflow.label'/>" />
													</c:if>
													<!-- 源码 -->
													<%-- <input id="workflow_btn" class="deal_btn_r"
														onclick="showPrecessAreaTd('workflow')" type="button"
														value="<fmt:message key='workflow.label'/>" /> --%>
													<!-- 项目：徐矿集团   【屏蔽公文详情中的流程按钮】 作者：jiangchenxi 时间：2019年3月11日 end  -->
												</c:if></td>

											<td align="right" nowrap="nowrap">
												<div align="right">

													<span style="display: block;" class="div-float-right">

														<c:if test="${param.from ne 'Pending'}">
															<input type="hidden" name="currContentNum"
																id="currContentNum" value="0" />
														</c:if>
														<%--<a href="#" onclick="opentPdf('', '/seeyon/edocController.do?method=pdfView&amp;id=${param.summaryId}&amp;bodyContent=${summary.firstBody.content}');">
		                           		<i class="icon-check-c"></i><font style="font-weight:bold;color:#000000">签批查看&nbsp;</font></a>--%>
														<%--查看PDF文档--%>
														<%--                            zhou:查看文单--%>
														<c:if test="${summary.formId==5212468394961879816 && summary.state == 3}">
															<span  onclick="opentPdf('', '/seeyon/ext/edocDetail.do?method=index&id=${param.summaryId}&bodyContent=${summary.firstBody.content}');" class="cursor-hand" id='viewEdocDetail'	title="查看文单">
																<span class="ico16 print_16 margin_lr_5"></span> 查看文单<c:out value="${}"/>
															</span>
														</c:if>
														<%--                            zhou:查看文单--%>

															<c:if test="${!empty firstPDFId}">
																<span id="pdfBrowser" class="cursor-hand" title="pdf"
																	onclick='_pdfFullSize();'> <span
																	class="ico16 pdf_16 margin_lr_5"></span> pdf
																</span>
															</c:if>
														 <c:if
															test="${isSupervis == true && from == 'supervise'}">
															<fmt:message key='edoc.superviseTitle.label' bundle='${colI18N}'
																var="superviseLabel" />
															<span class="cursor-hand" title="${superviseLabel }"
																onClick="showSupervise('${param.summaryId}');"> <span
																class="ico16 view_unhandled_16 margin_lr_5"></span>
																${superviseLabel }
															</span>
														</c:if>

														<c:if
															test="${printEdocTable }">
															<span class="cursor-hand" id='edoc_print'
																title="${printLabel }" onclick='colPrint()'> <span
																class="ico16 print_16 margin_lr_5"></span> ${printLabel }
															</span>
														</c:if>

															<span class="cursor-hand" id='edoc_attachment'
																title="${ctp:i18n('collaboration.summary.findAttachmentList')}" onclick='showOrCloseAttachmentList(true)'> <span
																class="ico16 affix_16 margin_lr_5"></span> ${ctp:i18n('collaboration.common.flag.attachmentList')}
															</span>
														 <fmt:message key="newflow.viewDetailandDaily"
															bundle='${colI18N}' var="viewDetailandDailyLabel" /> <c:if
															test="${!onlySeeContent}">
															<!-- 项目：徐矿集团   【屏蔽公文、签报详情中的明细日志按钮】 作者：jiangchenxi 时间：2019年3月11日 start -->
<%--														zhou :明细日志--%>
<%--															<c:if test="${open==true }">--%>
<%--																<span class="cursor-hand" id="mxrz1" title="${ctp:i18n('edoc.detail.log')}"--%>
<%--																	onclick="showDetailAndLog('${param.summaryId}','${summary.processId}','','${appTypeName}',logTitle,'fromSummary')">--%>
<%--																	<span class="ico16 view_log_16 margin_lr_5"></span> <fmt:message--%>
<%--																		key='edoc.detail.log' />--%>
<%--																</span>--%>
<%--															</c:if>--%>
															<!-- 源码 -->
															<%-- <span class="cursor-hand" id="mxrz1" title="${ctp:i18n('edoc.detail.log')}"
																onclick="showDetailAndLog('${param.summaryId}','${summary.processId}','','${appTypeName}',logTitle,'fromSummary')">
																<span class="ico16 view_log_16 margin_lr_5"></span> <fmt:message
																	key='edoc.detail.log' />
															</span> --%>
															<!-- 项目：徐矿集团   【屏蔽公文、签报详情中的明细日志按钮】 作者：jiangchenxi 时间：2019年3月11日 end -->
															<fmt:message key="newflow.viewPropertyState"
																bundle="${colI18N}" var="viewPropertyStateLabel" />
															<span class="cursor-hand" id="sxzt1" title="${ctp:i18n('edoc.Attribute.state')}"
																onclick="showAttribute('${param.affairId}', '${param.from}')">
																<span class="ico16 attribute_16 margin_lr_5"></span> <fmt:message
																	key='edoc.Attribute.state' />
															</span>
														</c:if>
														<c:if
															test="${param_from == 'true' && !summary.finished && openFrom !='sendRegisterResult' && openFrom !='recRegisterResult'}">
															<fmt:message key='common.toolbar.supervise.label' bundle='${v3xCommonI18N}' var="sendedSuperviseLabel" />
															<span class="cursor-hand"
																title="${sendedSuperviseLabel }"
																onclick='showSuperviseWindow()'> <span
																class="ico16 setting_16 margin_lr_5"></span>
																${sendedSuperviseLabel }
															</span>
														</c:if> <c:if test="${hasWorkflowDataaButton}">
															<fmt:message key="edoc.metadata_item.zslc"
																var="zslcLabel" />
															<span class="cursor-hand" title="${zslcLabel}"
																id="wflczs1" onclick="showOrCloseWorkflowTrace_edoc()">
																<span class="ico16 review_flow_16 margin_lr_5"></span> <fmt:message
																	key="edoc.metadata_item.zslc" />
															</span>
														</c:if> <c:if test="${isCanViewTurnRecEdoc == 'true' }">
															<span class="cursor-hand" title=""
																onclick='showTurnRecInfo();'> <span
																class="ico16 setting_16 margin_lr_5"></span> <fmt:message
																	key="edoc.turn.rec.info" bundle="${exchangeI18N}" />
															</span>
														</c:if>
														<c:if test="${hasMeetingPlugin == 'true'}">
														 	<span id="caozuo_more_btn" class="ico16 arrow_2_b left margin_l_5"></span>
														 	<div id="caozuo_moreDiv" class="menu_simple" style="display:none;position:absolute;top:90px;right:20px;z-index:201; height:24px;border:1px solid black;text-align:center;padding-top:2px;overflow:hidden;text-overflow:ellipsis;">
																<span class="cursor-hand" id="createMeetingFlag" title="${ctp:i18n('collaboration.summary.createMeeting')}"
																	onclick="createMeeting('${param.affairId}','${param.from}')">
																	<span class="ico16 margin_lr_5"></span> ${ctp:i18n('collaboration.summary.createMeeting')}
																</span>
														 	</div>
														 </c:if>
													</span>

												</div>
											</td>
										</tr>
									</table>

								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td colspan="2" valign="top" id="hhhhhhh">
						<table width="100%" id="signAreaTable" style="border: none;"
							height="100%" border="0" cellspacing="0" cellpadding="0">
							<tr id="closeTR" style="display: none" valign="top">
								<td colspan="3">&nbsp;</td>
							</tr>
							<tr id="workflowTR" style="display: none" valign="top">
								<td colspan="3" style="width: 100%; height: 100%;" valign="top">
									<c:choose>
										<c:when
											test="${isSupervis == true && from == 'supervise' && finished!=true}">
											<input type="button"
												onclick="showDigarm('${bean.id}','fromSupervis');"
												value="<fmt:message key="edoc.edit.workflow.label" bundle="${colI18N}"/>" />
											<iframe name="monitorFrame" id="monitorFrame"
												frameborder="0" marginheight="0" marginwidth="0"
												height="100%" width="100%" scrolling="auto"></iframe>
										</c:when>
										<c:otherwise>
											<iframe name="monitorFrame" id="monitorFrame"
												frameborder="0" marginheight="0" marginwidth="0"
												height="100%" width="100%" scrolling="auto"></iframe>
										</c:otherwise>
									</c:choose>
								</td>
							</tr>
							<tr id="edocformTR">
								<td colspan="3">
									<iframe
										src="${detailURL}?method=getContent&summaryId=${summary.id}&affairId=${param.affairId}&from=${ctp:toHTML(param.from)}&openFrom=${ctp:toHTML(openFrom)}&lenPotent=${ctp:toHTML(lenPotent)}&docId=${ctp:toHTML(docId)}&docResId=${ctp:toHTML(param.docResId)}&canUploadRel=${canUploadRel}&canUploadAttachment=${canUploadAttachment}&position=${showOpinionButton?position:''}&firstPDFId=${firstPDFId}&optionType=${optionType}&recType=${param.recType == null || param.recType == '' ? ctp:toHTML(recType) : ctp:toHTML(param.recType)}&relSends=${param.relSends == null || param.relSends == '' ? ctp:toHTML(relSends) : ctp:toHTML(param.relSends)}&relRecs=${param.relRecs == null || param.relRecs == '' ? ctp:toHTML(relRecs) : ctp:toHTML(param.relRecs)}&sendSummaryId=${ctp:toHTML(param.sendSummaryId)}&recEdocId=${param.recEdocId == null || param.recEdocId == '' ? ctp:toHTML(recEdocId) : ctp:toHTML(param.recEdocId)}&forwardType=${ctp:toHTML(param.forwardType)}&archiveModifyId=${param.archiveModifyId}&openEdocByForward=${ctp:toHTML(param.openEdocByForward)}&isTransFrom=${ctp:toHTML(isTransFrom)}${ctp:csrfSuffix()}"
										width="100%" height="100%" id="contentIframe"
										name="contentIframe" frameborder="0" scrolling="yes"
										marginheight="0" marginwidth="0"></iframe>

									<input
									type="hidden" name="sattitude" id="sattitude">
								</td>
							</tr>
							<tr id="contentTR" style="display: none;">
								<td colspan="3" valign="top" align="center" id="scrollContentTd">
									<!--div style="overflow:auto;">&nbsp;</div--> <iframe
										width="100%" height="100%" name="htmlContentIframe"
										id="htmlContentIframe" frameborder="0" scrolling="yes"
										marginheight="0" marginwidth="0"></iframe>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</form>

		<div id="formContainer" style="display: none"></div>
		<iframe name="showDiagramFrame" frameborder="0" height="0" width="0"
			scrolling="no" marginheight="0" marginwidth="0"></iframe>
		<iframe  frameborder="0" style="display:none;position:absolute;top:90px;right:20px;width:650px;z-index:200;height: 180px" id="attachmentList" class="over_auto align_right" src="" >
                        			</iframe>
	</div>

	<div style="width:100%;height:0px;overflow:hidden" id="ctn">
		<c:if test="${summary.edocType==0 && v3x:hasPlugin('barCode')}">
		<v3x:webBarCode  writerId="PDF417Manager"/>
		</c:if>
		<div name="edocContentDiv" id="edocContentDiv" style="width:2px;height:2px;overflow:hidden">
			<v3x:showContent  htmlId="edoc-contentText" content="${summary.firstBody.content}" type="${summary.firstBody.contentType}" createDate="${summary.firstBody.createTime}" contentName="${summary.firstBody.contentName}" loadOfficeImmediate="false" viewMode ="edit"/>
		</div>
		<input type="hidden" id="bodyContentId" name="bodyContentId" value="${summary.firstBody.id}">
		<script>
		if(typeof(officeParams)!="undefined"){
			officeParams.hasPDF417Manager = "${summary.edocType==0 && v3x:hasPlugin('barCode')}";
			officeParams.canWrite = "true";
			officeParams.officecanSaveLocal = contentSaveLocal;
			officeParams.canEdit = canEdit;
			officeParams.currentUserName = currentUserName;
			officeParams.setSignatureBlack = "${isDeptPigeonhole}";
			officeParams.officecanPrint = contentPrint;
			officeParams.showHl= showHl;
			officeParams.actionCanSign = hasSign;
		}

		function opentPdf(object,url) {
			var _url =  url;
			var rv = v3x.openWindow({
				url: _url,
				dialogType:'open',
				workSpace: 'yes'
			});
		}
		</script>
	</div>
</body>
</html>
