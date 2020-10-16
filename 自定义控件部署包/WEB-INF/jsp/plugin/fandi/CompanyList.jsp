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
var companyName="";	

var o=new Object();
o.serial_id=serial_id;
o.companyName=companyName;

//alert("serial_id:"+serial_id);
$().ready(function() {
 initSearchDiv()
 ajaxTable(o);

});

function ajaxTable(o){
 grid = $("#mytable").ajaxgrid({
    colModel: [
		{display:'选择',name:'serial_id',width:'100',type:'radio'},
		{display:'供应商名称',name:'companyName',width:'220'},
		{display:'供应商编码',name:'companyCode',width:'110'},
		{display:'银行账户',name:'bankNumber',width:'220'},
		{display:'开户行',name:'bankCode',width:'220'},
		],
    managerName: "fdManager",
    managerMethod: "getCompanyData",
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
                id: 'companyName',
                name: 'companyName',
                type: 'input',
                text: '供应商名称',
                value: 'companyName'
            },{
                id: 'companyCode',
                name: 'companyCode',
                type: 'input',
                text: '供应商编码',
                value: 'companyCode'
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



function OK(){
	var data =  grid.grid.getSelectRows();
	if(data==null||data==undefined||data==""){ 
		$.alert('请选择一条记录！');
		return null;
	}
	
	var syjh_fieldname = manager.getMasterFieldName(win.form.id,"收款公司名");//获取父页面属性ID	
	$("#"+syjh_fieldname,win.document).val(data[0].companyName);//给获取到的父页面属性赋值
	
	var bankNumber_fieldname = manager.getMasterFieldName(win.form.id,"收款公司银行名");//获取父页面属性ID	
	$("#"+bankNumber_fieldname,win.document).val(data[0].bankCode);//给获取到的父页面属性赋值
	$("#"+bankNumber_fieldname,win.document).attr("readOnly",true);
	
	var bankCode_fieldname = manager.getMasterFieldName(win.form.id,"收款公司银行账号");//获取父页面属性ID	
	$("#"+bankCode_fieldname,win.document).val(data[0].bankNumber);//给获取到的父页面属性赋值
	$("#"+bankCode_fieldname,win.document).attr("readOnly",true	);
	
	//给主表隐藏字段赋值
	var companyCode_fieldname = manager.getMasterFieldName(win.form.id,"收款公司编码");//获取父页面属性ID	
	var ckflag=manager.setYSValue(win.form.id,contentDataId,companyCode_fieldname,data[0].companyCode);

	return data[0];
	 
}


function searchByName()
{
	var o=new Object();
	o.serial_id=serial_id;
	o.companyName=$("#proName").val();
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