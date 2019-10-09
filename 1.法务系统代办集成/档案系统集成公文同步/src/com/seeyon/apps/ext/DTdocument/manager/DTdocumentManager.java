package com.seeyon.apps.ext.DTdocument.manager;

import com.seeyon.apps.ext.Portal190724.po.UserPas;

public interface DTdocumentManager {
    UserPas selectDocAccount(String s);

    void setDocAccount(UserPas userPas) throws Exception;// 用户设置档案系统user\pas

    int updateDocState(String columnName, String id);
}
