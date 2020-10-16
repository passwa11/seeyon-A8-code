var urlParam;
var page = {};
/*
 * 会议室审核js
 */
cmp.ready(function () {
	
	urlParam = cmp.href.getParam();
	initPageBack();
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.meetingRoom.calcelPostscript"));
		_$("#cancelComment").setAttribute("placeholder",cmp.i18n('meeting.meetingRoom.pleaseCancelContent'));
		//初始化会议点击事件
		initEvent();
	},meetingBuildVersion);
	
});

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}

function _goBack() {
    if(MeetingUtils.getBackURL() == "weixin"){
        //返回到外层, 微信入口逻辑，因为微信没办法返回到首页，所以这样处理， 暂时不要和else分支合并
        cmp.href.closePage();
    }else {
        //返回到外层
        cmp.href.back();
    }
}

function initEvent() {
	_$("#calcelBtn").addEventListener("tap",function(){
		 _goBack();
	});
	
	_$("#sumbitBtn").addEventListener("tap",submit);
	
	_$("#cancelComment").addEventListener('input', fnFontCount);
}

function submit() {
	var content = _$("#cancelComment").value;
	page.cancelContent = content;
	
	
	var alertMessage = cmp.i18n("meeting.meetingRoomCancel.noContent");
	//提前结束的提示语和
	if(urlParam.openFrom != undefined && urlParam.openFrom == "advanceMeeting"){
		alertMessage = cmp.i18n("meeting.meeting.advanceNoCotent");// 提前结束附言不能为空!
	}
	
	//撤销的时候需要必填写意见
	if(page.cancelContent.trim() == ""){
		_alert(alertMessage);
		return;
	}
	if(urlParam.openFrom != undefined && urlParam.openFrom == "meetingDetail") {
		var isBatch = "false";
		//周期性会议
		if(urlParam.category !=undefined && urlParam.category == "1") {
			// 单条撤销、批量撤销
			var items = [{key:"noBatch",name:cmp.i18n("meeting.meeting.singleUndo")},
			             {key:"yesBatch",name:cmp.i18n("meeting.meeting.batchRevocation")}];
		    cmp.dialog.actionSheet(items, cmp.i18n("meeting.page.action.cancle"),function (data){
		    	if(data.key=="yesBatch") {
		    		isBatch = "true";
		    	}
		    	cancelMeetingFtn(content,isBatch);
		    });
		} else {
			cancelMeetingFtn(content,isBatch);
		}
	} else {
		page.roomAppId = urlParam.roomAppId;
		$s.Meeting.cancelMeetRoomApp({},page,{
			success : function(result) {
				if(result){
					//触发所有webview刷新事件
					MeetingUtils.fireAllWebviewEvent();
					if(urlParam.openType == "todo"){
						cmp.href.back(2);
					}else{
						if(MeetingUtils.isFromM3NavBar()){
							cmp.href.closePage();
						}else{
							cmp.href.back(2);
						}
					}
				} else {
					_alert(cmp.i18n("meeting.exception.cancelException"));
					cmp.href.back(2);
				}
			},
	        error : function(result){
	        	//解除各按钮的绑定
	        	_removeEvent();
	        	//处理异常
	        	MeetingUtils.dealError(result);
	        }
		});
	}
	
}

function cancelMeetingFtn(content,isBatch){
	var params = {
		"meetingId":urlParam.meetingId,
		"isBatch":isBatch, 
		"content":content,
		"sendSMS":"false"
	}
	$s.Meeting.cancelMeeting({},params,{
		success : function(result) {
			//触发所有webview刷新事件
			MeetingUtils.fireAllWebviewEvent();
			if(result.message = "success") {
				cmp.href.back(2);
			} else {
				_alert(cmp.i18n("meeting.exception.cancelException"));
				cmp.href.back(2);
			}
		},
        error : function(result){
        	//解除各按钮的绑定
        	_removeEvent();
        	//处理异常
        	MeetingUtils.dealError(result);
        }
	});
}

function fnFontCount() {
    var feedback = _$("#cancelComment");
    var content = getTextDealComment();
    if (content.length > 100) {
        feedback.value = content.substr(0, 100);
        content = feedback.value;
    }
    // 剩余可以输入的字数
    _$("#fontCount").innerHTML = 100 - content.length;
}

//获取意见内容
function getTextDealComment(){
    var tValue = MeetingUtils.filterUnreadableCode(_$("#cancelComment").value);
	return tValue;
}

function _removeEvent(){
	_$("#sumbitBtn").removeEventListener("tap",submit);
}