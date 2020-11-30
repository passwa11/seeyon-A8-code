package com.seeyon.apps.ext.zxzyk.dao;

import com.seeyon.apps.ext.zxzyk.po.OrgLevel;
import com.seeyon.apps.ext.zxzyk.util.SyncConnectionUtil;
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
    public List<OrgLevel> queryOrgLevel() {
        List<OrgLevel> levelList = new ArrayList<>();
        String sql = "select vl.id,vl.name,vl.code,vl.is_enable from V_ORG_LEVEL vl where not exists (select * from M_ORG_LEVEL ml where ml.code=vl.code)";
        Connection connection = SyncConnectionUtil.getMidConnection();
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
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

        return levelList;
    }

    @Override
    public void insertOrgLevel(List<OrgLevel> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into M_ORG_LEVEL(id,name,code,description) values (?,?,?,?)";
        try {
            connection = SyncConnectionUtil.getMidConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgLevel orgLevel : list) {
                    map = new HashMap();
                    map.put("orgAccountId", new OrgCommon().getOrgAccountId());
                    map.put("code", orgLevel.getLevelcode());
                    map.put("name", orgLevel.getLevelname());
                    JSONObject json = client.post("/orgLevel", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            String deptid = ent.getString("id");
                            ps.setString(1, deptid);
                            ps.setString(2, orgLevel.getLevelname());
                            ps.setString(3, orgLevel.getLevelcode());
                            ps.setString(4, "");
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
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }
    }

    @Override
    public List<OrgLevel> queryChangerLevel() {
        List<OrgLevel> levelList = new ArrayList<>();
        String sql = "select VL.CODE,VL.name,ML.ID from V_ORG_LEVEL vl,M_ORG_LEVEL ml where VL.code =ML.code and VL.name <> ML.NAME";
        ResultSet rs = null;
        Connection connection = SyncConnectionUtil.getMidConnection();
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
                orgLevel.setLevelid(rs.getString("id"));
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

        return levelList;
    }

    @Override
    public void updateOrgLevel(List<OrgLevel> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        String sql = "update M_ORG_LEVEL set ";
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgLevel orgLevel : list) {
                    map = new HashMap();
                    map.put("id", orgLevel.getLevelid());
                    map.put("name", orgLevel.getLevelname());

                    JSONObject json = client.put("/orgLevel", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sql = sql.concat("name='" + orgLevel.getLevelname() + "' where id=" + orgLevel.getLevelid());
                        }
                    }
                    SyncConnectionUtil.insertResult(sql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    @Override
    public List<OrgLevel> queryNotExistLevel() {
        List<OrgLevel> levelList = new ArrayList<>();
        String sql = "select * from M_ORG_LEVEL ml where not EXISTS (select * from V_ORG_LEVEL vl where VL.CODE =ML.code)";
        ResultSet rs = null;
        Connection connection = SyncConnectionUtil.getMidConnection();
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
                orgLevel.setLevelid(rs.getString("id"));
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

        return levelList;
    }

    @Override
    public void deleteOrgLevel(List<OrgLevel> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        String sql = "delete from M_ORG_LEVEL where ";
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgLevel orgLevel : list) {
                    map = new HashMap();
                    map.put("id", orgLevel.getLevelid());
                    map.put("enabled", false);

                    JSONObject json = client.put("/orgLevel/" + orgLevel.getLevelid() + "/enabled/false", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sql = sql.concat("id='" + orgLevel.getLevelid() + "'");
                            SyncConnectionUtil.insertResult(sql);

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
}
