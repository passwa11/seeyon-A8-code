<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script type="text/javascript" src="/seeyon/common/toastr/toastr.min.js"></script>
<link href="/seeyon/common/toastr/toastr.min.css" rel="stylesheet" type="text/css"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>账户设置</title>
    <style type="text/css">

        .button {
            background-color: #4CAF50;
            border: none;
            color: white;
            padding: 10px 20px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }

    </style>
</head>

<body>
<center>
    <form id="lawForm" name="lawForm" method="post">

        <div style="width: 500px;margin-top: 50px;" align="center">
            <fieldset>
                <legend style="font-size: 16px;color: #030303"> 档案系统设置</legend>
                <br>
                <table width="70%" border="0" cellspacing="0" cellpadding="0" height="100px;"
                       align="center">
                    <tr>
                        <td class="bg-gray" width="20%" nowrap="nowrap">用户名:</td>
                        <td class="new-column" width="80%">
                            <input class="input-100per" style="width: 200px;" type="text" name="record_user" id="record_user"
                                   maxlength="50" value="${requestScope.userPas.record_user}"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="bg-gray" width="20%" nowrap="nowrap">密码:</td>
                        <td class="new-column" width="80%">
                            <input class="input-100per" style="height: 28px;width: 200px;" type="password"
                                   name="record_pas" id="record_pas" maxlength="50" value="${requestScope.userPas.record_pas}"/>
                        </td>
                    </tr>
                </table>
            </fieldset>
            <div id="btnDiv">
                <input type="reset" style="border: none;color: #030303;padding: 10px 20px;text-align: center;
            text-decoration: none;display: inline-block;font-size: 16px;margin: 4px 2px;cursor: pointer;" value="取消"/>
                <input type="button" class="button" onclick="setLaw()" value="确定"/>&nbsp;&nbsp;
            </div>

        </div>
    </form>
</center>
</body>
<script type="text/javascript">
    toastr.options = {
        closeButton: false,  	//是否显示关闭按钮（提示框右上角关闭按钮）。
        debug: false,  			//是否为调试。
        progressBar: false,  	//是否显示进度条（设置关闭的超时时间进度条）
        positionClass: "toast-top-center",  	//消息框在页面显示的位置
        onclick: null,  		//点击消息框自定义事件
        showDuration: "300",  	//显示动作时间
        hideDuration: "1000",  	//隐藏动作时间
        timeOut: "2000",  		//自动关闭超时时间
        extendedTimeOut: "1000",
        showEasing: "swing",
        hideEasing: "linear",
        showMethod: "fadeIn",  	//显示的方式，和jquery相同
        hideMethod: "fadeOut"  	//隐藏的方式，和jquery相同
        //等其他参数
    };
    function setLaw() {
        var record_user = document.getElementById('record_user').value;
        var record_pas = document.getElementById('record_pas').value;
        if (record_user == '' || record_pas == '') {
            alert('请输入正确的用户名、密码');
            return false;
        }
        $.post("/seeyon/ext/documentSetUserController.do?method=setResult", {
            record_user: record_user,
            record_pas: record_pas
        }, function (data) {
            if (data.code == 0) {
                $("#btnDiv").hide();
                toastr.success('设置成功');
            } else {
                toastr.error('设置失败');
            }
        });
    }
</script>
</html>
