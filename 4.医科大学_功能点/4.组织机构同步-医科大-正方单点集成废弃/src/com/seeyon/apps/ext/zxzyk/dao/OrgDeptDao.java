package com.seeyon.apps.ext.zxzyk.dao;

import com.seeyon.apps.ext.zxzyk.po.OrgDept;

import java.util.List;

/**
 * Created by Administrator on 2019-7-29.
 */
public interface OrgDeptDao {


    List<OrgDept> queryByFirstDept();
    /**
     * 获取第三方系统其他部门
     */
    List<OrgDept> queryByOtherDept(String accountId);

//    void insertOrgDept(List<OrgDept> list);

    void insertOrgDept_new(List<OrgDept> list);

    void insertFirstDept(List<OrgDept> list);

    void deleteDept();

    void updateOrgDept();

}
