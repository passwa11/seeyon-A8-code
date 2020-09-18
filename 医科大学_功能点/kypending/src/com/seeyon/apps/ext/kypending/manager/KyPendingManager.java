package com.seeyon.apps.ext.kypending.manager;

import com.alibaba.fastjson.JSONObject;
import com.aspose.imaging.internal.aR.C;
import com.seeyon.apps.ext.kypending.controller.kypendingController;
import com.seeyon.apps.ext.kypending.util.RestfulInfo;
import com.seeyon.apps.ext.kypending.util.RestfulUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.controller.BaseController;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.FlipInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KyPendingManager extends BaseController {
    private static Log LOGGER = LogFactory.getLog(kypendingController.class);

    AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public static void main(String[] args) {
        KyPendingManager manager = new KyPendingManager();
        manager.getPending();
    }

    public void sendData() {
        String url = "http://ehall.xzhmu.edu.cn/taskcenterapp/sys/taskCenter/taskReceive/pushTask.do";  //换成学校的地址
        String appId = "amp"; //换成学校的参数
        String accessToken = "bb5133cadb3b7be7bfa8618f8e2c0e44"; //换成学校的参数
        //插入的数据格式
        List<CtpAffair> list = getPending();
        /**
         * 1:是协同
         * 4:公文
         * 6：会议
         */
        for (CtpAffair affair : list) {
            System.out.println(affair.getApp());
            Map<String, Object> map = new HashMap<>();
            map.put("app_id", affair.getId().longValue() + "");
            map.put("task_id", affair.getId().longValue() + "");
            map.put("subject", affair.getSubject());
            map.put("biz_key", affair.getId().longValue() + "");
            map.put("biz_domain", "OA");
            map.put("status", "ACTIVE");
            String formUrl = "";
            if (affair.getApp().intValue() == 1) {
                formUrl = "http://222.193.95.134:8888/seeyon/openPending.jsp?ticket=100002014019&affairId=" + affair.getId().longValue() + "&app=1&objectId=" + affair.getObjectId() + "";
            } else if (affair.getApp().intValue() == 4) {
                formUrl = "http://222.193.95.134:8888/seeyon/openPending.jsp?ticket=100002014019&affairId=" + affair.getId().longValue() + "&app=4&objectId=" + affair.getObjectId() + "";
            } else if (affair.getApp().intValue() == 6) {
                formUrl = "http://222.193.95.134:8888/seeyon/openPending.jsp?ticket=100002014019&affairId=" + affair.getId().longValue() + "&app=6&objectId=" + affair.getObjectId() + "";
            }
            map.put("form_url", formUrl);
            map.put("process_instance_id", affair.getActivityId().longValue() + "");

            String insertData = JSONObject.toJSONString(map);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("appId", appId));
            params.add(new BasicNameValuePair("taskInfo", insertData));
            RestfulInfo info = new RestfulInfo();
            info.setUrl(url);
            info.setAppId(appId);
            info.setAccessToken(accessToken);
            RestfulUtil.post(info, params);
        }

    }

    public List<CtpAffair> getPending() {
        User user = AppContext.getCurrentUser();
        FlipInfo flipInfo = new FlipInfo();
        flipInfo.setNeedTotal(false);
        String pageSize = "100";
        String pageNo = "1";
        try {
            if (pageSize != null) {
                flipInfo.setSize(Integer.parseInt(pageSize));
            }
            if (pageNo != null) {
                flipInfo.setPage(Integer.parseInt(pageNo));
            }
        } catch (NumberFormatException e) {

        }

        if (flipInfo != null && user.getId() != null) {
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("memberId", user.getId());
            params.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
            params.put("delete", Boolean.valueOf(false));

            List<Integer> subStates = new ArrayList<Integer>();
            //不查substate 为15的事项
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_takeBack.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_Back.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_unRead.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_read.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_ZCDB.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_assign.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_specialBacked.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderCancel.getKey()));
            subStates.add(Integer.valueOf(SubStateEnum.col_pending_specialBackToSenderReGo.getKey()));

            params.put("subState", subStates);

            try {
                affairManager.getByConditions(flipInfo, params);
            } catch (BusinessException e) {
                LOGGER.error("获得待办affair数据报错!", e);
            }
        }
        List<CtpAffair> list = flipInfo.getData();
        return list;
    }
}
