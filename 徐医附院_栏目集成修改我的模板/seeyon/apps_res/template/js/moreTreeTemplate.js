var params = {};

$().ready(function() {
	if(selectId==''){
		$("#tree").tree({
			onClick : showTemplate,//1
			idKey : "id",
			pIdKey : "pId",
			nameKey : "name",
			title: 'name',
			nodeHandler : function(n) {
				n.isParent = true;
				if (n.data.id == "0") {
					n.open = true;
				}
			}
		});
	}else{
		$("#tree").tree({
			onClick : showTemplate_change,//1
			idKey : "id",
			pIdKey : "pId",
			nameKey : "name",
			title: 'name',
			nodeHandler : function(n) {
				n.isParent = true;
				if (n.data.id == "0") {
					n.open = false;
				}
			}
			// ,onExpand:showTemplate_change
		});
		var zTree_Menu = $.fn.zTree.getZTreeObj("tree");
		var node = zTree_Menu.getNodeByParam("id",selectId );
		zTree_Menu.selectNode(node,true);//指定选中ID的节点
		zTree_Menu.expandNode(node, true, true);//指定选中ID节点展开
		node.trigger('click',showTemplate_change('tree',node));
	}

	function showTemplate_change(treeId, node) {

		var seletedCategoryId = node.data.id;
		var childNodeList = getShowData(node);
		//清空查询条件
		$("#searchValue").val("");
		params.seletedCategoryId = seletedCategoryId;
		params.childNodeList = childNodeList;
		initSearch();
	}


    function showTemplate(e, treeId, node) {

    	var seletedCategoryId = node.data.id;
    	var childNodeList = getShowData(node);
    	//清空查询条件
    	$("#searchValue").val("");
        params.seletedCategoryId = seletedCategoryId;
        params.childNodeList = childNodeList;
        initSearch();
    }
});

$(document).ready(function () {
	//初始化此单位的全部模版
	search();
    $("#searchValue").keydown(function(event){
      if(event.keyCode === 13){
        search();
      }
    });
    getCtpTop().hideLocation();

    $("#templateListDiv").height($("#center").height());
});

function search() {
	params.seletedCategoryId = "1";
	initSearch();
}



function initSearch() {
	var tempates = page.showTemplates;//所有模板
	var showData = params.childNodeList;//显示的模板分类树
	var showTempaltes = []; //所选模板分类下面的直接模板

	if (showData == undefined) {//初始化时
		var allCategories = [];
		$.extend(true,allCategories, page.showCategorys);
		var allTemplates = [];
		$.extend(true,allTemplates, page.showTemplates);
		showData = [];
		var catagerIds = [];
		//最近使用
		var templateMan = new templateManager();
		var recentUseTempaltes = templateMan.getRecentUseTemplate(category,recent);
		var recentUseTemplateData = getFinallyShowData(-1, recentUseTempaltes);
		addShowData(showData, catagerIds, recentUseTemplateData);
		//公文模板
		var edocCategorys = getAllChildrenCategory(allCategories, 4);
		getCategoryChildren(edocCategorys, allTemplates);
		var edocTempaltes = getAllTemplate(edocCategorys, 4);
		var edocTemplateData = getFinallyShowData(4, edocTempaltes);
		addShowData(showData, catagerIds, edocTemplateData);
		//信息报送模板
		initInfoTempalte(allCategories,allTemplates,showData,catagerIds);
		//公共模板
		var publiceRootCategorys = getAllChildrenCategory(allCategories, 0);
		getCategoryChildren(publiceRootCategorys, allTemplates);
		var publicRootTemplates = getAllTemplate(publiceRootCategorys, 0);
		var publicRootTemplateData =getFinallyShowData(0, publicRootTemplates);
		addShowData(showData, catagerIds, publicRootTemplateData);
		//个人模板
		var personTempaltes = [];
		getPersonTemplate(personTempaltes, tempates, 100)
		var personTemplateData = getFinallyShowData(100, personTempaltes);
		addShowData(showData, catagerIds, personTemplateData);
	}
	var dataHTML = "";
	var seletedCategoryId = params.seletedCategoryId;
	if (seletedCategoryId == "-1") { //最近使用模版
		var templateMan = new templateManager();
		showTempaltes = templateMan.getRecentUseTemplate(category,recent);
	} else if (isPersonalTemplate(seletedCategoryId) && seletedCategoryId != "100") {//选择的是个人模板分类
		getPersonTemplate(showTempaltes, tempates, seletedCategoryId)
		showData = [];
	} else {
		//所选分类下的直接模板
		showTempaltes = getSubShowData(seletedCategoryId);
	}

	//查询
	var searchValue = $("#searchValue").val();
	if (searchValue != "" && searchValue.trim() !="") {
		var tempaltes = [];
		for (var i = 0;i<tempates.length ; i++) {
			if (tempates[i].tapSubject.indexOf(searchValue) != -1) {
				tempaltes.push(tempates[i]);
			}
		}
		initHTML(null, tempaltes);
	} else {
		initHTML(showData, showTempaltes);
	}
}

/**
 * 初始化信息报送模板数据
 * @param allCategories 所有的模板分类
 * @param allTemplates  所有的模板数据
 * @param showData
 * @param catagerIds
 */
function initInfoTempalte(allCategories,allTemplates,showData,catagerIds){
	var category = null;
	for(var i=0;i<allCategories.length;i++) {
		if(allCategories[i].id == 32){
			category = allCategories[i];
		}
	}
	if(category != null){
		category.children=[];
	}
	for (var i=0; i< allTemplates.length; i++){//循环模板
		if (allTemplates[i].categoryId == 32  && allTemplates[i].system && category != null) {//模板所有栏目
			category.children.push(allTemplates[i]);//将模板添加到栏目的模板属性中
		}
	}
	if(category != null){
		showData.push(category);
		catagerIds.push(category.id);
	}
}

function initHTML(categoryList, allTemplates) {
	var showHTML = "";

	if (allTemplates != null|| allTemplates != undefined) {
		showHTML += initShowTemplate(allTemplates);
	}

	if (categoryList != null || categoryList != undefined) {
		showHTML += initShowCatagory(categoryList);
	}

	$("#templateDatasTab").html(showHTML);

}

function initShowCatagory(categoryList) {
	var dataHTML = "";
	for (var i=0;i<categoryList.length;i++) {
		var showCategory = categoryList[i];
		if (typeof(showCategory.children) == "undefined") {
			showCategory.children = [];
		}

		var categoryName = escapeStringToHTML(showCategory.name);
		if (showCategory.children == undefined || showCategory.children.length < 1) {
			categoryName += "（"+$.i18n('collaboration.deadline.no')+"）";
		}
		var categoryCount = i+1;
		var categoryCountId = categoryCount%3;

		var categoryspanCount = 1;
		var aHTML = "<tr><td colspan='4' class='sortsHead border_b' > <div class='div-float no-wrap' style='margin-top: 10px;'> <span class='ico16 folder_16'></span><span class='font_size12 font_bold padding_l_5'>"
			+categoryName+"</span></div></td>";
		for (var j=0; j<showCategory.children.length; j++) {
			var tempate = showCategory.children[j];
			var thisCount = j+1;
			var countId = thisCount%3;

			var templeteName = escapeStringToHTML(tempate.tapSubject);
			var templeteShowName = escapeStringToHTML(tempate.subject);

			var colspanCount = 1;
			var isHR = "";
			if (countId == 0 && thisCount!=showCategory.children.length) {
				isHR = "</tr><tr>";
			}
			if (countId == 1) {
				aHTML += "<tr>";
			}
			aHTML += "<td class='text-indent-1em sorts' style='padding-top:12px;padding-bottom:5px;text-overflow:ellipsis;padding-right:20px;overflow:hidden'>";
			if (tempate.moduleType == 19) {
				aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewEdoc(\"sendManager\",\"0\",\""+tempate.id+"\");'>";
			} else if (tempate.moduleType == 20) {
				aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewEdoc(\"recManager\",\"1\",\""+tempate.id+"\");'>";
			} else if (tempate.moduleType == 21) {
				aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewEdoc(\"signReport\",\"2\",\""+tempate.id+"\");'>";
			} else if (tempate.moduleType == 401 || tempate.moduleType == 402 || tempate.moduleType == 404) {
				aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewGovdoc(\""+tempate.id+"\","+tempate.moduleType+");'>";
			} else if (tempate.moduleType == 32) {
				aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewInfo(\""+tempate.id+"\");'>";
			} else if (tempate.moduleType != 19 && tempate.moduleType != 20 && tempate.moduleType != 21 && tempate.moduleType != 32) {
				aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewWindow(\""+tempate.id+"\");'>";
			}
			aHTML += "<span class='ico16 "+tempate.templeteIcon+"'></span>&nbsp;"+templeteShowName+"</a></td>"+ isHR;
		}

		dataHTML += aHTML + "</tr>";

	}
	return "<tr>"+dataHTML+"</tr>";
}
function initShowTemplate(tempates){
	var dataHTML = "";
	for (var i=0;i<tempates.length;i++) {
		var tempate = tempates[i];
		var thisCount = i+1;
		var countId = thisCount%3;

		var templeteName = escapeStringToHTML(tempate.tapSubject);
		var templeteShowName = escapeStringToHTML(tempate.subject);

		var colspanCount = 1;
		var isHR = "";
		if (countId == 0 && thisCount!=tempates.length) {
			isHR = "</tr><tr>";
		}
		var aHTML = "<td class='sorts' style='padding-top:0;padding-bottom:10px;'>";
		if (tempate.moduleType == 19) {
			aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewEdoc(\"sendManager\",\"0\",\""+tempate.id+"\");'>";
		} else if (tempate.moduleType == 20) {
			aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewEdoc(\"recManager\",\"1\",\""+tempate.id+"\");'>";
		} else if (tempate.moduleType == 21) {
			aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewEdoc(\"signReport\",\"2\",\""+tempate.id+"\");'>";
		} else if (tempate.moduleType == 401 || tempate.moduleType == 402 || tempate.moduleType == 404) {
			aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewGovdoc(\""+tempate.id+"\","+tempate.moduleType+");'>";
		} else if (tempate.moduleType == 32) {
			aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewInfo(\""+tempate.id+"\");'>";
		} else if (tempate.moduleType != 19 && tempate.moduleType != 20 && tempate.moduleType != 21 && tempate.moduleType != 32) {
			aHTML += "<a title='"+templeteName+"&#13"+tempate.templeteCreatorAlt+"' class='defaultlinkcss text_overflow' href='javascript:openNewWindow(\""+tempate.id+"\");'>";
		}
		aHTML += "<span class='ico16 "+tempate.templeteIcon+"'></span>&nbsp;"+templeteShowName+"</a></td>"
		dataHTML += aHTML + isHR;
	}
	var showHTML = "<tr>"+dataHTML+"</tr>";

	return showHTML;
}

function onSelectAccount() {
	var selectAccountId =  $("#selectAccountId option:selected").val();
	var url = _ctxPath+"/template/template.do?method=moreTreeTemplate&selectAccountId="+selectAccountId+"&category="+category+"&recent="+recent+"&showRecentTemplate="+showRecentTemplate + CsrfGuard.getUrlSurffix();
	window.location.href=url;
}

function moreTemplate() {

	var ajaxMapBean = new Object();
	callBackendMethod("collaborationTemplateManager","saveCustomViewType","1",{
        success : function(returnData){
        	var url =  _ctxPath+"/template/template.do?method=listRACITemplate&category="+category+"&recent="+recent + CsrfGuard.getUrlSurffix();
        	window.location.href=url;
        },
		error : function(request, settings, e){
	        $.alert(e);
	    }
	});
}

function openNewWindow(templateId) {
    var url =  _ctxPath + "/collaboration/collaboration.do?method=newColl&from=templateNewColl&templateId="+templateId + CsrfGuard.getUrlSurffix();
    openCtpWindow({'url':url});
}
function openNewEdoc(entry,typeId,templateId) {
	var url = _ctxPath + "/edocController.do?method=entryManager&entry="+entry+"&toFrom=newEdoc&listType=newEdoc&edocType="+typeId+"&templeteId="+templateId + CsrfGuard.getUrlSurffix();
	location.href = url;
}
function openNewInfo(templateId) {
	var url = _ctxPath + "/info/infocreate.do?method=createInfo&templateId="+templateId+"&action=template" + CsrfGuard.getUrlSurffix();
	location.href = url;
}

//是否是个人模版
function isPersonalTemplate(seletedCategoryId) {
	if (seletedCategoryId == "100" || seletedCategoryId == "101"
  			   || seletedCategoryId == "102" || seletedCategoryId ==  "103"
  			   || seletedCategoryId == "104"|| seletedCategoryId == "105") {
		return true;
	}
	return false;
}

//公文模版
function isEdocTemplate(moduleType) {
	if (moduleType == "19" || moduleType == "20" || moduleType == "21"
		|| moduleType == "401" || moduleType == "402" || moduleType == "404") {
		return true;
	}
	return false;
}

/**
 * 模板更多页面右边显示树形结构
 */
function getShowData(nodes){

	var nodeId = nodes.id;
	var allCategories = [];//全部模板分类
	$.extend(true,allCategories, page.showCategorys);
	var allTemplates = [];//全部模板
	$.extend(true,allTemplates, page.showTemplates);

	if(nodeId == 32){
		var returnCategory;
		for(var i=0;i<allCategories.length;i++) {
			if(allCategories[i].id == 32){
				returnCategory = allCategories[i];
			}
		}
		returnCategory.children=[];
		for (var i=0; i< allTemplates.length; i++){//循环模板
			if (allTemplates[i].categoryId == 32  && allTemplates[i].system) {//模板所有栏目
				returnCategory.children.push(allTemplates[i]);//将模板添加到栏目的模板属性中
			}
		}
	}else{
		var categorys = getAllChildrenCategory(allCategories, nodeId);//获取子类模板分类
		getCategoryChildren(categorys, allTemplates);//为每个模板分类添加具体的模板值

		var returnCategory = getReturnCategory(categorys, nodeId);//只保留第一级模板分类

		/*if (returnCategory.length < 1) {//最子类
			returnCategory = getSubShowData(nodes.id);
		}*/
	}

	return returnCategory;
}
/**
 * 获取nodeId所在节点以下的所有子节点分类
 */
function getAllChildrenCategory(allCategories, nodeId){
	var allCategoryIds = [];
	for(var i=0;i<allCategories.length;i++) {
		if(allCategories[i].pId == nodeId){//子节点
			allCategoryIds.push(allCategories[i]);
			var childList = getAllChildrenCategory(allCategories, allCategories[i].id);//查找子节点的子节点
			for (var j=0; j<childList.length;j++) {//将找到的子节点添加到返回参数中
				allCategoryIds.push(childList[j]);
			}
		}
	}
	return allCategoryIds;
}
/**
 * 将allTemplates中的模板对应添加到categorys的模板分类中
 */
function getCategoryChildren(categorys, allTemplates) {
	for (var i=0; i<categorys.length; i++){//循环分类
		for (var j=0; j< allTemplates.length; j++){//循环模板
			if (categorys[i].id == allTemplates[j].categoryId  && allTemplates[j].system) {//模板所有栏目
				if (typeof(categorys[i].children)=="undefined") {
					categorys[i].children=[];
				}
				categorys[i].children.push(allTemplates[j]);//将模板添加到栏目的模板属性中
			} else if (!allTemplates[j].system){ //个人模板
				if (allTemplates[j].moduleType == 1 &&  allTemplates[j].type =="template" && categorys[i].id == "101") {
					if (typeof(categorys[i].children)=="undefined") {
						categorys[i].children=[];
					}
					categorys[i].children.push(allTemplates[j]);
				} else if (allTemplates[j].moduleType == 1 && allTemplates[j].type == "text" && categorys[i].id == "102") {
					if (typeof(categorys[i].children)=="undefined") {
						categorys[i].children=[];
					}
					categorys[i].children.push(allTemplates[j]);
				} else if (allTemplates[j].moduleType == 1 &&  allTemplates[j].type == "workflow" && categorys[i].id == "103") {
					if (typeof(categorys[i].children)=="undefined") {
						categorys[i].children=[];
					}
					categorys[i].children.push(allTemplates[j]);
				} else if (isEdocTemplate(allTemplates[j].moduleType) && categorys[i].id == "104") {
					if (typeof(categorys[i].children)=="undefined") {
						categorys[i].children=[];
					}
					categorys[i].children.push(allTemplates[j]);
				} else if (allTemplates[j].moduleType == 32 && categorys[i].id == "105") {
					if (typeof(categorys[i].children)=="undefined") {
						categorys[i].children=[];
					}
					categorys[i].children.push(allTemplates[j]);
				} else if (categorys[i].id == "100") {
					if (typeof(categorys[i].children)=="undefined") {
						categorys[i].children=[];
					}
					categorys[i].children.push(allTemplates[j]);
				}
			}
		}
	}
}

/**
 * 将categorys所有的模板分类，只保留nodeId对应的第一级子分类，其余子分类的模板追加到对应的第一级分类的模板属性中
 */
function getReturnCategory(categorys, nodeId){
	var list = [];
	var categoryList = [];
	var count = 0;
	for(var i=0; i<categorys.length;i++){
		if(categorys[i].pId == nodeId){//第一级分类
			var childList = getAllTemplate(categorys, categorys[i].id);//得到分类下面的所有模板（包括子类）
			categoryList.push(categorys[i]);
			if (typeof(categoryList[count].children) == "undefined") {
				categoryList[count].children = [];
			}
			for (var k=0; k<childList.length;k++) {//追加具体的模板
				categoryList[count].children.push(childList[k]);
			}
			list.push(categoryList[count]);
			count++;
		}
	}
	return list;
}

/**
 * 获取categorys模板分类集合中属于categoryId分类下面的全部模板
 */
function getAllTemplate(categorys, categoryId){
	var list = [];
	for(var i=0; i<categorys.length;i++){
		if(categorys[i].pId == categoryId){//categorys中父类是categoryId模板分类
			if (categorys[i].children != undefined) {//如果子类中有具体模板
				for (var j=0;j<categorys[i].children.length; j++) {
					list.push(categorys[i].children[j]);//将具体的模板添加到返回值中
				}
			}
			var childList = getAllTemplate(categorys, categorys[i].id);//继续查找子类的子类模板
			for (var k=0; k<childList.length;k++) {
				list.push(childList[k]);//追加模板到返回值中
			}
		}
	}
	return list;
}

/**
 * catagoryId所属分类下的直接模板（不包含子分类模板）
 */
function getSubShowData(catagoryId) {
	var allTemplates = [];//所有模板
	$.extend(true,allTemplates, page.showTemplates);
	var showTempaltes = [];
	for (var i=0; i<allTemplates.length; i++) {
		if(isEdocTemplate(allTemplates[i].moduleType)){
			if (allTemplates[i].categoryId == catagoryId && allTemplates[i].system) {//模板所有catagoryId栏目
				showTempaltes.push(allTemplates[i]);
			}
		}else if(allTemplates[i].categoryId == 32){
			if (allTemplates[i].categoryId == catagoryId && allTemplates[i].system) {//模板所有catagoryId栏目
				showTempaltes.push(allTemplates[i]);
			}
		}else{
			if (allTemplates[i].categoryId == catagoryId) {//模板所有catagoryId栏目
				showTempaltes.push(allTemplates[i]);
			}
		}
	}
	return showTempaltes;//getFinallyShowData(catagoryId, showTempaltes);
}

/**
 * 为catagoryId所在模板分类添加具体的模板showTemplates
 */
function getFinallyShowData(catagoryId, showTemplates) {
	var returnCategory = [];
	var allCategories = [];//所有模板分类
	$.extend(true, allCategories, page.showCategorys);

	//查找catagoryId对应的模板分类
	for (var i=0; i<allCategories.length;i++) {
		if (catagoryId == allCategories[i].id) {
			returnCategory.push(allCategories[i]);
			break;
		}
	}

	//为returnCategory添加具体的模板
	if (returnCategory.length > 0) {
		for (var j=0; j<showTemplates.length; j++) {
			if (returnCategory[0].children == undefined) {
				returnCategory[0].children = [];
			}
			returnCategory[0].children.push(showTemplates[j]);
		}
	}

	return returnCategory;
}

/**
 * 将showTemplate模板分类追加到showData中，并且将模板分类ID添加到catagerIds中
 */
function addShowData(showData, catagerIds, showTemplate) {
	if (showTemplate != undefined) {
		for (var i=0; i<showTemplate.length; i++) {
			if ($.inArray(showTemplate[i].id, catagerIds)==-1) {
				showData.push(showTemplate[i]);
				catagerIds.push(showTemplate[i].id);
			}
		}
	}
}

/**
 * 获取个人模板
 * showTempaltes：seletedCategoryId分类的个人模板
 * tempates：所有模板
 * seletedCategoryId：模板ID
 */
function getPersonTemplate(showTempaltes, tempates, seletedCategoryId) {
	for(var i=0;i<tempates.length;i++) {
		if (!tempates[i].system) {
			if (tempates[i].moduleType == 1 &&  tempates[i].type =="template" && seletedCategoryId == "101") {
				showTempaltes.push(tempates[i]);
			} else if (tempates[i].moduleType == 1 && tempates[i].type == "text" && seletedCategoryId == "102") {
				showTempaltes.push(tempates[i]);
			} else if (tempates[i].moduleType == 1 &&  tempates[i].type == "workflow" && seletedCategoryId == "103") {
				showTempaltes.push(tempates[i]);
			} else if (isEdocTemplate(tempates[i].moduleType) && seletedCategoryId == "104") {
				showTempaltes.push(tempates[i]);
			} else if (tempates[i].moduleType == 32 && seletedCategoryId == "105") {
				showTempaltes.push(tempates[i]);
			} else if (seletedCategoryId == "100") {
				showTempaltes.push(tempates[i]);
			}
		}
	}
}


function openNewGovdoc(templateId,moduleType) {
	var subApp = "";
	if(moduleType == "401"){
		subApp = "1"
	}else if(moduleType == "402"){
		subApp = "2"
	}else if(moduleType == "404"){
		subApp = "3"
	}
    url =  _ctxPath + "/govdoc/govdoc.do?method=newGovdoc&from=template&app=4&sub_app="+subApp+"&templateId="+templateId;
    openCtpWindow({'url':url});
}



