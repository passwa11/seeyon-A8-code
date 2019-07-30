package com.seeyon.apps.synorg.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.po.OrgLevel;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.po.OrgPost;
import com.seeyon.ctp.organization.po.OrgUnit;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Strings;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午9:55:04
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncOrgDaoImpl implements SyncOrgDao {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public OrgLevel getLevelByCode(Long accountId, String property, Object feildvalue) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append(" FROM " + OrgLevel.class.getSimpleName());
        hql.append(" WHERE deleted=false");
        if(accountId != null) {
            hql.append(" AND orgAccountId=:accountId");
            params.put("accountId", accountId);
        }
        if(StringUtils.isNotBlank(property) && !"null".equals(property)) {
            hql.append(" AND ").append(property).append("=:feildvalue");
            params.put("feildvalue", feildvalue);
        }
        hql.append(" ORDER BY levelId ASC ");
        List list = DBAgent.find(hql.toString(), params);
        if(!list.isEmpty() && list.get(0) != null) {
            return (OrgLevel)list.get(0);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public OrgMember getMemberByCode(Long accountId, String property, Object feildvalue) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");
        hql.append(" FROM OrgMember as m");
        hql.append(" WHERE ");
        hql.append(" m.deleted=false AND m.admin=false AND m.virtual=false AND m.assigned=true");
        if(accountId != null) {
            hql.append(" AND m.orgAccountId=:accountId ");
            params.put("accountId", accountId);
        }
        if(Strings.isNotBlank(property) && !"null".equals(property)) {
            hql.append(" AND ").append(property).append("=:feildvalue");
            params.put("feildvalue", feildvalue);
        }
        hql.append(" ORDER BY m.sortId ASC");
        List list = DBAgent.find(hql.toString(), params);
        if(!list.isEmpty() && list.get(0) != null) {
            return (OrgMember)list.get(0);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public OrgPost getPostByCode(Long accountId, String property, Object feildvalue) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append(" FROM " + OrgPost.class.getSimpleName());
        hql.append(" WHERE deleted=false");
        if(accountId != null) {
            hql.append(" AND orgAccountId=:accountId");
            params.put("accountId", accountId);
        }
        if(StringUtils.isNotBlank(property) && !"null".equals(property)) {
            hql.append(" AND ").append(property).append("=:feildvalue");
            params.put("feildvalue", feildvalue);
        }
        hql.append(" ORDER BY sortId ASC");
        List list = DBAgent.find(hql.toString(), params);
        if(!list.isEmpty() && list.get(0) != null) {
            return (OrgPost)list.get(0);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public OrgUnit getDeptByCode(Long accountId, String property, Object feildvalue) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");
        hql.append(" FROM " + OrgUnit.class.getSimpleName() + " as m ");
        hql.append(" WHERE m.deleted=false ");
        hql.append(" AND m.type=:type ");
        params.put("type", OrgConstants.UnitType.Department.name());
        if(accountId != null && accountId != V3xOrgEntity.VIRTUAL_ACCOUNT_ID) {
            hql.append(" AND m.orgAccountId=:orgAccountId ");
            params.put("orgAccountId", accountId);
        }
        if(StringUtils.isNotBlank(property) && !"null".equals(property)) {
            hql.append(" and m.").append(property).append("=:feildvalue");
            params.put("feildvalue", feildvalue);
        }
        hql.append(" ORDER BY m.sortId ASC");
        List list = DBAgent.find(hql.toString(), params);
        if(!list.isEmpty() && list.get(0) != null) {
            return (OrgUnit)list.get(0);
        } else {
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public V3xOrgEntity getDepOrUnittByCode(Long accountId, String property, Object feildvalue) {
        StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");
        hql.append(" FROM " + OrgUnit.class.getSimpleName() + " as m ");
        hql.append(" WHERE m.deleted=false ");
        hql.append(" AND (m.type=:typedep ");
        params.put("typedep", OrgConstants.UnitType.Department.name());
        hql.append(" OR m.type=:typeunit )");
        params.put("typeunit", OrgConstants.UnitType.Account.name());
        if(accountId != null && accountId != V3xOrgEntity.VIRTUAL_ACCOUNT_ID) {
            hql.append(" AND m.orgAccountId=:orgAccountId ");
            params.put("orgAccountId", accountId);
        }
        if(StringUtils.isNotBlank(property) && !"null".equals(property)) {
            hql.append(" and m.").append(property).append("=:feildvalue");
            params.put("feildvalue", feildvalue);
        }
        hql.append(" ORDER BY m.sortId ASC");
        List list = DBAgent.find(hql.toString(), params);
        if(!list.isEmpty() && list.get(0) != null) {
            return (V3xOrgEntity)list.get(0);
        } else {
            return null;
        }
    }

	@Override
	public OrgUnit getUnitByCode(Long accountId, String property,
			Object feildvalue) {
		StringBuilder hql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        hql.append("SELECT m ");
        hql.append(" FROM " + OrgUnit.class.getSimpleName() + " as m ");
        hql.append(" WHERE m.deleted=false ");
        hql.append(" AND m.type=:type ");
        params.put("type", OrgConstants.UnitType.Account.name());
        if(StringUtils.isNotBlank(property) && !"null".equals(property)) {
            hql.append(" and m.").append(property).append("=:feildvalue");
            params.put("feildvalue", feildvalue);
        }
        hql.append(" ORDER BY m.sortId ASC");
        List list = DBAgent.find(hql.toString(), params);
        if(!list.isEmpty() && list.get(0) != null) {
            return (OrgUnit)list.get(0);
        } else {
            return null;
        }
	}

}
