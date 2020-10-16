/**
 * Constants Defined
 */

var webFXTreeConfig = {
	rootIcon        : basePath + '/common/SelectPeople/tree/images/foldericon.png',
	openRootIcon    : basePath + '/common/SelectPeople/tree/images/openfoldericon.png',
	folderIcon      : basePath + '/common/SelectPeople/tree/images/foldericon.png',
	openFolderIcon  : basePath + '/common/SelectPeople/tree/images/openfoldericon.png',
	fileIcon        : basePath + '/common/SelectPeople/tree/images/foldericon.png',
	folder1Icon     : basePath + '/common/SelectPeople/tree/images/folder1.png',
	openFolder1Icon : basePath + '/common/SelectPeople/tree/images/openfolder1.png',
	folder2Icon     : basePath + '/common/SelectPeople/tree/images/folder2.png',
	iIcon           : basePath + '/common/SelectPeople/tree/images/I.png',
	lIcon           : basePath + '/common/SelectPeople/tree/images/L.png',
	lMinusIcon      : basePath + '/common/SelectPeople/tree/images/Lminus.png',
	lPlusIcon       : basePath + '/common/SelectPeople/tree/images/Lplus.png',
	tIcon           : basePath + '/common/SelectPeople/tree/images/T.png',
	tMinusIcon      : basePath + '/common/SelectPeople/tree/images/Tminus.png',
	tPlusIcon       : basePath + '/common/SelectPeople/tree/images/Tplus.png',
	blankIcon       : basePath + '/common/SelectPeople/tree/images/blank.png',
	defaultText     : 'No Data',
	defaultAction   : '',
	defaultBehavior : 'classic',
	usePersistence	: false
};

var webFXTreeHandler = {
	rootId    : "",
	all       : {},
	behavior  : null,
	selected  : null,
	expanded  : null,	//add by tanmf 2006-06-05
	type      : "",
	onSelect  : null, /* should be part of tree, not handler */
	toggle    : function (oItem) { this.all[oItem.id.replace('-plus','')].toggle(); },
	select    : function (oItem) { this.all[oItem.id.replace(/-icon|-checkbox/,'')].select(); },
	focus     : function (oItem) { this.all[oItem.id.replace('-anchor','')].focus(); },
	blur      : function (oItem) { this.all[oItem.id.replace('-anchor','')].blur(); },
	keydown   : function (oItem, e) {  },
	cookies   : new WebFXCookie(),
	insertHTMLBeforeEnd	:	function (oElement, sHTML) {
		if (oElement.insertAdjacentHTML != null) {
			oElement.insertAdjacentHTML("BeforeEnd", sHTML)
			return;
		}
		var df;	// DocumentFragment
		var r = oElement.ownerDocument.createRange();
		r.selectNodeContents(oElement);
		r.collapse(false);
		df = r.createContextualFragment(sHTML);
		oElement.appendChild(df);
	},
	clickCheckbox : function (oItem){
//		tempNowSelected.clear();

		var item = this.all[oItem.id.replace(/-icon|-checkbox/,'')];

		var e1 = new Element(item.type, item.id, item.text, "", "");
		this.addOrRemoveTempNowSelected(oItem.checked, e1);
		var account0 = accessableAccounts.get(item.id);
		var allChild = findAllChildInListByPath(accessableAccounts.values(), account0.path);

		// 是否勾选子单位
		if(allChild.size()>1){
			var childIsChicked = false;
			for(var i = 0 , allChildSize = allChild.size(); i < allChildSize; i++) {
				var tempItem = allChild.get(i);
				var isSelect = false;
				for(var j = 0, tempNowSelectedSize = tempNowSelected.size(); j < tempNowSelectedSize; j++) {
					if(tempNowSelected.get(j).id == tempItem.id){
						isSelect = true;
						break;
					}
				}
				var obj = document.getElementById(tempItem.id + "-checkbox");
				if((obj != null && obj.checked) || isSelect){
					childIsChicked = (obj != null && obj.checked) || isSelect;
					break;
				}
			}
			var isChecked = document.getElementById(item.id + "-checkbox").checked;
			var i18n = $.i18n("selectPeople.ifChildDept");;
			if(!isChecked){
				i18n = $.i18n("selectPeople.ifChildDept.unselected");
			}
			if((item.id != this.rootId) && (childIsChicked || isChecked)){
				if(confirm(i18n)){
					this.clickCheckboxAll(item.id,isChecked);
				}
			}
		}
	},
	addOrRemoveTempNowSelected : function(isChecked, e){
		var index = tempNowSelected.indexOf(e, "id");
		if(isChecked){
		    if(!checkIncludeElements(e.type, e.id)){
		        return;
		    }

			if(index < 0){
				tempNowSelected.add(e);
			}
			else{
				//ignore
			}
		}
		else{
			tempNowSelected.removeElementAt(index);
		}
	},
	clickCheckboxAll : function(id,isChecked){
		if(tempNowPanel.type == Constants_Account){ //先写死吧
			var curentCheckObj = document.getElementById(id + "-checkbox");
			if(isChecked == null || isChecked == undefined){
				isChecked = !curentCheckObj.checked;
			}

			curentCheckObj.checked = isChecked;
			var item1 = this.all[id.replace(/-icon|-checkbox/,'')];
			var e1 = new Element(Constants_Account, item1.id, item1.text, "", "");
			this.addOrRemoveTempNowSelected(isChecked, e1);


			//var allChild = topWindow.findAllChildInList(accessableAccounts.values(), id);
			var account0 = accessableAccounts.get(id);
            //var allChild = account0.accessChildren;
			var allChild = findAllChildInListByPath(accessableAccounts.values(), account0.path);

			for(var i = 0; i < allChild.size(); i++) {
				var item = allChild.get(i);

				var obj = document.getElementById(item.id + "-checkbox");
				if(obj != null){
					obj.checked = isChecked;
				}
				var e = new Element(Constants_Account, item.id, item.name, "", "");
				this.addOrRemoveTempNowSelected(isChecked, e);
			}
		}
	},
	isCheckboxSelected: function(oItem){
		var item = this.all[oItem.id.replace(/-icon|-checkbox/,'')];
		for(var i = 0; i < tempNowSelected.size(); i++) {
			if(tempNowSelected.get(i).id == item.id){
				return true;
			}
		}

		return false;
	},
	isCheckboxDisabled: function(oItem){
	    var item = this.all[oItem.id.replace(/-icon|-checkbox/,'')];
	    return !checkIncludeElements(item.type, item.id);
	}

};

function findAllChildInListByPath(list, parentPath) {
	var temp = new ArrayList();
	if(list == null){
		return temp;
	}

	for(var i = 0; i < list.size(); i++){
		var obj = list.get(i);
		if(obj.path.indexOf(parentPath)==0){
			temp.add(obj);
		}
	}

	return temp;
}

/*
 * WebFXCookie class
 */

function WebFXCookie() {
//	if (document.cookie.length) { this.cookies = ' ' + document.cookie; }
}

WebFXCookie.prototype.setCookie = function (key, value) {
//	document.cookie = key + "=" + escape(value);
}

WebFXCookie.prototype.getCookie = function (key) {
//	if (this.cookies) {
//		var start = this.cookies.indexOf(' ' + key + '=');
//		if (start == -1) { return null; }
//		var end = this.cookies.indexOf(";", start);
//		if (end == -1) { end = this.cookies.length; }
//		end -= start;
//		var cookie = this.cookies.substr(start,end);
//		return unescape(cookie.substr(cookie.indexOf('=') + 1, cookie.length - cookie.indexOf('=') + 1));
//	}
//	else { return null; }
}

/*
 * WebFXTreeAbstractNode class
 */

function WebFXTreeAbstractNode(id, type, sText, hasChild, sAction, fixed, description) {
	this.childNodes  = [];
	this.id     = id;
	this.type   = type;
	this.text   = sText || webFXTreeConfig.defaultText;
	this.hasChild = hasChild;
	this.fixed = fixed || false;
	this.action = sAction || webFXTreeConfig.defaultAction;
	this._last  = false;
	this.hasShowChild = false; //add by tanmf 2006-6-1
	this.hasGoChild = false;
	this.description = description == null ? sText : description;
	webFXTreeHandler.all[this.id] = this;
}

/*
 * To speed thing up if you're adding multiple nodes at once (after load)
 * use the bNoIdent parameter to prevent automatic re-indentation and call
 * the obj.ident() method manually once all nodes has been added.
 */

WebFXTreeAbstractNode.prototype.add = function (node, bNoIdent) {
	node.parentNode = this;
	this.childNodes[this.childNodes.length] = node;
	var root = this;
	if (this.childNodes.length >= 2) {
		this.childNodes[this.childNodes.length - 2]._last = false;
	}
	while (root.parentNode) { root = root.parentNode; }
	if (root.rendered) {
		if (this.childNodes.length >= 2) {
			document.getElementById(this.childNodes[this.childNodes.length - 2].id + '-plus').src = ((this.childNodes[this.childNodes.length -2].folder)?((this.childNodes[this.childNodes.length -2].open)?webFXTreeConfig.tMinusIcon:webFXTreeConfig.tPlusIcon):webFXTreeConfig.tIcon);
			this.childNodes[this.childNodes.length - 2].plusIcon = webFXTreeConfig.tPlusIcon;
			this.childNodes[this.childNodes.length - 2].minusIcon = webFXTreeConfig.tMinusIcon;
			this.childNodes[this.childNodes.length - 2]._last = false;
		}
		this._last = true;
		var foo = this;
		while (foo.parentNode) {
			for (var i = 0; i < foo.parentNode.childNodes.length; i++) {
				if (foo.id == foo.parentNode.childNodes[i].id) { break; }
			}
			if (i == foo.parentNode.childNodes.length - 1) { foo.parentNode._last = true; }
			else { foo.parentNode._last = false; }
			foo = foo.parentNode;
		}
		webFXTreeHandler.insertHTMLBeforeEnd(document.getElementById(this.id + '-cont'), node.toString());
		if ((!this.folder) && (!this.openIcon)) {
			this.icon = webFXTreeConfig.folderIcon;
			this.openIcon = webFXTreeConfig.openFolderIcon;
		}
		if (!this.folder) { this.folder = true; this.collapse(true); }
		//if (!bNoIdent) { this.indent(); }
	}
	return node;
}

WebFXTreeAbstractNode.prototype.toggle = function() {
	//部门下的单位,单位下的集团单位总是展开,不能收起
	if ((this.folder || this.hasChild) &&
		!((this.type == Constants_Account || this.type == Constants_BusinessAccount || this.type == Constants_JoinAccountTag) && this.childType!=undefined && this.childType!="undefined")) {
		//this.expand(); //Edit by tanmf 2006-06-04
		if (this.open) { this.collapse(); }
		else if (this.folder){ this.expand(); }
	}
}

WebFXTreeAbstractNode.prototype.select = function() {
	document.getElementById(this.id + '-anchor').focus();
}

WebFXTreeAbstractNode.prototype.deSelect = function() {
	if(document.getElementById(this.id + '-anchor') == null){
		return;
	}
	document.getElementById(this.id + '-anchor').className = '';
	webFXTreeHandler.selected = null;
}

WebFXTreeAbstractNode.prototype.focus = function() {
	if ((webFXTreeHandler.selected) && (webFXTreeHandler.selected != this)) { webFXTreeHandler.selected.deSelect(); }
	webFXTreeHandler.selected = this;

	if(!this.isShowCheckbox){
		tempNowSelected.clear();

		if(checkIncludeElements(this.type, this.id)){
		    var _element = new Element(this.type, this.id, this.text, "", "");
		    if (this.externalType) {
		        _element.externalType = this.externalType;
		    }
		    if(this.type === 'MemberMetadataTag' && (!(this._level) || this._level < 1)){
		    	return;
		    }

		    if(this.type == 'BusinessDepartment'){
		    	var businessDepartment = topWindow.getObject('BusinessDepartment', this.id);
		    	if(businessDepartment){
		    		_element.name = businessDepartment.preShow + "-" + businessDepartment.name;
		    	}
		    }
		    _element.hasChild = this.hasChild;
		    tempNowSelected.add(_element);   //add by tanmf 2006-6-1
		}
	}

	if ((this.openIcon) && (webFXTreeHandler.behavior != 'classic')) { document.getElementById(this.id + '-icon').src = this.openIcon; }
	document.getElementById(this.id + '-anchor').className = 'selected';
	document.getElementById(this.id + '-anchor').focus();
	if (webFXTreeHandler.onSelect) { webFXTreeHandler.onSelect(this); }
}


WebFXTreeAbstractNode.prototype.blur = function() {
	if ((this.openIcon) && (webFXTreeHandler.behavior != 'classic')) { document.getElementById(this.id + '-icon').src = this.icon; }
	document.getElementById(this.id + '-anchor').className = 'selected-inactive';
}

/**
 * ????????????
 */
WebFXTreeAbstractNode.prototype.doExpand = function(childType) {
	if(!this.hasShowChild || this.hasGoChild){
		var len = this.childNodes.length;
		for(var i = 0; i < len; i++){
			this.childNodes[0].remove();
		}

		showChildTree(childType, this.id, this); //add by tanmf 2006-6-1  ??????????
	}

	this.doExpandAction();

	//************** add by tanmf
	if(this.hasShowChild && this.parentNode != null){
		this.parentNode.hasGoChild = true;
		var thisChildNodes = this.parentNode.childNodes;
		var clength = thisChildNodes.length;
		var k = 0;
		for (var i = 0; i < clength; i++) {
/*			if (this != thisChildNodes[k]) {
				thisChildNodes[k].remove();
			}
			else{
				k++;
				this.hasShowChild = true;
			}*/
			if (this === thisChildNodes[k]) {
				k++;
				this.hasShowChild = true;
			}
		}
	}
}

WebFXTreeAbstractNode.prototype.doExpandAction = function() {
	if (webFXTreeHandler.behavior == 'classic') {
		if(this.id == null || this.id == undefined){
			return;
		}
		document.getElementById(this.id + '-icon').src = this.openIcon;
	}
	if (this.childNodes.length) {  document.getElementById(this.id + '-cont').style.display = 'block'; }
	this.open = true;

	if (webFXTreeConfig.usePersistence) {
		webFXTreeHandler.cookies.setCookie(this.id.substr(1,this.id.length - 1), '1');
	}
}
/**
 * ??
 */
WebFXTreeAbstractNode.prototype.doCollapse = function() {
	if (webFXTreeHandler.behavior == 'classic') { document.getElementById(this.id + '-icon').src = this.icon; }
	if (this.childNodes.length) { document.getElementById(this.id + '-cont').style.display = 'none'; }
	this.open = false;
	if (webFXTreeConfig.usePersistence) {
		webFXTreeHandler.cookies.setCookie(this.id.substr(1,this.id.length - 1), '0');
	}
}

WebFXTreeAbstractNode.prototype.expandAll = function() {
	this.expandChildren();
	if ((this.folder) && (!this.open)) { this.doExpandAction(); }
}

WebFXTreeAbstractNode.prototype.expandChildren = function() {
	for (var i = 0; i < this.childNodes.length; i++) {
		this.childNodes[i].expandAll();
} }

WebFXTreeAbstractNode.prototype.collapseAll = function() {
	this.collapseChildren();
	if ((this.folder) && (this.open)) { this.collapse(true); }
}

WebFXTreeAbstractNode.prototype.collapseChildren = function() {
	for (var i = 0; i < this.childNodes.length; i++) {
		this.childNodes[i].collapseAll();
} }

WebFXTreeAbstractNode.prototype.indent = function(lvl, del, last, level, nodesLeft) {
	/*
	 * Since we only want to modify items one level below ourself,
	 * and since the rightmost indentation position is occupied by
	 * the plus icon we set this to -2
	 */
	if (lvl == null) { lvl = -2; }
	var state = 0;
	for (var i = this.childNodes.length - 1; i >= 0 ; i--) {
		state = this.childNodes[i].indent(lvl + 1, del, last, level);
		if (state) { return; }
	}
	if (del) {
		if ((level >= this._level) && (document.getElementById(this.id + '-plus'))) {
			if (this.folder) {
				document.getElementById(this.id + '-plus').src = (this.open)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.lPlusIcon;
				this.plusIcon = webFXTreeConfig.lPlusIcon;
				this.minusIcon = webFXTreeConfig.lMinusIcon;
			}
			else if (nodesLeft) { document.getElementById(this.id + '-plus').src = webFXTreeConfig.lIcon; }
			return 1;
	}	}
	var foo = document.getElementById(this.id + '-indent-' + lvl);
	if (foo) {
		if ((foo._last) || ((del) && (last))) { foo.src =  webFXTreeConfig.blankIcon; }
		else { foo.src =  webFXTreeConfig.iIcon; }
	}
	return 0;
}

/*
 * WebFXTree class
 * @param type ??????
 * @param childType ??????
 * "type" added by tanmf 2006-6-1 defined as D or O and so on
 */

function WebFXTree(id, type, sText, childType, hasChild, sAction, fixed, description, sBehavior, sIcon, sOpenIcon) {
	webFXTreeHandler.rootId = id;
	this.base = WebFXTreeAbstractNode;
	this.base(id, type, sText, hasChild, sAction, fixed, description);
	this.childType = childType;
	this.icon      = sIcon || webFXTreeConfig.rootIcon;
	this.openIcon  = sOpenIcon || webFXTreeConfig.openRootIcon;
	/* Defaults to open */
	if (webFXTreeConfig.usePersistence) {
		this.open  = (webFXTreeHandler.cookies.getCookie(this.id.substr(1,this.id.length - 1)) == '0')?false:true;
	} else { this.open  = true; }
	this.folder    = true;
	this.rendered  = false;
	this.onSelect  = null;
	webFXTreeHandler.expanded = this;
	webFXTreeHandler.selected = this;
	if (!webFXTreeHandler.behavior) {  webFXTreeHandler.behavior = sBehavior || webFXTreeConfig.defaultBehavior; }
}

WebFXTree.prototype = new WebFXTreeAbstractNode;

WebFXTree.prototype.setBehavior = function (sBehavior) {
	webFXTreeHandler.behavior =  sBehavior;
};

WebFXTree.prototype.getBehavior = function (sBehavior) {
	return webFXTreeHandler.behavior;
};

WebFXTree.prototype.getSelected = function() {
	if (webFXTreeHandler.selected) { return webFXTreeHandler.selected; }
	else { return null; }
}

WebFXTree.prototype.getExpanded = function() {
	if (webFXTreeHandler.expanded) { return webFXTreeHandler.expanded; }
	else { return null; }
}

WebFXTree.prototype.remove = function() { }

WebFXTree.prototype.expand = function() {
	this.doExpand(this.childType);
}

WebFXTree.prototype.collapse = function(b) {
	if (!b) { this.focus(); }
	this.doCollapse();
}

WebFXTree.prototype.getFirst = function() {
	return null;
}

WebFXTree.prototype.getLast = function() {
	return null;
}

WebFXTree.prototype.getNextSibling = function() {
	return null;
}

WebFXTree.prototype.getPreviousSibling = function() {
	return null;
}

WebFXTree.prototype.keydown = function(key) {
	if (key == 39) {
		if (!this.open) { this.expand(); }
		else if (this.childNodes.length) { this.childNodes[0].select(); }
		return false;
	}
	if (key == 37) { this.collapse(); return false; }
	if ((key == 40) && (this.open) && (this.childNodes.length)) { this.childNodes[0].select(); return false; }
	return true;
}

WebFXTree.prototype.toString = function() {
    if (this.externalType) {
        if (this.externalType == "3") {//V-Join平台
            this.icon = webFXTreeConfig.folder1Icon;
            this.openIcon = webFXTreeConfig.openFolder1Icon;
        }
    }

	var str = new StringBuffer();
	str.append("<div id=\"").append(this.id).append("\" title=\""+this.text).append("\" onclick=\"")
	.append((this.fixed ? "" : "webFXTreeHandler.toggle(this);")).append("\" class=\"webfx-tree-item\" onkeydown=\"")
	.append((this.fixed ? "" : "return webFXTreeHandler.keydown(this, event)")).append("\">").append("<img id=\"")
	.append(this.id).append("-icon\" class=\"webfx-tree-icon cursor-hand\" src=\"")
	.append(((webFXTreeHandler.behavior == 'classic' && this.open)?this.openIcon:this.icon)).append("\" onclick=\"")
	.append((this.fixed ? "" : "webFXTreeHandler.select(this);")).append("\">")
	.append(this.isShowCheckbox ? ("<input type='checkbox' name='ItemCheckBox' onclick=\"webFXTreeHandler.clickCheckbox(this)\" id='" + this.id + "-checkbox' " + (webFXTreeHandler.isCheckboxSelected(this) ? 'checked' : '') + " " + (webFXTreeHandler.isCheckboxDisabled(this) ? 'disabled' : '') + ">") : "")
	.append("<a href=\"javascript:void(null)\"  onclick=\"")
	.append((this.fixed ? "" : (this.action + ";webFXTreeHandler.select(this.previousSibling)"))).append("")
	.append(";return false\" id=\"").append(this.id).append("-anchor\" onfocus=\"")
	.append((this.fixed ? "" : "webFXTreeHandler.focus(this);")).append("\" onblur=\"")
	.append((this.fixed ? "" : "webFXTreeHandler.blur(this);")).append("\"")
	.append(this.target ? " target=\"" + this.target + "\"" : "").append(">")
	.append(this.text).append("</a></div>").append("<div id=\"").append(this.id)
	.append("-cont\" class=\"webfx-tree-container\" style=\"display: ").append(((this.open)?'block':'none')).append(";\">");
	var sb = [];
	for (var i = 0; i < this.childNodes.length; i++) {
		sb[i] = this.childNodes[i].toString(i, this.childNodes.length);
	}
	this.rendered = true;


	return str.toString() + sb.join("") + "</div>";
};

/*
 * WebFXTreeItem class
 */

function WebFXTreeItem(id, type, sText, hasChild, sAction, fixed, description, eParent, sIcon, sOpenIcon) {
	this.base = WebFXTreeAbstractNode;
	this.base(id, type, sText, hasChild, sAction, fixed, description);
	/* Defaults to close */
	if (webFXTreeConfig.usePersistence) {
		this.open = (webFXTreeHandler.cookies.getCookie(this.id.substr(1,this.id.length - 1)) == '1')?true:false;
	} else { this.open = false; }
	if (sIcon) { this.icon = sIcon; }
	if (sOpenIcon) { this.openIcon = sOpenIcon; }
	if (eParent) { eParent.add(this); }
	this.isShowCheckbox = false;
}

WebFXTreeItem.prototype = new WebFXTreeAbstractNode;

WebFXTreeItem.prototype.remove = function() {
	var iconSrc = document.getElementById(this.id + '-plus').src;
	var parentNode = this.parentNode;
	var prevSibling = this.getPreviousSibling(true);
	var nextSibling = this.getNextSibling(true);
	var folder = this.parentNode.folder;
	var last = ((nextSibling) && (nextSibling.parentNode) && (nextSibling.parentNode.id == parentNode.id))?false:true;
	//this.getPreviousSibling().focus();
	this._remove();

	if (parentNode.childNodes.length == 0) {
		document.getElementById(parentNode.id + '-cont').style.display = 'none';
		parentNode.doCollapse();
		parentNode.folder = false;
		parentNode.open = false;
	}
	if (!nextSibling || last) { parentNode.indent(null, true, last, this._level, parentNode.childNodes.length); }

	//add by tanmd 2006-6-2 ??????????????????????
	if(this.parentNode.id == webFXTreeHandler.rootId) {
		return;
	}
	if ((prevSibling == parentNode) && !(parentNode.childNodes.length)) {
		prevSibling.folder = false;
		prevSibling.open = false;

		if(document.getElementById(prevSibling.id + '-plus')){
    		iconSrc = document.getElementById(prevSibling.id + '-plus').src;
    		iconSrc = iconSrc.replace('minus', '').replace('plus', '');
    		document.getElementById(prevSibling.id + '-plus').src = iconSrc;
		}
		document.getElementById(prevSibling.id + '-icon').src = webFXTreeConfig.fileIcon;
	}
	if (prevSibling && document.getElementById(prevSibling.id + '-plus')) {
		if (parentNode == prevSibling.parentNode) {
			iconSrc = iconSrc.replace('minus', '').replace('plus', '');
			document.getElementById(prevSibling.id + '-plus').src = iconSrc;
		}
	}
}

WebFXTreeItem.prototype._remove = function() {
	for (var i = this.childNodes.length - 1; i >= 0; i--) {
		this.childNodes[i]._remove();
 	}
	for (var i = 0; i < this.parentNode.childNodes.length; i++) {
		if (this == this.parentNode.childNodes[i]) {
			for (var j = i; j < this.parentNode.childNodes.length; j++) {
				this.parentNode.childNodes[j] = this.parentNode.childNodes[j+1];
			}
			this.parentNode.childNodes.length -= 1;
			if (i + 1 == this.parentNode.childNodes.length) { this.parentNode._last = true; }
			break;
	}	}
	webFXTreeHandler.all[this.id] = null;
	var tmp = document.getElementById(this.id);
	if (tmp) { tmp.parentNode.removeChild(tmp); }
	tmp = document.getElementById(this.id + '-cont');
	if (tmp) { tmp.parentNode.removeChild(tmp); }
}

WebFXTreeItem.prototype.expand = function() {
	this.doExpand(this.type);
	document.getElementById(this.id + '-plus').src = this.minusIcon;
}

WebFXTreeItem.prototype.collapse = function(b) {
	if (!b) { this.focus(); }
	this.doCollapse();
	document.getElementById(this.id + '-plus').src = this.plusIcon;
}

WebFXTreeItem.prototype.getFirst = function() {
	return this.childNodes[0];
}

WebFXTreeItem.prototype.getLast = function() {
	if(!this.childNodes || this.childNodes.length == 0) return null;
	if (this.childNodes[this.childNodes.length - 1].open) { return this.childNodes[this.childNodes.length - 1].getLast(); }
	else { return this.childNodes[this.childNodes.length - 1]; }
}

WebFXTreeItem.prototype.getNextSibling = function() {
	for (var i = 0; i < this.parentNode.childNodes.length; i++) {
		if (this == this.parentNode.childNodes[i]) { break; }
	}
	if (++i == this.parentNode.childNodes.length) { return this.parentNode.getNextSibling(); }
	else { return this.parentNode.childNodes[i]; }
}

WebFXTreeItem.prototype.getPreviousSibling = function(b) {
	for (var i = 0; i < this.parentNode.childNodes.length; i++) {
		if (this == this.parentNode.childNodes[i]) { break; }
	}
	if (i == 0) { return this.parentNode; }
	else {
		if ((this.parentNode.childNodes[--i].open) || (b && this.parentNode.childNodes[i].folder)) { return this.parentNode.childNodes[i].getLast(); }
		else { return this.parentNode.childNodes[i]; }
} }

WebFXTreeItem.prototype.keydown = function(key) {
	if ((key == 39) && (this.folder)) {
		if (!this.open) { this.expand(); }
		else { this.getFirst().select(); }
		return false;
	}
	else if (key == 37) {
		if (this.open) { this.collapse(); }
		else { this.parentNode.select(); }
		return false;
	}
	else if (key == 40) {
		if (this.open) { this.getFirst().select(); }
		else {
			var sib = this.getNextSibling();
			if (sib) { sib.select(); }
		}
		return false;
	}
	else if (key == 38) { this.getPreviousSibling().select(); return false; }
	return true;
}

WebFXTreeItem.prototype.toString = function (nItem, nItemCount) {
	var foo = this.parentNode;
	var indent = '';
	if (nItem + 1 == nItemCount) { this.parentNode._last = true; }
	var i = 0;
	while (foo.parentNode) {
		foo = foo.parentNode;
		indent = "<img id=\"" + this.id + "-indent-" + i + "\" src=\"" + ((foo._last)?webFXTreeConfig.blankIcon:webFXTreeConfig.iIcon) + "\">" + indent;
		i++;
	}
	this._level = i;
	if(this.type === Constants_Account){
        var account0 = accessableAccounts.get(this.id);
        //var allChild = account0.accessChildren;
        var allChild = findAllChildInListByPath(accessableAccounts.values(), account0.path);
        //var allChild = topWindow.findAllChildInList(accessableAccounts.values(), this.id);
	}
	if ((this.childNodes.length || this.hasChild) && ((this.type === Constants_Account && allChild && allChild.size()>0)||this.type != Constants_Account)) { this.folder = 1; }
	else { this.open = false; }
	if ((this.folder) || (webFXTreeHandler.behavior != 'classic')) {
		if (!this.icon) { this.icon = webFXTreeConfig.folderIcon; }
		if (!this.openIcon) { this.openIcon = webFXTreeConfig.openFolderIcon; }
	}
	else if (!this.icon) { this.icon = webFXTreeConfig.fileIcon; }

	if (this.externalType) {
        if (this.externalType == "1") {//外部机构
            this.icon = webFXTreeConfig.folder1Icon;
            this.openIcon = webFXTreeConfig.openFolder1Icon;
        } else if (this.externalType == "2") {//外部单位
            this.icon = webFXTreeConfig.folder2Icon;
            this.openIcon = webFXTreeConfig.folder2Icon;
        }
    }

	var label = this.text.replace(/</g, '&lt;').replace(/>/g, '&gt;');
	var str = new StringBuffer();

	str.append("<div id=\"").append(this.id).append("\" title=\""+this.text).append("\" ondblclick=\"")
	.append((this.fixed ? "" : "webFXTreeHandler.toggle(this);")).append("\" class=\"webfx-tree-item\" onkeydown=\"")
	.append((this.fixed ? "" : "return webFXTreeHandler.keydown(this, event)")).append("\">").append(indent)
	.append("<img id=\"").append(this.id).append("-plus\" class='cursor-hand' src=\"").append(((this.folder)?((this.open)?((this.parentNode._last)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.tMinusIcon):((this.parentNode._last)?webFXTreeConfig.lPlusIcon:webFXTreeConfig.tPlusIcon)):((this.parentNode._last)?webFXTreeConfig.lIcon:webFXTreeConfig.tIcon)))
	.append("\" onclick=\"").append((this.fixed ? "" : "webFXTreeHandler.toggle(this);") + "\">")
	.append("<img id=\"").append(this.id).append("-icon\" class=\"webfx-tree-icon\" src=\"")
	.append(((webFXTreeHandler.behavior == 'classic' && this.open)?this.openIcon:this.icon))
	.append("\" onclick=\"").append((this.fixed ? "" : "webFXTreeHandler.select(this);")).append("\">")
	.append(this.isShowCheckbox ? ("<input type='checkbox' name='ItemCheckBox' onclick=\"webFXTreeHandler.clickCheckbox(this)\" id='" + this.id + "-checkbox' " + (webFXTreeHandler.isCheckboxSelected(this) ? 'checked' : '') + " " + (webFXTreeHandler.isCheckboxDisabled(this) ? 'disabled' : '') + ">") : "")
		//zhou
		.append("<a href=\"javascript:void(null)\" ondblclick=\"dbClickDeptSelectedMember()\" onclick=\"").append((this.fixed ? "" : this.action)).append(";").append((this.fixed ? "" : "webFXTreeHandler.select(this.previousSibling)")).append(";return false;\" id=\"")
	.append(this.id).append("-anchor\" onfocus=\"").append((this.fixed ? "" : "webFXTreeHandler.focus(this);")).append("\" onblur=\"")
	.append((this.fixed ? "" : "webFXTreeHandler.blur(this);")).append( "\"").append((this.target ? " target=\"" + this.target + "\"" : ""))
	.append((this.isShowCheckbox ? " ondblclick=\"webFXTreeHandler.clickCheckboxAll('" + this.id + "')\"" : ""))
	.append(">").append(label).append("</a></div>").append("<div id=\"")
	.append(this.id).append("-cont\" class=\"webfx-tree-container\" style=\"display: ").append(((this.open)?'block':'none')).append(";\">");
	var sb = [];
	for (var i = 0; i < this.childNodes.length; i++) {
		sb[i] = this.childNodes[i].toString(i,this.childNodes.length);
	}
	this.plusIcon = ((this.parentNode._last)?webFXTreeConfig.lPlusIcon:webFXTreeConfig.tPlusIcon);
	this.minusIcon = ((this.parentNode._last)?webFXTreeConfig.lMinusIcon:webFXTreeConfig.tMinusIcon);

	return str.toString() + sb.join("") + "</div>";
}
