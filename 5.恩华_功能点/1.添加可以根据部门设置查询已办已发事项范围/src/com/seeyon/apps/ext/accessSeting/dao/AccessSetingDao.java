package com.seeyon.apps.ext.accessSeting.dao;

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
    List<Map<String, Object>> queryAllDepartment();
}
