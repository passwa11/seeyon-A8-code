package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.SynMember;

/**
 * 人员管理
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncMemberDao {

    /**
     * 创建人员数据
     * @param memberList 人员列表对象
     */
    public void createAll(List<SynMember> memberList);

    /**
     * 更新同步信息
     * @param memberList 人员列表
     */
    public void updateAll(List<SynMember> memberList);
    
    /**
     * 更新同步信息
     * @param member 人员
     */
    public void update(SynMember member);

    /**
     * 查找全部人员
     */
    public List<SynMember> findAll();
    
    /**
     * 通过登录名查找人员
     */
    public SynMember findPersonByAccount(String loginname);

    /**
     * 清空人员数据
     */
    public void deleteAll();
    /**
     * 清空指定人员数据
     */
    public void delete(SynMember member);
}
