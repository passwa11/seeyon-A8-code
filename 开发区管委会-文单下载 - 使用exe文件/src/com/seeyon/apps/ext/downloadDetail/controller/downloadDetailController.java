package com.seeyon.apps.ext.downloadDetail.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.downloadDetail.util.HtmlToPDFUtils;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;

import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.document.impl.DocumentFactoryImpl;
import org.apache.commons.fileupload.FileItem;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class downloadDetailController extends BaseController {

    private DocumentFactory df = new DocumentFactoryImpl();
    private TransformerFactory tFactory = TransformerFactory.newInstance();

    public final String FONT = "NotoSansCJKsc-Regular.otf";

    public String toHandleSpecial(String info) {
        String[] arr = {"\n", "\t", "\b"};
        for (int i = 0; i < arr.length; i++) {
            if (info.contains(arr[i])) {
                info.replaceAll(arr[i], "");
            }
        }
        return info;
    }


    public ModelAndView downLoadHtmltoPdf(HttpServletRequest request, HttpServletResponse response) {
        String affairId = request.getParameter("affairId");
        String summaryId = request.getParameter("summaryId");
        String subject = request.getParameter("subject");
        String[] htmlContent = null;
        String opinionSql = "select case attribute when 2 then '【'||'同意'||'】' when  3 then '【'||'不同意'||'】' else '' end attribute,policy,department_name,create_time,content,(select name from org_member where id= s.create_user_id) create_user_id from (select * from edoc_opinion where edoc_id=?) s";
        try {
            Connection connection = JDBCAgent.getRawConnection();
            ResultSet opinionSet = null;
            PreparedStatement opinionPs = null;

            opinionPs = connection.prepareStatement(opinionSql);
            opinionPs.setString(1, summaryId);
            opinionSet = opinionPs.executeQuery();
            htmlContent = df.exportOfflineEdocModel(Long.parseLong(affairId));
            String head = "<!DOCTYPE html><head><meta charset=\"utf-8\"></head>";
//            String head = "<!DOCTYPE html><head><meta ></head>";
            String msg = handlerToString(opinionSet, head + htmlContent[1]);
            OutputStream fos = null;
            String filename = toHandleSpecial(subject);
            InputStream in = null;
//            byte[] b=new byte[1024];
            try {

                response.setContentType("application/octet-stream;charset=gb2312");
//                response.setContentType("application/octet-stream;charset=gbk");
                response.setHeader("Content-disposition", "attachment;filename=" + new String(filename.getBytes("gbk"), "iso8859-1")+".pdf");
                fos = response.getOutputStream();
//                String i=msg.replaceAll("宋体","方正小标宋简体");
                FileItem fileItem = HtmlToPDFUtils.build().convertPDFFromText(msg, filename);
//                in = fileItem.getInputStream();
//                while (in.read(b)!=-1){
//                    fos.write(b);
//                }
                fos.write(fileItem.get());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(null !=in){
                        in.close();
                    }
                    if (null != fos) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String handlerToString(ResultSet set, String htmlContent) throws SQLException {
//        com.itextpdf.licensekey.LicenseKey
        htmlContent = htmlContent.replaceAll("xdRichTextBox\\{", "xdRichTextBox{color:#000000;");
        while (set.next()) {
            String attribute = set.getString("attribute");
            String content = set.getString("content");
            String deptName = set.getString("department_name");
            String userName = set.getString("create_user_id");
            String createTime = set.getString("create_time").substring(0, set.getString("create_time").lastIndexOf(":"));
            String val = "<span style='color:#000000'>" + attribute + "  " + content + "  " + deptName + "  " + userName + "  " + createTime + "</span>";
            //切割符
            String splitStr = "id='" + set.getString("policy") + "'";
            String[] arr = htmlContent.split(splitStr);
            String arr2 = arr[1].substring(0, arr[1].indexOf(">") + 1) + val;
            String result = arr[0] + splitStr + arr2 + arr[1].substring(arr[1].indexOf(">") + 1);
            htmlContent = result;
        }
        return htmlContent;
    }

    public String getJsStrings(ResultSet set) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("<script type=\"text/javascript\">");
        while (set.next()) {
            String attribute = set.getString("attribute");
            String content = set.getString("content");
            String deptName = set.getString("department_name");
            String userName = set.getString("create_user_id");
            String createTime = set.getString("create_time").substring(0, set.getString("create_time").lastIndexOf(":"));
            String val = attribute + "  " + content + "  " + deptName + "  " + userName + "  " + createTime;
            sb.append("document.getElementById(\"" + set.getString("policy") + "\").innerHTML =\"" + val + "\";");

        }
        sb.append("</script>");
        return sb.toString();
    }


}
