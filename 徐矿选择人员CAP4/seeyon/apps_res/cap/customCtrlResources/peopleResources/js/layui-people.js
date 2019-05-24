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

    //保存
    $("#save").on('click', function () {
        // alert(selectedMember.join(","));
        // $.ajax({
        //     sync: true,
        //     type: "POST",
        //     url: "/seeyon/ext/selectPeople.do?method=insertData",
        //     data: {"info": JSON.stringify(selectedMember)},
        //     dataType: 'text',
        //     success: function (data) {
        //         layer.msg('人员信息保存成功', {icon: 1, time: 1500});
        //
        //     },
        //     error: function (XMLHttpRequest, textStatus, errorThrown) {
        //         layer.msg('人员信息保存失败！！！', {icon: 5, time: 1500});
        //     }
        // });
    });

    //清除
    $("#selected_info_reset").on('click', function () {
        $("dl.selected-info dd").remove();
        selectedMember = [];
    });

//    jtldFun(null);


    //集团领导
    $("#queryjtldPeople").on('click', function () {
        //执行重载
        table.reload('jtldId', {
            page: {
                curr: 1 //重新从第 1 页开始
            }
            , where: {
                name: $("#jtldInput").val()
            }
        });
    });
    table.render({
        id: 'jtldId'
        , elem: '#jtld'
        , url: '/seeyon/ext/selectPeople.do?method=selectJtldEntity'
        , height: 400
        , page: false //开启分页
        , cols: [[ //表头
            {type:'checkbox'},
            {field: 'field0004', title: '所属部门', width: '44%', sort: true},
            {field: 'field0003', title: '用户名', width: '44%'}
        ]]
    });

    table.on('checkbox(jtldFilter)', function(obj){
        console.log(obj.data);
    });

    table.on('row(jtldFilter)', function (obj) {

        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
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
                // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

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
    $("#querydzbksPeople").on('click', function () {
        //执行重载
        table.reload('dzbksId', {
            page: {
                curr: 1 //重新从第 1 页开始
            }
            , where: {
                name: $("#dzbksInput").val()
            }
        });
    });
    table.render({
        id: 'dzbksId'
        , elem: '#dzbks',
        url: '/seeyon/ext/selectPeople.do?method=selectFormmain0148_policy'
        , height: 400
        , page: false //开启分页
        , cols: [[ //表头
            {type:'checkbox'},
            {field: 'field0004', title: '科室', width: '44%'}
            , {field: 'field0003', title: '机要员', width: '44%', sort: true}
        ]]
    });


    table.on('row(dzbksFilter)', function (obj) {
        // alert(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

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
                // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

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
    $("#querybsxxPeople").on('click', function () {
        //执行重载
        table.reload('bsxxId', {
            page: {
                curr: 1 //重新从第 1 页开始
            }
            , where: {
                name: $("#bsxxInput").val()
            }
        });
    });
    table.render({
        id: 'bsxxId'
        , elem: '#bsxx'
        , height: 400
        , url: '/seeyon/ext/selectPeople.do?method=selectFormmain0106_organ'
        , page: false //开启分页
        , cols: [[ //表头
            {type:'checkbox'},
            {field: 'field0004', title: '部室', width: '44%'}
            , {field: 'field0003', title: '机要员', width: '44%', sort: true}
        ]]
    });


    table.on('row(bsxxFilter)', function (obj) {
        // alert(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

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
                // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

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
    $("#queryjcdwPeople").on('click', function () {
        //执行重载
        table.reload('jcdwId', {
            page: {
                curr: 1 //重新从第 1 页开始
            }
            , where: {
                name: $("#jcdwInput").val()
            }
        });
    });
    table.render({
        id: 'jcdwId'
        , elem: '#jcdw'
        , url: '/seeyon/ext/selectPeople.do?method=selectFormmain0087_baseUnits'
        , height: 400
        , page: false //开启分页
        , cols: [[ //表头
            {type:'checkbox'},
            {field: 'field0004', title: '单位', width: '44%'}
            , {field: 'field0003', title: '机要员', width: '44%', sort: true}
        ]]

    });


    table.on('row(jcdwFilter)', function (obj) {
        // alert(obj.data);
        var tr_obj = obj.data;
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

            // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
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
                // var option = '<dd lay-value="' + obj.value + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-value="' + obj.value + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';

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