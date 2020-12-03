package com.seeyon.cap4.form.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

import java.util.Map;


public interface CreditqueryManager {


    FlipInfo creditqueryPageing(FlipInfo fi, Map<String, Object> params) throws BusinessException;

}
