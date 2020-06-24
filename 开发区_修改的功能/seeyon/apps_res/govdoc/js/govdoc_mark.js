var docMarkChooseUrl = "/govdoc/mark.do?method=openMarkChooseDialog&markType=0";
var serialNoChooseUrl = "/govdoc/mark.do?method=openMarkChooseDialog&markType=1";
var signMarkChooseUrl = "/govdoc/mark.do?method=openMarkChooseDialog&markType=2";
var formZwIframe;
var formContainer;

var nullColor = "#FCDD8B";
var notNullColor = "#FFFFFF";

var actionInit = "0";
var actionChangeWordNo = "1";
var actionChangeDuanhao = "2";
var actionChangeShouxie = "3";
var actionChangeToZidong = "4";
var actionFengsong = "5";

var selectTypeZidong = "0";
var selectTypeShouxie = "1";
var selectTypeDuanhao = "2";
var selectTypeYuliu = "3";
var selectTypeXianxia = "4";//线下占用，EdocMarkHistory中才会生成数据，前端无法选择

var docMarkFieldspan;
var docMarkField;
var docMarkFieldtxt;
var docMarkReadonly = false;

var serialNoFieldspan;
var serialNoField;
var serialNoFieldtxt;
var serialNoReadonly = false;

var signMarkFieldspan;
var signMarkField;
var signMarkFieldtxt;
var signMarkReadonly = false;

var serialNoFieldspan;
var serialNoField;
var serialNoFieldtxt;

var openFrom;
var markGovdocType = "";
var isLoadForm = false;
var isLoadDocMark = false;
var isLoadSerialNo = false;
var isLoadSignMark = false;

var docMarkElement = "doc_mark";
var serialNoElement = "serial_no";
var signMarkElement = "sign_mark";

var docMarkName = "edocDocMark";
var serialNoName = "edocInnerMark";
var signMarkName = "edocSignMark";

/**
 * 表单加载后回调方法
 */
function formLoadCallback(from) {
    //加载表单容器
    initFormZwIframe();

    if (!formContainer) {
        return;
    }

    //加载文单中公文文号
    initFormDocMark(docMarkElement, docMarkName);

    //加载文单中内部文号(包括分段的见办功能)
    initFormDocMark(serialNoElement, serialNoName);

    //加载文单中的签收编号
    initFormDocMark(signMarkElement, signMarkName);
}

/**
 * 机构代字切换事件回调方法
 */
function selectChangeCallBack(txtObj, selectObj) {
    try {
        //reloadGovdocForm();

        var objTxtId = txtObj.attr("id");
        if (objTxtId && objTxtId.indexOf("_txt") > 0) {
            var objId = objTxtId.substring(0, objTxtId.length - 4);
            var spanId = objId + "_span";

            //因为在select添加txt时，页面还未加载，需要手动加载当前页面
            initFormZwIframe();
            if (!formContainer) {
                return;
            }

            //公文文号
            var markName = docMarkName;
            var tempDocMarkFieldspan = formContainer.find("span[fieldval*='" + markName + "']");
            if (tempDocMarkFieldspan && tempDocMarkFieldspan.size() > 0 && tempDocMarkFieldspan.attr("id") == spanId) {
                var isNotNew = isLoadDocMark;

                //因为在select添加txt时，页面还未加载，需要手动初始化文号
                markType = "doc_mark";
                initFormDocMark(markType, markName);

                if (isNotNew) {
                    setWordNoOptionSelectedByHtml(markType, txtObj.val());
                    changeWordNoEvent(markType, actionChangeWordNo);
                }
            }

            //内部文号
            markName = serialNoName;
            var tempSerialNoFieldspan = formContainer.find("span[fieldval*='" + markName + "']");
            if (tempSerialNoFieldspan && tempSerialNoFieldspan.size() > 0 && tempSerialNoFieldspan.attr("id") == spanId) {
                //因为在select添加txt时，页面还未加载，需要手动初始化内部文号
                var isNotNew = isLoadSerialNo;

                markType = "serial_no";
                initFormDocMark(markType, markName);

                if (isNotNew) {
                    setWordNoOptionSelectedByHtml(markType, txtObj.val());
                    changeWordNoEvent(markType, actionChangeWordNo);
                }
            }

            //签收编号
            markName = signMarkName;
            var tempSignMarkFieldspan = formContainer.find("span[fieldval*='" + markName + "']");
            if (tempSignMarkFieldspan && tempSignMarkFieldspan.size() > 0 && tempSignMarkFieldspan.attr("id") == spanId) {
                //因为在select添加txt时，页面还未加载，需要手动初始化签收编号
                var isNotNew = isLoadSignMark;

                markType = "sign_mark";
                initFormDocMark(markType, markName);

                if (isNotNew) {
                    setWordNoOptionSelectedByHtml(markType, txtObj.val());
                    changeWordNoEvent(markType, actionChangeWordNo);
                }
            }
        }
    } catch (e) {
        alert($.i18n("govdoc.number.initialization.error") + e);
    }
}

/************** 文号页面加载 start ***********/
function initFormZwIframe(from) {
    if (isLoadForm) {
        return;
    }
    isLoadForm = true;

    openFrom = from;

    if ($("#subApp") && $("#subApp").val()) {
        markGovdocType = $("#subApp").val();
    }

    if (openFrom && "extend" == openFrom) {
        formZwIframe = $(document);
    } else {
        try {
            formZwIframe = $(window.frames["govDocZwIframe"].document);//拟文界面
        } catch (e) {
            try {
                formZwIframe = $(window.frames["componentDiv"].window.frames["zwIframe"].document);//处理界面
            } catch (e2) {
                openFrom = "extend";
                formZwIframe = $(document);
            }
        }
    }
    formContainer = formZwIframe;
    if (formZwIframe) {
        if (formZwIframe.find("#viewsTabs").find("li").size() > 1) {
            formZwIframe.find("#viewsTabs").find("li").each(function () {
                if ($(this).hasClass("current")) {
                    var index = $(this).attr("index");
                    formContainer = formZwIframe.find("#mainbodyHtmlDiv_" + index);
                }
            });
        }
    }
}

/************** 文号页面加载 end *************/

/************** 公文文号加载 start ***********/
function initFormDocMark(markType, markName) {
    if (markType == "doc_mark") {
        if (isLoadDocMark) {
            return;
        }
        isLoadDocMark = true;
    } else if (markType == "serial_no") {
        if (isLoadSerialNo) {
            return;
        }
        isLoadSerialNo = true;
    } else if (markType == "sign_mark") {
        if (isLoadSignMark) {
            return;
        }
        isLoadSignMark = true;
    }

    var fieldReadonly = false;
    var field;
    var fieldtxt;
    var fieldspan = formContainer.find("span[fieldval*='" + markName + "']");
    if (!fieldspan || fieldspan.size() == 0) {
        fieldspan = formContainer.find("#" + fieldId + "_span");
        if (!fieldspan || fieldspan.html() == "") {
            return;
        }
    }

    if (!fieldspan || fieldspan.size() == 0) {
        field = formContainer.find("[mappingField=" + markType + "]");
        if (field && field.size() > 0) {
            fieldspan = field.parent("span");
        }
    }
    if (!fieldspan || fieldspan.size() == 0) {
        return;
    }
    if (fieldspan.hasClass("browse_class")) {
        fieldReadonly = true;
    }

    //var field = formContainer.find("[mappingField=" + markType + "]");
    if (!field || field.size() == 0) {
        field = fieldspan.find("select").eq(0);//处理非映射文号字段
    }
    if (!field || field.size() == 0) {//收文公文文号
        field = fieldspan.find("input").eq(0);
    }
    if (!field || field.size() == 0) {//公文文号只读状态
        field = fieldspan.find("span").eq(0);
    }
    if (!field) {
        return;
    }

    var fieldId = field.attr("id");
    if (!fieldId || fieldId == "") {
        return;
    }

    if (!fieldtxt || fieldtxt.size() == 0) {
        fieldtxt = fieldspan.find("#" + fieldId + "_txt");
    }

    //设置机构字内容状态为实时更新
    if (fieldtxt && fieldtxt.size() > 0) {
        fieldtxt.attr("realUpdate", true);
    }

    if (markType == "doc_mark") {
        docMarkField = field;
        docMarkFieldspan = fieldspan;
        docMarkFieldtxt = fieldtxt;
        docMarkReadonly = fieldReadonly;
    } else if (markType == "serial_no") {
        serialNoField = field;
        serialNoFieldspan = fieldspan;
        serialNoFieldtxt = fieldtxt;
        serialNoReadonly = fieldReadonly;
    } else if (markType == "sign_mark") {
        signMarkField = field;
        signMarkFieldspan = fieldspan;
        signMarkFieldtxt = fieldtxt;
        signMarkReadonly = fieldReadonly;
    }

    if (!fieldtxt || !(fieldtxt.attr("id"))) {
        return;
    }

    if (!fieldReadonly) {
        //加载公文文号分段控件
        initMarkFenduan(markType);
        //加载公文文号手写输入框
        initMarkShouxie(markType);
        //加载公文文号小图标
        initMarkIcon(markType);
        //回填公文文号
        fillDocMarkData(markType);
        //加载文号样式
        initDocMarkStyle(markType);
        //加载公文文号校验
        initDocMarkValidate(markType);
        //加载公文文号事件
        initMarkFieldEvent(markType);
    }
}

/************** 公文文号加载 end *************/

/**
 * 初发化文号控件三段显示
 */
function initMarkFenduan(markType) {
    var defaultYear = new Date().getFullYear();
    var yearNoSelect = "<select id='yearNo_" + markType + "' style='width:68px;height:22px;margin:0px;margin-left:2px;vertical-align:middle;'>";
    yearNoSelect += "<option>" + defaultYear + "</option>";
    yearNoSelect += "</select>";

    var markNumberInput = "<input id='markNumber_" + markType + "' class='xdTextBox' style='width:48px;height:18px;margin-left:2px;vertical-align:middle;text-valign:middle;'/>";

    var suffix = "<span id='suffix_" + markType + "' style='width:10px;height:18px;vertical-align:middle;text-align:center;'>" + $.i18n("govdoc.number") + "</span>";

    getDocMarkFieldspan(markType).append(yearNoSelect);
    getDocMarkFieldspan(markType).append(markNumberInput);
    getDocMarkFieldspan(markType).append(suffix);
}

/**
 * 初始化文号手工输入控件
 * @param markType
 */
function initMarkShouxie(markType) {
    var selectType = selectTypeZidong;
    var selectTypeInput = '<input type="hidden" id="selectType_' + markType + '" value="' + selectType + '" />';
    //var selectType = '<input type="hidden" id="markNoType" value="0" />';
    var shouxieInput = '<input type="text" class="xdTextBox" id="shouxieInput_' + markType + '" style="display:none; height: 18px; font-family: 仿宋; font-size: 18.66px; margin-top: 0px; margin-right: 0px; margin-left: 0px;" />';
    getDocMarkFieldspan(markType).append(selectTypeInput);
    //getDocMarkFieldspan(markType).append(markNoType);
    getDocMarkFieldspan(markType).append(shouxieInput);
}

/**
 * 初始化文号断号/手工文号等小图标显示
 * @param markType
 */
function initMarkIcon(markType) {
    var markTypeValue = getMarkTypeValue(markType);
    var isShowCall = $("#isMarkShowCall_" + markTypeValue).val();
    var isHandInput = $("#isMarkHandInput_" + markTypeValue).val();
    if (isShowCall === "true") {
        getDocMarkFieldspan(markType).append("<span id='zidongIcon_" + markType + "' class='ico16'></span>");
    }
    if (isHandInput === "true") {
        getDocMarkFieldspan(markType).append("<span id='shouxieIcon_" + markType + "' class='ico16 number_change_16'></span>");
    }
}

/**
 * 加载分段文号控件显示样式(宽度/字体及大小)
 */
function initDocMarkStyle(markType) {
    if (openFrom == "extend") {

    } else {
        var fontSize = getDocMarkFieldtxt(markType).css("font-size");
        var fontFamily = getDocMarkFieldtxt(markType).css("font-family");
        var fontColor = getDocMarkFieldtxt(markType).css("color");
        var fontBold = getDocMarkFieldtxt(markType).css("font-weight");
        var fieldWidth = getDocMarkFieldtxt(markType).width();
        var fieldHeight = getDocMarkFieldtxt(markType).height();

        var yearNoWidth = 62;
        var markNumberWidth = 40;
        var suffixWidth = 10;
        var borderWidth = 5;
        var wordNoWidth = fieldWidth - yearNoWidth - markNumberWidth - suffixWidth - borderWidth;

        if (fieldWidth >= 260) {
            wordNoWidth = wordNoWidth - 20;
        } else if (fieldWidth >= 252) {
            wordNoWidth = wordNoWidth - 20;
        } else if (fieldWidth >= 250) {
            wordNoWidth = wordNoWidth - 5;
        } else if (fieldWidth >= 230) {
            wordNoWidth = wordNoWidth - 20;
        }
        if (wordNoWidth < 60) {
            wordNoWidth = fieldWidth;
        }
        getYearNoSelect(markType).css({"font-size": fontSize, "font-family": fontFamily, "color": fontColor, "font-weight": fontBold});
        getMarkNumberInput(markType).css({"font-size": fontSize, "font-family": fontFamily, "color": fontColor, "font-weight": fontBold});
        getSuffixHtml(markType).css({"font-size": fontSize, "font-family": fontFamily, "color": fontColor, "font-weight": fontBold});
        getShouxieInput(markType).css({"font-size": fontSize, "font-family": fontFamily, "color": fontColor, "font-weight": fontBold});

        getDocMarkFieldtxt(markType).attr("fieldWidth", fieldWidth);
        getDocMarkFieldtxt(markType).attr("wordNoWidth", wordNoWidth);
        getDocMarkFieldtxt(markType).css("border-width", "1px");
        getDocMarkFieldtxt(markType).css("border-style", "solid");
        getDocMarkFieldtxt(markType).css("border-color", "#e4e4e4");

        getDocMarkFieldtxt(markType).width(wordNoWidth);
        getShouxieInput(markType).width(fieldWidth);

        getDocMarkFieldtxt(markType).height(fieldHeight + 4);
        getYearNoSelect(markType).height(fieldHeight + 4);
        getMarkNumberInput(markType).height(fieldHeight + 1);
        getSuffixHtml(markType).height(fieldHeight + 1);
        getShouxieInput(markType).height(fieldHeight + 1);
    }
}

/**
 * 初始化文号按钮事件
 * @param markType
 */
function initMarkFieldEvent(markType) {
    //文号断号选择
    var zidongIcon = getZidongIcon(markType);
    if (zidongIcon) {
        zidongIcon.click(function () {
            openDocMarkDialog(markType);
        });
    }

    //手工文号切换
    var shouxieIcon = getShouxieIcon(markType);
    if (shouxieIcon) {
        shouxieIcon.click(function () {
            changeShouxieTypeEvent(markType);
            //设置文号必填项背景
            //changeDocMarkBackgroundColor(markType);
        });
    }

    //公文年号下拉切换
    getYearNoSelect(markType).change(function () {
        changeMarkNumberValueEvent(markType);
    });

    //文号序号输入
    getMarkNumberInput(markType).keyup(function () {
        this.value = this.value.replace(/[^\d]/g, '');
        changeMarkNumberValueEvent(markType);
        //设置文号必填项背景
        changeDocMarkBackgroundColor(markType);
    });

    //手工文号输入
    getShouxieInput(markType).keyup(function () {
        changeShouxieValueEvent(markType);
    });

    //手工文号输入
    getShouxieInput(markType).unbind("paste").bind("paste", function () {
        setTimeout(function () {
            changeShouxieValueEvent(markType);
        }, 100);
    });
}

/**
 * 回填公文文号/内部文号控件值
 * @param markType
 */
function fillDocMarkData(markType) {
    var markTypeValue = getMarkTypeValue(markType);
    var oldmarkstr = $("#old_markstr_" + markTypeValue).val();
    var oldtemplateMarkDefId = $("#old_templateMarkDefId_" + markTypeValue).val();
    var oldisTemplate = $("#old_isTemplate_" + markTypeValue).val();
    var oldisSysTemplate = $("#old_isSysTemplate_" + markTypeValue).val();
    var markstr = $("#markstr_" + markTypeValue).val();//用于切换公文单时，上一次的文号

    //文号有值
    if ((oldmarkstr != "" || markstr != "") || (oldtemplateMarkDefId != "" && oldisTemplate == "true")) {
        if (markstr != "") {//切换文单后，将最后一次的文号赋值为old
            $("#old_selectType_" + markTypeValue).val($("#selectType_" + markTypeValue).val());
            $("#old_markstr_" + markTypeValue).val($("#markstr_" + markTypeValue).val());
            $("#old_markDefId_" + markTypeValue).val($("#markDefId_" + markTypeValue).val());
            $("#old_categoryId_" + markTypeValue).val($("#categoryId_" + markTypeValue).val());
            $("#old_wordNo_" + markTypeValue).val($("#wordNo_" + markTypeValue).val());
            $("#old_yearEnabled_" + markTypeValue).val($("#yearEnabled_" + markTypeValue).val());
            $("#old_yearNo_" + markTypeValue).val($("#yearNo_" + markTypeValue).val());
            $("#old_markNumber_" + markTypeValue).val($("#markNumber_" + markTypeValue).val());
        }
        var oldmarkDefId = $("#old_markDefId_" + markTypeValue).val();
        var oldselectType = $("#old_selectType_" + markTypeValue).val();
        var oldcallId = $("#old_callId_" + markTypeValue).val();
        var oldmarkstr = $("#old_markstr_" + markTypeValue).val();
        var oldwordNo = $("#old_wordNo_" + markTypeValue).val();
        var oldyearEnabled = $("#old_yearEnabled_" + markTypeValue).val();
        var oldyearNo = $("#old_yearNo_" + markTypeValue).val();
        var oldmarkNumber = $("#old_markNumber_" + markTypeValue).val();
        var oldCurrentNo = $("#old_currentNo_" + markTypeValue).val();
        var oldLeft = $("#old_left_" + markTypeValue).val();
        var oldRight = $("#old_right_" + markTypeValue).val();
        var oldLength = $("#old_length_" + markTypeValue).val();
        var oldSuffix = $("#old_suffix_" + markTypeValue).val();

        if (oldselectType == 1) {//手工输入
            getShouxieInput(markType).val(oldmarkstr);
            getSelectType(markType).val(oldselectType);
            changeControlToShouxie(markType, actionInit);
        } else {
            var hasMarkRole = true;
            if (oldisTemplate == "true") {//模板
                hasMarkRole = setWordNoOptionSelectedByTemplate(markType, oldmarkDefId, oldwordNo, oldisSysTemplate);
                if (oldisSysTemplate == "true") {//系统模板
                    //用于回填公文文号
                    oldmarkstr = oldwordNo;
                    if (oldyearEnabled == "true") {
                        oldmarkstr += oldLeft + oldyearNo + oldRight;
                    }
                    oldmarkstr += getFullMarkNumber(oldCurrentNo, oldLength) + oldSuffix;
                }
            } else {//公文编辑
                hasMarkRole = setWordNoOptionSelectedByMarkDefId(markType, oldmarkDefId, actionInit, oldwordNo);
            }
            var selectedObj = getDocMarkField(markType).find("option:selected");
            if (!hasMarkRole) {//若当前人对文号没有使用权限，则手工添加一个，并赋上对应的属性及值
                selectedObj.attr("markDefId", oldmarkDefId);
                selectedObj.attr("wordNo", oldwordNo);
                selectedObj.attr("yearEnabled", oldyearEnabled);
                selectedObj.attr("currentNo", oldCurrentNo);
                selectedObj.attr("left", oldLeft);
                selectedObj.attr("right", oldRight);
                selectedObj.attr("markLength", oldLength);
                selectedObj.attr("suffix", oldSuffix);
                selectedObj.val(oldmarkstr);
            }
            if (oldmarkstr != "" && $("#markAction").val() == "deal") {
                /*if(oldmarkNumber == "")  {
                    oldmarkNumber = oldCurrentNo;
                }*/
            }
            selectedObj.attr("markstr", oldmarkstr);
            selectedObj.attr("callId", oldcallId);
            selectedObj.attr("oldmarkNumber", oldmarkNumber);
            selectedObj.attr("markNumber", oldmarkNumber);
            selectedObj.attr("yearNo", oldyearNo);

            //收文-收文编号-见办
            if (markGovdocType == "2" && markType == "serial_no" && $("#oldJianbanType").val() == "2") {
                setJianbanWordNoOptionSelectedByMarkDefId(markType, oldmarkDefId, actionInit, $.i18n("govdoc.see") + oldwordNo);

                selectedObj = getDocMarkField(markType).find("option:selected");
                selectedObj.attr("jianbanType", "2");
                selectedObj.attr("markDefId", oldmarkDefId);
                selectedObj.attr("wordNo", oldwordNo);
                selectedObj.attr("yearEnabled", oldyearEnabled);
                selectedObj.attr("currentNo", oldCurrentNo);
                selectedObj.attr("left", oldLeft);
                selectedObj.attr("right", oldRight);
                selectedObj.attr("markLength", oldLength);
                selectedObj.attr("suffix", oldSuffix);

                selectedObj.attr("markstr", oldmarkstr);
                selectedObj.attr("callId", oldcallId);
                selectedObj.attr("oldmarkNumber", oldmarkNumber);
                selectedObj.attr("markNumber", oldmarkNumber);
                selectedObj.attr("yearNo", oldyearNo);
            }

            getSelectType(markType).val(oldselectType);
            getDocMarkFieldtxt(markType).val(selectedObj.html());

            changeControlToZidong(markType, oldselectType, actionInit);
            changeWordNoEvent(markType, actionInit);
        }
    }
}

/**
 * 自动/手写类型切换
 */
function changeShouxieTypeEvent(markType, action) {
    if (getSelectType(markType).val() != selectTypeShouxie) {//当前文号显示的是非手写状态，需改成文号手写状态
        changeControlToShouxie(markType, action);
    } else {//当前文号显示的是手写状态，需改成文号自动状态
        changeControlToZidong(markType, selectTypeZidong, actionChangeToZidong);
    }
}

function changeControlToShouxie(markType, action) {
    getDocMarkFieldtxt(markType).hide();
    getDocMarkFieldspan(markType).find("input[name='acToggle']").hide();
    setTimeout(function () {
        getDocMarkFieldspan(markType).find("input[name='acToggle']").hide();
    }, 50);
    getYearNoSelect(markType).hide();
    getMarkNumberInput(markType).hide();
    getSuffixHtml(markType).hide();
    getShouxieInput(markType).show();
    getSelectType(markType).val(selectTypeShouxie);//从手工文号切换后，默认显示自动文号
    //if(action != actionInit) {//初始化回填数据时，不重置参数，因为回填方法里面已经设置过了
    resetDocMarkFieldValue(markType, actionChangeShouxie, null);
    //}
}

function changeControlToZidong(markType, selectType, action) {
    getDocMarkFieldtxt(markType).show();
    getDocMarkFieldspan(markType).find("input[name='acToggle']").show();
    getYearNoSelect(markType).show();
    getMarkNumberInput(markType).show();
    getSuffixHtml(markType).show();
    getShouxieInput(markType).hide();

    getSelectType(markType).val(selectType);//从手工文号切换后，默认显示自动文号

    if (action == actionChangeToZidong) {//从手工文号切换到自动文号
        var lastMarkDefId = getShouxieInput(markType).attr("lastMarkDefId");
        if (lastMarkDefId && lastMarkDefId != "") {
            setWordNoOptionSelectedByMarkDefId(markType, lastMarkDefId);
        } else {
            getDocMarkField(markType).find("option").eq(0).attr("selected", true);
        }
    }
    var selectedObj = getDocMarkField(markType).find("option:selected");
    getDocMarkFieldtxt(markType).val(selectedObj.html());

    if (selectedObj.attr("yearEnabled") == "false") {
        getYearNoSelect(markType).hide();
    }
    if (action != actionChangeDuanhao) {
        resetDocMarkFieldValue(markType, action, null);
    }
}

/**
 * 手写文号输入内容变更事件
 */
function changeShouxieValueEvent(markType) {
    getSelectType(markType).val(selectTypeShouxie);
    resetDocMarkFieldValue(markType, selectTypeShouxie, null);
    //设置文号必填项背景
    changeDocMarkBackgroundColor(markType);
}

/**
 * 机构字号切换事件
 * @param markType
 * @param action
 * @param markstr
 */
function changeWordNoEvent(markType, action) {
    //获取文号的相关数据
    var selectedObj = getDocMarkField(markType).find("option:selected");
    if (!selectedObj) {
        return;
    }
    selectedObj.attr("selected", true);
    selectedObj.attr("selectType", selectTypeZidong);
    if (selectedObj.val() == "") {
        selectedObj.attr("markNumber", "");
    }

    //机构字切换后，重置年号控件
    setYearNoOption(markType, selectedObj, action);
    //机构字切换后，获取公文编号
    setMarkNumberInput(markType, selectedObj, action);
    //机构字切换后，重置文号后缀
    setSuffixHtml(markType, selectedObj);
    //机构字切换、年号切换、编号改变后，重置公文文号映射字段doc_mark
    changeMarkNumberValueEvent(markType, selectedObj, action);
}

/**
 * 获取机构字控件select及input中值内容，拼装新的文号
 * @param markType
 * @param docMark
 */
function changeMarkNumberValueEvent(markType, selectedObj, action) {
    //获取文号的相关数据
    if (!selectedObj) {
        selectedObj = getDocMarkField(markType).find("option:selected");
    }
    var selectedDefId = selectedObj.val();
    if (selectedDefId == "") {//选择为空

    } else {
        var wordNo = selectedObj.html();
        var left = selectedObj.attr("left");
        var right = selectedObj.attr("right");
        var suffix = selectedObj.attr("suffix");
        var yearEnabled = selectedObj.attr("yearEnabled");
        var markLength = selectedObj.attr("markLength");
        var currentNo = selectedObj.attr("currentNo");
        var yearNo = getYearNoSelect(markType).val();
        var markFullNumber = getFullMarkNumber(getMarkNumberInput(markType).val(), markLength);

        var markstr = wordNo + left + yearNo + right + markFullNumber + suffix;
        if (yearEnabled == "false") {
            markstr = wordNo + markFullNumber + suffix;
        }
        selectedObj.attr("markstr", markstr);
        selectedObj.attr("yearNo", yearNo);
        selectedObj.attr("markNumber", markFullNumber);
        selectedObj.val(markstr);
    }
    resetDocMarkFieldValue(markType, action, selectedObj);
}

/**
 * 将拼装好的文号值放入隐藏的文号选择框
 * @param markType
 * @param markstr
 * @param fieldValue
 * @param selectedDefId
 */
function resetDocMarkFieldValue(markType, action, selectedObj) {
    if (!selectedObj) {
        selectedObj = getDocMarkField(markType).find("option:selected");
    }

    getDocMarkField(markType).find("option").each(function () {
        if (getSelectType(markType).val() == selectTypeShouxie) {
            $(this).attr("selected", false);
        }
        if ($(this).attr("isAdd") == "true") {
            $(this).remove();
        }
    });

    //手写切换图标点击事件
    if (getSelectType(markType).val() == selectTypeShouxie) {
        if (selectedObj.attr("markDefId")) {
            getShouxieInput(markType).attr("lastMarkDefId", selectedObj.attr("markDefId"));
        }

        var markstr = getShouxieInput(markType).val();
        if (markstr != "") {
            markstr = $.trim(markstr);//产品经理要求：手工文号需要清空前后空格
        }
        var shouxieValue = markstr;
        var shouxieOption = "<option value='" + shouxieValue + "' markstr='" + markstr + "' selectType='" + selectTypeShouxie + "' isAdd='true' style='display:none'>" + markstr + "</option>";
        getDocMarkField(markType).append(shouxieOption);
        getDocMarkField(markType).find("option").last().attr("selected", true);

        selectedObj = getDocMarkField(markType).find("option:selected");
    }

    getDocMarkFieldtxt(markType).attr("data", select2DataStr(getDocMarkField(markType)));

    resetDocMarkSubmitData(markType, selectedObj, action);
}

function resetDocMarkSubmitData(markType, selectedObj, action) {
    var markstr = "";
    var wordNo = "";
    var categoryId = "";
    var markDefId = "";
    var callId = "";
    var selectType = "";
    var yearNo = "";
    var markNumber = "";
    var yearEnabled = "";

    if (selectedObj) {
        selectType = getSelectType(markType).val();
        categoryId = selectedObj.attr("categoryId");
        markDefId = selectedObj.attr("markDefId");
        callId = selectedObj.attr("callId");
        markstr = selectedObj.attr("markstr");
        wordNo = selectedObj.html();
        markNumber = selectedObj.attr("markNumber");
        yearNo = selectedObj.attr("yearNo");
        yearEnabled = selectedObj.attr("yearEnabled");
        if (wordNo == $.i18n("govdoc.institutional.pronouns")) {
            wordNo = "";
        }
        if (selectedObj.attr("jianbanType") == "2") {
            $("#jianbanType").val("2");
        } else {
            $("#jianbanType").val("1");
        }
    }
    if ($("#jianbanType").val() == "") {
        $("#jianbanType").val("1");
    }

    var markTypeValue = getMarkTypeValue(markType);
    var hasMark = $("#hasMark_" + markTypeValue).val();
    if (action && (action != actionInit && action != actionFengsong) && hasMark != "true") {
        hasMark = "true";
        $("#hasMark_" + markTypeValue).val("true");
    }

    //分送时，若文号没有做改动，则将old文号内容赋值给文号当前内容
    if (hasMark != "true" && action == actionFengsong) {
        hasMark = "true";
        $("#hasMark_" + markTypeValue).val("true");
        if (!selectedObj) {
            selectType = $("#old_selectType_" + markTypeValue).val();
            categoryId = $("#old_categoryId_" + markTypeValue).val();
            markDefId = $("#old_markDefId_" + markTypeValue).val();
            callId = $("#old_callId_" + markTypeValue).val();
            markstr = $("#old_markstr_" + markTypeValue).val();
            wordNo = $("#old_wordNo_" + markTypeValue).val();
            markNumber = $("#old_markNumber_" + markTypeValue).val();
            yearNo = $("#old_yearNo_" + markTypeValue).val();
            yearEnabled = $("#old_yearEnabled_" + markTypeValue).val();
        }
    }
    if (action != actionInit) {
        $("#hasMark_" + markTypeValue).val("true");
    } else if (action == actionInit && $("#markAction").val() == "deal" && markstr != "") {
        $("#hasMark_" + markTypeValue).val("true");
    } else if (hasMark != "true" && $("#markAction").val() == "new") {//新建界面
        $("#hasMark_" + markTypeValue).val("true");
    }

    $("#markstr_" + markTypeValue).val(markstr);
    $("#wordNo_" + markTypeValue).val(wordNo);
    $("#categoryId_" + markTypeValue).val(categoryId);
    $("#markDefId_" + markTypeValue).val(markDefId);
    $("#callId_" + markTypeValue).val(callId);
    $("#selectType_" + markTypeValue).val(selectType);
    $("#yearNo_" + markTypeValue).val(yearNo);
    $("#yearEnabled_" + markTypeValue).val(yearEnabled);
    $("#markNumber_" + markTypeValue).val(markNumber);
}


/**
 * 提交前重置公文文号参数
 */
function resetMarkParamBeforeSubmit(markType) {

}

function getMarkTypeValue(markType) {
    if (markType == "doc_mark") {
        return "0";
    } else if (markType == "serial_no") {
        return "1";
    } else if (markType == "sign_mark") {
        return "2";
    }
}

function getMarkNameByType(markType) {
    if (markType == "doc_mark") {
        return docMarkName;
    } else if (markType == "serial_no") {
        return serialNoName;
    } else if (markType == "sign_mark") {
        return signMarkName;
    }
}

/**
 * 设置年号下拉列值(changeWordNoEvent调用)
 * @param markType
 * @param twoYear
 * @param selectedYearNo
 */
function setYearNoOption(markType, selectedObj, action) {
    var yearEnabled = selectedObj.attr("yearEnabled");
    //机构字切换后，重置年号控件
    if (yearEnabled == "false") {
        getYearNoSelect(markType).hide();
    } else {
        getYearNoSelect(markType).html("");
        getYearNoSelect(markType).show();

        var twoYear = selectedObj.attr("twoYear");
        var yearNo = selectedObj.attr("yearNo");
        //获取当前年
        var thisYear = new Date().getFullYear();
        //开启可跨前后两年
        var distance = 0;
        if (twoYear == "true") {
            distance = 1;
        }
        for (var i = 0 - distance; i <= distance; i++) {
            getYearNoSelect(markType).append("<option value='" + (thisYear + i) + "'>" + (thisYear + i) + "</option>");
        }

        if (yearNo) {
            var hasOldyearNo = false;
            getYearNoSelect(markType).find("option").each(function () {
                if ($(this).val() == yearNo) {
                    hasOldyearNo = true;
                }
            });
            if (hasOldyearNo == true) {
                getYearNoSelect(markType).val(yearNo);
            } else {//若上一年的文号在文号下拉框中没有，则手工加上
                getYearNoSelect(markType).append("<option selected='true'>" + yearNo + "</option>");
            }
        } else {
            getYearNoSelect(markType).val(thisYear);
        }
    }
}

/**
 * 设置公文文号流水号(changeWordNoEvent调用)
 * @param markType
 * @param twoYear
 * @param selectedYearNo
 */
function setMarkNumberInput(markType, selectedObj, action) {
    var markLength = selectedObj.attr("markLength");
    var currentNo = selectedObj.attr("currentNo");//文号当前序号
    var markNumber = selectedObj.attr("markNumber");//当前使用的序号-数据发出后，用于保存到数据库中，最后真正使用的
    var oldmarkNumber = selectedObj.attr("oldmarkNumber");//编辑界面文号序号-用于回填

    var realNumber;
    if (action == actionChangeDuanhao || action == actionChangeWordNo) {//选用断号/预留号后选择的文号
        realNumber = markNumber;
    } else if (action == actionInit || action == actionChangeShouxie) {
        realNumber = oldmarkNumber;
    }
    if (action != actionInit) {//因为初始化流水号回填时，有可能没有填写流水号
        if (isUnDefined(realNumber)) {
            //realNumber = currentNo;
        }
    }
    var markFullNumber = "";
    if (realNumber != "") {
        markFullNumber = getFullMarkNumber(realNumber, markLength);
    }
    getMarkNumberInput(markType).val(markFullNumber);
}

function isUnDefined(value) {
    if (value && value != null && value != "" && value != "null") {
        return false;
    }
    return true;
}

/**
 * 设置公文文号后缀(changeWordNoEvent调用)
 * @param markType
 * @param twoYear
 * @param selectedYearNo
 */
function setSuffixHtml(markType, selectedObj) {
    var suffix = selectedObj.attr("suffix");
    if (!suffix) {
        suffix = $.i18n("govdoc.number");
    }
    getSuffixHtml(markType).html(suffix);
}

/**
 * 打开断号选择框
 */
function openDocMarkDialog(markType) {
    var url = getDocMarkChooseUrl(markType);
    var title = $.i18n("govdoc.number.select.document");
    if (markType == "sign_mark") {
        title = $.i18n("govdoc.number.select.signature");
    } else if (markType == "serial_no") {
        title = $.i18n("govdoc.number.select.internal");
        if (markGovdocType == "2" || markGovdocType == "4") {
            title = $.i18n("govdoc.number.select.receive");
        }
    }
    var markTypeValue = getMarkTypeValue(markType);
    var oldtemplateMarkDefId = $("#old_templateMarkDefId_" + markTypeValue).val();
    var oldisSysTemplate = $("#old_isSysTemplate_" + markTypeValue).val();
    if (oldtemplateMarkDefId && oldtemplateMarkDefId != "") {
        url += "&templateMarkDefId=" + oldtemplateMarkDefId;
    }
    $.fn.openMarkDialog(url, title, 450, 420, {}, function (objs) {//objs
        if (!objs) {
            return;
        }
        var i = 0;
        var selectType = objs[i++];
        var markDefId = objs[i++];
        var callId = objs[i++];
        var markstr = objs[i++];
        var wordNo = objs[i++];
        var yearNo = objs[i++];
        var markNumber = objs[i++];
        var length = objs[i++];

        getSelectType(markType).val(selectType);
        getShouxieInput(markType).attr("lastMarkDefId", markDefId);
        changeControlToZidong(markType, selectType, actionChangeToZidong);

        var selectedObj = getDocMarkField(markType).find("option:selected");
        selectedObj.attr("markDefId", markDefId);
        selectedObj.attr("callId", callId);
        selectedObj.attr("markstr", markstr);
        selectedObj.attr("wordNo", wordNo);
        selectedObj.attr("yearNo", yearNo);
        selectedObj.attr("markNumber", markNumber);

        changeWordNoEvent(markType, actionChangeDuanhao);
    });
}

/************** 公共方法加载 end *************/


/************** 文号样式加载 start ***********/

/**
 * 文号必填时设置控制背景颜色
 * @param markType
 */
function changeDocMarkBackgroundColor(markType) {
    //文号分三段显示
    /*if(docMarkFenduanKaiguan || serialNoFenduanKaiguan) {
        if(getMarkNoType(markType).val() != "2") {//自动文号
            if(getDocMarkFieldtxt(markType).attr("changeBackgroundColor") == "true") {
                if(isMarkNull(getDocMarkFieldtxt(markType).val())) {
                    getDocMarkFieldtxt(markType).css("background-color", nullColor);
                } else {
                    getDocMarkFieldtxt(markType).css("background-color", notNullColor);
                }
            }
            if(getMarkNumberInput(markType).attr("changeBackgroundColor") == "true") {
                if(isMarkNull(getMarkNumberInput(markType).val())) {
                    getMarkNumberInput(markType).css("background-color", nullColor);
                } else {
                    getMarkNumberInput(markType).css("background-color", notNullColor);
                }
            }
        } else {//手写文号
            if(getShouxieInput(markType).attr("changeBackgroundColor") == "true") {
                if(isMarkNull(getShouxieInput(markType).val())) {
                    getShouxieInput(markType).css("background-color", nullColor);
                } else {
                    getShouxieInput(markType).css("background-color", notNullColor);
                }
            }
        }
    } else {
        if(getDocMarkFieldtxt(markType).attr("changeBackgroundColor") == "true") {
            if(isMarkNull(getDocMarkFieldtxt(markType).val())) {
                getDocMarkFieldtxt(markType).css("background-color", nullColor);
            } else {
                getDocMarkFieldtxt(markType).css("background-color", notNullColor);
            }
        }
    }*/
}


/************** 对象获取方法 start ***********/
function getDocMarkFieldspan(markType) {
    try {
        var fieldspan;
        if (markType == "serial_no") {
            fieldspan = serialNoFieldspan;
        } else if (markType == "doc_mark") {
            fieldspan = docMarkFieldspan;
        } else {
            fieldspan = signMarkFieldspan;
        }
        return fieldspan;
    } catch (e) {
        return getRealMarkField(markType, "fieldspan");
    }
}

function getDocMarkField(markType) {
    try {
        var field;
        if (markType == "serial_no") {
            field = serialNoField;
        } else if (markType == "doc_mark") {
            field = docMarkField;
        } else {
            field = signMarkField;
        }

        if (field && field.size() > 0 && field[0].tagName) {
        }
        return field;
    } catch (e) {
        return getRealMarkField(markType, "field");
    }
}

function getRealMarkField(markType, type) {
    var markName = getMarkNameByType(markType);
    var field;
    var fieldtxt;
    var fieldspan = formContainer.find("span[fieldval*='" + markName + "']");
    if (!fieldspan) {
        fieldspan = formContainer.find("#" + fieldId + "_span");
    }

    if (!fieldspan || fieldspan.size() == 0) {
        field = formContainer.find("[mappingField=" + markType + "]");
        if (field && field.size() > 0) {
            fieldspan = field.parent("span");
        }
    }
    if (type == "fieldspan") {
        return fieldspan;
    } else if (type == "fieldtxt") {
        var fieldtxt;
        var fieldId = field.attr("id");
        if (!fieldId || fieldId == "") {
            return;
        }

        if (!fieldtxt) {
            fieldtxt = fieldspan.find("#" + fieldId + "_txt");
        }
        return fieldtxt;
    } else {
        return field;
    }
}

function getDocMarkFieldtxt(markType) {
    try {
        var fieldtxt;
        if (markType == "serial_no") {
            fieldtxt = serialNoFieldtxt;
        } else if (markType == "doc_mark") {
            fieldtxt = docMarkFieldtxt;
        } else {
            fieldtxt = signMarkFieldtxt;
        }

        if (fieldtxt && fieldtxt.size() > 0 && fieldtxt[0].tagName) {
        }
        return fieldtxt;
    } catch (e) {
        return getRealMarkField(markType, "fieldtxt");
    }
}

function getYearNoSelect(markType) {
    return getDocMarkFieldspan(markType).find("#yearNo_" + markType);
}

function getMarkNumberInput(markType) {
    return getDocMarkFieldspan(markType).find("#markNumber_" + markType);
}

function getSuffixHtml(markType) {
    return getDocMarkFieldspan(markType).find("#suffix_" + markType);
}

function getShouxieInput(markType) {
    return getDocMarkFieldspan(markType).find("#shouxieInput_" + markType);
}

function getZidongDiv(markType) {

}

function getShouxieDiv(markType) {

}

function getMarkNoType(markType) {
    return getDocMarkFieldspan(markType).find("#markNoType_" + markType);
}

function getSelectType(markType) {
    return getDocMarkFieldspan(markType).find("#selectType_" + markType);
}

function getZidongIcon(markType) {
    return getDocMarkFieldspan(markType).find("#zidongIcon_" + markType);
}

function getShouxieIcon(markType) {
    return getDocMarkFieldspan(markType).find("#shouxieIcon_" + markType);
}

function getDocMarkChooseUrl(markType) {
    if (markType == "serial_no") {
        return serialNoChooseUrl + "&selDocmark=my:" + markType;
    } else if (markType == "doc_mark") {
        return docMarkChooseUrl + "&selDocmark=my:" + markType;
    } else {
        return signMarkChooseUrl + "&selDocmark=my:" + markType;
    }
}

function getDocMarkValidate(markType) {
    if (markType == "serial_no") {
        return 'name:' + $.i18n("govdoc.Internal.document.number") + ',fieldType:"VARCHAR",errorMsg:' + $.i18n("govdoc.number.select.internal.notEmpty") + ',errorAlert:true,notNull:true,checkNull:true,errorIcon:false,func:validateBaseMark';
    } else {
        return 'name:' + $.i18n("govdoc.document.number") + ',fieldType:"VARCHAR",errorMsg:' + $.i18n("govdoc.number.select.document.notEmpty") + ',errorAlert:true,notNull:true,checkNull:true,errorIcon:false,func:validateBaseMark';
    }
}

/************** 对象获取方法 end *************/


/************** 工具方法加载 start ***********/
/**
 * 按文号长度填充文号，如文号"1"长度为4，则返回"0001"
 * @param markFullNumber
 * @param markLength
 * @returns {String}
 */
function getFullMarkNumber(markFullNumber, markLength) {
    if (-1 == markFullNumber) {
        return "";
    }
    var str = "";
    if (markFullNumber) {
        if (markFullNumber.length < markLength) {
            for (var i = 0; i < markLength - markFullNumber.length; i++) {
                str += "0";
            }
        }
        str += markFullNumber;
    }
    return str;
}

/**
 * 通过文号格式，拆分文号，获取年份
 * @param markstr
 * @param left
 * @param right
 * @param suffix
 * @returns
 */
function getMarkYearNo(markstr, left, right, suffix) {
    if (markstr) {
        var rightIndex = markstr.lastIndexOf(right);
        if (rightIndex != -1) {
            var rightstr = markstr.substring(0, rightIndex);
            var leftIndex = rightstr.lastIndexOf(left);
            var yearNo = rightstr.substring(leftIndex + 1, rightIndex);
            return yearNo;
        }
    }
    return yearNo;
}

function setWordNoOptionSelectedByHtml(markType, txtVal) {
    var selectObj = getDocMarkField(markType);
    selectObj.find("option").each(function () {
        $(this).attr("selected", false);
    });
    selectObj.find("option").each(function () {
        if ($(this).html() == txtVal) {
            $(this).attr("selected", true);
        }
    });
}

function setWordNoOptionSelectedByTemplate(markType, markDefId, wordNo, isSysTemplate) {
    var hasMarkRole = false;
    var selectObj = getDocMarkField(markType);
    if (isSysTemplate == "true") {
        selectObj.find("option").each(function () {
            if ($(this).attr("markDefId") == markDefId) {
                $(this).attr("selected", true);
                hasMarkRole = true;
            } else {
                $(this).remove();
            }
        });
        if (!hasMarkRole) {
            selectObj.html("");
            var templateOption = "<option>" + wordNo + "</option>";
            getDocMarkField(markType).append(templateOption);
            getDocMarkField(markType).find("option").last().attr("selected", true);
        }
    } else {
        selectObj.find("option").each(function () {
            if ($(this).attr("markDefId") == markDefId) {
                $(this).attr("selected", true);
                hasMarkRole = true;
            }
        });
    }
    return hasMarkRole;
}

function setWordNoOptionSelectedByMarkDefId(markType, markDefId, action, wordNo) {
    var selectObj = getDocMarkField(markType);
    selectObj.find("option").each(function () {
        if ($(this).attr("isAdd") == "true") {
            $(this).remove();
        } else {
            $(this).attr("selected", false);
        }
    });

    var hasMarkRole = false;
    selectObj.find("option").each(function () {
        if ($(this).attr("markDefId") == markDefId) {
            $(this).attr("selected", true);
            hasMarkRole = true;
            return;
        }
    });

    //公文编辑或处理编辑时，文号授权被取消，手动回填文号
    if (action == actionInit && !hasMarkRole) {
        var editOption = "<option>" + wordNo + "</option>";
        selectObj.append(editOption);
        selectObj.find("option").last().attr("selected", true);
    }

    return hasMarkRole;
}

function setJianbanWordNoOptionSelectedByMarkDefId(markType, markDefId, action, wordNo) {
    var selectObj = getDocMarkField(markType);
    selectObj.find("option").each(function () {
        if ($(this).attr("isAdd") == "true") {
            $(this).remove();
        } else {
            $(this).attr("selected", false);
        }
    });

    //公文编辑或处理编辑时，文号授权被取消，手动回填文号
    var editOption = "<option>" + wordNo + "</option>";
    selectObj.append(editOption);
    selectObj.find("option").last().attr("selected", true);
}

function reloadGovdocForm() {
    isLoadDocMark = false;
    isLoadSerialNo = false;
    isLoadSignMark = false;
    isLoadForm = false;
}

/************** 工具方法加载 end *************/

/**
 *
 * @param markType
 */
function initDocMarkValidate(markType) {

}

function validateBaseMark(obj, param) {
    var markTypestr = "";
    if (obj.attr("mappingfield") == "doc_mark") {
        markTypestr = "doc_mark";
    } else if (obj.attr("mappingfield") == "serial_no") {
        markTypestr = "serial_no";
    } else if (obj.attr("mappingfield") == "sign_mark") {
        markTypestr = "sign_mark";
    }
    if (markTypestr && markTypestr != "") {
        var validate = getDocMarkField(markTypestr).attr("validate");
        if (validate && validate != "") {
            var validateObj = $.parseJSON("{" + validate + "}");
            if (param.checkNull && validateObj && validateObj.markNotNull) {
                var selectedOption = getDocMarkField(markTypestr).find("option:selected");
                if (selectedOption.val() == "") {
                    return false;
                }
            }
        }
    }
    return true;
}

function checkFormMark(action) {
    //isOnlyCheckFinish参数：发文公文文号才需要验证流程是否结束，需在做了预提交后单独验证
    if (docMarkFieldspan && docMarkFieldspan.size() > 0) {
        if (markGovdocType != "4") {//交换流程，不验证公文文号
            //zhou 注释
            // if (!checkGovdocMark("doc_mark", action)) {
            //     return false;
            // }
        }
    }
    if (serialNoFieldspan && serialNoFieldspan.size() > 0) {
        //zhou注释
        // if (!checkGovdocMark("serial_no", action)) {
        //     return false;
        // }
    }
    if (signMarkFieldspan && signMarkFieldspan.size() > 0) {
        if (markGovdocType != "1" && markGovdocType != "3") {//发文/签报，不验证签收编号
            //zhou注释
            // if (!checkGovdocMark("sign_mark", action)) {
            //     return false;
            // }
        }
    }
    return true;
}

function checkGovdocMark(markType, action) {
    // 知会节点不需要校验文号重复
    var isZhihui = $("#policy").val() == "zhihui";
    if (isZhihui) {
        return true;
    }
    var isFensong = $("#isQuickSend").val() == "true" || $("#policy").val() == "faxing" || $("#fenfadanwei").is(":visible");

    var callFlag = true;
    var usedFlag = true;
    var markTypeValue = getMarkTypeValue(markType);
    var isFawen = markGovdocType == "1" ? "1" : "2";

    var markUsedType = $("#markUsedType_" + markTypeValue + "_" + isFawen).val();//模式1/模式2
    var isMarkCheckCall = $("#isMarkCheckCall_" + markTypeValue + "_" + isFawen).val() == "true";//启用文号使用提醒
    var isMarkFinish = $("#isMarkFinish_" + markTypeValue + "_" + isFawen).val() == "true";//启用文号使用提醒
    var workflow_last_input = "false";
    if ($("#markAction").val() == "deal") {
        workflow_last_input = document.getElementById("workflow_last_input").value;
        if ($("#xuban").size() > 0 && $("#xuban").is(':visible') && ($("#customDealWith").attr("checked") == "checked" || $("#customDealWith").attr("checked") == "true")) {
            document.getElementById("workflow_last_input").value = "false";
            workflow_last_input = "false";
        }
    }
    //模式1：发文开启流程结束占号，流程即将结束时，文号不能为空
    var isLastNode = markUsedType == "1" && isMarkFinish && (workflow_last_input == "true");

    var markstr = getMarkstr(markType, true, false, false);

    if (markGovdocType == "1" && markType == "doc_mark" && isFensong) {//发文分送时校验公文文号
        //分送前做文号非空校验
        ////西咸新区去掉分送文号非空校验
        /*if(!checkMarkstr(markType, markstr, true)) {
            return false;
        }*/
        usedFlag = checkGovdocMarkIsUsed(markType, action, markUsedType, isLastNode);//分送-占号判重
    } else {
        //非分送情况下，不做文号非空校验
        if (!checkMarkstr(markType, markstr, false)) {
            return true;
        }

        if (action == "draft") {//模式1-断号判重(保存待发)
            var needCheck = true;
            //收文及交换不验证来文文号的占号判重
            if ((markGovdocType == "2" || markGovdocType == "4") && markType == "doc_mark") {
                needCheck = false;
            }
            if (needCheck) {
                //保存待发-占号判重(为解决先占号，再保存待发若做占号判重，则又会生成断号)
                usedFlag = checkGovdocMarkIsUsed(markType, action, markUsedType, isLastNode);
            }
        } else if (action == "send") {//发起
            usedFlag = checkGovdocMarkIsUsed(markType, action, markUsedType, isLastNode);//发起-占号判重
        } else if (action == "zcdb") {//暂存待办
            if (markUsedType == "2") {//模式2-占号判重(暂存待办)
                usedFlag = checkGovdocMarkIsUsed(markType, action, markUsedType, isLastNode);//暂存待办-占号判重
            }
        } else if (action == "deal") {//提交
            usedFlag = checkGovdocMarkIsUsed(markType, action, markUsedType, isLastNode);//提交-占号判重
        }
    }

    if (!usedFlag) {//不通过，返回false
        return false;
    }

    //不占用-则再次判断断号
    if (!isMarkCheckCall) {
        if ((markGovdocType == "2" || markGovdocType == "4") && markType == "doc_mark") {
            isMarkCheckCall = true;
        }
    }
    if (isMarkCheckCall) {
        callFlag = checkGovdocMarkIsCalled(markType);
    }

    if (!callFlag) {
        var markTypeTitle = getMarkTypeTitle(markType, markGovdocType);
        if (!confirm(markTypeTitle + $.i18n("govdoc.used.already"))) {
            return false;
        }
    }

    var isFensong = $("#policy").val() == "faxing" || $("#fenfadanwei").is(":visible");
    if (markGovdocType == "1" && isFensong) {//分送
        var markTypeValue = getMarkTypeValue(markType);
        var hasMark = $("#hasMark_" + markTypeValue).val();
        if (hasMark == "false") {
            resetDocMarkSubmitData(markType, null, actionFengsong);
        }
    }

    return true;
}

/***
 * 验证文号是否
 * @param markstr
 * @param markType
 * @param callback
 */
function checkGovdocMarkIsCalled(markType, callback) {
    var docMark = getDocMarkField(markType);
    if (!docMark || !docMark.attr("id")) {
        return true;
    }

    //获取文号
    var markstr = getMarkstr(markType, true, true, false);
    //空文号不需要验证断号
    if (!checkMarkstr(markType, markstr, false)) {
        return true;
    }

    var summaryId = $("#summaryId").val() == "" ? $("#summary_id").val() : $("#summaryId").val();
    var orgAccountId = $("#orgAccountId").val();
    var jianbanType = $("#jianbanType").val();
    var markTypeValue = getMarkTypeValue(markType);
    var newflowType = $("#newflowType_" + markTypeValue).val();//0主流程/1子流程/2自动触发
    if (newflowType == "") {//老数据没有生成文号记录
        newflowType = $("#newflowType").val();
    }
    if (newflowType == "1") {//子流程
        //若子流程找得到父流程，则用父流程文号去判重
        if ($("#parentSummaryId_" + markTypeValue).val() && $("#parentSummaryId_" + markTypeValue).val() != "") {
            summaryId = $("#parentSummaryId_" + markTypeValue).val();
        } else {//若子流程找不到父流程，则不验证文号判重
            return true;
        }
    } else if (newflowType == "2") {//触发流程
        return true;
    }

    //签收流程、收文流程中的公文文号，在不可编辑状态下，不提示文号重复。编辑状态下，依然判重并提示
    if (markGovdocType == "2" || markGovdocType == "4") {
        if (markType == "doc_mark") {
            var isEdit = markInputIsEdit(markType);
            if (isEdit == false) {
                return true;
            }
        }
    }

    var manager = new govdocMarkManager();
    var isCalled = manager.checkMarkIsCalled(summaryId, orgAccountId, markGovdocType, "", jianbanType, getMarkTypeValue(markType), markstr);
    if (isCalled) {
        return false;
    }

    return true;
}

/**
 * 检查公文文号重复
 * @param markstr
 * @param markType
 * @param callback
 * @returns {Boolean}
 */
function checkGovdocMarkIsUsed(markType, action, markUsedType, isLastNode) {
    if (markGovdocType == "2" || markGovdocType == "4") {//收文/签收
        if (markType == "doc_mark") {//收文/签报不校验公文文号占用情况
            return true;
        }
        if ($("#jianbanType").val() == "2") {
            return true;
        }
    }

    if (markGovdocType == "1" || markGovdocType == "3") {//发文/签报
        if (markType == "sign_mark") {//发文/签报不校验签收编号占用情况
            return true;
        }
    }

    var docMark = getDocMarkField(markType);
    if (!docMark || !docMark.attr("id")) {
        return true;
    }

    var isFensong = $("#isQuickSend").val() == "true" || $("#policy").val() == "faxing" || $("#fenfadanwei").is(":visible");
    var markstr = getMarkstr(markType, true, true, true);
    //交换或模式2才需要校验文号是否为空(也就是需要占号时，才验证空)
    if (isFensong && markType == 'doc_mark') {
        //西咸新区去掉分送文号非空校验
        /*if(!checkMarkstr(markType, markstr, true)) {
            return false;
        }*/
    } else {//markUsedType=="2"或其它
        //空文号不需要验证
        if (!checkMarkstr(markType, markstr, false)) {
            return true;
        }
    }
    var summaryId = $("#summaryId").val() == "" ? $("#summary_id").val() : $("#summaryId").val();
    var orgAccountId = $("#orgAccountId").val();
    var jianbanType = $("#jianbanType").val();
    var markTypeValue = getMarkTypeValue(markType);
    var newflowType = $("#newflowType_" + markTypeValue).val();//0主流程/1子流程/2自动触发
    if (newflowType == "") {//老数据没有生成文号记录
        newflowType = $("#newflowType").val();
    }
    if (newflowType == "1") {//子流程
        //若子流程找得到父流程，则用父流程文号去判重
        if ($("#parentSummaryId_" + markTypeValue).val() && $("#parentSummaryId_" + markTypeValue).val() != "") {
            summaryId = $("#parentSummaryId_" + markTypeValue).val();
        } else {//若子流程找不到父流程，则不验证文号判重
            return true;
        }
    } else if (newflowType == "2") {//触发流程
        return true;
    }

    var manager = new govdocMarkManager();
    var isUsed = manager.checkMarkIsUsed(summaryId, orgAccountId, markGovdocType, "", jianbanType, getMarkTypeValue(markType), markstr);
    if (isUsed) {
        var markTypeTitle = getMarkTypeTitle(markType, markGovdocType);
        alert(markTypeTitle + $.i18n("govdoc.repeat.reSelect"));
        return false;
    }

    return true;
}

function setOptionSelected(field) {
    var selectedObj;
    field.find("option").each(function () {
        if ($(this).attr("selected") == true || $(this).attr("selected") == "true" || $(this).attr("selected") == "selected") {
            field.attr("selected", true);
            selectedObj = $(this);
        }
    });
    return selectedObj;
}

function getMarkstr(markType, markNumberIs0, markNumberIsNull, markIsNull) {
    var markstr = "";
    var field = getDocMarkField(markType);
    if (field && field.size() > 0) {
        var fieldObj = field[0];
        if (fieldObj.tagName == 'select' || fieldObj.tagName == 'SELECT') {//可编辑
            var selectedObj = field.find("option:selected");
            var selectType = selectedObj.attr("selectType");
            markstr = selectedObj.attr("markstr");
            if (selectType != 1) {//非手工文号
                if (markIsNull) {
                    if (selectedObj.val() == "") {
                        return "wordNoIsNull";
                    }
                }
                var markNumber = getMarkNumberInput(markType).val();
                try {
                    if (markNumberIsNull) {//是否校验文号为空
                        if (markNumber == "") {
                            return "markNumberIsNull";
                        }
                    }
                    if (markNumberIs0) {//是否校验文号流水号为0
                        if (markNumber != "" && Number(markNumber) == 0) {
                            return "markNumberIs0";
                        }
                    }
                } catch (e) {
                }
            } else {
                if (markstr != null && markstr != "") {
                    markstr = $.trim(markstr);//产品经理要求：手工文号需要清空前后空格
                }
            }
        } else if (fieldObj.tagName == 'span' || fieldObj.tagName == 'SPAN') {//只读
            markstr = field.html();
            if (markstr != "") {//文号不为空
                var isSendDocMark = (markGovdocType == "1" || markGovdocType == "3") && (markType == "doc_mark" || markType == "serial_no");
                if (isSendDocMark) {//发文/签报的公文文号、内部文号
                    var markTypeValue = getMarkTypeValue(markType);
                    var oldSelectType = $("#old_selectType_" + markTypeValue).val();
                    var oldMarkNumber = $("#old_markNumber_" + markTypeValue).val();
                    if (oldSelectType != 1 && oldMarkNumber == "") {//非手工输入
                        return "markNumberIsNull";
                    }
                }
            }
        } else if (fieldObj.tagName == 'input' || fieldObj.tagName == 'INPUT') {//收文doc_mark
            markstr = field.val();
        }
    }
    if (markIsNull) {
        if (markstr == "") {
            return "markstrIsNull";
        }
    }
    return markstr;
}

function markInputIsEdit(markType) {
    var isEdit = true;
    var field = getDocMarkField(markType);
    if (field && field.size() > 0) {
        var fieldObj = field[0];
        if (fieldObj.tagName == 'span' || fieldObj.tagName == 'SPAN') {//只读
            isEdit = false;
        }
    }
    return isEdit;
}

function checkMarkstr(markType, markstr, isAlert) {
    var markTypeTitle = getMarkTypeTitle(markType, markGovdocType);
    var errorMsg = "";
    if (markstr == "wordNoIsNull" || markstr == "markstrIsNull") {
        errorMsg = markTypeTitle + $.i18n("govdoc.empty.cannot");
    } else if (markstr == "markNumberIs0") {
        errorMsg = markTypeTitle + $.i18n("govdoc.serial.canNot.0");
    } else if (markstr == "markNumberIsNull") {
        errorMsg = markTypeTitle + $.i18n("govdoc.serial.canNot.empty");
    } else if (!markstr) {
        errorMsg = markTypeTitle + $.i18n("govdoc.empty.cannot");
    }
    if (errorMsg != "") {
        if (isAlert) {
            alert(errorMsg);
        }
        return false;
    }
    return true;
}

function getMarkTypeTitle(markType, markGovdocType) {
    var markTypeTitle = "";
    if (markType == "doc_mark") {
        if (markGovdocType == "1" || markGovdocType == "3") {
            markTypeTitle = $.i18n("govdoc.document.number");
        } else {
            markTypeTitle = $.i18n("govdoc.communication.number");
        }
    } else if (markType == "serial_no") {
        if (markGovdocType == "1" || markGovdocType == "3") {
            markTypeTitle = $.i18n("govdoc.Internal.document.number");
        } else if (markGovdocType == "4") {
            markTypeTitle = $.i18n("govdoc.signMark.label");
        } else {
            markTypeTitle = $.i18n("govdoc.receive.label");
        }
    } else {
        markTypeTitle = $.i18n("govdoc.signMark.label");
    }
    return markTypeTitle;
}


/**
 *
 * @param markType
 * @param markstr
 * @returns
 */
function resetMarkValueByMarkType(markType, markstr) {

}

var markDialog;
$.fn.openMarkDialog = function (url, title, width, height, obj, callback) {
    markDialog = $.dialog({
        url: _ctxPath + url,
        id: 'markDialog',
        width: width,
        height: height,
        targetWindow: getCtpTop(),
        title: title,
        buttons: [{
            id: "sure",
            isEmphasize: true,
            text: $.i18n('common.button.ok.label'),
            handler: function (param) {
                var o = markDialog.getReturnValue();
                callback(o);
                if (o && o.length > 0) {
                    markDialog.close();
                }
            }
        }, {
            id: "cancel",
            text: $.i18n('common.button.cancel.label'),
            handler: function () {
                markDialog.close();
            }
        }]
    });
};


/**
 * 从select提取数据，拼装为字符串
 * @param select
 * @returns {String}
 */
function select2DataStr(select) {
    var datastr = "selectdata : [";
    var i = 0;
    var length = select.find('option').size();
    select.find('option').each(function () {
        var option = $(this);
        var label = option.text();
        var title = option.attr('title');
        if (title == undefined) {
            title = label;
        }
        if (i != 0) {
            datastr += ",";
        }
        datastr += "{";
        datastr += "label:'" + label + "',";
        datastr += "title:'" + title + "',";
        datastr += "value:'" + option.val() + "'";
        datastr += "}";
        i++;
    });
    datastr += "]";
    return datastr;
}
