package com.seeyon.v3x.edoc.dao;

import com.seeyon.v3x.edoc.domain.CtpPdfSavepath;

import java.util.List;

/**
 * 周刘成   2019/4/9
 */
public interface CtpPdfSavepathDao {

    public void insertCtpPdfSavepath(CtpPdfSavepath ctpPdfSavepath);

    void deleteCtpPdfSavepath(CtpPdfSavepath ctpPdfSavepath);

    CtpPdfSavepath selectCtpPdfSavepathBySummaryId(String edocSummaryId);

}
