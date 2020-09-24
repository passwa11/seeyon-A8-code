<%@ page isELIgnored="false" language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.seeyon.ctp.common.constants.ApplicationCategoryEnum"%>
<%@page import="com.seeyon.ctp.common.constants.Constants"%>
<%@page import="com.seeyon.v3x.edoc.manager.EdocSwitchHelper"%>
<html xmlns="http://www.w3.org/1999/xhtml" style="overflow: hidden;">
<head>
<%@ include file="../common/INC/noCache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="edocHeader.jsp"%>

<title><fmt:message key='${newEdoclabel}' /></title>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery-ui.custom.min.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.plugin.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery-ui.custom.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/edoc/js/leave.js${v3x:resSuffix()}" />"></script>
<c:if test="${hwjs ne null}">
	<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/office/js/hw.js${v3x:resSuffix()}" />"></script>
</c:if>

<c:set value="${pageContext.request.contextPath}" var="path" />
<%--以下两个时间用于调用模板时，使用模板的流程期限 --%>
<c:set
	value="${(summaryFromTemplate.deadline == null || summaryFromTemplate.deadline <= 0) ? (formModel.deadline) : (summaryFromTemplate.deadline)}"
	var="finalDeadline" />
<c:set
	value="${(summaryFromTemplate.deadline > 0 && summaryFromTemplate.updateTime != null) ? (summaryFromTemplate.updateTime) : (formModel.edocSummary.createTime)}"
	var="finalCreateTime" />

<link rel="stylesheet" href="${path}/common/all-min.css" />
<%@ include file="/WEB-INF/jsp/ctp/portal/common/components_theme.jsp"%>
<link rel="stylesheet" type="text/css"
	href="<c:url value="/apps_res/edoc/css/edocDisplay.css${v3x:resSuffix()}" />">
<c:if test="${param.barCode =='true'}">
	<script type="text/javascript" charset="UTF-8"
		src="<c:url value="/common/barCode/js/barCode.js" />"></script>
	<c:set var="hasBarCode" value="true" />
</c:if>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/edoc/js/newEdoc.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript">
//客开：徐矿集团【是否添加单选按钮，且能否点击】 chenqiang 2019年3月27日  start
var xkjtAttachmentHasRadio = '${xkjtAttachmentHasRadio}';
var canClick = '${canClick}';
function getXkjtTitleAttachmentTitle(obj){
	var title = obj.title;
	document.getElementById("my:subject").value = title;
}
//客开：徐矿集团【是否添加单选按钮，且能否点击】 chenqiang 2019年3月27日  end
var selfCreateFlow = ${selfCreateFlow};

var processDefaultTitle = selfCreateFlow ? "<fmt:message key='default.workflowInfo.value'/>":"<fmt:message key='alert_notcreateflow_loadtemplate'/>" 

var hasPluginDoc = "${ctp:hasPlugin('doc')}";
	
var checkPDFIsNull=false; //PDF正文非空验证标记
//表单签章相关,hw.js中需要用到
var hwVer = '<%=DBstep.iMsgServer2000.Version("iWebSignature")%>';
var mids="${trackIds}"; 
var theform = document.getElementById("sendForm");
var registerType = "${ctp:escapeJavascript(param.registerType)}";
var agentToId = "${agentToId}";
var agentToName = "${agentToName}";
var agentToAccountShortName = "${agentToAccountShortName}";
//服务器时间和本地时间的差异
var server2LocalTime = <%=System.currentTimeMillis()%> - new Date().getTime();
try{
parent.treeFrame.changeTreeToCreate();
}catch(e){}

if(typeof(isLeave)!=undefined) {
    isLeave = ${param.backBoxToEdit=='true'} ? true : false;
}
//给精灵弹出的窗口设置title参数。
if(typeof(parent.document.title)!="undefined")parent.document.title="<fmt:message key='${newEdoclabel}'/>";
var isNewColl = true ;
var isBoundSerialNo = "${isBoundSerialNo}";
var logoURL = "${logoURL}";
var processing=false;
var currentPage="newEdoc";//公文督办的时候，如果是新页面就不提交，否则提交到服务器。
// 快速需求(FastDemand):发文、签报支持PDF正文
var supportPdfMenu=true;
// 设置收文登记页面支持PDF正文。
// var supportPdfMenu=('${appType}'=='20'?true:false);

var hasDiagram = <c:out value="${hasWorkflow}" default="false" />;        
var caseProcessXML = '${process_xml}';
var caseLogXML = "";
var caseWorkItemLogXML = "";
var currentNodeId = null;
var showHastenButton = "";
var appName="${appName}";
var isTemplete = false;
var hasDoc = "${hasDoc}";
var templeteCategrory="${templeteCategrory}";
var defaultPermName="<fmt:message key='${defaultPermLabel}' bundle='${v3xCommonI18N}'/>";
//原始正文的ID，即联合发文的时候原始正文的ID.如果拟文的时候，联合发文需要套红的话就要使用此变量来调出服务器端保存的原始正文。
var oFileId=""; 
var draftTaoHong="";
var noDepManager = "${noDepManager}";
var isOnlySender = "${isOnlySender}";
if(isOnlySender != "true")
{
if(noDepManager == "true"){
    alert(_("edocLang.edoc_supervise_nodepartmentManager"));
}
}


var policys = null;
var nodes = null;

var unallowedSelectEmptyGroup_wf = true;

var isFromTemplate = <c:out value="${isFromTemplate}" default="false" />;


var templateType="${templateType}";
var showMode = 1;
showMode = ((isFromTemplate && templateType !='text') || selfCreateFlow==false) ? 0 : showMode;
var hiddenColAssignRadio_wf = true;
var editWorkFlowFlag = "true";
var actorId="${actorId}";
var currentUserId = "${currentUserId}";
var currentUserName = "${ctp:escapeJavascript(currentUserName)}";
var currentUserAccountId = "${currentUserAccountId}";
var currentUserAccountName = "${currentUserAccountName}";
var recPostAccountId = "${recPostAccountId}";
var recPostAccountName = "${recPostAccountName}";
var templeteProcessId ="${templeteProcessId}";
var jsEdocType=${formModel.edocSummary.edocType};
var edocType = '${formModel.edocSummary.edocType}';
hasWorkflow = <c:out value='${hasWorkflow}' default='false' />;

var selectedElements = null;
var _canUpdateContent=${canUpdateContent};
var taohongSendUnitType = 1;
var taohongFileUrl="";
var customSetTrackFlag = '${customSetTrack}' == 'true';
var fromTemplateFlag ='${fromTemplateFlag}' =='true';
var templatSetTrackNot ='${templatSetTrackNot}' =='true';
var personalTemplateFlag ="${personalTemplateFlag}" == "1";
//对登记时正文本地保存的权限进行控制:有公文开关<外来公文登记是否允许修改>的权限 = 有本地保存的权限。
var officecanSaveLocal="${canUpdateContent}";
//OA-35531,归档后修改，不允许下载到本地
if("${param.from}" == "archived"){
  officecanSaveLocal = "false";
}
formOperation = "aa";
${opinionsJs}
//维持一个Map,保存正文的ID和正文编号（0：原始正文 1：套红1 2：套红2）
${contentRecordId}
//
${docMarkByTemplateJs}

//分支
var branchs = new Array();
var keys = new Array();
var team = new Array();
var secondpost = new Array();
<c:if test="${branchs != null}">
    var handworkCondition = _('edocLang.handworkCondition');
    <c:forEach items="${branchs}" var="branch" varStatus="status">
        var branch = new ColBranch();
        branch.id = ${branch.id};
        branch.conditionType = "${branch.conditionType}";
        branch.formCondition = "${v3x:escapeJavascript(branch.formCondition)}";
        branch.conditionTitle = "${v3x:escapeJavascript(branch.conditionTitle)}";
        //if(branch.conditionType!=2)
            branch.conditionDesc = "${v3x:escapeJavascript(branch.conditionDesc)}";
        /*else
            branch.conditionDesc = handworkCondition;*/
        branch.isForce = "${branch.isForce}";
        eval("branchs["+${branch.linkId}+"]=branch");
        keys[${status.index}] = ${branch.linkId};
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
//分枝 结束
var summaryId = "${formModel.edocSummaryId}";
var sendEdocId = "${ctp:escapeJavascript(param.edocId)}";
//从待登记列表打开的时候得使用这种方式赋值
if(!sendEdocId){
	sendEdocId = "${ctp:escapeJavascript(edocId)}";
}
var waitSendFlag = "${waitSendFlag}" == "true";
var affairTrackType = "${affair.track}";
var isAllowContainsChildDept_ExchangeUnit = true;
var hasTemplate = "${hasTemplate}";
var archivedModify= "${archivedModify}";
var notOpenSave = "${param.notOpenSave != 'true'}";
var edocType ="${formModel.edocSummary.edocType}";
var isQuickSend = "${formModel.edocSummary.isQuickSend}";
var edocSummaryQuick = "${edocSummaryQuick}";
var taohongTemplateUrl = "${ctp:escapeJavascript(edocSummaryQuick.taohongTemplateUrl)}";
var list = "${ctp:escapeJavascript(deptSenderList)}";
var createrExchangeDepts = "${ctp:escapeJavascript(createrExchangeDepts)}";
var recSummaryId = "${recSummaryId}";
var formDisableWarning = "${formDisableWarning}";
var comm = "${ctp:escapeJavascript(comm)}";
var isA8PaperRegister = "${isA8PaperRegister}";
var recEdocId = "${recEdocId}";
var recType = "${recType}";
var forwardType = "${forwardType}";
var relationUrl = "${relationUrl}";
var param_newContactReceive = "${ctp:escapeJavascript(param.newContactReceive)}";
var _newContactReceive = "${ctp:escapeJavascript(newContactReceive)}";
var relationRecd= "${relationRecd}";
var relationSend= "${relationSend}";
var receiveEdocIdFromTemplate="${receiveEdocIdFromTemplate}";
var relationRecId = "${ctp:escapeJavascript(param.edocId)}";
var _relationRecId = "${relationRecId}";
var param_edocType = "${param.edocType}";
var templete_id = "${templete.id}";
var getSystemProperty = "${ctp:getSystemProperty('edoc.isG6')}";
var finalDeadline = "${finalDeadline}";
var agentToId_ctp_OrgAccountName = "${ctp:showOrgAccountNameByMemberid(agentToId)}";
var subState = "${subState}";
var isRepealTemplate = "${isRepealTemplate}";
var caseId = "${caseId}";
var processId = "${processId}";
var isRepealFree = "${isRepealFree}";
var supEdocId = "${supEdocId}";
var appType = "${appType}";
var waitRegister_recieveId = "${waitRegister_recieveId}";
var edoc_resourse_notExist = "<fmt:message key='edoc.resourse.notExist'/>";
var edoc_flowtime_validate = "<fmt:message key='edoc.flowtime.validate'/>";
var edoc_sendQuick_0_label = "<fmt:message key='edoc.sendQuick_0.label'/>";
var edoc_sendQuick_1_label = "<fmt:message key='edoc.sendQuick_1.label'/>";
var edoc_element_shenpi = "<fmt:message key='edoc.element.shenpi'/>";
var edoc_element_yuedu = "<fmt:message key='edoc.element.yuedu'/>";
var edoc_Cancel_exchange_privileges1 = "<fmt:message key='edoc.Cancel.exchange.privileges1' />";
var edoc_Cancel_exchange_privileges2 = "<fmt:message key='edoc.Cancel.exchange.privileges2'/>";
var edoc_turn_rec_info = "<fmt:message key='edoc.turn.rec.info' bundle='${exchangeI18N}'/>";
var _trackTitle = "${ctp:i18n('collaboration.newColl.alert.zdgzrNotNull')}";
var isTrackId="${empty affair ? 0 : affair.track}";
var ApplicationCategoryEnumEdoc = '<%=ApplicationCategoryEnum.edoc.key()%>';
//加载正文类型-书生
var bodyType="${formModel.edocBody.contentType}";
window.onload=init_newedoc;

var defaultNodeName = "${defaultNodeName}";
var defaultNodeLable = "${defaultNodeLable}";
var subState = "${subState}";
</script>
${hwjs}
</head>
<body style="overflow: hidden;" class="h100b page_color"
	onUnload="unload('${formModel.edocSummaryId}');/*TODO changyi 离开当前页面  myOnUnload();*/"
	onbeforeunload="return onbeforeunloadEdoc()">

<div class="newDiv">
<form name="sendForm" id="sendForm" method="post">
   
    <%--流程是不是一发出去就结束，工作流预提交会自动给这个字段赋值 --%>
    <input type="hidden" name="workflow_last_input" id="workflow_last_input" value="false"/>
    <%--发转发时，设置的收文的affairId --%>
	<input type="hidden" name="sendEdocId" id="sendEdocId" value ="${ctp:toHTML(edocId)}"/>
	<input type="hidden" name="taohongriqiSwitch" id="taohongriqiSwitch" value ="${taohongriqiSwitch}"><%--正文套红签发日期显示开关 --%>
    <input id="forwordtosend_recAffairId" name="forwordtosend_recAffairId" type="hidden" value="${forwordtosend_recAffairId }" /> <%--转发时 设置forwardMember，发送后需要保存到affair中 --%>
    <input id="forwardMember" name="forwardMember" type="hidden" value="${forwardMember }" /> <%-- 从待登记 直接到收文分发 记录下签收id 还可能调用模板，在controller中将签收id保存到request中，所以这里的签收id的值可能从两种方式获得--%> 
    <input id="waitRegister_recieveId" name="waitRegister_recieveId" type="hidden" value="${!empty ctp:escapeJavascript(param.recieveId) ? ctp:escapeJavascript(param.recieveId) : ctp:escapeJavascript(waitRegister_recieveId)}" />

	<%-- 代理相关参数 --%>
	<input id="isAgent" name="isAgent" type="hidden" value="${isAgent}"/>
	<input id="agentToId" name="agentToId" type="hidden" value="${agentToId}"/>
	<input id="agentToName" name="agentToName" type="hidden" value="${agentToName}"/>
	
	<input id="process_desc_by" name="process_desc_by" type="hidden" /> 
	<input id="process_xml" name="process_xml" type="hidden" value="" /> 
	<input id="process_rulecontent" name="process_rulecontent" type="hidden" /> 
	<input id="readyObjectJSON" name="readyObjectJSON" type="hidden" /> 
	<input id="process_subsetting" name="process_subsetting" type="hidden" />
	<input id="moduleType" name="moduleType" type="hidden" />
	<input id="workflow_newflow_input" name="workflow_newflow_input" type="hidden" />
    <input id="workflow_node_peoples_input" name="workflow_node_peoples_input" type="hidden" />
    <input id="workflow_node_condition_input" name="workflow_node_condition_input" type="hidden" />
    <input id="processId" name="processId" type="hidden" value="${processId }" />
    <input id="caseId" name="caseId" type="hidden" value="${caseId }" />
	<input id="subObjectId" name="subObjectId" type="hidden" /> 
	<input id="currentNodeId" name="currentNodeId" type="hidden" /> 
	<%--当调用的不是正文模板时，就保存模板id,表示流程已经编辑了，前台发文时就可以直接发送了 --%> 
	<input id="templeteProcessId" name="templeteProcessId" type="hidden" value="${templeteProcessId}" /> 
	<%-- GOV-4927 公文管理，发文内部文号重复时，提示错误！ --%>
    <input type="hidden" id="edocType_mark" name="edocType_mark" value="${param.edocType }" /> 
	<%-- 当通过待登记进行转发文时，关联id用签收id --%> 
	<input type="hidden" id="backBoxToEdit" name="backBoxToEdit" value="${ctp:escapeJavascript(param.backBoxToEdit)}" /> 
	<input type="hidden" id="recieveId" name="recieveId" value="${ctp:escapeJavascript(recieveId) }" /> 
	<input type="hidden" id="registerId" name="registerId" value="${registerId }" /> 
	<input type="hidden" id="distributeEdocId" name="distributeEdocId" value="${distributeEdocId }" /> 
	<%-- -- 区别已登记和待分发所关联的发文--> --%>
	<input type="hidden" id="forwordType" name="forwordType" value="${forwordType }" /> 
	<input type="hidden" id="app" name="app" value="${ctp:escapeJavascript(param.app)}" /> 
	<input type="hidden" id="openFrom" name="openFrom" value="" /> 
	<input type="hidden" id="edocGovType" name="edocGovType" /> 
	<%--puyc 添加 --%> 
	<input type="hidden" id="subType" name="subType" value="${param.subType }" /> 
	<input type="hidden" id="relationRecd" name="relationRecd" value="isNot" /> 
	<input type="hidden" id="relationRecId" name="relationRecId" value="-1" /> 
	<input type="hidden" id="relationSend" name="relationSend" value="isNot" /> 
	<input type="hidden" id="recSummaryIdVal" name="recSummaryIdVal" value="-1" />
	<%-- <!-- 分发，收文的summaryId --> --%>
    <%--puyc 添加  结束--%> 
    <input type="hidden" id="pageview" name="pageview" value="${pageview}" />
    <%-- 阅转办页面跳转标识  wangjingjing --%> 
    <%-- 套红代码的JS中需要使用此变量--%> 
    <input type="hidden" id="currContentNum" name="currContentNum" value="0" /> 
    <%-- 后台保存获取数据需要使用此变量--%>
    <input type="hidden" id="contentNo" name="contentNo" value="0" /> 
    <input type="hidden" id="isUniteSend" name="isUniteSend" value="false" /> 
    <input type="hidden" id="appName" name="appName" value="<%=ApplicationCategoryEnum.edoc.getKey()%>"/> 
    <input type="hidden" name="comm" id="comm" value="${ctp:escapeJavascript(comm)}"/> 
    <input type="hidden" name="id" value="${formModel.edocSummaryId}"/> 
	<%-- 办文转起草标识，目标是屏蔽第一次加载转文后的正文修改提示--%>
    <input type="hidden" id="forwordtosend" name="forwordtosend" value="${forwordtosend}"> 
    <%-- 套红代码的JS中需要使用此变量--%> 
	<input type="hidden" id="summary_id" name="summary_id" value="${formModel.edocSummaryId}"/> 
	<input type="hidden" id="summaryId" name="summaryId" value="${formModel.edocSummaryId}"/>
    <%-- changyi 性能优化，用于判断是否是新建公文--%> 
    <input type="hidden" id="newSummaryId" name="newSummaryId" value="${!empty param.summaryId ? param.summaryId : newSummaryId}"/>

    <input type="hidden" id="edocType" name="edocType" value="${formModel.edocSummary.edocType}" /> 
	<input type="hidden" name="exchangeId" value="${ctp:escapeJavascript(param.exchangeId)}" /> 
	<input type="hidden" name="actorId" value="${actorId}" /> 
	<input type="hidden" name="returnDeptId" id="returnDeptId" value=""/> 
	<input type="hidden" name="currContentNum" id="currContentNum" value="0"/>
    <c:choose>
	  <c:when test="${empty templeteId}">
		<c:set value="${param.templeteId }" var="tempId" />
	  </c:when>
	  <c:otherwise>
		<c:set value="${templeteId }" var="tempId" />
	  </c:otherwise>
    </c:choose> 
	<c:set value="${v3x:hasPlugin('doc')}" var="docResourceExist" />
    <input type="hidden" id="templeteId" name="templeteId"value="${tempId}" /> 
    <input type="hidden" name="currentNodeId" value="start" /> 
	<input type="hidden" name="supervisorId" id="supervisorId" value="${colSupervisors }"/> 
	<input type="hidden" name="supervisors" id="supervisors" value="${colSupervise.supervisors }"/> 
	<%--用来保存模板自带的督办人员 BUG-OA-39192--%>
    <input type="hidden" name="unCancelledVisor" id="unCancelledVisor" value="${unCancelledVisor}"/> 
	<input type="hidden" name="sVisorsFromTemplate" id="sVisorsFromTemplate" value="${sVisorsFromTemplate}"/> 
	<%--OA-26859 调用发文模板后将其存为个人模板，督办设置，基准时长没有保存（如图） --%>
    <%----<input type="hidden" name="awakeDate" id="awakeDate" value="${awakeDate}">--> --%>
    <input type="hidden" name="awakeDate" id="awakeDate" value="${superviseDate}"/> 
	<input type="hidden" name="superviseTitle" id="superviseTitle" value="${v3x:toHTML(colSupervise.title) }"/> 
	<input type="hidden" name="fromSend" id="fromSend" value="${fromSend}" /> 
	<input type="hidden" name="loginAccountId" id="loginAccountId" value="${v3x:currentUser().loginAccount}"/> 
	<input type="hidden" name="orgAccountId" id="orgAccountId" value="${formModel.edocSummary.orgAccountId}"/> 
	<%--strEdocId:收文登记的时候，保存来文EdocSummary的ID--%>
    <input type="hidden" name="strEdocId" id="strEdocId" value="${strEdocId}"/> 
	<input type="hidden" name="__ActionToken" readonly value="SEEYON_A8"/> 
	<%-- post提交的标示，先写死，后续动态 --%>
    <input type="hidden" name="archiveId" value="${formModel.edocSummary.archiveId}"/> 
	<input type="hidden" name="hasArchive" value="${formModel.edocSummary.hasArchive}"/> 
	<%-- 接收从弹出页面提交过来的数据 --%>
    <input type="hidden" name="popJsonId" id="popJsonId" value=""/> 
    <input type="hidden" name="popNodeSelected" id="popNodeSelected" value=""/>
    <input type="hidden" name="popNodeCondition" id="popNodeCondition" value=""/> 
	<input type="hidden" name="popNodeNewFlow" id="popNodeNewFlow" value=""/> 
	<input type="hidden" name="allNodes" id="allNodes" value=""/> 
	<input type="hidden" name="nodeCount" id="nodeCount" value=""/> 
	<%--判断文单正文附件是否被修改--%>
    <input type="hidden" name="isModifyContent" id="isModifyContent" value="0"/> 
	<input type="hidden" name="isModifyForm" id="isModifyForm" value="0"/> 
	<input type="hidden" name="isModifyAtt" id="isModifyAtt" value="0"/> 
	<%--退回拟稿人后的选择 --%>
    <input type="hidden" name="draftChoose" id="draftChoose"/> 
    <%--待发时传递过来 --%>
    <input type="hidden" name="affairId" id="affairId" value="${param.affairId }"/> 
	<input type="hidden" id="checkOption" name="checkOption" value="${checkOption}" /> 
	<input type="hidden" name="docMarkValue" id="docMarkValue" value="${formModel.edocSummary.docMark}"/> 
	<input type="hidden" name="docMarkValue2" id="docMarkValue2" value="${formModel.edocSummary.docMark2}"/> 
	<input type="hidden" name="docInmarkValue" id="docInmarkValue" value="${formModel.edocSummary.serialNo}"/> 
	<input id="templeteBodyTyep" type="hidden" value="${templeteBodyTyep}"/>
    <input id="isSystemTemplate" type="hidden" value="${isSystemTemplate}"/>
    <input id="A8registerBodyType" type="hidden" value="${A8registerBodyType}"/> 
    <!-- 客开：徐矿集团【保存主附件】 chenqiang 2019年3月27日  start -->
    <input id="mainAttachmentId" name="mainAttachmentId" type="hidden" value=""/>
    <input id="mainAttachmentValue" name="mainAttachmentValue" type="hidden" value=""/>
    <!-- 客开：徐矿集团【保存主附件】 chenqiang 2019年3月27日  end -->
	<%@include file="unitId.jsp"%>

    <span id="people" style="display: none;"> 
        <c:out value="${peopleFields}" escapeXml="false" /> 
    </span> 
    <script type="text/javascript">
      <!--
        var isConfirmExcludeSubDepartment_wf = true;
      //-->
    </script> 
    <v3x:selectPeople id="wf" panels="Department,Post,Team"
	selectType="Department,Member,Post,Team"
	departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
	jsFunction="setPeopleFields(elements, 'detailIframe.mainIframe.listFrame')"
	viewPage="selectNode4EdocWorkflow" /> 
	
	<script type="text/javascript">
      <!--
        onlyLoginAccount_wf=false;
        isNewOfficeFilePage=true;
        showOriginalElement_wf=false;
        showAccountShortname_wf="yes";
        accountId_wf="${formModel.edocSummary.orgAccountId}";
      //-->
    </script>

<div name="edocContentDiv" id="edocContentDiv" style="width:2px;height:2px;overflow:hidden; position: absolute;">
    <div>${cloneOriginalAtts}</div>

    <v3x:editor htmlId="content" editType="${canUpdateContent ? '1,0' : '0,0'}"
	  content="${formModel.edocBody.content}"
	  type="${formModel.edocBody.contentType=='' || (formModel.edocBody.contentType==null) ? 'OfficeWord' : formModel.edocBody.contentType}"
	  createDate="${formModel.edocBody.createTime}"
	  originalNeedClone="${formModel.edocBody.contentType=='gd' ? false : cloneOriginalAtts}"
	  category="<%=ApplicationCategoryEnum.edoc.getKey()%>"
	  contentName="${formModel.edocBody.contentName}" loadOfficeImmediate="false"/>
</div>
<script type="text/javascript"> 
var officeFileId = ""
if(typeof(officeParams)!="undefined"){
	officeFileId = officeParams.fileId;
	officeParams.isNewOfficeFilePage = true; 
	officeParams.currentPage = "newEdoc";
	officeParams.isNewEdocPage = true;
	officeParams.canEdit = "${canUpdateContent}";
}else{
	officeFileId = fileId;
}
var newTemplateType = "${newTemplateType}";
if(newTemplateType=="workflow"){//调用流程模版时，没有originalFileId
	officeParams.originalFileId="";
}
var newEdocBodyId=officeFileId;
//var newEdocBodyId;
//调用模板的时候使用。
if((typeof(isFromTemplate)!="undefined" && isFromTemplate==true) || ("${ctp:escapeJavascript(comm)}"=="distribute"||"${ctp:escapeJavascript(comm)}"=="register") || ${cloneOriginalAtts} ) {
    contentOfficeId.put("0",officeFileId); 
}

<fmt:message key="common.${((isFromTemplate && templateType !='text') || (templateType != 'text' && selfCreateFlow == 'false') || subState == '16') ? 'view' : 'design'}.workflow.label" bundle="${v3xCommonI18N}" var="workflowLable" />

var myBar = new WebFXMenuBar("${pageContext.request.contextPath}");

var saveAs = new WebFXMenu;
saveAs.add(new WebFXMenuItem("saveAsText", "<fmt:message key='templete.text.label' />", "saveAsTemplete('text')", "<c:url value='/apps_res/collaboration/images/text.gif'/>"));
saveAs.add(new WebFXMenuItem("saveAsWorkflow", "<fmt:message key='templete.workflow.label' />", "saveAsTemplete('workflow')", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>"));
saveAs.add(new WebFXMenuItem("saveAsTemplete", "<fmt:message key='templete.category.type.0' />", "saveAsTemplete('templete')", "<c:url value='/apps_res/collaboration/images/text_wf.gif'/>"));

var insert = new WebFXMenu;
insert.add(new WebFXMenuItem("", "<fmt:message key='permission.operation.UploadAttachment' bundle='${v3xCommonI18N}' />", "insertAttachmentFn()"));
insert.add(new WebFXMenuItem("", "<fmt:message key='permission.operation.UploadRelDoc' bundle='${v3xCommonI18N}' />", "quoteDocumentEdocFn()"));

<%-- 
/*var workflow = new WebFXMenu;
//workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.no.label' />", "doWorkFlow('no')"));
workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.new.label' />", "doWorkFlow('new')"));

if(window.dialogArguments) {
    workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.edit.label' />", "designWorkFlow('detailIframe.mainIframe.listFrame')"));
} else {
    workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.edit.label' />", "designWorkFlow('detailIframe.mainIframe.listFrame')"));
}
*/
--%>
<c:if test="${archivedModify==null}">
    <c:if test="${param.backBoxToEdit=='true'}">    
        myBar.add(new WebFXMenuButton("save", "<fmt:message key='edoc.receive.register.saveToDraftBox'/>", "saveWaitSent()", [1,5], "", null));
    </c:if> 

    //阅文转办文 不显示草稿箱
    <c:if test="${param.backBoxToEdit!='true' && ((param.pageview != 'listReading' && isAgent!=true && distributerId==v3x:currentUser().id && param.listType!='listV5Register') || (param.listType=='listV5Register' && enableRecWaitSend!='false' || isG6Ver && param.recListType=='listDistribute'))}">  //TODO --&&  enableWaitSend!='false'
       
       <%-- 排除阅文转办文 和 阅文转办文再调用模版的情况 --%>
       <c:if test="${param.comm != 'forwordtosend' || param.checkOption != '3'}">
          myBar.add(new WebFXMenuButton("save", "<fmt:message key='edoc.receive.register.saveToDraftBox'/>", "saveWaitSent()", [1,5], "", null));
       </c:if>
    </c:if>

    <%-- 从待登记点登记纸质收文时，草稿箱显示保存待发 --%>
    <%--
    <c:if test="${(param.listType=='listV5Register' && enableRecWaitSend!='false' || isG6Ver && param.recListType=='listDistribute')}">
        myBar.add(new WebFXMenuButton("save", "<fmt:message key='edoc.receive.register.saveToDraftBox'/>", "saveWaitSent()", [1,5], "", null));
    </c:if>
--%>
    myBar.add(new WebFXMenuButton("saveAs", "<fmt:message key='saveAs.label'/><fmt:message key='templete.personal.label'/>", "saveAsTemplete()", [3,5], "",null));    
 <%--   
    /*if(window.dialogArguments) {
        myBar.add(new WebFXMenuButton("workflow", "${workflowLable}", "createEdocWFPersonal()", [3,6], "", null));
    } else {
        myBar.add(new WebFXMenuButton("workflow", "${workflowLable}", "createEdocWFPersonal()", [3,6], "", null));
    }*/
 --%>   
    <c:if test="${param.backBoxToEdit!='true' || param.canOpenTemplete == 'true'}">
        myBar.add(new WebFXMenuButton("templete", "<fmt:message key='common.toolbar.templete.label' bundle='${v3xCommonI18N}'/>", "openTemplete(templeteCategrory,'${templete.id==null ? 1 : templete.id}')", [3,7], "", null));
    </c:if>
    
</c:if>

<c:if test="${archivedModify}">
    myBar.add(new WebFXMenuButton("saveArchived", "<fmt:message key='common.toolbar.save.label' bundle='${v3xCommonI18N}'/>", "saveArchived()", [1,5], "", null));
</c:if> 

<%--

/*
if(isFromTemplate) {
    myBar.add(new WebFXMenuButton("workflow", "<fmt:message key='workflow.label' />", "designWorkFlow('detailIframe')", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>", "", null));
} else {
    myBar.add(new WebFXMenuButton("workflow", "<fmt:message key='workflow.label' />", "", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>", "", workflow));
}*/  

--%>

myBar.add(new WebFXMenuButton("insert", "<fmt:message key='common.toolbar.insert.label' bundle='${v3xCommonI18N}'/>", null, [1,6], "", insert));

<%--

//var hasOfficeOcx=${v3x:hasPlugin('officeOcx')};
//if(hasOfficeOcx){myBar.add(createOfficeMenu(v3x));}

--%>

<c:if test="${archivedModify==null}"> 
    myBar.add(${v3x:bodyTypeSelector("v3x")});
</c:if>

myBar.add(new WebFXMenuButton("content", "<fmt:message key='common.toolbar.content.label' bundle='${v3xCommonI18N}' />","dealPopupContentWinWhenDraft('0');", [8,10], "", null));

<c:if test="${appType==19}">
    <c:if test="${hasBody1}">
        myBar.add(new WebFXMenuButton("content", "<fmt:message key='edoc.contentnum1.label' />","dealPopupContentWinWhenDraft('1');", [8,10], "", null));
    </c:if>
    <c:if test="${hasBody2}">
        myBar.add(new WebFXMenuButton("content", "<fmt:message key='edoc.contentnum2.label' />","dealPopupContentWinWhenDraft('2');", [8,10], "", null));
    </c:if>
 <%--
//myBar.add(new WebFXMenuButton("taohong", "<fmt:message key='edoc.action.form.template' />","taohongWhenDraft('edoc');", '<c:url value="common/images/zwth.gif"/>', "", null));
--%>
</c:if>

<c:if test="${archivedModify==null}"> 
    myBar.add(new WebFXMenuButton("superviseSetup", "<fmt:message key='common.toolbar.supervise.label' bundle='${v3xCommonI18N}' />", "openSuperviseWindow()", [21,1], "", null));
    var _label = "";
    <c:if test = "${appType eq 19}">
        _label = "<fmt:message key='edoc.sendQuick_0.label'/>";
    </c:if>
    <c:if test = "${appType eq 20}">
        _label = "<fmt:message key='edoc.sendQuick_1.label'/>";
    </c:if>
    <c:if test= "${(appType eq 19 or appType eq 20)}" >
         myBar.add(new WebFXMenuButton("isQuickS", "<input id='isQuickSend' name='isQuickSend' type='checkbox' style='margin-left:8px;' onclick='showQuickSend();' <c:if test='${formModel.edocSummary.isQuickSend}'>checked='true'</c:if> />"+"  "+_label, "","" , "", null));
    </c:if>
</c:if>

var categroy2Forms = '${categroy2Forms}';

</script>


<table border="0" cellspacing="0" cellpadding="0" class="w100b">
	<tr id="tb_height">
		<td height="22" class="webfx-menu-bar border_b border-left border-right">
		  <div id="toolbar_div" class="h100b w100b" style="overflow: hidden;">
		    <span id="toolbarLocation"></span>
		  	<script type="text/javascript">
                document.getElementById("toolbarLocation").outerHTML = myBar.toString();
            </script>
		  </div>
        </td>
	</tr>
	<tr id="form_height">
		<td class="border-left border-right">
		<div id="paddingtopdiv_5" style="height: 5px;overflow: hidden;">&nbsp;</div>
		<table border="0" height="10" cellpadding="0" cellspacing="0" width="100%" align="center" id="contentTable">
			<tr>
				<!-- 发送 -->
				<td id="send_td" rowspan="2" width="1%" nowrap="nowrap">
				  <c:if test="${archivedModify==null}">
					<a href="javascript:void(0)" id='send'
					<%-- 如果showDraftChoose方法报错，请查看edoc.js历史 --%>
						onClick='${(archivedModify==null && fromSendBack=="true") ? "showDraftChoose()" : "sendCallBack_newEdoc()"};return false;'
						class="margin_lr_10 display_inline-block align_center new_btn">
					<fmt:message key='common.toolbar.send.label' bundle='${v3xCommonI18N}' /> </a>
				  </c:if>
				</td>

				<!-- 流程 -->
				<fmt:message key="${selfCreateFlow ? 'default.workflowInfo.value' : 'alert_notcreateflow_loadtemplate'}" var="dfwf" />
				<th nowrap="nowrap" width="1%" align="right" class="padding_r_5">
				  <div id="sel_label_workflow"><fmt:message key="workflow.label" />:&nbsp; </div>
				  <c:if test="${appType==19}">
					<div id="sel_label_exchange" style="display: none;">
					<fmt:message key="exchange.edoc.exchangeLabel" />:</div>
				  </c:if>
				</th>
				<td width="30%">
				<div class="common_txtbox_wrap" id="process_info_div">
				  <input id="process_info" id="workflowInfo" name="workflowInfo"
					class="w100b cursor-hand" readonly
					value="${workflowNodesInfo==''?dfwf:workflowNodesInfo }"
					onClick="selectPeo();" ${((isFromTemplate == true && templateType != 'text' ) || selfCreateFlow==false || (archivedModify==true) || (fromSendBack==true) || subState=='16') ? 'disabled' : ''}/>
				</div>
				<c:if test="${appType==19}">
					<%--快速发文 公文交换 start --%>
					<div id="exchangeSel" style="display: none;width: 100%;">
					<div class="metadataItemDiv">
					  <label for="edocExchangeType_depart"> 
					    <input type="radio"
						  class="validationStepback" name="edocExchangeType"
						  id="edocExchangeType_depart" onclick="hideMemberList()" value="0"
						  <c:choose>
						    <c:when test="${edocSummaryQuick!=null && edocSummaryQuick.exchangeType==0}">checked='true'</c:when>
						    <c:when test="${edocSummaryQuick==null && exchangeTypeSwitchValue==0}">checked='true'</c:when>
						  </c:choose>
						  />
						  <span><fmt:message key="edoc.exchangetype.department.label"/></span></label>
					</div>
					<div class="metadataItemDiv">
                      <div id="selectExchangeDeptType"
                        <c:choose>
                            <c:when test="${edocSummaryQuick!=null && edocSummaryQuick.exchangeType==0}">style="display:block;"</c:when>
                            <c:when test="${edocSummaryQuick==null && exchangeTypeSwitchValue==0}">style="display:block;"</c:when>
                            <c:otherwise>style="display:none;"</c:otherwise>
                        </c:choose>>
                        <select name="exchangeDeptType" class="condition" style="width: 115px">
                          <option value="Creater"
                              <c:choose>
                                <c:when test="${edocSummaryQuick!=null && edocSummaryQuick.exchangeDeptType=='Creater'}">selected="selected"</c:when>
                                <c:when test="${edocSummaryQuick==null && exchangeDeptTypeSwitchValue=='Creater'}">selected="selected"</c:when>
                                <c:otherwise></c:otherwise>
                              </c:choose>>
                            <fmt:message key="edoc.label.exchange.dept.creater" bundle="${v3xMainI18N}"/>
                          </option>
                          <option value="Dispatcher" <c:choose>
                                <c:when test="${edocSummaryQuick!=null && edocSummaryQuick.exchangeDeptType=='Dispatcher'}">selected="selected"</c:when>
                                <c:when test="${edocSummaryQuick==null && exchangeDeptTypeSwitchValue=='Dispatcher'}">selected="selected"</c:when>
                                <c:otherwise></c:otherwise>
                              </c:choose>>
                            <fmt:message key="edoc.label.exchange.dept.dispatcher" bundle="${v3xMainI18N}"/>
                          </option>
                        </select>
                      </div>
                    </div>
					<div class="metadataItemDiv">
                      <label for="edocExchangeType_company">
                        <input type="radio"
                          class="validationStepback" name="edocExchangeType"
                          id="edocExchangeType_company" onClick="showMemberList()" value="1"
                          <c:choose>
                            <c:when test="${edocSummaryQuick!=null && edocSummaryQuick.exchangeType==1}">checked='true'</c:when>
                            <c:when test="${edocSummaryQuick==null && exchangeTypeSwitchValue==1}">checked='true'</c:when>
                          </c:choose>
                        />
                        <span><fmt:message key="edoc.exchangetype.company.label" /></span></label>
                    </div>
					<div class="metadataItemDiv">
					  <div id="selectMemberList"
						<c:choose>
						  <c:when test="${edocSummaryQuick!=null && edocSummaryQuick.exchangeAccountMemberId!=null}">style="display:block;"</c:when>
						  <c:when test="${(edocSummaryQuick!=null && edocSummaryQuick.exchangeType==1) || (edocSummaryQuick==null && exchangeTypeSwitchValue==1)}">style="display:block;"</c:when>
				          <c:otherwise>style="display:none;"</c:otherwise>
				        </c:choose>>
					    <select name="memberList" class="condition" style="width: 115px">
						  <option value=""><fmt:message key="select.label.unitEdocOper" /></option>
					      <c:forEach items="${memberList}" var="member">
						    <option value="${member.id}"
							  <c:if test="${edocSummaryQuick.exchangeAccountMemberId==member.id}">selected='true'</c:if>>${v3x:toHTML(member.name)}
					        </option>
					      </c:forEach>
					    </select>
					  </div>
					</div>
					</div>
					<%--快速发文 公文交换 end --%>
				</c:if></td>
				<td width="1%">
				<div class="padding_l_10" id="workflowInfo_div">
				  <c:set value="${archivedModify!=null || (readWorkflow == 'disabled' && selfCreateFlow==false)}" var="disabledFlag" />			  
				  <a class="common_button common_button_icon comp" style="color:#333;"
					<c:if test="${disabledFlag==true}">  disabled="true"  style="color:gray;" </c:if>
					<c:if test="${disabledFlag==false}"> onClick="createEdocWFPersonal(); return false;"</c:if>
					href="javascript:void(0)">
				    <em class="ico16 process_16 w100b"> </em>${workflowLable}
				  </a>
				</div>
				</td>

				<!-- 流程期限 -->
				<th nowrap="nowrap" width="1%" align="right" class="padding_r_5">
				  <div id="sel_label_deadline" class="padding_l_30">
				    <fmt:message key="process.cycle.label" />:&nbsp;
				  </div>
				  <div id="sel_label_taohong" class="padding_l_30" style="display: none;">
				    <fmt:message key="edoc.action.form.template" />:&nbsp;
				  </div>
				</th>
				<td width="40%" id="deadlineTD">
				<div id="deadline_div" class="common_selectbox_wrap w100b">
				  <table border="0" cellpadding="0" cellspacing="0" style="width: 100%;TABLE-LAYOUT: fixed;">
				      <colgroup>
                        <col style="WIDTH: 60px"></col>
                      </colgroup>
				    <tr>
				      <td>
				         <input type="hidden" name="deadline2" id="deadline2" value="${finalDeadline}" /> 
		                  <select name="deadlineSelect" id="deadline" class="input-100per" style="width: 100%;margin: 0px;" onChange="javascript:compareTime(this)"
		                    <c:if test="${archivedModify || (empty parentSummary && isFromTemplate&&(summaryFromTemplate.deadline)!=0) || (!empty parentSummary && isFromTemplate && parentSummary.deadline !=0 )}">DISABLED</c:if>>
		                    <v3x:metadataItem metadata="${deadlineMetadata}" showType="option" name="deadline" selected="${finalDeadline}" bundle="${colI18N}" />
		                </select>
				      </td>
				      <td style="display: none;">
				        <table id="deadLineCalender" border="0" cellpadding="0" cellspacing="0" style="width: 100%;TABLE-LAYOUT: fixed;">
				          <tr>
				             <td style="width: 6px;">&nbsp;</td>
				             <td>
				                <input type="text" name="deadLineDateTimeInput" style="width: 100%; height: 20px;" id="deadLineDateTimeInput"
		                            class="cursor-hand input-100per" readonly="true"
		                            onclick="selectDeadline('${pageContext.request.contextPath}',this,400,200);"
		                            value=""/>
				             </td>
				          </tr>
				        </table>
				      </td>
				    </tr>
				  </table>
				  <input type="hidden" id="deadLineDateTime" name="deadLineDateTime" value="${formModel.edocSummary.deadlineDatetime}" />
			      <input type="hidden" id="deadline4temp" name="deadline4temp" value="${formModel.deadline4temp}" />
			    </div>
				<div id="taohong_div" class="common_selectbox_wrap w100b" style="display: none;">
				  <select id="fileUrl" name="fileUrl" class="input-100per" style="" onchange="sendQuick_taohong('edoc')">
					<option value=""><fmt:message key='templete.select_template.label' /></option>
				  </select>
				  <select id="fileUrl_bak" name="fileUrl_bak" class="hidden" style="">
					<c:forEach items="${taohongList}" var="taohong">
						<c:set var="taohongurl" value="${taohong.fileUrl}&${taohong.textType}" />
						<option value=""><fmt:message key='templete.select_template.label' /></option>
						<option value="${taohong.fileUrl}&${taohong.textType}"
							<c:if test="${edocSummaryQuick.taohongTemplateUrl eq taohongurl}">selected='true'</c:if>>
						${taohong.name}</option>
					</c:forEach>
				  </select>
				</div>
				</td>

				<!-- 提醒 -->
				<th nowrap="nowrap" width="1%" align="right" class="padding_r_5" colspan="1">
				  <input type="hidden" name="deadlineTime" id="deadlineTime" value="${v3x:showDeadlineTime(finalCreateTime,finalDeadline)}" />
				  <div class="padding_l_10" id="sel_label_tixing">
				    <fmt:message key="common.remind.time.label" bundle='${v3xCommonI18N}' />:&nbsp;
				  </div>
				</th>
				<td width="25%" <c:if test="${!isFromTemplate}">colspan="1"</c:if>>
				<div class="common_selectbox_wrap w100b" id="tixing_div">
				  <input type="hidden" name="advanceRemind2" value="${formModel.edocSummary.advanceRemind}" />
				  <select name="advanceRemind" id="advanceRemind" class="input-100per" onChange=""
					<c:if test="${archivedModify || (empty parentSummary && isFromTemplate&&(summaryFromTemplate.advanceRemind)>0)|| (!empty parentSummary && isFromTemplate && parentSummary.advanceRemind !=0 )}">DISABLED</c:if>>
					<v3x:metadataItem metadata="${remindMetadata}" showType="option"
						name="deadline" selected="${formModel.edocSummary.advanceRemind}"
						bundle="${v3xCommonI18N}" />
				  </select>
				</div>
				</td>
				
				<%-- 对应更多列 , 收文和调用模版的时候出现--%>
				<c:if test="${isFromTemplate || formModel.edocSummary.edocType==1}">
				    <td width="60">&nbsp;</td>
				</c:if>
				
				<!-- 右缀 -->
				<td width="10" id="tdRight1" valign="middle" nowrap="nowrap" align="right"></td>
			</tr>

			<tr>
				<!-- 公文单 -->
				<th nowrap="nowrap" align="right" class="padding_r_5">
				<div><fmt:message key="edocTable.label" />:&nbsp;</div>
				</th>
				<td colspan="2">
				<table border="0" cellpadding="0" cellspacing="0" style="width: 100%;TABLE-LAYOUT: fixed;">
				  <tr>
				     <c:set value="${formModel.edocSummary.edocType eq 0 && v3x:getSystemProperty('edoc.hasEdocCategory')}" var="wid"></c:set>
				     <c:if test="${wid}">
				     <td style="width: 30%">
				        <select name="edocCategorySelect" id="edocCategorySelect" style="width: 100%"
                        onChange="javascript:changeCategory(this, ${edocFormId});"
                        <c:if test="${(isFromTemplate && templateType!='workflow')|| (archivedModify==true)}">DISABLED</c:if>>
                        <option value="all">
                          <fmt:message key="edoc.category.all.label" />
                        </option>
                        <c:forEach var="cg" items="${categorys}">
                            <option title="<c:out value='${cg.name}' />" value="<c:out value="${cg.id}"/>"
                                <c:if test="${cg.id eq categoryId}">selected</c:if>><c:out
                                value="${cg.name}" />
                            </option>
                        </c:forEach>
                       </select>
				     </td>
				     </c:if>
				     <td>
				       <select name="edoctable" id="edoctable" style="width:100%"
                    onChange="javascript:changeEdocForm(this, ${edocFormId});"
                    <c:if test="${(isFromTemplate && templateType!='workflow')|| (archivedModify==true)}">DISABLED</c:if>>
                    <c:forEach var="edocForm" items="${edocForms}">
                        <option value="<c:out value="${edocForm.id}"/>"
                            <c:if test="${edocForm.id==edocFormId}">selected</c:if>><c:out
                            value="${edocForm.name}" />
                        </option>
                    </c:forEach>
                   </select>
				     </td>
				  </tr>
				</table>
				</td>
				
				<c:if test="${isFromTemplate && docResourceExist}">
                         <%--OA-20113 拟文页面填写标题、流程后另存为个人模版，然后再去调用这个个人模版，新的拟文页面多了预归档的功能 --%>
                  <%--当调用系统模板，或者调用的个人模板是  从系统模板中存为个人模板，才显示预归档 --%>
                  <%-- 来自模板  --%>
                  <th nowrap="nowrap" align="right" class="padding_r_5">
                  <div>
                    <c:choose>
                      <c:when test="${archivedModify}">
                          <fmt:message key='pigeonhole.label.to' />:&nbsp;
                      </c:when>
                      <c:otherwise>
                          <fmt:message key="prep-pigeonhole.label" />:&nbsp;
                      </c:otherwise>
                    </c:choose>
                  </div>
                  </th>
                  <td>
                  <div class="common_selectbox_wrap w100b"><%--OA-24783 lixsh调用模板后，选择预归档目录发送后，在已发中撤销后，在待发中编辑，预归档目录不可以编辑了 --%>
                  <select id="selectPigeonholePath" title="${fullArchiveName}"
                      class="input-100per" style="width:100%;"
                      onchange="guidangFromTemplete(this);"
                      <c:if test="${archivedModify ||(empty parentSummary && archiveName ne null && archiveName ne '' && setArchive == 'true') || (!empty parentSummary && parentSummary.archiveId ne null && setArchive == 'true')}">disabled</c:if>>
                      <option id="defaultOption" value="1"><fmt:message
                          key="common.default" bundle="${v3xCommonI18N}" /></option>
                      <option id="modifyOption" value="2">${v3x:_(pageContext, 'click.choice')}</option>
                      <c:if test="${archiveName ne null && archiveName ne ''}">
                          <option value="3" selected>${archiveName}</option>
                      </c:if>
                  </select></div>
                  </td>
                         
                      </c:if>
				
				
				<c:if test="${(isFromTemplate && formModel.edocSummary.edocType!=1) || !isFromTemplate}">
				
				<%-- 跟踪 --%>
				<c:set var="genzongFlag" value="${isFromTemplate || isTrack}"/>
				<c:set var="genzongFlag2" value="${isFromTemplate || !isTrack}"/>
				<c:set var="archivedModifyFlag" value="${!isFromTemplate && (archivedModify == null)}"/>
				
                <th nowrap="nowrap" align="right">
                  <div id="genzong_div" class="margin_l_20">
                    <label for="isTrack">
                      <input type="checkbox" name="isTrack" value="1" onclick="setTrackRadiio();" id="isTrack"
                      ${formModel.edocSummary.canTrack==1 && genzongFlag ? 'checked' : ''} ${archivedModify==true ? 'DISABLED' : ''} />
                      <fmt:message key="track.label" />: &nbsp; 
                    </label>
                  </div>
                  
                  <c:if test="${!isFromTemplate}">
                  <div id="sel_label_guidang" class="padding_l_30" style="display: none;">
                    <fmt:message key="edoc.edoctitle.pigeonholeLabel" />:
                  </div>
                  </c:if>
                </th>
                <td nowrap="nowrap">
                
                <div id="genzong_label">
                  <table border="0" cellpadding="0" cellspacing="0" style="width: 100%;TABLE-LAYOUT: fixed;">
                      <colgroup>
                        <col style="WIDTH: 53px"></col>
                        <col style="WIDTH: 65px"></col>
                      </colgroup>
                      <tr>
                        <td>
                          <label for="trackRange_all">
                            <input type="radio" name="trackRange" id="trackRange_all" onclick="setTrackCheckboxChecked();"
                              value="1" ${empty trackIds && formModel.edocSummary.canTrack==1 && genzongFlag ? 'checked' : ''} ${(archivedModify==true || archivedModifyFlag || formModel.edocSummary.canTrack==0) && genzongFlag2 ? 'DISABLED' : ''} />
                            <fmt:message key="col.track.all" bundle="${v3xCommonI18N}" />
                          </label>
                        </td>
                        <td>
                          <div style="width:120px;">
	                          <label for="trackRange_part">
	                            <input type="radio" name="trackRange" id="trackRange_part"
	                              onclick="selectPeopleFunTrackNewCol()" value="0" ${not empty trackIds  && isTrack ? 'checked' : ''} ${(archivedModify==true || archivedModifyFlag || formModel.edocSummary.canTrack==0) && genzongFlag2 ? 'DISABLED' : ''} />
	                            <fmt:message key="col.track.part" bundle="${v3xCommonI18N}" />
	                            <input type="hidden" value="${trackIds}" name="trackMembers" id="trackMembers" />
	                            <c:set value="${v3x:parseElementsOfIds(trackIds, 'Member')}" var="mids" />
	                            <v3x:selectPeople id="track" panels="Department,Team,Post,Outworker,RelatePeople"
	                              selectType="Member" jsFunction="setPeople(elements)"
	                              originalElements="${mids}" />
	                          </label>
                          </div>
                        </td>
                        <td>
                          <input type="text" id="zdgzryName" name="zdgzryName" style="display:none;width: 100%;" onclick="selectPeopleFunTrackNewCol()" />
                        </td>
                      </tr>
                  </table>
                </div>
                
                <c:if test="${!isFromTemplate}">
                <div id="guidang_div" style="display: none;">
                  <select id="selectPigeonholePath" name="kuaisuGuidangSelect"
                    title="${quickArchiveName}" class="input-100per"
                    style="width: 100%;"
                    onChange="pigeonholeEvent(this,'<%=ApplicationCategoryEnum.edoc.key()%>','finishWorkItem',this.form)">
                    <option id="defaultOption" value="1"><fmt:message key="common.default" bundle="${v3xCommonI18N}" /></option>
                    <option id="modifyOption" value="2"><fmt:message key="edoc.click.choice"/></option>
                    <c:if test="${quickArchiveName ne null && quickArchiveName ne ''}">
                        <option value="${quickArchiveId }" selected>${quickArchiveName}</option>
                    </c:if>
                  </select>
                </div>
                </c:if>
                </td>
                </c:if>
						
		         <!-- lijl添加,2011-10-8,处理类型(办文1\阅文2) -->
                 <c:choose>
                 <c:when test="${formModel.edocSummary.edocType==1}">
                     <c:if test="${readFlag == 'true' && isG6}">
                         <td align="right"  nowrap="nowrap" class="padding_r_5" id="bllx_label_1">
                           <div class="padding_l_10">
                             <fmt:message key="col.process.type" bundle="${v3xCommonI18N}" />:&nbsp;
                           </div>
                         </td>
                         <td nowrap="nowrap" id="bllx_label_2">
                           <input type="radio" id="bw_processType" name="processType2"
                             <c:if test="${ireive ==1}">disabled="true" </c:if>
                             onClick="setProcessTypeIdValue(1)"
                             ${((formModel.edocSummary.processType==null&&isG6)||formModel.edocSummary.processType==1) ? "checked" : ""} value="1" /> 
                           <label for="bw_processType">
                             <fmt:message key="col.process.type.BW" bundle="${v3xCommonI18N}" />
                           </label>
                           <input type="radio" id="yw_processType" name="processType2"
                             <c:if test="${ireive  ==1}">disabled="true" </c:if>
                             onClick="setProcessTypeIdValue(2)"
                             ${((formModel.edocSummary.processType==null&&!isG6)||formModel.edocSummary.processType==2) ? "checked" : ""} value="2" />
                           <label for="yw_processType">
                             <fmt:message key="col.process.type.YW" bundle="${v3xCommonI18N}" />
                           </label>
                           <input type="hidden" name="processType" id="processType"
                             value="${((formModel.edocSummary.processType==null&&!isG6)||formModel.edocSummary.processType==2) ? 2 : 1}" />
                         </td>
                     </c:if>
                     <c:if test="${readFlag != 'true' || !isG6}">
                        <td colspan="2">
                           <input type="hidden" name="processType" id="processType"
                             value="${((formModel.edocSummary.processType==null&&!isG6)||formModel.edocSummary.processType==2) ? 2 : 1}" />
                        </td>
                     </c:if>
                 </c:when>
                 <c:when test="${!isFromTemplate || !docResourceExist}">
                    <%-- 普通的增加流程追溯开始 add by libing --%>
                      <th nowrap="nowrap" align="right" class="padding_r_5">
                        <div class="padding_l_10" id="zhuisu_label_div">
                          <fmt:message key="trace.label.traceornot" />:&nbsp;
                        </div>
                      </th>
                      <td nowrap="nowrap">
                        <div id="zhuisu_div"><fmt:message key="trace.label.designby" /></div>
                      </td>
                 </c:when>
                 <c:otherwise>
                     <%-- 发文调用模版的情况 --%>
                 </c:otherwise>
               </c:choose>
				
				<%-- 更多 --%>
				<c:if test="${isFromTemplate || formModel.edocSummary.edocType==1}">
                  <td id="show_more_td" class="" nowrap="nowrap" align="right">
                    <a id="show_more" class="clearfix" style="">&nbsp;&nbsp;${ctp:i18n('collaboration.newcoll.show')}
                      <span class="ico16 arrow_2_b"></span>
                    </a>
                  </td>
                </c:if>

				<!-- 右缀 -->
				<td width="10" id="tdRight2" valign="middle" nowrap="nowrap" align="right"></td>
			</tr>
            <%--更多第一行 --%>
            <tr id="show_more_tr1" class="newinfo_more" style="display: none">
            
              <%-- 模板的时候增加流程追溯--%>
              <c:choose>
                <c:when test="${isFromTemplate}">
                    
                  <%-- 模板的时候增加基准时长 --%>
                  <th nowrap="nowrap" align="right" class="padding_r_5">
                    <div id="jizhunshichang_label_div">
                      <fmt:message key="common.reference.time.label" bundle='${v3xCommonI18N}' />:&nbsp;
                    </div>
                  </th>
                  <td colspan="2">
                    <div class="common_txtbox_wrap" id="jizhunshichang_label_div2">
                      <c:choose>
                        <c:when test="${empty standardDuration or standardDuration eq 0 }">
                          <fmt:message key="time.no" bundle="${workflowI18N}" var="v"></fmt:message>
                        </c:when>
                        <c:otherwise>
                          <c:set value="${v3x:showDateByNature(standardDuration)}" var="v"></c:set>
                        </c:otherwise>
                      </c:choose>
                      <input type="text" id="jizhunshichang" disabled="disabled" value="${v}" style="width: 100%"/>
                      <input type="hidden" name="standardDuration" id="standardDuration" value="${standardDuration}" />
                    </div>
                  </td>
                    
                    <c:if test="${formModel.edocSummary.edocType==1}">
                
                <%-- 跟踪 --%>
                <c:set var="genzongFlag" value="${isFromTemplate || isTrack}"/>
                <c:set var="genzongFlag2" value="${isFromTemplate || !isTrack}"/>
                <c:set var="archivedModifyFlag" value="${!isFromTemplate && (archivedModify == null)}"/>
                
                <th nowrap="nowrap" align="right">
                  <div id="genzong_div" class="margin_l_20">
                    <label for="isTrack">
                      <input type="checkbox" name="isTrack" value="1" onclick="setTrackRadiio();" id="isTrack"
                      ${formModel.edocSummary.canTrack==1 && genzongFlag ? 'checked' : ''} ${archivedModify==true ? 'DISABLED' : ''} />
                      <fmt:message key="track.label" />: &nbsp; 
                    </label>
                  </div>
                </th>
                <td nowrap="nowrap">
                
                <div id="genzong_label">
                  <table border="0" cellpadding="0" cellspacing="0" style="width: 100%;TABLE-LAYOUT: fixed;">
                      <colgroup>
                        <col style="WIDTH: 53px"></col>
                        <col style="WIDTH: 65px"></col>
                      </colgroup>
                      <tr>
                        <td>
                          <label for="trackRange_all">
                            <input type="radio" name="trackRange" id="trackRange_all" onclick="setTrackCheckboxChecked();"
                              value="1" ${empty trackIds && formModel.edocSummary.canTrack==1 && genzongFlag ? 'checked' : ''} ${(archivedModify==true || archivedModifyFlag || formModel.edocSummary.canTrack==0) && genzongFlag2 ? 'DISABLED' : ''} />
                            <fmt:message key="col.track.all" bundle="${v3xCommonI18N}" />
                          </label>
                        </td>
                        <td>
                          <label for="trackRange_part">
                            <input type="radio" name="trackRange" id="trackRange_part"
                              onclick="selectPeopleFunTrackNewCol()" value="0" ${not empty trackIds  && isTrack ? 'checked' : ''} ${(archivedModify==true || archivedModifyFlag || formModel.edocSummary.canTrack==0) && genzongFlag2 ? 'DISABLED' : ''} />
                            <fmt:message key="col.track.part" bundle="${v3xCommonI18N}" />
                            <input type="hidden" value="${trackIds}" name="trackMembers" id="trackMembers" />
                            <c:set value="${v3x:parseElementsOfIds(trackIds, 'Member')}" var="mids" />
                            <v3x:selectPeople id="track" panels="Department,Team,Post,Outworker,RelatePeople"
                              selectType="Member" jsFunction="setPeople(elements)"
                              originalElements="${mids}" />
                          </label>
                        </td>
                        <td>
                          <input type="text" id="zdgzryName" name="zdgzryName" style="display:none;width: 100%;" onclick="selectPeopleFunTrackNewCol()" />
                        </td>
                      </tr>
                  </table>
                </div>
                </td>
                </c:if>
                    
                  <!-- 流程追溯 -->
                  <c:choose>
                    <%--OA-23515  调用模版，新建发文，在已发中撤销了，待发中编辑，编辑页面丢了基准时长   --%>
                    <%-- 这里加上基准时间不为0时 就显示 --%>
                    <c:when test="${(isSystem || isFormParentId || (!empty standardDuration && standardDuration != 0)||isFromTemplate) && docResourceExist }">
                      <th nowrap="nowrap" align="right" class="padding_r_5">
                        <div id="zhuisu_label_div" class="padding_l_10">
                          &nbsp;&nbsp;<fmt:message key="trace.label.traceornot" />:&nbsp;
                        </div>
                      </th>
                      <td colspan="2">
                        <div id="zhuisu_div">
                          <c:if test="${temTraceType eq null || temTraceType eq 0}">
                            <fmt:message key="trace.label.designby" />
                          </c:if>
                          <c:if test="${null ne temTraceType && temTraceType eq 1}">
                            <fmt:message key="trace.label.zhuisuo" />
                          </c:if>
                          <c:if test="${null ne temTraceType && temTraceType eq 2}">
                            <fmt:message key="trace.label.buzhuisuo" />
                          </c:if>
                        </div>
                        <input type="hidden" value="${temTraceType}" id='canTrackWorkFlow' name='canTrackWorkFlow' />
                      </td>
                    </c:when>
                    <c:otherwise>
                      <td nowrap="nowrap" align="right" colspan="3"></td>
                    </c:otherwise>
                  </c:choose>
                   
                   <c:if test="${formModel.edocSummary.edocType!=1}">
                       <%-- 非收文要进行补全两个TD --%>
                       <td colspan="2"></td>
                   </c:if>
                    
                   <td colspan="1">&nbsp;</td>
                   
                </c:when>
                 <c:when test="${formModel.edocSummary.edocType==1}">
                   <%-- 普通的增加流程追溯开始 add by libing --%>
                  <th nowrap="nowrap" align="right" class="padding_r_5">
                    <div class="" id="zhuisu_label_div">
                      <fmt:message key="trace.label.traceornot" />:&nbsp;
                    </div>
                  </th>
                  <td colspan="2" nowrap="nowrap">
                    <div id="zhuisu_div">
                      <fmt:message key="trace.label.designby" />
                    </div>
                  </td>
                  <%-- 普通的增加流程追溯结束 add by libing --%>
                  <td nowrap="nowrap" colspan="7"></td>
                 </c:when>
                 <c:otherwise>
                      <td nowrap="nowrap" align="right" colspan="10"></td>
                 </c:otherwise>
              </c:choose>
              </tr>
            
			<tr id="attachment2TR" class="" style="display: none;"
				height="18">
				<td nowrap="nowrap" colspan="2" class="bg-gray" align="right">
				<div class="w100b" align="right" valign="top"><fmt:message
					key="common.toolbar.insert.mydocument.label"
					bundle="${v3xCommonI18N}" />:(<span id="attachment2NumberDiv"></span>)</div>
				</td>
				<td valign="top" <c:choose>
                                        <c:when test="${isFromTemplate || formModel.edocSummary.edocType==1}">
                                            colspan="8"
                                        </c:when>
                                        <c:otherwise>
                                             colspan="7"
                                        </c:otherwise>
                                      </c:choose>>
				<div class="div-float" style="margin-top: 5px;"></div>
				<div id="attachment2Area" style="margin-top: 2px;"></div>
				</td>
			</tr>

			<tr id="attachmentTR" class="" style="display: none;" height="18">
				<td nowrap="nowrap" colspan="2" class="bg-gray" align="right">
				  <div class="w100b" align="right">
				    <fmt:message key="common.attachment.label" bundle="${v3xCommonI18N}" />:(<span id="attachmentNumberDiv"></span>)
				  </div>
				</td>
				<td valign="top" <c:choose>
                                        <c:when test="${isFromTemplate || formModel.edocSummary.edocType==1}">
                                            colspan="8"
                                        </c:when>
                                        <c:otherwise>
                                             colspan="7"
                                        </c:otherwise>
                                      </c:choose>>
                   <v3x:fileUpload attachments="${attachments}" canDeleteOriginalAtts="${true}"
					originalAttsNeedClone="${cloneOriginalAtts}" /></td>
			</tr>
		</table>
		<div id="paddingbottomdiv_5" style="height: 5px;overflow: hidden;">&nbsp;</div>
		</td>
	</tr>
	<tr valign="top">
		<td>
		<c:if test="${hasBarCode}">
			<v3x:webBarCode readerId="PDF417Reader" readerCallBack="initDate" />
		</c:if>
		<table width="100%" height="100%" border="0" cellspacing="0"
			cellpadding="0">
			<tr valign="top">
				<td>
				<div id="formAreaDiv" class="scrollList bg_color_white border_all relative">
				<div id="relationRec"
					style="display: none; position: relative; float: right; height: 30px; line-height: 30px; margin-right: 40px;">
				<a href="#" onclick="relationRecv()"> <font color="#0278A9"><fmt:message
					key='edoc.docmark.inner.send' /><fmt:message
					key='edoc.associated.geting' /></font> </a></div>
				<div id="relationSen" align="right" style="display: none;"><a
					href="#" onclick="relationSendV()"><font color=red><fmt:message
					key='edoc.associated.posting' /></font></a></div>
				<c:if test="${showTurnRecInfo == 'true' }">
					<div id="turnRecInfo" style="position: absolute; right: 50px;">
					<a href="#" onclick="showTurnRecInfo()"> <font color="#0278A9"><fmt:message
						key="edoc.turn.rec.info" bundle="${exchangeI18N}" /></font> </a></div>
				</c:if>
				<div style="display: none"><textarea id="xml" cols="40"
					rows="10">
                                     ${formModel.xml}
                                </textarea></div>
				<div style="display: none"><textarea id="xslt" cols="40"
					rows="10">   
                                    ${formModel.xslt}
                                </textarea></div>

				<!-- 愚昧啊,div 加个边框就可以滚动了,要不就把页面撑开了 --> <br />
				<div id="html" name="html"
					style="border: 1px solid; border-color: #FFFFFF; height: 0px;">${formStr}</div>

				<div id="img" name="img" style="height: 0px;"></div>
				<div style="display: none"><textarea name="submitstr"
					id="submitstr" cols="80" rows="20"></textarea></div>

				</div>
				<!-- formAreaDiv --></td>

				<td width="35" id="noteAreaTd" nowrap="nowrap"
					class="h100b page-color"
					<c:if test="${archivedModify!=null}"> style="display:none;"</c:if>>
				<div id="noteMinDiv" style="height: 100%" class="sign-min-bg">
				<div class="sign-min-label-newcoll" style="height: 100%"
					onclick="changeLocation('senderNote');showNoteArea()">
				<div class=more_btn></div>
				<div class="span_text" style="height: 80px; line-height: 30px;"><fmt:message
					key="sender.note.label" /></div>
				</div>
				</div>
				<table id="noteAreaTable" width="100%" height="100%" border="0"
					cellspacing="0" cellpadding="0">
					<tr>
						<td height="25">
						<div id="hiddenPrecessAreaDiv" onclick="hiddenNoteArea()"
							title="<fmt:message key='common.display.hidden.label' bundle='${v3xCommonI18N}' />"></div>
						<script type="text/javascript">
                                        var panels = new ArrayList();
                                        panels.add(new Panel("senderNote", '<fmt:message key="sender.note.label" />'));
                                        //panels.add(new Panel("colQuery", '<fmt:message key="edoc.query.label"/>'));
                                        showPanels(false);
                                    </script></td>
					</tr>
					<tr>
						<td height="25" class="senderNode"><fmt:message
							key="sender.note.label" />(<fmt:message
							key="common.charactor.limit.label" bundle="${v3xCommonI18N}">
							<fmt:param value="500" />
						</fmt:message>)
						<td>
					</tr>
					<tr id="senderNoteTR" style="display: none;">
						<td class="note-textarea-td h100b"><input type="hidden"
							name="policy" value="${policy}"> <textarea
							id="textarea_fy" style="height: 100%" cols="" rows="7"
							name="note" validate="maxLength"
							inputName="<fmt:message key='sender.note.label' />" maxSize="500"
							class="note-textarea wordbreak"><c:out
							value='${formModel.senderOpinion.content}' escapeXml='true' /></textarea></td>
					</tr>
					<tr id="colQueryTR" style="display: none;">
						<td>&nbsp;</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>

<div style="display: none" id="processModeSelectorContainer"></div>
</form>

</div>

<iframe name="personalTempleteIframe" scrolling="no" frameborder="0"
	height="0" width="0" class="hidden"></iframe>

<script type="text/javascript">
//initWorkFlow();
initProcessXml();
hiddenNoteArea();
<c:if test="${isFromTemplate}" >
isFromTemplate = true;
</c:if>
<c:if test="${not isFromTemplate}" >       
isFromTemplate = false;
</c:if>

<c:if test="${!empty formModel.senderOpinion.content}">
changeLocation('senderNote');
showNoteArea();
</c:if>
if(v3x.isIpad){
    var oHtml = document.getElementById('html'); 
    if(oHtml){
        oHtml.style.height = "470px";   
        oHtml.style.overflow = "auto";  
        touchScroll("html");
    }
}

previewFrame('Down');

</script>
<div class="hidden"><iframe id="formIframe" name="formIframe"
	scrolling="no" frameborder="1" height="0px" width="0px"></iframe> <iframe
	name="toXmlFrame" scrolling="no" frameborder="1" height="0px"
	width="0px"></iframe></div>
<script type="text/javascript">
<c:if test="${hasBarCode}">
        if(registerType == "3"){
            if(openBarCodePort()){
                alert("<fmt:message key='edoc.scanningGun.success'/>");
            }else{
                alert("<fmt:message key='edoc.scanningGun.fail'/>");
            }
        }
</c:if>
</script>
</body>
</html>