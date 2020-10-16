<%@ include file="../header.jsp" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.seeyon.ctp.common.taglibs.functions.Functions" %>
<%@ page import="com.seeyon.ctp.util.Strings" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.IOException" %>

<%
    //OA-26762 修复参数脚本注入漏洞
    String type = request.getParameter("type");
// type = type==null?"":type.replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","\\\"");
    type = Functions.toHTML(type);
    String extensions = request.getParameter("extensions");
    extensions = extensions == null ? "" : extensions.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "\\\"");
    String applicationCategory = request.getParameter("applicationCategory");
//applicationCategory = applicationCategory==null?"":applicationCategory.replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("\"","\\\"");
    applicationCategory = Functions.toHTML(applicationCategory);
    String destDirectory = request.getParameter("destDirectory");
    destDirectory = destDirectory == null ? "" : destDirectory.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "\\\"");
    String destFilename = request.getParameter("destFilename");
    destFilename = destFilename == null ? "" : destFilename.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "\\\"");
    String maxSize = request.getParameter("maxSize");
    maxSize = Strings.escapeJavascript(maxSize);
    String isEncrypt = request.getParameter("isEncrypt");
    isEncrypt = isEncrypt == null ? "" : isEncrypt.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "\\\"");

    String pMaxSize = request.getParameter("maxSize");
    pMaxSize = Strings.escapeJavascript(pMaxSize);
    if (!com.seeyon.ctp.util.Strings.isBlank(pMaxSize)) {
%>
<c:set var="maxSize" value="<%=com.seeyon.ctp.util.Strings.formatFileSize(Long.parseLong(pMaxSize), false) %>"/>
<%
    }
%>
<%--zhou start--%>
<%!

    String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[4];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }


%>
<%
    HashMap<String, String> mFileTypes = new HashMap<String, String>();
    // images
    mFileTypes.put("FFD8FF", "jpg");
    mFileTypes.put("89504E47", "png");
    mFileTypes.put("47494638", "gif");
    mFileTypes.put("49492A00", "tif");
    mFileTypes.put("424D", "bmp");
    //
    mFileTypes.put("41433130", "dwg"); // CAD
    mFileTypes.put("38425053", "psd");
    mFileTypes.put("7B5C727466", "rtf"); // 日记本
    mFileTypes.put("3C3F786D6C", "xml");
    mFileTypes.put("68746D6C3E", "html");
    mFileTypes.put("44656C69766572792D646174653A", "eml"); // 邮件
    mFileTypes.put("D0CF11E0", "doc");
    mFileTypes.put("5374616E64617264204A", "mdb");
    mFileTypes.put("252150532D41646F6265", "ps");
    mFileTypes.put("255044462D312E", "pdf");
    mFileTypes.put("504B0304", "docx");
    mFileTypes.put("52617221", "rar");
    mFileTypes.put("57415645", "wav");
    mFileTypes.put("41564920", "avi");
    mFileTypes.put("2E524D46", "rm");
    mFileTypes.put("000001BA", "mpg");
    mFileTypes.put("000001B3", "mpg");
    mFileTypes.put("6D6F6F76", "mov");
    mFileTypes.put("3026B2758E66CF11", "asf");
    mFileTypes.put("4D546864", "mid");
    mFileTypes.put("1F8B08", "gz");
    mFileTypes.put("4D5A9000", "exe/dll");
    mFileTypes.put("75736167", "txt");

    FileInputStream is = null;
    String value = null;


%>
<%--zhou end--%>


<html style="height: 100%">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${ctp:i18n("fileupload.page.title")}</title>
    <link rel="stylesheet" href="${path}/common/all-min.css">

    <%@ include file="/WEB-INF/jsp/ctp/portal/common/components_theme.jsp" %>


    <c:if test="${e ne null}">
        <script type="text/javascript">
            var _parent = typeof (transParams) != "undefined" ? transParams.parentWin : null;
            if (typeof (_parent) == 'undefined' || _parent == null) {
                _parent = window.dialogArguments;
            }
            if (typeof (_parent) == 'undefined' || _parent == null) {
                _parent = window.parent;
            }
            alert("${v3x:showException(e, pageContext)}");
            //parent.again();
            //OA-106325系统后台：M3登录页自定义，上传背景图大于50m，提示框点击确定后报js(防护处理)
            if ((_parent != undefined) && (_parent.again != undefined)) {
                _parent.again();
            } else {
                //M3中上传文件没有使用统一方式，加一个特殊处理
                window.close();
            }
        </script>
    </c:if>

    <script type="text/javascript">//Attachment(id, reference, subReference, category, type, filename, mimeType, createDate, size, fileUrl, description){

    <%
        Object msg = request.getAttribute("upload.event.message");
    %>
    <%if(msg!=null){%>
    alert('<%=msg%>');
    <%}%>
    //判断是否安装了精灵
    //为了保持与原有代码的一致性，这里沿用该命名，通过H5中有的对象来判定浏览器是否支持H5
    function getIsA8geniusAdded() {
        if (window.applicationCache) {
            return true;
        } else {
            return false;
        }
    }

    var isA8geniusAdded = getIsA8geniusAdded();

    <c:if test="${atts != null}">
    // try {
    //     getA8Top().endProc();
    // } catch (e) {
    // }

    var callback = null;
    <c:if test="${not empty param.callback}" >
    if (parent.${ctp:escapeJavascript(param.callback)}) callback = parent.${ctp:escapeJavascript(param.callback)};
    </c:if>
    <c:if test="${timeZoneValue ne null}" >
    <fmt:setTimeZone value="${timeZoneValue}" />
    </c:if>
    var reAtts = new ArrayList();
    var fileurls = "";
<%--    <c:forEach items="${atts}" var="att">--%>
<%--    fileurls = fileurls + "," + '${att.fileUrl}';--%>
<%--    reAtts.add(new Attachment("${att.id}", "${att.reference}", "${ att.subReference}", "${ att.category}", "${ att.type}",--%>
<%--        "${ctp:escapeJavascript(att.filename)}", "${ctp:escapeJavascript(att.mimeType)}", "<fmt:formatDate value='${ att.createdate}' pattern='yyyy-MM-dd'/>", "${ att.size}", "${ att.fileUrl}", '',--%>
<%--        null, "${ att.extension}", "${ att.icon}", true, 'true', "${att.v}"));--%>

<%--    </c:forEach>--%>
    <c:if test="${(empty param.targetAction and empty param.callMethod )  or (not empty param.callMethod and 'false' eq param.takeOver)}" >
    <c:forEach items="${atts}" var="f">
    <fmt:formatDate value="${f.createdate}" pattern="yyyy-MM-dd HH:mm:ss" var="createDate" />
    <c:if test="${empty param.attachmentTrId}" >
    if (callback) {
        callback('${f.type}', '<v3x:out value="${f.filename}" escapeJavaScript="true" />', '${f.mimeType}', '${createDate}', '${f.size}', '${f.fileUrl}', null, null, '${f.extension}', '${f.icon}', "${f.v}");
    } else {
        parent.addAttachment('${f.type}', '<v3x:out value="${f.filename}" escapeJavaScript="true" />', '${f.mimeType}', '${createDate}', '${f.size}', '${f.fileUrl}', null, null, '${f.extension}', '${f.icon}', "${f.v}");
    }
    </c:if>
    <c:if test="${not empty param.attachmentTrId}" >
    parent.addAttachmentPoi('${f.type}', '<v3x:out value="${f.filename}" escapeJavaScript="true" />', '${f.mimeType}', '${createDate}', '${f.size}', '${f.fileUrl}', null, null, '${f.extension}', '${f.icon}', '${ctp:toHTML(param.attachmentTrId)}', null, '<%=applicationCategory%>', null, null, '${ctp:escapeJavascript(param.embedInput)}', null, null, "${f.v}", null, '${ ctp:escapeJavascript(param.isShowImg)}');
    </c:if>
    </c:forEach>
    </c:if>

    <c:if test="${not empty param.targetAction or not empty param.callMethod}" >
    <c:if test="${'true' ne param.firstSave}">
    parent.callforward(fileurls.substring(1), "${ctp:escapeJavascript(param.repeat)}");
    </c:if>
    <c:if test="${'true' eq param.firstSave}">
    parent.callforward(reAtts, "${ctp:escapeJavascript(param.repeat)}");
    </c:if>
    </c:if>
    <c:if test="${param.closeWindow != 'false'}">

    windowClose();

    </c:if>
    </c:if>

    var number = 0;

    function checks() {
        if (number == 0 || files.isEmpty()) {
            //return false;
            alert("${ctp:i18n_1('fileupload.selectfile.label',maxSize)}");
            return;
        }

        //$("#b1").disable();
        document.getElementById("b1").disabled = true;
        show();

        for (var i = 1; i <= index; i++) {
            var o = document.getElementById("file" + i);
            if (!o) {
                continue;
            }

            if (!o.value) {
                document.getElementById("fileInputDiv" + i).parentNode.removeChild(document.getElementById("fileInputDiv" + i));
            }
        }
        if ($("#importExplain").size() > 0) {
            $("#importExplain").removeAttr("onclick");//OA-121629防护处理，防止由于提交过程中点击“导入说明”，造成窗口无法关闭
        }
        document.getElementById('form_upload').submit();
        //return true;

    }

    function checksH5() {
        // if(number == 0 || files.isEmpty()){
        //     //return false;
        //      alert("${ctp:i18n_1('fileupload.selectfile.label',maxSize)}");
        //     return ;
        // }

        // //$("#b1").disable();
        document.getElementById("b1").disabled = true;
        if (number == 0) {
            alert("${ctp:i18n_1('fileupload.selectfile.label',maxSize)}");
            document.getElementById("b1").disabled = false;
            return false;
        }
        for (var i = 1; i <= index; i++) {
            var o = document.getElementById("file" + i);
            if (!o) {
                continue;
            }
            // if(o.files.length<1)
            if (o.files.length < 1) {
                document.getElementById("fileInputDiv" + i).parentNode.removeChild(document.getElementById("fileInputDiv" + i));
            }
        }
        show();
        if ($("#importExplain").size() > 0) {
            $("#importExplain").removeAttr("onclick");//OA-121629防护处理，防止由于提交过程中点击“导入说明”，造成窗口无法关闭
        }
        document.getElementById('form_upload').submit();
        return true;

    }

    function addInput(i) {
        //var html =  "<div id=\"fileInputDiv" + i + "\" class=\"file_unload clearfix\" style=\"\">" + "</div>";
        var e = document.createElement("div");
        e.setAttribute("id", "fileInputDiv" + i);
        e.className = "file_unload clearfix";
        var fileNameIndexFlag = i;
        if (quantity == 1) fileNameIndexFlag = 1;
        var inputHTML1 = "";
        inputHTML1 += "<a class=\"common_button common_button_icon file_click\" href=\"###\"><em class=\"ico16 affix_16\"></em>${ctp:i18n('fileupload.addfile.label')}";
        inputHTML1 += "<INPUT type=\"file\" name=\"file" + fileNameIndexFlag + "\" id=\"file" + i + "\" onchange=\"addNextInput(this)\" onkeydown=\"return false\" onkeypress=\"return false\" style=\"width: 100%\"/>";
        inputHTML1 += "</a>";
        e.innerHTML = inputHTML1;
        document.getElementById("fileInputDiv").appendChild(e);
    }

    function addNextInput(obj) {
        if (obj == undefined || obj == null) {
            //当程序不支持HTML5改变为普通的文件上传时,获取对应的input（IE8特殊处理）
            obj = this;
        }
        if (obj.type != undefined && obj.type == "change") {
            //当程序不支持HTML5改变为普通的文件上传时，获取对应的input
            obj = obj.target;
        }
        isExtensions = 0;
        number++;
        //alert("++=" + number);
        if (files.values().contains(obj.value)) {
            removeInput(index);
            addInput(index);

            alert('${ctp:i18n("fileupload.uploading.upload_alert_exist")}');
            return false;
        }

        if (!checkExtensions(obj)) {
            isExtensions = 1;
            removeInput(index);
            addInput(index);
            return false;
        }

        if (number > quantity) {
            removeInput(index);
            addInput(index);

            alert($.i18n("fileupload.uploading.upload_alert_limit").format(quantity));
            //alert('${ctp:i18n("fileupload.uploading.upload_alert_limit")}');
            return false;
        }

        document.getElementById("fileInputDiv" + index).style.display = 'none';
        var s = obj.value.lastIndexOf("\\");
        if (s < 0) {
            s = obj.value.lastIndexOf("/");
        }
        var filename = obj.value.substring(s + 1);
        var nameHTML = "";
        //<li><span title="图片上传的附件.jpg">图片上传的附件.jpg</span><em class="ico16 revoked_process_16"></em></li>
        nameHTML += "<li id='fileNameDiv" + index + "' class='margin_r_10'>"
        nameHTML += "<span title='" + escapeStringToHTML(filename) + "'>"
        nameHTML += filename.getLimitLength(16).escapeHTML();
        nameHTML += "</span>"
        nameHTML += "<em  onclick='deleteOne(" + index + ")' class='ico16 affix_del_16' title='${ctp:i18n("fileupload.uploading.attachment_delete")}'></em>";
        nameHTML += "</li>";

        document.getElementById("fileNameDiv").innerHTML += nameHTML;
        files.put(index, obj.value);
        index++;
        addInput(index);
        return true;
    }

    function checkExtensionsH5(fileType, fileName) {
        var extensionstr = document.getElementById("extensions").value;
        //alert(extensionstr);
        //alert(fileType);
        if (!extensionstr) {
            return true;
        }
        var lastSeparator = fileName.lastIndexOf(".");
        if (lastSeparator == -1) {
            return true;
        }

        if (extensionstr) {
            var extensions = extensionstr.split(",");
            var fileNameExtensin = fileName.substring(lastSeparator + 1).toUpperCase();
            for (var i = 0; i < extensions.length; i++) {
                var matched = extensions[i];
                if ((fileType.toUpperCase().indexOf(matched.toUpperCase()) > 0) || (matched.toUpperCase() == fileNameExtensin)) {
                    return true;
                }
                if (i == extensions.length - 1) {
                    alert("${ctp:i18n_1('fileupload.exception.UnallowedExtension',ctp:toHTML(param.extensions))}");
                    return false;
                }
            }
        }
    }

    function checkExtensions(obj) {
        var filepath = obj.value;
        if (!filepath) {
            return false;
        }

        var extensionstr = document.getElementById("extensions").value;
        if (!extensionstr) {
            return true;
        }

        var lastSeparator = filepath.lastIndexOf(".");
        if (lastSeparator == -1) {
            return true;
        } else {
            if (extensionstr) {
                var extension = filepath.substring(lastSeparator + 1).toLowerCase();
                var extensions = extensionstr.split(",");

                for (var i = 0; i < extensions.length; i++) {
                    if (extensions[i] == extension) {
                        return true;
                    }
                }
            }
        }

        alert("${ctp:i18n_1('fileupload.exception.UnallowedExtension',ctp:toHTML(param.extensions))}");

        return false;
    }

    function show() {
        document.getElementById("upload1").style.display = "none";
        document.getElementById("uploadprocee").style.display = "";
    }

    function addAttachment(type, filename, mimeType, createDate, size, fileUrl, canDelete, needClone, extension, icon, v) {
        //var _parent = window.opener;
        debugger;
        var _parent = typeof (transParams) != "undefined" ? transParams.parentWin : null;
        if (_parent == null) {
            _parent = window.dialogArguments;
        }
        var callback = null;
        <c:if test="${not empty param.callback}" >
        if (_parent.${ctp:escapeJavascript(param.callback)}) callback = _parent.${ctp:escapeJavascript(param.callback)};
        </c:if>

        if (callback) {
            callback(type, filename, mimeType, createDate, size, fileUrl, canDelete, needClone, '', extension, icon, null, null, false, v);
        } else {
            _parent.addAttachment(type, filename, mimeType, createDate, size, fileUrl, canDelete, needClone, '', extension, icon, null, '<%=applicationCategory%>', false, null, false, false, v);
        }

        //根据这个变量来判断是否选择了附件。
        if (typeof (_parent.hasUploadAtt) != 'undefined') _parent.hasUploadAtt = true;

    }

    function windowClose() {
        if (getCtpTop().addattachDialog) {
            getCtpTop().addattachDialog.close();
        } else if (getA8Top().addattachDialog) {
            getA8Top().addattachDialog.close();
        } else if (parent.addattachDialog) {
            parent.addattachDialog.close();
        } else if (parent.parent.addattachDialog) {
            parent.parent.addattachDialog.close();
        } else {
            parent.window.close();
        }
    }

    function addAttachmentPoi(type, filename, mimeType, createDate, size, fileUrl, canDelete, needClone, extension, icon, poi, reference, category, onlineView, width, embedInput, hasSaved, isCanTransform, v, canFavourite, isShowImg) {
        //var _parent = window.opener;
        var _parent = typeof (transParams) != "undefined" ? transParams.parentWin : null;
        if (typeof (_parent) == 'undefined' || _parent == null) {
            _parent = window.dialogArguments;
        }
        if (typeof (_parent) == 'undefined' || _parent == null) {
            _parent = window.parent;
        }
        _parent.addAttachmentPoi(type, filename, mimeType, createDate, size, fileUrl, canDelete, needClone, '', extension, icon, poi, reference, category, onlineView, width, embedInput, hasSaved, isCanTransform, v, canFavourite, isShowImg);


        //根据这个变量来判断是否选择了附件。
        if (typeof (_parent.hasUploadAtt) != 'undefined') _parent.hasUploadAtt = true;
    }

    function callforward(fileurls, repeat) {
        //alert(fileurls);
        // var _parent = window.opener;
        var _parent = typeof (transParams) != "undefined" ? transParams.parentWin : null;
        <c:if test="${not empty param.targetAction}" >

        if (_parent == null) {
            _parent = window.dialogArguments;
        }
        if (typeof (_parent) == 'undefined' || _parent == null) {
            _parent = window.parent;
        }
        _parent.${ ctp:escapeJavascript(param.targetAction)}(fileurls, repeat);
        //window.close();
        </c:if>
        <c:if test="${not empty param.callMethod}" >
        //var _parent = window.opener;
        if (_parent == null) {
            _parent = window.dialogArguments;
        }
        if (typeof (_parent) == 'undefined' || _parent == null) {
            _parent = window.parent;
        }
        if (_parent != null) {
            try {
                if (_parent.$("#${ ctp:toHTML(param.inputId)}").parent().hasClass("error-form")) {
                    _parent.$("#${ ctp:toHTML(param.inputId)}").parent().removeClass("error-form");
                }
                var spanEl = _parent.$("#${ ctp:toHTML(param.inputId)}").parent().next();
                if (spanEl != null && spanEl.hasClass("error-title")) {
                    spanEl.remove();
                }
            } catch (ex) {

            }

        }
        if (_parent.${ ctp:escapeJavascript(param.callMethod)} != null) {
            _parent.${ ctp:escapeJavascript(param.callMethod)}(fileurls, repeat);
        } else if (_parent.getA8Top != null && _parent.getA8Top().${ ctp:escapeJavascript(param.callMethod)} != null) {
            _parent.getA8Top().${ ctp:escapeJavascript(param.callMethod)}(fileurls, repeat);
        }

        //window.close();
        </c:if>
    }

    function listenerKeyPress() {
        if (event.keyCode == 27) {
            //window.close();
        }
    }

    function again() {
        document.getElementById("b1").disabled = false;
        //$("#b1").enable();
        document.getElementById("upload1").style.display = "";
        document.getElementById("uploadprocee").style.display = "none";

        number = 0;
        files.clear();

        document.getElementById("fileNameDiv").innerHTML = " <li><a>&nbsp;</a></li>";
        document.getElementById("fileInputDiv").innerHTML = "";
        //OA-103451,上传超过100kb的图片，第一次提示正常，第二次上传，报js错误
        //需要针对系统是否支持H5多文件上传来选择上传的方法
        if (isA8geniusAdded) {
            addInputH5(index);
        } else {
            addInput(index);
        }
    }

    var quantity = 9999;
    // 参数限制文件数小于5时，安装了精灵也受控，否则不受参数限制
    var paramQuantity = <c:out value="${param.quantity}" default="30" />;
    //测试过程中发现的漏洞问题，当传入的限制数大于等于5时传入的数量限制没有效果，都是9999，查看了历史记录3.5就是这么写的
    //不清楚为啥这么搞呀
    quantity = paramQuantity;
    var index = 1;

    var files = new Properties();
    var geniusNum = 0;

    var isExtensions = 0;
    //zhou
    //图片格式  :  '.bmp','.jpg','.png','.tif','.gif','.pcx','.tga','.exif','.fpx','.svg','.psd','.cdr','.pcd','.dxf','.ufo','.eps','.ai','.raw','.WMF','.webp'
    var acceptType_z = ['.syz', '.sfp', '.txt', '.doc', 'docx', '.xls', '.xlsx','.xsn'
        //图片格式
        , '.bmp', '.jpg', '.png', '.tif', '.gif', '.pcx', '.tga', '.exif', '.fpx', '.svg', '.psd', '.cdr', '.pcd', '.dxf', '.ufo', '.eps', '.ai', '.raw', '.WMF', '.webp'];

    function addNextInputH5(obj) {
        var accept_z = obj.accept;
        var filename_z = (obj.value).substring(obj.value.lastIndexOf("."));
        if (accept_z == '') {
            alert("不允许上传的文件类型");
        } else {
            if (acceptType_z.indexOf(filename_z) != -1) {
                var selectedFileSize = obj.files.length;
                var oldNumber = number;// 本次选择文件前已经选择的文件数
                for (var i = 0; i < selectedFileSize; i++) {
                    //alert(i+":"+selectedFileSize);
                    var selectedFile = obj.files[i];
                    if (i == 0) {
                        number = number + selectedFileSize;
                    }
                    document.getElementById("fileInputDiv" + index).style.display = 'none';
                    //  var s = obj.value.lastIndexOf("\\");
                    //  if(s < 0){
                    //     s = obj.value.lastIndexOf("/");
                    // }
                    var filename = selectedFile.name;
                    var fileType = selectedFile.type;
                    //alert(":"+ filename);
                    var nameHTML = "";
                    var fileNameDivIndex = index + "_" + i;
                    if (!checkExtensionsH5(fileType, filename)) {
                        //alert("error");
                        isExtensions = 1;
                        removeInput(fileNameDivIndex);
                        addInputH5(index);
                        return false;
                    }
                    try {
                        var maxSize = "${(not empty param.maxSize) ? param.maxSize : v3x:getSystemProperty('fileUpload.maxSize')}";
                        var maxSizeInt = parseInt(maxSize);
                        if (selectedFile.size > maxSizeInt) {
                            removeInput(fileNameDivIndex);
                            addInputH5(index);
                            alert(selectedFile.name + ",${ctp:i18n_1('fileupload.exception.MaxSize',maxSize)}");
                            //document.getElementById("fileInputDiv" + i).parentNode.removeChild(document.getElementById("fileInputDiv" + i));
                            return false;
                        }
                    } catch (err) {

                    }
                    if (number > quantity) {
                        removeInput(fileNameDivIndex);
                        addInputH5(index);
                        number = oldNumber;// 还原为之前已选择的文件数量
                        alert($.i18n("fileupload.uploading.upload_alert_limit").format(quantity));
                        //alert('${ctp:i18n("fileupload.uploading.upload_alert_limit")}');
                        return false;
                    }

                    nameHTML += "<li id='fileNameDiv" + fileNameDivIndex + "' class='margin_r_10'>"
                    nameHTML += "<span title='" + escapeStringToHTML(filename) + "'>"
                    nameHTML += filename.getLimitLength(16).escapeHTML();
                    nameHTML += "</span>"
                    if (i == (selectedFileSize - 1)) {
                        //由于H5操作一次上传的多个文件时，无法操作其中的一个文件，只能是要删除的话都删除；删除按钮放到最后一个文件的后面
                        nameHTML += "<em  onclick='deleteOne(" + "\"" + fileNameDivIndex + "\"" + ")' class='ico16 affix_del_16' title='${ctp:i18n("fileupload.uploading.attachment_delete")}'></em>";
                    }
                    nameHTML += "</li>";

                    document.getElementById("fileNameDiv").innerHTML += nameHTML;
                    // files.put(index, obj.value);

                }
                index++;
                addInputH5(index);
                return true;
            } else {
                alert("不允许上传的文件类型");
            }

        }
    }

    function addInputH5(i) {
        //var html =  "<div id=\"fileInputDiv" + i + "\" class=\"file_unload clearfix\" style=\"\">" + "</div>";
        var e = document.createElement("div");
        e.setAttribute("id", "fileInputDiv" + i);
        e.className = "file_unload clearfix";
        var fileNameIndexFlag = i;
        if (quantity == 1) fileNameIndexFlag = 1;
        //var eInput;
        /*         if(navigator.userAgent.indexOf("MSIE")>0){
                     var inputHTML = "<INPUT type=\"file\" name=\"file" + fileNameIndexFlag + "\" id=\"file" + i + "\" onchange=\"addNextInput(this)\" onkeydown=\"return false\" onkeypress=\"return false\" style=\"width: 100%\">";
                     eInput = document.createElement(inputHTML);
                } else { */

        var inputHTML1 = "";
        inputHTML1 += "<a class=\"common_button common_button_icon file_click\" href=\"###\"><em class=\"ico16 affix_16\"></em>${ctp:i18n('fileupload.addfile.label')}";
        // zhou
        inputHTML1 += "<input accept=\".xsn,.syz,.sfp,.txt,.doc,docx,.xls,.xlsx,.bmp,.jpg,.png,.tif,.gif,.pcx,.tga,.exif,.fpx,.svg,.psd,.cdr,.pcd,.dxf,.ufo,.eps,.ai,.raw,.WMF,.webp\" " +
            "multiple type=\"file\" name=\"file" + fileNameIndexFlag + "\" id=\"file" + i + "\" onchange=\"addNextInputH5(this)\" onkeydown=\"return false\" onkeypress=\"return false\" style=\"width: 100%\"/>";
        inputHTML1 += "</a>";
        e.innerHTML = inputHTML1;

        /*
            eInput = document.createElement("input");
            eInput.setAttribute("type","file");
            eInput.setAttribute("name","file" + fileNameIndexFlag);
            eInput.setAttribute("id","file" + i);
            eInput.setAttribute("onchange","addNextInput(this)");
            eInput.setAttribute("onkeydown","return false");
            eInput.setAttribute("onkeypress","return false");
            eInput.setAttribute("style","width:100%");
            */

        /*         } */
        //e.appendChild(eInput);
        document.getElementById("fileInputDiv").appendChild(e);
    }


    function deleteOne(i) {
        //alert(i);
        // var pos = getIndex(i);
        removeInput(i);

        // files.remove(i);

    }

    function removeInput(i) {
        i = i + "";
        var errorNode = false;
        //var pos = getIndex(i);
        var inputFileIndex = i.split("_");
        var inputIndex = inputFileIndex[0];
        //var fileIndex=inputFileIndex[1];
        try {
            if (isA8geniusAdded) {
                number = number - document.getElementById("file" + inputIndex).files.length;
            } else {
                number--;
                files.remove(parseInt(i));
            }

            if (number < 0) {
                number = 0;
            }

            document.getElementById("fileInputDiv" + inputIndex).parentNode.removeChild(document.getElementById("fileInputDiv" + inputIndex));
        } catch (e) {
            errorNode = true;
        }

        try {
            $("li[id^='fileNameDiv" + inputIndex + "']").remove();
            //document.getElementById("fileNameDiv" + i).parentNode.removeChild(document.getElementById("fileNameDiv" + i));
        } catch (e) {
            errorNode = true;
        }
    }


    // 根据fileInputDiv的后缀数字获取它在数组中的位置
    function getIndex(index) {
        var name = 'fileNameDiv' + index;
        var container = document.getElementById("fileNameDiv");
        var children = container.getElementsByTagName('div');
        var len = children.length;
        var result = 0;
        for (i = 0; i < len; i++) {
            var element = children[i];
            result++;
            if (name == element.id) {
                return result;
            }

        }
        return -1;
    }


    function cbmthod(type, str) {
        if (type == 0) {
            var att = $.parseJSON(str)[0];
            reAtts.add(att);
            fileurls = fileurls + "," + att.fileUrl;
            <c:if test="${(empty param.targetAction and empty param.callMethod )  or (not empty param.callMethod and 'false' eq param.takeOver)}" >
            <c:if test="${empty param.attachmentTrId}" >
            if (callback) {
                callback(att.type, att.filename, att.mimeType, att.createdate, att.size, att.fileUrl, null, null, att.extension, att.icon, att.v);
            } else {
                //parent.addAttachment(att.type,  att.filename, att.mimeType,att.createdate, att.size, att.fileUrl,null,null,att.extension,att.icon,att.v);
                addAttachment(att.type, att.filename, att.mimeType, att.createdate, att.size, att.fileUrl, null, null, att.extension, att.icon, att.v);
            }
            </c:if>
            <c:if test="${not empty param.attachmentTrId}" >
            addAttachmentPoi(att.type, att.filename, att.mimeType, att.createdate, att.size, att.fileUrl, null, null, att.extension, att.icon, '${ctp:toHTML(param.attachmentTrId)}', null, null, null, null, '${ctp:escapeJavascript(param.embedInput)}', null, null, att.v, null, '${ ctp:escapeJavascript(param.isShowImg)}');
            </c:if>
            </c:if>

        }

        var i = str.indexOf('{');
        if (i == 0) {
            geniusNum++;
            var str1 = str.split('}');
            var str2 = str.substr(0, str.lastIndexOf('}') + 1);//str1[0] + '}';
            var str2 = str2.substr(0, str2.lastIndexOf('}') + 1);
            var strJsion = '(' + str2 + ')';
            var f = eval(strJsion);
            //parent.addAttachment(f.type, f.filename, f.mimeType, f.createDate, f.size, f.fileUrl,null,null,f.extension,f.icon);
            addAttachment(f.type, f.filename, f.mimeType, f.createDate, f.size, f.fileUrl, null, null, f.extension, f.icon);
            if (geniusNum == number) {
                //parent.close();
                windowClose();
            }

            str = str1[1];

        }

        if (type == 1) {
            var percent = document.getElementById("file_percent");
            var fileName = str.substr(str.lastIndexOf("\\") + 1);
            var genNum = geniusNum + 1;
            percent.innerText = $.i18n('upload.cbmthod.msg1') + number + $.i18n('upload.cbmthod.msg2') + genNum + $.i18n('upload.cbmthod.msg3') + getLimitLength(fileName, 50);
        } else if (type == -1) {
            alert($.i18n('upload.cbmthod.msg4'));
            document.getElementById("b1").disabled = false;
        } else if (type == 2) {
            var percent = document.getElementById("percent");
            var pcent = str.split('/');
            percent.innerText = $.i18n('upload.cbmthod.msg5') + pcent[0] + $.i18n('upload.cbmthod.msg6') + pcent[1] + "KB";
        } else if (type == 100) {
            <c:if test="${not empty param.targetAction or not empty param.callMethod}" >
            var repeat = "0";
            try {
                var repeats = document.getElementsByName("repeat");
                for (var i = 0; i < repeats.length; i++) {
                    if (repeats[i].checked) {
                        repeat = repeats[i].value;
                        break;
                    } else {
                        continue;
                    }
                }
            } catch (e) {
            }
            <c:if test="${'true' ne param.firstSave}">
            callforward(fileurls.substring(1), repeat);
            </c:if>
            <c:if test="${'true' eq param.firstSave}">
            callforward(reAtts, repeat);
            </c:if>
            </c:if>

            //parent.window.close();
            windowClose();
        }
        return "true";
    }

    function onUpload() {
        try {
            //OA-99062	处理协同上传附件：上传没有进度条--添加按钮可以点击--多点击几次IE停止运行
            if ($('.file_click')) {
                $('.file_click').css('display', 'none');
            }
            //如果用户没有选择文件，什么都不做
            if (number != 0) {
                //document.getElementById("b1").disabled = false;
                $("#b1").disable();
                SetServerName_OnClick();
                show();
                var percent = document.getElementById("result");
                percent.innerText = "";
                var UFIDA_Upload1 = document.getElementById("UFIDA_Upload1");
                UFIDA_Upload1.height = '20';
                var ret = UFIDA_Upload1.StartUpload();
                if (ret == '-100') {
                    window.close();
                }
            } else {//v5.1要求有提示
                $('.file_click').css('display', 'block');
                alert("${ctp:i18n_1('fileupload.selectfile.label',maxSize)}");
                return;
            }
        } catch (e) {
            alert(e);
        }
    }

    /**
     * 显示表单导入规则说明
     */
    function importExplain() {
        var current_dialog = $.dialog({
            url: _ctxPath + "/form/formula.do?method=formulaHelp&formType=2",
            title: $.i18n('form.formulahelp.label'),
            width: 600,
            height: 450,
            targetWindow: getCtpTop(),
            buttons: [{
                text: $.i18n('form.trigger.triggerSet.confirm.label'), id: "sure",
                handler: function () {
                    current_dialog.close();
                }
            }]
        });
    }

    </script>
</head>

<body onkeydown="listenerKeyPress()" style="height: 100%;overflow: hidden" class="page_color">
<form style="height: 100%;display: block;" enctype="multipart/form-data" name="uploadForm" method="post" id="form_upload"
      action="${pageContext.request.contextPath}/fileUpload.do?method=processUpload&maxSize=<%=maxSize%>${ctp:csrfSuffix()}" target="uploadIframe">
    <input type="hidden" id="type" name="type" value="<%=Functions.toHTML(type)%>">
    <input type="hidden" name="extensions" id="extensions" value="<%=Functions.toHTML(extensions)%>">
    <input type="hidden" name="applicationCategory" id="applicationCategory" value="<%=Functions.toHTML(applicationCategory)%>">
    <input type="hidden" name="destDirectory" id="destDirectory" value="<%=Functions.toHTML(destDirectory)%>">
    <input type="hidden" name="destFilename" id="destFilename" value="<%=Functions.toHTML(destFilename)%>">
    <input type="hidden" name="maxSize" id="maxSize" value="<%=Functions.toHTML(maxSize)%>">
    <input type="hidden" name="isEncrypt" id="isEncrypt" value="<%=Functions.toHTML(isEncrypt)%>">
    <input type="hidden" id="form_action" value="/seeyon/fileUpload.do">
    <input type="hidden" id="fileStr" value="">
    <c:if test="${not empty param.targetAction }">
        <input type="hidden" name="targetAction" id="targetAction" value="${ctp:toHTML(param.targetAction)}">
    </c:if>
    <c:if test="${not empty param.callMethod }">
        <input type="hidden" name="callMethod" id="callMethod" value="${ctp:toHTML(param.callMethod)}">
    </c:if>
    <c:if test="${not empty param.attachmentTrId }">
        <input type="hidden" name="attachmentTrId" id="attachmentTrId" value="${ctp:toHTML(param.attachmentTrId)}">
    </c:if>
    <c:if test="${not empty param.firstSave }">
        <input type="hidden" name="firstSave" id="firstSave" value="${ctp:toHTML(param.firstSave)}">
    </c:if>
    <c:if test="${not empty param.takeOver }">
        <input type="hidden" name="takeOver" id="takeOver" value="${ctp:toHTML(param.takeOver)}">
    </c:if>
    <c:if test="${not empty param.embedInput }">
        <input type="hidden" name="embedInput" id="takeOver" value="${ctp:toHTML(param.embedInput)}">
    </c:if>
    <c:if test="${not empty param.callback }">
        <input type="hidden" name="callback" id="callback" value="${ctp:toHTML(param.callback)}">
    </c:if>
    <c:if test="${not empty param.isShowImg }">
        <input type="hidden" name="isShowImg" id="isShowImg" value="${ctp:toHTML(param.isShowImg)}">
    </c:if>
    <c:if test="isA8geniusAdded">
        <input type="hidden" name="isA8geniusAdded" id="isA8geniusAdded" value="${ctp:toHTML(param.isA8geniusAdded)}">
    </c:if>

    <table class="popupTitleRight font_size12" width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td align="top" style="padding:0;">
                <div id="scroll_div" style="height:188px;overflow-y:auto;overflow-x:hidden;">
                    <table width="385" border="0" cellspacing="0" cellpadding="0">
                        <c:if test="${!empty param.selectRepeatSkipOrCover}">
                            <tr>
                                <td height="30" class="padding_10">
                                        ${ctp:i18n(empty param.selectRepeat ? 'fileupload.page.repeat' : param.selectRepeat)}&nbsp;
                                    <label for="skip">
                                        <input type="radio" id="skip" name="repeat" value="0" checked="checked">
                                            ${ctp:i18n(empty param.selectRepeatSkip ? 'fileupload.page.skip' : param.selectRepeatSkip)}
                                    </label>
                                    <label for="cover">
                                        <input type="radio" id="cover" name="repeat" value="1">
                                            ${ctp:i18n(empty param.selectRepeatCover ? 'fileupload.page.cover' : param.selectRepeatCover)}
                                    </label>
                                    <c:if test="${!empty param.selectRepeatOther}">
                                        <!-- cap4 导入表单用的选项 -->
                                        <label for="other">
                                            <input type="radio" id="other" name="repeat" value="2">
                                                ${ctp:i18n(param.selectRepeatOther)}
                                        </label>
                                    </c:if>
                                    <c:if test="${!empty param.selectRepeatOther1}">
                                        <!-- HR薪资导入传入用的选项 -->
                                        <label for="other1">
                                            <input type="radio" id="other1" name="repeat" value="3">
                                                ${ctp:i18n(param.selectRepeatOther1)}
                                        </label>
                                    </c:if>
                                    <c:if test="${!empty param.importExplain}">
                                        <a href="#" onclick="importExplain()" id="importExplain" style="margin-left: 100px;">
                                            [${ctp:i18n('form.base.importexplain.label')}]
                                        </a>
                                    </c:if>
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td height="20">
                                <div class="margin_l_20">${ctp:i18n_1("fileupload.selectfile.label",maxSize)}</div>
                            </td>
                        </tr>
                        <tr>
                            <td id="upload1" class="bg-advance-middel padding_10" style="vertical-align: top; padding-left:20px; padding-right: 0;">

                                <div class="file_box">
                                    <div id="fileInputDiv" style="height: 30px;float:right;width:92px;margin-left:5px;">
                                        <div id="fileInputDiv1" style="" class="file_unload clearfix">
                                            <a class="common_button common_button_icon file_click" href="###"><em class="ico16 affix_16"></em>${ctp:i18n("fileupload.addfile.label")}
                                                <%--zhou:2019-11-13 对文件的类型添加限制--%>
                                                <input type="file" multiple name="file1"
                                                       accept=".xsn,.syz,.sfp,.txt,.doc,docx,.xls,.xlsx,.bmp,.jpg,.png,.tif,.gif,.pcx,.tga,.exif,.fpx,.svg,.psd,.cdr,.pcd,.dxf,.ufo,.eps,.ai,.raw,.WMF,.webp"
                                                       size="51" id="file1" onchange="addNextInputH5(this)" onkeydown="return false" onkeypress="return false"/><br/>
                                                <%--                                                <input type="file" multiple name="file1"  size="51" id="file1" onchange="addNextInputH5(this)" onkeydown="return false" onkeypress="return false"/><br/>--%>
                                            </a>

                                        </div>
                                    </div>

                                    <ul id="fileNameDiv" class="file_select  border_all clearfix bg_color_white"
                                        style="float:left;width:250px;padding:0px 5px;margin:0px;${!empty param.selectRepeatSkipOrCover?'height: 28px;overflow: hidden;':''}">
                                        <li><a>&nbsp;</a></li>
                                    </ul>

                                </div>


                            </td>
                        </tr>
                        <tr id="uploadprocee" style="display:none;">
                            <td style="background:#fff;" align="center" class="bg-advance-middel padding_10">
                                <table width="240" height="45" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td align='center' height='30' valign='bottom'>${ctp:i18n("fileupload.uploading.label")}...</td>
                                    </tr>
                                    <tr>
                                        <td align='center'><span class='process'>&nbsp;</span></td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
        </tr>
        <tr>
            <td align="right" valign="top" class="bg-advance-bottom padding_lr_10 border_t">
                <a id="b1" class="button-default_emphasize" style="cursor: pointer;vertical-align: top;" onclick="checksH5()">${ctp:i18n('common.button.ok.label')}</a><a id="b2"
                                                                                                                                                                          class="margin_l_10 button-default-2 margin_r_10"
                                                                                                                                                                          style="cursor: pointer; vertical-align: top;"
                                                                                                                                                                          onclick="windowClose()">${ctp:i18n('common.button.cancel.label')}</a>
            </td>
        </tr>
    </table>

</form>
<iframe name="uploadIframe" frameborder="0" height="0" width="0" class="hidden" style="display:none" scrolling="no" marginheight="0" marginwidth="0"></iframe>
</body>
</html>
