package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.SynUnit;

public interface SyncUnitDao {
    /**
     * 创建单位数据 增加一个字段 保存 在OA中创建成功单位以后 存OA里单位ID
     * @param deptList 部门列表
     */
    public void createAll(List<SynUnit> unitList);
    
    /**
     * 创建单位数据
     * @param 
     */
    public void create(SynUnit unit);

    /**
     * 更新同步信息：更新同步状态， 更新创建成功以后OA中的ID
     * @param deptList 部门列表
     */
    public void updateAll(List<SynUnit> unitList);

    /**
     * 查找全部没有同步的单位信息
     */
    public List<SynUnit> findAll();
    
    /**
     * 查找全部已经同步的单位信息
     */
    public List<SynUnit> findAllSynUnit();
    
    /**
     * 通过组织编码查找单位信息
     */
    public SynUnit findAllByCode(String code);

    /**
     * 不提供清楚单位中间表方法，为后续创建部门 查找对应单位做准备
     * public void deleteAll();
     */
    
    /**
     * 不提供清楚单位中间表方法，为后续创建部门 查找对应单位做准备
     */
    public void delete(SynUnit unit);
    

}
