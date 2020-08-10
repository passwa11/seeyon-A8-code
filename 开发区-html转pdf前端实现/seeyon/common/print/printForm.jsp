<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="ctp" uri="http://www.seeyon.com/ctp" %>
<html>
<head id='linkList'>
    <c:choose>
        <c:when test="${param.printType != 'cap4Print'}">
            <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE9">
        </c:when>
        <c:otherwise>
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
            <meta name="renderer" content="webkit|ie-stand|ie-comp"/>
        </c:otherwise>
    </c:choose>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${ctp:i18n('collaboration.newcoll.print')}</title>
    <%@ include file="/WEB-INF/jsp/common/common_header.jsp" %>
    <style>
        #__pageTitle center span {
            word-break: break-word;
            word-wrap: break-word;
        }

        .stadic_layout_body {
            position: relative;
        }

        .hiddenNullOpinion {
            display: none;
        }

        .hiddenLocalPrint {
            display: none !important;
        }

        .ico16 {
            vertical-align: middle;
        }

        .body {
            border-top: 10px #ededed solid;
            border-bottom: 0px #ededed solid;
            margin: 0px;
            background: #fff;
        }

        /***全局样式干扰表单样式，此处需要重新定义这些样式***/
        #context div, #context form, #context input, #context textarea, #context p, #context th, #context td, #context ul, #context li {
            font-family: inherit;
        }

        #context .xdLayout td, #context .xdLayout td div {
            font-family: inherit;
        }

        .header {
            background: #ededed;
            width: 100%;
            height: 70px;
        }

        #context {
            background: #ffffff;
            margin: 0px auto;
            padding-left: 35px;
            padding-right: 35px;
            word-break: normal;
            /*text-align: center\0;*/
            /**text-align: center;*/
        }

        /**input框被替换成了span导致input[type='text']无法适用于原来的input内容，所以添加此样式**/
        span[type="text"] {
            font-size: 12px;
            height: auto;
            line-height: 20px;
            white-space: normal;
        }

        @media print {
            #header {
                display: none;
            }

            .body {
                border: 0px;
                margin: 0px;
            }

            #iSignatureHtmlDiv {
                display: none;
            }

            #context {
                padding: 0;
            }
        }

        .content .list_style_original ul {
            *margin-left: 40px;
            margin-left: 40px \0;
        margin-left( ): 37 px;
        }

        .list_style_original ol {
            *padding-left: 40px;
            padding-left: 40px \0;
        padding-left( ): 0 px;
        }

        .list_style_original ul li {
            list-style: disc;
        padding-left( ): 3 px;
        }

        .list_style_original ol li {
            list-style: decimal;
        }

        /*------2016-3-28--add-------*/
        /*.content ol,*/
        .content ul {
            margin: 3px 40px 0px 40px;
            margin: 3px 20px 0px 40px \9;
        margin-left( ): 37 px;
        }

        @media screen and (-webkit-min-device-pixel-ratio: 0) {
            .list_style_original ul {
                padding-left: 20px;
            }

            .content ul {
                margin: 0 20px;
                margin-left: 37px;
            }

            .content ol {
                padding-left: 34px;
            }

            .content > div[style^=font-size] ol {
                padding-left: 34px;
            }

            .content > div[style^=font-size] ul {
                padding-left: 20px;
            }

            .content ol li {
                padding-left: 6px;
            }
        }

        /*Firefox*/
        @-moz-document url-prefix() {

            .content ul {
                margin: 0 20px;
            }
            .content ul {
                padding-left: 22px;
            }
            .content ol li {
                padding-left: 3px;
            }
            .font_size14.list_style_original ol {
                padding-left: 36px;
            }
            .font_size14.list_style_original ul {
                padding-left: 20px;
            }
        }

        .content ul li {
            list-style: none;
        }

        .content ol li {
            list-style: decimal;
        }

        .processing_view .content > ul > li {
            list-style: none;
        }

        .processing_view .content .per_title {
            height: 38px !important;
            line-height: 38px !important;
        }

        .licomContent ul {
            list-style: disc;
        }

        .licomContent ul li {
            list-style: disc;
        }

        <%--div span span#field0001{--%>
        <%--overflow: hidden!important;--%>
        <%--height: inherit!important;--%>
        <%--min-height: 13px!important;--%>
        <%--line-height: 12px!important;--%>
        <%--}--%>
        <%--div font span span#field0001{--%>
        <%--height:auto!important;--%>
        <%--min-height:20px!important;--%>
        <%--}--%>
        .commentForwardDiv .processing_view .content ul {
            list-style: none;
        }

        .commentForwardDiv .processing_view .content ul ul {
            list-style: disc;
        }

        .commentForwardDiv .processing_view .content ul ul li {
            list-style: disc;
        }

        .commentForwardDiv .processing_view .content ul ol {
            list-style: decimal;
        }

        .commentForwardDiv .processing_view .content ul ol li {
            list-style: decimal;
        }

        #context ul li {
            list-style: disc;
        }

        TABLE.msoUcTable TD {
            BORDER-BOTTOM-COLOR: windowtext;
            BORDER-LEFT-COLOR: windowtext;
            BORDER-TOP-COLOR: windowtext;
            BORDER-RIGHT-COLOR: windowtext;
        }

        .per_title .right {
            width: 10%;
        }

        .per_title .right .right {
            width: auto;
        }

        .per_title .right .like_number {
            padding-right: 0;
        }

        .reply_data .licomContent {
            list-style: none;
        }

        #context .content .reply_data_li ul li {
            list-style: none;
        }

        #context .content li.replyContentLi {
            list-style: none;
        }

        #context .licomContent ol li {
            list-style: decimal;
        }

        /** 表单时，对字体进行处理 **/
        <c:if test="${param.contentType eq 20 or param.contentType eq 10}">
        tr div {
            font-family: SimSun, Arial, Helvetica, sans-serif;
        }

        </c:if>
    </style>
    <%@ include file="/WEB-INF/jsp/common/common_footer.jsp" %>
    <c:if test="${param.contentType eq 20 or param.contentType eq 10}">
        <script type="text/javascript" src="/seeyon/common/content/form.js"></script>
    </c:if>
    <script type="text/javascript">
        if ($.ctx.CurrentUser == null) {
            $.ctx.CurrentUser = opener.$.ctx.CurrentUser;
        }
        var printType = "${ctp:escapeJavascript(param.printType)}" || "commonPrint";
    </script>
    <script type="text/javascript">
        //表单签章相关,hw.js中需要用到
        var hwVer = '<%=DBstep.iMsgServer2000.Version("iWebSignature")%>';
        var webRoot = _ctxPath;
        var moduleType;
        //G6 6.1sp2 公文单的打印
        if (typeof (window.opener.parent.parent.affairApp) != 'undefined' && window.opener.parent.parent.affairApp && window.opener.parent.parent.affairApp == '4') {//公文处理节点
            moduleType = window.opener.parent.parent.affairApp;
            window.document.write('<style>.browse_class SPAN{color:windowtext}</style>');
        } else if (typeof (window.opener.parent.parent.app) != 'undefined' && window.opener.parent.parent.app && window.opener.parent.parent.app == '4') {//新建公文
            moduleType = window.opener.parent.parent.affairApp;
            window.document.write('<style>.browse_class SPAN{color:windowtext}</style>');
        }
    </script>
    <script type="text/javascript" src="${path}/common/isignaturehtml/js/isignaturehtml.js"></script>
    <script type="text/javascript" src="${path}/common/office/js/hw.js"></script>
    <%-- G6 6.1 新表单公文的方法单独提取 以后合并代码方便 --%>
    <%@ include file="/common/print/govdocPrint.js.jsp" %>
    <script type="text/javascript">
        //onload="printLoad();disabled($('context'));showOrDisableButton();loadSign();" style="overflow:hidden;" onbeforeunload="releaseISignatureHtmlObj();" id="bg"
        var contentType = "${ctp:escapeJavascript(param.contentType)}";
        var viewState = "${ctp:escapeJavascript(param.viewState)}";
        var showType = "all";

        var _currentZoom = 0;

        function doChangeSize(changeType) {
            var content = document.getElementById("context");
            if (changeType == "bigger") {
                thisMoreBig(content);
            } else if (changeType == "smaller") {
                thisSmaller(content);
            } else if (changeType == "self") {
                thisToSelf(content);
            } else if (changeType == "customize") {
                thisCustomize(content);
            }
        }

        function thisMoreBig(content, size) {
            if (!size) {
                size = 0.05;
            }
            zoomIt(content, size);
        }

        function thisSmaller(content, size) {
            if (!size) {
                size = -0.05;
            } else {
                size = size * -1;
            }
            zoomIt(content, size);
        }

        function thisToSelf(content) {
            zoomIt(content);
        }

        function thisCustomize(content) {
            var print8 = document.getElementById("print8");
            if ((parseFloat($(print8).val()) != $(print8).val())) {
                alert("${ctp:i18n('common.print.ratio.number.label')}");
                $(print8).val(0);
            }

            if ($(print8).val() > 500 || parseFloat($(print8).val()) < 0) {
                $(print8).val(0);
                return void (0);
            }
            if (content && print8 && print8.value != "") {
                if (isNaN(print8.value)) {
                    alert("${ctp:i18n('common.print.ratio.number.label')}");
                    return;
                }
                _currentZoom = 0;
                zoomIt(content, parseFloat(print8.value / 100) - 1, print8.value);
            }
        }

        <%-- 页面离开的时候卸载签章--%>
        window.onbeforeunload = releaseISignatureHtmlObj;
        if ($.browser.msie) {
            if ($.browser.version != '6.0' && $.browser.version != '7.0' && $.browser.version != '8.0') {
                window.onresize = function () {
                    window.location.href = window.location;
                }
            }
        }

        window.onresize = function () {
            if ($.browser.msie) {
                $("#header").css("width", $(document).width());
                $("#toolbarPrint").css("width", $(document).width() - 580);
            } else {
                $("#header").css("width", $(window).width());
                $("#toolbarPrint").css("width", $(window).width() - 480);
            }
        }
        var progress1;

        function removeWaterMark4Form() {
            //表单不需要在context上加水印，表单内部会加。解决BUG：OA-175910
            if (contentType == 20) {
                $("#context").css("backgroundImage", "");
            }
        }
    </script>
    <script language=javascript for=SignatureControl
            event=EventOnSign(DocumentId,SignSn,KeySn,Extparam,EventId,Ext1)>
        //作用：重新获取签章位置
        if (SignatureControl && EventId == 4) {
            CalculatePosition();
            SignatureControl.EventResult = true;
        }
    </script>
    <script type="text/javascript" src="/seeyon/common/js/ui/seeyon.ui.print-iframe-debug.js"></script>
    <script type="text/javascript" src="/seeyon/common/htmlzhou/html2canvas.min.js"></script>
    <script type="text/javascript" src="/seeyon/common/htmlzhou/jspdf.debug.js"></script>
<%--    <script src="https://cdn.bootcss.com/jspdf/1.5.3/jspdf.debug.js"></script>--%>
<%--    <script src="https://cdn.bootcss.com/html2canvas/0.5.0-beta4/html2canvas.min.js"></script>--%>

</head>
<body onLoad="printIframeLoad();" class="body">

<script>
    progress1 = new MxtProgressBar();
    if ($.browser.msie && printType != 'cap4Print') {
        $(".common_loading_progress_box").css("top", "300px");
    }
    progress1.start();
</script>
<div id="header" class="header" style="overflow:hidden">
    <div class="padding_l_10 left">
        <a id="print1" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('print.label')}</a>
        <a id="localPrint" class="hiddenLocalPrint common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('print.downloadLocal.label')}</a>
        <a id="print2" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('print.setting.label')}</a>
        <a id="print3" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('print.preview.label')}</a>
        <a id="print4" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('print.close.label')}</a>
        <p class="margin_t_5 margin_b_5 font_size12" id="_showOrDisableButton">
            <a id="print5" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('person.format.bigger')}</a>
            <a id="print6" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('person.format.smaller')}</a>
            <a id="print7" class="common_button common_button_gray" href="javascript:void(0)">${ctp:i18n('person.format.self')}</a>
            <a id="print10" class="common_button common_button_gray" href="javascript:void(0)">文单下载</a>
            <span class="margin_l_5">${ctp:i18n('person.format.size')}：</span><input type=text id="print8" style="border:1px #b6b6b6 solid;height:24px;width:30px;" value="100" onblur="doChangeSize('customize')"/>%
        </p>
    </div>
    <div class="padding_r_10 margin_t_20 right" id="toolbarPrint">
        <div id="checkOption" class="common_checkbox_box clearfix align_right right"></div>
    </div>

    <script type="text/javascript">
        if ($.browser.msie) {
            $("#toolbarPrint").css("width", $(document).width() - 580);
        } else {
            $("#toolbarPrint").css("width", $(document).width() - 480);
        }
    </script>
</div>
<%--<div style="height:0px;width:0px;position:absolute;">--%>
<%--    <div id="inputPosition"></div>--%>
<%--</div>--%>
<%-- HTML才增加id=newInputPosition的;表单正文在表单的第一个控件中添加，此处不用添加 --%>
<c:if test="${param.contentType ne 20}">
    <div style="height:0px;width:0px;position:absolute;background-color: red;">
        <div id="newInputPosition"></div>
    </div>
</c:if>
<div class="content set_ul_ls" id="context" style="zoom:1;word-break:normal;"></div>
</div>
<OBJECT id=WebBrowser classid=CLSID:8856F961-340A-11D0-A96B-00C04FD705A2 style="width:0px;height:0px;margin:0px;padding:0px"></OBJECT>
<script type="text/javascript">
    $(document).ready(function () {
        $("#toolbarPrint").css("width", $(document).width() - 480);
        //新公文支持“下载到本地打印”功能
        try {
            if (typeof (window.opener.parent.parent.canLocalPrint) != 'undefined' && window.opener.parent.parent.canLocalPrint == 'true' && (viewState == 1 || viewState == 2)) {
                $("#localPrint").removeClass("hiddenLocalPrint");
            }
        } catch (e) {
        }
        progress1.close();

        $('#print1').click(function () {
            if (isMSie8()) {
                printIt(1);
                return;
            }
            var mLength = document.getElementsByName("iHtmlSignature").length;
            for (var i = 0; i < mLength; i++) {
                var vItem = document.getElementsByName("iHtmlSignature")[i];
                vItem.SetParam('PRINTTYPE', '1');
            }
            printIt(1);
            for (var i = 0; i < mLength; i++) {
                var vItem = document.getElementsByName("iHtmlSignature")[i];
                vItem.SetParam('PRINTTYPE', '0');
            }
        });

        $('#print2').click(function () {
            printIt(8);
        });
        $('#print3').click(function () {
            if (isMSie8()) {
                printIt(7);
                return;
            }
            var mLength = document.getElementsByName("iHtmlSignature").length;
            for (var i = 0; i < mLength; i++) {
                var vItem = document.getElementsByName("iHtmlSignature")[i];
                vItem.SetParam('PRINTTYPE', '1');
            }
            printIt(7);
            for (var i = 0; i < mLength; i++) {
                var vItem = document.getElementsByName("iHtmlSignature")[i];
                vItem.SetParam('PRINTTYPE', '0');
            }
        });
        $('#print4').click(function () {
            thisclose();
        });
        //zhou
        $("#print10").click(function () {
            html2canvas(document.getElementById("context"),{
                    dpi: 10000,//导出pdf清晰度
                    onrendered: function (canvas) {
                        var contentWidth = canvas.width;
                        var contentHeight = canvas.height;
                        //一页pdf显示html页面生成的canvas高度;
                        // var pageHeight = contentWidth / 592.28 * 841.89;
                        var pageHeight = contentWidth / 592.28 * 841.89;
                        //未生成pdf的html页面高度
                        var leftHeight = contentHeight;
                        //pdf页面偏移
                        var position = 0;
                        //html页面生成的canvas在pdf中图片的宽高（a4纸的尺寸[595.28,841.89]）
                        var imgWidth = 595.28;
                        var imgHeight = 592.28 / contentWidth * (contentHeight);
                        //zhou

                        var pageData = canvas.toDataURL('image/jpeg', 1.0);
                        var pdf = new jsPDF('', 'pt', 'a4');

                        //有两个高度需要区分，一个是html页面的实际高度，和生成pdf的页面高度(841.89)
                        //当内容未超过pdf一页显示的范围，无需分页
                        if (leftHeight < pageHeight) {
                            pdf.addImage(pageData, 'JPEG', 0, 0, imgWidth, imgHeight);
                        } else {
                            // while (leftHeight > 0) {
                            //     pdf.addImage(pageData, 'JPEG', 0, position, imgWidth, imgHeight)
                            //     leftHeight -= pageHeight;
                            //     position -= 841.89;
                            //     //避免添加空白页
                            //     if (leftHeight > 0) {
                            //         pdf.addPage();
                            //     }
                            // }
                        }
                        pdf.save('content.pdf');
                    },
                    //背景设为白色（默认为黑色）
                    background: "#fff"
                    // x: 400 ,
                    // scrollY:0

                    // height: 841.89
                });
        });
        <%--变大--%>
        $('#print5').click(function () {
            doChangeSize('bigger')
        });
        <%--缩小--%>
        $('#print6').click(function () {
            doChangeSize('smaller')
        });
        $('#print7').click(function () {
            doChangeSize('self')
        });
        if (!$.browser.msie) {
            $('#print2').hide();
            $('#print3').hide();
        }
        $('#localPrint').click(function () {
            localPrint();
        });
    });
</script>
</body>
</html>
