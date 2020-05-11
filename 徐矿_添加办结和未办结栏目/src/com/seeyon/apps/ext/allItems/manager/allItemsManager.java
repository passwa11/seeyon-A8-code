package com.seeyon.apps.ext.allItems.manager;

import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

public interface allItemsManager {
    List<Object> findXkjtAllBanJie(String templetIds);

    List<Object> findXkjtAllNoBanJie(String templetIds);

    /**
     * 所有办结更多页面
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
