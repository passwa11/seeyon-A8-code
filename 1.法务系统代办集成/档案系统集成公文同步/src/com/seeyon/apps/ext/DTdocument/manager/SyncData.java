//package com.seeyon.apps.ext.DTdocument.manager;
//
//import com.seeyon.apps.ext.DTdocument.util.DbConnUtil;
//import com.seeyon.v3x.services.document.DocumentFactory;
//import com.seeyon.v3x.services.document.impl.DocumentManager;
//
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.OutputStreamWriter;
//import java.io.StringReader;
//import java.sql.CallableStatement;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
///**
// * Created by Administrator on 2019-9-16.
// */
//public class SyncData {
//
////    private static DocumentFactory documentFactory;
////    /**
////     * 同步公文
////     */
////    public void syncSummary() {
////        Connection connection = DbConnUtil.getInstance().getConnection();
////        Statement statement = null;
////        ResultSet rs = null;
////        try {
////            CallableStatement edoc_key = connection.prepareCall("{call pro_test(?)}");
////            edoc_key.setInt(1, 1);
////            edoc_key.execute();
////            CallableStatement edoc_record = connection.prepareCall("{call pro_test(?)}");
////            edoc_record.setInt(1, 2);
////            edoc_record.execute();
////            CallableStatement edoc_content = connection.prepareCall("{call pro_test(?)}");
////            edoc_content.setInt(1, 3);
////            edoc_content.execute();
////            CallableStatement edoc_attach = connection.prepareCall("{call pro_test(?)}");
////            edoc_attach.setInt(1, 4);
////            edoc_attach.execute();
//////            正式
////            String sql = "select a.id as id, a.subject as subject, substr(to_char(a.create_time, 'yyyy-mm-dd'), 0, 4) year,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 6, 2) month,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 9, 2) day from edoc_summary a, edoc_body b where a.has_archive = 1 and a.id = b.edoc_id and a.id in (select id from TEMP_NUMBER1)";
//////            测试
//////            String sql = "select a.id as id, a.subject as subject, substr(to_char(a.create_time, 'yyyy-mm-dd'), 0, 4) year,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 6, 2) month,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 9, 2) day from edoc_summary a, edoc_body b where a.has_archive = 1 and a.id = b.edoc_id and a.id in (select id from TEMP_NUMBER10)";
////            statement = connection.createStatement();
////            rs = statement.executeQuery(sql);
//////            DocumentFactory df = V3XLocator.getInstance().lookup(DocumentFactoryImpl.class);
//////            if(documentFactory==null){
//////                documentFactory=(DocumentFactory)AppContext.getBean("documentFactory");
//////            }
////            TransformerFactory tFactory = TransformerFactory.newInstance();
////            String[] htmlContent = null;
////            String sPath = "";
////            while (rs.next()) {
////                String idTest = rs.getString("id");
//////                htmlContent = documentFactory.exportOfflineEdocModel(Long.parseLong(rs.getString("id")));
////                htmlContent = DocumentManager.getInstance().exportOfflineEdocModel(Long.parseLong(rs.getString("id")));
////                Transformer transformer = tFactory.newTransformer(new StreamSource(new StringReader(htmlContent[1])));
////                sPath = "/upload/" + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day") + File.separator + rs.getString("id") + ".html";
////                if (!(new File("/upload" + File.separator + rs.getString("year"))).exists() && !(new File("/upload" + File.separator + rs.getString("year"))).isDirectory()) {
////                    (new File("/upload" + File.separator + rs.getString("year"))).mkdir();
////                }
////
////                if (!(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))).exists() && !(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))).isDirectory()) {
////                    (new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))).mkdir();
////                }
////
////                if (!(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day"))).exists() && !(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day"))).isDirectory()) {
////                    (new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day"))).mkdir();
////                }
////
////                if (!(new File(sPath)).exists()) {
////                    (new File(sPath)).createNewFile();
////                }
////
////                System.out.println("上传HTML文件路径>>>>>>" + sPath);
////                transformer.transform(new StreamSource(new StringReader(htmlContent[0])), new StreamResult(new OutputStreamWriter(new FileOutputStream(sPath), "GBK")));
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
//}
