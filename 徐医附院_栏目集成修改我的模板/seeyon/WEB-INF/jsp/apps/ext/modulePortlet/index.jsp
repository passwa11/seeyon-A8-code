<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${ctp:i18n("common.my.template")}</title>

</head>
<body>
<c:forEach var="cate" items="${showCategorys}" varStatus="v">
    <c:if test="${cate.id!=-1}">
        <c:if test="${cate.pId!=null}">
            <c:set var="z" scope="session" value="${count+1}"/>
            <font >
                <label onclick="test('${cate.id}')" style="cursor: pointer;font-size: 14px;">
                    <span class="ico16 folder_16"></span>&nbsp;&nbsp;${cate.name}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                </label>
            </font>
            <c:if test="${z%5==0}">
                <br>
            </c:if>
            <c:set var="count" scope="session" value="${z}"/>
        </c:if>
    </c:if>
</c:forEach>
</body>
<script>
    function test(id){
        openCtpWindow({
            'url': '/seeyon/template/template.do?method=moreTreeTemplate&id='+id
        });
        // showMenu('/template/template.do?method=moreTreeTemplate&amp;type=more','sectionPortal', 'mainFrame', '', this);
    }
</script>
</HTML>
