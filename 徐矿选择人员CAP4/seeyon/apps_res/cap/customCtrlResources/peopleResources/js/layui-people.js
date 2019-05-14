//selectpeople.html  js渲染文件
layui.use(['table', 'layer', 'element'], function () {
    var $ = layui.jquery;
    var layer = layui.layer;
    var table = layui.table;
    var selectedMember = [];//选择信息数组

    var element = layui.element; //Tab的切换功能，切换事件监听等，需要依赖element模块

    //触发事件
    var active = {

        tabChange: function () {
            //切换到指定Tab项
            element.tabChange('demo', '22'); //切换到：用户管理
        }
    };

    //集团领导
    $.ajax({
        type:'GET',
        url:'/seeyon/ext/selectPeople.do?method=selectJtldEntity',
        dataType:'json',
        success:function (data) {
            table.render({
                elem: '#jtld'
                , id: 'jtldId'
                , height: 500
                // ,url: '/demo/table/user/' //数据接口
                , page: true //开启分页
                , cols: [[ //表头
                    {field: 'field0003', title: '用户名', width: '50%'}
                    , {field: 'field0004', title: '所属部门', width: '50%', sort: true}
                ]],
                data:data.data
            });
        }
    });

    table.on('row(jtldFilter)', function (obj) {
        // alert(obj.data);
        console.log(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            selectedMember.unshift(obj);//存储选择信息
            $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                $(this).remove();
                //刷新选择信息
                selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                    return obj_selected.value != obj.value;
                });
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
                var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                selectedMember.unshift(obj);//存储选择信息
                $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    $(this).remove();
                    //刷新选择信息
                    selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                        return obj_selected.value != obj.value;
                    });
                });
            }
        }
    });

    //党政办
    $.ajax({
        type:'GET',
        url:'/seeyon/ext/selectPeople.do?method=selectFormmain0148_policy',
        dataType:'json',
        success:function (data) {
            table.render({
                elem: '#dzbks'
                , height: 500
                // ,url: '/demo/table/user/' //数据接口
                , page: true //开启分页
                , cols: [[ //表头
                    {field: 'field0002', title: '科室', width: '46%'}
                    , {field: 'name', title: '机要员', width: '50%', sort: true}
                ]],
                data: data.data
            });
        }
    });

    table.on('row(dzbksFilter)', function (obj) {
        // alert(obj.data);
        console.log(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0003;
        obj["text"] = tr_obj.field0002;
        obj["dept"] = tr_obj.name;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            selectedMember.unshift(obj);//存储选择信息
            $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                $(this).remove();
                //刷新选择信息
                selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                    return obj_selected.value != obj.value;
                });
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
                var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                selectedMember.unshift(obj);//存储选择信息
                $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    $(this).remove();
                    //刷新选择信息
                    selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                        return obj_selected.value != obj.value;
                    });
                });
            }
        }
    });

    //机关部门
    $.ajax({
        type:'GET',
        url:'/seeyon/ext/selectPeople.do?method=selectFormmain0106_organ',
        dataType:'json',
        success:function (data) {
            table.render({
                elem: '#bsxx'
                , height: 500
                , page: true //开启分页
                , cols: [[ //表头
                    {field: 'field0013', title: '部室', width: '46%'}
                    , {field: 'name', title: '机要员', width: '50%', sort: true}
                ]],
                data: data.data
            });
        }
    });

    table.on('row(bsxxFilter)', function (obj) {
        // alert(obj.data);
        console.log(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0011;
        obj["text"] = tr_obj.field0013;
        obj["dept"] = tr_obj.name;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            selectedMember.unshift(obj);//存储选择信息
            $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                $(this).remove();
                //刷新选择信息
                selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                    return obj_selected.value != obj.value;
                });
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
                var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                selectedMember.unshift(obj);//存储选择信息
                $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    $(this).remove();
                    //刷新选择信息
                    selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                        return obj_selected.value != obj.value;
                    });
                });
            }
        }
    });

    //基层单位
    $.ajax({
        type:'GET',
        url:'/seeyon/ext/selectPeople.do?method=selectFormmain0087_baseUnits',
        dataType:'json',
        success:function (data) {
            table.render({
                elem: '#jcdw'
                , height: 500
                , page: true //开启分页
                , cols: [[ //表头
                    {field: 'field0010', title: '单位', width: '46%'}
                    , {field: 'name', title: '机要员', width: '50%', sort: true}
                ]],
                data: data.data

            });
        }
    });

    table.on('row(jcdwFilter)', function (obj) {
        // alert(obj.data);
        console.log(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0007;
        obj["text"] = tr_obj.field0010;
        obj["dept"] = tr_obj.name;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            selectedMember.unshift(obj);//存储选择信息
            $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                $(this).remove();
                //刷新选择信息
                selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                    return obj_selected.value != obj.value;
                });
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
                var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                selectedMember.unshift(obj);//存储选择信息
                $(".selected-info dd[lay-value=" + obj.value + "]").addClass("selected-this").siblings().removeClass("selected-this");
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    $(this).remove();
                    //刷新选择信息
                    selectedMember = $.grep(selectedMember, function (obj_selected, n) {
                        return obj_selected.value != obj.value;
                    });
                });
            }
        }
    });


});