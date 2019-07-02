package com.seeyon.ctp.workflow.designer.dao;

import com.seeyon.ctp.workflow.designer.util.JDBCUtil;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/7/1
 */
public class WorkitemRunDaoImpl implements WorkitemRunDao {
    @Override
    public List<Map<String, Object>> selectProcessInfo(String processid) {
        String sql ="select t.*,m.name from (select ID,PROCESSID,ACTIVITYID,PERFORMER,FINISHER,STATE from WF_WORKITEM_RUN where PROCESSID='"+processid+"' ) t LEFT JOIN ORG_MEMBER m on t.PERFORMER=m.id";
        List<Map<String, Object>> result = JDBCUtil.doQuery(sql.toString());
        return result;
    }
}
