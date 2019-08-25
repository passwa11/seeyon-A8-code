package com.seeyon.apps.ext.zxzyk.quartz;

import com.seeyon.apps.ext.zxzyk.dao.OrgCommon;
import com.seeyon.apps.ext.zxzyk.manager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2019-7-29.
 */
public class SyncDeptTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SyncDeptTask.class);


    private OrgDeptManager orgDeptManager = new OrgDeptManagerImpl();

    private OrgLevelManager orgLevelManager = new OrgLevelManagerImpl();

    private OrgMemberManager orgMemberManager = new OrgMemberManagerImpl();

    private OrgCommon orgCommon = new OrgCommon();

    @Override
    public void run() {
        logger.info("==============================同步组织信息执行了吗？======================================");

        //部门
        orgDeptManager.updateOrgDept();
        orgDeptManager.insertOtherDept();


        //职级
        orgLevelManager.updateOrgLevel();
        orgLevelManager.insertOrgLevel();


        //人员
        orgMemberManager.updateOrgMember();
        orgMemberManager.insertOrgMember();


        orgMemberManager.deleteOrgMember();
        orgDeptManager.deleteOrgDept();
        orgLevelManager.deleteNotExistLevel();
    }

    public OrgDeptManager getOrgDeptManager() {
        return orgDeptManager;
    }

    public void setOrgDeptManager(OrgDeptManager orgDeptManager) {
        this.orgDeptManager = orgDeptManager;
    }

    public OrgLevelManager getOrgLevelManager() {
        return orgLevelManager;
    }

    public void setOrgLevelManager(OrgLevelManager orgLevelManager) {
        this.orgLevelManager = orgLevelManager;
    }

    public OrgMemberManager getOrgMemberManager() {
        return orgMemberManager;
    }

    public void setOrgMemberManager(OrgMemberManager orgMemberManager) {
        this.orgMemberManager = orgMemberManager;
    }

    public OrgCommon getOrgCommon() {
        return orgCommon;
    }

    public void setOrgCommon(OrgCommon orgCommon) {
        this.orgCommon = orgCommon;
    }
}
