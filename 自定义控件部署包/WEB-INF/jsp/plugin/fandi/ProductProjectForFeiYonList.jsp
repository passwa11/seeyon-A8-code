<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<html>
<head>
<title>产品项目</title>
<script type="text/javascript" src="${path}/ajax.do?managerName=fdManager"></script>

<script type="text/javascript">
var manager = new fdManager();
var grid ;
//父子节点
var win = window.transParams;
var contentDataId = $("#contentDataId",win.document).val();
var tempSpan = $(win.fillField);
var serial_id=contentDataId;
var productName="";

var o=new Object();
o.serial_id=serial_id;
o.productName=productName;

$().ready(function() {
 initSearchDiv();
 ajaxTable(o);

});


function initSearchDiv() {
        searchobj = $.searchCondition({
            top:2,
            right:5,
            searchHandler: function(){
                var returnValue = searchobj.g.getReturnValue();
                if(returnValue != null){
					var obj = setQueryParams(returnValue);
                    $("#mytable").ajaxgridLoad(obj);
                }
            },
            conditions: [{
                id: 'productName',
                name: 'productName',
                type: 'input',
                text: '产品名称',
                value: 'productName'
            },{
                id: 'productCode',
                name: 'productCode',
                type: 'input',
                text: '产品编码',
                value: 'productCode'
            }]
        });
    }
	
	
function setQueryParams(returnValue) {
		var condition = returnValue.condition;
		var value = returnValue.value;
		var obj = new Object();
		if (grid != null) {
			if (grid.p.params) {
				obj = grid.p.params;
			}
		}
		obj.type = "zw";
		obj.condition = condition;
		obj.queryValue = value;
		if (condition.length == 0) {
			obj.condition = "none";
			obj.queryValue = "";
		}
		//alert(obj);
		return obj;
	}

function ajaxTable(o){
 grid = $("#mytable").ajaxgrid({
    colModel: [
		{display:'选择',name:'serial_id',width:'50',type:'radio'},
		{display:'产品名称',name:'productName',width:'420'},
		{display:'产品编码',name:'productCode',width:'420'},
		],
    managerName: "fdManager",
    managerMethod: "getProductProjectData",
    parentId: 'center',
    vChangeParam: {
      overflow: 'hidden',
      position: 'relative'
    },
    slideToggleBtn: false,
    showTableToggleBtn: false,
    vChange: false
  });
  $("#mytable").ajaxgridLoad(o);
  $("#mytable").ajaxgridLoad();
}


function OK() {
	var rows = grid.grid.getSelectRows();
	if(rows==null||rows==undefined||rows==""){
		alert('请选择一项');
		return;
	}
	return {
		dataValue : rows[0].productName+"--"+rows[0].productCode
	};
}
	
//循环取重复表的列,从第一列开始循环
function getInputField(dir, pos){
	var target = $(window.dialogArguments.window.fillField).parent().parent();
	if(dir == 'prev'){
		for(var i = 0; i < pos; i++){
			target = target.prev();
		}
	}else if(dir == "next"){
		for(var i = 0; i < pos; i++){
			target = target.next();
		}
	}		
	//alert($(target).find("input").eq(0));
	return $(target).find("input").eq(0);
}

function GetQueryString(name)
{
     var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
     var r = window.location.search.substr(1).match(reg);
     if(r!=null)return  unescape(r[2]); return null;
}
</script>
</head>
<body>
<div id='layout' class="comp" comp="type:'layout'">
    <div class="comp" comp="type:'breadcrumb',code:'T02_showPostframe'"></div>
    <div class="layout_north" layout="height:30,sprit:false,border:false">
    </div>
    <div class="layout_center over_hidden" layout="border:false" id="center">
        <table id="mytable" class="flexme3" border="0" cellspacing="0" cellpadding="0" ></table>
    </div>
</div>
</body>
</html>