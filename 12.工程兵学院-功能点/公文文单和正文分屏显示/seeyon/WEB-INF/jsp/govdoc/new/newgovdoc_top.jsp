<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div id="colMainData" class="form_area">

    <%@ include file="newgovdoc_form.jsp"%><!-- 一屏式布局A/B-拟文界面-隐藏控件用于提交数据到后端 -->

    <div class="edocHead clearfix" id="toolbar"><!-- 一屏式布局A/B-拟文界面-操作按钮/页面数据填写区域 -->
        <div class="left edocHeadLeft" <c:if test="${isQuickSend}">style="min-width:50%"</c:if>>
            <%-- 公文单 --%>
            <span>${ctp:i18n('edocTable.label')}:</span>
            <select
                    <c:if test="${((vobj.noselfflow eq 'noselfflow') || (vobj.fromSystemTemplete)) && vobj.isQuickSend ne 'true'}">disabled="disabled" </c:if>
                    class="edocHeadSelect1" id="edocCategoryList" onchange="changeGovdocFormList(this)">
                <c:forEach items="${vobj.edocCategoryList }" var="item" varStatus="status">
                    <option value="${item.id }"<c:if
                            test="${item.id == vobj.defaultCategoryId}"> selected="selected"</c:if>>${item.name }</option>
                </c:forEach>
            </select>
            <select
                    <c:if test="${((vobj.noselfflow eq 'noselfflow') || (vobj.fromSystemTemplete)) && vobj.isQuickSend ne 'true'}">disabled="disabled" </c:if>
                    class="edocHeadSelect4" onchange="changeGovdocFormIframe(this)" id="formList">
            </select>

            <%-- 调用模板 --%>
            <c:if test="${!isQuickSend && isSpecialSteped != true}"> <!-- 不是指定回退且不是快速拟文 -->
                <a id="refresh2" href="javascript:void(0)" class="common_button" onClick="handleTemplate()">
                    <em class="ico16 call_template_16" id="refresh2_em"></em>
                    <span title="${ctp:i18n('govdoc.call.template')}" class="menu_span" id="refresh2_span"
                          style="vertical-align: middle;">${ctp:i18n('common.toolbar.templete.label')}</span>
                </a>
            </c:if>

            <%-- 流程 --%>
            <c:if test="${!isQuickSend && vobj.customDealWith ne 'true'}">
                <span class="margin_l_20">${ctp:i18n('common.workflow.label')}：</span>
                <div id="process_info_div" style="display: inline-block;line-height:25px;">
                    <input readonly="readonly" type="text" class="edocHeadSelectInput"
                           id="process_info" name="process_info" app="4"
                           defaultValue="${ctp:i18n('collaboration.default.workflowInfo.value')}"
                           value="<c:out value='${vobj.contentContext.workflowNodesInfo}'></c:out>"
                           title="${ctp:i18n('collaboration.newcoll.clickforprocess')}"
                           <c:if test="${(vobj.subState eq '16') || (vobj.noselfflow eq 'noselfflow') || (vobj.fromSystemTemplete)}">disabled="disabled" </c:if>/>
                </div>

                <div id="workflowInfo" style="display: inline-block;">
                    <c:if test="${(vobj.subState eq '16') || (vobj.noselfflow eq 'noselfflow') || (vobj.fromSystemTemplete)}">
                        <c:set value="${ vobj.contentContext.wfCaseId==null ? '-1' : vobj.contentContext.wfCaseId }"
                               var="caseId"/>
                        <c:set value="${ vobj.contentContext.wfProcessId==null ? '-1' : vobj.contentContext.wfProcessId }"
                               var="workflowId"/>
                        <c:set value="${vobj.systemTemplate && vobj.subState ne '16' ? true : false}"
                               var="systemTemplateValue"/>
                        <c:if test="${vobj.systemTemplate && vobj.subState ne '16'}">
                            <c:set value="${vobj.contentContext.processTemplateId}" var="workflowId"/>
                        </c:if>

                        <a style="border-radius:0; background:#FFF;"
                           comp="type:'workflowEdit',defaultPolicyId:'${curPerm.defaultPolicyId}',defaultPolicyName:'${curPerm.defaultPolicyName}',moduleType:'edoc',isTemplate:${systemTemplateValue},isView:true,workflowId:'${workflowId}',caseId:'${caseId }'"
                           class="common_button common_button_icon comp edit_flow" href="#">
                            <em class="ico16 process_16"> </em>${ctp:i18n('collaboration.newColl.findFlow')}
                        </a><!-- 查看流程 -->
                    </c:if>
                    <c:if test="${vobj.subState ne '16'}">
                        <c:if test="${(vobj.noselfflow ne 'noselfflow') && (!vobj.fromSystemTemplete)}">
                            <a id="workflow_btn" onclick="workflow_edit()"
                               style="border-radius:0; background:#FFF;height:24px;line-height:24px;color:#333;"
                               class="common_button common_button_icon comp edit_flow" href="#">
                                <em class="ico16 process_16"> </em>${ctp:i18n('common.design.workflow.label')}
                            </a><!-- 编辑流程 -->
                        </c:if>
                    </c:if>
                </div>
            </c:if>

            <%-- 续办区域 --%>
            <c:if test="${vobj.customDealWith eq 'true' && !isQuickSend}">
                <div class="margin_l_20" id="xuban_div" style="display: inline-block;">
			<span>
				${ctp:i18n('govdoc.customDealWith.member.label')}<!-- 续办人员: -->
			</span>

                    <select class="edocHeadSelect3" id="permissionRange" onchange="permissionChange(this);"
                            <c:if test="${(vobj.subState eq '16') || (vobj.noselfflow eq 'noselfflow') || (vobj.fromSystemTemplete)}">disabled="disabled" </c:if>>
                        <option value="0"
                                selected="selected">${ctp:i18n('govdoc.customDealWith.pleaseChooseMode')}</option>
                        <c:forEach items="${vobj.permissions }" var="permission" varStatus="status">
                            <option
                                    <c:if test="${vobj.customDealWithPermission eq permission.name }">selected='selected'</c:if>
                                    <c:if test="${status.index lt vobj.returnPermissionsLength }">return='true'</c:if>
                                    value="${permission.name }" title="${permission.label}">${permission.label}
                            </option>
                        </c:forEach>
                    </select>

                    <select class="edocHeadSelect" id="memberRange" onchange="memberRangeChange(this);"
                            <c:if test="${(vobj.subState eq '16') || (vobj.noselfflow eq 'noselfflow') || (vobj.fromSystemTemplete)}">disabled="disabled" </c:if>>
                        <option value="0"
                                selected="selected">${ctp:i18n('govdoc.customDealWith.pleaseChooseMember')}</option>
                        <c:forEach items="${vobj.members }" var="member">
                            <option
                                    <c:if test="${vobj.customDealWithMemberId eq member.id }">selected='selected'</c:if>
                                    userId="${member.id }" userName="${member.name }" type="${member.type }"
                                    accountId="${member.orgAccountId }" value="${member.id }">${member.name}</option>
                        </c:forEach>
                        <option value="-1">${ctp:i18n('govdoc.customDealWith.more')}</option>
                    </select>
                </div>
            </c:if>
            <!--end(续办区域) -->

            <%-- 正文套红 --%>
            <c:if test="${isQuickSend}">
                <span>${ctp:i18n('edoc.action.form.template')}:</span>
                <select id="fileUrl" name="fileUrl" class="edocHeadSelect2" style=""
                        onchange="doTaohongQuick('govdoc')">
                    <option value="">${ctp:i18n('templete.select_template.label')}</option>
                    <c:forEach items="${vobj.bodyVo.taohongList}" var="taohong">
                        <c:if test="${taohong.status eq 1}">
                            <c:set var="taohongurl" value="${taohong.fileUrl}&${taohong.textType}"/>
                            <option value="${taohong.fileUrl}&${taohong.textType}">${taohong.name}</option>
                        </c:if>
                    </c:forEach>
                </select>
            </c:if>
            <c:if test="${ctp:hasPlugin('doc')}">
                <%-- 预归档 --%>
                <span class="margin_l_20 edocHeadSpan1">${ctp:i18n('collaboration.prep-pigeonhole.label')}：</span>
                <input type="hidden" name="isTemplateHasPigeonholePath" id="isTemplateHasPigeonholePath"
                       value="${isPrePighole}"/>
                <select id="colPigeonhole" title="${ctp:toHTML(vobj.archiveAllName)}" class="edocHeadSelect3"
                        onchange="pigeonholeEvent(this)" ${!canArchive|| isPrePighole || setDisabled ? 'disabled=disabled' : "" } >
                    <option id="defaultOption" value="1">${ctp:i18n('common.default')}</option>
                    <!-- 请选择 -->
                    <option id="modifyOption" value="2">${ctp:i18n('common.pleaseSelect.label')}</option>
                    <c:if test="${vobj.archiveName ne null && vobj.archiveName ne ''}">
                        <option value="3" selected>${ctp:toHTML(vobj.archiveName)}</option>
                    </c:if>
                </select>
            </c:if>
            <%-- 存为模板 --%>
            <c:if test="${!isQuickSend}">
                <a id="refresh2" href="javascript:void(0)" class="common_button" onClick="saveAsTemplete()">
                    <em id="refresh1_em" class="ico16 save_template_16"></em>
                    <span id="refresh1_span" title="${ctp:i18n('govdoc.templete.saveAS')}"
                          style="vertical-align: middle;">${ctp:i18n('common.toolbar.saveAs.label.rep')}</span>
                </a>
            </c:if>

            <%-- 打印 --%>
            <c:if test="${!isQuickSend && canPrint}">
                <a id="print" href="javascript:void(0)" class="common_button" onclick="newDoPrint('newGovdoc')">
                    <em id="print_em" class="ico16 print_16"></em>
                    <span id="print_span" title="${ctp:i18n('govdoc.print')}"
                          style="vertical-align: middle;">${ctp:i18n('edoc.element.print')}</span>
                </a>
            </c:if>

            <c:if test="${vobj.subApp == '2'}">
                <c:if test="${ctp:hasPlugin('barCode') }">
                    <a id="scan_barcode" href="javascript:void(0)" class="common_button"
                       onclick="loadDataByBarcode()">${ctp:i18n('govdoc.scanbarcode.text')}</a>
                </c:if>

                <c:if test="${hasStepFenBan }"><!-- 是否分办 -->
                    <a id="stepback" href="javascript:void(0)" class="common_button"
                       onclick="relDeal()">${ctp:i18n('govdoc.rollback.fenban')}</a>
                </c:if>
            </c:if>

        </div><!-- end left -->

        <div class="right">
            <%-- 发送 --%>
            <a id="sendId" href="javascript:void(0)" class="common_button common_button_emphasize"
               onClick="send()">${ctp:i18n('common.toolbar.send.label')}</a>

            <%-- 保存待发 --%>
            <a id="insert" href="javascript:void(0)" class="common_button"
               onClick="saveDraft()">${ctp:i18n('common.toolbar.savesend.label')}</a>
        </div><!-- end right -->

    </div><!-- end edocHead -->

    <div class="edocContainer">
        <!-- 一屏式布局A/B-拟文界面-左侧正文区域 -->
        <div class="right edocContainerLeft edocBorderbox" style="padding-top: 50px;">
            <!-- 一屏式布局A/B-拟文界面-右侧页签区域 -->
            <table class="edocNav" id="edocNav">
                <tr data-id="edocRightContent1" class="edocNavSelect">
                    <td>
                        <div>${ctp:i18n('govdoc.document.form')}</div>
                    </td>
                </tr>
                <tr data-id="edocRightContent3" style="display:${ !isQuickSend ? '' : 'none' }">
                    <td>
                        <div>${ctp:i18n('govdoc.process.settings')}</div>
                    </td>
                </tr>
                <c:choose>
                    <c:when test="${(!canUploadRel && !canUploadAttachment) || (isQuickSend && !canUploadAttachment)}">
                        <!-- 不是快速发文，没有关联文档，并且没有上传附件 -->
                        <c:if test="${vobj.atts != null && fn:length(vobj.atts) gt 0}">
                            <tr data-id="edocRightContent2">
                                <td>
                                    <div>${ctp:i18n('govdoc.attachment.information')}</div>
                                </td>
                            </tr>
                        </c:if>
                    </c:when>
                    <c:when test="${!isQuickSend && canUploadRel && !canUploadAttachment }">
                        <!-- 不是快速发文，只有关联文档 -->
                        <tr data-id="edocRightContent2">
                            <td>
                                <div>${ctp:i18n('govdoc.sender.postscript.correlationDocument')}</div>
                            </td>
                        </tr>
                    </c:when>
                    <c:when test="${!isQuickSend && !canUploadRel && canUploadAttachment }">
                        <!-- 不是快速发文，只有上传附件 -->
                        <tr data-id="edocRightContent2">
                            <td>
                                <div>${ctp:i18n('govdoc.upload.attachment')}</div>
                            </td>
                        </tr>
                    </c:when>
                    <c:when test="${!isQuickSend && canUploadRel && canUploadAttachment }">
                        <!-- 不是快速发文，有上传附件也有关联文档 -->
                        <tr data-id="edocRightContent2">
                            <td>
                                <div>${ctp:i18n('govdoc.upload.attachment')}<span
                                        class="edocNavSpan">\</span>${ctp:i18n('govdoc.sender.postscript.correlationDocument')}
                                </div>
                            </td>
                        </tr>
                    </c:when>
                    <c:when test="${isQuickSend && canUploadAttachment }">
                        <!-- 是快速发文，有上传附件也有关联文档 -->
                        <tr data-id="edocRightContent2">
                            <td>
                                <div>${ctp:i18n('govdoc.upload.attachment')}</div>
                            </td>
                        </tr>
                    </c:when>
                </c:choose>
                <tr data-id="edocRightContent6">
                    <td>
                        <div>${ctp:i18n('govdoc.postscript')}</div>
                    </td>
                </tr>
            </table><!-- edocNav -->

            <!-- 一屏式布局A/B-拟文界面-正文选择区域 -->
                <div class="edocContainerLeftHead" style="position: absolute;top: 0px;right: 50px;">
                    <select name="govdocTypeChoose" id="govdocTypeChoose"></select>
                    <span id="uploadGovdocBody"><em
                            class="ico16 upload_files_16"></em>${ctp:i18n('govdoc.upload.text')}</span>
                    <div id="attachmentAreaContentType" style="display:none">
                        <div style="display:none" id="uploadBody10" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody10',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                        <div style="display:none" id="uploadBody41" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody41',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                        <div style="display:none" id="uploadBody42" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody42',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                        <div style="display:none" id="uploadBody43" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody43',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                        <div style="display:none" id="uploadBody44" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody44',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                        <div style="display:none" id="uploadBody45" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody45',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                        <div style="display:none" id="uploadBody46" class="comp"
                             comp="type:'fileupload',attachmentTrId:'uploadBody46',applicationCategory:'4',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_ContentType',delCallMethod:'insertAtt_ContentType',takeOver:false,noMaxheight:true,quantity:1"></div>
                    </div>
                </div><!-- end edocContainerLeftHead -->

                <!-- 一屏式布局A/B-拟文界面-正文展示区域 -->
            <%--            zhou--%>
            <div class="edocContainerLeftContent" style="height:100%;margin-right: 50px;">
                    <%--公文正文位置 --%>
                    <%@ include file="/WEB-INF/jsp/govdoc/govdocBody.jsp" %>

                    <!-- html正文修改iframe -->
                    <c:if test="${vobj.bodyVo.bodyType eq 'HTML' and isFenBan}">
                        <c:set var="summaryId" value="${ vobj.summary.id eq null ? vobj.signSummaryId : vobj.summary.id }"/>
                        <iframe id="htmlBodyIframe" name="htmlBodyIframe" width="100%" height="100%" frameborder="0"
                                scrolling="no" marginheight="0" marginwidth="0"
                                src='${path }/govdoc/govdoc.do?method=htmlBody&summaryId=${summaryId}&affairId=${affairId}'
                                style="display: none;">
                        </iframe>
                    </c:if>
                </div>

<%--            </div>--%>

        </div><!-- end edocContainerLeft -->

        <!-- 一屏式布局A/B-拟文界面-右侧区域 -->
        <div class="left edocContainerRight edocBorderbox h100b">


            <div class="edocContainerRightContent h100b" id="edocContainerRightContent">
                <!-- 一屏式布局A/B-拟文界面-右侧文单展示区域 -->
                <div id="edocRightContent1" class="edocRightContent display_block w100b h100b">
                    <iframe id='govDocZwIframe' name='govDocZwIframe'
                            style="border: 0; width: 100%; height: 100%; display: block;" frameborder="0"
                            marginheight="0" marginwidth="0" src="" onload="_contentSetText()"></iframe>
                </div>

                <!-- 一屏式布局A/B-拟文界面-右侧流程设置区域 -->
                <div id="edocRightContent3" class="edocRightContent" style="display:${ !isQuickSend ? '' : 'none' }">
                    <%@ include file="newgovdoc_top_wf_detail.jsp" %>
                </div><!-- end edocRightContent3 -->

                <!-- 一屏式布局A/B-拟文界面-右侧上传附件\关联文档展示区域 -->
                <div id="edocRightContent2" class="edocRightContent">
                    <div style="height:33px;overflow:hidden;display:inline-block; padding-left:20px; margin-top:10px;margin-bottom:5px;">
                        <c:if test="${canUploadAttachment }">
                            <%-- 上传附件 --%>
                            <!-- <a href="javascript:void(0)" class="common_button common_button_icon" onclick="insertAttachmentPoi('Att')"><em class="ico16 affix_16"></em>${ctp:i18n('common.upload.attachment.label')}</a> -->
                            <span id="upload" style="display:inline-block;"></span>
                        </c:if>

                        <c:if test="${!isQuickSend && canUploadRel}">
                            <%-- 关联文档 --%>
                            <a href="javascript:void(0)" class="common_button common_button_icon"
                               onclick="quoteDocument('Doc1')"><em
                                    class="ico16 associated_document_16"></em>${ctp:i18n('common.toolbar.insert.mydocument.label')}
                            </a>
                        </c:if>
                    </div>

                    <div class="attachmentArea" style="max-height:500px;">
                        <div id="attachmentTRAtt" style="display:none; margin-top:4px; padding-left:20px;">
                            <table border="0" cellspacing="0" cellpadding="0" width="100%" class="line_height180">
                                <tr id="attList">
                                    <td class="align_left" valign="top" style="width: 20px;">
                                        <div class="div-float margin_t_5">
                                            <em class="syIcon sy-attachment" style="color:#1F85EC;"></em>
                                        </div>
                                    </td>
                                    <td valign="top" width="30" nowrap="nowrap">
                                        <div class="div-float margin_t_5 margin_r_5">(<span
                                                id="attachmentNumberDivAtt"></span>)
                                        </div>
                                    </td>
                                    <td class="align_left">
                                        <div id="attFileDomain" class="comp"
                                             comp="type:'fileupload',attachmentTrId:'Att',applicationCategory:'1',canFavourite:false,canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_AttCallback',delCallMethod:'insertAtt_AttCallback',takeOver:false,noMaxheight:true"
                                             attsdata='${vobj.attListJSON }'></div>
                                    </td>
                                </tr>
                            </table>
                        </div><!-- end attachmentTRAtt -->
                        <div id="attachment2TRDoc1" style="display:none; margin-top:4px; margin-left:20px;">
                            <table border="0" cellspacing="0" cellpadding="0" width="100%" class="line_height180">
                                <tr id="docList">
                                    <td class="align_left" valign="top" style="width: 20px;">
                                        <div class="margin_t_5"><span style="color:#1F85EC;"
                                                                      class="syIcon sy-associated_document"></span>
                                        </div>
                                    </td>
                                    <td valign="top" width="30" nowrap="nowrap">
                                        <div class="div-float margin_t_5 margin_r_5">(<span
                                                id="attachment2NumberDivDoc1"></span>)
                                        </div>
                                    </td>
                                    <td class="align_left">
                                        <div class="comp" id="assDocDomain"
                                             comp="type:'assdoc',attachmentTrId:'Doc1',modids:'1,3',applicationCategory:'1',referenceId:'${vobj.summary.id}',canDeleteOriginalAtts:true,originalAttsNeedClone:${empty vobj.cloneOriginalAtts?false:vobj.cloneOriginalAtts},callMethod:'insertAtt_AttCallback',delCallMethod:'insertAtt_AttCallback',noMaxheight:true"
                                             attsdata='${vobj.attListJSON }'></div>
                                    </td>
                                </tr>
                            </table>
                        </div><!-- end attachment2TRDoc1 -->
                    </div><!-- attachmentArea -->
                    <!-- 原收文流程显示区域 -->
                    <c:if test="${vobj.govdocRelation1=='true' }">
                        <table border="0" cellspacing="0" cellpadding="0" width="100%" style="margin-top: 10px;">
                            <tr>
                                <td style="padding-left:5px;text-align:left" colspan="2">
                                    <span class="hand" onclick="showDetail('${vobj.forwardSummaryId}');"><span
                                            class="ico16 view_log_16 margin_lr_5"></span>${ctp:i18n('govdoc.sendGrid.oldRecEdoc')}</span>
                                </td>
                            </tr>
                        </table>
                    </c:if>
                </div><!-- end edocRightContent2 -->

                <!-- 一屏式布局A/B-拟文界面-右侧附言区域 -->
                <div id="edocRightContent6" class="edocRightContent">
                    <div id="comment_deal" style="width:100%; text-align:center;">
                        <input type="hidden" id="moduleType" value="${vobj.contentContext.moduleType}">
                        <input type="hidden" id="moduleId"
                               value="${ !empty vobj.contentContext.moduleId ? vobj.contentContext.moduleId : vobj.summaryId }">
                        <input type="hidden" id="ctype" value="-1">
                        <table class="h100b" style="width:90%;margin-top:20px;margin-left:20px;" cellpadding="0"
                               cellspacing="0" border="0">
                            <tr id="fuyan_area">
                                <td id="fuyan" valign="top" align="center" class="editadt_box padding_l_5"><textarea
                                        style="width: 100%; padding: 0 5px; font-size: 12px; min-height: 115px;"
                                        class="h100b" id="content_coll" name="content_coll"
                                        onclick="checkContent();" onblur="checkContentOut();"><c:forEach
                                        items="${commentSenderList}" var="csl"><c:out
                                        value="${csl.content}"></c:out><c:out value="${__huanhang }"
                                                                              escapeXml="false"></c:out></c:forEach></textarea>
                                </td>
                            </tr>
                        </table>
                    </div><!-- end comment_deal -->
                </div><!-- edocRightContent6 -->

            </div><!-- end edocContainerRightContent -->

        </div><!-- end edocContainerRight -->

    </div><!-- end edocContainer -->

</div>
<!-- end colMainData -->


<script>

    $(document).ready(function () {
        function changeEdocContainerLeftWidth() {
            var edocContainerLeft = $('.edocContainerLeft');
            if (edocContainerLeft.width() < 800) {
                edocContainerLeft.css('width', '790px');
                $('.edocContainerRight').css('width', $('body').width() - 800 + 'px');
            } else {
                edocContainerLeft.css('width', '50%');
                $('.edocContainerRight').css('width', '50%');
            }
        }

        changeEdocContainerLeftWidth();
        $(window).on('resize', function () {
            changeEdocContainerLeftWidth();
        });
        var _module = "twoPanelB";

        var edocNavClickState = false;
        $('#edocNav').on('click', 'tr', function () {
            //右侧导航点击
            $('#edocNav tr').removeClass('edocNavSelect');
            $('#edocContainerRightContent .edocRightContent').hide();
            $('#' + $(this).addClass('edocNavSelect').attr('data-id')).show();
            try {
                $('#govDocZwIframe').css('height', '99%');
                clearTimeout(edocNavClickState);
                edocNavClickState = setTimeout(function () {
                    $('#govDocZwIframe').css('height', '100%');

                });
            } catch (e) {

            }
        });
    });
</script>
	