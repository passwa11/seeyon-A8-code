<%--
 $Author: lilong $
 $Rev: 4423 $
 $Date:: 2012-09-24 18:13:06#$:
  
 Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 This software is the proprietary information of Seeyon, Inc.
 Use is subject to license terms.
--%>
<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<!DOCTYPE html>
<script type="text/javascript">
    var allowGroupEditAccount = ${ctp:getSystemSwitch('allow_group_edit_account') == 'enable'};
    var aManager = new accountManager();
    var oManager = new orgManager();
    //var defaultTreeNode = "${selectTreeNode}";//默认的选中节点：集团或者是单位管理虚拟节点
    var selectTreeNode = "-1";
    var currentAccountId = "${currentAccountId}";
    var parentAccountId = "${parentAccountId}";
    var managementSubunit = "${managementSubunit}";

    var isConti = false;
    $().ready(function () {
        $.post("/seeyon/ext/Zmember.do?method=getPeopleOfUnitCount", null, function (data) {
            console.log(data);
            var count=data.total;
            $("#zpeople").html(count);
        });
        $("#isLdapEnabled").val(aManager.isEnableLdap());
        $("#unitTable").ajaxgrid({
            gridType: 'autoGrid',
            colModel: [{
                display: 'id',
                name: 'id',
                width: 'smallest',
                align: 'center',
                type: 'checkbox'
            },
                {
                    display: "${ctp:i18n('org.account_form.name.label')}",
                    name: 'name',
                    sortable: true,
                    width: 'medium'
                },
                {
                    display: "${ctp:i18n('org.account_form.sortId.label')}",
                    name: 'sortId',
                    sortType: 'number',
                    sortable: true,
                    width: 'small'
                },
                {
                    display: "${ctp:i18n('org.account_form.shortname.label')}",
                    name: 'shortName',
                    sortable: true,
                    width: 'medium'
                },
                {
                    display: "${ctp:i18n('org.account_form.superior.label')}",
                    name: 'superiorName',
                    sortable: true,
                    width: 'medium'
                },
                {
                    display: "${ctp:i18n('org.account_form.code.label')}",
                    name: 'code',
                    sortable: true,
                    width: 'small'
                },
                {
                    display: "${ctp:i18n('org.account_form.adminName.label')}",
                    name: 'loginName',
                    sortable: true,
                    width: 'small'
                },
                {
                    display: "${ctp:i18n('common.state.label')}",
                    name: 'enabled',
                    codecfg: "codeId:'common.enabled'",
                    sortable: true,
                    width: 'small'
                }],
            managerName: "accountManager",
            managerMethod: "showAccounts",
            parentId: $(".layout_center").eq(0).attr("id"),
            vChange: true,
            vChangeParam: {
                overflow: "hidden",
                autoResize: false
            },
            showTableToggleBtn: false,
            resizable: false,
            dblclick: dblclk,
            render: rend,
            //click: clickGrid,
            slideToggleBtn: false
        });


        function rend(txt, data, r, c) {
            if (c == 1) {
                return '<a href="javascript:viewByName(\'' + data.id + '\')" id="panle' + r + '">' + txt + '</a>';
            } else return txt;
        }

        //表格加载
        //var o = new Object();
        //$("#unitTable").ajaxgridLoad(o);
        //机构树
        $("#unitTree").tree({
            idKey: "id",
            pIdKey: "parentId",
            nameKey: "name",
            onClick: clk,
            onDblClick: treeDBClk,
            enableAsync: true,
            managerName: "accountManager",
            managerMethod: "showAccountTree",
            onAsyncSuccess: treeSuccess,
            nodeHandler: function (n) {
                if (n.data.parentId == '-1' || (managementSubunit == 'true' && n.data.id == currentAccountId)) {
                    n.open = true;
                } else {
                    n.open = false;
                }

            }
        });
        $("#unitTree").treeObj().reAsyncChildNodes(null, "refresh", false);

        function treeSuccess() {
            var id = selectTreeNode;
            if (managementSubunit != true && managementSubunit != 'true') {//集团管理员首次进去
                if (${param.from != "groupControl"} &&
                id === "-1"
            )
                {
                    id = "-1730833917365171641";
                }
            } else {
                id = parentAccountId;
            }

            var selectNode = $("#unitTree").treeObj().getNodeByParam("id", id);
            if (selectNode) {
                $("#unitTree").treeObj().selectNode(selectNode);
                if (isConti) {
                    var aDetail;
                    if (id === "-1") {
                        id = "-1730833917365171641";
                    }
                    if (managementSubunit == 'true') {
                        if (selectTreeNode == '-1') {
                            selectTreeNode = parentAccountId;
                            id = parentAccountId;
                        }
                        selectNode = $("#unitTree").treeObj().getNodeByParam("id", id);
                        $("#unitTree").treeObj().selectNode(selectNode);
                        if (selectTreeNode == parentAccountId || selectTreeNode == currentAccountId) {
                            id = currentAccountId;
                        }
                    }
                    var aDetail = aManager.viewAccount(id);
                    $("tr[class='nonGroup']").show();
                    $("tr[class='cGroup']").hide();
                    $("tr[class='cGroup']").disable();
                    $("tr[class='nonGroup']").enable();
                    $("input[name='permissionType']").get(0).checked = true;
                    disRange0();
                    if (aDetail.group === 'true') {
                        showSizeDiv("group", aDetail);
                    } else if ((managementSubunit == true || managementSubunit == 'true') && selectTreeNode == parentAccountId) {
                        showSizeDiv("managementSubunit", aDetail);
                    } else {
                        showSizeDiv("account", aDetail);
                    }

                } else {
                    clk('', '', selectNode);
                }
            }
        }

        //表格单击事件
        function clickGrid(data, r, c) {
            clk('', '', data);
        }

        //机构树上的单点击事件
        function clk(e, treeId, node) {
            if (node.id === '-1') { //点击单位总数显示列表
                selectTreeNode = "-1";
                $("#table_area").show();
                $("#form_area").hide();
                var o = new Object();
                $("#unitTable").ajaxgridLoad(o);
                $("#id").val("-1");
                $("#orgAccountId").val("-1");
                $("#path").val("-1");
                $("#ldapSet").hide();
            } else if ((managementSubunit == true || managementSubunit == 'true') && node.id == parentAccountId) {//点击单位管理节点
                selectTreeNode = node.id;
                $("#table_area").hide();
                $("#form_area").show();
                //TODO 单位树
                $(".accountSizeDiv").hide();
                $(".groupSizeDiv").hide();
                $(".ManageAccountSizeDiv").show();
                $("#table_area").hide();
                $("#form_area").show();

                viewOne(currentAccountId);
                $("#formField").hide();
                $("#tree_area").enable();
                $("#formFooter").enable();
                $("#tree_area").show();
                $('#showAccountTreeFrame').attr('src', "/seeyon/organization/account.do?method=showAccountTree&from=orgAccountManager${ctp:csrfSuffix()}");
                $("#button_area").hide();
                $("#formFooter").hide();
            } else { //点击单位回填form
                selectTreeNode = node.id;
                $("#all-blocks").show();
                viewOne(node.id);
            }

        }

        //机构树上双击修改事件
        function treeDBClk(e, treeId, node) {
            if (null == node) return;
            editDetail(node.id);
        }

        //表格上双击事件
        function dblclk(data, r, c) {
            editDetail(data.id);
        }

        //页面按钮
        var toolBarVar = $("#toolbar").toolbar({
            toolbar: [{
                id: "add",
                name: "${ctp:i18n('common.toolbar.new.label')}",
                className: "ico16",
                click: newAccount
            },
                {
                    id: "edit",
                    name: "${ctp:i18n('common.button.modify.label')}",
                    className: "ico16 editor_16",
                    click: editAccount
                },
                {
                    id: "delete",
                    name: "${ctp:i18n('common.toolbar.delete.label')}",
                    className: "ico16 delete del_16",
                    click: delAccount
                }]
        });

        //连续添加机构
        function newAccount4conti() {
            //代码重复
            $("#id").val("-1");
            $("#name").val("");
            $("#shortName").val("");
            $("#secondName").val("");
            $("#code").val("");
            $("#description").val("");
            $("#adminName").val("");
            var initpwd = oManager.getInitPWDForPage();
            if (null != initpwd && initpwd != "" && initpwd != false) {
                initpwd = initpwd.substring(8);
            } else {
                initpwd = "";
            }
            $("#adminPass").val(initpwd);
            $("#adminPass1").val(initpwd);
            $("#sortId").val(parseInt($("#sortId").val()) + 1);

            $("#form_area").enable();
            $("#form_area").show();
            $("#table_area").hide();
            $("#button_area").show();
            $("#button_area").enable();
            $("#formField").css("bottom", "50px");
            $("#id").val("-1");
            if ($("#orgAccountId").val() === '-1') {
                //不点击树上节点直接点新建单位默认上级单位是集团，先写死  TODO
                if (managementSubunit == true || managementSubunit == 'true') {
                    var rootAcc = aManager.viewAccount(currentAccountId);
                    $("#superiorName").comp({
                        value: 'Account|' + currentAccountId,
                        text: rootAcc.name
                    });

                } else {
                    $("#superiorName").comp({
                        value: 'Account|-1730833917365171641',
                        text: "${ctp:i18n('account.group')}"
                    });
                }
            } else {
                //自动新建自动关联从左侧树选中过来的上级单位
                if ($("#unitTree").treeObj().getSelectedNodes()[0]) {
                    if ((managementSubunit == true || managementSubunit == 'true')
                        && $("#unitTree").treeObj().getSelectedNodes()[0].id == parentAccountId) {
                        var rootAcc = aManager.viewAccount(currentAccountId);
                        $("#superiorName").comp({
                            value: 'Account|' + currentAccountId,
                            text: rootAcc.name
                        });
                    } else {
                        var pName = $("#unitTree").treeObj().getSelectedNodes()[0].name;
                        $("#superiorName").comp({
                            value: 'Account|' + $("#orgAccountId").val(),
                            text: pName
                        });
                    }
                }

            }
            //自动增加连续排序号
            var preSortId = aManager.getMaxSort();
            if (preSortId == -1) {
                preSortId = 0
            }//集团排序号是-1，自增需要从1开始，排序号不能填0
            if (preSortId >= 999999999) {
                preSortId = 999999998
            }
            $("#sortId").val(parseInt(preSortId) + 1);
            $("#sortIdtype1").attr("checked", "checked");
            $("input[type='radio'][name='enabled'][value='true']").attr("checked", "checked");
            //单位管理员信息框
            $("#adminName").removeAttr("readonly");
            $("#adminPasswordtr").show();
            $("#validatepasswordtr").show();
            $("#pswpromotetr").show();
            $("tr[class='nonGroup']").show();
            $("tr[class='nonGroup']").enable();
            //!!防止由于集团版的input引起id相同无法提交
            $("tr[class='cGroup']").disable();
            $("tr[class='cGroup']").hide();
            $("#pswchecktr").hide();
            $("#lianxu").show();
            $("#copyGL").show();
            //默认选择全部可见
            $("input[name='permissionType']").get(0).checked = true;
            $("#sLevel").attr("checked", false);
            $("#pLevel").attr("checked", false);
            $("#xLevel").attr("checked", false);
            $("#range").val("");
            //ldap/ad
            if ('true' == $("#isLdapEnabled").val()) {
                $("#ldapSet").show();
            } else {
                $("#ldapSet").hide();
            }
            disRange0();
            //单位登录页
            $("input[name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
            $("#customLoginUrlTr").hide();
            $("#customLoginUrlDescTr").hide();

            $("#managementSubunit").attr("checked", false);
            if (managementSubunit == 'true') {
                //非集团管理员管理，不显示选项：允许管理子单位的组织及模型
                $("#managementSubunitTr").hide();
            }
        }

        //新建机构
        function newAccount() {
            $("#lianxu").show();
            $("#copyGL").show();
            $("#form_area").clearform();
            var initpwd = oManager.getInitPWDForPage();
            if (null != initpwd && initpwd != "" && initpwd != false) {
                initpwd = initpwd.substring(8);
            } else {
                initpwd = "";
            }
            $("#adminPass").val(initpwd);
            $("#adminPass1").val(initpwd);
            $("#form_area").enable();
            $("#form_area").show();
            $("#table_area").hide();
            $("#button_area").show();
            $("#button_area").enable();
            $("#tree_area").hide();
            $("#formField").show();
            $("#formField").css("bottom", "50px");
            $("#formFooter").show();
            $("#id").val("-1");
            if ($("#orgAccountId").val() === '-1') {
                if (managementSubunit == true || managementSubunit == 'true') {
                    var rootAcc = aManager.viewAccount(currentAccountId);
                    $("#superiorName").comp({
                        value: 'Account|' + currentAccountId,
                        text: rootAcc.name
                    });

                } else {
                    var rootAcc = aManager.viewAccount('-1730833917365171641');
                    $("#superiorName").comp({
                        value: 'Account|-1730833917365171641',
                        text: rootAcc.name
                    });
                }
            } else {
                //自动新建自动关联从左侧树选中过来的上级单位
                if ($("#unitTree").treeObj().getSelectedNodes()[0]) {
                    if ((managementSubunit == true || managementSubunit == 'true')
                        && $("#unitTree").treeObj().getSelectedNodes()[0].id == parentAccountId) {
                        var rootAcc = aManager.viewAccount(currentAccountId);
                        $("#superiorName").comp({
                            value: 'Account|' + currentAccountId,
                            text: rootAcc.name
                        });
                    } else {
                        var pName = $("#unitTree").treeObj().getSelectedNodes()[0].name;
                        $("#superiorName").comp({
                            value: 'Account|' + $("#orgAccountId").val(),
                            text: pName
                        });
                    }
                }
            }
            //自动增加连续排序号
            var preSortId = aManager.getMaxSort();
            if (preSortId == -1) {
                preSortId = 0
            }//集团排序号是-1，自增需要从1开始，排序号不能填0
            if (preSortId >= 999999999) {
                preSortId = 999999998
            }
            $("#sortId").val(parseInt(preSortId) + 1);
            $("#sortIdtype1").attr("checked", "checked");
            $("input[type='radio'][name='enabled'][value='true']").attr("checked", "checked");
            //默认勾选连续添加勾选复制集团职务
            $("#copyGroupLevel").attr("checked", "checked");
            $("#isCopyGroupLevel").val("1");
            $("#conti").attr("checked", "checked");
            //单位管理员信息框
            $("#adminName").removeAttr("readonly");
            $("#adminPasswordtr").show();
            $("#validatepasswordtr").show();
            $("#pswpromotetr").show();
            $("tr[class='nonGroup']").show();
            $("tr[class='nonGroup']").enable();
            //!!防止由于集团版的input引起id相同无法提交
            $("tr[class='cGroup']").disable();
            $("tr[class='cGroup']").hide();
            $("#pswchecktr").hide();

            //新建时不显示并发设置
            $(".permission").hide();

            //默认选择全部可见
            $("input[name='permissionType']").get(0).checked = true;
            $("#pType").val("1");
            disRange0();
            //ldap/ad
            if ('true' == $("#isLdapEnabled").val()) {
                $("#ldapSet").show();
            } else {
                $("#ldapSet").hide();
            }
            $("input[name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
            $("#customLoginUrlTr").hide();
            $("#customLoginUrlDescTr").hide();
            $("#managementSubunit").attr("checked", false);
            if (managementSubunit == 'true') {
                //非集团管理员管理，不显示选项：允许管理子单位的组织及模型
                $("#managementSubunitTr").hide();
            }

            $("tr[name='customerRoleTr']").remove();
            //新建单位的时候，因为还没有单位信息，自定义的单位角色，只展示，不能进行修改。展示信息取集团自动以单位角色信息
            var rolelist = aManager.getAccountCustomerRoles('-1730833917365171641')["accountCustomerRoles"];
            for (i = rolelist.length - 1; i >= 0; i--) {
                $("#managementSubunitTr").after("<tr name='customerRoleTr' class='nonGroup'><th><label id=\"role" + i + "\" style=\"margin-right:10px;\" for=\"text\"></label></th><td><div class=\"common_txtbox_wrap\">" +
                    "<input disabled='disabled' type='text' id='customerRole" + i + "'><input type='hidden' id='customerRole" + i + "Id'></div></td></tr>");
                var name = rolelist[i]['showName'];
                $("#role" + i).attr("title", name);
                if (name.length > 8) {
                    name = name.substring(0, 7) + "...";
                }
                $("#role" + i).text(name + ":");
                $("#customerRole" + i).click(function () {
                    var inputName = $(this).attr("id");
                    $("#customerRole" + i).resetValidate();
                    $.selectPeople({
                        type: 'selectPeople',
                        panels: 'Department',
                        selectType: 'Member',
                        accountId: '-1',
                        minSize: 1,
                        maxSize: 1,
                        onlyLoginAccount: true,
                        returnValueNeedType: false,
                        callback: function (ret) {
                            $("#" + inputName).val(ret.text);
                            $("#" + inputName + "Id").val(ret.value);
                        }
                    });
                });
            }

        }

        //修改一个
        function editAccount() {
            if (managementSubunit == 'true' && selectTreeNode == parentAccountId) {
                $.alert("${ctp:i18n('account.team.choose.edit')}");
                return;
            }
            var aDetail = new Object();
            if ($("#id").val() !== "-1") {
                aDetail = aManager.viewAccount($("#id").val());
            } else {
                var boxs = $("#unitTable input:checked");
                if (boxs.length === 0) {
                    $.alert("${ctp:i18n('account.team.choose.edit')}");
                    return;
                } else if (boxs.length > 1) {
                    $.alert("${ctp:i18n('org.member_form.choose.personnel.one.edit')}");
                    return;
                } else {
                    aDetail = aManager.viewAccount(boxs[0].value);
                    $("#sortIdtype1").attr("checked", "checked");
                }
            }
            $("#lianxu").hide();
            $("#copyGL").hide();
            $("#form_area").clearform();
            $("#accountForm").enable();
            $("#table_area").hide();
            $("#button_area").show();
            $("#form_area").show();
            $("#form_area").fillform(aDetail);
            $("#formField").css("bottom", "50px");
            $("#formFooter").show();
            showPermission(aDetail);
            if (aDetail.enabled == true || aDetail.enabled == 'true') {
                $("input[type='radio'][name='enabled'][value='true']").attr("checked", "checked");
            } else {
                $("input[type='radio'][name='enabled'][value='false']").attr("checked", "checked");
            }
            $("#superiorName").comp({
                value: 'Account|' + aDetail["superior"],
                text: aDetail['superiorName'],
                excludeElements: 'Account|' + aDetail.id
            });

            $("#sortIdtype1").attr("checked", "checked");
            $("#permissionType1").attr("checked", "checked");
            $("#checkManager").removeAttr("checked");
            $("#pswchecktr").show();

            $("#range").comp({value: '', text: ''});
            if (aDetail.group === 'true') {
                $("tr[class='nonGroup']").hide();
                $("tr[class='cGroup']").show();
                $("tr[class='nonGroup']").disable();
                $("tr[class='cGroup']").enable();
                $("#lianxu").hide();
                showSizeDiv("group", aDetail);
            } else if ((managementSubunit == true || managementSubunit == 'true') && selectTreeNode == parentAccountId) {
                $("tr[class='nonGroup']").show();
                $("tr[class='cGroup']").hide();
                $("tr[class='cGroup']").disable();
                $("tr[class='nonGroup']").enable();
                showSizeDiv("managementSubunit", aDetail);
            } else {
                $("tr[class='nonGroup']").show();
                $("tr[class='cGroup']").hide();
                $("tr[class='cGroup']").disable();
                $("tr[class='nonGroup']").enable();
                showSizeDiv("account", aDetail);
            }
            //单位可见
            dealAccess(aDetail);
            //ldap/ad
            if ('true' == $("#isLdapEnabled").val()) {
                $("#ldapSet").show();
            } else {
                $("#ldapSet").hide();
            }
            //单位管理员框
            $("#adminName").attr("readonly", "readonly");
            $("#adminPasswordtr").hide();
            $("#validatepasswordtr").hide();
            $("#pswpromotetr").hide();
            //单位登录页选项回写
            if (aDetail.customLogin == 'true') {
                $("input[name='isCustomLoginUrl'][value='true']").attr("checked", "checked");
                $("#customLoginUrlTr").show();
                $("#customLoginUrlDescTr").show();
                $("#customLoginUrl").val(aDetail.customLoginUrl);
            } else {
                $("input[name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
                $("#customLoginUrlTr").hide();
                $("#customLoginUrlDescTr").hide();
            }

            if (managementSubunit == 'true' && aDetail.id == currentAccountId) {
                $("#customLoginUrlTr").hide();
                $("#customLoginUrlDescTr").hide();
            }

            if (aDetail.managementSubunit == 'true' || aDetail.managementSubunit == true) {
                $("#managementSubunit").attr("checked", true);
            } else {
                $("#managementSubunit").attr("checked", false);
            }


            $("tr[name='customerRoleTr']").remove();
            if (aDetail.group != 'true' && aDetail.group != true) {
                var cAccountId = $("#orgAccountId").val();
                var rolelist = aManager.getAccountCustomerRoles(cAccountId)["accountCustomerRoles"];
                for (i = rolelist.length - 1; i >= 0; i--) {
                    $("#managementSubunitTr").after("<tr name='customerRoleTr' class='nonGroup'><th><label id=\"role" + i + "\" style=\"margin-right:10px;\" for=\"text\"></label></th><td><div class=\"common_txtbox_wrap\">" +
                        "<input type='text' id='customerRole" + i + "'><input type='hidden' id='customerRole" + i + "Id'></div></td></tr>");
                    var name = rolelist[i]['showName'];
                    $("#role" + i).attr("title", name);
                    if (name.length > 8) {
                        name = name.substring(0, 7) + "...";
                    }
                    $("#role" + i).text(name + ":");
                    $("#customerRole" + i).val(aDetail["customerRole" + i + "Text"]);
                    $("#customerRole" + i + "Id").val(aDetail["customerRole" + i + "Id"]);
                    $("#customerRole" + i).click(function () {
                        var inputName = $(this).attr("id");
                        var customerRoleValue = aDetail[inputName + "Id"];
                        var customerRoleText = aDetail[inputName + "Text"];
                        $("#customerRole" + i).resetValidate();
                        var num = inputName.substring(inputName.length - 1);
                        var roleId = rolelist[num]['id'];
                        var o = new orgManager();
                        var role = o.getRoleById(roleId);
                        var isoutworker = "";
                        if (!o.isBaseRole(role.code) || role.code == "ExternalStaff" || role.code == "GeneralStaff") {
                            isoutworker = ",Outworker";
                        } else {
                            isoutworker = "";
                        }
                        $.selectPeople({
                            value: customerRoleValue,
                            type: 'selectPeople',
                            panels: 'Department,Post,Level' + isoutworker,
                            selectType: 'Account,Member,Department,Post,Level',
                            accountId: cAccountId,
                            minSize: 0,
                            hiddenPostOfDepartment: true,
                            onlyLoginAccount: true,
                            isCheckInclusionRelations: false,
                            params: {
                                value: customerRoleValue
                            },
                            callback: function (ret) {
                                $("#" + inputName).val(ret.text);
                                $("#" + inputName + "Id").val(ret.value);
                            }
                        });

                    });
                }
            }

            try {
                $("#name").focus();//回到顶部
            } catch (e) {
            }
            ;

            if (aDetail.hasAccountbindPlugin == true || aDetail.hasAccountbindPlugin == 'true') {
                $("#gname").disable();
            }
        }

        //删除单位
        function delAccount() {
            if (managementSubunit == 'true' && selectTreeNode == parentAccountId) {
                $.alert("${ctp:i18n('account.rootnode.not.delete')}");
                return;
            }
            var accoutIds = new Array();
            if ($("#id").val() === '-1730833917365171641') {
                $.alert("${ctp:i18n('account.group.not.delete')}");
                return;
            } else if ($("#id").val() !== "-1") {
                $.confirm({
                    title: "${ctp:i18n('common.prompt')}",
                    msg: "${ctp:i18n('account.delete.ok.manager')}",
                    ok_fn: function () {
                        accoutIds.push($("#id").val());
                        if (accoutIds.contains(currentAccountId)) {
                            $.alert("${ctp:i18n('account.not.delete.self')}");
                            return;
                        }
                        aManager.delAccounts(accoutIds, {
                            success: function () {
                                $.messageBox({
                                    id: 'delOKBox',
                                    title: "${ctp:i18n('common.prompt')}",
                                    type: 0,
                                    imgType: 0,
                                    msg: "${ctp:i18n('organization.ok')}",
                                    ok_fn: function () {
                                        location.reload();
                                    }
                                });
                            }
                        });
                    }
                });
            } else {
                var boxs = $("#unitTable input:checked");
                if (boxs.length === 0) {
                    $.alert("${ctp:i18n('account.group.choose.delete')}");
                    return;
                } else {
                    var isG = false;
                    boxs.each(function () {
                        if ($(this).val() === '-1730833917365171641') {
                            $.alert("${ctp:i18n('account.group.not.delete')}!");
                            isG = true;
                        } else {
                            accoutIds.push($(this).val());
                        }
                    });
                    if (!isG) {
                        $.confirm({
                            'title': "${ctp:i18n('common.prompt')}",
                            'msg': "${ctp:i18n('account.delete.ok.manager')}",
                            ok_fn: function () {
                                if (accoutIds.contains(currentAccountId)) {
                                    $.alert("不能删除本单位！");
                                    return;
                                }
                                aManager.delAccounts(accoutIds, {
                                    success: function () {
                                        $.messageBox({
                                            'title': "${ctp:i18n('common.prompt')}",
                                            'type': 0,
                                            'imgType': 0,
                                            'msg': "${ctp:i18n('organization.ok')}",
                                            ok_fn: function () {
                                                location.reload();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }

        //add account
        $("#btnok").click(function () {
            if (!($("#accountForm").validate())) return;
            $("#accountForm").resetValidate();
            if ($("#adminPass").val() !== $("#adminPass1").val()) {
                $.messageBox({
                    'title': "${ctp:i18n('common.prompt')}",
                    'type': 0,
                    'imgType': 2,
                    'msg': "${ctp:i18n('account.system.newpassword.again.not.consistent')}"
                });
                $("#adminPass").val("");
                $("#adminPass1").val("");
                $("#adminPass").focus();
                return;
            }
            if (!checkCustomLoginUrl()) {//5.1新属性如果开启自定义登录页，则为必填项
                return;
            }
            if ('true' == "${isLdapEnabled}" && 'false' == "${LdapCanOauserLogon}") {
                if ($("#ldapOu").val() == '') {
                    $.alert("${ctp:i18n('org.alert.ldapOUNotNull')}");
                    return;
                }
            }
            if ($("#id").val() === '-1') {
                if ("checked" === $("#copyGroupLevel").attr("checked")) {
                    $("#isCopyGroupLevel").val("1");
                } else {
                    $("#isCopyGroupLevel").val("0");
                }
                if (getCtpTop() && getCtpTop().startProc) getCtpTop().startProc();
                aManager.createAcc($("#accountForm").formobj(), {
                    success: function (accBean) {
                        $("#id").val(accBean);
                        if ($("#conti").attr("checked") === "checked") {
                            isConti = true;
                            $("#unitTree").treeObj().reAsyncChildNodes(null, "refresh", false);
                            newAccount4conti();
                        } else {
                            isConti = false;
                            $.messageBox({
                                'title': "${ctp:i18n('common.prompt')}",
                                'type': 0,
                                'imgType': 0,
                                'msg': "${ctp:i18n('organization.ok')}",
                                ok_fn: function () {
                                    location.reload();
                                }
                            });
                        }
                        try {
                            if (getCtpTop() && getCtpTop().endProc) {
                                getCtpTop().endProc()
                            }
                        } catch (e) {
                        }
                        ;
                    }
                });
            } else {
                if (getCtpTop() && getCtpTop().startProc) getCtpTop().startProc();
                aManager.updateAcc($("#accountForm").formobj(), {
                    success: function (accBean) {
                        $("#id").val(accBean);
                        $.messageBox({
                            'title': "${ctp:i18n('common.prompt')}",
                            'type': 0,
                            'imgType': 0,
                            'msg': "${ctp:i18n('organization.ok')}",
                            ok_fn: function () {
                                if ($("#id").val() == "-1730833917365171641") {
                                    //getCtpTop().refreshAccountName($("#gshortName").val(),$("#secondName").val(),"","");//OA-22912
                                }
                                location.reload();
                            }
                        });
                        try {
                            if (getCtpTop() && getCtpTop().endProc) {
                                getCtpTop().endProc()
                            }
                        } catch (e) {
                        }
                        ;
                    }
                });
            }
        });
        //取消按钮
        $("#btncancel").click(function () {
            $("#form_area").clearform();
            var node = $("#unitTree").treeObj().getNodeByParam("id", selectTreeNode);
            clk('', '', node);
            //location.reload();
        });

        if (${param.from != "groupControl"}) {
            $("#form_area").show();
            $("#table_area").hide();
        } else {
            $("#table_area").show();
            $("#form_area").hide();
        }

        $("#checkManager").click(function () {
            //单位管理员框
            if ($("#checkManager").attr("checked") === "checked") {
                $("#adminName").removeAttr("readonly");
                $("#adminPasswordtr").show();
                $("#validatepasswordtr").show();
                $("#pswpromotetr").show();
                $("#pswchecktr").show();
                $("#checkManager").val("1");
                $("#adminPass").val("");
                $("#adminPass1").val("");
            } else {
                //单位管理员框
                $("#adminName").attr("readonly", "readonly");
                $("#adminPasswordtr").hide();
                $("#validatepasswordtr").hide();
                $("#pswpromotetr").hide();
                $("#pswchecktr").show();
                $("#checkManager").val("0");
                $("#adminPass").val("~`@%^*#?");
                $("#adminPass1").val("~`@%^*#?");
            }
        });

        function editDetail(accountId) {
            selectTreeNode = accountId;
            if ((managementSubunit == true || managementSubunit == 'true') && selectTreeNode == parentAccountId) {
                return;
            }
            if (accountId === '-1') { //点击根显示列表
                $("#table_area").show();
                $("#form_area").hide();
                var o = new Object();
                $("#unitTable").ajaxgridLoad(o);
                $("#id").val("-1");
                $("#orgAccountId").val("-1");
                $("#path").val("-1");
            } else {
                var aDetail = aManager.viewAccount(accountId);
                $("#form_area").clearform();
                $("#sortIdtype1").attr("checked", "checked");
                $("#accountForm").enable();
                $("#table_area").hide();
                $("#button_area").show();
                $("#form_area").show();
                $("#form_area").fillform(aDetail);
                $("#formField").css("bottom", "50px");
                showPermission(aDetail);
                if (aDetail.enabled == true || aDetail.enabled == 'true') {
                    $("input[type='radio'][name='enabled'][value='true']").attr("checked", "checked");
                } else {
                    $("input[type='radio'][name='enabled'][value='false']").attr("checked", "checked");
                }
                $("#superiorName").comp({
                    value: 'Account|' + aDetail["superior"],
                    text: aDetail['superiorName'],
                    excludeElements: "Account|" + aDetail.id
                });

                $("#sortIdtype1").attr("checked", "checked");
                $("#checkManager").removeAttr("checked");
                $("#pswchecktr").show();

                $("#range").comp({value: '', text: ''});
                $("#copyGL").hide();
                $("#lianxu").hide();
                //ldap/ad
                if ('true' == $("#isLdapEnabled").val()) {
                    $("#ldapSet").show();
                } else {
                    $("#ldapSet").hide();
                }
                if (aDetail.group === 'true') {
                    $("tr[class='nonGroup']").hide();
                    $("tr[class='cGroup']").show();
                    $("tr[class='nonGroup']").disable();
                    $("tr[class='cGroup']").enable();
                    showSizeDiv("group", aDetail);
                    $("#formFooter").show();
                } else if ((managementSubunit == true || managementSubunit == 'true') && selectTreeNode == parentAccountId) {
                    $("tr[class='nonGroup']").show();
                    $("tr[class='cGroup']").hide();
                    $("tr[class='cGroup']").disable();
                    $("tr[class='nonGroup']").enable();
                    showSizeDiv("managementSubunit", aDetail);
                } else {
                    $("tr[class='nonGroup']").show();
                    $("tr[class='cGroup']").hide();
                    $("tr[class='cGroup']").disable();
                    $("tr[class='nonGroup']").enable();
                    showSizeDiv("account", aDetail);
                    $("#formFooter").show();
                }
                dealAccess(aDetail); //单位访问权限回写
                //单位管理员框
                $("#adminName").attr("readonly", "readonly");
                $("#adminPasswordtr").hide();
                $("#validatepasswordtr").hide();
                $("#pswpromotetr").hide();
                //单位登录页选项回写
                if (aDetail.customLogin == 'true') {
                    $("input[name='isCustomLoginUrl'][value='true']").attr("checked", "checked");
                    $("#customLoginUrlTr").show();
                    $("#customLoginUrlDescTr").show();
                    $("#customLoginUrl").val(aDetail.customLoginUrl);
                } else {
                    $("input[name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
                    $("#customLoginUrlTr").hide();
                    $("#customLoginUrlDescTr").hide();
                }

                if (managementSubunit == 'true' && aDetail.id == currentAccountId) {
                    $("#customLoginUrlTr").hide();
                    $("#customLoginUrlDescTr").hide();
                }

                if (aDetail.managementSubunit == 'true' || aDetail.managementSubunit == true) {
                    $("#managementSubunit").attr("checked", true);
                } else {
                    $("#managementSubunit").attr("checked", false);
                }

                $("tr[name='customerRoleTr']").remove();
                if (aDetail.group != 'true' && aDetail.group != true) {
                    var cAccountId = $("#orgAccountId").val();
                    var rolelist = aManager.getAccountCustomerRoles(cAccountId)["accountCustomerRoles"];
                    for (i = rolelist.length - 1; i >= 0; i--) {
                        $("#managementSubunitTr").after("<tr name='customerRoleTr' class='nonGroup'><th><label id=\"role" + i + "\" style=\"margin-right:10px;\" for=\"text\"></label></th><td><div class=\"common_txtbox_wrap\">" +
                            "<input type='text' id='customerRole" + i + "'><input type='hidden' id='customerRole" + i + "Id'></div></td></tr>");
                        var name = rolelist[i]['showName'];
                        $("#role" + i).attr("title", name);
                        if (name.length > 8) {
                            name = name.substring(0, 7) + "...";
                        }
                        $("#role" + i).text(name + ":");
                        $("#customerRole" + i).val(aDetail["customerRole" + i + "Text"]);
                        $("#customerRole" + i + "Id").val(aDetail["customerRole" + i + "Id"]);
                        $("#customerRole" + i).click(function () {
                            var inputName = $(this).attr("id");
                            var customerRoleValue = aDetail[inputName + "Id"];
                            var customerRoleText = aDetail[inputName + "Text"];
                            $("#customerRole" + i).resetValidate();
                            var num = inputName.substring(inputName.length - 1);
                            var roleId = rolelist[num]['id'];
                            var o = new orgManager();
                            var role = o.getRoleById(roleId);
                            var isoutworker = "";
                            if (!o.isBaseRole(role.code) || role.code == "ExternalStaff" || role.code == "GeneralStaff") {
                                isoutworker = ",Outworker";
                            } else {
                                isoutworker = "";
                            }
                            $.selectPeople({
                                value: customerRoleValue,
                                type: 'selectPeople',
                                panels: 'Department,Post,Level' + isoutworker,
                                selectType: 'Account,Member,Department,Post,Level',
                                accountId: cAccountId,
                                minSize: 0,
                                hiddenPostOfDepartment: true,
                                onlyLoginAccount: true,
                                isCheckInclusionRelations: false,
                                params: {
                                    value: $("#" + inputName + "Id").val()
                                },
                                callback: function (ret) {
                                    $("#" + inputName).val(ret.text);
                                    $("#" + inputName + "Id").val(ret.value);
                                }
                            });

                        });
                    }
                }
                try {
                    $("#name").focus();//回到顶部
                } catch (e) {
                }
                ;

                if (aDetail.hasAccountbindPlugin == true || aDetail.hasAccountbindPlugin == 'true') {
                    $("#gname").disable();
                }
            }

        }

        //单位自定义登录页
        function checkCustomLoginUrl() {
            if ($('input[name="isCustomLoginUrl"]:checked').val() == 'true') {
                var _customLoginUrl = $("#customLoginUrl").val().trim();
                var patten1 = /^[a-z]*$/g;
                ;//英文字母
                var patten2 = /\s+/;//不能有空格
                if (_customLoginUrl == '') {
                    $.alert("${ctp:i18n('custom.loginurl.null')}");
                    $("#customLoginUrl").focus();
                    return false;
                } else if (_customLoginUrl.length > 20) {
                    $.alert("${ctp:i18n('custom.loginurl.tolong')}");
                    $("#customLoginUrl").focus();
                    return false;
                } else if (!patten1.test(_customLoginUrl)) {
                    $.alert("${ctp:i18n('custom.loginurl.nomeng')}");
                    $("#customLoginUrl").focus();
                    return false;
                } else if (patten2.test(_customLoginUrl)) {
                    $.alert("${ctp:i18n('custom.loginurl.noblank')}");
                    $("#customLoginUrl").focus();
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }

        $('input[name="isCustomLoginUrl"]').bind('click', function () {
            dealCustomLogin();
        });

        //搜索框
        var searchobj = $.searchCondition({
            top: 7,
            right: 10,
            searchHandler: function () {
                console.log("查询");
                console.log(searchobj.g.showSearchBox());
                var s = searchobj.g.getReturnValue();
                $("#id").val("-1");
                var selectNode = $("#unitTree").treeObj().getNodeByParam("id", "-1730833917365171641");
                $("#unitTree").treeObj().selectNode(selectNode);

                $("#table_area").show();
                $("#form_area").hide();
                $("#unitTable").ajaxgridLoad(s);
            },
            conditions: [{
                id: 'search_all',
                name: 'search_all',
                text: "${ctp:i18n('post.all')}",
                value: 'all'
            },
                {
                    id: 'search_name',
                    name: 'search_name',
                    type: 'input',
                    text: "${ctp:i18n('org.dept.form.name')}",
                    value: 'name'
                },
                {
                    id: 'search_shortName',
                    name: 'search_shortName',
                    type: 'input',
                    text: "${ctp:i18n('org.dept.shortened.form.name')}",
                    value: 'shortName'
                }
                ,
                {
                    id: 'search_code',
                    name: 'search_code',
                    type: 'input',
                    text: "${ctp:i18n('org.account_form.code.label')}",
                    value: 'code'
                }
                , {
                    id: 'search_peoplename',
                    name: 'search_peoplename',
                    type: 'input',
                    text: "人员姓名",
                    value: 'code'
                }
            ]
        });

        /******ldap/ad*******/
        //弹出选择目录树的界面
        $("#ldapOu").click(function () {
            var sendResult = $.dialog({
                url: "/seeyon/ldap/ldap.do?method=viewOuTree" + "${ctp:csrfSuffix()}",
                width: "410",
                height: "325",
                scrollbars: "yes",
                title: " ",
                buttons: [{
                    id: "ldapOK",
                    text: "${ctp:i18n('common.button.ok.label')}",
                    handler: function () {
                        var rvalue = sendResult.getReturnValue();
                        if (!rvalue) {
                            sendResult.close();
                            return;
                        } else {
                            document.getElementById("ldapOu").value = rvalue;
                        }
                        sendResult.close();
                    }
                }]
            });
        });
        /********************/

        //单位可见，选中一类其他类后面的选项disable
        $("[name='permissionType']").click(function () {
            var pFlag = $("input[name='permissionType']:radio:checked").val();
            if (0 == pFlag) {
                disRange0();
            } else if (1 == pFlag) {
                disRange1();
            } else {
                disRange2();
            }
        });
    });


    function viewByName(id) {
        selectTreeNode = id;
        viewOne(id);
    }

    function dealCustomLogin() {
        var is_CunstomLoginUrl = $('input[name="isCustomLoginUrl"]:checked').val();
        if ('true' == is_CunstomLoginUrl || true == is_CunstomLoginUrl) {
            $("#customLoginUrlTr").show();
            $("#customLoginUrlDescTr").show();
            $("input[type='radio'][name='isCustomLoginUrl'][value='true']").attr("checked", "checked");
        } else if ('false' == is_CunstomLoginUrl || false == is_CunstomLoginUrl) {
            $("#customLoginUrlTr").hide();
            $("#customLoginUrlDescTr").hide();
            $("input[type='radio'][name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
        }
    }

    /**显示许可数*/
    function showPermission(aDetail) {
        if (aDetail.isGroup) {
            $("#serverPermissionGroup").get(0).innerHTML = aDetail.serverPermission;
            $("#m1PermissionGroup").get(0).innerHTML = aDetail.m1Permission;
            $("#unitRegCounts").get(0).innerHTML = aDetail.unitRegCounts;
        } else {
            $("#serverPermission").get(0).innerHTML = aDetail.serverPermission;
            $("#m1Permission").get(0).innerHTML = aDetail.m1Permission;
        }
    }

    function viewOne(id) {
        var aDetail = aManager.viewAccount(id);

        $("#form_area").clearform();
        $("#form_area").show();
        $("#button_area").hide();
        $("#formField").css("bottom", "0");
        $("#table_area").hide();
        $("#sortIdtype1").attr("checked", "checked");
        $("#checkManager").removeAttr("checked");
        $("#form_area").fillform(aDetail);
        showPermission(aDetail);
        if (aDetail.enabled == true || aDetail.enabled == 'true') {
            $("input[type='radio'][name='enabled'][value='true']").attr("checked", "checked");
        } else {
            $("input[type='radio'][name='enabled'][value='false']").attr("checked", "checked");
        }
        if (aDetail.group === 'true') {
            $("tr[name='customerRoleTr']").remove();
            $("tr[class='nonGroup']").hide();
            $("tr[class='cGroup']").show();
            showSizeDiv("group", aDetail);
        } else if (managementSubunit && selectTreeNode == parentAccountId) {
            $("tr[class='nonGroup']").show();
            $("tr[class='cGroup']").hide();
            showSizeDiv("managementSubunit", aDetail);
        } else {
            $("tr[class='nonGroup']").show();
            $("tr[class='cGroup']").hide();
            showSizeDiv("account", aDetail);

            //上级单位回写
            $("#superiorName").comp({
                value: 'Account|' + aDetail["superior"],
                text: aDetail['superiorName'],
                excludeElements: parseElements('Account|' + aDetail.id)
            });
            $("#adminPasswordtr").hide();
            $("#validatepasswordtr").hide();
            $("#pswpromotetr").hide();
            $("#orgAccountId").val(id);
            $("#range").comp({value: '', text: ''});
            dealAccess(aDetail); //单位访问权限回写
            //单位登录页选项回写
            if (aDetail.customLogin == 'true') {
                $("input[name='isCustomLoginUrl'][value='true']").attr("checked", "checked");
                $("#customLoginUrlTr").show();
                $("#customLoginUrlDescTr").show();
                $("#customLoginUrl").val(aDetail.customLoginUrl);
            } else {
                $("input[name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
                $("#customLoginUrlTr").hide();
                $("#customLoginUrlDescTr").hide();
            }

            if (managementSubunit == 'true' && aDetail.id == currentAccountId) {
                $("#customLoginUrlTr").hide();
                $("#customLoginUrlDescTr").hide();
            }

            if (aDetail.managementSubunit == 'true' || aDetail.managementSubunit == true) {
                $("#managementSubunit").attr("checked", true);
            } else {
                $("#managementSubunit").attr("checked", false);
            }

            $("tr[name='customerRoleTr']").remove();
            var cAccountId = $("#orgAccountId").val();
            var rolelist = aManager.getAccountCustomerRoles(cAccountId)["accountCustomerRoles"];
            for (i = rolelist.length - 1; i >= 0; i--) {
                $("#managementSubunitTr").after("<tr name='customerRoleTr' class='nonGroup'><th><label id=\"role" + i + "\" style=\"margin-right:10px;\" for=\"text\"></label></th><td><div class=\"common_txtbox_wrap\">" +
                    "<input type='text' disabled='disabled' id='customerRole" + i + "'><input type='hidden' id='customerRole" + i + "Id'></div></td></tr>");
                var name = rolelist[i]['showName'];
                $("#role" + i).attr("title", name);
                if (name.length > 8) {
                    name = name.substring(0, 7) + "...";
                }
                $("#role" + i).text(name + ":");
                $("#customerRole" + i).val(aDetail["customerRole" + i + "Text"]);
                $("#customerRole" + i + "Id").val(aDetail["customerRole" + i + "Id"]);
                $("#customerRole" + i).click(function () {
                    var inputName = $(this).attr("id");
                    var customerRoleValue = aDetail[inputName + "Id"];
                    var customerRoleText = aDetail[inputName + "Text"];
                    $("#customerRole" + i).resetValidate();
                    var num = inputName.substring(inputName.length - 1);
                    var roleId = rolelist[num]['id'];
                    var o = new orgManager();
                    var role = o.getRoleById(roleId);
                    var isoutworker = "";
                    if (!o.isBaseRole(role.code) || role.code == "ExternalStaff" || role.code == "GeneralStaff") {
                        isoutworker = ",Outworker";
                    } else {
                        isoutworker = "";
                    }
                    $.selectPeople({
                        value: customerRoleValue,
                        type: 'selectPeople',
                        panels: 'Department,Post,Level' + isoutworker,
                        selectType: 'Account,Member,Department,Post,Level',
                        accountId: cAccountId,
                        minSize: 0,
                        hiddenPostOfDepartment: true,
                        onlyLoginAccount: true,
                        isCheckInclusionRelations: false,
                        params: {
                            value: $("#" + inputName + "Id").val()
                        },
                        callback: function (ret) {
                            $("#" + inputName).val(ret.text);
                            $("#" + inputName + "Id").val(ret.value);
                        }
                    });

                });
            }
            try {
                $("#name").focus();//回到顶部
            } catch (e) {
            }
            ;
        }
        //ldap/ad
        if ('true' == $("#isLdapEnabled").val()) {
            $("#ldapSet").show();
        } else {
            $("#ldapSet").hide();
        }
        $("#accountForm").disable();
        try {
            $("#name").focus();//回到顶部
        } catch (e) {
        }
        ;

    }

    //用于回写单位分级设置的分级选中的方法
    function dealAccess(aDetail) {
        var permisson = $("input[type='radio'][name='permissionType']");
        permisson.each(function () {
            if (aDetail.permissionType == $(this).val()) $(this).attr("checked", "checked");
        });
        if (0 == aDetail.permissionType) {
            disRange0();
            //统一设置
            if (aDetail.isCanAccess) {
                $("#pType").val("1");
            } else {
                $("#pType").val("0");
            }
        } else if (1 == aDetail.permissionType) {
            disRange1();
            //分级设置
            if ('1' == aDetail.sLevel) {
                $("#sLevel").attr("checked", "checked");
            }
            if ('1' == aDetail.pLevel) {
                $("#pLevel").attr("checked", "checked");
            }
            if ('1' == aDetail.xLevel) {
                $("#xLevel").attr("checked", "checked");
            }
        } else {
            disRange2();
            //自由设置
            if (aDetail.isCanAccess) {
                $("#rangType").val("1");
            } else {
                $("#rangType").val("0");
            }
            $("#range").comp({
                value: aDetail.strValueId,
                text: aDetail.strValueName
            });
        }
    }

    function disRange0() {
        $("#pType").enable();
        $("#pType").val("1");
        $("#sLevel").disable();
        $("#pLevel").disable();
        $("#xLevel").disable();
        $("#rangType").disable();
        $("#permissionType4tr").disable();
    }

    function disRange1() {
        $("#pType").disable();
        $("#sLevel").enable();
        $("#pLevel").enable();
        $("#xLevel").enable();
        $("#rangType").disable();
        $("#permissionType4tr").disable();
    }

    function disRange2() {
        $("#pType").disable();
        $("#sLevel").disable();
        $("#pLevel").disable();
        $("#xLevel").disable();
        $("#rangType").enable();
        $("#rangType").val("1");
        $("#permissionType4tr").enable();
    }

    function canInput(o) {
        if (o.readOnly) {
            o.blur();
        }
    }

    function showSizeDiv(type, aDetail) {
        if (type == "group") {
            $(".stadic_body_top_bottom").css("top", 90);
            $(".accountSizeDiv").hide();
            $(".ManageAccountSizeDiv").hide();
            $(".groupSizeDiv").show();
            $("#accountSize").text(aDetail.accountSize);
            $("#gPostSize").text(aDetail.gPostSize);
            $("#gLevelSize").text(aDetail.gLevelSize);
            $("#gRoleSize").text(aDetail.gRoleSize);
            $("#concurrentSize").text(aDetail.concurrentSize);
            $("#gTeamSize").text(aDetail.gTeamSize);
            $("#tree_area").hide();
            $("#formField").show();
        } else if (type == "managementSubunit") {
            $(".stadic_body_top_bottom").css("top", 90);
            $(".accountSizeDiv").hide();
            $(".groupSizeDiv").hide();
            $(".ManageAccountSizeDiv").show();
            $("#mAccountSize").text(aDetail.mAccountSize);
            $("#mConcurrentSize").text(aDetail.mConcurrentSize);
            $("#mRedistributionSize").text(aDetail.mRedistributionSize);
            $("#formField").hide();
            //$("#button_area").hide();
            $("#tree_area").show();
            $('#showAccountTreeFrame').attr('src', "/seeyon/organization/account.do?method=showAccountTree&from=orgAccountManager${ctp:csrfSuffix()}");
            $("#tree_area").enable();
            $("#formFooter").hide();
        } else {
            $("#tree_area").hide();
            $("#formField").show();
            $(".groupSizeDiv").hide();
            if (allowGroupEditAccount) {
                $(".stadic_body_top_bottom").css("top", 90);
                $(".accountSizeDiv").show();
                $(".ManageAccountSizeDiv").hide();
                $("#deptSize").text(aDetail.deptSize);
                $("#postSize").text(aDetail.postSize);
                $("#levelSize").text(aDetail.levelSize);
                $("#memberSize").text(aDetail.memberSize);
                $("#roleSize").text(aDetail.roleSize);
                $("#extMemberSize").text(aDetail.extMemberSize);
                $("#teamSize").text(aDetail.teamSize);
            } else {
                $(".stadic_body_top_bottom").css("top", 0);
                $(".accountSizeDiv").hide();
                $(".ManageAccountSizeDiv").hide();
                $("#all-blocks").hide();
            }
        }

        if (isConti) {
            //单位登录页
            $("input[name='isCustomLoginUrl'][value='false']").attr("checked", "checked");
            $("#customLoginUrlTr").hide();
            $("#customLoginUrlDescTr").hide();
        }

        if (managementSubunit == true || managementSubunit == 'true') {
            //非集团管理员管理，不显示选项：允许管理子单位的组织及模型
            $("#managementSubunitTr").hide();
            if (aDetail.id == currentAccountId && $("#id").val() != '-1') {//当前单位的管理员和hr管理员，不能修改自己单位的状态和访问权限
                $("#sortIdTr").hide();
                $("#sortIdTypeTr").hide();
                $("#statusTr").hide();
                $("#isCustomLoginTr").hide();
                $("#customLoginUrlTr").hide();
                $("#customLoginUrlDescTr").hide();

                $("#isAdmin1").hide();
                $("#isAdmin2").hide();
                $("#isAdmin3").hide();
                $("#isAdmin4").hide();
                $("#permissionType1tr").hide();
                $("#permissionType2tr").hide();
                $("#permissionType3tr").hide();
                $("#permissionType4tr").hide();
            } else {
                $("#sortIdTr").show();
                $("#sortIdTypeTr").show();
                $("#statusTr").show();
                $("#isCustomLoginTr").show();
                $("#customLoginUrlTr").show();
                $("#customLoginUrlDescTr").show();

                $("#isAdmin1").show();
                $("#isAdmin2").show();
                $("#isAdmin3").show();
                $("#isAdmin4").show();
                $("#permissionType1tr").show();
                $("#permissionType2tr").show();
                $("#permissionType3tr").show();
                $("#permissionType4tr").show();

            }
        }
        $("#tree_area,#showAccountTreeFrame").height($(document.body).height() - 90 - 40 - 2 + "px");
        $("#tree_area").css("overflow", "hidden");
    }

    function openOrgWindow(type) {
        var id = $("#id").val();
        if (id == "-1730833917365171641") {
            var url = "";
            if (type == "1") {
                url = "/organization/account.do?method=listAccounts&from=groupControl";
            } else if (type == "2") {
                url = "/organization/postController.do?method=showPostframe";
            } else if (type == "3") {
                url = "/organization/levelController.do?method=showLevelframe";
            } else if (type == "4") {
                url = "/organization/role.do?method=showRoleList";
            } else if (type == "5") {
                url = "/organization/conPost.do?method=index";
            } else if (type == "6") {
                url = "/organization/teamController.do?method=showTeamframe";
            } else if (type == "z7") {
                //z周刘成
                url = "/ext/Zmember.do?method=listAll";
            }
            getCtpTop().showMenu(_ctxPath + url);
        } else {
            if (type == "11") {
                var selectNode = $("#unitTree").treeObj().getNodeByParam("id", "-1");
                if (selectNode) {
                    $("#unitTree").treeObj().selectNode(selectNode);
                    selectTreeNode = "-1";
                    $("#table_area").show();
                    $("#form_area").hide();
                    var o = new Object();
                    $("#unitTable").ajaxgridLoad(o);
                    $("#id").val("-1");
                    $("#orgAccountId").val("-1");
                    $("#path").val("-1");
                    $("#ldapSet").hide();
                }
            }

        }
    }

    function openOrgDialog(type) {
        var enabled = $("input[name='enabled']:radio:checked").val();
        if (enabled == "false") {
            return;
        }

        var id = $("#id").val();
        var name = $("#name").val();
        if (id != "-1730833917365171641") {
            var title = "";
            var url = "";
            if (type == "1") {
                title = "${ctp:i18n('system.menuname.Dept.Management')}";
                url = "/organization/department.do?method=showDepartmentFrame&style=tree";
            } else if (type == "2") {
                title = "${ctp:i18n('system.menuname.PostManagement')}";
                url = "/organization/postController.do?method=showPostframe";
            } else if (type == "3") {
                title = "${ctp:i18n('system.menuname.LevelManangement')}";
                url = "/organization/levelController.do?method=showLevelframe";
            } else if (type == "4") {
                title = "${ctp:i18n('system.menuname.PersonnelManangement')}";
                url = "/organization/member.do?method=listByAccount";
            } else if (type == "5") {
                title = "${ctp:i18n('system.menuname.RoleSetting')}";
                url = "/organization/role.do?method=showRoleList";
            } else if (type == "6") {
                title = "${ctp:i18n('system.menuname.ExternalPersonnel')}";
                url = "/organization/externalController.do?method=showExternalframe";
            } else if (type == "7") {
                title = "${ctp:i18n('system.menuname.TeamManagement')}";
                url = "/organization/teamController.do?method=showTeamframe";
            } else if (type == "8") {
                title = "${ctp:i18n('system.menuname.AccessLevelConfig')}";
                url = "/organization/workscopeController.do?method=showWorkscopeframe";
            } else if (type == "12") {
                title = "${ctp:i18n('system.menuname.Part-timeManagement')}";
                url = "/organization/conPost.do?method=index&from=accountControl";
            } else if (type == "13") {
                title = "${ctp:i18n('system.menuname.PersonnelRedistribution')}";
                url = "/organization/distributeController.do?method=showDistributeframe";
            }

            if (title == "") {
                return;
            }

            var openOrgDialog = $.dialog({
                id: 'openOrgDialog',
                url: _ctxPath + url + "&accountId=" + selectTreeNode + "${ctp:csrfSuffix()}",
                width: "1106px",
                height: $(window).height(),
                title: name + " - " + title,
                targetWindow: getCtpTop(),
                closeParam: {
                    'show': true,
                    autoClose: false,
                    handler: function () {
                        openOrgDialog.close();
                    }
                }
            });
        }
    }
</script>