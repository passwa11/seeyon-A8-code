<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!--标题+附件区域-->
<span style="display:none;"> 
	<a href="#" class="color_gray2" id="panleStart">${ctp:toHTML(summaryVO.startMemberName)}</a>
	<span class="color_gray2">${ctp:formatDateByPattern(summaryVO.summary.createTime, 'yyyy-MM-dd HH:mm')}</span>
</span>
<%--暂时头部区域--%>
<div id="edocContainerFooterStrategy" class="edocContainerFooter clear clearfix ">

    <!-- 一屏式布局A-查看界面-下部按钮区域-提醒：当前公文尚未进行套红或盖章 -->
    <div id="checkIsTHhOrGZ" class="footerTips" style="display:none;">
        <div><em class="gov_summary_warn_icon"></em><span>${ctp:i18n('govdoc.edoc.isNotTHOrGz')}</span></div>
    </div>
    <!-- 查看原文档按钮 -->
    <c:if test="${(summaryVO.bodyVo.bodyType eq 'OfficeWord' ||
					   summaryVO.bodyVo.bodyType eq 'OfficeExcel' ||
					   summaryVO.bodyVo.bodyType eq 'WpsWord' ||
					   summaryVO.bodyVo.bodyType eq 'WpsExcel' ||
					   summaryVO.bodyVo.bodyType eq 'Pdf') &&
		   			  showContentByGovdocNodePropertyConfig && v3x:isOfficeTran()}">
        <div class="view-orginal-btn viewOriginalContentA">
		<span class="hand orginal-view-content" onclick="showEditorDialog()">
		<em class="ico16 text_16"
            title="${ctp:i18n('collaboration.content.viewOriginalContent')}"></em><span>${ctp:i18n("collaboration.content.viewOriginalContent")}</span>
		</span>
        </div>
    </c:if>
    <!-- 查看原文档end -->
    <!-- 一屏式布局A-查看界面-下部按钮区域-策略显示区域 -->
    <div id="toolb" style="display: inline-block;vertical-align: top;"></div>
    <div id="toolb_temp" style="display:none;vertical-align: top;"></div>
    <c:if test="${hasDealRole}">
        <c:set var="commonActionNodeCount" value="0"/>
        <c:forEach items="${summaryVO.commonActionList}" var="operation"><!-- 常用策略 -->
            <c:set var="commonActionNodeCount" value="${commonActionNodeCount+1}"/>
            <%-- 可修改流程 --%>
            <c:if test="${summaryVO.switchVo.canModifyWorkFlow && summaryVO.switchVo.superNodestatus==0}">
                <%--加签 --%><c:if test="${'AddNode' eq operation}"><input type="hidden"
                                                                         id="tool_${commonActionNodeCount}"
                                                                         value="_commonAddNode"/></c:if>
                <%--分发 --%><c:if test="${'FaDistribute' eq operation}"><input type="hidden"
                                                                              id="tool_${commonActionNodeCount}"
                                                                              value="_commonFaDistribute"/></c:if>
                <%--当前会签 --%><c:if test="${'JointSign' eq operation}"><input type="hidden"
                                                                             id="tool_${commonActionNodeCount}"
                                                                             value="_commonAssign"/></c:if>
                <%--减签 --%><c:if test="${'RemoveNode' eq operation}"><input type="hidden" value="_commonDeleteNode"
                                                                            id="tool_${commonActionNodeCount}"/></c:if>
                <%--知会 --%><c:if test="${'Infom' eq operation}"><input type="hidden" value="_commonAddInform"
                                                                       id="tool_${commonActionNodeCount}"/></c:if>
                <%--多级会签 --%><c:if test="${'moreSign' eq operation }"><input type="hidden" value="_commonMoreSign"
                                                                             id="tool_${commonActionNodeCount }"/></c:if>
                <%--传阅 --%><c:if test="${('PassRead' eq operation)}"><input type="hidden" value="_commonPassRead"
                                                                            id="tool_${commonActionNodeCount}"/></c:if>
            </c:if><!-- end 可修改流程 summaryVO.switchVo.canModifyWorkFlow && summaryVO.switchVo.superNodestatus==0 &&summaryVO.openFrom == 'listPending'&&summaryVO.affair.state eq 3 -->

            <%--回退 --%><c:if test="${ ('Return' eq operation) && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_commonStepBack" id="tool_${commonActionNodeCount}"/></c:if>
            <%--修改附件 --%><c:if
                    test="${('allowUpdateAttachment' eq operation)  && summaryVO.switchVo.superNodestatus==0 }"><input
                    type="hidden" value="_commonUpdateAtt" id="tool_${commonActionNodeCount}"/></c:if>
            <%--分办 --%><c:if test="${('Distribute' eq operation) && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_distribute" id="tool_${commonActionNodeCount}"/></c:if>
            <%--联合发文 --%><c:if test="${('JointlyIssued' eq operation) && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_jointlyIssued" id="tool_${commonActionNodeCount}"/></c:if>
            <%--终止 --%><c:if test="${('Terminate' eq operation) && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_commonStepStop" id="tool_${commonActionNodeCount}"/></c:if>
            <%--撤销--%><c:if
                    test="${ ('Cancel' eq operation  && !summaryVO.isNewflow) && summaryVO.switchVo.superNodestatus==0 }"><input
                    type="hidden" value="_commonCancel" id="tool_${commonActionNodeCount}"/></c:if>
            <%--转发--%><c:if test="${ ('Forward' eq operation)  && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_commonForward" id="tool_${commonActionNodeCount}"/></c:if>
            <%--文单签章 --%><c:if
                    test="${ ('Sign' eq operation && summaryVO.summary.bodyType ne '45')  && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_commonSign" id="tool_${commonActionNodeCount}"/></c:if>
            <%--转事件 --%><c:if test="${ ('Transform' eq operation)  && summaryVO.switchVo.superNodestatus==0 }"><input
                    type="hidden" value="_commonTransform" id="tool_${commonActionNodeCount}"/></c:if>
            <%--督办设置--%><c:if test="${('SuperviseSet' eq operation)  && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_commonSuperviseSet" id="tool_${commonActionNodeCount}"/></c:if>
            <%--指定回退--%><c:if
                    test="${ ('SpecifiesReturn' eq operation )  && summaryVO.switchVo.superNodestatus==0}"><input
                    type="hidden" value="_dealSpecifiesReturn" id="tool_${commonActionNodeCount}"/></c:if>
            <%--修改正文 --%><c:if test="${('Edit' eq operation)}"><input type="hidden" value="_commonEdit"
                                                                      id="tool_${commonActionNodeCount}"/></c:if>
            <%--正文套红 --%><c:if test="${('EdocTemplate' eq operation) }"><input type="hidden" value="_commonEdocTemplate"
                                                                               id="tool_${commonActionNodeCount}"/></c:if>
            <%--文单套红--%><c:if test="${('ScriptTemplate' eq operation)}"><input type="hidden"
                                                                               value="_commonScriptTemplate"
                                                                               id="tool_${commonActionNodeCount}"/></c:if>
            <%--WORD转PDF --%><c:if test="${('TanstoPDF' eq operation)}"><input type="hidden" value="_commonTanstoPDF"
                                                                               id="tool_${commonActionNodeCount}"/></c:if>
            <%--正文转OFD --%><c:if test="${('TransToOfd' eq operation)}"><input type="hidden" value="_commonTransToOfd"
                                                                              id="tool_${commonActionNodeCount}"/></c:if>
            <%--正文盖章 --%><c:if test="${('ContentSign' eq operation)}"><input type="hidden" value="_commonContentSign"
                                                                             id="tool_${commonActionNodeCount}"/></c:if>
            <%--文单签批 --%><c:if test="${('HtmlSign' eq operation)}"><input type="hidden" value="_commonHtmlSign"
                                                                          id="tool_${commonActionNodeCount}"/></c:if>
            <%--全文签批 --%><c:if test="${('PDFSign' eq operation)}"><input type="hidden" value="_commonPDFSign"
                                                                         id="tool_${commonActionNodeCount}"/></c:if>
            <%--签批缩放 --%><c:if test="${('SignChange' eq operation)}"><input type="hidden" value="_commonSignChange"
                                                                            id="tool_${commonActionNodeCount}"/></c:if>
            <%--转公告 --%><c:if test="${('TransmitBulletin' eq operation)}"><input type="hidden"
                                                                                 value="_commonTransmitBulletin"
                                                                                 id="tool_${commonActionNodeCount}"/></c:if>
            <%--转收文 --%><c:if test="${('TurnRecEdoc' eq operation)}"><input type="hidden" value="_commonTurnRecEdoc"
                                                                            id="tool_${commonActionNodeCount}"/></c:if>
            <%--转督办 --%><c:if test="${('TranstoSupervise' eq operation)}"><input type="hidden"
                                                                                 value="_commonTranstoSupervise"
                                                                                 id="tool_${commonActionNodeCount}"/></c:if>
            <%--转发文(启用节点转发文策略) --%><c:if test="${('Zhuanfawen' eq operation )&&('true' eq zhuanfawenTactics)}"><input
                    type="hidden" value="_commonZhuanfawen" id="tool_${commonActionNodeCount}"/></c:if>
            <%--移交 --%><c:if test="${('Transfer' eq operation )}"><input type="hidden" value="_commonTransfer"
                                                                         id="tool_${commonActionNodeCount}"/></c:if>
            <%--转事务 --%><c:if test="${('Zhuanshiwu' eq operation )}"><input type="hidden" value="_commonZhuanshiwu"
                                                                            id="tool_${commonActionNodeCount}"/></c:if>
        </c:forEach><!-- end summaryVO.commonActionList -->
    </c:if><!-- end hasDealRole -->

    <c:if test="${hasDealArea}">
		<span class="edocContainerFooterMore" id="moreLabel" style="line-height:65px;">
		<em class="ico16 arrow_2_b" style="position: relative;top: 4px;"></em>
		</span>
    </c:if>

    <%-- 续办 --%>
    <c:if test="${hasDealArea && summaryVO.switchVo.canXuban}">
        <c:set value="true" var="showMoreFlag"></c:set>
        <div class="customDealWithDiv" id="xuban">
            <input type="hidden" id="customDealWithActivitys" name="customDealWithActivitys">
            <c:if test="${summaryVO.xubanVo.nextMember != null }">
                <input type="hidden" id="nextMember"
                       userId="${summaryVO.xubanVo.nextMember.id }"
                       userName="${summaryVO.xubanVo.nextMember.name }"
                       accountId="${summaryVO.xubanVo.nextMember.orgAccountId }"
                       policyId="${summaryVO.xubanVo.currentPolicyId}"
                       policyName="${summaryVO.xubanVo.currentPolicyName }"
                       notExistChengban="${summaryVO.xubanVo.notExistChengban }">
            </c:if>
            <table border="0" cellspacing="0" cellpadding="0" style="table-layout: fixed;">
                <tr>
                    <td class="align_left">
                        <label class="hand">
                            <!-- 续办人员: -->
                            <input id="customDealWith" type="checkbox" class="radio_com"
                                   onclick="checkSpecifyFallback();" style="margin-top: -8px;"
                                   <c:if test="${summaryVO.switchVo.canXuban}">checked='checked'</c:if>>
                            <span style="font-size:12px;">${ctp:i18n('govdoc.customDealWith.member.label')}</span>
                        </label>
                    </td>
                    <td class="padding_5">
                        <select id="permissionRange" onchange="permissionChange(this);" style="width: 120px;">
                            <option value="0"
                                    selected="selected">${ctp:i18n('govdoc.customDealWith.pleaseChooseMode')}</option>
                            <c:forEach items="${summaryVO.xubanVo.permissions }" var="permission" varStatus="status">
                                <option value="${permission.name }" title="${permission.label}"
                                        <c:if test="${summaryVO.xubanVo.customDealWithPermission eq permission.name }">selected='selected'</c:if>
                                        <c:if test="${status.index lt summaryVO.xubanVo.returnPermissionsLength }">return='true'</c:if>>${permission.label}
                                </option>
                            </c:forEach>
                        </select>
                    </td>
                    <td class="padding_5">
                        <select id="memberRange" style="width: 125px" onchange="memberRangeChange(this);">
                            <option value="0"
                                    selected="selected">${ctp:i18n('govdoc.customDealWith.pleaseChooseMember')}</option>
                        </select>
                    </td>
                </tr>
            </table>
        </div>
        <!-- end xuban -->
    </c:if><!-- end hasDealArea && summaryVO.switchVo.canXuban -->
    <%--收藏 --%>
    <%--判断是否有处理后归档权限或者发送协同时勾选了‘归档’，如果没有，则不能收藏 ,a6没有收藏功能,knowledge从文档中心打开不显示收藏--%>
    <c:if test="${canFavorite && !isFromTraceFlag}">
        <c:if test="${ctp:escapeJavascript(param.openFrom) ne 'favorite' and ctp:escapeJavascript(param.openFrom) ne 'listWaitSend'  and ctp:escapeJavascript(param.openFrom) ne 'glwd' and productId ne '0' and summaryVO.affair.state ne '1'}">
			<span class="edocContainerFooterStored" id="edocContainerFooterStored"><em
                    class="ico16 ${!isCollect ?'unstore_16':'stored_16'}"></em>
			<c:if test="${!isCollect }">
                ${ctp:i18n('common.collection.label')}
            </c:if>
			<c:if test="${isCollect }">
                ${ctp:i18n('common.cancel.collection.label')}
            </c:if>
			</span>
        </c:if>
    </c:if><!-- end canFavorite && !isFromTraceFlag && !onlySeeContent -->
    <!-- 致信客户端 -->
    <script type="text/javascript" charset="UTF-8"
            src="${path}/apps_res/uc/rongcloud/chat.js${ctp:resSuffix()}"></script>

    <div id="_dealDiv" style="float:right;">
        <%--       zhou: 更多--%>
        <%--        <span class="edocHandleContentDiv1More" id="edocHandleContentDiv1More">--%>
        <%--            ${ctp:i18n('govdoc.more.label')}<em class="ico16 arrow_2_b"></em>--%>
        <%--        </span>--%>
        <%-- zhou--%>
        <div id="gnBtn"></div>
        <c:if test="${hasDealArea}">
            <c:choose>
                <c:when test="${summaryVO.switchVo.showButton=='sign'}"><!-- 签收 -->
                    <a id="_dealSubmit" href="javascript:void(0)"
                       class="common_button common_button_emphasize">${ctp:i18n('govdoc.affair.reSign')}</a>
                    <input type="hidden" id="reSign" name="reSign" value="1"/>
                </c:when>
                <c:when test="${summaryVO.switchVo.showButton=='distribute'}"><!-- 分办 -->
                    <input id="_distribute" type="button" class="common_button common_button_emphasize margin_r_5 hand"
                           style="width: auto;max-width: none;min-width: 63px;"
                           value="${ctp:i18n('govdoc.default.distribute')}">
                    <input type="hidden" id="_dealSubmit"/>
                </c:when>
                <c:when test="${summaryVO.switchVo.showButton=='distributeAndFinish'}"><!-- 分办或提交 -->
                    <a id="_dealSubmit" href="javascript:void(0)"
                       class="common_button common_button_emphasize">${ctp:i18n('common.button.submit.label')}</a>
                    <a id="_distribute" href="javascript:void(0)"
                       class="common_button common_button_emphasize">${ctp:i18n('govdoc.default.distribute')}</a>
                </c:when>
                <c:when test="${summaryVO.switchVo.fengsong }"><!-- 分送 -->
                    <a id="_dealSubmit" href="javascript:void(0)" class="common_button common_button_emphasize"
                       style="width: 28px; height: 28px; line-height: 28px;"
                       href="javascript:void(0)">${ctp:i18n('govdoc.distribute')}</a>
                    <input type="hidden" id="isFaDistribute" value="1"/>
                </c:when>
                <c:otherwise>
                    <c:if test="${(ctp:containInCollection(summaryVO.basicActionList, 'ContinueSubmit'))}">
                        <input id="_dealSubmit" type="button" class="common_button common_button_emphasize"
                               value="${ctp:i18n('common.button.submit.label')}"/>
                    </c:if>
                </c:otherwise>
            </c:choose>

            <%--存为草稿 --%>
            <c:if test="${(summaryVO.switchVo.canShowOpinion and summaryVO.switchVo.canComment) && (summaryVO.switchVo.superNodestatus==0 || summaryVO.switchVo.superNodestatus==1)}">
                <a id="_dealSaveDraft" href="javascript:void(0)"
                   class="common_button">${ctp:i18n('common.toolbar.saveDraftOpinion.label')}</a>
            </c:if>

            <!-- 暂存待办 -->
            <c:if test="${(summaryVO.switchVo.canComment) && (summaryVO.switchVo.superNodestatus==0 || summaryVO.switchVo.superNodestatus==2)}">
                <a id="_dealSaveWait" href="javascript:void(0)"
                   class="common_button">${ctp:i18n("common.save.and.pause.flow")}</a>
            </c:if>
            <span class="edocContainerFooterMore" id="submitMoreLabel" style="display:none;">
			<em class="ico16 arrow_2_b" style="top: 4px; position: relative;"></em>
    	</span>
        </c:if><!-- end hasDealArea -->
    </div><!-- end _dealDiv -->
    <div id="customerToolBar" class="left" style="padding-left:9px;"></div>


</div>
<!-- end edocContainerFooterStrategy -->
<div class="edocContainer" style="${onlySeeContent ? 'bottom:0px;' : ''}">

    <!-- 一屏式布局A-查看界面-中部展示区域-左侧正文区域 -->
    <div class="right edocContainerLeft">
        <!-- 一屏式布局A-查看界面-中部展示区域-左侧正文区域-上部页签切换  -->
        <div class="head" id="edocContainerLeftHead" style="display:none">
            <!-- 正文 -->
            <div id="body_view_li" class="selectStyle" data-type="1">${ctp:i18n('govdoc.summary.text')}</div>
            <!-- 转PDF后显示PDF正文 -->
            <c:if test="${ not empty summaryVO.bodyVo.pdfFileId }">
                <div id="transpdf_view_li" data-type="2">${ctp:i18n('govdoc.pdf.content.text')}</div>
            </c:if>
            <!-- 转OFD后显示OFD正文 -->
            <c:if test="${ not empty summaryVO.bodyVo.ofdFileId }">
                <div id="transofd_view_li" data-type="3">${ctp:i18n('govdoc.ofd.content.text')}</div>
            </c:if>
        </div><!-- end edocContainerLeftHead -->

        <!-- 一屏式布局A-查看界面-中部展示区域-左侧正文区域-下部正文展示区载  -->
        <div class="content" id="contentDIV" style="width: 94%;">
            <%--公文正文位置 --%>
            <%@ include file="/WEB-INF/jsp/govdoc/govdocBody.jsp" %>

            <c:choose>
                <c:when test="${showContentByGovdocNodePropertyConfig }">
                    <!-- html正文修改iframe -->
                    <c:if test="${summaryVO.bodyVo.bodyType eq 'HTML' && hasDealArea && summaryVO.switchVo.canEditBody}">
                        <iframe id="htmlBodyIframe" name="htmlBodyIframe" width="100%" height="100%" frameborder="0"
                                scrolling="no" marginheight="0" marginwidth="0" style="display: none;"></iframe>
                        <script>
                            $("#htmlBodyIframe").attr("src", "${path }/govdoc/govdoc.do?method=htmlBody&summaryId=${summaryVO.summary.id}&affairId=${summaryVO.affairId}");
                        </script>
                    </c:if>
                    <!-- 转PDF后显示PDF正文 -->
                    <c:if test="${not empty summaryVO.bodyVo.pdfFileId }">
                        <iframe id="transPdfIframe" name="transPdfIframe" width="100%" height="100%" frameborder="0"
                                scrolling="no" marginheight="0" marginwidth="0"></iframe>
                    </c:if>
                    <!-- 转OFD后显示PDF正文 -->
                    <c:if test="${ not empty summaryVO.bodyVo.ofdFileId }">
                        <iframe id="transOfdIframe" name="transOfdIframe" width="100%" height="100%" frameborder="0"
                                scrolling="no" marginheight="0" marginwidth="0" src="" style="display:none;"></iframe>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div style="text-align:center;top:200px;position:relative;">
                        <img id="cantSeeContentTip" src="<c:url value='/apps_res/govdoc/images/eye.png'/>"/>
                        <p>${ctp:i18n('govdoc.browsing.permission')}</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div><!-- end contentDIV -->
    </div><!-- end edocContainerLeft -->

    <!-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域 -->
    <c:choose>
        <c:when test="${onlySeeContent}"><%-- 只允许查看正文 --%>
            <div class="left edocContainerRight edocBorderbox">
                <div class="edocContainerRightContent" id="edocContainerRightContent">
                    <img id="cantSeeContentTip" src="<c:url value='/apps_res/govdoc/images/eye.png'/>"
                         style="text-align:center;top:200px;position:relative;"/>
                    <p>${ctp:i18n('govdoc.form.permission')}</p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-右侧页签 --%>
            <div class="left edocContainerRight edocBorderbox">
                <c:if test="${hasDealArea}">
                    <%@ include file="govdoc_deal_top.jsp" %>
                </c:if><!-- end hasDealArea -->
                <c:if test="${!hasDealArea}">
                    <style>
                        .edocContainerRight {
                            padding: 5px 39px 3px 10px;
                        }
                    </style>
                </c:if>
                <c:if test="${!(summaryVO.switchVo.duanxintixing || summaryVO.switchVo.canTrack || (summaryVO.summary.canArchive  && summaryVO.switchVo.superNodestatus==0 && summaryVO.switchVo.canArchive))}">
                    <style>
                        .edocHandleContent {
                            padding-bottom: 15px;
                        }
                    </style>
                </c:if>
                <div class="edocContainerRightContent" id="edocContainerRightContent">

                        <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-意见(展现) --%>
                    <div id="commentContent" class="edocRightContent w100b h100b">
                        <iframe id="commentDiv" name="commentDiv" width="99%" height="95%" frameborder="0" src=''
                                style=''></iframe>
                    </div><!-- end commentContent -->

                        <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-文单(展现) --%>
                    <div id="formContent" class="edocRightContent display_block w100b h100b">
                        <iframe id="componentDiv" name="componentDiv" width="99%" height="100%" frameborder="0"
                                style='${onlySeeContent? "display:none" : ""}' src="${componentDivUrl}"></iframe>
                        <script>
                            //公文单加载
                            // $("#componentDiv").attr("src", "${ctp:escapeJavascript(componentDivUrl)}");
                            if ("${summaryVO.bodyVo.pdfFileId}" != "") {
                                if ("${hasDealArea and (summaryVO.switchVo.fengsong)}" == "true") {
                                    $("#transPdfIframe").attr("src", "${path }/govdoc/govdoc.do?method=transBody&summaryId=${summaryVO.summary.id}&contentType=45");
                                }
                            }
                        </script>
                    </div><!-- end formContent -->

                        <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-全文签批单(展现) --%>
                    <c:if test="${not empty summaryVO.aipFileId}">
                        <div id="signContent" class="edocRightContent w100b h100b">
                            <iframe name="qwqpIframe" id="qwqpIframe" width="100%" height="95%" frameborder="0"
                                    scrolling="yes" marginheight="0" marginwidth="0" src=""
                                    style="display:none;"></iframe>
                        </div>
                        <!-- end signContent -->
                    </c:if>

                        <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-附件及相关信息(展现) --%>
                    <div id="attContent" class="edocRightContent h100b" style="overflow-y:auto;">
                        <!-- 附件列表 -->
                        <c:if test='${!isFromTraceFlag}'>
                            <iframe id="attachmentList" frameborder="0"
                                    style="display: none; position: absolute; top: 45px; right: 40px; width: 620px; z-index: 200; height: 180px"
                                    class="over_auto align_right" src=""></iframe>

                            <span style="padding-right:10px" class="hand right set_color_gray" id="attachmentListFlag1"
                                  onclick="javascript:showOrCloseAttachmentList(true)">
					<span class="ico16 affix_16 margin_lr_5"
                          title="${ctp:i18n('collaboration.summary.findAttachmentList')}"></span>
					${ctp:i18n('collaboration.common.flag.attachmentList')}
				</span>
                        </c:if>

                            <%--如果有权限修改就显示“插入附件”按钮，没有权限就显示"附件"--%>
                        <c:if test="${canEditAtt}">
				<span id="uploadAttachmentTR" style="padding-right:10px" class="hand right set_color_gray"
                      onclick="javascript:updateAttFromSender('sender')">
					<span class="ico16 editor_16 margin_lr_5"
                          title="${ctp:i18n('govdoc.summary.updateAtt')}"></span>${ctp:i18n('govdoc.summary.updateAtt')}
				</span>
                        </c:if>

                        <table border="0" cellspacing="0" cellpadding="0" width="100%" class="newgovdoc_table">
                            <tr>
                                <td class="newgovdoc_att_td">
                                    <div class="edocRightContent3h3"
                                         style="background-color: rgb(241, 241, 241);">${ctp:i18n('govdoc.official.attachment')}</div>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <!-- 附件 -->
                                    <div id="attachmentTRshowAttFile" style="display: none;">
                                        <div style="display: none;float:left;padding-top:5px;margin-left:0px;"><span
                                                class="ico16 affix_16"></span>(<span
                                                id="attachmentNumberDivshowAttFile"></span>)
                                        </div>
                                        <div id="attFileDomain" isGrid="true" class="comp"
                                             comp="type:'fileupload',newGovdocView:1,attachmentTrId:'showAttFile',canFavourite:${canFavorite},applicationCategory:'4',canDeleteOriginalAtts:false"
                                             attsdata='${attListJSON }'></div>
                                    </div>
                                    <!-- 关联文档 -->
                                    <div id="attachment2TRDoc1" style="display: none;">
                                        <div style="display: none;float:left;padding-top:5px;margin-left:0px;"><span
                                                class="ico16 associated_document_16"></span>(<span
                                                id="attachment2NumberDivDoc1"></span>)
                                        </div>
                                        <div style="float: right;" id="assDocDomain" isCrid="true" class="comp"
                                             comp="type:'assdoc',newGovdocView:1,attachmentTrId:'Doc1',applicationCategory:'4',displayMode:'auto',modids:1,canDeleteOriginalAtts:false"
                                             attsdata='${attListJSON }'></div>
                                    </div>
                                    <div id="attActionLogDomain" style="display: none;"></div>
                                </td>
                            </tr>
                        </table><!-- end newgovdoc_table -->

                        <!-- 发文交换状态 -->
                        <c:if test="${summaryVO.summary.govdocType==1 && govdocview_delivery!=0 }">
                            <c:set value="" var="exchangeAStatus"/>
                            <c:if test="${isSender!=1}">
                                <c:set value="disabled='true' style='color:black'" var="exchangeAStatus"/>
                            </c:if>
                            <table border="0" cellspacing="0" cellpadding="0" width="100%" class="newgovdoc_table">
                                <tr style="height:60px;">
                                    <td class="newgovdoc_att_td" style="position: absolute;left: 25px; right: 55px;">
                                        <div style="background-color: rgb(241, 241, 241);"
                                             class="edocRightContent3h3">${ctp:i18n('govdoc.DistributeState.label')}</div>
                                    </td>
                                </tr>
                                <tr style="font-size:12px;">
                                    <td style="line-height:2;width:12%">
                                        <div style="padding-left: 10px;">${ctp:i18n('govdoc.exchange.senddetail.totalmsg.label')}<a
                                                id="totalCount" ${exchangeAStatus }
                                                onclick="showDistributeState('${summaryVO.summary.id}')">${govdocview_delivery }</a>${ctp:i18n('govdoc.exchange.senddetail.homemsg.label')}，
                                        </div>
                                    </td>
                                    <td style="line-height:2;width:8%">
                                        <div>${ctp:i18n('govdoc.done.exSign.label')}<a
                                                id="sendCount" ${exchangeAStatus }
                                                onclick="showDistributeState('${summaryVO.summary.id}',2)">${govdocview_signed }</a>${ctp:i18n('govdoc.exchange.senddetail.homemsg.label')}，
                                        </div>
                                    </td>
                                    <td style="line-height:2;width:8%">
                                        <div>${ctp:i18n('govdoc.pending.exSign.label')}<a
                                                id="waitSignCount" ${exchangeAStatus }
                                                onclick="showDistributeState('${summaryVO.summary.id}',1)">${govdocview_waitSign }</a>${ctp:i18n('govdoc.exchange.senddetail.homemsg.label')}，
                                        </div>
                                    </td>
                                    <td style="line-height:2;width:25%">
                                        <div>${ctp:i18n('govdoc.rollback.already')}<a id="backCount" ${exchangeAStatus }
                                                                                      onclick="showDistributeState('${summaryVO.summary.id}',10)">${govdocview_hasBack }</a>${ctp:i18n('govdoc.exchange.senddetail.homemsg.label')}
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </c:if>

                        <table border="0" cellspacing="0" cellpadding="0" width="100%" class="">
                            <tr>
                                <td class="newgovdoc_att_td">
                                    <div class="edocRightContent3h3"
                                         style="background-color: rgb(241, 241, 241);">${ctp:i18n('govdoc.attachment.flow')}</div>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <div id="attachmentTRATT" style="">
                                        <div style="display: none;float:left;padding-top:5px;margin-left:0px;"><span
                                                class="ico16 affix_16"></span>(<span id="attachmentNumberDivATT"></span>)
                                        </div>
                                        <div id="lzAttFileDomain" isGrid="true" class="comp"
                                             comp="type:'fileupload',newGovdocView:1,attachmentTrId:'lzAtt',canFavourite:${canFavorite},applicationCategory:'4',canDeleteOriginalAtts:false,autoHeight:true"
                                             attsdata='${commentShowAttrstr}'></div>
                                    </div>
                                    <!-- 关联文档 -->
                                    <div id="attachment2TRATT" style="">
                                        <div style="display: none;float:left;padding-top:5px;margin-left:0px;"><span
                                                class="ico16 associated_document_16"></span>(<span
                                                id="attachment2NumberDivATT"></span>)
                                        </div>
                                        <div style="float: right;" isCrid="true" class="comp"
                                             comp="type:'assdoc',newGovdocView:1,attachmentTrId:'Doc2',applicationCategory:'4',displayMode:'auto',modids:1,canDeleteOriginalAtts:false"
                                             attsdata='${commentShowAttrstr }'></div>
                                    </div>

                                </td>
                            </tr>
                        </table>

                        <table border="0" cellspacing="0" cellpadding="0" width="100%">
                            <!-- 联合发文 -->
                            <c:if test="${_jointlyIssued eq '1'}">
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('processLog.action.name.163')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="jointlyIssued"><span
                                            class="syIcon sy-unite-docdispatch margin_lr_5" style="color:#1f85ec;"
                                            title="${ctp:i18n('processLog.action.name.163')}"
                                            onclick="showJointlyListFunc()"></span>${ctp:i18n('processLog.action.name.163')}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 收文信息 -->
                            <c:if test="${govdocExchangeMainId != null }">
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.information.receive')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="exchangeRecInfo"
                                              onclick="exchangeRecInfo('${govdocExchangeMainId}')"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.exchange.recInfo.label')}"></span>${ctp:i18n('govdoc.exchange.recInfo.label')}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 收文信息 -->
                            <c:if test="${govdocExchangeRecSummaryId != null }">
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.information.receive')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="exchangeRecInfo"
                                              onclick="showSummary('${govdocExchangeRecSummaryId}')"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.exchange.recInfo.label')}"></span>${govdocExchangeRecSummarySubject}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 签收信息 -->
                            <c:if test="${govdocExchangeSignSummaryId != null }">
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.information.sign')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="exchangeSignProcess"
                                              onclick="showSummary('${govdocExchangeSignSummaryId}')"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.exchange.signProcess.label')}"></span>${govdocExchangeSignSummarySubject}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 来文信息 -->
                            <c:if test="${govdocExchangeSendSummaryId != null && haveTurnRecEdoc2 == null}">
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.information.communication')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="exchangeSendInfo"
                                              onclick="showSummary('${govdocExchangeSendSummaryId}')"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.exchange.sendInfo.label')}"></span>${govdocExchangeSendSummarySubject}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 转办信息 -->
                            <c:if test="${haveTurnRecEdoc2 != null}">
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="turnRecEdocInfoDetails1"
                                              onclick="showTurnRecEdocInfo2('${haveTurnRecEdoc2}')"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 原收文流程 -->
                            <c:if test='${haveTurnSendEdoc1 != null}'>
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.sendGrid.oldRecEdoc')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="haveTurnSendEdoc1"
                                              onclick="showDetail('${haveTurnSendEdoc1}');"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.oldRecEdoc')}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 转办信息(转办信息默认放在外面，可以直接点击查看) -->
                            <c:if test='${haveTurnRecEdoc != null}'>
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.sendGrid.turnRecEdoc')} </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="turnRecEdocInfoDetails"
                                              onclick="showTurnRecEdocInfo('${haveTurnRecEdoc}')"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}</span>
                                    </td>
                                </tr>
                            </c:if>
                            <!-- 转发文流程 -->
                            <c:if test='${haveTurnSendEdoc2 != null}'>
                                <tr>
                                    <td>
                                        <div class="edocRightContent3h3">${ctp:i18n('govdoc.sendGrid.turnSendEdoc')}</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td><span class="hand left set_color_gray" id="haveTurnSendEdoc1"
                                              onclick="showTurnSendEdocInfo()"><span
                                            class="ico16 view_log_16 margin_lr_5"
                                            title="${ctp:i18n('govdoc.sendGrid.turnRecEdoc')}"></span>${ctp:i18n('govdoc.sendGrid.turnSendEdoc')}</span>
                                    </td>
                                </tr>
                            </c:if>
                        </table>
                    </div><!-- end edocRightContent -->

                        <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-流程(显示) --%>
                    <div id="workflowContent" class="edocRightContent w100b h100b">
                        <c:if test="${!onlySeeContent}">
                            <!-- 流程最大化 -->
                            <span style="padding-right:15px;padding-top:5px;" class="hand right set_color_gray"
                                  id="processMaxFlag" title="${ctp:i18n('collaboration.summary.flowMax')}">
					<span class="ico16 process_max_16 margin_lr_5"></span>${ctp:i18n('collaboration.summary.flowMax')}
				</span>
                            <!-- 明细日志 -->
                            <c:if test='${!isFromTraceFlag}'>
					<span class="hand right set_color_gray" style="padding-top:5px" id="showDetailLog"
                          title="${ctp:i18n('collaboration.sendGrid.findAllLog')}">
						<span class="ico16 view_log_16 margin_lr_5"></span>${ctp:i18n('collaboration.common.flag.showDetailLog')}
					</span>
                            </c:if>
                            <!-- 修改流程 -->
                            <div class="padding_l_25 padding_t_5 hidden" id="show_edit_workFlow">
                                <a id="edit_workFlow"
                                   class="common_button common_button_gray">${ctp:i18n('common.detail.label.editWf')}</a>
                            </div>
                            <!-- end show_edit_workFlow -->

                            <!-- 流程iframe -->
                            <iframe id="iframeright" class="bg_color_white" src="about:blank" width="100%" height="100%"
                                    frameborder="0" scrolling="no"></iframe>
                        </c:if>
                    </div><!-- end workflowContent -->

                        <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-处理意见(展现) --%>
                    <div id="attributeContent" class="edocRightContent w100b h100b">
                        <iframe id="attributeInfoDiv" name="attributeInfoDiv" width="100%" height="95%" frameborder="0"
                                src='' style=''></iframe>
                    </div><!-- end attributeContent -->

                </div><!-- end edocContainerRightContent -->

                    <%-- 一屏式布局A-查看界面-中部展示区域-右侧文单区域-页签 --%>
                <table class="edocNav" id="edocNav" style="position: fixed;top: 100px;">
                    <tr data-id="formContent" class="edocNavSelect" id="formContentTr">
                        <td>
                            <div id="form_view_li">${ctp:i18n('edoc.doctemplate.wendan')}</div>
                        </td>
                    </tr>
                    <c:if test="${not empty summaryVO.aipFileId}">
                        <tr data-id="signContent" id="qwqpTr">
                            <td>
                                <div id="qwqp_view_li">${ctp:i18n('edoc.aip.label')}</div>
                            </td>
                        </tr>
                    </c:if>
                    <tr data-id="commentContent">
                        <td>
                            <div>${ctp:i18n('edoc.element.otherOpinion')}</div>
                        </td>
                    </tr>
                    <tr data-id="attContent">
                        <td>
                            <div>${ctp:i18n('govdoc.menu.attachmentsAndInformation')}<span
                                    id="attNum">(${attNum })</span></div>
                        </td>
                    </tr>
                    <tr data-id="workflowContent" id="workflow_view">
                        <td>
                            <div id="workflow_view_li">${ctp:i18n('common.workflow.label')}</div>
                        </td>
                    </tr>
                    <!-- <tr data-id="edocRightContent5"><td><div onclick="changeToDetailLog();">明细日志</div></td></tr> -->
                    <tr data-id="attributeContent">
                        <td>
                            <div>${ctp:i18n('edoc.Attribute.state')}</div>
                        </td>
                    </tr>
                    <!-- <tr data-id="edocRightContent6"><td><div>督办事项</div></td></tr> -->
                </table><!-- end edocNav -->

                    <%--<c:if test="${hasDealArea}">--%>
                    <%--<%@ include file="govdoc_deal_top.jsp" %>--%>
                    <%--</c:if><!-- end hasDealArea -->--%>
                    <%--<c:if test="${!hasDealArea}">--%>
                    <%--<style>--%>
                    <%--.edocContainerRight{--%>
                    <%--padding: 10px 39px 3px 10px;--%>
                    <%--}--%>
                    <%--</style>--%>
                    <%--</c:if>--%>

            </div>
            <!-- end edocContainerRight -->


            <%-- 跟踪区域开始 --%>
            <input type="hidden" id="zdgzry" name="zdgzry"/>
            <input type="hidden" name="can_Track" id="can_Track" value="${summary.canTrack ? 1 : 0}"/>
            <input type="hidden" name="trackType" id="trackType" value="${trackType}"/>
            <div id="htmlID" class="hidden">
                <div class="padding_tb_10 padding_l_10">
                    <input type="text" style="display: none" id="zdgzryName" name="zdgzryName" size="30"
                           onclick="$('radio4').click()"/>
                    <span class="valign_m">${ctp:i18n('collaboration.forward.page.label4')}:</span><!-- 跟踪 -->
                    <select id="gz" class="valign_m">
                        <option value="1">${ctp:i18n('common.yes')}</option><!-- 是 -->
                        <option value="0">${ctp:i18n('common.no')}</option><!-- 否 -->
                    </select>
                    <div id="gz_ren" class="common_radio_box clearfix margin_t_10">
                        <label for="radio1" class="margin_r_10 hand">
                            <input type="radio" value="0" id="radio1" name="option"
                                   class="radio_com">${ctp:i18n('common.pending.all')}</label><!-- 全部 -->
                        <label for="radio4" class="margin_r_10 hand">
                            <input type="radio" value="0" id="radio4" name="option"
                                   class="radio_com">${ctp:i18n('col.track.part')}</label><!-- 指定人 -->
                    </div><!-- end gz_ren -->
                </div>
            </div>
            <!-- end htmlID -->
            <%-- 跟踪区域结束 --%>

        </c:otherwise>
    </c:choose><!-- end !onlySeeContent -->

</div>
<!-- edocContainer -->

<%-- 处理界面参数，用于提交到后端，放到layout中 start --%>
<input type="hidden" name="secretLevel" id="secretLevel" value="${summaryVO.summary.secretLevel}"/>
<input type="hidden" id="affairId" value="${summaryVO.affairId}">
<div id="colSummaryData" class="newinfo_area title_view">
    <%@ include file="summary_form.jsp" %>
</div>
<%@ include file="../govdoc_mark_deal.jsp" %>
<jsp:include page="/WEB-INF/jsp/common/content/workflow.jsp"/>
<%-- 处理界面参数，用于提交到后端，放到layout中 end --%>

<script>


</script>

<!-- 一屏式前端需要用到的js -->
<script type="text/javascript" charset="UTF-8" src="${path}/common/js/jquery-ui.custom.js${ctp:resSuffix()}"></script>
<script>
    commonActionNodeCount = '${commonActionNodeCount}';
    //zhou start
    function tocreateMeeting() {
        createMeeting(affairId, openFrom, getCtpTop().frames['main'], true);
    }
    function toZzhixin(){
        if (isChinaOs()) {
            $.alert("当前系统暂不支持该功能！");
        } else {
            // 写死都不能转发
            wakeZX('4', summaryId, hasForward ? 1 : 0);
        }
    }
    //zhou end
    //从default_govdoc.jsp抓来的变量
    $(document).ready(function () {
        function changeEdocContainerLeftWidth() {
            var edocContainerLeft = $('.edocContainerLeft');
            if (edocContainerLeft.width() < 800) {
                edocContainerLeft.css('width', '790px');
                $('.edocContainerRight').css('width', $('body').width() - 800 + 'px');
                $('#attachmentList').css('width', '515px');

            } else {
                edocContainerLeft.css('width', '50%');
                $('.edocContainerRight').css('width', '50%');
                $('#attachmentList').css('width', '650px');

            }
        }

        changeEdocContainerLeftWidth();
        $(window).on('resize', function () {
            changeEdocContainerLeftWidth();
        });
        //签收更多
        if ($("#edocHandleContentDiv1More")) {
            var _moreMenu = [];
            if (((paramOpenFrom == 'listSent')
                || (paramOpenFrom == 'listDone')
                || (paramOpenFrom == 'listPending')
                || (paramOpenFrom == 'supervise')) && (!isFromTraceFlag) && (isHistoryFlag != 'true')) {
                //创建会议
                if ($.ctx.resources.contains('F09_meetingArrange')) {
                    _moreMenu.push({
                        id: "createMeeting",
                        name: $.i18n('common.new.meeting.label'),
                        className: "ico16",
                        customAttr: "class='nodePerm'",
                        handle: function () {
                            createMeeting(affairId, openFrom, getCtpTop().frames['main'], true);
                        }
                    });
                    //zhou
                    let html = "<span class=\"common_button\" style='cursor: pointer;' onclick='tocreateMeeting()'>" + $.i18n('common.new.meeting.label') + "</span>";
                    $("#gnBtn").append(html);
                }
                //置信
                if (typeof (canShare) != "undefined" && canShare) {
                    _moreMenu.push({
                        name: $.i18n("common.collaboration.label.zhixinCommunication"), // 致信交流
                        className: "ico16",
                        img: "syIcon sy-m3-zhixin-fill",
                        handle: function (json) {
                            if (isChinaOs()) {
                                $.alert("当前系统暂不支持该功能！");
                            } else {
                                // 写死都不能转发
                                wakeZX('4', summaryId, hasForward ? 1 : 0);
                            }
                        }
                    });
                    //zhou
                    let html = "<span class=\"common_button\" style='cursor: pointer;' onclick='toZzhixin()'>" + $.i18n("common.collaboration.label.zhixinCommunication") + "</span>";
                    $("#gnBtn").append(html);
                }
            }
            //督办设置
            if (paramOpenFrom == 'listSent' && isHistoryFlag != 'true' && (switchObj.chuantou == null || switchObj.chuantou == '' || switchObj.chuantou == "false")) {
                _moreMenu.push({
                    id: "showSuperviseSettingWindow",
                    name: $.i18n('common.toolbar.supervise.label'),
                    className: "setting_16",
                    customAttr: "class='nodePerm'",
                    handle: superviseSettingFunc
                });
                //zhou
                let html = "<span class=\"common_button\" style='cursor: pointer;' onclick='superviseSettingFunc()'>" + $.i18n('common.toolbar.supervise.label') + "</span>";
                $("#gnBtn").append(html);
            } else if (((paramOpenFrom == 'listDone' && isCurrentUserSupervisor) || (paramOpenFrom == 'supervise')) && isHistoryFlag != 'true') {//督办
                _moreMenu.push({
                    id: "showSuperviseWindow",
                    name: $.i18n('common.node.supervise'),
                    className: "meeting_look_1",
                    customAttr: "class='nodePerm'",
                    handle: superviseFunc
                });
                //zhou
                let html = "<span class=\"common_button\" style='cursor: pointer;' onclick='superviseFunc()'>" + $.i18n('common.node.supervise') + "</span>";
                $("#gnBtn").append(html);
            }
            //追溯流程
            if (${summaryVO.showTraceWrokflowBtn}) {
                _moreMenu.push({
                    id: "showWorkflowtrace",
                    name: $.i18n('collaboration.workflow.label.lczs'),
                    className: "review_flow_16",
                    customAttr: "class='nodePerm'",
                    handle: showOrCloseWorkflowTrace
                });
                //zhou
                let html = "<span class=\"common_button\" style='cursor: pointer;' onclick='showOrCloseWorkflowTrace()'>" + $.i18n('collaboration.workflow.label.lczs') + "</span>";
                $("#gnBtn").append(html);
            }
            //跟踪
            if ((paramOpenFrom == 'listSent' || paramOpenFrom == 'listDone') && isHistoryFlag != 'true' && (switchObj.chuantou == null || switchObj.chuantou == '')) {
                _moreMenu.push({
                    id: "gzbutton",
                    name: $.i18n('collaboration.forward.page.label4'),
                    className: "track_16",
                    customAttr: "class='nodePerm'",
                    handle: setTrack
                });
                //zhou
                let html = "<span class=\"common_button\" style='cursor: pointer;' onclick='setTrack()'>" + $.i18n('collaboration.forward.page.label4') + "</span>";
                $("#gnBtn").append(html);
            }
            if (_moreMenu.length > 0) {
                $("#edocHandleContentDiv1More").menuSimple({//一屏式布局A-新建会议等更多
                    id: "handleMoreMenu",
                    width: 100,
                    direction: "BR",
                    offsetLeft: -20,
                    data: _moreMenu
                });
                $("#edocHandleContentDiv1More").click(function () {
                    stopHideOfficeObj(1);//正文(0) 标准文单(2) 全文签批单(1)
                });
            } else {
                $("#_dealSubmit").addClass("dealSubmit");
                $("#edocHandleContentDiv1More").hide();
            }

        }

        //签收收起
        if ($('#edocHandleHeadicon2')) {
            $('#edocHandleHeadicon2').on('click', function () {
                $('#east').css('height', 'auto');
                $('#edocHandleHeadicon3').show();
                $(this).hide();
            });
        }
        //签收展开
        if ($('#edocHandleHeadicon3')) {
            $('#edocHandleHeadicon3').on('click', function () {
                $('#east').css('height', '40px');
                $('#edocHandleHeadicon2').show();
                $(this).hide();
            });
        }

        //收藏
        if ($('#edocContainerFooterStored')) {
            $('#edocContainerFooterStored').on('click', function () {
                var html = '<em class="ico16 unstore_16"></em> ' + $.i18n('common.collection.label');
                var target = $(this);
                if (target.find('.unstore_16').length >= 1) {
                    html = '<em class="ico16 stored_16"></em> ' + $.i18n('common.cancel.collection.label');
                    favorite(4, affairId, hasAttsFlag, 3);
                    //收藏
                } else {
                    var offsetWidth = document.body.offsetWidth;
                    var offsetHeight = document.body.offsetHeight;
                    if (newGovdocView == '2') {
                        offsetHeight = 260;
                    }
                    cancelFavorite(4, affairId, hasAttsFlag, 3, '', offsetHeight - 145, offsetWidth / 4);
                    //取消收藏
                }
                target.html(html);
            });
        }
        if ($('#footerDivSwitch')) {
            $('#footerDivSwitch').on('click', function () {
                var html = $.i18n("govdoc.edoc.expand"), dom = $(this), target = $('#footerDiv');
                if (target.is(':hidden')) {
                    html = $.i18n("govdoc.edoc.expand2");
                    target.show();
                } else {
                    target.hide();
                }
                dom.html(html);
            });
        }
    });

    //操作系统
    function isChinaOs() {
        var isChina = false;
        var ua = navigator.userAgent;
        var isMSIE = (navigator.appName == "Microsoft Internet Explorer") || ua.indexOf('Trident') != -1;
        if (!isMSIE) {
            isChina = (String(navigator.platform).indexOf("Linux") > -1);
        }

        return isChina;
    }
</script>