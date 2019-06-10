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

    //


    @Override
    public List<Map> selectPeopleByDeskWorkId(String id) {
        String sql="select f.FORMMAIN_ID,f.FIELD0003,o.NAME,f.field0002 \n" +
                "from ( select * from ( select fs.FIELD0003,FS.FORMMAIN_ID,FS.ID,FM.FIELD0001,FM.FIELD0002 from formson_0291 fs LEFT JOIN formmain_0290 fm on FS.FORMMAIN_ID=FM.ID   ) h where h.FORMMAIN_ID='"+id+"') f \n" +
                "LEFT JOIN ORG_MEMBER o on f.FIELD0003=o.id";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectDeskWork(String name) {
        String sql ="select f.id,f.FIELD0001,f.FIELD0002 from formmain_0290 f where f.FIELD0002 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectJtldEntity(String name) {
        //field0001:人员编号
        //field0002:岗位编号
        //field0003:人员姓名
        //field0004:岗位名称
        String sql = "select t.id,t.state,t.field0001,t.field0002,t.field0003,t.field0004 from formmain_0150 t where t.field0003 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectFormmain0148(String name) {
        String sql = "select f.id,f.state,f.field0001 field0002,f.field0002 field0004,f.field0003 field0001,m.name field0003 from formmain_0148 f left join org_member m on f.field0003= m.id where f.field0002 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectFormmain0106(String name) {
        String sql = "select t.field0010 field0002,t.field0011 field0001,t.field0012,t.field0013 field0004,m.name field0003 from FORMMAIN_0106 t  left join org_member m on t.field0011 = m.id where t.field0013 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map> selectFormmain0087(String name) {
        String sql = "select f.field0001 field0002,f.field0007 field0001,f.field0008,f.field0009,f.field0010 field0004,m.name field0003 from FORMMAIN_0087 f left join org_member m on f.field0007=m.id where f.field0010 like '%"+name+"%'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }

    //
    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        DBAgent.saveAll(formson0174);
    }
}
