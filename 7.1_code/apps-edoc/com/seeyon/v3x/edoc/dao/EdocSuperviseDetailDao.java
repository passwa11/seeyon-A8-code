package com.seeyon.v3x.edoc.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocSuperviseDetail;
import com.seeyon.v3x.edoc.util.Constants;

public class EdocSuperviseDetailDao extends BaseHibernateDao<EdocSuperviseDetail> {

	public List<EdocSuperviseDetail> findToBeProcessedDetailBySupervisor(Long supervisorId){
		
		String queryString = "from EdocSuperviseDetail as detail where detail.edocSupervisors.supervisorId = ? and detail.status = ?";
		Object[] values = new Object[]{supervisorId, Constants.EDOC_SUPERVISE_PROGRESSING};
		return super.findVarargs(queryString, values);

	} 
	public List<EdocSuperviseDetail> findProcessedDetailBySupervisor(Long supervisorId){
		
		String queryString = "from EdocSuperviseDetail as detail where detail.edocSupervisors.supervisorId = ? and detail.status = ?";
		Object[] values = new Object[]{supervisorId, Constants.EDOC_SUPERVISE_TERMINAL};
		return super.findVarargs(queryString, values);

	} 
	
	/**
	 * 根据公文id查找所有的detail记录,每一条公文只可能对应一条督办记录
	 * @param summaryId
	 * @return
	 */
	public EdocSuperviseDetail findEdocSuperviseDetailBySummaryId(Long summaryId){
		
		String queryString = "from EdocSuperviseDetail as de where de.edocId = ?";
		List <EdocSuperviseDetail> list = super.findVarargs(queryString, summaryId);
		if(null!=list && list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}
	
	public void saveOrUpdateDetail(EdocSuperviseDetail detail){
		super.getHibernateTemplate().saveOrUpdate(detail);
	}
	public List<CtpAffair> getALLAvailabilityAffairList(ApplicationCategoryEnum app,Long objectId, boolean needPagination){
		DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class)
		.add(Expression.eq("objectId", objectId))
		.add(Expression.eq("app", app.getKey())).add(Expression.eq("delete", false))
		.add(Expression.in("state", new Object[]{StateEnum.col_sent.key(),
				StateEnum.col_done.key(), StateEnum.col_pending.key()})) //鏌ユ壘鎵�湁鏈夋晥浜嬮」
				;
		criteria.addOrder(Order.desc("receiveTime"));
		List<CtpAffair> affairList = null;
		if(needPagination){
			affairList = super.executeCriteria(criteria);
		}else{
			affairList = super.executeCriteria(criteria, -1, -1);
		}
		return affairList;
	}
	public List<CtpAffair> getALLAvailabilityAffairList(ApplicationCategoryEnum app,ApplicationSubCategoryEnum subApp,Long objectId, boolean needPagination){
		DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class)
		.add(Expression.eq("objectId", objectId))
		.add(Expression.eq("app", app.getKey()))
		.add(Expression.eq("subApp", subApp.getKey()))
		.add(Expression.eq("delete", false))
		.add(Expression.in("state", new Object[]{StateEnum.col_sent.key(),
				StateEnum.col_done.key(), StateEnum.col_pending.key()})) //鏌ユ壘鎵�湁鏈夋晥浜嬮」
				;
		criteria.addOrder(Order.desc("receiveTime"));
		List<CtpAffair> affairList = null;
		if(needPagination){
			affairList = super.executeCriteria(criteria);
		}else{
			affairList = super.executeCriteria(criteria, -1, -1);
		}
		return affairList;
	}
}
