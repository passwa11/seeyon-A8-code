(function (factory) {
    var nameSpace = 'field_5193746555244385517';
    if (!window[nameSpace]) {
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
})(function () {
    /**
     * 构造函数
     *
     * @param options
     * @constructor
     */
    function App(options) {

        var self = this;
        // 初始化参数
        self.initParams(options);
        // 初始化dom
        self.initDom();
        // 事件
        self.events();
        
        function scan() {
            $("textarea").bind("keydown", function (e) {
                // 兼容FF和IE和Opera
            	// var e= event ? event || window.event;
                var theEvent = e || window.event;
                var code = theEvent.keyCode || theEvent.which || theEvent.charCode;
                if (code == 13) {
                    //回车执行查询
                	if (e.currentTarget.value && e.currentTarget.value.length == 13) {
                		var messageObj = self.messageObj;
                		var adaptation = self.adaptation;
                		var privateId = self.privateId;
                		var value = e.currentTarget.value;
                		var content = messageObj.formdata.content;
                		addLineAndFilldata(content, adaptation, messageObj, privateId, value, e);
                    } else {
                    	$.alert("请检查条码是否13位，并且删除多余的空格!");
                    }
                }
            });
        }
        
        function addLineAndFilldata(content, adaptation, messageObj, privateId, value, e) {
        	$.ajax({
                type: 'get',
                async: true,
                url: (_ctxPath ? _ctxPath : '/seeyon') + "/rest/cap4/customFieldCtrl/scan",
                dataType: 'json',
                data: {
                    'formId': content.contentTemplateId,
                    'masterId': content.contentDataId,
                    'value': value
                },
                contentType: 'application/json',
                success: function (res) {
                	/*var process = top.$.progressBar({
                        text: "正在解析条码，请等待..."
                    });*/
                	// 判断是否需要添加
                	if(res.code != 0) {
                		$.alert(res.message);
                		return;
                	}
                	var add = res.data.add;
                	if(add) {
                		var api = window.thirdPartyFormAPI;
                		var addLineParam = {};
                		addLineParam.tableName = res.data.subTbName;
                		addLineParam.isFormRecords = true;
                		addLineParam.callbackFn = function() {
                			addLineAndFilldata(content, adaptation, messageObj, privateId, value, e);
                		}
                		api.insertFormsonRecords(addLineParam);
                	} else {
                		var backfill = {};
                		var array = res.data.subs;
                		for (var i = 0; i < array.length; i++) {
                			var arr = array[i];
                			// 回填重复表
                			backfill.tableName = arr.tbName;
                			backfill.tableCategory = "formson";
                			backfill.updateData = arr.data;
                			backfill.updateRecordId = arr.recordId;
                			adaptation.backfillFormControlData(backfill, privateId);
                		}
                		
                		// 清空扫码区域,回填主表的其他信息
                		var codeArea = {};
                		codeArea.tableName = adaptation.formMessage.tableName;
                		codeArea.tableCategory = adaptation.formMessage.tableCategory;
                		//codeArea.updateData = res.data.code;
                		codeArea.updateData = res.data.maindata;
                		adaptation.backfillFormControlData(codeArea, privateId);
                		// 结束
                		stopDefaultKey(e);
                	}
                }
            });
        }
        
        //清空textarea回车
        function stopDefaultKey(e) {
            if (e && e.preventDefault) {
                e.preventDefault();
            } else {
                window.event.returnValue = false;
            }
            return false;
        }

        //监听表单扫码事件
        function listenScan() {
        	scan();
        }
        listenScan();
    }

    App.prototype = {
        initParams: function (options) {
            var self = this;
            self.adaptation = options.adaptation;
            self.adaptation.formMessage = options.formMessage;
            self.privateId = options.privateId;
            self.messageObj = options.getData;
            self.preUrl = options.url_prefix;
        },
        initDom: function () {
            var self = this;
            dynamicLoading.css(self.preUrl + 'css/scanBarcode.css');
            self.appendChildDom();
            self.listenScanInput();
        },
        listenScanInput: function () {

        },
        events: function () {
            var self = this;
            // 监听是否数据刷新
            self.adaptation.ObserverEvent.listen('Event' + self.privateId, function () {
                self.messageObj = self.adaptation.childrenGetData(self.privateId);
                self.appendChildDom();
            });
        },
        appendChildDom: function () {
            var self = this;
            var domStructure = '<section class="customButton_box_content"  style="display: none">' +
                '<div class="customButton_class_box ' + self.privateId + '" title="' + self.messageObj.display.escapeHTML() + '">' + self.messageObj.display.escapeHTML() + '</div>' +
                '</section>';
            document.querySelector('#' + self.privateId).innerHTML = domStructure;

            document.querySelector('.' + self.privateId).addEventListener('click', function () {
                self.scanLineFeed(self.privateId, self.messageObj, self.adaptation);
            });
        },
        scanLineFeed: function (privateId, messageObj, adaptation) {

        }
    };

    var dynamicLoading = {
        css: function (path) {
            if (!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var link = document.createElement('link');
            link.href = path;
            link.rel = 'stylesheet';
            link.type = 'text/css';
            head.appendChild(link);
        },
        mediaCss: function (path) {
            if (!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var link = document.createElement('link');
            link.href = path;
            link.rel = 'stylesheet';
            link.type = 'text/css';
            link.media = 'print';
            head.appendChild(link);
        },
        js: function (path) {
            if (!path || path.length === 0) {
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