package com.seeyon.v3x.edoc.dao;

import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.RegisterBody;

public class RegisterBodyDao extends BaseHibernateDao<RegisterBody>   {

	/**
	 * 
	 * @param registerId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<RegisterBody> findRegisterBodyByRegisterId(long registerId) {
		String hsql = "from RegisterBody where edocRegister.id=?";
		return super.findVarargs(hsql, registerId);
	}
	
	/**
	 * 删除登记（电子版假删，手工草稿真删）
	 * @param ids
	 */	
	public void deleteRegisterBody(RegisterBody registerBody) {
		 super.delete(registerBody);
	}
	
	
}
