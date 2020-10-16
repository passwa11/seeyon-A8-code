package com.seeyon.apps.synorg.manager;

import java.util.List;

import com.seeyon.apps.synorg.po.SynUnit;

public interface SyncUnitManager {
	
    /**
     * 同步单位
     * @throws Exception
     */
    public void synAllUnit();
    
    /**
     * 同步单位下的部门
     * @throws Exception
     */
    public void synAllUnitsDepartment();

    /**
     * 创建单位数据
     * @param deptList 单位列表
     */
    public void create(List<SynUnit> unitList);
    
    /**
     * 创建单位数据
     * @param deptList 单位
     */
    public void create(SynUnit unit);

}
