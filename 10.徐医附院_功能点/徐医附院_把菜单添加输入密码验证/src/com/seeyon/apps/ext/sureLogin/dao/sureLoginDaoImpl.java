package com.seeyon.apps.ext.sureLogin.dao;

import java.util.List;

import com.seeyon.apps.ext.sureLogin.po.SureLogin;
import com.seeyon.ctp.util.DBAgent;

public class sureLoginDaoImpl implements sureLoginDao {

    @Override
    public SureLogin selectByID(Long userId) {
        return DBAgent.get(SureLogin.class, userId);
    }

    @Override
    public void insertSureLogin(SureLogin sureLogin) {
        DBAgent.save(sureLogin);
    }
}
