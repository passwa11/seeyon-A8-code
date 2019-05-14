var unflowList, param, loading, process;
$(document).ready(function() {
	param = initParam().params;// 获取页面参数
	// 处理进度条
	process = top.$.progressBar({
		text : "加载中..."
	});
	loading = true;
	getUnflowList();
});


// 获取底表
function getUnflowList() {
	$.ajax({
		url : "/seeyon/rest/cap4/selectUnflow/select",
		async : true,
		success : function(data) {
			// 处理进度条
			if (loading)
				process.close();
			loading = false;

			var result = data.data;
			unflowList = result;
			for (var i = 0; i < result.length; i++) {
				$("#queryList ul").append(
					$("<li id='"
						+ result[i].id
						+ "'><span class='icon'></span>"
						+ result[i].name
						+ "</li>").attr("info",
					JSON.stringify(result[i])));
			}
			$("#queryList ul").delegate(
					"li",
					"click",
					function() {
						$(this).siblings().removeClass('active').end()
								.addClass('active');
					});
			if (param && param.id) { // 激活当前项
				$('#' + param.id).trigger('click');
			}
		}
	});
}

// 搜索
function search() {
	var searchValue = $("#search_query").val();
	$("#queryList ul").empty();
	for (var i = 0; i < unflowList.length; i++) {
		if (unflowList[i].name.indexOf(searchValue) != -1) {
			$("#queryList ul").append(
					$("<li><span class='icon'></span>"
					+ unflowList[i].name + "</li>").attr(
					"info", JSON.stringify(unflowList[i])));
		}
	}
}

// 返回参数获取
function getResult() {
	return JSON.parse($("#queryList ul").find(".active").attr("info")
			|| "{}");
}

// --------------------以下为固定方法，需要实现返回参数获取--------------

// 获取弹窗传递过来的参数
function initParam() {
	var obj = window.parentDialogObj && (window.parentDialogObj["ctrlDialog"]);// 获取窗口对象
	if (obj && obj.getTransParams) {
		// 然后通过V5方法获取弹窗传递过来的参数
		return obj.getTransParams();
	}
}

// 确定按钮调用方法，返回需要的json数据
function OK() {
	var result = getResult();
	console.log(JSON.stringify(result));
	if ($.isEmptyObject(result))
		return {
			valid : true,
			data : null
		};

	if (param && param.designId !== result.designId)
		return {
			valid : true,
			data : {
				customParam : {
					templateId : result,
					mapping : null
				}
			}
		};

	return {
		valid : true,
		data : result
	};
}