var urlParam;
var page = {};
var pageX = {};
var _storge_key = "";

/********************************** 初发化方法  ***********************************/

cmp.ready(function () {
	
	_initParamData();
	
	_initPageBack();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		setPageTitle(cmp.i18n("meeting.page.action.receipt"));
		
		_initPageData();
		
		setTimeout(_initEvent, 20);
		
	},meetingBuildVersion);
});

function _initParamData() {
	urlParam = cmp.href.getParam();
	_storge_key = MeetingCacheKey.summary.reply;
}

/****************************** 监听返回事件(放到最前头)  ******************************/

function _initPageBack() {
  //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}

function _goBack() {
	var contentValue = document.getElementById("content").value;
	if(contentValue == '') {
		page.isFromStorage = false;
	    cmp.href.back();
	} else {
		var btnArray = [cmp.i18n("meeting.page.lable.no"), cmp.i18n("meeting.page.lable.yes")];
		cmp.notification.confirm(cmp.i18n("meeting.page.confirm.exitReply"),function(e) {
			//e==1 是 e==0否
			if (e == 1) {
				page.isFromStorage = false;
				
			    cmp.href.back();
			} else {
				//留在当前页面
			}
		}, cmp.i18n("meeting.page.confirm.title"), btnArray)
	}
}

/********************************** 页面数据初始化  ***********************************/

function _storePageObj(){
	page.cacheContent = _$("#content").value;
	if (page.meeting.businessType == 9) {//告知状态
		page.cacheFeedbackFlag = 3;
	} else{
	    var flagBtns = _$('button[name="feedbackFlag"]', true);
	    for(var i = 0, len = flagBtns.length; i < len; i++){
	        var btn = flagBtns[i];
	        if(btn.classList.contains("cmp-active")){
                page.cacheFeedbackFlag = btn.getAttribute("data-value");
            }
	    }
	}
	
    pageX.cache = {};
    pageX.cache.page = page;
    cmp.storage.save(_storge_key, cmp.toJSON(pageX.cache.page), true);
}

function _initPageData() {
	
    var cacheData = MeetingUtils.loadCache(_storge_key, true);
    
    //缓存不存在
    if(!cacheData) {
		//因为此处不需要发请求数据，接收传输过来的数据
		page = urlParam;
		
		page.replyAttachment = {};
		page.replyAttachment.fileUrlIds = [];
		page.replyAttachment.fileComponent = null;
		
		page.isFromStorage = false;
    } else {
    	page = cacheData;
    	page.isFromStorage = true;
    	
    	cmp.storage["delete"](_storge_key, true);
    }
    
    _initHtml();
	
}

/********************************** 页面布局  ***********************************/

function _initHtml() {
	
	/** 意见回填 **/
	//回填回复态度
    var cmp_handle=document.querySelector('#attitudeForm');
    var handle=cmp_handle.querySelectorAll('.handle');
    for(var k= 0,len2=handle.length;k<len2;k++){
        (function(_){
            handle[_].addEventListener('tap',function(){
                for(var m= 0,n=handle.length;m<n;m++){
                    handle[m].classList.remove('cmp-active');
                }
                this.classList.add('cmp-active');
            },false);
        })(k);
    }
	
	if(page.isFromStorage) {//从缓存中取数据
		_initFillData(page.cacheContent, page.cacheFeedbackFlag);
	} else {
		//回填会议回复意见
		var replyList = page.replyList;
		var userId = page.currentUserId;
		var reply = {};
		var initAtt;
		var replyUserId = userId;
		if(page.meeting.proxyId && page.meeting.proxyId!=null && page.meeting.proxyId!="" && page.meeting.proxyId!="0") {
			replyUserId = page.meeting.proxyId;
		}
		for (var i = 0; i < replyList.length; i++) {
			if(replyList[i].userId == replyUserId) {
				reply = replyList[i];
				_initFillData(reply.feedback, reply.feedbackFlag);
				initAtt = reply.attachmentList
				break;
			}
		}
	}
	
	/** 附件组件 **/
	_initAttachment(initAtt);
	
	/** 加载页面信息 **/
	_initPageInfo();
	
	/** 页面样式 **/
	_initPageStyle();
	
}

function _initPageInfo() {
	
	//显示会议回复框提示语
	document.getElementById('content').setAttribute('placeholder', cmp.i18n("meeting.page.action.meetingAdvice"));
	
	//震荡回复及知会节点 隐藏回复态度
	if(page.operate != "comment" && page.meeting.businessType != 9) {
		_$("#attitudeForm").style.display = "";
	}
}

function _initPageStyle() {
	showHeight();

	cmp.description.init(document.querySelector("#content"));
}

//数据回填
function _initFillData(feedback, feedbackFlag) {
	_$("#content").value = typeof(feedback) == "undefined" ? "" : feedback;
	var flagBtns = _$('button[name="feedbackFlag"]', true);
	for(var i = 0, len = flagBtns.length; i< len; i++){
	    var btn = flagBtns[i];
	    if(btn.getAttribute("data-value")==1){
            if(btn.classList.contains("cmp-active")){
                btn.classList.remove("cmp-active");
            }
        }
        if(feedbackFlag == btn.getAttribute("data-value")){
            if(!btn.classList.contains("cmp-active")){
                btn.classList.add("cmp-active");
            }
        }
	}
	_initInputEvent();
}

//附件
function _initAttachment(initAtt){
	if(initAtt && initAtt.length > 0){
		var tempCount = initAtt.length;
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
	var initParam = {
		showAuth : -1,
		uploadId : "picture",
		handler : "#attchmentFile",
		initAttData : initAtt,
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
	page.replyAttachment.fileComponent = new SeeyonAttachment({initParam : initParam});
}

/********************************** 初始化控件事件  ***********************************/

function _initEvent() {    
	document.addEventListener('beforepageredirect', _storePageObj);
	
	_$("#sendBtn").addEventListener("tap", _submitForm);
	_$("#content").addEventListener("input", _initInputEvent);
}

function _removeEvent(){
	_$("#sendBtn").removeEventListener("tap", _submitForm);
}

function _initInputEvent() {
	var feedback = _$("#content"),maxLength = 1200;
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
	
	var paramData = {};
	var emojiUtil = cmp.Emoji();
    var content = emojiUtil.EmojiToString(_$("#content").value);
	paramData.content = content;
	paramData.meetingId = page.meeting.id;
	paramData.proxyId = page.meeting.proxyId;
	paramData.replyId = page.tempRelyId;
	paramData.pagetype = 1;
	paramData.memberId = page.memberId;
	
	if (page.meeting.businessType == 9) {//告知状态
		paramData.feedbackFlag = 3;
	} else{
	    var flagBtns = _$('button[name="feedbackFlag"]', true);
        for(var i = 0, len = flagBtns.length; i < len; i++){
            var btn = flagBtns[i];
            if(btn.classList.contains("cmp-active")){
                paramData.feedbackFlag = btn.getAttribute("data-value");
            }
        }
	}
	
	//附件数据
	if(page.replyAttachment.fileComponent) {
		paramData.fileJson = cmp.toJSON(page.replyAttachment.fileComponent.getFileArray());
	}
	
	$s.Meeting.reply({}, paramData, {
		success : function(result) {
			if(result["errorMsg"] && result["errorMsg"]!="") {
        		cmp.notification.alert(result["errorMsg"], null, cmp.i18n("meeting.page.dialog.note"), cmp.i18n("meeting.page.dialog.OK"));
        		isSubmit = false;
        		return;
        	}
			
			_replySucceed();
			refreshCmpPending();
			_submitGoBack();
		},
        error : function(result){
        	//解除各按钮的绑定
        	_removeEvent();
        	//处理异常
        	MeetingUtils.dealError(result);
        }
	});
}

function _submitGoBack() {
    if(MeetingUtils.getBackURL() == "weixin"){
        //返回到外层, 微信入口逻辑，因为微信没办法返回到首页，所以这样处理， 暂时不要和else分支合并
        cmp.href.closePage();
    }else {
        //返回到外层
        cmp.href.back(2);
    }
}

function refreshCmpPending(){
	cmp.webViewListener.fire({ 
	    type: 'com.seeyon.m3.ListRefresh',
	    data: {affairid: urlParam["affairId"]}
	});
	//触发平台事件，用于刷新列表数据
    cmp.webViewListener.fire({
        type: "com.seeyon.m3.ListRefresh",
        data: {type: 'update'}
    });
	cmp.webViewListener.fire({
		type : "meeting.ListRefresh",
		data : {
			refreshList : "true"
		}
	});
}

/********************************** 页面使用工具  ***********************************/

//回执成功提示
function _replySucceed() {
	cmp.notification.toast(cmp.i18n("meeting.page.action.replySucceed"), 'top', 1000);
}
//隐藏或展示意见区域
function hideOrShowAttForm(){
	var styleDisply = _$("#attitudeForm").style.display;
	if(styleDisply == "none"){
		showHeight();
	}else{
		calHeight();
	}
}
//弹出软键盘计算页面高度
function calHeight(){
	var winH = window.innerHeight;
	var footerH = _$(".number").offsetHeight + _$("footer").offsetHeight;
	var headerH = 0;//_$("header").offsetHeight;
	_$("#attitudeForm").style.display = "none";
	_$("#content").style.height = winH - footerH -headerH +"px";
}
function showHeight(){
    //震荡回复及知会节点 隐藏回复态度
    if(page.operate != "comment" && page.meeting.businessType != 9) {
        _$("#attitudeForm").style.display = "";
    }
	var winH = window.innerHeight;
	var footerH = _$(".number").offsetHeight + _$("footer").offsetHeight;
	var headerH = 0;//_$("header").offsetHeight;
	var navH = _$("#attitudeForm").offsetHeight;
	_$("#content").style.height = winH - footerH - headerH - navH +"px";
}