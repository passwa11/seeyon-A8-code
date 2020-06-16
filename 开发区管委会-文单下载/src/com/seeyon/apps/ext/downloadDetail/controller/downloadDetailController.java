package com.seeyon.apps.ext.downloadDetail.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerFactory;

import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.document.impl.DocumentFactoryImpl;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class downloadDetailController extends BaseController {

    private DocumentFactory df = new DocumentFactoryImpl();
    private TransformerFactory tFactory = TransformerFactory.newInstance();

    public ModelAndView downloadfile(HttpServletRequest request, HttpServletResponse response) {
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
            String opinion = getJsString(opinionSet);
            htmlContent = df.exportOfflineEdocModel(Long.parseLong(affairId));
            String msg = htmlContent[1] + " " + opinion;

            OutputStream fos = null;
            String filename = toHandleSpecial(subject)+".html";
            try {
                response.setContentType("application/octet-stream;charset=utf-8");
                response.setHeader("Content-disposition", "attachment;filename=" + new String(filename.getBytes("GBK"), "iso-8859-1"));
                //创建临时文件
//                File file=File.createTempFile("",".html");
                fos = response.getOutputStream();
                fos.write(msg.getBytes());
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {

        }
        return null;
    }

    public String toHandleSpecial(String info) {
        String[] arr = {"\n", "\t", "\b"};
        for (int i = 0; i < arr.length; i++) {
            if (info.contains(arr[i])) {
                info.replaceAll(arr[i], "");
            }
        }
        return info;
    }


    public String getJsString(ResultSet set) throws SQLException {
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
