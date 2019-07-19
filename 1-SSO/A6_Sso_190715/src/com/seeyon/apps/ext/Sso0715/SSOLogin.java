package com.seeyon.apps.ext.Sso0715;

import com.seeyon.apps.ext.Sso0715.util.JDBCUtil;
import com.seeyon.apps.ext.Sso0715.util.StringHandlerUtil;
import com.seeyon.ctp.portal.sso.SSOLoginHandshakeAbstract;
import java.util.Map;

/**
 * 周刘成   2019/7/15
 */
public class SSOLogin extends SSOLoginHandshakeAbstract {
    @Override
    public String handshake(String ticket) {
//ticket
        if (ticket == null || ticket.equals("")) {
            return null;
        }
        String t = "";
        Map<String, Object> map = null;
        String userName = "";
        try {
            t = StringHandlerUtil.decode(ticket);
//            oracle 的写法
//            String sql = "select ID,THIRDPART_ACCOUNT,OA_ACCOUNT from EXTEND_SSO where THIRDPART_ACCOUNT='" + t + "' and rownum =1";
//            mysql的写法
            String sql = "select ID,THIRDPART_ACCOUNT,OA_ACCOUNT from EXTEND_SSO where THIRDPART_ACCOUNT='" + t + "'  limit 1";
            map = JDBCUtil.doQueryByOne(sql);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if ("OA_ACCOUNT".equalsIgnoreCase(key)) {
                    userName = (String) entry.getValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userName;
    }

    @Override
    public void logoutNotify(String ticket) {
        System.out.println("out success!");
        // TODO Auto-generated method stub
    }

}
