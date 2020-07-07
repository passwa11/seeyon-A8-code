<%--
  这个页面是内嵌页面
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="com.seeyon.ctp.common.constants.ApplicationCategoryEnum" %>
<%@ page import="com.seeyon.ctp.common.SystemEnvironment" %>
<%@ page isELIgnored="false" import="com.seeyon.ctp.common.flag.BrowserEnum" %>
<script type="text/javascript" src="${path}/ajax.do?managerName=edocExchangeManager"></script>
<script type="text/javascript" src="${path}/ajax.do?managerName=xkjtManager"></script>
<script type="text/javascript">
    $(function () {
        loadFileData();
        document.getElementById("sendUnit").setAttribute("value", '${summary.sendDepartment}');
        document.getElementById("sendUnitId").setAttribute("value", '${summary.sendDepartmentId}');
    });

    function loadFileData() {
        $.ajax({
            type: 'post',
            async: true,
            url: '/seeyon/ext/xkEdoc.do?method=sendFileList&summaryId=${summary.id}&isQuickSend=${summary.isQuickSend}',
            dataType: 'json',
            success: function (res) {
                var list = res.data;
                var mainlist = res.main;
                var tmp = '';
                var maintmp = '';
                if (list.length > 0) {
                    for (var i = 0; i < list.length; i++) {
                        tmp += '<tr style="height: 35px;border-top: 1px solid red">';
                        tmp += '<td width="80%" style="border-top:  #ff0000 1pt solid;">' +
                            '<a href="javascript:void(0);" onclick="downloadfilez(\'' + list[i].type + '\',\'1\',\'' + list[i].filepath + '\',\'' + list[i].createdate + '\',\'' + list[i].filename + '\',\'${summary.isQuickSend}\',\'${summary.id}\')">' + list[i].filename + '</a></td>';
                        // tmp += '<td width="auto" style="border-top:  #ff0000 1pt solid;">' + list[i].createdate + '</td>';
                        tmp += '</tr>';
                    }
                } else {
                    tmp += '<tr><td width="80%" style="border-top:  #ff0000 1pt solid;">无数据！</td></tr>';
                }


                if (mainlist.length > 0) {
                    for (var j = 0; j < mainlist.length; j++) {
                        maintmp += '<tr style="height: 35px;border-top: 1px solid red">';
                        maintmp += '<td width="80%" style="border-top:  #ff0000 1pt solid;">' +
                            '<a href="javascript:void(0);" onclick="downloadfilez(\'' + mainlist[j].type + '\',\'2\',\'' + mainlist[j].filepath + '\',\'' + mainlist[j].createdate + '\',\'' + mainlist[j].filename + '\',\'${summary.isQuickSend}\',\'${summary.id}\')">' + mainlist[j].filename + '</a></td>';
                        maintmp += '</tr>';
                    }
                } else {
                    maintmp += '<tr><td width="80%" style="border-top:  #ff0000 1pt solid;">无数据！</td></tr>';
                }
                $("#fileTable").html(tmp);

                $("#mainTable").html(maintmp);


            }, error: function (res) {
            }
        });
    }

    function downloadfilez(ztype, type, fileUrl, createdate, filename, isQuickSend, summaryId) {
        var time = new Date(parseInt(createdate));
        var year = time.getFullYear();
        var month = time.getMonth() + 1;
        if (9 >= month) {
            month = "0" + month;
        }

        var day = time.getDate();
        if (9 >= day) {
            day = "0" + day;
        }
        var uploadTime = year + "-" + month + "-" + day;

        //判断是否是关联文挡
        if (ztype == '2' || ztype == '4') {
            $.ajax({
                type: 'post',
                async: true,
                url: '/seeyon/ext/xkEdoc.do?method=toAnalyzeFileIsOpenOrUpload&summaryId='+(summaryId + '')+'&filename='+encodeURI(filename)+'&createDate='+uploadTime+'&fileId='+(fileUrl + ''),
                dataType: 'json',
                success: function (res) {
                    if (res.isExist == 'true') {
                        var url = "/seeyon/ext/xkEdoc.do?method=downloadfile&type=" + type + "&fileId=" + (fileUrl + '') + "&createDate=" + uploadTime + "&filename=" + encodeURI(filename)
                            + "&isQuickSend=" + isQuickSend + "&summaryId=" + (summaryId + '');
                        $("#downloadFileFrame").attr("src", url);
                    } else {
                        var url="/seeyon/edocController.do?method=detailIFrame&from=Done&affairId=${affairId}";
                        window.open(url,"_blank");
                    }
                }, error: function (res) {
                }
            });

        } else {
            var url = "/seeyon/ext/xkEdoc.do?method=downloadfile&type=" + type + "&fileId=" + (fileUrl + '') + "&createDate=" + uploadTime + "&filename=" + encodeURI(filename)
                + "&isQuickSend=" + isQuickSend + "&summaryId=" + (summaryId + '');
        }
        $("#downloadFileFrame").attr("src", url);
    }

    function openEdoc() {
        _url = edocURL + "?method=edocDetailInDoc&summaryId=${summary.id}&openFrom=lenPotent&lenPotent=100" + CsrfGuard.getUrlSurffix();
        //alert(_url)
        v3x.openWindow({
            url: _url,
            workSpace: 'yes',
            resizable: "false",
            dialogType: "open"
        });
    }

    //detailId -> 每一条交换回执记录的Id, sendRecordId -> 发送记录的Id
    var _sendRecordId = "";
    var _detailId = "";
    var _accountId = "";

    function withdraw(sendRecordId, detailId, accountId) {
        xkjtObj.xkjtCheckedArr = [];
        _sendRecordId = sendRecordId;
        _detailId = detailId;
        _accountId = accountId;
        //验证是否可撤销交换
        var requestCaller = new XMLHttpRequestCaller(this, "ajaxEdocExchangeManager", "canWithdraw", false);
        requestCaller.addParameter(1, "String", sendRecordId);
        requestCaller.addParameter(2, "String", detailId);
        var bool = requestCaller.serviceRequest();
        if (bool && bool == "false") {
            alert(v3x.getMessage('ExchangeLang.exchange_send_withdraw_forbidden'));
            document.location.reload();
            return false;
        }
        getA8Top().win123 = getA8Top().v3x.openDialog({
            title: "<fmt:message key='exchange.send.withdraw'/>",
            transParams: {'parentWin': window},
            url: 'exchangeEdoc.do?method=openEdocSendRecordCancelDialog&sendRecordId=' + sendRecordId + "&detailId=" + detailId,
            width: "400",
            height: "300",
            resizable: "0",
            scrollbars: "true",
            dialogType: "modal"
        });
    }

    /* 客开：徐矿集团【验证是否可撤销交换】 chenqiang 2019年3月18日 start */
    var xkjtObj = {
        xkjtCheckedArr: []
    };

    function withdrawAll() {
        xkjtObj.xkjtCheckedArr = [];
        var checkedIds = document.getElementsByName("xkjtId");

        for (var i = 0; i < checkedIds.length; i++) {
            //选择待签收  已签收  未阅  已阅数据
            if (checkedIds[i].checked && (checkedIds[i].getAttribute("status") != 2 || checkedIds[i].getAttribute("status") != 3 || checkedIds[i].getAttribute("status") == 13)) {
                var xkjtCheckedObj = {};
                xkjtCheckedObj.sendRecordId = checkedIds[i].getAttribute("sendRecordId");
                xkjtCheckedObj.detailId = checkedIds[i].getAttribute("detailId");
                xkjtCheckedObj.accountId = checkedIds[i].getAttribute("accountId");
                xkjtCheckedObj.leaderId = checkedIds[i].getAttribute("leaderId");
                xkjtCheckedObj.daiyueId = checkedIds[i].getAttribute("daiyueId");
                xkjtObj.xkjtCheckedArr.push(xkjtCheckedObj);
            }
        }
        ;
        debugger;
        if (xkjtObj.xkjtCheckedArr.length == 0) {
            alert("请选择交换记录！")
        } else {
            getA8Top().win123 = getA8Top().v3x.openDialog({
                title: "<fmt:message key='exchange.send.withdraw'/>",
                transParams: {'parentWin': window},
                url: 'exchangeEdoc.do?method=openEdocSendRecordCancelDialog&sendRecordId=' + xkjtObj.xkjtCheckedArr[0].sendRecordId + "&detailId=" + xkjtObj.xkjtCheckedArr[0].detailId,
                width: "400",
                height: "300",
                resizable: "0",
                scrollbars: "true",
                dialogType: "modal"
            });
        }


    }

    function withdrawCallback(returnValues) {
        if (returnValues != null && returnValues != undefined) {
            //确认是否撤销交换记录
            if (!window.confirm(v3x.getMessage('ExchangeLang.exchange_send_withdraw'))) {
                return;
            }
            if (xkjtObj.xkjtCheckedArr.length == 0) {
                //撤销交换记录
                var requestCaller = new XMLHttpRequestCaller(this, "ajaxEdocExchangeManager", "withdraw", false);
                requestCaller.addParameter(1, "String", _sendRecordId);
                requestCaller.addParameter(2, "String", _detailId);
                requestCaller.addParameter(3, "String", _accountId);
                requestCaller.addParameter(4, "String", returnValues[0]);
                var back = requestCaller.serviceRequest();
                //撤销后刷新当前页面
                var alertNote = '';
                if (back == "3") {
                    alertNote = v3x.getMessage('edocLang.exchange_sendRecordDetail_cancel_send_already');//已撤销
                } else if (back == "2") {
                    alertNote = v3x.getMessage('edocLang.exchange_sendRecordDetail_stepback_already');//已退回
                } else if (back == "1") {
                    alertNote = v3x.getMessage('edocLang.exchange_sendRecordDetail_recieved_already');//已签收
                }
                if (alertNote != '') {
                    alert(alertNote);
                }
                location.reload();
            } else {
                //撤销交换记录
                var requestCaller = new XMLHttpRequestCaller(this, "ajaxEdocExchangeManager", "withdrawAll", false);
                requestCaller.addParameter(1, "String", JSON.stringify(xkjtObj));
                requestCaller.addParameter(2, "String", returnValues[0]);
                var back = requestCaller.serviceRequest();
                //撤销后刷新当前页面
                var alertNote = '';
                if (back == "3") {
                    alertNote = v3x.getMessage('edocLang.exchange_sendRecordDetail_cancel_send_already');//已撤销
                } else if (back == "2") {
                    alertNote = v3x.getMessage('edocLang.exchange_sendRecordDetail_stepback_already');//已退回
                } else if (back == "1") {
                    alertNote = v3x.getMessage('edocLang.exchange_sendRecordDetail_recieved_already');//已签收
                }
                if (alertNote != '') {
                    alert(alertNote);
                }
                location.reload();
                xkjtObj.xkjtCheckedArr = [];
            }


        }
    }

    /* 客开：徐矿集团【验证是否可撤销交换】 chenqiang 2019年3月18日 end */
    var allSelPerElements;
    var _lastSelectVal = "${v3x:showOrgEntitiesOfTypeAndId(elements, pageContext)}";

    function setPeopleFields(elements) {
        if (elements) {
            //var obj1 = getNamesString(elements);
            //var obj2 = getIdsString(elements,false);

            //document.getElementById("depart").value = getNamesString(elements);
            //document.getElementById("depart").setAttribute("value", getNamesString(elements));
            //setAttribute浏览器不兼容，在IE10下存在问题，不知为何将以前的方法调整成setAttribute，这里修改为用jquery方式来赋值
            //$("#depart").attr("value",getNamesString(elements));
            //document.getElementById("grantedDepartId").value = getIdsString(elements,true);

            var _sq = v3x.getMessage("V3XLang.common_separator_label");//分隔符
            var departObj = document.getElementById("depart");

            var srcVal = departObj.value;

            var sendUnit = getNamesString(elements);

            var newValue = _removeRepeat(srcVal, _lastSelectVal, _sq);
            newValue = _removeRepeat(newValue, sendUnit, _sq);

            if (sendUnit) {
                if (newValue == "") {
                    newValue = sendUnit;
                } else {
                    newValue += _sq + sendUnit;
                }
            }

            _lastSelectVal = sendUnit;

            //$("#depart").val(sendUnit);
            //OA-50892 公文收发员打开待发送公文单，添加送往单位时，送往单位选择框显示不出来刚选择的单位
            departObj.value = newValue;
            //OA-49069  在公文交换-待发送列表中填写了送往单位，点击打印，打印的时候显示不出来刚填写的送往单位
            departObj.setAttribute("value", newValue);
            $("#grantedDepartId").val(getIdsString(elements, true));
            allSelPerElements = elements;
        }
    }

    //周刘成
    function setPeopleFieldsZ(elements) {
        document.getElementById("sendUnit").setAttribute("value", '');
        document.getElementById("sendUnitId").setAttribute("value", '');
        if (elements) {

            var _sq = v3x.getMessage("V3XLang.common_separator_label");//分隔符
            var departObj = document.getElementById("sendUnit");

            var srcVal = departObj.value;

            var sendUnit = getNamesString(elements);

            var newValue = _removeRepeat(srcVal, _lastSelectVal, _sq);
            newValue = _removeRepeat(newValue, sendUnit, _sq);

            if (sendUnit) {
                if (newValue == "") {
                    newValue = sendUnit;
                } else {
                    newValue += _sq + sendUnit;
                }
            }

            _lastSelectVal = sendUnit;

            //$("#depart").val(sendUnit);
            //OA-50892 公文收发员打开待发送公文单，添加送往单位时，送往单位选择框显示不出来刚选择的单位
            departObj.value = newValue;
            //OA-49069  在公文交换-待发送列表中填写了送往单位，点击打印，打印的时候显示不出来刚填写的送往单位
            departObj.setAttribute("value", newValue);
            $("#sendUnitId").val(getIdsString(elements, true));
            allSelPerElements = elements;
        }
    }

    function _removeRepeat(src, toCon, sq) {

        var ret = "";
        if (src && toCon) {
            var srcArray = this.splitValue(src);
            var toConArray = toCon.split(sq);
            for (var i = 0; i < srcArray.length; i++) {
                var temp = srcArray[i];
                if ("" == temp) {
                    continue;
                }
                var toRemove = false;
                for (var j = 0; j < toConArray.length; j++) {
                    if (temp == toConArray[j]) {
                        toRemove = true;
                        break;
                    }
                }
                if (!toRemove) {
                    if (ret != "") {
                        ret += sq;
                    }
                    ret += temp;
                }
            }
        } else {
            ret = src;
        }

        return ret;
    }

    //分隔符定义
    var separatorLabel = ["、", ",", "，"];

    function splitValue(value) {
        if (value == null) {
            return;
        }
        var splitArray = new Array();
        for (var i = 0; i < separatorLabel.length; i++) {
            var sq = separatorLabel[i];
            if (splitArray.length == 0) {
                splitArray = value.split(sq);
            } else {
                var tempArray = new Array();
                for (var j = 0; j < splitArray.length; j++) {
                    tempArray = tempArray.concat(splitArray[j].split(sq));
                }
                splitArray = tempArray;
            }
        }
        return splitArray;
    }

    //手动输入主送单位时进行ID检验
    function _checkInputIds(obj) {
        var currentAccountId = document.getElementById("orgAccountId").value;
        var inputIdObj = document.getElementById("grantedDepartId");
        if (inputIdObj) {

            var objVal = obj.value;
            obj.setAttribute("value", objVal);//打印用
            if (typeof (allSelPerElements) == 'undefined') {
                allSelPerElements = elements_grantedDepartId;
            }
            var objEles = allSelPerElements;
            var objValus = this.splitValue(objVal);

            if (objEles) {

                var newIds = "";
                var newEles = [];
                var newLastSel = "";

                for (var i = 0; i < objEles.length; i++) {

                    var tempName = objEles[i].name;
                    var departmentType = objEles[i].type;
                    var elementAccountId = objEles[i].accountId;
                    if (currentAccountId != elementAccountId && objEles[i].accountShortname != null && objEles[i].accountShortname != "") {
                        tempName += "(" + objEles[i].accountShortname + ")";
                    }

                    var toAdd = false;
                    for (var j = 0; j < objValus.length; j++) {
                        if (departmentType == "Department") {
                            if (objValus[j].startsWith(tempName)) {
                                toAdd = true;
                                if (newLastSel != "") {
                                    newLastSel += ",";
                                }
                                newLastSel += tempName;
                                break;
                            }
                        } else {
                            if (tempName == objValus[j]) {
                                toAdd = true;
                                if (newLastSel != "") {
                                    newLastSel += ",";
                                }
                                newLastSel += tempName;
                                break;
                            }
                        }
                    }
                    if (toAdd) {
                        if (newIds != "") {
                            newIds += ",";
                        }
                        newIds += objEles[i].type + "|" + objEles[i].id;
                        newEles[newEles.length] = objEles[i];
                    }
                }
                _lastSelectVal = newLastSel;
                inputIdObj.value = newIds;
                elements_grantedDepartId = newEles;
            }
        }
    }

    function initiate(modelType) {
        if (modelType == "toSend") {
            var status = '${bean.status}';
            if (status == "2") {
                document.getElementById("sent").className = "";
            } else {
                document.getElementById("sent").className = "hidden";
            }
            document.getElementById("sendButton").className = "";
        } else if (modelType == "sent") {
            var exchangeMode = "${bean.exchangeMode}";
            if (exchangeMode != 1) {
                document.getElementById("sent").className = "";
                document.getElementById("sendButton").className = "hidden";
            }
            document.getElementById("sendButton2").className = "";
            var depName = document.getElementById("depart");
        }
    }

    var isNeedCheckLevelScope_grantedDepartId = false;
    var showAccountShortname_grantedDepartId = "auto";

    var isAllowContainsChildDept_grantedDepartId = true;
    var isCheckInclusionRelations_grantedDepartId = false;
    var isCanSelectGroupAccount_grantedDepartId = false;

    function openStepBackInfo(readOnly, accountId) {
        var exchangeSendEdocId = '${bean.id}';

        //这个回调为huituiCallback
        getA8Top().win123 = getA8Top().v3x.openDialog({
            title: "<fmt:message key='exchange.stepBack'/>",
            transParams: {'parentWin': window},
            url: 'exchangeEdoc.do?method=openStepBackDlg&exchangeSendEdocId=' + exchangeSendEdocId + '&readOnly=1&accountId=' + accountId,
            width: "400",
            height: "300"
        });
    }

    /**
     * 点击回退信息显示，防止JS报错
     */
    function huituiCallback(returnValues) {
        //暂时没有处理
    }

    function openSendCancelInfo(readOnly, accountId) {
        //这个回调为withdrawCallback
        getA8Top().win123 = getA8Top().v3x.openDialog({
            title: "<fmt:message key='exchange.send.withdraw'/>",
            transParams: {'parentWin': window},
            url: 'exchangeEdoc.do?method=openEdocSendRecordCancelDialog&sendRecordId=${bean.id}&readOnly=1&accountId=' + accountId,
            width: "400",
            height: "300"
        });
    }


    var tempDetailId = "";

    function openCuiban(detailId) {

        //这个回调为withdrawCallback
        getA8Top().win123 = getA8Top().v3x.openDialog({
            title: "<fmt:message key='hasten.label' bundle='${edocI18N}'/>",
            transParams: {'parentWin': window},
            url: 'exchangeEdoc.do?method=openCuiban&detailId=' + detailId,
            width: "400",
            height: "300"
        });
        tempDetailId = detailId;
    }

    /**
     * 催办回调函数
     */
    function openCuibanCallback(rv) {
        if (rv) {
            if (1 == rv[0]) {
                var formObj = document.getElementById("detailForm");
                formObj.target = "";

                var requestCaller = new XMLHttpRequestCaller(this, "edocExchangeManager", "cuiban", false);
                requestCaller.addParameter(1, 'String', tempDetailId);
                requestCaller.addParameter(2, 'String', rv[1]);
                var ret = requestCaller.serviceRequest();
                if (ret == "ok") {
                    alert(edocLang.edoc_supervise_sendMessage_success);
                } else {
                    alert(edocLang.edoc_supervise_sendMessage_failure);
                }
                location.reload();
            }
        }
    }

    function sendPrint1() {


        //table组件高度设置
        var $gBodyEl = $("#bDivsendDetail");
        var gTempHeight = $gBodyEl.height();

        $("#scrollListDiv").css("height", "");
        $gBodyEl.css("height", "");

        var edocBody = document.getElementById("printDiv").innerHTML;

        $("#scrollListDiv").css("height", "100%");
        $gBodyEl.height(gTempHeight);

        var edocBodyFrag = new PrintFragment("交换单", edocBody);

        var cssList = new ArrayList();
        cssList.add(v3x.baseURL + "/apps_res/exchange/css/exchange.css");
        cssList.add("/seeyon/common/skin/dist/common/skin.css");
        var pl = new ArrayList();
        pl.add(edocBodyFrag);
        printList(pl, cssList);
    }


</script>
<style type="text/css">
    <%--OA-25168 在公文交换---待交换中主送的单位--外部单位手动输入特殊字符，已发送中查看，"链接"是蓝色字体--%>
    #exchangeOrg a {
        color: #000;
    }

    #mainDiv td {
        text-align: left;
    }

    <%--OA-101029IE8，公文送文单送往单位处显示了横向滚动条 --%>
    #depart {
    <%
        String browserString=BrowserEnum.valueOf1(request);
        if (browserString.indexOf("IE") > -1) {
    %> width: 99%;
    <%
        } else {
    %> width: 100%;
    <%
        }
    %>
    }
</style>

<c:set value="${v3x:join(markDef.edocMarkAcls, 'orgDepartment.name', pageContext)}" var="depart"/>
<iframe id="downloadFileFrame" src="" class="" style="display: none"></iframe>
<form name="detailForm" id="detailForm" action="${exchange}?method=${operType}&modelType=${modelType}&reSend=${param.reSend}&fromlist=${param.fromlist}" method="post" target="edocDetailIframe">
    <c:set value="${v3x:parseElementsOfTypeAndId(elements)}" var="grantedDepartId"/>
    <!-- 项目：徐矿集团  【发文送文单中的选人界面增加选择人员】 作者：jiangchenxi 时间：2019年5月27日 start -->
    <!-- best 主送和抄送支持选择组 start -->
    <v3x:selectPeople id="grantedDepartId" panels="Account,Department,ExchangeAccount,OrgTeam,Team" selectType="Account,Department,ExchangeAccount,OrgTeam,Member,Team" jsFunction="setPeopleFields(elements)" originalElements="${grantedDepartId}"
                      viewPage="" minSize="0" departmentId="${sessionScope['com.seeyon.current_user'].departmentId}" showAllAccount="true"/>
    <%--	周刘成	--%>
    <v3x:selectPeople id="sendUnitId" panels="Account,Department,ExchangeAccount" selectType="Account,Department,ExchangeAccount,Member" jsFunction="setPeopleFieldsZ(elements)" originalElements="${grantedDepartId}" viewPage="" minSize="0"
                      departmentId="${sessionScope['com.seeyon.current_user'].departmentId}" showAllAccount="true"/>
    <!-- best 主送和抄送支持选择组 start -->
    <!-- 项目：徐矿集团  【发文送文单中的选人界面增加选择人员】 作者：jiangchenxi 时间：2019年5月27日 end -->
    <input type="hidden" name="appName" value="<%=ApplicationCategoryEnum.edoc.getKey()%>">
    <input type="hidden" id="orgAccountId" name="orgAccountId" value="${summary.orgAccountId}">
    <input type="hidden" id="modelType" name="modelType" value="${modelType}">
    <input type="hidden" id="id" name="id" value="${bean.id}">
    <input type="hidden" id="affairId" name="affairId" value="${affairId}">
    <input type="hidden" id="summaryId" name="summaryId" value="${summary.id}">
    <div align="center" id="printDiv" name="printDiv">
        <div id="mainDiv" name="mainDiv" width="100%">
            <table class="xdLayout" align="center" style="BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 60%; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word"
                   borderColor="buttontext" border="1">
                <colgroup>
                    <col style="WIDTH: 90px"></col>
                    <col style="WIDTH: 180px"></col>
                    <col style="WIDTH: 105px"></col>
                    <col style="WIDTH: 90px"></col>
                    <col style="WIDTH: 90px"></col>
                </colgroup>
                <tbody vAlign="top">
                <tr>
                    <td colSpan="5"
                        style="HEIGHT: 40px;PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none; BORDER-LEFT-STYLE: none"
                        nowrap="nowrap">
                        <div style="height: 20px;line-height: 20px;" align="center">
                            <font face="宋体" color="#ff0000" size="4">
                                <strong>
                                    <c:if test="${bean.isTurnRec == 0 }">
                                        <fmt:message key="exchange.edoc.sendform"/>
                                    </c:if>
                                    <c:if test="${bean.isTurnRec == 1 }">
                                        收文送文单
                                    </c:if>
                                </strong></font></div>
                        <c:if test="${allowShowEdocInSend eq true}">
                            <div align="right" onclick="openEdoc();"><font face="宋体" color="#ff0000" size="2" style="cursor:hand;"><fmt:message key="exchange.edoc.preview"/></font></div>
                        </c:if>
                    </td>
                </tr>

                <tr>
                    <%-- 标题--%>
                    <td style="HEIGHT: 32px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 20px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div style="text-align: right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.title"/></font>
                        </div>
                    </td>
                    <td colSpan="4"
                        style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="subject" name="subject" style="overflow: auto;">
                            ${v3x:toHTML(bean.subject)}</div>
                    </td>
                </tr>

                <tr>
                    <%-- 送往单位--%>
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.sendToNames"/></font>
                        </div>
                    </td>
                    <td colSpan="4"
                        style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="exchangeOrg" name="exchangeOrg" style="overflow: auto;">
                            <c:if test="${modelType=='toSend'}">
                                <input type="text" inputName="<fmt:message key="exchange.edoc.sendToNames" />" validate="notNull" id="depart" onkeyup="_checkInputIds(this)" name="depart"
                                       value="<%--${sendEntityName} --%>${sendEntityName!=null?fn:escapeXml(sendEntityName):(v3x:showOrgEntitiesOfTypeAndId(elements, pageContext))}">
                                <img id="grantedDepartIdImg" src="<%=SystemEnvironment.getContextPath()%>/apps_res/edoc/images/wordnochange.gif" onclick="selectPeopleFun_grantedDepartId()" title="点击选择"/>
                                <input type="hidden" id="grantedDepartId" name="grantedDepartId" value="${elements}">
                            </c:if>
                            <c:if test="${modelType!='toSend'}">
                                <%-- ${sendEntityName!=null?sendEntityName:(v3x:showOrgEntitiesOfTypeAndId(elements, pageContext))}--%>
                                ${v3x:toHTML(sendEntityName)}
                                <input type="hidden" id="grantedDepartId" name="grantedDepartId" value="${elements}">
                            </c:if>
                        </div>
                    </td>
                </tr>

                <tr>
                    <%-- 送文人  --%>
                    <td style="HEIGHT: 35px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 10px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <%--								<font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.sendperson" /></font>--%>
                            <font face="宋体" color="#ff0000" size="4">发文人</font>
                        </div>
                    </td>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div>
                            ${bean.sendUserNames}
                        </div>
                        <input type="hidden" name="sendUserId" id="sendUserId" value="${bean.sendUserId}">
                        <input type="hidden" name="sender" id="sender" value="${bean.sendUserNames}">
                    </td>
                    <%-- 送文日期 --%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 30px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" style="white-space:pre-line;" color="#ff0000" size="4"><fmt:message key="edoc.sendTime.label"/></font>
                        </div>
                    </td>
                    <td colSpan="2"
                        style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="sendTime" name="sendTime">
                            ${v3x:formatDateByPattern(bean.sendTime,'yyyy年MM月dd日 HH:mm')}
                        </div>
                    </td>
                </tr>

                <tr>
                    <%--  发文单位  --%>
                    <td style="HEIGHT: 35px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 10px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.sendaccount"/></font>
                        </div>
                    </td>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <%--							周刘成--%>
                        <%--							<div id="sendUnit" name="sendUnit">${bean.sendUnit}--%>
                        <%--								<c:if test="${modelType=='toSend'}">--%>
                        <input type="text" style="width: 350px;" inputName="<fmt:message key="exchange.edoc.sendToNames" />" validate="notNull" id="sendUnit" name="sendUnit" value="<%--${sendEntityName} --%>" readonly>
                        <%--									<img id="grantedDepartIdImg" src="<%=SystemEnvironment.getContextPath()%>/apps_res/edoc/images/wordnochange.gif" onclick="selectPeopleFun_sendUnitId()" title="点击选择"/>--%>
                        <%--									<input type="hidden" id="sendUnitId" name="sendUnitId" value="${elements}">--%>
                        <%--								</c:if>--%>
                        <%--								<c:if test="${modelType!='toSend'}">--%>
                        <%-- ${sendEntityName!=null?sendEntityName:(v3x:showOrgEntitiesOfTypeAndId(elements, pageContext))}--%>
                        <%--									${v3x:toHTML(sendEntityName)}--%>
                        <%--									<input type="hidden" id="sendUnitId" name="sendUnitId" value="${elements}">--%>
                        <%--								</c:if>--%>
                        <%--							</div>--%>
                        <%--							<input  id="sendUnit" name="sendUnit" value="${bean.sendUnit}"/>--%>
                    </td>
                    <%-- 公文级别 --%>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 30px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="edoc.unitLevel.label"/></font>
                        </div>
                    </td>
                    <td colSpan="2"
                        style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <div id="unitLevel" name="unitLevel">
                            <v3x:metadataItemLabel metadata="${colMetadata['edoc_unit_level']}" value="${summary.unitLevel}"/>
                        </div>
                    </td>
                </tr>


                <tr>
                    <%-- 公文文号     --%>
                    <td style="HEIGHT: 35px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 10px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.wordNo"/></font>
                        </div>
                    </td>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="docMark" name="docMark">
                            ${v3x:toHTML(bean.docMark)}
                        </div>
                    </td>
                    <%-- 公文种类      --%>
                    <td style="padding-right: 30px; border: 1pt solid rgb(255, 0, 0); vertical-align: middle;" width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" style="white-space:pre-line;" color="#ff0000" size="4"><fmt:message key="edoc.element.doctype" bundle="${edocI18N}"/></font>
                        </div>
                    </td>
                    <td colSpan="2"
                        style="border-style: solid none solid solid; padding: 1px; vertical-align: middle; border-top-color: rgb(255, 0, 0); border-bottom-color: rgb(255, 0, 0); border-left-color: rgb(255, 0, 0); border-top-width: 1pt; border-bottom-width: 1pt; border-left-width: 1pt;">
                        <div id="docType" name="docType">
                            <v3x:metadataItemLabel metadata="${colMetadata['edoc_doc_type']}" value="${bean.docType}"/>
                        </div>
                    </td>
                </tr>

                <c:if test="${bean.isTurnRec == 0 }">
                    <tr>
                            <%-- 签发人   --%>
                        <td style="HEIGHT: 32px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 20px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                            nowrap="nowrap">
                            <div style="text-align: right;">
                                <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.signingpeople"/></font>
                            </div>
                        </td>
                        <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                            <div id="issuer" name="issuer">
                                    ${v3x:toHTML(bean.issuer)}
                            </div>
                        </td>
                            <%-- 签发日期    --%>
                        <td style="padding-right:30px; border: 1pt solid rgb(255, 0, 0); vertical-align: middle;" width="100%;" nowrap="nowrap">
                            <div style="text-align: right;">
                                <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.signingdate"/></font>
                            </div>
                        </td>
                        <td colSpan="2"
                            style="border-style: solid none solid solid; padding: 1px; vertical-align: middle; border-top-color: rgb(255, 0, 0); border-bottom-color: rgb(255, 0, 0); border-left-color: rgb(255, 0, 0); border-top-width: 1pt; border-bottom-width: 1pt; border-left-width: 1pt;">
                            <div id="issueDate" name="issueDate">
                                    ${v3x:formatDateByPattern(bean.issueDate,'yyyy年MM月dd日 HH:mm')}
                            </div>
                        </td>
                    </tr>
                </c:if>


                <tr>
                    <%-- 密级       --%>
                    <td style="HEIGHT: 37px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 20px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" style="white-space:pre-line;" color="#ff0000" size="4"><fmt:message key="exchange.edoc.secretlevel"/></font>
                        </div>
                    </td>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="secretLevel" name="secretLevel">
                            <v3x:metadataItemLabel metadata="${colMetadata.edoc_secret_level}" value="${bean.secretLevel}"/>
                        </div>
                    </td>
                    <%-- 紧急程度        --%>
                    <td style="padding-right: 30px; border: 1pt solid rgb(255, 0, 0); vertical-align: middle;" width="100%;" nowrap="nowrap">
                        <div style="text-align: right;">
                            <font face="宋体" style="white-space:pre-line;" color="#ff0000" size="4"><fmt:message key="exchange.edoc.urgentlevel"/></font>
                        </div>
                    </td>
                    <td colSpan="2"
                        style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <v3x:metadataItemLabel metadata="${colMetadata['edoc_urgent_level']}" value="${bean.urgentLevel}"/>
                    </td>
                </tr>

                <tr>
                    <%-- 根据国家行政公文规范,去掉主题词
                    <td style="BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div>
                            <font face="宋体" color="#ff0000" size="2"><fmt:message key="edoc.element.keyword" bundle="${edocI18N}" /></font>
                        </div>
                    </td>
                    <td style="BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid">
                        <div id="keywords" name="keywords">${v3x:toHTML(bean.keywords)}
                        </div>
                    </td> --%>
                    <%-- 份数       --%>
                    <td style="HEIGHT: 33px;BORDER-left: none ;BORDER-RIGHT: #ff0000 1pt solid; PADDING-RIGHT: 20px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid"
                        nowrap="nowrap">
                        <%-- <c:if test="${bean.isTurnRec == 0 }"> --%>
                        <div style="text-align: right;">
                            <font face="宋体" color="#ff0000" size="4"><fmt:message key="exchange.edoc.copy"/></font>
                        </div>
                        <%-- </c:if> --%>
                    </td>
                    <td colSpan="4"
                        style="PADDING-RIGHT: 1px; BORDER-TOP: #ff0000 1pt solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #ff0000 1pt solid; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1pt solid; BORDER-RIGHT-STYLE: none">
                        <%-- <c:if test="${bean.isTurnRec == 0 }"> --%>
                        <div id="copies" name="copies">
                            ${copies}
                        </div>
                        <%-- </c:if> --%>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <fieldset width="60%" style="border: 0px;margin-top: 30px;">
            <legend style="color: red;font-size: 20px;">主文件</legend>
        </fieldset>
        <table style="border-color: red;border-top: #ff0000 1pt solid;border-bottom:  #ff0000 1pt solid;" cellspacing="0" width="60%" align="center">
            <tbody id="mainTable" align="center">

            </tbody>
        </table>
        <fieldset width="60%" style="border: 0px">
            <legend style="color: red;font-size: 20px;">附件信息</legend>
        </fieldset>
        <table style="border-color: red;border-top: #ff0000 1pt solid;border-bottom:  #ff0000 1pt solid;" cellspacing="0" width="60%" align="center">
            <%--            <thead style="color: red;height: 35px" >--%>
            <%--            <th style="height: 35px;font-size: 14px;">附件名称</th>--%>
            <%--&lt;%&ndash;            <th style="height: 35px;font-size: 14px;">创建时间</th>&ndash;%&gt;--%>
            <%--            </thead>--%>
            <tbody id="fileTable" align="center">

            </tbody>
        </table>

        <c:if test="${bean.exchangeMode ne 1}">
            <div id="sendButton" name="sendButton" class="" style="clear:both;">
                <table border="0" width="60%">
                    <tr>
                        <c:if test="${v3x:hasPlugin('sursenExchange') && modelType == 'toSend'}">
                            <td height="42" align="left" width="50%;">
                                <fmt:message key="edoc.exchangeMode"/><fmt:message key="label.colon"/><%-- 交换方式： --%>
                                <input id="internalExchange" type="checkbox" name="exchangeMode" value="0" checked="checked"><fmt:message key="edoc.exchangeInternal.input"/></input> <%-- 交内部公文交换 --%>
                                <input id="sursenExchange" type="checkbox" name="exchangeMode" value="1"><fmt:message key="edoc.exchangeSursen.input"/></input> <%-- 交书生公文交换 --%>
                            </td>
                        </c:if>

                        <td height="42" align="${!v3x:hasPlugin('sursenExchange')?'center':'left'}">
                            <input id="oprateBut" type="button" value="<fmt:message key='exchange.edoc.send' />" class="button-default_emphasize" onclick="oprateSubmit();">
                            <input type="button" value="<fmt:message key='common.toolbar.print.label' bundle='${v3xCommonI18N}' />" class="button-default-2" onclick="sendPrint2();">
                        </td>
                    </tr>
                </table>
            </div>
            <div class="hidden" id="sent" name="sent" style="width: 100%">
                <div id="div1" name="div1">
                    <table border="0" align="center" cellspacing="0" cellpadding="0" id="detailTable" name="detailTable" width="80%">
                        <tr>
                            <td height="30px" style="BORDER-LEFT: none; border-right: none; border-bottom:none">&nbsp;</td>
                        </tr>
                        <tr>
                            <td align="center" class="td-detail">
                                <strong><font face="宋体" color="#ff000" size="2"><fmt:message key="exchange.edoc.replyinfo"/></font></strong>
                            </td>
                        </tr>
                        <!-- 客开：徐矿集团【添加已签收和未签收数量】 chenqiang 2019年4月02日 start -->
                        <tr>
                            <td align="center">
                                <strong><font face="宋体" color="#00000" size="2">已签收：${bean.xkjtSign}&nbsp;&nbsp;未签收：${bean.xkjtPresign}</font></strong>
                            </td>
                        </tr>
                        <!-- 客开：徐矿集团【添加已签收和未签收数量】 chenqiang 2019年4月02日 end -->
                        <tr>
                            <!-- 项目：徐州矿物集团【让页面默认显示6条数据】 作者：wxt.xiangrui 时间：2019-6-3 start -->
                            <td style="height: 290px;">
                                <!-- 项目：徐州矿物集团【让页面默认显示6条数据】 作者：wxt.xiangrui 时间：2019-6-3 end -->
                                    <%-- 组件没有分页信息的时候高度不对，补回30px; --%>
                                <div id="scrollListDiv" style="width: 100%;height: 100%;">
                                    <v3x:table htmlId="sendDetail" data="${bean.sendDetailList}" var="detail" width="100%" showPager="false" bundle="${edocI18N}">
                                        <!-- 客开：徐矿集团【添加全选的复选框】 chenqiang 2019年3月18日 start -->
                                        <v3x:column width="5%" align="center" label="<input type='checkbox' onclick='selectAll(this, \"xkjtId\")'/>">
                                            <input type='checkbox' name='xkjtId' value="<c:out value="${bean.id}"/>" daiyueId="${detail.daiyueId}" leaderId="${detail.leaderId}" sendRecordId="${bean.id}" detailId="${detail.id}"
                                                   accountId="${detail.recOrgId}" status="${detail.status}"/>
                                        </v3x:column>
                                        <!-- 客开：徐矿集团【添加全选的复选框】 chenqiang 2019年3月18日 start -->
                                        <v3x:column width="20%" type="String" label="exchange.edoc.exchangeUnit">
                                            <a style="color: #000" title="${bean.exchangeOrgName}">${v3x:getLimitLengthString(bean.exchangeOrgName,-1,"...")}</a>
                                        </v3x:column>
                                        <v3x:column width="20%" type="String" label="exchange.edoc.receiveaccount">
                                            <c:if test="${not empty detail.recOrgName}"><%--将bean.status==1条件 改为not empty detail.recOrgName，修复GOV-3319 --%>
                                                <a style="color: #000" title="${detail.recOrgName}">${v3x:getLimitLengthString(detail.recOrgName,-1,"...")}</a>
                                            </c:if>
                                        </v3x:column>
                                        <v3x:column width="13%" type="String" label="exchange.edoc.signingNo" alt="${detail.recNo}">
                                            <c:if test="${detail.status!=0}">
                                                ${v3x:toHTML(v3x:getLimitLengthString(detail.recNo,-1,"..."))}
                                            </c:if>
                                        </v3x:column>
                                        <v3x:column width="9%" type="String" label="exchange.edoc.receivedperson">
                                            <c:if test="${detail.status!=0}">
                                                <span style="font-size:12px">${detail.recUserName }</span>
                                            </c:if>

                                            <!-- 项目：徐州矿物集团【如果送往单位为个人，那么签收单位和签收人是同一条数据】 作者：wxt.xiangrui 时间：2019-6-3 start -->
                                            <c:if test="${detail.recUserName==null&&detail.recTime!=null}">
                                                <c:if test="${not empty detail.recOrgName}"><%--将bean.status==1条件 改为not empty detail.recOrgName，修复GOV-3319 --%>
                                                    <a style="color: #000" title="${detail.recOrgName}">${v3x:getLimitLengthString(detail.recOrgName,-1,"...")}</a>
                                                </c:if>
                                            </c:if>
                                            <!-- 项目：徐州矿物集团【如果送往单位为个人，那么签收单位和签收人是同一条数据】 作者：wxt.xiangrui 时间：2019-6-3 start -->

                                        </v3x:column>
                                        <v3x:column width="12%" type="Date" align="center" label="exchange.edoc.receiveddate">
                                            <c:if test="${detail.status!=0}">
                                                <span style="font-size:12px"><fmt:formatDate value='${detail.recTime}' pattern='yyyy-MM-dd HH:mm'/></span>
                                            </c:if>
                                        </v3x:column>
                                        <v3x:column width="6%" type="String" align="center" label="exchange.edoc.status">
                                            <!-- 客开：徐矿集团【发给人时状态11为待签收12为已签收】 chenqiang 2019年4月4日 start -->
                                            <c:choose>
                                                <c:when test="${detail.status==0}">
                                                    <span style="font-size:12px"><fmt:message key='common.toolbar.presign.label' bundle='${v3xCommonI18N}'/></span>
                                                </c:when>
                                                <c:when test="${detail.status==1}">
                                                    <span style="font-size:12px"><fmt:message key="exchange.edoc.sign"/></span>
                                                </c:when>
                                                <c:when test="${detail.status==2}">
                                                    <a href="javascript:openStepBackInfo(1,'${detail.recOrgId}')"><fmt:message key="exchange.edoc.yihuitui"/>
                                                </c:when>
                                                <c:when test="${detail.status==3}">
                                                    <a href="javascript:openSendCancelInfo(1,'${detail.recOrgId}')"><fmt:message key="exchange.edoc.yichexiao"/>
                                                </c:when>
                                                <c:when test="${detail.status==13}">
                                                    <a href="javascript:(0)"><fmt:message key="exchange.edoc.yichexiao"/>
                                                </c:when>
                                                <c:when test="${detail.status==11}">
                                                    <span style="font-size:12px">未阅</span>
                                                </c:when>
                                                <c:when test="${detail.status==12}">
                                                    <span style="font-size:12px">已阅</span>
                                                </c:when>

                                            </c:choose>
                                            <!-- 客开：徐矿集团【发给人时状态11为待签收12为已签收】 chenqiang 2019年4月4日 end -->
                                        </v3x:column>
                                        <c:if test="${bean.status!=0}">
                                            <v3x:column width="12%" type="String" align="center" label="hasten.label">


                                                <%-- 项目：徐州矿物集团【待签收的可以催办】 作者：wxt.xiangrui 时间：2019-6-5 start --%>
                                                <c:if test="${detail.status==0}">
                                                    <a href="javascript:openCuiban('${detail.id }');"/><fmt:message key="hasten.label" bundle="${edocI18N}"/>（${detail.cuibanNum}<fmt:message key="edoc.supervise.count" bundle="${edocI18N}"/>）</a>
                                                </c:if>
                                                <%-- 项目：徐州矿物集团【待签收的可以催办】 作者：wxt.xiangrui 时间：2019-6-5 end --%>
                                                <!-- 客开：徐矿集团【发给人时不显示催办】 chenqiang 2019年4月4日 start -->
                                                <c:choose>

                                                    <c:when test="${detail.status==11 or detail.status==12 or detail.status==13 or detail.status==0}"></c:when>

                                                    <c:otherwise><span style="font-size:12px"><fmt:message key="hasten.label" bundle="${edocI18N}"/>（${detail.cuibanNum}<fmt:message key="edoc.supervise.count"
                                                                                                                                                                                     bundle="${edocI18N}"/>）</span></c:otherwise>

                                                </c:choose>
                                                <!-- 客开：徐矿集团【发给人时不显示催办】 chenqiang 2019年4月4日 end -->
                                            </v3x:column>
                                        </c:if>
                                        <v3x:column width="5%" align="center" label="exchange.send.withdraw">
                                            <c:if test="${detail.status==0}">
                                                <a href="javascript:withdraw('${bean.id}','${detail.id}','${detail.recOrgId}');" id="withdrawhref${detail.id}"/><fmt:message key='exchange.send.withdraw'/></a>
                                            </c:if>
                                            <!-- 客开：徐矿集团【发给人时显示撤销】 chenqiang 2019年4月4日 start -->
                                            <c:if test="${detail.status==11}">
                                                <a href="javascript:withdrawDaiyue('${detail.daiyueId}');" id="withdrawhref${detail.daiyueId}"/><fmt:message key='exchange.send.withdraw'/></a>
                                            </c:if>
                                            <!-- 客开：徐矿集团【发给人时显示撤销】 chenqiang 2019年4月4日 end -->
                                        </v3x:column>
                                    </v3x:table>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </c:if>
    </div>

    <div id="sendButton2" name="sendButton2" class="hidden">
        <table border="0" width="20%" align="center">
            <tr>
                <td height="42" align="center">
                    <input type="button" value="<fmt:message key='common.toolbar.print.label' bundle='${v3xCommonI18N}' />" class="button-default-2" onclick="sendPrint1();">
                </td>
                <!-- 客开：徐矿集团【添加全部撤销按钮】 chenqiang 2019年3月18日  start -->
                <td height="42" align="center">
                    <input type="button" value="全部撤销" class="button-default-2" onclick="withdrawAll();">
                </td>
                <!-- 客开：徐矿集团【添加全部撤销按钮】 chenqiang 2019年3月18日  end -->
            </tr>
        </table>
    </div>
</form>
<iframe name="edocDetailIframe" frameborder="0" height="0" width="0" scrolling="no" marginheight="0" marginwidth="0"></iframe>

<script type="text/javascript">
    initiate('${modelType}');

    /* 客开：徐矿集团【撤销领导人待阅】 chenqiang 2019年4月16日  start */
    function withdrawDaiyue(id) {
        var param = {
            id: id,
            status: 13
        };
        var requestCaller = new XMLHttpRequestCaller(this, "xkjtManager", "updateXkjtLeaderDaiYueByCondition", false);
        requestCaller.addParameter(1, "String", param.id);
        requestCaller.addParameter(2, "String", param.status);
        var bool = requestCaller.serviceRequest();
        document.location.reload();
    }

    /* 客开：徐矿集团【撤销领导人待阅】 chenqiang 2019年4月16日  end */
</script>
