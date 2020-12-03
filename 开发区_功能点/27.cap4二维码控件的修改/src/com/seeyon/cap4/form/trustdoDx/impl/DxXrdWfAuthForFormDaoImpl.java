package com.seeyon.cap4.form.trustdoDx.impl;

import com.seeyon.cap4.form.trustdoDx.DxXrdWfAuthForFormDao;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.JDBCAgent;
import org.apache.commons.logging.Log;

import java.util.List;

/**
 * @description none
 * </br>
 * @create by fuqiang
 * @create at 2019-06-24 11:12
 * @see com.seeyon.cap4.form.trustdoDx.impl
 * @since v7.1sp
 */
public class DxXrdWfAuthForFormDaoImpl implements DxXrdWfAuthForFormDao {

    private Log log = CtpLogFactory.getLog(DxXrdWfAuthForFormDaoImpl.class);

    @Override
    public int saveWfAuth(List<Object> paramList) {
        JDBCAgent jdbcAgent = new JDBCAgent();
        try {
            String insertSql = "INSERT INTO dx_xrd_wf_auth(id, formAppId, processId, nodeId, ctrlId, signLayout ,orderno) VALUES (?, ?, ?, ?, ?, ?, ?)";
            return jdbcAgent.execute(insertSql, paramList);
        } catch (Exception e) {
            log.error("存储流程权限数据异常，请检查：", e);
            return -1;
        } finally {
            jdbcAgent.close();
        }
    }

    @Override
    public int deleteWfAuth(List<Object> paramList) {
        JDBCAgent jdbcAgent = new JDBCAgent();
        String deleteSql = "delete from dx_xrd_wf_auth where formAppId = ? and processId = ?";
        try {
            return jdbcAgent.execute(deleteSql, paramList);
        } catch (Exception e) {
            log.error("删除流程权限数据异常，请检查：", e);
            return -1;
        } finally {
            jdbcAgent.close();
        }

    }
}
