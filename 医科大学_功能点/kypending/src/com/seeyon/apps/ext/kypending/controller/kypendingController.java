package com.seeyon.apps.ext.kypending.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


public class kypendingController extends BaseController {

    private static Log LOGGER = LogFactory.getLog(kypendingController.class);


    AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");

    @Context
    UriInfo uriInfo;

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        KyPendingManager kyPendingManager = new KyPendingManager();
        kyPendingManager.sendData();
        return null;
    }
}