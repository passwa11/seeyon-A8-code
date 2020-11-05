package com.seeyon.apps.ext.oauthLogin.manager;

import com.seeyon.apps.ext.oauthLogin.po.LoginRecord;

import java.util.List;
import java.util.Map;

public interface oauthLoginManager {

    String selectLoginNameByCode(String code);

    void saveLoginRecord(LoginRecord loginRecord);

    void updateLoginRecord(LoginRecord loginRecord);

    List<LoginRecord> selectLoginRecordByLoginName(Map<String, Object> map);

}