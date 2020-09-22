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

    public void insertCtpAffair(String url, String appId, String accessToken, List<Map<String, Object>> inserttaskList) {
        if (inserttaskList.size() > 0) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("inserttasks", inserttaskList);
            String insertData = JSONObject.toJSONString(map1);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("appId", appId));
            params.add(new BasicNameValuePair("taskInfo", insertData));
            RestfulInfo info = new RestfulInfo();
            info.setUrl(url);
            info.setAppId(appId);
            info.setAccessToken(accessToken);
            String resutl = RestfulUtil.post(info, params);
            System.out.println(resutl);
        }
    }


//    public void eachMemberToSendData() {
//        KyPendingManager manager = new KyPendingManager();
//        String sql = "select m.name membername,m.id,p.login_name,u.name unitname from ORG_MEMBER m ,ORG_PRINCIPAL p,ORG_UNIT u where m.id=p.MEMBER_ID and m.ORG_DEPARTMENT_ID=u.id";
//        List<Map<String, Object>> list = null;
//        if (ConstantUtil.DEBUGGER) {
//            list = new ArrayList<>();
//            Map<String, Object> oMap = new HashMap<>();
//            oMap.put("membername", "教师测试账号");
//            oMap.put("id", "1671034040412032430");
//            oMap.put("login_name", "999992016998");
//            oMap.put("unitname", "教研室（测试）");
//            list.add(oMap);
//
//        } else {
//            list = JDBCUtil.doQuery(sql);
//        }
//        for (Map<String, Object> map : list) {
//            String username = (String) map.get("membername");
//            String userId = map.get("id").toString();
//            String loginName = (String) map.get("login_name");
//            String unitname = (String) map.get("unitname");
//            sendData(username, userId, loginName, unitname);
//        }
//
//    }
//    public void sendData(String username, String userid, String loginName, String unitname) {
//
//        String url = "";
//        String appId = "";
//        String accessToken = "";
//
////        url = "http://apis.xzhmu.edu.cn/mdm_taskcenterapp-sys-taskCenter-taskReceive-pushTask-dodo/ProxyService/taskcenterapp-sys-taskCenter-taskReceive-pushTask-dodoProxyService";  //换成学校的地址
//        url = "http://ehall.xzhmu.edu.cn/taskcenterapp/sys/taskCenter/taskReceive/pushTask.do";
//        appId = "amp"; //换成学校的参数
//        accessToken = "bb5133cadb3b7be7bfa8618f8e2c0e44"; //换成学校的参数
//        //插入的数据格式
//        List<CtpAffair> list = getPending(Long.parseLong(userid));
//        /**
//         * 1:是协同
//         * 4:公文
//         * 6：会议
//         */
//        List<Map<String, Object>> inserttaskList = new ArrayList<>();
//        Map<String, Object> map = null;
//        for (CtpAffair affair : list) {
//            map = new HashMap<>();
//            map.put("app_id", affair.getId().longValue() + "");
//            map.put("task_id", affair.getId().longValue() + "");
//            map.put("created_by_ids", loginName);
//            map.put("created_by_names", username);
//            map.put("created_by_depts", unitname);
//            map.put("subject", affair.getSubject());
//            map.put("biz_key", affair.getId().longValue() + "");
//            map.put("biz_domain", "OA");
//            map.put("status", "ACTIVE");
//            map.put("priority", "0");
//
//            List<Map<String, Object>> aList = new ArrayList<>();
//            Map<String, Object> assmap = new HashMap<>();
//            assmap.put("assign_dept", unitname);
//            assmap.put("assign_id", loginName);
//            assmap.put("assign_name", username);
//            aList.add(assmap);
//            map.put("assignments", aList);
//            String formUrl = "";
//            String oaUrl = ReadConfigTools.getInstance().getString("oaurl");
//            if (affair.getApp().intValue() == 1) {
//                formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + loginName + "&affairId=" + affair.getId().longValue() + "&app=1&objectId=" + affair.getObjectId() + "";
//            } else if (affair.getApp().intValue() == 4) {
//                formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + loginName + "&affairId=" + affair.getId().longValue() + "&app=4&objectId=" + affair.getObjectId() + "";
//            } else if (affair.getApp().intValue() == 6) {
//                formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + loginName + "&affairId=" + affair.getId().longValue() + "&app=6&objectId=" + affair.getObjectId() + "";
//            }
//            map.put("node_name", affair.getNodePolicy());
//            map.put("node_id", affair.getActivityId());
//            map.put("form_url", formUrl);
//            map.put("process_instance_id", affair.getActivityId() + "");
//            inserttaskList.add(map);
//        }
//        if (inserttaskList.size() > 0) {
//            Map<String, Object> map1 = new HashMap<>();
//            map1.put("inserttasks", inserttaskList);
//            String insertData = JSONObject.toJSONString(map1);
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//            params.add(new BasicNameValuePair("appId", appId));
//            params.add(new BasicNameValuePair("taskInfo", insertData));
//            RestfulInfo info = new RestfulInfo();
//            info.setUrl(url);
//            info.setAppId(appId);
//            info.setAccessToken(accessToken);
//            String resutl = RestfulUtil.post(info, params);
//            System.out.println(resutl);
//        }
//
//
//    }

//    public List<CtpAffair> getPending(Long userId) {
//        FlipInfo flipInfo = new FlipInfo();
//        flipInfo.setNeedTotal(false);
//        String pageSize = "100";
//        String pageNo = "1";
//        try {
//            if (pageSize != null) {
//                flipInfo.setSize(Integer.parseInt(pageSize));
//            }
//            if (pageNo != null) {
//                flipInfo.setPage(Integer.parseInt(pageNo));
//            }
//        } catch (NumberFormatException e) {
//
//        }
//
//        if (flipInfo != null && userId != null) {
//            Map<String, Object> params = new HashMap<String, Object>();
//
//            params.put("memberId", userId);
//            params.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
//            params.put("delete", Boolean.valueOf(false));
//
//            List<Integer> subStates = new ArrayList<Integer>();
//            //不查substate 为15的事项
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_takeBack.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_Back.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_unRead.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_read.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_assign.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderCancel.getKey()));
//            subStates.add(Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderReGo.getKey()));
//
//            params.put("subState", subStates);
//
//            try {
//                affairManager.getByConditions(flipInfo, params);
//            } catch (BusinessException e) {
//                LOGGER.error("获得待办affair数据报错!", e);
//            }
//        }
//        List<CtpAffair> list = flipInfo.getData();
//        return list;
//    }
}
