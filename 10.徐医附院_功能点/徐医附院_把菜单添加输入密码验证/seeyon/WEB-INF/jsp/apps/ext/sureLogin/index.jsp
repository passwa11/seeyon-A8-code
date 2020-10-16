<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js" />"></script>

    <script type="text/javascript">
        function OK() {
            var info = new Array();
            var obj = {};
            var check = $("#newPassword").val();
            if (undefined != check && check != '') {
                obj['flag'] = 'login';
                obj['password'] = $("#newPassword").val();
            } else {
                var find=$("#answer").val();
                if(undefined != find && find != ''){
                    obj['flag'] = 'find';
                    obj['answer'] = $("#answer").val();
                }else{
                    obj['flag'] = 'set';
                    obj['firstPwd'] = $("#firstPassword").val();
                    obj['surePwd'] = $("#surePassword").val();
                    obj['answer'] = $.trim($("#answer1").val());
                }

            }

            info.push(obj);
            return {
                valid: true,
                data: info
            };
        }
    </script>
    <style>
        span {
            color: red;
        }
    </style>
</head>
<body>
<table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0" class="popupTitleRight" style="background:#fafafa;">
    <tr>
        <td height="5" colspan="2"></td>
    </tr>
    <c:choose>
        <c:when test="${first==true}">
            <tr valign="middle">
                <td width="50"></td>
                <td nowrap="nowrap" align="left"><label for="firstPassword">
                    <font color="red">*</font>请设置密码:</label>
                </td>
            </tr>
            <tr>
                <td width="50"></td>
                <td class="new-column">
                    <input type="password" maxlength="50" id="firstPassword" style="width:262px" class="input-80per"
                           inputName="" onblur="mouseLeave('firstPassword','err1','surePassword')" name="firstPassword" validate="notNull">
                </td>
                <td><span id="err1"></span></td>
            </tr>
            <tr valign="middle">
                <td width="50"></td>
                <td nowrap="nowrap" align="left">
                    <label for="surePassword"><font color="red">*</font>确认密码:</label>
                </td>
            </tr>
            <tr>
                <td width="50"></td>
                <td class="new-column">
                    <input type="password" maxlength="50" id="surePassword" style="width:262px" class="input-80per"
                           name="surePassword" onblur="surecheck()" validate="notNull">
                </td>
                <td><span id="err2"></span></td>
            </tr>
            <tr>
                <td colspan="5">
                    <hr>
                </td>
            </tr>
            <tr style="height: 20px;">
                <td colspan="5">密码找回设置：</td>
            </tr>
            <tr valign="middle">
                <td width="50"></td>
                <td nowrap="nowrap" align="left">
                    <label for="answer1"><font color="red">*</font>你最喜欢的小学老师是:</label>
                </td>
            </tr>
            <tr>
                <td width="50"></td>
                <td class="new-column">
                    <input type="text" maxlength="50" id="answer1" style="width:262px" class="input-80per"
                           name="answer1" validate="notNull" onblur="mouseLeave('answer1','err3')">
                </td>
                <td><span id="err3"></span></td>
            </tr>
        </c:when>
        <c:otherwise>
            <tr class="a3" valign="middle">
                <td width="50"></td>
                <td nowrap="nowrap" align="left"><label for="newPassword">
                    <font color="red">*</font>请输入密码:</label>
                </td>
            </tr>
            <tr class="a3" valign="top">
                <td width="50"></td>
                <td class="new-column">
                    <input type="password" maxlength="50" id="newPassword" style="width:262px" class="input-80per"
                           inputName="" name="newPassword">
                </td>
            </tr>
            <tr class="a3" valign="middle">
                <td width="50"></td>
                <td nowrap="nowrap" align="center">
                    <label style="color: #4886e2"><a href="#" onclick="findPwd()">忘记密码?</a></label>
                </td>
            </tr>

            <tr class="a1" valign="middle" hidden>
                <td width="50"></td>
                <td nowrap="nowrap" align="left">
                    <label for="answer1">你最喜欢的小学老师是:</label>
                </td>
            </tr>
            <tr class="a1" hidden>
                <td width="50"></td>
                <td >
                    <span>请填写你的答案</span><br>
                    <input type="text" maxlength="50" id="answer" style="width:262px" class="input-80per"
                           name="answer" validate="notNull" >
                </td>
                <td><span id="err4"></span></td>
            </tr>
            <tr class="a1" hidden valign="middle">
                <td width="50"></td>
                <td nowrap="nowrap" align="center">
                    <label style="color: #4886e2"><a href="#" onclick="inputPwd()">输入密码登录</a></label>
                </td>
            </tr>


        </c:otherwise>
    </c:choose>

    <tr>
        <td height="5" colspan="2"></td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
    </tr>

</table>
</body>
<script>
    $(function () {
        $("#firstPassword").focus();
        $("#newPassword").val("");
    });

    function inputPwd(){
        $("#newPassword").val("");
        $("#answer").val("");
        $(".a1").hide();
        $(".a3").show();
    }
    function findPwd() {
        $("#newPassword").val("");
        $(".a1").show();
        $(".a3").hide();
    }

    function surecheck() {
        var first = $.trim($("#surePassword").val());
        if (first == '') {
            $("#err2").html("请确认密码");
        } else {
            var first = $("#firstPassword").val();
            var second = $("#surePassword").val();
            if (first != second) {
                $("#err2").html("确认密码不正确！");
            } else {
            }
        }
    }

    function mouseLeave(id, err, next) {
        var first = $.trim($("#" + id).val());
        if (first == '') {
            $("#" + id).focus();
            $("#" + err).html("请输入内容！");
        } else {
            $("#" + err).html("");
            if (next != undefined && next != null && next != '') {
                $("#" + next).focus();
            }
        }
    }
</script>
</HTML>
