<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2018/1/29
  Time: 11:07
  To change this template use File | Settings | File Templates.
--%>
<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<html class="over_hidden h100b">
<head>
    <title>${ctp:i18n("cap.ctrl.barcode.set.title")}</title>
    <link rel="stylesheet" href="${path}/common/cap4/businessRelation/css/relation.css${ctp:resSuffix()}"/>
    <script type="text/javascript"
            src="${path}/apps_res/cap/customCtrlResources/capctrlbarcode/js/barcodeSet.js${ctp:resSuffix()}"></script>
    <style>
        #contentSet {
            text-align: center;
            width: 200px;
            color: #1f85ec;
            border: 1px solid #9bcdff;
        }
    </style>

</head>
<%--zhou:start--%>
<%
    String fileId = request.getParameter("fileId");
    String filePath = request.getParameter("filePath");
    String imgFileId = "";
    String imgPath = "";
    if (fileId != null && com.seeyon.ctp.util.Strings.isDigits(fileId)) {
        imgFileId = fileId;
    }
    if (filePath != null) {
        imgPath = filePath.replace("/seeyon", "");
    }
%>
<c:set var="imgFileId" value="<%= imgFileId %>"/>
<c:set var="imgPath" value="<%= imgPath %>"/>
<%--zhou:end--%>
<body class="over_hidden h100b font_size12" style="overflow: auto">
<div class="margin_l_20 margin_t_20">
    <div>
        <div style="display: inline-block;width: 100px;text-align: right;line-height: 26px;font-weight: bold;margin-right:20px">
            <span>${ctp:i18n("cap.ctrl.barcode.barcodetype")}</span>
        </div>
        <div style="display: inline-block;">
            <label for="text" class="margin_r_10 hand"><input type="radio" class="radio_com" id="text" name="type"
                                                              checked="checked"
                                                              value="text">${ctp:i18n('form.input.varchar.label')}
            </label>
            <label for="url" class="margin_r_10 hand"><input class="radio_com" type="radio" id="url" name="type"
                                                             value="url">URL</label>
        </div>
        <%--    zhou：    背景图片--%>
        <input id="myfile" type="hidden" value=""/>
        <table border="0" cellSpacing="0" cellPadding="0" style="margin: 50px auto 0 auto;">
            <tr>
                <td>
                    <div style="display: inline-block;">
                        <div>
                            <span>上传背景图片:</span>
                        </div>
                    </div>
                </td>
                <td>
                    <div style="float: right">
                        <input id="selectImgBtn" type="button" class="button-default-2"
                               style="width:140px;height:30px;font-size: 15px;background: #1f85ec;border: 1px solid #1f85ec;color: #fff;border-radius: 5px;cursor: pointer;font-size: 12px;"
                               value="${ctp:i18n('portal.button.upload') }"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <div style="width: 300px;height: 200px;border: 2px solid #ddd;background: url(/seeyon/portal/images/uploadImg/grid.png);">
                        <img width="200" height="300" id="originalImg"
                             src="${path}/fileUpload.do?method=showRTE&fileId=${ctp:toHTML(imgFileId) }&type=image${ctp:csrfSuffix()}"/>
                    </div>
                </td>

            </tr>
        </table>
    </div>
    <div class="margin_t_20" style="margin-left: 80px;">
        <a class="common_button margin_l_30" href="javascript:void(0)" id="contentSet">
            <span class="iconfont cap-icon-bitian span_bitianicon"
                  style="color: red;"></span>${ctp:i18n('cap.ctrl.barcode.content.set.title')}
            <em id="icon" class="ico16 processed_16 margin_l_5" style="display: none"></em></a>
    </div>
</div>
</body>
<script>
    //zhou:添加如下内容
    // 图片id
    var callBackFileId = "${ctp:escapeJavascript(imgFileId)}";
    var callBackFilePath = "${ctp:escapeJavascript(imgPath)}";

    $(document).ready(function () {
        $("#selectImgBtn").click(function () {
            dymcCreateFileUpload("myfile", 1, "jpg,jpeg,png,gif,bmp", 1, false, "uploadCallBack", "poi", true, true, null, false, true, 512000 * 2);
            //上传图片
            insertAttachmentPoi("poi");
        });
    });

    function uploadCallBack(attachment) {
        if (attachment.instance[0]) {
            var fileId = attachment.instance[0].fileUrl;
            $("#originalImg").attr("src", "${path}/fileUpload.do?method=showRTE&fileId=" + fileId + "&type=image${ctp:csrfSuffix()}");
            callBackFileId = fileId;
            callBackFilePath = "/fileUpload.do?method=showRTE&fileId=" + fileId + "&type=image";
        }
        return;
    }
</script>
</html>

