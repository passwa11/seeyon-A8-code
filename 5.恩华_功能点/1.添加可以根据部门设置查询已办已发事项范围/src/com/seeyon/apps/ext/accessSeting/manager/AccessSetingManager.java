package com.seeyon.apps.ext.accessSeting.manager;

import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.po.TempTemplateStop;
import com.seeyon.apps.ext.accessSeting.po.ZorgMember;
import com.seeyon.ctp.common.exceptions.BusinessException;

import java.util.List;
import java.util.Map;

public interface AccessSetingManager {
    List<Map<String,Object>> queryAllUnit(Long accountId);

    void saveDepartmentViewTimeRange(DepartmentViewTimeRange range);

    void updateDepartmentViewTimeRange(DepartmentViewTimeRange range);

    List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String,Object> range);

    //*************************************************************
    void saveTempTemplateStop(TempTemplateStop stop);

    void updateTempTemplateStop(TempTemplateStop stop);

    List<TempTemplateStop> getTemplateStop(Map<String, Object> param);

    List<Map<String,String>> getTemplateInfos(Map<String,String> map);

    //*************************************************************

    //周刘成
    List<ZorgMember> showPeople(Map<String,Object> params) throws BusinessException;

}
