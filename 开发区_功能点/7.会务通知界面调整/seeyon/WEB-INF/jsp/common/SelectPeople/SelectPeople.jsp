<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<c:set value="${ctp:currentUser().userSSOFrom}" var="topFrameName" />
<html id="selectPeoples" class="h100b ${(topFrameName!=null && topFrameName == true)?'':'over_hidden'}">
<head>
<%@ include file="/WEB-INF/jsp/common/INC/noCache.jsp"%>
<title>${ctp:i18n("selectPeople.page.title")}</title>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/SelectPeople/js/orgDataCenter.js${ctp:resSuffix()}" />"></script>
<script type="text/javascript">
canShowBusinessOrg = true;
if(!$.ctx.hasPlugin("businessorganization")){
	canShowBusinessOrg = false;
}
var ua = navigator.userAgent;
if(ua.indexOf('Android') > -1 || ua.indexOf('iPhone') > -1){
    $("#selectPeoples").removeClass("over_hidden");
}
function getIsCheckSelectedData(){
    try {
        return getBottomRadio("sequence").checked == false;
    }
    catch (e) {
        return true;
    }
}

function OK(){
    var data = null;

    try{
        data = getSelectedPeoples();
    }
    catch(e){
        if(e != 'continue'){
            $.alert({
              msg:e,
              targetWindow:parent.window
            });
        }
        return -1;
    }

    var obj = null;

    var showFlowTypeRadio = getParentWindowData("showFlowTypeRadio");
    if(showFlowTypeRadio){
        var flowtypes = parent.document.getElementsByName("flowtype");
        if(flowtypes == null){
            flowtypes = document.getElementsByName("flowtype");
        }

        for(var i = 0; i < flowtypes.length; i++){
            if(flowtypes[i].checked){
                flowtypes = flowtypes[i].value;
                break;
            }
        }

        obj = [data, flowtypes];
    }
    else{
        obj = data; //Element[]
    }

    var returnValueNeedType = getParentWindowData("returnValueNeedType", true);

    //正常返回数据，如果没有人，则data是一个length为0的数组，即data = []
    var v = {
        "value" :   getIdsString(data, returnValueNeedType),
        "text" :    getNamesString(data, "${ctp:i18n('common.separator.label')}"),
        "obj":      obj
    };

    <c:if test="${param.isFromModel eq 'true'}">
    window.returnValue = v;
    window.close();
    </c:if>

    return v;
}

var basePath = "${pageContext.request.contextPath}";

var topWindow = null;

function findTopWindow(thisObj, iterative){
    //为了适应精灵消息中选人界面(适合本页面引入orgDataCenter.js,并有选人界面相关常量的情况下)
    if(thisObj && thisObj.orgDataCenterFlag){
        topWindow = thisObj;
        return;
    }

    if(thisObj && thisObj.getA8Top().orgDataCenterFlag){
        topWindow = thisObj.getA8Top();
        return;
    }
    var parentObj = null;
    var parentObj = thisObj.getA8Top().dialogArguments;
    if(!parentObj){
    	//OA-154419 部门下有1000人，表单应用绑定选人界面没有表单列表的授权选人界面显示快.
    	//选人界面topWindow 中只有orgdataCenter.js 的数据有用，而每个选人界面所在的窗口，都会加载orgdataCenter.js，所以没有必要在父窗口中取。提高性能
        parentObj = thisObj.getA8Top().opener;
    }

    if(parentObj && parentObj.getA8Top().orgDataCenterFlag){
        topWindow = parentObj.getA8Top();
    }
    else{
        if(iterative > 0){
          findTopWindow(parentObj, iterative - 1);
        }
    }

    return null;
}

//当前页面是dialog
var parentWindowData = null;
if(window.parentDialogObj){
    parentWindowData = window.parentDialogObj["SelectPeopleDialog"].getTransParams();
}
else{
    parentWindowData = window.dialogArguments;
}

try{
	var parentWindow = parentWindowData._window;
    findTopWindow(parentWindow, 5);
}
catch(e){
}
if(topWindow == null){
    topWindow = this;
}

var Constants_Component    = new Properties();

Constants_Component.put(Constants_Account,          "${ctp:i18n('org.account.label')}");
Constants_Component.put(Constants_Department,       "${ctp:i18n('org.department.label')}");
Constants_Component.put(Constants_Team,             "${ctp:i18n('org.team.label')}");
Constants_Component.put(Constants_Post,             "${ctp:i18n('org.post.label')}");
Constants_Component.put(Constants_Level,            "${ctp:i18n('org.level.label')}");
Constants_Component.put(Constants_Member,           "${ctp:i18n('org.member.label')}");
Constants_Component.put(Constants_Role,             "${ctp:i18n('org.role.label')}");
Constants_Component.put(Constants_Outworker,        "${ctp:i18n('org.outworker.label')}");
Constants_Component.put(Constants_ExchangeAccount,  "${ctp:i18n('org.exchangeAccount.label')}");
Constants_Component.put(Constants_OrgTeam,          "${ctp:i18n('org.orgTeam.label')}");
Constants_Component.put(Constants_RelatePeople,     "${ctp:i18n('org.RelatePeople.label')}");
Constants_Component.put(Constants_FormField,        "${ctp:i18n('form.selectPeople.extend')}");
Constants_Component.put(Constants_OfficeField,      "${ctp:i18n('office.selectPeople.extend')}");
Constants_Component.put(Constants_Admin,            "${ctp:i18n('org.admin.label')}");
Constants_Component.put(Constants_Node,             "${ctp:i18n('org.node.label')}");
Constants_Component.put(Constants_WfSuperNode,      "${ctp:i18n('workflow.node.supernode.name')}");
Constants_Component.put(Constants_OrgRecent,        "${ctp:i18n('org.orgrecent.label')}");
Constants_Component.put(Constants_OrgUp,            "${ctp:i18n('org.metadata.access_permission.up')}");
Constants_Component.put(Constants_WFDynamicForm,    "${ctp:i18n('wfdynamicform.selectpeple.label')}");
Constants_Component.put(Constants_JoinOrganization, "${ctp:i18n('org.JoinOrganization.label')}");
Constants_Component.put(Constants_JoinAccount,      "${ctp:i18n('org.JoinAccount.label')}");
Constants_Component.put(Constants_JoinAccountTag,   "${ctp:i18n('org.JoinAccountTag.label')}");
Constants_Component.put(Constants_JoinPost,         "${ctp:i18n('org.JoinPost.label')}");
Constants_Component.put(Constants_Guest,            "${ctp:i18n('org.guest.label')}");
Constants_Component.put(Constants_MemberMetadataTag,"${ctp:i18n('org.member.metadatatag.label')}");
Constants_Component.put(Constants_BusinessDepartment,"${ctp:i18n('selectPeople.bussinessOraganization.label')}");
Constants_Component.put(Constants_BusinessAccount,"${ctp:i18n('selectPeople.bussinessOraganization.account.label')}");
Constants_Component.put(Constants_BusinessRole,"${ctp:i18n('selectPeople.bussinessOraganization.role.label')}");


var PeopleRelate_TypeName = {
    1 : "${ctp:i18n('relate.type.leader')}",
    2 : "${ctp:i18n('relate.type.assistant')}",
    3 : "${ctp:i18n('relate.type.junior')}",
    4 : "${ctp:i18n('relate.type.confrere')}"
};


topWindow.PeopleRelate_TypeName = PeopleRelate_TypeName;

var isAdministrator = ${CurrentUser.administrator}; //单位管理员
var groupAdmin = ${CurrentUser.groupAdmin} || ${CurrentUser.auditAdmin}; //集团管理员 审计管理员
var systemAdmin = ${CurrentUser.systemAdmin}; //系统管理员

var isAdmin = isAdministrator || groupAdmin || systemAdmin;

var isInternal = ${CurrentUser.internal}; //是否是内部人员
var isV5Member = ${CurrentUser.externalType == 0}; //是否是V5人员
var isVjoinMember = ${CurrentUser.externalType == 1}; //是否是Vjoin人员
var isSubVjoinAdmin = ${isSubVjoinAdmin};//是否是子机构管理员

var myAccountId = "${CurrentUser.accountId}";
var loginAccountId = "${CurrentUser.loginAccount}";

var isGroupAccessable = ${isGroupAccessable};
var accessableRootAccountId = [${accessableRootAccountId}];
</script>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/SelectPeople/css/css.css${ctp:resSuffix()}" />" />
<link rel="stylesheet" type="text/css" href="<c:url value="/common/SelectPeople/tree/xtree.css${ctp:resSuffix()}" />" />

<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/SelectPeople/js/Panel.js${ctp:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/SelectPeople/tree/xtree.js${ctp:resSuffix()}" />"></script>

<c:set value="${v3x:getSysFlagByName('selectPeople_showAccounts')}" var="showAccounts" />
<c:set value="${CurrentUser.loginAccount}" var="defaultLoginAccount" />
<c:if test="${CurrentUser.externalType != 0 || CurrentUser.systemAdmin || CurrentUser.groupAdmin || CurrentUser.auditAdmin}">
    <c:set value="${firstAccountId}" var="defaultLoginAccount" />
</c:if>
<c:if test="${not empty param.accountId}">
    <c:set value="${param.accountId}" var="defaultLoginAccount" />
</c:if>

<script type="text/javascript">

currentMemberId  = "${CurrentUser.id}";

//是否是多组织吧
var isGroupVer = ${v3x:getSysFlagByName("sys_isGroupVer")};

ShowMe       = getParentWindowData("showMe", true);
SelectType   = getParentWindowData("selectType", "");
Panels       = getParentWindowData("panels", "");
//首先聚焦那个页签 lilong 20140411
tempNowPanel = Constants_Panels.get(getParentWindowData("nowPanel", ""));
showRecent     = getParentWindowData("showRecent", true);

accountId    = "${defaultLoginAccount}" || getParentWindowData("accountId") || accountId;

memberId     = getParentWindowData("memberId");
departmentId = getParentWindowData("departmentId") || "${currentDepartment}";
postId       = getParentWindowData("postId");
levelId      = getParentWindowData("levelId");

maxSize = getParentWindowData("maxSize", 1000);
minSize = getParentWindowData("minSize", 1);

spId = "-1";

selectTypes.addAll(SelectType.split(","));

panels.addAll(Panels.split(","));

showAccountPanel = getParentWindowData("showAccountPanel");

if(showAccountPanel == false){
    panels.remove(Constants_Account);
}

selectTypes.remove(Constants_RelatePeople);

//为了让cap4在企业版下选择单位,这个设置暂时舍弃，看会不会有问题
//CAPF-12641  应用包导入，集团版的包设置有关单位，导入到企业版，重定向无法选单位，直接阻塞导入了
/* <c:if test="${!showAccounts}">
    panels.remove(Constants_Account);
</c:if> */


<c:if test="${!ctp:hasPlugin('vjoin') || !showVJoinPanel}">
if (panels.size() > 1) {
    panels.remove(Constants_JoinOrganization);
    panels.remove(Constants_JoinAccount);
    panels.remove(Constants_JoinAccountTag);
    panels.remove(Constants_JoinPost);
}
</c:if>

if(!canShowBusinessOrg || (isVjoinMember && isVjoinMember == true)){
	if (panels.size() > 0) {
	    panels.remove(Constants_BusinessDepartment);
	    panels.remove(Constants_BusinessAccount);
	    panels.remove(Constants_BusinessRole);
	}
}

var currentVjoinAccountId = null;//当前显示的V-Join单位ID
<c:forEach items="${allAccounts}" var="ac">
    allAccounts.put("${ac.id}", new Account("${ac.id}", "${ac.superior}", "${ac.path}", "${v3x:escapeJavascript(ac.name)}", true, "${v3x:escapeJavascript(ac.shortName)}", "${ac.levelScope}", "", "${ac.externalType}"));
    <c:if test="${ac.group == true}">
    rootAccount = new Account("${ac.id}", "${ac.superior}", "${ac.path}", "${v3x:escapeJavascript(ac.name)}", true, "${v3x:escapeJavascript(ac.shortName)}", "${ac.levelScope}", "", "${ac.externalType}");
    </c:if>
    <c:if test="${ac.externalType == '3'}">
    currentVjoinAccountId = "${ac.id}";
    </c:if>
</c:forEach>

<c:forEach items="${accessableAccounts}" var="account">
    accessableAccountIds.add(new Object({id:'${account.id}', superior:'${account.superior}'}));
</c:forEach>
for(var i = 0; i < accessableAccountIds.size(); i++){
    var accessableAccountId = accessableAccountIds.get(i).id;
    var ac = allAccounts.get(accessableAccountId);
    if(ac){
        accessableAccounts.put(accessableAccountId, ac);

        var parentAccountId = accessableAccountIds.get(i).superior;
        var pac = allAccounts.get(parentAccountId);
        if(pac){
            pac.accessChildren.add(ac);
        }
    }
}

<c:forEach items="${groupLevels}" var="l" varStatus="s">
groupLevels["${l.id}"] = ${s.index + 1};
</c:forEach>

function openAddExternalUnitDlg(){

    if(typeof(getA8Top().$.dialog)!='undefined'){
    getA8Top().win123 = getA8Top().$.dialog({
      id : "saveAsTeamDialog",
      url :"/seeyon/exchangeEdoc.do?method=addExchangeAccountFromEodc",
      width : 380,
      height: 300,
      title : $.i18n("selectPeople.externalAccountAdd.lable.js"),
      isDrag :false,
      targetWindow : getA8Top().window,
      transParams : {
          _window : window,
          _callbackRefresh:refreshEdocExternalUnit
      }
    });
    }else if(typeof(getA8Top().v3x.openDialog)!='undefined'){
     getA8Top().win123 = getA8Top().v3x.openDialog({
      id : "saveAsTeamDialog",
      url :"/seeyon/exchangeEdoc.do?method=addExchangeAccountFromEodc",
      width : 380,
      height: 300,
      isDrag :false,
      targetWindow : getA8Top().window,
      transParams : {
          _window : window,
          _callbackRefresh:refreshEdocExternalUnit
      }
    });
    }


}
function refreshEdocExternalUnit(){
    topWindow.initOrgModel(accountId, currentMemberId, "");
    selPanel("ExchangeAccount");
}

</script>
</head>
<body class="font_size12 h100b ${(topFrameName!=null && topFrameName == true)?'':'over_hidden'} page_color">
<script type="text/javascript">

    var proce = $.progressBar();

    var setting = {
        view: {
            dblClickExpand: false,
            selectedMulti: false
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            onClick: onClick
        }
    };

    function onClick(e, treeId, treeNode) {
        var nodes = $("#accessableAccounts4Tree").treeObj().getSelectedNodes();
        var aId = nodes[0].id;
        var aName = nodes[0].name || "";

        $("#currentAccountId").val(aName.getLimitLength(16));
        $("#currentAccountId").attr("title", aName);

        hideMenu();
        chanageAccount(aId);
    }

    function showMenu() {
    	var accountContentOffsetTop = $("#accountContent")[0].offsetTop;
    	if(accountContentOffsetTop > 0){
    		//已经展开，就收起。
    		hideMenu();
    		return;
    	}

        var selectDisabled = $('#select_input_div').prop("disabled");
        if(selectDisabled == true || selectDisabled == "true" || selectDisabled == "disabled"){
            return;
        }
        var accountObj = $("#select_input_div").parent();//#select_input_div_parent
        var accountOffset = accountObj.offset();
        $("#accountContent").css({left:accountOffset.left + "px", top:accountOffset.top + accountObj.outerHeight() + "px"}).show();
        $("#accountContentIframe").css({left:accountOffset.left + "px", top:accountOffset.top + accountObj.outerHeight() + "px"}).show();
        if($("#AreaTop1_Post").css("display") != "none"){
            $("#accountContent,#accountContentIframe,#accountContentChild").height(410 - $("#areaTopList1_Post").height());
        }else{
            $("#accountContent,#accountContentIframe,#accountContentChild").height(410);
        }

        var treeObj = $.fn.zTree.getZTreeObj("accessableAccounts4Tree");
        var node = treeObj.getNodeByParam("id", accountId, null);
        if(node){
            treeObj.expandNode(node, true);
            treeObj.selectNode(node);
        }

        $("body").bind("mousedown", onBodyDown);
    }

    function hideMenu() {
        $("#accountContent").hide();
        $("#accountContentIframe").hide();
        $("body").unbind("mousedown", onBodyDown);
    }

    function onBodyDown(event) {
        if (!(event.target.id == "accountContent" || $(event.target).parents("#accountContent").length > 0 || event.target.id == "currentAccountId")) {
            hideMenu();
        }
    }

    $(document).ready(function(){
        <c:if test="${showAccounts}">
        $("#accessableAccounts4Tree").tree({
            idKey   : "id",
            pIdKey  : "superior",
            nameKey : "name",
            onClick : onClick
        });
        </c:if>
    });

</script>
<div id="accountContent" style="display:none; position:absolute;background: #ffffff; width:368px; height:410px;;z-index: 10">
    <div id="accountContentChild" style="margin-top:0px; width:368px; height: 400px; display: block; overflow: auto">
        <ul id="accessableAccounts4Tree" class="ztree"></ul>
    </div>
</div>
<iframe id="accountContentIframe" height="400" width="328" frameborder="0" marginheight="0" marginwidth="0" scrolling="no" style="display:none; position:absolute;background: #ffffff; width:368px; height:400px;z-index: 9"></iframe>
<table id="selectPeopleTable" width="100%" height="100%" border="0" class="bg-body1" align="center" cellpadding="0" cellspacing="0" style="display: none;">
    <tr valign="top">
        <td>
        <table width="100%" border="0" height="100%" cellpadding="0" cellspacing="0">
            <tr>
                <td height="33" valign="bottom" class="padding_l_15 padding_r_15">
                    <table border="0" cellpadding="0" width="100%" cellspacing="0">
                        <tr valign="top" id="tdPanel">
                            <td width="1" height="26"></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td class="padding_l_15 padding_r_15 padding_t_10">
                    <table border="0" width="100%" height="100%" cellspacing="0" cellpadding="0">
                        <tr>
                            <td valign="top">
                        <div style="border:1px solid #D8DBDD; width:368px; background:#fff;">
                                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                    <tr style="display: none;" id="AreaTop1_Post">
                                        <td>
                                            <select style="width:368px; overflow:auto; border:none; font-family: Arial, Helvetica, sans-serif" class="codecfg" codecfg="codeId: 'organization_post_types',showLevel:'first'" id="areaTopList1_Post" name="areaTopList1_Post" onChange="initList('Post')">
                                                <option value="AllPosts">${ctp:i18n("selectPeople.post.all")}</option>
                                            </select>
                                        </td>
                                    </tr>

                                    <tr style="display: none;" id="AreaTop1_RelatePeople">
                                        <td>
                                            <select style="width:368px; overflow:auto; border:none; font-family: Arial, Helvetica, sans-serif" id="areaTopList1_RelatePeople" name="areaTopList1_RelatePeople" onChange="initList('RelatePeople')">
                                                <option value="All">${ctp:i18n("selectPeople.relatepeople.all")}</option>
                                                <option value="1">${ctp:i18n("relate.type.leader")}</option>
                                                <option value="2">${ctp:i18n("relate.type.assistant")}</option>
                                                <option value="3">${ctp:i18n("relate.type.junior")}</option>
                                                <option value="4">${ctp:i18n("relate.type.confrere")}</option>
                                            </select>
                                        </td>
                                    </tr>
                                     <tr style="display: none;" id="AreaTop1_WFDynamicForm">
                                        <td>
                                            <select style="width:368px; overflow:auto; border:none; font-family: Arial, Helvetica, sans-serif" id="areaTopList1_WFDynamicForm" name="areaTopList1_WFDynamicForm" onChange="initList('WFDynamicForm')">
                                            </select>
                                        </td>
                                    </tr>
                                    <tr valign="top">
                                        <td>
			                                <table width="100%" border="0" cellspacing="0" cellpadding="0">
			                                    <tr>
			                                        <td id="select_input_div_parent" height="46" bgColor="#F6F9F9">
			                                        <c:if test="${showAccounts}">
			                                            <div id="select_input_div" class="border_all margin_l_5 padding_l_5 padding_r_5 bg_color_white" onClick="showMenu(); return false;" style="width: 145px;">
			                                                <input name="currentAccountId" id="currentAccountId" type="text" class="select_input_width arrow_6_b hand" style="height: 24px; border: 0px; width: 143px;" readonly="readonly" value="" />
			                                            </div>
			                                        </c:if>
			                                        </td>
			                                        <td height="46" bgColor="#F6F9F9" align="right">
			                                            <ul class="common_search" id="common_search_ul" style="width: 190px;">
			                                                <li id="inputBorder" class="common_search_input"><input style="width: 156px;" id="q" class="search_input"  type="text" onclick="checkSearchAlt()" onfocus="checkSearchAlt()" onblur="checkSearchAlt(true)" onkeydown="if(event.keyCode == 13)searchItems()"></li>
			                                                <li><a class="common_button search_buttonHand" onclick="searchItems()"><em></em></a></li>
			                                            </ul>
			                                        </td>
			                                        <td height="46" bgColor="#F6F9F9" nowrap="nowrap" class="padding_l_5">
			                                            <c:if test="${showAccounts}">
			                                                <span id="seachGroupMember">
			                                                    <label for="seachGroup"><input type="checkbox" id="seachGroup" name="seachGroup" onClick="hideSeparatorDIV(this)" /><span>&nbsp;${ctp:i18n("selectPeople.search.group.label")}</span></label>
			                                                </span>
			                                            </c:if>
			                                        </td>
			                                    </tr>
			                                   <!-- 多维组织部门树 -->
			                                    <tr style="display: none;" id="AreaTop1_BusinessDepartment">
			                                        <td height="36" bgColor="#F6F9F9" colspan="3">
			                                            <select style="width:368px; overflow:auto; border:none; font-family: Arial, Helvetica, sans-serif" id="areaTopList1_BusinessDepartment" name="areaTopList1_BusinessDepartment" onChange="initBusinessorganization(currentAccountId,this.value)">
			                                            </select>
			                                        </td>
			                                    </tr>
			                                    <tr>
			                                        <td id="Area1" style="border:none;" colspan="3"></td>
			                                    </tr>
			                                </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td id="Separator1" valign="middle" style="text-align:left;">
                                            <table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0">
                                                <tr>
                                                    <td id="" align="center" class="border_t w100b margin_0 padding_0" style="font-size:2px; background:#F6F9F9;">
                                        <div id="Separator1_0" style="height:8px;font-size:2px;text-align: center;">
                                           <span style="width:30px; height:8px; display:block; background:#D1D4DB; border-radius:0 0 3px 3px; display: inline-block;"><span class="ico16 arrow_1_t margin_0 padding_0" style="width:25px; height: 8px;background-position:0px -504px;vertical-align: top;" onclick="hiddenArea1()"></span></span>
                                        </div>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td id="Separator1_1" class="padding_l_5" bgColor="#F6F9F9" height="24px">
                                                        <div id="separatorDIV_Department" style="display: none;">
                                                            <label for="sep_per"  id="sep_per_l"  style="display: none;" class="margin_t_5 hand"><input type="radio" name="sep" id="sep_per"  class="radio_com" value="Member" onClick="showList2OfDep(this.value)" checked="checked"><span>${ctp:i18n("org.member.label")}</span></label>&nbsp;
                                                            <label for="sep_post" id="sep_post_l" style="display: none;" class="margin_t_5 hand"><input type="radio" name="sep" id="sep_post" class="radio_com" value="Post" onClick="showList2OfDep(this.value)"><span>${ctp:i18n("org.post.label")}</span></label>&nbsp;
                                                            <label for="sep_role" id="sep_role_l" style="display: none;" class="margin_t_5 hand"><input type="radio" name="sep" id="sep_role" class="radio_com" value="Role" onClick="showList2OfDep(this.value)"><span>${ctp:i18n("org.role.label")}</span></label>
                                                        </div>
                                                        <div id="separatorDIV_Team" style="display: none;">
                                                            <label for="sep_team"><input type="checkbox" name="sep_team" id="sep_team" onClick="showTeamRelativeMembers()">&nbsp;&nbsp;<span>${ctp:i18n("org.team.showRelative.label")}</span></label>
                                                        </div>
                                                        <div id="separatorDIV_Post" style="display: none;">
                                                            <span onClick="showDetailPost()" class="hand">${ctp:i18n("selectPeople.post.showDetail.label")}</span>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td valign="top">
                                        <div id="Separator1_2" class="border_b" style="height:8px;font-size:2px;text-align: center; background:#F6F9F9;">
                                           <span style="width:30px; height:8px; display:block; background:#D1D4DB; border-radius:3px 3px 0 0; display: inline-block;"><span class="ico16 arrow_1_b margin_0 padding_0" style="width:25px; height: 8px;background-position:-24px -504px;vertical-align: top;" onclick="hiddenArea2()"></span></span>
                                        </div>
                                        <div id="Area2">
                                            <c:choose>
                                                <c:when test="${v3x:getBrowserFlagByUser('selectPeopleShowType', CurrentUser)==true}">
                                                    <select id="memberDataBodyOrginal" multiple="multiple" style="width:368px;overflow:auto; height:190px; border:none;" size="13"></select>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="div-select" id="memberDataBodyOrginal"></div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        </td>
                                    </tr>
                                </table>
                        </div>
                            </td>
                            <td width="50" align="center">
                                <p>
                                    <span class="select_selected" title='${ctp:i18n("selectPeople.alt.select")}' onclick="selectOne()"></span>
                                </p>
                                <br/>
                                <p>
                                    <span class="select_unselect" title='${ctp:i18n("selectPeople.alt.unselect")}' onclick="removeOne()"></span>
                                </p>
                            </td>
                            <td style="height:406px;" valign="top">
                                <c:choose>
                                    <c:when test="${v3x:getBrowserFlagByUser('selectPeopleShowType', CurrentUser)==true}">
                            <div style="border:1px solid #D8DBDD; width:328px;">
                            <div style="height:46px; background:#F6F9F9;">
                                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td height="46" width="100" nowrap="nowrap" align="center" id="saveAsTeamDiv">
                                            <c:if test="${v3x:getBrowserFlagByUser('SelectPeople', CurrentUser)==true && CurrentUser.externalType == 0}">
                                                <a class="color_gray2" onClick="saveAsTeam()">[${ctp:i18n("selectPeople.saveAsTeam.lable")}]</a>
                                            </c:if>
                                        </td>
                                        <td height="46" nowrap="nowrap" align="right" class="padding_r_10">
                                            <ul id="SearchButton3_1" class="common_search right hidden">
                                                <li id="q3Li" class="common_search_input"><input id="q3" class="search_input" style="width: 100px" type="text" onkeydown="if(event.keyCode == 13)searchItems3()" onblur="showSearchButton3(false)" value=""></li>
                                                <li><a class="common_button right search_buttonHand" onclick="searchItems3()" id="ok_msg_btn_first2" title="${ctp:i18n('selectPeople.search3.alt')}"><em></em></a></li>
                                            </ul>
                                            <div id="SearchButton3_2" class="search_b hand" onclick="showSearchButton3(true)" title="${ctp:i18n('selectPeople.search3.alt')}"></div>
                                        </td>

                                        <td height="46" nowrap="nowrap">
                                            <a class="like-a" onClick="openAddExternalUnitDlg()" style="display: none;" id="addExternalAccountDiv">${ctp:i18n("selectPeople.externalAccountAdd.lable")} </a>
                                        </td>


                                    </tr>
                                </table>
                            </div>
                                        <select id="List3" onclick="" ondblclick="removeOne(this.value, this)" multiple="multiple" style="padding-top: 3px;height:395px;width:328px;overflow:auto; border:none;" size="28" >
                                        </select>
                            </div>
                                    </c:when>
                                    <c:otherwise>
                            <div style="height:46px; background:#F6F9F9;">
                                <!-- 这里一样 -->
                            </div>
                                        <div id="List3" class="div-select-list3"></div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td width="40" align="center">
                                <p>
                                    <span class="sort_up" title='${ctp:i18n("selectPeople.alt.up")}' onclick="exchangeList3Item('up')"></span>
                                </p>
                                <br/>
                                <p>
                                    <span class="sort_down" title='${ctp:i18n("selectPeople.alt.down")}' onclick="exchangeList3Item('down')"></span>
                                </p>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        </td>
    </tr>
    <c:if test="${param.isFromModel eq 'true'}">
    <tr>
        <td height="44" id="btns" class="bg-button-select" style="padding-right:30px;">
            <table width="100%" border="0" height="100%" align="center" cellpadding="0" cellspacing="0">
                <tr>
                    <td width="60%" align="left">
                        <table id="flowTypeDiv" class="hidden" border="0" height="100%" align="left" cellpadding="0" cellspacing="0" style="padding-top: 7px;">
                            <td id="concurrentType">&nbsp;&nbsp;&nbsp;&nbsp;
                                <label for="concurrent">
                                    <input id="concurrent" name="flowtype" type="radio" value="1" checked><span>${ctp:i18n("selectPeople.flowtype.concurrent.lable")}</span>
                                </label>
                            </td>
                            <td id="sequenceType">&nbsp;&nbsp;&nbsp;
                                <label for="sequence">
                                    <input id="sequence" name="flowtype" type="radio" value="0"><span>${ctp:i18n("selectPeople.flowtype.sequence.lable")}</span>
                                </label>
                            </td>
                            <td id="multipleType">&nbsp;&nbsp;&nbsp;
                                <label for="multiple">
                                    <input id="multiple" name="flowtype" type="radio" value="2"><span>${ctp:i18n("selectPeople.flowtype.multiple.lable")}</span>
                                </label>
                            </td>
                            <td id="colAssignType">&nbsp;&nbsp;&nbsp;
                                <label for="colAssign">
                                    <input id="colAssign" name="flowtype" type="radio" value="3"><span>${ctp:i18n("selectPeople.flowtype.colAssign.lable")}</span>
                                </label>
                            </td>
                        </table>
                    </td>
                    <td align="right">
                        <input name="Submit" type="button" onClick="OK();" class="common_button common_button_gray"
                            value='${ctp:i18n("common.button.ok.label")}'>&nbsp;&nbsp;
                        <input name="close" type="button" onclick="window.close()" class="common_button common_button_gray"
                            value='${ctp:i18n("common.button.cancel.label")}'>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </c:if>
</table>
<script type="text/javascript">

var showFlowText = getParentWindowData("showFlowText");

function getBottomRadio(name){
	if(showFlowText == true){//优先显示文本内容，没有文本内容，再处理显示选项（并发，串发..） 许树祥
		return;
	}
    var pObj = parent.document.getElementById(name);
    return pObj == null ? document.getElementById(name) : pObj;
}

function getBottomText(name){
    var pObj = parent.document.getElementById(name);
    return pObj == null ? document.getElementById(name) : pObj;
}

if(showFlowText == true){
	getBottomText("flowTextDiv").className = "";
    var flowTextSpan = getBottomText("flowText");
    flowTextSpan.innerHTML=getParentWindowData("flowText");
}

var showFlowTypeRadio = getParentWindowData("showFlowTypeRadio");
if(showFlowTypeRadio == true){
    getBottomRadio("flowTypeDiv").className = "";
}

var invalidationMultipleRadio = getParentWindowData("invalidationMultipleRadio");
if(invalidationMultipleRadio == true){
    getBottomRadio("multipleType").disabled = true;
}

var hiddenMultipleRadio = getParentWindowData("hiddenMultipleRadio");
if(hiddenMultipleRadio == true){
    getBottomRadio("multipleType").style.display = "none";
}

var invalidationColAssignRadio = getParentWindowData("invalidationColAssignRadio");
if(invalidationColAssignRadio == true){
    getBottomRadio("colAssignType").disabled = true;
}

var hiddenColAssignRadio = getParentWindowData("hiddenColAssignRadio");
if(hiddenColAssignRadio == true){
    getBottomRadio("colAssignType").style.display = "none";
}

setRadio("concurrentType");
setRadio("sequenceType");
setRadio("multipleType");
setRadio("colAssignType");

function setRadio(id){
	var obj = getBottomRadio(id);
	if(obj && obj.style){
		obj.style.color="#fff";
		obj.style.alignContent="right";
		obj.style.fontSize="12px";
	}
}

var _flowtype = getParentWindowData("flowtype");
if(_flowtype){
    var flowtypeObj = getBottomRadio(_flowtype);
    if(flowtypeObj){
        flowtypeObj.checked = true;
    }
}

</script>
</body>
</html>
