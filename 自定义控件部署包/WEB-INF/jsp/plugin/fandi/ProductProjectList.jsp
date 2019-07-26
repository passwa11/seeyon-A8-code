<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<%@ include file="/WEB-INF/jsp/common/common.jsp"%>
<style>
.stadic_head_height{
    height:0px;
}
.stadic_body_top_bottom{
    bottom: 0px;
    top: 0px;
}
.stadic_footer_height{
    height:37px;
}
.box_relative {
  position: relative;
  left: 30px;
  top: 20px;
}
</style>
<script type="text/javascript" src="${path}/ajax.do?managerName=fdManager"></script>
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/V3X.js${ctp:resSuffix()}" />"></script>

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

//alert("serial_id:"+serial_id);
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
function OK(){
	var data =  grid.grid.getSelectRows();
	//alert(data);
	if(data==null||data==undefined||data==""){ 
		$.alert('请选择一条记录！');
		return null;
	}
	
	var syjh_fieldname = manager.getMasterFieldName(win.form.id,"产品项目");//获取父页面属性ID	
	$("#"+syjh_fieldname,win.document).val(data[0].productName);//给获取到的父页面属性赋值
	
	var syjh_fieldCode = manager.getMasterFieldName(win.form.id,"产品项目编码");//获取父页面属性ID	
	var ckflag=manager.setYSValue(win.form.id,contentDataId,syjh_fieldCode,data[0].productCode);
	//$("#"+syjh_fieldCode,win.document).val(data[0].productCode);//给获取到的父页面属性赋值
	
	//$("#"+syjh_fieldname,win.document).html(data[0].company_code);
	return data[0];
	 
}


function searchByName()
{
	var o=new Object();
	o.serial_id=serial_id;
	o.productName=$("#proName").val();
	//alert($("#proName").val());
	//刷新页面
	ajaxTable(o);
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