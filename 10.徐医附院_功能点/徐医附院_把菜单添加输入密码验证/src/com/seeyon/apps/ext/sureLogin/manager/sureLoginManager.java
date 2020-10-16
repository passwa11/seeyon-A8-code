package com.seeyon.apps.ext.sureLogin.manager;

import com.seeyon.apps.ext.sureLogin.po.SureLogin;

public interface sureLoginManager {


    SureLogin selectSureLoginByid(Long userId);

    void insertSureLogin(SureLogin sureLogin);
}
