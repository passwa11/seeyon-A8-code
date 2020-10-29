package com.seeyon.apps.ext.selectPeople.manager;

import com.seeyon.apps.ext.selectPeople.po.Formson0174;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/5/7
 */
public interface JtldEntityManager {

    //驻区单位
    List<Map<String, Object>> selectZhuQu0032(String name);

    //镇办
    List<Map<String, Object>> selectZhenBan0031(String name);

    //机关部门
    List<Map<String, Object>> selectJiGuan0030(String name);

    //党政办人员
    List<Map<String, Object>> selectDangZhengBan0029(String name);

    //总工会_人武部
    List<Map<String, Object>> selectGonghui(String name);


    List<Map<String, Object>> selectTwoLevelDept(String name);


    public void insertFormson0174(List<Formson0174> formson0174);


}
