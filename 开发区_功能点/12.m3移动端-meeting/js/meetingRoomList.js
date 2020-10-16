var urlParam = {};
var isSearch = false; //是否查询，用于控制是否覆盖列表数据
var showChoiceBtn = false;//通过此变量与applyState共同控制是否显示按钮
var nextStartDate, nextEndDate; //传入到下个页面的开始、结束时间
var showToday = false; //状态是否展示"今日"
var cacheKey_mrlStorgeDatas = "m3_v5_meeting_meetingRoomList_queryParams"; //会议室列表查询条件
var pageX = {};
pageX.meetingCreateCache = {};
pageX.searchCacheKey = "m3_v5_meeting_room_search_cache_key";
pageX.searchCondition = {};
pageX.cachData = {};
var roomListView;
/**
 * 接收参数描述
 * action      执行动作
 *     applyMeetingRoom   申请会议室
 *     createMeeting      新建会议
 * cacheKey_mcBackDatas   缓存key，申请会议室时，跳转过来后需要返回的数据
 */
cmp.ready(function () {
	if (_getQueryString("VJoinOpen") == "VJoin") {
        //对VJoin穿透过来的新闻进行处理
        urlParam['action'] = _getQueryString("action");
    } else {
        urlParam = cmp.href.getParam() || {};
    }
	if(urlParam.isFromM3NavBar && urlParam.isFromM3NavBar == "true"){
		cmp.storage.save("isFromM3NavBar", true, true);
	}
	initPageBack();
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.meetingRoomList.list"));
		initPageData();
		initEvent();
	},meetingBuildVersion);
});

function initPageBack() {
	//cmp控制返回
    cmp.backbutton();
	cmp.backbutton.push(_goBack);
	//注册webview事件
	MeetingUtils.addMeetingWebviewEvent(webviewEvent);
}

/**
 * 多webview事件
 * @param {*} data 
 */
var webviewEvent = function(parameters){
	var data = parameters.data;
	if(data && data.isRefresh){
		roomListView && roomListView.refreshInitData();
	}
}

function _goBack() {
	cmp.href.back();
}

function initPageData(){
	
	var searchCacheData = cmp.storage.get(pageX.searchCacheKey, true);
    if(searchCacheData){
        pageX.searchCondition = JSON.parse(searchCacheData);
        cmp.storage["delete"](pageX.searchCacheKey, true);
    }

    var queryParams = cmp.storage.get(cacheKey_mrlStorgeDatas, true);
    
    if(queryParams){
		queryParams = cmp.parseJSON(queryParams);
		var condition = queryParams.queryType;  //传递过来的参数搜索条件
		pageX.searchCondition.condition = condition;
		if("roomName" == condition){
   		 pageX.searchCondition.value = queryParams.roomName;
   		 pageX.searchCondition.type = "text";
   		 pageX.searchCondition.text = cmp.i18n("meeting.meetingRoomList.queryName");
	   	}else if("time" == condition){
	   		pageX.searchCondition.text = cmp.i18n("meeting.meetingRoomList.queryTime");
	   		pageX.searchCondition.dateBegin = queryParams.startDate;
	   		pageX.searchCondition.dateEnd = queryParams.endDate;
	   		pageX.searchCondition.type = "date";
	   	}
		//清空查询条件缓存
		cmp.storage["delete"](cacheKey_mrlStorgeDatas, true);
    }
    initPageList();
}

function initPageList() {
	//重置状态
	showChoiceBtn = false;
	//搜索条件
	if (pageX.searchCondition.condition != undefined) {
		_$("#searchHeader").style.display = "none";
		_$("#reSearch").style.display = "block";
		
		if (pageX.searchCondition.condition != "time") {
	      	_$("#searchText").style.display = "block";
	      	_$("#searchDate").style.display = "none";
	      	_$("#cmp_search_title").innerHTML = pageX.searchCondition.text;
	      	_$("#searchTextValue").value = pageX.searchCondition.value;
	      	pageX.cachData[pageX.searchCondition.condition] = pageX.searchCondition.value;
	    } else {
	      	_$("#searchText").style.display = "none";
	      	_$("#searchDate").style.display = "block";
	      	_$("#cmp_search_title").innerHTML = pageX.searchCondition.text;
	      	_$("#searchDateBeg").value = pageX.searchCondition.dateBegin;
	      	_$("#searchDateEnd").value = pageX.searchCondition.dateEnd;
	      	pageX.cachData.startDate = pageX.searchCondition.dateBegin;
        	pageX.cachData.endDate =pageX.searchCondition.dateEnd;
        	nextStartDate = pageX.searchCondition.dateBegin;
        	nextEndDate = pageX.searchCondition.dateEnd;
        	showChoiceBtn = true;
	   		
	   		var l_startTime = new Date(nextStartDate.replace(/\-/g, '/')).getTime();
	   		var l_endTime = new Date(nextEndDate.replace(/\-/g, '/')).getTime();
	   		if(l_startTime > l_endTime){
	   			return;
	   		}
	   		showToday = false;
	    }
		pageX.cachData.condition = pageX.searchCondition.condition;
	} else {
		_$("#searchHeader").style.display = "block";
		_$("#reSearch").style.display = "none";
		showToday = true;
	}
	if (urlParam['action'] == "createVideoMeeting") {
		pageX.cachData.openFrom = "createVideoMeeting";
	}
	getMeetingRooms([{},pageX.cachData]);
}

function initEvent(){
	 /****************查询绑定****************/  
    _$('#search').addEventListener("tap",function(){
  		searchFn(null);
  	});
    
    _$("#toSearch").addEventListener("tap",function(){
    	var params = {};
    	params.type = pageX.searchCondition.type;
    	params.text = pageX.searchCondition.text;
    	params.condition = pageX.searchCondition.condition;
    	if(pageX.searchCondition.type == "date") {
    		params.value = [pageX.searchCondition.dateBegin,pageX.searchCondition.dateEnd];
    	} else {
    		params.value = pageX.searchCondition.value;
    	}
    	params.modelId = pageX.searchCondition.modelId;
    	searchFn(params);
    });
    
    
    //取消重新加载页面
    _$("#cancelSearch").addEventListener("tap",function(){
    	//重置搜索条件
    	pageX.cachData = {};
    	pageX.searchCondition = {};
    	isSearch = true;
    	initPageList();
    });
    
    //视频会议室不支持点击详情
    if (urlParam['action'] != "createVideoMeeting") {
    	cmp("#meetingRoomList").on("tap", ".room_list", function(e) {
    		var action = urlParam.action;
    		//将查询条件放在session中,返回的时候需要用到
    		var condition = pageX.searchCondition.condition;
    		var params = {};
    		if ("roomName" == condition) {
    			params = {
    					type : "query",
    					queryType : condition,
    					roomName : pageX.searchCondition.value
    			};
    		} else if("time" == condition){
    			params = {
    					type : "query",
    					queryType : condition,
    					startDate : _$("#searchDateBeg").value,
    					endDate : _$("#searchDateEnd").value
    			};
    		}
    		cmp.storage.save(cacheKey_mrlStorgeDatas, cmp.toJSON(params), true);
    		var roomId = this.getAttribute("roomId");
    		
    		/**
    		 * 点击图片跳转会议室详情
    		 */
			var option = {
				pushInDetailPad : true
			};

    		var target = e.target;
    		if(target.tagName.toLowerCase() == 'img' || target.classList.contains("img_wrap")){
    			cmp.href.next(_meetingPath + "/html/meetingRoomDetail.html", {roomId : roomId},option);
    			return;
    		}

    		var paramData = {
    				roomId : roomId,
    				roomName : this.getAttribute("roomName"),
    				action : action,
    				formChooseKey : urlParam.formChooseKey,
    				roomNeedApp : document.getElementById("roomNeedApp_"+ roomId).value=="true" ? 1 : 0,
    						cacheKey_mrlStorgeDatas : cacheKey_mrlStorgeDatas,
    						cacheKey_mcBackDatas : urlParam.cacheKey_mcBackDatas
    		};
    		cmp.href.next(_meetingPath + "/html/meetingRoomOccupancyCondition.html"+meetingBuildVersion, paramData,option);
    	});
    }
	
	cmp("#meetingRoomList").on("tap", ".choice_room", function(e) {
		e.stopPropagation();
		
		var action = urlParam.action;
		
		var paramData = {
			roomName : this.getAttribute("roomName"),
			roomId : this.getAttribute("roomId"),
			startDate : nextStartDate,
			endDate : nextEndDate,
			action : action
		};
		if(action == "createMeeting"){
			if(Date.parse(new Date()) > Date.parse(new Date(nextEndDate.replace(/\-/g, "/")))){
				cmp.notification.alert(cmp.i18n("meeting.meetingRoomList.beforeNowTime"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
				return;
			}
			
			pageX.meetingCreateCache = {
				roomName : this.getAttribute("roomName"),
				roomId : this.getAttribute("roomId"),
				startDate : nextStartDate,
				endDate : nextEndDate,
				meetingPlace_type : "apply"
			};
			cmp.storage.save(urlParam.cacheKey_mcBackDatas, cmp.toJSON(pageX.meetingCreateCache), true);
			
			//删除跳转前存入的缓存
			cmp.storage["delete"](cacheKey_mrlStorgeDatas, true);
			
			cmp.href.back();
		}else if(action == "createVideoMeeting"){
			if(Date.parse(new Date()) > Date.parse(new Date(nextEndDate.replace(/\-/g, "/")))){
				cmp.notification.alert(cmp.i18n("meeting.meetingRoomList.beforeNowTime"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
				return;
			}
			
			pageX.meetingCreateCache = {
				videoRoomName : this.getAttribute("roomName"),
				videoRoomId : this.getAttribute("roomId"),
				videoRoomStartDate : nextStartDate,
				videoRoomEndDate : nextEndDate,
				meetingNature_value : 2
			};
			cmp.storage.save(urlParam.cacheKey_mcBackDatas, cmp.toJSON(pageX.meetingCreateCache), true);
			
			//删除跳转前存入的缓存
			cmp.storage["delete"](cacheKey_mrlStorgeDatas, true);
			
			cmp.href.back();
		} else if(action == "applyMeetingRoom"){
			cmp.href.next(_meetingPath + "/html/meetingRoomApply.html"+meetingBuildVersion, paramData);
		} else if (action = "formChoose") {//表单自定义控件选择会议室
			var cacheValue = {};
			var roomId = this.getAttribute("roomId");
			var roomNeedApp = document.getElementById("roomNeedApp_"+ roomId).value=="true" ? 1 : 0;
			var display = this.getAttribute("roomName")+"("+nextStartDate+"--"+nextEndDate+")";//表单展示值
			var value = this.getAttribute("roomName")+","+roomNeedApp+","+nextStartDate+","+nextEndDate+","+roomId;//表单保存值
			cacheValue.display = display;
			cacheValue.value = value;
			cmp.storage.save(urlParam.formChooseKey, cmp.toJSON(cacheValue), true);
			
			cmp.href.back();
		}
	});
}
//添加缓存
function intListCache() {
	//保存查询条件的缓存
	cmp.storage.save(pageX.searchCacheKey, JSON.stringify(pageX.searchCondition), true);
}

function getMeetingRooms(params){
	roomListView = cmp.listView("#pullrefresh",{
		imgCache:true,
		config: {
		    isClear: false,
		    clearCache: isListViewRefresh(),
	        params: params,
	        dataFunc: function(fn1, params, options){
	        	$s.Meeting.getMeetingRooms({}, params, {
		    		success : function(result) {
		    			if(options.success) {
		            		options.success(result);
		            	}
		            },
		            error : function(result){
		            	options.error();
		            	//处理异常
		            	MeetingUtils.dealError(result);
		            }
		        })
	        },
	        renderFunc: renderData
		},
		down: {
  	  		contentprepage:cmp.i18n("meeting.page.lable.prePage"),//上一页
  	  		contentdown:cmp.i18n("meeting.page.action.pullDownRefresh"),
  	  		contentover: cmp.i18n("meeting.page.action.loseRefresh"),
  	  		contentrefresh: cmp.i18n("meeting.page.state.refreshing")
  	  	},
  	  	up: {
	      	contentnextpage:cmp.i18n("meeting.page.lable.nextPage"),//下一页
	        contentdown: cmp.i18n("meeting.page.action.loadMore"),
	        contentrefresh: cmp.i18n("meeting.page.state.loading"),
	        contentnomore: cmp.i18n("meeting.page.state.noMore")
  	  	}
   });
}

function renderData(result, isRefresh){
    var pendingTPL = _$("#list_li_meetingRoom").innerHTML;
    var html = cmp.tpl(pendingTPL, result);
    if (isRefresh || isSearch) {//是否刷新操作，刷新操作 直接覆盖数据
        _$("#meetingRoomList").innerHTML = html;
        isSearch = false;
    } else {
    	var table = _$("#meetingRoomList").innerHTML;
    	_$("#meetingRoomList").innerHTML = table + html;
    }
    cmp.IMG.detect();
}
//通过传入的type区分是点击放大镜图标还是已选的条件
function searchFn(params){
	if(params == null) {
		var initDate = getInitDate();
		params = {type : "date",condition : "time",text : cmp.i18n("meeting.meetingRoomList.queryTime"),value : [initDate, ""]};
	}
	var searchObj = [{type:"date",condition:"time",text:cmp.i18n("meeting.meetingRoomList.queryTime")},
	             {type:"text",condition:"roomName",text:cmp.i18n("meeting.meetingRoomList.queryName")}];
	if (urlParam['action'] == "createVideoMeeting") {
		searchObj = [{type:"date",condition:"time",text:cmp.i18n("meeting.meetingRoomList.queryTime")}];
	}
	cmp.search.init({
    	id:"#search",
        model : {                    //定义该搜索组件用于的模块及使用者的唯一标识（如：该操作人员的登录id）搜索结果会返回给开发者
            name:"meetingRoom",   //模块名，如："协同"，名称开发者自定义
            id:"9009"           //模块的唯一标识：
		},
        parameter : params,
        TimeQueryControl : false, //两个时间框只选择一个时间后是否立即执行查询
        dateOptions : {
        	type : "dateTimeCalender",
        	MinutesScale : "5"
        }, // 5分钟刻度类型的日期组件
        items : searchObj,
        TimeMin : false,//false：第一个时间小于当前时间，不进行查询
        TimeAlert:true,//控制历史记录搜索弹出框只显示一个确定按钮
        lessTime : cmp.i18n("meeting.meetingRoomList.beforeToday"),
        callback : searchCallback //回调函数：会将输入的搜索条件和结果返回给开发者
    });
}
//处理查询返回数据
function searchCallback(result){
	if (pageX.searchCondition.condition != undefined) {
		pageX.cachData = {};
	}
    var data = result.item;   //返回的搜索相关的数据
    var condition = data.condition;  //返回的搜索条件
    var dataSoure = "";        //搜索输入的数据  如果type="text",为普通文本，如果type="date"
    var type  = data.type;       //搜索输入的数据类型有text和date两种
    
    var tSearhContent = _$("#CMP_SearchContent");
    if(tSearhContent){
        tSearhContent.style.display = "none";
    }
    if (type == "date") {
    	pageX.cachData.startDate = result.searchKey[0];
    	pageX.cachData.endDate =result.searchKey[1];
    	pageX.searchCondition.dateBegin = result.searchKey[0];
    	pageX.searchCondition.dateEnd = result.searchKey[1];
    	//开始时间早于当前时间，不执行查询
    	if(beforeNowTime(pageX.cachData.startDate)){
    		return;
    	}
    } else {
    	pageX.cachData[condition] = result.searchKey[0];
    	dataSoure = result.searchKey[0];
    }
    pageX.cachData.condition = condition;
	//查询条件返回
    pageX.searchCondition.type = type;
    pageX.searchCondition.condition = condition;
    pageX.searchCondition.text = data.text;
    pageX.searchCondition.value = dataSoure;

	isSearch = true;
	initPageList();
}
//获取初始化的时间  开始时间是当前时间后接近的半点或整点
function getInitDate(){
	var startDate, minutes;
	var nowDate = new Date();
	var nowMinutes = nowDate.getMinutes();
	if(nowMinutes == 30 || nowMinutes == 0){
		startDate = nowDate;
	}else{
		minutes = nowMinutes - 30 > 0 ? 60 - nowMinutes : 30 - nowMinutes;
		startDate = new Date(Date.parse(nowDate) + (60000 * minutes));
	}
	return formatDate(startDate);
}
//格式化时间，yyyy-MM-dd hh:mm 格式
function formatDate(time){
	var year = time.getFullYear(),
		month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
		date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate(),
		hours = time.getHours() == 0 ? "00" : time.getHours() < 10 ? "0" + time.getHours() : time.getHours(),
		minutes = time.getMinutes() == 0 ? "00" : time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes();
	
	return year + "-" + month + "-" + date + " " + hours + ":" + minutes;
}
//开始时间早于当前时间
function beforeNowTime(beginTime){
	var l_startTime = new Date(beginTime.replace(/\-/g, '/')).getTime();
	var l_nowTime = new Date().getTime();
	if(l_startTime < l_nowTime){
		//cmp.notification.alert(cmp.i18n("meeting.meetingRoomList.beforeNowTime"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
		return true;
	}
	return false;
}
//解析url方法
function _getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
}