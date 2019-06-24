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
    public List<Map<String,Object>> selectPeopleByDeskWorkId(List<String> id) {
        StringBuilder sql = new StringBuilder();
        List<Map<String,Object>> list = null;
        if (null != id && id.size() > 0) {
            if (id.size() > 0 && id.size() == 1) {
                String s = "select f.FORMMAIN_ID,f.FIELD0003,o.NAME,f.field0002 \n" +
                        "from ( select * from ( select fs.FIELD0003,FS.FORMMAIN_ID,FS.ID,FM.FIELD0001,FM.FIELD0002 from formson_0188 fs LEFT JOIN formmain_0187 fm on FS.FORMMAIN_ID=FM.ID   ) h where h.FORMMAIN_ID='" + id.get(0) + "') f \n" +
                        "LEFT JOIN ORG_MEMBER o on f.FIELD0003=o.id";
                sql.append(s);
            } else if (id.size() > 1) {
                for (int i = 0; i < id.size(); i++) {
                    if (i != 0) {
                        sql.append(" UNION all ");
                    }
                    String s = "select f.FORMMAIN_ID,f.FIELD0003,o.NAME,f.field0002 \n" +
                            "from ( select * from ( select fs.FIELD0003,FS.FORMMAIN_ID,FS.ID,FM.FIELD0001,FM.FIELD0002 from formson_0188 fs LEFT JOIN formmain_0187 fm on FS.FORMMAIN_ID=FM.ID   ) h where h.FORMMAIN_ID='" + id.get(i) + "') f \n" +
                            "LEFT JOIN ORG_MEMBER o on f.FIELD0003=o.id";
                    sql.append(s);
                }
            }
            list = JDBCUtil.doQuery(sql.toString());
        }

        return list;
    }

    @Override
    public List<Map<String,Object>> selectDeskWork(String name) {
        String sql = "select f.id,f.FIELD0001,f.FIELD0002,f.FIELD0005 zsort from formmain_0187 f where f.FIELD0002 like '%" + name + "%' order by zsort asc";
        List<Map<String,Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String,Object>> selectJtldEntity(String name) {
        //field0001:人员编号
        //field0002:岗位编号
        //field0003:人员姓名
        //field0004:岗位名称
        String sql = "select t.id,t.state,t.field0001,t.field0002,t.field0003,t.field0004,t.field0005 zsort from formmain_0150 t where t.field0003 like '%" + name + "%' order by zsort asc" ;
        List<Map<String,Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String,Object>> selectFormmain0148(String name) {
        String sql = "select f.id,f.state,f.field0001 field0002,f.field0002 field0004,f.field0003 field0001,m.name field0003,f.FIELD0004 zsort from formmain_0148 f left join org_member m on f.field0003= m.id where f.field0002 like '%" + name + "%'  ORDER BY zsort asc";
        List<Map<String,Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String,Object>> selectFormmain0106(String name) {
        String sql = "select t.field0010 field0002,t.field0011 field0001,t.field0012,t.field0013 field0004,m.name field0003,t.field0014 zsort from FORMMAIN_0106 t  left join org_member m on t.field0011 = m.id where t.field0013 like '%" + name + "%' order by zsort asc";
        List<Map<String,Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    @Override
    public List<Map<String,Object>> selectFormmain0087(String name) {
        String sql = "select f.field0001 field0002,f.field0007 field0001,f.field0008,f.field0009,f.field0012 field0004,m.name field0003,f.FIELD0011 zsort from FORMMAIN_0087 f left join org_member m on f.field0007=m.id where f.field0010 like '%" + name + "%' order by zsort asc";
        List<Map<String,Object>> list = JDBCUtil.doQuery(sql);
        return list;
    }

    //
    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        DBAgent.saveAll(formson0174);
    }
}
