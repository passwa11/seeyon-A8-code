<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>${ctp:i18n("common.my.template")}</title>
	<script type="text/javascript">
		var page = [];
		var category = "${ctp:escapeJavascript(category)}";
		var recent = "${ctp:escapeJavascript(recent)}";
		page.showTemplates = ${showTemplates};
		page.showCategorys = ${showCategorys};
		var loginAccountId = "${ctp:escapeJavascript(CurrentUser.loginAccount)}";
		var showRecentTemplate = "${ctp:escapeJavascript(showRecentTemplate)}";
		var selectId="${selectId}";
	</script>
	<style type="text/css">
		a {
		    font-size: 12px;
		    cursor: pointer;
		    color: #000;
		    text-decoration: none;
		}
		.companyCheck{
			width:199px;
			height:34px;
			border:0;
			border-radius:3px;
			left:-6px;
			padding-left:9px;
			line-height: 30px;
			color: #999;
			font-size: 12px;
			position: relative;
			top:-4px;
		}
		.changeBtn{
		border:1px solid #dae3ea;
		height: 26px;
		line-height: 26px;
		list-style: none;
		width:69px;
		border-radius: 5px;
		display: inline-block;
		margin-left: 26px;
		z-index: 4;
		position: relative;
		background: #fff;
	}
	.treeBtn{
		width:34px;
		line-height: 26px;
		height:26px;
		position: relative;
		cursor: pointer;
		border-right: 1px solid #dae3ea;
		border-radius: 4px 0 0 5px;
	}
	.leftBtn {
		width:34px;
		line-height: 26px;
		height:26px;
		position: relative;
		cursor: pointer;
		border-radius: 0 4px   4px 0;
	}
	.selectBtn {
		background: #8d929b;
	}
	.emBtn {
		position:relative;
		bottom:2px;
		left:8px;
	}
	.layout_north{
		background-color: #ececec;
	}
	#searchValue{
		width:298px;height:28px; border:1px solid #E5E5E5;
		border-right: transparent;
	    vertical-align: top;
	}
	#searchValue:hover{
		border:1px solid #2E8DE9;
	}
	.search span{
		/*display: inline-block;*/
		/*width:90px;*/
		border-radius: 0;
		height:28px;line-height:28px;
		cursor: pointer;
	}
	.go-button{
		height:27px;
		background-color:#fff;
		color:#6EA8F0;
		font-size:12px;
		line-height:27px;
		border-radius: 100px;
		width:94px;
		text-align:center;
		border:1px solid #dfdfdf;
	}
	.go-button a{
		color:#009DEF;
	}
	.go-button:hover{
		background-color:#6EA8F0;
		color:#fff;
	}
	.go-button:hover a{
		background-color:#6EA8F0;
		color:#fff;
	}
	.go-button:hover a:hover{
		color:#fff;
	}
	.search-bar{
		background-color:#fff;
		line-height:27px;
		height:27px;
		border:1px solid #dfdfdf;
		border-radius: 100px;
		width:270px;

	}
	.search-input #searchValue{
		margin-left: 15px;
		border:0;
		width:200px;
		height: 24px;
	}
	.search-icon{
		margin-right: 6px;
		margin-top: -3px;
	}
	.spare-line{
		border: 1px solid #EDEDED;
		border-width: 0 1px 0 0;
		    height: 26px;
    margin-left: 15px;
	}
	.breadTitle{
	   color:#333;font-size:14px;position:relative;top:2px;
	}
	td.text-indent-1em.sorts{
		padding-left: 20px;
	}
	</style>

</head>
<body style="margin-top:0px">
	 <div id='layout' class="comp f0f0f0" comp="type:'layout'">
        <div class="layout_north" layout="height:70,sprit:false,border:false">

		<div class="breadcrumb" id="breadcrumb" style="display: block;padding-top:3px;box-sizing: border-box;"><span class="nowLocation_ico"><img
			src="/seeyon/main/skin/frame/default/menuIcon/moresectionicon.png"></span><span
	class="nowLocation_content"><a class="breadTitle" style="color:#333;">${ctp:i18n("template.templatePub.myTemplate")}</a></span></div>

		    <form id="searchForm" method="post" action="${path}/template/template.do?method=moreTemplateList">
		    	<input type="hidden" name="CSRFTOKEN" value="${sessionScope['CSRFTOKEN']}" />
			    <div style="width: 100%;height: 100%" class="overflow clearfix"  >
					<div class="left padding_5">
						<select class="companyCheck" id="selectAccountId" onchange="onSelectAccount()">
							<option value="1">${ctp:i18n("template.moreTemplate.allAccount")}</option>
							<c:forEach items="${accounts }" var="accounts" >
								<option value="${accounts.key}"
										<c:if test='${orgAccountId == accounts.key && isShowTemplates eq "false"}'>selected="selected"</c:if> >
										${accounts.value}
									 </option>
							</c:forEach>
						</select>
					</div>
					<div class=" left" style="height:40px;">
						<div class="search-bar left clearfix">
							<div class='search-input left'>
								<input id="searchValue" name="searchValue" value="${searchValue}" />
							</div>
							<div class='search-icon right'>
							 <em class="ico16 search_16" onclick="javascript:search();return false;"></em>
						</div>
						</div>

						<div class="left spare-line"></div>
					<!-- 	<span class="search left">
							<input id="searchValue" name="searchValue" value="${searchValue}" /><span class="common_button common_button_emphasize" onclick="javascript:search();return false;">
							${ctp:i18n('template.templateJs.search') }
	</span>
						</span> -->

						<div class="left go-button" style="margin-left:15px;">
							<a class="button" href="${path }/collTemplate/collTemplate.do?method=showTemplateConfig${ctp:csrfSuffix()}">${ctp:i18n('template.templatePub.configurationTemplates')}</a><!-- 配置模板 -->
						</div>
					</div>
					<div class="right margin_r_5" style="margin-top:3px;">

						<ul class="changeBtn right margin_r_5">
							<li class="left treeBtn selectBtn">
								<em title="${ctp:i18n('template.moreTemplate.tree')}" class="ico16 emBtn viewStyle_tree_checked"></em>
							</li>
							<li class="left leftBtn">
								<em onclick="moreTemplate()" title="${ctp:i18n('template.moreTemplate.flat')}" class="ico16 emBtn switchView_table_16"></em>
							</li>
						</ul>
					</div>
				</div>
			</form>
        </div>
        <div class="layout_west" id="west" layout="border:false">
            <table width="99%" height="100%" style="background: #fff;" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="padding_10" valign="top">
<%--						zhou:更多模板树--%>
                        <div id="tree" class="ztree"></div>
                    </td>
                </tr>
            </table>
        </div>
        <div class="layout_center " id="center" style="overflow:auto;background: #fff;" layout="border:false">
            <div width="100%" height="100%">
            	<table id="templateDatasTab" border="0" cellpadding="0" cellspacing="0"  align="center" class="font_size12 w100b padding_10" style="table-layout:fixed">
			     </table>
            </div>
        </div>
      </div>
</body>
<script type="text/javascript" charset="UTF-8" src="${path}/apps_res/template/js/moreTreeTemplate.js${ctp:resSuffix()}"></script>
</html>
