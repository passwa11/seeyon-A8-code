<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
                <legend style="font-size: 16px;color: #030303"> 法律系统设置</legend>
                <br>
                <table width="70%" border="0" cellspacing="0" cellpadding="0" height="100px;"
                       align="center">
                    <tr>
                        <td class="bg-gray" width="20%" nowrap="nowrap">用户名:</td>
                        <td class="new-column" width="80%">
                            <input class="input-100per" style="width: 200px;" type="text" name="law_user" id="law_user"
                                   maxlength="50" value="${requestScope.loginname}"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="bg-gray" width="20%" nowrap="nowrap">密码:</td>
                        <td class="new-column" width="80%">
                            <input class="input-100per" style="height: 28px;width: 200px;" type="password" name="law_pas" id="law_pas"
                                   maxlength="50" value="${requestScope.pwd}"/>
                        </td>
                    </tr>
                </table>
            </fieldset>

            <input type="reset" style="border: none;
            color: #030303;
            padding: 10px 20px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;" value="取消"/>

            <input type="button" class="button" onclick="return setLaw(this.form)" value="确定"/>&nbsp;&nbsp;
        </div>
    </form>
</center>
</body>
</html>
