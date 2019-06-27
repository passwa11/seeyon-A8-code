package com.seeyon.apps.ext.zMember.dao.impl;

import com.seeyon.apps.ext.zMember.dao.zMemberDao;
import com.seeyon.apps.ext.zMember.util.JDBCUtil;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.xpath.operations.Bool;

import java.math.BigDecimal;
import java.util.*;

/**
 * 周刘成   2019/6/20
 */
public class zMemberDaoImpl implements zMemberDao {

    @Override
    public List<OrgMember> getAllMemberPO_New(Map<String, Object> param, Boolean isInternal, Boolean enable, FlipInfo flipInfo) {
        StringBuilder sql = new StringBuilder();

        int page = (flipInfo.getPage() - 1) * 20;
        int pages = flipInfo.getPage() * 20;

        sql.append("select * from ( select tw.*,rownum  rowno from ( select DISTINCT m.*  ");
        if (null != param && param.containsKey("loginName")) {
            sql.append(" FROM ORG_MEMBER  m, ORG_PRINCIPAL  p ");
            sql.append(" WHERE m.ID=p.MEMBER_ID ");
        } else {
            sql.append(" FROM ORG_MEMBER  m ");
            sql.append(" WHERE 1=1 ");
        }

        if (isInternal != null) {
            if (isInternal == true) {
                sql.append(" AND m.IS_INTERNAL=1");
            } else {
                sql.append(" AND m.IS_INTERNAL=0");
            }
        }
        if (enable != null) {
            if (enable == true) {
                sql.append(" AND m.IS_ENABLE=1 ");
            } else {
                sql.append(" AND m.IS_ENABLE=0 ");
            }
        }

        sql.append(" AND m.IS_DELETED=0 AND m.IS_ADMIN=0 AND m.IS_VIRTUAL=0 AND m.IS_ASSIGNED=1 ");
        if (null != param) {
            Set<Map.Entry<String, Object>> paramSet = param.entrySet();
            for (Map.Entry<String, Object> entry : paramSet) {
                String condition = entry.getKey();
                Object feildvalue = entry.getValue();
                if (StringUtils.isNotBlank(condition) && !"null".equals(condition)) {
                    if ("workLocal".equals(condition)) {
                        if ("".equals(entry.getValue())) {
                            sql.append(" AND (m.EXT_ATTR_36").append("like '%" + feildvalue + "%' or m.EXT_ATTR_36 is null)");
                        } else {
                            sql.append(" AND m.EXT_ATTR_36").append(" like '%" + feildvalue + "%'");
                        }
                    } else if (feildvalue instanceof String) {
                        if ("loginName".equals(condition)) {
                            sql.append(" AND (p.LOGIN_NAME").append(" LIKE '%" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%')");
                        } else if ("name".equals(condition)) {
                            sql.append(" AND (m.NAME").append(" LIKE '%" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%')");
                        } else if ("code".equals(condition)) {
                            sql.append(" AND (m.CODE").append(" LIKE '" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%')");
                        }
                    }
                }
            }
        }
        sql.append(" ORDER BY m.SORT_ID ASC ) tw where rownum<=" + pages + " ) thr where thr.rowno > " + page + " ");

        List<Map<String, Object>> result = JDBCUtil.doQuery(sql.toString());
        List<OrgMember> rows = new ArrayList<>();
        if (null != result && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                OrgMember orgMember = new OrgMember();
                orgMember.setName((String) result.get(i).get("name"));
                orgMember.setCode((String) result.get(i).get("code"));
                String internalz = ((BigDecimal) result.get(i).get("is_internal")).toString();
                int Internal = Integer.parseInt(internalz);
                orgMember.setInternal(getBoolean(Internal));
                orgMember.setLoginable(getBoolean(Integer.parseInt(((BigDecimal) result.get(i).get("is_loginable")).toString())));
                orgMember.setVirtual(getBoolean(Integer.parseInt(((BigDecimal) result.get(i).get("is_virtual")).toString())));
                orgMember.setAdmin(getBoolean(Integer.parseInt(((BigDecimal) result.get(i).get("is_admin")).toString())));
                orgMember.setAssigned(getBoolean(Integer.parseInt(((BigDecimal) result.get(i).get("is_assigned")).toString())));
                orgMember.setType(Integer.parseInt(((BigDecimal) result.get(i).get("type")).toString()));
                orgMember.setState(Integer.parseInt(((BigDecimal) result.get(i).get("state")).toString()));
                orgMember.setEnable(getBoolean(Integer.parseInt(((BigDecimal) result.get(i).get("is_enable")).toString())));
                orgMember.setDeleted(getBoolean(Integer.parseInt(((BigDecimal) result.get(i).get("is_deleted")).toString())));
                orgMember.setStatus(Integer.parseInt(((BigDecimal) result.get(i).get("status")).toString()));
                orgMember.setSortId(Long.parseLong(((BigDecimal) result.get(i).get("sort_id")).toString()));
                orgMember.setOrgDepartmentId(Long.parseLong(((BigDecimal) result.get(i).get("org_department_id")).toString()));
                orgMember.setOrgPostId(Long.parseLong(((BigDecimal) result.get(i).get("org_post_id")).toString()));
                orgMember.setOrgLevelId(Long.parseLong(((BigDecimal) result.get(i).get("org_level_id")).toString()));
                orgMember.setOrgAccountId(Long.parseLong(((BigDecimal) result.get(i).get("org_account_id")).toString()));
                orgMember.setDescription((String) result.get(i).get("description"));
                orgMember.setCreateTime((Date) result.get(i).get("create_time"));
                orgMember.setUpdateTime((Date) result.get(i).get("update_time"));
                orgMember.setExternalType(Integer.parseInt(((BigDecimal) result.get(i).get("external_type")).toString()));
                orgMember.setId(Long.parseLong(((BigDecimal) result.get(i).get("id")).toString()));
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
    public List<OrgMember> getAllMemberPO(Map<String, Object> param, Boolean isInternal, Boolean enable, FlipInfo flipInfo) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");

        if (null != param && param.containsKey("loginName")) {
            hql.append(" FROM OrgMember as m, OrgPrincipal as p ");
            hql.append(" WHERE m.id=p.memberId ");
        } else {
            hql.append(" FROM OrgMember as m ");
            hql.append(" WHERE 1=1 ");
        }

        if (isInternal != null) {
            hql.append(" AND m.internal=:internal");
            params.put("internal", isInternal);
        }
        if (enable != null) {
            hql.append(" AND m.enable=:enable ");
            params.put("enable", enable);
        }

        hql.append(" AND m.deleted=false AND m.admin=false AND m.virtual=false AND m.assigned=true");
        if (null != param) {
            Set<Map.Entry<String, Object>> paramSet = param.entrySet();
            for (Map.Entry<String, Object> entry : paramSet) {
                String condition = entry.getKey();
                Object feildvalue = entry.getValue();
                if (StringUtils.isNotBlank(condition) && !"null".equals(condition)) {
                    if ("workLocal".equals(condition)) {
                        if ("".equals(entry.getValue())) {
                            hql.append(" AND (m.extAttr36").append("like :" + condition + " or m.extAttr36 is null)");
                        } else {
                            hql.append(" AND m.extAttr36").append(" like :" + condition);
                        }
                        params.put(condition, feildvalue);
                    } else if (feildvalue instanceof String) {
                        if ("loginName".equals(condition)) {
                            hql.append(" AND (p.").append(condition).append(" LIKE :" + condition + SQLWildcardUtil.setEscapeCharacter() + ")");
                        } else {
                            hql.append(" AND (m.").append(condition).append(" LIKE :" + condition + SQLWildcardUtil.setEscapeCharacter() + ")");
                        }
                        feildvalue = "%" + SQLWildcardUtil.escape(String.valueOf(feildvalue)) + "%";
                        params.put(condition, String.valueOf(feildvalue));
                    } else if (feildvalue != null) {
                        hql.append(" AND m.").append(condition).append("=:" + condition);
                        params.put(condition, feildvalue);
                    }
                }
            }
        }
        hql.append(" ORDER BY m.sortId ASC");
        return DBAgent.find(hql.toString(), params, flipInfo);
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
