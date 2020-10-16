package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.SynPost;

/**
 * 职务管理
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncPostDao {

    /**
     * 创建岗位数据
     * @param postList 岗位列表
     */
    public void createAll(List<SynPost> postList);
    
    /**
     * 创建岗位数据
     * @param post 岗位
     */
    public void create(SynPost post);

    /**
     * 更新同步信息
     * @param postList 岗位列表
     */
    public void updateAll(List<SynPost> postList);

    /**
     * 查找全部岗位
     */
    public List<SynPost> findAll();
    
    /**
     * 查找岗位
     */
    public SynPost findPostByCode(String code);

    /**
     * 清空岗位数据
     */
    public void deleteAll();
    /**
     * 清空岗位数据
     */
    public void delete(SynPost post);
}
