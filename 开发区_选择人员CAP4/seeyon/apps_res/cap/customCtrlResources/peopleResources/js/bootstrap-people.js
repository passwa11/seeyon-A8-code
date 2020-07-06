layui.use(['element'], function () {
    var element = layui.element;
});
$(function () {
    dangZhengBanTable();
    jiguan30Table();
    zhenban31Table();
    zhuqu32Table();
    gongHuiTable();

});

// 排序问题 对已选择人员进行部门排序
function dataSorting() {
    var arr29 = [];
    var arr30 = [];
    var arr31 = [];
    var arr32 = [];
    var arrGH = [];

    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var field0001 = $(this).attr("lay-name");
        var field0002 = $(this).attr("lay-field002");
        var field0003 = $(this).attr("lay-dept");
        var name = $(this).attr("lay-username");
        var flag = $(this).attr("lay-flag");
        var bs = $(this).attr("lay-bs");
        var bsname = $(this).attr("lay-bsname");
        var zsort = $(this).attr("lay-zsort");
        if (undefined == id) {
            id = "";
        }
        var obj = {};//添加成员对象
        obj["id"] = id;
        obj["field0001"] = field0001;
        obj["field0002"] = field0002;
        obj["name"] = name;
        obj["field0003"] = field0003;
        obj["field0005"] = bs + '';
        obj["mval"] = bsname;
        obj["field0007"] = zsort;
        obj["flag"] = flag;
        if (flag == '29') {
            arr29.push(obj);
        } else if (flag == '30') {
            arr30.push(obj);
        } else if (flag == '31') {
            arr31.push(obj);
        } else if (flag == '32') {
            arr32.push(obj);
        } else if (flag == 'gh') {
            arrGH.push(obj);
        }
    });
    var l29 = arrsyDataSort(arr29);
    var l30 = arrsyDataSort(arr30);
    var l31 = arrsyDataSort(arr31);
    var l32 = arrsyDataSort(arr32);
    var lgh = arrsyDataSort(arrGH);
    var option = "";
    var html29 = '';
    var html30 = '';
    var html31 = '';
    var html32 = '';
    var htmlgh = '';
    if (l29.length > 0) {
        html29 = htmlShow(l29, '29');
    }
    if (l30.length > 0) {
        html30 = htmlShow(l30, '30');
    }
    if (l31.length > 0) {
        html31 = htmlShow(l31, '31');
    }
    if (l32.length > 0) {
        html32 = htmlShow(l32, '32');
    }
    if (lgh.length > 0) {
        htmlgh = htmlShow(lgh, 'gh');
    }
    clearSelect();
    option += html29 + html30 + html31 + html32 + htmlgh;
    $("dl.selected-info").append(option);
}

function htmlShow(data, flag) {
    var html = "";
    for (var i = 0; i < data.length; i++) {
        html += '<dd ondblclick="removeDdRow'+flag+'(this)" lay-bsname="' + data[i].mval + '"  lay-field002="' + data[i].field0002 + '"  lay-zsort="' + data[i].field0007 + '" lay-id="' + data[i].id + '" lay-bs="' + data[i].field0005 + '"  lay-value="' + data[i].id + '" lay-username="' + data[i].name + '" lay-flag="' + flag + '" lay-name="' + data[i].field0001 + '" lay-dept="' + data[i].field0003 + '" class="">' + data[i].field0001 + '</dd>';
    }
    return html;
}

function arrsyDataSort(data) {
    for (var i = 0; i < data.length - 1; i++) {
        for (var j = 0; j < data.length - 1 - i; j++) {
            if (data[j].field0007 > data[j + 1].field0007) {
                var obj = data[j];
                data[j] = data[j + 1];
                data[j + 1] = obj;
            }
        }
    }
    return data;
}


//


////////////////////////////////////////////////////////////////////////////////////
//工会
function gongHuiTable() {
    $('#gonghui').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=gh',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        queryParams: queryParams,
        uniqueId: "id",
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
                field: 'field0001',
                title: '接收对象',
                width: '88%',
            }
        ]
        , onDblClickRow: function (row, $element, field) {
            var tr_obj = row;
            var obj = {};//添加成员对象
            obj["value"] = tr_obj.id;
            obj["text"] = tr_obj.field0001;
            obj["dept"] = tr_obj.field0003;
            if ($("dl.selected-info dd").length <= 0) {
                var option = '<dd ondblclick="removeDdRowgh(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '" lay-flag="gh" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
                    var option = '<dd ondblclick="removeDdRowgh(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="gh"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                    $("dl.selected-info").append(option);
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
            dataSorting();
            removeTableRowGH(row);
        }
    });

}

var idsgh = [];
function removeTableRowGH(row) {
    idsgh.push(row.id);
    $('#gonghui').bootstrapTable('remove', {
        field: 'id',
        values: idsgh
    });
}

function removeDdRowgh(item) {
    $(item).remove();
    $('#gonghui').bootstrapTable('insertRow', {
        index: 0,
        row: {
            id: $(item).attr("lay-id"),
            field0001: $(item).attr("lay-name"),
            field0002: $(item).attr("lay-value"),
            name: $(item).attr("lay-username"),
            field0003: $(item).attr("lay-dept"),
            field0005: $(item).attr("lay-bs"),
            mval: $(item).attr("lay-bsname"),
            field0007: $(item).attr("lay-zsort")
        }
    });
    for (var i = 0; i <idsgh.length ; i++) {
        var index=idsgh.indexOf($(item).attr("lay-id"));
        if(index>-1){
            idsgh.splice(index,1);
        }
    }
}

function dangZhengBanTable() {
    $('#dzb29').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=29',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        queryParams: queryParams,
        uniqueId: "id",
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
                field: 'field0001',
                title: '接收对象',
                width: '88%',
            }
        ]
        , onDblClickRow: function (row, $element, field) {

            var tr_obj = row;
            var obj = {};//添加成员对象
            obj["value"] = tr_obj.id;
            obj["text"] = tr_obj.field0001;
            obj["dept"] = tr_obj.field0003;
            if ($("dl.selected-info dd").length <= 0) {
                var option = '<dd ondblclick="removeDdRow29(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '" lay-flag="29" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
                    var option = '<dd ondblclick="removeDdRow29(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="29"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                    $("dl.selected-info").append(option);
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
            dataSorting();
            removeTableRow29(row);

        }

    });
}
var ids29 = [];
function removeTableRow29(row) {
    ids29.push(row.id);
    $('#dzb29').bootstrapTable('remove', {
        field: 'id',
        values: ids29
    });
    console.log(ids29.toString())
}

function removeDdRow29(item) {
    $(item).remove();
    $('#dzb29').bootstrapTable('insertRow', {
        index: 0,
        row: {
            id: $(item).attr("lay-id"),
            field0001: $(item).attr("lay-name"),
            field0002: $(item).attr("lay-value"),
            name: $(item).attr("lay-username"),
            field0003: $(item).attr("lay-dept"),
            field0005: $(item).attr("lay-bs"),
            mval: $(item).attr("lay-bsname"),
            field0007: $(item).attr("lay-zsort")
        }
    });
    for (var i = 0; i <ids29.length ; i++) {
        var index=ids29.indexOf($(item).attr("lay-id"));
        if(index>-1){
            ids29.splice(index,1);
        }
    }
    console.log(ids29);
}

function jiguan30Table() {
    $('#jiguan30').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=30',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        queryParams: queryParams,
        uniqueId: "id",
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
                field: 'field0001',
                title: '接收对象',
                width: '88%',
            }
        ]
        , onDblClickRow: function (row, $element, field) {
            var tr_obj = row;
            var obj = {};//添加成员对象
            obj["value"] = tr_obj.id;
            obj["text"] = tr_obj.field0001;
            obj["dept"] = tr_obj.field0003;
            if ($("dl.selected-info dd").length <= 0) {
                var option = '<dd ondblclick="removeDdRow30(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="30" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
                    var option = '<dd ondblclick="removeDdRow30(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="30"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                    $("dl.selected-info").append(option);
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
            //
            dataSorting();
            removeTableRow30(row);
        }
    });
}
var ids30 = [];
function removeTableRow30(row) {
    ids30.push(row.id);
    $('#jiguan30').bootstrapTable('remove', {
        field: 'id',
        values: ids30
    });
}

function removeDdRow30(item) {
    $(item).remove();
    $('#jiguan30').bootstrapTable('insertRow', {
        index: 0,
        row: {
            id: $(item).attr("lay-id"),
            field0001: $(item).attr("lay-name"),
            field0002: $(item).attr("lay-value"),
            name: $(item).attr("lay-username"),
            field0003: $(item).attr("lay-dept"),
            field0005: $(item).attr("lay-bs"),
            mval: $(item).attr("lay-bsname"),
            field0007: $(item).attr("lay-zsort")
        }
    });
    for (var i = 0; i <ids30.length ; i++) {
        var index=ids30.indexOf($(item).attr("lay-id"));
        if(index>-1){
            ids29.splice(index,1);
        }
    }
}

function zhenban31Table() {
    $('#zhenb31').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=31',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        queryParams: queryParams,
        uniqueId: "id",
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
                field: 'field0001',
                title: '接收对象',
                width: '88%',
            }
        ]
        , onDblClickRow: function (row, $element, field) {
            var tr_obj = row;
            var obj = {};//添加成员对象
            obj["value"] = tr_obj.id;
            obj["text"] = tr_obj.field0001;
            obj["dept"] = tr_obj.field0003;
            if ($("dl.selected-info dd").length <= 0) {
                var option = '<dd ondblclick="removeDdRow31(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="31" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
                    var option = '<dd ondblclick="removeDdRow31(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="31"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                    $("dl.selected-info").append(option);
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
            //
            dataSorting();
            removeTableRow31(row);

        }
    });
}
var ids31 = [];
function removeTableRow31(row) {
    ids31.push(row.id);
    $('#zhenb31').bootstrapTable('remove', {
        field: 'id',
        values: ids31
    });
}

function removeDdRow31(item) {
    $(item).remove();
    $('#zhenb31').bootstrapTable('insertRow', {
        index: 0,
        row: {
            id: $(item).attr("lay-id"),
            field0001: $(item).attr("lay-name"),
            field0002: $(item).attr("lay-value"),
            name: $(item).attr("lay-username"),
            field0003: $(item).attr("lay-dept"),
            field0005: $(item).attr("lay-bs"),
            mval: $(item).attr("lay-bsname"),
            field0007: $(item).attr("lay-zsort")
        }
    });
    for (var i = 0; i <ids31.length ; i++) {
        var index=ids31.indexOf($(item).attr("lay-id"));
        if(index>-1){
            ids31.splice(index,1);
        }
    }

}

function zhuqu32Table() {
    $('#zhuqu32').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=32',
        queryParamsType: '',              //默认值为 'limit' ,在默认情况下 传给服务端的参数为：offset,limit,sort
        queryParams: queryParams,
        uniqueId: "id",
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
                field: 'field0001',
                title: '接收对象',
                width: '88%',
            }
        ]
        , onDblClickRow: function (row, $element, field) {
            var tr_obj = row;
            var obj = {};//添加成员对象
            obj["value"] = tr_obj.id;
            obj["text"] = tr_obj.field0001;
            obj["dept"] = tr_obj.field0003;
            if ($("dl.selected-info dd").length <= 0) {
                var option = '<dd ondblclick="removeDdRow32(this)" lay-bsname="' + tr_obj.mval + '" lay-field002="' + tr_obj.field0002 + '" lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="32" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
                    var option = '<dd  ondblclick="removeDdRow32(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="32"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                    $("dl.selected-info").append(option);
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
            //
            dataSorting();
            removeTableRow32(row);

        }
    });
}
var ids32=[];
function removeTableRow32(row) {
    ids32.push(row.id);
    $('#zhuqu32').bootstrapTable('remove', {
        field: 'id',
        values: ids32
    });
}

function removeDdRow32(item) {
    $(item).remove();
    $('#zhuqu32').bootstrapTable('insertRow', {
        index: 0,
        row: {
            id: $(item).attr("lay-id"),
            field0001: $(item).attr("lay-name"),
            field0002: $(item).attr("lay-value"),
            name: $(item).attr("lay-username"),
            field0003: $(item).attr("lay-dept"),
            field0005: $(item).attr("lay-bs"),
            mval: $(item).attr("lay-bsname"),
            field0007: $(item).attr("lay-zsort")
        }
    });
    for (var i = 0; i <ids32.length ; i++) {
        var index=ids32.indexOf($(item).attr("lay-id"));
        if(index>-1){
            ids32.splice(index,1);
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////


function sureSelect() {
    dangZhengBanSure();
    Sure30();
    Sure31();
    Sure32();
    // SureGonghui();

}

//确认 党政办人员
function dangZhengBanSure() {
    var $table = $('#dzb29');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;

    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.id;
        obj["text"] = tr_obj.field0001;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd  ondblclick="removeDdRow29(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '" lay-flag="29" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
            $("dl.selected-info").append(option);
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
                var option = '<dd  ondblclick="removeDdRow29(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="29"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
        dataSorting();
    }
    ids29= $.map(rows, function (row) {
        return row.id
    });
    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var flag = $(this).attr("lay-flag");
        if(flag=='29'){
            if(undefined != id){
                ids29.push(id);
            }
        }
    });

    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids29
    });
    console.log(ids29);

}

function Sure30() {
    var $table = $('#jiguan30');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.id;
        obj["text"] = tr_obj.field0001;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd  ondblclick="removeDdRow30(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="30" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
            $("dl.selected-info").append(option);
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
                var option = '<dd ondblclick="removeDdRow30(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="30"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
        dataSorting();
    }

    ids30 = $.map(rows, function (row) {
        return row.id
    });
    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var flag = $(this).attr("lay-flag");
        if(flag=='30'){
            if(undefined != id){
                ids30.push(id);
            }
        }
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids30
    });
}

function Sure31() {
    var $table = $('#zhenb31');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.id;
        obj["text"] = tr_obj.field0001;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd  ondblclick="removeDdRow31(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="31" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
            $("dl.selected-info").append(option);
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
                var option = '<dd ondblclick="removeDdRow31(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="31"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
        dataSorting();
    }
    ids31= $.map(rows, function (row) {
        return row.id
    });
    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var flag = $(this).attr("lay-flag");
        if(flag=='31'){
            if(undefined != id){
                ids31.push(id);
            }
        }
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids31
    });
}

function Sure32() {
    var $table = $('#zhuqu32');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.id;
        obj["text"] = tr_obj.field0001;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd  ondblclick="removeDdRow32(this)"  lay-bsname="' + tr_obj.mval + '" lay-field002="' + tr_obj.field0002 + '" lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="32" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
            $("dl.selected-info").append(option);
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
                var option = '<dd  ondblclick="removeDdRow32(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="32"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
        dataSorting();
    }
    ids32 = $.map(rows, function (row) {
        return row.id
    });
    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var flag = $(this).attr("lay-flag");
        if(flag=='32'){
            if(undefined != id){
                ids32.push(id);
            }
        }
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids32
    });
}

function SureGonghui() {
    var $table = $('#gonghui');
    var rows = $table.bootstrapTable('getSelections');
    var arrJtld = rows;
    for (var i = 0; i < arrJtld.length; i++) {
        var tr_obj = arrJtld[i];
        var obj = {};//添加成员对象
        obj["value"] = tr_obj.id;
        obj["text"] = tr_obj.field0001;
        obj["dept"] = tr_obj.field0003;
        if ($("dl.selected-info dd").length <= 0) {
            var option = '<dd  ondblclick="removeDdRowgh(this)"  lay-bsname="' + tr_obj.mval + '" lay-field002="' + tr_obj.field0002 + '" lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="gh" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
            $("dl.selected-info").append(option);
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
                var option = '<dd  ondblclick="removeDdRowgh(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="gh"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
                $("dl.selected-info").append(option);
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
        dataSorting();
    }
    idsgh = $.map(rows, function (row) {
        return row.id
    });
    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var flag = $(this).attr("lay-flag");
        if(flag=='gh'){
            if(undefined != id){
                idsgh.push(id);
            }
        }
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: idsgh
    });
}

function commonInfo(s) {
    var list = $("dl").find("dd" + s);
    $.each(list, function (i, item) {
        $(".selected-info dd[lay-value=" + $(item).attr('lay-value') + "]").remove();
        var type = $(item).attr("lay-flag");
        if (type == '29') {
            $('#dzb29').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: $(item).attr("lay-id"),
                    field0001: $(item).attr("lay-name"),
                    field0002: $(item).attr("lay-value"),
                    name: $(item).attr("lay-username"),
                    field0003: $(item).attr("lay-dept"),
                    field0005: $(item).attr("lay-bs"),
                    mval: $(item).attr("lay-bsname"),
                    field0007: $(item).attr("lay-zsort")
                }
            });
        }
        if (type == '30') {
            $('#jiguan30').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: $(item).attr("lay-id"),
                    field0001: $(item).attr("lay-name"),
                    field0002: $(item).attr("lay-value"),
                    name: $(item).attr("lay-username"),
                    field0003: $(item).attr("lay-dept"),
                    field0005: $(item).attr("lay-bs"),
                    mval: $(item).attr("lay-bsname"),
                    field0007: $(item).attr("lay-zsort")
                }
            });
        }
        if (type == '31') {
            $('#zhenb31').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: $(item).attr("lay-id"),
                    field0001: $(item).attr("lay-name"),
                    field0002: $(item).attr("lay-value"),
                    name: $(item).attr("lay-username"),
                    field0003: $(item).attr("lay-dept"),
                    field0005: $(item).attr("lay-bs"),
                    mval: $(item).attr("lay-bsname"),
                    field0007: $(item).attr("lay-zsort")
                }
            });
        }
        if (type == '32') {
            $('#zhuqu32').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: $(item).attr("lay-id"),
                    field0001: $(item).attr("lay-name"),
                    field0002: $(item).attr("lay-value"),
                    name: $(item).attr("lay-username"),
                    field0003: $(item).attr("lay-dept"),
                    field0005: $(item).attr("lay-bs"),
                    mval: $(item).attr("lay-bsname"),
                    field0007: $(item).attr("lay-zsort")
                }
            });
        }
        if (type == 'gh') {
            $('#gonghui').bootstrapTable('insertRow', {
                index: 0,
                row: {
                    id: $(item).attr("lay-id"),
                    field0001: $(item).attr("lay-name"),
                    field0002: $(item).attr("lay-value"),
                    name: $(item).attr("lay-username"),
                    field0003: $(item).attr("lay-dept"),
                    field0005: $(item).attr("lay-bs"),
                    mval: $(item).attr("lay-bsname"),
                    field0007: $(item).attr("lay-zsort")
                }
            });
        }

    });
}

function removeSelect() {
    commonInfo('.selected-this');
}

function clearSelect() {
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
