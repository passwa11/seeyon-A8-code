package com.seeyon.apps.ext.kypending.listener;

import com.seeyon.apps.ext.kypending.event.GovdocRepalEvent;
import com.seeyon.apps.ext.kypending.event.GovdocStopEvent;
import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.apps.ext.kypending.util.ReadConfigTools;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GovdocCancelListener {

    @ListenEvent(event = GovdocStopEvent.class, async = true)
    public void stop(GovdocStopEvent event) {
        CtpAffair ctpAffair = event.getCurrentAffair();
        Map<String, Object> map2 = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
        map2.put("task_id", ctpAffair.getObjectId().longValue() + "");
        map2.put("task_delete_flag", 1);
        map2.put("process_instance_id", ctpAffair.getProcessId());
        map2.put("process_delete_flag", 1);
        mapList.add(map2);

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
    }


    @ListenEvent(event = GovdocRepalEvent.class, async = true)
    public void cancel(GovdocRepalEvent event) {
        List<CtpAffair> list = event.getList();
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map2 = null;

        for (CtpAffair affair : list) {
            map2 = new HashMap<>();
            map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
            map2.put("task_id", affair.getObjectId().longValue() + "");
            map2.put("task_delete_flag", 1);
            map2.put("process_instance_id", affair.getProcessId());
            map2.put("process_delete_flag", 1);
            mapList.add(map2);
        }

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
    }

}
