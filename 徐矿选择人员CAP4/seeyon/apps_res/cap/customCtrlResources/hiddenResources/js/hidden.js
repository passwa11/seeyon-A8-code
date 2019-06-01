(function (factory) {
    var nameSpace = 'field_6571213415994281300';
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
            // dynamicLoading.css(self.preUrl + 'css/zlc.css');
            // dynamicLoading.js(self.preUrl + 'js/JsBarcode.all.min.js');
            // dynamicLoading.js(self.preUrl + 'js/layui-people.js');
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
            console.log("----------------");
            console.log(self.messageObj);

            var showHTML = '';
            var display = self.messageObj.display.escapeHTML();
            showHTML += '<section class="cap4-people is-one " style="display: none">'
                + '<div class="">'
                + '<div class="cap4-people__cnt" id="editDiv_' + self.privateId + '" style="display:block;">'
                + '<div class="cap4-people__text" id="show_' + self.privateId + '">'+self.messageObj.showValue+'</div>'
                + '</div></div></section>';
            document.querySelector('#' + self.privateId).innerHTML = showHTML;
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