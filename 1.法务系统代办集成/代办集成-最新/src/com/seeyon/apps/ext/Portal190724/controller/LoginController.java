package com.seeyon.apps.ext.Portal190724.controller;

import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.util.DesUtil;
import com.seeyon.apps.ext.Portal190724.util.GetTokenTool;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.v3x.common.web.login.CurrentUser;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 周刘成   2019-8-12
 */
public class LoginController extends BaseController {

    private Portal190724Manager manager = new Portal190724ManagerImpl();

    private GetTokenTool tokenTool = new GetTokenTool();

    private SystemProperties sys = SystemProperties.getInstance();

    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long currentUserId = CurrentUser.get().getId();
        Map<String, Object> map = manager.select(Long.toString(currentUserId));
        String appKey = tokenTool.getAppKey();
        String secretKey = tokenTool.getSecretKey();
        String tokenUrl = tokenTool.getGetTokenUrl();
        String Timespan = String.valueOf(new Date().getTime() / 1000);
        String appkeyLoginName = "";
        if (map.size() > 0) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                appkeyLoginName = appKey.concat(entry.getKey());
            }
            String loginName = DesUtil.getEncString(appkeyLoginName, appKey);

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("AppKey", appKey);
            paramMap.put("SecretKey", secretKey);
            paramMap.put("Timestamp", Timespan);
            String token = tokenTool.getTokenByPost(tokenUrl, paramMap);

            String Timespan2 = String.valueOf(new Date().getTime() / 1000);
            response.setHeader("Pragma", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
            if (!("").equals(token)) {
                request.setAttribute("Token", token);
                request.setAttribute("Timespan", Timespan2);
                request.setAttribute("AppKey", appKey);
                request.setAttribute("loginName", loginName);
                manager.updateState(Long.toString(currentUserId));
                return new ModelAndView("apps/ext/Portal190724/login");
            } else {
                request.setAttribute("lawAuthority", "1");// 未配置法律系统用户名、密码
                return new ModelAndView("sysMgr/individual/individualManager");
            }
        } else {
            request.setAttribute("lawAuthority", "1");// 未配置法律系统用户名、密码
            return new ModelAndView("sysMgr/individual/individualManager");
        }
    }


}
