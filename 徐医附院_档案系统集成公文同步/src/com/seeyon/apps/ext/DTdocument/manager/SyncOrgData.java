package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.DTdocument.po.TempDate;
import com.seeyon.apps.ext.DTdocument.util.DbConnUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.FileManagerImpl;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.manager.EdocSummaryManagerImpl;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.document.impl.DocumentFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import www.seeyon.com.utils.FileUtil;

import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2019-9-10.
 */
public class SyncOrgData {

    private FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
    private static Logger logger = LoggerFactory.getLogger(SyncOrgData.class);

    public static SyncOrgData syncOrgData;

    private DTdocumentManager dt = new DTdocumentManagerImpl();
    private DocumentFactory df = new DocumentFactoryImpl();
    private TransformerFactory tFactory = TransformerFactory.newInstance();
    private EdocSummaryManagerImpl edocSummaryManager = (EdocSummaryManagerImpl) AppContext.getBean("edocSummaryManager");

    public static SyncOrgData getInstance() {
        return syncOrgData = new SyncOrgData();
    }

    public SyncOrgData() {
    }

    public void getSummary() {
        try {
            EdocSummary summary = edocSummaryManager.getEdocSummaryById(7529576166400344252l, true, false);
            Set<EdocBody> bodySet = summary.getEdocBodies();
        } catch (EdocException e) {
            e.printStackTrace();
        }
    }

    public int syncOrg(String sql) {
        Connection connection = DbConnUtil.getInstance().getConnection();
        PreparedStatement ps = null;
        int flag = 0;
        try {
            ps = connection.prepareStatement(sql);
            flag = ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                ps.close();
                connection.close();
            } catch (SQLException sq) {
                sq.printStackTrace();
            }
        }
        return flag;
    }


    /**
     * 同步公文
     */
    public void syncSummary() {
        //获取系统路径
        try {
            String spath = fileManager.getFolder(new Date(), false);
            System.out.println(spath);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        Connection connection = DbConnUtil.getInstance().getConnection();
        Statement statement = null;
        ResultSet rs = null;
        try {
            CallableStatement edoc_key = connection.prepareCall("{call pro_xyfy2(?)}");
            edoc_key.setInt(1, 1);
            edoc_key.execute();
            CallableStatement edoc_record = connection.prepareCall("{call pro_xyfy2(?)}");
            edoc_record.setInt(1, 2);
            edoc_record.execute();
            CallableStatement edoc_content = connection.prepareCall("{call pro_xyfy2(?)}");
            edoc_content.setInt(1, 3);
            edoc_content.execute();
            CallableStatement edoc_attach = connection.prepareCall("{call pro_xyfy2(?)}");
            edoc_attach.setInt(1, 4);
            edoc_attach.execute();

            String sql = "SELECT A . affairId AS ID,A.edocSummaryId,A .subject AS subject," +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),0,4) YEAR," +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),6,2) MONTH," +
                    "SUBSTR (TO_CHAR (A .create_time, 'yyyy-mm-dd'),9,2) DAY " +
                    "FROM (select c.id affairId,e.id edocSummaryId,e.SUBJECT,e.create_time,e.has_archive from CTP_AFFAIR c,EDOC_SUMMARY e where c.OBJECT_ID=e.id and c.ARCHIVE_ID is not null and e.has_archive = 1) A," +
                    "(SELECT * FROM CTP_CONTENT_ALL C,CTP_FILE F WHERE TO_NUMBER(C.CONTENT) = F.ID AND C.CONTENT_TYPE NOT IN (10)) B WHERE A .has_archive = 1 " +
                    " AND to_number(A . edocSummaryId) = to_number(b.MODULE_ID) AND to_number(A . edocSummaryId) IN (SELECT to_number(ID) FROM TEMP_NUMBER10 where status='0')";
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            String[] htmlContent = null;
            String sPath = "";
            String classPath = fileManager.getFolder(new Date(), false);
            String p = classPath.substring(0, classPath.indexOf("Seeyon"));
            //linux设置权限
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            perms.add(PosixFilePermission.OWNER_READ);//设置所有者的读取权限
            perms.add(PosixFilePermission.OWNER_WRITE);//设置所有者的写权限
            perms.add(PosixFilePermission.OWNER_EXECUTE);//设置所有者的执行权限
            perms.add(PosixFilePermission.GROUP_READ);//设置组的读取权限
            perms.add(PosixFilePermission.GROUP_EXECUTE);//设置组的读取权限
            perms.add(PosixFilePermission.OTHERS_READ);//设置其他的读取权限
            perms.add(PosixFilePermission.OTHERS_EXECUTE);//设置其他的读取权限
            while (rs.next()) {
                htmlContent = df.exportOfflineEdocModel(Long.parseLong(rs.getString("id")));
                sPath = p + File.separator + "upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day") + File.separator + rs.getString("edocSummaryId") + "";
                File f = new File(sPath);
                //linux设置文件和文件夹的权限
                Path pathParent = Paths.get(f.getParentFile().getAbsolutePath());
                Path pathDest = Paths.get(f.getAbsolutePath());
                Files.setPosixFilePermissions(pathParent, perms);//修改文件夹路径的权限
                Files.setPosixFilePermissions(pathDest, perms);//修改图片文件的权限
                File parentfile = f.getParentFile();
                if (!parentfile.exists()) {
                    parentfile.mkdirs();
                }

                if (!(f.exists())) {
                    f.createNewFile();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    String msg = htmlContent[1];
                    fos.write(msg.getBytes());
                } catch (IOException e) {
                    logger.info("向文件中写入内容出错了:" + e.getMessage());
                } finally {
                    fos.close();
                }

            }
        } catch (SQLException sql) {
            logger.info("同步公文sql出错了：" + sql.getMessage());
        } catch (Exception e) {
            logger.info("同步公文出错了：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制OA正文
     */
    public void copyEdoc() {
        String str = "";
        Statement st = null;
        ResultSet rs = null;
        Connection conn = DbConnUtil.getInstance().getConnection();
        try {
//            str = " select '/upload/' || substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||  substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||  substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' || C.Filename || '.doc' as C_FTPFILEPATH  from edoc_summary A left join (select * from edoc_body where content_type <> 'HTML') B on B.Edoc_Id = A.Id left join ctp_file C on to_char(B.content) = C.Id  where a.has_archive = 1 and B.Id is not null  and a.id in (select id from TEMP_NUMBER10)";
            str = "SELECT '/upload/'||SUBSTR(TO_CHAR(C.Create_Date,'yyyy-mm-dd'),0,4)||'/'||SUBSTR(TO_CHAR(C.Create_Date,'yyyy-mm-dd'),6,2)||'/'||SUBSTR(TO_CHAR(C.Create_Date,'yyyy-mm-dd'),9,2)||'/'||C.Filename||'.doc' AS C_FTPFILEPATH " +
                    "FROM edoc_summary A LEFT JOIN (SELECT ZC.CONTENT,ZC.MODULE_ID FROM CTP_CONTENT_ALL zC,CTP_FILE F WHERE TO_NUMBER(zC.CONTENT) = F.ID AND zC.CONTENT_TYPE NOT IN (10)) B ON B.module_id = A . ID " +
                    "LEFT JOIN ctp_file C ON TO_CHAR (B. CONTENT) = C. ID WHERE A .has_archive = 1 AND A . ID IN (SELECT ID FROM TEMP_NUMBER10)";
            st = conn.createStatement();
            rs = st.executeQuery(str);
            String sPath = "";
            String sFilePath = "";
            String classPath = this.getClass().getResource("/").getPath();
            String p = classPath.substring(0, classPath.indexOf("ApacheJetspeed")).concat("base");
            while (rs.next()) {
                sPath = rs.getString("C_FTPFILEPATH");
                sFilePath = sPath.substring(0, sPath.lastIndexOf("."));
                if ((new File(p + sFilePath)).exists()) {
                    FileUtil.copyFile(new File(p + sFilePath), new File(p + sPath));
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            try {
                rs.close();
                st.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 复制OA附件
     */
    public void copyAttachment() {
        String str = "";
        Statement st = null;
        ResultSet rs = null;
        Connection conn = DbConnUtil.getInstance().getConnection();
        try {
            str = " select '/upload/' || substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||  substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||  substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||  C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH   from edoc_summary A left join (SELECT ZC.CONTENT,ZC.MODULE_ID FROM CTP_CONTENT_ALL zC,CTP_FILE F WHERE TO_NUMBER(zC.CONTENT) = F.ID AND zC.CONTENT_TYPE NOT IN (10))  B on B.MODULE_ID = A.Id left join ctp_attachment C on b.MODULE_ID = c.att_reference  where a.has_archive = 1 and C.id is not null  and a.id in (select id from TEMP_NUMBER10)";
            st = conn.createStatement();
            rs = st.executeQuery(str);
            String sPath = "";
            String sFilePath = "";
            String classPath = this.getClass().getResource("/").getPath();
            String p = classPath.substring(0, classPath.indexOf("ApacheJetspeed")).concat("base");

            while (rs.next()) {
                sPath = rs.getString("C_FTPFILEPATH");
                sFilePath = sPath.substring(0, sPath.lastIndexOf("."));
                if ((new File(p + sFilePath)).exists()) {
                    FileUtil.copyFile(new File(p + sFilePath), new File(p + sPath));
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            try {
                rs.close();
                st.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 清空临时表的数据
     */
    public void clearTemporary() {
        String sql10 = "delete from temp_number10";
        String sql20 = "delete from temp_number20";
        String sql30 = "delete from temp_number30";
        String sql40 = "delete from temp_number40";
        DbConnUtil.getInstance().deleteSql(sql10);
        DbConnUtil.getInstance().deleteSql(sql20);
        DbConnUtil.getInstance().deleteSql(sql30);
        DbConnUtil.getInstance().deleteSql(sql40);
    }
}
