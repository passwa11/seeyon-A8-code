//# sourceURL=\seeyon\portal\sections\component\dateCalender.js

var dateCalender = function(ele,args) {
	this.ele = ele;

	//先把年存起来，计算闰年
	this.tempy = args.options.y;
	var t = this;
	
	this.options= ({
		// y:date.getFullYear(),
		// m:date.getMonth()+1,
		// d:date.getDate(),
		//这里是由传入进来的日期渲染....
		y:args.options.y,
		m:args.options.m,
		d:args.options.d,
		type: "week",
		yText:' / ',
		mText:'',
		dText:'',
		wTextB:'第',
		wTextE:'周',
		nText:'今天',
		wArr:[$.i18n("calendar.week.sunday"),$.i18n("calendar.week.monday"),$.i18n("calendar.week.tuesday"),$.i18n("calendar.week.wednesday"),$.i18n("calendar.week.thursday"),$.i18n("calendar.week.friday"),$.i18n("calendar.week.saturday")],
		mArr:[0,31, 28+this.is_leap(t.tempy), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],
		mi18nArr:[$.i18n("calendar.month.jan"),$.i18n("calendar.month.feb"),$.i18n("calendar.month.mar"),$.i18n("calendar.month.apr"),$.i18n("calendar.month.may"),$.i18n("calendar.month.june"),$.i18n("calendar.month.july"),$.i18n("calendar.month.aug"),$.i18n("calendar.month.sep"),$.i18n("calendar.month.oct"),$.i18n("calendar.month.nov"),$.i18n("calendar.month.nov")]
	});


	//把存进来的参数存起来，后面点击日期刷新数据LIST的时候会用到
	args._source?this._source = args._source:"";
	args._state?this._state = args._state:"";

	args._pageName?this._pageName = args._pageName:"";
	args._pageOrgId?this._pageOrgId = args._pageOrgId:"";

	args._calendarMap?this._calendarMap = args._calendarMap:"";
	args._sectionId?this._sectionId = args._sectionId:"";

	if(args.type){
		this.options.type = args.type;
	}

	if(args._sectionWidth){
		this._sectionWidth = args._sectionWidth;
	}

	//今天
	this.options.nowDate = this.dateStr(this.options.y,this.options.m,this.options.d);

	this.init();
	// this.initEvent();
	// this.loadData();
}
//是否为闰年
dateCalender.prototype.is_leap=function(year){
	return (year%100==0?res=(year%400==0?1:0):res=(year%4==0?1:0));
}

//时间格式
dateCalender.prototype.dateStr=function(y,m,d){
	m=m<10?"0"+m:m;
	d=d<10?"0"+d:d;
	var dateStr=y+"-"+m+"-"+d;
	return dateStr;
};

dateCalender.prototype.goBaseToday = function(){
	if(this.tempType == "week" ){
		//周视图返回当天
		this.weekRender(this.options.y,this.options.m,this.options.d);
	}else{
		//月视图返回当天
		this.monthRender(this.options.y,this.options.m,this.options.d);
	}


}

dateCalender.prototype.init = function(){

	var _allHtml = new StringBuffer();
	_allHtml.append('<div class="cal_head"><span id="'+this.ele+'_nowDayBig" class="nowDayBig" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].goBaseToday();"  >'+(this.options.d<10?'0'+this.options.d:this.options.d)+'</span>');
	_allHtml.append('<span class="cal_head_right">');
	_allHtml.append('<span class="cal_prev" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].prev();"> < </span>');
	if(vPortal.locale != "zh_CN" && vPortal.locale != "zh_TW"){
		_allHtml.append('<span class="cal_date" id="'+this.ele+'_m_date" >' + this.options.y + this.options.yText + this.options.m + this.options.mText +'</span>');
	}else{
		_allHtml.append('<span class="cal_date" id="'+this.ele+'_m_date" >' + this.options.y + this.options.yText + this.options.m + '</span>');
	}
	_allHtml.append('<span class="cal_next" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].next();"> > </span></span></div>');

	_allHtml.append('<table width = "100%">');
	//表头
	_allHtml.append('<thead><tr>');
	for(var i=0,len=this.options.wArr.length;i<len;i++){
		_allHtml.append('<th>'+this.options.wArr[i]+'</th>');
	}
	_allHtml.append('</tr></thead>');
	_allHtml.append('</table>');

	_allHtml.append('<div id="'+ this.ele +'_body"></div>');

	var obj = document.getElementById(this.ele);
	obj.innerHTML =  _allHtml.toString();

	if(this.options.type == "week"){
		// console.log('周视图');
		this.tempType = "week";
		this.weekRender(this.options.y,this.options.m,this.options.d,"switchView");
	}else{
		// console.log('月视图');
		this.tempType = "mouth";
		this.monthRender(this.options.y,this.options.m,this.options.d,"switchView");
	}

	this.clicktag = 0;


};


// 左箭头
dateCalender.prototype.prev = function(){
	if(this.clicktag == 0){
		this.clicktag = 1;
		//上一月
		if(this.tempType == "mouth"){
			var rightM = this.tempm-1;
			var rightY = this.tempy;
			if(rightM<1){
				rightM = 12;
				rightY = this.tempy -1;
			}
			this.monthRender(rightY,rightM,1);
		}else{//上一周
			var rightY = this.tempy;
			var rightM = this.tempm;
			var rightD = this.tempd - this.tempSelectedDay - 7;

			if(rightD<=0){
				if(this.tempm == 1){
					rightY = this.tempy - 1;
					rightM = 12;
				}else{
					rightM = this.tempm-1;
				}
				rightD =  (this.options.mArr[rightM] - Math.abs(rightD));
			}
			this.weekRender(rightY,rightM,rightD);
		}

		that = this;
		setTimeout(function () {
			that.clicktag = 0 ;
		}, 300);
	}else {
		// console.log("你点击太快了.........");
	}



}

// 右箭头
dateCalender.prototype.next = function(){
	if(this.clicktag == 0){
		this.clicktag = 1;

		//下一月
		if(this.tempType == "mouth"){
			var rightM = this.tempm+1;
			var rightY = this.tempy;
			if(rightM>12){
				rightM = 1;
				rightY = this.tempy +1;
			}
			this.monthRender(rightY,rightM,1);
		}else{//下一周
			var rightY = this.tempy;
			var rightM = this.tempm;
			var rightD = this.tempd - this.tempSelectedDay + 7;

			if(rightD<=0){
				if(this.tempm == 1){
					rightY = this.tempy - 1;
					rightM = 12;
				}else{
					rightM = this.tempm-1;
				}
				rightD =  (this.options.mArr[rightM] - Math.abs(rightD));
			}else if(rightD>this.options.mArr[rightM]) {

				if(this.tempm == 12){
					rightY = this.tempy + 1;
					rightM = 1;
					rightD = rightD-this.options.mArr[this.tempm];
				}else{
					rightM = this.tempm+1;
					rightD = rightD-this.options.mArr[rightM-1];
				}

			}
			this.weekRender(rightY,rightM,rightD);
		}


		that = this;
		setTimeout(function () {
			that.clicktag = 0 ;
		}, 300); 
	}else{
		// console.log("你点击太快了.........");
	}

}

// 减少 一个月
dateCalender.prototype.getRightYearAndMonthS=function(){

	var rightYear = this.tempy;
	var rightMonth;
	if(this.tempm == 1){
		rightYear = this.tempy - 1;
		rightMonth = 12;
	}else{
		rightMonth = this.tempm - 1
	}
	var tempParam = [];
	tempParam.push(rightYear);
	tempParam.push(rightMonth);
	return tempParam;
}


// 增加 一个月   适应场景   渲染下一月
dateCalender.prototype.getRightYearAndMonthB=function(){

	var rightYear = this.tempy;
	var rightMonth;
	if(this.tempm == 12){
		rightYear = this.tempy + 1;
		rightMonth = 1;
	}else{
		rightMonth = this.tempm + 1
	}
	var tempParam = [];
	tempParam.push(rightYear);
	tempParam.push(rightMonth);
	return tempParam;
}



dateCalender.prototype.monthRender=function(y,m,d,type){
	this.tempy = y;
	this.tempm = m;
	this.tempd = d;

	//更新年、月
	var tempmI18n = this.options.mi18nArr[this.tempm-1];
	if(vPortal.locale != "zh_CN" && vPortal.locale != "zh_TW"){
		document.getElementById(this.ele+"_m_date").innerHTML =  tempmI18n + ' ' + this.tempy;
	}else{
		document.getElementById(this.ele+"_m_date").innerHTML =  this.tempy + this.options.yText + this.tempm + this.options.mText ;
	}
	//更新切换视图按钮的Class
	if(this._sectionWidth>=600){
		document.getElementById(this.ele+"_switchView").className = "dateCalender_switchView collapseIcon hidden";
	}else{
		document.getElementById(this.ele+"_switchView").className = "dateCalender_switchView collapseIcon";
	}
	document.getElementById(this.ele+"_switchView").innerHTML = "<i class='vPortal vp-arrow-up-serif'></i>";

	var n1str = new Date(this.tempy,this.tempm-1,1); //当月第一天Date资讯

	var tempFirstday = n1str.getDay(); //当月第一天星期几
	var trNum = Math.ceil((this.options.mArr[this.tempm] + tempFirstday)/7); //表格所需要行数

	var cHtml = new StringBuffer();
	cHtml.append('<table width = "100%"><tbody>');
	for(i=0;i<trNum;i++) { //表格的行
		cHtml.append('<tr class="week">');
		var idx,date_str,eventClass;
	   for(k=0;k<7;k++) {//表格每行的单元格
	      idx=i*7+k; //单元格自然序列号
	      date_str=idx-tempFirstday+1; //计算日期


	      cHtml.append('<td><span ');
	      //点击上一月、下一月的时候，选择第一天
	      if(this.tempm != this.tempm && date_str ==1) {

	      	var dataDate = this.dateStr(this.tempy,this.tempm,date_str);
	      	eventClass = (typeof(this._calendarMap[dataDate]) !="undefined" && this._calendarMap[dataDate] == "yes")?"hasEvent":"";
	      	var date_strWithTag = ' class="selectedDay '+eventClass+'" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" data-date="'+dataDate+'"><i class="notThisM">'+ date_str + '</i>';
	      	this.tempNowDate = this.dateStr(this.tempy,this.tempm,date_str);


	      	var n2str = new Date(this.tempy,this.tempm-1,date_str); //获取当前选中Date资讯
			this.tempSelectedDay = n2str.getDay(); //获取当前选中的是星期几
	      }else if(date_str == this.tempd && this.tempm == this.tempm) {//当前日期

	      	var dataDate = this.dateStr(this.tempy,this.tempm,date_str);
	      	eventClass = (typeof(this._calendarMap[dataDate]) !="undefined" && this._calendarMap[dataDate] == "yes")?"hasEvent":"";
	      	var date_strWithTag = ' class="selectedDay nowDay '+eventClass+'" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" data-date="'+dataDate+'">'+ date_str;
	      	this.tempNowDate = this.dateStr(this.tempy,this.tempm,date_str);

	      	var n2str = new Date(this.tempy,this.tempm-1,date_str); //获取当前选中Date资讯
			this.tempSelectedDay = n2str.getDay(); //获取当前选中的是星期几
	      }else{
	      	  //过滤无效日期（小于等于零的）
		      if(date_str<=0) {
		      	var tempParam = this.getRightYearAndMonthS();
		      	date_str =  (this.options.mArr[tempParam[1]] - Math.abs(date_str));
		      	var dataDate = this.dateStr(tempParam[0],tempParam[1],date_str);

		      	var date_strWithTag = ' class="defaultCursor" data-date="'+dataDate+'"><i class="notThisM">'+ date_str + '</i>';
		      }else if(date_str>this.options.mArr[this.tempm]){//过滤无效日期（大于月总天数的）
		      	date_str = (date_str - this.options.mArr[this.tempm]);

		      	var tempParam = this.getRightYearAndMonthB();
		      	var dataDate = this.dateStr(tempParam[0],tempParam[1],date_str);
		      	var date_strWithTag = ' class="defaultCursor" data-date="'+dataDate+'"><i class="notThisM">'+ date_str + '</i>';
		      }else{
		      	var dataDate = this.dateStr(this.tempy,this.tempm,date_str);
		      	eventClass = (typeof(this._calendarMap[dataDate]) !="undefined" && this._calendarMap[dataDate] == "yes")?"hasEvent":"";
//		      	//切换之后，切回到当天的月的时候，优先选择当天
//		      	if(this.options.nowDate == dataDate){
//		      		cHtml = cHtml.replace("selectedDay"," ");
//		      		eventClass = eventClass +" selectedDay";
//		      		this.tempNowDate = dataDate;
//		      	}
		      	var date_strWithTag = ' class="'+eventClass+'" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" data-date="'+dataDate+'">'+date_str;
		      }
	      }
	      cHtml.append(date_strWithTag + '</span></td>');

	   }
	   eventClass = "";
	   cHtml.append('</tr>');//表格的行结束
	}
	cHtml.append('</tbody></table>');

	var obj = document.getElementById(this.ele +'_body');
	if(obj == null) return;
	obj.innerHTML =  cHtml.toString();
	this.alignCenterDate();

	//非默认渲染，点上下月或者周视图、月视图切换的时候
	if(this.tempTrNum){
		//当行数和点击之前的行数不一样的时候，动态修改下之前的高度【并且只要窄栏的时候才需要.....】
		if(trNum != this.tempTrNum && this._sectionWidth<600){
			this.fixCalenderHeight(trNum,this.tempTrNum);
			this.tempTrNum = trNum;
		}
	}else if(type == "switchView" && this._sectionWidth<600){
		if(trNum == 6){
			this.fixCalenderHeight(6,5);
		}
		//切换的时候也要赋值
		this.tempTrNum = trNum;
	}else{
		//默认渲染的时候只是赋值
		this.tempTrNum = trNum;
	}

	//切换视图的时候不重新加载数据...
	if(!(type == "switchView")){
		this.loadData();
	}

};


dateCalender.prototype.weekRender=function(y,m,d,type){


	this.tempy = y;
	this.tempm = m;

	if(typeof(this.tempd) == "undefined"){
		this.tempd = this.options.d;
	}else{
		this.tempd = d;
	}

	var tempm_1 = this.tempm-1<0?11:this.tempm-1;

	var n1str = new Date(this.tempy,tempm_1,1); //当月第一天Date资讯
	var tempFirstday = n1str.getDay(); //当月第一天星期几

	var beginTrNum = parseInt((tempFirstday + this.tempd)/7);

	//刚好整除的时候代表是行的最后一列，这里应该渲染当行
	if ((tempFirstday + this.tempd) % 7 == 0) {
		beginTrNum = beginTrNum -1;
	}

	//更新切换视图按钮的Class
	document.getElementById(this.ele+"_switchView").className = "dateCalender_switchView expandIcon";
	document.getElementById(this.ele+"_switchView").innerHTML = "<i class='vPortal vp-arrow-down-serif'></i>";
	var cHtml = new StringBuffer();
	cHtml.append('<table width = "100%"><tbody>');
	   cHtml.append("<tr class='week'>");
	   var idx,date_str,eventClass;
	   for(k=0;k<7;k++) { //表格每行的单元格
	      idx=beginTrNum*7+k; //单元格自然序列号
	      date_str=idx-tempFirstday+1; //计算日期

	       cHtml.append('<td><span ');

	      if(date_str == this.tempd && date_str<=this.options.mArr[this.tempm]) {
	      	var dataDate = this.dateStr(this.tempy,this.tempm,date_str);
	      	eventClass = (typeof(this._calendarMap[dataDate]) !="undefined" && this._calendarMap[dataDate] == "yes")?"hasEvent":""

	      	var	date_strWithTag = ' onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" id="'+this.ele+'_d'+date_str+'" data-date="'+dataDate+'" class="selectedDay '+eventClass+'">'+date_str;
	      	this.tempNowDate = this.dateStr(this.tempy,this.tempm,date_str);

	      	var n2str = new Date(this.tempy,this.tempm-1,date_str); //获取当前选中Date资讯
	   		this.tempSelectedDay = n2str.getDay(); //获取当前选中的是星期几
	      }else{
	      	if(date_str<=0) {
	      		date_str =  (this.options.mArr[this.tempm] - Math.abs(date_str));
		      	var tempParam = this.getRightYearAndMonthS();
		      	var dataDate = this.dateStr(tempParam[0],tempParam[1],date_str);

		      	var date_strWithTag = ' onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" id="'+this.ele+'_ld'+date_str+'" data-date="'+dataDate+'">'+ date_str;
	      	}else if(date_str>this.options.mArr[this.tempm]){//过滤无效日期（大于月总天数的）
	      		date_str = (date_str - this.options.mArr[this.tempm]);

	      		var tempParam = this.getRightYearAndMonthB();
		      	var dataDate = this.dateStr(tempParam[0],tempParam[1],date_str);
		      	var date_strWithTag = ' onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" id="'+this.ele+'_nd'+date_str+'" data-date="'+dataDate+'">'+ date_str;
	      	}else{

		      	var dataDate = this.dateStr(this.tempy,this.tempm,date_str);
		      	eventClass = (typeof(this._calendarMap[dataDate]) !="undefined" && this._calendarMap[dataDate] == "yes")?"hasEvent":"";

		      	// //切换之后，切回到当天的周的时候，优先选择当天
		      	// if(this.options.nowDate == dataDate){
		      	// 	cHtml = cHtml.replace("selectedDay"," ");
		      	// 	eventClass = eventClass +" selectedDay"
		      	// }
				var	date_strWithTag = ' class="'+eventClass+'" onclick="javascript:vPortal.sectionHandler.calendarForWeekOrMonthTemplete[\''+this.ele+'\'].loadDateCalenderData(\''+dataDate+'\',\''+this.ele+'\',this);" id="'+this.ele+'_d'+date_str+'" data-date="'+dataDate+'">'+date_str;
	      	}
	      }
	      cHtml.append(date_strWithTag + '</span></td>');

	   }
	   eventClass = "";
	   cHtml.append("</tr>"); //表格的行结束
	   cHtml.append('</tbody></table>');

	   var newTempNowDate = this.dateStr(this.tempy,this.weekBeginMonth,this.weekBeginday);

	   if(this.tempNowDate == newTempNowDate){
	     this.tempNowDate  = newTempNowDate;
	   }else{
	     this.tempNowDate  = this.tempNowDate;
	   }


	   var obj = document.getElementById(this.ele +'_body');
	   obj.innerHTML =  cHtml.toString();
	   this.alignCenterDate();


		//取本周第一天和最后一天，并且转换格式
		var bbbbbb = (obj.querySelectorAll("span")[0].getAttribute("data-date")).substring(5).replace("-",".");
		var eeeeee = (obj.querySelectorAll("span")[6].getAttribute("data-date")).substring(5).replace("-",".");

		document.getElementById(this.ele+"_m_date").innerHTML = bbbbbb +" - " + eeeeee;

	   var kkk = [];
	   for(i=0;i<obj.querySelectorAll("span").length;i++){
	   	kkk.push(obj.querySelectorAll("span")[i].getAttribute("data-date"));
	   }
	   // console.table(kkk);

		//切换视图的时候不重新加载数据...
		if(!(type == "switchView")){
			this.loadData();
		}

}

//设置一下当日的大数字的宽度，以便和下面的对齐....
dateCalender.prototype.alignCenterDate = function(){
	var obj = document.getElementById(this.ele +'_nowDayBig');
	fixWidth = document.getElementById(this.ele).querySelectorAll("td")[0].offsetWidth - 20;
	obj.style.width = fixWidth+"px";
}

//点击上下月可能行数不一样，这个时候fix一下高度
dateCalender.prototype.fixCalenderHeight = function(newTrNum,oldTrNum){
	var calenderObj = document.getElementById(this.ele).parentNode;
	var calenderObjHeight = calenderObj.clientHeight;
	var listObj = document.getElementById(this.ele +'List');
	var listObjHeight = listObj.clientHeight;

	if(newTrNum>oldTrNum){
		calenderObj.style.height = calenderObjHeight + 40 +"px";
		listObj.style.height = listObjHeight - 40 + "px";
	}else{
		calenderObj.style.height = calenderObjHeight - 40 +"px";
		listObj.style.height = listObjHeight + 40 + "px";
	}
}


//切换周视图、月视图
dateCalender.prototype.switchView = function(){

	//更新相关高度
	var  needComputeObj1= document.getElementById(this.ele+"_switchView").parentNode;//日历部分
	// var  needComputeObj2= document.getElementById(this.ele+"List");//日程事件LIST
	// var stepHeight = 170;//周视图和月视图的的高度差
	var isNarrow = needComputeObj1.parentNode.className.indexOf("wideCalender") != -1?false:true;//是否窄栏


	var y = this.tempy;
	var m = this.tempm;
	var d = this.tempd;

	if(this.tempType == "week"){
		//只有窄栏的时候才更新
		if(isNarrow){
			needComputeObj1.style.height = "320px";
			// needComputeObj1.style.height = needComputeObj1.clientHeight + stepHeight + "px";
			// needComputeObj2.style.height = needComputeObj2.clientHeight - stepHeight + "px";
		}

		this.monthRender(y,m,d,"switchView");
		this.tempType = "mouth";

	}else{
		//只有窄栏的时候才更新
		if(isNarrow){
			needComputeObj1.style.height = "149px";
			// needComputeObj1.style.height = needComputeObj1.clientHeight - stepHeight + "px";
			// needComputeObj2.style.height = needComputeObj2.clientHeight + stepHeight + "px";
		}

		this.weekRender(y,m,d,"switchView");
		this.tempType = "week";
	}
}

dateCalender.prototype.loadData = function(){

	// console.log("开始loadData:"+this.tempNowDate);
	this.loadDateCalenderData(this.tempNowDate,this);

}



dateCalender.prototype.loadDateCalenderData = function(chooseDate,CalenderName,thisObj){

	//点击非当前日期的需要更新当前选中的class
	if(typeof(thisObj)!="undefined"){
		vPortal.sectionHandler.calendarForWeekOrMonthTemplete[CalenderName].tempd = Number(thisObj.innerHTML);
		var tempObj = document.getElementById(CalenderName+"_body").querySelector(".selectedDay");
		tempObj.className = tempObj.className.replace(/selectedDay/g,'');
		thisObj.className = thisObj.className +" selectedDay";
	}

	//点击日期的时候需要更新tempNowDate
	if(this.tempNowDate != chooseDate){
		var tempChooseDateArr = chooseDate.split("-"); //获取当前选中Date资讯
		tempChooseDateArr[0] = Number(tempChooseDateArr[0]);
		tempChooseDateArr[1] = Number(tempChooseDateArr[1]);
		var rightYear = tempChooseDateArr[0];
		var rightMonth;
		if(tempChooseDateArr[1] == 1){
			rightYear = tempChooseDateArr[0] - 1;
			rightMonth = 12;
		}else{
			rightMonth = tempChooseDateArr[1] - 1
		}

		var tempChooseDate = new Date(rightYear,rightMonth,tempChooseDateArr[2]);
		this.tempSelectedDay = tempChooseDate.getDay();
		this.tempNowDate = chooseDate;
	}

	//需要加载相关日程
	var param = {};
	param.time = this.tempNowDate;
	param.calenderId = this.ele;

	//下面的是个参数是从D.data里面带过来，加载数据的时候需要传进去
	param.source = this._source;
	param.state  = this._state;
	param.pageName = this._pageName;
	param.orgId = this._pageOrgId;
	param.sectionId = this._sectionId;
	var dateCalender = this.ele;
	callBackendMethod("calendarPortalManager", "getCalendarByPortal", param,{
        success: function(d) {
        	var calendarMap = d.calendarMap;
        	console.log("加载"+chooseDate+"的相关日程...");
        	//把当前高亮的页签传回去，渲染模板的时候去高亮
        	var dates = document.getElementById(dateCalender).querySelectorAll(".week span");
        	for(var i = 0 ;i< dates.length;i++){
        		var dataDate = dates[i].getAttribute('data-date');
        		if(calendarMap[dataDate] != 'undefined' && calendarMap[dataDate] == 'yes'){
//        			dates[i].classList.add('hasEvent');
        			var className = dates[i].getAttribute("class");
        			dates[i].setAttribute("class",className+" "+"hasEvent");
        		}
        	}
        	var currentPageOrgId;
        	for(var i = 0;i<d.orgPages.length;i++){
        		if(d.orgPages[i].current) {
        			currentPageOrgId  = d.orgPages[i].pageOrgId;
        			break;
        		}
        	}
        	var _entityId = param.calenderId.replace(/dateCalender/g,"");
			//刷新页签上面的数字
        	vPortal.sectionHandler.calendarForWeekOrMonthTemplete.renderCalendarTab(d.orgPages,_entityId);

			//刷新下面的列表
        	vPortal.sectionHandler.calendarForWeekOrMonthTemplete.renderCalendarList(d,param.calenderId,currentPageOrgId);
        }
    });


}


