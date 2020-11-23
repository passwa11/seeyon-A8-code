<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js" "></script>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/zTree/js/jquery.ztree.js" "></script>
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/zTree/css/zTreeStyle/zTreeStyle.css" ">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/layui.css" ">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/selection_infomation.css" ">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/selection_infomation-lt.css" ">
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/layui/layui.all.js" "></script>
    <style type="text/css">
        #nav {
            float: left;
            width: 19%;
            height: 90%;
            background-color: #fdfffd;
        }

        .content {
            float: left;
            width: 37%;
            height: 90%;
            background-color: #fdfffd;
        }

        .midd {
            float: left;
            width: 5%;
            height: 90%;
            background-color: #fdfffd;
        }
    </style>
</head>
<body>
<div class="content_wrap">
    <div id="nav" style="border: 1px solid #90d7bb;">
        <div class="layui-card" style="margin-top: 0px">
            <div class="layui-table-header">
                <div style="line-height: 30px;height: 30px;padding-left: 20px;">
                    机构树
                </div>
            </div>
        </div>
        <ul id="treeDemo" class="ztree"></ul>
    </div>
    <div class="content">
        <div style="border: 1px solid #90d7bb;width: 95%;height: 100%;margin-left: 10px;">
            <div class="layui-card" style="margin-top: 0px">
                <div class="layui-table-header">
                    <div style="line-height: 30px;height: 30px;padding-left: 20px;">
                        人员信息
                    </div>
                </div>
            </div>
            <div id="ishow" class="layui-form-item" style="margin-top: 20px;margin-left: 20px;">
                <div class="layui-input-inline">
                    <input type="text" id="memberInput" name="username" lay-verify="title"
                           autocomplete="off"
                           placeholder="姓名" class="layui-input">
                </div>
                <button class="layui-btn layui-btn-primary layui-btn-radius" id="queryMember">查询</button>
                <button class="layui-btn layui-btn-primary layui-btn-radius" id="queryallMember">显示全部</button>
            </div>
            <table id="memberTable" lay-filter="memberTableFilter"></table>
        </div>
    </div>
    <div class="midd">
        <div class="" style="margin-top: 200px">
            <button class="layui-btn layui-btn-radius  layui-btn-sm layui-btn-primary" id="sureSelect">
                &nbsp;<i class="layui-icon layui-icon-next"></i>
            </button>
        </div>
    </div>
    <div class="content">
        <div style="border: 1px solid #90d7bb;width: 95%;height: 100%;">
            <div class="layui-card" style="margin-top: 0px">
                <div class="layui-table-header">
                    <div style="line-height: 30px;height: 30px;padding-left: 20px;">
                        已选择人员
                    </div>
                </div>
            </div>
            <!-- 已选择信息 -->
            <dl class="selected-info" style="height: 550px;"></dl>
            <div style="text-align: center" id="btnShow">
                <div style="margin-top: 20px;"></div>
                <div class="layui-input-inline">
                    <button class="layui-btn layui-btn-danger layui-btn-radius" id="selected_info_reset">清空</button>
                    <button class="layui-btn  layui-btn-radius" id="saveMembers">保存</button>
                    <div id="idInfo"></div>
                    <div id="nameInfo"></div>
                    <div id="deptInfo"></div>
                </div>
            </div>
        </div>
    </div>
    <input type="hidden" id="deptid"/>
</div>

</body>
</html>
