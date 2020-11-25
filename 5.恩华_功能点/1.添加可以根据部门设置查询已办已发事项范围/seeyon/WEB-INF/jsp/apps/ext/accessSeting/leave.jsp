<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <%@ include file="../../../common/common.jsp" %>
</head>
<body>
<div class="content_wrap">
    <div id="postform" class="form_area">
        <br>
        <br>
        <br>
        <br>
        <br>
        <div class="one_row">
            <fieldset>
                <legend>
                    设置离职人员查看已发已办等数据
                </legend>
                <table border="0" cellspacing="0" cellpadding="0">
                    <tbody>
                    <tr>
                        <th nowrap="nowrap">
                            <label class="margin_r_10">
                                是否允许:
                            </label>
                        </th>
                        <td width="100%">
                            <input type="hidden" id="isenableval" value="${leave.isEnable}">
                            <div class="common_radio_box clearfix">
                                <label for="radio22" class="margin_r_10 hand">
                                    <input type="radio" value="1" id="radio22" name="isEnable" class="radio_com"
                                           checked>允许
                                </label>
                                <label for="radio11" class="margin_r_10 hand">
                                    <input type="radio" value="0" id="radio11" name="isEnable"
                                           class="radio_com">不允许
                                </label>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </fieldset>

        </div>
        <div class="layui-input-inline" style="margin-top: 20px;text-align: center;">
            <button class="common_button common_button_emphasize" id="saveRange">保存</button>
        </div>
    </div>
</div>

</body>
<script type="text/javascript">
    $(function () {
        var enable = $("#isenableval").val();
        if (enable == 1) {
            $("#radio22").attr("checked", "checked");
        }
        if (enable == 0) {
            $("#radio11").attr("checked", "checked");
        }
        $("#saveRange").on('click', function () {
            var obj = {};
            var val = $(":radio:checked").val();
            obj['isEnable'] = val;
            $.post("/seeyon/ext/accessSetting.do?method=saveLeaveSeting", obj, function (ref) {
                if (ref.code == 0) {
                    $("#info").append("保存成功！");
                } else {
                    $("#info").append("保存失败！");
                }
            });
            setTimeout(function () {
                $("#info").replaceWith("");
            }, 2000);
        });
    });


</script>
</html>
