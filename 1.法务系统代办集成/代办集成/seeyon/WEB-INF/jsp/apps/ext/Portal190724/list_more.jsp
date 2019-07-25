<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <title>更多</title>
    <script type="text/javascript" src="${path}/ajax.do?managerName=portal190724Manager"></script>
    <%--    <script type="text/javascript" charset="UTF-8" src="${path}/apps_res/xkjt/dai_yue_more.js${ctp:resSuffix()}"></script>--%>
</head>
<body>
<h1>tttttttttttt</h1>
<div id="layout">
    <div class="layout_north bg_color" id="north">
        <div class="common_crumbs">
            <span class="margin_r_10">当前位置:</span><a href="javascript:;">代办</a>
        </div>
    </div>
    <div class="layout_center over_hidden" id="center">
        <table class="flexme3" style="display: none" id="daibanTable"></table>
    </div>
</div>
</body>
<script>
    $(function () {

        //表格加载
        var grid = $('#daibanTable').ajaxgrid({
            colModel: [
                {
                    display: "公文标题",
                    name: 'taskName',
                    sortable: true,
                    width: '31%'
                }, {
                    display: "发起者",
                    name: 'createUser',
                    sortable: true,
                    width: '31%'
                }, {
                    display: "创建时间",
                    name: 'beginTime',
                    sortable: true,
                    width: '31%'
                }],

            click: openDetail,
            render: rend,
            height: 200,
            showTableToggleBtn: true,
            parentId: 'center',
            vChange: true,
            vChangeParam: {
                overflow: "hidden",
                autoResize: true
            },
            isHaveIframe: true,
            slideToggleBtn: true,
            managerName: "portal190724Manager",
            managerMethod: "findMoreLaw"
        });
        $("#daibanTable").ajaxgridLoad();
    });

    function rend(txt, data, r, c) {
        return txt;
    }

    function openDetail() {
        alert(1);
    }
</script>
</html>
