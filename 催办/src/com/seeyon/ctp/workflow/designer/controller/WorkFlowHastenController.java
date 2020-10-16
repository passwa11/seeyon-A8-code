package com.seeyon.ctp.workflow.designer.controller;

import com.seeyon.ctp.workflow.designer.WorkFlowBaseController;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.workflow.designer.manager.WorkFlowHastenManager;
import com.seeyon.ctp.workflow.designer.manager.WorkitemRunManager;
import com.seeyon.ctp.workflow.designer.manager.WorkitemRunManagerImpl;
import com.seeyon.ctp.workflow.manager.WorkFlowAppExtendInvokeManager;
import com.seeyon.ctp.workflow.vo.User;
import com.seeyon.ctp.workflow.wapi.WorkFlowAppExtendManager;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * 周刘成   2019/6/27
 */
public class WorkFlowHastenController extends WorkFlowBaseController {

    private WorkFlowHastenManager workFlowHastenManager;

    private WorkitemRunManager workitemRunManager = new WorkitemRunManagerImpl();

    public WorkFlowHastenController() {
    }

    public ModelAndView preHasten(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("ctp/workflow/workflowPreHasten");
        String appName = request.getParameter("appName");
        mav.addObject("appName", appName);
        String activityId = request.getParameter("activityId");
        mav.addObject("activityId", activityId);
        String processId = request.getParameter("processId");
        mav.addObject("processId", processId);
        List<User> userList = this.workFlowHastenManager.getMemberListFromFromCache(activityId, processId, appName);
        mav.addObject("userList", userList);
        boolean canSendPhone = false;
        WorkFlowAppExtendManager hastenManager = WorkFlowAppExtendInvokeManager.getAppManager(appName);
        if (hastenManager != null) {
            canSendPhone = hastenManager.isCanSendPhoneMessage(AppContext.currentUserId(), AppContext.currentAccountId());
        }

        List<Map<String, Object>> list = workitemRunManager.selectProcessInfo(processId);

        mav.addObject("canSendPhone", canSendPhone);
        return mav;
    }

    public ModelAndView hasten(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("");
        return mav;
    }

    public WorkFlowHastenManager getWorkFlowHastenManager() {
        return this.workFlowHastenManager;
    }

    public void setWorkFlowHastenManager(WorkFlowHastenManager workFlowHastenManager) {
        this.workFlowHastenManager = workFlowHastenManager;
    }

}
