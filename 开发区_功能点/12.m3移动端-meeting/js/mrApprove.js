var cacheKey_mcStorageDatas = "m3_v5_meeting_room_approve_datas"; //缓存当前已经录入的数据，离开页面返回后回现
var urlParam;
var page = {};
var pageX = {};
pageX.cache = {};
/*
 * 会议室审核js
 */
cmp.ready(function () {

    urlParam = cmp.href.getParam();
    if (urlParam.isFromM3NavBar && urlParam.isFromM3NavBar == "true") {
        cmp.storage.save("isFromM3NavBar", true, true);
    }
    initPageBack();

    cmp.i18n.init(_meetingPath + "/i18n/", "MeetingResources", function () {
        initPageTitle();
        _$("#commentContent").setAttribute("placeholder", cmp.i18n('meeting.meetingRoom.pleaseContent'));
        //初始化数据
        initPageData();
        //初始化会议点击事件
        initEvent();

    }, meetingBuildVersion);

});

function initPageTitle() {
    if ("mrApproveList" == urlParam.openFrom) {
        //会议室申请
        setPageTitle(cmp.i18n("meeting.page.lable.title.room.approve"));
    } else {
        //会议室审核
        setPageTitle(cmp.i18n("meeting.meetingRoom.mrApprove"));
    }
}

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_isClearGoBack);
}

function _isClearGoBack() {
    setListViewRefresh("false");
    _goBack();
}

function _goBack() {
    if (MeetingUtils.getBackURL() == "weixin") {
        //返回到外层, 微信入口逻辑，因为微信没办法返回到首页，所以这样处理， 暂时不要和else分支合并
        cmp.href.closePage();
    } else {
        //返回到外层
        cmp.href.back();
    }
}

function initPageData() {
    $s.Meeting.getMeetingRoomApp({}, urlParam, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                _alert(result["errorMsg"], function () {
                    _goBack();
                });
                return;
            }

            _$("#startDatetime").innerHTML = result.startDatetime.substr(0, 10);
            _$("#startDatetimeLabel").innerHTML = result.startDatetime.substr(11, 5);
            _$("#endDatetime").innerHTML = result.endDatetime.substr(0, 10);
            _$("#endDatetimeLabel").innerHTML = result.endDatetime.substr(11, 5);
            _$("#roomName").innerHTML = result.roomName;
            _$("#appPerName").innerHTML = result.appPerName;
            _$("#description").innerHTML = result.description;
            _$("#meetResources").innerHTML = result.meetingResources;
            _$("#hcyq").innerHTML = result.hcyq;
            _$("#ldname").innerHTML = result.ldname;
            _$("#sqrdept").innerHTML = result.sqrdept;
            _$("#sqrdh").innerHTML = result.sqrdh;

            var cache = cmp.storage.get(cacheKey_mcStorageDatas, true);
            if (cache != null) {
                pageX.cache = cmp.parseJSON(cache);
                _$("#commentContent").innerHTML = pageX.cache.commentContent;
                cmp.storage["delete"](cacheKey_mcStorageDatas, true);
            }

            var auditPerName = "";
            for (var key in result.adminLab) {
                auditPerName += "<a class='showMemberCard' id='" + key + "' href='javascript:void(0)'>" + result.adminLab[key] + "</a>,";

            }
            _$("#auditPerName").innerHTML = auditPerName.substring(0, auditPerName.length - 1);
            page.roomId = result.roomId;
            page.roomAppId = result.roomAppId;
            //会议室所属会议信息（用于提前结束）
            page.meetingId = result.meetingId;
            page.meetingName = result.meetingName;

            page.appPerId = result.appPerId;


            var showFooterDiv1 = false;
            var showFooterDiv2 = false;
            //能否撤销判断
            if (result.cancelMRApp) {
                _$("#cancel").style.display = "table-cell";
                showFooterDiv1 = true;
            } else {
                _$("#cancel").style.display = "none";
            }
            //能否提前结束判断
            if (result.isFinishMRApp) {
                _$("#finish").style.display = "table-cell";
                showFooterDiv1 = true;
            } else {
                _$("#finish").style.display = "none";
            }
            //审核列表判断按钮显示
            if (urlParam.openFrom == "mrAuditList" && result.appStatus == 0) {
                _$("#disagree").style.display = "table-cell";
                _$("#agree").style.display = "table-cell";
                showFooterDiv2 = true;
            } else if (urlParam.openFrom != "mrAuditList" || (result.appStatus != 0 && urlParam.openFrom == "mrAuditList")) { //当前不是审核页面，或者已经审核了的数据，
                _$("#contentTH").innerHTML = result.permDescription;
            }
            if (!showFooterDiv1 && !showFooterDiv2) {
                _$("#footerDiv").style.display = "none";
            }

            //申请人在已申请不显示、在审核页面显示
            if (urlParam.openFrom != "mrAuditList") {
                _$("#roomSentPer").style.display = "none";
                _$("#roomAuditPer").style.display = "block";
            }

            //会议用品不为空显示
            if (result.meetingResources != "") {
                _$("#meetResource").style.display = "block";
            }

            resizeHight();

        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result, cmp.i18n("meeting.page.alert.meetingroomAppCancle"), _goBack);
        }
    });

}

function initEvent() {
    _$("#roomName").addEventListener("tap", fnGoMeetingRoomInfo);
    _$("#disagree").addEventListener("tap", noAgreeSubmit);
    _$("#agree").addEventListener("tap", agreeSubmit);

    _$("#cancel").addEventListener("tap", function () {
        var paramData = {
            "roomAppId": page.roomAppId,
            "openType": urlParam.openType
        }

        cmp.href.next(_meetingPath + "/html/meetingCancel.html" + meetingBuildVersion, paramData);
    });
    _$("#finish").addEventListener("tap", finishSubmit);
    if (_$("#commentContent")) {
        _$("#commentContent").addEventListener("input", fnFontCount);
    }

    //添加发起人员卡片
    _$("#appPerName").addEventListener("tap", function () {
        saveCacheKey_mcStorageDatas();
        if (/^-?\d+$/.test(page.appPerId)) {
            cmp.visitingCard(page.appPerId);
        }
    });

    cmp("#roomAuditPer").on("tap", ".showMemberCard", function () {
        saveCacheKey_mcStorageDatas();
        if (/^-?\d+$/.test(this.id)) {
            cmp.visitingCard(this.id);
        }
    });

    //监听横竖屏切换
    cmp.event.orientationChange(function (res) {
        /*if(res == "landscape"){ //横屏

        }else if(res == "portrait"){ //竖屏

        }*/
        resizeHight();
    });

}

function fnGoMeetingRoomInfo() {
    var paramData = {
        "roomId": page.roomId
    }
    saveCacheKey_mcStorageDatas();
    cmp.href.next(_meetingPath + "/html/meetingRoomDetail.html" + meetingBuildVersion, page);
}

function finishSubmit() {
    page.isContainMeeting = "false";
    if (page.meetingId != null) {
        //该会议室有绑定会议
        cmp.notification.confirm(
            cmp.i18n("meeting.page.confirm.mrSureFinish1", page.meetingName), function (index) { //点击按钮的回调函数
                if (index == 0) { //取消按钮
                    //do something
                } else if (index == 1) { //确定按钮
                    page.isContainMeeting = "true";
                    doFinishSubmit();
                }
            }, "", [cmp.i18n("meeting.page.action.cancle"), cmp.i18n("meeting.page.dialog.OK")]);
    } else {
        cmp.notification.confirm(
            cmp.i18n("meeting.page.confirm.mrSureFinish2"), function (index) { //点击按钮的回调函数
                if (index == 0) { //取消按钮
                    //do something
                } else if (index == 1) { //确定按钮
                    doFinishSubmit();
                }
            }, "", [cmp.i18n("meeting.page.action.cancle"), cmp.i18n("meeting.page.dialog.OK")]);
    }
}

function doFinishSubmit() {
    var parm = {
        "roomAppId": page.roomAppId,
        "isContainMeeting": page.isContainMeeting
    };
    $s.Meeting.finishMeetingRoom({}, parm, {
        success: function (result) {
            if (result["success"]) {
                //触发所有webview刷新事件
                MeetingUtils.fireAllWebviewEvent();
                _goBack();
            } else {
                cmp.notification.alert(cmp.i18n("meeting.meetingRoom.fail"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
            }
        },
        error: function (result) {
            //解除各按钮的绑定
            _removeEvent();
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

function noAgreeSubmit() {
    page.permStatus = 2;
    submit();
}

function agreeSubmit() {
    page.permStatus = 1;
    submit();
}

//加上放重复提交控制
var isAuditProcessing = false;

function submit() {
    if (isAuditProcessing) {
        return;
    }
    isAuditProcessing = true;

    page.description = _$("#commentContent").value;
    cmp.dialog.loading(cmp.i18n("meeting.mrApprove.dialog.msg1"));
    $s.Meeting.finishAuditMeetingRoom({}, page, {
        success: function (result) {
            cmp.dialog.loading(false);
            isAuditProcessing = false;
            if (result) {
                //触发所有webview刷新事件
                MeetingUtils.fireAllWebviewEvent();
                if (urlParam.openType == "todo") {
                    cmp.href.back();
                } else {
                    if (MeetingUtils.isFromM3NavBar()) {
                        cmp.href.closePage();
                    } else {
                        /**
                         * 添加判断如果是ipad就关闭webview
                         */
                        cmp.href.isInDetailPad(function (ret) {
                            if (ret) {
                                cmp.href.closePage();
                            } else {
                                cmp.href.replaceNode(_meetingPath + "/html/meetingRoomAdminApproveList.html");
                            }
                        });
                    }
                }
            }
        },
        error: function (result) {
            cmp.dialog.loading(false);
            isAuditProcessing = false;
            //解除各按钮的绑定
            _removeEvent();
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

function fnFontCount() {
    var feedback = _$("#commentContent");
    var content = getTextDealComment();
    if (content.length > 85) {
        feedback.value = content.substr(0, 85);
        content = feedback.value;
    }
}

//获取意见内容
function getTextDealComment() {
    var tValue = MeetingUtils.filterUnreadableCode(_$("#commentContent").value);
    return tValue;
}

function resizeHight() {
    var hh = window.innerHeight;
    document.getElementById("contentDiv").style.height = hh - _$("#footerDiv").offsetHeight - 15 + "px";
}

function _removeEvent() {
    _$("#disagree").removeEventListener("tap", noAgreeSubmit);
    _$("#agree").removeEventListener("tap", agreeSubmit);
}

function saveCacheKey_mcStorageDatas() {
    if (_$("#commentContent")) {
        pageX.cache.commentContent = _$("#commentContent").value;
        cmp.storage.save(cacheKey_mcStorageDatas, cmp.toJSON(pageX.cache), true);
    }
}