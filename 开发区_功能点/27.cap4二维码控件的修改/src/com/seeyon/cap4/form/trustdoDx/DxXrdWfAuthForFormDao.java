package com.seeyon.cap4.form.trustdoDx;

import java.util.List;
import java.util.Map;

/**
 * @description 表单-流程节点权限dao
 * </br>
 * @create by fuqiang
 * @create at 2019-06-24 10:51
 * @see com.seeyon.cap4.form.trustdoDx
 * @since v7.1sp
 */
public interface DxXrdWfAuthForFormDao {


    /**
     * 保存流程权限对应关系
     * @param paramList
     * @return
     */
    int saveWfAuth(List<Object> paramList);

    /**
     * 删除流程权限对应关系
     * @param paramList
     * @return
     */
    int deleteWfAuth(List<Object> paramList);
}
