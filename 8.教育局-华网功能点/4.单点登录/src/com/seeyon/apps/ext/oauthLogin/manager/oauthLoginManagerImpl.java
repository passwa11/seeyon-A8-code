package com.seeyon.apps.ext.oauthLogin.manager;

import com.seeyon.apps.ext.oauthLogin.dao.oauthLoginDaoImpl;
import com.seeyon.apps.ext.oauthLogin.po.LoginRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.oauthLogin.dao.oauthLoginDao;

import java.util.List;
import java.util.Map;

public class oauthLoginManagerImpl implements oauthLoginManager {
    private static final Log log = LogFactory.getLog(oauthLoginManagerImpl.class);

    private oauthLoginDao demoDao = new oauthLoginDaoImpl();

    @Override
    public String selectLoginNameByCode(String code) {
        return demoDao.selectLoginNameByCode(code);
    }

    @Override
    public void saveLoginRecord(LoginRecord loginRecord) {
        demoDao.saveLoginRecord(loginRecord);
    }

    @Override
    public void updateLoginRecord(LoginRecord loginRecord) {
        demoDao.updateLoginRecord(loginRecord);
    }

    @Override
    public List<LoginRecord> selectLoginRecordByLoginName(Map<String, Object> map) {
        return demoDao.selectLoginRecordByLoginName(map);
    }
}