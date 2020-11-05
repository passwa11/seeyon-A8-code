/*
 * 顶部右侧操作区
 */
vPortalMainFrameElements.topRightsystemOperation = {
    config: !(vPortal.subPortal == "true"),
    afterInit: function(_domId, _menuData) {
        /*if (showSkinchoose == "true") {
            vPortalMainFrameElements.topRightsystemOperation.showSkinPanle();
        }*/
        if(!vPortal.isDesigner) {
            $("body").on("click", function(e) {
                if (e.target.id !== "searchAreaInput" && $("#autoCompletion") && $("#autoCompletion:visible")) {
                    $("#autoCompletion").hide();
                }
            });
            // 输入框监听回车键
            var searchWordIpt = document.getElementById("searchAreaInput");
            if(searchWordIpt) {
                searchWordIpt.onkeyup = function(_event) {
                    var _event = _event ? _event : window.event;
                    autoCompletionFun.action(this,'autoCompletion');
                }
            }
        }
    },//刷新热点里面影响本元素的点
    refreshThisElement:function(){
        if( vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.topRightsystemOperationBgc && document.getElementById("topRightsystemOperationWidth")){
            var element = document.getElementById("topRightsystemOperationWidth");
            if(element){
                element.style.backgroundColor = vPortal.themeHotspots.skin.topRightsystemOperationBgc;
            }
        }
    },
    getData: function() {
        if(vPortal &&   vPortal.portalId=="590619364748181927" && vPortal.isDesigner){
            //vjoin不展示
            return false;
        }
        if (typeof(vPortal.themeHotspots.topRightsystemOperation) == "undefined") {
            vPortal.themeHotspots.topRightsystemOperation = {
                "btnBack": "0",
                "btnSign": "1",
                "btnSignStyle": "icon",
                "btnonlineNum":"1",
                "btnSearch": "1",
                "btnRefresh": "0",
                "btnSalaryView": "1",
                "btnProductNav": "0",
                "btnAppCenter": "1",
                "btnHomeSet": "1",
                "btnGo": "0",
                "btnMessage": "1"
            };
        }
        if (vPortal.themeHotspots.topRightsystemOperation && !vPortal.salaryEnabled) {
            vPortal.themeHotspots.topRightsystemOperation.btnSalaryView = "0";
        }
        if (vPortal.themeHotspots.topRightsystemOperation && !vPortal.themeHotspots.topRightsystemOperation.btnonlineNum) {
            vPortal.themeHotspots.topRightsystemOperation.btnonlineNum = "1";
        }
        var hasPlugin=(vPortal.plugins && $.inArray("mytool",vPortal.plugins)>-1);
        if(!hasPlugin){
            vPortal.themeHotspots.topRightsystemOperation.btnAbout = "0";
        }
        return vPortal.themeHotspots.topRightsystemOperation;
    },
    //在线人数
    onlineMember: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        onlineMember();
    },
    //个人中心
    showPersonCenter: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        showPersonCenter();
    },
    // 在线打卡
    onlineCardClick: function() {
        showMenu(_ctxPath + "/attendance/attendance.do?method=intoMyAttendance");
    },
    // 点击之后显示搜索框
    searchOpen: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        document.getElementById("searchContainer").style.display = "block";
        document.getElementById("searchAreaInput").focus();
    },
    searchClose: function() {
        var searchContainer = document.getElementById("searchContainer");
        if (searchContainer) {
            searchContainer.style.display = "none";
        }
    },
    // 按了回车键
    pressEnter: function(_inputId) {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        vPortalMainFrameElements.commonSearch.pressEnter(_inputId);
    },
    // 点了搜索按钮
    search: function(_inputId) {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        vPortalMainFrameElements.commonSearch.search(_inputId);
        vPortalMainFrameElements.topRightsystemOperation.searchClose();
    },
    // 唤醒致信
    msgClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        wakeZX();
    },
    //刷新
    refreshPageClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        window.onbeforeunload = null;
        outSysFlag = false;
        window.location.reload();
    },
    // 显示/隐藏换肤面板
    showSkinPanle: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        //避免点关闭报错，不能去掉
        vPortalMainFrameElements.topRightsystemOperation.progressBar =  $.progressBar();
        //第一次点击首页设置 -> 展开面板的时候，才去渲染skinSet元素
        if (vPortalMainFrameElements.skinSet.initState === false) {
            //改为同步请求
            var themeCategorys = callBackendMethod("portalThemeManager","getThemeCategorys");
            if(!vPortal.themeCategorys){
                vPortal.themeCategorys = themeCategorys;
            }
            var elementTplHtml = document.getElementById("tpl-skinSet").innerHTML;
            var _tempData = themeCategorys;
            var __tempData = isNaN(_tempData) ? _tempData : _tempData.toString();
            renderTpl(__tempData, elementTplHtml, "topSkinSet");
            vPortalMainFrameElements.skinSet.initState === true;
        }

        var result = callBackendMethod("portalManager", "getNavAndMenuCustomSwitch",vPortal.portalId);
        if(result && result.navCustomSwitch=="0"){
            $("#navSetId").hide();
        }else {
            $("#navSetId").show();
        }
        if(result && result.menuCustomSwitch=="0"){
            $("#menuSetId").hide();
        }else {
            $("#menuSetId").show();
        }
        var panleObj = document.getElementById("skin_set");
        var skin_content = document.getElementById("skin_content");
        if (panleObj.className.indexOf("display_none") !== -1) {
            panleObj.className = "skin_set_contaner";
            panleObj.style.display = "block";
            panleObj.style.height = "100%";
            panleObj.style.width = "560px";
            skin_content.innerHTML = '<iframe allowtransparency="true" src="'+_ctxPath+'/portal/portalController.do?method=skinSet&portalId=' + vPortal.portalId + '&themeId=' + vPortal.themeId + CsrfGuard.getUrlSurffix() + '" width="100%" height="100%" frameborder="0"></iframe>';
        }
        hideOfficeObj();
    },
    onlineServiceClick: function() {
        //加密狗中是够有插件
        if (vPortal.hasserviceEndDate == null || vPortal.hasserviceEndDate == undefined || vPortal.hasserviceEndDate == '') {
            $.alert($.i18n('onlineCustomerService.tips.not.purchased'));
            // 插件是否到期
        } else if (vPortal.hasserviceEndDate == "no") {
            $.alert($.i18n('onlineCustomerService.tips.expire'));
        } else {
            callBackendMethod("onlineCustomerServiceManager", "getCustomerServicePage", {
                success: function(data) {
                    if (data.tips != null) {
                        $.alert(data.tips);
                    } else {
                        window.open(data.url, "_blank", "width=780,height=490, top=100, left=380, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=no, status=no");
                    }
                },
                error: function(e) {
                    $.alert(e);
                }
            });
        }
    },
    // 个人设置
    mySetClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        var url = "/personalAffair.do?method=personCenter&read=myConfig";
        var html = "<span class='nowLocation_ico'><img src='"+_ctxPath+"/main/skin/frame/default/menuIcon/personal.png'/></span>";
        html += "<span class='nowLocation_content'>";
        html += "<a class=\"hand\" onclick=\"showMenu('" + _ctxPath +
            "/portal/portalController.do?method=personalInfo')\">" +
            $.i18n("menu.personal.affair") + "</a>";
        html += "</span>";
        showMenu(_ctxPath + url);
        hideLocation();

    },
    // 产品导航
    productViewClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        //内部关闭的时候会用到，只能放全局变量里
        getCtpTop().productView_Obj = $.dialog({
            id: 'productView',
            width: 1000,
            height: 640,
            checkMax: false,
            type: 'panel',
            url: _ctxPath + "/portal/portalController.do?method=showProductView",
            shadow: true,
            showMask: true,
            title: false,
            panelParam: {
                'show': false,
                'margins': false
            },
            closeParam: {
                show: false
            }
        });
    },
    // 薪资查看
    viewSalaryClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        var url = "/hrViewSalary.do?method=viewSalary";
        var html = "<i class='vportal vp-personal'></i>";
        html += "<span class='nowLocation_content'>";
        html += "<a class=\"hand\" onclick=\"showMenu('" + _ctxPath + "/portal/portalController.do?method=personalInfo')\">" + $.i18n("menu.personal.affair") + "</a>";
        html += " &gt; <a class=\"hand\" onclick=\"showMenu('" + _ctxPath + "/portal/portalController.do?method=personalInfoFrame&path=" + url + "')\">" + $.i18n("menu.hr.salary.show") + "</a>";
        html += "</span>";
        showLocation(html);
        showMenu(_ctxPath + "/portal/portalController.do?method=personalInfoFrame&path=" + url);
    },
    // 应用中心
    appcenterClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        var url = _ctxPath + "/mallindex.do";
        window.open(url, "appcenter_id");
    },
    // 关于
    showAbout: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        var dialog = $.dialog({
            id: "showAbout",
            url: _ctxPath + "/main.do?method=showAbout",
            width: 504,
            height: 303,
            title: $.i18n("product.about.title"),
            buttons: [{
                text: $.i18n("common.button.close.label"),
                handler: function() {
                    dialog.close();
                }
            }]
        });
    },
    // 退出
    logoutClick: function() {
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        if (vPortal.subPortal == "true") {
            var ok = confirm($.i18n('portal.element.topRightsystemOperation.alert1'));
            if (ok) {
                window.close();
            }
        } else {
            logout();
            //zhou
            // window.location.href=_ctxPath+"/logoutAuth";
        }
    },
    // 设计器中获取可设置属性
    getProp: function(_dataJson) {
        window.parent.changeRightTitle($.i18n('portal.hotspot.button.title.label'));
        var dataJson = _dataJson.topRightsystemOperation;
        var json = [{
            "groupName": $.i18n('portal.hotspot.button.iconshow.label'),
            "groupType": "",
            "groupValue": []
        }, {
            "groupName": $.i18n('portal.hotspot.button.menushow.label'),
            "groupType": "",
            "groupValue": []
        }];
        json[0].groupValue.push(
            {
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('portal.hotspot.button.appcenter.label'),//云应用中心
                    "id": "btnAppCenter",
                    "name": "btnAppCenter",
                    "checked": (dataJson && dataJson.btnAppCenter == '1') ? "on" : "off"
                }],
                "click": "clickFun"
            }
        );

        //在线人数  和html模板里面控制方式一致
        if(document.getElementById("onlineNumArea") == null){
            json[0].groupValue.unshift(
                {
                    "id": "",
                    "name": "",
                    "type": "checkbox",
                    "label": "",
                    "typeChoose": "default",
                    "value": [{
                        "label": $.i18n('portal.onlineNum.label1'),//在线人数
                        "id": "btnonlineNum",
                        "name": "btnonlineNum",
                        "checked": (dataJson && (dataJson.btnonlineNum == '1') || !dataJson.btnonlineNum) ? "on" : "off"
                    }],
                    "click": "clickFun"
                }
            )
        }


        //搜索
        if(vPortal.searchEnabled){
            json[0].groupValue.push( {
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('common.search.label'),
                    "id": "btnSearch",
                    "name": "btnSearch",
                    "checked": (dataJson && dataJson.btnSearch == '1' && vPortal.searchEnabled) ? "on" : "off"
                }],
                "click": "clickFun"
            });
        }
        //刷新
        json[0].groupValue.push({
            "id": "",
            "name": "",
            "type": "checkbox",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label": $.i18n('common.toolbar.refresh.label'),
                "id": "btnRefresh",
                "name": "btnRefresh",
                "checked": (dataJson && dataJson.btnRefresh == '1') ? "on" : "off"
            }],
            "click": "clickFun"
        });

        //致信
        if(vPortal.ucEnabled == 'true'){
            json[0].groupValue.push({
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('portal.hotspot.button.msg.label'),
                    "id": "btnMessage",
                    "name": "btnMessage",
                    "checked": (dataJson && dataJson.btnMessage == '1') ? "on" : "off"
                }],
                "click": "clickFun"
            });
        }

        //签到
        if (vPortal.cardEnabled) {
            json[0].groupValue.unshift({
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('common.sign.label'),//显示签到
                    "id": "btnSign",
                    "name": "btnSign",
                    "checked": (dataJson && dataJson.btnSign == '1') ? "on" : "off"
                }],
                "click": "clickFun"
            }, {
                "id": "btnSignStyle",
                "name": "btnSignStyle",
                "type": "select",
                "typeShow": "half",
                "label": $.i18n('portal.hotspot.button.signstyle.label'),//签到样式
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('portal.hotspot.button.signstyle1.label'),
                    "value": "text",
                    "select": (dataJson && dataJson.btnSignStyle == 'text') ? "on" : "off"
                }, {
                    "label": $.i18n('portal.hotspot.button.signstyle2.label'),
                    "value": "icon",
                    "select": (dataJson && dataJson.btnSignStyle == 'icon') ? "on" : "off"
                }],
                "click": ""
            });
        }
        //在线客服
        if (vPortal.OnlineCustomerServiceEnabled) {
            json[0].groupValue.push({
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('menu.onlineService.label'),//在线客服
                    "id": "btnonlineService",
                    "name": "btnonlineService",
                    "checked": (dataJson && dataJson.btnonlineService == '1') ? "on" : "off"
                }],
                "click": "clickFun"
            });
        }
        //首页设置
        json[1].groupValue.push({
            "id": "",
            "name": "",
            "type": "checkbox",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label": $.i18n('portal.changeSkin.lable'),
                "id": "btnHomeSet",
                "name": "btnHomeSet",
                "checked": (dataJson && dataJson.btnHomeSet == '1') ? "on" : "off"
            }],
            "click": "clickFun"
        });

        //产品导航
        /*if(vPortal.locale == "zh_CN" ){  //仅简体中文下展示'产品导航'，'产品导航'里全是图片，不适合国际化
            json[1].groupValue.push({
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('menu.productNavigation.label'),
                    "id": "btnProductNav",
                    "name": "btnProductNav",
                    "checked": (dataJson && dataJson.btnProductNav == '1') ? "on" : "off"
                }],
                "click": "clickFun"
            });
        }*/

        //薪资查看
        if(vPortal.salaryEnabled==true){
            json[1].groupValue.push({
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('menu.hr.salary.show'),
                    "id": "btnSalaryView",
                    "name": "btnSalaryView",
                    "checked": (dataJson && dataJson.btnSalaryView == '1') ? "on" : "off"
                }],
                "click": "clickFun"
            });
        }

        //个人设置
        json[1].groupValue.push({
            "id": "",
            "name": "",
            "type": "checkbox",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label": $.i18n('menu.personal.affair'),
                "id": "btnPersonalSet",
                "name": "btnPersonalSet",
                "disabled": true,
                "checked": "on"
            }],
            "click": "clickFun"
        });
        //cloud任务项,V5Cloud_应用_49-05,关于受我的工具的插件控制
        if (vPortal.plugins && $.inArray("mytool",vPortal.plugins)>-1) {
            //关于
            json[1].groupValue.push({
                "id": "",
                "name": "",
                "type": "checkbox",
                "label": "",
                "typeChoose": "default",
                "value": [{
                    "label": $.i18n('product.about.title'),
                    "id": "btnAbout",
                    "name": "btnAbout",
                    "checked": (dataJson && (!dataJson.btnAbout || dataJson.btnAbout == '1')) ? "on" : "off"
                }],
                "click": "clickFun"
            });
        }

        //退出
        json[1].groupValue.push({
            "id": "",
            "name": "",
            "type": "checkbox",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label": $.i18n('seeyon.top.close.alt'),
                "id": "btnOut",
                "name": "btnOut",
                "disabled": true,
                "checked": "on"
            }],
            "click": "clickFun"
        });

        return json;
    },
    onPropChange: function(json, id, key, value) {
        //签到
        if(key == "btnSign"){
            if (value == "1") {
                $("#" + id).find(".topIco").find(".signBtn").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".signBtn").hide();
            }
        }

        //签到样式
        if(key == "btnSignStyle"){
            if (value == "text") {
                $("#" + id).find(".topIco").find(".signBtn").find("span").show();
                $("#" + id).find(".topIco").find(".signBtn").find("i").hide();
            } else if (value == "icon") {
                $("#" + id).find(".topIco").find(".signBtn").find("span").hide();
                $("#" + id).find(".topIco").find(".signBtn").find("i").show();
            }
        }

        //在线人数
         if(key == "btnonlineNum"){
            if (value == "1") {
                $("#" + id).find(".topIco").find(".onlineNum").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".onlineNum").hide();
            }
        }

        //应用中心
        if(key == "btnAppCenter"){
            if (value == "1") {
                $("#" + id).find(".topIco").find(".appIco").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".appIco").hide();
            }
        }

        //搜索
        if(key == "btnSearch"){
            if (value == "1") {
                $("#" + id).find(".topIco").find(".searchIco").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".searchIco").hide();
            }
        }


        //刷新
        if(key == "btnRefresh"){
            if (value == "1") {
                $("#" + id).find(".topIco").find(".refreshIco").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".refreshIco").hide();
            }
        }

        //致信
        if(key == "btnMessage"){
            if (value == "1") {
                $("#" + id).find(".topIco").find(".messageIco").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".messageIco").hide();
            }
        }

        //在线客服
         if (key == "btnonlineService" && vPortal.OnlineCustomerServiceEnabled) {
            if (value == "1") {
                $("#" + id).find(".topIco").find(".onlineServiceIco").show();
            } else if (value == "0") {
                $("#" + id).find(".topIco").find(".onlineServiceIco").hide();
            }
        }
        //修改遮罩的位置
        if(key == "btnSign" || key == "btnSignStyle" || key == "btnonlineNum" || key == "btnAppCenter" || key == "btnSearch" || key == "btnRefresh" || key == "btnMessage" || key == "btnonlineService"){
            for (var j = 0; j < $(".commonShade").length; j++) {
                var tempCommonShade = $(".commonShade").eq(j);
                if($("#" + tempCommonShade.attr("data-id")).children().length > 0) {
                    window.parent.setCommonShadePos(tempCommonShade.attr("data-tpl"));
                }
            }
        }
        window.parent.putData(key, value, "topRightsystemOperation");
    },
    showHelp: function() {
        window.open("https://service.seeyon.com/docs/help/" + vPortal.helpProductVersion + "/" + vPortal.helpProductId + "/user.html");
    },
    // 布局设置和皮肤设置切换
    chooseOperate: function(_num) {
        var _contentWindow = document.getElementById("skin_content").querySelector("iframe").contentWindow;
        if (document.querySelector(".skin_top_bar_left").children[0].classList) {
            if (_num == 0) {
                document.querySelector(".skin_top_bar_left").children[0].classList.add("choosed");
                //document.querySelector(".skin_top_bar_left").children[1].classList.remove("choosed");
                _contentWindow.toggleSet(_num);
            } else if (_num == 1) {
                document.querySelector(".skin_top_bar_left").children[0].classList.remove("choosed");
                //document.querySelector(".skin_top_bar_left").children[1].classList.add("choosed");
                _contentWindow.toggleSet(_num);
            }
        } else {
            if (_num == 0) {
                document.querySelector(".skin_top_bar_left").children[0].setAttribute("class","padding_l_5 hand padding_r_5 choosed");
                //document.querySelector(".skin_top_bar_left").children[1].setAttribute("class", "padding_l_5 hand padding_r_5");
                _contentWindow.toggleSet(_num);
            } else if (_num == 1) {
                document.querySelector(".skin_top_bar_left").children[0].setAttribute("class", "padding_l_5 hand padding_r_5");
                //document.querySelector(".skin_top_bar_left").children[1].setAttribute("class","padding_l_5 hand padding_r_5 choosed");
                _contentWindow.toggleSet(_num);
            }
        }
    },
    spaceSet: function() {
        var dwidth = $(top).width();
        var dheight = $(top).height();
        var vPortalWindowDialog = $.dialog({
            targetWindow: top,
            id: 'spaceSetDialog',
            url: _ctxPath + "/portal/portalDesigner.do?method=vPortalEngineIndex&securityType=0&selected=space&from=dialog",
            width: dwidth,
            height: dheight,
            title: $.i18n('portal.spacesetting.0'),
            overflow: 'hidden',
            transParams: {
                vPortalWindowDialogObj: window
            },
            closeParam: {
                'show': true,
                handler: function() {
                    if (getCtpTop()["removeOnbeforeunload"] != null) {
                        getCtpTop()["removeOnbeforeunload"]();
                    }
                    getCtpTop().location.reload();
                }
            }
        });
    },
    showPersonContainer: function() {
        var _personContainer = document.getElementById("personContainer");
        _personContainer.style.display = "block";

        var _personContainer_iframe = document.getElementById("personContainer_iframe");
        _personContainer_iframe.style.display = "block";
        //只计算一次iframe的宽高
        if(_personContainer.getAttribute("calculated") == "true"){
            return;
        }
        _personContainer_iframe.style.width = _personContainer.offsetWidth + "px";
        _personContainer_iframe.style.height = (_personContainer.offsetHeight - 35) + "px";
        _personContainer.setAttribute("calculated","true");
        hideOfficeObj();
    },
    hidePersonContainer: function() {
        document.getElementById("personContainer").style.display = "none";
        document.getElementById("personContainer_iframe").style.display = "none";
        showOfficeObj();
    }
}