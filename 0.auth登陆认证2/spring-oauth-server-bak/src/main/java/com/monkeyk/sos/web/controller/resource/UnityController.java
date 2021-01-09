package com.monkeyk.sos.web.controller.resource;

import com.monkeyk.sos.service.dto.UserJsonDto;
import com.monkeyk.sos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Shengzhao Li
 */
@Controller
@RequestMapping("/unity/")
public class UnityController {


    @Autowired
    private UserService userService;


    @RequestMapping("dashboard")
    public String dashboard() {
        return "unity/dashboard";
    }

    @RequestMapping("user_info")
    @ResponseBody
    public UserJsonDto userInfo(HttpServletRequest request, Authentication auth) {
        String token = request.getParameter("access_token");
        return userService.loadCurrentUserJsonDto();
    }



    @RequestMapping("check_user_status")
    @ResponseBody
    public UserJsonDto checkuserInfo(HttpServletRequest request, Authentication auth) {
        String token = request.getParameter("access_token");
        return userService.loadCurrentUserJsonDto();
    }


}