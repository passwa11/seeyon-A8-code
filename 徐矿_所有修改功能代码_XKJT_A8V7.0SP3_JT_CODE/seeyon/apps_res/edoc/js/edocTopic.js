function edocFormDisplay(){
	initIpadScroll('bodyId',400,400);
	if(onlySeeContent=="true")
	{
	    var divObj=document.getElementById("formAreaDiv");
	    divObj.style.display="none";
	    /*divObj=document.getElementById("edocContentDiv");
	    divObj.style.display="block";
	    divObj.style.width="100%";
	    divObj.style.height="100%";*/
	    divObj=document.getElementById("colOpinion");
	    divObj.style.display="none";
		return true;
	}
  	var xml = document.getElementById("xml");
	var xsl = document.getElementById("xslt");
	
	    //设置修复字段宽度的传参
    	window.fixFormParam = {"isPrint" : true, "reLoadSpans" : false};
  		initReadSeeyonForm(xml.value,xsl.value);
  		//OA-45506fengjing这个账号处理公文时，某个公文文号未能显示出来
  		//是因为文号样式问题导致的，现在设置为和内部文号样式一致
  		//var docMark = document.getElementById("my:doc_mark");
  		//if(docMark){
  		//   var $docMark = $(docMark);
  	     //  $docMark.attr("style","width: 100%; border-top-color: currentColor; border-right-color: currentColor; border-bottom-color: currentColor; border-left-color: currentColor; border-top-width: 0px; border-right-width: 0px; border-bottom-width: 0px; border-left-width: 0px; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none;");
  		//}
  		
  		dispOpinions(opinions,sendOpinionStr);
  		hiddeBorder(opn);
  		initHandWrite();
  		substituteLogo(logoURL);

  		//OA-22643 发文单中含有附件元素，拟文时插入附件，在已发中查看文单中不能将附件名称显示完全，打印时可以显示全
  		/*
  		var atta = document.getElementById("my:attachments");
  		if(atta){
  		  var text = atta.value;
  		  $(atta).after("<div>"+text+"</div>");
  		  $(atta).remove();
        }*/
        
        //这里是将 意见表Id,回退状态回填到edocSummary的隐藏域中，如果在后台设置了意见处理方式为回退时
        //选择意见处理方式，提交时会弹出对话框选择意见覆盖方式，会用到这些参数
        if(parent.document.getElementById("optionId")){//****从快速处理中 显示，就不设置下面的**** 
	        parent.document.getElementById("optionId").value = document.getElementById("optionId").value;
	        parent.document.getElementById("affairState").value = document.getElementById("affairState").value;
	        parent.document.getElementById("optionType").value = document.getElementById("optionType").value;
        }
        return false;
}
function dorepform()
{
    var subbutton=document.getElementById("subbtton");
    subbutton.disabled=true;
    return true;
}

//从apps_res/v3xmain/js/phrase.js中将下面两个方法移动到这里
//政务版公文专用意见常用语
function showPhrase_gov(showTop){
    if(showTop && (typeof(showTop)=='string'  || typeof(showTop)=='number' )){
        defaultPhrase_gov(showTop);
    }else if(showTop && (typeof(showTop)=='object')){
      defaultPhrase_gov(showTop);
    }else{
        defaultPhrase_gov();
    }
    
}
//政务版公文专用意见常用语显示位置
function defaultPhrase_gov(showTop){
    var _phraseFrame = document.getElementById('phraseFrame');
    if(_phraseFrame.src != phraseURL){
        document.getElementById('phraseFrame').src = phraseURL+"&dateTime="+new Date().getTime()+CsrfGuard.getUrlSurffix();
    }
    var divPhrase = document.getElementById('divPhrase');
    if(typeof(showTop)!="undefined" && showTop){
        divPhrase.style.top = "30px";
        divPhrase.style.right="auto";
        divPhrase.parentNode.className="position_relative";
    }
    document.getElementById('divPhrase').style.display = '';
}
//修改人：杨帆 2011-12-10———显示意见处理输入栏在相应意见位置 --start
function showOpinionsInputForm() {
	var insertObj = parent.document.getElementById("disPosition").value;
	//如果意见框显示在 上级意见汇报元素，那么处理公文时可能需要向上级单位提交意见，则设置一个标志
    if(insertObj=='report'){
		parent.document.getElementById("isReportToSupAccount").value = "true";
    }
	
	if(opinionSpans==null) { initSpans(); }
	
	//删除附件和关联文档的onclick事件替换
	var tempAtts = _dealAtts();
    var str1 = tempAtts[0];
    var str2 = tempAtts[1];
	
	//处理态度
	var str3="";
	var processAttitudeObj = parent.document.getElementById("processAttitude");
	if(parent.canShowAttitude=="true" ) {
		str3= processAttitudeObj.innerHTML;
		
		//设置文字不换行，IE7样式问题处理
		var tempRex = /<div([\s\S]*?)<\/div>/ig;
		str3 = str3.replace(tempRex, "<div style='word-break:normal;word-wrap:normal;white-space:nowrap;height:20px;' $1<\/div>");
	}

	var processHTML="";
	if(parent.canShowCommonPhrase=="true"){//不需要该事件 onmouseover="showPhrase_gov()"
	  processHTML+='<div id="notPrint"><div oncontextmenu="return false"';
	  processHTML+='style="position:absolute; right:350px; top:120px; width:450px; height:150px; z-index:2; background-color: #ffffff;display:none;overflow:no;border:1px solid #000000;"';
	  processHTML+='id="divPhrase" oncontextmenu="return false">';//onmouseout="hiddenPhrase()"
	  processHTML+='<IFRAME width="100%" id="phraseFrame" onmouseout="hiddenPhrase()" name="phraseFrame" height="100%" frameborder="0" align="middle" scrolling="no"';
	  processHTML+='marginheight="0" marginwidth="0"></IFRAME>';
	  processHTML+='</div>';
	}
	processHTML+="<table width='100%' border='0' cellspacing='0' cellpadding='0' >";
	processHTML+="<tr><td height='30' class='edoc_deal' style='padding: 0px 5px;'><div class='edoc_deal_div'>";

	if(parent.canShowAttitude=="true" && insertObj!='report'){
		/*processHTML+="<div style='margin-top:-4px;display:inline-black;'>";*/
		processHTML+=str3;
		/*processHTML+="</div>";*/
	}else{
	    if(processAttitudeObj != null && typeof(processAttitudeObj)!='undefined'){
	    	parent.document.getElementById("processAttitude").innerHTML = '';
	    }
	}
	if(parent.canShowCommonPhrase=="true"){
	    processHTML += '<div class="metadataItemDiv" style="white-space: nowrap; -ms-word-break: normal; -ms-word-wrap: normal;height:20px;">';
		processHTML+="<a class='validationStepback' onclick='javascript:parent.windowResizeTop();showPhrase_gov(this);' style='font-size:12px;display:inline-block;margin-right:5px;word-break:normal;word-wrap:normal;white-space:nowrap;'>";
		processHTML+="<span class='ico16 common_language_16 margin_r_5'></span>";
		processHTML+=v3x.getMessage("edocLang.edoc_common_language")+"</a>";
		processHTML += '</div>';
	}
	processHTML+="<div style='clear:both;'></div></td></tr>";

	if(parent.canShowOpinion=="true"){
	   processHTML+="<tr><td height='30' style='padding: 0px 5px;width:98%;'>";
	   
	   //#moreStyle 是设置的一个标志，用于替换成文单中的style属性
       processHTML+="<textarea id='contentOP' name='contentOP' rows='10'  style='width:100%;word-break: normal;#moreStyle' maxSize='1000' validate='maxLength'></textarea>";
       processHTML+="</td></tr>";
	}
    if(parent.canUploadAttachment=="true" || parent.canUploadRel=="true" ||parent._atts == "true"){//没有上传附件和关联文档的权限，但是处理意见有附件时，显示出附件
       processHTML+="<tr><td><div id='processatt1'>"+str1+"</div><div id='processatt2'>"+str2+"</div></td></tr>";
    }
    
    if(parent.canUploadAttachment=="true"||parent.canUploadRel=="true" && insertObj!='report'){
    	processHTML+="<tr><td>";
    	//附件
    	if(parent.canUploadAttachment=="true"){
    		processHTML+="<a class='validationStepback' href='javascript:parent.insertAttachment(null, null, \"_insertAttCallback\", \"false\");' style='font-size:12px;display:inline-block;margin-right:5px;word-break:normal;word-wrap:normal;white-space:nowrap;float:left;'>";
        	processHTML+="<span class='ico16 affix_16 margin_r_5'></span>"+V5_Edoc().$.i18n("permission.operation.UploadAttachment")+"</a>";
    	}
    	//关联文档
    	if(parent.canUploadRel=="true" && insertObj!='report'){
    		processHTML+="<a class='validationStepback' href='javascript:parent.quoteDocument();' style='margin-right:5px;font-size:12px;display:inline-block;word-break:normal;word-wrap:normal;white-space:nowrap;float:left;'>";
        	processHTML+="<span class='ico16 associated_document_16 margin_r_5'></span>"+V5_Edoc().$.i18n("permission.operation.UploadRelDoc")+"</a>";
    	}
    	//消息推送
    	var pushMsg= parent.document.getElementById("pushMessage").innerHTML;
    	processHTML+=pushMsg;
    	processHTML+="</td></tr>";
    }
    processHTML+="</table></div>";

    
    var inputObj = opinionSpans.get("my:"+insertObj,null);
	if(inputObj == null) {		  
		  inputObj=opinionSpans.get("my:otherOpinion",null);
		  parent.isOutOpinions = true;//在表单外，或者在在其他意见框内，都不受到后台表单的设置的限制
		  if(inputObj==null){
			  document.getElementById("dealOpinionTitleDiv").style.display = '';
			  processHTML = processHTML.replace("#moreStyle", "");//替换无用的标志
			  document.getElementById("processOtherOpinions").innerHTML=processHTML;
			  document.getElementById("processOtherOpinions").style.display='';
		  }
	}
    
	//意见显示在文单中
	if(inputObj){
	    
	  //样式复制
        var styleStr = $(inputObj).attr("style");
        if("string" != typeof(styleStr)){
            styleStr = inputObj.style.cssText;//360浏览器返回的是对象
        }
        
        if(/WHITE-SPACE: nowrap;/i.test(styleStr)){
            styleStr = styleStr.replace(/WHITE-SPACE: nowrap;/i,"word-break: break-all;");
        }
        if(/white-space: normal;/i.test(styleStr)){
            styleStr = styleStr.replace(/WHITE-SPACE: normal;/i,"word-break: break-all;");
        }
        
        //样式不能有高度和宽度属性
        styleStr = styleStr.replace(/width[ ]?:[ ]?.+?[;"']/ig, "");
        styleStr = styleStr.replace(/height[ ]?:[ ]?.+?[;"']/ig, "");
        styleStr = styleStr.replace(/border.*?:.+?[;"']/ig, "");

	    hiddeBorderAndHeight(insertObj);
	    processHTML = processHTML.replace("#moreStyle", styleStr);//替换无用的标志
        inputObj.insertAdjacentHTML("beforeBegin", processHTML);
	}
	
    if(parent.canShowOpinion=="true"){
      	document.getElementById("contentOP").value=parent.document.getElementById("contentOP").value.trim();
    }
  	//退回人打开信息操作控制
    if(''!=subState){
    	 if(subState =='15' || subState =='17'){
    	    	canEdit=false;
    	    	$(".validationStepback").attr('disabled','disabled').attr("href","javascript:;").css("color","#ccc").removeAttr("onclick");
    	    }
    }

}
//重新加载附件显示
function reloadParentAtt(){
	
    var tempAtts = _dealAtts();
    var str1 = tempAtts[0];
    var str2 = tempAtts[1];
    
	document.getElementById("processatt1").innerHTML=str1;
	document.getElementById("processatt2").innerHTML=str2;
	
}

//处理附件样式问题
function _dealAtts(){
    
    var str1=parent.document.getElementById("attachmentArea").innerHTML;
    var str2=parent.document.getElementById("attachment2Area").innerHTML;
    str1=str1.replace(/deleteAttachment/g,'deleteParentAtt');
    str2=str2.replace(/deleteAttachment/g,'deleteParentAtt');
    
    //附件文字比较长时，字体重叠我呢体OA-66099
    var replaceHeight = /<div([\s\S]*?)style[ ]*?=(['"]{1})(.*?)['"]{1}([\s\S]*?)>/ig;
    str1 = str1.replace(replaceHeight, "<div$1style=$2$3height:auto;line-height:normal;float: none;$2$4>");
    str2 = str2.replace(replaceHeight, "<div$1style=$2$3height:auto;line-height:normal;float: none;$2$4>");
    
    str1 = str1.replace(/noWrap/ig, "");
    str2 = str2.replace(/noWrap/ig, "");

    str1 += "<div style='clear:both'></div>";
    str2 += "<div style='clear:both'></div>";
    
    
    var ret = [];
    ret.push(str1);
    ret.push(str2);
    
    return ret;
}

//删除附件并重新加载插件显示
function deleteParentAtt(fileurl){
	parent.deleteAttachment(fileurl);
	reloadParentAtt();
}

//隐藏意见框中的文本框 
function hiddeBorderAndHeight(opn) {
	if(opinionSpans==null) { initSpans(); }
	var ops = opn.split(",");
	var spanObj;
	for(i=0;i<ops.length;i++) {
		spanObj=opinionSpans.get("my:"+ops[i]);
		if(spanObj!=null && spanObj.innerText=="") {			
			spanObj.style.border="0px";	
			spanObj.style.height="0px"; 
			spanObj.style.display="none";
		}
	}
	if(opinionSpans.get("my:otherOpinion") !=null){
		opinionSpans.get("my:otherOpinion").style.border="0px";
	}
}
//修改人：杨帆 2011-12-10———显示意见处理输入栏在相应意见位置 --end


function relationSendV(){
	var recType = paramRecType;
	var url = "edocController.do?method=relationNewEdoc&recEdocId="+paramRecEdocId+"&recType="+paramRecType+"&forwardType="+paramForwardType+"&newDate="+new Date().getTime();
    window.relationSendVWin = getA8Top().$.dialog({
        title:'　',
        transParams:{'parentWin':window},
        url: url,
        targetWindow:getA8Top(),
        width:"600",
        height:"600"
    });
}
function relationRecV() {// 弹出收文菜单
    var url = relationUrl;
    url = url + "&sendSummaryId=" + summaryId;
    if (url == null || url == "") {
        alert(edocResourseNotExist);
    } else {

        var rv = v3x.openWindow({
            url : url,
            workSpaceFull : 'yes',
            dialogType : "open"
        });

    }
}

function loadRelationButton(){
//关联发文
var relSends = paramRelSends;
if(relSends == "haveMany"){
 document.getElementById("relationSend").style.display="block";
}
// 关联收文
var relRecs = paramRelRecs;
if(relRecs == "haveMany"){
 document.getElementById("relationRec").style.display="block";
}
}

/*OA-17772.单击打开列表，文单都是靠左放置的，参考G6的功能，都是居中放置.*/
function loadUE() {
if($("#html")!=null) {
	if($("#html").find("table.xdLayout")!=null) {
	 	setTimeout(function(){
	 		$("#html").find("table.xdLayout").attr("align", "center");
	 	}, 300);
	}
}

//$("span.link-blue").css("font-size","12px");
}