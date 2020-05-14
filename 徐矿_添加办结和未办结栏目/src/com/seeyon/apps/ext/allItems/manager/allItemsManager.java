package com.seeyon.apps.ext.allItems.manager;

import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

public interface allItemsManager {

    /**
     * 协同所有未办结
     */
    List<Map<String,Object>> findCooprationNobanjie(String templetids);

    FlipInfo findMoreCooprationNobj(FlipInfo flipInfo, Map<String, Object> map);

    /**
     * 协同所有办结
     * @param id
     * @return
     */
    List<Map<String,Object>> findCtpAffairIdbySummaryid(String id);

    List<Map<String,Object>> findCoopratiionBanjie(String templetIds);

    List<Object> findXkjtAllBanJie(String templetIds);

    List<Map<String, Object>> findXkjtAllNoBanJie(String templetIds);

    /**
     * 所有办结更多页面
     *
     * @param flipInfo
     * @param map
     * @return
     */
    FlipInfo findMoreXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map);
    FlipInfo findMoreCooprationXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map);


    /**
     * 所有未办结更多页面
     *
     * @param flipInfo
     * @param map
     * @return
     */
    FlipInfo findMoreXkjtNoBanjie(FlipInfo flipInfo, Map<String, Object> map);
}
