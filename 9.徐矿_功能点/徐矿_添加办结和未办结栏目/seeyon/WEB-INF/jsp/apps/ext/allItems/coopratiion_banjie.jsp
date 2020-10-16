<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>协同所有办结</title>
    <style>

        html,body{
            margin:0;
            padding:0;
        }

        a{
            text-decoration:none;
            color:#000;
        }

        td {
            font-family: Arial, "Ping Fang SC", "Microsoft YaHei", Helvetica, sans-serif, "SimSun";
            height:30px;
            line-height:30px;
            white-space: nowrap;
            padding-left: 5px;
            font-size:12px;
        }
        table{
            table-layout:fixed;/* 只有定义了表格的布局算法为fixed，下面td的定义才能起作用。 */
        }
        td{
            word-break:keep-all;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
    </style>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <tbody>
    <c:forEach var="finish" items="${list}">
        <tr>
            <td style="font-size:14px" width="40%" onclick="xkjtOpen(this,'${finish.id}')">
                <a href="javascript:void(0);">${finish.subject}</a>
            </td>
            <td width="20%" align="center">
                <span title="<fmt:formatDate value='${finish.start_date}' pattern='yyyy-MM-dd' />"><fmt:formatDate value='${finish.start_date}' pattern='yyyy-MM-dd' /></span>
            </td>
            <td style="color:#505050" width="10%" align="center">
                <a onclick="javascript:window.top.showMemberCard('${finish.start_member_id}','5')" style="width:5px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;" title="${finish.start_name}">${finish.start_name}</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
<script type="text/javascript">
    function xkjtOpen(obj,id){
        $.post("allItems.do?method=getCtpAffairIdBycolSummaryById",{id:id},function (data) {
            var code = data.code;
            var url="collaboration/collaboration.do?method=summary&openFrom=listSent&affairId="+code;
            window.open(url,"_blank");
        });

    }
    $(document).ready(function(){
        $(".paper").mouseover(function(){
            this.css('color','#0066FF')
        })

        $(".paper").mouseout(function(){
            this.css('color','#505050')
        })
    })
</script>
</html>
