package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.hr.HrMember;

/**
 * 人员管理
 * 
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:23:17
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public interface HrMemberDao {

	/**
	 * 查找全部人员
	 */
	public List<HrMember> findAll();

}
