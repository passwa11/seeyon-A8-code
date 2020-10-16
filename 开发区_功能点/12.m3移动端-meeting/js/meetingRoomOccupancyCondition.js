var urlParam;
//格式化时间类型
var formatType = {
    a: "yyyy-MM-dd",
    b: "hh:mm",
    c: "yyyy-MM-dd hh:mm",
    d: "MM-dd hh:mm"
};
//计算时用到的参数
var time = {
    minHour: 0,
    maxHour: 24,
    a: "00:00",
    c: 2400,
    d: "24:00"
};
var pageX = {};
pageX.cache = {};
pageX.cache.datas = {};
//会议室占用情况页面跳转后返回所需数据
var cacheKey_mrocStorageDatas = "m3_v5_meeting_occupancyCondition";
//时间轴id
var timeLineId;
//新建会议页面缓存key
var cacheKey_mcBackDatas = "m3_v5_meeting_meetingCreate_backDatas";

var meetingSecurity = {};

// var scrollState = false;

/**
 * 接收参数描述
 * roomId      会议室ID
 * roomName    会议室名称
 * action      执行动作
 *     applyMeetingRoom   申请会议室
 *     createMeeting      新建会议
 * cacheKey_mrlStorgeDatas    缓存key，会议室列表查询条件，申请通过后需要清除此缓存
 * cacheKey_mcBackDatas       缓存key，申请会议室时，跳转过来后需要返回的数据
 */
cmp.ready(function () {
    //初始参数
    initPageParam();
    initPageBack();
    cmp.i18n.init(_meetingPath + "/i18n/", "MeetingResources", function () {
        setPageTitle(cmp.i18n("meeting.page.lable.title.room.condition"));
        initPageData();
        initEvent();
        //计算时间高度
        // _$('.cmp-scroll').style.height = _$('.cmp-content').offsetHeight  - 161 +'px';
        _$('.cmp-scroll').style.height = (_$('.cmp-content').offsetHeight - _$("#dateCalender").offsetHeight) - 60 + 'px';

        //OA-165608新建会议-选择会议室-会议占用情况没有预约按钮
        if (urlParam.action == "createMeeting") {
            _$("#apply").style.display = "none";
        }
        //加载会议权限配置
        initSecurity();
    }, meetingBuildVersion);
});

var initSecurity = function () {
    MeetingUtils.meetingSecurity(function (security) {
        meetingSecurity = security;
    });
}

function initPageParam() {
    urlParam = cmp.href.getParam();
}

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}

function _goBack() {
    //清空占用情况页面缓存
    cmp.storage["delete"](cacheKey_mrocStorageDatas, true);
    //从扫描进来的情况
    if (MeetingUtils.isFromQrCode()) {
        cmp.href.closePage();
    } else {
        cmp.href.back();
    }
}

function initPageData() {
    //初始日历
    getOrderDate();
}

/**
 * 加载会议室基本信息
 */
function initRoom() {
    $s.Meeting.meetingRoomState({}, {
        meetingRoomId: urlParam.roomId,
        startDate: getFirstTimeOfDay(pageX.cache.datas.selectDate),
        endDate: getLastTimeOfDay(pageX.cache.datas.selectDate)
    }, {
        success: function (result) {
            var roomInfo = result.data;
            urlParam.roomName = roomInfo.roomName;
            urlParam.roomNeedApp = roomInfo.roomNeedApp ? 1 : 0;

            _$("#title").innerHTML = roomInfo.roomName;
            _$("#roomSeatCount").innerHTML = cmp.i18n("meeting.meetingRoomList.seatCount", [roomInfo.roomSeatCount]);
            _$("#adminNames").innerHTML = cmp.i18n("meeting.meetingRoomList.admin") + " " + roomInfo.adminNames;
            /**
             * 占用情况
             */
            switch (roomInfo.applyState) {
                case "1" :
                    _$("#applyState").classList.remove("colorOrange");
                    _$("#applyState").classList.remove("colorRed");
                    _$("#applyState").classList.add("colorGreen");

                    _$("#applyStateIcon").classList.remove("colorOrange");
                    _$("#applyStateIcon").classList.remove("colorRed");
                    _$("#applyStateIcon").classList.add("colorGreen");

                    _$("#applyState").innerHTML = cmp.i18n("meeting.qrCode.todayFree");
                    break;
                case "2" :
                    _$("#applyState").classList.remove("colorGreen");
                    _$("#applyState").classList.remove("colorRed");
                    _$("#applyState").classList.add("colorOrange");

                    _$("#applyStateIcon").classList.remove("colorGreen");
                    _$("#applyStateIcon").classList.remove("colorRed");
                    _$("#applyStateIcon").classList.add("colorOrange");

                    _$("#applyState").innerHTML = cmp.i18n("meeting.qrCode.occupyApart");
                    break;
                case "3" :
                    _$("#applyState").classList.remove("colorGreen");
                    _$("#applyState").classList.remove("colorOrange");
                    _$("#applyState").classList.add("colorRed");

                    _$("#applyStateIcon").classList.remove("colorGreen");
                    _$("#applyStateIcon").classList.remove("colorOrange");
                    _$("#applyStateIcon").classList.add("colorRed");

                    _$("#applyState").innerHTML = cmp.i18n("meeting.qrCode.occupyAll");
                    break;
                default :
                    _$("#applyState").classList.remove("colorOrange");
                    _$("#applyState").classList.remove("colorRed");
                    _$("#applyState").classList.add("colorGreen");

                    _$("#applyStateIcon").classList.remove("colorOrange");
                    _$("#applyStateIcon").classList.remove("colorRed");
                    _$("#applyStateIcon").classList.add("colorGreen");

                    _$("#applyState").innerHTML = cmp.i18n("meeting.qrCode.todayFree");
            }
        },
        error: function (error) {
            MeetingUtils.dealError(error);
        }
    });
}

function initEvent() {
    //点击列表空白区域跳转至申请列表
    cmp("#timeList").on("tap", ".cell-item", function () {
        if (this.getAttribute("readonly")) {
            return;
        }
        timeLineId = this.getAttribute("id");
        if (urlParam.action === "createMeeting") {
            goApply();
        } else {
            openNewDialog();
        }
    });

    cmp("#timeList").on("tap", ".nextPage", function () {
        var paramData = {
            appId: this.getAttribute("appId")
        };
        cmp.event.trigger("beforepageredirect", document);
        cmp.href.next(_meetingPath + "/html/meetingRoomAppDetail.html" + meetingBuildVersion, paramData);
    });

    //点击详情，查看会议室详情
    _$("#showDetail").addEventListener("tap", goMeetingRoomDetail);

    //预约按钮
    _$("#apply").addEventListener("tap", function () {
        if (urlParam.action === "createMeeting") {
            goApply();
        } else {
            openNewDialog();
        }
    });

    //关闭按钮
    _$(".close").addEventListener("tap", function () {
        timeLineId = undefined;
        _$("#maskContainer").style.display = "none";
        _$(".occupancy-container").classList.remove("blur");
    });

    //新建会议按钮
    _$("#meetingCreate").addEventListener("tap", function () {
        if (!meetingSecurity.haveMeetingArrangeRole) {
            _alert("无权新建会议！");
            return;
        }
        meetingCreate();
    });

    //申请会议室按钮
    _$("#meetingApply").addEventListener("tap", function () {
        if (!meetingSecurity.haveMeetingRoomApp) {
            _alert("无会议室权限！");
            return;
        }
        goApply();
    });

    //跳转页面前执行的事件
    document.addEventListener('beforepageredirect', function (e) {
        _storagePageData();
    });
}

/**
 * 打开新建会议和会议室dialog
 */
function openNewDialog() {
    _$("#maskContainer").style.display = "block";
    _$(".occupancy-container").classList.add("blur");
}

function _initCalenderHight() {
    _$('.cmp-scroll').style.height = (_$('.cmp-content').offsetHeight - _$("#dateCalender").offsetHeight) - 60 + 'px';
}

//存储状态数据
function _storagePageData() {
    cmp.storage.save(cacheKey_mrocStorageDatas, cmp.toJSON(pageX.cache), true);
}

//跳转至会议室详情
function goMeetingRoomDetail() {
    var paramData = {
        roomId: urlParam.roomId
    };
    cmp.event.trigger("beforepageredirect", document);
    cmp.href.next(_meetingPath + "/html/meetingRoomDetail.html" + meetingBuildVersion, paramData);
}

/**
 * 跳转至申请页面
 */
function goApply() {
    var selectDate = getSelectDate(timeLineId);
    var startDatetime = selectDate.startDatetime;
    var endDatetime = selectDate.endDatetime;

    var paramData = {
        roomId: urlParam.roomId,
        roomName: urlParam.roomName,
        startDate: startDatetime,
        endDate: endDatetime,
        action: urlParam.action,
        roomNeedApp: urlParam.roomNeedApp,
        formChooseKey: urlParam.formChooseKey,
        cacheKey_mrlStorgeDatas: urlParam.cacheKey_mrlStorgeDatas,
        cacheKey_mcBackDatas: urlParam.cacheKey_mcBackDatas,
        cacheKey_mrocStorageDatas: cacheKey_mrocStorageDatas
    };
    cmp.event.trigger("beforepageredirect", document);
    cmp.href.next(_meetingPath + "/html/meetingRoomApply.html" + meetingBuildVersion, paramData);
}

/**
 * 跳转到新建会议页面
 */
function meetingCreate() {
    var selectDate = getSelectDate(timeLineId);
    var startDatetime = selectDate.startDatetime;
    var endDatetime = selectDate.endDatetime;

    var meetingCreateCache = {
        roomId: urlParam.roomId,
        roomName: urlParam.roomName,
        startDate: startDatetime,
        endDate: endDatetime,
        meetingPlace_type: "apply"
    };
    cmp.storage.save(cacheKey_mcBackDatas, cmp.toJSON(meetingCreateCache), true);
    cmp.href.next(_meetingPath + "/html/meetingCreate.html", {fromApp: 6});
}

/**
 * 获取时间轴上的时间
 * @param {Object} time
 */
var getSelectDate = function (time) {
    var startDate, endDate;
    if (time) {
        var hour = time.substr(0, 2);
        var minutes = time.substr(2, 3);

        var temp_selectDate = pageX.cache.datas.selectDate.split("-");
        startDate = new Date(temp_selectDate[0], parseInt(temp_selectDate[1]) - 1, temp_selectDate[2], hour, minutes, "00");
        endDate = new Date(Date.parse(startDate) + 60 * 60 * 1000);
    } else {
        var nowDate = new Date();
        var nowMinutes = nowDate.getMinutes();
        if (nowMinutes == 15 || nowMinutes == 30 || nowMinutes == 45 || nowMinutes == 0) {
            startDate = nowDate;
        } else {
            minutes = 15 - nowMinutes % 15;
            startDate = new Date(Date.parse(nowDate) + (60000 * minutes));
        }

        endDate = new Date(Date.parse(startDate) + (60 * 60 * 1000));
    }

    //判断时间是否符合要求
    checkData(startDate, endDate);

    var startDatetime = formatDate(pageX.startDate, formatType.c);
    var endDatetime = formatDate(pageX.endDate, formatType.c);

    return {
        startDatetime: startDatetime,
        endDatetime: endDatetime
    };
}

/**
 * 判断时间是否符合要求
 *
 * startDate  开始时间
 * endDate    结束时间
 */
function checkData(startDate, endDate) {
    var appliedArray = pageX.cache.appliedArray;
    var l_startDate = Date.parse(startDate);
    var l_endDate = Date.parse(endDate);

    for (var i = 0; i < appliedArray.length; i++) {
        var obj = appliedArray[i];
        //选择的开始时间大于等于占用的结束时间
        if (l_startDate >= obj.endDatetime) {
            continue;
        }
        //选择的开始时间在占用的时间段内
        if (l_startDate >= obj.startDatetime && l_startDate <= obj.endDatetime) {
            l_startDate = parseInt(obj.endDatetime);
            l_endDate = l_startDate + (60 * 60 * 1000);//结束时间按新的开始时间往后推一小时
            continue;
        }
        //选择的结束时间大于等于占用的开始时间，将选择的结束时间置为占用的开始时间
        if (l_endDate >= obj.startDatetime) {
            l_endDate = parseInt(obj.startDatetime);
        }
    }

    pageX.startDate = new Date(l_startDate);
    pageX.endDate = new Date(l_endDate);
}

//获取已经预订的会议室情况，返回已预订的会议室天数集合
function getOrderDate() {
    var params = {
        roomId: urlParam.roomId
    };
    $s.Meeting.getOrderDate({}, params, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
                return;
            }
            //调用日历组件
            dateCalender(result);
        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

/**
 * 获取会议室申请信息
 * roomId  会议室ID
 * _date   查询日期 (格式：yyyy-mm-dd)
 */
function getMeetingRoomApps(roomId, _date) {
    var params = {
        roomId: roomId,
        qDate: _date
    };

    $s.Meeting.getMeetingRoomApps({}, params, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
                return;
            }

            pageX.cache.appliedArray = result;

            //生成会议室初始列表HTML
            var defaultHtml = createDefaultHtml();
            //根据会议室占用情况生成浮层
            var occupancyHtml = createOccupancyHtml();

            _$("#timeList").innerHTML = defaultHtml + occupancyHtml;

            var spareHeight = 60;//0点距离日历组件的高度
            // var scrollinitState = true;
            if (pageX.cache.scrollHeight > spareHeight) {
                _$("#scrollArea").style["-webkit-overflow-scrolling"] = "auto";
                setTimeout(function () {
                    // _$("#scrollArea").scrollTop = pageX.cache.scrollHeight + 110 - spareHeight; //110是一个适当的高度，保持8点在顶部
                    _$('#scrollArea').scrollTop = new Date().getHours() * 72;
                    _$("#scrollArea").style["-webkit-overflow-scrolling"] = "touch";
                }, 1);
            }
            /*_$("#scrollArea").onscroll=function(e){
                if(!scrollinitState){
                    var target = _$("#scrollArea"),content= _$(".cmp-content"),head = _$("#headContainer");
                    if(target.scrollTop >0 && head.offsetHeight != 0){
                        content.style.top = "0px";
                        head.style.height = "0px";
                        target.style.height = target.offsetHeight + 74 + 'px';
                    }else if(target.scrollTop  == 0 && head.offsetHeight == 0){
                        content.style.top = "74px";
                        head.style.height = "74px";
                        target.style.height = target.offsetHeight - 74 + 'px';
                    }
                }
                scrollinitState = null;
            }*/
        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

//生成会议室初始列表HTML
function createDefaultHtml() {
    var tempHtml = "", //临时HTML代码
        fHours,        //格式化小时成2位数
        fMinutes,      //格式化的分钟数  两位
        tempTime;      //临时时间变量  hhmm格式

    var today = new Date();
    var hours = today.getHours() == 0 ? "00" : today.getHours() < 10 ? "0" + today.getHours() : today.getHours();
    var minutes = today.getMinutes() == 0 ? "00" : today.getMinutes() < 10 ? "0" + today.getMinutes() : today.getMinutes();

    var isPassTime = false;  //是否是过去的时间
    if (pageX.selectDateType == "0") {
        isPassTime = true;
    }

    for (var i = time.minHour; i < time.maxHour; i++) {  //小时
        var fHours = i < 10 ? "0" + i : i; //格式化小时成2位数

        tempHtml += "<div class=\"line-cell\">";
        tempHtml += "<div class=\"left-cell\"><span class=\"time\">" + fHours + ":00</span><span class=\"circle\"></span></div>";
        tempHtml += "<div class=\"right-cell\">";
        for (var j = 15; j <= 60; j = j + 15) {  // 15分钟刻度
            var tempTime = "" + fHours + j;

            if (isPassTime && (hours + ":" + minutes) != time.a) {
                tempHtml += "<p id=\"" + tempTime + "\" readonly=\"readonly\" class=\"cell-item unable\"></p>";
            } else {
                if (pageX.selectDateType == "-1") {
                    tempHtml += "<p id=\"" + tempTime + "\" readonly=\"readonly\" class=\"cell-item\"></p>";
                } else {
                    tempHtml += "<p id=\"" + tempTime + "\" class=\"cell-item\"></p>";
                }
            }

            if (pageX.selectDateType == "0" && isPassTime) {
                //刻度时间大于当前时间
                if (parseInt(tempTime) > parseInt("" + hours + minutes)) {
                    isPassTime = false;
                }
                //刻度时间等于当前时间
                if (j == 60 && parseInt((i + 1) + "00") == parseInt("" + hours + minutes)) {
                    isPassTime = false;
                }
                //刻度时间等于当前时间
                if (j != 60 && tempTime == ("" + hours + minutes)) {
                    isPassTime = false;
                }
            }
        }

        tempHtml += "</div>";
        tempHtml += "</div>";
    }

    //创建当前时间标识
    if (pageX.selectDateType == 0) {
        var topNum = 0;
        if (today.getMinutes() <= 15) {
            topNum = 17;
        } else if (today.getMinutes() <= 30) {
            topNum = 36;
        } else if (today.getMinutes() <= 45) {
            topNum = 57;
        } else if (today.getMinutes() <= 60) {
            topNum = 74;
        }
        tempHtml += '<div class="todayTimeDiv" style="top:' + (today.getHours() * 72 + topNum + 48) + 'px;"><span>' + hours + ':' + minutes + '</span><span></span><span></span></div>';
    }

    return listHeader() + tempHtml + listFooter();
}

//根据会议室占用情况生成浮层
function createOccupancyHtml() {
    //处理申请信息，获取时间段数组
    var appsInfo = getAppsInfo();
    var tempHtml = "<ul class=\"timeline-bglist\">";

    for (var i = 0; i < appsInfo.length; i++) {
        var appInfo = appsInfo[i];
        var _count = getCountBetweenTwoTime(appInfo);
        var height = _count.occupancyCount * 4 + (_count.occupancyCount) * 2;
        var top = _count.topCount * 4 + _count.topCount * 2 + 60;

        //计算最后的滑动值
        if (top < 576) {
            if (pageX.cache.scrollHeight == 0) {
                pageX.cache.scrollHeight = top;
            } else {
                if (pageX.cache.scrollHeight > top) {
                    pageX.cache.scrollHeight = top;
                }
            }
        }

        var status = appInfo.status;
        var backgroundColor = "abled";
        // 移动端取消申请中的状态判断
        /*if(status == 0){//判断会议是否审核通过
            backgroundColor = "able";  //
        }else if(status == 1){
            backgroundColor = "abled"; //蓝色
        }*/
        if (appInfo.finish) {//已过期
            backgroundColor = "overState";  //浅灰色
        }
        if (_count.occupancyCount < 6) {//小于半个小时只有色块
            tempHtml += "<li appId=\"" + appInfo.appId + "\" class=\"" + backgroundColor + " nextPage\" style=\"height:10px; top:" + top + "px;\">";
        } else {
            tempHtml += "<li appId=\"" + appInfo.appId + "\" class=\"" + backgroundColor + " nextPage\" style=\"height:" + height + "px; top:" + top + "px;\">";
        }
        if (_count.occupancyCount >= 6 && _count.occupancyCount < 12) {//显示单行 半个小时到1个小时
            tempHtml += "<p class=\"font-blueColor appInfoDescriptionP\">";
            tempHtml += "用途：" + appInfo.description == undefined ? "" : appInfo.description;
            tempHtml += "</p>";
        } else if (_count.occupancyCount >= 12) {//显示两行 大于一个小时
            tempHtml += "<p  class=\"font-blueColor appInfoDescriptionP\">用途：";
            tempHtml += appInfo.description == undefined ? "" : appInfo.description;
            tempHtml += "</p>";
            tempHtml += "<p  class=\"font-blueColor appInfoDescriptionP\">会场要求：";
            tempHtml += appInfo.hcyq == undefined ? "" : appInfo.hcyq;
            tempHtml += "</p>";
            tempHtml += "<p  class=\"font-blueColor\">(" + appInfo.sqrDept + ")" + appInfo.appPerName + "<span style='margin-left:10px;'>";
            tempHtml += appInfo.showTime + " ；电话：" + appInfo.sqrdh + "</span> </p>";

        }
        tempHtml += "</li>";
    }
    tempHtml += "</ul>";
    return tempHtml;
}

//获取时间刻度,返回占用刻度与距离8点的刻度
function getCountBetweenTwoTime(appInfo) {
    var startDatetime = appInfo.calcStartDatetime;
    var endDatetime = appInfo.calcEndDatetime;

    var sh = parseInt((startDatetime.split(":"))[0]);
    var sm = parseInt((startDatetime.split(":"))[1]);
    var eh = parseInt((endDatetime.split(":"))[0]);
    var em = parseInt((endDatetime.split(":"))[1]);

    var countDate = {
        occupancyCount: (eh - sh) * 12 + ((em - sm == 0) ? 0 : (em - sm) / 5),
        topCount: sh * 12 + ((sm == 0) ? 0 : sm / 5)
    };

    return countDate;
}

//获取下一个申请对象，取完时返回null字符串
/**
 * appInfo  申请信息
 * appIndex 获取申请信息的下标
 */
function getAppInfoByIndex(appInfo, appIndex) {
    if (appInfo[appIndex]) {
        return appInfo[appIndex];
    } else {
        return "null";
    }
}

//处理申请信息
function getAppsInfo() {
    var apps = new Array();

    var selectDate = pageX.cache.datas.selectDate;
    var selectDate_0 = Date.parse(new Date(selectDate.split("-")[0], parseInt(selectDate.split("-")[1]) - 1, selectDate.split("-")[2], 0, 0, 0));
    var selectDate_24 = Date.parse(new Date(selectDate.split("-")[0], parseInt(selectDate.split("-")[1]) - 1, selectDate.split("-")[2], 24, 0, 0));

    var appliedArray = pageX.cache.appliedArray;

    for (var i = 0; i < appliedArray.length; i++) {
        var obj = appliedArray[i];

        //当天无会议
        if (obj.endDatetime <= selectDate_0 || obj.startDatetime >= selectDate_24) {
            continue;
        }

        var startDatetime = formatDate(new Date(parseInt(obj.startDatetime)), formatType.b);
        if (startDatetime.split(":")[0] == "24") {
            startDatetime = "00:" + startDatetime.split(":")[1];
        }
        var endDatetime = formatDate(new Date(parseInt(obj.endDatetime)), formatType.b);
        if (endDatetime.split(":")[0] == "24") {
            if (endDatetime.split(":")[1] == "00") {
                endDatetime = time.d;
            } else {
                endDatetime = "00:" + endDatetime.split(":")[1];
            }
        }

        var showTime;
        if (obj.startDatetime < selectDate_0 || obj.endDatetime > selectDate_24) {
            showTime = formatDate(new Date(parseInt(obj.startDatetime)), formatType.d) + " - " + formatDate(new Date(parseInt(obj.endDatetime)), formatType.d);
        } else {
            showTime = startDatetime + " - " + endDatetime;
        }
        //开始时间早于当天，赋值零点
        if (obj.startDatetime < selectDate_0) {
            startDatetime = time.a;
        }
        //结束时间晚于当天，赋值24点
        if (obj.endDatetime > selectDate_24) {
            endDatetime = time.d;
        }

        var appInfo = {
            calcStartDatetime: startDatetime,
            calcEndDatetime: endDatetime,
            showTime: showTime,
            description: obj.description,
            appPerName: obj.appPerName,
            status: obj.status,
            appId: obj.appId,
            //zhou:[开发区]
            sqrdh: obj.sqrdh == null ? "" : obj.sqrdh,
            sqrDept: obj.sqrDept,
            hcyq: (obj.hcyq) == null ? "无" : obj.hcyq
        };

        apps.push(appInfo);
    }
    return apps;
}

//占用情况头部HTML
function listHeader() {
    var headerHtml = "<div class=\"line-cell\">";
    headerHtml += "<div class=\"left-cell\"></div>";
    headerHtml += "<div class=\"right-cell\" style=\"border-top:none;height: 40px;\"></div>";
    headerHtml += "</div>";
    return headerHtml;
}

//占用情况底部HTML
function listFooter() {
    var footerHtml = "<div class=\"line-cell\">";
    footerHtml += "<div class=\"left-cell\"><span class=\"time\">24:00</span><span class=\"circle\"></span></div>";
    footerHtml += "<div class=\"right-cell listFooterDiv\" style=\"height:40px;\"></div>";
    footerHtml += "</div>";
    return footerHtml;
}

/**
 * 日历组件
 * orderDate  已被预订的时间集合  格式：["2016-12-22","2016-12-26"]
 */
function dateCalender(orderDate) {
    var initParams = {
        // themeStyle : "timeArrange",
        orderD: orderDate,  //哪些天被预定
        uichangeToCallback: false,
        callback: calenderCallback
    }

    var cache = cmp.storage.get(cacheKey_mrocStorageDatas, true);
    if (cache) {
        cmp.storage["delete"](cacheKey_mrocStorageDatas, true);
        var temp_cache = cmp.parseJSON(cache);
        var selectDate = temp_cache.datas.selectDate;
        var temp_selectData = selectDate.split("-");

        initParams.y = parseInt(temp_selectData[0]);
        initParams.m = parseInt(temp_selectData[1]);
        initParams.d = parseInt(temp_selectData[2]);
    }

    new cmp.DateCalender("#dateCalender", initParams);
}

function calenderCallback(date) {
    if (!date.value) {
        return;
    }
    //初始化计算参数
    pageX.cache.scrollHeight = 576;

    setTimeout(function () {
        _initCalenderHight();
    }, 50);

    pageX.cache.datas.selectDate = date.value;

    isPassday(pageX.cache.datas.selectDate);
    //获取会议室申请列表
    getMeetingRoomApps(urlParam.roomId, pageX.cache.datas.selectDate);
    //刷新会议室状态
    initRoom();
}

//判断是否当天
function isPassday(date) {
    var tempDate = parseInt(date.replace(/-/g, ""));

    var time = new Date();
    var year = time.getFullYear(),
        month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
        _date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate();

    var tempToday = parseInt("" + year + month + _date);

    //选择的时间类型，-1：过去;0：当天;1：将来
    if (tempDate < tempToday) {
        pageX.selectDateType = "-1";
    } else if (tempDate == tempToday) {
        pageX.selectDateType = "0";
    } else {
        pageX.selectDateType = "1";
    }
}

//格式化时间
function formatDate(time, type) {
    if (type == formatType.a) {
        var year = time.getFullYear(),
            month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
            date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate();

        return year + "-" + month + "-" + date;
    } else if (type == formatType.b) {
        var hours = time.getHours() == 0 ? "24" : time.getHours() < 10 ? "0" + time.getHours() : time.getHours(),
            minutes = time.getMinutes() == 0 ? "00" : time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes();

        return hours + ":" + minutes;
    } else if (type == formatType.c) {
        var year = time.getFullYear(),
            month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
            date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate(),
            hours = time.getHours() == 0 ? "00" : time.getHours() < 10 ? "0" + time.getHours() : time.getHours(),
            minutes = time.getMinutes() == 0 ? "00" : time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes();

        return year + "-" + month + "-" + date + " " + hours + ":" + minutes;
    } else if (type == formatType.d) {
        var month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
            date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate(),
            hours = time.getHours() == 0 ? "00" : time.getHours() < 10 ? "0" + time.getHours() : time.getHours(),
            minutes = time.getMinutes() == 0 ? "00" : time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes();

        return month + "-" + date + " " + hours + ":" + minutes;
    }
}

/**
 * 获取指定日期的当天开始时间
 * @param {Object} date
 */
function getFirstTimeOfDay(date) {
    date = date ? /-|:/g.test(date) ? new Date(date.replace(/-/g, "/")) : new Date(parseInt(date)) : new Date();
    return date.format("yyyy-MM-dd") + " 00:00:00";
}

/**
 * 获取指定日期的当天结束时间
 * @param {Object} date
 */
function getLastTimeOfDay(date) {
    date = date ? /-|:/g.test(date) ? new Date(date.replace(/-/g, "/")) : new Date(parseInt(date)) : new Date();
    return date.format("yyyy-MM-dd") + " 23:59:59";
}
