package com.seeyon.apps.ext.Sso0715.manager;

import java.util.List;
import java.util.Map;

public interface Sso0715Manager {
    List<Map<String, Object>> selectAccountOA();

    List<Map<String, Object>> selectThirdAccount();
}