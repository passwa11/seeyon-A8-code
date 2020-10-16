package com.seeyon.ctp.workflow.designer.manager;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/7/1
 */
public interface WorkitemRunManager {
    List<Map<String, Object>> selectProcessInfo(String processid);
}
