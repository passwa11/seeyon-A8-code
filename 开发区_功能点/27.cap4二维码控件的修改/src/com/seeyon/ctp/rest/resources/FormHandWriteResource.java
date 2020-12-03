package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("cap4/formHandWrite")
@Consumes({MediaType.APPLICATION_JSON})
@Produces(MediaType.APPLICATION_JSON)
public class FormHandWriteResource {
    /**
     * 返回当前用户信息
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("currntUserInfo")
    public User currntUserInfo() {
        User user = AppContext.getCurrentUser();
        return user;
    }
}
