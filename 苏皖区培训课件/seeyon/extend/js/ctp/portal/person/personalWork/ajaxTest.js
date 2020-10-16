$.ajax({
	type : 'get',
	async : true,
	// 记得加随机数，不然如果ajax轮询请求会不执行
	url : encodeURI('/seeyon/ajaxTest.do?datetime=' + Math.random()),
	dataType : 'json',
	contentType : 'application/json',
	success : function(res) {
		$.alert(res.success + ":44654564" + res.msg);
	}
});