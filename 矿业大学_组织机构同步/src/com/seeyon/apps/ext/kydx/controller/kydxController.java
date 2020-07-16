package com.seeyon.apps.ext.kydx.controller;

import javax.servlet.http.HttpServletRequest;

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
            deptManager.insertOrgDept();

            deptManager.insertOtherOrgDept();

            deptManager.updateOrgDept();

            deptManager.deleteOrgDept();

//            levelManager.insertOrgLevel();
//            levelManager.updateOrgLevel();
//            levelManager.deleteOrgLevel();

//            postManager.insertPost();

//            postManager.updatePost();
//            postManager.deletePost();
            memberManager.insertMember();
            memberManager.updateMember();
            memberManager.deleteMember();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
