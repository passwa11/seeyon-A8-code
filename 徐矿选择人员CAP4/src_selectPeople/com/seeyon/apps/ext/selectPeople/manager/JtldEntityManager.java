package com.seeyon.apps.ext.selectPeople.manager;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.selectPeople.po.Formson0174;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public interface JtldEntityManager {

    public List<Map<String,Object>> selectJtldEntity(String name);

    public List<Map<String,Object>> selectFormmain0148(String name);

    public List<Map<String,Object>> selectFormmain0106(String name);

    public List<Map<String,Object>> selectFormmain0087(String name);

    public List<Map<String,Object>> selectDeskWork(String name);
    public List<Map<String,Object>> selectPeopleByDeskWorkId(List<String> id);

    public void insertFormson0174(List<Formson0174> formson0174);


}
