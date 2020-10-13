(function(){
	var parameters;
	var signCondition;
	
	cmp.ready(function(){
		//返回事件
		backEvent();
		//初始参数
		initParam();
		//页面高度
		initPage();
		//加载数据
		initData();
		//事件
		events();
	});
	
	/**
	 * 返回事件
	 */
	var backEvent = function(){
		cmp.backbutton();
    	cmp.backbutton.push(function(){
    		cmp.href.closePage();
    	});
	}
	
	/**
	 * 初始化页面参数
	 */
	var initParam = function(){
		parameters = {
			meetingId : MeetingUtils.getQueryString("meetingId")
		}
	}
	
	/**
	 * 页面高度计算
	 */
	var initPage = function(){
		var windowH= window.innerHeight;
		document.querySelector(".meeting-sign-container").style.height = windowH + 'px';
	}
	
	/**
	 * 数据加载
	 */
	var initData = function(){
		if(!parameters.meetingId){
			cmp.notification.toast("no meetingId found!");
			return;
		}
		$s.Meeting.meetingSignCondition(undefined,{meetingId : parameters.meetingId},{
			success : function(result){
				signCondition = result.data;
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
	
	/**
	 * 事件绑定
	 */
	var events = function(){
		/**
		 * 签到按钮
		 */
		var isProcessing = false;
		cmp("body").on("tap",".sign-btn button",function(){
			if(!signCondition.hasSign && signCondition.isMeetingActive){
				if(isProcessing){
					return;
				}
				isProcessing = true;
				$s.Meeting.meetingSign(undefined,{meetingId : parameters.meetingId},{
					success : function(result){
						cmp.notification.toast(cmp.i18n("meeting.qrCode.checkInSuccess"), "center",2000,1);
						initData();
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
			}
		});
	}
	
	/**
	 * 渲染结果
	 */
	var render = function(data){
		var tpl;
		var tplData = formatRenderData(data);
		if(tplData.isError){
			tpl = document.querySelector("#signTpl-error").innerHTML;
		}else{
			tpl = document.querySelector("#signTpl-normal").innerHTML;
		}
		document.querySelector(".meeting-sign-container").innerHTML = cmp.tpl(tpl,tplData);
		/**
		 * 页面渲染完成后弹出访客提示
		 */
		if(tplData.showVisitorTips){
	        cmp.notification.confirm(cmp.i18n("meeting.sign.confirm.msg1"), function (index) {
                if (index == 1) {
                    cmp.notification.close();
                }
            },null,[cmp.i18n("meeting.page.dialog.OK")]);
		}
	}
	
	var formatRenderData = function(data){
		var renderData = {};
		/**
		 * 会议已结束
		 */
		if(data.isMeetingEnd){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.meetingEndjj");
			renderData.meetingName = cmp.i18n("meeting.qrCode.meetingEndMsg",[data.meetingName,data.meetingEndDate]);
			return renderData;
		}
		
		/**
		 * 会议已撤销
		 */
		if(data.isMeetingCancel){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.meetingCancel");
			renderData.meetingName = cmp.i18n("meeting.qrCode.meetingCancelMsg",[data.meetingName]);
			return renderData;
		}
		
		/**
		 * 二维码禁用
		 */
		if(!data.showMeetingQrCode){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.expired");
			renderData.meetingName = "";
			return renderData;
		}
		
		/**
		 * 会议审核中
		 */
		if(!data.isAudited){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.meetingAudit");
			renderData.meetingName = cmp.i18n("meeting.qrCode.meetingAuditMsg",[data.meetingName]);
			return renderData;
		}
		
		/**
		 * 未到签到时间
		 */
		if(!data.isMeetingActive){
			renderData.isError = false;
			renderData.meetingSignBtnShow = false;
			renderData.meetingName = data.meetingName;
			renderData.meetingBeginDate = data.meetingBeginDate;
			renderData.meetingSignTime = formatDate(data.meetingSignDate,'hh:mm');
			renderData.meetingSignDate = formatDate(data.meetingSignDate,'yyyy-MM-dd');
			renderData.meetingSignTips = cmp.i18n("meeting.qrCode.signStartTime");
			renderData.meetingSignBtn = cmp.i18n("meeting.qrCode.beforeSignTime");
			renderData.meetingBeginTips = cmp.i18n("meeting.qrCode.meetingStartMsg",[data.meetingBeginDate]);
			return renderData;
		}
		
		/**
		 * 不在参会人中
		 */
		if(!data.isMeetingMember && !data.isPublic){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.noMeetingAuth");
			renderData.meetingName = cmp.i18n("meeting.qrCode.noMeetingAuthMsg",[data.meetingName]);
			return renderData;
		}
		
		/**
		 * 已经签到
		 */
		if(data.hasSign){
			renderData.isError = false;
			renderData.meetingSignBtnShow = false;
			renderData.meetingName = data.meetingName;
			renderData.meetingBeginDate = data.meetingBeginDate;
			renderData.meetingSignTime = formatDate(data.signDate,'hh:mm');
			renderData.meetingSignDate = "";
			renderData.meetingSignTips = cmp.i18n("meeting.qrCode.yourSignTime");
			renderData.meetingSignBtn = cmp.i18n("meeting.qrCode.signedIn");
			renderData.meetingBeginTips = cmp.i18n("meeting.qrCode.meetingStartMsg",[data.meetingBeginDate]);
			return renderData;
		}
		
		/**
		 * 正常的签到
		 */
		renderData.isError = false;
		renderData.meetingSignBtnShow = true;
		renderData.meetingName = data.meetingName;
		renderData.meetingBeginDate = data.meetingBeginDate;
		renderData.meetingSignTime = formatDate(new Date().getTime(),'hh:mm');
		renderData.meetingSignDate = "";
		renderData.meetingSignTips = cmp.i18n("meeting.qrCode.currentTime");
		renderData.meetingSignBtn = cmp.i18n("meeting.qrCode.confirmSign");
		renderData.meetingBeginTips = cmp.i18n("meeting.qrCode.meetingStartMsg",[data.meetingBeginDate]);
		
		/**
		 * 最后判断是否提示访客签到
		 */
		if(!data.isMeetingMember && data.isPublic && !data.hasSign){
			renderData.showVisitorTips = true;
		}
		
		return renderData;
	}
	
	/**
	 * 日期格式
	 * @param {Object} sec
	 * @param {Object} pattern
	 */
	var formatDate = function(sec,pattern){
		return new Date(parseInt(sec)).format(pattern);
	}
})();
