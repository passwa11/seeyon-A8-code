<%@ page isELIgnored="false" language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.seeyon.ctp.common.constants.ApplicationCategoryEnum" %>
<%@page import="com.seeyon.ctp.common.constants.Constants" %>
<%@page import="com.seeyon.v3x.edoc.manager.EdocSwitchHelper"%>
<html xmlns="http://www.w3.org/1999/xhtml" style="overflow: hidden;">
<head> 
<%@ include file="../common/INC/noCache.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<%@ include file="edocHeader.jsp" %>

<title><fmt:message key='${newEdoclabel}'/></title>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery-ui.custom.min.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.plugin.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery-ui.custom.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/apps_res/edoc/js/leave.js${v3x:resSuffix()}" />"></script>

<c:set value="${pageContext.request.contextPath}" var="path" />
<%--以下两个时间用于调用模板时，使用模板的流程期限 --%>
<c:set value="${(summaryFromTemplate.deadline==null||summaryFromTemplate.deadline<=0)?(formModel.deadline):(summaryFromTemplate.deadline)}" var="finalDeadline" />
<c:set value="${(summaryFromTemplate.deadline>0 && summaryFromTemplate.updateTime!=null)?(summaryFromTemplate.updateTime):(formModel.edocSummary.createTime)}" var="finalCreateTime" />

<link rel="stylesheet" href="${path}/common/all-min.css" />
<%@ include file="/WEB-INF/jsp/ctp/portal/common/components_theme.jsp"%>
<link rel="stylesheet" type="text/css" href="<c:url value="/apps_res/edoc/css/edocDisplay.css${v3x:resSuffix()}" />">
<c:if test="${param.barCode =='true'}">
    <script type="text/javascript" charset="UTF-8" src="<c:url value="/common/barCode/js/barCode.js" />"></script>
    <c:set var="hasBarCode" value="true"/>
</c:if>

<script type="text/javascript">
<!--
var theform = document.getElementById("sendForm");
var registerType = "${param.registerType }";
var agentToId = "${agentToId}";
var agentToName = "${agentToName}";
var agentToAccountShortName = "${agentToAccountShortName}";
//服务器时间和本地时间的差异
var server2LocalTime = <%=System.currentTimeMillis()%> - new Date().getTime();
try{
parent.treeFrame.changeTreeToCreate();
}catch(e){}

if(typeof(isLeave)!=undefined) {
    isLeave = ${param.backBoxToEdit=='true'}?true:false;
}
//给精灵弹出的窗口设置title参数。
if(typeof(parent.document.title)!="undefined")parent.document.title="<fmt:message key='${newEdoclabel}'/>";
var isNewColl = true ;
var isBoundSerialNo = "${isBoundSerialNo}";
var logoURL = "${logoURL}";
var processing=false;
var currentPage="newEdoc";//公文督办的时候，如果是新页面就不提交，否则提交到服务器。
// 快速需求(FastDemand):发文、签报支持PDF正文
var supportPdfMenu=true;
// 设置收文登记页面支持PDF正文。
// var supportPdfMenu=('${appType}'=='20'?true:false);

var hasDiagram = <c:out value="${hasWorkflow}" default="false" />;        
var caseProcessXML = '${process_xml}';
var caseLogXML = "";
var caseWorkItemLogXML = "";
var currentNodeId = null;
var showHastenButton = "";
var appName="${appName}";
var isTemplete = false;
var templeteCategrory="${templeteCategrory}";
var defaultPermName="<fmt:message key='${defaultPermLabel}' bundle='${v3xCommonI18N}'/>";
//原始正文的ID，即联合发文的时候原始正文的ID.如果拟文的时候，联合发文需要套红的话就要使用此变量来调出服务器端保存的原始正文。
var oFileId=""; 
var draftTaoHong="";
var noDepManager = "${noDepManager}";
var isOnlySender = "${isOnlySender}";
if(isOnlySender != "true")
{
if(noDepManager == "true"){
    alert(_("edocLang.edoc_supervise_nodepartmentManager"));
}
}


var policys = null;
var nodes = null;

var unallowedSelectEmptyGroup_wf = true;

var isFromTemplate = <c:out value="${isFromTemplate}" default="false" />;

var selfCreateFlow=${selfCreateFlow};
var templateType="${templateType}";
var showMode = 1;
showMode = ((isFromTemplate && templateType !='text') || selfCreateFlow==false) ? 0 : showMode;
var hiddenColAssignRadio_wf = true;
var editWorkFlowFlag = "true";
var actorId="${actorId}";
var currentUserId = "${currentUserId}";
var currentUserAccountId = "${currentUserAccountId}";
var templeteProcessId ="${templeteProcessId}";
var jsEdocType=${formModel.edocSummary.edocType};

hasWorkflow = <c:out value='${hasWorkflow}' default='false' />;

var selectedElements = null;
var _canUpdateContent=${canUpdateContent};
var taohongSendUnitType = 1;


//对登记时正文本地保存的权限进行控制:有公文开关<外来公文登记是否允许修改>的权限 = 有本地保存的权限。
var officecanSaveLocal="${canUpdateContent}";
//OA-35531,归档后修改，不允许下载到本地
if("${param.from}" == "archived"){
  officecanSaveLocal = "false";
}
function edocFormDisplay(){
            /*if(!v3x.isMSIE){
                alert(_("edocLang.isNotIe"));
                //window.location= "<c:url value='/edocController.do?method=entryManager&entry=recManager'/>";
                //非IE浏览器，页面跳转到待办
                parent.parent.location.href="<c:url value='edocController.do?method=listIndex&controller=edocController.do&listType=listPending&edocType=${formModel.edocSummary.edocType}'/>";
            }*/
            canUpdateContent=_canUpdateContent;
            enableButton("send");           
            var xml = document.getElementById("xml");
            var xsl = document.getElementById("xslt");
            if(canUpdateContent==false)
            {
                /*try{
                    initReadSeeyonForm(xml.value,xsl.value);
                }catch(e){
                    alert(_("edocLang.edoc_form_xml_error") +e);
                    disableButton("send");
                    window.location.href=window.location.href;
                    return false;
                }*/
               //bug 31615 收文登记外来文不允许修改的时候，让内部文号可以编辑。 312SP1 muj
              
              myBar.disabled("bodyTypeSelector");
            }
            //34171 320单独控制正文，不控制文单
            try{
                initSeeyonForm(xml.value,xsl.value);
            }catch(e){
                alert(_("edocLang.edoc_form_xml_error") +e);
                disableButton("send");
                window.location.href=window.location.href;
                return false;
            }
            
            //setObjEvent();
            initContentTypeState();
            initBodyType();
            substituteLogo(logoURL);
            //初始化公文处理意见，有可能来自回退、撤销的待发流程
            if(typeof(opinions)!="undefined")
            {
                dispOpinions(opinions,sendOpinionStr);
            }
            //外来文登记，不允许保存待发
/* OA-41833升级测试：升级前guoby设置wenj为代理人，升级后wenj代理处理待登记的公文，保存待发是置灰的__改这个bug，需求要求把这个置灰去掉
if("${comm}"=="distribute"||"${comm}"=="register")
            {
                myBar.disabled("save");
            } 
*/
            <%--控制正文类型菜单置灰与否--%>
            <%-- OA-17720 老功能bug：新建自由收文，调用模版，正文类型应该也是不能修改的才对 --%>
            if((isFromTemplate==true && templateType !='workflow' )
                    ||('${comm}' == 'distribute'||'${comm}' == 'register')||'${dianziRec_workflow_template}'=='true')
            {               
                myBar.disabled("bodyTypeSelector");
            }
            //confirmSelectPersonSetDefaultValue();
            showEdocMark();
            return false;
        }
 
  
formOperation = "aa";
${opinionsJs}
//维持一个Map,保存正文的ID和正文编号（0：原始正文 1：套红1 2：套红2）
${contentRecordId}
//
${docMarkByTemplateJs}

function adjustDivHeight()
{  
  var formDivObj=document.getElementById("formAreaDiv");
  formDivObj.style.height=(screen.availHeight-258)+"px";  
}

//分枝 开始
//分支
    var branchs = new Array();
    var keys = new Array();
    var team = new Array();
    var secondpost = new Array();
    <c:if test="${branchs != null}">
        var handworkCondition = _('edocLang.handworkCondition');
        <c:forEach items="${branchs}" var="branch" varStatus="status">
            var branch = new ColBranch();
            branch.id = ${branch.id};
            branch.conditionType = "${branch.conditionType}";
            branch.formCondition = "${v3x:escapeJavascript(branch.formCondition)}";
            branch.conditionTitle = "${v3x:escapeJavascript(branch.conditionTitle)}";
            //if(branch.conditionType!=2)
                branch.conditionDesc = "${v3x:escapeJavascript(branch.conditionDesc)}";
            /*else
                branch.conditionDesc = handworkCondition;*/
            branch.isForce = "${branch.isForce}";
            eval("branchs["+${branch.linkId}+"]=branch");
            keys[${status.index}] = ${branch.linkId};
        </c:forEach>
    </c:if>
    <c:if test="${teams != null}">
        <c:forEach items="${teams}" var="team">
            team["${team.id}"] = ${team.id};
        </c:forEach>
    </c:if>
    <c:if test="${secondPosts != null}">
        <c:forEach items="${secondPosts}" var="secondPost">
            secondpost["${secondPost.depId}_${secondPost.postId}"] = "${secondPost.depId}_${secondPost.postId}";
        </c:forEach>
    </c:if>
//分枝 结束


//跟踪相关函数
function setTrackRadiio(){
    var obj = document.getElementById("isTrack");
    if(obj!=null){
        var all = document.getElementById("trackRange_all");
        var part = document.getElementById("trackRange_part");
        if(obj.checked){
            all.disabled = false;
            part.disabled = false;
            
            all.checked = true;
        }else {
            all.disabled = true;
            part.disabled = true;

            all.checked = false;
            part.checked = false;
        }
    }
}
function setTrackCheckboxChecked(){
    var obj = document.getElementById("isTrack");
    if(obj!=null){
        obj.checked = true;
    }
}
function selectPeopleFunTrackNewCol(){
    setTrackCheckboxChecked();
    selectPeopleFun_track();
}
function setPeople(elements){
    var memeberIds = "";
    if(elements){
        for(var i= 0 ;i<elements.length ; i++){
            if(memeberIds ==""){
                memeberIds = elements[i].id;
            }else{
                memeberIds +=","+elements[i].id;
            }
        }
        document.getElementById("trackMembers").value = memeberIds;
    }
}

var summaryId = "${formModel.edocSummaryId}";
var sendEdocId = "${param.edocId}";
//从待登记列表打开的时候得使用这种方式赋值
if(!sendEdocId){
	sendEdocId = "${edocId}";
}
var isAllowContainsChildDept_ExchangeUnit = true;

function unload(summaryId){
    try{
        unlockHtmlContent(summaryId);
    }catch(e){
    }
}
//OA-36095 wangchw登记了纸质公文，在待办中转发文--收文关联新发文，收文处理节点查看有关联链接，处理时回退该流程，发起人在待发中查看有此链接直接发送后已发待办中也有，但是若在待发中编辑没有此链接，发送后也没此链接
function relationSendV(){
  var url = "edocController.do?method=relationNewEdoc&recEdocId=${recEdocId}&recType=${recType}&forwardType=${forwardType}&newDate="+new Date().getTime();
  var rv = v3x.openWindow({
              url: url,
              height : 600,
              width  : 600 
   });
  if (rv == "true") {
    getA8Top().reFlesh();
}
}

//puyc  关联收文
function relationRecv(){
        var url = "${relationUrl}";
        if(url == null || url == ""){
        	alert("<fmt:message key='edoc.resourse.notExist'/>");//alert("资源不存在！");
            }else{
        var rv = v3x.openWindow({
            url: url,
            workSpace: 'yes',
            dialogType: v3x.getBrowserFlag('pageBreak') == true ? 'modal' : '1'
        });
        if (rv == "true") {
            getA8Top().reFlesh();
        }
            }
    }   


function loadRelationButton(){
    var newContactReceive = "${param.newContactReceive}";// 管理收文
    //OA-32958 收文转发文，选择新发文关联收文。然后在发文-拟文时保存待发，在待发中编辑，发文关联收文的字样不在了。但是发送以后，在已发中打开，可以关联到收文。
    if(!newContactReceive){
      newContactReceive = "${newContactReceive}";
    }
    var newContactReceives = "";
    var relationRec = document.getElementById("relationRec");
    var relationSen = document.getElementById("relationSen");

    //BUG20120725011863_G6_v1.0_徐州市元申软件有限公司_公文收文结束转发文，调用模板附件、正文、文单内容丢失
    //调用模板后，数据会丢失，要保持传递过来--start
    var relationRecd= "${relationRecd}";
    var relationSend= "${relationSend}";
    var receiveEdocIdFromTemplate="${receiveEdocIdFromTemplate}";

    if(relationRecd=="haveYes"){//关联收文
      document.getElementById("relationRecd").value = "haveYes";
      relationRec.style.display="block";
     }
    if(relationSend=="haveYes"){//关联发文
      document.getElementById("relationSend").value = "haveYes";
    }
    if(receiveEdocIdFromTemplate!="null"){
      document.getElementById("relationRecId").value = receiveEdocIdFromTemplate;//收文Id
    }
    //调用模板后，数据会丢失，要保持传递过来--end
  
    if(newContactReceive != null){
        
        var relationRecId = "${param.edocId}";
        //OA-33197 收文转发文，先保存待发，然后编辑，发送，页面出异常。但是系统消息显示该公文发送成功了。
        //relationRecId 是从待发编辑中查找发文关联的收文时设置的
        if(!relationRecId) relationRecId = "${relationRecId}";
        
        document.getElementById("relationRecId").value = relationRecId;//收文Id
        newContactReceives = newContactReceive.split(","); 
        for(var i = 0;i<newContactReceives.length;i++){
        if(newContactReceives[i]=="1"){//关联收文
          document.getElementById("relationRecd").value = "haveYes";
          relationRec.style.display="block";
          }
        if(newContactReceives[i]=="2"){//关联发文
          document.getElementById("relationSend").value = "haveYes";
        }
        //OA-36659  收文转发文，拟文页面有个关联收文，还有个关联发文。点击关联发文，报错。应该没有关联发文，只有关联收文。  
        if('${param.edocType}' == 1 && newContactReceives[i]=="2"){
          relationSen.style.display="block";  
        }
       }
    }   

    //puyc 分发，收文的summaryId，传到分发
    var recSummaryId = "${recSummaryId}";
    if(recSummaryId != null && recSummaryId != ""){
        document.getElementById("recSummaryIdVal").value = "${recSummaryId}";
        }
  }
function selectDeadline(request,obj,width,height){
    
    var now = new Date();//当前系统时间

    whenstart(request,obj, width, height,'datetime');
    
    if(obj.value != ""){
        var days = obj.value.substring(0,obj.value.indexOf(" "));
        var hours = obj.value.substring(obj.value.indexOf(" "));
        var temp = days.split("-");
        var temp2 = hours.split(":");
        var d1 = new Date(parseInt(temp[0],10),parseInt(temp[1],10)-1,parseInt(temp[2],10),parseInt(temp2[0],10),parseInt(temp2[1],10));
        if(d1.getTime()<now.getTime()){
            if(!window.confirm("<fmt:message key='edoc.flowtime.validate'/>")){
                obj.value = "";
                return false;
                
            }
        }
    }
}
//G6 V1.0 SP1后续功能_流程期限--start
function selectDateTime(request,obj,width,height){
    var now = new Date();//当前系统时间
    var beforeValue=obj.value;
    whenstart(request,obj, width, height,'datetime');
    
    if(obj.value != ""){
        var days = obj.value.substring(0,obj.value.indexOf(" "));
        var hours = obj.value.substring(obj.value.indexOf(" "));
        var temp = days.split("-");
        var temp2 = hours.split(":");
        var d1 = new Date(parseInt(temp[0],10),parseInt(temp[1],10)-1,parseInt(temp[2],10),parseInt(temp2[0],10),parseInt(temp2[1],10));
        if(d1.getTime()<=(now.getTime()+server2LocalTime)){
            alert("<fmt:message key='edoc.flowtime.validate'/>");//流程期限不能小于等于服务器时间
            obj.value=beforeValue;
            return false;
        }else{
            var deadline = document.getElementById("deadline");
            var isContainCustom=false;
            if(deadline.options.length>0){
                for(var i=0;i<deadline.options.length;i++){
                    if(deadline.options[i].text.length==16){
                        isContainCustom=true;
                        deadline.options[i].selected=true;
                        break;
                    }
                }
            }
            if(isContainCustom){
                var index=deadline.selectedIndex; 
                deadline.options.remove(index);
            }
            var deadlineValue=Math.round((d1.getTime()-now.getTime()-server2LocalTime)/1000/60);
            deadline.options.add(new Option(obj.value,deadlineValue));
            deadline.options[deadline.options.length-1].selected=true;
        }
    }
}
//G6 V1.0 SP1后续功能_流程期限--end

//用于判断表单、附件是否被修改过的方法，保存原始值
function formInitialValue(){
  
    $("input[id^='my:']").each(function() {
        jQuery(this).attr('_value', jQuery(this).val());
    });
    
    $("select[id^='my:']").each(function() {
        jQuery(this).attr('_value', jQuery(this).val());
    });

    $("textarea[id^='my:']").each(function() {
        jQuery(this).attr('_value', jQuery(this).val());
    });
    
    $("#attachmentArea").attr('_value', $("#attachmentArea").html());
    $("#attachment2Area").attr('_value', $("#attachment2Area").html());

}

var hasTemplate = "${hasTemplate}";
function valiDeadAndLcqx(obj){
    //流程期限
    var dl = $("#deadline");
    var remind = $("#advanceRemind");
    var mycal=$("#deadLineDateTimeInput");
    var hiddenObj=$("#deadLineDateTime")[0];
   //流程期限
    if(obj.id=='deadline'){
        var deadLineTime=dl[0].value;
        $("#deadline2").val(deadLineTime);
        //日期计算
        var dateValueStr=getDateTimeValue(deadLineTime);
        if(mycal&&hiddenObj){
            mycal.val(dateValueStr);
            hiddenObj.value=dateValueStr;
        }
        if(getDateTimeValue(remind[0].value) >= getDateTimeValue(dl[0].value) && parseInt(remind[0].value) != 0){
            //未设置流程期限或流程期限小于,等于提前提醒时间
            alert(v3x.getMessage("edocLang.remindTimeLessThanDeadLine")); 
            remind[0].selectedIndex = 0;
            dl[0].selectedIndex = 0;
        }
        if(dl[0].selectedIndex==1){//自定义
            dl.css("width","auto");
            dl.css("float","left");
            $("#deadLineCalender").css("display","block");
            $("#deadLineDateTimeInput").attr("disabled",false);
        }else if(dl[0].selectedIndex==0){//无
            $("#deadLineCalender").css("display","none");
            $("#deadLineDateTimeInput").attr("value","");
            hiddenObj.value="";
            dl.removeAttr("style");
        }else{
        	dl.css("width","auto");
            dl.css("float","left");
            $("#deadLineCalender").css("display","block");
            $("#deadLineDateTimeInput").attr("disabled",true);
        }
    }
    //提醒
    if(obj.id=='advanceRemind'){
        if(getDateTimeValue(remind[0].value) >= mycal.val() && parseInt(remind[0].value) != 0){
            //未设置流程期限或流程期限小于,等于提前提醒时间
            alert(v3x.getMessage("edocLang.remindTimeLessThanDeadLine"));
            remind[0].selectedIndex = 0;
        }
    }
}
function init_newedoc(){
    var deadLineSelect = $("#deadline");
     $("#deadline option[value='0']").remove();//删除第一项 “无”
     deadLineSelect.prepend("<option value='-2'>"+v3x.getMessage("edocLang.edoc_deadline_custom")+"</option>");  //增加 “自定义”到第一个位置
     deadLineSelect.prepend("<option value='0'>"+v3x.getMessage("edocLang.edoc_deadline_no")+"</option>");  //增加“无”到第一个位置
     //当调用模板的时候，要将流程期限转换成日期值，并赋值给deadlineDatetime
     var mycal = $("#deadLineDateTimeInput");
     var hiddenObj = $("#deadLineDateTime")[0];
     var deadline2=$("#deadline2").val();
     if(deadline2=="-1"){//流程期限默认值是-1
         deadLineSelect.val(0);
     }else if(deadline2=="-2"){//-2表示自定义
         deadLineSelect.val(-2);
     }
     // 流程期限
     if (deadLineSelect&&deadLineSelect.length==1) {
        var deadLineTime = deadLineSelect[0].value;
        if(deadLineTime&&deadLineTime!="0"&&deadLineTime!="-2"){
            // 日期计算
            var dateValueStr = getDateTimeValue(deadLineTime);
            if (mycal&&hiddenObj) {
                mycal.val(dateValueStr);
                hiddenObj.value=dateValueStr;
            }
            deadLineSelect.css("width","auto");
            deadLineSelect.css("float","left");
            $("#deadLineCalender").css("display","block");
            $("#deadLineDateTimeInput").attr("disabled",true);
        }else if(deadLineTime!="0"){
        	var deadLineStr=$("#deadLineDateTime").val();
            if(deadLineStr){
                mycal.val(deadLineStr.substring(0,deadLineStr.lastIndexOf(":")));
            }
            deadLineSelect.css("width","auto");
            deadLineSelect.css("float","left");
            $("#deadLineCalender").css("display","block");
        }
     }
    $("#deadline").bind("change",function(){
        valiDeadAndLcqx(this);
     });
     $("#advanceRemind").bind("change",function(){
        valiDeadAndLcqx(this);      
     });
    <c:if test="${param.notOpenSave != 'true'}">
    //changyi 离开当前页面
    initLeave(${formModel.edocSummary.edocType});
    </c:if>
    
    edocFormDisplay();
    loadRelationButton();
    formInitialValue();

    //OA-22567 兼职人员test01的兼职单位将自建流程开关关闭后，调用模板拟文保存待发在编辑，弹出选择模板的窗口
    
    if(selfCreateFlow == false && ${archivedModify==null} && hasTemplate !="true" && '${templeteProcessId}' == ''){<%-- xiangfan 修改 修复设置不允许公文之间流程，拟文 选择个人模板时无限弹出选择模板窗口的错误 GOV-5038 --%>
        myBar.disabled("workflow"); 
        openTemplete(templeteCategrory,'${templete.id==null?1:templete.id}');
    }
    
    //G6 V1.0 SP1后续功能_流程期限--start
    if("${ctp:getSystemProperty('edoc.isG6')}" == "true"){
	    var deadline=document.getElementById("deadline");
	    if(deadline.disable!=true &&'${finalDeadline}'>0 && deadline.value==0 && document.getElementById("deadlineTime").value!=""){
	        var nowValue='${finalDeadline}';
	        deadline.options.add(new Option(document.getElementById("deadlineTime").value),nowValue);
	        for(var i=0;i<deadline.options.length;i++){
	            if(deadline.options[i].text.length==16){
	                deadline.options[i].selected=true;
	                deadline.options[i].value=nowValue;
	                break;
	            }
	        }
	    }
    }
    //G6 V1.0 SP1后续功能_流程期限--end
    initEdocIe10AutoScroll();
    
    <c:if test="${formModel.edocSummary.isQuickSend}">
    showQuickSend();
    if(document.getElementById("bodyType") && document.getElementById("bodyType").value=='HTML'){
       document.getElementById("fileUrl").disabled="true";
    }
    </c:if>
    if(document.getElementById("isQuickS")){
       if(jsEdocType==0){
         document.getElementById("isQuickS").title = "<fmt:message key='edoc.sendQuick_0.label'/>";
       }else if(jsEdocType==1){
         document.getElementById("isQuickS").title = "<fmt:message key='edoc.sendQuick_1.label'/>";
       }
    }
    
    //套红模板根据当前正文类型过滤
    resetTaohongList();
    
    <c:if test="${edocSummaryQuick!=null && edocSummaryQuick.taohongTemplateUrl!=null && taohongTemplateUrl!=''}">
    myBar.disabled("isQuickS");
    </c:if>
 }
 function resetTaohongList(){
 	var bType = "";
     var bodyTypeObj = document.getElementById("bodyType");
     if(bodyTypeObj != null){
     	bType = bodyTypeObj.value;
     }
     var taohongselectObj = document.getElementById("fileUrl");
 	if(taohongselectObj){
 		for (var i = taohongselectObj.options.length-1 ; i >= 0; i--) {
 			var a = taohongselectObj.options[i].value.split("&");
 			var selectBodyType = "";
 			if(a.length == 2){
 				selectBodyType = a[1];
 			}
 			if (selectBodyType != bType.toLowerCase() && selectBodyType!='') {
 				taohongselectObj.remove(i);
 		   }
 	   }
 	}
 }

window.onload=init_newedoc;


function getWFtype(){
  var type ="";
  if('${param.edocType}'== 0){
      type = "edocSend";
  }else if('${param.edocType}' == 1){
      type = "recEdoc";   
  }else{
      type = "signReport";    
  }
  return type;
}

function getPolicy(type){
  var policy={};
  policy.name = defaultNodeLable;
  policy.bianma = defaultNodeName;
  return policy;
}

function getParentFrame(){
  return window.parent.parent.parent;
}


function selectPeo(){
    var top2 = getParentFrame();
    var type = getWFtype();
    var selectPanels = "Department,Team,Post";
    var selectTypePara= "Member,Account,Department,Team,Post,Outworker,RelatePeople";
    var policy = getPolicy(type);
    top2.createProcessXml(type,top2,window, 
        '${currentUserId}', '${ctp:escapeJavascript(currentUserName)}','${currentUserAccountName}',
        policy.bianma,'${currentUserAccountId}',policy.name,selectPanels,selectTypePara
        );
}

function createEdocWFPersonal() {
    var type = getWFtype();
    var policy = getPolicy(type);
    var top2 = getParentFrame();
    if('${subState}' == '16'){//指定回退给发起人[直接提交给我]查看
      top2.showWFCDiagram(getA8Top(),'${caseId}','${processId}',false,false,null,window,'edoc',null,'');
    }else if('${isRepealTemplate}'=='true'){//撤销回来的模板流程
      top2.showWFCDiagram(getA8Top(),'','${processId}',false,false,null,window,'edoc',null,'');
    }else if('${isRepealFree}'=='true'){//撤销回来的自由流程
      top2.createWFPersonal(getA8Top(),getWFtype('${param.edocType}'), '${currentUserId}', '${currentUserName}'
          ,'${currentUserAccountName}','${processId}',window,policy.bianma,'${currentUserAccountId}',policy.name);
    }else if((isFromTemplate && templateType !='text')
        //当后台设置开关时，设置不能自建流程时，就不能编辑流程了，只能查看流程 (最开始 按钮置灰)
        //而且要在调用模板之后，当templeteProcessId有值时，才能点查看流程
        || '${selfCreateFlow}' == 'false') {//调用系统模板(非格式模板)后只能查看流程，调用个人模板后可以编辑流程
        if('${templeteProcessId}' != ''){
            top2.showWFTDiagram(getA8Top(),'${templeteProcessId}',window,'${ctp:escapeJavascript(currentUserName)}','${currentUserAccountName}');
        }
        //格式模板
        else{
          top2.createWFPersonal(getA8Top(),getWFtype('${param.edocType}'), '${currentUserId}', '${ctp:escapeJavascript(currentUserName)}'
              ,'${currentUserAccountName}','${processId}',window,policy.bianma,'${currentUserAccountId}',policy.name);
        }
    } else {//创建
      top2.createWFPersonal(getA8Top(),getWFtype('${param.edocType}'), '${currentUserId}', '${ctp:escapeJavascript(currentUserName)}'
          ,'${currentUserAccountName}','${processId}',window,policy.bianma,'${currentUserAccountId}',policy.name);
	}
    return false;
}


//因为调用模板时，是从外层框架中调用的，所以需要取消离开当前新建公文时弹出的确认对话框
function cancel_onbeforeunload(){
  $("body").attr("onbeforeunload","");
}

function setProcessTypeIdValue(value) {
    //GOV-5076.【收文管理-新建收文】先选择流程，流程节点默认为审批，再切换为"阅文"，再点击"编辑流程"，出现的页面里，每个人都变成了阅读 暂时pingbi
    if(${formModel.edocSummary.edocType==1}) {
        document.getElementById("processType").value = value;
        /*if(value == "2") {
            defaultPermName = "<fmt:message key='node.policy.yuedu' bundle='${v3xCommonI18N}'/>";
        } else {
            defaultPermName = "<fmt:message key='node.policy.shenpi' bundle='${v3xCommonI18N}'/>";
        }*/
    }
}

function initEdocIe10AutoScroll(){
  //OA-23050  调用公文模板后点击保存待发，提示填写标题后，点击确定，保存待发等的toolbar没有了  
  /*initIe10AutoScroll('formAreaDiv',130);*/
  initIe10AutoScroll('noteMinDiv', 120);
}

//OA-29541 后台设置不允许自建流程，前台拟文时关闭了模版的选择界面，填写标题，然后发送，这时正常弹出了无流程的提示，单击提示的确定，继续弹出了流程的选人界面
function sendCallBack_newEdoc(){
	var calDateTime=$("#deadLineDateTimeInput");
	var dateTimeObj=$("#deadLineDateTime");
	if(calDateTime&&dateTimeObj){
		dateTimeObj.val(calDateTime.val());
	}
	
  if("${selfCreateFlow}"=="false"){
    sendCallBack(1);
  }else{
    sendCallBack();
  }
}

//快速发文
function showQuickSend(){
    //快速发文的标识
    var isQuickSend=false;
    if(!document.getElementById("isQuickSend")){return;}
    if(document.getElementById("isQuickSend").checked){
    	isQuickSend= true;
    }

	 if(isQuickSend){
	 	  //流程控件
	 	  document.getElementById("sel_label_workflow").style.display="none";
	 	  document.getElementById("process_info_div").style.display="none";
	 	  document.getElementById("workflowInfo_div").style.display="none";
	 	  //交换控件
	 	  if(document.getElementById("sel_label_exchange")){
	 	   document.getElementById("sel_label_exchange").style.display="block";
	 	  }
	      if(document.getElementById("exchangeSel")){
	        document.getElementById("exchangeSel").style.display="block";
	      }
	    
	    //流程期限控件
	    document.getElementById("sel_label_deadline").style.display="none";
	    document.getElementById("deadline_div").style.display="none";
	    
	    //套红控件
	    if(jsEdocType !=1){
	      document.getElementById("sel_label_taohong").style.display="block";
	      document.getElementById("taohong_div").style.display="block";
	    }
	    
	    if(document.getElementById("bodyType") && document.getElementById("bodyType").value=='HTML'){
          document.getElementById("fileUrl").disabled="true";
        }

	    //提醒控件
	    document.getElementById("sel_label_tixing").style.display="none";
	    document.getElementById("tixing_div").style.display="none";
	    
	    //跟踪控件
	    document.getElementById("genzong_div").style.display="none";
	    document.getElementById("genzong_label").style.display="none";
	    
	    //归档控件
	    if(document.getElementById("sel_label_guidang") && document.getElementById("guidang_div")){
	       document.getElementById("sel_label_guidang").style.display="block";
	       document.getElementById("guidang_div").style.display="block";
	    }
	    
	    //基准时长
	    if(document.getElementById("jizhunshichang_label_div")){
	    	document.getElementById("jizhunshichang_label_div").style.display="none";
	    }
	    if(document.getElementById("jizhunshichang_label_div2")){
	    	document.getElementById("jizhunshichang_label_div2").style.display="none";
	    }
	    if(document.getElementById("jizhunshichang")){
	    	document.getElementById("jizhunshichang").type="hidden";
	    }
	    
	    //流程追溯
	    document.getElementById("zhuisu_label_div").style.display="none";
	    document.getElementById("zhuisu_div").style.display="none";
	    
	    //办理类型
	    if(document.getElementById("bllx_label_1")){
	       document.getElementById("bllx_label_1").style.display="none";
	    }
	    if(document.getElementById("bllx_label_2")){
	       document.getElementById("bllx_label_2").style.display="none";
	    }
	    
	    //按钮置灰
	    myBar.disabled("saveAs");
	    myBar.disabled("templete");
	    myBar.disabled("superviseSetup");
   }else{
        if((jsEdocType==0 && !confirm(edocLang.edoc_quicksend_return_send))||
           jsEdocType ==1 && !confirm(edocLang.edoc_quicksend_return_rec)){
          document.getElementById("isQuickSend").checked="true";
          return;
        }
        
   	  //流程控件
   		document.getElementById("sel_label_workflow").style.display="block";
   		document.getElementById("process_info_div").style.display="block";
   		document.getElementById("workflowInfo_div").style.display="block";
	 	  //交换控件
	 	  if(document.getElementById("sel_label_exchange")){
	 	   document.getElementById("sel_label_exchange").style.display="none";
	 	  }
	      if(document.getElementById("exchangeSel")){
	        document.getElementById("exchangeSel").style.display="none";
	      }
	    //流程期限控件
	    document.getElementById("sel_label_deadline").style.display="block";
	    document.getElementById("deadline_div").style.display="block";
	    //套红控件
	    if(jsEdocType !=1){
	       document.getElementById("sel_label_taohong").style.display="none";
	       document.getElementById("taohong_div").style.display="none";
	    }

	    
	    //提醒控件
	    document.getElementById("sel_label_tixing").style.display="block";
	    document.getElementById("tixing_div").style.display="block";
	    
	    //跟踪控件
	    document.getElementById("genzong_div").style.display="block";
	    document.getElementById("genzong_label").style.display="block";
	    
	    //归档控件
	    if(document.getElementById("sel_label_guidang") && document.getElementById("guidang_div")){
	      document.getElementById("sel_label_guidang").style.display="none";
	      document.getElementById("guidang_div").style.display="none";
	    }
	    //基准时长
	    if(document.getElementById("jizhunshichang_label_div")){
	    	document.getElementById("jizhunshichang_label_div").style.display="block";
	    }
	    if(document.getElementById("jizhunshichang_label_div2")){
	    	document.getElementById("jizhunshichang_label_div2").style.display="block";
	    }
	    if(document.getElementById("jizhunshichang")){
	    	document.getElementById("jizhunshichang").type="text";
	    }
	    
	    //流程追溯
	    document.getElementById("zhuisu_label_div").style.display="block";
	    document.getElementById("zhuisu_div").style.display="block";
	    
	    //办理类型
	    if(document.getElementById("bllx_label_1")){
	       document.getElementById("bllx_label_1").style.display="block";
	    }
	    if(document.getElementById("bllx_label_2")){
	       document.getElementById("bllx_label_2").style.display="block";
	    }
	    
	    //按钮还原
	    myBar.enabled("saveAs");
	    myBar.enabled("templete");
	    myBar.enabled("superviseSetup");
	    
   }
}

//快速发文--交换类型检查
function checkExchangeRole(){
	  var typeAndIds="";
	  var msgKey="edocLang.alert_set_departExchangeRole";	  
	  var obj=document.getElementById("edocExchangeType_depart");
	  if(obj==null){return true;}
	  var selectObj = document.getElementsByName("memberList")[0];
	  var selectdExchangeUserId = (selectObj.options[selectObj.selectedIndex]).value;
	  var selectdExchangeUserName = (selectObj.options[selectObj.selectedIndex]).innerHTML;

	  if(obj.checked)
	  {
		  <%-- xiangfan 添加  修复GOV-4911 Start --%>
		  var list='${ctp:escapeJavascript(deptSenderList)}';
		  if(list!=null&&list!="undifined"&&list!=""){
			  var _url= encodeURI(genericControllerURL+"edoc/selectDeptSender&memberList='"+list+"'");
			  var listArr=list.split("|");
			  if(listArr.length>1){
				  sendUserDepartmentId = v3x.openWindow({
			     		 url: _url,
			     		 width: 342,
				      	 height: 170,
				      	 resizable: "no"
			  	  });
			  	  if(sendUserDepartmentId=="cancel" || typeof(sendUserDepartmentId) == 'undefined'){
				  		<%--取消或者直接点击关闭--%>
						return false;
				  }
			  }else if(listArr.length==1){
				  sendUserDepartmentId=listArr[0].split(',')[0];;
			  }
			  document.getElementById("returnDeptId").value=sendUserDepartmentId;
		  }
		  <%-- xiangfan 添加  修复GOV-4911 End --%>
		  
	    typeAndIds="Department|"+sendUserDepartmentId;	  
	    selectdExchangeUserId=""; 
	  }
	  else
	  {
	  	typeAndIds="Account|"+currentUserAccountId;
	  	msgKey="edocLang.alert_set_accountExchangeRole";
	  }
	  var requestCaller = new XMLHttpRequestCaller(this, "ajaxEdocExchangeManager", "checkExchangeRole",false);
		  requestCaller.addParameter(1, "String", typeAndIds);
		  requestCaller.addParameter(2, "String", selectdExchangeUserId);
	  var ds = requestCaller.serviceRequest();
	  if(ds=="check ok")
	  {
		  return true;
	  }else if(ds == "changed")//xiangfan 添加逻辑判断
	  {
		  alert("<fmt:message key='edoc.Cancel.exchange.privileges1' />" +"<fmt:message key='edoc.Cancel.exchange.privileges2'/>");
		  return false;
	  }
	  else
	  {
	    alert(_(msgKey,ds));
	  }
	  return false;
}

function showMemberList(){
	var memberListDiv = document.getElementById("selectMemberList");
	memberListDiv.style.display = "";
}
function hideMemberList(){
	var memberListDiv = document.getElementById("selectMemberList");
	memberListDiv.style.display = "none";
}


function guidangFromTemplete(selectObjs){
	if(document.getElementById("isQuickSend") && document.getElementById("isQuickSend").checked){
		pigeonholeEvent(selectObjs,'<%=ApplicationCategoryEnum.edoc.key()%>','finishWorkItem',this.sendForm);
  }else{
  	pigeonholeEvent(selectObjs,'<%=ApplicationCategoryEnum.edoc.key()%>','',this.sendForm);
  }
	
}
//-->
</script>
</head>
<body style="overflow: hidden;" class="h100b page_color" onUnload="unload('${formModel.edocSummaryId}');">

<div class="newDiv">

<form name="sendForm" id="sendForm" method="post">
<%--发转发时，设置的收文的affairId --%>
<input id="forwordtosend_recAffairId" name="forwordtosend_recAffairId" type="hidden" value="${forwordtosend_recAffairId }"/>
<%--转发时 设置forwardMember，发送后需要保存到affair中 --%>
<input id="forwardMember" name="forwardMember" type="hidden" value="${forwardMember }"/>
<%-- 从待登记 直接到收文分发 记录下签收id 
    还可能调用模板，在controller中将签收id保存到request中，所以这里的签收id的值可能从两种方式获得
--%>
<input id="waitRegister_recieveId" name="waitRegister_recieveId" type="hidden" 
            value="${!empty param.recieveId ? param.recieveId : waitRegister_recieveId }"/>

<input id="process_desc_by" name="process_desc_by" type="hidden"/>
<input id="process_xml" name="process_xml" type="hidden" value=""/>
<input id="process_rulecontent" name="process_rulecontent" type="hidden"/>

<input id="readyObjectJSON" name="readyObjectJSON" type="hidden"/>
<input id="process_subsetting" name="process_subsetting" type="hidden"/>

<input id="moduleType" name="moduleType" type="hidden"/>
<input id="workflow_newflow_input" name="workflow_newflow_input" type="hidden"/>
<input id="workflow_node_peoples_input" name="workflow_node_peoples_input" type="hidden"/>
<input id="workflow_node_condition_input" name="workflow_node_condition_input" type="hidden"/>

<input id="processId" name="processId" type="hidden" value="${processId }"/>
<input id="caseId" name="caseId" type="hidden" value="${caseId }"/>
<input id="subObjectId" name="subObjectId" type="hidden"/>
<input id="currentNodeId" name="currentNodeId" type="hidden"/>

<%--当调用的不是正文模板时，就保存模板id,表示流程已经编辑了，前台发文时就可以直接发送了 --%>
<input id="templeteProcessId" name="templeteProcessId" type="hidden" value="${templeteProcessId}"/>

<%-- GOV-4927 公文管理，发文内部文号重复时，提示错误！ --%>
<input type="hidden" id="edocType_mark" name="edocType_mark" value="${param.edocType }" />

<!-- 当通过待登记进行转发文时，关联id用签收id -->
<input type="hidden" id="backBoxToEdit" name="backBoxToEdit" value="${param.backBoxToEdit }" />
<input type="hidden" id="recieveId" name="recieveId" value="${recieveId }" />
<input type="hidden" id="registerId" name="registerId" value="${registerId }" />
<input type="hidden" id="distributeEdocId" name="distributeEdocId" value="${distributeEdocId }" />

<!-- 区别已登记和待分发所关联的发文-->
<input type="hidden" id="forwordType" name="forwordType" value="${forwordType }" />
<input type="hidden" id="app" name="app" value="${param.app }" />
<input type="hidden" id="edocGovType" name="edocGovType"/>

<%--puyc 添加 --%>

 <input type="hidden" id="subType" name="subType" value="${param.subType }"/>
 <input type="hidden" id="relationRecd" name="relationRecd" value="isNot"/>
 <input type="hidden" id="relationRecId" name="relationRecId" value="-1"/>
 <input type="hidden" id="relationSend" name="relationSend" value="isNot"/>
 <input type="hidden" id="recSummaryIdVal" name="recSummaryIdVal" value="-1"/><!-- 分发，收文的summaryId -->
 <%--puyc 添加  结束--%>
<input type="hidden" id="pageview" name="pageview" value="${pageview}"/><%//阅转办页面跳转标识  wangjingjing %>
<%-- 套红代码的JS中需要使用此变量--%>
<input type="hidden" id="currContentNum" name="currContentNum" value="0"/>
<%-- 后台保存获取数据需要使用此变量--%>
<input type="hidden" id="contentNo" name="contentNo" value="0"/>
<input type="hidden" id="isUniteSend" name="isUniteSend" value="false"/> 
<input type="hidden" id="appName"name="appName" value="<%=ApplicationCategoryEnum.edoc.getKey()%>">
<input type="hidden" name="comm" id="comm" value="${comm}">
<input type="hidden" name="id" value="${formModel.edocSummaryId}">
<%-- 办文转起草标识，目标是屏蔽第一次加载转文后的正文修改提示--%>
<input type="hidden" id="forwordtosend" name="forwordtosend" value="${forwordtosend}">
<%-- 套红代码的JS中需要使用此变量--%>
<input type="hidden" id="summary_id" name="summary_id" value="${formModel.edocSummaryId}">
<input type="hidden" id="summaryId" name="summaryId" value="${formModel.edocSummaryId}">
<%-- changyi 性能优化，用于判断是否是新建公文--%>
<input type="hidden" id="newSummaryId" name="newSummaryId" value="${!empty param.summaryId ? param.summaryId :newSummaryId }">

<input type="hidden" id = "edocType" name="edocType" value="${formModel.edocSummary.edocType}"/>
<input type="hidden" name="exchangeId" value="${param.exchangeId}"/>
<input type="hidden" name="actorId" value="${actorId}"/>
<input type="hidden" name="returnDeptId" id="returnDeptId" value="">
<input type="hidden" name="currContentNum" id="currContentNum" value="0">
<c:choose>
<c:when test ="${empty templeteId}">
    <c:set value="${param.templeteId }" var="tempId"/>
</c:when>
<c:otherwise>
    <c:set value="${templeteId }" var="tempId"/>
</c:otherwise>
</c:choose>

<input type="hidden" id="templeteId" name="templeteId" value="${tempId}" />
<input type="hidden" name="currentNodeId" value="start" />
<input type="hidden" name="supervisorId" id="supervisorId" value="${colSupervisors }">
<input type="hidden" name="supervisors" id="supervisors" value="${colSupervise.supervisors }">
<%--用来保存模板自带的督办人员 BUG-OA-39192--%>
<input type="hidden" name="unCancelledVisor" id="unCancelledVisor" value="${unCancelledVisor}">
<input type="hidden" name="sVisorsFromTemplate" id="sVisorsFromTemplate" value="${sVisorsFromTemplate}">
<%--OA-26859 调用发文模板后将其存为个人模板，督办设置，基准时长没有保存（如图） --%>
<!--<input type="hidden" name="awakeDate" id="awakeDate" value="${awakeDate}">-->
<input type="hidden" name="awakeDate" id="awakeDate" value="${superviseDate}"> 
<input type="hidden" name="superviseTitle" id="superviseTitle" value="${v3x:toHTML(colSupervise.title) }">
<input type="hidden" name="fromSend" id="fromSend" value="${fromSend}"/>
<input type="hidden" name="loginAccountId" id="loginAccountId" value="${v3x:currentUser().loginAccount}" >
<input type="hidden" name="orgAccountId" id="orgAccountId" value="${formModel.edocSummary.orgAccountId}" >
<%--strEdocId:收文登记的时候，保存来文EdocSummary的ID--%>
<input type="hidden" name="strEdocId" id="strEdocId" value="${strEdocId}" >
<input type="hidden" name="__ActionToken" readonly value="SEEYON_A8" > <%-- post提交的标示，先写死，后续动态 --%>
<input type="hidden" name="archiveId" value="${formModel.edocSummary.archiveId}">
<!-- 接收从弹出页面提交过来的数据 -->
<input type="hidden" name="popJsonId" id="popJsonId" value="">
<input type="hidden" name="popNodeSelected" id="popNodeSelected" value="">
<input type="hidden" name="popNodeCondition" id="popNodeCondition" value="">
<input type="hidden" name="popNodeNewFlow" id="popNodeNewFlow" value="">
<input type="hidden" name="allNodes" id="allNodes" value="">
<input type="hidden" name="nodeCount" id="nodeCount" value="">
<%--判断文单正文附件是否被修改--%>
<input type="hidden" name="isModifyContent" id="isModifyContent" value="0">
<input type="hidden" name="isModifyForm" id="isModifyForm" value="0">
<input type="hidden" name="isModifyAtt" id="isModifyAtt" value="0">
<%--退回拟稿人后的选择 --%>
<input type="hidden" name="draftChoose">
<%--待发时传递过来 --%>
<input type="hidden" name="affairId" value="${param.affairId }">
 <input type="hidden" id="checkOption" name="checkOption" value="${checkOption}" />
 
 <input type="hidden" name="docMarkValue" id="docMarkValue" value ="${formModel.edocSummary.docMark}">
<input type="hidden" name="docMarkValue2" id="docMarkValue2" value ="${formModel.edocSummary.docMark2}">
<input type="hidden" name="docInmarkValue" id="docInmarkValue" value ="${formModel.edocSummary.serialNo}">

<span id="people" style="display:none;">
<c:out value="${peopleFields}" escapeXml="false" />
</span>
<script type="text/javascript">
    <!--
    var isConfirmExcludeSubDepartment_wf = true;
    //-->
</script>
<v3x:selectPeople id="wf" panels="Department,Post,Team" selectType="Department,Member,Post,Team" departmentId="${sessionScope['com.seeyon.current_user'].departmentId}"
 jsFunction="setPeopleFields(elements, 'detailIframe.mainIframe.listFrame')" viewPage="selectNode4EdocWorkflow"/>
  
<script>
<!--
onlyLoginAccount_wf=false;
//-->
</script>
<script>isNewOfficeFilePage=true;</script>

<script>showOriginalElement_wf=false;</script>
<script>
showAccountShortname_wf="yes";
accountId_wf="${formModel.edocSummary.orgAccountId}";
</script>


<script> 
function insertAttachmentFn(){
    insertAttachment();
    resizeLayout();
}
function quoteDocumentEdocFn(){
    quoteDocumentEdoc('${appType}');
    resizeLayout();
}
function resizeLayout(){
    $("#attachmentArea img,#attachment2Area img").click(function(){
        resizeLayout()
    });
    var _h=0;
    if (v3x.isMSIE9){
        _h=5;
    }
    $("#textarea_fy").height($("body").height()-$("#tb_height").height()-$("#form_height").height()-_h-60);
    $("#formAreaDiv").height($("body").height()-$("#tb_height").height()-$("#form_height").height()-_h);
}

var newEdocBodyId=fileId;
//var newEdocBodyId;
//调用模板的时候使用。
if(isFromTemplate || ("${comm}"=="distribute"||"${comm}"=="register") ) {
    contentOfficeId.put("0",fileId); 
}

<fmt:message key="common.${((isFromTemplate && templateType !='text') || (templateType!='text' && selfCreateFlow == 'false') || subState == '16')? 'view':'design'}.workflow.label" bundle="${v3xCommonI18N}" var="workflowLable" />

var myBar = new WebFXMenuBar("${pageContext.request.contextPath}");

var saveAs = new WebFXMenu;
saveAs.add(new WebFXMenuItem("saveAsText", "<fmt:message key='templete.text.label' />", "saveAsTemplete('text')", "<c:url value='/apps_res/collaboration/images/text.gif'/>"));
saveAs.add(new WebFXMenuItem("saveAsWorkflow", "<fmt:message key='templete.workflow.label' />", "saveAsTemplete('workflow')", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>"));
saveAs.add(new WebFXMenuItem("saveAsTemplete", "<fmt:message key='templete.category.type.0' />", "saveAsTemplete('templete')", "<c:url value='/apps_res/collaboration/images/text_wf.gif'/>"));

var insert = new WebFXMenu;
insert.add(new WebFXMenuItem("", "<fmt:message key='common.toolbar.insert.localfile.label' bundle='${v3xCommonI18N}' />", "insertAttachmentFn()"));
insert.add(new WebFXMenuItem("", "<fmt:message key='common.toolbar.insert.mydocument.label' bundle='${v3xCommonI18N}' />", "quoteDocumentEdocFn()"));

/*var workflow = new WebFXMenu;
//workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.no.label' />", "doWorkFlow('no')"));
workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.new.label' />", "doWorkFlow('new')"));

if(window.dialogArguments) {
    workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.edit.label' />", "designWorkFlow('detailIframe.mainIframe.listFrame')"));
} else {
    workflow.add(new WebFXMenuItem("", "<fmt:message key='workflow.edit.label' />", "designWorkFlow('detailIframe.mainIframe.listFrame')"));
}
*/
<c:if test="${archivedModify==null}">

    <c:if test="${param.backBoxToEdit=='true'}">    
        myBar.add(new WebFXMenuButton("save2", "<fmt:message key='edoc.receive.register.saveToDraftBox'/>", "saveToBackBox()", [1,5], "", null));
    </c:if> 

    //阅文转办文 不显示草稿箱
    <c:if test="${param.backBoxToEdit!='true' && param.pageview != 'listReading' && isAgent!=true && distributerId==v3x:currentUser().id && param.listType!='listV5Register'}">  //TODO --&&  enableWaitSend!='false'
        myBar.add(new WebFXMenuButton("save", "<fmt:message key='edoc.receive.register.saveToDraftBox'/>", "save()", [1,5], "", null));
    </c:if>

    <%-- 从待登记点登记纸质收文时，草稿箱显示保存待发 --%>
    <c:if test="${(param.listType=='listV5Register' && enableRecWaitSend!='false' || isG6Ver && param.recListType=='listDistribute')}">
        myBar.add(new WebFXMenuButton("save", "<fmt:message key='edoc.receive.register.saveToDraftBox'/>", "save()", [1,5], "", null));
    </c:if>

    myBar.add(new WebFXMenuButton("saveAs", "<fmt:message key='saveAs.label'/><fmt:message key='templete.personal.label'/>", "saveAsTemplete()", [3,5], "",null));    
    
    /*if(window.dialogArguments) {
        myBar.add(new WebFXMenuButton("workflow", "${workflowLable}", "createEdocWFPersonal()", [3,6], "", null));
    } else {
        myBar.add(new WebFXMenuButton("workflow", "${workflowLable}", "createEdocWFPersonal()", [3,6], "", null));
    }*/
    
    <c:if test="${param.backBoxToEdit!='true' || param.canOpenTemplete == 'true'}">
        myBar.add(new WebFXMenuButton("templete", "<fmt:message key='common.toolbar.templete.label' bundle='${v3xCommonI18N}'/>", "openTemplete(templeteCategrory,'${templete.id==null?1:templete.id}')", [3,7], "", null));
    </c:if>
    
</c:if>

<c:if test="${archivedModify}">
    myBar.add(new WebFXMenuButton("saveArchived", "<fmt:message key='common.toolbar.save.label' bundle='${v3xCommonI18N}'/>", "saveArchived()", [1,5], "", null));
</c:if> 

/*
if(isFromTemplate) {
    myBar.add(new WebFXMenuButton("workflow", "<fmt:message key='workflow.label' />", "designWorkFlow('detailIframe')", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>", "", null));
} else {
    myBar.add(new WebFXMenuButton("workflow", "<fmt:message key='workflow.label' />", "", "<c:url value='/apps_res/collaboration/images/workflow.gif'/>", "", workflow));
}*/     
myBar.add(new WebFXMenuButton("insert", "<fmt:message key='common.toolbar.insert.label' bundle='${v3xCommonI18N}'/>", null, [1,6], "", insert));

//var hasOfficeOcx=${v3x:hasPlugin('officeOcx')};
//if(hasOfficeOcx){myBar.add(createOfficeMenu(v3x));}
<c:if test="${archivedModify==null}"> 
    myBar.add(${v3x:bodyTypeSelector("v3x")});
</c:if>

myBar.add(new WebFXMenuButton("content", "<fmt:message key='common.toolbar.content.label' bundle='${v3xCommonI18N}' />","dealPopupContentWinWhenDraft('0');", [8,10], "", null));

<c:if test="${appType==19}">
    <c:if test="${hasBody1}">
        myBar.add(new WebFXMenuButton("content", "<fmt:message key='edoc.contentnum1.label' />","dealPopupContentWinWhenDraft('1');", [8,10], "", null));
    </c:if>
    <c:if test="${hasBody2}">
        myBar.add(new WebFXMenuButton("content", "<fmt:message key='edoc.contentnum2.label' />","dealPopupContentWinWhenDraft('2');", [8,10], "", null));
    </c:if>
//myBar.add(new WebFXMenuButton("taohong", "<fmt:message key='edoc.action.form.template' />","taohongWhenDraft('edoc');", '<c:url value="common/images/zwth.gif"/>', "", null));
</c:if>

<c:if test="${archivedModify==null}"> 
    myBar.add(new WebFXMenuButton("superviseSetup", "<fmt:message key='common.toolbar.supervise.label' bundle='${v3xCommonI18N}' />", "openSuperviseWindow()", [21,1], "", null));
    <c:if test="${appType==19 && (subState!='2' && subState!='3' &&subState!='4' &&subState!='5' && subState!='16'&& subState!='18')}">
    myBar.add(new WebFXMenuButton("isQuickS", "<input id='isQuickSend' name='isQuickSend' type='checkbox' onclick='showQuickSend();' <c:if test='${formModel.edocSummary.isQuickSend}'>checked='true'</c:if> /> <fmt:message key='edoc.sendQuick_0.label'/>", "", [21,100] , "", null));
    </c:if>
    <c:if test="${appType==20 && (subState!='2' && subState!='3' && subState!='4' && subState!='5' && subState!='16'&& subState!='18')}">
    myBar.add(new WebFXMenuButton("isQuickS", "<input id='isQuickSend' name='isQuickSend' type='checkbox' onclick='showQuickSend();' <c:if test='${formModel.edocSummary.isQuickSend}'>checked='true'</c:if> /> <fmt:message key='edoc.sendQuick_1.label'/>", "", [21,100] , "", null));
    </c:if>
</c:if>
<c:if test="${archivedModify}">
    myBar.add(new WebFXMenuButton("returnlist", "<fmt:message key='common.toolbar.back.label' bundle='${v3xCommonI18N}'/>", "returntolist('"+jsEdocType+"')", [4,1], "", null));
</c:if>
var categroy2Forms = '${categroy2Forms}';
</script>


<table border="0" cellspacing="0" cellpadding="0" class="w100b">

<tr valign="top">
    <td colspan="${not empty tempId?8:7 }"  >
		<c:if test="${hasBarCode}">
		<v3x:webBarCode readerId="PDF417Reader" readerCallBack="initDate"/>
		</c:if>
        <table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
        <tr valign="top">
            <td>    
                <div id="formAreaDiv" class="scrollList bg_color_white border_all">
                    <div id="relationRec" style="display:none;position:relative;left:720px;"> 
                        <a href="#" onclick="relationRecv()" >
                            <font color="#0278A9"><fmt:message key='edoc.docmark.inner.send'/><fmt:message key='edoc.associated.geting'/></font>
                        </a>
                    </div>
                    <div id="relationSen" align="right" style="display:none;"> <a href="#" onclick="relationSendV()" ><font color=red><fmt:message key='edoc.associated.posting'/></font></a></div>
                    <div style="display:none">
                        <textarea id="xml" cols="40" rows="10">
                             ${formModel.xml}
                        </textarea>
                    </div>
                    <div style="display:none">
                        <textarea id="xslt" cols="40" rows="10">   
                            ${formModel.xslt}
                        </textarea>
                    </div>
            
                    <!-- 愚昧啊,div 加个边框就可以滚动了,要不就把页面撑开了 -->
                    <br />
                    <div id="html" name="html" style="border:1px solid;border-color:#FFFFFF;height:0px;">${formStr}</div>
           
                    <div id="img" name="img" style="height:0px;"></div> 
                    <div style="display:none">
                        <textarea name="submitstr" id="submitstr" cols="80" rows="20"></textarea>
                    </div>
            
                </div><!-- formAreaDiv -->        
            </td>
            
            <td width="35" id="noteAreaTd" nowrap="nowrap" class="h100b page-color" 
             <c:if test="${archivedModify!=null}"> style="display:none;"</c:if>
            >
                

                                <div id="noteMinDiv" style="height: 100%" class="sign-min-bg">
                                    <div class="sign-min-label-newcoll" style="height: 100%" onclick="changeLocation('senderNote');showNoteArea()">
                                        <div class=more_btn></div>
                                        <div class="span_text" style="height:80px;line-height:30px;"><fmt:message key="sender.note.label" /></div>
                                    </div>
                                </div>
                                <table id="noteAreaTable" width="100%" height="100%" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                        <td height="25">
                                            <div id="hiddenPrecessAreaDiv" onclick="hiddenNoteArea()" title="<fmt:message key='common.display.hidden.label' bundle='${v3xCommonI18N}' />"></div>
                                            <script type="text/javascript">
                                                var panels = new ArrayList();
                                                panels.add(new Panel("senderNote", '<fmt:message key="sender.note.label" />'));
                                                //panels.add(new Panel("colQuery", '<fmt:message key="edoc.query.label"/>'));
                                                showPanels(false);
                                            </script>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td height="25" class="senderNode"><fmt:message key="sender.note.label"/>(<fmt:message key="common.charactor.limit.label" bundle="${v3xCommonI18N}"><fmt:param value="500" /></fmt:message>)<td>
                                    </tr>
                                    <tr id="senderNoteTR" style="display:none;">
                                        <td class="note-textarea-td h100b">
                                            <input type="hidden" name="policy" value="${policy}">
                                            <textarea id="textarea_fy" style="height:100%" cols="" rows="7" name="note" validate="maxLength" inputName="<fmt:message key='sender.note.label' />" maxSize="500" class="note-textarea wordbreak"><c:out value='${formModel.senderOpinion.content}' escapeXml='true' /></textarea>
                                        </td>
                                    </tr>
                                    <tr id="colQueryTR" style="display:none;">
                                        <td>&nbsp;</td>
                                    </tr>                       
                                </table>

            </td>
        </tr>
        </table>
    </td>
</tr>
</table>

<div style="display:none" id="processModeSelectorContainer">
</div>

</form>

</div>

<iframe name="personalTempleteIframe" scrolling="no" frameborder="0" height="0" width="0" class="hidden"></iframe>

<script type="text/javascript">
//initWorkFlow();
initProcessXml();
hiddenNoteArea();
<c:if test="${isFromTemplate}" >
isFromTemplate = true;
</c:if>
<c:if test="${not isFromTemplate}" >       
isFromTemplate = false;
</c:if>

<c:if test="${!empty formModel.senderOpinion.content}">
changeLocation('senderNote');
showNoteArea();
</c:if>
if(v3x.isIpad){
    var oHtml = document.getElementById('html'); 
    if(oHtml){
        oHtml.style.height = "470px";   
        oHtml.style.overflow = "auto";  
        touchScroll("html");
    }
}

previewFrame('Down');

</script>
<script type="text/javascript">
function initDate(reader){
    if(reader){
    	//var codetext = reader.GetBarCodeBuff();//必须有14个^
        //标题
        var subject = document.getElementById("my:subject");
        if(subject)
            subject.value = reader.GetDocTitle();
        //来问字号
        var docMark = document.getElementById("my:doc_mark");
        if(docMark)
            docMark.value = reader.GetDocIssue();
        //主送单位
        var sendTo = document.getElementById("my:send_unit");
        if(sendTo)
            sendTo.value = reader.GetReceCompany();
        //成文日期
        var edocDate = document.getElementById("my:createdate");
        if(edocDate)
            edocDate.value = reader.GetDocTime();
        
        var options;
        //公文密级
        var urgentLevel = document.getElementById("my:secret_level");
        var scanUrgentLevel = reader.GetSecuLevel();
        if(urgentLevel && scanUrgentLevel){
            options = urgentLevel.options;
            if(options){
                for(var i=0;i<options.length;i++){
                    if(options[i].text == scanUrgentLevel)
                    	options[i].selected = true;
                }
            }
        }
        //紧急程度
        var secretLevel = document.getElementById("my:urgent_level");
        var scanSecretLevel = reader.GetUrgenLevel();
        if(secretLevel && scanSecretLevel){
            options = secretLevel.options;
            for(var i=0;i<options.length;i++){
                if(options[i].text == scanSecretLevel)
                    options[i].selected = true;
            }
        }
    }
}
</script>
<div class="hidden">
<iframe id="formIframe" name="formIframe" scrolling="no" frameborder="1" height="0px" width="0px"></iframe>
<iframe name="toXmlFrame" scrolling="no" frameborder="1" height="0px" width="0px"></iframe>
<iframe id="_selectIframe" src="edocController.do?method=newEdocIframe" frameborder="1" style="position: absolute;top:10px;left:10px;width:800px;height:500px;display: none;z-index: :1000;"></iframe>

</div>
<script type="text/javascript">
<c:if test="${hasBarCode}">
        if(registerType == "3"){
            if(openBarCodePort()){
                alert("<fmt:message key='edoc.scanningGun.success'/>");
            }else{
                alert("<fmt:message key='edoc.scanningGun.fail'/>");
            }
        }
</c:if>

$(function(){ 
    $("#noteAreaTd div:eq(0)").click(function(){ 
        $("#noteAreaTd table").height($("#noteAreaTd").parents("table").find("tr:eq(0) td:eq(0)").height()-15) 
    }); 
    resizeFun(); 
    $(window).resize(resizeFun); 
}) 
function resizeFun(){ 
    $("#newDiv").height($("body").height()); 
    $("#deadline").addClass("left"); 
    $("#lcqx_time").show(); 
    resizeLayout(); 
}
</script>
</body>
</html>