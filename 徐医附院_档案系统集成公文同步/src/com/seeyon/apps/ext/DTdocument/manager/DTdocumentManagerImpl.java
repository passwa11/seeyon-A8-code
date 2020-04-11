package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.DTdocument.dao.DTdocumentDaoImpl;
import com.seeyon.apps.ext.DTdocument.po.TempDate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.seeyon.apps.ext.DTdocument.dao.DTdocumentDao;

import java.util.List;


public class DTdocumentManagerImpl implements DTdocumentManager {
    private static final Log log = LogFactory.getLog(DTdocumentManagerImpl.class);

    private DTdocumentDao dTdocumentDao = new DTdocumentDaoImpl();

    @Override
    public List<TempDate> getAllTempDate() {
        return dTdocumentDao.getAllTempDate();
    }
}
