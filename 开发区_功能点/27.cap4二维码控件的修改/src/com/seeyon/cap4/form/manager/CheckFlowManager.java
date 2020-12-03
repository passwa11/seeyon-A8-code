package com.seeyon.cap4.form.manager;

/**
 * @description 检查流程管理器
 * </br>
 * @create by fuqiang
 * @create at 2019-06-22 12:06
 * @see com.seeyon.cap4.form.manager
 * @since v7.1sp
 */
public interface CheckFlowManager {



    /**
     * 保存流程前，判断流程中所对应的盖章和签字节点是否有相对应的权限；
     * @param formAppId     表单AppId
     * @param processXml    流程xml
     * @param processId     流程id
     * @return
     */
    String onSaveWorkFlow(String formAppId, String processXml, String processId);
}
