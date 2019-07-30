<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>中间库数据</title>
    <script>
    var type = "${type}";
    </script>
    <script type="text/javascript" src="${path}/ajax.do?managerName=syncDataManager"></script>
    <script type="text/javascript" charset="UTF-8" src="${path}/apps_res/plugin/synorg/js/synchMidDB.js${ctp:resSuffix()}"></script>
</head>
<body>
    <div id='layout'>
            <div class="layout_north bg_color" id="north">
            <!-- 显示菜单栏 -->
            <div id="toolbars" class="f0f0f0"> </div>  
        </div>
        <div class="layout_center over_hidden" id="center">
            <table  class="flexme3" id="dataList"></table>
        </div>
    </div>
</body>
</html>
