package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.DTdocument.dao.DTdocumentDaoImpl;
import com.seeyon.apps.ext.DTdocument.po.TempDate;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.DTdocument.dao.DTdocumentDao;
import com.seeyon.ctp.common.AppContext;

import java.util.List;


public class DTdocumentManagerImpl implements DTdocumentManager {
    private static final Log log = LogFactory.getLog(DTdocumentManagerImpl.class);

    private DTdocumentDao dTdocumentDao = new DTdocumentDaoImpl();


    @Override
    public UserPas selectDocAccount(String s) {
        return dTdocumentDao.selectDocAccount(s);
    }

    @Override
    public void setDocAccount(UserPas userPas) throws Exception {
        dTdocumentDao.setDocAccount(userPas);
    }

    @Override
    public int updateDocState(String columnName, String id) {
        return dTdocumentDao.updateDocState(columnName, id);
    }

    @Override
    public List<TempDate> getAllTempDate() {
        return dTdocumentDao.getAllTempDate();
    }
}
