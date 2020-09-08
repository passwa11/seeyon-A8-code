<%--
 $Author: lilong $
 $Rev: 4423 $
 $Date:: 2012-09-24 18:13:06#$:
  
 Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 This software is the proprietary information of Seeyon, Inc.
 Use is subject to license terms.
--%>
<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html class="over_hidden h100b">
<head>
<title>Member2Role</title>
<script type="text/javascript" src="${pageContext.request.contextPath}/ajax.do?managerName=xkjtManager"></script>
<script type="text/javascript" language="javascript">
var isBat = window.dialogArguments;
var isBat = window.parentDialogObj["xkjtRole"].getTransParams();//已选数据
//请勿轻易修改这个变量不仅批量关闭窗口用，角色回填也需要回传值
$().ready(function() {
    //列表
    var grid = $("#accountRoleTable").ajaxgrid({
        gridType:'autoGrid',
        colModel: [{
            display: 'id',
            name: 'id',
            width: 'small',
            align: 'center',
            type: 'checkbox'
        },
        {
            display : "${ctp:i18n('role.name')}",
            name : 'showName',
            width : 'big'
        }],
        slideToggleBtn: false,
        showTableToggleBtn: false,
        resizable: false,
        striped:true,
        usepager: false,
        width: "auto",
        height:"300",
        managerName: "xkjtManager",
        managerMethod: "findAllRoles",
        onSuccess: function() {
            if((isBat != 'true' || isBat != 'false') && '' != isBat && isBat!=undefined) {
                var temRoles = isBat.split(",");
                for(i=0;i<temRoles.length;i++) {
                    $("input[value='"+temRoles[i]+"']").attr("checked","checked");
                }
            }
        }
    });
    //加载表格
    var o1 = new Object();
    o1.orgAccountId = "${accountId}";
    $("#accountRoleTable").ajaxgridLoad(o1);
});
function OK() {
    var roles = new Array();
    var boxs = $("#accountRoleTable input:checked");
    if (boxs.length >= 1) {
        boxs.each(function() {
            roles.push($(this).val());
        });
    }
    if('true' == isBat) {
        window.close();
    } else {
//        window.parent.dialog4Role.close();
    }   
    return roles;
}
</script>
</head>
<body class="over_hidden h100b">
    <div id='layout' class="comp" comp="type:'layout'">
        <div class="layout_center" id="center" layout="border:false" style='overflow:hidden;overflow-y:hidden'>
            <table id="accountRoleTable" class="flexme3" style="display: none"></table>
        </div>
    </div>
</body>
</html>