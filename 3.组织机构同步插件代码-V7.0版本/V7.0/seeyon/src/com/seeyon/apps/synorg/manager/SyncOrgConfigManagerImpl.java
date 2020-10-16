package com.seeyon.apps.synorg.manager;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.constants.SynOrgConstants.SynOrgTriggerDate;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.po.config.ConfigItem;

/**
 * @author Yang.Yinghai
 * @date 2016年8月2日下午4:16:45
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncOrgConfigManagerImpl implements SyncOrgConfigManager {

    /** 是否自动同步 */
    private static boolean isAutoSync;

    /** 配置参数管理器 */
    private ConfigManager configManager;

    /**
     * {@inheritDoc}
     */
    public void init() {
        // 指定同步日期
        ConfigItem orgSyncDate = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DATE);
        // 指定同步时间
        ConfigItem orgSyncTime = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_TIME);
        // 是否启用自动同步
        ConfigItem orgSyncAuto = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.CANAUTOORGSYNC);
        // 同步类型
        ConfigItem orgSyncType = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_TYPE);
        // 轮循同步间隔时间
        ConfigItem orgSyncIntervalTime = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_INTERVAL_TIME);
        // 默认职务名
        ConfigItem orgSyncDefLevelName = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_LEVEL_NAME);
        // 默认岗位名
        ConfigItem orgSyncDefPostName = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_POST_NAME);
        // 默认登录密码
        ConfigItem orgSyncDefPassword = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_PASSWORD);
        // 是否同步人员密码
        ConfigItem orgSyncPassState = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_PASSWORD_STATE);
        // 中间库根节点编码
        ConfigItem rootDeptCode = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_ROOTCODE);
        // 同步范围
        ConfigItem orgSyncScope = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_SCOPE);
        // 没有找到，则直接写入
        if(orgSyncDate == null || orgSyncTime == null) {
            // 同步日期默认为：每天
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DATE, String.valueOf(SynOrgTask.getTriggerDate().key()));
            // 同步时间默认为：23:59
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_TIME, SynOrgTask.getTriggerHour() + ":" + SynOrgTask.getTriggerMinute());
        } else {
            SynOrgTask.setTriggerDate(SynOrgTriggerDate.values()[Integer.valueOf(orgSyncDate.getConfigValue())]);
            String[] strs = orgSyncTime.getConfigValue().split(":");
            SynOrgTask.setTriggerTime(Integer.valueOf(strs[0]), Integer.valueOf(strs[1]));
        }
        if(orgSyncType == null || orgSyncIntervalTime == null) {
            // 同步时间类型默认为：间隔时间
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_TYPE, String.valueOf(SynOrgTask.getSynchTimeType()));
            // 间隔时间:默认10分钟
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_INTERVAL_TIME, SynOrgTask.getIntervalHour() + ":" + SynOrgTask.getIntervalMin());
        } else {
            if("0".equals(orgSyncType.getConfigValue())) {
                SynOrgTask.setSynchTimeType(SynOrgConstants.SYNC_TIME_TYPE.setTime.ordinal());
            } else {
                SynOrgTask.setSynchTimeType(SynOrgConstants.SYNC_TIME_TYPE.intervalTime.ordinal());
            }
            String[] strs = orgSyncIntervalTime.getConfigValue().split(":");
            SynOrgTask.setIntervalTime(Integer.valueOf(strs[0]), Integer.valueOf(strs[1]));
        }
        if(orgSyncAuto == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.CANAUTOORGSYNC, "false");
        } else {
            isAutoSync = Boolean.parseBoolean(orgSyncAuto.getConfigValue());
        }
        if(orgSyncDefLevelName == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_LEVEL_NAME, SynOrgTask.getDefaultLevelName());
        } else {
            SynOrgTask.setDefaultLevelName(orgSyncDefLevelName.getConfigValue());
        }
        if(orgSyncDefPostName == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_POST_NAME, SynOrgTask.getDefaultPostName());
        } else {
            SynOrgTask.setDefaultPostName(orgSyncDefPostName.getConfigValue());
        }
        if(rootDeptCode == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_ROOTCODE, SynOrgTask.getRootDeptCode());
        } else {
            SynOrgTask.setRootDeptCode(rootDeptCode.getConfigValue());
        }
        if(orgSyncDefPassword == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_PASSWORD, SynOrgTask.getDefaultPassword());
        } else {
            SynOrgTask.setDefaultPassword(orgSyncDefPassword.getConfigValue());
        }
        if(orgSyncPassState == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_PASSWORD_STATE, "false");
        } else {
            SynOrgTask.setSynPassword(Boolean.parseBoolean(orgSyncPassState.getConfigValue()));
        }
        if(orgSyncScope == null) {
            configManager.addConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_SCOPE, SynOrgTask.getSynScope());
        } else {
            SynOrgTask.setSynScope(orgSyncScope.getConfigValue());
        }
        // 如果启用了自动同步，则立即注册同步任务
        SynOrgTask.registerSyncTask(isAutoSync);
    }

    /**
     * {@inheritDoc}
     */
    public void setOrgTriggerDate(SynOrgTriggerDate date, int hour, int minute) {
        // 更新同步日期
        String dateStr = String.valueOf(date.key());
        ConfigItem dateItem = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DATE);
        if(!dateStr.equals(dateItem.getConfigValue())) {
            dateItem.setConfigValue(dateStr);
            configManager.updateConfigItem(dateItem);
            SynOrgTask.setTriggerDate(date);
        }
        String triggerTime = hour + ":" + minute;
        // 更新同步时间
        ConfigItem timeItem = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_TIME);
        if(!triggerTime.equals(timeItem.getConfigValue())) {
            timeItem.setConfigValue(triggerTime);
            configManager.updateConfigItem(timeItem);
            SynOrgTask.setTriggerTime(hour, minute);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAutoSync() {
        ConfigItem orgSyncAuto = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.CANAUTOORGSYNC);
        String ce = orgSyncAuto.getConfigValue();
        return Boolean.parseBoolean(ce);
    }

    /**
     * {@inheritDoc}
     */
    public void setAutoSync(boolean isAutoSync) {
        String canExcuteStr = String.valueOf(isAutoSync);
        ConfigItem it = this.configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.CANAUTOORGSYNC);
        if(!canExcuteStr.equals(it.getConfigValue())) {
            it.setConfigValue(canExcuteStr);
            configManager.updateConfigItem(it);
        }
        SynOrgTask.registerSyncTask(isAutoSync);
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultInfo(String defaultPostName, String defaultLevelName, String defaultPassword, String rootDeptCode) {
        ConfigItem orgSyncDefPostName = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_POST_NAME);
        // 如果设置改变了则更新数据库
        if(!defaultPostName.equals(orgSyncDefPostName.getConfigValue())) {
            orgSyncDefPostName.setConfigValue(defaultPostName);
            configManager.updateConfigItem(orgSyncDefPostName);
            SynOrgTask.setDefaultPostName(defaultPostName);
        }
        ConfigItem orgSyncDefLevelName = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_LEVEL_NAME);
        // 如果设置改变了则更新数据库
        if(!defaultLevelName.equals(orgSyncDefLevelName.getConfigValue())) {
            orgSyncDefLevelName.setConfigValue(defaultLevelName);
            configManager.updateConfigItem(orgSyncDefLevelName);
            SynOrgTask.setDefaultLevelName(defaultLevelName);
        }
        ConfigItem orgSyncDefPassword = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_PASSWORD);
        // 如果设置改变了则更新数据库
        if(!defaultPassword.equals(orgSyncDefPassword.getConfigValue())) {
            orgSyncDefPassword.setConfigValue(defaultPassword);
            configManager.updateConfigItem(orgSyncDefPassword);
            SynOrgTask.setDefaultPassword(defaultPassword);
        }
        ConfigItem rootDeptCodeItem = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_DEFAULT_ROOTCODE);
        // 如果设置改变了则更新数据库
        if(!rootDeptCode.equals(rootDeptCodeItem.getConfigValue())) {
            rootDeptCodeItem.setConfigValue(rootDeptCode);
            configManager.updateConfigItem(rootDeptCodeItem);
            SynOrgTask.setRootDeptCode(rootDeptCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSyncScope(String scope) {
        ConfigItem orgSyncScope = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_SCOPE);
        // 如果设置改变了则更新数据库
        if(!scope.equals(orgSyncScope.getConfigValue())) {
            orgSyncScope.setConfigValue(scope);
            configManager.updateConfigItem(orgSyncScope);
            SynOrgTask.setSynScope(scope);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setIsSynPassword(boolean isSynPassword) {
        String isSynPasswordStr = String.valueOf(isSynPassword);
        ConfigItem orgSyncPassState = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_PASSWORD_STATE);
        // 如果设置改变了则更新数据库
        if(!isSynPasswordStr.equals(orgSyncPassState.getConfigValue())) {
            orgSyncPassState.setConfigValue(isSynPasswordStr);
            configManager.updateConfigItem(orgSyncPassState);
            SynOrgTask.setSynPassword(isSynPassword);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setIntervalTime(int intervalHour, int intervalMin) {
        String intervalTimeStr = intervalHour + ":" + intervalMin;
        ConfigItem orgSyncIntervalTime = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_INTERVAL_TIME);
        // 如果设置改变了则更新数据库
        if(!intervalTimeStr.equals(orgSyncIntervalTime.getConfigValue())) {
            orgSyncIntervalTime.setConfigValue(intervalTimeStr);
            configManager.updateConfigItem(orgSyncIntervalTime);
            SynOrgTask.setIntervalTime(intervalHour, intervalMin);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSynchTimeType(int synchTimeType) {
        String synchTimeTypeStr = String.valueOf(synchTimeType);
        ConfigItem orgSyncType = configManager.getConfigItem(SynOrgConstants.SYNORG_CONFIGURATION, SynOrgConstants.ORG_SYNC_TYPE);
        // 如果设置改变了则更新数据库
        if(!synchTimeTypeStr.equals(orgSyncType.getConfigValue())) {
            orgSyncType.setConfigValue(synchTimeTypeStr);
            configManager.updateConfigItem(orgSyncType);
            SynOrgTask.setSynchTimeType(synchTimeType);
        }
    }

    /**
     * 设置configManager
     * @param configManager configManager
     */
    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}
