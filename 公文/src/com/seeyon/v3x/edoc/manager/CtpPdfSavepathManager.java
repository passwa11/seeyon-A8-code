package com.seeyon.v3x.edoc.manager;

import com.seeyon.v3x.edoc.domain.CtpPdfSavepath;

import java.util.List;

/**
 * 周刘成   2019/4/9
 */
public interface CtpPdfSavepathManager {

    public void insertCtpPdfSavepath(CtpPdfSavepath ctpPdfSavepath);

    CtpPdfSavepath selectCtpPdfSavepathBySummaryId(String edocSummaryId);
}
