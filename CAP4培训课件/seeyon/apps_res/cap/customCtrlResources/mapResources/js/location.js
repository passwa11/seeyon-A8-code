(function (factory) {
    var nameSpace = 'field_4793689415239855349';
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
            dynamicLoading.js("http://api.map.baidu.com/getscript?v=2.0&ak=59xkhaYrPrauvvubXjGWZKqGiZi5amKP&services=&t=20190123111209");
            self.appendChildDom();
            //dynamicLoading.js("http://api.map.baidu.com/api?v=2.0&ak=59xkhaYrPrauvvubXjGWZKqGiZi5amKP");
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
            var showHTML = '';
            var display = self.messageObj.display.escapeHTML();
            if (display != "") {
            	showHTML += '<section class="cap4-people is-one ">'
            		+'<div class="cap4-people__left">'+display+'</div>'
            		+'<div class="cap4-people__right">';
            } else {
            	showHTML += '<div >';
            }
            showHTML += '<div class="cap4-people__cnt" id="editDiv_'+self.privateId+'" style="display:block;">'
            	+'<div class="cap4-people__text" id="show_'+self.privateId+'">'+self.messageObj.showValue+'</div>'
            	+'<div class="cap4-people__picker" id="click_'+self.privateId+'"';
            
            showHTML += '><img src="'+ window.top._ctxPath +'/skin/dist/images/location.png"></div></div>';
            showHTML +="<div id='allmap'></div>";
            showHTML += "</div></section>";
            /*var div = document.getElementById(privateId);
            div.innerHTML = showHTML;*/
            
            document.querySelector('#' + self.privateId).innerHTML = showHTML;
            document.querySelector('#click_'+ self.privateId).addEventListener('click', function() {
        		  self.location(self.privateId, self.messageObj, self.adaptation);
    	  	});
        },
        location: function (privateId, messageObj, adaptation) {
        	/*var proce = $.progressBar({
        	    text: "正在定位中...."
        	});*/
        	/*var map = new BMap.Map("allmap");        //在container容器中创建一个地图,参数container为div的id属性;
        	 
            var point = new BMap.Point(116.331398,39.897445);    //创建点坐标
            map.centerAndZoom(point, 12);                //初始化地图，设置中心点坐标和地图级别
        	var geolocation = new BMap.Geolocation();
        	geolocation.enableSDKLocation();
        	geolocation.getCurrentPosition(function(r){
        		if(this.getStatus() == BMAP_STATUS_SUCCESS){
        			console.log(r);
        			var mk = new BMap.Marker(r.point);
        			map.addOverlay(mk);
        			map.panTo(r.point);
        			var address = r.address.province + r.address.city + r.address.street;
			    	//保存数据
			    	messageObj = adaptation.childrenGetData(privateId);
			    	messageObj.showValue = address;
			    	messageObj.value = address;
			    	messageObj.valueId = address;
			    	adaptation.childrenSetData(messageObj, privateId);
        			proce.close();
        		}
        		else {
        			proce.close();
        			alert('failed'+this.getStatus());
        		}        
        	},{enableHighAccuracy: true})*/
        	var dialog = $.dialog({
                id: 'dialog',
                url: this.preUrl + '/html/baidumap.html',
                width: 1050,
                height: 620,
                title: '百度地图',
                type : 'panel',
                transParams : {oldPlace : messageObj.value},
                checkMax:true,
            closeParam:{
                'show':false,
                autoClose:false,
                handler:function(){
                }
            },
            buttons: [{
                text: "定位",
                handler: function () {
	                var address = dialog.getReturnValue();
	                messageObj = adaptation.childrenGetData(privateId);
			    	messageObj.showValue = address;
			    	messageObj.value = address;
			    	messageObj.valueId = address;
			    	adaptation.childrenSetData(messageObj, privateId);
	                dialog.close()
                }
             }, {
                text: "取消",
                handler: function () {
	                dialog.close()
                }
                }]
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