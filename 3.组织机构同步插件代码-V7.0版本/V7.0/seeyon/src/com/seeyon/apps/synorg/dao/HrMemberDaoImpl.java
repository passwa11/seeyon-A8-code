package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.hr.HrMember;
import com.seeyon.ctp.common.dao.BaseHibernateDao;

/**
 * 人员管理实现类
 * 
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public class HrMemberDaoImpl extends BaseHibernateDao<HrMember> implements HrMemberDao {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HrMember> findAll() {
		return super.getAll();
	}

}
