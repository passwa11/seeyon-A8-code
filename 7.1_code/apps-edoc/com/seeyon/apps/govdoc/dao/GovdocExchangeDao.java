package com.seeyon.apps.govdoc.dao;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.seeyon.apps.govdoc.constant.GovdocEnum.ExchangeDetailStatus;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetail;
import com.seeyon.apps.govdoc.po.GovdocExchangeDetailLog;
import com.seeyon.apps.govdoc.po.GovdocExchangeMain;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;

public class GovdocExchangeDao {
	
	protected static final Logger LOGGER = Logger.getLogger(GovdocExchangeDao.class);
	
	/**
	 * 通过mainId查询ExchangeMain表数据
	 * @param mainId
	 * @return
	 */
	public GovdocExchangeMain getGovdocExchangeMainById(Long mainId) {
		return DBAgent.get(GovdocExchangeMain.class, mainId);
	}

	/**
	 * 通过summaryId查询ExchangeMain表数据
	 * @param summaryId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GovdocExchangeMain findBySummaryId(Long summaryId, Integer exchangerType) {
		if(exchangerType == null) {
			exchangerType = GovdocExchangeMain.EXCHANGE_TYPE_JIAOHUAN;
		}
		String hql = "from GovdocExchangeMain where summaryId = :summaryId and type = :exchangerType";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("summaryId", summaryId);
		params.put("exchangerType", exchangerType);
		
		List<GovdocExchangeMain> list = DBAgent.find(hql, params);
		if (Strings.isNotEmpty(list)) {
			return list.get(0);
		}
		return null;
	}
	
	public void saveTransferByExchange(Long summaryId, Long memberId, List<Integer> exchangeType) throws BusinessException {
		if(Strings.isNotEmpty(exchangeType)) {
			String hql = "update GovdocExchangeMain set startUserId = :memberId where summaryId = :summaryId and type in (:exchangeType)";
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("summaryId", summaryId);
			params.put("memberId", memberId);
			params.put("exchangeType", exchangeType);
			DBAgent.bulkUpdate(hql, params);
		}
	}
	
	/**
	 * 
	* @Title: findByReferenceIdId
	* @Description: 根据主公文ID获取关联信息
	* @param summaryId 主公文id
	* @param exchangerType 默认为3 转发文
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocExchangeMain> findByReferenceIdId(Long summaryId, Integer exchangerType) {
		if(exchangerType == null) {
			exchangerType = GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN;
		}
		
		String hql = "from GovdocExchangeMain where referenceId=:summaryId and type = :exchangerType";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("summaryId", summaryId);
		params.put("exchangerType", exchangerType);
		
		return DBAgent.find(hql,params);
	}

	/**
	 * 
	* @Title: findRelationBySummaryIdAndReference
	* @Description: 根据主公文ID和关联ID查询是否已存在关联关系
	* @param summaryId
	* @param referenceId
	 */
	@SuppressWarnings("unchecked")
	public int findRelationBySummaryIdAndReference(Long summaryId,Long referenceId) {
		if(summaryId==null || referenceId==null){
			return -1;
		}
		Integer exchangerType = GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN;
		String hql = "from GovdocExchangeMain where type = :exchangerType and referenceId = :referenceId and summaryId=:summaryId";
		Map<String, Object> namedParameterMap = new HashMap<String, Object>();
		namedParameterMap.put("referenceId", referenceId);
		namedParameterMap.put("exchangerType", exchangerType);
		namedParameterMap.put("summaryId", summaryId);
		List<GovdocExchangeMain> resultList = DBAgent.find(hql, namedParameterMap);
		if(resultList!=null && resultList.size()>0){
			return resultList.size();
		}
		return -1;
	}
	
	public FlipInfo findListByPage(FlipInfo flipInfo, Map<String, String> query) {
		Integer exchangerType = GovdocExchangeMain.EXCHANGE_TYPE_ZHUANFAWEN;
		if (Strings.isNotBlank(query.get("type"))) {
			exchangerType = Integer.valueOf(query.get("type"));
		}
		
		String hql = "from GovdocExchangeMain where type = :exchangerType";
		Map<String, Object> namedParameterMap = new HashMap<String, Object>();
		if (Strings.isNotBlank(query.get("referenceId"))) {
			hql += " and referenceId = :referenceId";
			String referenceId = query.get("referenceId");
			namedParameterMap.put("referenceId", Long.valueOf(referenceId));
		}
		namedParameterMap.put("exchangerType", exchangerType);
		
		DBAgent.find(hql, namedParameterMap, flipInfo);
		return flipInfo;
	}
	
	@SuppressWarnings("deprecation")
	public void saveOrUpdateMain(GovdocExchangeMain main) {
		DBAgent.saveOrUpdate(main);
		LOGGER.info("保存main" + new Timestamp(System.currentTimeMillis()));
	}
	
	///////////////////////////////////////////////////////////////////////////
	/**
	 * 通过detailId查询GovdocExchangeDetail表数据
	 * @param detailId
	 * @return
	 */
	public GovdocExchangeDetail getExchangeDetailById(Long detailId) {
		return DBAgent.get(GovdocExchangeDetail.class, detailId);
	}
	
	@SuppressWarnings("unchecked")
	public GovdocExchangeDetail findDetailBySummaryId(Long summaryId) {
		Map<String, Object> params = new HashMap<String, Object>();
		String hql = "from GovdocExchangeDetail where summaryId = :summaryId";
		params.put("summaryId", summaryId);
		List<GovdocExchangeDetail> details =  DBAgent.find(hql,params);
		if (CollectionUtils.isNotEmpty(details)) {
			return details.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public GovdocExchangeDetail findDetailByRecSummaryId(Long recSummaryId) {
		String hql = "from GovdocExchangeDetail where recSummaryId = :recSummaryId";
        Map<String, Object> namedParameterMap = new HashMap<String, Object>();
        namedParameterMap.put("recSummaryId",recSummaryId);
        List<GovdocExchangeDetail> list = DBAgent.find(hql, namedParameterMap);
        if (list != null && list.size() > 0) {
			return list.get(0);
		}
        return null;
	}

	@SuppressWarnings("unchecked")
	public List<GovdocExchangeDetail> getDetailByMainId(Long mainId) {
		String hql = "from GovdocExchangeDetail detail where mainId = :mainId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mainId", mainId);
		return DBAgent.find(hql, params);
	}
	
	@SuppressWarnings("unchecked")
	public List<GovdocExchangeDetail> findGovdocExchangeDetail(FlipInfo flipInfo, Map<String, String> conditionMap) {
		StringBuilder hql = new StringBuilder();
		hql.append("from GovdocExchangeDetail detail where 1=1");
		
		Map<String, Object> params = new HashMap<String, Object>();
		if (conditionMap.get("mainId") != null) {
			hql.append(" and detail.mainId = :mainId ");
			params.put("mainId", Long.valueOf(conditionMap.get("mainId")));
		}
		if (conditionMap.get("status") != null) {
			Integer status = Integer.valueOf(conditionMap.get("status"));
			if (status == 2) {
				hql.append(" and detail.status in (:status) ");
				params.put("status", new Integer[]{2,3,5,15});
			} else {
				hql.append(" and detail.status = :status ");
				params.put("status", status);
			}
		}
		if (conditionMap.containsKey("size")) {
			flipInfo.setSize(Integer.valueOf(conditionMap.get("size")));
		}
		hql.append(" order by detail.createTime desc");
		return DBAgent.find(hql.toString(),params,flipInfo);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findDetailCountBySummaryId(Long mainId) {
		Map<String, Object> params = new HashMap<String, Object>();
		String countSql = "count(detail.id),sum(case when detail.status=2 or detail.status=3 or detail.status=5 or detail.status=15 then 1 else 0 end),sum(case when detail.status=1 then 1 else 0 end),sum(case when detail.status=10 then 1 else 0 end) ";
		StringBuilder hql = new StringBuilder("select "+countSql+" from GovdocExchangeDetail detail where detail.mainId = :mainId");
		params.put("mainId", mainId);
		return (List<Object[]>)DBAgent.find(hql.toString(), params);
	}
	@SuppressWarnings("unchecked")
	public List<GovdocExchangeDetailLog> findGovdocExchangeDetailLog(FlipInfo flipInfo, Map<String, String> conditionMap) {
		String hql = "from GovdocExchangeDetailLog where detailId = :detailId order by time desc, timeMS desc";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("detailId", Long.valueOf(conditionMap.get("detailId")));
		return DBAgent.find(hql, params,flipInfo);
	}
	
	public void updateExchangeDetailState(Long detailId, ExchangeDetailStatus state) {
		String hql = "update GovdocExchangeDetail detail set detail.status = :status where detail.id = :detailId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("status", state.getKey());
		params.put("detailId", detailId);
		DBAgent.bulkUpdate(hql.toString(),params);
	}
	@SuppressWarnings("deprecation")
	public void updateDetail(GovdocExchangeDetail govdocExchangeDetail) {
		DBAgent.saveOrUpdate(govdocExchangeDetail);
	}
	public void saveDetailList(List<GovdocExchangeDetail> details) {
		DBAgent.saveAll(details);
	}	
	public void updateDetailList(List<GovdocExchangeDetail> details) {
		DBAgent.updateAll(details);
	}
	public void saveDetailLog(GovdocExchangeDetailLog detailLog) {
		DBAgent.save(detailLog);
	}
	public void saveDetailLogList(List<GovdocExchangeDetailLog> logs) {
		DBAgent.saveAll(logs);
	}
	
}
