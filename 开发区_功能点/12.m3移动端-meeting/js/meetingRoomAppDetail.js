var urlParam;

/**
 * 接收参数描述
 * appId      申请ID
 */
cmp.ready(function () {
    urlParam = cmp.href.getParam();
    initPageBack();

    cmp.i18n.init(_meetingPath + "/i18n/", "MeetingResources", function () {
        initPageData();
        initEvent();
    }, meetingBuildVersion);
});

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}

function initEvent() {
    _$("#roomName").addEventListener("tap", function () {
        var paramData = {
            roomId: this.getAttribute("roomId")
        };
        cmp.href.next(_meetingPath + "/html/meetingRoomDetail.html" + meetingBuildVersion, paramData);
    });
    if (MeetingUtils.isCMPShell()) {
        _$("#appPerName").addEventListener("tap", function () {
            var perId = this.getAttribute("perId")
            cmp.href.next("http://my.m3.cmp/v/layout/my-person.html?page=search-next&id=" + perId + "&from=m3&enableChat=true" + meetingBuildVersion_and);
        });
    } else {
        _$("#appPerName").style.color = "#333333";
    }
}

function _goBack() {
    cmp.href.back();
}

function initPageData() {
    var paramData = {
        appId: urlParam.appId
    };

    $s.Meeting.getMeetingRoomAppDetail({}, paramData, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
                return;
            }

            document.title = result.roomName;
            _$("#startDatetime").innerHTML = result.startDatetime;
            _$("#endDatetime").innerHTML = result.endDatetime;
            _$("#roomName").innerHTML = result.roomName;
            _$("#appPerName").innerHTML = result.appPerName;
            _$("#appDepartment").innerHTML = result.appDepartment;
            _$("#description").innerHTML = result.description;
            //zhou:[开发区] 开始
            _$("#appSqrdh").innerHTML = result.sqrdh;
            _$("#appLdname").innerHTML = result.appLdname;
            _$("#hcyq").innerHTML = result.hcyq;
            //zhou:[开发区] 结束

            _$("#roomName").setAttribute("roomId", result.roomId);
            _$("#appPerName").setAttribute("perId", result.perId);
        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}