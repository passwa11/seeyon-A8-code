/**
 * 初始化方法
 */
jQuery(function($) {
  var bul_type = document.getElementById('bulTypeId').value;
  if(document.getElementById("spaceType").value=="1"){
    document.getElementById("publishDepartmentName").disabled="disabled";
  }
  if(bul_type==null||bul_type==""){
      load_bulType();
      changeTypeCotrol();
  }else{
    if(document.getElementById("isAuditEditBul").value!=" "){
       //load_bulType();
       var optionSpace = document.getElementById("orgType");
       var optionBul = document.getElementById("typeList_id");
       var option_list = document.getElementById('isAuditEditBul').value;
       var option_list_Key = eval("("+option_list+")");
       var bulTypeNode = document.getElementById('typeList_id');
       var spaceTypeNode = document.getElementById('orgType');
       bulTypeNode.options.length=0;//清空
       spaceTypeNode.options.length=0;//清空
       bulTypeNode.add(new Option(option_list_Key[2],option_list_Key[1]));
       spaceTypeNode.add(new Option(option_list_Key[4],option_list_Key[3]));
       bulTemplateList();
    }else {
      $("#boardType_"+bul_type).attr("selected",true);
      var spaceNode = document.getElementById('orgType');
      var typeNode = document.getElementById('typeList_id');
      if(spaceNode.value=="1"){
        typeNode.value = bul_type;
        load_bulType();
      }else{
    	load_bulType();
    	typeNode.value = bul_type;
        changeTypeCotrol();
      }
    }
  }

  if(document.getElementById('showRecordReadId') != null){
	  var showRecordReadId = document.getElementById('showRecordReadId').value;
	  if(showRecordReadId != null &&  String(showRecordReadId) == "1"){
		  document.getElementById('recordRead_em').className="checkBox checked";
		  if(document.getElementById('showOpenRecordsReadId') != null){
			  var showOpenRecordsReadId = document.getElementById('showOpenRecordsReadId').value;
			  if(showOpenRecordsReadId != null && String(showOpenRecordsReadId) =="1"){
				  document.getElementById('openRecordsReadLi').style.display='block';
			  } else if(showOpenRecordsReadId != null && String(showOpenRecordsReadId) =="0") {
				  /*document.getElementById('openRecordsReadLi').style.display='none';*/
				  document.getElementById('openRecordsRead_em').className="checkBox";
				  document.getElementById('openRecordsRead').value = '0';
			  }
		  }
	  } else if(showRecordReadId != null &&  String(showRecordReadId) == "0") {
		  document.getElementById('recordRead_em').className="checkBox";
		  document.getElementById('openRecordsReadLi').style.display='none';
		  document.getElementById('recordReadId').value = '0';
		  document.getElementById('openRecordsRead').value = '0';
	  }
  }

  $("#checkbox li em,#checkbox li span").bind('click', function() {
    var em = $(this).parent().find('em');
    if (em.hasClass('checked')) {
      em.removeClass('checked');
      if (em.attr('id') == 'showPublishUser') {
        document.getElementById('showPublish').value = 'false';
        document.getElementById('publishChoose').style.display='none';
        document.getElementById('writePublishSelLi').style.display='none';
        document.getElementById('writePublishWriLi').style.display='none';
      }else if (em.attr('id') == 'recordRead_em') {
        document.getElementById('recordReadId').value = '0';
        document.getElementById('openRecordsReadLi').style.display='none';
        document.getElementById('openRecordsRead').value = '0';
      }else if (em.attr('id') == 'openRecordsRead_em') {
        document.getElementById('openRecordsRead').value = '0';
      }else if (em.attr('id') == 'printPermit_em') {
        document.getElementById('printFla').value = '0';
      }else if (em.attr('id') == 'toPdf') {
        document.getElementById('pdf').value = 'false';
      }
      showPublisher("cancel");
    } else {
      em.addClass('checked');
      if (em.attr('id') == 'showPublishUser') {
        document.getElementById('showPublish').value = 'true';
        if(writePermit == "true"){
          document.getElementById('publishChoose').style.display='block';
          if(document.getElementById("publishChooseSelect").selectedIndex == 1){
            document.getElementById('writePublishSelLi').style.display='block';
            document.getElementById('writePublishWriLi').style.display='none';
          } else if(document.getElementById("publishChooseSelect").selectedIndex == 2){
            document.getElementById('writePublishSelLi').style.display='none';
            document.getElementById('writePublishWriLi').style.display='block';
          } else {
            document.getElementById('writePublishSelLi').style.display='none';
            document.getElementById('writePublishWriLi').style.display='none';
          }
        }
      }else if (em.attr('id') == 'recordRead_em') {
    	  document.getElementById('readMes').value = '1';
    	  document.getElementById('recordReadId').value = '1';
          document.getElementById('openRecordsReadLi').style.display='block';
          document.getElementById('openRecordsRead_em').className="checkBox checked";
          document.getElementById('openRecordsRead').value = '1';
      }else if(em.attr('id') == 'openRecordsRead_em'){
    	  document.getElementById('openRecordsRead').value = '1';
      }else if (em.attr('id') == 'printPermit_em') {
        document.getElementById('printFla').value = '1';
      }else if (em.attr('id') == 'toPdf') {
        document.getElementById('pdf').value = 'true';
      }
      showPublisher("checked");
    }
  });

  var type_Id = document.getElementById("typeList_id").value;
  document.getElementById("typeId").value = type_Id;

  if(document.getElementById("readMes").value=="1"){
    document.getElementById("recordRead_em").className = "checkBox checked";
  } else {
    document.getElementById("recordRead_em").className = "checkBox";
  }
  if(document.getElementById("printFla0").value=="1"){
    document.getElementById("printPermit_em").className = "checkBox checked";
    document.getElementById('printFla').value = '1';
  }else if(document.getElementById("printFla0").value=="0"){
    document.getElementById("printPermit_em").className = "checkBox";
    document.getElementById("printFla").value ="0";
  }
  if(document.getElementById("publishInput").value){
    document.getElementById("issueAreaName").value=document.getElementById("publishInput").value;
  }
  var bodyTypeObj = document.getElementById("dataFormat").value;
  if (bodyTypeObj == "HTML") {
      myBar.enabled("preview");
  } else {
      myBar.disabled("preview");
  }

  if(writePermit == "true"){
    if(document.getElementById("publishChooseHidden").value == "1"){
      document.getElementById('writePublishSelLi').style.display='block';
    } else if(document.getElementById("publishChooseHidden").value == "2") {
      document.getElementById('writePublishWriLi').style.display='block';
    }
  }
  showPublisher("flag");
  $(".write_em").mouseover(function(e){
    $("#write_help_li").show();
  }).mouseout(function(){
    $("#write_help_li").hide();
  });

  resizeFckeditor();
  initColor(document.getElementById('title'));
  initColor(document.getElementById('issueAreaName'));
});

//zhou
function changSendRang() {
  var typeID = document.getElementById("typeId").value;
  $.ajax({
    url: '/seeyon/ehSendRangeController.do?method=getSendRange',
    type: 'POST',
    dataType: 'json',
    data: {id: typeID},
    success: function (result) {
      if (result.code == 0) {
        var range = result.data;
        if(range!=null){
          $("#publishScopeId").val(range.rangeId);
          $("#publishInput").val(range.rangeName);
          $("#issueAreaName").val(range.rangeName);
        }
      }
    }
  });
}
function myCheckForm(form) {
  if (form.typeList_id.value == null || form.typeList_id.value == "") {
    alert(alert_noNull);
    return false;
  }
  return checkForm(form);
}

function initColor(elements) {
  if(getDefaultValue(elements) == elements.value){
	elements.style.color = "gray";
  }
}

function loadBulTemplate() {
  // 对Office的处理
  if (confirm(alert_temp)) {
    document.getElementById("dataForm").removeAttribute("target");
    $('form_oper').value = "loadTemplate";
    isFormSumit = true;

    var tempId = document.getElementById('bulTempl').value
    document.getElementById('templateId').value = tempId;
    // 去掉离开提示
    window.onbeforeunload = function() {
      try {
        removeCtpWindow(null, 2);
      } catch (e) {
      }
    }
    saveAttachment();
    if(document.getElementById("isBulEdit").value == ""){
    	document.getElementById("isBulEdit").value = "isBulEdit";
    } else {
    	document.getElementById("isBulEdit").value = "";
    }
    $('dataForm').submit();
  } else {
    var tId = document.getElementById('templateId').value;
    var temp = document.getElementById('bulTempl');
    if (tId == null || tId == '')
      temp.options[0].selected = true;
    else
      temp.value = tId;
  }
  ORRInit();
}

var saveBtnClick = false;
function saveForm(operType) {
  var bulTypeId = document.getElementById("typeList_id").value;
  if (validAuditUserEnabled(bulTypeId, 'ajaxBulDataManager') == 'false') {
    alert(v3x.getMessage("bulletin.bulletin_checker_enabled_please_reset"));
    return;
  }
  var flag = validTypeExist(bulTypeId, 'ajaxBulDataManager');
  $('dataFormat').value = document.getElementById("bodyType").value;

   window.onbeforeunload = function() {
    removeCtpWindow(null, 2);
  }
  if (flag == 'false') {
    alert(v3x.getMessage("bulletin.type_deleted"));
    isFormSumit = true;
    getA8Top().document.getElementById('main').src = "${bulDataURL}?method=index&spaceType=${spaceType}&spaceId=${param.spaceId}"
  } else {
    if (!checkForm(dataForm)) {
      return;
    }
    if(document.getElementById('showPublish').value == 'true'){
      if(document.getElementById("publishChooseSelect").value == "1" && !document.getElementById("choosePublshId").value){
        alert(alert_pleChoosePublish);
        return;
      } else if (document.getElementById("publishChooseSelect").value == "2"&& !document.getElementById("writePublish").value){
        alert(alert_pleWritePublish);
        return;
      } else if (document.getElementById("publishChooseSelect").value == "2"&& document.getElementById("writePublish").value.length > 40){
        alert(alert_pleWriteLen1 + document.getElementById("writePublish").value.length + alert_pleWriteLen2);
        return;
      }
    }
    if(!document.getElementById("publishScopeId").value){
      if(document.getElementById("publishChooseHidden").value == "1"){
        document.getElementById('writePublishSelLi').style.display='block';
      } else if(document.getElementById("publishChooseHidden").value == "2") {
        document.getElementById('writePublishWriLi').style.display='block';
      }
    }
    var bodyType = document.getElementById("bodyType");
    var changePdf = document.getElementById("pdf");
    $('dataFormat').value = bodyType.value;
    $('form_oper').value = operType;
    if ($('dataForm').onsubmit()) {
      document.getElementById("dataForm").removeAttribute("target");
      saveAttachment();
      document.getElementById("ext5").value = "";
      if (bodyType.value == "Pdf") {
        var isSuccess = savePdf();
        if (!isSuccess) {
          return false;
        }
      } else {
        if (bodyType.value == "OfficeWord" && changePdf && changePdf.value=='true') {
          if (!newsSaveOffice())
            return false;
          var fileId = getUUID();
          document.getElementById("ext5").value = fileId;
          if (!OfficeAPI.transformWordToPdf(fileId))
            return false;
        } else {
          if (bodyType.value == "OfficeWord" || bodyType.value == "OfficeExcel" || bodyType.value == "WpsWord" || bodyType.value == "WpsExcel") {
            if (!newsSaveOffice())
              return false;
          }
        }
      }
      isFormSumit = true;
      if (!saveBtnClick) {
        saveBtnClick = true;
        myCheckForm($('dataForm'));
        $('method').value= 'bulSave';
        $('dataForm').submit();
      }
    }
  }
}
/**
 * 判断公告、新闻板块审核员是否可用
 */
function validAuditUserEnabled(typeId, mgrName){
  var requestCaller = new XMLHttpRequestCaller(this, mgrName, "isAuditUserEnabled", false);
  requestCaller.addParameter(1, "Long", typeId);
  return requestCaller.serviceRequest();
}
function validTypeExist(id, mgrName){
  var requestCaller = new XMLHttpRequestCaller(this, mgrName,
     "typeExist", false);
  requestCaller.addParameter(1, "long", id);

  var ret = requestCaller.serviceRequest();

  return ret;
}
function newsSaveOffice() {
  var bodyType = document.getElementById("bodyType");
  if (bodyType) {
    bodyType = bodyType.value;
    if (bodyType != 'OfficeWord' && bodyType != 'OfficeExcel' && bodyType != 'WpsWord' && bodyType != 'WpsExcel') {
      return true;
    }
  }
  try {
    document.getElementById("content").value = OfficeAPI.getOfficeOcxRecordID();
  } catch (e) {
  }
  return OfficeAPI.saveOffice();
}


// 公告预览
function viewPage() {
  $('method').value= 'bulPreview';
  var title = document.getElementById('title');
  if(checkDefaultValue(title)){
    document.getElementById("previewTitle").value = title.value;
  }
  if ($('dataForm').onsubmit()) {
    saveAttachment();
    $('dataForm').submit();
  }
  $('method').value= 'bulEdit';
}


function checkDefaultValue(element){
    var value = element.value;
    var defaultValue = getDefaultValue(element);
    if(value == defaultValue){
        return false;
    }
    return true;
};

/**将焦点设置到office控件上，否则容易出现因为打开模态对话框以后
 * office控件焦点丢失不能编辑的问题。
 * */
function activeOcx() {
  try {
    activeOfficeOcx();
  } catch (e) {

  }
}

// 进行解锁
function unlock(id) {
  try {
    if (document.getElementById('isAuditEdit').value == 'true') {
      return;
    }
    var requestCaller = new XMLHttpRequestCaller(this, "ajaxBulDataManager", "unlock", false);
    requestCaller.addParameter(1, "Long", id);
    //如果用户直接点击退出或关闭IE，此时解锁无法进行，可能形成死锁
    requestCaller.needCheckLogin = false;
    var ds = requestCaller.serviceRequest();
  } catch (ex1) {
    alert("Exception : " + ex1);
  }
}

function resizeFckeditor() {
  try {
    var bodyH = parseInt(document.body.clientHeight);
    var tr1H = 42;
    var tr2H = document.getElementById("bulEditTD").clientHeight;
    var fckOuterBlock = document.getElementById("editerDiv");
    if (bodyH != "0" && tr2H != "0") {
      fckOuterBlock.style.height = bodyH - parseInt(tr1H) - parseInt(tr2H) + "px";
      var fckContentList = document.getElementsByClassName("cke_contents");
      if(fckContentList.length>0){
          var fckBlock = fckContentList[0];
          fckBlock.style.height = (bodyH - parseInt(tr1H) - parseInt(tr2H) - parseInt(50)) + "px";
      }
    }
  } catch (e) {
    // 防止打开pdf内容后获取不到ckedit
  }
}

function changeBodyTypeCallBack() {
  var _editerDivHeight = document.getElementById("editerDiv");
  try{
    if (v3x.isMSIE8) {
      var _bodyTypeObj = document.getElementById("bodyType");
      if (_bodyTypeObj && _bodyTypeObj.value == "HTML") {
        var editerDiv_Height = document.getElementById("editerDiv").clientHeight;
        var _setHeight = setInterval(function(){
          if(document.getElementById("cke_49_contents")){
            var ck_top = document.getElementById("cke_49_top").clientHeight;
            document.getElementById("cke_49_contents").style.height = editerDiv_Height - ck_top + 'px';
            window.clearInterval(_setHeight);
          }
        },500);
      }
    }
  }catch(e){}
  setTimeout(function(){
    if(document.getElementById("editerDiv").style.clientHeight == 0){
      document.getElementById("editerDiv").style.clientHeight = _editerDivHeight;
    }
  },1000)
}

function deletAttrCallBackFun() {
  resizeFckeditor();
}

function reSize() {
  if (v3x.isMSIE8) {// 112 140 128+24 152
    try {
      var editerDiv_tdHeight = document.getElementById("editerDiv_td").clientHeight;
      editerDiv_tdHeight = editerDiv_tdHeight - 112;
      if (document.documentElement.clientWidth < 1024) {
        editerDiv_tdHeight = editerDiv_tdHeight - 40;
      } else {
        editerDiv_tdHeight = editerDiv_tdHeight - 28;
      }
      document.getElementById("editerDiv_td").style.height = editerDiv_tdHeight + "px";
      document.getElementById("editerDiv").style.height = editerDiv_tdHeight + "px";
    } catch (e) {
    }
  }
}
function showEditer(){
  document.getElementById('editerDiv').style.display='';
}


function openAdvancedWindow() {
  v3x.openWindow({
    url : "${newsDataURL}?method=openAdvance&spaceType=${param.spaceType}",
    width : "400",
    height : "200",
    scrollbars : "no"
  });
}

/**
 * 切换单位、集团版块
 *
 */
function changeSelect(){
  if(document.getElementById("bulTempl").value!=""){
    if (confirm(alert_spaceType_select)) {
      document.getElementById("dataForm").removeAttribute("target");
      //切换集团、单位版块数据保留
      document.getElementById("spaceType_change").value="1";
      $('spaceType').value =document.getElementById("orgType").value;
      isFormSumit = true;
      // 去掉离开提示
      window.onbeforeunload = function() {
        try {
          removeCtpWindow(null, 2);
        } catch (e) {
        }
      }
      saveAttachment();
      document.getElementById("bulTypeId").value="";
      $('dataForm').submit();
    }else {
      var tId = document.getElementById('spaceType').value;
      var temp = document.getElementById('orgType');
      if (tId == null || tId == '')
        temp.options[0].selected = true;
      else
        temp.value = tId;
    }
  } else {
    document.getElementById("dataForm").removeAttribute("target");
    if(document.getElementById("bulTempl")){
      document.getElementById("bulTempl").value="";
    }
    //切换集团、单位版块数据保留
    document.getElementById("spaceType_change").value="1";
    $('spaceType').value =document.getElementById("orgType").value;
    if(document.getElementById("isBulEdit").value == ""){
    	document.getElementById("isBulEdit").value = "isBulEdit";
    } else {
    	document.getElementById("isBulEdit").value = "";
    }
    isFormSumit = true;
    // 去掉离开提示
    window.onbeforeunload = function() {
      try {
        removeCtpWindow(null, 2);
      } catch (e) {
      }
    }
    saveAttachment();
    document.getElementById("bulTypeId").value="";
    $('dataForm').submit();
  }
  ORRInit();
}

//公告版块的加载
function load_bulType(){
  var space_Type = document.getElementById("spaceType").value;
  var orgType = document.getElementById('orgType');
  if(orgType.value==null || orgType.value==''){
    return;
  }
  var key = "1";
  if(space_Type != "1"){
    key = orgType.value;
    if(key == "2"){
      key = "bulletin.type.corporation";
    }else if(key == "3"){
      key = "bulletin.type.group";
    }else if(key == "4"){
      key = "bulletin.type.custom";
    }else if(key == "17"){
      key = "bulletin.type.public.custom";
    }else if(key == "18"){
      key = "bulletin.type.public.custom.group";
    }
    var bul_list = document.getElementById('bul_TypeInfo_id').value;
    var groupBoard = eval("("+bul_list+")");
    //生成选项
    var list = groupBoard[key];
    var typeNode = document.getElementById('typeList_id');
    typeNode.options.length=0;//清空
    for(var i = 0;i<list.length;i++){
      typeNode.add(new Option(list[i].typeName,list[i].id));
    }
  }
  if(!_isCustom){
    //选人点击事件
    if(key=="1"){
      var deptId = document.getElementById("typeList_id").value;
      openAjaxNew(deptId);
      document.getElementById("issueAreaName").onclick=function(event,data){
        selectPeopleFun_spDept();
      }
    }else if(key=="bulletin.type.corporation"){
      document.getElementById("issueAreaName").onclick=function(event,data){
        selectPeopleFun_spAccount();
      }
    }else if(key=="bulletin.type.group"){
      document.getElementById("issueAreaName").onclick=function(event,data){
        selectPeopleFun_spGroup();
      }
    }
  }
  bulTemplateList();
}
function changeCheckBox(){
  var check_Type = document.getElementById("orgType").value;
  var deptId = document.getElementById("typeList_id").value;
  if(check_Type=="1"){
    openAjaxNew(deptId);
  }else {
    changeTypeCotrol();
  }
  var type_Id = document.getElementById("typeList_id").value;
  document.getElementById("typeId").value = type_Id;
  ORRInit();
}

//切换版块时，发布人，打印选项控制
var writePermit = "false";
function changeTypeCotrol(){
    var deptId = document.getElementById("typeList_id").value;
    var requestCaller;
    requestCaller = new XMLHttpRequestCaller(this, "ajaxBulDataManager", "bulTypeDefault", false);
    requestCaller.addParameter(1, "Long", deptId);
    var temp= requestCaller.serviceRequest();
    var ds = eval("("+temp+")");
    //根据版块设置打印选项
    var printFlag = "";
    try {
    	printFlag = ds["printFlag"];
    	if(printFlag == null || printFlag  == undefined){
        	printFlag = "";
        }
	} catch (e) {
		printFlag = "";
		console.log(e);
	}
    var printDefault = "";
    try {
    	printDefault = ds["printDefault"];
    	if(printDefault == null || printDefault  == undefined){
    		printDefault = "";
        }
	} catch (e) {
		printDefault = "";
		console.log(e);
	}
    if(printFlag=="false"){
      document.getElementById('printPermit').style.display='none';
      document.getElementById('printFla').value = '0';
    } else if(printFlag=="true"){
      document.getElementById('printPermit').style.display='';
      var em = document.getElementById('printPermit_em');
      var check_class = em.className;
      var check_flag = false;
      var wsdf = check_class.indexOf("checked");
      if(check_class.indexOf("checked")>0){
        check_flag = true;
      }
      if(printDefault=="true"){
        document.getElementById('printFla').value = '1';
        if(!check_flag){
            em.className="checkBox checked";
        }
      } else if(printDefault=="false"){
        document.getElementById('printFla').value = '0';
        if(check_flag){
          em.className="checkBox";
        }
      }
    }
    //根据版块设置发布人选项
    try {
    	writePermit = ds["writePermit"];
    	if(writePermit == null || writePermit  == undefined){
    		writePermit = "";
        }
	} catch (e) {
		writePermit = "";
		console.log(e);
	}
    if(document.getElementById("showPublishExi").value == "true"){
      if(writePermit == "true"){
        document.getElementById('publishChoose').style.display='block';
        if(document.getElementById("writePublishSelLi").value == 1){
          document.getElementById('writePublishSelLi').style.display='block';
        }else if(document.getElementById("publishChoose").value == 2){
          document.getElementById('writePublishWriLi').style.display='block';
        }
      } else {
        document.getElementById('publishChoose').style.display='none';
        document.getElementById('writePublishSelLi').style.display='none';
        document.getElementById('writePublishWriLi').style.display='none';
        document.getElementById("publishChooseSelect").options[0].selected = true;
      }
    } else if(document.getElementById("bul_Id").value == ""){
      if(ds["defaultPublish"] == "true"){
        document.getElementById('showPublishUser').className="checkBox checked";
        document.getElementById('showPublish').value = 'true';
        if(writePermit == "true"){
          document.getElementById('publishChoose').style.display='block';
          // if(document.getElementById("writePublishSelLi").value == 1){
          //   document.getElementById('writePublishSelLi').style.display='block';
          // }else if(document.getElementById("publishChoose").value == 2){
          //   document.getElementById('writePublishWriLi').style.display='block';
          // }
        } else {
          document.getElementById('publishChoose').style.display='none';
          document.getElementById('writePublishSelLi').style.display='none';
          document.getElementById('writePublishWriLi').style.display='none';
          document.getElementById("publishChooseSelect").options[0].selected = true;
        }
      } else {
        document.getElementById('showPublishUser').className="checkBox";
        document.getElementById('showPublish').value = 'false';
        document.getElementById('publishChoose').style.display='none';
        document.getElementById('writePublishSelLi').style.display='none';
        document.getElementById('writePublishWriLi').style.display='none';
        document.getElementById("publishChooseSelect").options[0].selected = true;
      }
    }
    //根据版块设置发布人选项
    var _finalPublish = "";
    try {
    	_finalPublish = ds["finalPublish"];
    	if(_finalPublish == null || _finalPublish  == undefined){
    		_finalPublish = "";
        }
	} catch (e) {
		_finalPublish = "";
		console.log(e);
	}
    if(_finalPublish =="1"){
      document.getElementById("publishChooseSelect_0").innerText = bulsender;
    }else if(_finalPublish =="2"){
      document.getElementById("publishChooseSelect_0").innerText = bulAuditer;
    } else if(_finalPublish =="0"){
      document.getElementById("publishChooseSelect_0").innerText = bulRealer;
    }
}

function changePublishChoose(){
  var publishChooseVal = document.getElementById("publishChooseSelect").value;
  if(publishChooseVal == "0"){
    document.getElementById('writePublishSelLi').style.display='none';
    document.getElementById('writePublishWriLi').style.display='none';
  } else if(publishChooseVal == "1"){
    document.getElementById('writePublishSelLi').style.display='block';
    document.getElementById('writePublishWriLi').style.display='none';
  } else if(publishChooseVal == "2"){
    document.getElementById('writePublishSelLi').style.display='none';
    document.getElementById('writePublishWriLi').style.display='block';
  }

}
function bulTemplateList(){
  var space_Type_id=document.getElementById("orgType").value;
  var requestCaller;
  requestCaller = new XMLHttpRequestCaller(this, "ajaxBulDataManager", "getBulTempl", false);
  requestCaller.addParameter(1, "Integer", space_Type_id);
  var ds = requestCaller.serviceRequest();
  if(ds!=null){
    var list = eval("("+ds+")");
    var templNode = document.getElementById('bulTempl');
    var templ = list.template;
    templNode.options.length=1;//清除之后的
    for(var j=0;j<templ.length;j++){
      templNode.add(new Option(templ[j].templateName,templ[j].id));
    }
    var _templateId = document.getElementById('templateId').value;
    if(_templateId!=null && _templateId!=''){
      templNode.value = _templateId;
    }

  }
}


  function selectPeople(elemId,idElem,nameElem){
    if(true)
      eval('selectPeopleFun_'+elemId+'()');
    else{
      var dlgArgs=new Array();
      dlgArgs['width']=238;
      dlgArgs['height']=310;
      dlgArgs['url']='/seeyon/selectPeople.jsp';
      var elements=v3x.openWindow(dlgArgs);
      if(elements!=null && elements.length>0)
        setBulPeopleFields(elements,idElem,nameElem);
        showOriginalElement_per=true;
    }
    activeOcx();
  }

  function showPublisher(flag){
	  var isAuditEdit = document.getElementById('isAuditEdit').value;
	  var writePermit = document.getElementById('writePermit').value;
	  var finalPublish = document.getElementById('finalPublish').value;
	  var bulAuditPublishChoose = document.getElementById('bulAuditPublishChoose').value;
	  var showPublisherName = document.getElementById('showPublisherName').value;
	  if(isAuditEdit != "" && isAuditEdit == "true"
		  && writePermit != "" && writePermit == "true" ){
		  if(bulAuditPublishChoose != "" && bulAuditPublishChoose =="0"){
			  document.getElementById('writePublishSelLi').style.display='none';
	          document.getElementById('writePublishSelLi').style.display='none';
			  if(finalPublish != "" && finalPublish == "0"){
				  document.getElementById("publishChooseSelect_0").innerText = bulRealer;
				  if(flag=="checked" || flag=="flag" ){
					  document.getElementById('publishChoose').style.display='block';
				  } else {
					  document.getElementById('publishChoose').style.display='none';
				  }
			  } else if(finalPublish != "" && finalPublish == "1"){
				  document.getElementById("publishChooseSelect_0").innerText = bulsender;
				  if(flag=="checked" || flag=="flag"){
					  document.getElementById('publishChoose').style.display='block';
				  } else {
					  document.getElementById('publishChoose').style.display='none';
				  }
			  } else if(finalPublish != "" && finalPublish == "2"){
				  document.getElementById("publishChooseSelect_0").innerText = bulAuditer;
				  if(flag=="checked" || flag=="flag"){
					  document.getElementById('publishChoose').style.display='block';
				      if(document.getElementById("publishChoose").value == 2){
				    	  document.getElementById('writePublishSelLi').style.display='block';
				    	  document.getElementById('writePublishWriLi').style.display='block';
				      }
				  } else {
					  document.getElementById('publishChoose').style.display='none';
					  document.getElementById('writePublishSelLi').style.display='none';
				      document.getElementById('writePublishWriLi').style.display='none';
				  }
			  }
		  } else if(bulAuditPublishChoose != "" && bulAuditPublishChoose =="1"){
			  if(flag=="checked" || flag=="flag"){
				  document.getElementById('publishChoose').style.display='block';
				  document.getElementById('writePublishSelLi').style.display='block';
		          document.getElementById('writePublishSelLi').style.display='block';
			  } else {
				  document.getElementById('publishChoose').style.display='none';
				  document.getElementById('writePublishSelLi').style.display='none';
				  document.getElementById('writePublishSelLi').style.display='none';
			  }
		  } else if(bulAuditPublishChoose != "" && bulAuditPublishChoose =="2"){
			  if(flag=="checked" || flag=="flag"){
				  document.getElementById('publishChoose').style.display='block';
				  document.getElementById('writePublishSelLi').style.display='none';
		    	  document.getElementById('writePublishWriLi').style.display='block';
			  } else {
				  document.getElementById('publishChoose').style.display='none';
				  document.getElementById('writePublishSelLi').style.display='none';
			      document.getElementById('writePublishWriLi').style.display='none';
			  }
		  }

	  }
  }

//初始化查看阅读信息和公开阅读信息
function ORRInit(){
	if(document.getElementById('recordRead_em').className.indexOf('checked')==-1){
		document.getElementById('recordRead_em').className="checkBox checked";
		document.getElementById('recordReadId').value = '1';
		document.getElementById('openRecordsReadLi').style.display='block';
	}
	if(document.getElementById('openRecordsRead_em').className.indexOf('checked')==-1){
		document.getElementById('openRecordsReadLi').style.display='block';
		document.getElementById('openRecordsRead_em').className="checkBox checked";
		document.getElementById('openRecordsRead').value = '1';
	}
}
