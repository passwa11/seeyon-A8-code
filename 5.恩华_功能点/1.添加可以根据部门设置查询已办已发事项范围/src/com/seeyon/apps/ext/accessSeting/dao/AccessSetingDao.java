package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;

import java.util.List;
import java.util.Map;

public interface AccessSetingDao {
    /**
     * 获取所有的单位
     *
     * @return
     */
    List<Map<String, Object>> queryAllAccount();

    /**
     * 获取所有的部门
     *
     * @return
     */
    List<Map<String, Object>> queryAllDepartment(Long accountid);


    void saveDepartmentViewTimeRange(DepartmentViewTimeRange range);

    void updateDepartmentViewTimeRange(DepartmentViewTimeRange range);

    List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String,Object> range);
}
