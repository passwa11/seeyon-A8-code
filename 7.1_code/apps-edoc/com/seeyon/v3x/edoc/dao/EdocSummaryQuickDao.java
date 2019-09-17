package com.seeyon.v3x.edoc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocSummaryQuick;


public class EdocSummaryQuickDao extends BaseHibernateDao<EdocSummaryQuick> {
	 /**
	  * 保存类表
	  */
	public void save(EdocSummaryQuick edocSummaryQuick){
		super.getHibernateTemplate().save(edocSummaryQuick);
	}
	
	
	public void deleteEdocSummaryQuickBySummaryId(Long summaryId){
		String hql = "delete from EdocSummaryQuick as obj where obj.summaryId=? ";
		super.bulkUpdate(hql, null, summaryId);
	}
	
	public EdocSummaryQuick findBySummaryId(Long summaryId){
        String hql = "from EdocSummaryQuick where summaryId =:summaryId";
        Map parameter = new HashMap();
        parameter.put("summaryId", summaryId);
        List<EdocSummaryQuick> list = super.find(hql,-1,-1, parameter);
        return list.get(0);
	}
}





