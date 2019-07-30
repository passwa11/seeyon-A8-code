package com.seeyon.apps.synorg.manager;

import java.util.List;

import com.seeyon.apps.synorg.po.SynDepartment;
import com.seeyon.apps.synorg.po.SynLevel;
import com.seeyon.apps.synorg.po.SynMember;
import com.seeyon.apps.synorg.po.SynPost;
import com.seeyon.apps.synorg.po.SynUnit;

/**
 * 获取第三方组织机构数据接口
 * @author Yang.Yinghai
 * @date 2015-11-24下午3:47:39
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public interface AdapterOrgDataManager {
    /**
     * 获取单位列表数据
     * @return 职务列表
     */
    public List<SynUnit> getUnitList();

    /**
     * 获取职务列表数据
     * @return 职务列表
     */
    public List<SynDepartment> getDepartmentList();

    /**
     * 获取部门列表数据
     * @return 部门列表
     */
    public List<SynLevel> getLevelList();

    /**
     * 获取岗位列表数据
     * @return 岗位列表
     */
    public List<SynPost> getPostList();

    /**
     * 获取人员列表数据
     * @return 人员列表
     */
    public List<SynMember> getMemberList();
}
