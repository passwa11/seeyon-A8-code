package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.OrgLevel;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
import com.seeyon.client.CTPRestClient;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgLevelDaoImpl implements OrgLevelDao {
    @Override
    public List<OrgLevel> queryInsertOrgLevelList() {
        String sql = "select name,code,description from THIRD_ORG_LEVEL l where not exists (select * from M_ORG_LEVEL m where m.code=l.code) and (l.is_enable <>'0' or l.is_delete <>'1')";
        List<OrgLevel> levelList = new ArrayList<>();
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgLevel orgLevel = null;
            while (rs.next()) {
                orgLevel = new OrgLevel();
                orgLevel.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgLevel.setLevelcode(rs.getString("code"));
                orgLevel.setLevelname(rs.getString("name"));
                orgLevel.setDescription(rs.getString("description"));
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }

        return levelList;
    }

    @Override
    public void insertOrgLevel(List<OrgLevel> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into M_ORG_LEVEL(id,name,code,sort_id,description) values (?,?,?,?,?)";
        try {
            connection = SyncConnectionInfoUtil.getMidConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgLevel orgLevel : list) {
                    map = new HashMap();
                    map.put("orgAccountId", new OrgCommon().getOrgAccountId());
                    map.put("code", orgLevel.getLevelcode());
                    map.put("name", orgLevel.getLevelname());
                    map.put("description", orgLevel.getDescription());
                    JSONObject json = client.post("/orgLevel", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            String deptid = ent.getString("id");
                            ps.setString(1, deptid);
                            ps.setString(2, orgLevel.getLevelname());
                            ps.setString(3, orgLevel.getLevelcode());
                            ps.setString(4, ent.getString("sortId"));
                            ps.setString(5, orgLevel.getDescription());
                            ps.addBatch();
                        }
                    }
                }
            }
            ps.executeBatch();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }
    }

    @Override
    public List<OrgLevel> queryUpdateOrgLevelList() {
        String sql = "select l.is_enable,l.name,l.code,l.description,m.id from (select * from THIRD_ORG_LEVEL where is_enable <>'0' or is_delete <>'1') l,M_ORG_LEVEL m where l.code=m.code and (l.is_enable<>m.IS_ENABLE or l.name<>m.name or l.code <>m.code or l.description <>m.description)";
        List<OrgLevel> levelList = new ArrayList<>();
        ResultSet rs = null;
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();

            OrgLevel orgLevel = null;
            while (rs.next()) {
                orgLevel = new OrgLevel();
                orgLevel.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgLevel.setLevelcode(rs.getString("code"));
                orgLevel.setLevelname(rs.getString("name"));
                orgLevel.setDescription(rs.getString("description"));
                orgLevel.setLevelid(rs.getString("id"));
                orgLevel.setIsEnable(rs.getString("is_enable"));
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeResultSet(rs);
        }

        return levelList;
    }

    @Override
    public void updateOrgLevel(List<OrgLevel> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        String sql = "update M_ORG_LEVEL set ";
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgLevel orgLevel : list) {
                    map = new HashMap();
                    map.put("id", orgLevel.getLevelid());
                    map.put("name", orgLevel.getLevelname());
                    if (orgLevel.getIsEnable().equals("0")) {
                        map.put("enabled", false);
                    } else {
                        map.put("enabled", true);
                    }
                    map.put("description", orgLevel.getDescription());

                    JSONObject json = client.put("/orgLevel", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sql = sql.concat("name='" + orgLevel.getLevelname() + "', description='" + orgLevel.getDescription() + "',IS_ENABLE='" + orgLevel.getIsEnable() + "'  where id=" + orgLevel.getLevelid());
                        }
                    }
                    SyncConnectionInfoUtil.insertResult(sql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrgLevel> queryDeleteOrgLevel() {
        String sql = "select m.id from (select * from THIRD_ORG_LEVEL where is_enable <>'1' or is_delete <>'0') l,M_ORG_LEVEL m where l.code =m.code union select m.id from M_ORG_LEVEL m where not exists (select * from THIRD_ORG_LEVEL l where l.code=m.code) ";
        List<OrgLevel> levelList = new ArrayList<>();
        ResultSet rs = null;
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgLevel orgLevel = null;
            while (rs.next()) {
                orgLevel = new OrgLevel();
                orgLevel.setLevelid(rs.getString("id"));
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeResultSet(rs);
        }

        return levelList;
    }

    @Override
    public void deleteOrglevel(List<OrgLevel> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgLevel orgLevel : list) {
                    map = new HashMap();
                    map.put("id", orgLevel.getLevelid());
                    map.put("enabled", false);
//                    JSONObject json = client.put("/orgLevel/" + orgLevel.getLevelid() + "/enabled/false", map, JSONObject.class);
                    JSONObject json = client.delete("/orgLevel/" + orgLevel.getLevelid(), map, JSONObject.class);
                    String sql = "delete from M_ORG_LEVEL where ";
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sql = sql.concat("id='" + orgLevel.getLevelid() + "'");
                            SyncConnectionInfoUtil.insertResult(sql);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
