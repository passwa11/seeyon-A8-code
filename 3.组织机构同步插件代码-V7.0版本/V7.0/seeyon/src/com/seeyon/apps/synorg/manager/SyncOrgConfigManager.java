package com.seeyon.apps.synorg.manager;

import com.seeyon.apps.synorg.constants.SynOrgConstants.SynOrgTriggerDate;

/**
 * @author Yang.Yinghai
 * @date 2016年8月2日下午4:15:52
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncOrgConfigManager {

    /**
     * 初始化环境 这里会读取配置,并自动启动定时器
     */
    public void init();

    /**
     * 设置组织模型同步的时间参数,设置后,定时器会更新
     * @param date 同步日期
     * @param hour 小时
     * @param minute 分钟
     */
    public void setOrgTriggerDate(SynOrgTriggerDate date, int hour, int minute);

    /**
     * 获取是否启动自动同步
     * @return
     */
    public boolean isAutoSync();

    /**
     * 设置是否自动同步
     * @param canExcute 是否自动同步
     */
    public void setAutoSync(boolean isAutoSync);

    /**
     * 设置同步时间类型
     * @param synchTimeType 指定同步 或者 间隔同步
     */
    public void setSynchTimeType(int synchTimeType);

    /**
     * 设置间隔同步时间
     * @param intervalHour 间隔小时数
     * @param intervalMin 间隔分钟数
     */
    public void setIntervalTime(int intervalHour, int intervalMin);

    /**
     * 设置同步默认值
     * @param defaultPostName 默认岗位名
     * @param defaultLevelName 默认职务名
     * @param defaultPassword 默认登录密码
     * @param rootDeptCode 根部门编码
     */
    public void setDefaultInfo(String defaultPostName, String defaultLevelName, String defaultPassword, String rootDeptCode);

    /**
     * 设置同步范围
     * @param scope 同步范围
     */
    public void setSyncScope(String scope);

    /**
     * 设置是否同步人员密码
     * @param isSynPassword 是否同步
     */
    public void setIsSynPassword(boolean isSynPassword);
}
