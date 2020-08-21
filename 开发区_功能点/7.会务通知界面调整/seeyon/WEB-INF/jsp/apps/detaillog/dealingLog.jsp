<%--
 $Author:  zhaifeng$
 $Rev:  $
 $Date:: 2012-11-07#$:

 Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 This software is the proprietary information of Seeyon, Inc.
 Use is subject to license terms.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<!DOCTYPE html>
<html class="h100b over_hidden">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title></title>
    <script type="text/javascript">
        $(document).ready(function () {
            var grid = $('#showListInfo').ajaxgrid({
                colModel: [
                           {
                               display: "${ctp:i18n('common.workflow.handler')}",//处理人
                               name: 'handler',
                               sortname : 'affair.memberId',
                               sortable : true,
                               width: '10%'
                           },{
                                display: "所属单位",//所属单位
                                name: 'deptName',
                                sortname : 'affair.memberId',
                                sortable : true,
                                width: '10%'
                            }, {
                               display: "${ctp:i18n('common.workflow.policy')}",//节点权限
                               name: 'policyName',
                               sortname : 'affair.nodePolicy',
                               sortable : true,
                               width: '10%'
                           }, {
                               display: "${ctp:i18n('common.deal.state')}",//处理状态
                               name: 'stateLabel',
                               sortname : 'affair.state',
                               sortable : true,
                               width: '9%'
                           }, {
                               display: "${ctp:i18n('common.workflow.create.date')}",//发起/收到时间
                               name: 'createDate',
                               sortname : 'affair.createDate',
                               sortable : true,
                               width: '10%'
                           }, {
                               display: "${ctp:i18n('common.workflow.first.view.date')}",//首次查看时间
                               name: 'firstViewDate',
                               sortname : 'affair.firstViewDate',
                               sortable : true,
                               width: '10%'
                           }, {
                               display: "${ctp:i18n('common.workflow.finish.date')}",//处理时间
                               name: 'finishDate',
                               sortname : 'affair.completeTime',
                               sortable : true,
                               width: '12%'
                           }, {
                               display: "${ctp:i18n('common.workflow.dealTime.date')}",//处理时长
                               name: 'dealTime',
                               sortname : 'affair.completeTime',
                               sortable : true,
                               width: '12%'
                           }, {
                               display: "${ctp:i18n('common.workflow.deadline.date')}",//处理期限
                               name: 'deadline',
                               sortname : 'affair.deadlineDate',
                               sortable : true,
                               width: '12%'
                           }, {
                               display: "${ctp:i18n('collaboration.timeouts.label')}",//超时时长
                               name: 'deadlineTime',
                               sortname : 'affair.completeTime',
                               sortable : true,
                               width: '12%'
                           }],
                render : rend,
                managerName : "detaillogManager",
                managerMethod : "getFlowNodeDetail",
                height:$(document).height()-135,
                resizable :false
            });
            //回调函数
            function rend(txt, data, r, c) {
                //zhou
                var state=data.state;
                if(state==3){
                    if(txt=='待办'){
                        txt = "<span class='color_red' style='font-weight: bold'>"+txt+"</span>";
                    }
                }
                // if(state==2){
                //     if(txt=='已发'){
                //         txt = "<span style='color: #0000cb'>"+txt+"</span>";
                //     }
                // }

                if(c > 3 && ($.trim(txt) === "")){
                    return "－";
                }
                //超期 变红色
                if(c==7){
                	if(data.coverTime == true){
	                	txt = "<span class='color_red'>"+txt+"</span>";
	                }
                }
                return txt;
           }


           $("#logCategory li").bind("click",function(){
              $(this).addClass("current").siblings('li').removeClass('current');
           });
        })
        var summaryId = '${ctp:escapeJavascript(_summaryId)}';
        var _isHistoryFlag = '${ctp:escapeJavascript(_isHistoryFlag)}';
        var app = '${ctp:escapeJavascript(app)}';
        var subApp = '${ctp:escapeJavascript(subApp)}';
		function classificationDisplay(classification,elObj){
            $(".active_item").removeClass("active_item");
            $(elObj).addClass("active_item");
        	var obj = new Object();
        	obj.objectId = summaryId;
        	obj.isHistoryFlag = _isHistoryFlag;
        	obj.subApp = subApp;
        	obj.app = app;
        	if(classification == 'numProcessed'){
        		obj.numProcessed = 1;
        	}else if(classification == 'numPending'){
        		obj.numPending = 1;
        	}else if(classification == 'numViewed'){
        		obj.numViewed = 1;
        	}else if(classification == 'numNotViewed'){
        		obj.numNotViewed = 1;
        	}
        	$("#showListInfo").ajaxgridLoad(obj);
        }
    </script>
    <link rel="stylesheet" type="text/css" href="${path}/apps_res/bpm/portal/css/bpmPortalIndex.css${ctp:resSuffix()}">
    <style type="text/css">
        .stadic_head_height { height: 75px; }
        .stadic_body_top_bottom { overflow-y:hidden; bottom: 0px; top: 75px;overflow-x:hidden;}
    </style>
</head>
<body class="page_color  h100b over_hidden" id='print'>
    <!-- 处理明细 -->
    <div class="stadic_layout">
      <div class="stadic_layout_head stadic_head_height">
                <div id="statisticalToolBar" class="query_menu_bar" style="height: 70px; padding-left: 5px; padding-top:5px; background-color: rgb(245, 247, 251);">
                    <div onclick="classificationDisplay('numTotal',this)" title="${ctp:i18n('common.detail.label.all')}" class="menu_item active_item" >
                        <div class="item_number blue_num" id="numTotal">${numTotal}</div>
                        <div class="item_text">${ctp:i18n('common.detail.label.all')}</div>
                    </div>

                <div class="menu_item" title="${ctp:i18n('common.detail.label.done')}"  style="margin-left:10px;" onclick="classificationDisplay('numProcessed',this)">
                    <div class="item_number green_num" id="numProcessed">${numProcessed}</div>
                    <div class="item_text">${ctp:i18n('common.detail.label.done')}</div>
                </div>

                <div class="menu_item" style="margin-left:10px;" onclick="classificationDisplay('numPending',this)" title="${ctp:i18n('common.detail.label.pending')}">
                    <div class="item_number pending_num" id="numPending">${numPending}</div>
                    <div class="item_text">${ctp:i18n('common.detail.label.pending')}</div>
                </div>

                <c:if test="${configReadStateEnable ne 'disable'}">
                    <div class="menu_item" onclick="classificationDisplay('numViewed',this)" title="${ctp:i18n('common.detail.label.viewed')}" style="margin-left:10px;" onclick="tabSwitch('myDepartment')">
                        <div class="item_number viewed_num" id="numViewed">${numViewed}</div>
                        <div class="item_text">${ctp:i18n('common.detail.label.viewed')}</div>
                    </div>

                    <div class="menu_item" onclick="classificationDisplay('numNotViewed',this)" title="${ctp:i18n('common.detail.label.noteviewed')}" style="margin-left:10px;" onclick="tabSwitch('fromleader')">
                        <div class="item_number numNotViewed_num" id="numNotViewed">${numNotViewed}</div>
                        <div class="item_text">${ctp:i18n('common.detail.label.noteviewed')}</div>
                    </div>

                </c:if>
         </div>

      </div>
      <div class="stadic_layout_body stadic_body_top_bottom">
        <table class="flexme3" id="showListInfo"></table>
      </div>
    </div>
</body>
</html>
