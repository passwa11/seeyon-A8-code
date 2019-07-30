package com.seeyon.apps.synorg.manager;

import java.util.List;

import com.seeyon.apps.synorg.po.SynPost;

/**
 * 岗位同步管理接口
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:44:00
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncPostManager {

    /**
     * 职务级别同步
     * @throws Exception
     */
    public void synAllPost();

    /**
     * 创建岗位数据
     * @param memberList 岗位列表
     */
    public void create(List<SynPost> postList);
    /**
     * 创建岗位数据
     * @param memberList 岗位
     */
    public void create(SynPost post);

    /**
     * 清空岗位数据
     */
    public void delete();
}
