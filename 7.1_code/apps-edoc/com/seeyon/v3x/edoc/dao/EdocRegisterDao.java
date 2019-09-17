package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum.RegisterState;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;

/**
 * 
 * @author Administrator
 * 
 */
public class EdocRegisterDao extends BaseHibernateDao<EdocRegister> {

	/**
	 * 登记公文
	 * 
	 * @param edocRegister
	 *            登记对象
	 * @return boolean 登记是否成功
	 */
	public void create(EdocRegister edocRegister) {
		super.save(edocRegister);
	}

	/**
	 * 批量登记公文
	 * 
	 * @param EDOCREGISTER
	 * @return
	 */
	public void create(List<EdocRegister> list) {
       DBAgent.saveAll(list);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public EdocRegister getEdocRegister(long id) {
		return super.get(id);
	}

	/**
	 * 重构登记列表查询方法
	 * @param type
	 * @param condition
	 * @return
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	public List<EdocRegister> findEdocRegisterList(int state, Map<String, Object> condition) throws BusinessException {
		int registerType = (Integer)condition.get("registerType");
		Long userId = (Long)condition.get("userId");
		Long orgAccountId = (Long)condition.get("orgAccountId");
		Long memberId = (Long)condition.get("memberId");
		String conditionKey = (String)condition.get("conditionKey");
		String textfield = (String)condition.get("textfield");
		String textfield1 = (String)condition.get("textfield1");
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder hsql = new StringBuilder(" select e from EdocRegister e ");
		//当是退件箱列表时，需要根据退回时间排序,需要关联affair表
		if(state == EdocNavigationEnum.RegisterState.retreat.ordinal()){
			 hsql.append(" ,CtpAffair affair where ((e.edocId = affair.objectId and e.recieveId=affair.subObjectId) or (e.id=affair.objectId)) and affair.delete=false and affair.app=:app and affair.memberId=:memberId and affair.state=:affairState");
			 parameterMap.put("app", 24);
			 parameterMap.put("affairState", StateEnum.col_pending.key());
		} else {
			hsql.append(" where 1=1");
		}
		
		hsql.append(" and ( e.registerUserId = :memberId or e.registerUserId = 0) and e.orgAccountId = :orgAccountId ");
		parameterMap.put("memberId", memberId);
		parameterMap.put("orgAccountId", orgAccountId);
		if(state != -1) {
			hsql.append(" and e.state = :state");
			parameterMap.put("state", state);
		}
		
		if(registerType != EdocNavigationEnum.RegisterType.All.ordinal()) {
			hsql.append(" and e.registerType = :registerType");
			parameterMap.put("registerType", registerType);
		}
		
		if (Strings.isNotBlank(conditionKey)) {
			if("recTime".equals(conditionKey)) {//签收时间
				if (Strings.isNotBlank(textfield)) {
	   				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
	   				String paramName = "timestamp1";
	   				hsql.append(" and e.").append(conditionKey).append(" >= :").append(paramName);
	   				parameterMap.put(paramName, stamp);
	   			}
				if (Strings.isNotBlank(textfield1)) {
	   				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
	   				String paramName = "timestamp2";
	   				hsql.append(" and e.").append(conditionKey).append(" <= :").append(paramName);
	   				parameterMap.put(paramName, stamp);
	   			}
			} else if("registerDate".equals(conditionKey)){//登记时间
				if (Strings.isNotBlank(textfield)) {
	   				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
	   				String paramName = "timestamp1";
	   				hsql.append(" and e.").append(conditionKey).append(" >= :").append(paramName);
	   				parameterMap.put(paramName, stamp);
	   			}
				if (Strings.isNotBlank(textfield1)) {
	   				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
	   				String paramName = "timestamp2";
	   				hsql.append(" and e.").append(conditionKey).append(" <= :").append(paramName);
	   				parameterMap.put(paramName, stamp);
	   			}
			} else if("secretLevel".equals(conditionKey) && Strings.isNotBlank(textfield)){//密级
                hsql.append(" and e.secretLevel = :secretLevel");
                parameterMap.put("secretLevel", textfield);
			} else {
				if (Strings.isNotBlank(textfield)) {//标题
					hsql.append(" and e.").append(conditionKey).append(" like '%").append(textfield).append("%'");
				}
			}			
   		}
		
		//当是退件箱列表时，需要根据退回时间排序
		if(state == EdocNavigationEnum.RegisterState.retreat.ordinal()){
			 hsql.append(" order by affair.createDate desc ");
		} else {
			 hsql.append(" order by e.createTime desc ");
		}
		
		List<EdocRegister> result = (List<EdocRegister>)super.find(hsql.toString(), parameterMap);
		
		List<EdocRegister> list = new ArrayList<EdocRegister>();
		if(result!=null && result.size()>0) {
			for(EdocRegister edocRegister : result) {
				if(edocRegister.getState() == RegisterState.Registed.ordinal() && edocRegister.getRegisterType()==1) {//电子登记，已登记
					CtpAffair affair = EdocHelper.getAffairByEdocRegisterId(edocRegister);
					if(affair != null) {
						if(edocRegister.getRegisterUserId() != null && !edocRegister.getRegisterUserId().equals(userId)) {//代理人列表
							if(affair.getTransactorId()!=null && affair.getTransactorId().longValue()==userId) {//代理人处理
								edocRegister.setProxyLabel("("+ResourceUtil.getString("edoc.proxy")+ EdocHelper.getMemberById(edocRegister.getRegisterUserId()).getName()+")");
							} else {
								//  显示xxx处理
								edocRegister.setProxyLabel("("+ResourceUtil.getString("edoc.proxy.deal", EdocHelper.getMemberById(edocRegister.getRegisterUserId()).getName())+")");
							}
						} else {//当事人列表查看
							if(affair.getTransactorId()!=null && affair.getTransactorId().longValue()!=userId) {//代理人处理    显示xxx代理
								edocRegister.setProxyLabel("("+ResourceUtil.getString("edoc.proxyDeal", EdocHelper.getMemberById(affair.getTransactorId()).getName())+")");
							}
						}
					}					
				}
				list.add(edocRegister);
			}
		}
		return list;
	}
	
	/**
	 * 查询登记信息
	 * 
	 * @param orgAccountId 单位id
	 * @param state 登记状态
	 * @param registerType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<EdocRegister> findList(String registerUserIds, int state, int registerType, String condition, String[] value) {
		StringBuilder hsql = new StringBuilder(" select e from EdocRegister e ");
		//当是退件箱列表时，需要根据退回时间排序,需要关联affair表
		if(state == EdocNavigationEnum.RegisterState.retreat.ordinal()){
			 hsql.append(" ,Affair affair ");
		}
		
		hsql.append(" where e.state = " + state);
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();

		 /*****************puyc 代理*******************/
        //获取代理相关信息
  		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(Long.parseLong(registerUserIds));//代理人
      	List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(Long.parseLong(registerUserIds));//被代理人
  		List<AgentModel> agentModelList = null;
  		boolean agentToFlag = false;//被代理标记
  		boolean agentFlag = false;//代理标记
  		if(_agentModelList != null && !_agentModelList.isEmpty()){
  			agentModelList = _agentModelList;
  			agentFlag = true;
  		}else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
  			agentModelList = _agentModelToList;
  			agentToFlag = true;
  		}
  		//Map<Integer, AgentModel> agentModelMap = new HashMap<Integer, AgentModel>();
  		List<AgentModel> edocAgent = new ArrayList<AgentModel>();
  		
  		if(agentModelList != null && !agentModelList.isEmpty()){//代理有值
  			java.util.Date now = new java.util.Date();
  			for(AgentModel agentModel : agentModelList){
    			if(agentModel.isHasEdoc()){
    				if(agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now))
    					edocAgent.add(agentModel);
    			}
  	    	}
  		}
  		if(edocAgent.isEmpty()){ 
  		    agentFlag = false;
  		    agentToFlag = false;
  		}
  		
          
          /*****************puyc 代理 结束*******************/
		
		
		//发起人
		if(condition!=null && "createPerson".equals(condition)) {
			if(value[0]!=null && !"".equals(value[0])) {
				hsql = new StringBuilder("select e from EdocSummary a ,EdocRegister e where a.id = e.distributeEdocId and e.state = ")
				          .append(state)
				          .append(" and a.createPerson like '%").append(value[0]).append("%'");
			}
		}
		//发起时间 
		else if(condition!=null && "createDate".equals(condition)) {
			String where = "e.createTime";
			if(registerType == 1){
				hsql = new StringBuilder("select e from EdocSummary a ,EdocRegister e where a.id = e.distributeEdocId and e.state = ").append(state);
				where = "a.createTime";
			}
			if (value[0] != null && !"".equals(value[0])) {
   				java.util.Date stamp = Datetimes.getTodayFirstTime(value[0]);
   				String paramName = "timestamp1";
   				hsql.append(" and ").append(where).append(" >= :").append(paramName);
   				parameterMap.put(paramName, stamp);
   			}
   			if (value[1] != null && !"".equals(value[1])) {
   				java.util.Date stamp = Datetimes.getTodayLastTime(value[1]);
   				String paramName = "timestamp2";
   				hsql.append(" and ").append(where).append(" <= :").append(paramName);
   				parameterMap.put(paramName, stamp);
   			}
		}
		
		else if(condition!=null && "recTime".equals(condition)) {
			if(value[0]!=null && !"".equals(value[0])) {
				hsql.append(" and a.").append(condition).append(" >= '").append(value[0]).append("'");
			} 
			if(value[1]!=null && !"".equals(value[1])) {
				hsql.append(" and a.").append(condition).append(" <= '").append(value[1]).append("'");
			}
		}
		//签收时间  
		else if (condition != null && "recieveDate".equals(condition)){
			if (value[0] != null && !"".equals(value[0])) {
   				java.util.Date stamp = Datetimes.getTodayFirstTime(value[0]);
   				String paramName = "timestamp1";
   				hsql.append(" and e.recTime >= :").append(paramName);
   				parameterMap.put(paramName, stamp);
   			}
   			if (value[1] != null && !"".equals(value[1])) {
   				java.util.Date stamp = Datetimes.getTodayLastTime(value[1]);
   				String paramName = "timestamp2";
   				hsql.append(" and e.recTime <= :").append(paramName);
   				parameterMap.put(paramName, stamp);
   			}
   		}
		//登记时间
		else if (condition != null && "registerDate".equals(condition)){
			if (value[0] != null && !"".equals(value[0])) {
   				java.util.Date stamp = Datetimes.getTodayFirstTime(value[0]);
   				String paramName = "timestamp1";
   				hsql.append(" and e.registerDate >= :").append(paramName);
   				parameterMap.put(paramName, stamp);
   			}
   			if (value[1] != null && !"".equals(value[1])) {
   				java.util.Date stamp = Datetimes.getTodayLastTime(value[1]);
   				String paramName = "timestamp2";
   				hsql.append(" and e.registerDate <= :").append(paramName);
   				parameterMap.put(paramName, stamp);
   			}
		} 
		else if (null != condition && !"".equals(condition) && null != value[0] && !"".equals(value[0])) {
			hsql.append(" and e.").append(condition).append(" like '%").append(value[0]).append("%'");
		}
		if (registerType != EdocNavigationEnum.RegisterType.All.ordinal()) {
			hsql.append(" and e.registerType=").append(registerType);
		}
		
		/********puyc 代理**********/
		
		//草稿箱不做代理
		if(Strings.isNotBlank(registerUserIds)){
	          if(!edocAgent.isEmpty() && state != EdocNavigationEnum.RegisterState.DraftBox.ordinal()){//代理
				if (!agentToFlag) {
					hsql.append(" and (")
					    .append(" e.registerUserId in(")
					    .append(registerUserIds)
					    .append(")");
					for (AgentModel agent : edocAgent) {
						hsql.append(" or ")
						    .append(" e.registerUserId in(")
						    .append(agent.getAgentToId())
						    .append(")");
					}
					 hsql.append(")");
				}else{
					 hsql.append(" and e.registerUserId in (")
					     .append(registerUserIds)
					     .append(")");
				}
					
	        }else{
	        	 hsql.append(" and e.registerUserId in (")
	        	     .append(registerUserIds)
	        	     .append(")");
	        }
	     }
		
		
		
		 //当是退件箱列表时，需要根据退回时间排序
		 if(state == EdocNavigationEnum.RegisterState.retreat.ordinal()){
			 hsql.append(" and e.edocId = affair.objectId  and affair.state = :affairState and affair.memberId = :memberId ")
			     .append(" and affair.app = :app ").append(" and affair.subApp = :subApp ")
			     .append(" order by affair.createDate desc ");
			 parameterMap.put("affairState", 3);
			 parameterMap.put("memberId", Long.parseLong(registerUserIds));
			 parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
			 parameterMap.put("subApp", ApplicationSubCategoryEnum.old_edocRegister.getKey());
			 
		 }else{
			 hsql.append(" order by e.updateTime desc");
		 }
		 
		 List<EdocRegister> list= super.find(hsql.toString(), parameterMap); 
		 
		//草稿箱不做代理
		if(state != EdocNavigationEnum.RegisterState.DraftBox.ordinal()){
			for(int i=0;i<list.size();i++){
		    	   EdocRegister register = list.get(i);
		    	   if(agentFlag && !AppContext.getCurrentUser().getId().equals(register.getRegisterUserId())){
		    		   Long proxyMemberId = register.getRegisterUserId();//被代理的userId
		    		   
		    		   //多个人都设置我为代理人
		    		   for(AgentModel agentModel : edocAgent){
		    			   //判断当前的是 谁让我代理的
			    		   if(agentModel.getAgentToId().longValue() == proxyMemberId.longValue()){
			    			   //已登记时间在在代理时间之内的，才设置为代理，代理人在查看列表时才显示为蓝色的
			    			   if(agentModel.getStartDate().before(register.getRegisterDate()) &&
			    					   agentModel.getEndDate().after(register.getRegisterDate())){
			    				   register.setProxyUserId(proxyMemberId);//被代理的userUser
			    				   register.setProxy(true);
			    			   }
			    			   break;
			    		   }
			    	   }
		    	   }
		    	   else if(agentToFlag){
		    		   register.setProxy(true);
		    	   }
		       }
		}
	         
	        // 代理结束
	       return list;
	       
		/***********代理结束****************/
	/*	if (registerUserIds != null && !"".equals(registerUserIds)) {
			hsql += " and e.registerUserId in (";
			hsql += registerUserIds;
			hsql += ")";
		}
		hsql += " order by e.registerDate desc";
		return super.find(hsql,parameterMap);
		*/
		
	}

	/**
	 * 
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<EdocRegister> findList(String[] ids) {
		if(ids == null || ids.length==0)
			return null;
		List<Long> list = new ArrayList<Long>(ids.length);
		for(String id:ids) {
			list.add(Long.parseLong(id));
		}
		String hsql = "from EdocRegister where id in (:ids)";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("ids", list);
		return super.find(hsql, param);
	}

	/**
	 * 在分发的时候,修改一些数据(状态,分发人信息等)
	 * 
	 * @author lijl
	 * @param edocRegister
	 *            根据id查询出的EdocRegister对象
	 */
	public void saveOrUpdate(EdocRegister edocRegister) {
		/*org.hibernate.Session s = super.getSession();
		s.setFlushMode(FlushMode.AUTO);
		s.update(edocRegister);
		s.flush();*/
		super.update(edocRegister);
	}

	public void updateEdocRegisterState(Long registerId, int state) {
		String hsql = "update EdocRegister set state=:state where id=:registerId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("state", state);
		params.put("registerId", registerId);
		super.bulkUpdate(hsql, params);
	}
	
	public void updateEdocRegisterState(Long[] registerId, int state) {
		String hsql = "update EdocRegister set state=:state where id in (:registerId)";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("state", state);
		params.put("registerId", registerId);
		super.bulkUpdate(hsql, params);
	}	
	
	/**
	 * 删除登记（电子版假删，手工草稿真删）
	 * 
	 * @param ids
	 */
	public void deleteEdocRegister(EdocRegister edocRegister) {
		DBAgent.delete(edocRegister);
		DBAgent.commit();
	}
	
	 /**
	 * 获取已登记的收文
	 * @author lijl
	 * @param state 状态:0草稿 1已登记
	 * @param currentUser 当前用户
	 * @return List
	 */
	public List<EdocRegister> findRegisterByState(String condition, String[] values,int state,User user){
		
		long userId=0;
		long departmentId=0;
		if(user!=null){
			userId=user.getId();
			departmentId=user.getLoginAccount();
		}
		
		 /*****************puyc 代理*******************/
        //获取代理相关信息
  		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(userId);//代理人
      	List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(userId);//被代理人
  		List<AgentModel> agentModelList = null;
  		boolean agentToFlag = false;//被代理标记
  		boolean agentFlag = false;//代理标记
  		if(_agentModelList != null && !_agentModelList.isEmpty()){
  			agentModelList = _agentModelList;
  			agentFlag = true;
  		}else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
  			agentModelList = _agentModelToList;
  			agentToFlag = true;
  		}
  		//Map<Integer, AgentModel> agentModelMap = new HashMap<Integer, AgentModel>();
  		List<AgentModel> edocAgent = new ArrayList<AgentModel>();
  		if(agentModelList != null && !agentModelList.isEmpty()){//代理有值
  			java.util.Date now = new java.util.Date();
  	    	for(AgentModel agentModel : agentModelList){
    			if(agentModel.isHasEdoc()){
    				if(agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now))
    					edocAgent.add(agentModel);
    			}
  	    	}
  		}
  		if(edocAgent == null || edocAgent.isEmpty()){ 
  		    agentFlag = false;
  		    agentToFlag = false;
  		}
  		
          
          /*****************puyc 代理 结束*******************/
		
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		String hql = "select register from EdocRegister as register where " +
				" register.state=:state and register.distributeState="+EdocNavigationEnum.EdocDistributeState.WaitDistribute.ordinal()+
				" and register.orgAccountId=:orgAccountId ";
		parameterMap.put("state", state);
		parameterMap.put("orgAccountId", departmentId);
		
		if(!edocAgent.isEmpty()){//代理
			if (!agentToFlag) {//被代理==false，代理=true
				hql += "and (";
				hql +=" register.distributerId=:distributerId ";
				parameterMap.put("distributerId", userId);
				
					hql += "   or ";
					int i = 0;
					for(AgentModel agent : edocAgent){
						if(i != 0){
							hql +=" or ";
						}
						hql += " ( register.distributerId=:edocAgentToId"+i+" and register.updateTime>=:proxyCreateDate"+i;
						parameterMap.put("edocAgentToId"+i, agent.getAgentToId());
						parameterMap.put("proxyCreateDate"+i, agent.getStartDate());
						hql +=" )";
						i++;
					}
					hql +=" )";
			}else{
				hql +=" and register.distributerId=:distributerId ";
				parameterMap.put("distributerId", userId);
			}
		}else{
			hql +=" and register.distributerId=:distributerId ";
			parameterMap.put("distributerId", userId);
		}
		
		if(Strings.isNotBlank(condition)){
		    /**
	         * 收文管理  -- 成文单位
	         */ 
	        if ("edocUnit".equals(condition) && StringUtils.isNotBlank(values[0])) {
	            String paramName = "edocUnit";
	            hql += " and register.edocUnit like :" + paramName + " ";
	            String paramValue = "%" +  values[0] + "%";
	            parameterMap.put(paramName, paramValue);
	        }
	        
	        /**
	         * 收文管理  -- 签收时间
	         */
	        else if (("recieveDate".equals(condition)|| "recTime".equals(condition))) {
	            if (StringUtils.isNotBlank(values[0])) {
	                java.util.Date stamp = Datetimes.getTodayFirstTime(values[0]);
	                String paramName = "timestamp1";
	                hql +=  " and register.recTime >= :" + paramName;
	                parameterMap.put(paramName, stamp);
	            }
	            if (StringUtils.isNotBlank(values[1])) {
	                java.util.Date stamp = Datetimes.getTodayLastTime(values[1]);
	                String paramName = "timestamp2";
	                hql += " and register.recTime <= :" + paramName;
	                parameterMap.put(paramName, stamp);
	            }
	        }
	        /**
	         * 收文管理  -- 登记时间
	         */
	        else if ("registerDate".equals(condition)) {
	            if (StringUtils.isNotBlank(values[0])) {
	                java.util.Date stamp = Datetimes.getTodayFirstTime(values[0]);
	                String paramName = "timestamp1";
	                hql += " and register.registerDate >= :" + paramName;
	                parameterMap.put(paramName, stamp);
	            }
	            if (StringUtils.isNotBlank(values[1])) {
	                java.util.Date stamp = Datetimes.getTodayLastTime(values[1]);
	                String paramName = "timestamp2";
	                hql += " and register.registerDate <= :" + paramName;
	                parameterMap.put(paramName, stamp);
	            }
	        }
	        /**
	         * 发文时间
	         */ 
	        else if ("createDate".equals(condition)) {
	            if (StringUtils.isNotBlank(values[0])) {
	                java.util.Date stamp = Datetimes.getTodayFirstTime(values[0]);
	                String paramName = "timestamp1";
	                hql += " and register.createTime >= :" + paramName;
	                parameterMap.put(paramName, stamp);
	            }
	            if (StringUtils.isNotBlank(values[1])) {
	                java.util.Date stamp = Datetimes.getTodayLastTime(values[1]);
	                String paramName = "timestamp2";
	                hql += " and register.createTime <= :" + paramName;
	                parameterMap.put(paramName, stamp);
	            }
	        }
	        /**
	         * 发文人
	         */ 
	        else if ("startMemberName".equals(condition)) {
	            String paramName = "createUserName";
	            hql += " and register.createUserName like :" + paramName + " ";
	            String paramValue = "%" +  values[0] + "%";
	            parameterMap.put(paramName, paramValue);
	        }   
	        /**
	         * 标题
	         */ 
	        else if ("subject".equals(condition)) {
	            String paramName = "subject";
	            hql += " and register.subject like :" + paramName + " ";
	            String paramValue = "%" +  values[0] + "%";
	            parameterMap.put(paramName, paramValue);
	        }
	        /**
	         * 来文字号
	         */ 
	        else if ("docMark".equals(condition)) {
	            String paramName = "docMark";
	            hql += " and register.docMark like :" + paramName + " ";
	            String paramValue = "%" +  values[0] + "%";
	            parameterMap.put(paramName, paramValue);
	        }
	        /**
	         * 内部文号
	         */ 
	        else if ("docInMark".equals(condition)) {
	            String paramName = "serialNo";
	            hql += " and register.serialNo like :" + paramName + " ";
	            String paramValue = "%" +  values[0] + "%";
	            parameterMap.put(paramName, paramValue);
	        }
	        /**
	         * 文件秘级 
	         */
	        else if("secretLevel".equals(condition) && Strings.isNotBlank(values[0])) {
                String paramName = "secretLevel";
                hql += " and register.secretLevel = :secretLevel";
                parameterMap.put(paramName, values[0]);
	        }
	        /**
	         * 交换方式
	         */
	        else if("exchangeMode".equals(condition) && Strings.isNotBlank(values[0])){
	        	String paramName = "exchangeMode";
                hql += " and register.exchangeMode = :exchangeMode";
                parameterMap.put(paramName, Integer.valueOf(values[0]));
	        }
		}
		
        if("2".equals(EdocSwitchHelper.isOpenRegister())) {
        	hql += " order by register.createTime desc ";
        }else{
        	hql += " order by register.recTime desc ";
        }
        
        @SuppressWarnings("unchecked")
        List<EdocRegister> list = super.find(hql,parameterMap);
        for(int i=0;i<list.size();i++){
        	EdocRegister register = list.get(i);
        	/*CtpAffair affair = null;
			try {
				affair = EdocHelper.getDistributeAffair(register.getId());
			} catch (BusinessException e) {
			   log.error("", e);
			}*/	
			if(agentFlag && !AppContext.getCurrentUser().getId().equals(register.getDistributerId())){
      		   Long proxyMemberId = register.getDistributerId();//被代理的userId
      		   register.setProxyUserId(proxyMemberId);//被代理的userUser
      		   register.setProxy(true);
      		   
      	   	}
      	   	//当事人列表
      	   	else if(agentToFlag){
      	   		//register.setProxy(true);
      	   	}
        }  
        return list;
	}
	/**
	 * 根据ID获取EdocRegister对象
	 * @author lijl
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
	public EdocRegister findRegisterById(long id){
		EdocRegister edocRegister=null;
		String hql="from EdocRegister as register where register.id=?";
		
		@SuppressWarnings("unchecked")
        List<EdocRegister> ls = super.findVarargs(hql,id);
		if(ls!=null && ls.size()>0){
			edocRegister=ls.get(0);
		}
		return edocRegister;
	}
	/**
	 * 根据distribute_edoc_id获取EdocRegister对象
	 * @author lijl
	 * @param id Register对象Id
	 * @return EdocRegister对象
	 */
	public EdocRegister findRegisterByDistributeEdocId(long id){
		EdocRegister edocRegister=null;
		String hql="from EdocRegister as register where register.distributeEdocId=?";
		
		@SuppressWarnings("unchecked")
        List<EdocRegister> ls = super.findVarargs(hql,id);
		
		if(ls!=null && ls.size()>0){
			edocRegister=ls.get(0);
		}
		return edocRegister;
	}
	
	public List<EdocRegister> findRegister(long id){
		String hql="from EdocRegister as register where register.distributeEdocId=?";
		
		@SuppressWarnings("unchecked")
        List<EdocRegister> ls = super.findVarargs(hql,id);
		return ls;
	}
	
	/**
	 * 在分发的时候,修改一些数据(状态,分发人信息等)
	 * @author lijl
	 * @param edocRegister 根据id查询出的EdocRegister对象
	 */
	public void update(EdocRegister edocRegister)
	{
		super.getHibernateTemplate().update(edocRegister);
	}
	
	public EdocRegister findRegisterByRecieveId(long recieveId) {
		EdocRegister edocRegister=null;
		String hql="from EdocRegister as register where register.recieveId=?";
		
		@SuppressWarnings("unchecked")
        List<EdocRegister> ls = super.findVarargs(hql,recieveId);
		
		if(ls!=null && ls.size()>0){
			edocRegister=ls.get(0);
		}
		return edocRegister;
	}
	
	/**
     * 某单位下是否有登记待发数据(用于登记开关关闭时的 前提条件判断，当有登记待发数据时，不能关闭)
     * @param accountId
     * @return
     */
	public List<EdocRegister> isHasWaitRegistersByAccountId(long accountId){
		String hql = " from EdocRegister where orgAccountId = :orgAccountId and state=:state";
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("orgAccountId", accountId);
		parameterMap.put("state", 0);
		
		@SuppressWarnings("unchecked")
        List<EdocRegister> list = super.find(hql,parameterMap);
		
		return list;
	}
	
	public int findWaitRegisterCountByAccountId(Long accountId) {
		String hql = " from EdocRegister where orgAccountId = :orgAccountId and state=:state";
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("orgAccountId", accountId);
		parameterMap.put("state", 0);
		int count = super.count(hql,parameterMap);
		return count;
	}
	
	
	/**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param registerId  公文ID
	 * @param serialNo   内部文号
	 * @param loginAccount  登录单位
	 * @return (1：存在  0：不存在)
	 */
	public int checkSerialNoExsit(String registerId,String serialNo,Long orgAccountId){
		if(serialNo==null||"".equals(serialNo))return 0;//为空，或者表单中没有这个字段。
		
		StringBuilder sb=new StringBuilder();
		sb.append("from EdocRegister as summary where ");
		if(registerId!=null && !"".equals(registerId)){
			sb.append(" summary.id!=? and ");
		}
		sb.append(" summary.serialNo = ?  and summary.orgAccountId=? ");
		int count =0;
		
		if(registerId!=null && !"".equals(registerId)){
			
			Object[] values3 = {Long.parseLong(registerId),SQLWildcardUtil.escape(serialNo),orgAccountId};
			count = super.getQueryCount(sb.toString(),values3, new Type[]{Hibernate.LONG,Hibernate.STRING,Hibernate.LONG});
		}else {
			Object[] values2 = {SQLWildcardUtil.escape(serialNo),orgAccountId};
			count = super.getQueryCount(sb.toString(),values2, new Type[]{Hibernate.STRING,Hibernate.LONG});
		}
		if(count>0) {
			return 1;    //找到
		}else{			 //没找到
			return 0;	
		}
	}
	
	
}
