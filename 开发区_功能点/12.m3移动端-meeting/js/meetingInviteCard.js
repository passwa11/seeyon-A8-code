(function(){
	var parameters;
	
	cmp.ready(function(){
		//返回事件
		backEvent();
		//初始参数
		initParam();
		//页面计算
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
    		cmp.href.back();
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
	 * 页面计算
	 */
	var initPage = function(){
    	var docH = document.documentElement.clientHeight;
    	var isIphoneX = /iphone/gi.test(navigator.userAgent) && (screen.height == 812 && screen.width == 375);
    	if(isIphoneX){
    		docH = docH - 68;
    	}
    	document.documentElement.style.fontSize = docH / 667 * 20 + 'px';
	}
	
	/**
	 * 数据加载
	 */
	var initData = function(){
		if(!parameters.meetingId){
			cmp.notification.toast("no meetingId found!");
			return;
		}
		$s.Meeting.meetingInviteCard({meetingId : parameters.meetingId},{
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
		cmp("#qrCodeInviteContainer").on("tap",".sign-barcode-backdrop",function(){
			document.querySelector(".sign-barcode-backdrop").style.display="none";
		});
	}
	
	/**
	 * 渲染结果
	 */
	var render = function(data){
		var tpl = document.querySelector("#qrCodeInviteTpl").innerHTML;
		var inviteContainer = document.querySelector("#qrCodeInviteContainer .inviteContainer .border-line");
		inviteContainer.innerHTML = cmp.tpl(tpl,data);
	}
})();
