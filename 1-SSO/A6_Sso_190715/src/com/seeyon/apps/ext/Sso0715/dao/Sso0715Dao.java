package com.seeyon.apps.ext.Sso0715.dao;

import com.seeyon.apps.ext.Sso0715.pojo.Ssoentity;

import java.util.List;
import java.util.Map;

public interface Sso0715Dao {

    List<Map<String, Object>> selectAccountOA();

    List<Map<String, Object>> selectThirdAccount();

    void insertSso(List<Ssoentity> list);
}