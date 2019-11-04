package com.seeyon.apps.ext.DTdocument.quartz;

import com.seeyon.apps.ext.DTdocument.manager.SyncOrgData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 周刘成   2019-11-4
 */
public class SyncDataTask implements Runnable {
    private Logger logger = LoggerFactory.getLogger(SyncDataTask.class);

    @Override
    public void run() {
        /**
         * 同步机构
         */
        SyncOrgData.getInstance().syncOrgUnit();
        /**
         * 同步人员
         */
        SyncOrgData.getInstance().syncOrgMember();
        /**
         * 同步公文
         */
        SyncOrgData.getInstance().syncSummary();
        /**
         * 复制正文
         */
        SyncOrgData.getInstance().copyEdoc();
        /**
         * 复制附件
         */
        SyncOrgData.getInstance().copyAttachment();
    }
}
