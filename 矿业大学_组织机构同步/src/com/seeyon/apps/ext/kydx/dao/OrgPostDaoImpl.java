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
        String sql = "insert into M_ORG_POST (id,code,name,DESCRIPTION) values(?,?,?,?)";
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
        String sql = "";
        return null;
    }

    @Override
    public void updatePost(List<OrgPost> list) {

    }

    @Override
    public List<OrgPost> queryDeletePost() {
        return null;
    }

    @Override
    public void deletePost(List<OrgPost> list) {

    }
}
