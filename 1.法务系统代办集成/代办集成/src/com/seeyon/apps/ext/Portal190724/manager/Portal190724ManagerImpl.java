package com.seeyon.apps.ext.Portal190724.manager;


import com.seeyon.apps.ext.Portal190724.dao.Portal190724Dao;
import com.seeyon.apps.ext.Portal190724.dao.Portal190724DaoImpl;
import com.seeyon.apps.ext.Portal190724.pojo.Contract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;


public class Portal190724ManagerImpl implements Portal190724Manager {
    private static final Log log = LogFactory.getLog(Portal190724ManagerImpl.class);

    private Portal190724Dao dao = new Portal190724DaoImpl();

    @Override
    public int selectState(String s) {
        return dao.selectState(s);
    }

    @Override
    public Map select(String s) {
        return dao.select(s);
    }

    @Override
    public boolean save(Long long1, List<Contract> list) {
        return dao.save(long1, list);
    }

    @Override
    public List<Contract> getAllLaw(Long long1) {
        return dao.getAllLaw(long1);
    }
}
