package com.seeyon.apps.testBPM.manager;

import java.util.Map;

import com.seeyon.ctp.util.annotation.AjaxAccess;

/**
 * @author Fangaowei
 * <pre>
 * 表单js自定义
 * </pre>
 * @date 2018年8月27日 下午3:34:16
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface FormJsManager {
    
    @AjaxAccess
    public Map<String, Object> helloWorld() throws Exception;
    
}
