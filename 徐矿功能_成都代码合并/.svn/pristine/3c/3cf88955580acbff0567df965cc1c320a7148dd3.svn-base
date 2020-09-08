var toolbar;
var dataType;//数据类型，0：当前数据；1：转储数据


function showFlowChartAJax(_affairId,_contextCaseId,_contextProcessId,_templateId,_contextActivityId){
	//显示流程图
	showFlowChart(_contextCaseId,_contextProcessId,_templateId,_contextActivityId);
	//发送点击计数ajax请求
	callBackendMethod("colManager","showWFCDiagram",_affairId,"listSent");
}
//显示流程图
function showFlowChart(_contextCaseId,_contextProcessId,_templateId,_contextActivityId){
    var showHastenButton='true';
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
//回调函数
function rend(txt, data, r, c) {
	var open = document.getElementById("open");
	var show = "";
	if(open!=null){
		show = open.value;
	}
    if(c === 1){
    	//标题列加深
	    txt="<span class='grid_black'>"+txt+"</span>";
        //加图标
        //重要程度
        if(data.importantLevel !=""&& data.importantLevel != 1){
            txt = "<span class='ico16 important"+data.importantLevel+"_16 '></span>"+ txt ;
        }
        //附件
        if(data.hasAttsFlag === true){
            txt = txt + "<span class='ico16 affix_16'></span>" ;
        }
        //表单授权
        if(data.showAuthorityButton){
            txt = txt + "<span class='ico16 authorize_16'></span>";
        }
        //协同类型
        if(data.bodyType!==""&&data.bodyType!==null&&data.bodyType!=="10"&&data.bodyType!=="30"){
            txt = txt+ "<span class='ico16 office"+data.bodyType+"_16'></span>";
        }
        //流程状态
        if(data.state !== null && data.state !=="" && data.state != "0"){
            txt = "<span class='ico16  flow"+data.state+"_16 '></span>"+ txt ;
        }
        return txt;
    }else if(c===3){
		if(txt == null){
			txt = "";
		}
		/**项目：徐矿集团 【屏蔽协同已发事项列表中的当前待办人】 作者：jiangchenxi 时间：2019年3月11日 start*/
		//if(show==true){
			return "<a href='javascript:void(0)' class='noClick' title='"+txt+"' onclick='showFlowChartAJax(\""+ data.affairId + "\",\""+ data.caseId +"\",\""+data.processId+"\",\""+data.templeteId+"\",\""+data.activityId+"\")'>"+txt+"</a>";
		//}else {
			//return "";
		//}
		//源码
		//return "<a href='javascript:void(0)' class='noClick' title='"+txt+"' onclick='showFlowChartAJax(\""+ data.affairId + "\",\""+ data.caseId +"\",\""+data.processId+"\",\""+data.templeteId+"\",\""+data.activityId+"\")'>"+txt+"</a>";
		/**项目：徐矿集团 【屏蔽协同已发事项列表中的当前待办人】 作者：jiangchenxi 时间：2019年3月11日 end*/
    }else if(c === 4){
        if(data.processIsCoverTime){
            var title = $.i18n('collaboration.listsent.overtime.title');
            txt = "<span class='color_red' title='"+title+"'>"+txt+"</span>";
        }else{
            var title = $.i18n('collaboration.listsent.overtime.no.title');
            txt = "<span title='"+title+"'>"+txt+"</span>";
        }
        return txt;
    }else if(c === 5){
        if(txt != null){
            return $.i18n('message.yes.js');
        } else {
            return $.i18n('message.no.js');
        }
    }else if(c === 6){
    	var v_onclick = "onclick='setTrack(this)'";
        var v_style = "";
    	if(dataType == '1'){//切换转储数据
    		v_onclick = "";
            v_style = " style=\"color:black;\"";
    	}
        //添加跟踪的代码
        if(txt === null || txt === false){
            return "<a href='javascript:void(0)' class='noClick' " + v_onclick + " objState="+data.state+" affairId="+data.affairId+" summaryId="+data.summaryId+" trackType="+data.trackType+" senderId="+data.startMemberId+v_style+">"+$.i18n('message.no.js')+"</a>";
        }else{
            return "<a href='javascript:void(0)' class='noClick' " + v_onclick + " objState="+data.state+" affairId="+data.affairId+" summaryId="+data.summaryId+" trackType="+data.trackType+" senderId="+data.startMemberId+v_style+">"+$.i18n('message.yes.js')+"</a>";
        }
    }else if (c === 7){
    	/**项目：徐矿集团 【屏蔽协同已发事项列表中的流程】 作者：jiangchenxi 时间：2019年3月11日 start*/
    	if(data.processId!=""){
    		return "<a class='ico16 view_log_16 noClick' href='javascript:void(0)' onclick='tempShowDetailLogDialog(\""+data.summaryId+"\",\""+data.processId+"\",2)'></a>";
    	}else{
    		//return "";
    	}
    	//源码
        /*return "<a class='ico16 view_log_16 noClick' href='javascript:void(0)' onclick='tempShowDetailLogDialog(\""+data.summaryId+"\",\""+data.processId+"\",2)'></a>";*/
    	/**项目：徐矿集团 【屏蔽协同已发事项列表中的流程】 作者：jiangchenxi 时间：2019年3月11日 end*/
    	}else{
            return txt;
        }
   }     

function tempShowDetailLogDialog(summaryId, processId, showFlag){
	if(dataType == '1'){//转储数据
		showDetailLogDialog(summaryId, processId, showFlag, true);
	}else{
		showDetailLogDialog(summaryId, processId, showFlag, false);
	}
} 
    
function transmitCol(){
	transmitColFromGrid(grid);
}

//重复发起
function resendColl(){
    resend(grid);
}
//编辑流程
function editWorkFlow(){
	var affairId = "";
	var id_checkbox = grid.grid.getSelectRows();
	if (id_checkbox.length === 1) {
		var selRow = id_checkbox[0]; 
	    affairId = selRow.affairId;
	}
    var bean = new Object();
    bean["affairId"] = affairId; 
    _designWorkflow(grid,function(){
    	callBackendMethod("colManager","transUpdateCurrentInfo",bean);
   	 	try{
   	 		$("#listSent").ajaxgridLoad();
   	 	}catch(e){
   	 	}
   	});
}
//置灰ToolBar
function disabledToolbar() {
    toolbar.disabled("transmit");
    //归档
    if(nodePolicy.pigeonhole && isPigeonholeBtn() && hasDoc=="true"){
        toolbar.disabled("pigeonhole");
    }
    toolbar.disabled("cancelWorkFlow");
    toolbar.disabled("editWorkFlow");
    toolbar.disabled("resend");
    toolbar.disabled("delete");
    toolbar.disabled("relationAuthority");
}
function enabledToolbar(){
    toolbar.enabled("transmit");
    //归档
    if(nodePolicy.pigeonhole && isPigeonholeBtn() && hasDoc=="true"){
        toolbar.enabled("pigeonhole");
    }
    toolbar.enabled("cancelWorkFlow");
    toolbar.enabled("editWorkFlow");
    toolbar.enabled("resend");
    toolbar.enabled("delete");
    toolbar.enabled("relationAuthority");
}
//撤销流程
function cancelWorkFlow(){
	
	
    disabledToolbar();
    var id_checkbox = grid.grid.getSelectRows();
    if (id_checkbox.length === 0) {
        //请选择需要撤销的协同！
        $.alert($.i18n('collaboration.listSent.selectRevokeSyn'));
        enabledToolbar();
        return;
    }
    if(id_checkbox.length > 1){
        //只能选择一条记录!
        $.alert($.i18n('collaboration.listSent.selectOneData'));
        enabledToolbar();
        return;
    }
    var selRow = id_checkbox[0];
    var affairId = selRow.affairId;
    var summaryId = selRow.summaryId;
  //js事件接口
    var idMap ={
    		"summaryID":summaryId,
    		"affairID":affairId
    }
	var sendDevelop = $.ctp.trigger('beforeSentCancel',idMap);
    if (!sendDevelop) {
        //$.alert($.i18n('collaboration.page.js.third.error.alert.js'));
        enabledToolbar();
        return;
    }
    
    
    var processId = selRow.processId;
    //校验开始
    var params = new Object();
    params["summaryId"] = summaryId;
    //校验是否流程结束、是否审核、是否核定，涉及到的子流程调用工作流接口校验
    var canDealCancel = callBackendMethod("colManager","checkIsCanRepeal",params);
    if(canDealCancel.msg != null){
        $.alert(canDealCancel.msg);
        enabledToolbar();
        return;
    }

    if(!isAffairValid(affairId)) {
        enabledToolbar();
    	return;
    }
    //调用工作流接口校验是否能够撤销流程 
    var repeal = canRepeal('collaboration',processId,'start');
    //不能撤销流程
    if(repeal[0] === 'false'){
        $.alert(repeal[1]);
        enabledToolbar();
        return;
    }
    var lockWorkflowRe = lockWorkflow(selRow.processId, $.ctx.CurrentUser.id, 12);
    if(lockWorkflowRe[0] == "false"){
        $.alert(lockWorkflowRe[1]);
        enabledToolbar();
        return;
    }
    var appName = "collaboration";
    var isForm = selRow.bodyType=='20';
    if(isForm){
    	appName= "form";
    }
   
    
    var isSubmit = false;
    //撤销流程
    var dialog = $.dialog({
        url: _ctxPath + "/workflowmanage/workflowmanage.do?method=showRepealCommentDialog&affairId="+affairId,
        width:450,
        height:240,
        bottomHTML:'<label for="trackWorkflow" class="margin_t_5 hand">'+
        			'<input style="position: relative;top:3px;" type="checkbox" id="trackWorkflow" name="trackWorkflow" class="radio_com">'+$.i18n("collaboration.workflow.trace.traceworkflow")+
        			'</label><span class="color_blue hand" style="color:#318ed9;" title="'+$.i18n("collaboration.workflow.trace.summaryDetail2")+
        			'">['+$.i18n("collaboration.workflow.trace.title")+']</span>',
        title:$.i18n('common.repeal.workflow.label'),//撤销流程
        targetWindow:getCtpTop(),
        buttons : [ {
            text : $.i18n('collaboration.button.ok.label'),//确定
            btnType:1,
            handler : function() {
              if(isSubmit){
            	  return;
              }
              isSubmit = true;
        	  enabledToolbar();
              var returnValue = dialog.getReturnValue();
              
              if (!returnValue){
            	  isSubmit = false;
                  return;
              }
              
              if(!executeWorkflowBeforeEvent("BeforeCancel",summaryId,affairId,processId,processId,selRow.activityId,selRow.formRecordid,appName,selRow.formAppId,"",selRow.formViewOperation,"")){
                  enabledToolbar();
                  releaseWorkflowByAction(selRow.processId, $.ctx.CurrentUser.id, 12);
              	return;
          	  }
              
              var ajaxSubmitFunc =function(){
                  //var ajaxColManager = new colManager();
                  var tempMap = new Object();
                  tempMap["repealComment"] = returnValue[0];
                  tempMap["summaryId"] = summaryId; 
                  tempMap["affairId"] = affairId;
                  tempMap["isWFTrace"] =  returnValue[1];
                  callBackendMethod("colManager","transRepal",tempMap,{
                      success: function(msg){
                          if(msg==null||msg==""){
                              $("#summary").attr("src","");
                              $(".slideDownBtn").trigger("click");
                              $("#listSent").ajaxgridLoad();
                          }else{
                            $.alert(msg);
                          }
                          //撤销后关闭，子页面
                          try{closeOpenMultyWindow(affairId);}catch(e){};
                          dialog.close();
                      }
                  });
              }
              //V50_SP2_NC业务集成插件_001_表单开发高级
              beforeSubmit(affairId,"repeal", returnValue[0],dialog,ajaxSubmitFunc,function(){
            	  releaseWorkflowByAction(selRow.processId, $.ctx.CurrentUser.id, 12);
            	  dialog.close();
              });
            }
          }, {
            text : $.i18n('collaboration.button.cancel.label'),//取消
            handler : function() {
                enabledToolbar();
                releaseWorkflowByAction(selRow.processId, $.ctx.CurrentUser.id, 12);
                dialog.close();
            }
          } ],
          closeParam:{
            show:true,
            handler:function(){
                enabledToolbar();
                releaseWorkflowByAction(selRow.processId, $.ctx.CurrentUser.id, 12);
            }
          }
      });
}  

 //删除
function deleteCol(){
	deleteItems('sent',grid,'listSent',paramMethod);
}
//点击事件
function dbclickRow(data,rowIndex, colIndex){
    if(!isAffairValid(data.affairId)){
        $("#listSent").ajaxgridLoad();
        return;
    }
    var url = _ctxPath + "/collaboration/collaboration.do?method=summary&openFrom=listSent&affairId="+data.affairId;
    var title = data.subject;
    doubleClick(url,escapeStringToHTML(title));
}


//表单授权
var _count = 0;
function relationAuthority(){
	
	_count++;
	if(_count>1){
		alert($.i18n('collaboration.common.repeat.click.js'));//请不要重复点击！
		return;
	}
	
	
    var id_checkbox = grid.grid.getSelectRows();
    if (!id_checkbox){
    	releaseApplicationButtons();
        return;
    }
    var len=id_checkbox.length;
    if (len === 0) {
        //请选择要关联授权的协同!
        $.alert($.i18n('collaboration.listSent.selectAuthorized'));
        releaseApplicationButtons();
        return;
    }
    var moduleIds=new Array();
    var affairIds=new Array();
    for (var i = 0; i < len; i++) {       
        if(parseInt(id_checkbox[i].bodyType) !== 20){
            //只能对表单模板进行关联授权!
            $.alert($.i18n('collaboration.listSent.onlyFromAuthorized'));
            releaseApplicationButtons();
            return;
        }
        moduleIds.push(id_checkbox[i].summaryId);
        affairIds.push(id_checkbox[i].affairId);
    }
    setRelationAuth(moduleIds,1,function(flag){         
         var param = new Object();
         param.affairIds = affairIds;
         param.flag = flag;
         callBackendMethod("colManager","updateAffairIdentifierForRelationAuth",param,{
             success: function(){
             	releaseApplicationButtons();
                 $("#listSent").ajaxgridLoad();
              }
          });
    },releaseApplicationButtons);
}
         
function releaseApplicationButtons(){
	_count =  0;
}        
var zzGzr='';
var grid;
var searchobj;
var nodePolicy;
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
        submenu.push({name: $.i18n('collaboration.transmit.col.label'),click: transmitCol});
    };
    //判断是否有转发邮件的资源权限，如果没有则屏蔽转发协同
    if ($.ctx.resources.contains('F12_mailcreate')) {
        //邮件
    	if (emailShow) {
           submenu.push({name: $.i18n('collaboration.transmit.mail.label'),click: transmitMail});
    	}
    };
    var toolbarArray = new Array();
    //取新建节点权限
  	nodePolicy = $.parseJSON(pTemp.nodePolicy);
  	
  	//转发
  	 if(nodePolicy.forward){
  		 toolbarArray.push({id: "transmit",name:  $.i18n('collaboration.transmit.label'),className: "ico16 forwarding_16",subMenu: submenu});
  	 }
  	
    //归档
    if(nodePolicy.pigeonhole && isPigeonholeBtn() && hasDoc=="true"){
        toolbarArray.push({id: "pigeonhole",name: $.i18n('collaboration.toolbar.pigeonhole.label'),className: "ico16 filing_16",click: function(){
        	doPigeonhole("sent", grid, "listSent");
       }});
    }
    
    	//撤销流程
    if(nodePolicy.cancel){
    	toolbarArray.push({id: "cancelWorkFlow",name: $.i18n('common.repeal.workflow.label'),className: "ico16 revoked_process_16",click:cancelWorkFlow});
    }
    
  	//编辑流程
  	if(nodePolicy.editWorkFlow){
  		toolbarArray.push({id: "editWorkFlow",name: $.i18n('common.design.workflow.label'),className: "ico16 process_16",click:editWorkFlow});
  	}
    	//重复发起
  	if(nodePolicy.repeatSend){
    	toolbarArray.push({id: "resend",name: $.i18n('common.toolbar.resend.label'),className: "ico16 repeat_launched_16",click:resendColl});
  	}
    //删除
  	if(nodePolicy.reMove){
  		toolbarArray.push({id: "delete",name: $.i18n('collaboration.button.delete.label'),className: "ico16 del_16",click:deleteCol});
  	}
    //表单授权(安装了表单高级插件才有表单授权)
    if (isFormAdvanced == "true" && isV5Member) {
        toolbarArray.push({id: "relationAuthority",name: $.i18n('common.toolbar.relationAuthority.label'),className: "ico16 authorize_16",click:relationAuthority});
    }
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
    condition.push({
        id: 'title',
        name: 'title',
        type: 'input',
        text: $.i18n("common.subject.label"),//标题
        value: 'subject',
        maxLength:100
    });
    condition.push({
        id: 'templateName',
        name: 'templateName',
        type: 'input',
        text: $.i18n("ctp.dr.template.name.js"),//模板名称
        value: 'templateName'
    });
    condition.push({
        id: 'importent',
        name: 'importent',
        type: 'select',
        text: $.i18n("common.importance.label"),//重要程度
        value: 'importantLevel',
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
    condition.push({
        id: 'datetime',
        name: 'datetime',
        type: 'datemulti',
        text: $.i18n("common.date.sendtime.label"),//发起时间
        value: 'createDate',
        ifFormat:'%Y-%m-%d',
        dateTime: false
    });
    condition.push({
    	id:'deadlineDatetime',
    	name:'deadlineDatetime',
    	type:'datemulti',
    	text:$.i18n("collaboration.process.cycle.label"),
    	value:'deadlineDatetime',
    	ifFormat:'%Y-%m-%d',
    	dateTime:false
    });
    //流程状态
    condition.push({
    	id:'workflowState',
    	name:'workflowState',
    	type:'select',
    	text:$.i18n("common.flow.state.label"),
    	value: 'state',
        items:[{
        	text:$.i18n("collaboration.unend"),
        	value:'0'
        },{
        	text:$.i18n("collaboration.ended"),
        	value:'1'
        },{
        	text:$.i18n("collaboration.terminated"),
        	value:'2'
        }]
    });

    //是否超期:流程超期查询出来。
    condition.push({
    	id:'isOverdue',
    	name:'isOverdue',
    	type:'select',
    	text: $.i18n('collaboration.condition.summaryOverdue'), //流程超期
    	value:'isOverdue',
    	items: [{
            text: $.i18n('message.yes.js'),
            value: '1'
        }, {
            text: $.i18n('message.no.js'),
            value: '0'
        }]
    });
    //接收人
    condition.push({
        id: 'receiver',
        name: 'receiver',
        type: 'input',
        text: $.i18n("collaboration.listsent.receiver.label.js"),//接收人
        value: 'receiver'
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
	    condition.push({id:'saoyisao',
	        name:'saoyisao',
	        type:'barcode',
	        text: $.i18n('common.barcode.search.saoyisao'),
	        value:'barcode'
	    });
    }
    
    searchobj = $.searchCondition({
        top:topSearchSize,
        right:60,
        searchHandler: function(){//chenxd
        	
            var val = searchobj.g.getReturnValue();
            if(val !== null){
                $("#listSent").ajaxgridLoad(getSearchValueObj());
            }
        },
        conditions:condition
    });
    //表格加载
        grid = $('#listSent').ajaxgrid({
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
        }, {
            display:  $.i18n("common.date.sendtime.label"),//发起时间
            name: 'startDate',
            sortable : true,
            width: 'medium'
        },{
            display:  $.i18n("collaboration.list.currentNodesInfo.label"),//当前处理人
            name: 'currentNodesInfo',
            sortable : true,
            width: 'medium'
        },{
            display: $.i18n("collaboration.process.cycle.label"),//流程期限
            name: 'processDeadLineName',
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
        parentId: $('.layout_center').eq(0).attr('id'),
        vChange: true,
        vChangeParam: {
            overflow: "hidden",
            autoResize:false //表格下方是否自动显示
        },
        isHaveIframe:true,
        slideToggleBtn:true,
        managerName : "colManager",
        managerMethod : "getSentList"
    });
    
    
    //页面底部说明加载
    $('#summary').attr("src","listDesc.do?method=listDesc&type=listSent&size="+grid.p.total+"&r=" + Math.random() + CsrfGuard.getUrlSurffix());
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
            ,panels:'Department,Team,Post,Outworker,RelatePeople'
            ,selectType:'Member'
            ,text:$.i18n('common.default.selectPeople.value')
            ,showFlowTypeRadio: false
            ,returnValueNeedType: false
            ,params:{
               value: zzGzr
            }
            ,targetWindow:getCtpTop()
            ,callback : function(res){
                if(res && res.obj && res.obj.length>0){
                        $("#zdgzry").val(res.value);
                }
            }
        });
   });
    
   
    
});

//二维码传参chenxd
function precodeCallback(){
	var obj = getSearchValueObj();
	obj.openFrom = "listSent";
	return obj;
}

function getSearchValueObj(){
	o = new Object();
    var templeteIds = $.trim(_paramTemplateIds);
    if(templeteIds != ""){
        o.templeteIds = templeteIds;
    }
    var choose = $('#'+searchobj.p.id).find("option:selected").val();
  //使用高级查询条件查询之后，再使用普通查询时，查询条件不生效
    if(advanceObj && !choose){
    	o = advanceObj;
    }else{
    	if(choose === 'subject'){
    		o.subject = $('#title').val();
    	}else if(choose === 'templateName'){
    		o.templateName = $('#templateName').val();
    	}else if(choose === 'importantLevel'){
    		o.importantLevel = $('#importent').val();
    	}else if(choose === 'createDate'){
    		var fromDate = $('#from_datetime').val();
    		var toDate = $('#to_datetime').val();
    		var date = fromDate+'#'+toDate;
    		o.createDate = date;
    		if(fromDate != "" && toDate != "" && fromDate > toDate){
    			$.alert($.i18n('collaboration.rule.date'));//开始时间不能早于结束时间
    			return;
    		}
    	}else if(choose === 'deadlineDatetime'){//流程期限
    		o.deadlineDatetime=$('#deadlineDatetime').val();
    		var fromDate = $('#from_deadlineDatetime').val();
    		var toDate = $('#to_deadlineDatetime').val();
    		var date = fromDate+'#'+toDate;
    		o.deadlineDatetime = date;
    		if(fromDate != "" && toDate != "" && fromDate > toDate){
    			$.alert($.i18n('collaboration.rule.date'));//开始时间不能早于结束时间
    			return;
    		}
    	}else if(choose === 'state'){ //流程状态
    		o.workflowState=$('#workflowState').val();
    	}else if(choose == 'isOverdue') {
    		o.isOverdue = $("#isOverdue").val();
    	}else if(choose == 'receiver'){
    		o.receiver = $("#receiver").val();
    	}else if(choose == 'affairArchiveId'){
            o.affairArchiveId = $("#affairArchiveId").val();
        }
    }
    if(window.location.href.indexOf("condition=templeteAll&textfield=all") != -1){
		o.templeteAll="all";
	}
    //判断获取主库数据还是分库数据
    if(dataType == '1'){
    	o.dumpData = 'true';
    }else{
    	o.dumpData = 'false';
    }
    return o;
}

function currentData(){
	enabledToolbar();

	document.getElementById("currentData_a").style.display = "none";
	document.getElementById("dumpData_a").style.display = "";

	//控制是否展示无法查询的条件
	var objs = $('a[value=receiver]');
	if(objs != null){
		for(var i = 0 ; i < objs.length ; i++){
			objs[i].style.display = '';
		}
	}
	
	dataType = '0'; //当前数据
	
	$("#listSent").ajaxgridLoad(getSearchValueObj());
}

//转储数据
function dumpData(){
	disabledToolbar();

	document.getElementById("dumpData_a").style.display = "none";
	document.getElementById("currentData_a").style.display = "";

	//控制是否展示无法查询的条件
	var objs = $('a[value=receiver]');
	if(objs != null){
		for(var i = 0 ; i < objs.length ; i++){
			objs[i].style.display = 'none';
		}
	}

	dataType = '1'; //转储数据

	$("#listSent").ajaxgridLoad(getSearchValueObj());
}