package com.seeyon.apps.ext.xk263Email.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.xk263Email.manager.OrgMember263MailDaoManager;
import com.seeyon.apps.ext.xk263Email.manager.OrgMember263MailDaoManagerImpl;
import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.apps.ext.xk263Email.util.ZCommonUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.organization.po.OrgUnit;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class xk263EmailController extends BaseController {

    private OrgMember263MailDaoManager mailDaoManager = new OrgMember263MailDaoManagerImpl();

    public ModelAndView doSave263EmailMapper(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String mail = request.getParameter("mail263Name");

        User user = AppContext.getCurrentUser();
        OrgUnit unit = mailDaoManager.getOrgUnit(Long.toString(user.getDepartmentId()));
        OrgMember263EmailMapper m = new OrgMember263EmailMapper();
        m.setUserId(user.getId());
        m.setLoginName(user.getName());
        m.setMail263Name(mail);
        m.setStatus("1");
        m.setDeptId(Long.toString(user.getDepartmentId()));
        m.setDeptName(unit.getName());
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String f = format.format(time);
        m.setUpdateTime(f);
        Map<String, Object> map = new HashMap<>();
        try {
            mailDaoManager.insert263MailMapper(m);
            map.put("code", 0);
            map.put("msg", "success");
        } catch (Exception e) {
            map.put("code", -1);
            map.put("msg", "出错了！");
        }
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView openSetAccountPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/xk263Email/setAccount");
        return modelAndView;
    }

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginUrl = ZCommonUtil.get263LoginUrl();
        String count = ZCommonUtil.getUnreadCount();

        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("url", loginUrl);
        map.put("count", count);
        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }


    /**
     * 单点到263邮箱
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView login263Email(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginUrl = ZCommonUtil.get263LoginUrl();
        String count = ZCommonUtil.getUnreadCount();
        StringBuffer url = new StringBuffer();
        url.append("redirect:");
        url.append(loginUrl);
        return new ModelAndView(url.toString());
    }

    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
