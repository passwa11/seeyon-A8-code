package com.seeyon.apps.ext.copyFile.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.ext.copyFile.util.ConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.ext.copyFile.pojo.DocEntity;

public class DocEntityDao {
    private static final Log log = LogFactory.getLog(DocEntityDao.class);


    public Connection getConnection() {
        Connection conn = null; // 创建用于连接数据库的Connection对象
        try {
            Class.forName(ConfigProperties.getDriverClassName());// 加载Mysql数据驱动

            conn = DriverManager.getConnection(ConfigProperties.getUrl(), ConfigProperties.getUsername(), ConfigProperties.getPassword());// 创建数据连接

        } catch (Exception e) {

            e.printStackTrace();
        }
        return conn; // 返回所建立的数据库连接
    }

    /**
     * 加载路径
     */
    public Map<String, String> loadPath(long theDocLibId) {
        StringBuffer tmpSelectSQL = new StringBuffer(500);
        tmpSelectSQL.append("select ");
        tmpSelectSQL.append("id,fr_name");
        tmpSelectSQL.append(" from");
        tmpSelectSQL.append(" DOC_RESOURCES where DOC_LIB_ID=?");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        Map tmpList = new HashMap(100);
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(tmpSelectSQL.toString(), 1003, 1007);
            log.info("tmpSelectSQL=====" + tmpSelectSQL);
            pstmt.setLong(1, theDocLibId);
            rs = pstmt.executeQuery();
            rs.setFetchSize(100);
            String name = null;
            long x = 0L;
            log.info("rs===" + rs);
            while (rs.next()) {
                x = rs.getLong(1);
                log.info("x===" + x);
                name = rs.getString(2);
                log.info("name===" + name);
                name = (name == null) ? "1" : name.trim();
                tmpList.put(String.valueOf(x), name);
            }
        } catch (SQLException localSQLException) {
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return tmpList;
    }

    public List<DocEntity> load(long theDocLibId, String[] folderIds) {
        String where = "(";
        for (int i = 0; i < folderIds.length; ++i) {
            if (i > 0) {
                where = where + " or ";
            }
            where = where + " logical_path like '%" + folderIds[i] + "%'";
        }
        where = where + ")";

        StringBuffer tmpSelectSQL = new StringBuffer(500);
        tmpSelectSQL.append("select ");
        tmpSelectSQL.append("id,doc_lib_id,parent_fr_id,fr_name,is_folder");
        tmpSelectSQL.append(",source_id,logical_path");
        tmpSelectSQL.append(" from");
        tmpSelectSQL.append(" DOC_RESOURCES where DOC_LIB_ID=?");
        tmpSelectSQL.append(" and ");
        tmpSelectSQL.append(where);
        tmpSelectSQL.append(" order by FR_ORDER,LAST_UPDATE");
        log.info("tmpSelectSQL01=====" + tmpSelectSQL);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        List<DocEntity> tmpList = new ArrayList<DocEntity>();
        try {
            // conn =
            // JdbcConnectionLocator.getInstance().getConnection("java:comp/env/jdbc/ctpDataSource");
            conn = getConnection();
            pstmt = conn.prepareStatement(tmpSelectSQL.toString());
            System.out.println(theDocLibId);
            pstmt.setLong(1, theDocLibId);
            rs = pstmt.executeQuery();
            rs.setFetchSize(100);
            while (rs.next()) {

                tmpList.add(convertResultSet(rs));
            }
        } catch (SQLException localSQLException) {
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return tmpList;
    }

    public List<DocEntity> load() {
        StringBuffer tmpSelectSQL = new StringBuffer(500);
        tmpSelectSQL.append("select ");
        tmpSelectSQL.append("id,doc_lib_id,parent_fr_id,fr_name,is_folder");
        tmpSelectSQL.append(",source_id,logical_path");
        tmpSelectSQL.append(" from");
        tmpSelectSQL.append(" DOC_RESOURCES where DOC_LIB_ID=-3989647408744217611 order by FR_ORDER,LAST_UPDATE");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        List tmpList = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(tmpSelectSQL.toString(), 1003, 1007);
            rs = pstmt.executeQuery();
            rs.setFetchSize(100);
            while (rs.next()) {
                if (tmpList == null) {
                    tmpList = new ArrayList(10);
                }
                tmpList.add(convertResultSet(rs));
            }
        } catch (SQLException localSQLException) {
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return tmpList;
    }

    public DocEntity convertResultSet(ResultSet rs) throws SQLException {
        int index = 0;
        DocEntity tmpModel = new DocEntity();

        ++index;
        tmpModel.setId(rs.getLong(index));

        ++index;
        tmpModel.setDoc_lib_id(rs.getLong(index));

        ++index;
        tmpModel.setParent_fr_id(rs.getLong(index));

        ++index;
        tmpModel.setFr_name(rs.getString(index));

        ++index;
        tmpModel.setIs_folder(rs.getInt(index));

        ++index;
        tmpModel.setSource_id(rs.getLong(index));

        ++index;
        tmpModel.setLogical_path(rs.getString(index));
        System.out.println(tmpModel.toString());
        return tmpModel;
    }

    private void closeConnection(Connection con) {
        try {
            if (con != null)
                con.close();
        } catch (SQLException localSQLException) {
        }
        con = null;
    }

    private void closeStatement(Statement statement) {
        try {
            if (statement != null)
                statement.close();
        } catch (SQLException localSQLException) {
        }
        statement = null;
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException localSQLException) {
        }
        rs = null;
    }

    /**
     * source_id 文件号
     */
    public Map<String, String> getSummaryId(long source_id) {

        StringBuffer tmpSelectSQL = new StringBuffer(500);
        tmpSelectSQL.append("select ");
        tmpSelectSQL.append("id,object_id");
        tmpSelectSQL.append(" from");
        tmpSelectSQL.append(" ctp_affair where id=?");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        Map tmpList = new HashMap(100);
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(tmpSelectSQL.toString(), 1003, 1007);
            log.info("tmpSelectSQL=====" + tmpSelectSQL);
            pstmt.setLong(1, source_id);
            rs = pstmt.executeQuery();
            rs.setFetchSize(100);
            String name = null;
            long x = 0L;
            log.info("rs===" + rs);
            while (rs.next()) {
                x = rs.getLong(1);
                log.info("x===" + x);
                name = rs.getString(2);
                log.info("name===" + name);
                name = (name == null) ? "1" : name.trim();
                tmpList.put(String.valueOf(x), name);
            }
        } catch (SQLException localSQLException) {
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return tmpList;
    }

    public Map<String, String> getZhengWenId(String summaryId) {
        StringBuffer tmpSelectSQL = new StringBuffer(500);
        tmpSelectSQL.append("select ");
        tmpSelectSQL.append("module_id,content");
        tmpSelectSQL.append(" from");
        tmpSelectSQL.append(" ctp_content_all where content_type != '20' and module_id=?");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        Map tmpList = new HashMap(100);
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(tmpSelectSQL.toString(), 1003, 1007);
            log.info("tmpSelectSQL1111=====" + tmpSelectSQL);
            pstmt.setString(1, summaryId);
            rs = pstmt.executeQuery();
            rs.setFetchSize(100);
            String name = null;
            long x = 0L;
            log.info("rs1===" + rs);
            while (rs.next()) {
                x = rs.getLong(1);
                log.info("x1===" + x);
                name = rs.getString(2);
                log.info("name1===" + name);
                name = (name == null) ? "1" : name.trim();
                tmpList.put(String.valueOf(x), name);
            }
        } catch (SQLException localSQLException) {
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return tmpList;
    }
}