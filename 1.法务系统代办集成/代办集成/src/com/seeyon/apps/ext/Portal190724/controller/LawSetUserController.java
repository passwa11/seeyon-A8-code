package com.seeyon.apps.ext.Portal190724.controller;

import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.po.UserPas;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 周刘成   2019/7/26
 */
public class LawSetUserController extends BaseController {

    private Portal190724Manager manager = new Portal190724ManagerImpl();

    /**
     * 跳转到账户设置页面
     */
    public ModelAndView toSetUserPage(HttpServletRequest request, HttpServletResponse response) {
        CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance("http://127.0.0.1:80");
        CTPRestClient client = clientManager.getRestClient();
        boolean flag = client.authenticate("gw", "gw111111");
        // 新建岗位
        Map data = new HashMap() {
            {
                put("orgAccountId", "5351864036140924954"); //单位ID
                put("code", "111111");//编号
                put("name", "子级菜单");//名称
                put("description", "子级菜单");
                put("enabled","true");//启用是否
                put("superior", "-8730818828569195440");//父级ID
                put("superiorName", "test");//父级ID
            }
        };

        String post = client.post("/orgDepartment", data, String.class);//注意：这里的Map data 切勿传入null，及时data没有信息，也需Map data = new HashMap();
        System.out.println(post);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("apps/ext/Portal190724/setAccount");
        return modelAndView;
    }

    public ModelAndView setResult(HttpServletRequest req, HttpServletResponse resp, UserPas userPas) throws Exception {
        Long currentUserID = CurrentUser.get().getId();
        userPas.setId(currentUserID + "");
        int save = manager.setAddAccount(userPas);
        if (save == 0) {
            req.setAttribute("law", "ok");
        }
        return null;

    }
}
