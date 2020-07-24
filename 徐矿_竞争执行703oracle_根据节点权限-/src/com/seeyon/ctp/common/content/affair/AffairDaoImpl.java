/**
 * $Author: wangwy $
 * $Rev: 17049 $
 * $Date:: 2015-04-27 09:36:00#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.content.affair;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.CTPHibernateDaoSupport;

import com.seeyon.apps.agent.bo.AgentDetailModel;
import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;

/**
 * <p>
 * Title: T1开发框架
 * </p>
 * <p>
 * Description: 内容组件封装Affair数据处理接口实现
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 * 
 * @since CTP2.0
 */
public class AffairDaoImpl  extends BaseHibernateDao<CtpAffair> implements AffairDao {
	private static final Log LOGGER = LogFactory.getLog(AffairDaoImpl.class);
	@Override
	public void save(CtpAffair affair) throws BusinessException {
		DBAgent.save(affair);
	}

	public void saveAffairs(List<CtpAffair> affairs) throws BusinessException {
		DBAgent.saveAll(affairs);
	}

	public CtpAffair get(Long id){
	   return DBAgent.get(CtpAffair.class,id);
	}
	  
	public CtpAffair getByHis(Long id) throws BusinessException{
	  return super.get(id);
	}
	

	@Override
	public void update(CtpAffair affair) throws BusinessException {
		DBAgent.update(affair);
	}
	@Override
    public void updateAffairs(List<CtpAffair> affairs) throws BusinessException {
        DBAgent.updateAll(affairs);
    }

	@Override
	public CtpAffair getByObjectId(ApplicationCategoryEnum app, Long objectId)
			throws BusinessException {
		DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class)
				.add(Restrictions.eq("objectId", objectId));
		return (CtpAffair) DBAgent.findByCriteria(criteria);
	}

	public CtpAffair getSenderAffair(Long objectId) throws BusinessException {
		DetachedCriteria criteria = DetachedCriteria
				.forClass(CtpAffair.class)
				.add(Restrictions.eq("objectId", objectId))
				.add(Restrictions.in("state",
						new Object[] { StateEnum.col_sent.key(),
								StateEnum.col_waitSend.key() }));
		List<CtpAffair> list = super.executeCriteria(criteria, -1, -1);
		CtpAffair senderAffair = null;
		if (Strings.isNotEmpty(list)) {
		    for(CtpAffair a: list){
		    	//如果有多条数据，并且存在垃圾数据取正常的那条数据
		    	if(!a.isDelete()){
		    		senderAffair = a;
		    		break;
		    	}
		    }
		    if(senderAffair == null){
		    	senderAffair = list.get(0);
		    }
		}
		return senderAffair;
	}

	 public CtpAffair getSenderAffairHis(Long objectId) throws BusinessException {
	    DetachedCriteria criteria = DetachedCriteria
	        .forClass(CtpAffair.class)
	        .add(Restrictions.eq("objectId", objectId))
	        .add(Restrictions.in("state",
	            new Object[] { StateEnum.col_sent.key(),
	                StateEnum.col_waitSend.key() }));
	    List<CtpAffair> list = super.executeCriteria(criteria, -1, -1);
	    CtpAffair senderAffair = null;
	    if (Strings.isNotEmpty(list)) {
	        for(CtpAffair a: list){
	          //如果有多条数据，并且存在垃圾数据取正常的那条数据
	          if(!a.isDelete()){
	            senderAffair = a;
	            break;
	          }
	        }
	        if(senderAffair == null){
	          senderAffair = list.get(0);
	        }
	    }
	    return senderAffair;
	  }
	
	 /**
	 * IDX_REF_A_O(ObjectId)
	 * IDX_AFFAIR_APP(APP)
	 */
	public void deleteByAppAndObjectId(ApplicationCategoryEnum appEnum,
			Long objectId) throws BusinessException {
		String hql = "update CtpAffair as affair set affair.delete=:isDelete where affair.objectId = :objectId and affair.app=:app ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("app", appEnum.key());
		map.put("objectId", objectId);
		map.put("isDelete", true);
		DBAgent.bulkUpdate(hql, map);
	}

	/**
     * IDX_REF_A_O  (ObjectId)
     * NO  (MemberId)
     */
	@Override
	public void deleteByObjectIdAndMemberId(Long objectId, Long memberId)
			throws BusinessException {
		String hql = "update CtpAffair as affair set affair.delete=:isDelete where affair.objectId=:objectId and affair.memberId = :memberId ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("memberId", memberId);
		map.put("objectId", objectId);
		map.put("isDelete", true);
		DBAgent.bulkUpdate(hql, map);
	}
	
	/**
     * IDX_REF_A_O  (ObjectId)
     * IDX_AFFAIR_STATE  (State)
     * IDX_AFFAIR_APP   (APP)
     */
	@Override
	public List<Long> getMemberIdListByAppAndObjectId(ApplicationCategoryEnum app, Long id) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<Integer> states = new ArrayList<Integer>();
        states.add(StateEnum.col_pending.key());
        states.add(StateEnum.col_done.key());
        states.add(StateEnum.col_sent.key());
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("select a.memberId from CtpAffair as a where a.objectId=:objectId and a.state in(:state)");
    	
        if(ApplicationCategoryEnum.edoc.equals(app)){
        	
			List<Integer> apps = new ArrayList<Integer>();
			apps.add(ApplicationCategoryEnum.edocSend.key());
			apps.add(ApplicationCategoryEnum.edocRec.key());
			apps.add(ApplicationCategoryEnum.edocSign.key());
			
			sb.append(" and app in (:apps) " );
			map.put("apps", apps);
		} else if(ApplicationCategoryEnum.meeting.equals(app)) {
			states.add(StateEnum.mt_attend.key());
			states.add(StateEnum.mt_unAttend.key());
		} else {
			sb.append(" and a.app = :app ");
			map.put("app", app.key());
		}
        
        map.put("objectId", id);
        map.put("state", states);
        
        return DBAgent.find(sb.toString(),map);
    }
	
	
	/**
     * IDX_REF_A_O  (ObjectId)
     * IDX_AFFAIR_STATE  (State)
     * IDX_AFFAIR_APP   (APP)
     */
	
	@Override
	public List<Long> getMemberIdListByAppAndObjectIdHis(ApplicationCategoryEnum app, Long id) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<Integer> states = new ArrayList<Integer>();
        states.add(StateEnum.col_pending.key());
        states.add(StateEnum.col_done.key());
        states.add(StateEnum.col_sent.key());
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("select a.memberId from CtpAffair as a where a.objectId=:objectId and a.state in(:state)");
    	
        if(ApplicationCategoryEnum.edoc.equals(app)){
        	
			List<Integer> apps = new ArrayList<Integer>();
			apps.add(ApplicationCategoryEnum.edocSend.key());
			apps.add(ApplicationCategoryEnum.edocRec.key());
			apps.add(ApplicationCategoryEnum.edocSign.key());
			
			sb.append(" and app in (:apps) " );
			map.put("apps", apps);
			
		} else if(ApplicationCategoryEnum.meeting.equals(app)) {
			states.add(StateEnum.mt_attend.key());
			states.add(StateEnum.mt_unAttend.key());
		} else {
			sb.append(" and a.app = :app ");
			map.put("app", app.key());
			
		}
        
        map.put("objectId", id);
        map.put("state", states);
        
        return super.find(sb.toString(), -1, -1, map);
    }
	
	/**
     * IDX_REF_A_O  (ObjectId)
     * IDX_AFFAIR_STATE  (State)
     */
	@Override
	public List<Long> findMembers(ApplicationCategoryEnum category, Long objectId,
            List<StateEnum> states, FlipInfo flp) throws BusinessException{
	    
	    StringBuilder hql= new StringBuilder("select a.memberId ")
	            .append(" from CtpAffair as a ")
                .append(" where a.objectId=:objectId ");
	    
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objectId", objectId);
        if(Strings.isNotEmpty(states)){
            hql.append(" and a.state in(:state) ");
            List<Integer> _states = new ArrayList<Integer>();
            for(StateEnum e : states){
                _states.add(e.getKey());
            }
            map.put("state", _states);
        }
        hql.append(" order by a.receiveTime asc");
        
        List<Long> memberIds = DBAgent.find(hql.toString(), map);
        List<Long> ret = new ArrayList<Long>();
        if(Strings.isNotEmpty(memberIds)){
            for(Long id : memberIds){
                if(!ret.contains(id)){
                    ret.add(id);
                }
            }
        }
        
        if(flp != null){
            ret = DBAgent.memoryPaging(ret, flp);
        }
        
        return ret;
	}
	
	@Override
	public void delete(Long id) throws BusinessException {
		String hql = "update CtpAffair as affair set affair.delete=:isDelete where affair.id=:id ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("isDelete", true);
		DBAgent.bulkUpdate(hql, map);
	}
	public void deletePhysicalById(Long id)throws BusinessException {
	    String hql = "delete from CtpAffair as affair where affair.id=:id ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        DBAgent.bulkUpdate(hql, map);
	}
	
	/**
     * IDX_REF_A_O  (ObjectId)
     */
	
	public void deletePhysicalByObjectId(Long objectId)throws BusinessException {
        String hql = "delete from CtpAffair as affair where affair.objectId=:objectId ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objectId", objectId);
        DBAgent.bulkUpdate(hql, map);
    }
	
	/**
     * IDX_REF_A_O  (ObjectId)
     * NO  (MemberId)
     */
	
	public void deletePhysicalByObjectIdAndMemberId(Long objectId, Long memberId)throws BusinessException {
	    String hql = "delete from CtpAffair as affair where affair.objectId=:objectId and affair.memberId=:memberId";
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("objectId", objectId);
	    map.put("memberId", memberId);
	    DBAgent.bulkUpdate(hql, map);
	}
	
	/**
     * IDX_REF_A_O  (ObjectId)
     * NO  (MemberId)
     * IDX_AFFAIR_APP (App)
     */
	public void deletePhysicalByAppAndObjectIdAndMemberId(ApplicationCategoryEnum app, Long objectId, Long memberId)throws BusinessException {
	    String hql = "delete from CtpAffair as affair where  affair.objectId=:objectId and affair.memberId=:memberId and affair.app=:app  ";
	    Map<String, Object> map = new HashMap<String, Object>();
	    map.put("app", app.getKey());
	    map.put("objectId", objectId);
	    map.put("memberId", memberId);
	    DBAgent.bulkUpdate(hql, map);
	}

	

	/**
	 * yangwulin 提供F111接口
	 * @param flipInfo 分页对象
	 * @param params  需要设置的参数 memberId、senderId
	 * 
	 * 
	 * IDX_TT1(MEMBER_ID, STATE, APP, SENDER_ID, IS_DELETE, CREATE_DATE)
	 * 
	 * @return
	 */
    public List<CtpAffair> getSenderOrMemberColAndEdocList(FlipInfo flipInfo, Map params) throws BusinessException {
        StringBuilder sb = new StringBuilder("from ");
        sb.append(CtpAffair.class.getName()).append(" a where");
        sb.append(" a.memberId=:memberId");
        sb.append(" and (a.state in (3,4)) ");
        sb.append(" and (a.app in (1,2,19,20,21) )");
        sb.append(" and (a.senderId=:senderId)");
        if(params.containsKey("delete")){
        	sb.append(" and a.delete = false ");
        }
        sb.append(" order by a.createDate desc");
        
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("memberId", Long.parseLong(String.valueOf(params.get("memberId"))));
        map.put("senderId", Long.parseLong(String.valueOf(params.get("senderId"))));
        
        return DBAgent.find(sb.toString(), map,flipInfo);
    }
    
    /**
	 * 
	 * @param flipInfo 分页对象
	 * @param params  需要设置的参数 memberId、senderId
	 * @return
	 */
    public List<CtpAffair> getSenderColAndEdocList(FlipInfo flipInfo, Map params) throws BusinessException {
        return this.getSenderOrMemberColAndEdocList(flipInfo, params);
    }
    
    /**
     * yangwulin  提供给F111接口
     * @param flipInfo 分页对象
     * @param params 需要设置的参数 memberId、senderId
     * 
     * IDX_TT1(MEMBER_ID, STATE, APP, SENDER_ID, IS_DELETE, CREATE_DATE)
     * 
     * @return List<CtpAffair>
     * @throws BusinessException
     */
    public List<CtpAffair> getSenderOrMemberMtList(FlipInfo flipInfo, Map params) throws BusinessException{
        StringBuffer sb = new StringBuffer("from ");
        sb.append(CtpAffair.class.getName()).append(" a where 1=1");
        sb.append(" and a.memberId=:memberId");
        sb.append(" and a.app=:meeting");
        sb.append(" and a.senderId=:senderId");
        sb.append(" and a.delete=:delete ");
        sb.append(" order by a.createDate desc");
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("memberId", Long.parseLong(String.valueOf(params.get("memberId"))));
        map.put("meeting", ApplicationCategoryEnum.meeting.getKey());
        map.put("senderId", Long.parseLong(String.valueOf(params.get("senderId"))));
        map.put("delete", Boolean.FALSE);
        return DBAgent.find(sb.toString(), map,flipInfo);
    }
	
    @SuppressWarnings("unchecked")
	public List<CtpAffair> getByConditions(FlipInfo flipInfo, Map conditions)throws BusinessException {
		List<CtpAffair> affairs = getByConditions(flipInfo, conditions, false).getData();
		return affairs;
	}
    
    @Override
    public int getCountByConditions(Map conditions) throws BusinessException{
        return getByConditions(null, conditions, true).getTotal();
    }
    
    /**
     * 
     * 重载getByConditions， 支持只获取数量
     * 
     * @param flipInfo
     * @param conditions
     * @param onlyCount
     * @return
     * @throws BusinessException
     *
     * @Author      : xuqw
     * @Date        : 2016年6月24日上午10:41:20
     *
     */
    private FlipInfo getByConditions(FlipInfo flipInfo, Map conditions, boolean onlyCount)throws BusinessException{
        FlipInfo ret = flipInfo;
        
        Map<String, Object> param = new HashMap<String, Object>();
        
        StringBuffer sb = new StringBuffer(" from");
        sb.append(" CtpAffair a where 1=1");
        
        String notInform = (String)conditions.get("notInform");
    	if(Strings.isNotBlank(notInform)){//更新当前待办人时，查询非知会节点的数据
    		List<String> nodePolicy = new ArrayList<String>();
    		nodePolicy.add("zhihui");
    		nodePolicy.add("inform");
    		sb.append(" and a.nodePolicy not in(:nodePolicy)");
    		param.put("nodePolicy", nodePolicy);
    	}
    	
        Iterator<Map.Entry> iter = conditions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            String key = (String) entry.getKey();
            
            if("receiveTimeAscOrDesc".equals(key)){
                continue;
            }
            
            if("notInform".equals(key)){
            	continue;
            }
            
            if("sortWeightNotNull".equals(key)) {
            	continue;
            }
            
            Object value = entry.getValue();
            if (value instanceof List) {
            	sb.append(" and a." + key + " in (:" + key +")");
            }else if(value.getClass().isArray()){
            	sb.append(" and a." + key + " in (:" + key +")");
            } else {
            	sb.append(" and a." + key + " = :" + key );
            }
            param.put(key, value);
        }
        
        //放到后面，避免前面的索引失效
        String sortWeightNotNull = (String)conditions.get("sortWeightNotNull");
    	if(Strings.isNotBlank(sortWeightNotNull)) {
    		sb.append(" and a.sortWeight is null ");
    	}
        
        String asc = (String)conditions.get("receiveTimeAscOrDesc");
        
        List<CtpAffair> affairs = null;
        if(ret == null){
            ret = new FlipInfo();
            if(onlyCount){
                int count = super.count(sb.toString(), param);
            	ret.setTotal(count);
            }else{
            	affairs = super.find(sb.toString(), -1, -1, param);
                ret.setData(affairs);
            }
        }else{
            if(onlyCount){
            	int count =  super.count(sb.toString(), param);
                flipInfo.setTotal(count);
            }else {
            	String orderBy = " order by a.receiveTime desc, id ";
            	if(Strings.isNotBlank(asc) && "asc".equalsIgnoreCase(asc)){
            		orderBy = " order by a.receiveTime asc, id ";
            	}
                sb.append(orderBy);
            	flipInfo.setPagination();
            	
                affairs = super.find(sb.toString(), param);
                flipInfo.setData(affairs);
            }
        }
        return ret;
    }
    
	public List<CtpAffair> getAffairsAll() throws BusinessException {
		StringBuilder hql = new StringBuilder("from "
				+ CtpAffair.class.getName() + " a  order by a.createDate");
		List<CtpAffair> list = DBAgent.find(hql.toString());
		return list;
	}

	/**
	 * 
	 */
	@Override
	public List<CtpAffair> getAffairsByAppAndObjectId(
			ApplicationCategoryEnum collaboration, long summaryId)
			throws BusinessException {
		String hql = "from CtpAffair as affair where affair.objectId = :objectId and affair.app=:app ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("app", collaboration.key());
		map.put("objectId", summaryId);
		return DBAgent.find(hql, map);
	}
	
	@Override
	public List<CtpAffair> getAffairsByObjectId(long summaryId) throws BusinessException {
		String hql = "from CtpAffair as affair where affair.objectId = :objectId ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objectId", summaryId);
		return DBAgent.find(hql, map);
	}
	
	public List<CtpAffair> getAffairsByAppAndObjectIdHis(ApplicationCategoryEnum collaboration, long summaryId)
			throws BusinessException {
		String hql = "from CtpAffair as affair where affair.objectId = :objectId and affair.app=:app ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("app", collaboration.key());
		map.put("objectId", summaryId);
		return super.find(hql, -1, -1, map);
	}

	@Override
	public CtpAffair getWaitSendAffairByObjectIdAndState(
			ApplicationCategoryEnum collaboration, long summaryId, int key)
			throws BusinessException {
		String hql = "from CtpAffair as affair where affair.objectId= :summaryId and affair.state= :state";
		Map map = new HashMap();
		map.put("summaryId", summaryId);
		map.put("state", key);
		// 只返回了第一个元素
		return (CtpAffair) DBAgent.find(hql, map).get(0);
	}

	@Override
	public List<CtpAffair> getAvailabilityTrackingAffairsBySummaryId(
			Long objectId) throws BusinessException {
		String hql = "select a.id,a.senderId,a.memberId,a.state,a.track,a.forwardMember,a.transactorId,a.delete from CtpAffair as a where a.objectId=:objectId and a.state in(:state) and a.track in (:track)";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objectId", objectId);
		List<Integer> l = new ArrayList<Integer>();
		l.add(TrackEnum.all.ordinal());
		l.add(TrackEnum.part.ordinal());
		List<Integer> state = new ArrayList<Integer>();
		state.add(StateEnum.col_sent.key());
		state.add(StateEnum.col_pending.key());
		state.add(StateEnum.col_done.key());
		state.add(StateEnum.col_stepStop.key());
		state.add(StateEnum.col_waitSend.key());
		map.put("track", l);
		map.put("state", state);
		
		List<Object[]> temp = DBAgent.find(hql, map);
		
		List<CtpAffair> result = new ArrayList<CtpAffair>(temp.size());
		for (Object[] objects : temp) {
		    int n = 0;
		    
		    CtpAffair a = new CtpAffair();
		    a.setId((Long)objects[n++]);
		    a.setSenderId((Long)objects[n++]);
		    a.setMemberId((Long)objects[n++]);
		    a.setState((Integer)objects[n++]);
		    a.setTrack((Integer)objects[n++]);
		    a.setForwardMember((String)objects[n++]);
		    a.setTransactorId((Long)objects[n++]);
		    a.setDelete((Boolean)objects[n++]);
		    
		    result.add(a);
        }
		
		return result;
	}

	@Override
	public List<CtpAffair> getSenderAffairsBySubObjectId(Long subObjectId)
			throws BusinessException {
		String hql = "from CtpAffair as affair where affair.subObjectId=:subObjectId and affair.state in(:state)";
		Map<String, Object> map = new HashMap<String, Object>();
		List<Integer> state = new ArrayList<Integer>();
		state.add(+StateEnum.col_waitSend.key());
		state.add(StateEnum.col_sent.key());
		map.put("subObjectId", subObjectId);
		map.put("state", state);
		return DBAgent.find(hql, map);
	}

	@Override
	public List<CtpAffair> getAvailabilityAffairsByAppAndObjectId(ApplicationCategoryEnum appEnum, Long objectId)
			throws BusinessException {
		
	    return getAvailabilityAffairsByAppAndObjectId(appEnum, objectId, false);
	}

	@Override
	public List<CtpAffair> getAvailabilityAffairsByAppAndObjectIdHis(
            ApplicationCategoryEnum appEnum, Long objectId)
            throws BusinessException{
	    return getAvailabilityAffairsByAppAndObjectId(appEnum, objectId, true);
	}
	
	/**
	 * 重构 {@link #getAvailabilityAffairsByAppAndObjectId(ApplicationCategoryEnum, Long)}
	 * 
	 * @param appEnum
	 * @param objectId
	 * @param isHis
	 * @return
	 *
	 */
	private List<CtpAffair> getAvailabilityAffairsByAppAndObjectId(ApplicationCategoryEnum appEnum, Long objectId, boolean isHis){
	    
	    String hql = "from CtpAffair as affair where affair.objectId=:objectId and affair.state in (:state) and affair.app=:app ";

        Map<String, Object> map = new HashMap<String, Object>();

        List<Integer> state = new ArrayList<Integer>();

        state.add(StateEnum.col_done.key());
        state.add(StateEnum.col_pending.key());
        state.add(StateEnum.col_pending_repeat_auto_deal.key());
        state.add(StateEnum.col_waitSend.key());
        state.add(StateEnum.col_sent.key());

        map.put("objectId", objectId);
        map.put("app", appEnum.key());
        map.put("state", state);
        
        if(isHis){
            return super.find(hql, -1, -1, map);
        }else {
            return DBAgent.find(hql, map);
        }
	}
	
	@Override
	public CtpAffair getAffairBySubObjectId(Long subObjectId)
			throws BusinessException {
		String hql = "from CtpAffair as affair where affair.subObjectId=:subObjectId";
		Map map = new HashMap();
		map.put("subObjectId", subObjectId);
		List list= DBAgent.find(hql, map);
		if(null!=list && list.size()>0){
		    return (CtpAffair) list.get(0);
		}else{
		    return null;
		}
	}
	@Override
	public List<CtpAffair> getAffairsByObjectIdAndUserId(
			ApplicationCategoryEnum app, Long summaryId, Long memberId)
			throws BusinessException {
	    return getAffairsByObjectIdAndSubObjectIdAndUserId(app, summaryId, null, memberId, false);
	}
	
	@Override
	public List<CtpAffair> getAffairsByObjectIdAndUserIdHis(
            ApplicationCategoryEnum appEnum, Long objectId, Long userId)
            throws BusinessException{
	    return getAffairsByObjectIdAndSubObjectIdAndUserId(appEnum, objectId, null, userId, true);
	}

    @Override
    public List<CtpAffair> getAffairsByObjectIdAndSubObjectIdAndUserId(ApplicationCategoryEnum appEnum, Long objectId,
            Long subObjectId, Long memberId) throws BusinessException {
        return getAffairsByObjectIdAndSubObjectIdAndUserId(appEnum, objectId, subObjectId, memberId, false);
    }
    
    /**
     * {@link #getAffairsByObjectIdAndSubObjectIdAndUserId(ApplicationCategoryEnum, Long, Long, Long)}
     * 
     * @param appEnum
     * @param objectId
     * @param subObjectId
     * @param memberId
     * @param isHis
     * @return
     * @throws BusinessException
     */
    private List<CtpAffair> getAffairsByObjectIdAndSubObjectIdAndUserId(ApplicationCategoryEnum appEnum, Long objectId,
            Long subObjectId, Long memberId, boolean isHis) throws BusinessException {
        Map<String, Object> params = new HashMap<String, Object>();
        
        StringBuffer hql = new StringBuffer();
		hql.append(" from CtpAffair as affair where affair.objectId=:objectId");
		hql.append(" and affair.state in (:state)");
		hql.append(" and affair.app in (:app)");
		hql.append(" and affair.memberId = :memberId");
		if(subObjectId != null){
			hql.append(" and affair.subObjectId = :subObjectId");
			params.put("subObjectId", subObjectId);
		}
		
		List<Integer> apps = new ArrayList<Integer>();
        if (appEnum == ApplicationCategoryEnum.edoc) {
            apps.add(ApplicationCategoryEnum.edoc.key());
            apps.add(ApplicationCategoryEnum.edocRec.key());
            apps.add(ApplicationCategoryEnum.edocRegister.key());
            apps.add(ApplicationCategoryEnum.edocSend.key());
            apps.add(ApplicationCategoryEnum.edocSign.key());
            apps.add(ApplicationCategoryEnum.exchange.key());
            apps.add(ApplicationCategoryEnum.exSend.key());
            apps.add(ApplicationCategoryEnum.exSign.key());
        } else {
            apps.add(appEnum.key());
        }
		
		List<Integer> state = new ArrayList<Integer>();
		state.add(StateEnum.col_sent.key());
		state.add(StateEnum.col_waitSend.key());
		state.add(StateEnum.col_done.key());
		state.add(StateEnum.col_pending.key());
		
		params.put("memberId", memberId);
		params.put("objectId", objectId);
		params.put("app", apps);
		params.put("state", state);
		
		return super.find(hql.toString(), params);
    }
	
	@Override
	public void updatePendingAndDoneAffairsByObjectIdAndSubObjectIds(
			StateEnum stateEnum, SubStateEnum subStateEnum, Long objectId,
			List<Long> subObjectIds) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		StringBuffer hql = new StringBuffer();
		Map<String, Object> map = new HashMap<String, Object>();
		hql.append("update CtpAffair as affair set affair.state=:state,affair.subState=:subState,affair.updateDate=:updateDate ");
		hql.append("where affair.objectId=:objectId and affair.subObjectId in (:subObjectIds) ");
		map.put("state", stateEnum.key());
		map.put("subState", subStateEnum.key());
		map.put("updateDate", now);
		map.put("objectId", objectId);
		map.put("subObjectIds", subObjectIds);
		DBAgent.bulkUpdate(hql.toString(), map);
		
	}


	@Override
	public List<CtpAffair> getAffairsByObjectIdAndActivityId(Long objectId, Long activityId)
			throws BusinessException {
		StringBuffer hql = new StringBuffer();
		Map<String, Object> map = new HashMap<String, Object>();
		hql.append("from CtpAffair as affair where affair.objectId=:objectId and affair.activityId=:activityId");
		map.put("objectId", objectId);
		map.put("activityId", activityId);
		return super.find(hql.toString(), -1, -1, map);
	}

	@Override
	public List<CtpAffair> getAffairsByObjectIdAndState(Long objectId,
			StateEnum state) throws BusinessException {
		StringBuffer hql = new StringBuffer();
		Map<String, Object> map = new HashMap<String, Object>();
		hql.append("from CtpAffair as affair where affair.objectId=:objectId and affair.state=:state");
		map.put("objectId", objectId);
		map.put("state", state.key());
		return DBAgent.find(hql.toString(), map);
	}
	
	@SuppressWarnings("unchecked")
	public List<CtpAffair> getAffairs(Long objectId,StateEnum state,SubStateEnum subState){
		StringBuffer hql = new StringBuffer();
		Map<String, Object> map = new HashMap<String, Object>();
		hql.append("from CtpAffair as affair where affair.objectId=:objectId and affair.state=:state and affair.subState=:subState");
		map.put("objectId", objectId);
		map.put("state", state.key());
		map.put("subState", subState.key());
		return DBAgent.find(hql.toString(), map);
	}
	public void update(String hql, Map<String, Object> params) throws BusinessException{
		DBAgent.bulkUpdate(hql, params);
	}
	@Override
	public void update(Map<String, Object> columns, Object[][]  where)
			throws BusinessException {
		Object[][] w = where;
		if (columns == null || columns.size() == 0) {
			return;
		}

		if (w == null) {
			w = new Object[0][2];
		}

		StringBuilder sb = new StringBuilder();
		sb.append("update " + CtpAffair.class.getName()).append(" a set ");
		//Object[] values = new Object[columns.size() + w.length];

		Map<String,Object> params = new HashMap<String,Object>();
		
		Set<String> keys = columns.keySet();

		int len = keys.size();

		int i = 0;
		for (String key : keys) {
			sb.append("a." + key + "= :"+key);
			
			if (i < len - 1) {
				sb.append(", ");
			}
			i++;
			params.put(key,columns.get(key));
		}

		if (w != null && w.length > 0) {
			sb.append(" where ");

			len = w.length;
			int j = 0;
			for (Object[] key : w) {
				if(key[1] instanceof List){
					if(((List)key[1]).size() >1 ){
						
						sb.append("(a." + key[0] + " in (:wh"+key[0]+"))");  //加wh前缀，避免update字段和where字段相同的情况
					}
					else {
						sb.append("(a." + key[0] + " = :wh"+key[0]+")");  //加wh前缀，避免update字段和where字段相同的情况
					}
				}
				else{
					sb.append("(a." + key[0] + " = :wh"+key[0]+")");
				}
				
				if (j < len - 1) {
					sb.append(" and ");
				}
				j++;

				
				params.put((String)("wh"+key[0]), key[1]);
			}
		}

		DBAgent.bulkUpdate(sb.toString(), params);
	}

    @Override
    public List<CtpAffair> findPageCriteria(DetachedCriteria criteria, FlipInfo flipInfo) throws BusinessException {
        return DBAgent.findByCriteria(criteria, flipInfo);
    }

    @Override
    public List<CtpAffair> findPageCriteria(DetachedCriteria criteria) throws BusinessException {
        return DBAgent.findByCriteria(criteria);
    }

	@Override
	public int getTrackCount4BizConfig(Long memberId, List<Long> tempIds) {
		Map<String, Object> params = new HashMap<String, Object>();
    	StringBuffer hql = new StringBuffer();
    	setHqlAndParams(hql, params, true, memberId, tempIds);
    	return DBAgent.count(hql.toString(), params);
	}
	
	private void setHqlAndParams(StringBuffer hql, Map<String, Object> params, boolean queryCount, Long memberId, List<Long> tempIds) {
	    hql.append(" select a ");
		hql.append(" from CtpAffair as a, ColSummary as c where ");
    	hql.append(" a.app=:col and a.memberId=:memberId and (a.state=:sentState or a.state=:doneState) ");
    	hql.append(" and a.delete=:isDelete and a.archiveId is null and a.finish=:isFinish and a.track in (:isTrack)")
    	   .append(" and a.objectId=c.id and c.templeteId in (:formTempIds)");
    	if(!queryCount) {
    	   hql.append(" order by a.createDate desc");
    	}

    	params.put("col", ApplicationCategoryEnum.collaboration.key());
    	params.put("memberId", memberId);
    	params.put("isFinish", Boolean.FALSE);
    	params.put("isDelete", Boolean.FALSE);
    	List<Integer> l = new ArrayList<Integer>();
    	l.add(TrackEnum.all.ordinal());
    	l.add(TrackEnum.part.ordinal());
    	params.put("isTrack", l);
    	params.put("sentState", StateEnum.col_sent.key());
    	params.put("doneState", StateEnum.col_done.key());
    	params.put("formTempIds", tempIds);
	}
	@Override
	public void updateFinishFlag(Long objectId) {
		String hql = "update " + CtpAffair.class.getName() + " a set a.finish = ?  , track = ?  where a.objectId = ?";
		Object[] values = {Boolean.TRUE , Integer.valueOf(TrackEnum.no.ordinal()), objectId};
		DBAgent.bulkUpdate(hql, values);
	}

	@Override
	public List<CtpAffair> getAffairsByActivityId(Long activityId) {
		String hql="from CtpAffair as a where a.activityId= :activityId order by a.completeTime desc ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("activityId",activityId);
		return DBAgent.find(hql, map);
	}

	@Override
	public List<CtpAffair> getPendingAffairListByObject(Long summaryId)
			throws BusinessException {
		String hql="from CtpAffair as affair where affair.objectId= :summaryId and affair.state=:state";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("summaryId",summaryId);
		map.put("state", StateEnum.col_pending.key());
		return DBAgent.find(hql, map);
	}
    public List<CtpAffair> getAffairsByObjectIdAndStates(Long summaryId,List<Integer> states) throws BusinessException{
        String hql="from CtpAffair as affair where affair.objectId= :summaryId and affair.state in(:state) ";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("summaryId",summaryId);
        map.put("state", states);
        return super.find(hql,-1,-1, map);
    }
	//@Override
//	public List<CtpAffair> getTrackList4BizConfig(Long memberId,
//			List<Long> tempIds) {
//		Map<String, Object> params = new HashMap<String, Object>();
//    	StringBuffer hql = new StringBuffer();
//    	setHqlAndParams(hql, params, false, memberId, tempIds);
//    	return DBAgent.find(hql.toString(), params);
//	}

    @Override
    public List<CtpAffair> getALLAvailabilityAffairList(FlipInfo flipInfo, Map params) throws BusinessException {
        String hql = "from CtpAffair as affair where 1=1 and affair.objectId = :objectId ";
        Map<String, Object> map = new HashMap<String, Object>();
        Long objectId = null;
        if(params.get("objectId") != null){
            objectId = Long.parseLong(params.get("objectId").toString());
        }
        if(objectId == null){
            return null;
        }
        map.put("objectId", objectId);
        if(null != params.get("numProcessed")){
        	hql = hql + " and affair.state in (:state) ";
        	map.put("state", new Object[]{StateEnum.col_done.key(),StateEnum.col_pending_repeat_auto_deal.key()});
        }else if(null != params.get("numPending")){
        	hql = hql + " and (affair.state in (:state) or (affair.state = :waitSend and affair.subState in (:backedWaitSend) )) ";
        	map.put("waitSend", StateEnum.col_waitSend.key());
        	List list = new ArrayList();
        	list.add(SubStateEnum.col_pending_specialBacked.key());
        	list.add(SubStateEnum.col_pending_Back.key());
        	map.put("backedWaitSend", list);
        	map.put("state", new Object[]{StateEnum.col_pending.key()});
        }else if(null != params.get("numViewed")){
        	hql = hql + " and (affair.state in (:state) and affair.subState in(:viewed)) ";
        	map.put("viewed",new Object[]{SubStateEnum.col_pending_read.key(),SubStateEnum.col_pending_specialBacked.key(),
        			SubStateEnum.col_pending_ZCDB.key(),SubStateEnum.col_pending_specialBack.key(),
        			SubStateEnum.col_pending_takeBack.getKey()});
        	map.put("state", new Object[]{StateEnum.col_pending.key()});
        }else if(null != params.get("numNotViewed")){
        	hql = hql + " and (affair.state in (:state) and affair.subState = :notViewed) ";
        	map.put("notViewed", SubStateEnum.col_pending_unRead.key());
        	map.put("state", new Object[]{StateEnum.col_pending.key()});
        }else{
        	hql = hql + " and (affair.state in (:state) or (affair.state = :waitSend and affair.subState = :backedWaitSend)) ";
        	map.put("waitSend", StateEnum.col_waitSend.key());
        	map.put("backedWaitSend", SubStateEnum.col_pending_specialBacked.key());
        	map.put("state", new Object[]{StateEnum.col_sent.key(),StateEnum.col_done.key(), StateEnum.col_pending.key(),StateEnum.col_pending_repeat_auto_deal.key()});
        	if(params.get("subState") != null){
                hql+=" and affair.subState in (:subState) ";
                map.put("subState", (List)params.get("subState"));
            }
            if(params.get("delete") != null){
               hql+=" and affair.delete = :isDelete ";
               map.put("isDelete", (Boolean)params.get("delete"));
            }
        }
        if(params.get("app") != null){
            hql = hql + " and affair.app = :app ";
            map.put("app", Integer.valueOf(params.get("app").toString()));
        }
        
        hql = hql +" order by affair.state desc, affair.receiveTime desc ,affair.subObjectId asc ";
        if(flipInfo == null){
            return DBAgent.find(hql, map);
        }else { 
        	flipInfo.setPagination();
        	List find = super.find(hql, map);
        	flipInfo.setTotal(Pagination.getRowCount(false));
    		if(flipInfo.getPage() > flipInfo.getPages().intValue()){
    			flipInfo.setPage(flipInfo.getPages());
    	        Pagination.setFirstResult(flipInfo.getStartAt());
    	        find = super.find(hql.toString(), map);
    		}
        	return find;
        }
    }
    
    public Map<Long,Integer>  getOverNodeCount(
            Long templeteId,
            Long accountId,
            boolean isCol,
            List<Integer> states,
            Date startDate,
            Date endDate){
        
        final StringBuilder sb = new StringBuilder();
        sb.append(" select ");
        sb.append(" affair.activityId");
        sb.append(" from ");
        sb.append(" CtpAffair as affair ,");
        if(isCol){
            sb.append(" ColSummary as summary ");
        }else{
            sb.append(" EdocSummary as summary ");
        }
        sb.append(" where ");
        sb.append(" affair.objectId = summary.id ");
        sb.append(" and summary.orgAccountId = :accountId");
        sb.append(" and affair.templeteId = :templeteId ");
        sb.append(" and affair.overWorktime > 0 ");
        sb.append(" and affair.createDate between :startDate and :endDate ");
        sb.append(" and affair.state in (:state) ");
        sb.append(" group by affair.activityId,affair.objectId ");
    
        final Map<String,Object> parameter = new HashMap<String,Object>();
        parameter.put("templeteId", templeteId);
        parameter.put("startDate", startDate);
        parameter.put("endDate", endDate);
        parameter.put("accountId", accountId);
        parameter.put("state", states);
        
        Map<Long,Integer> m = new HashMap<Long,Integer>();
        List l = DBAgent.find(sb.toString(), parameter);
        for(Object o :l){
            Long activityId = 0L;
            if(o!=null){
                activityId = ((Number)o).longValue();
            }
            if(m.get(activityId)== null){
                m.put(activityId, 1);
            }else{
                m.put(activityId, m.get(activityId)+1);
            }
        }
        return m;
    }
    public Map<Long,String>  getNodeCountAndSumRunTime(
            Long templeteId,
            Long accountId,
            boolean isCol,
            List<Integer> states,
            Date startDate,
            Date endDate){
        
        final StringBuilder sb = new StringBuilder();
        sb.append(" select ");
        sb.append(" affair.activityId ,");
        sb.append(" max(affair.runWorktime) ");
        sb.append(" from ");
        sb.append(" CtpAffair affair, ");
        if(isCol){
            sb.append(" ColSummary summary ");
        }else{
            sb.append(" EdocSummary summary ");
        }
        sb.append(" where ");
        sb.append(" affair.objectId = summary.id ");
        sb.append(" and summary.orgAccountId = :accountId");
        sb.append(" and affair.templeteId = :templeteId ");
        sb.append(" and affair.createDate between :startDate and :endDate ");
        sb.append(" and affair.state in (:state) ");
        sb.append(" group by affair.activityId,affair.objectId ");
    
        final Map<String,Object> parameter = new HashMap<String,Object>();
        parameter.put("templeteId", templeteId);
        parameter.put("startDate", startDate);
        parameter.put("endDate", endDate);
        parameter.put("accountId", accountId);
        parameter.put("state", states);
        
        List l = DBAgent.find(sb.toString(), parameter);
        Map<Long,String> map = new HashMap<Long,String>();
        Map<Long,Integer> cm = new HashMap<Long,Integer>();
        Map<Long,Long>  sm = new HashMap<Long,Long>();
        for(Object o :l){
            Object[] arr =(Object[])o;
            
            int count = 0;
            Long sumRum = 0l;
            
            Long activityId = 0L;
            if(arr[0]!=null){
                activityId = ((Number)arr[0]).longValue();
            }
            Long time = 0L;
            if(arr[1] != null){
                time = ((Number)arr[1]).longValue();
            }
        
            if(cm.get(activityId) == null){
                count = 1;
            }else{
                count = cm.get(activityId)+1;
            }
            cm.put(activityId, count);
            
            if(sm.get(activityId)==null){
                sumRum = time;
            }else{
                sumRum = sm.get(activityId)+time;
            }
            sm.put(activityId, sumRum);
        }
        
        for(Iterator<Long> it = cm.keySet().iterator();it.hasNext();){
            Long activityId = it.next();
            map.put(activityId,cm.get(activityId)+"_"+sm.get(activityId));
        }
        
        return map;
    }
    public List<CtpAffair> getAffairByActivityId(
            Long templeteId,
            Long orgAccountId,
            boolean isCol,
            List<Integer> states,
            Long activityId,
            Date startDate,
            Date endDate){
        StringBuilder sb = new StringBuilder();
        sb.append(" select ");
        sb.append(" affair ");
        sb.append(" from CtpAffair as affair,");
        if(isCol){
            sb.append(" ColSummary as summary ");
        }else{
            sb.append(" EdocSummary as summary ");
        }
        sb.append(" where ");
        sb.append(" summary.id = affair.objectId ");
        sb.append(" and affair.templeteId = :templeteId ");
        sb.append(" and summary.orgAccountId = :accountId ");
        sb.append(" and affair.activityId = :activityId ");
        sb.append(" and affair.createDate between :startDate and :endDate ");
        sb.append(" and affair.delete = :isDelete ");
        sb.append(" and affair.state in (:state) ");
        
        Map<String,Object> map =new HashMap<String,Object>();
        map.put("templeteId", templeteId);
        map.put("accountId", orgAccountId);
        map.put("activityId", activityId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("isDelete", Boolean.FALSE);
        map.put("state", states);
        return DBAgent.find(sb.toString(), map);
    } 
    public Map<Long,String> getStaticsByActivityId(
            Long templeteId,
            Long orgAccountId,
            boolean isCol,
            List<Integer> states,
            Long activityId,
            Date startDate,
            Date endDate){
        StringBuilder sb = new StringBuilder();
        sb.append(" select");
        sb.append(" affair.memberId,");
        sb.append(" count(affair.id),");
        sb.append(" sum(affair.runWorktime),");
        sb.append(" max(affair.deadlineDate) ");
        sb.append(" from CtpAffair as affair ,");
        if(isCol){
            sb.append(" ColSummary as summary ");
        }else{
            sb.append(" EdocSummary as summary ");
        }
        sb.append(" where ");
        sb.append(" summary.id = affair.objectId ");
        sb.append(" and affair.templeteId = :templeteId ");
        sb.append(" and summary.orgAccountId = :accountId ");
        sb.append(" and affair.activityId = :activityId ");
        sb.append(" and affair.createDate between :startDate and :endDate ");
        sb.append(" and affair.delete = :isDelete ");
        sb.append(" and affair.state in (:state) ");
        sb.append(" group by affair.memberId ");
        
        Map<String,Object> map =new HashMap<String,Object>();
        map.put("templeteId", templeteId);
        map.put("accountId", orgAccountId);
        map.put("activityId", activityId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("isDelete", Boolean.FALSE);
        map.put("state", states);
        
        Map<Long,String> m = new HashMap<Long,String>();
        List l = DBAgent.find(sb.toString(),map);
        if(l!=null && l.size()!=0){
            for(Object o : l){
                Object[] a = (Object[])o;
                Long memberId =((Number)a[0]).longValue();
                Integer c = ((Number)a[1]).intValue();
                Integer avgRunWorkTime = 0 ;
                if (a[2] != null)
                    avgRunWorkTime =((Number)a[2]).intValue();
                m.put(memberId, c+"_"+avgRunWorkTime);
            }
        }
        return m;
    } 
    public Map<Long,Integer> getOverCountByMember(
            Long templeteId,
            Long orgAccountId,
            boolean isCol,
            List<Integer> states,
            Long activityId,
            Date startDate,
            Date endDate){
        StringBuilder sb = new StringBuilder();
        sb.append(" select");
        sb.append(" affair.memberId,");
        sb.append(" count(affair.id)");
        sb.append(" from CtpAffair as affair,");
        if(isCol){
            sb.append(" ColSummary as summary ");
        }else{
            sb.append(" EdocSummary as summary ");
        }
        sb.append(" where ");
        sb.append(" summary.id = affair.objectId ");
        sb.append(" and affair.templeteId = :templeteId ");
        sb.append(" and summary.orgAccountId = :accountId ");
        sb.append(" and affair.activityId = :activityId ");
        sb.append(" and affair.createDate between :startDate and :endDate ");
      //  sb.append(" and affair.delete = :isDelete ");
        sb.append(" and affair.overWorktime>0 ");
        sb.append(" and affair.state in (:state) ");
        sb.append(" group by affair.memberId ");
        
        Map<String,Object> map =new HashMap<String,Object>();
        map.put("templeteId", templeteId);
        map.put("accountId", orgAccountId);
        map.put("activityId", activityId);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
      //  map.put("isDelete", Boolean.FALSE);
        map.put("state", states);
        List l =  DBAgent.find(sb.toString(), map);
        
        Map<Long,Integer> m = new HashMap<Long,Integer>();
        
        if(l!=null && l.size()!=0){
            for(Object o :l){
                Object[] a = (Object[])o;
                Long memberId = ((Number)a[0]).longValue();
                Integer overCount = ((Number)a[1]).intValue();
                m.put(memberId, overCount);
            }
        }
        return m;
    }

    @Override
    public void deleteByAppAndSubObjectId(ApplicationCategoryEnum appEnum, Long subObjectId) throws BusinessException {
        String hql = "update CtpAffair as affair set affair.delete=:isDelete where affair.app=:app and affair.subObjectId = :subObjectId ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("app", appEnum.key());
        map.put("subObjectId", subObjectId);
        map.put("isDelete", true);
        DBAgent.bulkUpdate(hql, map);
        
    } 
    
    public Date getMinStartTimePending(Long memberId) throws BusinessException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("memberId", memberId);
        String hql = "select min(a.receiveTime) from CtpAffair a where a.memberId=:memberId and a.state=3";
        List list = DBAgent.find(hql, params);
        if (null != list && list.size() > 0) {
            if (list.get(0) != null) {
                return (Date) list.get(0);
            }
        }
        return null;
    }
    

	@Override
	public void updateAffairByActivity(Long objectId, List<Long> activities,
			Integer state, Integer subState, Integer otherwise)
			throws BusinessException {
		Map<String,Object> param = new HashMap<String,Object>();
    	param.put("activityId", activities);
    	param.put("objectId", objectId);
    	String hql = "update CtpAffair set state=?,subState=? where objectId=:objectId and activityId in(:activityId)";
    	if(otherwise != null) {
    		hql += " and subState=:otherwise";
    		param.put("otherwise", otherwise);
    	}else {
    		hql += " and state!=:state";
    		param.put("state", state);
    	}
    	DBAgent.bulkUpdate(hql, param,state,subState);
	}

	@Override
	public void updateAffairsStateAndUpdateDate(Long objectId) throws BusinessException {
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		StringBuffer hql = new StringBuffer();
		Map<String, Object> map = new HashMap<String, Object>();
		hql.append("update CtpAffair as affair set affair.state=:state,affair.subState=:subState,affair.updateDate=:updateDate ");
		hql.append("where affair.objectId=:objectId and affair.state in (:states)");
		List<Integer> states = new ArrayList<Integer>();
		states.add(StateEnum.col_pending.key());
		states.add(StateEnum.col_done.key());
		states.add(StateEnum.col_pending_repeat_auto_deal.key());
		
		
		map.put("state", StateEnum.col_cancel.key());
		map.put("subState", SubStateEnum.col_normal.key());
		map.put("updateDate", now);
		map.put("objectId", objectId);
		map.put("states", states);
		DBAgent.bulkUpdate(hql.toString(), map);
	}

	public List<CtpAffair> getTrackingAndPendingAffairBySummaryId(Long summaryId,int app){
	   
	    DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class)
        .add(Expression.eq("objectId", summaryId))
        .add(Expression.eq("app", app))
        .add(Expression.or(
                Expression.eq("state", StateEnum.col_pending.key()),
                Expression.and(Expression.eq("state", StateEnum.col_done.key()),Expression.or( Expression.eq("track", TrackEnum.all.ordinal()), Expression.eq("track", TrackEnum.part.ordinal())))
            )
        ) ;
        return DBAgent.findByCriteria(criteria);
	}
	public List<CtpAffair> getPendingAffairListByNodes(Long summaryId, List<Long> nodeIds) throws BusinessException {
        if(nodeIds == null || nodeIds.isEmpty()){
            return null;
        }
        DetachedCriteria criteria = DetachedCriteria.forClass(CtpAffair.class)
        .add(Restrictions.eq("objectId", summaryId))
        .add(Restrictions.eq("state", StateEnum.col_pending.key()))
        .add(Restrictions.in("activityId", nodeIds))
        .add(Restrictions.eq("delete", false));
        return DBAgent.findByCriteria(criteria);
    }

	/**
     * 首页PORTAL查询指定发起人代办事项。
     * @param 组织模型对象串   Account|1,3_Department|1,23_Member|1,23
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAffairListBySender(final String sql, final Map<String, Object> parameter, final boolean onlyCount, final FlipInfo fi,final String... groupByPropertyName) {
    	
		return (Object) super.getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				boolean isNeedCount = onlyCount || (fi != null && fi.isNeedTotal());
				int count = 0;
				String s="";
				if(isNeedCount){
					if(groupByPropertyName.length==2){
						s = "select "+groupByPropertyName[0]+","+groupByPropertyName[1]+",count(distinct affair.id) " + sql+ " group by "+groupByPropertyName[0]+","+groupByPropertyName[1];
						SQLQuery query = session.createSQLQuery(s);
						setParameter(parameter, query);
						return query.list();
					}else if(groupByPropertyName.length==1){
						s = "select "+groupByPropertyName[0]+",count(distinct affair.id) " + sql+"group by "+groupByPropertyName[0];
						SQLQuery query = session.createSQLQuery(s);
						setParameter(parameter, query);
						return query.list();
					}else{
						s = "select count(distinct affair.id) as count " + sql;
						SQLQuery query = session.createSQLQuery(s);
						setParameter(parameter, query);
						query.addScalar("count", Hibernate.INTEGER);
						Object r = query.uniqueResult();
						count = r == null ? 0 : (Integer)r;
					}
				}
				
				if(onlyCount){
					return count;
				}
				else if(fi != null){
				    boolean aiSort = false;
				    boolean topSort = false;
                    for(FlipInfo.SortPair sortPair : fi.getSortPairs()){
                        if("sortWeight".equals(sortPair.getSortField())){
                            aiSort = true;
                        }
                        if("topTime".equals(sortPair.getSortField())){
                        	topSort = true;
                        }
                    }
					s = "select distinct affair.* " + sql;
                    if(aiSort) {
                        s += "order by affair.sort_weight desc,affair.receive_time desc";
                    } else if (topSort) {
                    	s += "order by affair.top_time desc ,affair.receive_time desc";
                    } else {
                        s += "order by affair.receive_time desc";
                    }
					SQLQuery query = session.createSQLQuery(s);
					setParameter(parameter, query);
					
					query.addEntity(CtpAffair.class);
					
					query.setFirstResult(fi.getStartAt());
					query.setMaxResults(fi.getSize());
					
					List list = query.list();
					
					fi.setData(list);
					fi.setTotal(count);
					
					return list;
				}
				
				return null;
			}
		});
	}
	
	private static void setParameter(Map<String, Object> parameter, Query query){
		if (parameter != null) {
			Set<Map.Entry<String, Object>> entries = parameter.entrySet();
			for (Map.Entry<String, Object> entry : entries) {
				String name = entry.getKey();
				Object value = entry.getValue();
				if(value instanceof Collection){
					query.setParameterList(name, (Collection)value);
				}
				else if(value instanceof Object[]){
					query.setParameterList(entry.getKey(), (Object[])value);
				}
				else{
					query.setParameter(entry.getKey(), value);
				}
			}
		}
	}

	@Override
	public void updateAllAvailabilityAffair(ApplicationCategoryEnum appEnum,
			Long objectId, Map<String, Object> values) {
		StringBuffer hql = new StringBuffer("update CtpAffair as affair set ");
		Set<String> keys = values.keySet();
		Map<String,Object> parameter = new HashMap<String,Object>();
		for(String key :keys){
			Object value = values.get(key);
			hql.append(key+"=:"+key);
			parameter.put(key, value);
		}
		hql.append(" where affair.objectId=:objectId and affair.app = :app and affair.state in (:state) and affair.delete=:isDelete ");
		parameter.put("objectId", objectId);
		parameter.put("app", appEnum.getKey());
		parameter.put("state", new Object[]{StateEnum.col_sent.key(),
				StateEnum.col_done.key(), StateEnum.col_pending.key()});
		parameter.put("isDelete", false);
		DBAgent.bulkUpdate(hql.toString(), parameter);
	}
	public boolean checkPermission4TheObject(ApplicationCategoryEnum app,
            Long objectId, List<Long> memberIds) {
        String hql = "select count(id) from CtpAffair where objectId=:objectId and app=:app and memberId in(:memberIds)";
        Map<String, Object> namedParameterMap = new HashMap<String, Object>();
        namedParameterMap.put("objectId", objectId);
        namedParameterMap.put("app", app.ordinal());
        namedParameterMap.put("memberIds", memberIds);
        List results = DBAgent.find(hql, namedParameterMap);
        if(null==results || results.isEmpty() || results.size()==0){
            return false;
        }
        return ((Number)results.get(0)).longValue() > 0;
    }
	
	public List<CtpAffair> getAffairsByActivityId(Long objectId,Long activityId) throws BusinessException{
        String hql="from CtpAffair as a where a.activityId= :activityId and a.objectId=:objectId  and a.delete = :isDelete and a.state in(:state) order by a.completeTime desc ";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("activityId",activityId);
        map.put("objectId", objectId);
        map.put("isDelete", Boolean.FALSE);
        List<Integer> states= new ArrayList<Integer>();
        states.add(StateEnum.col_pending.getKey());
        states.add(StateEnum.col_done.getKey());
        states.add(StateEnum.col_stepStop.getKey());
        states.add(StateEnum.col_pending_repeat_auto_deal.getKey());
        map.put("state", states);
      
        return super.find(hql,-1,-1, map);
    }
	  /**
     * 支持公文迁移过滤 的验证
     * @param objectId
     * @return
     * @throws BusinessException
     */
   public  CtpAffair getEdocSenderAffair(Long objectId) throws BusinessException {
       DetachedCriteria criteria = DetachedCriteria
               .forClass(CtpAffair.class)
               .add(Restrictions.eq("objectId", objectId))
               .add(Restrictions.eq("delete", false))
               .add(Restrictions.in("state",
                       new Object[] { StateEnum.col_sent.key(),
                               StateEnum.col_waitSend.key() }));
       List<CtpAffair> list = DBAgent.findByCriteria(criteria);
       
       CtpAffair senderAffair = null;
       if (Strings.isNotEmpty(list)) {
           senderAffair = list.get(0);
       }
       return senderAffair;
   }
   
   @Override
   public int getCountAffairsByAppsAndStatesAndMemberId(List<ApplicationCategoryEnum> appEnums,List<StateEnum> statesEnums,Long memberId){
       return getAffairsByAppsAndStatesAndMemberId(null, appEnums, statesEnums, memberId, true).getTotal();
   }
   
   @Override
   public List<CtpAffair> getAffairsByAppsAndStatesAndMemberId(FlipInfo flipInfo,List<ApplicationCategoryEnum> appEnums,List<StateEnum> statesEnums,Long memberId){
       return getAffairsByAppsAndStatesAndMemberId(flipInfo, appEnums, statesEnums, memberId, false).getData();
   }
   
   /**
    * 
    * 重构getCountAffairsByAppsAndStatesAndMemberId 方法
    * 
    * @param flipInfo
    * @param appEnums
    * @param statesEnums
    * @param memberId
    * @param onlyCount 是否只获取数量
    * @return
    *
    * @Since A8-V5 6.1
    * @Author      : xuqw
    * @Date        : 2017年2月15日下午1:42:13
    *
    */
   private FlipInfo getAffairsByAppsAndStatesAndMemberId(FlipInfo flipInfo,List<ApplicationCategoryEnum> appEnums,
           List<StateEnum> statesEnums,Long memberId, boolean onlyCount){
       
       String hql="from CtpAffair as a where a.memberId = :memberId and a.app in (:app) and a.delete = :isDelete and a.state in(:state)";
       
       Map<String, Object> map = new HashMap<String, Object>();
       map.put("memberId",memberId);
      
       map.put("isDelete", Boolean.FALSE);
       List<Integer> apps= new ArrayList<Integer>();
       for(ApplicationCategoryEnum app : appEnums){
           apps.add(app.getKey());
       }
       map.put("app", apps);
       
       List<Integer> states= new ArrayList<Integer>();
       for(StateEnum se : statesEnums){
           states.add(se.getKey());
       }
       map.put("state", states);
       
       //作为复合对象传递出去
       FlipInfo f = new FlipInfo();
       f.setNeedTotal(false);
       
       if(onlyCount){
           int count = DBAgent.count(hql, map);
           f.setTotal(count);;
       }else{
           List<CtpAffair> affairs = DBAgent.find(hql, map,flipInfo);
           f.setData(affairs);
       }
       return f;
   }
   
   
   
   @SuppressWarnings("unchecked")
	public CtpAffair getSimpleAffair(Long id) throws BusinessException{
	   	StringBuilder sb = new StringBuilder();
	   	sb.append(" SELECT ");
	   	sb.append(" affair.id, ");
	   	sb.append(" affair.app, ");
	   	sb.append(" affair.subject, ");
	   	sb.append(" affair.state, ");
	   	sb.append(" affair.subState, ");
	   	sb.append(" affair.senderId, ");
	   	sb.append(" affair.delete, ");
	   	sb.append(" affair.forwardMember, ");
		sb.append(" affair.objectId, ");
		sb.append(" affair.formRecordid, ");
		sb.append(" affair.nodePolicy ");
	   	sb.append(" FROM ");
	   	sb.append(" CtpAffair as affair ");
	   	sb.append(" WHERE ");
	   	sb.append(" affair.id = :id ");
	   	
		Map parameterMap = new HashMap();
		parameterMap.put("id", id);
	
		List list =  super.find(sb.toString(), -1, -1, parameterMap);
		
		CtpAffair affair = null;
		if(Strings.isNotEmpty(list)){
			Object[] p = (Object[]) list.get(0);
			affair = new CtpAffair();
			
			affair.setId((Long)p[0]);
			affair.setApp((Integer)p[1]);
			affair.setSubject((String)p[2]);
			affair.setState((Integer)p[3]);
			affair.setSubState((Integer)p[4]);
			affair.setSenderId((Long)p[5]);
			affair.setDelete((Boolean)p[6]);
			affair.setForwardMember((String)p[7]);
			affair.setObjectId((Long)p[8]);
			try{
				affair.setFormRecordid((Long)p[9]);
			}catch(Exception e){}
			affair.setNodePolicy((String)p[10]);
		}
		return affair;
   }

   @Override
	public Object getAffairListBySender(PortalQueryParam portalQueryParam) {
		
		Long memberId = portalQueryParam.getMemberId();
		String orgStr = portalQueryParam.getOrgStr();
		AffairCondition condition = portalQueryParam.getCondition();
		boolean onlyCount  = portalQueryParam.isOnlyCount();
		FlipInfo fi = portalQueryParam.getFi();
		List<Integer> appEnum = portalQueryParam.getAppEnum();
		boolean isGroupBy = portalQueryParam.isGroupBy();
		String[] groupByPropertyName = portalQueryParam.getGroupByPropertyName();
		
		if(orgStr == null||"".equals(orgStr)) return new ArrayList<CtpAffair>();
	   	String [] types = orgStr.split("[,]");
	   	boolean isAccount  = false;
	   	boolean isDepartment = false;
	   	boolean isMember = false;
	   	boolean isTeam = false;
	   	boolean isPost = false;
	   	boolean isPeopleRelate = false;
	   	List<Long> accountIds = new ArrayList<Long>();
	   	List<Long> departmentIds = new ArrayList<Long>();
	   	List<Long> postIds = new ArrayList<Long>();
	   	List<Long> peopleRelateTypes = new ArrayList<Long>();
	   	List<Long> memberIds = new ArrayList<Long>();
	   	List<Long> teamIds = new ArrayList<Long>();
	   	for(int i=0; i <types.length ;i++){
	   		String singleType[] = types[i].split("[|]");
	   		if("Account".equals(singleType[0])){
	   			isAccount = true;
	   			accountIds.add(Long.valueOf(singleType[1]));
	   		}else if("Department".equals(singleType[0])){
	   			isDepartment = true;
	   			departmentIds.add(Long.valueOf(singleType[1]));
	   			try {
	   					//包含子部门
	   					OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
						List<V3xOrgDepartment>  depts = orgManager.getChildDepartments(Long.valueOf(singleType[1]),false);
						for(V3xOrgDepartment dept :depts){
							departmentIds.add(dept.getId());
						}
					} catch (NumberFormatException e) {
						LOGGER.error("",e);
					} catch (BusinessException e) {
						LOGGER.error("",e);
					}
	   		}else if("Member".equals(singleType[0])){
	   			isMember = true;
	   			memberIds.add(Long.valueOf(singleType[1]));
	   		}else if("Post".equals(singleType[0])){
	   			isPost = true;
	   			postIds.add(Long.valueOf(singleType[1]));
	   		}else if("RelatePeople".equals(singleType[0])){
	   			isPeopleRelate = true;
	   			peopleRelateTypes.add(Long.valueOf(singleType[1]));
	   		}else if("Team".equals(singleType[0])){
	   			isTeam = true;
	   			teamIds.add(Long.valueOf(singleType[1]));
	   		}
	   	}
	   	final StringBuilder hql = new StringBuilder();
	   	final Map<String,Object> parameter = new HashMap<String,Object>();
	     	
	
	   	StateEnum stateEnum = null;
	       if(condition.getState() == null){
	           stateEnum = StateEnum.col_pending;
	       }else{
	           stateEnum = condition.getState();
	       }
	       
	   	if (!isGroupBy) {
	   	    hql.append("from ctp_affair affair ");
	   	}
	   	if(isMember || isPost || isDepartment || isAccount){
	   		hql.append(" inner join org_member m on affair.sender_id= m.id ");
	   	}
	   	if(isPeopleRelate){
	   		hql.append(" left join relate_member pr on affair.member_id = pr.related_id  ");
	   	}
	   	if(isTeam) {
	   		hql.append(" left join org_relationship rship on affair.sender_id = rship.objective0_id  ");
	   		hql.append(" left join org_team rteam on rship.source_id = rteam.id  ");
	   	}
	   	hql.append(" where ");
	   	if(appEnum.size()>0){
	   		hql.append(" affair.app in (:appEnum) and ");
	   		parameter.put("appEnum",appEnum);
	   	}
	
	   	hql.append(" affair.member_id = :memberId ");
	
	   	hql.append(" and affair.sub_state != :substate  ");
	   	parameter.put("substate",SubStateEnum.meeting_pending_periodicity.getKey());
	 
	       //当查已办affair时，还需要加上complete_time不为空的条件
	       if(stateEnum.key() == StateEnum.col_done.key()){
	           hql.append(" and affair.complete_time is not null ");
	       }
	       //跟踪栏目
	       if(condition.getIsTrack()!= null && condition.getIsTrack()){
	           hql.append(" and affair.track !=:track ");
	           parameter.put("track", 0);
	           hql.append(" and affair.state in (:states) ");
	           List<Integer> stateList = new ArrayList<Integer>();
	           stateList.add(StateEnum.col_sent.key());
	           stateList.add(StateEnum.col_done.key());
	           stateList.add(StateEnum.col_pending.key());
	           parameter.put("states", stateList);
	       } else if(StateEnum.col_done.getKey() == stateEnum.key()){
	       	hql.append(" and affair.state = :state  and  affair.app not in(:notInApp) ");
	           //当查待办和已办affair时先直接设置外面传来的stateEnum
	           parameter.put("state", stateEnum.key());
	           List<Integer> notInApp = new ArrayList<Integer>();
	           notInApp.add(ApplicationCategoryEnum.edocRegister.key());
	           notInApp.add(ApplicationCategoryEnum.edocRecDistribute.getKey());
	           parameter.put("notInApp", notInApp);
	       } else if(StateEnum.col_sent.getKey() == stateEnum.key()){
	       	hql.append(" and affair.state = :state  and  affair.app not in(:notInApp) ");
	           //当查待办和已办affair时先直接设置外面传来的stateEnum
	           parameter.put("state", stateEnum.key());
	           List<Integer> notInApp = new ArrayList<Integer>();
	           notInApp.add(ApplicationCategoryEnum.meeting.key());
	           parameter.put("notInApp", notInApp);
	       }else{
	           hql.append(" and affair.state = :state ");
	           //当查待办和已办affair时先直接设置外面传来的stateEnum
	           parameter.put("state", stateEnum.key());
	       }
	       
	   	hql.append(" and affair.is_delete = :isDelete ");
	   	parameter.put("memberId",memberId);
	   	parameter.put("isDelete", false);
	   	hql.append(" and ");
	   	int len = condition.getApps().size();
	   	if(len>0){
	   		hql.append("(");
	   	}
	   	int count = 0;
	   	for(ApplicationCategoryEnum app : condition.getApps()){
				switch(app){
				case collaboration:
					 count++;
					 if(count>1)
						 hql.append(" or ");
					 hql.append(condition.getSql4ColAgent("affair",parameter));
					 break;
				case info:
					 count++;
					 if(count>1)
						 hql.append(" or ");
					 hql.append("(");
					 hql.append("affair.app = :getHql32ColAgentapp ");
					 parameter.put("getHql32ColAgentapp", ApplicationCategoryEnum.info.key());
					 hql.append(")");
					 break;
				case edoc:
				case meeting:
				case bulletin:
				case news:
				case inquiry:
				case office:
					 count++;
					 if(count>1)
						 hql.append(" or ");
					 hql.append(condition.getSql4AppAgent(app,"affair",parameter));
					break;
				}
			}
	   	if(len>0){
	   		hql.append(")");
	   	}
	   	if(isMember || isAccount || isDepartment || isPost || isPeopleRelate || isTeam){
	   		hql.append(" and ");
	   		hql.append(" ( ");
	   	}
	   	boolean hasCondition  = false;
	   	if(isPeopleRelate){
	   		if(hasCondition){
	   			hql.append(" or ");
	   		}
	   		hql.append(" (pr.relate_member_id = affair.sender_id and pr.relate_type in (:relateType))");
	   		parameter.put("relateType",peopleRelateTypes);
	   		hasCondition = true;
	   	}
	   	if(isMember){
	   		if(hasCondition){
	   			hql.append(" or ");
	   		}
	   		if(memberIds.size()>1){
	   			hql.append(" m.id in (:senderId)" );
	   			parameter.put("senderId", memberIds);
	   		}else{
	   			hql.append(" m.id  = :senderId" );
	   			parameter.put("senderId", memberIds.get(0));
	   		}
	   		hasCondition = true;
	   	}
	   	if(isAccount){
	   		if(hasCondition){
	   			hql.append(" or ");
	   		}
	   		if(accountIds.size()>1){
	   			hql.append(" m.org_account_id in (:accountId) ");
	   			parameter.put("accountId", accountIds);
	   		}else{
	   			hql.append(" m.org_account_id = :accountId ");
	   			parameter.put("accountId", accountIds.get(0));
	   		}
	   		hasCondition = true;
	   	}
	   	if(isDepartment){
	   		if(hasCondition){
	   			hql.append(" or ");
	   		}
	   		if(departmentIds.size()>1){
	   			hql.append(" m.org_department_id in (:orgDepartmentId)");
	   			parameter.put("orgDepartmentId",departmentIds);
	   		}else{
	   			hql.append(" m.org_department_id = :orgDepartmentId");
	   			parameter.put("orgDepartmentId",departmentIds.get(0));
	   		}
	   		hasCondition = true;
	   	}
	   	if(isPost){
	   		if(hasCondition){
	   			hql.append(" or ");
	   		}
	   		if(postIds.size()>1){
	   			hql.append(" m.org_post_id in (:orgPostId) ");
	   			parameter.put("orgPostId", postIds);
	   		}else{
	   			hql.append(" m.org_post_id = :orgPostId ");
	   			parameter.put("orgPostId", postIds.get(0));
	   		}
	
	   		hasCondition = true;
	   	}
	   	
	   	if(isTeam) {
	   		if(hasCondition){
	   			hql.append(" or ");
	   		}
	   		hql.append(" rteam.id in (:orgTeamIds)");
	   		parameter.put("orgTeamIds", teamIds);
	   		hasCondition = true;
	   	}
	   	if(isMember || isAccount || isDepartment || isPost || isPeopleRelate || isTeam){
	   		hql.append(" ) ");
	   	}
	   	String searchHql=condition.getSearchHql(parameter);
	   	if(Strings.isNotBlank(searchHql)){
	   		hql.append(searchHql);
	   	}
	   	StringBuilder sbHql = new StringBuilder();
	   	if (isGroupBy) {
	   	    sbHql.append(" from ctp_affair affair ");
	   	    sbHql.append(hql);
	   	    sbHql.append(" and affair.complete_time in (select max(complete_time) from ctp_affair affair ");
	   	    sbHql.append(hql);
	           sbHql.append(" group by affair.object_id ) ");
	           
	       } else {
	           sbHql.append(hql);
	       }
			return getAffairListBySender(sbHql.toString(), parameter, onlyCount, fi,groupByPropertyName);
		}


   public List<CtpAffair> getAffairsByObjectIdAndStates(FlipInfo flipInfo,Long objectId, List<Integer> states)
		throws BusinessException {
		String hql="select a.id,a.senderId,a.memberId,a.state,nodePolicy from CtpAffair as a where a.objectId= :summaryId and a.state in(:state)  and a.delete =:delete ";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("summaryId",objectId);
		map.put("state", states);
		map.put("delete", false);
		List<Object[]> temp = DBAgent.find(hql, map, flipInfo);
		
		List<CtpAffair> result = new ArrayList<CtpAffair>(temp.size());
        for (Object[] objects : temp) {
            int n = 0;
            
            CtpAffair a = new CtpAffair();
            a.setId((Long)objects[n++]);
            a.setSenderId((Long)objects[n++]);
            a.setMemberId((Long)objects[n++]);
            a.setState((Integer)objects[n++]);
            a.setNodePolicy((String)objects[n++]);
            
            result.add(a);
        }
        
        return result;
	}

   /**
    * 查找分库一个表的数据，用作判断分库是否存数据
    * @return
    */
	public Integer getAffairHis() {
		String hql = "from CtpAffair ";
		return super.count(hql.toString(), null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getDeduplicationAffairList(final String sql,final Map<String, Object> parameter, final boolean onlyCount, final FlipInfo fi,final String orderBySql)
			throws BusinessException {
		CTPHibernateDaoSupport s = (CTPHibernateDaoSupport) AppContext.getThreadContext(GlobalNames.SPRING_HIBERNATE_DAO_SUPPORT);
		
		return (Object) s.getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				boolean isNeedCount = onlyCount || (fi != null && fi.isNeedTotal());
				int count = 0;
				String s="";
				if(isNeedCount){
					s = "select count(distinct affair.id)  count " + sql;
					SQLQuery query = session.createSQLQuery(s);
					setParameter(parameter, query);
					query.addScalar("count", Hibernate.INTEGER);
					Object r = query.uniqueResult();
					count = r == null ? 0 : (Integer) r ;
				}
				if(onlyCount){
					return count;
				}else if(fi != null){
					s = "select distinct affair.* " + sql + orderBySql;
					SQLQuery query = session.createSQLQuery(s);
					setParameter(parameter, query);
					query.addEntity(CtpAffair.class);
					query.setFirstResult(fi.getStartAt());
					query.setMaxResults(fi.getSize());
					List list = query.list();
					fi.setData(list);
					fi.setTotal(count);
					return list;
				}
				
				return null;
			}
		});
	}
	
	@Override
	public void updateFormCollSubject(Long summaryId, String newSubject) throws BusinessException {
		String hql = "update CtpAffair as affair set affair.subject=:newSubject where affair.objectId=:objectId ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("newSubject", newSubject);
		map.put("objectId", summaryId);
		DBAgent.bulkUpdate(hql, map);
	}

	@Override
	public List<Long> getAllAffairIdByAppAndObjectId(ApplicationCategoryEnum appEnum, Long objectId) {
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("objectId", objectId);
		paramsMap.put("app", appEnum.key());
		return DBAgent.find("select id from CtpAffair where objectId=:objectId and app=:app", paramsMap);
	}

	@Override
	public List getAffairDetailsBygorup(Map<String, Object> params)
			throws BusinessException {
		Long objectId = ParamUtil.getLong(params, "objectId");
		String hql = "select count(id),state,subState from CtpAffair where 1=1 and objectId=:objectId ";
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("objectId", objectId);
		if(Strings.isNotBlank(ParamUtil.getString(params, "app"))){
            hql = hql + " and app = :app ";
            paramsMap.put("app", ParamUtil.getInt(params, "app"));
        }
		hql = hql + " group by state,subState";
		return super.find(hql,paramsMap);
	}

	/* (non-Javadoc)
	 * @see com.seeyon.ctp.common.content.affair.AffairDao#getStartAffairStateByObjectId(java.lang.Long)
	 */
	@Override
	public Integer getStartAffairStateByObjectId(Long objectId) throws BusinessException {
		
		List<Integer> states = new ArrayList<Integer>();
		states.add(StateEnum.col_sent.key());
		states.add(StateEnum.col_waitSend.key());
		
		
		String hql="select a.state,a.delete from CtpAffair as a where a.objectId= :summaryId and a.state in(:state) ";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("summaryId",objectId);
		map.put("state", states);
		List<Object[]> list = DBAgent.find(hql, map);
		
	
		CtpAffair senderAffair = null;
		Integer state = null;
		if (Strings.isNotEmpty(list)) {
		    for(Object[] a: list){
		    	//如果有多条数据，并且存在垃圾数据取正常的那条数据
		    	boolean isDelete = a[1] == null ? false :(Boolean)a[1];
		    	if(!isDelete){
		    		state = ((Number)a[0]).intValue();
		    		break;
		    	}
		    }
		    if(state == null){
		    	Object[] b = list.get(0);
		    	state = ((Number)b[0]).intValue();
		    }
		}
		return state;
	}
	
	@Override
	public void updateAffairSummaryState(Long objectId, Integer summaryState) throws BusinessException {
		StringBuffer hql = new StringBuffer();
		hql.append("update CtpAffair as affair set affair.summaryState = :summaryState ");
		hql.append(" where affair.objectId = :objectId ");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("objectId", objectId);
		map.put("summaryState", summaryState);
		DBAgent.bulkUpdate(hql.toString(), map);
	}

	@Override
	public List<CtpAffair> getAffairsForCurrentUsers(FlipInfo flipInfo,
			Map<String, Object> map) throws BusinessException {
		String hql="select a.id,a.senderId,a.memberId,a.state,a.nodePolicy,a.subState from CtpAffair as a where a.objectId= :objectId and a.state in(:state)  and a.delete =:delete and a.nodePolicy <>:nodePolicy ";
		List<Object[]> temp = DBAgent.find(hql, map, flipInfo);
		
		List<CtpAffair> result = new ArrayList<CtpAffair>(temp.size());
        for (Object[] objects : temp) {
            int n = 0;
            
            CtpAffair a = new CtpAffair();
            a.setId((Long)objects[n++]);
            a.setSenderId((Long)objects[n++]);
            a.setMemberId((Long)objects[n++]);
            a.setState((Integer)objects[n++]);
            a.setNodePolicy((String)objects[n++]);
            a.setSubState((Integer)objects[n++]);
            
            result.add(a);
        }
        
        return result;
	}

    @Override
    public Integer countPendingAffairs(Map<String, Object> param) throws BusinessException {
		StringBuilder hql= new StringBuilder();
		Map<String, Object> map = new HashMap<String, Object>();
		
		hql.append("select count(*) from CtpAffair as affair where affair.delete = :delete ");

		map.put("delete", Boolean.FALSE);

		Long memberId = (Long)param.get("memberId");
		if(memberId != null){
			hql.append(" and (affair.memberId = :memberId ");
			map.put("memberId", memberId);
			
			String appStr = (String)param.get("app");
			Integer app = Integer.valueOf(appStr);
			if (appStr != null) {
				//查询我所代理的人(我为代理人，我给别人干活)列表
		        List<AgentModel> agentModelList = MemberAgentBean.getInstance().getAgentModelList(memberId);
		    	int index = 0;
				for (AgentModel agent : agentModelList) {
					boolean hasAgent = false;
					
					if (app.equals(ApplicationCategoryEnum.edoc.getKey())) {
						hasAgent = agent.isHasEdoc();
					} else if (app.equals(ApplicationCategoryEnum.collaboration.getKey())) {
						hasAgent = agent.isHasCol() || agent.isHasTemplate();
					} else if (app.equals(ApplicationCategoryEnum.meeting.getKey())) {
						hasAgent = agent.isHasMeeting();
					}
					if (hasAgent && agent.getStartDate().before(new Date())
							&& agent.getEndDate().after(new Date())) {
						index++;
						
						hql.append(" OR (");
						
						hql.append("affair.memberId=:memId" + index + " AND ").append("affair.receiveTime>=:startDate" + index);
						map.put("memId" + index, agent.getAgentToId());
						map.put("startDate" + index, agent.getStartDate());
						if (agent.isHasCol() && agent.isHasTemplate() && Strings.isEmpty(agent.getAgentDetail())) {
							// 全部协同，不需要templeteId作为条件
						} else {
							boolean c = agent.isHasCol();
							if (c) { // 自由协同
								hql.append(" and (").append("affair.templeteId is null");
							}
							if (agent.isHasTemplate()) {
								hql.append(c ? " OR " : " AND ");
								hql.append("(");
								hql.append(" (").append("affair.templeteId is not null)");
								if (!Strings.isEmpty(agent.getAgentDetail())) {
									List<Long> templateIds = new ArrayList<Long>();
									for (AgentDetailModel agentDetailModel : agent.getAgentDetail()) {
										templateIds.add(agentDetailModel.getEntityId());
									}
									hql.append(" AND (").append("affair.templeteId in (:templeteIds" + index + "))");
									map.put("templeteIds" + index, templateIds);
								}
								hql.append(")");
							}
							if (c) { // 自由协同
								hql.append(")");
							}
						}
						hql.append(")");
					}
				}
				
				hql.append(" ) ");
			}
		
			List<Integer> apps = new ArrayList<Integer>();
			if (app.equals(ApplicationCategoryEnum.edoc.getKey())) {
				apps.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
    			apps.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
    			apps.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
			} else {
				apps.add(app);
			}
			if (apps.size() == 1) {
				hql.append(" and affair.app = :apps ");
			} else {
				hql.append(" and affair.app in (:apps) ");
			}
    		map.put("apps", apps);
    		
			if(app.equals(ApplicationCategoryEnum.meeting.getKey())) {
				List<Integer> states = new ArrayList<Integer>();
				states.add(StateEnum.col_pending.key());
				states.add(StateEnum.mt_attend.key());
				hql.append(" and (affair.state in (:states) ");
				map.put("states", states);
				hql.append(" or ( affair.state = :unAttend and affair.completeTime > :now) ) ");
				map.put("unAttend", StateEnum.mt_unAttend.key());
				Date now = new Date();
				map.put("now", now);
			} else {
	    		hql.append(" and affair.state = :state ");
				map.put("state", StateEnum.col_pending.key());
			}
		}

		List list = DBAgent.find(hql.toString(), map);
		
		if(Strings.isNotEmpty(list) && ((Long)list.get(0))>0) {
			return Integer.valueOf(list.get(0) + "");
		}
 		
		return 0;
    }

	@Override
	public List getAIProcessingCountByMemberId(Date beginTime, Date endTime) {
		String hql= "select memberId,count(*) from CtpAffair where "
				+ "completeTime between :beginDate and :endDate and "
				+ "state = 4 and aiProcessing = 1 "
				+ "group by memberId";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("beginDate", beginTime);
		map.put("endDate", endTime);
		return DBAgent.find(hql,map);
	}
	
	public List<CtpAffair> getAffairListByMemberIdBodyTypeAndState(Long memberId,List<String> bodyTypeList,StateEnum state)
			throws BusinessException {
		String hql="select affair from CtpAffair as affair,CtpTemplate as template where affair.memberId= :memberId and "
				+ "affair.state=:state and affair.templeteId = template.id and template.bodyType in (:bodyTypeList) and template.canAIProcessing = 1";
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("memberId",memberId);
		map.put("state", state.key());
		map.put("bodyTypeList", bodyTypeList);
		return DBAgent.find(hql, map);
	}


	@Override
	public List<CtpAffair> getProcessOverdueAffairs(Date beginTime, Date endTime) throws BusinessException {
		String hql="select affair from CtpAffair as affair,ColSummary as summary "
				+ "where affair.state = 3 and affair.objectId = summary.id and summary.state = 0 and ";
		Map<String, Object> map = new HashMap<String, Object>();
		if(beginTime != null && endTime != null) {
			hql += "summary.deadlineDatetime between :beginDate and :endDate ";
			map.put("beginDate", beginTime);
			map.put("endDate", endTime);
		}else {
			hql += "summary.deadlineDatetime is not null ";
		}
		return DBAgent.find(hql.toString(), map);
	}

	@Override
	public List<CtpAffair> getNodeOverdueAffairs(Date beginTime, Date endTime) throws BusinessException {
		String hql="from CtpAffair where state = 3 and ";
		Map<String, Object> map = new HashMap<String, Object>();
		if(beginTime != null && endTime != null) {
			hql += "expectedProcessTime between :beginDate and :endDate and state = 3";
			map.put("beginDate", beginTime);
			map.put("endDate", endTime);
		}else {
			hql += "expectedProcessTime is not null ";
		}
		return DBAgent.find(hql.toString(), map);
	}

	@Override
	public List<CtpAffair> getAffairsByAppAndReceivetimeAndState(ApplicationCategoryEnum appEnum,Date beginTime, Date endTime,StateEnum stateEnum) throws BusinessException {
		String hql="from CtpAffair where app = :app and state = :state and ";
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("app", appEnum.getKey());
		map.put("state", stateEnum.getKey());
		if(beginTime != null && endTime != null) {
			hql += "receiveTime between :beginDate and :endDate ";
			map.put("beginDate", beginTime);
			map.put("endDate", endTime);
		}else {
			hql += "receiveTime is not null ";
		}
		return DBAgent.find(hql.toString(), map);
	}

	@Override
	public void updateSortWeight(int sortWeight, List<Long> affairIdList) throws BusinessException {
		String hql = "update CtpAffair as affair set affair.sortWeight= :sortWeight where affair.id in (:ids) ";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ids", affairIdList);
		map.put("sortWeight", sortWeight);
		DBAgent.bulkUpdate(hql, map);
	}
	
	/**
	 * 根据节点权限获取affairs
	 * add by shenwei
	 * 20200724
	 * @param a
	 * @return
	 */
	@Override
	public List<CtpAffair> getAffairsByNodePolicy(String nodePolicy) throws BusinessException 
	{
		String hql="from CtpAffair where nodePolicy = :nodePolicy ";
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nodePolicy", nodePolicy);
		
		return DBAgent.find(hql.toString(), map);
	}
	
	
}
