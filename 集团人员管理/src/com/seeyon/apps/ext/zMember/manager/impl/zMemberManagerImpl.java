package com.seeyon.apps.ext.zMember.manager.impl;

import com.seeyon.apps.ext.zMember.dao.impl.zMemberDaoImpl;
import com.seeyon.apps.ext.zMember.dao.zMemberDao;
import com.seeyon.apps.ext.zMember.manager.zMemberManager;
import com.seeyon.apps.ldap.event.OrganizationLdapEvent;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgMember;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/20
 */
public class zMemberManagerImpl implements zMemberManager {
    private Logger logger = LoggerFactory.getLogger(zMemberManagerImpl.class);

    private zMemberDao zMemberDao = new zMemberDaoImpl();

    protected OrgManager orgManager;
    private OrganizationLdapEvent organizationLdapEvent;

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setOrganizationLdapEvent(OrganizationLdapEvent organizationLdapEvent) {
        this.organizationLdapEvent = organizationLdapEvent;
    }

    @Override
    public FlipInfo showPeople(FlipInfo fi, Map params) throws BusinessException {

        /********过滤和条件搜索*******/
        Map queryParams = new HashMap<String, Object>();
        Boolean enabled = null;
        Long secondPostId = null;
        String workLocal = null;
        String condition = String.valueOf(params.get("condition"));
        Object value = params.get("value") == null ? "" : params.get("value");
        if ("state".equals(condition)) {//在职/离职过滤
            value = "1".equals(String.valueOf(params.get("value"))) ? Integer.valueOf(1) : Integer.valueOf(2);
            queryParams.put("state", value);
        }
        if ("name".equals(condition)) {
            queryParams.put("name", value);
        }

        if ("accountName".equals(condition)) {
            queryParams.put("accountName", value);
        }
        if ("loginName".equals(condition)) {
            queryParams.put("loginName", value);
        }
        if (params.containsKey("state") && Strings.isNotBlank(String.valueOf(params.get("state")))) {
            queryParams.put("state", Integer.parseInt(String.valueOf(params.get("state"))));
        }
        if (params.containsKey("enabled") && Strings.isNotBlank(String.valueOf(params.get("enabled")))) {
            enabled = (Boolean) params.get("enabled");
        }
        if (params.containsKey("orgDepartmentId") && Strings.isNotBlank(String.valueOf(params.get("orgDepartmentId")))) {
            queryParams.put("orgDepartmentId", Long.parseLong(String.valueOf(params.get("orgDepartmentId"))));
        }
        if ("secPostId".equals(condition)) {
            List<String> strs = (List<String>) params.get("value");
            if (Strings.isEmpty(strs)) {
                value = null;
            } else {
                String s1 = strs.get(1).trim();
                if (Strings.isEmpty(s1)) {
                    value = null;
                } else {
                    String[] strs2 = s1.split("[|]");
                    value = Long.valueOf(strs2[1].trim());
                }
            }
            enabled = true;
            secondPostId = (Long) value;
        }
        if ("code".equals(condition)) {
            queryParams.put("code", value);
        }
        /********************/

        zMemberDao.getAllMemberPO(queryParams,true,enabled, fi);
        return this.dealResult(fi);
    }

    /**
     * 将结果处理内部使用方法
     *
     * @param fi
     * @return
     * @throws BusinessException
     */
    private FlipInfo dealResult(FlipInfo fi) throws BusinessException {
        List<OrgMember> members = fi.getData();
        List<V3xOrgMember> memberBOs = (List<V3xOrgMember>) OrgHelper.listPoTolistBo(members);
        List<WebV3xOrgMember> result = new ArrayList<WebV3xOrgMember>(memberBOs.size());
        String noPost = ResourceUtil.getString("org.member.noPost");
        for (V3xOrgMember bo : memberBOs) {
            WebV3xOrgMember o = new WebV3xOrgMember();
            o.setAccountName(orgManager.getAccountById(bo.getOrgAccountId()).getName());
            o.setDepartmentName((orgManager.getDepartmentById(bo.getOrgDepartmentId()) == null) ? noPost : OrgHelper.showDepartmentFullPath(bo.getOrgDepartmentId()));
            o.setPostName((orgManager.getPostById(bo.getOrgPostId()) == null) ? noPost : orgManager.getPostById(bo.getOrgPostId()).getName());
            o.setLevelName((orgManager.getLevelById(bo.getOrgLevelId())) == null ? noPost : orgManager.getLevelById(bo.getOrgLevelId()).getName());
            o.setCode(bo.getCode());
            o.setSortId(bo.getSortId());
            o.setName(bo.getName());
            o.setLoginName(bo.getLoginName());
            o.setV3xOrgMember(bo);
            o.setId(bo.getId());
            o.setTypeName(bo.getType() == null ? "" : bo.getType().toString());
            o.setStateName(bo.getState() == null ? "" : bo.getState().toString());
            //ldap
            this.showLdapLoginName(bo, o);
            result.add(o);
        }
        fi.setData(result);
        return fi;
    }

    /**
     * 组装LDAP/AD帐号
     *
     * @param member
     * @param webMember
     */
    private void showLdapLoginName(V3xOrgMember member, WebV3xOrgMember webMember) {
        if (LdapUtils.isLdapEnabled()) {
            try {
                webMember.setLdapLoginName(organizationLdapEvent.getLdapAdLoginName(member.getLoginName()));
            } catch (Exception e) {
                logger.error("ldap/ad 显示ldap帐号异常！", e);
            }

        }
    }

    @Override
    public int selectUnitPeopleCount() {
        return zMemberDao.selectUnitPeopleCount();
    }
}
