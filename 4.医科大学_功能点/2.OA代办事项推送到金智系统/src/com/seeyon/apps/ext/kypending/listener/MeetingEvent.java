package com.seeyon.apps.ext.kypending.listener;

import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.apps.ext.kypending.po.TempPendingData;
import com.seeyon.apps.ext.kypending.util.JDBCUtil;
import com.seeyon.apps.ext.kypending.util.ReadConfigTools;
import com.seeyon.apps.meeting.bo.MeetingBO;
import com.seeyon.apps.meeting.event.MeetingAffairsAssignedEvent;
import com.seeyon.apps.meeting.event.MeetingFinishEvent;
import com.seeyon.apps.meeting.event.MeetingInviteEvent;
import com.seeyon.apps.meeting.event.MeetingReplyEvent;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeetingEvent {

//    @ListenEvent(event = MeetingAffairsAssignedEvent.class, async = true)
    public void m1(MeetingAffairsAssignedEvent event) throws BusinessException {
        List<Map<String, Object>> insertList = new ArrayList<>();

        List<CtpAffair> list = event.getAffairs();

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        if (list.size() > 0) {
            TempPendingData pendingData = null;
            Map<String, Object> map = null;
            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
            map2.put("task_id", list.get(0).getObjectId().longValue() + "");
            map2.put("task_delete_flag", 1);
            map2.put("process_instance_id", list.get(0).getProcessId());
            map2.put("process_delete_flag", 1);
            mapList.add(map2);

            KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);


            for (CtpAffair affair : list) {

                Map<String, Object> bliMap = JDBCUtil.getMemberInfo(affair.getMemberId());
                Map<String, Object> sendMap = JDBCUtil.getMemberInfo(affair.getSenderId());

                map = new HashMap<>();
                map.put("app_id", ReadConfigTools.getInstance().getString("appId"));
                map.put("task_id", affair.getObjectId().longValue() + "");
                map.put("created_by_ids", sendMap.get("login_name"));
                map.put("created_by_names", sendMap.get("membername"));
                map.put("created_by_depts", sendMap.get("unitname"));
                map.put("subject", affair.getSubject());
                map.put("biz_key", affair.getId().longValue() + "");
                map.put("biz_domain", "OA");
                map.put("status", "ACTIVE");
                map.put("priority", "0");

                List<Map<String, Object>> aList = new ArrayList<>();
                Map<String, Object> assmap = new HashMap<>();
                assmap.put("assign_dept", bliMap.get("unitname"));
                assmap.put("assign_id", bliMap.get("login_name"));
                assmap.put("assign_name", bliMap.get("membername"));
                aList.add(assmap);
                map.put("assignments", aList);
                String formUrl = "";
                String oaUrl = ReadConfigTools.getInstance().getString("oaurl");
                if (affair.getApp().intValue() == 1) {
                    formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + bliMap.get("login_name") + "&affairId=" + affair.getId().longValue() + "&app=1&objectId=" + affair.getObjectId() + "";
                } else if (affair.getApp().intValue() == 4) {
                    formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + bliMap.get("login_name") + "&affairId=" + affair.getId().longValue() + "&app=4&objectId=" + affair.getObjectId() + "";
                } else if (affair.getApp().intValue() == 6) {
                    formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + bliMap.get("login_name") + "&affairId=" + affair.getId().longValue() + "&app=6&objectId=" + affair.getObjectId() + "";
                }
                map.put("node_name", affair.getNodePolicy());
                map.put("node_id", affair.getActivityId());
                map.put("form_url", formUrl);
                map.put("process_instance_id", affair.getProcessId() + "");
                insertList.add(map);
            }
            KyPendingManager.getInstance().updateCtpAffair("inserttasks", todopath, appId, accessToken, insertList);

        }
    }

//    @ListenEvent(event = MeetingInviteEvent.class, async = true)
    public void re(MeetingInviteEvent event) {
        System.out.println("------");
        System.out.println("------");
        System.out.println("------");

    }

//    @ListenEvent(event = MeetingReplyEvent.class, async = true)
    public void re(MeetingReplyEvent event) {
        System.out.println("------");
        System.out.println("------");
        System.out.println("------");

    }

//    @ListenEvent(event = MeetingFinishEvent.class, async = true)
    public void finish(MeetingFinishEvent event) {
        MeetingBO meeting = event.getMeeting();
        Map<String, Object> map = null;
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map2 = new HashMap<>();
        map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
        map2.put("task_id", meeting.getId() + "");
        map2.put("task_delete_flag", 1);
        mapList.add(map2);

//        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);

    }


}
