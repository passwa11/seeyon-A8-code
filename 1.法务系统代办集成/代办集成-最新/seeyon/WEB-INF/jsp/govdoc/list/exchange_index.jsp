<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../govdoc_header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html class="h100b over_hidden">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>公文交换</title>
</head>
<script type="text/javascript">
var listType = "${ctp:escapeJavascript(param.listType)}";

$(document).ready(function() {
	$("#tabs").find("li").each(function() {
		var _this = $(this);
		_this.bind("click", function() {
			reloadFrame(this);
		});
	});

	loadFirstTabEvent();
});

function loadFirstTabEvent() {
	if(listType && listType != "") {
		var tabId = (listType + "Tab");
		$("#tabs").find("#"+ tabId).trigger("click");
	} else {
		$("#tabs").find("li").eq(0).trigger("click");
	}
}

function reloadFrame(obj) {
	$("#tabs").find("li").each(function() {
		 var _this = $(this);
		 _this.removeClass("current");
	});

	var _this = $(obj);
	_this.addClass("current");

 	var _url = _ctxPath + _this.find("a").attr("url");
    $("#main").attr("src", _url);
};

</script>
<body class="h100b over_hidden">

<div>
	<div class="comp" comp="type:'breadcrumb',comptype:'location',code:'${ctp:escapeJavascript(param._resourceCode) }'"></div>
</div>

<div id='layout' class="comp page_color" comp="type:'layout'">
	<div class="layout_north" layout="height:32,sprit:false,border:false" id="north">
		<div id="tabs" class="comp page_color" comp="type:'tab'">
        	<div id="tabs_head" class="common_tabs clearfix margin_t_5">
				<ul class="left">
					<c:if test="${isSender }">
<%--						zhou:2019-09-06--%>
	                    <li id="listExchangeSendPendingTab" class="current"><a hideFocus="true" href="javascript:void(0)" tgt="main" class="no_b_border" url="/govdoc/list.do?method=listExchange&listType=listExchangeSendDone"><span>分送</span></a></li>
<%--	                    <li id="listExchangeSendPendingTab" class="current"><a hideFocus="true" href="javascript:void(0)" tgt="main" class="no_b_border" url="/govdoc/list.do?method=listExchange&listType=listExchangeSendPending"><span>分送</span></a></li>--%>
					</c:if>
					<c:if test="${isSigner }">
						<li id="listExchangeSignPendingTab"><a hideFocus="true" href="javascript:void(0)" tgt="main" class="no_b_border" url="/govdoc/list.do?method=listExchange&listType=listExchangeSignPending"><span>签收</span></a></li>
					</c:if>
					<c:if test="${isBacker }">
						<li id="listExchangeFallbackTab"><a hideFocus="true" href="javascript:void(0)" tgt="main" class="no_b_border" url="/govdoc/list.do?method=listExchange&listType=listExchangeFallback"><span>回退</span></a></li>
					</c:if>
                </ul>
            </div>
        </div>
    </div>

    <div id="tabs_body" class="layout_center page_color over_hidden" layout="border:false" style="overflow:hidden; margin-bottom:0px;padding-bottom:0px;">
    	<iframe id="main" name="main" border="0" frameBorder="no" width="100%" height="100%"></iframe>
	</div>

</div>

</body>
</html>
