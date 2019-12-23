package com.seeyon.v3x.edoc.manager;

import com.seeyon.v3x.edoc.dao.CtpPdfSavepathDao;
import com.seeyon.v3x.edoc.dao.CtpPdfSavepathDaoImpl;
import com.seeyon.v3x.edoc.domain.CtpPdfSavepath;

import java.util.List;

/**
 * 周刘成   2019/4/9
 */
public class CtpPdfSavepathManagerImpl implements CtpPdfSavepathManager {

    private CtpPdfSavepathDao ctpPdfSavepathDao = new CtpPdfSavepathDaoImpl();

    @Override
    public void insertCtpPdfSavepath(CtpPdfSavepath ctpPdfSavepath) {
        getCtpPdfSavepathDao().insertCtpPdfSavepath(ctpPdfSavepath);
    }

    @Override
    public void deleteCtpPdfSavepath(CtpPdfSavepath ctpPdfSavepath) {
        getCtpPdfSavepathDao().deleteCtpPdfSavepath(ctpPdfSavepath);
    }

    @Override
    public CtpPdfSavepath selectCtpPdfSavepathBySummaryId(String edocSummaryId) {
        return ctpPdfSavepathDao.selectCtpPdfSavepathBySummaryId(edocSummaryId);
    }

    public CtpPdfSavepathDao getCtpPdfSavepathDao() {
        return ctpPdfSavepathDao;
    }
}
