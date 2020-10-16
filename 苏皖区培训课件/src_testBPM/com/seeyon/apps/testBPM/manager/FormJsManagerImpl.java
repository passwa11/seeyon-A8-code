package com.seeyon.apps.testBPM.manager;

import java.util.HashMap;
import java.util.Map;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.annotation.AjaxAccess;

/**
 * @author Fangaowei
 * <pre>
 * 
 * </pre>
 * @date 2018年8月27日 下午3:34:49
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class FormJsManagerImpl implements FormJsManager{

    @AjaxAccess
    @Override
    public Map<String, Object> helloWorld() throws Exception {
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("code", 0);
        res.put("name", "Hello World! I'm " + AppContext.currentUserName() + "!");
        return res;
    }
}
