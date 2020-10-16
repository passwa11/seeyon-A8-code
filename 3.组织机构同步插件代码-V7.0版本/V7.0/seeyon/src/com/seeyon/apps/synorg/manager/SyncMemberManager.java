package com.seeyon.apps.synorg.manager;

import java.util.List;

import com.seeyon.apps.synorg.po.SynMember;

/**
 * 人员同步管理接口
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:31:06
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncMemberManager {

    /**
     * 同步全步人员
     * @throws Exception
     */
    public void synAllMember();

    /**
     * 创建人员数据
     * @param memberList 人员列表
     */
    public void create(List<SynMember> memberList);
    
    /**
     * 创建人员数据
     * @param memberList 人员列表
     */
    public void create(SynMember member);

    /**
     * 清空人员数据
     */
    public void delete();
}
