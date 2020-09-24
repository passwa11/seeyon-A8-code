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
    
    var params = {
        width : 800,
        height : 320,
        listType : "sendRegister",
        datatablId : "sendRegisterDataTabel"
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
    
    var edocType = row.edocType;
    var edocId = row.id;
    var state = row.affairState;
    
    var requestCaller = new XMLHttpRequestCaller(this, "ajaxEdocManager", "getAffairIdByEdocId", false);
    requestCaller.addParameter(1, "String", edocId);
    var affairId = requestCaller.serviceRequest();
    if(affairId == ""){
    	return;
    }
    
    //OA-33399 应用检查：本单位下外部门人员封发完成的数据，发文登记簿时查询出来了，单击穿透查看，已办结的公文还有督办设置功能，自己本单位本部门已办结的公文没有督办设置功能
    //改为已办的链接
    var fromValue="Done";
    if(state=='2'){
        fromValue="sended";
    }
    var url = "from="+fromValue+"&affairId="+affairId+"&edocType="+edocType+"&edocId="+edocId+"&openEdocByForward=true&openFrom=sendRegisterResult";//从登记簿查询结果中穿透查看公文页面，不进行安全性检查，虽然是转发文的参数，为了不再增加多余的参数，就传这个了
    url = _ctxPath + "/edocController.do?method=detailIFrame&" + url;
    v3x.openWindow({
      url: url,
      workSpace: 'yes',
      dialogType: 'open',
      closePrevious : "no"
    });  
  }

/**
 * 加载列表
 */
function _doInitGrid(){
    
    var colModels = [];
	colModels.push( {
		display : $.i18n("edoc.element.subject"),// 公文标题
		id : 'subject',
		name : 'subject',
		width : '30%',
		sortable : true,
		align : 'left',
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.senddepartment'),// 发文部门
		name : 'departmentName',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.edoctitle.createDate.label'),// 拟文日期
		name : 'startTime',
		width : '10%',
		sortable : true,
		sortType : "date",
		align : 'left',
		dataType : "date",
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.wordno.label'),// 公文文号
		name : 'docMark',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.wordinno.label'),// 内部文号
		name : 'serialNo',
		width : '10%',
		align : 'left',
		sortable : true,
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.issuer'),// 签发人
		name : 'issuer',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.sendingdate'),// 签发日期
		name : 'signingDate',
		width : '10%',
		sortable : true,
		sortType : "date",
		dataType : "date",
		align : 'left',
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.author'),// 拟稿人
		name : 'createPerson',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : false
	});
	colModels.push( {
		display : $.i18n('edoc.element.review'),// 复核人
		name : 'review',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.sendunit'),// 发文单位
		name : 'sendUnit',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.sendtounit'),// 主送单位
		name : 'sendTo',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.copytounit'),// 抄送单位
		name : 'copyTo',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.copies'),// 印发份数
		name : 'copies',
		width : '10%',
		sortable : true,
		align : 'left',
		sortType : "number",
		hide : true

	});
	colModels.push( {
		display : $.i18n('exchange.edoc.sendperson'),// 送文人
		name : 'sender',
		width : '10%',
		sortable : true,
		align : 'left',
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.secretlevel.simple'),// 密级
		name : 'secretLevel',
		width : '10%',
		sortable : true,
		align : 'left',
		codecfg : "codeId:'edoc_secret_level'",
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.keepperiod'),// 保密期限
		name : 'keepPeriod',
		width : '10%',
		sortable : true,
		align : 'left',
		codecfg : "codeId:'edoc_keep_period'",
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.urgentlevel'),// 紧急程度
		name : 'urgentLevel',
		width : '10%',
		sortable : true,
		align : 'left',
		codecfg : "codeId:'edoc_urgent_level'",
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.sendTime'),// 送文日期
		name : 'sendTime',
		width : '10%',
		sortable : true,
		align : 'left',
		sortType : "date",
		hide : true
	});
	colModels.push( {
		display : $.i18n('edoc.element.unitLevel'),// 公文级别
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
            datatablId : "sendRegisterDataTabel",
            clickFn : showEdocDetail,
            ajaxMethod : "getSendRegisterData",
            customId : "edoc_send_register_grid"
    }
    
    _initGrid(params);
}

/**
 * toolbar初始化
 */
function _doInitToolbar() {

    var params = {
            toolbarId: "sendRegist_toolbar",
            listType : "sendRegister"
    }
    _initToolbar(params);
}

/**
 * 初始化查询框
 */
function _doInitSearchObj() {

    var conditionsCol = [];
	conditionsCol.push( {
		id : 'subject',
		name : 'subject',
		type : 'input',
		text : $.i18n("edoc.element.subject"),//公文标题
		value : 'subject'
	});
	conditionsCol.push( {
		id : 'docMark',
		name : 'docMark',
		type : 'input',
		text : $.i18n('edoc.element.wordno.label'),//公文文号
		value : 'docMark'
	});
	conditionsCol.push( {
		id : 'serialNo',
		name : 'serialNo',
		type : 'input',
		text : $.i18n('edoc.element.wordinno.label'),//内部文号
		value : 'serialNo'
	});
	conditionsCol.push( {
		id : 'sendUnit',
		name : 'sendUnit',
		type : 'input',
		text : $.i18n('edoc.element.sendunit'),//发文单位
		value : 'sendUnit'
	});
	conditionsCol.push( {
		id : 'startTime',
		name : 'startTime',
		type : 'datemulti',
		text : $.i18n('edoc.edoctitle.createDate.label'),//拟文日期
		value : 'startTime',
		ifFormat : '%Y-%m-%d',
		dateTime : false
	});
	conditionsCol.push( {
		id : 'signingDate',
		name : 'signingDate',
		type : 'datemulti',
		text : $.i18n('edoc.element.sendingdate'),//签发日期
		value : 'signingDate',
		ifFormat : '%Y-%m-%d',
		dateTime : false
	});
	conditionsCol.push( {
		id : 'sendTime',
		name : 'sendTime',
		type : 'datemulti',
		text : $.i18n('edoc.exchange.sendDate'),//送文日期
		value : 'sendTime',
		ifFormat : '%Y-%m-%d',
		dateTime : false
	});
	conditionsCol.push( {
		id : 'departmentName',
		name : 'departmentName',
		type : 'input',
		text : $.i18n('edoc.element.senddepartment'),//发文部门
		value : 'departmentName'
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
	}*/ 
    
    var params = {
            conditionsCol : conditionsCol,
            datatablId : "sendRegisterDataTabel"
    }
    
    _initSearchObj(params);
}