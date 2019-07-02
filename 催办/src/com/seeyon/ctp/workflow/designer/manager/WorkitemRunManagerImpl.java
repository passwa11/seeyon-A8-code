package com.seeyon.ctp.workflow.designer.manager;

import com.seeyon.ctp.workflow.designer.dao.WorkitemRunDao;
import com.seeyon.ctp.workflow.designer.dao.WorkitemRunDaoImpl;

import java.util.List;
import java.util.Map;

/**
 * 周刘成   2019/7/1
 */
public class WorkitemRunManagerImpl implements WorkitemRunManager {

    private WorkitemRunDao workitemRunDao = new WorkitemRunDaoImpl();

    @Override
    public List<Map<String, Object>> selectProcessInfo(String processid) {
        return workitemRunDao.selectProcessInfo(processid);
    }
}
