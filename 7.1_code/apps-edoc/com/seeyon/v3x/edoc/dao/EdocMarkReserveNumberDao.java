package com.seeyon.v3x.edoc.dao;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;

public class EdocMarkReserveNumberDao extends BaseHibernateDao<EdocMarkReserveNumber> {

	public void deleteByReservedId(List<Long> delReservedIdList) throws BusinessException {
		if(Strings.isNotEmpty(delReservedIdList)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("delReservedIdList", delReservedIdList);
			super.bulkUpdate("delete from EdocMarkReserveNumber where reserveId in (:delReservedIdList)", paramMap);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocMarkReserveNumber> findAll() throws BusinessException {
		Calendar cal = Calendar.getInstance();
		Integer yearNo = cal.get(Calendar.YEAR);
		List<EdocMarkReserveNumber> list = super.findVarargs("from EdocMarkReserveNumber where yearNo = ?", yearNo);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocMarkReserveNumber> findAllNotUsed(EdocMarkDefinition markDef) throws BusinessException {
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("defID", markDef.getId());
		List<EdocMarkReserveNumber> list = super.find("from EdocMarkReserveNumber as reserveNumber where reserveNumber.isUsed != 1 and reserveNumber.markDefineId =:defID ",-1,-1,paraMap);
		return list;
	}
	
	public void updateStatus(String[] memo) {
		String hql = "update from EdocMarkReserveNumber as reserveNumber set reserveNumber.isUsed = 1 where reserveNumber.docMark=:docMark and reserveNumber.markDefineId = :defID order by reserveNumber.markNo";
		Map<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("defID", Long.valueOf(memo[0]));
		paraMap.put("docMark", SQLWildcardUtil.escape(memo[1]));
		super.bulkUpdate(hql, paraMap);
	}
	
	public void updateMarkReserveIsUsedNew(List<Long> markDefIdList, List<String> markstrList, boolean isUsed) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("markDefineId", markDefIdList);
		paramMap.put("docMark", markstrList);
		paramMap.put("isUsed", isUsed);
    	DBAgent.bulkUpdate("update EdocMarkReserveNumber set isUsed = :isUsed where markDefineId in (:markDefineId) and docMark in (:docMark)", paramMap);
	}
	
}
