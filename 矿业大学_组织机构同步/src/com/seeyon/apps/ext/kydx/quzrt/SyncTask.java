package com.seeyon.apps.ext.kydx.quzrt;

import com.seeyon.apps.ext.kydx.dao.OrgCommon;
import com.seeyon.apps.ext.kydx.manager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2019-7-29.
 */
public class SyncTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SyncTask.class);


    private OrgDeptManager deptManager = new OrgDeptManagerImpl();

    private OrgLevelManager levelManager = new OrgLevelManagerImpl();

    private OrgPostManager postManager = new OrgPostManagerImpl();

    private OrgMemberManager memberManager = new OrgMemberManagerImpl();

    private OrgCommon orgCommon = new OrgCommon();

    @Override
    public void run() {
        logger.info("==============================同步组织信息执行了吗？======================================");
        deptManager.insertOrgDept();
        deptManager.insertOtherOrgDept();
        deptManager.updateOrgDept();


        levelManager.insertOrgLevel();
        levelManager.updateOrgLevel();


        postManager.insertPost();
        postManager.updatePost();

        memberManager.insertMember();
        memberManager.updateMember();

        deptManager.deleteOrgDept();
        levelManager.deleteOrgLevel();
        postManager.deletePost();
        memberManager.deleteMember();


    }


    public OrgCommon getOrgCommon() {
        return orgCommon;
    }

    public void setOrgCommon(OrgCommon orgCommon) {
        this.orgCommon = orgCommon;
    }
}
