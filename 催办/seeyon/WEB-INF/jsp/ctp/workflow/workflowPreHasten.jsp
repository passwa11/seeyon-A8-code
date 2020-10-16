<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ include file="/WEB-INF/jsp/ctp/workflow/workflow_header.jsp"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<html class="h100b">
<head>
    <title>${ctp:i18n("workflow.hasten.label")}</title>
    <%@ include file="/WEB-INF/jsp/ctp/workflow/workflow_meta.jsp"%>
    <style type="text/css">
        .stadic_bottom_height{
            height:140px;
            bottom:0;
        }
        .stadic_body_top_bottom{
            bottom:140px;
            top:0px;
        }
        .tt_table { border-right:solid 1px #DDDDDD;}
        .tt_table td,.tt_table th { padding:5px; font-size: 12px; border-right:solid 1px #DDDDDD; border-bottom:solid 1px #DDDDDD;}
    </style>
</head>
<body class="h100b over_hidden">
        <div class="  " style="overflow:hidden;_border_bottom:1px solid #e3e3e3;height:280px;">
            <div class="font_size12 margin_5">${ctp:i18n("workflow.hasten.selectPeople.please.label") }</div>
            <div class="border_all margin_5" style="height:250px;overflow-y:auto;overflow-x:hidden">
            	<table class="tt_table" border="0" cellspacing="0"  cellpadding="0" width="100%">
                <thead>
                    <tr style="border-right:0;text-align: left;background: #e3e3e3;">
                        <th width="20"><input id="selectAllCheckbox" type="checkbox" checked="checked"/></th>
                        <th style="text-align: left;">${ctp:i18n("common.name.label") }</th>
                    </tr>
                </thead>
                <tbody id="peoplesTbody">
                    <c:choose>
                        <c:when test="${userList==null || fn:length(userList)<=0}">
                            <tr class="erow">
                                <td></td>
                                <td style="border-right:0;">${ctp:i18n("workflow.deletePeople.noChildren") }</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${userList}" var="d">
                            <tr class="sort">
                                <td width="1%"><input type="checkbox" checked="checked" name="member" value="${d.id}"</td>
                                <td style="border-right:0;">${ctp:toHTML(d.name)}hhhhhhhhhhhh</td>
                            </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            	</table>
            </div>
            
        </div>
        <div id="contentDiv" class="" style="height:140px;">
        	<c:if test="${canSendPhone}">
        	<div class="common_checkbox_box clearfix margin_5">
				<label for="sendPhoneMessage" class="margin_t_5 hand display_block">
				<input type="checkbox" value="0" id="sendPhoneMessage" name="option" class="radio_com">${ctp:i18n('workflow.hasten.sendsms.label')}</label>
            </div>
            </c:if>
            <div class="font_size12 margin_5">${ctp:i18n('workflow.hasten.postscript.label')}:</div>
            <div class="border_all margin_5" style="height:100px;">
            	<textarea id="contentTextarea" style="height:99px;overflow:auto;width:98.6%;border:0;" class="font_size12 validate" validate="name:'${ctp:i18n("workflow.hasten.postscript.label")}',type:'string',notNull:true,maxLength:85"></textarea>
            </div>
            
        </div>
<script type="text/javascript">
    $("#selectAllCheckbox").click(function(){
    	if($(this).prop("checked")){
    	   $("#peoplesTbody input[name=member]").each(function(i){
              $(this).attr("checked",true);
           });
    	} else {
    	  $("#peoplesTbody input[name=member]").each(function(i){
            $(this).attr("checked",false);
          });
    	}
    });
    function OK(){
        debugger;
    	var result = {};
    	var checkedSize= 0;
    	$("#peoplesTbody input[name=member]").each(function(i){
    	  var checked= $(this).attr("checked");
          if(checked){
            checkedSize++;
          }
    	});
    	if(checkedSize<=0){
    		result.success = false;
    		$.alert('${ctp:i18n("workflow.hasten.least_select_singleton") }');
    		return $.toJSON(result);
    	}
    	var value = $("#contentTextarea").val() || "";
    	value = $.trim(value);
    	//if(!MxtCheckForm($("#contentDiv"),{})){
        /*if(value.search(/[-~!@#$%^&*()_+='"]/g)>-1){
        	$.alert("附言不能含有以下特殊字符[-~!@#$%^&*()_+='\"]！");
        	$("#contentTextarea").val(value);
            result.success = false;
            checkForm = false;
            return $.toJSON(result);
        }*/
    	if(value.length>85){
    		$.alert("${ctp:i18n('workflow.hasten.content.validateLabel')} "+value.length+"！");
    		$("#contentTextarea").val(value);
    		result.success = false;
            checkForm = false;
            return $.toJSON(result);
        }
        result.success = true;
        result.memberIdArray = [];
        $("#peoplesTbody input[name=member]").each(function(i){
          var checked= $(this).attr("checked");
          if(checked){
            result.memberIdArray.push($(this).val());
          }
        });
        result.content = value;
        var temp = $("#sendPhoneMessage");
        if(temp.size()>0){
        	result.sendPhoneMessage = temp.prop("checked");
        }else{
        	result.sendPhoneMessage = false;
        }
        temp = null;
        return $.toJSON(result);
    }
</script>
</body>
</html>