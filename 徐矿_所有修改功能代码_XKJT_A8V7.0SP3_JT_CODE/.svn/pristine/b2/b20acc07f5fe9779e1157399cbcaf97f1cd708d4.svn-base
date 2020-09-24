function doSearch2(){
	var flag = true;
	var recieveDate = document.getElementById("recieveDateDiv");
	
	if(recieveDate && recieveDate.style.display == "block"){
		var begin = document.getElementById("textfield").value;
		var end = document.getElementById("textfield1").value;
		flag = timeValidate(begin,end);
	}

	var registerDate = document.getElementById("registerDateDiv");
	if(registerDate && registerDate.style.display == "block"){
		var begin = document.getElementById("textfield").value;
		var end = document.getElementById("textfield1").value;
		flag = timeValidate(begin,end);
	}
	var registerDate = document.getElementById("deadlineDatetimeDiv");
	if(registerDate && registerDate.style.display == "block"){
		var begin = document.getElementById("deadlineDateBegin").value;
		var end = document.getElementById("deadlineDateEnd").value;
		flag = timeValidate(begin,end);
	}
	if(flag){
		/** 打开进度条 */
		//try { getA8Top().startProc(); } catch(e) {}
		doSearch();
	}
}