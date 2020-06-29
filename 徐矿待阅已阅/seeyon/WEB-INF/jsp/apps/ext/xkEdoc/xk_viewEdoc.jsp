<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%--<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>--%>
<%--<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>--%>
<%--<%@ taglib uri="http://v3x.seeyon.com/taglib/core" prefix="v3x" %>--%>
<%--<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>--%>
<%--<%@ taglib uri="http://v3x.seeyon.com/bridges/spring-portlet-html" prefix="html" %>--%>
<%--<%@ taglib prefix="ctp" uri="http://www.seeyon.com/ctp" %>--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Insert title here</title>
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/layui.css" />">
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/V3X.js" />"></script>
    <script type="text/javascript" charset="UTF-8"
            src="<c:url value="/common/js/jquery.js${v3x:resSuffix()}" />"></script>
    <script type="text/javascript" src="<c:url value="/common/layui/layui.all.js"/>"></script>
    <script type="text/javascript">

    </script>
    <%--    <script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/edoc/js/edocSummary.js${v3x:resSuffix()}" />"></script>--%>
    <style type="text/css">
        .layui-form-label2 {
            float: left;
            display: block;
            padding: 9px 15px;
            /*width: 80px;*/
            font-weight: 400;
            line-height: 20px;
            text-align: left;
        }
    </style>
</head>
<body>


<div class="layui-row">

    <div class="layui-col-md8 layui-col-md-offset2">

        <input id="summaryId" type="hidden" value="${summary.id}">
        <input id="content" type="hidden" value="${content}">
        <input id="time" type="hidden" value="${summary.createTime}">
        <input id="isQuickSend" type="hidden" value="${summary.isQuickSend}">
        <input id="suject" type="hidden" value="${summary.subject}">
        <input id="suffix" type="hidden" value="${suffix}">
        <fieldset class="layui-elem-field layui-field-title" style="margin-top: 30px;">
            <legend>文单信息</legend>
        </fieldset>
        <form class="layui-form" action="" style="margin-top:30px;">
            <div class="layui-form-item">
                <label class="layui-form-label">发文标题</label>
                <div class="layui-input-block">
                    <label class="layui-form-label2">
                        <h3 style="color: #322b80">
                            <a href="javascript:void(0)" onclick="opentPdf();" style="color: #456dcd" title="点击标题查看PDF">
                                ${summary.subject}
                            </a>
                        </h3>

                    </label>

                </div>
            </div>

            <div class="layui-form-item">
                <div class="layui-row">
                    <div class="layui-col-md6">
                        <label class="layui-form-label">发文编号</label>
                        <div class="layui-input-block">
                            <input type="tel" lay-verify="" autocomplete="off" class="layui-input" disabled
                                   value="${summary.docMark}">
                        </div>
                    </div>
                    <div class="layui-col-md6">
                        <label class="layui-form-label">文件类型</label>
                        <div class="layui-input-block">
                            <c:choose>
                                <c:when test="${summary.edocType == 0}">
                                    <input type="text" lay-verify="" autocomplete="off" class="layui-input" value="发文" disabled>
                                </c:when>
                                <c:when test="${summary.edocType == 1}">
                                    <input type="text" lay-verify="" autocomplete="off" class="layui-input" value="收文" disabled>
                                </c:when>
                            </c:choose>

                        </div>
                    </div>
                </div>
            </div>
            <div class="layui-form-item">
                <div class="layui-row">
                    <div class="layui-col-md6">
                        <label class="layui-form-label">发文单位</label>
                        <div class="layui-input-block">
                            <input type="tel" lay-verify="" autocomplete="off" class="layui-input" disabled value="${summary.sendDepartment}">
                        </div>
                    </div>
                    <div class="layui-col-md6">
                        <label class="layui-form-label">发文时间</label>
                        <div class="layui-input-block">
                            <input type="text" lay-verify="" autocomplete="off" class="layui-input" disabled
                                   value="${createTime}">
                        </div>
                    </div>
                </div>
            </div>
            <div class="layui-form-item">
                <div class="layui-row">
                    <div class="layui-col-md6">
                        <label class="layui-form-label">发文人员</label>
                        <div class="layui-input-block">
                            <input type="tel" lay-verify="" autocomplete="off" class="layui-input" disabled
                                   value="${summary.createPerson}">
                        </div>
                    </div>
                    <%--                    <div class="layui-col-md6">--%>
                    <%--                        <label class="layui-form-label">拟稿日期</label>--%>
                    <%--                        <div class="layui-input-block">--%>
                    <%--                            <input type="text" lay-verify="" autocomplete="off" class="layui-input" disabled--%>
                    <%--                                   value="${createTime}">--%>
                    <%--                        </div>--%>
                    <%--                    </div>--%>
                </div>
            </div>
            <div class="layui-form-item">
                <div class="layui-row">
                    <div class="layui-col-md6">
                        <label class="layui-form-label">签发人员</label>
                        <div class="layui-input-block">
                            <input type="tel" lay-verify="" autocomplete="off" class="layui-input" disabled>
                        </div>
                    </div>
                    <div class="layui-col-md6">
                        <label class="layui-form-label">签发日期</label>
                        <div class="layui-input-block">
                            <input type="text" lay-verify="" autocomplete="off" class="layui-input" disabled>
                        </div>
                    </div>
                </div>
            </div>
            <div class="layui-form-item">
                <div class="layui-row">
                    <div class="layui-col-md6">
                        <label class="layui-form-label">紧急程度</label>
                        <div class="layui-input-block">
                            <c:choose>
                                <c:when test="${summary.secretLevel=='1'}">
                                    <input type="text" class="layui-input" value="普通" disabled>
                                </c:when>
                                <c:when test="${summary.secretLevel=='2'}">
                                    <input type="text" class="layui-input" value="秘密" disabled>
                                </c:when>
                                <c:when test="${summary.secretLevel=='3'}">
                                    <input type="text" class="layui-input" value="机密" disabled>
                                </c:when>
                                <c:when test="${summary.secretLevel=='4'}">
                                    <input type="text" class="layui-input" value="绝密" disabled>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                    <div class="layui-col-md6">
                        <label class="layui-form-label">密级等级</label>
                        <div class="layui-input-block">
                            <c:choose>
                                <c:when test="${summary.urgentLevel=='1'}">
                                    <input type="text" value="普通" class="layui-input" disabled>
                                </c:when>
                                <c:when test="${summary.urgentLevel=='2'}">
                                    <input type="text" value="平急" class="layui-input" disabled>
                                </c:when>
                                <c:when test="${summary.urgentLevel=='3'}">
                                    <input type="text" value="加急" class="layui-input" disabled>
                                </c:when>
                                <c:when test="${summary.urgentLevel=='4'}">
                                    <input type="text" value="特级" class="layui-input" disabled>
                                </c:when>
                            </c:choose>

                        </div>
                    </div>
                </div>
            </div>

        </form>
        <fieldset class="layui-elem-field layui-field-title" style="margin-top: 30px;">
            <legend>附件列表</legend>
        </fieldset>
        <table id="fileList" lay-filter="fileListFilter"></table>
    </div>
    <iframe id="downloadFileFrame" src="" class="" style="display: none"></iframe>
</div>
<script type="text/javascript">
    var v3x = new V3X();

    var param = {
        id: '${daiYueId}',
        status: 12
    };
    var dyId = '${daiYueId}';
    if (dyId != '') {
        var requestCaller = new XMLHttpRequestCaller(this, "xkjtManager", "updateXkjtLeaderDaiYueByCondition", false);
        requestCaller.addParameter(1, "String", param.id);
        requestCaller.addParameter(2, "String", param.status);
        var bool = requestCaller.serviceRequest();
    }

    function opentPdf() {
        // var time = $("#time").val();
        // var arr = time.split(" ");
        // var isQuickSend = $("#isQuickSend").val();
        // var summaryId = $("#summaryId").val();
        // var _url = '/seeyon/ext/xkEdoc.do?method=pdfView&content=' + $("#content").val() + "&time=" + arr[0] + "&isQuickSend=" + isQuickSend + "&summaryId=" + summaryId;
        // var rv = v3x.openWindow({
        //     url: _url,
        //     dialogType: 'open',
        //     workSpace: 'yes'
        // });
        var fileUrl = $("#content").val();
        var time = $("#time").val();
        var arr = time.split(" ");
        var fileName = $("#suject").val();
        var isQuickSend = $("#isQuickSend").val();
        var summaryId = $("#summaryId").val();
        var suffix = $("#suffix").val();
        var url = "/seeyon/ext/xkEdoc.do?method=downloadfile&type=2&fileId=" + fileUrl + "&createDate=" + arr[0] + "&filename=" + encodeURI(fileName + suffix)
            + "&isQuickSend=" + isQuickSend + "&summaryId=" + summaryId;
        $("#downloadFileFrame").attr("src", url);
    }


    layui.use(['table', 'layer', 'element'], function () {
        var $ = layui.jquery;
        var layer = layui.layer;
        var table = layui.table;

        table.render({
            id: 'fileListId'
            ,
            elem: '#fileList'
            ,
            url: '/seeyon/ext/xkEdoc.do?method=getFileList&summaryId=' + $("#summaryId").val()
            // , height: 400
            ,
            page: false //开启分页
            ,
            cols: [[ //表头
                // {type: 'checkbox'},
                {field: 'filename', title: '附件名称', width: '60%', templet: "#toolBar3"},
                {field: 'size', title: '大小', width: '10%'},
            ]],
        });

        //监听工具条
        table.on('tool(fileListFilter)', function (obj) {
            var data = obj.data; //获得当前行数据
            debugger;
            // var v = data.v;
            var fileUrl = data.filepath;
            var time = new Date(data.createdate);
            var year = time.getFullYear();
            var month = time.getMonth() + 1;
            if (9 >= month) {
                month = "0" + month;
            }

            var day = time.getDate();
            if (9 >= day) {
                day = "0" + day;
            }
            var uploadTime = year + "-" + month + "-" + day;
            var fileName = data.filename;
            var isQuickSend = $("#isQuickSend").val();
            var summaryId = $("#summaryId").val();
            if (obj.event === 'downloadFile') {
                if (data.mimeType == 'edoc') {
                    var url="/seeyon/edocController.do?method=detailIFrame&from=Done&affairId="+data.filepath+'';
                    window.open(url,"_blank");
                } else {
                    var url = "/seeyon/ext/xkEdoc.do?method=downloadfile&type=1&fileId=" + fileUrl + "&createDate=" + uploadTime + "&filename=" + encodeURI(fileName)
                        + "&isQuickSend=" + isQuickSend + "&summaryId=" + summaryId;

                    $("#downloadFileFrame").attr("src", url);
                }
            }
        });
    });
</script>
<script type="text/html" id="toolBar3">

    <a class="" lay-event="downloadFile">{{ d.filename}}</a>

</script>
</body>
</html>
