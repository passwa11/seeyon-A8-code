package com.seeyon.apps.ext.kypending.manager;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.ext.kypending.controller.kypendingController;
import com.seeyon.apps.ext.kypending.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KyPendingManager {
    private static Log LOGGER = LogFactory.getLog(kypendingController.class);

    private static KyPendingManager kyPendingManager;

    public static KyPendingManager getInstance() {
        if (null == kyPendingManager) {
            return new KyPendingManager();
        } else {
            return kyPendingManager;
        }
    }

    public void updateCtpAffair(String type, String url, String appId, String accessToken, List<Map<String, Object>> inserttaskList) {
        if (inserttaskList.size() > 0) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put(type, inserttaskList);
            String insertData = JSONObject.toJSONString(map1);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("appId", appId));
            params.add(new BasicNameValuePair("taskInfo", insertData));
            RestfulInfo info = new RestfulInfo();
            info.setUrl(url);
            info.setAppId(appId);
            info.setAccessToken(accessToken);
            String resutl = RestfulUtil.post(info, params);
            String s=resutl;
        }
    }

}
