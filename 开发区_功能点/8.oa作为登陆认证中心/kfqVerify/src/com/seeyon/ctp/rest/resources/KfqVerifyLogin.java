package com.seeyon.ctp.rest.resources;

import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.security.MessageEncoder;
import org.apache.commons.logging.Log;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("oa")
@Produces({MediaType.APPLICATION_JSON})
public class KfqVerifyLogin extends BaseResource {

    private static Log LOGGER = CtpLogFactory.getLog(KfqVerifyLogin.class);

    private final String APP_KEY = "oa987654321~=";

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("verifyLogin")
    public Response verifyLogin(Map data, @HeaderParam("appKey") String appKey) {
        Map map = new HashMap();
        if (null != appKey && !"".equals(appKey)) {
            if (appKey.equals(MD5Util.md5Encode(APP_KEY))) {
                String loginName = data.get("uid").toString();
                String sql = "select id,name from ORG_MEMBER where id=" + Long.parseLong(loginName) + "";
                if (null != loginName || !"".equals(loginName)) {
                    try {
                        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
                        if (list.size() > 0) {
                            Map<String, Object> pricipal = list.get(0);
                            String id = (pricipal.get("id")).toString();
                            Map<String, Object> dmap = new HashMap<>();
                            dmap.put("userId", id);
                            map.put("code", 0);
                            map.put("msg", "请求成功");
                            map.put("data", dmap);
                        } else {
                            map.put("code", 1);
                            map.put("msg", "账号不存在");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.error("验证登陆接口出错了" + e);
                    }
                } else {
                    map.put("code", 1);
                    map.put("msg", "账号不存在");
                }
            }
        } else {
            map.put("code", 110);
            map.put("msg", "appKey不正确！");
        }

        return Response.ok(map).build();
    }


    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("verifyAccountAndPwd")
    public Response verifyLoginByOaPwd(Map data) {
        String loginName = data.get("loginName").toString().trim();
        String pwd = data.get("pwd").toString();
        Map map = new HashMap();
        String sql = "select m.name,p.member_id,p.login_name,p.credential_value from ORG_MEMBER m,ORG_PRINCIPAL p where m.id=p.MEMBER_ID and LOGIN_NAME='" + loginName + "'";
        if (null != loginName || !"".equals(loginName)) {
            if (null != pwd || !"".equals(pwd)) {
                try {
                    MessageEncoder encoder = new MessageEncoder();
                    String encodePwd = encoder.encode(loginName, pwd);
                    List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
                    if (list.size() > 0) {
                        Map<String, Object> pricipal = list.get(0);
                        String r_loginName = (String) pricipal.get("login_name");
                        String r_pwd = (String) pricipal.get("credential_value");
                        if (r_loginName.equals(loginName)) {
                            if (r_pwd.equals(encodePwd)) {
                                String memberId = pricipal.get("member_id").toString();
//                                Set<String> set = verityPermission(memberId);
//                                Iterator<String> it = set.iterator();
//                                StringBuilder permission = new StringBuilder();
//                                while (it.hasNext()) {
//                                    permission.append(it.next() + ",");
//                                }
                                Map<String, Object> dmap = new HashMap<>();
                                dmap.put("loginName", r_loginName);
                                dmap.put("uid", memberId);
                                map.put("code", 0);
                                map.put("msg", "请求成功");
                                map.put("data", dmap);
                            } else {
                                map.put("code", 2);
                                map.put("msg", "密码不正确");
                            }
                        }
                    } else {
                        map.put("code", 1);
                        map.put("msg", "账号不存在");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("验证登陆接口出错了" + e);
                }
            } else {
                map.put("code", 2);
                map.put("msg", "密码不正确");
            }
        } else {
            map.put("code", 1);
            map.put("msg", "账号不存在");
        }
        return Response.ok(map).build();
    }


    private CacheAccessable factory = CacheFactory.getInstance(KfqVerifyLogin.class);
    private CacheMap<String, ArrayList> cacheMap = factory.createMap("kfqCache");

    public Set<String> verityPermission(String memberId) {
        List<String> idList = new ArrayList<>();
        idList.add(memberId);
        String sqlUnit = "select id,path,type from org_unit ";
        List<Map<String, Object>> listUnit = JDBCUtil.doQuery(sqlUnit);
        String sql = "select id,path from org_unit where id =(select org_department_id from org_member where id='" + memberId + "')";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        idList.add(list.get(0).get("id").toString());
        String path = list.get(0).get("path").toString();
        int length = path.length();
        int num = (length / 4) - 2;
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                String lu = path.substring(0, 4 * (2 + i));
                String unitId = getUnitId(listUnit, lu);
                idList.add(unitId);
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append("select * from (select p.description,a.user_id from PORTAL_LINK_SYSTEM p,PORTAL_LINK_ACL a where p.id=a.link_system_id ) w where user_id in (0");
        for (int i = 0; i < idList.size(); i++) {
            sb.append("," + idList.get(i));
        }

        sb.append(")");
        List<Map<String, Object>> mapList = JDBCUtil.doQuery(sb.toString());
        Set<String> set = new HashSet<>();
        for (int i = 0; i < mapList.size(); i++) {
            if ((mapList.get(i).get("description").toString()).indexOf("kfq") != -1) {
                set.add(mapList.get(i).get("description").toString().substring(4));
            }
        }
        return set;
    }

    public String getUnitId(List<Map<String, Object>> list, String len) {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i).get("path")).equals(len)) {
                result = (list.get(i).get("id")).toString();
            }
        }
        return result;
    }


}
