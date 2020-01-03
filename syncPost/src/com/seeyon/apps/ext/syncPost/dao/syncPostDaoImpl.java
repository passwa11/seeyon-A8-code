package com.seeyon.apps.ext.syncPost.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.ext.syncPost.po.MidPost;
import com.seeyon.apps.ext.syncPost.po.SyncOrgPost;
import com.seeyon.apps.ext.syncPost.util.JDBCUtil;
import com.seeyon.apps.ext.syncPost.util.OrgCommon;
import com.seeyon.apps.ext.syncPost.util.SyncConnectionUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.util.DBAgent;
import net.sf.json.JSONObject;

public class syncPostDaoImpl implements syncPostDao {

    @Override
    public List<SyncOrgPost> queryNotExitPost() {
        String sql = "select MP.*, 0 POST_ID from mid_post@gcxylink mp where MP.t_id not in (select POST_CODE from MID_POST)";
        return this.generalMethod(sql);
    }

//    @Override
//    public List<SyncOrgPost> queryChangePost() {
//        String sql = "select l.*,m.post_id from MID_POST m,MID_POST@gcxylink l where m.POST_CODE= l.POST_CODE and ( m.POST_NAME<> l.POST_NAME or m.POST_DESC <> l.POST_DESC)";
//        return this.generalMethod(sql);
//    }

    public List<SyncOrgPost> generalMethod(String sql) {
        List<SyncOrgPost> levelList = new ArrayList<>();
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        try {
            SyncOrgPost post = null;
            for (Map<String, Object> map : list) {
                post = new SyncOrgPost();
                post.settId(map.get("t_id") != null ? ((BigDecimal) map.get("t_id")).longValue() : 0);
                post.setAccountId(map.get("account_id") != null ? ((BigDecimal) map.get("account_id")).longValue() : 0);
                post.setSortId(map.get("sort_id") != null ? ((BigDecimal) map.get("sort_id")).longValue() : 0);
                post.setPostCode(((String) map.get("t_id")));
                post.setPostName((String) map.get("post_name"));
                post.setPostDesc((String) map.get("post_desc"));
                post.setOperationType(map.get("operation_type") != null ? ((BigDecimal) map.get("operation_type")).longValue() : 0);
                post.setOaType(map.get("oa_type") != null ? ((BigDecimal) map.get("oa_type")).longValue() : 0);
                post.setPostId(map.get("post_id") != null ? ((BigDecimal) map.get("post_id")).longValue() : 0);
                levelList.add(post);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return levelList;
    }


    @Override
    public void insertPost(List<SyncOrgPost> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();

        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String thirtySql = "update mid_post set oa_id=?,post_code=? where t_id=?";
            ps = connection.prepareStatement(thirtySql);
            if (null != list && list.size() > 0) {
                Map map = null;
                MidPost mp = null;
                for (SyncOrgPost post : list) {
                    map = new HashMap();
                    map.put("orgAccountId", new OrgCommon().getOrgAccountId());
                    map.put("code", post.getPostCode());
                    map.put("name", post.getPostName());
                    map.put("description", post.getPostDesc());
                    map.put("typeId", 1);
                    JSONObject json = client.post("/orgPost", map, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            Long postId = ent.getLong("id");

                            //向oa中间库中插入数据
                            mp = new MidPost();
                            mp.setId(Long.parseLong(post.getPostCode()));
                            mp.setAccountId(post.getAccountId());
                            mp.setPostCode(post.getPostCode());
                            mp.setPostName(post.getPostName());
                            mp.setPostDesc(post.getPostDesc());
                            mp.setPostId(postId);
                            DBAgent.save(mp);

                            //向第三方中间库中写数据
                            ps.setObject(1, postId);
                            ps.setObject(2, post.getPostCode());
                            ps.setObject(3, post.gettId());
                            ps.executeUpdate();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionUtil.closeResultSet(rs);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }
    }

//    @Override
//    public void updatePost(List<SyncOrgPost> list) {
//        CTPRestClient client = SyncConnectionUtil.getOaRest();
//        try {
//            if (null != list && list.size() > 0) {
//                Map map = null;
//                MidPost mp = null;
//                List updateList = new ArrayList();
//                List<MidPost> midPostList = new ArrayList<>();
//                for (SyncOrgPost post : list) {
//                    map = new HashMap();
//                    map.put("id", post.getPostId());
//                    map.put("code", post.getPostCode());
//                    map.put("name", post.getPostName());
//                    map.put("description", post.getPostDesc());
//                    updateList.add(map);
//
//                    mp = new MidPost();
//                    mp.setId(post.gettId());
//                    mp.setPostName(post.getPostName());
//                    mp.setPostDesc(post.getPostDesc());
//                    mp.setPostCode(post.getPostCode());
//                    mp.setPostId(post.getPostId());
//                    midPostList.add(mp);
//                }
//                JSONObject json = client.post("/orgPost/updatePosts", updateList, JSONObject.class);
//                if (null != json) {
//                    if (json.getBoolean("success")) {
//                        for (MidPost n : midPostList) {
//                            String sql = "update mid_post set POST_NAME='" + n.getPostName() + "',POST_DESC='" + n.getPostDesc() + "' where POST_ID=" + n.getPostId();
//                            JDBCUtil.doUpdate(sql);
//                        }
//
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
