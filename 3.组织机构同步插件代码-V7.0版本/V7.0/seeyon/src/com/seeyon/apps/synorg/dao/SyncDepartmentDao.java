package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.SynDepartment;

/**
 * 部门管理
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncDepartmentDao {

    /**
     * 创建部门数据
     * @param deptList 部门列表
     */
    public void createAll(List<SynDepartment> deptList);
    
    /**
     * 创建部门数据
     * @param dept 部门
     */
    public void create(SynDepartment dept);

    /**
     * 更新同步信息
     * @param deptList 部门列表
     */
    public void updateAll(List<SynDepartment> deptList);

    /**
     * 查找全部部门
     */
    public List<SynDepartment> findAll();
    
    /**
     * 按照编码查找部门
     */
    public SynDepartment findDepByCode(String code);

    /**
     * 清空部门数据
     */
    public void deleteAll();
    /**
     * 删除部门数据
     */
    public void delete(SynDepartment dep);
}
