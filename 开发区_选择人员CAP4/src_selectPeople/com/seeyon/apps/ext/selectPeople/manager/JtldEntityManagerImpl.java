package com.seeyon.apps.ext.selectPeople.manager;

import com.seeyon.apps.ext.selectPeople.dao.JtldEntityDao;
import com.seeyon.apps.ext.selectPeople.dao.JtldEntityDaoImpl;
import com.seeyon.apps.ext.selectPeople.po.Formson0174;

import java.util.List;
import java.util.Map;

/**
 * 鍛ㄥ垬鎴�   2019/5/7
 */
public class JtldEntityManagerImpl implements JtldEntityManager {

    private JtldEntityDao dao = new JtldEntityDaoImpl();

    @Override
    public List<Map<String, Object>> selectZhuQu0032(String name) {
        return dao.selectZhuQu0032(name);
    }

    @Override
    public List<Map<String, Object>> selectZhenBan0031(String name) {
        return dao.selectZhenBan0031(name);
    }

    @Override
    public List<Map<String, Object>> selectJiGuan0030(String name) {
        return dao.selectJiGuan0030(name);
    }

    @Override
    public List<Map<String, Object>> selectDangZhengBan0029(String name) {
        return dao.selectDangZhengBan0029(name);
    }

    @Override
    public void insertFormson0174(List<Formson0174> formson0174) {
        dao.insertFormson0174(formson0174);
    }

    @Override
    public List<Map<String, Object>> selectTwoLevelDept(String name) {
        return dao.selectTwoLevelDept(name);
    }

    @Override
    public List<Map<String, Object>> selectGonghui(String name) {
        return dao.selectGonghui(name);
    }
}
