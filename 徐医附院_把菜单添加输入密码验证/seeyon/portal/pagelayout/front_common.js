"use strict";
/*--公共变量--*/
var _ctxPath = "/seeyon";
//多窗口缓存
var _windowsMap = new Properties();
//主页面刷新控制变量
vPortal.pageLoad = true;
var isOffice = false;
var officeObj = null;
//echarts主题，栏目也可以根据此变量来判断栏目是否加载echarts需要的主题js
var echart_theme;
/**jquery-json（摘至seeyon/common/js/jquery.json-debug.js）：begin**/
var preLoginBtn = null;

var LogoutFlag = false; //标识掉线
var CsrfGuard = {
    getToken: function () {
        var token = typeof (CSRFTOKEN) === 'undefined' ? getCtpTop().CSRFTOKEN : CSRFTOKEN;
        return typeof (token) === 'undefined' || token === 'null' ? '' : token;
    },
    isEnabled: function () {
        return this.getToken() !== '';
    },
    getUrlSurffix: function (url) {
        if (typeof (url) !== 'undefined' && url.indexOf('CSRFTOKEN=') > 0) {
            return '';
        }

        function getPrefix(url) {
            var prefix = '&';
            if (typeof (url) !== 'undefined') {
                if (url.indexOf('?') < 0) {
                    prefix = '?';
                }
            }
            return prefix;
        }

        return !this.isEnabled() ? '' : getPrefix(url) + 'CSRFTOKEN=' + this.getToken();
    },
    beforeAjaxSend: function (request) {
        var token_value = CsrfGuard.getToken();
        if (token_value !== '') {
            request.setRequestHeader("X-Requested-With", "XMLHttpRequest");
            request.setRequestHeader('CSRFTOKEN', token_value);
        }
    }
};
/**
 * author liaojl
 * 此方法同时存在于front_common.js、v3x.js、v3x-debug.js，分别供首页、老模块（公文、会议、文档中心等）、新模块使用，如要修改，请同步更新
 * thisWindowZindex 默认值从600开始，页面元素的z-index应该控制在600以下
 * 根据现有的最高z-index层级计算出新的值
 * 不传值或传入1，默认增长1位
 * 传入的值>1 则按照传入的值留出空位，并返回原来的值+1
 *
 **/
var thisWindowZindex = 600;
var getMaxZindex = function (increaseNum) {
    if (typeof (increaseNum) != "undefined") {
        increaseNum = Number(increaseNum);
        if (isNaN(Number(increaseNum))) $.alert("Please check the incoming z-index value!");
    }
    if (typeof (increaseNum) == "undefined" || increaseNum == 1) {
        thisWindowZindex = thisWindowZindex + 1;
        return thisWindowZindex;
    } else {
        thisWindowZindex = thisWindowZindex + increaseNum;
        return (thisWindowZindex - increaseNum + 1);
    }
}

var m = {
        '\b': '\\b',
        '\t': '\\t',
        '\n': '\\n',
        '\f': '\\f',
        '\r': '\\r',
        '"': '\\"',
        '\\': '\\\\'
    },
    s = {
        'array': function (x) {
            var a = ['['],
                b, f, i, l = x.length,
                v;
            for (var i = 0; i < l; i += 1) {
                v = x[i];
                if (v === undefined) {
                    v = null;
                }
                f = s[typeof v];
                if (f) {
                    v = f(v);
                    if (typeof v == 'string') {
                        if (b) {
                            a[a.length] = ',';
                        }
                        a[a.length] = v;
                        b = true;
                    }
                }
            }
            a[a.length] = ']';
            return a.join('');
        },
        'date': function (x) {
            return s.string(x.dateFormat('Y-m-d'));
        },
        'boolean': function (x) {
            return String(x);
        },
        'null': function (x) {
            return "null";
        },
        'number': function (x) {
            return isFinite(x) ? String(x) : 'null';
        },
        'object': function (x) {
            if (!(typeof (x) === 'object')) return 'null';
            if (x) {
                if ($.isArray(x)) {
                    return s.array(x);
                } else if (x instanceof Date) {
                    return s.date(x);
                }
                var a = ['{'],
                    b, f, i, v;
                for (var i in x) {
                    v = x[i];
                    f = s[typeof v];
                    if (f) {
                        v = f(v);
                        if (typeof v == 'string') {
                            if (b) {
                                a[a.length] = ',';
                            }
                            a.push(s.string(i), ':', v);
                            b = true;
                        }
                    }
                }
                a[a.length] = '}';
                return a.join('');
            }
            return 'null';
        },
        'string': function (x) {
            if (/["\\\x00-\x1f]/.test(x)) {
                x = x.replace(/([\x00-\x1f\\"])/g, function (a, b) {
                    var c = m[b];
                    if (c) {
                        return c;
                    }
                    c = b.charCodeAt();
                    return '\\u00' + Math.floor(c / 16).toString(16) + (c % 16).toString(16);
                });
            }
            return '"' + x + '"';
        },
        'function': function (x) {
            return x.toString().match(/function\s+([^\s\(]+)/)[1];
        }
    };

jQuery.toJSON = function (v) {
    var f;
    if (isNaN(v)) {
        f = s[typeof v];
    } else {
        f = ($.isArray(v)) ? s["array"] : s["number"];
    }
    if (f) return f(v);
};

jQuery.parseJSON = function (v, safe) {
    if (safe === undefined) safe = jQuery.parseJSON.safe;
    if (safe && !/^("(\\.|[^"\\\n\r])*?"|[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t])+?$/.test(v))
        return undefined;
    return eval('(' + v + ')');
};

jQuery.parseJSON.safe = false;
/**jquery-json（摘至seeyon/common/js/jquery.json-debug.js）：end**/


/**ajax组件,摘至 /seeyon/common/js/misc/jsonGateway-debug.js、Moo-debug.js：begin**/
var Class = function (properties) {
    var klass = function () {
        if (this.initialize && arguments[0] != 'noinit') return this.initialize.apply(this, arguments);
        else return this;
    };
    for (var property in this) klass[property] = this[property];
    klass.prototype = properties;
    return klass;
};

Class.empty = function () {
};

Class.prototype = {
    extend: function (properties) {
        var pr0t0typ3 = new this('noinit');

        var parentize = function (previous, current) {
            if (!previous.apply || !current.apply) return false;
            return function () {
                this.parent = previous;
                return current.apply(this, arguments);
            };
        };

        for (var property in properties) {
            var previous = pr0t0typ3[property];
            var current = properties[property];
            if (previous && previous != current) current = parentize(previous, current) || current;
            pr0t0typ3[property] = current;
        }
        return new Class(pr0t0typ3);
    },

    implement: function (properties) {
        for (var property in properties) this.prototype[property] = properties[property];
    }

};

Object.extend = function () {
    var args = arguments;
    args = (args[1]) ? [args[0], args[1]] : [this, args[0]];
    for (var property in args[1]) args[0][property] = args[1][property];
    return args[0];
};

Object.Native = function () {
    for (var i = 0; i < arguments.length; i++) arguments[i].extend = Class.prototype.implement;
};

new Object.Native(Function, Array, String, Number, Class);

var CallerResponder = new Class({
    debug: false,
    context: new Object(),
    error: function (request, settings, e) {
        if (request.status == 500) {
            var jsonError = jQuery.parseJSON(request.responseText);
        }
        if (this.debug) {
            alert("ajax error: " + request.responseText);
            alert(e);
        }
    },
    complete: function (res, status) {
        if (this.debug) {
            alert("ajax complete");
        }
    },
    beforeSend: function (xml) {
        if (this.debug) {
            alert("ajax beforeSend:" + xml);
        }
    }
});
var ajaxCallFunc = function (args, bsMethod) {
    var url = typeof (this.m) === 'undefined' ? this.jsonGateway : '/seeyon/ajax.do?method=ajaxAction&managerName=' + this.m;
    return ajaxCallFuncInner(url, bsMethod, args);
}

function ajaxCallFuncInner(url, bsMethod, args) {
    var callbackOption = null;
    if (args.length >= 1) {
        var tmpArg_2 = args[args.length - 1];
        if (tmpArg_2 != null && typeof (tmpArg_2.success) != "undefined" && $.isFunction(tmpArg_2.success)) {
            callbackOption = tmpArg_2;
            Array.prototype.splice.apply(args, [args.length - 1, 1]);
        }
    }

    var newArgs = new Array();
    for (var i = 0; i < args.length; i++) {
        newArgs[i] = args[i];
        // If this param object is invalid, hault this ajax
        if ($._isInValid(newArgs[i]))
            return null;
    }

    var data = new Object();
    data.managerMethod = bsMethod;
    data.arguments = $.toJSON(newArgs);

    var _async = true;
    var result = null;
    if (callbackOption && callbackOption.success) {
        _async = true;
        callbackOption = $.extend(new CallerResponder(), callbackOption);
    } else {
        _async = false;
        callbackOption = new CallerResponder();
        callbackOption.success = function (jsonObj) {
            if (typeof jsonObj === 'string') {
                if (jsonObj == '__LOGOUT') {
                    offlineFun();
                    return;
                }
                try {

                    /*            if ( useNativeJSONParser ){
                                  result = JSON.parse(jsonObj);
                              }else{*/
                    result = $.parseJSON(jsonObj);
                    //          }
                    //非json格式的数字串会错误解析
                    if (typeof result === 'number') {
                        result = jsonObj;
                    }
                } catch (e) {
                    result = jsonObj;
                }
            } else
                result = jsonObj;
        }
    }
    jQuery.ajax({
        type: "POST",
        url: url + '&rnd=' + parseInt(Math.random() * 100000),
        data: data,
        dataType: "json",
        beforeSend: CsrfGuard.beforeAjaxSend,
        async: _async,
        success: callbackOption.success,
        error: callbackOption.error,
        complete: callbackOption.complete
    });
    return result;
}

var RemoteJsonService = new Class({
    jsonGateway: "/json/",
    async: true,
    ajaxCall: ajaxCallFunc,
    c: ajaxCallFunc
});
var RJS = RemoteJsonService;

function callBackendMethod(managerName, methodName) {
    var url = '/seeyon/ajax.do?method=ajaxAction&managerName=' + managerName;
    return ajaxCallFuncInner(url, methodName, Array.prototype.slice.call(arguments, 2));
}

/**ajax组件,摘至 /seeyon/common/js/misc/jsonGateway-debug.js、Moo-debug.js：end**/


/***************************************************  AJAX ******************************************************/

var AJAX_XMLHttpRequest_DEFAULT_METHOD = "POST";

var AJAX_XMLHttpRequest_DEFAULT_async = true; //默认异步

var AJAX_RESPONSE_XML_TAG_BEAN = "B";
var AJAX_RESPONSE_XML_TAG_LIST = "L";
var AJAX_RESPONSE_XML_TAG_Value = "V";
var AJAX_RESPONSE_XML_TAG_Property = "P";
var AJAX_RESPONSE_XML_TAG_Name = "n";


/**
 * AJAX Service Parameter
 */
function AjaxParameter() {
    this.instance = [];
};

AjaxParameter.prototype.put = function (index, type, value) {
    var isArray = type.indexOf("[]") > -1;

    this.instance[this.instance.length] = {
        index: index,
        type: isArray ? type.substring(0, type.length - 2) : type,
        value: value,
        isArray: isArray
    };
};

/**
 *
 */
AjaxParameter.prototype.toAjaxParameter = function (serviceName, methodName, needCheckLogin, returnValueType) {
    needCheckLogin = needCheckLogin == null ? "false" : needCheckLogin;
    if (!serviceName || !methodName) {
        return null;
    }

    var str = "";
    str += "S=" + serviceName;
    str += "&M=" + methodName;
    str += "&CL=" + needCheckLogin;
    str += "&RVT=" + returnValueType;

    if (this.instance != null && this.instance.length > 0) {
        for (var i = 0; i < this.instance.length; i++) {
            var obj = this.instance[i];

            var paramterName = "P_" + obj.index + "_" + obj.type;

            if (obj.isArray) { //数组
                if (obj.value == null || obj.value.length == 0) {
                    str += "&" + paramterName + "_A_N=";
                } else if (obj.value instanceof Array) {
                    for (var k = 0; k < obj.value.length; k++) {
                        str += "&" + paramterName + "_A=" + encodeURIComponent(obj.value[k]);
                    }
                }
            } else {
                var v = obj.value == null ? "" : obj.value;
                str += "&" + paramterName + "=" + encodeURIComponent(v);
            }
        }
    }

    return str;
};

/**
 * Browser independent XMLHttpRequestLoader
 *
 * @param _caller d
 */
function XMLHttpRequestCaller(_caller, serviceName, methodName, async, method, needCheckLogin, actionUrl) {
    if ((!serviceName || !methodName) && !actionUrl) {
        alert("AJAX Service name or method, actionUrl is not null.");
        throw new Error(3, "AJAX Service name or method is not null.");
    }

    this.params = new AjaxParameter();
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.needCheckLogin = needCheckLogin == null ? "true" : needCheckLogin;
    this.returnValueType = "XML"; //XML TEXT

    this.method = method || AJAX_XMLHttpRequest_DEFAULT_METHOD;
    this.async = (async == null ? AJAX_XMLHttpRequest_DEFAULT_async : async);
    this._caller = _caller;
    this.actionUrl = actionUrl;

    this.filterLogoutMessage = true;
    this.closeConnection = false;
};

/**
 *
 * caller.addParameter(1, "String", "a8");
 * caller.addParameter(2, "Long", 2345234);
 * caller.addParameter(3, "String[]", ["tanmf", "jicnm", "maok", ""]);
 * caller.addParameter(4, "date", "2007-01-01 12:25:23");
 *
 * @param index 参数顺序，从1开始
 * @param type 参数类型 当前支持byte Byte short Short int Integer long Long double Double float Float boolean Boolean char character String date datetime
 * @param value 参数值 可以是数组
 */
XMLHttpRequestCaller.prototype.addParameter = function (index, type, value) {
    this.params.put(index, type, value);
};

/**
 * 发出请求
 */
XMLHttpRequestCaller.prototype.serviceRequest = function () {
    var url = null;
    var sendContent = null;
    if (this.actionUrl) {
        url = getBaseURL() + this.actionUrl;
        sendContent = this.sendData;
    } else {
        var _url = getBaseURL() + "/getAjaxDataServlet"
        var _queryString = this.params.toAjaxParameter(this.serviceName, this.methodName, this.needCheckLogin, this.returnValueType);
        if (!_queryString) {
            throw new Error(5, "No parameters");
        }

        if (_queryString.length < 500) {
            this.method = "GET";
        }

        if (this.method.toUpperCase() == "POST") {
            url = _url;
            sendContent = _queryString;
        } else if (this.method.toUpperCase() == "GET") {
            url = _url + "?" + _queryString
        }
    }

    var xmlRequest = getHTTPObject();
    var c = (typeof this.invoke != 'undefined') ? this : this._caller;
    var flm = this.filterLogoutMessage;

    if (!xmlRequest) {
        throw new Error(2, "The current browser does not support XMLHttpRequest");
    }

    if (this.async) { //异步
        xmlRequest.onreadystatechange = function () {
            if (xmlRequest.readyState == 4) {
                if (xmlRequest.status == 200) {
                    var returnValue = getXMLHttpRequestData(xmlRequest, flm);
                    c.invoke(returnValue); //回调主函数
                } else {
                    if (c && c.showAjaxError) {
                        c.showAjaxError(xmlRequest.status);
                    } else {
                        c.invoke(null);
                    }
                }
            }
        };
    }

    xmlRequest.open(this.method, url, this.async);
    xmlRequest.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    xmlRequest.setRequestHeader("RequestType", "AJAX");
    if (this.closeConnection) {
        xmlRequest.setRequestHeader("Connection", "close");
    }

    var csrfToken = CsrfGuard.getToken();
    if (csrfToken !== '') {
        xmlRequest.setRequestHeader("CSRFTOKEN", csrfToken);
    }
    xmlRequest.send(sendContent);

    if (!this.async) { //同步
        if (xmlRequest.readyState == 4) {
            if (xmlRequest.status == 200) {
                return getXMLHttpRequestData(xmlRequest, flm);
            } else {
                //              throw "There was a problem retrieving the XML data:\n" + xmlRequest.statusText + " for AjAX Service: \n" + this.serviceName + "." + this.methodName;
            }
        }
    }
};

function getXMLHttpRequestData(xmlRequest, filterLogoutMessage) {
    var ct = xmlRequest.getResponseHeader("content-type");
    var isXML = ct && ct.indexOf("xml") >= 0;
    var data = isXML ? xmlRequest.responseXML : xmlRequest.responseText;

    //window.clipboardData.setData("text", data);
    if (isXML) {
        data = xmlHandle(data) || xmlRequest.responseText;
    }

    //不需要过滤[logout]，默认都要过滤
    if (filterLogoutMessage == true && data != null && data.toString().indexOf("[LOGOUT]") == 0) {
        return null;
    }

    return data;
}

/**
 * 解析XML
 */
function xmlHandle(xmlDom) {
    if (!xmlDom) {
        return null;
    }

    try {
        var root = xmlDom.documentElement;
        if (null != root) {
            var type = root.nodeName;

            if (type == AJAX_RESPONSE_XML_TAG_BEAN) {
                return beanXmlHandle(root); //bean xml
            } else if (type == AJAX_RESPONSE_XML_TAG_LIST) {
                return listXmlHandle(root); //bean xml
            } else if (type == AJAX_RESPONSE_XML_TAG_Value) {
                return root.firstChild.nodeValue;
            }
        }
    } catch (e) {
        throw e.message;
    }

    return null;
};

/**
 * 解析
 * @return Properties
 */
function beanXmlHandle(_node) {
    if (!_node) {
        return null;
    }

    var properties = new Properties();
    properties.type = "";

    var propertys = _node.childNodes;

    if (propertys != null && propertys.length > 0) {
        for (var i = 0; i < propertys.length; i++) {
            var key = propertys[i].attributes.getNamedItem(AJAX_RESPONSE_XML_TAG_Name).nodeValue;
            var value = "";
            var fChild = propertys[i].firstChild;


            if (fChild != null) {
                if (fChild.childNodes != null && fChild.childNodes.length > 0) { //有子节点
                    var type = fChild.nodeName;

                    if (type == AJAX_RESPONSE_XML_TAG_BEAN) {
                        value = beanXmlHandle(fChild);
                    } else if (type == AJAX_RESPONSE_XML_TAG_LIST) {
                        value = listXmlHandle(fChild);
                    } else if (type == AJAX_RESPONSE_XML_TAG_Value) {
                        value = fChild.firstChild.nodeValue;
                    }
                } else {
                    value = fChild.nodeValue;
                }
            }

            properties.putRef(key, (value));
        }
    }

    return properties;
};

/**
 *
 * @return Array Properties[]
 */
function listXmlHandle(_node) {
    var list = new Array();

    if (_node != null) {
        var properties = new Properties();
        var beans = _node.childNodes;

        if (beans != null && beans.length > 0) {
            for (var i = 0; i < beans.length; i++) {
                var type = beans[i].nodeName;
                var returnVal = "";

                if (type == AJAX_RESPONSE_XML_TAG_BEAN) {
                    returnVal = beanXmlHandle(beans[i]);
                } else if (type == AJAX_RESPONSE_XML_TAG_LIST) {
                    returnVal = listXmlHandle(beans[i]);
                } else if (type == AJAX_RESPONSE_XML_TAG_Value) {
                    returnVal = beans[i].firstChild.nodeValue;
                }

                list[i] = returnVal;
            }
        }
    }

    return list;
};

/** Cross browser XMLHttpObject creator */
function getHTTPObject() {
    var xmlhttp;
    /*@cc_on
    @if (@_jscript_version >= 5)
      try {
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
      }
      catch (e) {
        try {
          xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        }
        catch (E) {
          xmlhttp = false;
        }
      }
    @else
    xmlhttp = false;
    @end @*/
    if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
        try {
            xmlhttp = new XMLHttpRequest();
        } catch (e) {
            xmlhttp = false;
        }
    }
    return xmlhttp;
};

function getBaseURL() {
    return "/seeyon";
};

/**ajax组件：end**/

/**表单验证组件的部分功能,摘至 /seeyon/common/js/ui/ seeyon.ui.checkform-debug.js：start**/
function isNull(value, notTrim) {
    if (value == null) {
        return true;
    } else if (typeof (value) == "string") {
        value = notTrim == true ? $.trim(value) : value;
        if (value == "") {
            return true;
        }
    }
    return false;
}

/*
 * isNaN返回值为true的话，表明不是数字。返回值为false的话表明是数字。
 */
function isANumber(value) {
    if (typeof value == "string") {
        value = value;
    }
    return /^[-+]?\d+([\.]\d+)?$/.test(value);
}

$.fn.validate = function (options) {
    var settings = {
        errorIcon: true,
        errorAlert: false,
        errorBg: false,
        validateHidden: false,
        checkNull: true
    };
    options = $.extend(settings, options);
    return MxtCheckForm(this, options);
};

$.fn.validateChange = function (obj) {
    var tempElem = this;
    if (tempElem != null && tempElem.size() > 0) {
        addCheckMsg(tempElem, obj);
    }
    tempElem = null;
}
var attrObjs = [];
$.fn.attrObj = function (name, value) {
    var obj;
    for (var i = 0; i < attrObjs.length; i++) {
        if (attrObjs[i].o == this[0]) {
            obj = attrObjs[i];
            break;
        }
    }
    if (!obj) {
        obj = new Object();
        obj.o = this[0];
        obj.v = new Object();
        attrObjs.push(obj);
    }
    if (value) {
        obj.v[name] = value;
    } else {
        return obj.v[name];
    }
};

$.fn.dataI18n = function (options) {
    var input = $(this);
    var id = input.attr('id'), name = input.attr('name'), i18nValue = input.attr('value');

    // 保证id和name都有值
    if ((id == undefined || id == null) && (name == undefined || name == null)) {
        var d = new Date();
        id = "dataI18n_" + d.getTime();
        name = id;
    } else if (id == undefined || id == null) {
        id = name;
    } else if (name == undefined || name == null) {
        name = id;
    }

    input.addClass("msHideClear");
    input.attr('id', id + '_text');
    input.attr('name', name + '_text');
    input.attr('value', '');// 先置空，后面会重新赋值
    input.on('blur', function () {// 使用on，可以让多次绑定的函数都执行
        setI18nValue(id, options);
    });

    var valueInput = $('<input type="hidden" />');
    valueInput.attr('id', id);
    valueInput.attr('name', name);
    valueInput.attrObj('_comp', input);
    input.before(valueInput);

    // 此处参考的日期组件样式
    var languageBtn = $('<span class="internationalization-wrapper">' +
        '<span id="' + id + '_click" title="' + $.i18n('system.menuname.i18nresource') +
        '" class="vportal vp-internationalization-resources internationalizationBtn" style="display:none">' +
        '</span>' +
        '</span>');
    input.after(languageBtn);

    $("#" + id + "_click").on('click', function () {
        var validate = input.attr('validate');
        openDataI18nDialogSingle(id, options, validate);
    });

    var compI18nSwitch = getI18nSwitch(options);
    fillI18nValue(id, i18nValue, options, compI18nSwitch);
};

/**
 * 回填表单元素的值，业务代码中请不要调用此方法。
 * @param eleId
 * @param i18nValue
 * @param options
 * @param compI18nSwitch
 */
function fillI18nValue(eleId, i18nValue, options, compI18nSwitch) {
    var inputText = $("#" + eleId + "_text");
    if (!i18nValue) {
        i18nValue = "";
    }

    // ajax请求目的：1.获取国际化数据  2.获取是否开启了国际化
    callBackendMethod("dataI18nManager", "getDataI18nInfo", i18nValue, {
        success: function (res) {
            if (res.dataI18nSwitch && compI18nSwitch) {// 开启了数据国际化
                $("#" + eleId + "_click").css("display", "inline-block");// 显示小地球

                var inputHidden = $("#" + eleId);
                var earthIcon = $("#" + eleId + "_click");
                if (inputHidden.prop("disabled") || inputHidden.prop("readonly") || inputText.prop("disabled") || inputText.prop("readonly")) {
                    // 不能点击，置灰
                    earthIcon.css("color", "#999");
                } else {// 能点击
                    earthIcon.hover(function () {
                        earthIcon.css("color", "#4A90E2 ");// 高亮
                    }, function () {
                        earthIcon.css("color", ""); // 移除内联样式，使用默认样式颜色
                    });
                }
            }

            var showText = res.showText;
            if (!showText) {
                showText = '';
            }

            if (res.internationalized) {// 数据被国际化
                var attrObj = new Object();
                attrObj.i18n = true;
                attrObj.currentLanguage = res.currentLanguage;
                attrObj.allLanguageValue = $.toJSON(res.allLanguageValue);
                setI18nAttr(eleId, attrObj);

                inputText.val(showText);
                $("#" + eleId).val(i18nValue);
            } else {
                if (showText.length > 0) {
                    inputText.val(showText);
                    $("#" + eleId).val(showText);
                }
            }
        }
    });
}

/**
 * 国际化对话框
 * @param eleId
 * @param options
 * @param validate
 */
function openDataI18nDialogSingle(eleId, options, validate) {
    var inputHidden = $("#" + eleId);
    var inputText = $("#" + eleId + "_text");
    var hiddenVal = inputHidden.val();
    var showText = inputText.val();
    validate = (validate != undefined && validate != null && validate.length > 0) ? validate : "";
    var settings = {showText: showText, validate: validate};
    options = $.extend(settings, options);

    if (inputHidden.prop("disabled") || inputHidden.prop("readonly") || inputText.prop("disabled") || inputText.prop("readonly")) {
        return;
    }

    var categoryName = "";
    if (options.categoryName != undefined && options.categoryName != null) {
        categoryName = ' - ' + options.categoryName;
    }
    var dataI18nID = isDataI18nId(hiddenVal) ? hiddenVal : "";
    var url = _ctxPath + '/international/dataI18n.do?method=singleSet&dataI18nID=' + dataI18nID + CsrfGuard.getUrlSurffix();
    var dialog = $.dialog({
        id: "showDataI18nDialogSingle",
        url: url,
        width: 500,
        height: 220,
        title: $.i18n('common.datai18n.title') + categoryName,
        checkMax: true,
        transParams: options,
        targetWindow: getCtpTop(),
        buttons: [
            {
                text: $.i18n('common.button.ok.label'),
                isEmphasize: true,
                handler: function () {
                    var returnValue = dialog.getReturnValue();
                    if (returnValue == undefined || !returnValue.validate) {
                        return;
                    }

                    var currentLocale = returnValue.currentLocale;
                    var currentLocaleText = "";
                    for (var key in returnValue) {
                        if (currentLocale == key) {
                            currentLocaleText = returnValue[key];// 当前语言的国际化值
                        }
                    }

                    var submitStr = $.toJSON(returnValue);
                    callBackendMethod("dataI18nManager", "saveSingleI18n", submitStr, {
                        success: function (res) {
                            var inputText = $("#" + eleId + "_text");

                            var attrObj = new Object();
                            attrObj.i18n = true;
                            attrObj.currentLanguage = res.currentLanguage;
                            attrObj.allLanguageValue = $.toJSON(res.allLanguageValue);
                            setI18nAttr(eleId, attrObj);

                            inputText.val(currentLocaleText);
                            $("#" + eleId).val(returnValue.id);

                            inputText.trigger("blur");// 触发事件，主要为了执行业务自定义的onblur事件
                            dialog.close();
                        }
                    });
                }
            },
            {
                text: $.i18n('common.button.cancel.label'),
                handler: function () {
                    dialog.close();
                }
            }
        ]
    });
};

function getI18nSwitch(options) {
    if (options.i18nSwitch != undefined && options.i18nSwitch != null && options.i18nSwitch == "off") {
        return false;
    } else {
        return true;
    }
}

/**
 * 填充组件值，供业务代码中使用
 * @param eleId
 * @param i18nValue
 */
$.fn.setI18nVal = function (i18nValue) {
    var input = $(this);
    var eleId = input.prop('id');
    fillI18nValue(eleId, i18nValue, null, true);
}

/**
 * 填充组件值，组件内部使用
 * @param eleId
 * @param options
 */
function setI18nValue(eleId, options) {
    var inputText = $("#" + eleId + "_text");
    var inputVal = $.trim(inputText.val());
    if (inputText.prop("data-i18n") == true) {// 字段已经国际化
        // 更新当前语言的数据。直接在组件文本框中(非国际化弹出框)编辑了数据之后会走此处逻辑。
        var langVal = inputText.prop("data-allLanguageValue");// 所有国际化信息
        var currentLanguage = inputText.prop("data-currentLanguage");// 当前语言
        if (langVal == undefined || langVal == null || currentLanguage == undefined || currentLanguage == null) {
            return;
        }

        var currLangOldVal = "";// 当前语言旧的国际化值
        var langValJSON = $.parseJSON(langVal);
        for (var key in langValJSON) {
            if (key == currentLanguage) {
                currLangOldVal = langValJSON[key];
                langValJSON[key] = inputVal;// 当前语言新的国际化值
                break;
            }
        }

        var attrObj = {};
        attrObj.allLanguageValue = $.toJSON(langValJSON);
        setI18nAttr(eleId, attrObj);// 更新组件属性

        if (inputVal.length > 0 && inputVal != currLangOldVal) {// 当前语言国际化值有变化，进行更新操作
            var submitObj = {};
            submitObj.id = $("#" + eleId).val();
            submitObj.category = options.category;
            submitObj.language = currentLanguage;
            submitObj.languageValue = inputVal;
            var submitStr = $.toJSON(submitObj);// 不要直接拼接json串出来，直接拼接会有问题。

            callBackendMethod("dataI18nManager", "updateSingleLanguage", submitStr, {
                success: function (res) {
                }
            });
        }
    } else {
        $("#" + eleId).val(inputVal);
    }
}

function setI18nAttr(eleId, attrs) {
    var inputText = $("#" + eleId + "_text");
    if (attrs.i18n) {
        inputText.prop("data-i18n", attrs.i18n);// 是否国际化
    }
    if (attrs.currentLanguage) {
        inputText.prop("data-currentLanguage", attrs.currentLanguage);// 当前语言
    }
    if (attrs.allLanguageValue) {
        inputText.prop("data-allLanguageValue", attrs.allLanguageValue);// 所有语言的国际化信息

        // 提示信息
        var langValJSON = $.parseJSON(attrs.allLanguageValue);
        var langValTip = "";
        for (var key in langValJSON) {
            langValTip = langValTip + (key + " : " + langValJSON[key] + "   ");
        }
        inputText.prop("title", langValTip);
    }
}

function isDataI18nId(i18nValue) {
    var reg = /^[-]{0,1}[\d]{1,19}$/;
    return reg.test(i18nValue);
}

var invalid = [];
$._invalidObj = function (obj) {
    if (obj)
        invalid.push(obj);
    invalid.contains(obj);
};

$._isInValid = function (obj) {
    if (invalid.contains) {
        return invalid.contains(obj);
    }
    return null;
};
$.isNull = isNull;
$.isANumber = isANumber;

/**表单验证组件的部分功能,摘至 /seeyon/common/js/ui/ seeyon.ui.checkform-debug.js：end**/


/* 公共JS-摘至v3x-debug.js： begin */

var messageRegEx_0 = /\{0\}/g;
var messageRegEx_1 = /\{1\}/g;
var messageRegEx_2 = /\{2\}/g;
var messageRegEx_3 = /\{3\}/g;
var messageRegEx_4 = /\{4\}/g;
var messageRegEx_5 = /\{5\}/g;
var messageRegEx_6 = /\{6\}/g;
var messageRegEx_7 = /\{7\}/g;
var messageRegEx_8 = /\{8\}/g;
var messageRegEx_9 = /\{9\}/g;
var messageRegEx_10 = /\{10\}/g;
var messageRegEx_11 = /\{11\}/g;
var messageRegEx_12 = /\{12\}/g;
var messageRegEx_13 = /\{13\}/g;
var messageRegEx_14 = /\{14\}/g;
var messageRegEx_15 = /\{15\}/g;

var getCtpTop = function () {
    try {
        var A8TopWindow = getCtpParentWindow(window);
        if (A8TopWindow) {
            return A8TopWindow;
        } else {
            return top;
        }
    } catch (e) {
        return top;
    }
}

var getA8Top = getCtpTop;

var getCtpParentWindow = function (win) {
    var currentWin = win;
    for (var i = 0; i < 20; i++) {
        if (typeof currentWin.isCtpTop != 'undefined' && currentWin.isCtpTop) {
            return currentWin;
        } else {
            //当window.top没有isCtpTop标记的时候,优化下，避免取20次的问题 【新开窗口基本上都是这种场景】
            if (currentWin == window.top) break;
            currentWin = currentWin.parent;
        }
    }
}
/**
 * 日期格式化
 */
Date.prototype.format = function (pattern) {
    var hour = this.getHours();
    var o = {
        "M+": this.getMonth() + 1, //month
        "d+": this.getDate(), //day
        "H+": hour, //hour
        "h+": (hour > 12 ? hour - 12 : hour), //hour
        "m+": this.getMinutes(), //minute
        "s+": this.getSeconds(), //second
        "q+": Math.floor((this.getMonth() + 3) / 3), //quarter
        "S": this.getMilliseconds() //millisecond
    }

    if (/(y+)/.test(pattern)) {
        pattern = pattern.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }

    for (var k in o)
        if (new RegExp("(" + k + ")").test(pattern)) {
            pattern = pattern.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
        }

    return pattern;
}
var getElementPosition = function (el) {
    var ua = navigator.userAgent.toLowerCase();
    var isOpera = (ua.indexOf('opera') != -1);
    var isIE = (ua.indexOf('msie') != -1 && !isOpera);
    // not opera spoof
    if (el.parentNode === null || el.style.display == 'none') {
        return false;
    }
    var parent = null;
    var pos = [];
    var box;
    if (el.getBoundingClientRect) { //IE
        box = el.getBoundingClientRect();
        var scrollTop = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        var scrollLeft = Math.max(document.documentElement.scrollLeft, document.body.scrollLeft);
        return {
            x: box.left + scrollLeft,
            y: box.top + scrollTop
        };
    } else if (document.getBoxObjectFor) { // gecko
        box = document.getBoxObjectFor(el);
        var borderLeft = (el.style.borderLeftWidth) ? parseInt(el.style.borderLeftWidth) : 0;
        var borderTop = (el.style.borderTopWidth) ? parseInt(el.style.borderTopWidth) : 0;
        pos = [box.x - borderLeft, box.y - borderTop];
    } else { // safari & opera
        pos = [el.offsetLeft, el.offsetTop];
        parent = el.offsetParent;
        if (parent != el) {
            while (parent) {
                pos[0] += parent.offsetLeft;
                pos[1] += parent.offsetTop;
                parent = parent.offsetParent;
            }
        }
        if (ua.indexOf('opera') != -1 || (ua.indexOf('safari') != -1 && el.style.position == 'absolute')) {
            pos[0] -= document.body.offsetLeft;
            pos[1] -= document.body.offsetTop;
        }
    }
    if (el.parentNode) {
        parent = el.parentNode;
    } else {
        parent = null;
    }
    while (parent && parent.tagName != 'BODY' && parent.tagName != 'HTML') { // account for any scrolled ancestors
        pos[0] -= parent.scrollLeft;
        pos[1] -= parent.scrollTop;
        if (parent.parentNode) {
            parent = parent.parentNode;
        } else {
            parent = null;
        }
    }
    return {
        x: pos[0],
        y: pos[1]
    };
}

function V3X() {
    this.windowArgs = new Array();
    this.lastWindow = null;
    // Browser check
    var ua = navigator.userAgent;
    this.isMSIE = (navigator.appName == "Microsoft Internet Explorer") || ua.indexOf('Trident') != -1;
    this.isMSIE5 = this.isMSIE && (ua.indexOf('MSIE 5') != -1);
    this.isMSIE5_0 = this.isMSIE && (ua.indexOf('MSIE 5.0') != -1);
    this.isMSIE6 = this.isMSIE && (ua.indexOf('MSIE 6') != -1);
    this.isMSIE7 = this.isMSIE && (ua.indexOf('MSIE 7') != -1);
    this.isMSIE8 = this.isMSIE && (ua.indexOf('MSIE 8') != -1);
    this.isMSIE9 = this.isMSIE && (ua.indexOf('MSIE 9') != -1);
    this.isMSIE10 = this.isMSIE && (ua.indexOf('MSIE 10') != -1);
    this.isMSIE11 = this.isMSIE && (ua.indexOf('rv:11') != -1);
    this.isGecko = ua.indexOf('Gecko') != -1;
    this.isGecko18 = ua.indexOf('Gecko') != -1 && ua.indexOf('rv:1.8') != -1;
    this.isSafari = ua.indexOf('Safari') != -1;
    this.isOpera = ua.indexOf('Opera') != -1;
    this.isFirefox = ua.indexOf('Firefox') != -1;
    this.isMac = ua.indexOf('Mac') != -1;
    this.isNS7 = ua.indexOf('Netscape/7') != -1;
    this.isNS71 = ua.indexOf('Netscape/7.1') != -1;
    this.isIpad = ua.indexOf('iPad') != -1;
    this.isChrome = ua.indexOf('Chrome') != -1;
    //IE6/7/8、IE9、FireFox、iPad、Chrome、Safari、Opera
    this.currentBrowser = "";
    //引用if(v3x.getBrowserFlag('selectPeople')){}
    this.browserFlag = {
        //弹出模态窗口还是正常窗口,true:模态对话框 ；false：正常窗口 -- 首页打开、处理协同
        openWindow: [true, true, false, false, false, false, false],
        //首页栏目是否用模态对话框打开，只有ipad用open window
        sectionOpenDetail: [true, true, true, false, true, true, false],
        //选人界面 true pc false ipad -- 选人界面内部
        selectPeople: [true, true, true, false, true, true, false],
        //不支持富文本，只提供纯文本编辑框,true:支持富文本，false不支持 -- 问本编辑器
        htmlEditer: [true, true, true, false, true, true, false],
        //菜单 -- 系统内部toolbar
        hideMenu: [true, true, true, false, true, true, false],
        //使用flash 新建流程图
        newFlash: [true, true, true, false, true, false, false],
        //签章,true支持 false不支持 -- 签章
        signature: [true, true, false, false, false, false, false],
        //新建流程 true pc模式 false 只新建一次 --
        createProcess: [true, true, true, false, true, true, false],
        //flash  pc模式 false html5 --
        flash: [true, true, true, false, true, true, false],
        //是否允许下载 true 允许下载 false 屏蔽下载  -- 系统内部toolbar
        downLoad: [true, true, true, false, true, true, false],
        //打印 true pc false ipad -- 系统内部toolbar/功能
        print: [true, true, true, false, true, true, false],
        //导出Excel  true pc false ipad -- 隐藏导出功能
        exportExcel: [true, true, true, false, true, true, false],
        //是否显示上下结构,true:显示上下结构;false:纯列表显示 -- 上下结构
        pageBreak: [true, true, true, false, true, true, false],
        //菜单定位只准对ipad -- 空间栏目下拉菜单
        menuPosition: [false, false, false, true, false, false, false],
        //office插件 --
        officeMenu: [true, true, true, false, true, false, false],
        //选人界面内部div改造 -- 选人界面select list ipad 不能展开
        selectPeopleShowType: [true, true, true, false, true, true, false],
        //div实现模态窗口 -- div实现模态
        OpenDivWindow: [true, true, true, false, true, true, false],
        //select div改造
        selectDivType: [true, true, true, false, true, true, false],
        //ipad不支持双击事件
        onDbClick: [true, true, true, false, true, true, true],
        //safari 下需要模态
        needModalWindow: [true, true, true, false, true, true, false],
        //只有ie
        onlyIe: [true, true, false, false, false, false, false]
    }
    this.isOfficeSupport = function () {
        return this.getBrowserFlag("officeMenu") == true;
    }
    this.dialogCounter = 0;

    this.defaultLanguage = "en";
    this.currentLanguage = "";
    this.baseURL = "";
    this.loadedFiles = new Array();
    this.workSpaceTop = 130;
    if (this.isMSIE8) {
        this.workSpaceTop = 140;
    }
    if (!this.isMSIE7 && !this.isMSIE8) {
        this.workSpaceTop = 130;
    }
    this.workSpaceLeft = 0;
    this.workSpaceWidth = screen.width - this.workSpaceLeft;
    this.workSpaceheight = screen.height - this.workSpaceTop - 20 - (this.isMSIE7 ? 35 : 0);

    // Fake MSIE on Opera and if Opera fakes IE, Gecko or Safari cancel those
    if (this.isOpera) {
        this.isMSIE = true;
        this.isGecko = false;
        this.isSafari = false;
    }

    this.settings = {
        dialog_type: "modal",
        resizable: "yes",
        scrollbars: "yes"
    };
}

V3X.prototype.init = function (contextPath, language) {
    if (contextPath) {
        this.baseURL = contextPath;
    }

    this.currentLanguage = language;

    //this.loadScriptFile(this.baseURL + "/common/office/license.js?V=5_0_5_30");
    this.getCurrentBrowser();
}


V3X.prototype.getCurrentBrowser = function () {

    ////IE6/7/8、IE9、FireFox、iPad、Chrome、Safari、Opera

    if (this.isMSIE || this.isMSIE5 || this.isMSIE5_0 || this.isMSIE7 || this.isMSIE8) this.currentBrowser = 'MSIE';

    if (this.isMSIE9) this.currentBrowser = 'MSIE9';

    if (this.isFirefox) this.currentBrowser = 'FIREFOX';

    if (this.isSafari) this.currentBrowser = 'SAFARI';

    if (this.isChrome) this.currentBrowser = 'CHROME';

    if (this.isIpad) this.currentBrowser = 'IPAD';

    if (this.isOpera) this.currentBrowser = 'OPERA';


}

V3X.prototype.getBrowserFlag = function (name) {

    ////IE6/7/8、IE9、FireFox、iPad、Chrome、Safari、Opera
    if (name != null && name != '') {

        var i = 0;

        if (this.currentBrowser == 'MSIE') i = 0;

        if (this.currentBrowser == 'MSIE9') i = 1;

        if (this.currentBrowser == 'FIREFOX') i = 2;

        if (this.currentBrowser == 'IPAD') i = 3;

        if (this.currentBrowser == 'CHROME') i = 4;

        if (this.currentBrowser == 'SAFARI') i = 5;

        if (this.currentBrowser == 'OPERA') i = 6;

        return this.browserFlag[name][i];

    }

}
//div窗口
V3X.prototype.openDialog = function (json) {
    return new MxtWindow(json);
}
//获得event,兼容多浏览器
V3X.prototype.getEvent = function () {
    if (this.isMSIE) {
        return window.event; //如果是ie
    }
    func = v3x.getEvent.caller;
    while (func != null) {
        var arg0 = func.arguments[0];
        if (arg0) {
            if ((arg0.constructor == Event || arg0.constructor == MouseEvent) || (typeof (arg0) == "object" && arg0.preventDefault && arg0.stopPropagation)) {
                return arg0;
            }
        }
        func = func.caller;
    }
    return null;
}
/**
 *
 var args = new Array();

 args['file']   = 'about.htm';
 args['width']  = 480;
 args['height'] = 380;

 v3x.openWindow(args});
 */
V3X.prototype.openWindow = function (args) {
    var html, width, height, x, y, resizable, scrollbars, url;

    this.windowArgs = args;

    html = args['html'];

    if (args["FullScrean"]) {
        width = this.workSpaceWidth;
        height = this.workSpaceheight + this.workSpaceTop;
        width = width - 20;

        x = 0;
        y = 0;
    } else if (args["workSpace"]) {
        width = this.workSpaceWidth;
        height = this.workSpaceheight;

        width = width - 30;
        x = this.workSpaceLeft;
        y = this.workSpaceTop;
        //if(this.isSafari){
        //所有浏览器，减去任务栏高度
        y = y - 40;
        //}
    } else if (args["workSpaceRight"]) {
        width = this.workSpaceWidth - 155;
        height = this.workSpaceheight;
        if (this.isMSIE8) {
            height = this.workSpaceheight - 48;
        }
        if (!this.isMSIE7 && !this.isMSIE8) {
            width = this.workSpaceWidth - 165;
            height = this.workSpaceheight - 35;
        }
        x = 140;
        y = this.workSpaceTop;
    } else {
        width = args['width'] || 320;
        height = args['height'] || 200;

        width = parseInt(width);
        height = parseInt(height);

        if (this.isMSIE) {
            if (this.isMSIE7 || this.isMSIE8) {
                height -= 6;
            } else {
                height += 20;
            }
        }

        x = args["left"] || parseInt(screen.width / 2.0) - (width / 2.0);
        y = args["top"] || parseInt(screen.height / 2.0) - (height / 2.0);
    }

    resizable = args['resizable'] || this.settings["resizable"];
    scrollbars = args['scrollbars'] || this.settings["scrollbars"];

    url = args['url'];

    if (html) {
        var win = window.open("", "v3xPopup" + new Date().getTime(), "top=" + y + ",left=" + x + ",scrollbars=" + scrollbars + ",dialog=yes,minimizable=" + resizable + ",modal=yes,width=" + width + ",height=" + height + ",resizable=" + resizable);
        if (win == null) {
            return;
        }

        win.document.write(html);
        win.document.close();
        win.resizeTo(width, height);
        win.focus();

        return win;
    } else {
        var dialog_type = args["dialogType"] || this.settings["dialog_type"];

        if (dialog_type == "modal" && window.showModalDialog) {
            var features = "resizable:" + resizable + ";scroll:" + scrollbars + ";status:no;help:no;dialogWidth:" + width + "px;dialogHeight:" + height + "px;";

            if (args["workSpace"] || args["workSpaceRight"] || (args["left"] && args["top"])) {
                features += "dialogTop:" + y + "px;dialogLeft:" + x + "px;";
            } else {
                var cw = (parseInt(getA8Top().document.body.offsetWidth) - width) / 2;
                var ch = (parseInt(getA8Top().document.body.offsetHeight) - height) / 2;
                if (cw == null || ch == null || cw < 0 || ch < 0) {
                    cw = 200;
                    ch = 200;
                }
                features += this.isMSIE ? "center:yes;" : "dialogTop:" + ch + "px;dialogLeft:" + cw + "px;";
            }
            if (url.indexOf('?') != -1)
                url += '&';
            else
                url += '?';
            url += '_isModalDialog=true';
            var rv = window.showModalDialog(url, window, features);

            var temp = null;
            if (this.ModalDialogResultValue == undefined) {
                temp = rv;
            } else {
                temp = this.ModalDialogResultValue;
                this.ModalDialogResultValue = undefined;
            }
            return temp;
        } else {
            var rv = null;
            var modal = (resizable == "yes") ? "no" : "yes";

            if (this.isGecko && this.isMac)
                modal = "no";

            if (args['closePrevious'] != "no")
                try {
                    this.lastWindow.close();
                } catch (ex) {
                }
            if (window.dialogArguments && args["workSpace"]) {
                y -= 5;
                height -= 25;
            }
            var win = window.open(url, "v3xPopup" + new Date().getTime(), "top=" + y + ",left=" + x + ",scrollbars=" + scrollbars + ",dialog=" + modal + ",minimizable=" + resizable + ",modal=" + modal + ",width=" + width + ",height=" + height + ",resizable=" + resizable);
            if (win == null) {
                return;
            }

            if (args['closePrevious'] != "no")
                this.lastWindow = win;

            //          eval('try { win.resizeTo(width, height); } catch(e) { }');

            // Make it bigger if statusbar is forced
            if (this.isGecko && !this.isMSIE && !this.isFirefox) {
                if (win.document.defaultView.statusbar.visible)
                    win.resizeBy(0, this.isMac ? 10 : 24);
            }

            win.focus();

            return win;
        }
    }
}

V3X.prototype.setResultValue = function (obj) {
    this.getParentWindow().v3x.ModalDialogResultValue = obj;
}

V3X.prototype.closeWindow = function (win) {
    win.close();
}

/**
 * 得到弹出当前窗口的直接父窗口
 */
V3X.prototype.getParentWindow = function (win) {
    win = win || window;
    if (win.dialogArguments) {
        return win.dialogArguments;
    } else {
        return win.opener || win;
    }
}

V3X.prototype.loadLanguage = function (url) {
    this.loadScriptFile(this.baseURL + url + "/" + this.currentLanguage + ".js?V=3_50_2_29");
}

/**
 * JS的国际化
 */
V3X.prototype.getMessage = function (key) {
    try {
        var msg = eval("" + key);

        if (msg && arguments.length > 1) {
            for (var i = 0; i < arguments.length - 1; i++) {
                var regEx = eval("messageRegEx_" + i);
                var repMe = "" + arguments[i + 1];
                if (repMe.indexOf("$_") != -1) {
                    repMe = repMe.replace("$_", "$$_");
                }
                msg = msg.replace(regEx, repMe);
            }
        }

        return msg;
    } catch (e) {
    }

    return "";
}


/**
 *
 */
V3X.prototype.loadScriptFile = function (url) {
    for (var i = 0; i < this.loadedFiles.length; i++) {
        if (this.loadedFiles[i] == url)
            return;
    }

    document.write('<script language="javascript" type="text/javascript" charset="UTF-8" src="' + url + '"></script>');

    this.loadedFiles[this.loadedFiles.length] = url;
};
V3X.prototype.getElementPosition = function (el) {
    var ua = navigator.userAgent.toLowerCase();
    var isOpera = (ua.indexOf('opera') != -1);
    var isIE = (ua.indexOf('msie') != -1 && !isOpera);
    // not opera spoof
    if (el.parentNode === null || el.style.display == 'none') {
        return false;
    }
    var parent = null;
    var pos = [];
    var box;
    if (el.getBoundingClientRect) { //IE
        box = el.getBoundingClientRect();
        var scrollTop = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        var scrollLeft = Math.max(document.documentElement.scrollLeft, document.body.scrollLeft);
        return {
            x: box.left + scrollLeft,
            y: box.top + scrollTop
        };
    } else if (document.getBoxObjectFor) { // gecko
        box = document.getBoxObjectFor(el);
        var borderLeft = (el.style.borderLeftWidth) ? parseInt(el.style.borderLeftWidth) : 0;
        var borderTop = (el.style.borderTopWidth) ? parseInt(el.style.borderTopWidth) : 0;
        pos = [box.x - borderLeft, box.y - borderTop];
    } else { // safari & opera
        pos = [el.offsetLeft, el.offsetTop];
        parent = el.offsetParent;
        if (parent != el) {
            while (parent) {
                pos[0] += parent.offsetLeft;
                pos[1] += parent.offsetTop;
                parent = parent.offsetParent;
            }
        }
        if (ua.indexOf('opera') != -1 || (ua.indexOf('safari') != -1 && el.style.position == 'absolute')) {
            pos[0] -= document.body.offsetLeft;
            pos[1] -= document.body.offsetTop;
        }
    }
    if (el.parentNode) {
        parent = el.parentNode;
    } else {
        parent = null;
    }
    while (parent && parent.tagName != 'BODY' && parent.tagName != 'HTML') { // account for any scrolled ancestors
        pos[0] -= parent.scrollLeft;
        pos[1] -= parent.scrollTop;
        if (parent.parentNode) {
            parent = parent.parentNode;
        } else {
            parent = null;
        }
    }
    return {
        x: pos[0],
        y: pos[1]
    };
}

/**
 * 是按钮失效，参数button支持id，和object
 *
 */
function disableButton(button, height) {
    height = height || "100%";
    if (!button) {
        return false;
    }

    var el = null;
    if (typeof button == "string") {
        el = document.getElementById(button);
    } else {
        el = button;
    }

    if (!el) {
        return false;
    }

    if (document.readyState != "complete") {
        if (typeof button == "string") {
            window.setTimeout("disableButton('" + button + "')", 2500);
        } else {
            window.setTimeout("disableButton(" + button + ")", 2500);
        }

        return;
    }

    var cDisabled = el.cDisabled;
    cDisabled = (cDisabled != null);
    if (!cDisabled) {
        el.cDisabled = true;

        if (document.getElementsByTagName) {
            var str = "<span style='background: buttonshadow; filter: chroma(color=white) dropshadow(color=buttonhighlight, offx=1, offy=1); height: " + height + ";'>";
            str += "  <span style='filter: mask(color=white); height: " + height + "'>";
            str += el.innerHTML
            str += "  </span>";
            str += "</span>";

            el.innerHTML = str;
        } else {
            el.innerHTML = '<span style="background: buttonshadow; width: 100%; height: 100%; text-align: center;">' + '<span style="filter:Mask(Color=buttonface) DropShadow(Color=buttonhighlight, OffX=1, OffY=1, Positive=0); height: 100%; width: 100%; text-align: center;">' + el.innerHTML + '</span>' + '</span>';
        }

        if (el.onclick != null) {
            el.cDisabled_onclick = el.onclick;
            el.onclick = null;
        }

        if (el.onmouseover != null) {
            el.cDisabled_onmouseover = el.onmouseover;
            el.onmouseover = null;
        }

        if (el.onmouseout != null) {
            el.cDisabled_onmouseout = el.onmouseout;
            el.onmouseout = null;
        }
    }
}

/**
 * 使按钮生效
 */
function enableButton(button) {
    if (!button) {
        return false;
    }

    var el = null;
    if (typeof button == "string") {
        el = document.getElementById(button);
    } else {
        el = button;
    }

    if (!el) {
        return false;
    }

    var cDisabled = el.cDisabled;
    cDisabled = (cDisabled != null);

    if (cDisabled) {
        el.cDisabled = null;
        el.innerHTML = el.children[0].children[0].innerHTML;

        if (el.cDisabled_onclick != null) {
            el.onclick = el.cDisabled_onclick;
            el.cDisabled_onclick = null;
        }

        if (el.cDisabled_onmouseover != null) {
            el.onmouseover = el.cDisabled_onmouseover;
            el.cDisabled_onmouseover = null;
        }

        if (el.cDisabled_onmouseout != null) {
            el.onmouseout = el.cDisabled_onmouseout;
            el.cDisabled_onmouseout = null;
        }

    }
}

/********************************** 表单验证 *****************************************/
/**
 * 常量定义
 */
var formValidate = {
    unCharactor: "\"\\/|><:*?'&%$",
    integerDigits: "10",
    decimalDigits: "0"
}

/**
 * 表单验证
 */
V3X.prototype.checkFormAdvanceAttribute = "";

function checkForm(formObj) {
    var elements = formObj.elements;

    var clearValueElements = [];

    if (elements != null) {
        for (var i = 0; i < elements.length; i++) {
            var e = elements[i];
            var clearValue = e.getAttribute("clearValue");

            if (clearValue == "true") {
                clearValueElements[clearValueElements.length] = e;
                continue;
            }
            V3X.checkFormAdvanceAttribute = e.getAttribute("advance");
            var validateAtt = e.getAttribute("validate");
            if (validateAtt != null && validateAtt != "" && validateAtt != "undefined") {
                var validateFuns = validateAtt.split(",");

                for (var f = 0; f < validateFuns.length; f++) {
                    var fun = validateFuns[f];

                    if (fun) {
                        var result = eval(fun + "(e)");

                        if (!result) {
                            return false;
                        }
                    }
                }
            }
        }
    }

    for (var j = 0; j < clearValueElements.length; j++) {
        clearDefaultValueWhenSubmit(clearValueElements[j]);
    }

    return true;
};

/**
 * 执行正则表达式
 */
function testRegExp(text, re) {
    return new RegExp(re).test(text);
};

/**
 * 在提交的时候，清除掉默认值
 */
function clearDefaultValueWhenSubmit(element) {
    var defaultValue = getDefaultValue(element);

    var v = element.value;

    if (v == defaultValue) {
        element.value = "";
    }
};

/**
 * 打印出提示消息，并聚焦
 */
function writeValidateInfo(element, message) {
    alert(message);

    var onAfterAlert = element.getAttribute("onAfterAlert");
    if (onAfterAlert) {
        try {
            eval(onAfterAlert);
        } catch (e) {
        }
    } else {
        try {
            element.focus();
            element.select();
        } catch (e) {
        }
    }
};

function notSpecChar(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");
    //修改[]之间的内容，其它部分不许修改
    if (/^[^\|\\"'<>]*$/.test(value)) {
        return true;
    } else {
        writeValidateInfo(element, $.i18n("formValidate_specialCharacter", inputName));
        return false;
    }
}

function notSpecCharWithoutApos(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");
    //修改[]之间的内容，其它部分不许修改
    if (/^[^\|\\\/"<>]*$/.test(value)) {
        return true;
    } else {
        writeValidateInfo(element, $.i18n("formValidate_specialCharacter_withoutApos", inputName));
        return false;
    }
}

/**
 * 验证是否为空，不允许空格
 */
function notNull(element) {
    var value = element.value;
    value = value.replace(/[\r\n]/g, "");
    var inputName = element.getAttribute("inputName");

    if (value == null || value == "" || value.trim() == "" || value == $.i18n('section.hasNoSet')) {
        writeValidateInfo(element, $.i18n("formValidate_notNull", inputName));
        return false;
    }

    var maxLength = element.getAttribute("maxSize");

    if (maxLength && value.length > maxLength) {
        writeValidateInfo(element, $.i18n("formValidate_maxLength", inputName, maxLength, value.length));
        return false;
    }

    return true;
};

/**
 * 检测长度
 */
function maxLength(element) {
    var value = element.value;
    if (!value) {
        return true;
    }

    var inputName = element.getAttribute("inputName");

    var maxLength = element.getAttribute("maxSize");

    if (maxLength && value.length > maxLength) {
        writeValidateInfo(element, $.i18n("formValidate_maxLength", inputName, maxLength, value.length));
        return false;
    }

    return true;
};

/**
 *  检测最小长度
 */
function minLength(element) {
    var value = element.value;
    if (!value) {
        return true;
    }

    var inputName = element.getAttribute("inputName");

    var minLength = element.getAttribute("minLength");

    if (minLength && value.length < minLength) {
        writeValidateInfo(element, $.i18n("formValidate_minLength", inputName, minLength, value.length));
        return false;
    }

    return true;
};

/**
 * 是否为数字
 */
function isNumber(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var integerDigits = element.getAttribute("integerDigits") || formValidate.integerDigits;
    var decimalDigits = element.getAttribute("decimalDigits") || formValidate.decimalDigits;
    var integerMax = element.getAttribute("integerMax");
    var integerMin = element.getAttribute("integerMin");

    if (value == "0") {
        return true;
    }

    if (!testRegExp(value, "^-?[0-9]{0," + integerDigits + "}\\.?[0-9]{0," + decimalDigits + "}$")) {
        writeValidateInfo(element, $.i18n("formValidate_isNumber", inputName));
        return false;
    }

    if (integerMax && parseInt(value) > integerMax) {
        writeValidateInfo(element, $.i18n("formValidate_too_max", inputName, integerMax, value));
        return false;
    }

    if (integerMin && parseInt(value) < integerMin) {
        writeValidateInfo(element, $.i18n("formValidate_too_min", inputName, integerMin, value));
        return false;
    }

    return true;
};

/**
 * 校验输入的数字是否为正数，必须大于0
 */
function positive(element) {
    var str = element.value.trim();
    if (str != '') {
        var value = parseFloat(element.value.trim());
        var inputName = element.getAttribute("inputName");
        if (value <= 0) {
            writeValidateInfo(element, $.i18n("formValidate_positive", inputName));
            return false;
        }
    }
    return true;
}

/**
 * 校验输入的百分比，必须在0~100之间
 */
function percent(element) {
    var str = element.value.trim();
    if (str != '') {
        var value = parseFloat(element.value.trim());
        var inputName = element.getAttribute("inputName");
        if (value < 0 || value > 100) {
            writeValidateInfo(element, $.i18n("formValidate_percent", inputName, str));
            return false;
        }
    }
    return true;
}

/**
 *
 */
function notNum(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var integerDigits = element.getAttribute("integerDigits") || formValidate.integerDigits;
    var decimalDigits = element.getAttribute("decimalDigits") || formValidate.decimalDigits;

    if (value == "0") {
        return true;
    }
    if (testRegExp(value, "^-?[0-9]{0," + integerDigits + "}\\.?[0-9]{0," + decimalDigits + "}$")) {
        writeValidateInfo(element, $.i18n("formValidate_isNotNumber", inputName));
        return false;
    }

    return true;


}

/**
 * 检测是否是邮箱
 */
function isEmail(element) {
    var value = element.value;
    if (!value) {
        return true;
    }

    var inputName = element.getAttribute("inputName");

    if (value.indexOf("@") == -1 || value.indexOf(".") == -1) {
        writeValidateInfo(element, $.i18n("formValidate_isEmail", inputName));
        return false;
    }

    return true;
};

/**
 * 验证是否为空，允许空格
 */
function notNullWithoutTrim(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    if (value == null || value == "") {
        writeValidateInfo(element, $.i18n("formValidate_notNull", inputName));
        return false;
    }

    var maxLength = element.getAttribute("maxLength");
    if (maxLength && value.length > maxLength) {
        writeValidateInfo(element, $.i18n("formValidate_maxLength", inputName, maxLength));
        return false;
    }

    return true;
};

/**
 * 验证是否为整数，并验证max和min
 */
function isInteger(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var max = element.getAttribute("max");
    var min = element.getAttribute("min");

    if (value != "0" && (isNaN(value) || value.indexOf("0") == 0 || !testRegExp(value, "^-?[0-9]*$"))) {
        writeValidateInfo(element, $.i18n("formValidate_isInteger", inputName));
        return false;
    }

    if ((max != null && max != "") && (min != null && min != "")) {
        if (parseInt(value) > parseInt(max) || parseInt(value) < parseInt(min)) {
            writeValidateInfo(element, $.i18n("formValidate_isInteger_range", inputName, (min + " - " + max)));
            return false;
        }
    } else {
        if (max != null && max != "" && parseInt(value) > parseInt(max)) {
            writeValidateInfo(element, $.i18n("formValidate_isInteger_max", inputName, max));
            return false;
        }

        if (min != null && min != "" && parseInt(value) < parseInt(min)) {
            writeValidateInfo(element, $.i18n("formValidate_isInteger_min", inputName, min));
            return false;
        }
    }

    return true;
};

/**
 * 是否为正常的字符串，不允许特殊字符，如：/ character
 */
function isWord(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var character = element.getAttribute("character") || formValidate.unCharactor;

    var _c = "";
    for (var i = 0; i < character.length; i++) {
        if (value.indexOf(character.charAt(i)) > -1) {
            _c += character.charAt(i);
        }
    }

    if (_c.length > 0) {
        writeValidateInfo(element, $.i18n("formValidate_isWord", inputName, _c, character));
        return false;
    }

    return true;
};

/**
 * 是否为正常的字符串，不允许特殊字符，如：/ character
 * add by wangchw
 */
function isSepcialWord(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var character = element.getAttribute("character") || "\\!@#$|%^&*?:<>\/\'";

    var _c = "";
    for (var i = 0; i < character.length; i++) {
        if (value.indexOf(character.charAt(i)) > -1) {
            _c += character.charAt(i);
        }
    }

    if (_c.length > 0) {
        writeValidateInfo(element, $.i18n("formValidate_isWord", inputName, _c, character));
        return false;
    }

    return true;
};

/**
 * 是否是数字、字母、下划线
 */
function isCriterionWord(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    if (!testRegExp(value, '^[\\w-]+$')) {
        writeValidateInfo(element, $.i18n("formValidate_isCriterionWord", inputName));
        return false;
    }

    return true;
};

/**
 * 判断是否符合url格式
 */
function isUrl(element) {
    var value = element.value;
    if (!value) {
        return true;
    }
    var inputName = element.getAttribute("inputName");
    //之前：^http://{1}([\w-]+\.)+[\w-]+     匹配http://www.********
    //     ^http://{1}([\\w-]+\.)+[\\w-]+   匹配http://***********
    if (!testRegExp(value, "^http://{1}([\\w-]+\.)+[\\w-]+")) {
        writeValidateInfo(element, $.i18n("formValidate_isUrl", inputName));
        return false;
    }
    return true;
}

/**
 * 以指定文本开头
 */
function startsWith(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var prefix = element.getAttribute("prefix");

    if (value.indexOf(prefix) != 0) { // prefix是扩展的属性
        writeValidateInfo(element, $.i18n("formValidate_startsWith", inputName, prefix));
        return false;
    }

    return true;
};

/**
 * 历史原因,拼写错误,废弃,但能正常运行,请用isDefaultValue
 */
function isDeaultValue(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var deaultValue = getDefaultValue(element);

    if (value == deaultValue) {
        writeValidateInfo(element, $.i18n("formValidate_notNull", inputName));
        return false;
    }

    return true;
};

function isDefaultValue(element) {
    var value = element.value;
    var inputName = element.getAttribute("inputName");

    var defaultValue = getDefaultValue(element);

    if (value == defaultValue) {
        writeValidateInfo(element, $.i18n("formValidate_notNull", inputName));
        return false;
    }

    return true;
};

var UUID_seqence = 0;

//组织模型会用到
var EmptyArrayList = new ArrayList();

/**
 * 产生UUID，返回类型是String
 */
function getUUID() {
    var UUIDConstants_Time = new Date().getTime() + "" + (UUID_seqence++);
    if (UUID_seqence >= 100000) {
        UUID_seqence = 0;
    }

    return UUIDConstants_Time;
}

/**
 * 对ArrayList快速排序
 *
 * @param list 要排序的ArrayList
 * @param comparatorProperies 对数据中元素的某个属性值作为排序依据
 */
function QuickSortArrayList(list, comparatorProperies) {
    QuickSortArray(list.toArray(), comparatorProperies);
}

/**
 * 对数组快速排序
 *
 * @param arr 要排序的数组
 * @param comparatorProperies 对数据中元素的某个属性值作为排序依据
 */
function QuickSortArray(arr, comparatorProperies) {
    if (comparatorProperies) {
        arr.sort(function (o1, o2) {
            return o1[comparatorProperies] < o2[comparatorProperies] ? -1 : (o1[comparatorProperies] == o2[comparatorProperies] ? 0 : 1);
        });
    } else {
        arr.sort();
    }
}

/**********************************************/
/* 以下方法用在上传
/**********************************************/
var fileUploadAttachments = new Properties();
//即时上传 不用长期保留的附件
var fileUploadAttachment = null;
// 上传数量
var fileUploadQuantity = 5;
//显示附件的区域
var attachObject = '';
//显示附件的类型
var atttachTr = '';
//是否显示附件 删除按钮
var attachDelete;
//显示附件的个数的区域
var attachCount = true;
//表单正文中的已经删除的附件
var theHasDeleteAtt = new Properties();
//附件mineType对应a8文件类型
var attFileType = new Properties();

/**
 * 是否上传了附件
 */
function isUploadAttachment() {
    return !fileUploadAttachments.isEmpty();
}

/**
 * attObj 附件显示的区域
 * attachTr 附件类型显示的区域
 * attachDe 是否显示删除按钮
 * attachC  是否显示附件个数
 */
function resetAttachment(attObj, attachTr, attachDe, attachC) {
    attachObject = attObj;
    atttachTr = attachTr;
    attachDelete = attachDe;
    attachCount = attachC;
    fileUploadAttachment = new Properties();
}

function clearUploadAttachments() {
    attachObject = '';
    atttachTr = '';
    attachDelete = null;
    attachCount = true;
    fileUploadAttachment.clear();
    fileUploadAttachment = null;
}

/**
 * 将附件转成input
 */
function saveAttachment(inputObj, saveEditLog) {
    var atts = null;
    if (fileUploadAttachment != null) {
        atts = fileUploadAttachment.values();
    } else {
        atts = fileUploadAttachments.values();
    }

    var attachmentInputsObj = inputObj || document.getElementById("attachmentInputs") || document.getElementById("attachmentEditInputs");


    var attInputStr = "";
    for (var i = 0; i < atts.size(); i++) {
        attInputStr += atts.get(i).toInput();
    }
    if (attachmentInputsObj) {
        attachmentInputsObj.innerHTML = attInputStr;
        //保存编辑后的附件
        if (!saveEditLog || saveEditLog != 'false') {
            if (attActionLog && !fileUploadAttachments.isEmpty()) {
                //              attachmentInputsObj.innerHTML += attActionLog.toInput();
                attachmentInputsObj.innerHTML += "<input type='hidden' name='isEditAttachment' value='1'/>";
            }
        }
    } else {
        alert("Warn: Save attachments unsuccessful")
        return false;
    }

    return true;
}

/**
 * 获取当前组件提交的附件数据。对于一页存在多个上传组件，每个组件所在区域单独处理附件时（即只处理本区域附件，其他区域附件不处理），
 * 使用该方法，可以把该组件上传的附件数据，存放到以组件ID为domainid的区域内。
 * inputDomainId 上传组件id
 */
function saveAttachmentPart(inputDomainId) {
    var atts = null;
    if (fileUploadAttachment != null) {
        atts = fileUploadAttachment.values();
    } else {
        atts = fileUploadAttachments.values();
    }
    if (inputDomainId) {
        var t = $("#" + inputDomainId);
        var tj = $.parseJSON('{' + t.attr("comp") + '}');
        var attachmentTrId = tj.attachmentTrId;
        var attInputStr = "";

        for (var i = 0; i < atts.size(); i++) {
            if (attachmentTrId == atts.get(i).showArea) {
                attInputStr += atts.get(i).toInput();
            }
        }
        t.append($(attInputStr));

    } else {
        alert("ERROR: Incoming parameter error!")
        return false;
    }
    return true;
}

function saveContentAttachment(inputObj) {
    var atts = null;
    if (fileUploadAttachment != null) {
        atts = fileUploadAttachment.values();
    } else {
        atts = fileUploadAttachments.values();
    }

    //删除附件的时候也要能够保存，所以将下面的注释掉

    var attInputStr = "";
    for (var i = 0; i < atts.size(); i++) {
        attInputStr += atts.get(i).toContentInput();
    }

    var attachmentInputsObj = inputObj || parent.detailRightFrame.document.getElementById("contentAttachmentInputs");
    if (attachmentInputsObj) {
        attachmentInputsObj.innerHTML = attInputStr;
        attachmentInputsObj.innerHTML += "<input type='hidden' name='isContentAttchmentChanged' value='1'>";
    } else {
        alert("Warn: Save attachments unsuccessful")
        return false;
    }

    return true;
}

/**
 * 将附件转成input返回
 */
function getAttachmentsToMap() {
    var atts = fileUploadAttachments.values();

    if (!atts || atts.isEmpty()) {
        return true;
    }

    var attInputStr = "";
    for (var i = 0; i < atts.size(); i++) {
        attInputStr += atts.get(i).toMap();
    }
    if (attInputStr != null) {
        return attInputStr;
    }

}

/**
 * 将附件对象转换成数据框
 */
Attachment.prototype.toMap = function () {
    var str = "#attachment_id=" + this.id + ";";
    str += "attachment_reference" + this.reference + ";";
    str += "attachment_subReference=" + this.subReference + ";";
    str += "attachment_category=" + this.category + ";";
    str += "attachment_type=" + this.type + ";";
    str += "attachment_filename=" + escapeStringToHTML(this.filename) + ";";
    str += "attachment_mimeType=" + this.mimeType + ";";
    str += "attachment_createDate=" + this.createDate + ";";
    str += "attachment_size=" + this.size + ";";
    str += "attachment_fileUrl=" + this.fileUrl + ";";
    str += "attachment_description=" + this.description + ";";
    str += "attachment_needClone=" + this.needClone + ";";

    return str;
}

/**
 * 设置附件的是否复制属性
 */
function cloneAllAttachments() {
    var atts = fileUploadAttachments.values();

    for (var i = 0; i < atts.size(); i++) {
        atts.get(i).needClone = true;
    }
}

/**
 * 删除附件
 */
function deleteAttachment(fileUrl, showAlert) {
    var file = fileUploadAttachments.get(fileUrl);
    if (file == null) {
        return;
    }
    if (showAlert != false) {
        if (file.type == 2) {
            if (!confirm($.i18n("assdoc.isdeletesomeone.mesg").format(file.filename))) {
                return 1;
            }
        } else {
            if (!confirm($.i18n("common.isdeletesomeone.label").format(file.filename))) {
                return 1;
            }
        }
    }

    //  editAttachments();
    //  attActionLog = new AttActionLog();

    fileUploadAttachments.remove(fileUrl);
    document.getElementById("attachmentDiv_" + fileUrl).parentNode.removeChild(document.getElementById("attachmentDiv_" + fileUrl));
    //更新附件、关联文档隐藏域
    //因为只记录位置信息，非业务信息，可以不清除

    showAttachmentNumber(file.type, file);

    var num = getFileAttachmentNumber(file.type, file.showArea);
    if (num < 1) {
        if (!(typeof (_updateAttachmentState) != "undefined" && _updateAttachmentState))
            showAtachmentTR(file.type, "none", file.showArea);
    }
    //只有是已发送中递过来的才马上提交。
    var attachmentInputsObj = document.getElementById("attachmentInputs");
    var canUpdateAttachmentFromSended = document.getElementById("canUpdateAttachmentFromSended");
    if (canUpdateAttachmentFromSended && canUpdateAttachmentFromSended.value == "submit")
        updateAttachment('del', attachmentInputsObj);
    //是否执行了删除操作。
    if (typeof (removeChanged) != 'undefined') removeChanged = true;
    ///
    try {
        quoteDocumentFrame.window.deselectItem(fileUrl);
    } catch (e) {
    }
    //在删除时回调
    try {
        var attdiv = "attachment2Area";
        if (file.showArea != null)
            attdiv += file.showArea;
        eval($("#" + attdiv).attr("callMethod"))();
    } catch (e) {
    }
    if (typeof (addScrollForDocument) == "function") {
        addScrollForDocument();
    }
    try {
        var attdiv = "attachmentArea";
        if (file.showArea != null)
            attdiv += file.showArea;
        eval($("#" + attdiv).attr("delCallMethod"))();
    } catch (e) {
    }
}

//删除附件并重置其他数据 -- 用于新闻图片的添加
function deleteAttachmentForImage(fileUrl, showAlert) {
    deleteAttachment(fileUrl, showAlert);
    var imageId = document.getElementById("imageId");
    if (imageId) {
        imageId.value = "";
    }
}

/**
 * locationElementid, 位置id，放置上传组件的位置，比如div的id（"dyncid"）：<div id="dyncid"> </div>
 * applicationCategory, 应用模块id
 * extensions, 文件扩展名，逗号分隔的字符串
 * quantity, 最多上传的文件数
 * isEncrypt, 是否加密
 * callMethod, 回调方法名
 *  attachmentTrId, 同一页多个上传组件的区分id
 *  firstSave，是否先保存附件表信息，默认false：后保存。
 *  atts 附件数据
 *  canDeleteOriginalAtts 是否可删除附件
 *  originalAttsNeedClone 是否需要克隆
 *  takeOver 回调方法是否接管后续逻辑，默认值（null）为接管。
 */
function dymcCreateFileUpload(locationElementid, applicationCategory, extensions, quantity, isEncrypt, callMethod, attachmentTrId, firstSave, canDeleteOriginalAtts, atts, originalAttsNeedClone, takeOver, maxSize, canFavourite) {
    var downloadURL = _ctxPath + "/fileUpload.do?type=" + ((firstSave == null) ? '' : ("&firstSave=" + firstSave)) + "&applicationCategory=" + applicationCategory + "&extensions=" + ((extensions == null) ? '' : extensions) + ((quantity == null) ? '' : ("&quantity=" + quantity)) + ((isEncrypt == null) ? '' : ("&isEncrypt=" + isEncrypt))
        //+ "&popupTitleKey=&isA8geniusAdded=" + isA8geniusAdded
        +
        ((attachmentTrId == null) ? '' : ("&attachmentTrId=" + attachmentTrId)) + ((callMethod == null) ? '' : ("&callMethod=" + callMethod)) + ((maxSize == null) ? '' : ("&maxSize=" + maxSize)) + ((takeOver == null) ? '' : ("&takeOver=" + takeOver));

    //精灵上传附件
    var isA8geniusAdded = false;
    try {
        var ufa = new ActiveXObject('UFIDA_Upload.A8Upload.2');
        ufa.SetLimitFileSize(1024);
        isA8geniusAdded = true;
    } catch (e) {
        isA8geniusAdded = false;
    }
    downloadURL += ((!isA8geniusAdded) ? '' : ("&isA8geniusAdded=" + isA8geniusAdded));

    var showAreaDiv = "<div id='attachmentArea" + ((attachmentTrId == null) ? '' : attachmentTrId) + "' style=\"overflow: auto;\" requrl='" + downloadURL + "'></div>";

    if ($("#downloadFileFrame").length == 0) {
        showAreaDiv = showAreaDiv +
            "<div style=\"display:none;\"><iframe name=\"downloadFileFrame\" id=\"downloadFileFrame\" frameborder=\"0\" width=\"0\" height=\"0\"></iframe></div>";
    }
    $("#" + locationElementid).replaceWith(showAreaDiv);

    parseAttData(atts, attachmentTrId, true, canDeleteOriginalAtts, originalAttsNeedClone, canFavourite);
}

function parseAttData(atts, attachmentTrId, isAtt, canDeleteOriginalAtts, originalAttsNeedClone, canFavourite) {
    if (atts != null && atts != '') {
        atts = $.parseJSON(atts);
    }

    if (atts && atts instanceof Array) {
        var att;
        for (var i = 0; i < atts.length; i++) {
            att = atts[i];
            if (isAtt) {
                if (att.type == 2)
                    continue;
            } else {
                if (att.type != 2)
                    continue;
            }
            if (canFavourite == null)
                canFavourite = true;

            if (attachmentTrId) {
                addAttachmentPoi(att.type, att.filename, att.mimeType,
                    att.createdate ? att.createdate.toString() : null, att.size,
                    att.fileUrl, canDeleteOriginalAtts, originalAttsNeedClone,
                    att.description, att.extension, att.icon, attachmentTrId,
                    att.reference, att.category, false, null, '', true, att.officeTransformEnable, att.v, canFavourite);
            } else {
                addAttachment(att.type, att.filename, att.mimeType,
                    att.createdate ? att.createdate.toString() : null, att.size,
                    att.fileUrl, canDeleteOriginalAtts, originalAttsNeedClone,
                    att.description, att.extension, att.icon, att.reference,
                    att.category, false, null, true, att.officeTransformEnable, att.v, canFavourite);
            }
        }
    }
}

/**
 * locationElementid, 位置id，放置关联组件的位置，比如div的id（"dyncid"）：<div id="dyncid"> </div>
 *  attachmentTrId, 同一页多个关联组件的区分id
 *  atts 关联文档数据
 *  referenceId 只当前页面正在编辑的业务ID,例如，当前正在编辑协同页面，在该页面需要引入关联文档，  此时的referenceId 为正在编辑的协同Id
 *  applicationCategory 当前应用id
 *  modids 应用id字符串为逗号（,）分隔，id值为全系统统一编号
 */
function dymcCreateAssdoc(locationElementid, attachmentTrId, modids, atts, referenceId, applicationCategory) {
    var showAreaDiv = '<div id="attachment2Area' + (attachmentTrId ? attachmentTrId : '') + '" poi="' + (attachmentTrId ? attachmentTrId : '') + '" requestUrl="' + _ctxPath + '/ctp/common/associateddoc/assdocFrame.do?isBind=' + (modids ? modids : '') + '&referenceId=' + (referenceId ? referenceId : '') + '&applicationCategory=' + (applicationCategory ? applicationCategory : '') + '&poi=' + (attachmentTrId ? attachmentTrId : '') + '" style="overflow: auto;"></div>';

    $("#" + locationElementid).replaceWith(showAreaDiv);
    parseAttData(atts, attachmentTrId, false);
}

/**
 * 按钮事件
 */
function insertAttachment(targetAction, importExplain) {
    //  var url = downloadURL + "&quantity=" + fileUploadQuantity;
    //  改成由程序员控制

    //从组件中获取此区域的是上传url
    var url = $("#attachmentArea").attr("requrl");

    /*   在应用模块中实现，不能修改标准过程
     * */
    if (targetAction != null && targetAction != '') {
        url += "&targetAction=" + targetAction;
    }
    //是否显示表单导入规则说明
    if (importExplain) {
        url += "&importExplain=" + importExplain;
    }

    getCtpTop().addattachDialog = null;
    getCtpTop().addattachDialog = getCtpTop().$.dialog({
        title: $.i18n("fileupload.page.title"),
        transParams: {
            'parentWin': window
        },
        url: url,
        width: 400,
        height: 250
    });
}

function preViewDialog(url) {
    getCtpTop().addattachDialog = null;
    getCtpTop().addattachDialog = getCtpTop().$.dialog({
        title: $.i18n("officeTrans.view.label"),
        transParams: {
            'parentWin': window
        },
        url: url,
        width: 1280,
        height: 800
    });
}

/**
 * 打开特定的上传组件
 */
function insertAttachmentPoi(divid) {
    //从组件中获取此区域的是上传url
    var attdiv = "attachmentArea" + divid;
    var url = $("#" + attdiv).attr("requrl");
    getCtpTop().addattachDialog = null;
    getCtpTop().addattachDialog = getCtpTop().$.dialog({
        title: $.i18n("fileupload.page.title"),
        transParams: {
            'parentWin': window
        },
        url: url,
        width: 400,
        height: 250
    });
}


/**
 * 打开关联文档对话框
 */
function quoteDocument(divid) {
    if (v3x.getBrowserFlag('OpenDivWindow') == true) {
        /*    if(isBind !="isBind"){
              isBind="";//流程表单绑定界面不显示协同
            }*/
        var attdiv = "attachment2Area";
        if (divid != undefined)
            attdiv += divid;
        var url = $("#" + attdiv).attr("requestUrl");

        try {
            hideOfficeObj();
        } catch (e) {
        }
        getCtpTop().addassDialog = null;
        getCtpTop().addassDialog = getCtpTop().$.dialog({
            title: $.i18n("common.mydocument.label"),
            transParams: {
                'parentWin': window,
                'divid': divid
            },
            url: url,
            width: 1000,
            height: 800
        });

    }
}

function quoteDocumentCallback(atts, divid) {
    var attdiv = "attachment2Area";
    if (divid != undefined)
        attdiv += divid;

    if (atts) {
        deleteAllAttachment(2, $("#" + attdiv).attr("poi"));
        for (var i = 0; i < atts.length; i++) {
            var att = atts[i]
            //addAttachment(type, filename, mimeType, createDate, size, fileUrl, canDelete, needClone, description)
            if ($("#" + attdiv).attr("poi") == att.showArea) {
                addAttachmentPoi(att.type, att.filename, att.mimeType, att.createDate, att.size, att.fileUrl, true, false, att.description, null, att.mimeType + ".gif", $("#" + attdiv).attr("poi"), att.reference, att.category, null, null, $("#" + attdiv).attr("embedInput"))
            }
        }
        if ($("#" + attdiv).attr("callMethod")) {
            try {
                eval($("#" + attdiv).attr("callMethod"))();
            } catch (e) {
            }
        }
    }
    if (typeof (setCssOverFlow) == 'function') {
        setCssOverFlow();
    }
}

/**
 * 附件对象
 * 特别说明：needClone 是指，该附件需要复制，如：转发协同的原有附件需要复制一份
 */
function Attachment(id, reference, subReference, category, type, filename, mimeType, createDate, size, fileUrl, description, needClone, extension, icon, onlineView, isCanTransform, v) {
    this.id = id;
    this.reference = reference;
    this.subReference = subReference;
    this.category = category;
    this.type = type;
    this.filename = filename;
    this.mimeType = mimeType;
    this.createDate = createDate;
    this.size = size;
    this.fileUrl = fileUrl;
    this.description = description || "";
    this.needClone = needClone;
    this.extension = extension;

    this.icon = icon;

    //office永中转换开关判断参数
    this.isCanTransform = isCanTransform == 'enable' ? true : false;

    this.onlineView = onlineView == null ? true : onlineView;

    this.extReference = ""; //扩展Reference，在保存附件表的时候，Reference字段以这个值为准（前提是不空），否则以接口传来的参数为准
    this.extSubReference = ""; //扩展subReference，在保存附件表的时候，subReference字段以这个值为准（前提是不空），否则以接口传来的参数为准
    this.showArea = "";
    this.embedInput = ""; //内嵌inputid值
    this.hasSaved = false;
    this.v = v;
    this.canFavourite = true;
    this.isShowImg = false;
    this.fileUrl2 = fileUrl; //fileUrl2 用于备份旧的fileUrl，当编辑office时，会产生一个新的fileUrl，此时将旧的fileUrl备份，fileUrl变量中始终存储的是最新值
}

// 打开正文编辑器插入的关联
function openEditorAssociate(id, mimeType, description, reference, category, createDate, filename, v) {

    var url;
    var isDownload = false;
    var moduleId = document.getElementById('moduleId');
    if (moduleId != null) {
        reference = moduleId.value;
    } else if (typeof (summary_id) !== undefined && (typeof (summary_id) !== 'undefined')) {
        reference = summary_id;
    }
    if (typeof (_baseObjectId) !== undefined && (typeof (_baseObjectId) !== 'undefined')) {
        reference = _baseObjectId;
    }
    var moduleType = document.getElementById('moduleType');
    if (moduleType != null) {
        category = moduleType.value;
    } else if (typeof (_baseApp) !== undefined && (typeof (_baseApp) !== 'undefined')) {
        category = _baseApp;
    }
    if (mimeType == "collaboration") {
        url = "collaboration/collaboration.do?method=summary&openFrom=glwd&type=&affairId=" + description + "&baseObjectId=" + reference + "&baseApp=" + category;
    } else if (mimeType == "edoc") {
        url = "edocController.do?method=detailIFrame&from=Done&openFrom=glwd&affairId=" + description + "&isQuote=true&baseObjectId=" + reference + "&baseApp=4"; //+ category;
    } else if (mimeType == "km") {
        url = "doc.do?method=docOpenIframeOnlyId&openFrom=glwd&docResId=" + description + "&baseObjectId=" + reference + "&baseApp=" + category;
    } else if (mimeType == "meeting") {
        url = "mtMeeting.do?method=myDetailFrame&id=" + description + "&isQuote=true&baseObjectId=" + reference + "&baseApp=" + category + "&state=10";
    } else {
        isDownload = true;
        url = 'fileUpload.do?method=download&fileId=' + id + '&createDate=' + createDate.substring(0, 10) + '&filename=' + encodeURI(filename) + '&v=' + v;
    }
    url = url + '&fromEditor=1';
    url = getContextPath() + '/' + url;
    if (isDownload) {
        var downloadFileFrame = document.getElementById('downloadFileFrame');
        if (downloadFileFrame) {
            downloadFileFrame.src = url;
        } else {
            window.open(url, '_blank');
        }
    } else {
        openCtpWindow({
            "url": url
        });
    }
}

function getContextPath() {
    if (typeof (v3x) != "undefined") {
        return v3x.baseURL ? v3x.baseURL : parent._ctxPath;
    }
    return '/seeyon';
}

var sx_variable = {
    detailFrameName: "",
    title: "",
    imgSrc: "",
    count: 0,
    description: "",

    isShow: false
}

/**
 * 上下结构的页面，显示下面的图片、总数、描述
 *
 * @param detailFrameName 下面页面的frame的名称
 * @param title 显示的标题 要国际化哦
 * @param imgSrc 显示的图标名称，统一放在/common/images/detailBannner下，比如：/common/images/detailBannner/101.gif
 * @param count 显示的总数，如果为null或者为负数，表示不显示总数
 * @param description 显示的描述
 */
function showDetailPageBaseInfo(detailFrameName, title, imgSrc, count, description) {
    parent.sx_variable.detailFrameName = detailFrameName;
    parent.sx_variable.title = title;
    parent.sx_variable.imgSrc = imgSrc;
    parent.sx_variable.count = count;
    parent.sx_variable.description = description;

    parent.doDetailPageBaseInfo();
}

function doDetailPageBaseInfo() {
    if (!sx_variable.detailFrameName) {
        return;
    }

    var detailDocument = null;
    try {
        detailDocument = eval(sx_variable.detailFrameName)
    } catch (e) {
    }

    if (detailDocument && detailDocument.document.readyState == "complete") { //下面的页面已经加载完了
        var flag = eval("detailDocument.detailPageBaseInfoFlag");
        if (!flag) {
            detailDocument.location.href = v3x.baseURL + "/common/detail.jsp";
            window.setTimeout("doDetailPageBaseInfo()", 500);
            return;
        }

        detailDocument.document.getElementById("titlePlace").innerHTML = sx_variable.title;
        //        //icon坐标
        //        if(typeof(sx_variable.imgSrc) == 'object'){
        //            var y = parseInt(sx_variable.imgSrc[0],10)-1;
        //            var x = parseInt(sx_variable.imgSrc[1],10)-1;
        //            detailDocument.document.getElementById("imgDiv").innerHTML="<img id=\"img\" alt=\"\" src=\""+v3x.baseURL+"/common/images/space.gif\" class=\"detail-images\" style=\" background-position:-"+ (x*160) +' -' + (y*70) +"\">";
        //        }
        //        else{
        //            detailDocument.document.getElementById("imgDiv").innerHTML="<img id=\"img\" alt=\"\" src=\""+v3x.baseURL + sx_variable.imgSrc+"\" height=\"70\" width=\"160\">";
        //        }

        if (sx_variable.count != null && sx_variable.count >= 0) {
            detailDocument.document.getElementById("countPlace").innerHTML = v3x.getMessage("V3XLang.common_detailPage_count_label", "<span class='countNumber'>" + sx_variable.count + "</span>");
        }

        detailDocument.document.getElementById("descriptionPlace").innerHTML = sx_variable.description || "";

        detailDocument.document.getElementById("allDiv").style.display = "";
    } else {
        window.setTimeout("doDetailPageBaseInfo()", 500);
    }
}

function reloadDetailPageBaseInfo() {
    try {
        parent.doDetailPageBaseInfo();
    } catch (e) {
    }
}


function Set() {
    this.instance = new Array();
    this.key = {}
}

/**
 * var a = new Set();
 * a.add(1);
 * a.add(2);
 * a.add(3);
 * a.add(4);
 * a.add(5, 6, 7, 8, 9);
 */
Set.prototype.add = function () {
    if (arguments == null || arguments.length < 1) {
        throw "arguments is null";
    }

    for (var i = 0; i < arguments.length; i++) {
        var a = arguments[i];
        if (!this.contains(a)) { //存在
            this.instance[this.size()] = a;
            this.key[a] = "A8"; //随便给个值
        }
    }
}

Set.prototype.size = function () {
    return this.instance.length;
}

Set.prototype.contains = function (o) {
    return this.key[o] != null;
}

Set.prototype.isEmpty = function () {
    return this.size() == 0;
}

Set.prototype.clear = function () {
    this.instance = new Array();
    this.key = {}
}

Set.prototype.get = function (index) {
    if (this.isEmpty()) {
        return null;
    }

    if (index > this.size()) {
        return null;
    }

    return this.instance[index];
}

Set.prototype.toArray = function () {
    return this.instance;
}
Set.prototype.toString = function () {
    return this.instance.join(', ');
}


function clearOfficeFlag() {
    try {
        var ua = navigator.userAgent;
        var isMSIE = (navigator.appName == "Microsoft Internet Explorer") || ua.indexOf('Trident') != -1;
        //      if(isMSIE)return; //当有弹出窗时， 针对office正文需要在ie下进行隐藏
        var _tpWin = getA8Top();
        _tpWin.isOffice = false;
        _tpWin.officeObj = null;
    } catch (e) {
    }
}

function setOfficeFlag(flag, offObj) {
    try {
        var ua = navigator.userAgent;
        var isMSIE = (navigator.appName == "Microsoft Internet Explorer") || ua.indexOf('Trident') != -1;
        if (isMSIE) { //ie 下 当有弹出窗时，针对office正文进行隐藏
            var _tpWin = getA8Top();
            _tpWin.isOffice = flag;
            _tpWin.officeObj = [];
            _tpWin.officeObj.push(offObj);
            return;
        }
        var _tpWin = getA8Top();
        _tpWin.isOffice = flag;
        if (_tpWin.officeObj && typeof _tpWin.officeObj == 'object' && '[object Array]' == Object.prototype.toString.call(_tpWin.officeObj)) {
            _tpWin.officeObj.push(offObj);
        } else {
            _tpWin.officeObj = [];
            _tpWin.officeObj.push(offObj);
        }
    } catch (e) {
    }
}

function hideOfficeObj() {
    try {
        var ua = navigator.userAgent;
        var isMSIE = (navigator.appName == "Microsoft Internet Explorer") || ua.indexOf('Trident') != -1;
        // if(isMSIE)return;
        var _tpWin = getA8Top();
        if (_tpWin.isOffice && _tpWin.officeObj && _tpWin.officeObj.length > 0) {
            for (var i = 0; i < _tpWin.officeObj.length; i++) {
                var _temp = _tpWin.officeObj[i];
                if (_temp && _temp.style) {
                    try {
                        if (isMSIE) {
                            _temp.Hide(0);
                        } else {
                            _temp.HidePlugin(0);
                        }
                    } catch (e) {
                    }
                }
            }
        }
    } catch (e) {
    }
}

/**
 * 自定义 显示或者隐藏offfice控件
 */
var OfficeObjExt = {
    iframeId: null,
    /**
     * 提供对弹出窗中的office正文 页面中，再次弹出窗 显示office正文的接口，提供外部重写
     */
    showDialogOffice: function () {
    },
    /**
     * 提供使用office控件的jsp页面中脚本调用,目的是将office所在的iframe的id引入，如果jsp中没有重写OfficeObjExt.showExt函数，将
     * 按照默认方法执行显示或者隐藏office控件,必须调用
     */
    setIframeId: function (id) {
        OfficeObjExt.iframeId = id;
    },
    firstHeight: null,
    showExt: function () {
        //var iframe = document.getElementById("zwIframe");
        if (OfficeObjExt.iframeId == null) {
            return;
        }
        //alert(OfficeObjExt.iframeId);
        var iframe = document.getElementById(OfficeObjExt.iframeId);

        var h;
        if (OfficeObjExt.firstHeight == null) {
            h = iframe.style.height;
            OfficeObjExt.firstHeight = h;
        } else {
            h = OfficeObjExt.firstHeight;
        }
        h = h + "";
        //alert(h);
        var height = h;
        if (h.indexOf("%") > 0) {
            height = h.substring(0, h.length - 1);
            height = parseInt(height);
            height = height - 2;
            iframe.style.height = height + "%";
        } else if (h.indexOf("px") > 0) {
            height = h.substring(0, h.length - 2);
            height = parseInt(height);
            height = height - 2;
            iframe.style.height = height + "px";
        } else {

            h = $(iframe).height();
            OfficeObjExt.firstHeight = h + "px";
            iframe.style.height = (h - 2) + "px";
        }
        window.setTimeout(function () {
            iframe.style.height = h;
        }, 2);
    },
    /**
     * 公共显示 OfficeObject的方法
     firstAttr: 指定能唯一对应Office Object 的key值，该值在第一次渲染时会进行高度获取，并缓存高度，第二次渲染回填原值
     iframe: 可以指定Office Object的对象，也可以指定为Office Object的上层Iframe对象
     callback
     */
    showIfame: function (cfg) {
        var firstAttr, iframe, callback;
        firstAttr = cfg.firstAttr;
        iframe = cfg.iframe;
        callback = cfg.callback || function () {
        };
        var h;
        if (OfficeObjExt[firstAttr] == null) {
            h = iframe.style.height;
            OfficeObjExt[firstAttr] = h;
        } else {
            h = OfficeObjExt[firstAttr];
        }
        h = h + "";
        //alert(h);
        var height = h;
        if (h.indexOf("%") > 0) {
            height = h.substring(0, h.length - 1);
            height = parseInt(height);
            height = height - 2;
            iframe.style.height = height + "%";
        } else if (h.indexOf("px") > 0) {
            height = h.substring(0, h.length - 2);
            height = parseInt(height);
            height = height - 2;
            iframe.style.height = height + "px";
        } else {
            h = $(iframe).height();
            OfficeObjExt[firstAttr] = h + "px";
            iframe.style.height = (h - 2) + "px";
        }
        window.setTimeout(function () {
            //iframe.style.height = h;
            window.setTimeout(cfg.callback, 1);
        }, 2);
    }
};

function showOfficeObj() {
    try {
        // var ua = navigator.userAgent;
        // var isMSIE = (navigator.appName == "Microsoft Internet Explorer")||ua.indexOf('Trident')!=-1;
        // if(isMSIE)return;
        var isChrome = navigator.userAgent.toLowerCase().match(/chrome/) != null;
        var _tpWin = getCtpTop();
        //判断是否存在窗口 如果窗口存在则不能显示office
        if (_tpWin.$(".layui-layer").length > 0 || _tpWin.$(".dialog_main").length > 0) {
            return void (0);
        }
        try {
            if (_tpWin.$('.shield').size() >= 1) {
                return
            }
        } catch (e) {

        }

        try {
            if ((_tpWin.$('.mask').size() > 0 && _tpWin.$('.mask').css('display') != 'none') || (_tpWin.$('.shield').size() > 0 && _tpWin.$('.shield').css('display') != 'none')) {
                if (typeof OfficeObjExt.showDialogOffice == 'undefined') {
                    return;
                }
                OfficeObjExt.showDialogOffice();
                // return;
            }
        } catch (e) {
        }
        if (_tpWin.isOffice && _tpWin.officeObj && _tpWin.officeObj.length > 0) {
            for (var i = 0; i < _tpWin.officeObj.length; i++) {
                var _temp = _tpWin.officeObj[i];
                if (_temp && _temp.style) {
                    try {
                        if (isChrome) {
                            _temp.HidePlugin(1);
                            _temp.FuncExtModule.ShowToolBar = "1";
                        } else {
                            _temp.Hide(1);
                        }
                    } catch (e) {
                    }
                }
            }
        }
        if (isChrome) {
            window.setTimeout(OfficeObjExt.showExt, 50);
        }
    } catch (e) {
    }
}

/* 公共JS-摘至v3x-debug.js： end */

/* 公共JS-摘至v3x.js： begin */
/**
 * 获取QueryString参数
 */
function getParameter(name1) {
    var queryString = document.location.search;

    if (queryString) {
        queryString = queryString.substring(1);

        var params = queryString.split("&");

        for (var i = 0; i < params.length; i++) {
            var items = params[i].split("=");

            if (name1 == items[0]) {
                return items[1];
            }
        }
    }
}

/* 公共JS-摘至v3x.js： end */

/*--摘至seeyon.ui.arraylist-debug.js begin--*/

/**
 * @author macj
 */
/**
 * StringStringBuffer对象
 */
function StringBuffer() {
    this._strings_ = new Array();
}

StringBuffer.prototype.append = function (str) {
    if (str) {
        if (str instanceof Array) {
            this._strings_ = this._strings_.concat(str);
        } else {
            this._strings_[this._strings_.length] = str;
        }
    }

    return this;
}
StringBuffer.prototype.reset = function (newStr) {
    this.clear();
    this.append(newStr);
}
StringBuffer.prototype.clear = function () {
    this._strings_ = new Array();
}
StringBuffer.prototype.isBlank = function () {
    return this._strings_.length == 0;
}

StringBuffer.prototype.toString = function (sp) {
    sp = sp == null ? "" : sp;
    if (this._strings_.length == 0)
        return "";
    return this._strings_.join(sp);
}
String.prototype.getBytesLength = function () {
    var cArr = this.match(/[^\x00-\xff]/ig);
    return this.length + (cArr == null ? 0 : cArr.length);
};

String.prototype.getSBCCaseLength = function () {
    var cArr = this.match(/[^\x00-\xff]/ig);
    var CBytelength = (cArr == null ? 0 : cArr.length);
    return (this.length - CBytelength) * 0.75 + CBytelength;
}

String.prototype.getLimitLength = function (maxlengh, symbol) {
    if (!maxlengh || maxlengh < 0) {
        return this;
    }
    var len = this.getBytesLength();

    if (len <= maxlengh) {
        return this;
    }

    symbol = symbol == null ? ".." : symbol;
    maxlengh = maxlengh - symbol.length;

    var a = 0;
    var temp = '';

    for (var i = 0; i < this.length; i++) {
        if (this.charCodeAt(i) > 255)
            a += 2;
        else
            a++;

        temp += this.charAt(i);

        if (a >= maxlengh) {
            return temp + symbol;
        }
    }

    return this;
};

String.prototype.escapeHTML = function (isEscapeSpace, isEscapeBr) {
    try {
        return escapeStringToHTML(this, isEscapeSpace, isEscapeBr);
    } catch (e) {
    }

    return this;
};
String.prototype.escapeHTMLWithoutBr = function (isEscapeSpace) {
    try {
        return escapeStringToHTMLWithoutBr(this, isEscapeSpace);
    } catch (e) {
    }

    return this;
};
String.prototype.escapeJavascript = function () {
    return escapeStringToJavascript(this);
};

String.prototype.escapeSpace = function () {
    return this.replace(/ /g, "&nbsp;");
};

String.prototype.escapeSameWidthSpace = function () {
    return this.replace(/ /g, "&nbsp;&nbsp;");
};

String.prototype.escapeXML = function () {
    return this.replace(/\&/g, "&amp;").replace(/\</g, "&lt;").replace(/\>/g, "&gt;").replace(/\"/g, "&quot;");
};
String.prototype.escapeQuot = function () {
    return this.replace(/\'/g, "&#039;").replace(/"/g, "&#034;");
};
String.prototype.startsWith = function (prefix) {
    return this.indexOf(prefix) == 0;
};
String.prototype.endsWith = function (subfix) {
    var pos = this.indexOf(subfix);
    return pos > -1 && pos == this.length - subfix.length;
};

/**
 * 去掉空格
 */
String.prototype.trim = function () {
    var chs = this.toCharArray();

    var st = 0;
    var off = chs.length;

    for (var i = 0; i < chs.length; i++) {
        var c = chs[i];
        if (c == ' ') {
            st++;
        } else {
            break;
        }
    }

    if (st == this.length) {
        return "";
    }

    for (var i = chs.length; i > 0; i--) {
        var c = chs[i - 1];
        if (c == ' ') {
            off--;
        } else {
            break;
        }
    }

    return this.substring(st, off);
};

/**
 * 将字符串转成数组
 */
String.prototype.toCharArray = function () {
    var array = [];

    for (var i = 0; i < this.length; i++) {
        array[i] = this.charAt(i);
    }

    return array;
};

//Array扩展
Array.prototype.indexOf = function (object) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == object) {
            return i;
        }
    }
    return -1;
}

/**
 * ArrayList like java.util.ArrayList
 */
function ArrayList() {
    this.instance = new Array();
}

ArrayList.prototype.size = function () {
    return this.instance.length;
}
/**
 * 在末尾追加一个
 */
ArrayList.prototype.add = function (o) {
    this.instance[this.instance.length] = o;
}
/**
 * 当list中不存在该对象时才添加
 */
ArrayList.prototype.addSingle = function (o) {
    if (!this.contains(o)) {
        this.instance[this.instance.length] = o;
    }
}
/**
 * 在指定位置增加元素
 * @param posation 位置， 从0开始
 * @param o 要增加的元素
 */
ArrayList.prototype.addAt = function (position, o) {
    if (position >= this.size() || position < 0 || this.isEmpty()) {
        this.add(o);
        return;
    }

    this.instance.splice(position, 0, o);
}

/**
 * Appends all of the elements in the specified Collection to the end of
 * this list, in the order that they are returned by the
 * specified Collection's Iterator.  The behavior of this operation is
 * undefined if the specified Collection is modified while the operation
 * is in progress.  (This implies that the behavior of this call is
 * undefined if the specified Collection is this list, and this
 * list is nonempty.)
 */
ArrayList.prototype.addAll = function (array) {
    if (!array || array.length < 1) {
        return;
    }

    this.instance = this.instance.concat(array);
}

/**
 * 追加一个List在队尾
 */
ArrayList.prototype.addList = function (list) {
    if (list && list instanceof ArrayList && !list.isEmpty()) {
        this.instance = this.instance.concat(list.instance);
    }
}

/**
 * @return the element at the specified position in this list.
 */
ArrayList.prototype.get = function (index) {
    if (this.isEmpty()) {
        return null;
    }

    if (index > this.size()) {
        return null;
    }

    return this.instance[index];
}

/**
 * 最后一个
 */
ArrayList.prototype.getLast = function () {
    if (this.size() < 1) {
        return null;
    }

    return this.instance[this.size() - 1];
}

/**
 * Replace the element at the specified position in the list with the specified element
 * @param index int index of element to replace
 * @param obj Object element to be stored at the specified posotion
 * @return Object the element previously at the specified posotion
 * @throws IndexOutOfBoundException if index out of range
 */
ArrayList.prototype.set = function (index, obj) {
    if (index >= this.size()) {
        throw "IndexOutOfBoundException : Index " + index + ", Size " + this.size();
    }

    var oldValue = this.instance[index];
    this.instance[index] = obj;

    return oldValue;
}

/**
 * Removes the element at the specified position in this list.
 * Shifts any subsequent elements to the left (subtracts one from their
 * indices).
 */
ArrayList.prototype.removeElementAt = function (index) {
    if (index > this.size() || index < 0) {
        return;
    }

    this.instance.splice(index, 1);
}
/**
 * Removes the element in this list.
 */
ArrayList.prototype.remove = function (o) {
    var index = this.indexOf(o);
    this.removeElementAt(index);
}
/**
 * @return <tt>true</tt> if this list contains the specified element.
 */
ArrayList.prototype.contains = function (o, comparatorProperies) {
    return this.indexOf(o, comparatorProperies) > -1;
}
/**
 * Searches for the first occurence of the given argument, testing
 * for equality using the <tt>==</tt> method.
 */
ArrayList.prototype.indexOf = function (o, comparatorProperies) {
    for (var i = 0; i < this.size(); i++) {
        var s = this.instance[i];
        if (s == o) {
            return i;
        } else if (comparatorProperies != null && s != null && o != null && s[comparatorProperies] == o[comparatorProperies]) {
            return i;
        }
    }

    return -1;
}
/**
 * Returns the index of the last occurrence of the specified object in this list.
 * @return the index of the last occurrence of the specified object in this list;
 *         returns -1 if the object is not found.
 */
ArrayList.prototype.lastIndexOf = function (o, comparatorProperies) {
    for (var i = this.size() - 1; i >= 0; i--) {
        var s = this.instance[i];
        if (s == o) {
            return i;
        } else if (comparatorProperies != null && s != null && o != null && s[comparatorProperies] == o[comparatorProperies]) {
            return i;
        }
    }

    return -1;
}

/**
 * Returns a view of the portion of this list between
 * fromIndex, inclusive, and toIndex, exclusive.
 * @return a view of the specified range within this list.
 */
ArrayList.prototype.subList = function (fromIndex, toIndex) {
    if (fromIndex < 0) {
        fromIndex = 0;
    }

    if (toIndex > this.size()) {
        toIndex = this.size();
    }

    var tempArray = this.instance.slice(fromIndex, toIndex);

    var temp = new ArrayList();
    temp.addAll(tempArray);

    return temp;
}
/**
 * Returns an array containing all of the elements in this list in the correct order;
 *
 * @return Array
 */
ArrayList.prototype.toArray = function () {
    return this.instance;
}

/**
 * Tests if this list has no elements.
 *
 * @return <tt>true</tt> if this list has no elements;
 */
ArrayList.prototype.isEmpty = function () {
    return this.size() == 0;
}
/**
 * Removes all of the elements from this list.  The list will
 * be empty after this call returns.
 */
ArrayList.prototype.clear = function () {
    this.instance = new Array();
}
/**
 * show all elements
 */
ArrayList.prototype.toString = function (sep) {
    sep = sep || ", ";
    return this.instance.join(sep);
}

/**
 *
 */
function Properties(jsProps) {
    this.instanceKeys = new ArrayList();
    this.instance = {};

    if (jsProps) {
        this.instance = jsProps;
        for (var i in jsProps) {
            this.instanceKeys.add(i);
        }
    }
}

/**
 * Returns the number of keys in this Properties.
 * @return int
 */
Properties.prototype.size = function () {
    return this.instanceKeys.size();
}

/**
 * Returns the value to which the specified key is mapped in this Properties.
 * @return value
 */
Properties.prototype.get = function (key, defaultValue) {
    if (key == null) {
        return null;
    }

    var returnValue = this.instance[key];

    if (returnValue == null && defaultValue != null) {
        return defaultValue;
    }

    return returnValue;
}
/**
 * Removes the key (and its corresponding value) from this
 * Properties. This method does nothing if the key is not in the Properties.
 */
Properties.prototype.remove = function (key) {
    if (key == null) {
        return null;
    }
    this.instanceKeys.remove(key);
    delete this.instance[key]
}
/**
 * Maps the specified <code>key</code> to the specified
 * <code>value</code> in this Properties. Neither the key nor the
 * value can be <code>null</code>. <p>
 *
 * The value can be retrieved by calling the <code>get</code> method
 * with a key that is equal to the original key.
 */
Properties.prototype.put = function (key, value) {
    if (key == null) {
        return null;
    }

    if (this.instance[key] == null) {
        this.instanceKeys.add(key);
    }

    this.instance[key] = value;
}

/**
 * 直接追加，不考虑重复key
 */
Properties.prototype.putRef = function (key, value) {
    if (key == null) {
        return null;
    }

    this.instanceKeys.add(key);
    this.instance[key] = value;
}

/**
 * Returns the value to which the specified value is mapped in this Properties.
 * e.g:
 * userinfo.getMultilevel("department.name")  the same sa :  userinfo.get("department").get("name")
 * @return string
 */
Properties.prototype.getMultilevel = function (key, defaultValue) {
    if (key == null) {
        return null;
    }

    var _keys = key.split(".");

    function getObject(obj, keys, i) {
        try {
            if (obj == null || (typeof obj != "object")) {
                return null;
            }

            var obj1 = obj.get(keys[i]);

            if (i < keys.length - 1) {
                obj1 = getObject(obj1, keys, i + 1);
            }

            return obj1;
        } catch (e) {
        }

        return null;
    }

    var returnValue = getObject(this, _keys, 0);

    return returnValue == null ? defaultValue : returnValue;
}

/**
 * Tests if the specified object is a key in this Properties.
 * @return boolean
 */
Properties.prototype.containsKey = function (key) {
    if (key == null) {
        return false;
    }

    return this.instance[key] != null;
}

/**
 * Returns an ArrayList of the keys in this Properties.
 * @return ArrayList
 */
Properties.prototype.keys = function () {
    return this.instanceKeys;
}

/**
 * Returns an ArrayList of the values in this Properties.
 * @return ArrayList
 */
Properties.prototype.values = function () {
    var vs = new ArrayList();
    for (var i = 0; i < this.instanceKeys.size(); i++) {
        var key = this.instanceKeys.get(i);

        if (key) {
            var value = this.instance[key];
            vs.add(value);
        }
    }

    return vs;
}

/**
 * Tests if this Properties maps no keys to values.
 * @return boolean
 */
Properties.prototype.isEmpty = function () {
    return this.instanceKeys.isEmpty();
}

/**
 * Clears this Properties so that it contains no keys.
 */
Properties.prototype.clear = function () {
    this.instanceKeys.clear();
    this.instance = {};
}
/**
 * exchange entry1(key1-value1) with entry2(key2-value2)
 */
Properties.prototype.swap = function (key1, key2) {
    if (!key1 || !key2 || key1 == key2) {
        return;
    }

    var index1 = -1;
    var index2 = -1;

    for (var i = 0; i < this.instanceKeys.instance.length; i++) {
        if (this.instanceKeys.instance[i] == key1) {
            index1 = i;
        } else if (this.instanceKeys.instance[i] == key2) {
            index2 = i;
        }
    }

    this.instanceKeys.instance[index1] = key2;
    this.instanceKeys.instance[index2] = key1;
}

Properties.prototype.entrySet = function () {
    var result = [];

    for (var i = 0; i < this.instanceKeys.size(); i++) {
        var key = this.instanceKeys.get(i);
        var value = this.instance[key];

        if (!key) {
            continue;
        }

        var o = new Object();
        o.key = key;
        o.value = value;

        result[result.length] = o;
    }

    return result;
}

/**
 *
 */
Properties.prototype.toString = function () {
    var str = "";

    for (var i = 0; i < this.instanceKeys.size(); i++) {
        var key = this.instanceKeys.get(i);
        str += key + "=" + this.instance[key] + "\n";
    }

    return str;
}
/**
 * 转换成key1=value1;key2=value2;的形式
 * token1 -- 对应第一层分隔符  如上式的";"
 * token2 -- 对应第二层分隔符  如上式的"="
 */
Properties.prototype.toStringTokenizer = function (token1, token2) {
    token1 = token1 == null ? ";" : token1;
    token2 = token2 == null ? "=" : token2;
    var str = "";

    for (var i = 0; i < this.instanceKeys.size(); i++) {
        var key = this.instanceKeys.get(i);
        var value = this.instance[key];

        if (!key) {
            continue;
        }

        if (i > 0) {
            str += token1;
        }
        str += key + token2 + value;
    }

    return str;
}

Properties.prototype.toQueryString = function () {
    if (this.size() < 1) {
        return "";
    }

    var str = "";
    for (var i = 0; i < this.instanceKeys.size(); i++) {
        var key = this.instanceKeys.get(i);
        var value = this.instance[key];

        if (!key) {
            continue;
        }

        if (i > 0) {
            str += "&";
        }

        if (typeof value == "object") {

        } else {
            str += key + "=" + encodeURIat(value);
        }
    }

    return str;
}

/*--摘至seeyon.ui.arraylist-debug.js end--*/


/*--摘至jquery.comp-debug.js start--*/
$.messageBox = function (options) {
    return new MxtMsgBox(options);
};

$.alert = function (msg) {
    var options = null;
    if (typeof (msg) == "object") {
        options = msg;
    }
    options = options == null ? {} : options;
    options.title = options.title ? options.title : $.i18n('system.prompt.js');
    options.type = options.type ? options.type : 0;
    options.imgType = options.imgType ? options.imgType : 2;
    options.close_fn = options.close_fn ? options.close_fn : null;
    if (typeof (msg) != "object") {
        options.msg = msg;
    }

    return new MxtMsgBox(options);
};
$.infor = function (msg) {
    var options = null;
    if (typeof (msg) == "object") {
        options = msg;
    }
    var options = options == undefined ? {} : options;
    options.title = $.i18n('system.prompt.js');
    options.type = 0;
    if (typeof (msg) != "object") {
        options.msg = msg;
    }
    options.imgType = options.imgType ? options.imgType : 0;
    options.close_fn = options.close_fn ? options.close_fn : null;

    return new MxtMsgBox(options);
};
$.confirm = function (options) {
    var options = options == undefined ? {} : options;
    // options.title = options.title ? options.title : $.i18n('system.prompt.js');
    options.title = options.title ? options.title : $.i18n('system.prompt.js');
    options.type = 1;
    options.imgType = options.imgType ? options.imgType : 4;
    options.close_fn = options.close_fn ? options.close_fn : null;

    return new MxtMsgBox(options);
};
$.error = function (msg) {
    var options = null;
    if (typeof (msg) == "object") {
        options = msg;
    }
    var options = options == undefined ? {} : options;
    options.title = options.title ? options.title : $.i18n('system.prompt.js');
    options.type = options.type ? options.type : 0;
    options.imgType = options.imgType ? options.imgType : 1;
    options.close_fn = options.close_fn ? options.close_fn : null;
    if (typeof (msg) != "object") {
        options.msg = msg;
    }
    return new MxtMsgBox(options);
};

$.dialog = function (options) {

    // return new MxtDialog(options);
    var optionsTargetWindow = options.targetWindow;
    if (!options.targetWindow) { //targetWindow未定义的情况下取getCtpTop()，建议必须定义
        optionsTargetWindow = getCtpTop();
    }

    //dialog存在复制dom---存在所在window复制targetWindow显示与只在所在window复制与显示两种情况
    if (!(options.url) && !(options.type) && options.htmlId) {
        if (!options.targetWindow) { //只在所在window复制与显示
            optionsTargetWindow = window;
            options.contentCopyWindow = window; //复制dom的情况增加参数记录dom所在的window
        } else { //所在window复制targetWindow显示
            options.contentCopyWindow = window;
        }
    }
    //panel复制dom---只在所在window复制与显示
    if (options.type == "panel" && options.htmlId) {
        optionsTargetWindow = window;
        options.contentCopyWindow = window;
    }
    if (options.type == "panel" && options.targetId && options.url) {
        optionsTargetWindow = window;
        options.contentCopyWindow = window;
    }
    if (options.type == "panel" && options.targetId && options.html) {
        optionsTargetWindow = window;
        options.contentCopyWindow = window;
    }
    //赋值为正确的targetWindow
    options.targetWindow = optionsTargetWindow;

    if (typeof (getCtpTop().isVJTop) != "undefined" && getCtpTop().isVJTop != null) {
        getCtpTop().vjOpenDialog(options);
    } else {
        if (options.targetWindow.layer) {
            return options.targetWindow.layer.open(options); //调用相应窗口的layer，由于layer是全局定义，每个window的layer都是独立的
        } else {
            return window.layer.open(options);
        }
    }
};

$.selectPeople = function (options) {
    var settings = {
        mode: 'div'
    };

    function cloneArray(ary) {
        var newAry = [];
        for (var i = 0; i < ary.length; i++) {
            if (Object.prototype.toString.call(ary[i]) == '[object Array]') {
                newAry.push(cloneArray(ary[i]));
            } else {
                newAry.push(ary[i]);
            }
        }
        return newAry;
    }

    options._window = window;
    options = $.extend(settings, options);
    var onlyShowChildrenAccount = options.onlyShowChildrenAccount;
    var url = _ctxPath + '/selectpeople.do?onlyShowChildrenAccount=' + onlyShowChildrenAccount,
        ret;
    if (options.mode == 'modal') {
        if (options.preCallback)
            options.preCallback(options);
        // 弹出新窗口
        var retv = window.showModalDialog(url + "&isFromModel=true", options, 'dialogWidth=708px;dialogHeight=568px');
        if (retv != null && (typeof retv == "object")) {
            retv.obj = cloneArray(retv.obj);
        } else if (retv == -1) {
            return;
        }
        if (retv) {
            ret = retv;
            if (options.callback)
                options.callback(retv, options);
        }
    } else {
        if (options.preCallback)
            options.preCallback(options);
        var dialog = $
            .dialog({
                id: "SelectPeopleDialog",
                url: url,
                width: 820,
                height: 506,
                title: $.i18n("selectPeople.page.title"),
                checkMax: true,
                transParams: options,
                closeParam: {
                    show: true,
                    autoClose: true,
                    handler: function () {
                        if (options.canclecallback) {
                            options.canclecallback();
                        }
                    }
                },
                targetWindow: getCtpTop(),
                buttons: [{
                    text: $.i18n('common.button.ok.label'),
                    isEmphasize: true,
                    handler: function () {
                        var retv = dialog.getReturnValue(),
                            cl = true;
                        if (retv == -1) {
                            return;
                        }
                        if (retv) {
                            if (options.callbk && options.callbk(retv))
                                cl = false;
                            if (options.callback && options.callback(retv, options))
                                cl = false;
                        }
                        if (cl) {
                            var retvalue = "";
                            var count = 0;
                            var retvalueDate = retv.value.split(",");
                            for (var i = 0; i < retvalueDate.length; i++) {
                                var retvalueItem = retvalueDate[i];
                                if (retvalueItem.indexOf("Member") == 0) {
                                    if (retvalue == "") {
                                        retvalue = retvalueItem;
                                    } else {
                                        retvalue = retvalue + "," + retvalueItem;
                                    }
                                    count++;
                                    if (count >= 30) {
                                        break;
                                    }
                                }
                            }
                            $.ajax({
                                type: "POST",
                                beforeSend: CsrfGuard.beforeAjaxSend,
                                url: encodeURI("/seeyon/organization/orgIndexController.do?method=saveRecentData4OrgIndex&rData=" + retvalue)
                            });
                            dialog.close(dialog.index);
                        }
                    }
                }, {
                    text: $.i18n('common.button.cancel.label'),
                    handler: function () {
                        if (options.canclecallback) {
                            options.canclecallback();
                        }
                        dialog.close();
                    }
                }],

                bottomHTML: "<table id=\"flowTypeDiv\" class=\"hidden\" width=\"\" border=\"0\" height=\"30\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\r\n" + "  <tr>\r\n" + "    <td id=\"concurrentType\">&nbsp;&nbsp;&nbsp;&nbsp;\r\n" + "      <label for=\"concurrent\">\r\n" + "        <input id=\"concurrent\" name=\"flowtype\" type=\"radio\" value=\"1\" checked>&nbsp;<span>" + $.i18n("selectPeople.flowtype.concurrent.lable") + "</span>\r\n" + "      </label>&nbsp;&nbsp;&nbsp;\r\n" + "    </td>\r\n" + "    <td id=\"sequenceType\">\r\n" + "      <label for=\"sequence\">\r\n" + "        <input id=\"sequence\" name=\"flowtype\" type=\"radio\" value=\"0\">&nbsp;<span>" + $.i18n("selectPeople.flowtype.sequence.lable") + "</span>\r\n" + "      </label>&nbsp;&nbsp;&nbsp;\r\n" + "    </td>\r\n" + "    <td id=\"multipleType\">\r\n" + "      <label for=\"multiple\">\r\n" + "        <input id=\"multiple\" name=\"flowtype\" type=\"radio\" value=\"2\">&nbsp;<span>" + $.i18n("selectPeople.flowtype.multiple.lable") + "</span>\r\n" + "      </label>&nbsp;&nbsp;&nbsp;\r\n" + "    </td>\r\n" + "    <td id=\"colAssignType\">\r\n" + "      <label for=\"colAssign\">\r\n" + "        <input id=\"colAssign\" name=\"flowtype\" type=\"radio\" value=\"3\">&nbsp;<span>" + $.i18n("selectPeople.flowtype.colAssign.lable") + "</span>\r\n" + "      </label>\r\n" + "    </td>\r\n" + "  </tr>\r\n" + "</table>"
            });
    }
    return ret;
};

$.progressBar = function (options) {
    if (options == undefined) {
        options = {}
    }
    return new MxtProgressBar(options)
}
$.i18n = function (key) {
    function sleep(milliseconds) {
        var start = new Date().getTime();
        for (var i = 0; i < 1e7; i++) {
            if ((new Date().getTime() - start) > milliseconds) {
                break;
            }
        }
    }

    function getLocale() {
        if (typeof _locale === 'undefined') {
            if (typeof vPortal === 'undefined') {
                return 'zh_CN';
            } else {
                return vPortal.locale;
            }
        } else {
            return _locale;
        }
    }

    try {
        var msg = '';
        if (window.localStorage) {
            msg = window.localStorage.getItem('i18n_' + key);
            if (msg == null) {
                var data = window.localStorage.getItem("i18n_DATA");
                if (data == null || (window.localStorage.getItem('i18n_LASTUPDATE_TIMESTAMP') == null)) {
                    $.ajax({
                        url: "/seeyon/i18n_init_" + getLocale() + ".js",
                        async: false,
                        beforeSend: CsrfGuard.beforeAjaxSend,
                        dataType: "script",
                        cache: true
                    });
                }
                var k = '|_' + key + ',';
                var firstIndex = data.indexOf(k);
                if (firstIndex > -1) {
                    var lastIndex = data.indexOf("_|", firstIndex);
                    msg = data.substring(firstIndex + k.length, lastIndex);
                    localStorage.setItem('i18n_' + key, msg);
                }
                data = null;
            }
        } else {
            if (!CTPLang) {
                $.ajax({
                    url: "/seeyon/i18n_" + getLocale() + ".js",
                    async: false,
                    beforeSend: CsrfGuard.beforeAjaxSend,
                    dataType: "script"
                });
            }
            var lang = CTPLang[getLocale()];
            if (!lang)
                return key;
            var msg = lang[key + _editionI18nSuffix.toUpperCase()];
            if (!msg) {
                msg = lang[key + _editionI18nSuffix.toLowerCase()];
            }
            if (!msg) {
                msg = lang[key];
            }
        }
        if (msg && arguments.length > 1) {
            var messageRegEx_0 = /\{0\}/g;
            var messageRegEx_1 = /\{1\}/g;
            var messageRegEx_2 = /\{2\}/g;
            var messageRegEx_3 = /\{3\}/g;
            var messageRegEx_4 = /\{4\}/g;
            var messageRegEx_5 = /\{5\}/g;
            var messageRegEx_6 = /\{6\}/g;
            var messageRegEx_7 = /\{7\}/g;
            var messageRegEx_8 = /\{8\}/g;
            var messageRegEx_9 = /\{9\}/g;
            var messageRegEx_10 = /\{10\}/g;
            var messageRegEx_11 = /\{11\}/g;
            var messageRegEx_12 = /\{12\}/g;
            var messageRegEx_13 = /\{13\}/g;
            var messageRegEx_14 = /\{14\}/g;
            var messageRegEx_15 = /\{15\}/g;
            for (var i = 0; i < arguments.length - 1; i++) {
                var regEx = eval("messageRegEx_" + i);
                var repMe = "" + arguments[i + 1];
                if (repMe.indexOf("$_") != -1) {
                    repMe = repMe.replace("$_", "$$_");
                }
                msg = msg.replace(regEx, repMe);
            }
        }

        return msg;
    } catch (e) {
        if (typeof (console) !== "undefined" && typeof (console.log) !== "undefined") {
            console.log(e);
        }
    }

    return "";
}

/**
 * 将字符串转换成HTML代码
 */
function escapeStringToHTML(str, isEscapeSpace, isEscapeBr) {
    if (!str) {
        return "";
    }

    str = str.replace(/&/g, "&amp;");
    str = str.replace(/</g, "&lt;");
    str = str.replace(/>/g, "&gt;");
    str = str.replace(/\r/g, "");
    if (typeof (isEscapeBr) == 'undefined' || (isEscapeBr == true || isEscapeBr == "true")) {
        str = str.replace(/\n/g, "<br/>");
    }
    str = str.replace(/\'/g, "&#039;");
    str = str.replace(/"/g, "&#034;");

    if (typeof (isEscapeSpace) != 'undefined' && (isEscapeSpace == true || isEscapeSpace == "true")) {
        str = str.replace(/ /g, "&nbsp;");
    }

    return str;
}

/**
 * 将字符串转换成HTML代码  \n不转，用于title
 */
function escapeStringToHTMLWithoutBr(str, isEscapeSpace) {
    if (!str) {
        return "";
    }

    str = str.replace(/&/g, "&amp;");
    str = str.replace(/</g, "&lt;");
    str = str.replace(/>/g, "&gt;");
    str = str.replace(/\r/g, "");
    str = str.replace(/\'/g, "&#039;");
    str = str.replace(/"/g, "&#034;");

    if (typeof (isEscapeSpace) != 'undefined' && (isEscapeSpace == true || isEscapeSpace == "true")) {
        str = str.replace(/ /g, "&nbsp;");
    }

    return str;
}

function escapeStringToJavascript(str) {
    if (!str) {
        return str;
    }

    str = str.replace(/\\/g, "\\\\");
    str = str.replace(/\r/g, "");
    str = str.replace(/\n/g, "");
    str = str.replace(/\'/g, "\\\'");
    str = str.replace(/"/g, "\\\"");

    return str;
}

function showMask() {
    // 开始遮罩
    try {
        if (getCtpTop() && getCtpTop().startProc) getCtpTop().startProc();
    } catch (e) {
    }
}

function hideMask() {
    // 取消遮罩
    try {
        if (getCtpTop() && getCtpTop().endProc) getCtpTop().endProc();
    } catch (e) {
    }
}

function getMultyWindowId(idName, url) {
    if (url == undefined || idName == undefined) {
        return;
    }
    var idStr;
    var _idIndex = url.indexOf(idName);
    var _idEndIndex = url.indexOf("&", _idIndex);
    if (_idEndIndex == -1) {
        idStr = url.substring(_idIndex + idName.length + 1);
    } else {
        idStr = url.substring(_idIndex + idName.length + 1, _idEndIndex);
    }
    return idStr;
}

function removeCtpWindow(id, type) {
    //ie9下页签切换报错。
    try {
        var _top = getCtpTop();
        if (id == null || id == undefined) {
            id = _top.location + "";
            var _ss = id.indexOf('/seeyon/');
            if (_ss != -1) {
                id = id.substring(_ss)
            }
        }
        if (type == 2) {
            if (typeof (isCtpTop) !== "undefined" && isCtpTop) return;
            if (opener) {
                _top = opener;
            }
        }
        var _wmp = _top._windowsMap;
        if (_wmp) {
            _wmp.remove(id);
        }
    } catch (e) {
        console.error(e);
    }
}

function openCtpWindow(args) {
    var width, height, x, y, resizable, scrollbars, url, dialog_type, modal, _id, _settings;
    //默认设置
    _settings = {
        width: parseInt(screen.width) - 20,
        height: parseInt(screen.height) - 105,
        dialog_type: "open",
        resizable: "yes",
        scrollbars: "yes"
    };
    //获取参数，如果调用时未传，采用默认的
    url = args['url'];
    url = url.indexOf('seeyon') == 0 ? _ctxPath + url : url;
    url += CsrfGuard.getUrlSurffix(url);
    width = args['width'] || _settings.width;
    height = args['height'] || _settings.height;
    dialog_type = args["dialogType"] || _settings["dialog_type"];
    resizable = args['resizable'] || _settings["resizable"];
    scrollbars = args['scrollbars'] || _settings["scrollbars"];
    _id = args["id"];
    if (_id == undefined) {
        _id = url;
    }
    //记录已打开的窗口
    var _wmp = _windowsMap;
    var _wmpKeys = _wmp.keys();
    for (var p = 0; p < _wmp.keys().size(); p++) {
        var _kkk = _wmp.keys().get(p);
        try {
            var _fff = _wmp.get(_kkk);
            var _dd = _fff.document;
            if (_dd) { //窗口的body高度为0，关闭它
                var _p = parseInt(_dd.body.clientHeight);
                if (_p == 0) {
                    if ($.browser.msie) {
                        _dd.write('');
                    }
                    _fff.close();
                    _fff = null;
                    _wmp.remove(_kkk);
                    p--;
                }
            } else {
                _fff = null;
                _wmp.remove(_kkk);
                p--;
            }
        } catch (e) {
            _fff = null;
            _wmp.remove(_kkk);
            p--;
        }
    }
    //打开的窗口数不能超过10个
    if (_wmp.size() == 10) {
        alert($.i18n("window.max.length.js"));
        return;
    }
    //同一url的窗口只允许同时打开一个
    var exitWin = _wmp.get(_id);
    if (exitWin) {
        try {
            var exitWinUrl = exitWin.location.href;
            var tempExitUrl = '';
            var e_idx = exitWinUrl.indexOf('/seeyon/');
            if (e_idx != -1) {
                tempExitUrl = exitWinUrl.substring(e_idx);
            }
            if (tempExitUrl == url || tempExitUrl === url + "#") { //增加对相同url的逻辑判断,如果相同则提示已经打开
                var _dd = exitWin.document;
                if (args["window"]) {
                    args["window"].alert($.i18n("window.already.exit.js"));
                } else {
                    alert($.i18n("window.already.exit.js"));
                }
                exitWin.focus();
                return;
            }
        } catch (e) {
            exitWin = null;
            _wmp.remove(_id);
        }

    }

    var win;
    if (url.indexOf("linkConnectForMenu") != -1 || url.indexOf("linkSystemController") != -1) {
        win = window.open(url, "", "top=0,left=0,scrollbars=yes,dialog=yes,minimizable=yes,modal=open,width=" + width + ",height=" + height + ",resizable=yes");
    } else if (url.indexOf("openMagazinePublishDialog") != -1) {
        var topP = (_settings.height - height) / 2;
        var leftP = (_settings.width - width) / 2;
        win = window.open(url, "", "top=" + topP + ",left=" + leftP + ",scrollbars=yes,dialog=yes,minimizable=yes,modal=open,width=" + width + ",height=" + height + ",resizable=yes");
    } else {
        win = window.open(url, "ctpPopup" + new Date().getTime(), "top=0,left=0,scrollbars=yes,dialog=yes,minimizable=yes,modal=open,width=" + width + ",height=" + height + ",resizable=yes");
        try {
            //最大化窗口
            win.resizeTo(screen.availWidth, screen.availHeight);
        } catch (e) {
            //不支持resizeTo的浏览器，忽略之
        }

    }
    if (win == null) {
        return;
    }
    win.focus();

    if (_wmp) {
        _wmp.putRef(_id, win);
    }
    return win;
}

/*--摘至jquery.comp-debug.js end--*/

/*--摘至common-debug.js start--*/
function removeAllDialog() {
    try {
        getCtpTop().$('.mask').remove();
        var _dialog_box = getCtpTop().$('.dialog_box');
        if (_dialog_box.size() > 0) {
            var _iframes = getCtpTop().$('.dialog_box .dialog_main_content iframe');
            if (_iframes.size() > 0) {
                for (var k = 0; k < _iframes.size(); k++) {
                    _iframes[k].contentWindow.document.write('');
                    _iframes[k].contentWindow.close();
                }
                _iframes.remove();
            }
            _dialog_box.remove();
        }
        getCtpTop().$('.shield').remove();
        getCtpTop().$('.mxt-window').remove();
    } catch (e) {
    }
}

/*--摘至common-debug.js end--*/

/*--摘至frount_common.js start--*/
function removeAllWindow() {
    try {
        getCtpTop().$('.mask').remove();
        var _dialog_box = getCtpTop().$('.dialog_box');
        if (_dialog_box.size() > 0) {
            var _iframes = getCtpTop().$('.dialog_box .dialog_main_content iframe');
            if (_iframes.size() > 0) {
                for (var k = 0; k < _iframes.size(); k++) {
                    _iframes[k].contentWindow.document.write('');
                    _iframes[k].contentWindow.close();
                }
                _iframes.remove();
            }
            _dialog_box.remove();
        }
        getCtpTop().$('.shield').remove();
        getCtpTop().$('.mxt-window').remove();
        if (getCtpTop().$('.layui-layer-shade')) {
            getCtpTop().$('.layui-layer-shade').remove();
        }
        if (getCtpTop().$('.layui-layer')) {
            getCtpTop().$('.layui-layer').remove();
        }
    } catch (e) {
    }
}

//刷新操作
//当前在二级页面就刷新二级页面，如果在顶层空间就直接刷新
function reFlesh() {
    if (vPortal.currentSpaceId) {
        if (vPortal.admin != "true") {
            var mainIframe = document.getElementById("mainIframe");
            //存在iframe并且就直接切换
            if (mainIframe) {
                if (getCtpTop()["removeOnbeforeunload"] != null) {
                    getCtpTop()["removeOnbeforeunload"]();
                }
                //直接刷新iframe
                var href = mainIframe.contentWindow.location.href;
                if (href.indexOf("fromReFlesh=") > 0) {
                    mainIframe.contentWindow.location.reload();
                } else {
                    if (href.indexOf("?") > 0) {
                        mainIframe.contentWindow.location.href = href + "&fromReFlesh=true"
                    } else {
                        mainIframe.contentWindow.location.href = href + "?fromReFlesh=true"
                    }
                }
            } else {
                if (getCtpTop()["removeOnbeforeunload"] != null) {
                    getCtpTop()["removeOnbeforeunload"]();
                }
                //这里暂无刷新空间的快捷方法，直接刷新顶层window，后续可优化
                getCtpTop().location.reload();
            }
        }
    }
}


//开始进度条
var commonProgressbar = null;

function startProc(title) {
    try {
        var options = {
            text: title
        };
        if (title == undefined) {
            options = {};
        }
        if (commonProgressbar != null) {
            commonProgressbar.start();
        } else {
            commonProgressbar = new MxtProgressBar(options);
        }
    } catch (e) {
    }
}

//结束进度条
function endProc() {
    try {
        if (commonProgressbar) commonProgressbar.close();
        commonProgressbar = null;
    } catch (e) {
    }
}

/*--摘至frount_common.js end--*/

//系统通知下线
var showLogoutMsgFlag = true;

function showLogoutMsg(msg) {
    if (showLogoutMsgFlag) {
        try {
            alert(msg);
            showLogoutMsgFlag = false;
            if (getCtpTop()["removeOnbeforeunload"] != null) {
                getCtpTop()["removeOnbeforeunload"]();
            }
            window.location.href = encodeURI(_ctxPath + "/main.do?method=logout&reason=" + msg);
        } catch (e) {
            showLogoutMsgFlag = true;
        }
    }
}

//销毁离开提醒功能
var pb_strConfirmCloseMessage = $.i18n('window.exit.leave');

function removeOnbeforeunload() {
    window.onbeforeunload = null;
    $(window).unbind("onbeforeunload", window["beforeLogoutcLoseWin"]);
    if (window.detachEvent != null) {
        window.detachEvent('onbeforeunload', window["beforeLogoutcLoseWin"]);
    }
    window.outSysFlag = false;
}

//首页丢失时  关闭相应打开的界面
function beforeLogoutcLoseWin() {
    window.event.returnValue = pb_strConfirmCloseMessage;
    for (var p = 0; p < _windowsMap.keys().size(); p++) {
        var _kkk = _windowsMap.keys().get(p);
        try {
            var _fff = _windowsMap.get(_kkk);
            var _dd = _fff.document;
            if (_dd) {
                var _p = parseInt(_dd.body.clientHeight);
                if (_p == 0) {
                    _fff = null;
                    _windowsMap.remove(_kkk);
                    p--;
                }
            }
        } catch (e) {
            _fff = null;
            _windowsMap.remove(_kkk);
            p--;
        }
    }
    if (!SeeUtils.isIE) {
        if (_windowsMap.size() > 0) {
            try {
                //关闭打开的相关窗口
                for (var nums in _windowsMap.instance) {
                    var win = _windowsMap.instance[nums];
                    if (win != null) {
                        win.onbeforeunload = null;
                        win.opener = null;
                        win.close();
                    }
                }
            } catch (e) {

            }
        }
    }
}

//首页丢失时  关闭相应打开的界面
var outSysFlag = true;

function logoutcLoseWin() {
    window.onbeforeunload = null;
    try {
        //如果不是开发模式 则进行掉线操作
        if (!vPortal.isDevelop && outSysFlag) {
            try { // OA-19281
                // 修复IE下点击退出，但触发iframe工作区内的onbeforeunload事件并选择留在当前页面时，此跳转会出现js异常的问题，通过忽略异常的方式处理
                isLogoutExecuted = true;
                //window.location.href = _ctxPath + "/main.do?method=logout";
                $.ajax({
                    sync: true,
                    type: "POST",
                    url: "/seeyon/main.do?method=logout"
                });
                outSysFlag = true;
            } catch (e) {
            }
        }
    } catch (ex) {

    }

    if (_windowsMap.size() > 0) {
        try {
            //关闭打开的相关窗口
            for (var nums in _windowsMap.instance) {
                var win = _windowsMap.instance[nums];
                if (win != null) {
                    win.onbeforeunload = null;
                    win.opener = null;
                    win.close();
                }

            }
        } catch (e) {

        }
    }
}

// 退出登录
var isLogoutExecuted = false; //用来控制退出时只执行一次logout请求
var logoutConfirm_I18n = $.i18n("system.logout.confirm");
var isDirectClose = true;

function logout(closeAll) {
    for (var p = 0; p < _windowsMap.keys().size(); p++) {
        var _kkk = _windowsMap.keys().get(p);
        try {
            var _fff = _windowsMap.get(_kkk);
            var _dd = _fff.document;
            if (_dd) {
                var _p = parseInt(_dd.body.clientHeight);
                if (_p == 0) {
                    _fff = null;
                    _windowsMap.remove(_kkk);
                    p--;
                }
            }
        } catch (e) {
            _fff = null;
            _windowsMap.remove(_kkk);
            p--;
        }
    }

    if (closeAll) {
        if (_windowsMap && _windowsMap.size() > 0) {
            alert($.i18n("window.genius.isClose"));
            return;
        }
        isDirectClose = false;
        window.open("closeIE7.htm", "_self");
    } else {

        var closeMessage = "";
        if (_windowsMap.size() > 0) {
            if (confirm($.i18n("window.exit.isClose"))) {
                var _keys = _windowsMap.keys();
                for (var i = 0; i < _keys.size(); i++) {
                    var _k = _keys.get(i);
                    var _tp = _windowsMap.get(_k);
                    if (_tp) {
                        _tp.onbeforeunload = null;
                        _tp.opener = null;
                        _tp.close();
                    }

                }
                if (navigator.userAgent.toLowerCase().indexOf("nt 10.0") != -1 && navigator.userAgent.toLowerCase().indexOf("trident") != -1) {
                    var leaveConfirm = confirm(logoutConfirm_I18n);
                    if (leaveConfirm == true) {
                        window.onbeforeunload = null;
                        isDirectClose = false;
                        removeBeforeunloadAndLogout();
                    } else {
                        isDirectClose = true;
                        return false;
                    }
                } else {
                    $.confirm({
                        'type': 1,
                        'msg': logoutConfirm_I18n,
                        ok_fn: function () {
                            window.onbeforeunload = null;
                            Task.prototype.clear();
                            isDirectClose = false;
                            removeBeforeunloadAndLogout();
                        },
                        cancel_fn: function () {
                            isDirectClose = true;
                        }
                    });
                }
            }
        } else {
            if (navigator.userAgent.toLowerCase().indexOf("nt 10.0") != -1 && navigator.userAgent.toLowerCase().indexOf("trident") != -1) {
                var leaveConfirm = confirm(logoutConfirm_I18n);
                if (leaveConfirm == true) {
                    window.onbeforeunload = null;
                    isDirectClose = false;
                    removeBeforeunloadAndLogout();
                } else {
                    isDirectClose = true;
                    return false;
                }
            } else {
                $.confirm({
                    'type': 1,
                    'msg': logoutConfirm_I18n,
                    ok_fn: function () {
                        window.onbeforeunload = null;
                        isDirectClose = false;
                        removeBeforeunloadAndLogout();
                    },
                    cancel_fn: function () {
                        isDirectClose = true;
                    }
                });
            }
        }
    }
}

function removeBeforeunloadAndLogout() {
    try { // OA-19281
        // 修复IE下点击退出，但触发iframe工作区内的onbeforeunload事件并选择留在当前页面时，此跳转会出现js异常的问题，通过忽略异常的方式处理
        isLogoutExecuted = true;
        if (getCtpTop()["removeOnbeforeunload"] != null) {
            getCtpTop()["removeOnbeforeunload"]();
        }
        if (typeof (onbeforeunloadFunction) === "function") {
            onbeforeunloadFunction("logout");
        }
        window.location.href = _ctxPath + "/main.do?method=logout";
    } catch (e) {
    }
}

//代理事件的弹框
function agentAlert() {
    var dialogAgent = $.dialog({
        id: 'dialogAgent',
        url: _ctxPath + '/agent.do?method=agentAlert',
        title: $.i18n("common.prompt"),
        width: 410,
        height: 200
    });
}

/**
 * 门户公共JS，7.0门户会战新开发的
 * @author huy
 * @date 2017-09-06
 */
//seeyonPortalBody的高度
var mainParentNodeH = 0;

//resize 事件的主入口集合
var doResizeAll = function () {
    var _main = document.getElementById('main');
    var _mainIframe = document.getElementById('mainIframe');
    //刷新菜单的各种宽高
    if (vPortal.layoutFunction && typeof (vPortal.layoutFunction.reizeMenu) == "function") {
        vPortal.layoutFunction.reizeMenu();
    } else {
        refreshCurrentSpaceMenuNavForPortal();
    }
    //更新相关变量
    if (document.querySelector(".webStyle")) { //网页式，父元素没高度，不能直接取它的值
        mainParentNodeH = 995;
    } else {  //非网页式，父元素是绝对定位的，对取它的高
        mainParentNodeH = _main.parentNode.offsetHeight;
    }
    //如果工作区是空间，就刷新它
    if (_main.className.indexOf("hasIframe") < 0 && !resizeIsFromH5Video) {
        refreshCurrentSpace("resize");
    }
    //如果工作区是主题空间，也刷新它
    if (_mainIframe && _mainIframe.src && _mainIframe.src.indexOf('method=showThemSpace') > -1 && !resizeIsFromH5Video) {
        _mainIframe.contentWindow.refreshCurrentSpace();
    }
    _main = null;
    _mainIframe = null;
}

//视频栏目的全屏event不要触发"刷新空间"，否则chrome下视频栏目无法全屏
var resizeIsFromH5Video = false;
if (document.addEventListener) {
    try {
        window.addEventListener("webkitfullscreenchange", function () {
            resizeIsFromH5Video = !resizeIsFromH5Video;
        }, false);
    } catch (e) {
        //非webkit浏览器不支持这个API
    }
}


//当前空间类型，用于当前位置图标显示
var currentSpaceType = "personal";

//产品导航里面的关闭会用到
var productView_Obj;

/*-- 页面渲染、元素渲染之类的函数 --*/

//初始化页面，在body的onload里调用
var initV5Portal = function () {
    vPortal.mainFrameIsLoad = false;
    // var loadingV5Portal = $.progressBar();
    //如果有左侧区，收起/折叠左侧区
    if (document.getElementById("seeyonPortalLeft")) {
        initLeftCollapse();
    }
    //根据设置的变量，调整页面宽度
    vPortal.layoutFunction && vPortal.layoutFunction.initWebStyleWidth && vPortal.layoutFunction.initWebStyleWidth();
    //渲染主框架元素
    renderMainFrameElement();
    //如果模板存在afterMainFrameElementFunction，执行它
    if (vPortal.layoutFunction && vPortal.layoutFunction.afterMainFrameElementFunction) {
        vPortal.layoutFunction.afterMainFrameElementFunction();
    }
    //移动端设计器的main区
    if (isMobileDesigner) {
        if (!document.getElementById("wrapper")) {
            loadingV5Portal.close();
            return;
        }
        document.getElementById("wrapper").setAttribute("class", "wrapper designerStatus");
        renderMobileDesignerMain();
        //渲染热点数据
        initDefaultSkinHotspots();
        //根据热点数据 渲染layout的样式
        if (typeof (renderLayoutHotspot) !== "undefined" && typeof (renderLayoutHotspot) === "function") {
            renderLayoutHotspot();
        }

        if (typeof (layoutFun) !== "undefined" && typeof (layoutFun) === "function") {
            layoutFun();
        }
        // loadingV5Portal.close();
        return;
    }
    if (vPortal.portalSet && vPortal.portalSet.portalType == "3") { //如果为登录前门户,fix bug:OA-146307
        if ((vPortal.space == null || vPortal.space.length <= 0) && vPortal.spaceErrorMsg == "") {
            if (getCtpTop()["removeOnbeforeunload"] != null) {
                getCtpTop()["removeOnbeforeunload"]();
            }
            window.location.reload(true);
            return;
        }
    }

    //渲染第一个空间（管理员的空间数为0，不渲染）
    if (vPortal.space && vPortal.space.length > 0) {
        var _currentSpaceObject = null;
        if (vPortal.currentSpaceId) { //默认打开时指定一个空间
            var spaceIndex = getSpaceIndex(vPortal.currentSpaceId);
            if (spaceIndex !== -1) {
                if (spaceIndex instanceof Array) {
                    //空间在vPortal.space下的某个list中
                    _currentSpaceObject = vPortal.space[spaceIndex[0]].list[spaceIndex[1]];
                } else {
                    //空间直接位于vPortal.space下
                    _currentSpaceObject = vPortal.space[spaceIndex];
                }
            }
        } else {
            if (vPortal.space[0].navType === "spaceGroup" && vPortal.space[0].list !== null && vPortal.space[0].list.length > 0) {
                _currentSpaceObject = vPortal.space[0].list[0];
            } else if (vPortal.space[0].navType === "portalGroup" && vPortal.defaultSpaceId) {
                var spaceIndex = getSpaceIndex(vPortal.defaultSpaceId);
                if (spaceIndex !== -1) {
                    if (spaceIndex instanceof Array) {
                        //空间在vPortal.space下的某个list中
                        _currentSpaceObject = vPortal.space[spaceIndex[0]].list[spaceIndex[1]];
                    } else {
                        //空间直接位于vPortal.space下
                        _currentSpaceObject = vPortal.space[spaceIndex];
                    }
                }
            } else {
                _currentSpaceObject = vPortal.space[0];
            }
            vPortal.currentSpaceId = _currentSpaceObject.id || _currentSpaceObject.spaceId;
        }
        if (_currentSpaceObject) {
            if (_currentSpaceObject.openType === "mainfrm" || _currentSpaceObject.openType === "2") {
                initV5Space(_currentSpaceObject, "fromInit");
            } else if (_currentSpaceObject.openType === "newWindow" || _currentSpaceObject.openType === "1") {
                findAndRender1stMainiframeSpace(vPortal.space);
            }
        }
    } else if (document.getElementById("main") && vPortal.spaceErrorMsg) {
        document.getElementById("main").innerHTML = vPortal.spaceErrorMsg;
    }
    // loadingV5Portal.close();
    //大屏循环加载空间
    if (vPortal.portalSet && vPortal.portalSet.portalType == "2") {
        spaceLoop();
    }
    // //第2、6套主题空间不加纵向滚动条
    // if(!isCtpTop && getCtpTop().document.querySelector(".webStyle")){
    //     document.querySelector("html").style.overflowY = "auto";
    // }

    //在线人数和人员在线状态轮询   注释掉-->改为元素里面取初始化
    // initMessage(vPortal.messageIntervalSecond);

    if (typeof (isPortalTemplateSwitching) != "undefined" && isPortalTemplateSwitching == "true") {
        doPwdCheck = false;
    }
    //代理提示
    if (vPortal.isMessageForAgentAlertNotEmpty && vPortal.isIdsForAgentAlertNotEmpty && vPortal.subPortal !== "true") {
        //密码过期/不符合强度要求提醒
        if (vPortal.portalSet && vPortal.portalSet.portalType == "0") {
            if (typeof (doPwdCheck) !== "undefined" && doPwdCheck) {
                agentAlert();
            }
        }
    }
    //密码过期/不符合强度要求提醒
    if (vPortal.portalSet && vPortal.portalSet.portalType == "0") {
        if (typeof (doPwdCheck) !== "undefined" && doPwdCheck && vPortal.subPortal != "true") {
            checkPwdIsExpired();
        }
    }
}

//循环加载空间-by大屏
function spaceLoop() {
    var mainDom = document.getElementById("main");
    var spaceTime = parseInt(vPortal.portalSet.moreSpaceCycle) * 60 * 1000;
    var sectionTime = parseInt(vPortal.portalSet.columnContentCycle) * 60 * 1000;
    var spaceInterval;
    var sectionInterval;
    var sectionTimeout1;
    if (vPortal.portalSet.portalType == '2') {
        var currentSpace = 0;
        if (vPortal.space.length > 1) {
            document.querySelector(".spaceTitle").innerHTML = vPortal.space[currentSpace].navName;
        }
        var count = 0;
        var length = vPortal.space.length;
        if ((count + 1) == length) {
            count = 0;
        } else {
            count++;
        }
        window.clearInterval(spaceInterval);
        window.clearInterval(sectionInterval);
        window.clearTimeout(sectionTimeout1);
        if (spaceTime > sectionTime) {
            sectionInterval = setInterval(function () {
                for (var i in vPortal.spacesSummary[vPortal.currentSpaceId].portlets) {
                    for (var j in vPortal.spacesSummary[vPortal.currentSpaceId].portlets[i]) {
                        (function (_i) {
                            var sectionDom = document.getElementById("panelBody_" + _i.id);
                            sectionDom.style.animation = "bigSectionAnimateOut 0s 0.5";
                            renderEachPanel(_i.sections[0].id);
                        })(vPortal.spacesSummary[vPortal.currentSpaceId].portlets[i][j]);
                    }
                }
                sectionTimeout1 = setTimeout(function () {
                    for (var i = 0; i < document.querySelectorAll(".section-body").length; i++) {
                        (function (_i) {
                            var sectionDom = document.querySelectorAll(".section-body")[_i];
                            sectionDom.style.animation = "";
                            sectionDom.style.animation = "bigSectionAnimateIn 0s 0.5";
                        })(i)
                    }
                }, 1000);
            }, sectionTime);
        }
        if (length > 1) {
            spaceInterval = setInterval(function () {
                mainDom.style.animation = "";
                mainDom.style.animation = "bigSpaceAnimateOut 0s 0.5";
                setTimeout(function () {
                    mainDom.style.visibility = "hidden";
                    initV5Space(vPortal.space[count], "isForceLoadData");
                    currentSpace = count;
                    document.querySelector(".spaceTitle").innerHTML = vPortal.space[currentSpace].navName;
                    if ((count + 1) == length) {
                        count = 0;
                    } else {
                        count++;
                    }
                    setTimeout(function () {
                        mainDom.style.visibility = "visible";
                        mainDom.style.animation = "";
                        mainDom.style.animation = "bigSpaceAnimateIn 0s 0.5";
                    }, 500);
                }, 2060);
                window.clearInterval(sectionInterval);
                window.clearTimeout(sectionTimeout1);
                if (spaceTime > sectionTime) {
                    sectionInterval = setInterval(function () {
                        for (var i in vPortal.spacesSummary[vPortal.currentSpaceId].portlets) {
                            for (var j in vPortal.spacesSummary[vPortal.currentSpaceId].portlets[i]) {
                                (function (_i) {
                                    var sectionDom = document.getElementById("panelBody_" + _i.id);
                                    sectionDom.style.animation = "bigSectionAnimateOut 0s 0.5";
                                    renderEachPanel(_i.sections[0].id);
                                })(vPortal.spacesSummary[vPortal.currentSpaceId].portlets[i][j]);
                            }
                        }
                        sectionTimeout1 = setTimeout(function () {
                            for (var i = 0; i < document.querySelectorAll(".section-body").length; i++) {
                                (function (_i) {
                                    var sectionDom = document.querySelectorAll(".section-body")[_i];
                                    sectionDom.style.animation = "";
                                    sectionDom.style.animation = "bigSectionAnimateIn 0s 0.5";
                                })(i)
                            }
                        }, 1000);
                    }, sectionTime);
                }
            }, spaceTime); //为了测试暂时定为10s
        }
    }
}

//如果有左侧区，收起/折叠左侧区
var initLeftCollapse = function () {
    var _wrapper = document.getElementById("wrapper");
    //1：当前处于收起状态，2：当前处于展开状态
    var judgeNum = vPortal.customize.left_panel_set ? vPortal.customize.left_panel_set : "2";
    if (judgeNum == "1" && vPortal.admin == "false") {
        _wrapper.setAttribute("class", "wrapper collapse");
    } else {
        _wrapper.setAttribute("class", "wrapper");
    }
}

//渲染空间
var initV5Space = function (_currentSpaceObject, _fromType) {
    vPortal.sectionPropertyIsEditing = false;
    var thisSpacetype = _currentSpaceObject.spaceType;
    if (thisSpacetype == "8") { //第三方系统空间
        currentSpaceType = "thridpartyspace";
    } else if (thisSpacetype == "11") { //关联系统
        currentSpaceType = "linksystem";
    } else if (thisSpacetype == "12" || thisSpacetype == "26") { //关联项目
        currentSpaceType = "relateproject";
    } else {
        currentSpaceType = "personal"; //来回切换的时候恢复初始值
    }
    if (_currentSpaceObject.masterPortalSpace == true) {
        $.alert($.i18n('portal.tips.openSpaceInSysPortal'));
        return;
    }
    if (_currentSpaceObject === undefined) {
        return;
    }
    //模板自定义的一些函数,如“入口式”需要先恢复首页的默认布局，“网页式”需要先恢复首页工作区的高度
    if (vPortal.isTransmutative && vPortal.layoutFunction && vPortal.layoutFunction.recoverIndexLayout) {
        vPortal.layoutFunction.recoverIndexLayout(_currentSpaceObject.openType);
    }
    if (_currentSpaceObject.openType == "newWindow" || _currentSpaceObject.openType == "1" || _currentSpaceObject.navType == "newWindow") {
        //openType为1时，在新窗口打开
        openCtpWindow({
            'url': _currentSpaceObject.spacePath || _currentSpaceObject.url
        });
        //非initV5Portal时执行空间高亮动作，因为在initV5Portal时会走元素中的空间高亮动作
        if (_fromType !== "fromInit") {
            setSpaceListHighlight(_currentSpaceObject);
        }
        vPortal.mainIsSpace = false;
        //vPortal.currentSpaceId = _currentSpaceObject.id;
    } else if (_currentSpaceObject.openType == "mainfrm" || _currentSpaceObject.openType == "2" || _currentSpaceObject.navType == "mainfrm") {
        //openType为2时，在工作区打开
        //是否显示空间的面包屑
        if (vPortal.showLocation || top.vPortal && top.vPortal.showLocation) {
            if (_currentSpaceObject.spaceIcon && _currentSpaceObject.spaceIcon != undefined && _currentSpaceObject.spaceIcon != "" || _currentSpaceObject.icon && _currentSpaceObject.icon != undefined && _currentSpaceObject.icon != "") {
                var spaceIcon = _currentSpaceObject.spaceIcon || _currentSpaceObject.icon;
                if (spaceIcon.indexOf("/fileUpload.do") > -1) {
                    var spaceIconType = "img";
                } else {
                    spaceIcon = spaceIcon.split(".")[0];
                    var spaceIconType = "icon";
                }
            } else {
                var spaceIcon = "";
            }
            var spaceName = _currentSpaceObject.spaceName || _currentSpaceObject.navName;
            if (spaceIconType === "img") {
                var html = '<img src="' + _ctxPath + spaceIcon + '"/ >';
            } else {
                var html = '<i class="vportal vp-' + spaceIcon + '"></i>';
            }
            html += '<span class="nowLocation_content">';
            html += '<a>' + spaceName + '</a>';
            html += '</span>';
            showLocation(html);
        } else {
            hideLocation();
        }

        //如果从二级页面点击空间，此时需要去掉hasIframe
        var mainObj = document.getElementById("main");
        if (mainObj && mainObj.className.indexOf("hasIframe")) {
            mainObj.setAttribute("class", "main");
            if (typeof (isCtpTop) !== "undefined" && isCtpTop) {
                mainObj.style.overflowY = "auto";
            }
        }

        //当第三方系统空间、关联系统、关联项目在工作区打开时，不需要请求spacesSummary和栏目布局，直接替换#main的url即可
        if ((_currentSpaceObject.openType === "mainfrm" && (_currentSpaceObject.navType === "linkSystem" || _currentSpaceObject.navType === "linkProject" || _currentSpaceObject.spaceType === "11" || _currentSpaceObject.spaceType === "8")) || (_currentSpaceObject.openType === "2" && (_currentSpaceObject.spaceType === "8" || _currentSpaceObject.spaceType === "11" || _currentSpaceObject.spaceType === "12"))) {
            var _spacePathUrl = _currentSpaceObject.spacePath || _currentSpaceObject.url;
            showMenu(_spacePathUrl, _currentSpaceObject.id);
            vPortal.currentSpaceId = _currentSpaceObject.id;
            return;
        }

        //标准空间去掉这个
        autoLeftMargin(mainObj, false);

        //根据vPortal.spacesSummary是否有该空间的摘要数据和isForceLoadData参数，调用不同的渲染函数
        if (_fromType === "isForceLoadData" || (vPortal.spacesSummary[_currentSpaceObject.id] === undefined && vPortal.spacesSummary[_currentSpaceObject.spaceId] === undefined)) {
            //调用本函数时传了需要强制刷新的参数isForceLoadData，或者缓存中无该空间的摘要数据时，均需要向台请求后台数据
            getSpaceMenusAndSummaryForPortal(_currentSpaceObject, "isForceLoadData");
        } else if ((_fromType === undefined || _fromType === "fromInit") && (vPortal.spacesSummary[_currentSpaceObject.id] !== undefined || vPortal.spacesSummary[_currentSpaceObject.spaceId] !== undefined)) {
            //调用本函数时没有传需要强制刷新的参数isForceLoadData，且缓存中有该空间的摘要数据时，直接使用缓存中的空间摘要渲染空间即可
            LoadSectionThirdJsFiles(_currentSpaceObject);
        }
        //空间的高亮效果
        vPortal.currentSpaceObject = _currentSpaceObject;
        //非initV5Portal时执行空间高亮动作，因为在initV5Portal时会走元素中的空间高亮动作
        if (_fromType !== "fromInit") {
            setSpaceListHighlight();
        }
        vPortal.mainIsSpace = true;
    }
}

//根据需要，部分模板需要额外的一些样式控制
var autoLeftMargin = function (mainObj, autoType, url) {
    //报表空间直接返回
    if (typeof (getCtpTop().vPortal) == "undefined") return;

    if (getCtpTop().vPortal.layoutName === "layout_bpm" && url != null) {
        getCtpTop().document.getElementById("main").style.paddingLeft = '15px';
    }

    //下面这些模板需要特殊处理
    if (getCtpTop().vPortal.layoutName != "layout04_a" && getCtpTop().vPortal.layoutName != "layout04_c" && getCtpTop().vPortal.layoutName != "layout05_a" && getCtpTop().vPortal.layoutName != "layout02_a" && getCtpTop().vPortal.layoutName != "layout02_c") return;
    if (autoType) {
        mainObj.style.marginLeft = '15px';
    } else {
        mainObj.style.marginLeft = "";
    }
}

//为当前空间设置高亮效果
var setSpaceListHighlight = function (_spaceObject) {
    var _currentSpaceObject;
    if (_spaceObject === undefined) {
        _currentSpaceObject = vPortal.currentSpaceObject
    } else {
        _currentSpaceObject = _spaceObject;
    }
    if (!_currentSpaceObject) return;
    var _showedNav = document.getElementById("showedNav");
    var _currentSpace = document.getElementById("spaceLi_" + _currentSpaceObject.id);
    var _moreSpaceNav = document.getElementById("moreSpaceNav");
    //topcenterNav、miniNav中有#showedNav，本空间位于已显示空间中，且未高亮，需进行高亮操作
    if (_showedNav && _showedNav.querySelector("#spaceLi_" + _currentSpaceObject.id) && _currentSpace.className !== "current") {
        //高亮效果
        SeeUtils.siblings(_currentSpace, "li", function (aEl) {
            SeeUtils.removeClass(aEl, "current");
        });
        SeeUtils.addClass(_currentSpace, "current");
    }
    //topcenterNav、miniNav中有#moreSpaceNav，本空间位于下拉中，需要提上来并高亮
    if (_moreSpaceNav && _currentSpace && _moreSpaceNav.querySelector("#spaceLi_" + _currentSpaceObject.id)) {
        var maxShowedNav = (vPortal.themeHotspots.topCenterSpaceNav && vPortal.themeHotspots.topCenterSpaceNav.portalNavMax !== null) ? Number(vPortal.themeHotspots.topCenterSpaceNav.portalNavMax) : 3;
        if (document.getElementById("tpl-topCenterNav")) {
            var _navElementType = "topcenterNav";
        }
        if (document.getElementById("tpl-miniNav")) {
            var _navElementType = "miniNav";
        }
        var _allLi = _showedNav.querySelectorAll("li");
        for (var i = 0; i < _allLi.length; i++) {
            SeeUtils.removeClass(_allLi[i], "current");
        }
        if (_allLi.length <= maxShowedNav) { //显示出来的空间<=N个
            spaceLiDOmUp2Top(_currentSpaceObject, _navElementType, _currentSpace);
            // _showedNav.parentNode.style.width = (100 * (maxShowedNav + 1) + 20) + "px";
            // _showedNav.style.width = (100 * (maxShowedNav + 1)) + "px";
        } else { //显示出来的空间大于N个了
            _showedNav.removeChild(_showedNav.lastChild);
            spaceLiDOmUp2Top(_currentSpaceObject, _navElementType, _currentSpace);
        }
    }
    //topNav、leftNav、menu4Entrance
    var _navArea = document.querySelector(".navHotspotsArea");
    if (_navArea) {
        //去掉一级菜单的高亮效果，空间或门户无论平铺还是收进一个导航中，都需要这个操作
        var _allLi = document.querySelectorAll(".lev1Li");
        for (var i = 0; i < _allLi.length; i++) {
            SeeUtils.removeClass(_allLi[i], "current");
        }
        //如果传了_spaceObject，表示在新窗口打开的空间，需要对当前高亮的二级空间去高亮效果
        if (_spaceObject !== undefined) {
            _allLi = document.querySelectorAll(".lev2Li");
            for (var i = 0; i < _allLi.length; i++) {
                SeeUtils.removeClass(_allLi[i], "current");
            }
        }
        //空间或门户平铺于一级导航中
        if (_currentSpace && _currentSpace.parentNode.className === "lev1Li") {
            SeeUtils.addClass(_currentSpace.parentNode, "current");
        }
        //空间或门户被收进一个导航中
        if (_currentSpace && (_currentSpace.parentNode.className.indexOf(" lev1Ul") > -1 || _currentSpace.parentNode.className === "lev1Ul" || _currentSpace.parentNode.className === "lev2Ul")) {
            //去掉所有二级菜单的高亮效果，并给当前空间添加高亮效果
            if (_currentSpace.parentNode.className === "lev2Ul") {
                _allLi = document.querySelectorAll(".lev2Li");
                for (var i = 0; i < _allLi.length; i++) {
                    SeeUtils.removeClass(_allLi[i], "current");
                }
            }
            SeeUtils.addClass(_currentSpace, "current");
        }
    }

}

//定义一个全局变量记录当前最新的SpaceId，后面在getSpaceMenusAndSummaryForPortal异步里面需要作对比
var newestSpaceId = "";

/**
 * 获取空间的菜单及栏目摘要等信息
 */
function getSpaceMenusAndSummaryForPortal(_currentSpaceObject) {
    //每次都更新一下SpaceId，不能直接用SpaceId，因为比较的适合SpaceId还没更新,有时候.id为undefined，就取spaceId
    newestSpaceId = _currentSpaceObject.id || _currentSpaceObject.spaceId;
    //拼接参数
    var _parameter = {
        "spaceId": _currentSpaceObject.id || _currentSpaceObject.spaceId,
        "spacePath": _currentSpaceObject.url || _currentSpaceObject.spacePath,
        "spaceType": "5"
    };
    callBackendMethod("portalManager", "getSpaceMenusAndSummaryForPortal", _parameter, {
        success: function (_result) {
            if (_result == '__LOGOUT') {
                offlineFun();
                return;
            }
            if (_result && _result.length == 1 && _result[0] == "spaceDeleted") {
                alert($.i18n("portal.index.refresh.msg"));
                if (getCtpTop()["removeOnbeforeunload"] != null) {
                    getCtpTop()["removeOnbeforeunload"]();
                }
                getCtpTop().location.href = "/seeyon/main.do?method=changeLoginAccount&isPortalTemplateSwitching=true&login.accountId=" +
                    vPortal.CurrentUser.loginAccount;
                return;
            }
            var spaceMenusAndSummary = _result[0];
            var decorationCode = _result[1];
            if (_currentSpaceObject.id == undefined) {
                _currentSpaceObject.id = _currentSpaceObject.spaceId;
            }

            //以前的异步直接return，不需要再渲染之前的空间了
            if (_currentSpaceObject.id != newestSpaceId) return;

            _currentSpaceObject.decoration = decorationCode;
            vPortal.spacesSummary[_currentSpaceObject.id] = spaceMenusAndSummary;
            LoadSectionThirdJsFiles(_currentSpaceObject, "isForceLoadData");
        },
        error: function (error) {
            console.error(error);
            // alert($.i18n("portal.index.refresh.msg"));
            // if (getCtpTop()["removeOnbeforeunload"] != null) {
            //     getCtpTop()["removeOnbeforeunload"]();
            // }
            // getCtpTop().location.href = "/seeyon/main.do?method=changeLoginAccount&isPortalTemplateSwitching=true&login.accountId=" +
            //     vPortal.CurrentUser.loginAccount;
        }
    });
}

//判断本空间下的栏目是否有第三方JS，如果有，就异步加载它们
var LoadSectionThirdJsFiles = function (_currentSpaceObject, _isForceLoadData) {
    hasPreLoginSectionShowed = false;
    var _spaceId = _currentSpaceObject.id || _currentSpaceObject.spaceId;
    var _templeteList = vPortal.spacesSummary[_spaceId].tplCodes;
    if (_templeteList && !(vPortal.moduleCodeSet && vPortal.moduleCodeSet.ALL === "ALL")) {
        var _templeteListArray = _templeteList.split(',');
        var _newList = "";
        for (var i = 0, len = _templeteListArray.length; i < len; i++) {
            var _this = _templeteListArray[i];
            if (vPortal.moduleCodeSet[_this] === undefined) {
                vPortal.moduleCodeSet[_this] = _this;
                _newList += "," + _this;
            }
        }
        if (_newList.indexOf(",") === 0) {
            _newList = _newList.substring(1);
        }
        //如果本空间下栏目的第三方JS文件已缓存过了，就直接渲染栏目区(#main)的布局
        if (_newList === "") {
            renderDecoration2Main(_currentSpaceObject, "isForceLoadData");
        } else {
            //如果本空间下栏目有第三方JS未加载过，就异步加载它们
            var _randomTime = new Date().getTime();
            var _url = _ctxPath + "/portal/portalController.do?method=getModuleJs&tplCodes=" + _newList + "&fd=0" + CsrfGuard.getUrlSurffix() + "&rnd=" + _randomTime;
            loadScript(_url, function (result) {
                renderDecoration2Main(_currentSpaceObject, "isForceLoadData");
            }, true);
        }
    } else {
        renderDecoration2Main(_currentSpaceObject, "isForceLoadData");
    }
}

//渲染栏目区(#main)的布局，仅布局，不含栏目
var renderDecoration2Main = function (_currentSpaceObject, _isForceLoadData) {
    var _spaceId = _currentSpaceObject.id || _currentSpaceObject.spaceId;
    var _randomTime = new Date().getTime();
    var _navId = _currentSpaceObject.id || _currentSpaceObject.spaceId;
    //清空iframe内存

    clearMemory(['interval', 'iframe'], function () {
        renderDecoration2MainFuc(_navId, _currentSpaceObject);
    });
    var _mainIframe = document.getElementById("mainIframe");
    if (!_mainIframe) {
        renderDecoration2MainFuc(_navId, _currentSpaceObject);
        _mainIframe = null;
    }

}

function renderDecoration2MainFuc(_navId, _currentSpaceObject) {
    if (vPortal.decoration[_currentSpaceObject["decoration"]]) {
        //vPortal.decoration中有这套布局的内容了，直接使用
        document.getElementById("main").innerHTML = "";
        document.getElementById("main").innerHTML = vPortal.decoration[_currentSpaceObject["decoration"]];
        //渲染栏目，renderPortlets在section.js中定义
        renderPortlets(_navId);
    } else {
        //vPortal.decoration中还没有这套布局的内容，通过loadScript来请求
        var _decorationJSURL = "/seeyon/portal/decoration/layout/" + _currentSpaceObject.decoration + "/layout.js";
        if (_currentSpaceObject.decoration.indexOf("custom_") == 0) {
            var decorationFileId = _currentSpaceObject.decoration.substring(7);
            var _randomTime = new Date().getTime();
            _decorationJSURL = "/seeyon/portal/portalController.do?method=jsData&decorationCode=" + _currentSpaceObject.decoration + "&rnd=" + _randomTime;
        }
        loadScript(_decorationJSURL, function () {
            if (document.getElementById("main")) {
                document.getElementById("main").innerHTML = "";
                document.getElementById("main").innerHTML = vPortal.decoration[_currentSpaceObject["decoration"]];
                //渲染栏目，renderPortlets在section.js中定义
                renderPortlets(_navId);
            }

        }, true);
    }
}

//释放内存。
function clearMemory(arr, iframeLoadFuc) {
    //interval、清空全部栏目的定时器
    if (arr.indexOf("interval") != -1) {
        for (var key in vPortal.spaceInterval) {
            clearInterval(vPortal.spaceInterval[key]);
            delete vPortal.spaceInterval[key];
        }
    }

    //iframe、销毁iframe，释放iframe所占用的内存。
    if (arr.indexOf("iframe") != -1) {
        var _mainIframe = document.getElementById("mainIframe");
        if (_mainIframe) {
            _mainIframe.src = 'about:blank';
            var _mainIframeonloadfuc = function () {
                try {
                    var iframe = _mainIframe.contentWindow;
                    iframe.document.write('');
                    iframe.document.clear();
                    iframe.close();
                    iframe = null;
                } catch (e) {
                }
                _mainIframe.parentNode.removeChild(_mainIframe);
                _mainIframe = null;
                //iframe load后需要执行的方法
                if (iframeLoadFuc) {
                    iframeLoadFuc();
                }
            }

            if (_mainIframe.attachEvent) {
                _mainIframe.detachEvent("onload", _mainIframeonloadfuc);
                _mainIframe.attachEvent("onload", _mainIframeonloadfuc);
            } else {
                _mainIframe.onload = _mainIframeonloadfuc;
            }

        }
    }

}

//渲染移动端设计器的main区
var renderMobileDesignerMain = function () {
    var _tempHtml = new StringBuffer();
    _tempHtml.append("<div class='columnDemo'>");
    for (var i = 0, len = 5; i < len; i++) {
        _tempHtml.append("<div class='columnSection'>");
        _tempHtml.append("<div class='column_header' style='display: block;'><div class='sectionName column_header_title cmp-ellipsis'>" + $.i18n('portal.section.test.title') + "</div><div class='column_header_more' style='display: block;'><i class='vportal vp-forward'></i><span class='more_text'>" + $.i18n('common_more_label') + "</span></div></div>");
        _tempHtml.append("<div class='columnBody'><img width='35' src='" + _ctxPath + "/portal/images/mobile/column-noIData.png' />" + $.i18n('vportal.nodata') + "</div>");
        _tempHtml.append("</div>")
    }
    _tempHtml.append("</div>")
    document.getElementById("main").innerHTML = _tempHtml.toString();
    _tempHtml = null;
}

//渲染某个DOM，可渲染框架元素、框架、everything
var renderTpl = function (_data, _tpl, _domId) { //_data：json数据，_tpl：模板的html，_dom：被渲染的DOM
    //如果_data为""，赋值为{}，避免laytpl报错
    var __data = _data === "" ? {} : _data;
    laytpl(_tpl).render(__data, function (_htmlTpl) {
        document.getElementById(_domId).innerHTML = _htmlTpl;
        _tpl = null;
        _htmlTpl = null;
    });
}

//渲染主框架元素
var renderMainFrameElement = function () {
    /*vPortal.mainFrameElements,它在portal/pagelayout/index.jsp中定义*/
    /*
    渲染思路：
    第一轮循环：查找所有元素的beforeInit和afterInit，如果有，将它们放入一个数组，供后面调用;
    第二轮循环：查找所有元素，如果某个元素有beforeInit方法，执行元素的beforeInit;
    第三轮循环：通过元素的getData返回数据，如果有数据，将数据传到tpl模板中进行渲染，如果无数据，通过innerHTML将元素渲染至对应的dom，如果元素有init方法，执行对应的init();
    第四轮循环：根据第一步查找出来的afterInitElements，执行元素的afterInit
    */

    //步骤一：查找所有元素的beforeInit和afterInit
    var beforeInitElements = {};
    var afterInitElements = {};
    for (var i = 0, len = vPortal.mainFrameElements.length; i < len; i++) {
        //获取第i个元素
        var currentElement = vPortal.mainFrameElements[i];
        var elementId = currentElement.id;
        var elementTplId = currentElement.tpl.split("-")[1];
        if (!vPortal.isDesigner && vPortal.hasLoginSection == "true" && elementTplId == "loginPreBtn") {
            preLoginBtn = $("#" + elementId);
            preLoginBtn.hide();
            continue;
        }
        var elementObject = vPortalMainFrameElements[elementTplId];
        //查找beforeInit
        var _isbeforeInit = (elementObject !== undefined && elementObject.beforeInit !== undefined) ? true : false;
        if (_isbeforeInit) {
            beforeInitElements[i] = {};
            beforeInitElements[i]["id"] = elementId;
            beforeInitElements[i]["tpl"] = elementTplId;
        }
        //查找afterInit
        var _isafterInit = (elementObject !== undefined && elementObject.afterInit !== undefined) ? true : false;
        if (_isafterInit) {
            afterInitElements[i] = {};
            afterInitElements[i]["id"] = elementId;
            afterInitElements[i]["tpl"] = elementTplId;
        }
    }
    //步骤二：执行beforeInit(如果有)
    for (var i in beforeInitElements) {
        var elementId = beforeInitElements[i]["id"];
        var elementTplId = beforeInitElements[i]["tpl"];
        vPortalMainFrameElements[elementTplId].beforeInit && vPortalMainFrameElements[elementTplId].beforeInit(elementId);
    }

    //20181120新增：热点的beforeInit
    if (vPortal.dataJsonPropEvent !== undefined) {
        for (var el in vPortal.dataJsonPropEvent) {
            vPortal.dataJsonPropEvent[el].beforeInit && vPortal.dataJsonPropEvent[el].beforeInit();
        }
    }
    if (vPortal.dataJsonPropEvent !== undefined && vPortal.dataJsonPropEvent.custom !== undefined) {
        for (var el in vPortal.dataJsonPropEvent.custom) {
            vPortal.dataJsonPropEvent.custom[el].beforeInit && vPortal.dataJsonPropEvent.custom[el].beforeInit();
        }
    }

    //步骤三：轮询所有元素，获取数据(getData)并初始化(init)
    for (var i = 0, len = vPortal.mainFrameElements.length; i < len; i++) {
        if (LogoutFlag) { //如果已掉线则不继续渲染元素
            return;
        }
        var currentElement = vPortal.mainFrameElements[i];
        var elementId = currentElement.id;
        var elementTplFullId = currentElement.tpl;
        var elementTplId = elementTplFullId.split("-")[1];
        if (document.getElementById(elementTplFullId)) {
            var elementTplHtml = document.getElementById(elementTplFullId).innerHTML;
        } else {
            console.log(elementTplFullId + "Template file missing");
            continue;
        }
        //获取第i个元素
        var elementObject = vPortalMainFrameElements[elementTplId];
        //获取元素的数据
        var _tempData = (elementObject !== undefined && typeof (elementObject.getData) !== "undefined" && typeof (elementObject.getData) === "function") ? elementObject.getData(elementId) : null;
        //渲染元素
        if (_tempData !== null && _tempData !== undefined) { //元素包含动态数据，需要调用laytpl模板引擎进行渲染
            //如果动态数据返回为false，表示该元素不需要渲染
            if (_tempData !== false) {
                //如果_tempData为数字，转换为string
                var __tempData = isNaN(_tempData) ? _tempData : _tempData.toString();
                renderTpl(__tempData, elementTplHtml, elementId);
            } else {
                continue;
            }
        } else { //没动态数据，无需要调用tpl渲染，直接innerHTML即可
            document.getElementById(elementId).innerHTML = elementTplHtml;
        }
        //初始元素的相关事件（如果有）
        elementObject !== undefined && elementObject.init !== undefined && elementObject.init(elementTplId) !== undefined && elementObject.init(elementTplId);
    }

    //20181120新增：热点的init
    if (vPortal.dataJsonPropEvent !== undefined) {
        for (var el in vPortal.dataJsonPropEvent) {
            vPortal.dataJsonPropEvent[el].init && vPortal.dataJsonPropEvent[el].init();
        }
    }
    if (vPortal.dataJsonPropEvent !== undefined && vPortal.dataJsonPropEvent.custom !== undefined) {
        for (var el in vPortal.dataJsonPropEvent.custom) {
            vPortal.dataJsonPropEvent.custom[el].init && vPortal.dataJsonPropEvent.custom[el].init();
        }
    }

    //步骤四：执行afterInit(如果有)
    for (var i in afterInitElements) {
        var elementId = afterInitElements[i]["id"];
        var elementTplId = afterInitElements[i]["tpl"];
        vPortalMainFrameElements[elementTplId].afterInit && vPortalMainFrameElements[elementTplId].afterInit(elementId);
    }

    //20181120新增：热点的beforeInit
    if (vPortal.dataJsonPropEvent !== undefined) {
        for (var el in vPortal.dataJsonPropEvent) {
            vPortal.dataJsonPropEvent[el].afterInit && vPortal.dataJsonPropEvent[el].afterInit();
        }
    }
    if (vPortal.dataJsonPropEvent !== undefined && vPortal.dataJsonPropEvent.custom !== undefined) {
        for (var el in vPortal.dataJsonPropEvent.custom) {
            vPortal.dataJsonPropEvent.custom[el].afterInit && vPortal.dataJsonPropEvent.custom[el].afterInit();
        }
    }
    vPortal.mainFrameIsLoad = true;
}


/*--事件触发类的公共函数--*/

//为document添加监听，处理一些事件
if (document.attachEvent) { //ie6-8
    document.attachEvent("onclick", function () {
        docEventList();
    });
} else if (document.addEventListener) { //ie9+、chrome、firefox
    document.addEventListener("click", function () {
        docEventList();
    });
}
;

// 当点击页面时，需要隐藏某些元素
var docEventList = function () {
    var _hideElement = document.querySelectorAll(".autoHideElement");
    if (_hideElement && _hideElement.length > 0) {
        for (var i = 0, len = _hideElement.length; i < len; i++) {
            _hideElement[i].style.display = "none";
        }
    }
}

/*-- 动态加载JS --*/
var loadScript = function (url, callback, cache) {
    $.ajax({
        type: 'GET',
        url: url,
        success: callback,
        dataType: 'script',
        ifModified: true,
        beforeSend: CsrfGuard.beforeAjaxSend,
        cache: true
    });
};

/**
 * 打开部门空间设计界面
 */
var vPortalSpaceDesignerDialog;

function editDepartmentSpace(spaceId, spacePath) {
    var dwidth = $(top).width();
    var dheight = $(top).height();
    vPortalSpaceDesignerDialog = $.dialog({
        targetWindow: top,
        id: 'vPortalSpaceDesignerDialog',
        url: '/seeyon/portal/portalDesigner.do?method=spaceDesigner&accountType=&from=departmentSpaceSetting&spaceId=' + spaceId,
        width: dwidth,
        height: dheight,
        title: $.i18n('portal.system.menu.spaceset'),
        overflow: 'hidden',
        transParams: {
            vPortalSpaceDesignerDialogWindowObj: window
        },
        closeParam: {
            'show': true,
            handler: function () {
                vPortalSpaceDesignerDialog = null;
            }
        }
    });
}

/** 点击菜单跳转二级页面 **/
var showMenu = function (url, id, target, resourceCode, _obj) {
    //zhou
    var oldUrl = url;
    var dialog;
    if (url.indexOf("spaceController") != -1 && url.indexOf("showThemSpace") != -1) {

        dialog = $.dialog({
            id: 'sureLogin',
            url: '/seeyon/ext/sureLogin.do?method=index',
            width: 500,
            height: 500,
            title: '请输入密码',
            checkMax: true,
            buttons: [{
                text: "确认",
                handler: function () {
                    var info = dialog.getReturnValue();
                    var flag = info.data[0].flag;
                    if (flag == 'login') {
                        var password = info.data[0].password;
                        $.post("/seeyon/ext/sureLogin.do?method=toLogin", {pwd: password}, function (res) {
                            var code = res.code;
                            if (code == 1) {
                                dialog.close();
                                window.open(url, '_blank ');
                            } else {
                                alert("密码不正确！");
                            }
                        });
                    } else if (flag == 'set') {
                        dialog.startLoading();
                        var first = info.data[0].firstPwd;
                        var sure = info.data[0].surePwd;
                        var answer = info.data[0].answer;
                        if (first != sure) {
                            alert("密码不一致!");
                        } else {
                            if (answer == '') {
                                alert("找回设置不能为空！");
                            } else {
                                $.post("/seeyon/ext/sureLogin.do?method=doSave", {pwd: first, answer: answer}, function (res) {
                                    var code = res.code;
                                    if (code == 1) {
                                        dialog.close();
                                        var r = confirm("现在打开吗？");
                                        if (r == true) {
                                            dialog.close();
                                            window.open(url, '_blank ');
                                        }
                                    }
                                });
                            }
                        }
                    } else if (flag == 'find') {
                        var answer = info.data[0].answer;
                        $.post("/seeyon/ext/sureLogin.do?method=check", {answer: answer}, function (res) {
                            if(res.code==1){
                                alert("你的密码是："+res.pwd);
                            }
                        });

                    }


                    // alert(JSON.stringify(peoples));
                    // dialog.startLoading()
                    // var oooooooooo = dialog.getReturnValue({ 'name': 'macj' });
                    // dialog.close();
                    // window.open(url, '_blank ');
                    // openCtpWindow({
                    //     url:b,//新窗口地址
                    // });
                }
            }, {
                text: "取消",
                handler: function () {
                    // dialog.endLoading()
                    dialog.close();
                }
            }]
        });
    } else {
        vPortal.isFromShowMenu = true;
        if (vPortal && vPortal.isDesigner) {
            return;
        }
        var departmentSpaceManageUrl = "/seeyon/showDepartmentSpaceDesigner.do?spaceId=";
        if (url.indexOf(departmentSpaceManageUrl) == 0) { //弹出部门空间设计器界面
            var spaceId = url.substring(departmentSpaceManageUrl.length); //部门空间ID
            editDepartmentSpace(spaceId, "");
        } else {
            showMask();
            // 公文应用设置在非IE下不允许进入
            /*if (!$.browser.msie && url === _ctxPath + "/edocController.do?method=sysCompanyMain") {
                $.alert("公文应用设置页面不支持此浏览器！");
                hideMask();
                return;
            }*/
            if (url.indexOf('?') == -1) {
                url += "?";
            } else {
                url += "&";
            }
            url += "portalId=" + vPortal.portalId;
            // 增加rescode的判断，用于同步新建协同窗口的url，避免可以同时打开两个
            if (resourceCode) {
                if (url.indexOf("_resourceCode") == -1) {
                    if (url.indexOf('?') == -1) {
                        url += "?";
                    } else {
                        url += "&";
                    }
                    url += "_resourceCode=" + resourceCode;
                }
            }
            var checkUrl1 = _ctxPath + "http";
            var checkUrl2 = _ctxPath + "https";
            if (url.indexOf(checkUrl1) == 0) {
                url = url.substring(7, url.length);
            } else if (url.indexOf(checkUrl2) == 0) {
                url = url.substring(8, url.length);
            }

            url += CsrfGuard.getUrlSurffix(url);
            //判断打开类型
            if (target === "newWindow") {
                if (url.indexOf('showAbout') > 0) {
                    vPortalMainFrameElements.topRightsystemOperation.showAbout();
                } else if (url.indexOf('showHelp') > 0) {
                    vPortalMainFrameElements.topRightsystemOperation.showHelp();
                } else if (url.indexOf("newPlan") != -1) {
                    openCtpWindow({
                        'url': url,
                        'id': vPortal.CurrentUser.id
                    });
                } else {
                    openCtpWindow({
                        'url': url
                    });
                }
            } else if (target === "portal") {
                var portalId = oldUrl;
                if (oldUrl.indexOf("/seeyon") == 0) {
                    portalId = oldUrl.substring(7, oldUrl.length);
                }
                //门户的打开方法，参考磁贴打开门户的方式
                if (id == vPortal.portalId) {
                    $.alert($.i18n('portal.currentportalIsOpened'));
                } else {
                    //门户地址打开
                    var _openUrl = "/seeyon/main.do?method=main&portalId=" + portalId + "&subPortal=true" + CsrfGuard.getUrlSurffix();
                    openCtpWindow({
                        'url': _openUrl,
                        'id': portalId
                    });
                }
            } else {
                //模板自定义的一些函数,如“入口式”需要先变个型，才继续展示二级页面
                if (top.vPortal && top.vPortal.layoutFunction && top.vPortal.layoutFunction.init) {
                    top.vPortal.layoutFunction.init(url, target);
                }
                showLocation();
                if (url.indexOf("linkConnectForMenu") != -1) {
                    url = url + "&target=mainFrame";
                    showLocation("");
                }

                //主题空间需要在这里判断下，不然会在主题空间里面再生成一个mainIframe
                var _select = document;
                if (vPortal.isThemeSpace) {
                    _select = getCtpTop().document;
                }
                var portalBody = _select.getElementById("seeyonPortalBody");
                var mainDiv = _select.getElementById("main");
                var mainIframe = _select.getElementById("mainIframe");

                if (url.indexOf("spaceController.do?method=showThemSpace") != -1) {
                    autoLeftMargin(mainDiv, false, url);
                } else {
                    autoLeftMargin(mainDiv, true, url);
                }

                if (typeof (isCtpTop) !== "undefined" && isCtpTop) {
                    mainDiv.style.overflowY = "hidden";
                }

                if (SeeUtils.isIE8) {
                    url += "&random=" + Math.floor(Math.random() * 10000); //ie8 iframe切换时，如果是相同url时会白屏1秒以上，加个随机数
                }

                //存在iframe就直接切换
                if (mainIframe) {
                    mainIframe.setAttribute('src', url);
                } else {
                    mainDiv.className = mainDiv.className + ' hasIframe';

                    //不存在就生成iframe
                    var iframe = new StringBuffer();
                    iframe.append('<iframe id="mainIframe" name="mainIframe" frameborder="0" allowtransparency="true" height="100%" src=\"' + url + '\" width="100%"></iframe>')
                    mainDiv.innerHTML = iframe.toString();
                }
                //start:zhou
                if (url.indexOf("spaceController") != -1 && url.indexOf("showThemSpace") != -1) {
                    window.location.href = "/seeyon/ext/sureLogin.do?method=sureLoginPage&datetime=" + Math.random();
                } else {
                    if (url.indexOf("showThemSpace") > -1) {
                        vPortal.mainIsSpace = true
                    } else {
                        vPortal.mainIsSpace = false
                    }
                }
                //end:zhou
            }

            setMenuListHighlight(_obj);
            hideMask();
        }
        vPortal.isFromShowMenu = false;
    }
}

//菜单高亮效果
var setMenuListHighlight = function (_obj) {
    if (typeof (_obj) === "undefined") {
        return;
    }
    var _allLev1Li = document.querySelectorAll(".lev1Li");
    if (typeof (_allLev1Li) !== "undefined" && _allLev1Li.length > 0) {
        //去除所有一级菜单的高亮效果
        for (var i = 0; i < _allLev1Li.length; i++) {
            SeeUtils.removeClass(_allLev1Li[i], "current");
        }
        ;
        var _allLev2Li = document.querySelectorAll(".lev2Li");
        if (typeof (_allLev2Li) !== "undefined" && _allLev2Li.length > 0) {
            //去除所有二级菜单的高亮效果
            for (var i = 0; i < _allLev2Li.length; i++) {
                SeeUtils.removeClass(_allLev2Li[i], "current");
            }
            ;
            var _allLev3Li = document.querySelectorAll(".lev3Li");
            if (typeof (_allLev3Li) !== "undefined" && _allLev3Li.length > 0) {
                //去除所有三级菜单的高亮效果
                for (var i = 0; i < _allLev3Li.length; i++) {
                    SeeUtils.removeClass(_allLev3Li[i], "current");
                }
                ;
            }
        }
        //如果当前是二级菜单下的，高亮一二级菜单
        if (typeof (_obj.parentNode) != 'undefined' && _obj.parentNode && _obj.parentNode.className.indexOf("lev2Li") > -1) {
            SeeUtils.addClass(SeeUtils.getParentsByClass(_obj, ".lev1Li")[0], "current");
            SeeUtils.addClass(SeeUtils.getParentsByClass(_obj, ".lev2Li")[0], "current");
        }
        //如果当前是三级菜单下的，高亮一二三级菜单
        if (typeof (_obj.parentNode) != 'undefined' && _obj.parentNode && _obj.parentNode.className.indexOf("lev3Li") > -1) {
            SeeUtils.addClass(SeeUtils.getParentsByClass(_obj, ".lev1Li")[0], "current");
            SeeUtils.addClass(SeeUtils.getParentsByClass(_obj, ".lev2Li")[0], "current");
            SeeUtils.addClass(SeeUtils.getParentsByClass(_obj, ".lev3Li")[0], "current");
        }
    }
}
//通过resourceCode来设置菜单的高亮效果
var setMenuListHighlight4resourceCode = function (_resourceCode) {
    setMenuListHighlight(document.getElementById(_resourceCode));
}


/*栏目更多页面现在当前位置方法
 * 参考老的frount_commonjs添加，图片先用原来的图片代替
 * @author yinr   业务配置栏目
 */
function showMoreSectionLocation(text) {
    var html = '<span class="nowLocation_ico"><img src="' + _ctxPath + '/main/skin/frame/default/menuIcon/moresectionicon.png"></span>';
    html += '<span class="nowLocation_content">';
    html += '<a>' + escapeStringToHTML(text, false) + '</a>';
    html += '</span>';
    getCtpTop().showLocation(html);
}

//面包屑：显示当前位置
function showLocation(html, type) {
    //补充逻辑，有些地方把这里传入了空，实际是要隐藏面包屑
    if (!html && !type) hideLocation();
    if (typeof (vPortal.showLocation) !== "undefined" && !vPortal.showLocation) return;  //如果vPortal.showLocation为false，不论从哪里调用的showLocation，都不继续执行了，以门户中配置的是否显示面包屑为准(vPortal.showLocation值)

    var _breadcrumbDom = document.getElementById("breadcrumb");
    var _main = document.getElementById("main");

    //第一次的时候记录高度
    if (mainParentNodeH == 0 && _main.parentNode.offsetHeight != 0) {
        if (document.querySelector(".webStyle")) { //网页式，父元素没高度，不能直接取它的值
            mainParentNodeH = 995;
        } else {  //非网页式，父元素是绝对定位的，对取它的高
            mainParentNodeH = _main.parentNode.offsetHeight;
        }
    }

    //之前v3x-debug里面showCtpLocation的逻辑
    if (type && type.html) {
        if (top.showLocation) {
            var icon = "vp-personal";
            var html = '<i class="vportal ' + icon + '"></i>';
            type.html = html + type.html;
            if (_main.getAttribute("class").indexOf("hasIframe") != -1) {
                _main.style.height = mainParentNodeH - 40 + "px";
            }
            top.showLocation(type.html);

        }
        return;
    }

    //不同空间类型，如果后面替换为图标的话，这里需要对传入的html进行替换
    if (_breadcrumbDom && html && html != "") {
        if (_main.getAttribute("class").indexOf("hasIframe") != -1 || (vPortal.layoutFunction && vPortal.layoutFunction.mainAreaNeedScroll)) {
            _main.style.height = mainParentNodeH - 40 + "px";
        }
        //显示
        _breadcrumbDom.innerHTML = html;
        _breadcrumbDom.style.display = "block";

    }
}

var showCtpLocation = showLocation;

//隐藏当前位置
function hideLocation() {
    var _breadcrumbDom = document.getElementById("breadcrumb");
    var _main = document.getElementById("main");
    if (_main && window.location.href.indexOf("showThemSpace") == -1 && window.location.href.indexOf("bpmPortal") == -1) {
        if (_breadcrumbDom) {
            _breadcrumbDom.innerHTML = "";
            _breadcrumbDom.style.display = "none";
            _main.style.top = "0";
            _main.style.height = "100%";
        } else {
            _main.style.height = "100%";
        }
    }
}

/**
 * 从vPortal.space中查看当前空间的index
 */
var getSpaceIndex = function (_spaceId) {
    for (var i = 0, _len = vPortal.space.length; i < _len; i++) {
        if (vPortal.space[i].navType == "space" && (vPortal.space[i].id == _spaceId || vPortal.space[i].spaceId == _spaceId)) {
            return i;
        }
        //新窗口打开的子门户空间、关联系统等等也需要返回index   项目空间 mainfrm
        if ((vPortal.space[i].openType == "newWindow" || vPortal.space[i].openType == "mainfrm") && vPortal.space[i].id == _spaceId) {
            return i;
        }
        if (vPortal.space[i].navType == "spaceGroup" && vPortal.space[i].list && vPortal.space[i].list.length > 0) {
            for (var j = 0, _len1 = vPortal.space[i].list.length; j < _len1; j++) {
                if (vPortal.space[i].list[j].navType == "space" && (vPortal.space[i].list[j].id == _spaceId || vPortal.space[i].list[j].spaceId == _spaceId)) {
                    return [i, j];
                }
            }
        }
    }
    return -1;
}

// 刷新当前空间
var refreshCurrentSpace = function (_from) {
    if (window.location.href.indexOf("showThemSpace") > -1) {
        //主题空间
        if (getCtpTop()["removeOnbeforeunload"] != null) {
            getCtpTop()["removeOnbeforeunload"]();
        }
        window.location.reload();
    } else {
        //普通空间
        var spaceIndex = getSpaceIndex(vPortal.currentSpaceId);
        if (spaceIndex !== -1) {
            if (spaceIndex instanceof Array) {
                //空间在vPortal.space下的某个list中
                var _currentSpaceObject = vPortal.space[spaceIndex[0]].list[spaceIndex[1]];
            } else {
                //空间直接位于vPortal.space下
                var _currentSpaceObject = vPortal.space[spaceIndex];
            }
            if (_currentSpaceObject.openType === "newWindow") {
                return;
            }
            if (_from == "resize" && vPortal.layoutFunction && typeof (vPortal.layoutFunction.reizeMenu) == "function") {
                vPortal.layoutFunction.reizeMenu();
            } else if (_from != "notFreshMenuNav") {
                refreshSpaceMenuNavForPortal(vPortal.currentSpaceId, "true");
            }
            initV5Space(_currentSpaceObject, "isForceLoadData");
        }
    }
}

//刷新当前空间的导航或菜单：如果单导航时，刷新导航，如果空间+菜单时，刷新菜单
var refreshCurrentSpaceMenuNavForPortal = function (_isForceLoadData) {
    refreshSpaceMenuNavForPortal(vPortal.currentSpaceId, _isForceLoadData);
}

//刷新导航或菜单：如果单导航时，刷新导航，如果空间+菜单时，刷新菜单
var refreshSpaceMenuNavForPortal = function (_spaceId, _isForceLoadData) {
    var elementId, elementTpl, elementTplId;
    //从vPortal.mainFrameElements中分析当前模板用的导航元素还是菜单元素
    for (var i = 0; i < vPortal.mainFrameElements.length; i++) {
        var _currentElementId = vPortal.mainFrameElements[i].id;
        if (_currentElementId == "menuNav") {
            elementId = _currentElementId;
            elementTpl = vPortal.mainFrameElements[i].tpl;
            elementTplId = vPortal.mainFrameElements[i].tpl.split("-")[1];
            break;
        }
    }
    if (typeof (elementId) === "undefined") {
        for (var i = 0; i < vPortal.mainFrameElements.length; i++) {
            var _currentElementId = vPortal.mainFrameElements[i].id;
            if (_currentElementId == "topNav" || _currentElementId == "leftNav" || _currentElementId == "miniNav") {
                elementId = _currentElementId;
                elementTpl = vPortal.mainFrameElements[i].tpl;
                elementTplId = vPortal.mainFrameElements[i].tpl.split("-")[1];
                break;
            }
        }
    }
    if (typeof (elementId) === "undefined") {
        console.error("No navigation or menu in the page");
        return;
    }
    if (vPortalMainFrameElements[elementTplId]) {
        var elementObject = vPortalMainFrameElements[elementTplId];
    } else {
        console.error("The corresponding menu object was not found in JS Files");
        return;
    }
    // var menuData = vPortal.menu;//如果门户统一配置菜单则用系统菜单
    //后台配置为使用空间下菜单的时候获取一下空间菜单
    // getSpaceMenusForPortal(_spaceId,_isForceLoadData,elementObject);

    //因菜单的渲染均在afterInit中，所以执行afterInit，afterInit里面有获取数据的逻辑，故不再传入
    elementObject.afterInit && elementObject.afterInit(elementId, _spaceId, _isForceLoadData);
}
//更新面包屑用的menu对象(切换空间的时候用于存储空间菜单)
var updateLocationMenus = function (menus) {
    if (menus && menus.length > 0) {
        vPortal.memberMenus = menus;
    } else {
        vPortal.memberMenus = [];
    }
}
/** ！！ 注意 ！！ **/
/** 传入elementObject的时候采用异步取数据，然后执行elementObject的afterInitSuccess;  --  适用于初始化**/
/** 没有传入elementObject的时候采用同步取数据，然后返回数据 -- 适用于非初始化的交互场景 **/
//获取某空间的菜单数据
var getSpaceMenusForPortal = function (_spaceId, _isForceLoadData, elementObject, _extParameter) {
    //如果缓存中有该空间的菜单数据，取缓存中的即可，否则通过ajax请求后台
    if (vPortal.spaceMenusCacheObj && vPortal.spaceMenusCacheObj[_spaceId] !== undefined && vPortal.spaceMenusCacheObj[_spaceId] !== null && _isForceLoadData === undefined) {
        if (_extParameter !== "notUpdateMemberMenu") {
            updateLocationMenus(vPortal.spaceMenusCacheObj[_spaceId]);
        }
        if (elementObject) {
            //缓存中有
            elementObject.afterInitSuccess && elementObject.afterInitSuccess(vPortal.spaceMenusCacheObj[_spaceId]);
        } else {
            return vPortal.spaceMenusCacheObj[_spaceId];
        }
    } else { //缓存中无，或指定了需要强制请求(_isForceLoadData)，需要ajax请求
        var menuSource = "";
        if (vPortal.themeHotspots && vPortal.themeHotspots.leftMenuNav && vPortal.themeHotspots.leftMenuNav.menuSource) {
            menuSource = vPortal.themeHotspots.leftMenuNav.menuSource;
        }
        if (elementObject) {
            // var menuData=callBackendMethod("portalManager", "getPortalMenus",vPortal.portalId,_spaceId,menuSource);
            callBackendMethod("portalManager", "getPortalMenus", vPortal.portalId, _spaceId, menuSource, {
                success: function (data) {
                    if (data == '__LOGOUT') {
                        offlineFun();
                        return;
                    }
                    if (vPortal.spaceMenusCacheObj === undefined) {
                        vPortal.spaceMenusCacheObj = new Object();
                    }
                    vPortal.spaceMenusCacheObj[_spaceId] = data;
                    updateLocationMenus(data);
                    // var menuData = replaceMenuIcon(data);
                    //因菜单的渲染均在afterInitSuccess中，所以执行afterInitSuccess
                    elementObject && elementObject.afterInitSuccess && elementObject.afterInitSuccess(data);

                }
            });
        } else {
            //没有传elementObject的采用同步！   非初始化，不会阻塞dom渲染
            var menuData = callBackendMethod("portalManager", "getPortalMenus", vPortal.portalId, _spaceId, menuSource);
            if (vPortal.spaceMenusCacheObj === undefined) {
                vPortal.spaceMenusCacheObj = new Object();
            }
            vPortal.spaceMenusCacheObj[_spaceId] = menuData;
            if (_extParameter !== "notUpdateMemberMenu") {
                updateLocationMenus(menuData);
            }
            return menuData;
        }
    }
}

// 分页加载菜单
vPortal.eachPageMenusNum = 20; //每页加载20条
var getSpaceMenusForPortalPage = function (_spaceId, elementObject, _eachPageMenusNum, _page, _isForceLoadData, _callBackFun) {
    if (typeof (_eachPageMenusNum) === "undefined") {
        _eachPageMenusNum = vPortal.eachPageMenusNum;
    }
    var menuSource = "";
    if (vPortal.themeHotspots && vPortal.themeHotspots.leftMenuNav && vPortal.themeHotspots.leftMenuNav.menuSource) {
        menuSource = vPortal.themeHotspots.leftMenuNav.menuSource;
    }
    if (vPortal.spaceMenusPageCacheObj === undefined) { //分页请求回来的数据，放进这个缓存对象下
        vPortal.spaceMenusPageCacheObj = new Object();
    }
    if (vPortal.spaceMenusPageCacheObj[_spaceId] === undefined) {
        vPortal.spaceMenusPageCacheObj[_spaceId] = new Object();
        vPortal.spaceMenusPageCacheObj[_spaceId].dataMenu = new Array();
        vPortal.spaceMenusPageCacheObj[_spaceId].hasMore = true;
        vPortal.spaceMenusPageCacheObj[_spaceId].cachedPage = _page;
        vPortal.spaceMenusPageCacheObj[_spaceId].isPending = false;
    }
    if (vPortal.spaceMenusPageCacheObj[_spaceId].hasMore || typeof (_isForceLoadData) !== "undefined" && _isForceLoadData) { //hasMore为true，表示还有菜单，向后台发请求取菜单；_isForceLoadData为true也表示需要强制请求菜单数据
        if (!vPortal.spaceMenusPageCacheObj[_spaceId].isPending) {
            vPortal.spaceMenusPageCacheObj[_spaceId].isPending = true;
            var data = callBackendMethod("portalManager", "getPortalMenusPage", vPortal.portalId, _spaceId, menuSource, _eachPageMenusNum, _page); //同步
            if (data == '__LOGOUT') {
                offlineFun();
                return;
            }
            if (data == null) {
                return;
            }
            var currentIndex = _eachPageMenusNum * (_page - 1);
            if (data.dataMenu !== null) {
                for (var i = 0; i < data.dataMenu.length; i++) {
                    vPortal.spaceMenusPageCacheObj[_spaceId].dataMenu[currentIndex] = data.dataMenu[i];
                    currentIndex++;
                }
                updateLocationMenus(vPortal.spaceMenusPageCacheObj[_spaceId].dataMenu);
            }
            vPortal.spaceMenusPageCacheObj[_spaceId].dataMenu.length = currentIndex;  //部分菜单被菜单管理员删除时，重设length可起到从缓存数据中删除已失效菜单的效果
            vPortal.spaceMenusPageCacheObj[_spaceId].hasMore = data.hasMore;
            vPortal.spaceMenusPageCacheObj[_spaceId].cachedPage = _page;
            vPortal.spaceMenusPageCacheObj[_spaceId].isPending = false;
            //因菜单的渲染均在afterInitSuccess中，所以执行afterInitSuccess
            elementObject && elementObject.afterInitSuccess && elementObject.afterInitSuccess(vPortal.spaceMenusPageCacheObj[_spaceId].dataMenu, vPortal.spaceMenusPageCacheObj[_spaceId].hasMore);
            typeof (_callBackFun) !== "undefined" && _callBackFun(data);
            data = null;
        } else {
            return;
        }
    } else { //hasMore为false，表示没有更多菜单了，无需要继续请求，直接使用缓存中的菜单
        elementObject && elementObject.afterInitSuccess && elementObject.afterInitSuccess(vPortal.spaceMenusPageCacheObj[_spaceId].dataMenu, vPortal.spaceMenusPageCacheObj[_spaceId].hasMore);
        _callBackFun && _callBackFun();
    }
}

/** ！！ 注意 ！！ **/
/** 传入elementObject的时候采用异步取数据，然后执行elementObject的afterInitSuccess;  --  适用于初始化**/
/** 没有传入elementObject的时候采用同步取数据，然后返回数据 -- 适用于非初始化的交互场景 **/
//获取导航数据，导航可能包含这几类数据：门户,空间(包含第三方系统空间),一级菜单,关联系统空间,项目空间
var getNavData = function (_portalId, needMenu, elementObject, _isForceLoadData) {
    var menuNav = document.getElementById("menuNav");
    var hasMenu = "0";
    if (!menuNav) {
        hasMenu = "1";
    }
    if (needMenu) {
        hasMenu = needMenu;
    }
    var needSpaceGroup = "0"; //当前门户空间是否分组
    var needPortalGroup = "0"; //其它门户是否分组
    if (vPortal.themeHotspots && vPortal.themeHotspots.topCenterSpaceNav) {
        if (vPortal.themeHotspots.topCenterSpaceNav.portalNavSpaceGroup) {
            needSpaceGroup = vPortal.themeHotspots.topCenterSpaceNav.portalNavSpaceGroup;
        }
        if (vPortal.themeHotspots.topCenterSpaceNav.portalNavPortalGroup) {
            needPortalGroup = vPortal.themeHotspots.topCenterSpaceNav.portalNavPortalGroup;
        }
    }
    var needTileSecondMenu = "1"; //当导航中只有一个菜单的时候,是否把二级菜单平铺,配合hasMenu 使用
    // return callBackendMethod("portalNavManager", "getCurrentUserNav", _portalId, hasMenu, needSpaceGroup, needPortalGroup,needTileSecondMenu);
    if (elementObject) {
        callBackendMethod("portalNavManager", "getCurrentUserNav", _portalId, hasMenu, needSpaceGroup, needPortalGroup, needTileSecondMenu, {
            success: function (data) {
                if (data == '__LOGOUT') {
                    offlineFun();
                    return;
                }
                if (data) {
                    //因导航的渲染均在afterInitSuccess中，所以执行afterInitSuccess
                    elementObject.afterInitSuccess && elementObject.afterInitSuccess(data);
                }
            }
        });
    } else {
        //没有传elementObject的采用同步！   非初始化，不会阻塞dom渲染
        return callBackendMethod("portalNavManager", "getCurrentUserNav", _portalId, hasMenu, needSpaceGroup, needPortalGroup, needTileSecondMenu);
    }
}

//分页请求导航的数据
vPortal.eachPageNavsNum = 20; //每页加载20条
var getNavDataPage = function (_portalId, needMenu, elementObject, _eachPageNavsNum, _page, _isForceLoadData, _callBackFun) {
    if (typeof (_eachPageNavsNum) === "undefined") {
        _eachPageNavsNum = vPortal.eachPageNavsNum;
    }
    var menuNav = document.getElementById("menuNav");
    var hasMenu = "0";
    if (!menuNav) {
        hasMenu = "1";
    }
    if (needMenu) {
        hasMenu = needMenu;
    }
    var needSpaceGroup = "0"; //当前门户空间是否分组
    var needPortalGroup = "0"; //其它门户是否分组
    if (vPortal.themeHotspots && vPortal.themeHotspots.topCenterSpaceNav) {
        if (vPortal.themeHotspots.topCenterSpaceNav.portalNavSpaceGroup) {
            needSpaceGroup = vPortal.themeHotspots.topCenterSpaceNav.portalNavSpaceGroup;
        }
        if (vPortal.themeHotspots.topCenterSpaceNav.portalNavPortalGroup) {
            needPortalGroup = vPortal.themeHotspots.topCenterSpaceNav.portalNavPortalGroup;
        }
    }
    var needTileSecondMenu = "1"; //当导航中只有一个菜单的时候,是否把二级菜单平铺,配合hasMenu 使用
    if (vPortal.spaceNavsPageCacheObj === undefined) { //分页请求回来的数据，放进这个缓存对象下
        vPortal.spaceNavsPageCacheObj = new Object();
        vPortal.spaceNavsPageCacheObj[_portalId] = new Object();
        vPortal.spaceNavsPageCacheObj[_portalId].dataNav = new Array();
        vPortal.spaceNavsPageCacheObj[_portalId].hasMore = true;
        vPortal.spaceNavsPageCacheObj[_portalId].cachedPage = _page;
        vPortal.spaceNavsPageCacheObj[_portalId].isPending = false;
    }
    if (vPortal.spaceNavsPageCacheObj[_portalId].hasMore || typeof (_isForceLoadData) !== "undefined" && _isForceLoadData) { //hasMore为true，表示还有菜单，向后台发请求取菜单；_isForceLoadData为true也表示需要强制请求菜单数据
        if (!vPortal.spaceNavsPageCacheObj[_portalId].isPending) {
            vPortal.spaceNavsPageCacheObj[_portalId].isPending = true;
            var data = callBackendMethod("portalNavManager", "getCurrentUserNavPage", _portalId, hasMenu, needSpaceGroup, needPortalGroup, needTileSecondMenu, _eachPageNavsNum, _page);  //同步
            if (data == '__LOGOUT') {
                offlineFun();
                return;
            }
            if (data == null) {
                return;
            }
            var currentIndex = _eachPageNavsNum * (_page - 1);
            if (data.dataNav !== null) {
                for (var i = 0; i < data.dataNav.length; i++) {
                    vPortal.spaceNavsPageCacheObj[_portalId].dataNav[currentIndex] = data.dataNav[i];
                    currentIndex++;
                }
            }
            vPortal.spaceNavsPageCacheObj[_portalId].dataNav.length = currentIndex;  //部分导航菜单被菜单管理员删除时，重设length可起到从缓存数据中删除已失效导航菜单的效果
            vPortal.spaceNavsPageCacheObj[_portalId].hasMore = data.hasMore;
            vPortal.spaceNavsPageCacheObj[_portalId].cachedPage = _page;
            vPortal.spaceNavsPageCacheObj[_portalId].isPending = false;
            //更新缓存
            vPortal.space = vPortal.spaceNavsPageCacheObj[_portalId].dataNav;
            //因菜单的渲染均在afterInitSuccess中，所以执行afterInitSuccess
            elementObject && elementObject.afterInitSuccess && elementObject.afterInitSuccess(vPortal.spaceNavsPageCacheObj[_portalId].dataNav, vPortal.spaceNavsPageCacheObj[_portalId].hasMore);
            typeof (_callBackFun) !== "undefined" && _callBackFun(data);
            data = null;
        } else {
            return;
        }
    } else { //hasMore为false，表示没有更多菜单了，无需要继续请求，直接使用缓存中的菜单
        elementObject && elementObject.afterInitSuccess && elementObject.afterInitSuccess(vPortal.spaceNavsPageCacheObj[_portalId].dataNav, vPortal.spaceNavsPageCacheObj[_portalId].hasMore);
        _callBackFun && _callBackFun();
    }
}

//更新vPortal.space中的id和path
var updateSpacePathAndId4Cache = function (_newId, _newPath) {
    var _oldspaceIndex = getSpaceIndex(vPortal.currentSpaceId);
    //空间未被组合进一组时：
    if (vPortal.space[_oldspaceIndex] && vPortal.space[_oldspaceIndex].navType == "space") {
        vPortal.space[_oldspaceIndex].id = _newId;
        vPortal.space[_oldspaceIndex].url = _newPath;
    } else { //被分组了
        var groupIndex = _oldspaceIndex[0];
        var spaceIndex = _oldspaceIndex[1];
        vPortal.space[groupIndex].list[spaceIndex].id = _newId;
        vPortal.space[groupIndex].list[spaceIndex].url = _newPath;
    }
    vPortal.currentSpaceId = _newId;
}

/*--针对浏览器兼容性，进行一些扩展--*/

// console
if (!window.console) {
    window.console = window.console || (function () {
        var c = {};
        c.log = c.warn = c.debug = c.info = c.error = c.time = c.dir = c.profile = c.clear = c.exception = c.trace = c.assert = function () {
        };
        return c;
    })();
}

//function bind
if (!Function.prototype.bind) {
    Function.prototype.bind = function (oThis) {
        if (typeof this !== "function") {
            throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
        }
        var aArgs = Array.prototype.slice.call(arguments, 1),
            fToBind = this,
            fNOP = function () {
            },
            fBound = function () {
                return fToBind.apply(this instanceof fNOP && oThis ? this : oThis,
                    aArgs.concat(Array.prototype.slice.call(arguments)));
            };
        fNOP.prototype = this.prototype;
        fBound.prototype = new fNOP();
        return fBound;
    };
}

//querySelectorAll for IE8-
if (!document.querySelectorAll) {
    document.querySelectorAll = function (selectors) {
        var style = document.createElement('style'),
            elements = [],
            element;
        document.documentElement.firstChild.appendChild(style);
        document._qsa = [];

        style.styleSheet.cssText = selectors + '{x-qsa:expression(document._qsa && document._qsa.push(this))}';
        window.scrollBy(0, 0);
        style.parentNode.removeChild(style);

        while (document._qsa.length) {
            element = document._qsa.shift();
            element.style.removeAttribute('x-qsa');
            elements.push(element);
        }
        document._qsa = null;
        return elements;
    };
}

//getElementsByClassName for IE8-
if (!document.getElementsByClassName) {
    document.getElementsByClassName = function (className, element) {
        var children = (element || document).getElementsByTagName('*');
        var elements = new Array();
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            var classNames = child.className.split(' ');
            for (var j = 0; j < classNames.length; j++) {
                if (classNames[j] == className) {
                    elements.push(child);
                    break;
                }
            }
        }
        return elements;
    };
}

//Object keys ie8-
if (!Object.keys) Object.keys = function (o) {
    if (o !== Object(o))
        throw new TypeError('Object.keys called on a non-object');
    var k = [],
        p;
    for (p in o)
        if (Object.prototype.hasOwnProperty.call(o, p)) k.push(p);
    return k;
}


//fixRgba2LowIE IE8转一下颜色值
function fixRgba2LowIE(_color) {
    if (_color.length == 0) return _color;
    if (_color.indexOf("rgba") != -1 && SeeUtils.isIE8) {
        var _colorArr = _color.replace("rgba(", "").split(",", 3);
        _color = "rgb(" + _colorArr[0] + "," + _colorArr[1] + "," + _colorArr[2] + ")";
        return _color;
    } else {
        return _color;
    }
}

//stopPropagation
function stopProp(event) {
    //非IE浏览器
    if (event && event.stopPropagation) {
        event.stopPropagation();
    } else {
        window.event.cancelBubble = true;
    }
}

var selectPictureDialog;

/**
 * 从图片库选择图片
 * msn:最大可选择的图片个数
 * @param catagory 分类
 */
function selectPicture(msn, catagory, saveSelectImageIdFunction) {
    var dwidth = $(top).width();
    var dheight = $(top).height();
    selectPictureDialog = $.dialog({
        targetWindow: top,
        id: 'selectPictureDialog',
        url: '/seeyon/portal/portalDesigner.do?method=selectPictures&msn=' + msn,
        width: dwidth,
        height: dheight - 200,
        title: $.i18n('portal.common.imgdialog.title'),
        overflow: 'hidden',
        buttons: [{
            text: $.i18n('common.button.ok.label'),
            isEmphasize: true,
            handler: function () {
                var returnValue = selectPictureDialog.getReturnValue({
                    "innerButtonId": "ok"
                });
                if (returnValue) {
                    if (typeof saveSelectImageIdFunction == "function") {
                        selectPictureDialog.close();
                        saveSelectImageIdFunction(returnValue);
                    } else {
                        selectPictureDialog.close();
                        $.alert($.i18n('portal.tips.checkCallbackMethod'));
                    }
                    selectPictureDialog = null;
                }
            }
        }, {
            text: $.i18n('common.button.cancel.label'),
            isEmphasize: false,
            handler: function () {
                selectPictureDialog.close();
                selectPictureDialog = null;
            }
        }]
    });
}

/**
 * 选择图标库
 * oprate chooseOne  单个图标选择标识
 * styleId  图标形状标识  0 或者1   0代表线型  1 代表面型
 */
var _makeDialogiconLibrary;

function iconLibrary(oprate, styleId, saveSelectIconFunction) {
    var url = _ctxPath + "/portal/portalDesigner.do?method=iconLibrary&oprate=" + oprate + "&styleId=" + styleId;
    _makeDialogiconLibrary = $.dialog({
        url: url,
        id: 'moveToDialog',
        htmlId: 'dialog',
        targetWindow: getA8Top(),
        width: 1500,
        height: 520,
        title: '',
        overflow: 'hidden',
        buttons: [{
            id: 'ok',
            isEmphasize: true,
            text: $.i18n('common.button.ok.label'),
            handler: function () {
                if (styleId == 0) {
                    $("#plane").hide();
                    if (!$("#line").hasClass("choose")) {
                        $("#line").addClass("choose");
                    }
                } else {
                    $("#line").hide();
                    if (!$("#plane").hasClass("choose")) {
                        $("#plane").addClass("choose");
                    }
                }
                var txt = _makeDialogiconLibrary.getReturnValue();
                if (typeof saveSelectIconFunction == "function") {
                    if (txt == 'noChoose') {
                        $.alert($.i18n('portal.common.icon.alert1'));
                    } else {
                        _makeDialogiconLibrary.close();
                        _makeDialogiconLibrary = null;
                        saveSelectIconFunction(txt);
                    }
                } else {
                    _makeDialogiconLibrary.close();
                    _makeDialogiconLibrary = null;
                    $.alert($.i18n('portal.tips.checkCallbackMethod'));
                }
            }
        }, {
            id: 'cancel',
            text: $.i18n('common.button.cancel.label'),
            handler: function () {
                _makeDialogiconLibrary.close();
                _makeDialogiconLibrary = null;
            }
        }]
    });
}

var imageLibDialog;

/**
 * 个人上传图片 + 图片库选择组建
 * @augments {_callBack}
 * @augments {_fileId, _filePath} 传入一个即可
 * @return {fileId, filePath} [返回数组]
 */
function imageLibUploadDialog(_callBack, _fileId, _filePath) {
    imageLibDialog = $.dialog({
        targetWindow: getA8Top(),
        id: 'imageLibDialog',
        url: '/seeyon/ctp/common/imageIconUpload.do?method=imageUpload&fileId=' + (_fileId || '') + "&filePath=" + encodeURIComponent(_filePath || ''),
        width: 650,
        height: 320,
        title: $.i18n('portal.common.imgdialog.title'),
        overflow: 'hidden',
        buttons: [{
            text: $.i18n('common.button.ok.label'),
            isEmphasize: true,
            handler: function () {
                var returnValue = imageLibDialog.getReturnValue();
                if (returnValue && returnValue.length > 0) {
                    if (typeof _callBack == "function") {
                        imageLibDialog.close();
                        _callBack(returnValue);
                    } else {
                        imageLibDialog.close();
                    }
                    imageLibDialog = null;
                }
            }
        }, {
            text: $.i18n('common.button.cancel.label'),
            isEmphasize: false,
            handler: function () {
                imageLibDialog.close();
                imageLibDialog = null;
            }
        }]
    });
}

var iconLibDialog;

/**
 * 个人上传图片 + 图标库选择组建
 * @augments {_callBack, iconType 图标类型}
 * @return  returnValue[0] : 1 图标类型，0 图片类型;
 *          returnValue[1] : 图标className 、 图片id
 */
function iconLibUploadDialog(_callBack, iconType) {
    if (!iconType) {
        iconType = "plane";
    }
    iconLibDialog = $.dialog({
        targetWindow: getA8Top(),
        id: 'iconLibDialog',
        url: '/seeyon/ctp/common/imageIconUpload.do?method=iconUpload&iconType=' + iconType,
        width: 430,
        height: 220,
        title: $.i18n('portal.common.icondialog.title'),
        overflow: 'hidden',
        buttons: [{
            text: $.i18n('common.button.ok.label'),
            isEmphasize: true,
            handler: function () {
                var returnValue = iconLibDialog.getReturnValue();
                if (returnValue && returnValue.length == 2) {
                    if (typeof _callBack == "function") {
                        iconLibDialog.close();
                        _callBack(returnValue);
                    } else {
                        iconLibDialog.close();
                    }
                    iconLibDialog = null;
                }
            }
        }, {
            text: $.i18n('common.button.cancel.label'),
            isEmphasize: false,
            handler: function () {
                iconLibDialog.close();
                iconLibDialog = null;
            }
        }]
    });
}

/**
 * 这个方法在文件formSectionShow.js.jsp中 getCtpTop().refreshMenus();调用，为了不报错，先写个空方法，后续完善
 */
function refreshMenus() {
    return;
}

/**
 * 密码超期/不符合强度提醒
 * @returns
 */
var isforce = false;

function checkPwdIsExpired() {
    var pwdMsg = null;
    //提示密码过期
    //&& !v3x.isIpad
    var isPwdExpirationInfoNotEmpty = vPortal.isPwdExpirationInfoNotEmpty;
    var login_validatePwdStrength = vPortal.validateLoginPwdStrength;
    var isPwdExpirationInfo1Empty = vPortal.isPwdExpirationInfo1Empty
    var isCurrentUserSystemAdmin = vPortal.systemAdmin;
    var isCurrentUserAuditAdmin = vPortal.auditAdmin;
    var isCurrentUserGroupAdmin = vPortal.groupAdmin;
    var isCurrentUserAdministrator = vPortal.administrator;
    var isCurrentUserAdmin = vPortal.admin;
    var pwdmodify_force_enable = vPortal.pwdModifyForceEnable;
    var isNotPersonModifyPwd = !vPortal.modifyPwdEnable;
    var checkPwd = vPortal.checkPwd;
    var datePwd = vPortal.datePwd;

    if (isNotPersonModifyPwd && checkPwd && (isPwdExpirationInfoNotEmpty || !login_validatePwdStrength)) {
        var msg = $.i18n("message.pwd.expired");
        //只是密码强度不符合要求
        if (!isPwdExpirationInfoNotEmpty && !login_validatePwdStrength) {
            msg = $.i18n("manager.pwdStrength.require");
        }

        var msgInfo = "";
        if (isPwdExpirationInfo1Empty || !login_validatePwdStrength) {
            msgInfo = "<div class='msgbox_img_2' style='float:left'></div><div class='margin_t_5 margin_l_5' style='float:left'>" + msg + "</div>";
        } else {
            msgInfo = "<div class='msgbox_img_2' style='float:left'></div><div style='margin-left:30px;margin-top:5px;'>" + msg + "<br>" + $.i18n('message.pwd.expiredappend', datePwd) + "</div>";
        }
        if (pwdmodify_force_enable == "enable") {
            isforce = true;
            pwdMsg = $.messageBox({
                'id': "pwdMessageBox",
                'type': 100,
                'msg': msgInfo,
                'title': $.i18n("system.prompt.js"),
                close_fn: function () {
                    _pwdModify(msg);
                    keydownLisner();
                },
                buttons: [{
                    id: 'btn1',
                    text: $.i18n("message.pwd.ok"),
                    isEmphasize: true,
                    handler: function () {
                        _pwdModify(msg);
                        pwdMsg.close();
                        keydownLisner();
                    }
                }]
            });
        } else {
            pwdMsg = $.messageBox({
                'id': "pwdMessageBox",
                'type': 100,
                'msg': msgInfo,
                'title': $.i18n("system.prompt.js"),
                buttons: [{
                    id: 'btn1',
                    text: $.i18n("message.pwd.ok"),
                    isEmphasize: true,
                    handler: function () {
                        _pwdModify(msg);
                        pwdMsg.close();
                    }
                }, {
                    id: 'btn2',
                    text: $.i18n("message.pwd.cancle"),
                    handler: function () {
                        pwdMsg.close();
                    }
                }]
            });
        }
    }
    if (isforce) {
        keydownLisner();
    }
}

function keydownLisner() {
    $(document).off().on("keydown", function (event) {
        var key = event.keyCode;
        if (key == 27 || key == 116) {
            // 禁用esc键,禁用F5刷新
            return false;
        }
    })
}

function cancelKeydownLisner() {
    $(document).off().on("keydown", function (event) {
    })
}

var pwdModify;

function _pwdModify(msg) {
    var tempUrl = _ctxPath + "/individualManager.do?method=managerFrame&forcemodify=true";
    if (!isforce) {
        pwdModify = getA8Top().$.dialog({
            beforeSend: CsrfGuard.beforeAjaxSend,
            title: msg,
            transParams: {
                'parentWin': window
            },
            url: tempUrl + getA8Top().CsrfGuard.getUrlSurffix(),
            width: 500,
            height: 290,
            isDrag: false,
            isClose: true,
            closeParam: {
                'show': true,
                autoClose: true,
                handler: function () {
                    pwdModify.close();
                }
            },
            buttons: [{
                text: $.i18n("message.pwd.ok"),
                isEmphasize: true,
                handler: function () {
                    var rv = pwdModify.getReturnValue();
                    if (rv == "true") {
                        $.infor($.i18n("vportal.password.modify.success"));
                        pwdModify.close();
                    }
                }
            }, {
                text: $.i18n("message.pwd.cancle"),
                handler: function () {
                    pwdModify.close();
                }
            }]
        });
    } else {
        pwdModify = getA8Top().$.dialog({
            beforeSend: CsrfGuard.beforeAjaxSend,
            title: msg,
            transParams: {
                'parentWin': window
            },
            url: tempUrl + getA8Top().CsrfGuard.getUrlSurffix(),
            width: 500,
            height: 290,
            isDrag: false,
            isClose: false,
            closeParam: {
                'show': false,
                autoClose: false,
                handler: function () {
                    pwdModify.close();
                }
            },
            buttons: [{
                text: $.i18n("message.pwd.ok"),
                isEmphasize: true,
                handler: function () {
                    var rv = pwdModify.getReturnValue();
                    if (rv == "true") {
                        $.infor($.i18n("vportal.password.modify.success"));
                        pwdModify.close();
                        cancelKeydownLisner();
                    }
                }
            }]
        });
    }

}

var xmlDoc = null;
if (vPortal.systemAdmin != "true") {
    getDom();
}

function getDom() {
    if (xmlDoc == null) {
        try {
            xmlDoc = new ActiveXObject("SeeyonFileDownloadLib.SeeyonFileDownload");
            var userId = vPortal.CurrentUser.id;
            //du xue feng 调整增加route参数nginx集群用
            if (route != "") {
                userId = vPortal.CurrentUser.id + '@' + route;
            }
            xmlDoc.AddUserParam(vPortal.currentUserLocale, vPortal.currentUserLoginName, sessionId, userId);
        } catch (ex1) {
            /**
             * TODO:暂时屏蔽控件加载异常
             */
            //alert("批量下载控件加载错误 : " + ex1.message);
        }
    }
    return xmlDoc;
}

function escapeSpecialChar(str, cannotConvertltgt) {
    if (!str) {
        return str;
    }
    if (cannotConvertltgt == true) {
        str = str.replace(/\&/g, "&amp;").replace(/\"/g, "&quot;");
    } else {
        str = str.replace(/\&/g, "&amp;").replace(/\</g, "&lt;").replace(/\>/g, "&gt;").replace(/\"/g, "&quot;");
    }
    str = str.replace(/\'/g, "&#039;").replace(/"/g, "&#034;");
    return str;
}

function joinUrl(url) {
    if (url && url.indexOf(_ctxPath + '/') < 0) { // /seeyonReport indexof的时候也能发现ctxPath所以加个'/'
        return _ctxPath + url;
    } else {
        return url;
    }
}

//设计器栏目间距设置
var sectionSpacingChange = function (_value) {
    //把这个值缓存到父级，后面如果选了栏目外框之类需要刷新iframe的操作，渲染栏目的时候要取这个变量
    parent.vPortalSectionSpacing = _value;
    vPortal.sectionSpacing = _value;
    //清空fragment和sectionPanel
    var _fragmentList = document.querySelectorAll(".fragment");
    for (var i = 0; i < _fragmentList.length; i++) {
        var _colDom = _fragmentList[i].parentNode;
        var _sectionPanelList = _colDom.querySelectorAll(".sectionPanel");
        for (var j = 0; j < _sectionPanelList.length; j++) {
            _colDom.removeChild(_sectionPanelList[j]);
        }
    }
    //重新渲染栏目
    renderPortlets(vPortal.currentSpaceId);
};

//从6.1copy过来就是空的
function gotoDefaultPortal() {

}

//筛选空间：找出第一个在mainIframe打开的空间并激活它，因为当第一个空间打开方式为newWindow时，不允许自动打开，mainIframe显示空白导致体验不好。
var findAndRender1stMainiframeSpace = function (_spacesObject) {
    for (var i = 0; i < _spacesObject.length; i++) {
        if (_spacesObject[i].openType === "mainfrm") {
            var currentSpaceLi = document.getElementById("spaceLi_" + _spacesObject[i].id);
            if (vPortalMainFrameElements.topCenterNav && currentSpaceLi) {
                vPortalMainFrameElements.topCenterNav.showNavigation(i, currentSpaceLi);
            }
            if (vPortalMainFrameElements.miniNav && currentSpaceLi) {
                vPortalMainFrameElements.miniNav.showNavigation(i, currentSpaceLi);
            }
            if (vPortalMainFrameElements.topNav && currentSpaceLi) {
                vPortalMainFrameElements.topNav.showNavigation(i, currentSpaceLi);
            }
            if (vPortalMainFrameElements.leftNav && currentSpaceLi) {
                vPortalMainFrameElements.leftNav.showNavigation(i, currentSpaceLi);
            }
            break;
        }
    }
}

/*监听框架、栏目、某类栏目的加载，当加载完成后的执行自定义事件
-------------------------------------------------------------
调用方式：
框架：vPortalLoadEvent.mainFrame(_callBackFun);
空间：vPortalLoadEvent.space(_callBackFun);
某类栏目：vPortalLoadEvent.sections(_sectionBeanId, _callBackFun);  注意：多页签栏目组合，只检测第一个页签下的栏目，因为空间的渲染机制也是多页签渲染载第一个，后面的需要手动点击都会去渲染
-------------------------------------------------------------
_callBackFun：回调函数，各业务场景自己去定义
_sectionBeanId：栏目的sectionBeanId，如待办：pendingSection 任务：taskMySection等
举个例子：vPortalLoadEvent.sections("pendingSection",function(){console.log("待办类栏目加载完成了")});
*/
var vPortalLoadEvent = {
    mainFrame: function (_callBackFun) {
        var checkMainFrame = setInterval(function (_callBackFun) {
            if (vPortal.mainFrameIsLoad && typeof (_callBackFun) === "function") { //vPortalLoadState.mainFrameIsLoad是实时变化的，当所有框架元素加载完成后它会变为true
                _callBackFun();
                window.clearInterval(checkMainFrame);
            }
        }, 100, _callBackFun);
    },
    space: function (_callBackFun) {
        var checkSpace = setInterval(function (_callBackFun) {
            if (vPortal.currentSpaceIsLoad && typeof (_callBackFun) === "function") { //vPortalLoadState.spaceIsLoad是实时变化的，当本空间下所有栏目加载完成后它会变为true
                _callBackFun();
                window.clearInterval(checkSpace);
            }
        }, 100, _callBackFun);
    },
    sections: function (_sectionBeanId, _callBackFun) {
        var _sectionBeanId = _sectionBeanId;
        if (typeof (vPortal.allSectionPanels) === "undefined") {
            return;
        }
        //先清理出该类型下的栏目
        var tempPanel = [];
        for (var key in vPortal.allSectionPanels) {
            if (vPortal.allSectionPanels[key].sectionBeanId === _sectionBeanId) {
                tempPanel.push({
                    "panelId": key,
                    "isload": vPortal.allSectionPanels[key].isLoad
                });
            }
        }
        var allCurrentSectionBeanIdisLoad = false;
        var checkSections = setInterval(function () {
            //notStarted:初始状态，栏目是否需要加载还未知，isStart：栏目需要加载但未加载完，isEnd：栏目已加载完
            for (var i = 0; i < tempPanel.length; i++) {
                if (tempPanel[i].isload === "notStarted" || tempPanel[i].isload === "isEnd") {
                    allCurrentSectionBeanIdisLoad = true;
                } else {
                    allCurrentSectionBeanIdisLoad = false;
                }
            }
            if (allCurrentSectionBeanIdisLoad && typeof (_callBackFun) === "function") {
                _callBackFun();
                window.clearInterval(checkSections);
            }
        }, 100, _callBackFun);
    }
};

//掉线处理
var offlineFun = function () {
    alert($.i18n("loginUserState.unknown"));
    var _openerWin = window.opener;
    if (_openerWin !== null && _openerWin !== undefined) { //通过openCtpWindow或window.open打开的页面，关闭它，并对window.opener作登出操作
        if (typeof (_openerWin.getCtpTop) === "function") {
            var _openerWinCtpTop = _openerWin.getCtpTop();
            if (_openerWinCtpTop && _openerWinCtpTop["removeOnbeforeunload"] != null) {
                _openerWinCtpTop["removeOnbeforeunload"]();
            }
            _openerWin.location.href = encodeURI('/seeyon/main.do?method=logout');
            window.close();
        }
    } else { //不是通过openCtpWindow或window.open打开的页面，作登出操作
        if (typeof (getCtpTop) === "function") {
            var _ctpTop = getCtpTop();
            if (_ctpTop["removeOnbeforeunload"] != null) {
                _ctpTop["removeOnbeforeunload"]();
            }
            getCtpTop().location.href = encodeURI('/seeyon/main.do?method=logout');
        }
    }
};

/**
 * 发送短信窗口
 */
var sendSMSV3X = function (receiverIds) {
    var linkURL = getBaseURL() + "/message.do?method=showSendSMSDlg";
    if (receiverIds) {
        linkURL += "&receiverIds=" + receiverIds;
    }
    if (typeof (getA8Top().isCtpTop) !== "undefined" && getA8Top().isCtpTop) {
        getA8Top().senSmsWin = getA8Top().$.dialog({
            title: $.i18n('top.alt.sendMobileMsg'),
            transParams: {
                'parentWin': window
            },
            url: linkURL,
            width: 420,
            height: 280,
            isDrag: false
        });
    } else {
        getA8Top().senSmsWin = v3x.openDialog({
            title: $.i18n('top.alt.sendMobileMsg'),
            transParams: {
                'parentWin': window
            },
            url: linkURL,
            width: 420,
            height: 280,
            isDrag: false
        });
    }
};

//ie8不支持rgba，直接截取为rgb
var rgba2rgb4ie8 = function (value) {
    if (SeeUtils.isIE8 && value.indexOf("rgba") === 0) {
        var _temp = value.split("rgba")[1].split(",");
        var _rgb = "rgb" + _temp[0] + "," + _temp[1] + "," + _temp[2] + ")";
        return _rgb;
    } else {
        return value;
    }
}
