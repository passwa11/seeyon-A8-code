package com.seeyon.apps.ext.kypending.listener;

import com.seeyon.apps.collaboration.event.*;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColManagerImpl;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.apps.ext.kypending.manager.TempPendingDataManager;
import com.seeyon.apps.ext.kypending.manager.TempPendingDataManagerImpl;
import com.seeyon.apps.ext.kypending.po.TempPendingData;
import com.seeyon.apps.ext.kypending.util.JDBCUtil;
import com.seeyon.apps.ext.kypending.util.ReadConfigTools;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;

import java.text.SimpleDateFormat;
import java.util.*;

public class CollaborationEvent {


    private TempPendingDataManager dataManager = new TempPendingDataManagerImpl();

    public TempPendingDataManager getDataManager() {
        return dataManager;
    }

    public ColManager colManager = (ColManager) AppContext.getBean("colManager");

    public void setColManager(ColManager colManager) {
        this.colManager = colManager;
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 发起监听
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationStartEvent.class, async = true)
    public void doLog(CollaborationStartEvent event) throws BusinessException {
        CtpAffair ctpAffair = event.getAffair();
        TempPendingData pendingData = new TempPendingData();
        pendingData.setId(System.currentTimeMillis() + "");
        pendingData.setProcessid(ctpAffair.getProcessId());
        pendingData.setSummaryid(ctpAffair.getObjectId().longValue() + "");
        dataManager.save(pendingData);

    }

    @ListenEvent(event = CollaborationFinishEvent.class, async = true)
    public void finish(CollaborationFinishEvent event) {
        CtpAffair ctpAffair = event.getAffair();
        updateCompleteStatus(ctpAffair);
    }

    /**
     * 下一节点处理信息
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationAffairsAssignedEvent.class, async = true)
    public void assigned(CollaborationAffairsAssignedEvent event) throws BusinessException {
        List<Map<String, Object>> insertList = new ArrayList<>();

        List<CtpAffair> list = event.getAffairs();
        CtpAffair currentAffair = event.getCurrentAffair();

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        if (list.size() > 0) {
            TempPendingData pendingData = null;
            Map<String, Object> map = null;
            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> map2 = new HashMap<>();
//            map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
//            map2.put("task_id", currentAffair.getObjectId().longValue() + "");
//            map2.put("task_delete_flag", 1);
//            map2.put("process_instance_id", currentAffair.getProcessId());
//            map2.put("process_delete_flag", 1);
            Map<String, Object> currentUserMap = JDBCUtil.getMemberInfo(list.get(0).getMemberId());
            map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
            map2.put("task_id", currentAffair.getObjectId().longValue() + "");

            map2.put("actual_owner_id",currentUserMap.get("login_name"));
            map2.put("actual_owner_name",currentUserMap.get("membername"));
            map2.put("actual_owner_dept",currentUserMap.get("unitname"));
            map2.put("status", "COMPLETE");
            map2.put("end_on", simpleDateFormat.format(new Date()));
            map2.put("process_instance_id", currentAffair.getProcessId());
            map2.put("process_instance_status", "COMPLETE");
            map2.put("process_instance_ent_date", simpleDateFormat.format(new Date()));
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
                ColSummary colSummary = colManager.getSummaryById(affair.getObjectId());
                int importantLevel = colSummary.getImportantLevel().intValue();
                //金智 1: 特急 2:紧急 3:一般
                //oa:1普通，2重要，3非常重要
                switch (importantLevel) {
                    case 1:
                        map.put("priority", "3");
                        break;
                    case 2:
                        map.put("priority", "2");
                        break;
                    case 3:
                        map.put("priority", "1");
                        break;
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

    /**
     * 撤销监听
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationCancelEvent.class, async = true)
    public void cancelListener(CollaborationCancelEvent event) throws BusinessException {
        String summaryId = event.getSummaryId().longValue() + "";
        Map<String, Object> map = new HashMap<>();
        map.put("summaryid", summaryId);
        List<TempPendingData> list = dataManager.findTempPending(map);
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map2 = new HashMap<>();
        map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
        map2.put("task_id", list.get(0).getSummaryid());
        map2.put("task_delete_flag", 1);
        map2.put("process_instance_id", list.get(0).getProcessid());
        map2.put("process_delete_flag", 1);
        mapList.add(map2);

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
    }

    /**
     * 终止
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationStopEvent.class, async = true)
    public void StopListener(CollaborationStopEvent event) throws BusinessException {
//        CtpAffair ctpAffair = event.getAffair();
        String summaryId = event.getSummaryId().longValue() + "";
        Map<String, Object> map = new HashMap<>();
        map.put("summaryid", summaryId);
        List<TempPendingData> list = dataManager.findTempPending(map);
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map2 = new HashMap<>();
        map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
        map2.put("task_id", list.get(0).getSummaryid());
        map2.put("task_delete_flag", 1);
        map2.put("process_instance_id", list.get(0).getProcessid());
        map2.put("process_delete_flag", 1);
        mapList.add(map2);

        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);
    }

    /**
     * 删除事件监听
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationDelEvent.class, async = true)
    public void StopListener(CollaborationDelEvent event) throws BusinessException {
        CtpAffair ctpAffair = event.getAffair();
        updateCommon(ctpAffair);
    }

    @ListenEvent(event = CollaborationTakeBackEvent.class, async = true)
    public void TakeBack(CollaborationTakeBackEvent event) {
        CtpAffair ctpAffair = event.getAffair();
        List<CtpAffair> list = new ArrayList<>();
        list.add(ctpAffair);
        insertCommon(list);
    }

    @ListenEvent(event = CollaborationStepBackEvent.class, async = true)
    public void StepBack(CollaborationStepBackEvent event) {
        CtpAffair ctpAffair = event.getAffair();
        List<CtpAffair> list = new ArrayList<>();
        list.add(ctpAffair);
        insertCommon(list);
    }

    public void insertCommon(List<CtpAffair> list) {
        List<Map<String, Object>> insertList = new ArrayList<>();
        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
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
            map2.put("form_url_view", formUrl);

            map.put("process_instance_id", affair.getProcessId() + "");
            insertList.add(map);
        }
        KyPendingManager.getInstance().updateCtpAffair("inserttasks", todopath, appId, accessToken, insertList);

    }

    private String _todoPath;
    private String _appId;
    private String _accessToken;


    public void updateCompleteStatus(CtpAffair ctpAffair) {
        Map<String, Object> currentUserMap = JDBCUtil.getMemberInfo(ctpAffair.getMemberId());
        List<Map<String, Object>> mapList = new ArrayList<>();
        //todo 在这里将处理的数据状态改为已完成，使数据在金智已办栏目中显示
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("app_id", ReadConfigTools.getInstance().getString("appId"));
        map2.put("task_id", ctpAffair.getObjectId().longValue() + "");

        map2.put("actual_owner_id",currentUserMap.get("login_name"));
        map2.put("actual_owner_name",currentUserMap.get("membername"));
        map2.put("actual_owner_dept",currentUserMap.get("unitname"));
        map2.put("status", "COMPLETE");
        map2.put("end_on", sdf.format(new Date()));
        map2.put("process_instance_id", ctpAffair.getProcessId());
        map2.put("process_instance_status", "COMPLETE");
        map2.put("process_instance_ent_date", sdf.format(new Date()));
        String formUrl = "";
        String oaUrl = ReadConfigTools.getInstance().getString("oaurl");
        if (ctpAffair.getApp().intValue() == 1) {
            formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + currentUserMap.get("login_name") + "&affairId=" + ctpAffair.getId().longValue() + "&app=1&objectId=" + ctpAffair.getObjectId() + "";
        } else if (ctpAffair.getApp().intValue() == 4) {
            formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + currentUserMap.get("login_name") + "&affairId=" + ctpAffair.getId().longValue() + "&app=4&objectId=" + ctpAffair.getObjectId() + "";
        } else if (ctpAffair.getApp().intValue() == 6) {
            formUrl = oaUrl + "/seeyon/openPending.jsp?ticket=" + currentUserMap.get("login_name") + "&affairId=" + ctpAffair.getId().longValue() + "&app=6&objectId=" + ctpAffair.getObjectId() + "";
        }
        map2.put("form_url", formUrl);
        map2.put("form_url_view", formUrl);
        mapList.add(map2);
        String todopath = ReadConfigTools.getInstance().getString("todopath");
        String appId = ReadConfigTools.getInstance().getString("appId");
        String accessToken = ReadConfigTools.getInstance().getString("accessToken");
        KyPendingManager.getInstance().updateCtpAffair("updatetasks", todopath, appId, accessToken, mapList);

    }


    public void updateCommon(CtpAffair ctpAffair) {
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

    public String get_todoPath() {
        return _todoPath;
    }

    public void set_todoPath(String _todoPath) {
        this._todoPath = ReadConfigTools.getInstance().getString("todopath");
    }

    public String get_appId() {
        return _appId;
    }

    public void set_appId(String _appId) {
        this._appId = ReadConfigTools.getInstance().getString("appId");
    }

    public String get_accessToken() {
        return _accessToken;
    }

    public void set_accessToken(String _accessToken) {
        this._accessToken = ReadConfigTools.getInstance().getString("accessToken");
    }
}
