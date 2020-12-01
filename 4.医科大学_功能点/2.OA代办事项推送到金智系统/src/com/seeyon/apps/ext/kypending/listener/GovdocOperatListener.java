package com.seeyon.apps.ext.kypending.listener;

import com.seeyon.apps.edoc.event.EdocAffairsAssignedEvent;
import com.seeyon.apps.edoc.event.EdocProcessEvent;
import com.seeyon.apps.ext.kypending.event.GovdocOperationEvent;
import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.apps.ext.kypending.manager.TempPendingDataManager;
import com.seeyon.apps.ext.kypending.manager.TempPendingDataManagerImpl;
import com.seeyon.apps.ext.kypending.po.TempPendingData;
import com.seeyon.apps.ext.kypending.util.JDBCUtil;
import com.seeyon.apps.ext.kypending.util.ReadConfigTools;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.impl.GovdocSummaryManagerImpl;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GovdocOperatListener {
    TempPendingDataManager dataManager = new TempPendingDataManagerImpl();

    public GovdocSummaryManager govdocSummaryManager =(GovdocSummaryManager) AppContext.getBean("govdocSummaryManager");

    public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
        this.govdocSummaryManager = govdocSummaryManager;
    }

    //金智 1: 特急 2:紧急 3:一般
    @ListenEvent(event = GovdocOperationEvent.class, async = true)
    public void govdoc(GovdocOperationEvent event) {

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        List<Map<String, Object>> insertList = new ArrayList<>();
        String type = event.getType();
        String summaryId = event.getSummaryId();
        if (null != type) {
            if (type.equals("finish") || type.equals("cancel")) {
                CtpAffair affair = event.getCurrentAffair();
                if (null == affair) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("summaryid", summaryId);
                    List<TempPendingData> pendingData = dataManager.findTempPending(map);
                    if (pendingData.size() > 0) {
                        List<Map<String, Object>> mapList = new ArrayList<>();
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
                        map2.put("task_id", pendingData.get(0).getSummaryid());
                        map2.put("task_delete_flag", 1);
                        map2.put("process_instance_id", pendingData.get(0).getProcessid());
                        map2.put("process_delete_flag", 1);
                        mapList.add(map2);
                        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
                    }
                } else {
                    List<Map<String, Object>> mapList = new ArrayList<>();
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
                    map2.put("task_id", affair.getObjectId().longValue() + "");
                    map2.put("task_delete_flag", 1);
                    map2.put("process_instance_id", affair.getProcessId());
                    map2.put("process_delete_flag", 1);
                    mapList.add(map2);
                    KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
                }

            } else if (type.equals("assigned")) {
                List<CtpAffair> list = event.getAffairs();
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

                    /**在这里做一次保存操作，因为在终止时拿不到ctpaffair，所以在这里记录一下*/
                    TempPendingData tempPendingData = new TempPendingData();
                    tempPendingData.setId(System.currentTimeMillis() + "");
                    tempPendingData.setSummaryid(list.get(0).getObjectId().longValue() + "");
                    tempPendingData.setProcessid(list.get(0).getProcessId());
                    dataManager.save(tempPendingData);

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
                        EdocSummary signSummary = null;
                        //金智 1: 特急 2:紧急 3:一般
                        //oa :1 普通
                        //2 平急
                        //3 加急
                        //4 特急
                        //5 特提
                        try {
                            signSummary = govdocSummaryManager.getSummaryById(affair.getObjectId());
                        } catch (BusinessException e) {
                            e.printStackTrace();
                        }
                        String urgentLevel = signSummary.getUrgentLevel();
                        switch (urgentLevel){
                            case "1":
                                map.put("priority", "3");
                                break;
                            case "2":
                                map.put("priority", "2");
                                break;
                            case "3":
                                map.put("priority", "2");
                                break;
                            case "4":
                                map.put("priority", "1");
                                break;
                            case "5":
                                map.put("priority", "1");
                                break;
                            default:
                                map.put("priority", "3");
                        }


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
        } else {

        }

    }


}
