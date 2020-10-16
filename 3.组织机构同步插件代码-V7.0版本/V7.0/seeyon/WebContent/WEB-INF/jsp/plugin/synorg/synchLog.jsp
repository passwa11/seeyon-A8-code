<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>同步日志</title>
    <script type="text/javascript" src="${path}/ajax.do?managerName=syncLogManager"></script>
    <script type="text/javascript" charset="UTF-8" src="${path}/apps_res/plugin/synorg/js/synchLog.js${ctp:resSuffix()}"></script>
    <script type="text/javascript">
		//导航菜单
		getA8Top().showLocation("<span class=\"nowLocation_ico\"><img src=\"/seeyon/main/skin/frame/harmony/menuIcon/personal.png\"></span><span class=\"nowLocation_content\"><a style=\"cursor:default\" >组织同步插件</a> > <a href=\"javascript:void(0)\" class=\"hand\" onclick=\"showMenu(\'/seeyon/synorgController.do?method=synchLog\')\">同步日志</a>")
    </script>
</head>
<body>
    <div id='layout'>
        <div class="layout_north bg_color" id="north">
            <!-- 显示菜单栏 -->
            <div id="toolbars" class="f0f0f0"> </div>  
        </div>
        <div class="layout_center over_hidden" id="center">
            <table  class="flexme3" id="logList"></table>
        </div>
    </div>
</body>
</html>
