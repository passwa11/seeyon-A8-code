/*----------------------------------------------------------------------------\
|                                Cross Panel 1.0                              |
|-----------------------------------------------------------------------------|
|                       Created by Tanmf (tanmf@seeyon.com)                   |
|                    For UFIDA-Seeyon (http://www.seeyon.com/)                |
|-----------------------------------------------------------------------------|
| A utility will be used for selected people(Member,Department,Team,Post,     |
| Levle,Organization,Special rules etc.).                                     |
|-----------------------------------------------------------------------------|
|                            Copyright (c) 2006 Tanmf                         |
|-----------------------------------------------------------------------------|
| Dependencies:                                                               |
|-----------------------------------------------------------------------------|
| 2006-05-27 | Original Version Posted.                                       |
| 2006-06-06 | Added expanding the current user's department or the given     |
|            | department trees when initiate department-tree.                |
| 2006-06-08 | Extends single select-people mode.                             |
| 2006-06-08 | Support to other Web-Browsers, e.g: Firefox,Opera etc,         |
|            | according as W3C Standard.                                     |
| 2006-09-29 | 支持显示所有面板以及选择所有类型的参数                                |
|-----------------------------------------------------------------------------|
| 主窗口可配置的参数：                                                            |
| 1. elements_${id}               Element[]    原有数据，默认为null              |
| 2. showOriginalElement_${id}    true|false   是否回显原有数据，默认为true       |
| 3. hiddenSaveAsTeam_${id}       true|false   是否隐藏“另存为组”，默认为false    |
| 4. hiddenMultipleRadio_${id}    true|false   是否隐藏“多层”按钮，默认为false    |
| 5. excludeElements_${id}        Element[]    不在被选框中显示  默认为null       |
| 6. isNeedCheckLevelScope_${id}  true|false   是否进行职务级别范围验证  默认true  |
| 7. onlyLoginAccount_${id}       true|false   是否只显示登录单位  默认false       |
| 8. showAccountShortname_${id}   yes|no|auto  是否只一直显示登录简称  默认auto    |
| 9. showConcurrentMember_${id}   true|false   是否显示兼职人员（只外单位）  true  |
| 9. showSecondMember_${id}       true|false   是否显示副岗人员（只外单位）  true  |
|10. hiddenPostOfDepartment_${id} true|false   是否隐藏部门下的岗位 默认false     |
|11. hiddenRoleOfDepartment_${id} true|false   是否隐藏部门下的角色 默认false     |
|12. onlyCurrentDepartment_${id}  true|false   是否仅显示当前部门 默认false       |
|13. showDeptPanelOfOutworker_${id} true|false 当是外部人员时，显示部门面板 默认false|
|14. unallowedSelectEmptyGroup_${id} true|false不允许选择空的组、部门, 默认false   |
|15. showTeamType_${id}            "1,2,3"     需要显示的组类型1-个人,2-系统,3-项目, 默认null，表示所有|
|16. hiddenOtherMemberOfTeam_${id} true|false  是否隐藏组下的外单人员(兼职保留)，默认false   |
|17. hiddenAccountIds_${id}        "1,2,3"     隐藏的单位，将不在单位下拉中出现           |
     showAccountIds_${id}          "1,2,3"     显示的单位，将在单位下拉中出现           |
|18. isCanSelectGroupAccount_${id} true|false  是否可以选择集团单位，默认true      |
|19. showAllOuterDepartment_${id}  true|false  是否显示所有的外部部门，默认false   |
|20. hiddenRootAccount_${id}       true|false   是否隐藏集团单位，默认false        |
|21. hiddenGroupLevel_${id}        true|false    是否隐藏集团职务级别，默认false   |
|22. showDepartmentsOfTree_${id}   "部门Id,"    部门树上可以显示的部门                                    |
|23. showRelativeRole_${id}        true|false  显示流程相对角色，默认false         |
|24. showFixedRole_${id}           true|false  显示特定角色，默认false             |
|25. hiddenAddExternalAccount_${id} true|false  显示增加外部单位连接，默认false     |
|26. showDepartmentMember4Search_${id} true|false  部门查询可用时，是否显示部门下面的成员，默认false，不显示                              |
|27. isAllowContainsChildDept_${id} true|false 在部门面板选择部门时，是否允许同时选择父部门和子部门，默认为false，不允许|
|28. isConfirmExcludeSubDepartment_${id} true|false 选择部门时，是否提示“是否包含子部门”，默认false即包含子部门                       |
|29. returnValueNeedType_${id}      true|false 返回值是否需要带类型，默认true，如果false则只返回Id，只用于单一选择               |
|30. includeElements_${id}         Element[]   备选的数据范围                                               |
|31. showAdminTypes_${id}          "SystemAdmin,AuditAdmin,GroupAdmin,AccountAdministrator,AccountAdministrator_-81232345"   需要显示的管理员,如果不指定，表示显示所有|
|32. extParameters_${id}           文本           用于后台页签显示的扩展参数,通过(String)AppContext.gettThreadContext("extParameters")获取  |
|33. isCheckInclusionRelations     true|false  是否检查已选数据的包含关系，默认true |
|34. hiddenFormFieldRole           true|false  是否隐藏表单控件下的角色，默认false |
|35. isNotShowNoMemberConfirm      true|false 是否不显示“xxx部门下无人，是否继续选择”的提示语，默认false，即显示。
|36. isNotCheckDuplicateData       true|false 是否不检查重复数据，默认false，即检查。
|37. showRecent_${id}              true|false 是否显示最近页签，默认为true，即显示。
|38. notShowAccountRole            true|false （相对角色，表单控件下） 是否显示单位角色，默认按照集团管理员勾选‘选人界面’的设置来。
|39. showExternalType              1:外部机构 2:外部单位 默认为null  都显示
|40. onlyShowChildrenAccount       true|false 是否只显示子单位  默认false 都显示（切换单位和单位树 ）
|41. alwaysShowPanels              始终显示的页签，不受切换单位时隐藏某些页签的限制（hiddenOnChanageAccount）
|42. unallowedChangeBusinessAccount         true|false   不允许切换业务线，  默认false       |
|43. currentBusinessAccount_${id}        默认显示的业务线      |
|44. returnMemberWithDept_${id}    true|false 返回人员信息是否包含部门信息（在哪个部门下选到的，部门信息就是哪个部门，同一个人在不同部门下可以同时选择）      | 返回的数据格式为：Member|部门id#人员id
|45. allowSeachGroup               true|false 是否允许全集团查询人员，缺省允许 true.
|49. alwaysShowBusiness_{ids}      总是显示的多维组织的id
|-----------------------------------------------------------------------------|
| 关键方法：                                                                    |
| 1. getSelectedPeoples() 点击确认按钮，返回数据                                  |
| 2. searchItems() 搜索                                                        |
| 3. preReturnValueFun_${Element[]} [false, message] 点击"确定"按钮前的回调函数   |
|-----------------------------------------------------------------------------|
| Created 2006-05-27 | All changes are in the log above. | Updated 2010-04-28 |
\----------------------------------------------------------------------------*/

var select2_tag_prefix_fullWin = '<select id="memberDataBody" ondblclick="selectOneMember(this)" onchange="listenermemberDataBody(this)" multiple="multiple" style="padding-top: 1px;overflow:auto; width:100%;height:100%">';
var select2_tag_prefix = '<select id="memberDataBody" ondblclick="selectOneMember(this)" onchange="listenermemberDataBody(this)" multiple="multiple" style="border:none; padding-top: 1px;overflow:auto; width:368px; height:185px">';
var select2_tag_orgTeam_prefix = '<select id="memberDataBody" disabled="disabled"  multiple="multiple" style="border:none; padding-top: 1px;overflow:auto; width:363px; height:190px">';
var select2_tag_subfix = "</select>";
var selectTeam2_tag_prefix = '<select id="memberDataBody" multiple="multiple" style="border:none; padding-top: 1px;overflow:auto; width:100%; height:190px">';
//div展示人员select
var memberDataBody_div = '<div class="div-select" id="memberDataBody">';
//组div展示
var teamMemberDataBody_div = '<div class="div-select" id="memberDataBody">';
//div后缀
var memberDataBody_div_end = '</div>';
var temp_Div = null;

//~ 固定角色，诸如：AccountManager AccountAdmin account_exchange account_edoccreate FormAdmin HrAdmin ProjectBuild DepManager DepAdmin department_exchange
var FixedRoles = new ArrayList();
{
    FixedRoles.add("DepManager");
    FixedRoles.add("DepLeader");
}

var ua = navigator.userAgent;
var browserIsMSIE = (navigator.appName == "Microsoft Internet Explorer") || ua.indexOf('Trident') != -1;
var browserIsEDGE = ua.indexOf('Edge') != -1;

var selectedPeopleElements = new Properties();
//新增机构组部门是否重复校验
var orgTeamDepartment = new Properties();
//~ 选人的标示
var spId = null;

var pageSize = 20;//首次加载的条数，如果需要显示全部，可以点击‘加载全部’。数据量太大，ie下渲染太慢。
var pagingParam = new Properties();//分页需要的数据参数
//~ 最大选择数， -1表示没有限制
var maxSize = -1;

//~ 最小选择数， -1表示没有限制
var minSize = -1;

//~ List1当前选择的对象
var nowSelectedList1Item = null;

//~ 区域12的开关状态
var area12Status = "M"; //M-居中、T-在上面、B-在下面

//~ Tree模型
var tree = null;

//~ 是否显示到子部门，当参数departmentId不为空时
var treeInMyDepart = false;

//~ 当前被选择的内容项
var tempNowSelected = new ArrayList();

//~ 当前显示的Panel
var tempNowPanel = null;

//~ 是否显示最近联系人页签，默认true显示，false不显示
var showRecent = true;

//~ 当前显示的单位
var currentAccount = null;

//~ 当前单位的职务级别范围
var currentAccountLevelScope = -1;

//~ 当前显示的单位Id
var currentAccountId = null;
;

//~ 当前登录者的Id
var currentMemberId = null;

//~ 当前登录者
var currentMember = null;

//~ 当前登录者的职务级别数
var currentMemberLevelSortId = null;

//~ 是否现实兼职人员（只外单位）
var showConcurrentMember = true;
var showSecondMember = true;

var onlyCurrentDepartment = false;

//~ 是否只显示当前登录部门的人员
var onlyLoginAccount = false;

//~ 是否显示所有的外部部门，默认false(按照自己的访问权限来)
var showAllOuterDepartmentFlag = false;

//~ 需要显示的组的类型：1-个人,2-系统,3-项目，默认null，表示所有
var showTeamType = null;

var selectableTableRows = null;

var Constants_ShowMode_TREE = "TREE";//数据对象中必须有parentId字段
var Constants_ShowMode_LIST = "LIST";

var extParameters = null;

/**
 * list1的大小
 */
var Constants_List1_size = {
    showMember: 168, //显示人员
    showMember_1: 194, //显示人员
    noShowMember: 410 //不显示人员
};

//~ 名字最多显示的字节数
var nameMaxLength = {
    two: [22], //2列
    three: [16, 18]  //3列
};

//~ 名字与后面的补充信息之间的空格数
var nameMaxSpace = 2;

var NameSpace = {};
for (var x = 0; x < 40; x++) {
    var s = "";
    for (var y = 0; y < x; y++) {
        s += " ";
    }
    NameSpace[x] = s;
}

function getNameSpace(x) {
    if (x >= 39) {
        x = 39;
    }
    if (x <= 0) {
        x = 0;
    }
    return NameSpace[x];
}


//~ 连接多种实体的显示
var arrayJoinSep = "-";

//~ 连接多种实体的id
var valuesJoinSep = "_";

//~ 只连接多种实体value的id，如部门下的人员：deptId#memberId
var valuesJoinSep2 = "#";

//~ 不在选人被选区域显示的数据 type + id
var excludeElements = new ArrayList();

//~ 只在选人被选区域显示的数据 type + id ; null表示没有配置
var includeElements = null;

//~ 是否需要检测职务级别范围，默认true
var isNeedCheckLevelScope = true;

//~ 集团的职务级别 : key-level.id  value-index
var groupLevels = {};

//vjoin显示的部门类型（外部机构/外部单位）
var showExternalType = null;

var onlyShowChildrenAccount = null;

/*****************
 * 一下对象将在页面中重新赋值
 */
var panels = new ArrayList(); //当前需要显示的面板

var selectTypes = new ArrayList(); //当前可以选择的类型

var ShowMe = true;
var SelectType = "";
var Panels = "";

var accountId = "";
var memberId = "";
var departmentId = "";
var postId = "";
var levelId = "";


var currentArea2Type = Constants_Member;

/**
 * 面板对象,面板的显示名称 从Constants_Component中取
 *
 * @param type 面板类型 Constants_Department...
 * @param showMode 显示方式  tree || list
 * @param isShowMember 时候显示人员，如特殊角色不能显示人员
 * @param getMembersFun 获取其下面人员Member的方法，如 Department.prototype.getDirectMembers，只写getDirectMembers
 * @param getMembersFun 获取其直接子节点方法，只对true有效，如 Department.prototype.getDirectMembers，只写getDirectMembers
 * @param disabledAccountSelector 是否屏蔽单位切换按钮
 * @param hiddenOnChanageAccount 切换单位后是否隐藏面板
 * @param hiddenWhenRootAccount  选择集团后是否隐藏页签
 * @param searchArea 搜索区域，0-不搜索；1-Area1；2-Area2；12-Area1和Area2，默认0
 * @author tanmf
 */
function Panel(type, showMode, isShowMember, getMembersFun, getChildrenFun, disabledAccountSelector, hiddenOnChanageAccount, showQueryInputFun, searchArea, hiddenWhenRootAccount) {
    this.type = type; //M/O/D/T/P/L/....
    this.showMode = showMode;
    this.isShowMember = isShowMember;
    this.getMembersFun = getMembersFun;
    this.getChildrenFun = getChildrenFun;
    this.disabledAccountSelector = disabledAccountSelector;
    this.hiddenOnChanageAccount = hiddenOnChanageAccount;
    this.showQueryInputFun = showQueryInputFun;
    this.searchArea = searchArea;
    this.hiddenWhenRootAccount = hiddenWhenRootAccount;
}

Panel.prototype.toString = function () {
    return this.type + "\t" + this.name + "\t" + this.showMode;
};

//~ 系统提供的面板，面板名称不允许用“All”
var Constants_FormField = "FormField";
var Constants_Panels = new Properties();
Constants_Panels.put(Constants_Account, new Panel(Constants_Account, Constants_ShowMode_TREE, false, null, null, false, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_Department, new Panel(Constants_Department, Constants_ShowMode_TREE, true, "getDirectMembers", "getDirectChildren", false, false, "showQueryInputOfDepart", 3, true));
Constants_Panels.put(Constants_Team, new Panel(Constants_Team, Constants_ShowMode_LIST, true, null, null, false, false, "showQueryInput", 1, true));
Constants_Panels.put(Constants_Post, new Panel(Constants_Post, Constants_ShowMode_LIST, true, "getMembers", null, false, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_Level, new Panel(Constants_Level, Constants_ShowMode_LIST, true, "getMembers", null, false, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_Role, new Panel(Constants_Role, Constants_ShowMode_LIST, false, null, null, false, false, "showQueryInput", 1, true));
Constants_Panels.put(Constants_BusinessRole, new Panel(Constants_BusinessRole, Constants_ShowMode_LIST, false, null, null, false, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_Outworker, new Panel(Constants_Outworker, Constants_ShowMode_LIST, true, "getDirectMembers", null, false, true, "showQueryInputOfDepartOrTerm", 2, true));
Constants_Panels.put(Constants_ExchangeAccount, new Panel(Constants_ExchangeAccount, Constants_ShowMode_LIST, false, null, null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_OrgTeam, new Panel(Constants_OrgTeam, Constants_ShowMode_LIST, false, null, null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_RelatePeople, new Panel(Constants_RelatePeople, Constants_ShowMode_LIST, false, null, null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_FormField, new Panel(Constants_FormField, Constants_ShowMode_LIST, true, "getRoles", null, (canShowBusinessOrg == true) ? false : true, (canShowBusinessOrg == true) ? false : true, "showQueryInput", 1, (canShowBusinessOrg == true) ? false : true));
Constants_Panels.put(Constants_WFDynamicForm, new Panel(Constants_WFDynamicForm, Constants_ShowMode_LIST, true, "getRoles", null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_OfficeField, new Panel(Constants_OfficeField, Constants_ShowMode_LIST, true, "getRoles", null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_Admin, new Panel(Constants_Admin, Constants_ShowMode_LIST, false, null, null, true, true, "showQueryInput", 1, false));
Constants_Panels.put(Constants_Node, new Panel(Constants_Node, Constants_ShowMode_LIST, true, "getMembers", null, (canShowBusinessOrg == true) ? false : true, (canShowBusinessOrg == true) ? false : true, "showQueryInput", 1, (canShowBusinessOrg == true) ? false : true));
Constants_Panels.put(Constants_WfSuperNode, new Panel(Constants_WfSuperNode, Constants_ShowMode_LIST, false, null, null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_OrgRecent, new Panel(Constants_OrgRecent, Constants_ShowMode_LIST, false, "getMembers", null, true, true, "showQueryInput", 1, true));
Constants_Panels.put(Constants_JoinOrganization, new Panel(Constants_JoinOrganization, Constants_ShowMode_TREE, true, "getDirectMembers", "getDirectChildren", true, false, "showQueryInputOfDepart", 3, false));
Constants_Panels.put(Constants_JoinAccount, new Panel(Constants_JoinAccount, Constants_ShowMode_LIST, false, null, null, true, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_JoinAccountTag, new Panel(Constants_JoinAccountTag, Constants_ShowMode_TREE, false, null, "getDirectChildren", true, false, null, null, false));
Constants_Panels.put(Constants_MemberMetadataTag, new Panel(Constants_MemberMetadataTag, Constants_ShowMode_TREE, false, null, "getDirectChildren", true, false, null, null, false));
Constants_Panels.put(Constants_BusinessDepartment, new Panel(Constants_BusinessDepartment, Constants_ShowMode_TREE, true, "getDirectMembers", "getDirectChildren", false, false, "showQueryInputOfDepart", 3, false));
Constants_Panels.put(Constants_BusinessAccount, new Panel(Constants_BusinessAccount, Constants_ShowMode_LIST, false, null, null, false, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_JoinPost, new Panel(Constants_JoinPost, Constants_ShowMode_LIST, true, "getMembers", null, true, false, "showQueryInput", 1, false));
Constants_Panels.put(Constants_Guest, new Panel(Constants_Guest, Constants_ShowMode_LIST, true, null, null, false, false, "showQueryInput", 1, false));

/**
 * 按照面板对象的某个属性查找面板
 *
 * @return 面板的Id
 */
function findPanelsByProperty(property, value) {
    var result = new ArrayList();

    var ps = Constants_Panels.values();
    for (var i = 0; i < ps.size(); i++) {
        var p = ps.get(i);
        if (p[property] == value) {
            result.add(p.type);
        }
    }

    return result;
}

//中间分隔区域
var Constants_separatorDIV = new ArrayList();
Constants_separatorDIV.add(Constants_Department);
Constants_separatorDIV.add(Constants_BusinessDepartment);
Constants_separatorDIV.add(Constants_Team);
Constants_separatorDIV.add(Constants_Post);

var searchAlt = {
    "Department_Member": Constants_Component.get(Constants_Department) + "/" + Constants_Component.get(Constants_Member),
    "JoinOrganization_Member": Constants_Component.get(Constants_JoinOrganization) + "/" + Constants_Component.get(Constants_Member),
    "RelatePeople": Constants_Component.get(Constants_Member),
    "Post": Constants_Component.get(Constants_Post),
    "Role": Constants_Component.get(Constants_Role),
    "Member": Constants_Component.get(Constants_Member),
    "Department_Post": Constants_Component.get(Constants_Department) + "/" + Constants_Component.get(Constants_Post),
    "BusinessDepartment": Constants_Component.get(Constants_BusinessDepartment) + Constants_Component.get(Constants_Department),
    "BusinessDepartment_Member": Constants_Component.get(Constants_BusinessDepartment) + Constants_Component.get(Constants_Department) + "/" + Constants_Component.get(Constants_Member),
    "BusinessRole": Constants_Component.get(Constants_BusinessRole),
    "": ""
};

/**
 * 单位
 * @param id
 * @param name
 * @param levelScope
 * @param description
 */
function Account(id, parentId, path, name, hasChild, shortname, levelScope, description, externalType) {
    this.id = id;
    this.parentId = parentId;
    this.path = path;
    this.name = name;
    this.hasChild = hasChild;
    this.shortname = shortname;
    this.levelScope = levelScope;
    this.description = description;
    this.externalType = externalType;

    this.accessChildren = new ArrayList();
}

Account.prototype.toString = function () {
    return this.id + "\t" + this.name + "\t" + this.shortname + "\t" + this.levelScope;
};

//所有的单位
var allAccounts = new Properties();
//我能访问的单位
var accessableAccounts = new Properties();
var rootAccount = new Account();

//~ 我的能访问的单位 [id, superior]
var accessableAccountIds = new ArrayList();

var returnMemberWithDept = false;

/*************************************************************************************************
 * 页面初始化
 */
window.onload = function () {
    var startTime = new Date().getTime();

    if (allAccounts.isEmpty()) { //没有单位
        var msg = $.i18n("selectPeople.alertNoAccount");
        document.getElementById("uploadingDiv").innerHTML = "<font color='red'>" + msg + "</font>";
        document.getElementById("processTR").style.display = "none";
        return;
    }

    if (getParentWindowData("hiddenSaveAsTeam") == true || !checkCanSelectMember()
        || isAdministrator == true || groupAdmin == true || systemAdmin == true) {
        var saveAsTeamDiv = document.getElementById("saveAsTeamDiv");
        if (saveAsTeamDiv) {
            saveAsTeamDiv.style.display = "none";
        }
    }

    if (isAdministrator == true || groupAdmin == true || systemAdmin == true) { //管理员默认不限制
        isNeedCheckLevelScope = false;
    } else {
        if (getParentWindowData("isNeedCheckLevelScope") == false) {
            isNeedCheckLevelScope = false;
        } else if (!checkCanSelectMember()) { //不能选人就进行验证了
            isNeedCheckLevelScope = false;
        }
    }

    if (getParentWindowData("hiddenGroupLevel")) {
        Constants_Panels.get(Constants_Level).hiddenWhenRootAccount = true;
    }

    returnMemberWithDept = getParentWindowData("returnMemberWithDept") || false;

    onlyLoginAccount = getParentWindowData("onlyLoginAccount") || false;
    showAllOuterDepartmentFlag = getParentWindowData("showAllOuterDepartment") || false;
    extParameters = getParentWindowData("extParameters", "");

    accountId = getParentWindowData("accountId") || accountId;
    memberId = getParentWindowData("memberId") || memberId;
    departmentId = getParentWindowData("departmentId") || departmentId;
    postId = getParentWindowData("postId") || postId;
    levelId = getParentWindowData("levelId") || levelId;
    showExternalType = getParentWindowData("showExternalType");
    onlyShowChildrenAccount = getParentWindowData("onlyShowChildrenAccount");

    currentAccountId = accountId;

    showConcurrentMember = getParentWindowData("showConcurrentMember");
    if (showConcurrentMember == null) {
        showConcurrentMember = true;
    }
    showSecondMember = getParentWindowData("showSecondMember");
    if (showSecondMember == null) {
        showSecondMember = true;
    }

    onlyCurrentDepartment = getParentWindowData("onlyCurrentDepartment") || onlyCurrentDepartment;

    if (isV5Member) {
        if (isInternal == false && getParentWindowData("showDeptPanelOfOutworker") != true) {// 编外人员
            panels.remove(Constants_Account);
            panels.remove(Constants_Post);
            panels.remove(Constants_Level);
            panels.remove(Constants_Role);
            disabledChanageAccountSelector();
        }
    } else {// V-Join人员
        if (!isAdministrator && !isSubVjoinAdmin && panels.size() > 1) {
            panels.remove(Constants_Account);
            panels.remove(Constants_Team);
            panels.remove(Constants_Post);
            panels.remove(Constants_Level);
            panels.remove(Constants_Role);
        }
    }

    //管理员去掉管理人员页签
    if (isAdministrator || groupAdmin || systemAdmin) {
        panels.remove(Constants_RelatePeople);
    }

    //初始化单位选择器
    initChanageAccountTd();

    currentAccount = allAccounts.get(currentAccountId);
    if (currentAccount == null) { //如果是null，取第一个单位
        currentAccount = allAccounts.values().get(0);
        currentAccountId = currentAccount.id;
    }

    if (document.getElementById("currentAccountId")) {
        var aName = currentAccount.name || "";
        $("#currentAccountId").val(aName.getLimitLength(16));
        $("#currentAccountId").attr("title", aName);
    }

    //加载操作者主单位数据
    if (allAccounts.get(myAccountId)) {
        var _status = topWindow.initOrgModel(myAccountId, currentMemberId, extParameters);
        if (_status == false) {
            return;
        }
    }

    //加载V-Join主单位数据
    if (allAccounts.get(currentVjoinAccountId)) {
        var _status = topWindow.initOrgModel(currentVjoinAccountId, currentMemberId, extParameters, true);
        if (_status == false) {
            return;
        }
    }

    //加载自定义的选人页签
    var custom_Panels = topWindow.Constants_Custom_Panels.values();
    for (var i = 0; i < custom_Panels.size(); i++) {
        var customPanel = custom_Panels.get(i);
        var customPanelType = customPanel.type;
        var customPanelName = customPanel.name;
        var area1ShowType = customPanel.area1ShowType;
        Constants_Panels.put(customPanelType, new Panel(customPanelType, area1ShowType == 'LIST' ? Constants_ShowMode_LIST : Constants_ShowMode_TREE, true, "getRelationData", null, true, true, "showQueryInput", 1, true));
        Constants_Component.put(customPanelType, customPanelName);

    }

    //需要显示组的类型
    var showTeamTypeStr = getParentWindowData("showTeamType");
    if (showTeamTypeStr) {
        showTeamType = new ArrayList();
        showTeamType.addAll(showTeamTypeStr.split(","));
    }

    if (myAccountId != currentAccountId) {
        //加载当前单位数据，往往出现在兼职单位切换的时候
        var _status = topWindow.initOrgModel(currentAccountId, currentMemberId, extParameters);
        if (_status == false) {
            return;
        }
    }

    //加载不显示的数据
    initExcludeElements();
    initIncludeElements();

    try {
        if (isV5Member) {
            currentMember = topWindow.getObject(Constants_Member, currentMemberId, currentAccountId);
        } else {
            currentMember = topWindow.getObject(Constants_Member, currentMemberId, currentVjoinAccountId);
        }
    } catch (e) {
    }

    //外部人员，给一个最低职务界别
    if (currentMember && !currentMember.isInternal) {
        var lastLevel = topWindow.getDataCenter(Constants_Level).getLast();
        if (lastLevel) {
            currentMember.levelId = lastLevel.id;
        }
    }

    mappingLevelSortId();

    proce.close();
    document.getElementById('selectPeopleTable').style.display = "";

    //显示面板
    try {
        initAllPanel(currentAccountId);
    } catch (e) {
        /*if(e && e.number == 1){
			var chanageAccountSelectorObj = document.getElementById("chanageAccountSelector");
			if(chanageAccountSelectorObj){
				var nextAccountId = chanageAccountSelectorObj.options[chanageAccountSelectorObj.selectedIndex + 1];
				if(nextAccountId){
					chanageAccount(nextAccountId.value);
					selectChangAccount(nextAccountId.value);
				}
			}
		}*/
    }

    //回显原有数据
    initOriginalData();

    log.debug("初始化数据耗时：" + (new Date().getTime() - startTime) + "MS");
};

/*function selectChangAccount(_accountId){
	var chanageAccountSelectorObj = document.getElementById("chanageAccountSelector");
	if(chanageAccountSelectorObj){
		for(var i = 0; i < chanageAccountSelectorObj.options.length; i++) {
			var o = chanageAccountSelectorObj.options[i];
			if(o.value == _accountId){
				chanageAccountSelectorObj.selectedIndex = i;
				break;
			}
		}
	}
}*/

/**
 * 切换单位
 */
function chanageAccount(newAccountId) {
    try {
        initAllPanel(newAccountId);
    } catch (e) {
        //selectChangAccount(currentAccountId);
        return;
    }

    currentAccountId = newAccountId;

    if (topWindow.initOrgModel(currentAccountId, currentMemberId, extParameters) == false) { //加载数据
        getA8Top().close();
        return;
    }

    currentAccount = allAccounts.get(currentAccountId);

    if (tempNowPanel) {
        selPanel(tempNowPanel.type); //显示当前面板
    }

    mappingLevelSortId();
}

/**
 * 需要检测工作范围显示时：切换单位，把当前登录者的职务级别换算成当前显示单位的职务级别
 */
function mappingLevelSortId() {
    if (isNeedCheckLevelScope && currentMember) {
        if (currentAccountId != myAccountId) {
            var concurentM = topWindow.getObject(Constants_concurentMembers, currentMember.id, currentAccountId);
            if (concurentM != null) { //我在当前单位兼职
                if (concurentM.getLevel()) {
                    currentMemberLevelSortId = concurentM.getLevel().sortId;
                } else {
                    currentMemberLevelSortId = null;
                }

                return;
            }

            var levelIdOfGroup = currentMember.getLevel() ? currentMember.getLevel().groupLevelId : "-1"; //当前登录者对应集团的职务级别id
            var level = null;

            if (levelIdOfGroup && levelIdOfGroup != "0" && levelIdOfGroup != "-1") { //我的职务级别没有映射到集团，菜单当前单位的最低职务级别
                var myGroupLevelIndex = groupLevels[levelIdOfGroup]; //我在集团职务级别中index

                for (var groupLevelId in groupLevels) {
                    var index = groupLevels[groupLevelId];
                    if (myGroupLevelIndex > index) {
                        continue;
                    }

                    var _level = topWindow.findByProperty(topWindow.getDataCenter(Constants_Level), "groupLevelId", groupLevelId);
                    if (_level) {
                        level = _level;
                        break;
                    }
                }
            }

            if (!level) {
                level = topWindow.getDataCenter(Constants_Level).getLast(); //最低职务级别
            }

            if (level) {
                currentMemberLevelSortId = level.sortId;
            } else {
                currentMemberLevelSortId = null;
            }
        } else {
            level = currentMember.getLevel();
            if (level) {
                currentMemberLevelSortId = level.sortId;
            } else {
                currentMemberLevelSortId = null;
            }
        }
    }
}

/**
 * 非本单位，隐藏不显示的面板, 集团管理员和系统管理员例外
 */
function getPanels(accountId) {
    var _panels = new ArrayList();
    _panels.addList(panels);

    if (!systemAdmin && !groupAdmin && loginAccountId != accountId) {
        var ps = findPanelsByProperty("hiddenOnChanageAccount", true);
        var alwaysShowPanels = getParentWindowData("alwaysShowPanels");
        alwaysShowPanels = "," + alwaysShowPanels + ",";
        for (var i = 0; i < ps.size(); i++) {
            if (ps.get(i) == Constants_ExchangeAccount) {
                var hiddenOnChanageAccountForEA = getParentWindowData("hiddenOnChanageAccountForExchangeAccount");
                if (hiddenOnChanageAccountForEA == null || hiddenOnChanageAccountForEA)
                    _panels.remove(ps.get(i));
            } else if (ps.get(i) == Constants_OrgTeam) {
                var hiddenOnChanageAccountForOT = getParentWindowData("hiddenOnChanageAccountForOrgTeam");
                if (hiddenOnChanageAccountForOT == null || hiddenOnChanageAccountForOT)
                    _panels.remove(ps.get(i));
            } else if (alwaysShowPanels.indexOf("," + ps.get(i) + ",") >= 0) {//总是显示的页签
                continue;
            } else {
                _panels.remove(ps.get(i));
            }
        }
    }

    if (accountId == rootAccount.id) {
        var ps2 = findPanelsByProperty("hiddenWhenRootAccount", true);
        for (var i = 0; i < ps2.size(); i++) {
            _panels.remove(ps2.get(i));
        }
    }

    return (_panels);
}

/**
 * 初始化单位选择器
 */
function initChanageAccountTd() {
    /*var chanageAccountSelectorObj = document.getElementById("chanageAccountSelector");
	if(!chanageAccountSelectorObj){
		return;
	}
	var showAccountDivObj = document.getElementById("showAccountValueDiv");

	var selectedInd = 0;
	var _index = 0;
	var _hiddenAccountIds = getParentWindowData("hiddenAccountIds") || "";
	var hiddenAccountIds = _hiddenAccountIds.split(",");
	var hiddenRootAccount = getParentWindowData("hiddenRootAccount");
	for(var i = 0; i < accessableAccountIds.size(); i++) {
		var accessableAccount = accessableAccountIds.get(i);
		var _accountId = accessableAccount.id;
		var _superiorAccountId = accessableAccount.superior;
		if(hiddenRootAccount && _accountId == rootAccount.id){
			continue;
		}

		for(var j = 0; j < accessableAccountIds.size(); j++) {
			var accessableAccount1 = accessableAccountIds.get(j);
			var _accountId1 = accessableAccount1.id;
			var _superiorAccountId1 = accessableAccount1.superior;

			if(_accountId == _superiorAccountId1){
				if(!accessableAccount.children){
					accessableAccount.children = [];
				}

				accessableAccount.children[accessableAccount.children.length] = accessableAccount1;
				accessableAccount1.hasSuperior = true;
			}
		}
	}

	for(var i = 0; i < accessableAccountIds.size(); i++) {
		var _account = accessableAccountIds.get(i);
		if(hiddenRootAccount && _account.id == rootAccount.id){
			continue;
		}

		if(!_account.hasSuperior){
			draw(_account, 0);
		}
	}

	chanageAccountSelectorObj.selectedIndex = selectedInd;
	if(showAccountDivObj){
		showAccountDivObj.innerText = chanageAccountSelectorObj.options[selectedInd].text.trim().getLimitLength(14);
	}

	currentAccountId = chanageAccountSelectorObj.value;*/

    if (onlyLoginAccount == true) {
        disabledChanageAccountSelector();
    }

    /*function draw(_account, spaceIndex){
		var _accountId = _account.id;
		var account = allAccounts.get(_accountId);
		if(!account){
			return;
		}

		if(hiddenAccountIds.indexOf(_accountId) == -1){
			var text = "";
			for(var i = 0; i < spaceIndex; i++) {
				text += " ";
			}

			var option = new Option(text + account.shortname, account.id);

			if(account.id == currentAccountId){
				selectedInd = _index;
			}

			chanageAccountSelectorObj.options.add(option);

		_index += 1;
		}

		if(_account.children && _account.children.length > 0){
			for(var i = 0; i < _account.children.length; i++) {
				draw(_account.children[i], spaceIndex + 2);
			}
		}
	}*/
}

/*function clickAccount(selectValue){
	var chanageAccountSelectorObj = document.getElementById("chanageAccountSelector");
	if(chanageAccountSelectorObj){
		var options = chanageAccountSelectorObj.options;
		for(var i =0;i<options.length;i++){
			if(options[i].value == selectValue){
				chanageAccountSelectorObj.selectedIndex = i;
				var showAccountDivObj = document.getElementById("showAccountValueDiv");
				showAccountDivObj.innerText = options[i].text.trim().getLimitLength(14);
				chanageAccount(selectValue);
			}
		}
	}
}*/
/*function showSelectAccount(flag){
	var showAccountDiv = document.getElementById('showAccountDiv');
	if(showAccountDiv){
		var allAccountDiv = document.getElementById('allAccountDiv');
		var dis = showAccountDiv.getAttribute("disable");
		if((!dis || dis=="false") && allAccountDiv && allAccountDiv.style.display!="block" && flag == "show"){
			allAccountDiv.style.display='block';
		}else if(allAccountDiv){
			allAccountDiv.style.display='none'
		}
	}
}*/

// 能够搜索其他单位
function canSearchGroup() {
    var allowSeachGroup = getParentWindowData("allowSeachGroup", true);
    if (allowSeachGroup == false) {
        return false;
    }
    return accessableAccounts.size() > 2;
}

function disabledChanageAccountSelector(state) {
    if (onlyLoginAccount == true) {
        state = true;
    } else {
        state = state == null ? true : state;
    }

    if (isInternal == false && getParentWindowData("showDeptPanelOfOutworker") != true) {
        state = true;
    }
    state = chanageStateFromParameter(state);

    var _flag4showSearchGroup = false;//是否显示【查全集团】临时标示
    if (state == true || state == "true") {
        $('#select_input_div').prop("disabled", true).addClass("bg_color_gray");
    } else {
        $('#select_input_div').prop("disabled", false).removeClass("bg_color_gray");
        _flag4showSearchGroup = true;
    }
    // 访问访问不是全否 && 当前面板为部门 && 选择到人 && 可以切换单位 = 显示"查全集团"
    var showSearchGroup = canSearchGroup()
        && tempNowPanel != null
        && tempNowPanel.type == Constants_Department
        && checkCanSelectMember()
        && _flag4showSearchGroup;
    /**
     * Fix AEIGHT-4247&AEIGHT-4214
     * 当选人界面只能在本单位内选择某人时，即切换单位置灰时，屏蔽【查全集团】功能
     * modify by lilong 2012-04-12
     */
    //不能选择单位则不能在全集团范围内查询人员
    if (!showSearchGroup || onlyShowChildrenAccount == true) {
        $("#seachGroupMember").hide();
    } else {
        $("#seachGroupMember").show();
        $("#select_input_div").css("width", "115px");
        // $("#select_input_div_parent").css("width","120px");
        $("#currentAccountId").css("width", "113px");
        $("#q").css("width", "106px");
        $("#common_search_ul").css("width", "146px");//$("#q").css("width")+10+30
    }
    _flag4showSearchGroup = null;

    /*var chanageAccountSelectorObj = document.getElementById("chanageAccountSelector");
	if(chanageAccountSelectorObj){
		chanageAccountSelectorObj.disabled = state;
		var showAccountDiv = document.getElementById('showAccountDiv');
		if(showAccountDiv){
			if(state==true || state == "true"){
				showAccountDiv.disabled = true;
				showAccountDiv.setAttribute("disable","true");
			}else{
				showAccountDiv.disabled = false;
				showAccountDiv.setAttribute("disable","false");
			}
		}
	}*/
}

/**
 * 根据前台页面的参数设置来判断时候disable掉单位选择器。
 */
function chanageStateFromParameter(state) {
    if (tempNowPanel != null) {
        //从前台获取参数，判断是否disable掉单位选择器。
        if (tempNowPanel.type == Constants_OrgTeam) {
            var disableOrNot = getParentWindowData("disabledAccountSelectorForOrgTeam");
            if (disableOrNot != null)
                state = disableOrNot;
        }
        if (tempNowPanel.type == Constants_ExchangeAccount) {
            var disableOrNot = getParentWindowData("disabledAccountSelectorForExchangeAccount");
            if (disableOrNot != null)
                state = disableOrNot;
        }
    }
    return state;
}

/**
 * 检测是否可以选择该类型
 */
function checkCanSelect(type) {
    var types = type.split(valuesJoinSep);
    if (types.length == 2) {
        if (types[0] == Constants_Department) {
            return selectTypes.contains(types[1]);
        } else if (types[0] == Constants_BusinessDepartment) {
            return selectTypes.contains(types[1]);
        } else if (types[0] == Constants_Account && types[1] == Constants_Role) {
            return true;
        }
    } else {
        return selectTypes.contains(type);
    }

    return false;
}

/**
 * 检测是否可以选择Member
 */
function checkCanSelectMember() {
    return selectTypes.contains(Constants_Member);
}

/**新增 选择机构组显示**/
function checkCanSelectOrgTeam() {
    return selectTypes.contains(Constants_OrgTeam);
}

var canSelectEmailOrMobileValue = null;

function getCanSelectEmailOrMobile() {
    if (canSelectEmailOrMobileValue == null) {
        if (selectTypes.contains("Email")) {
            canSelectEmailOrMobileValue = "email";
        } else if (selectTypes.contains("Mobile")) {
            canSelectEmailOrMobileValue = "mobile";
        } else {
            canSelectEmailOrMobileValue = "";
        }
    }

    return canSelectEmailOrMobileValue;
}

function checkIsShowArea2() {
    var type = tempNowPanel.type;
    if (type == Constants_BusinessDepartment && checkCanSelect(Constants_BusinessRole) && getParentWindowData("hiddenRoleOfDepartment") != true) {
        return true;
    }

    if (type == Constants_BusinessRole) {
        return false;
    }

    if (isRootAccount()) {
        if (canShowBusinessOrg && (type == Constants_Node || type == Constants_FormField)) {
            return true;
        }

        if (type == Constants_BusinessDepartment && checkCanSelect(Constants_Member)) {
            return true;
        }

        return false;
    }

    if (type == Constants_Department && (getParentWindowData("showDepartmentMember4Search"))) {
        return true;
    } else if (type == Constants_Department && ((checkCanSelect(Constants_Post) && getParentWindowData("hiddenPostOfDepartment") != true) || (checkCanSelect(Constants_Role) && getParentWindowData("hiddenRoleOfDepartment") != true))) {
        return true;
    }

    if (type == Constants_FormField && (getParentWindowData("hiddenFormFieldRole") == true)) {
        return false;
    }
    if (type == Constants_WFDynamicForm) {
        return false;
    }

    if (type == Constants_Node) {
        return true;
    }

    if (type == Constants_OrgTeam) {
        return true;
    }

    if (type == Constants_Guest) {
        return false;
    }

    if (topWindow.Constants_Custom_Panels.keys().contains(type)) {
        var customPanel = topWindow.Constants_Custom_Panels.get(type);
        return customPanel.isShowArea2 == 'true' ? true : false;
    }

    return checkCanSelectMember() && tempNowPanel.isShowMember;
}

/**
 * 检测职务级别差
 * 1、同一部门的返回true
 * 2、被检测人职务级别高于当前操作者一个数字，返回false
 *
 * @member 要访问的人
 * @return true 有权访问, false 无权访问
 */
function checkLevelScope(member, entity, childDepts) {
    if (!isNeedCheckLevelScope
        || (currentMember && (currentMember.departmentId == member.departmentId || currentMember.isSecondPostInDept(member.departmentId)))
        || currentAccount == null || childDepts.contains(member.departmentId)
        || currentAccountLevelScope < 0) {
        return true;
    }
    // 外部人员通过工作范围校验
    if (isSubVjoinAdmin) {
        return true;
    }
    if (!currentMember.isInternal && checkExternalMemberWorkScopeOfMember(member)) {
        return true;
    }
    if (currentMemberLevelSortId == null) {
        return false;
    }

    //我在这个部门做兼职，我可以访问在这个部门的所有人
    try {
        if (entity) {
            var c = entity.getConcurents();
            if (c && c.contains(currentMember, "id")) {
                return true;
            }
        }
    } catch (e) {
    }

    //副岗在这个部门的有权限
    if (member.isSecondPostInDept(currentMember.departmentId)) {
        return true;
    }

    var level = member.getLevel();

    if (!level || level == null) {
        level = topWindow.getDataCenter(Constants_Level, currentAccountId).getLast(); //最低职务级别
    }

    if (currentMemberLevelSortId - level.sortId <= currentAccountLevelScope) {
        return true;
    }

    return false;
}

/**
 * 检测越级访问，包含子部门的职务级别校验
 * @return true 有权访问, false 无权访问
 */
function checkAccessLevelScopeWithChildDept(id) {
    if (tempNowPanel.type == Constants_Team) {
        return true;
    }
    if (!isNeedCheckLevelScope || currentAccountLevelScope < 0) {
        return true;
    }
    if (currentMemberLevelSortId == null) {
        if (isVjoinMember) {
            return checkAccessLevelScope4VJoin(tempNowPanel.type, id, true);
        } else {
            return false;
        }
    }

    var members = null;
    var notNeedCheckLevelScope = isChildDeptOfCurrent(currentMember, id);
    if (currentMember && (currentMember.departmentId == id || currentMember.isSecondPostInDept(id) || notNeedCheckLevelScope)) {
        return true;
    }

    var entity = topWindow.getObject(Constants_Department, id);
    if (entity && entity.getConcurents().contains(currentMember, "id")) {
        return true;
    }
    members = topWindow.getObject(Constants_Department, id).getAllMembers();
    if (members) {
        var childDepts = childDeptOfCurrent(currentMember);
        for (var i = 0; i < members.size(); i++) {
            if (!checkLevelScope(members.get(i), null, childDepts)) {
                return false;
            }
        }
    }

    return true;
}

/**
 * 检测越级访问，只要部门/组里面有任何一个人不能选择，则该部门/组不能选择
 * @return true 有权访问, false 无权访问
 */
function checkAccessLevelScope(type, id) {
    if (tempNowPanel.type == Constants_Team || tempNowPanel.type == Constants_Guest) {
        return true;
    }
    if (!isNeedCheckLevelScope || currentAccountLevelScope < 0) {
        return true;
    }
    if (currentMemberLevelSortId == null) {
        if (isVjoinMember) {
            return checkAccessLevelScope4VJoin(tempNowPanel.type, id, false);
        } else {
            return false;
        }
    }

    var members = null;
    if (type == Constants_Department) {
        var notNeedCheckLevelScope = isChildDeptOfCurrent(currentMember, id);
        if (currentMember && (currentMember.departmentId == id || currentMember.isSecondPostInDept(id) || notNeedCheckLevelScope)) {
            return true;
        }

        var entity = topWindow.getObject(type, id);
        if (entity && entity.getConcurents().contains(currentMember, "id")) {
            return true;
        }
        members = topWindow.getObject(Constants_Department, id).getDirectMembers();
    } else if (type == Constants_Team) {
        return true;
    } else if (type == Constants_Level) {
        var entity = topWindow.getObject(type, id);
        return currentMemberLevelSortId - entity.sortId <= currentAccountLevelScope;
    } else if (type == Constants_Post) {
        var entity = topWindow.getObject(type, id);
        members = entity.getAllMembers();
    } else if (type == Constants_Department + "_" + Constants_Post) {
        var ids = id.split("_");
        var types = type.split("_");

        var entity = topWindow.getObject(types[0], ids[0]);
        members = new ArrayList();
        if (entity) {
            var ms = entity.getAllMembers();
            if (ms) {
                for (var i = 0; i < ms.size(); i++) {
                    var m = ms.get(i);
                    if (m.postId == ids[1]) {
                        members.add(m);
                    }
                }
            }
        }
    } else if (type == Constants_Account) {
        if (isSubVjoinAdmin) {
            return true;
        }
        var allLevels = topWindow.getDataCenter(Constants_Level, id);

        if (allLevels && allLevels.size() > 0) {
            var highLevel;
            for (var i = 0; i < allLevels.size(); i++) {
                var l = allLevels.get(i);
                var levelMembers = l.getMembers();
                if (levelMembers && levelMembers.size() > 0) {
                    highLevel = l;
                    break;
                }
            }
            if (highLevel) {
            } else {
                highLevel = allLevels.get(0);
            }
            return currentMemberLevelSortId - highLevel.sortId <= currentAccountLevelScope;
        }
        return true;
    } else if (type == Constants_BusinessAccount) {
        var businessAccount = topWindow.getObject(Constants_BusinessAccount, id);
        if (businessAccount) {
            var accessMemberIds = businessAccount.accessMemberIds;
            var childBusinessDepartment = topWindow.findChildInList(topWindow.getDataCenter(Constants_BusinessDepartment, currentAccountId), id);
            for (var i = 0; i < childBusinessDepartment.size(); i++) {
                var members = childBusinessDepartment.get(i).getAllMembers();
                for (var j = 0; j < members.size(); j++) {
                    var member = members.get(j);
                    if (accessMemberIds.indexOf(member.id) < 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    } else {
        return true;
    }

    if (members) {
        var childDepts = childDeptOfCurrent(currentMember);
        for (var i = 0; i < members.size(); i++) {
            if (!checkLevelScope(members.get(i), null, childDepts)) {
                return false;
            }
        }
    }

    return true;
}

/**
 * 如果当前人是外部人员，检测改实体是否可以访问
 *
 * @param type
 * @param id
 * @return true:可以访问
 */
function checkExternalMemberWorkScope(type, id) {
    if (isInternal) {//当前人不是外部人员
        return true;
    }

    if (tempNowPanel.type == Constants_Team) {
        return true;
    }

    var _type = type.split(valuesJoinSep)[0];
    var _id = id.split(valuesJoinSep)[0];


    //编外人员
    if (isV5Member) {
        if (type.startsWith(Constants_Account) || type.startsWith(Constants_BusinessAccount)) {
            return false;
        }

        var o = topWindow.getObject(type, id);
        if (o && o.externalType && o.externalType != "0") {//编外人员不允许访问vjoin
            return false;
        }

        if (!type.startsWith(Constants_Department)) {
            return true;
        }

        var _ExternalMemberWorkScope = topWindow.ExternalMemberWorkScope;
        if (_ExternalMemberWorkScope.get(0) == "0") {
            return true;
        }

        for (var i = 0; i < _ExternalMemberWorkScope.size(); i++) {
            var wsDepartId = _ExternalMemberWorkScope.get(i); //工作部门的Id 如：D0.1
            if (_type == Constants_Department) {
                var d = topWindow.getObject(Constants_Department, _id);
                if (d && (("D" + d.path) == wsDepartId || ("D" + d.path).indexOf(wsDepartId) == 0)) {
                    return true;
                }
            }
        }
    }

    //vjoin 人员
    if (isVjoinMember) {
        if (isAdministrator == true) {
            return true;
        }

        if (isSubVjoinAdmin == true) {
            if (tempNowPanel.type == Constants_JoinOrganization && _type == Constants_Account) {//子机构管理员，不能选择vj根节点(如选择机构的上级机构)
                return false;
            }
            if (tempNowPanel.type == Constants_Department && _type == Constants_Account) {//子机构管理员，可以选择准出单位的单位（和vjoin-admin一样，比如授权内外互访权限）
                return true;
            }
        }

        if (tempNowPanel.type != Constants_Department && tempNowPanel.type != Constants_JoinOrganization) {
            return true;
        }
        var _VjoinMemberWorkScope = topWindow.VjoinMemberWorkScope;
        if (_VjoinMemberWorkScope.get(0) == "0") {
            return true;
        }

        if (tempNowPanel.type == Constants_Department) {
            var d = topWindow.getObject(Constants_Department, _id);
            for (var i = 0; i < _VjoinMemberWorkScope.size(); i++) {
                var wsDepartId = _VjoinMemberWorkScope.get(i); //工作部门的Id 如：D0.1
                if (_type == Constants_Department) {
                    if (d && (("D" + d.path) == wsDepartId || ("D" + d.path).indexOf(wsDepartId) == 0)) {
                        return true;
                    }
                }

                if (_type == Constants_Member) {
                    if (_VjoinMemberWorkScope.contains("M" + _id)) {
                        return true;
                    }
                }

            }
            if (_type == Constants_Department) {
                if (!isNeedCheckLevelScope) {//只要能看到這個部門，就能選
                    var _AccessInnerDepts = topWindow.AccessInnerDepts;
                    if (_AccessInnerDepts != null) {
                        if (_AccessInnerDepts.contains("D" + _id)) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    var _ms = d.getAllMembers();
                    var isSelect = true;
                    for (var i = 0; i < _ms.size(); i++) {
                        if (!_VjoinMemberWorkScope.contains("M" + _ms.get(i).id)) {
                            isSelect = false;
                            break;
                        }
                    }

                    if (isSelect) {
                        return true;
                    }
                }

            }
        }

        if (tempNowPanel.type == Constants_JoinOrganization) {
            var cm = topWindow.getObject(Constants_Member, currentMemberId);

            if (_type == Constants_Member) {
                var m = topWindow.getObject(Constants_Member, _id);
                if (m != null) {
                    var _VjMemberAccessVjAccounts = topWindow.VjMemberAccessVjAccounts;
                    if (_VjMemberAccessVjAccounts != null && _VjMemberAccessVjAccounts.contains("D" + m.departmentId)) {//可以访问本外部单位可访问的其他外部单位下的人员
                        return true;
                    }
                }
            } else {
                var d = topWindow.getObject(Constants_Department, _id);
                if (_type == Constants_Account || (cm != null && cm.departmentId == d.id)) {
                    return true;
                }

                var _AccessVjoinDepts = topWindow.AccessVjoinDepts;
                if (!isNeedCheckLevelScope || isSubVjoinAdmin == true) {//只要能看到這個部門，就能選
                    if (_AccessVjoinDepts != null && _AccessVjoinDepts.contains("D" + _id)) {
                        return true;
                    } else {
                        return false;
                    }
                } else if (_AccessVjoinDepts.contains("D" + _id)) {
                    var _ms = d.getAllMembers();
                    var _VjMemberAccessVjAccounts = topWindow.VjMemberAccessVjAccounts;
                    for (var i = 0; i < _ms.size(); i++) {
                        if (_VjMemberAccessVjAccounts != null && !_VjMemberAccessVjAccounts.contains("D" + _ms.get(i).departmentId)) {//所选择的外单位/机构，下的人员全部在 当前外部人员可访问的外部单位内，则可以选择。
                            return false;
                        }
                    }
                    return true;
                }

            }

        }

    }

    return false;
}

/**
 * 校验vjoin人员，是否可以选择部门包含子部门
 * @param type
 * @param id
 * @returns {Boolean}
 */
function checkAccessLevelScope4VJoin(_type, _id, withChildDept) {
    //vjoin 人员
    if (!isNeedCheckLevelScope) {
        return true;
    }
    if (isAdministrator == true || isSubVjoinAdmin == true) {
        return true;
    }
    var _VjoinMemberWorkScope = topWindow.VjoinMemberWorkScope;
    if (_VjoinMemberWorkScope.get(0) == "0") {
        return true;
    }

    var d = topWindow.getObject(Constants_Department, _id);
    /*	for(var i = 0; i < _VjoinMemberWorkScope.size(); i++) {
		var wsDepartId = _VjoinMemberWorkScope.get(i); //工作部门的Id 如：D0.1
		if(_type == Constants_Department){
			if(d && (("D" + d.path) == wsDepartId || ("D" + d.path).indexOf(wsDepartId) == 0)){
				return true;
			}
		}
	}*/

    var _ms = d.getDirectMembers();
    if (withChildDept == true) {
        _ms = d.getAllMembers();
    }
    var isSelect = true;
    for (var i = 0; i < _ms.size(); i++) {
        if (!_VjoinMemberWorkScope.contains("M" + _ms.get(i).id)) {
            isSelect = false;
            break;
        }
    }

    if (isSelect) {
        return true;
    }
    return false;
}

/**
 * 判断外部人员是否可以访问内部具体的人
 */
function checkExternalMemberWorkScopeOfMember(member) {
    if (isInternal) {//当前人不是外部人员
        return true;
    }

    if (isV5Member) {
        if (!member.isInternal && member.getDepartment().id == currentMember.departmentId) {
            return true;
        }
        var _ExternalMemberWorkScope = topWindow.ExternalMemberWorkScope;
        if (_ExternalMemberWorkScope.isEmpty()) {
            return false;
        }

        if (member && member.externalType && member.externalType != "0") {//编外人员不允许访问vjoin
            return false;
        }

        if (_ExternalMemberWorkScope.get(0) == "0") {
            return true;
        }

        if (_ExternalMemberWorkScope.contains("M" + member.id)) {
            return true;
        }

    }

    if (isVjoinMember) {
        if (isAdministrator == true || isSubVjoinAdmin == true) {
            return true;
        }

        if (currentMember != null) {
            if (member.getDepartment().id == currentMember.departmentId) {//可以访问本外部单位的人员
                return true;
            }

            var _VjMemberAccessVjAccounts = topWindow.VjMemberAccessVjAccounts;
            if (_VjMemberAccessVjAccounts != null && _VjMemberAccessVjAccounts.contains("D" + member.getDepartment().id)) {//可以访问本外部单位可访问的其他外部单位下的人员
                return true;
            }
        }

        var _VjoinMemberWorkScope = topWindow.VjoinMemberWorkScope;
        if (_VjoinMemberWorkScope.isEmpty()) {
            return false;
        }

        if (_VjoinMemberWorkScope.get(0) == "0") {
            return true;
        }

        if (_VjoinMemberWorkScope.contains("M" + member.id)) {
            return true;
        }

        return false;

    }

    return checkExternalMemberWorkScope(Constants_Department, member.getDepartment().id);
}

/**
 * 判断内部人员是否可以访问vjoin具体的人
 */
function checkVjoinMemberWorkScopeOfMember(member) {
    if (!isInternal) {
        return true;
    }

    var _InnerMemberAccessVjoinMember = topWindow.InnerMemberAccessVjoinMember;
    if (_InnerMemberAccessVjoinMember.isEmpty()) {
        return false;
    }

    if (_InnerMemberAccessVjoinMember.get(0) == "0") {
        return true;
    }

    if (_InnerMemberAccessVjoinMember.contains("M" + member.id)) {
        return true;
    }

    return false;

}

/**
 * 初始化面板，并把第一个面板作为当前显示的面板
 */
function initAllPanel(_accoutId) {
    var _panels = getPanels(_accoutId);

    if (_panels == null || (_panels != null && _panels.isEmpty())) {
        initNoPanel();
        return;
    }

    var tdPanelObj = document.getElementById("tdPanel");
    var length1 = tdPanelObj.cells.length;
    for (var i = 0; i < length1; i++) {
        tdPanelObj.deleteCell(i);
    }

    // 将要被显示的面板 <Panel>
    var toShowPanels = new ArrayList();
    var toShowPanels2 = new ArrayList();
    var containsDepartment = false;
    for (var i = 0; i < _panels.size(); i++) {
        var panel = Constants_Panels.get(_panels.get(i));
        if (panel == null) {
            //log.warn("The Panel's type '" + _panels.get(i) + "' undefined.");
            continue;
        }
        if (panel.type == Constants_Department) {
            containsDepartment = true;
        }
        toShowPanels2.add(panel);
    }
    /**
     * 20140411 讨论结果：出现【最近】页签的前提，有部门页签，且用来选人就出现最近联系人
     */
    if (containsDepartment && checkCanSelectMember()) {
        //只有当前单位不出现最近
        if (onlyLoginAccount == true) {
            showRecent = false;
        }
        if (showRecent && !isAdmin && isV5Member) {
            toShowPanels.add(Constants_Panels.get(Constants_OrgRecent));
        }
        for (var i = 0; i < toShowPanels2.size(); i++) {
            toShowPanels.add(toShowPanels2.get(i));
        }
    } else {
        toShowPanels = toShowPanels2;
    }

    var canSelectTempNowPanel = false;
    var canSelectDepartmentPanel = false;
    for (var i = 0; i < toShowPanels.size(); i++) {
        if (tempNowPanel != null && toShowPanels.get(i).type == tempNowPanel.type) {
            canSelectTempNowPanel = true;
            break;
        }
        if (toShowPanels.get(i).type == "Department") {
            canSelectDepartmentPanel = true;
        }
    }

    //默认是部门页签。
    if (!canSelectTempNowPanel && canSelectDepartmentPanel) {
        tempNowPanel = Constants_Panels.get(Constants_Department);
    }

    var CellTd = tdPanelObj.insertCell(-1);
    CellTd.style.height = "32px";//顶部TAB选项卡高度
    CellTd.style.verticalAlign = "bottom";

    var newDiv = document.createElement("div");
    newDiv.className = "common_tabs clearfi";
    CellTd.appendChild(newDiv);

    var newul = document.createElement("ul");
    newul.className = "left";
    newDiv.appendChild(newul);

    //选项宽度为iframe的body宽度-左右间距15*2
    var _tabsAreaWidth = document.getElementsByTagName("body")[0].offsetWidth - 15 * 2;
    //tab区的高度
    var _tabAreaHeight = document.getElementById("tdPanel").offsetHeight;
    //计算宽度的计数器
    var _nowWh = 0;
    //tab区能显示下多少tab页签的计数器
    var _tabMaxNum = 0;
    //tab数量是否过多，宽度超过一行了，初始值false
    var _needReDraw = false;
    //tab的字体大小，用于计算宽度
    var _fontSize = 12;
    //一共有多少个tab页签
    var panelsSize = toShowPanels.size();
    //将面板对象转化为HTML代码
    for (var i = 0; i < panelsSize; i++) {
        var panel = toShowPanels.get(i);
        var title = Constants_Component.get(panel.type);

        //获取当前页签的字节数（全角字符、汉字占两字符）
        var _titleBytes = title.getBytesLength();
        //宽度<50时，以50计(与css文件中的定义同步)，这里里的8*2为左右padding各8px
        var _titleLen = (_fontSize / 2 * _titleBytes + 8 * 2) > (50 + 8 * 2) ? (_fontSize / 2 * _titleBytes + 8 * 2) : (50 + 8 * 2);
        //宽度>120时，以120计(与css文件中的定义同步)
        _titleLen = _titleLen > (120 + 8 * 2) ? (120 + 8 * 2) : _titleLen;

        _nowWh += _titleLen;
        //20为下拉箭头所占宽度
        if (_nowWh + 20 > _tabsAreaWidth) {
            _needReDraw = true;
            break;
        }
        _tabMaxNum = i + 1;


        var li = document.createElement("li");
        li.id = 'li_' + panel.type;
        newul.appendChild(li);

        var a = document.createElement("a");
        li.appendChild(a);

        a.onclick = new Function("selPanel('" + panel.type + "', '" + panel.showMode + "')");
        if (i == panelsSize - 1) {
            a.className = "no_b_border last_tab";
        } else {
            a.className = "no_b_border";
        }
        a.innerText = title;
        a.title = title;

//		var right = document.createElement("div");
//		right.id='right'+panel.type;
//		right.className="tab-tag-right";
//
//		var separator = document.createElement("div");
//		separator.className="tab-separator";

//		newTd.appendChild(left);
        newul.appendChild(li);
//		newTd.appendChild(right);
//		newTd.appendChild(separator);
    }

    //一行显示不下所有tab页签时，剩下的页签单独放进右侧的下拉div中
    if (_needReDraw) {
        //下拉区
        var _newMoretab = document.createElement("div");
        _newMoretab.id = "moreTabsArea";
        _newMoretab.className = "moreTabsArea left";
        newDiv.appendChild(_newMoretab);

        //下拉箭头的div
        var _newMoretabArror = document.createElement("div");
        _newMoretabArror.id = "moreTabsArror";
        _newMoretabArror.className = "moreTabsArror";
        _newMoretab.appendChild(_newMoretabArror);
        _newMoretabArror.style.height = _tabAreaHeight + "px";

        //下拉箭头的图标
        var _newMoretabArrorSpan = document.createElement("span");
        _newMoretabArrorSpan.className = "ico16 arrow_2_b";
        _newMoretabArror.appendChild(_newMoretabArrorSpan);
        _newMoretabArrorSpan.style.marginTop = (_tabAreaHeight - 24) + "px";

        //下拉列表区
        var _newMoretabList = document.createElement("div");
        _newMoretabList.id = "moreTabsList";
        _newMoretabList.className = "moreTabsList";
        _newMoretab.appendChild(_newMoretabList);

        //下拉列表li
        for (var j = _tabMaxNum; j < panelsSize; j++) {
            var morePanel = toShowPanels.get(j);
            var moreTitle = Constants_Component.get(morePanel.type);

            var moreLi = document.createElement("li");
            moreLi.id = 'li_' + morePanel.type;
            _newMoretabList.appendChild(moreLi);

            var moreA = document.createElement("a");
            moreLi.appendChild(moreA);

            moreA.onclick = new Function("selPanel('" + morePanel.type + "', '" + morePanel.showMode + "')");
            moreA.innerText = moreTitle;
            moreA.title = moreTitle;
        }
    }

    //显示第一个面板
    if (!toShowPanels.isEmpty()) {
        if (tempNowPanel && toShowPanels.contains(tempNowPanel, "type")) {
            selPanel(tempNowPanel.type); //显示当前面板
        } else {
            tempNowPanel = null;
            selPanel(toShowPanels.get(0).type);
        }
    } else {
        initNoPanel();
    }
}

function initNoPanel() {
    //没有任何面板
    var area1Reduction = 0;
    if (window.innerHeight < 500) {
        area1Reduction = 500 - window.innerHeight;
    }

    document.getElementById("Area1").style.display = "";
    document.getElementById("Area1").style.height = (450 - area1Reduction - 50) + "px";
    if (document.getElementById("List1")) {
        document.getElementById("List1").style.height = (450 - area1Reduction - 50 + (true ? 10 : 0)) + "px";
    }

    document.getElementById("Area2").style.display = "none";

    document.getElementById("Separator1").style.height = "0px";
    document.getElementById("Separator1_0").style.display = "none";
    document.getElementById("Separator1_1").style.display = "none";
    document.getElementById("Separator1_2").style.display = "none";
    $("#Separator1_2").css("margin-top", "0");

    $("#seachGroupMember").hide();
    var objectQ = document.getElementById("q");
    objectQ.disabled = true;
    objectQ.className = "search_input color_gray";
    objectQ.style.background = "#F8F8F8";
    objectQ.value = "-";
}

/**
 * 根据面板类型显示面板
 *
 * @param type 面板类型
 */
function selPanel(type) {
    if (tempNowPanel != null) {//
        var middel = $('#li_' + tempNowPanel.type);
        middel.removeClass('current');
        middel.onclick = new Function("selPanel('" + tempNowPanel.type + "', '" + tempNowPanel.showMode + "')");
    }

    var middel = $('#li_' + type);
    middel.addClass("current");
    middel.onclick = new Function("");

    //不隐藏外部单位连接时
    if (type == "ExchangeAccount" && (!getParentWindowData("hiddenAddExternalAccount"))) {
        //外部单位时，打开链接对象
        var addExternalAccountObj = document.getElementById("addExternalAccountDiv");
        addExternalAccountObj.style.display = "";
    } else {
        var addExternalAccountObj = document.getElementById("addExternalAccountDiv");
        addExternalAccountObj.style.display = "none";
    }

    tempNowPanel = Constants_Panels.get(type);

    tempNowSelected.clear();

    showList1(type, tempNowPanel.showMode);

    reArea_1_2();
    if (!checkIsShowArea2()) {
        hiddenArea2(true);
        document.getElementById("Separator1").style.display = "none";
    }

    //部门页签下查询人员时是否支持全集团查询
    if ((type == Constants_Department || type == Constants_BusinessDepartment) && onlyShowChildrenAccount != true) {
        $("#seachGroupMember").show();
        $("#select_input_div").css("width", "145px");
        // $("#select_input_div_parent").css("width","160px");
        $("#currentAccountId").css("width", "143px");
        $("#q").css("width", "156px");
        $("#common_search_ul").css("width", "190px");//$("#q").css("width")+10+30
        //实际#seachGroupMember是隐藏的
        var obj = document.getElementById("seachGroup");
    } else if (type == Constants_BusinessDepartment) {
        $("#seachGroupMember").hide();
        $("#select_input_div").css("width", "145px");
        // $("#select_input_div_parent").css("width","160px");
        $("#currentAccountId").css("width", "143px");
        $("#q").css("width", "156px");
        $("#common_search_ul").css("width", "190px");//$("#q").css("width")+10+30
        showSeparatorDIV(Constants_BusinessDepartment);
    } else {
        $("#seachGroupMember").hide();
        $("#select_input_div").css("width", "145px");
        // $("#select_input_div_parent").css("width","160px");
        $("#currentAccountId").css("width", "143px");
        $("#q").css("width", "156px");
        $("#common_search_ul").css("width", "190px");//$("#q").css("width")+10+30
    }

    if (type == Constants_OrgRecent || !isGroupVer) {
        $("#select_input_div").hide();
        $("#common_search_ul").width("350px");
        $("#q").width("315px");
    } else {
        $("#select_input_div").show();
    }

    var ps = findPanelsByProperty("disabledAccountSelector", true);
    disabledChanageAccountSelector(ps.contains(type));


    showSearchInput(type);

    //不能查询时则不显示"查全集团"
    if (document.getElementById("q").disabled || !checkCanSelectMember()) {
        $("#seachGroupMember").hide();
        $("#select_input_div").css("width", "145px");
        // $("#select_input_div_parent").css("width","160px");
        $("#currentAccountId").css("width", "143px");
        $("#q").css("width", "156px");
        $("#common_search_ul").css("width", "190px");//$("#q").css("width")+10+30
    }
}

function showSearchInput(type) {
    var _showQueryInputFun = tempNowPanel.showQueryInputFun;
    var disabledQ = true;
    var objectQ = document.getElementById("q");
    if (_showQueryInputFun) {
        var _showQueryInputFunState = eval(_showQueryInputFun + "()");
        if (_showQueryInputFunState == false) {
            objectQ.value = "";
        }
        disabledQ = (_showQueryInputFunState == false);
    }

    objectQ.disabled = disabledQ;
    if (!disabledQ) {
        var searchAltSuf = null;
        if (type == Constants_Department) {
            var dd = [];

            if (panels.size() == 1 && panels.get(0) == "Department" && selectTypes.size() == 1 && selectTypes.get(0) == "Post") {
                //只是查询部门下的岗位，设置副岗
                dd[dd.length++] = "Department_Post";
                currentArea2Type = Constants_Post;
            } else if (currentArea2Type == Constants_Post || currentArea2Type == Constants_Role) {
                dd[dd.length++] = currentArea2Type;
            } else {
                if (checkCanSelect(type)) {
                    dd[dd.length++] = type;
                }
                if (checkCanSelectMember()) {
                    dd[dd.length++] = "Member";
                }
            }

            searchAltSuf = searchAlt[dd.join("_")];
        } else if (type == Constants_BusinessDepartment) {
            var dd = [];
            if (currentArea2Type == Constants_BusinessRole) {
                dd[dd.length++] = currentArea2Type;
            } else {
                dd[dd.length++] = type;
            }

            if (checkCanSelectMember()) {
                dd[dd.length++] = "Member";
            }

            searchAltSuf = searchAlt[dd.join("_")];
        } else if (type == Constants_JoinOrganization) {
            var dd = [];
            if (checkCanSelect(Constants_Department)) {
                dd[dd.length++] = type;
            }

            if (checkCanSelectMember()) {
                dd[dd.length++] = "Member";
            }

            searchAltSuf = searchAlt[dd.join("_")];
        }

        searchAltSuf = searchAltSuf || searchAlt[type] || Constants_Component.get(type) || "";
        objectQ.value = objectQ.defaultValue = $.i18n("selectPeople.search.alt", searchAltSuf);
        objectQ.className = "search_input color_gray";
        objectQ.title = $.i18n("selectPeople.search.alt", searchAltSuf);
    } else {
        objectQ.className = "search_input color_gray";
        // objectQ.style="background:#F8F8F8";
        objectQ.style.background = "#F8F8F8";
        objectQ.value = "-";
    }

}

function checkSearchAlt(checkEmpty) {
    var objectQ = document.getElementById("q");
    if (objectQ.defaultValue == objectQ.value) {
        objectQ.value = "";
        objectQ.className = "search_input";
    }
    if (checkEmpty && objectQ.value == "") {
        objectQ.value = objectQ.defaultValue;
        objectQ.className = "search_input color_gray";
    }
}

/**
 * ??????
 */
function showList1(type, showMode) {

    clearList2();

    if (nowSelectedList1Item != null) {
        nowSelectedList1Item = null;
    }

    showSeparatorDIV(type);
    for (var i = 0; i < Constants_Panels.keys().size(); i++) {
        var pn = Constants_Panels.keys().get(i);
        try {
            document.getElementById("AreaTop1_" + pn).style.display = "none";
        } catch (e) {
        }
    }

    if (showMode == Constants_ShowMode_TREE) {
        enableButton("button1");
        if (type == Constants_JoinOrganization) {
            initTree(Constants_Department, currentVjoinAccountId);
        } else if (type == Constants_JoinAccountTag) {
            initTree(type, currentVjoinAccountId);
        } else if (type == Constants_MemberMetadataTag) {
            initTree(type, currentAccountId);
        } else if (type == Constants_BusinessDepartment) {
            initTree(type, currentAccountId);
        } else {
            initTree(type, currentAccountId);
        }
    } else if (showMode == Constants_ShowMode_LIST) {
        disableButton("button1");
        initList(type);
    } else {
        log.warn("The Paramter showMode '" + showMode + "' is undefined.");
    }

//	showMemberTitle(type);
}

/**
 * 显示部门下的内容：人/岗位/角色
 */
function showList2OfDep(area2Type, keyword) {
    //显示人员时才支持在全集团范围内查询
    if (area2Type == "Member" && onlyShowChildrenAccount != true && tempNowPanel.type == Constants_Department && canSearchGroup()) {
        $("#seachGroupMember").show();
        $("#select_input_div").css("width", "115px");
        // $("#select_input_div_parent").css("width","120px");
        $("#currentAccountId").css("width", "113px");
        $("#q").css("width", "106px");
        $("#common_search_ul").css("width", "146px");//$("#q").css("width")+10+30

    } else {
        $("#seachGroupMember").hide();
        $("#select_input_div").css("width", "145px");
        // $("#select_input_div_parent").css("width","160px");
        $("#currentAccountId").css("width", "143px");
        $("#q").css("width", "156px");
        $("#common_search_ul").css("width", "190px");//$("#q").css("width")+10+30

    }

    if (!tree.getSelected()) {
        return;
    }

    var _Id = tree.getSelected().id;
    var _type = tree.getSelected().type;
    if (tempNowPanel.type == Constants_BusinessDepartment) {
        _type = Constants_BusinessDepartment;
        if (area2Type == Constants_Role) {
            area2Type = Constants_BusinessRole;
        }
        showList2(_type, _Id, area2Type, keyword);
        showSearchInput(Constants_BusinessDepartment);
    } else {
        showList2(_type, _Id, area2Type, keyword);
        showSearchInput(Constants_Department);
    }

}

/**
 * 显示区域2的内容
 */
//zhou
var ztype = "";
var zid;

function showList2(type, id, area2Type, keyword) {
    ztype = type;
    zid = id;

    clearList2();
    if (type == Constants_Account) {
        if (!area2Type) {
            var _seps = document.getElementsByName("sep");
            for (var i = 0; i < _seps.length; i++) {
                var _sep = _seps[i];
                if (_sep.checked) {
                    area2Type = _sep.value;
                    break;
                }
            }
        }

        if (!checkExternalMemberWorkScope(type, id)) {
            return;
        }

        if (area2Type == Constants_Member) {
            //ignore
        } else if (area2Type == Constants_Role) {
            showSubOfAccount(id, area2Type, keyword);
        }
    } else if (type == Constants_BusinessAccount) {
    } else if (type == Constants_Department) {
        if (!area2Type) {
            var _seps = document.getElementsByName("sep");
            for (var i = 0; i < _seps.length; i++) {
                var _sep = _seps[i];
                if (_sep.checked) {
                    area2Type = _sep.value;
                    break;
                }
            }
        }

        if (area2Type == Constants_Member) {
            showMember(type, id, keyword);
        } else {
            if (!checkExternalMemberWorkScope(type, id)) {
                return;
            }

            showSubOfDepartment(id, area2Type, keyword);
        }
    } else if (type == Constants_BusinessDepartment) {
        if (!area2Type) {
            area2Type = Constants_Member;
            var _seps = document.getElementsByName("sep");
            for (var i = 0; i < _seps.length; i++) {
                var _sep = _seps[i];
                if (_sep.checked) {
                    area2Type = _sep.value;
                    if (area2Type == Constants_Role) {
                        if (checkCanSelect(Constants_BusinessRole)) {
                            area2Type = Constants_BusinessRole;
                        } else {
                            area2Type = Constants_Member;
                        }
                    } else if (area2Type == Constants_Post) {
                        area2Type = Constants_Member;
                    }
                    break;
                }
            }
        }

        if (area2Type == Constants_Member) {
            showMember(type, id, keyword);
        } else {
            if (!checkExternalMemberWorkScope(type, id)) {
                return;
            }

            showSubOfBusinessDepartment(id, area2Type, keyword);
        }
    } else if (type == Constants_Node) {
        showList2OfNode(type, id, keyword, "");
    } else if (type == Constants_FormField) {
        showList2OfNode(type, id, keyword, "#");
    } else if (type == Constants_OfficeField) {
        showList2OfNode(type, id, keyword, "");
    } else if (topWindow.Constants_Custom_Panels.keys() != null && topWindow.Constants_Custom_Panels.keys().contains(type)) {
        showList2OfCustom(type, id, keyword);
    } else {
        showMember(type, id, keyword);
    }

    if (area2Type && (checkCanSelect(Constants_Role) || checkCanSelect(Constants_Post))) {
        currentArea2Type = area2Type;
    } else {
        currentArea2Type = Constants_Member;
    }

}

/**
 * 显示/隐藏 部门树和人员列表中见的人/岗位/角色
 */
function showSeparatorDIV(type) {
    for (var i = 0; i < Constants_separatorDIV.size(); i++) {
        var d = Constants_separatorDIV.get(i);
        /*		if(d == type){
			continue;
		}*/
        if (d === Constants_BusinessDepartment) {
            d = Constants_Department;
        }

        document.getElementById("separatorDIV_" + d).style.display = "none";
    }

    //部门面板 选择部门
    if ((type == Constants_Department || type == Constants_Account) && (checkCanSelect(Constants_Role) || checkCanSelect(Constants_Post))) {
        document.getElementById("separatorDIV_Department").style.display = "";
        document.getElementById("sep_per_l").style.display = "none";
        document.getElementById("sep_post_l").style.display = "none";
        document.getElementById("sep_role_l").style.display = "none";
        var selectedIndex = -1;
        if (checkCanSelectMember()) {
            document.getElementById("sep_per_l").style.display = "";
            selectedIndex = 0;
        }

        if (checkCanSelect(Constants_Post) && getParentWindowData("hiddenPostOfDepartment") != true && isInternal) {
            document.getElementById("sep_post_l").style.display = "";
            if (selectedIndex == -1) {
                selectedIndex = 1;
            }
        }

        if (checkCanSelect(Constants_Role) && getParentWindowData("hiddenRoleOfDepartment") != true && isInternal) {
            document.getElementById("sep_role_l").style.display = "";
            if (selectedIndex == -1) {
                selectedIndex = 2;
            }
        }

        if (selectedIndex != -1) {
            document.getElementsByName("sep")[selectedIndex].checked = true;
        }
    } else if (type == Constants_BusinessDepartment && checkCanSelect(Constants_BusinessRole)) {
        document.getElementById("separatorDIV_Department").style.display = "";
        document.getElementById("sep_per_l").style.display = "none";
        document.getElementById("sep_post_l").style.display = "none";
        document.getElementById("sep_role_l").style.display = "none";
        var selectedIndex = -1;
        if (checkCanSelectMember()) {
            document.getElementById("sep_per_l").style.display = "";
            selectedIndex = 0;
        }

        document.getElementById("sep_post_l").style.display = "none";

        if (checkCanSelect(Constants_BusinessRole) && getParentWindowData("hiddenRoleOfDepartment") != true) {
            document.getElementById("sep_role_l").style.display = "";
            if (selectedIndex == -1) {
                selectedIndex = 2;
            }
        }

        if (selectedIndex != -1) {
            document.getElementsByName("sep")[selectedIndex].checked = true;
        }
    } else if (type == Constants_Team && checkCanSelect(Constants_Member)) { //组面板
        document.getElementById("separatorDIV_" + type).style.display = "";
    } else if (type == Constants_Post && checkCanSelect(Constants_Post)) { //岗位组面板
        document.getElementById("separatorDIV_" + type).style.display = "";
    }
}

/**
 * 选中"查全集团"时, 不显示岗位、角色; 反之显示.
 */
function hideSeparatorDIV(obj) {
    if (obj && obj.checked) {
        if (!$("#sep_post_l").is(":hidden")) {
            $("#sep_post_l").hide();
        }
        if (!$("#sep_role_l").is(":hidden")) {
            $("#sep_role_l").hide();
        }
    } else {
        showSeparatorDIV(Constants_Department);
    }
}

/**
 * 在Area2显示部门下的岗位
 */
function showSubOfDepartment(departmentId, subType, keyword) {
    var department = topWindow.getObject(Constants_Department, departmentId);
    if (!department) {
        return;
    }

    var entites = eval("department.get" + subType + "s()");
    var selectHTML = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        selectHTML.append(memberDataBody_div);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    if (entites) {
        for (var i = 0; i < entites.size(); i++) {
            var entity = entites.get(i);

            if (entity == null) {
                continue;
            }

            if (keyword && entity.name.toLowerCase().indexOf(keyword) < 0) {
                continue;
            }

            var _id = departmentId + valuesJoinSep + entity.id;
            var _type = Constants_Department + valuesJoinSep + subType;
            //该数据不显示
            if (excludeElements.contains(_type + _id)) {
                continue;
            }

            var text = null;
            var showTitle = "";
            if (entity.code) {
                text = entity.name.getLimitLength(nameMaxLength.two[0]);
                if (text != entity.name) {
                    showTitle = entity.name.escapeSpace();
                }
                text += getNameSpace(nameMaxLength.two[0] + nameMaxSpace - text.getBytesLength());
                text += entity.code;
            } else {
                text = entity.name;
            }


            if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title=\"" + showTitle.escapeHTML(true) + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeHTML(true)).append("</div>");

            } else {
                selectHTML.append("<option title=\"" + showTitle.escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeSpace()).append("</option>");
            }

        }
    }

    selectHTML.append(select2_tag_subfix);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

/**
 * 在Area2显示部门下的岗位
 */
function showSubOfBusinessDepartment(departmentId, subType, keyword) {
    var businessDepartment = topWindow.getObject(Constants_BusinessDepartment, departmentId);
    if (!businessDepartment) {
        return;
    }

    var entites = eval("businessDepartment.get" + subType + "s()");
    var selectHTML = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        selectHTML.append(memberDataBody_div);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    if (entites) {
        for (var i = 0; i < entites.size(); i++) {
            var entity = entites.get(i);

            if (entity == null) {
                continue;
            }

            if (keyword && entity.name.toLowerCase().indexOf(keyword) < 0) {
                continue;
            }

            var _id = departmentId + valuesJoinSep + entity.id;
            var _type = Constants_BusinessDepartment + valuesJoinSep + subType;
            //该数据不显示
            if (excludeElements.contains(_type + _id)) {
                continue;
            }

            var text = null;
            var showTitle = "";
            if (entity.code) {
                text = entity.name.getLimitLength(nameMaxLength.two[0]);
                if (text != entity.name) {
                    showTitle = entity.name.escapeSpace();
                }
                text += getNameSpace(nameMaxLength.two[0] + nameMaxSpace - text.getBytesLength());
                text += entity.code;
            } else {
                text = entity.name;
            }


            if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title=\"" + showTitle.escapeHTML(true) + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeHTML(true)).append("</div>");

            } else {
                selectHTML.append("<option title=\"" + showTitle.escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeSpace()).append("</option>");
            }

        }
    }

    selectHTML.append(select2_tag_subfix);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

/**
 * 在单位下查询所有的部门-岗位 关系（用于支持搜索副岗）
 */
function showDepartmentPostOfAccount(accountId, subType, keyword) {
    if (keyword == arrayJoinSep) {
        return;
    }

    var account = topWindow.getObject(Constants_Account, accountId);
    if (!account) {
        return;
    }

    var selectHTML = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        selectHTML.append(memberDataBody_div);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    var departments = topWindow.getDataCenter(Constants_Department, accountId);
    for (var d = 0; d < departments.size(); d++) {
        var department = departments.get(d);
        if (!department) {
            return;
        }
        var departmentId = department.id;
        var departmentName = department.name;
        var entites = eval("department.get" + subType + "s()");

        if (entites) {
            for (var i = 0; i < entites.size(); i++) {
                var entity = entites.get(i);

                if (entity == null) {
                    continue;
                }

                var deptPostName = departmentName + arrayJoinSep + entity.name;
                if (keyword && deptPostName.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                var _id = departmentId + valuesJoinSep + entity.id;
                var _type = Constants_Department + valuesJoinSep + subType;
                //该数据不显示
                if (excludeElements.contains(_type + _id)) {
                    continue;
                }

                var text = null;
                var showTitle = "";
                if (entity.code) {
                    text = entity.name.getLimitLength(nameMaxLength.two[0]);
                    if (text != entity.name) {
                        showTitle = entity.name.escapeSpace();
                        showTitle = departmentName + arrayJoinSep + showTitle;
                    }
                    text += getNameSpace(nameMaxLength.two[0] + nameMaxSpace - text.getBytesLength());
                    text += entity.code;
                } else {
                    text = entity.name;
                }

                text = departmentName + arrayJoinSep + text;

                if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                    selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title=\"" + showTitle.escapeHTML(true) + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeHTML(true)).append("</div>");

                } else {
                    selectHTML.append("<option title=\"" + showTitle.escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeSpace()).append("</option>");
                }

            }
        }
    }


    selectHTML.append(select2_tag_subfix);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}


/**
 * 在Area2显示单位下的岗位
 */
function showSubOfAccount(accountId, subType, keyword) {
    var entites = topWindow.getCustomerAccountRole(accountId);
    if (!entites) {
        return;
    }

    var selectHTML = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        selectHTML.append(memberDataBody_div);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    if (entites) {
        for (var i = 0; i < entites.length; i++) {
            var entity = entites[i];

            if (entity == null) {
                continue;
            }

            if (keyword && entity.name.toLowerCase().indexOf(keyword) < 0) {
                continue;
            }

            var _id = accountId + valuesJoinSep + entity.id;
            var _type = Constants_Account + valuesJoinSep + Constants_Role;

            var text = null;
            var showTitle = "";
            if (entity.code) {
                text = entity.name.getLimitLength(nameMaxLength.two[0]);
                if (text != entity.name) {
                    showTitle = entity.name.escapeSpace();
                }
                text += getNameSpace(nameMaxLength.two[0] + nameMaxSpace - text.getBytesLength());
                text += entity.code;
            } else {
                text = entity.name;
            }


            if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title=\"" + showTitle.escapeHTML(true) + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeHTML(true)).append("</div>");

            } else {
                selectHTML.append("<option title=\"" + showTitle.escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeSpace()).append("</option>");
            }

        }
    }

    selectHTML.append(select2_tag_subfix);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

function showList2OfNode(type, id, keyword, sp) {
    var node = topWindow.getObject(type, id, currentAccountId);
    if (!node) {
        return;
    }

    var selectHTML = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        selectHTML.append(memberDataBody_div);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    var showBusinessOrg = false;
    var businessId = -1;
    if (canShowBusinessOrg && (type == Constants_Node || type == Constants_FormField)) {
        var businessId = $("#areaTopList1_BusinessDepartment").val();
        if (businessId != -1 && businessId != null) {
            showBusinessOrg = true;
        }
    }
    var entites = node.getRoles();//行政组织下的角色
    if (showBusinessOrg) {
        entites = new ArrayList();
        var roles = topWindow.getDataCenter(Constants_BusinessRole, currentAccountId);
        for (var i = 0; i < roles.size(); i++) {
            var role = roles.get(i);
            if (businessId == role.businessId) {
                entites.add(role);
            }
        }
    }

    if (entites) {
        var notShowAccountRole = getParentWindowData("notShowAccountRole") || false;
        for (var i = 0; i < entites.size(); i++) {
            var entity = entites.get(i);

            if (entity == null) {
                continue;
            }
            if (showBusinessOrg) {//多维组织角色
                if (keyword && entity.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                //相对角色 发起者,上节点,发起者上级部门，发起者主管各部门，发起者分管各部门，上节点上级部门， 上节点主管各部门，上节点分管各部门  下，显示多维组织部门角色
                if (type == Constants_Node
                    && id != "Sender" && id != "NodeUser" && id != "SenderSuperDept" && id != "SenderManageDep" && id != "SenderLeaderDep"
                    && id != "NodeUserSuperDept" && id != "NodeUserManageDep" && id != "NodeUserLeaderDep"
                    && id != "CurrentNode") {
                    continue;
                }

                //表单控件中，只有选人，多选人，显示多维组织角色
                if (type == Constants_FormField && id.toLowerCase().indexOf("multimember@field") < 0 && id.toLowerCase().indexOf("member@field") < 0) {
                    continue;
                }

                var _type = type;
                var _id = node.id + sp + entity.id;

                //该数据不显示
                if (excludeElements.contains(_type + _id) || !checkIncludeElements(_type, _id)) {
                    continue;
                }

                var text = entity.name;
                var preShow = entity.preShow;

                if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                    selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title=\"" + text.escapeHTML(true) + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeHTML(true)).append("</div>");

                } else {
                    selectHTML.append("<option title=\"" + (preShow + "-" + node.name + text).escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeSpace()).append("</option>");
                }

            } else {//行政组织下的角色
                if (keyword && entity.N.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                //相对角色下，发起者，上节点，发起者上级单位，上节点上级单位,当前节点，当前节点上级单位 下显示：集团定义的角色
                if (type == "Node" && id != "Sender" && id != "NodeUser" && id != "SenderSuperAccount" && id != "NodeUserSuperAccount" && id != "CurrentNode" && id != "CurrentNodeSuperAccount" && entity.T == 2 && entity.B == 1) {
                    continue;
                }

                //相对角色下， 发起者上级单位，上节点上级单位,当前节点上级单位  下只显示：集团定义的角色
                if (type == "Node" && (id == "SenderSuperAccount" || id == "NodeUserSuperAccount" || id == "CurrentNodeSuperAccount") && entity.B != 1) {
                    continue;
                }

                //相对角色发起者或者上节点下，显示汇报人。
                if (type == "Node" && id != "Sender" && id != "NodeUser" && id != "CurrentNode" && entity.K.indexOf("ReciprocalRoleReporter") >= 0) {
                    continue;
                }

                //相对角色发起者或者上节点下，显示人员的自定义元数据（选人）信息。
                if (type == "Node" && id != "Sender" && id != "NodeUser" && id != "CurrentNode" && entity.K.indexOf("MemberMetadataRole") >= 0) {
                    continue;
                }

                //表单控件中，只有选人和多选人时，显示汇报人
                if (type == Constants_FormField && id.toLowerCase().indexOf("multimember@field") < 0 && id.toLowerCase().indexOf("member@field") < 0 && entity.K.indexOf("ReciprocalRoleReporter") >= 0) {
                    continue;
                }

                //表单控件中，只有选人，多选人，单位，多单位时，显示单位角色
                if (type == Constants_FormField
                    && id.toLowerCase().indexOf("multimember@field") < 0 && id.toLowerCase().indexOf("member@field") < 0
                    && id.toLowerCase().indexOf("multiaccount@field") < 0 && id.toLowerCase().indexOf("account@field") < 0
                    && id.toLowerCase().indexOf("accountanddepartment@field") < 0
                    && entity.T == 2 && entity.B == 1) {
                    continue;
                }

                //表单控件中，只有单位，多单位时，只显示单位角色
                if (type == Constants_FormField
                    && (id.toLowerCase().indexOf("multiaccount@field") >= 0 || id.toLowerCase().indexOf("account@field") >= 0)
                    && entity.B != 1) {
                    continue;
                }

                //表单控件中，只有选人，多选人时，显示人员的自定义元数据（选人）信息。
                if (type == Constants_FormField
                    && id.toLowerCase().indexOf("multimember@field") < 0 && id.toLowerCase().indexOf("member@field") < 0
                    && entity.K.indexOf("MemberMetadataRole") >= 0) {
                    continue;
                }

                //如果设置了不显示单位角色，直接跳过
                if (type == Constants_FormField && entity.T == 2 && entity.B == 1 && notShowAccountRole) {
                    continue;
                }
                var _type = type;
                var _id = node.id + sp + entity.K;

                //该数据不显示
                if (excludeElements.contains(_type + _id) || !checkIncludeElements(_type, _id)) {
                    continue;
                }

                var text = entity.N;

                if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                    selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title=\"" + text.escapeHTML(true) + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeHTML(true)).append("</div>");

                } else {
                    selectHTML.append("<option title=\"" + (node.name + text).escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(text.escapeSpace()).append("</option>");
                }
            }

        }
    }

    selectHTML.append(select2_tag_subfix);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

function showList2OfCustom(type, id, keyword) {
    var custom = topWindow.getObject(type, id, currentAccountId);
    if (!custom) {
        return;
    }

    var entites = custom.getRelationData();
    var selectHTML = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        selectHTML.append(memberDataBody_div);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    var customPanel = topWindow.Constants_Custom_Panels.get(type);

    var area2SelectMode = customPanel.area2SelectMode;
    var sp = customPanel.sp;

    if (entites) {
        for (var i = 0; i < entites.size(); i++) {
            var entity = entites.get(i);

            if (entity == null) {
                continue;
            }

            if (keyword && entity.N.toLowerCase().indexOf(keyword) < 0) {
                continue;
            }

            var _type = type;
            var _id = custom.id + sp + entity.K;

            //该数据不显示
            if (excludeElements.contains(_type + _id) || !checkIncludeElements(_type, _id)) {
                continue;
            }

            var _text = custom.name + entity.N;

            //区域2的内容单独选择
            if (area2SelectMode == 'SINGLE') {
                _type = (entity.T == undefined || entity.T == null) ? type : entity.T;
                _id = entity.K;
                _text = entity.N;
            }

            selectHTML.append("<option title=\"" + _text.escapeSpace() + "\" value='").append(_id).append("' type='").append(_type).append("'>").append(entity.N.escapeSpace()).append("</option>");
        }
    }

    selectHTML.append(select2_tag_subfix);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

/**
 * 显示组的关联人员
 */
function showTeamRelativeMembers() {
    var id = Constants_Team + "DataBody";
    var dataBody = document.getElementById(id);
    if (dataBody) {
        var s = dataBody.value;
        if (s) {
            tempNowSelected.clear();
            clearList2();
            addTeamMember2List2(s);

            selectList1Item(Constants_Team, dataBody);
        }
    }
}

/**
 * ??member???header
 */
function showMemberTitle(type) {
    var name = "";

    if (type == Constants_Department) {//
        name = Constants_Component.get(Constants_Post);
    } else if (type == Constants_Role) {

    } else { //
        name = Constants_Component.get(Constants_Department);
    }

    document.getElementById("memberTitle2").innerHTML = "&nbsp;" + name;
}

function getFormFieldListHTMLStr(keyword, selectBusinessAccountId) {

    var id = Constants_FormField + "DataBody";
    var datas = topWindow.getDataCenter(Constants_FormField);
    var size = tempNowPanel.isShowMember && checkCanSelectMember() && checkIsShowArea2() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;

    var html = new StringBuffer();
    html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_FormField + "', this)\" ondblclick=\"selectOne('" + Constants_FormField + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");

    if (datas) {
        //var postTypeId = document.getElementById("areaTopList1").value;
        for (var i = 0; i < datas.size(); i++) {
            var item = datas.get(i);

            if (canShowBusinessOrg) {
                if (selectBusinessAccountId && selectBusinessAccountId != -1) {
                    if (item.id.toLowerCase().indexOf("multimember@field") < 0 && item.id.toLowerCase().indexOf("member@field") < 0) {
                        continue;
                    }
                }

            }
            var name = item.name || "";
            try {
                if (keyword && name.toLowerCase().indexOf(keyword) == -1) {
                    continue;
                }
                html.append("<option title=\"" + name.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_FormField).append("\">").append(name.escapeHTML(true)).append("</option>");
            } catch (e) {
                log.error("", e);
            }
        }
    }

    html.append("</select>");

    return html.toString();
}


function getWFDynamicFormListHTMLStr(keyword) {
    var id = Constants_WFDynamicForm + "DataBody";
    var datas = topWindow.getDataCenter(Constants_WFDynamicForm);
    var size = tempNowPanel.isShowMember && checkCanSelectMember() && checkIsShowArea2() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;
    size = size - 50;
    var html = new StringBuffer();
    html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_FormField + "', this)\" ondblclick=\"selectOne('" + Constants_FormField + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");

    if (datas) {

        var _FID = document.getElementById("areaTopList1_WFDynamicForm").value;
        var isNeedInitAreaTopList1 = false;


        var FO = {};
        for (var i = 0; i < datas.size(); i++) {
            var item = datas.get(i);
            var name = item.name || "";

            FO[item.formId] = item.formName;

            try {
                if (keyword && name.toLowerCase().indexOf(keyword) == -1) {
                    continue;
                }
                if (!_FID) {
                    _FID = item.formId;
                    isNeedInitAreaTopList1 = true;
                }

                if (_FID != item.formId) {
                    continue;
                }
                html.append("<option title=\"" + name.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_WFDynamicForm).append("\">").append(name.escapeHTML(true)).append("</option>");
            } catch (e) {
                log.error("", e);
            }
        }


        if (isNeedInitAreaTopList1) {
            var topHtml = new StringBuffer();
            for (var i in FO) {
                var topId = i;
                var topName = FO[i];
                topHtml.append("<option title=\"" + topName.escapeHTML(true) + "\" value=\"").append(topId).append("\" type=\"").append(Constants_WFDynamicForm).append("\">").append(topName.escapeHTML(true)).append("</option>");
            }
            document.getElementById("areaTopList1_WFDynamicForm").innerHTML = topHtml.toString();
        }
    }

    html.append("</select>");

    return html.toString();
}


function getOfficeFieldListHTMLStr(keyword) {
    var id = Constants_OfficeField + "DataBody";
    var datas = topWindow.getDataCenter(Constants_OfficeField);
    var size = tempNowPanel.isShowMember && checkCanSelectMember() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;

    var html = new StringBuffer();
    html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_OfficeField + "', this)\" ondblclick=\"selectOne('" + Constants_OfficeField + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");

    if (datas) {
        for (var i = 0; i < datas.size(); i++) {
            var item = datas.get(i);
            var name = item.name || "";
            try {
                if (keyword && name.toLowerCase().indexOf(keyword) == -1) {
                    continue;
                }
                html.append("<option title=\"" + name.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_OfficeField).append("\">").append(name.escapeHTML(true)).append("</option>");
            } catch (e) {
                log.error("", e);
            }
        }
    }

    html.append("</select>");

    return html.toString();
}

function getRoleListHtmlStr(keyword, selectAccountId) {
    var id = Constants_Role + "DataBody";
    var datas = topWindow.getDataCenter(Constants_Role, selectAccountId);
    var size = tempNowPanel.isShowMember && checkCanSelectMember() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;

    var html = new StringBuffer();
    html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_Role + "', this)\" ondblclick=\"selectOne('" + Constants_Role + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");

    if (datas) {
        //var postTypeId = document.getElementById("areaTopList1").value;
        var showFixedRole = getParentWindowData("showFixedRole", false);

        for (var i = 0; i < datas.size(); i++) {
            var item = datas.get(i);
            var name = item.name || "";

            if (keyword && name.toLowerCase().indexOf(keyword) == -1) {
                continue;
            }

            var value = item.id;

            if (item.bond == 2) {
                if (showFixedRole) {
                    //保留
                } else {
                    continue;
                }
            } else {
                if (showFixedRole) {
                    continue;
                }
            }

            html.append("<option title=\"" + name.escapeHTML(true) + "\" value=\"").append(value).append("\" type=\"").append(Constants_Role).append("\">").append(name.escapeHTML(true)).append("</option>");
        }
    }

    html.append("</select>");

    return html.toString();
}

function getCustomFieldListHTMLStr(type, keyword) {
    var customPanel = topWindow.Constants_Custom_Panels.get(type);
    var id = type + "DataBody";
    var datas = topWindow.getDataCenter(type);
    var size = customPanel.isShowArea2 == 'true' ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;
    //size = size - 50;
    var html = new StringBuffer();
    html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + type + "', this)\" ondblclick=\"selectOne('" + type + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");
    if (datas) {
        for (var i = 0; i < datas.size(); i++) {
            var item = datas.get(i);
            var name = item.name || "";
            try {
                if (keyword && name.toLowerCase().indexOf(keyword) == -1) {
                    continue;
                }
                html.append("<option title=\"" + name.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(type).append("\">").append(name.escapeHTML(true)).append("</option>");
            } catch (e) {
                log.error("", e);
            }
        }
    }

    html.append("</select>");

    return html.toString();
}

/**
 *
 */
function initList(type, keyword, selectBusinessAccountId) {
//	var startTime = new Date().getTime();

    var id = type + "DataBody";

    if (document.getElementById("AreaTop1_" + type)) {
        document.getElementById("AreaTop1_" + type).style.display = "";
    } else if (canShowBusinessOrg) {//显示多维组织信息
        if (type == Constants_Node || type == Constants_FormField || type == Constants_BusinessRole) {
            var businessAccounts = new ArrayList();
            var allBusinessAccounts = topWindow.getDataCenter(Constants_BusinessAccount, currentAccountId);
            if (!isAdmin) {
                var alwaysShowBusiness = getParentWindowData("alwaysShowBusiness") || "";
                for (var i = 0; i < allBusinessAccounts.size(); i++) {
                    var ba = allBusinessAccounts.get(i);
                    var isPublic = ba.isPublic;
                    if (isPublic == true || isPublic == 'true' || alwaysShowBusiness.indexOf(ba.id) >= 0) {//公开的业务线都能看见
                        businessAccounts.add(ba);
                    } else {
                        var memberIds = ba.memberIds;//私有的业务线只有业务线内的人可见
                        if (memberIds.indexOf(currentMemberId.toString()) >= 0) {
                            businessAccounts.add(ba);
                        }
                    }
                }
            } else {
                businessAccounts = allBusinessAccounts;
            }

            $("#areaTopList1_BusinessDepartment option").remove();
            if ((type == Constants_Node || type == Constants_FormField) && loginAccountId == currentAccountId) {
                $("#areaTopList1_BusinessDepartment").append("<option value='-1'>行政组织</option>");
                if (!selectBusinessAccountId) {
                    selectBusinessAccountId = -1;
                }
            } else {
                var currentBusinessAccount = getParentWindowData("currentBusinessAccount");
                if (!selectBusinessAccountId && businessAccounts.size() > 0) {
                    if (currentBusinessAccount) {
                        selectBusinessAccountId = currentBusinessAccount;
                    } else {
                        selectBusinessAccountId = businessAccounts.get(0).id;
                    }
                }
            }

            for (var i = 0; i < businessAccounts.size(); i++) {
                var ba = businessAccounts.get(i);
                var bId = ba.id;
                var bName = ba.name;

                if (!selectBusinessAccountId) {
                    selectBusinessAccountId = bId;
                }
                if (selectBusinessAccountId == bId) {
                    $("#areaTopList1_BusinessDepartment").append("<option value='" + bId + "' selected='selected'>" + bName + "</option>");
                } else {
                    $("#areaTopList1_BusinessDepartment").append("<option value='" + bId + "'>" + bName + "</option>");
                }
            }

            if (businessAccounts.size() == 0 && loginAccountId != currentAccountId) {
                document.getElementById("AreaTop1_BusinessDepartment").style.display = "none";
            } else {
                document.getElementById("AreaTop1_BusinessDepartment").style.display = "";
            }

            reArea_1_2();
        }

    }

    var str = null;
    if (type == Constants_Team) {
        str = getTeamListHTMLStr(keyword);
    } else if (type == Constants_Post || type == Constants_JoinPost) {
        clearList2();
        if (type == Constants_JoinPost) {
            if (isV5Member && !isInternal) {
            } else {
                str = getPostListHTMLStr(keyword, currentVjoinAccountId, false);
            }
        } else {
            str = getPostListHTMLStr(keyword, currentAccountId, true);
        }
    } else if (type == Constants_Guest) {
        clearList2();
        str = getGuestListHTMLStr(keyword, currentAccountId);
    } else if (type == Constants_RelatePeople) {
        str = getRelatePeopleListHTMLStr(keyword, currentAccountId);
    } else if (type == Constants_OrgRecent) {
        clearList2();
        str = getOrgRecentHTMLStr(keyword, currentAccountId);
    } else if (type == Constants_FormField) {
        clearList2();
        str = getFormFieldListHTMLStr(keyword, selectBusinessAccountId);
    } else if (type == Constants_WFDynamicForm) {
        clearList2();
        str = getWFDynamicFormListHTMLStr(keyword);
    } else if (type == Constants_OfficeField) {
        clearList2();
        str = getOfficeFieldListHTMLStr(keyword);
    } else if (type == Constants_Role) {
        clearList2();
        str = getRoleListHtmlStr(keyword, currentAccountId);
    } else if (topWindow.Constants_Custom_Panels.keys() != null && topWindow.Constants_Custom_Panels.keys().contains(type)) {//自定义页签的列表展示
        clearList2();
        str = getCustomFieldListHTMLStr(type, keyword);
    } else {
        var _selectType = "";
        var _accountId = "";
        var datas = new ArrayList();
        if (type == Constants_JoinAccount) {
            _selectType = Constants_Department;
            _accountId = currentVjoinAccountId;
            if (isV5Member && !isInternal) {
            } else {
                datas = topWindow.getDataCenter(_selectType, _accountId);
            }
        } else if (type == Constants_BusinessAccount) {
            _selectType = type;
            _accountId = currentAccountId;
            var allBusinessAccounts = topWindow.getDataCenter(type, _accountId);
            if (!isAdmin) {
                var alwaysShowBusiness = getParentWindowData("alwaysShowBusiness") || "";
                for (var i = 0; i < allBusinessAccounts.size(); i++) {
                    var ba = allBusinessAccounts.get(i);
                    var isPublic = ba.isPublic;
                    if (isPublic == true || isPublic == 'true' || alwaysShowBusiness.indexOf(ba.id) >= 0) {//公开的业务线都能看见
                        datas.add(ba);
                    } else {
                        var memberIds = ba.memberIds;//私有的业务线只有业务线内的人可见
                        if (memberIds.indexOf(currentMemberId.toString()) >= 0) {
                            datas.add(ba);
                        }
                    }
                }
            } else {
                datas = allBusinessAccounts;
            }

        } else if (type == Constants_BusinessRole) {
            _selectType = type;
            _accountId = currentAccountId;
            datas = new ArrayList();
            var businessRoles = topWindow.getDataCenter(type, _accountId);
            for (var i = 0; i < businessRoles.size(); i++) {
                var businessRole = businessRoles.get(i);
                if (businessRole.businessId == selectBusinessAccountId) {
                    datas.add(businessRole);
                }
            }

        } else if (type == Constants_Node || type == Constants_FormField) {
            _selectType = type;
            _accountId = currentAccountId;
            datas = new ArrayList();
            if (selectBusinessAccountId && selectBusinessAccountId != -1) {
                datas = new ArrayList();
                var tempDatas = topWindow.getDataCenter(_selectType, _accountId);
                for (var i = 0; i < tempDatas.size(); i++) {
                    var td = tempDatas.get(i);
                    //多维角色相对角色只显示  发起者,上节点,发起者上级部门，发起者主管各部门，发起者分管各部门，上节点上级部门， 上节点主管各部门，上节点分管各部门，当前节点
                    if (td.id == "Sender" || td.id == "NodeUser" || td.id == "SenderSuperDept" || td.id == "SenderManageDep" || td.id == "SenderLeaderDep"
                        || td.id == "NodeUserSuperDept" || td.id == "NodeUserManageDep" || td.id == "NodeUserLeaderDep"
                        || td.id == "CurrentNode") {
                        datas.add(td);
                    }
                }
            } else {
                _selectType = type;
                _accountId = loginAccountId;
                datas = topWindow.getDataCenter(_selectType, _accountId);
            }
        } else {
            _selectType = type;
            _accountId = currentAccountId;
            datas = topWindow.getDataCenter(_selectType, _accountId);
        }

        var size = tempNowPanel.isShowMember && checkCanSelectMember() && !isRootAccount() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;
        var html = new StringBuffer();
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + _selectType + "', this)\" ondblclick=\"selectOne('" + _selectType + "', this)\" multiple style='padding-top: 2px;width:368px; overflow:auto; border:none; height: " + size + "px'>");
        } else {
            var classStr = tempNowPanel.isShowMember && checkCanSelectMember() ? 'team-list' : 'relatePeople-list';
            html.append("<div id=\"" + id + "\"  class=\"" + classStr + "\">");
        }

        if (datas) {
            var secondPostDepartmentPaths = null;
            if (currentMember) {
                secondPostDepartmentPaths = new ArrayList();
                secondPostDepartmentPaths.add(currentMember.getDepartment().path);

                var departIds = currentMember.getSecondPost().keys();
                for (var i = 0; i < departIds.size(); i++) {
                    var department = topWindow.getObject(Constants_Department, departIds.get(i));
                    if (department) {
                        secondPostDepartmentPaths.add(department.path);
                    }
                }
            }

            var showAdminTypes = new ArrayList();
            if (getParentWindowData("showAdminTypes", "")) {
                showAdminTypes.addAll(getParentWindowData("showAdminTypes", "").split(","));
            }

            for (var i = 0; i < datas.size(); i++) {
                var item = datas.get(i);

                //过滤外部机构
                if (type == Constants_JoinAccount) {
                    if (item.externalType != "2") {
                        continue;
                    }
                    //如果是vjoin人员，只保留自己的外部单位和其他可见的外单位
                    if (isVjoinMember && isAdministrator != true) {
                        var _VjMemberAccessVjAccounts = topWindow.VjMemberAccessVjAccounts;
                        if (_VjMemberAccessVjAccounts != null && !_VjMemberAccessVjAccounts.contains("D" + item.id)) {//可以访问本外部单位可访问的其他外部单位下的人员
                            continue;
                        }
                    }
                }

                if (keyword) {
                    var text = item.name.toLowerCase();
                    if (text.indexOf(keyword) == -1) {
                        continue;
                    }
                }

                //该数据不显示
                if (excludeElements.contains(_selectType + item.id) || !checkIncludeElements(_selectType, item.id)) {
                    continue;
                }

                if (_selectType == Constants_Admin && !showAdminTypes.isEmpty()) {
                    if (showAdminTypes.contains(item.role) || showAdminTypes.contains(item.role + "_" + item.id)) {
                        //保留
                    } else {
                        continue;
                    }
                }

                var text = null;
                var showTitle = "";

                if (_selectType == Constants_Outworker) {
                    //当前登录者是外部人员,只能看到自己的部门
                    if (!showAllOuterDepartmentFlag && !topWindow.ExtMemberScopeOfInternal.containsKey(item.id)) {
                        if (currentMember && !currentMember.isInternal) {
                            if (!item.isInternal && item.id != currentMember.departmentId) {
                                continue;
                            }
                        } else {
                            //上级是部门
                            if (secondPostDepartmentPaths && !secondPostDepartmentPaths.isEmpty() && item.parentDepartment) {
                                var isShow = false;
                                var parentPathOfOuter = item.parentDepartment.path;
                                for (var k = 0; k < secondPostDepartmentPaths.size(); k++) {
                                    var p = secondPostDepartmentPaths.get(k);
                                    if (p.startsWith(parentPathOfOuter)) {
                                        isShow = true;
                                        break;
                                    }
                                }

                                if (!isShow) {
                                    continue;
                                }
                            }
                        }
                    }

                    var ts = getOutworkerListOptionText(item);
                    text = ts[0];
                    showTitle = ts[1];
                } else if (item.code) {
                    text = item.name.getLimitLength(nameMaxLength.two[0]);
                    if (text != item.name) {
                        showTitle = item.name.escapeSpace();
                    }
                    text += getNameSpace(nameMaxLength.two[0] + nameMaxSpace - text.getBytesLength());
                    text += item.code;
                } else {
                    text = item.name;
                }
                if (v3x.getBrowserFlag('selectPeopleShowType')) {
                    html.append("<option title=\"" + (text.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;") + "\" value=\"").append(item.id).append("\" type=\"").append(_selectType).append("\" accountId=\"").append(_accountId).append("\">").append((text.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;")).append("</option>");
                } else {
                    html.append("<div class='member-list-div' seleted='false' ondblclick=\"selectOne('" + _selectType + "',this,'" + id + "')\"  onclick=\"selectList1ItemDiv('" + _selectType + "','" + id + "',this)\"  title=\"" + showTitle.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(_selectType).append("\" accountId=\"").append(_accountId).append("\">").append((text.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;")).append("</div>");
                }
            }
        }
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            html.append("</select>");
        } else {
            html.append("</div>");
        }
        str = html.toString();
    }

    document.getElementById("Area1").innerHTML = str;
    document.getElementById("Area1").className = "";
    initIpadScroll(id);//ipad滚动条解决

//	log.debug("显示列表耗时：" + (new Date().getTime() - startTime) + "MS");
}

function getTeamListHTMLStr(keyword) {
    var id = Constants_Team + "DataBody";
    var size = tempNowPanel.isShowMember && checkCanSelectMember() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;

    var datas = topWindow.getDataCenter(Constants_Team, currentAccountId);
    var html = new StringBuffer();
    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_Team + "', this)\" ondblclick=\"selectOne('" + Constants_Team + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");
    } else {
        html.append("<div id=\"" + id + "\" class='team-list'>");
    }
    if (datas) {
        var item;
        var text;
        var typeName; //类型名称：1-个人 2-系统(单位、集团) 3-项目
        var showText;
        var temp;
        var tempIndex;
        var len = datas.size();
        for (var i = 0; i < len; i++) {
            try {
                item = datas.get(i);

                //排除不需要显示的类型
                if (showTeamType && !showTeamType.contains("" + item.type)) {
                    continue;
                }

                if (keyword) {
                    text = item.name.toLowerCase();
                    if (text.indexOf(keyword) == -1) {
                        continue;
                    }
                }

                //该数据不显示
                if (excludeElements.contains(Constants_Team + item.id) || !checkIncludeElements(Constants_Team, item.id)) {
                    continue;
                }

                typeName = ""; //类型名称：1-个人 2-系统(单位、集团) 3-项目

                if (item.type == 1) {
                    typeName = $.i18n("selectPeople.personalTeam");
                } else if (item.type == 2) {
                    var dep = item.getDepartment();
                    if (dep) {
                        typeName = dep.name;
                    } else {
                        var a = allAccounts.get(item.accountId);
                        if (a) {
                            typeName = a.shortname;
                        }
                    }

                    if (!typeName) {
                        typeName = $.i18n("selectPeople.accountTeam");
                    }
                } else if (item.type == 3) {
                    typeName = $.i18n("selectPeople.projectTeam");
                }

                showText = item.name.getLimitLength(nameMaxLength.two[0]);
                showText += $.browser.chrome ? (showText.getBytesLength() % 2 > 0 ? "|," : "") : "";
                if ($.browser.safari) {
                    temp = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
                    tempIndex = (temp - (nameMaxLength.two[0] - temp - 3)) > 20 ? 20 : temp - (nameMaxLength.two[0] - temp - 3);
                    showText += getNameSpace(tempIndex);
                } else {
                    tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
                    showText += getNameSpace(tempIndex);
                }
                showText += typeName;
                showText = showText.toString().replace("\|,", " ");
                if (v3x.getBrowserFlag('selectPeopleShowType')) {
                    //zhou
                    html.append("<option ondblclick=\"dbClickDeptSelectedMember()\" title=\"" + item.name.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"Team\" accountId=\"").append(item.accountId).append("\">").append((showText.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;")).append("</option>");
                } else {
                    html.append("<div  class='member-list-div' seleted='false' ondblclick=\"selectOne('" + Constants_Team + "',this,'" + id + "')\"  onclick=\"selectList1ItemDiv('" + Constants_Team + "','" + id + "',this)\"  title=\"" + item.name.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"Team\" accountId=\"").append(item.accountId).append("\">").append((showText.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;")).append("</div>");
                }

            } catch (e) {
                log.error("", e);
            }
        }
    }

    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("</select>");
    } else {
        html.append("</div>");
    }

    return html.toString().replace("\|,", " ");
}



function getOrgRecentHTMLStr(keyword, selectAccountId) {
    var id = Constants_OrgRecent + "DataBody";
    var size = tempNowPanel.isShowMember && checkCanSelectMember() ? Constants_List1_size.showMember : Constants_List1_size.noShowMember;
    size = size - 20;

    var html = new StringBuffer();
    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_Member + "', this)\" ondblclick=\"selectOne('" + Constants_Member + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height:" + size + "px'>");
    } else {
        html.append("<div id=\"" + id + "\" class='recent-list'>");
    }
    var isCanSelectOuter = checkCanSelect(Constants_Member);
    var datas = topWindow.getDataCenter(Constants_OrgRecent, selectAccountId);
    if (datas) {
        for (var i = 0; i < datas.size(); i++) {
            var item = datas.get(i).getOneMember();
            if (item) {
                var text = item.name;
                if (keyword && keyword != document.getElementById("q").defaultValue) {
                    if (text.indexOf(keyword) == -1) {
                        continue;
                    }
                }
                //该数据不显示
                if (excludeElements.contains(Constants_Member + item.id) || !checkIncludeElements4Member(item) || (!isCanSelectOuter && !item.isInternal)) {
                    continue;
                }
                html.append(addMember(Constants_RelatePeople, null, item));
            }
        }
    }

    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("</select>");
    } else {
        html.append("</div>");
    }

    return html.toString();
}

function getPostListHTMLStr(keyword, selectAccountId, showAreaTop1) {
    pagingParam = new Properties();
    var id = Constants_Post + "DataBody";
    var size = tempNowPanel.isShowMember && checkCanSelectMember() && !isRootAccount() ? Constants_List1_size.showMember_1 : Constants_List1_size.noShowMember;
    size = size - 50;
    if (!showAreaTop1) {
        size = size + 26;
    }
    var html = new StringBuffer();
    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_Post + "', this)\" ondblclick=\"selectOne('" + Constants_Post + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");
    } else {
        html.append("<div id=\"" + id + "\" class='post-list'>");
    }

    if (selectAccountId != null) {
        var datas = topWindow.getDataCenter(Constants_Post, selectAccountId);
        if (datas) {
            //设置分页需要的参数
            pagingParam.put("keyword", keyword);
            pagingParam.put("selectAccountId", selectAccountId);
            pagingParam.put("datas", datas);
            //首次加载一页数据
            var pagingHtml = getPostListHTMLStrPaging(0, keyword, selectAccountId, datas);
            if (pagingHtml != "") {
                html.append(pagingHtml);
            }
        }
    }

    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("</select>");
    } else {
        html.append("</div>");
    }
    return html.toString();
}

function getPostListHTMLStrPaging(startIndex, keyword, selectAccountId, datas) {
    var html = new StringBuffer();
    var postTypeId = document.getElementById("areaTopList1_Post").value;
    var isShowAllPosts = ("AllPosts" == postTypeId);

    var item;
    var text;
    var showTitle;
    var text1;
    var titleShow;
    var len = datas.size();
    var firstPageSize = 0;
    for (var i = startIndex; i < len; i++) {
        try {
            item = datas.get(i);
            if (!isShowAllPosts && item.type != postTypeId) {
                continue;
            }

            if (keyword) {
                text = item.name.toLowerCase();
                if (text.indexOf(keyword) == -1) {
                    continue;
                }
            }

            //该数据不显示
            if (excludeElements.contains(Constants_Post + item.id) || !checkIncludeElements(Constants_Post, item.id)) {
                continue;
            }

            text = null;
            showTitle = "";
            text1 = null;
            titleShow = "";
            if (item.code) {
                text = item.name.getLimitLength(nameMaxLength.two[0]);
                text1 = item.name;
                if (text != item.name) {
                    showTitle = item.name.escapeSpace();
                }
                text += getNameSpace(nameMaxLength.two[0] + nameMaxSpace - text.getBytesLength());
                text1 += "   ";
                text += item.code;
                text1 += item.code;
            } else {
                text = item.name;
                text1 = item.name;
            }
            if (v3x.getBrowserFlag('selectPeopleShowType')) {
                html.append("<option title=\"" + text1.escapeSpace() + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_Post).append("\" accountId=\"").append(selectAccountId).append("\" externalType=\"").append(item.externalType).append("\">").append(text.escapeSpace()).append("</option>");
            } else {
                html.append("<div class='member-list-div' seleted='false' ondblclick=\"selectOne('" + Constants_Post + "',this,'" + id + "')\"  onclick=\"selectList1ItemDiv('" + Constants_Post + "','" + id + "',this)\"  title=\"" + text.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_Post).append("\" accountId=\"").append(selectAccountId).append("\" externalType=\"").append(item.externalType).append("\">").append(text.escapeHTML(true)).append("</div>");
            }
            firstPageSize++;
            if (startIndex == 0 && firstPageSize >= pageSize) {
                break;
            }
        } catch (e) {
            log.error("", e);
        }
    }

    if (startIndex == 0 && (i + 1) < datas.size()) {//需要显示‘点击加载全部’
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            html.append("<option id='more' startIndex='" + (i + 1) + "' style='margin:4px 0 0 4px;color:blue;cursor:pointer;'>").append($.i18n("selectPeople.data.load.all")).append("</option>");
        } else {
            //html.append("<div class='member-list-div' seleted='false' ondblclick=\"selectOne('" + Constants_Post + "',this,'"+id+"')\"  onclick=\"selectList1ItemDiv('"+Constants_Post+"','"+id+"',this)\"  title=\"" + text.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_Post).append("\" accountId=\"").append(selectAccountId).append("\" externalType=\"").append(item.externalType).append("\">").append(text.escapeHTML(true)).append("</div>");
        }
    }

    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("</select>");
    } else {
        html.append("</div>");
    }
    return html.toString();
}


function getGuestListHTMLStr(keyword, selectAccountId) {
    var id = Constants_Guest + "DataBody";
    var size = Constants_List1_size.noShowMember;
    var html = new StringBuffer();
    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_Guest + "', this)\" ondblclick=\"selectOne('" + Constants_Guest + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height: " + size + "px'>");
    } else {
        html.append("<div id=\"" + id + "\" class='post-list'>");
    }
    var datas = topWindow.getDataCenter(Constants_Guest, selectAccountId);
    if (datas) {
        for (var i = 0; i < datas.size(); i++) {
            try {
                var item = datas.get(i);
                //该数据不显示
                if (excludeElements.contains(Constants_Guest + item.id) || !checkIncludeElements(Constants_Guest, item.id)) {
                    continue;
                }

                if (keyword) {
                    var text = item.name.toLowerCase();
                    if (text.indexOf(keyword) == -1) {
                        continue;
                    }
                }

                var guestAccountId = item.accountId;
                var id = item.id;
                var text = item.name;
                if (v3x.getBrowserFlag('selectPeopleShowType')) {
                    html.append("<option title=\"" + text.escapeSpace() + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_Guest).append("\" accountId=\"").append(guestAccountId).append("\">").append(text.escapeSpace()).append("</option>");
                } else {
                    html.append("<div class='member-list-div' seleted='false' ondblclick=\"selectOne('" + Constants_Guest + "',this,'" + id + "')\"  onclick=\"selectList1ItemDiv('" + Constants_Guest + "','" + id + "',this)\"  title=\"" + text.escapeHTML(true) + "\" value=\"").append(item.id).append("\" type=\"").append(Constants_Guest).append("\" accountId=\"").append(guestAccountId).append("\">").append(text.escapeHTML(true)).append("</div>");
                }
            } catch (e) {
                log.error("", e);
            }
        }
    }

    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("</select>");
    } else {
        html.append("</div>");
    }

    return html.toString();
}

function getRelatePeopleListHTMLStr(keyword, selectAccountId) {
    var id = Constants_RelatePeople + "DataBody";
    var size = Constants_List1_size.noShowMember;
    if ($("#select_input_div_parent").css("display") != "none") {
        size = size - $("#select_input_div_parent").height();
    }
    var html = new StringBuffer();
    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("<select id=\"" + id + "\" onchange=\"selectList1Item('" + Constants_Member + "', this)\" ondblclick=\"selectOne('" + Constants_Member + "', this)\" multiple style='padding-top: 1px;width:368px; overflow:auto; border:none; height:" + size + "px'>");
    } else {
        html.append("<div id=\"" + id + "\" class='relatePeople-list'>");
    }

    var datas = topWindow.getDataCenter(Constants_RelatePeople, selectAccountId);
    if (datas) {
        var toShowType = $("#areaTopList1_RelatePeople").val() || "All";

        for (var i = 0; i < datas.size(); i++) {
            if (toShowType != "All" && toShowType != datas.get(i).type) {
                continue;
            }

            var members = datas.get(i).getMembers(selectAccountId);
            for (var j = 0; j < members.size(); j++) {
                var item = members.get(j);
                var text = item.name;

                if (keyword) {
                    var text = item.name.toLowerCase();
                    if (text.indexOf(keyword) == -1) {
                        continue;
                    }
                }

                if (!excludeElements.contains(Constants_Member + item.id) && checkIncludeElements4Member(item)) {
                    //function addMember(type, entity, member, fullWin, shadowMembers)
                    html.append(addMember(Constants_RelatePeople, null, item));
                }
            }
        }
    }

    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        html.append("</select>");
    } else {
        html.append("</div>");
    }

    return html.toString();
}

function getOutworkerListOptionText(entity) {
    var showText = entity.name.getLimitLength(nameMaxLength.two[0]);
    if ($.browser.safari) {
        var temp = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
        var tempIndex = (temp - (nameMaxLength.two[0] - temp - 3)) > 20 ? 20 : temp - (nameMaxLength.two[0] - temp - 3);
        showText += getNameSpace(tempIndex);
    } else {
        var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
        showText += getNameSpace(tempIndex);
    }


    var showTile = "";
    var typeName = null;
    if (entity.parentDepartment) {
        typeName = entity.parentDepartment.name;
        showTile = entity.parentDepartment.getFullName() + "/" + entity.name;
    } else {
        typeName = currentAccount.shortname;
    }

    showText += typeName;

    return [showText, showTile];
}


/**
 * 隐藏区域1，区域2最大化
 */
function hiddenArea1() {
    if (area12Status == "B") {
        reArea_1_2();
        return;
    }

    var area1Reduction = 0;
    if (tempNowPanel == undefined || tempNowPanel == null) {
        return;
    }
    var AreaTop1Obj = document.getElementById("AreaTop1_" + tempNowPanel.type);
    if ((AreaTop1Obj && AreaTop1Obj.style.display != "none")) {
        area1Reduction = 26;//顶部<所有岗位>高度
    } else if (canShowBusinessOrg) {
        area1Reduction = 26;//顶部<所有岗位>高度
    }

    document.getElementById("Area1").style.display = "none";

    document.getElementById("Area2").style.display = "";
    var memberDataBodyObj = document.getElementById("memberDataBody") || document.getElementById("memberDataBodyOrginal");
    if (memberDataBodyObj) {
        memberDataBodyObj.style.height = (450 - area1Reduction - 50 - 1) + "px";
    }

    document.getElementById("Separator1").style.height = "0";
    document.getElementById("Separator1_0").style.display = "none";
    document.getElementById("Separator1_1").style.display = "none";
    document.getElementById("Separator1_2").style.display = "";
    $("#Separator1_2").css("margin-top", "-1px");

    area12Status = "T";
}

/**
 * 隐藏区域2，区域1最大化
 */
function hiddenArea2(isHiddenSeparator1) {
    if (area12Status == "T") {
        reArea_1_2();
        return;
    }

    var area1Reduction = 0;
    if (window.innerHeight < 500) {
        area1Reduction = 500 - window.innerHeight;
    }
    if (tempNowPanel == undefined || tempNowPanel == null) {
        return;
    }
    var AreaTop1Obj = document.getElementById("AreaTop1_" + tempNowPanel.type);
    if (canShowBusinessOrg) {
        if (tempNowPanel.type == Constants_BusinessDepartment || tempNowPanel.type == Constants_BusinessRole || tempNowPanel.type == Constants_Node || tempNowPanel.type == Constants_FormField) {
            AreaTop1Obj = document.getElementById("AreaTop1_BusinessDepartment");
        }
    }
    if ((AreaTop1Obj && AreaTop1Obj.style.display != "none")) {
        area1Reduction = 26;//顶部<所有岗位>高度
    }
    document.getElementById("Area1").style.display = "";
    document.getElementById("Area1").style.height = (450 - area1Reduction - 50) + "px";
    if (document.getElementById("List1")) {
        document.getElementById("List1").style.height = (450 - area1Reduction - 50 + (isHiddenSeparator1 ? 10 : 0)) + "px";
    }
    try {
        document.getElementById(tempNowPanel.type + "DataBody").style.height = (450 - 50 - area1Reduction) + "px";
    } catch (e) {
    }

    document.getElementById("Area2").style.display = "none";

    document.getElementById("Separator1").style.height = isHiddenSeparator1 ? "0px" : "10px";
    document.getElementById("Separator1_0").style.display = isHiddenSeparator1 ? "none" : "";
    document.getElementById("Separator1_1").style.display = "none";
    document.getElementById("Separator1_2").style.display = "none";
    $("#Separator1_2").css("margin-top", "0");

    area12Status = "B";
}

/**
 * 把左侧(区域1\区域2)还原到原始状态
 *
 * @return
 */
function reArea_1_2() {
    var area1Reduction = 0;

    var AreaTop1Obj = document.getElementById("AreaTop1_" + tempNowPanel.type);
    if (tempNowPanel.type == Constants_Node || tempNowPanel.type == Constants_FormField || tempNowPanel.type == Constants_BusinessRole) {
        AreaTop1Obj = document.getElementById("AreaTop1_" + Constants_BusinessDepartment);
    }

    if (!AreaTop1Obj || AreaTop1Obj.style.display == "none") {
    } else if (AreaTop1Obj && AreaTop1Obj.style.display != "none") {
        area1Reduction = 40;//顶部<所有岗位>高度
    } else if (canShowBusinessOrg) {
        area1Reduction = 40;//顶部<所有岗位>高度
    }

    if (document.getElementById("List1")) {
        document.getElementById("List1").style.height = (Constants_List1_size.showMember - area1Reduction) + "px";
    }
    document.getElementById("Area1").style.display = "";
    document.getElementById("Area1").style.height = (Constants_List1_size.showMember - area1Reduction) + "px";

    if (document.getElementById(tempNowPanel.type + "DataBody")) {
        document.getElementById(tempNowPanel.type + "DataBody").style.height = (Constants_List1_size.showMember - area1Reduction) + "px";
    }
    document.getElementById("Area2").style.display = "";
    var memberDataBodyObj = document.getElementById("memberDataBody") || document.getElementById("memberDataBodyOrginal");
    if (memberDataBodyObj) {
        memberDataBodyObj.style.height = "190px";
    }

    document.getElementById("Separator1").style.height = "30px";

    document.getElementById("Separator1").style.display = "";
    document.getElementById("Separator1_0").style.display = "";
    document.getElementById("Separator1_1").style.display = "";
    document.getElementById("Separator1_2").style.display = "";

    area12Status = "M";
}

function getMembersHTML(type, id, keyword, fullWin) {

    var _getMembersFun = null;

    var selectHTML = new StringBuffer();

    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        //div展示人员select
        selectHTML.append(memberDataBody_div);
    } else if (fullWin == true) {
        selectHTML.append(select2_tag_prefix_fullWin);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    if (Constants_Panels.get(type) && (_getMembersFun = Constants_Panels.get(type).getMembersFun) != null) {
        var memberDataBody = document.getElementById("memberDataBody");
        var entity = topWindow.getObject(type, id);
        if (!entity || (type == Constants_Department && entity.externalType == '1')) {
            return selectHTML;
        }

        var __members = eval("entity." + _getMembersFun + "()");
        if (!__members) {
            return selectHTML;
        }
        var isExternalLookDept = false;
        if (type == Constants_Department) {
            isExternalLookDept = checkExternalMemberWorkScope(type, id);
        }

        var _isNeedCheckLevelScope = true;
        var childDepts = [];
        var notNeedCheckLevelScope = true;
        if (isNeedCheckLevelScope && currentAccountLevelScope >= 0) {
            childDepts = childDeptOfCurrent(currentMember);
            notNeedCheckLevelScope = childDepts.contains(id);
        }
        if (!isNeedCheckLevelScope
            || (type == Constants_Department && currentMember && (currentMember.departmentId == id || currentMember.isSecondPostInDept(id) || notNeedCheckLevelScope))) {
            _isNeedCheckLevelScope = false;
        }


        var member;
        var len = __members.size();
        for (var i = 0; i < len; i++) {
            member = __members.get(i);

            if (keyword && member.name.toLowerCase().indexOf(keyword) < 0) {
                continue;
            }

            if (checkIncludeElements(type, id)) { //如果一区能选择，那二区就不限制
                //ignore
            } else if (!checkIncludeElements(Constants_Member, member.id)) {
                continue;
            }

            if (excludeElements.contains(Constants_Member + member.id)) {
                continue;
            }

            try {
                if (isInternal && member.externalType == '1') {
                    if (isNeedCheckLevelScope && !checkVjoinMemberWorkScopeOfMember(member)) {
                        continue;
                    }
                } else {
                    if (isInternal && _isNeedCheckLevelScope && !checkLevelScope(member, entity, childDepts)) { //越级
                        continue;
                    }

                    if (!isExternalLookDept && !checkExternalMemberWorkScopeOfMember(member)) {
                        continue;
                    }

                    if (isVjoinMember && isAdministrator != true && !checkExternalMemberWorkScopeOfMember(member)) {
                        continue;
                    }
                }

                //当前登录者是内部人员，显示的部门是外部部门，根据工作范围重新计算
                if (!showAllOuterDepartmentFlag && isInternal && !isAdmin && type == Constants_Outworker && !entity.isInternal) {
                    var extMember = topWindow.ExtMemberScopeOfInternal.get(id);
                    if (!extMember || !extMember.contains(member.id)) {
                        continue;
                    }
                }

                var shadowMembers = new Array();
                if (Constants_Department == type) { // 列出部门下的人员
                    while (i + 1 < __members.size()) { // 还有下一个
                        var nMember = __members.get(i + 1); // 下一个
                        if (nMember.id == member.id) { // 下一个与当前是同一人员
                            shadowMembers.push(nMember); // 合并显示
                            i++;
                        } else {
                            break;
                        }
                    }
                }
                selectHTML.append(addMember(type, entity, member, fullWin, shadowMembers));
            } catch (e) {
                log.error("", e);
                continue;
            }
        }
    }
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        //div展示人员select
        selectHTML.append(memberDataBody_div_end);
    } else {
        selectHTML.append(select2_tag_subfix);
    }
    return selectHTML.toString();
}

function getBusinessMembersHTML(type, id, keyword, fullWin) {
    var _getMembersFun = null;

    var selectHTML = new StringBuffer();

    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        //div展示人员select
        selectHTML.append(memberDataBody_div);
    } else if (fullWin == true) {
        selectHTML.append(select2_tag_prefix_fullWin);
    } else {
        selectHTML.append(select2_tag_prefix);
    }

    if (Constants_Panels.get(type) && (_getMembersFun = Constants_Panels.get(type).getMembersFun) != null) {
        var memberDataBody = document.getElementById("memberDataBody");

        var entity = topWindow.getObject(type, id);
        if (!entity || (type == Constants_Department && entity.externalType == '1')) {
            return selectHTML;
        }
        var __members = eval("entity." + _getMembersFun + "()");
        if (!__members) {
            return selectHTML;
        }

        var businessId = $("#areaTopList1_" + type).val();
        var businessAccount = topWindow.getObject(Constants_BusinessAccount, businessId);
        if (businessAccount) {
            var accessMemberIds = businessAccount.accessMemberIds;

            for (var i = 0; i < __members.size(); i++) {
                var member = __members.get(i);

                if (keyword && member.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                if (checkIncludeElements(type, id)) { //如果一区能选择，那二区就不限制
                    //ignore
                } else if (!checkIncludeElements(Constants_Member, member.id)) {
                    continue;
                }

                if (excludeElements.contains(Constants_Member + member.id)) {
                    continue;
                }

                try {
                    if (isNeedCheckLevelScope && accessMemberIds.indexOf(member.id) == -1) {
                        continue;
                    }
                    selectHTML.append(addMember(type, entity, member, fullWin, ""));
                } catch (e) {
                    log.error("", e);
                    continue;
                }
            }
        }
    }
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        //div展示人员select
        selectHTML.append(memberDataBody_div_end);
    } else {
        selectHTML.append(select2_tag_subfix);
    }
    return selectHTML.toString();
}

/**
 * 显示人员
 */
function showMember(type, id, keyword) {

    if ((tempNowPanel.type != Constants_OrgTeam && !checkCanSelectMember()) || (tempNowPanel.type == Constants_OrgTeam && !checkCanSelectOrgTeam())) {
        return;
    }

    //组
    if (type == Constants_Team) {
        addTeamMember2List2(id, keyword);
    } else if (type == Constants_OrgTeam) {//新增机构组显示
        addOrgTeamToList(id, "");
    } else if (type == Constants_BusinessDepartment) {
        var selectHTML = getBusinessMembersHTML(type, id, keyword);
        document.getElementById("Area2").innerHTML = selectHTML;
        initIpadScroll("memberDataBody");//ipad滚动条解决
    } else { //直接关系人
        var selectHTML = getMembersHTML(type, id, keyword);
        document.getElementById("Area2").innerHTML = selectHTML;
        initIpadScroll("memberDataBody");//ipad滚动条解决
    }
}

/**添加机构组部门显示到select option中**/
function addOrgTeamToList(id, keyword) {

    var orgTeam = topWindow.getObject(Constants_OrgTeam, id);
    if (!orgTeam) { //个人组不管
        return;
    }
    var concurentDepartments = topWindow.getDataCenter(Constants_concurentMembers, currentAccountId);
    var selectHTML = new StringBuffer();
    selectHTML.append(select2_tag_prefix);

    var sepTteamObj = document.getElementById("sep_team");
    var isShowRelatemember = sepTteamObj && sepTteamObj.checked == true;
    selectHTML.append(addOrgTeamDepartmentOfType(concurentDepartments, orgTeam.getOrgTeamDepartment(), "OrgTeam"));

    function addOrgTeamDepartmentOfType(concurentDepartments, _departments, type) {
        if (!_departments) {
            return "";
        }


        var loadAccountData = new Array();

        var str = new StringBuffer();
        for (var i = 0; i < _departments.size(); i++) {
            var _accountId;
            var department = _departments.get(i);
            var departmentType = department.type;
            var departmentIdStr = department.id;
            var departmentId = departmentIdStr;
            if (departmentId.indexOf("_") >= 0) {
                var index0 = departmentIdStr.indexOf("_");
                _accountId = departmentIdStr.substr(0, index0);
                departmentId = departmentIdStr.substr(index0 + 1);

                if (!loadAccountData.contains(_accountId) && currentAccountId != _accountId) {
                    loadAccountData.push(_accountId);
                    topWindow.initOrgModel(_accountId, currentMemberId, extParameters);
                }
            }
            var showText = department.name;
            var titleText = department.name;

            if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                str.append("<div class='member-list-div' seleted='false' ondblclick=\"selectOne('Department', this)\"  onclick=\"selectMemberFn(this,'memberDataBody')\"  value=\"").append(departmentId).append("\" type=\"").append(departmentType).append("\" accountId=\"").append(_accountId).append("\">").append(showText.escapeHTML(true)).append("</div>");
            } else {
                str.append("<option value=\"").append(departmentId).append("\"title=\"").append(titleText.escapeHTML(true)).append("\" type=\"").append(departmentType).append("\" accountId=\"").append(_accountId).append("\" class='TeamMember_" + type + "'>").append(showText.escapeHTML(true)).append("</option>");
            }

        }

        return str;
    }

    selectHTML.append(memberDataBody_div_end);
    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

/**
 * 把List2区域的数据清空
 */
function clearList2() {
    var memberDataBody = document.getElementById("memberDataBody");
    if (memberDataBody) {
        if (memberDataBody.options) {
            var len = memberDataBody.options.length;
            for (var i = 0; i < len; i++) {
                memberDataBody.remove(0);
            }
        } else {
            memberDataBody.innerHTML = '';
        }
    }
}

/**
 * 把人员添加到区域2
 * @param type List1的类型
 * @param entity list1的对象
 * @param member Member对象
 * @param fullWin
 * @param shadowMembers 需要合并显示的Member数组
 */
function addMember(type, entity, member, fullWin, shadowMembers) {
    if (ShowMe == false && currentMemberId && member.id == currentMemberId) {
        return;
    }
    var sFlag = shadowMembers && shadowMembers.length > 0;
    var mArray = new Array();
    mArray.push(member);
    if (sFlag) {
        for (var i = 0; i < shadowMembers.length; i++) {
            var sMember = shadowMembers[i];
            mArray.push(sMember);
        }
    }

    var attribute = "Department";

    if (type == Constants_Department || type == Constants_Outworker) {
        attribute = "Post";
    }

    var showText = null;
    var _accountId = member.accountId;
    var className = "";
    var secondPostInDepartId = null;
    var showTitle = "";
    var showDeptN = "";
    var emailOrMobileAttribute = getCanSelectEmailOrMobile();
    var emailOrMobile = null;
    if (emailOrMobileAttribute) {
        emailOrMobile = member[emailOrMobileAttribute];
    }

    //显示手机号或email，而该人没有设置
    if (emailOrMobileAttribute && !emailOrMobile) {
        return null;
    }

    var selectPeople_secondPostLabel = "(" + $.i18n("selectPeople.secondPost") + ")";

    if (member.type == "E") { //兼职
        if (showConcurrentMember == false) {
            return "";
        }

        var account = allAccounts.get(_accountId);
        if (!account) {
            log.warn("兼职[" + member.name + "]的主岗单位[" + _accountId + "]不存在");
            return "";
        }

        showText = member.name + "(" + account.shortname + ")";
        showText = showText.getLimitLength(nameMaxLength.two[0]);
        showText += $.browser.chrome ? (showText.getBytesLength() % 2 > 0 ? "|," : "") : "";
        if ($.browser.safari) {
            var temp = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
            var tempIndex = (temp - (nameMaxLength.two[0] - temp - 3)) > 20 ? 20 : temp - (nameMaxLength.two[0] - temp - 3);
            showText += getNameSpace(tempIndex);
        } else {
            var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
            showText += getNameSpace(tempIndex);
        }
        showText = showText.toString().replace("\|,", " ");
        if (emailOrMobile) {
            showText += emailOrMobile;
        } else {
            for (var i = 0; i < mArray.length; i++) {
                var cMember = mArray[i];
                var object_ = eval("cMember.get" + attribute + "()");
                if (object_) {
                    if (i > 0) {
                        showText += " ";
                        showTitle += " ";
                    }
                    showText += selectPeople_secondPostLabel + ((fullWin == true) ? object_.getFullName() : object_.name);
                    if (fullWin && object_.getFullName) {
                        showTitle += selectPeople_secondPostLabel + object_.getFullName();
                    } else {
                        showTitle += selectPeople_secondPostLabel + object_.name;
                    }
                } else if (type == Constants_RelatePeople) {
                    showText += cMember.departmentName;
                    showTitle += cMember.departmentName;
                }
                var object_1 = cMember.getDepartment();
                if (object_1) {
                    if (object_1.getFullName) {
                        var fullName = object_1.getFullName();
                        showDeptN += selectPeople_secondPostLabel + fullName;
                    } else {
                        showDeptN += selectPeople_secondPostLabel + object_1.name;
                    }
                }
            }
            /*			var object_ = null;
			if(type == Constants_Post){ //岗位页签，显示兼职部门
				object_ = member.getDepartment();

				if(object_ && fullWin == true){
					showTitle += member.name + "\n" + Constants_Component.get(Constants_Department) + ": " + object_.getFullName();
				}
			}
			else{
				object_ = member.getPost();
			}

			if(object_){
				showText += selectPeople_secondPostLabel + ((fullWin == true) ? object_.getFullName() : object_.name);
			}*/
        }

//		className = "secondPost-true";
    } else if (member.type == "G") {//在全集团范围内查出来的
        showText = member.name;
        showText = showText.getLimitLength(nameMaxLength.two[0]);
        showText += $.browser.chrome ? (showText.getBytesLength() % 2 > 0 ? "|," : "") : "";

        if ($.browser.safari) {
            var temp = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
            var tempIndex = (temp - (nameMaxLength.two[0] - temp - 3)) > 20 ? 20 : temp - (nameMaxLength.two[0] - temp - 3);
            showText += getNameSpace(tempIndex);
        } else {
            var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
            if (tempIndex < 0) {
                tempIndex = 0;
            }
            showText += getNameSpace(tempIndex);
        }

        if (emailOrMobile) {
            showText += emailOrMobile;
        } else {
            var account = allAccounts.get(_accountId);
            if (account) {
                var fullName = "/" + member.departmentName;
                showText += account.shortname + fullName;
                if (attribute == "Post") {
                    var post = topWindow.getObject(Constants_Post, member.postId);
                    if (post == null && member.accountId != currentAccountId) {
                        topWindow.initOrgModel(member.accountId, currentMemberId, extParameters)
                        post = topWindow.getObject(Constants_Post, member.postId);
                    }
                    if (post) {
                        showTitle += post.name;
                    }
                } else {
                    showTitle += account.shortname + fullName;
                }

            }
        }
        var object_1 = member.departmentNameF;
        if (object_1) {
            showDeptN += object_1;
        }
    } else {
        if (showSecondMember == false && member.type == "F") {
            return "";
        }

        showText = member.name.getLimitLength(nameMaxLength.two[0]);
        showText += $.browser.chrome ? (showText.getBytesLength() % 2 > 0 ? "|," : "") : "";

        if ($.browser.safari) {
            var temp = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
            var tempIndex = (temp - (nameMaxLength.two[0] - temp - 3)) > 20 ? 20 : temp - (nameMaxLength.two[0] - temp - 3);
            showText += getNameSpace(tempIndex);
        } else {
            var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
            showText += getNameSpace(tempIndex);
        }

        if (emailOrMobile) {
            showText += emailOrMobile;
        } else {
            for (var i = 0; i < mArray.length; i++) {
                var cMember = mArray[i];
                var jianzhiFlag = (cMember.type == "F" ? selectPeople_secondPostLabel : "");
                var object_ = eval("cMember.get" + attribute + "()");
                if (object_) {
                    if (i > 0) {
                        showText += " ";
                        showTitle += " ";
                    }
                    showText += jianzhiFlag + ((fullWin == true) ? object_.getFullName() : object_.name);
                    if (object_.getFullName) {
                        showTitle += jianzhiFlag + object_.getFullName();
                    } else {
                        showTitle += jianzhiFlag + object_.name;
                    }
                }
                var object_1 = cMember.getDepartment();
                if (object_1) {
                    if (object_1.getFullName) {
                        var fullName = object_1.getFullName();
                        showDeptN += jianzhiFlag + fullName;
                    } else {
                        showDeptN += jianzhiFlag + object_1.name;
                    }
                }
            }
        }
    }

    showText = showText.toString().replace("\|,", " ");
    if (showTitle.length > 0) {
        showTitle = member.name.escapeHTML(true) + "&#13;" + eval("Constants_Component.get(Constants_" + attribute + ")") + ": " + showTitle.escapeHTML(true);
        if (attribute != Constants_Department) {
            showTitle = showTitle + "&#13;" + Constants_Component.get(Constants_Department) + ": " + showDeptN.escapeHTML(true);
        }
    } else {
        showTitle = member.name.escapeHTML(true);
    }

    var mId = member.id;
    if (returnMemberWithDept && type == Constants_Department) {
        mId = entity.id + valuesJoinSep2 + member.id;
    }

    var sb = new StringBuffer();
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        sb.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\" title='" + showTitle + "' value='").append(mId).append("' type='Member' accountId='").append(_accountId).append("' externalType='").append(member.externalType).append("'>").append((showText.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;")).append("</div>");
    } else {
        sb.append("<option style='margin:4px 0 0 4px' title='" + showTitle + "' value='").append(mId).append("' type='Member' accountId='").append(_accountId).append("' externalType='").append(member.externalType).append("'>").append((showText.escapeHTML(true)).replace(new RegExp("&nbsp;", 'g'), "&ensp;")).append("</option>");
    }
    return sb.toString().replace("\|,", " ");
}

/**
 * 添加组的成员到List2
 */
function addTeamMember2List2(id, keyword) {

    var team = topWindow.getObject(Constants_Team, id);
    if (!team) { //个人组不管
        return;
    }
    var hiddenOtherMemberOfTeam = getParentWindowData("hiddenOtherMemberOfTeam");
    var concurentMembers = topWindow.getDataCenter(Constants_concurentMembers, currentAccountId);


    var selectHTML = new StringBuffer();
    selectHTML.append(select2_tag_prefix);

    var sepTteamObj = document.getElementById("sep_team");
    var isShowRelatemember = sepTteamObj && sepTteamObj.checked == true;

    if (isShowRelatemember) {
        selectHTML.append(addTeamMemberOfType(concurentMembers, team.getSupervisors(), "Supervisor"));
    }

    selectHTML.append(addTeamMemberOfType(concurentMembers, team.getLeaders(), "Leader"));
    selectHTML.append(addTeamMemberOfType(concurentMembers, team.getMembers(), "Member"));

    if (isShowRelatemember) {
        selectHTML.append(addTeamMemberOfType(concurentMembers, team.getRelatives(), "Relative"));
    }

    //Type : Leader/主管 Member/组员 Supervisors/领导 Relative/关联人员
    //如果type 部门，岗位，组，人员    _members 中包含list 只有一个对应的实体
    //如果type是部门下的岗位，_members 中包含list，一个是部门对象，一个是岗位对象
    function addTeamMemberOfType(concurentMembers, _members, type) {
        if (!_members) {
            return "";
        }

        var loadAccountData = new Array();

        var str = new StringBuffer();
        for (var i = 0; i < _members.size(); i++) {
            var _accountId;
            var member = _members.get(i);
            var memberType = member.type;
            var memberIdStr = member.id;
            var memberId = memberIdStr;
            if (memberId.indexOf("_") >= 0) {
                var index0 = memberIdStr.indexOf("_");
                _accountId = memberIdStr.substr(0, index0);
                memberId = memberIdStr.substr(index0 + 1);

                if (!loadAccountData.contains(_accountId) && currentAccountId != _accountId) {
                    loadAccountData.push(_accountId);
                    topWindow.initOrgModel(_accountId, currentMemberId, extParameters);
                }
            }

            if (excludeElements.contains(memberType + memberId)) {
                continue;
            }

            var showText = member.name.getLimitLength(nameMaxLength.three[0]);
            var titleText = member.name;

            titleText = titleText + "  ";
            showText = showText.getLimitLength(nameMaxLength.three[0]);
            showText += getNameSpace(nameMaxLength.three[0] + nameMaxSpace - showText.getBytesLength());

            if (memberType == Constants_Member) {
                if (excludeElements.contains(Constants_Member + memberId)) {
                    continue;
                }
                var teamMember = topWindow.getObject(Constants_Member, memberId, _accountId);
                if (teamMember && teamMember.departmentId) {
                    var memberDept = topWindow.getObject(Constants_Department, teamMember.departmentId, _accountId);
                    if (memberDept) {
                        showText += memberDept.name;
                        titleText += memberDept.name;
                    }
                }
            }

            var tempString = getNameSpace(nameMaxLength.three[0] + nameMaxSpace + nameMaxLength.three[1] + nameMaxSpace - showText.getBytesLength());
            showText += tempString;
            titleText += "  ";
            showText += $.i18n("selectPeople.Team" + type + "_label");
            titleText += $.i18n("selectPeople.Team" + type + "_label");

            if (!v3x.getBrowserFlag('selectPeopleShowType')) {
                str.append("<div class='member-list-div' seleted='false' ondblclick=\"selectOne('" + memberType + "', this)\"  onclick=\"selectMemberFn(this,'memberDataBody')\"  value=\"").append(memberId).append("\" type=\"").append(memberType).append("\" accountId=\"").append(_accountId).append("\">").append(showText.escapeHTML(true)).append("</div>");
            } else {
                str.append("<option value=\"").append(memberId).append("\" ondblclick=\"selectOne('" + memberType + "', this)\" title=\"").append(titleText.escapeHTML(true)).append("\" type=\"").append(memberType).append("\" accountId=\"").append(_accountId).append("\" class='TeamMember_" + type + "'>").append(showText.escapeHTML(true)).append("</option>");
            }

        }

        return str;
    }

    selectHTML.append(memberDataBody_div_end);

    document.getElementById("Area2").innerHTML = selectHTML.toString();
    initIpadScroll("memberDataBody");//ipad滚动条解决
}

/**
 * 监听Member的事件
 */
function listenermemberDataBody(object) {
    tempNowSelected.clear();
    var ops = object.options;
    for (var i = 0; i < ops.length; i++) {
        var option = ops[i];
        if (option.selected) {
            var e = getElementFromOption(option);
            if (e) {
                tempNowSelected.add(e);
            }
        }
    }
}

/**
 *
 * @return 0-允许访问; 1-允许看,无动作; -1; //不显示
 */
function isShowDepartmentTree(depart) {
    var showDepartmentsOfTree = getParentWindowData("showDepartmentsOfTree");
    if (showDepartmentsOfTree != null && showDepartmentsOfTree != "") {
        var showDepartmentsOfTreeStr = showDepartmentsOfTree.split(",");

        for (var i = 0; i < showDepartmentsOfTreeStr.length; i++) {
            var d = topWindow.getObject(Constants_Department, showDepartmentsOfTreeStr[i]);
            if (!d) {
                d = topWindow.getObject(Constants_BusinessDepartment, showDepartmentsOfTreeStr[i]);
            }
            if (d && d.accountId && currentAccountId) {
                if (d.path == depart.path || depart.path.startsWith(d.path)) {//当前部门或子部门
                    return 0; //允许访问
                }

                if (d.path.startsWith(depart.path)) {//当前部门是我的上级部门
                    return 1; //允许看
                }
            }
        }

        return -1; //禁止
    }

    var showDepartmentsNoChildrenOfTree = getParentWindowData("showDepartmentsNoChildrenOfTree");
    if (showDepartmentsNoChildrenOfTree != null && showDepartmentsNoChildrenOfTree != "") {
        var showDepartmentsOfTreeStr = showDepartmentsNoChildrenOfTree.split(",");

        for (var i = 0; i < showDepartmentsOfTreeStr.length; i++) {
            var d = topWindow.getObject(Constants_Department, showDepartmentsOfTreeStr[i]);
            if (d && d.accountId && currentAccountId) {
                if (d.path == depart.path) {//当前部门或子部门
                    return 0; //允许访问
                }

                if (d.path.startsWith(depart.path)) {//当前部门是我的上级部门
                    return 1; //允许看
                }
            }
        }

        return -1; //禁止
    }

    return 0; //允许访问

}

function initBusinessorganization(currentAccountId, selectBusinessAccountId) {
    if (tempNowPanel.type == Constants_BusinessDepartment) {
        initTree(Constants_BusinessDepartment, currentAccountId, selectBusinessAccountId);
    } else if (tempNowPanel.type == Constants_Node || tempNowPanel.type == Constants_FormField || tempNowPanel.type == Constants_BusinessRole) {
        initList(tempNowPanel.type, '', selectBusinessAccountId);
    }
    clearList2();
    reArea_1_2();
    if (!checkIsShowArea2()) {
        hiddenArea2(true);
        document.getElementById("Separator1").style.display = "none";
    }
}

//zhou:开发区双击部门选择人员信息
function dbClickDeptSelectedMember() {
    var entity = null;
    if (Constants_Department == ztype) {
        var _getMembersFun = null;
        entity = topWindow.getObject(ztype, zid);
        if (!entity || (ztype == Constants_Department && entity.externalType == '1')) {
            return selectHTML;
        }
        _getMembersFun = Constants_Panels.get(ztype).getMembersFun;
        var __members = eval("entity." + _getMembersFun + "()");
        selectedListMemberData(__members);
    } else if (Constants_Team == ztype) {
        var team = topWindow.getObject(Constants_Team, zid);
        selectedListMemberData(team.getLeaders());
        selectedListMemberData(team.getMembers());
    }
}

//zhou:公共的方法
function selectedListMemberData(__members) {
    var element = null;
    for (var i = 0; i < __members.size(); i++) {
        element = __members.get(i);
        if (element) {
            tempNowSelected.clear();
            tempNowSelected.add(element);
            selectOne();
        }
    }
}


/**
 * 显示树形结构
 */
function initTree(type, selectAccountId, selectBusinessAccountId) {
    tree = new WebFXTree();

    var root = null;
    var allRoots = [];
    var allAccountRoots = [];
    var currentNodeId = null;
    var lockTree = false;

    if (type == Constants_Account) {
        root = allAccounts.get(accessableRootAccountId[0]);
        currentNodeId = accessableRootAccountId[0];

        for (var i = 0; i < accessableRootAccountId.length; i++) {
            allRoots.push(allAccounts.get(accessableRootAccountId[i]));
        }
    } else if (type == Constants_BusinessDepartment) {
        clearList2();
        var businessAccounts = new ArrayList();
        var allBusinessAccounts = topWindow.getDataCenter(Constants_BusinessAccount, currentAccountId);
        if (!isAdmin) {
            var alwaysShowBusiness = getParentWindowData("alwaysShowBusiness") || "";
            for (var i = 0; i < allBusinessAccounts.size(); i++) {
                var ba = allBusinessAccounts.get(i);
                var isPublic = ba.isPublic;
                if (isPublic == true || isPublic == 'true' || alwaysShowBusiness.indexOf(ba.id) >= 0) {//公开的业务线都能看见
                    businessAccounts.add(ba);
                } else {
                    var memberIds = ba.memberIds;//私有的业务线只有业务线内的人可见
                    if (memberIds.indexOf(currentMemberId.toString()) >= 0) {
                        businessAccounts.add(ba);
                    }
                }
            }
        } else {
            businessAccounts = allBusinessAccounts;
        }

        var currentBusinessAccount = getParentWindowData("currentBusinessAccount");
        if (!selectBusinessAccountId && businessAccounts.size() > 0) {
            if (currentBusinessAccount) {
                selectBusinessAccountId = currentBusinessAccount;
            } else {
                selectBusinessAccountId = businessAccounts.get(0).id;
            }
        }

        $("#areaTopList1_" + type + " option").remove();
        for (var i = 0; i < businessAccounts.size(); i++) {
            var ba = businessAccounts.get(i);
            var bId = ba.id;
            var bName = ba.name;

            if (!selectBusinessAccountId) {
                selectBusinessAccountId = bId;
            }
            if (selectBusinessAccountId == bId) {
                $("#areaTopList1_" + type).append("<option value='" + bId + "' selected='selected'>" + bName + "</option>");
                root = ba;
                allRoots.push(root);
            } else {
                $("#areaTopList1_" + type).append("<option value='" + bId + "'>" + bName + "</option>");
            }
        }
        if (businessAccounts.size() == 0) {
            document.getElementById("AreaTop1_BusinessDepartment").style.display = "none";
        } else {
            document.getElementById("AreaTop1_BusinessDepartment").style.display = "";
        }

        reArea_1_2();

        var unallowedChangeBusinessAccount = getParentWindowData("unallowedChangeBusinessAccount") || false;
        if (unallowedChangeBusinessAccount) {
            $("#AreaTop1_" + type).disable();
        }

    } else {
        root = allAccounts.get(selectAccountId);
        currentNodeId = departmentId;
        lockTree = onlyCurrentDepartment;

        if (root != null) {
            currentAccountLevelScope = parseInt(root.levelScope, 10);
            allRoots.push(root);
        }
    }

    if (isV5Member && !isInternal) {
        currentNodeId = null;
        if (root && root.externalType && root.externalType != "0") {//编外人员在任何情况下都看不到外部人员
            root = null;
            allRoots = [];
        }
    }

    var isShowCheckbox = (type == Constants_Account) && checkCanSelect(type) && maxSize != 1;

    if (lockTree) disableButton("button1");

    var treeHtml = new StringBuffer();

    for (var i = 0; i < allRoots.length; i++) {
        var r = allRoots[i];
        var t;
        if (type == Constants_Department) {
            t = new WebFXTree(r.id, Constants_Account, r.name, type, true, "showList2('" + Constants_Account + "', '" + r.id + "')", lockTree, "");
        } else if (type == Constants_JoinAccountTag) {
            t = new WebFXTree(r.id, type, r.name, type, true, "clearList2()", lockTree, "");
        } else if (type == Constants_MemberMetadataTag) {
            t = new WebFXTree(r.id, type, r.name, type, true, "clearList2()", lockTree, "");
        } else if (type == Constants_BusinessDepartment) {
            t = new WebFXTree(r.id, Constants_BusinessAccount, r.name, type, true, "showList2('" + Constants_BusinessAccount + "', '" + r.id + "')", lockTree, "");
        } else {
            t = new WebFXTree(r.id, Constants_Account, r.name, type, true, "clearList2()", lockTree, "");
        }
        t.setBehavior('classic');
        t.hasShowChild = true;
        t.hasGoChild = true;
        t.isShowCheckbox = isShowCheckbox;
        t.externalType = r.externalType;

        treeHtml.append(t);

        allAccountRoots.push(t);

        if (i == 0) {
            tree = t;
        }
    }

    if (document.getElementById("AreaTop1_" + type)) {
        document.getElementById("Area1").innerHTML = "<div id='List1' style='width:363px; height:138px; overflow:auto;padding:0 0 0 5px;'>" + treeHtml + "</div>";
    } else {
        document.getElementById("Area1").innerHTML = "<div id='List1' style='width:363px; height:168px; overflow:auto;padding:0 0 0 5px;'>" + treeHtml + "</div>";
    }
    document.getElementById("Area1").className = "iframe";

    if (root == null) {
        return;
    }

    var allParents = null;

    if (!treeInMyDepart && type == Constants_Department && currentNodeId != null) {
        allParents = topWindow.findMultiParent(topWindow.getDataCenter(type, selectAccountId), currentNodeId);
        showChildTree(type, root.id, tree, onlyCurrentDepartment);
        var childs = tree.childNodes;
        var action = null;
        for (var i = 0; i < childs.length; i++) {
            var c = childs[i];
            if (c != null) {
                if (allParents.size() == 0) {
                    if (c.id == currentNodeId) {
                        var entity = topWindow.getObject(type, c.id);
                        action = (isShowDepartmentTree(entity) == 0) ? "showList2('" + type + "', '" + c.id + "')" : "";
                        c.select();
                        break;
                    }
                } else {
                    if (c.id == currentNodeId) {
                        var entity = topWindow.getObject(type, c.id);
                        action = (isShowDepartmentTree(entity) == 0) ? "showList2('" + type + "', '" + c.id + "')" : "";
                        c.select();
                        break;
                    }
                    for (var j = 0; j < allParents.size(); j++) {
                        var n = allParents.get(j);
                        if (((n.isInternal == false || n.isInternal == "false") && n.externalType == "0") || n.id == root.id) {
                            continue;
                        }
                        //该部门不显示
                        if (excludeElements.contains(type + n.id) || !checkIncludeElements(type, n.id)) {
                            continue;
                        }
                        var _status = isShowDepartmentTree(n);
                        if (_status == -1) {
                            continue;
                        }
                        if (n.id == c.id) {
                            c.expand();
                            childs = c.childNodes;
                            i = -1;
                            break;
                        }
                    }
                }
            }
            if (childs == null) {
                break;
            }
        }
        if (action != null) {
            eval(action);
        }
    } else if (type == Constants_Account) {
        allParents = topWindow.findMultiParent(accessableAccounts.values(), currentNodeId);

        if (allParents != null && isGroupVer) {
            var expandNode = tree;
            var isShowCheckbox = (type == Constants_Account) && checkCanSelect(type) && maxSize != 1;
            for (var i = 0; i < allParents.size(); i++) {
                var n = allParents.get(i);
                if (n.isInternal == false || n.isInternal == "false" || n.id == root.id) {
                    continue;
                }

                //该部门不显示
                if (excludeElements.contains(type + n.id) || !checkIncludeElements(type, n.id)) {
                    continue;
                }

                var _status = isShowDepartmentTree(n);
                if (_status == -1) {
                    continue;
                }

                var action = (_status == 0) ? "showList2('" + type + "', '" + n.id + "')" : "";

                var item = new WebFXTreeItem(n.id, type, n.name, n.hasChild, action, lockTree, n.description);
                item.isShowCheckbox = isShowCheckbox;
                item.externalType = n.externalType;
                expandNode.add(item);

                expandNode = item;
            }

            var myNode = showChildTree(type, expandNode.id, expandNode, lockTree);
            webFXTreeHandler.expanded = expandNode;

            for (var i = 0; i < allAccountRoots.length; i++) {
                if (allAccountRoots[i].id == expandNode.id) continue;
                showChildTree(type, allAccountRoots[i].id, allAccountRoots[i]);
            }

            //tree.expandAll();

            if (myNode != null) {
                myNode.toggle();
                myNode.select();

                eval(myNode.action);
            }

            //		treeInMyDepart = true;

            return;
        }

        showChildTree(type, root.id, tree);
    } else {
        showChildTree(type, root.id, tree);
    }
}

/**
 * ????
 */
function showChildTree(type, id, parentNode, _onlyCurrentDepartment) {
    if (!type || type == undefined) {
        return;
    }
    var datas2Show = null;
    if (type == Constants_Account) {
        var account0 = accessableAccounts.get(id);
        datas2Show = account0.accessChildren;
    } else if (type == Constants_BusinessDepartment) {
        datas2Show = topWindow.findChildInList(topWindow.getDataCenter(type, currentAccountId), id);
    } else {
        var _getChildrenFun = Constants_Panels.get(type).getChildrenFun;

        var entity = topWindow.getObject(type, id);
        if (entity && _getChildrenFun) {
            datas2Show = eval("entity." + _getChildrenFun + "()");
        } else {
            datas2Show = topWindow.findChildInList(topWindow.getDataCenter(type, id), id);
        }
    }

    if (!datas2Show) {
        return;
    }

    if (!isAdmin && (isNeedCheckLevelScope || isVjoinMember)) {
        if (type == Constants_Department) {
            var temp = new ArrayList();
            if (tempNowPanel.type == Constants_JoinOrganization) {
                var _AccessVjoinDepts = topWindow.AccessVjoinDepts;
                if (_AccessVjoinDepts != null) {
                    for (var i = 0; i < datas2Show.size(); i++) {
                        if (_AccessVjoinDepts.contains("D" + datas2Show.get(i).id)) {
                            temp.add(datas2Show.get(i));
                        }
                    }
                }
                datas2Show = temp;
            } else {
                if (!isV5Member && tempNowPanel.type == Constants_Department) {
                    var _AccessInnerDepts = topWindow.AccessInnerDepts;

                    if (_AccessInnerDepts != null) {
                        for (var i = 0; i < datas2Show.size(); i++) {
                            if (_AccessInnerDepts.contains("D" + datas2Show.get(i).id)) {
                                if (datas2Show.get(i).hasChild) {
                                    var datas2Child = topWindow.findChildInList(topWindow.getDataCenter(type, currentAccountId), datas2Show.get(i).id);
                                    var hasChild = false;
                                    for (var j = 0; j < datas2Child.size(); j++) {
                                        if (_AccessInnerDepts.contains("D" + datas2Child.get(j).id)) {
                                            hasChild = true;
                                            break;
                                        }
                                    }
                                    datas2Show.get(i).hasChild = hasChild;
                                }
                                temp.add(datas2Show.get(i));
                            }
                        }
                    }

                    datas2Show = temp;
                }
            }

        }
    }


    var myNode = null;
    var isShowCheckbox = (type == Constants_Account) && checkCanSelect(type) && maxSize != 1;

    for (var i = 0; i < datas2Show.size(); i++) {
        var n = datas2Show.get(i);

        if (n.externalType != "0" && type == Constants_Account) {
            continue;
        }

        if (n.externalType != "0" && showExternalType && showExternalType != n.externalType) {
            continue;
        }

        if (_onlyCurrentDepartment == true && departmentId != n.id) {
            continue;
        }

        if ((isV5Member || (isVjoinMember && isAdmin)) && n.externalType == "0" && (n.isInternal == false || n.isInternal == "false")) {
            continue;
        }

        //该部门不显示
        if (excludeElements.contains(type + n.id)) {
            continue;
        }

        var _status = isShowDepartmentTree(n);
        if (_status == -1) {
            continue;
        }

        var action = (_status == 0) ? "showList2('" + type + "', '" + n.id + "')" : "";

        var item = new WebFXTreeItem(n.id, type, n.name, n.hasChild, action, false, n.description);
        item.isShowCheckbox = isShowCheckbox;
        item.externalType = n.externalType;

        if (departmentId == n.id) {
            myNode = item;
        }

        parentNode.add(item);
    }

    parentNode.hasShowChild = true;
    parentNode.hasGoChild = false;

    webFXTreeHandler.expanded = parentNode;

    return myNode;
}

/**
 * ????
 */
function showParentTree() {
    if (area1Status) {//????????????????????????????
        hiddenArea1();
    }

    if (tree == null) {
        return;
    }

    var nowExpandNode = tree.getSelected();

    var _parentNode = nowExpandNode.parentNode;

    if (nowExpandNode == null || _parentNode == null) {
        return;
    }

    webFXTreeHandler.toggle(document.getElementById(_parentNode.id));
    _parentNode.select();
    showList2(_parentNode.type, _parentNode.id);
}

/*
 * ???????
 */
function selectList1Item(type, objTD) {
    tempNowSelected.clear();

    var ops = objTD.options;
    var count = 0;
    for (var i = 0; i < ops.length; i++) {
        var option = ops[i];
        if (option.selected) {
            if (option.id && option.id == "more") {
                option.id = "loading";
                option.innerHTML = $.i18n("selectPeople.data.loading");

                var startIndex = option.getAttribute("startIndex");
                window.setTimeout(function () {
                    var pagingHtml = "";
                    var keyword = pagingParam.get("keyword");
                    var datas = pagingParam.get("datas");
                    if (type == Constants_Post) {
                        var selectAccountId = pagingParam.get("selectAccountId");
                        pagingHtml = getPostListHTMLStrPaging(startIndex, keyword, selectAccountId, datas);
                    }

                    $("option[id='loading']").remove();
                    tempNowSelected.clear();
                    //追加分页数据
                    $("#" + objTD.id).append(pagingHtml);
                }, 100);
            } else {
                var e = getElementFromOption(option);
                if (e) {
                    tempNowSelected.add(e);
                    count++;
                }
            }
        }
    }

    //机构组点击监听判断
    if (count == 1 && type == Constants_OrgTeam) {
        var id = objTD.value;
        showList2(type, id);
    }
    if (count == 1 && tempNowPanel.isShowMember == true) {
        var id = objTD.value;
        showList2(type, id);
    }

    if (nowSelectedList1Item != null) {
        nowSelectedList1Item = null;
    }

    nowSelectedList1Item = objTD;
}

/**
 * ??????
 */
function selectOneMember(selectObj) {

    if (!selectObj || selectObj.selectedIndex < 0) {
        return;
    }

    var option = selectObj.options[selectObj.selectedIndex];
    if (!option) {
        return;
    }

    var element = getElementFromOption(option);
    if (element) {
        tempNowSelected.clear();
        tempNowSelected.add(element);

        selectOne();
    }
}

/**
 * 选择了区域2的项目，转换成Element对象
 */
function getElementFromOption(option) {
    if (!option) {
        return null;
    }

    var typeStr = option.getAttribute("type");
    var idStr = option.getAttribute("value");
    var _accountId = option.getAttribute("accountId");
    var _externalType = option.getAttribute("externalType");

    //Element(type, id, name, typeName, accountId, accountShortname, description)
    //如果是选择部门或者部门下的岗位,title 显示部门的全路径（全路径暂时存放在description属性中）
    var _element = new Element(typeStr, idStr, getName(typeStr, idStr, _accountId), "", _accountId, "", getFullNameStr(typeStr, idStr));
    if (_externalType) {
        _element.externalType = _externalType;
    }
    return _element;
}

function getName(typeStr, idStr, accountId0) {
    if (idStr == "" || typeStr == "") {
        return "";
    }
    if (typeStr == Constants_Node || typeStr == Constants_FormField || typeStr == Constants_OfficeField || typeStr == Constants_WfSuperNode) {
        var m = typeStr == Constants_FormField ? 1 : 0;
        var nodes = topWindow.getDataCenter(typeStr, currentAccountId);
        for (var i = 0; i < nodes.size(); i++) {
            if (idStr == nodes.get(i).id) {
                return nodes.get(i).name;
            }
            if (idStr.startsWith(nodes.get(i).id)) {
                var r = nodes.get(i).getRoles();
                for (var j = 0; j < r.size(); j++) {
                    if (r.get(j).K == idStr.substring(nodes.get(i).id.length + m)) {
                        return nodes.get(i).name + r.get(j).N;
                    }
                }
            }
            if (idStr.startsWith(nodes.get(i).id)) {//业务线下的角色
                var r = topWindow.getDataCenter(Constants_BusinessRole, currentAccountId);
                for (var z = 0; z < r.size(); z++) {
                    if (r.get(z).id == idStr.substring(nodes.get(i).id.length + m)) {
                        return r.get(z).preShow + "-" + nodes.get(i).name + r.get(z).name;
                    }
                }
            }
        }
        return;
    }
    if (typeStr == Constants_BusinessDepartment) {
        var businessDepartment = topWindow.getObject(typeStr, idStr, accountId0);
        if (businessDepartment) {
            return businessDepartment.preShow + "-" + businessDepartment.name;
        }
        return;
    }

    if (topWindow.Constants_Custom_Panels.keys() != null && topWindow.Constants_Custom_Panels.keys().contains(typeStr)) {
        var custom = topWindow.getDataCenter(typeStr, currentAccountId);
        var customPanel = topWindow.Constants_Custom_Panels.get(typeStr);
        var sp = customPanel.sp;
        for (var i = 0; i < custom.size(); i++) {
            if (idStr == custom.get(i).id) {
                return custom.get(i).name;
            }
            if (idStr.startsWith(custom.get(i).id)) {
                var r = custom.get(i).getRelationData();
                for (var j = 0; j < r.size(); j++) {
                    if (r.get(j).K == idStr.substring(custom.get(i).id.length + sp.length)) {
                        return custom.get(i).name + r.get(j).N;
                    }
                }
            }
        }
        return;
    }


    var businessPreShow = "";
    var types = typeStr.split(valuesJoinSep);
    var ids = idStr.split(valuesJoinSep);

    var elementName = [];
    var entity;
    for (var i = 0; i < types.length; i++) {
        entity = topWindow.getObject(types[i], ids[i], currentAccountId);

        if (entity == null) {
            //加载单位数据
            accountId = accountId || currentAccountId;
            topWindow.initOrgModel(accountId, currentMemberId, extParameters);
            entity = topWindow.getObject(types[i], ids[i], accountId);
            if (entity == null && accountId0) {
                topWindow.initOrgModel(accountId0, currentMemberId, extParameters);
                entity = topWindow.getObject(types[i], ids[i], accountId0);
            }

            if (entity == null) {
                entity = topWindow.getObject(types[i], ids[i]);
            }
        }
        elementName[elementName.length] = entity ? entity.name : ((searchNames.get(idStr)) ? searchNames.get(idStr).name : '');
        if (types[i] == Constants_BusinessDepartment || types[i] == Constants_BusinessRole) {
            businessPreShow = entity.preShow;
        }
    }

    if (businessPreShow != "") {
        return businessPreShow + "-" + elementName.join(arrayJoinSep);
    }
    return elementName.join(arrayJoinSep);
}

function getFullNameStr(typeStr, idStr) {
    if (typeStr == "Department_Post") {

        var types = typeStr.split(valuesJoinSep);
        var ids = idStr.split(valuesJoinSep);

        var elementName = [];
        var entity;
        for (var i = 0; i < types.length; i++) {
            entity = topWindow.getObject(types[i], ids[i]);
            if (entity == null) {
                accountId = accountId || currentAccountId;
                topWindow.initOrgModel(accountId, currentMemberId, extParameters);
                entity = topWindow.getObject(types[i], ids[i]);
            }
            elementName[elementName.length] = entity ? ((types[i] == Constants_Department) ? entity.getFullName() : entity.name) : searchNames.get(idStr).name;
        }

        return elementName.join(arrayJoinSep);
    }// sunqs 2018-10-18 右侧显示人员所属部门全路径
    else if (typeStr == Constants_Member) {
        var elementName = [];

        var entity;
        var deptEntity;

        if (returnMemberWithDept && tempNowPanel.type == Constants_Department) {
            var ids = idStr.split(valuesJoinSep2);
            entity = topWindow.getObject(Constants_Member, ids[1]);
            deptEntity = topWindow.getObject(Constants_Department, ids[0]);
        } else {
            entity = topWindow.getObject(typeStr, idStr);
            deptEntity = topWindow.getObject(Constants_Department, entity.departmentId);
        }
        var showTitle = entity.name;
        var showDeptN = "";
        if (deptEntity) {
            showDeptN = deptEntity.getFullName();
        }
        var depTypeName = Constants_Component.get(Constants_Department);
        if (!entity.isInternal) {
            if (entity.externalType != "0") {
                depTypeName = Constants_Component.get(Constants_JoinOrganization);
            } else {
                depTypeName = $.i18n("selectPeople.externalOrg.js");
            }
        }
        showTitle = showTitle + "\r\n" + depTypeName + ": " + showDeptN.escapeHTML(true);
        elementName[elementName.length] = entity ? showTitle : searchNames.get(idStr).name;
        return elementName.join(arrayJoinSep);
    }
    return "";

}

var NeedCheckEmptyMemberType = new ArrayList();
NeedCheckEmptyMemberType.add(Constants_Account);
NeedCheckEmptyMemberType.add(Constants_Department);
NeedCheckEmptyMemberType.add(Constants_Team);
NeedCheckEmptyMemberType.add(Constants_Post);
NeedCheckEmptyMemberType.add(Constants_Level);
NeedCheckEmptyMemberType.add(Constants_Department + "_" + Constants_Post);
NeedCheckEmptyMemberType.add(Constants_BusinessDepartment);
NeedCheckEmptyMemberType.add(Constants_BusinessDepartment + "_" + Constants_BusinessRole);


/**
 * 当前显示单位是否是根单位
 */
function isRootAccount(selectAccountId) {
    if (selectAccountId) {
        return (selectAccountId == rootAccount.id);
    } else {
        return (currentAccountId == rootAccount.id);
    }
}

/**
 * 检测集合里面是否是空的，一般检测部门和组
 * @return true - 是空的， false - 不是空的或者不需要检测
 */
function checkEmptyMember(type, id) {
    if (isRootAccount(id)) {
        return false;
    }

    if (type == Constants_Level && currentAccountId == '-1730833917365171641') {//集团职务级别，不做空校验
        return false;
    }

    if (!type
        || !id
        || !NeedCheckEmptyMemberType.contains(type)
        || !checkCanSelectMember()) {
        return false;
    }

    var ids = id.split("_");
    var types = type.split("_");

    var entity = topWindow.getObject(types[0], ids[0]);
    if (!entity) {
        return true;
    }

    if (type == Constants_Post && entity.accountId == '-1730833917365171641') { //集团基准岗，不做校验
        return false;
    }

    if (type == Constants_Account) {
        var childrenDept = topWindow.findChildInList(topWindow.getDataCenter(Constants_Department, entity.id), entity.id);
        if (childrenDept == null || childrenDept.isEmpty()) {
            topWindow.initOrgModel(entity.id, currentMemberId, extParameters);
            childrenDept = topWindow.findChildInList(topWindow.getDataCenter(Constants_Department, entity.id), entity.id);
        }

        for (var i = 0; i < childrenDept.size(); i++) {
            var dept = childrenDept.get(i);
            var ms = dept.getAllMembers();
            if (ms != null && !ms.isEmpty()) {
                return false;
            }
        }
        return true;
//		if(entity.memberSize == 0){
//			return true;
//		}
    } else {
        if (entity.type == Constants_Department) {
            if (entity.externalType == '1') {
                return false;
            }
        }

        var ms = entity.getAllMembers();
        if (ms == null || ms.isEmpty()) {
            return true;
        }

        if (type == Constants_Department + "_" + Constants_Post) {
            for (var i = 0; i < ms.size(); i++) {
                var m = ms.get(i);
                if (m.postId == ids[1]) {
                    return false;
                }

                //部门下的副岗，可能是多个
                var sps = m.getSecondPost().get(ids[0]);
                if (sps) {
                    for (var c = 0; c < sps.size(); c++) {
                        if (sps.get(c).id == ids[1]) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }


    return false;
}

/**
 * 不包含子部门时，父部门没有人员的校验
 */
function checkEmptyMemberWithoutChildDept(type, id) {
    if (isRootAccount()) {
        return false;
    }
    if (!type
        || !id
        || !NeedCheckEmptyMemberType.contains(type)
        || !checkCanSelectMember()) {
        return false;
    }

    var ids = id.split("_");
    var types = type.split("_");

    var entity = topWindow.getObject(types[0], ids[0]);
    if (!entity) {
        return true;
    }

    var ms = entity.getDirectMembers();
    if (ms == null || ms.isEmpty()) {
        return true;
    }

    if (type == Constants_Department + "_" + Constants_Post) {
        for (var i = 0; i < ms.size(); i++) {
            var m = ms.get(i);
            if (m.postId == ids[1]) {
                return false;
            }

            //部门下的副岗，可能是多个
            var sps = m.getSecondPost().get(ids[0]);
            if (sps) {
                for (var c = 0; c < sps.size(); c++) {
                    if (sps.get(c).id == ids[1]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    return false;
}

/*
 * ?????????????
 * tempNowSelect ArrayList<Element>
 */
function selectOne(type, objTD) {

    var flag = false;
    if (type && objTD) {
        tempNowSelected.clear();
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            var ops = objTD.options;
            var count = 0;
            for (var i = 0; i < ops.length; i++) {
                var option = ops[i];
                if (option.selected) {
                    var e = getElementFromOption(option);
                    if (e) {
                        tempNowSelected.add(e);
                    }
                }
            }
        } else {
            if (arguments[2]) {
                var ops = document.getElementById(arguments[2]).childNodes;
                var count = 0;
                for (var i = 0; i < ops.length; i++) {
                    var option = ops[i];
                    if (option.getAttribute('seleted')) {
                        var e = getElementFromOption(option);
                        if (e) {
                            tempNowSelected.add(e);
                        }
                    }
                }
                selectOneMemberDiv(objTD);
                flag = true;
            }
        }
    }
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        if (arguments[2]) {
            //双击组 选择组
            listenermemberDataBodyDiv(document.getElementById(arguments[2]));
        } else {
            listenermemberDataBodyDiv(document.getElementById(temp_Div));
        }
    }
    if (tempNowSelected == null || tempNowSelected.isEmpty()) {
        return;
    }

    var _showAccountShortname = false;
    var unallowedSelectEmptyGroup = getParentWindowData("unallowedSelectEmptyGroup") || false;

    var isNotShowNoMemberConfirm = getParentWindowData("isNotShowNoMemberConfirm") || false;

    var alertMessageBeyondLevelScop = new StringBuffer();
    var alertMessageEmptyMemberNO = new StringBuffer();
    var alertMessageBeyondWorkScop = new StringBuffer();

    var isCanSelectGroupAccount = getParentWindowData("isCanSelectGroupAccount");
    var isConfirmExcludeSubDepartment = getParentWindowData("isConfirmExcludeSubDepartment");
    for (var i = 0; i < tempNowSelected.size(); i++) {
        var element = tempNowSelected.get(i);
        var type = element.type;
        if (type == Constants_WFDynamicForm && element.name == "" && element.id != "") {
            var elId = element.id;
            var els = elId.split("#");
            element.name = els[1];
        }
        if (type == Constants_Outworker) {
            type = Constants_Department;

            if (!checkIncludeElements(type, element.id)) {
                continue;
            }
        }

        if (!checkCanSelect(type)) {
            continue;
        }

        //外部单位标签，只能选枚举值
        if (type == Constants_JoinAccountTag) {
            if (element.hasChild) {
                continue;
            }
        }

        if (type == Constants_Account && excludeElements.contains(type + element.id)) {
            continue;
        }

        if (!checkExternalMemberWorkScope(type, element.id)) {
            if (isVjoinMember && !isAdmin && !isSubVjoinAdmin) {
                alert("有超过工作范围的人员，不能选择！");
            }
            continue;
        }

        var _entity = topWindow.getObject(type, element.id);
        if (element.type == Constants_Department) {
            if (_entity != null && _entity.getFullName) {
                element.description = _entity.getFullName();
            }
        }

        if (element.type == Constants_Department || element.type == Constants_JoinOrganization || element.type == Constants_JoinAccount) {
            element.externalType = _entity.externalType;
        }

        //内部人员: 当前是外部人员面板，检查外部单位能否直接选择，逻辑：只要有一个不可见，就返回；管理员：只判断人员是否为空
        if (isInternal && tempNowPanel.type == Constants_Outworker && unallowedSelectEmptyGroup && type == Constants_Department && checkCanSelectMember()) {
            var _ms = _entity.getAllMembers();

            if (!_ms || _ms.isEmpty()) {
                alertMessageEmptyMemberNO.append(element.name);
                continue;
            }

            if (!isAdmin && !showAllOuterDepartmentFlag) { //普通用户
                var extMember = topWindow.ExtMemberScopeOfInternal.get(element.id);
                if (!extMember) {
                    alertMessageEmptyMemberNO.append(element.name);
                    continue;
                }

                var isSelect = true;
                for (var i = 0; i < _ms.size(); i++) {
                    if (!extMember.contains(_ms.get(i).id)) {
                        isSelect = false;
                        break;
                    }
                }

                if (!isSelect) {
                    alertMessageBeyondWorkScop.append(element.name);
                    continue;
                }
            }
        }


        //如果是vjoin的根节点
        if (tempNowPanel.type == Constants_JoinOrganization && type == Constants_Account && checkCanSelectMember()) {
            //没有职务级别空的的，可以直接选，有职务级别控制的，需要判断人员是否都可以选
            if (isNeedCheckLevelScope && element.externalType != null && element.externalType == '3') {
                if (!isAdmin && !isAdministrator) {
                    var _ms = topWindow.getDataCenter(Constants_Member, currentVjoinAccountId);
                    var canSelect = true;
                    if (isV5Member) {
                        for (var i = 0; i < _ms.size(); i++) {
                            if (!checkVjoinMemberWorkScopeOfMember(_ms.get(i))) {
                                canSelect = false;
                                break;
                            }
                        }
                    } else if (isVjoinMember) {
                        var _VjMemberAccessVjAccounts = topWindow.VjMemberAccessVjAccounts;
                        for (var i = 0; i < _ms.size(); i++) {
                            if (_VjMemberAccessVjAccounts != null && !_VjMemberAccessVjAccounts.contains("D" + _ms.get(i).departmentId)) {//所选择的外单位/机构，下的人员全部在 当前外部人员可访问的外部单位内，则可以选择。
                                canSelect = false;
                                break;
                            }
                        }
                    }
                    if (!canSelect) {
                        alert("有超过工作范围的人员，不能选择！");
                        continue;
                    }
                }
            }
        }

        if (isInternal && tempNowPanel.type == Constants_JoinOrganization && type == Constants_Department && checkCanSelectMember() && isNeedCheckLevelScope) {
            /*			//如果是vjoin的根节点，不能选
			if(element.externalType!=null && element.externalType == '3'){
				continue;
			}*/
            var _ms = _entity.getAllMembers();

            /*		    if(!_ms || _ms.isEmpty()){
            alertMessageEmptyMemberNO.append(element.name);
            continue;
		    }*/

            if (!isAdmin && !showAllOuterDepartmentFlag) { //普通用户
                var vjoinMember = topWindow.VjoinMemberScopeOfInternal.get(_entity.accountId);
                /*		        if(!vjoinMember){
		            alertMessageEmptyMemberNO.append(element.name);
		            continue;
		        }*/

                var isSelect = true;
                for (var i = 0; i < _ms.size(); i++) {
                    if (!vjoinMember.contains(_ms.get(i).id)) {
                        isSelect = false;
                        break;
                    }
                }

                if (!isSelect) {
                    alertMessageBeyondWorkScop.append(element.name);
                    continue;
                }
            }
        }


        if ((isCanSelectGroupAccount == false || isGroupAccessable == false) && type == Constants_Account && element.id == rootAccount.id) {
            continue;
        }
        //检测越级访问，只要部门/组里面有任何一个人不能选择，则该部门/组不能选择
        if (type != Constants_Member && type != Constants_Department && type != Constants_BusinessDepartment && !checkAccessLevelScope(type, element.id)) {
            alertMessageBeyondLevelScop.append(element.name);
            continue;
        }

        //检测集团是否可选
        var canselectGroup = true;
        if (type == Constants_Account && element.id == "-1730833917365171641") {
            for (var i = 0; i < allAccounts.size(); i++) {
                var account = allAccounts.values().get(i);
                if (!checkAccessLevelScope(type, account.id)) {
                    canselectGroup = false;
                    break;
                }
            }
            if (!canselectGroup) {
                alertMessageBeyondLevelScop.append(element.name);
                continue;
            }
        }

        var key = type + element.id;

        if (key == "NodeCurrentNode" || key == "NodeNodeUser" || key == "NodeSenderSuperDept" || key == "NodeNodeUserSuperDept"
            || key == "NodeNodeUserManageDep" || key == "NodeNodeUserLeaderDep" || key == "NodeSenderManageDep" || key == "NodeSenderLeaderDep"
            || key == "NodeCurrentNodeSuperDept" || key == "NodeSenderSuperAccount" || key == "NodeNodeUserSuperAccount"
            || key.endsWith("MMNEOF")) {//自定义元数据：NodeNodeUserMemberMetadataNode-1854419294040210113MMNEOF"，不能直接选择如：上节点费用归属部门
            continue;
        }

        //综合办公控件-用车部门
        if (key == Constants_OfficeField + "UseDept") {
            continue;
        }

        if (selectedPeopleElements.containsKey(key)) {    //??????????????????
            continue; //Exist
        }

        //判断是否要子部门 //Constants_Node 工作流用的节点type
        if ((type == Constants_Department || type == Constants_Node) && isConfirmExcludeSubDepartment) {
            var isShowPageConfirm4Select = false;

            if (type == Constants_Department) {//当前选择的部门
                var _getChildrenFun = Constants_Panels.get(type).getChildrenFun;

                var entity = topWindow.getObject(type, element.id);
                if (entity) {
                    datas2Show = eval("entity." + _getChildrenFun + "()");
                } else {
                    datas2Show = topWindow.findChildInList(topWindow.getDataCenter(type), id);
                }
                isShowPageConfirm4Select = datas2Show && !datas2Show.isEmpty();
            } else if (type == Constants_Node) {
                isShowPageConfirm4Select = element.id.endsWith("DeptMember");
            }

            if (element.externalType != null && element.externalType == '1') {
                isShowPageConfirm4Select = false;
            }
            if (isShowPageConfirm4Select) {
                var _index = element.name.indexOf("(" + $.i18n("selectPeople.excludeChildDepartment") + ")");
                if (_index != -1) {
                    element.name = element.name.substring(0, _index);
                }

                //【包含】 true 【不包含】 false【取消】 ''
                var temp = showConfirm4Select(element.name);
                if (temp == '') {
                    continue; //表示不选，跳过
                } else if (temp == 'false') {//通过JSP页面来提示是否包含子部门
                    element.excludeChildDepartment = true;
                    element.name += "(" + $.i18n("selectPeople.excludeChildDepartment") + ")";
                } else {
                    element.excludeChildDepartment = false;
                }
            }
            if (element.excludeChildDepartment == false) {//包含子部门
                if (type != "Node") {
                    if (!checkAccessLevelScopeWithChildDept(element.id)) {
                        alertMessageBeyondLevelScop.append(element.name);
                        continue;
                    }
                }
            } else {//OA-48542
                if (!checkAccessLevelScope(type, element.id)) {//不包含子部门
                    alertMessageBeyondLevelScop.append(element.name);
                    continue;
                }
            }
        } else if (type == Constants_Department && !checkAccessLevelScope(type, element.id)) {
            alertMessageBeyondLevelScop.append(element.name);
            continue;
        }

        //判断是否要业务线子部门
        if (type == Constants_BusinessDepartment) {
            var businessId = _entity.businessId;
            var entity = topWindow.getObject(type, element.id);

            if (isConfirmExcludeSubDepartment) {
                var isShowPageConfirm4Select = false;

                var _getChildrenFun = Constants_Panels.get(type).getChildrenFun;

                if (entity) {
                    datas2Show = eval("entity." + _getChildrenFun + "()");
                } else {
                    datas2Show = topWindow.findChildInList(topWindow.getDataCenter(type), id);
                }
                isShowPageConfirm4Select = datas2Show && !datas2Show.isEmpty();

                if (element.externalType != null && element.externalType == '1') {
                    isShowPageConfirm4Select = false;
                }
                if (isShowPageConfirm4Select) {
                    var _index = element.name.indexOf("(" + $.i18n("selectPeople.excludeChildDepartment") + ")");
                    if (_index != -1) {
                        element.name = element.name.substring(0, _index);
                    }

                    //【包含】 true 【不包含】 false【取消】 ''
                    var temp = showConfirm4Select(element.name);
                    if (temp == '') {
                        continue; //表示不选，跳过
                    } else if (temp == 'false') {//通过JSP页面来提示是否包含子部门
                        element.excludeChildDepartment = true;
                        element.name += "(" + $.i18n("selectPeople.excludeChildDepartment") + ")";
                    } else {
                        element.excludeChildDepartment = false;
                    }
                }

                var businessAccount = topWindow.getObject(Constants_BusinessAccount, businessId);
                if (businessAccount) {
                    var accessMemberIds = businessAccount.accessMemberIds;
                    var pass = true;
                    if (isNeedCheckLevelScope) {
                        if (element.excludeChildDepartment == false) {//包含子部门
                            var members = entity.getAllMembers();
                            for (var i = 0; i < members.size(); i++) {
                                var member = members.get(i);
                                if (accessMemberIds.indexOf(member.id) < 0) {
                                    alertMessageBeyondLevelScop.append(element.name);
                                    pass = false;
                                    break;
                                }
                            }
                        } else {
                            var members = entity.getDirectMembers();
                            for (var i = 0; i < members.size(); i++) {
                                var member = members.get(i);
                                if (accessMemberIds.indexOf(member.id) < 0) {
                                    alertMessageBeyondLevelScop.append(element.name);
                                    pass = false;
                                    break;
                                }
                            }
                        }
                        if (!pass) {
                            continue;
                        }
                    }
                }

            } else {
                var businessAccount = topWindow.getObject(Constants_BusinessAccount, businessId);
                if (businessAccount) {
                    var accessMemberIds = businessAccount.accessMemberIds;
                    var members = entity.getAllMembers();
                    var pass = true;
                    if (isNeedCheckLevelScope) {
                        for (var i = 0; i < members.size(); i++) {
                            var member = members.get(i);
                            if (accessMemberIds.indexOf(member.id) < 0) {
                                alertMessageBeyondLevelScop.append(element.name);
                                pass = false;
                                break;
                            }
                        }
                        if (!pass) {
                            continue;
                        }
                    }
                }
            }
        }


        var isconfirmed = false;
        //检测集合里面是否是空的，一般检测部门和组
        if (checkEmptyMember(type, element.id)) {
            if (unallowedSelectEmptyGroup) { //不允许选择空组
                alertMessageEmptyMemberNO.append(element.name);
                continue;
            } else {
                if (!isNotShowNoMemberConfirm) {
                    isconfirmed = true;
                    if (!confirm($.i18n("selectPeople.alertEmptyMember", element.name))) {
                        continue;
                    }
                }
            }
        }
        if (type == Constants_Department && element.excludeChildDepartment == true) {//不包含子部门，父部门下没有人
            if (checkEmptyMemberWithoutChildDept(type, element.id)) {
                if (unallowedSelectEmptyGroup) { //不允许选择空部门
                    alertMessageEmptyMemberNO.append(element.name);
                    continue;
                } else {
                    if (!isNotShowNoMemberConfirm) {
                        if (!isconfirmed) {
                            if (!confirm($.i18n("selectPeople.alertEmptyMember", element.name))) {
                                continue;
                            }
                        }
                    }
                }
            }
        }

        var _accountId = currentAccountId;
        if (element && element.accountId) {
            _accountId = element.accountId;
        } else if (_entity && _entity.accountId) {
            _accountId = _entity.accountId;
        }
        var accountShortname = allAccounts.get(_accountId).shortname;

        element.type = type;
        element.typeName = Constants_Component.get(type);
        element.accountId = _accountId;
        element.accountShortname = accountShortname;
        if (element.type == Constants_Member) {
            if (returnMemberWithDept && tempNowPanel.type == Constants_Department) {
                var ids = element.id.split(valuesJoinSep2);
                var deptEntity = topWindow.getObject(Constants_Department, ids[0]);
                if (deptEntity && deptEntity.name) {
                    element.name = element.name + "(" + deptEntity.name + ")";
                }
            }
        }

        add2List3(element);
        selectedPeopleElements.put(key, element);
    }

    var sp = $.i18n("common_separator_label");
    var alertMessage = "";
    if (!alertMessageBeyondWorkScop.isBlank()) {
        alertMessage += ($.i18n("selectPeople.alertBeyondWorkScope", alertMessageBeyondWorkScop.toString(sp).getLimitLength(50, "..."))) + "\n\n";
    }
    if (!alertMessageBeyondLevelScop.isBlank()) {
        alertMessage += ($.i18n("selectPeople.alertBeyondLevelScope", alertMessageBeyondLevelScop.toString(sp).getLimitLength(50, "..."))) + "\n\n";
    }
    if (!alertMessageEmptyMemberNO.isBlank()) {
        alertMessage += ($.i18n("selectPeople.alertEmptyMemberNO", alertMessageEmptyMemberNO.toString(sp).getLimitLength(50, "...")));
    }

    if (alertMessage) {
        alert(alertMessage);
    }
}

//选人界面弹出页面按照提示进行，【包含】【不包含】【取消】提示
function showConfirm4Select(name) {
    if (v3x.isChrome || v3x.isFirefox) {
        var rv = confirm('"' + name + '"' + $.i18n("selectPeople.ConfirmChildDept") + "(" + $.i18n("selectPeople.ConfirmChildDesc.js") + ")");
        if (rv == true) {
            return 'true';
        } else {
            return 'false';
        }
    } else {
        var rv = v3x.openWindow({
            url: _ctxPath + "/selectpeople.do?method=selectPeople4Confirm&name=" + encodeURIComponent(name) + CsrfGuard.getUrlSurffix(),
            height: 120,
            width: 350
        });
        if (rv == 0) {
            return 'true';
        }
        if (rv == 1) {
            return 'false';
        }


        return '';
    }
}

/**
 * ????????
 */
function add2List3(element) {
    var key = element.type + element.id;

    var text = element.name;
    try {
        if (element.type === 'BusinessDepartment' || element.type === 'BusinessAccount') {
            //多维组织已经加了单位简称，不需要再后面追加单位简称了
        } else if (element.accountShortname && checkShowAccountShortname4Element(element) && $.ctx.CurrentUser.loginAccount != element.accountId) {
            var endName = "(" + element.accountShortname + ")";
            if (!text.endsWith(endName)) {
                text = text + endName;
            }
        }
    } catch (e) {
    }
    if (v3x.getBrowserFlag('selectPeopleShowType')) {
        var option = new Option(text, key);
        option.id = element.id;
        option.type = element.type;
        option.className = element.type + "";
        if ((element.type == Constants_Department || element.type == Constants_Member || element.type == "Department_Post") && element.description != "" && element.description != undefined) {
            //如果是选择部门或者部门下的岗位,title 显示部门的全路径（全路径暂时存放在description属性中）
            option.title = element.description;
        } else {
            option.title = element.name;
        }
        option.setAttribute('style', 'margin:4px 0 0 4px');
        option.accountId = element.accountId;
        option.accountShortname = element.accountShortname;

        option.setAttribute('type', element.type);
        option.setAttribute('accountId', element.accountId);
        option.setAttribute('accountShortname', element.accountShortname);
        option.setAttribute('externalType', element.externalType);

        document.getElementById("List3").options.add(option);
    } else {
        var option = document.createElement('div');
        var text = document.createTextNode(text);
        option.appendChild(text);
        option.setAttribute('id', element.id);
        option.setAttribute('value', key);
        option.setAttribute('type', element.type);
        option.setAttribute('seleted', 'false');
        option.setAttribute('style', 'margin:4px 0 0 4px');
        option.setAttribute('class', 'member-list-div');
        option.setAttribute('accountId', element.accountId);
        option.setAttribute('accountShortname', element.accountShortname);
        option.setAttribute('externalType', element.externalType);

        option.onclick = function () {
            selectMemberFn(this);
        }
        option.ondblclick = function () {
            removeOne(key, this);
        }
        document.getElementById("List3").appendChild(option);
        initIpadScroll("List3");//ipad滚动条解决
    }
}

/*
 * 从List3种删除数据，需要选择List3-item
 */
function removeOne(key, obj) {
    if (!key) {	//删除多项
        var ops = document.getElementById("List3");
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            for (var i = 0; i < ops.length; i++) {
                if (ops[i].selected) {
                    var key = ops[i].value;
                    document.getElementById("List3").remove(i);

                    selectedPeopleElements.remove(key);
                    i--;
                }
            }
        } else {
            var ops = document.getElementById("List3").childNodes;
            for (var i = 0; i < ops.length; i++) {
                var option = ops[i];
                if (option) {
                    if (option.getAttribute('seleted') == 'true') {
                        var key = option.getAttribute('value');
                        option.parentNode.removeChild(option);
                        selectedPeopleElements.remove(key);
                        i--;
                    }
                }
            }
        }
    } else {	//删除单项
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            var i = obj.selectedIndex;
            if (i >= 0) {
                document.getElementById("List3").remove(obj.selectedIndex);
                selectedPeopleElements.remove(key);
            }
        } else {
            obj.parentNode.removeChild(obj);
            selectedPeopleElements.remove(key);
        }
    }
}

/******************** ?? List3 ?????????????? ********************/

//上移或下移已经选择了的数据
function exchangeList3Item(direction) {
    var list3Object = document.getElementById("List3");
    var list3Items = list3Object.options;
    var nowIndex = list3Object.selectedIndex;
    //ipad div实现select
    if (!v3x.getBrowserFlag('selectPeopleShowType')) {
        list3Items = list3Object.childNodes;
        for (var i = 0; i < list3Items.length; i++) {
            var op = list3Items[i];
            var selected = op.getAttribute('seleted');
            if (selected == 'true') {
                nowIndex = i;
            }
        }
    }

    if (direction == "up") {
        if (nowIndex > 0) {
            if (v3x.getBrowserFlag('selectPeopleShowType')) {
                var nowOption = list3Items.item(nowIndex);
                var nextOption = list3Items.item(nowIndex - 1);

                //多浏览器处理
                var textTemp = nowOption.innerHTML;
                var valueTemp = nowOption.getAttribute('value');
                var classTemp = nowOption.getAttribute('class');
                var nowTitle = nowOption.title;
                var nowId = nowOption.id;

                var textTemp2 = nextOption.innerHTML;
                var valueTemp2 = nextOption.getAttribute('value');
                var classTemp2 = nextOption.getAttribute('class');
                var nextTitle = nextOption.title;
                var nextId = nextOption.id;

                nowOption.innerHTML = textTemp2;
                nowOption.setAttribute('value', valueTemp2);
                nowOption.setAttribute('class', classTemp2);
                nowOption.setAttribute('title', nextTitle);
                nowOption.setAttribute('id', nextId);

                nextOption.innerHTML = textTemp;
                nextOption.setAttribute('value', valueTemp);
                nextOption.setAttribute('class', classTemp);
                nextOption.setAttribute('title', nowTitle);
                nextOption.setAttribute('id', nowId);
                list3Object.selectedIndex = nowIndex - 1;
                /*
				var newOption = new Option(nowOption.text, nowOption.value);
				newOption.className = nowOption.className;
				newOption.selected = true;
				list3Object.add(newOption, nowIndex - 1);
				list3Object.remove(nowIndex + 1);
				*/
                selectedPeopleElements.swap(nowOption.value, nextOption.value);
            } else {
                var nowOption = list3Items[nowIndex];
                var nextOption = list3Items[nowIndex - 1];

                var textTemp = nextOption.innerHTML;
                var valueTemp = nextOption.getAttribute('value');
                var nowTitle = nowOption.title;
                var nowId = nowOption.id;

                var nextTitle = nextOption.title;
                var nextId = nextOption.id;

                nowOption.innerHTML = textTemp;
                nowOption.setAttribute('value', valueTemp);
                nowOption.setAttribute('seleted', 'false');
                nowOption.setAttribute('class', 'member-list-div');
                nowOption.setAttribute('title', nextTitle);
                nowOption.setAttribute('id', nextId);

                nextOption.innerHTML = nowOption.innerHTML;
                nextOption.setAttribute('value', nowOption.getAttribute('value'));
                nextOption.setAttribute('seleted', 'true');
                nextOption.setAttribute('class', 'member-list-div-select');
                nextOption.setAttribute('title', nowTitle);
                nextOption.setAttribute('id', nowId);
                selectedPeopleElements.swap(nowOption.getAttribute('value'), nextOption.getAttribute('value'));
            }

        }
    } else if (direction == "down") {
        if (nowIndex > -1 && nowIndex < list3Items.length - 1) {
            if (v3x.getBrowserFlag('selectPeopleShowType')) {
                var nowOption = list3Items.item(nowIndex);
                var nextOption = list3Items.item(nowIndex + 1);

                //多浏览器处理
                var textTemp = nowOption.innerHTML;
                var valueTemp = nowOption.getAttribute('value');
                var classTemp = nowOption.getAttribute('class');
                var nowTitle = nowOption.title;
                var nowId = nowOption.id;

                var textTemp2 = nextOption.innerHTML;
                var valueTemp2 = nextOption.getAttribute('value');
                var classTemp2 = nextOption.getAttribute('class');
                var nextTitle = nextOption.title;
                var nextId = nextOption.id;

                nowOption.innerHTML = textTemp2;
                nowOption.setAttribute('value', valueTemp2);
                nowOption.setAttribute('class', classTemp2);
                nowOption.setAttribute('title', nextTitle);
                nowOption.setAttribute('id', nextId);

                nextOption.innerHTML = textTemp;
                nextOption.setAttribute('value', valueTemp);
                nextOption.setAttribute('class', classTemp);
                nextOption.setAttribute('title', nowTitle);
                nextOption.setAttribute('id', nowId);
                list3Object.selectedIndex = nowIndex + 1;

                /**
                 var newOption = new Option(nowOption.text, nowOption.value);
                 newOption.className = nowOption.className;
                 newOption.selected = true;
                 list3Object.add(newOption, nowIndex + 2);
                 list3Object.remove(nowIndex);
                 **/
                selectedPeopleElements.swap(nowOption.value, nextOption.value);
            } else {
                var nowOption = list3Items[nowIndex];
                var nextOption = list3Items[nowIndex + 1];
                var nowTitle = nowOption.title;
                var nowId = nowOption.id;

                var textTemp = nextOption.innerHTML;
                var valueTemp = nextOption.getAttribute('value');
                var nextTitle = nextOption.title;
                var nextId = nextOption.id;

                nextOption.innerHTML = nowOption.innerHTML;
                nextOption.setAttribute('value', nowOption.getAttribute('value'));

                nowOption.innerHTML = textTemp;
                nowOption.setAttribute('value', valueTemp);
                nowOption.setAttribute('seleted', 'false');
                nowOption.setAttribute('class', 'member-list-div');
                nowOption.setAttribute('title', nextTitle);
                nowOption.setAttribute('id', nextId);

                nextOption.setAttribute('seleted', 'true');
                nextOption.setAttribute('class', 'member-list-div-select');
                nextOption.setAttribute('title', nowTitle);
                nextOption.setAttribute('id', nowId);
                selectedPeopleElements.swap(nowOption.getAttribute('value'), nextOption.getAttribute('value'));
            }
        }
    } else {
        log.warn('The direction ' + direction + ' is not defined.');
    }
}

function searchTemplate(members, _members, type) {
    for (var i = 0; i < _members.length; i++) {
        var m = _members[i];

        var secondPostIds = null;
        var SP = m["F"];
        if (SP) {
            secondPostIds = new ArrayList();
            for (var s = 0; s < SP.length; s++) {
                var secondPostId = new Array();
                secondPostId[0] = SP[s][0];
                secondPostId[1] = SP[s][1];
                secondPostIds.add(secondPostId);
            }
        } else {
            secondPostIds = EmptyArrayList;
        }

        var member = new Member(m["K"], m["N"], m["S"], m["D"], m["P"], secondPostIds, m["L"], m["I"], m["Y"], m["M"], "", m["A"], m["E"]);
        member.departmentName = m["DM"];
        member.departmentNameF = m["DF"];
        if (type == "G") {
            member.type = "G";
        } else {
            if (currentAccount.id != member.accountId) {
                member.type = "E";
            }
            member.post = new Post(member.postId, m["PM"]);
        }
        members.add(member);
        searchNames.put(member.id, member);
    }
}

//在全集团范围内查询出的人员, 用于获取人员姓名
var searchNames = new Properties();

/*******************************
 * 搜索
 */
var isSearch = false;

function searchItems() {
    if (tempNowPanel == null) {
        return;
    }

    var type = tempNowPanel.type;
    var showMode = tempNowPanel.showMode;
    var searchArea = tempNowPanel.searchArea;

    if (document.getElementById("q").disabled) {
        return;
    }

    reArea_1_2();
    if (!checkIsShowArea2()) {
        hiddenArea2(true);
        document.getElementById("Separator1").style.display = "none";
    }

    var keyword = document.getElementById("q").value;

    keyword = (keyword == document.getElementById("q").defaultValue) ? "" : keyword;

    var originalKey = keyword;

    checkSearchAlt(true);

    if (!keyword) {//没有关键字, 给出提示
        $("#q").blur();
        if (showMode == Constants_ShowMode_TREE) {
            var expandedNode = tree.getSelected();
            if (!expandedNode) {
                return;
            }
            if (expandedNode.folder || expandedNode.hasChild) {
                expandedNode.expand();
            }
//			expandedNode.toggle();
            clearList2();

            if (type == Constants_Department) {
                var n = topWindow.getObject(type, expandedNode.id);
                if (n) {
                    var _status = isShowDepartmentTree(n);
                    if (_status != 0) {
                        return;
                    }
                }
            }

            showList2(type, expandedNode.id);
            return;
        }
        /* else if(showMode == Constants_ShowMode_LIST){
			initList(type);
			if(nowSelectedList1Item){
				showList2(type, nowSelectedList1Item.id);
			}
		}*/
        //$.alert($.i18n("selectPeople.index_input_error"));
        //return;
    }

    keyword = keyword.toLowerCase();

    if (showMode == Constants_ShowMode_LIST && searchArea == 1) {//只搜索1区
        if (type == Constants_Node) {
            try {
                var v = nowSelectedList1Item.options[nowSelectedList1Item.selectedIndex].value;
                if (v) {
                    showList2(type, v, null, keyword);
                }
            } catch (e) {
            }

        } else {
            if (canShowBusinessOrg && (type == Constants_Node || type == Constants_FormField)) {
                initList(type, keyword, $("#areaTopList1_BusinessDepartment").val());
            } else {
                initList(type, keyword);
            }

            reArea_1_2();
            if (!checkIsShowArea2()) {
                hiddenArea2(true);
                document.getElementById("Separator1").style.display = "none";
            }
        }

        return;
    }

    if (type == Constants_Department || type == Constants_Account) { //当前是部门面板
        reArea_1_2();

        clearList2();

        var members = null;
        var department = null;
        var departments = null;
        var accounts = null;

        var seachGroup = !$("#seachGroupMember").is(":hidden") && $("#seachGroup").prop("checked");

        if (seachGroup) {
            var spm = new selectPeopleManager();
            var result = spm.getQueryOrgModel(originalKey, isNeedCheckLevelScope);

            if (!result) {
                return;
            }

            var _members = result[Constants_Member];
            if (_members) {
                members = new ArrayList();
                searchTemplate(members, _members, "G");
            }
        } else if (type == Constants_Account) {
            accounts = accessableAccounts.values();
        } else {
            var expandedNode = tree.getSelected();
            if (!expandedNode) {
                return;
            }

            var id = expandedNode.id;
            var _type = expandedNode.type;

            if (currentArea2Type != Constants_Member) {
                if (_type == Constants_Department) {
                    showSubOfDepartment(id, currentArea2Type, keyword);
                } else if (_type == Constants_Account && currentArea2Type == Constants_Post) {//在单位下查询岗位
                    showDepartmentPostOfAccount(id, currentArea2Type, keyword);
                }

                return;
            }

            if (_type == Constants_Department) {
                department = topWindow.getObject(Constants_Department, id);
                if (!department) {
                    return;
                }
                members = department.getAllMembers();
                departments = department.getAllChildren();
            } else if (_type == Constants_Account) {
                department = currentAccount;
                if (isV5Member) {
                    if (browserIsMSIE || browserIsEDGE) {
                        var spm = new selectPeopleManager();
                        var result = spm.getQueryOrgModel(originalKey, isNeedCheckLevelScope, currentAccount.id);

                        if (!result) {
                            return;
                        }

                        var _members = result[Constants_Member];
                        members = new ArrayList();
                        if (_members != null && _members != undefined) {
                            len = _members.length;
                            for (var i = 0; i < len; i++) {
                                var TempMember = topWindow.getObject(Constants_Member, _members[i]["K"]);
                                if (TempMember && TempMember.isInternal) {
                                    members.add(TempMember);
                                }
                            }
                        }

                        var deptResult = spm.getQueryOrgModelByType(originalKey, isNeedCheckLevelScope, currentAccount.id, Constants_Department);

                        if (deptResult) {
                            var _departments = deptResult[Constants_Department];
                            departments = new ArrayList();
                            if (_departments != null && _departments != undefined) {
                                len = _departments.length;
                                for (var i = 0; i < len; i++) {
                                    var TempDepartment = topWindow.getObject(Constants_Department, _departments[i]["K"]);
                                    if (TempDepartment && TempDepartment.isInternal) {
                                        departments.add(TempDepartment);
                                    }
                                }
                            }
                        }
                    } else {
                        members = topWindow.getDataCenter(Constants_Member, currentAccount.id);
                        departments = topWindow.getDataCenter(Constants_Department, currentAccount.id);
                    }
                } else {
                    members = topWindow.getDataCenter(Constants_Member, currentAccount.id);
                    departments = topWindow.getDataCenter(Constants_Department, currentAccount.id);
                }


            }
        }

        var selectHTML = new StringBuffer();
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_prefix);
        } else {
            selectHTML.append(memberDataBody_div);
        }
        if (departments && checkCanSelect(Constants_Department)) {
            for (var d = 0; d < departments.size(); d++) {
                var dept = departments.get(d);

                if (!dept.isInternal || dept.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                if (isVjoinMember) {
                    var _AccessInnerDepts = topWindow.AccessInnerDepts;
                    if (_AccessInnerDepts == null) continue;
                    if (!checkExternalMemberWorkScope(Constants_Department, dept.id)) {
                        continue;
                    }
                }

                var parentDepartmentId = dept.parentId;

                var parentDeptName = null;
                if (parentDepartmentId == currentAccountId) {
                    parentDeptName = allAccounts.get(parentDepartmentId).shortname;
                } else {
                    parentDeptName = topWindow.getObject(Constants_Department, dept.parentId).name;
                }

                var showText = dept.name;
                var showTitle = Constants_Component.get(Constants_Department) + ": " + dept.name + "&#13;"
                    + Constants_Component.get(Constants_OrgUp) + ": " + parentDeptName;

                if (parentDeptName) {
                    showText = showText.getLimitLength(nameMaxLength.two[0]);
                    var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
                    showText += getNameSpace(tempIndex);
                    showText += parentDeptName;
                }

                if (!excludeElements.contains(Constants_Department + dept.id) && checkIncludeElements(Constants_Department, dept.id)) {
                    if (v3x.getBrowserFlag('selectPeopleShowType')) {
                        selectHTML.append("<option value='").append(dept.id).append("' class='Department' type='Department' accountId='").append(currentAccountId).append("' title='" + showTitle + "'").append("'>").append(showText.escapeHTML(true)).append("</option>");
                    } else {
                        selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\"  value='").append(dept.id).append("' class='Department' type='Department' accountId='").append(currentAccountId).append("' title='" + showTitle + "'").append("'>").append(showText.escapeHTML(true)).append("</div>");
                    }
                }
            }
        }
        if (accounts) {
            for (var d = 0; d < accounts.size(); d++) {
                var acc = accounts.get(d);
                if (!acc || acc.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                var showText = acc.name;
                if (!excludeElements.contains(Constants_Account + acc.id) && checkIncludeElements(Constants_Account, acc.id)) {
                    if (v3x.getBrowserFlag('selectPeopleShowType')) {
                        selectHTML.append("<option value='").append(acc.id).append("' class='Account' type='Account' accountId='").append(acc.id).append("'>").append(showText.escapeHTML(true)).append("</option>");
                    } else {
                        selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)' value='").append(acc.id).append("' class='Account' type='Account' accountId='").append(acc.id).append("'>").append(showText.escapeHTML(true)).append("</div>");
                    }
                }
            }
        }

        if (members && checkCanSelectMember()) {
            var hasShowMembers = {};

            var currentDepartment = null;
            if (onlyCurrentDepartment) {
                currentDepartment = topWindow.getObject(Constants_Department, departmentId);
            }

            var member_size = members.size();
            var childDepts = childDeptOfCurrent(currentMember);
            for (var m = 0; m < member_size; m++) {
                var member = members.get(m);

                if (!member.isInternal || hasShowMembers[member.id]) { //已经显示了，防止副岗兼职重复出现
                    continue;
                }
                if (member.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                if (member.type != "G" && !checkLevelScope(member, department, childDepts)) { //越级
                    continue;
                }

                if (isVjoinMember && !checkExternalMemberWorkScope(Constants_Member, member.id)) {
                    continue;
                }

                var mDept = member.getDepartment();

                var _status = isShowDepartmentTree(mDept);
                if (_status != 0) {
                    continue;
                }

                if (onlyCurrentDepartment && currentDepartment && !mDept.path.startsWith(currentDepartment.path)) {
                    continue;
                }

                if (!excludeElements.contains(Constants_Member + member.id) && checkIncludeElements4Member(member)) {
                    selectHTML.append(addMember(Constants_Department, department, member));
                    hasShowMembers[member.id] = "T";
                }
            }

            hasShowMembers = null;
        }
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_subfix);
        } else {
            selectHTML.append(memberDataBody_div_end);
        }

        document.getElementById("Area2").innerHTML = selectHTML.toString();
    }

    if (type == Constants_BusinessDepartment) { //当前是多维组织部门面板
        var businessId = $("#areaTopList1_" + type).val();
        reArea_1_2();

        clearList2();

        var members = null;
        var department = null;
        var departments = null;
        var accounts = null;
        var expandedNode = tree.getSelected();
        if (!expandedNode) {
            return;
        }

        var id = expandedNode.id;
        var _type = expandedNode.type;

        if (currentArea2Type != Constants_Member) {
            if (_type == Constants_BusinessDepartment) {
                showSubOfBusinessDepartment(id, currentArea2Type, keyword);
            }
            return;
        }

        if (_type == Constants_BusinessDepartment) {
            department = topWindow.getObject(Constants_BusinessDepartment, id);
            if (!department) {
                return;
            }
            members = department.getAllMembers();
            departments = department.getAllChildren();
        } else if (_type == Constants_BusinessAccount) {
            departments = new ArrayList();
            members = new ArrayList();

            var allBusinessDepartments = topWindow.getDataCenter(Constants_BusinessDepartment, currentAccount.id);
            for (var i = 0; i < allBusinessDepartments.size(); i++) {
                var b = allBusinessDepartments.get(i);
                if (b.businessId == businessId) {
                    departments.add(b);
                    var deptDirMembers = b.getDirectMembers();
                    if (deptDirMembers.size() > 0) {
                        for (var j = 0; j < deptDirMembers.size(); j++) {
                            members.add(deptDirMembers.get(j));
                        }
                    }
                }
            }
        }

        var selectHTML = new StringBuffer();
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_prefix);
        } else {
            selectHTML.append(memberDataBody_div);
        }
        if (departments && checkCanSelect(Constants_BusinessDepartment)) {
            for (var d = 0; d < departments.size(); d++) {
                var dept = departments.get(d);

                if (dept.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                var parentDeptName = "";
                var parent = topWindow.getObject(Constants_BusinessDepartment, dept.parentId);
                if (parent) {
                    var parentDeptName = parent.name;
                }

                var showText = dept.name;
                var showTitle = Constants_Component.get(Constants_BusinessDepartment) + ": " + dept.name + "&#13;"
                    + Constants_Component.get(Constants_OrgUp) + ": " + parentDeptName;

                if (parentDeptName) {
                    showText = showText.getLimitLength(nameMaxLength.two[0]);
                    var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
                    showText += getNameSpace(tempIndex);
                    showText += parentDeptName;
                }

                if (!excludeElements.contains(Constants_BusinessDepartment + dept.id) && checkIncludeElements(Constants_BusinessDepartment, dept.id)) {
                    if (v3x.getBrowserFlag('selectPeopleShowType')) {
                        selectHTML.append("<option value='").append(dept.id).append("' class='BusinessDepartment' type='BusinessDepartment' accountId='").append(currentAccountId).append("' title='" + showTitle + "'").append("'>").append(showText.escapeHTML(true)).append("</option>");
                    } else {
                        selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\"  value='").append(dept.id).append("' class='BusinessDepartment' type='BusinessDepartment' accountId='").append(currentAccountId).append("' title='" + showTitle + "'").append("'>").append(showText.escapeHTML(true)).append("</div>");
                    }
                }
            }
        }

        if (members && checkCanSelectMember()) {
            var businessAccount = topWindow.getObject(Constants_BusinessAccount, businessId);
            if (businessAccount) {
                var accessMemberIds = businessAccount.accessMemberIds;
                var member_size = members.size();

                var hasMemberId = new ArrayList();
                for (var m = 0; m < member_size; m++) {
                    var member = members.get(m);

                    if (member.name.toLowerCase().indexOf(keyword) < 0) {
                        continue;
                    }

                    if (isNeedCheckLevelScope && accessMemberIds.indexOf(member.id) == -1) {
                        continue;
                    }

                    if (!excludeElements.contains(Constants_Member + member.id) && checkIncludeElements4Member(member)) {
                        if (!hasMemberId.contains(member.id)) {
                            hasMemberId.add(member.id);
                            selectHTML.append(addMember(Constants_Department, department, member));
                        }
                    }
                }
            }
        }
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_subfix);
        } else {
            selectHTML.append(memberDataBody_div_end);
        }

        document.getElementById("Area2").innerHTML = selectHTML.toString();
    } else if (type == Constants_JoinOrganization) { //当前是外部机构页签
        reArea_1_2();

        clearList2();

        var members = null;
        var department = null;
        var departments = null;
        var accounts = null;

        if (isV5Member && !isInternal) {
            return;
        }
        var expandedNode = tree.getSelected();
        if (!expandedNode) {
            return;
        }

        var id = expandedNode.id;
        var _type = expandedNode.type;

        if (_type == Constants_Department) {
            department = topWindow.getObject(Constants_Department, id);
            if (!department) {
                return;
            }
            members = department.getAllMembers();
            departments = department.getAllChildren();
        } else if (_type == Constants_Account) {
            department = currentAccount;
            if (browserIsMSIE || browserIsEDGE) {
                var spm = new selectPeopleManager();
                var result = spm.getQueryOrgModelByType(originalKey, isNeedCheckLevelScope, currentAccount.id, "JoinMember");

                if (!result) {
                    return;
                }

                var _members = result["JoinMember"];
                members = new ArrayList();
                if (_members != null && _members != undefined) {
                    len = _members.length;
                    for (var i = 0; i < len; i++) {
                        var TempMember = topWindow.getObject(Constants_Member, _members[i]["K"]);
                        if (TempMember) {
                            members.add(TempMember);
                        }
                    }
                }

                var deptResult = spm.getQueryOrgModelByType(originalKey, isNeedCheckLevelScope, currentAccount.id, "JoinAccount");

                if (deptResult) {
                    var _departments = deptResult["JoinAccount"];
                    departments = new ArrayList();
                    if (_departments != null && _departments != undefined) {
                        len = _departments.length;
                        for (var i = 0; i < len; i++) {
                            var TempDepartment = topWindow.getObject(Constants_Department, _departments[i]["K"]);
                            if (TempDepartment) {
                                departments.add(TempDepartment);
                            }
                        }
                    }
                }

            } else {
                var _members = topWindow.getDataCenter(Constants_Member, currentVjoinAccountId);
                members = new ArrayList();
                if (_members != null && _members != undefined) {
                    len = _members.size();
                    for (var i = 0; i < len; i++) {
                        var TempMember = topWindow.getObject(Constants_Member, _members.get(i).id);
                        if (TempMember) {
                            members.add(TempMember);
                        }
                    }
                }

                departments = topWindow.getDataCenter(Constants_Department, currentVjoinAccountId);
            }
        }

        var selectHTML = new StringBuffer();
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_prefix);
        } else {
            selectHTML.append(memberDataBody_div);
        }

        var _AccessVjoinDepts = topWindow.AccessVjoinDepts;

        if (departments && checkCanSelect(Constants_Department) && _AccessVjoinDepts != null) {
            for (var d = 0; d < departments.size(); d++) {
                var dept = departments.get(d);

                if (dept.isInternal || dept.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                if (dept.externalType == "0" || (showExternalType && showExternalType != dept.externalType)) {
                    continue;
                }

                if (isV5Member) {
                    if (isNeedCheckLevelScope && !_AccessVjoinDepts.contains("D" + dept.id)) {
                        continue;
                    }
                }

                if (isVjoinMember) {
                    var _AccessInnerDepts = topWindow.AccessInnerDepts;
                    if (_AccessInnerDepts == null) continue;
                    if (!checkExternalMemberWorkScope(Constants_Department, dept.id)) {
                        continue;
                    }
                }

                var parentDeptName = null;
                var parentDepartmentId = dept.parentId;
                if (parentDepartmentId == currentVjoinAccountId) {
                    parentDeptName = allAccounts.get(parentDepartmentId).shortname;
                } else {
                    parentDeptName = topWindow.getObject(Constants_Department, parentDepartmentId).name;
                }

                var showText = dept.name;
                var showTitle = Constants_Component.get(Constants_Department) + ": " + dept.name + "&#13;"
                    + Constants_Component.get(Constants_OrgUp) + ": " + parentDeptName;

                if (parentDeptName) {
                    showText = showText.getLimitLength(nameMaxLength.two[0]);
                    var tempIndex = nameMaxLength.two[0] + nameMaxSpace - showText.getBytesLength();
                    showText += getNameSpace(tempIndex);
                    showText += parentDeptName;
                }

                if (!excludeElements.contains(Constants_Department + dept.id) && checkIncludeElements(Constants_Department, dept.id)) {
                    if (v3x.getBrowserFlag('selectPeopleShowType')) {
                        selectHTML.append("<option value='").append(dept.id).append("' class='Department' type='Department' accountId='").append(currentVjoinAccountId).append("' title='" + showTitle + "'").append("'>").append(showText.escapeHTML(true)).append("</option>");
                    } else {
                        selectHTML.append("<div class='member-list-div' seleted='false' ondblclick='selectOneMemberDiv(this)'  onclick=\"selectMemberFn(this,'memberDataBody')\"  value='").append(dept.id).append("' class='Department' type='Department' accountId='").append(currentVjoinAccountId).append("' title='" + showTitle + "'").append("'>").append(showText.escapeHTML(true)).append("</div>");
                    }
                }
            }
        }

        if (members && checkCanSelectMember()) {
            var hasShowMembers = {};
            var member_size = members.size();
            for (var m = 0; m < member_size; m++) {
                var member = members.get(m);

                if (!member.externalType == "1" || hasShowMembers[member.id]) { //已经显示了，防止副岗兼职重复出现
                    continue;
                }
                if (member.name.toLowerCase().indexOf(keyword) < 0) {
                    continue;
                }

                if (isV5Member) {
                    if (isNeedCheckLevelScope && !checkVjoinMemberWorkScopeOfMember(member)) {
                        continue;
                    }
                } else if (isVjoinMember) {
                    if (!isAdministrator) {
                        var _AccessVjoinDepts = topWindow.AccessVjoinDepts;
                        if (_AccessVjoinDepts != null) {
                            if (!_AccessVjoinDepts.contains("D" + member.getDepartment().id)) {
                                continue;
                            }
                        }

                    }
                }

                if (!excludeElements.contains(Constants_Member + member.id) && checkIncludeElements4Member(member)) {
                    selectHTML.append(addMember(Constants_Department, department, member));
                    hasShowMembers[member.id] = "T";
                }
            }

            hasShowMembers = null;
        }
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_subfix);
        } else {
            selectHTML.append(memberDataBody_div_end);
        }

        document.getElementById("Area2").innerHTML = selectHTML.toString();
    } else if (showMode == Constants_ShowMode_LIST && nowSelectedList1Item) {
        if (!checkCanSelectMember()) {
            return;
        }

        showList2(type, nowSelectedList1Item.value, null, keyword);
    } else if (showMode == Constants_ShowMode_LIST && type == Constants_Outworker) {
        if (!checkCanSelectMember()) {
            return;
        }

        var selectHTML = new StringBuffer();
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_prefix);
        } else {
            selectHTML.append(memberDataBody_div);
        }
        //找出我能看到的外部人员
        var allExMembers = topWindow.ExtMemberScopeOfInternal.values();
        for (var i = 0; i < allExMembers.size(); i++) {
            var departMembers = allExMembers.get(i);
            for (var j = 0; j < departMembers.size(); j++) {
                var mId = departMembers.get(j);
                var member = topWindow.getObject(Constants_Member, mId);
                if (member != null && member.name.toLowerCase().indexOf(keyword) > -1) {
                    var department = member.getDepartment();
                    selectHTML.append(addMember(Constants_Department, department, member));
                }
            }
        }
        if (v3x.getBrowserFlag('selectPeopleShowType')) {
            selectHTML.append(select2_tag_subfix);
        } else {
            selectHTML.append(memberDataBody_div_end);
        }

        document.getElementById("Area2").innerHTML = selectHTML.toString();
    }
}

function searchItems3() {
    var keyword = document.getElementById("q3").value || "";

    var ops = document.getElementById("List3").options;
    for (var i = ops.length - 1; i >= 0; i--) {
        var op = ops.item(i);
        if (keyword && op.text.indexOf(keyword) >= 0) {
            op.selected = true;
        } else {
            op.selected = false;
        }
    }
}

function removeFromList3(key) {
    var ops = document.getElementById("List3").options;
    for (var i = 0; i < ops.length; i++) {
        if (ops.item(i).value == key) {
            ops.remove(i);
            break;
        }
    }
}

/**
 * Member(id, name, departmentId, postId, levelId, email, mobile, description)
 *
 * @return Array<Element>
 */
function getSelectedPeoples(_maxSize, _minSize, needlessPreReturnValueFun) {
    var _selectedPeopleElements = new ArrayList();
    var _selectedPeopleTypes = new Properties();

    var _selectedPeopleKeys = selectedPeopleElements.keys();

    for (var i = 0; i < _selectedPeopleKeys.size(); i++) {
        var key = _selectedPeopleKeys.get(i);
        if (key) {
            var value = selectedPeopleElements.get(key);
            _selectedPeopleElements.add(value);

            var type = value.type;
            if (type != Constants_Member) {
                var _indexes = _selectedPeopleTypes.get(type);
                if (_indexes == null) {
                    _indexes = new ArrayList();
                    _selectedPeopleTypes.put(type, _indexes);
                }

                _indexes.add(i);
            }
        }
    }

    _maxSize = _maxSize == null ? maxSize : _maxSize;
    _minSize = _minSize == null ? minSize : _minSize;

    var nowSize = _selectedPeopleElements.size();
    if (_maxSize > 0 && nowSize > _maxSize) {
        throw ($.i18n("selectPeople.alert_maxSize", _maxSize, nowSize));
    }

    if (_minSize > 0 && nowSize < _minSize) {
        throw ($.i18n("selectPeople.alert_minSize", _minSize, nowSize));
    }

    if (nowSize < 2) { //就一项数据比什么比嘛
        return getData();
    }

    //getIsCheckSelectedData() 是否检测被选数据的重复性，由JSP实现
    var isNotCheckDuplicateData = getParentWindowData("isNotCheckDuplicateData") || false;
    if (getIsCheckSelectedData() == false || getParentWindowData("isCheckInclusionRelations") == false || isNotCheckDuplicateData == true) {
        return getData();
    }

    if (_selectedPeopleTypes.containsKey(Constants_OrgTeam)) {
        isOrgTeamContainChildDepartment();
    }

    if (_selectedPeopleTypes.containsKey(Constants_Account) || _selectedPeopleTypes.containsKey(Constants_BusinessAccount)) {
        checkIsContainAccount();
    }

    //不允许同时选择部门和其下的子部门
    if (!getParentWindowData("isAllowContainsChildDept")) {
        if (_selectedPeopleTypes.containsKey(Constants_Department)) {
            //检查部门子部门的包含关系
            checkIsContainChildDepartment();
        }

        if (_selectedPeopleTypes.containsKey(Constants_BusinessDepartment)) {
            checkIsContainChildBusinessDepartment();
        }
    }

    var message = new ArrayList();
    var repeatingItem = new ArrayList();

    for (var i = 0; i < _selectedPeopleElements.size(); i++) {
        var element = _selectedPeopleElements.get(i);

        //检测人
        if (element.type == Constants_Member) {
            var member = topWindow.getObject(Constants_Member, element.id);

            if (member == null) { //人员可能被删除了
                continue;
            }

            //检测该人的部门是否也被选择了（包括所有上级部门）
            var departmentIndexes = _selectedPeopleTypes.get(Constants_Department);
            if (departmentIndexes && checkCanSelect(Constants_Department)) {
                for (var t = 0; t < departmentIndexes.size(); t++) {
                    var el = _selectedPeopleElements.get(departmentIndexes.get(t));
                    if (el && el.type == Constants_Department) {
                        var entity = topWindow.getObject(Constants_Department, el.id);
                        if (!entity) {
                            continue;
                        }
                        var flag = false;
                        if (el.excludeChildDepartment) {
                            flag = member.departmentId == el.id;
                        } else {
                            var members = entity.getAllMembersMap();
                            flag = members.containsKey(member.id);
                        }
                        if (flag) {
                            message.add($.i18n("selectPeople.alert_contain_member", el.name, member.name));
                            repeatingItem.add(Constants_Member + element.id);
                            break;//判断一个就够了
                        }
                    }
                }
            }

            var departmentIndexes = _selectedPeopleTypes.get(Constants_BusinessDepartment);
            if (departmentIndexes && checkCanSelect(Constants_BusinessDepartment)) {
                for (var t = 0; t < departmentIndexes.size(); t++) {
                    var el = _selectedPeopleElements.get(departmentIndexes.get(t));
                    if (el && el.type == Constants_BusinessDepartment) {
                        var entity = topWindow.getObject(Constants_BusinessDepartment, el.id);
                        if (!entity) {
                            continue;
                        }
                        var flag = false;
                        if (el.excludeChildDepartment) {
                            var members = entity.getDirectMembersMap();
                            flag = members.containsKey(member.id);
                        } else {
                            var members = entity.getAllMembersMap();
                            flag = members.containsKey(member.id);
                        }
                        if (flag) {
                            message.add($.i18n("selectPeople.alert_contain_member", el.name, member.name));
                            repeatingItem.add(Constants_Member + element.id);
                            break;//判断一个就够了
                        }
                    }
                }
            }

            //检测该人的岗位是否也被选择了
            if (_selectedPeopleTypes.containsKey(Constants_Post) && checkCanSelect(Constants_Post)) {
                el = selectedPeopleElements.get(Constants_Post + member.postId);

                if (!el && member.secondPostIds) { //副岗
                    var _secondPostIds = member.secondPostIds; //List<[Department.id, Post.id]>
                    for (var t = 0; t < _secondPostIds.size(); t++) {
                        var _secondPostId = _secondPostIds.get(t);	//[Department.id, Post.id]
                        var _postId = _secondPostId[1];

                        el = selectedPeopleElements.get(Constants_Post + _postId);
                        if (el) {
                            break;
                        }
                    }
                }
                if (!el) { //兼职
                    var _concurents = topWindow.getObject(Constants_concurentMembers, member.id); //此人在该单位的兼职
                    if (_concurents) {
                        for (var t = 0; t < _concurents.length; t++) {
                            var _concurent = _concurents[t]; //判断是否是这个岗位
                            var __concurentPost = _concurent.postId;
                            el = selectedPeopleElements.get(Constants_Post + _concurentPost);
                            if (el) {
                                break;
                            }
                        }
                    }
                }

                if (el) {
                    message.add($.i18n("selectPeople.alert_contain_member", el.name, member.name));
                    repeatingItem.add(Constants_Member + element.id);
                    /*
					if(window.confirm($.i18n("selectPeople.alert_contain_member", el.name, member.name))){
						selectedPeopleElements.remove(Constants_Member + element.id);
						removeFromList3(Constants_Member + element.id);
					}
					*/
                    continue;
                }
            }

            //检测该人的职务级别是否也被选择了
            if (_selectedPeopleTypes.containsKey(Constants_Level) && checkCanSelect(Constants_Level)) {
                el = selectedPeopleElements.get(Constants_Level + member.levelId);
                if (el) {
                    message.add($.i18n("selectPeople.alert_contain_member", el.name, member.name));
                    repeatingItem.add(Constants_Member + element.id);
                    /*
					if(window.confirm($.i18n("selectPeople.alert_contain_member", el.name, member.name))){
						selectedPeopleElements.remove(Constants_Member + element.id);
						removeFromList3(Constants_Member + element.id);
					}
					*/
                    continue;
                }
            }

            //组
            var teamIndexes = _selectedPeopleTypes.get(Constants_Team);
            if (teamIndexes && checkCanSelect(Constants_Team)) {
                var memberExists = false;
                for (var t = 0; t < teamIndexes.size(); t++) {
                    el = _selectedPeopleElements.get(teamIndexes.get(t));
                    if (el.type == Constants_Team) {
                        //var memberList = topWindow.getObject(Constants_Team, el.id).getAllMemberIds();
                        var memberList = topWindow.getObject(Constants_Team, el.id).members;
                        for (var k = 0; k < memberList.size(); k++) {
                            var teamMember = memberList.get(k);
                            var teamMemberType = teamMember.type;
                            if (teamMemberType == Constants_Member) {
                                var teamMemberId = teamMember.id.split("_")[1];
                                if (teamMemberId == memberId) {
                                    message.add($.i18n("selectPeople.alert_contain_member", el.name, member.name));
                                    repeatingItem.add(Constants_Member + element.id);
                                    memberExists = true;
                                    break;//判断一个就够了
                                }
                            }
                        }
                    }
                    if (memberExists) {
                        break;
                    }
                }
            }
        }
    }

    if (!message.isEmpty()) {
        var size = message.size();

        var messageStr = (message.subList(0, 10).toString("<br/>") + "<br/><br/>" + $.i18n("selectPeople.alert_contain_item", size));

        $.confirm({
            msg: messageStr,
            targetWindow: parent.window,
            ok_fn: function () {
                removeRepeatingItem(repeatingItem);
            }
        });

        throw "continue";
    }

    function getData() {
        var selectedElements = selectedPeopleElements.values().toArray();

        for (var i = 0; i < selectedElements.length; i++) {
            if (!checkShowAccountShortname4Element(selectedElements[i])) {
                selectedElements[i].accountShortname = null;
                if (selectedElements[i].type == Constants_WfSuperNode) {
                    var node = topWindow.getObject(Constants_WfSuperNode, selectedElements[i].id);
                    if (node && node.processMode) {
                        selectedElements[i]["processMode"] = node.processMode;
                        selectedElements[i]["nodeDesc"] = node.nodeDesc;
                        selectedElements[i]["interveneMemberId"] = node.interveneMemberId;
                        selectedElements[i]["stepBackType"] = node.stepBackType;
                        selectedElements[i]["tolerantModel"] = node.tolerantModel;
                    }
                }
            }
        }

        if (needlessPreReturnValueFun != false) {
            var _preReturnValueFun = getParentWindowData("preReturnValueFun");
            if (_preReturnValueFun) {
                var preSelectedElements = new Array();
                for (var i = 0; i < selectedElements.length; i++) {
                    var el = selectedElements[i];
                    preSelectedElements[i] = new Element();
                    preSelectedElements[i].copy(el);
                    preSelectedElements[i].entity = topWindow.getObject(el.type, el.id);
                }

                try {
                    var preResult = null;
                    eval("preResult = parentWindow." + _preReturnValueFun + "(preSelectedElements)");
                    if (preResult && preResult.length == 2 && preResult[0] == false) {
                        throw preResult[1];
                    }
                } catch (e) {
                    throw e;
                }
            }
        }

        return selectedElements;
    }

    return getData();
}

/**
 * 检测已选择的数据中是否包含单位
 */
function checkIsContainAccount() {
    if ((!checkCanSelect(Constants_Account) && !checkCanSelect(Constants_BusinessAccount)) || selectedPeopleElements.size() < 2) {
        return;
    }

    var _selectedPeopleElements = selectedPeopleElements.values();

    var message = new ArrayList();
    var repeatingItem = new ArrayList();

    for (var i = 0; i < _selectedPeopleElements.size(); i++) {
        var element = _selectedPeopleElements.get(i);
        if (element.id == "BlankNode") {
            continue;
        }
        if (element.type == Constants_Account) {
            for (var k = 0; k < _selectedPeopleElements.size(); k++) {
                var el = _selectedPeopleElements.get(k);
                //AEIGHT-9496 20130606 lilong 客户BUG修改，tanmf同意修改
                //OA-35642 同样此问题，选了单位根不能其他的组
                if (el.type == Constants_Team || el.type == Constants_JoinAccountTag || el.type == Constants_MemberMetadataTag || el.type == Constants_Guest || el.type == Constants_BusinessAccount || el.type == Constants_BusinessDepartment) {
                    continue;
                }
                if ((el.type != Constants_Account && el.accountId == element.id) || (element.id == rootAccount.id && el.id != rootAccount.id)) {
                    var obj = topWindow.getObject(el.type, el.id);
                    if (obj && (obj.isInternal == false || (obj.externalType && obj.externalType != '0'))) {
                        if (obj.externalType && obj.externalType != '0') {
                            if (element.id == "-1730833917365171641") {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    if (el.id != "BlankNode") {
                        message.add($.i18n("selectPeople.alert_contain_member", element.name, escapeStringToHTML(el.name)));
                        repeatingItem.add(el.type + el.id);
                    }
                }
            }
        } else if (element.type == Constants_BusinessAccount) {//校验多维组织 和多维组织部门的包含关系
            var businessAccount = topWindow.getObject(Constants_BusinessAccount, element.id);
            for (var k = 0; k < _selectedPeopleElements.size(); k++) {
                var el = _selectedPeopleElements.get(k);
                var obj = topWindow.getObject(el.type, el.id);
                if (obj && obj.businessId) {
                    if (el.type == Constants_BusinessDepartment && obj.businessId == businessAccount.id) {//校验多维组织根 和 多维组织部门的包含关系
                        if (el.id != "BlankNode") {
                            message.add($.i18n("selectPeople.alert_contain_member", element.name, escapeStringToHTML(el.name)));
                            repeatingItem.add(el.type + el.id);
                        }
                    } else if (el.type == Constants_Member) {//校验多维组织根  和 人员的包含关系
                        if (businessAccount) {
                            var accessMemberIds = businessAccount.accessMemberIds;
                            if (accessMemberIds.indexOf(el.id) >= 0) {
                                if (el.id != "BlankNode") {
                                    message.add($.i18n("selectPeople.alert_contain_member", element.name, escapeStringToHTML(el.name)));
                                    repeatingItem.add(el.type + el.id);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (!message.isEmpty()) {
        var size = message.size();

        var messageStr = (message.subList(0, 10).toString("<br/>") + "<br/><br/>" + $.i18n("selectPeople.alert_contain_item", size));

        $.confirm({
            msg: messageStr,
            targetWindow: parent.window,
            ok_fn: function () {
                removeRepeatingItem(repeatingItem);
            }
        });

        throw "continue";
    }
}


/**
 * 检测是否包含子部门
 */
function checkIsContainChildDepartment() {
    if (!checkCanSelect(Constants_Department) || selectedPeopleElements.size() < 2) {
        return;
    }

    var message = new ArrayList();
    var repeatingItem = new ArrayList();

    var _selectedPeopleElements = selectedPeopleElements.values();
    for (var i = 0; i < _selectedPeopleElements.size(); i++) {
        var element = _selectedPeopleElements.get(i);

        // 数据中心不存在该单位的数据，说明没有改变
        if (!topWindow.dataCenter.containsKey(element.accountId)) {
            continue;
        }

        if (element.type == Constants_Department) {
            var obj = topWindow.getObject(element.type, element.id);
            if (!obj || obj.isInternal == false) {
                if (obj.externalType != null && obj.externalType != '0') {
                    //vjoin 的数据
                } else {
                    continue;
                }
            }
            var acId = obj.accountId;
            if (obj.accountId == null) {
                acId = element.accountId;
            }
            var allParents = topWindow.findMultiParent(topWindow.getDataCenter(Constants_Department, acId), element.id);
            if (!allParents || allParents.isEmpty()) {
                continue;
            }

            for (var k = 0; k < allParents.size(); k++) {
                var entity = allParents.get(k);
                if (!entity || entity.id == element.id) {
                    continue;
                }

                var ancestor = selectedPeopleElements.get(Constants_Department + entity.id);
                // 选中其祖先且其祖先包含子部门
                if (ancestor && !ancestor.excludeChildDepartment) {
                    message.add($.i18n("selectPeople.alert_contain_member", entity.name, element.name));
                    repeatingItem.add(Constants_Department + element.id);

                    /*
					if(window.confirm($.i18n("selectPeople.alert_contain_member", entity.name, element.name))){
						selectedPeopleElements.remove(Constants_Department + element.id);
						removeFromList3(Constants_Department + element.id);
					}
					*/

                    continue;
                }
            }
        }
    }

    if (!message.isEmpty()) {
        var size = message.size();

        var messageStr = (message.subList(0, 10).toString("<br/>") + "<br/><br/>" + $.i18n("selectPeople.alert_contain_item", size));

        $.confirm({
            msg: messageStr,
            targetWindow: parent.window,
            ok_fn: function () {
                removeRepeatingItem(repeatingItem);
            }
        });

        throw "continue";
    }
}

/**
 * 检测是否包含业务子部门
 */
function checkIsContainChildBusinessDepartment() {
    if (!checkCanSelect(Constants_BusinessDepartment) || selectedPeopleElements.size() < 2) {
        return;
    }

    var message = new ArrayList();
    var repeatingItem = new ArrayList();

    var _selectedPeopleElements = selectedPeopleElements.values();
    for (var i = 0; i < _selectedPeopleElements.size(); i++) {
        var element = _selectedPeopleElements.get(i);

        // 数据中心不存在该单位的数据，说明没有改变
        if (!topWindow.dataCenter.containsKey(element.accountId)) {
            continue;
        }

        if (element.type == Constants_BusinessDepartment) {
            var obj = topWindow.getObject(element.type, element.id);
            var acId = obj.accountId;
            if (obj.accountId == null) {
                acId = element.accountId;
            }
            var allParents = topWindow.findMultiParent(topWindow.getDataCenter(Constants_BusinessDepartment, acId), element.id);
            if (!allParents || allParents.isEmpty()) {
                continue;
            }

            for (var k = 0; k < allParents.size(); k++) {
                var entity = allParents.get(k);
                if (!entity || entity.id == element.id) {
                    continue;
                }

                var ancestor = selectedPeopleElements.get(Constants_BusinessDepartment + entity.id);
                // 选中其祖先且其祖先包含子部门
                if (ancestor && !ancestor.excludeChildDepartment) {
                    message.add($.i18n("selectPeople.alert_contain_member", entity.name, element.name));
                    repeatingItem.add(Constants_BusinessDepartment + element.id);
                    continue;
                }
            }
        }
    }

    if (!message.isEmpty()) {
        var size = message.size();

        var messageStr = (message.subList(0, 10).toString("<br/>") + "<br/><br/>" + $.i18n("selectPeople.alert_contain_item", size));

        $.confirm({
            msg: messageStr,
            targetWindow: parent.window,
            ok_fn: function () {
                removeRepeatingItem(repeatingItem);
            }
        });

        throw "continue";
    }
}

function removeRepeatingItem(repeatingItem) {
    if (!repeatingItem) {
        return;
    }

    for (var i = 0; i < repeatingItem.size(); i++) {
        var key = repeatingItem.get(i);

        selectedPeopleElements.remove(key);
        removeFromList3(key);
    }
}

/**
 *校验机构组是否包含部门，包含剔除重复的部门
 * */
function isOrgTeamContainChildDepartment() {
    var message = new ArrayList();
    var repeatingItem = new ArrayList();

    var _selectedOrgTeam = selectedPeopleElements.values();
    for (var i = 0; i < _selectedOrgTeam.size(); i++) {
        var orgTeam = _selectedOrgTeam.get(i);
        var type = orgTeam.type;
        if (type == Constants_OrgTeam) {
            var _orgTeam = topWindow.getObject(Constants_OrgTeam, orgTeam.id);
            if (!_orgTeam) {
                continue;
            }
            var depts = _orgTeam.getOrgTeamDepartment();
            for (var j = 0; j < depts.size(); j++) {
                var dept = depts.get(j);
                var type = dept.type
                var deptidstr = dept.id;
                var deptId = deptidstr;
                if (deptId.indexOf("_") >= 0) {
                    var index0 = deptidstr.indexOf("_");
                    deptId = deptidstr.substr(index0 + 1);
                    var key = Constants_Department + deptId
                    orgTeamDepartment.put(key, _orgTeam.name);
                }
            }
        }
    }

    for (var i = 0; i < _selectedOrgTeam.size(); i++) {
        var orgTeamDept = _selectedOrgTeam.get(i);
        var type = orgTeamDept.type;
        var deptId = orgTeamDept.id;
        var key = Constants_Department + deptId;
        if (type == Constants_Department) {
            if (orgTeamDepartment.containsKey(key)) {
                message.add($.i18n("selectPeople.alert_contain_member", orgTeamDepartment.get(key), orgTeamDept.name));
                repeatingItem.add(Constants_Department + deptId);
                continue;

            }
        }
    }

    if (!message.isEmpty()) {
        var size = message.size();

        var messageStr = (message.subList(0, 10).toString("<br/>") + "<br/><br/>" + $.i18n("selectPeople.alert_contain_item", size));

        $.confirm({
            msg: messageStr,
            targetWindow: parent.window,
            ok_fn: function () {
                removeRepeatingItem(repeatingItem);
            }
        });

        throw "continue";
    }
}

/**移除已经包含的部门**/
function removeRepeatingOrgTeamDept(repeatingItem) {
    if (!repeatingItem) {
        return;
    }

    for (var i = 0; i < repeatingItem.size(); i++) {
        var key = repeatingItem.get(i);

        selectedPeopleElements.remove(key);
        removeFromList3(key);
    }
}


/**
 * 回显原来的选人数据
 * 在父窗口的elements_${id}中
 */
function initOriginalData() {
    if (getParentWindowData("showOriginalElement") == false) {
        return;
    }

    var originalDataValue;
    var originalDataElements;
    var originalDataText;
    if (null != getParentWindowData("params")) {
        //Post|-673337325302758626,Post|-5908430834075939152,Post|-6224392101839417413,Post|-2371790963429915424
        originalDataValue = getParentWindowData("params").data || getParentWindowData("params").value;
        originalDataText = getParentWindowData("params").text;
        originalDataElements = getParentWindowData("elements");
    }

    if (!originalDataValue && !originalDataElements) {
        return;
    }

    var elements = null;
    //该方法有具体jsp实现
    if (originalDataElements) {
        elements = originalDataElements;
    } else {
        elements = [];

        var enteries = originalDataValue.split(",");
        var textenteries = null;
        if (originalDataText != null && originalDataText != undefined) {
            textenteries = originalDataText.toString().replace(new RegExp("、", 'g'), ",").split(",");
        }

        var originalDataValue0 = null;
        var enteriesMap = {};
        if (enteries.length > 0 && enteries[0]) {
            for (var i = 0; i < enteries.length; i++) {
                if (enteries[i].split("|").length < 3) {
                    if (originalDataValue0 == null) {
                        originalDataValue0 = enteries[i];
                    } else {
                        originalDataValue0 = originalDataValue0 + "," + enteries[i];
                    }
                }
            }
        }

        if (originalDataValue0 != null) {
            var spm = new selectPeopleManager();
            originalDataValue0 = spm.parseElements(originalDataValue0);
            var enteries0 = originalDataValue0.substring("[WLCCYBD-V5]".length).split(",");
            for (var i = 0; i < enteries0.length; i++) {
                if (!enteries0[i]) {
                    continue;
                }
                var e = enteries0[i].split("|");
                var id = e[1];
                enteriesMap[id] = e;
            }
        }

        for (var i = 0; i < enteries.length; i++) {
            if (!enteries[i]) {
                continue;
            }

            var e = enteries[i].split("|");
            if (!e) {
                continue;
            }

            //function Element(type, id, name, typeName, accountId, accountShortname, description)
            var type = e[0];
            var id = e[1];
            if (!type || !id) {
                continue;
            }
            if (enteriesMap.hasOwnProperty(id)) {
                e = enteriesMap[id];
            }
            var name = null;
            var accountId = null;
            var isEnabled = true;
            var isexChild = false;

            var entity = topWindow.getObject(type, id);
            if (e.length > 3) {
                if (type == "FormField") {//OA-50418 表单控件回填特殊处理
                    if (e[1].indexOf("#") == e[1].lastIndexOf("#")) {
                        name = e[2].substring(e[2].indexOf("#"));
                    } else {
                        if (null != textenteries && textenteries.length > i) {
                            name = textenteries[i];
                        } else {
                            name = originalDataText;
                        }
                    }
                } else if (type.indexOf("BusinessDepartment") != -1 || type.indexOf("BusinessRole") != -1) {
                    if (!entity) {
                        topWindow.initOrgModel(e[3], currentMemberId, extParameters);
                        if (type.indexOf(valuesJoinSep) > 0) {
                            var types = type.split(valuesJoinSep);
                            var ids = id.split(valuesJoinSep);
                            entity = topWindow.getObject(types[0], ids[0], e[3]);
                        } else {
                            entity = topWindow.getObject(type, id, e[3]);
                        }
                    }
                    if (entity) {
                        name = entity.preShow + "-" + e[2];
                    } else {
                        name = e[2];
                    }
                } else {
                    name = e[2];
                }
                accountId = e[3];

                if (e.length > 4) {
                    isEnabled = (e[4] == "true");
                }
                if (e.length > 5) {
                    isexChild = (e[5] == "1");
                    if (isexChild) {
                        name = name + "(" + $.i18n("selectPeople.excludeChildDepartment") + ")";
                    }
                }
            } else {
                if (entity) {
                    if (type == "BusinessDepartment") {
                        name = entity.preShow + "-" + entity.name;
                    } else {
                        name = entity.name;
                    }
                    if ((type == Constants_Department || type == Constants_BusinessDepartment) && e.length > 2 && e[2] == 1) {
                        isexChild = true;
                        name = name + "(" + $.i18n("selectPeople.excludeChildDepartment") + ")";
                    }
                    accountId = entity.accountId;
                } else {
                    name = getName(type, id);
                    if (type == "Node" && e.length > 2 && e[2] == 1) {
                        isexChild = true;
                        name = name + "(" + $.i18n("selectPeople.excludeChildDepartment") + ")";
                    }
                }
            }

            var ele = new Element(type, id, name, null, accountId, null, '');
            ele.isEnabled = isEnabled;
            if (entity) {
                ele.externalType = entity.externalType;
            }
            if (isexChild) {
                ele.excludeChildDepartment = isexChild;
            }
            elements[elements.length] = ele;
        }
    }

    if (!elements || elements.length < 1) {
        return;
    }

    var disabledE = new ArrayList();
    var _toAccount = new ArrayList();

    aaa:
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            if (element == null || element.name == "") continue;

            var _entity = topWindow.getObject(element.type, element.id);
            if (_entity && _entity.externalType && _entity.externalType != "0") {
                element.externalType = _entity.externalType;
            }

            if (element.type == Constants_Node || element.type == Constants_WfSuperNode || element.type == Constants_Account
                || (topWindow.Constants_Custom_Panels.keys() != null && topWindow.Constants_Custom_Panels.keys().contains(element.type))) { //如果是这些类型，不校验数据有效性

            } else if (element.type == Constants_Guest && element.id == "-6964000252392685202") {//系统登录前guest账号
                var defaultGuest = topWindow.getObject(Constants_Guest, "-6964000252392685202");
                if (defaultGuest == null || defaultGuest == undefined || !defaultGuest.enable) {
                    disabledE.add(element.name);
                    continue aaa;
                }
            } else if (groupAdmin && element.accountId != "-1730833917365171641") {//如果是集团管理员，不校验单位的数据，单位的其他数据太多了，每次都要再加载一遍单位的数据，有性能问题
                var _accountId = element.accountId;
                var account = allAccounts.get(_accountId);
                if (account) {
                    element.accountShortname = account.shortname;
                }
            } else {
                var _accountId = element.accountId;
                var account = allAccounts.get(_accountId);
                if (account) {
                    element.accountShortname = account.shortname;

                    //加载单位在数据
                    if (!_toAccount.contains(_accountId)) {
                        _toAccount.add(_accountId);
                        if (_accountId != currentAccountId) {
                            topWindow.initOrgModel(_accountId, currentMemberId, extParameters);
                        }
                    }
                }

                if (_accountId != currentAccountId && onlyLoginAccount && element.type != Constants_Member) {
                    if ((element.externalType && element.externalType != "0") || element.type == Constants_JoinAccountTag || element.type == Constants_MemberMetadataTag) {
                        //vjoin 数据回填
                    } else if (element.type == Constants_Team && element.accountId == "-1730833917365171641") {
                        //系统组
                    } else {
                        if (element.name && element.name != "") {
                            disabledE.add(element.name);
                        }

                        continue aaa;
                    }
                }

                if (element.isEnabled == false) {
                    if (element.name && element.name != "") {
                        disabledE.add(element.name);
                    }
                    continue aaa;
                } else {
                    var types = element.type.split(valuesJoinSep);
                    var ids = element.id.split(valuesJoinSep);

                    for (var k = 0; k < types.length; k++) {
                        if (types[k] != Constants_FormField) {
                            var __element = topWindow.getObject(types[k], ids[k]);
                            if (__element == null || __element.isEnabled == false) {
                                if (element.name && element.name != "") {
                                    disabledE.add(element.name);
                                }
                                continue aaa;
                            }
                        }
                    }

                    element.description = getFullNameStr(element.type, element.id);
                }
            }

            var key = element.type + element.id;

            if (element.type == Constants_Department) {
                var _entity = topWindow.getObject(element.type, element.id);
                if (_entity != null && _entity != undefined) {
                    if (_entity.getFullName) {
                        element.description = _entity.getFullName();
                    }
                }
            }

            add2List3(element);
            selectedPeopleElements.put(key, element);
        }

    if (!disabledE.isEmpty()) {
        alert($.i18n("selectPeople.disabledE", disabledE));
    }

    if (!_toAccount.isEmpty()) {
        topWindow.initOrgModel(currentAccountId, currentMemberId, extParameters);
    }
}


var showAccountShortnameFalg = getParentWindowData("showAccountShortname");

/**
 * 指定的类型是否需要显示类型<br>
 * 外部单位、单位、管理员、表单控制、动态角色 不显示单位简称。
 * @param element
 * @return true：要显示，false：不显示
 */
function checkShowAccountShortname4Element(element) {
    if (showAccountShortnameFalg == "yes") {
        return true;
    } else if (showAccountShortnameFalg == "no") {
        return false;
    }

    var type = element.type;
    if (type == Constants_ExchangeAccount || type == Constants_Account || type == Constants_Admin || type == Constants_FormField || type == Constants_Node || type == Constants_WfSuperNode) {
        return false;
    }

    if (loginAccountId != element.accountId) {
        return true;
    }

    return false;
}

/**
 * 从主窗口取到排除数据
 */
function initExcludeElements() {
    try {
        var originalElement = getParentWindowData("excludeElements");

        if (!originalElement) {
            return;
        }

        if ((typeof originalElement) == "string") {
            var enteries = originalElement.split(",");
            for (var i = 0; i < enteries.length; i++) {
                if (!enteries[i]) {
                    continue;
                }

                var e = enteries[i].split("|");
                excludeElements.add(e[0] + e[1]);
            }
        } else {
            for (var i = 0; i < originalElement.length; i++) {
                excludeElements.add(originalElement[i].type + originalElement[i].id);
            }
        }
    } catch (e) {
    }
}

function initIncludeElements() {
    try {
        var originalElement = getParentWindowData("includeElements");

        if (!originalElement) {
            return;
        }

        includeElements = new ArrayList();

        if ((typeof originalElement) == "string") {
            var enteries = originalElement.split(",");
            for (var i = 0; i < enteries.length; i++) {
                if (!enteries[i]) {
                    continue;
                }

                var e = enteries[i].split("|");
                includeElements.add(e[0] + e[1]);

                AddChildDept(e[0], e[1]);
            }
        } else {
            for (var i = 0; i < originalElement.length; i++) {
                includeElements.add(originalElement[i].type + originalElement[i].id);

                AddChildDept(originalElement[i].type, originalElement[i].id);
            }
        }
    } catch (e) {
        //ignore
    }

    function AddChildDept(type, id) {
        if (type == Constants_Department) {
            var d = topWindow.getObject(Constants_Department, id);
            if (d) {
                var cs = d.getAllChildren();
                if (cs) {
                    for (var j = 0; j < cs.size(); j++) {
                        includeElements.add(Constants_Department + cs.get(j).id);
                    }
                }
            }
        }
    }
}

/**
 * 检查是否要在备选栏目中显示
 *
 * @param type
 * @param id
 * @return true显示，false不在备选中显示
 */
function checkIncludeElements(type, id) {
    if (includeElements == null || includeElements.isEmpty()) { //没有指定
        return true;
    }

    if (type == Constants_Outworker) {
        type = Constants_Department;
    }

    /*
     *单位、集团在备选范围,那下面的部门和人员能选择
     */
    if (type != Constants_Account) {
        if (includeElements.contains(Constants_Account + currentAccountId) || (includeElements.contains(Constants_Account + rootAccount.id) && accessableAccounts.containsKey(rootAccount.id))) {
            return true;
        }
    }

    if (type == Constants_Account && includeElements.contains(Constants_Account + rootAccount.id) && accessableAccounts.containsKey(rootAccount.id)) {
        var path = allAccounts.get(id).path;
        if (path.startsWith(rootAccount.path)) {
            return true;
        }
    }

    return includeElements.contains(type + id);
}

function checkIncludeElements4Member(member) {
    if (includeElements == null || includeElements.isEmpty()) { //没有指定
        return true;
    }

    if (includeElements.contains(Constants_Department + member.departmentId) || includeElements.contains(Constants_Member + member.id)) {
        return true;
    }

    /*
     *单位、集团在备选范围,那下面的部门和人员能选择
     */
    if (includeElements.contains(Constants_Account + member.accountId) || (includeElements.contains(Constants_Account + rootAccount.id) && accessableAccounts.containsKey(rootAccount.id))) {
        return true;
    }

    var secondDepartIds = member.getSecondDepartmentIds();
    if (secondDepartIds) {
        for (var i = 0; i < secondDepartIds.size(); i++) {
            if (includeElements.contains(Constants_Department + secondDepartIds.get(i))) {
                return true;
            }
        }
    }

    return false;
}

/**
 * 取得主窗口的数据
 */
function getParentWindowData(_name, defaultValue) {
    try {
        if (!parentWindow) {
            return;
        }

        var data = parentWindowData[_name];

        return data == null || data == undefined ? defaultValue : data;
    } catch (e) {
        return defaultValue;
    }
}

function showQueryInput() {
    return true;
}

function showQueryInputOfDepartOrTerm() {
    return checkCanSelectMember();
}

function showQueryInputOfDepart() {
//	if(getParentWindowData("showDepartmentMember4Search")){
//		return true;
//	}
//	else{
//		return selectTypes.contains(Constants_Member) || selectTypes.contains(Constants_Post);
//	}

    return true;
}

/**
 * 另存为组
 */
var saveAsTeamData = null;

function saveAsTeam() {
    try {
        saveAsTeamData = getSelectedPeoples(500, 2, false);
    } catch (e) {
        if (e != 'continue') {
            $.alert({
                msg: e,
                targetWindow: window
            });
        }
        return;
    }

    for (var i = 0; i < saveAsTeamData.length; i++) {
        var m = saveAsTeamData[i];
        if (m.type != Constants_Member) {
            $.alert($.i18n("selectPeople.saveAsTeam_alert_OnlnMember"));
            return;
        } else {
            var mm = topWindow.getObject(Constants_Member, m.id);
            if (mm.externalType == "1") {
                $.alert("组中不能包含外部机构人员!");
                return;
            }
        }
    }

    var saveAsTeamData0 = saveAsTeamData;
    var dialog = $.dialog({
        id: "saveAsTeamDialog",
        url: _ctxPath + "/selectpeople.do?method=saveAsTeam",
        width: 360,
        height: 140,
        title: $.i18n("selectPeople.saveAsTeam.lable"),
        isDrag: false,
        targetWindow: window,
        panelParam: {'show': false},
        maxParam: {'show': false},
        minParam: {'show': false},
        closeParam: {
            'show': false, handler: function () {
                window.close();
            }
        },
        transParams: {
            "memberIds": getIdsString(saveAsTeamData, false),
            "memberNames": getNamesString(saveAsTeamData, $.i18n("common.separator.label"))
        },
        buttons: [
            {
                text: $.i18n('common.button.ok.label'),
                isEmphasize: true,
                handler: function () {
                    var result = dialog.getReturnValue();
                    if (result != -1) {
                        var teamId = result.TeamId;
                        var teamName = result.TeamName;
                        addPersonalTeam0(teamId, teamName, saveAsTeamData0);
                        topWindow.initOrgModel(currentAccountId, currentMemberId, extParameters);
                        dialog.close();
                    }
                }
            },
            {
                text: $.i18n('common.button.cancel.label'),
                handler: function () {
                    dialog.close();
                }
            }
        ]
    });

    saveAsTeamData = null;
}

/**
 * 添加个人组
 * @param memberIds 逗号分隔的人员id
 */
function addPersonalTeam0(teamId, teamName, members) {
    if (members == null) {
        return;
    }

    topWindow.addPersonalTeam(loginAccountId, teamId, teamName, members);

    if (tempNowPanel.type == Constants_Team) {
        initList(Constants_Team);
    }
}

function showDetailPost() {
    var postDataBodyObj = document.getElementById("PostDataBody");
    if (!postDataBodyObj || !postDataBodyObj.value) {
        return;
    }

    var dialog = $.dialog({
        url: _ctxPath + "/selectpeople.do?method=showDetailPost",
        width: 530,
        height: 330,
        title: $.i18n("selectPeople.page.title"),
        isDrag: false,
        targetWindow: window,
        panelParam: {'show': false},
        maxParam: {'show': false},
        minParam: {'show': false},
        closeParam: {
            'show': true, handler: function () {
                window.close();
            }
        },
        transParams: {
            _window: window
        },
        buttons: [
            {
                isEmphasize: true,
                text: $.i18n('common.button.ok.label'),
                handler: function () {
                    var result = dialog.getReturnValue();
                    if (result != -1) {
                        cb(result);
                        dialog.close();
                    }
                }
            },
            {
                text: $.i18n('common.button.cancel.label'),
                handler: function () {
                    dialog.close();
                }
            }
        ]

    });

    function cb(result) {
        tempNowSelected.clear();

        for (var i = 0; i < result.length; i++) {
            var a = result[i];
            var e = new Element(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
            tempNowSelected.add(e);
        }

        selectOne();
    }
}

/************************************div实现function****************************************/
//单击组、岗位显示该组人员列表
function selectList1ItemDiv(type, objId, objTD) {
    tempNowSelected.clear();

    var ops = document.getElementById(objId).childNodes;
    for (var i = 0; i < ops.length; i++) {
        var option = ops[i];
        option.setAttribute('seleted', 'false');
        option.setAttribute('class', 'member-list-div');
    }
    objTD.setAttribute('seleted', 'true');
    objTD.setAttribute('class', 'member-list-div-select');
    var count = 0;
    var e = getElementFromOption(objTD);
    if (e) {
        tempNowSelected.add(e);
        count++;
    }
    if (count == 1 && tempNowPanel.isShowMember == true) {
        var id = objTD.getAttribute('value');
        showList2(type, id);
    }

    if (nowSelectedList1Item != null) {
        nowSelectedList1Item = null;
    }

    nowSelectedList1Item = objTD;
}

//单击人员列表改变背景 设置selectd 属性
function selectMemberFn(obj, temp_Id) {
    if (!obj) {
        return;
    }
    var seleted = obj.getAttribute('seleted');
    if (seleted == 'false') {
        obj.setAttribute('seleted', 'true');
        obj.setAttribute('class', 'member-list-div-select');
    } else {
        obj.setAttribute('seleted', 'false');
        obj.setAttribute('class', 'member-list-div');
    }
    if (temp_Id) {
        temp_Div = temp_Id;
    }
}

//双击人员列表设置selected 属性 选择 人员
function selectOneMemberDiv(selectObj) {
    if (!selectObj) {
        return;
    }
    selectObj.setAttribute('seleted', 'true');
    var element = getElementFromOption(selectObj);
    if (element) {
        tempNowSelected.clear();
        tempNowSelected.add(element);

        selectOne();
    }
}

//选中多个人员 一起选择过去
function listenermemberDataBodyDiv(object) {
    if (object == null) {
        return;
    }
    tempNowSelected.clear();
    var ops = object.childNodes;
    for (var i = 0; i < ops.length; i++) {
        var option = ops[i];
        if (option) {
            if (option.getAttribute('seleted') == 'true') {
                var e = getElementFromOption(option);
                if (e) {
                    tempNowSelected.add(e);
                }
                option.parentNode.removeChild(option);
                i--;
            }
        }
    }
}

function showSearchButton3(isShow) {
    if (isShow) {
        $("#SearchButton3_1").removeClass("hidden");
        $("#SearchButton3_2").addClass("hidden");
        $("#q3").focus();
    } else if (!isShow && !$("#q3").val()) {
        $("#SearchButton3_1").addClass("hidden");
        $("#SearchButton3_2").removeClass("hidden");
    }
}

function isChildDeptOfCurrent(currentMember, departid) {
    var isTrue = false;
    if (currentMember) {
        var childs = currentMember.getDepartment().getAllChildren();
        for (var index = 0; index < childs.size(); index++) {
            if (childs.get(index).id == departid) {
                isTrue = true;
                break;
            }
        }
    }
    return isTrue;
}

function childDeptOfCurrent(currentMember) {
    var childDepts = [];
    if (currentMember) {
        var childs = currentMember.getDepartment().getAllChildren();
        for (var index = 0; index < childs.size(); index++) {
            childDepts.push(childs.get(index).id);
        }
    }
    return childDepts;
}

function showDepart(fullDepartment) {
    var showName = "";
    if (fullDepartment) {
        var departNames = fullDepartment.split("/").slice(-3);
        var length = departNames.length;
        for (var i = 0; i < length; i++) {
            showName = showName + departNames[i] + "/";
        }
        if (showName && showName !== "" && showName.length > 0) {
            return showName.substring(0, showName.length - 1);
        } else {
            return "";
        }
    }
}
