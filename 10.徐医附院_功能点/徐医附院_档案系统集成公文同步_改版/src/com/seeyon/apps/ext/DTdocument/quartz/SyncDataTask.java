package com.seeyon.apps.ext.DTdocument.quartz;

import com.seeyon.apps.ext.DTdocument.manager.ClearTemp40;
import com.seeyon.apps.ext.DTdocument.manager.SyncOrgDataNew;
import com.seeyon.apps.ext.DTdocument.manager.WriteMiddleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 周刘成   2019-11-4
 */
public class SyncDataTask implements Runnable {
    private Logger logger = LoggerFactory.getLogger(SyncDataTask.class);

    @Override
    public void run() {

        /**
         * 同步公文
         */
        try {
            SyncOrgDataNew.getInstance().syncSummary();
            WriteMiddleData.getInstance().batchSqlByType();
            ClearTemp40.getInstance().clearTableData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
