package com.seeyon.v3x.edoc.dao;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;

public class EdocMarkReserveDao extends BaseHibernateDao<EdocMarkReserve> {

	public List<EdocMarkReserve> findAll() throws BusinessException {
		Calendar cal = Calendar.getInstance();
		Integer yearNo = cal.get(Calendar.YEAR);
		List<EdocMarkReserve> list = super.findVarargs("from EdocMarkReserve where yearNo = ? order by createTime desc", yearNo);
		return list;
	}
	
	public List<EdocMarkReserve> findList(Long markDefineId) throws BusinessException {
		List<EdocMarkReserve> list = super.findVarargs("from EdocMarkReserve where markDefineId=?", markDefineId);
		return list;
	}
	
	public List<EdocMarkReserve> findList(Integer type, Long markDefineId) throws BusinessException {
		List<EdocMarkReserve> list = super.findVarargs("from EdocMarkReserve where type=? and markDefineId=?", type, markDefineId);
		return list;
	}
	
	public void delete(List<Long> delReservedIdList) throws BusinessException {
		if(Strings.isNotEmpty(delReservedIdList)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("delReservedIdList", delReservedIdList);
			super.bulkUpdate("delete from EdocMarkReserve where id in (:delReservedIdList)", paramMap);
		}
	}
	
}
