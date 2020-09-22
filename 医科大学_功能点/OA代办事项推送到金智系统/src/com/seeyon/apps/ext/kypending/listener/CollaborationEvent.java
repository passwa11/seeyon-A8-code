package com.seeyon.apps.ext.kypending.listener;

import com.seeyon.apps.collaboration.event.*;
import com.seeyon.apps.ext.kypending.manager.KyPendingManager;
import com.seeyon.apps.ext.kypending.manager.TempPendingDataManager;
import com.seeyon.apps.ext.kypending.manager.TempPendingDataManagerImpl;
import com.seeyon.apps.ext.kypending.po.TempPendingData;
import com.seeyon.apps.ext.kypending.util.JDBCUtil;
import com.seeyon.apps.ext.kypending.util.ReadConfigTools;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaborationEvent {

    private String preMemberId;

    private String nextMemberId;

    private String preAffairId;

    private String nextAffairId;

    private TempPendingDataManager dataManager = new TempPendingDataManagerImpl();

    public TempPendingDataManager getDataManager() {
        return dataManager;
    }

    /**
     * 发起监听
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationStartEvent.class, async = true)
    public void doLog(CollaborationStartEvent event) throws BusinessException {
        CtpAffair ctpAffair = event.getAffair();
        this.setPreAffairId(ctpAffair.getId().longValue() + "");
        this.setPreMemberId(ctpAffair.getMemberId().longValue() + "");
        System.out.println(ctpAffair.getId());
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
        if (list.size() > 0) {
            TempPendingData pendingData = null;
            Map<String, Object> map = null;
            for (CtpAffair affair : list) {

                //前后关联关系 start
                pendingData = new TempPendingData();
                pendingData.setId(System.currentTimeMillis() + "");
                pendingData.setSummaryid(affair.getObjectId().longValue() + "");
                pendingData.setPreaffairid(this.getPreAffairId());
                pendingData.setPrememberid(this.getPreMemberId());
                pendingData.setNextaffairid(affair.getId().longValue() + "");
                pendingData.setNextmemberid(affair.getMemberId().longValue() + "");
                pendingData.setProcessid(affair.getProcessId());
                dataManager.save(pendingData);

                //前后关联关系 end

                Map<String, Object> bliMap = getMemberInfo(affair.getMemberId());
                Map<String, Object> sendMap = getMemberInfo(affair.getSenderId());

                map = new HashMap<>();
                map.put("app_id", affair.getId().longValue() + "");
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
            String todopath = ReadConfigTools.getInstance().getString("todopath");
            String appId = ReadConfigTools.getInstance().getString("appId");
            String accessToken = ReadConfigTools.getInstance().getString("accessToken");
            KyPendingManager.getInstance().insertCtpAffair(todopath, appId, accessToken, insertList);
        }

    }

    public Map<String, Object> getMemberInfo(Long userId) {
        String sql = "select m.name membername,m.id,p.login_name,u.name unitname from ORG_MEMBER m ,ORG_PRINCIPAL p,ORG_UNIT u where m.id=p.MEMBER_ID and m.ORG_DEPARTMENT_ID=u.id and m.id =" + userId;
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        return list.get(0);
    }

    /**
     * 完成事件监听
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationFinishEvent.class, async = true)
    public void FinishListener(CollaborationFinishEvent event) throws BusinessException {
        System.out.println(event);
        System.out.println(event);
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
        map.put("app_id", "");
        map.put("task_id", "");
        map.put("task_delete_flag", "");
        map.put("task_delete_flag", "");

        System.out.println(event);
        System.out.println(event);
    }

    /**
     * 取回
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationTakeBackEvent.class, async = true)
    public void TakeBackListener(CollaborationTakeBackEvent event) throws BusinessException {
        CtpAffair ctpAffair = event.getAffair();
        System.out.println(ctpAffair);
    }


    /**
     * 回退
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationStepBackEvent.class, async = true)
    public void StepBackListener(CollaborationStepBackEvent event) throws BusinessException {
        System.out.println(event);
        System.out.println(event);
    }

    /**
     * 终止
     *
     * @param event
     * @throws BusinessException
     */
    @ListenEvent(event = CollaborationStopEvent.class, async = true)
    public void StopListener(CollaborationStopEvent event) throws BusinessException {
        CtpAffair ctpAffair = event.getAffair();
        System.out.println(ctpAffair);
        System.out.println(event);
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
        System.out.println(ctpAffair);
        System.out.println(event);
    }

    public String getPreMemberId() {
        return preMemberId;
    }

    public void setPreMemberId(String preMemberId) {
        this.preMemberId = preMemberId;
    }

    public String getNextMemberId() {
        return nextMemberId;
    }

    public void setNextMemberId(String nextMemberId) {
        this.nextMemberId = nextMemberId;
    }

    public String getPreAffairId() {
        return preAffairId;
    }

    public void setPreAffairId(String preAffairId) {
        this.preAffairId = preAffairId;
    }

    public String getNextAffairId() {
        return nextAffairId;
    }

    public void setNextAffairId(String nextAffairId) {
        this.nextAffairId = nextAffairId;
    }
}
