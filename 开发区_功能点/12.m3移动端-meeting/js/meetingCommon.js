

var MeetingCacheKey = (function(){

    var cacheMap = {
         "summary" : {
             "reply" : {
            	 "comment" : ""
             }
         }
    }
    
    function createPath(src, obj){
        
        for(var key in obj){
            
            var nowPath = src + key + "_";
            
            if(obj[key] != ""){
                createPath(nowPath, obj[key]);
            }else if(obj[key] == ""){
                obj[key] = nowPath;
            }
        }
    }
    
    var root = "m3_v5_meeting_";
    createPath(root, cacheMap);
    createPath = null;
    
    /**
     * 获取域下的所有缓存前缀
     */
    cacheMap.getCacheKeys = function(pObj){
        var keys = [], doSearch;
        
        function doSearch(obj, arr){
            
            if(typeof obj === "string"){
                    arr.push(obj);
            }else{
                for(var key in obj){
                    doSearch(obj[key], arr);
                }
            }
        }
        doSearch(pObj, keys);
        return keys;
    }
    
    /**
     * 删除域下的前缀与后缀拼接的缓存
     */
    cacheMap.delCacheKeys = function(pObj, subfix){
        var keys = CollCacheKey.getCacheKeys(pObj);
        for(var i = 0; i < keys.length; i++){
            cmp.storage.removeCacheData(keys[i] + subfix, true);
        }
        return keys;
    }
    
    return cacheMap;
})();

//简化选择器
function _$(selector, queryAll, pEl){
    
    var p = pEl ? pEl : document;
    
    if(queryAll){
        return p.querySelectorAll(selector);
    }else{
        return p.querySelector(selector);
    }
}

function _alert(message, completeCallback, title, buttonLabel) {
	if(!title) {
		title = cmp.i18n("meeting.page.dialog.note");
	}
	if(!buttonLabel) {
		buttonLabel = cmp.i18n("meeting.page.dialog.OK");
	}
	cmp.notification.alert(message, completeCallback, title, buttonLabel);
}

/**
 * 公共方法
 */
var MeetingUtils = {
    //判断是不是CMP壳
	isCMPShell : function(){
    	return cmp.platform.CMPShell;
    },
    getBackURL:function(){
        return MeetingUtils.getHrefParam("backURL");
    },
    getQueryString : function(){
        var s = window.location.search;
        if(s){
            //去掉文号
            s = "&" + s.substring(1);
            
            //cmp路径里面的特殊字符
            s = s.replace(/&cmphistoryuuid=\d+/i, "");
        }
        return s + window.location.hash;
    },
    getHrefParam:function(name){
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var s = window.location.search;
        if(!s){
            s = window.location.hash;
        }
        if(s){
            var r = s.substr(1).match(reg);
            if(r!=null)
                return  unescape(r[2]); 
        }
        return "";
    },/** 过滤emoji字符 **/
    getHrefQuery: function ()  {
        var url = window.location.search,
            reg_url = /^\?([\w\W]+)$/, 
            reg_para = /([^&=]+)=([\w\W]*?)(&|$|#)/g, 
            arr_url = reg_url.exec(url), 
            ret = {};
        if (arr_url && arr_url[1]) {
            var str_para = arr_url[1], result;
            while ((result = reg_para.exec(str_para)) != null) {
                ret[result[1]] = result[2];
            }
        }
        return ret;
    },
    filterUnreadableCode : function(val){
        if(!val){
            return val;
        }
        //ios输入中文只输入拼音不选汉字时特殊字符进行替换
        val = val.replace(/\u2006/g, " ");//8203特殊字符
        
        return val;
        
    },
    getDefaultImage : function(){
    	return "/meeting/img/defaultMeetingRoom.jpg";
    },
    // 找到ID下面的所有input 和 textarea，通过ID拼装成json
    formPostData : function(selector) {
        var ret = {}

        function _formData(type, ele, retJson) {
            var inputs = ele.querySelectorAll(type);
            if (inputs && inputs.length > 0) {
                for (var i = 0, len = inputs.length; i < len; i++) {
                    var input = inputs[i];
                    var tempId = input.getAttribute("id");
                    if (!tempId) {
                        tempId = input.getAttribute("name");
                    }
                    if (tempId) {
                        var inputType = input.getAttribute("type");
                        if(inputType && (inputType.toLocaleLowerCase == "radio" || inputType.toLocaleLowerCase == "checkbox"))
                            retJson[tempId] = input.checked;
                        else
                            retJson[tempId] = MeetingUtils.filterUnreadableCode(input.value);
                    }
                }
            }
        }
        var ele = typeof selector == "object" ? selector : document.getElementById(selector);
        if (ele) {
            _formData("input", ele, ret);
            _formData("textarea", ele, ret);
        }

        return ret;
    },
    /**
     * 封装后的调人组件
     * @param key  绑定事件的ID
     * @param valueKey  返回后的数据存放的ID
     * @param _opts  自定义参数
     */
    selectOrg : function(key, callBackFn, _opts){
    	var fillBackData = cmp.storage.get("m3_v5_meeting_selectOrg_bachCache_" + key, true);
    	if(fillBackData){
    		fillBackData = cmp.parseJSON(fillBackData);
    	}

    	var excludeData = cmp.storage.get("m3_v5_meeting_selectOrg_bachCache_except_" + key, true);
    	if(excludeData){
    		excludeData = cmp.parseJSON(excludeData);
    	}
    	
    	//未写注释的为固定值
    	var opts = {
    		type : 1,
    		flowType : 2,
    		permission : false,
    		directDepartment : false,
    		fillBackData : fillBackData,
    		excludeData : excludeData,
    		closeCallback : function(){
    			cmp.selectOrgDestory("selectOrg_" + key);
    		},
    		callback : function(result){
    			var fillBackData = new Array();
    			var r_value = "";
    			
    			var orgResult = cmp.parseJSON(result).orgResult;
    			
    			var tempHtml = "";
    			for(var i = 0 ; i < orgResult.length ; i++){
    				tempHtml += orgResult[i].name + ",";
    				
    				var data = {
    					id : orgResult[i].id,
    					name : orgResult[i].name,
    					type : orgResult[i].type
    				}
    				fillBackData.push(data);
    			}
    			tempHtml = tempHtml.substr(0, tempHtml.length -1);
    			_$("#"+key).value = tempHtml;
    			
    			//缓存将需要回填的数据
    			cmp.storage.save("m3_v5_meeting_selectOrg_bachCache_" + key, cmp.toJSON(fillBackData), true);
    			//缓存选人组件的后缀
    			var cacheKey = cmp.storage.get("m3_v5_meeting_selectOrg_bachCacheKey", true);
    			if(cacheKey){
    				cacheKey = cmp.parseJSON(cacheKey);
    				var existsKey = false
    				for(var i = 0 ; i < cacheKey.length ; i++){
    					if(cacheKey[i] == key){
    						existsKey = true;
    						break;
    					}
    				}
    				if(!existsKey){
    					cacheKey.push(key);
    				}
    			}else{
    				cacheKey = new Array();
    				cacheKey.push(key);
    			}
    			cmp.storage.save("m3_v5_meeting_selectOrg_bachCacheKey", cmp.toJSON(cacheKey), true);
    			
    			callBackFn(orgResult);
    			
    			//每次回调后销毁选人组件
    			cmp.selectOrgDestory("selectOrg_" + key);
    		}
    	};
        
        //其他输入框失去焦点
        _$("#meetingName") && _$("#meetingName").blur();
        _$("#content") && _$("#content").blur();

    	opts = MeetingUtils.extend(opts, _opts);
    	cmp.selectOrg("selectOrg_" + key, opts);
    },
    clearSelectOrgCache : function(){
    	var batchCacheKey = cmp.storage.get("m3_v5_meeting_selectOrg_bachCacheKey", true);
    	
    	if(!batchCacheKey){
    		return;
    	}
    	
    	batchCacheKey = cmp.parseJSON(batchCacheKey);
    	for(var cacheKey in batchCacheKey){
    		//清除回填数据
    		cmp.storage["delete"]("m3_v5_meeting_selectOrg_bachCache_" + batchCacheKey[cacheKey], true);
    		//清除排除在外的数据
    		cmp.storage["delete"]("m3_v5_meeting_selectOrg_bachCache_except_" + batchCacheKey[cacheKey], true);
    	}
    	//清除后缀缓存数据
    	cmp.storage["delete"]("m3_v5_meeting_selectOrg_bachCacheKey", true);
    },
    //合并两个数组，将第二个数组内容合并至第一个数组
    extend : function(arr1, arr2){
    	for(var key1 in arr1){
    		for(var key2 in arr2){
    			if(key1 == key2){
    				arr1[key1] = arr2[key2];
    				arr2[key2] = undefined;
    				break;
    			}
    		}
    	}
    	
    	for(var key2 in arr2){
    		if(typeof(arr2[key2]) != "undefined"){
    			arr1[key2] = arr2[key2];
    		}
    	}
    	
    	return arr1;
    },
    mergeArray:function (arr1, arr2){
    	if(typeof(arr1.length) == "undefined" || arr1.length == 0){
    		return arr2;
    	}
    	var newArr = arr1.concat();
    	for(var i = 0 ; i < arr2.length ; i++){
    		var hasSameData = false;
    		for(var j = 0 ; j < arr1.length ; j++){
    			if(arr1[j].id == arr2[i].id){
    				hasSameData = true;
    				break;
    			}
    		}
    		if(!hasSameData){
    			newArr.push(arr2[i]);
    		}
    	}
    	return newArr;
    },
    /**
     * 校验是否存在冲突
     * @param paramData
     * 		类型      名称             必填  备注
     * 		String   meetingId      N    会议Id
     * 		Long     beginDatetime  Y    会议开始时间
     * 		Long     endDatetime    Y    会议结束时间
     * 		String   emceeId        N    主持人ID(直接人员ID）
     * 		String   recorderId     N    记录人ID(直接人员ID）
     * 		String   conferees      Y    选择的会议参会人员ID（格式：Member|1212223,Member|1212223)
     *      String   errorMsg       Y    存在冲突后的提示语
     * @param successCallback
     * @param errorCallback
     */
    checkConfereesConflict : function(paramData, successCallback, errorCallback, fn_error){
    	//判断选择的会议参与人是否有会议冲突
        $s.Meeting.checkConfereesConflict({}, paramData, {
            success : function(result) {
                if (result["conflict"]) {
                    cmp.notification.confirm(paramData.errorMsg, function(e){ // e==1是/e==0 否
                        if(e==1){ // 是
                        	successCallback();
                        }else{
                        	errorCallback();
                        }
                    },null, [ cmp.i18n("meeting.meetingCommon.cancel"), cmp.i18n("meeting.meetingCommon.continue")]);
                } else {
                	successCallback();
                }
            },
            error : function(result){
            	fn_error();
            	//处理异常
            	MeetingUtils.dealError(result);
            }
        });
    },
    loadCache : function(key, isDel){
        var ret = null, storageObj;
        storageObj = cmp.storage.get(key, true);
        if(storageObj) {
            ret = cmp.parseJSON(storageObj);
            if(isDel === true){
                cmp.storage["delete"](key, true);
            }
        }
        return ret;
    },
    isNull : function(obj){
    	if(obj == null){
    		return true;
    	}
    	if(typeof(obj) == "undefined"){
    		return true;
    	}
    	if(obj == ""){
    		return true;
    	}
    	return false;
    },
    isNotNull : function(obj){
    	return !this.isNull(obj);
    },
    //处理ajax异常
    dealError : function(obj, errmsg, completeCallback){
    	var cmpHandled = cmp.errorHandler(obj);
    	if(!cmpHandled){
    		if(errmsg){
    			_alert(errmsg, completeCallback);
    		}else{
    			_alert(cmp.i18n("meeting.exception.reqException"));
    		}
    	}
    },
    // 查看Value是否在array中
    isInArray : function(array, value) {
        var ret = false;
        if (array && array.length > 0) {
            for (var i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    },
    isFromM3NavBar : function(){
    	var isFromM3NavBar = cmp.storage.get("isFromM3NavBar", true);
    	if(!isFromM3NavBar){
    		var matchM3 = window.location.href.match('m3from=navbar');
    		cmp.storage.save("isFromM3NavBar", matchM3 == "m3from=navbar", true);
    	}
    	isFromM3NavBar = cmp.storage.get("isFromM3NavBar", true);
    	if(isFromM3NavBar == "false"){
    		return false;
    	}else{
    		return true;
    	}
    },
    isFromQrCode : function(){
    	var isFromQrCode = cmp.storage.get("isFromQrCode",true);
    	return !!isFromQrCode;
    },
    /**
	 * 获取url参数
	 * @param {Object} name
	 */
	getQueryString : function(name) { 
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"); 
        var r = window.location.search.substr(1).match(reg); 
        if (r != null) return unescape(r[2]); 
        return null; 
    },
    /**
     * 日期转换
     * @param sec
     * @param pattern
     */
    formatDate : function (sec,pattern) {
        return new Date(parseInt(sec)).format(pattern);
    },
    /**
     * 添加会议多webview事件监听
     * @param {*} handler 
     */
    addMeetingWebviewEvent : function(handler){
        cmp.href.addWebviewListener("meeting_webview_event",handler);
    },
    /**
     * 添加会议删除多webview事件监听
     * @param {*} handler 
     */
    addMeetingDeleteWebviewEvent : function(handler){
        cmp.href.addWebviewListener("meeting_delete_webview_event",handler);
    },
    /**
     * 触发会议多webview事件
     * @param {*} options 
     */
    fireAllWebviewEvent : function(options){
        //触发会议自身事件
        cmp.href.notify("meeting_webview_event",{
            isRefresh : true
        });

        if(options && options.type === "delete"){
			//触发修改任务进度事件
			cmp.href.notify("meeting_delete_webview_event",{
				isRefresh : true,
				data : options.data
			});
        }
        
        //触发从待办打开的事件
        cmp.webViewListener.fire({ 
            type: 'com.seeyon.m3.ListRefresh', 
            data: {
                type: "update",
                appId: "6"
            }
        });
        //触发平台事件，用于刷新列表数据
        cmp.webViewListener.fire({
            type: "com.seeyon.m3.ListRefresh",
            data: {type: 'update'}
        });
        //触发底导航打开日程的webview事件
        cmp.href.notify("calendar_webview_event",{
            isRefresh : true
        });
    },
    meetingSecurity : function(callback){
        $s.Meeting.meetingUserPeivMenu({}, {
            success : function(result) {
                var security = {
                    haveMeetingDoneRole : result.haveMeetingDoneRole,
                    haveMeetingPendingRole : result.haveMeetingPendingRole,
                    haveMeetingArrangeRole : result.haveMeetingArrangeRole,
                    haveMeetingRoomApp : result.haveMeetingRoomApp,
                    haveMeetingRoomPerm : result.haveMeetingRoomPerm,
                    isMeetingRoomAdminRole : result.data
                };
                callback(security);
            },
            error : function(result){
                MeetingUtils.dealError(result);
            }
        });
    }
}
/**
 * 设置跳转回协同首页listView刷新
 */
function setListViewRefresh(isRefresh){
	cmp.storage.save("isListViewRefresh",isRefresh,true);
}

/**
 * 判断listView是否刷新
 */
function isListViewRefresh(){
	var isRefresh = cmp.storage.get("isListViewRefresh",true);
	var ret = true;
	if(isRefresh == "false"){
	    ret = false;
	}
	if(isRefresh){
	    cmp.storage["delete"]("isListViewRefresh",true);
	}
	return ret;
}

/**
 * 动态设置国际化标题
 */
function setPageTitle(title){
	document.title = title;
}

/**
 * 获取小程序token
 */
function getMiniProgramToken(){
	var thirdSessionId = window.localStorage.getItem("thirdSessionId");
    thirdSessionId = thirdSessionId != undefined ? thirdSessionId : '';
    var mpToken = window.localStorage.getItem("mpToken");
    mpToken = mpToken != undefined ? mpToken : '';
    return {
    	thirdSessionId : encodeURIComponent(thirdSessionId),
    	mpToken : mpToken
    }
}

/**
 * 缓加载工具类
 */
var LazyUtil = (function(){
    
  //缓加载机制
    var lazyStack = {};
    function LazyTool(){}
    LazyTool.prototype.addLazyStack = function(item){
        
        /**
         * item.code 字符串 | 堆栈标识
         * item.depend 字符串 | 依赖的js
         * item.dependModel strong/强关联，必须等父任务执行完成后在进行加载  weak/若关联，加载顺序没关系（默认值）
         * item.css 数组 | css数组
         * item.js  数组 | js数组
         * 
         */
        if(item.code){
            var i = lazyStack[item.code];
            if(!i){
                item.loaded = false;
                item.isLoading = false;
                lazyStack[item.code] = item;
            }else{
                console.warn("重复设置懒加载, code=" + item.code);
            }
        }else{
            alert(cmp.i18n("meeting.exception.setException"));
        }
    };
    
  //启动懒加载
    LazyTool.prototype.startLazy = function(groups){
        
        for(var k in lazyStack){
            
            var thisI = lazyStack[k], _this = this;
            if(thisI.loaded || thisI.isLoading){
                continue;
            }
            //按组加载
            if(groups && thisI.groups != groups){
                continue;
            }
            thisI.isLoading = true;
            
            function loadThis(i){
                if(i.css && i.css.length > 0){
                    cmp.asyncLoad.css(i.css);
                }
                if(i.js && i.js.length > 0){
                    //console.log("开始加载:" + i.js);
                    cmp.asyncLoad.js(i.js, function(){
                        //console.log("完成加载:" + i.js);
                        _this._onJSLoad(i);
                    });
                }else{
                    _this._onJSLoad(i);
                }
            }
            
            if(thisI.depend && thisI.dependModel === "strong"){
                (function(child){
                    _this.addLoadedFn(child.depend, function(){
                        loadThis(child);
                    });
                })(thisI);
            }else{
                loadThis(thisI);
            }
        }
    };
    
    /**
     * js加载完成后执行脚本
     */
    LazyTool.prototype._onJSLoad = function(i){
        var _this = this;
        i.loaded = true;
        if(i.functions && i.functions.length > 0){
            var checkRet = _this.isLoadChain(i.code);
            if(checkRet.finish){
                for(var j = 0; j < i.functions.length; j++){
                    i.functions[j]();
                }
                i.functions = [];
            }else{
                //事件转移
                for(var j = 0; j < i.functions.length; j++){
                    //console.log("事件转移:" + i.code + " to " + checkRet.code);
                    _this.addLoadedFn(checkRet.code, i.functions[j]);
                }
                i.functions = [];
            }
        }
    }
    
  //加载脚本加载完成后
    LazyTool.prototype.addLoadedFn = function(code, fn){
        //console.log("接收转移事件:code=" + code + " fn=" + fn);
        var i = lazyStack[code], _this = this;
        if(i){
            var checkRet = _this.isLoadChain(i.code);
            if(checkRet.finish){
                fn();
            }else{
                if(checkRet.code == i.code){
                    i.functions = i.functions || [];
                    i.functions.push(fn);
                }else{
                    //转移
                    _this.addLoadedFn(checkRet.code, fn);
                }
            }
        }else{
            fn();
        }
    };
    
  //校验依赖路径是否加载完成
    LazyTool.prototype.isLoadChain = function(code){
        
        if(!code){
            return {
                "finish" : true
            };
        }else{
            var i = lazyStack[code];
            if(!i.loaded){
                return {
                    "finish" : false,
                    "code" : code
                };
            }else{
                return this.isLoadChain(i.depend);
            }
        }
    };
    
    return new LazyTool();
})();
//会议用编译打版时间
var meetingBuildVersion =  "";
var meetingBuildVersion_and =  "";
