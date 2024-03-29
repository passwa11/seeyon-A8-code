package com.seeyon.apps.ext.kydx.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.apps.ext.kydx.dao.MidData;
import com.seeyon.apps.ext.kydx.manager.*;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public class kydxController extends BaseController {

    private OrgDeptManager deptManager = new OrgDeptManagerImpl();

    private OrgLevelManager levelManager = new OrgLevelManagerImpl();

    private OrgPostManager postManager = new OrgPostManagerImpl();

    private OrgMemberManager memberManager = new OrgMemberManagerImpl();

    @Override
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/kydx/index");
    }

    public ModelAndView syncData(HttpServletRequest request, HttpServletResponse response) {
        try {

            new MidData().insertDwToOa();
            new MidData().insertAccountToOa();
            new MidData().updateAccount();
            //new MidData().deleteMorgMember();


            deptManager.insertOrgDept();
            deptManager.insertOtherOrgDept();
            deptManager.updateOrgDept();
            deptManager.deleteOrgDept();

            memberManager.insertMember();
            memberManager.updateMember();
            memberManager.deleteMember();

            memberManager.updateLdap();

            memberManager.queryDeleteMemberByGh();

            deptManager.updateIsUse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
