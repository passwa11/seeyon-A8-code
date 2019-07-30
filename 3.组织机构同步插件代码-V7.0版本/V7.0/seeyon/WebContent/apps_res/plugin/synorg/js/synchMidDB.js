/** 工具栏对象 */
var toolbar;

/** 数列表表对象 */
var grid;

/** 当前选择的实体类型 */
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
    //单位
    toolbarArray.push({id: "unit",name: '单位',className: "ico16 department_16",click:filterLogsListByUnit});
    // 部门
    toolbarArray.push({id: "dept",name: '部门',className: "ico16 department_16",click:filterListByDepartment});
    // 岗位
    toolbarArray.push({id: "post",name: '岗位',className: "ico16 radio_post_16",click:filterListByPost});
    // 职务
    toolbarArray.push({id: "level",name: '职务',className: "ico16 radio_level_16",click:filterListByLevel});
    // 人员
    toolbarArray.push({id: "member",name: '人员',className: "ico16 staff_16",click:filterListByMember});

    // 设置工具栏
    toolbar = $("#toolbars").toolbar({
        toolbar: toolbarArray
    });
    
    // 搜索框
    var topSearchSize = 2;
    if($.browser.msie && $.browser.version=='6.0'){
        topSearchSize = 5;
    }
    
    var searchobj = $.searchCondition({
        top:topSearchSize,
        right:10,
        searchHandler: function(){
        	// 查询条件对象
            var conditionObj = new Object();
            conditionObj.entityType = type
            var choose = $('#'+searchobj.p.id).find("option:selected").val();
            // 实体名称
            if(choose === 'name'){
            	conditionObj.name = $('#name').val();
            }
            // 实体编码
            else if(choose === 'code'){
            	conditionObj.code = $('#code').val();
            }
            // 上级编码
            else if(choose === 'pcode'){
            	conditionObj.pcode = $('#pcode').val();
            }
            // 创建时间
            else if(choose === 'createDate'){
                var fromDate = $('#from_createDate').val();
                var toDate = $('#to_createDate').val();
                var date = fromDate+'#'+toDate;
                conditionObj.createDate = date;
                if(fromDate != "" && toDate != "" && fromDate > toDate){
                    return;
                }
            }
            // 同步状态
            else if(choose === 'synState'){ // 流程状态
            	conditionObj.synState=$('#synState').val();
            }
            
            var val = searchobj.g.getReturnValue();
            if(val !== null){
                $("#dataList").ajaxgridLoad(conditionObj);
            }
        },
        conditions: [{
        	// 实体名称
            id: 'name',
            name: 'name',
            type: 'input',
            text: '名称',
            value: 'name',
            maxLength:100
        },{
        	// 实体编码
            id: 'code',
            name: 'code',
            type: 'input',
            text: '编码',
            value: 'code',
            maxLength:100
        },{
        	// 上级编码
            id: 'pcode',
            name: 'pcode',
            type: 'input',
            text: '上级编码',
            value: 'pcode',
            maxLength:100
        },{
        	// 创建时间
            id: 'createDate',
            name: 'createDate',
            type: 'datemulti',
            text: '创建时间',
            value: 'createDate',
            ifFormat:'%Y-%m-%d',
            dateTime: false
        },{
        	// 同步状态
        	id:'synState',
        	name:'synState',
        	type:'select',
        	text:'同步状态',
        	value: 'synState',
            items:[{
            	text:'未同步',
            	value:'0'
            },{
            	text:'失败',
            	value:'-1'
            },{
            	text:'成功',
            	value:'1'
            }]
        }]
    });
    
    searchobj.g.setCondition("name","");
    
    // 表格加载
    grid = $('#dataList').ajaxgrid({
        colModel: [{
        	// 名称
            display: '名称',
            name: 'name',
            sortable : true,
            width: '30%'
        },{
        	// 编码
            display:  '编码',
            name: 'code',
            sortable : true,
            width: '20%'
        },{
        	// 上级编码
            display:  '上级编码',
            name: 'parentCode',
            sortable : true,
            width: '20%'
        },{
        	// 创建日期
            display: '创建日期',
            name: 'createDate',
            sortable : true,
            width: '20%'                  
        },{
        	// 同步状态
            display: '同步状态',
            name: 'syncState',
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
        managerName : "syncDataManager",
        managerMethod : "getSyncData"
    });
    
    //$('#dataList').ajaxgridLoad();
    
});

/**
 * 列表加载 回调函数
 */
function rend(txt, data, r, c) {
    if(c === 1){
    	// 标题列加深
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
 * 获取单位同步日志
 */
function filterLogsListByUnit(){
	currentEntityType = "Unit";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#unit_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#dataList").ajaxgridLoad(conditionObj);
}
	
/**
 * 获取部门同步数据
 */
function filterListByDepartment(){
	currentEntityType = "Department";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#dept_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#dataList").ajaxgridLoad(conditionObj);
}

/**
 * 获取岗位同步数据
 */
function filterListByPost(){
	currentEntityType = "Post";	
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#post_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#dataList").ajaxgridLoad(conditionObj);
}

/**
 * 获取职务同步数据
 */
function filterListByLevel(){
	currentEntityType = "Level";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#level_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#dataList").ajaxgridLoad(conditionObj);
}

/**
 * 获取职人员同步数据
 */
function filterListByMember(){
	currentEntityType = "Member";
	var conditionObj = new Object();
	$("a[style=\"background-color: white;\"]").removeAttr("style");
	$("#member_a").css('backgroundColor','white'); 
	conditionObj.entityType = currentEntityType
	$("#dataList").ajaxgridLoad(conditionObj);
}
