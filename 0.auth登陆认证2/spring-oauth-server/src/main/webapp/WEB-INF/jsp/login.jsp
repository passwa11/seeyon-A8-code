<%--
 * 
 * @author Shengzhao Li
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>OAuth Login</title>
    <meta charset="utf-8"/>
    <c:set var="contextPath" value="${pageContext.request.contextPath}" scope="application"/>

</head>

<body >
<%--powderblue--%>
<h2 style="margin-bottom: 20px;margin-top:130px;text-align:center;color:black;font-weight:bold;">认 证 登 录</h2>

<div class="row" style="padding-left:250px;" >
    <div class="col-md-9"  >

        <div class="panel panel-default" style="background-color:rgba(0,0,0,0.0001);box-shadow: none;border:none;">
            <div class="panel-body">

                <form style="margin-top:20px" action="${pageContext.request.contextPath}/signin" method="post" class="form-horizontal">
                    <tags:csrf/>
                    <div class="form-group" padding-top: 25px;>
                        <label for="username" class="col-sm-3 control-label">用户名</label>

                        <div class="col-sm-6">
                            <input style="background-color:rgba(255,255,255,0.4);" type="text" id="username" name="oidc_user" value="" placeholder="用户名"
                                   required="required" class="form-control"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password" class="col-sm-3 control-label">密码</label>

                        <div class="col-sm-6">
                            <input style="background-color:rgba(255,255,255,0.4);" type="password" name="oidcPwd" id="password" value="" placeholder="密码"
                                   required="required" class="form-control"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password" class="col-sm-3 control-label"></label>

                        <div class="col-sm-9" >
                            <input type="submit" value="登录"  style="width:299px" class="btn btn-primary"/>
                            <%--Login error--%>
                            <c:if test="${param.error eq '2'}"><span
                                    class="label label-danger">拒绝访问!!!</span></c:if>
                            <c:if test="${param.error eq '1'}"><span
                                    class="label label-danger">身份验证失败！</span></c:if>
                        </div>
                    </div>

                </form>
            </div>
        </div>

    </div>
    <%--<div class="col-md-6">
        <p>你可以使用以下几个初始的账号进行登录:</p>
        <table class="table table-bordered">
            <thead>
            <tr>
                <th>Username</th>
                <th>Password</th>
                <th>Remark</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>admin</td>
                <td>admin</td>
                <td>All privileges, allow visit [Mobile] and [Unity] resources, manage user</td>
            </tr>
            <tr>
                <td>unity</td>
                <td>unity</td>
                <td>Only allow visit [Unity] resource, support grant_type:
                    <em>authorization_code,refresh_token,implicit</em></td>
            </tr>
            <tr>
                <td>mobile</td>
                <td>mobile</td>
                <td>Only allow visit [Mobile] resource, support grant_type: <em>password,refresh_token</em></td>
            </tr>
            </tbody>
        </table>
    </div>--%>
</div>

</body>
</html>