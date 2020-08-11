<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="bbsHeader.jsp"%>
<html class="h100b over_hidden">
<head>
<c:if test="${article.id == null }">
<title>${ctp:i18n('bbs.bbs.button')}</title>
</c:if>
<c:if test="${article.id != null }">
<title>${ctp:i18n('bbs.reply.modify.label')}</title>
</c:if>
<link rel="stylesheet" type="text/css" href="${path}/skin/dist/modules/bbs.css${v3x:resSuffix()}" />
<script src="${path}/apps_res/bbs/js/bbsEdit.js${v3x:resSuffix()}"></script>
<script type="text/javascript">

  var ajax_bbsArticleManager = new bbsArticleManager();
  //集团版块选人组件
  var isNeedCheckLevelScope_group = true;
  var hiddenPostOfDepartment_group = true;
  //单位版块选人组件
  var isNeedCheckLevelScope_account = true;
  var hiddenPostOfDepartment_account = true;
  var onlyLoginAccount_account = true;
  var hiddenOtherMemberOfTeam_account = true;
  //部门版块选人组件
  var isNeedCheckLevelScope_dept = false;
  var hiddenPostOfDepartment_dept = true;
  var onlyLoginAccount_dept = true;
  var includeElements_dept = "${v3x:parseElementsOfTypeAndId(entity)}";
  //自定义(集团/单位/团队)
  var isNeedCheckLevelScope_space = false;
  var hiddenPostOfDepartment_space = true;
  var showAllOuterDepartment_space = false;
  var onlyLoginAccount_space = false;
  includeElements_space = "${v3x:parseElementsOfTypeAndId(entity)}";

  function selectIssueArea(type) {
    if (type == "group") {
      selectPeopleFun_group();
    }
    if (type == "account") {
      selectPeopleFun_account();
    }
    if (type == "dept") {
      selectPeopleFun_dept();
    }
    if (type == "space") {
      selectPeopleFun_space();
    }
  }
  function setPeopleFields(elements) {
    if (!elements) {
      return;
    }
    $("#issueArea").val(getIdsString(elements));
    $("#issueAreaName").val(getNamesString(elements)).removeClass("color_gray");
    hasIssueArea = true;
  }

  var _spaceType = "${v3x:escapeJavascript(spaceType)}";
  var _boardId = "${empty v3x:escapeJavascript(param.boardId) ? article.boardId : v3x:escapeJavascript(param.boardId)}";
  var _isCustom = ${spaceType == '18'||spaceType == '17'||spaceType == '4'};
  isModify = ${article.id != null};
</script>
</head>
<body scroll='no' class="h100b over_hidden">
  <form id="articlePostForm" action="" style="height: 100%;">
    <input type="hidden" id="articleId" name="articleId" value="${article.id}">
    <input type="hidden" name="boardType" id="boardType" value="${boardType }">
    <input type="hidden" name="boardName" id="boardName" value="${ctp:toHTML(boardName)}">
    <input type="hidden" name="boardInfo" id="boardInfo" value="${ctp:toHTML(boardMapJson)} ">
    <input type="hidden" id="issueArea" value="${empty issueArea ? entity : issueArea}" name="issueArea">
    <c:set value = "${article.id != null ? 'disabled' : ''}" var="disabledFlag"/>
    <c:set value = "${isVjoinMember ? 'disabled' : ''}" var="lockAccount"/>
    <c:set value="${v3x:parseElementsOfTypeAndId(empty issueArea ? entity : issueArea)}" var="org" />
    <c:set var="issueAreaName" value="${v3x:showOrgEntitiesOfTypeAndId(empty issueArea ? entity : issueArea, pageContext)}" />
    <%--选人 --%>
    <v3x:selectPeople id="group"  originalElements="${org}" panels="Account,Department,Team,Post,Level,BusinessDepartment" selectType="Member,Department,Account,Post,Level,Team,BusinessAccount,BusinessDepartment" departmentId=""
      jsFunction="setPeopleFields(elements)" />
    <v3x:selectPeople id="account" originalElements="${org}" panels="Department,Team,Post,Level,Outworker${showVjoin|| isVjoinMember ? ',JoinOrganization' : ''},BusinessDepartment" selectType="Member,Department,Account,Post,Level,Team,BusinessAccount,BusinessDepartment"
      departmentId="${v3x:currentUser().departmentId}" jsFunction="setPeopleFields(elements)" />
    <v3x:selectPeople id="dept" originalElements="${org}" panels="Department,Team,Post,Level,Outworker" selectType="Member,Department,Account,Post,Level,Team"
      departmentId="${v3x:currentUser().departmentId}" jsFunction="setPeopleFields(elements)" />
    <v3x:selectPeople id="space"  originalElements="${org}" panels="Account,Department,Team,Post,Level,Outworker" selectType="Member,Department,Account,Post,Level,Team"
      departmentId="${v3x:currentUser().departmentId}" jsFunction="setPeopleFields(elements)" />
    <div class="dialog_body">
      <div class="dialog_header">
        <span class="index_logo" style="padding-left: 96px;">
          <img src="${path}/skin/dist/images/cultural/bbs/logo_create.png" width="31" style="margin-top: 15px;">
          <span class="index_logo_name">${ctp:i18n('bbs.bbs.button')}+</span>
        </span>
      </div>
      <div class="dialog_content" style="background: #f7f7f7;">
        <div class="dialog_content_top over_hidden">
          <%-- 标题 --%>
          <input type="text" class="input_left font16" id="articleTitle" value="${v3x:toHTML(article.articleName)}">
          <select class="news_select margin_r_10 font12" onchange="createBoardSelect(true);" id="boardTypeSelect" ${(article.id != null || isVjoinMember ) ? "disabled":""}>
            <c:choose>
              <c:when test="${spaceType==18}">
                <option id="boardType_18" value="group" selected>${ctp:i18n('bbs.board.public.custom.group')}</option>
              </c:when>
              <c:when test="${spaceType==17}">
                <option id="boardType_17" value="account" selected>${ctp:i18n('bbs.board.public.custom')}</option>
              </c:when>
              <c:when test="${spaceType==4}">
                <option id="boardType_4" value="custom" selected>${ctp:i18n('bbs.board.custom')}</option>
              </c:when>
              <c:when test="${spaceType==12}">
                <option id="boardType_12" value="project" selected>${ctp:i18n('bbs.title.label')}</option>
              </c:when>
              <c:when test="${spaceType==1}">
                <option id="boardType_1" value="dept" selected>${ctp:i18n('bbs.departmentBbsSection.label')}</option>
              </c:when>
              <c:when test="${boardType == 2 || boardType == 3}">
                <c:if test="${fn:length(boardMap['group'])>0}">
                  <option value="group" id="boardType_3" ${boardType==3 ? 'selected' : '' }>${ctp:i18n('Groupdiscussion.label')}</option>
                </c:if>
                <c:if test="${fn:length(boardMap['account'])>0}">
                  <option value="account" id="boardType_2" ${boardType==2 ? 'selected' : '' }>${ctp:i18n('Unitdiscussion.label')}</option>
                </c:if>
                <%--修改时 如果没有任何一个权限，按当前讨论版块来算--%>
                <c:if test="${fn:length(boardMap['account'])==0&&fn:length(boardMap['group'])==0}">
                  <c:if test="${boardType == 2}">
                    <option value="account" id="boardType_2" ${boardType==2 ? 'selected' : '' }>${ctp:i18n('Unitdiscussion.label')}</option>
                  </c:if>
                  <c:if test="${boardType == 3}">
                    <option value="group" id="boardType_3" ${boardType==3 ? 'selected' : '' }>${ctp:i18n('Groupdiscussion.label')}</option>
                  </c:if>
                </c:if>
              </c:when>
            </c:choose>
          </select>
          <select class="active_select font12" id="boardSelect" name="boardSelect" onchange="${article.id != null ? '' : 'changeCheckBox()' } ">
          </select>
        </div>
        <div class="dialog_content_bottom margin_t_10 margin_b_10">
          <c:choose>
            <c:when test="${spaceType == '18'||spaceType == '17'||spaceType == '4'}">
              <input id="issueAreaName" type="text" readonly="readonly" value="${issueAreaName}" class="issue_area margin_r_10" onclick="javascript:selectIssueArea('space');">
            </c:when>
            <c:when test="${spaceType == '1'}">
              <input id="issueAreaName" type="text" readonly="readonly" value="${issueAreaName}" class="issue_area margin_r_10" onclick="javascript:selectIssueArea('dept');">
            </c:when>
            <c:when test="${spaceType == '12'}">
              <input id="issueAreaName" type="text" readonly="readonly" value="${issueAreaName}" disabled class="issue_area margin_r_10">
            </c:when>
            <c:otherwise>
              <input id="issueAreaName" type="text" readonly="readonly" value="${issueAreaName}" class="issue_area margin_r_10">
            </c:otherwise>
          </c:choose>
          <span style="margin-right: 20px;" class="font12">
            <label for="radio1">
              <input type="radio" name="typeRadio" class="radio" ${article.resourceFlag==0 || empty article.resourceFlag ? 'checked' : ''} value="0" id="radio1" ${disabledFlag }>
              ${ctp:i18n('bbs.showArticle.Noth') }
              <span style="margin-right: 5px"></span>
            </label>
            <label for="radio2">
              <input type="radio" name="typeRadio" class="radio" ${article.resourceFlag==1 ? 'checked' : ''} value="1" id="radio2" ${disabledFlag }>
              ${ctp:i18n('bbs.yuan.label') }
              <span style="margin-right: 5px"></span>
            </label>
            <label for="radio3">
              <input type="radio" name="typeRadio" class="radio" ${article.resourceFlag==2 ? 'checked' : ''} value="2" id="radio3" ${disabledFlag }>
              ${ctp:i18n('bbs.zhuan.label') }
              <span style="margin-right: 5px"></span>
            </label>
          </span>
          <span class="font16">
            <label for="isGetNewReply">
              <input type="checkbox" class="checkbox" id="isGetNewReply" value="${ctp:i18n('bbs.receive.message.label') }" ${article.messageNotifyFlag == true || empty article.messageNotifyFlag ? 'checked' : ''} >
              <span class="margin_l_5 font12" style="margin-right: 5px;">${ctp:i18n('bbs.receive.message.label') }</span>
            </label>
            <label for="isSendSecret">
              <input type="checkbox" class="checkbox" id="isSendSecret" value="${ctp:i18n('anonymous.post') }" ${article.anonymousFlag == true ? 'checked' : ''} ${disabledFlag }>
              <span class="margin_l_5 font12" style="margin-right: 5px;">${ctp:i18n('anonymous.post') }</span>
            </label>
            <label for="isReplySecret">
              <input type="checkbox" class="checkbox" id="isReplySecret" value="${ctp:i18n('anonymous.reply') }" ${article.anonymousReplyFlag == true ? 'checked' : ''} ${disabledFlag }>
              <span class="margin_l_5 font12" style="margin-right: 5px;">${ctp:i18n('anonymous.reply') }</span>
            </label>
          </span>
        </div>
        <!-- 附件 -->
        <div id="attachmentDiv" class="textarea_edit_reply">
          <div class="attch_flag left">
            <span class="pointer">
              <em class="icon16 file_attachment_16 margin_b_5"></em>
              <span class="insert_file" onclick="javascript:insertAttachmentPoi('atts1')">${ctp:i18n('common.attachment.label')}</span>
            </span>
            <span id="attachmentTRatts1" style="display: none;">
              &nbsp;&nbsp;(
              <span id="attachmentNumberDivatts1"></span>
              )&nbsp;&nbsp;
            </span>
          </div>
          <div id="atts2" class="comp"
            comp="type:'fileupload',applicationCategory:'9',attachmentTrId:'atts1',canDeleteOriginalAtts:true,canFavourite:false,originalAttsNeedClone:false,callMethod:'attchmentCallBack',takeOver:false"
            attsdata='${attListJSON}'></div>
          <div id="attachmentAreaatts1" class="attachment_area left"></div>
        </div>
      </div>
      <%--正文编辑器 --%>
      <div class="text_editor">
        <iframe id="replyArticle" name="replyArticle" frameborder="0" width="100%" height="100%" src="${detailURL}?method=createArticleEditor&articleId=${article.id }${v3x:csrfSuffix()}"></iframe>
      </div>
      <div class="bottom_button" style="margin-top: -5px; position: relative; z-index: 2;">
        <a class="right create_button margin_tr_10" onclick="saveArticle('');">${ctp:i18n('bbs.create.issue.js')}</a>
        <a class="left create_button margin_tr_10" onclick="previewBbs();">${ctp:i18n('bbs.create.preView.js') }</a>
        <c:if test="${(empty article.state || article.state == 3) && spaceType!='12'}">
          <a class="left create_button margin_tr_10 save" onclick="saveArticle('true');">${ctp:i18n('button.save') }</a>
        </c:if>
      </div>
    </div>
  </form>
  <form name="preForm" action='${path}/bbs.do?method=bbsPreview${ctp:csrfSuffix()}' method="post" target="_blank">
    <input id="preTitle" name="preTitle" type="hidden">
    <input id="preContent" name="preContent" type="hidden">
    <input id="preScope" name="preScope" type="hidden">
    <input id="preBoardId" name="preBoardId" type="hidden">
    <input id="preAttachment" name="preAttachment" type="hidden">
  </form>
</body>
</html>
