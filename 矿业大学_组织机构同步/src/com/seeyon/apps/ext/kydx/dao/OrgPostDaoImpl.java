package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.OrgLevel;
import com.seeyon.apps.ext.kydx.po.OrgPost;
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

public class OrgPostDaoImpl implements OrgPostDao {
    @Override
    public List<OrgPost> queryInsertPost() {
        String sql = "select t.code,t.name,t.description from third_org_post t where not exists (select * from M_ORG_POST m where m.code=t.code) and (t.is_enable <> '0' and t.is_delete <> '1')";
        List<OrgPost> levelList = new ArrayList<>();
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgPost orgPost = null;
            while (rs.next()) {
                orgPost = new OrgPost();
                orgPost.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgPost.setPostcode(rs.getString("code"));
                orgPost.setPostname(rs.getString("name"));
                orgPost.setDescription(rs.getString("description"));
                levelList.add(orgPost);
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
    public void insertPost(List<OrgPost> list) {
        String sql = "insert into M_ORG_POST (id,code,name,DESCRIPTION,is_enable,is_delete) values(?,?,?,?,?,?)";
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = SyncConnectionInfoUtil.getMidConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgPost orgPost : list) {
                    map = new HashMap();
                    map.put("orgAccountId", new OrgCommon().getOrgAccountId());
                    map.put("code", orgPost.getPostcode());
                    map.put("name", orgPost.getPostname());
                    map.put("description", orgPost.getDescription());
                    JSONObject json = client.post("/orgPost", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            String deptid = ent.getString("id");
                            ps.setString(1, deptid);
                            ps.setString(2, orgPost.getPostcode());
                            ps.setString(3, orgPost.getPostname());
                            ps.setString(4, orgPost.getDescription());
                            ps.setString(5, "1");
                            ps.setString(6, "0");
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
    public List<OrgPost> queryUpdatePost() {
        String sql = "select t.code,t.name,t.DESCRIPTION,m.id,t.IS_ENABLE from THIRD_ORG_POST t ,m_org_post m where t.code =m.code and (t.IS_ENABLE <>m.IS_ENABLE or t.name <> m.name or t.DESCRIPTION <> m.DESCRIPTION) ";
        List<OrgPost> postList = new ArrayList<>();
        ResultSet rs = null;
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();

            OrgPost orgPost = null;
            while (rs.next()) {
                orgPost = new OrgPost();
                orgPost.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgPost.setPostcode(rs.getString("code"));
                orgPost.setPostname(rs.getString("name"));
                orgPost.setDescription(rs.getString("description"));
                orgPost.setPostid(rs.getString("id"));
                orgPost.setIsEnable(rs.getString("is_enable"));
                postList.add(orgPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeResultSet(rs);
        }

        return postList;
    }

    @Override
    public void updatePost(List<OrgPost> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        String sql = "update M_ORG_POST set ";
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgPost orgPost : list) {
                    map = new HashMap();
                    map.put("id", orgPost.getPostid());
                    map.put("name", orgPost.getPostname());
                    if (orgPost.getIsEnable().equals("1")) {
                        map.put("enabled", true);
                    } else {
                        map.put("enabled", false);
                    }
                    map.put("description", orgPost.getDescription());

                    JSONObject json = client.put("/orgPost", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sql = sql.concat("name='" + orgPost.getPostname() + "', description='" + orgPost.getDescription() + "' ,is_enable='" + orgPost.getIsEnable() + "'  where id=" + orgPost.getPostid());
                            SyncConnectionInfoUtil.insertResult(sql);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrgPost> queryDeletePost() {
        String sql = "select m.id from  m_org_post m where not exists (select * from THIRD_ORG_POST t where m.code =t.code) union select m.id from THIRD_ORG_POST t,m_org_post m where t.code =m.code and (t.IS_DELETE<>'0')";
        List<OrgPost> postList = new ArrayList<>();
        ResultSet rs = null;
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgPost orgPost = null;
            while (rs.next()) {
                orgPost = new OrgPost();
                orgPost.setPostid(rs.getString("id"));
                postList.add(orgPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeResultSet(rs);
        }

        return postList;
    }

    @Override
    public void deletePost(List<OrgPost> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                for (OrgPost orgPost : list) {
                    map = new HashMap();
                    map.put("id", orgPost.getPostid());
                    JSONObject json = client.delete("/orgPost/" + orgPost.getPostid(), map, JSONObject.class);
                    String sql = "delete from m_org_post where ";
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sql = sql.concat("id='" + orgPost.getPostid() + "'");
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
