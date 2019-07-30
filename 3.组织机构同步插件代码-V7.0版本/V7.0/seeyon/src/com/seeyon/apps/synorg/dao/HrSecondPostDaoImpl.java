package com.seeyon.apps.synorg.dao;

import java.util.List;

import com.seeyon.apps.synorg.po.hr.HrSecondPost;
import com.seeyon.ctp.common.dao.BaseHibernateDao;

/**
 * 部门管理实现类
 * 
 * @author Yang.Yinghai
 * @date 2015-8-18下午4:24:41
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public class HrSecondPostDaoImpl extends BaseHibernateDao<HrSecondPost> implements HrSecondPostDao {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HrSecondPost> findAll() {
		return super.getAll();
	}
}
