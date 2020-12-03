package com.seeyon.cap4.form.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * Created by weijh on 2018-12-29.
 */
public interface NewFormDataCtrlManager {
    List<Long> getCurrentAccountFormIds();

    /**
     * 表单内 自定义控件 新建
     */
    Map<String, Object> dealMappingData(Map<String, Object> params);
}
