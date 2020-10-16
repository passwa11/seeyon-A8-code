var urlParam;
var cacheKey_mrAppParams = "m3_v5_meeting_meetingModify_meetingRoomAppQueryParams";

/**
 * 接收参数描述
 * startDate   开始时间    yyyy-MM-dd
 * endDate     结束时间    yyyy-MM-dd
 * action      执行动作
 *     applyMeetingRoom   申请会议室
 *     createMeeting      新建会议
 *     formChoose          自定义控件选择会议室
 * roomId      会议室ID
 * cacheKey_mrlStorgeDatas    缓存key，会议室列表查询条件，申请通过后需要清除此缓存
 * cacheKey_mrocStorageDatas  缓存key，占用情况存储的数据，申请后需要清除
 * cacheKey_mcBackDatas       缓存key，申请会议室时，跳转过来后需要返回的数据
 */

//会议室界面tab页签缓存key
var mrlistKey = "m3_v5_meeting_room_list_type";

cmp.ready(function () {
    urlParam = cmp.href.getParam();
    initPageBack();
    cmp.i18n.init(_meetingPath + "/i18n/", "MeetingResources", function () {
        setPageTitle(cmp.i18n("meeting.meetingRoomApply.application"));
        initPageData();
        _$("#conferees").addEventListener("tap", function () {
            MeetingUtils.selectOrg("conferees", setConfereesValue, {showBusinessOrganization: true})
        });
        initEvent();
    }, meetingBuildVersion);
});

//设置与会人值
function setConfereesValue(result) {
    console.log(result);
    var tempHtml = "";
    for (var i = 0; i < result.length; i++) {
        tempHtml += result[i].type + "|" + result[i].id + ",";
    }
    tempHtml = tempHtml.substr(0, tempHtml.length - 1);
    console.log(tempHtml, "tempHtml");
    _$("#conferees_value").value = tempHtml;
}

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}

function _goBack() {
    //zhou
    //删除选人组件使用的缓存
    MeetingUtils.clearSelectOrgCache();
    cmp.storage["delete"](cacheKey_mrAppParams, true);
    cmp.href.back();
}

function initPageData() {
    setAttValue("conferees", "placeholder", cmp.i18n("meeting.meetingCreate.inputConferees"));
    _$("#romeName").innerHTML = urlParam.roomName;
    _$("#startDate").value = urlParam.startDate.substr(0, 10);
    _$("#startDatetimeLabel").value = urlParam.startDate.substr(11, 5);
    _$("#endDate").value = urlParam.endDate.substr(0, 10);
    _$("#endDatetimeLabel").value = urlParam.endDate.substr(11, 5);

    if (urlParam.action == "createMeeting" || urlParam.action == "formChoose") {
        _$(".apply_wrap").remove();
    } else {
        setAttValue("description", "placeholder", cmp.i18n("meeting.meetingRoomApply.inputDescription"));
    }
}

function initEvent() {
    cmp.event.click(document.querySelector("#applyBtn"), submitForm);

    cmp.event.click(document.querySelector("#showStartDate"), cmpData_start);
    cmp.event.click(document.querySelector("#showEndDate"), cmpData_end);
}

function cmpData_start() {
    cmpData("startDate");
}

function cmpData_end() {
    cmpData("endDate");
}

//调用日期组件
function cmpData(id) {
    var options = {
        type: "",
        MinutesScale: "5",
        value: _$("#" + id).value + " " + _$("#" + id + "timeLabel").value
    };
    var picker = new cmp.DtPicker(options);
    picker.show(function (rs) {
        _$("#" + id).value = rs.value.substr(0, 10);
        _$("#" + id + "timeLabel").value = rs.value.substr(11, 5);
        picker.dispose();
    });
}

function radioChange(val) {
    switch (val) {
        case 0:
            _$("#confereesCell").style.display = "none";
            //删除选人组件使用的缓存
            MeetingUtils.clearSelectOrgCache();
            cmp.storage["delete"](cacheKey_mrAppParams, true);

            break;
        case 1:
            _$("#confereesCell").style.display = "block";
            break;

        default:
            _$("#confereesCell").style.display = "none";

    }
}

var isSubmit = false;

function submitForm() {
    //防止重复点击
    if (isSubmit) {
        return;
    }
    isSubmit = true;

    var action = urlParam.action;
    var p_startDate = _$("#startDate").value + " " + _$("#startDatetimeLabel").value;
    var p_endDate = _$("#endDate").value + " " + _$("#endDatetimeLabel").value;

    if (action == "createMeeting") {
        if (Date.parse(new Date()) > Date.parse(new Date(p_startDate.replace(/\-/g, "/")))) {
            createAlter(cmp.i18n("meeting.meetingRoomList.beforeNowTime"));
            isSubmit = false;
            return;
        }

        var meetingCreateCache = {
            roomId: urlParam.roomId,
            roomName: urlParam.roomName,
            startDate: p_startDate,
            endDate: p_endDate,
            meetingPlace_type: "apply"
        };
        cmp.storage.save(urlParam.cacheKey_mcBackDatas, cmp.toJSON(meetingCreateCache), true);

        //清空查询条件缓存
        cmp.storage["delete"](urlParam.cacheKey_mrlStorgeDatas, true);
        //清空占用情况页面缓存
        cmp.storage["delete"](urlParam.cacheKey_mrocStorageDatas, true);

        cmp.href.back(3);
        return;
    } else if (action == "formChoose") {
        if (Date.parse(new Date()) > Date.parse(new Date(p_startDate.replace(/\-/g, "/")))) {
            createAlter(cmp.i18n("meeting.meetingRoomList.beforeNowTime"));
            isSubmit = false;
            return;
        }

        //清空查询条件缓存
        cmp.storage["delete"](urlParam.cacheKey_mrlStorgeDatas, true);
        //清空占用情况页面缓存
        cmp.storage["delete"](urlParam.cacheKey_mrocStorageDatas, true);

        var cacheValue = {};
        var roomId = urlParam.roomId;
        var roomNeedApp = urlParam.roomNeedApp;
        var display = urlParam.roomName + "(" + p_startDate + "--" + p_endDate + ")";//表单展示值
        var value = urlParam.roomName + "," + roomNeedApp + "," + p_startDate + "," + p_endDate + "," + roomId;//表单保存值
        cacheValue.display = display;
        cacheValue.value = value;
        cmp.storage.save(urlParam.formChooseKey, cmp.toJSON(cacheValue), true);

        if (cmp.platform.CMPShell) {
            //触发M3webview事件
            cmp.webViewListener.fire({
                type: "webview_event_chooseMeetingRoom",
                data: {
                    display: display,
                    value: value
                },
                success: function () {
                },
                error: function (error) {
                    console.log(error);
                }
            });
            setTimeout(function () {
                cmp.href.closePage();
            }, 500);
        } else {
            cmp.href.back(3);
        }
        return;
    }

    var emojiUtil = cmp.Emoji();
    var discription = emojiUtil.EmojiToString(_$("#description").value);
    var hcyq = emojiUtil.EmojiToString(_$("#hcyq").value);

    var ldname = _$("#conferees").value;
    var ldid = _$("#conferees_value").value;

    var paramData = {
        roomId: urlParam.roomId,
        description: discription,
        startDatetime: p_startDate,
        endDatetime: p_endDate,
        //zhou:【开发区添加字段】 开始
        sqrdh: _$("#sqrdh").value,
        sfygwhldcj: _$("#sfygwhldcj:checked").value,
        hcyq: hcyq,
        ldid: ldid,
        ldname: ldname
        //zhou:【开发区添加字段】 结束
    };

    var temp_empty = checkEmpty();
    if (typeof (temp_empty) != "undefined") {
        createAlter(temp_empty);
        isSubmit = false;
        return;
    }

    //用于计算的变量
    var temp_startDate = Date.parse(new Date(p_startDate.replace(/\-/g, "/")));
    var temp_endDate = Date.parse(new Date(p_endDate.replace(/\-/g, "/")));
    var temp_now = Date.parse(new Date());

    //校验时间
    if (temp_startDate >= temp_endDate) {
        createAlter(cmp.i18n("meeting.meetingRoomApply.checkTime"));
        isSubmit = false;
        return;
    }
    if (temp_startDate <= temp_now) {
        createAlter(cmp.i18n("meeting.meetingRoomApply.startTimeError"));
        isSubmit = false;
        return;
    }

    $s.Meeting.execApp({}, paramData, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                createAlter(result["errorMsg"], null);
                isSubmit = false;
                return;
            }
            //触发所有webview刷新事件
            MeetingUtils.fireAllWebviewEvent();

            //删除选人组件使用的缓存
            MeetingUtils.clearSelectOrgCache();
            cmp.storage["delete"](cacheKey_mrAppParams, true);

            //清空查询条件缓存
            cmp.storage["delete"](urlParam.cacheKey_mrlStorgeDatas, true);
            //清空占用情况页面缓存
            cmp.storage["delete"](urlParam.cacheKey_mrocStorageDatas, true);

            if (result["roomAppState"]) {
                try {
                    var message;
                    if (result["roomAppState"] == 0) {  // 待审核
                        message = cmp.i18n("meeting.meetingRoomApply.publish1");
                    } else if (result["roomAppState"] == 1) { // 审核通过
                        message = cmp.i18n("meeting.meetingRoomApply.publish");
                    }
                    createAlter(message, function () {
                        if (MeetingUtils.isFromM3NavBar()) {
                            cmp.href.closePage();
                        } else {
                            setTimeout(function () {
                                cmp.storage.save(mrlistKey, "mrApproveList", true);
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
                            }, 1)
                        }
                    });
                } catch (err) {
                    alert(err);
                }
            }
        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

function createAlter(msg, callback) {
    cmp.notification.alert(msg, callback, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
}

//检查必录项
function checkEmpty() {
    var empty = cmp.i18n("meeting.meetingRoomApply.empty");

    var startDate = _$("#startDate").value;
    if (typeof (startDate) == "undefined" || startDate == "") {
        return cmp.i18n("meeting.meetingRoomApply.startTime") + empty;
    }

    var endDate = _$("#endDate").value;
    if (typeof (endDate) == "undefined" || endDate == "") {
        return cmp.i18n("meeting.meetingRoomApply.endTime") + empty;
    }

    var sqrdh = _$("#sqrdh").value;
    if (typeof (sqrdh) == "undefined" || sqrdh == "") {
        return "申请人电话" + empty;
    }


}

function setAttValue(id, type, value) {
    document.getElementById(id).setAttribute(type, value);
}