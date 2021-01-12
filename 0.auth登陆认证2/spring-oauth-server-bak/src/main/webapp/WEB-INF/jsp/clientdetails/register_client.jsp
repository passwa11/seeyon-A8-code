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
    <title>注册client</title>
    <script src="${contextPath}/static/angular.min.js"></script>

</head>
<body>
<div class="row">
    <div class="col-md-1"></div>
    <div class="col-md-10" style="background-color:rgb(255,255,255);height: 900px;">

        <%--        <a href="./" class="btn btn-info">返回</a>--%>

        <h2>注册应用</h2>
        <hr/>

        <div ng-app style="background-color:rgb(255,255,255);">
            <div ng-controller="RegisterClientCtrl">
                <form:form modelAttribute="formDto" cssClass="form-horizontal">
                    <div class="form-group">
                        <label for="clientId" class="col-sm-2 control-label">应用名称<em class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="clientName" name="clientName" placeholder=""
                                   value="">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="clientId" class="col-sm-2 control-label">应用标识(client_id)<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <form:input path="clientId" cssClass="form-control" id="clientId" placeholder="client_id"
                                        required="required" readonly="true"/>

                        </div>
                    </div>
                    <div class="form-group">
                        <label for="clientSecret" class="col-sm-2 control-label">授权秘钥(client_secret)<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <form:input path="clientSecret" cssClass="form-control" id="clientSecret"
                                        placeholder="client_secret" required="required" readonly="true"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="accessTokenValidity" class="col-sm-2 control-label">token有效时间</label>

                        <div class="col-sm-10">
                            <input type="number" class="form-control" name="accessTokenValidity"
                                   id="accessTokenValidity"
                                   placeholder="access_token_validity"/>

                            <p class="help-block">设定客户端的access_token的有效时间值(单位:秒),可选, 若不设定值则使用默认的有效时间值(60 * 60 * 12,
                                12小时);
                                若设定则必须是大于0的整数值</p>
                        </div>
                    </div>
                    <div class="form-group" style="display: none;">
                        <label for="resourceIds" class="col-sm-2 control-label">resource_ids<em
                                class="text-danger">*</em></label>

                        <div class="col-sm-10">
                            <input type="hidden" id="resourceIds" name="resourceIds" value="sos-resource">

                            <p class="help-block">resourceIds必须选择; 可选值必须来源于与<code>OAuth2ServerConfiguration.java</code>中固定值
                            </p>
                        </div>
                    </div>

                    <div class="form-group" style="display: none;">
                        <label for="scope" class="col-sm-2 control-label">scope<em class="text-danger">*</em></label>

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
                            <form:input path="webServerRedirectUri" id="webServerRedirectUri" placeholder="redirect_uri"
                                        cssClass="form-control"/>
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
                            <button type="submit" class="btn btn-success">注册</button>
                            <a href="client_details" class="btn btn-default">取消</a>
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