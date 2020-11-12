<%--
 * 
 * @author Shengzhao Li
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fun" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>Home</title>
    <c:set var="contextPath" value="${pageContext.request.contextPath}" scope="application"/>
    <script src="${contextPath}/static/angular.min.js"></script>
    <script src="${contextPath}/static/jquery.min.js"></script>
    <script>
        function test(id) {
            $.post("archive_client/${cli.clientId}", function (data) {

            });
        }

        function sureDelete(id) {
            if (confirm("确定要删除数据吗？")) {
                location.href = "archive_client/" + id;
            }
        }
    </script>

</head>
<body>
<h2>认证服务
    <small class="badge" title="Version">${mainVersion}</small>
</h2>
<div>
    当前登录账户: <span class="text-success">${SPRING_SECURITY_CONTEXT.authentication.principal.username}</span>
    <form action="${contextPath}/signout" method="post">
        <tags:csrf/>
        <button class="btn btn-default" type="submit">退出</button>
    </form>
</div>
<sec:authorize access="hasRole('ROLE_ADMIN')">
    <div class="row">
        <div class="col-md-10">
            <h3>已注册应用信息</h3>
        </div>
        <div class="col-md-2">
            <div class="pull-right">
                <a href="register_client" class="btn btn-success btn-sm">注册应用</a>
            </div>
        </div>
    </div>
</sec:authorize>
<hr/>

<div class="list-group">
    <table class="table table-bordered" style="word-break:break-all;">
        <thead>
        <th>应用名称</th>
        <th>授权id</th>
        <th>授权密码</th>
        <th>第三方应用首页地址</th>
        <th>操作</th>
        </thead>
        <tbody>
        <c:forEach items="${clientDetailsDtoList}" var="cli">
            <tr>
                <td width="15%">${cli.clientName}</td>
                <td width="18%">${cli.clientId}</td>
                <sec:authorize access="hasRole('ROLE_ADMIN')">
                    <td width="18%">${cli.realsecret}</td>
                </sec:authorize>
                <sec:authorize access="!hasRole('ROLE_ADMIN')">
                    <td width="18%">${cli.clientSecret}</td>
                </sec:authorize>
                <td width="auto">${cli.webServerRedirectUri}</td>
                <td width="15%">
                    <sec:authorize access="hasRole('ROLE_ADMIN')">
                        <a href="register_client/${cli.clientId}" class="btn btn-info btn-sm">修改</a>
                        <%--                        <a href="archive_client/${cli.clientId}" class="btn btn-warning btn-sm">删除</a>--%>
                        <a href="javascript:sureDelete('${cli.clientId}')" class="btn btn-warning btn-sm">删除</a>
                    </sec:authorize>
                </td>
            </tr>
        </c:forEach>

        </tbody>

    </table>
</div>
</body>

</html>