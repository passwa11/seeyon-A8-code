var urlParam;
var page = {};

/********************************** 初发化方法  ***********************************/

cmp.ready(function () {
	
	urlParam = cmp.href.getParam();
	
	initPageBack();
	
	cmp.i18n.init(_meetingPath+"/i18n/", "MeetingResources", function() {
		page = urlParam;
		initHtml();
	},meetingBuildVersion);
});

/****************************** 监听返回事件(放到最前头)  ******************************/
function initPageBack() {
    
    //cmp控制返回
    cmp.backbutton();
    cmp.backbutton.push(_goBack);
}


function _goBack() {
    cmp.href.back();
}
/********************************** 页面布局  ***********************************/

/**
 * 页面数据装载 
 */
function initHtml() {
	if(page.operate == "summary") {//来源于会议纪要
		
		_$("title").innerHTML = cmp.i18n("meeting.page.lable.actualJoin")+"("+page.memberNumber+")";
		initPageData();
	} else {
		setPageTitle(cmp.i18n("meeting.page.lable.title.summary.conferee"));
	    cmp.IMG.detect();
	    cmp.listView("#page011",{offset:{x:0,y:0},imgCache:true});
	}

}
/**
 * 页面数据装载 
 */
function initPageData() {
    
    cmp.listView("#page011",{
        offset:{x:0,y:0},
        imgCache:true,
        config: {
            params: {
                "recordId" : page.summary.id
            },
            dataFunc: function(params, options){
            	$s.Meeting.showMeetingSummaryMembers(params, {
		    		success : function(result) {
		    			if(options.success) {
		            		options.success(result);
		            	}
		            },
		            error : function(result){
		            	//处理异常
		            	MeetingUtils.dealError(result);
		            }
		        })
	        },
            isClear: false,
            renderFunc: renderData
        },
        down: {
            contentprepage:cmp.i18n("meeting.page.lable.prePage"),//上一页
            contentdown:cmp.i18n("meeting.page.action.pullDownRefresh"),//可选，在下拉可刷新状态时，下拉刷新控件上显示的标题内容
            contentover: cmp.i18n("meeting.page.action.loseRefresh"),//可选，在释放可刷新状态时，下拉刷新控件上显示的标题内容
            contentrefresh: cmp.i18n("meeting.page.state.refreshing")//可选，正在刷新状态时，下拉刷新控件上显示的标题内容
        },
        up: {
            contentnextpage:cmp.i18n("meeting.page.lable.nextPage"),//下一页
            contentdown: cmp.i18n("meeting.page.action.loadMore"),//可选，在上拉可刷新状态时，上拉刷新控件上显示的标题内容
            contentrefresh: cmp.i18n("meeting.page.state.loading"),//可选，正在加载状态时，上拉加载控件上显示的标题内容
            contentnomore: cmp.i18n("meeting.page.state.noMore")//可选，请求完毕若没有更多数据时显示的提醒内容；
        }
    });
}

function renderData(result, isRefresh){
    
    var pendingTPL = _$("#replyMemberTpl").innerHTML;
    var html = cmp.tpl(pendingTPL, result);
    var $fillArea = _$("#joinUserDiv");
    if (isRefresh) {//是否刷新操作，刷新操作 直接覆盖数据
        $fillArea.innerHTML = html;
        
    } else {
        cmp.append($fillArea,html);
    }
}

