package com.seeyon.apps.ext.allItems.dao;

import java.sql.SQLException;
import java.util.*;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;

public class allItemsDaoImpl implements allItemsDao {
    private static final Log LOGGER = LogFactory.getLog(allItemsDaoImpl.class);

    @AjaxAccess
    @Override
    public FlipInfo findMoreCooprationNobanjie(FlipInfo flipInfo, Map<String, Object> map) {
        StringBuffer sql = new StringBuffer();
        sql.append("select * from (");
        sql.append("select sup.*,cac.complete_time,cac.id from (");
        sql.append("select distinct subject,object_id,sender_id,(select name from ORG_MEMBER where id=sender_id) send_name,create_date,");
        sql.append("MEMBER_ID,(select name from ORG_MEMBER where id=MEMBER_ID) member_name  ,PRE_APPROVER,(select name from ORG_MEMBER where id=PRE_APPROVER) PRE_name  from (");
        sql.append("select * from (");
        sql.append("select ca.* from COL_SUMMARY cs LEFT JOIN  (select * from CTP_AFFAIR where state =4) ca on cs.id=CA.object_id");
        if (map.get("templetIds") != null && !"null".equals(map.get("templetIds")) && !"".equals(map.get("templetIds"))) {
            sql.append(" AND cs.TEMPLETE_ID IN (" + map.get("templetIds") + ")");
        }
        sql.append(") s where 1=1 and s.SORT_WEIGHT=-1 and is_finish <>1");
        sql.append(") ORDER BY create_date desc");
        sql.append(") sup LEFT JOIN ");
        sql.append("(select max(COMPLETE_TIME) complete_time,max(id) id,object_id from CTP_AFFAIR where COMPLETE_TIME is not null GROUP BY object_id) cac on sup.object_id=cac.object_id");
        sql.append(") osup where 1=1");
        if (map.get("title") != null && map.get("title") != "") {
            sql.append(" and  osup.subject like '%"+map.get("title") +"%'");
        }
        if (map.get("sender") != null) {
            sql.append(" AND osup.send_name like '%" + map.get("sender") + "%'");
        }
        List<Map<String, Object>> nobj = null;
        try (JDBCAgent jdbcAgent = new JDBCAgent(true)) {
            jdbcAgent.execute(sql.toString());
            nobj = jdbcAgent.resultSetToList();
        } catch (SQLException e) {
            LOGGER.error("办结栏目更多页加条件查询获取异常", e);
        } catch (BusinessException b) {
            LOGGER.error("办结栏目更多页加条件查询获取异常", b);
        }
        int page = flipInfo.getPage();
        int size = flipInfo.getSize();
        flipInfo.setTotal((nobj).size());
        List newList = new ArrayList();
        int currIdx = page > 1 ? (page - 1) * size : 0;
        for (int i = 0; i < size && i < (nobj).size() - currIdx; ++i) {
            newList.add((nobj).get(currIdx + i));
        }
        flipInfo.setData(nobj);
        return flipInfo;
    }

    @Override
    public List<Map<String, Object>> findCooprationNobanjie(String templetIds) {
        StringBuffer sql = new StringBuffer();
        sql.append("select sup.*,cac.complete_time,cac.id from (");
        sql.append("select distinct subject,object_id,sender_id,(select name from ORG_MEMBER where id=sender_id) send_name,create_date,");
        sql.append("MEMBER_ID,(select name from ORG_MEMBER where id=MEMBER_ID) member_name  ,PRE_APPROVER,(select name from ORG_MEMBER where id=PRE_APPROVER) PRE_name  from (");
        sql.append("select * from (");
        sql.append("select ca.* from COL_SUMMARY cs LEFT JOIN  (select * from CTP_AFFAIR where state =4) ca on cs.id=CA.object_id");
        if (templetIds != null && !templetIds.equals("null") && !templetIds.equals("")) {
            sql.append(" and cs.TEMPLETE_ID='" + templetIds + "'");
        }
        sql.append(") s where 1=1 and s.SORT_WEIGHT=-1 and is_finish <>1");
        sql.append(") ORDER BY create_date desc");
        sql.append(") sup LEFT JOIN ");
        sql.append("(select max(COMPLETE_TIME) complete_time,max(id) id,object_id from CTP_AFFAIR where COMPLETE_TIME is not null GROUP BY object_id) cac on sup.object_id=cac.object_id");
        JDBCAgent jdbcAgent = new JDBCAgent(true);
        List<Map<String, Object>> wbj = null;
        try {
            jdbcAgent.execute(sql.toString());
            wbj = jdbcAgent.resultSetToList();
        } catch (Exception e) {
            LOGGER.error("办结栏目获取异常：", e);
        }
        return wbj;
    }

    @Override
    public FlipInfo findMoreCooprationXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map) {
        List<Map<String, Object>> banjieList = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
//        sql.append("select DISTINCT id,subject,start_date,current_nodes_info,TEMPLETE_ID,name,START_MEMBER_ID from (");
//        sql.append("select s.id,s.subject,s.start_date,s.current_nodes_info,CA.id aid,s.TEMPLETE_ID,s.name,s.start_member_id  from (");
//        sql.append("select * from (select s.*,m.name from COL_SUMMARY s LEFT JOIN ORG_MEMBER m on s.START_MEMBER_ID=m.ID) sm where SM.current_nodes_info is null  and SM.state =3) s ");
//        sql.append("LEFT  JOIN CTP_AFFAIR ca on s.id=CA.OBJECT_ID) ss where aid is not null");
        sql.append("select * from (");
        sql.append("select id,subject,START_DATE,FINISH_DATE,TEMPLETE_ID,START_MEMBER_ID,(select name from ORG_MEMBER where id=START_MEMBER_ID) start_name,current_nodes_info ");
        sql.append("from COL_SUMMARY where START_MEMBER_ID='6124365271652712753'  and finish_date is not null ");
        sql.append(") where 1=1");
        String condition = "";
//标题
        if (map.get("title") != null) {
            sql.append(" AND subject like '%" + map.get("title") + "%'");
        }
//开始时间
        if (map.get("beginTime") != null) {
            sql.append(" AND start_date >=to_date('" + map.get("beginTime") + "','yyyy-mm-dd')");
        }
//截止时间
        if (map.get("endTime") != null) {
            sql.append(" AND start_date <=to_date('" + map.get("endTime") + "','yyyy-mm-dd')");
        }
//发起人
        if (map.get("sender") != null) {
            sql.append(" AND start_name like '%" + map.get("sender") + "%'");
        }
//        模板
        if (map.get("templetIds") != null && !"".equals(map.get("templetIds"))) {
            sql.append(" AND TEMPLETE_ID IN (" + map.get("templetIds") + ")");
        }
        sql.append(" order by start_date desc");
        try (JDBCAgent jdbcAgent = new JDBCAgent(true)) {
            jdbcAgent.execute(sql.toString());
            banjieList = jdbcAgent.resultSetToList();
        } catch (SQLException e) {
            LOGGER.error("办结栏目更多页加条件查询获取异常", e);
        } catch (BusinessException b) {
            LOGGER.error("办结栏目更多页加条件查询获取异常", b);
        }
        List<Map<String, Object>> addNodeInfoList = new ArrayList<>();
        for (int i = 0; i < banjieList.size(); i++) {
            Map<String, Object> map1 = banjieList.get(i);
            map1.put("current_nodes_info", "已结束");
            addNodeInfoList.add(map1);
        }
        int page = flipInfo.getPage();
        int size = flipInfo.getSize();
        flipInfo.setTotal((banjieList).size());
        List newList = new ArrayList();
        int currIdx = page > 1 ? (page - 1) * size : 0;

        for (int i = 0; i < size && i < (banjieList).size() - currIdx; ++i) {
            newList.add((banjieList).get(currIdx + i));
        }

        flipInfo.setData(addNodeInfoList);
        return flipInfo;
    }


    @Override
    public List<Map<String, Object>> findCtpAffairIdbySummaryid(String id) {
        StringBuffer sql = new StringBuffer();
        sql.append("select * from (select id from CTP_AFFAIR where object_id ='" + id + "' and state=4 ORDER BY complete_time desc) where rownum=1");
        JDBCAgent jdbcAgent = new JDBCAgent(true);
        List<Map<String, Object>> banjie = null;
        try {
            jdbcAgent.execute(sql.toString());
            banjie = jdbcAgent.resultSetToList();
        } catch (Exception e) {
            LOGGER.error("办结栏目获取异常：", e);
        }
        return banjie;
    }

    @Override
    public List<Map<String, Object>> findCoopratiionBanjie(String templetIds) {
        StringBuffer sql = new StringBuffer();
//        sql.append("select DISTINCT id,subject,start_date,current_nodes_info,TEMPLETE_ID,name,START_MEMBER_ID from (");
//        sql.append("select s.id,s.subject,s.start_date,s.current_nodes_info,CA.id aid,s.TEMPLETE_ID,s.name,s.start_member_id  from (");
//        sql.append("select * from (select s.*,m.name from COL_SUMMARY s LEFT JOIN ORG_MEMBER m on s.START_MEMBER_ID=m.ID) sm where SM.current_nodes_info is null  and SM.state =3) s ");
//        sql.append("LEFT  JOIN CTP_AFFAIR ca on s.id=CA.OBJECT_ID) ss where aid is not null");
//        if (templetIds != null && !templetIds.equals("null") && !templetIds.equals("")) {
//            sql.append(" and SS.TEMPLETE_ID='" + templetIds + "'");
//        }
//        sql.append(" order by start_date desc");
        sql.append("select id,subject,START_DATE,FINISH_DATE,TEMPLETE_ID,START_MEMBER_ID,(select name from ORG_MEMBER where id=START_MEMBER_ID) start_name ");
        sql.append("from COL_SUMMARY where START_MEMBER_ID='6124365271652712753'  and finish_date is not null ORDER BY START_DATE desc");
        JDBCAgent jdbcAgent = new JDBCAgent(true);
        List<Map<String, Object>> banjie = null;
        try {
            jdbcAgent.execute(sql.toString());
            banjie = jdbcAgent.resultSetToList();
        } catch (Exception e) {
            LOGGER.error("办结栏目获取异常：", e);
        }
        return banjie;
    }

    @Override
    public FlipInfo findMoreXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map) {
        List<Object> banjieList = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT summary.id,summary.state,summary.COMPLETE_TIME,summary.DOC_MARK,summary.EDOC_TYPE,summary.SEND_UNIT,summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID from EDOC_SUMMARY summary where summary.STATE = 3 ");
        String condition = "";
//标题
        if (map.get("title") != null) {
            condition = (String) map.get("title");
            map.remove("title");
            map.put("title", "%" + condition + "%");
            sql.append(" AND summary.SUBJECT like '" + map.get("title") + "'");
        }
//开始时间
        if (map.get("beginTime") != null) {
            sql.append(" AND summary.CREATE_TIME >=to_date('" + map.get("beginTime") + "','yyyy-mm-dd')");
        }
//截止时间
        if (map.get("endTime") != null) {
            sql.append(" AND summary.CREATE_TIME <=to_date('" + map.get("endTime") + "','yyyy-mm-dd')");
        }
//发起人
        if (map.get("sender") != null) {
            condition = (String) map.get("sender");
            map.remove("sender");
            map.put("sender", "%" + condition + "%");
            sql.append(" AND summary.CREATE_PERSON like '" + map.get("sender") + "'");
        }
//        模板
        if (map.get("templetIds") != null && !"".equals(map.get("templetIds"))) {
            sql.append(" AND summary.TEMPLETE_ID IN (" + map.get("templetIds") + ")");
        }
        sql.append("   ORDER BY summary.CREATE_TIME DESC ");
        try (JDBCAgent jdbcAgent = new JDBCAgent(true)) {
            jdbcAgent.execute(sql.toString());
            banjieList = jdbcAgent.resultSetToList();
        } catch (SQLException e) {
            LOGGER.error("办结栏目更多页加条件查询获取异常", e);
        } catch (BusinessException b) {
            LOGGER.error("办结栏目更多页加条件查询获取异常", b);
        }
        int page = flipInfo.getPage();
        int size = flipInfo.getSize();
        flipInfo.setTotal((banjieList).size());
        List newList = new ArrayList();
        int currIdx = page > 1 ? (page - 1) * size : 0;

        for (int i = 0; i < size && i < (banjieList).size() - currIdx; ++i) {
            newList.add((banjieList).get(currIdx + i));
        }

        flipInfo.setData(newList);
        return flipInfo;
    }

    @Override
    public FlipInfo findMoreXkjtNoBanjie(FlipInfo flipInfo, Map<String, Object> map) {
        List<Map<String, Object>> noBanjielist = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT affair.name,affair.RECEIVE_TIME,summary.id,summary.state,summary.COMPLETE_TIME,summary.DOC_MARK,summary.EDOC_TYPE,summary.SEND_UNIT,trim(summary.SUBJECT) SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID from EDOC_SUMMARY summary,(select m.name,a.OBJECT_ID ,a.state,a.IS_DELETE,a.RECEIVE_TIME from ctp_affair a LEFT JOIN ORG_MEMBER m on a.MEMBER_ID = m.ID ) affair where   summary.ID = affair.OBJECT_ID and summary.STATE = 0 AND affair.state = 3 AND affair.IS_DELETE = 0  ");
        String condition = "";
        if (map.get("templetIds") != null && !"".equals(map.get("templetIds"))) {
            sql.append(" AND summary.TEMPLETE_ID IN (" + map.get("templetIds") + ")");
        }
        //        标题
        if (map.get("title") != null) {
            condition = (String) map.get("title");
            map.remove("title");
            map.put("title", "%" + condition + "%");
            sql.append(" AND summary.SUBJECT like '" + map.get("title") + "'");
        }
        if (map.get("name") != null) {
            sql.append(" AND affair.name like '%" + map.get("name") + "%'");
        }
        if (map.get("beginTime") != null) {
            sql.append(" AND summary.CREATE_TIME >=to_date('" + map.get("beginTime") + "','yyyy-mm-dd')");
        }

        if (map.get("endTime") != null) {
            sql.append(" AND summary.CREATE_TIME <=to_date('" + map.get("endTime") + "','yyyy-mm-dd')");
        }

        if (map.get("dealBeginTime") != null) {
            sql.append(" AND summary.COMPLETE_TIME >=to_date('" + map.get("dealBeginTime") + "','yyyy-mm-dd')");
        }

        if (map.get("dealEndTime") != null) {
            sql.append(" AND summary.COMPLETE_TIME <=to_date('" + map.get("dealEndTime") + "','yyyy-mm-dd')");
        }
//发起人

        if (map.get("sender") != null) {
            condition = (String) map.get("sender");
            map.remove("sender");
            map.put("sender", "%" + condition + "%");
            sql.append(" AND summary.CREATE_PERSON like '" + map.get("sender") + "'");
        }
        sql.append("   ORDER BY summary.CREATE_TIME DESC ");
        try (JDBCAgent jdbcAgent = new JDBCAgent(true)) {
            jdbcAgent.execute(sql.toString());
            noBanjielist = jdbcAgent.resultSetToList();
        } catch (SQLException e) {
            LOGGER.error("未办结栏目更多页加条件查询获取异常", e);
        } catch (BusinessException b) {
            LOGGER.error("未结栏目更多页加条件查询获取异常", b);
        }
        List<Map<String, Object>> depu = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < noBanjielist.size(); i++) {
            String subject = (String) noBanjielist.get(i).get("subject");
            if (set.add(subject)) {
                depu.add(noBanjielist.get(i));
            }
        }
        int page = flipInfo.getPage();
        int size = flipInfo.getSize();
        flipInfo.setTotal((depu).size());
        List<Map<String, Object>> newList = new ArrayList();
        int currIdx = page > 1 ? (page - 1) * size : 0;

        for (int i = 0; i < size && i < (depu).size() - currIdx; ++i) {
            newList.add((depu).get(currIdx + i));
        }

        flipInfo.setData(newList);
        return flipInfo;
    }

    /**
     * 所有未办结
     *
     * @param templetIds
     * @return
     */
    @Override
    public List<Map<String, Object>> findXkjtAllNoBanJie(String templetIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from (");
        sb.append(" SELECT affair.name,summary.id,summary.state,summary.COMPLETE_TIME,summary.DOC_MARK,summary.EDOC_TYPE,summary.SEND_UNIT,summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID from EDOC_SUMMARY summary,(select m.name,a.OBJECT_ID ,a.state,a.IS_DELETE from ctp_affair a LEFT JOIN ORG_MEMBER m on a.MEMBER_ID = m.ID ) affair where   summary.ID = affair.OBJECT_ID and summary.STATE = 0 AND affair.state = 3 AND affair.IS_DELETE = 0  ");
        if (Strings.isNotBlank(templetIds)) {
            sb.append(" AND summary.TEMPLETE_ID IN (" + templetIds + ")");
        }
        sb.append("   ORDER BY summary.CREATE_TIME DESC ");
        sb.append(" ) where rownum<20 ");
        List<Map<String, Object>> banjie = new ArrayList<>();
        JDBCAgent jdbcAgent = new JDBCAgent(true);
        try {
            jdbcAgent.execute(sb.toString());
            banjie = jdbcAgent.resultSetToList();
        } catch (Exception e) {
            LOGGER.error("办结栏目获取异常：", e);
        }
        List<Map<String, Object>> depu = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        for (int i = 0; i < banjie.size(); i++) {
            String subject = (String) banjie.get(i).get("subject");
            if (set.add(subject)) {
                depu.add(banjie.get(i));
            }
        }

        return depu;
    }

    /**
     * 所有办结
     *
     * @param templetIds
     * @return
     */
    @Override
    public List<Object> findXkjtAllBanJie(String templetIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from (");
        sb.append(" SELECT summary.id,summary.state,summary.COMPLETE_TIME,summary.DOC_MARK,summary.EDOC_TYPE,summary.SEND_UNIT,summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID from EDOC_SUMMARY summary where summary.STATE = 3 ");
        if (Strings.isNotBlank(templetIds)) {
            sb.append(" AND summary.TEMPLETE_ID IN (" + templetIds + ")");
        }
        sb.append("   ORDER BY summary.CREATE_TIME DESC ");
        sb.append(" ) where rownum<20 ");
        List<Object> banjie = new ArrayList<>();
        JDBCAgent jdbcAgent = new JDBCAgent(true);
        try {
            jdbcAgent.execute(sb.toString());
            banjie = jdbcAgent.resultSetToList();
        } catch (Exception e) {
            LOGGER.error("办结栏目获取异常：", e);
        }
        return banjie;
    }


}
