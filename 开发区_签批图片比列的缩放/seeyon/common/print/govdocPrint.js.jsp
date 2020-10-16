<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<%@ taglib prefix="ctp" uri="http://www.seeyon.com/ctp"%>
<script type="text/javascript" src="${path}/ajax.do?managerName=govdocAjaxManager${ctp:csrfSuffix()}"></script>
<script type="text/javascript">
var isGovdocForm;
try{
	if(typeof(window.opener.parent.parent.affairApp)!='undefined' && window.opener.parent.parent.affairApp && window.opener.parent.parent.affairApp =='4'){//公文处理节点
		  window.document.write('<style>.browse_class SPAN{color:windowtext}</style>');
	 }else if(typeof(window.opener.parent.parent.app)!='undefined'  && window.opener.parent.parent.app && window.opener.parent.parent.app =='4'){//新建公文
		   window.document.write('<style>.browse_class SPAN{color:windowtext;padding:0;}</style>');
	 }
}catch(e){}

function newGovdocMethod(){
	if(moduleType == '4'){
		isGovdocForm = "1";
		//表单意见
		if(window.opener && window.opener.parent && window.opener.parent.parent &&
				window.opener.parent.parent.opinions){
			dispOpinions(window.opener.parent.parent.opinions,null);
		}
		showSingImg(window.opener.parent.parent.summaryId);

		//打印时去掉背景
		var bb = document.getElementById("bodyBlock");
		var md = document.getElementById("mainbodyDiv");
		if(typeof(bb)!='undefined'){
			bb.style.backgroundColor="transparent";
			bb.style.backgroundImage="";
		}
		if(typeof(md)!='undefined'){
			md.style.backgroundColor="";
			md.style.backgroundImage="";
		}
		try{window.opener.parent.parent.showOpinionOperate();}catch(e){}
	}

}
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
				//zhou:
				hwObj.style.maxWidth="30%";
				divObj[0].style.height="auto"||"100%";
			}
		}
	}

}
function dispOpinions(opinions,senderOpinion) {
	try {
		var i;
		var otherOpinion = "";
		var spanObj;
		var isboundSender = false;
		var flag;
		initSpans();

		//附件文字比较长时，字体重叠我呢体OA-66099
		var replaceHeight = /<div([^<]*?)attachmentDiv([^<]*?)style[ ]*?=(['"]{1})(.*?)['"]{1}([^<]*?)>/ig;
		var linkCss = /<a([^<]*?style[ ]*?=[ ]*?['"].*?)(['"].*?)>/ig;//关联文档a标签设置了不换行

		/** 显示意见 **/
		for(i=0;i<opinions.length;i++) {
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
		    	  //此处字体与公文处理页面处理附件的字体大小一致 参考content_js_end.js
		    	  tempInnerHTML = tempInnerHTML.replace(linkCss, "<a$1;white-space:normal;line-height:normal;font-size:14px;$2>");
				  //解决勾选了“意见与落款换行显示”，打印字体改变问题
				  var fieldName = opinions[i][0];
				  var fieldNode = document.getElementById(fieldName);
				  var fontS;
				  var fontF;
				  if(fieldNode.currentStyle) {
						//IE、Opera
						//alert("我支持currentStyle");
					  fontS =  fieldNode.currentStyle.fontSize;
					  fontF =  fieldNode.currentStyle.fontFamily;
				  } else {
					//FF、chrome、safari
					//alert("我不支持currentStyle");
					  fontS = getComputedStyle(fieldNode,false).fontSize;
					  fontF = getComputedStyle(fieldNode,false).fontFamily;
				  }

				  tempInnerHTML = _addStyle(tempInnerHTML, "div", "font-family:" + fontF);
				  tempInnerHTML = _addStyle(tempInnerHTML, "div", "font-Size:" + fontS);
				  //------------------------------------------------

			      spanObj.innerHTML=tempInnerHTML;
			      if($(spanObj).find("span").length>0){
			    	   //打印预览界面-可点击处理意见的落款人员并弹出报错界面
		                for (var n=0;n<$(spanObj).find("span").length;n++) {
		                  $(spanObj).find("span")[n].onclick=function(){};
		                }
			      }

			    //文单意见区有附件和关联文档，取得链接
			      if($(spanObj).find("a").length>0){
		                $(spanObj).find("a").each(function(){
			    		    $(this).removeAttr('onclick');
			    		    $(this).removeAttr('href');
			    		  });
			      }
			      spanObj.style.border="0px";
			      spanObj.contentEditable="false";
			      spanObj.style.minHeight = "0px";
			      spanObj.style.height="auto";
			      spanObj.style.whiteSpace = "normal";
			      spanObj.style.display = "block";
				  spanObj.parentNode.style.display="block";
			      //spanObj.style.overflowY ="auto";
		    }
		}
		/** 其它意见框 **/
		spanObj=opinionSpans.get("otherOpinion", null);
		if(otherOpinion!="" && spanObj == null) {
			//在公文督办中查看公文，发起人附言和处理意见没做区分，应在意见前面显示处理意见
			document.getElementById("dealOpinionTitleDiv").style.display = '';
			spanObj = document.getElementById("displayOtherOpinions");
			if(spanObj!=null) {
		        spanObj.innerHTML=otherOpinion;
		        spanObj.style.visibility="visible";
		        spanObj.style.whiteSpace = "normal";
		        spanObj.contentEditable="false";
		        spanObj.style.border="0px";
				spanObj.style.display = "block";
				spanObj.parentNode.style.display="block";
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
function changeFontsize(opinion,spanObj){
	  try{

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
	    } catch (e) {
	    }
	}
var opinionSpans=null;
function initSpans()
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

function _addStyle(op, tagNode, styleVal){

    var upTagNode = "<" + tagNode.toUpperCase();
    var lowTagNode = "<" + tagNode.toLowerCase();

    var newStr = op;

    var tempArry = [];
    tempArry[0] = upTagNode;
    tempArry[1] = lowTagNode;
    for(var i = 0; i < tempArry.length; i++){

        var tempTagNode = tempArry[i];
        var index = op.indexOf(tempTagNode, 0);//从0点开始搜索
        while(index != -1){
            var tempEnd = op.indexOf(">", index);
            var attrStr = op.substring(index + tempTagNode.length, tempEnd);
            //if(attrStr.indexOf("attachmentDiv") == -1){//排除附件DIV
            if((attrStr.indexOf("attachmentDiv") != -1 && styleVal.indexOf("font-family")!=-1)||attrStr.indexOf("attachmentDiv") == -1){
            	//附件使用公文处理页面字体
            	var newSttStr = attrStr;
                if(newSttStr.match(/style/i)){
                    newSttStr = newSttStr.replace(/style[ ]*?=(["']{1})(.*?)["']{1}/, "style='$2;"+styleVal+";'");
                }else {
                    newSttStr += " style='"+styleVal+"'";
                }
                newStr = newStr.replace(tempTagNode + attrStr + ">", tempTagNode + newSttStr + ">");
            }
            //}

            index = op.indexOf(tempTagNode, tempEnd);//从继续搜索
        }
    }
    return newStr;
}
//打印模版下载到本地
function localPrint(){
	if(typeof(window.opener.parent.parent)=='undefined'){
		return;
	}
	 var taohongTemplateContentType = "OfficeWord";
	 var ajaxManager = new govdocAjaxManager();
	 var formId= window.opener.parent.parent.formAppId;
		if(!formId || formId == "" || formId == "-1"){
			return;
		}
	 var fileUrl = ajaxManager.getLocalPrintTemplate(formId);
	 if(fileUrl==""||fileUrl==null){
	 return;
	 }
	 var arrTemp = fileUrl.split("&");
	 taohongTemplateContentType = arrTemp[1];
	 fileUrl = arrTemp[0];

	var urlStr = "${path}/govdoc/doctemplate.do?method=govdocwendanTaohongIframe"+"&tempContentType=" + taohongTemplateContentType+"&summaryId="+window.opener.parent.parent.summaryId;

	var isNewGovdoc = false;
	if(typeof(window.opener.parent.parent.currentPageName)!='undefined' &&  window.opener.parent.parent.currentPageName== 'newGov'){
		isNewGovdoc = true;
	}

	window.opener.parent.parent.page_receivedObj = fileUrl;
	window.opener.parent.parent.page_templateType = "script";
	window.opener.parent.parent.page_extendArray = new Array();

	window.formTaohongWin = $.dialog({
		id:"formTaohongWin",
		title : $.i18n('govdoc.nodePerm.ScriptTemplate.label'),
		transParams : {
			'parentWin' : window.opener.parent.parent.window,
			"popWinName" : "formTaohongWin",
			"isNew":isNewGovdoc,
			"popCallbackFn" : function() {
			}
		},
		url : urlStr,
		targetWindow : getA8Top(),
		width : getA8Top().screen.availWidth,
		height : getA8Top().screen.availHeight
	});
}
</script>
