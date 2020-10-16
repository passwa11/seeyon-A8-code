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
            if(choose === 'name'){
            	conditionObj.name = $('#name').val();
            }
            //实体编码
            else if(choose === 'code'){
            	conditionObj.code = $('#code').val();
            }
            //登录名
            else if(choose === 'loginName'){
            	conditionObj.loginName = $('#loginName').val();
            }
            // 部门编码
            else if(choose === 'deptCode'){
            	conditionObj.deptCode = $('#deptCode').val();
            }
            // 岗位编码
            else if(choose === 'postCode'){
            	conditionObj.postCode = $('#postCode').val();
            }
            // 职务编码
            else if(choose === 'levelCode'){
            	conditionObj.levelCode = $('#levelCode').val();
            }
            // email
            else if(choose === 'email'){
            	conditionObj.email = $('#email').val();
            }
            // 电话
            else if(choose === 'telNum'){
            	conditionObj.telNum = $('#telNum').val();
            }
            // 性别
            else if(choose === 'gender'){
            	conditionObj.gender = $('#gender').val();
            }
            // 同步状态
            else if(choose === 'synState'){ //流程状态
            	conditionObj.synState=$('#synState').val();
            }
            
            var val = searchobj.g.getReturnValue();
            if(val !== null){
                $("#dataList").ajaxgridLoad(conditionObj);
            }
        },
        conditions: [{
        	//实体名称
            id: 'name',
            name: 'name',
            type: 'input',
            text: '名称',
            value: 'name',
            maxLength:100
        },{
        	//实体编码
            id: 'code',
            name: 'code',
            type: 'input',
            text: '编码',
            value: 'code',
            maxLength:100
        },{
        	//登录名
            id: 'loginName',
            name: 'loginName',
            type: 'input',
            text: '登录名',
            value: 'loginName',
            maxLength:100
        },
        {
        	//部门编码
            id: 'deptCode',
            name: 'loginName',
            type: 'input',
            text: '部门编码',
            value: 'loginName',
            maxLength:100
        },
        {
        	//岗位编码
            id: 'postCode',
            name: 'postCode',
            type: 'input',
            text: '岗位编码',
            value: 'postCode',
            maxLength:100
        },
        {
        	//职务编码
            id: 'levelCode',
            name: 'levelCode',
            type: 'input',
            text: '职务编码',
            value: 'levelCode',
            maxLength:100
        },
        {
        	//email
            id: 'email',
            name: 'email',
            type: 'input',
            text: '邮箱',
            value: 'email',
            maxLength:100
        },{
        	//操作类型
            id: 'gender',
            name: 'gender',
            type: 'select',
            text: '性别',
            value: 'gender',
            items: [{
                text:  '男',
                value: '1'
            }, {
                text:  '女',
                value: '2'
            }]
        },{
        	//同步状态
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
    
    //表格加载
    grid = $('#dataList').ajaxgrid({
        colModel: [ {
        	//名称
            display: '名称',
            name: 'name',
            sortable : true,
            width: '15%'
        },{
        	//编码
            display:  '编码',
            name: 'code',
            sortable : true,
            width: '10%'
        },{
            display:  '登录名',
            name: 'loginName',
            sortable : true,
            width: '15%'
        },{
            display: '部门编码',
            name: 'deptCode',
            sortable : true,
            width: '10%'
        },{
            display: '职务编码',
            name: 'levelCode',
            width: '10%'                  
        },{
            display: '岗位编码',
            name: 'postCode',
            width: '10%'
        },{
            display: 'E-mail',
            name: 'email',
            sortable : true,
            width: '10%'
        },{
            display: '电话号码',
            name: 'telNum',
            width: '10%'                  
        },{
            display: '性别',
            name: 'gender',
            width: '5%'
        },{
            display: '是否同步',
            name: 'syncState',
            width: '5%'
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
        managerMethod : "getSyncMemberData"
    });
    
    //$('#dataList').ajaxgridLoad();
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
