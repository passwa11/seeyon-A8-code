layui.use(['element'], function () {
    var element = layui.element;
});
$(function () {
    dangZhengBanTable();
    jiguan30Table();
    zhenban31Table();
    zhuqu32Table();
});

////////////////////////////////////////////////////////////////////////////////////

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
                    var option = '<dd ondblclick="removeDdRow29(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="29"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
            removeTableRow29(row);
        }

    });

}

function removeTableRow29(row) {
    var ids = [];
    ids.push(row.id);
    $('#dzb29').bootstrapTable('remove', {
        field: 'id',
        values: ids
    });
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
                    var option = '<dd ondblclick="removeDdRow30(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="30"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
            //
            removeTableRow30(row);
        }
    });
}
function removeTableRow30(row) {
    var ids = [];
    ids.push(row.id);
    $('#jiguan30').bootstrapTable('remove', {
        field: 'id',
        values: ids
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
                    var option = '<dd ondblclick="removeDdRow31(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="31"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
            //
            removeTableRow31(row);
        }
    });
}

function removeTableRow31(row) {
    var ids = [];
    ids.push(row.id);
    $('#zhenb31').bootstrapTable('remove', {
        field: 'id',
        values: ids
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
                    var option = '<dd  ondblclick="removeDdRow32(this)" lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="32"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
            //
            removeTableRow32(row);
        }
    });
}

function removeTableRow32(row) {
    var ids = [];
    ids.push(row.id);
    $('#zhuqu32').bootstrapTable('remove', {
        field: 'id',
        values: ids
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
}

////////////////////////////////////////////////////////////////////////////////////


function sureSelect() {
    dangZhengBanSure();
    Sure30();
    Sure31();
    Sure32();

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
                var option = '<dd  ondblclick="removeDdRow29(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="29"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
        return row.id
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids
    });
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
                var option = '<dd ondblclick="removeDdRow30(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="30"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
        return row.id
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids
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
                var option = '<dd ondblclick="removeDdRow31(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '"  lay-username="' + tr_obj.name + '" lay-flag="31"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
        return row.id
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids
    });
}

function Sure32() {
    debugger;
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
                var option = '<dd  ondblclick="removeDdRow32(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="32"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
        return row.id
    });
    $table.bootstrapTable('remove', {
        field: 'id',
        values: ids
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
