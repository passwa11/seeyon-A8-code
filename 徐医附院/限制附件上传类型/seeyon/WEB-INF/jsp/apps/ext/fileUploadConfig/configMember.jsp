<%@ page contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js" />"></script>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/zTree/js/jquery.ztree.js" />"></script>
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/zTree/css/zTreeStyle/zTreeStyle.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/layui.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/selection_infomation.css" />">
    <link rel="stylesheet" type="text/css" href="<c:url value="/common/layui/css/selection_infomation-lt.css" />">
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

<SCRIPT type="text/javascript">
    layui.use(['table', 'layer', 'element'], function () {
        var layer = layui.layer;
        var table = layui.table;
        var selectedMember = [];//选择信息数组
        $("#btnShow").show();
        var setting = {
            callback: {
                onClick: zTreeOnClick
            }
        };
        table.render({
            id: 'memberTableId'
            , elem: '#memberTable'
            , url: '/seeyon/ext/fileUploadConfig.do?method=getMemberByDepartmentId'
            , height: 550
            , page: false //开启分页
            , cols: [[ //表头
                {type: 'checkbox'},
                {field: 'name', title: '姓名', width: '30%', sort: true},
                {field: 'levelName', title: '职务', width: '30%'}
            ]]
            , where: {
                departmentId: "",
                name: ""
            }
        });

        table.on('row(memberTableFilter)',function(rowData){
            var tr_obj = rowData.data;
            var obj = {};//添加成员对象
            obj["value"] = tr_obj.id;
            obj["text"] = tr_obj.name;
            obj["dept"] = tr_obj.levelName;
            if ($("dl.selected-info dd").length <= 0) {
                var option = '<dd lay-value="' + obj.value + '" lay-flag="jtld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                selectedMember.unshift(obj);//存储选择信息
                $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                $(".selected-info dd[lay-value=" + obj.value + "]").bind('click', function () {
                    $(this).remove();
                    //刷新选择信息
                    var t = {};
                    t["value"] = $(this).attr('lay-value');
                    t["text"] = $(this).attr('lay-name');
                    t["dept"] = $(this).attr('lay-dept');
                    selectedMember.splice($.inArray(t, selectedMember), 1);
                });
            } else {
                var selected = function () {//判断是否已选择了该人员
                    var flag = true;
                    $("dl.selected-info dd").each(function (i, item) {
                        if ($(item).attr("lay-value") == obj.value) {
                            layer.msg('已选择了[' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + ']', {time: 1500});
                            $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                            flag = false;//已经选择
                        }
                    });
                    return flag;
                }
                if (selected()) {
                    // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                    var option = '<dd lay-value="' + obj.value + '" lay-flag="jtld"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

                    $("dl.selected-info").prepend(option);
                    selectedMember.unshift(obj);//存储选择信息
                    $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                    $(".selected-info dd[lay-value=" + obj.value + "]").bind('click', function () {
                        $(this).remove();
                        //刷新选择信息
                        var t = {};
                        t["value"] = $(this).attr('lay-value');
                        t["text"] = $(this).attr('lay-name');
                        t["dept"] = $(this).attr('lay-dept');
                        selectedMember.splice($.inArray(t, selectedMember), 1);
                    });
                }
            }
        });

        function zTreeOnClick(event, treeId, treeNode) {
            var id = treeNode.id;
            $("#deptid").val(id);
            table.reload('memberTableId', {
                where: {
                    departmentId: id.toString(),
                    name: ""
                }
            });

        };
        var zNodes =${list};


        $("#queryMember").bind('click', function () {
            //执行重载
            table.reload('memberTableId', {
                where: {
                    departmentId: $("#deptid").val().toString(),
                    name: $("#memberInput").val()
                }
            });
        });
        $("#queryallMember").bind('click', function () {
            //执行重载
            $("#deptid").val("");
            table.reload('memberTableId', {
                where: {
                    departmentId: $("#deptid").val().toString(),
                    name: $("#memberInput").val()
                }
            });
        });
        $("#sureSelect").bind('click', function () {
            var member = layui.table.checkStatus("memberTableId");
            var mArrs = member.data;
            for (var i = 0; i < mArrs.length; i++) {
                var tr_obj = mArrs[i];
                var obj = {};//添加成员对象
                obj["value"] = tr_obj.id;
                obj["text"] = tr_obj.name;
                obj["dept"] = tr_obj.levelName;
                if ($("dl.selected-info dd").length <= 0) {
                    var option = '<dd lay-value="' + obj.value + '" lay-flag="jtld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                    $("dl.selected-info").prepend(option);
                    selectedMember.unshift(obj);//存储选择信息
                    $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                    $(".selected-info dd[lay-value=" + obj.value + "]").bind('click', function () {
                        $(this).remove();
                        //刷新选择信息
                        // selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                        //     return obj_selected.value != obj.value;
                        // });
                        var t = {};
                        t["value"] = $(this).attr('lay-value');
                        t["text"] = $(this).attr('lay-name');
                        t["dept"] = $(this).attr('lay-dept');
                        selectedMember.splice($.inArray(t, selectedMember), 1);
                    });
                } else {
                    var selected = function () {//判断是否已选择了该人员
                        var flag = true;
                        $("dl.selected-info dd").each(function (i, item) {
                            if ($(item).attr("lay-value") == obj.value) {
                                layer.msg('已选择了[' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + ']', {time: 1500});
                                $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                                flag = false;//已经选择
                            }
                        });
                        return flag;
                    }
                    if (selected()) {
                        // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                        var option = '<dd lay-value="' + obj.value + '" lay-flag="jtld"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                        $("dl.selected-info").prepend(option);
                        selectedMember.unshift(obj);//存储选择信息
                        $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                        $(".selected-info dd[lay-value=" + obj.value + "]").bind('click', function () {
                            $(this).remove();
                            //刷新选择信息
                            // selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                            //     return obj_selected.value != obj.value;
                            // });
                            var t = {};
                            t["value"] = $(this).attr('lay-value');
                            t["text"] = $(this).attr('lay-name');
                            t["dept"] = $(this).attr('lay-dept');
                            selectedMember.splice($.inArray(t, selectedMember), 1);
                        });
                    }
                }
            }
        });

        //清除
        $("#selected_info_reset").bind('click', function () {
            $("dl.selected-info dd").remove();
            selectedMember = [];
        });
        //
        $("#saveMembers").bind('click', function () {
            selectedMember=[];
            var list=$("dl").find("dd");
            $.each(list,function(i,item){
                var t = {};
                t["value"] = $(item).attr('lay-value');
                t["text"] = $(item).attr('lay-name');
                t["dept"] = $(item).attr('lay-dept');
                selectedMember.push(t);
            });
            $.ajax({
                type: 'post',
                url: "/seeyon/ext/fileUploadConfig.do?method=insertUploadMember",
                cache: false,
                data: {data: JSON.stringify(selectedMember)},
                success: function (data) {
                    var code = data.code;
                    if (code == 0) {
                        $("#btnShow").hide();
                    }
                },
                error: function (data) {
                    alert("error");
                }
            })
        });
        $(document).ready(function () {
            $.fn.zTree.init($("#treeDemo"), setting, zNodes);

            $.ajax({
                type: 'get',
                url: "/seeyon/ext/fileUploadConfig.do?method=getAllUploadMem",
                cache: false,
                data: {},
                success: function (data) {
                    selectedMember = [];
                    var code = data.code;
                    if (code == 0) {
                        var list = data.list;
                        for (var i = 0; i < list.length; i++) {
                            var m = list[i];
                            var obj = {};//添加成员对象
                            obj["value"] = m.userid;
                            obj["text"] = m.loginname;
                            obj["dept"] = m.deptid;

                            var option = '<dd lay-value="' + obj.value + '" lay-flag="jtld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                            $("dl.selected-info").prepend(option);
                            selectedMember.unshift(obj);//存储选择信息
                            $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                            $(".selected-info dd[lay-value=" + obj.value + "]").bind('click', function () {
                                $(this).remove();
                                var t = {};
                                t["value"] = $(this).attr('lay-value');
                                t["text"] = $(this).attr('lay-name');
                                t["dept"] = $(this).attr('lay-dept');
                                //刷新选择信息
                                // selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                                //     return obj_selected.value != obj.value;
                                // });
                                selectedMember.splice($.inArray(t, selectedMember), 1);
                            });
                        }
                    }
                },
                error: function (data) {
                    alert("error");
                }
            });
        });

    });
</SCRIPT>
</body>
</HTML>
