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
	if(listType == 'listExchangeSendPending') {
		toolbarArray = loadExchangeSendToolbar(toolbarArray).reverse();
	} else if(listType == 'listExchangeSignPending') {
		toolbarArray = loadExchangeSignToolbar(toolbarArray);
	} else {
		if(govdocType=="" || govdocType.indexOf("2") > -1){
			if (isShowPigeonhole()) {
				toolbarArray.push({//转发文
					id : "forwardText",
					name : $.i18n('govdoc.button.forwardText.label'),
					className : "ico16 forwarding_16",
					click : doForward
				});
			}
		}
	}
	// toolbar扩展
	for (var i = 0; i < addinMenus.length; i++) {
		toolbarArray.push(addinMenus[i]);
	}
	// 工具栏
	var myBar = $("#toolbars").toolbar({
		toolbar : toolbarArray
	});
	myBar.selected(listType);
}

function loadExchangeSendToolbar(toolbarArray) {
	toolbarArray.push({
		id : "listExchangeSendPending",
		name : $.i18n('govdoc.exchange.toolbar.sendListPending.label'),
		className : "ico16 online1 forwardText_16",
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
	// 标题
	conditionArray.push({
		id : 'subject',
		name : 'subject',
		type : 'input',
		text : $.i18n("cannel.display.column.subject.label"),
		value : 'subject',
		maxLength : 100
	});
	// 公文文号
	conditionArray.push({
		id : 'docMark',
		name : 'docMark',
		type : 'input',
		text : $.i18n("govdoc.docMark.label"),
		value : 'docMark',
		maxLength : 100
	});
	// 内部文号
	conditionArray.push({
		id : 'serialNo',
		name : 'serialNo',
		type : 'input',
		text : $.i18n("govdoc.serialNo.label"),
		value : 'serialNo',
		maxLength : 100
	});
	// 发起人
	conditionArray.push({
		id : 'startUserName',
		name : 'startUserName',
		type : 'input',
		text : $.i18n("cannel.display.column.sendUser.label"),
		value : 'startUserName'
	});
	// 发起时间
	conditionArray.push({
		id : 'startTime',
		name : 'startTime',
		type : 'datemulti',
		text : $.i18n("common.date.sendtime.label"),
		value : 'startTime',
		ifFormat : '%Y-%m-%d',
		dateTime : false
	});
	// 接收时间
	conditionArray.push({
		id : 'receiveTime',
		name : 'receiveTime',
		type : 'datemulti',
		text : $.i18n("cannel.display.column.receiveTime.label"),
		value : 'receiveTime',
		ifFormat : '%Y-%m-%d',
		dateTime : false
	});
	// 密级
	conditionArray.push({
		id : 'secretLevel',
		name : 'secretLevel',
		type : 'select',
		text : $.i18n("govdoc.secretLevel.label"),
		value : 'secretLevel',
		items : secretLevelOptions
	});
	// 紧急程度
	conditionArray.push({
		id : 'urgentLevel',
		name : 'urgentLevel',
		type : 'select',
		text : $.i18n("govdoc.urgentLevel.label"),
		value : 'urgentLevel',
		items : urgentLevelOptions
	});
	if(listType != "listExchangeSignPending"){
		// 处理期限
		conditionArray.push({
			id : 'affairExpectedProcessTime',
			name : 'affairExpectedProcessTime',
			type : 'datemulti',
			text : $.i18n("collaboration.process.label"),
			value : 'affairExpectedProcessTime',
			ifFormat : '%Y-%m-%d',
			dateTime : false
		});
		// 处理状态
		var dealStateItems = new Array();
		dealStateItems.push({
			text : $.i18n("collaboration.toolTip.label11"),
			value : '11'
		});// 未读
		dealStateItems.push({
			text : $.i18n("collaboration.toolTip.label12"),
			value : '12'
		});// 已读
		dealStateItems.push({
			text : $.i18n("collaboration.dealAttitude.temporaryAbeyance"),
			value : '13'
		});// 暂存待办
		dealStateItems.push({
			text : $.i18n("collaboration.toolTip.label16"),
			value : '2'
		});// 被回退
		dealStateItems.push({
			text : $.i18n("collaboration.default.stepBack"),
			value : '15'
		});// 指定回退
		conditionArray.push({
			id : 'dealState',
			name : 'dealState',
			type : 'select',
			text : $.i18n("collaboration.trans.label"),
			value : 'dealState',
			ifFormat : '%Y-%m-%d',
			dateTime : false,
			items : dealStateItems
		});
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
	o.govdocType = govdocType;
	o.listType = listType;
	o.configId = configId;
	o.templateIds = templateIds;

	var choose = $('#' + searchobj.p.id).find("option:selected").val();
	if (choose == 'docMark') {// 公文文号
		o.docMark = $('#docMark').val();
	} else if (choose == 'serialNo') {// 内部文号
		o.serialNo = $('#serialNo').val();
	} else if (choose == 'secretLevel') {// 密级
		o.secretLevel = $('#secretLevel').val();
	} else if (choose == 'urgentLevel') {// 紧急程度
		o.urgentLevel = $('#urgentLevel').val();
	} else if (choose === 'subject') {// 标题
		o.subject = $('#subject').val();
	} else if (choose === 'importantLevel') {// 重要程度
		o.importantLevel = $('#importent').val();
	} else if (choose === 'startUserName') {// 发起人
		o.startUserName = $('#startUserName').val();
	} else if (choose === 'startTime') {// 发起时间
		if(!checkTime("startTime", o, false)) {//发起日期
	    	return;
	    }
	} else if (choose === 'receiveTime') {//接收时间
		if(!checkTime("receiveTime", o, false)) {//发起日期
	    	return;
	    }
	} else if (choose === 'affairExpectedProcessTime') {// 处理期限
		if(!checkTime("affairExpectedProcessTime", o, false)) {//发起日期
	    	return;
	    }
	} else if (choose === 'dealState') {// 处理状态
		o.dealState = $('#dealState').val();
	}

	o.condition = "choose";
	return o;
}

/**
 * 列表数据加载
 */
function loadData() {
	var colModels;
	if(listType == "listExchangeSendPending") {
		colModels = getSendListColumns();
	} else if(listType == "listExchangeSignPending") {
		colModels = getSignListColumns();
	} else {
		colModels = getListColumns();
	}
	grid = $('#listPending').ajaxgrid({
		colModel : colModels,
		click : dbclickRow,
		render : rend,
		height : 200,
		showTableToggleBtn : true,
		parentId : $('.layout_center').eq(0).attr('id'),
		vChange : false,
		vChangeParam : {
			overflow : "hidden",
			autoResize : false
		},
		resizable: false,
		isHaveIframe : false,
		slideToggleBtn : false,
		managerName : "govdocListManager",
		managerMethod : "findPendingList"
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
	//if(govdocType=="" || (govdocType.indexOf("2") > -1 && govdocType.indexOf("4") > -1)) {//收文管理
	if(govdocType=="") {
		colModels.push({// 分类
			display : "分类",
			name : 'govdocType',
			sortable : true,
			width : '5%',
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
	colModels.push({// 发起时间
		display : $.i18n("common.date.sendtime.label"),
		name : 'startTime',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 办理剩余时间
		display : $.i18n("govdoc.remainingTime.label"),
		name : 'surplusTime',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 处理期限（节点期限）
		display : $.i18n("pending.deadlineDate.label"),
		name : 'affairDeadLineName',
		sortable : true,
		width : '9%'
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
	// 流程日志
	colModels.push({
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
	colModels.push({// 内部文号
		display : $.i18n("govdoc.serialNo.label"),
		name : 'serialNo',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 上一步处理人
		display: $.i18n("govdoc.preUserName.label"),
		name: 'preUserName',
		sortable : true,
		width: '9%'
	});
//	colModels.push({// 上一步操作
//		display: $.i18n("govdoc.preNodePolicyName.label"),
//		name: 'preNodePolicyName',
//		sortable : true,
//		width: '9%'
//	});
//	colModels.push({// 最近处理时间
//		display: $.i18n("govdoc.preTime.label"),
//		name: 'preTime',
//		sortable : true,
//		width: '9%'
//	});
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
	colModels.push({// 发起时间
		display : $.i18n("common.date.sendtime.label"),
		name : 'startTime',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 办理剩余时间
		display : $.i18n("govdoc.remainingTime.label"),
		name : 'surplusTime',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 处理期限（节点期限）
		display : $.i18n("pending.deadlineDate.label"),
		name : 'affairDeadLineName',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 发起人
		display : $.i18n("cannel.display.column.sendUser.label"),
		name : 'startUserName',
		sortable : true,
		width : '7%'
	});
	colModels.push({// 流程日志
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
	colModels.push({// 紧急程度
		display : $.i18n("govdoc.urgentLevel.label"),
		name : 'urgentLevel',
		sortable : true,
		width : '9%'
	});
	colModels.push({// 发文单位
		display : $.i18n("govdoc.exchange.sendAccount.label"),
		name : 'sendUnit',
		sortable : true,
		width : '15%'
	});
	colModels.push({// 签发人
		display : $.i18n('edoc.element.issuer'),
		name : 'issuer',
		sortable : true,
		width : '10%'
	});
	colModels.push({// 签发时间
		display : $.i18n('edoc.element.sendingdate'),
		name : 'signingDate',
		width : '10%',
		sortable : true,
		sortType : "date",
		dataType : "date",
		width : '10%'
	});
	colModels.push({// 主送单位
		display : $.i18n('edoc.element.sendtounit'),
		name : 'exchangeRecUnitName',
		sortable : true,
		width : '10%'
	});
	return colModels;
}

/**
 * 重新加载列表
 */
function searchByCondition(o) {
    $("#listPending").ajaxgridLoad(o);
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

	if (col.name === "subject") {// 标题
		txt = getListSubjectTxt(txt, data, true, true, true, true, true, true, true);
	} else if (col.name === "govdocType") {// 分类
		txt = getListGovdocTypeName(data.govdocType, data.affairSubApp);
	} else if (col.name === "affairDeadLineName") {// 处理期限（节点期限）
		txt = getListRedColorTxt(txt, data);
	} else if (col.name === "surplusTime") {//办理剩余时间
		txt = getListSurplusTimeTxt(data);
	} else if (col.name === "processId") {//流程日志
		txt = getListProcessIdTxt(data);
	} else if(col.name === "startUserName") {//发起人
		txt = getListRedColorTxt(txt, data);
	} else if(col.name == "affairHastenTimes") {//催办次数
		if(txt == "") {
			txt = "0";
		}
	} else if(col.name === "signingDate") {//发起人
		if(txt != "") {
			txt = txt.split('&nbsp;')[0];
		}
	}

	//未读加粗显示
	txt = getListBoldTxt(txt, data);

	return txt;
}

// 双击事件
function dbclickRow(data, rowIndex, colIndex) {
	if (colIndex == 11) {
		return;
	}
	// 取消加粗
	cancelBold("listPending", rowIndex);

	if (!isAffairValid(data.affairId)) {
		reloadListGrid();
		return;
	}

	var _url = getListClickUrl(data, "listPending");
	if(!_url) {
		return;
	}

	var _title = data.subject;
	openGovdocDialog(_url, escapeStringToHTML(_title));
	grid.grid.resizeGridUpDown('down');

}

function loadPendingGrid() {
	var url = window.location.search;
	var o = new Object();
//	setParamsToObject(o, url);
//	$("#listPending").ajaxgridLoad(o);
	$("#listPending").ajaxgridLoad();
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
