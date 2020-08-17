<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <%@ include file="/WEB-INF/jsp/common/common.jsp" %>
    <title></title>
    <script type="text/javascript" src="${path}/ajax.do?managerName=roomHistoryManager"></script>
    <script type="text/javascript" language="javascript">
        //请勿轻易修改这个变量不仅批量关闭窗口用，角色回填也需要回传值
        $().ready(function () {
            //加载表格
            var o = {};
            var searchobj = $.searchCondition({
                top: 10,
                right: 10,
                //点搜索按钮取值
                searchHandler: function () {
                    var params = searchobj.g.getReturnValue();
                    console.log(params,'params');
                    if (params != null) {
                        o = {};
                        if (params.condition == 'meetingname') {
                            o.meetingname = params.value;
                        }
                        if (params.condition == 'starttime') {
                            if (params.value[0] != "") {
                                o.beginTime = params.value[0];
                            }
                            if (params.value[1] != "") {
                                o.endTime = params.value[1];
                            }
                        }
                    }
                    console.log(o, 'zhou')
                    $('#mtAppHistory').ajaxgridLoad(o);
                },
                conditions: [{
                    id: 'meetingname',
                    name: 'meetingname',
                    type: 'input',
                    text: "会议室名称",//标题
                    value: 'meetingname',
                    maxLength: 100
                }, {
                    id: 'starttime',
                    name: 'starttime',
                    type: 'datemulti',
                    text: "会议开始时间",//发起时间
                    value: 'starttime',
                    dateTime: false,
                    ifFormat: '%Y-%m-%d'
                }]
            });
            //列表
            var grid = $("#mtAppHistory").ajaxgrid({
                gridType: 'autoGrid',
                colModel: [
                    //     {
                    //     display: 'id',
                    //     name: 'id',
                    //     // width: '3%',
                    //     // align: 'center',
                    //     type: 'checkbox'
                    // },
                    {
                        display: "申请会议室",
                        name: 'meetingname',
                        width: '12%'
                    }, {
                        display: "申请人",
                        name: 'pername',
                        width: '8%'
                    }, {
                        display: "申请人电话",
                        name: 'sqrdh',
                        width: '12%'
                    }, {
                        display: "申请人所在部门",
                        name: 'deptname',
                        width: '12%'
                    }, {
                        display: "开始时间",
                        name: 'startdatetime',
                        width: '12%'
                    }, {
                        display: "结束时间",
                        name: 'enddatetime',
                        width: '12%'
                    }, {
                        display: "用途",
                        name: 'description',
                        width: '12%'
                    }, {
                        display: "会场要求",
                        name: 'hcyq',
                        width: '12%'
                    }],
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
                managerName: "roomHistoryManager",
                managerMethod: "findPageByCondition",

            });
            $('#mtAppHistory').ajaxgridLoad(o);

        });
    </script>
</head>
<body class="body-pading" leftmargin="0" topmargin="" marginwidth="0" marginheight="0">
<div id='layout' class="comp" comp="type:'layout'">
    <div class="layout_north f0f0f0" id="north">
        <div height="100%" id="webfx-menu-object-1" class="webfx-menu-bar-gray">
            <table border="0" cellspacing="0" cellpadding="0" height="55">
                <tbody>
                <tr height="100%" valign="middle">
                    <td height="55">
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div class="layout_center over_hidden" id="center">
        <table id="mtAppHistory" class="flexme3" style="display: none;"></table>
    </div>
</div>
</body>
</html>
