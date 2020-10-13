var urlParam;
var meetingRoomImgs = [];
/**
 * 接收参数描述
 * roomId      会议室ID
 */
cmp.ready(function () {
    urlParam = cmp.href.getParam();
    initPageBack();
    cmp.i18n.init(_meetingPath + "/i18n/", "MeetingResources", function () {
        setPageTitle(cmp.i18n("meeting.meetingRoomDetail.detail"));
        initPageData();
    }, meetingBuildVersion);
    //页面事件
    initPageEvent();
});

function initPageBack() {

    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_isClearGoBack);
}

function _isClearGoBack() {
    setListViewRefresh("true");
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

/**
 * 页面事件
 */
var initPageEvent = function () {
    //大图查看
    cmp("#banners").on("tap", "img", function () {
        var index = this.attributes['index'].nodeValue;
        cmp.imageViewer(meetingRoomImgs, index);
    });

    //zhou:[开发区修改会议室名称]
    _$("#sureUpdateMeetingRoom").addEventListener("tap", function () {
        var v=_$("#name").value;
        var paramData = {roomId: v+"",roomAppId: urlParam.roomAppId};
        $s.Meeting.updateMeetingRoom(undefined, paramData, {
            success: function (result) {
                if(result.code==0){
                    cmp.notification.toast('修改成功！','center','',0);
                }else {
                    cmp.notification.toast('修改失败！','center','',0);
                }
            },
            error: function (result) {
                //处理异常
                MeetingUtils.dealError(result);
            }
        });
    });
}

function selectedEvent(){
    var v=_$("#name").value;
    var paramData = {roomId: v+"",roomAppId: urlParam.roomAppId};
    $s.Meeting.getMeetingRoom(undefined, paramData, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
                return;
            }
            _$("#seatCount").innerHTML = result.meetingRoom.seatCount ? result.meetingRoom.seatCount : '';
            _$("#place").innerHTML = result.meetingRoom.place ? result.meetingRoom.place : '';
            _$("#description").innerHTML = result.meetingRoom.description ? result.meetingRoom.description : '';
            _$("#device").innerHTML = result.meetingRoom.eqdescription ? result.meetingRoom.eqdescription : '';
            _$("#adminNames").innerHTML = result.adminNames ? result.adminNames : '';

            //初始化会议图片轮播
            initBanner(result.attatchImage);

            cmp.listView("#scroller");
        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

function initPageData() {
    var paramData = {roomId: urlParam.roomId, roomAppId: urlParam.roomAppId};
    $s.Meeting.getMeetingRoom(undefined, paramData, {
        success: function (result) {
            if (result["errorMsg"] && result["errorMsg"] != "") {
                cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
                return;
            }

            var roomList = result.roomList;
            var selectHtml = "";
            for (let i = 0; i < roomList.length; i++) {
                if(roomList[i].id==result.meetingRoom.id){
                    selectHtml +="<option value='"+roomList[i].id+"' selected>"+roomList[i].name+"</option>";
                }else{
                    selectHtml +="<option value='"+roomList[i].id+"'>"+roomList[i].name+"</option>";
                }
            }

            // _$("#name").innerHTML = result.meetingRoom.name ? result.meetingRoom.name : '';
            _$("#name").innerHTML = selectHtml;
            _$("#seatCount").innerHTML = result.meetingRoom.seatCount ? result.meetingRoom.seatCount : '';
            _$("#place").innerHTML = result.meetingRoom.place ? result.meetingRoom.place : '';
            _$("#description").innerHTML = result.meetingRoom.description ? result.meetingRoom.description : '';
            _$("#device").innerHTML = result.meetingRoom.eqdescription ? result.meetingRoom.eqdescription : '';
            _$("#adminNames").innerHTML = result.adminNames ? result.adminNames : '';

            //初始化会议图片轮播
            initBanner(result.attatchImage);

            cmp.listView("#scroller");
        },
        error: function (result) {
            //处理异常
            MeetingUtils.dealError(result);
        }
    });
}

/**
 * 初始化会议图片轮播
 * @param {Object} attachments
 */
var initBanner = function (attachments) {
    showAttachments = attachments || [];

    /**
     * 根据屏幕宽度计算banner高度以适配图片展示
     * 图片是16:9尺寸（400*225）
     */
    var width = document.querySelector("#banners").offsetWidth;
    document.querySelector("#banners").style.height = (width * (9 / 16)) + "px";

    var thirdSessionId = window.localStorage.getItem("thirdSessionId");
    thirdSessionId = thirdSessionId != undefined ? thirdSessionId : '';
    var mpToken = window.localStorage.getItem("mpToken");
    mpToken = mpToken != undefined ? mpToken : '';
    //OA-171237 移动端查看会议室图片，图片没有显示出来. 小程序上没有传token导致后端图片组件拒绝访问，本来打算在后端开放小程序访问权限的，但是社区里说IOS没法识别小程序身份，这个作为应急方案
    var suffix = "thirdSessionId=" + encodeURIComponent(thirdSessionId) + "&mpToken=" + mpToken;

    for (var i = 0; i < showAttachments.length; i++) {
        meetingRoomImgs.push({
            name: showAttachments[i].filename,
            url: cmp.serverIp + "/seeyon/rest/commonImage/showImage?id=" + showAttachments[i].fileUrl + "&size=custom&w=400&h=255&" + suffix,
            small: cmp.serverIp + "/seeyon/rest/commonImage/showImage?id=" + showAttachments[i].fileUrl + "&size=auto&" + suffix,
            big: cmp.serverIp + "/seeyon/rest/commonImage/showImage?id=" + showAttachments[i].fileUrl + "&size=source&" + suffix
        });
    }
    //没有图片时显示默认图片
    if (meetingRoomImgs.length === 0) {
        meetingRoomImgs.push({
            name: "default",
            url: _meetingPath + "/img/defaultMeetingRoom.jpg",
            small: _meetingPath + "/img/defaultMeetingRoom.jpg",
            big: _meetingPath + "/img/defaultMeetingRoom.jpg"
        });
    }

    //加载轮播组件(最多9张)
    loadBanner(meetingRoomImgs.slice(0, 9));
}

/**
 * 加载轮播图
 * @param {Object} imgs
 */
var loadBanner = function (imgs) {
    var tpl_img = document.querySelector("#bannerTpl_img").innerHTML;
    var tpl_indicator = document.querySelector("#bannerTpl_indicator").innerHTML;

    var html_img = cmp.tpl(tpl_img, imgs);
    var html_indicator = cmp.tpl(tpl_indicator, imgs);

    var container_img = document.querySelector("#banners .cmp-slider-group");
    var container_indicator = document.querySelector("#banners .cmp-slider-indicator");

    container_img.innerHTML = html_img;
    container_indicator.innerHTML = html_indicator;

    //只有一张图片时不轮播
    if (imgs.length === 1) {
        document.querySelector(".cmp-slider-indicator").style.display = "none";
        return;
    }
    var slider = cmp("#banners").slider({
        slideshowDelay: 3000, //自动轮播动画时间
        interval: 1000 //自动轮播周期，若为0则不自动播放，默认为0；
    });
}
/**
 * 在0~(range-1)范围内取num个随机整数
 * @param {Object} range
 * @param {Object} num
 */
var getRandomNum = function (range, num) {
    var randomArray = [];
    while (num > 0) {
        var random = Math.floor(random = Math.random() * range);
        if (randomArray.indexOf(random) <= 0) {
            randomArray.push(random);
            num--;
        }
    }
    return randomArray;
}
