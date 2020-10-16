var urlParam;
var refreshListview = false;
var _currentListDiv= ""; //当前列表
var isBatch = false; // 是否处于批量操作状态
var noAllSelect = true; //是否处于全选状态
var _nowTab = "";
var meeting_list_type_cache_key= "m3_v5_meeting_list_type";
var searchModelId = 9003;
var MEETING_LIST_CACHE_KEY = "m3_v5_meeting_list";
var pageX = {
    searchCacheKey : "m3_v5_collaboration_colAffairs_search_cache_key",
    searchCondition : {},
    cache : {},
    cachePeivMenu : null,//cache已经被用残了
    loadAll : false, //是否刷新之前的所有数据
    listViewKey : {} //存放listview对象
};

cmp.ready(function () {
	urlParam = cmp.href.getParam();
	
	initPageBack();
	
	_loadCahce();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.page.lable.meeting"));
		initPageData();
		//添加缓存
	    document.addEventListener('beforepageredirect', function(e){ 
	        _storagePageData();
	    });
	},meetingBuildVersion);
});

function _storagePageData(){
    
    var toCache = {
    	cachePeivMenu : pageX.cachePeivMenu
    }
    cmp.storage.save(MEETING_LIST_CACHE_KEY, cmp.toJSON(toCache), true);
}

function _loadCahce(){
    
    var fromCache = MeetingUtils.loadCache(MEETING_LIST_CACHE_KEY, true);
    if(fromCache){
        pageX.cachePeivMenu = fromCache.cachePeivMenu;
    }
}


function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
    if(MeetingUtils.isFromM3NavBar()){
    	cmp.backbutton.push(cmp.closeM3App);
    }else{
    	cmp.backbutton.push(_goBack);
	}
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
		for(var i in pageX.listViewKey){
			pageX.listViewKey[i].refreshInitData();
		}
	}
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
//添加缓存
function intListCache() {
	//当前列表显示的缓存
	cmp.storage.save(meeting_list_type_cache_key, pageX.listType, true);
	
	//使用local storage
	cmp.storage.save(pageX.searchCacheKey, JSON.stringify(pageX.searchCondition), true);
}


function initPageData(){
	//会议菜单权限判断
	pageX.listType = "listPending";
	
	var urlListType = MeetingUtils.getHrefParam("listType");
	if(urlListType){
		pageX.urlListType = urlListType;
		pageX.listType = urlListType;
	}else{
		/**
		 * 小致语音查询适配
		 */
		var sourceType = urlParam["sourceType"];
		if(sourceType && sourceType == 79 ){
			pageX.listType = getXiaozListType(urlParam["status"]);
			pageX.searchCondition.searchCrumbsId = "search" + cmp.buildUUID();
			if(urlParam["beginDate"]){//根据会议时间查询
				pageX.searchCondition.condition = "beginDate";
				pageX.searchCondition.text = cmp.i18n("meeting.page.lable.meetingTime");
		      	pageX.searchCondition.dateBegin = new Date(urlParam["beginDate"]).format("yyyy-MM-dd") + " 00:00:00";
		      	if(urlParam["endDate"]){
		      		pageX.searchCondition.dateEnd = new Date(urlParam["endDate"]).format("yyyy-MM-dd") + " 23:59:59";
		      	}else{
		      		pageX.searchCondition.dateEnd = new Date(urlParam["beginDate"]).format("yyyy-MM-dd") + " 23:59:59";
		      	}
		      	pageX.cache.textfield = pageX.searchCondition.dateBegin;
	        	pageX.cache.textfield1 =pageX.searchCondition.dateEnd;
			}else if(urlParam["title"]){//根据会议标题查询
				pageX.searchCondition.condition = "title";
				pageX.searchCondition.text =  cmp.i18n("meeting.meetingCreate.meetingName");
		      	pageX.searchCondition.value = urlParam["title"];
		      	pageX.cache.textfield = pageX.searchCondition.value;
			}else if(urlParam["createUser"]){//根据会议创建人查询
				pageX.searchCondition.condition = "createUser";
				pageX.searchCondition.text =  cmp.i18n("meeting.meetingList.sentName");
		      	pageX.searchCondition.value = urlParam["createUser"];
		      	pageX.cache.textfield = pageX.searchCondition.value;
			}
			intListCache(); 
		}
	}
	
	if(!pageX.cachePeivMenu){
	    
	    cmp.dialog.loading();
	    $s.Meeting.meetingUserPeivMenu({}, {
	        success : function(result) {
	            
	            cmp.dialog.loading(false);
	            pageX.cachePeivMenu = result;
	            _initPervMenu(pageX.cachePeivMenu);
	            
	        },
	        error : function(result){
	            
	            cmp.dialog.loading(false);
	            
	            //处理异常
	            MeetingUtils.dealError(result);
	        }
	    });
	}else{
	    _initPervMenu(pageX.cachePeivMenu);
	}
}

/**
 * 小致列表状态转换
 */
function getXiaozListType(status){
	var listType = "listPending";
	switch (status){
		case "pending":
			listType = "listPending";
			break;
		case "done":
			listType = "listDone";
			break;
		case "sent":
			listType = "listSent";
			break;
		case "waitSent":
			listType = "listWaitSent";
			break;
		default:
			break;
	}
	return listType;
}

function _initPervMenu(result){
    if(result) {
      //会议安排权限
        pageX.haveMeetingArrangeRole = result.haveMeetingArrangeRole;
        //会议室申请权限
        pageX.haveMeetingRoomApp = result.haveMeetingRoomApp;
        //页签权限判断
        initPageTab(result);
        
        initPageList();
        
        //初始化会议点击事件
        initEvent();
    }else{
        _alert(cmp.i18n("meeting.exception.noPermissionException"));
    }
}


//页签权限判断
function initPageTab(result) {
	if(result.haveMeetingPendingRole || result.haveMeetingDoneRole || result.haveMeetingArrangeRole) {
		//下面的顺序不要修改。当前显示的页面是有优先级的。优先显示顺序已开，待开、已发、待发
	    if(result.haveMeetingArrangeRole) { 
            _$("#sentTab").style.display = "";
            _$("#waitSentTab").style.display = "";
            _$("#meetingCreateDiv").style.display = "";
            if(!pageX.urlListType){
            	pageX.listType = "listSent";
            }
        }
	    if (result.haveMeetingPendingRole) {
            _$("#pendingTab").style.display = "";
            if(!pageX.urlListType){
            	pageX.listType = "listPending";
            }
        }
	    if(result.haveMeetingDoneRole) {
            _$("#doneTab").style.display = "";
            if(!pageX.urlListType){
            	pageX.listType = "listDone";
            }
        }
	} else {
		_alert(cmp.i18n("meeting.page.msg.noPermission"),function(){
			cmp.href.back();
		});
	}
	
	//从缓存从读取当前显示的页签
	var listTypeValue = cmp.storage.get(meeting_list_type_cache_key, true);
    if(listTypeValue){
    	pageX.listType = listTypeValue;
    	cmp.storage["delete"](meeting_list_type_cache_key, true);
    }
    var searchCacheData = cmp.storage.get(pageX.searchCacheKey, true);
    if(searchCacheData){
        pageX.searchCondition = JSON.parse(searchCacheData);
        cmp.storage["delete"](pageX.searchCacheKey, true);
    }
}

function initPageList() {
	//显示当前页签
	initCurrentDiv(pageX.listType);
	//初始化会议列表及数据
	loadData(tab2DataContainer[pageX.listType]["container"],[{},pageX.cache],tab2DataContainer[pageX.listType]["fn"]);
}

function initCurrentDiv(listTypeValue) {
	if(listTypeValue == "listPending") {
		_$("#pendingTab").classList.add("cmp-active");
		_$("#doneTab").classList.remove("cmp-active");
		_$("#sentTab").classList.remove("cmp-active");
		_$("#waitSentTab").classList.remove("cmp-active");
		
		_$("#pendingDiv").classList.add("cmp-active");
		_currentListDiv = "#pendingContain";
	} else if(listTypeValue == "listDone") {
		_$("#doneTab").classList.add("cmp-active");
		_$("#pendingTab").classList.remove("cmp-active");
		_$("#sentTab").classList.remove("cmp-active");
		_$("#waitSentTab").classList.remove("cmp-active");
		
		_$("#doneDiv").classList.add("cmp-active");
		_currentListDiv = "#doneContain";
	} else if(listTypeValue == "listSent") {
		_$("#sentTab").classList.add("cmp-active");
		_$("#doneTab").classList.remove("cmp-active");
		_$("#pendingTab").classList.remove("cmp-active");
		_$("#waitSentTab").classList.remove("cmp-active");
		
		_$("#sentDiv").classList.add("cmp-active");
		_currentListDiv = "#sentContain";
	} else if(listTypeValue == "listWaitSent") {
		_$("#waitSentTab").classList.add("cmp-active");
		_$("#doneTab").classList.remove("cmp-active");
		_$("#sentTab").classList.remove("cmp-active");
		_$("#pendingTab").classList.remove("cmp-active");
		
		_$("#waitSentDiv").classList.add("cmp-active");
		_currentListDiv = "#waitSentContain";
	}
	
	//搜索条件
	if (pageX.searchCondition.condition !=undefined) {
		_$("#searchHeader").style.display = "none";
		_$("#reSearch").style.display = "block";
		_$("#dataCommonDiv").style.top = "44px";
		
		if (pageX.searchCondition.condition != "beginDate") {
	      	_$("#searchText").style.display = "block";
	      	_$("#searchDate").style.display = "none";
	      	_$("#cmp_search_title").innerHTML = pageX.searchCondition.text;
	      	_$("#searchTextValue").value = pageX.searchCondition.value;
	      	pageX.cache.textfield = pageX.searchCondition.value;
	    } else {
	      	_$("#searchText").style.display = "none";
	      	_$("#searchDate").style.display = "block";
	      	_$("#cmp_search_title").innerHTML = pageX.searchCondition.text;
	      	_$("#searchDateBeg").value = pageX.searchCondition.dateBegin;
	      	_$("#searchDateEnd").value = pageX.searchCondition.dateEnd;
	      	
	      	pageX.cache.textfield = pageX.searchCondition.dateBegin;
        	pageX.cache.textfield1 =pageX.searchCondition.dateEnd;
	    }
		pageX.cache.condition = pageX.searchCondition.condition;
	} else {
		_$("#searchHeader").style.display = "block";
		_$("#reSearch").style.display = "none";
		_$("#dataCommonDiv").style.top = "0";
	}	
}

var tab2DataContainer = {
        "listPending" : {"container":"#pendingContain","fn":function(fn1, params, options){
        	$s.Meeting.findPendingMeetings({}, params, {
	    		success : function(result) {
	    			if(options.success) {
	            		options.success(result.data);
	            	}
	            },
	            error : function(result){
	            	//处理异常
	            	MeetingUtils.dealError(result);
	            }
	        })
        }},
        "listDone" : {"container":"#doneContain","fn":function(fn1, params, options){
        	$s.Meeting.findDoneMeetings({}, params, {
	    		success : function(result) {
	    			if(options.success) {
	            		options.success(result);
	            	}
	            },
	            error : function(result){
	            	//处理异常
	            	MeetingUtils.dealError(result);
	            }
	        })
        }},
        "listSent" : {"container":"#sentContain","fn":function(fn1, params, options){
        	$s.Meeting.findSentMeetings({}, params, {
	    		success : function(result) {
	    			if(options.success) {
	            		options.success(result.data);
	            	}
	            },
	            error : function(result){
	            	//处理异常
	            	MeetingUtils.dealError(result);
	            }
	        })
        }},
        "listWaitSent" : {"container":"#waitSentContain","fn":function(fn1, params, options){
        	$s.Meeting.findWaitSentMeetings({}, params, {
	    		success : function(result) {
	    			if(options.success) {
	            		options.success(result.data);
	            	}
	            },
	            error : function(result){
	            	//处理异常
	            	MeetingUtils.dealError(result);
	            }
	        })
        }}
}

function loadData(currentListDiv,params,subDataFunc) {
	
	//保存当前listView缓存键值
	var crumbsID = pageX.searchCondition.searchCrumbsId ? pageX.searchCondition.searchCrumbsId : currentListDiv;
	pageX.currentListViewKey = currentListDiv + "&" + crumbsID;
	
	pageX.listViewKey[currentListDiv] = cmp.listView(currentListDiv, {
		imgCache:true,
	    config: {
	    	onePageMaxNum:60,
	        isClear: false,
	        clearCache: isListViewRefresh(),
	        pageSize: 20,
	        crumbsID : crumbsID,
	        params: params,
	        dataFunc: function(p1, p2, options) {
	        	pageX[_currentListDiv] = {};
	        	pageX[_currentListDiv]['pageNo'] = p2.pageNo;
	        	
	        	if(pageX.loadAll){
	        		p2.pageNo = 1;
	        		p2.pageSize = pageX[_currentListDiv]['pageNo'] * p2.pageSize;
	        		pageX.loadAll = false;
	        	}
	        	subDataFunc({}, p2, options);
	        },
	        renderFunc: renderData,
	        renderNoDataCallback:NoDataCallbackFunc
	    },
	    down: {
  	  		contentprepage:cmp.i18n("meeting.page.lable.prePage"),//上一页
  	  		contentdown:cmp.i18n("meeting.page.action.pullDownRefresh"),//可选，在下拉可刷新状态时，下拉刷新控件上显示的标题内容
  	  		contentover: cmp.i18n("meeting.page.action.loseRefresh"),//可选，在释放可刷新状态时，下拉刷新控件上显示的标题内容
  	  		contentrefresh: cmp.i18n("meeting.page.state.refreshing"),//可选，正在刷新状态时，下拉刷新控件上显示的标题内容
  	  		callback:clickNextFn
  	  	},
  	  	up: {
	      	contentnextpage:cmp.i18n("meeting.page.lable.nextPage"),//下一页
	        contentdown: cmp.i18n("meeting.page.action.loadMore"),//可选，在上拉可刷新状态时，上拉刷新控件上显示的标题内容
	        contentrefresh: cmp.i18n("meeting.page.state.loading"),//可选，正在加载状态时，上拉加载控件上显示的标题内容
	        contentnomore: cmp.i18n("meeting.page.state.noMore"),//可选，请求完毕若没有更多数据时显示的提醒内容；
	        callback:clickNextFn
  	  	}
	});
	
}

function clickNextFn(){
	__allSelectNext(true);
}

function renderData(result, isRefresh){
	var restAllselect = false;
	var $fillArea = _$("#"+pageX.listType);
	var pendingTPL = _$("#list_li_tpl").innerHTML;
	var html = cmp.tpl(pendingTPL, result);
	if (isRefresh || refreshListview) {//是否刷新操作，刷新操作 直接覆盖数据
		$fillArea.innerHTML = html;
		if(isRefresh){
        	restAllselect=true;
        }
		refreshListview = false;
	} else {
		cmp.append($fillArea,html);
	}
	
	if(isBatch){
    	__allSelectNext(restAllselect);
    }
}

function __allSelectNext(clickNext){
	noAllSelect = true;
	if(clickNext){
		_$(_currentListDiv+" .count").innerText = _$(_currentListDiv+" .img_click",true).length;
		_$(_currentListDiv+" .lableBtn span").classList.remove("see-icon-v5-common-select-fill-color");
		_$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect");
	}
	var allDatas = _$(_currentListDiv+" .img_click",true);
	for(var i=0;i<allDatas.length;i++){
		allDatas[i].style.display="";
	}
}

function initEvent(){
    //跳转到待开列表
    _$("#pendingTab").addEventListener("tap",fnGoPendingList);
    //跳转到已开列表
    _$("#doneTab").addEventListener("tap",fnGoDoneList);
    //跳转到已发列表
    _$("#sentTab").addEventListener("tap",fnGoSentList);
    //跳转到待发列表
    _$("#waitSentTab").addEventListener("tap",fnGoWaitSentList);
	
    //快捷键
    _$(".close").addEventListener("tap", fnClose);
      
    /****************查询绑定****************/  
    _$('#search').addEventListener("tap",function(){
  		searchFn(searchModelId, null);
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
    	searchFn(searchModelId, params);
    });
    //取消重新加载页面
    _$("#cancelSearch").addEventListener("tap",function(){
    	//重置搜索条件
    	var condition = pageX.searchCondition.condition;
    	pageX.cache = {};
    	pageX.searchCondition = {};
    	
		_$("#listPending").classList.remove("loaded");
		_$("#listSent").classList.remove("loaded");
		_$("#listDone").classList.remove("loaded");
		_$("#listWaitSent").classList.remove("loaded");
		_$("#dataCommonDiv").style.top = "0";
		
		//点击取消时，重置listview的crumbsId
		if(pageX.searchCondition.searchCrumbsId){
			delete pageX.searchCondition.searchCrumbsId;
		}
		
		initPageList();
    });
    /****************查询绑定****************/  
    
    //快捷入口，跳转会议室列表
    _$("#meetingApply").addEventListener("tap", function(){
    	var param1 = {
			action : "applyMeetingRoom"
		};
    	cmp.event.trigger("beforepageredirect", document);
    	
		var option = {};
		if(MeetingUtils.isFromM3NavBar()){
			option.openWebViewCatch = true;
			param1.isFromM3NavBar = "true";
		}
		
		cmp.href.next(_meetingPath + "/html/meetingRoomList.html"+meetingBuildVersion, param1, option);
    });
    
    //快捷入口，跳转会议新建页面
    _$("#meetingCreate").addEventListener("tap", function(){
    	var param2 = {
    		"haveMeetingRoomApp" : pageX.haveMeetingRoomApp	
    	};
    	cmp.event.trigger("beforepageredirect", document);

		var option = {};
		if(MeetingUtils.isFromM3NavBar()){
			option.openWebViewCatch = true;
		}
		
		cmp.href.next(_meetingPath + "/html/meetingCreate.html"+meetingBuildVersion, param2, option);
    });
    
    //点击展开详细页面
	cmp("#dataCommonDiv").on("tap", ".right_wrap", function() {
		if(isBatch){//处于全选操作时，点击不能进入详情页面
			selectedBatchDataFn(this.parentElement.getElementsByClassName("img_click")[0]);
		}else{
			var paramData = {
					"openFrom" : getOpenFrom(),
					"meetingId" : this.getAttribute("id"),
					"affairId" : this.getAttribute("affairId"),
					"proxyId" : this.getAttribute("proxyId"),
					"haveMeetingRoomApp" : pageX.haveMeetingRoomApp
			}
			
			var option = {
				pushInDetailPad : true
			};
			
			//存人缓存
			intListCache();
			if("waitSent" == getOpenFrom()){
				if(this.getAttribute("category") == "1"){
					cmp.notification.alert(cmp.i18n("meeting.meetingDetail.category"), null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
					return;
				}
				cmp.event.trigger("beforepageredirect", document);
				cmp.href.next(_meetingPath + "/html/meetingModify.html"+meetingBuildVersion, paramData, option);
			}else{
			    cmp.event.trigger("beforepageredirect", document);
				cmp.href.next(_meetingPath + "/html/meetingDetail.html"+meetingBuildVersion, paramData, option);
			}
		}
	});
	
	/*************************批量操作事件绑定*********************************/
	_$("#dataCommonDiv").addEventListener("tap", function(e){
		e.stopPropagation();//阻止冒泡
	
		var target = e.target;
		if(target.classList.contains("img_click")){//选择与取消选择
			selectedBatchDataFn(target);
		
		}else if(target.classList.contains("all_click")){//全选
			_AllSelectFn();
		
		}else if(target.classList.contains("cancel_click")){//退出批量操作状态
			_cancelBatchFn();
		
		}else if(target.classList.contains("batch_click")){//批量操作事件
		
			batchEventFn();
		}
	});
	_$(".meeting_remove").addEventListener("tap",removeMeeting);	   
	/*************************批量操作事件绑定*********************************/
}

function getOpenFrom() {
	var openFrom = "pending";
	if(pageX.listType == "listPending") {
		openFrom = "pending";
	} else if(pageX.listType == "listDone") {
		openFrom = "dones";
	} else if(pageX.listType == "listSent") {
		openFrom = "sent";
	} else if(pageX.listType == "listWaitSent") {
		openFrom = "waitSent";
	}
	return openFrom;
}

function fnGoPendingList() {
	tabGoExitBatch(function(){
		_currentListDiv = "#pendingContain";
		newTabContain(function(){
			if(pageX.listType != "listPending"){
				pageX.listType = "listPending";
				loadData(tab2DataContainer[pageX.listType]["container"],[{},pageX.cache],tab2DataContainer[pageX.listType]["fn"]);				
			}
		});
	});
}

function fnGoDoneList() {
	tabGoExitBatch(function(){
		_currentListDiv = "#doneContain";
		newTabContain(function(){
			if(pageX.listType != "listDone"){
				pageX.listType = "listDone";
				loadData(tab2DataContainer[pageX.listType]["container"],[{},pageX.cache],tab2DataContainer[pageX.listType]["fn"]);
			}
		});
	});
}

function fnGoSentList() {
	tabGoExitBatch(function(){
		_currentListDiv = "#sentContain";
		newTabContain(function(){
			if(pageX.listType != "listSent"){
				pageX.listType = "listSent";
				loadData(tab2DataContainer[pageX.listType]["container"],[{},pageX.cache],tab2DataContainer[pageX.listType]["fn"]);
			}
		});
	});
}

function fnGoWaitSentList() {
	tabGoExitBatch(function(){
		_currentListDiv = "#waitSentContain";
		newTabContain(function(){
			if(pageX.listType != "listWaitSent"){
				pageX.listType = "listWaitSent";
				loadData(tab2DataContainer[pageX.listType]["container"],[{},pageX.cache],tab2DataContainer[pageX.listType]["fn"]);
			}
		});
	});
}

/***************************************批量操作开始**********************************/
//批量操作事件
function batchEventFn(){
	
  containHeight = _$("#dataCommonDiv").offsetHeight;
  _$(".meeting_remove").style.display="";
  cmp.listView(_currentListDiv).updateAdditionalParts(true);//更新额外部件
  cmp.listView(_currentListDiv).refreshHeight(containHeight-50);
  //是否处于批量操作状态
  isBatch = true;
  var allSelects = _$(_currentListDiv+" .img_click",true);
  if(allSelects.length){
	  for(var i=0;i<allSelects.length;i++){
		  allSelects[i].style.display="";
	  }
  }else{
	  allSelects.style.display="";
  }
//引导
  var isHit = cmp.storage.get("m3_v5_meeting_summary_batch_tip");
  if(!isHit || isHit != "true"){
      var hitEle;
      hitEle = _$("#src_form_view_hint");
      _$("#src_form_view_hint_txt", false, hitEle).innerHTML = cmp.i18n("meeting.page.lable.batchTip")
      hitEle.classList.remove("display_none");
      cmp.storage.save("m3_v5_meeting_summary_batch_tip","true");
      hitEle.querySelector(".btn")
            .addEventListener("tap", function(){
                hitEle.classList.add("display_none");
            });
  }
}

//退出批量操作
function _cancelBatchFn(){
	var allSelects = _$(_currentListDiv+" .img_click",true);
	for(var i = 0;i<allSelects.length;i++){
		allSelects[i].style.display="none";
		batchSelectState(allSelects[i],false);
	}
	cmp.listView(_currentListDiv).refreshHeight(containHeight);
	_$(".meeting_remove").style.display="none";
    cmp.listView(_currentListDiv).updateAdditionalParts(false);
    _$(_currentListDiv+" .lableBtn span").classList.remove("see-icon-v5-common-select-fill-color");
    _$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect");
    _$(_currentListDiv+" .count").innerHTML = 0;
    isBatch = false;
    noAllSelect = true;
    _$("#src_form_view_hint").classList.add("display_none");
}

//批量操作选中和取消选中状态
function batchSelectState(_target,selected){
	if(selected){
		_target.classList.remove("unselected");
		_target.classList.add("selected");
		_target.classList.add("see-icon-v5-common-select-fill-color");
	}else{
		_target.classList.add("unselected");
		_target.classList.remove("selected");
		_target.classList.remove("see-icon-v5-common-select-fill-color");
	}
}

//选择与取消选择
function selectedBatchDataFn(target){
	var _count = _$(_currentListDiv+" .count").innerText*1;
	if(target.classList.contains("see-icon-v5-common-select-fill-color")){
		batchSelectState(target,false);
		if(!noAllSelect){
			_count = _count-1;
			_$(_currentListDiv+" .count").innerHTML= _count;
			_$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect")+"("+_count+")";
		}
	}else{
		if(!noAllSelect){
			_count = _count+1;
			_$(_currentListDiv+" .count").innerHTML= _count;
			_$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect")+"("+_count+")";
		}
		batchSelectState(target,true);
	}
}

//全选
function _AllSelectFn(){
	
	var allSelects = _$(_currentListDiv+" .img_click",true);
	var maxSize = allSelects.length;
	
	if(allSelects.length>100){
		maxSize = 100;
	}
	
	if(noAllSelect){
		for(var i = 0;i<maxSize;i++){
			batchSelectState(allSelects[i],true);
		}
		
		_$(_currentListDiv+" .lableBtn span").classList.add("see-icon-v5-common-select-fill-color");
		_$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect")+"("+maxSize+")";
		noAllSelect = false;
		_$(_currentListDiv+" .count").innerHTML = maxSize;
	}else{//取消全选
		
		for(var i = 0;i<maxSize;i++){
			affairIds = "";
			batchSelectState(allSelects[i],false);
		}
		
		_$(_currentListDiv+" .lableBtn span").classList.remove("see-icon-v5-common-select-fill-color");
		_$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect");
		noAllSelect = true;
		_$(_currentListDiv+" .count").innerHTML = 0;
	}
}

function newTabContain(callback){
	if("#pendingContain"==_currentListDiv){
		_$("#pendingDiv").classList.add("cmp-active");
		_$("#doneDiv").classList.remove("cmp-active");
		_$("#sentDiv").classList.remove("cmp-active");
		_$("#waitSentDiv").classList.remove("cmp-active");
		
		_$("#pendingTab").classList.add("cmp-active");
		_$("#doneTab").classList.remove("cmp-active");
		_$("#sentTab").classList.remove("cmp-active");
		_$("#waitSentTab").classList.remove("cmp-active");
		_nowTab = "listPending";
	}else if("#doneContain"==_currentListDiv){
		_$("#pendingDiv").classList.remove("cmp-active");
		_$("#doneDiv").classList.add("cmp-active");
		_$("#sentDiv").classList.remove("cmp-active");
		_$("#waitSentDiv").classList.remove("cmp-active");
		
		_$("#pendingTab").classList.remove("cmp-active");
		_$("#doneTab").classList.add("cmp-active");
		_$("#sentTab").classList.remove("cmp-active");
		_$("#waitSentTab").classList.remove("cmp-active");
		_nowTab = "listDone";
	}else if("#sentContain"==_currentListDiv){
		_$("#pendingDiv").classList.remove("cmp-active");
		_$("#doneDiv").classList.remove("cmp-active");
		_$("#sentDiv").classList.add("cmp-active");
		_$("#waitSentDiv").classList.remove("cmp-active");
		
		_$("#pendingTab").classList.remove("cmp-active");
		_$("#doneTab").classList.remove("cmp-active");
		_$("#sentTab").classList.add("cmp-active");
		_$("#waitSentTab").classList.remove("cmp-active");
		_nowTab = "listSent";
	}else if("#waitSentContain"==_currentListDiv){
		_$("#pendingDiv").classList.remove("cmp-active");
		_$("#doneDiv").classList.remove("cmp-active");
		_$("#sentDiv").classList.remove("cmp-active");
		_$("#waitSentDiv").classList.add("cmp-active");
		
		_$("#pendingTab").classList.remove("cmp-active");
		_$("#doneTab").classList.remove("cmp-active");
		_$("#sentTab").classList.remove("cmp-active");
		_$("#waitSentTab").classList.add("cmp-active");
		_nowTab = "listWaitSent";
	}
	callback();
}


function tabGoExitBatch(exitNowList){
	if(isBatch){
		newTabContain(function(){
			cmp.notification.confirm(cmp.i18n("meeting.page.alert.exitBatch"),function(e){ 
				if(e==1){ //是
					_cancelBatchFn();
					exitNowList();
				}else{
					initCurrentDiv(_nowTab);
					return;  
				}
			},null, [ cmp.i18n("meeting.page.action.cancle"), cmp.i18n("meeting.page.dialog.OK")]);
		});
	}else{
		exitNowList();
	}
}

//获取选择的数据
function removeMeeting(){
	var meetingIds = "";
	var allSelects = _$(_currentListDiv+" .img_click",true);
	var num = 0;
	for(var i=0;i<allSelects.length;i++){
		if(allSelects[i].classList.contains("selected")){
			var state = allSelects[i].previousElementSibling.getAttribute("state");
			if(state==10 || state==20) {//未召开||召开中
				_alert(cmp.i18n("meeting.meetinglist.cancel.alert"));//会议还未结束，不能进行删除操作！
				return;
			}
			if(meetingIds != "") {
				meetingIds += ",";
			}
			meetingIds += allSelects[i].previousElementSibling.getAttribute("meetingId");
			num++;
		}
	}
	if(num == 0) {
		_alert(cmp.i18n("meeting.select.remove.alert"));//请选择删除数据
		return;
	}else{
		cmp.notification.confirm(cmp.i18n("meeting.remove.data.alert"),function(e){ //e==1是/e==0 否
	        if(e==1){ //是
	        	var listType = "";
	    		if("#waitSentContain"==_currentListDiv){
	    			listType = "listWaitSendMeeting";
	    		}else if("#doneContain"==_currentListDiv){
	    			listType = "listDoneMeeting";
	    		}else if("#sentContain"==_currentListDiv){
	    			listType = "listSendMeeting";
	    		}
	    		var param = {"id":meetingIds,"listType":listType}
	    		$s.Meeting.removeMeeting({}, param,{
	    			success:function(ret){
	    				//退出批量操作
	    				_cancelBatchFn();
	    				//删除成功以后刷新列表
						cmp.listView(_currentListDiv).refreshInitData();
						//触发多webview事件
						MeetingUtils.fireAllWebviewEvent({
							type : "delete",
							data : meetingIds
						});
	    			},
	    			error : function(result){
	    	        	//处理异常
	    	        	MeetingUtils.dealError(result);
	    			}
	    		});
	        }else{
	        	return;  
	        }
	    },null, [ cmp.i18n("meeting.page.action.cancle"), cmp.i18n("meeting.page.dialog.OK")]);
		
	}
	
}

//没有数据的时候，平台已做了退出批量操作动作
function NoDataCallbackFunc() {
    if (isBatch) {
        cmp.listView(_currentListDiv).refreshHeight(containHeight);
        _$(".meeting_remove").style.display="none";
        _$(_currentListDiv+" .lableBtn span").classList.remove("see-icon-v5-common-select-fill-color");
        _$(_currentListDiv+" .lab_all_select").innerHTML = cmp.i18n("meeting.page.lable.button.allSelect");
        _$(_currentListDiv+" .count").innerHTML = 0;
        isBatch = false;
        noAllSelect = true;
        _$("#src_form_view_hint").classList.add("display_none");
    }
}

/***************************************批量操作结束**********************************/


function searchFn(modelId,params) {
	//待开、已开：会议名称、发起人、会议时间 ；已发、待发：标题、会议时间
	var searchObj = [{type:"text",condition:"title",text:cmp.i18n("meeting.meetingCreate.meetingName")},
	                 {type:"text",condition:"createUser",text:cmp.i18n("meeting.meetingList.sentName")},
	                 {type:"date",condition:"beginDate",text:cmp.i18n("meeting.page.lable.meetingTime")}];
	var listState = getOpenFrom(); 
	if (listState == "sent" || listState =="waitSent") {
		modelId = "9001";
		searchObj =  [{type:"text",condition:"title",text:cmp.i18n("meeting.meetingCreate.meetingName")},
		              {type:"date",condition:"beginDate",text:cmp.i18n("meeting.page.lable.meetingTime")}];
	}
	//当前查询的集合不是页面上传递的集合的时候
	if (params != null && params.modelId != modelId) {
		//只有1:标题、2:已发、待办、已办的时间 可公用 查询。其他不要传递到查询页面。
		if (params.condition == "createUser" && modelId == "9001") {
			params = null;
		}
	}
	cmp.search.init({
    	id:"#search",
        model:{                    //定义该搜索组件用于的模块及使用者的唯一标识（如：该操作人员的登录id）搜索结果会返回给开发者
            name:"meetingList",   //模块名，如："协同"，名称开发者自定义
            id:modelId           //模块的唯一标识：
		},
		dateOptions:{"type": "dateCalender"},
        parameter:params,
        TimeNow:false,
        items : searchObj,
        callback:function(result){ //回调函数：会将输入的搜索条件和结果返回给开发者
        	if (pageX.searchCondition.condition != undefined) {
        		pageX.cache = {};
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
            	pageX.cache.textfield = result.searchKey[0];
            	pageX.cache.textfield1 =result.searchKey[1];
            	pageX.searchCondition.dateBegin = result.searchKey[0];
            	pageX.searchCondition.dateEnd = result.searchKey[1];
            	
            } else {
            	pageX.cache.textfield = result.searchKey[0];
            	dataSoure = result.searchKey[0];
            }
            pageX.cache.condition = condition;
            
            //查询条件返回
            pageX.searchCondition.type = type;
            pageX.searchCondition.condition = condition;
            pageX.searchCondition.text = data.text;
            pageX.searchCondition.value = dataSoure;
            pageX.searchCondition.modelId = modelId;
            
            isSearch = true;
            
            //查询时listview要加上crumbsId参数
            pageX.searchCondition.searchCrumbsId = "search" + cmp.buildUUID();
            
            initPageList();
        }
    });
}


function fnClose(){
    _$("#maskContainer").style.display = "none";
    //_$("header").classList.remove("blur");
    _$(".cmp-content").classList.remove("blur");
    _$("footer").classList.remove("blur");
}
