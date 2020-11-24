package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.po.DepartmentViewTimeRange;
import com.seeyon.apps.ext.accessSeting.util.JDBCUtil;
import com.seeyon.ctp.util.DBAgent;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AccessSetingDaoImpl implements AccessSetingDao {

    @Override
    public void saveDepartmentViewTimeRange(DepartmentViewTimeRange range) {
        DBAgent.save(range);
    }

    @Override
    public void updateDepartmentViewTimeRange(DepartmentViewTimeRange range) {
        DBAgent.update(range);
    }

    @Override
    public List<DepartmentViewTimeRange> getDepartmentViewTimeRange(Map<String, Object> map) {
        return DBAgent.find("from DepartmentViewTimeRange where deptmentId=:deptmentId", map);
    }

    @Override
    public List<Map<String, Object>> queryAllAccount() {
        String sql = "select id,name,code,type,path,org_account_id from ORG_UNIT where IS_ENABLE=1 and IS_DELETED=0 and type='Account'";
        try {
            return JDBCUtil.doQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> queryAllDepartment(Long accountid) {
        String sql = "select id,name,code,type,path,org_account_id from ORG_UNIT where IS_ENABLE=1 and IS_DELETED=0 and ORG_ACCOUNT_ID=" + accountid;
        try {
            return JDBCUtil.doQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
