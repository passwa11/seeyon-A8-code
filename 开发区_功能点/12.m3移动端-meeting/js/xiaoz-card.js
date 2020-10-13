;(function($) {
    window.xiaoz = $.life('.xiaozhi-card', {
        //国际化
        $i18n: [
            {
                i18nPath: 'http://xiaoz.v5.cmp/v1.0.0/i18n/',
                moduleName: 'xiaoz'
            }
        ],

        $onloaded: function() {
            var _this = this;
            this.initEvent();
            this.initData(function() {
                var type = _this.getListType();
                //数据格式处理下，分类展示的数据格式不一样， 初始化一下
                type === 'classifyList' && (_this.data = _this.data.data);
                _this.render(type);
                _this.setIconHeader();
            });
        },

        methods: {

            //数据
            data: null,

            //默认头像
            defaultHeader: 'http://cmp/v/img/def-header.png',

            //remote类型的默认头像，暂时使用头像，后面UI重新提供
            remoteDefHeader: this.defaultHeader,

            //头像
            header: [],

            //事件初始化
            initEvent: function() {
                var node = $cmp(this.target);
                //跳转详情
                node.on('tap', '.xiaoz-litem', function() {
                    var param = JSON.parse($cmp(this).attr('data-param'));
                    var gotoUrl = '';
                    if(param.state && param.state == 0){ //待发跳到编辑页面,其它状态调到详情页
                    	gotoUrl = 'http://meeting.v5.cmp/v1.0.0/html/meetingModify.html';
                    }else{
                    	gotoUrl = 'http://meeting.v5.cmp/v1.0.0/html/meetingDetail.html';
                    }
                    cmp.speechRobot.openPage({
                        url: gotoUrl,
                        params:param,
                        success:function(){},
                        error:function(){}

                    });
                });

                //跳转更多
                node.on('tap', '.xiaoz-more', function() {
                    var param = JSON.parse(this.getAttribute("data-param"));
                    cmp.speechRobot.openPage({
                        url: 'http://meeting.v5.cmp/v1.0.0/html/meeting_list_mine.html',
                        params: param,
                        success: function(){},
                        error: function(){}
                    })
                });
            },

            getListType: function() {
                var dataType = Object.prototype.toString.call(this.data);
                if (dataType === '[object Array]'){
                    return 'create'
                }
                if (dataType === '[object Object]') {
                    return 'classifyList';
                }
            },

            //数据初始化
            initData: function(callback) {
                var _this = this;
                cmp.speechRobot.getSpeechInput({
                    success: function(ret) {
                        console.log(ret)
                        _this.data = ret;
                        callback();
                    },
                    error: function() {
                        cmp.notification.toast(_this.i18n('getCardInfoError'), 'center', 2000);
                    }
                });
                
            },

            //渲染
            render: function(listType) {
                //每次渲染重置
                this.header = [];
                //根据listType获取对应的模板
                var template = this[ listType + 'Template' ]();
                $cmp(this.target).html(template);
            },

            //新建模板
            createTemplate: function() {
                return  '<div class="xiaoz-list">\
                            <ul>\
                                ' + this.createListItemTemplate(this.data[ 0 ]) + '\
                            </ul>\
                        </div>'
            },

            createListItemTemplate: function(data) {
                var title = (data.title || '').escapeHTML(),
                    name = (data.createUserName || '').escapeHTML();
                return '<li class="xiaoz-litem flex-h" data-param=\'' + JSON.stringify(data || '{}') + '\'>\
                            ' + this.getLeftIcon(data) + '\
                            <div class="xiaoz-cont flex-1">\
                                <p class="xiaoz-title textover-2">' + title + '</p>\
                                <p class="xiaoz-info">' + this.i18n('sender') + name + '</p>\
                                <p class="xiaoz-info meeting-time">' + this.i18n('meetingTime') + data.showTime.escapeHTML() + '</p>\
                            </div>\
                        </li>';
            },

            //列表模板
            listTemplate: function() {
                var total = this.data.data.length,
                    param = this.data.queryParams;
                return  '<div class="xiaoz-list">\
                            <ul>\
                                ' + this.listItemTemplate(this.data.data) + '\
                            </ul>\
                            ' + this.moreTemplate(total, param) + '\
                        </div>'
            },

            //列表分类模板
            classifyListTemplate: function() {
                var data = this.data,
                    str = '';
                //外层循环 分类
                for (var i = 0;i < data.length;i++) {
                    var classify = data[ i ],
                        total = classify.data.length,
                        param = classify.queryParams;
                    if (!classify.data || (classify.data && classify.data.length === 0)) {continue;}
                    str +=  '<div class="xiaoz-list">\
                            ' + this.classifyNameTemplate(classify) + '\
                                <ul>\
                                    ' + this.listItemTemplate(classify.data) + '\
                                </ul>\
                                ' + this.moreTemplate(total, param) + '\
                            </div>';
                }
                return str;
            },

            //分类名称模板
            classifyNameTemplate: function(classify) {
                return '<h3 class="classify-name textover-1">' + classify.title.escapeHTML() + '</h3>'
            },

            //更多的模板
            moreTemplate: function(total, param) {
                //小于等于三条不显示
                if (total <= 3) {return '';}
                return  '<a class="xiaoz-more x-center flex-h" data-param=\'' + JSON.stringify(param || '{}') + '\'>\
                            <span class="cmp-icon cmp-icon-search"></span>\
                            <span class="more-text flex-1">' + this.i18n('more') + '</span>\
                            <span class="right_arrow see-icon-v5-common-arrow-right"></span>\
                        </a>';
            },

            //列表中的单个item的模板
            listItemTemplate: function(item) {
                var str = '';
                for (var i = 0;i < 3;i++) {
                    if (!item[ i ]) {continue;}
                    var data = item[ i ],
                        state = this.getMeetingState(data.feedbackFlag || ''),
                        title = (data.title || '').escapeHTML();
                    str += '<li class="xiaoz-litem flex-h" data-param=\'' + JSON.stringify(data.gotoParams || '{}') + '\'>\
                                ' + this.getLeftIcon(data) + '\
                                <div class="xiaoz-cont flex-1">\
                                    <p class="xiaoz-title textover-2">' + title + '</p>\
                                    ' + this.getSecondLine(data) + '\
                                    ' + this.getThirdLine(data) + '\
                                    ' + this.getFourthLine(data) + '\
                                    ' + this.getFifthLine(data) + '\
                                    ' + state + '\
                                </div>\
                            </li>';
                }
                return str;
            },

            getMeetingState: function(data) {
                var map = {
                    //不参加
                    '0': {txt: this.i18n('unjoin'), icon: 'meet-unjoin'},
                    //参加
                    '1': {txt: this.i18n('join'), icon: 'meet-join'},
                    //待定
                    '-1': {txt: this.i18n('wait'), icon: 'meet-wait'},
                    //未回执
                    '-100': {txt: this.i18n('noRes'), icon: 'meet-noRes'}
                }, btnCfg = map[ data ];
                console.log(btnCfg);
                //异常检查
                if (!btnCfg) {console.warn('会议数据feedbackFlag异常', data);return '';}
                return '<span class="meeting-state m3-icon-status-seal ' + btnCfg.icon + '"><i class="textover-1">' + btnCfg.txt + '</i></p>'
            },

            getSecondLine: function(data) {
                var name = this.subString(data.createUserName || '', 15).escapeHTML(),
                    space = '&nbsp;&nbsp;',
                    hasAttachment = data.hasAttachments ? '<span class="see-icon-accessory"></span>' : '';
                return '<p class="xiaoz-info">' + name + space + this.formatDate(data.createDate) + hasAttachment + '</p>'
            },

            getThirdLine: function(data) {
                var space = '&nbsp;&nbsp;&nbsp;',
                    joinNum = (data.joinCount || 0) + '&nbsp;',
                    waitNum = (data.pendingCount || 0) + '&nbsp;',
                    unJoinNum = (data.unjoinCount || 0) + '&nbsp;';
                return '<p class="xiaoz-info">' + joinNum + this.i18n('join') + space + unJoinNum + this.i18n('unjoin') + space +waitNum + this.i18n('wait') + '</p>'
            },

            getFourthLine: function(data) {
                if (!data.meetPlace) {return '';}
                var place = (data.meetPlace || '').escapeHTML();
                return '<p class="xiaoz-info textover-1">' + this.i18n('meetingPlace') + place + '</p>';
            },

            getFifthLine: function(data) {
                var start = this.formatDate(data.beginDate),
                    end = this.formatDate(data.endDate);
                return '<p class="xiaoz-info textover-2 meeting-time">' + this.i18n('meetingTime') + start + ' - ' + end + '</p>';
            },

            subString: function(str, len) {
                var dotStr = str.length > len ? '...' : ''
                return str.substring(0, len) + dotStr;
            },

            //获取左侧图标内容
            getLeftIcon: function(item) {
                var id = 'icon' + Math.floor(Math.random() * 10000000);
                    iconValue = item.icon;
                this.header.push({
                    id: id,
                    src: cmp.seeyonbasepath + '/rest/orgMember/avatar/' + iconValue + '?maxWidth=200',
                    default: this.defaultHeader
                });
                return '<div class="list-header" id="' + id + '"></div>'
            },

            //设置头像
            setIconHeader: function() {
                for (var i = 0;i < this.header.length;i++) {
                    this.loadImg(this.header[ i ]);
                }
            },

            //加载图片
            loadImg: function(opts) {
                var img = new Image();
                img.onload = function() {
                    $cmp('#' + opts.id).css({
                        backgroundImage: 'url(' + opts.src + ')'
                    });
                };
                img.onerror = function() {
                    $cmp('#' + opts.id).css({
                        backgroundImage: 'url(' + opts.default + ')'
                    });
                };
                img.src = opts.src;
            },

            //获取国际化
            i18n: function(key) {
                return this.getI18n('xiaoz.m3.h5.' + key);
            },

            formatDate: function(timeStamp) {
                timeStamp = parseInt(timeStamp);
                if (isNaN(timeStamp)) {return 'NaN';}
                var today = new Date(),
                    dataStamp = new Date(timeStamp),
                    yesStart, 
                    yesEnd;
                today.setHours(0);
                today.setMinutes(0);
                today.setSeconds(0);
                today.setMilliseconds(0);
                var addZore = function(data) {
                    data = data + '';
                    if (data.length > 1)
                        return data;
                    else
                        return '0' + data;
                },
                todayStart = today.getTime(),
                todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1000;
                yesEnd = todayStart - 1000;
                yesStart = todayStart - 24 * 60 * 60 * 1000;
                //昨天以前显示：年月日
                if (dataStamp < yesStart) {
                    return addZore(dataStamp.getFullYear()) + '-' + addZore(dataStamp.getMonth() + 1) + '-' + addZore(dataStamp.getDate());
                //昨天显示：昨天 + 时分
                } else if (dataStamp >= yesStart && dataStamp <= yesEnd) {
                    return this.i18n('yesterday') + ' ' + addZore(dataStamp.getHours()) + ':' + addZore(dataStamp.getMinutes());
                //今天显示：今天 + 时分
                } else if (dataStamp >= todayStart && dataStamp <= todayEnd) {
                    return addZore(dataStamp.getHours()) + ':' + addZore(dataStamp.getMinutes());
                //明天以及明天之后显示：年月日 + 时分
                } else {
                    return addZore(dataStamp.getFullYear()) + '-' + addZore(dataStamp.getMonth() + 1) + '-' + addZore(dataStamp.getDate()) + ' ' + addZore(dataStamp.getHours()) + ':' + addZore(dataStamp.getMinutes());
                }  
            }
        }
    })
})(cmp);