var urlParam;
var page = {};
var pageX = {};
var _storge_key = "";

/********************************** 初发化方法  ***********************************/

cmp.ready(function () {//缓存详情页面数据
	
	_initParamData();
	
	_initPageBack();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources",function(){
		
		_initPageData();
		
		_initEvent();
		
	},meetingBuildVersion);
	
});

function _initParamData() {
	urlParam = cmp.href.getParam();
	_storge_key = MeetingCacheKey.summary.reply.comment;
}

/****************************** 监听返回事件(放到最前头)  ******************************/

function _initPageBack() {
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}

function _goBack(nocheck) {
    
    if(nocheck){
        cmp.href.back();
    }else{
        var contentValue = document.getElementById("content").value;
        if(contentValue == '') {
            cmp.href.back();
        } else {
            var btnArray = [cmp.i18n("meeting.page.lable.no"), cmp.i18n("meeting.page.lable.yes")];
            cmp.notification.confirm(cmp.i18n("meeting.page.confirm.exitReply"),function(e) {
                //e==1 是 e==0否
                if (e == 1) {
                    cmp.href.back();
                } else {
                    //留在当前页面
                }
            }, cmp.i18n("meeting.page.confirm.title"), btnArray);
        }
    }
}

/********************************** 页面数据初始化  ***********************************/

function _storePageObj() {
	page.cacheContent = _$("#content").value;
	
    cmp.storage.save(_storge_key, cmp.toJSON(page), true);
}

function _initPageData() {
	
	var cacheData = MeetingUtils.loadCache(_storge_key, true);
    
    //缓存不存在
    if(!cacheData) {
	
		//因为此处不需要发请求数据，接收传输过来的数据
		page = urlParam;
		
		page.hiddencomment = false;
		page.hidden2creator = false;
		page.sendMsg = true;//发送消息默认为ON
		
		page.commentAttachment = {};
		page.commentAttachment.fileUrlIds = [];
		page.commentAttachment.fileJson = "[]";
		page.commentAttachment.fileComponent = null;
		
		page.isFromStorage = false;
    } else {
    	page = cacheData;
    	page.isFromStorage = true;
    }
    
    _initHtml();
}

/********************************** 页面布局  ***********************************/

function _initHtml() {
	
	/** 意见回填 **/
	_initFillData();
	
	/** 附件组件 **/
	_initAttachment();
	
	/** 加载页面信息 **/
	_initPageInfo();
	
	/** 页面样式 **/
	_initPageStyle();
	
}

function _initPageInfo() {
	//国际化title标签
	_$("title").innerText=cmp.i18n("meeting.page.action.reply");
	
	//显示会议回复框提示语
	document.getElementById('content').setAttribute('placeholder', cmp.i18n("meeting.page.action.meetingAdvice"));
}

function _initFillData() {
	if(page.isFromStorage) {//从缓存中取数据
		_$("#content").value = page.cacheContent;

		//初始化开关
	    var swContainer = _$("#opinionArea");
	    _initSwitch("hiddencomment", page.hiddencomment, swContainer);
	    if(!page.hiddencomment) {
	        page.hidden2creator = false;
	    }
	    _initSwitch("hidden2creator", page.hidden2creator, swContainer);
	    _initSwitch("sendMsg", page.sendMsg, swContainer);

	}
	
	_initInputEvent();
}

/**
 * 切换开关
 */
function _initSwitch(code, value, container){
    
    var $switchs = container || _$("#opinionArea"),
        sws =_$(".cmp-switch", true, $switchs);
    
    for(var i = 0; i < sws.length; i++){
        var sw = sws[i];
        if(sw.getAttribute("code") == code){
            var isActive = sw.classList.contains("cmp-active");
            if(value != isActive){
                if(value){
                    sw.classList.add("cmp-active");
                    if(code == "hiddencomment"){
                        //如果没有勾选隐藏，也不能对发起人隐藏
                        _$(".cmp-switch[code='hidden2creator']").classList.remove("cmp-disabled");
                    }
                }else{
                    sw.classList.remove("cmp-active");
                }
            }
            break;
        }
    }
}


function _initPageStyle() {
	calHeight();
	
	//初始化页面布局
	_initListView();
}

//附件
function _initAttachment() {
//	if(!cmp.platform.CMPShell) {
//		document.getElementById("attchmentFile").remove();
//		return;
//	}
	var initParam = {
		showAuth : cmp.platform.miniprogram ? 1 : -1,
		uploadId : "picture",
		handler : "#attchmentFile",
		selectFunc : function(fileArray){
			//展示附件数量
		    var tempCount = fileArray.length;
		    var tempText = "";
		    if(tempCount > 0){
		        tempText = tempCount;
		    }
		    document.getElementById("attCount").innerHTML = tempText;
		    //附件图标有附件时显示蓝色
		    var attDom = document.querySelector("#attchmentFile");
		    if(!attDom.classList.contains("cmp-active")){
		    	attDom.classList.add("cmp-active");
		    }
		}
	}
	page.commentAttachment.fileComponent = new SeeyonAttachment({initParam : initParam});
}


function _initListView() {
	//必要函数 滚动列表
	//cmp.listView('#content1');
}

/********************************** 初始化控件事件  ***********************************/

function _initEvent() {
	
	document.addEventListener('beforepageredirect', _storePageObj);
	
	_$("#sendBtn").addEventListener("tap", _submitForm);
	
	_$("#content").addEventListener("input", _initInputEvent);
	
	cmp(".cmp-switch").each(function(){
		this.addEventListener("toggle",function(e){
			var _id = this.getAttribute("cmp-data");
			if(_id == "hiddencomment"){
				if(e.detail.isActive){
					document.getElementById("id_hidden2creator").classList.remove("cmp-disabled");
				}else{
					_closeBtn("id_hidden2creator");
					document.getElementById("id_hidden2creator").classList.add("cmp-disabled");
				}
			}
			page[this.getAttribute("cmp-data")] = e.detail.isActive;
		});
	});
}

function _closeBtn(id){
	page[id] = false;
	var obj = document.getElementById(id);
	obj.classList.remove("cmp-active");
	obj.children[0].style.transform = "translate3d(0,0,0)";
	obj.children[0].style.webkitTransform= "translate3d(0,0,0)";
}

function _initInputEvent() {
	var feedback = _$("#content"),maxLength = 500;
	var content = feedback.value;
	if(content.length > maxLength){
		feedback.value = content.substr(0,maxLength);
		content = feedback.value;
	}
	//剩余可以输入的字数
	_$("#fontCount").innerHTML = maxLength-content.length;
}

/********************************** 页面提交及跳转  ***********************************/

var isSubmit = false;
function _submitForm() {
	//防止重复点击
	if(isSubmit){
		return;
	}
	isSubmit = true;
	
	cmp.dialog.loading();
	if(_$("#content").value.length == 0){
		cmp.dialog.loading(false);
		cmp.notification.alert(cmp.i18n("meeting.page.alert.noEmptyReply"),null,cmp.i18n("meeting.page.alert.sysMessage"),cmp.i18n("meeting.page.action.cancle"));
		isSubmit = false;
		return;
	}
	var paramData = {};
	var emojiUtil = cmp.Emoji();
	paramData.content = emojiUtil.EmojiToString(_$("#content").value);
	paramData.meetingId = page.meeting.id;
	paramData.replyId = page.tempRelyId;
	paramData.proxyId = page.meeting.proxyId;
	paramData.hiddencomment = page.hiddencomment;
	paramData.hidden2creator = page.hidden2creator;
	paramData.sendMsg = page.sendMsg;
	paramData.memberId = page.memberId;
	
	//附件数据
	if(page.commentAttachment.fileComponent) {
		paramData.fileJson = cmp.toJSON(page.commentAttachment.fileComponent.getFileArray());
	}
	
	$s.Meeting.comment({}, paramData, {
		success : function(result) {
			cmp.dialog.loading(false);
			_goBack(true);
		},
        error : function(result){
        	//解除各按钮的绑定
        	_removeEvent();
        	//处理异常
        	MeetingUtils.dealError(result);
        }
	});
}
//弹出软键盘计算页面高度
function calHeight(){
	var winH = window.innerHeight;
	var footerH = _$(".number").offsetHeight + _$("footer").offsetHeight;
	var headerH = 0;//_$("header").offsetHeight;
	_$("#content").style.height = winH - footerH -headerH +"px";
}

function _removeEvent(){
	_$("#sendBtn").removeEventListener("tap", _submitForm);
}