package com.seeyon.apps.ext.selectPeople.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.selectPeople.dao.JtldEntityDao;
import com.seeyon.apps.ext.selectPeople.po.Formson0174;
import com.seeyon.ctp.common.AppContext;

import java.util.List;
import java.util.Map;

/**
 * 鍛ㄥ垬鎴�   2019/5/7
 */
public class JtldEntityManagerImpl implements JtldEntityManager {

    private JtldEntityDao jtldEntityDao;

    @Override
    public List<Map> selectPeopleByDeskWorkId(String id) {
        return jtldEntityDao.selectPeopleByDeskWorkId(id);
    }

    @Override
    public List<Map> selectDeskWork(String name) {
        return jtldEntityDao.selectDeskWork(name);
    }

    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        getJtldEntityDao().insertFormson0174(formson0174);
    }

    public JtldEntityDao getJtldEntityDao() {
        if (jtldEntityDao == null) {
            jtldEntityDao = (JtldEntityDao) AppContext.getBean("jtldEntityDao");
        }
        return jtldEntityDao;
    }

    @Override
    public List<Map> selectJtldEntity(String name) {
        List<Map> list = getJtldEntityDao().selectJtldEntity(name);
//        JSONArray json = JSON.parseArray(JSON.toJSONString(list));
        return list;
    }

    @Override
    public List<Map> selectFormmain0148(String name) {
        List<Map> list = getJtldEntityDao().selectFormmain0148(name);
//        JSONArray json = JSON.parseArray(JSON.toJSONString(list));

        return list;
    }

    @Override
    public List<Map> selectFormmain0106(String name) {
        List<Map> list = getJtldEntityDao().selectFormmain0106(name);
//        JSONArray json = JSON.parseArray(JSON.toJSONString(list));
        return list;
    }

    @Override
    public List<Map> selectFormmain0087(String name) {
        List<Map> list = getJtldEntityDao().selectFormmain0087(name);
//        JSONArray json = JSON.parseArray(JSON.toJSONString(list));
        return list;
    }
}
