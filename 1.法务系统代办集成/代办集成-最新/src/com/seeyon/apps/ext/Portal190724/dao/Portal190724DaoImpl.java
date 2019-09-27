package com.seeyon.apps.ext.Portal190724.dao;


import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.apps.ext.Portal190724.util.ReadConfigTools;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;

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

    @Override
    public UserPas selectDocAccount(String id) {
        String sql = "select * from Law_Fox_Table where id=?";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        UserPas userPas=null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(sql);
            //绑定参数
            pst.setString(1, id);
            rs = pst.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    userPas=new UserPas();
                    userPas.setLaw_user(rs.getString("LAW_USERNAME"));
                    userPas.setLaw_pas(rs.getString("LAW_PASSWORD"));
                    userPas.setFox_user(rs.getString("FOX_USERNAME"));
                    userPas.setFox_pas(rs.getString("FOX_PASSWORD"));
                    userPas.setRecord_user(rs.getString("RECORD_USERNAME"));
                    userPas.setRecord_pas(rs.getString("RECORD_PASSWORD"));
                }
            } else {
                return userPas;
            }
        } catch (Exception e) {
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userPas;
    }

    @Override
    public void setDocAccount(UserPas userPas) {
        String checkSql = "select * from Law_Fox_Table where id=" + userPas.getId();
        String updateSql = "update Law_Fox_Table lft set lft.RECORD_USERNAME='" + userPas.getRecord_user() + "',lft.RECORD_PASSWORD='" + userPas.getRecord_pas() + "',lft.RECORD_STATE='0' where lft.id=" + userPas.getId();
        String sql = "insert into Law_Fox_Table values(?,'','','','',?,?,0,0,0,0,0,0,0,0,0)";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(checkSql);
            rs = pst.executeQuery();
            boolean next = rs.next();
            if (next == true) {
                pst.executeUpdate(updateSql);
            } else {
                pst = conn.prepareStatement(sql);
                pst.setString(1, userPas.getId());
                pst.setString(2, userPas.getRecord_user());
                pst.setString(3, userPas.getRecord_pas());
                pst.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("查询发生异常：异常信息------->" + e.getMessage());
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteLaw(String userId) {
        String sql = "delete from CONTRACT where OAUSERID= ?";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            if (conn != null) {
                pst = conn.prepareStatement(sql);
                // 绑定参数
                pst.setString(1, userId);
                pst.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pst.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int updateState(String id) {
        int i = -1;
        String sql = "update Law_Fox_Table lft set lft.law_state='1' where lft.id=?";// 用户已登录法律系统
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            if (conn != null) {
                pst = conn.prepareStatement(sql);
                // 绑定参数
                pst.setString(1, id);
                int row = pst.executeUpdate();
                if (row == 1) {
                    i = 0;
                }
            }
        } catch (Exception e) {
            i = -1;
            System.out.println("更待状态发生异常：异常信息------->" + e.getMessage());
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
    public int updateDocState(String columnName,String id) {
        int i = -1;
        String sql = "update Law_Fox_Table lft set lft."+columnName+"='1' where lft.id=?";// 用户已登录法律系统
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            if (conn != null) {
                pst = conn.prepareStatement(sql);
                // 绑定参数
                pst.setString(1, id);
                int row = pst.executeUpdate();
                if (row == 1) {
                    i = 0;
                }
            }
        } catch (Exception e) {
            i = -1;
            System.out.println("更待状态发生异常：异常信息------->" + e.getMessage());
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

    @Override
    public FlipInfo findMoreLaw(FlipInfo fi, Map params) throws BusinessException {
        String hql = "from Contract where oauserId=:oauserId order by beginTime desc";
        DBAgent.find(hql, params, fi);
        return fi;
    }

    @Override
    public int setAddAccount(UserPas userPas) {
        int i = -1;
        String checkSql = "select * from Law_Fox_Table where id=" + userPas.getId();
        String updateSql = "update Law_Fox_Table lft set lft.LAW_USERNAME='" + userPas.getLaw_user() + "',lft.LAW_PASSWORD='" + userPas.getLaw_pas() + "' where lft.id=" + userPas.getId();
        String sql = "insert into Law_Fox_Table values(?,?,?,'','','','',0,0,0,0,0,0,0,0,0)";
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
            pst = conn.prepareStatement(checkSql);
            rs = pst.executeQuery();
            boolean next = rs.next();
            if (next == true) {
                int count = pst.executeUpdate(updateSql);
                if (count == 1) {
                    try {
                        pst.close();
                        conn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                } else {
                    try {
                        pst.close();
                        conn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return -1;
                }

            } else {
                pst = conn.prepareStatement(sql);
                pst.setString(1, userPas.getId());
                pst.setString(2, userPas.getLaw_user());
                pst.setString(3, userPas.getLaw_pas());
                int rows = pst.executeUpdate();
                if (rows == 1) {
                    i = 0;
                }
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
}
