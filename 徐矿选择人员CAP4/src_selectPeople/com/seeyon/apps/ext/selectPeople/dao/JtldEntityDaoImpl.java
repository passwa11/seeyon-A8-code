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
    public List<Map> selectJtldEntity(String name) {
        String sql = "select t.id,t.state,t.start_member_id,t.start_date,t.approve_member_id,t.approve_date,t.finishedflag,t.ratifyflag,t.ratify_member_id,t.ratify_date,t.sort,t.modify_member_id,t.modify_date,t.field0001,t.field0002,t.field0003,t.field0004 from formmain_0150 t where t.field0003 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectFormmain0148(String name) {
        String sql = "select f.id,f.state,f.start_member_id,f.start_date,f.approve_member_id,f.approve_date,f.finishedflag,f.ratifyflag,f.ratify_member_id,f.ratify_date,f.sort,f.modify_member_id,f.modify_date,f.field0001,f.field0002,f.field0003,m.name from formmain_0148 f left join org_member m on f.field0003= m.id where f.field0002 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectFormmain0106(String name) {
        String sql = "select t.field0010,t.field0011,t.field0012,t.field0013,m.name from FORMMAIN_0106 t  left join org_member m on t.field0011 = m.id where t.field0013 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectFormmain0087(String name) {
        String sql = "select f.field0001,f.field0007,f.field0008,f.field0009,f.field0010,m.name from FORMMAIN_0087 f left join org_member m on f.field0007=m.id where f.field0010 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    //
    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        DBAgent.saveAll(formson0174);
    }
}
