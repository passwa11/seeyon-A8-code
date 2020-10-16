package com.seeyon.apps.ext.sureLogin.dao;


import com.seeyon.apps.ext.sureLogin.po.SureLogin;

public interface sureLoginDao {

    SureLogin selectByID(Long userId);

    void insertSureLogin(SureLogin sureLogin);
}
