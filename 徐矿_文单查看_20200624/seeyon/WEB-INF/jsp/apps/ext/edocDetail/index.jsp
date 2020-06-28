<%@ page contentType="text/html; charset=UTF-8" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <style type="text/css">


        .tr-text {
            word-wrap: break-word;
            word-break: break-all;
        }

        .tr-text-float {
            word-wrap: break-word;
            word-break: break-all;
            float: left;
        }

        label {
            font-size: medium;
            color: #339933;
            font-family: 黑体;
            float: left;
        }

        table {
            margin: auto;
            margin-top: 0px;
            width: 700px;
            table-layout: fixed;
        }

        td {
            margin-top: 6px;
            height: 33px;
        }
    </style>
</head>
<body>
<div style="text-align: center;margin-top: 20px;">
    <table>
        <tr>
            <td colspan="4">
                <span style="color: #008000;font-size: 30px;">徐州矿务集团党政办公室请示办文单</span>
            </td>
        </tr>
        <tr style="color: #ff0000">
            <td colspan="4">
                <span style="color: #008000;font-size: 30px;">请示办文单</span>
            </td>
        </tr>
    </table>
    <table frame="below" style="margin-top: 40px;">
        <tr class="tr-height">
            <td width="129"><label>编号:</label></td>
            <td width="216" class="tr-text"><span></span></td>
            <td width="129"><label>日期:</label></td>
            <td width="216" class="tr-text"><span></span></td>
        </tr>
    </table>
    <table frame="below">
        <tr class="tr-height">
            <td width="129"><label>来文单位:</label></td>
            <td width="216" class="tr-text"><span></span></td>
            <td width="129"><label>来文号:</label></td>
            <td width="216" class="tr-text">
                <span>

                </span>
            </td>
        </tr>
    </table>
</div>
<div style="margin-bottom: 20px;">
    <table frame="below">
        <tr>
            <td><label>文件标题:</label></td>
        </tr>
        <tr style="height: 35px;">
            <td class="tr-text-float">
                <div style="float: left;">
                </div>
            </td>
        </tr>
    </table>
    <table frame="below">
        <tr>
            <td><label>拟办意见:</label></td>
        </tr>
        <tr style="height: 100px;">
            <td class="tr-text-float">
                <div style="float: left;">
                </div>
            </td>
        </tr>
    </table>
    <table frame="below">
        <tr>
            <td><label>批示意见:</label></td>
        </tr>
        <tr style="height: 150px;">
            <td class="tr-text-float">
                <div style="float: left;">
                </div>
            </td>
        </tr>
    </table>
    <table frame="below">
        <tr>
            <td><label>办理意见:</label></td>
        </tr>
        <tr style="height: 150px;">
            <td class="tr-text-float">
                <div style="float: left;">
                </div>
            </td>
        </tr>
    </table>
    <table frame="below">
        <tr style="height: 50px;">
            <td width="129"><label>备注:</label></td>
            <td class="tr-text-float">
                <div style="float: right;">
                </div>
            </td>
        </tr>
    </table>
</div>

</body>
</html>
