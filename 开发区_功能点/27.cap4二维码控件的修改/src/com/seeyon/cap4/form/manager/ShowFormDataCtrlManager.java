package com.seeyon.cap4.form.manager;

import java.util.Map;

/**
 * 查看控件manager
 * @author wanxiang
 * @since V7.1
 */
public interface ShowFormDataCtrlManager {

    /**
     * 获取查询记录
     */
    Map<String, Object> getShowRecord(Map<String, Object> params);
}