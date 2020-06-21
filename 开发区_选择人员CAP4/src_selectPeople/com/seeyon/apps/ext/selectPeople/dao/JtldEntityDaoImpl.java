package com.seeyon.apps.ext.selectPeople.dao;

import com.seeyon.apps.ext.selectPeople.po.Formson0174;
import com.seeyon.apps.ext.selectPeople.util.JDBCUtil;
import com.seeyon.ctp.util.DBAgent;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public class JtldEntityDaoImpl implements JtldEntityDao {

    @Override
    public List<Map<String, Object>> selectZhuQu0032(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0032 ";
//        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0184 ";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectZhenBan0031(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0031 ";
//        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0183 ";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectJiGuan0030(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0030 ";
        //        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0175 ";

        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectDangZhengBan0029(String name) {
        String sqltest = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0029 ";
        //        正式
        String sql = "select id,field0001 ,field0002,field0003,field0005,field0007,(select showvalue from ctp_enum_item where id =field0005) mval from formmain_0174 ";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }


    @Override
    public List<Map<String, Object>> selectJtldEntity(String name) {
        //field0001:人员编号
        //field0002:岗位编号
        //field0003:人员姓名
        //field0004:岗位名称
        String sql = "select t.id,t.state,t.field0001,t.field0002,t.field0003,t.field0004,t.field0005 zsort,t.FIELD0006 from formmain_0150 t where t.field0003 like '%" + name + "%' order by zsort asc";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }


    //
    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        DBAgent.saveAll(formson0174);
    }
}
