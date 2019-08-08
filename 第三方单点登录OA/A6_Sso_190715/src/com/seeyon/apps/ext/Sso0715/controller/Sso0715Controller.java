package com.seeyon.apps.ext.Sso0715.controller;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.Sso0715.manager.Sso0715Manager;
import com.seeyon.apps.ext.Sso0715.manager.Sso0715ManagerImpl;
import com.seeyon.apps.ext.Sso0715.pojo.Member;
import com.seeyon.apps.ext.Sso0715.pojo.Pocket;
import com.seeyon.apps.ext.Sso0715.util.JDBCUtil;
import com.seeyon.apps.ext.Sso0715.util.PocketUtil;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.util.annotation.NeedlessCheckLogin;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 周刘成   2019/7/16
 */
public class Sso0715Controller extends BaseController {

    private Sso0715Manager sso0715Manager = new Sso0715ManagerImpl();

    public ModelAndView syncSsoAccount(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            List<Map<String, Object>> oaList = sso0715Manager.selectAccountOA();
            /**
             * 获取所有的人员信息
             */
            String memberUrl = "https://192.168.10.17:4430/cgi-bin/roster/department/get_member";
            String memberStr = PocketUtil.getAllMember(memberUrl, "1", "1");
            Pocket pocket = JSONObject.parseObject(memberStr, Pocket.class);
            System.out.println(pocket.getMember().size());
            String token = PocketUtil.getToken();
            List<Member> memberList = pocket.getMember();

            List<LinkedHashMap<String, Object>> yx = new ArrayList<LinkedHashMap<String, Object>>();
            LinkedHashMap<String, Object> m = null;
            if (null != oaList && null != memberList) {
                for (int i = 0; i < oaList.size(); i++) {
                    String phone = (String) oaList.get(i).get("phone");
                    if (!"".equals(phone) && null != phone) {
                        for (int j = 0; j < memberList.size(); j++) {
                            String thirdPhone = memberList.get(j).getAlias();
                            if (!"".equals(thirdPhone) && null != thirdPhone) {
                                if (phone.trim().equals(thirdPhone.trim())) {
                                    m = new LinkedHashMap<String, Object>();
                                    m.put("thirdpart_account", (String) memberList.get(j).getAlias());
                                    m.put("oa_account", (String) oaList.get(i).get("login_name"));
                                    yx.add(m);
                                }
                            }
                        }
                    }
                }
            }
            String sql = "insert into extend_sso(thirdpart_account,oa_account) values (?,?)";
            String delSql = "delete from extend_sso";
            boolean flag = JDBCUtil.deleteTable(delSql);
            if (flag == true) {
                JDBCUtil.batchInsert(sql, yx);
            }

            map.put("code", 0);
            map.put("message", "完成同步");

        } catch (Exception e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("message", "同步失败！！！");
        }

        JSONObject json = new JSONObject(map);
        render(response, json.toJSONString());
        return null;
    }

    /**
     * 给前台渲染json数据
     *
     * @param response
     * @param text
     */
    private void render(HttpServletResponse response, String text) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.setContentLength(text.getBytes("UTF-8").length);
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Sso0715Manager getSso0715Manager() {
        return sso0715Manager;
    }


}
