package com.seeyon.apps.ext.selectPeople.dao;

import com.seeyon.apps.ext.selectPeople.po.Formson0174;
import com.seeyon.apps.ext.selectPeople.util.JDBCUtil;
import com.seeyon.apps.ext.selectPeople.util.KfqContants;
import com.seeyon.ctp.util.DBAgent;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public class JtldEntityDaoImpl implements JtldEntityDao {

    @Override
    public List<Map<String, Object>> selectCommon(String name, String tableName) {
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from " + tableName + "  order by FIELD0007 asc";

        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sql);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectZhuQu0032(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0032  order by FIELD0007 asc";
//        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0184  order by FIELD0007 asc";

        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sqltest);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectZhenBan0031(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0031  order by FIELD0007 asc";
//        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0183  order by FIELD0007 asc";
        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sqltest);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectJiGuan0030(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0030  order by FIELD0007 asc";
        //        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0175  order by FIELD0007 asc";
        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sqltest);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectDangZhengBan0029(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0029  order by FIELD0007 asc";
        //        正式
        String sql = "select id,field0001 ,field0002,field0004 field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0174  order by FIELD0007 asc";
        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sqltest);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectGonghui(String name) {
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0285 order by FIELD0007 asc";
        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sql);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> selectTwoLevelDept(String name) {
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0533 order by FIELD0007 asc";
        List<Map<String, Object>> list = null;
        if (KfqContants.DEBUGGER) {
            list = JDBCUtil.doQuery(sql);
        } else {
            list = JDBCUtil.doQuery(sql);
        }
        return list;
    }

    //
    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        DBAgent.saveAll(formson0174);
    }
}
