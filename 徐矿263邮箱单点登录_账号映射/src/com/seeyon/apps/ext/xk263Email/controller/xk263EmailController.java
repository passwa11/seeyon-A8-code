package com.seeyon.apps.ext.xk263Email.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.xk263Email.manager.OrgMember263EmailMapperManager;
import com.seeyon.apps.ext.xk263Email.manager.OrgMember263EmailMapperManagerImpl;
import com.seeyon.apps.ext.xk263Email.po.OrgMember263EmailMapper;
import com.seeyon.apps.ext.xk263Email.util.ZCommonUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class xk263EmailController extends BaseController {

    private static Log log = CtpLogFactory.getLog(xk263EmailController.class);

    private OrgMember263EmailMapperManager mapperManager = new OrgMember263EmailMapperManagerImpl();

    public ModelAndView openSetAccountPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        OrgMember263EmailMapper member263EmailMapper = mapperManager.selectByUserId(userId.toString());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/xk263Email/setAccount");
        modelAndView.addObject("member", member263EmailMapper);
        return modelAndView;

    }

    public ModelAndView doSave263(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String emailName = request.getParameter("mail263Name");
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        OrgMember263EmailMapper member263EmailMapper = mapperManager.selectByUserId(userId.toString());
        List<OrgMember263EmailMapper> list = mapperManager.selectBy263Name(emailName);
        if (null == member263EmailMapper) {

            if (list.size() > 0) {
                map.put("code", -1);
                map.put("msg", "此邮箱账户已被" + list.get(0).getLoginName() + "(单位:" + list.get(0).getDeptName() + ")绑定！！！");
            } else {
                OrgMember263EmailMapper mapper = new OrgMember263EmailMapper();
                mapper.setUserId(userId);
                mapper.setLoginName(user.getName());
                mapper.setMail263Name(emailName);
                mapper.setDeptId(user.getDepartmentId().toString());
                mapper.setDeptName(user.getLoginAccountName());
                mapper.setStatus("1");

                mapper.setUpdateTime(formatter.format(localDateTime));
                try {
                    mapperManager.insertOrgMember263Email(mapper);
                    map.put("code", 0);
                } catch (Exception e) {
                    log.error("设置263账户保存出错：" + e.getMessage(), e);
                    map.put("code", -1);
                }
            }

        } else {
            Long id = member263EmailMapper.getUserId();
            if (userId.longValue() == id.longValue()) {
                if(list.get(0).getMail263Name().equals(emailName)){
                    map.put("code", -1);
                    map.put("msg", "此邮箱账户已被" + list.get(0).getLoginName() + "(单位:" + list.get(0).getDeptName() + ")绑定！！！");
                }else{
                    try {
                        member263EmailMapper.setMail263Name(emailName);
                        member263EmailMapper.setUpdateTime(formatter.format(localDateTime));
                        member263EmailMapper.setDeptId(user.getDepartmentId().toString());
                        member263EmailMapper.setDeptName(user.getLoginAccountName());
                        mapperManager.updateOrgMember263Email(member263EmailMapper);
                        map.put("code", 0);
                    } catch (Exception e) {
                        log.error("设置263账户->修改出错：" + e.getMessage(), e);
                        map.put("code", -1);
                    }
                }
            }
        }


        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        OrgMember263EmailMapper member263EmailMapper = null;
        try {
            member263EmailMapper = mapperManager.selectByUserId(userId.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String loginUrl = "";
        if (null != member263EmailMapper) {
            loginUrl = ZCommonUtil.get263LoginUrl(member263EmailMapper.getMail263Name());
        }

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
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        OrgMember263EmailMapper member263EmailMapper = null;
        try {
            member263EmailMapper = mapperManager.selectByUserId(userId.toString());
            StringBuffer url = new StringBuffer();
            if (null != member263EmailMapper) {
                String loginUrl = ZCommonUtil.get263LoginUrl(member263EmailMapper.getMail263Name());
                url.append("redirect:");
                url.append(loginUrl);
            }
            return new ModelAndView(url.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
