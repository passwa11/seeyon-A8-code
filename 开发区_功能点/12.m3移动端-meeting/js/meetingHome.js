(function(){
	var pendingListView,appliedRoomsListView,meetingSecurity = {};
	cmp.ready(function(){
		//加载国际化
		cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources",function(){
			setPageTitle(cmp.i18n("meeting.page.lable.meeting"));
		});
		//返回事件
		initPageBack();
		//初始参数
		initParam();
		//dom
		initLayout();
		//数据
		initData();
		//事件
		bindEvent();
	});
	
	/**
	 * 返回事件
	 */
	var initPageBack = function(){
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
			pendingListView && pendingListView.refreshInitData();
			appliedRoomsListView && appliedRoomsListView.refreshInitData();
		}
	}
	
	var initParam = function(){
		
	}
	
	var initLayout = function(){
		var header = document.querySelector(".headContainer");
		var btnContainer = document.querySelector(".btnContainer");
		var tabControl = document.querySelector("#segmentedControl");
		
		var pendingList = document.querySelector("#pendingList");
		var appliedRooms = document.querySelector("#appliedRooms");
		
		var windowH = window.innerHeight;
		var headerH = header ? header.offsetHeight : 0;
		var btnH = btnContainer ? btnContainer.offsetHeight : 0;
		var tabH = tabControl ? tabControl.offsetHeight : 0;
		
		/*pendingList.style.height = (windowH - tabH) + 'px';
		appliedRooms.style.height = (windowH - tabH) + 'px';*/
		
		/**
		 * 自定义源生头部
		 */
		cmp.header.customCMPHeader({
	        backgroundColor: '#4D63F1',
	        textColor: '#ffffff',
	        statusBarValue: 1
		});
		//设置通用app按钮颜色
		cmp.api.setCommonAppEntryBtnColor("#ffffff");
	}
	
	//初始数据
	var initData = function(){
		//加载待开会议列表
		initPendingList();
		//加载已申请会议室列表
		initAppliedRooms();
		//加载会议权限配置
		initSecurity();
	}

	var initSecurity = function(){
		MeetingUtils.meetingSecurity(function(security){
			meetingSecurity = security;
		});
	}
	
	//事件
	var bindEvent = function(){
		//待开会议点击
		cmp("#pendingList").on("tap","li",function(){
			var meetingId = this.getAttribute("id");
			var affairId = this.getAttribute("affairId");
			var proxyId = this.getAttribute("proxyId");
			
			var paramData = {
				meetingId : meetingId,
				affairId : affairId,
				proxyId : proxyId
			}
			
			var option = {
				pushInDetailPad : true
			};
			
			cmp.href.next(_meetingPath + "/html/meetingDetail.html", paramData, option);
		});
		
		//已申请会议室列表点击事件
		cmp("#appliedRooms").on("tap","li",function(){
			var roomAppId = this.getAttribute("roomappId");
			
			var paramData = {
				openFrom : "mrApproveList",
				roomAppId : roomAppId
			}
			
			var option = {
				pushInDetailPad : true
			};
    		
    		cmp.href.next(_meetingPath + "/html/meetingRoomApprove.html", paramData,option);
		});
		
		//我的会议
		document.querySelector("#myMeetingList").addEventListener("tap",function(){
			cmp.href.next(_meetingPath + "/html/meeting_list_mine.html",{});
		});
		
		//会议室
		document.querySelector("#meetingRoomList").addEventListener("tap",function(){
			if(!meetingSecurity.haveMeetingRoomApp && !meetingSecurity.haveMeetingRoomPerm){
				_alert("无会议室权限！");
				return;
			}
			cmp.href.next(_meetingPath + "/html/meetingRoomAdminApproveList.html",{});
		});
		
		//会议纪要
		document.querySelector("#meetingSummaryList").addEventListener("tap",function(){
			cmp.href.next(_meetingPath + "/html/meetingSummaryList.html",{});
		});
		
		//新建会议
		document.querySelector("#createMeeting").addEventListener("tap",function(){
			if(!meetingSecurity.haveMeetingArrangeRole){
				_alert("无权新建会议！");
				return;
			}
			var option = {
				pushInDetailPad : true
			};
			cmp.href.next(_meetingPath + "/html/meetingCreate.html",{fromApp:6},option);
		});
		
		//申请会议室
		document.querySelector("#applyRoom").addEventListener("tap",function(){
			if(!meetingSecurity.haveMeetingRoomApp){
				_alert("无会议室权限！");
				return;
			}
			cmp.href.next(_meetingPath + "/html/meetingRoomList.html",{action:"applyMeetingRoom"});
		});
	}
	
	/**
	 * 初始待开会议列表
	 */
	var initPendingList = function(){
		pendingListView = cmp.listView("#pendingList", {
			imgCache:true,
		    config: {
		        pageSize: 20,
		        params: {},
		        dataFunc: function(params,options) {
		        	$s.Meeting.findPendingMeetings({}, params, {
			    		success : function(result) {
			    			//待开会议数量
			    			var domTarget = document.querySelector("#pendingNum");
			    			domTarget.innerHTML = result.data.total;
			    			try {
			    				if(Number( result.data.total)>0){
			    					domTarget.style.display = 'inline-block';
			    				}else{
			    					domTarget.style.display = 'none';
			    				}
			    			} catch(e) {
			    			}
			    			if(options.success) {
			            		options.success(result.data);
			            	}
			            },
			            error : function(result){
			            	MeetingUtils.dealError(result);
			            }
			        });
		        },
		        renderFunc: function(result,isRefresh){
		        	var tpl = document.querySelector("#tpl_pending_list").innerHTML;
		        	var container = document.querySelector("#pendingList ul");
		        	var html = cmp.tpl(tpl,result);
		        	if(isRefresh){
		        		container.innerHTML = html;
		        	}else{
		        		container.innerHTML += html;
		        	}
		        	cmp.i18n.detect();
		        }
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
	
	/**
	 * 加载已申请会议室
	 */
	var initAppliedRooms = function(){
		appliedRoomsListView = cmp.listView("#appliedRooms", {
			imgCache:true,
		    config: {
		        pageSize: 20,
		        params: {
		        	openFrom : 'meetingRoomList'
		        },
		        dataFunc: function(params,options) {
		        	$s.Meeting.getApplyMeemtingRooms({}, params, {
			    		success : function(result) {
			    			if(options.success) {
			            		options.success(result);
			            	}
			            },
			            error : function(result){
			            	MeetingUtils.dealError(result);
			            }
			        });
		        },
		        renderFunc: function(result,isRefresh){
		        	var tpl = document.querySelector("#tpl_applied_rooms").innerHTML;
		        	var container = document.querySelector("#appliedRooms ul");
		        	var html = cmp.tpl(tpl,result);
		        	if(isRefresh){
		        		container.innerHTML = html;
		        	}else{
		        		container.innerHTML += html;
		        	}
		        	cmp.i18n.detect();
		        }
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
})();
