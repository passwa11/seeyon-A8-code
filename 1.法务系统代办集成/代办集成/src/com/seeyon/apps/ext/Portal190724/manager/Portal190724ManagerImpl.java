package com.seeyon.apps.ext.Portal190724.manager;


import com.seeyon.apps.ext.Portal190724.dao.Portal190724Dao;
import com.seeyon.apps.ext.Portal190724.dao.Portal190724DaoImpl;
import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
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

    @Override
    public List<Contract> getLimitLaw(Long long1) {
        return dao.getLimitLaw(long1);
    }

    @Override
    @AjaxAccess
    public FlipInfo findMoreLaw(FlipInfo fi, Map params) throws BusinessException {
        User user = AppContext.getCurrentUser();
        params.put("oauserId", user.getId());
        this.dao.findMoreLaw(fi, params);
        return fi;
    }

    @Override
    public int setAddAccount(UserPas userPas) {
        return dao.setAddAccount(userPas);
    }
}
