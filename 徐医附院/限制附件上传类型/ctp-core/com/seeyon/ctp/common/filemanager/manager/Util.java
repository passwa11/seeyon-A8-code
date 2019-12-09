/**
 * 
 */
package com.seeyon.ctp.common.filemanager.manager;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;

/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2007-6-12
 */
public class Util {
    public static boolean jinge2StandardOffice(String oldPathName, String newPathName) {
        DBstep.iMsgServer2000 msgObj = new DBstep.iMsgServer2000();
        msgObj.MsgFileLoad(oldPathName);
        msgObj.MsgFileBody(msgObj.ToDocument(msgObj.MsgFileBody()));
        return msgObj.MsgFileSave(newPathName);
    }

    public static String AttachmentToHtml(Attachment att, boolean isShowLink, boolean isShowDelete) {
    	boolean showlink = isShowLink;
        StringBuilder sb = new StringBuilder();
        String contextPath = WebUtil.getRequest().getContextPath();
        sb.append("<div id='attachmentDiv_").append(att.getFileUrl())
                .append("' style='float: left;height: 16px; line-height: 14px;' noWrap>");
        sb.append("<img src='").append(contextPath).append("/common/images/attachmentICON/").append(att.getIcon())
                .append("' border='0' height='16' width='16' align='absmiddle' style='margin-right: 3px;'>");
        if (showlink && att.getType() == 0) {//downloadURL
            try {
                sb.append("<a href='").append(contextPath).append("/fileDownload.do?method=download&fileId=")
                        .append(att.getFileUrl()).append("&v=").append(att.getV()).append("&createDate=")
                        .append(Datetimes.formatDate(att.getCreatedate())).append("&filename=")
                        .append(URLEncoder.encode(Strings.escapeJavascript(att.getFilename()), "UTF-8"))
                        .append("' title='").append(Strings.toHTML(att.getFilename()))
                        .append("' target='downloadFileFrame' style='font-size:12px'>");
            } catch (Exception e) {
            }
        }
        if (att.getType() == 2 && att.getDescription() != null) { //文档
            String click = "";
            if ("collaboration".equals(att.getMimeType()) || "edoc".equals(att.getMimeType())) {
                click = "openDetail('" + Strings.toHTML(att.getFilename()) + "', 'from=Pending&affairId="
                        + att.getDescription() + "&openFrom=glwd')";
            } else if ("km".equals(att.getMimeType())) {
                click = "openDetailURL(docURL+" + "'?method=docOpenIframeOnlyId&openFrom=glwd&docResId="
                        + att.getDescription() + "')";
            }

            sb.append("<a class=\"like-a\" onclick=\"").append(click).append("\" title=\"")
                    .append(Strings.toHTML(att.getFilename())).append("\" style='font-size:12px'>");
            showlink = true;
        }

        String showFileName = "";
        if (att.getFilename().length() > 12) {
            showFileName = Strings.toHTML(att.getFilename().substring(0, 8)) + "...";
        } else {
            showFileName = Strings.toHTML(att.getFilename());
        }

        sb.append(showFileName);

        if (att.getSize() > 0 && att.getType() == 0) {
            sb.append("(").append(Strings.formatFileSize(att.getSize(), true)).append(")");
        }

        //显示链接
        if (showlink) {
            sb.append("</a>");
        }

        //显示删除
        if (isShowDelete) {
            sb.append("<img src='")
                    .append(contextPath)
                    .append("/common/images/attachmentICON/delete.gif' onclick='deleteAttachment(\"")
                    .append(att.getFileUrl())
                    .append("\")' class='cursor-hand' title='")
                    .append(ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources",
                            "common.toolbar.delete.label")).append("' height='11' align='absmiddle'>");
        }

        sb.append("&nbsp;</div>");

        return sb.toString();
    }

    public static String AttachmentToHtmlWithShowAllFileName(Attachment att, boolean isShowLink, boolean isShowDelete) {
        StringBuilder sb = new StringBuilder();
        String contextPath = "";
        HttpServletRequest request = WebUtil.getRequest();
        if (request != null)
            contextPath = request.getContextPath();
        sb.append("<div id='attachmentDiv_").append(att.getFileUrl())
                .append("' style='float: left;line-height: 14px;' noWrap>");
        sb.append("<img src='").append(contextPath).append("/common/images/attachmentICON/").append(att.getIcon())
                .append("' border='0' height='16' width='16' align='absmiddle' style='margin-right: 3px;'>");
        if (isShowLink && att.getType() == 0) {//downloadURL
            try {
                sb.append("<a href='").append(contextPath).append("/fileDownload.do?method=download&fileId=")
                        .append(att.getFileUrl()).append("&v=").append(att.getV()).append("&createDate=")
                        .append(Datetimes.formatDate(att.getCreatedate())).append("&filename=")
                        .append(URLEncoder.encode(Strings.escapeJavascript(att.getFilename()), "UTF-8"))
                        .append("' title='").append(Strings.toHTML(att.getFilename()))
                        .append("' target='downloadFileFrame' style='font-size:12px'>");
            } catch (Exception e) {
            }
        }
        if (att.getType() == 2 && att.getDescription() != null) { //文档
            String click = "";
            if ("collaboration".equals(att.getMimeType())) {
                click = "openDetailURL(colURL+" + "'?method=summary&affairId="
                        + att.getDescription() + "&baseObjectId=" + att.getReference() 
                        + "&baseApp=" + att.getCategory() + "&openFrom=glwd')";
            } else if("edoc".equals(att.getMimeType())){
                click = "openDetailURL(edocDetailURL+" + "'?method=detailIFrame&affairId="
                        + att.getDescription() + "&baseObjectId=" + att.getReference() 
                        + "&baseApp=" + att.getCategory() + "&openFrom=glwd&from=Done&isQuote=true')";
            } else if("meeting".equals(att.getMimeType())){
                click = "openDetailURL(mtMeetingUrl+" + "'?method=myDetailFrame&id="
                        + att.getDescription() + "&baseObjectId=" + att.getReference()
                        + "&baseApp=" + att.getCategory() + "&openFrom=glwd&isQuote=true&state=10')";
            } else if ("km".equals(att.getMimeType())) {
                click = "openDetailURL(docURL+" + "'?method=docOpenIframeOnlyId&openFrom=glwd&docResId="
                        + att.getDescription() + "&baseObjectId=" + att.getReference() + "&baseApp="
                        + att.getCategory() + "')";
            }

            sb.append("<a class=\"like-a\" onclick=\"").append(click).append("\" title=\"")
                    .append(Strings.toHTML(att.getFilename())).append("\" style='font-size:12px'>");
            isShowLink = true;
        }

        String showFileName = "";
        //不管文件名有多长，全部显示。
        showFileName = Strings.toHTML(att.getFilename());
        /*		if(att.getFilename().length() > 12){
        			showFileName = Strings.toHTML(att.getFilename().substring(0, 8)) + "...";
        		}
        		else{
        			showFileName = Strings.toHTML(att.getFilename());
        		}*/

        sb.append(showFileName);

        if (att.getSize() > 0 && att.getType() == 0) {
            sb.append("(").append(Strings.formatFileSize(att.getSize(), true)).append(")");
        }

        //显示链接
        if (isShowLink) {
            sb.append("</a>");
        }

        //显示删除
        if (isShowDelete) {
            sb.append("<img src='")
                    .append(contextPath)
                    .append("/common/images/attachmentICON/delete.gif' onclick='deleteAttachment(\"")
                    .append(att.getFileUrl())
                    .append("\")' class='cursor-hand' title='")
                    .append(ResourceBundleUtil.getString("com.seeyon.v3x.common.resources.i18n.SeeyonCommonResources",
                            "common.toolbar.delete.label")).append("' height='11' align='absmiddle'>");
        }

        sb.append("&nbsp;</div>");

        return sb.toString();
    }

    /**
     * 读取所有附件到列表，根据回复意见ID分类
     * @param atts
     * @return
     */
    public static Hashtable<Long, List<Attachment>> sortBySubreference(List<Attachment> atts) {
        List<Attachment> tempList = null;
        Hashtable<Long, List<Attachment>> hs = new Hashtable<Long, List<Attachment>>();
        if (atts == null) {
            return hs;
        }
        for (Attachment att : atts) {
            tempList = hs.get(att.getSubReference());
            if (tempList == null) {
                tempList = new ArrayList<Attachment>();
            }
            tempList.add(att);
            hs.put(att.getSubReference(), tempList);
        }
        return hs;
    }
}
