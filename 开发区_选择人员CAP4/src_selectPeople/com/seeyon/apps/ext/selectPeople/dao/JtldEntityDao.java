package com.seeyon.apps.ext.selectPeople.dao;


import com.seeyon.apps.ext.selectPeople.po.Formson0174;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public interface JtldEntityDao {

    /**
     * 驻区单位
     *
     * @param name
     * @return
     */
    List<Map<String, Object>> selectZhuQu0032(String name);

    //    镇办
    List<Map<String, Object>> selectZhenBan0031(String name);

    //    机关部门
    List<Map<String, Object>> selectJiGuan0030(String name);

    //党政办人员
    List<Map<String, Object>> selectDangZhengBan0029(String name);


    public List<Map<String, Object>> selectJtldEntity(String name);

    public void insertFormson0174(List<Formson0174> formson0174);
}
