package com.seeyon.apps.synorg.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;

/**
 * 自动执行插件sql脚本工具类
 * @author Yang.Yinghai
 * @date 2015-12-14下午5:26:03
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class ExcuteSqlFileUtil {

    /** 日志对象 */
    private static final Log log = LogFactory.getLog(ExcuteSqlFileUtil.class);

    /**
     * @param dsName dataSourceName
     * @param sqlDir sql存放目录，相对于CfgHome的目录
     */
    public static void initSqlFile() {
        try {
            Connection conn = JDBCAgent.getConnection();
            String dbName = getDBName(conn);
            File file = new File(AppContext.getCfgHome(), new StringBuilder("/plugin/synorg/sql/").append(dbName).append(".sql").toString());
            String initTableSql = FileUtils.readFileToString(file, "utf-8");
            try {
                String regexCreateTable = "CREATE\\s{1,}TABLE[\\s\\S]{1,}?\\(";
                Pattern pCreateTable = Pattern.compile(regexCreateTable, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher mCreateTable = pCreateTable.matcher(initTableSql);
                if(mCreateTable.find()) {
                    String match = mCreateTable.group();
                    String tabName = match.replaceAll("CREATE\\s{1,}TABLE\\s{1,}", "").replaceAll("\\(", "").trim();
                    // 查找表名
                    String testTable = "select 1 from " + tabName + " where 1=0";
                    PreparedStatement pst = conn.prepareStatement(testTable);
                    ResultSet rs = pst.executeQuery();
                    rs.close();
                }
            } catch(Exception e) {// 找不到中间表，执行脚本
                Statement pst = null;
                try {
                    conn.setAutoCommit(false);
                    pst = conn.createStatement();
                    String[] sqls = initTableSql.replaceAll("\r", "").replaceAll("\n", "").split(";");
                    for(String sql : sqls) {
                        if(Strings.isNotBlank(sql.trim())) {
                            pst.addBatch(sql);
                        }
                    }
                    pst.executeBatch();
                    conn.commit();
                } catch(Exception e1) {
                    log.error("", e1);
                } finally {
                    if(pst != null) {
                        pst.close();
                    }
                }
            }
        } catch(SQLException e1) {
            log.error("", e1);
        } catch(IOException e) {
            log.error("", e);
        }
    }

    /**
     * 根据连接对象 获取数据源对应数据库类型
     * @param con 数据连接
     * @return 数据库类型
     * @throws SQLException 异常
     */
    private static String getDBName(Connection con) throws SQLException {
        String dbName = con.getMetaData().getDatabaseProductName();
        if(dbName != null) {
            dbName = dbName.toUpperCase().replaceAll("\\s{1,}", "");
            if(dbName.contains("SQLSERVER")) {
                return "SQLSERVER";
            } else if(dbName.contains("ORACLE")) {
                return "ORACLE";
            } else if(dbName.contains("POSTGRESQL")) {
                return "POSTGRESQL";
            } else if(dbName.contains("DMDBMS")) {
                return "DM";
            } else if(dbName.contains("MYSQL")) {
                return "MYSQL";
            }
        }
        return "ORACLE";
    }
}
