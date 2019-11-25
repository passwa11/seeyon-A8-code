<%@ page import="com.seeyon.ctp.common.authenticate.domain.User" %>
<%@ page import="com.seeyon.apps.ext.fileUploadConfig.manager.fileUploadConfigManager" %>
<%@ page import="com.seeyon.apps.ext.fileUploadConfig.manager.fileUploadConfigManagerImpl" %>
<%@ page import="com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUploadMember" %>
<%@ page import="com.seeyon.ctp.common.AppContext" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div id='newGovDoc_layout' class="comp" comp="type:'layout'">

<div class="layout_north f0f0f0" layout="height:41,border:false,sprit:false">
    <div class="padding_l_5 border_b">
	    <div id="toolbar"></div><!-- 经典布局-拟文界面-操作按钮区域 -->
    </div>
</div>
<%
	User user = AppContext.getCurrentUser();
	Long userid=user.getId();
	fileUploadConfigManager manager = new fileUploadConfigManagerImpl();
	ZOrgUploadMember zm=manager.selectUploadMemberByuserId(Long.toString(userid));
	if(null != zm){
		request.setAttribute("zflag",true);
	}
%>
<div class="layout_center" id="centerBar" style="overflow-y:hidden;background:#fff;" layout="">

	<div id="north_area_h"><!-- 经典布局-拟文界面-页面数据填写区域 -->

		<div id="colMainData" class="form_area padding_t_10">

	    	<%@ include file="newgovdoc_form.jsp"%><!-- 一屏式布局A/B-拟文界面-隐藏控件用于提交数据到后端 -->

		    <c:if test="${vobj.customDealWith ne 'true' || vobj.isQuickSend eq 'true'}"><!-- 续办或快速发文 -->
				<%@ include file="/WEB-INF/jsp/govdoc/new/newGovdocLayout.jsp"%>
			</c:if>
		   	<c:if test="${vobj.customDealWith eq 'true' && vobj.isQuickSend ne 'true' }"><!-- 续办或拟文界面 -->
		   		<%@ include file="/WEB-INF/jsp/govdoc/new/newCustomDealWithGovdoc.jsp"%>
		    </c:if>

	        <div style="height:33px;overflow:hidden;">
	        	<span style="display:inline-block; margin-left:88px; margin-top:10px;margin-bottom:5px;">
	        		<!-- 上传附件小图标 -->
<%--					zhou 2019-11-21--%>
					<c:if test="${zflag}">
						<c:if test="${vobj.switchVo.canUploadAttachment eq 'true'}">
							<span class="font_size12 color_666 margin_r_20 hand" onclick="insertAttachmentPoi('Att')">
								<span class="ico16 affix_16 margin_b_5"></span>
									${ctp:i18n('permission.operation.UploadAttachment')}
							</span>
						</c:if>
					</c:if>
		        	<c:if test="${vobj.isQuickSend ne 'true' && vobj.switchVo.canUploadRel eq 'true'}">
		        	<!-- 关联文档小图标 -->
		            <span class="font_size12 color_666 hand" onclick="quoteDocument('Doc1')">
		            	<span class="ico16 associated_document_16 margin_b_5"></span>
		            		${ctp:i18n('permission.operation.UploadRelDoc')}
		            </span>
		        	</c:if>
	            </span>
	            <!-- 更多 -->
            	<span class="padding_r_25 right"  style="display:inline-block;text-align:right; margin-top:10px;margin-bottom:5px;">
	            	<a id="show_more" class="clearfix" style="width:100px;"><span class="ico16 arrow_2_b"></span>${ctp:i18n('collaboration.newcoll.show')}</a>
	            </span>
			</div>

            <div class="attachmentArea">
            	<!-- 附件显示区域 -->
	            <div id="attachmentTRAtt" style="display:none;">
					<table border="0" cellspacing="0" cellpadding="0" width="100%" class="line_height180">
						<tr id="attList">
                        	<td class="align_right" valign="top" style="width: 104px">
                            	<div class="div-float margin_t_5">
                            	<em class="ico16 affix_16"></em>
                            	</div>
                            </td>
                            <td valign="top" width="30" nowrap="nowrap"><div class="div-float margin_t_5 margin_r_5">(<span id="attachmentNumberDivAtt"></span>) </div></td>
                            <td class="align_left">
                                <div id="attFileDomain"  class="comp" comp="type:'fileupload',attachmentTrId:'Att',applicationCategory:'1',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${vobj.cloneOriginalAtts},callMethod:'insertAtt_AttCallback',delCallMethod:'insertAtt_AttCallback',takeOver:false,noMaxheight:true" attsdata='${vobj.attListJSON }'></div>
                            </td>
						</tr>
					</table>
				</div><!-- end attachmentTRAtt -->
				<!-- 关联文档显示区域 -->
                <div id="attachment2TRDoc1" style="display:none; margin-top:4px;">
                    <table border="0" cellspacing="0" cellpadding="0" width="100%" class="line_height180">
                        <tr id="docList">
                            <td class="align_right" valign="top" style="width: 104px"><div class="margin_t_5"><span class="ico16 associated_document_16"></span></div> </td>
                            <td valign="top" width="30" nowrap="nowrap"><div class="div-float margin_t_5 margin_r_5">(<span id="attachment2NumberDivDoc1"></span>) </div></td>
                            <td class="align_left">
                                <div class="comp" id="assDocDomain" comp="type:'assdoc',attachmentTrId:'Doc1',modids:'1,3',applicationCategory:'1',referenceId:'${vobj.summary.id}',canDeleteOriginalAtts:true,originalAttsNeedClone:${vobj.cloneOriginalAtts},callMethod:'insertAtt_AttCallback',delCallMethod:'insertAtt_AttCallback',noMaxheight:true" attsdata='${vobj.attListJSON }'></div>
                            </td>
                        </tr>
                    </table>
               </div><!-- end attachment2TRDoc1 -->
			</div><!-- attachmentArea -->

			<!-- 原收文流程显示区域 -->
           	<c:if test="${vobj.govdocRelation1=='true' }">
			<table border="0" cellspacing="0" cellpadding="0" width="100%" style="margin-top: 10px;">
				<tr>
					<td valign="top" style="width: 100px" class="align_right">
                             </td>
					<td style="padding-left:5px;text-align:left" colspan="2">
						<span class="hand" onclick="showDetail('${vobj.forwardSummaryId}');"><span class="ico16 view_log_16 margin_lr_5" ></span>${ctp:i18n('govdoc.sendGrid.oldRecEdoc')}</span>
					</td>
				</tr>
			</table>
			</c:if>

       	</div><!-- end colMainData -->
	</div><!-- end north_area_h -->

    <!-- 经典布局-拟文界面-页面数据公文单展示区域 -->
	<div class="" id="centerBar" style="overflow-y:auto; background:#d8d9db;">
		<iframe id='govDocZwIframe' name='govDocZwIframe' style="border: 0; width: 100%; height: 500px; display: block;" frameborder="0" marginheight="0" marginwidth="0" src="" onload="_contentSetText()"></iframe>
	</div>

	<!-- 经典布局-拟文界面-附言显示区域-->
	<div class="over_hidden" id="comment_deal" style="z-index:2; height: 41px; background:#d8d9db; width:100%; text-align:center;position:absolute;bottom:0;">
		<input type="hidden" id="moduleType" value="${vobj.contentContext.moduleType}">
		<input type="hidden" id="moduleId" value="${ !empty vobj.contentContext.moduleId ? vobj.contentContext.moduleId : vobj.summaryId }">
		<input type="hidden" id="ctype" value="-1">
		<table style="border-top:1px solid #ccc;background:#f7f7f7;position: relative;width:630px;left: 50%;margin-left: -393px;" class="h100b" cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td id="adtional" valign="middle" align="left" height="35" class="padding_t_5 font_size12">
                       <em id="adtional_ico" class="ico16 arrow_2_b msg_expansionIco margin_ico margin_b_5"></em>
                       <span class="adtional_text margin_t_5 font_size14 color_666">${ctp:i18n('collaboration.newcoll.dangfuyan')}</span>
				</td>
			</tr>
			<tr id="fuyan_area" class="hidden">
				<td id="fuyan" valign="top" align="center"class="editadt_box padding_l_5"><textarea
					style="width: 766px; padding: 0 5px; font-size: 12px; min-height: 115px;"
					class="h100b" id="content_coll" name="content_coll"
					onclick="checkContent();" onblur="checkContentOut();"><c:forEach items="${commentSenderList}" var="csl"><c:out value="${csl.content}"></c:out><c:out value="${__huanhang }" escapeXml="false"></c:out></c:forEach></textarea></td>
			</tr>
		</table>
	</div><!-- end comment_deal -->

</div><!-- end layout_center -->
</div><!-- end newGovDoc_layout -->

<!-- 经典布局-拟文界面-正文展示区域(无所谓放哪)-->
<%@ include file="/WEB-INF/jsp/govdoc/govdocBody.jsp"%>
