package com.seeyon.apps.ext.accessSeting.manager;

import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;

import java.util.List;
import java.util.Map;

public interface AccessSetingManager {
    List<Map<String,Object>> queryAllUnit(Long accountId);

    void saveDepartmentViewTimeRange(DepartmentViewTimeRange range);

    void updateDepartmentViewTimeRange(DepartmentViewTimeRange range);

    List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String,Object> range);
}
