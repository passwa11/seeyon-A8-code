/**
 * 新建调查页面
 */
var proce; //进度条
function closeProce(){
    if(proce){
        proce.close();
    }
}
$(function () {
    //window.onbeforeunload = function(){unlock($("#inquiryId").val());};
    //var _length = $(".question_num").length;
    //pageFormat();
    isFormSubmit = true;
    $.confirmClose();


    dragFormat();
    $(".content_container").click(saveAllOptEdit);
    titleClick();
    if($("#inquiryState").val()=="4"){
        $("#traftButton").hide();
    }

    if($("#inquiryTypeOf").val() == '3'){
        $("#boardType option[value='group']").attr("selected", true);
    }else if($("#inquiryTypeOf").val() =='2'){
        $("#boardType option[value='account']").attr("selected", true);
    }else if($("#inquiryTypeOf").val() =='4'){
        $("#boardType option[value='costom']").attr("selected", true);
    }
    changeSelect();
    $("#boardList option[value='"+ $("#inquiryTypeId").val() +"']").attr("selected", true);
    changeBoard();

    $("#inquiryQuBar > li").click(function(e){
        var qType = parseInt($(this).val());
        createQu(qType);
        e.stopPropagation();
    });

    $("#step2Confirm").attr("onclick","step2Confirm()");

    $(".left_content_list li").each(function(){
       $(this).hover(function(){
           var qType = $(this).val();
           if(qType == "0"){
               $(this).find("span").removeClass("examine_radio");
               $(this).find("span").addClass("examine_radio_hover");
           }
           if(qType == "1"){
               $(this).find("span").removeClass("examine_checkbox");
               $(this).find("span").addClass("examine_checkbox_hover");
           }
           if(qType == "4"){
               $(this).find("span").removeClass("examine_imageRadio");
               $(this).find("span").addClass("examine_imageRadio_hover");
           }
           if(qType == "5"){
               $(this).find("span").removeClass("examine_imageCheckbox");
               $(this).find("span").addClass("examine_imageCheckbox_hover");
           }
           if(qType == "3"){
               $(this).find("span").removeClass("examine_singleInput");
               $(this).find("span").addClass("examine_singleInput_hover");
           }
           if(qType == "2"){
               $(this).find("span").removeClass("examine_multiInput");
               $(this).find("span").addClass("examine_multiInput_hover");
           }
       },function(){
           var qType = $(this).val();
           if(qType == "0"){
               $(this).find("span").addClass("examine_radio");
               $(this).find("span").removeClass("examine_radio_hover");
           }
           if(qType == "1"){
               $(this).find("span").addClass("examine_checkbox");
               $(this).find("span").removeClass("examine_checkbox_hover");
           }
           if(qType == "4"){
               $(this).find("span").addClass("examine_imageRadio");
               $(this).find("span").removeClass("examine_imageRadio_hover");
           }
           if(qType == "5"){
               $(this).find("span").addClass("examine_imageCheckbox");
               $(this).find("span").removeClass("examine_imageCheckbox_hover");
           }
           if(qType == "3"){
               $(this).find("span").addClass("examine_singleInput");
               $(this).find("span").removeClass("examine_singleInput_hover");
           }
           if(qType == "2"){
               $(this).find("span").addClass("examine_multiInput");
               $(this).find("span").removeClass("examine_multiInput_hover");
           }
       })
    });

    if($('#isEdit').val()=="true"){
        goStep(2,1);
    }else{
        goStep(parseInt($("#step").val()));
    }

});
function step2Confirm(){
    if(saveAllOptEdit()){
        if(checkData(2)){
            saveAll(2);
            goStep(3,2);
        }
    }
    return null;
}
/**
 * 拖动初始化
 */
function dragFormat() {
    $("#inquiryQuBox").sortable({
        revert: true,
        items: "li.question",
        axis: "y",
        placeholder: 'holderCss',
        handle:".Drag_area",
        start:function(event,ui){
            $(".optSortX .dragZone").hide();
            isDragFlag = true;
        },
        stop:function(event,ui){
            $(".optSortX .dragZone").show();
            isDragFlag = false;
        },
        receive: function (event, ui) {
            var quType = ui.item.val();
            var qu = new Qu().initQu(quType,null);
            var temp = $("#inquiryQuBox li.ui-draggable");
            temp.after(qu).remove();
            reSetAllQuNum();
        },
        deactivate: function(event,ui){
            reSetAllQuNum();
        },
        over: function(event, ui){
            //预留备用
            checkFirstQu();
        },
        out: function(event,ui){
            checkLastQu();
        }
    });

    $("#inquiryQuBar > li").draggable({
        appendTo: "body",
        connectToSortable: "#inquiryQuBox",
        helper: function (event) {
            var qu = new Qu();
            return qu.getQuPreview($(this).val());
        }
        //handel

    });
}
function titleClick(){
    //加特技-题目标题修改
    $(".right_ul_title,.other_li").click(function(){
        if($(this).find(".edit_textarea").length>0){
            return false;
        }
        saveAllOptEdit();
        var _text = $.trim($(this).text());
        $(this).empty();
        var textInput = $("<input type='text' class='edit_textarea' style='font-size: 16px;width: 100%;height: 38px;line-height: 38px;border: 1px solid #42b3e5;'/>");
        textInput.val(_text);
        $(this).append(textInput);
        return false;
    });
    $(".question_msg").click(function(){
        if($(this).attr("beginEdit")=="1"){
            return false;
        }
        saveAllOptEdit();
        $(this).css("border","1px solid #42b3e5");
        $(this).attr("beginEdit","1");
        //$(this).empty().append("<textarea  rows='3' class='edit_textarea' style='height:65px;line-height:normal;margin-top: 15px;font-size: 16px;width: 100%;border: 1px solid #42b3e5;'>"+$(this).html()+"</textarea>");
        return false;
    });
}
/**
 * 跳转到步骤
 * @param num
 * @param from
 */
function goStep(num,from) {
    debugger;
    var s1 = $("#step_1");
    var s2 = $("#step_2");
    var rightBox = $("#rightBox");
    var s3 = $("#step_3");
    switch (num) {
        case 1:
            if(from==2){
                if($("#inquiryType").val()!="new"){
                    $.confirm({
                        "msg" : $.i18n('inquiry.temp.emptyconfirm'),
                        ok_fn : function() {
                            $(".top_ul li").removeClass("current");
                            $(".top_ul li:eq(0)").addClass("current");
                            s1.show(0,null);
                            rightBox.hide(0,null);
                            s2.hide(0,null);
                            s3.hide(0,null);
                            $("#step").val(num);

                            $("#inquiryName").val("");
                            $(".right_ul_title").text("");
                            $("#titleImgUrl").val("");
                            $("#titleImgShow").hide().attr("src","");

                            $(".question_msg").val($.i18n('inquiry.default.beforedesc'));
                            $(".other_li").text($.i18n('inquiry.default.afterdesc'));
                            $("#inquiryQuBox > li").remove();
                            $("#noQuTips").show();

                            $("#issueArea").val("");
                            $("#inquiryScope").val("");
                            elements_spGroup="";
                            elements_spAccount="";
                            $("#inquiryVoteType2").attr("checked",true);
                            //删附件
                            deleteAllAttachment(0,"atts1");
                            deleteAllAttachment(2,"atts2");
                        },
                        cancel_fn : function() {
                            return null;
                        }
                    });
                }else{
                    $(".top_ul li").removeClass("current");
                    $(".top_ul li:eq(0)").addClass("current");
                    var text = $(".right_ul_title").text();
                    $("#inquiryName").val(text);
                    s1.show(0,null);
                    rightBox.hide(0,null);
                    s2.hide(0,null);
                    s3.hide(0,null);
                    $("#step").val(num);
                }
            }else{
                $(".top_ul li").removeClass("current");
                $(".top_ul li:eq(0)").addClass("current");
                s1.show(0,null);
                rightBox.hide(0,null);
                s2.hide(0,null);
                s3.hide(0,null);
                $("#step").val(num);
            }
            break;
        case 2:
            if(from!=1||checkData(from)){
                $(".top_ul li").removeClass("current");
                $(".top_ul li:eq(2)").addClass("current");
                if(from!=3){
                    if($("#inquiryType").val()!="new"||$("#isEdit").val()=="true"){
                        var tempId = $("#tempList input:checked").val();
                        var isTemp = false;
                        if(tempId!=null||tempId!=""){
                            isTemp = true;
                        }
                        if($("#isEdit").val()=="true") {
                            tempId = $("#inquiryId").val();
                        }
                        $("#inquiryTempId").val(tempId);
                        var data = {
                            inquiryId : tempId
                        };
                        //载入模版
                        ajax_inquiryManager.loadTempOrEdit(data, {
                            success: function (rv) {
                                var temp = eval("("+rv+")");
                                var pac =temp["package"];
                                var quList = temp["questions"];
                                var meta = temp["metaData"];
                                $(".right_ul_title").text(pac.inquiryTitle);
                                $("#inquiryName").val(pac.inquiryTitle);
                                //$("#inquiryName").text(pac.inquiryTitle);
                                $(".question_msg").val(pac.inquiryBefore);
                                $(".other_li").text(pac.inquiryAfter);
                                $(".other_li").text(pac.inquiryAfter);
                                $("#titleImgUrl").val(pac.inquiryImg);
                                if(pac.inquiryImg!=""&&pac.inquiryImg!=null&&pac.inquiryImg!="null"){
                                    $("#titleImgShow").attr("src",_ctxPath + "/commonimage.do?method=showImage&id=" + pac.inquiryImg+ "&size=custom&h=320&w=880").show();
                                }
                                if(from==1){
                                    $("#inquiryQuBox > li").remove();
                                }
                                for(var i = 0;i<quList.length;i++){
                                    var quData = quList[i];
                                    var qu = new Qu().initQu(null,quData);
                                    $("#inquiryQuBox").append(qu);
                                }
                                $("#noQuTips").hide();
                                reSetAllQuNum();

                                var deptElement = parseElements(meta.inquiryDeptId);
                                var deptName = getNamesString(deptElement);
                                var deptScope = getIdsString(deptElement,true);
                                var inquiryScope = meta.inquiryScope;

                                $("#beginDeptName").val(deptName);
                                $("#beginDeptId").val(meta.inquiryDeptId);
                                $("#selectBeginDept").val(deptScope);
                                elements_spDEPT = deptElement;

                                var inquriyScopeElement = parseElements(inquiryScope);
                                if($("#isEdit").val()!="true"&&isTemp) {
                                    inquriyScopeElement= "";
                                }
                                if(meta.inquiryBoardType == 3){

                                    $("#boardType option[value='group']").attr("selected", true);
                                    changeSelect();
                                    $("#boardList option[value='"+ meta.inquiryBoardId +"']").attr("selected", true);
                                    changeBoard('group');
                                    elements_spGroup = inquriyScopeElement;

                                }else if(meta.inquiryBoardType == 2){
                                    $("#boardType option[value='account']").attr("selected", true);
                                    changeSelect();
                                    $("#boardList option[value='"+ meta.inquiryBoardId +"']").attr("selected", true);
                                    changeBoard('account');
                                    elements_spAccount = inquriyScopeElement;
                                }else if(meta.inquiryBoardType == 4){
                                    $("#boardType option[value='custom']").attr("selected", true);
                                    changeSelect();
                                    $("#boardList option[value='"+ meta.inquiryBoardId +"']").attr("selected", true);
                                    changeBoard('custom');
                                    elements_spCustomSpace = inquriyScopeElement;
                                }else if(meta.inquiryBoardType == 17){
                                    $("#boardList option[value='"+ meta.inquiryBoardId +"']").attr("selected", true);
                                    changeBoard('account');
                                    elements_spAccount = inquriyScopeElement;
                                }else if(meta.inquiryBoardType == 18){
                                    $("#boardList option[value='"+ meta.inquiryBoardId +"']").attr("selected", true);
                                    changeBoard('group');
                                    elements_spGroup = inquriyScopeElement;
                                }

                                if($("#isEdit").val()!="true"&&isTemp) {
                                    $("#issueArea").val("");
                                    $("#inquiryScope").val("");
                                }else{
                                    $("#issueArea").val(getIdsString(inquriyScopeElement));
                                    $("#inquiryScope").val(getNamesString(inquriyScopeElement));
                                }

                                $("#closeDate").val(meta.inquiryCloseTime.substring(0,16));

                                $("input#inquiryVoteType1").removeAttr("checked");
                                $("input#inquiryVoteType2").removeAttr("checked");
                                if(meta.inquiryVoteType==1){
                                    $("#inquiryVoteType2").attr("checked",true);
                                }else{
                                    $("#inquiryVoteType1").attr("checked",true);
                                    checkIsSecret();
                                }
                                $("#inquiryResultBeforeJoin").removeAttr("checked");
                                $("#inquiryVoteBackDoor").removeAttr("checked");
                                $("#inquiryResultAfterEnd").removeAttr("checked");
                                $("#inquiryResultBeforeJoin").attr("checked",meta.inquiryResultBeforeJoin==1);
                                $("#inquiryVoteBackDoor").attr("checked",meta.inquiryVoteBackDoor==1);
                                $("#inquiryResultAfterEnd").attr("checked",meta.inquiryResultAfterEnd==1);

                                if($("#isEdit").val()=="true") {
                                    $("#boardType").disable();
                                }

                                deleteAllAttachment(0,"atts1");
                                deleteAllAttachment(2,"atts2");
                                dymcCreateFileUpload("attachmentAreaatts1",10,null,null,null,null, "atts1",null,true,JSON.stringify(meta.inquiryAtts),null,null);
                                dymcCreateAssdoc("attachment2Areaatts2", "atts2",null,JSON.stringify(meta.inquiryAtts));

                            },
                            error: function (data) {
                                $.alert($.i18n('inquiry.error.tips'));
                            }
                        });
                    }else{
                        //$(".right_ul_title").text(pac.inquiryTitle);
                        if(from==1){
                            //$(".question_msg").text("感谢您对此次调查的理解和支持！");
                            //$(".other_li").text("您已完成本次问卷，感谢您的帮助与支持。");
                            //$("#inquiryQuBox > li").remove();
                            //$("#noQuTips").show();
                        }

                    }
                }
                s1.hide(0,null);
                s2.show(0,null);
                rightBox.show(0,null);
                s3.hide(0,null);
                $("#step").val(num);
            }
            break;
        case 3:
            if(checkData(from)) {
                $(".top_ul li").removeClass("current");
                $(".top_ul li:eq(4)").addClass("current");
                s1.hide(0, null);
                rightBox.hide(0, null);
                s2.hide(0, null);
                s3.show(0, null);
                $("#step").val(num);
            }
            break;
        case 4:
            proce = $.progressBar();
            var antiDbSignal = $("#step3Confirm").attr("dbSignal");
            if(antiDbSignal != 1){
                $("#step3Confirm").attr("dbSignal",1);
                if(checkData(from)){
                    saveAll(3);
                    createInquiry("create");
                }else{
                    closeProce();
                }
            }
            $("#step3Confirm").attr("dbSignal",0);
            break;
        default:
//            $.alert("我练功发自真心！");
            break;
    }

}
function checkData(stepNo,isTemp) {
    //校验数据
    switch (stepNo) {
        case 1:
            var inquiryType = $("#inquiryType").val();
            if($("#isEdit").val()=="true"){
                return true;
            }
            if(inquiryType=="new"){
                var inquiryNameInput = $("#inquiryName");
                var inquiryName = inquiryNameInput.val();
                if($.trim(inquiryName)==""||inquiryName==null){
                    inquiryNameInput.removeClass("error");
                    inquiryNameInput.addClass("error");
                    quFormatAlert($.i18n('inquiry.qu.check.title1'));
                    return false;
                }else if(getReallength(inquiryName)>170){
                    inquiryNameInput.removeClass("error");
                    inquiryNameInput.addClass("error");
                    quFormatAlert($.i18n('inquiry.qu.check.title2'));
                    return false;
                }else{
                    inquiryNameInput.removeClass("error");
                    $(".right_ul_title").text(inquiryName);//写标题
                    return true;
                }
            }else if(inquiryType=="temp"){
                if($("#tempList input:checked").length==0){
                    quFormatAlert($.i18n('inquiry.temp.choose'));
                    return false;
                }else{
                    return true;
                }
            }
            break;
        case 2:
            var isGood = true;

            if(!checkInquiryTitle()){
                return false;
            }
            if(!checkDesc()){
                return false;
            }
            if(!checkDesc2()){
                return false;
            }
            var quList = $("#inquiryQuBox li.question");
            if(quList.length==0){//没有题目
                quFormatAlert($.i18n('inquiry.qu.check.qu3'));
                return false;
            }else{
                quList.each(function(){
                    var qType = $(this).attr("qtype");
                    if(qType == 0||qType==1||qType == 4||qType==5){
                        var optList = $(this).find(".choose_msg_info");

                        if(optList.length<2){
                            quFormatAlert($.i18n('inquiry.qu.check.optnum4'));
                            isGood = false;
                            return false;
                        }
                    }
                });
            }
            return isGood;
            break;
        case 3:
            if(isTemp!='temp'){
                if($("#issueArea").val()==null||$("#issueArea").val()==""){
                    quFormatAlert($.i18n('inquiry.create.check.scope'));
                    return false;
                }
            }
            if($("#selectBeginDept").val()==null||$("#selectBeginDept").val()==""){
                quFormatAlert($.i18n('inquiry.create.check.dept'));
                return false;
            }
            if($("#inquiryVoteType2").is(":checked")&&$("#inquiryVoteType2").is(":disabled")){
                quFormatAlert($.i18n('inquiry.create.check.noname'));
                return false;
            }
            return true;
            break;
        default:
//            $.alert("我练功发自真心！");
            break;
    }
}
function directCreate(){
    $("#myTempTable").hide();
    $("#inquiryType").val("new");
    $("#inquiryTempId").val("");//写标题
    if($(this).hasClass("current")){
        return null;
    }else{
        $(".question_title").show();
        $(".flexigrid").hide();
        $("#inquiryName").show();
        $("#chooseTemp").removeClass("current");
        $("#directCreate").addClass("current");
    }
}
function chooseTemp(){
    createMyTempTable();
    $("#inquiryType").val("temp");
    $("#inquiryTempId").val("");//写标题
    if($(this).hasClass("current")){
        return null;
    }else{
        $(".question_title").hide();
        $("#inquiryName").hide();
        $(".flexigrid").show();
        $("#directCreate").removeClass("current");
        $("#chooseTemp").addClass("current");
    }
}

/**
 * 前端现有的初始化代码
 */
function pageFormat() {
    $(".q_title").mouseenter(function () {
        $(this).parent().parent().siblings(".title_set").show();
        var _text = $.trim($(this).text());
        $(this).empty().append("<textarea class='edit_title_textarea'>" + _text + "</textarea>");
    }).mouseleave(function () {
        var _text = $(this).find(".edit_title_textarea").val();
        $(this).empty().text(_text);
        $(this).parent().parent().siblings(".title_set").hide();
    });
    $(".msg_li .choose_msg_info,.Imgli .choose_msg_info").mouseenter(function () {
        $(this).siblings(".fast_machine").show();
        $(this).parent().parent().parent().parent().siblings(".choose_set").show();
        var _text = $.trim($(this).text());
        $(this).empty().append("<textarea class='edit_textarea'>" + _text + "</textarea>");
    }).mouseleave(function () {
        var _text = $(this).find(".edit_textarea").val();
        $(this).empty().text(_text);
        $(this).siblings(".fast_machine").hide();
        $(this).parent().parent().parent().parent().siblings(".choose_set").hide();


    });
    $('.module').mouseenter(function () {
        $(this).find(".setup-group em,.operationH em").css("display", "inline-block");
    }).mouseleave(function () {
        $(this).find(".setup-group em,.operationH em").css("display", "none");
    });

    $(".logistic_set .examine_logistic_24").click(function () {
        $(this).parent().parent().parent().parent().siblings(".info_corner,.info_corner1,.logistic_set_info").show();
    })
}

/**
 * 保存step2的信息
 * {"package":{"inquiryId":"123456","inquiryTitle":"标题","inquiryBefore":"前导言","inquiryImg":"图片URL","inquiryAfter":"结束语"},"questions":[{"qId":"a001","qContent":"内容","qType":0,"isRandom":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]},{"qId":"a001","qContent":"内容","qType":0,"isRandom":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]}],"metaData":{"inquiryBoardId":"版块ID","inquiryDeptId":"部门ID","inquiryScope":"发布范围","inquiryCloseTime":"发布范围","inquiryVoteType":0,"inquiryVoteBackDoor":0,"inquiryJoinResult":0,"inquiryResultBeforeJoin":0,"inquiryResultBeforeEnd":0,"inquiryPushResult":0}}
 * @param step
 */
function saveAll(step) {
    if(step==2){
        //{"inquiryId":"123456","inquiryTitle":"标题","inquiryBefore":"前导言","inquiryAfter":"结束语","questions":[{"qId":"a001","qContent":"内容","qType":0,"isRandom":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]},{"qId":"a001","qContent":"内容","qType":0,"isRandom":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]}]}
        var inquiryId = $("#inquiryId").val();
        var inquiryTitle = escapeStringToJavascript($.trim($("li.right_ul_title").text()));
        var inquiryBefore = valueReplace($(".question_msg").val());
        var inquiryAfter = escapeStringToJavascript($.trim($("li.other_li").text()));
        var titleImgUrl = $.trim($("#titleImgUrl").val());
        var packageStr = "\"package\": {" +
            "\"inquiryId\": \""+ inquiryId +"\"," +
            "\"inquiryTitle\": \"" + inquiryTitle + "\"," +
            "\"inquiryBefore\": \"" + inquiryBefore + "\"," +
            "\"inquiryImg\": \""+titleImgUrl+"\"," +
            "\"inquiryAfter\": \""+ inquiryAfter +"\"" +
            "}";

        $("#packageStr").val(packageStr);


        var questionStr = "";
        var i = 0;
        $("#inquiryQuBox li.question").each(function () {
            // var o={"xlid":"cxh","xldigitid":123456,"topscore":2000,"topplaytime":"2009-08-20"}
            var qJson = getQuInfo(this) + ",";
            questionStr = questionStr + qJson;
            i++;
        });
        if(i>0){
            questionStr = questionStr.substring(0, questionStr.length - 1);
        }
        questionStr = "\"questions\":["+ questionStr +"]";

        $("#questionStr").val(questionStr);

        return true;
    }else if(step==3){
        //"metaData":{"inquiryBoardId":"版块ID","inquiryDeptId":"部门ID","inquiryScope":"发布范围","inquiryCloseTime":"发布范围","inquiryVoteType":0,"inquiryVoteBackDoor":0,"inquiryJoinResult":0,"inquiryResultBeforeJoin":0,"inquiryResultBeforeEnd":0,"inquiryPushResult":0}
        var inquiryBoardId = $("#boardList").val();
        var beginDeptId = ($("#selectBeginDept").val());
        var issueArea = $("#issueArea").val();
        var closeDate = $("#closeDate").val();
        var inquiryVoteType = $("#inquiryVoteType1").attr("checked")=="checked"?0:1;//0为实名
        var inquiryVoteBackDoor = $("#inquiryVoteBackDoor").attr("checked")=="checked"?1:0;//1为可看
        var inquiryResultBeforeJoin = $("#inquiryResultBeforeJoin").attr("checked")=="checked"?1:0;
        var inquiryResultBeforeEnd = $("#inquiryResultAfterEnd").attr("checked")=="checked"?1:0;
        var metaDataStr = "\"metaData\": {"+
            "\"inquiryBoardId\": \""+ inquiryBoardId +"\","+
            "\"inquiryDeptId\": \""+ beginDeptId.replace("Department|","") +"\","+
            "\"inquiryScope\": \""+ issueArea +"\","+
            "\"inquiryCloseTime\": \""+ closeDate +"\","+
            "\"inquiryVoteType\": "+ inquiryVoteType +","+
            "\"inquiryVoteBackDoor\": "+ inquiryVoteBackDoor +","+
            "\"inquiryJoinResult\": 0,"+  //允许参与人查看评论内容 没用
            "\"inquiryResultBeforeJoin\": "+ inquiryResultBeforeJoin +","+ //允许提交前查看内容
            "\"inquiryResultAfterEnd\": "+ inquiryResultBeforeEnd +","+ //允许提交后查看内容
            "\"inquiryPushResult\": 0"+ //调查结束后推送调查结果 没用
            "}";

        $("#metaData").val(metaDataStr);
    }
}

function createInquiry(type){
    var packageStr = $("#packageStr").val();
    var questionStr =$("#questionStr").val();
    var metaData =$("#metaData").val();

    var isEdit = $("#isEdit").val();

    $("#atts2").children().remove();
    saveAttachmentPart("atts2");
    var attachList2 = $("#atts2").formobj();

    $("#atts1").children().remove();
    saveAttachmentPart("atts1");
    var attachList1 = $("#atts1").formobj();

    if(packageStr!=null&&packageStr!=""&&questionStr!=null&&questionStr!=""&&metaData!=null&&metaData!=""){
        var strJson = "{"+packageStr+","+questionStr+","+metaData+"}";
        var data = {
            inquiryJson: strJson,
            createType:type,
            isEdit:isEdit,
            atts1:attachList1,
            atts2:attachList2
        };
        ajax_inquiryManager.createInquiry(data, {
            success: function (rv) {
                closeProce();
                var info = eval("("+rv+")");
                if(info.state=="0"&&type!="censor"){
                    $.messageBox({
                        'type' : 0,
                        'msg' : info.msg,
                        'imgType':0,
                        close_fn : function() {
                            if(type!="temp"){
                                //parent.window.opener.location.reload();
                                //unlock($("#inquiryId").val(),true);
                                isFormSubmit = false;
                                try {
                                    if(window.opener){
                                        if(window.opener.getCtpTop().isCtpTop){
                                            window.opener.getCtpTop().reFlesh();
                                        }else{
                                            window.opener.location.reload();
                                        }
                                    }
                                }catch (e){

                                }
                                window.close();
                            }
                        },
                        ok_fn : function() {
                            if(type!="temp"){
                                //parent.window.opener.location.reload();
                                //unlock($("#inquiryId").val(),true);
                                isFormSubmit = false;
                                try {
                                    if(window.opener){
                                        if(window.opener.getCtpTop().isCtpTop){
                                            window.opener.getCtpTop().reFlesh();
                                        }else{
                                            window.opener.location.reload();
                                        }
                                    }
                                }catch (e){

                                }
                                window.close();
                            }
                        }
                    });
                }else if(info.state=="0"&&type=="censor"){
                    //unlock($("#inquiryId").val(),true);
                    isFormSubmit = false;
                    try {
                        if(window.opener){
                            if(window.opener.getCtpTop().isCtpTop){
                                window.opener.getCtpTop().reFlesh();
                            }else{
                                window.opener.location.reload();
                            }
                        }
                    }catch (e){

                    }
                    window.close();
                }else{
                    if(info.state=="-1"&&type=="temp"){//如果有同名模板，弹出覆盖原模板确认框
                        $.confirm({
                            "msg" : info.msg,
                            ok_fn : function() {
                                data.overrideTemp = "1";
                                ajax_inquiryManager.createInquiry(data,{
                                    success : function(rv) {
                                        closeProce();
                                        var rvv = eval("("+rv+")");
                                        $.infor(rvv.msg);
                                        //刷新模板列表数据
                                        $("#tempList").ajaxgridLoad();
                                    },
                                    error: function (data) {
                                        closeProce();
                                        $("#step3Confirm").attr("warnMsg",$.i18n('inquiry.error'));
                                    }
                                });
                            },
                            cancel_fn : function() {
                                return null;
                            }
                        });
                    }else{
                        $.alert(info.msg);
                    }
                }
            },
            error: function (data) {
                closeProce();
                $("#step3Confirm").attr("warnMsg",$.i18n('inquiry.error'));
            }
        });

    }else{
        $.alert($.i18n('inquiry.message.not.enough'));
    }
}
/**
 * 获取小题Json字符串
 *"questions":[{"qId":"a001","qContent":"内容","qType":0,"qMin":1,"qMax":1,"qJump":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]},{"qId":"a001","qContent":"内容","qType":0,"isRandom":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]}]
 * @param qu
 * @returns {string}
 */
function getQuInfo(qu) {
    //{"qId":"a001","qContent":"内容","qType":0,"isRandom":0,"qOpts":[{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}]}
    var qId = $(qu).attr("qId");
    var qContent = escapeStringToJavascript($.trim($(qu).find("div.q_title").text()));
    var qType = $(qu).attr("qType");
    var min_num = $(qu).find("input.min_num").val();
    var max_num = $(qu).find("input.max_num").val();
    var isJump = $(qu).find("isJump").attr("checked")?1:0;

    if(min_num==null||min_num==""||isNaN(min_num)){
        min_num = 0;
    }
    if(max_num==null||max_num==""||isNaN(max_num)){
        max_num = 0;
    }
    var quJson = "{" +
                    "\"qId\":\"" + qId + "\"," +
                    "\"qContent\":\"" + qContent + "\"," +
                    "\"qType\":\"" + qType + "\"," +
                    "\"qMin\": "+min_num+"," +
                    "\"qMax\": "+max_num+"," +
                    "\"qJump\": "+isJump+"," +
                    "\"qOpts\":[";
    if (qType == 0 || qType == 1) {//单多选
        //{"oId":"o001","oContent":"选项内容","oUrl":""},{"oId":"o002","oContent":"选项内容","oUrl":""}
        var optJson = "";
        var i = 0;
        $(qu).find("ul.unstyled .msg_li").each(function () {
            var optContent = escapeStringToJavascript($.trim($(this).find(".T_edit_min span").text()));
            var optExtContent = escapeStringToJavascript($.trim($(this).find(".T_edit_min > input").val()));
            optJson = optJson + "{\"oId\":\"" + i + "\"," +
                "\"oContent\":\"" + optContent + "\"," +
                "\"oExtContent\":\"" + optExtContent + "\"," +
                "\"oUrl\":\"\"},";
            i++;
        });
        if (i > 0) {
            optJson = optJson.substring(0, optJson.length - 1);
        }
        quJson = quJson + optJson;
    } else if (qType == 4||qType == 5) {//单多图选
        var optJson = "";
        var i = 0;
        $(qu).find(".unstyled li.imgQu").each(function () {
            var optContent = valueReplace($.trim($(this).find("label").text()));
            optJson = optJson + "{\"oId\":\"" + i + "\"," +
                "\"oContent\":\"" + optContent + "\"," +
                "\"oExtContent\":\"" + 0 + "\"," +
                "\"oUrl\":\""+$(this).attr("id")+"\"},";
            i++;
        });
        if (i > 0) {
            optJson = optJson.substring(0, optJson.length - 1);
        }
        quJson = quJson + optJson;
    } else if (qType == 2) {
        optJson = "{\"oId\": \"o001\",\"oContent\": \"\",\"oUrl\": \"\"}";
        quJson = quJson + optJson;
    } else if (qType == 3) {
        optJson = "{\"oId\": \"o001\",\"oContent\": \"\",\"oUrl\": \"\"}";
        quJson = quJson + optJson;
    }
    quJson = quJson + "]}";
    return quJson;
}


function changeSelect(){
    if($("#boardType").val()=="group"){
        $("#boardList").empty();
        $("#boardList").append($(groupOption));
        $("#boardList option[bType='group']:first").attr("selected", true);
    }
    if($("#boardType").val()=="account"){
        $("#boardList").empty();
        $("#boardList").append($(accountOption));
        $("#boardList option[bType='account']:first").attr("selected", true);
    }
    if($("#boardType").val()=="custom"){
      $("#boardList option[bType='custom']:first").attr("selected", true);
    }
    changeBoard($("#boardType").val());
}

function changeBoard(key){
    if(!_isCustom){
      includeElements_spGroup="";
      elements_spGroup="";
      includeElements_spAccount="";
      elements_spAccount="";
      //zhou
      // $("#issueArea").val("");
      // $("#inquiryScope").val("");
      $("#inquiryVoteType1").removeAttr("checked");
	  $("#inquiryVoteType2").attr("checked",true).removeAttr("disabled");
	  $("#backDoor").show();
	  $("#spaninquiryVoteType2").css("color","");
	  var state = $("#boardList :selected").attr("state");
      if(state == "1"){
		   $("#inquiryVoteType1").attr("checked",true);
		   $("#inquiryVoteType2").attr("disabled",true);
		   $("#spaninquiryVoteType2").css("color","#dddddd");
           $("#backDoor").hide();
      }
      //选人点击事件
      if(key=="account"){
        document.getElementById("inquiryScope").onclick=function(event,data){
          selectIssueArea('account');
        }
        onlyLoginAccount_spDEPT = true;
		hiddenOtherMemberOfTeam_spDEPT = true;
      }else if(key=="group"){
        document.getElementById("inquiryScope").onclick=function(event,data){
          selectIssueArea('group');
        }
        onlyLoginAccount_spDEPT = false;
		hiddenOtherMemberOfTeam_spDEPT = false;
      }
    }

    var auth = $("#boardList :selected").attr("isAuth");
    if(auth!="1"){
        $("#step_3 .confirm_button").text($.i18n('inquiry.create.stepbutton.auth'));
    }else{
        $("#step_3 .confirm_button").text($.i18n('inquiry.create.stepbutton.submit'));
    }
}

/**
 * 我的模版列表
 */
function createMyTempTable(){
    $("#tempList").ajaxgrid({
        managerName : "inquiryManager",
        managerMethod : "createMyTempTable",
        usepager : true,
        slideToggleBtn : false,
        customize : false,
        resizable : false,
        //params :{censor : "all"},
        //render : iStartedRend,
        //onSuccess : function() {
        //    $("#yxtTable input[type='checkbox']").each(function() {
        //        if ($("#span" + $(this).val()).attr("value") == "1") {
        //            $(this).attr("checked", true);
        //        }
        //    });
        //},
        height : 630,
        gridType:'autoGrid',
        colModel : [ {
            display : 'id',
            name : 'id',
            width : 'smallest',
            align : 'center',
            type : 'radio'
        }, {
            display : $.i18n('inquiry.create.loadtemp.th1'),
            name : 'surveyName',
            width : 'medium',
            sortname : 'surveyName',
            sortable : true
        }, {
            display : $.i18n('inquiry.create.loadtemp.th2'),
            name : 'surveyTypeName',
            width : 'medium',
            sortname : 'surveyTypeName',
            sortable : true
        },{
            display : $.i18n('inquiry.create.loadtemp.th3'),
            name : 'sendDate',
            width : 'medium',
            sortname : 'sendDate',
            sortable : true
        }]
    });
    $("#tempList").ajaxgridLoad();
}
/**
 * 单击创建
 * @quType
 */
function createQu(quType){
    reSetAllQuNum();
    checkFirstQu();
    var qu = new Qu().initQu(quType,null);
    $("#inquiryQuBox").append(qu);
    reSetAllQuNum();
    var _top = parseInt($(".content_container").scrollTop()) + parseInt($("li.question:last").offset().top);
    $('.content_container').animate({
        scrollTop: _top
    }, 1000);
    return false;
}

function checkIsSecret(){
    var isChecked = $("#inquiryVoteType1").attr("checked");
    if(isChecked == "checked"){
        $("#backDoor").attr("checked",true).hide();
    }else{
        $("#backDoor").removeAttr("checked").show();
    }
}

function previewInquiry(){
    saveAll(2);
    saveAll(3);
    $("#atts1").children().remove();
    saveAttachmentPart("atts1");
    var attachList = $("#atts1").formobj();
    $("#preAttachment").val($.toJSON(attachList).toString());
    document.loginForm.submit();
}

function upLoadTitleImg(){
    var poiAddParamImageId = "titleImgDiv";
    dymcCreateFileUpload("titleImgDivParam", 10, 'jpg,jpeg,gif,bmp,png', 1, false, 'addTitleImageCallBack',poiAddParamImageId , true, true, null, false, true);
    insertAttachmentPoi(poiAddParamImageId);

}
function addTitleImageCallBack(attachments){
    $("#titleImgUrl").val("");
    $("#titleImgShow").attr("src","");
    var attachment = attachments.instance[0];
    $("#titleImgUrl").val(attachment.fileUrl);
    $("#titleImgShow").attr("src",_ctxPath + "/commonimage.do?method=showImage&id=" + attachment.fileUrl+ "&size=custom&h=320&w=880").show();
}
function removeTitleImg(){
    $.confirm({
        "msg" : $.i18n('inquiry.create.cleanimg.confirm'),
        ok_fn : function() {
            $("#titleImgUrl").val("");
            $("#titleImgShow").hide().attr("src","");
        },
        cancel_fn : function() {
            return null;
        }
    });
}
