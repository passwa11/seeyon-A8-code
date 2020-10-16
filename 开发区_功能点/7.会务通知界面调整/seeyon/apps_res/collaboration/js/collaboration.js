var dialogDealColl;// 双击弹出协同处理框
var page_types = {
  // 待发 已发
    'draft' : 'draft',
    'sent' : 'sent',
    'pending' : 'pending',
    'finish' : 'finish'
};


function deleteItems(pageType,grid,tableId,fromMethod){
  if (!pageType || !page_types[pageType]) {
      $.alert('pageType is illegal:' + pageType);
        return true;
    }
  var rows = grid.grid.getSelectRows();
  var affairIds ="";
  var deleteSubject = "";
  if(rows.length <= 0) {
          // 请选择要删除的协同。
      $.alert($.i18n('collaboration.grid.selectDelete'));
      return true;
   }
  var sendDevelop = $.ctp.trigger('beforeDeleteColl');
	if(!sendDevelop){
		return;
	}
  var processIndex = 0;
    var obj;
    for (var i = 0; i < rows.length; i++) {
        obj = rows[i];
        try{closeOpenMultyWindow(obj.affairId,false);}catch(e){};
          // 指定回退状态状态不能删除
          if(pageType == "draft"){
               if(obj.subState == '16' ){
                  $.alert($.i18n('collaboration.alert.CantModifyBecauseOfAppointStepBack'));
                  obj.checked = false;
                  return true;
                }
           }

          // 当前节点设置不允许删除:
          if(!obj.canReMove){
        	  if (rows.length == 1){//当前节点权限不可进行删除操作！
        		  $.alert($.i18n('collaboration.alert.CantReMoveOne'));
        		  obj.checked = false;
                  return true;
        	  } else {//根据当前节点权限以下流程不可删除！
        		  deleteSubject += "<br>&lt;"+escapeStringToHTML(obj.subject)+"&gt;";
        		  continue;
        	  }
          }

          if(pageType == "pending"){
            if(obj.templeteId){
                  // 未办理的模板协同不允许直接归档或删除!
                $.alert($.i18n('collaboration.template.notHandle.notDeleteArchive') + "<br><br>" + "&lt;"+escapeStringToHTML(obj.subject)+"&gt;");
                obj.checked = false;
                return true;
            }

              var lockWorkflowRe = lockWorkflow(obj.processId, $.ctx.CurrentUser.id, 14);
              if(lockWorkflowRe[0] == "false"){
                  $.alert(lockWorkflowRe[1] + "<br><br>" + "&lt;"+escapeStringToHTML(obj.subject)+"&gt;");
                  obj.checked = false;
                  return true;
              } else {
            	  lockProcessIds[processIndex++] = obj.processId;
              }
              // 以下事项要求意见不能为空，不能直接归档或删除:
              if(obj.canDeleteORarchive){
            	  $.alert($.i18n('collaboration.template.notDeleteArchive.nullOpinion') + "<br><br>" + "&lt;"+escapeStringToHTML(obj.subject)+"&gt;");
            	  obj.checked = false;
                  unlockWorkflow();
                  return true;
              }
              // 指定回退时不能处理
              var canSubmitWorkFlowRe= canSubmitWorkFlow(obj.workitemId);
              if(canSubmitWorkFlowRe[0]== "false"){
                $.alert(canSubmitWorkFlowRe[1]);
                check = true;
                unlockWorkflow();
                return false;
              }
          }
    }
    if (deleteSubject.length > 1) {
    	$.alert($.i18n('collaboration.alert.CantReMove') + deleteSubject);
    	obj.checked = false;
        return true;
    }

  // end
  var confirm = $.confirm({
          // 该操作不能恢复，是否进行删除操作
      'msg': $.i18n('collaboration.confirmDelete'),
      ok_fn: function () {
          for(var count = 0 ; count < rows.length; count ++){
                if(count == rows.length -1){
                    affairIds += rows[count].affairId;
                }else{
                    affairIds += rows[count].affairId +",";
                }
            }
            // table提交
             //var callerResponder = new CallerResponder();
            // 实例化Spring BS对象
           // var collManager = new colManager();

            var obj = new Object();
            obj.pageType=pageType;
            obj.affairIds = affairIds;
            obj.fromMethod= fromMethod;
            //collManager.checkCanDelete(obj,{});
            callBackendMethod("colManager","checkCanDelete",obj,{

                success : function(flag){
                    if("success" == flag){
                          // 循环删除，隔离后台的事务，否则多个流程事务一起提交工作流部分的数据可能会出错
                          var ids = affairIds.split(",");
                          for(var i=0 ;i<ids.length;i++){
                             //collManager.deleteAffair(pageType,ids[i]);
                             callBackendMethod("colManager","deleteAffair",pageType,ids[i]);
                          }
                          //关闭已经删除了事项打开的子页面
                          for (var i = 0 ; i<ids.length;i++) {
                              try{closeOpenMultyWindow(ids[i],false);}catch(e){}
                          }

                        // 成功删除，并刷新列表
                        $.messageBox({
                            'title':$.i18n('collaboration.system.prompt.js'),
                            'type': 0,
                            'msg': $.i18n('collaboration.link.prompt.deletesuccess'),
                            'imgType':0,
                            ok_fn:function() {
                            	unlockWorkflow();
                                var totalNum = grid.p.total - 1;
                                $('#summary').attr("src","listDesc.do?method=listDesc&type="+tableId+"&size="+totalNum);
                                $("#"+tableId).ajaxgridLoad();
                            },
                            close_fn :function(){
                            	unlockWorkflow();
                                var totalNum = grid.p.total - 1;
                                $('#summary').attr("src","listDesc.do?method=listDesc&type="+tableId+"&size="+totalNum);
                                $("#"+tableId).ajaxgridLoad();
                            }
                        });
                    }else{
                        $.alert(flag);
                        unlockWorkflow();
                    }
                },
                error : function(request, settings, e){
                    $.alert(e);
                    unlockWorkflow();
                }

            });
      },
      "cancel_fn":function(){
    	  unlockWorkflow();
      }
  });
}

function addURLPara(obj){
	var _urlPara = window.location.href;
	if(_urlPara.indexOf("&condition=templeteAll&textfield=all") != -1){
		obj.templeteAll="all";
	}

	if(_urlPara.indexOf("condition=templeteCategorys&textfield=") != -1){
		obj.templeteCategorys = $("#bisnissMap").val();
	}
	if(_urlPara.indexOf("condition=templeteIds&textfield=") != -1
			&& window.location.href.indexOf("srcFrom=bizconfig") == -1 ){
		obj.templeteIds = $("#bisnissMap").val();
	}
	if(_urlPara.indexOf("srcFrom=bizconfig&condition=templeteIds&textfield=") != -1){
		obj.templeteIds = $("#frombizconfigIds").val();
	}
	return obj;
}

function editFromWaitSend(grid){
  var rows = grid.grid.getSelectRows();
  var count;
  count= rows.length;
  if(count == 0){
    // 请选择要编辑的事项
    $.alert($.i18n('collaboration.grid.alert.selectEdit'));
    return;
  }
  if(count > 1){
    // 只能选择一项事项进行编辑
    $.alert($.i18n('collaboration.grid.alert.selectOneEdit'));
    return;
  }
  if(count == 1){
    var obj = rows[0];
    var affairId = obj.affairId;
    var summaryId= obj.summaryId;
    var subState = obj.subState;
    if(!$.ctx.resources.contains('F01_newColl') && !obj.templeteId) {
          $.alert($.i18n('collaboration.listWaitSend.noNewCol'));
          return false;
    }
    editCol(affairId,summaryId,subState);
  }
}



function editCol(affairId,summaryId,subState){
    var url = _ctxPath + "/collaboration/collaboration.do?method=newColl&summaryId="+summaryId+"&affairId="+affairId+"&from=waitSend&subState="+subState;
    openCtpWindow({'url':url,'id':affairId});
}

function checkTemplateCanUse(templateId){
  // var colMan = new colManager();
   //var strFlag = colMan.checkTemplateCanUse(templateId);
   var strFlag = callBackendMethod("colManager","checkTemplateCanUse",templateId);
   if(strFlag.flag =='cannot'){
      return false;
   }else{
      return true;
   }
 }

// 待发列表发送
function sendFromWaitSend(grid) {
	startTopPageProcessBar();
  var rows = grid.grid.getSelectRows();
  if(rows.length > 1){
          // 只能选择一项协同进行发送
    $.alert($.i18n('collaboration.grid.alert.selectOneSend'));
    endTopPageProcessBar();
    return;
  }
  if(rows.length < 1){
    $.alert($.i18n('collaboration.grid.alert.selectSend'));
    endTopPageProcessBar();
    return;
  }
  var obj = rows[0];
  var processId = obj.processId;
  var bodyType = obj.bodyType;
  var summaryId =obj.summaryId;
  var orgAccountId = obj.orgAccountId;
  var caseId = obj.caseId;
  var affairId = obj.affairId;
  var deadlineDatetime=obj.processDeadLineName;
  //检查流程期限是不是比当前日期早
  if(typeof(deadlineDatetime)!="undefined"){
    var nowDatetime=new Date();
    if(deadlineDatetime && (nowDatetime.getTime()+server2LocalTime) > new Date(deadlineDatetime.replace(/-/g,"/")).getTime()){
      $.alert($.i18n('collaboration.deadline.sysAlert'));
      endTopPageProcessBar();
      return;
    }
  }
  $("#summaryId").val(summaryId);
  $("#affairId").val(affairId);
  var templeteId = null;
  if(obj.subState != '16'){
    templeteId= obj.templeteId;
  }
  var newflowType = obj.newflowType;
  // 自动触发的新流程 不校验,指定回退不校验
  if(templeteId != null && templeteId != "" && obj.subState != '16' && newflowType != '2'){
    if(!(checkTemplateCanUse(templeteId))){
            $.alert($.i18n('template.cannot.use'));
            endTopPageProcessBar();
            return;
        }
  }
  if(!$.ctx.resources.contains('F01_newColl') && !templeteId){
	   $.alert($.i18n('collaboration.listWaitSend.noNewCol'));
	   endTopPageProcessBar();
	   return;
  }
  try{closeOpenMultyWindow(affairId,false);}catch(e){}
  sendFromWaitSendList(bodyType,processId,templeteId,summaryId,caseId);
}

var _topPage_proce = null;
function startTopPageProcessBar() {
    try {
    	_topPage_proce = getCtpTop().$.progressBar();
    } catch (e) {
    }
}
function endTopPageProcessBar(){
    if (_topPage_proce != null) {
        try {
        	_topPage_proce.close();
        } catch (e) {
        }
    }
}

//待发列表发送 抽取方法
function sendFromWaitSendList(bodyType,processId,templeteId,summaryId,caseId){
    if(bodyType == "20" || bodyType == 20){
          //表单流程请双击进入新建页面进行发送
          $.alert($.i18n('collaboration.grid.alert.dclickSendFrom'));
          endTopPageProcessBar();
          return;
     }
    if(window.matchRequestToken){
        //工作流缓存ID
        var randT = (new Date().getTime());
        window.matchRequestToken =  "PC-" + summaryId + "-t-" + randT;
    }
    //var collManager = new colManager();
   if(templeteId){
         isTemplate = true;
         var callerResponder = new CallerResponder();
         //var sflag = collManager.getTemplateId(templeteId);//根据模板ID去查询出流程的Id,顺便判断权限问题
         var sflag = callBackendMethod("colManager","getTemplateId",templeteId);
         if(sflag.wflag =='cannot'){
             $.alert($.i18n('collaboration.send.fromSend.templeteDelete'));//模板已经被删除，或者您已经没有该模板的使用权限三
             endTopPageProcessBar();
             return;
         }else if(sflag.wflag =='noworkflow'){
             //协同没有流程,不能发送
             $.alert($.i18n('collaboration.send.fromSend.noWrokFlow'));
             endTopPageProcessBar();
             return;
         }
         if(sflag.wflag =='isTextTemplate'){
            if(!processId){
              $.alert($.i18n('collaboration.send.fromSend.noWrokFlow'));
              endTopPageProcessBar();
              return;
            }
            processId = processId;

          /*  preSendOrHandleWorkflow(window, '-1', '',processId,
                    'start', $.ctx.CurrentUser.id, caseId, $.ctx.CurrentUser.loginAccount,
                    '', 'collaboration','', window);
            */


            var options = {
               		tWindow : window,
               		workitemId : "-1",
               		processTemplateId : "",
               		processId : processId,
               		activityId : "start",
               		currentUserId : $.ctx.CurrentUser.id,
               		caseId :caseId,
               		currentAccountId :  $.ctx.CurrentUser.loginAccount,
               		appName : "collaboration"
               }

            preSendOrHandleWorkflow(options);

            endTopPageProcessBar();
         }else{
             processId = sflag.wflag;
            /* preSendOrHandleWorkflow(window, '-1',processId, '',
                   'start',$.ctx.CurrentUser.id, caseId, $.ctx.CurrentUser.loginAccount,
                   '', 'collaboration','', window);
             */

             var options = {
                		tWindow : window,
                		workitemId : "-1",
                		processTemplateId : processId,
                		processId : "",
                		activityId : "start",
                		currentUserId : $.ctx.CurrentUser.id,
                		caseId :caseId,
                		currentAccountId :  $.ctx.CurrentUser.loginAccount,
                		appName : "collaboration"
                }

             preSendOrHandleWorkflow(options);
             endTopPageProcessBar();
         }
      } else if(processId){//这个优先级应更高
	     if( !processId||processId == ""){
	         $.alert($.i18n('collaboration.send.fromSend.noWrokFlow'));
	         endTopPageProcessBar();
	         return;
	       }
	      /* preSendOrHandleWorkflow(window, '-1', '',processId,
	               'start', $.ctx.CurrentUser.id, caseId,$.ctx.CurrentUser.loginAccount,
	               '', 'collaboration','', window);*/

	       var options = {
           		tWindow : window,
           		workitemId : "-1",
           		processTemplateId : "",
           		processId : processId,
           		activityId : "start",
           		currentUserId : $.ctx.CurrentUser.id,
           		caseId :caseId,
           		currentAccountId :  $.ctx.CurrentUser.loginAccount,
           		appName : "collaboration"
           }

           preSendOrHandleWorkflow(options);


	       endTopPageProcessBar();
	   }else{
          var newProcessId = "";
          if(!processId||processId == ""){
              newProcessId = callBackendMethod("colManager","getProcessId",summaryId);
          }
          if(!newProcessId || newProcessId == ""){
              $.alert($.i18n('collaboration.send.fromSend.noWrokFlow'));
              endTopPageProcessBar();
              return;
          } else{
              $.alert($.i18n('collaboration.waitSend.noProcessRefresh')); //请刷新列表后再发送!
              endTopPageProcessBar();
              return;
          }
          /*preSendOrHandleWorkflow(window, '-1', '',processId,
                  'start', $.ctx.CurrentUser.id, caseId, $.ctx.CurrentUser.loginAccount,
                  '', 'collaboration','', window);*/


          var options = {
             		tWindow : window,
             		workitemId : "-1",
             		processTemplateId : "",
             		processId : processId,
             		activityId : "start",
             		currentUserId : $.ctx.CurrentUser.id,
             		caseId :caseId,
             		currentAccountId :  $.ctx.CurrentUser.loginAccount,
             		appName : "collaboration"
             }

             preSendOrHandleWorkflow(options);


          endTopPageProcessBar();
      }
}

$.content.callback.workflowNew = function() {
     $("<input type='hidden' id='workflow_node_peoples_input' name='workflow_node_peoples_input' value='"+$("#workflow_node_peoples_input",window.document)[0].value+"' />").appendTo($("#sendForm"));
     $("<input type='hidden' id='workflow_node_condition_input' name='workflow_node_condition_input' value='"+$("#workflow_node_condition_input",window.document)[0].value+"' />").appendTo($("#sendForm"));
     $("<input type='hidden' id='workflow_newflow_input' name='workflow_newflow_input' value='"+$("#workflow_newflow_input",window.document)[0].value+"' />").appendTo($("#sendForm"));
     $("<input type='hidden' id='toReGo' name='toReGo' value='"+$("#toReGo",window.document)[0].value+"' />").appendTo($("#sendForm"));
     $("#sendForm").attr("action",_ctxPath+"/collaboration/collaboration.do?method=sendImmediate");
     if(parent.bpmMenuFlag == true){
        document.getElementById("bpmMenuFlag").value = "1";
        $("#sendForm").jsonSubmit({
             callback : function(args){
                 var argsJSON = null;
                 if(args && typeof args === 'string'){
                     try{
                         argsJSON = $.parseJSON(args);
                     }catch(e){
                         // 忽略错误...
                     }
                 }
                 if(argsJSON && argsJSON.resourceCode != null && parent.clickMenu){
                     parent.clickMenu(argsJSON.resourceCode); //跳转
                	 setMenuListHighlight4resourceCode(argsJSON.resourceCode);
                 }
             }
         });
     }else{
        $("#sendForm").jsonSubmit();
     }

};




//已发列表 重复发起
function resend(grid) {
  var rows = grid.grid.getSelectRows();
  if(rows.length < 1){
          //请选择要重复发起的协同
    $.alert($.i18n('collaboration.grid.send.selectRepeatCol'));
    return;
  }
  if(rows.length >1){
          //只能选择一项协同进行重复发起
    $.alert($.i18n('collaboration.grid.send.selectOneRepeatCol'));
    return;
  }
    var isNewflow = false;
    var summaryId = null;
      var checkedObj =rows[0];
      summaryId = checkedObj.summaryId;
    if(checkedObj.newflowType && checkedObj.newflowType == '1'){
        //该流程为自动触发的子流程，不能重发
        $.alert($.i18n('collaboration.send.workFlow.notResend'));
        return;
      }
      //MainBodyType 里面20的时候表明是 表单格式正文
  if(checkedObj.bodyType && checkedObj.bodyType =='20'){
          //表单协同不能被重复发起
    $.alert($.i18n('collaboration.send.fromSend.notResend'));
    return;
  }
  //没有新建权限，自由协同不能重复发起
  if(isHaveNewColl != "true" && (checkedObj.templeteId == null || checkedObj.templeteId == "")){
	  $.alert($.i18n('collaboration.send.notNewPolicy.notResend.js'));
	  return;
  }

  if ((checkedObj.parentformSummaryid != null && !checkedObj.canEdit)
		  && checkedObj.bodyType && checkedObj.bodyType !='41' && checkedObj.bodyType !='42'
			  && checkedObj.bodyType !='43' && checkedObj.bodyType!='44' && checkedObj.bodyType!='45' ) {
		  //转发表单不允许重复发起！
		  $.alert($.i18n('collaboration.send.fromSend.forwardFrom'));
	      return;
  }

    url ="collaboration.do?method=newColl&summaryId="+summaryId+"&from=resend";
    openCtpWindow({'url': url});
}



//已发列表编辑流程
function _designWorkflow(grid,callback){
  var editFlowForm = document.getElementsByName('editFlowForm')[0];
  if(!editFlowForm) return;

  var rows  =  grid.grid.getSelectRows();
  if(rows.length < 1){
          //请选择要编辑的协同
        $.alert($.i18n('collaboration.sendGrid.selectColEdit'));
        return;
  }
  if(rows.length >1){
          //只能选择一项协同进行编辑
    $.alert($.i18n('collaboration.sendGrid.selectOneColEdit'));
    return;
  }
  var selObj = rows[0];
  var caseId = selObj.caseId;
  var processId = selObj.processId;
  var deadline = selObj.deadline;
  var advanceRemind = selObj.advanceRemind;
  var templeteId = selObj.templeteId;

  if((selObj.flowFinished || selObj.flowFinished == 'true') || templeteId){
          //该流程已结束或为模板流程不允许修改
    $.alert($.i18n('collaboration.sendGrid.workFlowEndAndTemplate.notEdit'));
    return;
  }
  $("#processId",editFlowForm).val(processId);
  $("#deadline",editFlowForm).val(deadline);
  $("#advanceRemind",editFlowForm).val(advanceRemind);
  //var col = new colManager();
  //var defaultNodeMap =  col.getColDefaultNode($.ctx.CurrentUser.loginAccount);
  var defaultNodeMap =  callBackendMethod("colManager","getColDefaultNode",$.ctx.CurrentUser.loginAccount);

 /* function editWFCDiagramEdocModalDialog(tWindow,caseId,processId,vWindow,appName,isTemplate,
		  flowPermAccountId,defaultPolicyId,defaultPolicyName,callback,alertStyle,myTitle,isHistoryFlag){*/

  var options = {
		  targetWin : window.parent,
		  caseId : caseId,
		  processId : processId,
		  valueWin : window,
		  appName : 'collaboration',
		  isTemplate : false ,
		  flowPermAccountId: $.ctx.CurrentUser.loginAccount,
		  defaultPolicyId : defaultNodeMap.defaultNodeName,
		  defaultPolicyName : defaultNodeMap.defaultNodeLable,
		  scene : 4,
		  buttons :  [{
				"id" : "saveDBOK",
				"callBackFn":callback
			}, {
				"id" : "cancel"
			} ],
		 SPK:"freeFlow",
		 NPS:"default"
  }

  showDiagram(options);
  return;
}

function doubleClick(url,title){
    var parmas = [$('#summary'),$('.slideDownBtn'),$('#listPending')];
    collaborationApi.showSummayDialogByURL(url);
}

//打开正文内容,专门给事件中调用这个方法。
function showSummayToEventDialog(url,title){
    var width = $(getA8Top().document).width() - 60;
    var height = $(getA8Top().document).height() - 50;
    dialogDealColl = $.dialog({
        url: url,
        width: width,
        height: height,
        title: escapeStringToHTML(title),
        targetWindow:getCtpTop()
    });
}


/**
* 获取打开正文详细信息页面的url
* affairId 事项表id
* openFrom 从哪里打开，用来设置是否显示右侧处理区域
*/
function getShowSummaryURL(affairId,openFrom){
    var url = _ctxPath + "/collaboration/collaboration.do?method=summary&openFrom="+openFrom+"&affairId='"+affairId+"'";
    return url;
}



/**
 * 明细日志 弹出对话框
 * showFlag  初始化时 显示的内容 1:显示处理明细 2:显示流程日志 3:显示催办(督办)日志
 */
function showDetailLogDialog(summaryId,processId,showFlag,isHistoryFlag){

    var dialog = $.dialog({
        url : _ctxPath+'/detaillog/detaillog.do?method=showDetailLog&summaryId='+summaryId+'&processId='+processId+"&showFlag="+showFlag+"&isHistoryFlag="+isHistoryFlag,
        //zhou 已发列表中的明细，弹出框调大
        width : 1540,
        height : 780,
        title : $.i18n('collaboration.sendGrid.findAllLog'), //查看明细日志
        targetWindow:getCtpTop(),
        buttons : [{
            text : $.i18n('collaboration.button.close.label'),
            handler : function() {
              dialog.close();
            }
        }]
    });
}

/**
* 预归档
* 模板页面和新建页面都走这里
*/
var _from = "";
var _type = "";
function doPigeonhole_pre(flag, appName, from, type) {
	_from = from;
	_type = type;
    if (flag == "no") {
        //TODO 清空信息
    }
    else if (flag == "new") {
        var result;
        if(from == "templete"){
            result = pigeonhole(appName, null, false, false,'templeteManage', "doPigeonhole_preCallback");
        }else{
            result = pigeonhole(appName,null, "", "", "", "doPigeonhole_preCallback");
        }
    }
}

/**
 * doPigeonhole_pre归档回调
 */
function doPigeonhole_preCallback(result){
    if(_from != "templete"){
    	var theForm = document.getElementsByName("sendForm")[0];
    	if(result == "cancel"){
    		var oldPigeonholeId = theForm.archiveId.value;
    		var selectObj = theForm.colPigeonhole;
    		if(oldPigeonholeId != "" && selectObj.options.length >= 3){
    			selectObj.options[2].selected = true;
    		}
    		else{
    			var oldOption = document.getElementById("defaultOption");
    			oldOption.selected = true;
    		}
    		return;
    	}
    	var pigeonholeData = result.split(",");
    	pigeonholeId = pigeonholeData[0];
    	pigeonholeName = pigeonholeData[1];
    	if(pigeonholeId == "" || pigeonholeId == "failure"){
    		theForm.archiveName.value = "";
    		$.alert($.i18n("collaboration.alert.pigeonhole.failure"));//归档失败
    	}
    	else{
    		var oldPigeonholeId = theForm.archiveId.value;
    		theForm.archiveId.value = pigeonholeId;
    		if(document.getElementById("prevArchiveId")){
    			document.getElementById("prevArchiveId").value = pigeonholeId;
    		}
    		var selectObj = document.getElementById("colPigeonhole");
    		var option = document.createElement("OPTION");
    		option.id = pigeonholeId;
    		option.text = pigeonholeName;
    		option.value = pigeonholeId;
    		option.selected = true;
    		if(oldPigeonholeId == "" && selectObj.options.length<=2){
    			selectObj.options.add(option, selectObj.options.length);
    		}
    		else{
    			selectObj.options[selectObj.options.length-1] = option;
    		}
    	}
    }else{//协同模板页面设置附件预归档
    	if(result != "cancel"){
    		var pigeonholeData = result.split(",");
        	pigeonholeId = pigeonholeData[0];
        	pigeonholeName = pigeonholeData[1];
        	if(_type=="coll"){//协同归档
        		$("#archiveCollPathName").val(pigeonholeName);
                $("#archiveCollPathId").val(pigeonholeId);
        	}else if(_type=="attachment"){//附件归档目录回填
        		$("#attachmentArchiveName").val(pigeonholeName);
                $("#attachmentArchiveId").val(pigeonholeId);
        	}
    	}
    }
}



  //归档
var lockProcessIds = [];
function doPigeonhole(pageType, grid, tableId) {
    var v = $("#"+tableId).formobj({
      gridFilter : function(data, row) {
    	if(row!=null){
    		return $("input:checkbox", row)[0].checked;
    	}else{
    		return true;
    	}
      }
    });
//	  var v =  $();
//	  $("#"+tableId+" :checkbox").each(function(){
//		  if($(this).prop("checked")){
//			  v = v.add($(this));
//		  }
//	  });
//	  if("sent" == pageType){
//		   v = $("#"+tableId).formobj({
//		      gridFilter : function(data, row) {
//		        return $("input:checkbox", row)[0].checked;
//		      }
//		    });
//	  }

    if (v.length === 0) {
      $.alert($.i18n('collaboration.pighole.alert.select'));
      return;
    }
    var ids = new Array();
    var check = false;
    var archiveSubject='';
    var workflowSubject='';
    var superNodeSubject = '';
    var opinionSubject='';
    var processIndex = 0;
    var canContinueExecute = true;
    $(v).each(function(index, elem) {

        var isSuperNode = elem.superNode;
        if(isSuperNode === true){

            check = true;
            superNodeSubject += "<br>"+"&lt;"+escapeStringToHTML(elem.subject)+"&gt;";

        }else if(pageType == "pending"){
            //待办的逻辑(还需要翟峰多查询几个条件)
            if(elem.templeteId){
                //未办理的模板协同不允许直接归档或删除
                check = true;
                archiveSubject += "<br>"+"&lt;"+escapeStringToHTML(elem.subject)+"&gt;";
            }

            //添加提交锁
            var lockWorkflowRe = lockWorkflow(elem.processId, $.ctx.CurrentUser.id, 14);
            if(lockWorkflowRe[0] == "false") {
                check = true;
                if(workflowSubject!=""){
                	workflowSubject += "<br>";
                }
                workflowSubject += lockWorkflowRe[1] + "<br>"+"&lt;"+escapeStringToHTML(elem.subject)+"&gt;";
            } else {
            	lockProcessIds[processIndex++] = elem.processId;
            }


            if(elem.canDeleteORarchive){
                //以下事项要求意见不能为空，不能直接归档或删除
                check = true;
                opinionSubject += "<br>"+"&lt;"+escapeStringToHTML(elem.subject)+"&gt;";
            }
            // 指定回退时不能处理
            var canSubmitWorkFlowRe= canSubmitWorkFlow(elem.workitemId);
            if(canSubmitWorkFlowRe[0]== "false") {
              $.alert(canSubmitWorkFlowRe[1]);
              check = true;
              unlockWorkflow();
              canContinueExecute = false;
              return false;
            }
            if(!isAffairValid(elem.affairId)){
            	check = true;
                unlockWorkflow();
                canContinueExecute = false;
                return false;
            }
        }
        ids.push(elem.affairId);
    });
    if(!canContinueExecute){
    	return;
    }
    if(archiveSubject.length > 1){
    	$.alert($.i18n('collaboration.template.notHandle.notDeleteArchive') + "<br>" + archiveSubject);
    	unlockWorkflow();
    	return;
    }

    if(superNodeSubject.length > 1){
        // 超级节点不允许转发
        $.alert($.i18n('workflow.supernode.msg.not_archive.js') + "<br>" + superNodeSubject);
        unlockWorkflow();
        return;
    }

    if(workflowSubject.length > 1){
    	$.alert(workflowSubject);
    	unlockWorkflow();
    	return;
    }
    if(opinionSubject.length > 1){
    	$.alert($.i18n('collaboration.template.notDeleteArchive.nullOpinion')+"<br>" + opinionSubject);
    	unlockWorkflow();
    	return;
    }
    if(check){
    	unlockWorkflow();
    	return;
    }
    //var cm = new colManager();
    //cm.getPigeonholeRight(ids, callerResponder);
    callBackendMethod("colManager","getPigeonholeRight",ids, {success:function(jsonObj){
    	 if(jsonObj != ""){
       	  $.alert(escapeStringToHTML(jsonObj));
       	  unlockWorkflow();
         }else{
           doPigeonholeCheck(ids, pageType, tableId, grid);
         }
    }});
  }

  function doPigeonholeCol(ids, folder, pageType, tableId, grid){
    //var cm = new colManager();
    for(var i = 0;i<ids.length;i++){
      //cm.transPigeonhole(ids[i], folder[0], pageType);
      callBackendMethod("colManager","transPigeonhole",ids[i], folder[0], pageType);
    }
    //归档成功
    $.infor($.i18n('collaboration.grid.alert.archiveSuccess'));
    //删除重复的文档
    //cm.transPigeonholeDeleteStepBackDoc(ids, folder[0]);
    callBackendMethod("colManager","transPigeonholeDeleteStepBackDoc",ids, folder[0]);
    $("#"+tableId).ajaxgridLoad();
    grid.grid.resizeGridUpDown("down");
    if(tableId === "listPending"){
      $('#summary').attr("src",_ctxPath + "/collaboration/listDesc.do?method=listDesc&type=listPending&size="+grid.p.total);
    }
    //归档后关闭子页面打开的协同
    for (var i = 0 ; i < ids.length;i++) {
        var affairId = ids[i];
        try{closeOpenMultyWindow(affairId);}catch(e){}
    }
  }

  // 检查是否已存在归档协同
  var doPigeonholeCheckCallbackIds = "";
  var doPigeonholeCheckCallbackPageType = "";
  var doPigeonholeCheckCallbackTableId = "";
  var doPigeonholeCheckCallbackGrid = "";
  function doPigeonholeCheck(ids, pageType, tableId, grid){

      doPigeonholeCheckCallbackIds = ids;
      doPigeonholeCheckCallbackPageType = pageType;
      doPigeonholeCheckCallbackTableId = tableId;
      doPigeonholeCheckCallbackGrid = grid;

      var result = pigeonhole(null, null, null, null, null, "doPigeonholeCheckCallback");
  }


    /**
     * doPigeonholeCheck归档回调
     */
    function doPigeonholeCheckCallback(result) {
    	if(result) {
    		if(result != "cancel") {
    			var folder = result.split(",");
                var callerResponder = new CallerResponder();
                callerResponder.success = function(jsonObj) {
                    if (jsonObj === "" || confirm(jsonObj)) {
                        doPigeonholeCol(doPigeonholeCheckCallbackIds, folder,
                                doPigeonholeCheckCallbackPageType,
                                doPigeonholeCheckCallbackTableId,
                                doPigeonholeCheckCallbackGrid);
                        unlockWorkflow();
                    } else {
                    	unlockWorkflow();
                    }
                }
                try {
                	//var cm = new colManager();
                   // cm.getIsSamePigeonhole(doPigeonholeCheckCallbackIds, folder[0], callerResponder);
                    callBackendMethod("colManager","getIsSamePigeonhole",doPigeonholeCheckCallbackIds, folder[0], callerResponder);
                } catch(e) {
                	unlockWorkflow();
                }
    		} else {
    			unlockWorkflow();
    		}
    	} else {
    		unlockWorkflow();
    	}
    }


  //转发协同
  function transmitColFromGrid(grid,listFrom){
      // 需要判断是否只勾选了一项，并给出相应的提示
      var selectBox = grid.grid.getSelectRows();
      if (selectBox.length === 0) {
          //请选择要转发的协同
          $.alert($.i18n('collaboration.grid.alert.transmitCol'));
          return;
      }
      else if(selectBox.length > 20) {
          //只能选择20项协同进行转发
          $.alert($.i18n('collaboration.grid.alert.transmitColOnly20'));
          return;
      }

      var sendDevelop = $.ctp.trigger('beforeTransmitCol',selectBox);
      if(!sendDevelop) {
           return;
      }

      for(var i=0; i<selectBox.length; i++){
         var selectedObj = selectBox[i];
         var app = selectedObj.applicationCategoryKey || selectedObj.app;
         if(app == 19 || app == 20 || app == 21){
             //公文不允许转发协同
             $.alert($.i18n('collaboration.grid.alert.DocumentNotForwardCol'));
             return;
         }else if(app=='32') {
			 //信息报送不允许转发协同
			 $.alert($.i18n('collaboration.grid.alert.InfoNotForwardCol'));
			 return;
		 }else if(app == 81){
             //模板审批不允许转发协同
             $.alert($.i18n('collaboration.grid.alert.InfoNotTemplateApproveColl'));
             return;
         }else if(app == 4){
        	 $.alert($.i18n('govdoc.grid.alert.DocumentNotForwardCol'));
             return;
         }
      }

      var data = [];
      //var cm = new colManager();
      for(var i = 0; i < selectBox.length; i++){
          var summaryId, affairId, isSuperNode;
          if(selectBox[i].summaryId && selectBox[i].affairId){
              summaryId = selectBox[i].summaryId;
              affairId = selectBox[i].affairId;
              isSuperNode = selectBox[i].superNode;
          }
          else{//首页待办更多是Affair对象
              summaryId = selectBox[i].objectId;
              affairId = selectBox[i].id;
          }
          if(listFrom && listFrom=="listWaitSend"){
    		  if(selectBox[i].templeteId){
    			  //var r = cm.checkTemplateCanUse(selectBox[i].templeteId);
    			  var r = callBackendMethod("colManager","isTemplateDeleted",selectBox[i].templeteId);
    			  if(r == "1"){ //被删除
    				 $.alert($.i18n("template.coll.not.ues.js",selectBox[i].subject));
    				 return;
    			  }
    		  }
          }

          if(isSuperNode === true){
              $.alert($.i18n("workflow.supernode.msg.not_transmit.js"));
              return;
          }

          data[i] = {"summaryId": summaryId, "affairId": affairId};
      }

      transmitColById(data,grid);
   }

   function transmitColById(data,grid){
      var checkPermissionData = "";
      var doForwardData = "";
      for(var i = 0; i < data.length; i++){
    	  doForwardData += data[i]["summaryId"] + "_" + data[i]["affairId"] + ",";
          checkPermissionData += data[i]["affairId"] + ",";
      }
      var r = callBackendMethod("colManager","checkForwardPermission",checkPermissionData);
      if(r && (r instanceof Array) && r.length > 0){
          //以下协同不能转发，请重新选择
          $.alert($.i18n('collaboration.grid.alert.thisSelectNotForward')+"<br><br>" + r.join("<br>"));
          return;
      }

      var dialog = $.dialog({
          id : "showForwardDialog",
          height:"400",
          width:"550",
          url : _ctxPath+"/collaboration/collaboration.do?method=showForward&data=" + doForwardData,
          title : $.i18n('collaboration.transmit.col.label'),
          targetWindow:getCtpTop(),
          isClear:false,
          transParams:{
              commentContent : ""
          },
          buttons: [{
              id : "okButton",
              text: $.i18n("collaboration.button.ok.label"),
              btnType:1,
              handler: function () {
                  var rv = dialog.getReturnValue();
              },
              OKFN : function(){
                  dialog.close();
                  try{
                      $("#"+grid.id).ajaxgridLoad();
                      setTimeout(function(){
			            if ("function"==typeof(refreshPendingCenterCount)) {
			           	  	refreshPendingCenterCount(grid.p.total);
			             }
			         },1000);
                  }
                  catch(e){
                  }
              }
          }, {
              id:"cancelButton",
              text: $.i18n("collaboration.button.cancel.label"),
              handler: function () {
                  dialog.close();
              }
          }]
      });
   }

   //ajax判断事项是否可用。
   function isAffairValid(affairId){
     //var pam = new portalAffairManager();
     //var msg = pam.checkAffairValid(affairId);
     var msg = callBackendMethod("portalAffairManager","checkAffairValid",affairId);
     if($.trim(msg) !=''){
          $.alert(msg);
          return false;
     }
     return true;
   }

   function transmitMail(whfrom){
     if("true"==emailShow || emailShow == true){
         //需要判断是否只勾选了一项，并给出相应的提示
         var selectBox =grid.grid.getSelectRows();
         if (selectBox.length === 0) {
             //请选择要转发的协同!
             $.alert($.i18n('collaboration.grid.alert.transmitCol'));
             return;
         } else if(selectBox.length > 1) {
             //只能选择一项协同进行转发
             $.alert($.i18n('collaboration.grid.alert.transmitColOnlyOne'));
             return;
         }
         for(var i=0; i<selectBox.length; i++){
             var selectedObj = selectBox[i];
             var app = selectedObj.applicationCategoryKey || selectedObj.app;
             if(app == "19" || app == "20" || app == "21" || app == "4"){
                 //公文不允许转发协同
                 $.alert($.i18n('collaboration.grid.alert.DocumentNotForwardEmail'));
                 return;
             } else if(app=='32') {
				 //信息报送不允许转发邮件
                 $.alert($.i18n('collaboration.grid.alert.InfoNotForwardEmail'));
                 return;
			 }else if(app == '81'){
                 //协同模板不允许转发邮件
                 $.alert($.i18n('collaboration.grid.alert.InfoNotTemplateApproveEmail'));
                 return;
             }
          }

         var summaryId, affairId, isSuperNode;

         if(selectBox[0].summaryId && selectBox[0].affairId){
           summaryId = selectBox[0].summaryId;
           affairId = selectBox[0].affairId;
           isSuperNode = selectBox[0].superNode;
         }
         else{//首页待办更多是Affair对象
           summaryId = selectBox[0].objectId;
           affairId = selectBox[0].id;
         }

         //var cm = new colManager();
        // var r = cm.checkForwardPermission(summaryId + "_" + affairId);
         var r = callBackendMethod("colManager","checkForwardPermission",affairId);
         if(r && (r instanceof Array) && r.length > 0){
             //以下协同不能转发，请重新选择
             $.alert($.i18n('collaboration.grid.alert.thisSelectNotForward')+"<br><br>" + r.join("<br>"));
             return;
         }

         if(isSuperNode === true){
             $.alert($.i18n("workflow.supernode.msg.not_transmit.js"));
             return;
         }

         if(whfrom == "morepending"){
             parent.parent.mainIframe.location.href =_ctxPath + "/collaboration/collaboration.do?method=forwordMail&summaryId=" +summaryId+"&affairId="+affairId;

         }else{
             //处理自己的相关逻辑
             window.location.href =_ctxPath + "/collaboration/collaboration.do?method=forwordMail&summaryId=" +summaryId+"&affairId="+affairId;
         }
     }
 }
function setTrack(obj){
     var text = $(obj).text();
     var affairId = $(obj).attr("affairId");
     var summaryId = $(obj).attr("summaryId");
     var dialog = $.dialog({
          targetWindow:getCtpTop(),
          id: 'trackDialog',
          url: _ctxPath+'/collaboration/collaboration.do?method=openTrackDetail&objectId='+$(obj).attr("summaryId")+
                 '&affairId='+$(obj).attr("affairId"),
          width: 328,
          height: 100,
          title: $.i18n('collaboration.listDone.traceSettings'),
          buttons: [{
              id : "trackSubmit",
              text: $.i18n('collaboration.pushMessageToMembers.confirm'), //确定
              handler: function () {
                 var returnValue = dialog.getReturnValue();
                 if( returnValue && returnValue != null){//返回值到父页面
                     if(returnValue == '1' || returnValue =='2'){
                         $(obj).text($.i18n('message.yes.js')); //是
                     } else {
                         $(obj).text($.i18n('message.no.js')); //否
                     }
                     try{
                         $("#trackType",window.summaryF.document).val(newTrackType);
                     }catch(e){}
                     dialog.close();
                 }
              }
          }, {
              text: $.i18n('collaboration.pushMessageToMembers.cancel'), //取消
              handler: function () {
                  dialog.close();
              }
          }]
      });
 }
var queryDialog;
var o = new Object();
var advanceObj;
function _getGridHeight(){
   var paddingHeight=12;
  //如果不存在查询bar栏 则不存在12像素的padding差
  if($(".query_menu_bar").get(0)==null){
    paddingHeight=0;
  }

   var pageHeight=$(document.body).height();
//   查询切换menu工具栏
    if($(".query_menu_bar").get(0)!=null&&$(".query_menu_bar").get(0).style.display!="none"){
        //13表示north的上边距
        pageHeight=pageHeight-$(".query_menu_bar").height() - paddingHeight;
    }

    //如果工具栏存在 则需要减去工具栏的高度
    if($("#toolbars").get(0).style.display!="none"){
        pageHeight=pageHeight-$("#toolbars").height();
    }
    //如果查询区域存在 则减去查询区域的高度
    if($(".more_query_area").get(0).style.display!="none"){
        //这里的8 是间距的宽度
        pageHeight=pageHeight-$(".more_query_area").height();
    }
    //由于存在分页条  所以传入的高度 需要减去其中的分页高度
    pageHeight=pageHeight-63;
    return pageHeight;


}


var advanceSearchFlag = false;//当前显示的是不是高级查询
/**
 * 控制高级查询区域
 * @param openFrom  来源
 * @param showAdvanceSearch  展示或隐藏高级查询区域(true:展示;flase:不展示)
 */
function openQueryViews(openFrom, showAdvanceSearch){
	  var paddingHeight=12;
	  //如果不存在查询bar栏 则不存在12像素的padding差
	  if($(".query_menu_bar").get(0)==null){
	    paddingHeight=0;
	  }
	   if(showAdvanceSearch){
	        advanceSearchFlag = true;//高级查询
	        $(".more_query_area").show();
	        if(typeof(isShowStatisticalToolBar) != 'undefined' && isShowStatisticalToolBar=="0"){
	        	layoutObj.setNorth($(".more_query_area").height()+paddingHeight+$("#toolbars").height());
	        }else{
	        	layoutObj.setNorth($(".more_query_area").height()+$(".query_menu_bar").height()+paddingHeight+$("#toolbars").height());
	        }
	        $(".common_search_condition ").hide();
	    }else{
	        advanceSearchFlag = false;//toolbar查询
	        $(".more_query_area").hide();
	        if(typeof(isShowStatisticalToolBar) != 'undefined' && isShowStatisticalToolBar=="0"){
	        	layoutObj.setNorth(paddingHeight+$("#toolbars").height());
	        }else{
	        	layoutObj.setNorth($(".query_menu_bar").height()+paddingHeight+$("#toolbars").height());
	        }

	        $(".common_search_condition ").show();
	     }

	   var relHeight=_getGridHeight();
	    if(grid!=null){
	        $(grid.grid.bDiv).height(relHeight-37);
	    }
	    document.getElementById("subject").value = document.getElementById("title").value;
}

function advanceQuery(openFrom){
	var param = new Object();
	/************公共参数开始***********/
	param.subject = $("#subject").val();
	param.importantLevel = $("#importantLevel").val();
	var createDate = getDates($("#from_createDate").val(),$("#to_createDate").val());
	if(createDate == 'false'){
		return;
	}
	param.createDate = createDate;
	if(openFrom=='listSent' || openFrom=='listDone'){
		param.affairArchiveId = $("#affairArchiveIdTd").val();
	}
	if(openFrom=='listSent' || openFrom=='listDone' || openFrom=='listPending'){
		param.templateName = $("#templateName").val();
	}
	if(_paramTemplateIds){
	    param.templeteIds= _paramTemplateIds;
	}
	var selectVal = $("#deduplication").prop("checked");
    if(selectVal){
        param.deduplication = "true";
    }else{
        param.deduplication = "false";
    }
    var selectVal = $("#aiProcessingRecord").prop("checked");
    if(selectVal){
    	param.aiProcessing = "true";
    }else{
    	param.aiProcessing = "false";
    }

	//判断获取主库数据还是分库数据
    if(typeof(dataType) == "undefined"){
		dataType = "0";
	}

    if(dataType == '1'){
    	param.dumpData = 'true';
    }

	/************公共参数结束***********/
	if(openFrom=='listSent'){
		var deadlineDatetime = getDates($("#from_deadlineDatetime").val(),$("#to_deadlineDatetime").val());
		if(deadlineDatetime == 'false'){
			return;
		}
		param.deadlineDatetime = deadlineDatetime;
		param.workflowState = $("#workflowState").val();
		param.receiver = $("#receiver").val();
		param.isOverdue = $("#isOverdue").val();
		param.state = "2";
		param.type = handoverType;
		param.memberName = $("#memberName").val();
		param.sendeeName = $("#sendeeName").val();
		param.startMemberName = $('#startMemberName').val();
		$("#listSent").ajaxgridLoad(param);
	}else if(openFrom=='listWaitSend'){
		param.subState = $("#subState").val();
		$("#listWaitSend").ajaxgridLoad(param);
	}else if(openFrom=='listDone'){
		var dealDate = getDates($("#from_dealDate").val(),$("#to_dealDate").val());
		if(dealDate == 'false'){
			return;
		}
		param.dealDate = dealDate;
		param.startMemberName = $('#startMemberName').val();
		param.preApproverName = $('#preApproverName').val();
		param.isOverdue = $('#isOverdue').val();
		param.workflowState = $('#workflowState').val();
		param.state = "4";
		param.type = handoverType;
		param.memberName = $("#memberName").val();
		param.sendeeName = $("#sendeeName").val();
		$("#listDone").ajaxgridLoad(param);
	}else if(openFrom=='listPending'){
		param.startMemberName = $('#startMemberName').val();
		param.preApproverName = $('#preApproverName').val();
		var receiveDate = getDates($("#from_receiveDate").val(),$("#to_receiveDate").val());
		if(!receiveDate == 'false'){
			return;
		}
		param.receiveDate = receiveDate;
		var expectprocesstime = getDates($("#from_expectprocesstime").val(),$("#to_expectprocesstime").val());
		if(expectprocesstime == 'false'){
			return;
		}
		param.expectprocesstime = expectprocesstime;
		param.subState = $("#subState").val();
		param.isOverdue = $("#isOverdue").val();
	    var toTab = $(".active_item").attr("id");
	    if(toTab=='overTime'){
	    	param.coverTime="1";
		}else if(toTab=='sevenOverTime'){
			param.sevenDayOverdue="sevenDayOverdue";
		}else if(toTab=='myDepartment'){
			param.myDept="myDept";
		}else if(toTab=='fromleader'){
			param.myLeader="myLeader";
		}
		$("#listPending").ajaxgridLoad(param);
	}
}

function getDates(fromDate,toDate){
	if(fromDate != "" && toDate != "" && fromDate > toDate){
		$.alert($.i18n('collaboration.rule.date'));//开始时间不能早于结束时间
		return 'false';
	}
	var dealDate = fromDate+'#'+toDate;
	if(fromDate=='' && toDate==''){
		dealDate='';
	}else if(fromDate=='' && toDate!=''){
		dealDate = "#"+toDate;
	}else if(fromDate!='' && toDate==''){
		dealDate = fromDate+"#";
	}
	return dealDate;
}

function resetParam(){
	document.getElementById("advanceQueryParam").reset();
}
//二维码
function codeCallback(reval){

	if(reval && reval.openFrom){//如果有数据，新开一个窗口，打开协同
		setTimeout(function(){
			colSum(reval.affairId,reval.openFrom,reval.summaryId,reval.subState);
		},100);
	}else{
		$.alert($.i18n('collaboration.erweima.nodata.js'));
	}
}

function colSum(affairId,openFrom,summaryId,subState){
	if(openFrom=="listWaitSend"){
		editCol(affairId,summaryId,subState);
	}else{
		var	url = _ctxPath + "/collaboration/collaboration.do?method=summary&openFrom="+openFrom+"&affairId="+affairId;
	    doubleClick(url,null);
	    grid.grid.resizeGridUpDown('down');
	}
    //页面底部说明加载
    //$('#summary').attr("src","listDesc.do?method=listDesc&type=listDone&size="+grid.p.total+"&r=" + Math.random());
}

//是否显示归档按钮
function isPigeonholeBtn(){
	var hasResourceCode = ($.ctx.resources.contains('F04_docIndex')
			|| $.ctx.resources.contains('F04_myDocLibIndex')
			|| $.ctx.resources.contains('F04_accDocLibIndex')
    		|| $.ctx.resources.contains('F04_proDocLibIndex')
    		|| $.ctx.resources.contains('F04_eDocLibIndex')
    		|| $.ctx.resources.contains('F04_docLibsConfig'));
	return hasResourceCode;

}

function unlockWorkflow() {
	if(lockProcessIds.length>0) {
		for(var i=0; i<lockProcessIds.length; i++) {
			releaseWorkflowByAction(lockProcessIds[i], $.ctx.CurrentUser.id, 14);
		}
		lockProcessIds.length = 0;
	}
}

function barCodeOpen(){
	if($.browser.chrome||(!!window.ActiveXObject || "ActiveXObject" in window)){
		closeBarCodePort();
		if(openBarCodePort()){
			$.infor($.i18n("common.barcode.ready.label"));
		}
		return;
	}
	$.alert($.i18n('form.barcode.only.suport.ie.lable'));

}

//批量打印
function batchPrint() {
    var id_checkbox = grid.grid.getSelectRows();
    var size = id_checkbox.length;;
    if(size < 1){
        //请选中一个元素在点击打印按钮
        $.alert( $.i18n('collaboration.batch.print.checkinfo'));
        return;
    }
    var affairIds = "", subjects = "", noSupportCount = 0;
    for(var i = 0 ; i<size;i++){
        var selRow = id_checkbox[i];
        var bodyType = selRow.bodyType;
        if(bodyType == "10" || bodyType == "20"){
        	affairIds += selRow.affairId + ",";
        }else{
        	noSupportCount++;
        	subjects += selRow.subject + ",";
        }
    }
    if(noSupportCount != 0){
    	subjects = subjects.substr(0,subjects.length-1);
    	alert($.i18n("collaboration.batch.print.alert.js",size,noSupportCount,subjects));
    }
    //所有都不支持，不打开打印页面
    if(noSupportCount == size){
    	return;
    }
    affairIds = affairIds.substr(0,affairIds.length-1);
    openCtpWindow({'url':_ctxPath+"/collaboration/collaboration.do?method=batchPrintPdf&affairIds="+affairIds+"&type="+paramMethod+"&openFrom=listPending"});
}
