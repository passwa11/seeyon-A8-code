package com.seeyon.apps.ext.KfqInform.controller;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.KfqInform.manager.KfqInformManager;
import com.seeyon.apps.ext.KfqInform.manager.KfqInformManagerImpl;
import com.seeyon.apps.ext.KfqInform.po.KfqInform;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.oainterface.impl.organizationmgr.utils.OrgDepartmentUtils;
import com.seeyon.v3x.dee.util.SqlExcuteUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class KfqInformController extends BaseController {

    private KfqInformManager informManager = new KfqInformManagerImpl();


    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String list = request.getParameter("list");
        if (null != list || !"".equals(list)) {
            JSONObject json = JSON.parseObject(list);
            JSONArray jsonArray = (JSONArray) json.get("instance");
            KfqInform inform = new KfqInform();
            User user = AppContext.getCurrentUser();

            Connection connection = JDBCAgent.getRawConnection();
            try {
                String memberId = null;
                if (jsonArray.size() > 0) {
                    delete(Long.toString(user.getId()));
                    for (int i = 0; i < jsonArray.size(); i++) {
                        memberId = ((String) jsonArray.get(i));
                        if (memberId.contains("Member")) {
                            inform.setCreateuserid(Long.toString(user.getId()));
                            inform.setId(System.currentTimeMillis() + i);
                            inform.setMemberid(memberId.substring(6));
                            inform.setSort(i + 1);
//                            inform.setMembername(getParentDept(Long.toString(user.getId()), connection, mapList));
                            informManager.saveInform(inform);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != connection) {
                    connection.close();
                }
            }
        }

        return null;
    }



    public void delete(String id) {
        Connection connection = null;
        PreparedStatement ps = null;
        String del = "delete from z_inform_temp where createuserid=?";
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(del);
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (null != ps) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (null != connection) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public KfqInformManager getInformManager() {
        return informManager;
    }
}
