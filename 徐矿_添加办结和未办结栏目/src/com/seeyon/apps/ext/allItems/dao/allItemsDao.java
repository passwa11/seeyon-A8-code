package com.seeyon.apps.ext.allItems.dao;

import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

public interface allItemsDao {

    /**
     * 所有办结
     */
    List<Object> findXkjtAllBanJie(String templetIds);

    /**
     * 所有未办结
     *
     * @param templetIds
     * @return
     */
    List<Map<String,Object>> findXkjtAllNoBanJie(String templetIds);

    /**
     * 所有办结更多页面
     *
     * @param flipInfo
     * @param map
     * @return
     */
    FlipInfo findMoreXkjtBanjie(FlipInfo flipInfo, Map<String, Object> map);

    /**
     * 所有未办结更多页面
     * @param flipInfo
     * @param map
     * @return
     */
    FlipInfo findMoreXkjtNoBanjie(FlipInfo flipInfo, Map<String, Object> map);
}
