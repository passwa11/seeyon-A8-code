package com.monkeyk.sos.web.controller.resource;

import com.monkeyk.sos.domain.CheckUserStatus;
import com.monkeyk.sos.domain.shared.security.SOSUserDetails;
import com.monkeyk.sos.domain.user.User;
import com.monkeyk.sos.service.CheckUserStatusServiceImpl;
import com.monkeyk.sos.service.dto.UserJsonDto;
import com.monkeyk.sos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Shengzhao Li
 */
@Controller
@RequestMapping("/unity/")
public class UnityController {


    @Autowired
    private UserService userService;

    @Autowired
    private CheckUserStatusServiceImpl statusService;


    @RequestMapping("dashboard")
    public String dashboard() {
        return "unity/dashboard";
    }

    @RequestMapping("user_info")
    @ResponseBody
    public UserJsonDto userInfo(HttpServletRequest request, Authentication auth) {
        SOSUserDetails map = (SOSUserDetails) auth.getPrincipal();
        User user = map.user();
        String username = user.username();
        String token = request.getParameter("access_token");
        CheckUserStatus cus = new CheckUserStatus();
        cus.setToken(token);
        cus.setLoginname(username);
        statusService.addUserStatus(cus);
        return userService.loadCurrentUserJsonDto();
    }


    @RequestMapping("check_user_status")
    @ResponseBody
    public boolean checkuserInfo(HttpServletRequest request, Authentication auth) {
        String token = request.getParameter("access_token");
        SOSUserDetails map = (SOSUserDetails) auth.getPrincipal();
        String username = map.user().username();
        List<Map<String, Object>> mapList = statusService.findAll(username);
        if (mapList.size() > 0) {
            return true;
        }
        return false;
    }


}