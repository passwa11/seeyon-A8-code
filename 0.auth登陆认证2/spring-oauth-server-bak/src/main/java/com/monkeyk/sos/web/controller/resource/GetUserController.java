package com.monkeyk.sos.web.controller.resource;

import com.monkeyk.sos.domain.user.User;
import com.monkeyk.sos.service.UserService;
import com.monkeyk.sos.service.dto.UserJsonDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/userInfo/")
public class GetUserController {

    @Autowired
    private UserService userService;

    @RequestMapping("queryByUsername")
    @ResponseBody
    public UserJsonDto queryByUsername(String name, HttpServletRequest request) {
        User user = userService.findByUsername(name);
        UserJsonDto jsonDto = new UserJsonDto();
        if (null != user) {
            jsonDto.setUsername(user.username());
            jsonDto.setGuid(user.guid());
            jsonDto.setPhone(user.phone());
            jsonDto.setRealname(user.realname());
        }
        return jsonDto;
    }
}
