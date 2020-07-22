package com.seeyon.apps.ext.kydx.dao;

import com.seeyon.apps.ext.kydx.po.CtpOrgUser;
import com.seeyon.apps.ext.kydx.po.OrgMember;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.JDBCAgent;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class OrgMemberDaoImpl implements OrgMemberDao {

    @Override
    public List<OrgMember> queryInsertMember() {
        String sql = "select * from (select u.jzgid,u.xm,u.gh,u.yddh,u.bglxdh,u.dzxx,u.grjj,u.yrfsdm,(select m.oaid from m_org_unit m where m.dwh=u.dwh) oaUnitId,u.dwh from (select * from seeyon_oa_jzgjbxx where dwh is not null)  u) w where not exists (select * from m_org_member m where w.jzgid=m.jzgid)";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection =null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection= JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setId(rs.getString("jzgid"));
                orgMember.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgMember.setName(rs.getString("xm"));
//                orgMember.setLoginname(rs.getString("loginname"));
                if (null != rs.getString("oaUnitId") && !"".equals(rs.getString("oaUnitId"))) {
                    orgMember.setOrgDepartmentId(rs.getString("oaUnitId"));
                } else {
                    orgMember.setOrgDepartmentId(new OrgCommon().getOrgAccountId());
                }
                orgMember.setPostId(new OrgCommon().getOrgPostId());
                orgMember.setLevelId(new OrgCommon().getOrgLevelId());
                orgMember.setPhone(rs.getString("yddh"));
                orgMember.setTel(rs.getString("bglxdh"));
                orgMember.setEmail(rs.getString("dzxx"));
//                orgMember.setIsEnable(rs.getString("is_enable"));
                orgMember.setCode(rs.getString("gh"));
                orgMember.setDescription(rs.getString("grjj"));
                orgMember.setSubDepartmentId(rs.getString("dwh"));
                orgMember.setYrfsdm(rs.getString("yrfsdm"));
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
        String insertSql = "insert into m_org_member (memberId,jzgid,xm,gh,yddh,bglxdh,dzxx,grjj,oaUnitId,dwh,yrfsdm) values(?,?,?,?,?,?,?,?,?,?,?)";
        try {
            connection = JDBCAgent.getRawConnection();
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertSql);

            if (null != list && list.size() > 0) {
                Map memberMap = null;
                for (OrgMember member : list) {
                    memberMap = new HashMap();
                    memberMap.put("name", member.getName());
                    memberMap.put("loginName", member.getCode());
                    memberMap.put("orgAccountId", member.getOrgAccountId());
                    memberMap.put("orgLevelId", member.getLevelId());
                    memberMap.put("orgPostId", member.getPostId());
                    memberMap.put("orgDepartmentId", member.getOrgDepartmentId());
                    memberMap.put("code", member.getCode());
//                    memberMap.put("enabled", member.getIsEnable());
                    memberMap.put("telNumber", null == member.getPhone() ? "" : member.getPhone());
                    memberMap.put("officeNum", null == member.getTel() ? "" : member.getTel());

                    memberMap.put("description", null == member.getDescription() ? "" : member.getDescription());

                    JSONObject memberJson = client.get("/orgMember?loginName=" + member.getCode(), JSONObject.class);
                    if (null == memberJson) {
                        JSONObject json = client.post("/orgMember", memberMap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                                String userid = ent.getString("id");
                                ps.setString(1, userid);
                                ps.setString(2, member.getId());
                                ps.setString(3, member.getName());
                                ps.setString(4, member.getCode());
                                ps.setString(5, member.getPhone());
                                ps.setString(6, member.getTel());
                                ps.setString(7, member.getEmail());
                                ps.setString(8, member.getDescription());
                                ps.setString(9, member.getOrgDepartmentId());
                                ps.setString(10, member.getSubDepartmentId());
                                ps.setString(11, member.getYrfsdm());
                                ps.addBatch();

                                CtpOrgUser orgUser = new CtpOrgUser();
                                orgUser.setId(Long.parseLong(userid));
                                orgUser.setType("ldap.member.openLdap");
                                orgUser.setLoginName(ent.getString("loginName"));
                                orgUser.setExLoginName(member.getCode());
                                orgUser.setExPassword("1");
                                orgUser.setExId(member.getId());
                                orgUser.setExUserId(member.getId());
                                orgUser.setMemberId(Long.parseLong(userid));
                                orgUser.setActionTime(new Date());
                                orgUser.setDescription("");
                                orgUser.setExUnitCode("uid=" + ent.getString("loginName")+",ou="+member.getYrfsdm());
                                DBAgent.save(orgUser);
                            }
                        }
                    } else {
                        String userid = memberJson.getString("id");
                        ps.setString(1, userid);
                        ps.setString(2, member.getId());
                        ps.setString(3, member.getName());
                        ps.setString(4, member.getCode());
                        ps.setString(5, member.getPhone());
                        ps.setString(6, member.getTel());
                        ps.setString(7, member.getEmail());
                        ps.setString(8, member.getDescription());
                        ps.setString(9, member.getOrgDepartmentId());
                        ps.setString(10, member.getSubDepartmentId());
                        ps.setString(11, member.getYrfsdm());

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
        String sql = "select j.yrfsdm,j.jzgid,j.xm,j.gh,j.dwh,j.yddh,j.bglxdh,j.dzxx,j.grjj,m.memberId,(select u.oaid from m_org_unit u where u.dwh=j.dwh) unitId from " +
                "(select oa.* from seeyon_oa_jzgjbxx oa where oa.dwh is not null) j,m_org_member m " +
                "where j.gh=m.gh and (j.xm <> m.xm or j.dwh<>m.dwh  or j.yddh <>m.yddh or j.bglxdh <>m.bglxdh or j.dzxx<>m.dzxx or j.grjj<>m.grjj or j.yrfsdm<>m.yrfsdm) ";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection=JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setId(rs.getString("memberId"));
                orgMember.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgMember.setName(rs.getString("xm"));
                orgMember.setLoginname(rs.getString("gh"));
                orgMember.setOrgDepartmentId(rs.getString("unitId"));
                orgMember.setPostId(new OrgCommon().getOrgPostId());
                orgMember.setLevelId(new OrgCommon().getOrgLevelId());
                orgMember.setPhone(rs.getString("yddh"));
                orgMember.setTel(rs.getString("bglxdh"));
                orgMember.setEmail(rs.getString("dzxx"));
//                orgMember.setIsEnable(rs.getString("is_enable"));
                orgMember.setCode(rs.getString("gh"));
                orgMember.setDescription(rs.getString("grjj"));
                orgMember.setSubDepartmentId(rs.getString("dwh"));
                orgMember.setYrfsdm(rs.getString("yrfsdm"));
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
                                String sql = "update m_org_member set ";
                                if (member.getName() != null && !"".equals(member.getName())) {
                                    sql = sql + " xm = '" + member.getName() + "', ";
                                } else {
                                    sql = sql + " xm = '', ";
                                }

//                                if (member.getCode() != null && !"".equals(member.getCode())) {
//                                    sql = sql + " code = '" + member.getCode() + "', ";
//                                } else {
//                                    sql = sql + " code = '', ";
//                                }

//                                if (member.getLoginname() != null && !"".equals(member.getLoginname())) {
//                                    sql = sql + " loginname = '" + member.getLoginname() + "', ";
//                                } else {
//                                    sql = sql + " loginname = '', ";
//                                }

                                if (member.getPhone() != null && !"".equals(member.getPhone())) {
                                    sql = sql + " yddh = '" + member.getPhone() + "', ";
                                } else {
                                    sql = sql + " yddh = '', ";
                                }

                                if (member.getTel() != null && !"".equals(member.getTel())) {
                                    sql = sql + " bglxdh = '" + member.getTel() + "', ";
                                } else {
                                    sql = sql + " bglxdh = '', ";
                                }

                                if (member.getEmail() != null && !"".equals(member.getEmail())) {
                                    sql = sql + " dzxx = '" + member.getEmail() + "', ";
                                } else {
                                    sql = sql + " dzxx = '', ";
                                }

                                if (member.getDescription() != null && !"".equals(member.getDescription())) {
                                    sql = sql + " grjj = '" + member.getDescription() + "' ,";
                                } else {
                                    sql = sql + " grjj = '', ";
                                }

                                if (member.getOrgDepartmentId() != null && !"".equals(member.getOrgDepartmentId())) {
                                    sql = sql + " oaUnitId = '" + member.getOrgDepartmentId() + "' ,";
                                } else {
                                    sql = sql + " oaUnitId = '', ";
                                }

                                if (member.getSubDepartmentId() != null && !"".equals(member.getSubDepartmentId())) {
                                    sql = sql + " dwh = '" + member.getSubDepartmentId() + "' ";
                                } else {
                                    sql = sql + " dwh = '' ";
                                }

                                sql = sql + " where memberId = '" + member.getId() + "' ";

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
        String sql = "select m.memberId from m_org_member m where not exists(select * from seeyon_oa_jzgjbxx j where j.gh=m.gh)";
        List<OrgMember> memberList = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection=JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgMember orgMember = null;
            while (rs.next()) {
                orgMember = new OrgMember();
                orgMember.setId(rs.getString("memberId"));
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
                dsql.append("delete from M_ORG_MEMBER where memberId in (0 ");
                for (OrgMember member : list) {
                    map = new HashMap();
                    map.put("id", member.getId());
//                    map.put("enabled", false);
//                    JSONObject jsonObject = client.put("/orgMember/" + member.getId() + "/enabled/false", map, JSONObject.class);
                    JSONObject jsonObject = client.delete("/orgMember/" + member.getId(), map, JSONObject.class);
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
