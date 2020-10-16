/** 工具栏对象 */
var toolbar;

/** 数列表表对象 */
var grid;

/** 当前选择的实体类型*/
var currentEntityType;

/**
 * 页面初始化
 */
$(document).ready(function () {
    new MxtLayout({
        'id': 'layout',
        'northArea': {
            'id': 'north',
            'height': 30,
            'sprit': false,
            'border': false
        },
        'centerArea': {
            'id': 'center',
            'border': false,
            'minHeight': 20
        }
    });
   
    var toolbarArray = new Array();
    //全部日志
    toolbarArray.push({id: "all",name: '全部',className: "ico16 radio_group_16",click:filterLogsListByAll});
    //单位
    toolbarArray.push({id: "unit",name: '单位',className: "ico16 department_16",click:filterLogsListByUnit});
    //部门
    toolbarArray.push({id: "dept",name: '部门',className: "ico16 department_16",click:filterLogsListByDepartment});
    //岗位
    toolbarArray.push({id: "post",name: '岗位',className: "ico16 radio_post_16",click:filterLogsListByPost});
    //职务
    toolbarArray.push({id: "level",name: '职务',className: "ico16 radio_level_16",click:filterLogsListByLevel});
    //人员
    toolbarArray.push({id: "member",name: '人员',className: "ico16 staff_16",click:filterLogsListByMember});
    //清除日志
    toolbarArray.push({id: "delete",name: '删除',className: "ico16 del_16",click:deleteCol});

    //设置工具栏
    toolbar = $("#toolbars").toolbar({
        toolbar: toolbarArray
    });
    
    //搜索框
    var topSearchSize = 2;
    if($.browser.msie && $.browser.version=='6.0'){
        topSearchSize = 5;
    }
    var searchobj = $.searchCondition({
        top:topSearchSize,
        right:10,
        searchHandler: function(){
        	//查询条件对象
            var conditionObj = new Object();
            conditionObj.entityType = currentEntityType
            var choose = $('#'+searchobj.p.id).find("option:selected").val();
            //实体名称
            if(choose === 'entityName'){
            	conditionObj.entityName = $('#entityName').val();
            }
            //实体编码
            else if(choose === 'entityCode'){
            	conditionObj.entityCode = $('#entityCode').val();
            }
            //操作类型
            else if(choose === 'synType'){
            	conditionObj.synType = $('#synType').val();
            }
            //同步时间
            else if(choose === 'synDate'){
                var fromDate = $('#from_synDate').val();
                var toDate = $('#to_synDate').val();
                var date = fromDate+'#'+toDate;
                conditionObj.synDate = date;
                if(fromDate != "" && toDate != "" && fromDate > toDate){
                    $.alert("开始时间不能早于结束时间!");
                    return;
                }
            }
            //同步状态
            else if(choose === 'synState'){ //流程状态
            	conditionObj.synState=$('#synState').val();
            }
            
            var val = searchobj.g.getReturnValue();
            if(val !== null){
                $("#logList").ajaxgridLoad(conditionObj);
            }
        },
        conditions: [{
        	//实体名称
            id: 'entityName',
            name: 'entityName',
            type: 'input',
            text: '名称',
            value: 'entityName',
            maxLength:100
        },{
        	//实体编码
            id: 'entityCode',
            name: 'entityCode',
            type: 'input',
            text: '编码',
            value: 'entityCode',
            maxLength:100
        },{
        	//操作类型
            id: 'synType',
            name: 'synType',
            type: 'select',
            text: '操作类型',
            value: 'synType',
            items: [{
                text:  '新增',
                value: '1'
            }, {
                text:  '更新',
                value: '2'
            }, {
                text:  '删除',
                value: '3'
            }]
        },{
        	//同步时间
            id: 'synDate',
            name: 'synDate',
            type: 'datemulti',
            text: '同步时间',
            value: 'synDate',
            ifFormat:'%Y-%m-%d',
            dateTime: false
        },{
        	//同步状态
        	id:'synState',
        	name:'synState',
        	type:'select',
        	text:'同步状态',
        	value: 'synState',
            items:[{
            	text:'失败',
            	value:'-1'
            },{
            	text:'成功',
            	value:'1'
            }]
        }]
    });
    
    //表格加载
    grid = $('#logList').ajaxgrid({
        colModel: [{
        	//选择框
            display: 'id',
            name: 'id',
            width: '4%',
            type: 'checkbox'
        }, {
        	//类型
            display: '类别',
            name: 'entityType',
            sortable : true,
            width: '8%'
        }, {
        	//名称
            display: '名称',
            name: 'entityName',
            sortable : true,
            width: '10%'
        }, {
        	//编码
            display:  '编码',
            name: 'entityCode',
            sortable : true,
            width: '8%'
        },{
        	//操作类型
            display:  '操作类型',
            name: 'synType',
            sortable : true,
            width: '8%'
        },{
        	//同步日志
            display: '同步日志',
            name: 'synLog',
            sortable : true,
            width: '42%'
        }, {
        	//同步日期
            display: '同步日期',
            name: 'synDate',
            sortable : true,
            width: '10%'                  
        }, {
        	//同步状态
            display: '同步状态',
            name: 'synState',
            width: '10%'
        }],
        render : rend,
        height: 200,
        rpMaxSize : 999,
        showTableToggleBtn: true,
        parentId: $('.layout_center').eq(0).attr('id'),
        vChange: true,
        vChangeParam: {
            overflow: "hidden",
            autoResize:true
        },
        isHaveIframe:true,
        slideToggleBtn:true,
        managerName : "syncLogManager",
        managerMethod : "getSynLogList"
    });
    
    //默认显示全部日志
    $("#all_a").css('backgroundColor','white'); 
});

/**
 * 列表加载 回调函数
 */
function rend(txt, data, r, c) {
    if(c === 1){
    	//标题列加深
	    txt="<font class='red-input'>"+txt+"</font>";
        return txt;
    }else if(c===3){
    	return txt;
    }else if(c === 4){
       
        return txt;
    }else if(c === 5){
    	return txt;
    }else if (c === 6){
    	return txt;
    }else{
    	return txt;
    }
} 


/**
 * 获取全部同步日志
 */
function filterLogsListByAll(){
	currentEntityType = "";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#all_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#logList").ajaxgridLoad(conditionObj);
}
	
/**
 * 获取单位同步日志
 */
function filterLogsListByUnit(){
	currentEntityType = "Unit";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#unit_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#logList").ajaxgridLoad(conditionObj);
}
/**
 * 获取部门同步日志
 */
function filterLogsListByDepartment(){
	currentEntityType = "Department";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#dept_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#logList").ajaxgridLoad(conditionObj);
}

/**
 * 获取岗位同步日志
 */
function filterLogsListByPost(){
	currentEntityType = "Post";	
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#post_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#logList").ajaxgridLoad(conditionObj);
}

/**
 * 获取职务同步日志
 */
function filterLogsListByLevel(){
	currentEntityType = "Level";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#level_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#logList").ajaxgridLoad(conditionObj);
}

/**
 * 获取人员同步日志
 */
function filterLogsListByMember(){
	currentEntityType = "Member";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#member_a").css('backgroundColor','white'); 
	debugger;
	conditionObj.entityType = currentEntityType
	$("#logList").ajaxgridLoad(conditionObj);
}

/**
 * 删除同步日志
 */
function deleteCol(){
	var rows = grid.grid.getSelectRows();
	var ids = "";
	if(rows.length <= 0) {
		$.alert("请选择要删除的日志");
		return true;
	}

	var confirm = $.confirm({
		'msg': "该操作不能恢复，是否进行删除操作?",
	    ok_fn: function () {
	    	for(var count = 0 ; count < rows.length; count ++){
	    		if(count == rows.length -1){
	    			ids += rows[count].id;
	    		}else{
	    			ids += rows[count].id +",";
	    		}
	    	}
	    	// 实例化Spring BS对象
	    	var logManager = new syncLogManager();
	    	logManager.deleteSyncLogByIds(ids);

	    	// 成功删除，并刷新列表
	    	$.messageBox({
	    		'title':'提示框',
	    		'type': 0,
	    		'msg': '删除成功',
	    		'imgType':0,
	    		ok_fn:function(){
	    			$("#logList").ajaxgridLoad();
	    		}
	    	});
	    }
   	}); 
}