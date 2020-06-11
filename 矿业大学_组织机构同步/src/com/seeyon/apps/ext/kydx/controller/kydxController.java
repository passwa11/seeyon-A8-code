package com.seeyon.apps.ext.kydx.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.kydx.manager.OrgDeptManager;
import com.seeyon.apps.ext.kydx.manager.OrgDeptManagerImpl;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public class kydxController extends BaseController {

    private OrgDeptManager deptManager = new OrgDeptManagerImpl();

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/kydx/index");
    }

    public ModelAndView syncData(HttpServletRequest request, HttpServletResponse response) {
        try {
//            deptManager.insertOrgDept();
//            deptManager.insertOtherOrgDept();

            deptManager.updateOrgDept();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
