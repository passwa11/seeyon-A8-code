<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>中间库数据</title>
    <script type="text/javascript">
		//导航菜单
		getA8Top().showLocation("<span class=\"nowLocation_ico\"><img src=\"/seeyon/main/skin/frame/harmony/menuIcon/personal.png\"></span><span class=\"nowLocation_content\"><a style=\"cursor:default\" >组织同步插件</a> > <a href=\"javascript:void(0)\" class=\"hand\" onclick=\"showMenu(\'/seeyon/synorgController.do?method=synchData\')\">中间库数据</a>")
    </script>
</head>
<body>
    <div id="tabs" class="comp" comp="type:'tab',width:600,height:200,parentId:'tabs'">
    <div id="tabs_head" class="common_tabs clearfix">
        <ul class="left">
        	<li class="current"><a hidefocus="true" href="javascript:void(0)" tgt="unit"><span class="ico16 unit_16"></span>单位</a></li>
            <li ><a hidefocus="true" href="javascript:void(0)" tgt="dept"><span class="ico16 department_16"></span>部门</a></li>
            <li><a hidefocus="true" href="javascript:void(0)" tgt="post"><span class="ico16 radio_post_16"></span>岗位</a></li>
            <li><a hidefocus="true" href="javascript:void(0)" tgt="level"><span class="ico16 radio_level_16"></span>职级</a></li>
            <li><a hidefocus="true" href="javascript:void(0)" tgt="member"><span class="ico16 staff_16"></span>人员</a></li>
        </ul>
    </div>
    <div id="tabs_body" class="common_tabs_body ">
    	<iframe id="unit" width="100%" src="/seeyon/synorgController.do?method=synchMidDB&type=Unit" frameborder="no" border="0"></iframe>
        <iframe id="dept" width="100%" src="/seeyon/synorgController.do?method=synchMidDB&type=Department" frameborder="no" border="0"></iframe>
        <iframe id="post" width="100%" src="/seeyon/synorgController.do?method=synchMidDB&type=Post" frameborder="no" border="0"></iframe>
        <iframe id="level" width="100%" src="/seeyon/synorgController.do?method=synchMidDB&type=Level" frameborder="no" border="0"></iframe>
        <iframe id="member" width="100%" src="/seeyon/synorgController.do?method=synchMemberData" frameborder="no" border="0"></iframe>
    </div>
</div>
</body>
</html>
