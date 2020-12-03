package com.seeyon.cap4.form.manager;

import java.util.Map;

/**
 * Created by xiaox on 2019/1/15.
 */
public interface ShowFormDataBtnManager {
    /**
     * 获取查询记录
     */
    Map<String, Object> getShowRecord(Map<String, Object> params);
}