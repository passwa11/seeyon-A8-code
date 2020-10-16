package com.seeyon.apps.synorg.manager;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.dao.SyncOrgDao;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.dao.OrgHelper;

/**
 * 同步插件新增组织机构方法
 * @author Yang.Yinghai
 * @date 2015-8-18下午10:17:21 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncOrgManagerImpl implements SyncOrgManager {

    /** 日志对象 */
    private static final Log log = LogFactory.getLog(SyncOrgManagerImpl.class);

    /**组织机构同步DAO*/
    private SyncOrgDao syncOrgDao;

    /** 是否正在手动同步 */
    private volatile boolean isSyning;
    
    /** 单位同步管理器 */
    private SyncUnitManager syncUnitManager;

    /** 部门同步管理器 */
    private SyncDepartmentManager syncDepartmentManager;

    /** 职务同步管理器 */
    private SyncLevelManager syncLevelManager;

    /** 岗位同步管理器 */
    private SyncPostManager syncPostManager;

    /** 部门角色同步管理器 */
    private SyncDeptRoleManager syncDeptRoleManager;

    /** 人员同步管理器 */
    private SyncMemberManager syncMemberManager;

    /**
     * {@inheritDoc}
     */
    public boolean isSyning() {
        return isSyning;
    }

    /**
     * {@inheritDoc}
     */
    public void synThreadOperation() {
    	log.info("isSyning=="+isSyning);
        if(isSyning) {
            return;
        }
        isSyning = true;
        Thread thread = new SynchThread();
        thread.setName("A8-V5.6sp1 organization sync operation");
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncThirdOrgDataToSeeyon() throws Exception {
        // 调用接口将第三方的组织机构数据写入到中间库表中
        Map<String, AdapterOrgDataManager> aMsgM = AppContext.getBeansOfType(AdapterOrgDataManager.class);
        for(Iterator<AdapterOrgDataManager> iter = aMsgM.values().iterator(); iter.hasNext();) {
            AdapterOrgDataManager orgDataManager = iter.next();
//            syncDepartmentManager.delete();
//            syncDepartmentManager.create(orgDataManager.getDepartmentList());
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_LEVEL)) {
                // syncLevelManager.delete();
                // syncLevelManager.create(orgDataManager.getLevelList());
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_POST)) {
                // syncPostManager.delete();
                // syncPostManager.create(orgDataManager.getPostList());
            }
//            syncMemberManager.delete();
//            syncMemberManager.create(orgDataManager.getMemberList());
        }
        if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_UNIT)) {
        	SynOrgConstants setGroup=new SynOrgConstants();
        	setGroup.setOrgSyncIsGroup(true);
            /** 同步单位 */
            syncUnitManager.synAllUnit();
            /** 同步单位 下部门*/
        	if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_DEPARTMENT)) {
                /** 同步部门 */
        		syncDepartmentManager.synAllUnitsDepartment();
        		syncDepartmentManager.delete();
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_POST)) {
                /** 同步岗位 */
                syncPostManager.synAllPost();
                syncPostManager.delete();
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_MEMBER)) {
                /** 同步人员 */
                syncMemberManager.synAllMember();
                syncMemberManager.delete();
            }
        }else{
        	if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_DEPARTMENT)) {
                /** 同步部门 */
                syncDepartmentManager.synAllDepartment();
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_POST)) {
                /** 同步岗位 */
                // syncPostManager.synAllPost();
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_MEMBER)) {
                /** 同步人员 */
                syncMemberManager.synAllMember();
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_LEVEL)) {
                /** 同步职务 */
                // syncLevelManager.synAllLevel();
            }
            if(SynOrgTask.getSynScope().contains(SynOrgConstants.ORG_ENTITY_DEPTROLE)) {
                /** 同步部门角色 */
                syncDeptRoleManager.synAllDeptRole();
            }
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public V3xOrgEntity getEntityByProperty(String entityClassName, String property, Object value, Long accountId) {
        V3xOrgEntity entity = null;
        if(entityClassName.equals(V3xOrgLevel.class.getSimpleName())) {
            entity = (V3xOrgLevel)OrgHelper.poTobo(syncOrgDao.getLevelByCode(accountId, property, value));
        } else if(entityClassName.equals(V3xOrgMember.class.getSimpleName())) {
            entity = (V3xOrgMember)OrgHelper.poTobo(syncOrgDao.getMemberByCode(accountId, property, value));
        } else if(entityClassName.equals(V3xOrgPost.class.getSimpleName())) {
            entity = (V3xOrgPost)OrgHelper.poTobo(syncOrgDao.getPostByCode(accountId, property, value));
        } else if(entityClassName.equals(V3xOrgDepartment.class.getSimpleName())) {
            entity = (V3xOrgDepartment)OrgHelper.poTobo(syncOrgDao.getDeptByCode(accountId, property, value));
        } else if(entityClassName.equals(V3xOrgAccount.class.getSimpleName())){
        	entity=(V3xOrgAccount)OrgHelper.poTobo(syncOrgDao.getUnitByCode(accountId, property, value));
        }
        return entity;
    }

    /**
     * 同步线程
     * @author Yang.Yinghai
     * @date 2016年8月2日下午4:06:25 @Copyright(c) Beijing Seeyon Software Co.,LTD
     */
    private class SynchThread extends Thread {

        @Override
        public void run() {
            try {
                syncThirdOrgDataToSeeyon();
            } catch(Throwable e) {
                log.error(Thread.currentThread().getName() + " 手工同步异步线程异常: ", e);
            } finally {
                isSyning = false;
            }
        }
    }

    /**
     * 设置syncOrgDao
     * @param syncOrgDao syncOrgDao
     */
    public void setSyncOrgDao(SyncOrgDao syncOrgDao) {
        this.syncOrgDao = syncOrgDao;
    }

    /**
     * 设置isSyningHand
     * @param isSyningHand isSyningHand
     */
    public void setSyning(boolean isSyning) {
        this.isSyning = isSyning;
    }
    
    /**
     * 设置syncUnitManager
     * @param syncUnitManager syncUnitManager
     */
    public void setSyncUnitManager(SyncUnitManager syncUnitManager) {
        this.syncUnitManager = syncUnitManager;
    }

    /**
     * 设置syncDepartmentManager
     * @param syncDepartmentManager syncDepartmentManager
     */
    public void setSyncDepartmentManager(SyncDepartmentManager syncDepartmentManager) {
        this.syncDepartmentManager = syncDepartmentManager;
    }

    /**
     * 设置syncLevelManager
     * @param syncLevelManager syncLevelManager
     */
    public void setSyncLevelManager(SyncLevelManager syncLevelManager) {
        this.syncLevelManager = syncLevelManager;
    }

    /**
     * 设置syncPostManager
     * @param syncPostManager syncPostManager
     */
    public void setSyncPostManager(SyncPostManager syncPostManager) {
        this.syncPostManager = syncPostManager;
    }

    /**
     * 设置syncMemberManager
     * @param syncMemberManager syncMemberManager
     */
    public void setSyncMemberManager(SyncMemberManager syncMemberManager) {
        this.syncMemberManager = syncMemberManager;
    }

    /**
     * 设置syncDeptRoleManager
     * @param syncDeptRoleManager syncDeptRoleManager
     */
    public void setSyncDeptRoleManager(SyncDeptRoleManager syncDeptRoleManager) {
        this.syncDeptRoleManager = syncDeptRoleManager;
    }
}
