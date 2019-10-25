package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.DTdocument.util.DbConnUtil;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.document.impl.DocumentFactoryImpl;
import www.seeyon.com.utils.FileUtil;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.*;

/**
 * Created by Administrator on 2019-9-10.
 */
public class SyncOrgData {

    public static SyncOrgData syncOrgData;

    private DocumentFactory df = new DocumentFactoryImpl();
    private TransformerFactory tFactory = TransformerFactory.newInstance();

    public static SyncOrgData getInstance() {
        return syncOrgData = new SyncOrgData();
    }

    public SyncOrgData() {
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
     * 同步机构
     *
     * @return
     */
    public int syncOrgUnit() {
        String sql = "insert into s_midorg@testlink(ID,FLDSSGSID,FLDSSGSMC,C_ORGNAME,C_ORGID,C_PARENTID,C_DATE,I_TAG,I_STATE) " +
                "select OU1.id,ou1.ORG_ACCOUNT_ID fldssgsid,nvl((select name from ORG_UNIT ou2 where OU2.id=OU1.ORG_ACCOUNT_ID),ou1.name) fldssgsmc,ou1.name C_ORGNAME,ou1.id C_ORGID ,ou1.path C_PARENTID,SYSDATE,0 tag,0 s_state " +
                "from ORG_UNIT ou1,ORG_UNIT ou3 where  substr(OU1.path,0,length(OU1.path)-4)=ou3.path and ou1.IS_DELETED='0' and ou1.id not in (select id from s_midorg@testlink) ORDER BY OU1.SORT_ID asc";
        int flag = syncOrg(sql);
        return flag;
    }

    /**
     * 同步人员
     *
     * @return
     */
    public int syncOrgMember() {
        String sql = "insert into s_midusers@testlink(ID,FLDSSGSID,FLDSSGSMC,C_USERNAME,C_LOGNAME,C_ORGID,C_PASSWORD,C_TELPHONE,C_EMAIL,I_LEAVE,I_TAG,I_STATE,I_DATE,C_PDE1,C_PDE2,C_PDE3) " +
                "select id,org_account_id FLDSSGSID,(select name from ORG_UNIT ou where OU.id=OM1.org_account_id) fldssgsmc ,name C_USERNAME,(select login_name from ORG_PRINCIPAL om2 where OM2.MEMBER_ID=OM1.id) C_LOGNAME,OM1.ORG_DEPARTMENT_ID C_ORGID, " +
                "(select CREDENTIAL_VALUE from ORG_PRINCIPAL om2 where OM2.MEMBER_ID=OM1.id) pwd,ext_attr_1 mobile,ext_attr_2 email,(case OM1.state when 1 then 0 when 2 then 1 else 1 end) I_LEAVE,0 I_TAG,0 I_STATE,SYSDATE I_DATE,'' C_PDE1,'' C_PDE2,'' C_PDE3 " +
                "from ORG_MEMBER om1 where  OM1.id not in (select id from s_midusers@testlink)";
        int flag = syncOrg(sql);
        return flag;
    }

    /**
     * 同步公文
     */
    public void syncSummary() {
        Connection connection = DbConnUtil.getInstance().getConnection();
        Statement statement = null;
        ResultSet rs = null;
        try {
            CallableStatement edoc_key = connection.prepareCall("{call pro_test(?)}");
            edoc_key.setInt(1, 1);
            edoc_key.execute();
            CallableStatement edoc_record = connection.prepareCall("{call pro_test(?)}");
            edoc_record.setInt(1, 2);
            edoc_record.execute();
            CallableStatement edoc_content = connection.prepareCall("{call pro_test(?)}");
            edoc_content.setInt(1, 3);
            edoc_content.execute();
            CallableStatement edoc_attach = connection.prepareCall("{call pro_test(?)}");
            edoc_attach.setInt(1, 4);
            edoc_attach.execute();
//            正式
            String sql = "select a.id as id, a.subject as subject, substr(to_char(a.create_time, 'yyyy-mm-dd'), 0, 4) year,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 6, 2) month,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 9, 2) day from edoc_summary a, edoc_body b where a.has_archive = 1 and a.id = b.edoc_id and a.id in (select id from TEMP_NUMBER1)";
//            测试
//            String sql = "select a.id as id, a.subject as subject, substr(to_char(a.create_time, 'yyyy-mm-dd'), 0, 4) year,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 6, 2) month,  substr(to_char(a.create_time, 'yyyy-mm-dd'), 9, 2) day from edoc_summary a, edoc_body b where a.has_archive = 1 and a.id = b.edoc_id and a.id in (select id from TEMP_NUMBER10)";
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            String[] htmlContent = null;
            String sPath = "";
            String classPath = this.getClass().getResource("/").getPath();
            String p = classPath.substring(0, classPath.indexOf("ApacheJetspeed")).concat("base");
            while (rs.next()) {
                htmlContent = df.exportOfflineEdocModel(Long.parseLong(rs.getString("id")));
                Transformer transformer = tFactory.newTransformer(new StreamSource(new StringReader(htmlContent[1])));
                sPath = "/upload/" + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day") + File.separator + rs.getString("id") + ".html";
                if (!(new File("/upload" + File.separator + rs.getString("year"))).exists() && !(new File("/upload" + File.separator + rs.getString("year"))).isDirectory()) {
                    (new File("/upload" + File.separator + rs.getString("year"))).mkdir();
                }

                if (!(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))).exists() && !(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))).isDirectory()) {
                    (new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))).mkdir();
                }

                if (!(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day"))).exists() && !(new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day"))).isDirectory()) {
                    (new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month") + File.separator + rs.getString("day"))).mkdir();
                }

                if (!(new File(p + sPath)).exists()) {
                    (new File(p + sPath)).createNewFile();
                }

//                System.out.println("上传HTML文件路径>>>>>>" + sPath);
                transformer.transform(new StreamSource(new StringReader(htmlContent[0])), new StreamResult(new OutputStreamWriter(new FileOutputStream(p+sPath), "GBK")));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            str = " select '/upload/' || substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' ||  substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' ||  substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' || C.Filename || '.doc' as C_FTPFILEPATH  from edoc_summary A left join (select * from edoc_body where content_type <> 'HTML') B on B.Edoc_Id = A.Id left join ctp_file C on to_char(B.content) = C.Id  where a.has_archive = 1 and B.Id is not null  and a.id in (select id from TEMP_NUMBER10)";
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
            str = " select '/upload/' || substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' ||  substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' ||  substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' ||  C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH   from edoc_summary A left join edoc_body  B on B.Edoc_Id = A.Id left join ctp_attachment C on b.Edoc_Id = c.reference  where a.has_archive = 1 and C.id is not null  and a.id in (select id from TEMP_NUMBER10)";
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
        }

    }

}
