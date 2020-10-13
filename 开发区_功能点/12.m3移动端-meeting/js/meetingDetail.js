var urlParam;
var page = {};
var pageX = {};//会议回执页面等相关参数
pageX.receiptInfoFlag = true;

var listType;
var isComentShow = false;//意见区域不可见
var _storge_key = "m3_v5_meeting_detail_page";
var _storge_key_reply = MeetingCacheKey.summary.reply;
var atComponent,insertAtScope;

cmp.ready(function () {
	
	_initParamData();
	
	initPageBack();
	
	//注册懒加载
	_registLazy();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.page.lable.meetingDetail"));
		//数据缓存机制，不用每次打开刷新页面
		initInStorage();
		
		//从数据库中取数据
		initPageData();
		
		//启动懒加载, 性能要求
		setTimeout(function(){
		    LazyUtil.startLazy();
		}, 0);
		
		//初始化事件
		initEvent();

		//从附件页面返回打开回执面板
		if(pageX.isShowR) mettingReplyinit();

	},meetingBuildVersion);
	
});

//注册缓加载
function _registLazy(){
    
    LazyUtil.addLazyStack({
        "code" : "lazy_content",
        "depend" : "lazy_cmp",
        "groups" : "lazy_content",
        "css" : [
                 _common_v5_path + "/cmp-resources/content.css" + meetingBuildVersion
                 ],
                 "js" : [
                         _common_v5_path + "/widget/SeeyonContent.js" + meetingBuildVersion,
                         _common_v5_path + "/js/editContent-jssdk.js" + meetingBuildVersion
                        ]
    });
    
    LazyUtil.addLazyStack({
        "code" : "lazy_sliders",
        //"depend" : "lazy_listView",
        "css" : [
                 _cmpPath + "/css/cmp-sliders.css" + meetingBuildVersion
                 ],
        "js" : [
                _cmpPath + "/js/cmp-sliders.js" + meetingBuildVersion
                ]
    });
    
      LazyUtil.addLazyStack({
          "code" : "lazy_cmp",
          "css" : [
                   _cmpPath + "/css/cmp-audio.css" + meetingBuildVersion,
                   _cmpPath + "/css/cmp-selectOrg.css" + meetingBuildVersion
                   ],
          "js" : [
                  _cmpPath + "/js/cmp-audio.js" + meetingBuildVersion,
                  _cmpPath + "/js/cmp-push.js" + meetingBuildVersion,
                  _cmpPath + "/js/cmp-app.js" + meetingBuildVersion,
                  _cmpPath + "/js/cmp-selectOrg.js" + meetingBuildVersion,
                  _cmpPath + "/js/cmp-emoji.js" + meetingBuildVersion
                  ]
      });
      
  }

function _initParamData() {

	urlParam = cmp.href.getParam() || {};
	
	if(cmp.isEmptyObject(urlParam)) {
		urlParam = MeetingUtils.getHrefQuery();
	}
	
}

/****************************** 监听返回事件(放到最前头)  ******************************/
function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
	cmp.backbutton.push(_isClearGoBack);
	//注册webview事件
	MeetingUtils.addMeetingDeleteWebviewEvent(webviewEvent);
}
/**
 * 多webview事件
 * @param {*} data 
 */
var webviewEvent = function(parameters){
	var data = parameters.data;
	if(data && data.isRefresh){
		var meetingIds = data.data;
		if(meetingIds.indexOf(page.meetingId) != -1){
			_goBack();
		}
	}
}

function _isClearGoBack() {
    setListViewRefresh("false");
    _goBack();
}

function _goBack() {

    if(urlParam.fromXz || MeetingUtils.getBackURL() == "weixin"){
        //返回到外层, 微信入口逻辑，因为微信没办法返回到首页，所以这样处理， 暂时不要和else分支合并
        cmp.href.closePage();
    }else {
        //返回到外层
        cmp.href.back();
    }
}

/********************************** 缓存操作  ***********************************/

function initInStorage() {
    var cacheData = cmp.storage.get(_storge_key, true);
    if(cacheData) {
        cmp.storage["delete"](_storge_key, true);
        page = JSON.parse(cacheData);
    }
    
    var reply_cacheData = MeetingUtils.loadCache(_storge_key_reply, true);
    
    //缓存不存在
    if(!reply_cacheData) {
		pageX.replyAttachment = {};
		pageX.replyAttachment.fileUrlIds = [];
		pageX.replyAttachment.fileComponent = null;
		
		pageX.isFromStorage = false;
    } else {
    	pageX = reply_cacheData;
    	pageX.isFromStorage = true;
    	
    	cmp.storage["delete"](_storge_key_reply, true);
    }
}

function _M3_Save_Storage() {
	page.isLoadStorage = true;
	cmp.storage.save(_storge_key, JSON.stringify(page), true);
	
	cmp.storage.save(_storge_key_reply, JSON.stringify(pageX), true);
}

function showReceiptInfo(){
    var receiptInfoDiv = document.getElementById("receiptInfo");

    if(receiptInfoDiv && pageX.receiptInfoFlag){
        receiptInfoDiv.style.display = "";
    }
}
//点击关闭隐藏消息
function hideReceiptInfo( ) {
    var receiptInfoDiv = document.getElementById("receiptInfo");
    if(receiptInfoDiv){
        pageX.receiptInfoFlag = false;
        receiptInfoDiv.style.display = "none";
	}


}
/********************************** 页面数据初始化  ***********************************/

/**
 * 后台数据装载到页面缓存page对象中 
 */
function initPageData() {
	var paramAffairId = -1;
	if (urlParam["affairId"] != undefined && urlParam["affairId"]!="" && urlParam["affairId"] != "null") {
		paramAffairId = urlParam["affairId"]
	}
	var paramMeetingId = (!urlParam["meetingId"]||urlParam["meetingId"]=="") ? -1 : urlParam["meetingId"];
	var paramOpenFrom = (!urlParam["openFrom"]||urlParam["openFrom"]=="") ? "pending" : urlParam["openFrom"];
	var proxyId = (urlParam.proxyId && urlParam.proxyId != "null") ? urlParam.proxyId : -1;
    var paramData = {
    			"affairId":paramAffairId, 
				"meetingId":paramMeetingId,
				"proxyId": proxyId,
    			"openFrom":paramOpenFrom};

    cmp.dialog.loading();
    $s.Meeting.detail(paramData, {
		success : function(result) {
            cmp.dialog.loading(false);
            
			/** 当前Affair状态校验 */
            if(result["error_msg"] && result["error_msg"]!="") {
        		_alert(result["error_msg"], function() {
        			_goBack();
        		});
        		return;
        	}
            //API中两种都有返回,所以只能判断两个了,囧
            if(result["errorMsg"] && result["errorMsg"]!="") {
        		_alert(result["errorMsg"], function() {
        			_goBack();
        		});
        		return;
        	}

            page = result.data;

			page.affairId = page.meeting.affairId;
			page.meetingId = page.meeting.id;
			
			page.proxyId = page.meeting.proxyId;
			page.openFrom = urlParam.openFrom;
			//消息穿透无memeberId 此处做兼容
			page.memberId = page.meeting.proxyId == 0 ? page.currentUserId : page.meeting.proxyId;
			
			listType = page.openFrom;
			
			LazyUtil.addLoadedFn("lazy_cmp", function(){
			    var emojiUtil = cmp.Emoji();
	            for (var i = 0; i < page.replyList.length; i++) {
	                page.replyList[i].feedback = emojiUtil.StringToEmoji(page.replyList[i].feedback);
	                page.replyList[i].feedbackHtml = emojiUtil.StringToEmoji(page.replyList[i].feedbackHtml);
	            }
	            //会议没有结束且存在回执状态就显示
	            if(page.feedbackFlag != "" && page.feedbackFlag != "-100" && page.feedbackFlag != "3" && page.meetingIsEnd == "0"){
				  showReceiptInfo();
                  var receiptInfoTypeHtml = "";
                  var  receiptInfoType = emojiUtil.StringToEmoji(page.feedbackFlag);
                  receiptInfoTypeHtml =  cmp.i18n("meeting.cmp.reply.feedback_flag."+receiptInfoType);
                  _$("#receiptInfoType").innerHTML = receiptInfoTypeHtml;
				}

		    });
			
			//初始化页面
			initHtml();
			
			//初始化打印按钮
			initPrint();
		},
		error : function(result){
        	//处理异常
        	MeetingUtils.dealError(result);
		}
	});
}

/********************************** 页面布局  ***********************************/

/**
 * 从缓存加载到页面
 */
function initHtml() {
	
	//1、初始化会议详情基本信息
	//周期性会议
	if(page.meeting.category !=undefined && page.meeting.category == "1") {
		_$("#titleDiv").innerHTML = cmp.i18n("meeting.page.perMeeting") + page.meeting.titleHtml;
	} else {
		_$("#titleDiv").innerHTML = page.meeting.titleHtml;
	}
	
	_$("#createUserName").innerHTML = page.meeting.createUserName;
	_$("#createDate").innerHTML = page.meeting.createDateFormat;
	
	_$("#meetingDate").innerHTML = page.meeting.beginDateFormat + " - " + page.meeting.endDateFormat;
	_$("#meetingPlace").innerHTML = page.meeting.meetPlaceHtml;
	_$("#meetingType").innerHTML = page.meetingTypeName;
	_$("#meetingHost").innerHTML = page.meeting.emceeName;
	_$("#recorderName").innerHTML = page.meeting.recorderName;
	_$("#confereesNames").innerHTML = page.meeting.confereesNames;
	if(page.meeting.leaderNames){
		_$("#leaderNames").innerHTML = page.meeting.leaderNames;
		_$("#showLeader").style.display = "";
	}
	if (page.meeting.impartNames != "") {
		_$("#impartNames").innerHTML = page.meeting.impartNames;
	}
	if(page.meeting.impartNames != "") {
		_$("#goImpartListBtn").addEventListener("tap", gotoImprtMemberListHtml);
	}
	
	if (typeof(page.meetingTaskNum)!="undefined" && page.meetingTaskNum != "" && page.meetingTaskNum != "0") {
		_$("#meetingTaskNum").innerHTML = page.meetingTaskNum;
	}
	//视频入口
	if((page.v_entrance) && MeetingUtils.isCMPShell()){
		_$("#v_entrance").style.display = "";
	}
	//视频会议室
	if(page.showVideoRoom) {
		_$("#videoRoomDiv").style.display = "";
		_$("#videoPasswordDiv").style.display = "";
		_$("#videoeetingPlace").innerHTML = page.videoRoomName;
		if (typeof(page.password)!="undefined") {
			_$("#meetingPassword").innerHTML = page.password;
		}
	}
	//会议二维码展示(仅会议发起人和主持人展示二维码)
	if(page.qrCodeCheckIn && (page.meeting.createUser == page.memberId || page.meeting.emceeId == page.memberId)){
		_$("#liMtQrcode").style.display = "block";
		var img =document.querySelector('#qrcodeImg');
		img.setAttribute("src",cmp.serverIp +'/seeyon/commonimage.do?method=showImage&id='+ page.qrCodeCheckIn);
        _$('.bar-code-tips').innerHTML = cmp.i18n("meeting.meetingCreate.sign.preminutes", page.preMinutes);
	}else{
		_$("#liMtQrcode").style.display = "none";
	}
	
	cmp.api.isSupportPrint({
	    success: function(canPrint) {
	        if(!canPrint){
	        	_$(".bar-code-print").style.display = "none";
	        }
	    },
	    fail: function(e) {
	        //code 为500，不支持打印
	        console.log('设备不支持打印', e);
	        _$(".bar-code-print").style.display = "none";
	    }
	})
	
	//初始化页面回执、快速回执相关按钮
    initBtnView();
	LazyUtil.addLoadedFn("lazy_content", function(){
		if(cmp.platform.miniprogram && page.meeting.bodyType.toLowerCase() != "html"){
			_alert(cmp.i18n("meeting.page.msg.notSupportOffice"));
		}else{
			//2、初始化会议正文
			var contentConfig = {
				"target" : "listView",
				"bodyType" : SeeyonContent.getBodyCode(page.meeting.bodyType),
				"padding" : "0",
				"content" : page.meeting.content,
				"lastModified" : page.meeting.lastModified,
				"moduleType" : "6",
				"rightId" : "",
				"viewState" : "",
				"momentum" : true,
				"onload" : null,
				"allowTrans" : page.allowTrans,
				"onScrollBottom" : function(){
					_toggleContent(true);//展示意见区域
				},
				"ext" : {
					reference : page.meeting.id
				}
			}
			SeeyonContent.init(contentConfig);
			/**
			 * office正文暂时固定高度
			 */
			if(!(/html/i.test(page.meeting.bodyType))){
				var windowH = window.innerHeight;
				var header = document.querySelector(".meeting-detail");
				var infoItem = document.querySelector(".meeting-info");
				var video = document.querySelector("#v_entrance");
				var footer = document.querySelector("#buttonArea");
				
				var headerH = header ? header.offsetHeight : 0;
				var infoItemH = infoItem ? infoItem.offsetHeight : 0;
				var videoH = video ? video.offsetHeight : 0;
				var footerH = footer ? footer.offsetHeight : 0;
				
				_$("#listView").style.height = (windowH - headerH - infoItemH - videoH - footerH - 80) + 'px';
			}
		}
    });
	
	//3、初始化会议附件
	if (page.meeting.hasAttsFlag) {
		_$("#attachment").style.display = "block";
		_$("#attachmentAttCount").innerHTML = "(" +page.attCount+")";
		_$("#attachmentDocCount").innerHTML = "(" + (page.attachmentList.length - parseInt(page.attCount))+")";
		
		//初始化会议附件列表
		//会议小程序屏蔽关联文档
		var attachmentList = [];
		if(cmp.platform.miniprogram){
			_$("#docLabel").style.display='none';
			_$("#attachmentDocCount").style.display='none';
			for(var i=0;i<page.attachmentList.length;i++){
				if(page.attachmentList[i].type == "0"){
					attachmentList.push(page.attachmentList[i]);
				}
			}
		}else{
			attachmentList = page.attachmentList;
		}
		var loadParam = {
				selector : "#attListUl",
				atts : attachmentList
		}
		new SeeyonAttachment({loadParam : loadParam});
		//展开与收起
		_addToggleEvent("attachment");
	}
	
	//1-13 初始化会议回执意见头
	if (page.replyList.length > 0) {
		_$("#replySize").innerHTML = "(" + page.replyList.length + ")";
		//展开与收起
		_addToggleEvent("replyConation");
	} else {
		_$("#replyConation").style.display = "none";
	}
   
   //初始化会议回执意见列表
	var replyListTpl = document.getElementById("replyListTpl").innerHTML;
	var replyListHtml = cmp.tpl(replyListTpl,page);
	_$("#replyListUl").innerHTML = replyListHtml;
	
	/**
	 * 初始化at组件
	 */
	var atMembers;
	var replyUserId = page.currentUserId;
	if(page.meeting.proxyId && page.meeting.proxyId!=null && page.meeting.proxyId!="" && page.meeting.proxyId!="0") {
		replyUserId = page.meeting.proxyId;
	}
	for(var i = 0;i < page.replyList.length;i++){
		if(page.replyList[i].userId == replyUserId){
			atMembers = page.replyList[i].atMembers ? cmp.parseJSON(page.replyList[i].atMembers) : [];
			break;
		}
	}
	atComponent = new AtComponent({
		containerId : "replyContent",
		handlerId : "openAt",
		scope : page.atScope,
		atMembers :atMembers
	});
	
	//初始化会议回复意见区域附件组件实例化
	for(var item in page.attmentMap){
		//会议小程序屏蔽关联文档
		var replyAttachment = [];
		if(cmp.platform.miniprogram){
			for(var i=0;i<page.attmentMap[item].length;i++){
				if(page.attmentMap[item][i].type == "0"){
					replyAttachment.push(page.attmentMap[item][i]);
				}
			}
		}else{
			replyAttachment = page.attmentMap[item];
		}
		var loadParam = {
			selector : "#"+item,
	   		atts : replyAttachment
		}
		new SeeyonAttachment({loadParam : loadParam});
	}
	
	//根据后台返回的参数控制是否加载视频会议所需的js
	if(page.v_url){
		cmp.asyncLoad.js([page.v_url], function(){});
	}
	
	cmp.IMG.detect();
	new cmp.iScroll('#scroller', {hScroll: false, vScroll: true, zoom:true});
	// cmp.listView("#scroller",{offset:{x:0,y:0},imgCache:true});
}

function initPrint(){
	
	cmp.api.isSupportPrint({
	    success: function(canPrint) {
	    	if(canPrint){
	    		if(!cmp.os.ios && !(/html|pdf/i.test(page.meeting.bodyType))){
		        	//Android office文档不可打印
		        }else{
		            //原生头部增加打印按钮
		            cmp.header.insertRightBtn({
		                index: 0,
		                items: [{
		                        type: 'image',
		                        imageUrl: cmp.config.printImg
		                }],
		                callback: function(index) {
		                	if(index == 0){
		                		if(!(/html/i.test(page.meeting.bodyType))){
		                			// 调用正文的打印
		                			cmp.dialog.loading("");
		                			SeeyonContent.print("listView",null,function(r){
		                				cmp.dialog.loading(false);
		                			},this);            			
		                		}else{
		                			printHtml();
		                		}
		                	}
		                }
		            });
		        }
	    	}//end of if(canPrint){
	    },
	    fail: function(e) {
	        //code 为500，不支持打印
	        console.log('设备不支持打印', e);
	    }
	})
	
}


function printHtml(){
	cmp.dialog.loading("");
	$s.Meeting.screenSlot({},{"meetingId":page.meeting.id,"oper":"onlyContent"},{
		success:function(rs){
			cmp.dialog.loading(false);
			var path = cmp.origin + "/rest/attachment/file/" + rs.data.id + "?createDate="+rs.data.extraMap.createdatestr+"&fileName=" + encodeURI(rs.data.filename);
			console.log(path);
			cmp.api.print({
				path : path,
				fileType: 'pdf',
    	        lastModify: new Date().getTime(),
    	        success: function() {
    	        	console.log('打印唤起成功');
    	        },
    	        fail: function() {
    	        	console.log('打印唤起失败');
    	        }
    		});
		},error: function (error) {
        	cmp.dialog.loading(false);
        }
	});//end of $s.Meeting.screenSlot
}

//附件区域显示和影藏
function _toggleContent(show){
    /*var shadeDisplay = "none";
    var tContentView = _$("#contentView");
    isComentShow = true;
    if(!show){
        isComentShow  = false;
        shadeDisplay = "";
    }
    _$("#scroll-shade").style.display = shadeDisplay;*/
}


function initBtnView() {
	//非发起者的样式控制
	if(page.currentUser.id != page.meeting.createUser){
		// debugger;
		_$("#buttonArea").classList.add('newStyle');
	}
	//纪要
	if(page.canSummary && page.meeting.recordId  != "0"){
		_$("#showSummary").style.display = "";
	}

	if(page.canSeeTask && page.canNewTask && !page.isImpart && !cmp.platform.miniprogram){
		_$("#showTask").style.display = "";
	}
	
	//是否展示按钮区域
	if(page.showButton){
		_$("#buttonArea").style.display = "";
		_$("#scroller").style.height = "calc(100% - 50px)";
	}else{
		return;
	}
	//回执
	if(page.canReply){
		_$("#replyBtn").style.display = "";
	}
	//快速回执
	/*if(page.canQuickReply){
		_$("#quickReplyBtn").style.display = "";
	}*/
	//撤销
	if(page.canCancel){
		_$("#transCacelBtn").style.display = "";
	}
	//编辑
	if(page.canModify){
		_$("#modifyBtn").style.display = "";
	}
	//提前结束
	if(page.canAdvanceOver){
		_$("#transAdvanceOverBtn").style.display = "";
	}
	//邀请
	if(page.meeting.canInvite) {
	    _$("#transformBtnLable").style.display = "";
	}
	//分享（只能是M3或微协同才显示分享按钮）
	page.qrCodeInvite = page.qrCodeInvite && !cmp.platform.miniprogram;
	if(page.qrCodeInvite){
		_$("#qrCodeInviteBtn").style.display = "";
	}
	
	//邀请后面还有其他按钮的时候显示分割线
	if(page.meeting.canInvite && (page.canReply || page.canQuickReply || page.qrCodeInvite)) {
	    _$("#transformBtnLable").classList.remove("border_r_n");
	}	
}

function initListView(showButton) {
    cmp.listView("#contentView", {
    	imgCache:true,
    	config : {
            customScrollMoveEvent: function (scrollY) {  //启用自定义时，其他参数不要传
                if(scrollY > 30){
                    _toggleContent(false);
                }
            }
        }
    }).refresh();
}

/********************************** 初始化控件事件  ***********************************/

function initEvent() {
	_$("#replyBtn").addEventListener("tap", gotoMeetingReplyHtml);
	
	_$("#summaryBtn").addEventListener("tap",gotoMeetingSummaryHtml);
	
	_$("#meetingTask").addEventListener("tap",gotoMeetingTaskHtml);
	
	//撤销
	_$("#transCacelBtn").addEventListener("tap",function(){
		var params = {
			"meetingId":page.meetingId,
			"affairId":page.affairId,
			"meetingState":page.meeting.state,
			"category":page.meeting.category, // 0 普通会议，1周期性会议
			"openFrom":"meetingDetail"
		}
		// cmp.href.next(_meetingPath+"/html/meetingCancel.html"+meetingBuildVersion,params);
		// 改为本页面弹出层的结构
		mettingCancelinit();
	});
	
	//编辑
	_$("#modifyBtn").addEventListener("tap",function(){
		if(page.meeting.category == "1"){
			cmp.notification.alert(cmp.i18n("meeting.meetingDetail.category"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
			return;
		}
		var params = {
			"meetingId":page.meetingId,
			"openFrom" : "sent",
			"haveMeetingRoomApp" : urlParam["haveMeetingRoomApp"]
		}
		_storePageObj();
		cmp.href.next(_meetingPath+"/html/meetingModify.html"+meetingBuildVersion,params);
	});
	
	//提前结束
	_$("#transAdvanceOverBtn").addEventListener("tap", fn_transAdvanceOver);
	
	//跳转到与会人员
	_$("#goLeaderListBtn").addEventListener("tap",gotoLeaderMemberListHtml);
	_$("#goUserListBtn").addEventListener("tap",gotoMeetingUserListHtml);
	cmp("#replyListUl").on("tap", ".commentBtn", gotoMeetingCommentHtml);
	/*cmp("body").on('tap', "#quickReplyBtn", function (e) {
		var items = [{key:"attend",name:cmp.i18n("meeting.page.action.attend")},
		             {key:"noAttend",name:cmp.i18n("meeting.page.action.noAttend")},
		             {key:"pending",name:cmp.i18n("meeting.page.action.pending")} ]
	    cmp.dialog.actionSheet(items, cmp.i18n("meeting.page.action.cancle"),function (data){
	    	submitForm(data);
	    });
	});*/
	
	//跳转到邀请选人界面
	_$("#transformBtnLable").addEventListener("tap",gotoSelectOrg);
	
	//视频会议入口
	_$("#v_entrance").addEventListener("tap", gotoVideo);
	//签到二维码展示
	_$("#mtQrcode").addEventListener("tap", gotoViewQrcode);
	_$("#mtQrcodeClose").addEventListener("click", hideQrcode);
	
	if(cmp.platform.CMPShell){		
		//二维码打印
		_$(".bar-code-print").addEventListener("click", function(){
			cmp.dialog.loading("");
			$s.Meeting.screenSlot({},{"meetingId":page.meeting.id,"oper":"showBarCode"},{
				success:function(rs){
					cmp.dialog.loading(false);
					var path = cmp.origin + "/commonimage.do?method=showImage&id=" + rs.data.id + "&createDate=" + rs.data.extraMap.createdatestr + "&size=source";;
					cmp.api.print({
						path : path,
						fileType: 'png',
						lastModify: new Date().getTime(),
						success: function() {
							console.log('打印唤起成功');
						},
						fail: function() {
							console.log('打印唤起失败');
						}
					});
				},error: function (error) {
		        	cmp.dialog.loading(false);
		        }
			});
		});
	}
	
	/**
	 * 分享
	 */
	_$("#qrCodeInviteBtn").addEventListener("tap",function(){
		if(cmp.platform.CMPShell){
			if(!page.v5Domain){
				cmp.notification.alert(cmp.i18n("meeting.meetingDetail.msg.error1"));
				return;
			}
			if(!page.M3QRCode){
				cmp.notification.alert(cmp.i18n("meeting.meetingDetail.msg.error2"));
				return;
			}
			cmp.api.shareToWXMiniProgram({
				title : page.meeting.titleHtml,
				description : "",
				webpageUrl : cmp.serverIp + "/seeyon/m3/apps/v5/meeting/html/meetingInviteCard.html?meetingId=" + page.meetingId + "&CMPWechatCanShare=true",
				path : "pages/index/index?v5Domain=" + page.v5Domain + "&v5AccountId=" + page.v5AccountId + "&M3QRCode=" + page.M3QRCode,
				thumbImage : _meetingPath + "/img/meetngShare.png",
				hdThumbImage : _meetingPath + "/img/meetngShare.png",
				withShareTicket : "",
				miniProgramType : 0,
				userName : "gh_8fe938d72038",
				forbiddenErrorHandle : true,
				success : function(){
					console.log("success");
				},
				fail : function(e){
					console.log(e);
					cmp.notification.alert(e.message);
				}
			});
		}else{
			cmp.href.next(_meetingPath + "/html/meetingInviteCard.html?meetingId=" + page.meetingId + "&CMPWechatCanShare=true");
		}
	});
}

function fn_transAdvanceOver(){
	var params = {
		"meetingId":page.meetingId
	}
	var alertMessage = cmp.i18n("meeting.page.confirm.sureToEnd");
	//当前有会议室的时候提前结束的提示为：提前结束将释放当前会议室，您确定要提前结束使用吗？
	if(page.meeting.roomId != undefined && page.meeting.roomId != null) {
		alertMessage = cmp.i18n("meeting.page.confirm.mrSureFinish2");
	}
	var btnArray = [cmp.i18n("meeting.page.action.cancle"), cmp.i18n("meeting.page.confirm.title")];
	cmp.notification.confirm(alertMessage,function(e) {
		//e==1 是 e==0否
		if (e == 1) {
			$s.Meeting.advanceMeeting({},params,{
				success : function(result) {
					if(result.message = "success") {
						//触发所有webview刷新事件
						MeetingUtils.fireAllWebviewEvent();
						_goBack();
					} else {
						_alert(cmp.i18n("meeting.exception.earlyEndException"));
						_goBack();
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
	}, cmp.i18n("meeting.page.confirm.title"), btnArray);
}
/********************************** 页面提交及跳转  ***********************************/

function submitForm(data) {
	//临时传输对象，此处用于提交
	var paramData = {};
	if (data.key== "attend") {
		paramData.feedbackFlag = 1;
	} else if (data.key == "noAttend") {
		paramData.feedbackFlag = 0;
	} else if (data.key == "pending") {
		paramData.feedbackFlag = -1;
	}
	//到回复列表中找到当前用户的回执信息
	var replyList = page.replyList;
	var userId = page.currentUserId;
	for (var i = 0; i < replyList.length; i++) {
		if(replyList[i].userId == userId){
			paramData.replyId = replyList[i].id;
			break;
		}
	}
	paramData.meetingId = page.meeting.id;
	paramData.affairId = page.meeting.affairId;
	paramData.proxyId = page.meeting.proxyId;
	paramData.memberId = page.memberId;
	//设置快速回执
	paramData.pagetype = 1;
	$s.Meeting.reply({}, paramData, {
		success : function(result) {
			if(result["errorMsg"] && result["errorMsg"]!="") {
        		_alert(result["errorMsg"]);
        		return;
        	}
			replySucceed();
			refreshCmpPending();
			_goBack();
		},
		error : function(result){
        	//处理异常
        	MeetingUtils.dealError(result);
		}
	});
}

function gotoLeaderMemberListHtml() {
	if(page.meeting.roomState != "1" || page.meeting.state == "0"){
		return;
	}
	page.operate = "leader";
	_storePageObj();
	cmp.href.next(_meetingPath + "/html/attendees.html"+meetingBuildVersion, page);
}
function gotoMeetingUserListHtml() {
	if(page.meeting.roomState != "1" || page.meeting.state == "0"){
		return;
	}
	page.operate = "conferee";
	_storePageObj();
	cmp.href.next(_meetingPath + "/html/attendees.html"+meetingBuildVersion, page);
}
function gotoImprtMemberListHtml() {
	if(page.meeting.roomState != "1" || page.meeting.state == "0"){
		return;
	}
	page.operate = "impart";
	_storePageObj();
	cmp.href.next(_meetingPath + "/html/attendees.html"+meetingBuildVersion, page);
}

function gotoMeetingReplyHtml() {
	// _M3_Save_Storage();
	// cmp.href.next(_meetingPath + "/html/meetingReply.html?_r=1"+meetingBuildVersion_and + MeetingUtils.getQueryString(), page);

	//改为本页面弹出层的交互方式

	mettingReplyinit();

}

function gotoMeetingCommentHtml() {
	page.tempRelyId = this.getAttribute("replyid");
	_storePageObj();
	cmp.href.next(_meetingPath + "/html/meetingComment.html"+meetingBuildVersion, page);
}

function gotoMeetingSummaryHtml() {
	_storePageObj();
	page.meetingDate = _$("#meetingDate").innerHTML;
	cmp.href.next(_meetingPath + "/html/meetingSummary.html"+meetingBuildVersion, page);
}

function gotoMeetingTaskHtml(){
	_storePageObj();
	taskmanageApi.jumpToTask({
		sourceType : 6,
		sourceId : page.meetingId,
		canRecord : page.canRecord
	});
}

function gotoSelectOrg(){
    var meetingId = page.meeting.id;
    $s.Meeting.removeInvitePer({"meetingId" : meetingId}, {
        success : function(result) {
        	var confereeIds = new Array();
            for (var i = 0 ; i < result.length ; i++) {
            	confereeIds[i] = result[i].id;
            }
            page.confereeIds = confereeIds;
            _opts = {
                    type : 1,//1是标准选人组件
                    showBusinessOrganization :true, //多维组织
                    excludeData:result, //被排除的不能选择的数据，格式同fillBackData一样默认是被排除的人员是被选中的，如果想要被排除的数据不被默认选中，需要在数据上传属性disable:true 如:[{id:181818,name:"杨海",type:"member",disable:true}]
                    choosableType : ['member'],//只能选择人
                    notSelectAccount:true, //不能选择单位
                    callback : function(msg){
                        _invitePer(msg);
                    }
            }
            MeetingUtils.selectOrg("transformBtnLable", null,_opts);
        },
        error : function(result){
        	//解除各按钮的绑定
        	_removeEvent();
        	//处理异常
        	MeetingUtils.dealError(result);
        }
    });
}
function _invitePer(msg){
    var conferees = "";
    var perMsg = cmp.parseJSON(msg).orgResult;
    if(perMsg.length == 0){
        return;
    }
    confereesNames = page.meeting.confereesNames;
    var confereeIds = page.confereeIds;
    page.inviteCount = 0;
    insertAtScope = [];
    for (var i = 0; i < perMsg.length; i++) {
    	if (!MeetingUtils.isInArray(confereeIds, perMsg[i]["id"])){
    		conferees += "Member|" + perMsg[i]["id"] + ",";
    		confereesNames += "," + perMsg[i]["name"];
    		page.inviteCount++;
    		
    		//@范围
    		insertAtScope.push({
    			id : perMsg[i].id,
    			name : perMsg[i].name,
    			role : cmp.i18n("meeting.page.lable.conferees")
    		});
    	}
    }
    page.meeting.confereesNames = confereesNames;
    var beginDate = page.meeting.beginDate;
    var endDate = page.meeting.endDate;
    var meetingId = page.meeting.id;
    _params = {
            "meetingId" : meetingId,
            "beginDatetime" :beginDate,
            "endDatetime" : endDate,
            "emceeId" : null,
            "recorderId" : null,
            "conferees" : conferees,
            "errorMsg" : cmp.i18n("meeting.meetingIntive.conflict")//邀请人员会议时间冲突提示语
    };
    //判断选择的会议参与人是否有会议冲突
    MeetingUtils.checkConfereesConflict(_params,function() {
        _transInviteConferees(conferees)
    }, function(){
    	
    });
}

function _transInviteConferees(conferees) {
    meetingId = page.meeting.id;
    _params = {
            "meetingId" : meetingId,
            "conferees" :conferees
    }
    
    //执行邀请人员
    $s.Meeting.transInviteConferees(_params,{
        success : function (result) {
        	var ret = result["success"];
        	if(ret != "true"){
        		_alert(ret);
        	}else {
                _$("#confereesNames").innerHTML = page.meeting.confereesNames;
                page.con_noReply = parseInt(page.con_noReply) + page.inviteCount;
                //销毁选人组件
                cmp.selectOrgDestory("selectOrg_transformBtnLable");
                //更新@范围
                atComponent.insertScope(insertAtScope);
            }
        },
        error : function(result){
        	//处理异常
        	MeetingUtils.dealError(result);
        }
    });
}

//调用视频会议接口
function gotoVideo(){
	//直接链接跳转方式参加视频会议
	if (typeof(page.video_url)!="undefined") {
		if (page.meeting.emceeId == page.memberId) {//主持人起会
			params = {
					"meetingId" : page.meeting.id,
					"videoRoomId" : page.videoRoomId
			}
			$s.Meeting.getVideoMeetingZPK({}, params,{
				success : function (result) {
					if (result.errorMsg) {
						_alert(result.errorMsg);
					} else {
						var newZpk = result.videoZpk;
						if (newZpk != "") {
							//替换掉zpk信息
							var videoURL = page.video_url;
							var videoURLArr = videoURL.split("zpk=");
							videoURL = videoURLArr[0] + "zpk=" + newZpk;
							
							if (videoURLArr.length > 1) {
								var other = videoURLArr[1].split("&");
								if (other.length > 1){
									for (var i=0; i<other.length; i++) {
										videoURL += "&" + other[i]
									}
								}
							}
							page.video_url = videoURL;
							loadVideoInBrowse();
						}
					}
				},
				error : function(result){
					//处理异常
					MeetingUtils.dealError(result);
				}
			});
		} else {
			loadVideoInBrowse();
		}
	} else {
		params = {
				"meetingId" : page.meeting.id
		}
		$s.Meeting.videoMeetingParams({}, params,{
			success : function (result) {
				var v_methodName = result.v_methodName;
				var v_params = result.v_params;
				eval(v_methodName+"(v_params)");
			},
			error : function(result){
				//处理异常
				MeetingUtils.dealError(result);
			}
		});
	}
}

function loadVideoInBrowse() {
	//使用多WebView的方式跳转
	var option = {
			"url" : page.video_url
	};
	cmp.router.loadAppInBrowse(option, 
			function(){}, //成功回调
			function(result){MeetingUtils.dealError(result);}//失败的回调
	);
}

function refreshCmpPending(){
	cmp.webViewListener.fire({ 
	    type: 'com.seeyon.m3.ListRefresh',
	    data: {affairid: urlParam["affairId"]}
	});
	//触发平台事件，用于刷新列表数据
    cmp.webViewListener.fire({
        type: "com.seeyon.m3.ListRefresh",
        data: {type: 'update'}
    });
	cmp.webViewListener.fire({
		type : "meeting.ListRefresh",
		data : {
			refreshList : "true"
		}
	});
}

//会议二维码展示
function gotoViewQrcode(){
    _$("#sign-barcode-container").style.display = 'block';
}
function hideQrcode(){
	_$("#sign-barcode-container").style.display = 'none';
}
/********************************** 页面使用工具  ***********************************/
//回执成功提示
function replySucceed(){
	cmp.notification.toast(cmp.i18n("meeting.page.action.replySucceed"), 'top', 1000);
}

function _removeEvent(){
	_$("#transAdvanceOverBtn").removeEventListener("tap", fn_transAdvanceOver);
}

//展开与收起
function _addToggleEvent(itemId){
    
    var el = _$("#" + itemId);
    if(el){
        el.querySelector(".attach-title").addEventListener("tap", function(){
            
            var eBody = this.nextElementSibling;
            var eClass = eBody.classList;
            if (eClass.contains('display_none')) {
                eClass.remove('display_none');
            }else{
                eClass.add('display_none');
            }
            
            var icon = this.querySelector('[class*="see-icon-v5-common-arrow"]');
            var iClass = icon.classList;
            if(!iClass.contains("see-icon-v5-common-arrow-right")){
                
                var iRClass = 'see-icon-v5-common-arrow-top';
                var iAClass = 'see-icon-v5-common-arrow-down';
                if (iClass.contains(iAClass)) {
                    var tAClass = iRClass;
                    iRClass = iAClass;
                    iAClass = tAClass;
                }
                
                iClass.remove(iRClass);
                iClass.add(iAClass);
            }
        });
    }
}

/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由meetingReply.js挪动过来***************************/

//展开回执面板
function mettingReplyinit(){

	var ClassList = document.querySelector('#animateR').classList;
	ClassList.add('bottom-go');
	ClassList.add('cmp-active');

	_initParamData_r();
	setPageTitle(cmp.i18n("meeting.page.action.receipt"));
	_initPageBack();

	_initHtml();

	setTimeout(_initEvent, 20);


}

function _initParamData_r() {
	pageX.isShowR = true;
}


function _initPageBack() {
  //cmp控制返回
    cmp.backbutton.push(_goBackR);
}

function _goBackR() {
	cmp.backbutton.pop();
	pageX.isShowR = undefined;
	setPageTitle(cmp.i18n("meeting.page.lable.meetingDetail"));
	document.querySelector('#animateR').classList.remove('cmp-active');
	document.querySelector("#replyContent").blur();
}

/********************************** 页面数据初始化  ***********************************/

function _storePageObj(){
	pageX.cacheContent = _$("#replyContent").value;
	if (page.meeting.businessType == 9) {//告知状态
		pageX.cacheFeedbackFlag = 3;
	} else{
	    var flagBtns = _$('li[name="feedbackFlag"]', true);
	    for(var i = 0, len = flagBtns.length; i < len; i++){
	        var btn = flagBtns[i];
	        if(btn.classList.contains("cmp-active")){
                pageX.cacheFeedbackFlag = btn.getAttribute("data-value");
            }
	    }
	}

    _M3_Save_Storage();
}


/********************************** 页面布局  ***********************************/

function _initHtml() {
	
	/** 意见回填 **/
	//回填回复态度
    var cmp_handle=document.querySelector('#attitudeForm');
    var handle=cmp_handle.querySelectorAll('li');
    for(var k= 0,len2=handle.length;k<len2;k++){
        (function(_){
            handle[_].addEventListener('tap',function(){
                for(var m= 0,n=handle.length;m<n;m++){
                    handle[m].classList.remove('cmp-active');
                }
                this.classList.add('cmp-active');
            },false);
        })(k);
    }
	
    var initAtt;
	if(pageX.isFromStorage) {//从缓存中取数据
		if (pageX.replyAttachment.fileComponent) {
			initAtt = pageX.replyAttachment.fileComponent.attObjArray;
		}
		_initFillData(pageX.cacheContent, pageX.cacheFeedbackFlag);
	} else {
		//回填会议回复意见
		var replyList = page.replyList;
		var userId = page.currentUserId;
		var reply = {};
		var replyUserId = userId;
		if(page.meeting.proxyId && page.meeting.proxyId!=null && page.meeting.proxyId!="" && page.meeting.proxyId!="0") {
			replyUserId = page.meeting.proxyId;
		}
		for (var i = 0; i < replyList.length; i++) {
			if(replyList[i].userId == replyUserId) {
				reply = replyList[i];
				_initFillData(reply.feedback, reply.feedbackFlag);
				initAtt = reply.attachmentList
				break;
			}
		}
	}
	
	/** 附件组件 **/
	_initAttachment(initAtt);
	
	/** 加载页面信息 **/
	_initPageInfo();
	
	/** 页面样式 **/
	_initPageStyle();
	
}

function _initPageInfo() {
	
	//显示会议回复框提示语
	document.getElementById('replyContent').setAttribute('placeholder', cmp.i18n("meeting.page.action.meetingAdvice"));
	
	//震荡回复及知会节点 隐藏回复态度
	if(page.operate != "comment" && page.meeting.businessType != 9) {
		_$("#attitudeForm").style.display = "";
	}
}

function _initPageStyle() {
	showHeight();

	cmp.description.init(document.querySelector("#replyContent"));
}

//数据回填
function _initFillData(feedback, feedbackFlag) {
	_$("#replyContent").value = typeof(feedback) == "undefined" ? "" : feedback;
	var flagBtns = _$('li[name="feedbackFlag"]', true);
	for(var i = 0, len = flagBtns.length; i< len; i++){
	    var btn = flagBtns[i];
	    if(btn.getAttribute("data-value")==1){
            if(btn.classList.contains("cmp-active")){
                btn.classList.remove("cmp-active");
            }
        }
        if(feedbackFlag == btn.getAttribute("data-value")){
            if(!btn.classList.contains("cmp-active")){
                btn.classList.add("cmp-active");
            }
        }
	}
	_initInputEvent();
}

//附件
function _initAttachment(initAtt){
	if(initAtt && initAtt.length > 0){
		//会议小程序屏蔽关联文档
		if(cmp.platform.miniprogram){
			for(var i=0;i<initAtt.length;i++){
				if(initAtt.type != "0"){
					initAtt.splice(i,1);
				}
			}
		}
		var tempCount = initAtt.length;
	    var tempText = "";
	    if(tempCount > 0){
	        tempText = tempCount;
	    }
	    document.getElementById("attCount").innerHTML = tempText;
	    //附件图标有附件时显示蓝色
	    var attDom = document.querySelector("#attchmentFile");
	    if(!attDom.classList.contains("cmp-active")){
	    	attDom.classList.add("cmp-active");
	    }
	}
	var initParam = {
		showAuth : cmp.platform.miniprogram ? 1 : -1,
		uploadId : "picture",
		handler : "#attchmentFile",
		initAttData : initAtt,
		selectFunc : function(fileArray){
			//展示附件数量
		    var tempCount = fileArray.length;
		    var tempText = "";
		    if(tempCount > 0){
		        tempText = tempCount;
		    }
		    document.getElementById("attCount").innerHTML = tempText;
		    //附件图标有附件时显示蓝色
		    var attDom = document.querySelector("#attchmentFile");
		    if(!attDom.classList.contains("cmp-active")){
		    	attDom.classList.add("cmp-active");
		    }
		}
	}
	pageX.replyAttachment.fileComponent = new SeeyonAttachment({initParam : initParam});
}

/********************************** 初始化控件事件  ***********************************/

function _initEvent() {    
	document.addEventListener('beforepageredirect', _storePageObj);
	
	_$("#sendBtn").addEventListener("tap", _submitForm);
	_$("#replyContent").addEventListener("input", _initInputEvent);

	cmp("#animateR").on("tap",".shadebg",function(){
		 _goBackR();
	});

}

function _removeEventR(){
	_$("#sendBtn").removeEventListener("tap", _submitForm);
}

function _initInputEvent() {
	var feedback = _$("#replyContent"),maxLength = 1200;
	var content = feedback.value;
	if(content.length > maxLength){
		feedback.value = content.substr(0,maxLength);
		content = feedback.value;
	}
	//剩余可以输入的字数
	_$("#fontCount").innerHTML = maxLength-content.length;
}

/********************************** 页面提交及跳转  ***********************************/

var isSubmit = false;
function _submitForm() {
	//防止重复点击
	if(isSubmit){
		return;
	}
	isSubmit = true;
	
	var paramData = {};
	var emojiUtil = cmp.Emoji();
    var content = emojiUtil.EmojiToString(_$("#replyContent").value);
	paramData.content = content;
	paramData.meetingId = page.meeting.id;
	paramData.proxyId = page.meeting.proxyId;
	paramData.replyId = page.tempRelyId;
	paramData.pagetype = 1;
	paramData.memberId = page.memberId;
	
	if (page.meeting.businessType == 9) {//告知状态
		paramData.feedbackFlag = 3;
	} else{
	    var flagBtns = _$('li[name="feedbackFlag"]', true);
        for(var i = 0, len = flagBtns.length; i < len; i++){
            var btn = flagBtns[i];
            if(btn.classList.contains("cmp-active")){
                paramData.feedbackFlag = btn.getAttribute("data-value");
            }
        }
	}
	
	//附件数据
	if(pageX.replyAttachment.fileComponent) {
		paramData.fileJson = cmp.toJSON(pageX.replyAttachment.fileComponent.getFileArray());
	}
	
	//@数据
	paramData.atMembers = atComponent.getResult();
	
	$s.Meeting.reply({}, paramData, {
		success : function(result) {
			if(result["errorMsg"] && result["errorMsg"]!="") {
        		cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
        		isSubmit = false;
        		return;
        	}
			
			_replySucceed();
			refreshCmpPending();
			_submitGoBack();
		},
        error : function(result){
        	//解除各按钮的绑定
        	_removeEventR();
        	//处理异常
        	MeetingUtils.dealError(result);
        }
	});
}

function _submitGoBack() {
    if(MeetingUtils.getBackURL() == "weixin"){
        //返回到外层, 微信入口逻辑，因为微信没办法返回到首页，所以这样处理， 暂时不要和else分支合并
        cmp.href.closePage();
    }else {
        //返回到外层
        cmp.href.back();
    }
}

function refreshCmpPending(){
	cmp.webViewListener.fire({ 
	    type: 'com.seeyon.m3.ListRefresh',
	    data: {affairid: urlParam["affairId"]}
	});
	//触发平台事件，用于刷新列表数据
    cmp.webViewListener.fire({
        type: "com.seeyon.m3.ListRefresh",
        data: {type: 'update'}
    });
	cmp.webViewListener.fire({
		type : "meeting.ListRefresh",
		data : {
			refreshList : "true"
		}
	});
}

function delayHideOrShowAttForm(){
//	setTimeout(function(){
//		hideOrShowAttForm();
//	},500);//延迟取window.innerHeight才会准确;
}

//回执
//监听软键盘弹起
cmp('body').on('focusin','#replyContent',delayHideOrShowAttForm);
//监听软键盘收起
cmp('body').on('focusout','#replyContent',delayHideOrShowAttForm);

//撤销
//监听软键盘弹起
cmp('body').on('focusin','#cancelComment',delayHideOrShowAttForm);
//监听软键盘收起
cmp('body').on('focusout','#cancelComment',delayHideOrShowAttForm);

/********************************** 页面使用工具  ***********************************/
//记录默认的屏幕高度，用来判断是否软键盘弹开，如果支持横竖屏的话 此变量需要更新才靠谱！！！！！！！！！！！！！！！
var _winH = window.innerHeight;
//回执成功提示
function _replySucceed() {
	cmp.notification.toast(cmp.i18n("meeting.page.action.replySucceed"), 'top', 1000);
}
//隐藏或展示意见区域
function hideOrShowAttForm(){
	var tempWinH = window.innerHeight;

	var animateRclassList = document.querySelector('#animateR').classList;
	var animateCclassList = document.querySelector('#animateC').classList;
	if(animateRclassList.contains("cmp-active")){
		//回执
		// var styleDisply = _$("#attitudeForm").style.display;
		// 现在根据高度来判断是否弹起软键盘
		if(tempWinH == _winH){
			showHeight();
		}else{
			calHeight();
		}
	}else if(animateCclassList.contains("cmp-active")){
		//撤销
		// 现在根据高度来判断是否弹起软键盘
		if(tempWinH == _winH){
			CshowHeight();
		}else{
			calHeight();
		}

	}
}
//弹出软键盘计算页面高度  --- 回执
function calHeight(){
	var winH = window.innerHeight;
	var footerH = _$(".number").offsetHeight + _$("footer").offsetHeight;
	var headerH = 0;//_$("header").offsetHeight;
	// _$("#attitudeForm").style.display = "none";
	var navH = _$("#attitudeForm").offsetHeight;
	_$("#mainBodyArea").style.top = "auto";
	_$("#animateR").style.bottom = "0px";
	// alert(winH - footerH -headerH - navH - winH*0.2);
	_$("#replyContent").style.height = winH - footerH -headerH - navH - winH*0.2 +"px";
}
function showHeight(){
    //震荡回复及知会节点 隐藏回复态度 --- 回执
    if(page.operate != "comment" && page.meeting.businessType != 9) {
        _$("#attitudeForm").style.display = "";
    }
	var winH = window.innerHeight;
	var footerH = _$(".number").offsetHeight + _$("footer").offsetHeight;
	var headerH = 0;//_$("header").offsetHeight;
	var navH = _$("#attitudeForm").offsetHeight;
	// _$("#mainBodyArea").style.top = winH*0.2+"px";
	_$("#mainBodyArea").style.top = "auto";
	_$("#animateR").style.bottom = "0px";
	// alert(winH - footerH -headerH - navH - winH*0.2);
	_$("#replyContent").style.height = winH - footerH - headerH - navH - winH*0.2 +"px";
}

//弹出软键盘计算页面高度  --- 撤销
function CshowHeight(){
	var winH = window.innerHeight;
	var footerH = _$(".max-text").offsetHeight + _$("footer").offsetHeight;
	var headerH = 0;//_$("header").offsetHeight;
	_$("#mainBodyArea").style.top = "auto";
	_$("#animateR").style.bottom = "0px";
	// alert(winH - footerH -headerH - navH - winH*0.2);
	_$("#replyContent").style.height = winH - footerH -headerH - winH*0.2 +"px";
}
//关闭软键盘计算页面高度  --- 撤销
function CshowHeight(){
	_$("#replyContent").style.height = "";
}


/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
/************************注意：因交互改变，下面的代码由mrCancel.js挪动过来***************************/
function mettingCancelinit(){
	var ClassList = document.querySelector('#animateC').classList;
	ClassList.add('bottom-go');
	ClassList.add('cmp-active');
	_initPageBackC();
	setPageTitle(cmp.i18n("meeting.meetingRoom.calcelPostscript"));
	_$("#cancelComment").setAttribute("placeholder",cmp.i18n('meeting.meetingRoom.pleaseCancelContent'));
	//初始化会议点击事件
	initEventC();
}

function _initPageBackC() {
  	//cmp控制返回
    cmp.backbutton.push(_goBackC);
}

function _goBackC() {
	cmp.backbutton.pop();
	setPageTitle(cmp.i18n("meeting.page.lable.meetingDetail"));
	document.querySelector('#animateC').classList.remove('cmp-active');
	document.querySelector("#cancelComment").blur();
}


function initEventC() {
	_$("#animateC .shadebg").addEventListener("tap",function(){
		 _goBackC();
	});
	_$("#calcelBtn").addEventListener("tap",function(){
		 _goBackC();
	});
	
	_$("#sumbitBtn").addEventListener("tap",submit);
	
	_$("#cancelComment").addEventListener('input', fnFontCount);
}

function submit() {
	var content = _$("#cancelComment").value;
	page.cancelContent = content;
	
	
	var alertMessage = cmp.i18n("meeting.meetingRoomCancel.noContent");
	
	//撤销的时候需要必填写意见
	if(page.cancelContent.trim() == ""){
		_alert(alertMessage);
		return;
	}
	var isBatch = "false";
	//周期性会议
	if(page.meeting.category !=undefined && page.meeting.category == "1") {
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
	
}

function cancelMeetingFtn(content,isBatch){
	var params = {
		"meetingId":page.meetingId,
		"isBatch":isBatch, 
		"content":content,
		"sendSMS":"false"
	}
	$s.Meeting.cancelMeeting({},params,{
		success : function(result) {
			if(result.message = "success") {
				//触发所有webview刷新事件
				MeetingUtils.fireAllWebviewEvent();
				cmp.href.back();
			} else {
				_alert(cmp.i18n("meeting.exception.cancelException"));
				cmp.href.back();
			}
		},
        error : function(result){
        	//解除各按钮的绑定
        	_removeEventC();
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

function _removeEventC(){
	_$("#sumbitBtn").removeEventListener("tap",submit);
}
