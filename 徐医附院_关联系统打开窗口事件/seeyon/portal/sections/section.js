"use strict";
/**
 * 栏目，包含栏目的加载和渲染等功能
 * @author huy
 * @date 2017-09-06
 */

// 全局变量
//所有sectionBeanId的模板js文件
vPortal.sectionBeanIdJsCache = {};
//所有栏目的模板
vPortal.sectionTplCache = {};
//所有栏目的模板js文件
vPortal.sectionTplJsCache = {};
//缓存所有定时器,dom移出之前先清除定时器，避免内存泄漏
vPortal.spaceInterval = {};
//所有栏目的内间距，栏目高宽需要减去它们
vPortal.sectionPaddingTop = 10;
vPortal.sectionPaddingRight = 15;
vPortal.sectionPaddingBottom = 10;
vPortal.sectionPaddingLeft = 15;
//缓存点击的url,避免重复点击
vPortal.urlCache = {};
//判断是否IE8，目前有很多地方需要对IE8兼容处理
vPortal.isIE8 = (navigator.userAgent.indexOf("MSIE") >= 0 || navigator.userAgent.indexOf("Trident") >= 0) && /msie 8\.0/i.test(navigator.userAgent) || (navigator.userAgent.indexOf("MSIE") >= 0 || navigator.userAgent.indexOf("Trident") >= 0) && /msie 7\.0/i.test(navigator.userAgent);

//旧栏目设置中需要的变量，修改代价大，暂行挪过来
var spaceType = "";
var trueSpaceType = "";
var spaceId = "";
var pagePath = "";
var spaceName = "";
var ownerId = "";
var isAllowdefined = "";
var hasPreLoginSectionShowed = false;

/*
 * Portal：门户
 * Portlet：门户组件
 * Portlets：Portlet的复数
 * 1个Portlets含有n个Portlet（也可叫section）
 * 1个Portlet含有n个Panel，每个Portlet对应坐标系中的每个Y
 */

//渲染所有的Portlets
var renderPortlets = function (_currentSpaceId) {
    vPortal.pageLoad = true;
    vPortal.currentSpaceIsLoad = false;
    _currentSpaceId == undefined && console.error("_spaceId is undefined");
    //所有栏目页签的摘要，每次刷新空间需要初始化
    vPortal.allSectionPanels = {};
    //spaceType、trueSpaceType、spaceId、pagePath、spaceName、ownerId等空间有关的变量
    var _currentSpaceSummary = vPortal.spacesSummary[_currentSpaceId];
    spaceType = _currentSpaceSummary.spaceType;
    trueSpaceType = _currentSpaceSummary.trueSpaceType;
    spaceId = _currentSpaceSummary.spaceId;
    pagePath = _currentSpaceSummary.pagePath;
    spaceName = _currentSpaceSummary.spaceName;
    ownerId = _currentSpaceSummary.ownerId;
    isAllowdefined = _currentSpaceSummary.isAllowdefined;
    vPortal.currentSpaceId = _currentSpaceId;
    var _container = document.getElementById("main").querySelector(".container");

    //更新空间的栏目间距
    if (parent.vPortalSectionSpacing) { //设计器里面，设置了该属性，还未点保存的时候，取这个值
        var _sectionSpacing = vPortal.sectionSpacing = parent.vPortalSectionSpacing;
    } else {
        var _sectionSpacing = vPortal.sectionSpacing ? vPortal.sectionSpacing : (getCtpTop && getCtpTop().vPortal && getCtpTop().vPortal.sectionSpacing);
    }
    if (_container) {
        _container.setAttribute("class", "container columnSpacing" + _sectionSpacing);
    }
    if (document.getElementById("main").querySelector(".container-fluid")) {
        document.getElementById("main").querySelector(".container-fluid").setAttribute("class", "container-fluid columnSpacing" + _sectionSpacing);
    }
    _container = null;
    //渲染空间的布局和外框架
    var _rendePortletFrame = new rendePortletFrame(_currentSpaceId);
    _rendePortletFrame = null;
    //渲染栏目数据
    var _renderAllSection = new renderAllSection(_currentSpaceId);
    _renderAllSection = null;
}

//根据摘要数据，将栏目外框架渲染至对应位置
var rendePortletFrame = function (_spaceId) {
    //根据栏目摘要数据，按X、Y坐标为栏目创建占位DOM
    //网页式的滚动条不在main区域，这里就不设置了，设置了之后会导致data-section-width比实际偏小
    if (!document.querySelector(".webStyle")) {
        document.getElementById("main").style.overflowY = "scroll";
    }
    //根据空间ID，获取栏目摘要
    var currentSpaceSummary = vPortal.spacesSummary[_spaceId];
    //根据Y坐标，遍历Portlet
    for (var y in currentSpaceSummary.portlets) {
        //获取当前Portlet的fragment
        var _fragmentDOM = document.getElementById("fragment_" + y + "_0");
        //防护一下垃圾数据
        if (_fragmentDOM == undefined) {
            continue;
        }
        var _sectionWidth = Math.floor(_fragmentDOM.offsetWidth) - 3;//原本只需要-2,2代表左右边框，-3是因为新版本谷歌会向上取整
        //根据X坐标，遍历section
        for (var x in currentSpaceSummary.portlets[y]) {
            //当前的section
            var _currentSection = currentSpaceSummary.portlets[y][x];
            if (typeof (_currentSection.sections) == "undefined" || !_currentSection.sections) {
                continue;
            }
            var _fragmentDOM = document.getElementById("fragment_" + y + "_0");
            //创建div
            var _sectionTempDiv = document.createElement("div");
            _sectionTempDiv.className = "sectionPanel";
            _sectionTempDiv.id = "section_" + _currentSection.id;
            _sectionTempDiv.setAttribute("data-section-width", _sectionWidth);
            _fragmentDOM.parentNode.appendChild(_sectionTempDiv);
            //渲染：根据id，渲染对应的栏目外框架（含标题区、内容区，不含数据）至它的占位div中
            var sectionTpl = document.getElementById("tpl-sectionMainFrame").innerHTML;
            //调用模板引擎渲染栏目外框架
            //如果sections<1时，将sbt强制改为0，因为有时多页签栏目删除页签后，后台未进行sbt的更新，这里防护一下
            if (_currentSection.sections.length == 1) {
                _currentSection.sbt = "0";
            }
            //将_sectionWidth传入栏目摘要，组件栏目为个性图标时，需要用它
            _currentSection._sectionWidth = _sectionWidth;
            renderTpl(_currentSection, sectionTpl, "section_" + _currentSection.id);
            //渲染栏目的页签
            var _tabStyle = _currentSection.style;
            var _tabLen = _currentSection.sections.length;
            if (_currentSection.sst === "1" && _tabStyle !== undefined && (((_tabStyle === "default" || _tabStyle === "standard") && _tabLen > 0) || ((_tabStyle === "card1" || _tabStyle === "card2") && _tabLen === 1))) {
                //标准页签(standard)，card1或card2且是单页签时，均使用标准页签的展现方式
                renderSectionTabs(_currentSection);
            } else if (_currentSection.sst == "1" && _currentSection.style == "card2" && _currentSection.sections.length > 0) {
                renderSectionTabs_card2(_currentSection);
            }
            //AI智能排序按钮是否显示
            if (_currentSection.sections.length > 0) {
                checkAndSetAiSort(_currentSection.id, _currentSection.sections[0].id, _currentSection.sections[0].aiSort, _currentSection.sections[0].aiSortValue);
            }
        }
    }
    //入口式这类强制不出纵向滚动条的除外，其它的main区滚动条都设置为auto
    if (document.querySelector(".wideContainer")) {
        document.getElementById("main").style.overflowY = "hidden";
    } else {
        document.getElementById("main").style.overflowY = "auto";
    }
}

var totalSectionNum = 0; //空间默认渲染的栏目总数
var loadSectionNum = 0; //已经加载的栏目数量
//渲染所有栏目的数据
var renderAllSection = function (_spaceId) {
    //刷新空间的时候重置变量
    totalSectionNum = 0;
    loadSectionNum = 0;

    //默认0个栏目的标识为true，进入for循环则改为false，证明数组非空
    var noPortlets = true;
    var containLoginSection = false;
    //根据空间ID，获取栏目摘要
    var currentSpaceSummary = vPortal.spacesSummary[_spaceId];
    //根据Y坐标，遍历Portlet，请求每个栏目并渲染它
    for (var y in currentSpaceSummary.portlets) {
        //根据X坐标，遍历section
        for (var x in currentSpaceSummary.portlets[y]) {
            //第一次进入，标识一下，栏目非空
            if (noPortlets) noPortlets = false;
            //当前的section
            var _currentSectionSummary_X = currentSpaceSummary.portlets[y][x];
            if (typeof (_currentSectionSummary_X.sections) == "undefined" || !_currentSectionSummary_X.sections || _currentSectionSummary_X.sections.length == 0) {
                continue;
            }
            var currentMySections = _currentSectionSummary_X.sections[0].sections;
            if (currentMySections.indexOf("loginSection") >= 0) {
                containLoginSection = true;
            }
            var _Panel0 = _currentSectionSummary_X.sections[0];
            var _ordinal = _Panel0.ordinal;
            var r_ordinal = _Panel0.r_ordinal;
            var fadd = _Panel0.fadd;
            //获取当前Portlet的fragment
            var _fragmentDOM = document.getElementById("fragment_" + y + "_0");
            //防护一下垃圾数据
            if (_fragmentDOM == undefined || _currentSectionSummary_X.sections.length < 1) {
                continue;
            }
            var _sWidth = _fragmentDOM.getAttribute("swidth");
            var _sectionBeanId = _currentSectionSummary_X.sections[0].sbd;
            var _entityId = _currentSectionSummary_X.id;

            var _sectionPanelDOM = document.getElementById("section_" + _entityId);
            var _sectionPanelDOMwidth = _sectionPanelDOM.getAttribute("data-section-width");

            //定义参数
            //行高
            var _paramKeys = ["lineHeight", "aiSort", "aiSortValue", "setAiSort"];
            var _paramValues = [vPortal.sectionBodyLineHeight.toString(), _Panel0.aiSort, _Panel0.aiSortValue, "0"];
            //其它参数
            var _parameter = {
                //section的X坐标
                "x": _currentSectionSummary_X.x,
                //section的Y坐标
                "y": y,
                //section的X序号，和X坐标不同的是，序号是连续递增的，x有可能因无权限等原因不会连续递增
                "xIndex": x,
                //当前section下第一个标题页签的sectionBeanId
                "sectionBeanId": _sectionBeanId,
                //当前section的ID
                "entityId": _entityId,
                //是多标题页签中的第几个，因为此轮只渲染标题页签中的第一个栏目，所以值为"0"
                "ordinal": _ordinal,
                "r_ordinal": r_ordinal,
                "fadd": fadd,
                //空间id
                "spaceId": _spaceId,
                //空间类型
                "spaceType": currentSpaceSummary.spaceType,
                //swidth
                "width": _sWidth,
                "ownerId": currentSpaceSummary.ownerId,
                //项目空间需要的sprint
                "sprint": vPortal.sprint,
                //把宽度传到栏目内容里面去
                "sectionWidth": _sectionPanelDOMwidth,
                //把是否显示大标题传到栏目内容里面去
                "sbt": _currentSectionSummary_X.sbt,
                //把是否显示小标题传到栏目内容里面去
                "sst": _currentSectionSummary_X.sst,
                //把大标题传到栏目内容里面去
                "b_t": _currentSectionSummary_X.b_t,
                //把大标题样式传到栏目内容里面去
                "b_s": _currentSectionSummary_X.style,
                //因为每次登录首页时，后台会查一次lineHeight并输出到前端，在这里传给后台，就不需要后台每次渲染栏目时再去查一次lineHeight
                "paramKeys": _paramKeys,
                "paramValues": _paramValues,
                //传入 panelId，供后续使用
                "panelId": _Panel0.id,
                //传入 bgc，供后续使用
                "bgc": _Panel0.bgc,
                //传入 bodyHeight，供后续使用
                "bodyHeight": _Panel0.height,
                //传入 模板类型，供后续使用
                "rf": _Panel0.rf,
                //传入 aiSort，渲染栏目按钮里需要
                "aiSort": _Panel0.aiSort,
                //传入 aiSortValue，渲染栏目按钮里需要
                "aiSortValue": _Panel0.aiSortValue
            }

            var _extParameter = {
                //传入 from
                "from": "renderSpace",
                "total": _Panel0.t
            }

            /*--搭个车：组装并缓存vPortal.allSectionPanels，供后续其它功能使用，每个key对应每个页签，vPortal.allSectionPanels与此轮渲染无关--*/
            cacheAllSectionPanels(_parameter);
            //通过空间摘要数据，设置栏目背景色和栏目内容区高度，数据来源为第一个页签
            setSectionBackgroundColor(_entityId, _Panel0.bgc);
            //显示的栏目才渲染
            if (_sectionPanelDOM.style && _sectionPanelDOM.style.display !== "none") {
                // 某些sectionBeanId有自己的JS需要在请求数据前执行，如天气栏目等，do it
                getInitFunction(_parameter, _extParameter);
            }
        }
        //根据Y坐标,请求当前Portlet下的所有标题页签的总条数，并渲染它，功能等完善
    }
    //没有栏目的时候，直接计算内容区的最小高度  让底部区可以在最下面显示
    if (noPortlets) {
        setMainMinHeight();
    }
    if (!containLoginSection && preLoginBtn) {
        preLoginBtn.show();
        if (document.getElementById("loginPreBtnIframe") && document.getElementById("loginPreBtnIframe").contentWindow && document.getElementById("loginPreBtnIframe").contentWindow.location) {
            document.getElementById("loginPreBtnIframe").contentWindow.location.reload(true);
        }
    }
}

//缓存AllSectionPanels，每个sectionPanels即一个页签
var cacheAllSectionPanels = function (_parameter) {
    for (var p = 0, len = vPortal.spacesSummary[vPortal.currentSpaceId]['portlets'][Number(_parameter.y)][Number(_parameter.xIndex)].sections.length; p < len; p++) {
        cacheThisPanel(_parameter, p);
    }
}

//组装并缓存单个pannel
var cacheThisPanel = function (_parameter, _index, _extParameter) {
    //空间下的栏目摘要
    var _currentSpaceSummary = vPortal.spacesSummary[vPortal.currentSpaceId];
    //当前X坐标的摘要
    var _secionX = _currentSpaceSummary['portlets'][Number(_parameter.y)][Number(_parameter.xIndex)];
    //当前pannerl
    var _currentSectionPanel = _secionX.sections[_index];
    var _ordinal = _currentSectionPanel.ordinal;
    var fadd = _currentSectionPanel.fadd;
    var r_ordinal = _currentSectionPanel.r_ordinal;
    //开始缓存当前的pannel
    var _totalString = (_parameter.total != null) ? (_parameter.totalUnit ? "(" + _parameter.total + _parameter.totalUnit + ")" : $.i18n("section.title.total", _parameter.total)) : "";
    var _sectionPanelParameter = {
        "id": _currentSectionPanel.id,
        "nodeId": _currentSectionPanel.nodeId,
        "sectionId": _currentSectionPanel.sections,
        "sectionBeanId": _currentSectionPanel.sbd,
        "title": windowAtob(_currentSectionPanel.sl),
        "total": _currentSectionPanel.t,
        "totalString": _totalString,
        "entityId": _secionX.id,
        "ordinal": _ordinal,
        "fadd": fadd,
        "r_ordinal": r_ordinal,
        "icon": _currentSectionPanel.icon,
        "delay": _currentSectionPanel.delay,
        "isReadOnly": _currentSectionPanel.isReadOnly,
        "hasParam": _currentSectionPanel.hasParam,
        "singleBordId": _currentSectionPanel.singleBordId,
        "isNoHeader": _currentSectionPanel.isNoHeader,
        "index": _index,
        "len": _secionX.sections.length,
        "x": _parameter.x,
        "y": _parameter.y,
        "xIndex": _parameter.xIndex,
        "spaceId": _parameter.spaceId,
        "spaceType": _currentSpaceSummary.spaceType,
        "ownerId": _currentSpaceSummary.ownerId,
        "trueSpaceType": _currentSpaceSummary.trueSpaceType,
        "pagePath": _currentSpaceSummary.pagePath,
        "sWidth": _parameter.width,
        "sprint": "",
        "sectionWidth": _parameter.sectionWidth,
        "sbt": _secionX.sbt,
        "sst": _secionX.sst,
        "b_t": _secionX.b_t,
        "b_s": _secionX.style,
        "bgc": _currentSectionPanel.bgc,
        "bodyHeight": _currentSectionPanel.height,
        "rf": _currentSectionPanel.rf,
        "paramKeys": _parameter.paramKeys,
        "paramValues": _parameter.paramValues,
        "aiSort": _currentSectionPanel.aiSort,
        "aiSortValue": _currentSectionPanel.aiSortValue,
        "isLoad": "notStarted"
    }
    vPortal.allSectionPanels[_currentSectionPanel.id] = _sectionPanelParameter;
    //paramValues为长度不定的数组，如报表栏目就有许多扩展的value，需要拷贝一份并更新ai按钮的两个值，不能像上面那样直接从_currentSectionPanel中取
    updateAiSort4ParamValue(_currentSectionPanel.id, _currentSectionPanel, _parameter.paramValues);
    _sectionPanelParameter = null;
}

//更新AI智能排序按钮的值
var updateAiSort4ParamValue = function (_panelId, _sectionPanel, _paramValues) {
    var _tempArray = new Array();
    for (var i = 0; i < _paramValues.length; i++) {
        if (i === 1) {
            _tempArray[i] = _sectionPanel.aiSort;
        } else if (i === 2) {
            _tempArray[i] = _sectionPanel.aiSortValue;
        } else {
            _tempArray[i] = _paramValues[i];
        }
    }
    vPortal.allSectionPanels[_panelId].paramValues = _tempArray;

}

//通过请求回来的data更新vPortal.allSectionPanels或_parameter
var updatePanelOrParameter = function (_data, _parameter, _type) {
    if (!_data.preference) return;
    var typeObj = {
        "updatePanelCache": vPortal.allSectionPanels[_data.preference.panelId],
        "updateParameter": _parameter
    };
    if (!typeObj[_type]) {
        return;
    }
    if (_data.preference.panelId !== undefined && _data.preference.panelId !== null) {
        typeObj[_type].id = _data.preference.panelId;
    }
    if (_data.preference.sections !== undefined && _data.preference.sections !== null) {
        typeObj[_type].sectionId = _data.preference.sections;
    }
    if (_data.preference.columnsName !== undefined && _data.preference.columnsName !== null) {
        typeObj[_type].title = _data.preference.columnsName;
    }
    if (_data.preference.entityId !== undefined && _data.preference.entityId !== null) {
        typeObj[_type].entityId = _data.preference.entityId;
    }
    if (_data.preference.ordinal !== undefined && _data.preference.ordinal !== null && _data.preference.ordinal != "-1") {
        typeObj[_type].ordinal = _data.preference.ordinal;
    }
    if (_data.preference.x !== undefined && _data.preference.x !== null) {
        typeObj[_type].x = _data.preference.x;
    }
    if (_data.preference.y !== undefined && _data.preference.y !== null) {
        typeObj[_type].y = _data.preference.y;
    }
    if (_data.preference.spaceId !== undefined && _data.preference.spaceId !== null) {
        typeObj[_type].spaceId = _data.preference.spaceId;
    }
    if (_data.preference.spaceType !== undefined && _data.preference.spaceType !== null) {
        typeObj[_type].spaceType = _data.preference.spaceType;
    }
    if (_data.preference.ownerId !== undefined && _data.preference.ownerId !== null) {
        typeObj[_type].ownerId = _data.preference.ownerId;
    }
    if (_data.preference.sections_bigtitle !== undefined && _data.preference.sections_bigtitle !== null) {
        typeObj[_type].b_t = _data.preference.sections_bigtitle;
    }
    if (_data.preference.sWidth !== undefined && _data.preference.sWidth !== null) {
        typeObj[_type].sWidth = _data.preference.width;
    }
    if (_data._sectionWidth !== undefined && _data._sectionWidth !== null) {
        typeObj[_type].sectionWidth = _data._sectionWidth;
    }
    if (_data.preference.backgroundColor !== undefined && _data.preference.backgroundColor !== null) {
        typeObj[_type].bgc = _data.preference.backgroundColor;
    }
    if (_data.preference.height !== undefined && _data.preference.height !== null) {
        typeObj[_type].bodyHeight = _data.preference.height;
    }
    if (_data.preference.rf !== undefined && _data.preference.rf !== null) {
        typeObj[_type].rf = _data.preference.rf;
    }
    if (_data.preference.aiSort !== undefined && _data.preference.aiSort !== null) {
        typeObj[_type].aiSort = _data.preference.aiSort;
    }
    if (_data.preference.aiSortValue !== undefined && _data.preference.aiSortValue !== null) {
        typeObj[_type].aiSortValue = _data.preference.aiSortValue;
    }
    if (_type === "updateParameter") {
        return _parameter;
    }
}

//通过空间摘要数据，设置栏目背景色
var setSectionBackgroundColor = function (_entityId, _value) {
    var _currentSectionDom = document.getElementById("section_" + _entityId);
    var _currentBodyDom = document.getElementById("panelBody_" + _entityId);
    if (_value != null && _currentSectionDom && _currentBodyDom) {
        if (_value == "") {
            _currentSectionDom.style.backgroundColor = "";
            _currentBodyDom.style.backgroundColor = "";
        } else {
            //ie8不支持rgba，直接截取为rgb
            _currentSectionDom.style.backgroundColor = rgba2rgb4ie8(_value);
            _currentBodyDom.style.backgroundColor = rgba2rgb4ie8(_value);
        }
    }
}
//通过空间摘要数据，设置栏目内容区高度
var setSectionBodyHeight = function (_panelId, _entityId, _value) {
    var _currentSectionBodyDom = document.getElementById("panelBody_" + _entityId);
    if (!_currentSectionBodyDom) return;
    if (_value != null && _value != 0) {
        if (_currentSectionBodyDom && _currentSectionBodyDom.getAttribute("class") && _currentSectionBodyDom.getAttribute("class").indexOf("padding10") > 0) {
            //如果有内padding10，需要将高度减去20
            var b_s = vPortal.allSectionPanels[_panelId].b_s;
            if (b_s && b_s === "card1") {
                _currentSectionBodyDom.style.height = (Number(_value) - 60 - vPortal.sectionPaddingTop - vPortal.sectionPaddingBottom) + "px";
            } else if (b_s && b_s === "card2") {
                _currentSectionBodyDom.style.height = (Number(_value) - 90 - 10) + "px";
            } else {
                _currentSectionBodyDom.style.height = (Number(_value) - vPortal.sectionPaddingTop - vPortal.sectionPaddingBottom) + "px";
            }
        } else {
            //如果无内padding10，直接使用设置的高度
            _currentSectionBodyDom.style.height = _value + "px";
        }
    }
}

// 某些sectionBeanId有自己的JS需要在请求数据前执行，如天气栏目，有些栏目的init又在resolveFunction中，所以需要判断
var getInitFunction = function (_parameter, _extParameter) {
    if (vPortal.allSectionPanels[_parameter.panelId]) {
        vPortal.allSectionPanels[_parameter.panelId].isLoad = "isStart";
    }
    var _sectionBeanId = _parameter.sectionBeanId;
    var _rf = _parameter.rf;
    var _hasResolveFunctionInit = false;
    //有返回resolveFunction，多用于非第一轮渲染时
    if (_rf !== undefined && _rf !== "") {
        //有resolveFunction对应的JS事件需要执行
        if (vPortal.sectionHandler[_rf] !== undefined && vPortal.sectionHandler[_rf].init !== undefined) {
            vPortal.sectionHandler[_rf].init(_parameter, _extParameter);
            _hasResolveFunctionInit = true;
        }
    }
    //没执行过resolveFunction对应的JS事件，看看有没有sectionBeanId对应的事件
    if (!_hasResolveFunctionInit) {
        if (vPortal.sectionHandler[_sectionBeanId] !== undefined && vPortal.sectionHandler[_sectionBeanId].init !== undefined) {
            vPortal.sectionHandler[_sectionBeanId].init(_parameter, _extParameter);
        } else {
            selectRenderData(_parameter, _extParameter);
        }
    }
}

//如果栏目有假数据，调用假数据进行渲染，否则向后台发起ajax请求，获取真实的数据
var selectRenderData = function (_parameter, _extParameter) {
    var _sectionBeanId = _parameter.sectionBeanId;
    if (vPortal.isDesigner && previewVPortalSpace == false) {
        if (vPortal.sectionTestData != undefined && vPortal.sectionTestData[_sectionBeanId] != undefined) {
            var _result = vPortal.sectionTestData[_sectionBeanId]();
            var _drawColumnData = new getTplJsAndBeforeInit(_result, _parameter);
            _drawColumnData = null;
        } else {
            if (_parameter.sectionBeanId == "weatherSection") { //天气栏目
                document.getElementById("panelBody_" + _parameter.entityId).innerHTML = "<div style='height:65px;line-height:65px;text-align:center'>北京 晴天/晴天</div>";
            } else {
                document.getElementById("panelBody_" + _parameter.entityId).innerHTML = "<div style='height:" + (300 - vPortal.sectionPaddingTop - vPortal.sectionPaddingBottom) + "px;'></div>";
            }
        }
    } else {
        var _getDataAndRenderSection = new getDataAndRenderSection(_parameter, _extParameter);
    }
}

//根据ID等参数，请求1个section，并调用它的渲染函数
var getDataAndRenderSection = function (_parameter, _extParameter, retryCount) {
    //刷新页签等操作，不更新此变量
    if (_extParameter && _extParameter.from == "renderSpace") totalSectionNum++;

    if (typeof (_extParameter) != "undefined" && _extParameter.total != null && _extParameter.total != '') {
        _parameter.pageLoad = vPortal.pageLoad;
    }
    if (_parameter.r_ordinal && _parameter.r_ordinal != _parameter.ordinal) {
        _parameter.ordinal = _parameter.r_ordinal;
    }

    var data = new Object();
    data.managerMethod = "doProjection";
    data.arguments = $.toJSON(_parameter);
    var url = '/seeyon/ajax.do?method=ajaxAction&managerName=sectionManager'; //注意：为了etag，这个url不能加随机数

    if (typeof (retryCount) == "undefined") {
        retryCount = 0;
    }
    jQuery.ajax({
        type: "GET",
        url: url,
        data: data,
        dataType: "json",
        beforeSend: CsrfGuard.beforeAjaxSend,
        async: true,
        success: function (jsonObj) {
            if (retryCount < 1 && jsonObj == "__LOGOUT") { //如果是苹果浏览器遇到这种情况，重试1次
                var time = new Date().getTime();
                _parameter.time = time;
                getDataAndRenderSection(_parameter, _extParameter, retryCount);
            } else {
                //掉线了之后直接返回
                if (jsonObj == "__LOGOUT") {
                    document.getElementById("panelBody_" + _parameter.entityId).innerHTML = "<span class='colorRed'>" + $.i18n('vportal.tips.clearBrowsercache') + "</span>";
                    return;
                }
                var _result;
                if (typeof jsonObj === 'string') {
                    _result = $.parseJSON(jsonObj);
                } else {
                    _result = jsonObj;
                }

                if (JSON.stringify(_result) == "{}" || !_result) {
                    document.getElementById("panelBody_" + _parameter.entityId).innerHTML = "<span class='colorRed'>" + $.i18n("portal.section.error.label1") + "</span>";
                    return;
                }
                if (_result && _result.error !== undefined) {
                    document.getElementById("panelBody_" + _parameter.entityId).innerHTML = _result.error;
                    return;
                }
                if (_result && _result.Data != undefined) {
                    //统计渲染栏目的耗时，IE8-不支持，所以使用try
                    try {
                        //console.time("【" + _result.Name + _parameter.entityId + "】（包含beforeInit、renderTpl、afterInit）共用时");
                    } catch (e) {
                    }
                    ;
                    if (_result.Data.refreshSpaceId && _result.Data.refreshSpaceId !== "" && _result.Data.refreshSpacePath && _result.Data.refreshSpacePath !== "") {
                        //点击的是ai智能排序，且账号无个性化空间数据，后台会生成用户的个性化空间数据，前端需要刷新空间
                        updateSpacePathAndId4Cache(_result.Data.refreshSpaceId, _result.Data.refreshSpacePath);
                        refreshCurrentSpace("notFreshMenuNav");
                    } else {
                        //执行栏目的beforeInit
                        _parameter = updatePanelOrParameter(_result, _parameter, "updateParameter");
                        getTplJsAndBeforeInit(_result, _parameter, _extParameter);
                    }
                }
            }
        },
        error: function (request, settings, e) {
            console.error(e);
        }
    });
}

//获取tpl对应的js，并执行相应的beforeInit
var getTplJsAndBeforeInit = function (_result, _parameter, _extParameter) {
    var _resolveFunction = _result.Data.resolveFunction;
    if (vPortal.sectionHandler[_resolveFunction] !== undefined && vPortal.sectionHandler[_resolveFunction].beforeInit !== undefined) {
        vPortal.sectionHandler[_resolveFunction].beforeInit(_result, _parameter);
    }
    //根据模板，渲染栏目
    var _drawColumnData = new renderEachSectionData(_result, _parameter, _extParameter);
    _drawColumnData = _resolveFunction = null;
}

//准备渲染1个栏目
var renderEachSectionData = function (_data, _parameter, _extParameter) {
    //更新一下vPortal.allSectionPanel的缓存数据
    updatePanelOrParameter(_data, _parameter, "updatePanelCache");
    this._data = _data;
    //传入portlet(section)的Id 供tpl模版里面使用
    this._data._entityId = _parameter.entityId;
    //传入sectionBeanId 共tpl模版里面使用
    this._data._sectionBeanId = _parameter.sectionBeanId;
    //传入 栏目宽度 供tpl模版里面使用
    this._data._sectionWidth = _parameter.sectionWidth;
    //传入 栏目占比 供tpl模版里面使用
    this._data._swidth = _parameter.width;
    //传入 栏目是否显示大标题 供tpl模版里面使用
    this._data._sbt = _parameter.sbt;
    //传入 栏目是否显示大标题 供tpl模版里面使用
    this._data._sst = _parameter.sst;
    //传入 panelId，供后续使用
    this._data._panelId = _parameter.panelId;
    //传入 bgc
    this._data._bgc = _parameter.bgc;
    //传入 bodyHeight，供后续使用
    if (_parameter.b_s === "card1") {
        //如果标题区为标准图标(card1)，需减去它的高度60
        this._data._bodyHeight = _parameter.bodyHeight - 60;
    } else if (_parameter.b_s === "card2") {
        //如果标题区为多彩磁贴(card2)，需减去它的高度80，card2的高度为90，但当为card2时,section-body的上padding为0（正常的为10），所以这里减去80即可
        this._data._bodyHeight = _parameter.bodyHeight - 80;
    } else {
        //其它，无需要单独减去什么
        this._data._bodyHeight = _parameter.bodyHeight;
    }
    //传入 aiSort，供后续使用
    this._data._aiSort = _parameter.aiSort;
    //console.log(_data);
    //console.log(_parameter);
    //通过空间摘要数据，设置栏目背景色和栏目
    setSectionBackgroundColor(_parameter.entityId, _parameter.bgc);

    //通过空间摘要数据，设置栏目内容区高度   这里的判断条件和changeTabAndReloadSection基本一致
    //防护一下，有的栏目模板没有定义相关的js，避免报错
    if (typeof (vPortal.sectionHandler[_parameter.rf]) != "undefined") {
        //如果在栏目模板里面定义了autoHeight=true则不设置
        if (typeof (vPortal.sectionHandler[_parameter.rf].autoHeight) == "undefined" || (typeof (vPortal.sectionHandler[_parameter.rf].autoHeight) != "undefined" && vPortal.sectionHandler[_parameter.rf].autoHeight === false)) {
            setSectionBodyHeight(_parameter.panelId, _parameter.entityId, _parameter.bodyHeight);
        }
    }

    //获取模板并渲染
    this.getTplAndRenderTpl(_data, _parameter, _extParameter);
    var aiSortValue = _parameter.aiSortValue;
    if (_data.preference) {
        aiSortValue = _data.preference.aiSortValue
    }
    checkAndSetAiSort(_parameter.entityId, _parameter.panelId, _parameter.aiSort, aiSortValue);
};

//查找栏目的tpl模板，如果缓存中没有，就通过ajax向后台请求，并调用渲染的方法
renderEachSectionData.prototype.getTplAndRenderTpl = function (_data, _parameter, _extParameter) {
    var _this = this;
    //获取tpl模板名称
    var _resolveFunction = _data.Data.resolveFunction;
    if (_data.Data.customizeResolveFunction) {
        _resolveFunction = _data.Data.customizeResolveFunction;
    }
    if (_resolveFunction == undefined) {
        console.error($.i18n("portal.section.error.label2"));
        return;
    }
    //获取被渲染的区域：内容区
    var _drawArea = "panelBody_" + _parameter.entityId;
    var _drawAreaObj = document.getElementById(_drawArea);

    //因为此方法异步了，假如空间1的栏目还未加载完毕，点击空间2的时候，空间1的栏目DOM被干掉了，此时就会有问题
    if (_drawAreaObj == null) return;

    //缓存中是否在本tpl模板，如果有，直接调用，否则就通过ajax请求tpl模板
    if (vPortal.sectionTplCache[_resolveFunction] != undefined) {
        var _currentSectionTpl = vPortal.sectionTplCache[_resolveFunction];
        this.renderTpl(_currentSectionTpl, _drawArea, _data, _parameter, _extParameter);

        loadSectionNumIncrease();
        ///执行栏目的afterInit
        getTplJsAndAfterInit(_data, _parameter);
    } else {
        var _resSuffix = typeof (vPortal.resSuffix) !== "undefined" ? vPortal.resSuffix : "";
        var _url = _ctxPath + "/portal/sections/tpl/tpl-" + _resolveFunction + ".html" + _resSuffix + "?a=1" + CsrfGuard.getUrlSurffix();
        $.ajax({
            url: _url,
            type: 'get',
            dataType: 'html',
            cache: true,
            beforeSend: CsrfGuard.beforeAjaxSend,
            success: function (result) {
                vPortal.sectionTplCache[_resolveFunction] = result;
                _this.renderTpl(result, _drawArea, _data, _parameter, _extParameter);

                loadSectionNumIncrease();
                ///执行栏目的afterInit
                getTplJsAndAfterInit(_data, _parameter);
            },
            error: function () {
                console.error("no tpl：" + _resolveFunction);
            }
        });
    }
}

/*--渲染1个栏目数据--*/
renderEachSectionData.prototype.renderTpl = function (_tpl, _drawArea, _data, _parameter, _extParameter) {
    var _this = this;
    //统计渲染栏目的耗时，IE8-不支持，所以使用try
    try {
        //console.time("【" + _this._data.Name + _this._data._entityId + "】 渲染用时");
    } catch (e) {
    }
    ;

    var _drawAreaObj = document.getElementById(_drawArea);
    //因为此方法异步了，假如空间1的栏目还未加载完毕，点击空间2的时候，空间1的栏目DOM被干掉了，此时就会有问题
    if (_drawAreaObj == null) return;

    laytpl(_tpl).render(this._data, function (_html) {
        _drawAreaObj.innerHTML = _html;
        _html = null;
        _tpl = null;
        //统计渲染栏目的耗时，IE8-不支持，所以使用try
        try {
            //console.timeEnd("【" + _this._data.Name + _this._data._entityId + "】 渲染用时");
        } catch (e) {
        }
        ;
    });
    //标题为多彩磁贴时，有个特殊的需求：section-body的padding-top为0
    if (typeof (_parameter) !== "undefined" && typeof (_parameter.b_s) !== "undefined" && _parameter.b_s == "card2") {
        _drawAreaObj.style.paddingTop = 0;
    }
    _drawAreaObj = null;
    //登录前和大屏不显示按钮
    var _hideButton = vPortal.portalType && (vPortal.portalType == "2" || vPortal.portalType == "3");
    if (this._data._sst == "1") {
        //渲染栏目的按钮
        this.renderButtons(_hideButton);
        //渲染标题（改变了标题时需要渲染标题）
        //this.renderTitle();
        //渲染总数
        if (typeof (_extParameter) != "undefined" && (_extParameter.from === "reload" || _extParameter.from === "changeTab") && this._data.Total != undefined) {
            this.renderTotalFromData();
        }
    }
    //有几个特殊的无头栏目，也有另类的bottomButtons
    if (this._data._sst == "0" && !_hideButton && vPortal.sectionHandler && vPortal.sectionHandler[this._data.Data.resolveFunction] && vPortal.sectionHandler[this._data.Data.resolveFunction].renderBottomButtons && this._data.Data.bottomButtons != null) {
        vPortal.sectionHandler[this._data.Data.resolveFunction].renderBottomButtons(this._data);
    }
    //“换一换”的功能
    this.refreshAnotherData();
    //更新一个标题
    if (document.getElementById("titleName_" + this._data._panelId) && _extParameter && _extParameter.from && _extParameter.from === "reload") {
        document.getElementById("titleName_" + this._data._panelId).innerHTML = this._data.Name;
    }
};

//渲染栏目的两类按钮：自定义功能按钮、更多
renderEachSectionData.prototype.renderButtons = function (_hideButton) {
    //“更多”按钮
    var _moreButton = this._data.Data.moreButton;
    var _entityId = this._data._entityId;
    var _moreButtonNode = document.getElementById("sectionHeaderMore" + _entityId);
    if (_hideButton) {
        _moreButtonNode.style.display = "none";
        return;
    }
    //没有“更多”按钮的栏目，同时干掉栏目框架上预留的“更多”按钮区
    if (_moreButton == null) {
        //没有“更多”按钮
        if (_moreButtonNode) {
            _moreButtonNode.parentNode.removeChild(_moreButtonNode);
        }
        //如果按钮在大标题区
        if (document.getElementById("section-bigTitle-" + _entityId) && document.getElementById("section-bigTitle-" + _entityId).querySelector(".section-header-button")) {
            document.getElementById("section-bigTitle-" + _entityId).querySelector(".section-header-button").style.right = "15px";
        }
        //如果按钮在标题区
        if (document.getElementById("section-header-" + _entityId) && document.getElementById("section-header-" + _entityId).querySelector(".section-header-button")) {
            document.getElementById("section-header-" + _entityId).querySelector(".section-header-button").style.right = "15px";
        }
    } else if (_moreButton !== null && _moreButton !== undefined) {
        //没有“更多”按钮dom，需要创建
        if (_moreButtonNode === null) {
            _moreButtonNode = document.createElement("div");
            _moreButtonNode.className = "section-header-more";
            _moreButtonNode.id = "sectionHeaderMore" + _entityId;
            //如果有大标题区，“更多”按钮就显示在大标题区，否则显示在标题区
            var _sectionBigHeaderDom = document.getElementById("section-bigTitle-" + _entityId);
            var _sectionHeaderDom = document.getElementById("section-header-" + _entityId);
            if (_sectionBigHeaderDom) {
                _sectionBigHeaderDom.appendChild(_moreButtonNode);
            } else if (_sectionHeaderDom) {
                _sectionHeaderDom.appendChild(_moreButtonNode);
            }
        }
        //链接的打开方式
        if (_moreButton.target && _moreButton.target == "href_blank") {
            var _openType = 3;
        } else {
            var _openType = 2;
        }
        //有“更多”按钮
        if (_moreButton.link && _moreButton.link.indexOf("javascript") > -1) {
            //链接中自带jascript标签
            _moreButtonNode.innerHTML = "<i onclick=\"" + _moreButton.link + "\" class=\"vportal vp-section-more\" title=\"" + _moreButton.label + "\"></i>";
        } else {
            //普通的链接
            _moreButtonNode.innerHTML = "<i onclick=\"javascript:_openDataLink({'url':'" + _moreButton.link + "','obj':this,'openType':'" + _openType + "','sectionBeanId':'" + this._data._sectionBeanId + "'});\" class=\"vportal vp-section-more\" title=\"" + escapeSpecialChar(_moreButton.label) + "\"></i>";
        }
        //如果有AI智能排序的按钮，将.section-header-button往左挪挪
        var _aiSortBtn = this._data._aiSort;
        if (_aiSortBtn != undefined && _aiSortBtn == "1") {
            //如果按钮在大标题区
            if (document.getElementById("section-bigTitle-" + _entityId) && document.getElementById("section-bigTitle-" + _entityId).querySelector(".section-header-button")) {
                document.getElementById("section-bigTitle-" + _entityId).querySelector(".section-header-button").style.right = "75px";
            }
            //如果按钮在标题区
            if (document.getElementById("section-header-" + _entityId) && document.getElementById("section-header-" + _entityId).querySelector(".section-header-button")) {
                document.getElementById("section-header-" + _entityId).querySelector(".section-header-button").style.right = "75px";
            }
        }
        //如果无AI智能排序的按钮，将.section-header-button复位
        if (_aiSortBtn != undefined && _aiSortBtn == "0") {
            //如果按钮在大标题区
            if (document.getElementById("section-bigTitle-" + _entityId) && document.getElementById("section-bigTitle-" + _entityId).querySelector(".section-header-button")) {
                document.getElementById("section-bigTitle-" + _entityId).querySelector(".section-header-button").style.right = "45px";
            }
            //如果按钮在标题区
            if (document.getElementById("section-header-" + _entityId) && document.getElementById("section-header-" + this._data._entityId).querySelector(".section-header-button")) {
                document.getElementById("section-header-" + _entityId).querySelector(".section-header-button").style.right = "45px";
            }
        }
    }

    //栏目自定义的功能的按钮
    var _bottomButtons = this._data.Data.bottomButtons;
    var _sectionButtonAllInOneDOm = document.getElementById("sectionButtonAllInOne" + this._data._entityId);
    if (typeof (_bottomButtons) == "undefined" || _bottomButtons == null) {
        if (_sectionButtonAllInOneDOm) {
            _sectionButtonAllInOneDOm.style.display = "none";
        }
        return;
    }
    ;
    if (_bottomButtons.length == 0 && _sectionButtonAllInOneDOm) {
        _sectionButtonAllInOneDOm.style.display = "none";
    } else if (_bottomButtons.length > 0 && _sectionButtonAllInOneDOm) {
        _sectionButtonAllInOneDOm.style.display = "block";
    }
    var _bottomButtonsLen = _bottomButtons.length;
    if (_bottomButtonsLen == 1) {
        if (_bottomButtons[0].link.indexOf("javascript") > -1) {
            _sectionButtonAllInOneDOm.innerHTML = "<span onclick=\"" + _bottomButtons[0].link + "\" title=\"" + escapeSpecialChar(_bottomButtons[0].label) + "\"><i class='vportal vp-section-btn-" + _bottomButtons[0].icon + "'></i></span>";
        } else if (_bottomButtons[0].handler) {

            //把object转为json并替换"为'
            var handlerStr = $.toJSON(_bottomButtons[0].handler);
            var reg = new RegExp("\"", "g");
            handlerStr = handlerStr.replace(reg, "\'");
            _sectionButtonAllInOneDOm.innerHTML = "<span onclick=\"javascript:_openDataLink({'url':'" + _bottomButtons[0].link + "','obj':this,'target':" + _bottomButtons[0].target + ",'label':'" + _bottomButtons[0].label + "','sectionBeanId':'" + this._data._sectionBeanId + "','handler': " + handlerStr + "});\" title=\"" + escapeSpecialChar(_bottomButtons[0].label) + "\"><i class='vportal vp-section-btn-" + _bottomButtons[0].icon + "'></i></span>";
        } else {
            var _openlinkType = "2";
            if (_bottomButtons[0].target == "href_blank") {
                _openlinkType = "3";
            }
            _sectionButtonAllInOneDOm.innerHTML = "<span onclick=\"javascript:_openDataLink({'url':'" + _bottomButtons[0].link + "','obj':this,'openType':'" + _openlinkType + "','sectionBeanId':'" + this._data._sectionBeanId + " '});\" title=\"" + escapeSpecialChar(_bottomButtons[0].label) + "\"><i class='vportal vp-section-btn-" + _bottomButtons[0].icon + "'></i></span>";
        }
        //如果无“编辑、移除、添加”这些按钮，功能按钮向右挪10PX
        if (isAllowdefined == "false" && _sectionButtonAllInOneDOm) {
            _sectionButtonAllInOneDOm.style.marginRight = "0px";
        }
    } else if (_bottomButtonsLen > 1) {
        var _tempStr = "";
        _tempStr += "<span><i class='vportal vp-dropdown-operation'></i></span>";
        _tempStr += "<div class='btnMoreListArea'>";
        _tempStr += "<ul class='btnMoreList'>";
        var handlerStr, reg;
        for (var i = 0; i < _bottomButtonsLen; i++) {
            var linkStr = _bottomButtons[i].link.split("%");
            //标识从表单业务配置过来的情况，需要通过onClick获得鼠标点击坐标，以显示出现的表单上报框的定位
            if (linkStr.length > 0 && linkStr[0] == 'FormBizConfig') {
                _tempStr += "<li onclick=\"" + linkStr[1] + "\" title=\"" + escapeSpecialChar(_bottomButtons[i].label) + "\"><div>" + escapeSpecialChar(_bottomButtons[i].label) + "</div></li>";
            } else if (linkStr.handler) {
                //把object转为json并替换"为'
                handlerStr = $.toJSON(linkStr.handler);
                reg = new RegExp("\"", "g");
                handlerStr = handlerStr.replace(reg, "\'");
                _tempStr += "<li onclick=\"javascript:_openDataLink({'url':'" + _bottomButtons[i].link + "','obj':this,'target':" + _bottomButtons[i].target + ",'label':'" + escapeSpecialChar(_bottomButtons[i].label) + "','sectionBeanId':'" + this._data._sectionBeanId + ",'handler': " + handlerStr + " });\" title=\"" + escapeSpecialChar(_bottomButtons[i].label) + "\"><div>" + escapeSpecialChar(_bottomButtons[i].label) + "</div></li>";
                reg = "";
                handlerStr = "";
            } else if (_bottomButtons[i].link.indexOf("javascript") > -1) {
                _tempStr += "<li onclick=\"" + _bottomButtons[i].link + "\" title=\"" + escapeSpecialChar(_bottomButtons[i].label) + "\"><div>" + escapeSpecialChar(_bottomButtons[i].label) + "</div></li>";
            } else {
                _tempStr += "<li onclick=\"javascript:_openDataLink({'url':'" + _bottomButtons[i].link + "','obj':this,'openType':'2','sectionBeanId':'" + this._data._sectionBeanId + "'});\" title=\"" + escapeSpecialChar(_bottomButtons[i].label) + "\"><div>" + escapeSpecialChar(_bottomButtons[i].label) + "</div></li>";
            }
        }
        _tempStr += "</ul>";
        _tempStr += "</div>";
        document.getElementById("sectionButtonAllInOne" + this._data._entityId).innerHTML = _tempStr;
        //如果无“编辑、移除、添加”这些按钮，功能按钮向右挪10PX
        if (isAllowdefined == "false" && _sectionButtonAllInOneDOm) {
            _sectionButtonAllInOneDOm.style.marginRight = "0px";
        }
    }
}

//渲染总数，这个总数来源于栏目数据
renderEachSectionData.prototype.renderTotalFromData = function () {
    if (this._data._panelId) {
        var _thisPanelId = this._data._panelId;
    } else {
        return;
    }
    var _totalNumber = this._data.Total != undefined && this._data.Total != null ? (Number(this._data.Total) > 999 ? "<span class='total999'>999<sup>+</sup></span>" : this._data.Total) : "";
    var _totalUnit = this._data.TotalUnit != undefined && this._data.TotalUnit != null ? this._data.TotalUnit : "";
    if (_thisPanelId && _totalNumber !== "") {
        var _currentPanelDom = document.getElementById("sectionName_" + _thisPanelId);
        var _titleStr = this._data.Name;
        if (vPortal.allSectionPanels[_thisPanelId].b_s == "default" || vPortal.allSectionPanels[_thisPanelId].b_s == "standard") {
            var _totalStr = _totalNumber + _totalUnit;
            _currentPanelDom.setAttribute("title", _titleStr + "(" + this._data.Total + _totalUnit + ")");
        } else {
            var _totalStr = "" + _totalNumber + _totalUnit + "";
            _currentPanelDom.setAttribute("title", _titleStr + "(" + this._data.Total + _totalUnit + ")");
        }
        if (document.getElementById("total_" + _thisPanelId)) {
            document.getElementById("total_" + _thisPanelId).innerHTML = _totalNumber;
        }
    }
}

//渲染标题
renderEachSectionData.prototype.renderTitle = function () {
    if (this._data._panelId) {
        var _thisPanelId = this._data._panelId;
    } else {
        return;
    }
    var _titleStr = this._data.Name;
    if (_thisPanelId && _titleStr) {
        var _currentPanelDom = document.getElementById("titleName_" + _thisPanelId);
        if (_currentPanelDom) {
            _currentPanelDom.innerHTML = escapeSpecialChar(_titleStr);
        }
    }
}

//“换一换”的功能
renderEachSectionData.prototype.refreshAnotherData = function () {
    var _sectionPanelDom = document.getElementById("section_" + this._data._entityId);
    if (this._data.Data.showChangeButton && this._data.Data.datas !== null) {
        //此页签有“换一换”的功能
        //此页签还没有“换一换”的dom，需要创建
        if (!document.getElementById("refreshAnotherData_" + this._data._entityId)) {
            var refreshAnotherDataDom = document.createElement("div");
            refreshAnotherDataDom.className = "refreshAnotherData";
            if (this._data.Data.showHeader == "0") {
                refreshAnotherDataDom.className = refreshAnotherDataDom.className + " padding_t_15";
            }
            refreshAnotherDataDom.id = "refreshAnotherData_" + this._data._entityId;
            _sectionPanelDom.appendChild(refreshAnotherDataDom);
        }
        var refreshAnotherDataStr = '<span onclick="javascript:sectionRefreshAnotherData(\'' + this._data._panelId + '\',\'' + this._data.Data.pageNo + '\')"><span class="vportal vp-change-one-change"></span> <span>' + $.i18n('section.name.flag.change') + '</span></span>';
        document.getElementById("refreshAnotherData_" + this._data._entityId).innerHTML = refreshAnotherDataStr;
        document.getElementById("refreshAnotherData_" + this._data._entityId).style.display = "block";
    } else if (!this._data.Data.showChangeButton && _sectionPanelDom.querySelector(".refreshAnotherData")) {
        //此页签无“换一换”的功能，但有“换一换”的dom，需要移除它
        _sectionPanelDom.removeChild(_sectionPanelDom.querySelector(".refreshAnotherData"));
    }
}

//获取tpl对应的js，并执行相应的afterinit
var getTplJsAndAfterInit = function (_result, _parameter) {
    var _resolveFunction = _result.Data.resolveFunction;
    if (_result.Data.customizeResolveFunction) {
        _resolveFunction = _result.Data.customizeResolveFunction;
    }
    if (vPortal.sectionHandler[_resolveFunction] !== undefined && vPortal.sectionHandler[_resolveFunction].afterInit !== undefined) {
        vPortal.sectionHandler[_resolveFunction].afterInit(_result, _parameter);
        vPortal.sectionHandler.updateLoadStateBySectionBeanId(_result._panelId);
    } else {
        vPortal.sectionHandler.updateLoadStateBySectionBeanId(_result._panelId);
    }
    updateSectionState();

    //统计渲染栏目的耗时，IE8-不支持，所以使用try
    try {
        //console.timeEnd("【" + _result.Name + _result._entityId + "】（包含beforeInit、renderTpl、afterInit）共用时");
    } catch (e) {
    }
    ;
    if (vPortal.pageLoad && preLoginBtn && !hasPreLoginSectionShowed) {
        var containLoginSection = false;
        if (_parameter.sectionBeanId == "loginSection") {
            containLoginSection = true;
        } else {
            var currentMySections = vPortal.spacesSummary[vPortal.currentSpaceId].portlets[_parameter.y][_parameter.xIndex].sections[0].sections;
            if (currentMySections.indexOf("loginSection") >= 0) {
                containLoginSection = true;
            }
        }
        if (containLoginSection) {
            var loginSectionTop = $("#panelBody_" + _parameter.entityId).offset().top;
            var loginSectionLeft = $("#panelBody_" + _parameter.entityId).offset().left;
            var windowHeight = $(window).height() - 170;
            var windowWidth = $(window).width() - 170;
            if ((windowHeight < 0 || windowWidth < 0) || (loginSectionTop >= windowHeight || loginSectionLeft > windowWidth)) {
                preLoginBtn.show();
                document.getElementById("loginPreBtnIframe").contentWindow.location.reload(true);
            } else {
                preLoginBtn.hide();
                hasPreLoginSectionShowed = true;
            }
        }
    }
}


//更新栏目的加载状态
var updateSectionState = function () {
    var topWrapper = getCtpTop().document.getElementById("wrapper");
    //栏目全部加载完毕 || 加载完毕后点击页签  &&  主题空间||项目空间  && 需要变形的页面
    if (totalSectionNum == loadSectionNum && vPortal.isThemeSpace && topWrapper) {
        var _containerHeight = document.querySelector(".container").clientHeight;
        var isFirstLoad = typeof (vPortal.spacesSummary[spaceId].containerHeight) == "undefined"; //第一次加载此主题空间
        var isTabchange = vPortal.spacesSummary[spaceId].containerHeight && vPortal.spacesSummary[spaceId].containerHeight != _containerHeight; //击页签后，高度发生变化的时候,
        var isNeedSet = topWrapper.className.indexOf("isThemeSpace") != -1 || topWrapper.className.indexOf("isProjectSpace") != -1; //顶层框架下 且是主题空间或项目空间

        if ((isFirstLoad || isTabchange) && isNeedSet) {
            vPortal.spacesSummary[spaceId].containerHeight = _containerHeight;
            doAutoIframeHeight(_containerHeight);
        }
    } else if (totalSectionNum == loadSectionNum && document.getElementById("main").style.minHeight == "") {
        vPortal.currentSpaceIsLoad = true;
        //栏目全部加载完毕 || 加载完毕后点击页签  && 没有设置过minHeight
        setMainMinHeight();
    }
    topWrapper = null;
}

//当前已加载的栏目标识
var loadSectionNumIncrease = function () {
    //页签切换的时候  不会++
    if (totalSectionNum !== loadSectionNum) loadSectionNum++;
}

/**
 * 更新iframe的高度值
 * 传入containerHeight的时候直接使用
 * 不传入containerHeight的时候，自动计算，适用一些特殊场景，在高度变化后，直接调用即可
 * 如果某个栏目在AfterInit里面有异步且会改变高度的话，可以调用一下此方法
 **/
var doAutoIframeHeight = function (containerHeight) {
    var lastHeight;
    var topWrapper = getCtpTop().document.getElementById("wrapper");
    var _isThemeSpace = topWrapper.className.indexOf("isThemeSpace") != -1; //顶层框架下 且是主题空间
    var _isProjectSpace = topWrapper.className.indexOf("isProjectSpace") != -1; //顶层框架下 且是项目空间

    //编辑栏目的时候需要计算高度，其他时候直接设置即可
    if (typeof (containerHeight) == "undefined" && (_isThemeSpace || _isProjectSpace)) {
        containerHeight = document.querySelector(".container").clientHeight;
    }
    if (_isThemeSpace) { //主题空间
        lastHeight = containerHeight + "px";
    } else if (_isProjectSpace) { //项目空间
        var ProjectSpaceTop = 85; //项目空间的栏目区距离上面有85px
        lastHeight = containerHeight + ProjectSpaceTop + "px";
    }
    getCtpTop().document.getElementById("main").style.height = lastHeight;
    getCtpTop().document.getElementById("seeyonPortalBody").style.height = "auto";
    // console.log("lastHeight:" + lastHeight);
    topWrapper = null;
}

//网页式门户模板，设置main区域的  最小高度
var setMainMinHeight = function () {
    //非网页式门户直接return
    var webStyle = getCtpTop().document.querySelector(".webStyle");
    if (webStyle == null) return;

    var seeyonPortalHeaderH, topNavH, slideVPortalH, webStyleFooterH;
    seeyonPortalHeaderH = getCtpTop().document.getElementById("seeyonPortalHeader");
    topNavH = getCtpTop().document.getElementById("topNav");
    slideVPortalH = getCtpTop().document.getElementById("slideVPortal");
    webStyleFooterH = getCtpTop().document.getElementById("webStyleFooter");
    seeyonPortalHeaderH = (seeyonPortalHeaderH != null ? seeyonPortalHeaderH.clientHeight : 0);
    topNavH = (topNavH != null ? topNavH.clientHeight : 0);
    slideVPortalH = (slideVPortalH != null ? slideVPortalH.clientHeight : 0);
    webStyleFooterH = (webStyleFooterH != null ? webStyleFooterH.clientHeight : 0);
    //元素的高度总和
    var elementsH = seeyonPortalHeaderH + topNavH + slideVPortalH + webStyleFooterH;

    //每次调用之前，先把minHeight干掉，避免#main的clientHeight计算错误
    getCtpTop().document.getElementById("main").style.minHeight = "";
    //栏目区的总和
    var mainH = getCtpTop().document.getElementById("main").clientHeight;
    var mainMinH = getCtpTop().document.body.clientHeight - elementsH - 1;//-1为.seeyon-portal-body的border-top-width

    //默认不满一屏才设置最小高度
    if ((elementsH + mainH) < getCtpTop().document.body.clientHeight) {
        getCtpTop().document.getElementById("main").style.minHeight = mainMinH + "px"
    }
}

//渲染栏目标题区的页签（默认样式），因页签有计算宽度等功能，为了前端性能，不在tpl模板中渲染
var renderSectionTabs = function (_section) {
    //当前section栏目标题区
    var _currentTabsArea = document.getElementById("sectionHeaderTabs_" + _section.id)
    if (!_currentTabsArea) {
        return;
    }
    //当前section栏目标题区的宽度,减1是因为有的浏览器计算宽度会四舍五入，就会造成实际宽度比计算出来的小0.X像素
    var _currentTabsAreaWidth = Math.floor(_currentTabsArea.offsetWidth) - 1;
    //计算出当前section有多少个标题
    var _currentTabsNum = _section.sections.length;
    //计数器，用于计算所有标题页签的总宽度
    var _curentTabTempWidth = 0;
    //temp str
    var _tempStr = new StringBuffer();
    //temp length array
    var _tempArray = new Array();
    //遍历所有的栏目标题，计算出是否能显示下，如果显示不下，需要出下拉菜单，并将所有页签的宽度存入一个临时数组中，供后面使用
    for (var i = 0; i < _currentTabsNum; i++) {
        //当前的标题
        var _currentTab = _section.sections[i];
        //图标的宽度，如果icon为""，则宽度为0，否则宽度为20+5（5为图标右间距）
        //左右内间距：左padding + 右padding（这个后续可能会挪至后面循环中去，因为栏目可能会开放自定页签左内右间距的功能）
        var _eachTabLRPadding = 0 + 25;
        //左右外间距：左margin + 右margin（这个后续可能会挪至后面循环中去，因为栏目可能会开放自定页签左右外间距的功能）
        var _eachTabLRmargin = 0 + 0;
        //图标区宽度
        var _currentTabIconWidth = 0; //20180127，先屏蔽图标功能，但不删除相关代码，所以这里赋值为""，后续放开图标功能时删除这一行即可，并放开后面这一行
        //var _currentTabIconWidth = _currentTab.icon != "" ? 25 : 0;
        //字号，默认16号
        var _currentTabFontSize = _currentTab.f != undefined && _currentTab.f != "" ? _currentTab.f : 16;
        //页签的标题在页面中占的width
        if (_currentTab.t !== "") {
            var _currentTabText = windowAtob(_currentTab.sl) + "(" + _currentTab.t + ")" + _currentTab.tu;
        } else {
            var _currentTabText = windowAtob(_currentTab.sl)
        }
        var _currentTabTextWidth = getTextWidth(escapeSpecialChar(_currentTabText), 16);
        //当前页签的标题区需要的总宽度
        var _currentTabWidth = _currentTabIconWidth + _currentTabTextWidth + _eachTabLRPadding + _eachTabLRmargin;
        _tempArray.push(_currentTabWidth);
        _curentTabTempWidth += _currentTabWidth;
    }
    if (_curentTabTempWidth > _currentTabsAreaWidth) { //所有标题的总长度大于标题区的宽度，说明显示不下了，需要下拉（页签里仅一个标题的情况下除外）
        if (_currentTabsNum == 1) { //页签内只有一项时，强行塞下，不参与下拉
            _tempStr.append("<ul class='showedTabs' style='right:0'>");
            //当前的标题
            _currentTab = _section.sections[0];
            _currentTab.icon = ""; //20180127，先屏蔽图标功能，但不删除相关代码，所以这里赋值为""，后续放开图标功能时删除这一行即可
            if (_currentTab.icon != "") {
                var _currentTabIconStr = "<i class='vportal vp-sectionTitleIcon01'></i>";
                _currentTabIconWidth = 25;
            } else {
                var _currentTabIconStr = "";
                _currentTabIconWidth = 0;
            }
            if (_tempArray[0] > _currentTabsAreaWidth) {
                var _thisTabTextWidth = _currentTabsAreaWidth - _currentTabIconWidth;
            } else {
                var _thisTabTextWidth = _currentTabsAreaWidth - _currentTabIconWidth;
            }
            _thisTabTextWidth = _thisTabTextWidth > 0 ? _thisTabTextWidth : 0;
            //20180131新增下面一句，保证至少能显示一个汉字
            _thisTabTextWidth = _thisTabTextWidth > 30 ? _thisTabTextWidth : 30;
            //标题
            if (_currentTab.t !== "") {
                var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>" + "<span>(</span><span id='total_" + _currentTab.id + "'>" + getTotalStr(_currentTab.t) + "</span><span>)</span>" + _currentTab.tu;
            } else {
                var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>";
            }
            _tempStr.append("<li class='current' id='sectionName_" + _currentTab.id + "' style='padding-right:0'>" + _currentTabIconStr + "<div style='width:" + _thisTabTextWidth + "px' title='" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "' onclick='javascript:changeTabAndReloadSection(\"" + _section.id + "\",\"" + _currentTab.id + "\")'>" + _currentTabTextStr + "</div></li>");

            _tempStr.append("</ul>");
            _tempStr.toString();
        } else { //页签内有多项时，需要计算什么时候出下拉
            _tempStr.append("<ul class='showedTabs'>");
            //计数器，用于计算当前总宽度
            var _tempWidth = 0;
            //计数器，用于保存是哪一个标题开始超出页签的总宽度了
            var _tempIndex = 0;
            //下拉按钮的宽度，默认25px
            var _arrowMoreWidth = 25;
            //遍历一轮，将能显示下的放到标题区
            for (var i = 0, _tempWidth = _tempArray[0]; i < _currentTabsNum; i++) {
                //标题区能显示下的标题，或者处于页签中第一个标题
                if (_tempWidth <= _currentTabsAreaWidth - _arrowMoreWidth || i == 0) {
                    //当前的标题
                    _currentTab = _section.sections[i];
                    //当前的标题图标
                    _currentTab.icon = ""; //20180127，先屏蔽图标功能，但不删除相关代码，所以这里赋值为""，后续放开图标功能时删除这一行即可
                    if (_currentTab.icon != "") {
                        var _currentTabIconStr = "<i class='vportal vp-sectionTitleIcon01'></i>";
                        _currentTabIconWidth = 25;
                    } else {
                        _currentTabIconStr = "";
                        _currentTabIconWidth = 0;
                    }
                    //计算li的宽度，标题的宽度超过了标题名称区的总宽度
                    if (_tempArray[i] > _currentTabsAreaWidth - _eachTabLRPadding - _eachTabLRmargin - _arrowMoreWidth) {
                        var _thisTabTextWidth = _currentTabsAreaWidth - _arrowMoreWidth - _currentTabIconWidth;
                        var _thisLiwidth = _currentTabsAreaWidth - _arrowMoreWidth;
                        var _thsLiPaddingRight = ";padding-right:0";
                    } else {
                        var _thisTabTextWidth = (_tempArray[i]) - _currentTabIconWidth - _eachTabLRPadding - _eachTabLRmargin;
                        var _thisLiwidth = _tempArray[i] - _eachTabLRPadding - _eachTabLRmargin;
                        var _thsLiPaddingRight = "";
                    }
                    _thisTabTextWidth = _thisTabTextWidth > 0 ? _thisTabTextWidth : 0;
                    //20180131新增下面一句，保证至少能显示一个汉字
                    _thisTabTextWidth = _thisTabTextWidth > 30 ? _thisTabTextWidth : 30;
                    _thisLiwidth = _thisLiwidth > 0 ? _thisLiwidth : 0;
                    //是否有current状态，默认第一个页签有current状态
                    if (i == 0) {
                        var _currentStr = " class='current'";
                    } else {
                        var _currentStr = "";
                    }
                    //标题
                    if (_currentTab.t !== "") {
                        var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>" + "<span>(</span><span id='total_" + _currentTab.id + "'>" + getTotalStr(_currentTab.t) + "</span><span>)</span>" + _currentTab.tu;
                    } else {
                        var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>";
                    }
                    _tempStr.append("<li" + _currentStr + " id='sectionName_" + _currentTab.id + "' style='width:" + _thisLiwidth + "px" + _thsLiPaddingRight + "'>" + _currentTabIconStr + "<div style='width:" + _thisTabTextWidth + "px' title='" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "' onclick='javascript:changeTabAndReloadSection(\"" + _section.id + "\",\"" + _currentTab.id + "\")'>" + _currentTabTextStr + "</div></li>");
                    if (_tempArray[i + 1] != undefined) {
                        _tempWidth += _tempArray[i + 1];
                    } else {
                        _tempIndex = i;
                        break;
                    }
                } else {
                    _tempIndex = i;
                    break;
                }
            }
            _tempStr.append("</ul>");
            //标题区显示不下，需要在下拉中显示的标题，创建div.moreTabs
            _tempStr.append("<div class='moreTabs'>");
            _tempStr.append("<div class='arrowMore'><i class='vportal vp-arrow-down-serif'></i></div>");
            _tempStr.append("<div class='moreListArea'>");
            _tempStr.append("<ul class='moreList'>");
            //再遍历显示不下的后面几个标题，放进下拉中去
            for (var i = _tempIndex; i < _currentTabsNum; i++) {
                //当前的标题
                _currentTab = _section.sections[i];
                if (_currentTab.t !== "") {
                    var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>" + "<span>(</span><span id='total_" + _currentTab.id + "'>" + getTotalStr(_currentTab.t) + "</span><span>)</span>" + _currentTab.tu;
                } else {
                    var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>";
                }
                //标题
                _tempStr.append("<li id='sectionName_" + _currentTab.id + "' title='" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "' onclick='javascript:changeTabAndReloadSection(\"" + _section.id + "\",\"" + _currentTab.id + "\")'>" + "<div>" + _currentTabTextStr + "</div></li>");
            }
            _tempStr.append("</ul>");
            _tempStr.append("</div>");
            _tempStr.append("</div>");
            _tempStr.toString();
        }
    } else { //能显示下，无需下拉
        _tempStr.append("<ul class='showedTabs'>");
        for (var i = 0; i < _currentTabsNum; i++) {
            //当前的标题
            _currentTab = _section.sections[i];
            //当前的标题图标
            _currentTab.icon = ""; //20180127，先屏蔽图标功能，但不删除相关代码，所以这里赋值为""，后续放开图标功能时删除这一行即可
            _currentTabIconStr = _currentTab.icon != "" ? "<i class='vportal vp-sectionTitleIcon01'></i>" : "";
            //是否有current状态，默认第一个页签有current状态
            if (i == 0) {
                var _currentStr = " class='current'";
            } else {
                var _currentStr = "";
            }
            //标题右侧的总数
            if (_currentTab.t !== "") {
                var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>" + "<span>(</span><span id='total_" + _currentTab.id + "'>" + getTotalStr(_currentTab.t) + "</span><span>)</span>" + _currentTab.tu;
            } else {
                var _currentTabTextStr = "<span id='titleName_" + _currentTab.id + "'>" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "</span>";
            }
            //标题
            var _thisLiwidth = _tempArray[i] - _eachTabLRPadding - _eachTabLRmargin;
            _tempStr.append("<li " + _currentStr + " id='sectionName_" + _currentTab.id + "' title='" + escapeSpecialChar(windowAtob(_currentTab.sl)) + "' onclick='javascript:changeTabAndReloadSection(\"" + _section.id + "\",\"" + _currentTab.id + "\")'>" + _currentTabIconStr + "<div>" + _currentTabTextStr + "</div></li>");
        }
        _tempStr.append("</ul>");
        _tempStr.toString();
    }
    //将页签追加至DOM中
    _currentTabsArea.innerHTML = _tempStr;
    //release memory
    _tempArray = _tempStr = _tempWidth = null;
}

//处理total，如果大于999时，显示为999+
var getTotalStr = function (_t) {
    if (typeof (_t) !== "undefined" && _t !== "") {
        if (Number(_t) > 999) {
            var _tempStr = "<span class='total999'>999<sup>+</sup></span>";
        } else {
            var _tempStr = "" + _t;
        }
        return _tempStr;
    } else {
        return "";
    }
}

//渲染栏目标题区页签：样式2
var renderSectionTabs_card2 = function (_section) {
    var _tabsLen = _section.sections.length;
    var _currentTabsArea = document.getElementById("section-header-card2_" + _section.id);
    var _currentTabsAreaWidth = Math.floor(_currentTabsArea.offsetWidth) - 1;
    var _eachLiRightMargin = 10; // LI的右间距
    var _eachTabDefaultWidth = 145 + _eachLiRightMargin + 2; //145的宽度+LI的右间距+2像素的边框
    var _tabColorArray = ["#49a4ea", "#8693f3", "#5484ff", "#ff916e", "#3cbaff", "#38a0f5", "#ffae43"];
    var _eachRowNumber = Math.floor((_currentTabsAreaWidth + _eachLiRightMargin) / _eachTabDefaultWidth);
    var _tempStr = new StringBuffer();
    if (_tabsLen <= _eachRowNumber) {
        //一行能显示下所有页签
        _tempStr.append("<ul class='tabCard oneRow'>");
        for (var i = 0; i < _tabsLen; i++) {
            _tempStr.append(packagingCardTabsLi(_section, _section.sections[i], i, undefined, true));
        }
        _tempStr.append("</ul>");
        _currentTabsArea.innerHTML = _tempStr.toString();
        _tempStr = null;
    } else {
        //一行显示不下所有页签，需要两行
        //每列的宽度，重新计算
        var _eachRowWidth = Math.floor((_currentTabsAreaWidth - (Math.ceil(_tabsLen / 2) - 1) * _eachLiRightMargin) / Math.ceil(_tabsLen / 2) - 2);
        _eachRowWidth = _eachRowWidth > 145 ? _eachRowWidth : 145;
        var _thisUlWidth = (_eachRowWidth + 2) * Math.ceil(_tabsLen / 2) + _eachLiRightMargin * Math.ceil(_tabsLen / 2) - _eachLiRightMargin;
        if (_tabsLen % 2 == 0) {
            //页签总数为偶数，采用两行横向平铺的显示方式
            _tempStr.append("<ul class='tabCard twoRow evenCard firstRow' style='width:" + _thisUlWidth + "px'>");
            //第一行
            for (var i = 0; i < (_tabsLen / 2); i++) {
                _tempStr.append(packagingCardTabsLi(_section, _section.sections[i], i, _eachRowWidth, false));

            }
            _tempStr.append("</ul>");
            _tempStr.append("<ul class='tabCard twoRow evenCard lastRow' style='width:" + _thisUlWidth + "px'>");
            //第二行
            for (var i = _tabsLen / 2; i < _tabsLen; i++) {
                _tempStr.append(packagingCardTabsLi(_section, _section.sections[i], i, _eachRowWidth, false));
            }
            _tempStr.append("</ul>");
            _currentTabsArea.innerHTML = _tempStr.toString();
            _tempStr = null;
        } else {
            //页签总数为奇数，采用两行1+N的显示方式
            _tempStr.append("<div class='tabCard twoRow oddCard' style='width:" + _thisUlWidth + "px'>");
            //第一列
            _tempStr.append("<ul class='firstTab'>");
            _tempStr.append(packagingCardTabsLi(_section, _section.sections[0], 0, _eachRowWidth, true));
            _tempStr.append("</ul>");
            //其它列
            _tempStr.append("<div class='otherTab'>");
            var _firstRowEndNum = (_tabsLen - 1) / 2;
            //其它列：第一行
            _tempStr.append("<ul class='firstRow'>");
            for (var i = 1; i <= _firstRowEndNum; i++) {
                _tempStr.append(packagingCardTabsLi(_section, _section.sections[i], i, _eachRowWidth, false));

            }
            _tempStr.append("</ul>");
            //其它列：第二行
            _tempStr.append("<ul>");
            for (var i = _firstRowEndNum + 1; i < _tabsLen; i++) {
                _tempStr.append(packagingCardTabsLi(_section, _section.sections[i], i, _eachRowWidth, false));

            }
            _tempStr.append("</ul>");
            _tempStr.append("</div>");
            _tempStr.append("</div>");
            _currentTabsArea.innerHTML = _tempStr.toString();
            _tempStr = null;
        }
    }

    //ie8不支持css里面的:last-child 用js实现下
    if (SeeUtils.isIE8) {
        var _liLastParents = _currentTabsArea.querySelectorAll(".section-header-card2 ul");
        for (var i = 0; i < _liLastParents.length; i++) {
            _liLastParents[i].lastChild.style.marginRight = "0px";
        }
    }
}

//组装页签的LI：样式2
var packagingCardTabsLi = function (_currentSection, _sectionTab, i, _eachRowWidth, _showIconBg) {
    var _tabColorArray = ["#49a4ea", "#8693f3", "#5484ff", "#ff916e", "#3cbaff", "#38a0f5", "#ffae43"];
    var _tempLiStr = new StringBuffer();
    var _currentStatus = i == 0 ? " class='current'" : "";
    var _colorIndex = i < 7 ? i : (i + 1) % 7 - 1;
    var _thisLiwidth = _eachRowWidth != undefined ? "width:" + _eachRowWidth + "px" : "";
    //是否显示背景图标
    if (_showIconBg) {
        var _iconBgStr = "<div class='bgIcon'><i class='vportal vp-colourful-icons" + _colorIndex + "'></i></div>";
    } else {
        var _iconBgStr = "";
    }
    //根据是否有总数，拼接成不同的dom
    if (_sectionTab.t != null && _sectionTab.t != "") {
        //有总数的页签
        _tempLiStr.append("<li id='sectionName_" + _sectionTab.id + "'" + _currentStatus + " title='" + escapeSpecialChar(windowAtob(_sectionTab.sl)) + "' style='background-color:" + _tabColorArray[_colorIndex] + ";" + _thisLiwidth + "' onclick='javascript:changeTabAndReloadSection(\"" + _currentSection.id + "\",\"" + _sectionTab.id + "\")'><div class='leftTotalNum' id='total_" + _sectionTab.id + "'>" + getTotalStr(_sectionTab.t) + "</div><div class='rightName text_overflow' id='titleName_" + _sectionTab.id + "'>" + escapeSpecialChar(windowAtob(_sectionTab.sl)) + "</div>" + _iconBgStr + "<div class='rhombus' style='border-top-color:" + _tabColorArray[_colorIndex] + "'></div></li>");
    } else {
        //无总数的页签
        _tempLiStr.append("<li id='sectionName_" + _sectionTab.id + "'" + _currentStatus + " title='" + escapeSpecialChar(windowAtob(_sectionTab.sl)) + "'  style='background-color:" + _tabColorArray[_colorIndex] + ";" + _thisLiwidth + "' onclick='javascript:changeTabAndReloadSection(\"" + _currentSection.id + "\",\"" + _sectionTab.id + "\")'><div class='fullBockName text_overflow' id='titleName_" + _sectionTab.id + "'>" + escapeSpecialChar(windowAtob(_sectionTab.sl)) + "</div>" + _iconBgStr + "<div class='rhombus' style='border-top-color:" + _tabColorArray[_colorIndex] + "'></div></li>");
    }
    return _tempLiStr;
}

/*--切换标题页签--*/
var changeTabAndReloadSection = function (_sectionId, _panelId) {
    if (vPortal && vPortal.isDesigner) {
        return;
    }
    vPortal.pageLoad = false;
    document.getElementById("panelEditArea_" + _sectionId).innerHTML = "";
    //处理内容区高度，以免出现页面跳动
    var _panelBodyDom = document.getElementById("panelBody_" + _sectionId);
    _panelBodyDom.innerHTML = "<span class='contentLoading'>" + $.i18n('section.loading.label') + "<span class='ellipsis-anim'></span></span>";

    //防护一下，有的栏目模板没有定义相关的js，避免报错
    if (typeof (vPortal.sectionHandler[vPortal.allSectionPanels[_panelId].rf]) != "undefined") {
        //如果在栏目模板里面定义了autoHeight=true则不设置
        if (typeof (vPortal.sectionHandler[vPortal.allSectionPanels[_panelId].rf].autoHeight) == "undefined" || (typeof (vPortal.sectionHandler[vPortal.allSectionPanels[_panelId].rf].autoHeight) != "undefined" && vPortal.sectionHandler[vPortal.allSectionPanels[_panelId].rf].autoHeight === false)) {
            //不需要判断页签样式的区别，setSectionBodyHeight会处理
            setSectionBodyHeight(_panelId, vPortal.allSectionPanels[_panelId].entityId, vPortal.allSectionPanels[_panelId].bodyHeight);

            //废弃下面的代码，待验证
            /*var _sectionHeaderDom = document.getElementById("sectionHeaderTabs_" + _sectionId);
            //如果能找着sectionHeaderTabs_+_sectionId的dom，表明是默认的页签样式，不是卡片1或卡片2的样式
            if(_sectionHeaderDom){
                _panelBodyDom.style.height = Number(vPortal.allSectionPanels[_panelId].bodyHeight) - vPortal.sectionPaddingTop - vPortal.sectionPaddingBottom + "px";
            }else{
                //卡片1或卡片2的样式 需要减去body区域里面的页签高度
                setSectionBodyHeight(_panelId, vPortal.allSectionPanels[_panelId].entityId, vPortal.allSectionPanels[_panelId].bodyHeight)
                // _panelBodyDom.style.height = Number(vPortal.allSectionPanels[_panelId].bodyHeight) - vPortal.sectionPaddingTop - vPortal.sectionPaddingBottom + "px";
            }*/

        } else if (typeof (vPortal.sectionHandler[vPortal.allSectionPanels[_panelId].rf].autoHeight) != "undefined" && vPortal.sectionHandler[vPortal.allSectionPanels[_panelId].rf].autoHeight === true) {
            //设置了autoHeight的时候，需要还原
            _panelBodyDom.style.height = "";
        }
    }

    //如果是浏览态，刷新栏目，如果是编辑器，渲染对应的栏目属性编辑页
    if (!vPortal.sectionPropertyIsEditing) {
        //渲染这个panel(页签)的数据
        var _renderThisPanel = new renderEachPanel(_panelId, "changeTab");
        _renderThisPanel = null;
    } else {
        sectionOperation(_panelId, "loadSectionPro");
    }
    //多页签的高亮效果
    setTabHighlight(_sectionId, _panelId);
    //更新sectionOperation中几个按钮的参数
    updateSectionOperationLink(_sectionId, _panelId);
    //AI智能排序
    //checkAndSetAiSort(_sectionId, _panelId, vPortal.allSectionPanels[_panelId].aiSort, vPortal.allSectionPanels[_panelId].aiSortValue);
};

//处理多页签的高亮效果
var setTabHighlight = function (_sectionId, _panelId) {
    var _sectionHeaderDom = document.getElementById("sectionHeaderTabs_" + _sectionId);
    //如果能找着sectionHeaderTabs_+_sectionId的dom，表明是默认的页签样式，不是卡片1或卡片2的样式
    if (_sectionHeaderDom) {
        if (_sectionHeaderDom.querySelector(".showedTabs")) {
            var _mainLi = _sectionHeaderDom.querySelector(".showedTabs").querySelectorAll("li");
            for (var i = 0, _len = _mainLi.length; i < _len; i++) {
                _mainLi[i].setAttribute("class", "");
            }
        }
        if (_sectionHeaderDom.querySelector(".moreTabs")) {
            var _moreLi = _sectionHeaderDom.querySelector(".moreTabs").querySelectorAll("li");
            for (var i = 0, _len = _moreLi.length; i < _len; i++) {
                _moreLi[i].setAttribute("class", "");
            }
        }
    } else {
        //页签样式1、样式2
        if (!document.getElementById("section-header-" + _sectionId)) {
            return;
        }
        var _sectionHeaderAllLi = document.getElementById("section-header-" + _sectionId).querySelectorAll("li");
        for (var i = 0, _len = _sectionHeaderAllLi.length; i < _len; i++) {
            _sectionHeaderAllLi[i].setAttribute("class", "");
        }
    }
    if (document.getElementById("sectionName_" + _panelId)) {
        document.getElementById("sectionName_" + _panelId).setAttribute("class", "current");
    }
}

//通过panelId渲染某个页签
var renderEachPanel = function (_panelId, _from, _params) {
    vPortal.pageLoad = false;
    var _sectionPanel = vPortal.allSectionPanels[_panelId];
    var _panelTabLi = document.getElementById("sectionName_" + _panelId);
    //当来自vPortal.sectionHandler.reload，只渲染高亮的页签，非高亮的panel不渲染，直接return
    if (_panelTabLi && _from == "reload") {
        var _isCurrent = _panelTabLi.getAttribute("class");
        if (_isCurrent == null) {
            return
        } else if (_isCurrent.indexOf("current") < 0) {
            return;
        }
    }
    var _paramKeys = null;
    var _paramValues = null;
    if (_params) {
        if (typeof (_sectionPanel.paramKeys) != 'undefined' && _sectionPanel.paramKeys) {

            var paramKeysMap = new Object();
            if (_params.paramKeys && _params.paramValues) {
                for (var j = 0; j < _params.paramKeys.length; j++) {
                    paramKeysMap[_params.paramKeys[j]] = _params.paramValues[j];
                }
            }

            //    		var oldParamKeys= _sectionPanel.paramKeys;
            //        	var oldParamValues= _sectionPanel.paramValues;
            /**
             * 对象引用传递导致更新了多个栏目的信息
             * 相关bug号：OA-145933 OA-145933
             * marked by ouyp 2018-04-17
             */
            var oldParamKeys = $.parseJSON($.toJSON(_sectionPanel.paramKeys));
            var oldParamValues = $.parseJSON($.toJSON(_sectionPanel.paramValues));

            for (var i = 0; i < oldParamKeys.length; i++) {
                var paramKey = oldParamKeys[i];
                if (paramKey == "lineHeight") {
                    oldParamValues[i] = vPortal.sectionBodyLineHeight.toString();
                } else if (paramKeysMap[paramKey]) {
                    oldParamValues[i] = paramKeysMap[paramKey];
                    delete paramKeysMap[paramKey];
                }
            }

            for (var paramKey in paramKeysMap) {
                var paramValue = paramKeysMap[paramKey];
                if (paramValue) {
                    oldParamValues[oldParamValues.length] = paramValue;
                    oldParamKeys[oldParamKeys.length] = paramKey;
                }
            }
            _paramKeys = oldParamKeys;
            _paramValues = oldParamValues;
            _sectionPanel.paramKeys = _paramKeys;
            _sectionPanel.paramValues = _paramValues;
        } else {
            //定义参数
            //行高
            _paramKeys = new Array();
            _paramKeys[0] = 'lineHeight';
            _paramValues = new Array();
            _paramValues[0] = vPortal.sectionBodyLineHeight.toString();
            if (_params.paramKeys) {
                _paramKeys = _paramKeys.concat(_params.paramKeys);
            }
            if (_params.paramValues) {
                _paramValues = _paramValues.concat(_params.paramValues);
            }
        }
    } else {
        _paramKeys = _sectionPanel.paramKeys;
        _paramValues = _sectionPanel.paramValues;
    }

    //其它参数
    var _parameter = {
        //section的X坐标
        "x": _sectionPanel.x,
        //section的Y坐标
        "y": _sectionPanel.y,
        //section的序号
        "xIndex": _sectionPanel.xIndex,
        //当前section下第一个标题页签的sectionBeanId
        "sectionBeanId": _sectionPanel.sectionBeanId,
        //当前section的ID
        "entityId": _sectionPanel.entityId,
        //是多标题页签中的第几个，因为此轮只渲染标题页签中的第一个栏目，所以值为"0"
        "ordinal": "" + _sectionPanel.ordinal,
        "r_ordinal": "" + _sectionPanel.r_ordinal,
        "fadd": "" + _sectionPanel.fadd,
        //空间id
        "spaceId": _sectionPanel.spaceId,
        //空间类型
        "spaceType": _sectionPanel.spaceType,
        //swidth
        "width": _sectionPanel.sWidth,
        //项目空间需要的projectId
        "ownerId": _sectionPanel.ownerId,
        //项目空间需要的sprint
        "sprint": vPortal.sprint,
        //把宽度传到栏目内容里面去
        "sectionWidth": _sectionPanel.sectionWidth,
        //把是否显示大标题传到栏目内容里面去
        "sbt": _sectionPanel.sbt,
        //把是否显示小标题传到栏目内容里面去
        "sst": _sectionPanel.sst,
        //把大标题传到栏目内容里面去
        "b_t": _sectionPanel.b_t,
        //把大标题样式传到栏目内容里面去
        "b_s": _sectionPanel.b_s,
        //因为每次登录首页时，后台会查一次lineHeight并输出到前端，在这里传给后台，就不需要后台每次渲染栏目时再去查一次lineHeight
        //传了参数的话把key给带进去
        "paramKeys": _paramKeys,
        //传了参数的话把value给带进去
        "paramValues": _paramValues,
        //传入 panelId，供后续使用
        "panelId": _panelId,
        //传入 bgc，供后续使用
        "bgc": _sectionPanel.bgc,
        //传入 bodyHeight，供后续使用
        "bodyHeight": _sectionPanel.bodyHeight,
        //传入 resolveFunction，供后续使用
        "rf": _sectionPanel.rf,
        //传入 aiSort，供后续使用
        "aiSort": vPortal.allSectionPanels[_panelId].aiSort,
        //传入 aiSortValue，供后续使用
        "aiSortValue": vPortal.allSectionPanels[_panelId].aiSortValue
    }

    var _extParameter = {
        //传入来源
        "from": _from,
        "total": _sectionPanel.total
    }

    //来自于“换一换”
    if (_from == "fromRefreshAnotherData") {
        _parameter.pageNo = _params.pageNo;
    }

    // //更新vPortal.allSectionPanels
    // var _currentPanelSpaceId = _sectionPanel.spaceId;
    // var currentSpaceSummary = vPortal.spacesSummary[_currentPanelSpaceId];
    // var _currentSectionSummary_X = currentSpaceSummary.portlets[_sectionPanel.y][_sectionPanel.x];
    // //更新当前panel在vPortal.allSectionPanels中缓存的信息
    // cacheThisPanel(_parameter, _sectionPanel.index, _extParameter);
    if (_panelTabLi && _from == "tabRefresh") {
        var _isCurrent = _panelTabLi.getAttribute("class");
        if (_isCurrent == null) {
            return;
        } else if (_isCurrent.indexOf("current") < 0) {
            return;
        }
    }
    //执行栏目的相关JS，以及渲染栏目的数据
    var _getAndDrawColumnData = new getInitFunction(_parameter, _extParameter);
}

//更新当前portlet下所有页签的total数据
var updateCurrentPortletAllTotal = function (_currentSection, y, x, index) {
    if (vPortal.admin == "true") { //管理员状态，不执行这个方法
        return;
    }
    var _parameter = {
        "pagePath": pagePath,
        "portletId": _currentSection.id,
        "tab": index.toString()
    }
    callBackendMethod("sectionManager", "getPortletTotals", _parameter, {
        success: function (_result) {
            if (_result == '__LOGOUT') {
                offlineFun();
                return;
            }
            if (_result != null && _currentSection.sections.length > 1) {
                for (var i = 0; i < _result.length; i++) {
                    if (!_currentSection.sections[i]) {
                        continue;
                    }
                    var _currentPanelDom = document.getElementById("sectionName_" + _currentSection.sections[i].id);
                    if (_currentPanelDom && _result[i][0] && _result[i][0] != "" && _result[i][0] != "0") {
                        var _totalNumber = _result[i][0];
                        var _totalNumber = Number(_result[i][0]) > 999 ? "<span style='padding-right:8px;font-size:14px;position:relative'>999<sup>+</sup></span>" : _result[i][0];
                        var _totalUnit = _result[i][1];
                        var _titleStr = _currentPanelDom.getAttribute("title") == null ? "" : _currentPanelDom.getAttribute("title");
                        if (_currentSection.style == "default" || _currentSection.style == "standard") {
                            var _totalStr = _result[i][0] != "" ? _totalNumber + _totalUnit : "";
                            _currentPanelDom.setAttribute("title", _titleStr + _totalStr);
                        } else {
                            var _totalStr = _result[i][0] != "" ? "" + _totalNumber + _totalUnit + "" : "";
                            _currentPanelDom.setAttribute("title", _titleStr + _totalStr);
                        }
                        var _currentTotalDom = document.getElementById("total_" + _currentSection.sections[i].id);
                        if (_currentTotalDom) { //fix bug: 缺陷 	OA-145012,防护下
                            _currentTotalDom.innerHTML = _totalStr;
                        }
                    }
                }
            }
        },
        error: function (error) {
            console.error(error);
        }
    });
}

//更新sectionOperation中几个按钮的参数
var updateSectionOperationLink = function (_sectionId, _panelId) {
    //修改右侧4个按钮事件中的id
    if (document.getElementById("sectionButtonSetting_" + _sectionId)) {
        document.getElementById("edit_section_" + _sectionId).setAttribute("onclick", "javascript:sectionOperation('" + _panelId + "','loadSectionPro')");
        document.getElementById("del_section_" + _sectionId).setAttribute("onclick", "javascript:sectionOperation('" + _panelId + "','deleteFragment')");
        document.getElementById("add_section_" + _sectionId).setAttribute("onclick", "javascript:sectionOperation('" + _panelId + "','addSectionsToFrag')");
        document.getElementById("mbt_section_" + _sectionId) && document.getElementById("mbt_section_" + _sectionId).setAttribute("onclick", "javascript:sectionOperation('" + _panelId + "','modifyBigTitlePro')");
    }
}

//渲染单个portlet，包含外框和数据，参数来源为panelId
var renderPortletFromPanelId = function (_panelId) {
    //当前的portlet Dom
    var _currentSectionPanel = vPortal.allSectionPanels[_panelId];
    if (_currentSectionPanel == undefined) {
        console.log("panelId错误，请检查代码");
        return;
    }
    var _currentPortletEntityId = _currentSectionPanel.entityId;
    var _currentPortletDom = document.getElementById("section_" + _currentPortletEntityId);
    _currentPortletDom.innerHTML = "";
    var y = _currentSectionPanel.y;
    var x = _currentSectionPanel.x;
    var xIndex = _currentSectionPanel.xIndex;
    //当前的portlet
    var _currentPortlet = vPortal.spacesSummary[_currentSectionPanel.spaceId].portlets[y][xIndex];
    //如果sections<1时，将sbt强制改为0，因为有时多页签栏目删除页签后，后台未进行sbt的更新，这里防护一下
    if (_currentPortlet.sections.length == 1) {
        _currentPortlet.sbt = "0";
    }
    //有些缓存的变量在请求回来的spacesSummary中没有，传一下
    _currentPortlet._sectionWidth = _currentSectionPanel.sectionWidth;
    //调用模板引擎渲染栏目外框架
    //渲染：根据id，渲染对应的栏目外框架（含标题区、内容区，不含数据）至它的占位div中
    var sectionTpl = document.getElementById("tpl-sectionMainFrame").innerHTML;
    renderTpl(_currentPortlet, sectionTpl, "section_" + _currentPortletEntityId);
    var _tabStyle = _currentPortlet.style;
    var _tabLen = _currentPortlet.sections.length;
    //渲染栏目的页签
    if (_currentPortlet.sst === "1" && _tabStyle !== undefined && (((_tabStyle === "default" || _tabStyle === "standard") && _tabLen > 0) || ((_tabStyle === "card1" || _tabStyle === "card2") && _tabLen === 1))) {
        renderSectionTabs(_currentPortlet);
    } else if (_currentPortlet.sst == "1" && _currentPortlet.style == "card2" && _currentPortlet.sections.length > 0) {
        renderSectionTabs_card2(_currentPortlet);
    }
    //多页签的高亮效果
    setTabHighlight(_currentPortlet.id, _panelId);
    //渲染栏目的数据
    var _renderEachPanel = new renderEachPanel(_panelId);
    _renderEachPanel = null;
    //更新当前portlet下所有页签的total数据
    updateCurrentPortletAllTotal(_currentPortlet, y, x, _currentSectionPanel.ordinal);
    //更新sectionOperation中几个按钮的参数
    var _entityId = _currentSectionPanel.entityId;
    updateSectionOperationLink(_entityId, _panelId);
    //AI智能排序按钮
    checkAndSetAiSort(_entityId, _panelId, vPortal.allSectionPanels[_panelId].aiSort, vPortal.allSectionPanels[_panelId].aiSortValue);
}

//渲染单个portlet，包含外框和数据，参数来源为portletId
var renderPortletFromPortletId = function (_portletId) {
}
//AI智能排序按钮
var checkAndSetAiSort = function (_sectionId, _panelId, _hasAiSortBtn, _aiSortValue) {
    if (document.getElementById("aiSort-" + _sectionId)) {
        if (_hasAiSortBtn === "1" && document.getElementById("sectionHeaderTabs_" + _sectionId) && document.getElementById("sectionHeaderTabs_" + _sectionId).querySelector(".showedTabs") && document.getElementById("sectionHeaderTabs_" + _sectionId).querySelector(".showedTabs").querySelector("#sectionName_" + _panelId) || _hasAiSortBtn === "1" && document.getElementById("section-bigTitle-" + _sectionId)) {
            //_hasAiSortBtn为1，且该待办栏目不在多页签的下拉中时，需求显示AI排序按钮，或者当前栏目有大标题且_hasAiSortBtn为1时，也需要显示AI排序按钮
            document.getElementById("aiSort-" + _sectionId).style.display = "block";
            var _aiSortClass = _aiSortValue === "1" ? "AiSortOn" : "AiSortOff";
            if (_aiSortValue == "1") {
                document.getElementById("aiSort-" + _sectionId).innerHTML = "<span onclick=\"javascript:changeAiSort('" + _sectionId + "','" + _panelId + "','" + _aiSortValue + "')\" class=\"vportal vp-" + _aiSortClass + "\" title=\"" + $.i18n('section.ai.sortCancel.label') + "\"></span>";
            } else {
                document.getElementById("aiSort-" + _sectionId).innerHTML = "<span onclick=\"javascript:changeAiSort('" + _sectionId + "','" + _panelId + "','" + _aiSortValue + "')\" class=\"vportal vp-" + _aiSortClass + "\" title=\"" + $.i18n('section.ai.sort.label') + "\"></span>";
            }
        } else {
            //无需显示AI排序按钮
            document.getElementById("aiSort-" + _sectionId).style.display = "none";
        }
    }
}

//AI智能排序的功能
var changeAiSort = function (_sectionId, _panelId, _value) {
    //根据_value，改变“智能排序”按钮的状态，并请求和渲染此栏目，并通过_params告知后台，这个操作来自于点击“智能排序”按钮
    if (_value === "1") {
        vPortal.allSectionPanels[_panelId].aiSortValue = "0";
        document.getElementById("aiSort-" + _sectionId).innerHTML = "<span onclick=\"javascript:changeAiSort('" + _sectionId + "','" + _panelId + "','0')\" class=\"vportal vp-AiSortOff\" title=\"" + $.i18n('section.ai.sort.label') + "\"></span>";
        var _params = {
            paramKeys: ["aiSort", "aiSortValue", "setAiSort"],
            paramValues: ["1", "0", "1"]
        }
        renderEachPanel(_panelId, "changeAiSort", _params);
    } else {
        vPortal.allSectionPanels[_panelId].aiSortValue = "1";
        document.getElementById("aiSort-" + _sectionId).innerHTML = "<span onclick=\"javascript:changeAiSort('" + _sectionId + "','" + _panelId + "','1')\" class=\"vportal vp-AiSortOn\" title=\"" + $.i18n('section.ai.sortCancel.label') + "\"></span>";
        var _params = {
            paramKeys: ["aiSort", "aiSortValue", "setAiSort"],
            paramValues: ["1", "1", "1"]
        }
        renderEachPanel(_panelId, "changeAiSort", _params);
    }
}

/*--sectionHandler：删除、添加、编辑栏目--*/
var portalSectionHanderJsIsLoad = false;
var sectionOperation = function (_sectionId, _operationType) {
    if (portalSectionHanderJsIsLoad) {
        portalSectionHander[_operationType](_sectionId);
    } else {
        var _portalSectionHanderJSURL = "/seeyon/portal/sections/portalSectionHander.js";
        $.getScript(_portalSectionHanderJSURL, function (_result, _textStatus, _jqXHR) {
            //请求过来的JS文件实际为一串string，需要eval一下
            eval(_result);
            portalSectionHander[_operationType](_sectionId);
            portalSectionHanderJsIsLoad = true;
        }, _sectionId);
    }
}

/*--栏目内的数据打开链接--*/
var _openDataLink = function (_parameter, e) {
    debugger;

    console.log("栏目内的数据打开链接");
    //重复点击防护
    var eventFlag = avoidRepeatedClicks(_parameter.url);
    if (!eventFlag) return;

    //只有ie8 ie9下需要进行事件冒泡的阻止
    if (e != null && SeeUtils.isIE) {
        if (SeeUtils.isIE8) {
            //这是IE浏览器
            e.cancelBubble = true; //阻止冒泡事件
            e.returnValue = false; //阻止默认事件
        } else if (e && e.stopPropagation) {
            e.stopPropagation(); //阻止冒泡事件
            e.preventDefault(); //阻止默认事件
        }
    }

    // 标记已读
    var _obj = _parameter.obj;
    if (_obj && _obj.parentNode && _obj.parentNode.className.indexOf("articel_title") != -1 && _obj.parentNode.className.indexOf("AlreadyRead") == -1) {
        _obj.parentNode.className = _obj.parentNode.className + " AlreadyRead";
    }
    //如果传入的是js事件，就直接执行
    if (_parameter.url.indexOf("javascript:") != -1) {
        eval(_parameter.url);
        return;
    } else if (_parameter.handler) {
        var handlerType = _parameter.handler.type;
        switch (handlerType) {
            case "dialog":
                //打开dialog
                var dialog = $.dialog({
                    title: _parameter.label,
                    url: _ctxPath + _parameter.url,
                    width: _parameter.handler.width,
                    height: _parameter.handler.height,
                    targetWindow: _parameter.target,
                    buttons: [{
                        id: "ok",
                        isEmphasize: true,
                        text: $.i18n('common.button.ok.label'),
                        handler: function () {
                            dialog.close();
                            vPortal.sectionHandler.reload(_parameter.sectionBeanId); //刷新栏目
                        }
                    }, {
                        text: $.i18n('common.button.cancel.label'),
                        handler: function () {
                            dialog.close();
                        }
                    }]
                });
                break;
        }
        return;
    } else if (_parameter.url == "") {
        return;
    }
    var _openType = parseInt(_parameter.openType, 4);
    var _openUrl = _parameter.url.indexOf("http") == 0 || _parameter.url.indexOf("ftp") == 0 ? _parameter.url : _ctxPath + _parameter.url;
    // BUG_普通_V5_V7.0SP2_一星卡_无锡市地方税务局_通过空间栏目进入文档中心，点击“上传文件”按钮没有反应_20181107070189_2018-11-07
    // OA-164227 文档中心栏目栏目样式为多图轮播，显示子文件夹中的文档选否时，从栏目展现的文档夹穿透后，对文档进行文档封面、借阅、公开到广场等操作都没反应
    // 多图轮播时 文档内容中的文档夹 弹出窗口中的上传组件会报错，由于多图轮播无法传递openType,临时解决办法
    if (_openUrl.indexOf("method=docHomepageIndex") != -1 && _parameter.sectionBeanId == "knowledgeClassShowDocFolderSection") {
        _openType = 2;
    }
    // console.log(_openUrl,'zhou');
    //zhou:徐医附院  为了区分内联系统和点击跟多事件冲突
    if (_openUrl.indexOf('linkSystemController') != -1 && _openUrl.indexOf('method=linkConnect') != -1) {
        _openType = 5;

    }


    // $.ajaxSettings.async = false;
    // $.post("/seeyon/ext/loginCheck.do?method=getLinkid",null,function(data){
    //     var link =data.link;
    //     var arr=link.split(",");
    //     // if(_openUrl.indexOf('linkSystemController')!=-1 && _openUrl.indexOf('method=linkConnect')!=-1){
    //     for (var i = 0; i < arr.length; i++) {
    //         if(_openUrl.indexOf(arr[i])!=-1 ){
    //             _openType = 5;
    //             break;
    //         }
    //     }
    // });
    // $.ajaxSettings.async = true;

    switch (_openType) {
        case 0:
            //openWorkSpace 弹出 满工作区
            openCtpWindow({
                'url': _openUrl
            });
            break;
        case 1:
            //openWorkSpaceRight 弹出 只占用右边工作区
            openCtpWindow({
                'url': _openUrl
            });
            break;
        case 2:
            //href 直接超链
            showMenu(_openUrl, _parameter._sectionBeanId, 'mainFrame', '', this);
            break;
        case 3:
            //href_blank 直接超链，在新窗口打开
            openCtpWindow({
                'url': _openUrl
            });
            break;
        case 4:
            //multiWindow 多窗口打开
            openCtpWindow({
                'url': _openUrl
            });
            break;
        case 5:
            ///zhou:徐医附院
            //href_blank 直接超链，在新窗口打开
            var linid = _openUrl.substring(_openUrl.indexOf('linkId=') + 7);
            $.post("/seeyon/ext/loginCheck.do?method=index", {linkId: linid}, function (data) {
                var code = data.code;
                if (code == 0) {
                    var type = data.flag;
                    var linkIds = data.linkIds;
                    if (linkIds != '') {
                        var arr = linkIds.split(",");
                        if (type == 'nei') {
                            for (var i = 0; i < arr.length; i++) {
                                if (_openUrl.indexOf(arr[i]) != -1) {
                                    openCtpWindow({
                                        'url': _openUrl
                                    });
                                    break;
                                }
                            }
                        }
                        if (type == 'yuan') {
                            for (var i = 0; i < arr.length; i++) {
                                if (_openUrl.indexOf(arr[i]) != -1) {
                                    openCtpWindow({
                                        'url': _openUrl
                                    });
                                    break;
                                }
                            }
                        }
                        if (type == 'gong') {
                            for (var i = 0; i < arr.length; i++) {
                                if (_openUrl.indexOf(arr[i]) != -1) {
                                    openCtpWindow({
                                        'url': _openUrl
                                    });
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    confirm(data.msg);
                }
            });
            break;
        default:
            openCtpWindow({
                'url': _openUrl
            });
            break;
    }

}

//刷新页面中某个类型的所有栏目
vPortal.sectionHandler.reload = function (_sectionBeanId) {
    //在二级页面的时候就不刷新了，避免报错
    if (document.getElementById("main").className.indexOf("hasIframe") > -1) return;
    for (var key in vPortal.allSectionPanels) {
        if (vPortal.allSectionPanels[key].sectionBeanId === _sectionBeanId) {
            renderEachPanel(key, "reload");
        }
    }
}

//更新栏目加载状态
vPortal.sectionHandler.updateLoadStateBySectionBeanId = function (_sectionBeanId) {
    if (vPortal.allSectionPanels[_sectionBeanId]) {
        vPortal.allSectionPanels[_sectionBeanId].isLoad = "isEnd";
    }
}

//消息盒子新来消息后刷新所有消息栏目
vPortal.sectionHandler.refreshMessageIdsForSection = function (messages) {
    for (var key in vPortal.allSectionPanels) {
        if (vPortal.allSectionPanels[key].sectionBeanId == "messageSection") {
            vPortal.sectionHandler.messageTemplete.unshiftMessageIds(vPortal.allSectionPanels[key].id, messages);
        }
    }
}

vPortal.sectionHandler.refreshMessageSection = function (newMessage) {
    for (var key in vPortal.allSectionPanels) {
        if (vPortal.allSectionPanels[key].sectionBeanId == "messageSection") {
            vPortal.sectionHandler.messageTemplete.appendMessage(vPortal.allSectionPanels[key].id, newMessage);
        }
    }
}

/**
 * TODO：该方法由表单组迁移
 * 参考自老的sectionjs，方法迁移，方法内部作了修改
 * @author yinr 业务配置栏目
 */
/** 栏目挂接相关JS方法 Start **/
var showDiv = function (innerHtmlContent, bizConfigId, tar) {
    var sb = new StringBuffer();
    var a = innerHtmlContent.split("|");
    var len = a.length;
    for (var i = 0; i < len; i++) {
        var idAndSubject = a[i];
        if (a[i] == '' || a[i].length == 0) {
            continue;
        }
        var idAndSubject = a[i].split(",");
        var url = "/collaboration/collaboration.do?method=newColl&templateId=" + idAndSubject[1];
        if (idAndSubject[0] == "false") {
            url = "javascript:$.alert(\\'" + $.i18n('bizconfig.use.authorize.template') + "\\')";
        }
        var title = escapeStringToHTML(idAndSubject[2]);
        if (title.length > 10) {
            title = title.substring(0, 10) + "...";
        }
        sb.append("<div class=\"link_box clearfix\"><a onclick=\"_openDataLink({'url':'" + url + "','obj':this,'openType':'3','sectionBeanId':''});\" title=\"" + escapeStringToHTML(idAndSubject[2]) + "\">" + title + "</a></div>");
    }
    var postionX = parseInt(tar.getBoundingClientRect().left) - 160;
    var postionY = parseInt(tar.getBoundingClientRect().top);
    var oDiv = document.getElementById('showPositionDiv');
    oDiv.style.position = "fixed";
    oDiv.style.top = postionY + "px";
    oDiv.style.left = postionX + "px";
    oDiv.innerHTML = sb.toString();
    showTemp();
};
var showTemp = function () {
    document.getElementById('showPositionDiv').style.display = "block";
};
var hideTemp = function () {
    document.getElementById('showPositionDiv').style.display = "none";
};
var showResult = function (type, formId, bizConfigId, queryOrReportName) {
    var openUrl = "/report/queryReport.do?method=goIndexRight&type=query&fromPortal=true&reportId=" + formId + "&reportName=";
    if ("query" == type) {
        openUrl = "/form/queryResult.do?method=queryExc&fromPortal=true&type=query&queryId=" + formId + "&queryName=";
    }
    openUrl += encodeURIComponent(queryOrReportName) + "&formid=" + formId + "&bizConfigId=" + bizConfigId;
    if (formId.trim() == '' || formId.trim().length == 0 || queryOrReportName.trim() == '' || queryOrReportName.trim().length == 0) {
        alert($.i18n("MainLang.no_right_to_use_query_or_stat_temp"));
        getCtpTop().reFlesh();
        return false;
    }
    _openDataLink({
        "url": openUrl,
        "openType": "4"
    });
};
var openLink = function (url) {
    //此处url存在/seeyon前缀，需要截断处理
    _openDataLink({
        "url": url.slice(7, url.length),
        "openType": "2"
    });
};
/** 栏目挂接相关JS方法 End **/

//栏目中的点击显示人员卡片
var showMemberCard = function (memberId) {
    var _options = {
        "memberId": memberId
    };
    if (typeof (insertScriptP) == "undefined") {
        $.ajax({
            url: _ctxPath + "/common/js/ui/seeyon.ui.peopleCrad-debug.js",
            dataType: "script",
            cache: true,
            success: function () {
                insertScriptP();
                return PeopleCard(_options);
            }
        }, _options);
    } else {
        insertScriptP();
        return PeopleCard(_options);
    }
}

//栏目中的单列打开链接
var open_link = function (_url, _openType) {
    var _openUrl = "";
    if (_url.indexOf("/seeyon") == 0) {
        _openUrl = _url;
    } else {
        _openUrl = _ctxPath + _url;
    }

    switch (_openType) {
        case "0":
        //openWorkSpace 弹出 满工作区
        case "1":
        //openWorkSpaceRight 弹出 只占用右边工作区
        case "2":
            //href 直接超链
            showMenu(_openUrl);
            break;
        case "3":
        //href_blank 直接超链，在新窗口打开
        case "4":
            //multiWindow 多窗口打开
            openCtpWindow({
                'url': _openUrl
            });
            break;
        default:
            showMenu(_openUrl);
            break;
    }
}

//栏目底部的“换一换”功能
var sectionRefreshAnotherData = function (_panelId, _pageNo) {
    renderEachPanel(_panelId, "fromRefreshAnotherData", {
        pageNo: _pageNo
    });
}

/**
 *
 * @param id 当前事件ID
 * @param shareType 共享类型
 * @param receiveMemberId 共享人员ID
 * @param isHasUpdate 是否具有修改权限
 * @return
 */
function openCalEvent(id, shareType, receiveMemberId, isHasUpdate, sectionID) {
    var res = new calEventManager().isHasDeleteByType(id, "event");
    var isReceiveMember = false;
    var receiveList = receiveMemberId.split(",");
    for (var i = 0; i < receiveList.length; i++) {
        receiveList[i] = receiveList[i].substring(7);
        if (receiveList[i] == getCtpTop().vPortal.CurrentUser.id) {
            isReceiveMember = true;
        }
    }
    if (res != null && res != "") {
        $.alert({
            'msg': res,
            ok_fn: function () {
                sectionHandler.reload(sectionID, true);
            }
        });
    } else {
        var height = 520;
        if (shareType == 1 && receiveMemberId == "null") {
            height = 500;
        }
        var dialogCalendarUpdate = $.dialog({
            url: _ctxPath + '/calendar/calEvent.do?method=editCalEvent&id=' + id,
            id: 'editCalEvent',
            width: 600,
            height: height,
            targetWindow: getCtpTop(),
            checkMax: true,
            transParams: {
                diaClose: function () {
                    dialogCalendarUpdate.close();
                },
                showButton: function () {
                    dialogCalendarUpdate.showBtn("sure");
                    dialogCalendarUpdate.hideBtn("update");
                },
                isview: "true",
                sectionID: sectionID
            },
            title: $.i18n('calendar.event.search.title'),
            buttons: [{
                id: "sure",
                text: $.i18n('common.button.ok.label'),
                handler: function () {
                    var rv = dialogCalendarUpdate.getReturnValue();
                    if (rv) {
                        setTimeout(function () {
                            vPortal.sectionHandler.reload(sectionID);
                        }, 500);
                    }
                }
            }, {
                id: "update",
                text: $.i18n('common.button.modify.label'),
                handler: function () {
                    dialogCalendarUpdate.getReturnValue("update");
                }
            }, {
                id: "cancel",
                text: $.i18n('common.button.cancel.label'),
                handler: function () {
                    dialogCalendarUpdate.close();
                }
            }, {
                id: "btnClose",
                text: $.i18n('calendar.close'),
                handler: function () {
                    dialogCalendarUpdate.close();
                }
            }]
        });
        dialogCalendarUpdate.hideBtn("sure");
        dialogCalendarUpdate.hideBtn("btnClose");
        dialogCalendarUpdate.hideBtn("update");
        dialogCalendarUpdate.hideBtn("cancel");
        if (isHasUpdate == "false" && (isReceiveMember == "false" || isReceiveMember == false)) {
            dialogCalendarUpdate.showBtn("btnClose");
        } else {
            dialogCalendarUpdate.showBtn("update");
            dialogCalendarUpdate.showBtn("cancel");
        }
    }
}

//有些特殊的无头栏目，如果无背景色时，需要将背景色设置为透明，去掉阴影，边框设为透明
var setSectionBgcTransparent = function (_entityId, _sst, _bgc) {
    var _sectionDom = document.getElementById("section_" + _entityId);
    if (_sst == "0" && (_bgc == "" || _bgc == "transparent" || _bgc.indexOf("rgba") === 0 && _bgc.split(",").length === 4 && _bgc.split(",")[3].trim() === "0)") && _sectionDom) {
        _sectionDom.style.backgroundColor = "transparent";
        _sectionDom.style.border = "solid 1px transparent";
        _sectionDom.style.boxShadow = "none";
    }
}

//通过创建dom的方式，获取文字的宽度
function getTextWidth(_text, _fontSize) {
    var tempDiv = document.getElementById('getTextWidthSpan');
    if (!tempDiv) {
        tempDiv = document.createElement("span");
        tempDiv.id = 'getTextWidthSpan';
        tempDiv.style.fontSize = _fontSize + "px";
        tempDiv.style.fontFamily = "'Ping Fang SC','Microsoft YaHei', Arial, Helvetica, sans-serif, 'SimSun'";
        tempDiv.style.visibility = "hidden";
        document.body.appendChild(tempDiv);
    } else {
        tempDiv.style.display = 'inline';
    }
    if (typeof tempDiv.textContent != "undefined") {
        tempDiv.textContent = _text;
    } else {
        tempDiv.innerText = _text;
    }
    var _textWidth = Number(tempDiv.offsetWidth) + 2;
    tempDiv.style.display = 'none';
    return _textWidth;
}

//hover上去出现滚动条的统一方法
var initHoverScrollbar = function (_obj) {
    var _thisDiv = _obj;
    if (window.addEventListener && _thisDiv) {
        _thisDiv.addEventListener("mouseover", function () {
            this.style.overflow = "auto";
        });
        _thisDiv.addEventListener("mouseout", function () {
            this.style.overflow = "hidden";
        });
    } else if (_thisDiv) {
        _thisDiv.attachEvent("mouseover", function () {
            this.style.overflow = "auto";
        });
        _thisDiv.attachEvent("mouseout", function () {
            this.style.overflow = "hidden";
        });
    }
}


/* 20180822由tpl-weatherTemplete.js挪过来
 *  因为sp2新增了“头像+磁贴+天气”的组合栏
 *  如果空间仅配置了“头像+磁贴+天气”的组合栏目，却未配置纯天气栏目时，不会请求tpl-weatherTemplete.js，故将代码过来公用
 */
vPortal.alreaLocation = 0;
vPortal.needLocationWeatherSection = [];
vPortal.sectionHandler.weatherTemplete = {
    afterInit: function (_data, _parameter, _extParameter) {
        document.getElementById("section_" + _data._entityId).style.border = "none";
        //this.getCity({'obj':$("#prov_"+_data._entityId),'_entityId':_data._entityId});
        //如果无背景色时，需要将背景色设置为透明，去掉阴影，边框设为透明
        setSectionBgcTransparent(_data._entityId, _data._sst, _data._bgc);
        this.showWeatherSection(_data, _parameter);
    },
    showWeatherSection: function (_data, _parameter) {
        if (_data.Data.propertyMap && _data.Data.propertyMap.city) {
            _data.Data.city = _data.Data.propertyMap.city;
        }
        if (_data.Data.propertyMap && _data.Data.propertyMap.prov) {
            _data.Data.prov = _data.Data.propertyMap.prov;
        }
        if ((null == _data.Data.city || "" == _data.Data.city) && (undefined == _parameter.cityName || _parameter.cityName == "") && (undefined != _data.Data.prov && "当前城市" == _data.Data.prov || null == _data.Data.prov)) { //没设置过城市的栏目
            if (vPortal.currentCity && vPortal.currentCity != "") { //登录的时候后台从缓存中取到的定位城市
                this.showCurrentCityWeather(_data);
            } else {
                //定位开始
                if (vPortal.canLocation == 'true') {
                    if (!vPortal.alreaLocation || vPortal.alreaLocation == 0) { //定位只定一次
                        $.getScript(vPortal.locationUrl + "&callback=vPortal.sectionHandler.weatherTemplete.setCurrentCity", function () {
                            vPortal.sectionHandler.weatherTemplete.showCurrentCityWeather(_data);
                        });
                        vPortal.alreaLocation = 1;
                    } else {
                        if (!vPortal.needLocationWeatherSection) {
                            vPortal.needLocationWeatherSection = [];
                        }
                        //不定位但是记录哪个天气栏目需要定位,等第一次定位完成后统一刷新需要定位的天气栏目
                        vPortal.needLocationWeatherSection.push(_data._entityId);
                    }
                } else { //定位关闭的时候,默认返回北京
                    vPortal.currentCity = "北京";
                    this.showCurrentCityWeather(_data);
                }
            }
        }
    },
    updateWeatherToDom: function (w, _data) {
        if (w) {
            if (!vPortal.needLocationWeatherSection) {
                vPortal.needLocationWeatherSection = [];
            }
            vPortal.needLocationWeatherSection.push(_data._entityId);
            for (var i = 0; i < vPortal.needLocationWeatherSection.length; i++) {
                if (document.getElementById("iconDiv_" + vPortal.needLocationWeatherSection[i])) {
                    document.getElementById("iconDiv_" + vPortal.needLocationWeatherSection[i]).setAttribute("title", w.cityName + ":" + w.weather); //设置div的title
                    document.getElementById("iconI_" + vPortal.needLocationWeatherSection[i]).className = "vportal vp-" + w.weatherIcon; //设置图标class
                    document.getElementById("temperature_" + vPortal.needLocationWeatherSection[i]).innerHTML = w.temperature; //设置温度b
                    document.getElementById("cityName_" + vPortal.needLocationWeatherSection[i]).innerHTML = w.cityName; //设置城市span
                    document.getElementById("weatherText_" + vPortal.needLocationWeatherSection[i]).innerHTML = w.weather; //设置天气情况span
                }
            }
        }
    },
    needUpdateCurrentCityWeather: function () { //是否需要更新vPortal里缓存的当前城市天气,设置2小时缓存
        var t = new Date().getTime();
        if (!vPortal.currentCityWeatherTime || (t - vPortal.currentCityWeatherTime) > 7200000) {
            return true;
        } else {
            return false;
        }
    },
    setCurrentCity: function (json) {
        if (!json || !json.city || json.city == '') {
            return;
        }
        if (json && json.city && (vPortal.currentCity == "" || !vPortal.currentCity)) {
            if (json.city.indexOf("市") == (json.city.length - 1)) {
                vPortal.currentCity = json.city.substring(0, (json.city.length - 1)); //高德的定位城市名称多个"市"
            } else {
                vPortal.currentCity = json.city;
            }
            /*if (vPortal && vPortal.portalSet && vPortal.portalSet.portalType == "3") {

            } else {
                callBackendMethod("weatherAreaInfoManager", "setCurrentCity", vPortal.currentCity, {
                    success: function(data) {
                        if (data == '__LOGOUT') {
                            offlineFun();
                            return;
                        }
                    }
                }); //异步把定位放到后台缓存
            }*/
        }
    },
    showCurrentCityWeather: function (_data) {
        if (vPortal.currentCityWeather && !this.needUpdateCurrentCityWeather()) {
            this.updateWeatherToDom(vPortal.currentCityWeather, _data);
            return vPortal.currentCityWeather;
        }
        //var weather = callBackendMethod("weatherAreaInfoManager", "getWeatherByCityName", vPortal.currentCity);
        var data = new Object();
        data.managerMethod = "getWeatherByCityName";
        if (!vPortal.currentCity) {
            vPortal.currentCity = "北京";
        }
        data.arguments = $.toJSON(vPortal.currentCity);
        var url = '/seeyon/ajax.do?method=ajaxAction&managerName=weatherAreaInfoManager'; //注意：为了etag，这个url不能加随机数
        jQuery.ajax({
            type: "GET",
            url: url,
            data: data,
            dataType: "json",
            beforeSend: CsrfGuard.beforeAjaxSend,
            async: true,
            success: function (weather) {
                vPortal.sectionHandler.weatherTemplete.updateWeatherToDom(weather, _data);
                vPortal.currentCityWeather = weather;
                vPortal.currentCityWeatherTime = new Date().getTime();
                //return vPortal.currentCityWeather;
            }
        });
    },
    getCity: function (_p) {
        var selectObj = _p.obj;
        var cityObj = $("#city_" + _p._entityId);
        var key = $(selectObj).val();
        if (key == '') {
            $(cityObj).empty();
            $(cityObj).append("<option value=''>当前城市</option>");
            return;
        }
        callBackendMethod("weatherAreaInfoManager", "getDistrictListByProvEn", key, {
            success: function (data) {
                if (data == '__LOGOUT') {
                    offlineFun();
                    return;
                }
                if (null != data) {
                    $(cityObj).empty();
                    $.each(data, function (key, value) {
                        if (value == vPortal.cityName) {
                            $(cityObj).append("<option selected value='" + key + "'>" + value + "</option>");
                        } else {
                            $(cityObj).append("<option value='" + key + "'>" + value + "</option>");
                        }
                    });
                }
            }
        });
    },
    saveCity: function (_panelId) {
        var _currentSectionPanel = vPortal.allSectionPanels[_panelId];
        var url = "/seeyon/portal/weatherController.do?method=updateProperty&entityId=" + _currentSectionPanel.entityId + "&x=" + _currentSectionPanel.x + "&y=" + _currentSectionPanel.y + "&spaceId=" + _currentSectionPanel.spaceId + "&width=" + _currentSectionPanel.sWidth;
        var city = $("#city_" + _currentSectionPanel.entityId).find("option:selected").text();
        var newData = {
            "cityName": city,
            "x": _currentSectionPanel.x,
            "y": _currentSectionPanel.y,
            "sectionBeanId": _currentSectionPanel.sectionBeanId,
            "entityId": _currentSectionPanel.entityId,
            "ordinal": _currentSectionPanel.ordinal,
            "spaceId": _currentSectionPanel.spaceId,
            "width": _currentSectionPanel.sWidth,
            "sectionWidth": _currentSectionPanel.sectionWidth,
            "bodyHeight": _currentSectionPanel.bodyHeight,
            "panelId": _currentSectionPanel.id
        };

        if (city == "") {
            this.toggleWeather(_currentSectionPanel.entityId);
            return;
        } else if (city == '当前城市') {
            newData.cityName = "";
            city = "";
        }
        var data = "{prov:'" + $("#prov_" + _currentSectionPanel.entityId).find("option:selected").text() + "',city:'" + city + "'}";
        //保存
        $.ajax({
            url: url,
            data: $.parseJSON(data),
            type: 'POST',
            dataType: 'json',
            success: function (data) {
                if (data.result != "true") {
                    var pagePath = data.pagePath;
                    var spaceId = data.spaceId;
                    //生成了个性化数据，需要更新栏目摘要并刷新空间
                    updateSpacePathAndId4Cache(spaceId, pagePath);
                    refreshCurrentSpace("notFreshMenuNav");
                } else {
                    var reloadWeatherSection = new getDataAndRenderSection(newData);
                }
            }
        }, newData);
    },
    toggleWeather: function (_entityId) {
        var weatherObj = document.getElementById("weather_" + _entityId);
        var weatherAreaObj = document.getElementById("weatherArea_" + _entityId);
        if (weatherObj.style.display == "none") {
            weatherObj.style.display = 'block';
            weatherAreaObj.style.display = 'none';
        } else {
            weatherObj.style.display = 'none';
            weatherAreaObj.style.display = 'block';
        }
        //获取省的select对象
        var sDom = document.getElementById("prov_" + _entityId);

        var l = sDom.options.length;
        if (l == 0) {
            //添加省的数据
            callBackendMethod("weatherAreaInfoManager", "getProvList", {
                success: function (data) {
                    if (data == '__LOGOUT') {
                        offlineFun();
                        return;
                    }
                    var provObj = $("#prov_" + _entityId);
                    $(provObj).empty();
                    $(provObj).append("<option value=''>当前城市</option>");
                    $.each(data, function (key, value) {
                        $(provObj).append("<option value='" + key + "'>" + value + "</option>");
                    });
                    //添加城市
                    var cityObj = $("#city_" + _entityId);
                    $(cityObj).empty();
                    $(cityObj).append("<option value=''>当前城市</option>");
                }
            });
        }
    }
}

function windowAtob(sectionName) {
    if (vPortal.isDesigner && typeof (previewVPortalSpace) != "undefined" && previewVPortalSpace == false) {
        return sectionName;  // 设计器中的假数据无需转换
    }
    var base = new Base64();
    var str = base.decode(sectionName);
    return str;
}

function Base64() {

    // private property
    var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    // public method for encoding
    this.encode = function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;
        input = _utf8_encode(input);
        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output +
                _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
                _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
        }
        return output;
    }

    // public method for decoding
    this.decode = function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
        while (i < input.length) {
            enc1 = _keyStr.indexOf(input.charAt(i++));
            enc2 = _keyStr.indexOf(input.charAt(i++));
            enc3 = _keyStr.indexOf(input.charAt(i++));
            enc4 = _keyStr.indexOf(input.charAt(i++));
            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;
            output = output + String.fromCharCode(chr1);
            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }
        output = _utf8_decode(output);
        return output;
    }

    // private method for UTF-8 encoding
    var _utf8_encode = function (string) {
        string = string.replace(/\r\n/g, "\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);
            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if ((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }
        return utftext;
    }

    // private method for UTF-8 decoding
    var _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = 0;
        var c1 = 0;
        var c2 = 0;
        var c3 = 0;
        while (i < utftext.length) {
            c = utftext.charCodeAt(i);
            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if ((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i + 1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i + 1);
                c3 = utftext.charCodeAt(i + 2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }
        return string;
    }
}


var avoidRepeatedClicks = function (_key) {
    //重复点击防护
    if (vPortal.urlCache[_key]) {
        // console.log("you click too fast !");
        return false;
    } else {
        vPortal.urlCache[_key] = "clicked";
        setTimeout(function () {
            //释放，允许再次点击
            delete vPortal.urlCache[_key];
        }, 1000);
        return true;
    }
}
