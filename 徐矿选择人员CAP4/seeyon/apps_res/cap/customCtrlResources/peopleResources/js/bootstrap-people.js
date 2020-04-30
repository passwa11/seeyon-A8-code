layui.use(['element'], function () {
    var element = layui.element;
});
$(function () {
    initJtldTable();
    initDzbldTable();
    initDzbTable();
    initJgbmTable();
    initCompanyTable();
    initJcdwTable();
});
////////////////////////////////////////////////////////////////////////////////////
//集团领导
function initJtldTable() {
    $('#jtld').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectJtldEntity',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        queryParams: queryParams,
        uniqueId: "field0001",
        method: "post",
        contentType: "application/x-www-form-urlencoded",
        undefinedText: "",//当数据为 undefined 时显示的字符
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            // , {
            //     field: 'field0004',
            //     title: '职务',
            //     width: '63%',
            // }
            , {
                field: 'field0003',
                title: '用户名',
                width: '88%',
            }
        ]
    });
}

//党政办领导
function initDzbldTable() {
    $('#dzbld').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectFormmain0380',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        uniqueId: "field0001",
        method: "post",
        contentType: "application/x-www-form-urlencoded",
        undefinedText: "",//当数据为 undefined 时显示的字符
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            // , {
            //     field: 'field0004',
            //     title: '职务',
            //     width: '63%',
            // }
            , {
                field: 'field0003',
                title: '用户名',
                width: '88%',
            }
        ]
    });
}

//党政办科室
function initDzbTable() {
    $('#dzbks').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectDeskWork',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        uniqueId: "field0001",
        method: "post",
        contentType: "application/x-www-form-urlencoded",
        undefinedText: "",//当数据为 undefined 时显示的字符
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0002',
                title: '科室名称',
                width: '88%',
            }
        ]
    });
}

//机关部门
function initJgbmTable(){
    $('#jgbm').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectFormmain0106_organ',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        uniqueId: "field0001",
        method: "post",
        contentType: "application/x-www-form-urlencoded",
        undefinedText: "",//当数据为 undefined 时显示的字符
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0004',
                title: '部室',
                width: '88%',
            }
            // , {
            //     field: 'field0003',
            //     title: '机要员',
            //     width: '88%',
            // }
        ]
    });
}

//分公司
function initCompanyTable(){
    $('#company').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectFormmain0323_company',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        uniqueId: "field0001",
        method: "post",
        contentType: "application/x-www-form-urlencoded",
        undefinedText: "",//当数据为 undefined 时显示的字符
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0004',
                title: '单位',
                width: '88%',
            }
        ]
    });
}

//基层单位
function initJcdwTable(){
    $('#jcdw').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectFormmain0087_baseUnits',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        uniqueId: "field0001",
        method: "post",
        contentType: "application/x-www-form-urlencoded",
        undefinedText: "",//当数据为 undefined 时显示的字符
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0004',
                title: '单位',
                width: '88%',
            }
        ]
    });
}

////////////////////////////////////////////////////////////////////////////////////


function sureSelect() {
    jtldSure();
    dzbldSure();
    dzbSure();
    jgbmSure();
    companySure();
    jcdwSure();
}

//确认 集团领导
function jtldSure() {
    var $table = $('#jtld');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jtld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            var option = '<dd lay-zsort="'+tr_obj.zsort+'" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jtld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                var index = $(this).attr("class").indexOf("selected-this");
                if (index == 0) {
                    $(this).removeClass("selected-this");
                } else {
                    $(this).addClass("selected-this");
                }
            });
        } else {
            var selected = function () {//判断是否已选择了该人员
                var flag = true;
                $("dl.selected-info dd").each(function (i, item) {
                    if ($(item).attr("lay-value") == obj.value) {
                        flag = false;//已经选择
                    }
                });
                return flag;
            }
            if (selected()) {
                // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jtld"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jtld"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    var index = $(this).attr("class").indexOf("selected-this");
                    if (index == 0) {
                        $(this).removeClass("selected-this");
                    } else {
                        $(this).addClass("selected-this");
                    }
                });
            }
        }
    }
    var ids = $.map(rows, function (row) {
        return row.field0001
    });
    $table.bootstrapTable('remove', {
        field: 'field0001',
        values: ids
    });
}
//确认 党政办领导
function dzbldSure() {
    var $table = $('#dzbld');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="dzbld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            var option = '<dd  lay-zsort="'+tr_obj.zsort+'" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="dzbld" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                var index = $(this).attr("class").indexOf("selected-this");
                if (index == 0) {
                    $(this).removeClass("selected-this");
                } else {
                    $(this).addClass("selected-this");
                }
            });
        } else {
            var selected = function () {//判断是否已选择了该人员
                var flag = true;
                $("dl.selected-info dd").each(function (i, item) {
                    if ($(item).attr("lay-value") == obj.value) {
                        flag = false;//已经选择
                    }
                });
                return flag;
            }
            if (selected()) {
                // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="dzbld"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd  lay-zsort="'+tr_obj.zsort+'" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="dzbld"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    var index = $(this).attr("class").indexOf("selected-this");
                    if (index == 0) {
                        $(this).removeClass("selected-this");
                    } else {
                        $(this).addClass("selected-this");
                    }
                });
            }
        }
    }
    var ids = $.map(rows, function (row) {
        return row.field0001
    });
    $table.bootstrapTable('remove', {
        field: 'field0001',
        values: ids
    });
}
//确认 党政办
function dzbSure(){
    var $table = $('#dzbks');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0002;
        obj["dept"] = "";
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-bs="'+tr_obj.field0006+'"  lay-value="' + obj.value + '" lay-id="'+tr_obj.id+'"  lay-flag="dzb" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                var index = $(this).attr("class").indexOf("selected-this");
                if (index == 0) {
                    $(this).removeClass("selected-this");
                } else {
                    $(this).addClass("selected-this");
                }
            });
        } else {
            var selected = function () {//判断是否已选择了该人员
                var flag = true;
                $("dl.selected-info dd").each(function (i, item) {
                    if ($(item).attr("lay-value") == obj.value) {
                        flag = false;//已经选择
                    }
                });
                return flag;
            }
            if (selected()) {
                var option = '<dd  lay-zsort="'+tr_obj.zsort+'" lay-bs="'+tr_obj.field0006+'"  lay-value="' + obj.value + '" lay-id="'+tr_obj.id+'"  lay-flag="dzb" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    var index = $(this).attr("class").indexOf("selected-this");
                    if (index == 0) {
                        $(this).removeClass("selected-this");
                    } else {
                        $(this).addClass("selected-this");
                    }
                });
            }
        }
    }
    var ids = $.map(rows, function (row) {
        return row.field0001
    });
    $table.bootstrapTable('remove', {
        field: 'field0001',
        values: ids
    });
}
//确认 机关部门
function jgbmSure() {
    var $table = $('#jgbm');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jgbm" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jgbm" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
            var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jgbm" lay-name="' + obj.text + '" lay-dept="' + obj.text + '" class="">' + obj.text + '</dd>';
            $("dl.selected-info").prepend(option);
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                var index = $(this).attr("class").indexOf("selected-this");
                if (index == 0) {
                    $(this).removeClass("selected-this");
                } else {
                    $(this).addClass("selected-this");
                }
            });
        } else {
            var selected = function () {//判断是否已选择了该人员
                var flag = true;
                $("dl.selected-info dd").each(function (i, item) {
                    if ($(item).attr("lay-value") == obj.value) {
                        flag = false;//已经选择
                    }
                });
                return flag;
            }
            if (selected()) {
                // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jgbm"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jgbm"  lay-name="' + obj.text + '" lay-dept="' + obj.text + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").prepend(option);
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    var index = $(this).attr("class").indexOf("selected-this");
                    if (index == 0) {
                        $(this).removeClass("selected-this");
                    } else {
                        $(this).addClass("selected-this");
                    }
                });
            }
        }
    }
    var ids = $.map(rows, function (row) {
        return row.field0001
    });
    $table.bootstrapTable('remove', {
        field: 'field0001',
        values: ids
    });
}
//确认 分公司
function companySure(){
    var $table = $('#company');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="company" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="company" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                var index = $(this).attr("class").indexOf("selected-this");
                if (index == 0) {
                    $(this).removeClass("selected-this");
                } else {
                    $(this).addClass("selected-this");
                }
            });
        } else {
            var selected = function () {//判断是否已选择了该人员
                var flag = true;
                $("dl.selected-info dd").each(function (i, item) {
                    if ($(item).attr("lay-value") == obj.value) {
                        flag = false;//已经选择
                    }
                });
                return flag;
            }
            if (selected()) {
                // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="company"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="company"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    var index = $(this).attr("class").indexOf("selected-this");
                    if (index == 0) {
                        $(this).removeClass("selected-this");
                    } else {
                        $(this).addClass("selected-this");
                    }
                });
            }
        }
    }
    var ids = $.map(rows, function (row) {
        return row.field0001
    });
    $table.bootstrapTable('remove', {
        field: 'field0001',
        values: ids
    });
}
//确认 基层单位
function jcdwSure(){
    var $table = $('#jcdw');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.field0001;
        obj["text"] = tr_obj.field0004;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jcdw" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
            var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jcdw" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">'+ obj.dept + '</dd>';
            $("dl.selected-info").prepend(option);
            $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                var index = $(this).attr("class").indexOf("selected-this");
                if (index == 0) {
                    $(this).removeClass("selected-this");
                } else {
                    $(this).addClass("selected-this");
                }
            });
        } else {
            var selected = function () {//判断是否已选择了该人员
                var flag = true;
                $("dl.selected-info dd").each(function (i, item) {
                    if ($(item).attr("lay-value") == obj.value) {
                        flag = false;//已经选择
                    }
                });
                return flag;
            }
            if (selected()) {
                // var option = '<dd lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jcdw"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '&nbsp;&nbsp;&nbsp;&nbsp;' + obj.dept + '</dd>';
                var option = '<dd lay-zsort="'+tr_obj.zsort+'"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0006 + '"  lay-value="' + obj.value + '" lay-flag="jcdw"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.dept + '</dd>';
                $("dl.selected-info").prepend(option);
                $(".selected-info dd[lay-value=" + obj.value + "]").on('click', function () {
                    var index = $(this).attr("class").indexOf("selected-this");
                    if (index == 0) {
                        $(this).removeClass("selected-this");
                    } else {
                        $(this).addClass("selected-this");
                    }
                });
            }
        }
    }
    var ids = $.map(rows, function (row) {
        return row.field0001
    });
    $table.bootstrapTable('remove', {
        field: 'field0001',
        values: ids
    });
}


function commonInfo(s) {
    var list = $("dl").find("dd"+s);
    $.each(list, function (i, item) {
        $(".selected-info dd[lay-value=" + $(item).attr('lay-value') + "]").remove();
        var type=$(item).attr("lay-flag");
        if(type=='jtld'){
            $('#jtld').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    zsort:$(item).attr('lay-zsort'),
                    field0001: $(item).attr('lay-value'),
                    field0004: $(item).attr('lay-name'),
                    field0003: $(item).attr('lay-dept'),
                    field0006: $(item).attr('lay-bs'),
                    flag: $(item).attr("lay-flag"),
                    id: $(item).attr("lay-id")
                }
            });
        }
        if(type=='dzbld'){
            $('#dzbld').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    zsort:$(item).attr('lay-zsort'),

                    field0001: $(item).attr('lay-value'),
                    field0004: $(item).attr('lay-name'),
                    field0003: $(item).attr('lay-dept'),
                    field0006: $(item).attr('lay-bs'),
                    flag: $(item).attr("lay-flag"),
                    id: $(item).attr("lay-id")
                }
            });
        }
        if(type=='dzb'){
            $('#dzbks').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    zsort:$(item).attr('lay-zsort'),

                    field0001: $(item).attr('lay-value'),
                    field0002: $(item).attr('lay-name'),
                    field0006: $(item).attr('lay-bs'),
                    flag: $(item).attr("lay-flag"),
                    id: $(item).attr("lay-id")
                }
            });
        }
        if(type=='jgbm'){
            $('#jgbm').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    zsort:$(item).attr('lay-zsort'),

                    field0001: $(item).attr('lay-value'),
                    field0004: $(item).attr('lay-name'),
                    field0003: $(item).attr('lay-dept'),
                    field0006: $(item).attr('lay-bs'),
                    flag: $(item).attr("lay-flag"),
                    id: $(item).attr("lay-id")
                }
            });
        }
        if(type=='company'){
            $('#company').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    zsort:$(item).attr('lay-zsort'),

                    field0001: $(item).attr('lay-value'),
                    field0004: $(item).attr('lay-name'),
                    field0003: $(item).attr('lay-dept'),
                    field0006: $(item).attr('lay-bs'),
                    flag: $(item).attr("lay-flag"),
                    id: $(item).attr("lay-id")
                }
            });
        }
        if(type=='jcdw'){
            $('#jcdw').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    zsort:$(item).attr('lay-zsort'),

                    field0001: $(item).attr('lay-value'),
                    field0004: $(item).attr('lay-name'),
                    field0003: $(item).attr('lay-dept'),
                    field0006: $(item).attr('lay-bs'),
                    flag: $(item).attr("lay-flag"),
                    id: $(item).attr("lay-id")
                }
            });
        }
    });
}
function removeSelect() {
    commonInfo('.selected-this');
}

function clearSelect(){
    commonInfo('');
}
//查询条件
function queryParams(params) {
    return {
        name: $.trim($("#jtldInput").val())
    };
}

//查询事件
function SearchData() {
    $('#jtld').bootstrapTable('refresh', {pageNumber: 1});
}
