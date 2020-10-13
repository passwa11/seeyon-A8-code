(function(){
	var parameters;
	
	cmp.ready(function(){
		//返回事件
		initBack();
		//参数
		initParam();
		//加载数据
		initData();
		//事件
		bindEvents();
	});
	
	var initBack = function(){
		cmp.backbutton();
    	cmp.backbutton.push(function(){
    		cmp.href.closePage();
    	});
	}
	
	var initParam = function(){
		parameters = {
			meetingId : MeetingUtils.getQueryString("meetingId")
		}
	}
	
	/**
	 * 数据加载
	 */
	var initData = function(){
		if(!parameters.meetingId){
			cmp.notification.toast("no meetingId found!");
			return;
		}
		$s.Meeting.meetingInviteDetail(undefined,{meetingId : parameters.meetingId},{
			success : function(result){
				render(result.data);
			},
			error : function(error){
				var cmpHandled = cmp.errorHandler(error);
				if(!cmpHandled){
					console.log(error);
					if(error.message){
						cmp.notification.alert(error.message);
					}else{
						cmp.notification.alert(cmp.toJSON(error));
					}
				}
			}
		});
	}
	
	var initDom = function(){
		var scoller = document.querySelector("#scroller");
		var footer = document.querySelector(".meeting-invite-btn");
		
		var windowH = window.innerHeight;
		var footerH = footer ? footer.offsetHeight : 0;
		
		if(scoller){
			scoller.style.height = (windowH - footerH) + 'px';
		}
		
	}
	
	var render = function(data){
		var tpl = document.querySelector("#invite_tpl").innerHTML;
		var container = document.querySelector(".meeting-invite-container");
		container.innerHTML = cmp.tpl(tpl,data);
		/**
		 * 渲染附件
		 */
		if(document.querySelector("#attListUl") && data.fileAttachments.length > 0){
			var loadParam = {
				selector : "#attListUl",
				atts : data.fileAttachments
			}
			new SeeyonAttachment({loadParam : loadParam});
		}
		
		if(data.bodyType.toLowerCase() != "html"){
			_alert(cmp.i18n("meeting.page.msg.notSupportOffice"));
		}else{
			//初始化会议正文
			if(document.querySelector("#meetingContent")){
				var contentConfig = {
					"target" : "meetingContent",
					"bodyType" : SeeyonContent.getBodyCode(data.bodyType),
					"padding" : "0",
					"content" : data.content,
					"lastModified" : data.lastModified,
					"moduleType" : "6",
					"rightId" : "",
					"viewState" : "",
					"momentum" : true,
					"onload" : null,
					"allowTrans" : data.allowTrans,
					"onScrollBottom" : function(){
					
					},
					"ext" : {
						reference : parameters.meetingId
					}
				}
				SeeyonContent.init(contentConfig);
			}
		}
		
		//页面计算
		initDom();
		
		//滑动
		if(document.querySelector("#scroller")){
			new cmp.iScroll('#scroller', {hScroll: false, vScroll: true,useTransition:true});
		}
	}
	
	var bindEvents = function(){
		/**
		 * 附件显示和隐藏
		 */
		cmp(".meeting-invite-att").on("tap",".right",function(){
			if(this.classList.contains("see-icon-v5-common-arrow-down")){
				this.classList.remove("see-icon-v5-common-arrow-down");
				this.classList.add("see-icon-v5-common-arrow-top");
				document.querySelector("#attListUl").style.display = "";
			}else{
				this.classList.remove("see-icon-v5-common-arrow-top");
				this.classList.add("see-icon-v5-common-arrow-down");
				document.querySelector("#attListUl").style.display = "none";
			}
		});
		
		/**
		 * 参加按钮
		 */
		var isProcessing = false;
		cmp("body").on("tap",".meeting-invite-btn button.attend",function(){
			if(isProcessing){
				return;
			}
			isProcessing = true;
			$s.Meeting.attendMeeting(undefined,{meetingId : parameters.meetingId},{
				success : function(result){
					cmp.notification.toast(cmp.i18n("meeting.page.action.attendSuccess"), "center",2000,1);
			
					/**
					 * 手动切换参加按钮的样式
					 */
					var attendButton = document.querySelector(".meeting-invite-btn button.attend");
					attendButton.classList.remove("attend");
					attendButton.classList.add("has-attend");
					attendButton.innerHTML = cmp.i18n("meeting.page.action.attended");
					attendButton.disabled = true;
			
					setTimeout(function(){
						isProcessing = false;
					},1000);
				},
				error : function(error){
					var cmpHandled = cmp.errorHandler(error);
					if(!cmpHandled){
						console.log(error);
						if(error.message){
							cmp.notification.alert(error.message);
						}else{
							cmp.notification.alert(cmp.toJSON(error));
						}
					}
					isProcessing = false;
				}
			});
		});
	}
})();
