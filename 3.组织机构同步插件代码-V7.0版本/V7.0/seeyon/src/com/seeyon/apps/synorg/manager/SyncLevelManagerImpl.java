package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.dao.SyncLevelDao;
import com.seeyon.apps.synorg.po.SynLevel;
import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.apps.synorg.util.ErrorMessageUtil;
import com.seeyon.ctp.organization.bo.OrganizationMessage;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.bo.V3xOrgLevel;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午5:15:47
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncLevelManagerImpl implements SyncLevelManager {

    /** 组织机构管理器 */
    private OrgManagerDirect orgManagerDirect;

    /** 组织机构管理器 */
    private SyncOrgManager syncOrgManager;

    /** 职务实体查询接口 */
    private SyncLevelDao syncLevelDao;

    /** 同步日志管理器 */
    private SyncLogManager syncLogManager;

    /**
     * {@inheritDoc}
     */
    public void synAllLevel() {
        List<SynLevel> allLevels = syncLevelDao.findAll();
        if(allLevels != null && allLevels.size() > 0) {
            List<SynLog> logList = new ArrayList<SynLog>();
            for(int i = 0; i < allLevels.size(); i++) {
                SynLevel synLevel = allLevels.get(i);
                synLevel.setSyncDate(new Date());
                SynLog synLog = new SynLog(SynOrgConstants.ORG_ENTITY_LEVEL, synLevel.getCode(), synLevel.getName());
                V3xOrgLevel level = (V3xOrgLevel)syncOrgManager.getEntityByProperty(V3xOrgLevel.class.getSimpleName(), "code", synLevel.getCode(), null);
                try {
                    if(level != null) {
                        boolean isUpdate = false;
                        String updateInfo = "";
                        synLog.setEntityName(level.getName());
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_UPDATE);
                        // 修改职务名
                        if(!level.getName().equals(synLevel.getName().trim())) {
                            updateInfo += "名称改为:" + synLevel.getName().trim() + " ";
                            level.setName(synLevel.getName().trim());
                            isUpdate = true;
                        }
                        // 修改职务排序号
                        if(level.getSortId() != null && synLevel.getSortId() != null && level.getSortId().longValue() != synLevel.getSortId().longValue()) {
                            updateInfo += "排序号改为:" + synLevel.getSortId() + " ";
                            level.setSortId(synLevel.getSortId());
                            isUpdate = true;
                        }
                        // 修改职务描述
                        if(synLevel.getDescription() != null && !"".equals(synLevel.getDescription().trim())) {
                            if(level.getDescription() == null || !synLevel.getDescription().trim().equals(level.getDescription().trim())) {
                                updateInfo += "描述改为:" + synLevel.getDescription().trim() + " ";
                                level.setDescription(synLevel.getDescription().trim());
                                isUpdate = true;
                            }
                        } else {
                            if(level.getDescription() != null && !"".equals(level.getDescription().trim())) {
                                updateInfo += "描述改为: 空字符串 ";
                                level.setDescription("");
                                isUpdate = true;
                            }
                        }
                        if(isUpdate) {
                            OrganizationMessage mes = orgManagerDirect.updateLevel(level);
                            List<OrgMessage> errorMsgList = mes.getErrorMsgs();
                            if(errorMsgList.size() > 0) {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                                synLog.setSynLog(ErrorMessageUtil.getErrorMessageString(errorMsgList));
                                synLevel.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                            } else {
                                synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                                synLog.setSynLog(updateInfo);
                                synLevel.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            }
                        } else {
                            synLevel.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                            synLog = null;
                        }
                    } else {
                        /** 添加职务级别 **/
                        synLog.setSynType(SynOrgConstants.SYN_OPERATION_TYPE_CREATE);
                        level = new V3xOrgLevel();
                        level.setIdIfNew();
                        level.setLevelId(1);
                        level.setEnabled(true);
                        level.setOrgAccountId(SynOrgConstants.DEFAULT_ACCOUNT_ID);
                        level.setName(synLevel.getName().trim());
                        level.setCode(synLevel.getCode());
                        level.setSortId(synLevel.getSortId() != null ? synLevel.getSortId() : 1L);
                        level.setDescription(synLevel.getDescription() != null ? synLevel.getDescription().trim() : "");
                        level.setUpdateTime(new Date());
                        level.setCreateTime(new Date());
                        level.setStatus(1);
                        orgManagerDirect.addLevel(level);
                        synLog.setSynState(SynOrgConstants.SYN_STATE_SUCCESS);
                        synLevel.setSyncState(SynOrgConstants.SYN_STATE_SUCCESS);
                        synLog.setSynLog("新增职务：" + level.getName() + "[" + level.getCode() + "]");
                    }
                } catch(Exception e) {
                    synLog.setSynState(SynOrgConstants.SYN_STATE_FAILURE);
                    synLog.setSynLog(e.getMessage());
                    logList.add(synLog);
                    synLevel.setSyncState(SynOrgConstants.SYN_STATE_FAILURE);
                    continue;
                }
                if(synLog != null) {
                    logList.add(synLog);
                }
            }
            // 创建同步日志
            if(logList.size() > 0) {
                syncLogManager.createAll(logList);
            }
            // 更新同步信息
            syncLevelDao.updateAll(allLevels);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(List<SynLevel> levelList) {
        syncLevelDao.createAll(levelList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        syncLevelDao.deleteAll();
    }

    /**
     * 设置orgManagerDirect
     * @param orgManagerDirect orgManagerDirect
     */
    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect) {
        this.orgManagerDirect = orgManagerDirect;
    }

    /**
     * 设置syncLevelDao
     * @param syncLevelDao syncLevelDao
     */
    public void setSyncLevelDao(SyncLevelDao syncLevelDao) {
        this.syncLevelDao = syncLevelDao;
    }

    /**
     * 设置syncOrgManager
     * @param syncOrgManager syncOrgManager
     */
    public void setSyncOrgManager(SyncOrgManager syncOrgManager) {
        this.syncOrgManager = syncOrgManager;
    }

    /**
     * 设置syncLogManager
     * @param syncLogManager syncLogManager
     */
    public void setSyncLogManager(SyncLogManager syncLogManager) {
        this.syncLogManager = syncLogManager;
    }
}
