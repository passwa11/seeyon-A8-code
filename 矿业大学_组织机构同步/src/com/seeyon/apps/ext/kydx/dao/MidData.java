package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
import com.seeyon.ctp.util.JDBCAgent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MidData {
    /**
     * 单位
     */

    public List<Map<String, String>> queryDwData() {
        List<Map<String, String>> list = new ArrayList<>();
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select dwid,dwmc,dwjc,lsdwh,dwh,sfsy,dz_dqzyjg from seeyon_oa_dw";
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            Map<String, String> map = null;
            while (rs.next()) {
                map = new HashMap<>();
                map.put("dwid", rs.getString("dwid"));
                map.put("dwmc", rs.getString("dwmc"));
                map.put("dwjc", rs.getString("dwjc"));
                map.put("lsdwh", rs.getString("lsdwh"));
                map.put("dwh", rs.getString("dwh"));
                map.put("sfsy", rs.getString("sfsy"));
                map.put("dz_dqzyjg", rs.getString("dz_dqzyjg"));
                list.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sq) {
                sq.printStackTrace();
            }

        }
        return list;
    }

    public void cleanTableData(String sql) {
        Connection connection=null;
        PreparedStatement ps = null;
        try {
            connection = JDBCAgent.getRawConnection();
            ps=connection.prepareStatement(sql);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertDwToOa() {

        String clearTable = "delete from seeyon_oa_dw where 1=1";

        List<Map<String, String>> list = this.queryDwData();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into seeyon_oa_dw(dwid,dwmc,dwjc,lsdwh,dwh,sfsy,dz_dqzyjg) values (?,?,?,?,?,?,?)";
        try {
            connection = JDBCAgent.getRawConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);
            if(list.size()>0){
                this.cleanTableData(clearTable);

                for (int i = 0; i < list.size(); i++) {
                    ps.setString(1, list.get(i).get("dwid"));
                    ps.setString(2, list.get(i).get("dwmc"));
                    ps.setString(3, list.get(i).get("dwjc"));
                    ps.setString(4, list.get(i).get("lsdwh"));
                    ps.setString(5, list.get(i).get("dwh"));
                    ps.setString(6, list.get(i).get("sfsy"));
                    ps.setString(7, list.get(i).get("dz_dqzyjg"));
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sq) {
                sq.printStackTrace();
            }
        }
    }

    public List<Map<String,String>> queryAccountData(){
        List<Map<String, String>> list = new ArrayList<>();
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select zwjbm,jzgid,xm,gh,dwh,dzzw,yddh,bglxdh,dzxx,grjj,yrfsdm from seeyon_oa_jzgjbxx";
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            Map<String, String> map = null;
            while (rs.next()) {
                map = new HashMap<>();
                map.put("zwjbm", rs.getString("zwjbm"));
                map.put("jzgid", rs.getString("jzgid"));
                map.put("xm", rs.getString("xm"));
                map.put("gh", rs.getString("gh"));
                map.put("dwh", rs.getString("dwh"));
                map.put("dzzw", rs.getString("dzzw"));
                map.put("yddh", rs.getString("yddh"));
                map.put("bglxdh", rs.getString("bglxdh"));
                map.put("dzxx", rs.getString("dzxx"));
                map.put("grjj", rs.getString("grjj"));
                map.put("yrfsdm", rs.getString("yrfsdm"));
                list.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sq) {
                sq.printStackTrace();
            }

        }
        return list;
    }


    public void insertAccountToOa(){
        String  sql="delete from seeyon_oa_jzgjbxx where 1=1";
        List<Map<String, String>> list = this.queryAccountData();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into seeyon_oa_jzgjbxx(zwjbm,jzgid,xm,gh,dwh,dzzw,yddh,bglxdh,dzxx,grjj,yrfsdm) values (?,?,?,?,?,?,?,?,?,?,?)";
        try {
            connection = JDBCAgent.getRawConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);
            if(list.size()>0){
                this.cleanTableData(sql);

                for (int i = 0; i < list.size(); i++) {
                    ps.setString(1, list.get(i).get("zwjbm"));
                    ps.setString(2, list.get(i).get("jzgid"));
                    ps.setString(3, list.get(i).get("xm"));
                    ps.setString(4, list.get(i).get("gh"));
                    ps.setString(5, list.get(i).get("dwh"));
                    ps.setString(6, list.get(i).get("dzzw"));
                    ps.setString(7, list.get(i).get("yddh"));
                    ps.setString(8, list.get(i).get("bglxdh"));
                    ps.setString(9, list.get(i).get("dzxx"));
                    ps.setString(10, list.get(i).get("grjj"));
                    ps.setString(11, list.get(i).get("yrfsdm"));
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException sq) {
                sq.printStackTrace();
            }
        }
    }


}
