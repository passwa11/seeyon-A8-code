package com.seeyon.cap4.form.controller;

import com.seeyon.cap4.form.manager.CheckFlowManager;
import com.seeyon.cap4.form.util.PageUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description 流程校验controller
 * </br>
 * @create by fuqiang
 * @create at 2019-06-22 12:00
 * @see com.seeyon.cap4.form.controller
 * @since v7.1sp
 */
public class CheckFlowController extends BaseController {

    private Log log = CtpLogFactory.getLog(CheckFlowController.class);

    private CheckFlowManager checkFlowManager;

    public CheckFlowManager getCheckFlowManager() {
        return checkFlowManager;
    }

    public void setCheckFlowManager(CheckFlowManager checkFlowManager) {
        this.checkFlowManager = checkFlowManager;
    }

    /**
     * 保存流程时，判断当前流程所对应的表单中，是否有相对应的自定义的表单控件
     * @param request
     * @param response
     */
    public void onSaveWorkFlow(HttpServletRequest request, HttpServletResponse response) {
        String formAppId = request.getParameter("formAppId");
        String processXml = request.getParameter("processXml");
        String processId = request.getParameter("processId");
        String rs;
        try {
            rs = checkFlowManager.onSaveWorkFlow(formAppId, processXml, processId);
        }catch (Exception e) {
            log.error("流程检查校验异常：", e);
            rs = PageUtil.getResult("-1", "系统繁忙："
                    + e.getMessage());
        }
        PageUtil.renderJSON(response, rs);
    }
}
