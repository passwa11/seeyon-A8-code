package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.DTdocument.util.DbConnUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.manager.EdocSummaryManagerImpl;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.document.impl.DocumentFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import www.seeyon.com.utils.FileUtil;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2019-9-10.
 */
public class SyncOrgData {

    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private static Logger logger = LoggerFactory.getLogger(SyncOrgData.class);

    public static SyncOrgData syncOrgData;

    private DocumentFactory df = new DocumentFactoryImpl();
    private TransformerFactory tFactory = TransformerFactory.newInstance();
    private EdocSummaryManagerImpl edocSummaryManager = (EdocSummaryManagerImpl) AppContext.getBean("edocSummaryManager");

    public static SyncOrgData getInstance() {
        return syncOrgData = new SyncOrgData();
    }

    public SyncOrgData() {
    }

    /**
     * 同步公文
     */
    public void syncSummary() throws SQLException {
        //获取系统路径
        try {
            String spath = fileManager.getFolder(new Date(), false);
            System.out.println(spath);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        Connection connection = JDBCAgent.getRawConnection();
        try {
            String sql41 = "select id,edocSummaryId,subject,YEAR,MONTH,DAY from (SELECT A . affairId AS ID,A.edocSummaryId,A .subject AS subject,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),0,4) YEAR,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),6,2) MONTH,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),9,2) DAY FROM (  " +
                    "select * from (SELECT c.ID affairId,E.ID edocSummaryId,E.SUBJECT,E.create_time,E .has_archive FROM CTP_AFFAIR c,  " +
                    "(select s.* from (select * from EDOC_SUMMARY where has_archive=1) s,TEMP_NUMBER40 a where s.id=a.MODULE_ID and a.CONTENT_TYPE in (41,42,43,44,45)   " +
                    ") E WHERE c.OBJECT_ID = E . ID AND c.ARCHIVE_ID IS NOT NULL   " +
                    "AND E .has_archive = 1) c  " +
                    ") A WHERE A .has_archive = 1 ) ss where exists (SELECT * FROM TEMP_NUMBER10 t where 1=1 and SS.EDOCSUMMARYID=t.ID)";
            executeJdbc(connection, "3", sql41);


            String sql10 = "select id,edocSummaryId,subject,YEAR,MONTH,DAY from (SELECT A . affairId AS ID,A.edocSummaryId,A .subject AS subject,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),0,4) YEAR,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),6,2) MONTH,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),9,2) DAY FROM (  " +
                    "select * from (SELECT c.ID affairId,E.ID edocSummaryId,E.SUBJECT,E.create_time,E .has_archive FROM CTP_AFFAIR c,  " +
                    "(select s.* from (select * from EDOC_SUMMARY where has_archive=1) s,TEMP_NUMBER40 a where s.id=a.MODULE_ID and a.CONTENT_TYPE in (10,20) " +
                    ") E WHERE c.OBJECT_ID = E . ID AND c.ARCHIVE_ID IS NOT NULL   " +
                    "AND E .has_archive = 1) c  " +
                    ") A WHERE A .has_archive = 1 ) ss where exists (SELECT * FROM TEMP_NUMBER10 t where 1=1 and SS.EDOCSUMMARYID=t.ID)";
            executeJdbc(connection, "4", sql10);
        } catch (Exception e) {
            logger.info("同步公文出错了：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void executeJdbc(Connection connection, String type, String sql) {
        Statement statement = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            CallableStatement edoc_key = connection.prepareCall("{call pro_xyfy" + type + "(?)}");
            edoc_key.setInt(1, 1);
            edoc_key.execute();
            CallableStatement edoc_record = connection.prepareCall("{call pro_xyfy" + type + "(?)}");
            edoc_record.setInt(1, 2);
            edoc_record.execute();
            CallableStatement edoc_content = connection.prepareCall("{call pro_xyfy" + type + "(?)}");
            edoc_content.setInt(1, 3);
            edoc_content.execute();
            CallableStatement edoc_attach = connection.prepareCall("{call pro_xyfy" + type + "(?)}");
            edoc_attach.setInt(1, 4);
            edoc_attach.execute();

            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            String[] htmlContent = null;
            String sPath = "";
            String classPath = fileManager.getFolder(new Date(), false);
            LocalDate localDate = LocalDate.now();
            String syear = Integer.toString(localDate.getYear());
            String p = classPath.substring(0, classPath.indexOf(syear));
//            linux
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            perms.add(PosixFilePermission.OWNER_READ);//设置所有者的读取权限
            perms.add(PosixFilePermission.OWNER_WRITE);//设置所有者的写权限
            perms.add(PosixFilePermission.OWNER_EXECUTE);//设置所有者的执行权限
            perms.add(PosixFilePermission.GROUP_READ);//设置组的读取权限
            perms.add(PosixFilePermission.GROUP_EXECUTE);//设置组的读取权限
            perms.add(PosixFilePermission.OTHERS_READ);//设置其他的读取权限
            perms.add(PosixFilePermission.OTHERS_EXECUTE);//设置其他的读取权限

            String insertSql = "insert into TEMP_NUMBER30(ID,C_MIDRECID,C_FILETITLE,C_FTPFILEPATH,C_TYPE,I_SIZE,META_TYPE,STATUS) values(?,?,?,?,?,?,?,?)";
            String opinionSql = "select case attribute when 2 then '【'||'同意'||'】' when  3 then '【'||'不同意'||'】' else '' end attribute,policy,department_name,create_time,content,(select name from org_member where id= s.create_user_id) create_user_id from (select * from edoc_opinion where edoc_id=?) s";
            ResultSet opinionSet = null;
            PreparedStatement opinionPs = null;

            while (rs.next()) {
                opinionPs = connection.prepareStatement(opinionSql);
                opinionPs.setString(1, rs.getString("edocSummaryId"));
                opinionSet = opinionPs.executeQuery();
                String opinion = getJsString(opinionSet);

                htmlContent = df.exportOfflineEdocModel(Long.parseLong(rs.getString("id")));

                sPath = p + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day") + File.separator + rs.getString("edocSummaryId") + "";
                File f = new File(sPath);
                //linux设置文件和文件夹的权限
                Path pathParent = Paths.get(f.getParentFile().getAbsolutePath());
                Path pathDest = Paths.get(f.getAbsolutePath());
                Files.setPosixFilePermissions(pathParent, perms);//修改文件夹路径的权限

                File parentfile = f.getParentFile();
//                设置此抽象路径名的所有者或每个人的写入权限
                if (!parentfile.exists()) {
                    parentfile.mkdirs();
                }
                parentfile.setWritable(true,false);

                if (!f.exists()) {
                    f.createNewFile();
                }
                f.setWritable(true,false);


                //出错原因：下面这句话设置文件的权限必须在文件创建以后再修改权限，否则会报NoSuchFoundException
                Files.setPosixFilePermissions(pathDest, perms);//修改图片文件的权限

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    String msg = htmlContent[1] + " " + opinion;
                    fos.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.info("向文件中写入内容出错了:" + e.getMessage());
                } finally {
                    fos.close();
                    if (null != opinionSet) {
                        opinionSet.close();
                    }
                    if (null != opinionPs) {
                        opinionPs.close();
                    }
                }

                if (type.equals("4")) {
                    LocalDate date = LocalDate.now();
                    String prefix = date.getYear() + "";
                    //公文标椎正文  标椎正文是大文本数据  所以要先创建一个正文的文件，用来存正文内容
                    p = classPath.substring(0, classPath.indexOf(syear));
                    String sPath2 = p + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day") + File.separator + rs.getString("edocSummaryId") + prefix + "";
                    File f2 = new File(sPath2);

                    File parentfile2 = f2.getParentFile();
                    //设置此抽象路径名的所有者或每个人的写入权限
                    if (!parentfile2.exists()) {
                        parentfile2.mkdirs();
                    }
                    parentfile2.setWritable(true,false);


                    if (!f2.exists()) {
                        f2.createNewFile();
                    }
                    f2.setWritable(true,false);

                    String content = getZwData(connection, rs.getString("edocSummaryId"));
                    FileOutputStream fos2 = null;
                    try {
                        fos2 = new FileOutputStream(f2);
                        fos2.write(content.getBytes("UTF-8"));
                    } catch (IOException e) {
                        logger.info("向文件中写入内容出错了:" + e.getMessage());
                    } finally {
                        fos2.close();
                    }
                    ps = connection.prepareStatement(insertSql);
                    ps.setString(1, rs.getString("edocSummaryId") + prefix);
                    ps.setString(2, rs.getString("edocSummaryId"));
                    ps.setString(3, rs.getString("edocSummaryId") + prefix + ".html");
                    ps.setString(4, sPath2);
                    ps.setString(5, "正文");
                    ps.setString(6, f.length() + "");
                    ps.setString(7, ".html");
                    ps.setString(8, "0");
                    ps.executeUpdate();
                }

            }
        } catch (SQLException | BusinessException | ServiceException | IOException sbsi) {
            sbsi.printStackTrace();
            logger.info("同步公文sql出错了：" + sbsi.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public String getJsString(ResultSet set) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("<script type=\"text/javascript\">");
        while (set.next()) {
            String attribute = set.getString("attribute")==null?"":set.getString("attribute");
            String content = set.getString("content")==null?"":set.getString("content");
            String deptName = set.getString("department_name")==null?"":set.getString("department_name");
            String userName = set.getString("create_user_id")==null?"":set.getString("create_user_id");
            String createTime = set.getString("create_time").substring(0, set.getString("create_time").lastIndexOf(":"));
            String val = attribute + "  " + content + "  " + deptName + "  " + userName + "  " + createTime;
            sb.append("document.getElementById(\"" + set.getString("policy") + "\").innerHTML =\"" + val + "\";");

        }
        sb.append("</script>");
        return sb.toString();
    }

    public String getZwData(Connection connection, String summaryId) {
        Statement statement = null;
        ResultSet resultSet = null;
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Title</title></head><body><p>");
        Reader reader = null;
        try {
            char[] buffer = new char[1024];
            String sql = "select CONTENT from CTP_CONTENT_ALL where MODULE_ID=" + summaryId + " and CONTENT_TYPE=10";
            int i = 0;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                reader = resultSet.getCharacterStream("content");
                if (reader != null) {
                    while ((i = reader.read(buffer)) != -1) {
                        sb.append(new String(buffer, 0, i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                reader.close();
            } catch (SQLException | IOException sq) {
                sq.printStackTrace();
            }
        }
        sb.append("</p></body></html>");
        return sb.toString();
    }


//    /**
//     * 复制OA正文
//     */
//    public void copyEdoc2() {
//        String str = "";
//        Statement st = null;
//        ResultSet rs = null;
//        Connection conn = DbConnUtil.getInstance().getConnection();
//        try {
////            str = " select '/upload/' || substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||  substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||  substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' || C.Filename || '.doc' as C_FTPFILEPATH  from edoc_summary A left join (select * from edoc_body where content_type <> 'HTML') B on B.Edoc_Id = A.Id left join ctp_file C on to_char(B.content) = C.Id  where a.has_archive = 1 and B.Id is not null  and a.id in (select id from TEMP_NUMBER10)";
//            str = "SELECT '/upload/'||SUBSTR(TO_CHAR(C.Create_Date,'yyyy-mm-dd'),0,4)||'/'||SUBSTR(TO_CHAR(C.Create_Date,'yyyy-mm-dd'),6,2)||'/'||SUBSTR(TO_CHAR(C.Create_Date,'yyyy-mm-dd'),9,2)||'/'||C.Filename||'.doc' AS C_FTPFILEPATH " +
//                    "FROM edoc_summary A LEFT JOIN (SELECT ZC.CONTENT,ZC.MODULE_ID FROM CTP_CONTENT_ALL zC,CTP_FILE F WHERE TO_NUMBER(zC.CONTENT) = F.ID AND zC.CONTENT_TYPE NOT IN (10)) B ON B.module_id = A . ID " +
//                    "LEFT JOIN ctp_file C ON TO_CHAR (B. CONTENT) = C. ID WHERE A .has_archive = 1 AND A . ID IN (SELECT ID FROM TEMP_NUMBER10)";
//            st = conn.createStatement();
//            rs = st.executeQuery(str);
//            String sPath = "";
//            String sFilePath = "";
//            String classPath = this.getClass().getResource("/").getPath();
//            String p = classPath.substring(0, classPath.indexOf("ApacheJetspeed")).concat("base");
//            while (rs.next()) {
//                sPath = rs.getString("C_FTPFILEPATH");
//                sFilePath = sPath.substring(0, sPath.lastIndexOf("."));
//                if ((new File(p + sFilePath)).exists()) {
//                    FileUtil.copyFile(new File(p + sFilePath), new File(p + sPath));
//                }
//            }
//        } catch (Exception var7) {
//            var7.printStackTrace();
//        } finally {
//            try {
//                rs.close();
//                st.close();
//                conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    /**
//     * 复制OA附件
//     */
//    public void copyAttachment2() {
//        String str = "";
//        Statement st = null;
//        ResultSet rs = null;
//        Connection conn = DbConnUtil.getInstance().getConnection();
//        try {
//            str = " select '/upload/' || substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||  substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||  substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||  C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH   from edoc_summary A left join (SELECT ZC.CONTENT,ZC.MODULE_ID FROM CTP_CONTENT_ALL zC,CTP_FILE F WHERE TO_NUMBER(zC.CONTENT) = F.ID AND zC.CONTENT_TYPE NOT IN (10))  B on B.MODULE_ID = A.Id left join ctp_attachment C on b.MODULE_ID = c.att_reference  where a.has_archive = 1 and C.id is not null  and a.id in (select id from TEMP_NUMBER10)";
//            st = conn.createStatement();
//            rs = st.executeQuery(str);
//            String sPath = "";
//            String sFilePath = "";
//            String classPath = this.getClass().getResource("/").getPath();
//            String p = classPath.substring(0, classPath.indexOf("ApacheJetspeed")).concat("base");
//
//            while (rs.next()) {
//                sPath = rs.getString("C_FTPFILEPATH");
//                sFilePath = sPath.substring(0, sPath.lastIndexOf("."));
//                if ((new File(p + sFilePath)).exists()) {
//                    FileUtil.copyFile(new File(p + sFilePath), new File(p + sPath));
//                }
//            }
//        } catch (Exception var7) {
//            var7.printStackTrace();
//        } finally {
//            try {
//                rs.close();
//                st.close();
//                conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    /**
//     * 清空临时表的数据
//     */
//    public void clearTemporary() {
//        String sql10 = "delete from temp_number10";
//        String sql20 = "delete from temp_number20";
//        String sql30 = "delete from temp_number30";
//        String sql40 = "delete from temp_number40";
//        DbConnUtil.getInstance().deleteSql(sql10);
//        DbConnUtil.getInstance().deleteSql(sql20);
//        DbConnUtil.getInstance().deleteSql(sql30);
//        DbConnUtil.getInstance().deleteSql(sql40);
//    }
}
