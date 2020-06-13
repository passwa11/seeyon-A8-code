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
                if (null != rs.getString("org_department_id") && !"".equals(rs.getString("org_department_id"))) {
                    orgMember.setOrgDepartmentId(rs.getString("org_department_id"));
                } else {
                    orgMember.setOrgDepartmentId(new OrgCommon().getOrgAccountId());
                }
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
        String sql = "select MO.id,TM.name,TM.code,tm.loginname,TM.org_department_id,TM.post_id,TM.level_id,TM.PHONE,TM.TEL,TM.EMAIL,TM.IS_ENABLE,TM.DESCRIPTION from (select member.id,member.name,member.code,member.loginname,(select u.id from M_ORG_UNIT u where u.CODE=member.ORG_DEPARTMENT_ID) org_department_id,  " +
                "(select m.id from m_org_post m where m.CODE=member.POST_ID) post_id , (select ml.id from M_ORG_LEVEL ml where ml.code=member.level_id) level_id,phone,tel,email,is_enable,description  " +
                "from THIRD_ORG_MEMBER member where IS_DELETE='0' ) tm ,M_ORG_MEMBER mo where tm.id=MO.USERID and  " +
                "(tm.name<> MO.name or tm.LOGINNAME<> MO.LOGINNAME or TM.org_department_id<>MO.org_department_id or TM.post_id<>MO.POST_ID or TM.level_id<>MO.LEVEL_ID  " +
                "or TM.PHONE<>MO.PHONE or TM.tel<>MO.TEL or TM.EMAIL <> MO.EMAIL or TM.IS_ENABLE <> MO.IS_ENABLE or tm.code<>MO.CODE or TM.DESCRIPTION<>MO.DESCRIPTION)";
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
    public void updateMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        try {
            if (null != list && list.size() > 0) {
                Map memberMap = null;
                for (OrgMember member : list) {
                    memberMap = new HashMap();
                    memberMap.put("id", member.getId());
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

                    JSONObject memberJson = client.get("/orgMember/" + member.getId(), JSONObject.class);
                    if (null != memberJson) {
                        JSONObject json = client.put("/orgMember", memberMap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                String sql = "update M_ORG_MEMBER set ";
                                if (member.getName() != null && !"".equals(member.getName())) {
                                    sql = sql + " name = '" + member.getName() + "', ";
                                } else {
                                    sql = sql + " name = '', ";
                                }

                                if (member.getCode() != null && !"".equals(member.getCode())) {
                                    sql = sql + " code = '" + member.getCode() + "', ";
                                } else {
                                    sql = sql + " code = '', ";
                                }

                                if (member.getLoginname() != null && !"".equals(member.getLoginname())) {
                                    sql = sql + " loginname = '" + member.getLoginname() + "', ";
                                } else {
                                    sql = sql + " loginname = '', ";
                                }

                                if (member.getOrgDepartmentId() != null && !"".equals(member.getOrgDepartmentId())) {
                                    sql = sql + " org_department_id = '" + member.getOrgDepartmentId() + "', ";
                                } else {
                                    sql = sql + " org_department_id = '', ";
                                }

                                if (member.getPostId() != null && !"".equals(member.getPostId())) {
                                    sql = sql + " post_id = '" + member.getPostId() + "', ";
                                } else {
                                    sql = sql + " post_id = '', ";
                                }

                                if (member.getLevelId() != null && !"".equals(member.getLevelId())) {
                                    sql = sql + " level_id = '" + member.getLevelId() + "', ";
                                } else {
                                    sql = sql + " level_id = '', ";
                                }

                                if (member.getPhone() != null && !"".equals(member.getPhone())) {
                                    sql = sql + " phone = '" + member.getPhone() + "', ";
                                } else {
                                    sql = sql + " phone = '', ";
                                }

                                if (member.getTel() != null && !"".equals(member.getTel())) {
                                    sql = sql + " tel = '" + member.getTel() + "', ";
                                } else {
                                    sql = sql + " tel = '', ";
                                }
                                if (member.getEmail() != null && !"".equals(member.getEmail())) {
                                    sql = sql + " email = '" + member.getEmail() + "', ";
                                } else {
                                    sql = sql + " email = '', ";
                                }

                                if (member.getIsEnable() != null && !"".equals(member.getIsEnable())) {
                                    sql = sql + " is_enable = '" + member.getIsEnable() + "', ";
                                } else {
                                    sql = sql + " is_enable = '', ";
                                }

                                if (member.getDescription() != null && !"".equals(member.getDescription())) {
                                    sql = sql + " description = '" + member.getDescription() + "' ";
                                } else {
                                    sql = sql + " description = '' ";
                                }

                                sql = sql + " where id = '" + member.getId() + "' ";

                                SyncConnectionInfoUtil.insertResult(sql);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrgMember> queryDeleteMember() {
        String sql = "select m.id from THIRD_ORG_MEMBER t,M_ORG_MEMBER m where t.IS_DELETE ='1' and t.id=m.USERID union select m.id from M_ORG_MEMBER m where not EXISTS (select * from THIRD_ORG_MEMBER t where m.USERID=t.ID)";
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
    public void deleteMember(List<OrgMember> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        try {
            if (null != list && list.size() > 0) {
                Map map = null;
                StringBuffer dsql = null;
                dsql = new StringBuffer();
                dsql.append("delete from M_ORG_MEMBER where id in (0 ");
                for (OrgMember member : list) {
                    map = new HashMap();
                    map.put("id", member.getId());
                    map.put("enabled", false);
                    JSONObject jsonObject = client.put("/orgMember/" + member.getId() + "/enabled/false", map, JSONObject.class);
                    if (null != jsonObject) {
                        if (jsonObject.getBoolean("success")) {
                            dsql.append(",'" + member.getId() + "'");
                        }
                    } else {
                        dsql.append(",'" + member.getId() + "'");
                    }
                }
                dsql.append(")");
                SyncConnectionInfoUtil.insertResult(dsql.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
