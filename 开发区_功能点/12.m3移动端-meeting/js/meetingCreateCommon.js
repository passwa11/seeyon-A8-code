var cacheKey_mrQueryParams = "m3_v5_meeting_meetingRoomList_queryParams"; //会议室申请列表所需查询条件
var meeting_list_type_cache_key= "m3_v5_meeting_list_type"; //首页页面缓存值，需要与首页相同
var cacheKey_mrAppParams = "m3_v5_meeting_meetingModify_meetingRoomAppQueryParams"; //old会议室申请信息（判断会议室申请是否改变）
var pageX = {};
pageX.cache = {};
pageX.postData = {};
pageX.fileComponent = null;
pageX.cache.datas = {};

function initPageBack() {
	//cmp控制返回
    cmp.backbutton();
	cmp.backbutton.push(_goBack);
	//屏蔽手势返回
	cmp.api.setGestureBackState(false);
}

function _goBack() {
	inputBlur();
    var btns = [];
    cmp.notification.confirm(cmp.i18n("meeting.message.surequit"), function (index) {
        if (index == 0) {//取消
        	cmp.notification.close();
        } else if (index == 1) {//确定
        	cmp.webViewListener.fire({
                type: "meeting.ListRefresh",
                data: {closePage: 'true'}
            });
            //删除选人组件使用的缓存
            MeetingUtils.clearSelectOrgCache();
            cmp.storage["delete"](cacheKey_mrAppParams, true);
            cmp.href.back();
        }
    },null,null,null,1);
    /*
    //已发编辑时没有保存待发
    if(cmp.href.getParam() && cmp.href.getParam().openFrom == "sent"){
        btns = [{
            key : "delete",
            name : "<span style='color:#FF4141'>" + cmp.i18n("meeting.page.lable.cancelCreat") + "</span>"//放弃新建
        }];
    } else {
        btns = [{
            key : "saveDraft",
            name : cmp.i18n("meeting.meetingCreate.waitSend")//保存到待发
        },{
            key : "delete",
            name : "<span style='color:#FF4141'>" + cmp.i18n("meeting.page.lable.cancelCreat") + "</span>"//放弃新建
        }];
    }
    
    cmp.dialog.actionSheet(btns, cmp.i18n("meeting.page.action.cancle"), function(item) {
        //点击操作
        if("saveDraft" == item.key){
            send("save")
        }else if("delete" == item.key){
        	cmp.webViewListener.fire({
                type: "meeting.ListRefresh",
                data: {closePage: 'true'}
            });
        	
            //删除选人组件使用的缓存
            MeetingUtils.clearSelectOrgCache();
            cmp.storage["delete"](cacheKey_mrAppParams, true);
            cmp.href.back();
        }
    }, function() {
    	//点击取消
    });
    */
}
function _dealGoBack(n, listType) {
	//触发所有webview刷新事件
	MeetingUtils.fireAllWebviewEvent();
	/**
	 * 添加判断如果是ipad就关闭webview
	 */
	cmp.href.isInDetailPad(function(ret){
		if(ret){
			cmp.href.closePage();
		}else{
			//从小致打开的情况
			if(urlParam.sourceType == 79){
				cmp.href.back(1,{
					data:{
						appId: "6",
						forwardType: urlParam.forwardType,
						id: urlParam.relationId
					}

				});
				return;
			}
			//删除选人组件使用的缓存
			MeetingUtils.clearSelectOrgCache();
			cmp.storage["delete"](cacheKey_mrAppParams, true);
			/**
			 * 从首页新建和新建会议室新建成功后固定返回到我的会议列表
			 */
			if(urlParam.fromApp == 6){
				cmp.href.replaceNode(_meetingPath + "/html/meeting_list_mine.html");
			}else{
				cmp.href.back(n);
			}
		}
	});
}

function initEvent(){
	_$("#showOrHideMore").addEventListener("tap", showOrHideMore);
	_$("#showMeetingPlace").addEventListener("tap", function(){
		inputBlur();
		showPlace();
	});
	_$("#showMeetingType").addEventListener("tap", function(){
		inputBlur();
		showType();
	});
	_$("#showMeetingPlaceAlways").addEventListener("tap", showPlaceAlways);
	_$("#showMeetingTypeAlways").addEventListener("tap", showTypeAlways);
	_$("#reminder").addEventListener("tap", showReminder);
	_$("#showMeetingNature").addEventListener("tap", showMeetingNature);
	_$("#waitSend").addEventListener("tap", function(){send("save")});
	_$("#send").addEventListener("tap", function(){send("send")});
	_$("#conferees").addEventListener("tap", function(){
		MeetingUtils.selectOrg("conferees", setConfereesValue, {showBusinessOrganization:true})
	});
	_$("#confereesAlways").addEventListener("tap", function(){
		MeetingUtils.selectOrg("conferees", setConfereesValue, {})
	});
	_$("#host").addEventListener("tap", function(){
		MeetingUtils.selectOrg("host", setHostValue, {
			showBusinessOrganization:true,
			maxSize : 1,
			minSize : 1,
			type : 2
		})
	});
	_$("#hostAlways").addEventListener("tap", function(){
		MeetingUtils.selectOrg("host", setHostValue, {
			maxSize : 1,
			minSize : 1,
			type : 2
		})
	});
	_$("#recoder").addEventListener("tap", function(){
		MeetingUtils.selectOrg("recoder", setRecoderValue, {
			showBusinessOrganization:true,
			maxSize : 1,
			minSize : 0,
			type : 2
		})
	});
	_$("#recoderAlways").addEventListener("tap", function(){
		MeetingUtils.selectOrg("recoder", setRecoderValue, {
			maxSize : 1,
			minSize : 0,
			type : 2
		})
	});
	_$("#notify").addEventListener("tap", function(){
		MeetingUtils.selectOrg("notify", setNotifyValue, {showBusinessOrganization:true})
	});
	_$("#notifyAlways").addEventListener("tap", function(){
		MeetingUtils.selectOrg("notify", setNotifyValue, {})
	});
	_$("#showMeetingVideoPlace").addEventListener("tap", function(){
		goToRoomList("videoRoom");
	});
	_$("#showMeetingVideoPlaceAlways").addEventListener("tap", function(){
		goToRoomList("videoRoom");
	});
	
	//添加缓存
	document.addEventListener('beforepageredirect', saveCache);
	
	cmp.event.click(_$("#showStartDate"), cmpData_start);
	cmp.event.click(_$("#showEndDate"), cmpData_end);
	
}

//展开或收起更多区域
function showOrHideMore(){
	var show_more_icon = document.getElementById("show_more_icon");
  	if(pageX.cache.showMore){
	  	_$("#moreItme").style.display = "block";
	  	if(show_more_icon){
	        show_more_icon.classList.add("open");
	        show_more_icon.classList.remove("close");
	    }
	}else{
	 	_$("#moreItme").style.display = "none";
	    if(show_more_icon){
		    show_more_icon.classList.add("close");
		    show_more_icon.classList.remove("open");
	    }
  	}
	//设置会议正文高度
	setContentHight();
	pageX.cache.showMore = !pageX.cache.showMore;
}

//始终展示下拉
function showPlaceAlways(){
	showPlace(false);
}
/**
 * 始终展示会议分类
 */
function showTypeAlways(){
	showType(false);
}
/**
* 展示会议室地点的选项
* check 是否校验地址
*/
function showPlace(check){
	_showPlace(check);
}

function _showPlace(check){
	if(check && !_$("#meetingPlace").getAttribute("readonly") && !_$("#meetingPlace").value){
		return;
	}
	var userPicker = new cmp.PopPicker();
	var meetingRoomOption = [];
	if (pageX.cache.haveMeetingRoomApp) {
		meetingRoomOption.push({
		    value : "1",  //已申请
		    text : cmp.i18n("meeting.meetingCreate.applied")
		});
		meetingRoomOption.push({
			value : "2", //申请会议室
			text : cmp.i18n("meeting.meetingCreate.applyMeetingRoom")
		});
		if(pageX.cache.isMeetingPlaceInputAble){
			meetingRoomOption.push({
			  	value : "0", //手动输入
			  	text : cmp.i18n("meeting.meetingCreate.inputByHand")
			});
		}
	} else {
		if(pageX.cache.isMeetingPlaceInputAble){
			meetingRoomOption.push({
			  	value : "0", //手动输入
			  	text : cmp.i18n("meeting.meetingCreate.inputByHand")
			});
		}else{
			return;
		}
	}
	
	userPicker.setData(meetingRoomOption);
	userPicker.show(function(items) {
		var value = items[0].value;
		if(value == "1"){
			try{
			var paramData = {
				openFrom : "meetingCreate",
				listType : "mrApproveList",
				cacheKey_mcBackDatas : cacheKey_mcBackDatas
			};
	
			pageX.meetingPlace_type = "applied";
			
			//触发保存缓存事件
			cmp.event.trigger("beforepageredirect", document);
			
			cmp.href.next(_meetingPath + "/html/meetingRoomAdminApproveList.html"+meetingBuildVersion, paramData);
			}catch(e){
				alert(e);
			}
		}else if(value == "2"){
			goToRoomList();
		}else if(value == "0"){
			_$("#meetingPlace").removeAttribute("readonly");
			if(pageX.meetingPlace_type != "mtPlace"){
				_$("#meetingPlace").value = "";
				_$("#shadowMeetingPlace").value = "";
			}
			pageX.meetingPlace_type = "mtPlace";
			pageX.cache.meetingPlace_type = "mtPlace";
			_$("#meetingPlace").focus();
		}
	});
}

/**
 * 跳转会议室选择页面
 * @param type(videoRoom:视频会议室)
 * @returns
 */
function goToRoomList(type){
	//将查询条件放在session中,下个页面要使用
	var params = {
		type : "query",
		queryType : "time",
		startDate : _$("#startDate").value + " " + _$("#startDatetimeLabel").value,
		endDate : _$("#endDate").value + " " + _$("#endDatetimeLabel").value
	};
	cmp.storage.save(cacheKey_mrQueryParams, cmp.toJSON(params), true);

	var paramData={
		action : "createMeeting",
		cacheKey_mcBackDatas : cacheKey_mcBackDatas
	};

	if (type=="videoRoom") {
		paramData.action = "createVideoMeeting";
	} else {
		pageX.meetingPlace_type = "apply";
	}
	
	//触发保存缓存事件
	cmp.event.trigger("beforepageredirect", document);

	cmp.href.next(_meetingPath + "/html/meetingRoomList.html"+meetingBuildVersion, paramData);
}
/**
 * 展示会议分类
 */
function showType(check){
	var meetingTypePicker = new cmp.PopPicker();
	if(pageX.action = "modify" && pageX.cache.meetingType && pageX.cache.meetingType.isFormTrigger && "1" == pageX.cache.meetingType.isFormTrigger){
		var dataArray = new Array();
		 dataArray.push({value : "-1",  text : cmp.i18n("meeting.meetingCreate.none")});
		 dataArray.push({value : pageX.cache.meetingType.meetingType_value,  text : cmp.i18n("meeting.meetingCreate.formTrigger")});
		meetingTypePicker.setData(dataArray);
		meetingTypePicker.show(function(items){
		var value = items[0].value;
		var text = items[0].text;
		_$("#meetingType").value = text;
		_$("#meetingType_value").value = value;
		});
		return;
	}
	//取当前单位的会议分类
	var meetingType = cmp.storage.get("m3_v5_meeting_meetingType", true);
	if(meetingType != null && meetingType != undefined && meetingType != ""){//如果缓存有值
			var m = cmp.parseJSON(meetingType)[0].success;
			var dataArray = new Array();
			 dataArray.push({value : "-1",  text : cmp.i18n("meeting.meetingCreate.none")});
			for(i=0;i<m.length;i++){
				 dataArray.push({"value":m[i].id,"text":m[i].showName});
			}
			meetingTypePicker.setData(dataArray);
			meetingTypePicker.show(function(items){
			var value = items[0].value;
			var text = items[0].text;
			_$("#meetingType").value = text;
			_$("#meetingType_value").value = value;
			});
	}else{//重新获取
		 $s.Meeting.getMeetingSummaryTypeList(null,{
			 success:function(result){
				 var dataArray = new Array();
				 //先存放一个-1
				 dataArray.push({value : "-1",  text : cmp.i18n("meeting.meetingCreate.none")});
				for(i=0;i<result.success.length;i++){
					 dataArray.push({"value":result.success[i].id,"text":result.success[i].showName});
				}
				var meetingType = new Array();
				meetingType.push({
					success:result.success
				});
				//缓存
				cmp.storage.save("m3_v5_meeting_meetingType",cmp.toJSON(meetingType),true);
				
				meetingTypePicker.setData(dataArray);
				meetingTypePicker.show(function(items){
				var value = items[0].value;
				var text = items[0].text;
				_$("#meetingType").value = text;
				_$("#meetingType_value").value = value;
				});
				
			 }
		 });
	}
}
//展示提醒设置
function showReminder(){
	var userPicker = new cmp.PopPicker();
	var minutes = cmp.i18n("meeting.meetingCreate.minutes");
	
	userPicker.setData(reminderSelectData());
	
	userPicker.show(function(items) {
		_$("#reminder").value = items[0].text;
		_$("#reminder_value").value = items[0].value;
	});
}

//展示会议方式的选项
function showMeetingNature(){
	var userPicker = new cmp.PopPicker();
	userPicker.setData([{
		value : "1",  //普通会议
		text : cmp.i18n("meeting.meetingCreate.NatureNormal")
	},{
		value : "2", //视频会议
		text : cmp.i18n("meeting.meetingCreate.NatureVideo")
	}])
	userPicker.show(function(items){
		_$("#meetingNature").value = items[0].text;
		_$("#meetingNature_value").value = items[0].value;
		if(items[0].value == "2"){
			_$("#meetingPassword").style.display = "";
		}else{
			_$("#meetingPassword").style.display = "none";
		}
	});
}

//设置与会人值
function setConfereesValue(result){
	var tempHtml = "";
	for(var i = 0 ; i < result.length ; i++){
		tempHtml += result[i].type + "|" + result[i].id + ",";
	}
	tempHtml = tempHtml.substr(0, tempHtml.length -1);
	_$("#conferees_value").value = tempHtml;
	
	//处理互斥情况
	dealCache();
}
//设置主持人
function setHostValue(result){
	_$("#host_value").value = result[0].id;
	
	//处理互斥情况
	dealCache();
}
//设置记录人
function setRecoderValue(result){
	if(result && result.length > 0){
		_$("#recoder_value").value = result[0].id;
	}else{
		_$("#recoder_value").value = "";
	}
	
	//处理互斥情况
	dealCache();
}
//设置告知人值
function setNotifyValue(result){
	var tempHtml = "";
	for(var i = 0 ; i < result.length ; i++){
		tempHtml += result[i].type + "|" + result[i].id + ",";
	}
	tempHtml = tempHtml.substr(0, tempHtml.length -1);
	_$("#notify_value").value = tempHtml;
	
	//处理互斥情况
	dealCache();
}

//设置会议正文高度
function setContentHight() {
	var scrollerHeight = _$("#scroller").offsetHeight;
	var otherHeight = _$("#otherDiv").offsetHeight;
	
	var contentDivHeight = scrollerHeight - otherHeight;
	
	_$("#contentDiv").style.height = contentDivHeight + "px";
}

//处理缓存数据互斥关系
function dealCache(){
	var cache_conferees = cmp.storage.get("m3_v5_meeting_selectOrg_bachCache_conferees", true);//参会人
	var cache_host = cmp.storage.get("m3_v5_meeting_selectOrg_bachCache_host", true);//主持人
	var cache_recoder = cmp.storage.get("m3_v5_meeting_selectOrg_bachCache_recoder", true);//记录人
	var cache_notify = cmp.storage.get("m3_v5_meeting_selectOrg_bachCache_notify", true);//告知人
	
	var hideObj_conferees = MeetingUtils.mergeArray(cmp.parseJSON(cache_host), cache_recoder ? cmp.parseJSON(cache_recoder) : {});
	hideObj_conferees = MeetingUtils.mergeArray(hideObj_conferees, cache_notify ? cmp.parseJSON(cache_notify) : {});
	addExceptStyle(hideObj_conferees);
	cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_except_conferees", cmp.toJSON(hideObj_conferees), true);
	
	var hideObj_host = MeetingUtils.mergeArray(cache_conferees ? cmp.parseJSON(cache_conferees) : {}, cache_notify ? cmp.parseJSON(cache_notify) : {});
	addExceptStyle(hideObj_host);
	cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_except_host", cmp.toJSON(hideObj_host), true);
	
	var hideObj_recoder = MeetingUtils.mergeArray(cache_conferees ? cmp.parseJSON(cache_conferees) : {}, cache_notify ? cmp.parseJSON(cache_notify) : {});
	addExceptStyle(hideObj_recoder);
	cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_except_recoder", cmp.toJSON(hideObj_recoder), true);
	
	var hideObj_notify = MeetingUtils.mergeArray(cache_conferees ? cmp.parseJSON(cache_conferees) : {}, cmp.parseJSON(cache_host));
	hideObj_notify = MeetingUtils.mergeArray(hideObj_notify, cache_recoder ? cmp.parseJSON(cache_recoder) : {});
	addExceptStyle(hideObj_notify);
	cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_except_notify", cmp.toJSON(hideObj_notify), true);
}

function addExceptStyle(hideObj){
	for(var i = 0 ; i < hideObj.length ; i++){
		hideObj[i].display = "none";
	}
}

function cmpData_start(){
	inputBlur();
	cmpData("startDate");
}
function cmpData_end(){
	inputBlur();
	cmpData("endDate");
}
//调用日期组件
function cmpData(id){
	var options = {
		type : "dateTimeCalender",
		MinutesScale : "5",
		group:true,
		showEndTime:id=="endDate"?true:false,
		value : _$("#startDate").value + " "+_$("#startDatetimeLabel").value,
		endValue:_$("#endDate").value + " "+_$("#endDatetimeLabel").value
	};
	var picker = new cmp.DtPicker(options);
	picker.show(function(rs) {
		_$("#startDate").value = rs.beginData.value;;
		_$("#startDatetimeLabel").value = rs.beginTime;

		_$("#endDate").value = rs.endData.value;
		_$("#endDatetimeLabel").value = rs.endTime;

		// _$("#"+id).value = rs.value.substr(0, 10);
		// _$("#"+id+"timeLabel").value = rs.value.substr(10, 5);
		picker.dispose();
	});
}

//离开页面时存储已输入的数据
function saveCache(){
	pageX.cache.datas = MeetingUtils.formPostData("scroller");
	//保存附件
	pageX.cache.attachments = pageX.fileComponent.attObjArray;
	//二维码签到和外部会议
	pageX.cache.qrCodeSign = _$(".qrCodeSign").classList.contains("cmp-active") ? "1":"0";
	pageX.cache.isPublic = _$("#isPublic .cmp-switch").classList.contains("cmp-active") ? "1":"0";
	cmp.storage.save(cacheKey_mcStorageDatas, cmp.toJSON(pageX.cache), true);

}

//设置默认的展示值
function setDefaultShowData(){
	setAttValue("meetingName", "placeholder", cmp.i18n("meeting.meetingCreate.inputMeetingName"));
	setAttValue("videoRoomName", "placeholder", cmp.i18n("meeting.meetingCreate.inputMeetingVideoPlace"));
	setAttValue("meetingType", "placeholder", cmp.i18n("meeting.meetingCreate.inputMeetingType"));
	setAttValue("conferees", "placeholder", cmp.i18n("meeting.meetingCreate.inputConferees"));
	setAttValue("notify", "placeholder", cmp.i18n("meeting.meetingCreate.inputNotify"));
	setAttValue("reminder", "placeholder", cmp.i18n("meeting.meetingCreate.none"));
	setAttValue("meetingNature", "placeholder", cmp.i18n("meeting.meetingCreate.NatureNormal"));
	setAttValue("content", "placeholder", cmp.i18n("meeting.page.lable.pleaseInputContent"));
}

//设置缓存中的数据
function setCacheDatas(){
	getCache();
	//还原所有输入数据
	for(var key in pageX.cache.datas){
		_$("#" + key).value = pageX.cache.datas[key];
	}
	
	var cacheKey = new Array("host", "recoder", "conferees", "notify");
	cmp.storage.save("m3_v5_meeting_selectOrg_bachCacheKey", cmp.toJSON(cacheKey), true);
}

//返回页面时获取缓存数据
function getCache(){
	//获取跳转页面前的缓存
	var cache = cmp.storage.get(cacheKey_mcStorageDatas, true);
	if (cache!=null) {
		pageX.cache = cmp.parseJSON(cache);
	}
	cmp.storage["delete"](cacheKey_mcStorageDatas, true);
	
	//获取其他页面返回的数据
	var backDatasCache = cmp.storage.get(cacheKey_mcBackDatas, true);
	if(backDatasCache){
		var temp_backDatasCache = cmp.parseJSON(backDatasCache);
		//视频会议室
		if (temp_backDatasCache.videoRoomId) {
			//视频会议室
			pageX.cache.datas.videoRoomId = temp_backDatasCache.videoRoomId;
			pageX.cache.datas.videoRoomName = temp_backDatasCache.videoRoomName;
			//视频会议室相关参数
			if(temp_backDatasCache.videoRoomStartDate){
				//解析日期与时间
				var startDate = temp_backDatasCache.videoRoomStartDate;
				var p_startDate = startDate.substr(0, 10);
				var p_startDateTime = startDate.substr(11, 5);
				
				pageX.cache.datas.videoRoomStartDate = startDate;
				pageX.cache.datas.startDate = p_startDate;
				pageX.cache.datas.startDatetimeLabel = p_startDateTime;
			}
			if(temp_backDatasCache.videoRoomEndDate){
				//解析日期与时间
				var endDate = temp_backDatasCache.videoRoomEndDate;
				var p_endDate = endDate.substr(0, 10);
				var p_endDateTime = endDate.substr(11, 5);
				
				pageX.cache.datas.videoRoomEndDate = endDate;
				pageX.cache.datas.endDate = p_endDate;
				pageX.cache.datas.endDatetimeLabel = p_endDateTime;
			}
			if (temp_backDatasCache.meetingNature_value) {//会议分类设置为:视频会议
				pageX.cache.datas.meetingNature_value = temp_backDatasCache.meetingNature_value;
			}
		} else if (temp_backDatasCache.roomName) {//普通会议室
			pageX.cache.datas.meetingPlace = temp_backDatasCache.roomName;
			pageX.cache.datas.meetingPlace_value = temp_backDatasCache.roomId;
			pageX.cache.datas.meetingPlace_value1 = temp_backDatasCache.roomappId;
			pageX.meetingPlace_type = temp_backDatasCache.meetingPlace_type;
			//选择会议室类型后，第二次选择的时候直接返回时取值
			pageX.cache.meetingPlace_type = pageX.meetingPlace_type;
			if(temp_backDatasCache.startDate){
				//解析日期与时间
				var startDate = temp_backDatasCache.startDate;
				var p_startDate = startDate.substr(0, 10);
				var p_startDateTime = startDate.substr(11, 5);
				
				pageX.cache.datas.roomAppBeginDate = startDate;
				pageX.cache.datas.startDate = p_startDate;
				pageX.cache.datas.startDatetimeLabel = p_startDateTime;
			}
			if(temp_backDatasCache.endDate){
				//解析日期与时间
				var endDate = temp_backDatasCache.endDate;
				var p_endDate = endDate.substr(0, 10);
				var p_endDateTime = endDate.substr(11, 5);
				
				pageX.cache.datas.roomAppEndDate = endDate;
				pageX.cache.datas.endDate = p_endDate;
				pageX.cache.datas.endDatetimeLabel = p_endDateTime;
			}
		}
		if (typeof(pageX.meetingPlace_type) == "undefined"){
		    //上一次选择的有数据的时候，应该为上一个选择的数据
		    pageX.meetingPlace_type = pageX.cache.meetingPlace_type ? pageX.cache.meetingPlace_type : "mtRoom";
		}
		cmp.storage["delete"](cacheKey_mcBackDatas, true);
	} else if (pageX.meetingPlace_type != "mtPlace"){
	    //上一次选择的有数据的时候，应该为上一个选择的数据
	    pageX.meetingPlace_type = pageX.cache.meetingPlace_type ? pageX.cache.meetingPlace_type : "mtRoom";
	}
	
	//是否展开
	if(MeetingUtils.isNull(pageX.cache.showMore)){
		pageX.cache.showMore = false;
	}else{
		pageX.cache.showMore = pageX.cache.showMore ? false : true;
	}
}

//调用附件组件
function callSeeyonAttachment(){
	//会议小程序屏蔽关联文档
	var attachmentList = [];
	if(cmp.platform.miniprogram){
		if(pageX.cache.attachments){
			for(var i=0;i<pageX.cache.attachments.length;i++){
				if(pageX.cache.attachments[i] == "0"){
					attachmentList.push(pageX.cache.attachments[i]);
				}
			}
		}
	}else{
		attachmentList = pageX.cache.attachments;
	}
	var initParam = {
		showAuth : cmp.platform.miniprogram ? 1 : -1,//会议小程序暂时屏蔽关联文档
		uploadId : "picture",
		handler : "#attBtn",
		initAttData : attachmentList,
		selectFunc : function(fileArray){
			var attArray = new Array();
			var assArray = new Array();
			for(var i = 0 ; i < fileArray.length ; i++){
				if(fileArray[i].attachment_fileType == "associated"){
					assArray.push(fileArray[i]);
				}else if(fileArray[i].attachment_fileType == "file"){
					attArray.push(fileArray[i]);
				}
			}
			showAttCount(attArray.length, assArray.length);
		}
	}

	LazyUtil.addLoadedFn("lazy_cmp", function(){
	    pageX.fileComponent = new SeeyonAttachment({initParam : initParam});
	    pageX.cache.initAttData = pageX.fileComponent.attObjArray;
	    initParam = null;
    });
}

/**
 * 设置附件区域数量
 * attCount  附件数量
 * assCount  关联文档数量
 */
function showAttCount(attCount, assCount){
	var tempHtml = "";
	if(cmp.platform.miniprogram){
		tempHtml = cmp.i18n("meeting.meetingCreate.attachment") + "(" + attCount + ")";
	}else{
		tempHtml = cmp.i18n("meeting.meetingCreate.attachment") + "(" + attCount + ")" + cmp.i18n("meeting.meetingCreate.and") + 
		cmp.i18n("meeting.meetingCreate.associatedDocument") + "(" + assCount + ")";
	}
	_$("#showAttCount").innerHTML = tempHtml;
}

//展示或隐藏会议方式
function showNature(){
	if(pageX.cache.showVideoRoomArea) {
		//显示视频会议地点
		_$("#liMeetingVideoPlace").style.display = "";
	}else if(pageX.cache.showMeetingVideoArea){
		//显示会议方式
		_$("#showMeetingVideoArea").style.display = "";
	}
}

/**
 * 权限控制处理
 */
function dealAuthCondition(){
	/**
	 * 会议地点
	 */
	if(typeof pageX.cache.isMeetingPlaceInputAble == 'undefined' || typeof pageX.cache.haveMeetingRoomApp == 'undefined'){
		return;
	}
	if(pageX.cache.isMeetingPlaceInputAble){
		setAttValue("meetingPlace", "placeholder", cmp.i18n("meeting.meetingCreate.inputMeetingPlace"));
	}else{
		if(!pageX.cache.haveMeetingRoomApp){
			document.querySelector("#liMeetingPlace").style.display = 'none';
		}
		setAttValue("meetingPlace", "placeholder", cmp.i18n("meeting.page.label.selectMeetingRoom"));
	}
	
	/**
	 * 签到二维码
	 */
	dealQrcodeSign();
	
	/**
	 * 外部会议
	 */
	if(pageX.cache.enablePublicMeeting){
		document.querySelector("#isPublic").style.display = 'block';
	}
	dealIsPublic();
}

/**
 * 处理会议二维码签到
 */
function dealQrcodeSign(){
	if(pageX.cache.qrCodeSign) {
		if(pageX.cache.qrCodeSign == "1"){
			if(!_$(".qrCodeSign").classList.contains("cmp-active")){
				_$(".qrCodeSign").classList.add("cmp-active");
			}
		}else{
			if(_$(".qrCodeSign").classList.contains("cmp-active")){
				_$(".qrCodeSign").classList.remove("cmp-active");
			}
		}
	}
}

/**
 * 外部会议开关
 */
function dealIsPublic(){
	if(pageX.cache.isPublic){
		if(pageX.cache.isPublic == "1"){
			if(!_$("#isPublic .cmp-switch").classList.contains("cmp-active")){
				_$("#isPublic .cmp-switch").classList.add("cmp-active");
			}
		}else{
			if(_$("#isPublic .cmp-switch").classList.contains("cmp-active")){
				_$("#isPublic .cmp-switch").classList.remove("cmp-active");
			}
		}
	}
}

//根据会议方式控制是否展示密码输入框
function showPassword(){
	if(_$("#meetingNature_value").value == "2"){
		_$("#meetingPassword").style.display = "";
	}else{
		_$("#meetingPassword").style.display = "none";
	}
}

//提前提醒选项数据
function reminderSelectData(){
	var data = [{
		value : "0",  //无
		text : cmp.i18n("meeting.meetingCreate.none")
	  },{
	  	value : "5",
	  	text : "5" + cmp.i18n("meeting.meetingCreate.minutes")
	  },{
	  	value : "10",
	  	text : "10" + cmp.i18n("meeting.meetingCreate.minutes")
	  },{
	  	value : "15",
	  	text : "15" + cmp.i18n("meeting.meetingCreate.minutes")
	  },{
	  	value : "30",
	  	text : "30" + cmp.i18n("meeting.meetingCreate.minutes")
	  },{
	  	value : "60",
	  	text : "1" + cmp.i18n("meeting.meetingCreate.hours")
	  },{
	  	value : "120",
	  	text : "2" + cmp.i18n("meeting.meetingCreate.hours")
	  },{
	  	value : "180",
	  	text : "3" + cmp.i18n("meeting.meetingCreate.hours")
	  },{
	  	value : "240",
	  	text : "4" + cmp.i18n("meeting.meetingCreate.hours")
	  },{
	  	value : "480",
	  	text : "8" + cmp.i18n("meeting.meetingCreate.hours")
	  },{
	  	value : "720",
	  	text : "0.5" + cmp.i18n("meeting.meetingCreate.day")
	  },{
	  	value : "1440",
	  	text : "1" + cmp.i18n("meeting.meetingCreate.day")
	  },{
	  	value : "2880",
	  	text : "2" + cmp.i18n("meeting.meetingCreate.day")
	  },{
	  	value : "4320",
	  	text : "3" + cmp.i18n("meeting.meetingCreate.day")
	  },{
	  	value : "10080",
	  	text : "1" + cmp.i18n("meeting.meetingCreate.week")
	  }];
	return data;
}

//已发
var isSubmit = false;
function send(type){
	//防止重复点击
	if(isSubmit){
		return;
	}
	isSubmit = true;
	
	//校验必录项
	if(hasEmpty()){
		isSubmit = false;
		return;
	}
	
	//会议室相关逻辑
	pageX.meetingPlace_type = pageX.meetingPlace_type ? pageX.meetingPlace_type : "mtRoom";
	var meetingPlace = "",
		roomId = "",
		roomAppId = "",
		roomAppBeginDate,
		roomAppEndDate;
	if(pageX.meetingPlace_type == "applied"){
		roomAppId = _$("#meetingPlace_value1").value;
		roomAppBeginDate = pageX.cache.datas.roomAppBeginDate + ":00";
		roomAppEndDate = pageX.cache.datas.roomAppEndDate + ":00";
	}else if(pageX.meetingPlace_type == "apply"){
		roomId = _$("#meetingPlace_value").value;
		roomAppBeginDate = pageX.cache.datas.roomAppBeginDate + ":00";
		roomAppEndDate = pageX.cache.datas.roomAppEndDate + ":00";
	}else if(pageX.meetingPlace_type == "mtPlace"){
		meetingPlace = _$("#meetingPlace").value;
	}
	//视频会议室相关逻辑(没有选择视频会议室时,则为普通会议)
	if (pageX.cache.showVideoRoomArea && (typeof(pageX.cache.datas.videoRoomId)=="undefined" || pageX.cache.datas.videoRoomId=="" || pageX.cache.datas.videoRoomId=="-1")) {
		_$("#meetingNature_value").value = "1";
	}
	
	//附件相关逻辑
	var isHasAtt = "false";
	if(null != pageX.fileComponent && pageX.fileComponent.getFileArray().length > 0){
		pageX.postData.attFileDomain = pageX.fileComponent.getFileArray();
		isHasAtt = "true";
	}
	
	var emojiUtil = cmp.Emoji();
	var paramData = {
		meetingId : typeof(urlParam)=="undefined"?null:urlParam.meetingId,
		title : emojiUtil.EmojiToString(_$("#meetingName").value),
		beginDate : _$("#startDate").value + " " + _$("#startDatetimeLabel").value + ":00",
		endDate : _$("#endDate").value + " " + _$("#endDatetimeLabel").value + ":00",
		conferees : _$("#conferees_value").value,
		emceeId : _$("#host_value").value,
		recorderId : _$("#recoder_value").value,
		impart : _$("#notify_value").value,
		beforeTime : _$("#reminder_value").value,
		content : getContentInfo(),
		selectRoomType : pageX.meetingPlace_type,
		meetingPlace : emojiUtil.EmojiToString(meetingPlace),
		roomId : roomId,
		roomAppId : roomAppId,
		roomAppBeginDate : roomAppBeginDate,
		roomAppEndDate : roomAppEndDate,
		_json_params : cmp.toJSON(pageX.postData),
		isHasAtt : isHasAtt,
		type : type,
		meetingTypeId: _$("#meetingType_value").value,
		meetingNature : _$("#meetingNature_value").value,
		password : emojiUtil.EmojiToString(_$("#password").value),
		videoRoomId : pageX.cache.datas.videoRoomId,
		oldVideoRoomAppId : pageX.cache.datas.oldVideoRoomAppId,
		videoRoomName : pageX.cache.datas.videoRoomName,
		videoRoomAppBeginDate : pageX.cache.datas.videoRoomStartDate + ":00",
		videoRoomAppEndDate : pageX.cache.datas.videoRoomEndDate + ":00",
		qrCodeSign : _$(".qrCodeSign").classList.contains("cmp-active") ? "1":"0",
		isPublic : _$("#isPublic .cmp-switch").classList.contains("cmp-active") ? "1":"0",
		sourceType : urlParam ? urlParam.sourceType : null,
		sourceId : urlParam ? urlParam.sourceId : null
	};
	
	//是否存在异常数据
	if(hasAbnormalData(paramData)){
		isSubmit = false;
		return;
	}
	
	if(type == "save"){
		checkBeginDate(paramData);
	}else if(type == "send"){
		//校验视频会议密码是否符合
		if(typeof(pageX.cache.datas.videoRoomId)!="undefined"){
			var password = _$("#password").value;
			if(password!=""){
				var regExp = /^[A-Za-z0-9@\-_*]{0,10}$/;
				if(!regExp.test(password)) {   
					createAlter(cmp.i18n("meeting.meetingCreate.check.password"), null);
					isSubmit = false;
					return false;
				}
			}
		}
		if(pageX.meetingPlace_type == "apply"){
		    //校验会议室是否冲突
            var checkMeetingRoomData = {
                roomId : roomId,
                meetingId : typeof(urlParam)=="undefined"?null:urlParam.meetingId,
                beginDate : roomAppBeginDate,
                endDate : roomAppEndDate
            }
            $s.Meeting.checkMeetingRoomConflict({}, checkMeetingRoomData, {
                success : function(result) {
                    if(result["errorMsg"] && result["errorMsg"]!="") {
                        createAlter(result["errorMsg"], null);
                        isSubmit = false;
                        return;
                    }
                    //校验与会人是否存在冲突
                    checkConfereesData(paramData);
                }
            })
		}else{
		    //校验与会人是否存在冲突
            checkConfereesData(paramData);
		}
	}
}

//校验与会人是否存在冲突
function checkConfereesData(paramData){
	var checkConfereesData = {
			"meetingId" : typeof(urlParam) == "undefined"?null:urlParam.meetingId,
			"beginDatetime" : new Date(paramData.beginDate.replace(/\-/g, "/")).getTime(),
			"endDatetime" : new Date(paramData.endDate.replace(/\-/g, "/")).getTime(),
			"emceeId" : paramData.emceeId,
			"recorderId" : paramData.recorderId,
			"conferees" : paramData.conferees,
			"errorMsg" : cmp.i18n("meeting.meetingCreate.conflict")
	}
	MeetingUtils.checkConfereesConflict(checkConfereesData, function(){
		checkBeginDate(paramData);
	}, function(){
		isSubmit = false;
	}, function(){
		isSubmit = false;
	});
}

//校验开始时间早于当前时间，是否提交
function checkBeginDate(paramData){
	if(Date.parse(new Date()) > Date.parse(paramData.beginDate.replace(/\-/g, "/"))){
		cmp.notification.confirm(cmp.i18n("meeting.meetingCreate.begin_date_validate"),function(e){ // e==1是/e==0 否
			if(e==1){
			    checkMeetingRoomDate(paramData);
			}else{
				isSubmit = false;
			}
		},null, [cmp.i18n("meeting.meetingCommon.cancel"), cmp.i18n("meeting.meetingCommon.continue")]);
	}else{
	    checkMeetingRoomDate(paramData);
	}
}
//校验已申请了会议室，会议时间与会议室使用时间不符，是否继续?
function checkMeetingRoomDate(paramData){
    if (paramData.selectRoomType == "apply" || paramData.selectRoomType == "applied") {
        if(Date.parse(paramData.roomAppBeginDate.replace(/\-/g, "/")) != Date.parse(paramData.beginDate.replace(/\-/g, "/"))
                || Date.parse(paramData.roomAppEndDate.replace(/\-/g, "/")) != Date.parse(paramData.endDate.replace(/\-/g, "/"))){
            cmp.notification.confirm(cmp.i18n("meeting.meeting.meetingroomTimeNeqMeetingTime"),function(e){ // e==1是/e==0 否
                if(e==1){
                    submit(paramData);
                }else{
                    isSubmit = false;
                }
            },null, [cmp.i18n("meeting.meetingCommon.cancel"), cmp.i18n("meeting.meetingCommon.continue")]);
        }else{
            submit(paramData);
        }
    }else{
        submit(paramData);
    }
}

//检查必录项
function hasEmpty(){
	var empty = cmp.i18n("meeting.meetingCreate.empty");
	
	//会议名称
	if(MeetingUtils.isNull(_$("#meetingName").value)){
		createAlter(cmp.i18n("meeting.meetingCreate.meetingName") + empty, null);
		return true;
	}

	//开始时间
	if(MeetingUtils.isNull(_$("#startDate").value)){
		createAlter(cmp.i18n("meeting.meetingCreate.startDate") + empty, null);
		return true;
	}

	//结束时间
	if(MeetingUtils.isNull(_$("#endDate").value)){
		createAlter(cmp.i18n("meeting.meetingCreate.endDate") + empty, null);
		return true;
	}

	//与会人
	if(MeetingUtils.isNull(_$("#conferees").value)){
		createAlter(cmp.i18n("meeting.meetingCreate.conferees") + empty, null);
		return true;
	}
	
	return false;
}

//是否存在异常数据
function hasAbnormalData(paramData){
	//校验开始结束时间
	if(Date.parse(paramData.beginDate.replace(/\-/g, "/")) >= Date.parse(paramData.endDate.replace(/\-/g, "/"))){
		createAlter(cmp.i18n("meeting.meetingCreate.checkTime"), null);
		return true;
	}
	//校验特殊字符
	if(specialCharacter(paramData.title)){
		createAlter(cmp.i18n("meeting.meetingCreate.specialCharacter", ["（# ￥ % & ~ < > / | \ \" '）"]), null);
		return true;
	}
	//校验输入字符数量
	if(paramData.title.length > 60){
		createAlter(cmp.i18n("meeting.meetingCreate.maxLength", [cmp.i18n("meeting.meetingCreate.meetingName"), 60, paramData.title.length]), null);
		return true;
	}
	if(paramData.meetingPlace.length > 60){
		createAlter(cmp.i18n("meeting.meetingCreate.maxLength", [cmp.i18n("meeting.meetingCreate.meetingPlace"), 60, paramData.meetingPlace.length]), null);
		return true;
	}
	return false;
}

function getUserPeivMenu(check, urlParam){
	$s.Meeting.meetingUserPeivMenu({}, {
		success : function(result) {
		   if(result) {
		       //会议室申请权限
		       pageX.haveMeetingRoomApp = result.haveMeetingRoomApp;
		       _showPlace(check, urlParam);
		   }
		},
		error : function(result){
        	//处理异常
        	MeetingUtils.dealError(result);
		}
	});
}

//校验会议名称特殊字符
function specialCharacter(v){
	var patrn = /^[^#￥%&~<>/|\"']*$/;
	if(!patrn.test(v)){
		return true;
	}
	return false;
}

//获取正文信息
function getContentInfo(){
	if(MeetingUtils.isNotNull(pageX.cache.content)){
		if(pageX.action == "modify"){
			return pageX.cache.content;
		}
		
		return escapeStringToHTML(pageX.cache.content);
	}
	if(MeetingUtils.isNotNull(_$("#content").value)){
		var emojiUtil = cmp.Emoji();
		if(pageX.action == "modify"){
			return emojiUtil.EmojiToString(_$("#content").value);
		}
		
		return escapeStringToHTML(emojiUtil.EmojiToString(_$("#content").value));
	}
	return "";
}

function setAttValue(id, type, value){
	document.getElementById(id).setAttribute(type, value);
}

function createAlter(msg, callback){
	cmp.notification.alert(msg, callback, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
}

//会议名称增加输入控制
_$("#meetingName").oninput = function(){
	resizeMeetingNameHeight();
}

function resizeMeetingNameHeight(){
	_$("#shadowMeetingName").style.height = "0px";
	_$("#shadowMeetingName").value = _$("#meetingName").value;
	var scrollHeight = _$("#shadowMeetingName").scrollHeight;
	
	if((scrollHeight-10)/21 >= 4){
		scrollHeight = 84;
	}
	_$("#liMeetingName").style.height = (scrollHeight + 30) + "px";
	_$("#meetingName").style.height = scrollHeight + "px";
	_$("#sMeetingName").style.height = scrollHeight + "px";
}

//会议地点增加输入控制
_$("#meetingPlace").oninput = function(){
	resizeMeetingPlaceHeight();
}

function resizeMeetingPlaceHeight(){
	_$("#shadowMeetingPlace").style.height = "0px";
	_$("#shadowMeetingPlace").value = _$("#meetingPlace").value;
	var scrollHeight = _$("#shadowMeetingPlace").scrollHeight;
	
	if((scrollHeight-10)/21 >= 2){
		scrollHeight = 42;
	}
	_$("#liMeetingPlace").style.height = (scrollHeight + 30) + "px";
	_$("#meetingPlace").style.height = scrollHeight + "px";
	_$("#showMeetingPlace").style.height = scrollHeight + "px";
}

/**
 * 解决ios软键盘挡住弹出的选项
 */
function inputBlur(){
	_$("#meetingName").blur();
}