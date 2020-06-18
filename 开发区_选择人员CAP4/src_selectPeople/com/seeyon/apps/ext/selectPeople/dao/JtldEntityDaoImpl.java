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
        String sql = "select f32.field0001,( select name from ORG_MEMBER where id =f32.field0001) field0003,f32.field0004,f32.field0005,(select showvalue from ctp_enum_item where id =f32.field0005) mval,f32.field0007 from formmain_0032 f32";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectZhenBan0031(String name) {
        String sql = "select f.field0001,( select name from ORG_MEMBER where id =f.field0001) field0003,f.field0004,f.field0005,(select showvalue from ctp_enum_item where id =f.field0005) mval,f.field0007 from formmain_0031 f";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectJiGuan0030(String name) {
        String sql = "select f.field0001,( select name from ORG_MEMBER where id =f.field0001) field0003,f.field0004,f.field0005,(select showvalue from ctp_enum_item where id =f.field0005) mval,f.field0007 from formmain_0030 f";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectDangZhengBan0029(String name) {
        String sql = "select f.field0001,( select name from ORG_MEMBER where id =f.field0001) field0003,f.field0004,f.field0005,(select showvalue from ctp_enum_item where id =f.field0005) mval,f.field0007 from formmain_0029 f";
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
