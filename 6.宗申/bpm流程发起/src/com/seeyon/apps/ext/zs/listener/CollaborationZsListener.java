package com.seeyon.apps.ext.zs.listener;

import com.seeyon.apps.collaboration.event.CollaborationFinishEvent;
import com.seeyon.apps.collaboration.event.CollaborationStopEvent;
import com.seeyon.apps.ext.zs.util.JDBCUtil;
import com.seeyon.ctp.util.annotation.ListenEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollaborationZsListener {

    @ListenEvent(event = CollaborationFinishEvent.class, async = true)
    public void finish(CollaborationFinishEvent event) {
        Long summaryId = event.getSummaryId();
        String sql = "select a.COMPLETE_TIME,(select name from ORG_MEMBER where id= a.MEMBER_ID) memberName,(select OU.name from ORG_UNIT ou,ORG_MEMBER om where OM.id=a.MEMBER_ID and OM.ORG_DEPARTMENT_ID=OU.id) deptName,c.content,case c.ext_att4 when 'disagree' then '不同意' when 'agree' then '同意' when 'haveRead' then '已阅' else '' end result  " +
                " from CTP_COMMENT_ALL c,CTP_AFFAIR a where MODULE_ID=" + summaryId.longValue() + " and c.AFFAIR_ID=a.id order by a.COMPLETE_TIME asc";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        Map<String,Object> map=new HashMap<>();
        map.put("summaryId",summaryId);
        map.put("data",list);
        System.out.println(map);

    }

    @ListenEvent(event = CollaborationStopEvent.class, async = true)
    public void stop(CollaborationStopEvent event) {
        Long summaryId = event.getSummaryId();
        String sql = "select a.COMPLETE_TIME,(select name from ORG_MEMBER where id= a.MEMBER_ID) memberName,(select OU.name from ORG_UNIT ou,ORG_MEMBER om where OM.id=a.MEMBER_ID and OM.ORG_DEPARTMENT_ID=OU.id) deptName,c.content,case c.ext_att4 when 'disagree' then '不同意' when 'agree' then '同意' when 'haveRead' then '已阅' else '' end result  " +
                " from CTP_COMMENT_ALL c,CTP_AFFAIR a where MODULE_ID=" + summaryId.longValue() + " and c.AFFAIR_ID=a.id order by a.COMPLETE_TIME asc";
        List<Map<String, Object>> list = JDBCUtil.doQuery(sql);
        System.out.println(list);

    }


}
