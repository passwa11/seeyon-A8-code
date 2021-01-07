<%--
 * 
 * @author Shengzhao Li
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fun" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE HTML>
<html>
<head>
    <title>注册应用</title>
    <script src="${contextPath}/static/angular.min.js"></script>
</head>
<body>
<div class="row">
    <div class="col-md-1"></div>
    <div class="col-md-10" style="background-color:rgb(255,255,255);height: 900px;">
        <%--        <span style="float: right;margin-top: 20px;">--%>
        <%--            <a href="/server/" class="btn btn-info">返回</a>--%>
        <%--        </span>--%>
        <h2>注册应用</h2>
        <hr/>
        <div ng-app style="">
            <div ng-controller="RegisterClientCtrl">
                <form:form modelAttribute="formDto" cssClass="form-horizontal">
                    <div class="form-group">
                        <label for="clientId" class="col-sm-2 control-label">应用名称<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="clientName" name="clientName" placeholder=""
                                   value="${formDto.clientName()}">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="clientId" class="col-sm-2 control-label">应用id(client_id)<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="clientId" name="clientId" placeholder=""
                                   value="${formDto.clientId()}" readonly="readonly">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="clientSecret" class="col-sm-2 control-label">授权秘钥(client_secret)<em
                                class="text-danger">*</em></label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="clientSecret" name="clientSecret"
                                   placeholder=""
                                   value="${formDto.realsecret()}" readonly="readonly">
                        </div>
                    </div>
                    <div class="form-group" style="display: none;">
                        <label for="resourceIds" class="col-sm-2 control-label">resource_ids<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <input type="hidden" id="resourceIds" name="resourceIds" value="sos-resource">

                            <p class="help-block">resourceIds必须选择;
                                可选值必须来源于与<code>OAuth2ServerConfiguration.java</code>中固定值
                            </p>
                        </div>
                    </div>

                    <div class="form-group" style="display: none;">
                        <label for="scope" class="col-sm-2 control-label">scope<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <input type="text" id="scope" name="scope" value="read"/>
                            <p class="help-block">scope必须选择</p>
                        </div>
                    </div>

                    <div class="form-group" style="display: none">
                        <label class="col-sm-2 control-label">grant_type(s)<em class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <label class="checkbox-inline">
                                <input type="checkbox" name="authorizedGrantTypes"
                                       value="authorization_code" ${fun:containsIgnoreCase(formDto.authorizedGrantTypes, 'authorization_code') ?'checked':''}
                                       checked="true"/>
                                authorization_code
                            </label>

                            <label class="checkbox-inline">
                                <input type="checkbox" name="authorizedGrantTypes"
                                       value="refresh_token" ${fun:containsIgnoreCase(formDto.authorizedGrantTypes, 'refresh_token') ?'checked':''} />
                                refresh_token
                            </label>

                            <p class="help-block">至少勾选一项grant_type(s), 且不能只单独勾选<code>refresh_token</code></p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="webServerRedirectUri" class="col-sm-2 control-label">应用跳转地址</label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="webServerRedirectUri"
                                   name="webServerRedirectUri"
                                   placeholder="" value="${formDto.webServerRedirectUri()}">

                        </div>
                    </div>
                    <input type="hidden" name="trusted" value="true"/>
                    <input type="hidden" name="autoApprove" value="true"/>

                    <div class="form-group">
                        <div class="col-sm-2"></div>
                        <div class="col-sm-10">
                            <form:errors path="*" cssClass="text-danger"/>
                        </div>
                    </div>


                    <div class="form-group">
                        <div class="col-sm-2"></div>
                        <div class="col-sm-10">
                            <button type="submit" class="btn btn-success">修改</button>
                            <a href="/server/./" class="btn btn-default">取消</a>
                        </div>
                    </div>
                </form:form>
            </div>
        </div>
    </div>
</div>

<script>
    var RegisterClientCtrl = ["$scope", function ($scope) {
        $scope.visible = false;

        $scope.showMore = function () {
            $scope.visible = !$scope.visible;
        };
    }];
    $(function () {
    });
</script>
</body>
</html>