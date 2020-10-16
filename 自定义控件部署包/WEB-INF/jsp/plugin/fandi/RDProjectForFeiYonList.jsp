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

var RDName="";	


var o=new Object();
o.serial_id=serial_id;
o.RDName=RDName;

//alert("serial_id:"+serial_id);
$().ready(function() {
 initSearchDiv();
 ajaxTable(o);

});

function ajaxTable(o){
 grid = $("#mytable").ajaxgrid({
    colModel: [
		{display:'选择',name:'serial_id',width:'50',type:'radio'},
		{display:'项目名称',name:'RDName',width:'570'},
		{display:'项目编码',name:'RDCode',width:'255'},
		],
    managerName: "fdManager",
    managerMethod: "getRDProjectData",
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
                id: 'RDName',
                name: 'RDName',
                type: 'input',
                text: '项目名称',
                value: 'RDName'
            },{
                id: 'RDCode',
                name: 'RDCode',
                type: 'input',
                text: '项目编码',
                value: 'RDCode'
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
	//alert(data);
	if(data==null||data==undefined||data==""){ 
		$.alert('请选择一条记录！');
		return null;
	}
	
	return {
		dataValue : data[0].RDName+"--"+data[0].RDCode
	};
	return data[0];
	 
}


function searchByName()
{
	var o=new Object();
	o.serial_id=serial_id;
	o.RDName=$("#proName").val();
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