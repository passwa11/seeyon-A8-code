package com.seeyon.apps.meetingSeat.util;

import com.seeyon.ctp.util.JDBCAgent;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCUtil
{
  private static final Logger log = Logger.getLogger(JDBCUtil.class);

  public static List<Map> doQuery(String sql)
  {
    JDBCAgent jdbc = new JDBCAgent();
    List resultMap = new ArrayList();
    try {
      jdbc.execute(sql);
      return jdbc.resultSetToList();
    } catch (Exception e) {
      e.printStackTrace();
      log.info(e);
    } finally {
      if (jdbc != null) {
        jdbc.close();
      }
    }
    return resultMap;
  }

  public static int doUpdateOrInsert(String sql) {
    int result = 0;
    List params = new ArrayList();
    JDBCAgent jdbc = new JDBCAgent();
    try {
      jdbc.batch1Prepare(sql);
      jdbc.batch2Add(params);
      result = jdbc.batch3Execute();
    } catch (Exception e) {
      e.printStackTrace();
      log.info(e);
    } finally {
      if (jdbc != null) {
        jdbc.close();
      }
    }
    return result;
  }

  public static Connection getConnection4Oracle(Map<String, String> connectInfo)
  {
    Connection connector = null;
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      connector = DriverManager.getConnection((String)connectInfo.get("url"), 
        (String)connectInfo.get("userName"), (String)connectInfo.get("passWord"));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      log.info(e);
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    }
    return connector;
  }

  public static Connection getConnection4Oracle10()
  {
    log.info("进入数据库连接----------开始");
    Connection connector = null;
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      //String url = "jdbc:oracle:thin:@127.0.0.1:1521:V7SP3";
      //String user = "V7SP3";
      //String password = "V7SP3V7SP3";
      String url = "jdbc:oracle:thin:@10.100.1.72:1521:ora11g";
      String user = "seeyon";
      String password = "Oracle11g";
      connector = DriverManager.getConnection(url, user, password);
      log.info("进入数据库连接----------成功");
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    return connector;
  }

  public static Connection getConnection4OracleMYSQL()
  {
    Connection connector = null;
    try {
      String driver = "com.mysql.jdbc.Driver";
      String url = "jdbc:mysql://127.0.0.1:3306/suyin";
      String user = "root";
      String password = "was081200";
      Class.forName(driver);
      connector = DriverManager.getConnection(url, user, password);
      System.out.println("连接数据库成功----------");
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    return connector;
  }

  public static int executeUpdate4Oracle01(String sql)
  {
    Connection connector = getConnection4Oracle10();
    Statement statement = null;
    int i = 0;
    try {
      statement = connector.createStatement();
      i = statement.executeUpdate(sql);
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    } finally {
      closeConnection4Oracle(connector, statement);
    }
    return i;
  }

  public static boolean executeUpdate4Oracle02(String sql)
  {
    Connection connector = getConnection4Oracle10();
    Statement statement = null;
    boolean ss = false;
    try {
      statement = connector.createStatement();
      ResultSet sss = statement.executeQuery(sql);
      if (sss.next()) {
        ss = true;
        String idStr = sss.getString(1);
        System.out.println("idStr1修改==========" + idStr);
      }
      System.out.println("检查结果集合==========" + statement.executeQuery(sql));
      System.out.println("ss==========" + ss);
      return ss;
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    } finally {
      closeConnection4Oracle(connector, statement);
    }
    return false;
  }

  public static int executeUpdate4Oracle03(String sql)
  {
    Connection connector = getConnection4Oracle10();
    Statement statement = null;
    String idStr = null;
    int idMax = 0;
    try {
      statement = connector.createStatement();
      ResultSet resultSet = statement.executeQuery(sql);
      System.out.println("检查结果集合是否为空==========" + resultSet.next());
      if (resultSet.next()) {
        idStr = resultSet.getString(1);
        System.out.println("idStr1==========" + idStr);
        String idStr2 = resultSet.getString(2);
        System.out.println("idStr2==========" + idStr2);
        try {
          idMax = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
          e.printStackTrace();
          System.out.println("String转化为int失败！==========");
        }
      }

      System.out.println("idMax==========" + idMax);
      return idMax;
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    } finally {
      closeConnection4Oracle(connector, statement);
    }
    return idMax;
  }

  public static List<Map<String, Object>> executeUpdate4Oracle04(String sql)
  {
    Connection connector = getConnection4Oracle10();

    List list = new ArrayList();
    Statement statement = null;
    ResultSet sss = null;
    try {
      statement = connector.createStatement();

      sss = statement.executeQuery(sql);
      ResultSetMetaData md = sss.getMetaData();
      int columnCount = md.getColumnCount();

      while (sss.next()) {
        Map rowData = new HashMap();
        for (int i = 1; i <= columnCount; ++i) {
          rowData.put(md.getColumnName(i), sss.getObject(i));
        }
        list.add(rowData);
      }

      return list;
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    } finally {
      closeConnection4Oracle(connector, statement);
    }
    System.out.println("检查结果集合==========" + sss);
    return list;
  }

  public static void closeConnection4Oracle(Connection connector, Statement statement)
  {
    try
    {
      statement.close();
      connector.close();
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    }
  }

  public static void closeConnection4Oracle(Connection connector, Statement statement, ResultSet resultSet)
  {
    try {
      resultSet.close();
      statement.close();
      connector.close();
    } catch (SQLException e) {
      e.printStackTrace();
      log.info(e);
    }
  }
}