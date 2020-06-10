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
        String sql = "select id, dept_name, dept_code, dept_description, dept_enable, dept_parent_id,unit_id from third_org_dept t " +
                "where not exists (select * from m_org_dept m where m.id=t.id ) and dept_parent_id is  null ";
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
                orgDept.setId(rs.getString("id"));
                orgDept.setDeptCode(rs.getString("dept_code"));
                orgDept.setDeptName(rs.getString("dept_name"));
                orgDept.setDeptDescription(rs.getString("dept_description"));
                orgDept.setDeptEnable(rs.getString("dept_enable"));
                orgDept.setDeptParentId(rs.getString("dept_parent_id"));
                if (null != rs.getString("dept_parent_id") && !"".equals(rs.getString("dept_parent_id"))) {
                    orgDept.setSuperior(rs.getString("dept_parent_id"));
                } else {
                    orgDept.setSuperior(new OrgCommon().getOrgAccountId());
                }
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
        String insertSql = "insert into M_ORG_UNIT(id,code,name,uint,sort_id) values (?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = SyncConnectionInfoUtil.getMidConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(insertSql);
                for (OrgDept dept : list) {
                    Map<String, Object> dmap = new HashMap<>();
                    dmap.put("orgAccountId", dept.getSuperior());
                    dmap.put("code", dept.getDeptCode());
                    dmap.put("name", dept.getDeptName());
                    dmap.put("enabled", dept.getDeptEnable());
                    dmap.put("superiorName", "矿业大学");
                    String parentId = dept.getDeptParentId();
                    if (parentId != null && !parentId.equals("")) {
                        dmap.put("superior", parentId);
                    } else {
                        dmap.put("superior", dept.getSuperior());
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
                                boolean flag = dept.getSuperior() != null && !"".equals(dept.getSuperior()) && !dept.getSuperior().equals(dept.getDeptParentId());
                                ps.setString(4, flag ? dept.getSuperior() : "");
                                String sortId = ent.getString("sortId");
                                ps.setString(5, sortId != null && !sortId.equals("") ? sortId : "");
                                ps.addBatch();
                            }
                        }
                    } else {
                        com.alibaba.fastjson.JSONObject isExist = com.alibaba.fastjson.JSONObject.parseObject(jsonArray.get(0).toString());

                        String deptid = isExist.getString("id");
                        ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                        ps.setString(2, dept.getDeptCode() != null && !"".equals(dept.getDeptCode()) ? dept.getDeptCode() : "");
                        ps.setString(3, "");
                        boolean flag = dept.getSuperior() != null && !"".equals(dept.getSuperior()) && !dept.getSuperior().equals(dept.getDeptParentId());
                        ps.setString(4, dept.getSuperior());
                        ps.setString(5, "");
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

}
