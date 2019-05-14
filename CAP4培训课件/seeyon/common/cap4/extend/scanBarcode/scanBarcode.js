var api;

$(document).ready(function(){
	// 全局的api  操作所有的内容都需要用到这个
	api = window.thirdPartyFormAPI;
	
	// 事件注册
	api.registerEvent('test', function(params){
		$.alert(params);
	});
	
	// 清理不合理的数据  如果 库存数量小的  就直接删除
	api.registerEvent('afterFormFieldChange', function(params){
		var name = params.display;
		if(name != "领用数量") {
			return;
		}
		var recordId = params.recordId;
		var formdata = api.getFormData();
		var formson = formdata.formsons.front_formson_1;
		// 后台去获取 判断表名取数据
		var subdatas = formson.records;
		// 库存数量和领用数量
		var kc, ly, name;
		// 明细表和序号
		var sub, index;
		// 遍历校验 判断是哪一行的数据
		for(var i = 0; i <  subdatas.length; i++) {
			var arr = subdatas[i];
			if(arr.recordId == recordId) {
				sub = arr;
				index = Number(i);
			}
		}
		var fields = sub.lists;
		// 可以从后台获取表单的字段名 也可以前台进行解析
		var kcfield = 'field0013';
		kc = fields[kcfield].value;
		ly = fields.field0007.value;
		if(Number(kc) < Number(ly)) {
			name = fields.field0005.value;
			var confirm = $.confirm({
		        'msg': name + "库存数量不足 , 是否删除本行？",
		        ok_fn: function () { 
		        	var params = {};
					params.tableName = formson.tableName;
					// 是否在明细表中选中行删除
					params.isFormRecords = false;
					var tbName = formson.tableName;
					var chooseRecords = {};
					chooseRecords[tbName] = index;
					params.chooseRecords = chooseRecords;
					// 回调函数
					params.callbackFn = function() {
						$.alert("已经删除" + name + "的领用数据");
					}
					api.deleteFormsonRecods(params);
	        	},
		        cancel_fn : function() { 
		        	//$.alert('请修改' + name + "的领用数量！");
		        	params.value = kc;
		        	// 自定义事件的触发
		        	api.triggerEvent('test', "123");
	        	}
		    });
		}
		
	});
});