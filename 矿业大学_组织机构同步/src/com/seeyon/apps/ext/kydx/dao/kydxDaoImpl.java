package com.seeyon.apps.ext.kydx.dao;


import com.seeyon.apps.ext.kydx.po.OrgLevel;
import com.seeyon.apps.ext.kydx.util.SyncConnectionInfoUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class kydxDaoImpl implements kydxDao {
    @Override
    public List<OrgLevel> queryOrgLevel() {
        List<OrgLevel> levelList = new ArrayList<>();
        String sql = "select vl.id,vl.name,vl.code,vl.is_enable from V_ORG_LEVEL vl where not exists (select * from M_ORG_LEVEL ml where ml.code=vl.code)";
        Connection connection = SyncConnectionInfoUtil.getMidConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            OrgLevel orgLevel = null;
            while (rs.next()) {
                orgLevel = new OrgLevel();
                orgLevel.setOrgAccountId(new OrgCommon().getOrgAccountId());
                orgLevel.setLevelcode(rs.getString("code"));
                orgLevel.setLevelname(rs.getString("name"));
                levelList.add(orgLevel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SyncConnectionInfoUtil.closeResultSet(rs);
            SyncConnectionInfoUtil.closePrepareStatement(ps, null);
            SyncConnectionInfoUtil.closeConnection(connection);
        }

        return levelList;
    }

    @Override
    public List<OrgLevel> queryChangerLevel() {
        return null;
    }

    @Override
    public List<OrgLevel> queryNotExistLevel() {
        return null;
    }

    @Override
    public void insertOrgLevel(List<OrgLevel> list) {

    }

    @Override
    public void updateOrgLevel(List<OrgLevel> list) {

    }

    @Override
    public void deleteOrgLevel(List<OrgLevel> list) {

    }
}
