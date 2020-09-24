/*******************************************************************************
 * 公文发文登记簿JS文件 create by xuqiangwei
 */

// 页面初始化执行
$(function() {

    // 搜索框
    _doInitSearchObj();

    // toolbar
    _doInitToolbar();

    //组合查询设置
    _doInitCompQuery();
    
    //如果是栏目打开页面，进行搜索栏影藏
    _initPage();
    
    // 加载数据
    _doInitGrid();
});

/**
 * 组合查询设置
 */
function _doInitCompQuery(){
    
    var dialogHeight = 340;
    if(isG6 != 'true' || registerSwitch != 'true'){
        dialogHeight = dialogHeight - 36;
    }
    
    var params = {
        width : 800,
        height : dialogHeight,
        listType : "recRegister",
        datatablId : "recRegisterDataTabel"
    }
    _initCompQuery(params);
}

/**
 * 单击事件
 * @param row
 * @param colIndex
 * @param rowIndex
 */
function showEdocDetail(row, colIndex, rowIndex){
    
    var summaryId = row.id;
    var state = row.affairState;
    

    var requestCaller = new XMLHttpRequestCaller(this, "ajaxEdocManager", "getAffairIdByEdocId", false);
    requestCaller.addParameter(1, "String", summaryId);
    var affairId = requestCaller.serviceRequest();
    if(affairId == ""){
    	return;
    }
    
    var fromValue="Done";
    if(state=='2'){
        fromValue="sended";
    }
    var url = "from="+fromValue+"&summaryId="+summaryId+"&affairId="+affairId+"&openEdocByForward=true&openFrom=recRegisterResult";
    url = _ctxPath + "/edocController.do?method=detailIFrame&" + url;
    v3x.openWindow({
        url: url,
        workSpace: 'yes',
        dialogType: "open",
        closePrevious : "no"
    });;  
  }

/**
 * 加载列表
 */
function _doInitGrid() {

    var colModels = [];
    
    colModels.push({
        display : $.i18n("edoc.element.subject"),//公文标题
        id:'subject',
        name : 'subject',
        width : '30%',
        sortable : true,
        align : 'left',
        hide : false
    });
    
    colModels.push({
        display : $.i18n('edoc.element.communication.date'),//来文日期
        name : 'registrationDate',
        width : '10%',
        sortable : true,
        sortType : "date",
        dataType : "date",
        align : 'left',
        hide : false
    });
    
    colModels.push({
        display : $.i18n('edoc.edoctitle.fromUnit.label'),//来文单位
        name : 'sendUnit',
        width : '10%',
        sortable : true,
        align : 'left',
        hide : false
    });
    colModels.push({
        display : $.i18n('edoc.element.fromWordNo'),//来文文号
        name : 'docMark',
        width : '10%',
        sortable : true,
        align : 'left',
        hide : false
    });
    colModels.push({
        display : $.i18n('edoc.element.receive.serial_no'),//收文编号
        name : 'serialNo',
        width : '10%',
        align : 'left',
        sortable : true,
        hide : false
    });
    colModels.push({
        display : $.i18n('edoc.element.undertaker'),//承办人
        name : 'undertaker',
        width : '10%',
        sortable : true,
        align : 'left',
        hide : false
    });
    
    var createTimeLabel = $.i18n('edoc.edoctitle.regDate.label');//登记日期 
    if(isG6 == 'true'){
        createTimeLabel = $.i18n('edoc.edoctitle.disDate.label');//分发日期 G6 A8区隔
    }
    
    colModels.push({
        display : createTimeLabel,//
        name : 'createTime',
        width : '10%',
        sortable : true,
        sortType : "date",
        align : 'left',
        dataType : "date",
        hide : false
    });
    
    var distributerLabel = $.i18n('edoc.edoctitle.regPerson.label');//登记人
    if(isG6 == 'true'){
        distributerLabel = $.i18n('edoc.element.receive.distributer');//分发人
    }
    colModels.push({
        display : distributerLabel,//
        name : 'distributer',
        width : '10%',
        sortable : true,
        align : 'left',
        hide : false
    });
    
    colModels.push({
        display : $.i18n('edoc.element.undertakeUnit'),//承办机构 待处理...
        name : 'undertakenoffice',
        width : '10%',
        sortable : true,
        align : 'left',
        hide : true
    });
    
    colModels.push({
        display : $.i18n('edoc.element.receive.send_unit_type'),//来文类别
        name : 'sendUnitType',
        width : '10%',
        sortable : true,
        align : 'left',
        codecfg : "codeId:'send_unit_type'",
        hide : true
    });
    colModels.push({
        display : $.i18n('edoc.element.secretlevel.simple'),//密级
        name : 'secretLevel',
        width : '10%',
        sortable : true,
        align : 'left',
        codecfg : "codeId:'edoc_secret_level'",
        hide : true
    });
    colModels.push({
        display : $.i18n('edoc.element.keepperiod'),//保密期限
        name : 'keepPeriod',
        width : '10%',
        sortable : true,
        align : 'left',
        codecfg : "codeId:'edoc_keep_period'",
        hide : true
    });
    colModels.push({
        display : $.i18n('edoc.element.urgentlevel'),//紧急程度
        name : 'urgentLevel',
        width : '10%',
        sortable : true,
        align : 'left',
        codecfg : "codeId:'edoc_urgent_level'",
        hide : true
    });
    
    if(isG6 == 'true' && registerSwitch == 'true'){//G6环境并且开起了登记开关
        colModels.push({
            display : $.i18n('edoc.edoctitle.regDate.label'),//登记日期 做区隔
            name : 'registerDate',
            width : '10%',
            sortable : true,
            sortType : "date",
            align : 'left',
            dataType : "date",
            hide : true
        });
        colModels.push({
            display : $.i18n('edoc.edoctitle.regPerson.label'),//登记人 做区隔
            name : 'registerUserName',
            width : '10%',
            sortable : true,
            align : 'left',
            hide : true
        });
    }
    
    colModels.push({
        display : $.i18n('edoc.element.receipt_date'),//签收日期
        name : 'recTime',
        width : '10%',
        sortable : true,
        sortType : "date",
        align : 'left',
        dataType : "date",
        ifFormat : '%Y-%m-%d HH:mm',
        hide : true
    });
    
    colModels.push({
        display : $.i18n('exchange.edoc.receivedperson'),//签收人
        name : 'recieveUserName',
        width : '10%',
        sortable : true,
        align : 'left',
        hide : true
    });
    
    
    colModels.push({
        display : $.i18n('edoc.element.sendTime'),//送文日期 
        name : 'sendTime',
        width : '10%',
        sortable : true,
        align : 'left',
        sortType : "date",
        hide : true
    });
    colModels.push({
        display : $.i18n('edoc.element.unitLevel'),//公文级别
        name : 'unitLevel',
        width : '10%',
        sortable : true,
        align : 'left',
        codecfg : "codeId:'edoc_unit_level'",
        hide : true
    });
 // 无书生插件不显示
	/*if (hasSursen == "true") {
		colModels.push( {
			display : $.i18n('edoc.exchangeMode'),// 交换方式
			name : 'exchangeMode',
			width : '10%',
			sortable : true,
			align : 'left',
			hide : true
		});
	}*/
    
    var params = {
            colModels : colModels,
            datatablId : "recRegisterDataTabel",
            clickFn : showEdocDetail,
            ajaxMethod : "getRecRegisterData",
            customId : "edoc_rec_register_grid"
    }
    
    _initGrid(params);
}

/**
 * toolbar初始化
 */
function _doInitToolbar() {

    var params = {
            toolbarId: "recRegist_toolbar",
            listType : "recRegister"
    }
    _initToolbar(params);
}

/**
 * 初始化查询框
 */
function _doInitSearchObj() {

    var conditionsCol = [];
    conditionsCol.push({
        id : 'subject',
        name : 'subject',
        type : 'input',
        text : $.i18n("edoc.element.subject"),//公文标题
        value : 'subject'
    });
    conditionsCol.push({
        id : 'docMark',
        name : 'docMark',
        type : 'input',
        text : $.i18n('edoc.element.fromWordNo'),//来文文号
        value : 'docMark'
    });
    conditionsCol.push({
        id : 'serialNo',
        name : 'serialNo',
        type : 'input',
        text : $.i18n('edoc.element.receive.serial_no'),//收文编号
        value : 'serialNo'
    });
    conditionsCol.push({
        id : 'sendUnit',
        name : 'sendUnit',
        type : 'input',
        text : $.i18n('edoc.edoctitle.fromUnit.label'),//来文单位
        value : 'sendUnit'
    });
    
    var registLabel = $.i18n('edoc.edoctitle.regDate.label');//登记日期
    if(isG6 == 'true'){
        registLabel = $.i18n('edoc.edoctitle.disDate.label');//分发日期
    }
    conditionsCol.push({
        id : 'createTime',
        name : 'createTime',
        type : 'datemulti',
        text : registLabel,
        value : 'createTime',
        ifFormat : '%Y-%m-%d',
        dateTime : false
    });
    
    conditionsCol.push({
        id : 'recieveDate',
        name : 'recieveDate',
        type : 'datemulti',
        text : $.i18n('exchange.edoc.receiveddate'),//签收日期
        value : 'recieveDate',
        ifFormat : '%Y-%m-%d',
        dateTime : false
    });
    
    conditionsCol.push({
        id : 'undertakenoffice',
        name : 'undertakenoffice',
        type : 'input',
        text : $.i18n('edoc.element.undertakeUnit'),//承办机构 待处理...
        value : 'undertakenoffice',
        dateTime : false
    });
 // 无书生插件不显示
	/*if (hasSursen == "true") {
		conditionsCol.push( {
			id : 'exchangeMode',
			name : 'exchangeMode',
			type : 'select',
			text : $.i18n('edoc.exchangeMode'),//交换方式
			value : 'exchangeMode',
			items : [ {
				text : $.i18n('edoc.exchangeMode.internal'), // 内部公文交换
				value : '0'
			}, {
				text : $.i18n('edoc.exchangeMode.sursen'), // 书生公文交换
				value : '1'
			} ]
		});
	} */
    var params = {
            conditionsCol : conditionsCol,
            datatablId : "recRegisterDataTabel"
    }
    
    _initSearchObj(params);
}