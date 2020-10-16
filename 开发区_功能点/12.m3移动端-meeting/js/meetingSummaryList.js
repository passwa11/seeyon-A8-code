var urlParam = {};
var isSearch = false; //是否查询，用于控制是否覆盖列表数据
var nextStartDate, nextEndDate; //传入到下个页面的开始、结束时间
var cacheKey_mtSummaryStorgeDatas = "m3_v5_meeting_summary_queryParams"; //会议纪要列表查询条件
var page = {};
page.searchCacheKey = "m3_v5_meeting_summary_search_cache_key";
page.searchCondition = {};
page.cachData = {};
var summaryListView;

/**
 * 接收参数描述
 * action      执行动作
 * meetingSummaryList   会议纪要列表查询
 */
cmp.ready(function () {
	urlParam = cmp.href.getParam() || {};
	if(urlParam.isFromM3NavBar && urlParam.isFromM3NavBar == "true"){
		cmp.storage.save("isFromM3NavBar", true, true);
	}
	initPageBack();
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.meetingSummaryList.list"));
		initPageData();
		initEvent();
	},meetingBuildVersion);
});

function initPageBack() {
    cmp.backbutton();
	cmp.backbutton.push(_goBack);
	//注册webview事件
	MeetingUtils.addMeetingWebviewEvent(webviewEvent);
	//横竖屏切换事件
	cmp.orientation.onOrientationChange(function(){
		var listContainer = document.querySelector("#dataCommonDiv .cmp-control-content");
		listContainer.style.height = (window.innerHeight) - 50 + 'px';
		summaryListView.refresh();
	});
}

/**
 * 多webview事件
 * @param {*} data 
 */
var webviewEvent = function(parameters){
	var data = parameters.data;
	if(data && data.isRefresh){
		summaryListView && summaryListView.refreshInitData();
	}
}

function _goBack() {
	cmp.webViewListener.fire({ 
    	type: 'meetingSummary.ListRefresh', 
    	data: {
    		closePage : "true"
    	}
	})
	cmp.href.back();
}

function initPageData(){
	
	var searchCacheData = cmp.storage.get(page.searchCacheKey, true);
    if(searchCacheData){
        page.searchCondition = JSON.parse(searchCacheData);
        cmp.storage["delete"](page.searchCacheKey, true);
    }

    var queryParams = cmp.storage.get(cacheKey_mtSummaryStorgeDatas, true);
    if(queryParams){
		queryParams = cmp.parseJSON(queryParams);
		var condition = queryParams.queryType;  //传递过来的参数搜索条件
		page.searchCondition.condition = condition;
		if("meetingName" == condition){
   		 page.searchCondition.value = queryParams.meetingName;
   		 page.searchCondition.type = "text";
   		 page.searchCondition.text = cmp.i18n("meeting.meetingSummaryList.queryName");
	   	}
		if("time" == condition){
	   		page.searchCondition.text = cmp.i18n("meeting.meetingSummaryList.queryTime");
	   		page.searchCondition.dateBegin = queryParams.startDate;
	   		page.searchCondition.dateEnd = queryParams.endDate;
	   		page.searchCondition.type = "date";
	   	}
		//清空查询条件缓存
		cmp.storage["delete"](cacheKey_mtSummaryStorgeDatas, true);
    }
    initPageList();
}

function initPageList() {
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
	   		var l_startTime = new Date(nextStartDate.replace(/\-/g, '/')).getTime();
	   		var l_endTime = new Date(nextEndDate.replace(/\-/g, '/')).getTime();
	   		if(l_startTime > l_endTime){
	   			return;
	   		}
	    }
		page.cachData.condition = page.searchCondition.condition;
	} else {
		_$("#searchHeader").style.display = "block";
		_$("#reSearch").style.display = "none";
	}
	getMeetingSummarys([{},page.cachData]);
}

function initEvent(){
	/**
	 * 查询绑定
	 * */  
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

	/**
	 * 取消查询
	 */
	_$("#cancelSearch").addEventListener("tap",function(){
		page.cachData = {};
		page.searchCondition = {};
		isSearch = true;
		initPageList();
	});
	
	/**
	 * 数据列表点击事件
	 */
	cmp("#meetingSummaryList").on("tap", ".detail_wrap", function(e) {
		e.stopPropagation();
		var option = {
			pushInDetailPad : true
		};
		var action = urlParam.action;
		var paramData = {
				"meeting":{"recordId" : this.getAttribute("summaryId")},
				openFrom : "meetingSummaryList"
		};
		cmp.href.next(_meetingPath + "/html/meetingSummary.html"+meetingBuildVersion, paramData,option);
	});
}

/**
 * 添加缓存
 */
function intListCache() {
	cmp.storage.save(page.searchCacheKey, JSON.stringify(page.searchCondition), true);//保存查询条件的缓存
}

/**
 * 后台数据加载
 */
function getMeetingSummarys(params){
	summaryListView = cmp.listView("#pullrefresh",{
		imgCache:true,
		config: {
		    isClear: false,
		    clearCache: isListViewRefresh(),
	        params: params,
	        dataFunc: function(fn1, params, options){
	        	$s.Meeting.getMeetingSummarys({}, params, {
		    		success : function(result) {
		    			if(options.success) {
		            		options.success(result);
		            	}
		            },
		            error : function(result){
		            	options.error();
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
/**
 * 数据渲染到页面
 */
function renderData(result, isRefresh){
    var pendingTPL = _$("#list_li_meetingSummary").innerHTML;
    var html = cmp.tpl(pendingTPL, result);
    /*是否刷新操作，刷新操作 直接覆盖数据*/
    if (isRefresh || isSearch) {
        _$("#meetingSummaryList").innerHTML = html;
        isSearch = false;
    } else {
    	var table = _$("#meetingSummaryList").innerHTML;
    	_$("#meetingSummaryList").innerHTML = table + html;
    }
}

/**
 * 搜索组件初始化
 */
function searchFn(params){
	if(params == null) {
		params = {type : "date",condition : "time",text : cmp.i18n("meeting.meetingSummaryList.queryTime"),value : [getInitDate(), ""]};
	}
	var searchObj = [{type:"date",condition:"time",text:cmp.i18n("meeting.meetingSummaryList.queryTime")},
	                 {type:"text",condition:"meetingName",text:cmp.i18n("meeting.meetingSummaryList.queryName")}];
	
	cmp.search.init({
    	id:"#search",
        model : {                    //定义该搜索组件用于的模块及使用者的唯一标识（如：该操作人员的登录id）搜索结果会返回给开发者
            name:"meetingSummaryList",   //模块名，如："协同"，名称开发者自定义
            id:"9011"           //模块的唯一标识：
        },
        parameter : params,
        TimeQueryControl : false, //两个时间框只选择一个时间后是否立即执行查询
        dateOptions : {
        	type : "",
        	MinutesScale : "5"
        }, // 5分钟刻度类型的日期组件
        items : searchObj,
        TimeMin : true,//false：第一个时间小于当前时间，不进行查询
        TimeAlert:true,//控制历史记录搜索弹出框只显示一个确定按钮
        callback : searchCallback //回调函数：会将输入的搜索条件和结果返回给开发者
    });
}
/**
 * 查询回调函数
 */
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
	isSearch = true;
	initPageList();
}
/**
 * 获取初始化的时间  开始时间是当前时间后接近的半点或整点
 */
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
/**
 * 格式化时间，yyyy-MM-dd hh:mm 格式
 */
function formatDate(time){
	var year = time.getFullYear(),
		month = time.getMonth() < 9 ? "0" + (time.getMonth() + 1) : time.getMonth() + 1,
		date = time.getDate() < 10 ? "0" + time.getDate() : time.getDate(),
		hours = time.getHours() == 0 ? "00" : time.getHours() < 10 ? "0" + time.getHours() : time.getHours(),
		minutes = time.getMinutes() == 0 ? "00" : time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes();
	return year + "-" + month + "-" + date + " " + hours + ":" + minutes;
}
