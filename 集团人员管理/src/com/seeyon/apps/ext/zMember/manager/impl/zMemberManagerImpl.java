package com.seeyon.apps.ext.zMember.manager.impl;

import com.seeyon.apps.ext.zMember.dao.impl.zMemberDaoImpl;
import com.seeyon.apps.ext.zMember.dao.zMemberDao;
import com.seeyon.apps.ext.zMember.manager.zMemberManager;
import com.seeyon.apps.ldap.event.OrganizationLdapEvent;
import com.seeyon.apps.ldap.util.LdapUtils;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.webmodel.WebV3xOrgMember;
import com.seeyon.ctp.util.FlipInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        String username = (String) params.get("username");
        zMemberDao.getAllMemberPO(username, params, fi);
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
