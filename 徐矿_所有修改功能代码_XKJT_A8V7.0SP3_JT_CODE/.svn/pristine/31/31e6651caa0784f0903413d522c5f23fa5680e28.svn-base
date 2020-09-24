$(document).ready(function() {
	new MxtLayout({
		'id' : 'layout',
		'centerArea' : {
			'id' : 'center',
			'border' : false,
			'minHeight' : 20
		},
		'northArea' : {
			'id' : 'north',
			'height' : 30,
			'sprit' : false,
			'border' : false
		},
	});
	//表格加载
	var grid = $('#yiYueTable').ajaxgrid({
		colModel : [ {
			display : "id",
			name : 'id',
			sortable : true,
			width : '5%',
			type : 'checkbox'
		}, {
			display : "公文标题",
			name : 'title',
			sortable : true,
			width : '31%'
		}, {
			display : "发起者",
			name : 'senderName',
			sortable : true,
			width : '31%'
		}, {
			display : "创建时间",
			name : 'sendDate',
			sortable : true,
			width : '31%'
		}],

		click: openDetail,
		render : rend,
		height : 200,
		showTableToggleBtn : true,
		parentId : 'center',
		vChange : true,
		vChangeParam : {
			overflow : "hidden",
			autoResize : true
		},
		isHaveIframe : true,
		slideToggleBtn : true,
		managerName : "xkjtManager",
		managerMethod : "findXkjtLeaderBanJieByMemberId"
	});
});

function rend(txt, data, r, c) {
	return txt;
}
function openDetail(data, r, c){
	
	var url = "edocController.do?method=edocDetailInDoc&summaryId="+data.edocId+"&openFrom=lenPotent&lenPotent=100";
	window.open(url,"_blank");
}




