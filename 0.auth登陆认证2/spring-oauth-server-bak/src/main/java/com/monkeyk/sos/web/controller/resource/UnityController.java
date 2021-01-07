package com.monkeyk.sos.web.controller.resource;

import com.monkeyk.sos.service.dto.UserJsonDto;
import com.monkeyk.sos.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    public UserJsonDto userInfo() {
        return userService.loadCurrentUserJsonDto();
    }

    @RequestMapping("verifyLoginStatus")
    @ResponseBody
    public String verifyLoginStatus(HttpServletRequest request, HttpServletResponse response) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String sessioinId=attr.getSessionId();
        attr.getRequest().getCookies();
        return null;
    }

}