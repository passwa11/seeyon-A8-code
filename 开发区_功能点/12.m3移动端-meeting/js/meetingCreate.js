var cacheKey_mcStorageDatas = "m3_v5_meeting_meetingCreate_datas"; //缓存当前已经录入的数据，离开页面返回后回现
var cacheKey_mcBackDatas = "m3_v5_meeting_meetingCreate_backDatas"; //其他页面返回时，需要带回的数据


cmp.ready(function () {
	initPageBack();
	//注册懒加载
    _registLazy();
    //初始化页面参数
    initPageParam();
    
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.meetingCreate.createNewMeeting"));
		initPageData();
		initEvent();
	},meetingBuildVersion);
});
/**
 * 初始化页面参数
 */
var initPageParam = function(){
	urlParam = cmp.href.getParam() || {};
	pageX.action = "create";
}
//注册缓加载
function _registLazy(){
    
      LazyUtil.addLazyStack({
          "code" : "lazy_cmp",
          "css" : [
                   _cmpPath + "/css/cmp-picker.css" + $verstion,
                   _cmpPath + "/css/cmp-accDoc.css" + $verstion,
                   _cmpPath + "/css/cmp-att.css" + $verstion,
                   _cmpPath + "/css/cmp-search.css" + $verstion,
                   _cmpPath + "/css/cmp-selectOrg.css" + $verstion,
				   _cmpPath + "/css/cmp-listView.css" + $verstion,
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
                  _cmpPath + "/js/cmp-listView.js" + $verstion,
                  _common_v5_path + "/widget/SeeyonAttachment.s3js" + $verstion
                  ]
      });
  }

function initPageData(){
	//初始化人员人员信息
	setDefaultMemberInfo();
	
	//初始化时间
	var params = cmp.href.getParam();
	var currentDate = null;
	if (typeof(params) != "undefined") {
		if(params.sourceType && params.sourceType == 79){
			currentDate = params.beginDate;
		}else{
			currentDate = params.currentDate;
		}
	}
	var initDate = getInitDate_30(currentDate);
	_$("#startDate").value = initDate.startDatetime.substr(0, 10);
	_$("#startDatetimeLabel").value = initDate.startDatetime.substr(11, 5);
	_$("#endDate").value = initDate.endDatetime.substr(0, 10);
	_$("#endDatetimeLabel").value = initDate.endDatetime.substr(11, 5);
		
	setAttValue("reminder_value", "value", "0");
	setAttValue("meetingNature_value", "value", "1");
	
	//设置初始化数据
	initPageByParams();
	
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
    if (!MeetingUtils.isCMPShell()) {
    	
    }else{
        _$("#middleArea").style.bottom = "95px";
    }
	
	//初始化默认显示值
	setDefaultShowData();
	
	//会议地点显示
	dealAuthCondition();
	
	//设置附件区域数量
	showAttCount(0, 0);
	//调用附件组件
	callSeeyonAttachment();

	//启动懒加载, 性能要求, 延迟是为了指标
    setTimeout(function(){
        LazyUtil.startLazy();
    }, 0);
}

/**
 * 根据初始化数据回填页面
 * （目前小致用）
 */
function initPageByParams(){
	//会议名称
	if(urlParam.title){
		_$("#meetingName").value = urlParam.title;
	}
	/**
	 * 开始时间
	 */
	if(urlParam.beginDate){
		_$("#startDate").value = MeetingUtils.formatDate(urlParam.beginDate,'yyyy-MM-dd');
		_$("#startDatetimeLabel").value = MeetingUtils.formatDate(urlParam.beginDate,'hh:mm');
	}
	/**
	 * 结束时间
	 */
	if(urlParam.endDate){
		_$("#endDate").value = MeetingUtils.formatDate(urlParam.endDate,'yyyy-MM-dd');
		_$("#endDatetimeLabel").value = MeetingUtils.formatDate(urlParam.endDate,'hh:mm');
	}
	/**
	 * 内容
	 */
	if(urlParam.content){
		_$("#content").value = urlParam.content;
	}
	/**
	 * 与会人
	 */
	if(urlParam.conferees){
		var confereeLabel,confereeValue;
		cmp.each(urlParam.conferees,function(index,item){
			confereeLabel = confereeLabel ? (confereeLabel += "," + item.name) : item.name;
			confereeValue = confereeValue ? (confereeValue += "," + item.type + "|" + item.id) : (item.type + "|" + item.id);
		});
		_$("#conferees").value = confereeLabel;
		_$("#conferees_value").value = confereeValue;
		cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_conferees",cmp.toJSON(urlParam.conferees),true);

	}
}

//设置默认的人员信息
function setDefaultMemberInfo(){
	var cache = cmp.storage.get(cacheKey_mcStorageDatas, true);
	if(cache == null){
		$s.Meeting.create({}, {}, {
			success : function(result) {
				var userInfo = new Array();
				userInfo.push({
					id : result.userId,
					name : result.userName,
					type : "Member"
				});
				cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_host", cmp.toJSON(userInfo), true);
				cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_recoder", cmp.toJSON(userInfo), true);
				var meetingType = new Array();
				meetingType.push({
					success:result.meetingType
				});
				cmp.storage.save("m3_v5_meeting_meetingType",cmp.toJSON(meetingType),true);
				//默认选中第一个会议分类
				if (result.meetingType && result.meetingType.length > 0) {
					_$("#meetingType").value = result.meetingType[0].showName;
					_$("#meetingType_value").value = result.meetingType[0].id;
				}
				
				_$("#host").value = result.userName;
				_$("#host_value").value = result.userId;
				_$("#recoder").value = result.userName;
				_$("#recoder_value").value = result.userId;
				
				//不显示的人员
				var hideUserInfo = userInfo;
				hideUserInfo[0].display = "none";
				
				cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_except_conferees", cmp.toJSON(hideUserInfo), true);
				cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_except_notify", cmp.toJSON(hideUserInfo), true);
				
				pageX.cache.showMeetingVideoArea = result.isShowMeetingNature;
				pageX.cache.showVideoRoomArea = result.isShowVideoRoom;
				
				//是否允许手动输入会议地点
				pageX.cache.haveMeetingRoomApp = result.haveMeetingRoomApp;
				pageX.cache.isMeetingPlaceInputAble = result.isMeetingPlaceInputAble;
				//是否允许外部会议
				pageX.cache.enablePublicMeeting = result.enablePublicMeeting;
				dealAuthCondition();
				
				//展示或隐藏会议方式\视频会议地点
				showNature();

				dealCache();
			},
            error : function(result){
            	//处理异常
            	MeetingUtils.dealError(result);
            }
		});
	}
}

function submit(paramData){
	
	if(paramData.sourceType == 79 && paramData.type == 'send'){
		/**
		 * 适配小致会议发送提交时开始时间和结束时间需转换成时间戳格式
		 */
		paramData.beginDate =  new Date(paramData.beginDate.replace(/\-/g, "/")).getTime();
		paramData.endDate =  new Date(paramData.endDate.replace(/\-/g, "/")).getTime();
		
		/**
		 * 将与会人信息，格式Member|-1923578486510366786,Post|770538270407476994转换成数组格式
		 */
		if(paramData.conferees){
			var confereesArray = new Array();
			var members = paramData.conferees.split(',');
			for(var i = 0 ; i < members.length ; i++){
				var memberInfo = members[i].split('|');
				if(memberInfo.length == 2){
					var member = {};
					member.type = memberInfo[0];
					member.id = memberInfo[1];
					confereesArray.push(member);
				}
			}
			paramData.conferees = confereesArray;
		}
	}
	$s.Meeting.send({}, paramData, {
		success : function(result) {
			if(result["errorMsg"] && result["errorMsg"]!="") {
				createAlter(result["errorMsg"], null);
				isSubmit = false;
				return;
			}

			if(result["type"] == "send"){
				if(result["roomAppState"] && result["roomAppState"] == 0){ //待审核
					createAlter(cmp.i18n("meeting.meetingCreate.publish1"), function(){
						cmp.storage.save(meeting_list_type_cache_key, "listSent", true);
						_dealGoBack(1, "listSent");
					});
				}else{
					createAlter(cmp.i18n("meeting.meetingCreate.publish"), function(){
//						cmp.storage.save(meeting_list_type_cache_key, "listPending", true);
//						_dealGoBack(1, "listPending");
						cmp.storage.save(meeting_list_type_cache_key, "listSent", true);
						_dealGoBack(1, "listSent");
					});
				}
			}else if(result["type"] == "save"){
				createAlter(cmp.i18n("meeting.meetingCreate.publish2"), function(){
					cmp.storage.save(meeting_list_type_cache_key, "listWaitSent", true);
					_dealGoBack(1, "listWaitSent");
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

//获取初始化的时间  开始时间是当前时间后接近的半点或整点
function getInitDate_30(data){
	var nowDate = new Date();
	if(data != null ){
      nowDate  = new Date(data);
	}
	var startDatetime, endDatetime, minutes;
	var nowMinutes = nowDate.getMinutes();
	if(nowMinutes == 30 || nowMinutes == 0){
		startDatetime = nowDate;
	}else{
		minutes = nowMinutes - 30 > 0 ? 60 - nowMinutes : 30 - nowMinutes;
		startDatetime = new Date(Date.parse(nowDate) + (60000 * minutes));
	}
	
	endDatetime = new Date(Date.parse(startDatetime) + (60000 * 60));
	
	var dateTime = {
		startDatetime : formatDate(startDatetime),
		endDatetime : formatDate(endDatetime)
	};
	return dateTime;
}

//格式化时间
function formatDate(time){
	var year = time.getFullYear(),
	month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
	date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate(),
	hours = time.getHours() == 0 ? "00" : time.getHours() < 10 ? "0" + time.getHours() : time.getHours(),
	minutes = time.getMinutes() == 0 ? "00" : time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes();

	return year + "-" + month + "-" + date + " " + hours + ":" + minutes;
}