package com.seeyon.apps.ext.zMember.dao.impl;

import com.seeyon.apps.ext.zMember.dao.zMemberDao;
import com.seeyon.apps.ext.zMember.util.JDBCUtil;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/20
 */
public class zMemberDaoImpl implements zMemberDao {
    @Override
    public List<OrgMember> getAllMemberPO(String username, Map<String, Object> param, FlipInfo flipInfo) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");
        hql.append(" FROM OrgMember as m ");
        hql.append(" WHERE 1=1 ");
        if (username != null) {
            hql.append(" AND m.name=:username ");
            params.put("username", username);
        }
//		if (isInternal != null) {
//			hql.append(" AND m.internal=:internal");
//			params.put("internal", isInternal);
//		}
//		if (enable != null) {
//			hql.append(" AND m.enable=:enable ");
//			params.put("enable", enable);
//		}

        hql.append(" AND m.deleted=false AND m.admin=false AND m.virtual=false AND m.assigned=true");
        hql.append(" ORDER BY m.sortId ASC");

        return DBAgent.find(hql.toString(), params, flipInfo);
    }

    @Override
    public int selectUnitPeopleCount() {
        String sql = "select count(1) from org_member m where 1=1  AND m.IS_DELETED=false AND m.IS_ADMIN=false AND m.IS_VIRTUAL=false AND m.IS_ASSIGNED=true";
        int count = JDBCUtil.recordCount(sql);
        return count;
    }
}
