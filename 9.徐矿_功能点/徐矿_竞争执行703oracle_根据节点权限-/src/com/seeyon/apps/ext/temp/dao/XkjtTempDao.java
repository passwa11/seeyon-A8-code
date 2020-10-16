package com.seeyon.apps.ext.temp.dao;

import com.seeyon.apps.ext.temp.po.XkjtTemp;

import java.util.List;
import java.util.Map;

public interface XkjtTempDao {

    void saveXkjtTemp(XkjtTemp tm);

    List<XkjtTemp> findXkjtTemp(Map<String, Object> map);

    void deleteXkjtTemp(List<XkjtTemp> list);
}
