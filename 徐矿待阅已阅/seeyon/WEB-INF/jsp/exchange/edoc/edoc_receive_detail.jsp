<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="com.seeyon.ctp.common.SystemEnvironment" %>
<%--
    这是一个内嵌页面
 --%>
<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/jquery-ui.custom.css${v3x:resSuffix()}" />">
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery-ui.custom.min.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/jquery/jquery.plugin.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/jquery-ui.custom.js${v3x:resSuffix()}" />"></script>
<style type="text/css">
th {
    background-color: #EFEBDE;
    border-top-width: 1px;
    border-right-width: 1px;
    border-bottom-width: 1px;
    border-left-width: 1px;
    border-top-style: solid;
    border-right-style: solid;
    border-bottom-style: solid;
    border-left-style: solid;
    border-top-color: #FFFFFF;
    border-right-color: #808080;
    border-bottom-color: #808080;
    border-left-color: #FFFFFF;
    font-size: 12px;
    font-weight: normal;
}
</style>
<script type="text/javascript">

isNeedCheckLevelScope_grantedDepartId = true;
//已签收保留上次选择的人员，如果选人回调返回false需要用上次选择的人员覆盖本次，由于选人组件不能在回调方法中控制本次选人是否有效，只能临时采用这种方式
var oldElements_grantedDepartId;

function openEdoc(){
    _url = edocURL + "?method=edocDetailInDoc&summaryId=${summary.id}&openFrom=lenPotent&lenPotent=200&showHl=false";
    //alert(_url)
    v3x.openWindow({
        url: _url,
        workSpace : 'yes',
        resizable : "false",
        dialogType:"open"
    });
}




accountId_grantedDepartId="${exchangeAccountId}";

//退回
function stepBack() {
    var exchangeSendEdocId = '${bean.id}';

    var requestCaller = new XMLHttpRequestCaller(this, "edocExchangeManager",
            "receiveRecordIsCanStepBack", false);
    requestCaller.addParameter(1, "String", exchangeSendEdocId);
    var rs = requestCaller.serviceRequest();
    if (rs != "yes") {
        doEndSign_exchange(rs);
        return;
    }

    getA8Top().win123 = getA8Top().v3x.openDialog({
        title:'<fmt:message key="exchange.stepBack"/>',
        transParams:{'parentWin':window},
        url: 'exchangeEdoc.do?method=openStepBackDlg&exchangeSendEdocId=' + exchangeSendEdocId + CsrfGuard.getUrlSurffix(),
        width:"400",
        height:"300"
    });

}

/**
 * 回退窗口的回调函数
 */
function huituiCallback(returnValues) {

    if (returnValues) {

        if (1 == returnValues[0]) {
            var canSubmit = false;
            var requestCaller = new XMLHttpRequestCaller(this,
                    "edocExchangeManager", "checkEdocSendMember", false);
            requestCaller.addParameter(1, 'String', '${bean.id}');
            var re = requestCaller.serviceRequest();
            if (re[0] == 'false') {
                if (re[1] == 'true') {// 发送人已经不是收发员了，是否让其它收发员竞争
                    if (confirm(v3x
                            .getMessage('ExchangeLang.edoc_receive_stepBack_ToOther_exchangeRole'))) {
                        canSubmit = true;
                        document.getElementById("detailForm")['oneself'].value = "false";
                    }
                } else {// 发送人已经不是收发员了，并且发送单位已经没有收发员了
                    alert(v3x
                            .getMessage('ExchangeLang.edoc_receive_stepBack_hasnot_exchangeRole'));
                }
            } else if (re[0] == 'true' || re[0] == 'null') {
                canSubmit = true;
            }
            if (canSubmit) {
                var formObj = document.getElementById("detailForm");
                // formObj.target = "detailFrame";
                document.getElementById("stepBackInfo").value = returnValues[3];
                formObj.action = '${exchangeEdoc}?method=stepBack&stepBackSendEdocId='
                        + returnValues[1]
                        + '&stepBackEdocId='
                        + returnValues[2] + CsrfGuard.getUrlSurffix();
                formObj.submit();
            }
        }
    }
}

function openStepBackInfo(readOnly) {
    var exchangeSendEdocId = '${bean.id}';
    getA8Top().win123 = getA8Top().v3x.openDialog({
        title:"<fmt:message key='exchange.stepBack'/>",
        url:'exchangeEdoc.do?method=openStepBackDlg4Resgistering&resgisteringEdocId='+exchangeSendEdocId + '&readOnly=1',
        transParams:{'parentWin':window},
        width:"400",
        height:"300",
        resizable:"0",
        scrollbars:"true"
    });
}

</script>
<script type="text/javascript">
function setPeopleFields(elements){
    if(elements){
        var obj1 = getNamesString(elements);
        var obj2 = getIdsString(elements,false);

        var tempModelTemp = '${modelType}';
        var test = "${v3x:escapeJavascript(CurrentUser.name)}";
        if(tempModelTemp=="received"){
            var oldRegisterUserName = document.getElementById("memberId").value;
            try {

            	//变更登记人
    			if("${isG6Ver}"!="true" || "${isAutoRegister}" == "false"){
    				var requestCaller = new XMLHttpRequestCaller(this, "edocExchangeManager", "changeRegisterEdocPerson", false);
    	            requestCaller.addParameter(1,'String','${edocRecieveRecordID4ChgRegUser}');
    	            requestCaller.addParameter(2, "String", getIdsString(elements,false));
    	            requestCaller.addParameter(3, "String", getNamesString(elements));
    	            requestCaller.addParameter(4, "String", "${v3x:escapeJavascript(CurrentUser.name)}");
    	            requestCaller.addParameter(5, "String", "${v3x:currentUser().id}");
    	            rs = requestCaller.serviceRequest();
    	            //GOV-5030 【公文管理】-【收文管理】-【签收-已签收】列表对未登记数据进行变更登记人操作时没有对收文登记权限进行判断
   	                if(rs == "false"){
   	                	elements_grantedDepartId = oldElements_grantedDepartId;
   	                    alert(getNamesString(elements)+"<fmt:message key='edoc.Registrationprivileges.label' bundle='${edocI18N}'/>");
   	                    return;
   	                }
				}
				//变更分发人
    			else{
    				var requestCaller = new XMLHttpRequestCaller(this, "edocExchangeManager", "changeDistributeEdocPerson", false);
    	            requestCaller.addParameter(1,'String','${edoeRegisterId4ChgDrcUser}');
    	            requestCaller.addParameter(2, "String", getIdsString(elements,false));
    	            requestCaller.addParameter(3, "String", getNamesString(elements));
    	            requestCaller.addParameter(4, "String", "${v3x:escapeJavascript(CurrentUser.name)}");
    	            requestCaller.addParameter(5, "String", "${v3x:currentUser().id}");
    	            rs = requestCaller.serviceRequest();
    	            if(rs == "false"){
    	            	elements_grantedDepartId = oldElements_grantedDepartId;
   	                    alert(getNamesString(elements)+"<fmt:message key='edoc.distributeprivileges.label' bundle='${edocI18N}'/>");
   	                    return;
   	                }
        		}

            }
            catch (ex1) {
                alert("Exception : " + ex1);
                return false;
            }
            oldElements_grantedDepartId = elements_grantedDepartId;
            document.getElementById("received_registerUserId").innerHTML = getNamesString(elements);
            document.getElementById("memberId").value = getNamesString(elements);
            document.getElementById("registerUserId").value = getIdsString(elements,false);

            //修复bug GOV-3056 收文管理-签收-已签收，打开一条修改登记人后，跳转的页面有问题
            window.close();
            //modelTransfer('received');
        }

      	//OA-48255 签收时切换登记人，切换不成功
        $("#memberId").val(getNamesString(elements));
        $("#registerUserId").val(getIdsString(elements, false));
        //document.getElementById("memberId").value = getNamesString(elements);
        //兼容chrome OA-26828 【多浏览器】公文交换-代签收下打开签收单，填写纸质附件说明，然后点击打印，打印页面，纸质附件说明是空白。ie是正常
        //document.getElementById("memberId").setAttribute("value", getNamesString(elements));
        //document.getElementById("registerUserId").value = getIdsString(elements,false);
    }
}

//初始数据
function initiate(modelType){
    <c:choose>
        <c:when test="${sendEntityName!=null}">
            <c:set value="${sendEntityName}" var="theSenderName" />
        </c:when>
        <c:otherwise>
            <c:set value="${v3x:showOrgEntitiesOfTypeAndId(elements, pageContext)}" var="theSenderName" />
        </c:otherwise>
    </c:choose>

    /******************** 已签收 ********************/
    if(modelType == "received"){
        if("2"=='${isBeRegistered}'){
            //已登记，不显示选人
            document.getElementById("pDiv").className = "hidden";
        }else{
            //待登记打开公文，不显示选人
            var from = '${param.from}';
            if("tobook"==from){
                document.getElementById("pDiv").className = "hidden";
            }
            else{
                document.getElementById("pDiv").className = "";
            }
        }
        document.getElementById("toReceive_keepperiod").innerHTML= "";
        document.getElementById("received_keepperiod").className = "";
        document.getElementById("received_oper").className = "";
        document.getElementById("toReceive_oper").className = "hidden";
        document.getElementById("received_recNo").className = "";
        document.getElementById("received_recNo").innerHTML = "${v3x:toHTML(bean.recNo)}";
        document.getElementById("toReceive_recNo").className = "hidden";
        document.getElementById("toReceive_registerUserId").className = "hidden";
        document.getElementById("received_registerUserId").className = "";
        document.getElementById("received_registerUserId").innerText = '${v3x:escapeJavascript(registerName)}';
        document.getElementById("received_remark").className = "";
        document.getElementById("toReceive_remark").className = "hidden";
        document.getElementById("received_remark").innerHTML = "${v3x:toHTML(bean.remark)}";
        document.getElementById("sendTime").innerHTML = "${v3x:formatDateByPattern(bean.createTime,'yyyy年MM月dd日 HH:mm')}";
        document.getElementById("issueDate").innerHTML = "${v3x:formatDateByPattern(bean.issueDate,datePattern)}";
        document.getElementById("sendUnit").innerHTML = "${v3x:toHTML(summary.sendUnit)}";
        document.getElementById("issuer").innerHTML = "${v3x:toHTML(bean.issuer)}";
        document.getElementById("sendTo").innerHTML = "${v3x:toHTML(sendEntityName)}";
        document.getElementById("recOrg").innerHTML = "${v3x:toHTML(signedName)}";
        document.getElementById("recTime").innerHTML = "${v3x:formatDateByPattern(bean.recTime,'yyyy年MM月dd日 HH:mm')}";
        document.getElementById("recUser").innerHTML = "${v3x:toHTML(recUser)}";
        document.getElementById("sender").innerHTML = "${v3x:toHTML(bean.sender)}";
        document.getElementById("stepBackDiv").style.display = "none";
    }
    /******************** 签收/退件 ********************/
    else if(modelType=="toReceive" || modelType=="retreat"){
        document.getElementById("pDiv").className = "";
        document.getElementById("toReceive_keepperiod").className = "";
        document.getElementById("received_keepperiod").innerHTML= "";
        document.getElementById("toReceive_oper").className = "";
        document.getElementById("received_oper").className = "hidden";
        document.getElementById("toReceive_recNo").className = "";
        document.getElementById("received_recNo").className = "hidden";
        document.getElementById("received_registerUserId").className = "hidden";
        document.getElementById("toReceive_registerUserId").className = "";
        document.getElementById("toReceive_remark").className = "";
        document.getElementById("received_remark").className = "hidden";
        document.getElementById("issuer").innerHTML = "${v3x:toHTML(bean.issuer)}";
        document.getElementById("sendTime").innerHTML = "${v3x:formatDateByPattern(bean.createTime,'yyyy年MM月dd日 HH:mm')}";
        document.getElementById("issueDate").innerHTML = "${v3x:formatDateByPattern(bean.issueDate,datePattern)}";
        <%--document.getElementById("sendUnit").innerHTML = "${v3x:toHTML(summary.sendUnit)}";--%>

        document.getElementById("sendUnit").innerHTML = "${v3x:toHTML(summary.sendDepartment)}";
        document.getElementById("issuer").innerHTML = "${v3x:toHTML(bean.issuer)}";
        document.getElementById("recOrg").innerHTML = "${v3x:toHTML(signedName)}";
        document.getElementById("sendTo").innerHTML = "${v3x:toHTML(sendEntityName)}";
        document.getElementById("sender").innerHTML = "${v3x:toHTML(bean.sender)}";
        document.getElementById("recUser").innerHTML = "${v3x:toHTML(bean.recUser)}";
        document.getElementById("recTime").innerHTML = "${v3x:formatDateByPattern(bean.recTime,'yyyy年MM月dd日 HH:mm')}";
        //状态等于4代表已退回的待签收数据
        if('${bean.status}'==4){
            document.getElementById("stepBackDiv").style.display = "";
        }
    }
}
function openSelectPeopleDlg(){
    var tempModelTemp = '${modelType}';
    if (tempModelTemp == "received") {
        //加入Ajax检查，判断是否已经被登记
        var isCanBeRegisted = "true";
		var message,flag;
		if("${isG6Ver}" != "true" || "${isAutoRegister}" == "false"){
			flag = "register";
		}else{
			flag = "distribute";
		}
        try {
			if(flag == "register"){
				var requestCaller = new XMLHttpRequestCaller(this, "edocExchangeManager", "isBeRegistered", false);
	            requestCaller.addParameter(1,'String','${edocRecieveRecordID4ChgRegUser}');
	            isCanBeRegisted = requestCaller.serviceRequest();
	            message = v3x.getMessage('ExchangeLang.exchange_register_change');
	        }else{
	        	//需要加入是否已经被分发的ajax判断
				message = v3x.getMessage('ExchangeLang.exchange_register_distribute');
				var requestCaller = new XMLHttpRequestCaller(this, "edocExchangeManager", "isBeDistributed", false);
	            requestCaller.addParameter(1,'String','${edoeRegisterId4ChgDrcUser}');
	            isCanBeRegisted = requestCaller.serviceRequest();
		    }
        }
        catch (ex1) {
            alert("Exception : " + ex1);
            return false;
        }
        if(isCanBeRegisted=="true"){
            if (window.confirm(message)) {
                selectPeopleFun_grantedDepartId();
            }
        }else{
            if(flag == "register"){
            	alert(v3x.getMessage('ExchangeLang.exchange_alert_has_registe'));
            }else{
            	alert(v3x.getMessage('ExchangeLang.exchange_alert_has_distribute'));
            }
            parent.location.href = parent.location.href;
        }
    }
    else{
        selectPeopleFun_grantedDepartId();
    }
}
</script>
</head>

<body>

<v3x:selectPeople id="grantedDepartId" panels="Department" selectType="Member" minSize="1" maxSize="1" jsFunction="setPeopleFields(elements)" originalElements="Member|${registerId}" />

<form name="detailForm" id="detailForm" action="exchangeEdoc.do?method=${operType}&modelType=${modelType}&fromlist=${param.fromlist}" method="post">

<script>onlyLoginAccount_grantedDepartId=true;</script>
<script>showRecent_grantedDepartId=false;</script>
<input type="hidden" id="oneself" name="oneself" value="true" />
<input type="hidden" id="modelType" name="modelType" value="${modelType}">
<input type="hidden" id="id" name="id" value="${bean.id}">
<input type="hidden" id="affairId" name="affairId" value="${affairId}">
<input type="hidden" id="method" name="method" value="${operType}">
<input type="hidden" id="stepBackInfo" name="stepBackInfo" value="">
<input type="hidden" id="accountId" name="accountId" value="${accountId}">
<input type="hidden" id="depId" name="depId" value="${depId}">

<%-- 实际交换的单位ID，  代理情况下有用 --%>
<input type="hidden" name="exchangeAccountId" id="exchangeAccountId" value="${exchangeAccountId}" >

<div align="center" id="printDiv" name="printDiv">
    <div id="div1" name="div1" align="center">
        <table align="center" class="xdLayout" style="width:940px;BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word" borderColor="buttontext" border="1">
            <colgroup>
                <col style="WIDTH: 110px"></col>
                <col style="WIDTH: 135px"></col>
                <col style="WIDTH: 90px"></col>
                <col style="WIDTH: 135px"></col>
                <col style="WIDTH: 110px"></col>
                <col style="WIDTH: 360px"></col>
            </colgroup>
            <tbody vAlign="top">
                <tr>
                    <%--------------公文签收单----------------%>
                    <td colSpan="6" style="HEIGHT: 35px;PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none">
                        <div align="center">
                            <font face="宋体" color="#ff0000" size="4"><strong><fmt:message key="exchange.edoc.receive" /></strong></font></div>
                            <c:if test="${allowShowEdocInRec eq true &&  contentType!='gd'}">
                            <div align="right" onclick="openEdoc()"><font face="宋体" color="#ff0000" size="2" style="cursor:hand;"><fmt:message key="exchange.edoc.preview" /></font></div>
                            </c:if>
                            <div id="stepBackDiv" name="stepBackDiv" align="right" style="display:none;"><a href="javascript:openStepBackInfo(1)"><fmt:message key="exchange.edoc.yituihui" /></div>
                    </td>
                </tr>

                <tr>
                    <%--------------标题----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 20px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 19px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 32px;line-height: 32px;width: 70px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.title" /></font>
                        </div>
                    </td>
                    <td colSpan="5" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="subject" name="subject">${v3x:toHTML(bean.subject)}
                        </div>
                    </td>
                </tr>

                <tr>
                    <%--------------发文单位----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 32px;line-height: 32px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.sendaccount" /></font>
                        </div>
                    </td>
                    <td colSpan="3" style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="sendUnit" name="sendUnit">

                        </div>
                    </td>
                    <%-- 公文级别 --%>
						<td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 6px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
							<div style="text-align:right;">
								<font face="宋体" color="#ff0000" size="4"><fmt:message key="edoc.unitLevel.label" /> </font>
							</div>
						</td>
						<td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
							<div id="unitLevel" name="unitLevel">
								<v3x:metadataItemLabel metadata="${colMetadata['edoc_unit_level']}" value="${summary.unitLevel}"/>
							</div>
						</td>
                </tr>

<%--                 <tr>--%>
<%--                    &lt;%&ndash;------------送往单位--------------&ndash;%&gt;--%>
<%--                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">--%>
<%--                        <div style="text-align:right;HEIGHT: 32px;line-height: 32px;">--%>
<%--                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.sendToNames" /></font>--%>
<%--                        </div>--%>
<%--                    </td>--%>
<%--                    <td colSpan="5" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">--%>
<%--                        <div id="sendTo" name="sendTo">--%>
<%--                            --%>
<%--                        </div>--%>
<%--                        <input type="hidden" id="grantedDepartId" name="grantedDepartId" value="${grantedDepartId}" />--%>
<%--                    </td>--%>
<%--                </tr>--%>

                 <tr>
<%--                     zhou--%>
                 	 <%--------------送文人----------------%>
<%--                    <td style="border-width: 1pt 1pt 1pt medium; border-style: solid solid solid none; border-color: rgb(255, 0, 0) rgb(255, 0, 0) rgb(255, 0, 0) currentColor; padding-left: 12px; vertical-align: middle;padding-right:10px;">--%>
<%--                        <div style="text-align:right;HEIGHT: 32px;line-height: 32px;">--%>
<%--                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.sendperson" /></font>--%>
<%--                        </div>--%>
<%--                    </td>--%>
<%--                    <td colSpan="3" style="BORDER-RIGHT: none;PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">--%>
<%--                        <div id="sender" name="sender">--%>
<%--                            --%>
<%--                        </div>--%>
<%--                    </td>--%>

                   <%-- 送文日期 --%>
					<td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT:8px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
						<div style="text-align:right;">
							<font face="宋体" color="#ff0000" size="4"><fmt:message key="edoc.sendTime.label" /></font>
						</div>
					</td>
					<td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
						<div id="sendTime" name="sendTime">
						</div>
					</td>

                 </tr>



                <tr>
                	  <%--------------公文文号----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;height: 35px;line-height: 35px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.wordNo" /></font>
                        </div>
                    </td>
                    <td colSpan="3" style="BORDER-RIGHT: none;PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="docMark" name="docMark">${v3x:toHTML(bean.docMark)}
                        </div>
                    </td>

                    <%--------------公文文种----------------%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 8px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="edoc.element.doctype" bundle="${edocI18N}"/></font>
                        </div>
                    </td>
                    <td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="docType" name="docType">
                            <v3x:metadataItemLabel metadata="${colMetadata['edoc_doc_type']}" value="${bean.docType}" />
                        </div>
                    </td>
                </tr>
                       <%--------------隐藏签发人签发日期_zxy----------------%>
                  <tr hidden>

                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 10px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 12px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 32px;line-height: 32px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.signingperson" /></font>
                        </div>
                    </td>
                    <td colSpan="3" style="BORDER-RIGHT: none;PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="issuer" name="issuer">

                        </div>
                    </td>

                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 8px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.signingdate" /></font>
                        </div>
                    </td>
                    <td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="issueDate" name="issueDate">

                        </div>
                    </td>
                </tr>

                 <tr>
                    <%--------------密级----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 20px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 19px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 37px;width: 75px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.secretlevel" /></font>
                        </div>
                    </td>
                    <td  style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="secretLevel" name="secretLevel">
                            <v3x:metadataItemLabel metadata="${colMetadata['edoc_secret_level']}" value="${bean.secretLevel}" />
                        </div>
                    </td>
                    <%--------------紧急程度----------------%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 7px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div>
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.urgentlevel" /></font>
                        </div>
                    </td>
                    <td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="urgentLevel" name="urgentLevel">
                            <v3x:metadataItemLabel metadata="${colMetadata['edoc_urgent_level']}" value="${bean.urgentLevel}" />
                        </div>
                    </td>

                     <%--------------份数----------------%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 25px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 19px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;width: 55px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.copy" /></font>
                        </div>
                    </td>
                    <td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="copies" name="copies">
                            ${copies}
                        </div>
                    </td>
                </tr>

                <tr>
                 <%--------------签收单位----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 37px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.receiveDepart" /></font>
                        </div>
                    </td>
                    <td colSpan="5" style="border-style: solid none solid solid; padding: 1px; vertical-align: middle; border-top-color: rgb(255, 0, 0); border-bottom-color: rgb(255, 0, 0); border-left-color: rgb(255, 0, 0); border-top-width: 1pt; border-bottom-width: 1pt; border-left-width: 1pt;">
                        <div id="recOrg" name="recOrg">

                        </div>
                    </td>
                </tr>
                <tr>
                 <%--------------签收人----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 10px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 12px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 37px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.receivedperson" /></font>
                        </div>
                    </td>
                    <td colSpan="3" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div name="recUser" id="recUser">

                        </div>
                        <input type="hidden" name="recUserId" id="recUserId" value="${recUserId}">
                    </td>
                   <%--------------签收日期----------------%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 8px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.receiveddate" /></font>
                        </div>
                    </td>
                    <td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="recTime" name="recTime">

                        </div>
                    </td>

                </tr>

                <tr>
                    <%--------------签收编号----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 37px;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.signingNo" /></font>
                        </div>
                    </td>
                    <td colSpan="3" style="BORDER-RIGHT: none;PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="toReceive_recNo" name="toReceive_recNo" class="hidden" style="width:100%;">
                        <%-- GOV-4858 公文管理，收文签收，签收编号过长，点击签收出现红三角......然后点红三角的返回又报脚本错误...... --%>
                        <!-- G6 V1.0 SP1后续功能_自定义签收编号start -->
                        <select name="recNo" id="recNo" style="width:100%;"> <!-- onChange="showNextCondition(this)" class="condition" -->>
                        <c:choose>
                        <c:when test="${fn:length(def)==0 && modelType != 'received'}">
                        <option value=""><fmt:message key="edoc.editorial.empty.label" /></option>
                        </c:when>
                        <c:when test="${fn:length(def)>1}">
                        <option value="" selected="true"><fmt:message key="edoc.editorial.empty.label" /></option>
                        <c:forEach var="d" items="${def}" varStatus="i">
                        <option value="${d.selectV}">${d.wordNo}</option>
                        </c:forEach>
                        </c:when>
                        <c:otherwise>
                        <c:forEach var="d" items="${def}" varStatus="i">
                        <option value=""><fmt:message key="edoc.editorial.empty.label" /></option>
                        <option value="${d.selectV}" selected="true">${d.wordNo}</option>
                        </c:forEach>
                        </c:otherwise>
                        </c:choose>
                        </select>
                        <img src="<%=SystemEnvironment.getContextPath()%>/apps_res/edoc/images/wordnochange.gif" onclick="editorialChange();">
                        <!-- G6 V1.0 SP1后续功能_自定义签收编号end -->
                        </div>
                        <div id="received_recNo" name="received_recNo" class="hidden">

                        </div>
                    </td>
                    <%--------------保存期限----------------%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 8px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.keepperiod" /></font>
                        </div>
                    </td>
                    <td colSpan="1" style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">

                        <div id="toReceive_keepperiod" name="toReceive_keepperiod" class="hidden">
                            <select name="keepperiod" id="keepperiod" style="width:100%;">
                                <v3x:metadataItem metadata="${exchangeEdocKeepperiodMetadata}" showType="option" name="keepperiod" />
                            </select>
                        </div>

                        <div id="received_keepperiod" name="received_keepperiod" class="hidden">
                            <v3x:metadataItemLabel metadata="${exchangeEdocKeepperiodMetadata}" value="${bean.keepPeriod}" />
                        </div>
                    </td>
                </tr>

                <tr>
                    <%--------------登记人----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 10px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 12px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align:right;HEIGHT: 37px;">
                            <font face="宋体" color="#ff0000" size="4">
                            <c:choose>
							<c:when test="${isG6Ver == 'true'}">
								<c:choose>
								<c:when test="${isAutoRegister == 'true'}">
									<fmt:message key="edoc.element.receive.distributer" bundle="${edocI18N }"/>
								</c:when>
								<c:otherwise>
									<fmt:message key="exchange.edoc.booker" />
								</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<fmt:message key="exchange.edoc.booker" />
							</c:otherwise>
							</c:choose>
                            </font>
                        </div>
                    </td>
                    <td colspan="5"  style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="toReceive_registerUserId" name="toReceive_registerUserId" class="hidden" style="width:100%;float:left;">
                        <input onclick="" type="text" readOnly="readOnly" name="memberId" id="memberId" value="${v3x:toHTML(registerName)}" style="width:100%;">
                        </div>
                        <div id="received_registerUserId" class="hidden" style="float: left;">

                        </div>
                        <div id="pDiv" name="pDiv" style="width:100%;"><font color="blue"><a href="###"  class="ico16 select_single_16" onclick="openSelectPeopleDlg();"></a></font>
                        <input type="hidden" id="registerUserId" name="registerUserId" value="${registerId}">
                        </div>
                    </td>
                </tr>

                <tr>
                    <%--------------纸质附件说明----------------%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="HEIGHT: 65px;text-align:center;vertical-align: middle;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.paperinformation" /></font>
                        </div>
                    </td>
                    <td colspan="5" rowspan="1" style="BORDER-RIGHT: #ff0000 0pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <!--<input type="text" name="remark" id="remark" style="width:100%;height:100%;" >
                    -->
                    <div name="toReceive_remark" id="toReceive_remark" class="hidden">
                        <textarea name="remark" id="remark" rows="4" style="width:100%;overflow: auto;" validate="maxLength" inputName="<fmt:message key="exchange.edoc.paperinformation" />"  maxSize="80"></textarea>
                    </div>
                    <div name="received_remark" id="received_remark" class="hidden">
                    </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div id="sendButton" name="sendButton" class="">
        <div id="toReceive_oper" name="toReceive_oper" <c:if test="${hasButton!='true' }">class="hidden"</c:if> width="10px;">

            <table border="0" width="100%">
                <tr>
                    <td height="42" align="center">
                        <input type="button" id="subBtn" value="<fmt:message key='exchange.edoc.qianshou' />" class="button-default_emphasize" onclick="oprateSubmit();">
                        <input type="button" id="subBtn3" value="<fmt:message key='exchange.edoc.tuihui' />" class="button-default-2" onclick="stepBack();">

                        <input type="button" id="subBtn4" value="<fmt:message key='common.toolbar.print.label' bundle='${v3xCommonI18N}' />" class="button-default-2" onclick="recPrint();">
                    </td>
                </tr>
            </table>
        </div>

        <div  id="received_oper" name="received_oper" class="hidden" style="padding-top:10px;">
            <table border="0" width="100%">
                <tr>
                    <td height="42" align="center" valign="top">
                        <input type="button" value="<fmt:message key='common.toolbar.print.label' bundle='${v3xCommonI18N}' />" class="button-default-2" onclick="recPrint();">
                    </td>
                </tr>
            </table>
        </div>
    </div>

</div>
</form>
<script>
//GOV-4437 【收文管理-签收】打开签收单，登记人，签收编号和附件说明怎么都不能输入了？
//页面元素加载完，才执行js初始化方法
if(typeof elements_grantedDepartId != "undefined")
	oldElements_grantedDepartId = elements_grantedDepartId;
initiate('${modelType}');
setTimeout(function() {
	showEdocMark(null, function() {
		if("${bean.recNo}" != "") {
			$("#recNo_autocomplete").val("${bean.recNo}");
		}
	});
},100);
</script>
