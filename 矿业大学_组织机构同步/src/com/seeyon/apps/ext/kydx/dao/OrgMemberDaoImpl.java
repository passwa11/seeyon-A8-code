package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.CtpOrgUser;
import com.seeyon.apps.ext.kydx.po.OrgMember;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.util.DBAgent;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class OrgMemberDaoImpl implements OrgMemberDao {

    @Override
    public List<OrgMember> queryInsertMember() {
        String sql = "select * from (select member.id,member.name,member.code,member.loginname," +
                "(select u.id from M_ORG_UNIT u where u.CODE=member.ORG_DEPARTMENT_ID) org_department_id, " +
                "(select m.id from m_org_post m where m.CODE=member.POST_ID) post_id , " +
                "(select ml.id from M_ORG_LEVEL ml where ml.code=member.level_id) level_id,phone,tel,email,is_enable,description from THIRD_ORG_MEMBER member where IS_ENABLE ='1' and IS_DELETE='0' ) " +
                " tmember where not exists (select * from M_ORG_MEMBER mm where TMEMBER.id=mm.userid)";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setId(rs.getString("id"));
                orgMember.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgMember.setName(rs.getString("name"));
                orgMember.setLoginname(rs.getString("loginname"));
                orgMember.setOrgDepartmentId(rs.getString("org_department_id"));
                orgMember.setPostId(rs.getString("post_id"));
                orgMember.setLevelId(rs.getString("level_id"));
                orgMember.setPhone(rs.getString("phone"));
                orgMember.setTel(rs.getString("tel"));
                orgMember.setEmail(rs.getString("email"));
                orgMember.setIsEnable(rs.getString("is_enable"));
                orgMember.setCode(rs.getString("code"));
                orgMember.setDescription(rs.getString("description"));
                memberList.add(orgMember);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }

        return memberList;
    }

    @Override
    public void insertMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into m_org_member (id, userid, name, loginname, org_department_id, post_id, level_id, phone, tel, email, is_enable, is_delete,code,description) " +
                "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            connection = SyncConnectionInfoUtil.getMidConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);

            if (null != list && list.size() > 0) {
                Map memberMap = null;
                for (OrgMember member : list) {
                    memberMap = new HashMap();
                    memberMap.put("name", member.getName());
                    memberMap.put("loginName", member.getLoginname());
                    memberMap.put("orgAccountId", member.getOrgAccountId());
                    memberMap.put("orgLevelId", member.getLevelId());
                    memberMap.put("orgPostId", member.getPostId());
                    memberMap.put("orgDepartmentId", member.getOrgDepartmentId());
                    memberMap.put("code", member.getCode());
                    memberMap.put("enabled", member.getIsEnable());
                    memberMap.put("telNumber", member.getPhone());
                    memberMap.put("officeNum", member.getTel());
                    memberMap.put("description", member.getDescription());

                    JSONObject memberJson = client.get("/orgMember?loginName=" + member.getLoginname(), JSONObject.class);
                    if (null == memberJson) {
                        JSONObject json = client.post("/orgMember", memberMap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                                String userid = ent.getString("id");
                                ps.setString(1, userid);
                                ps.setString(2, member.getId());
                                ps.setString(3, member.getName());
                                ps.setString(4, member.getLoginname());
                                ps.setString(5, member.getOrgDepartmentId());
                                ps.setString(6, member.getPostId());
                                ps.setString(7, member.getLevelId());
                                ps.setString(8, member.getPhone());
                                ps.setString(9, member.getTel());
                                ps.setString(10, member.getEmail());
                                ps.setString(11, member.getIsEnable());
                                ps.setString(12, member.getIsDelete());
                                ps.setString(13, member.getCode());
                                ps.setString(14, member.getDescription());
                                ps.addBatch();

                                CtpOrgUser orgUser = new CtpOrgUser();
                                orgUser.setId(Long.parseLong(userid));
                                orgUser.setType("ldap.member.openLdap");
                                orgUser.setLoginName(ent.getString("loginName"));
                                orgUser.setExLoginName(member.getLoginname());
                                orgUser.setExPassword("1");
                                orgUser.setExId(member.getId());
                                orgUser.setExUserId(member.getId());
                                orgUser.setMemberId(Long.parseLong(userid));
                                orgUser.setActionTime(new Date());
                                orgUser.setDescription("");
                                orgUser.setExUnitCode("uid=" + ent.getString("loginName"));
                                DBAgent.save(orgUser);
                            }
                        }
                    } else {
                        String userid = memberJson.getString("id");
                        ps.setString(1, userid);
                        ps.setString(2, member.getId());
                        ps.setString(3, member.getName());
                        ps.setString(4, member.getLoginname());
                        ps.setString(5, member.getOrgDepartmentId());
                        ps.setString(6, member.getPostId());
                        ps.setString(7, member.getLevelId());
                        ps.setString(8, member.getPhone());
                        ps.setString(9, member.getTel());
                        ps.setString(10, member.getEmail());
                        ps.setString(11, member.getIsEnable());
                        ps.setString(12, member.getIsDelete());
                        ps.setString(13, member.getCode());
                        ps.setString(14, member.getDescription());

                        ps.addBatch();
                        ps.executeBatch();
                        connection.commit();
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
    public List<OrgMember> queryUpdateMember() {
        return null;
    }

    @Override
    public void updateMember(List<OrgMember> list) {

    }

    @Override
    public List<OrgMember> queryDeleteMember() {
        return null;
    }

    @Override
    public void deleteMember(List<OrgMember> list) {

    }
}
