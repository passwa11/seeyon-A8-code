<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
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
<BODY>
<div style="margin-top: 50px;margin-left: 50px;">
    <button class="button" onclick="syncData()" id="btn">组织机构同步</button>
</div>

<script type="text/javascript">
    function syncData() {
        $("#btn").attr('disabled', true);
        var proce = $.progressBar({
            text: "正在同步组织信息...."
        });
        $.post("/seeyon/ext/xzyk.do?method=syncDept", null, function (data) {
            if (data.code == 0) {
                $("#btn").attr('disabled', false);
                proce.close();
                alert("同步完成！");

            } else {
                $("#btn").attr('disabled', false);
                proce.close();
                alert("同步失败！");
            }
        });
    }
</script>
</BODY>
</HTML>