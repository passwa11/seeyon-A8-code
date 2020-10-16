package com.seeyon.apps.ext.selectPeople.dao;


import com.seeyon.apps.ext.selectPeople.po.Formson0174;
import com.seeyon.apps.ext.selectPeople.po.JtldEntity;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public interface JtldEntityDao {

    public List<Map<String,Object>> selectJtldEntity(String name);

    public List<Map<String,Object>> selectFormmain0380(String name);

    public List<Map<String,Object>> selectFormmain0148(String name);

    public List<Map<String,Object>> selectFormmain0106(String name);

    public List<Map<String,Object>> selectFormmain0087(String name);

    public List<Map<String,Object>> selectFormmain0323(String name);

    public List<Map<String,Object>> selectDeskWork(String name);
    public List<Map<String,Object>> selectPeopleByDeskWorkId(List<String> id);

    public void insertFormson0174(List<Formson0174> formson0174);
}
