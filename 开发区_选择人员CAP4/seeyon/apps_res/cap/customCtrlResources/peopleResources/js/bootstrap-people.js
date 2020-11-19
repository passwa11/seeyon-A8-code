layui.use(['element'], function () {
    var element = layui.element;
});

var idsdzbld = [];
var idsHwz = [];
$(function () {
    //dzbld:党政办领导
    tableCommon("dzbld");
    //
    dangZhengBanTable();
    jiguan30Table();
    zhenban31Table();
    zhuqu32Table();
    gongHuiTable();
    twoTable();
    //会务组
    hwzTable();

});

function dlCommon(ddType, row) {
    console.log("ddtype:====" + ddType);
    debugger;
    var tr_obj = row;
    var obj = {};//添加成员对象
    obj["value"] = tr_obj.id;
    obj["text"] = tr_obj.field0001;
    obj["dept"] = tr_obj.field0003;
    if ($("dl.selected-info dd").length <= 0) {
        var option = '<dd ondblclick="removeDbRow' + ddType + '(this)"    lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '" lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '" lay-flag="' + ddType + '" lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
            var option = '<dd ondblclick="removeDbRow' + ddType + '(this)"  lay-bsname="' + tr_obj.mval + '"  lay-field002="' + tr_obj.field0002 + '"  lay-zsort="' + tr_obj.field0007 + '"  lay-id="' + tr_obj.id + '" lay-bs="' + tr_obj.field0005 + '"  lay-value="' + obj.value + '" lay-username="' + tr_obj.name + '"  lay-flag="' + ddType + '"  lay-name="' + obj.text + '" lay-dept="' + obj.dept + '" class="">' + obj.text + '</dd>';
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
}

function hwzTable() {
    $('#hwz').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=hwz',
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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]
        , onCheck: function (row, $element) {
            dlCommon('hwz', row);
            dbSelectedToSortData('check');
            revokeReuse();
            removeTableRowhwz(row);
        }

    });
}

function removeTableRowhwz(row) {
    idsHwz.push(row.id);
    $('#hwz').bootstrapTable('remove', {
        field: 'id',
        values: idsHwz
    });
}

function removeDbRowhwz(item) {
    $(item).remove();

    insertSortExist('hwz', item);

    for (var i = 0; i < idsHwz.length; i++) {
        var index = idsHwz.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            idsHwz.splice(index, 1);
        }
    }
}


function tableCommon(tableId) {
    $('#' + tableId).bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=' + tableId,
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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]
        , onCheck: function (row, $element) {

            dlCommon('dzbld', row);

            dbSelectedToSortData('check');
            revokeReuse();

            removeTableRowdzbld(row);
        }

    });
}


function removeDbRowdzbld(item) {
    $(item).remove();

    insertSortExist('dzbld', item);

    for (var i = 0; i < idsdzbld.length; i++) {
        var index = idsdzbld.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            idsdzbld.splice(index, 1);
        }
    }
}

function removeTableRowdzbld(item) {
    $(item).remove();

    insertSortExist("dzbld", item);

    for (var i = 0; i < idsdzbld.length; i++) {
        var index = idsdzbld.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            idsdzbld.splice(index, 1);
        }
    }
}

function removeTableRowdzbld(row) {
    idsdzbld.push(row.id);
    $('#dzbld').bootstrapTable('remove', {
        field: 'id',
        values: idsdzbld
    });
}

function twoTable() {
    $('#two').bootstrapTable({
        url: '/seeyon/ext/selectPeople.do?method=selectListData&type=two',
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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]
        , onCheck: function (row, $element) {
            dlCommon('two', row);
            dbSelectedToSortData('check');
            revokeReuse();

            removeTableRowTwoLevelDept(row);
        }

    });

}


var idsTwoLevelDept = [];

function removeTableRowTwoLevelDept(row) {
    idsTwoLevelDept.push(row.id);
    $('#two').bootstrapTable('remove', {
        field: 'id',
        values: idsTwoLevelDept
    });
}

function removeDdRowtwo(item) {
    $(item).remove();

    insertSortExist('two', item);

    for (var i = 0; i < idsTwoLevelDept.length; i++) {
        var index = idsTwoLevelDept.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            idsTwoLevelDept.splice(index, 1);
        }
    }
}

function dbSelectedToSortData(flag) {
    debugger;
    var arrDzld = [];
    var arr29 = [];
    var arr30 = [];
    var arr31 = [];
    var arr32 = [];
    var arrGH = [];
    var arrTwoLevel = [];
    var arrHwz = [];

    var allData = [];
    if (flag == 'check') {
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
            allData.push(obj);
        });
    } else {
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
            } else if (flag == 'two') {
                arrTwoLevel.push(obj);
            } else if (flag == 'dzbld') {
                arrDzld.push(obj);
            } else if (flag == 'hwz') {
                arrHwz.push(obj);
            }
        });
    }

    var two = [];
    if (flag == 'check') {
        two = arrTwoLevel;
    } else {
        if (arrTwoLevel.length > 0) {
            two = arrsyDataSort(arrTwoLevel);
        }
    }

    var l29 = [];
    if (flag == 'check') {
        l29 = arr29;
    } else {
        if (arr29.length > 0) {
            l29 = arrsyDataSort(arr29);
        }
    }


    var l30 = [];
    if (flag == 'check') {
        l30 = arr30;
    } else {
        if (arr30.length > 0) {
            l30 = arrsyDataSort(arr30);
        }
    }


    var l31 = [];
    if (flag == 'check') {
        l31 = arr31;
    } else {
        if (arr31.length > 0) {
            l31 = arrsyDataSort(arr31);
        }
    }

    var l32 = [];
    if (flag == 'check') {
        l32 = arr32;
    } else {
        if (arr32.length > 0) {
            l32 = arrsyDataSort(arr32);
        }
    }


    var lgh = [];
    if (flag == 'check') {
        lgh = arrGH;
    } else {
        if (arrGH.length > 0) {
            lgh = arrsyDataSort(arrGH);
        }
    }

    var dzld = [];
    if (flag == 'check') {
        dzld = arrDzld;
    } else {
        if (arrDzld.length > 0) {
            dzld = arrsyDataSort(arrDzld);
        }
    }

    var hwz = [];
    if (flag == 'check') {
        hwz = arrHwz;
    } else {
        if (arrHwz.length > 0) {
            hwz = arrsyDataSort(arrHwz);
        }
    }

    var option = "";
    var html29 = '';
    var html30 = '';
    var html31 = '';
    var html32 = '';
    var htmlgh = '';
    var htmldzld = '';

    var htmlTwo = '';

    var htmlHwz = '';

    var allhtml = '';
    if (flag == 'check') {
        allhtml = AllhtmlShow(allData);
        $(".selected-info").html("");
        option += allhtml;
    } else {
        if (dzld.length > 0) {
            htmldzld = htmlShow(dzld, 'dzbld');
        }
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
        if (htmlTwo.length > 0) {
            htmlTwo = htmlShow(two, 'two');
        }
        if (htmlHwz.length > 0) {
            htmlHwz = htmlShow(hwz, 'hwz');
        }
        $(".selected-info").html("");
        option += htmldzld + html29 + html30 + html31 + html32 + htmlgh + htmlTwo + htmlHwz;
    }

    $("dl.selected-info").append(option);
    $(".selected-info dd").on('click', function () {
        var index = $(this).attr("class").indexOf("selected-this");
        if (index == 0) {
            $(this).removeClass("selected-this");
        } else {
            $(this).addClass("selected-this");
        }
    });
}

// 排序问题 对已选择人员进行部门排序
function dataSorting(arrdzbld0, arr290, arr300, arr310, arr320, arrGH0, arrTwoLevel, arrHwz00) {

    var arrdzbld = [];
    var arr29 = [];
    var arr30 = [];
    var arr31 = [];
    var arr32 = [];
    var arrGH = [];

    var arrTwo = [];
    var arrHw = [];

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
        } else if (flag == 'two') {
            arrTwo.push(obj);
        } else if (flag == 'dzbld') {
            arrdzbld.push(obj);
        } else if (flag == 'hwz') {
            arrHw.push(obj);
        }
    });

    var hw = [];
    if (arrHwz00 != null) {
        if (arrHw != null) {
            hw = arrsyDataSort(arrHw.concat(arrHwz00));
        } else {
            hw = arrsyDataSort(arrHwz00);
        }
    } else {
        if (arrHw != null) {
            hw = arrHw;
        } else {
            hw = arrsyDataSort(arrHw);
        }
    }

    var ld = [];
    if (arrdzbld0 != null) {
        if (arrdzbld != null) {
            ld = arrsyDataSort(arrdzbld.concat(arrdzbld0));
        } else {
            ld = arrsyDataSort(arrdzbld0);
        }
    } else {
        if (arrdzbld != null) {
            ld = arrdzbld;
        } else {
            ld = arrsyDataSort(arrdzbld);

        }
    }


    var two = [];
    if (arrTwoLevel != null) {
        if (arrTwo != null) {
            two = arrsyDataSort(arrTwo.concat(arrTwoLevel));
        } else {
            two = arrsyDataSort(arrTwoLevel);
        }
    } else {
        if (arrTwo != null) {
            two = arrTwo;
        } else {
            two = arrsyDataSort(arrTwo);

        }
    }


    var l29 = [];
    if (arr290 != null) {
        if (arr29 != null) {
            l29 = arrsyDataSort(arr29.concat(arr290));
            // l29 = arr29.concat(arr290);
        } else {
            l29 = arrsyDataSort(arr290);
        }
    } else {
        if (arr29 != null) {
            l29 = arr29;
        } else {
            l29 = arrsyDataSort(arr29);

        }
    }

    var l30 = [];
    if (arr300 != null) {
        if (arr30 != null) {
            l30 = arrsyDataSort(arr30.concat(arr300));
            // l30 = arr30.concat(arr300);
        } else {
            l30 = arrsyDataSort(arr300);
        }
    } else {
        if (arr30 != null) {
            l30 = arr30;
        } else {
            l30 = arrsyDataSort(arr30);

        }
    }

    var l31 = [];
    if (arr310 != null) {
        if (arr31 != null) {
            l31 = arrsyDataSort(arr31.concat(arr310));
            // l31 = arr31.concat(arr310);
        } else {
            l31 = arrsyDataSort(arr310);
        }
    } else {
        if (arr31 != null) {
            l31 = arr31;
        } else {
            l31 = arrsyDataSort(arr31);
        }
    }

    var l32 = [];
    if (arr320 != null) {
        if (arr32 != null) {
            l32 = arrsyDataSort(arr32.concat(arr320));
            // l32 = arr32.concat(arr320);
        } else {
            l32 = arrsyDataSort(arr320);
        }
    } else {
        if (arr32 != null) {
            l32 = arr32;
        } else {
            l32 = arrsyDataSort(arr32);
        }
    }

    var lgh = [];
    if (arrGH0 != null) {
        if (arrGH != null) {
            lgh = arrsyDataSort(arrGH.concat(arrGH0));
            // lgh = arrGH.concat(arrGH0);
        } else {
            lgh = arrsyDataSort(arrGH0);
        }
    } else {
        if (arrGH != null) {
            lgh = arrGH;
        } else {
            lgh = arrsyDataSort(arrGH);
        }
    }

    var option = "";
    var htmlld = "";
    var html29 = '';
    var html30 = '';
    var html31 = '';
    var html32 = '';
    var htmlgh = '';

    var htmlTwo = '';
    var htmlHw = '';
    $(".selected-info").html("");
    if (ld.length > 0) {
        htmlld = htmlShow(ld, 'dzbld');
    }

    $(".selected-info").html("");
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
    if (two.length > 0) {
        htmlTwo = htmlShow(two, 'two');
    }
    if (hw.length > 0) {
        htmlHw = htmlShow(hw, 'hwz');
    }

    option += htmlld + html29 + html30 + htmlTwo + html31 + html32 + htmlgh + htmlHw;
    $("dl.selected-info").append(option);
    $(".selected-info dd").on('click', function () {
        var index = $(this).attr("class").indexOf("selected-this");
        if (index == 0) {
            $(this).removeClass("selected-this");
        } else {
            $(this).addClass("selected-this");
        }
    });
}

function AllhtmlShow(data) {
    var html = "";
    for (var i = 0; i < data.length; i++) {
        var flag = '';
        if ((data[i].flag).indexOf("29") != -1) {
            flag = '29';
        } else if ((data[i].flag).indexOf("30") != -1) {
            flag = '30';
        } else if ((data[i].flag).indexOf("31") != -1) {
            flag = '31';
        } else if ((data[i].flag).indexOf("32") != -1) {
            flag = '32';
        } else if ((data[i].flag).indexOf("gh") != -1) {
            flag = 'gh';
        } else if ((data[i].flag).indexOf("two") != -1) {
            flag = 'two';
        } else if ((data[i].flag).indexOf("dzbld") != -1) {
            flag = 'dzbld';
        }
        html += '<dd ondblclick="removeDdRow' + flag + '(this)" lay-bsname="' + data[i].mval + '"  lay-field002="' + data[i].field0002 + '"  lay-zsort="' + data[i].field0007 + '" lay-id="' + data[i].id + '" lay-bs="' + data[i].field0005 + '"  lay-value="' + data[i].id + '" lay-username="' + data[i].name + '" lay-flag="' + flag + '" lay-name="' + data[i].field0001 + '" lay-dept="' + data[i].field0003 + '" class="">' + data[i].field0001 + '</dd>';
    }
    return html;
}

function htmlShow(data, flag) {
    var html = "";
    for (var i = 0; i < data.length; i++) {
        html += '<dd ondblclick="removeDdRow' + flag + '(this)" lay-bsname="' + data[i].mval + '"  lay-field002="' + data[i].field0002 + '"  lay-zsort="' + data[i].field0007 + '" lay-id="' + data[i].id + '" lay-bs="' + data[i].field0005 + '"  lay-value="' + data[i].id + '" lay-username="' + data[i].name + '" lay-flag="' + flag + '" lay-name="' + data[i].field0001 + '" lay-dept="' + data[i].field0003 + '" class="">' + data[i].field0001 + '</dd>';
    }
    return html;
}

function arrsyDataSort(data) {
    for (var i = 0; i < data.length - 1; i++) {
        for (var j = 0; j < data.length - 1 - i; j++) {
            if (parseInt(data[j].field0007) > parseInt(data[j + 1].field0007)) {
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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]
        , onCheck: function (row, $element) {
            dlCommon('gh', row);
            dbSelectedToSortData('check');
            revokeReuse();

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

    insertSortExist('gonghui', item);

    for (var i = 0; i < idsgh.length; i++) {
        var index = idsgh.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            idsgh.splice(index, 1);
        }
    }
}

function insertDataCommon(id, i, item) {
    $('#' + id).bootstrapTable('insertRow', {
        index: i,
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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ],
        onCheck: function (row, $element) {
            dlCommon('29', row);

            dbSelectedToSortData('check');
            revokeReuse();
            removeTableRow29(row);

        }
    });
}

//解决冲突
function revokeReuse() {
    if (idsHwz.length > 0) {
        $('#hwz').bootstrapTable('remove', {
            field: 'id',
            values: idsHwz
        });
    }
    if (idsdzbld.length > 0) {
        $('#dzbld').bootstrapTable('remove', {
            field: 'id',
            values: idsdzbld
        });
    }

    if (idsTwoLevelDept.length > 0) {
        $('#two').bootstrapTable('remove', {
            field: 'id',
            values: idsTwoLevelDept
        });
    }

    if (ids29.length > 0) {
        $('#dzb29').bootstrapTable('remove', {
            field: 'id',
            values: ids29
        });
    }
    if (ids30.length > 0) {
        $('#jiguan30').bootstrapTable('remove', {
            field: 'id',
            values: ids30
        });
    }
    if (ids31.length > 0) {
        $('#zhenb31').bootstrapTable('remove', {
            field: 'id',
            values: ids31
        });
    }
    if (ids32.length > 0) {
        $('#zhuqu32').bootstrapTable('remove', {
            field: 'id',
            values: ids32
        });
    }
    if (idsgh.length > 0) {
        $('#gonghui').bootstrapTable('remove', {
            field: 'id',
            values: idsgh
        });
    }
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
    insertSortExist('dzb29', item);

    for (var i = 0; i < ids29.length; i++) {
        var index = ids29.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            ids29.splice(index, 1);
        }
    }
}


function insertSortExist(id, item) {
    var data = $('#' + id).bootstrapTable('getData', {useCurrentPage: false, includeHiddenRows: false});
    if (data.length > 0) {
        var sort_ = parseInt($(item).attr("lay-zsort"));
        for (var i = 0; i < data.length; i++) {
            var _s = parseInt(data[i].field0007);
            var _end = parseInt(data[(i + 1) >= data.length ? i : (i + 1)].field0007);
            if (sort_ > _s & sort_ < _end) {
                insertDataCommon(id, i + 1, item);
                break;
            } else if (sort_ < _s) {
                insertDataCommon(id, i, item);
                break;
            } else {
                if ((i + 1) >= data.length) {
                    insertDataCommon(id, data.length, item);
                    break
                }
            }
        }
    } else {
        insertDataCommon(id, 0, item);
    }

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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]
        , onCheck: function (row, $element) {
            dlCommon('30', row);
            dbSelectedToSortData('check');
            revokeReuse();

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
    insertSortExist('jiguan30', item);

    for (var i = 0; i < ids30.length; i++) {
        var index = ids30.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            ids29.splice(index, 1);
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
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]

        , onCheck: function (row, $element) {

            dlCommon('31', row);

            dbSelectedToSortData('check');
            revokeReuse();

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

    insertSortExist('zhenb31', item);

    for (var i = 0; i < ids31.length; i++) {
        var index = ids31.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            ids31.splice(index, 1);
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
        sidePagination: "true",
        striped: false,                   //是否显示行间隔色
        cache: false,
        pagination: false,
        sortable: true, //是否启用排序
        sortOrder: "asc",//排序方式
        showRefresh: true,
        search: true,
        searchAlign: 'left',
        height: 380,
        virtualScroll: true,
        silentSort: true,
        showColumns: false,
        detailView: false,
        clickToSelect: true,                //是否启用点击选中行
        columns: [
            {checkbox: true, width: '5%'}
            , {
                field: 'field0001',
                title: '全选',
                width: '88%',
            }
        ]
        , onCheck: function (row, $element) {
            dlCommon('32', row);
            dbSelectedToSortData('check');
            revokeReuse();
            removeTableRow32(row);

        }

    });
}

var ids32 = [];

function removeTableRow32(row) {
    ids32.push(row.id);
    $('#zhuqu32').bootstrapTable('remove', {
        field: 'id',
        values: ids32
    });
}

function removeDdRow32(item) {
    $(item).remove();

    insertSortExist('zhuqu32', item);

    for (var i = 0; i < ids32.length; i++) {
        var index = ids32.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            ids32.splice(index, 1);
        }
    }
}


////////////////////////////////////////////////////////////////////////////////////


function sureSelect() {
    debugger;
    var $tablehwz = $("#hwz");
    var rowshwz = $tablehwz.bootstrapTable('getSelections');
    if (rowshwz != null) {
        if (rowshwz.length > 0) {
            sureCommon($tablehwz, rowshwz, 'hwz', idsHwz);
        }
    }

    var $tabledzbld = $("#dzbld");
    var rowsdzbld = $tabledzbld.bootstrapTable('getSelections');
    if (rowsdzbld != null) {
        if (rowsdzbld.length > 0) {
            sureCommon($tabledzbld, rowsdzbld, 'dzbld', idsdzbld);
        }
    }

    var $tableTwoLevelDept = $("#two");
    var rowsTwo = $tableTwoLevelDept.bootstrapTable('getSelections');
    if (rowsTwo != null) {
        if (rowsTwo.length > 0) {
            sureCommon($tableTwoLevelDept, rowsTwo, 'two', idsTwoLevelDept);
        }
    }

    var $table29 = $('#dzb29');
    var rows29 = $table29.bootstrapTable('getSelections');
    if (rows29 != null) {
        if (rows29.length > 0) {
            sureCommon($table29, rows29, '29', ids29);
        }
    }
    var $table30 = $('#jiguan30');
    var rows30 = $table30.bootstrapTable('getSelections');
    if (rows30 != null) {
        if (rows30.length > 0) {
            sureCommon($table30, rows30, '30', ids30);
        }
    }
    var $table31 = $('#zhenb31');
    var rows31 = $table31.bootstrapTable('getSelections');

    if (rows31 != null) {
        if (rows31.length > 0) {
            sureCommon($table31, rows31, '31', ids31);
        }
    }

    var $table32 = $('#zhuqu32');
    var rows32 = $table32.bootstrapTable('getSelections');
    if (rows32 != null) {
        if (rows32.length > 0) {
            sureCommon($table32, rows32, '32', ids32);
        }
    }

    var $tableGh = $('#gonghui');
    var rowsGh = $tableGh.bootstrapTable('getSelections');
    if (rowsGh != null) {
        if (rowsGh.length > 0) {
            sureCommon($tableGh, rowsGh, 'gh', idsgh);

        }
    }

    if (ids29.length > 0) {
        $('#dzb29').bootstrapTable('remove', {
            field: 'id',
            values: ids29
        });
    }
    if (ids30.length > 0) {
        $('#jiguan30').bootstrapTable('remove', {
            field: 'id',
            values: ids30
        });
    }
    if (ids31.length > 0) {
        $('#zhenb31').bootstrapTable('remove', {
            field: 'id',
            values: ids31
        });
    }
    if (ids32.length > 0) {
        $('#zhuqu32').bootstrapTable('remove', {
            field: 'id',
            values: ids32
        });
    }
    if (idsgh.length > 0) {
        $('#gonghui').bootstrapTable('remove', {
            field: 'id',
            values: idsgh
        });
    }

    if (idsTwoLevelDept.length > 0) {
        $('#two').bootstrapTable('remove', {
            field: 'id',
            values: idsTwoLevelDept
        });
    }

}

function sureCommon($table, rows, type, idsArr) {
    switch (type) {
        case "hwz":
            dataSorting(null, null, null, null, null, null, null, rows);
            break;
        case "dzbld":
            dataSorting(rows, null, null, null, null, null, null, null);
            break;
        case "two":
            dataSorting(null, null, null, null, null, null, rows, null);
            break;
        case "29":
            dataSorting(null, rows, null, null, null, null, null, null);
            break;
        case "30":
            dataSorting(null, null, rows, null, null, null, null, null);
            break;
        case "31":
            dataSorting(null, null, null, rows, null, null, null, null);
            break;
        case "32":
            dataSorting(null, null, null, null, rows, null, null, null);
            break;
        case "gh":
            dataSorting(null, null, null, null, null, rows, null, null);
            break;
        default:
            break;
    }

    idsArr = $.map(rows, function (row) {
        return row.id
    });
    $("dl").find('dd').each(function () {
        var id = $(this).attr("lay-id");
        var flag = $(this).attr("lay-flag");
        if (flag == type) {
            if (undefined != id) {
                idsArr.push(id);
            }
        }
    });

    $table.bootstrapTable('remove', {
        field: 'id',
        values: idsArr
    });
}


function commonInfo(s) {

    var list = $("dl").find("dd" + s);
    $.each(list, function (i, item) {
        $(".selected-info dd[lay-value=" + $(item).attr('lay-value') + "]").remove();
        var type = $(item).attr("lay-flag");
        if (type == '29') {
            idsHandler('dzb29', ids29, item);
        }
        if (type == '30') {
            idsHandler('jiguan30', ids30, item);
        }
        if (type == '31') {
            idsHandler('zhenb31', ids31, item);
        }
        if (type == '32') {
            idsHandler('zhuqu32', ids32, item);

        }
        if (type == 'gh') {
            idsHandler('gonghui', idsgh, item);
        }

        if (type == 'two') {
            idsHandler('two', idsTwoLevelDept, item);
        }

        if (type == 'dzbld') {
            idsHandler('dzbld', idsdzbld, item);
        }
        if (type == 'hwz') {
            idsHandler('hwz', idsHwz, item);
        }

    });
}

function idsHandler(type, ids, item) {
    insertSortExist(type, item);
    for (var i = 0; i < ids.length; i++) {
        var index = ids.indexOf($(item).attr("lay-id"));
        if (index > -1) {
            ids.splice(index, 1);
        }
    }
}

function removeSelect() {
    commonInfo('.selected-this');
}

function clearSelect2() {

    var list = $("dl").find("dd");
    $.each(list, function (i, item) {
        $(".selected-info dd[lay-value=" + $(item).attr('lay-value') + "]").remove();
        var type = $(item).attr("lay-flag");
        if (type == '29') {
            insertSortExist('dzb29', item);
        }
        if (type == '30') {
            insertSortExist('jiguan30', item);
        }
        if (type == '31') {
            insertSortExist('zhenb31', item);
        }
        if (type == '32') {
            insertSortExist('zhuqu32', item);
        }
        if (type == 'gh') {
            insertSortExist('gonghui', item);
        }
        if (type == 'two') {
            insertSortExist('two', item);
        }

        if (type == 'dzbld') {
            insertSortExist('dzbld', item);
        }

    });
    ids29 = [];
    ids30 = [];
    ids31 = [];
    ids32 = [];
    idsgh = [];
    idsTwoLevelDept = [];
    idsdzbld = [];
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
