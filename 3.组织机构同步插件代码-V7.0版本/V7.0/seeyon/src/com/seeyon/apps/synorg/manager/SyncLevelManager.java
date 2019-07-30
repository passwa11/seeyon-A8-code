package com.seeyon.apps.synorg.manager;

import java.util.List;

import com.seeyon.apps.synorg.po.SynLevel;

/**
 * 职务级别同步管理接口
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:44:00
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncLevelManager {

    /**
     * 职务级别同步
     * @throws Exception
     */
    public void synAllLevel();

    /**
     * 创建职务级别数据
     * @param levelList 职务级别列表
     */
    public void create(List<SynLevel> levelList);

    /**
     * 清空职务级别数据
     */
    public void delete();
}
