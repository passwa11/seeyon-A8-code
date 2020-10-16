package com.seeyon.apps.synorg.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.po.SynLog;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

/**
 * 同步插件日志管理
 * @author Yang.Yinghai
 * @date 2015-8-18下午9:57:52
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncLogManager {

    /**
     * 创建日志数据
     * @param Log 日志列表
     */
    public void createAll(List<SynLog> Log);

    /**
     * 按条件查找日志列表
     * @param flipInfo 查询对象
     * @param query 查询条件信息
     * @return 查询结果
     * @throws BusinessException
     */
    public FlipInfo getSynLogList(FlipInfo flipInfo, Map<String, String> query) throws BusinessException;

    /**
     * 根据纪录ID删除日志
     * @param ids ids
     */
    public void deleteSyncLogByIds(String ids);

    /**
     * 清空日志数据
     */
    public void deleteAll();
}
