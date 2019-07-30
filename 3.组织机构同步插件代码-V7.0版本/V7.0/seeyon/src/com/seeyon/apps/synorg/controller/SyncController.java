package com.seeyon.apps.synorg.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.synorg.constants.SynOrgConstants.SynOrgTriggerDate;
import com.seeyon.apps.synorg.manager.SyncDataManager;
import com.seeyon.apps.synorg.manager.SyncLogManager;
import com.seeyon.apps.synorg.manager.SyncOrgConfigManager;
import com.seeyon.apps.synorg.manager.SyncOrgManager;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 组织机构同步控制器
 * @author Yang.Yinghai
 * @date 2016年5月26日下午5:22:04 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncController extends BaseController {

    /** 同步日志管理器 */
    private SyncLogManager syncLogManager;

    /** 同步管理器 */
    private SyncOrgManager syncOrgManager;

    /** 同步日志管理器 */
    private SyncOrgConfigManager syncOrgConfigManager;

    private SyncDataManager syncDataManager;

    /**
     * 进入同步设置页面
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView 页面对象
     * @throws Exception 异常
     */
    public ModelAndView showConfig(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return getConfigModelAndView();
    }

    /**
     * 同步操作处理
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView 页面对象
     * @throws Exception 异常
     */
    public ModelAndView synchOperation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String synchTimeType = request.getParameter("synchTimeType");
        boolean isAutoSync = "1".equals(request.getParameter("isAutoSync"));
        syncOrgConfigManager.setAutoSync(isAutoSync);
        if(isAutoSync) {
            // 设定时间
            if("setTime".equals(synchTimeType)) {
                int date = Integer.valueOf(request.getParameter("date"));
                int hour = Integer.valueOf(request.getParameter("hour"));
                int min = Integer.valueOf(request.getParameter("min"));
                syncOrgConfigManager.setOrgTriggerDate(SynOrgTriggerDate.values()[date], hour, min);
                syncOrgConfigManager.setSynchTimeType(0);
            }
            // 间隔时间
            else if("intervalTime".equals(synchTimeType)) {
                int intervalDay = Integer.parseInt(request.getParameter("intervalDay"));
                int intervalHour = Integer.parseInt(request.getParameter("intervalHour"));
                int intervalMin = Integer.parseInt(request.getParameter("intervalMin"));
                // 间隔时间同步
                syncOrgConfigManager.setIntervalTime((intervalDay * 24 + intervalHour), intervalMin);
                syncOrgConfigManager.setSynchTimeType(1);
            }
        }
        // 设置是否同步人员密码
        syncOrgConfigManager.setIsSynPassword("true".equals(request.getParameter("synPassword")));
        String defaultPostName = request.getParameter("defaultPostName") == null ? "" : request.getParameter("defaultPostName");
        String defaultLevelName = request.getParameter("defaultLevelName") == null ? "" : request.getParameter("defaultLevelName");
        String defaultPassword = request.getParameter("defaultPassword") == null ? "" : request.getParameter("defaultPassword");
        String rootDeptCode = request.getParameter("rootDeptCode") == null ? "" : request.getParameter("rootDeptCode");
        syncOrgConfigManager.setDefaultInfo(defaultPostName, defaultLevelName, defaultPassword, rootDeptCode);
        String scopeStr = "";
        String[] scopeList = request.getParameterValues("synModul");
        if(scopeList != null && scopeList.length > 0) {
            for(int i = 0; i < scopeList.length; i++) {
                if(i == 0) {
                    scopeStr = scopeList[i];
                } else {
                    scopeStr += "," + scopeList[i];
                }
            }
        }
        // 保存同步范围
        syncOrgConfigManager.setSyncScope(scopeStr);
        return getConfigModelAndView();
    }

    /**
     * 同步日志界面
     * @param request HTTP请求
     * @param response HTTP响应
     * @return ModelAndView 页面对象
     * @throws Exception 异常
     */
    public ModelAndView synchLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("plugin/synorg/synchLog");
        request.setAttribute("fflogList", syncLogManager.getSynLogList(new FlipInfo(), new HashMap<String, String>()));
        return mav;
    }

    /**
     * 获取配置页对象
     * @return 页面对象
     */
    private ModelAndView getConfigModelAndView() {
        ModelAndView mav = new ModelAndView("plugin/synorg/config");
        // 是否正在手动同步
        mav.addObject("isSynching", syncOrgManager.isSyning());
        // 定时同步：同步日期
        mav.addObject("date", SynOrgTask.getTriggerDate().key());
        // 定时同步：同步小时
        mav.addObject("hour", SynOrgTask.getTriggerHour());
        // 定时同步：同步分钟
        mav.addObject("min", SynOrgTask.getTriggerMinute());
        // 自动同步是否开启
        mav.addObject("isAutoSync", syncOrgConfigManager.isAutoSync());
        int hours = SynOrgTask.getIntervalHour();
        // 周期同步：天
        mav.addObject("intervalDay", hours / 24);
        // 周期同步：小时
        mav.addObject("intervalHour", hours % 24);
        // 周期同步：分种
        mav.addObject("intervalMin", SynOrgTask.getIntervalMin());
        // 自动同步类型
        mav.addObject("synchTimeType", SynOrgTask.getSynchTimeType());
        // 同步范围
        mav.addObject("synScope", SynOrgTask.getSynScope());
        // 是否同步人员密码
        mav.addObject("synPassword", SynOrgTask.isSynPassword());
        // 默认岗位名称
        mav.addObject("defaultPostName", SynOrgTask.getDefaultPostName());
        // 默认职务名称
        mav.addObject("defaultLevelName", SynOrgTask.getDefaultLevelName());
        // 默认登录密码
        mav.addObject("defaultPassword", SynOrgTask.getDefaultPassword());
        // 中间库根节点编码
        mav.addObject("rootDeptCode", SynOrgTask.getRootDeptCode());
        return mav;
    }

    /**
     * Description:
     * 
     * <pre>
     * 同步数据界面
     * </pre>
     * 
     * @param request HTTP请求
     * @param response HTTP相应
     * @return ModelAndView synchData.jsp
     * @throws Exception
     */
    public ModelAndView synchData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("plugin/synorg/synchMidDB");
        return mav;
    }

    /**
     * Description:
     * 
     * <pre>
     * 中间库部门、岗位、职务数据界面
     * </pre>
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView synchMidDB(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("plugin/synorg/synchMidDB");
        String entityType = request.getParameter("type");
        Map<String, String> query = new HashMap<String, String>();
        query.put("entityType", entityType);
        mav.addObject("type", entityType);
        request.setAttribute("ffdataList", syncDataManager.getSyncData(new FlipInfo(), query));
        return mav;
    }

    /**
     * Description:
     * 
     * <pre>
     * 中间库人员数据界面
     * </pre>
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView synchMemberData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("plugin/synorg/synchMemberData");
        request.setAttribute("ffdataList", syncDataManager.getSyncMemberData(new FlipInfo(), new HashMap<String, String>()));
        return mav;
    }

    /**
     * 设置syncLogManager
     * @param syncLogManager syncLogManager
     */
    public void setSyncLogManager(SyncLogManager syncLogManager) {
        this.syncLogManager = syncLogManager;
    }

    /**
     * 设置syncOrgManager
     * @param syncOrgManager syncOrgManager
     */
    public void setSyncOrgManager(SyncOrgManager syncOrgManager) {
        this.syncOrgManager = syncOrgManager;
    }

    /**
     * 设置syncConfigManager
     * @param syncOrgConfigManager syncConfigManager
     */
    public void setSyncOrgConfigManager(SyncOrgConfigManager syncOrgConfigManager) {
        this.syncOrgConfigManager = syncOrgConfigManager;
    }

    /**
     * @param syncDataManager the syncDataManager to set
     */
    public void setSyncDataManager(SyncDataManager syncDataManager) {
        this.syncDataManager = syncDataManager;
    }
}
