package com.seeyon.apps.ext.kydx.dao;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.kydx.po.OrgDept;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;
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
                orgDept.setDeptEnable(rs.getString("is_enable"));
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
        String insertSql = "insert into m_org_dept(id, name, code, unit, sort_id) values (?,?,?,?,?)";
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
                    dmap.put("enabled", dept.getDeptEnable());
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
        String sql = "select * from (select id, dept_name, dept_code, dept_description, dept_enable, dept_parent_id,unit_id from third_org_dept where dept_parent_id is not null ) t where not exists (select DEPT_CODE from m_org_dept m where m.DEPT_CODE=t.dept_code ) ";
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement prep = null;
        ResultSet res = null;
        try {
            prep = connection.prepareStatement(sql);
            res = prep.executeQuery();
            OrgDept orgDept = null;
            String superior = new OrgCommon().getOrgAccountId();
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setId(res.getString("id"));
                orgDept.setDeptCode(res.getString("dept_code"));
                orgDept.setDeptName(res.getString("dept_name"));
                orgDept.setDeptDescription(res.getString("dept_description"));
                orgDept.setDeptEnable(res.getString("dept_enable"));
                orgDept.setDeptParentId(res.getString("dept_parent_id"));
                if (null != res.getString("dept_parent_id") && !"".equals(res.getString("dept_parent_id"))) {
                    orgDept.setOrgAccountId(res.getString("dept_parent_id"));
                } else {
                    orgDept.setOrgAccountId(new OrgCommon().getOrgAccountId());
                }
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
}
