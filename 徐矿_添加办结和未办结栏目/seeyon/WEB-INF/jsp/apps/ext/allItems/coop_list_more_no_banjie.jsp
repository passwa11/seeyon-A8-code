<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta http-equiv="Expires" content="0" />
    <title>协同更多办结</title>
    <script type="text/javascript" src="${path}/ajax.do?managerName=xkjtManager"></script>
    <script type="text/javascript" charset="UTF-8" src="${path}/apps_res/xkjtBanjie/coop_no_ban_jie_more.js${ctp:resSuffix()}"></script>
    <script type="text/javascript">
        //项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 start
        var templetIds = "${templetIds}";
        //项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 end
    </script>
</head>
<body>
<div id="layout">
    <div class="layout_north bg_color" id="north">
        <div class="common_crumbs">
            <span class="margin_r_10">当前位置:</span><a href="javascript:void(0)">所有办结</a>
        </div>
    </div>

    <div class="layout_center over_hidden" id="center">
        <table class="flexme3" style="display: none" id="banJieTable"></table>
    </div>
</div>
</body>

</html>
