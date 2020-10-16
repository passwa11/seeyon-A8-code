package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.SynLevel;

/**
 * 职务管理
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncLevelDao {

    /**
     * 创建职务数据
     * @param levelList 职务列表
     */
    public void createAll(List<SynLevel> levelList);

    /**
     * 更新同步信息
     * @param levelList 职务列表
     */
    public void updateAll(List<SynLevel> levelList);

    /**
     * 查找全部职务
     */
    public List<SynLevel> findAll();

    /**
     * 清空职务数据
     */
    public void deleteAll();
}
