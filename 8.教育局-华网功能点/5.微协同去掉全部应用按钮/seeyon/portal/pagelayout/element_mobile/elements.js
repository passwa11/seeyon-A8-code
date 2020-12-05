//这块内容需要放default.js，后面迁移过去
//移动门户设计器 -- 初始化默认的热点值
var commonAppEntryBtnColor = "#FFFFFF";
var initDefaultSkinHotspots = function(){
    var _platPath = (typeof(_ctxPath) != "undefined" || typeof(cmp) == "undefined") ? "" : cmp.serverIp;
    if (vPortal && vPortal.themeHotspots && vPortal.themeHotspots.skin == undefined) {
        vPortal.themeHotspots.skin = {
            "cssPath":"",
            "mWrapperBgc":"transparent",//整体门户页面区 -背景色
            "mWrapperBgi":"none",//整体门户页面区 -背景图
            "mNavBarSc":"inherit",//导航区 -选中条颜色
            "mNavBarColor":"inherit",//导航区 -文字颜色
            "mNavBarBgc":"transparent",//导航区 -背景颜色
            "mColumnAreaBgc":"transparent",//栏目内容区 -背景色
            "mColumnAreaBgi":"none",//栏目内容区 -背景图
            "mHeaderColor": "inherit",//头部区 -文字颜色
            "mHeaderBgc": "transparent",//头部区 -背景色
        };
    }else{
        vPortal.themeHotspots.skin.mWrapperBgc = vPortal.themeHotspots.skin.mWrapperBgc === undefined ? "transparent" : vPortal.themeHotspots.skin.mWrapperBgc;
        vPortal.themeHotspots.skin.mWrapperBgi = (vPortal.themeHotspots.skin.mWrapperBgi === '' || vPortal.themeHotspots.skin.mWrapperBgi === undefined) ? "none" : _platPath + vPortal.themeHotspots.skin.mWrapperBgi;
        vPortal.themeHotspots.skin.mNavBarSc = vPortal.themeHotspots.skin.mNavBarSc === undefined ? "inherit" : vPortal.themeHotspots.skin.mNavBarSc;
        vPortal.themeHotspots.skin.mNavBarColor = vPortal.themeHotspots.skin.mNavBarColor === undefined ? "inherit" : vPortal.themeHotspots.skin.mNavBarColor;
        vPortal.themeHotspots.skin.mNavBarBgc = vPortal.themeHotspots.skin.mNavBarBgc === undefined ? "transparent" : vPortal.themeHotspots.skin.mNavBarBgc;
        vPortal.themeHotspots.skin.mColumnAreaBgc = vPortal.themeHotspots.skin.mColumnAreaBgc === undefined ? "transparent" : vPortal.themeHotspots.skin.mColumnAreaBgc;
        vPortal.themeHotspots.skin.mColumnAreaBgi = (vPortal.themeHotspots.skin.mColumnAreaBgi === undefined || vPortal.themeHotspots.skin.mColumnAreaBgi === '') ? "none" : _platPath + vPortal.themeHotspots.skin.mColumnAreaBgi;
        vPortal.themeHotspots.skin.mHeaderColor = vPortal.themeHotspots.skin.mHeaderColor === undefined ? "inherit" : vPortal.themeHotspots.skin.mHeaderColor;
        vPortal.themeHotspots.skin.mHeaderBgc = vPortal.themeHotspots.skin.mHeaderBgc === undefined ? "transparent" : vPortal.themeHotspots.skin.mHeaderBgc;
    }
    if(!vPortal.isDesigner && cmp&& cmp.theme && (cmp.theme.getTheme() === 'black')) {
        vPortal.themeHotspots.skin.mWrapperBgc = '#000';
        vPortal.themeHotspots.skin.mNavBarSc = 'inherit';
        vPortal.themeHotspots.skin.mNavBarColor ='inherit';
        vPortal.themeHotspots.skin.mNavBarBgc = 'transparent';
        vPortal.themeHotspots.skin.mColumnAreaBgc = 'transparent';
        vPortal.themeHotspots.skin.mColumnAreaBgi = 'none';
        vPortal.themeHotspots.skin.mHeaderColor = '#ffffff';
        vPortal.themeHotspots.skin.mHeaderBgc = 'transparent';
    }
}



//热点渲染 移动端+设计器
var renderLayoutHotspot = function(){
    if ( vPortal.themeHotspots && vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.mWrapperBgc && vPortal.themeHotspots.skin.mWrapperBgc !="none" ) {
        document.getElementById("wrapper").style.backgroundColor = vPortal.themeHotspots.skin.mWrapperBgc;
    }
    if (vPortal.themeHotspots && vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.mWrapperBgi && vPortal.themeHotspots.skin.mWrapperBgi !="none" ) {
        document.getElementById("wrapper").style.backgroundImage = "url("+vPortal.themeHotspots.skin.mWrapperBgi+")";
    }
    if (vPortal.themeHotspots && vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.mColumnAreaBgc) {
        document.getElementById("columnArea").style.backgroundColor = vPortal.themeHotspots.skin.mColumnAreaBgc;
        //设计器里面是main
        document.getElementById("main").style.backgroundColor = vPortal.themeHotspots.skin.mColumnAreaBgc;
    }
    if (vPortal.themeHotspots && vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.mColumnAreaBgi && vPortal.themeHotspots.skin.mColumnAreaBgi !="none") {
        document.getElementById("columnArea").style.backgroundImage = "url("+vPortal.themeHotspots.skin.mColumnAreaBgi+")";
        if(typeof(isMobileDesigner) != "undefined" && isMobileDesigner){
            //设计器里面是main
            document.getElementById("main").style.backgroundImage = "url("+vPortal.themeHotspots.skin.mColumnAreaBgi+")";
        }
    }
    if (vPortal.themeHotspots && vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.scrollBgi && vPortal.themeHotspots.skin.scrollBgi ==="1" && vPortal.themeHotspots.skin.mWrapperBgi !="none") {
        //移动端才需要下面的设置
        if(typeof(isMobileDesigner) != "undefined" && isMobileDesigner === false){
            document.getElementById("scroller").style.backgroundColor = vPortal.themeHotspots.skin.mWrapperBgc;
            document.getElementById("main").style.backgroundColor = vPortal.themeHotspots.skin.mWrapperBgc;
            //00005 、00008 、 00011 模板
            if(typeof(backgroundSizeH) != "undefined"){
                //兼容老数据，没有该热点值，就取js里面定义的
                if(typeof(vPortal.themeHotspots.skin.mWrapperBgiHeight) === "undefined"){
                    vPortal.themeHotspots.skin.mWrapperBgiHeight = backgroundSizeH - 88;
                }
                backgroundSizeH = Number(vPortal.themeHotspots.skin.mWrapperBgiHeight) + 88;
                var _sizeValue = 'auto '+(Number(vPortal.themeHotspots.skin.mWrapperBgiHeight)+88)+'px';
                document.getElementById("main").style.backgroundSize = _sizeValue;
                document.getElementById("main").style.height = backgroundSizeH - 88 + "px";

                //微协同#wrapper不要背景图
                if(!cmp.platform.CMPShell){
                    document.getElementById("wrapper").style.backgroundImage = "none";
                }else{
                    document.getElementById("wrapper").style.backgroundSize = _sizeValue;
                }
            }else{
                cmp.notification.alert("scroll bgc must set height！");
            }
            document.getElementById("main").style.backgroundImage = "url("+vPortal.themeHotspots.skin.mWrapperBgi+")";
        }else{
            if(typeof(backgroundSizeH) != "undefined"){
                if(vPortal.themeHotspots.skin.mWrapperBgiHeight){
                    var _sizeValue = 'auto '+(Number(vPortal.themeHotspots.skin.mWrapperBgiHeight)+88)+'px';
                    document.getElementById("wrapper").style.backgroundSize = _sizeValue;
                }
            }
        }
    }else{
        if (vPortal.themeHotspots && vPortal.themeHotspots.skin && vPortal.themeHotspots.skin.mWrapperBgi && vPortal.themeHotspots.skin.mWrapperBgi !="none" ) {
            document.getElementById("wrapper").style.backgroundImage = "url("+vPortal.themeHotspots.skin.mWrapperBgi+")";
        }
    }
    //规则如下
    //当文字颜色 = 白色时，图标颜色为 #ffffff
    //当文字颜色 ≠ 白色时，图标颜色为#2C2C2C
    //移动端才需要下面的设置   底导航的时候才去做这个处理
    if(typeof(isMobileDesigner) != "undefined" && isMobileDesigner === false && typeof (Urlparam) !== "undefined" && Urlparam.m3from === 'navbar'){
        if(vPortal.themeHotspots.skin.mHeaderColor.toUpperCase() == "#FFFFFF"){
            cmp.api.setCommonAppEntryBtnColor('#FFFFFF');
            commonAppEntryBtnColor = "#FFFFFF";
        }else{
            cmp.api.setCommonAppEntryBtnColor('#2C2C2C');
            commonAppEntryBtnColor = "#2C2C2C";
        }
    }
}

//跳转到当前门户下的空间
var entrySpaceForCurrentPortal = function(_spaceId){
    var _selectSpace = vPortal.spacesSummary[_spaceId];
    if (_selectSpace) {
        initSection(_selectSpace.spacePath);
    }
}

//高亮导航上对应的空间
var highlightCurrentSpaceForNav = function(_spaceId){
    //上导航 、中导航 、 中导航的假导航 都需要定位到当前空间
    var navDomIdArr = ['#topNavListBar','#navListBar','#portalFalseNavBar','#portalFooterArea'];
    for(var i =0; i<navDomIdArr.length;i++){
        highlightCurrentSpace(navDomIdArr[i],_spaceId);
    }
}


var highlightCurrentSpace = function(navDomId,_spaceId){
    if(iwWxtMainPortal && navDomId == '#portalFooterArea'){
        return;
    }
    var dataUrlStr = "spaceId|"+_spaceId;
    var propItemDom = document.querySelectorAll(navDomId + " .propItem");
    var hasMarked = false;
    for (var i =0; i<propItemDom.length;i++) {
        //只高亮第一个
        if(propItemDom[i].getAttribute("data-url") === dataUrlStr && !hasMarked){
            hasMarked = true;
            propItemDom[i].classList.add("currentNav");
        }else{
            propItemDom[i].classList.remove("currentNav");
        }
    }
}

//进入上中导航自定义界面
var setTopOrMiddleNavSort = function(_obj){
    _obj.addEventListener("tap",function () {
        var options = {};
        if (cmp.platform.CMPShell) {
            options.openWebViewCatch = 1;

        }
        //传入导航类型，供自定义后回来刷新对应导航使用
        var _navType = _obj.getAttribute("navType");
        //这里为了空间栏目个性化页面获取数据
        localStorage.setItem("currentPortalId", currentPortalId);
        cmp.href.next(_portalPath + "/html/topOrMiddleNavSort.html?cmp_orientation=auto&datetime=" + new Date().getTime() + "&navType="+_navType, null, options);
    });
}


/*
 * 底部
 */
//是不是微协同入口的主门户
if(typeof(GetRequest) != "undefined" && GetRequest().wxtmainportal == "true" ){
    var iwWxtMainPortal = true;
}else{
    var iwWxtMainPortal = false;
}
//微协同底导航模拟数据
if (typeof(isMobileDesigner) === "undefined" || isMobileDesigner === false && iwWxtMainPortal) {

     //G6  url区隔
     if(cmp.api.getProduction().suffix != undefined){
        var productionSuffix = cmp.api.getProduction().suffix;
     }else{
        var productionSuffix = '';
     }
     //微协同主门户的底导航数据;
    var wxtMainPortalNavData =  {
            "mBottomNav" :
            [
                {
                    "id": "00000001",
                    "navName": cmp.i18n("portal.bottom.todo"),
                    "navType": "application",
                    "url": _v5Path + "/m3/apps/m3/todo/layout/todo-list"+productionSuffix+".html?weixinFrom=home",
                    "icon": "iconfont icon-backlog",
                },
                {
                    "id": "00000002",
                    "navName": cmp.i18n("portal.bottom.portal_index"),
                    "navType": "index",
                    "url": _v5Path + "/m3/apps/v5/portal/html/portalIndex.html?weixinFrom=home&wxtmainportal=true",
                    "icon": "iconfont icon-information-portal-fill",
                },
                // {
                //     "id": "00000003",
                //     "navName": cmp.i18n("portal.bottom.all_apps"),
                //     "navType": "application",
                //     "url": _v5Path + "/H5/wechat/html/allApps.html?weixinFrom=home",//全部应用
                //     "icon": "iconfont icon-wechat-m3-app",
                // },
                {
                    "id": "00000004",
                    "navName": cmp.i18n("portal.bottom.personal_center"),//个人中心
                    "navType": "application",
                    "url": _v5Path + "/H5/wechat/html/userCenter.html?weixinFrom=home",
                    "icon": "iconfont icon-personal-center",
                }
            ]
    };
}

vPortalMainFrameElements.portalFooter = {
    config: isMobileDesigner&&parent.themeJson&&parent.themeJson.from=="0"?false:true,//样式库不允许配置
    itemsLength: 0,
    getHeight:function(){
        if( (typeof (Urlparam) != "undefined" && Urlparam && Urlparam.m3from === 'navbar') || (!iwWxtMainPortal && vPortal.themeHotspots && vPortal.themeHotspots.base && vPortal.themeHotspots.base.showFooterNav == "0") || (!iwWxtMainPortal && typeof(vPortal.themeHotspots.base&&vPortal.themeHotspots.base.showFooterNav) == "undefined") || this.itemsLength == 1 || workbench == "workbench" || (typeof(currentPlatform) !== "undefined" && ( currentPlatform === "vjoin"  || GetRequest().weixinFrom =="app" ) ) ){
            return 0;
        }else{
            return 50;
        }
    },
    getData: function() {
        //M3工作台的话不显示门户底导航
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return false;
        //M3底导航的话不显示门户底导航,但是底导航进来，无M3底导航的时候，要显示门户的底部
        if ( typeof (Urlparam) != "undefined" && Urlparam && Urlparam.m3from === 'navbar' && Urlparam.hasFooterNavBar!= false ) return false;
        if (typeof(isMobileDesigner) !== "undefined" && isMobileDesigner) {
            return {
                "itemList": [
                    {
                        "navName": $.i18n('vportal.designer.mobile.nav1'),
                        "icon": "vp-association-application-settings",
                    }, {
                        "navName": $.i18n('vportal.designer.mobile.nav2'),
                        "icon": "vp-count"
                    }, {
                        "id":"maddNav",
                        "navName": $.i18n('vportal.designer.mobile.nav3'),
                        "icon": "vp-btn-add"
                    }, {
                        "navName": $.i18n('vportal.designer.mobile.nav4'),
                        "icon": "vp-XXApp"
                    }, {
                        "navName": $.i18n('vportal.designer.mobile.nav5'),
                        "icon": "vp-person-template"
                    }
                ]
            }
        }else{
            return false;
        }
    },
    afterInit: function(_elementId, thisElement, tplData) {
        if (typeof(isMobileDesigner) === "undefined" || isMobileDesigner) {
            return;
        }
        //M3工作台的话不显示门户底导航
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return;
        //M3底导航的话不显示门户底导航
        if ( typeof (Urlparam) != "undefined" && Urlparam && Urlparam.m3from === 'navbar' && Urlparam.hasFooterNavBar!= false ) return;
        //如果是微协同入口的主门户  是固定的导航，这个数据是写死的
        if( iwWxtMainPortal ){
            renderTpl(wxtMainPortalNavData, tplData, thisElement.id);
            this.loadNavJsEvent("portalFooterArea");
            return;//不需要再去请求真实的导航了
        }
        //设置了不显示底导航的时候，不加载底导航   ||  未设置热点的时候  默认不显示
        if(vPortal.themeHotspots.base&&vPortal.themeHotspots.base.showFooterNav=="0" || typeof(vPortal.themeHotspots.base&&vPortal.themeHotspots.base.showFooterNav) == "undefined"){
            return;
        }

        //导航数据已缓存的时候，不必发rest请求
        if( !isEmptyObject(vPortal.portalNavCache) ){
            if (vPortal.portalNavCache.mBottomNav && vPortal.portalNavCache.mBottomNav.length > 1) {//只有一个的时候不渲染
                //渲染底导航
                renderTpl(vPortal.portalNavCache, tplData, thisElement.id);
                this.afterInitSuccessFun(vPortal.portalNavCache);
            }else{//接口返回也有可能是0，也不显示
                //只有一个导航的时候，不渲染导航列表   且刷新高度
                vPortalMainFrameElements.portalFooter.itemsLength = 1;
                refreshWrapperHeight();
            }
            return;
        }

        //移动端才会调用接口获取真实数据
        var CMP_V5_TOKEN = window.localStorage.CMP_V5_TOKEN;
        var _url = _v5Path + "/rest/mobilePortal/getPortalNav/" + currentPortalId;
        var _this = this;
        var _getSpaceAjax = new newAjax({
            type: "GET",
            url: _url,
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
                'Accept-Language': "zh-CN",
                "token": CMP_V5_TOKEN || ""
            },
            dataType: "json",
            thisElement: thisElement,
            tplData: tplData,
            _this: _this,
            success: function(result, ret) {
                if (result.code == "200" && result.data && result.data.mBottomNav && result.data.mBottomNav.length > 1) {//只有一个的时候不渲染
                    vPortal.portalNavCache = result.data;
                    //渲染底导航
                    renderTpl(result.data, ret.tplData, ret.thisElement.id);
                    _this.afterInitSuccessFun(result.data);
                // }else if (result.data.mBottomNav.length == 1){
                }else{//接口返回也有可能是0，也不显示
                    vPortal.portalNavCache = result.data;
                    //只有一个导航的时候，不渲染导航列表   且刷新高度
                    vPortalMainFrameElements.portalFooter.itemsLength = 1;
                    refreshWrapperHeight();
                }
            },
            error: function(error, ret) {}
        });
    },//底导航渲染后的各种事件处理
    afterInitSuccessFun:function(navData){
        //高亮底导航
        highlightCurrentSpace('#portalFooterArea',currentSpaceId);
        // this.bindItemEvent(navData);
        this.loadNavJsEvent("portalFooterArea");
        var _this = this;
        if(navData.mAddNav && navData.mAddNav.length > 0){
            document.getElementById("mBottomNavPropbtn").addEventListener("tap",function () {
                _this.toggleBottomNavProp();
            });
            _this.addPropTobody(navData);
        }
    },
    //添加浮层，以及浮层里面的导航
    addPropTobody:function(navData){
        var domElement=document.createElement("div");
            domElement.id = "mBottomNavProp";
            domElement.className = "mBottomNavProp";
            domElement.style.display = "none";
            //一行最多3个，计算一下需要几行
            var div_wrapRowL = Math.ceil(navData.mAddNav.length/3);
            //一页最多3行，计算一下需要几页
            var div_pageL = Math.ceil(navData.mAddNav.length/9);

            var propwarpTop = div_wrapRowL *50;
            var domstr = '<div class="propwarp" style="margin-top:-'+propwarpTop+'px;">';
            domstr +='<div class="div_wrap">';
            if(div_pageL >1){

                var mAddNavGroups = [];
                for (var j = 0; j < div_pageL; j++) {
                    var _arrayGroupsSlice = navData.mAddNav.slice(j*9,(j+1)*9);
                    mAddNavGroups.push(_arrayGroupsSlice);
                }

                //超过9个的使用slider分页
                domstr +='<div class="cmp-slider footerNav_slider_prop">';
                    domstr +='<div class="cmp-slider-group" style="height:380px">';

                        //最后一组
                        var lastPage = mAddNavGroups[mAddNavGroups.length-1];
                        domstr +='<div class="cmp-slider-item cmp-slider-item-duplicate cmp-hidden">';
                        domstr +='<div class="div_wrap">';
                        for(a=0;a<lastPage.length;a++){
                            domstr +='<div class="propItem" portletkey="'+lastPage[a].allIds+'" data-url="'+escapeStringToHTML(lastPage[a].url)+'">';
                            if(lastPage[a].icon.indexOf("fileUpload.do")>-1){
                                domstr +='<img src="'+_v5Path + lastPage[a].icon+'" width="70" height="70" />';
                            }else{
                                domstr +='<span class="iconArea vportal '+lastPage[a].icon+'"></span>';
                            }
                            domstr +='<span class="name">'+lastPage[a].navName+'</span></div>';
                        }
                        domstr +='</div>';
                        domstr +='</div>';

                        for(b=0;b<mAddNavGroups.length;b++){
                            var thisGroup = mAddNavGroups[b];
                            domstr +='<div class="cmp-slider-item">';
                                domstr +='<div class="div_wrap">';
                                for(c=0;c<thisGroup.length;c++){
                                    domstr +='<div class="propItem" portletkey="'+thisGroup[c].allIds+'" data-url="'+escapeStringToHTML(thisGroup[c].url)+'">';
                                    if(thisGroup[c].icon.indexOf("fileUpload.do")>-1){
                                        domstr +='<img src="'+_v5Path + thisGroup[c].icon+'" width="70" height="70" />';
                                    }else{
                                        domstr +='<span class="iconArea vportal '+thisGroup[c].icon+'"></span>';
                                    }
                                    domstr +='<span class="name">'+thisGroup[c].navName+'</span></div>';
                                }
                                domstr +='</div>';
                            domstr +='</div>';
                        }

                        //第一组
                        var fristPage = mAddNavGroups[0];
                        domstr +='<div class="cmp-slider-item cmp-slider-item-duplicate cmp-hidden">';
                        domstr +='<div class="div_wrap">';
                        for(d=0;d<fristPage.length;d++){
                            domstr +='<div class="propItem"  portletkey="'+fristPage[d].allIds+'" data-url="'+escapeStringToHTML(fristPage[d].url)+'">';
                            if(fristPage[d].icon.indexOf("fileUpload.do")>-1){
                                domstr +='<img src="'+_v5Path + fristPage[d].icon+'" width="70" height="70" />';
                            }else{
                                domstr +='<span class="iconArea vportal '+fristPage[d].icon+'"></span>';
                            }
                            domstr +='<span class="name">'+fristPage[d].navName+'</span></div>';
                        }
                        domstr +='</div>';
                        domstr +='</div>';
                    domstr +='</div>';

                    domstr +='<div class="cmp-slider-indicator">';
                    for(e=0;e<mAddNavGroups.length;e++){
                        domstr +='<div class="cmp-indicator"></div>';
                    }
                    domstr +='</div>';
                domstr +='</div>';
            }else{
                //不超过9个不分页
                for(i=0;i<navData.mAddNav.length;i++){
                    domstr +='<div class="propItem" portletkey="'+navData.mAddNav[i].allIds+'" data-url="'+escapeStringToHTML(navData.mAddNav[i].url)+'">';
                    if(navData.mAddNav[i].icon.indexOf("fileUpload.do")>-1){
                        domstr +='<img src="'+_v5Path + navData.mAddNav[i].icon+'" width="70" height="70" />';
                    }else{
                        domstr +='<span class="iconArea vportal '+navData.mAddNav[i].icon+'"></span>';
                    }
                    domstr +='<span class="name">'+navData.mAddNav[i].navName+'</span></div>';
                }
            }
            domstr +='</div>';
            domstr +='</div><div class="close"><span class="see-icon-v5-common-close"></span></div>';

            domElement.innerHTML = domstr;
            document.body.appendChild(domElement);
            //大于一页的时候  初始化slider  翻页
            if(div_pageL >1){
                var _propSlider = cmp(".footerNav_slider_prop").slider({
                    slideshowDelay: 0
                });
            }

            this.loadNavJsEvent("mBottomNavProp");
            var _this = this;
            cmp("#mBottomNavProp").on("tap", ".close", function() {
                _this.toggleBottomNavProp();
            })
            // debugger;

    },//加载导航点击所需的js
    loadNavJsEvent:function(objId){
        var _this = this;
        _this.mBottomNavBindLinkUrl(objId);
    },
    //绑定导航的点击跳转事件
    mBottomNavBindLinkUrl:function(objId){
        var _linkDoms = document.getElementById(objId).querySelectorAll(".propItem");
        if (!_linkDoms) {
            return;
        }
        for (var i = 0; i < _linkDoms.length; i++) {
            (function(i, _this) {
                var url = _linkDoms[i].getAttribute("data-url");
                var portletkey = _linkDoms[i].getAttribute("portletkey");
                cmp.event.click(_linkDoms[i], function() {
                    shortCutEvent(url,objId,portletkey);
                });
            })(i, this);
        }
    },//显示 || 隐藏 加号的浮层
    toggleBottomNavProp:function(){
        var propDom = document.getElementById("mBottomNavProp");
        if(propDom.style.display == "none"){
            document.getElementById("portalBody").classList.add("blur");
            propDom.style.display = "block";
            myScroll.disable();
        }else{
            document.getElementById("portalBody").classList.remove("blur")
            propDom.style.display = "none";
            myScroll.enable();
        }
    },//设计器里面获取属性
    getProp: function(){
        var json = [{
            "groupName": "",
            "groupType": "mPortalFooterNavContent",
            "groupValue": [{
                "id": "mPortalFooterNavContent",
                "name": "mPortalFooterNavContent",
                "type": "underLine",
                "typeShow": "default",
                "label": $.i18n('portal.hotspot.nav.setlink.label'),
                "click": ""
            }]
        }];
        return json;
    }
}

/*
 * 顶部
 */

vPortalMainFrameElements.portalTop = {
    config : false,
    getStatusBarHeight:function(){
        if(cmp.os.isIphonex){
            return 44;
        }else if(cmp.os.iPhone || cmp.os.iPad ){
            return 20;
        }else if(cmp.os.android){
            return 0;
        }
    },
    getHeight: function(){
        if (typeof (Urlparam) !== "undefined" && (Urlparam.m3from === 'workbenchNav' || Urlparam.m3from === 'navbar') ){
            //默认高度  安卓无需考虑状态栏
            //iPhone和ipad的状态栏是20
            //iPhoneX是44
            var defaultHeight = 44;
            if(cmp.platform.CMPShell){
                //底导航的时候，高度为60px
                if (Urlparam.m3from === 'navbar'){
                    defaultHeight = 60;
                }
                defaultHeight = defaultHeight + this.getStatusBarHeight();
                //顶到头
                if (typeof (fixHeaderPos) != "undefined" && fixHeaderPos === true) {
                    defaultHeight = 0;
                }
            }
            return defaultHeight;
        }else{
            return 0;
        }
    },
    getData : function() {
        if (typeof (isMobileDesigner) !== "undefined" && isMobileDesigner) {
            //设计器里面有欢迎语的时候就不要头部了
            if(document.getElementById("portalheader4welcomes") ) return false;
            return {
                "portalName" : top.portalName || $.i18n("vportal.hotspot.skin.portalName"),
                "mHeaderColor" : vPortal.themeHotspots.skin.mHeaderColor,
                "mHeaderBgc" : vPortal.themeHotspots.skin.mHeaderBgc
            };
        }else if (typeof (Urlparam) !== "undefined" && (Urlparam.m3from === 'workbenchNav' || Urlparam.m3from === 'navbar') ){//M3工作台打开子门户.底导航打开门户的时候需要头部
            //默认高度  安卓无需考虑状态栏
            //iPhone和ipad的状态栏是20
            //iPhoneX是44
            var defaultHeight = 44;
            //底导航的时候，高度为60px
            if (Urlparam.m3from === 'navbar'){
                defaultHeight = 60;
            }
            var defaultPos = "static";
            if (typeof (fixHeaderPos) != "undefined" && fixHeaderPos === true) {
                defaultPos = "fixed";
            }
            var defaultPaddingTop = 0;
            if(cmp.platform.CMPShell){
                defaultHeight = defaultHeight + this.getStatusBarHeight();
                defaultPaddingTop = defaultPaddingTop + this.getStatusBarHeight();
            }
            var defaultPaddingLeft = "80px";
            if(Urlparam.m3from === 'navbar'){
                defaultPaddingLeft = "20px";
                defaultPaddingTop = defaultPaddingTop;
            }
            //背景图偏移一下，假装接起来
            if(vPortal.themeHotspots.skin.scrollBgi && vPortal.themeHotspots.skin.scrollBgi ==="1"){
                document.getElementById("main").style.backgroundPositionY = "-"+defaultHeight+"px";
            }

            return {
                "portalName" : vPortal.portalName,
                "mHeaderColor" : vPortal.themeHotspots.skin.mHeaderColor,
                "mHeaderBgc" : vPortal.themeHotspots.skin.mHeaderBgc,
                "mHeaderHeight" : defaultHeight,
                "mHeaderPaddingTop" : defaultPaddingTop,
                "mHeaderPaddingLeft" : defaultPaddingLeft,
                "isFix":defaultPos
            };
        } else {
            return false;
        }

    },
    changeAppTheme : function(currentThemeHotspot,actionType){
        if(cmp.platform.CMPShell && typeof (layoutStatusBarStyle) !== "undefined"){

            //移动端设计器里面的的这俩色值cmp那边需要修改状态栏，只支持16进制  且预制值只能为十六进制   类似#ffffff

            var _currentThemeHotspot = new Object();
            if(currentThemeHotspot.mHeaderBgc != "" && typeof(currentThemeHotspot.mHeaderBgc)!="undefined" ) {
                //如果是transparent強制转为00FFFFFF，要不然app会崩溃
                _currentThemeHotspot.bgColor = currentThemeHotspot.mHeaderBgc=="transparent"?"#00FFFFFF":currentThemeHotspot.mHeaderBgc
            }
            if(currentThemeHotspot.mHeaderColor != "" && typeof(currentThemeHotspot.mHeaderColor)!="undefined"){
                _currentThemeHotspot.color = currentThemeHotspot.mHeaderColor;
            }
            //传的有就取传入的，否则去layout_0x.js对应里面变量
            if( typeof(currentThemeHotspot.statusBarStyle) !="undefined" ){
                _currentThemeHotspot.statusBarStyle = currentThemeHotspot.statusBarStyle;
            }else{
                _currentThemeHotspot.statusBarStyle = layoutStatusBarStyle;
            }
            //如果来自恢复默认按钮的时候，需要还原，不能return
            if(actionType && actionType == "toDefault"){
                //安卓通不到顶部的时候，文字要传黑色
                if(cmp.os.android && _currentThemeHotspot.bgColor == "#00FFFFFF"){
                    _currentThemeHotspot.statusBarStyle = 0;
                    _currentThemeHotspot.bgColor = "#fefefe";
                }
            }else if(cmp.os.android && _currentThemeHotspot.bgColor == "#00FFFFFF"){//安卓切头部色透明的情况，不通上顶部
                return;
            }
            //安卓切头部色透明的情况，不通上顶部
            // if(cmp.os.android && _currentThemeHotspot.bgColor == "#00FFFFFF") return;
            //工作台的时候调用cmp的方法
            /*if(workbench == "workbench"){
                top.cmp.event.trigger("com.seeyon.m3.backgroundChange",top.document,_currentThemeHotspot);
            }else{*/
                //新开页面、 底导航的时候直接调用修改状态栏样式的方法即可
                cmp.app.setStatusBar({
                    statusBarStyle:_currentThemeHotspot.statusBarStyle,
                    bgColor:_currentThemeHotspot.bgColor
                });
            /*}*/
        }
    },
    afterInit : function(_elementId, thisElement, tplData) {
        if (typeof (isMobileDesigner) === "undefined" || isMobileDesigner) {
            return;
        }
        //元素渲染之后赋值给全局变量
        topNavPortalName = document.getElementById("topNavPortalName");
        //渲染主题颜色
        this.changeAppTheme(vPortal.themeHotspots.skin);

        if (typeof (currentPlatform) !== "undefined" && ((currentPlatform == "m3" && workbench == "workbench") || currentPlatform == "xcx" )) {
            //如果在门户模板里面定义了selfinitPage，就走自己的模板定义的initPageAsync，这里自己return
            if(typeof(currentPlatform) !== "undefined" && currentPlatform == "xcx" && typeof (selfinitPage) != "undefined" && selfinitPage ) return;
            // vjoin 以及 M3 从工作台进来 小程序  不渲染头部
            initPageAsync();
            return;
        }
        //报表空间 不渲染头部的门户list,但是要把门户名字渲染出来
        if (typeof (Urlparam) !== "undefined" && Urlparam.m3from === 'vreport'){
            document.title = Urlparam.portalName;
            initPageAsync();
            return;
        }else{
            if(typeof vPortal.portalName != "undefined"){
                document.title = vPortal.portalName;
            }
            initPageAsync();
        }
        //不显示头部的端背景图偏移一下，确保图片结束位置一致
        if(!cmp.platform.CMPShell && vPortal.themeHotspots.skin.scrollBgi && vPortal.themeHotspots.skin.scrollBgi ==="1"){
            var _defaultHeight = 88;
            document.getElementById("main").style.backgroundPositionY = "-"+_defaultHeight+"px";
        }

    },
    // 设计器中获取可设置属性
    getProp : function(dataJson) {
    },
    onPropChange : function(json, id, key, value) {
    }
}

/*
 * 空间列表、切换空间
 */
/** 标记--  待删除 **/
vPortalMainFrameElements.spaceBar = {
    config: true,
    spacesSummary: {},
    spaces: [],
    getData: function() {
        if (typeof(isMobileDesigner) !== "undefined" && isMobileDesigner) {
            // 设计器假数据
            var _mNavBarColor = vPortal.themeHotspots.skin.mNavBarColor;
            var _mNavBarBgc = vPortal.themeHotspots.skin.mNavBarBgc;
            var _mNavBarSc = vPortal.themeHotspots.skin.mNavBarSc;
            return {
                "spaces": [{
                    "spaceName": $.i18n('vportal.designer.mobile.nav1')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav2')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav3')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav4')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav5')
                }],
                "skin": {
                    "mNavBarColor": _mNavBarColor,
                    "mNavBarBgc": _mNavBarBgc,
                    "mNavBarSc": _mNavBarSc
                }
            }
        } else {
            // 移动端的真实数据
            return {
                "Data": [{
                    "spaceName": $.i18n('vportal.designer.mobile.nav1')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav2')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav3')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav4')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav5')
                }]
            }
        }
    },
    afterInit: function(_elementId, thisElement, tplData, spacePath) {

        //目前没有哪种情况会渲染空间列表了，直接return吧！！！！
        return;

        //设计器里面 或 传了参数不要spaceBar的时候
        if (typeof(isMobileDesigner) === "undefined" || isMobileDesigner || (Urlparam && Urlparam.spaceBar == false)) {
            return;
        }
        //调用移动端的接口获取数据
        var CMP_V5_TOKEN = window.localStorage.CMP_V5_TOKEN;
        var _url = _v5Path + "/rest/mobilePortal/spaces/" + currentPortalId;
        var _this = this;
        var _getSpaceAjax = new newAjax({
            type: "GET",
            url: _url,
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
                'Accept-Language': "zh-CN",
                "token": CMP_V5_TOKEN || ""
            },
            dataType: "json",
            thisElement: thisElement,
            tplData: tplData,
            _this: _this,
            success: function(result, ret) {
                if (result.code == "200" && result.data && result.data.data.length > 0) {
                    var _tempData = {
                        "Data": result.data.data,
                        "themeHotspots": vPortal.themeHotspots[ret.thisElement.vPortalMainFrameElementsId]
                    }
                    vPortalMainFrameElements.spaceBar.spacesSummary = {};
                    vPortalMainFrameElements.spaceBar.spaces = [];
                    for (var i = 0; i < result.data.data.length; i++) {
                        vPortalMainFrameElements.spaceBar.spaces[i] = result.data.data[i].spaceId;
                        vPortalMainFrameElements.spaceBar.spacesSummary[result.data.data[i].spaceId] = result.data.data[i];
                    }
                    ret._this.afterInitSuccessFun(_tempData, ret.tplData, ret.thisElement.id, spacePath);
                }
            },
            error: function(error, ret) {}
        });
    },
    afterInitSuccessFun: function(currentPortalData, tplData, thisElementId, spacePath) {
        //只有一个空间的时候，不渲染空间列表
        if (currentPortalData.Data.length == 1) return;
        //渲染空间列表
        renderTpl(currentPortalData, tplData, thisElementId);

        //初始化空间的IScroll对象
        var _spaceScrollObj = "spaceScroll";
        //需要使用IScroll的DOM
        var _spaceScroll = document.getElementById("spaceScroll");
        var _spaceScrollSection = document.getElementById("spaceListArea");
        //把内容宽度赋值给元素，这样才能滚动
        _spaceScrollSection.style.width = _spaceScrollSection.scrollWidth + "px";
        window[_spaceScrollObj] = new IScroll(_spaceScroll, {
            eventPassthrough: true,
            scrollX: true,
            scrollY: false,
            preventDefault: true
        });
        _spaceScrollObj = _spaceScroll = _spaceScrollSection = null;

        //为排序按钮绑定事件
        var _sortSpaceBtn = document.getElementById('sortSpaceBtn');
        if (_sortSpaceBtn && cmp && cmp.event) {
            var index = this.getSpaceIndex(currentSpaceId);
            if (index == "-1") {
                index = 0;
            }
            //切换空间
            var _spaceListDom = document.querySelectorAll("#spaceListArea span");
            if (_spaceListDom) {
                for (var i = 0; i < _spaceListDom.length; i++) {
                    if (i == index) {
                        _spaceListDom[i].classList.add("currentSpace");
                    }
                    this.bindShowSpaceEvent(_spaceListDom, currentPortalData.Data, i, spacePath);
                }
            }
            //排序按钮
            cmp.event.click(_sortSpaceBtn, function() {
                spaceSelectionSort(currentPortalId, function(spaceList) {
                    if (null != spaceList) {
                        var _element = {
                            id: "portalSpaceBar",
                            tpl: "tpl-spaceBar",
                            vPortalMainFrameElementsId: "spaceBar"
                        }
                        getElementDataAndInit(_element);
                        initSection(spaceList[0]["spacePath"]);
                    }
                });

                // sectionSelectionSort(currentPortalId,_spaceId,function(path){
                //     var _element = {
                //         id:"portalSpaceBar",
                //         tpl:"tpl-spaceBar",
                //         vPortalMainFrameElementsId:"spaceBar"
                //     }
                //     getElementDataAndInit(_element,path.data);
                //     initSection(path.data);
                // });
            });
        }
    },
    showSpace: function(spaceListDom, currentSpacePath, index) {
        for (var i = 0; i < spaceListDom.length; i++) {
            spaceListDom[i].classList.remove("currentSpace");
        }
        spaceListDom[index].classList.add("currentSpace");
        initSection(currentSpacePath);
    },
    bindShowSpaceEvent: function(spaceListDom, spaceListData, index, spacePath) {
        var _this = this;
        if (spacePath != undefined) {
            if (spaceListData[index]["spacePath"] == spacePath) {
                spaceListDom[index].classList.add("currentSpace");
            } else {
                spaceListDom[index].classList.remove("currentSpace");
            }
        }
        cmp.event.click(spaceListDom[index], function() {
            _this.showSpace(spaceListDom, spaceListData[index].spacePath, index);
        });
    },
    //设计器中获取可设置属性
    getProp: function(dataJson) {
        var json = [{
            "groupName": "",
            "groupType": "mPortalNavContent",
            "groupValue": [{
                "id": "mPortalNavContent",
                "name": "mPortalNavContent",
                "type": "underLine",
                "typeShow": "default",
                "label": $.i18n('portal.hotspot.nav.setlink.label'),
                "click": ""
            }]
        }];
        var setNavCustomJson = {
            "id": "setNavCustomSwitch",
            "name": "setNavCustomSwitch",
            "type": "checkbox2",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label":$.i18n("space.allowdefined.label"),
                "id": "mTopOrMiddleNav",
                "name": "mTopOrMiddleNav",
                "checked": (!vPortal || !vPortal.mTopOrMiddleNavCustomSwitch || vPortal.mTopOrMiddleNavCustomSwitch == '1') ? "on" : "off"
            }],
            "click":""
        };
        if(vPortal.portalId==vPortal.mobileMainPortalId){
            json[0].groupValue.push(setNavCustomJson);
        }
        return json;
    },
    onPropChange: function(json, id, key, value) {

    },
    getSpaceIndex: function(_selectSpaceId) {
        for (var i = 0; i < this.spaces.length; i++) {
            if (this.spaces[i] == _selectSpaceId) {
                return i;
            }
        }
        return -1;
    },
    entrySpace: function(_selectSpaceId) {
        var _spaceListDom = document.querySelectorAll("#spaceListArea span");
        if (_spaceListDom) {
            for (var i = 0; i < _spaceListDom.length; i++) {
                _spaceListDom[i].classList.remove("currentSpace");
            }
        }
        _spaceListDom[this.getSpaceIndex(_selectSpaceId)].classList.add("currentSpace");
        var _selectSpace = this.spacesSummary[_selectSpaceId];
        if (_selectSpace) {
            initSection(_selectSpace.spacePath);
        }
    }
}

/*
 * 顶部
 */

vPortalMainFrameElements.portalTopIncludeColumn = {
    config: isMobileDesigner&&parent.themeJson&&parent.themeJson.from=="0"?false:true,//样式库不允许配置
    columnDataObj: {
        sections: "",
        columnsName: (typeof (isMobileDesigner) !== "undefined" && isMobileDesigner)?$.i18n('portal.space.designer.setHeadColumn.addlabel'):cmp.i18n('portal.space.designer.setHeadColumn.addlabel')
    },
    setPortalcolumn:false,//是否添加过栏目标识
    getData: function() {
        //M3工作台的话不显示头部栏目
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return false;
        if (typeof(isMobileDesigner) !== "undefined" && isMobileDesigner) {

            // 调用PC端的接口获取数据
            var _topIncludeColumnColor = vPortal.themeHotspots.skin.mHeaderColor;
            var _topIncludeColumnBgc = vPortal.themeHotspots.skin.mHeaderBgc;
            var _topIncludeColumnBgi = vPortal.themeHotspots.skin.mHeaderBgi;

            var entityId = vPortal.themeId;
            if (vPortal.portalId) {
                entityId = vPortal.portalId;
            }
            if (typeof(callBackendMethod) === "undefined") {
                return
            };
            var result = callBackendMethod("mobilePortalManager", "getPortalHeadColumnElementData", entityId,vPortal.themeId);
            this.columnDataObj="";
            if(result && result.columnDataObj){
                this.columnDataObj = result.columnDataObj;
            }
            //防护一下
            if(!this.columnDataObj || this.columnDataObj==""){
                this.columnDataObj = {
                    "sections":''
                }
            }
            var columnsPropertyIframeSrc = _ctxPath + "/portal/mobilePortal.do?method=setPortalElementProperty&portletId=" + entityId + "&sectionId=" + this.columnDataObj.sections
            top.$("#sectionConfigDiv").attr("src", columnsPropertyIframeSrc);

            var name = $.i18n('portal.space.designer.setHeadColumn.addlabel');
            if (this.columnDataObj && this.columnDataObj["columnsName:0"] && this.columnDataObj["columnsName:0"] != name ) {
                name = this.columnDataObj["columnsName:0"];
                this.setPortalcolumn = true;
            }

            //设计器界面，返回假数据并渲染
            return {
                "portalName": top.portalName,
                "topIncludeColumnColor": _topIncludeColumnColor,
                "topIncludeColumnBgc": _topIncludeColumnBgc,
                "topIncludeColumnBgi": _topIncludeColumnBgi,
                "topIncludeColumnContent": {
                    "name": name
                }
            };
        } else { //真正的移动端渲染不走getData，走afterInit
            return "";
        }
    },
    afterInit: function(_elementId, thisElement, tplData) {
        //M3工作台的话不显示头部栏目
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return;
        if (!(typeof(isMobileDesigner) !== "undefined" && isMobileDesigner)) {
            //没有配栏目直接过滤掉
            if(vPortal.headColumnInfo && vPortal.headColumnInfo.columnDataObj && vPortal.headColumnInfo.columnDataObj.sections){
                vPortalMainFrameElements.portalTopIncludeColumn.columnDataObj = vPortal.headColumnInfo.columnDataObj;
                var _parameter = {
                    //栏目的X坐标
                    "x": 0,
                    //栏目的y坐标
                    "y": 0,
                    //当前栏目下第一个页签的sectionId，section是A8后台用的，用于区别栏目类别
                    "sectionBeanId": vPortal.headColumnInfo.columnDataObj.sections,
                    //当前栏目块的ID
                    "entityId": currentPortalId,
                    //是多页签的第几个
                    "ordinal": "0",
                    //空间类型
                    "spaceType": "36",
                    //空间id
                    "spaceId": _spaceId,
                    //受限于A8的机制，需要给后台传width，但实际没啥用，遗憾的是不传会报错
                    "width": "10",
                    //一路往下传栏目的_index，供后续使用
                    "_index": "0",
                    //表示从头部栏目来的
                    "fromTopColumn": true
                };
                var _topIncludeColumnArea = document.getElementById("topIncludeColumn");
                var _tempDom = "<div id='columnSection_" + currentPortalId + "' class='columnDom " + vPortal.headColumnInfo.columnDataObj.sections + "'>";
                _tempDom += "<div id='columnBody_" + currentPortalId +"'>";
                _tempDom += "</div></div>";
                _topIncludeColumnArea.innerHTML = _tempDom;

                getDataAndRenderSection(_parameter);
            }
        }
        //设计器 && 非样式库  && 已添加栏目的时候
        if( typeof(isMobileDesigner) !== "undefined" && isMobileDesigner && parent.themeJson &&parent.themeJson.from=="1" && this.setPortalcolumn ){
            this.addDeleteBtn();
        }
    },
    addDeleteBtn:function(){
        var topIncludeColumnDom = document.getElementById("topIncludeColumn");
        var _columnsName = this.columnDataObj.columnsName ||  this.columnDataObj["columnsName:0"];
        topIncludeColumnDom.innerHTML = '<span class="DeleteTopColumn vportal vp-close" onclick="vPortalMainFrameElements.portalTopIncludeColumn.deleteHeadColumnData();"></span><div class="topColumn">'+_columnsName+'</div>';
        $(".DeleteTopColumn")[0].onmouseover = function() {
            document.querySelectorAll(".commonShade[data-tpl=tpl-portalTopIncludeColumn]")[0].style.backgroundImage = "url(" + _ctxPath + "/portal/images/opacity/black50.png)";
        };
        $(".DeleteTopColumn")[0].onmouseout = function() {
            document.querySelectorAll(".commonShade[data-tpl=tpl-portalTopIncludeColumn]")[0].style.backgroundImage = "none";
        };
    },
    savePostion: function() {
        var portalNavBarDom = document.getElementById("portalNavBar");
        var containerWrapperDom = document.getElementById("containerWrapper");
        if(portalNavBarDom && containerWrapperDom !== "undefined"){
            vPortal.portalNavBarTopPosition = portalNavBarDom.getBoundingClientRect().top - containerWrapperDom.getBoundingClientRect().top;
        }
        //头部栏目渲染完之后，再设置一次，避免计算偏差【之前设置过才纠正，
        //方法内部会判断，没设置过就不处理，因为后续createColumnPostion会再执行一次】
        setFirstScreenOneColumn();
    },
    // 设计器中获取可设置属性
    getProp: function(dataJson) {
        var entityId = "";
        var btnText = $.i18n('portal.button.sectionAdd');
        if (this.columnDataObj && this.columnDataObj.sections) {
            entityId = vPortal.themeId;
            if (vPortal.portalId) {
                entityId = vPortal.portalId;
            }
            //btnText = "替换当前栏目";
        }
        var json0 = {
            "groupName": "",
            "groupType": "topIncludeColumnContent",
            "groupValue": [{
                "id": "topIncludeColumnContent",
                "name": "topIncludeColumnContent",
                "type": "underLine",
                "typeShow": "default",
                "label": btnText,
                "click": "iframeWindow.vPortalMainFrameElements.portalTopIncludeColumn.setHeadColumn"
            }]
        };
        var json1 = {
            "type": "function",
            "name": "loadSectionProperty",
            "params": {
                "sectionId": this.columnDataObj.sections,
                "entityId": entityId,
                "spaceType": "36"
            }
        };
        var json = [];
        json.push(json0);
        //选择过栏目才显示栏目属性的面板
        if (this.columnDataObj && this.columnDataObj.sections) {
            json[0].groupValue.push(json1);
        }
        return json;
    },
    deleteHeadColumnData:function(){
        var entityId = vPortal.themeId;
        if (vPortal.portalId) {
            entityId = vPortal.portalId;
        }
        callBackendMethod("mobilePortalManager", "deletePortalHeadColumnElementData", entityId);
        this.columnDataObj = {
            sections: "",
            columnsName: $.i18n('portal.space.designer.setHeadColumn.addlabel')
        }
        this.setPortalcolumn = false;

        $(".DeleteTopColumn").remove()
        $(".topColumn").text($.i18n('portal.space.designer.setHeadColumn.addlabel'));
        document.querySelectorAll(".commonShade[data-tpl=tpl-portalTopIncludeColumn]")[0].style.backgroundImage = "none";
        top.$("#sectionConfigDiv")[0].contentWindow.location.reload();
        // top.$.infor("头部栏目删除成功!");
        top.$.infor($.i18n('portal.space.designer.setHeadColumn.infolabel'));
    },
    //设置头部元素栏目
    setHeadColumn: function() {
        var setHeadColumnDialog = $.dialog({
            id: 'portal_setHeadColumnDialog',
            isHide: true,
            url: _ctxPath + "/portal/portalDesigner.do?method=setHeadColumn&themeId="+vPortal.themeId,
            width: 800,
            height: 470,
            title: $.i18n('portal.space.designer.choosesection.label'),
            targetWindow: window.top,
            transParams: {},
            buttons: [{
                text: $.i18n('common.button.ok.label'),
                isEmphasize: true,
                handler: function() {
                    var returnValue = setHeadColumnDialog.getReturnValue();
                    if (returnValue) {
                        var headSectionId = returnValue.selectedSectionId;
                        var headSectionName = returnValue.selectedSectionName;
                        vPortalMainFrameElements.portalTopIncludeColumn.columnDataObj.sections = headSectionId;
                        vPortalMainFrameElements.portalTopIncludeColumn.columnDataObj.columnsName = headSectionName;

                        $(".topColumn").text(returnValue.selectedSectionName);
                        setHeadColumnDialog.close();
                        //更新是否添加过栏目的标识
                        vPortalMainFrameElements.portalTopIncludeColumn.setPortalcolumn = true;
                        vPortalMainFrameElements.portalTopIncludeColumn.loadSectionProperty({
                            sectionId: returnValue.selectedSectionId,
                            entityId: returnValue.entityId,
                        });
                        //设计器 && 非样式库  && 已添加栏目的时候
                        if( typeof(isMobileDesigner) !== "undefined" && isMobileDesigner && parent.themeJson &&parent.themeJson.from=="1" && vPortalMainFrameElements.portalTopIncludeColumn.setPortalcolumn ){
                            vPortalMainFrameElements.portalTopIncludeColumn.addDeleteBtn();
                        }
                    }
                }
            }, {
                text: $.i18n('common.button.cancel.label'),
                handler: function() {
                    setHeadColumnDialog.close();
                }
            }]
        });
    },
    loadSectionProperty: function(_params) {
        var _sectionConfigIframe = top.document.getElementById("sectionConfigDiv").contentWindow;
        //在iframe没加载完的时候点击会报错，这里返回下
        if(!_sectionConfigIframe.$leftSideBar){
            console.log("return");
            //恢复点击
            document.querySelectorAll(".commonShade[data-tpl=tpl-portalTopIncludeColumn]")[0].setAttribute("clicked","0");
            top.$(".elementArea").hide();
            return;
        }else{
            top.$(".elementArea").show();

            //添加过栏目才调用
            if(this.setPortalcolumn){
                //初始化已经选择的栏目的数据
                var entityIdKey = "";
                if (this.columnDataObj && this.columnDataObj.sections) {
                    entityIdKey = vPortal.themeId;
                    if (vPortal.portalId) {
                        entityIdKey = vPortal.portalId;
                    }
                }
                _sectionConfigIframe.$designerStorage.setPropertyByEntityId(entityIdKey,this.columnDataObj);
            }
        }
        //添加过栏目才调用
        if(this.setPortalcolumn){
            _sectionConfigIframe.$leftSideBar.init({
                sectionId: _params.sectionId,
                entityId: _params.entityId,
                isNew: false
            });
        }
    },
    onPropChange: function(json, id, key, value) {
        window.parent.putData(key, value, "portalTop");
        if (key == "mHeaderColor") {
            document.getElementById("currentPortalName").style.color = value;
        } else if (key == "mHeaderBgc") {
            //模板2生效区域是整个页面
            if (layoutName == "layout01_02") {
                document.querySelector(".wrapper").style.backgroundColor = value;
                document.querySelector(".portal-mobile-top").style.backgroundColor = value;
            } else {
                document.querySelector(".portal-mobile-top").style.backgroundColor = value;
            }
        }
    },
    saveProp: function() { //保存头部栏目元素数据
        var columnDataJson = $.toJSON(this.columnDataObj);
        var themeId = vPortal.themeId;
        var portalId = vPortal.portalId;
        if (typeof(callBackendMethod) === "undefined") {
            return
        };
        if (columnDataJson) {
            callBackendMethod("mobilePortalManager", "savePortalHeadColumnElement", themeId, portalId, columnDataJson, {
                success: function(result) {
                    console.log(result);
                }
            });
        }
    }
}

/*
 * 顶部
 */

vPortalMainFrameElements.portalheader4welcomes = {
    // config: isMobileDesigner&&parent.themeJson&&parent.themeJson.from=="0"?false:true,//样式库不允许配置
    config: true,
    soupsData:[],
    soupsDataFun: function() {
        var $selector = (typeof (isMobileDesigner) !== "undefined" && isMobileDesigner)?$:cmp;
        var soupsData = [$selector.i18n('portal.element.soupsData1'),$selector.i18n('portal.element.soupsData2'),$selector.i18n('portal.element.soupsData3'),$selector.i18n('portal.element.soupsData4'),$selector.i18n('portal.element.soupsData5'),$selector.i18n('portal.element.soupsData6'),$selector.i18n('portal.element.soupsData7'),$selector.i18n('portal.element.soupsData8'),"",""];
        //默认的几句心灵鸡汤
        this.soupsData = soupsData;
    },
    /*getHeight:function(){
        //M3工作台不显示该元素
        if( workbench == "workbench" ){
            return 0;
        }else{
            return 70;
        }
    },*/
    getData: function() {
        //M3工作台的话不显示头部三选一
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return false;

        var nowDate = new Date();
        var NowHour = nowDate.getHours();
        var timeStr = "";

		var welShowType= "3";
        this.soupsDataFun();
		if(vPortal.themeHotspots && vPortal.themeHotspots.welcomeInfo){
			if(vPortal.themeHotspots.welcomeInfo.soupsData){
				// soup= $.parseJSON(vPortal.themeHotspots.welcomeInfo.soupsData);
                if (typeof (isMobileDesigner) !== "undefined" && isMobileDesigner) {
				    vPortalMainFrameElements.portalheader4welcomes.soupsData = $.parseJSON(vPortal.themeHotspots.welcomeInfo.soupsData);
                }else{//移动端需要使用cmp.parseJSON
                    vPortalMainFrameElements.portalheader4welcomes.soupsData = cmp.parseJSON(vPortal.themeHotspots.welcomeInfo.soupsData);
                }
			}
            //去掉了门户名称选项了
            if(vPortal.themeHotspots.welcomeInfo && vPortal.themeHotspots.welcomeInfo.welShowType == "1"){
                vPortal.themeHotspots.welcomeInfo.welShowType = "3";
            }
			if(vPortal.themeHotspots.welcomeInfo.welShowType){
				welShowType= vPortal.themeHotspots.welcomeInfo.welShowType;
			}
		}

        var tempsoupsData = [];
        for(i=0;i < this.soupsData.length; i++){
            if(this.soupsData[i] !=""){
                tempsoupsData.push(this.soupsData[i]);
            }
        }
        //Math.floor(Math.random()*(max-min+1)+min); 求范围内的随机数
        var rolli = Math.floor(Math.random()*(tempsoupsData.length-1-0+1)+0);
        var soupStr = tempsoupsData[rolli];
        soupStr = typeof(soupStr) == "undefined"?"":soupStr;
        // console.log(soupStr);

        var $selector = typeof (isMobileDesigner) !== "undefined" && isMobileDesigner?$:cmp;
        var dateJson = {w0:$selector.i18n('portal.element.topLeftsystemOperation.label1'),w1:$selector.i18n('portal.element.topLeftsystemOperation.label2'),w2:$selector.i18n('portal.element.topLeftsystemOperation.label3'),w3:$selector.i18n('portal.element.topLeftsystemOperation.label4'),w4:$selector.i18n('portal.element.topLeftsystemOperation.label5'),w5:$selector.i18n('portal.element.topLeftsystemOperation.label6'),w6:$selector.i18n('portal.element.topLeftsystemOperation.label7'),m0:$selector.i18n('portal.element.topLeftsystemOperation.m1'),m1:$selector.i18n('portal.element.topLeftsystemOperation.m2'),m2:$selector.i18n('portal.element.topLeftsystemOperation.m3'),m3:$selector.i18n('portal.element.topLeftsystemOperation.m4'),m4:$selector.i18n('portal.element.topLeftsystemOperation.m5'),m5:$selector.i18n('portal.element.topLeftsystemOperation.m6'),m6:$selector.i18n('portal.element.topLeftsystemOperation.m7'),m7:$selector.i18n('portal.element.topLeftsystemOperation.m8'),m8:$selector.i18n('portal.element.topLeftsystemOperation.m9'),m9:$selector.i18n('portal.element.topLeftsystemOperation.m10'),m10:$selector.i18n('portal.element.topLeftsystemOperation.m11'),m11:$selector.i18n('portal.element.topLeftsystemOperation.m12')};
        if (NowHour < 12){
            timeStr = $selector.i18n("portal.element.portalheader4welcomes.morning");
        }else if (NowHour < 19){
            timeStr =  $selector.i18n("portal.element.portalheader4welcomes.afternoon");
        }else{
            timeStr =  $selector.i18n("portal.element.portalheader4welcomes.evening");
        }
        var result = {
            "welShowType":welShowType,
            "portalName":top.portalName,
            "nameAndwelcomes" : (vPortal.CurrentUser&&vPortal.CurrentUser.n?vPortal.CurrentUser.n:"xxx")+"，"+timeStr,
            "beautifulWords" : soupStr,
            "mHeaderColor" : vPortal.themeHotspots.skin.mHeaderColor,
            "mHeaderBgc" : vPortal.themeHotspots.skin.mHeaderBgc,
            "week":dateJson['w'+nowDate.getDay()],
            "month":dateJson['m'+nowDate.getMonth()],
            "day":nowDate.getDate(),
            "dayText":$selector.i18n('portal.element.topLeftsystemOperation.day')
            // "monthText":$selector.i18n('common.time.month')
        };

        return result;
    },
    afterInit: function(_elementId, thisElement, tplData) {
        if (typeof(isMobileDesigner) === "undefined" || isMobileDesigner) {
            return;
        }
        var currentInfo = document.getElementById("portalheader4welcomes");
        var avatarDom = currentInfo.querySelector(".type3");
        if(avatarDom){
            //进入个人信息
            avatarDom.addEventListener("tap", function(e) {
                e.preventDefault();
                e.stopPropagation();
                if(cmp.platform.CMPShell){//m3
                    cmp.href.openWebViewCatch = function() {return 1;}
                    cmp.href.next( _my + "/layout/my-index.html?&ParamHrefMark=true", {fromPage: 'app'}, {animated: true, direction: "left", nativeBanner: false});
                }else{//微协同
                    cmp.href.next(_platPath + "/H5/wechat/html/userCenter.html" + $verstion);
                }
            });
        }
    },
    // 设计器中获取可设置属性
    getProp: function(dataJson) {
		var welShowType= "3";
        if(dataJson && dataJson.welcomeInfo && dataJson.welcomeInfo.welShowType){
            //去掉了门户名称选项了
            if(dataJson.welcomeInfo.welShowType == "1"){
                dataJson.welcomeInfo.welShowType = "3";
            }
			welShowType = dataJson.welcomeInfo.welShowType;
            window.parent.putData("welShowType", welShowType, "welcomeInfo");
        }

        var groupValue1 = {
            "groupName": "",
            "groupType": "",
            "groupValue": [
            {
                "id" : "welShowType",
                "name" : "welShowType",
                "type" : "radio",
                "label" : "",
                "typeChoose" : "default",
                "value" : [
                    /*{
                        "label" : $.i18n('portal.themeDesigner.portalheader4welcomes.note1'),
                        "id" : "welShowType1",
                        "checked" : (welShowType == '1') ? "on" : "off",
                        "value" : "1"
                    }, */{
                        "label" : $.i18n('portal.themeDesigner.portalheader4welcomes.note2'),
                        "id" : "welShowType2",
                        "checked" : (welShowType == '2') ? "on" : "off",
                        "value" : "2"
                    }, {
                        "label" : $.i18n('portal.themeDesigner.portalheader4welcomes.note3'),
                        "id" : "welShowType3",
                        "checked" : (welShowType == '3') ? "on" : "off",
                        "value" : "3"
                    }
                ],
                "click" : "clickFun"
            },{
                "id": "header4welcomes",
                "name": "header4welcomes",
                "type": "underLine",
                "typeShow": "default",
                "label": $.i18n('portal.themeDesigner.portalheader4welcomes.edit'),
                "click": "iframeWindow.vPortalMainFrameElements.portalheader4welcomes.setSoups"
            }]
        };
        var json = [];
        json.push(groupValue1);
        return json;
    },
    //编辑自定义用语
    setSoups: function() {
    	var setSoupsDialog = $.dialog({
            id: 'portal_setSoupsDialog',
            isHide: true,
            url: _ctxPath + "/portal/portalDesigner.do?method=setWelcomeList",
            width: 500,
            height: 450,
            title: $.i18n('portal.themeDesigner.portalheader4welcomes.title'),
            targetWindow: window.top,
            transParams: {
				soupsData: vPortalMainFrameElements.portalheader4welcomes.soupsData
			},
            buttons: [{
                text: $.i18n('common.button.ok.label'),
                isEmphasize: true,
                handler: function() {
                    var returnValue = setSoupsDialog.getReturnValue();
                    if (returnValue) {
                    	vPortalMainFrameElements.portalheader4welcomes.soupsData= returnValue;
						var  soupsDataStr= JSON.stringify(returnValue);
						window.parent.putData("soupsData", soupsDataStr, "welcomeInfo");
                        setSoupsDialog.close();
                    }
                }
            }, {
                text: $.i18n('common.button.cancel.label'),
                handler: function() {
                	setSoupsDialog.close();
                }
            }]
        });
    },
    onPropChange: function(json, id, key, value) {
        if(json.groupValue[0].value[1].checked == "on"){
            parent.$("#header4welcomes").show();
        }else{
            parent.$("#header4welcomes").hide();
        }
		if(key=='welShowType'){
			window.parent.putData(key, value, "welcomeInfo");
        }
        //右侧选了之后 左侧渲染模板
        var newDataJson = this.getNewDataJson();
        if(null!=newDataJson){
            document.getElementById("portalheader4welcomes").innerHTML = "";
            var oldTPL = document.getElementById("tpl-portalheader4welcomes").innerHTML;
            //渲染函数
            renderTpl(newDataJson, oldTPL, "portalheader4welcomes");
            this.afterInit("portalheader4welcomes");
        }
    },
    saveProp: function() { //保存头部栏目元素数据
        // debugger;
    },
    getNewDataJson: function() {
        var nowDate = new Date();
        var NowHour = nowDate.getHours();
        var timeStr = "";

        var welShowType= "3";
        // debugger;
        if(parent.dataJson && parent.dataJson.welcomeInfo){
            if(parent.dataJson.welcomeInfo.soupsData){
                vPortalMainFrameElements.portalheader4welcomes.soupsData = $.parseJSON(parent.dataJson.welcomeInfo.soupsData);
            }
            if(parent.dataJson.welcomeInfo.welShowType){
                welShowType= parent.dataJson.welcomeInfo.welShowType;
            }
        }

        var tempsoupsData = [];
        for(i=0;i < this.soupsData.length; i++){
            if(this.soupsData[i] !=""){
                tempsoupsData.push(this.soupsData[i]);
            }
        }
        //Math.floor(Math.random()*(max-min+1)+min); 求范围内的随机数
        var rolli = Math.floor(Math.random()*(tempsoupsData.length-1-0+1)+0);
        var soupStr = tempsoupsData[rolli];
        soupStr = typeof(soupStr) == "undefined"?"":soupStr;
        // console.log(soupStr);

        var dateJson = {w0:$.i18n('portal.element.topLeftsystemOperation.label1'),w1:$.i18n('portal.element.topLeftsystemOperation.label2'),w2:$.i18n('portal.element.topLeftsystemOperation.label3'),w3:$.i18n('portal.element.topLeftsystemOperation.label4'),w4:$.i18n('portal.element.topLeftsystemOperation.label5'),w5:$.i18n('portal.element.topLeftsystemOperation.label6'),w6:$.i18n('portal.element.topLeftsystemOperation.label7'),m0:$.i18n('portal.element.topLeftsystemOperation.m1'),m1:$.i18n('portal.element.topLeftsystemOperation.m2'),m2:$.i18n('portal.element.topLeftsystemOperation.m3'),m3:$.i18n('portal.element.topLeftsystemOperation.m4'),m4:$.i18n('portal.element.topLeftsystemOperation.m5'),m5:$.i18n('portal.element.topLeftsystemOperation.m6'),m6:$.i18n('portal.element.topLeftsystemOperation.m7'),m7:$.i18n('portal.element.topLeftsystemOperation.m8'),m8:$.i18n('portal.element.topLeftsystemOperation.m9'),m9:$.i18n('portal.element.topLeftsystemOperation.m10'),m10:$.i18n('portal.element.topLeftsystemOperation.m11'),m11:$.i18n('portal.element.topLeftsystemOperation.m12')};
        if (NowHour < 12){
            timeStr = $.i18n("portal.element.portalheader4welcomes.morning");
        }else if (NowHour < 19){
            timeStr =  $.i18n("portal.element.portalheader4welcomes.afternoon");
        }else{
            timeStr =  $.i18n("portal.element.portalheader4welcomes.evening");
        }
        var result = {
            "welShowType":welShowType,
            "portalName":top.portalName,
            "nameAndwelcomes" : "xxx，"+timeStr,
            "beautifulWords" : soupStr,
            "mHeaderColor" : parent.dataJson.skin.mHeaderColor,
            "mHeaderBgc" : parent.dataJson.skin.mHeaderBgc,
            "week":dateJson['w'+nowDate.getDay()],
            "month":dateJson['m'+nowDate.getMonth()],
            "day":nowDate.getDate(),
            "dayText":$.i18n('portal.element.topLeftsystemOperation.day')
            // "monthText":$.i18n('common.time.month')
        };
        return result;
    }
}

/*
 * 空间列表、切换空间
 */
var navFalseListAreaIscroll,navListAreaIscroll;
vPortal.navBarScrollLeft = 0;
var navListBarDom;
var falseNavListBarDom = document.getElementById("portalFalseNavBar");
vPortalMainFrameElements.navBar = {
    config: isMobileDesigner&&parent.themeJson&&parent.themeJson.from=="0"?false:true,//样式库不允许配置
    spacesSummary: {},
    spaces: [],
    getData: function() {
        //M3工作台的话不显示中导航
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return false;
        if (typeof(isMobileDesigner) !== "undefined" && isMobileDesigner) {
            // 设计器假数据
            var _mNavBarColor = vPortal.themeHotspots.skin.mNavBarColor;
            var _mNavBarBgc = vPortal.themeHotspots.skin.mNavBarBgc;
            var _mNavBarSc = vPortal.themeHotspots.skin.mNavBarSc;
            return {
                "spaces": [{
                    "spaceName": $.i18n('vportal.designer.mobile.nav1')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav2')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav3')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav4')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav5')
                }],
                "skin": {
                    "mNavBarColor": _mNavBarColor,
                    "mNavBarBgc": _mNavBarBgc,
                    "mNavBarSc": _mNavBarSc
                }
            }
        } else {
            // 移动端的真实数据
            return false;
        }
    },
    afterInit: function(_elementId, thisElement, tplData, spacePath) {
        this._thisElement = thisElement;
        this._tplData = tplData;

        //M3工作台的话不显示中导航
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return;
        // 移动端的真实数据在afterInit里渲染
        if (!(typeof(isMobileDesigner) !== "undefined" && isMobileDesigner)) {

            //导航数据已缓存的时候，不必发rest请求
            if( !isEmptyObject(vPortal.portalNavCache) ){
                if (vPortal.portalNavCache.mTopOrMiddleNav && vPortal.portalNavCache.mTopOrMiddleNav.length > 0) {
                    var _tempData = {
                        "Data": vPortal.portalNavCache.mTopOrMiddleNav,
                        "mTopOrMiddleNavCustomSwitch":vPortal.portalNavCache.mTopOrMiddleNavCustomSwitch
                    }
                    this.afterInitSuccessFun(_tempData, tplData, thisElement.id, spacePath);
                }
                return;
            }

            var _url = _v5Path + "/rest/mobilePortal/getPortalNav/" + currentPortalId;
            var _this = this;
            var _getSpaceAjax = new newAjax({
                type: "GET",
                url: _url,
                headers: {
                    'Content-Type': 'application/json; charset=utf-8',
                    'Accept-Language': "zh-CN",
                    "token": CMP_V5_TOKEN || ""
                },
                dataType: "json",
                thisElement: thisElement,
                tplData: tplData,
                _this: _this,
                success: function(result, ret) {
                    // result.data.mTopOrMiddleNavCustomSwitch = false;
                    if (result.code == "200" && result.data && result.data.mTopOrMiddleNav && result.data.mTopOrMiddleNav.length > 0) {
                        vPortal.portalNavCache = result.data;
                        var _tempData = {
                            "Data": result.data.mTopOrMiddleNav,
                            "mTopOrMiddleNavCustomSwitch":result.data.mTopOrMiddleNavCustomSwitch
                        }
                        ret._this.afterInitSuccessFun(_tempData, ret.tplData, ret.thisElement.id, spacePath);
                    }
                },
                error: function(error, ret) {}
            });
        }
    },
    afterInitSuccessFun: function(currentPortalData, tplData, thisElementId, spacePath) {
        //只有一个空间的时候，不渲染空间列表
        if (currentPortalData.Data.length == 1) return;
        //渲染空间列表
        renderTpl(currentPortalData, tplData, thisElementId);
        navListBarDom = document.getElementById("navListBar");
        var _navListArea = document.querySelector("#portalNavBar .navListBarLeft");
        if(typeof(IScroll) !== "undefined" && _navListArea !== null){
            navListAreaIscroll = new IScroll(_navListArea, {
                scrollX: true,
                scrollY: false,
                preventDefault: false,
                useTransition: true,
                probeType: 3,
                click: true
            });
            navListAreaIscroll.on('scroll', function() {
                vPortal.navBarScrollLeft = this.x;
            });
        }
        this.copyAndRenderNavBar();
        vPortalMainFrameElements.portalFooter.loadNavJsEvent("navListBar");
        vPortalMainFrameElements.portalFooter.loadNavJsEvent("portalFalseNavBar");
        //没有头部栏目的时候直接savePostion
        if( !(vPortal.headColumnInfo && vPortal.headColumnInfo.columnDataObj && vPortal.headColumnInfo.columnDataObj.sections) ){
            vPortalMainFrameElements.portalTopIncludeColumn.savePostion();
        }
        //绑定自定义导航按钮的点击事件
        setTopOrMiddleNavSort(document.querySelector("#portalNavBar .navListBarRight"));

    },
    refreshNav: function() {
        if (vPortal.portalNavCache.mTopOrMiddleNav && vPortal.portalNavCache.mTopOrMiddleNav.length > 0) {
            myScroll.scrollTo(0, 0, 0);
            scrollFun();
            scrollEndFun();
            //渲染中导航
            var _tempData = {
                "Data": vPortal.portalNavCache.mTopOrMiddleNav,
                "mTopOrMiddleNavCustomSwitch":vPortal.portalNavCache.mTopOrMiddleNavCustomSwitch
            }
            this.afterInitSuccessFun(_tempData, this._tplData, this._thisElement.id);
        }
    },
    showSpace: function(spaceListDom, currentSpacePath, index) {
        for (var i = 0; i < spaceListDom.length; i++) {
            spaceListDom[i].classList.remove("currentSpace");
        }
        spaceListDom[index].classList.add("currentSpace");
        initSection(currentSpacePath);
    },
    bindShowSpaceEvent: function(spaceListDom, spaceListData, index, spacePath) {
        var _this = this;
        if (spacePath != undefined) {
            if (spaceListData[index]["spacePath"] == spacePath) {
                spaceListDom[index].classList.add("currentSpace");
            } else {
                spaceListDom[index].classList.remove("currentSpace");
            }
        }
        cmp.event.click(spaceListDom[index], function() {
            _this.showSpace(spaceListDom, spaceListData[index].spacePath, index);
        });
    },
    copyAndRenderNavBar: function() {
        var _navListBarDom = document.getElementById("navListBar");
        var _navFalseBarDom = document.getElementById("portalFalseNavBar");
        if(_navListBarDom !== null && _navFalseBarDom !== null) {
            _navFalseBarDom.innerHTML = _navListBarDom.innerHTML;
        }
        var spanList = document.querySelectorAll("#portalFalseNavBar .propItem");
        for(var i = 0; i < spanList.length; i++) {
            spanList[i].removeAttribute("style");
            spanList[i].querySelector(".horizontal-before").removeAttribute("style");
        }
        if(document.querySelector("#portalFalseNavBar .iconfont.icon-spread") !== null){
            document.querySelector("#portalFalseNavBar .iconfont.icon-spread").removeAttribute("style");
        }
        if (typeof (fixHeaderPos) != "undefined" && fixHeaderPos === true && document.getElementById("topNavPortalName")) {
            _navFalseBarDom.style.top = document.getElementById("topNavPortalName").clientHeight + "px";
        }else{
            _navFalseBarDom.style.top = WrapperHeightArr[0].H + "px";
        }
        var _navFalseListArea = document.querySelector("#portalFalseNavBar .navListBarLeft");
        if(typeof(IScroll) !== "undefined" && _navFalseListArea !== null){
            navFalseListAreaIscroll = new IScroll(_navFalseListArea, {
                scrollX: true,
                scrollY: false,
                preventDefault: false,
                useTransition: true,
                probeType: 3,
                click: true
            });
            navFalseListAreaIscroll.on('scroll', function() {
                vPortal.navBarScrollLeft = this.x;
            });
        }
        //绑定自定义导航按钮的点击事件
        setTopOrMiddleNavSort(document.querySelector("#portalFalseNavBar .navListBarRight"));
    },
    //设计器中获取可设置属性
    getProp: function(dataJson) {
        var json = [{
            "groupName": "",
            "groupType": "mPortalNavContent",
            "groupValue": [{
                "id": "mPortalNavContent",
                "name": "mPortalNavContent",
                "type": "underLine",
                "typeShow": "default",
                "label": $.i18n('portal.hotspot.nav.setlink.label'),
                "click": ""
            }]
        }];
        var setNavCustomJson = {
            "id": "setNavCustomSwitch",
            "name": "setNavCustomSwitch",
            "type": "checkbox2",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label":$.i18n("space.allowdefined.label"),
                "id": "mTopOrMiddleNav",
                "name": "mTopOrMiddleNav",
                "checked": (!vPortal || !vPortal.mTopOrMiddleNavCustomSwitch || vPortal.mTopOrMiddleNavCustomSwitch == '1') ? "on" : "off"
            }],
            "click":""
        };
        if(vPortal.portalId==vPortal.mobileMainPortalId){
            json[0].groupValue.push(setNavCustomJson);
        }
        return json;
    },
    onPropChange: function(json, id, key, value) {

    },
    getSpaceIndex: function(_selectSpaceId) {
        for (var i = 0; i < this.spaces.length; i++) {
            if (this.spaces[i] == _selectSpaceId) {
                return i;
            }
        }
        return -1;
    },
    entrySpace: function(_selectSpaceId) {
        var _spaceListDom = document.querySelectorAll("#spaceListArea span");
        if (_spaceListDom) {
            for (var i = 0; i < _spaceListDom.length; i++) {
                _spaceListDom[i].classList.remove("currentSpace");
            }
        }
        _spaceListDom[this.getSpaceIndex(_selectSpaceId)].classList.add("currentSpace");
        var _selectSpace = this.spacesSummary[_selectSpaceId];
        if (_selectSpace) {
            initSection(_selectSpace.spacePath);
        }
    }
}

/*
 * 空间列表、切换空间
 */
vPortalMainFrameElements.topNav = {
    config: isMobileDesigner&&parent.themeJson&&parent.themeJson.from=="0"?false:true,//样式库不允许配置
    itemsLength: 0,
    getHeight: function(){
        if( typeof (workbench) != "undefined" && workbench == "workbench" ){
            return 0;
        }else if(this.itemsLength == 1){
            return 0;
        }else{
            return 35;
        }
    },
    getData: function() {
        if (typeof(isMobileDesigner) !== "undefined" && isMobileDesigner) {
            // 设计器假数据
            var _mNavBarColor = vPortal.themeHotspots.skin.mNavBarColor;
            var _mNavBarBgc = vPortal.themeHotspots.skin.mNavBarBgc;
            var _mNavBarSc = vPortal.themeHotspots.skin.mNavBarSc;
            return {
                "spaces": [{
                    "spaceName": $.i18n('vportal.designer.mobile.nav1')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav2')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav3')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav4')
                }, {
                    "spaceName": $.i18n('vportal.designer.mobile.nav5')
                }],
                "skin": {
                    "mNavBarColor": _mNavBarColor,
                    "mNavBarBgc": _mNavBarBgc,
                    "mNavBarSc": _mNavBarSc
                }
            }
        } else {
            // 移动端的真实数据
            return false;
        }
    },
    afterInit: function(_elementId, thisElement, tplData, spacePath) {
        this._thisElement = thisElement;
        this._tplData = tplData;

        //M3工作台的话不显示上导航
        if( typeof (workbench) != "undefined" && workbench == "workbench" ) return;
        // 移动端的真实数据在afterInit里渲染
        if (!(typeof(isMobileDesigner) !== "undefined" && isMobileDesigner)) {


            //导航数据已缓存的时候，不必发rest请求
            if( !isEmptyObject(vPortal.portalNavCache) ){
                if (vPortal.portalNavCache.mTopOrMiddleNav && vPortal.portalNavCache.mTopOrMiddleNav.length > 0) {
                    var _tempData = {
                        "Data": vPortal.portalNavCache.mTopOrMiddleNav,
                        "mTopOrMiddleNavCustomSwitch":result.data.mTopOrMiddleNavCustomSwitch
                    }
                    this.afterInitSuccessFun(_tempData, tplData, thisElement.id, spacePath);
                }else{//接口返回也有可能是0，也不显示
                    //只有一个导航的时候，不渲染导航列表   且刷新高度
                    vPortalMainFrameElements.topNav.itemsLength = 1;
                    refreshWrapperHeight();
                }
                return;
            }


            // this.appendH5portalName();
            var _url = _v5Path + "/rest/mobilePortal/getPortalNav/" + currentPortalId;
            var _this = this;
            var _getSpaceAjax = new newAjax({
                type: "GET",
                url: _url,
                headers: {
                    'Content-Type': 'application/json; charset=utf-8',
                    'Accept-Language': "zh-CN",
                    "token": CMP_V5_TOKEN || ""
                },
                dataType: "json",
                thisElement: thisElement,
                tplData: tplData,
                _this: _this,
                success: function(result, ret) {
                    if (result.code == "200" && result.data && result.data.mTopOrMiddleNav && result.data.mTopOrMiddleNav.length > 0) {
                        vPortal.portalNavCache = result.data;
                        var _tempData = {
                            "Data": result.data.mTopOrMiddleNav,
                            "mTopOrMiddleNavCustomSwitch":result.data.mTopOrMiddleNavCustomSwitch
                        }
                        ret._this.afterInitSuccessFun(_tempData, ret.tplData, ret.thisElement.id, spacePath);
                    }else{//接口返回也有可能是0，也不显示
                        vPortal.portalNavCache = result.data;
                        //只有一个导航的时候，不渲染导航列表   且刷新高度
                        vPortalMainFrameElements.topNav.itemsLength = 1;
                        refreshWrapperHeight();
                    }
                },
                error: function(error, ret) {}
            });
        }
    },
    appendH5portalName:function(){
        //需要H5的门户名称
        document.querySelector("#portalTop").innerHTML = "<div class='topNavPortalName cmp-ellipsis' style='background-color:"+vPortal.themeHotspots.skin.mHeaderBgc+"; color:"+vPortal.themeHotspots.skin.mHeaderColor+"; ' >"+vPortal.portalName+"</div>";
    },
    afterInitSuccessFun: function(currentPortalData, tplData, thisElementId, spacePath) {
        //只有一个导航的时候，不渲染导航列表   且刷新高度
        if (currentPortalData.Data.length == 1){
            vPortalMainFrameElements.topNav.itemsLength = 1;
            refreshWrapperHeight();
            return;
        };
        //渲染导航
        renderTpl(currentPortalData, tplData, thisElementId);
        //高亮上导航
        highlightCurrentSpace('#topNavListBar',currentSpaceId);
        var _navListArea = document.querySelector("#portalMobileTopNav .topNavListBarLeft");
        if(typeof(IScroll) !== "undefined" && _navListArea !== null){
            navListAreaIscroll = new IScroll(_navListArea, {
                scrollX: true,
                scrollY: false,
                preventDefault: false,
                useTransition: true,
                probeType: 1,
                click: true
            });
        }
        vPortalMainFrameElements.portalFooter.loadNavJsEvent("topNavListBar");
        //绑定自定义导航按钮的点击事件
        setTopOrMiddleNavSort(document.querySelector("#portalMobileTopNav .topNavListBarRight"));
    },
    refreshNav: function() {
        if (vPortal.portalNavCache.mTopOrMiddleNav && vPortal.portalNavCache.mTopOrMiddleNav.length > 0) {
            //渲染上导航
            var _tempData = {
                "Data": vPortal.portalNavCache.mTopOrMiddleNav,
                "mTopOrMiddleNavCustomSwitch":vPortal.portalNavCache.mTopOrMiddleNavCustomSwitch
            }
            this.afterInitSuccessFun(_tempData, this._tplData, this._thisElement.id);
        }
    },
    //设计器中获取可设置属性
    getProp: function(dataJson) {
        var json = [{
            "groupName": "",
            "groupType": "mPortalNavContent",
            "groupValue": [{
                "id": "mPortalNavContent",
                "name": "mPortalNavContent",
                "type": "underLine",
                "typeShow": "default",
                "label": $.i18n('portal.hotspot.nav.setlink.label'),
                "click": ""
            }]
        }];
        var setNavCustomJson = {
            "id": "setNavCustomSwitch",
            "name": "setNavCustomSwitch",
            "type": "checkbox2",
            "label": "",
            "typeChoose": "default",
            "value": [{
                "label":$.i18n("space.allowdefined.label"),
                "id": "mTopOrMiddleNav",
                "name": "mTopOrMiddleNav",
                "checked": (!vPortal || !vPortal.mTopOrMiddleNavCustomSwitch || vPortal.mTopOrMiddleNavCustomSwitch == '1') ? "on" : "off"
            }],
            "click":""
        };
        if(vPortal.portalId==vPortal.mobileMainPortalId){
            json[0].groupValue.push(setNavCustomJson);
        }
        return json;
    }
}

/*
 * 空间列表、切换空间
 */
vPortalMainFrameElements.backButton = {
    config: false,
    getData: function() {
        if(cmp.platform.CMPShell && Urlparam && Urlparam.m3from == 'workbenchNav' ){//m3上且弹出的时候才会显示
            //默认高度  安卓无需考虑状态栏
            //iPhone和ipad的状态栏是20
            //iPhoneX是44
            var defaultPaddingTop = 13;
            if(cmp.os.iPhone || cmp.os.iPad ){
                defaultPaddingTop = 33; //+ 20
                if(cmp.os.isIphonex){
                    defaultPaddingTop = 57; //+44
                }
            }
            return {
                "mHeaderColor" : vPortal.themeHotspots.skin.mHeaderColor,
                "mHeaderTop" : defaultPaddingTop
            };
        }else{
            return false;
        }
    },
    afterInit: function() {
        if (isMobileDesigner) {
            return false;
        }
        if(cmp.platform.CMPShell && Urlparam && Urlparam.m3from == 'workbenchNav' ){//m3上且弹出的时候才会显示
            //左上角返回按钮
            cmp("#h5headerBack").on("tap",".backBtnArea",function() {
                cmp.backbutton.trigger();
            })
            cmp.backbutton();
            cmp.backbutton.push(cmp.href.back);
        }
    }
}