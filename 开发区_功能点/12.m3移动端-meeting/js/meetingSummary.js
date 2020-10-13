var urlParam;
var page = {};

var bodyHeight = 0;//正文区域
var isComentShow = false;
var showAtt = true; //展开或收起附件列表
var _storge_key = document.location.href;
var pageListView;
/********************************** 缓存操作  ***********************************/

function initInStorage() {
    var cacheData = cmp.storage.get(_storge_key, true);
    if(cacheData) {
        cmp.storage["delete"](_storge_key, true);
        page = JSON.parse(cacheData);
    }
}

function _M3_Save_Storage() {
	page.isLoadStorage = true;
	cmp.storage.save(_storge_key, JSON.stringify(page), true);
}
/********************************** 初发化方法  ***********************************/

cmp.ready(function () {
	
	urlParam = cmp.href.getParam();
	
	initPageBack();
	
	//注册懒加载
    _registLazy();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources",function(){
		setPageTitle(cmp.i18n("meeting.page.lable.summary"));
		//缓存
		initInStorage();
		initPageData();
		
		//启动懒加载, 性能要求
        setTimeout(function(){
            LazyUtil.startLazy();
        }, 0);
		
		initEvent();
	},meetingBuildVersion);
});

/****************************** 监听返回事件(放到最前头)  ******************************/

function initPageBack() {
    //cmp控制返回
    cmp.backbutton();
	cmp.backbutton.push(_goBack);
	//横竖屏切换事件
	cmp.orientation.onOrientationChange(function(){
		var listContainer = document.querySelector(".cmp-control-content");
		listContainer.style.height = window.innerHeight + 'px';
		pageListView.refresh();
	});
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

/********************************** 页面数据初始化  ***********************************/

function initPageData() {
	//因为此处不需要发请求数据，接收传输过来的数据
	page = urlParam;
	if(MeetingUtils.getBackURL() == "weixin"){
		if(!page.meeting){
			page.meeting = {};
		}
		page.meeting.recordId = urlParam["meetingSummaryId"];
	}
	if(!page.summary){
		$s.Meeting.getMeetingSummary(page.meeting.recordId,null, {
			success : function(result) {
				//异常消息提示
				if(result && result.data && result.data.errorMsg){
					_alert(result.data.errorMsg,function(){
						_goBack();
					});
					return;
				}
				
				
//				//原生头部增加打印按钮
//	            cmp.header.insertRightBtn({
//	                //标准可以不声明type
//	                index: 0,
//	                items: [{
//	                        type: 'image',
//	                        imageUrl: cmp.config.printImg
//	                }],
//	                callback: function(index) {
//	                	if(index == 0){
//	                		cmp.dialog.loading("");
//	                		$s.Meeting.screenSlot({},{"meetingId":page.meeting.id,"oper":"onlySummary"},{
//	                			success:function(rs){
//	                				cmp.dialog.loading(false);
//	                				var path = cmp.origin + "/rest/attachment/file/" + rs.data.id + "?createDate="+rs.data.extraMap.createdatestr+"&fileName=" + encodeURI(rs.data.filename);
//	                    			console.log(path);
//	                    			cmp.api.print({
//	                    				path : path,
//	                    				fileType: 'pdf',
//	                        	        lastModify: new Date().getTime(),
//	                        	        success: function() {
//	                        	        	console.log('打印唤起成功');
//	                        	        },
//	                        	        error: function() {
//	                        	        	console.log('打印唤起失败');
//	                        	        }
//	                        		});
//	                			}
//	                		});
//	                	}
//	                }
//	            });
				
				
				page.summary =  result.data;
				page.memberNumber = result.memberNumber;
				page.actualUserVo = result.actualUserVo;
				//会议任务
				page.meetingTaskNum = result.meetingTaskNum;
				//消息链接查看纪要
				if (page.openFrom == "message" || page.openFrom == "meetingSummaryList") {
				    page.meeting = result.meeting;
				    page.meetingDate = page.meeting.beginDateFormat + " - " + page.meeting.endDateFormat;
				}
				//正文是否支持office转换
				page.allowTrans = result.allowTrans;
				initHtml();
				
				//初始化打印按钮
				initPrint();
			},
			error : function(result){
				//处理异常
	        	MeetingUtils.dealError(result);
			}
		});
				
	}else{
		initHtml();
	}
}


function initPrint(){
	
	cmp.api.isSupportPrint({
	    success: function(canPrint) {
	        if(canPrint){
	        	if(!cmp.os.ios && !(/html|pdf/i.test(page.summary.bodyType))){
	        		//Android下只能打html和pdf
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
	                    		if(!(/html/i.test(page.summary.bodyType))){
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
	$s.Meeting.screenSlot({},{"meetingId":page.meeting.id,"oper":"onlySummary"},{
		success:function(rs){
			cmp.dialog.loading(false);
			if(rs && rs.data && rs.data.id){
				var path = cmp.origin + "/rest/attachment/file/" + rs.data.id + "?createDate="+rs.data.extraMap.createdatestr+"&fileName=" + encodeURI(rs.data.filename);
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
			}else{
				cmp.notification.toast("截图文件获取失败,无法打印!");
			}
		},error: function (error) {
        	cmp.dialog.loading(false);
        }
	});//end of $s.Meeting.screenSlot
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
                   _cmpPath + "/css/cmp-audio.css" + $verstion
                   ],
          "js" : [
                  _cmpPath + "/js/cmp-audio.js" + $verstion,
                  _cmpPath + "/js/cmp-push.js" + $verstion,
                  _cmpPath + "/js/cmp-app.js" + $verstion
                  ]
      });
      
  }

/********************************** 页面布局  ***********************************/

/**
 * 从缓存加载到页面
 */
function initHtml() {

    //初始化会议纪要title
	_$("#summaryTitle").innerHTML = page.meeting.titleHtml;
	//纪要展示
	//_initPageLayout();
	
	LazyUtil.addLoadedFn("lazy_content", function(){
	        var contentConfig = {
	            "target" : "listView",
	            "bodyType" : SeeyonContent.getBodyCode(page.summary.bodyType),
	            "padding" : "0",
	            "content" : page.summary.content,
	            "lastModified" : page.summary.lastModified,
	            "moduleType" : "6",
	            "rightId" : "",
	            "viewState" : "",
	            "momentum" : true,
	            "onload" : null,
	            "allowTrans" : page.allowTrans,
	            "onScrollBottom" : function(){
	               //_toggleContent(true);//展示意见区域
	            },
                "ext" : {
                    reference : page.summary.id
                }
	        };
	        SeeyonContent.init(contentConfig);
			setTimeout(function () {
				dealAttContentIframeHeight();
			},350);
    });
    
	//会议纪要创建者
	_$("#meetingCreater").innerHTML = page.summary.createUserName;
	//会议纪要发起的日期和时间
	_$("#mtCreateDate").innerHTML = page.summary.createDateFormat.split(" ")[0];
	
	_$("#mtCreateTime").innerHTML = page.summary.createDateFormat.split(" ")[1];
	//会议时间
	_$("#mtTime").innerHTML = page.meetingDate;
	//会议实际与会人员 
	_$("#actualMName").innerHTML = page.summary.actualShowName;
	//会议地点 
	_$("#mtPlace").innerHTML = typeof(page.meeting.meetPlace) == "undefined" ? "" : page.meeting.meetPlace;
	//主持人
	_$("#emcee").innerHTML = page.meeting.emceeName;
	//记录人
	_$("#recordUser").innerHTML = page.summary.createUserName;
	
	
	if(page.summary.summaryAttmentList != null && page.summary.summaryAttmentList.length > 0){
		
			
		//初始化会议纪要附件消息头
		var attTitleTpl = _$("#attTitleTpl").innerHTML;
		var meetingContentHtml = cmp.tpl(attTitleTpl,page);
		_$("#attTitleDiv").innerHTML = meetingContentHtml;
		//初始化会议附件列表
		var loadParam = {
				selector : "#attListUl",
				atts : page.summary.summaryAttmentList
		}
		new SeeyonAttachment({loadParam : loadParam});
	}else{
		document.getElementById("attTitleDiv").style.display = 'none';
	}
    //国际化title标签 
	_$("title").innerText=cmp.i18n("meeting.page.lable.meetingSummary");
	 //初始化会议任务按钮显示
	initTaskBtnView();
	//页面整体初始化listview
	window.addEventListener('resize', function () {
		dealAttContentIframeHeight();
	},false);
	cmp.orientation.onOrientationChange(function () {
		dealAttContentIframeHeight();
	});
	pageListView = cmp.listView("#scroller");
	
}
function getElementTop (ele, top) {
	var newTop =  ele.offsetTop + top;
	if (ele.parentElement) {
		newTop += getElementTop(ele.parentElement, 0);
	}
	return newTop;
};
// ios 下 pdf问题
function dealAttContentIframeHeight() {
	var attContentIframe = document.getElementById('attContentIframe');
	if (attContentIframe) {
		var top = getElementTop(attContentIframe, 0);
		var meettingList = document.querySelector('#scroller .meeting-list');
		var attachmentWrap = document.querySelector('#scroller .attachment-wrap');
		var meetHeight = 0;
		var attHeight = 0;
		if (meettingList) {
			meetHeight = meettingList.offsetHeight + 8;
		}
		if (attachmentWrap) {
			attHeight = attachmentWrap.offsetHeight + 10;
		}
		var height = (window.innerHeight - top - meetHeight - attHeight -6);
		var parent = attContentIframe.parentElement;
		parent.style.maxHeight = height + 30 + 'px';
		parent.style.display = 'none';
		SeeyonContent.refresh('listView');
		setTimeout(function () {
			parent.style.display = '';
		}, 0)
	}
};
/**
 * 初始化会议任务按钮显示
 */
function initTaskBtnView(){
	//任务
	if(page.meetingTaskNum && page.meetingTaskNum  != "0"){
		_$("#meetingTask").style.display = "";
		_$("#meetingTaskNum").innerHTML = page.meetingTaskNum;
	}
}
/********************************** 初始化控件事件  ***********************************/

function initEvent() {
	_$("#goUserListBtn").addEventListener("tap", function(e){
    	gotoMeetingUserListHtml();
    });
	
	_$("#attTitleDiv").addEventListener("tap", function(e){
    	if(showAtt){
    		_$("#attIcon").classList.remove("cmp-icon-arrowup");
    		_$("#attIcon").classList.add("cmp-icon-arrowdown");
    		_$("#attListUl").style.display = "none";
    		showAtt = false;
    	}else{
    		_$("#attIcon").classList.remove("cmp-icon-arrowdown");
    		_$("#attIcon").classList.add("cmp-icon-arrowup");
    		_$("#attListUl").style.display = "block";
    		showAtt = true;
    	}
    });
	_$("#meetingTask").addEventListener("tap",gotoMeetingTaskHtml);
}

/********************************** 页面提交及跳转  ***********************************/

function gotoMeetingUserListHtml() {
	page.operate = "summary";
	cmp.href.next(_meetingPath + "/html/meetingUserList.html"+meetingBuildVersion, page);
}
function gotoMeetingTaskHtml(){
	//缓存数据
	_M3_Save_Storage();
	//跳转会议任务页面
	taskmanageApi.jumpToTask({
		sourceType:6,
		sourceId:page.meeting.id
	});
}

