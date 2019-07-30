package com.seeyon.apps.synorg.dao;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.po.OrgLevel;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.organization.po.OrgPost;
import com.seeyon.ctp.organization.po.OrgUnit;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午9:52:39
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface SyncOrgDao {

    /**
     * 根据编码获得职务对象
     * @param accountId 单位ID
     * @param property 属性名
     * @param feildvalue 属性值
     * @return 对象
     */
    public OrgLevel getLevelByCode(Long accountId, String property, Object feildvalue);

    /**
     * 根据编码获得人员对象
     * @param accountId 单位ID
     * @param property 属性名
     * @param feildvalue 属性值
     * @return 对象
     */
    public OrgMember getMemberByCode(Long accountId, String property, Object feildvalue);

    /**
     * 根据编码获得岗位对象
     * @param accountId 单位ID
     * @param property 属性名
     * @param feildvalue 属性值
     * @return 对象
     */
    public OrgPost getPostByCode(Long accountId, String property, Object feildvalue);

    /**
     * 根据编码获得部门对象
     * @param accountId 单位ID
     * @param property 属性名
     * @param feildvalue 属性值
     * @return 对象
     */
    public OrgUnit getDeptByCode(Long accountId, String property, Object feildvalue);
    
    /**
     * 根据编码获得部门或则单位对象
     * @param accountId 单位ID
     * @param property 属性名
     * @param feildvalue 属性值
     * @return 对象
     */
    public V3xOrgEntity getDepOrUnittByCode(Long accountId, String property, Object feildvalue);
    
    /**
     * 根据编码获得单位对象
     * @param accountId 单位ID
     * @param property 属性名
     * @param feildvalue 属性值
     * @return 对象
     */
    public OrgUnit getUnitByCode(Long accountId, String property, Object feildvalue);
}
