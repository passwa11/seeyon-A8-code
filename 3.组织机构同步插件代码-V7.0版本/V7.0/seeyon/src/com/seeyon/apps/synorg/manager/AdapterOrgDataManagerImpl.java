/**
 * 
 */
package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seeyon.apps.synorg.dao.HrDepartmentDao;
import com.seeyon.apps.synorg.dao.HrMemberDao;
import com.seeyon.apps.synorg.po.SynDepartment;
import com.seeyon.apps.synorg.po.SynLevel;
import com.seeyon.apps.synorg.po.SynMember;
import com.seeyon.apps.synorg.po.SynPost;
import com.seeyon.apps.synorg.po.SynUnit;
import com.seeyon.apps.synorg.po.hr.HrDepartment;
import com.seeyon.apps.synorg.po.hr.HrMember;
import com.seeyon.apps.synorg.scheduletask.SynOrgTask;

/**
 * @author Yang.Yinghai
 * @date 2016年4月13日下午1:30:38
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public class AdapterOrgDataManagerImpl implements AdapterOrgDataManager {

    private HrDepartmentDao hrDepartmentDao;

    private HrMemberDao hrMemberDao;
    //所有正常的部门
    private static List<HrDepartment> allEnabledDept ;
    
	@Override
	public List<SynUnit> getUnitList() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SynDepartment> getDepartmentList() {
        List<SynDepartment> result = new ArrayList<SynDepartment>();
        List<HrDepartment> list = hrDepartmentDao.findAll();
        System.out.println("数据库查询出来的部门条数：" + list.size());
        if(list != null) {
            allEnabledDept = findAllChilds(SynOrgTask.getRootDeptCode(), list);
            System.out.println("经过有效性过滤之后的部门条数：" + allEnabledDept.size());
            for(HrDepartment dept : allEnabledDept) {
                SynDepartment temp = new SynDepartment();
                temp.setName(dept.getName());
                temp.setCode(dept.getCode());
                temp.setParentCode(dept.getParentCode());
                temp.setSortId(dept.getSortId());
                temp.setCreateDate(new Date());
                temp.setSyncState(0);
                result.add(temp);
            }
        }
        return result;
    }

    // 查找子部门
    private List<HrDepartment> findAllChilds(String parentCode, List<HrDepartment> allDept) {
        List<HrDepartment> list = new ArrayList<HrDepartment>();
        for(HrDepartment dept : allDept) {
            if(dept.getParentCode().equals(parentCode)) {
                list.add(dept);
                list.addAll(findAllChilds(dept.getCode(), allDept));
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SynLevel> getLevelList() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SynPost> getPostList() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SynMember> getMemberList() {
        List<SynMember> result = new ArrayList<SynMember>();
        List<HrMember> list = hrMemberDao.findAll();
        System.out.println("数据库查询出来的人员条数：" + list.size());
        
        if(allEnabledDept!=null){
            for(HrDepartment dept : allEnabledDept){
                for(HrMember member : list) {
                    if(dept.getCode().equals(member.getDepartmentCode())){
                        SynMember temp = new SynMember();
                        temp.setCode(member.getCode());
                        temp.setName(member.getName());
                        temp.setLoginName(member.getLoginName());
                        temp.setEnable(member.getEnable());
                        temp.setDepartmentCode(member.getDepartmentCode());
                        temp.setPostCode(member.getPostName());
                        temp.setLevelCode(member.getLevelCode());
                        temp.setEmail(member.getEmail());
                        temp.setTelNumber(member.getTelNumber());
                        temp.setGender(member.getGender());
                        temp.setBirthday(member.getBirthday());
                        temp.setIdNum(member.getIdNum());
                        temp.setCreateDate(new Date());
                        temp.setSyncState(0);
                        result.add(temp);
                    }
                }
            }
        }
        System.out.println("经过有效性过滤之后的人员条数：" + result.size());
        return result;
    }

    /**
     * @param hrDepartmentDao the hrDepartmentDao to set
     */
    public void setHrDepartmentDao(HrDepartmentDao hrDepartmentDao) {
        this.hrDepartmentDao = hrDepartmentDao;
    }

    /**
     * @param hrMemberDao the hrMemberDao to set
     */
    public void setHrMemberDao(HrMemberDao hrMemberDao) {
        this.hrMemberDao = hrMemberDao;
    }
}
