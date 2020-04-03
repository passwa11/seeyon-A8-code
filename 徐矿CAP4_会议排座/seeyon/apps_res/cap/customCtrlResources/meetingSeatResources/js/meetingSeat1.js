/**
 * 说明： 是一个按钮类自定义控件的示例
 * */
$(function() {
    getPageData();
});

// 1.定义全局变量
var _page = {
    curCloneMember: {
        dom: {}, // 被拖拽cloneDom的人员对象
        index: "", // 被拖拽clone对象的人员序号
        name: "", // 被拖拽clone对象的人员名称
        moved: false // 是否完成拖拽动作（无论是否安插在右侧座位上）
    },
    curCloneSeat: {
        dom: {}, // 被拖拽cloneDom的位置对象
        index: "", // 被拖拽clone位置的人员序号
        name: "", // 被拖拽clone位置的人员名称
        moved: false // 是否完成拖拽动作（无论是否安插在右侧座位上）
    },
    // 被安插成功的人员位置信息，{row, col, index}
    insertedMembersSeats: [],
    isHandling: false,
    isDelete: false,
    resData: {},
    interfaceError: true
};

// 2.最终导出的会议室信息
var exportData = {
    "meetingId": "",
    "meetingRoomId": "",
    "rows": "",
    "cols": "",
    "users": [],
    "users1": [],
    "base64_png": "",
    "base64_pdf": "",
    "valid": false
};
var copyDom;

// 3.请求接口数据，获取左侧人员信息和右侧会议室排座信息
function getPageData() {
    // 获取dialog传过来的参数
    var transParams = window.parentDialogObj['meetingSeat'].getTransParams();
    var formMainData;

    for (var key in transParams.messageObj.formdata.formmains) {
        formMainData = transParams.messageObj.formdata.formmains[key];
    }

    $.ajax({
        loading: "请稍候...",
        url: '/seeyon/rest/cap4/meetingSeat/getMeetingSeatPeople',
        // url: '../json/meetingConfig.json',
        type: 'GET',
        dataType: 'json',
        contentType: 'application/json;charset=utf-8',
        data: { 'meetingId': formMainData.field0028.value },
        success: function(response) {
            _page.interfaceError = false;
            // 给人员添加序号并按序号重排序
            sortMembers(response.data);
            // 生成左侧人员信息
            createMembers(response.data, "newCreate");
            // 生成右侧座位
            createSeats(response.data);
            // 默认选中
            defaultSeats(response.data);
            // 一键入座
            quicklySeatMembers(response.data);
            // 一键清空
            quicklyCancel();
            // 批量操作
            partCancel();
            // 打开添加弹框
            showDialog();
            // 关闭添加弹框
            closeDialog();
            // 切换弹框内容
            checkContent();
            // 删除
            openDeleteBtn();
            // 搜索
            searchFn();
        },
        error: function(e) {
            var error = JSON.parse(e.responseText);
            if (error.message) {
                top.$.error(error.message);
            } else {
                top.$.error("接口请求失败");
            }
        }
    });
}

// 4.给人员按序号重排序
function sortMembers(data) {
    $.each(data.users, function(idx, item) {
        item.index = String(idx);
    });

    var index_start = data.users.length;
    $.each(data.users1, function(idx, item) {
        item.index = String(index_start + idx);
    });

    _page.resData = data;
}

// 5.生成左侧人员信息
function createMembers(data, type) {
    var $membersList = $(".left-list .members-list");
    var $unitsList = $(".left-list .units-list");
    $(".left-list .member").off("mousedown");
    $membersList.children().remove();
    $unitsList.children().remove();
    $(".members-list-title .counts").text(0);
    $(".units-list-title .counts").text(0);
    var ulIdx = 0,
        ulIdx1 = 0;
    var _users = data.users,
        _users1 = data.users1;

    if (type === "newCreate") {
        _users = data.users.filter(function(item) {
            return !(item.row && item.col)
        })
        _users1 = data.users1.filter(function(item) {
            return !(item.row && item.col)
        })
    }

    _users.forEach(function(row, idx) {
        if (idx % 3 === 0) {
            $membersList.append(
                '<ul class="members-row person">' +
                '<li class="member" memberIdx="' + row.index + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="member-dep">' + row.dep + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>' +
                '</ul>'
            );
        } else {
            var $membersRow = $(".members-list .members-row");
            ulIdx = parseInt(idx / 3, 10);
            $membersRow.eq(ulIdx).append(
                '<li class="member" memberIdx="' + row.index + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="member-dep">' + row.dep + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>'
            );
        }
        // 计数
        leftCountNumber($(".members-list-title .counts"));
    });

    _users1.forEach(function(row, idx) {
        if (idx % 3 === 0) {
            $unitsList.append(
                '<ul class="members-row unit">' +
                '<li class="member" memberIdx="' + row.index + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>' +
                '</ul>'
            );
        } else {
            var $unitsRow = $(".units-list .members-row");
            ulIdx1 = parseInt(idx / 3, 10);
            $unitsRow.eq(ulIdx1).append(
                '<li class="member" memberIdx="' + row.index + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>'
            );
        }
        // 计数
        leftCountNumber($(".units-list-title .counts"));
    });

    // 如果右侧有落座信息
    dragMember($(".left-list .member"));
}

// 6.生成右侧座位
function createSeats(data) {
    var ulNum = data.rows,
        liNum = data.cols;

    if (liNum && ulNum) {
        // 生成行
        var rowIdx = 0;
        for (var i = 0; i < ulNum; i++) {
            // 生成行头
            rowIdx++;
            // 生成内容行
            $(".seats-box .seats-content").append(
                '<ul class="seats-row" row="' + rowIdx + '"></ul>'
            );
        }

        // 生成列
        var colIdx = 0;
        for (var j = 0; j < liNum; j++) {
            // 生成列头
            colIdx++;
            $(".seats-box .seats-cols-head").append(
                '<li class="col-index" style="width: calc(100% / ' + liNum + ')">' + colIdx + '</li>'
            );

            // 生成内容列并添加col属性
            $(".seats-box .seats-row").append(
                '<li class="seat-li" style="width: calc(100% / ' + liNum + ')">' +
                '<span class="seat" col="' + colIdx + '" ></span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="move-icon"></span>' +
                '<span class="del-icon"></span>' +
                '</span>' +
                '</li>'
            );

            $(".seats-row").eq(j).children(".seat-li").eq(0).before('<li class="row-index">' + colIdx + '</li>')
        }

        // 给每个座位添加自定义row属性
        var $curSeatsRow;
        for (var m = 0; m < ulNum; m++) {
            $curSeatsRow = $(".seats-row").eq(m);
            $curSeatsRow.find(".seat").attr("row", $curSeatsRow.attr("row"));
        }

        // 删除单个排座操作
        cancelCurSeat();
        // 座位互换操作
        exchangeSeats();
    }
}

// 7.初始化选中
function defaultSeats(data) {
    var hasDefaultSeat = false;
    $.each(data.users, function(i, user) {
        if (user.row && user.col) {
            hasDefaultSeat = true;
            // 左侧选中
            $(".member").eq(user.index).addClass("seated-member").attr("memberIdx", user.index);
            // 右侧落座
            $(".seats-row").eq(user.row - 1).children(".seat-li").eq(user.col - 1).children(".seat").addClass("seated-member").text(user.name).attr("memberIdx", user.index);
            // 保存落座信息
            _page.insertedMembersSeats.push({
                row: user.row,
                col: user.col,
                index: user.index
            });
        }
    });
    $.each(data.users1, function(i, user) {
        if (user.row && user.col) {
            hasDefaultSeat = true;
            // 左侧选中
            $(".member").eq(user.index).addClass("seated-member").attr("memberIdx", user.index);
            // 右侧落座
            $(".seats-row").eq(user.row - 1).children(".seat-li").eq(user.col - 1).children(".seat").addClass("seated-member").text(user.name).attr("memberIdx", user.index);
            // 保存落座信息
            _page.insertedMembersSeats.push({
                row: user.row,
                col: user.col,
                index: user.index
            });
        }
    });

    if (!hasDefaultSeat) {
        $(".quickly-cancel").addClass("custome_disabled_btn");
        $(".part-handle").addClass("custome_disabled_btn");
    }
    rightCountNumber();
}

// 8.给左侧成员列表添加拖拽效果
function dragMember(el) {
    var _x; // 拖拽时克隆元素的x坐标
    var $seatsContent = $(".seats-content"), // 落座区信息定义
        seats_x,
        seats_y;

    el.on({
        "mousedown": function(e) {
            var self = $(this);
            if ($(e.target).hasClass("del-icon")) {
                return;
            }
            // 生成当前拖拽的clone member
            self.clone(false).addClass("clone_member").appendTo($("body"));
            _page.curCloneMember.dom = $(".clone_member");
            _x = e.clientX - parseInt(self.css("width"), 10) / 2;
            _page.curCloneMember.dom.css({
                "width": self.css("width"),
                "display": "none",
                "position": "fixed"
            });
            $(document).on({
                "mousemove": function(e) {
                    _page.curCloneMember.dom.css({
                        "left": e.clientX - parseInt(_page.curCloneMember.dom.css("width"), 10) / 2 + "px",
                        "top": e.clientY - parseInt(_page.curCloneMember.dom.css("height"), 10) / 2,
                        "display": "block"
                    });
                },
                "mouseup": function(e) {
                    // 移除clone元素
                    _page.curCloneMember.dom.remove();
                    // 移除document上的事件
                    $(this).off("mousemove");
                    $(this).off("mouseup");

                    // 获取当前mouseup事件时鼠标位置是否在落座区
                    seats_x = $seatsContent.offset().left;
                    seats_y = $seatsContent.offset().top + parseInt($seatsContent.css("height"), 10);
                    if (_page.isHandling) {
                        top.$.alert("请先取消批量操作");
                    } else {
                        if ((e.clientX > seats_x) && (e.clientY < seats_y)) {
                            _page.curCloneMember.index = _page.curCloneMember.dom.attr("memberIdx");
                            _page.curCloneMember.name = _page.curCloneMember.dom.children(".member-name").text();
                            _page.curCloneMember.moved = true;
                            self.addClass("seated-member");

                            // 给落座区绑定当前拖拽过来的左侧人员信息
                            seatMembers($(".seats-box .seat"));

                            // 左侧移除当前元素
                            if (self.parent().hasClass("person")) {
                                leftCountNumber($(".members-list-title .counts"), "delete");
                            } else {
                                leftCountNumber($(".units-list-title .counts"), "delete");
                            }
                            self.remove();

                        }
                    }
                }
            });
        }
    });
}

// 9.给右侧座位添加落座信息
function seatMembers(el) {
    el.on("mouseenter", function() {
        if (_page.curCloneMember.moved) {
            // 拖拽动作结束
            _page.curCloneMember.moved = false;
            // 开启一键清空，批量操作
            $(".quickly-cancel").removeClass("custome_disabled_btn");
            $(".part-handle").removeClass("custome_disabled_btn");
            // 判断当前座位上是否有人
            if ($(this).attr("memberIdx")) {
                for (var i = 0; i < $(".member.seated-member").length; i++) {
                    if ($(".member.seated-member").eq(i).attr("memberIdx") === _page.curCloneMember.index) {
                        $(".member.seated-member").eq(i).removeClass("seated-member");
                        return;
                    }
                }
            } else {
                // 安插本次座位上的人员
                $(this).text(_page.curCloneMember.name).attr("memberIdx", _page.curCloneMember.index).addClass("seated-member");
                // 记录本次人员的落座信息
                _page.insertedMembersSeats.push({
                    row: $(this).attr("row"),
                    col: $(this).attr("col"),
                    index: _page.curCloneMember.index
                });
                $(this).parent().attr("title",_page.curCloneMember.name);
                $(this).next(".icon-box").children().addClass("show");
            }
            rightCountNumber();
        }
    });
}

// 10.一键入座
function quicklySeatMembers(data) {
    quicklyCancel();

    var $members = $(".members-list .member"),
        $units = $(".units-list .member"),
        $seats = $(".seats-box .seat");
    $(".quickly-seat").on("click", function(e) {
        e.stopPropagation();
        if (_page.isHandling) {
            return;
        }

        for (var i = 0; i < $members.length; i++) {
            if ($members.eq(i).children().length) {
                $members.eq(i).addClass("seated-member");
            }
        }
        for (var j = 0; j < $units.length; j++) {
            if ($units.eq(j).children().length) {
                $units.eq(j).addClass("seated-member");
            }
        }
        // 清空当前排座
        clearSeats();
        $.each(data.users, function(idx, row) {
            $seats.eq(idx).text(row.name).addClass("seated-member").attr("memberIdx", row.index);
            _page.insertedMembersSeats.push({
                row: $seats.eq(idx).attr("row"),
                col: $seats.eq(idx).attr("col"),
                index: row.index
            });
        });

        var index_start = data.users.length
        $.each(data.users1, function(idx, row) {
            var _index = index_start + idx
            $seats.eq(_index).text(row.name).addClass("seated-member").attr("memberIdx", row.index);
            _page.insertedMembersSeats.push({
                row: $seats.eq(idx).attr("row"),
                col: $seats.eq(idx).attr("col"),
                index: row.index
            });
        });
        //zhou
        $(".seats-box .seated-member").next(".icon-box").children().addClass("show");

        // 开启一键清空，批量操作
        $(".quickly-cancel").removeClass("custome_disabled_btn");
        $(".part-handle").removeClass("custome_disabled_btn");
        rightCountNumber();

        // 清空左侧
        var $membersList = $(".members-list");
        var $unitsList = $(".units-list");
        $membersList.children().remove();
        $unitsList.children().remove();
        // 左侧计数清空
        $(".members-list-title .counts").text(0);
        $(".units-list-title .counts").text(0);
    });
}

// 11.一键清空
function quicklyCancel() {
    $(".quickly-cancel").on("click", function(e) {
        e.stopPropagation();
        if ($(this).hasClass("custome_disabled_btn")) {
            return;
        }

        // 清空左侧人员选中
        clearMembers();
        // 清空当前排座
        clearSeats();
        // 禁用一键清空，批量操作
        $(this).addClass("custome_disabled_btn");
        $(".part-handle").addClass("custome_disabled_btn").text("批量操作");
        _page.isHandling = false;
        // 开启一键入座
        $(".quickly-seat").removeClass("custome_disabled_btn");
        rightCountNumber();

        // 左侧计数清空
        $(".members-list-title .counts").text(0);
        $(".units-list-title .counts").text(0);
        // 左侧人员和计数重新渲染
        createMembers(_page.resData);
    });
}

// 12.批量操作
function partCancel() {
    $(".part-handle").on("click", function(e) {
        e.stopPropagation();
        if ($(this).hasClass("custome_disabled_btn")) {
            return;
        }

        // 批量操作
        if (!_page.isHandling) {
            _page.isHandling = true;
            // 禁用一键入座
            $(".quickly-seat").addClass("custome_disabled_btn");
            $(this).text("取消批量操作");
            // 获取当前已入座的座位，添加样式以及删除的dom
            $(".seats-box .seated-member").addClass("is-part-handleing").siblings(".icon-box").children().addClass("show");
        }
        // 取消批量操作
        else {
            _page.isHandling = false;
            // 开启一键入座
            $(".quickly-seat").removeClass("custome_disabled_btn");
            $(this).text("批量操作");
            $(".is-part-handleing").removeClass("is-part-handleing").siblings(".icon-box").children().removeClass("show");
        }
    });
}

// 13.批量删除排座
function cancelCurSeat() {
    var memberIdx;

    $(".right-seats .del-icon").on("click", function(e) {
        e.stopPropagation();
        memberIdx = $(this).parent().siblings(".seat").attr("memberIdx");
        // 清除左侧人员列表选中
        clearMembers(memberIdx);
        // 清除当前座位选中
        clearSeats($(this).parent().siblings(".seat"));
        // 清除_page.insertedMembersSeats中当前项
        for (var i = 0; i < _page.insertedMembersSeats.length; i++) {
            if (_page.insertedMembersSeats[i].index === memberIdx) {
                _page.insertedMembersSeats.splice(i, 1);
                break;
            }
        }

        if (!_page.insertedMembersSeats.length) {
            // 开启一键入座
            $(".quickly-seat").removeClass("custome_disabled_btn");
            // 禁用一键清空，批量操作
            $(".quickly-cancel").addClass("custome_disabled_btn");
            $(".part-handle").addClass("custome_disabled_btn").text("批量操作");
            _page.isHandling = false;
        }
        rightCountNumber();
        // 把当前人员在左侧展示出来
        var users = _page.resData.users.filter(function(user) {
            return _page.insertedMembersSeats.every(function(item, idx) {
                return user.index !== item.index
            })
        });
        var users1 = _page.resData.users1.filter(function(user) {
            return _page.insertedMembersSeats.every(function(item, idx) {
                return user.index !== item.index
            })
        });
        createMembers({
            users: users,
            users1: users1
        })
    });
}

// 14.座位互换操作
function exchangeSeats() {
    var _x, // 拖拽时克隆元素的x坐标
        _y; // 拖拽时克隆元素的y坐标
    var $seatsContent = $(".seats-content"), // 落座区信息定义
        seats_x,
        seats_y;

    $(".move-icon").on({
        "mousedown": function(e) {
            var self = $(this).parents(".seat-li");

            // 生成当前拖拽的clone member
            self.clone(false).addClass("clone_seat").appendTo($(".right-seats"));
            _page.curCloneSeat.dom = $(".clone_seat");
            _x = parseInt(self.css("width"), 10) / 2;
            _y = parseInt(self.css("height"), 10) / 2;
            _page.curCloneSeat.dom.css({
                "width": self.css("width"),
                "display": "none",
                "position": "fixed"
            });
            $(document).on({
                "mousemove": function(e) {
                    _page.curCloneSeat.dom.css({
                        "left": e.clientX - _x + "px",
                        "top": e.clientY - _y / 2,
                        "display": "block"
                    });
                },
                "mouseup": function(e) {
                    // 移除clone元素
                    _page.curCloneSeat.dom.remove();
                    // 移除document上的事件
                    $(this).off("mousemove");
                    $(this).off("mouseup");

                    // 获取当前mouseup事件时鼠标位置是否在落座区
                    seats_x = $seatsContent.offset().left;
                    seats_y = $seatsContent.offset().top + parseInt($seatsContent.css("height"), 10);
                    if ((e.clientX > seats_x) && (e.clientY < seats_y)) {
                        var curSeat = _page.curCloneSeat.dom.children(".seated-member");
                        _page.curCloneSeat = {
                            dom: $(".clone-obj"),
                            index: curSeat.attr("memberIdx"),
                            name: curSeat.text(),
                            moved: true
                        };

                        resetSeats($(".seats-box .seat"));
                    }
                }
            });
        }
    });
}

// 15.清除左侧人员选中
function clearMembers(memberIdx) {
    var $members = $(".members-list .member"),
        $members_len = $members.length;
    var $units = $(".units-list .member"),
        $units_len = $units.length;

    // memberIdx用来标记是否是批量操作
    if (memberIdx) {
        for (var i = 0; i < $members_len; i++) {
            if (memberIdx === $members.eq(i).attr("memberIdx")) {
                $members.eq(i).removeClass("seated-member");
                return;
            }
        }
        for (var i = 0; i < $units_len; i++) {
            if (memberIdx === $units.eq(i).attr("memberIdx")) {
                $units.eq(i).removeClass("seated-member");
                return;
            }
        }
    }
    // 一键清空
    else {
        $members.removeClass("seated-member");
        $units.removeClass("seated-member");
    }
}

// 16.清除当前排座
function clearSeats(curSeat) {
    if (_page.insertedMembersSeats.length) {
        // curSeat用来标记是否是批量操作
        if (curSeat) {
            curSeat.text("").removeClass("seated-member is-part-handleing").attr("memberIdx", null).siblings(".icon-box").children().removeClass("show");
        } else {
            $(".seats-box .seat").text("").removeClass("seated-member is-part-handleing").attr("memberIdx", null).siblings(".icon-box").children().removeClass("show");
            _page.insertedMembersSeats = [];
        }
    }
    // rightCountNumber();
}

// 17.座位互换时重整落座信息
function resetSeats(el) {
    el.on("mouseenter", function() {
        if (_page.curCloneSeat.moved) {
            // 拖拽动作结束
            _page.curCloneSeat.moved = false;

            var $seatedSeat = $(".seat.seated-member"),
                seatedSeat_len = $seatedSeat.length,
                curSeatMemberIdx = $(this).attr("memberIdx");

            if (_page.insertedMembersSeats.length) {
                // 判断当前座位上是否有人，座位信息互换
                if (curSeatMemberIdx) {
                    for (var i = 0; i < seatedSeat_len; i++) {
                        if ($seatedSeat.eq(i).attr("memberIdx") === _page.curCloneSeat.index) {
                            $seatedSeat.eq(i).text($(this).text()).attr("memberIdx", $(this).attr("memberIdx"));
                            break;
                        }
                    }
                    $(this).text(_page.curCloneSeat.name).attr("memberIdx", _page.curCloneSeat.index);

                    // 修改两者之间的位置信息
                    resetInsertedMembersSeats();
                } else {
                    // 安插本次座位上的人员
                    $(this).text(_page.curCloneSeat.name).attr("memberIdx", _page.curCloneSeat.index).addClass("seated-member is-part-handleing").siblings(".icon-box").children().addClass("show");

                    for (var o = 0; o < _page.insertedMembersSeats.length; o++) {
                        var item = _page.insertedMembersSeats[o];
                        if (_page.curCloneSeat.index === item.index) {
                            // 清空上次座位上的text()和样式
                            $(".seats-box .seats-row").eq(item.row - 1).find(".seat").eq(item.col - 1).text("").removeClass("seated-member is-part-handleing").attr("memberIdx", null).siblings(".icon-box").children().removeClass("show");
                            // 修改本次人员的落座信息
                            _page.insertedMembersSeats[o] = {
                                row: $(this).attr("row"),
                                col: $(this).attr("col"),
                                index: _page.curCloneSeat.index
                            };
                        }
                    }
                }
            }
        }
    });
}

// 18.重整_page.insertedMembersSeats的信息
function resetInsertedMembersSeats() {
    var $seatedMembers = $(".seats-box .seated-member");
    for (var i = 0; i < $seatedMembers.length; i++) {
        for (var j = 0; j < _page.insertedMembersSeats.length; j++) {
            if ($seatedMembers.eq(i).attr("memberIdx") === _page.insertedMembersSeats[j].index) {
                _page.insertedMembersSeats[j].row = $seatedMembers.eq(i).attr("row");
                _page.insertedMembersSeats[j].col = $seatedMembers.eq(i).attr("col");
            }
        }
    }
}

// 19.生成PDF
function loadPDF(source) {
    if (!copyDom) {
        copyDom = source.clone().css({
            "width": "auto",
            "height": "auto",
            "position": "absolute",
            "zIndex": "-100"
        }).appendTo("body");
    }

    html2canvas(copyDom[0], {
        logging: false,
        background: '#FFF',
        useCORS: true,
        allowTaint: true,
        taintTest: false
    }).then(function(canvas) {
        var contentWidth = canvas.width;
        var contentHeight = canvas.height;

        if (copyDom) {
            copyDom.remove();
        }
        //一页pdf显示html页面生成的canvas高度;
        var pageHeight = contentWidth / 595.28 * 841.89;
        //未生成pdf的html页面高度
        var leftHeight = contentHeight;
        //pdf页面偏移
        var position = 0;
        //a4纸的尺寸[595.28,841.89]，html页面生成的canvas在pdf中图片的宽高
        var imgWidth = 595.28;
        var imgHeight = 595.28 / contentWidth * contentHeight;

        var pageData = canvas.toDataURL('image/png', 1.0);
        var pdf = new jsPDF('', 'pt', 'a4');
        //有两个高度需要区分，一个是html页面的实际高度，和生成pdf的页面高度(841.89)
        //当内容未超过pdf一页显示的范围，无需分页
        if (leftHeight < pageHeight) {
            pdf.addImage(pageData, 'JPEG', 20, 0, imgWidth, imgHeight);
        } else {
            while (leftHeight > 0) {
                pdf.addImage(pageData, 'JPEG', 20, position, imgWidth, imgHeight);
                leftHeight -= pageHeight;
                position -= 841.89;
                //避免添加空白页
                if (leftHeight > 0) {
                    pdf.addPage();
                }
            }
        }

        // 将pdf输入为base格式的字符串
        exportData.base64_pdf = pdf.output("datauristring");
        exportData.base64_png = pageData;
        exportData.valid = true;
    })
}

// 20.dialog调用的ok方法
function OK() {
    //zhou
    $(".seats-box .seated-member").next(".icon-box").children().removeClass("show");
    // $(".is-part-handleing").removeClass("is-part-handleing").siblings(".icon-box").children().removeClass("show");
    if (_page.interfaceError) {
        return { valid: false, interfaceError: true }
    }
    if (_page.isHandling) {
        // 请先取消批量操作，todo...
        top.$.alert("请先取消批量操作");
        return { valid: false };
    }

    // 需要的base64是html2canvas的promise协议回调生成，在自定义控件点击保存时必须得等待此回调函数执行完毕再关闭弹框
    if (!exportData.valid) {
        loadPDF($('.seats-box'));
    } else {
        exportData.meetingId = _page.resData.meetingId;
        exportData.meetingRoomId = _page.resData.meetingRoomId;
        exportData.rows = _page.resData.rows;
        exportData.cols = _page.resData.cols;
        exportData.users = [];
        exportData.users1 = [];

        for (var i = 0, seated_len = _page.insertedMembersSeats.length; i < seated_len; i++) {
            for (var j = 0, resData_len = _page.resData.users.length; j < resData_len; j++) {
                // 手动添加的数据不需要返回
                if (_page.resData.users[j].newAdd) {
                    break;
                }
                if (_page.insertedMembersSeats[i].index === _page.resData.users[j].index) {
                    exportData.users.push({
                        name: _page.resData.users[j].name,
                        dep: _page.resData.users[j].dep,
                        row: _page.insertedMembersSeats[i].row,
                        col: _page.insertedMembersSeats[i].col
                    });
                }
            }
            for (var k = 0, resData_len = _page.resData.users1.length; k < resData_len; k++) {
                // 手动添加的数据不需要返回
                if (_page.resData.users1[k].newAdd) {
                    break;
                }
                if (_page.insertedMembersSeats[i].index === _page.resData.users1[k].index) {
                    exportData.users1.push({
                        name: _page.resData.users1[k].name,
                        dep: _page.resData.users1[k].dep,
                        row: _page.insertedMembersSeats[i].row,
                        col: _page.insertedMembersSeats[i].col
                    });
                }
            }
        }
    }

    return {
        valid: true,
        data: exportData
    }
}

// 人员计数
function leftCountNumber(dom, type) {
    var count = parseInt(dom.text(), 10);
    type === "delete" ? --count : ++count
    dom.text(count);
}
// 落座计数
function rightCountNumber() {
    $(".right-seats .counts").text(_page.insertedMembersSeats.length)
}
// 打开弹框
function showDialog() {
    $(".left-list .add-btn").on("click", function() {
        $(".edit-dialog input[type='text']").val("");
        $(".edit-dialog").addClass("show");
    })
}
// 切换弹框内容展示
function checkContent() {
    var radio = $('.edit-dialog input:radio[name="radio"]');
    radio.on("change", function() {
        var checkedVal = $(this).val();
        if (checkedVal === "member") {
            $(".dialog-content .member-item").removeClass("hide").addClass("show");
            $(".dialog-content .unit-item").removeClass("show").addClass("hide");
        } else if (checkedVal === "unit") {
            $(".dialog-content .unit-item").removeClass("hide").addClass("show");
            $(".dialog-content .member-item").removeClass("show").addClass("hide");
        }
    })
}
// 关闭弹框
function closeDialog() {
    var closeBtn = $(".edit-dialog .close-btn"),
        sureBtn = $(".edit-dialog .sure-btn"),
        cancelBtn = $(".edit-dialog .cancel-btn");
    closeBtn.on("click", function() {
        $(".edit-dialog").removeClass("show");
    })
    sureBtn.on("click", function() {
        setDataWhenSure();
        $(".edit-dialog").removeClass("show");
    })
    cancelBtn.on("click", function() {
        $(".edit-dialog").removeClass("show");
    })
}
// 点击确定时获取数据并展示数据
function setDataWhenSure() {
    var checkedVal = $('.edit-dialog input:radio[name="radio"]:checked').val();
    var name = $(".edit-dialog input[class='name']"),
        dep = $(".edit-dialog input[class='dep']");
    if (checkedVal === "member") {
        _page.resData.users.push({
            "name": name.eq(0).val(),
            "dep": dep.val(),
            "col": "",
            "row": "",
            "index": _page.resData.users.length + _page.resData.users1.length + '',
            "newAdd": true
        })
    } else if (checkedVal === "unit") {
        _page.resData.users1.push({
            "name": name.eq(1).val(),
            "dep": "",
            "col": "",
            "row": "",
            "index": _page.resData.users.length + _page.resData.users1.length + '',
            "newAdd": true
        })
    }
    // 生成左侧人员信息
    addMemberOrUnitDom(_page.resData, checkedVal);
    // 一键入座
    quicklySeatMembers(_page.resData);
    // 一键清空
    quicklyCancel();
}

function addMemberOrUnitDom(data, type) {
    $(".left-list .member").off("mousedown");
    var users = _page.resData.users.filter(function(user) {
        return _page.insertedMembersSeats.every(function(item, idx) {
            return user.index !== item.index
        })
    });
    var users1 = _page.resData.users1.filter(function(user) {
        return _page.insertedMembersSeats.every(function(item, idx) {
            return user.index !== item.index
        })
    });
    var idx = users.length - 1,
        ulIdx = parseInt(idx / 3, 10);
    var idx1 = users1.length - 1,
        ulIdx1 = parseInt(idx1 / 3, 10);
    var curIdx = _page.resData.users.length + _page.resData.users1.length - 1;
    if (type === "member") {
        var $membersList = $(".left-list .members-list");
        var row = users[idx];
        if (idx % 3 === 0) {
            $membersList.append(
                '<ul class="members-row person">' +
                '<li class="member" memberIdx="' + curIdx + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="member-dep">' + row.dep + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>' +
                '</ul>'
            );
        } else {
            var $membersRow = $(".members-list .members-row");
            $membersRow.eq(ulIdx).append(
                '<li class="member" memberIdx="' + curIdx + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="member-dep">' + row.dep + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>'
            );
        }
        // 计数
        leftCountNumber($(".members-list-title .counts"));
    } else if (type === "unit") {
        var $unitsList = $(".left-list .units-list");
        var row = users1[idx1];
        if (idx1 % 3 === 0) {
            $unitsList.append(
                '<ul class="members-row unit">' +
                '<li class="member" memberIdx="' + curIdx + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>' +
                '</ul>'
            );
        } else {
            var $unitsRow = $(".units-list .members-row");
            $unitsRow.eq(ulIdx1).append(
                '<li class="member" memberIdx="' + curIdx + '">' +
                '<span class="member-name">' + row.name + '</span>' +
                '<span class="icon-box clear-fix">' +
                '<span class="del-icon"></span>' +
                '</li>'
            );
        }
        // 计数
        leftCountNumber($(".units-list-title .counts"));
    }
    dragMember($(".left-list .member"));
}

// 开启删除模式
function openDeleteBtn() {
    $(".left-list .delete-btn").on("click", function(e) {
        e.stopPropagation();
        var members = $(".left-list .member");
        // 删除
        if (!_page.isDelete) {
            _page.isDelete = true;
            $(".icon-box", members).children().addClass("show");
            $(this).text("取消删除");
        }
        // 取消删除
        else {
            _page.isDelete = false;
            $(this).text("删除");
            $(".left-list .icon-box").children().removeClass("show");
        }
        deleteMemberOrUnit()
    })
}

// 删除左侧当前项
function deleteMemberOrUnit() {
    var delIcon = $(".left-list .del-icon");
    delIcon.on("click", function(e) {
        e.stopPropagation();
        var item = $(this).closest(".member");
        if (item.hasClass("seated-member")) {
            top.$.alert("当前状态不可删除！");
            return;
        }
        var index = item.index();
        var type = item.closest("div").hasClass("members-list") ? "member" : "unit";
        if (type === "member") {
            _page.resData.users.splice(index, 1);
            leftCountNumber($(".members-list-title .counts"), "delete");
        } else if (type === "unit") {
            _page.resData.users1.splice(index, 1);
            leftCountNumber($(".units-list-title .counts"), "delete");
        }
        item.empty();
    })
}

// 2020/3/8 新增搜索功能
function searchFn() {
    var $searchIpt = $(".search-ipt"),
        $meetingSeatSearch = $("#meetingSeatSearch");
    $meetingSeatSearch.on("input", function() {
        var users = _page.resData.users.filter(function(user) {
            return _page.insertedMembersSeats.every(function(item) {
                return user.index !== item.index
            })
        });
        var keyword = $searchIpt.val(),
            result = [];

        users.forEach(function(item) {
            if (item.name.indexOf(keyword) !== -1 || item.dep.indexOf(keyword) !== -1) {
                result.push(item)
            }
        })
        var data = {
            users: result,
            users1: _page.resData.users1
        };
        createMembers(data);
    });
}