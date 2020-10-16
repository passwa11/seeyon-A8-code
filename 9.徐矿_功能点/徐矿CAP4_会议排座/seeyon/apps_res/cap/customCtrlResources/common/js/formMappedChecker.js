/**
 * Created by yangchao on 2018/12/27.
 */

//<--start======================= 兼容IE8 Array.prototype.indexOf Object.keys
if (!Array.prototype.indexOf){
    Array.prototype.indexOf = function(elt /*, from*/){
        var len = this.length >>> 0;
        var from = Number(arguments[1]) || 0;
        from = (from < 0)
            ? Math.ceil(from)
            : Math.floor(from);
        if (from < 0)
            from += len;
        for (; from < len; from++)
        {
            if (from in this &&
                this[from] === elt)
                return from;
        }
        return -1;
    };
}

var DONT_ENUM =  "propertyIsEnumerable,isPrototypeOf,hasOwnProperty,toLocaleString,toString,valueOf,constructor".split(","),
    hasOwn = ({}).hasOwnProperty;
for (var i in {
    toString: 1
}){
    DONT_ENUM = false;
}

Object.keys = Object.keys || function(obj){
    var result = [];
    for(var key in obj ) if(hasOwn.call(obj,key)){
        result.push(key)
    }
    if(DONT_ENUM && obj){
        for(var i = 0 ;key = DONT_ENUM[i++]; ){
            if(hasOwn.call(obj,key)){
                result.push(key);
            }
        }
    }
    return result;
};

//end-->======================= 兼容IE8

function FormToFormStrategy (srcFormmain, srcFormson, dstFormmain, dstFormson) {
    this.srcFormmain = srcFormmain > 1 ? 1 : srcFormmain;
    this.srcFormson = srcFormson > 2 ? 2 : srcFormson;
    this.dstFormmain = dstFormmain > 1 ? 1 : dstFormmain;
    this.dstFormson = dstFormson > 2 ? 2 : dstFormson;
}

FormToFormStrategy.prototype.getKeyCode = function () {
    return String(this.srcFormmain * 1 + this.srcFormson * 2) + String(this.dstFormmain * 1 + this.dstFormson * 2);
}

/*按照排列组合，有36种情况， 2*3*2*3=36
主表权值为1，明细表权值为2，因为主表只可能有一个，明细可能有多个
权值码表：
0 没有表
1 有1个主表
2 有1个明细表
3 有1个主表+1个明细表
4 有多个明细表
5 有1个主表+多个明细表

忽略没有src或dst值不全的情况共11中情况，
权值码中含有0为未录入完全
权值码大于等于41的都是源表为多明细表

* */

//由于源表已经过滤了，这里直接提示目标表
var _strategyNoticeInfo = {
	'01': $.i18n('com.cap.btn.newFormDataBtn.01'),
	'02': $.i18n('com.cap.btn.newFormDataBtn.02'),
	'03': $.i18n('com.cap.btn.newFormDataBtn.03'),
	'04': $.i18n('com.cap.btn.newFormDataBtn.04'),
	'05': $.i18n('com.cap.btn.newFormDataBtn.04'), //同04
    '11': $.i18n('com.cap.btn.newFormDataBtn.01'),
    '12': $.i18n('com.cap.btn.newFormDataBtn.02'),
    '13': $.i18n('com.cap.btn.newFormDataBtn.03'),
    '14': $.i18n('com.cap.btn.newFormDataBtn.04'),
    '15': $.i18n('com.cap.btn.newFormDataBtn.04'), //同14
    '21': $.i18n('com.cap.btn.newFormDataBtn.01'),
    '22': $.i18n('com.cap.btn.newFormDataBtn.02'),
    '23': $.i18n('com.cap.btn.newFormDataBtn.03'),
    '24': $.i18n('com.cap.btn.newFormDataBtn.04'),
    '25': $.i18n('com.cap.btn.newFormDataBtn.04'), //同24
    '31': $.i18n('com.cap.btn.newFormDataBtn.31'),
    '32': $.i18n('com.cap.btn.newFormDataBtn.31'),
    '33': $.i18n('com.cap.btn.newFormDataBtn.31'),
    '34': $.i18n('com.cap.btn.newFormDataBtn.31'),
    '35': $.i18n('com.cap.btn.newFormDataBtn.31'), //同34
    '41': $.i18n('com.cap.btn.newFormDataBtn.41'),   //超过41以上都是源表为多明细表
    '42': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '43': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '44': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '45': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '51': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '52': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '53': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '54': $.i18n('com.cap.btn.newFormDataBtn.41'),
    '55': $.i18n('com.cap.btn.newFormDataBtn.41'),
	'10': $.i18n('com.cap.btn.newFormDataBtn.10'),
	'20': $.i18n('com.cap.btn.newFormDataBtn.20'),
	'30': $.i18n('com.cap.btn.newFormDataBtn.31'),
	'40': $.i18n('com.cap.btn.newFormDataBtn.41'),
	'50': $.i18n('com.cap.btn.newFormDataBtn.41'),
};

var _inputTypeMappedInfo = {
    text: ['text', 'textarea'],
    textarea: ['textarea'],
    checkbox: ['checkbox'],
    select: ['select', 'radio', 'imageradio', 'imageselect'],
    radio: ['select', 'radio', 'imageradio', 'imageselect'],
    imageselect: ['select', 'radio', 'imageradio', 'imageselect'],
    imageradio: ['select', 'radio', 'imageradio', 'imageselect'],
    date: ['date', 'datetime'],
    datetime: ['datetime'],
    linenumber: [],                 //序号不支持
    flowdealoption: ['textarea'],
    document: ['document'],
    attachment: ['attachment'],
    image: ['image', 'attachment'],
    maplocate: [],                  //位置定位不支持
    mapphoto: [],                   //拍照定位不支持
    mapmarked: ['mapmarked'],
    member: ['member', 'multimember'],
    post: ['post', 'multipost'],
    department: ['department', 'multidepartment'],
    account: ['account', 'multiaccount'],
    level: ['level', 'multilevel'],
    multimember: ['multimember'],
    multipost: ['multipost'],
    multidepartment: ['multidepartment'],
    multiaccount: ['multiaccount'],
    multilevel: ['multilevel']
};

// var _inputTypeDisplayInfo = {
//     text: '文本',
//     number: '数字',
//     textarea: '文本域',
//     checkbox: '复选',
//     radio: '单选',
//     select: '下拉',
//     imageselect: '图片下拉',
//     imageradio: '图片单选',
//     date: '日期',
//     datetime: '日期时间',
//     linenumber: '序号',                 //序号不支持
//     flowdealoption: '流程处理意见',
//     document: '关联文档',
//     attachment: '上传附件',
//     image: '上传图片',
//     maplocate: '位置定位',                  //位置定位不支持
//     mapphoto: '拍照定位',                   //拍照定位不支持
//     mapmarked: '地图标注',
//     member: '选人',
//     post: '选岗位',
//     department: '选部门',
//     account: '选单位',
//     level: '选职务级别',
//     multimember: '选多人',
//     multipost: '选多岗位',
//     multidepartment: '选多部门',
//     multiaccount: '选多单位',
//     multilevel: '选多职务级别'
// }

function FormMappedChecker(options) {
    this.allowedStrategies = options.allowedStrategies;
    this.initStrategies(this.allowedStrategies);
}

FormMappedChecker.prototype.initStrategies = function (strategies) {
    this.allowedStrategyCodeMap = {};
    var len = (strategies || []).length;
    for (var i=0; i<len; i++) {
        var allowedStrategy = strategies[i];
        this.allowedStrategyCodeMap[allowedStrategy.getKeyCode()] = '';
    }
}

FormMappedChecker.prototype.initCache = function () {
    this._cache = {
        srcFieldMapAsId: {},
        dstFieldMapAsId: {},
        srcFormmainMap: {},
        srcFormsonMap: {},
        dstFormmainMap: {},
        dstFormsonMap: {},
    };
}

FormMappedChecker.prototype.destroy = function () {

}

/**
 * 对当前映射关系进行验证的API
 * @param data 格式：data = {curFieldInfo: {srcFieldInfo, dstFieldInfo}, fieldInfos: [{srcFieldInfo, dstFieldInfo}]}
 * srcFieldInfo(dstFieldInfo) = {name, tableName, display, type, typeText, fieldType, fieldLength, enumInfo }
 * @returns {{success: boolean, msg: string}}
 */
FormMappedChecker.prototype.assert = function (data) {
    this.curFieldInfo = data.curFieldInfo;
    this.fieldInfos = data.fieldInfos || [];
    //取当前行的数据校验字段映射规则
    var ret = this.fieldMatchAssert(this.curFieldInfo);
    if (!ret.success) {
        return ret;
    }

    //预处理所有数据
    this.preProcessData();
    //然后排查N字段到1字段映射
    ret = this.dstFieldRepeatAssert();
    if (!ret.success) {
        return ret;
    }

    //验证表到表之间关系
    ret = this.formToFormAssert();
    return ret;
}

/**
 * 全验证逻辑，验证所有行，在提交的时候用
 * @param data 格式：data = {curFieldInfo: {srcFieldInfo, dstFieldInfo}, fieldInfos: [{srcFieldInfo, dstFieldInfo}]}
 * srcFieldInfo(dstFieldInfo) = {name, tableName, display, type, typeText, fieldType, fieldLength, enumInfo }
 * @returns {{success: boolean, msg: string}}
 */
FormMappedChecker.prototype.assertAll = function (data) {
    this.fieldInfos = data.fieldInfos || [];
    var len = this.fieldInfos.length;
	if (len === 0) {
		return {success: true};
	}
	
    var ret;
    for (var i=0; i<len; i++) {
        var curFieldInfo = this.fieldInfos[i];
        ret = this.fieldMatchAssert(curFieldInfo);
        if (!ret.success) {
            return ret;
        }
    }

    //预处理所有数据
    this.preProcessData();

    //验证表到表之间关系
    ret = this.formToFormAssert();
    return ret;
}

/**
 * 验证目标表是否存在多个相同字段, 只验证当前行
 * @returns {{success: boolean, msg: string}}
 */
FormMappedChecker.prototype.dstFieldRepeatAssert = function () {
    var dstFieldName = this.curFieldInfo.dstFieldInfo.name;
    if (this._cache.dstFieldMapAsId[dstFieldName] > 1) {
        return {success: false, msg: $.i18n('com.cap.btn.newFormDataBtn.alreadymapping',this.getFieldDisplay(this.curFieldInfo.dstFieldInfo)), code:'201'};
    }

    return {success: true};
}

/**
 * 控件类型匹配验证
 * @param srcFieldInfo
 * @param dstFieldInfo
 * @returns {{success: boolean, msg: string}}
 */
FormMappedChecker.prototype.ctrlTypeMatchAssert = function (srcFieldInfo, dstFieldInfo) {
	//文本到数字的映射要特殊判断一下，因为文本和数字，type都是text
	if ((srcFieldInfo.type ===  'text' && srcFieldInfo.fieldType !=='DECIMAL' && dstFieldInfo.type ===  'text' && dstFieldInfo.fieldType === 'DECIMAL') ||
		(_inputTypeMappedInfo[srcFieldInfo.type] || []).indexOf(dstFieldInfo.type) === -1) {
		//TODO 此处表名，控件类型应该返回描述信息
        return {success:false, msg: $.i18n('com.cap.btn.newFormDataBtn.wrong',this.getFieldDisplay(srcFieldInfo),this.getInputTypeText(srcFieldInfo),this.getFieldDisplay(dstFieldInfo),this.getInputTypeText(dstFieldInfo)), code: '101'};
	} else {
		return {success: true};
	}
}

/**
 * 字段匹配验证入口,主要验证控件类型、字段类型、字段长度、小数位数、枚举（绑定的枚举一致，枚举层级，末级枚举）
 * @param curFieldInfo
 * @returns {{success: boolean, msg: string}}
 */
FormMappedChecker.prototype.fieldMatchAssert = function (curFieldInfo) {
    var srcFieldInfo = curFieldInfo.srcFieldInfo;
    var dstFieldInfo = curFieldInfo.dstFieldInfo;

    if (!Object.keys(srcFieldInfo).length || !Object.keys(dstFieldInfo).length) {
        return { success: true };
    }

    //首先根据映射map，验证控件类型是否匹配
    var ret = this.ctrlTypeMatchAssert(srcFieldInfo, dstFieldInfo);
    if (!ret.success) {
        return ret;
    }

    if (srcFieldInfo.type === 'text' && srcFieldInfo.fieldType === 'DECIMAL') {
        //数字到数字类型,分别判断整数和小数部分
        if (dstFieldInfo.type === 'text' && dstFieldInfo.fieldType === 'DECIMAL') {
            var srcLengthArr = srcFieldInfo.fieldLength.split(',');
            var dstLengthArr = dstFieldInfo.fieldLength.split(',');
            var srcDigit = srcLengthArr.length > 1 ? Number(srcLengthArr[1]) : 0;
            var dstDigit = dstLengthArr.length > 1 ? Number(dstLengthArr[1]) : 0;
            var srcInteger = Number(srcLengthArr[0]) - srcDigit;
            var dstInteger = Number(dstLengthArr[0]) - dstDigit;
            if (dstDigit < srcDigit) {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong2',this.getFieldDisplay(srcFieldInfo),srcDigit,this.getFieldDisplay(dstFieldInfo),dstDigit), code: '103'};
            } else if (dstInteger < srcInteger) {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong2',this.getFieldDisplay(srcFieldInfo),srcInteger,this.getFieldDisplay(dstFieldInfo),dstInteger), code: '103'};
            } else {
                return {success: true};
            }
        } else if (dstFieldInfo.fieldType === 'VARCHAR') {
            //数字只能映射到文本，数字，文本域，如果是文本类型，判断一下是否VARCHAR,效验长度
            var srcLengthArr = srcFieldInfo.fieldLength.split(',');
            var srcDigit = srcLengthArr.length > 1 ? Number(srcLengthArr[1]) : 0;
            var srcLength = Number(srcLengthArr[0]);
            var dstLength = Number(dstFieldInfo.fieldLength);
            //如果有小数位数，最后效验长度时，要加上小数点
            if (dstLength > srcLength || (dstLength === srcLength && srcDigit === 0)) {
                return {success: true};
            } else {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong3',this.getFieldDisplay(srcFieldInfo),srcFieldInfo.fieldLength,this.getFieldDisplay(dstFieldInfo),dstFieldInfo.fieldLength), code: '103'};
            }

        } else {
            return {success: true};
        }
    } else if (['text', 'textarea', 'flowdealoption', 'multimember', 'multipost', 'multidepartment', 'multiaccount', 'multilevel'].indexOf(srcFieldInfo.type) !== -1) {
        //文本，文本域，流程意见，多组织控件
        if (srcFieldInfo.fieldType === 'VARCHAR') {
            //验证dstFieldInfo如果是VARCHAR时只能字段长度只能变大，如果LONGTEXT，则不需要判断长度
            if (dstFieldInfo.fieldType === 'VARCHAR' && Number(dstFieldInfo.fieldLength) < Number(srcFieldInfo.fieldLength)) {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong3',this.getFieldDisplay(srcFieldInfo),srcFieldInfo.fieldLength,this.getFieldDisplay(dstFieldInfo),dstFieldInfo.fieldLength), code: '103'};
            } else if (dstFieldInfo.fieldType !== 'VARCHAR' && dstFieldInfo.fieldType !== 'LONGTEXT') {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong4',this.getFieldDisplay(srcFieldInfo),this.getFieldDisplay(dstFieldInfo)), code: '102'};
            } else {
                return {success: true};
            }
        } else {
            //LONGTEXT,验证dstFieldInfo必须是LONGTEXT
            if (dstFieldInfo.fieldType !== 'LONGTEXT') {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong4',this.getFieldDisplay(srcFieldInfo),this.getFieldDisplay(dstFieldInfo)), code: '102'};
            } else {
                return {success: true};
            }
        }
    } else if (['select', 'radio', 'imageselect', 'imageradio'].indexOf(srcFieldInfo.type) !== -1) {
        //枚举（绑定的枚举一致，枚举层级，末级枚举）验证，为了保险用split，不然直接lastIndexOf就判断了
        var srcArr = srcFieldInfo.enumInfo.split('_');
        var dstArr = dstFieldInfo.enumInfo.split('_');
        if (srcArr.length >= 3 && dstArr.length >= 3) {
            if (srcArr[0] !== dstArr[0]) {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong5',this.getFieldDisplay(srcFieldInfo),this.getFieldDisplay(dstFieldInfo)), code: '104'};
            } else if (srcArr[1] !== dstArr[1]) {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong6',this.getFieldDisplay(srcFieldInfo),this.getFieldDisplay(dstFieldInfo)), code: '104'};
            } else if (srcArr[2] !== dstArr[2]) {
                return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong6',this.getFieldDisplay(srcFieldInfo),this.getFieldDisplay(dstFieldInfo)), code: '104'};
            } else {
                return {success: true};
            }
        } else {
            return {success: false, msg : $.i18n('com.cap.btn.newFormDataBtn.wrong5',this.getFieldDisplay(srcFieldInfo),this.getFieldDisplay(dstFieldInfo)), code: '104'};
        }
    }

    return {success: true};
}

/**
 * 表到表关系验证
 * @returns {{success: boolean, msg: string}}
 */
FormMappedChecker.prototype.formToFormAssert = function () {
    var curStrategy = this.buildCurStrategy();
    var curKeyCode = curStrategy.getKeyCode();
    if (!this.allowedStrategyCodeMap.hasOwnProperty(curKeyCode)) {
        return {success: false, msg: _strategyNoticeInfo[curKeyCode], code: curKeyCode};
    }

    return {success: true};
}

FormMappedChecker.prototype.getFieldDisplay = function (fieldInfo) {
    return fieldInfo.display || fieldInfo.name;
}

FormMappedChecker.prototype.getInputTypeText = function (fieldInfo) {
    return fieldInfo.typeText || fieldInfo.type;
}

FormMappedChecker.prototype.buildCurStrategy = function () {
	//统计当前录入策略的时候，如果出现半边空对象，当做已选择主表处理
	//10同11,20同21,30同31,40同41，50同51；01同11，02同12，03同13,04同14,05同15
	const v1 = Object.keys(this._cache.srcFormmainMap).length;
	const v2 = Object.keys(this._cache.srcFormsonMap).length;
	const v3 = Object.keys(this._cache.dstFormmainMap).length;
	const v4 = Object.keys(this._cache.dstFormsonMap).length;
    return new FormToFormStrategy(v1 || v2 ? v1 : 1, v2, v3 || v4 ? v3 : 1, v4);
}

FormMappedChecker.prototype.isMaster = function (fieldInfo) {
    return fieldInfo.tableName.indexOf('formmain') !== -1;
    //return fieldInfo.isMasterField;
}

/**
 * 预处理数据
 */
FormMappedChecker.prototype.preProcessData = function () {
    this.initCache();
    var len = this.fieldInfos.length;
    for (var i=0; i<len; i++) {
        var info = this.fieldInfos[i];
        //判断源表是否设置了信息
        var srcFieldInfo = info.srcFieldInfo;
        var dstFieldInfo = info.dstFieldInfo;
        if (Object.keys(srcFieldInfo || {}).length > 0) {
            var oldSrcCount = this._cache.srcFieldMapAsId[srcFieldInfo.name];
            this._cache.srcFieldMapAsId[srcFieldInfo.name] = oldSrcCount ? oldSrcCount + 1 : 1;
            if (this.isMaster(srcFieldInfo)) {
                this._cache.srcFormmainMap[srcFieldInfo.tableName] = '';
            } else {
                this._cache.srcFormsonMap[srcFieldInfo.tableName] = '';
            }
        }

        //判断目标表是否设置了信息
        if (Object.keys(dstFieldInfo || {}).length > 0) {
            var oldDstCount = this._cache.dstFieldMapAsId[dstFieldInfo.name];
            this._cache.dstFieldMapAsId[dstFieldInfo.name] = oldDstCount ? oldDstCount + 1 : 1;
            if (this.isMaster(dstFieldInfo)) {
                this._cache.dstFormmainMap[dstFieldInfo.tableName] = '';
            } else {
                this._cache.dstFormsonMap[dstFieldInfo.tableName] = '';
            }
        }
    }
}
