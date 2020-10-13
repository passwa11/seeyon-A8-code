/*
 * 会议室已申请和待审核列表
 */
var urlParam;
var page = {
	loadAll : false, //是否刷新之前的所有数据
	meetingCreateCache :{},
	searchCacheKey : "m3_v5_meeting_room_search_cache_key",
	searchCondition : {},
	listViewKey : {}, //存放listview对象
	cachData : {}
};
var $fillArea = ""; //当前列表容器，用于页面加载
var $fillTpl = "";  //当前列表数据模板
var refreshListview = false;
var _currentListDiv = ""; //当前列表

var isSearch = false; //是否查询，用于控制是否覆盖列表数据
var showChoiceBtn = false;//通过此变量与applyState共同控制是否显示按钮
var nextStartDate, nextEndDate; //传入到下个页面的开始、结束时间
var showToday = false; //状态是否展示"今日"
var cacheKey_mrlStorgeDatas = "m3_v5_meeting_meetingRoomList_queryParams"; //会议室列表查询条件

/**
 * 是否清除当前列表缓存
 */
var isClearCacheList = true;

var mrlistKey = "m3_v5_meeting_room_list_type";
cmp.ready(function () {
	
	urlParam = cmp.href.getParam() || {};
	initPageBack();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		
		//管理员权限判断
		$s.Meeting.meetingUserPeivMenu({},{
			success : function(result) {
			    page.haveMeetingRoomApp = result.haveMeetingRoomApp;
			    if(urlParam.openFrom && urlParam.openFrom == "meetingCreate"){//没有页签
					_$("#segmentedControl").style.display ="none";
					_$("#dataCommonDiv").style.top ="0";
					_$("#mrApprovesContainDiv").classList.add("border_t");
					document.title = cmp.i18n("meeting.meetingRoomCommon.alreadyApplied");
				}else if (!result.data) {//只有会议室申请页签
			    	document.title = cmp.i18n("meeting.meetingRoomCommon.alreadyApplied");
					_$("#segmentedControl").style.display ="";
			    }else{//管理员页签
					_$("#segmentedControl").style.display ="";
					_$("#mrApprovesContainDiv").classList.remove("border_t");
					document.title = cmp.i18n("meeting.meetingRoomCommon.meetingRoom");
					_$("#listMR").style.display = "";
					page.listType = "mrList";
				}
				if (result.haveMeetingRoomPerm && "meetingCreate" != urlParam.openFrom) {//有会议室审核
				    _$("#listMRAudit").style.display = "";
				}
				if (result.haveMeetingRoomApp) {//有会议室申请
				    _$("#listMRApprove").style.display = "";
                }
				if(urlParam.listType){
					page.listType = urlParam.listType;
				}
				//显示下方会议页签
				var meetingRole = result.haveMeetingPendingRole || result.haveMeetingDoneRole || result.haveMeetingArrangeRole;
				page.meetingRole = meetingRole;
				
				//审核权限
				page.haveMeetingRoomPerm = result.haveMeetingRoomPerm;
				
				//初始化列表及数据
				initPageData();
				//初始化会议点击事件
				initEvent();
			},
			error : function(result){
				//处理异常
	        	MeetingUtils.dealError(result);
			}
		});
		
	},meetingBuildVersion);
	
});

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
	cmp.backbutton.push(function(){
		cmp.href.back();
	});
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
		setTimeout(function(){
			for(var i in page.listViewKey){
				page.listViewKey[i].refreshInitData();
			}
		},500);
	}
}

function _goBack() {
	
	var backCount = 0;
	
	var openFrom = urlParam.openFrom;
    if(typeof(openFrom) == "undefined" || "meetingCreate" != openFrom){
    	var historyURL = cmp.parseJSON(cmp.storage.get("cmp-href-history",true));
    	
    	var historyURLCount = historyURL.length -1 ;
    	if(historyURLCount > 0) {
    		for(var i=historyURL.length;i>=0;i--) {
    			if(historyURL[i] != undefined && historyURL[i].url.indexOf("meeting_list_pending") != -1) {
    				backCount = historyURL.length - i;
    			}
    		}
    	}
    }
    if(!page.meetingRole) {
        if (backCount==0 || backCount==1) {
            backCount = 2;
        } else{
            backCount++;
        }
    }
	
	if(MeetingUtils.getBackURL() == "weixin"){
        //返回到外层, 微信入口逻辑，因为微信没办法返回到首页，所以这样处理， 暂时不要和else分支合并
        cmp.href.closePage();
    }else {
        //返回到外层
        cmp.href.back(backCount);
    }
}

//添加缓存
function intListCache() {
	//当前列表显示的缓存
	cmp.storage.save(mrlistKey, page.listType, true);
}

function initEvent(){
	//跳转到会议室列表
    _$("#listMR").addEventListener("tap",fnGoMRList);
    //跳转到已申请列表
    _$("#listMRApprove").addEventListener("tap",fnGoMRApproveList);
    //跳转到审核列表
    _$("#listMRAudit").addEventListener("tap",fnGoMRAuditList);
    
    var openFrom = urlParam.openFrom;
    if(openFrom && "meetingCreate" == openFrom){
    	//返回
    	cmp("#dataCommonDiv").on("tap", ".detail_wrap", function() {
    		var meetingCreateCache = {
				roomName : this.getAttribute("roomName"),
				roomId : this.getAttribute("roomId"),
				roomappId : this.getAttribute("roomappId"),
				startDate : this.getAttribute("startDatetime"),
				endDate : this.getAttribute("endDatetime"),
				meetingPlace_type : "applied"
			};
			cmp.storage.save(urlParam.cacheKey_mcBackDatas, cmp.toJSON(meetingCreateCache), true);
    		
    		cmp.href.back();
    	});
    }else{
    	//点击已申请会议室展开详情页面
    	cmp("#mrApproves").on("tap", ".detail_wrap", function() {
    		var paramData = {
				"openFrom" : getOpenFrom(),
				"roomAppId" : this.getAttribute("roomappId")
    		}
    		/**
    		 * 跳转前缓存
    		 */
    		page.listType = "mrApproveList";
			intListCache();
			
			var option = {
				pushInDetailPad : true
			};
    		if(MeetingUtils.isFromM3NavBar()){
				option.openWebViewCatch = true;
				paramData.isFromM3NavBar = "true";
			}
    		cmp.href.next(_meetingPath + "/html/meetingRoomApprove.html"+meetingBuildVersion, paramData,option);
    	});
    	//点击已审核会议室展开详情页面
    	cmp("#mrAudits").on("tap", ".detail_wrap", function() {
    		var paramData = {
				"openFrom" : getOpenFrom(),
				"roomAppId" : this.getAttribute("roomappId")
    		}
    		/**
    		 * 跳转前缓存
    		 */
    		page.listType = "mrAuditList";
			intListCache();
			
			var option = {
				pushInDetailPad : true
			};
    		cmp.href.next(_meetingPath + "/html/meetingRoomApprove.html"+meetingBuildVersion, paramData,option);
    	});
    	//点击会议室列表展开会议室占用详情页面
    	cmp("#mrListUL").on("tap", ".room_list", function(e) {
        	var action = urlParam.action;
        	//将查询条件放在session中,返回的时候需要用到
        	var condition = page.searchCondition.condition;
        	var params = {};
        	if ("roomName" == condition) {
        		params = {
        				type : "query",
        				queryType : condition,
        				roomName : page.searchCondition.value
        		};
        	} else if("time" == condition){
        		params = {
        				type : "query",
        				queryType : condition,
        				startDate : _$("#searchDateBeg").value,
        				endDate : _$("#searchDateEnd").value
        		};
        	}
        	
        	intListCache();
        	cmp.storage.save(cacheKey_mrlStorgeDatas, cmp.toJSON(params), true);
        	
        	var roomId = this.getAttribute("roomId");
        	var roomName = this.getAttribute("roomName");
        	
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
    			roomName : roomName,
    			action : action,
    			formChooseKey : urlParam.formChooseKey,
    			roomNeedApp : document.getElementById("roomNeedApp_"+ roomId).value=="true" ? 1 : 0,
    			cacheKey_mrlStorgeDatas : cacheKey_mrlStorgeDatas,
    			cacheKey_mcBackDatas : urlParam.cacheKey_mcBackDatas
        	};
        	cmp.href.next(_meetingPath + "/html/meetingRoomOccupancyCondition.html"+meetingBuildVersion, paramData,option);
        });
    	//会议室选择事件
    	cmp("#mrListUL").on("tap", ".choice_room", function(e) {
    		e.stopPropagation();
    		var paramData = {
    			roomName : this.getAttribute("roomName"),
    			roomId : this.getAttribute("roomId"),
    			startDate : nextStartDate,
    			endDate : nextEndDate,
    			action : "applyMeetingRoom"
    		};
    		cmp.href.next(_meetingPath + "/html/meetingRoomApply.html"+meetingBuildVersion, paramData);
    	});
    }

	//查询绑定
    _$('#search').addEventListener("tap",function(){
  		searchFn(null);
  	});
    _$("#toSearch").addEventListener("tap",function(){
    	var params = {};
    	params.type = page.searchCondition.type;
    	params.text = page.searchCondition.text;
    	params.condition = page.searchCondition.condition;
    	if(page.searchCondition.type == "date") {
    		params.value = [page.searchCondition.dateBegin,page.searchCondition.dateEnd];
    	} else {
    		params.value = page.searchCondition.value;
    	}
    	params.modelId = page.searchCondition.modelId;
    	searchFn(params);
    });
    
    //取消查询重新加载页面
    _$("#cancelSearch").addEventListener("tap",function(){
    	//重置搜索条件
    	page.cachData = {};
    	page.searchCondition = {};
    	isSearch = true;
    	initPageList();
    	//点击取消时，重置listview的crumbsId
		if(page.searchCondition.searchCrumbsId){
			delete page.searchCondition.searchCrumbsId;
		}
    });
}

//查询组件初始化
function searchFn(params){
	if(params == null) {
		var initDate = getInitDate();
		params = {type : "date",condition : "time",text : cmp.i18n("meeting.meetingRoomList.queryTime"),value : [initDate, ""]};
	}
	var searchObj = [{type:"date",condition:"time",text:cmp.i18n("meeting.meetingRoomList.queryTime")},
	                 {type:"text",condition:"roomName",text:cmp.i18n("meeting.meetingRoomList.queryName")}];

	cmp.search.init({
		id:"#search",
		model : {                    //定义该搜索组件用于的模块及使用者的唯一标识（如：该操作人员的登录id）搜索结果会返回给开发者
			name:"meetingRoom",   //模块名，如："协同"，名称开发者自定义
			id:"9009"           //模块的唯一标识：
		},
		parameter : params,
		TimeQueryControl : false, //两个时间框只选择一个时间后是否立即执行查询
		dateOptions : {
			type : "",
			MinutesScale : "5"
		}, // 5分钟刻度类型的日期组件
		items : searchObj,
		//TimeMin : false,//false：第一个时间小于当前时间，不进行查询
		//TimeAlert:true,//控制历史记录搜索弹出框只显示一个确定按钮
		//TimeNow:false,
		//lessTime : cmp.i18n("meeting.meetingRoomList.beforeToday"),
		callback : searchCallback //回调函数：会将输入的搜索条件和结果返回给开发者
	});
}
//处理查询返回数据
function searchCallback(result){
	if (page.searchCondition.condition != undefined) {
		page.cachData = {};
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
    	page.cachData.startDate = result.searchKey[0];
    	page.cachData.endDate =result.searchKey[1];
    	page.searchCondition.dateBegin = result.searchKey[0];
    	page.searchCondition.dateEnd = result.searchKey[1];
    	//开始时间早于当前时间，不执行查询
//  	if(beforeNowTime(page.cachData.startDate)){
//  		cmp.notification.alert(cmp.i18n("meeting.meetingRoomList.beforeNowTime"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
//  		return;
//  	}
    } else {
    	page.cachData[condition] = result.searchKey[0];
    	dataSoure = result.searchKey[0];
    }
    page.cachData.condition = condition;
	//查询条件返回
    page.searchCondition.type = type;
    page.searchCondition.condition = condition;
    page.searchCondition.text = data.text;
    page.searchCondition.value = dataSoure;
    //查询时listview要加上crumbsId参数
    page.searchCondition.searchCrumbsId = "search" + cmp.buildUUID();

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


/**
 * retrun mrApproveList、mrAuditList （会议室已申请列表、会议室审核列表）
 */
function getOpenFrom(){
	return page.listType;
}

/**
 * 跳转到会议室列表
 */
function fnGoMRList() {
	if (!_$("#listMR").classList.contains("cmp-active")) {
		page.listType = "mrList";
		loadData("#mrListContainDiv",$s.Meeting.getMeetingRooms,[{},page.cachData]);
	}
}

/**
 * 跳转到会议室已申请列表
 */
function fnGoMRApproveList() {
	if (!_$("#listMRApprove").classList.contains("cmp-active")) {
		page.listType = "mrApproveList";
		loadData("#mrApprovesContainDiv",$s.Meeting.getApplyMeemtingRooms,[{},page.cachData]);
	}
}
/**
 * 跳转到会议室审核列表
 */
function fnGoMRAuditList() {
	if (!_$("#listMRAudit").classList.contains("cmp-active")) {
		page.listType = "mrAuditList";
		loadData("#mrAuditsContainDiv",$s.Meeting.getMeetingRoomAudits,[{},page.cachData]);
	}
}

function initPageData() {
	
	var searchCacheData = cmp.storage.get(page.searchCacheKey, true);
    if(searchCacheData){
        page.searchCondition = JSON.parse(searchCacheData);
        cmp.storage["delete"](page.searchCacheKey, true);
    }
    var queryParams = cmp.storage.get(cacheKey_mrlStorgeDatas, true);
    if(queryParams){
		queryParams = cmp.parseJSON(queryParams);
		var condition = queryParams.queryType;  //传递过来的参数搜索条件
		page.searchCondition.condition = condition;
		if("roomName" == condition){
   		 page.searchCondition.value = queryParams.roomName;
   		 page.searchCondition.type = "text";
   		 page.searchCondition.text = cmp.i18n("meeting.meetingRoomList.queryName");
	   	}else if("time" == condition){
	   		page.searchCondition.text = cmp.i18n("meeting.meetingRoomList.queryTime");
	   		page.searchCondition.dateBegin = queryParams.startDate;
	   		page.searchCondition.dateEnd = queryParams.endDate;
	   		page.searchCondition.type = "date";
	   	}
		//清空查询条件缓存
		cmp.storage["delete"](cacheKey_mrlStorgeDatas, true);
    }
	
	var openFrom = urlParam.openFrom;
    if(!openFrom || "meetingCreate" != openFrom){
        if (page.haveMeetingRoomApp) {
            _$("#listMR").style.display = "";
        }
    }
	
    initPageList();
}

function initPageList() {
	//重置状态
	showChoiceBtn = false;
	//搜索条件
	if (page.searchCondition.condition != undefined) {
		_$("#searchHeader").style.display = "none";
		_$("#reSearch").style.display = "block";
		if (page.searchCondition.condition != "time") {
	      	_$("#searchText").style.display = "block";
	      	_$("#searchDate").style.display = "none";
	      	_$("#cmp_search_title").innerHTML = page.searchCondition.text;
	      	_$("#searchTextValue").value = page.searchCondition.value;
	      	page.cachData[page.searchCondition.condition] = page.searchCondition.value;
	    } else {
	      	_$("#searchText").style.display = "none";
	      	_$("#searchDate").style.display = "block";
	      	_$("#cmp_search_title").innerHTML = page.searchCondition.text;
	      	_$("#searchDateBeg").value = page.searchCondition.dateBegin;
	      	_$("#searchDateEnd").value = page.searchCondition.dateEnd;
	      	page.cachData.startDate = page.searchCondition.dateBegin;
        	page.cachData.endDate =page.searchCondition.dateEnd;
        	nextStartDate = page.searchCondition.dateBegin;
        	nextEndDate = page.searchCondition.dateEnd;
        	showChoiceBtn = true;
	   		
	   		var l_startTime = new Date(nextStartDate.replace(/\-/g, '/')).getTime();
	   		var l_endTime = new Date(nextEndDate.replace(/\-/g, '/')).getTime();
	   		if(l_startTime > l_endTime){
	   			return;
	   		}
	   		showToday = false;
	    }
		page.cachData.condition = page.searchCondition.condition;
		isSearch = true;
	} else {
		_$("#searchHeader").style.display = "block";
		_$("#reSearch").style.display = "none";
		showToday = true;
	}
	
	//从缓存从读取当前显示的页签。默认为会议室列表
	var listTypeValue = cmp.storage.get(mrlistKey, true) || page.listType;
    if(listTypeValue){
    	page.listType = listTypeValue;
    }else if(page.haveMeetingRoomPerm){
    	page.listType = "mrAuditList";
    } else {
    	page.listType = "mrList";//默认为会议室列表
    	intListCache();
    }
    if (urlParam['action'] == "createVideoMeeting") {
    	page.cachData.openFrom = "createVideoMeeting";
	}else if(urlParam.openFrom == "meetingCreate"){
		page.cachData.openFrom = "meetingCreate";
	}else{
		page.cachData.openFrom = "meetingRoomList";
	}
    if(page.listType == "mrList") {//会议室列表
    	loadData("#mrListContainDiv",$s.Meeting.getMeetingRooms,[{},page.cachData]);
    }else if(page.listType == "mrApproveList") {//已申请列表
		loadData("#mrApprovesContainDiv",$s.Meeting.getApplyMeemtingRooms,[{},page.cachData]);
	} else{ //管理员审核列表
		page.listType = "mrAuditList";
		loadData("#mrAuditsContainDiv",$s.Meeting.getMeetingRoomAudits,[{},page.cachData]);
	}
    //查询完毕删除缓存参数
    cmp.storage["delete"](mrlistKey, true);
}

function loadData(currentList,subDataFunc,params) {
	if(page.listViewKey[currentList]){
		cmp.listView(currentList).destroyListview();
	}
	initCurrentDiv(currentList);
	initListView(currentList,subDataFunc,params);
}

function initCurrentDiv(currentList) {
	if(currentList == '#mrListContainDiv'){
		$fillArea = _$("#mrListUL");
		$fillTpl  = _$("#list_li_meetingRoom").innerHTML;
		_$("#listMR").classList.add("cmp-active");
		_$("#mrContain").classList.add("cmp-active");
		_$("#listMRApprove").classList.remove("cmp-active");
		_$("#listMRAudit").classList.remove("cmp-active");
		_$("#mrApprovesContain").classList.remove("cmp-active");
		_$("#mrAuditsContain").classList.remove("cmp-active");
	}
	else if(currentList == '#mrApprovesContainDiv'){
		$fillArea = _$("#mrApproves");
		$fillTpl  = _$("#list_li_tpl").innerHTML;
		_$("#listMR").classList.remove("cmp-active");
		_$("#mrContain").classList.remove("cmp-active");
		_$("#listMRApprove").classList.add("cmp-active");
		_$("#mrApprovesContain").classList.add("cmp-active");
		_$("#listMRAudit").classList.remove("cmp-active");
		_$("#mrAuditsContain").classList.remove("cmp-active");
	}
	else if(currentList == '#mrAuditsContainDiv'){
		$fillArea = _$("#mrAudits");
		$fillTpl  = _$("#list_li_tpl").innerHTML;
		_$("#listMR").classList.remove("cmp-active");
		_$("#mrContain").classList.remove("cmp-active");
		_$("#listMRAudit").classList.add("cmp-active");
		_$("#mrAuditsContain").classList.add("cmp-active");
		_$("#listMRApprove").classList.remove("cmp-active");
		_$("#mrApprovesContain").classList.remove("cmp-active");
		
	}
}

function initListView(currentList,subDataFunc,params) {
	_currentListDiv = currentList;
	//保存当前listView缓存键值
	var crumbsID = page.searchCondition.searchCrumbsId ? page.searchCondition.searchCrumbsId : _currentListDiv;
	page.currentListViewKey = _currentListDiv + "&" + crumbsID;
	
	page.listViewKey[currentList] = cmp.listView(currentList, {
		imgCache:true,
	    config: {
	        isClear: false,
	        clearCache: isListViewRefresh(),
	        crumbsID : crumbsID,
	        params: params,
	        dataFunc: function(p1, p2, options) {
	        	page[_currentListDiv] = {};
	        	page[_currentListDiv]['pageNo'] = p2.pageNo;
	        	
	        	if(page.loadAll){
	        		p2.pageNo = 1;
	        		p2.pageSize = page[_currentListDiv]['pageNo'] * p2.pageSize;
	        		page.loadAll = false;
	        	}
	        	subDataFunc({}, p2, options);
	        },
	        renderFunc: renderData
	    },
	    down: {
  	  		contentdown:cmp.i18n("meeting.page.action.pullDownRefresh"),//可选，在下拉可刷新状态时，下拉刷新控件上显示的标题内容
  	  		contentover: cmp.i18n("meeting.page.action.loseRefresh"),//可选，在释放可刷新状态时，下拉刷新控件上显示的标题内容
  	  		contentrefresh: cmp.i18n("meeting.page.state.refreshing")//可选，正在刷新状态时，下拉刷新控件上显示的标题内容
  	  	},
  	  	up: {
	        contentdown: cmp.i18n("meeting.page.action.loadMore"),//可选，在上拉可刷新状态时，上拉刷新控件上显示的标题内容
	        contentrefresh: cmp.i18n("meeting.page.state.loading"),//可选，正在加载状态时，上拉加载控件上显示的标题内容
	        contentnomore: cmp.i18n("meeting.page.state.noMore")//可选，请求完毕若没有更多数据时显示的提醒内容；
  	  	}
	});
}

function renderData(result, isRefresh){
	var html = cmp.tpl($fillTpl, result);
	if (isRefresh || refreshListview ||isSearch) {//是否刷新操作，刷新操作 直接覆盖数据
		$fillArea.innerHTML = html;
		refreshListview = false;
	} else {
		cmp.append($fillArea,html);
	}
}

