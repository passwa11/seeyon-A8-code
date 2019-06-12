package com.seeyon.apps.ext.xkEdoc.dao;

import com.seeyon.apps.ext.xkEdoc.util.JDBCUtil;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/6/7
 */
public class XkjtSummaryAttDaoImpl implements XkjtSummaryAttDao {
    @Override
    public List<Map> queryHostFile(String summaryId) {
        String sql = "select t.summary_id,t.attachment_id,t.value from xkjt_summary_attachment t where t.summary_id = '" + summaryId + "'";
        List<Map> list = JDBCUtil.doQuery(sql);
        return list;
    }
}
