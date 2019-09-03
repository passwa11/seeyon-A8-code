package com.seeyon.apps.ext.zxzyk.dao;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.zxzyk.po.OrgDept;
import com.seeyon.apps.ext.zxzyk.util.SyncConnectionUtil;
import com.seeyon.apps.ext.zxzyk.util.TreeUtil;
import com.seeyon.client.CTPRestClient;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019-7-29.
 */
public class OrgDeptDaoImpl extends OrgCommon implements OrgDeptDao {

    private Logger log = LoggerFactory.getLogger(OrgDeptDaoImpl.class);

    @Override
    public List<OrgDept> queryByFirstDept() {

        List<OrgDept> firstDeptList = new ArrayList<>();
        String sql = "select v.code,v.name,(select m.id from m_org_unit m where m.code = v.uint) parent,v.uint from (select * from V_ORG_UNIT where IS_DELETED <> '1') v  where v.uint is not null and v.uint in('0') and not exists(select 1 from m_org_unit m where m.code = v.code) ";
        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement prep = null;
        ResultSet res = null;
        try {
            prep = connection.prepareStatement(sql);
            res = prep.executeQuery();
            OrgDept orgDept = null;
            String superior = this.getOrgAccountId();
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setDeptcode(res.getString("code"));
                orgDept.setDeptname(res.getString("name"));
                orgDept.setSuperior(res.getString("uint"));
                orgDept.setParentId(res.getString("parent"));
                orgDept.setOrgAccountId(superior);
                firstDeptList.add(orgDept);
            }
        } catch (Exception e) {
            System.out.println("一级部门新增查询数据库关闭异常：" + e.getMessage());
            log.error("一级部门新增查询数据库关闭异常：" + e.getMessage());
        } finally {
            SyncConnectionUtil.closeResultSet(res);
            SyncConnectionUtil.closePrepareStatement(prep);
            SyncConnectionUtil.closeConnection(connection);

        }
        return firstDeptList;
    }

    @Override
    public List<OrgDept> queryByOtherDept(String accountId) {

        List<OrgDept> firstDeptList = new ArrayList<>();
        String sql = "select v.code,v.name,(select m.id from m_org_unit m where m.code = v.uint) parent,v.uint from (select * from V_ORG_UNIT where IS_DELETED <> '1') v  where v.uint is not null and v.uint not in('0')  and not exists(select 1 from m_org_unit m where m.code = v.code)";
        Connection connection = SyncConnectionUtil.getMidConnection();
        PreparedStatement prep = null;
        ResultSet res = null;
        try {
            prep = connection.prepareStatement(sql);
            res = prep.executeQuery();
            OrgDept orgDept = null;
            String superior = this.getOrgAccountId();
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setDeptcode(res.getString("code"));
                orgDept.setDeptname(res.getString("name"));
                orgDept.setOrgAccountId(superior);
                orgDept.setSuperior(res.getString("uint"));
                orgDept.setParentId(res.getString("parent"));
                firstDeptList.add(orgDept);
            }
        } catch (Exception e) {
            System.out.println("非一级部门新增查询异常：" + e.getMessage());
            log.error("非一级部门新增查询异常：" + e.getMessage());
        } finally {
            SyncConnectionUtil.closeResultSet(res);
            SyncConnectionUtil.closePrepareStatement(prep);
            SyncConnectionUtil.closeConnection(connection);

        }
        return firstDeptList;
    }

    @Override
    public void insertFirstDept(List<OrgDept> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into m_org_unit(id,code,name,uint,sort_id) values (?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = SyncConnectionUtil.getMidConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(insertSql);
                for (OrgDept dept : list) {

                    Map<String, Object> dmap = new HashMap<>();
                    dmap.put("orgAccountId", dept.getOrgAccountId());
                    dmap.put("code", dept.getDeptcode());
                    dmap.put("name", dept.getDeptname());
                    dmap.put("enabled", dept.getEnabled());
                    dmap.put("superiorName", "徐医科大");
                    String parentId = dept.getParentId();
                    if (parentId != null && !parentId.equals("")) {
                        dmap.put("superior", parentId);
                    } else {
                        dmap.put("superior", dept.getOrgAccountId());
                    }

                    String isExist2 = client.get("/orgDepartment/code/" + dept.getDeptcode(), String.class);
                    JSONArray jsonArray = JSONArray.parseArray(isExist2);

                    if (0 == jsonArray.size()) {
                        JSONObject json = client.post("/orgDepartment", dmap, JSONObject.class);
                        if (null != json) {
                            if (json.getBoolean("success")) {
                                JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                                String deptid = ent.getString("id");
                                ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                                ps.setString(2, dept.getDeptcode() != null && !"".equals(dept.getDeptcode()) ? dept.getDeptcode() : "");
                                ps.setString(3, dept.getDeptname() != null && !"".equals(dept.getDeptname()) ? dept.getDeptname() : "");
                                boolean flag = dept.getSuperior() != null && !"".equals(dept.getSuperior()) && !dept.getSuperior().equals(dept.getOrgAccountId());
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
                        ps.setString(2, dept.getDeptcode() != null && !"".equals(dept.getDeptcode()) ? dept.getDeptcode() : "");
                        ps.setString(3, "");
                        boolean flag = dept.getSuperior() != null && !"".equals(dept.getSuperior()) && !dept.getSuperior().equals(dept.getOrgAccountId());
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
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }
    }

    @Override
    public void insertOrgDept_new(List<OrgDept> list) {
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        Connection connection = null;
        PreparedStatement ps = null;
        String insertSql = "insert into m_org_unit(id,code,name,uint,sort_id) values (?,?,?,?,?)";
        try {
            if (null != list && list.size() > 0) {
                connection = SyncConnectionUtil.getMidConnection();
                ps = connection.prepareStatement(insertSql);
                try {
                    for (int i = 0; i < list.size(); i++) {
                        List<OrgDept> deptList = queryByOtherDept(new OrgCommon().getOrgAccountId());
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
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

    }

    public void getList(List<OrgDept> list, CTPRestClient client, PreparedStatement ps) throws Exception {
        for (OrgDept dept : list) {
            Map<String, Object> dmap = new HashMap<>();
            dmap.put("orgAccountId", dept.getOrgAccountId());
            dmap.put("code", dept.getDeptcode());
            dmap.put("name", dept.getDeptname());
            dmap.put("enabled", dept.getEnabled());
            dmap.put("superiorName", "徐医科大");
            String parentId = dept.getParentId();
            if (parentId != null && !parentId.equals("")) {
                dmap.put("superior", parentId);
            } else {
                dmap.put("superior", dept.getOrgAccountId());
            }
            String isExist2 = client.get("/orgDepartment/code/" + dept.getDeptcode(), String.class);
            JSONArray jsonArray = JSONArray.parseArray(isExist2);
            if (0 == jsonArray.size()) {
                JSONObject json = client.post("/orgDepartment", dmap, JSONObject.class);
                if (null != json) {
                    if (json.getBoolean("success")) {
                        JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                        String deptid = ent.getString("id");
                        ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                        ps.setString(2, dept.getDeptcode() != null && !"".equals(dept.getDeptcode()) ? dept.getDeptcode() : "");
                        ps.setString(3, dept.getDeptname() != null && !"".equals(dept.getDeptname()) ? dept.getDeptname() : "");
                        boolean flag = dept.getSuperior() != null && !"".equals(dept.getSuperior()) && !dept.getSuperior().equals(dept.getOrgAccountId());
                        ps.setString(4, flag ? dept.getSuperior() : "");
                        String sortId = ent.getString("sortId");
                        ps.setString(5, sortId != null && !sortId.equals("") ? sortId : "");
                        ps.executeUpdate();
                    }
                }
            } else {
                com.alibaba.fastjson.JSONObject isExist = com.alibaba.fastjson.JSONObject.parseObject(jsonArray.get(0).toString());

                String deptid = isExist.getString("id");
                ps.setString(1, deptid != null && !"".equals(deptid) ? deptid : "");
                ps.setString(2, dept.getDeptcode() != null && !"".equals(dept.getDeptcode()) ? dept.getDeptcode() : "");
                ps.setString(3, "");
                boolean flag = dept.getSuperior() != null && !"".equals(dept.getSuperior()) && !dept.getSuperior().equals(dept.getOrgAccountId());
                ps.setString(4, "");
                ps.setString(5, "");
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void deleteDept() {

        CTPRestClient client = SyncConnectionUtil.getOaRest();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        StringBuffer sb = new StringBuffer();
        sb.append("delete from m_org_unit where id in (0");
        try {
            connection = SyncConnectionUtil.getMidConnection();
            //获取需要删除的部门
            String qSql = "select m.* from M_ORG_UNIT m where 1=1 and  not exists (select 1 from V_ORG_UNIT v where v.code=m.code)";
            ps = connection.prepareStatement(qSql);
            res = ps.executeQuery();
            List<OrgDept> deptList = new ArrayList<>();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setParentId(res.getString("id"));
                orgDept.setDeptid(res.getString("id"));
                deptList.add(orgDept);
            }

            //执行删除操作
            if (null != deptList && deptList.size() > 0) {
                for (OrgDept dept : deptList) {
                    HashMap deptMap = new HashMap();
                    deptMap.put("id", dept.getDeptid());
                    deptMap.put("enabled", false);
                    JSONObject deptJson = JSONObject.fromObject(deptMap);
                    JSONObject json = client.put("orgDepartment/" + dept.getParentId() + "/enabled/false", deptMap, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            sb.append("," + dept.getDeptid());
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
            SyncConnectionUtil.closeResultSet(res);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }

    }

    @Override
    public void updateOrgDept() {
        OrgCommon orgCommon = new OrgCommon();
        String sql = "select mv.id,mv.code,v2.name,v2.uint,m2.id unitid,v2.IS_ENABLE  from   (select distinct m.id,v.code from V_ORG_UNIT v,m_org_unit m where m.code = v.code     and (nvl(v.name,'~') <> nvl(m.name,'~') or nvl(v.uint,'~') <> nvl(m.uint,'~') or v.IS_ENABLE ='0') ) mv    left join V_ORG_UNIT v2 on mv.code = v2.code   left join m_org_unit m2 on v2.uint = m2.code order by mv.code";
        CTPRestClient client = SyncConnectionUtil.getOaRest();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            connection = SyncConnectionUtil.getMidConnection();
            ps = connection.prepareStatement(sql);
            res = ps.executeQuery();
            List<OrgDept> deptList = new ArrayList<>();
            OrgDept orgDept = null;
            while (res.next()) {
                orgDept = new OrgDept();
                orgDept.setDeptid(res.getString("id"));
                orgDept.setDeptcode(res.getString("code"));
                orgDept.setDeptname(res.getString("name"));
                orgDept.setOrgAccountId(orgCommon.getOrgAccountId());
                orgDept.setUnitcode(res.getString("uint") == null ? "" : res.getString("uint"));
                if (!"".equals(res.getString("unitid")) && !"0".equals(res.getString("unitid"))) {
                    orgDept.setSuperior(res.getString("unitid"));
                } else {
                    orgDept.setSuperior(orgCommon.getOrgAccountId());
                }
                String isEnable = res.getString("is_enable");
                if (null != isEnable && !"".equals(isEnable)) {
                    if (isEnable.equals("0")) {
                        orgDept.setEnabled(false);
                    }
                }
                deptList.add(orgDept);
            }
            List list = new ArrayList();
            if (deptList != null && deptList.size() > 0) {
                for (OrgDept dept : deptList) {
                    HashMap deptMap = new HashMap();
                    deptMap.put("id", dept.getDeptid());
                    deptMap.put("code", dept.getDeptcode());
                    deptMap.put("name", dept.getDeptname());
                    deptMap.put("superior", dept.getSuperior());
                    deptMap.put("enabled", dept.getEnabled());
                    list.add(deptMap);
                    JSONObject deptJson = JSONObject.fromObject(deptMap);
                    JSONObject json = client.put("/orgDepartment", deptMap, JSONObject.class);
                    if (null != json) {
                        if (json.getBoolean("success")) {
                            JSONObject ent = json.getJSONArray("successMsgs").getJSONObject(0).getJSONObject("ent");
                            StringBuffer updateSql = new StringBuffer();
                            updateSql.append("update m_org_unit set ");
                            if (null != dept.getDeptname() && !"".equals(dept.getDeptname())) {
                                updateSql.append(" name = '" + dept.getDeptname() + "',");
                            } else {
                                updateSql.append(" name = '',");
                            }

                            if (null != dept.getUnitcode() && !"".equals(dept.getUnitcode())) {
                                updateSql.append(" uint = '" + dept.getUnitcode() + "' ");
                            } else {
                                updateSql.append(" uint = '' ");
                            }
                            updateSql.append(" where id = '" + dept.getDeptid() + "' ");
                            SyncConnectionUtil.insertResult(updateSql.toString());
                        }
                    }

                }
            }

        } catch (Exception e) {
            log.error("修改部门信息出错了，错误信息：" + e.getMessage());
        } finally {
            SyncConnectionUtil.closeResultSet(res);
            SyncConnectionUtil.closePrepareStatement(ps);
            SyncConnectionUtil.closeConnection(connection);
        }
    }
}
