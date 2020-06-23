<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 经典布局-查看界面-右侧处理区域 -->
<div class="layout_east" id="east" style="background:#fff;">
	<%-- 这里ID有样式~~~~~ --%>
    <img id="hidden_side" class="right_hide_selector" onclick="hiddenSideFunc()"  src="/seeyon/common/images/shou.jpg">
	<div id="deal_area_show" onclick="hiddenSideFunc()"  class="font_size12 align_center h100b hidden hand">
		<table width="100%" height="100%">
			<tr>
				<td align="center" id='_opinionArea' valign="middle"><span class="ico16 arrow_2_l"></span><br />${ctp:i18n('collaboration.summary.handleOpinion')}</td>
			</tr>
		</table>
	</div>
	<%@ include file="govdoc_deal_old.jsp" %>
</div><!-- end layout_east -->

<!-- 经典布局-查看界面-左侧展示区域 -->
<div class="layout_center over_hidden h100b" id="center" style="display: none;">
<div class="h100b stadic_layout">

<!-- 经典布局-查看界面-左侧-顶部区域 -->
<div class="stadic_head_height" id="summaryHead">

<!-- 经典布局-查看界面-左侧-顶部区域-标题+附件 -->
<div id="colSummaryData" class="newinfo_area title_view">
    <%@include  file="summary_form.jsp"%><!-- 经典布局-查看界面-隐藏控件用于提交数据到后端 -->

	<ul border="0" cellspacing="0" cellpadding="0" width="100%" style="table-layout: fixed;">
		<li>
			<div class="text_overflow title" nowrap="nowrap">
				<span class="summary_view_tit padding_l_25 font_size18 font_family_yahei color_black2">
					<c:if test="${summaryVO.summary.importantLevel ne null && summaryVO.summary.importantLevel ne '1'}">
						<span class='ico16 important${summaryVO.summary.importantLevel}_16 '></span>
					</c:if><strong title='${(summaryVO.affair.subject)}'>${ctp:toHTML(summaryVO.affair.subject)}</strong>
				</span>
			</div>
		</li>
		<li>
			<div>
				<span class="padding_l_25">
					<a href="#" class="color_gray2" id="panleStart">${ctp:toHTML(summaryVO.startMemberName)}</a>
					<span class="color_gray2">${ctp:formatDateByPattern(summaryVO.summary.createTime, 'yyyy-MM-dd HH:mm')}</span>
				</span>
			</div>
		</li>
	</ul>
</div><!-- end colSummaryData -->

<!-- 经典布局-查看界面-左侧区域-上部区域-->
<div class="common_tabs common_tabs_big clearfix margin_t_5" style="border-bottom:solid 2px #97D1F0;">

	<!-- 经典布局-查看界面-左侧-上部区域-页签切换 -->
	<ul class="left margin_l_25">
	    <!-- 文单 -->
		<c:if test="${!onlySeeContent}">
			<li id="form_view_li" class="current"><a style="width: 70px" onClick="changeContentView('form_view_li')">${ctp:i18n('edoc.doctemplate.wendan')}</a></li>
			<!-- 全文签批单 -->
			<c:if test="${not empty summaryVO.aipFileId}">
				<li id="qwqp_view_li"><a style="width: 80px" onClick="changeContentView('qwqp_view_li')">${ctp:i18n('edoc.aip.label')}</a></li>
			</c:if>
		</c:if>
		<c:if test="${showContentByGovdocNodePropertyConfig}">
			<!-- 正文 -->
			<li id="body_view_li"><a style="width: 70px" onClick="changeContentView('body_view_li')">${ctp:i18n('govdoc.summary.text')}</a></li>
			<!-- 转PDF后显示PDF正文 -->
			<c:if test="${ not empty summaryVO.bodyVo.pdfFileId }">
				<li id="transpdf_view_li"><a style="width: 60px" onClick="changeContentView('transpdf_view_li')">${ctp:i18n('govdoc.pdf.content.text')}</a></li>
			</c:if>
			<!-- 转OFD后显示PDF正文 -->
			<c:if test="${ not empty summaryVO.bodyVo.ofdFileId }">
				<li id="transofd_view_li"><a onClick="changeContentView('transofd_view_li')" style="width:56px;">${ctp:i18n('govdoc.ofd.content.text')}</a></li>
	  		</c:if>
		</c:if>

		<c:if test="${!onlySeeContent and !(summaryVO.summary.isQuickSend)}">
			<!-- 流程 -->
			<li id="workflow_view_li"><a style="width: 80px" onclick="changeContentView('workflow_view_li')">${ctp:i18n('collaboration.workflow.label')}</a></li>
			<!-- 相关查询 :当有处理区或者已办列表中才显示。-->
			<%-- <c:if test="${ not empty summaryVO.affair.formRelativeQueryIds && (hasDealArea or param.openFrom eq 'listDone')}">
				<li id="query_view_li"><a
					onclick="showFormRelativeView('query','${summaryVO.affair.formRelativeQueryIds}','${summaryVO.summary.formAppid}')">${ctp:i18n('collaboration.summary.formquery.label')}</a></li>
			</c:if>
			<c:if
				test="${not empty summaryVO.affair.formRelativeStaticIds  && (hasDealArea or param.openFrom eq 'listDone')}">
				<li id="statics_view_li"><a
					onclick="showFormRelativeView('report','${summaryVO.affair.formRelativeStaticIds}','${summaryVO.summary.formAppid}')">${ctp:i18n('collaboration.summary.formstatic.label')}</a></li>
			</c:if> --%>
		</c:if>
	</ul>

	<!-- 经典布局-查看界面-左侧-上部区域-命令按钮 -->
	<div class="orderBt orderBt1 right align_center">
		<div style="position: absolute; right: 50px; top: 125px; width: 220px; z-index: 200; background-color: #ececec; display: none; overflow: auto; text-align: left; border: 1px #dadada solid; padding: 5px;" id="processAdvanceDIV">
			<input type="text" id="searchText" onkeydown="enterKeySearch(event)" name="searchText"
				onfocus="checkDefSubject(this, true)" onblur="checkDefSubject(this, false)"
				deaultvalue="&lt;${ctp:i18n('collaboration.summary.label.search.js')}&gt;"
				value="&lt;${ctp:i18n('collaboration.summary.label.search.js')}&gt;" />
			<span class="ico16 arrow_1_b" onclick="javascript:doSearch('forward')" class="cursor-hand"></span>
			<span class="ico16 arrow_1_t" onclick="javascript:doSearch('back')" class="cursor-hand"></span>
			<span class="ico16 close_16" onclick="javascript:advanceViews(false)" class="cursor-hand"></span>
		</div>

		<!-- 流程最大化，意见查找，附件列表，收藏，新建会议，即时交流，明细日志，属性状态，打印，督办 -->
		<c:set value="2" var="countBtn" />
		<script type="text/javascript">
		</script>
		<%--zhou:2020-06-15 start--%>
		<span class="hand left set_color_gray" id="downloadDetail" onclick="downloadDetail()">
			<span class="ico16 download_16 margin_lr_5" title="文单下载"></span>
			文单下载
		</span>
		<script>
			function downloadDetail(){
				var summaryId='${summaryVO.summary.id}';
				var affair='${summaryVO.affairId}';
				var url = "/seeyon/ext/downloadDetail.do?method=downLoadHtmltoPdf&affairId=" + affair+"&summaryId="+summaryId+"&subject="+encodeURI(${summaryVO.affair.subject});
				$("#downloadFileFrame").attr("src", url);
			}

		</script>
		<%--zhou:2020-06-15 end--%>

		<c:if test="${(summaryVO.bodyVo.bodyType eq 'OfficeWord' || summaryVO.bodyVo.bodyType eq 'OfficeExcel'
		|| summaryVO.bodyVo.bodyType eq 'WpsWord' || summaryVO.bodyVo.bodyType eq 'WpsExcel') && showContentByGovdocNodePropertyConfig && v3x:isOfficeTran()}">
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5 }">
				<span class="hand left set_color_gray viewOriginalContentA" onclick="showEditorDialog()"><span class="margin_lr_5 ico16 text_16" title="${ctp:i18n('collaboration.content.viewOriginalContent')}"></span>${ctp:i18n("collaboration.content.viewOriginalContent")}</span>
			</c:if>
		</c:if>
		<!-- 打印 -->
		<c:if test="${summaryVO.switchVo.canPrint && !isFromTraceFlag && !onlySeeContent}">
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5 }">
				<span class="hand left set_color_gray" id="print">
					<span class="ico16 print_16 margin_lr_5" title="${ctp:i18n('collaboration.newcoll.print')}"></span>${ctp:i18n('collaboration.newcoll.print')}
				</span>
			</c:if>
		</c:if>
		<!-- 正文打印-->
		<c:if test="${summaryVO.switchVo.officecanPrint && !isFromTraceFlag && !onlySeeContent && !((summaryVO.bodyVo.bodyType eq 'OfficeWord' || summaryVO.bodyVo.bodyType eq 'OfficeExcel'
		|| summaryVO.bodyVo.bodyType eq 'WpsWord' || summaryVO.bodyVo.bodyType eq 'WpsExcel' ||summaryVO.bodyVo.bodyType eq 'Pdf') && v3x:isOfficeTran())}">
				<c:set value="${countBtn+1}" var="countBtn" />
				<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="zwPrint">
					<span class="ico16 print_16 margin_lr_5" title="${ctp:i18n('edoc.metadata_item.ContentPrint')}"></span>${ctp:i18n('edoc.metadata_item.ContentPrint')}
				</span>
				</c:if>
		</c:if>
       <%-- 意见查找 --%>
       <c:set value="${countBtn+1}" var="countBtn" />
       <c:if test="${countBtn<=5}">
        	<span class="hand left set_color_gray" id='msgSearch' onclick="javascript:advanceViews(null,this)" title="${ctp:i18n('collaboration.summary.advanceViews')}">
            <span class="syIcon sy-search margin_lr_5"></span></span>
        </c:if>
		<!-- 附件列表 -->
		<c:if test='${!isFromTraceFlag}'>
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="attachmentListFlag1" onclick="javascript:showOrCloseAttachmentList(true)">
					<span class="ico16 affix_16 margin_lr_5" title="${ctp:i18n('collaboration.summary.findAttachmentList')}"></span>${ctp:i18n('collaboration.common.flag.attachmentList')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="attachmentListFlag" type="hidden" /></c:if>
		</c:if>

		<!-- 联合发文 --><c:if test="${_jointlyIssued eq '1'}"><span class="hand left set_color_gray" id="jointlyIssued"><span class="ico16 print_16 margin_lr_5" title="${ctp:i18n('processLog.action.name.163')}"></span>${ctp:i18n('processLog.action.name.163')}</span></c:if>
		<c:if test="${govdocExchangeMainId != null }"><span class="hand left set_color_gray" id="exchangeRecInfo" onclick="exchangeRecInfo('${govdocExchangeMainId}')"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.exchange.recInfo.label')}"></span>${ctp:i18n('govdoc.exchange.recInfo.label')}</span></c:if>
		<c:if test="${govdocExchangeSignSummaryId != null }"><span class="hand left set_color_gray" id="exchangeSignProcess" onclick="showSummary('${govdocExchangeSignSummaryId}')"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.exchange.signProcess.label')}"></span>${ctp:i18n('govdoc.exchange.signProcess.label')}</span></c:if>
		<!-- 加上haveTurnRecEdoc2判断 有转办信息就不需要显示来文信息 -->
		<c:if test="${govdocExchangeSendSummaryId != null && haveTurnRecEdoc2 == null}"><span class="hand left set_color_gray" id="exchangeSendInfo" onclick="showSummary('${govdocExchangeSendSummaryId}')"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.exchange.sendInfo.label')}"></span>${ctp:i18n('govdoc.exchange.sendInfo.label')}</span></c:if>
		<c:if test="${govdocExchangeRecSummaryId != null }"><span class="hand left set_color_gray" id="exchangeRecInfo" onclick="showSummary('${govdocExchangeRecSummaryId}')"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.exchange.recInfo.label')}"></span>${ctp:i18n('govdoc.exchange.recInfo.label')}</span></c:if>
		<!-- 原收文流程 --><c:if test='${haveTurnSendEdoc1 != null}'><span class="hand left set_color_gray" id="haveTurnSendEdoc1" onclick="showDetail('${haveTurnSendEdoc1}');"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.oldRecEdoc')}</span></c:if>
		<!-- 转发文流程 --><c:if test='${haveTurnSendEdoc2 != null}'><span class="hand left set_color_gray" id="haveTurnSendEdoc1" onclick="showTurnSendEdocInfo()"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.turnSendEdoc')}</span></c:if>

		<%--收藏 --%>
		<%--判断是否有处理后归档权限或者发送协同时勾选了‘归档’，如果没有，则不能收藏 ,a6没有收藏功能,knowledge从文档中心打开不显示收藏--%>
		<c:if test="${canFavorite && !isFromTraceFlag && !onlySeeContent}">
			<c:if test="${ctp:escapeJavascript(param.openFrom) ne 'favorite' and param.openFrom ne 'listWaitSend'  and param.openFrom ne 'glwd' and productId ne '0' and summaryVO.affair.state ne '1'}">
				<c:set value="${countBtn+1}" var="countBtn" />
				<c:if test="${countBtn<=5}">
					<span class="hand left span-special" style=${!isCollect ? 'display: inline-block;':'display:none;'} id="favoriteSpan${summaryVO.affairId}">
						<span class="ico16 unstore_16 margin_lr_5 " title="${ctp:i18n('collaboration.summary.favorite')}"></span>${ctp:i18n('collaboration.summary.favorite')}
					</span>
					<span class="hand left span-special"  style=${!isCollect ? 'display:none;':'display: inline-block;'} id="cancelFavorite${summaryVO.affairId}">
						<span class="ico16 stored_16 margin_lr_5" title="${ctp:i18n('collaboration.summary.favorite.cancel')}"></span>${ctp:i18n('collaboration.summary.favorite.cancel')}
					</span>
				</c:if>
				<c:if test="${countBtn>5}"><input id="favoriteFlag" type="hidden" /></c:if>
			</c:if>
		</c:if>

		<!-- 跟踪 -->
		<c:if test="${(param.openFrom eq 'listSent' or param.openFrom eq 'listDone') and isHistoryFlag ne 'true' and chuantou eq null}">
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="gzbutton">
					<span class="ico16 track_16 margin_lr_5" title="${ctp:i18n('collaboration.forward.page.label4')}"></span>${ctp:i18n('collaboration.forward.page.label4')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="gzbuttonFlag" type="hidden" /></c:if>
		</c:if>

		<!--借阅给他人时，是否是只能查看正文 部分按钮不显示-->
		<c:if test="${!onlySeeContent}">
			<!-- 明细日志 -->
			<c:if test='${!isFromTraceFlag}'>
				<c:set value="${countBtn+1}" var="countBtn" />
				<c:if test="${countBtn<=5}">
					<span class="hand left set_color_gray" id="showDetailLog">
						<span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('collaboration.sendGrid.findAllLog')}"></span>${ctp:i18n('collaboration.common.flag.showDetailLog')}
					</span>
				</c:if>
				<c:if test="${countBtn>5}"><input id="showDetailLogFlag" type="hidden" /></c:if>
			</c:if>

			<!-- 属性状态 -->
			<c:if test='${!isFromTraceFlag}'>
				<c:set value="${countBtn+1}" var="countBtn" />
				<c:if test="${countBtn<=5}">
					<span class="hand left set_color_gray" id="attributeSetting">
						<span class="ico16 attribute_16 margin_lr_5 display_none" title="${ctp:i18n('collaboration.common.flag.findAttributeSetting')}"></span>${ctp:i18n('collaboration.common.flag.attributeSetting')}
					</span>
				</c:if>
				<c:if test="${countBtn>5}"><input id="attributeSettingFlag" type="hidden" /></c:if>
			</c:if>
		</c:if><!-- 借阅给他人时，是否是只能查看正文 部分按钮不显示 -->

		<!-- 新建会议  & 及时交流  在已发已办中始终都有，不管流程结束与否，在文档中心等其他都地方没有 -->
		<c:if test="${((param.openFrom eq 'listSent')
                                   or (param.openFrom eq 'listDone')
                                   or (param.openFrom eq 'listPending')
                                   or (param.openFrom eq 'supervise'))&& !isFromTraceFlag && (isHistoryFlag ne 'true')}">
			<!-- 新建会议 -->
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="createMeeting">
					<span class="ico16 margin_lr_5" title="${ctp:i18n('collaboration.summary.createMeeting')}"></span>${ctp:i18n('collaboration.summary.createMeeting')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="createMeetingFlag" type="hidden" /></c:if>
			<!-- 即时交流 -->
			<%--<c:if test="${summaryVO.summary.exchangeSendAffairId == null}">--%>

				<%--<c:set value="${countBtn+1}" var="countBtn" />--%>
				<%--<c:if test="${countBtn<=5}">--%>
					<%--<span class="hand left set_color_gray" id="timelyExchange">--%>
						<%--<span class="ico16 communication_16 margin_lr_5" title="${ctp:i18n('collaboration.summary.timelyExchange')}"></span>${ctp:i18n('collaboration.summary.timelyExchange')}--%>
					<%--</span>--%>
				<%--</c:if>--%>
				<%--<c:if test="${countBtn>5}"><input id="timelyExchangeFlag" type="hidden" /></c:if>--%>
			<%--</c:if>--%>
		</c:if><!-- 新建会议 -->

		<!-- 追溯流程 -->
		<c:if test="${summaryVO.openFrom == 'listPending'}">
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="showWorkflowtrace">
					<span class="ico16 review_flow_16 margin_lr_5" title="${ctp:i18n('collaboration.workflow.label.lczs')}"></span>${ctp:i18n('collaboration.workflow.label.lczs')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="showWorkflowtraceFlag" type="hidden" /></c:if>
		</c:if>

		<c:choose>
		<c:when test="${ctp:escapeJavascript(param.openFrom) eq 'listSent' and (isHistoryFlag ne 'true') and chuantou eq false}">
			<%--如果是已发，则显示督办设置 --%>
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="showSuperviseSettingWindow">
					<span class="ico16 setting_16 margin_lr_5" title="${ctp:i18n('collaboration.common.flag.showSuperviseSetting')}"></span>
					${ctp:i18n('collaboration.common.flag.showSuperviseSetting')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="showSuperviseSettingWindowFlag" type="hidden" /></c:if>
		</c:when>
		<c:when
			test="${((param.openFrom eq 'listDone' and summaryVO.isCurrentUserSupervisor) or param.openFrom eq 'supervise') and (isHistoryFlag ne 'true')}">
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<!-- 督办 -->
				<span class="hand left set_color_gray" id="showSuperviseWindow">
					<span class="ico16 meeting_look_1 margin_lr_5" title="${ctp:i18n('collaboration.common.flag.showSupervise')}"></span>
					${ctp:i18n('collaboration.common.flag.showSupervise')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="showSuperviseWindowFlag" type="hidden" /></c:if>
		</c:when>
		</c:choose>

		<!--借阅给他人时，是否是只能查看正文  部分按钮不显示-->
		<c:if test="${!onlySeeContent}">
			<!--流程最大化 -->
			<c:set value="${countBtn+1}" var="countBtn" />
			<c:if test="${countBtn<=5}">
				<span class="hand left set_color_gray" id="processMaxFlag" title="${ctp:i18n('collaboration.summary.flowMax')}">
					<span class="ico16 process_max_16 margin_lr_5 "></span>
					${ctp:i18n('collaboration.summary.flowMax')}
				</span>
			</c:if>
			<c:if test="${countBtn>5}"><input id="_processMaxFlag" type="hidden" /></c:if>
		</c:if>

		<!-- 转收文信息 -->
        <c:if test='${haveTurnRecEdoc != null}'>
			<c:set value="${countBtn+1}" var="countBtn" />
			<!-- 转办信息默认放在外面，可以直接点击查看 -->
		    <span class="hand left set_color_gray" id="turnRecEdocInfoDetails" onclick="showTurnRecEdocInfo('${haveTurnRecEdoc}')"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}</span>
        </c:if>

        <!-- 被转收文信息   -->
        <c:if test='${haveTurnRecEdoc2 != null}'>
		<c:set value="${countBtn+1}" var="countBtn" />
			<!-- 转办信息默认放在外面，可以直接点击查看 -->
            <span class="hand left set_color_gray" id="turnRecEdocInfoDetails1" onclick="showTurnRecEdocInfo2('${haveTurnRecEdoc2}')"><span class="ico16 view_log_16 margin_lr_5" title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}</span>
        </c:if>

		<c:if test="${countBtn>5}"><span id="show_more" class="ico16 arrow_2_b left margin_l_5"></span></c:if>

	</div><!-- orderBt1 命令按钮区域 end -->

	<!-- 经典布局-查看界面-左侧-上部区域-提醒：当前公文尚未进行套红或盖章 -->
	<div id="checkIsTHhOrGZ" style="display:none;padding-left:10px;padding-top:9px;">
		<img src="/seeyon/apps_res/govdoc/images/notice.png" style="width:14px;height:14px;float:left;padding-top:4px;padding-left:10px;border-top-left-radius:4px;border-bottom-left-radius:4px;"/>
		<label style="float:left;padding-top:1px;background-color:#ffcfcf;border-top-right-radius: 4px;border-bottom-right-radius: 4px;">${(ctp:i18n('govdoc.edoc.isNotTHOrGz'))}</label>
	</div>

	<div style="clear: both;"></div>
</div><!-- end common_tabs -->

<!-- 经典布局-查看界面-左侧-上部区域-附件展示及修改 -->
<table border="0" cellspacing="0" id="workflowButton" cellpadding="0" width="100%" style="margin-top: 5px">
	<tr>
		<td nowrap class="bg-gray detail-subject" style="padding-left:5px; align:center; width:${summaryVO.canEditAtt?'60':'40' }px;">
			<%--如果有权限修改就显示“插入附件”按钮，没有权限就显示"附件"--%>
			<c:if test="${canEditAtt}">
				<span id="uploadAttachmentTR"><a onclick="updateAttFromSender('sender')">${ctp:i18n('govdoc.summary.updateAtt')}</a></span>
			</c:if>
		</td>
		<td>
			<div id="attachmentTRshowAttFile" style="display: none;">
				<div style="float: left; padding-top: 5px; margin-left: 0px;" class="margin_l_25">${ctp:i18n('govdoc.attachment.information')}：(<span id="attachmentNumberDivshowAttFile"></span>)</div>
				<div id="attFileDomain" isGrid="true" class="comp"
					comp="type:'fileupload',attachmentTrId:'showAttFile',canFavourite:${canFavorite},applicationCategory:'4',canDeleteOriginalAtts:false" attsdata='${attListJSON }'></div>
			</div>
		</td>
	</tr>
	<tr>
		<td nowrap class="bg-gray detail-subject" style="padding-left:5px; align:center; width:${canEditAtt?'60':'40' }px;"></td>
		<td>
			<%--关联文档 --%>
			<div id="attachment2TRDoc1" style="display: none;">
				<div style="float: left; padding-top: 5px; margin-left: 0px;" class="margin_l_25">
					${ctp:i18n('govdoc.sender.postscript.correlationDocument')}：(<span id="attachment2NumberDivDoc1"></span>)
				</div>
				<div style="float: right;" id="assDocDomain" isCrid="true" class="comp"
					comp="type:'assdoc',attachmentTrId:'Doc1',applicationCategory:'1',referenceId:'${vobj.summary.id}',modids:1,canDeleteOriginalAtts:false"
					attsdata='${attListJSON }'></div>
			</div>
			<div id="attActionLogDomain" style="display: none;"></div>
		</td>
	</tr>
</table><!-- end workflowButton -->

<!-- 经典布局-查看界面-左侧-上部区域-修改流程 -->
<div class="padding_l_25 padding_t_5 hidden" id="show_edit_workFlow">
	<a id="edit_workFlow" class="common_button common_button_gray" >${ctp:i18n('collaboration.summary.updateFlow')}</a>
</div><!-- end show_edit_workFlow -->

</div><!-- end summaryHead -->

<!-- 经典布局-查看界面-左侧-中部区域-->
<div id="content_workFlow" class="stadic_layout_body stadic_body_top_bottom processing_view align_center" style="width: 100%; top: 40px; visibility: visible;">

	<%-- 公文正文位置 --%>
	<%@ include file="/WEB-INF/jsp/govdoc/govdocBody.jsp"%>

	<div style="position: absolute; overflow: hidden; width: 100%; height: 10px; -moz-box-shadow: inset 0px 3px 5px #A8A8A8; box-shadow: inset 0px 3px 5px #A8A8A8;">&nbsp;</div>
		<!-- 附件列表 -->
		<iframe id="attachmentList" frameborder="0" style="display: none; position: absolute; top: 0px; right: 20px; width: 650px; z-index: 200; height: 180px" class="over_auto align_right" src=""></iframe>

		<!-- html正文修改iframe -->
		<c:if test="${summaryVO.bodyVo.bodyType eq 'HTML' && hasDealArea && summaryVO.switchVo.canEditBody}">
          	<iframe id="htmlBodyIframe" name="htmlBodyIframe" width="100%" height="100%" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" style="display: none;">
          	</iframe>
          	<script>
				$("#htmlBodyIframe").attr("src", "${path }/govdoc/govdoc.do?method=htmlBody&summaryId=${summaryVO.summary.id}");
			</script>
		</c:if>

		<!-- 流程iframe -->
		<iframe id="iframeright" class="hidden bg_color_white" src="about:blank" width="100%" height="100%" frameborder="0" scrolling="no"></iframe>

		<!-- 公文单iframe -->
		<iframe id="componentDiv" name="componentDiv" width="100%" height="100%" frameborder="0" style='${onlySeeContent? "display:none" : ""}'></iframe>
		<script>
			//公文单加载
			$("#componentDiv").attr("src", "${componentDivUrl }");
		</script>

		<!-- 公文签批单iframe -->
		<c:if test="${not empty summaryVO.aipFileId}">
        <iframe name="qwqpIframe" id="qwqpIframe" width="100%" height="100%" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" src="" style="display:none;"></iframe>
		</c:if>

        <%-- <!-- html正文修改iframe -->注释：经典布局不用这个展示这个html的，因为上面那个里面返回的htmlbod.jsp中有，相关bug：OA-175227。出现这个bug 的原因是浏览器窗体大小出现改变没有重新计算高度导致下面的内容顶上来了。从而暴露出这个问题。
		<c:if test="${summaryVO.bodyVo.bodyType eq 'HTML'}">
			<v3x:showContent  htmlId="edoc-contentText" content="${summaryVO.bodyVo.content}" type="${summaryVO.bodyVo.bodyType}" createDate="${summaryVO.bodyVo.createDate}"  viewMode ="edit"/>
		</c:if> --%>
		<!-- 转PDF后显示PDF正文 -->
		<c:if test="${ not empty summaryVO.bodyVo.pdfFileId }">
			<iframe id="transPdfIframe" name="transPdfIframe" width="100%" height="100%" frameborder="0" scrolling="no" marginheight="0" marginwidth="0"></iframe>
			<c:if test="${hasDealArea and (summaryVO.switchVo.fengsong)}">
				<script>
					//公文单加载
					$("#transPdfIframe").attr("src", "${path }/govdoc/govdoc.do?method=transBody&summaryId=${summaryVO.summary.id}&contentType=45");
				</script>
			</c:if>
		</c:if>
        <!-- 转OFD后显示OFD正文 -->
        <c:if test="${ not empty summaryVO.bodyVo.ofdFileId }">
        <iframe id="transOfdIframe" name="transOfdIframe" width="100%" height="100%" frameborder="0" scrolling="no" marginheight="0" marginwidth="0"  src="" style="display:none;"></iframe>
        </c:if>

		<!-- 相关查询 iframe -->
		<iframe id="formRelativeDiv" name="formRelativeDiv" width="100%" height="100%" frameborder="0" class="hidden bg_color_white" src=""></iframe>
	</div><!-- end content_workFlow -->

	<!-- 经典布局-查看界面-左侧-中部区域-设置跟踪弹出框-->
	<%-- 跟踪区域开始 --%>
	<input type="hidden" id="zdgzry" name="zdgzry" />
	<input type="hidden" name="can_Track" id="can_Track" value="${summary.canTrack ? 1 : 0}" />
    <input type="hidden" name="trackType" id="trackType" value="${trackType}" />
       <div id="htmlID" class="hidden">
		<div class="padding_tb_10 padding_l_10">
            	<input type="text" style="display: none" id="zdgzryName" name="zdgzryName" size="30" onclick="$('radio4').click()"/>
                <%-- 跟踪 --%>
                <span class="valign_m">${ctp:i18n('collaboration.forward.page.label4')}:</span>
                <select id="gz" class="valign_m">
                    <option value="1">${ctp:i18n('message.yes.js')}</option><%-- 是 --%>
                    <option value="0">${ctp:i18n('message.no.js')}</option><%-- 否 --%>
                </select>
                <div id="gz_ren" class="common_radio_box clearfix margin_t_10">
                    <label for="radio1" class="margin_r_10 hand"><input type="radio" value="0" id="radio1" name="option" class="radio_com">${ctp:i18n('collaboration.listDone.all')}</label><!-- 全部 -->
                    <label for="radio4" class="margin_r_10 hand"><input type="radio" value="0" id="radio4" name="option" class="radio_com">${ctp:i18n('collaboration.listDone.designee')}</label><!-- 指定人 -->
                </div><!-- gz_ren -->
        	</div>
   		</div><!-- htmlID -->
	<%-- 跟踪区域结束 --%>

</div><!-- stadic_layout -->
</div><!-- layout_center -->

<%-- 处理界面参数，用于提交到后端，放到layout中 start --%>
<input type="hidden" name="secretLevel" id="secretLevel" value="${summaryVO.summary.secretLevel}" />
<input type="hidden" id="affairId" value="${summaryVO.affairId}">
<%@ include file="../govdoc_mark_deal.jsp" %>
<%@ include file="/WEB-INF/jsp/common/content/workflow.jsp"%>
