package com.seeyon.apps.ext.kydx.dao;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.kydx.po.OrgDept;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
import com.seeyon.apps.ext.kydx.util.TreeUtil;
import com.seeyon.client.CTPRestClient;
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
        sb.append("delete from M_ORG_UNIT where id in (0");
        try {
            connection = SyncConnectionInfoUtil.getMidConnection();
            //获取需要删除的部门
            String qSql = "select m.* from M_ORG_UNIT m where not exists (select 1 from third_ORG_UNIT v where v.code=m.code) UNION select m.* from M_ORG_UNIT m ,third_ORG_UNIT t where m.code=t.code and t.is_delete='1'";
            ps = connection.prepareStatement(qSql);
            res = ps.executeQuery();
            List<OrgDept> deptList = new ArrayList<>();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setId(res.getString("id"));
                deptList.add(orgDept);
            }

            //执行删除操作
            if (null != deptList && deptList.size() > 0) {
                for (OrgDept dept : deptList) {
                    HashMap deptMap = new HashMap();
                    deptMap.put("id", dept.getId());
                    deptMap.put("enabled", false);
                    JSONObject deptJson = JSONObject.fromObject(deptMap);
                    JSONObject json = client.put("orgDepartment/" + dept.getId() + "/enabled/false", deptMap, JSONObject.class);
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
        String sql = " select m.id,t.name,t.code,t.unit,(select mo.id from m_org_unit mo where MO.code=t.unit) parentOaId,t.is_enable from (select * from third_org_unit where is_delete <>'1' ) t,m_org_unit m where t.code=m.code and (t.name <> m.name or t.unit <> m.unit)";
        CTPRestClient client = SyncConnectionInfoUtil.getOARestInfo();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            connection = SyncConnectionInfoUtil.getMidConnection();
            ps = connection.prepareStatement(sql);
            res = ps.executeQuery();
            List<OrgDept> deptList = new ArrayList<>();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setId(res.getString("id"));
                orgDept.setDeptCode(res.getString("code"));
                orgDept.setDeptName(res.getString("name"));
                orgDept.setOrgAccountId(orgCommon.getOrgAccountId());
                orgDept.setDeptParentId(res.getString("unit") == null ? "" : res.getString("unit"));
                if (null != res.getString("parentOaId") && !"".equals(res.getString("parentOaId"))) {
                    orgDept.setParentId(res.getString("parentOaId"));
                }

                String isEnable = res.getString("is_enable");
                if (null != isEnable && !"".equals(isEnable)) {
                    if (isEnable.equals("0")) {
                        orgDept.setDeptEnable(false);
                    }
                }
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
                    deptMap.put("enabled", dept.isDeptEnable());
                    list.add(deptMap);
                    JSONObject deptJson = JSONObject.fromObject(deptMap);
                    JSONObject json = client.put("/orgDepartment", deptMap, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            StringBuffer updateSql = new StringBuffer();
                            updateSql.append("update M_ORG_UNIT set ");
                            if (null != dept.getDeptName() && !"".equals(dept.getDeptName())) {
                                updateSql.append(" name = '" + dept.getDeptName() + "',");
                            } else {
                                updateSql.append(" name = '',");
                            }

                            if (null != dept.getDeptParentId() && !"".equals(dept.getDeptParentId())) {
                                updateSql.append(" unit = '" + dept.getDeptParentId() + "' ");
                            } else {
                                updateSql.append(" unit = '' ");
                            }
                            updateSql.append(" where id = '" + dept.getId() + "' ");
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
        String sql = "select tou.code,tou.name,tou.unit,tou.is_enable from  (select * from THIRD_ORG_UNIT where is_delete <> '1' and unit is not null and unit ='0' ) tou where  not exists (select * from M_ORG_UNIT m where m.code = tou.code)";
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        Statement statement = null;
        ResultSet rs = null;
        List<OrgDept> orgDeptList = new ArrayList<>();
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            OrgDept orgDept = null;
            while (rs.next()) {
                orgDept = new OrgDept();
                orgDept.setDeptCode(rs.getString("code"));
                orgDept.setDeptName(rs.getString("name"));
//                orgDept.setDeptEnable(rs.getString("is_enable"));
                orgDept.setDeptParentId(rs.getString("unit"));
                orgDept.setOrgAccountId(new OrgCommon().getOrgAccountId());
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
        String insertSql = "insert into M_ORG_UNIT(id, name, code, unit, sort_id) values (?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = SyncConnectionInfoUtil.getMidConnection();
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
                                ps.setString(2, dept.getDeptName() != null && !"".equals(dept.getDeptName()) ? dept.getDeptName() : "");
                                ps.setString(3, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");

                                boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId()) && !dept.getDeptParentId().equals(dept.getOrgAccountId());
                                ps.setString(4, flag ? dept.getDeptParentId() : "");
                                String sortId = ent.getString("sortId");
                                ps.setString(5, sortId != null && !sortId.equals("") ? sortId : "");
                                ps.addBatch();
                            }
                        }
                    } else {
                        com.alibaba.fastjson.JSONObject isExist = com.alibaba.fastjson.JSONObject.parseObject(jsonArray.get(0).toString());

                        String deptid = isExist.getString("id");
                        ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                        ps.setString(2, isExist.getString("name"));
                        ps.setString(3, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                        boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId()) && !dept.getDeptParentId().equals(dept.getOrgAccountId());
                        ps.setString(4, dept.getDeptParentId());
                        ps.setString(5, isExist.getString("sortId"));
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
        String sql = "select tou.code,tou.name,tou.is_enable,(select m.id from M_ORG_UNIT m where m.code= tou.UNIT) parent,tou.unit from  (select * from THIRD_ORG_UNIT where is_delete <> '1' and unit is not null and unit <>'0' ) tou where  not exists (select * from M_ORG_UNIT m where m.code = tou.code)";
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement prep = null;
        ResultSet res = null;
        try {
            prep = connection.prepareStatement(sql);
            res = prep.executeQuery();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setDeptCode(res.getString("code"));
                orgDept.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgDept.setDeptName(res.getString("name"));
//                orgDept.setDeptEnable(res.getString("is_enable"));
                orgDept.setDeptParentId(res.getString("unit"));
                orgDept.setParentId(res.getString("parent"));

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
        String insertSql = "insert into M_ORG_UNIT(id,code,name,unit,sort_id) values (?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = SyncConnectionInfoUtil.getMidConnection();
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
                        ps.setString(2, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                        ps.setString(3, dept.getDeptName() != null && !"".equals(dept.getDeptName()) ? dept.getDeptName() : "");
                        boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId()) && !dept.getDeptParentId().equals(dept.getOrgAccountId());
                        ps.setString(4, flag ? dept.getDeptParentId() : "");
                        String sortId = ent.getString("sortId");
                        ps.setString(5, sortId != null && !sortId.equals("") ? sortId : "");
                        ps.executeUpdate();
                    }
                }
            } else {
                com.alibaba.fastjson.JSONObject isExist = com.alibaba.fastjson.JSONObject.parseObject(jsonArray.get(0).toString());

                String deptid = isExist.getString("id");
                ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                ps.setString(2, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                ps.setString(3, isExist.getString("name"));
                boolean flag = dept.getDeptParentId() != null && !"".equals(dept.getDeptParentId()) && !dept.getDeptParentId().equals(dept.getOrgAccountId());
                ps.setString(4, "");
                ps.setString(5, "");
                ps.executeUpdate();
            }
        }
    }
}
