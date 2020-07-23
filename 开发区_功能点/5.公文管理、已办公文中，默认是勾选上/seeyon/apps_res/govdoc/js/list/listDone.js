/**
 * 页面加载初始化
 */
$(document).ready(function() {
	loadUE();
	loadToolbar();
	loadSearch();
	loadData();
});

/**
 * 加载页面按钮
 */
function loadToolbar() {
	var toolbarArray = new Array();

	if(listType == 'listExchangeFallback') {
	} else if(listType == 'listExchangeSendDone') {
		toolbarArray = loadExchangeSendToolbar(toolbarArray);
	} else if(listType == 'listExchangeSignDone') {
		toolbarArray = loadExchangeSignToolbar(toolbarArray);
	} else {
		// 归档
		if (isShowPigeonhole()) {
			toolbarArray.push({
				id : "pigeonhole",
				name : $.i18n('collaboration.toolbar.pigeonhole.label'),
				className : "ico16 filing_16",
				click : function() {
					doPigeonhole("govdocdone", grid, "listDone");
				}
			});
		}
		// 删除
		toolbarArray.push({
			id : "delete",
			name : $.i18n('collaboration.button.delete.label'),
			className : "ico16 del_16",
			click : function() {
				doDelete('finish', 'listDone','listDone');
			}
		});
		// 取回
		if (listType != "listFinished") {
			toolbarArray.push({
				id : "takeBack",
				name : $.i18n('common.toolbar.takeBack.label'),
				className : "ico16 retrieve_16",
				click : doTakeBack
			});
		}

		loadTransSendGovdocAndCollToolbar(toolbarArray);
		if(govdocType.indexOf("2") > -1 || listType == 'listDoneAllRoot' || listType == 'listDoneRoot'
			|| listType == 'listFinishedRoot'){
			//转办
			toolbarArray.push({
				id : "turnToRec",
				name : $.i18n('govdoc.button.turnToRec.label'),
				className : "ico16 forward_event_16",
				click : turnToRec
			});
		}
		//转公告
		toolbarArray.push({
			id : "transmitBulletin",
			name : $.i18n('govdoc.button.transmitBulletin.label'),
			className : "ico16 transfer_bulletin_16",
			click : dealTransmitBulletinFunc
		});
		// 同一流程只显示最后一条
		toolbarArray.push({
			id : "deduplication",
			type : "checkbox",
			//zhou
			checked : true,
			text : $.i18n('collaboration.portal.listDone.isDeduplication'),
			value : "1",
			click : doDeduplication
		});

	    if(hasDumpData == "true"){
	    	//当前数据
	    	toolbarArray.push({
	    		id: "currentData",
	    		name: $.i18n('govdoc.button.currentData.label.js'),
	    		className:"ico16 view_switch_16",
	    		click:currentData
	    	});
	    	//转储数据
	    	toolbarArray.push({
	    		id: "dumpData",
	    		name: $.i18n('govdoc.button.dumpData.label.js'),
	    		className:"ico16 view_switch_16",
	    		click:dumpData
	    	});
	    }
	}


	// toolbar扩展
	for (var i = 0; i < addinMenus.length; i++) {
		toolbarArray.push(addinMenus[i]);
	}
	// 工具栏
	toolbar = $("#toolbars").toolbar({
		toolbar : toolbarArray
	});

    if(hasDumpData == "true"){
	    //设置按钮样式
	    document.getElementById("currentData_a").style.display = "none";
    }

    toolbar.selected(listType);
}


function loadExchangeSendToolbar(toolbarArray) {
	toolbarArray.push({
		id : "listExchangeSendPending",
		name : $.i18n('govdoc.exchange.toolbar.sendListPending.label'),
		className : "ico16 forwardText_16",
		click : listExchangeSendPending
	});
	toolbarArray.push({
		id : "listExchangeSendDone",
		name : $.i18n('govdoc.exchange.toolbar.sendListDone.label'),
		className : "ico16 forwardText_16",
		click : listExchangeSendDone
	});
	return toolbarArray;
}

function loadExchangeSignToolbar(toolbarArray) {
	toolbarArray.push({
		id : "listExchangeSignPending",
		name : $.i18n('govdoc.exchange.toolbar.signListPending.label'),
		className : "ico16 forwardText_16",
		click : listExchangeSignPending
	});
	toolbarArray.push({
		id : "listExchangeSignDone",
		name : $.i18n('govdoc.exchange.toolbar.signListDone.label'),
		className : "ico16 forwardText_16",
		click : listExchangeSignDone
	});
	return toolbarArray;
}

/**
 * 加载列表小查询
 */
function loadSearch() {
	var conditionArray = new Array();
	conditionArray.push({// 标题
		id : 'subject',
		name : 'subject',
		type : 'input',
		text : $.i18n("cannel.display.column.subject.label"),
		value : 'subject',
		maxLength : 100
	});
	conditionArray.push({// 公文文号
		id : 'docMark',
		name : 'docMark',
		type : 'input',
		text : $.i18n("govdoc.docMark.label"),
		value : 'docMark',
		maxLength : 100
	});
	if(listType == "listExchangeFallback") {
		conditionArray.push({// 发文单位
			id : 'exchangeSendUnitName',
			name : 'exchangeSendUnitName',
			type : 'input',
			text : $.i18n("govdoc.exchange.sendAccount.label"),
			value : 'exchangeSendUnitName',
			maxLength : 100
		});
	} else {
		conditionArray.push({// 内部文号
			id : 'serialNo',
			name : 'serialNo',
			type : 'input',
			text : $.i18n("govdoc.serialNo.label"),
			value : 'serialNo',
			maxLength : 100
		});
		conditionArray.push({// 公文归档
			id : 'hasArchive',
			name : 'hasArchive',
			type : 'select',
			text : $.i18n("govdoc.list.hasArchive.label"),
			value : 'hasArchive',
			items : hasArchiveItems
		});
		conditionArray.push({// 发起人
			id : 'startUserName',
			name : 'startUserName',
			type : 'input',
			text : $.i18n("cannel.display.column.sendUser.label"),
			value : 'startUserName'
		});
		conditionArray.push({// 发起时间
			id : 'startTime',
			name : 'startTime',
			type : 'datemulti',
			text : $.i18n("common.date.sendtime.label"),
			value : 'startTime',
			ifFormat : '%Y-%m-%d',
			dateTime : false
		});
		conditionArray.push({// 密级
			id : 'secretLevel',
			name : 'secretLevel',
			type : 'select',
			text : $.i18n("govdoc.secretLevel.label"),
			value : 'secretLevel',
			items : secretLevelOptions
		});
		conditionArray.push({// 紧急程度
			id : 'urgentLevel',
			name : 'urgentLevel',
			type : 'select',
			text : $.i18n("govdoc.urgentLevel.label"),
			value : 'urgentLevel',
			items : urgentLevelOptions
		});
		conditionArray.push({// 处理时间
			id : 'completeTime',
			name : 'completeTime',
			type : 'datemulti',
			text : $.i18n("common.date.donedate.label"),
			value : 'completeTime',
			ifFormat : '%Y-%m-%d',
			dateTime : false
		});
		if (listType != 'listDone') {
			var flowStateItems = new Array();

			flowStateItems.push({text : $.i18n("collaboration.unend"), value : '0' });// 已终止
			flowStateItems.push({text : $.i18n("collaboration.ended"), value : '3' });// 已结束
			flowStateItems.push({text : $.i18n("collaboration.terminated"), value : '1' });// 已终止
			conditionArray.push({// 流程状态
				id : 'flowState',
				name : 'flowState',
				type : 'select',
				text : $.i18n("common.flow.state.label"),
				value : 'flowState',
				items : flowStateItems
			});
		}
	}
	searchobj = $.searchCondition({
		top : topSearchSize,
		right : 50,
		searchHandler : function() {
			var o = getConditionObj();
			if(o) {
				searchByCondition(o);
			}
		},
		conditions : conditionArray
	});
}

function getConditionObj() {
	var o = new Object();
	o.listType = listType;
	o.govdocType = govdocType;
	o.configId = configId;
	o.templateIds = templateIds;

	//同一流程只显示最后一条
	o.deduplication = "true";
	var isDedupCheck =  $("#deduplication").attr("checked");
    if (!isDedupCheck) {
        o.deduplication = "false";
    }
	var choose = $('#' + searchobj.p.id).find("option:selected").val();
	if (choose === 'subject') {//标题
		o.subject = $('#subject').val();
	} else if (choose == 'docMark') {// 公文文号
		o.docMark = $('#docMark').val();
	} else if (choose == 'serialNo') {// 内部文号
		o.serialNo = $('#serialNo').val();
	} else if (choose == 'secretLevel') {// 密级
		o.secretLevel = $('#secretLevel').val();
	} else if (choose == 'urgentLevel') {// 紧急程度
		o.urgentLevel = $('#urgentLevel').val();
	} else if (choose === 'startUserName') {//发起人
		o.startUserName = $('#startUserName').val();
	} else if (choose === 'startTime') {//发起时间
		if(!checkTime("startTime", o, false)) {
	    	return;
	    }
	} else if (choose === 'completeTime') {//处理时间
		if(!checkTime("completeTime", o, false)) {
	    	return;
	    }
	} else if (choose === 'flowState') {//流程状态
		o.flowState = $('#flowState').val();
	} else if (choose === 'exchangeSendUnitName') {//流程状态
		o.exchangeSendUnitName = $('#exchangeSendUnitName').val();
	}else if(choose === 'hasArchive'){//公文归档
		o.hasArchive = $('#hasArchive').val();;
	}
	o.condition = "choose";
    o.dumpData = dataType;
	return o;
}

/**
 * 列表数据加载
 */
function loadData() {
	var colModels;
	if(listType == "listExchangeSendDone") {
		colModels = getSendListColumns();
	} else if(listType == "listExchangeSignDone") {
		colModels = getSignListColumns();
	} else if(listType == "listExchangeFallback") {
		colModels = getFallbackListColumns();
	} else {
		colModels = getListColumns();
	}
	// 表格加载
	grid = $('#listDone').ajaxgrid({
		colModel : colModels,
		click : dbclickRow,
		render : rend,
		height : 200,
		showTableToggleBtn : true,
		parentId : 'center',
		vChange : false,
		vChangeParam : {
			overflow : "hidden",
			autoResize : false
		},
		resizable: false,
		isHaveIframe : false,
		slideToggleBtn : false,
		managerName : "govdocListManager",
		managerMethod : "findDoneList"
	});
}

function getListColumns() {
	var colModels = new Array();
	colModels.push({
		display : 'id',
		name : 'affairId',
		width : '4%',
		type : 'checkbox',
		isToggleHideShow : false
	});
	if(govdocType=="") {//收文管理
		colModels.push({// 分类
			display : $.i18n("govdoc.classified.label"),
			name : 'govdocType',
			sortable : true,
			width : '5%'
		});
	}
	colModels.push({// 标题
		display : $.i18n("common.subject.label"),
		name : 'subject',
		sortable : true,
		width : '30%'
	});
	colModels.push({// 公文文号
		display : $.i18n("govdoc.docMark.label"),
		name : 'docMark',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 内部文号
		display : $.i18n("govdoc.serialNo.label"),
		name : 'serialNo',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 公文归档
		display : $.i18n("govdoc.list.hasArchive.label"),
		name : 'hasArchive',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 紧急程度
		display : $.i18n("govdoc.urgentLevel.label"),
		name : 'urgentLevel',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 密级
		display : $.i18n("govdoc.secretLevel.label"),
		name : 'secretLevel',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 处理时间
		display : $.i18n("govdoc.processingtime.label"),
		name : 'affairCompleteTime',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 当前待办人
		display : $.i18n("govdoc.currentNodesInfo.label"),
		name : 'currentNodesInfo',
		sortable : true,
		width : '10%'
	});
	if(govdocType == "" || govdocType == "1") {
		colModels.push({//分送状态
			display : $.i18n("govdoc.DistributeState.label"),
			name : 'govdocExchangeMainId',
			width : '9%'
		});
	}
	colModels.push({//发起时间
		display : $.i18n("common.date.sendtime.label"),
		name : 'startTime',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 发起人
		display : $.i18n("cannel.display.column.sendUser.label"),
		name : 'startUserName',
		sortable : true,
		width : '7%'
	});
	colModels.push({// 催办次数
		display : $.i18n("collaboration.col.hasten.number.label"),
		name : 'affairHastenTimes',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 跟踪状态
		display: $.i18n("govdoc.canTrack.label"),
		name: 'affairTrack',
		sortable : true,
		width: '9%'
	});
	colModels.push({// 处理期限（节点期限）
		display: $.i18n("pending.deadlineDate.label"),
		name: 'affairDeadLineName',
		sortable : true,
		width: '9%'
	});
	colModels.push({// 流程日志
		display : $.i18n("processLog.list.title.label"),
		name : 'processId',
		width : '9%'
	});
	return colModels;
}

function getSendListColumns() {
	var colModels = new Array();
	colModels.push({// 标题
		display : $.i18n("common.subject.label"),
		name : 'subject',
		sortable : true,
		width : '30%'
	});
	colModels.push({// 公文文号
		display : $.i18n("govdoc.docMark.label"),
		name : 'docMark',
		sortable : true,
		width : '9%'
	});
	// 内部文号
	colModels.push({
		display : $.i18n("govdoc.serialNo.label"),
		name : 'serialNo',
		sortable : true,
		width : '9%'
	});
	// 公文归档
	colModels.push({
		display : $.i18n("govdoc.list.hasArchive.label"),
		name : 'hasArchive',
		sortable : true,
		width : '9%'
	});
	// 紧急程度
	colModels.push({
		display : $.i18n("govdoc.urgentLevel.label"),
		name : 'urgentLevel',
		sortable : true,
		width : '9%'
	});
	// 密级
	colModels.push({
		display : $.i18n("govdoc.secretLevel.label"),
		name : 'secretLevel',
		sortable : true,
		width : '9%'
	});
	// 处理时间
	colModels.push({
		display : $.i18n("govdoc.processingtime.label"),
		name : 'affairCompleteTime',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 当前待办人
		display : $.i18n("govdoc.currentNodesInfo.label"),
		name : 'currentNodesInfo',
		sortable : true,
		width : '10%'
	});
	if(govdocType == "" || govdocType == "1") {
		colModels.push({//分送状态
			display : $.i18n("govdoc.DistributeState.label"),
			name : 'govdocExchangeMainId',
			width : '9%'
		});
	}
	colModels.push({//发起时间
		display : $.i18n("common.date.sendtime.label"),
		name : 'startTime',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 发起人
		display : $.i18n("cannel.display.column.sendUser.label"),
		name : 'startUserName',
		sortable : true,
		width : '7%'
	});
	colModels.push({// 跟踪状态
		display: $.i18n("govdoc.canTrack.label"),
		name: 'affairTrack',
		sortable : true,
		width: '9%'
	});
	colModels.push({// 处理期限（节点期限）
		display: $.i18n("pending.deadlineDate.label"),
		name: 'affairDeadLineName',
		sortable : true,
		width: '9%'
	});
	// 流程日志
	colModels.push({
		display : $.i18n("processLog.list.title.label"),
		name : 'processId',
		width : '9%'
	});
	return colModels;
}

function getSignListColumns() {
	var colModels = new Array();
	colModels.push({// 标题
		display : $.i18n("common.subject.label"),
		name : 'subject',
		sortable : true,
		width : '30%'
	});
	colModels.push({// 公文文号
		display : $.i18n("govdoc.docMark.label"),
		name : 'docMark',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 发文单位
		display : $.i18n("govdoc.exchange.sendAccount.label"),
		name : 'sendUnit',
		sortable : true,
		width : '20%'
	});
	colModels.push({// 签收人
		display: $.i18n("govdoc.exchange.recieveUser.label"),
        name: 'signPerson',
		sortable : true,
		width : '15%'
	});
	colModels.push({// 签收时间
		display: $.i18n("govdoc.exchange.recieveDate.label"),
        name: 'receiptDate',
		sortable : true,
		width : '15%'
	});
	colModels.push({// 公文归档
		display : $.i18n("govdoc.list.hasArchive.label"),
		name : 'hasArchive',
		sortable : true,
		width : '9%'
	});
	return colModels;
}

function getFallbackListColumns(){
	var colModels = new Array();
	colModels.push({// 公文文号
		display : $.i18n("govdoc.exchange.docMark.label"),
		name : 'docMark',
		sortable : true,
		width : '15%'
	});
	colModels.push({// 退文时间
		display : $.i18n("govdoc.exchange.bakcTime.label"),
		name : 'completeTime',
		sortable : true,
		width : '12%'
	});
	colModels.push({// 标题
		display : $.i18n("govdoc.exchange.subject.label"),
		name : 'subject',
		sortable : true,
		width : '35%'
	});
	colModels.push({// 发文单位
		display : $.i18n("govdoc.exchange.sendAccount.label"),
		name : 'sendUnit',
		sortable : true,
		width : '14%'
	});
	colModels.push({// 接收单位/部门
		display : $.i18n("govdoc.exchange.recAccount.label"),
		name : 'exchangeRecUnitName',
		sortable : true,
		width : '12%'
	});
	colModels.push({// 退文原因
		display : $.i18n("govdoc.exchange.backOpinion.label"),
		name : 'backOpinion',
		sortable : true,
		width : '25%'
	});
	return colModels;
}

/**
 * 重新加载列表
 */
function searchByCondition(o) {
	o.dumpData = dataType;
    $("#listDone").ajaxgridLoad(o);
}

/**
 * 列表数据回填
 * @param txt
 * @param data
 * @param r
 * @param c
 * @param col
 * @returns
 */
function rend(txt, data, r, c, col) {
	if (null == txt) {// 修正 如果是未讀，那麼會顯示null字符串
		txt = "";
	}
	if (col.name === "govdocType") {//分类
		return getListGovdocTypeName(data.govdocType, data.affairSubApp);
	} else if (col.name === "subject") {//标题
		return getListSubjectTxt(txt, data, true, true, true, true, true, true, true);
	} else if (col.name === "govdocExchangeMainId") {//分送状态
		return getListDistributeStateTxt(data);
	} else if (col.name === "currentNodesInfo") {//当前待办人
		return getCurrentNodesInfoTxt(txt, data);
	} else if (col.name === "affairTrack") {//跟踪状态
		return getListTrackTxt(txt, data, false);
	} else if (col.name === "processId") {//流程日志
		return getListProcessIdTxt(data);
	}else if(col.name == "affairHastenTimes") {//催办次数
		if(txt == "") {
			txt = "0";
		}
	}else if(col.name == "hasArchive"){//公文归档
		if(txt == true){
			txt = $.i18n("govdoc.list.hasArchive.yes");
		}else{
			txt = $.i18n("govdoc.list.hasArchive.no");
		}
	}
	return txt;
}

// 双击事件
function dbclickRow(data, rowIndex, colIndex) {
	if (colIndex == 7 || colIndex == 8 || colIndex == 12 || colIndex == 14
			|| colIndex == 15) {
		return;
	}

	if (!isAffairValid(data.affairId)) {
		reloadListGrid();
		return;
	}
	var openFrom = "listDone";
	var _url;
	if (listType == "listExchangeFallback") {
		openFrom = "exchangeFallback";
		_url = _ctxPath + "/govdoc/govdoc.do?method=summary&summaryId="
				+ data.summaryId + "&openFrom=formQuery";
	} else {
		_url = getListClickUrl(data, openFrom);
		if (!_url) {
			return;
		}
	}

	var _title = data.subject;
	openGovdocDialog(_url, escapeStringToHTML(_title));
	grid.grid.resizeGridUpDown('down');
}
function listExchangeSendDone(){
	window.location.href = _ctxPath + "/govdoc/list.do?method=listExchange&listType=listExchangeSendDone";
}

function listExchangeSendPending(){
	window.location.href = _ctxPath + "/govdoc/list.do?method=listExchange&listType=listExchangeSendPending";
}

function listExchangeSignDone(){
	window.location.href = _ctxPath + "/govdoc/list.do?method=listExchange&listType=listExchangeSignDone";
}

function listExchangeSignPending(){
	window.location.href = _ctxPath + "/govdoc/list.do?method=listExchange&listType=listExchangeSignPending";
}

var dataType;//数据类型，0：当前数据；1：转储数据
//展示主库数据
function currentData(){
	//放开按钮
	toolbar.enabled("pigeonhole");
	toolbar.enabled("delete");
	toolbar.enabled("takeBack");
	toolbar.enabled("turnToRec");
	toolbar.enabled("transmitBulletin");
	toolbar.enabled("forwardCol");

	document.getElementById("deduplication").disabled = "";
    document.getElementById("deduplication").parentNode.style.opacity = "";

	document.getElementById("currentData_a").style.display = "none";
	document.getElementById("dumpData_a").style.display = "";

	//控制是否展示无法查询的条件
	var objs = $('a[value=startUserName]');
	if(objs != null){
		for(var i = 0 ; i < objs.length ; i++){
			objs[i].style.display = '';
		}
	}

	dataType = '0'; //当前数据

	$("#listDone").ajaxgridLoad(getConditionObj());
}
//展示转储数据
function dumpData(){
	//置灰按钮
	toolbar.disabled("pigeonhole");
	toolbar.disabled("delete");
	toolbar.disabled("takeBack");
	toolbar.disabled("turnToRec");
	toolbar.disabled("transmitBulletin");
	toolbar.disabled("forwardCol");

	document.getElementById("deduplication").disabled = "disabled";
	document.getElementById("deduplication").checked = false;
    document.getElementById("deduplication").parentNode.style.opacity = "0.5";

	document.getElementById("dumpData_a").style.display = "none";
	document.getElementById("currentData_a").style.display = "";

	//控制是否展示无法查询的条件
	var objs = $('a[value=startUserName]');
	if(objs != null){
		for(var i = 0 ; i < objs.length ; i++){
			objs[i].style.display = 'none';
		}
	}

	dataType = '1'; //转储数据

	$("#listDone").ajaxgridLoad(getConditionObj());
}
