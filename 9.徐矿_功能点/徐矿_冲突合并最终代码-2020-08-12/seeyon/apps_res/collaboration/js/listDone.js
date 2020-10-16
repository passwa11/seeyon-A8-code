var toolbar;
var dataType;//数据类型，0：当前数据；1：转储数据
function showFlowChartAJax(_affairId,_contextCaseId,_contextProcessId,_templateId,_contextActivityId){
	//显示流程图
	showFlowChart(_contextCaseId,_contextProcessId,_templateId,_contextActivityId);
	//发送点击计数ajax请求
	callBackendMethod("colManager","showWFCDiagram",_affairId,"listDone");
}
//显示流程图
function showFlowChart(_contextCaseId,_contextProcessId,_templateId,_contextActivityId){
    var showHastenButton='false';
    var supervisorsId="";
    var isTemplate=false;
    var operationId="";
    var senderName="";
    var openType=getA8Top();
    if(_templateId && "undefined"!=_templateId && "null"!=_templateId ){
        isTemplate=true;
    }
    var showHisWorkflow = false;
    if(dataType == '1'){
    	showHisWorkflow = true;
    }
    showWFCDiagram(openType,_contextCaseId,_contextProcessId,isTemplate,showHastenButton,supervisorsId,window, 'collaboration', false ,_contextActivityId,operationId,'' ,senderName, showHisWorkflow);
}
function rend(txt, data, r, c) {
    if (c === 1) {
        // 标题列加深
        txt = "<span class='grid_black'>" + txt + "</span>";
        // 如果是代理 ，颜色变成蓝色
        if (data.proxy) {
            txt = "<span class='color_blue'>" + txt + "</span>";
        }
        // 加图标
        // 重要程度
        if (data.importantLevel != "" && data.importantLevel != 1) {
            txt = "<span class='ico16 important" + data.importantLevel + "_16 '></span>" + txt;
        }
        // 附件
        if (data.hasAttsFlag == true) {
            txt = txt + "<span class='ico16 affix_16'></span>";
        }
        // 协同类型
        if (data.bodyType != "" && data.bodyType != null && data.bodyType != "10" && data.bodyType != "30") {
            txt = txt + "<span class='ico16 office" + data.bodyType + "_16'></span>";
        }
        // 流程状态
        if (data.state != null && data.state != "" && data.state != "0") {
            txt = "<span class='ico16  flow" + data.state + "_16 '></span>" + txt;
        }
        // 如果设置了处理期限(节点期限),添加超期图标
        if ((data.deadLineDate != null && data.deadLineDate != "" && data.deadLineDate != "0")
                || (data.expectedProcessTime != null && data.expectedProcessTime != "" && data.expectedProcessTime != "0")) {
            if (data.isCoverTime) {
                // 超期图标
                txt = txt + "<span class='ico16 extended_red_16'></span>";
            } else {
                // 未超期图标
                txt = txt + "<span class='ico16 extended_blue_16'></span>";
            }
        }
        return txt;
    } else if (c === 6) {
        if (txt == null) {
            txt = "";
        }
        return "<a href='javascript:void(0)' title='" + txt + "' class='noClick' onclick='showFlowChartAJax(\""
                + data.affairId + "\",\"" +data.caseId + "\",\"" + data.processId + "\",\"" + data.templeteId + "\",\"" + data.activityId
                + "\")'>" + txt + "</a>";
    } else if (c === 7) {
        if ((data.deadLineDate != null && data.deadLineDate != "" && data.deadLineDate != "0")
                || (data.expectedProcessTime != null && data.expectedProcessTime != "" && data.expectedProcessTime != "0")) {
            if (data.isCoverTime) {
                // 节点超期
                var title = $.i18n('collaboration.listDone.nodeIsCoverTime');
                txt = "<span class='color_red' title='" + title + "'>" + txt + "</span>";
            } else {
                // 节点未超期
                var title = $.i18n('collaboration.listDone.nodeNotCoverTime');
                txt = "<span title='" + title + "'>" + txt + "</span>";
            }
        }
        return txt;
    }else if(c === 8){
        if(txt != null){
            return $.i18n('message.yes.js');
        } else {
            return $.i18n('message.no.js');
        }
    } else if (c === 9) {
        var v_onclick = "onclick='setTrack(this)'";
        var v_style = "";
        if (dataType == '1') {// 切换转储数据
            v_onclick = "";
            v_style = " style=\"color:black;\"";
        }
        // 添加跟踪的代码
        if (txt === null || txt === false) {
            return "<a href='javascript:void(0)' class='noClick' " + v_onclick + " objState=" + data.state
                    + " affairId=" + data.affairId + " summaryId=" + data.summaryId + " trackType=" + data.track
                    + " senderId=" + data.startMemberId + v_style + ">" + $.i18n('message.no.js') + "</a>";
        } else {
            return "<a href='javascript:void(0)' class='noClick' " + v_onclick + " objState=" + data.state
                    + " affairId=" + data.affairId + " summaryId=" + data.summaryId + " trackType=" + data.track
                    + " senderId=" + data.startMemberId + v_style + ">" + $.i18n('message.yes.js') + "</a>";
        }
    } else if (c === 10) {
    	/**项目：徐矿集团 【屏蔽协同已发事项列表中的流程】 作者：jiangchenxi 时间：2019年3月11日 start*/
    	if(data.currentNodesInfo!=""){
        return "<a class='ico16 view_log_16 noClick' href='javascript:void(0)' onclick='tempShowDetailLogDialog(\""
                + data.summaryId + "\",\"" + data.processId + "\",2)'></a>";
    	}else {
			return "<div style='display:none'><a class='ico16 view_log_16 noClick' href='javascript:void(0)' onclick='tempShowDetailLogDialog(\""
                + data.summaryId + "\",\"" + data.processId + "\",2)'></div>";
		}
    } else {
        return txt;
    }
    return txt;
}

function tempShowDetailLogDialog(summaryId, processId, showFlag){
	if(dataType == '1'){//转储数据
		showDetailLogDialog(summaryId, processId, showFlag, true);
	}else{
		showDetailLogDialog(summaryId, processId, showFlag, false);
	}
}

  //删除
function deleteCol(){
	deleteItems('finish',grid,'listDone',paramMethod);
}
   
function transmitCol(){
	transmitColFromGrid(grid);
}

//ajax
function getAffairState(affairId){
  var _state = callBackendMethod("colManager","getAffairState",affairId);
  return _state;
}


 //取回
function takeBack() {
    var rows = grid.grid.getSelectRows();
    if (rows.length === 0) {
        //请选择要取回的协同!
        $.alert($.i18n('collaboration.listDone.selectBack'));
        return;
    }
    if (rows.length > 1) {
        //只能选择一项协同进行取回!
        $.alert($.i18n('collaboration.listDone.selectOneBack'));
        return;
    }

    
    if(getAffairState(rows[0].affairId) != '4') {
    	$.alert($.i18n('collaboration.listDone.tabkeback.state.js'));
        $("#listDone").ajaxgridLoad();
        return;
    }
    /**
     * 是否允许取回
     * 返回值是一个js对象，有以下属性
     * canTakeBack 是否允许取回
     * state:
     *  -1表示程序或数据发生异常,不可以取回
     *  0表示正常状态,可以取回
     *  1表示当前流程已经结束,不可以取回
     *  2表示后面节点任务事项已处理完成,不可以取回
     *  3表示当前节点触发的子流程已经结束,不可以取回
     *  4表示当前节点触发的子流程中已核定通过,不可以取回
     *  5表示当前节点是知会节点,不可以取回
     *  6表示当前节点为核定节点,不可以取回
     *  7表示当前节点为封发节点，不可以取回
     *  9表示当前节点触发的子流程中已审核通过,不可以取回
     */
    var workitemId = rows[0].workitemId;
    var processId = rows[0].processId;
    var caseId = rows[0].caseId;
    var appName = "collaboration";
    var nodeId = rows[0].activityId;
    var isForm = rows[0].bodyType=='20';
    var canTakeBackObj = canTakeBack(workitemId, processId, nodeId, null, caseId, appName, isForm);
    if(canTakeBackObj !=null && !canTakeBackObj.canTakeBack){
        
        var err_msg = "";
        if(canTakeBackObj.state == "10"){
            //超级节点
            err_msg = canTakeBackObj.err_msg;
        }else{
            err_msg = $.i18n('collaboration.takeBackErr.'+canTakeBackObj.state+'.msg');
        }
        
        $.alert(err_msg);
        //$("#listDone").ajaxgridLoad();
        return;
    }

    var lockWorkflowRe = lockWorkflow(processId, $.ctx.CurrentUser.id, 13);
    if(lockWorkflowRe[0] == "false"){
        $.alert(lockWorkflowRe[1]);
        return;
    }
    if(isForm){
    	appName= "form";
    }
    
    
    var isClick = false;//是否点击
    var dialog = $.dialog({
        url: _ctxPath + "/collaboration/collaboration.do?method=showTakebackConfirm",
        width: 400,
        height: 160,
        targetWindow:getCtpTop(),
        title: $.i18n('collaboration.system.prompt.js'),
        buttons: [{
            text: $.i18n('collaboration.pushMessageToMembers.confirm'),
            handler: function () {
            	if(!isClick){
            		isClick = true;
            		var rv = dialog.getReturnValue();
            		if (rv) {
            			
            			var ajaxSubmitFunc = function(){
            				var saveOpinion = (rv !="1");
            				var takeBackBean = new Object();
            				takeBackBean["affairId"] = rows[0].affairId;
            				takeBackBean["isSaveOpinion"] = saveOpinion;                    
            				callBackendMethod("colManager","transTakeBack",takeBackBean,{

            					success: function(msg){
            						if(msg==null||msg==""){
            							$("#summary").attr("src","");
            							$(".slideDownBtn").trigger("click");
            							$("#listDone").ajaxgridLoad();
            						}else{
            							$.alert(msg);
            						}
            						//撤销后关闭，子页面
            						try{closeOpenMultyWindow(rows[0].affairId);}catch(e){};
            						//回退成功后，打开该回退协同处理页面
            						var _url = _ctxPath + "/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId="+rows[0].affairId + (window.CsrfGuard ? CsrfGuard.getUrlSurffix() : "");
            						window.open(_url);
            						dialog.close();
            					}
            				
            				});
            			}
            			
            		    //js事件接口
            		    var idMap = {
            		            "summaryID":rows[0].summaryId,
            		            "affairID":rows[0].affairId
            		    }
            		    var sendDevelop = $.ctp.trigger('beforeDoneTakeBack',idMap);
            		    if(!sendDevelop){
            		        //$.alert($.i18n('collaboration.page.js.third.error.alert.js'));
            		        return;
            		    }
            		    
            			if(!executeWorkflowBeforeEvent("BeforeTakeBack",rows[0].summaryId,rows[0].affairId,processId,processId,nodeId,rows[0].formRecordid,appName)){
            		        return;
            		    }
            			//V50_SP2_NC业务集成插件_001_表单开发高级
            			beforeSubmit(rows[0].affairId,"takeback","",dialog,ajaxSubmitFunc,function(){
            				releaseWorkflowByAction(processId, $.ctx.CurrentUser.id, 13);
            				dialog.close();
            			});
            			
            		}
            		
            	}
                
            }
        }, {
            text: $.i18n('collaboration.pushMessageToMembers.cancel'),
            handler: function () {
                releaseWorkflowByAction(processId, $.ctx.CurrentUser.id, 13);
                dialog.close();
            }
        }],
        closeParam:{
            show:true,
            handler:function(){
                releaseWorkflowByAction(processId, $.ctx.CurrentUser.id, 13);
            }
          }
    });
}

//点击事件
function dbclickRow(data,rowIndex, colIndex){
    if(!isAffairValid(data.affairId)){
        $("#listDone").ajaxgridLoad();
        return;
    }
    var url = _ctxPath + "/collaboration/collaboration.do?method=summary&openFrom=listDone&affairId="+data.affairId;
    var title = data.subject;
    doubleClick(url,escapeStringToHTML(title));
    grid.grid.resizeGridUpDown('down');
    //页面底部说明加载
    $('#summary').attr("src","listDesc.do?method=listDesc&type=listDone&size="+grid.p.total+"&r=" + Math.random() + CsrfGuard.getUrlSurffix());
}


var zzGzr='';
var grid='';
var isFirstClickRow=true;
var searchobj;
var showPigonholeBtn = false;

$(document).ready(function () {
    new MxtLayout({
        'id': 'layout',
        'northArea': {
            'id': 'north',
            'height': 40,
            'sprit': false,
            'border': false
        },
        'centerArea': {
            'id': 'center',
            'border': false,
            'minHeight': 20
        }
    });
    var submenu = new Array();
    //判断是否有新建协同的资源权限，如果没有则屏蔽转发协同
    if ($.ctx.resources.contains('F01_newColl')) {
        //协同
        submenu.push({name: $.i18n('collaboration.transmit.col.label'),click: transmitCol });
    };
    //判断是否有转发邮件的资源权限，如果没有则屏蔽转发协同
    if ($.ctx.resources.contains('F12_mailcreate')) {
        //邮件
    	if (emailShow) {
    		submenu.push({name: $.i18n('collaboration.transmit.mail.label'),click: transmitMail });
    	}
    };
    var toolbarArray = new Array();
    //转发
    toolbarArray.push({id: "transmit",name: $.i18n('collaboration.transmit.label'),className: "ico16 forwarding_16",subMenu: submenu});
    //归档
    if(isPigeonholeBtn() && hasDoc=="true"){
    	showPigonholeBtn = true;
        toolbarArray.push({id: "pigeonhole",name: $.i18n('collaboration.toolbar.pigeonhole.label'),className: "ico16 filing_16",click: function(){doPigeonhole("done", grid, "listDone");}});
    }
    //删除
    toolbarArray.push({id: "delete",name:$.i18n('collaboration.button.delete.label'),className: "ico16 del_16",click:deleteCol});
    //取回
    toolbarArray.push({id: "takeBack",name: $.i18n('common.toolbar.takeBack.label'),className: "ico16 retrieve_16",click:takeBack});
    //回退记录
    toolbarArray.push({id: "stepbackRecord",name:$.i18n('collaboration.workflow.label.stepback'),className: "ico16 toback_16",click:function() {listSBRecord(showPigonholeBtn,hasDumpData);}});
    
    //"只列出智能处理"
    if(hasAIPlugin == "true"){
        var isCheckAI = false;
        if("true" == showAIProcessing){
            isCheckAI = true;
        }
        toolbarArray.push({id: "aiProcessingRecord",type: "checkbox",checked:isCheckAI,text: $.i18n('collaboration.portal.listDone.aiProcessingRecord'),value:"1",click:aiProcessing});
    }
    
    //同一流程只显示最后一条
    toolbarArray.push({id: "deduplication",type: "checkbox",checked:false,text: $.i18n('collaboration.portal.listDone.isDeduplication'),value:"1",click:debupCol});
    if(hasDumpData == "true"){
    	//当前数据
    	toolbarArray.push({id: "currentData", name: $.i18n('collaboration.portal.listDone.currentData.js'), className:"ico16 view_switch_16", click:currentData});
    	//转储数据
    	toolbarArray.push({id: "dumpData", name: $.i18n('collaboration.portal.listDone.dumpData.js'), className:"ico16 view_switch_16", click:dumpData});
    }
    //toolbar扩展
    for (var i = 0;i<addinMenus.length;i++) {
        toolbarArray.push(addinMenus[i]);
    }
    //工具栏
    toolbar = $("#toolbars").toolbar({
        toolbar: toolbarArray
    });

    if(hasDumpData == "true"){
	    //设置按钮样式
	    document.getElementById("currentData_a").style.display = "none";
    }
    //搜索框
    var topSearchSize = 7;
    if($.browser.msie && $.browser.version=='6.0'){
        topSearchSize = 10;
    }
    
    //查询条件
    var condition = new Array();
    //标题
    condition.push({id: 'title',name: 'title',type: 'input',text: $.i18n("common.subject.label"),value: 'subject',maxLength:100});
    //模板名称
    condition.push({id: 'templateName', name: 'templateName',type: 'input',text: $.i18n("ctp.dr.template.name.js"),value: 'templateName'});
    //重要程度
    condition.push({id: 'importent',name: 'importent',type: 'select',text: $.i18n("common.importance.label"),value: 'importantLevel',
        items: [{
            text:  $.i18n("common.importance.putong"),//普通
            value: '1'
        }, {
            text:  $.i18n("common.importance.zhongyao"),//重要
            value: '2'
        }, {
            text:  $.i18n("common.importance.feichangzhongyao"),//非常重要
            value: '3'
        }]
    });
    //发起人
    condition.push({id: 'spender',name: 'spender',type: 'input', text: $.i18n("cannel.display.column.sendUser.label"),value: 'startMemberName'});
    //addby libing 上一处理人
    condition.push({id: 'preApproverName',name: 'preApproverName',type: 'input',text: $.i18n("cannel.display.column.preApprover.label"),value: 'preApproverName'});
    //发起时间
    condition.push({id: 'datetime',name: 'datetime',type: 'datemulti',text: $.i18n("common.date.sendtime.label"),value: 'createDate',ifFormat:'%Y-%m-%d',dateTime: false});
    //处理时间
    condition.push({id: 'dealtime',name: 'dealtime',type: 'datemulti',text: $.i18n("common.date.donedate.label"),value: 'dealDate',ifFormat:'%Y-%m-%d',dateTime: false});
    //流程状态
    condition.push({id: 'status',name: 'status',type: 'select',text: $.i18n("common.flow.state.label"),value: 'workflowState',
        items: [{
            text: $.i18n("collaboration.unend"),//未结束
            value: '0'
        }, {
            text: $.i18n("collaboration.ended"),//已结束
            value: '1'
        }, {
            text: $.i18n("collaboration.terminated"),//已终止
            value: '2'
        }]
    });
    
    //是否超期:节点超期都查询出来。
    condition.push({
    	id:'isOverdue',
    	name:'isOverdue',
    	type:'select',
    	text: $.i18n('collaboration.condition.affairOverdue'), //节点超期
    	value:'isOverdue',
    	items: [{
            text: $.i18n('message.yes.js'),
            value: '1'
        }, {
            text: $.i18n('message.no.js'),
            value: '0'
        }]
    });
    
    //是否归档
    condition.push({
        id:'affairArchiveId',
        name:'affairArchiveId',
        type:'select',
        text: $.i18n("common.pigeonhole.trueOrNot"), //是否归档
        value:'affairArchiveId',
        items: [{
            text: $.i18n('message.yes.js'),
            value: '1'
        }, {
            text: $.i18n('message.no.js'),
            value: '0'
        }]
    });
    
    //扫一扫
    if (hasBarCode=="true") {
    	condition.push({id:'saoyisao',name:'saoyisao',type:'barcode',hide:false,text: $.i18n('common.barcode.search.saoyisao'), value:'barcode'});
    }
    
    searchobj = $.searchCondition({
        top:topSearchSize,
        right:55,
        searchHandler: function(){//chenxd
            
            var val = searchobj.g.getReturnValue();
            if(val !== null){
                $("#listDone").ajaxgridLoad(getSearchValueObj());
                var _summarySrc =  $('#summary').attr("src");
                if(_summarySrc.indexOf("listDesc") != -1){
                	setTimeout(function(){
                		$('#summary').attr("src","listDesc.do?method=listDesc&type=listDone&size="+grid.p.total+"&r=" + Math.random() + CsrfGuard.getUrlSurffix());	
                	},1000);
                }
            }
        },
        conditions:condition
    });
    if (hasAIPlugin == "true" && openFrom == "aiProcess") {
        searchobj.g.setCondition('dealtime', beginTime, endTime);
    }
    //表格加载
    grid = $('#listDone').ajaxgrid({
        colModel: [{
            display: 'id',
            name: 'id',
            width: 'smallest',
            type: 'checkbox',
            align:'center'
        }, {
            display: $.i18n("common.subject.label"),//标题
            name: 'subject',
            sortable : true,
            width: 'big'
        },{
            display: $.i18n("cannel.display.column.sendUser.label"),//发起人
            name: 'startMemberName',
            sortable : true,
            width: 'small'
        },{
            display: $.i18n("cannel.display.column.preApprover.label"),
            name: 'preApproverName',
            sortable : true,
            width: 'small'
        },{
            display:  $.i18n("common.date.sendtime.label"),//发起时间
            name: 'startDate',
            sortable : true,
            width: 'medium'
        }, {
            display: $.i18n("common.date.donedate.label"),//处理时间
            name: 'dealTime',
            sortable : true,
            width: 'medium'
        },{
            display:  $.i18n("collaboration.list.currentNodesInfo.label"),//当前处理人
            name: 'currentNodesInfo',
            sortable : true,
            width: 'medium'
        },{
            display:  $.i18n("pending.deadlineDate.label"),//处理期限（节点期限）
            name: 'nodeDeadLineName',
            sortable : true,
            width: 'medium'
        },{
            display: $.i18n("common.pigeonhole.trueOrNot"),//是否归档
            name: 'affairArchiveId',
            sortable : true,
            width: 'medium'
        }, {
            display: $.i18n("collaboration.isTrack.label"),//跟踪状态
            name: 'isTrack',
            sortable : true,
            width: 'small'
        }, {
            display: $.i18n("processLog.list.title.label"),//流程日志
            name: 'processId',
            width: 'small'
        }],
        click: dbclickRow,
        render : rend,
        height: 200,
        gridType:'autoGrid',
        showTableToggleBtn: true,
        parentId: 'center',
        vChange: true,
        vChangeParam: {
            overflow: "hidden",
            autoResize:false //表格下方是否自动显示
        },
        isHaveIframe:true,
        slideToggleBtn:true,
        managerName : "colManager",
        managerMethod : "getDoneList"
    });
    //页面底部说明加载
    $('#summary').attr("src","listDesc.do?method=listDesc&type=listDone&size="+grid.p.total+"&r=" + Math.random() + CsrfGuard.getUrlSurffix());
    
    
    //跟踪弹出框js
    $("#gz").change(function () {
        var value = $(this).val();
        var _gz_ren = $("#gz_ren");
        switch (value) {
            case "0":
                _gz_ren.hide();
                break;
            case "1":
                _gz_ren.show();
                break;
        }
    });

    $("#radio4").bind('click',function(){
     $.selectPeople({
            type:'selectPeople'
            ,panels:'Department,Team,Post,Level,Role,Outworker,FormField'
            ,selectType:'FormField,Department,Team,Post,Level,Role,Member'
            ,text:$.i18n('common.default.selectPeople.value')
            ,showFlowTypeRadio: true
            ,returnValueNeedType: false
            ,params:{
               value: zzGzr
            }
            ,targetWindow:getCtpTop()
            ,callback : function(res){
                if(res && res.obj && res.obj.length>0){
                        $("#zdgzry").val(res.value);
                } else {
                       
                }
            }
        });
   });
});

//智能处理记录
function aiProcessing(){
    $("#listDone").ajaxgridLoad(getSearchValueObj());
}

//回退记录
function listSBRecord(showPigonHoleBtn,hasDumpData) {
    var url = _ctxPath
            + "/collaboration/collaboration.do?method=listRecord&app=1&record=stepBackRecord&showPigonHoleBtn=" + showPigonHoleBtn + "&hasDumpData=" + hasDumpData + (window.CsrfGuard ? CsrfGuard.getUrlSurffix() : "");
    if(_srcFrom =="bizconfig"){
    	url += "&srcFrom=bizconfig"+"&paramTemplateIds="+_paramTemplateIds;
    }
    window.location.href = url;
}

function getSearchValueObj(){
	o = new Object();
    
	var choose = $('#'+searchobj.p.id).find("option:selected").val();
	//使用高级查询条件查询之后，再使用普通查询时，查询条件不生效
    if(advanceObj && !choose){
    	o = advanceObj;
    }else{
    	var templeteIds = $.trim(_paramTemplateIds);
        if(templeteIds != ""){
            o.templeteIds = templeteIds;
        }
    	if(choose === 'subject'){
    		o.subject = $('#title').val();
    	}else if(choose === 'templateName'){
    		o.templateName = $('#templateName').val();
    	}else if(choose === 'importantLevel'){
    		o.importantLevel = $('#importent').val();
    	}else if(choose === 'startMemberName'){
    		o.startMemberName = $('#spender').val();
    	}else if(choose == 'preApproverName'){
    		o.preApproverName = $('#preApproverName').val();
    	}else if(choose === 'createDate'){
    		var fromDate = $('#from_datetime').val();
    		var toDate = $('#to_datetime').val();
    		if(fromDate != "" && toDate != "" && fromDate > toDate){
    			$.alert($.i18n('collaboration.rule.date'));//开始时间不能早于结束时间
    			return;
    		}
    		var date = fromDate+'#'+toDate;
    		o.createDate = date;
    	}else if(choose === 'dealDate'){
    		var fromDate = $('#from_dealtime').val();
    		var toDate = $('#to_dealtime').val();
    		if(fromDate != "" && toDate != "" && fromDate > toDate){
    			$.alert($.i18n('collaboration.rule.date'));//开始时间不能早于结束时间
    			return;
    		}
    		var date = fromDate+'#'+toDate;
    		o.dealDate = date;
    		//当按照处理时间查询时候，查询所有的信息
    		//o.deduplication = "false";
    	}else if(choose === 'workflowState'){
    		o.workflowState = $('#status').val();
    	}else if(choose == 'isOverdue') {
    		o.isOverdue = $("#isOverdue").val();
    	}else if(choose == 'affairArchiveId'){
            o.affairArchiveId = $("#affairArchiveId").val();
        }
    }
    //同一流程只显示最后一条
    o.deduplication = "false";
    var isDedupCheck =  $("#deduplication").attr("checked");
    if (isDedupCheck) {
        o.deduplication = "true"; 
    }
    
    //判断获取主库数据还是分库数据
    if(dataType == '1'){
    	o.dumpData = 'true';
    }else{
        o.dumpData = 'false';
    }
    
    var selectVal = $("#aiProcessingRecord").attr("checked");
    if(selectVal){
        o.aiProcessing = "true";
    }else{
        o.aiProcessing = "false";
    }

    o = addURLPara(o);
    return o;
}
//二维码传参chenxd
function precodeCallback(){
	var obj = getSearchValueObj();
	obj.openFrom = "listDone";
	return obj;
}

function debupCol(){
    $("#listDone").ajaxgridLoad(getSearchValueObj());
}

function currentData(){
	//控制其他按钮样式
    toolbar.enabled("transmit");
    toolbar.enabled("pigeonhole");
    toolbar.enabled("delete");
    toolbar.enabled("takeBack");
    toolbar.enabled("stepbackRecord");
    document.getElementById("deduplication").disabled = "";
    document.getElementById("deduplication").parentNode.style.opacity = "";
    
    if(hasAIPlugin == "true"){
    	document.getElementById("aiProcessingRecord").disabled = "";
        document.getElementById("aiProcessingRecord").parentNode.style.opacity = "";
    }
    
	document.getElementById("currentData_a").style.display = "none";
	document.getElementById("dumpData_a").style.display = "";

	//控制是否展示无法查询的条件
	var objs = $('a[value=startMemberName],a[value=preApproverName]');
	if(objs != null){
		for(var i = 0 ; i < objs.length ; i++){
			objs[i].style.display = '';
		}
	}
	
	dataType = '0'; //当前数据
	
	debupCol();
}

//转储数据
function dumpData(){
	//控制其他按钮样式
    toolbar.disabled("transmit");
    toolbar.disabled("pigeonhole");
    toolbar.disabled("delete");
    toolbar.disabled("takeBack");
    toolbar.disabled("stepbackRecord");
    document.getElementById("deduplication").disabled = "disabled";
    document.getElementById("deduplication").checked = false;
    document.getElementById("deduplication").parentNode.style.opacity = "0.5";

    if(hasAIPlugin == "true"){
    	document.getElementById("aiProcessingRecord").disabled = "disabled";
    	document.getElementById("aiProcessingRecord").checked = false;
        document.getElementById("aiProcessingRecord").parentNode.style.opacity = "0.5";
    }

	document.getElementById("dumpData_a").style.display = "none";
	document.getElementById("currentData_a").style.display = "";

	//控制是否展示无法查询的条件
	var objs = $('a[value=startMemberName],a[value=preApproverName]');
	if(objs != null){
		for(var i = 0 ; i < objs.length ; i++){
			objs[i].style.display = 'none';
		}
	}
	
	dataType = '1'; //转储数据

	debupCol();
}