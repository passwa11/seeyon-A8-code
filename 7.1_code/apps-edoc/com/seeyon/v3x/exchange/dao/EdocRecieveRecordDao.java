package com.seeyon.v3x.exchange.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.util.DateUtil;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;

@SuppressWarnings("unchecked")
public class EdocRecieveRecordDao extends BaseHibernateDao<EdocRecieveRecord> {

    public List<EdocRecieveRecord> getEdocRecieveRecords(int status) {
        String hsql = "from EdocRecieveRecord as a where a.status=? order by a.createTime";
        return super.findVarargs(hsql, status); 
    }

    public List<EdocRecieveRecord> getToRegisterEdocs(long userId) {
        String hsql = "from EdocRecieveRecord as a where a.registerUserId=? and a.status=? order by a.createTime DESC";
        Object[] values = {userId, EdocRecieveRecord.Exchange_iStatus_Recieved};
        return super.find(hsql, -1, -1, null, values);
    }

    public List<EdocRecieveRecord> getWaitRegiterEdoc(String registerIds, int state, int registerType, String condition, String[] value) {
        
        StringBuilder hsql = new StringBuilder("from EdocRecieveRecord as a where a.status = ").append(state);
        Map parameterMap = new HashMap();
        
        /*****************puyc 代理*******************/
        //获取代理相关信息
  		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(Long.parseLong(registerIds));//代理人
      	List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(Long.parseLong(registerIds));//被代理人
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
    			if(agentModel.isHasEdoc() && agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now)){
    				edocAgent.add(agentModel);
    			}
	    	}
  		}
      	boolean isProxy = false;
  		if(edocAgent != null && !edocAgent.isEmpty()){
  			isProxy = true;//代理
  		}else{
  			agentFlag = false;
  			agentToFlag = false;
  		}
  		
          
          /*****************puyc 代理 结束*******************/
        // 成文单位
        if(condition != null && "edocUnit".equals(condition)) {
            hsql.append(" and a.sendUnit like '%").append(value[0]).append("%'");
        }
        // 发起人
        else if(condition != null && "createPerson".equals(condition)) {
            if(value[0] != null && !"".equals(value[0])) {
                hsql = new StringBuilder("select a from EdocSummary e ,EdocRecieveRecord a where e.id = a.edocId and a.status = ")
                    .append(state)
                    .append(" and e.createPerson like '%")
                    .append(value[0])
                    .append("%'");
            }
        }
        // 发起时间
        else if(condition != null && "createDate".equals(condition)) {
            hsql = new StringBuilder("select a from EdocSummary e ,EdocRecieveRecord a where e.id = a.edocId and a.status = ").append(state);
            if(value[0] != null && !"".equals(value[0])) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(value[0]);
                String paramName = "timestamp1";
                hsql.append(" and e.createTime >= :").append(paramName);
                parameterMap.put(paramName, stamp);
            }
            if(value[1] != null && !"".equals(value[1])) {
                java.util.Date stamp = Datetimes.getTodayLastTime(value[1]);
                String paramName = "timestamp2";
                hsql.append(" and e.createTime <= :").append(paramName);
                parameterMap.put(paramName, stamp);
            }
        }
        // 签收时间
        else if(condition != null && "recieveDate".equals(condition)) {
            if(value[0] != null && !"".equals(value[0])) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(value[0]);
                String paramName = "timestamp1";
                hsql.append(" and a.recTime >= :").append(paramName);
                parameterMap.put(paramName, stamp);
            }
            if(value[1] != null && !"".equals(value[1])) {
                java.util.Date stamp = Datetimes.getTodayLastTime(value[1]);
                String paramName = "timestamp2";
                hsql.append(" and a.recTime <= :").append(paramName);
                parameterMap.put(paramName, stamp);
            }
        } else if(null != condition && !"".equals(condition) && null != value[0] && !"".equals(value[0])) {
            hsql.append(" and a.").append(condition).append(" like '%").append(value[0]).append("%'");
        }
        //puyc 代理
        if(registerIds != null && !"".equals(registerIds)) {
          if(edocAgent != null && !edocAgent.isEmpty()){//代理
			if (!agentToFlag) {
				hsql.append(" and (").append(" a.registerUserId in(").append(registerIds).append(")");
				for(AgentModel agent : edocAgent){
					hsql.append(" or ").append(" a.registerUserId in(").append(agent.getAgentToId()).append(")");
				}
				 hsql.append(")");
			}else{
				 hsql.append(" and a.registerUserId in (").append(registerIds).append(")");
			}
				
        }else{
        	 hsql.append(" and a.registerUserId in (").append(registerIds).append(")");
        }
     }
        
       hsql.append(" order by a.recTime desc"); 
 
       List<EdocRecieveRecord> edocRecieveRecordlist= super.find(hsql.toString(), parameterMap);
       
       for(int i=0;i<edocRecieveRecordlist.size();i++){
    	   if(agentFlag && edocRecieveRecordlist.get(i).getRegisterUserId() !=  AppContext.getCurrentUser().getId()){
    		   Long proxyMemberId = edocRecieveRecordlist.get(i).getRegisterUserId();//被代理的userId
    		   edocRecieveRecordlist.get(i).setProxyUserId(proxyMemberId);//被代理的userUser,在代理人页面，显示被代理人的名字
    		   edocRecieveRecordlist.get(i).setProxy(true);
    	   }
    	   else if(agentToFlag){
    		   edocRecieveRecordlist.get(i).setProxy(true);
    	   }
    	   
       }  
        // 代理结束
       return edocRecieveRecordlist;
    }
    
    /**
     * 待登记列表
     * @param status
     * @param condition
     * @return
     */
    public List<EdocRecieveRecord> findWaitEdocRegisterList(int state, Map<String, Object> condition) {
    	String conditionKey = (String)condition.get("conditionKey");
    	String textfield = (String)condition.get("textfield");
    	String textfield1 = (String)condition.get("textfield1");
    	//Long userId = condition.get("userId")==null?null:(Long)condition.get("userId");
    	Long memberId = condition.get("memberId")==null?null:(Long)condition.get("memberId");
    	StringBuilder hsql = new StringBuilder();
    	Map<String, Object> parameterMap = new HashMap<String, Object>();
    	
    	
    	//如果是V5-G6版本，待登记列表数据包括 待登记数据和待分发退回数据
    	if(EdocHelper.isG6Version()){
    		hsql.append("select a from EdocRecieveRecord as a,CtpAffair af ");
    	}else{
    		hsql.append("select a from EdocRecieveRecord as a");
    	}
    	
    	
    	//按状态查询
    	
    	if(Strings.isNotBlank(conditionKey) && "serialNo".equals(conditionKey)) { 
    		if(EdocHelper.isG6Version()){
    			hsql.append(",EdocSummary as b where a.edocId=b.id and a.id = af.subObjectId and af.app = :app and af.subApp = :subApp " +
    					"and af.state = :afState and af.delete = :delete ");
    			parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
    			parameterMap.put("subApp", ApplicationSubCategoryEnum.old_edocRegister.getKey());
        		parameterMap.put("afState", StateEnum.col_pending.key());
        		parameterMap.put("delete", false);
    		}else{
    			hsql.append(",EdocSummary as b where a.edocId=b.id");
    		}
    	} else{
    		if(EdocHelper.isG6Version()){
    			hsql.append("where a.id = af.subObjectId and af.app = :app and af.subApp = :subApp and af.state = :afState and af.delete = :delete ");
        		parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
        		parameterMap.put("subApp", ApplicationSubCategoryEnum.old_edocRegister.getKey());
        		parameterMap.put("afState", StateEnum.col_pending.key());
        		parameterMap.put("delete", false);
    		}else{
    			hsql.append(" where 1=1");
    		}
    	}
    	
    	//V5-G6版本目前通过affair的数据来获取待登记数据，就不用签收数据的status了
    	if(!EdocHelper.isG6Version()){
    		hsql.append(" and a.status=:status");
        	parameterMap.put("status", state);
        	
        	hsql.append(" and a.registerUserId = :memberId");
        	parameterMap.put("memberId", memberId);
        } else{
        	hsql.append(" and af.memberId = :afMemberId ");
        	parameterMap.put("afMemberId", memberId);
        }
    	
    	
    	
    	if (Strings.isNotBlank(conditionKey)) {
			if("recTime".equals(conditionKey)) {//签收时间
				if (Strings.isNotBlank(textfield)) {
	   				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
	   				String paramName = "timestamp1";
	   				hsql.append(" and a."+conditionKey+" >= :" + paramName);
	   				parameterMap.put(paramName, stamp);
	   			}
				if (Strings.isNotBlank(textfield1)) {
	   				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
	   				String paramName = "timestamp2";
	   				hsql.append(" and a."+conditionKey+" <= :" + paramName);
	   				parameterMap.put(paramName, stamp);
	   			}
			} else {
				if (Strings.isNotBlank(textfield)) {//标题
					if("edocUnit".equals(conditionKey)) {
			            hsql.append(" and a.sendUnit like '%" + textfield + "%'");
			        } else if("serialNo".equals(conditionKey)) {
			        	hsql.append(" and b." + conditionKey + " like '%" + textfield + "%'");
			        } 
			        else if("secretLevel".equals(conditionKey)) {
		                hsql.append(" and a.secretLevel = :secretLevel");
                        parameterMap.put("secretLevel", textfield);
			        } 
			        else {
			        	hsql.append(" and a." + conditionKey + " like '%" + textfield + "%'");
			        }
				}
			}			
   		}
    	
    	hsql.append(" order by a.recTime desc"); 
    	
        return super.find(hsql.toString(), parameterMap);
    }

    public List<EdocRecieveRecord> findEdocRecieveRecords(String accountIds, String departIds, Set<Integer> statusSet, String condition, String[] value, int sendUnitType, int dateType, Map<String, Object> conditionParams) {
        String accWhere = null;
        String depWhere = null;
        
        long userId = (conditionParams==null||conditionParams.get("userId")==null)?CurrentUser.get().getId():(Long)conditionParams.get("userId");
        int affairState = (conditionParams==null||conditionParams.get("affairState")==null)?StateEnum.col_pending.key():Integer.parseInt((String)conditionParams.get("affairState"));
        int listValue = (conditionParams==null||conditionParams.get("listValue")==null)?EdocNavigationEnum.LIST_TYPE_EX_RECIEVE:(Integer)conditionParams.get("listValue");//列表类型,签收/登记
        int statusFlag = affairState==StateEnum.col_pending.key() ? 0 : 1;

        List<Object> objects = new ArrayList<Object>();
//        if((accountIds == null || "".equalsIgnoreCase(accountIds)) && (departIds == null || "".equalsIgnoreCase(departIds))) {
//            return null;
//        }
        if(statusSet == null || statusSet.size() == 0)
            return null;
        
        Map<String, Object> map = new HashMap<String, Object>();
        String hsql = "from EdocRecieveRecord as a where 1=1";
        
        //5.0新增加的待登记列表 只显示已签收 未登记的数据
        hsql += " and a.status in (:status)";
    	map.put("status", statusSet);
        
        if(sendUnitType == 1) {  
            hsql += " and a.sendUnitType=1";
        } else if(sendUnitType > 1) {
            hsql += " and a.sendUnitType!=1";
        } 
        
        //退件箱（自己的数据）
        String modelType = (conditionParams==null||conditionParams.get("modelType")==null)?null:(String)conditionParams.get("modelType"); 

        if(modelType != null && "retreat".equals(modelType)) {
        	hsql += " and a.recUserId=:recUserId2 ";
        	map.put("recUserId2", userId);
        }
        //签收时间
        if(condition != null && "recTime".equals(condition)) {
            if(value[0] != null && !"".equals(value[0])) {
            	hsql += " and a." + condition + " >= :starttime ";
            	java.util.Date stamp = Datetimes.getTodayFirstTime(value[0]);
            	objects.add(Timestamp.valueOf(Datetimes.formatDatetime(stamp)));
				map.put("starttime", Timestamp.valueOf(Datetimes.formatDatetime(stamp)));
            }
            if(value[1] != null && !"".equals(value[1])) {
            	hsql += " and a." + condition + " <= :endtime ";
            	java.util.Date stamp = Datetimes.getTodayLastTime(value[1]);
            	map.put("endtime", Timestamp.valueOf(Datetimes.formatDatetime(stamp)));
            }
        } else if(null != condition && !"".equals(condition) && null != value[0] && !"".equals(value[0])) {
        	hsql += " and a." + condition + " like :"+condition+"   ";
        	map.put(condition, "%"+value[0]+ "%");
        }
        if(dateType != 0) {
        	hsql += " and a.createTime >= :startCreatTime and a.createTime <= :endCreateTime ";
        	Date[] date = DateUtil.getStartEndTime(dateType);
            map.put("startCreatTime", date[0]);
            map.put("endCreateTime", date[1]);
        }
        if(Strings.isNotBlank(accountIds)) {
            accWhere = " (exchangeType=:exchangeType";
            accWhere += " and exchangeOrgId in (: accountIds ))";
            map.put("exchangeType", EdocRecieveRecord.Exchange_Receive_iAccountType_Org);
            List<Long> accountIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds,",");
            map.put("accountIds", accountIdsLong);
        }
        if(Strings.isNotBlank(departIds)) {
            depWhere = " (exchangeType=:exchangeType";
            depWhere += " and exchangeOrgId in (: departIds))";
            map.put("exchangeType", EdocRecieveRecord.Exchange_Receive_iAccountType_Dept);
            List<Long> departIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds,",");
            map.put("departIds", departIdsLong);
        }
        if(accWhere != null && depWhere != null) {
            hsql += " and (" + accWhere + " or " + depWhere + ")";
        } else if(accWhere != null && depWhere == null) {
            hsql += " and " + accWhere;
        } else if(accWhere == null && depWhere != null) {
            hsql += " and " + depWhere;
        }
        if(statusFlag == 1) {//已签收列表
        	if(listValue == EdocNavigationEnum.LIST_TYPE_REGISTER) {//待登记列表
        		hsql += " and a.registerUserId = :registerUserId ";
                map.put("registerUserId", userId);
        	} else {//已签收列表
        		hsql += " and a.recUserId = :recUserId ";
                map.put("recUserId", userId);
        	}
            hsql += " order by a.recTime desc";//已签收列表按签收时间倒序
        } else {//待签收列表
            hsql += " order by a.createTime desc";//待签收列表按创建时间倒序
        }
        return super.find(hsql,map);
    }

    public void deleteReceiveRecordByReplayId(long replayId) throws Exception {
        String hsql = "delete from EdocRecieveRecord where replyId = ? ";
        Object[] values = {String.valueOf(replayId)};
        super.bulkUpdate(hsql, null, values);
    }

    public EdocRecieveRecord getRecRecordByReplayId(long replyId) throws Exception {
        String hsql = "from EdocRecieveRecord  where replyId = ? ";
        Object[] values = {String.valueOf(replyId)};
        List<EdocRecieveRecord> list = super.findVarargs(hsql, values);
        if(null != list && list.size() > 0)
            return list.get(0);
        else
            return null;
    }
    
    
	public EdocRecieveRecord getByEdocId(long edocId){
        String hsql = "from EdocRecieveRecord  where edocId = ? ";
        Object[] values = {edocId};
        List<EdocRecieveRecord> list = super.findVarargs(hsql, values);
        if(null != list && list.size() > 0)
            return list.get(0);
        else
            return null;
	}
	
	public EdocRecieveRecord getEdocRecieveRecordByReciveEdocId(long id) {
		String hsql = "from EdocRecieveRecord as a where a.reciveEdocId=? ";
		List<EdocRecieveRecord> list =  super.findVarargs(hsql, id);
		if(null!=list && list.size()>0)
			return list.get(0);
		else
			return null;
	}	
	
	
	/********************************************公文交换列表       end*************************************************************/
	/**
	 * 公文签收列表
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecordList(int type, Map<String, Object> condition) {
		String conditionKey = condition.get("conditionKey")==null?null:(String)condition.get("conditionKey");;
		String textfield = condition.get("textfield")==null?null:(String)condition.get("textfield");
		String textfield1 = condition.get("textfield1")==null?null:(String)condition.get("textfield1");
		String accountIds = condition.get("accountIds")==null?null:(String)condition.get("accountIds");
		String departIds = condition.get("departIds")==null?null:(String)condition.get("departIds");
		List<Integer> statusList = condition.get("statusList")==null?null:(List<Integer>)condition.get("statusList");
		int sendUnitType = condition.get("sendUnitType")==null?0:(Integer)condition.get("sendUnitType");
		int state = condition.get("state")==null?0:(Integer)condition.get("state");
		User user = (User)condition.get("user");
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("from EdocRecieveRecord as a where 1=1");
		buffer.append(" and a.status in (:statusList)");
		parameterMap.put("statusList", statusList);		
		if(sendUnitType == 1) {//发送单位类型：内部单位
			buffer.append(" and a.sendUnitType=1");
        } else if(sendUnitType > 1) {//发送单位类型：外部单位
        	buffer.append(" and a.sendUnitType!=1");
        }
		if(Strings.isNotBlank(accountIds) && Strings.isBlank(departIds)) {//单位公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:accountIds))");
			parameterMap.put("exchangeType", EdocRecieveRecord.Exchange_Receive_iAccountType_Org);	
			List<Long> accountIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds,",");
			parameterMap.put("accountIds", accountIdsLong);	
		} else if(Strings.isBlank(accountIds) && Strings.isNotBlank(departIds)) {//部门公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:departIds))");
			parameterMap.put("exchangeType", EdocRecieveRecord.Exchange_Receive_iAccountType_Dept);	
			List<Long> departIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds,",");
			parameterMap.put("departIds", departIdsLong);	
		} else if(Strings.isNotBlank(accountIds) && Strings.isNotBlank(departIds)) {//单位公文收发员、部门公文收发员
			buffer.append(" and (");
			buffer.append(" (a.exchangeType=:exchangeTypeOrg and a.exchangeOrgId in (:accountIds))");
			buffer.append(" or ");
			buffer.append(" (a.exchangeType=:exchangeTypeDept and a.exchangeOrgId in (:departIds))");
			buffer.append(" )");
			parameterMap.put("exchangeTypeOrg", EdocRecieveRecord.Exchange_Receive_iAccountType_Org);	
			List<Long> accountIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds,",");
			parameterMap.put("accountIds", accountIdsLong);	
			parameterMap.put("exchangeTypeDept", EdocRecieveRecord.Exchange_Receive_iAccountType_Dept);	
			List<Long> departIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds,",");
			parameterMap.put("departIds", departIdsLong);	
		} else { //两个角色都不是，做防护
			buffer.append(" and 1=0 ");
		}
		if(Strings.isNotBlank(conditionKey)) {
			//签收时间
			if("recTime".equals(conditionKey)) {
				if(Strings.isNotBlank(textfield)) {
					java.util.Date stamp1 = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and a.recTime >=:timestamp1");
					parameterMap.put("timestamp1", stamp1);
				}
				if(Strings.isNotBlank(textfield1)) {
					java.util.Date stamp2 = Datetimes.getTodayFirstTime(textfield1);
					buffer.append(" and a.recTime <=:timestamp2");
					parameterMap.put("timestamp2", stamp2);
				}
			} else {
				if(Strings.isNotBlank(textfield)) {
					buffer.append(" and a."+ conditionKey + " like :conditionKey ");
					parameterMap.put("conditionKey", "%"+textfield+"%");
				}
			}
		}
		
		if(state == StateEnum.col_pending.key()) {//待签收列表
			buffer.append(" and (recUserId=0 or recUserId=:recUserId)");
			parameterMap.put("recUserId", user.getId());
			buffer.append(" order by a.createTime desc");
		} else {
			if(type == EdocNavigationEnum.LIST_TYPE_REGISTER) {//待登记列表
				buffer.append(" and a.registerUserId = :registerUserId");
				parameterMap.put("registerUserId", user.getId());
			} else {//已签收列表
				//buffer.append(" and a.recUserId = :recUserId");
				//parameterMap.put("recUserId", user.getId());
			}
			buffer.append(" order by a.recTime desc");
		}
		return super.find(buffer.toString(), parameterMap);
    }
	
	/**
	 * 逻辑删除发文交换数据
	 * @param ids
	 * @param status
	 * @throws BusinessException
	 */
	public void deleteEdocRecieveRecordByLogic(Object[] ids, int status) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		String hql = "update EdocRecieveRecord set status=:status where id in (:ids)";
		parameterMap.put("status", status);
		parameterMap.put("ids", ids);
		super.bulkUpdate(hql, parameterMap);
	}
	/********************************************公文交换列表      start***********************************************************/
	
	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param edocId
	 * @param exchangeOrgId
	 * @param status
	 * @return
	 */
	public List<EdocRecieveRecord> getEdocRecieveRecordByEdocIdAndOrgIdAndStatus(long edocId, long exchangeOrgId,int status){
		List<Integer> statusList = new ArrayList<Integer>();
		if (status == EdocRecieveRecord.Exchange_iStatus_Torecieve) {// 待发送列表
			statusList.add(status);
			statusList.add(EdocRecieveRecord.Exchange_iStatus_Receive_Retreat);
		}else{
			statusList.add(status);
		}
		
		String hsql = "from EdocRecieveRecord as a where  a.edocId=:edocId and a.exchangeOrgId=:exchangeOrgId and a.status in (:statusList) ";
		
		Map map = new HashMap();
		map.put("edocId", edocId);
		map.put("exchangeOrgId", exchangeOrgId);
		map.put("statusList", statusList);
		EdocRecieveRecord result = null;
		List<EdocRecieveRecord> records = super.find(hsql, map);

		return records;
	}
	
	/**
	 * 根据以下参数获得公文交换记录
	 * @param exchangeOrgId
	 * @param status
	 * @return
	 */
	public List<EdocRecieveRecord> getEdocRecieveRecordByOrgIdAndStatus(long exchangeOrgId,int status){
		List<Integer> statusList = new ArrayList<Integer>();
		if (status == EdocRecieveRecord.Exchange_iStatus_Torecieve) {// 待发送列表
			statusList.add(status);
			statusList.add(EdocRecieveRecord.Exchange_iStatus_Receive_Retreat);
		}else{
			statusList.add(status);
		}
		String hsql = "from EdocRecieveRecord as a where a.exchangeOrgId=:exchangeOrgId and a.status in (:statusList) ";
		
		Map map = new HashMap();
		map.put("exchangeOrgId", exchangeOrgId);
		map.put("statusList", statusList);
		EdocRecieveRecord result = null;
		List<EdocRecieveRecord> records = super.find(hsql, map);

		return records;
	}
	
	/**
	 * 获得某单位下某个状态的签收数据
	 * 用于 登记开关切换时，需要判断该单位中是否还有待登记的数据
	 * @param status
	 * @param accountId
	 * @return
	 */
	public List<EdocRecieveRecord> findEdocRecieveRecordByStatusAndAccountId(int status,List<Long> idList) {
		String hql = "from EdocRecieveRecord where status =:status and exchangeOrgId in (:exchangeOrgId)";
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("status", status);
		parameterMap.put("exchangeOrgId", idList);
		List<EdocRecieveRecord> list = super.find(hql,-1,-1,parameterMap);
		return list;
	}

	public int findEdocRecieveRecordCountByStatusAndAccountId(int status,List<Long> idList) {
		String hql = "from EdocRecieveRecord where status =:status and exchangeOrgId in (:exchangeOrgId)";
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("status", status);
		parameterMap.put("exchangeOrgId", idList);
		int count = super.count(hql,parameterMap);
		return count;
	}
	/**
	 * 公文签收列表
	 * @param type
	 * @param condition
	 * @return
	 */
	public int findEdocRecieveRecordCount(int type, Map<String, Object> condition) {
		String accountIds = condition.get("accountIds")==null?null:(String)condition.get("accountIds");
		String departIds = condition.get("departIds")==null?null:(String)condition.get("departIds");
		List<Integer> statusList = condition.get("statusList")==null?null:(List<Integer>)condition.get("statusList");
		int sendUnitType = condition.get("sendUnitType")==null?0:(Integer)condition.get("sendUnitType");
		int state = condition.get("state")==null?0:(Integer)condition.get("state");
		User user = (User)condition.get("user");
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("from EdocRecieveRecord as a where 1=1");
		buffer.append(" and a.status in (:statusList)");
		parameterMap.put("statusList", statusList);		
		if(sendUnitType == 1) {//发送单位类型：内部单位
			buffer.append(" and a.sendUnitType=1");
        } else if(sendUnitType > 1) {//发送单位类型：外部单位
        	buffer.append(" and a.sendUnitType!=1");
        }
		if(Strings.isNotBlank(accountIds) && Strings.isBlank(departIds)) {//单位公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:accountIds))");
			parameterMap.put("exchangeType", EdocRecieveRecord.Exchange_Receive_iAccountType_Org);	
			List<Long> accountIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds,",");
			parameterMap.put("accountIds", accountIdsLong);	
		} else if(Strings.isBlank(accountIds) && Strings.isNotBlank(departIds)) {//部门公文收发员
			buffer.append(" and (a.exchangeType=:exchangeType and a.exchangeOrgId in (:departIds))");
			parameterMap.put("exchangeType", EdocRecieveRecord.Exchange_Receive_iAccountType_Dept);	
			List<Long> departIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds,",");
			parameterMap.put("departIds", departIdsLong);	
		} else if(Strings.isNotBlank(accountIds) && Strings.isNotBlank(departIds)) {//单位公文收发员、部门公文收发员
			buffer.append(" and (");
			buffer.append(" (a.exchangeType=:exchangeTypeOrg and a.exchangeOrgId in (:accountIds))");
			buffer.append(" or ");
			buffer.append(" (a.exchangeType=:exchangeTypeDept and a.exchangeOrgId in (:departIds))");
			buffer.append(" )");
			parameterMap.put("exchangeTypeOrg", EdocRecieveRecord.Exchange_Receive_iAccountType_Org);	
			List<Long> accountIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(accountIds,",");
			parameterMap.put("accountIds", accountIdsLong);	
			parameterMap.put("exchangeTypeDept", EdocRecieveRecord.Exchange_Receive_iAccountType_Dept);	
			List<Long> departIdsLong = com.seeyon.v3x.edoc.util.StringUtils.convertStringToLongList(departIds,",");
			parameterMap.put("departIds", departIdsLong);	
		} else { //两个角色都不是，做防护
			buffer.append(" and 1=0 ");
		}
		if(state == StateEnum.col_pending.key()) {//待签收列表
			buffer.append(" and (recUserId=0 or recUserId=:recUserId)");
			parameterMap.put("recUserId", user.getId());
			buffer.append(" order by a.createTime desc");
		} else {
			if(type == EdocNavigationEnum.LIST_TYPE_REGISTER) {//待登记列表
				buffer.append(" and a.registerUserId = :registerUserId");
				parameterMap.put("registerUserId", user.getId());
			} 
			buffer.append(" order by a.recTime desc");
		}
		return super.count(buffer.toString(), parameterMap);
    }
}
