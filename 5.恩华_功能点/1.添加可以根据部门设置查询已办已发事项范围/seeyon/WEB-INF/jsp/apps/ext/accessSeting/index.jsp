<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <%@ include file="../../../common/common.jsp" %>
    <link type="text/css" rel="stylesheet" href="<c:url value="/common/js/xtree/xtree.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/zTree/css/zTreeStyle/zTreeStyle.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/layui.css" />">
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/layui/layui.all.js" />"></script>

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
    <div id="nav" style="border: 1px solid #90d7bb;margin-left: 10px;">
        <ul id="treeDemo" class="ztree"></ul>
    </div>
    <div style="margin-left: 10px;border: 1px solid #90d7bb;float: left;width: 72%;height: 90%;background-color: #fdfffd;">
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
            <button class="common_button" id="queryMember">查询</button>
            <button class="common_button" id="queryallMember">显示全部</button>
            <button class="common_button common_button_emphasize" id="setConfig">设置</button>
        </div>
        <table id="memberTable" lay-filter="memberTableFilter"></table>
    </div>


</div>

</body>
<script type="text/javascript">
    layui.use(['table', 'layer', 'element'], function () {

        var layer = layui.layer;
        var table = layui.table;
        var selectedMember = [];//选择信息数组

        table.render({
            id: 'memberTableId'
            , elem: '#memberTable'
            , url: '/seeyon/ext/accessSetting.do?method=getMemberByDepartmentId'
            , height: 550
            , page: false //开启分页
            , cols: [[ //表头
                {type: 'checkbox'},
                {field: 'name', title: '姓名', width: '20%', sort: true},
                {field: 'deptname', title: '部门', width: '30%', sort: true},
                {field: 'levelName', title: '职务', width: '20%'},
                {field: 'dayNum', title: '最近天数', width: '20%', sort: true}
            ]]
            , where: {
                name: ""
            }
        });
        //打开设置页面
        $('#setConfig').on('click', function () {
            console.log(table.checkStatus('memberTableId').data);
            layer.open({
                type: 2,
                id: 'setConfigId',
                title: '最近数据显示设置',
                shadeClose: false,
                maxmin: false, //开启最大化最小化按钮
                area: ['50%', '50%'],
                content: '/seeyon/ext/accessSetting.do?method=setting',
                success: function (layero, index) {
                }
            });
        });

        var setting = {
            callback: {
                onClick: zTreeOnClick
            }
        };

        function zTreeOnClick(event, treeId, treeNode) {
            var id = treeNode.id;
            var name = treeNode.name;
            $("#deptid").val(id);
            $("#deptname").val(name);

            table.reload('memberTableId', {
                where: {
                    departmentId: id.toString(),
                    name: ""
                }
            });

            // $.post("/seeyon/ext/accessSetting.do?method=getDepartmentRange", {departmentId: id + ""}, function (ref) {
            //     if (ref.data != null) {
            //         $("#startTime").val(ref.data.startTime);
            //         $("#endTime").val(ref.data.endTime);
            //     } else {
            //         $("#startTime").val("");
            //         $("#endTime").val("");
            //     }
            // });

        }

        $(function () {
            $.fn.zTree.init($("#treeDemo"), setting, ${list});

            $("#reset").on('click', function () {
                $("#startTime").val("");
                $("#endTime").val("");
                $("#deptid").val("");
                $("#deptname").val("");
            });

            $("#saveRange").on('click', function () {
                var obj = {};
                obj['deptmentId'] = $("#deptid").val() + "";
                obj['departmentName'] = $("#deptname").val();
                obj['startTime'] = $("#startTime").val();
                obj['endTime'] = $("#endTime").val();
                $.post("/seeyon/ext/accessSetting.do?method=saveDepartmentViewTimeRange", obj, function (ref) {
                    if (ref.code == 0) {
                        $("#info").append("保存成功！");
                    } else {
                        $("#info").append("保存失败！");
                    }
                });
                setTimeout(function () {
                    $("#info").replaceWith("");
                }, 2000);
            });
        });
    });

</script>
</html>
