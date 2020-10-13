var cacheKey_mcStorageDatas = "m3_v5_meeting_meetingModify_datas"; //缓存当前已经录入的数据，离开页面返回后回现
var cacheKey_mcBackDatas = "m3_v5_meeting_meetingModify_backDatas"; //其他页面返回时，需要带回的数据
var cacheKey_mrAppParams = "m3_v5_meeting_meetingModify_meetingRoomAppQueryParams"; //old会议室申请信息（判断会议室申请是否改变）

var isRequestData = true;

/**
 * 接收参数描述
 * meetingId      会议ID
 * openFrom       来源
 */
cmp.ready(function () {
	initPageBack();
	
	//初始化页面参数
    initPageParam();
	
	//注册懒加载
    _registLazy();
    
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.meetingModify.pageTitle"));
		initPageData();
		initEvent();
	},meetingBuildVersion);
});
/**
 * 初始化页面参数
 */
var initPageParam = function(){
	urlParam = cmp.href.getParam() || {};
	pageX.action = "modify";
}

//注册缓加载
function _registLazy(){
    
    LazyUtil.addLazyStack({
        "code" : "lazy_content",
        "depend" : "lazy_cmp",
        "groups" : "lazy_content",
        "css" : [
                 _common_v5_path + "/cmp-resources/content.css" + $verstion
                 ],
         "js" : [
                 _common_v5_path + "/widget/SeeyonContent.js" + $verstion,
                 _common_v5_path + "/js/editContent-jssdk.js" + $verstion
                 ]
    });
    
    LazyUtil.addLazyStack({
        "code" : "lazy_sliders",
        //"depend" : "lazy_listView",
        "css" : [
                 _cmpPath + "/css/cmp-sliders.css" + $verstion
                 ],
        "js" : [
                _cmpPath + "/js/cmp-sliders.js" + $verstion
                ]
    });
    
      LazyUtil.addLazyStack({
          "code" : "lazy_cmp",
          "css" : [
                   _cmpPath + "/css/cmp-picker.css" + $verstion,
                   _cmpPath + "/css/cmp-accDoc.css" + $verstion,
                   _cmpPath + "/css/cmp-att.css" + $verstion,
                   _cmpPath + "/css/cmp-search.css" + $verstion,
				   _cmpPath + "/css/cmp-selectOrg.css" + $verstion,
				   _cmpPath + "/css/cmp-dateCalender.css" + $verstion
                   ],
          "js" : [
                  _cmpPath + "/js/cmp-picker.js" + $verstion,
				  _cmpPath + "/js/cmp-popPicker.js" + $verstion,
				  _cmpPath + "/js/cmp-dateCalender.js" + $verstion,
				  _cmpPath + "/js/cmp-dtPicker-calender.js" + $verstion,
                  _cmpPath + "/js/cmp-dtPicker.js" + $verstion,
                  _cmpPath + "/js/cmp-accDoc.js" + $verstion,
                  _cmpPath + "/js/cmp-att.js" + $verstion,
                  _cmpPath + "/js/cmp-search.js" + $verstion,
                  _cmpPath + "/js/cmp-selectOrg.js" + $verstion,
                  _cmpPath + "/js/cmp-emoji.js" + $verstion,
                  _common_v5_path + "/widget/SeeyonAttachment.s3js" + $verstion
                  ]
      });
  }


function initPageData(){
	//区分是否请求后台数据
	initRequestType();
	
	//请求后台数据
	if(isRequestData){
		getMeetingDetail();
	}

	//待发列表穿透，显示待发按钮
	if(urlParam.openFrom == "waitSent"){
		_$("#waitSend").style.display = "";
	}
	
	//设置缓存数据
	setCacheDatas();
	//根据状态设置是否显示更多
	showOrHideMore();
	//展示或隐藏会议方式
	showNature();
	//根据会议方式控制是否展示密码输入框
	showPassword();
	//设置会议正文高度
	setContentHight();
	//微协同屏蔽附件区域
    if(!MeetingUtils.isCMPShell()) {
        _$("#attBtn").style.display = "none";
        _$("#middleArea").style.bottom = "55px";
    }else{
        _$("#middleArea").style.bottom = "95px";
    }
	
	if(!isRequestData){
    	//调用附件组件
    	callSeeyonAttachment();
		//调用正文组件
		callSeeyonContent();
	}
	
	//初始化默认显示值
	setDefaultShowData();
	
	//会议地点显示
	dealAuthCondition();
	
	//启动懒加载, 性能要求, 延迟是为了指标
	setTimeout(function(){
	    LazyUtil.startLazy();
	}, 0);
}

//请求后台获取需展示的数据
function getMeetingDetail(){
	var cache = cmp.storage.get(cacheKey_mcStorageDatas, true);
	if(cache != null){
		return;
	}
	
	var paramData = {
		meetingId : urlParam.meetingId
	};
	
	$s.Meeting.getMeetingModifyElement({}, paramData, {
		success : function(result) {
			if(result["errorMsg"] && result["errorMsg"]!="") {
				createAlter(result["errorMsg"], null);
				return;
			}
			
			_$("#meetingName").value = result.meetingName;
			_$("#startDate").value = result.startDate.substr(0, 10);
			_$("#startDatetimeLabel").value = result.startDate.substr(11, 5);
			_$("#endDate").value = result.endDate.substr(0, 10);
			_$("#endDatetimeLabel").value = result.endDate.substr(11, 5);
			_$("#meetingPlace").value = result.meetingPlace;
			_$("#meetingPlace_value").value = typeof(result.meetingPlace_value)=="undefined"?"":result.meetingPlace_value;
			_$("#meetingPlace_value1").value = typeof(result.meetingPlace_value1)=="undefined"?"":result.meetingPlace_value1;
			_$("#conferees").value = result.conferees;
			_$("#conferees_value").value = result.conferees_value;
			_$("#host").value = result.host;
			_$("#host_value").value = result.host_value;
			_$("#recoder").value = result.recoder;
			_$("#recoder_value").value = result.recoder_value;
			_$("#notify").value = result.notify;
			_$("#notify_value").value = result.notify_value;
			_$("#meetingNature_value").value = result.meetingNature_value;
			_$("#meetingType").value = result.meetingTypeName;
			_$("#meetingType_value").value = result.meetingTypeId;
			_$("#roomAppBeginDate").value = result.roomAppBeginDate;
			_$("#roomAppEndDate").value = result.roomAppEndDate;
			if(result.meetingNature_value == 2){
				_$("#meetingPassword").style.display = "";
				_$("#password").value = result.meeting_password;
			}
			
			pageX.cache.content = result.content;
			pageX.cache.data_format = result.data_format;
			pageX.cache.lastModified = result.lastModified;
			pageX.cache.allowTrans = result.allowTrans;
			
			pageX.cache.attachments = result.attachments;
			pageX.cache.datas = {};
			pageX.cache.datas.roomAppBeginDate = result.roomAppBeginDate;
			pageX.cache.datas.roomAppEndDate = result.roomAppEndDate;
			pageX.meetingPlace_type = result.meetingPlace_type;
			//防止跳转到其他页面直接返回，meetingPlace_type混乱
			pageX.cache.meetingPlace_type = result.meetingPlace_type;
			//是否表单触发的会议
			pageX.cache.meetingType = {};
			pageX.cache.meetingType.isFormTrigger = result.isFormTrigger;
			pageX.cache.meetingType.meetingType_value = result.meetingTypeId;
			//初始会议室申请信息
			if (typeof(result.meetingPlace_value)!="undefined") {
    			var params = {
    			        oldRoomId : result.meetingPlace_value,
    			        oldStartDate : result.roomAppBeginDate + ":00",
    	                oldEndDate : result.roomAppEndDate + ":00"
    	            };
    	        cmp.storage.save(cacheKey_mrAppParams, cmp.toJSON(params), true);
			}
			
			var _reminderSelectData = reminderSelectData();
			for(var i = 0 ; i < _reminderSelectData.length ; i++){
				if(_reminderSelectData[i].value == result.reminder){
					_$("#reminder_value").value = result.reminder;
					_$("#reminder").value = _reminderSelectData[i].text;
					break;
				}
			}
			
			//设置展现数据中，人员的缓存数据
			saveDefaultCache("conferees", result.conferees_fill);
			saveDefaultCache("notify", result.notify_fill);
			saveDefaultCache("host", result.host_fill)
			saveDefaultCache("recoder", result.recoder_fill);

			//处理互斥情况
			dealCache();
			//调用附件组件
    		callSeeyonAttachment();
			//调用正文组件
			callSeeyonContent();
			
			pageX.cache.showMeetingVideoArea = result.isShowMeetingNature;
			if(pageX.cache.showMeetingVideoArea){
				if(result.meetingNature_value == "1"){
					_$("#meetingNature").value = cmp.i18n("meeting.meetingCreate.NatureNormal");
				}else if(result.meetingNature_value == "2"){
					_$("#meetingNature").value = cmp.i18n("meeting.meetingCreate.NatureVideo");
					_$("#meetingPassword").style.display = "";
				}
			}
			//视频会议室相关参数
			pageX.cache.showVideoRoomArea = result.isShowVideoRoom;
			if(typeof(result.videoRoomId) != "undefined") {
				pageX.cache.datas.videoRoomId = result.videoRoomId;
				pageX.cache.datas.oldVideoRoomAppId = result.videoRoomAppId;
				pageX.cache.datas.videoRoomName = result.videoRoomName;
				pageX.cache.datas.videoRoomStartDate = result.videoRoomStartDate;
				pageX.cache.datas.videoRoomEndDate = result.videoRoomEndDate;
				_$("#videoRoomName").value = result.videoRoomName;
				_$("#videoRoomId").value = result.videoRoomId;
				_$("#oldVideoRoomAppId").value = result.videoRoomAppId;
				_$("#videoRoomStartDate").value = result.videoRoomStartDate;
				_$("#videoRoomEndDate").value = result.videoRoomEndDate;
				
			}
			
			//是否允许手动输入会议地点
			pageX.cache.haveMeetingRoomApp = result.haveMeetingRoomApp;
			pageX.cache.isMeetingPlaceInputAble = result.isMeetingPlaceInputAble;
			//是否允许外部会议
			pageX.cache.enablePublicMeeting = result.enablePublicMeeting;
			dealAuthCondition();
			
			//会议二维码签到
			pageX.cache.qrCodeSign = result.qrCodeSign;
			dealQrcodeSign();
			
			//外部会议标识
			pageX.cache.isPublic = result.isPublic;
			dealIsPublic();
			
			//展示或隐藏会议方式\视频会议地点
			showNature();
			
			resizeMeetingNameHeight();
			resizeMeetingPlaceHeight();
		},
        error : function(result){
        	//处理异常
        	MeetingUtils.dealError(result);
        }
	});
}

//设置展现数据中，人员的缓存数据
function saveDefaultCache(key, data){
	if(data){
		var cacheData = [];
		for(var i = 0 ; i < data.length ; i++){
			var d1 = {
				id : data[i].id,
				name : data[i].name,
				type : data[i].type
			};
			cacheData.push(d1);
		}
		cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_" + key, cmp.toJSON(cacheData), true)
	}
}

//区分是否请求后台数据
function initRequestType(){
	var cache = cmp.storage.get(cacheKey_mcStorageDatas, true);
	if(cache){
		isRequestData = false;
	}
}

//调用正文组件
function callSeeyonContent(){
    LazyUtil.addLoadedFn("lazy_content", function(){
        var contentConfig = {
                "target" : "content",
                "bodyType" : SeeyonContent.getBodyCode(pageX.cache.data_format),
                "content" : pageX.cache.content || "",
                "lastModified" : pageX.cache.lastModified,
                "momentum" : true,
                "moduleType" : "6",
                "allowTrans" : pageX.cache.allowTrans,
                "ext" : {
                    reference : urlParam.meetingId
                }
            }
            //初始化正文
            SeeyonContent.init(contentConfig);
    });
}

function submit(paramData){
    //验证您已重新申请了会议室，原有会议室会被撤销，是否提交?
    var oldMrAppcache = cmp.storage.get(cacheKey_mrAppParams, true);
    
    if (oldMrAppcache) {
        var temp_oldMrAppcache = cmp.parseJSON(oldMrAppcache);
        var flag = false;
        
        var roomId = _$("#meetingPlace_value").value;
        var roomAppBeginDate = paramData.roomAppBeginDate;
        var roomAppEndDate = paramData.roomAppEndDate;
        
        var oldRoomId = temp_oldMrAppcache.oldRoomId;
        var oldRoomAppBeginDate = temp_oldMrAppcache.oldStartDate;
        var oldRoomAppEndDate = temp_oldMrAppcache.oldEndDate;
        
        if (oldRoomId != roomId) { //修改了会议室
            flag = true;
        } else { //修改了会议室使用时间
            if (roomAppBeginDate!="" && roomAppEndDate!="") {
                if(roomAppBeginDate!=oldRoomAppBeginDate || roomAppEndDate!=oldRoomAppEndDate){
                    flag = true;
                }
            }
        }
        if (flag == true) {
            cmp.notification.confirm(cmp.i18n("meeting.meeting.meetingroomChanged"),function(e){ // e==1是/e==0 否
                if(e==1){
                    doSubmit(paramData);
                }else{
                    flag = false;
                    isSubmit = false;
                }
            },null, [cmp.i18n("meeting.meetingCommon.cancel"), cmp.i18n("meeting.meetingCommon.continue")]);
        } else {
            doSubmit(paramData);
        }
    } else {
        doSubmit(paramData);
    }
    
}
function doSubmit(paramData){
	$s.Meeting.send({}, paramData, {
		success : function(result) {
			if(result["errorMsg"] && result["errorMsg"]!="") {
				if(result["type"] == "save"){
					createAlter(cmp.i18n("meeting.meetingModify.fail") + "，" + result["errorMsg"], null);
				}else{
					createAlter(result["errorMsg"], null);
				}
				isSubmit = false;
				return;
			}
			
			//通过来源控制返回时，该跳多少个页面
			var n;
			if(urlParam.openFrom == "waitSent"){
				n = 1;
			}else{
				n = 2;
			}
			
			if(result["type"] == "send"){
				if(result["roomAppState"]){
					if(result["roomAppState"] == 0){  // 待审核
						createAlter(cmp.i18n("meeting.meetingCreate.publish1"), function(){
						    cmp.storage["delete"](cacheKey_mrAppParams, true);
							cmp.storage.save(meeting_list_type_cache_key, "listSent", true);
							_dealGoBack(n, "listSent");
						});
					}else if(result["roomAppState"] == 1){ // 审核通过
						createAlter(cmp.i18n("meeting.meetingCreate.publish"), function(){
							cmp.storage["delete"](cacheKey_mrAppParams, true);
//							cmp.storage.save(meeting_list_type_cache_key, "listPending", true);
//							_dealGoBack(n, "listPending");
							cmp.storage.save(meeting_list_type_cache_key, "listSent", true);
							_dealGoBack(n, "listSent");
						});
					}else if(result["roomAppState"] == 2){ //审核不通过
					    cmp.storage["delete"](cacheKey_mrAppParams, true);
						cmp.storage.save(meeting_list_type_cache_key, "listSent", true);
						_dealGoBack(n, "listSent");
					}
				}else{
					createAlter(cmp.i18n("meeting.meetingCreate.publish"), function(){
					    cmp.storage["delete"](cacheKey_mrAppParams, true);
//						cmp.storage.save(meeting_list_type_cache_key, "listPending", true);
//						_dealGoBack(n, "listPending");
						cmp.storage.save(meeting_list_type_cache_key, "listSent", true);
						_dealGoBack(n, "listSent");
					});
				}
				
			}else if(result["type"] == "save"){
				createAlter(cmp.i18n("meeting.meetingCreate.publish2"), function(){
				    cmp.storage["delete"](cacheKey_mrAppParams, true);
					cmp.storage.save(meeting_list_type_cache_key, "listWaitSent", true);
					_dealGoBack(n, "listWaitSent");
				});
			}

		},
		error : function(result){
		    isSubmit = false;
        	//处理异常
        	MeetingUtils.dealError(result);
		}
	});
}