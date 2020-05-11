package com.seeyon.apps.ext.allItems.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import org.apache.geode.internal.concurrent.LI;
import org.mvel2.util.Make;

public class allItemsDaoImpl implements allItemsDao {
    private static final Log LOGGER = LogFactory.getLog(allItemsDaoImpl.class);

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
        if (map.get("templetIds") != null) {
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
        List<Object> noBanjielist = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT affair.name,affair.RECEIVE_TIME,summary.id,summary.state,summary.COMPLETE_TIME,summary.DOC_MARK,summary.EDOC_TYPE,summary.SEND_UNIT,summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID from EDOC_SUMMARY summary,(select m.name,a.OBJECT_ID ,a.state,a.IS_DELETE,a.RECEIVE_TIME from ctp_affair a LEFT JOIN ORG_MEMBER m on a.MEMBER_ID = m.ID ) affair where   summary.ID = affair.OBJECT_ID and summary.STATE = 0 AND affair.state = 3 AND affair.IS_DELETE = 0  ");
        String condition = "";
        if (map.get("templetIds") != null) {
            sql.append(" AND summary.TEMPLETE_ID IN (" + map.get("templetIds") + ")");
        }
        //        标题
        if (map.get("title") != null) {
            condition = (String) map.get("title");
            map.remove("title");
            map.put("title", "%" + condition + "%");
            sql.append(" AND summary.SUBJECT like '" + map.get("title") + "'");
        }
        if(map.get("name")!=null){
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
        int page = flipInfo.getPage();
        int size = flipInfo.getSize();
        flipInfo.setTotal((noBanjielist).size());
        List newList = new ArrayList();
        int currIdx = page > 1 ? (page - 1) * size : 0;

        for (int i = 0; i < size && i < (noBanjielist).size() - currIdx; ++i) {
            newList.add((noBanjielist).get(currIdx + i));
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
    public List<Object> findXkjtAllNoBanJie(String templetIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from (");
        sb.append(" SELECT affair.name,summary.id,summary.state,summary.COMPLETE_TIME,summary.DOC_MARK,summary.EDOC_TYPE,summary.SEND_UNIT,summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID from EDOC_SUMMARY summary,(select m.name,a.OBJECT_ID ,a.state,a.IS_DELETE from ctp_affair a LEFT JOIN ORG_MEMBER m on a.MEMBER_ID = m.ID ) affair where   summary.ID = affair.OBJECT_ID and summary.STATE = 0 AND affair.state = 3 AND affair.IS_DELETE = 0  ");
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
