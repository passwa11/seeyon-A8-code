
var edocDetailURL = _ctxPath + "/edocController.do";
var colURL = _ctxPath + "/collaboration/collaboration.do";
var mtMeetingUrl = _ctxPath + "/mtMeeting.do";
var docURL = _ctxPath + "/doc.do";
var isGovdocForm = "1";
var waterMarkBase64  = "";

var isLoadFormMark = true;

$(function(){
	try {
		if(parent && parent.location.href.indexOf("template.do")>=0) {
			isLoadFormMark = false;
		}
		if(parent && parent.location.href.indexOf("govdoc.do")<0) {
			isLoadFormMark = false;
		}
		if($("#moduleType").val() != "4") {
			isLoadFormMark = false;
		}

		if(isLoadFormMark==true) {
				formLoadCallback();
		}

		//G6新公文改动
		if("undefined" != typeof parent.parent.opinions) {
			if(typeof(parent.parent.isGovdocForm)!='undefined' && parent.parent.isGovdocForm=='1') {//公文单
				dispOpinionsConflict(parent.parent.opinions,parent.parent.senderOpinion);
				showSingImg(parent.parent.summaryId);
				if(typeof(resizeContentIframeHeightForform) != "undefined") {
					resizeContentIframeHeightForform(true);
				}
				initMutView(parent.parent.opinions);
			} else {
				dispOpinionsConflictInfo(parent.parent.opinions,parent.parent.senderOpinion);
			}
		}
	} catch(e) {
		alert(e);
	}
});
//显示文单签章图片
function showSingImg(summaryId){
	var ajaxManager = new govdocAjaxManager();
	var signs = ajaxManager.getSignaturebySummaryId(summaryId);
	if(signs.length >0){
		for(var i=0;i<signs.length;i++){
			var param = signs[i];
			param.enabled = 0;
			param.objName = param.fieldName;
			param.recordId = param.summaryId;
			var insertObj = getActorNameByObjName(param.objName);
			if (typeof insertObj == "string") {
				if (opinionSpans == null) {
					initSpans();
				}
				inputObj = opinionSpans.get(insertObj, null);
			}
			if (inputObj == null) {
				alert(v3x.getMessage("V3XOfficeLang.alert_noHandWriteLocation"));
				return;
			}
			var divObj=$(inputObj).find("#"+param.affairId);// 获取意见div对象
			if(divObj.length >0){
				param.signObj = divObj[0];
				var hwObj = initHandWriteData(param);
				//src地址需要重新拼装
				hwObj.src = webRoot+"/signatPicController.do?method=writeGIF&RECORDID="+param.recordId+"&FIELDNAME="+ encodeURIComponent(param.objName || "")
				  +"&isNewImg=true&affairId="+param.affairId+"&r="+Math.random();
				//zhou:签批图片比列缩放
				hwObj.style.maxWidth="30%";
				divObj[0].style.height="auto"||"100%";
			}
		}
	}

}
function initMutView(opinions){
	var formContainer = $("body");
	if($("body").find("#viewsTabs").find("li").size() > 1) {
		for(i=0;i<opinions.length;i++) {
			var fieldId=opinions[i][0];
			var spanOp = formContainer.find($("span#"+fieldId));
			if(spanOp.size()>1){
				var wholeHtml = "";
				var curIndex = 0;
				for(var j=0;j<spanOp.size();j++){
					var curOp = spanOp.get(j);
					if(curOp.innerHTML.indexOf("showV3XMemberCard")>0){
						wholeHtml = curOp.innerHTML;
						curIndex = j;
						break;
					}
				}
				for(var j=0;j<spanOp.size();j++){
					if(j!=curIndex && wholeHtml && wholeHtml!=null){
						var curOp = spanOp.get(j);
						curOp.innerHTML = curOp.innerHTML + wholeHtml;
						curOp.style.height = "auto";
					}
				}
			}
		}
	}
}

$(window).unload(function(){
	if(isLoadFormMark==true) {
		reloadGovdocForm();
	}
});
var i = 0;
function formLoadCallback() {
	i++;
	if(i > 5){
		i = 0;
		return;
	}
	if(parent.formLoadCallback) {
		parent.formLoadCallback();
	} else if(parent.parent.formLoadCallback) {
		parent.parent.formLoadCallback();
	}
}

var j = 0;
function reloadGovdocForm() {
	j++;
	if(j > 5){
		j = 0;
		return;
	}
	if(typeof(parent.reloadGovdocForm) != "undefined") {
		parent.reloadGovdocForm();
	} else if(typeof(parent.parent.reloadGovdocForm) != "undefined") {
		parent.parent.reloadGovdocForm();
	}
}

function selectChangeCallBack(obj) {
	if(parent.selectChangeCallBack) {
		parent.selectChangeCallBack(obj);
	} else if(parent.parent.selectChangeCallBack) {
		parent.parent.selectChangeCallBack(obj);
	}
}

function validateBaseMark(obj,param){
	if(parent.parent.isChangeView){
		parent.parent.isChangeView = false;
		return true;
	}
	if(parent.validateBaseMark) {
		return parent.validateBaseMark(obj, param);
	} else if(parent.parent.validateBaseMark) {
		return parent.parent.validateBaseMark(obj, param);
	}
	return true;
}


//G6新公文改动
function changeFontsize(opinion,spanObj){
	try {
		if(spanObj!=null&&spanObj!=undefined){
			//修改字体大小
          if (spanObj.style.fontSize != null && spanObj.style.fontSize != "") {
          	opinion = _addStyle(opinion, "span", "font-Size:"+spanObj.style.fontSize);
          	opinion = _addStyle(opinion, "div", "font-Size:"+spanObj.style.fontSize);
          }
          //修改字体样式
          if (spanObj.style.fontFamily != null && spanObj.style.fontFamily != "") {
              opinion = _addStyle(opinion, "div", "font-family:" + spanObj.style.fontFamily);
              opinion = _addStyle(opinion, "span", "font-family:" + spanObj.style.fontFamily);
          }
		}
		return opinion;
  } catch (e) {}
}

//G6新公文改动
function insertAtt_callBack(a,b) {
	try{
		if(v3x.isMSIE8){//IE8公文处理意见 上传附件显示有问题 jira GOVA-2560 chenx
			var attdiv=$("#attachmentAreayijian");
			if(attdiv.length>0){
				var sp = attdiv.parents("span .xdRichTextBox:eq(0)");
				if(sp.length==1){
					sp.css("height", "");
					sp.css("height", "auto");
				}
			}
		}
	}catch(e){}
	resizeContentIframeHeightForform(true);
}

//G6新公文改动
function showOpinionsInputGovForm() {
	var att ="";
	if(typeof(parent.parent.handleAttachJSON)=='object'){
		att = getCtpTop().JSON.stringify(parent.parent.handleAttachJSON);
	}
	if(parent.parent.fileUploadOpinionAttachments!=''){
		att = parent.parent.fileUploadOpinionAttachments;
	}
	//删除附件和关联文档的onclick事件替换
	try{
	var str1="<div id=\"attachmentTRyijian\" style=\"display: none;\">"+
		            "<div id=\"deal_attach1\" isGrid=\"true\" class=\"comp\" comp=\"type:'fileupload',callMethod:'insertAtt_callBack',takeOver:'false',newGovdocView:1,attachmentTrId:'yijian',canFavourite:false,applicationCategory:'4',canDeleteOriginalAtts:true,checkSubReference:false\" attsdata='"+att+"' > </div>"+
		        "</div>";
	var str2 ="<div id=\"attachment2TRyijian\" style=\"display: none;\">"+
				"<div id=\"deal_attdoc1\" isGrid=\"true\" class=\"comp\" comp=\"type:'assdoc',newGovdocView:1,callMethod:'insertAtt_callBack',displayMode:'auto',takeOver:'false',attachmentTrId:'yijian',canFavourite:false,applicationCategory:'4',canDeleteOriginalAtts:true,checkSubReference:false\" attsdata='"+att+"' > </div>"+
	    	"</div>";
	}catch(e){};
	//处理态度
	var str3="";
	if(parent.parent.switchObj.canShowAttitude){
		try{
			str3= parent.parent.document.getElementById("processAttitude").innerHTML;
		}catch(e){}
	}
	var cvalue="";
	var processHTML="";
	if(parent.parent.switchObj.canShowCommonPhrase) {
		processHTML+='<div oncontextmenu="return false"';
		processHTML+='style="position:absolute; right:350px; top:120px; width:260px; height:160px; z-index:2; background-color: #ffffff;display:none;overflow:no;border:1px solid #000000;"';
		processHTML+='id="divPhrase" onmouseover="showPhrase_gov()" onmouseout="hiddenPhrase()" oncontextmenu="return false">';
		processHTML+='<IFRAME width="100%" id="phraseFrame" name="phraseFrame" height="100%" frameborder="0" align="middle" scrolling="no"';
		processHTML+='marginheight="0" marginwidth="0"></IFRAME>';
		processHTML+='</div>';
	}
	processHTML+="<div id='govdocDealArea' style='white-space:normal;font-size:10pt;font-family: SimSun;'>";
	processHTML+="<li style='padding: 0'><div class='edoc_deal' ><div class='edoc_deal_div' style='position:relative;word-break: keep-all;'>";
	if(parent.parent.switchObj.canShowAttitude) {
		processHTML+=str3;
	}

	var atRight = 2;
	if(parent.parent.switchObj.canShowCommonPhrase) {
		processHTML+="<a onclick='javascript:zwFrameShowphrase("+cvalue+")' id='cUseP'>";
		processHTML+="<span class='dealicons commonPhrase'></span>";
		processHTML+=$.i18n('commonPhrase.label')+"</a>";
	}
	try{
		var cphrase = parent.parent.document.getElementById("cphrase");
		cvalue = cphrase.attributes["curUser"].nodeValue;
	}catch(e){}
	processHTML+="<span class='ico24 at_24' id='atGovdoc' onclick='javascript:pushMessageFunc("+cvalue+")' style='font-size:12px;'>";
	processHTML+="</span>";
	atRight = 40;
	processHTML+="</div></div></li>";

	if(parent.parent.switchObj.canShowOpinion) {
	   processHTML+="<li><div height='100%' >";
	   processHTML+="<textarea id='contentOP' name='contentOP' rows='10' style='width: 99%;height:132px;white-space:pre-wrap;' maxSize='1000' validate='maxLength'>"+parent.parent.$("#content_deal_comment").val()+"</textarea>";
	   processHTML+="</div></li>";
	}
	if(parent.parent.switchObj.canUploadAttachment || parent.parent.switchObj.canUploadRel) {
		processHTML+="<li><div height='30' style='padding: 0px 10px;'>";
	}
	if(parent.parent.switchObj.canUploadAttachment) {
		processHTML+="<span onClick=\"insertAttachmentPoi('yijian')\" class=\"ico16 affix_16\" title="+$.i18n("govdoc.upload.attachment")+"></span>(<span id=\"attachmentNumberDivyijian\">0</span>)";
	}
	if(parent.parent.switchObj.canUploadRel) {
		processHTML+="<span onClick=\"quoteDocument('yijian')\" class=\"ico16 associated_document_16\"></span>(<span id=\"attachment2NumberDivyijian\">0</span>)";
	}
	if(parent.parent.switchObj.canUploadAttachment || parent.parent.switchObj.canUploadRel) {
		processHTML+="</div></li>";
		processHTML+="<li><div><div id='processatt1'>"+str1+"</div><div id='processatt2'>"+str2+"</div></div></li>";
	}
	processHTML+="</div>";

	return processHTML;
}

function pushMessageFunc() {
	parent.parent.pushMessageFunc();
}

function showHideFunc(){
	if($(".default_handle").css("display")=="block"){
		$(".default_handle").hide();
		$(".showHide").find(".ico16").removeClass("arrow_2_t").addClass("arrow_2_b");
		$(".showHide").find(".color_blue").text($.i18n("collaboration.summary.label.open.js"));
	}else{
		$(".default_handle").show();
		$(".showHide").find(".ico16").removeClass("arrow_2_b").addClass("arrow_2_t");
		$(".showHide").find(".color_blue").text($.i18n("collaboration.summary.label.close.js"));
	}
}
function attDivToggle() {
	if($(".upload_files_msg").css("display")=="none") {
		$(".upload_files_msg").show();
	} else {
		$(".upload_files_msg").hide();
	}
}
//重新加载附件显示
function reloadParentAtt(){
	try{
	var str1=parent.parent.document.getElementById("attachmentArea").innerHTML;
	var str2=parent.parent.document.getElementById("attachment2Area").innerHTML;
	str1=str1.replace(/deleteAttachment/g,'deleteParentAtt');
	str2=str2.replace(/deleteAttachment/g,'deleteParentAtt');
	document.getElementById("processatt1").innerHTML=str1;
	document.getElementById("processatt2").innerHTML=str2;
	}catch(e){}

}
//删除附件并重新加载插件显示
function deleteParentAtt(fileurl){
	parent.parent.deleteAttachment(fileurl);
	reloadParentAtt();
}

//重新加载附件显示
function reloadParentAtt(){
	try{
	var str1=parent.parent.document.getElementById("attachmentArea").innerHTML;
	var str2=parent.parent.document.getElementById("attachment2Area").innerHTML;
	str1=str1.replace(/deleteAttachment/g,'deleteParentAtt');
	str2=str2.replace(/deleteAttachment/g,'deleteParentAtt');
	document.getElementById("processatt1").innerHTML=str1;
	document.getElementById("processatt2").innerHTML=str2;
	}catch(e){}

}
//删除附件并重新加载插件显示
function deleteParentAtt(fileurl){
	parent.parent.deleteAttachment(fileurl);
	reloadParentAtt();
}

//重新加载附件显示
function reloadParentAtt(){
	try{
	var str1=parent.parent.document.getElementById("attachmentArea").innerHTML;
	var str2=parent.parent.document.getElementById("attachment2Area").innerHTML;
	str1=str1.replace(/deleteAttachment/g,'deleteParentAtt');
	str2=str2.replace(/deleteAttachment/g,'deleteParentAtt');
	document.getElementById("processatt1").innerHTML=str1;
	document.getElementById("processatt2").innerHTML=str2;
	}catch(e){}

}
//删除附件并重新加载插件显示
function deleteParentAtt(fileurl){
	parent.parent.deleteAttachment(fileurl);
	reloadParentAtt();
}

//展示常用语
function ShowphraseC(str,id) {
	/** 异步调用 */
    var phraseBean = [];
    callBackendMethod("phraseManager","getAllPhrases",{

        success : function(phraseBean) {
        	//特殊操作，常用语和节点说明回写冲突
              var phrasecontent = [];
              var phrasepersonal = [];
              for (var count = 0; count < phraseBean.length; count++) {
                  phrasecontent.push(phraseBean[count].content);
                  if (phraseBean[count].memberId == str && phraseBean[count].type == "0") {
                      phrasepersonal.push(phraseBean[count]);
                  }
              }

            var width, height, inputType;
            var contentCkeditor = CKEDITOR.instances[id];
            if(contentCkeditor==null){
            	width = $("#"+id).innerWidth()-4;
            	height = $("#"+id).innerHeight()-2;
            	inputType = "textarea";
            }else{
            	width = $("#cke_"+id).innerWidth()-4;
            	height = $("#cke_"+id).innerHeight()-2;
            	inputType = "ckeditor";
            }
            _top = $("#cUseP").offset().top;
            _left = $("#"+id).offset().left+4;
            if(typeof(jQuery.fn.comLanguage) === "function") {
              showphraseFun(phrasecontent,inputType,width,height,phrasepersonal,_top,_left,id);
            }else{
              var _comLanguageJSURL = _ctxPath + "/common/js/ui/seeyon.ui.comLanguage-debug.js";
              $.getScript(_comLanguageJSURL, function(_result, _textStatus, _jqXHR) {
                  //请求过来的JS文件实际为一串string，需要eval一下
                  eval(_result);
                  showphraseFun(phrasecontent,inputType,width,height,phrasepersonal,_top,_left,id);
              });
            }
            },
            error : function(request, settings, e) {
                $.alert(e);
            }

    });
}
function zwFrameShowphrase(str) {
	//var pManager = new phraseManager();
    /** 异步调用 */
    var phraseBean = [];
    callBackendMethod("phraseManager","getAllPhrases",{

        success : function(phraseBean) {
        	//特殊操作，常用语和节点说明回写冲突
              var phrasecontent = [];
              var phrasepersonal = [];
              for (var count = 0; count < phraseBean.length; count++) {
                  phrasecontent.push(phraseBean[count].content);
                  if (phraseBean[count].memberId == str && phraseBean[count].type == "0") {
                      phrasepersonal.push(phraseBean[count]);
                  }
              }

            var width, height, inputType;
            var contentCkeditor = CKEDITOR.instances["contentOP"];
            if(contentCkeditor==null){
            	width = $("#contentOP").innerWidth()-4;
            	height = $("#contentOP").innerHeight()-2;
            	inputType = "textarea";
            }else{
            	width = $("#cke_contentOP").innerWidth()-4;
            	height = $("#cke_contentOP").innerHeight()-2;
            	inputType = "ckeditor";
            }

            _top = $("#cUseP").offset().top;
            _left = $("#contentOP").offset().left+4;
            if(height<200){//OA-169921 公文文单中处理时，常用语弹出界面显示过短，影响客户使用体验
            	height = 200;
            }

            if(typeof(jQuery.fn.comLanguage) === "function") {
              showphraseFun(phrasecontent,inputType,width,height,phrasepersonal,_top,_left);
            }else{
              var _comLanguageJSURL = _ctxPath + "/common/js/ui/seeyon.ui.comLanguage-debug.js";
              $.getScript(_comLanguageJSURL, function(_result, _textStatus, _jqXHR) {
                  //请求过来的JS文件实际为一串string，需要eval一下
                  eval(_result);
                  showphraseFun(phrasecontent,inputType,width,height,phrasepersonal,_top,_left);
              });
            }
            },
            error : function(request, settings, e) {
                $.alert(e);
            }

    });
}

function showphraseFun(phrasecontent,inputType,width,height,phrasepersonal,_top,_left,textboxId) {
	var tId = "contentOP";
	if(textboxId!=undefined){
		tId = textboxId;
	}
	if(height<150){
		height=150;
	}
    $("#cUseP").comLanguage({
          textboxID : tId,
          data : phrasecontent,
          inputType:inputType,
          width:width,
          height:height-2,
          top:_top,
          left:_left,
          newBtnHandler : function(phraseper) {
              $.dialog({
                  url : _ctxPath + '/phrase/phrase.do?method=gotolistpage',
                  transParams : phrasepersonal,
                  width: 600,
                  height: 400,
                  targetWindow:top,
                  title : $.i18n('collaboration.sys.js.cyy')
              });
          }
      });
}
//函数名称冲突，改名
function initSpansConflict()
{

var i,key;
var spanObjs=document.getElementsByTagName("span");
opinionSpans=new Properties();
for(i=0;i<spanObjs.length;i++)
{
key=spanObjs[i].getAttribute("id");
if(key!=null)
{
	//spanObjs[i].style.fontSize="15px";
  //记录处理意见录入框的初始化大小，确定手写签批对话框大小;
	if(spanObjs[i].currentStyle) {
		spanObjs[i].initWidth=spanObjs[i].currentStyle.width;
	    spanObjs[i].initHeight=spanObjs[i].currentStyle.height;
	} else {
		spanObjs[i].initWidth = getComputedStyle(spanObjs[i],false).width;
		spanObjs[i].initWidth = getComputedStyle(spanObjs[i],false).width;
	}
  opinionSpans.put(key,spanObjs[i]);
}
}
}


//修改人：张东  2017-03-29———显示意见处理输入栏在相应意见位置 --end


function dispOpinionsConflictInfo(opinions,senderOpinion) {
	try {
		if("undefined" != typeof categoryApp && categoryApp!=null && categoryApp=="info"){
			//如果是信息报送查看页面，则不进行意见显示处理了，防止处理意见区会重复出现文单上的意见
			return;
		}

		var i;
		var otherOpinion = "";
		var spanObj;
		var isboundSender = false;
		if(opinionSpans == null) { initSpansConflict(); }

		//附件文字比较长时，字体重叠我呢体OA-66099
		var replaceHeight = /<div([^<]*?)attachmentDiv([^<]*?)style[ ]*?=(['"]{1})(.*?)['"]{1}([^<]*?)>/ig;
		var linkCss = /<a([^<]*?style[ ]*?=[ ]*?['"].*?)(['"].*?)>/ig;//关联文档a标签设置了不换行

		/** 显示意见 **/
		for(i=0;i<opinions.length;i++) {
			var fieldId=opinions[i][0];
			var fieldSpan=$("#"+fieldId);
			//隐藏处理意见
			if(fieldSpan&&fieldSpan.text().trim()=="*"){
				continue;
			}
			if(opinions[i][0] =="niwen" || opinions[i][0] == 'dengji' ) { isboundSender = true; }
			spanObj = opinionSpans.get(opinions[i][0], null);
			opinions[i][1] = changeFontsize(opinions[i][1], spanObj);
			if(spanObj==null||spanObj==undefined) {
				if(otherOpinion!=""){otherOpinion+="<br>";}
				var tempInnerHTML = (opinions[i][1]).replace(replaceHeight, "<div$1attachmentDiv$2style=$3$4height:auto;line-height:normal;float: none;$3$5>");
				tempInnerHTML = tempInnerHTML.replace(/noWrap/ig, "");
				tempInnerHTML = tempInnerHTML.replace(linkCss, "<a$1;white-space:normal;line-height:normal;$2>");
				otherOpinion+=tempInnerHTML;
		    } else {
		    	  var tempInnerHTML = (opinions[i][1]).replace(replaceHeight, "<div$1attachmentDiv$2style=$3$4height:auto;line-height:normal;float: none;$3$5>");
		    	  tempInnerHTML = tempInnerHTML.replace(/noWrap/ig, "");
		    	  tempInnerHTML = tempInnerHTML.replace(linkCss, "<a$1;white-space:normal;line-height:normal;$2>");
			      spanObj.innerHTML=tempInnerHTML;
			      //  spanObj.title=spanObj.innerText;
			      //spanObj.style.height="auto"||"100%";
			      spanObj.style.border="0px";
			      spanObj.contentEditable="false";
			      spanObj.style.minHeight = "0px";
			      spanObj.style.whiteSpace = "normal";
			      //spanObj.style.overflowY ="auto";
		    }
		}
		/** 其它意见框 **/
		spanObj=opinionSpans.get("otherOpinion", null);
		if(otherOpinion!="" && spanObj == null) {
			//OA-33421  test01在公文督办中查看公文，发起人附言和处理意见没做区分，应在意见前面显示处理意见
			var infoFlag = document.getElementById("infoFlag");//信息报送  不需要在下面显示意见
			if(!infoFlag){
				document.getElementById("dealOpinionTitleDiv").style.display = '';
				spanObj = document.getElementById("displayOtherOpinions");
				if(spanObj!=null) {
					spanObj.innerHTML=otherOpinion;
					spanObj.style.visibility="visible";
				   // spanObj.style.height="100%";
					spanObj.style.whiteSpace = "normal";
					spanObj.contentEditable="false";
					spanObj.style.border="0px";
				}
			}
		}

		/** 发起人意见 **/
		spanObj=opinionSpans.get("niwen",null);
		if(spanObj==null){spanObj=opinionSpans.get("dengji",null);}
		//当有登记意见和拟文意见，则为登记意见（实际的情况是不出现登记和拟文意见同时出现的情况）
		if(opinionSpans.get("niwen",null)!=null&&opinionSpans.get("dengji",null)!=null){
			spanObj=opinionSpans.get("dengji",null);
		}
		if(spanObj!=null && senderOpinion!=null && senderOpinion!="") {
			//spanObj.innerHTML=senderOpinion;
			//spanObj.style.height="100%";
			//spanObj.style.whiteSpace = "normal";
			//公文单上面有意见显示位置时，隐藏公文单下面的意见
			spanObj=document.getElementById("displaySenderOpinoinDiv");
			if(spanObj!=null && isboundSender ){spanObj.innerHTML="";}
		}
		if(senderOpinion=="") {//没有发起意见,或者发起意见意见绑定到其它显示位置;
			spanObj=document.getElementById("displaySenderOpinoinDiv");
			if(spanObj!=null){spanObj.innerHTML="";}
		}
	}catch(e){}
}
var opinionSpans = null;
//函数名称冲突，改名
function dispOpinionsConflict(opinions,senderOpinion) {
	try {
		var i;
		var otherOpinion = "";
		var spanObj;
		var isboundSender = false;
		if(opinionSpans == null) { initSpansConflict(); }
		var processHtml=showOpinionsInputGovForm();
		var initCount=0;
		//附件文字比较长时，字体重叠我呢体OA-66099
		var replaceHeight = /<div([^<]*?)attachmentDiv([^<]*?)style[ ]*?=(['"]{1})(.*?)['"]{1}([^<]*?)>/ig;
		var linkCss = /<a([^<]*?style[ ]*?=[ ]*?['"].*?)(['"].*?)>/ig;//关联文档a标签设置了不换行
		/** 显示意见 **/
		for(i=0;i<opinions.length;i++) {
			var fieldId=opinions[i][0];
			var fieldSpan=$("#"+fieldId);
			//隐藏处理意见
			if(fieldSpan&&fieldSpan.text().trim()=="*"){
				continue;
			}
			if(opinions[i][0] =="niwen" || opinions[i][0] == 'dengji' ) { isboundSender = true; }
			spanObj = opinionSpans.get(opinions[i][0], null);
			opinions[i][1] = changeFontsize(opinions[i][1], spanObj);
			if(spanObj==null||spanObj==undefined) {
				if(otherOpinion!=""){otherOpinion+="<br>";}
				var tempInnerHTML = (opinions[i][1]).replace(replaceHeight, "<div$1attachmentDiv$2style=$3$4height:auto;line-height:normal;float: none;$3$5>");
				tempInnerHTML = tempInnerHTML.replace(/noWrap/ig, "");
				tempInnerHTML = tempInnerHTML.replace(linkCss, "<a$1;white-space:normal;line-height:normal;font-size:14px;$2>");
				otherOpinion+=tempInnerHTML;
		    } else {
		    	  var tempInnerHTML = (opinions[i][1]).replace(replaceHeight, "<div$1attachmentDiv$2style=$3$4height:auto;line-height:normal;float: none;$3$5>");
		    	  tempInnerHTML = tempInnerHTML.replace(/noWrap/ig, "");
		    	  tempInnerHTML = tempInnerHTML.replace(linkCss, "<a$1;white-space:normal;line-height:normal;font-size:14px;$2>");
		    	  //追加意见框至文单中 zhangdong 20170329 start
		    	  if($("#"+opinions[i][0]+"_span").hasClass("edit_class")&&initCount==0&&parent.parent.switchObj.allowCommentInForm){
		    		  tempInnerHTML=tempInnerHTML;
		    		  initCount++;
		    	  }
		    	  //追加意见框至文单中 zhangdong 20170329 end
		    	  spanObj.innerHTML=tempInnerHTML;
		    	  parent.parent.$("#opinionDivGetVal").append("<span style='white-space:wrap' id='opinion_"+fieldId+"'>"+tempInnerHTML+"</span>");
			      //  spanObj.title=spanObj.innerText;
			      spanObj.style.height="auto";
			      spanObj.style.border="0px";
			      spanObj.contentEditable="false";
			      spanObj.style.minHeight = "0px";
			      //spanObj.style.overflowY ="auto";
		    }
		}
		/** 其它意见框 **/
		spanObj=opinionSpans.get("otherOpinion", null);
		if(otherOpinion!="" && spanObj == null) {
			//OA-33421  test01在公文督办中查看公文，发起人附言和处理意见没做区分，应在意见前面显示处理意见
			var infoFlag = document.getElementById("infoFlag");//信息报送  不需要在下面显示意见
			if(!infoFlag){
				try{
					document.getElementById("dealOpinionTitleDiv").style.display = '';
				}catch(e){}
				spanObj = document.getElementById("displayOtherOpinions");
				if(spanObj!=null) {
					spanObj.innerHTML=otherOpinion;
					spanObj.style.visibility="visible";
				   // spanObj.style.height="100%";
					spanObj.style.whiteSpace = "normal";
					spanObj.contentEditable="false";
					spanObj.style.border="0px";
				}
			}
		}

		/** 发起人意见 **/
		spanObj=opinionSpans.get("niwen",null);
		if(spanObj==null){spanObj=opinionSpans.get("dengji",null);}
		//当有登记意见和拟文意见，则为登记意见（实际的情况是不出现登记和拟文意见同时出现的情况）
		if(opinionSpans.get("niwen",null)!=null&&opinionSpans.get("dengji",null)!=null){
			spanObj=opinionSpans.get("dengji",null);
		}
		if(spanObj!=null && senderOpinion!=null && senderOpinion!="") {
			//spanObj.innerHTML=senderOpinion;
			//spanObj.style.height="100%";
			//spanObj.style.whiteSpace = "normal";
			//公文单上面有意见显示位置时，隐藏公文单下面的意见
			spanObj=document.getElementById("displaySenderOpinoinDiv");
			if(spanObj!=null && isboundSender ){spanObj.innerHTML="";}
		}
		if(senderOpinion=="") {//没有发起意见,或者发起意见意见绑定到其它显示位置;
			spanObj=document.getElementById("displaySenderOpinoinDiv");
			if(spanObj!=null){spanObj.innerHTML="";}
		}

		var formContainer = $("body");
		if($("body").find("#viewsTabs").find("li").size() > 1) {
			$("body").find("#viewsTabs").find("li").each(function () {
				if($(this).hasClass("current")) {
					var index = $(this).attr("index");
					formContainer = formContainer.find("#mainbodyHtmlDiv_" + index);
				}
			});
		}
		//如果没有已经填写的意见则查找可编辑控件进行意见框渲染  zhangdong 20170329 start
		var initShowDivCount=0;
		//判断只有允许在文单里面编辑才显示
		if(parent.parent.switchObj.allowCommentInForm){
			var strs= new Array(); //定义一数组
			strs=parent.parent.ols.split(","); //字符分割

			for(k=0;k<strs.length;k++) {
				if(formContainer.find("#"+strs[k]+"_span").hasClass("edit_class")){
					//先保存元素中原来的html
					initShowDivCount=1;

					try{
						if(document.documentMode ==7){
							document.all(strs[k],1).innerHTML=processHtml+document.all(strs[k],1).innerHTML;
						}else{
							formContainer.find($("span#"+strs[k])).prepend(processHtml);
						}
						$("#deal_attach1").comp();
						$("#deal_attdoc1").comp();
					}catch(e){
						alert($.i18n("govdoc.comments.loading.error"))
					}
					formContainer.find($("span#"+strs[k])).css({"width":"99%","margin-bottom":"5px"});
					break;
				}
			}

		}

		//如果设置的节点权限没有找到可编辑的控件，则显示原有的意见框
		if(initShowDivCount==0 || !parent.parent.switchObj.allowCommentInForm){
			try{
			if(parent.parent.document.getElementById("optionShowDiv")!=null){
				parent.parent.document.getElementById("optionShowDiv").style.display='block';
			}
			if(parent.parent.document.getElementById("attTempContentDiv")!=null){
				parent.parent.document.getElementById("attTempContentDiv").style.display='block';
			}
			if(parent.parent.document.getElementById("attachmentShowTempDiv")!=null){
				parent.parent.document.getElementById("attachmentShowTempDiv").style.display='block';
			}
			if(parent.parent.document.getElementById("content_deal_comment")!=null){
				parent.parent.document.getElementById("content_deal_comment").style.display='block';
			}
			if(parent.parent.document.getElementById("leaderSerialShortname")!=null){
				parent.parent.document.getElementById("leaderSerialShortname").style.display='block';
			}
			}catch(e){}
		}else if(initShowDivCount==0 || initShowDivCount==1){
			if(parent.parent.document.getElementById("leaderSerialShortname")!=null){
				parent.parent.document.getElementById("leaderSerialShortname").style.display='block';
			}
		}else{
			parent.parent.$("#uploadLiuzhuanFile").remove();
		}
		if(initShowDivCount > 0 && parent.parent.switchObj.allowCommentInForm){
			try{
				if(parent.parent.document.getElementById("optionShowDiv")!=null){
					parent.parent.document.getElementById("optionShowDiv").style.display='none';
				}
				if(parent.parent.document.getElementById("attTempContentDiv")!=null){
					parent.parent.document.getElementById("attTempContentDiv").style.display='none';
				}
				if(parent.parent.document.getElementById("content_deal_comment")!=null){
					parent.parent.document.getElementById("content_deal_comment").style.display='none';
				}
				if(parent.parent.document.getElementById("attachmentShowTempDiv")!=null){
					parent.parent.document.getElementById("attachmentShowTempDiv").style.display='none';
				}
				if(parent.parent.document.getElementById("formOpinionDiv")!=null){
					parent.parent.document.getElementById("formOpinionDiv").style.display='none';
				}
				if(parent.parent.document.getElementById("areaTopDiv")!=null){
					parent.parent.document.getElementById("areaTopDiv").style.display='none';
				}
				if(parent.parent.document.getElementById("processAttitude")!=null){
					parent.parent.document.getElementById("processAttitude").style.display='none';
				}
				// if(parent.parent.newGovdocView == '2'){
				// 	$("#moreCheckBox",parent.parent.document).removeClass("moreCheckbox_20p");
				// 	$("#moreCheckBox",parent.parent.document).addClass("moreCheckBox_80p");
				// 	$("#dealButton",parent.parent.document).removeClass("line_height_100");
				// 	$("#dealButton",parent.parent.document).removeClass("line_height_60");
				// 	$("#favoriteDiv",parent.parent.document).removeClass("line_height_100");
				// 	$("#favoriteDiv",parent.parent.document).removeClass("line_height_60");
				// 	var edocHead_h=parent.parent.document.getElementById('edocHead').offsetHeight;
				// 	$("#edocContainer",parent.parent.document).css({top:edocHead_h+'px'});
				// 	$("#dataRelationArea",parent.parent.document).css({top:edocHead_h+'px'});
				// 	$("#dealArea",parent.parent.document).css({border:'0px'});
				// }
				// if(parent.parent.newGovdocView == '2'&&parent.parent.document.getElementById("xuban")&&parent.parent.document.getElementById("pishiname")&&parent.parent.document.getElementById("dealButton")){
				// 	$(".moreCheckBox_80p",parent.parent.document).css({'margin-top':'78px'});
                //     var edocHead_h=parent.parent.document.getElementById('edocHead').offsetHeight;
                //     $("#edocContainer",parent.parent.document).css({top:edocHead_h+'px'});
				// }
				// if(parent.parent.newGovdocView == '2'&&!parent.parent.document.getElementById("xuban")&&parent.parent.document.getElementById("pishiname")&&parent.parent.document.getElementById("dealButton")){
				// 	$(".moreCheckBox_80p",parent.parent.document).css({'margin-top':'48px'});
                //     var edocHead_h=parent.parent.document.getElementById('edocHead').offsetHeight;
                //     $("#edocContainer",parent.parent.document).css({top:edocHead_h+'px'});
				// }
				// if(parent.parent.newGovdocView == '2'&&!parent.parent.document.getElementById("pishiname")&&!parent.parent.document.getElementById("xuban")){
				// 	$("#dealButton",parent.parent.document).css({'margin-top':'40px'});
				// 	$("#dealButton",parent.parent.document).css({'line-height':'0px'});
				// 	$("#favoriteDiv",parent.parent.document).css({'margin-top':'48px'});
				// 	$("#favoriteDiv",parent.parent.document).css({'line-height':'0px'});
                //     var edocHead_h=parent.parent.document.getElementById('edocHead').offsetHeight;
                //     $("#edocContainer",parent.parent.document).css({top:edocHead_h+'px'});
				// }
			}catch(e){}
		}
		//如果没有已经填写的意见则查找可编辑控件进行意见框渲染  zhangdong 20170329 end
	}catch(e){}
}
