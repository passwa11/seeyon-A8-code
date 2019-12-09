package com.seeyon.apps.ext.fileUploadConfig.dao;

import java.math.BigDecimal;
import java.util.*;

import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUnit;
import com.seeyon.apps.ext.fileUploadConfig.po.ZOrgUploadMember;
import com.seeyon.apps.ext.fileUploadConfig.po.ZorgMember;
import com.seeyon.apps.ext.fileUploadConfig.util.JDBCUtil;
import com.seeyon.ctp.util.DBAgent;

public class fileUploadConfigDaoImpl implements fileUploadConfigDao {

    @Override
    public List<ZOrgUploadMember> selectAllUploadMem() {
        return DBAgent.find("from ZOrgUploadMember");
    }

    @Override
    public void deleteUploadMember() {
        DBAgent.bulkUpdate("delete from ZOrgUploadMember where 1=1");
    }

    @Override
    public void insertUploadMember(ZOrgUploadMember zOrgUploadMember) {
        DBAgent.save(zOrgUploadMember);
    }

    @Override
    public ZOrgUploadMember selectUploadMemberByuserId(String userid) {
        return DBAgent.get(ZOrgUploadMember.class, Long.parseLong(userid));
    }

    @Override
    public List<ZOrgUnit> getUnitByAccountId(Long accountId) {
        String sql = "select id,name,path,org_account_id from ORG_UNIT where IS_ENABLE=1 and ORG_ACCOUNT_ID=" + accountId;
        List<Map<String, Object>> result = JDBCUtil.doQuery(sql);
        List<ZOrgUnit> list = new ArrayList<>();
        if (null != result && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                ZOrgUnit orgUnit = new ZOrgUnit();
                orgUnit.setId((BigDecimal) result.get(i).get("id"));
                orgUnit.setName((String) result.get(i).get("name"));
                orgUnit.setPath((String) result.get(i).get("path"));
                orgUnit.setOrgAccountId((BigDecimal) result.get(i).get("org_account_id"));
                list.add(orgUnit);
            }
        }
        return list;
    }

    @Override
    public List<ZorgMember> getAllMemberPO_New(Map<String, Object> param, Boolean isInternal, Boolean enable) {
        StringBuilder sql = new StringBuilder();
        sql.append("select m.id,m.name,m.ORG_LEVEL_ID,l.name levelName,p.LOGIN_NAME from ORG_MEMBER m,ORG_LEVEL l,ORG_PRINCIPAL p  where m.ORG_LEVEL_ID=l.id and p.MEMBER_ID=m.id and m.IS_ENABLE=1 ");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if ("name".equals(key)) {
                if (!"".equals(value)) {
                    sql.append(" and m.name like '%" + value + "%'");
                }
            }
            if ("departmentId".equals(key)) {
                if (!"".equals(value)) {
                    sql.append(" and m.ORG_DEPARTMENT_ID = " + value);
                }
            }
        }
        List<Map<String, Object>> result = JDBCUtil.doQuery(sql.toString());
        List<ZorgMember> rows = new ArrayList<>();
        if (null != result && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                ZorgMember orgMember = new ZorgMember();
                orgMember.setName((String) result.get(i).get("name"));
                orgMember.setOrgLevelId(((BigDecimal) result.get(i).get("org_level_id")).toString());
                orgMember.setId(((BigDecimal) result.get(i).get("id")).toString());
                orgMember.setLevelName((String) result.get(i).get("levelname"));
                orgMember.setLoginName((String) result.get(i).get("login_name"));
                rows.add(orgMember);
            }
        }
        return rows;
    }

    public boolean getBoolean(int i) {
        Boolean flag = null;
        if (i == 0) {
            flag = false;
        } else if (i == 1) {
            flag = true;
        }
        return flag;
    }

    @Override
    public int selectUnitPeopleCount() {
        String sql = "select m.name from org_member m where 1=1  AND m.IS_DELETED=0 AND m.IS_ADMIN=0 AND m.IS_VIRTUAL=0 AND m.IS_ASSIGNED=1 and IS_ENABLE=1";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        int count = 0;
        if (list != null) {
            count = list.size();
        }
        return count;
    }
}
