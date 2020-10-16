package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
/**
 * Data access object (DAO) for domain model class EdocMarkHistory.
 * @see .EdocMarkHistory
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkHistoryDAO extends BaseHibernateDao<EdocMarkHistory> {

    private static final Log log = LogFactory.getLog(EdocMarkHistoryDAO.class);	

    /**
     * 方法描述：保存公文文号历史
     */
    public void save(EdocMarkHistory edocMarkHistory) {
        log.debug("saving EdocMarkHistory instance");
        super.save(edocMarkHistory);
//        try {
//            getSession().save(edocMarkHistory);
//            log.debug("save successful");
//        } catch (RuntimeException re) {
//            log.error("save failed", re);
//            throw re;
//        }
    }
    
    public void deleteEdocMarkHistoryByEdocId(Long edocId) {
    	String hql="delete from EdocMarkHistory as mark where mark.edocId = :edocId";
    	Map<String,Object> nameParameters=new HashMap<String,Object>();
    	nameParameters.put("edocId", edocId);
    	super.bulkUpdate(hql, nameParameters);
    }
    
    /**
     * 查询相同文号数
     * @param edocMark
     * @param edocId
     * @return
     */
    public int getCount(String edocMark,Long edocId) {
    	StringBuilder hql = new StringBuilder("from EdocMarkHistory where docMark=?");
    	List<Object> values = new ArrayList<Object>();
    	List<Type> typeList = new ArrayList<Type>();
    	values.add(edocMark);
    	typeList.add(Hibernate.STRING);
    	if(edocId != null) {
    		hql.append(" and edocId<>?");
    		values.add(edocId);
    		typeList.add(Hibernate.LONG);
    	}
    	Type[] types = new Type[typeList.size()];
    	int i = 0;
    	for(Type type: typeList) {
    		types[i] = type;
    		i++;
    	}
    	return super.getQueryCount(hql.toString(), values.toArray(), types);
    }
    
    public EdocMarkHistory findEdocMarkHistoryByEdocSummaryId(Long edocSummaryId) {
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	List<EdocMarkHistory> list = super.find("from EdocMarkHistory as mark where mark.edocId = :edocId and govdocType=4 order by mark.docMarkNo desc ", paramMap);
    	if(list!=null && list.size()>0) {
    		return list.get(0);
    	}
    	return null;
    }
    
    public EdocMarkHistory findEdocMarkHistoryByEdocSummaryIdAndEdocMark(Long edocSummaryId,String edocMark,int markNum){
    	if(edocMark!=null) {
    		edocMark=SQLWildcardUtil.escape(edocMark.trim());
    	}
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	paramMap.put("edocMark", edocMark);
    	paramMap.put("markNum", markNum);
    	List<EdocMarkHistory> list = super.find("from EdocMarkHistory as mark where mark.edocId = :edocId and mark.docMark=:edocMark and mark.markNum=:markNum  order by mark.docMarkNo desc ", paramMap);
    	if(list!=null && list.size()>0) {
    		return list.get(0);
    	}
    	return null;
    }
    
    /**
     * @方法描述: 根据公文id删除文号
     * @param summaryId 公文Id
     */
    
    public void deleteMarkIdBySummaryId(Long summaryId){
    	 String hql="delete from EdocMarkHistory where edocId =?";
  	   super.bulkUpdate(hql, null,new Object[]{summaryId});
    }
    
    /**
     * 修改公文历史文号流转状态
     * @param transferStatus
     * @param summaryId
     */
    public void updateMarkHistoryTransferStatus(Integer transferStatus, Long summaryId) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("transferStatus", transferStatus);
		paramMap.put("summaryId", summaryId);
		DBAgent.bulkUpdate("update EdocMarkHistory set transferStatus=:transferStatus where edocId = :summaryId", paramMap);
	}
    
    public int getMaxUsedDocMark(Long definitionId){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("definitionId", definitionId);
    	List<Integer> list = super.find("select max(docMarkNo) as docMarkNo  from EdocMarkHistory as mark where mark.markDefinitionId=:definitionId and mark.docMarkNo is not null", paramMap);
    	if(list!=null && list.size()>0) {
    		return list.get(0);
    	}
    	return 0;
    }
}