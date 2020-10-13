var urlParam;
var page = {};

cmp.ready(function () {
	
	urlParam = cmp.href.getParam();
	
	initPageBack();
	
	//页面计算
	initLayout();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		page = urlParam;
		initHtml();
		//初始化页面数据
		initPageData();
	},meetingBuildVersion);
});

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(function(){
    	cmp.href.back();
    });
}

var initLayout = function(){
	var numberDiv = document.querySelector("#detailDiv");
	var page01 = document.querySelector("#page01"); 
	var page02 = document.querySelector("#page02");
	var page03 = document.querySelector("#page03");
	var page04 = document.querySelector("#page04");
	var numberDivH = numberDiv ? numberDiv.offsetHeight : 0;
	var windowH= window.innerHeight;
	
	page01.style.height = (windowH - numberDivH) + "px";
	page02.style.height = (windowH - numberDivH) + "px";
	page03.style.height = (windowH - numberDivH) + "px";
	page04.style.height = (windowH - numberDivH) + "px";
	page05.style.height = (windowH - numberDivH) + "px";
}

/**
 * 页面样式设置
 */
function initHtml() {
    if(page.operate == "conferee" || page.operate == "leader"){
      //国际化title标签
        var titleI18n;
        var memberNumber;
        if(page.operate == "conferee"){
        	titleI18n = "meeting.page.lable.conferees";
        }else if(page.operate == "leader"){
            titleI18n = "meeting.meetingDetail.leader";
        }
    }else if(page.operate == "impart"){
    	_$("#pending").style.display = "none";
    	_$("#noJoin").style.display = "none";
    	titleI18n = "meeting.meetingCreate.notify";
        _$("#join").innerHTML=cmp.i18n("meeting.meetingDetail.replyOk") + "<div class=\"userNumber\" id=\"joinNumber\"></div>";
    }
    if(!page.qrCodeSign){
    	_$("#haveSign").style.display = "none";
    	_$("#page05").style.display = "none";
    }
    _$("title").innerText=cmp.i18n(titleI18n);
}
/**
 * 页面数据装载 
 */
function initPageData() {
    var param = {
    	meetingId : page.meetingId,
    	operate : page.operate
    }
    $s.Meeting.showMeetingMembers(param,{
    	success : function(result){
    		initListData(result.data);
    	},
    	error : function(error){
    		MeetingUtils.dealError(error);
    	}
    });
}

var initListData = function(data){
	var attendList = [];
	var unAttendList = [];
	var pendingList = [];
	var noFeedbackList = [];
	var signList = data.signMembers;
	cmp.each(data.meetingMembers,function(i,v){
		switch(v.replyState){
			case "1" :
			case "3" :
				attendList.push(v);
				break;
			case "0" :
				unAttendList.push(v);
				break;
			case "-1" :
				pendingList.push(v);
				break;
			case "-100" :
				noFeedbackList.push(v);
				break;
			default : 
				noFeedbackList.push(v);
				break;
		}
	});
	
	/**
	 * 数量
	 */
    _$("#joinNumber").innerHTML = attendList.length;
    _$("#pendingNumber").innerHTML = pendingList.length;
    _$("#noJoinNumber").innerHTML = unAttendList.length;
    _$("#noFeedbackNumber").innerHTML = noFeedbackList.length;
    _$("#signNumber").innerHTML = signList.length;
	
	var pendingTPL = _$("#replyMemberTpl").innerHTML;
	var signTpl = _$("#signMemberTpl").innerHTML;
	//初始化参加列表
	document.querySelector("#page011 .list-before-line").innerHTML = cmp.tpl(pendingTPL, attendList);
	new cmp.iScroll('#page01', {hScroll: false, vScroll: true,useTransition:true});
	//初始待定列表
	document.querySelector("#page022 .list-before-line").innerHTML = cmp.tpl(pendingTPL, pendingList);
	new cmp.iScroll('#page02', {hScroll: false, vScroll: true,useTransition:true});
	//初始不参加列表
	document.querySelector("#page033 .list-before-line").innerHTML = cmp.tpl(pendingTPL, unAttendList);
	new cmp.iScroll('#page03', {hScroll: false, vScroll: true,useTransition:true});
	//初始未回执列表
	document.querySelector("#page044 .list-before-line").innerHTML = cmp.tpl(pendingTPL, noFeedbackList);
	new cmp.iScroll('#page04', {hScroll: false, vScroll: true,useTransition:true});
	//初始签到列表
	document.querySelector("#page055 .list-before-line").innerHTML = cmp.tpl(signTpl, signList);
	new cmp.iScroll('#page05', {hScroll: false, vScroll: true,useTransition:true});
	
	cmp.IMG.detect();
}

function remindersPending(){
	var currentUser=page.currentUser;
	var pendingUsers=page.replyList;
	var receiverIds="";
	for(var i=0;i<pendingUsers.length;i++){
		if(pendingUsers[i].feedbackFlag=="-1"){/*只添加待定状态的(-1：待定；0：不参加；1：参加)*/
			receiverIds+=pendingUsers[i].userId+",";
		}
	}
	var meetingId=page.meetingId;
	var senderId=page.currentUserId;
	var paramData={"meetingId":meetingId,"senderId":senderId,"receiverIds":receiverIds};
	sendRemindersMeetingReceiptMessage(paramData);
}

function remindersNoFeedback(){
	var receiverIds=page.noFeedBackMemberIds;
	var meetingId=page.meetingId;
	var senderId=page.currentUserId;
	var paramData={"meetingId":meetingId,"senderId":senderId,"receiverIds":receiverIds};
	sendRemindersMeetingReceiptMessage(paramData);
}

function sendRemindersMeetingReceiptMessage(paramData){
	$s.Meeting.sendRemindersMeetingReceiptMessage(paramData,{
		success:function(result){
			// cmp.notification.alert(msg, callback, title, btnName);
			if(result["success"] && result["success"]!=""){
				cmp.notification.alert(cmp.i18n("meeting.page.lable.reminders.success"),null,"",cmp.i18n("meeting.page.dialog.OK"));
			}else {
				cmp.notification.alert(cmp.i18n("meeting.page.lable.reminders.failer"),null,"",cmp.i18n("meeting.page.dialog.OK"));
			}
		}
	});
}

var DateUtil = {};
DateUtil.toString = function(sec, format) {
	if(sec == null){
		return '';
	}
    var date = new Date(parseInt(sec));
	return date.format(format);
};