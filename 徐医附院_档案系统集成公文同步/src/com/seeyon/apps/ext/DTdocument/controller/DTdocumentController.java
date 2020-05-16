package com.seeyon.apps.ext.DTdocument.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.DTdocument.manager.SyncOrgData;
import com.seeyon.apps.ext.DTdocument.manager.WriteMiddleData;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;

public class DTdocumentController extends BaseController {

    public ModelAndView syncdata(HttpServletRequest request, HttpServletResponse response) {
        /**
         * 同步公文
         */
        try {
            SyncOrgData.getInstance().syncSummary();
            WriteMiddleData.getInstance().batchSqlByType();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
