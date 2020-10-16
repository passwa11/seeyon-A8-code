package com.seeyon.apps.synorg.manager;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午9:57:52
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncOrgManager {

    /**
     * 根据编码获得职务对象
     * @param entityClassName 实体类名
     * @param property 属性名
     * @param value 属性值
     * @param accountId 单位ID
     * @return 组织对象
     */
    public V3xOrgEntity getEntityByProperty(String entityClassName, String property, Object value, Long accountId);

    /**
     * 手动同步是否正在进行中
     * @return
     */
    public boolean isSyning();

    /**
     * 设置同步状态
     */
    public void setSyning(boolean isSyning);

    /**
     * 同步第三方组织机构数据到A8数据库中
     */
    public void syncThirdOrgDataToSeeyon() throws Exception;

    /**
     * 手动同步操作
     */
    public void synThreadOperation();
}
