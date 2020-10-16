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
			roomId : MeetingUtils.getQueryString("roomId")
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
		if(!parameters.roomId){
			cmp.notification.toast("no meetingRoomId found!");
			return;
		}
		$s.Meeting.meetingRoomApplyCondition(undefined,{meetingRoomId : parameters.roomId},{
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
	
	/**
	 * 事件绑定
	 */
	var events = function(){

	}
	
	/**
	 * 渲染结果
	 */
	var render = function(data){
		var tpl;
		var tplData = formatRenderData(data);
		if(tplData.isError){
			tpl = document.querySelector("#signTpl-error").innerHTML;
			document.querySelector(".meeting-sign-container").innerHTML = cmp.tpl(tpl,tplData);
		}else{
			cmp.href.next(_meetingPath + "/html/meetingRoomOccupancyCondition.html",{roomId : parameters.roomId});
		}
	}
	
	var formatRenderData = function(data){
		var renderData = {};
		/**
		 * 会议室不存在
		 */
		if(!data.isMeetingRoomExist){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.meetingRoom.msg.error1");
			renderData.meetingRoomName = cmp.i18n("meeting.meetingRoom.msg.error1");
			return renderData;
		}
		
		/**
		 * 会议室删除
		 */
		if(data.roomDeleted){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.roomDeleted");
			renderData.meetingRoomName = cmp.i18n("meeting.qrCode.roomDeletedMsg",[data.meetingRoomName]);
			return renderData;
		}
		
		/**
		 * 会议室停用
		 */
		if(!data.roomEnable){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.roomDisabled");
			renderData.meetingRoomName = cmp.i18n("meeting.qrCode.roomDisabledMsg",[data.meetingRoomName]);
			return renderData;
		}
		
		/**
		 * 二维码禁用
		 */
		if(!data.qrCodeEnable){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.qrCodeDisabled");
			renderData.meetingRoomName = cmp.i18n("meeting.qrCode.qrCodeDisabledMsg",[data.meetingRoomName]);
			return renderData;
		}
		
		/**
		 * 无权限
		 */
		if(!data.roomAuth){
			renderData.isError = true;
			renderData.errorMsg = cmp.i18n("meeting.qrCode.roomNoAuth");
			renderData.meetingRoomName = cmp.i18n("meeting.qrCode.roomNoAuthMsg",[data.meetingRoomName]);
			return renderData;
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
