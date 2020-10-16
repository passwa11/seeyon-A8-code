(function (factory) {
    var nameSpace = 'field_4793655815239855349';
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
    }

    App.prototype = {
        initParams: function (options) {
            var self = this;
            self.adaptation = options.adaptation;
            self.privateId = options.privateId;
            self.messageObj = options.getData;
            self.preUrl = options.url_prefix;
        },
        initDom: function () {
            var self = this;
            dynamicLoading.css(self.preUrl + 'css/printBtn.css');
            dynamicLoading.mediaCss(self.preUrl + 'css/print.css?var=2018100722');
            dynamicLoading.js(self.preUrl + 'js/jquery.jqprint-0.3.js');
            dynamicLoading.js(self.preUrl + 'js/JsBarcode.all.min.js');
            self.appendChildDom();

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
            var domStructure = '<section class="customButton_box_content">' +
                '<div class="customButton_class_box ' + self.privateId + '" title="' + self.messageObj.display.escapeHTML() + '">' + self.messageObj.display.escapeHTML() + '</div>' +
                '</section>';
            document.querySelector('#' + self.privateId).innerHTML = domStructure;

            document.querySelector('.' + self.privateId).addEventListener('click', function () {
                self.print(self.privateId, self.messageObj, self.adaptation);
            });
        },
        print: function (privateId, messageObj, adaptation) {
            var content = messageObj.formdata.content;
            $.ajax({
                type: 'get',
                async: true,
                url: (_ctxPath ? _ctxPath : '/seeyon') + "/rest/cap4/customFieldCtrl/batchPrint",
                dataType: 'json',
                data: {
                    //'formId': content.contentTemplateId,
                    'masterId': content.contentDataId,
                    //'fieldName': messageObj.id,
                    'subId': messageObj.recordId	// 增加此参数的传递，重复表控件才会有
                },
                contentType: 'application/json',
                beforeSend: function () {

                },
                success: function (res) {
                    if (res.code != 0) {
                        $.alert(res.message);
                        return;
                    }
                    var backfill = {};
                    var barcode;
                    var array = res.data.subs;
                    var barcodehtml = "";
                    if (null != array && array.length > 0) {
                        for (var i = 0; i < array.length; i++) {
                            var arr = array[i];
                            // 回填重复表
                        	backfill.tableName = arr.tbName;
                        	backfill.tableCategory = "formson";
                        	backfill.updateData = arr.data;
                        	backfill.updateRecordId = arr.recordId;
                        	adaptation.backfillFormControlData(backfill, privateId);
                            barcode = arr.barcode;
                            barcodehtml += '    <p id="name">' + barcode.name + '</p>\n' +
                                '    <p id="price">￥:' + barcode.price + '</p>\n' +
                                '    <p id="dept">' + barcode.dept + '</p>\n' +
                                '    <img id="imgcode' + i + '" />';

                        }
                        
                        var html = '';
                        var id = "temB";
                            //打印物料条码
                        html += '<div id="' + id + '">' + barcodehtml + '</div>';
                        
                        $("body").append(html);
                        
                        if (null != array && array.length > 0) {
                            for (var i = 0; i < array.length; i++) {
                                var arr = array[i];
                                barcode = arr.barcode;
                                $("#imgcode" + i).JsBarcode(barcode.code, {
                                    //format: "CODE39",//选择要使用的条形码类型
                                    width: 1,//设置条之间的宽度
                                    height: 60,//高度
                                    displayValue: true,//是否在条形码下方显示文字
                                    text: barcode.code,//覆盖显示的文本
                                    fontOptions: "bold",//使文字加粗体或变斜体
                                    font: "Microsoft YaHei",//设置文本的字体
                                    textAlign: "center",//设置文本的水平对齐方式
                                    textPosition: "bottom",//设置文本的垂直位置
                                    textMargin: 5,//设置条形码和文本之间的间距
                                    fontSize: 15,//设置文本的大小
                                    background: "#FFF",//设置条形码的背景
                                    lineColor: "#000",//设置条和文本的颜色。
                                    margin: 1//设置条形码周围的空白边距
                                });
                            }
                        }
                        
                        $("#" + id).jqprint();
                        $("#" + id).css("display", "none");
                        $("#" + id).remove();
                    }
                }
            });
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