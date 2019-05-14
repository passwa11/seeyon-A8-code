package com.seeyon.apps.ext.selectPeople.manager;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.apps.ext.selectPeople.po.Formson0174;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public interface JtldEntityManager {

    public List<Map> selectJtldEntity();

    public List<Map> selectFormmain0148();

    public List<Map> selectFormmain0106();

    public List<Map> selectFormmain0087();


    public void insertFormson0174(List<Formson0174> formson0174);


}
