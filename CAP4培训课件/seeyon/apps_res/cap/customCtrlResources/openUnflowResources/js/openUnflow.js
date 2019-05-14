(function(factory){
    var nameSpace = 'field_4793655815239859651';
    if(!window[nameSpace]){
        var Builder = factory();
        window[nameSpace] = {
            instance: {}
        };
        window[nameSpace].init = function (options) {
            window[nameSpace].instance[options.privateId] = new Builder(options);
        };
        window[nameSpace].isNotNull = function (obj) {
            return true;
        };
    }
})(function(){
    /**
	 * 构造函数
     * @param options
     * @constructor
     */
	function App(options) {
		var self = this;
		//初始化参数
		self.initParams(options);
		//初始化dom
		self.initDom();
		//事件
		self.events();
    }
    
    App.prototype = {
		initParams : function (options) {
			var self = this;
            self.adaptation = options.adaptation;
            self.privateId = options.privateId;
            self.messageObj = options.getData;
            self.preUrl = options.url_prefix;
        },
		initDom : function () {
			var self = this;
            dynamicLoading.css(self.preUrl + 'css/formQueryBtn.css');
            self.appendChildDom();
        },
		events : function () {
			var self = this;
            // 监听是否数据刷新
            self.adaptation.ObserverEvent.listen('Event' + self.privateId, function() {
                self.messageObj = self.adaptation.childrenGetData(self.privateId);
                self.appendChildDom();
            });
        },
        appendChildDom : function () {
			var self = this;
            var domStructure = '<section class="customButton_box_content">'+
                '<div class="customButton_class_box '+ self.privateId + '" title="' + self.messageObj.display.escapeHTML() + '">'+ self.messageObj.display.escapeHTML() +'</div>'+
                '</section>';
            document.querySelector('#' + self.privateId).innerHTML = domStructure;
            var jumpFun = function() {
                if (self.messageObj.auth === 'hide' || !self.messageObj.customFieldInfo.customParam) {
                    return;
                }
                if(!self.messageObj.customFieldInfo.customParam.templateId){
                    top.$.alert('没有配置查询统计模版');
                    return;
                }
                
                var unflowId = self.messageObj.customFieldInfo.customParam.templateId.id;
                var unflowFormId = self.messageObj.customFieldInfo.customParam.templateId.formId;
                var pagename = self.messageObj.customFieldInfo.customParam.templateId.name;

                var content = self.messageObj.formdata.content;
                //防止预览状态下点击报错
                if(!content)
                    return;
                var url = "http://127.0.0.1/seeyon/cap4/businessTemplateController.do?method=capUnflowList&srcFrom=bizconfig&businessId=-3367226160953274461&moduleId=" + unflowId + "&formId=" + unflowFormId + "&type=baseInfo&tag=1550817477709&portalId=1&_resourceCode=null";
            	//var url = window.top._ctxPath + '/cap4/businessTemplateController.do?method=capUnflowList&srcFrom=bizconfig&businessId='+pagename.toUpperCase()+'&designId='+ appId + (window.top.CsrfGuard.getUrlSurffix() ? window.top.CsrfGuard.getUrlSurffix() : '');
                var openPageCreate = window.open(url, '_blank', self.privateId, 'width='+(window.screen.availWidth-10)+',height='+(window.screen.availHeight-70)+ ',top=5,left=35,toolbar=no,menubar=no,scrollbars=no, resizable=no,location=no, status=no');
                openPageCreate.moveTo(5,35);
                openPageCreate.resizeTo(window.screen.availWidth - 10, window.screen.availHeight - 70);
            };
            document.querySelector('.' + self.privateId).removeEventListener('click', jumpFun);
            document.querySelector('.' + self.privateId).addEventListener('click', jumpFun);
            //渲染隐藏权限
            if (self.messageObj.auth === 'hide') {
                document.querySelector('#' + self.privateId).innerHTML = '<div class="cap4-text__browse" style="line-height: 1.8; color: rgb(0, 0, 0) !important;">***</div>';
            }
        }
	};

    var dynamicLoading = {
        css: function(path) {
            if(!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var link = document.createElement('link');
            link.href = path;
            link.rel = 'stylesheet';
            link.type = 'text/css';
            head.appendChild(link);
        },
        js: function(path) {
            if(!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');
            script.src = path;
            script.type = 'text/javascript';
            head.appendChild(script);
        }
    }

    return App;
});