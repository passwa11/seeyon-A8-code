<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>人员同步数据</title>
    <script type="text/javascript" src="${path}/ajax.do?managerName=syncDataManager"></script>
    <script type="text/javascript" charset="UTF-8" src="${path}/apps_res/plugin/synorg/js/synchMemberData.js${ctp:resSuffix()}"></script>
</head>
<body>
    <div id='layout'>
        <div class="layout_center over_hidden" id="center">
            <table  class="flexme3" id="dataList"></table>
        </div>
    </div>
</body>
</html>
