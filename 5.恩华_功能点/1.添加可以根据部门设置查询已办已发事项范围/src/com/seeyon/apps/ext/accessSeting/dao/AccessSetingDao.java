package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.TempTemplateStop;
import com.seeyon.apps.ext.accessSeting.po.ZorgMember;

import java.sql.SQLException;
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

    List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String, Object> range);

    List<ZorgMember> getAllMemberPOByDeptId(Map<String, Object> param, Boolean p1, Boolean p2) throws SQLException;

    //****禁用模板流程***********************************************************************
    void saveTempTemplateStop(TempTemplateStop stop);

    void updateTempTemplateStop(TempTemplateStop stop);

    List<TempTemplateStop> getTemplateStop(Map<String, Object> param);

    List<Map<String,String>> getTemplateInfos(Map<String,String> map);

    //***************************************************************************
}
