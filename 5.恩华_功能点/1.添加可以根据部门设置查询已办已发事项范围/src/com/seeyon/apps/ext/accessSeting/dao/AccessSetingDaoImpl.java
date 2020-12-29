package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.TempTemplateStop;
import com.seeyon.apps.ext.accessSeting.po.ZorgMember;
import com.seeyon.apps.ext.accessSeting.util.JDBCUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.JDBCAgent;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessSetingDaoImpl implements AccessSetingDao {

    //****禁用模板流程*******************************************************


    @Override
    public List<TempTemplateStop> getStatusIsZero() {
        Map<String, Object> map = new HashMap<>();
        map.put("p1", "0");
        return DBAgent.find("from TempTemplateStop where p1=:p1 ", map);
//        return DBAgent.find("from TempTemplateStop where 1=1 ");
    }

    @Override
    public List<Map<String, String>> getTemplateInfos(Map<String, String> params) {
        String sql = "select w3.* from (select ct.*,nvl(s.P1,'-') p1 from (select c.id catid,c.name,t.subject,t.id from CTP_TEMPLATE_CATEGORY c, (select * from CTP_TEMPLATE where ORG_ACCOUNT_ID=" + params.get("orgAccountId") + " and IS_DELETE=0 and WORKFLOW_ID !=0 ) t where c.id=t.CATEGORY_ID) ct LEFT JOIN TEMP_TEMPLATE_STOP s on CT.id=s.TEMPLATE_ID) w3 where 1=1  ";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().equals("subject")) {
                if (null != entry.getValue() && !"".equals(entry.getValue())) {
                    sql += " and w3.subject like '%" + entry.getValue() + "%'";
                }
            }
            if (entry.getKey().equals("categoryId")) {
                if (null != entry.getValue() && !"".equals(entry.getValue())) {
                    sql += " and w3.CATID = " + entry.getValue();
                }
            }

        }

        List<Map<String, Object>> result = null;
        try {
            result = JDBCUtil.doQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Map<String, String>> nl = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            Map<String, Object> map = result.get(i);
            map.put("name", ResourceUtil.getString((String) map.get("name")));
            Map<String, String> m = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                m.put(entry.getKey(), entry.getValue() + "");
            }
            nl.add(m);
        }
        return nl;
    }

    @Override
    public void saveTempTemplateStop(TempTemplateStop stop) {
        DBAgent.save(stop);
    }

    @Override
    public void updateTempTemplateStop(TempTemplateStop stop) {
        DBAgent.update(stop);
    }

    @Override
    public List<TempTemplateStop> getTemplateStop(Map<String, Object> param) {
        return DBAgent.find("from TempTemplateStop where templateId=:templateId ", param);
    }

    //***********************************************************

    @Override
    public List<ZorgMember> getAllMemberPOByDeptId(Map<String, Object> param, Boolean p1, Boolean p2) {
        StringBuilder sql = new StringBuilder();
        sql.append("select s.* from (select mpl.*,nvl(d.DAY_NUM,'') DAY_NUM from (select m.ORG_DEPARTMENT_ID,m.ORG_ACCOUNT_ID,(select name from ORG_UNIT u where u.id=m.ORG_DEPARTMENT_ID) deptname,m.id,m.name,m.ORG_LEVEL_ID,l.name levelName,p.LOGIN_NAME from ORG_MEMBER m,ORG_LEVEL l,ORG_PRINCIPAL p where m.ORG_LEVEL_ID=l.id and p.MEMBER_ID=m.id and m.IS_ENABLE=1 ) mpl LEFT JOIN DEPARTMENT_VIEW_TIME_RANGE d on mpl.id=d.MEMBER_ID) s where 1=1 ");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if ("accountId".equals(key)) {
                if (!"".equals(value)) {
                    sql.append(" and s.ORG_ACCOUNT_ID =" + value);
                }
            }
            if ("name".equals(key)) {
                if (!"".equals(value)) {
                    sql.append(" and s.name like '%" + value + "%'");
                }
            }
            if ("departmentId".equals(key)) {
//                if (!"".equals(value)) {
                    sql.append(" and s.ORG_DEPARTMENT_ID = " + value);
//                }
            }

        }
        List<Map<String, Object>> result = null;
        try {
            result = JDBCUtil.doQuery(sql.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<ZorgMember> rows = new ArrayList<>();
        if (null != result && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                ZorgMember orgMember = new ZorgMember();
                orgMember.setName((String) result.get(i).get("name"));
                orgMember.setOrgLevelId(((BigDecimal) result.get(i).get("org_level_id")).toString());
                orgMember.setId(((BigDecimal) result.get(i).get("id")).toString());
                orgMember.setLevelName((String) result.get(i).get("levelname"));
                orgMember.setLoginName((String) result.get(i).get("login_name"));
                orgMember.setDayNum(!"".equals((String) result.get(i).get("day_num")) && null != ((String) result.get(i).get("day_num")) ? (String) result.get(i).get("day_num") : "-");
                orgMember.setDeptname((String) result.get(i).get("deptname"));
                orgMember.setOrgDepartmentId(((BigDecimal) result.get(i).get("org_department_id")).toString());
                rows.add(orgMember);
            }
        }
        return rows;
    }

    @Override
    public void saveDepartmentViewTimeRange(DepartmentViewTimeRange range) {
        DBAgent.save(range);
    }

    @Override
    public void updateDepartmentViewTimeRange(DepartmentViewTimeRange range) {
        DBAgent.update(range);
    }

    @Override
    public List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String, Object> map) {
        return DBAgent.find("from DepartmentViewTimeRange where memberId=:memberId", map);
    }

    @Override
    public List<Map<String, Object>> queryAllAccount() {
        String sql = "select id,name,code,type,path,org_account_id from ORG_UNIT where IS_ENABLE=1 and IS_DELETED=0 and type='Account'";
        try {
            return JDBCUtil.doQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> queryAllDepartment(Long accountid) {
        String sql = "select id,name,code,type,path,org_account_id from ORG_UNIT where IS_ENABLE=1 and IS_DELETED=0 and ORG_ACCOUNT_ID=" + accountid;
        try {
            return JDBCUtil.doQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
