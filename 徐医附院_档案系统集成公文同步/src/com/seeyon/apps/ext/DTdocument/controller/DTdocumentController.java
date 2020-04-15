package com.seeyon.apps.ext.DTdocument.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.DTdocument.manager.DTdocumentManager;
import com.seeyon.apps.ext.DTdocument.manager.DTdocumentManagerImpl;
import com.seeyon.apps.ext.DTdocument.manager.SyncOrgData;
import com.seeyon.apps.ext.DTdocument.manager.WriteMiddleData;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;


public class DTdocumentController extends BaseController {

    private DTdocumentManager manager = new DTdocumentManagerImpl();

    public ModelAndView syncdata(HttpServletRequest request, HttpServletResponse response) {
        /**
         * 同步公文
         */
        SyncOrgData.getInstance().syncSummary();
//        SyncOrgData.getInstance().copyEdoc();
//        SyncOrgData.getInstance().copyAttachment();
        // 清空临时表的数据
//        SyncOrgData.getInstance().clearTemporary();
        WriteMiddleData.getInstance().batchSqlByType();

        return null;
    }
}
