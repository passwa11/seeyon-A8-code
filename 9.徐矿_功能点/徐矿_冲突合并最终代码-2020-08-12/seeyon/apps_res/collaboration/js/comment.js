function showOrCloseNamesDiv(commentId){
    var $likeIcon = $('#likeIcoNumber'+commentId);
	var _title = $likeIcon.attr('title');
	if(_title == null){
	    //var cmr = new ctpCommentManager();
	    var obj = new Object();
	    obj.moduleId = commentId;
	    //var pn = cmr.getPraiseNames(obj);
	    var pn = callBackendMethod("ctpCommentManager","getPraiseNames",obj);
	    $likeIcon.attr('title', pn);
	}
}
function praiseComment(commentId){
	var obj = new Object();
	obj.moduleId = commentId;
	obj.praiseMemberId = $.ctx.CurrentUser.id;
	//删除
	if($("#likeIco"+commentId).hasClass("like_16")){
		callBackendMethod("ctpCommentManager","deletePraise",obj,{
			success : function(flag){

            	//回填名字  人数 
            	
            	$("#likeIcoNumber"+commentId)[0].innerText = flag;
            	
            	//改变图标的颜色 +title
            	$("#likeIco"+commentId).removeClass("like_16").addClass("no_like_16");
            	$("#likeIco"+commentId)[0].title=_praise;

    	}, 
    	error : function(request, settings, e){
    		$.alert(e);
    	}
	});
		
	//新增
	}else if($("#likeIco"+commentId).hasClass("no_like_16")){
		obj.subject = parent.summaryId;
		callBackendMethod("ctpCommentManager","addPraise",obj,{

	        success : function(flag){
	
	            	//回填名字  人数 
	            	$("#likeIcoNumber"+commentId)[0].innerText = flag;
	            	//改变图标的颜色 +title
	            	$("#likeIco"+commentId).removeClass("no_like_16").addClass("like_16");
	            	$("#likeIco"+commentId)[0].title=_praiseC;

	        }, 
	        error : function(request, settings, e){
	            $.alert($.i18n('collaboration.error.net.alert'));
	        }
	    
		});
	}else{
		return;
	}
	
	

}
 function showPropleCard(memberId){
       if(openForm != 'glwd' && openForm != 'docLib'){
           targetW = getCtpTop();
       } else {
           targetW = window;
       }
       $.PeopleCard({
           targetWindow:targetW,
           memberId:memberId
       });
   }
function _commentHidden(th) {
    var t = $(th), p = t.parent().parent().next();
    if(th.checked) {
      p.css("display","inline-block");
    }else {
        p.css("display","none");
    }
  }

//5.6的协同意见回复消息推送事件，后续不用可以删了
function _pushMessageHidden(th) {
  var t = $(th), p = t.parent().parent().next();
  if(th.checked) {
    p.show();
  }else {
    p.hide();
  }
}



var _commentCounter = 0;
var  _commentNum = 0;
var  currentSelectedObj = null;
$.content.commentSearchCreate = function(str, flag) {
	var defaultVal ="<"+$.i18n("collaboration.summary.label.search.js")+">";
  if(!str || "" === str || str == defaultVal){
      return;
  }
  _commentCounter++;
  if(_commentCounter>=3) {
    _commentCounter = 0;
      return; //这个变量又来避免查不到内容的时候死循环。
  }
  
  var commentSearchObj =  $("a[name='commentSearchCreate']");
  _commentTotal = commentSearchObj.size()-1;
  if(flag == "forward"){//向前查找
      var c;
      if( _commentNum != 0 && _commentNum <= _commentTotal) { 
          c = _commentNum;
      }else{
    	  c= 0;
      }      
      if(currentSelectedObj != null) {
          $(currentSelectedObj).removeClass("selectMemberName");
      }  
      
     
      for(var i =c;i<= _commentTotal;i++){
          var obj = commentSearchObj[i];
          if(obj){
              if(obj.innerHTML.indexOf(str) != -1){
                  
                  $('html,body').animate({scrollTop: $(obj).offset().top}, 100);
                  obj.scrollIntoView();
                  /*这种跳转到锚点的方式会导致Iframe上移动
                  var path = document.location.href;
                  var jinghao = path.indexOf("#");
                  if(jinghao > 0){
                      path = path.substring(0, jinghao);
                  }
                  document.location.href = path + "#commentSearchCreate" + i;*/
                  $(obj).addClass("selectMemberName");
                  _commentNum = i + 1;
                  _commentCounter = 0;
                  currentSelectedObj = obj;
                  break;
              }else if( i == _commentTotal){
                  _commentNum = 0;
                  $.content.commentSearchCreate(str,flag);
              }
          }
      }
  }else if(flag == "back"){ //向后查找
      var c;
      if( _commentNum != 0) { 
          c = _commentNum - 1;
      }else{
    	  c= _commentTotal;
      }    
      
      if(currentSelectedObj!=null) {
          $(currentSelectedObj).removeClass("selectMemberName");
      }
      for(var i =c;i>=0 ;i--){
    	  var obj = commentSearchObj[i];
          if(obj){
              if(obj.innerHTML.indexOf(str) != -1){
                  
                  $('html,body').animate({scrollTop: $(obj).offset().top}, 100);
                  
                  /*这种跳转到锚点的方式会导致Iframe上移动
                  var path = document.location.href;
                  var jinghao = path.indexOf("#");
                  if(jinghao > 0){
                      path = path.substring(0, jinghao);
                  }
                  document.location.href = path + "#commentSearchCreate" + i;*/
                  $(obj).addClass("selectMemberName");
                  _commentNum = i;
                  _commentCounter = 0;
                  currentSelectedObj = obj;
                  break;
              }else if( i == 1 || i == 0){
                  _commentNum = 0;
                  $.content.commentSearchCreate(str,flag);
              }
          }
      }
  }
};

  function showToIdSelectPeople(commonId){
      $.selectPeople({
          callback : function(rv){
              var $hide2CommentShowToText = $("#hidTo" + commonId + " #showToIdText");
              $hide2CommentShowToText.val(rv.text);
              $hide2CommentShowToText[0].title = rv.text;
              $("#hidTo" + commonId + " #showToId").val(rv.value);
          },
          showBtn:false,
          params : {
              text : $("#hidTo" + commonId + " #showToIdText").val(),
              value: $("#hidTo" + commonId + " #showToId").val()
          },
          mode:"open",
          panels:"Department,Team,Post,Outworker,RelatePeople,JoinOrganization",
          minSize:0,
          selectType:"Member",
          showFlowTypeRadio: false
      });      

  }

  function commentShowReplyComment(commentObj, obj){
	  //流程追溯，普通回退的时候要允许回复。   
	  if(workflowTraceType != '5' && workflowTraceType != '6' && workflowTraceType != '8' && workflowTraceType != '10' && openForm != 'supervise'
		  && openForm != 'repealRecord'){
		  if(!parent.isAffairValid(contentAffairId)) {
			  return;
		  }
	  }
      function replaceAll(htm, a, b) {
          return htm.replace(new RegExp("\{" + a + "\}", "gm"), b);
      };
  
      var html = $("#commentHTMLDiv").html().toString();
      
      html = replaceAll(html, "comment.id",         commentObj.id);
      html = replaceAll(html, "comment.clevel",     commentObj.clevel);
      html = replaceAll(html, "comment.moduleType", commentObj.moduleType);
      html = replaceAll(html, "comment.moduleId",   commentObj.moduleId);
      html = replaceAll(html, "comment.createId",   commentObj.createId);
      
      if (commentObj.createId != startMemberId) {
    	  html = replaceAll(html, "curComment.comId",  commentObj.createId);
    	  html = replaceAll(html, "curComment.comName",  commentObj.createName+","+stateMemberName);
    	  html = replaceAll(html, "curComment.showId",  commentObj.createId+",Member|"+startMemberId);
      } else {
    	  html = replaceAll(html, "curComment.comId",  commentObj.createId);
    	  html = replaceAll(html, "curComment.comName",  commentObj.createName);
    	  html = replaceAll(html, "curComment.showId",  commentObj.createId);
      }
      
      var temp = $(html);
      
      //性能问题，根据点击情况进行dom加载
      var $replyCter = $("#reply_" + commentObj.id);
      if(!$replyCter || $replyCter.length == 0){
          var $replayC, $ulcomContent, $replyContent;
          $replayC = $("#replay_c_" + commentObj.id);
          $ulcomContent = $("#ulcomContent" + commentObj.id, $replayC);
          if(!$ulcomContent || $ulcomContent.length == 0){
              $ulcomContent = $('<ul id="ulcomContent'+commentObj.id+'"></ul>');
              $replayC.append($ulcomContent);
          }
          
          $replyContent = $("#replyContent_" + commentObj.id, $ulcomContent);
          if(!$replyContent || $replyContent.length == 0){
              $replyContent = $('<li id="replyContent_'+commentObj.id+'" class="replyContentLi border_t display_none"></li>');
              $ulcomContent.append($replyContent);
          }
          
          $replyCter = $('<li id="reply_'+commentObj.id+'" class="textarea form_area"></li>');
          $ulcomContent.append($replyCter);
      }
      $replyCter.empty().append(temp);
      
      var $contentObj = $("#content",temp);
      
      $contentObj.atwho('destroy');//清除插件
      at2PushMesg($("#replyMesP",$("#reply_"+commentObj.id)), $contentObj, commentObj.id);
      
      commentShowReply(commentObj.id, commentObj.createName, obj);
      
      if(commentObj.createId != $.ctx.CurrentUser.id){
          var tempAffairId = contentAffairId;
          if (commentObj.affairId) {
              tempAffairId = commentObj.affairId;
          }
          var atUserData = {
        	  affairId : tempAffairId,
        	  memberId : commentObj.createId,
        	  memberName : "@" + commentObj.createName
          };
          $("#pushMessageToMembers", $("#reply_" + commentObj.id)).val($.toJSON(atUserData));
      }
  };
  
  
  
  //点击‘发起人附言’调用方法
  function commentShowReply(rid,createName,obj) {
		if(workflowTraceType != '5' && workflowTraceType != '6' && workflowTraceType != '8' && workflowTraceType != '10' && openForm != 'supervise'
			&& openForm != 'repealRecord'){
	      if(!parent.isAffairValid(contentAffairId)) {
	          return;
	      }
		}
    $("#reply_" + rid).show();
    $(window).scrollTop($(obj).position().top);
    if (rid != "sender"){
        $("#content", $("#reply_" + rid)).focus();
    }else{
        $("#pushMessage", $("#reply_sender")).attr("checked", "checked");
    	 //checkCommonContentOut("mutclick");
    }
    /*if($.i18n("collaboration.newcoll.fywbzyl") == $("#senderpostscriptDiv #content").val()){
    	$("#senderpostscriptDiv #content").val($.i18n("collaboration.newcoll.fywbzyl"));
    }*/
  }
  
  
  
  
  
  
  //点击 ‘取消'调用方法
  function commentHideReply(rid) {
	$("h3.per_title").css("clear","none");
    $("#reply_" + rid).hide();
    //将选中的'意见隐藏'置为不可选，并且隐藏相应的区域
    $("[name='hidden']").each(function(){
        //取消全选
        $(this).removeAttr("checked");
        _commentHidden(this);
    });
    //将选中的'消息推送'置为不可选，并且隐藏相应的区域
    $("[name='pushMessage']").each(function(){
        //取消全选
        $(this).removeAttr("checked");
        _pushMessageHidden(this);
    });
    $("#reply_"+rid+" #pushMessageToMembers").val("[]");
    clearAtt(rid);
    $("h3.per_title").css("display","block");
  }
  
  
  var proce = null;
  function startProcessBar() {
      proce = $.progressBar();
  }
  function endProcessBar(){
      if (proce != null) {
          proce.close();
          proce = null;
      }    
  }
  /**
   * 这是5.6消息推送和at数据合并的方法， 如果不采用消息推送的话这个方法就删了
   * @param $delMesPush 意见栏a的人员
   * @param $oldMesPush 原来的消息推送功能放的文本域
   * @param $textArea   意见框
   * @return
   */
  function mergeMesPushFun($delMesPush,$oldMesPush,$textArea){
      try{
          var d1 = $delMesPush.val();
          if(d1 && d1 != '') {
              d1 = $.parseJSON(d1);
          }
          
          var d2 = $oldMesPush.val();
          if(d2 && d2 != ''){
              d2 = $.parseJSON(d2);
          }
          
          var val = $textArea.val();
           
          var all =[];
          if(d2 instanceof Array && d2.length > 0){
              all = d2;
          }
          else if(d2){
              all.push(d2);
          }
          
          if(d1.length > 0){
              for(var i =0; i < d1.length; i++){
                  if(val.indexOf("@All") > -1 || val.indexOf(d1[i][2]) > -1){
                      var v={
                          affairId : d1[i][0],
                          memberId : d1[i][1],
                          memberName : d1[i][2]
                      };
                      all.push(v);
                  }
              }
          }
          $oldMesPush.val($.toJSON(all));
          
      }catch(e){
          
      }
      
  }
  function commentReply(rid,t) {

    _clickCount = addOne(_clickCount);
    if(parseInt(_clickCount)>=2){
    	//不能重复提交，用最原生的alert，$.ALERT不能阻塞，太慢了才弹出
        alert($.i18n("collaboration.summary.notDuplicateSub"));
        _clickCount = 0;
        return;
    }
    
    startProcessBar();
    
    var  dm = $("#reply_" + rid);
    var rContainer = $("#replay_c_" + rid);
    var mcp = rContainer.attr("mcp");
    var  cp = rContainer.attr("cp");
    var pt = cp;
    if ((mcp).length == 1) {
      pt += '00' + mcp;
    } else if ((mcp).length == 2) {
      pt += '0' + mcp;
    } else {
      pt += mcp;
    }
    var contentArea = $("#content", dm);
    if ($.trim(contentArea.val()) == ""){
        $.alert($.i18n('collaboration.common.deafult.commonNotNull'));
        contentArea.focus();
        _clickCount = 0;
        endProcessBar();
        return;
    }
    // 检查回复内容长度
    var checkLength = contentArea.val().length;
    if(checkLength > 500){
        $.alert($.i18n('collaboration.common.deafult.commonMaxSize',checkLength));
        contentArea.focus();
        _clickCount = 0;
        endProcessBar();
        return;
    }
    $("#path", dm).val(pt);
    rContainer.attr("mcp", parseInt(mcp) + 1);
    
    //先清空上一次保存的附件信息，否则会ID重复
    document.getElementById("reply_attach_"+rid).innerHTML="";
    document.getElementById("reply_assdoc_"+rid).innerHTML="";
    //add by libing
    var srcObj = document.getElementById("#reply_"+rid);
    mergeMesPushFun($("#reply_"+rid+" #replyMesP"),$("#reply_"+rid+" #pushMessageToMembers"),$("#reply_"+rid+" #content"));
    
    var obj = dm.formobj({errorIcon : false});
    
    var content = obj.content;
    if(content.indexOf("@All") == -1){
    	var oldMembers = $.parseJSON(obj.pushMessageToMembers);
    	var newMembers = [];
    	for(var i = 0 ; i < oldMembers.length ; i++ ){
    		var data = oldMembers[i];
    		var isReplyedMember = obj.createId == data["memberId"];
    		if(isReplyedMember || content.indexOf(data["memberName"]) != -1 ){  //是默认的被回复人||意见框中有人 
    			newMembers.push(data);
    		}
    	}
    	obj.pushMessageToMembers = $.toJSON(newMembers);
    }
    if(obj.pushMessageToMembers == "[[]]" || obj.pushMessageToMembers == "[]"){
    		obj.pushMessageToMembers="";
    }
    saveAttachmentPart("reply_attach_" + rid);
    obj.attachList = $("#reply_attach_" + rid).formobj();
    callBackendMethod("colManager","insertComment",obj,openForm,{

        success : function(ret) {
            
        var cHtml = '<p class="comments_title_in ">'
        + '<a onclick="showPropleCard(\''+ ret.createId +'\');" class="left title color_blue padding_lr_10" title="'+ ret.createName +'">'
        + getLimitLength(ret.createName,20,'...');
        
        var createName = '';
        if(ret.extAtt2 != null && ret.extAtt2 != ""){
            createName = "<div class='display_inline color_red'>"+$.i18n('collaboration.agent.label',ret.extAtt2)+"</div>";
        }
        cHtml += createName;
        
        cHtml += '</a>'
          + '<span class="right color_gray margin_t_5 margin_r_10 margin_r_5">'+ ret.createDateStr +'</span>';
          +'</p>';
         
          var cContaner = $("#replyContent_" + rid);
          cContaner.append(cHtml);
          
          var cContent = $('<div class="comments_content"></div>');
          cContent.append('<p class="font_size14">'+ ret.escapedContent +'</p>');
          
          var cAttContener = $('<div class="clearfix" style="background:none;"></div>');
        //如果插入了本地文档
          if(hasUploadAttachment( obj.attachList)){
            var htm = $("#attachmentTRreply_attach_" + rid)[0].innerHTML ;
            var ht = htm.replace(/reply_attach_/gi,'');
            cAttContener.append($("<div class='clearfix' style='padding-top:8px;'>"+ht+"</div>"));
          }
          
        //如果插入了关联附件
          if(hasUploadDocument( obj.attachList)){
            var htm = $("#attachment2TRreply_attach_" + rid)[0].innerHTML ;
            var ht = htm.replace(/reply_attach_/gi,'');
            cAttContener.append($("<div class='clearfix'>"+ht+"</div>"));
          }
          
          cContent.append(cAttContener);
          cContaner.append(cContent);
      
                
          $(".affix_del_16",cContaner).each(function(){
            var t = $(this);
            t.removeClass("affix_del_16 ico16");
          });
          
          cContaner.show();
          
          $("#content", dm).val('');
          clearAtt(rid);
          commentHideReply(rid);
          _clickCount = 0;
          endProcessBar();
        }
  
    });
    
  }
  function hasUploadAttachment (attachList){
    if(attachList == null) return false;
    for(var i = 0; i < attachList.length; i++){
      var att = attachList[i];
      if(att.attachment_type == '0') return true;
    }
    return false;
  }
  
  
  
  function hasUploadDocument (attachList){
    if(attachList == null) return false;
    for(var i = 0; i < attachList.length; i++){
      var att = attachList[i];
      if(att.attachment_type == '2') return true;
    }
    return false;
  }

  function clearAtt(rid){
    //清空正文
    $("#reply_" + rid + " #content").val("");
    //清空附件区，包括附件及统计数字label
    $("#attachmentAreareply_attach_" + rid).children().remove();
    $("#attachment2Areareply_attach_" + rid).children().remove();
    $("#attachmentTRreply_attach_" + rid).attr("style","display:none");
    $("#attachment2TRreply_attach_" + rid).attr("style","display:none");
    $("#attachmentNumberDivreply_attach_" + rid).val("");
    $("#attachment2NumberDivreply_attach_" + rid).val("");
    deleteAllAttachment(0,"reply_attach_"+rid);
    deleteAllAttachment(2,"reply_attach_"+rid);
  }
  function addOne(n){
    return parseInt(n)+1;
  }
  
  
  //发起人附言提交
  var _clickCount = 0; //重复提交计算器
  function commentSenderReply(t) { 
	//重复提交检验，点的太快了disable也不管用
	_clickCount = addOne(_clickCount);
    if(parseInt(_clickCount)>=2){
    	//不能重复提交，用最原生的alert，$.ALERT不能阻塞，太慢了才弹出
        alert($.i18n("collaboration.summary.notDuplicateSub"));
      	_clickCount = 0;
        return;
    }
    startProcessBar();
    var _args = arguments[1];
    checkCommonContent();
    
    if($("#pushMessage",$("#reply_sender")).is(":checked")) {
      $("#pushMessage",$("#reply_sender")).val(true);
    }else{
      $("#pushMessage",$("#reply_sender")).val(false);
    }
    var dm = $("#reply_sender");
    //清空上次操作saveAttachmentPart生产的Input隐藏域
    $("#reply_attach_sender").html("");
    var obj = dm.formobj({errorIcon : false});
    if($("#pushMessage",$("#reply_sender")).val()=="true"){
    	obj.pushMessage = true;
    }else if($("#pushMessage",$("#reply_sender")).val()=="false"){
    	obj.pushMessage = false;
    }
    if ($.trim($("#content", dm).val()) == ""){
        $.alert($.i18n('collaboration.common.default.fuyanNotNull'));  //附言内容不能为空！
        $("#content", dm).focus();
        _clickCount = 0;
        endProcessBar();
        return;
    }
    // 检查附言长度,不能超过500
    var checkLength = $("#content", dm).val().length;
    if(checkLength > 500){
        $.alert($.i18n('collaboration.common.deafult.commonMaxSize',checkLength)); 
        $("#content", dm).focus();
        _clickCount = 0;
        endProcessBar();
        return;
    }
    if($._isInValid(obj)){
      _clickCount = 0;
      endProcessBar();
      return;
    }
    saveAttachmentPart("reply_attach_sender");
    obj.attachList = $("#reply_attach_sender").formobj();
    callBackendMethod("colManager","insertComment",obj,openForm,{

        success : function(ret) {
          if(_args=="undefined"){
            var tmp = $('<li class="comment_li"></li>');
            
          }else if(_args=="true"){
            var tmp = $('<li class="comment_li ui_print_li_borderBottom" style="padding:13px 0px;border-bottom:none; line-height:24px;"></li>');
            var $li = $(".comment_li");
            $($li[$li.length-1]).css("border-bottom","1px solid #e4e4e4");
          }
          tmp.html("<div style='word-wrap:break-word;'><span class='font_size14'>" + ret.escapedContent + '</span></div>');
           
           // var wrap = $("<li></li>");
           // $("#attachmentAreareply_sender").children().removeAttr("style").wrapAll(wrap);
           // $("#reply_sender").before($("#attachmentAreareply_sender").children());
            if(hasUploadAttachment( obj.attachList)){
              var htm = $("#attachmentTRreply_attach_sender")[0].innerHTML ;
              var ht = htm.replace(/reply_attach_/gi,'');
              tmp.append($("<div class='clearfix' style='word-break: normal; word-wrap:break-word; text-justify: auto; text-align: justify;font-size:12px; margin-top:13px; padding-bottom:5px;'>"+ht+"</div>"));
            }

            //如果插入了关联附件
            if(hasUploadDocument( obj.attachList)){
              var htm = $("#attachment2TRreply_attach_sender")[0].innerHTML ;
              var ht = htm.replace(/reply_attach_/gi,'');
				if(hasUploadAttachment( obj.attachList)){
					tmp.append($("<div class='clearfix' style='padding-bottom:5px;'>"+ht+"</div>"));
				}else{
					tmp.append($("<div class='clearfix' style='padding-bottom:5px; padding-top:13px;'>"+ht+"</div>"));
				}
            }
            tmp.append('<div class="color_gray2" style="margin-top: 8px; line-height:22px;">'+ ret.createDateStr + '<div>');
            $("#reply_sender").before(tmp);
            //去掉删除图标
            $(".affix_del_16",$("#replyContent_sender")).each(function(){
              var t = $(this);
              t.removeClass("affix_del_16 ico16");
            });
            $("#content", dm).val('');
            clearAtt('sender');
            commentHideReply('sender');
            _clickCount = 0;
            endProcessBar();
        }
      
    });
  }
  
  function callManagerMethod(){
	  var param = {"summaryId":parent.summaryId};
	  var rset =  callBackendMethod("colManager","pushMessageToMembersList",param);
	  return rset;
  }
  
  //是否加载过人员列表，默认未加载
  var hasPush = false;
  var pushMessageToMembersArray = new Array();
  //加载At的人员列表
  function pushMessageToMembersList(dealOrComment){
	  var rset = new Array();
	  var membersList = new Array();
	  //已经加载了人员的不再次查数据库
	  if(hasPush){
		  rset = pushMessageToMembersArray;
	  }else{
		  rset =  callManagerMethod();
		  membersList = rset;
	  }
	  //处理意见框才加入加签的人员
	  if(dealOrComment && dealOrComment=="deal"){
		  var  assignNodeMember = findAssignNodeMember();
		  rset = assignNodeMember.concat(rset);
	  }
	  var length_ = rset.length;
	  var html = '';
	  var addMember = new Array();
	  for(var i = 0 ; i < length_ ; i++){
		  var state = rset[i].state;
		  var memberId = rset[i].memberId;
		  var id = rset[i].id;
		  var name = rset[i].name;
		  var i18n = rset[i].i18n;
		  
		  //去除重复的人员
		  if($.inArray(memberId, addMember)>-1 || memberId == $.ctx.CurrentUser.id){
			  continue;
		  }else{
			  addMember.push(memberId);
		  }
		  var class_ = '';
		  if(state == 2){
			  class_ = '_pm_fixed';
		  }
		  var style1 = '';
		  if(i != length_ - 1){
			  style1 = 'border-bottom: none;';
		  }
		  
		  html += '<tr class="' + class_ + '" align="center" memberId="' + memberId + '">';
		  html += '<td class="border_t" style="border-right:none;' + style1 + '">';
		  html += '<input type="checkbox" class="checkclass" value="' + id + '" memberName="' + name + '" memberId="' + memberId + '">';
		  html += '</td>';
		  html += '<td align="left" class="border_t" style="border-right:none;' + style1 + 'word-break: break-all;">' + name + '</td>';
		  html += '<td align="left" style="' + style1 + '" class="border_t">' + i18n + '</td>';
		  html += '</tr>';
	  }
	  if(!hasPush){
		  hasPush = true;
		  pushMessageToMembersArray = membersList;
	  }
	  var toHtml = $("#comment_pushMessageToMembers_tbody");
	  toHtml.html(html);
  }
  
  //点击At组件弹出消息推送框
  function pushMessage4At(params) {
	  var commentId = params.commentId
	  if(commentId){
		  pushMessageToMembersList();
	  }else{
		  pushMessageToMembersList("deal");
	  }
	  
      var callback = params.callback, $atAllMemberInput = params.atAllMemberInput;

    try {
        if ($.browser.mozilla || $.browser.version == '6.0' || $.browser.version == '7.0') {
            if ($.browser.version == '6.0' || $.browser.version == '7.0') {
                parent.showContentView();
            }
        }

        // TODO Dialog组件目前存在组件事件被清除的bug，暂时处理为每次弹出时添加事件
        $("#comment_pushMessageToMembers_dialog_searchBtn").click(function() {
            pushMessageToMembersSearch();
        });

        $("#checkAll").click(function() {// 当点击全选框时
            var flag = $(this).attr("checked");// 判断全选按钮的状态
            dialog.getObjectByClass("checkclass").each(function() {
                if ($.trim(flag) === "checked") {
                    $(this).attr("checked", "checked");// 选中
                } else {
                    $(this).removeAttr("checked"); // 取消选中
                }
            });
        });
        // 如果全部选中勾上全选框，全部选中状态时取消了其中一个则取消全选框的选中状态
        $(".checkclass").click(function() {
            if (dialog.getObjectByClass("checkclass:checked").length === dialog.getObjectByClass("checkclass").length) {
                dialog.getObjectById("checkAll").attr("checked", "checked");
            } else
                dialog.getObjectById("checkAll").removeAttr("checked");
        });

        $("#comment_pushMessageToMembers_dialog_searchBox").keypress(function(e) {
            var c;
            if ("which" in e) {
                c = e.which;
            } else if ("keyCode" in e) {
                c = e.keyCode;
            }
            if (c == 13) {
                pushMessageToMembersSearch();
            }
        });
        var targetVer = getCtpTop();
        if ($.browser.version == '6.0' || $.browser.version == '7.0') {
            targetVer = window.top;
        }
        //全局变量
        dialog = $.dialog({
            htmlId : 'comment_pushMessageToMembers_dialog',
            title : $.i18n('collaboration.pushMessageToMembers.choose'),
            width : 320,
            height : 270,
            targetWindow : targetVer,
            buttons : [ {
                text : $.i18n('collaboration.pushMessageToMembers.confirm'),
                btnType : 1,
                handler : function() {
                    var txt = "", val = [];
                    var _textTxt = "";
                    var dataList = [];
                    var at = "@";
                    dialog.getObjectByClass("checkclass").each(function() {
                        var $this = $(this);
                        if ($this.is(":checked")) {
                        	
                        	var dataMap = {};
                        	if($this.attr("id") == "checkAll"){
                                var data = _getAtAllData();
                                dataMap.context = at + data["memberName"];
                                data["atwho-at"] = at;
                                dataMap.data = data;
                            }else{
                            	dataMap.context = at + $this.attr("memberName");
                            	var data = {}
                            	data.affairId = $this.val();
                            	data.memberName = $this.attr("memberName");
                            	data.memberId = $this.attr("memberId");
                            	data["atwho-at"] = at;
                            	dataMap.data = data;
                            }
                            
                            dataList.push(dataMap);
                        }
                    });
                    callback(dataList);
                    dialog.close(dialog.index);
                }
            }, {
                text : $.i18n('collaboration.pushMessageToMembers.cancel'),
                handler : function() {
                    dialog.close(dialog.index);
                }
            } ]
        });
    } catch (e) {
    }
}
  
  //意见回复，点击at
  function atMembers4Comment(commentId){
      var params = {
              "atAllMemberInput" : $("#atAllMembers", $("#reply_" + commentId)),
              "callback" : function(dataList){
                  if(dataList && dataList.length > 0){
                      //数据填充
                      var $replyAtInput = $("#replyMesP",$("#reply_"+commentId));
                      //转换前一次的成数组
                      var cvold = $replyAtInput.val();
                      if(cvold) {
                          cvold = $.parseJSON(cvold);
                      }
                      
                      //赋值
                      var val = [];
                      if(cvold.length > 0){
                          val = cvold;
                      }
                      
                      var isAtAll = false;
                      for(var i = 0, len = dataList.length; i < len; i++){
                          var d = dataList[i];
                          if(d["data"]["affairId"] == "All"){
                        	  isAtAll = true;
                          }else{
                              var v = [];
                              v.push(d["data"]["affairId"]);
                              v.push(d["data"]["memberId"]);
                              v.push("@" + d["data"]["memberName"]);
                              val.push(v);
                          }
                      }
                      $replyAtInput.val($.toJSON(val));
                      
                      if(isAtAll){
                    	  var newDataList = [];
                    	  for(var i = 0, len = dataList.length; i < len; i++){
                              var d = dataList[i];
                              if(d["data"]["affairId"] == "All"){
                            	  newDataList.push(d);
                              }
                    	  }
                    	  dataList = newDataList;
                      }
                      $("#content", $("#reply_" + commentId)).atwho('insertNoSelect', dataList);
                  }
              }
      }
      params["commentId"] = commentId;
      pushMessage4At(params);
  }
  
  var _pushMessageLastIdx;
  var dialog;
  /*以前的消息推送调用的接口， 如果后续废弃了，这里可以删除*/
  function pushMessageToMembers(txtObj, valObj, boolPush, moveToTopId,affairId) {
      try{
      if($.browser.mozilla || $.browser.version=='6.0'|| $.browser.version=='7.0') {
          if ($.browser.version=='6.0'|| $.browser.version=='7.0'){
              parent.showContentView();
           }
          $("#comment_pushMessageToMembers_dialog").css({'display':'block'});
      } 
      if (txtObj && txtObj.val() != "" && valObj &&  valObj.val() == "[]") {
          valObj.val('[["'+affairId+'","'+moveToTopId+'"]]');
      }
    //置顶还原
    $("tbody tr._topped", $("#comment_pushMessageToMembers_grid")).each(function(){
      var t = $(this);
      if(_pushMessageLastIdx) {
        var par = $("tbody tr", $("#comment_pushMessageToMembers_grid"))[_pushMessageLastIdx];
        if(par != this)
          $(par).after(t);
      }
      t.removeClass("_topped");
    });
    if(moveToTopId) {
      //根据传入的优先排序记录ID进行排序
      $("tbody tr", $("#comment_pushMessageToMembers_grid")).each(function(i){
        var t = $(this);
        if(!t.hasClass("_pm_fixed") && t.attr("memberId") == moveToTopId) {
          var _pushMessageFixObj = $("tbody tr._pm_fixed", $("#comment_pushMessageToMembers_grid"));
          if(_pushMessageFixObj.length > 0)
            _pushMessageFixObj.after(t);
          else
            $("tbody", $("#comment_pushMessageToMembers_grid")).prepend(t);
          _pushMessageLastIdx = i;
          t.addClass("_topped");
          return false;
        }
      });
    }
    var cv = valObj.val();
    if(cv && cv != '') {
      cv = $.parseJSON(cv);
    }
    var count = 0;
    $(".checkclass").each(function(){
      var t = $(this);
      if(cv.length > 0 && cv[0].length > 0) {
        for(var i = 0; i < cv.length; i++) {
          if(t.attr("memberId") == cv[i][1]) {
            this.checked = true;
            count += 1;
            break;
          }else{
            this.checked = false;
          }
        }
      }else {
        this.checked = false;
      }
    });
    if (count !=0 && count == $(".checkclass").length-1) {
        $("#checkAll").attr("checked","checked");
    }
    //TODO Dialog组件目前存在组件事件被清除的bug，暂时处理为每次弹出时添加事件
    $("#comment_pushMessageToMembers_dialog_searchBtn").click(function(){
      pushMessageToMembersSearch();
    });
    
    $("#checkAll").click(function () {//当点击全选框时 
        var flag = $(this).attr("checked");//判断全选按钮的状态 
        dialog.getObjectByClass("checkclass").each(function(){
            if($.trim(flag)==="checked"){
                $(this).attr("checked","checked");//选中
            }else{
                $(this).removeAttr("checked"); //取消选中 
            }
        }); 
    });
    //如果全部选中勾上全选框，全部选中状态时取消了其中一个则取消全选框的选中状态  
      $(".checkclass").click(function () {
            if (dialog.getObjectByClass("checkclass:checked").length === dialog.getObjectByClass("checkclass").length) { 
                dialog.getObjectById("checkAll").attr("checked", "checked"); 
            }else 
                dialog.getObjectById("checkAll").removeAttr("checked"); 
      });
        
    $("#comment_pushMessageToMembers_dialog_searchBox").keypress(function(e){
      var c;
      if ("which" in e) {
        c = e.which;
      } else if ("keyCode" in e) {
        c = e.keyCode;
      }
      if (c == 13) {
        pushMessageToMembersSearch();
      }
    });
    var targetVer = getCtpTop();
    if ($.browser.version=='6.0' || $.browser.version=='7.0') {
        targetVer = window;
    }
    dialog = $.dialog({
      htmlId : 'comment_pushMessageToMembers_dialog',
      title : $.i18n('collaboration.pushMessageToMembers.choose'),
      width : 320,
      height : 270,
      targetWindow:targetVer,
      buttons : [ {
        text : $.i18n('collaboration.pushMessageToMembers.confirm'),
        btnType:1,
        handler : function() {
          var txt = "", val = [];
          var _textTxt = "";
          dialog.getObjectByClass("checkclass").each(function(){
            var t = $(this), v = [];
            if(t.is(":hidden")){
            	return;
            }
            if($(this).is(":checked")) {
              if(txt != "" && txt != "undefined") {
                  txt += ",";
              } else {
                  txt = "";
              }
              if(_textTxt ==""  && !parent.isclickInput){
            	  _textTxt +=t.attr("memberName");
              }else{
            	  _textTxt += "@"+ t.attr("memberName");
              }
              txt += t.attr("memberName");
              if (t.attr("memberId") != undefined && t.attr("memberId") !="undefined"){
                  v.push(t.val());
                  v.push(t.attr("memberId"));
                  val.push(v);
              } 
            }
          });
          if(txtObj && txt != "undefined"){
            txtObj.val(txt);
            try{txtObj[0].title = txt;}catch(e){}
          }
          if(valObj){
            valObj.val($.toJSON(val));
          }
          if(boolPush) {
            if(val.length > 0) {
              boolPush.val(true);
            }else {
              boolPush.val(false);
            }
          }
          dialog.close();
        }
      }, {
        text : $.i18n('collaboration.pushMessageToMembers.cancel'),
        handler : function() {
          dialog.close();
        }
      }]
    });
      }catch(e){}
  }
  
  
  //获取发送消息的人员信息
  function _getPushMesgMembers(dealOrComment){
	  //加载页面数据
	  pushMessageToMembersList(dealOrComment);
      //获取可以发送消息的人员
      var userDatas = [];
      $("#comment_pushMessageToMembers_tbody .checkclass").each(function(){
          var $this = $(this);
          var affairId = $this.val();
          var memberName = $this.attr("memberName");
          var memberId = $this.attr("memberId");
          
          var tempData = {};
          tempData.affairId = affairId;
          tempData.memberName = memberName;
          tempData.memberId = memberId;
          userDatas.push(tempData);
      });
      return userDatas;
  }
  
  /** 获取atAll 对象数据 **/
  function _getAtAllData(){
      var atAllData = {};
      atAllData.affairId = "All";
      atAllData.memberName = "All";
      atAllData.memberId = "All";
      return atAllData;
  }
  
  /**
     * 用at方式发送消息
     * 
     * @param $pushMembers
     * @param $textArea
     */
  var comment_userDatas;
  function at2PushMesg($pushMembers, $textArea, commentId){
      var at_config = {
              at: "@",
              alias : (new Date()).getTime(),
              data: null,
              searchKey : "memberName",
              displayTpl: "<li>${memberName}</li>",
              insertTpl: "${atwho-at}${memberName}",
              startWithSpace: false,
              customFn4LowIE : function(){
                  atMembers4Comment(commentId);
              },
              callbacks : {
                  beforeInsert : function(value, $li) {
                      var data = $li.data('item-data');
                      if(data["affairId"] == "All"){
                          var atUserData = $("#pushMessageToMembers", $("#reply_" + commentId));
                          var tempAtUserData = [];
                          if(atUserData.val() != ""){
                        	  tempAtUserData.push($.parseJSON(atUserData.val()));
                          }
                          
                          var atMembers = _getPushMesgMembers();
                          for(var k = 0, len = atMembers.length; k < len; k++){
                        	  var m = {
                    			  "affairId": atMembers[k].affairId,
                    			  "memberId" : atMembers[k].memberId,
                    			  "memberName" : "@" + atMembers[k].memberName
                        	  };
                        	  tempAtUserData.push(m);
                          }
                          //选择了all
                          atUserData.val($.toJSON(tempAtUserData));
                      }else{
                        //转换前一次的成数组
                          var cvold = $pushMembers.val();
                          if(cvold && cvold != '') {
                              cvold = $.parseJSON(cvold);
                          }
                          
                          //赋值
                          var val = [];
                          if(cvold.length > 0){
                              val = cvold;
                          }
                          
                          var v = [];
                          v.push(data["affairId"]);
                          v.push(data["memberId"]);
                          v.push("@" + data["memberName"]);
                          val.push(v);
                              
                          $pushMembers.val($.toJSON(val));
                      }
                      return value;
                  },
                  afterSorter : function(items, query, searchKey){
                      
                      if(!items){
                          items = [];
                      }
                      
                      if(items.length > 0 && items[0].affairId != "All"){
                          items.push(_getAtAllData());
                      }
                      return items;
                  },
                  remoteFilter : function(query, searchKey, callbackFn){
                	  //获取列表数据
                      if(!comment_userDatas){
                    	  comment_userDatas = _getPushMesgMembers();
                      }
                      
                      var ds = this.callbacks('filter').call(this, query, comment_userDatas, searchKey) || [];
                      
                      if(ds.length == 0){
                    	  var toAddAll = true;
                    	  if(query && "all".indexOf(query.toLocaleLowerCase()) == -1){
                    		  toAddAll = false;
                    	  }
                    	  if(toAddAll){
                    		  ds.push(_getAtAllData());
                    	  }
                      }
                      callbackFn.call(this, ds);
                  }
              },
              limit: 200
            };
            
            var $inputor = $textArea.atwho(at_config);
            //$inputor.caret('pos', $inputor.val().length);//位置
            //$inputor.focus();
            //$inputor.focus().atwho('run');
  }
  
  function pushMessageToMembersSearch() {
      var txt = dialog.getObjectById("comment_pushMessageToMembers_dialog_searchBox").val();
      dialog.getObjectById("comment_pushMessageToMembers_tbody").find('tr').each(function(){
          var t = $(this);
          if(!txt || txt == '' || txt.trim() == '') {
              t.show();
          } else {
              if($($("td", t)[1]).text().indexOf(txt) != -1) {
                  t.show();
              } else {
                  t.hide();
              }
          }
      });
    //清空所有选中状态
      dialog.getObjectById("comment_pushMessageToMembers_grid").find(".checkclass").each(function(){
          this.checked = false;
      });
  }

  function init_goToTop () {
      
      var _goToReplyHeight = $("#cc").height();
      
      var topClass = "GoTo_Top";
      //CAP4屏蔽扫一扫功能
      if(parent.bodyType == '20' && _scanCodeInput == '1' && (parent.hasBarCodePlug=='true' || parent.cap4Flag=='false')){
           $("body").prepend( "<iframe id='GoTo_Top_scan_iframe' scrolling='no' style=\"position:fixed;_position:absolute;width:42px;height:42px;left:50%;bottom:110px;margin-left:396px;border:none; padding:0;\"></iframe><a style=\"margin-left: 399px; display: inline;\" id=\"GoTo_Top_scan\" class=\"GoTo_Top_scan\" title=\"二维码\" href=\"javascript:barCode()\"></a>");    
           topClass = "GoTo_Top2";
      }
      
      // 返回顶部
      new GoTo_Top({
		  showHeight : $(window).height(),
		  marginLeft : 890,
          btnClass: topClass,
		  sTitle : $.i18n('collaboration.title.return.top.js')
      });//返回顶部

      // 滚动到回复区
      if (_goToReplyHeight == 0) {
          _goToReplyHeight = 786;
      };
      _xConHeightobj = new GoTo_Top({
          id: "goToReply",
          btnClass: "goToReply",
          showHeight: $(window).height(),
          marginLeft: 792,
          nGoToHeight: _goToReplyHeight,
          sTitle: $.i18n('collaboration.title.return.comment.js') //返回意见区
      });
      var showPIBook = false;
      if(parent.templateId != "" && openFrom !="glwd" && openFrom != 'supervise' && openFrom !='newColl' ){
    	  showPIBook= true;
      }
      var showRelData = true;
      if(!parent.showDataRelFlag || openFrom =="glwd" || openFrom =='supervise' || openFrom =='newColl' || openFrom == 'capQuery'){
    	  showRelData= false;
      }
      
      var _pbtemplateId = parent.templateId;
	  var _pbobjectId = parent.summaryId;
	  var _pbmoduleType = parent.$("#bodyType").val() =="20" ?"2":"1";
	  var _pburl = _ctxPath + "/template/template.do?method=templateProcessInstruction&templateId="+_pbtemplateId+
		"&needRead=1"+"&objectId="+_pbobjectId+"&moduleType="+_pbmoduleType;
	  var _pbid = getMultyWindowId("id",_pbobjectId);
      if(showRelData || showPIBook){
    	  openOtherMenu = new parent.GoTo_Top({
    		  id: "openOtherMenu",
    		  btnClass: "goToOtherMenu",
    		  showHeight: $(window).height(),
    		  marginLeft: 792,
    		  showBook:showPIBook,
    		  piShowFun:function(){
    				openCtpWindow({
    					"url"     : _pburl,
    					"id"	  : "processIns"+_pbid
    			  });
    		  },
    		  dataRelationShowFun : function(){
    			  parent._openGunLianFun();
    		  },
    		  showRelData: showRelData,
    		  nGoToHeight: _goToReplyHeight,
    		  sTitle: $.i18n('collaboration.title.return.comment.js') //返回意见区
    	  });
      }
  }
  //流程日志鼠标事件
  function _logSpanMouseEvent(obj, type){
      var $obj = $(obj);
      if("enter" == type){
           $(".log_msg",$obj).removeClass("display_none");
      }else if("leave"){
           $(".log_msg",$obj).addClass("display_none");
      }
  }
  
  function attDivToggle(o){
      var $uploadMsg = $("#upload_files_msg"+o);
      
      if($uploadMsg.css("display")=="none"){
          
          var $li = $("li", $uploadMsg);
          $uploadMsg.css("top", (0 - ($li.length * 30 + 10)) + "px");
          $uploadMsg.show();
      }else{
          $uploadMsg.hide();
      }
  }
  
  $(function(){
      
	if($.browser.mozilla){
      window.location.hash = "#"+window.parent.contentAnchor;
	}
	
	//性能问题，分开执行
	setTimeout(function(){
	    var $attBlock = $(".attachment_block");
	    $attBlock.css("margin-top","5px");
	    $("a", $attBlock).css({"display":"inline", "max-width":"none"});
	    
	  //yinr
	    var $lastLogMsg = $("#log_msg_index_" + log_msg_index);
	    $lastLogMsg.css({"top":10 - $lastLogMsg.children("p").length * 28 + "px"});
	    
	}, 50);
    
    
  //###特殊处理### ie8下延时3秒来设置返回顶部按钮。因为$("#cc")页面加载完还没有内容，获取的高度不对
    //先判断不是模版页面,模版详细页面也使用到了这个页面
    if (typeof(isTemplatePage) == "undefined") {
        setTimeout(function(){
            init_goToTop();
        },2000);
    }

    //加载意见
   initMoreComment("0");
  });
  
  function checkCommonContent(){
      var content = $("#senderpostscriptDiv #content").val();
      var defaultValue = $.i18n("collaboration.newcoll.fywbzyl");
      if (content == defaultValue) {
          $("#senderpostscriptDiv #content").val("");
      }
  }
  function checkCommonContentOut(mutclick){
      var content = $("#senderpostscriptDiv #content").val();
      var defaultValue = $.i18n("collaboration.newcoll.fywbzyl");
      if (content == "") {
           $("#senderpostscriptDiv #content").val(defaultValue);
           $("#senderpostscriptDiv #content").css("color","#a3a3a3");
        }else{
        	if(!mutclick){
        		$("#senderpostscriptDiv #content").css("color","#111");
        	}
      }
  }
  
  /**
   * ---------------------AJax异步加载意见--------------------------------------------------
   */
  var commentParam = {};

  var _pageSize = "500";
  var _queryType =  "0";
  var _currentPage = "1";
  var _forwardPage = "1";
  
  commentParam.isHistory = isHistoryFlag;
  commentParam.moduleId= _summaryId;
  commentParam.page = _currentPage;
  commentParam.pageSize =_pageSize;
  commentParam.queryType = _queryType;
  commentParam.forwardMember = forwardMember;
  commentParam.replyCounts = replyCounts;
  commentParam.affairId = contentAffairId;
  
  var allCommentCountMap = null;
  var allPraiseCountMap = null;
  
  var hasForwardMore = true;
  var hasCurrentMore = true;
  
  var isCurrentAnchorComment  = false;
  var forwardAreaHasMore = true;
  function loadNextPageComment(type){
      /**
       * 当前意见是锚点的意见、隐藏锚点意见、然后正常加载其他的意见
       */
      if(isCurrentAnchorComment && type == '2'){
          //$("#currentComment .reply_data").empty();
          $("#currentComment .content").empty();
          var $obj= $("#replyContent_sender_content  .replyContent_sender_content_ul");
          $obj.find(".ui_print_li_borderBottom").each(function(){
        	  $(this).remove();
          });
          fileUploadAttachments = new Properties();
          isCurrentAnchorComment = false;
      }
      initMoreComment(type);
  }
  
  /* queryType 查当前意见还是转发意见[ 0|全部，1|转发，2|当前]
   * needLoadAll 是否需要加载全部
   * callBack 全部意见加载完成后的回调
   */
  
  function initMoreComment(queryType,needLoadAll,callBack) {
     if (tps != "") {
        _pageSize = tps;
        commentParam.pageSize =_pageSize;
    }
    // 记载意见区 1:取模板 2：取数据 3：填充数据
    var tpl = document.getElementById("comTemp").innerHTML;
    if (queryType == "0") {// 首屏加载
        commentParam.page = "1";
        commentParam.queryType = "0";
        if(typeof(contentAnchor)!='undefined' && contentAnchor){
            commentParam.anchorCommentId  = contentAnchor;
        }
        callBackendMethod("colManager", "findsSummaryComments", commentParam, {
            success : function(data) {
                
                _forwardPage = (parseInt(_forwardPage) + 1) + "";
                if (typeof(contentAnchor)!='undefined' && contentAnchor) {
                    commentParam.anchorCommentId = "";
                    isCurrentAnchorComment = true;
                }
                else {
                    _currentPage = (parseInt(_currentPage) + 1) + "";
                }
                
                // 1:当前评论回复区
                laytpl(tpl).render(data.commentList || [], function(render) {
                    var html = $(render);
                    $("#currentComment .content").append(html);
                    html.find(".comp").each(function(i){
                        $(this).compThis();
                    })
                    
                });
                // 2：当前评论发起人附言
                var tplSender = document.getElementById("senderTpl").innerHTML;
                laytpl(tplSender).render(data.commentSenderList || [], function(render) {
                    // debugger;
                    var html = $(render);

                    if ($("#reply_sender").size() > 0) {
                        $("#reply_sender").before(html);
                    }
                    else {
                        $(".replyContent_sender_content_ul").append(html);
                    }
                    html.find(".comp").each(function(i){
                        $(this).compThis();
                    })
                    
                  
                });

                if (data.currentHasNext == "false") {
                    $("#curComMoreBtn").hide();
                    hasCurrentMore = false;
                }
                else{
                    $("#curComMoreBtn").show();
                }
                
                allPraiseCountMap = data.allPraiseCountMap;
                allCommentCountMap = data.allCommentCountMap;
                
                var forwardMap = data.commentForwardMap;
                var hasShow = false;
                for ( var forwardCount in forwardMap) {
                    if(!hasShow){  // 1.显示转发区顶层div
                        document.getElementById("forwardAllTopDIVTpl").style.display = "block";
                        hasShow = true;
                    }
                    appendForwadComment(forwardMap,forwardCount);
                }
                
                if (data.forwardHasNext == "false") {
                    $("#forwardCommentMoreBtn").hide();
                    hasForwardMore = false;
                }
                //缓存中转转发的keys
                commentParam.forwardCountKeys = data.forwardCountKeys;
                if(!allCommentCountMap["0"]){
                	allCommentCountMap["0"] = 0;
                }
                if(!canPraise){
                	$("#_commentInfo")[0].innerHTML = $.i18n('collaboration.opinion.handleOpinion1',allCommentCountMap["0"]);
                }else{
                	
                	$("#_commentInfo")[0].innerHTML = $.i18n('collaboration.opinion.handleOpinion',allCommentCountMap["0"],data.praiseToSumNum);
                }
                
        	    $("#comment_forward_region_btn").toggle(function(){
        	    	$("div[id^='fowardContainerDIV']").each(function(){
        		    	  $(this).hide();
        		      });
        	    	if($("#forwardCommentMoreBtn").css('display') == "none"){
        	    		forwardAreaHasMore = false;
        	    	}
        	    	$("#forwardCommentMoreBtn").hide();
        	      $("#comment_forward_region_btn").text($("#comment_forward_region_btn").attr("showTxt"));
        	    },function(){
        	      $("div[id^='fowardContainerDIV']").each(function(){
        	    	  $(this).show();
        	      });
        	      if(forwardAreaHasMore){
        	    	  $("#forwardCommentMoreBtn").show();
        	      }
        	      $("#comment_forward_region_btn").text($("#comment_forward_region_btn").attr("hideTxt"));
        	    });
                
        	    //隐藏进度条
        	    $("#commentloadProcess").hide();
            }
        });
    }
    else if (queryType == "2") {

       
       commentParam.page = _currentPage;
       commentParam.queryType = queryType;
       callBackendMethod("colManager", "findsSummaryComments", commentParam,{
           success : function(data) {
               if(data){
                   _currentPage = (parseInt(_currentPage) + 1) + "";
                   

                   // 1:当前发起人附言
                   if(data.commentSenderList){
                       var tplSender = document.getElementById("senderTpl").innerHTML;
                       var senderhtml =  laytpl(tplSender).render(data.commentSenderList || []);
                       function renderSenderList(senderhtml) {
                           var $html = $(senderhtml);
                           // $("#currentComment .content")[0].innerHTML = render;
                           $("#replyContent_sender_content .replyContent_sender_content_ul #reply_sender").before($html);
                           
                           $html.find(".comp").each(function(i){
                               $(this).compThis();
                           })
                           
                       } 
                       renderSenderList(senderhtml);
                   }
                   
                   // 1:当前评论回复区
                   if(data.commentList){
                       
                       var html =  laytpl(tpl).render(data.commentList || []);
                       function renderComment(render) {
                           var $html = $(render);
                           // $("#currentComment .content")[0].innerHTML = render;
                           $("#currentComment .content").append($html);
                           
                           $html.find(".comp").each(function(i){
                               $(this).compThis();
                           })
                           
                       } 
                       renderComment(html);
                   }
                   
                   

                   if (data.currentHasNext == "false") {
                       $("#curComMoreBtn").hide();
                       hasCurrentMore = false;
                       if(typeof(callBack)=="function"){
                    	   callBack();
                       }
                   }
                   else{
                       $("#curComMoreBtn").show();
                       if(needLoadAll){
                       		initMoreComment(queryType, needLoadAll,callBack);
                       }
                   }
                   
               } 
           }
       })
    }
    else if (queryType == "1") {
        
        commentParam.page = _forwardPage ;
        commentParam.queryType = queryType;
        
        callBackendMethod("colManager", "findsSummaryComments", commentParam,{
            success : function(data) {
                if(data){

                    _forwardPage = (parseInt(_forwardPage)+1) + "";
                    
                    // 1、当前评论回复区
                    var forwardMap = data.commentForwardMap; 
                    
                    // 2、排序
                    var arr = [];
                    for ( var forwardCount in forwardMap) {// key代表第几次转发中的数据
                        arr.push(forwardCount);
                    }
                    arr.sort();
                    
                    // 3、渲染
                    for(var i = 0 ; i < arr.length ; i++ ){
                        var forwardCount =  arr[i];
                        appendForwadComment(forwardMap,forwardCount);
                    }
                    if (data.forwardHasNext == "false") {
                        $("#forwardCommentMoreBtn").hide();
                        hasForwardMore = false;
                        if(typeof(callBack)=="function"){
                     	   callBack();
                        }
                    }else{
                    	if(needLoadAll){
                       		initMoreComment(queryType, needLoadAll,callBack);
                       }
                    }
                    
                
                }
            }
        });
    }
}
/**
 * 意见转发区
 * @param forwardMap  转发区的意见Map
 * @param forwardCount  转发的次数
 */
//4: 转发区回复
var forwardCommentDIVTpl = "";
var forwardCommentDATATpl = "";
//3: 转发区发起人附言
var forwardSenderDATATpl = "";
var forwardSenderDIVTpl = "";
var fowardDIVTpl = "";


function appendForwadComment(forwardMap,forwardCount){
    
      if(forwardCommentDIVTpl == ""){
          forwardCommentDIVTpl = document.getElementById("forwardCommentDIVTpl").innerHTML;
          forwardCommentDATATpl = document.getElementById("forwardCommentDATATpl").innerHTML;
          forwardSenderDATATpl = document.getElementById("forwardSenderDATATpl").innerHTML;
          forwardSenderDIVTpl = document.getElementById("forwardSenderDIVTpl").innerHTML;
          fowardDIVTpl = document.getElementById("fowardDIVTpl").innerHTML;
      }
      
      //key代表第几次转发中的数据

      var forwardCountMap = forwardMap[forwardCount];
      var fsenderList = forwardCountMap[-1];//-1代表是发起人附言
      var fcommentList = forwardCountMap[0];// 0代表是评论回复

      if (fsenderList != null || fcommentList != null) {

          var praiseCount = typeof(allPraiseCountMap[forwardCount])=='undefined' ? 0:allPraiseCountMap[forwardCount];  //赞数
          var commentCount = typeof(allCommentCountMap[forwardCount])=='undefined' ? 0:allCommentCountMap[forwardCount];  //意见数
          
          var param = {}
          param.forwardCount = forwardCount;
          param.forwardCountLable = $.i18n("collaboration.forward.oriOp.level.label",forwardCount);
          param.countLabel = $.i18n("collaboration.opinion.handleOpinion",commentCount,praiseCount);
          
          
          //第XXX次转发的区域是否存在
          var $domFowardContainerDIV = $("#fowardContainerDIV"+forwardCount);
          //第XXX次转发的意见区是否存在
          var $domForwardCommentDiv = $("#forwardCommentDiv"+forwardCount);
          //第XXX次转发的发起人附言区是否存在
          var $domForwardSenderDiv = $("#forwardSenderDiv"+forwardCount);
          
          var $forwardCommentDIVTpl = null;
          var $forwardSenderDIVTpl = null;
          var $fowardDIVTpl = null;
          
          
          var hasDomFowardDIVTpl = $domFowardContainerDIV.length != 0 
          if(!hasDomFowardDIVTpl){
              $fowardDIVTpl = $(laytpl(fowardDIVTpl).render(param || []));
              $domFowardContainerDIV = $fowardDIVTpl.find("#fowardContainerDIV" + param.forwardCount);
          }
          
          
          if (fsenderList != null) {
              
              
              var forwardSenderDATAHtml = laytpl(forwardSenderDATATpl).render(fsenderList || []);
              var $forwardSenderDATAHtml = $(forwardSenderDATAHtml);
              
              var hasDomForwardSenderDiv = $domForwardSenderDiv.length != 0;
              if(!hasDomForwardSenderDiv){
                  var forwardSenderDIVTplHtml = laytpl(forwardSenderDIVTpl).render(param || []);
                  $forwardSenderDIVTpl = $(forwardSenderDIVTplHtml);
                  $domForwardSenderDiv = $forwardSenderDIVTpl.find("#forwardSenderDiv"+forwardCount);
              }
             
            
              $domForwardSenderDiv.append($forwardSenderDATAHtml);
              
              if(!hasDomForwardSenderDiv){
                  $domFowardContainerDIV.append($forwardSenderDIVTpl);
              }
              
              if(!hasDomFowardDIVTpl){
                  $("#commentForwardDiv").append($fowardDIVTpl);
              }
              
              $forwardSenderDATAHtml.find(".comp").each(function(i){
                  $(this).compThis();
              })
          }
          
          if( fcommentList != null){
             
              var forwardCommentDATAHtml = laytpl(forwardCommentDATATpl).render(fcommentList || []);
              var $forwardCommentDATAHtml = $(forwardCommentDATAHtml)
              
              var hasDomForwardCommentDiv = $domForwardCommentDiv.length != 0 
              if(!hasDomForwardCommentDiv){
                  var forwardCommentDIVTplHtml = laytpl(forwardCommentDIVTpl).render(param || []);
                  $forwardCommentDIVTpl = $(forwardCommentDIVTplHtml);
                  $domForwardCommentDiv = $forwardCommentDIVTpl.find("#forwardCommentDiv" + param.forwardCount)
              }
              
              $domForwardCommentDiv.append($forwardCommentDATAHtml);
              
              if(!hasDomForwardCommentDiv){
                  $domFowardContainerDIV.append($forwardCommentDIVTpl);
              }
              if(!hasDomFowardDIVTpl){
                  $("#commentForwardDiv").append($fowardDIVTpl);
              }
              $forwardCommentDATAHtml.find(".comp").each(function(i){
                  $(this).compThis();
              })
          }

      }
  
  
}  

/**
 * 加载所有的意见(转发和全部的意见获取方式改成了异步的方式，后续执行的方法需要回调中执行。否则意见会没加载完)
 */
function loadAllComments(callBack){
    //if(转发更多可以看到)
    if(hasForwardMore){
    	//转发获取完成后再获取未显示的意见
        initMoreComment('1',true,function(){
        	loadAllCurrentComment(callBack);
        });
        
    }else{
    	loadAllCurrentComment(callBack);
    }
}

function loadAllCurrentComment(callBack){
	//直接获取更多意见
	if(hasCurrentMore){
		initMoreComment('2',true,callBack);
	}else{
		//首屏数据已经加载完成后需要直接调用回调否则打印不了
		if(typeof(callBack)=="function"){
     	   callBack();
        }
	}
}

/**
 * 获取流程当前会签的人员
 * @returns {Array}
 */
function findAssignNodeMember(){
	var processChangeMessage = parent.$("#processChangeMessage").val();
	var assignNodeMember = new Array();
	if(processChangeMessage){
		var processChangeMessageJson = $.parseJSON(processChangeMessage);
		var addNodeInfos = processChangeMessageJson["nodes"];
		if(addNodeInfos){
			for(var i = 0;i<addNodeInfos.length;i++){
				var addNodeInfo  = addNodeInfos[i];
				//当前会签的是人员时才进行解析
				if(addNodeInfo["fromType"]=="3" && addNodeInfo["eleType"]=="user"){
					var memeber = {};
					memeber["memberId"] = addNodeInfo["eleId"];
					memeber["name"] = addNodeInfo["eleName"];
					memeber["state"] = addNodeInfo["eleName"];
					memeber["id"] = "activity|"+addNodeInfo["id"];
					memeber["i18n"] = "";
					assignNodeMember.push(memeber);
				}
			}
		}
	}
	return assignNodeMember;
}
  
  
  
  
  
  
  
  
  