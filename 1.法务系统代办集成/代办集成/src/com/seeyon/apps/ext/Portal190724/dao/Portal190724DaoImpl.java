package com.seeyon.apps.ext.Portal190724.dao;


import com.seeyon.apps.ext.Portal190724.pojo.Contract;
import com.seeyon.apps.ext.Portal190724.util.ReadConfigTools;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portal190724DaoImpl implements Portal190724Dao {

    private ReadConfigTools configTools = null;
    private String url = "";
    private String username = "";
    private String password = "";
    private String driverClassName = "";

    public Portal190724DaoImpl() {
        configTools = new ReadConfigTools();
        url = configTools.getString("lowPortalPlugin.db.url");
        username = configTools.getString("lowPortalPlugin.db.username");
        password = configTools.getString("lowPortalPlugin.db.password");
        driverClassName = configTools.getString("lowPortalPlugin.db.driverClassName");
    }

    //获取登录法律系统用户名密码
    public Map<String, String> select(String id) {
        Map<String, String> map = new HashMap<String, String>();
        String sql = "select * from Law_Fox_Table where id=?";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            //绑定参数
            pst.setString(1, id);
            rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    map.put(rs.getString(2), rs.getString(3));
                    System.out.println("用户名2：" + rs.getString(2) + "，密码2：" + rs.getString(3));
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            map = null;
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    //查询用户是否登录法律
    @Override
    public int selectState(String id) {
        int i = 0;
        String sql = "select * from Law_Fox_Table lft where lft.id=? and lft.law_state='1'";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            //绑定参数
            pst.setString(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                i = 0;
            } else {
                i = -1;
            }
        } catch (Exception e) {
            i = -1;
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    @Override
    public boolean save(Long long1, List<Contract> lawList) {
        System.out.println("这是保存合同的方法");
        boolean flag = false;
        Connection conn = null;
        PreparedStatement pst = null;
        String sql = "";
        int size = 0;
//        int flag=0;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            sql = (new StringBuilder("begin delete Contract where OAUSERID = ")).append(long1).append(";").toString();
            size = lawList.size();
            System.out.println((new StringBuilder("法律条数size=")).append(size).toString());
            if (size == 0) {
                try {
                    pst.close();
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println((new StringBuilder("插入法律,用户id=")).append(long1).toString());
                for (int i = 0; i < size; i++) {
                    sql = (new StringBuilder(String.valueOf(sql))).
                            append(" insert into Contract(OAUSERID,taskName,busiType,createOrg,createUser,beginTime,taskUrl,handleUser,appTaskId) values('")
                            .append(long1).append("','")
                            .append((lawList.get(i)).getTaskName())
                            .append("','").append((lawList.get(i)).getBusiType())
                            .append("','").append((lawList.get(i)).getCreateOrg())
                            .append("','").append((lawList.get(i)).getCreateUser())
                            .append("','").append((lawList.get(i)).getBeginTime())
                            .append("','").append((lawList.get(i)).getTaskUrl())
                            .append("','").append((lawList.get(i)).getHandleUser())
                            .append("','").append((lawList.get(i)).getAppTaskId())
                            .append("'); ").toString();
                }
                sql = (new StringBuilder(String.valueOf(sql))).append(" end;").toString();
                System.out.println("插入contract的sql 语句：" + sql);
                pst = conn.prepareStatement(sql);
                int count = pst.executeUpdate();
                conn.commit();
                System.out.println("-------------插入法律成功！");
//                flag = 1;
                flag = true;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println((new StringBuilder("插入法律信息发生异常：异常信息------->")).append(e.getMessage()).toString());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println((new StringBuilder("插入法律信息发生异常：异常信息------->")).append(e.getMessage()).toString());
        } finally {
            try {
                if (null != pst) {
                    pst.close();
                }
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    @Override
    public List<Contract> getAllLaw(Long long1) {
        List<Contract> laws = new ArrayList<>();
        String sql = (new StringBuilder("select * from Contract where OAUSERID like '")).append(long1).append("%'").toString();
        System.out.println("获取所有法律代办列表的sql :" + sql);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            if (rs != null) {
                for (int i = 0; rs.next(); i++) {
                    Contract contract = new Contract();
                    contract.setTaskName(rs.getString("TASKNAME"));
                    contract.setBusiType(rs.getString("BUSITYPE"));
                    contract.setCreateOrg(rs.getString("CREATEORG"));
                    contract.setCreateUser(rs.getString("CREATEUSER"));
                    contract.setBeginTime(rs.getString("BEGINTIME"));
                    contract.setTaskUrl(rs.getString("TASKURL"));
                    contract.setHandleUser(rs.getString("HANDLEUSER"));
                    contract.setAppTaskId(rs.getString("APPTASKID"));
                    laws.add(contract);
                }
            }
        } catch (Exception e) {
            laws = null;
            System.out.println("查询所有法律信息发生异常：异常信息");
            e.printStackTrace();
        } finally {
            try {
                if (null != pst) {
                    pst.close();
                }
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return laws;
    }

    @Override
    public List<Contract> getLimitLaw(Long long1) {
        List<Contract> laws = new ArrayList<>();
        String sql = (new StringBuilder("select * from Contract where OAUSERID like '")).append(long1).append("%' and rownum <10").toString();
        System.out.println("获取所有法律代办列表的sql :" + sql);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            if (rs != null) {
                for (int i = 0; rs.next(); i++) {
                    Contract contract = new Contract();
                    contract.setTaskName(rs.getString("TASKNAME"));
                    contract.setBusiType(rs.getString("BUSITYPE"));
                    contract.setCreateOrg(rs.getString("CREATEORG"));
                    contract.setCreateUser(rs.getString("CREATEUSER"));
                    contract.setBeginTime(rs.getString("BEGINTIME"));
                    contract.setTaskUrl(rs.getString("TASKURL"));
                    contract.setHandleUser(rs.getString("HANDLEUSER"));
                    contract.setAppTaskId(rs.getString("APPTASKID"));
                    laws.add(contract);
                }
            }
        } catch (Exception e) {
            laws = null;
            System.out.println("查询所有法律信息发生异常：异常信息");
            e.printStackTrace();
        } finally {
            try {
                if (null != pst) {
                    pst.close();
                }
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return laws;
    }
}
