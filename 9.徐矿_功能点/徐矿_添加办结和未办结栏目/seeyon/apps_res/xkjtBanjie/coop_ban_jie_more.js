(function () {
    $(document).ready(function () {
        new MxtLayout({
            'id': 'layout',
            'centerArea': {
                'id': 'center',
                'border': false,
                'minHeight': 20
            },
            'northArea': {
                'id': 'north',
                'height': 50,
                'sprit': false,
                'border': false
            },
        });
        //项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 start
        var o = null;
        if (templetIds != null && templetIds != "") {
            o = {"templetIds": templetIds};
        } else {
            o = {};
        }

        //项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 end
        var searchobj = $.searchCondition({
            top: 23,
            right: 10,
            //left:10,
            //bottom:10,
            //点搜索按钮取值
            searchHandler: function () {
                var params = searchobj.g.getReturnValue();
                if (params != null) {
                    o = {};
                    o.templetIds = templetIds;
                    if (params.condition == 'title') {
                        o.title = params.value;
                    }
                    if (params.condition == 'importLevel') {
                        o.importLevel = params.value;
                    }
                    if (params.condition == 'sender') {
                        o.sender = params.value;
                    }
                    if (params.condition == 'createDate') {
                        if (params.value[0] != "") {
                            o.beginTime = params.value[0];
                        }
                        if (params.value[1] != "") {
                            o.endTime = params.value[1];
                        }
                    }
                    if (params.condition == 'dealDate') {
                        if (params.value[0] != "") {
                            o.dealBeginTime = params.value[0];
                        }
                        if (params.value[1] != "") {
                            o.dealEndTime = params.value[1];
                        }
                    }
                }
                $('#banJieTable').ajaxgridLoad(o);
            },
            conditions: [{
                id: 'title',
                name: 'title',
                type: 'input',
                text: "标题",//标题
                value: 'title',
                maxLength: 100
            }

                , {
                    id: 'sender',
                    name: 'sender',
                    type: 'input',
                    text: "发起人",//发起人
                    value: 'sender'
                }, {
                    id: 'datetime',
                    name: 'datetime',
                    type: 'datemulti',
                    text: "发起时间",//发起时间
                    value: 'createDate',
                    dateTime: false,
                    ifFormat: '%Y-%m-%d'
                }
            ]
        });
        //表格加载
        var grid = $('#banJieTable').ajaxgrid({
            colModel: [{
                display: "id",
                name: 'id',
                sortable: true,
                width: '5%',
                type: 'checkbox'
            }, {
                display: "标题",
                name: 'subject',
                sortable: true,
                width: '40%'
            }, {
                display: "创建时间",
                name: 'start_date',
                sortable: true,
                width: '20%'
            }, {
                display: "发起者",
                name: 'start_name',
                sortable: true,
                width: '20%'
            }
            , {
                display: "当前代办人",
                name: 'current_nodes_info',
                sortable: true,
                width: '15%'
            }
                // , {
                // 	display : "发文单位",
                // 	name : 'send_unit',
                // 	sortable : true,
                // 	width : '12.5%'
                // }
                // , {
                // 	display : "处理期限",
                // 	name : 'deadline_datetime',
                // 	sortable : true,
                // 	width : '12.5%'
                // }
                // , {
                // 	display : "分类",
                // 	name : 'edoc_type',
                // 	sortable : true,
                // 	width : '10%'
                // }
            ],
            render: rend,
            click: openDetail,
            height: 200,
            showTableToggleBtn: true,
            parentId: 'center',
            vChange: true,
            vChangeParam: {
                overflow: "hidden",
                autoResize: true
            },
            isHaveIframe: true,
            slideToggleBtn: true,
            managerName: "allItemsManager",
            managerMethod: "findMoreCooprationXkjtBanjie"
        });
        //项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 start
        $("#banJieTable").ajaxgridLoad(o);

        //项目 徐矿 办结栏目更多根据模板id查询   zelda 2019年12月7日11:09:24 end
        function rend(txt, rowData, rowIndex, colIndex, colObj) {
            if (colObj.name == 'edoc_type') {
                if (txt == 0) {
                    return '<a onclick="javascript:window.top.vPortal.sectionHandler.multiRowVariableColumnTemplete.open_link(\'/edocController.do?method=entryManager&amp;entry=sendManager&amp;listType=listFinish\');" >发文</a>';
                } else {
                    return '<a onclick="javascript:window.top.vPortal.sectionHandler.multiRowVariableColumnTemplete.open_link(\'/edocController.do?method=entryManager&amp;entry=sendManager&amp;listType=listFinish&objectId=-7217783385919962978\');" >收文</a>';
                }
            }
            return txt;
        }

        function openDetail(data, r, c) {
            $.post("allItems.do?method=getCtpAffairIdBycolSummaryById", {id: data.id}, function (data) {
                var code = data.code;
                var url = "collaboration/collaboration.do?method=summary&openFrom=listSent&affairId=" + code;
                window.open(url, "_blank");
            });
        }
    });

})();






