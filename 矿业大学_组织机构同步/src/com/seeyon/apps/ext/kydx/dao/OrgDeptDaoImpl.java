package com.seeyon.apps.ext.kydx.dao;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.kydx.po.OrgDept;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
import com.seeyon.apps.ext.kydx.util.TreeUtil;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.util.JDBCAgent;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgDeptDaoImpl implements OrgDeptDao {
    private Logger log = LoggerFactory.getLogger(OrgDeptDaoImpl.class);

    @Override
    public void deleteOrgDept() {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        StringBuffer sb = new StringBuffer();
        sb.append("delete from m_org_unit where oaid in (0");
        try {
            connection = JDBCAgent.getRawConnection();
            //获取需要删除的部门
            String qSql = "select oaid from m_org_unit u where not exists(select * from seeyon_oa_dw d where d.dwid=u.dwid)";
            ps = connection.prepareStatement(qSql);
            res = ps.executeQuery();
            List<OrgDept> deptList = new ArrayList<>();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setId(res.getString("oaid"));
                deptList.add(orgDept);
            }

            //执行删除操作
            if (null != deptList && deptList.size() > 0) {
                for (OrgDept dept : deptList) {
                    HashMap deptMap = new HashMap();
                    deptMap.put("id", dept.getId());
                    JSONObject json = client.delete("orgDepartment/" + dept.getId(),deptMap,JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sb.append("," + dept.getId());
                        }
                    }

                }
                sb.append(")");
                ps = connection.prepareStatement(sb.toString());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(res);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }
    }

    @Override
    public void updateOrgDept() {
        OrgCommon orgCommon = new OrgCommon();
//        String sql = " select m.id,t.name,t.code,t.unit,(select mo.id from m_org_unit mo where MO.code=t.unit) parentOaId,t.is_enable from (select * from third_org_unit where is_delete <>'1' ) t,m_org_unit m where t.code=m.code and (t.name <> m.name or t.unit <> m.unit)";
        String sql = "select d.dwmc,d.dwjc,d.dwh,d.lsdwh,u.oaid,(select mu.oaid from m_org_unit mu where mu.dwh=d.lsdwh) oaParentId ,d.dz_dqzyjg from seeyon_oa_dw d,m_org_unit u where d.dwid=u.dwid and (d.dwmc <>u.dwmc or d.DWJC<>u.dwjc or d.dwh<>u.dwh or d.lsdwh<>u.lsdwh or d.DZ_DQZYJG != ifnull(u.dz_dqzyjg,'1')) ";
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            connection =JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            res = ps.executeQuery();
            List<OrgDept> deptList = new ArrayList<>();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setId(res.getString("oaid"));
                orgDept.setDeptCode(res.getString("dwh"));
                orgDept.setDeptName(res.getString("dwmc"));
                orgDept.setShortName(res.getString("dwjc"));
                orgDept.setOrgAccountId(orgCommon.getOrgAccountId());
                orgDept.setDeptParentId(res.getString("lsdwh") == null ? "" : res.getString("lsdwh"));
                if (null != res.getString("oaParentId") && !"".equals(res.getString("oaParentId"))) {
                    orgDept.setParentId(res.getString("oaParentId"));
                }
                orgDept.setIsUse(res.getString("dz_dqzyjg"));
                orgDept.setDeptEnable(true);
                deptList.add(orgDept);
            }
            List list = new ArrayList();
            if (deptList != null && deptList.size() > 0) {
                for (OrgDept dept : deptList) {
                    HashMap deptMap = new HashMap();
                    deptMap.put("id", dept.getId());
                    deptMap.put("code", dept.getDeptCode());
                    deptMap.put("name", dept.getDeptName());
                    deptMap.put("superior", dept.getParentId());
                    if(null !=dept.getIsUse() && !"".equals(dept.getIsUse()) && "0".equals(dept.getIsUse())){
                        deptMap.put("enabled", false);
                    }else {
                        deptMap.put("enabled", true);
                    }
                    deptMap.put("shortName", dept.getShortName());
                    list.add(deptMap);
                    JSONObject deptJson = JSONObject.fromObject(deptMap);
                    JSONObject json = client.put("/orgDepartment", deptMap, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            StringBuffer updateSql = new StringBuffer();
                            updateSql.append("update m_org_unit set ");
                            if (null != dept.getDeptName() && !"".equals(dept.getDeptName())) {
                                updateSql.append(" dwmc = '" + dept.getDeptName() + "',");
                            } else {
                                updateSql.append(" dwmc = '',");
                            }
                            if (null != dept.getShortName() && !"".equals(dept.getShortName())) {
                                updateSql.append(" dwjc = '" + dept.getShortName() + "',");
                            } else {
                                updateSql.append(" dwjc = '',");
                            }

                            if (null != dept.getIsUse() && !"".equals(dept.getIsUse())) {
                                updateSql.append(" dz_dqzyjg = '" + dept.getIsUse() + "', ");
                            } else {
                                updateSql.append(" dz_dqzyjg = '', ");
                            }

                            if (null != dept.getDeptParentId() && !"".equals(dept.getDeptParentId())) {
                                updateSql.append(" lsdwh = '" + dept.getDeptParentId() + "' ");
                            } else {
                                updateSql.append(" lsdwh = '' ");
                            }
                            updateSql.append(" where oaid = '" + dept.getId() + "' ");
                            SyncConnectionInfoUtil.insertResult(updateSql.toString());
                        }
                    }

                }
            }

        } catch (Exception e) {
            log.error("修改部门信息出错了，错误信息：" + e.getMessage());
        } finally {
            SyncConnectionInfoUtil.closeResultSet(res);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }
    }

    /**
     * 获取一级部门
     *
     * @return
     */
    @Override
    public List<OrgDept> queryFirstOrgDept() {
//        String sql = "select tou.code,tou.name,tou.unit,tou.is_enable from  (select * from THIRD_ORG_UNIT where is_delete <> '1' and unit is not null and unit ='0' ) tou where  not exists (select * from M_ORG_UNIT m where m.code = tou.code)";
        String sql = "select * from (select dwid,dwmc,dwjc,lsdwh,dwh,sfsy,dz_dqzyjg from seeyon_oa_dw where ifnull(dz_dqzyjg,'1')!='0' and LSDWH ='000000' ) d where not EXISTS (select * from m_org_unit u where u.dwid=d.dwid)";
        Connection connection=null;
        Statement statement = null;
        ResultSet rs = null;
        List<OrgDept> orgDeptList = new ArrayList<>();
        try {
            connection = JDBCAgent.getRawConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            OrgDept orgDept = null;
            while (rs.next()) {
                orgDept = new OrgDept();
                orgDept.setDeptCode(rs.getString("dwh"));
                orgDept.setDeptName(rs.getString("dwmc"));
                orgDept.setShortName(rs.getString("dwjc"));
                orgDept.setDeptParentId(rs.getString("lsdwh"));
                orgDept.setDwid(rs.getString("dwid"));
                orgDept.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgDept.setIsUse(rs.getString("dz_dqzyjg"));
                orgDept.setParam1(rs.getString("sfsy"));
                orgDeptList.add(orgDept);
            }

        } catch (Exception e) {
            System.out.println("一级部门新增查询数据库关闭异常：" + e.getMessage());
            log.error("一级部门新增查询数据库关闭异常：" + e.getMessage());
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(null, statement);
            SyncConnectionInfoUtil.closeConnection(connection);

        }
        return orgDeptList;
    }

    @Override
    public void insertOrgDept(List<OrgDept> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into m_org_unit(oaid,dwid,dwmc,dwjc,dwh,lsdwh,sfsy,dz_dqzyjg) values (?,?,?,?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = JDBCAgent.getRawConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(insertSql);
                for (OrgDept dept : list) {
                    Map<String, Object> dmap = new HashMap<>();
                    dmap.put("orgAccountId", dept.getOrgAccountId());
                    dmap.put("code", dept.getDeptCode());
                    dmap.put("name", dept.getDeptName());
                    dmap.put("enabled", dept.isDeptEnable());
                    dmap.put("superiorName", "矿业大学");
                    dmap.put("superior", dept.getOrgAccountId());

                    String isExist2 = client.get("/orgDepartment/code/" + dept.getDeptCode(), String.class);
                    JSONArray jsonArray = JSONArray.parseArray(isExist2);

                    if (0 == jsonArray.size()) {
                        JSONObject json = client.post("/orgDepartment", dmap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                                String deptid = ent.getString("id");
                                ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                                ps.setString(2, dept.getDwid() != null && !"".equals(dept.getDwid()) ? dept.getDwid() : "");
                                ps.setString(3, dept.getDeptName() != null && !"".equals(dept.getDeptName()) ? dept.getDeptName() : "");
                                ps.setString(4, dept.getShortName() != null && !"".equals(dept.getShortName()) ? dept.getShortName() : "");
                                ps.setString(5, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                                boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId()) && !dept.getDeptParentId().equals(dept.getOrgAccountId());
                                ps.setString(6, flag ? dept.getDeptParentId() : "");
                                ps.setString(7, dept.getParam1());
                                ps.setString(8, dept.getIsUse());
                                ps.addBatch();
                            }
                        }
                    } else {
                        com.alibaba.fastjson.JSONObject isExist = com.alibaba.fastjson.JSONObject.parseObject(jsonArray.get(0).toString());

                        String deptid = isExist.getString("id");
                        ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                        ps.setString(2, dept.getDwid() != null && !"".equals(dept.getDwid()) ? dept.getDwid() : "");
                        ps.setString(3, dept.getDeptName() != null && !"".equals(dept.getDeptName()) ? dept.getDeptName() : "");
                        ps.setString(4, dept.getShortName() != null && !"".equals(dept.getShortName()) ? dept.getShortName() : "");
                        ps.setString(5, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                        boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId());
                        ps.setString(6, flag ? dept.getDeptParentId() : "");
                        ps.setString(7, dept.getParam1());
                        ps.setString(8, dept.getIsUse());
                        ps.addBatch();
                        ps.executeBatch();
                        connection.commit();//执行
                    }
                }
                ps.executeBatch();
                connection.commit();//执行
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }
    }

    /**
     * 获取有上级的部门信息
     *
     * @return
     */
    @Override
    public List<OrgDept> queryOtherOrgDept() {
        List<OrgDept> firstDeptList = new ArrayList<>();
//        String sql = "select tou.code,tou.name,tou.is_enable,(select m.id from M_ORG_UNIT m where m.code= tou.UNIT) parent,tou.unit from  (select * from THIRD_ORG_UNIT where is_delete <> '1' and unit is not null and unit <>'0' ) tou where  not exists (select * from M_ORG_UNIT m where m.code = tou.code)";
        String sql = "select dwid,dwmc,dwjc,lsdwh,dwh,(select oaid from m_org_unit u where u.dwh=d.LSDWH) oaParentId,sfsy,dz_dqzyjg from (select * from seeyon_oa_dw where ifnull(dz_dqzyjg,'1')!='0' and lsdwh <>'000000') d where not exists (select * from m_org_unit mu where mu.dwid=d.dwid)";
        Connection connection=null;
        PreparedStatement prep = null;
        ResultSet res = null;
        try {
            connection = JDBCAgent.getRawConnection();
            prep = connection.prepareStatement(sql);
            res = prep.executeQuery();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setDwid(res.getString("dwid"));
                orgDept.setDeptCode(res.getString("dwh"));
                orgDept.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgDept.setDeptName(res.getString("dwmc"));
                orgDept.setShortName(res.getString("dwjc"));
                orgDept.setDeptParentId(res.getString("lsdwh"));
                orgDept.setParentId(res.getString("oaParentId"));
                orgDept.setParam1(res.getString("sfsy"));
                orgDept.setIsUse(res.getString("dz_dqzyjg"));

                firstDeptList.add(orgDept);
            }
        } catch (Exception e) {
            System.out.println("非一级部门新增查询异常：" + e.getMessage());
            log.error("非一级部门新增查询异常：" + e.getMessage());
        } finally {
            SyncConnectionInfoUtil.closeResultSet(res);
            SyncConnectionInfoUtil.closePrepareStatement(prep, null);
            SyncConnectionInfoUtil.closeConnection(connection);

        }
        return firstDeptList;
    }

    @Override
    public void insertOtherOrgDept(List<OrgDept> list) {
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into m_org_unit(oaid,dwid,dwmc,dwjc,dwh,lsdwh,sfsy,dz_dqzyjg) values (?,?,?,?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = JDBCAgent.getRawConnection();
                ps = connection.prepareStatement(insertSql);
                try {
                    for (int i = 0; i < list.size(); i++) {
                        List<OrgDept> deptList = queryOtherOrgDept();
                        if (null != deptList && deptList.size() > 0) {
                            List<OrgDept> handleList = TreeUtil.getRootList(deptList);
                            getList(handleList, client, ps);
                        }

                    }
                } catch (Exception e) {
                    log.error("新增部门信息出错了，错误信息：" + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }
    }

    public void getList(List<OrgDept> list, CTPRestClient client, PreparedStatement ps) throws Exception {
        for (OrgDept dept : list) {
            Map<String, Object> dmap = new HashMap<>();
            dmap.put("orgAccountId", dept.getOrgAccountId());
            dmap.put("code", dept.getDeptCode());
            dmap.put("name", dept.getDeptName());
            dmap.put("enabled", dept.isDeptEnable());
            dmap.put("superiorName", "矿业大学");
            String parentId = dept.getParentId();
            if (parentId != null && !parentId.equals("")) {
                dmap.put("superior", parentId);
            } else {
                dmap.put("superior", dept.getOrgAccountId());
            }
            String isExist2 = client.get("/orgDepartment/code/" + dept.getDeptCode(), String.class);
            JSONArray jsonArray = JSONArray.parseArray(isExist2);
            if (0 == jsonArray.size()) {
                JSONObject json = client.post("/orgDepartment", dmap, JSONObject.class);
                if (null != json) {
                    if (json.getBoolean("success")) {
                        JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                        String deptid = ent.getString("id");

                        ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                        ps.setString(2, dept.getDwid() != null && !"".equals(dept.getDwid()) ? dept.getDwid() : "");
                        ps.setString(3, dept.getDeptName() != null && !"".equals(dept.getDeptName()) ? dept.getDeptName() : "");
                        ps.setString(4, dept.getShortName() != null && !"".equals(dept.getShortName()) ? dept.getShortName() : "");
                        ps.setString(5, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                        boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId());
                        ps.setString(6, flag ? dept.getDeptParentId() : "");
                        ps.setString(7, dept.getParam1());
                        ps.setString(8, dept.getIsUse());

                        ps.executeUpdate();
                    }
                }
            } else {
                com.alibaba.fastjson.JSONObject isExist = com.alibaba.fastjson.JSONObject.parseObject(jsonArray.get(0).toString());

                String deptid = isExist.getString("id");
                ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                ps.setString(2, dept.getDwid() != null && !"".equals(dept.getDwid()) ? dept.getDwid() : "");
                ps.setString(3, isExist.getString("name"));
                ps.setString(4, isExist.getString("shortName"));
                boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId());
                ps.setString(5, isExist.getString("code"));
                ps.setString(6, flag ? dept.getDeptParentId() : "");
                ps.setString(7, dept.getParam1());
                ps.setString(8, dept.getIsUse());
                ps.executeUpdate();
            }
        }
    }
}
