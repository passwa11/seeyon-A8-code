
function hello(){
	var confirm = $.confirm({
        'msg': '此操作将给所有用户发送提示消息，确认吗？',
        //绑定自定义事件
        ok_fn: function () { 
        	$.ajax({
    			type: 'get',
    			async: true,
    			// 记得加随机数，不然如果ajax轮询请求会不执行
    			url: encodeURI('/seeyon/ajaxTest.do?datetime=' + Math.random()),
    			dataType: 'json',
    			contentType: 'application/json',
    			success: function (res) {
    				$.alert(res.msg);
    				if(res.success) {
    	        		$("#mytable").ajaxgridLoad();
    				} 
    			}
    		});
    	},
        cancel_fn:function(){
        	
        	var param = new Object();
        	
        	param.msg = "hello";
        	
        	callBackendMethod("helloAjaxManager","hello", 99, param, {
        		success:function(returnVal){
        			$.alert(returnVal.msg + "," + returnVal.key + "!");
                }
        	});
        	
        	// return;
		}
    });
}
