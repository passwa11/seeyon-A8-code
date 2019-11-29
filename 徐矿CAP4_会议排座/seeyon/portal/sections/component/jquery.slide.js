//# sourceURL=/seeyon/portal/sections/component/jquery.slide.js
/**
 * @author liaojl
 * @Description slide图片切换组件
 * @width 外层区域width 例如:300  默认 '100%'
 * @height 外层区域height 例如：120
 * @slidePage 是否显示上一张、下一张按钮 true/false
 * @slideRemove 是否显示移出栏目按钮
 * @slideNum 受否显示当前帧序号 true/false
 * @auto 是否自动切换 true/false
 * @delayTime 自动切换延迟时间  例如：3000
 * @triggerType 鼠标移到序号上切换图片的触发方式   例如：‘click’,'hover'
 * @tabIndex 默认显示对应索引值  默认第一张 0
 * @effect 切换效果  fade(渐隐 不传参数默认), horizontal(水平滚动),vertical(垂直滚动)
 * @slideTitle 是否显示标题
 * @clearInterval 是否需要缓存定时器对象到vPortal.spaceInterval，供dom移出时候清除定时器用，一般用于空间栏目等会动态变化dom结构   false/true
 */
// JavaScript Document
var commonslide = function(_options,_element){

	//合并参数
	this.id = _options.id;
	this.width = _options.width || '100%';
	this.height = _options.height || 200;
	this.slidePage = _options.slidePage == undefined ? true : _options.slidePage;
	this.slideRemove = _options.slideRemove == undefined ? false : _options.slideRemove;
	this.sectionPanelId = _options.sectionPanelId == undefined ? '' : _options.sectionPanelId;
	this.slideNum = _options.slideNum == undefined ? true : _options.slideNum;
	this.auto = _options.auto == undefined ? true : _options.auto;
	this.delayTime = _options.auto == undefined ? 3000 : _options.delayTime;
	this.triggerType = _options.triggerType == undefined ? 'click' : _options.triggerType;
	this.tabIndex = _options.tabIndex == undefined ? 0 : _options.tabIndex;
	this.effect = _options.effect == undefined ? 'fade' : _options.effect;
	this.slideTitle = _options.slideTitle == undefined ? true : _options.slideTitle;
	this.clearInterval = _options.clearInterval == undefined ? false : _options.clearInterval;
	this.slidelength = _options.slidelength == undefined ?$("#"+this.id+" .slideImgs li").length:parseInt(_options.slidelength); //图片的个数 pyy 添加自定义

	$element = _element;
	if(this.width == '100%'){//如果是100%转换为px
		this.width = $element.parent().width();
	}
	if(this.height == '100%'){//如果是100%转换为px
		this.height = $element.parent().height();
	}

	this.init();


	//配了自动播放且大于一张才开启定时器
	if (this.auto == true && this.slidelength > 1) {
		this.showThis(this.tabIndex);
		this.autoPlay();
	}
	return this;
}

commonslide.prototype.init = function(){

	var self = this;
	//初始化
	if (this.effect != "vertical") {
		$("#"+this.id).addClass("pictureRotationBottom");
	} else {
		$("#"+this.id).addClass("pictureRotationLeft");
	}
	if ($("#"+this.id+" .slideNum")) {
		$("#"+this.id+" .slideNum").remove();
	}

	$("#"+this.id).css({
		"width": this.width,
		"height": this.height,
		"overflow": "hidden",
		"position": "relative"
	});

	$("#"+this.id+" .slideImgs li").css({
		"width": this.width,
		"height": this.height
	});



	switch (this.effect) {
		case 'horizontal': //水平
			$("#"+this.id+" .slideImgs").css({
				"width": this.width * this.slidelength,
				"height": this.height
			});
			$("#"+this.id+" .slideImgs li").css({
				"float": "left"
			});
			break;
		case 'vertical': //垂直
			$("#"+this.id+" .slideImgs").css({
				"width": "100%",
				"height": this.height * this.slidelength
			});
			break;
		default: //默认
			//do nothing
			break;
	}

	if (this.slideNum && this.slidelength > 1 && this.effect == "vertical") {
		if (this.triggerType == "click") {
			$("#"+this.id+" .slideTitles div").click(function() {
				self.showThis($(this).index());
			})
		} else if (this.triggerType == "div") {
			$("#"+this.id+" .slideTitles span").mouseover(function() {
				self.showThis($(this).index());
			})
		}
	} else if (this.slideNum && this.slidelength > 1) {
		var slideNumdiv = new StringBuffer();
		slideNumdiv.append('<div class="slideNum">');
		for (i = 0; i < this.slidelength; i++) {
			slideNumdiv.append('<span>'+(i+1)+'</span>')
		}
		slideNumdiv.append("</div>");
		slideNumdiv = slideNumdiv.toString();
		$("#"+this.id).append(slideNumdiv);
		slideNumdiv = null;

		if (this.triggerType == "click") {
			$("#"+this.id+" .slideNum span").click(function() {
				self.showThis($(this).index());
			})
		} else if (this.triggerType == "hover") {
			$("#"+this.id+" .slideNum span").mouseover(function() {
				self.showThis($(this).index());
			})
		}
	}
	if (this.slideTitle) {
		$("#"+this.id+' .slideTitles p').css("paddingRight", $("#"+this.id+' .slideNum').width() + 20);
		$("#"+this.id+' .slideTitles').show();
		if (this.slidelength == 1 && $("#"+this.id+' .slideTitles p:first').text()=="") {
			$("#"+this.id+' .slideTitles').hide();
		}
	}


	if (this.slidePage && this.slidelength > 1) {
		if (this.effect == "vertical") {
			$("#"+this.id).append('<span class="showPrev pageBtnVertical hidden">&and;</span><span class="showNext pageBtnVertical hidden">&or;</span>');
		} else {
			$("#"+this.id).append('<span class="showPrev pageBtnHorizontal hidden">&lt;</span><span class="showNext pageBtnHorizontal hidden">&gt;</span>');
		}
		$("#"+this.id+" .showPrev").on('click', function() {
			self.showPrev();
		});
		$("#"+this.id+" .showNext").on('click', function() {
			self.showNext();
		});
		$("#"+this.id).hover(function() {
			$(this).find(".showPrev,.showNext").show();
		}, function() {
			$(this).find(".showPrev,.showNext").hide();
		})
	}

	if (this.slideRemove && this.sectionPanelId) {
		//增加关闭功能(移除栏目)
		$("#"+this.id).append('<i onclick=\"javascript:sectionOperation(\''+this.sectionPanelId+'\',\'deleteFragment\')\" title=\"'+$.i18n('portal.button.sectionDel')+'" class=\"vportal vp-clear close_slideImgs\"></i>');

		$("#"+this.id).hover(function() {
			$("#"+this.id+" .close_slideImgs").show();
		}, function() {
			$("#"+this.id+" .close_slideImgs").hide();
		});
	}

	//配了自动播放且大于一张才绑定这个事件
	if (this.auto == true && this.slidelength > 1) {
		$("#"+this.id).mouseover(function() { //鼠标移动到幻灯片上，停止自动播放
			self.stopAuto();
		}).mouseout(function() {
			self.autoPlay();
		});
	}
}

commonslide.prototype.autoPlay = function(){
	var self = this;
	if(this.clearInterval){
		//栏目里面需要配置该参数
		//空间里面的slide 缓存定时器到vPortal.spaceInterval  dom移出时候需要清除，避免内存泄漏
		vPortal.spaceInterval["slide" + this.id] = setInterval(function() {
			self.showTrain();
		}, 1);
	}else{
		//自动播放
		this.slideInterval = setInterval(function() {
			self.showTrain();
		}, 1);
	}

}

commonslide.prototype.showTrain = function(){
	//跑马灯
	var thisIndexNum = $("#"+this.id+" .slideImgs li.active").index();
	if (thisIndexNum == (this.slidelength - 1)) {
		thisIndexNum = 0;
	} else {
		thisIndexNum = thisIndexNum + 1
	}
	this.showThisTrain(thisIndexNum);
}


commonslide.prototype.showPrev = function(){
	//上一个
	var thisIndexNum = $("#"+this.id+" .slideImgs li.active").index();
	if (thisIndexNum == 0) {
		thisIndexNum = this.slidelength - 1;
	} else {
		thisIndexNum = thisIndexNum - 1
	}
	this.showThis(thisIndexNum);
}
commonslide.prototype.showNext = function(){
	//下一个
	var thisIndexNum = $("#"+this.id+" .slideImgs li.active").index();
	if (thisIndexNum == (this.slidelength - 1)) {
		thisIndexNum = 0;
	} else {
		thisIndexNum = thisIndexNum + 1
	}
	this.showThis(thisIndexNum);
}

commonslide.prototype.stopAuto = function(){
	if(this.clearInterval){//栏目里面需要配置该参数
		clearInterval(vPortal.spaceInterval["slide" + this.id]);
		delete vPortal.spaceInterval["slide" + this.id];
	}else{
		clearInterval(this.slideInterval);
	}
}

commonslide.prototype.showThisTrain = function(num){
	//显示指定索引值的图片
	if (!$("#"+this.id+" .slideImgs").is(':animated') && !$("#"+this.id+" .slideImgs li").is(':animated')) { //正在运动的时候点击无效，防止多次点击
	
		//alert("dalay:"+this.delayTime);
		
		switch (this.effect) {
			case 'horizontal': //
			
				var thisLeft = this.width * num;
				
				if(0==num || this.slidelength - 1==num){
					$("#"+this.id+" ul").animate({marginLeft: -thisLeft},2,function(){
					
					});
				}else{
					$("#"+this.id+" ul").animate({marginLeft: -thisLeft},this.delayTime,function(){
					
					});
				}
				
				

				$("#"+this.id+" .slideImgs li").eq(num).addClass('active').siblings().removeClass('active');				
				$("#"+this.id+" .slideNum span").eq(num).addClass('active').siblings().removeClass('active');
				if ($("#"+this.id+" .slideTitles p").eq(num).text()=="") {
					$("#"+this.id+" .slideTitles").hide();
				} else {
					$("#"+this.id+" .slideTitles").show();
					$("#"+this.id+" .slideTitles p").eq(num).fadeIn().siblings().hide();
				}
								
				break;	
		}
	}
}





commonslide.prototype.showThis = function(num){
	//显示指定索引值的图片
	if (!$("#"+this.id+" .slideImgs").is(':animated') && !$("#"+this.id+" .slideImgs li").is(':animated')) { //正在运动的时候点击无效，防止多次点击
	
		
		
		switch (this.effect) {
			case 'horizontal': //水平滚动
				var thisLeft = this.width * num;
				//alert("width:"+this.width);
				
				/**
				$("#"+this.id+" ul").animate({marginLeft:this.width},600, function () {  
					$("#"+this.id+" ul").css('marginLeft','0px');  
				});  
				**/
				
				if(0==num || this.slidelength - 1==num){
					$("#"+this.id+" ul").animate({marginLeft: -thisLeft},2,function(){
					
					});
				}else{
					$("#"+this.id+" ul").animate({marginLeft: -thisLeft},600,function(){
					
				});
				}
				

				$("#"+this.id+" .slideImgs li").eq(num).addClass('active').siblings().removeClass('active');				
				$("#"+this.id+" .slideNum span").eq(num).addClass('active').siblings().removeClass('active');
				if ($("#"+this.id+" .slideTitles p").eq(num).text()=="") {
					$("#"+this.id+" .slideTitles").hide();
				} else {
					$("#"+this.id+" .slideTitles").show();
					$("#"+this.id+" .slideTitles p").eq(num).fadeIn().siblings().hide();
				}
								
				break;
			case 'vertical': //垂直滚动
				var thisTop = this.height * num;
				$("#"+this.id+"  .slideImgs").animate({
					top: -thisTop
				});
				$("#"+this.id+" .slideImgs li").eq(num).addClass('active').siblings().removeClass('active');
				$("#"+this.id+" .slideNum span").eq(num).addClass('active').siblings().removeClass('active');
				if ($("#"+this.id+" .slideTitles div").eq(num).text()=="") {
					$("#"+this.id+" .slideTitles").removeClass("current");
				} else {
					$("#"+this.id+" .slideTitles").show();
					$("#"+this.id+" .slideTitles div").eq(num).addClass("current").siblings().removeClass("current");
				}
				break;
			default: //默认 渐隐切换
				$("#"+this.id+"  .slideImgs li").eq(num).addClass('active').fadeIn().siblings().hide().removeClass('active');
				$("#"+this.id+"  .slideNum span").eq(num).addClass('active').siblings().removeClass('active');
				if ($("#"+this.id+" .slideTitles p").eq(num).text()=="") {
					$("#"+this.id+" .slideTitles").hide();
				} else {
					$("#"+this.id+" .slideTitles").show();
					$("#"+this.id+" .slideTitles p").eq(num).fadeIn().siblings().hide();
				}
				break;
		}
	}
}

$.fn.slide = function(options) {
	// debugger;
	if($(this).attr("id") != "undefined"){
		options.id = $(this).attr("id");
	}else{
		//没有ID的话生成一个ID，并赋值
		options.id = "slider_" + Math.floor(Math.random() * 100000000);
		$(this).attr("id",this.id);
	}
	if( typeof(window['commonslide'+options.id]) != "undefined" ){
		window['commonslide'+options.id].stopAuto();
		window['commonslide'+options.id] = null;
	}
	window['commonslide'+options.id] = new commonslide(options,$(this));
}

