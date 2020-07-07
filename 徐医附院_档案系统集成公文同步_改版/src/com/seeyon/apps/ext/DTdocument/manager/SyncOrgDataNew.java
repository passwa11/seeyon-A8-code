package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.document.impl.DocumentFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
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
public class SyncOrgDataNew {

    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private static Logger logger = LoggerFactory.getLogger(SyncOrgDataNew.class);

    public static SyncOrgDataNew syncOrgData;

    private DocumentFactory df = new DocumentFactoryImpl();

    public static SyncOrgDataNew getInstance() {
        return syncOrgData = new SyncOrgDataNew();
    }

    public SyncOrgDataNew() {
    }

    /**
     * 同步公文
     */
    public void syncSummary() throws SQLException {
        //获取系统路径
        System.out.println("开始同步");
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
                    ") A WHERE A .has_archive = 1 ) ss where exists (SELECT * FROM TEMP_NUMBER10 t where STATUS=0 and SS.EDOCSUMMARYID=t.ID)";
            executeJdbc(connection, "5", sql41);


            String sql10 = "select id,edocSummaryId,subject,YEAR,MONTH,DAY from (SELECT A . affairId AS ID,A.edocSummaryId,A .subject AS subject,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),0,4) YEAR,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),6,2) MONTH,  " +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),9,2) DAY FROM (  " +
                    "select * from (SELECT c.ID affairId,E.ID edocSummaryId,E.SUBJECT,E.create_time,E .has_archive FROM CTP_AFFAIR c,  " +
                    "(select s.* from (select * from EDOC_SUMMARY where has_archive=1) s,TEMP_NUMBER40 a where s.id=a.MODULE_ID and a.CONTENT_TYPE in (10,20) " +
                    ") E WHERE c.OBJECT_ID = E . ID AND c.ARCHIVE_ID IS NOT NULL   " +
                    "AND E .has_archive = 1) c  " +
                    ") A WHERE A .has_archive = 1 ) ss where exists (SELECT * FROM TEMP_NUMBER10 t where STATUS=0 and SS.EDOCSUMMARYID=t.ID)";
            executeJdbc(connection, "6", sql10);

            System.out.println("同步结束");

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
            if (type.equals("5")) {
                InsertManagerImpl.getInstance().querySqlInsert20();
                InsertManagerImpl.getInstance().querySqlInsert30();
            } else if (type.equals("6")) {
                InsertManagerImpl.getInstance().queryPro4();
            }
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

                if (type.equals("6")) {
                    LocalDate date = LocalDate.now();
                    String prefix = date.getYear() + "";
                    //公文标椎正文  标椎正文是大文本数据  所以要先创建一个正文的文件，用来存正文内容
                    p = classPath.substring(0, classPath.indexOf(syear));
                    String sPath2 = p + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day") + File.separator + rs.getString("edocSummaryId") + prefix + "";
                    File f2 = new File(sPath2);

                    File parentfile2 = f2.getParentFile();
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
            String attribute = set.getString("attribute") == null ? "" : set.getString("attribute");
            String content = set.getString("content") == null ? "" : set.getString("content");
            String deptName = set.getString("department_name") == null ? "" : set.getString("department_name");
            String userName = set.getString("create_user_id") == null ? "" : set.getString("create_user_id");
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


}
