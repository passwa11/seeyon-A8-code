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
            List<Map<String, Object>> mapList = getUnitData();
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
                            inform.setMembername(getParentDept(Long.toString(user.getId()), connection, mapList));
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

    public String getParentDept(String memberId, Connection connection, List<Map<String, Object>> mapList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuffer result = new StringBuffer();
        String sql = "select ORG_DEPARTMENT_ID from ORG_MEMBER where id ='" + memberId + "'";
        try {
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            String departmentId = "";
            while (rs.next()) {
                departmentId = rs.getString(1);
            }

            for (int i = 0; i < mapList.size(); i++) {
                if ((mapList.get(i).get("id").toString()).equals(departmentId)) {
                    String path = (mapList.get(i).get("path")).toString();
                    String p12 = path.substring(0, 12);
                    result.append("/");
                    result.append(getUnitName(p12, mapList));
                    if (path.length() >= 16) {
                        String p16 = path.substring(0, 16);
                        result.append("/");
                        result.append(getUnitName(p16, mapList));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public String getUnitName(String path, List<Map<String, Object>> mapList) {
        String name = "";
        for (int j = 0; j < mapList.size(); j++) {
            String p = mapList.get(j).get("path").toString();
            if (p.equals(path)) {
                name = mapList.get(j).get("name").toString();
            }
        }
        return name;
    }


    public List<Map<String, Object>> getUnitData() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> mapList = new ArrayList<>();
        String sql = "select id,name,path from ORG_UNIT";
        try {
            connection = JDBCAgent.getRawConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            Map<String, Object> map = null;
            while (rs.next()) {
                map = new HashMap<>();
                map.put("id", Long.toString(rs.getLong("id")));
                map.put("name", rs.getString("name"));
                map.put("path", rs.getString("path"));
                mapList.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != ps) {
                    ps.close();
                }
                if (null != connection) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mapList;
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
