package com.seeyon.apps.synorg.manager;

import java.util.List;

import com.seeyon.apps.synorg.po.SynDepartment;

/**
 * 部门同步管理接口
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:44:00
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncDepartmentManager {

    /**
     * 部门同步
     * @throws Exception
     */
    public void synAllDepartment();
    
    /**
     * 同步集团单位下的部门
     * @throws Exception
     */
    public void synAllUnitsDepartment();

    /**
     * 创建部门数据
     * @param deptList 部门列表
     */
    public void create(List<SynDepartment> deptList);
    
    /**
     * 创建部门数据
     * @param deptList 部门
     */
    public void create(SynDepartment dept);

    /**
     * 清空部门数据
     */
    public void delete();
}
