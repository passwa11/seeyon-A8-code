<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ include file="inquiryHeader.jsp"%>
<html>
	<head>
		<meta charset="utf-8">
		<c:if test="${isEdit}">
			<title>${ctp:i18n('inquiry.editinquiry')}</title><%--i18 发起调查 --%>

		</c:if>
		<c:if test="${!isEdit}">
			<title>${ctp:i18n('inquiry.createinquiry')}</title><%--i18 发起调查 --%>
		</c:if>
		<link rel="stylesheet" type="text/css" href="${path}/apps_res/inquiry/css/inquiryCreate.css${v3x:resSuffix()}" />
		<link rel="stylesheet" type="text/css" href="${path}/skin/dist/modules/inquiry.css${v3x:resSuffix()}" />
		<script src="${path}/apps_res/inquiry/js/common.js${v3x:resSuffix()}"></script>
		<script src="${path}/apps_res/inquiry/js/inquiryCreate.js${v3x:resSuffix()}"></script>
		<script src="${path}/apps_res/inquiry/js/inquiry-QuestionType.js${v3x:resSuffix()}"></script>
		<script type="text/javascript">
			var isDragFlag = false;
			var _path = "${path}";
			var ajax_inquiryManager = new inquiryManager();

			<%-- 集团版块选人组件 --%>
			var isNeedCheckLevelScope_spGroup = false;
			var hiddenPostOfDepartment_spGroup = true;
			<%-- 单位版块选人组件 --%>
			var isNeedCheckLevelScope_spAccount = false;
			var hiddenPostOfDepartment_spAccount = true;
			var onlyLoginAccount_spAccount = true;
			var hiddenOtherMemberOfTeam_spAccount = true;

			<%-- 发起部门选人组件 --%>
			var isNeedCheckLevelScope_spDEPT = false;
			var hiddenPostOfDepartment_spDEPT = true;
			var onlyLoginAccount_spDEPT = true;
			var hiddenOtherMemberOfTeam_spDEPT = true;


			function selectIssueArea(type){
				if(type=="group"){
					selectPeopleFun_spGroup();
				}
				if(type=="account"){
					selectPeopleFun_spAccount();
				}
				if(type=="dept"){
					selectPeopleFun_spDEPT();
				}
				if(type=="custom"){
                  selectPeopleFun_spCustomSpace();
                }
			}
			function setPeopleFields(elements){
				if(!elements){
					return;
				}
				$("#issueArea").val(getIdsString(elements));
				$("#inquiryScope").val(getNamesString(elements));
				hasIssueArea = true;
			}
			function setBeginDept(elements){
				if(!elements){
					return;
				}
				$("#selectBeginDept").val(getIdsString(elements));
				$("#beginDeptName").val(getNamesString(elements));
			}
			var includeElements_spGroup="";
			var elements_spGroup="";
			var includeElements_spAccount="";
			var elements_spAccount="";

			var _isCustom = ${spaceType == '18'||spaceType == '17'||spaceType == '4'};

			var groupOption = "";
			var accountOption = "";
			<c:forEach var="groupInquiryType" items="${groupInquiryTypeList}">
			    groupOption += '<option value="${groupInquiryType.inquirySurveytype.id}" bType="group" isAuth="${groupInquiryType.inquirySurveytype.censorDesc}" title="${ctp:toHTML(groupInquiryType.inquirySurveytype.typeName)}" state="${groupInquiryType.inquirySurveytype.anonymousFlag}">${ctp:toHTML(v3x:getLimitLengthString(groupInquiryType.inquirySurveytype.typeName,24,"..."))}</option>';
            </c:forEach>
            <c:forEach var="accountInquiryType" items="${accountInquiryTypeList}">
                accountOption += '<option value="${accountInquiryType.inquirySurveytype.id}" bType="account" isAuth="${accountInquiryType.inquirySurveytype.censorDesc}" title="${ctp:toHTML(accountInquiryType.inquirySurveytype.typeName)}" state="${accountInquiryType.inquirySurveytype.anonymousFlag}">${ctp:toHTML(v3x:getLimitLengthString(accountInquiryType.inquirySurveytype.typeName,24,"..."))}</option>';
            </c:forEach>
		</script>
	</head>
	<c:if test="${'true' == isEdit}">
	<body onunload="unlock('${inquiryId}')">
	</c:if>
	<c:if test="${'true' != isEdit}">
	<body>
	</c:if>
	<body>
		<input id="inquiryId" type="hidden" value="${inquiryId}">
		<input id="step" type="hidden" value="${step}">
		<input id="isEdit" type="hidden" value="${ctp:toHTML(isEdit)}">
		<input id="inquiryTypeId" type="hidden" value="${ctp:toHTML(inquiryTypeId)}">
		<input id="inquiryTypeOf" type="hidden" value="${inquiryTypeOf}">
		<input id="inquiryState" type="hidden" value="${inquiryState}">
		<input id="spaceType" type="hidden" value="${spaceType}">
		<div class="container">
			<div class="content_container no_header">
				<div class="container_body_top">
					<ul class="top_ul">
						<li class="current">
							<dl>
								<dt class="circle"></dt>
								<dt class="circle_title">${ctp:i18n('inquiry.create.step1')}</dt><%-- i18n 选择创建方式--%>
							</dl>
						</li>
						<li><span class="line"></span></li>
						<li>
							<dl>
								<dd class="circle"></dd>
								<dd class="circle_title">${ctp:i18n('inquiry.create.step2')}</dd><%-- i18n 编辑调查--%>
							</dl>
						</li>
						<li><span class="line"></span></li>
						<li>
							<dl>
								<dd class="circle"></dd>
								<dd class="circle_title">${ctp:i18n('inquiry.create.step3')}</dd><%-- i18n 提交审核/发布--%>
							</dl>
						</li>
					</ul>
					<ul>
						<li><span class="line_long"></span></li>
					</ul>
				</div>
				<div class="container_body_bottom">
					<div id="step_1" class="step_1" style="display:none;">
						<div class="bottom_button">
							<a id="directCreate" href="#this" class="buttonNew current" onclick="directCreate();">${ctp:i18n('inquiry.create.step1.newinquiry')}</a><%-- i18n 直接创建--%>
							<a id="chooseTemp" href="#this" class="buttonNew" onclick="chooseTemp();">${ctp:i18n('inquiry.create.step1.selecttemplate')}</a><%-- i18n 选择模板--%>
							<%--<img src="${path}/skin/dist/images/cultural/inquiry/start_corner.jpg" width="18" height="6" class="start_corner">--%>
						</div>
						<div class="bottom_handle">
							<p class="question_title">${ctp:i18n('inquiry.create.step1.inquiryname')}</p><%-- i18n 问卷标题--%>
							<input id="inquiryName" name="inquiryName" type="text" class="question_input">
							<table id="tempList" style="display: none;" width="576px" height="100%" class="table"></table>
							<a class="min-button right" href="#this" onclick="if(saveAllOptEdit()){goStep(2,1);}">${ctp:i18n('inquiry.create.step1.btnok')}</a><%-- i18n 确定--%>
						</div>
					</div>
					<div id="step_2" class="step_2" style="display:none;">
						<div class="bottom_left">
							<div class="row-fluid bottom_left_content">
								<div class="row1 left_content_list left">
									<ul id="inquiryQuBar" class="align_left">
										<li value="0">
											<span class="ico16 examine_radio"></span>
											${ctp:i18n('inquiry.qutype.radio')}<%--单选题--%>
										</li>
										<li value="1">
											<span class="ico16 examine_checkbox"></span>
											${ctp:i18n('inquiry.qutype.checkbox')}<%--多选题--%>
										</li>
										<li value="4">
											<span class="ico16 examine_imageRadio"></span>
											${ctp:i18n('inquiry.qutype.imgradio')}<%--图片单选题--%>
										</li>
										<li value="5">
											<span class="ico16 examine_imageCheckbox"></span>
											${ctp:i18n('inquiry.qutype.imgcheckbox')}<%--图片多选题--%>
										</li>
										<li value="3">
											<span class="ico16 examine_singleInput"></span>
											${ctp:i18n('inquiry.qutype.input')}<%--单行填空题--%>
										</li>
										<li value="2">
											<span class="ico16 examine_multiInput"></span>
											${ctp:i18n('inquiry.qutype.textarea')}<%--多行填空题--%>
										</li>
									</ul>
								</div>
								<div id="rightBox" class="row2 left_content_right" style="display:none">
									<ul class="title_ul margin_b_10">
										<li class="right_ul_title">${ctp:i18n('inquiry.create.li.title')}</li>
										<input type="hidden" id="inquiryType" value="new"><%--题目类型   new 新 temp 模版--%>
										<input type="hidden" id="inquiryTempId" value=""><%--题目类型   new 新 temp 模版--%>
										<li class="right_ul_img">
											<span class="question_img"><input type="hidden" id="titleImgUrl"><img id="titleImgShow" style="display: none;width: 880px;padding-left: 67px" src="" onclick="removeTitleImg()"><div id="titleImgDiv"></div></span>
										</li>
										<li class="right_ul_msg">
											<span class="question_num" title="${ctp:i18n('inquiry.default.titleimgtip')}"><img style="width: 30px;height: 30px;padding-top: 35px" src="${path}/apps_res/inquiry/css/images/titleImgUpload.png" onclick="upLoadTitleImg();"/><div id="titleImgDivParam"></div></span><%--插入序言图片,建议分辦率830*300--%>
											<textarea class="question_msg">${ctp:i18n('inquiry.default.beforedesc')}</textarea><%--感謝您對此次調查的理解和支持--%>
										</li>
									</ul>
									<ul class="fileUpload_ul margin_b_10">
										<li class="fileUpload_ul_li">
											<div id="attachmentDiv" class="attachmentDiv">
												<div class="attch_flag left">
                            						<span class="pointer">
                            						    <em class="icon16 file_attachment_16 margin_b_5"></em>
                            						    <span class="insert_file" onclick="javascript:insertAttachmentPoi('atts1')">${ctp:i18n('common.attachment.label')}</span>
                            						</span>
													<span id="attachmentTRatts1" style="display:none;">&nbsp;&nbsp;(<span id="attachmentNumberDivatts1"></span>)&nbsp;&nbsp;</span>
												</div>
												<div id="atts1" class="comp" comp="type:'fileupload',applicationCategory:'10',attachmentTrId:'atts1',canDeleteOriginalAtts:true,originalAttsNeedClone:false,takeOver:false"></div>
											</div>
										</li>
										<li class="fileUpload_ul_li">
											<div id="assdocDiv" class="attachmentDiv">
												<div class="attch_flag left">
                            						<span class="pointer">
                            						    <em class="ico16 associated_document_16 margin_b_5"></em>
                            						    <span class="insert_file" onclick="javascript:quoteDocument('atts2')">${ctp:i18n('common.mydocument.label')}</span>
                            						</span>
													<span id="attachment2TRatts2" style="display:none;">&nbsp;&nbsp;(<span id="attachment2NumberDivatts2"></span>)&nbsp;&nbsp;</span>
												</div>
												<div id="atts2" class="comp" comp="type:'assdoc',applicationCategory:'10',attachmentTrId:'atts2',canDeleteOriginalAtts:true,originalAttsNeedClone:false,takeOver:false"></div>
												<%--<div class="comp" id="assDocDomain" comp="type:'assdoc',applicationCategory:'1',attachmentTrId:'Doc1',modids:'1,3',referenceId:'${vobj.summary.id}',canDeleteOriginalAtts:true,originalAttsNeedClone:${vobj.cloneOriginalAtts},callMethod:'insertAtt_AttCallback',delCallMethod:'insertAtt_AttCallback',noMaxheight:true" attsdata='${vobj.attListJSON }'></div>--%>
											</div>
										</li>
									</ul>
									<ul  class="content_right_ul dragwen">
										<div id="inquiryQuBox" class="QUBOX">
											<div id="noQuTips" style="background: #FFF;text-align: center;height: 300px;">
												<img src="${path}/apps_res/pubinfo/image/null.png" class="no_qu_img" height="170" width="170">
												<div class="no_qu_msg">${ctp:i18n('inquiry.default.noqutip')}</div><%--拖拽或單擊左側相應題型，以添加題目--%>
											</div>
										</div>
										<li class="module bottom">
											<div class="topic_type">
												<div class="topic_type_menu">
													<div class="setup-group">
													</div>
												</div>
												<div class="topic_type_con">
													<ul class="bottom_ul">
														<li class="other_li" style="min-height:26px;word-break: break-all;width: 925px">${ctp:i18n('inquiry.default.afterdesc')}</li><%--您已完成本次问卷，感谢您的帮助与支持。--%>
													</ul>
												</div>
											</div>
										</li>
									</ul>
									<div class="bottom_button_msg">
										<a href="#this" class="buttonNew view_button" onclick="previewInquiry();">${ctp:i18n('inquiry.create.stepbutton.preview')}</a><%--预  览--%>
										<c:if test="${!isEdit}">
											<a href="#this" class="buttonNew prev_button" onclick="if(saveAllOptEdit()){goStep(1,2);}">${ctp:i18n('inquiry.create.stepbutton.back')}</a><%--上一步--%>
										</c:if>
										<a href="#this" id="step2Confirm" class="buttonNew confirm_button">${ctp:i18n('inquiry.create.stepbutton.next')}</a><%--下一步--%>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div id="step_3" class="step_3" style="display:none;">
						<div class="bottom_left_nav">
							<ul style="margin-left: 55px">
								<li id="traftButton" class="hand" onclick="if(checkData(3)){saveAll(3);createInquiry('censor');}">
									<span class="ico16 save_traft_16"></span>
									${ctp:i18n('inquiry.create.step3.draftbutton')}
								</li>
								<li id="tempButton" class="hand" onclick="if(checkData(3,'temp')){saveAll(3);createInquiry('temp');}">
									<span class="ico16 save_template_16"></span>
									${ctp:i18n('inquiry.create.step3.tempbutton')}
								</li>
							</ul>
						</div>
						<div class="step_3_body">
							<form name="loginForm" action='${path}/inquiryData.do?method=inquiryView${ctp:csrfSuffix()}' method="post"  target="_blank" >
								<%--上传附件 预览用--%>
								<input id="preAttachment" name="preAttachment" type="hidden">
							<%--选人 --%>
							<c:set value="${v3x:parseElementsOfTypeAndId(DEPARTMENTissueArea)}" var="org"/>
                            <c:set var="issueAreaName" value="${v3x:showOrgEntitiesOfTypeAndId(DEPARTMENTissueArea, pageContext)}" />
							<%--集团选人spGroup的panels增加Account可选全集团 --%>
							<v3x:selectPeople id="spGroup"  originalElements="${org}"
											  panels="Account,Department,Team,Post,Level,BusinessDepartment" selectType="Member,Department,Account,Post,Level,Team,BusinessAccount,BusinessDepartment"
											  departmentId="" jsFunction="setPeopleFields(elements)" />
							<v3x:selectPeople id="spAccount" originalElements="${org}"
											  panels="Department,Team,Post,Level,Outworker,BusinessDepartment"
											  selectType="Member,Department,Account,Post,Level,Team,BusinessAccount,BusinessDepartment"
											  departmentId="${v3x:currentUser().departmentId}"
											  jsFunction="setPeopleFields(elements)" />
                            <script type="text/javascript">
                              <!--
                               includeElements_spCustomSpace = "${v3x:parseElementsOfTypeAndId(entity)}";
                              //-->
                            </script>
                            <v3x:selectPeople id="spCustomSpace"
                                              originalElements="${org}"
                                              panels="Department,Team,Post,Level,Outworker"
                                              selectType="Member,Department,Account,Post,Level,Team"
                                              departmentId="${v3x:currentUser().departmentId}"
                                              jsFunction="setPeopleFields(elements)" />
                            <script type="text/javascript">
                            if("18" == "${v3x:escapeJavascript(param.spaceType)}" || "17" == "${v3x:escapeJavascript(param.spaceType)}" || "4" == "${v3x:escapeJavascript(param.spaceType)}") {
                                showAllOuterDepartment_spCustomSpace = false;
                            } else {
                                showAllOuterDepartment_spCustomSpace = true;
                            }
                            </script>
<%--							恩华药业		zhou--%>
							<input type="hidden" id="issueArea"
                                value="${spaceType == '18'||spaceType == '17'||spaceType == '4' ? DEPARTMENTissueArea : range.rangeId}"
                                name="issueArea"><%-- 选人信息 --%>
							<ul>
								<li>
									<span class="margin_right_span">
										<label>${ctp:i18n('inquiry.create.boardtype')}:</label><%--版块类型--%>
										<select id="boardType" class="step_3_select" onchange="changeSelect();">
                                            <c:choose>
                                              <c:when test="${spaceType==18}"><option id="boardType_18" value="group" selected>${ctp:i18n('inquiry.custom.group')}</option></c:when>
                                              <c:when test="${spaceType==17}"><option id="boardType_17" value="account" selected>${ctp:i18n('inquiry.custom.unit')}</option></c:when>
                                              <c:when test="${spaceType==4}"><option id="boardType_4" value="custom" selected>${ctp:i18n('inquiry.custom.team')}</option></c:when>
                                              <c:otherwise>
                                                <c:if test="${fn:length(accountInquiryTypeList)>0}">
                                                    <option value="account" id="boardType_2">${ctp:i18n('inquiry.unit.board')}</option>
                                                </c:if>
    											<c:if test="${fn:length(groupInquiryTypeList)>0}">
    												<option value="group" id="boardType_3">${ctp:i18n('inquiry.group.board')}</option>
    											</c:if>
                                              </c:otherwise>
                                            </c:choose>
										</select>
									</span>
									<span class="margin_right_span">
										<label>${ctp:i18n('inquiry.create.inquirytype')}:</label><%--调查版块--%>
<%--									恩华药业	zhou--%>
										<select class="step_3_select" id="boardList" name="boardList" onchange="changeBoard(),changSendRang();">
											<c:forEach items="${customInquiryTypeList}" var="inquiryBoard">
												<option value="${inquiryBoard.inquirySurveytype.id}" bType="custom" isAuth="${inquiryBoard.inquirySurveytype.censorDesc}" title="${ctp:toHTML(inquiryBoard.inquirySurveytype.typeName)}">${ctp:toHTML(v3x:getLimitLengthString(inquiryBoard.inquirySurveytype.typeName,24,"..."))}</option>
											</c:forEach>
										</select>
										<%--恩华药业                zhou--%>
								  <script type="text/javascript">
									  // 恩华药业   zhou
									  function changSendRang() {
										  var typeID = $("#boardList option:selected").val();
										  $.ajax({
											  url: '/seeyon/ehSendRangeController.do?method=getSendRange',
											  type: 'POST',
											  dataType: 'json',
											  data: {id: typeID+''},
											  success: function (result) {
												  if (result.code == 0) {
													  var range = result.data;
													  if(range!=null){
														  $("#issueArea").val(range.rangeId);
														  $("#inquiryScope").val(range.rangeName);
													  }
												  }
											  }
										  });
									  }
								  </script>
									</span>
									<span class="margin_right_span" style="">
										<label>${ctp:i18n('inquiry.create.startdept')}:</label><%--发起部门--%>
										<input type="text" id="beginDeptName" name="beginDeptName" class="step_3_input" value="${ctp:toHTML(beginDeptName)}" onclick="selectIssueArea('dept');" readonly>
										<input type="hidden" id="beginDeptId" value="${beginDeptId}">
										<input type="hidden" id="selectBeginDept" value="Department|${beginDeptId}">
                                        <c:set var="dep_id" value="${v3x:parseElementsOfIds(v3x:currentUser().departmentId, 'Department')}" />
									    <v3x:selectPeople id="spDEPT" panels="Department" maxSize="1" selectType="Department" originalElements="${dep_id}" departmentId="${v3x:currentUser().departmentId}" jsFunction="setBeginDept(elements)" />

									</span>
								</li>
								<li>
									<span class="margin_right_span">
										<label>${ctp:i18n('inquiry.meta.scope')}:</label><%--发布范围--%>
<%--									恩华药业	zhou--%>
                                        <c:choose>
                                            <c:when test="${spaceType == '18'||spaceType == '17'||spaceType == '4'}">
<%--          								      <input id="inquiryScope" name="inquiryScope" type="text" value="${issueAreaName}" onclick="javascript:selectIssueArea('custom');" class="step_3_input" readonly>--%>
          								      <input id="inquiryScope" name="inquiryScope" type="text" value="${issueAreaName}"  class="step_3_input" readonly>
                                            </c:when>
                                            <c:otherwise>
<%--												zhou 修改了id --%>
          								      <input id="inquiryScope"  name="inquiryScope" type="text" value="${range.rangeName}" class="step_3_input" readonly>
                                            </c:otherwise>
                                        </c:choose>
									</span>
									<span>
										<label>${ctp:i18n('inquiry.meta.enddate')}:</label><%--结束时间--%>
										<input id="closeDate" name="closeDate" type="text" class="comp step_3_input" comp="type:'calendar',ifFormat:'%Y-%m-%d %H:%M',showsTime:true" readonly>										<%--<input type="text" class="step_3_input">--%>
									</span>
								</li>
								<li class="mode_li">
									<span class="margin_r_15">
										<label>${ctp:i18n('inquiry.meta.votetype')}：</label><%--投票方式--%>
										<label for="inquiryVoteType1">
										<span class="margin_r_30"><input id="inquiryVoteType1" name="inquiryVoteType" type="radio" onclick="checkIsSecret();">${ctp:i18n('inquiry.meta.votetype1')}</span><%--实名--%>
										</label>
										<label for="inquiryVoteType2">
										<input id="inquiryVoteType2" name="inquiryVoteType" type="radio" onclick="checkIsSecret();" checked><span id="spaninquiryVoteType2">${ctp:i18n('inquiry.meta.votetype2')}</span><%--匿名--%>
										</label>
									</span>
									<span id = "backDoor">
										<label for="inquiryVoteBackDoor">
										<input  id="inquiryVoteBackDoor" name="inquiryVoteBackDoor" type="checkbox">${ctp:i18n('inquiry.meta.backdoor')}<%--发起人/版块管理员可查看已投票和未投票人--%>
										</label>
									</span>
								</li>
								<li class="mode_li">
									<span class="checkbox_span hidden">
										<input type="checkbox">
										${ctp:i18n('inquiry.marked.words')}
									</span>
									<span class="checkbox_span">
										<label for="inquiryResultBeforeJoin">
										<input id="inquiryResultBeforeJoin" name="inquiryResultBeforeJoin" type="checkbox" checked>
										${ctp:i18n('inquiry.allowviewresultahead.label')}
										</label>
									</span>
									<span class="checkbox_span">
										<label for="inquiryResultAfterEnd">
										<input id="inquiryResultAfterEnd" name="inquiryResultAfterEnd" type="checkbox" checked>
										${ctp:i18n('inquiry.allowviewresult.label')}
										</label>
									</span>
									<span class="checkbox_span hidden">
										<input type="checkbox">
										${ctp:i18n('inquiry.marked.words2')}
									</span>
								</li>
								<li class="button_li">

										<input type="hidden" name="packageStr" id="packageStr">
										<input type="hidden" name="questionStr" id="questionStr">
										<input type="hidden" name="metaData" id="metaData">

									<a href="#this" class="buttonNew view_button" onclick="previewInquiry();">${ctp:i18n('inquiry.create.stepbutton.preview')}</a>
									<a href="#this" class="buttonNew prev_button" onclick="goStep(2,3)">${ctp:i18n('inquiry.create.stepbutton.back')}</a>
									<a href="#this" class="buttonNew confirm_button" onclick="goStep(4,3)" id="step3Confirm">${ctp:i18n('inquiry.create.stepbutton.auth')}</a>
								</li>
							</ul>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
	<script type="text/javascript">

	</script>
</html>
