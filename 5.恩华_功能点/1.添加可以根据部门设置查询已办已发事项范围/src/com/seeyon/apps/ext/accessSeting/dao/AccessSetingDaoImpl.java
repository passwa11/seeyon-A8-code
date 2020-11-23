package com.seeyon.apps.ext.accessSeting.dao;

import com.seeyon.apps.ext.accessSeting.util.JDBCUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AccessSetingDaoImpl implements AccessSetingDao {

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
    public List<Map<String, Object>> queryAllDepartment() {
        String sql = "select id,name,code,type,path,org_account_id from ORG_UNIT where IS_ENABLE=1 and IS_DELETED=0 and type='Department'";
        try {
            return JDBCUtil.doQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
