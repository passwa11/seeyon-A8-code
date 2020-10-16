package com.seeyon.apps.ext.Portal190724.util;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724Manager;
import com.seeyon.apps.ext.Portal190724.manager.Portal190724ManagerImpl;
import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.apps.ext.Portal190724.po.Result;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 周刘成   2019/7/24
 */
public class JsonResolveTools {
    private static Log log = LogFactory.getLog(JsonResolveTools.class);

    private Portal190724Manager portal190724Manager = new Portal190724ManagerImpl();

    private ReadConfigTools configTools = new ReadConfigTools();

    public JsonResolveTools() {
    }

    public List<Contract> getJsonStr(Long u_id, String loginName, String pas) {

        //代办事项请求地址
        String getTodos = configTools.getString("portlet.getTodoz.path");
        GetTokenTool getTokenTool = new GetTokenTool();
        Map<String, Object> getMap = getTokenTool.checkToken();
        try {
            URL url = null;
            boolean checkCN = isChinese(loginName);
            if (!checkCN) {
                url = new URL(getTodos.concat("?handleUser=").concat(loginName));
            } else {
                url = new URL(getTodos.concat("?handleUser="));
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            for (Map.Entry entry : getMap.entrySet()) {
                connection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
            connection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {// 循环读取流
                sb.append(line);
            }
            br.close();
            connection.disconnect();
            if (null != sb.toString() && !"".equals(sb.toString()) && !"null".equals(sb.toString().trim())) {
                Result result = JSONObject.parseObject(sb.toString(), Result.class);
                List<Contract> contractList = result.getResultInfo().getData();
                return contractList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean savelaws(Long u_id, String loginName, String password) {
        boolean flag = false;
        List<Contract> contractList = getJsonStr(u_id, loginName, password);
        if (null != contractList) {
            if (contractList.size() == 0) {
                return flag;
            } else {
                flag = portal190724Manager.save(u_id, contractList);
            }
        }
        return flag;
    }

    public boolean isChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

}
