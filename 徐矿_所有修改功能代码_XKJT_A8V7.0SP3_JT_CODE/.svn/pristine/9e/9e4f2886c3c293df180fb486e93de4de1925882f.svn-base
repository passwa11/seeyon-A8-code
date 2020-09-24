package com.seeyon.v3x.edoc.manager;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.collaboration.util.CollaborationUtils;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.affair.constants.TrackEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.dao.OrgCache;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.dao.EdocListDao;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryCountVO;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.worktimeset.exception.WorkTimeSetExecption;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;

public class EdocListManagerImpl implements EdocListManager {

	private final static Log log = LogFactory.getLog(EdocListManagerImpl.class);

	private EdocListDao edocListDao;
	private OrgManager orgManager;
	private EdocManager edocManager;
	private WorkTimeManager workTimeManager;
	private OrgCache orgCache;

	public void setOrgCache(OrgCache orgCache) {
		this.orgCache = orgCache;
	}

	/**
	 * 公文待办查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocPendingList(int type, Map<String, Object> condition) throws BusinessException {
		condition = setEdocListCondition(type, condition);
		if(type==EdocNavigationEnum.LIST_TYPE_PENDING || type==EdocNavigationEnum.LIST_TYPE_DONE || type==EdocNavigationEnum.LIST_TYPE_OVER) {
            condition = getEdocAgentInfo(condition);
        }
		List<Object[]> result = edocListDao.findEdocList(type, condition);
        return bindListDataToEdocBo(type, condition, result);
	}

	/**
	 * 公文已办查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocDoneList(int type, Map<String, Object> condition) throws BusinessException {
		condition = getEdocAgentInfo(condition);
		condition = setEdocListCondition(type, condition);
		List<Object[]> result = edocListDao.findEdocList(type, condition);
        return bindListDataToEdocBo(type, condition, result);
	}

	/**
	 * 公文待发查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocWaitSendList(int type, Map<String, Object> condition) throws BusinessException {
		condition = setEdocListCondition(type, condition);
		String listType = condition.get("listType")==null ? "listSendAll" : (String)condition.get("listType");
		int edocType = condition.get("edocType")==null?-1:(Integer)condition.get("edocType");
		List<Object[]> result = null;
		//G6的收文分发待发页面才这样查询
  		if(EdocHelper.isG6Version() && edocType == EdocEnum.edocType.recEdoc.ordinal() 
  		        && "listWaitSend".equals(listType)){
  			result = edocListDao.findWaitEdocList(type, condition);
  			List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>(result.size());
  			int id=0;
  			for (int i = 0; i < result.size(); i++) {
  	            Object[] obj = (Object[]) result.get(i);
  	            EdocSummaryModel model = new EdocSummaryModel();
  	            EdocSummary summary = new EdocSummary();
  	            id = 0;
  	            summary.setId((Long)obj[id++]);
  	            summary.setSubject(String.valueOf(obj[id++]));
  	            summary.setSecretLevel(String.valueOf(obj[id++]));
  	            summary.setDocMark(String.valueOf(obj[id++]));
  	            summary.setSerialNo(String.valueOf(obj[id++]));
  	            
  	            summary.setCaseId((Long)obj[id++]);
  	            summary.setProcessId(String.valueOf(obj[id++]));
  	            //流程期限时间点
  	            Object deadlineDatetimeObj=obj[id++];
  	            if(deadlineDatetimeObj!=null){
  	            	summary.setDeadlineDatetime((java.util.Date)deadlineDatetimeObj);
  	            }

  	            String registerUserName = String.valueOf(obj[id++]);
	            if(Strings.isBlank(registerUserName)|| "null".equals(registerUserName))registerUserName = "";
	            model.setRegisterUserName(registerUserName);
  	            model.setRegisterDate((Date)obj[id++]);
  	            model.setRecieveDate((Timestamp)obj[id++]);

  	            String recUserName = String.valueOf(obj[id++]);
	            if(Strings.isBlank(recUserName)|| "null".equals(recUserName))recUserName = "";
	            model.setRecUserName(recUserName);
	            model.setAffairId((Long)obj[id++]);
	            model.setState((Integer)obj[id++]);
	            model.setRegisterType((Integer) obj[id++]);//登记类型
	            model.setRegisterId((Long)obj[id++]);
	            model.setAutoRegister((Integer)obj[id++]);
	            model.setReceiveId((Long)obj[id++]);
	            model.setExchangeMode((Integer)obj[id++]+"");//交换方式
	            Object isQuickSend = (Object)obj[id++];
	           
	            summary.setIsQuickSend(isQuickSend == null ? false:(Boolean)isQuickSend);
	           
	            model.setDeadlineDisplay(EdocHelper.getDeadLineName(summary.getDeadlineDatetime()));
	            //流程期限
	            String isNull = ResourceUtil.getString("collaboration.project.nothing.label"); //无
	            Long deadline = summary.getDeadline();
	            if(summary.getDeadlineDatetime()==null){
	            	model.setDeadlineDisplay((deadline == null || deadline == 0)? isNull : ColUtil.getDeadLineNameForEdoc(deadline,summary.getCreateTime()));
	            }else{
	            	model.setDeadlineDisplay(EdocHelper.getDeadLineName(summary.getDeadlineDatetime()));
	            }
  	            model.setSummary(summary);
  	            
     	        CtpAffair affair = new CtpAffair();
  	            affair.setSubState(model.getState());
  	            
  	            model.setAffair(affair);
  	            
  	            models.add(model);
  			}
  			return models;
  		}else{
  		    
  		    //设置特殊的标识，用于查询登记的数据
  		    if(edocType == EdocEnum.edocType.recEdoc.ordinal()){
  		        condition.put("a8WaitSendList", "true");
  		    }
  		    
  			result = edocListDao.findEdocList(type, condition);
  			return bindListDataToEdocBo(type, condition, result);
  		}

	}

	/**
	 * 公文已发查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<EdocSummaryModel> findEdocSentList(int type, Map<String, Object> condition) throws BusinessException {
		int listTypeInt = condition.get("listTypeInt")==null ? 0 : (Integer)condition.get("listTypeInt");
		if(listTypeInt != 0) {
			String[] dateFields = com.seeyon.v3x.edoc.util.DateUtil.getTimeTextFiledByTimeEnum(listTypeInt);
			condition.put("conditionKey", "createDate");
			condition.put("textfield", dateFields[0]);
			condition.put("textfield1", dateFields[1]);
		}
		condition = setEdocListCondition(type, condition);
		List<Object[]> result = edocListDao.findEdocList(type, condition);
        return bindListDataToEdocBo(type, condition, result);
	}

	/**
	 * 组合查询
	 * @param type
	 * @param condition
	 * @param edocSearchModel
	 * @return
	 */
     public List<EdocSummaryModel> combQueryByCondition(int type, Map<String, Object> condition, EdocSearchModel em) throws BusinessException {
    	 condition = setEdocListCondition(type, condition);
    	 if(type==EdocNavigationEnum.LIST_TYPE_PENDING || type==EdocNavigationEnum.LIST_TYPE_DONE) {
    		 condition = getEdocAgentInfo(condition);
    	 }
    	 List<Object[]> result = edocListDao.combQueryByCondition(type, condition, em);
         return bindListDataToEdocBo(type, condition, result, em);
     }

	/**
	 * 设置查询条件
	 * @param type
	 * @param condition
	 * @return
	 */
	public Map<String, Object> setEdocListCondition(int type, Map<String, Object> condition) {
		
		String conditionKey = ParamUtil.getString(condition, "conditionKey", "");
		String textfield = ParamUtil.getString(condition, "textfield", "");
		String textfield1 = ParamUtil.getString(condition, "textfield1", "");
		String listType = ParamUtil.getString(condition, "listType", "listPendingAll");
		/**徐矿集团【移动端增加办结公文数据查询接口】 wxt.dulong 2019-5-28 start*/
        if("listOver".equals(ParamUtil.getString(condition, "listType", "listPendingAll"))){
        	listType="listDoneAll";
		}
        /**徐矿集团【移动端增加办结公文数据查询接口】 wxt.dulong 2019-5-28 end*/
		int edocType = ParamUtil.getInt(condition, "edocType", -1);
		//如果条件来自自定义分类的时间段，设置查询条件和查询起始时间
		if("cusReceiveTime".equals(conditionKey)) {
			conditionKey = "receiveTime";
			String[] textfield_condition = edocManager.getTimeTextFiledByTimeEnum(Integer.parseInt(textfield));
			textfield = textfield_condition[0];
			textfield1 = textfield_condition[1];
		}
		//流程来源
		List<Integer> appList = new ArrayList<Integer>();
		
		// best 新增的栏目，如果是需要显示所有则需要显示收文和发文 start
		if (edocType == 999) {
			appList.add(ApplicationCategoryEnum.edocSend.getKey());
			appList.add(ApplicationCategoryEnum.edocRec.getKey());
		} else {
		// best 新增的栏目，如果是需要显示所有则需要显示收文和发文 end
			
			if(edocType == -1) {
				appList.add(ApplicationCategoryEnum.edocSend.getKey());
				appList.add(ApplicationCategoryEnum.edocRec.getKey());
				appList.add(ApplicationCategoryEnum.edocSign.getKey());
			} else {
				appList.add(EdocUtil.getAppCategoryByEdocType(edocType).getKey());
			}
		}
		//公文处理状态
		List<Integer> stateList = new ArrayList<Integer>();
		String state = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getState();
		if(!"-1".equals(state)) {
			String[] states = state.split(",");
			if(states != null) {
				for(int i=0; i<states.length; i++) {
					stateList.add(Integer.parseInt(states[i]));
				}
			}
		}
		//公文处理子状态
		List<Integer> substateList = new ArrayList<Integer>();
		String substate = EdocNavigationEnum.EdocV5ListTypeEnum.getEnumByKey(listType).getSubstate();
		if(!"-1".equals(substate)) {
			String[] substates = substate.split(",");
			if(substates != null) {
				for(int i=0; i<substates.length; i++) {
					substateList.add(Integer.parseInt(substates[i]));
				}
			}
		}
		//根据状态查询
		if("subState".equals(conditionKey)&&Strings.isNotBlank(textfield)) {
			if(textfield.equals(SubStateEnum.col_waitSend_stepBack.getKey()+"")){//回退，要查询出，指定回退的
				//协同-待发--指定回退到发起人并且选择流程重走，这是发起人的状态
				//公文--在办时，指定退回（直接提交给我）普通节点
				substateList.add(SubStateEnum.col_waitSend_stepBack.getKey());
				substateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
				substateList.add(SubStateEnum.col_pending_specialBacked.getKey());
			}else{
				substateList.add(Integer.parseInt(textfield));
			}
		}
		textfield = convertSpecialChat(textfield);
		textfield1 = convertSpecialChat(textfield1);
		condition.put("conditionKey", conditionKey);
		condition.put("textfield", textfield);
		condition.put("textfield1", textfield1);
		condition.put("appList", appList);
		condition.put("stateList", stateList);
		condition.put("substateList", substateList);
		return condition;
	}

	/**
	 * 绑定查询数据(小查询)，将po对象转化为bo对象
	 * @param type
	 * @param condition
	 * @param result
	 * @return
	 * @throws BusinessException
	 */
	private List<EdocSummaryModel> bindListDataToEdocBo(int type, Map<String, Object> condition, List<Object[]> result) throws BusinessException {
		return bindListDataToEdocBo(type, condition, result, null);
	}

	/**
	 * 绑定查询数据(组合查询)，将po对象转化为bo对象
	 * @param type
	 * @param condition
	 * @param result
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private List<EdocSummaryModel> bindListDataToEdocBo(int type, Map<String, Object> condition, List<Object[]> result, EdocSearchModel em) throws BusinessException {
		String listType = (String)condition.get("listType");
		/**徐矿集团【移动端增加办结公文数据查询接口】 wxt.dulong 2019-5-28 start*/
		if ("listOver".equals((String)condition.get("listType"))) {
			listType="listDoneAll";
		}
		/**徐矿集团【移动端增加办结公文数据查询接口】 wxt.dulong 2019-5-28 end*/
		String conditionKey = condition.get("conditionKey") == null ? null : (String)condition.get("conditionKey");
		User user = (User)condition.get("user");
		List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>(result.size());

		int size = result.size();

		//TODO 临时方案解决性能问题
		Map<Long, String> members = new HashMap<Long, String>();
		if(size > 100){
		    try{
    		    List<V3xOrgMember> allMember =  orgCache.getAllV3xOrgEntity(V3xOrgMember.class, null);
    		    for (V3xOrgMember m : allMember) {
    		        members.put(m.getId(), m.getName());
    		    }
		    }
		    catch(Exception e){
		        //ignore
		    }
		}

        for (int i = 0; i < size; i++) {
            Object[] object = (Object[]) result.get(i);
            CtpAffair affair = new CtpAffair();
            EdocSummary summary = new EdocSummary();
            int index = summary.make(object,summary,affair);
            summary.setSerialNo(summary.getSerialNo()==null?"":summary.getSerialNo());
            try {
                V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
                summary.setStartMember(member);
            } catch (BusinessException e) {
                log.error("组合查询抛出异常", e);
            }
            //开始组装最后返回的结果
            EdocSummaryModel model = new EdocSummaryModel();
            if("exchangeMode".equals(conditionKey)||"true".equals(condition.get("a8WaitSendList"))){
            	model.setExchangeMode((Integer) object[index++]+"");//设置交换方式
        	}
          //A8 收文待发 查询登记信息
            if("true".equals(condition.get("a8WaitSendList"))){
                model.setRegisterType((Integer) object[index++]);//登记类型
                model.setRegisterId((Long)object[index++]);
                model.setAutoRegister((Integer)object[index++]);
                model.setReceiveId((Long)object[index++]);
            }
            
            model.setWorkitemId(affair.getObjectId() + "");
            model.setCaseId(summary.getCaseId() + "");
            model.setNodePolicy(affair.getNodePolicy());
            model.setBodyType(affair.getBodyType());
            model.setFinshed(summary.getCompleteTime()!= null);
            //公文状态
            int affairState=affair.getState();
            if(affairState == StateEnum.col_waitSend.key()){model.setEdocType(EdocSummaryModel.EDOCTYPE.WaitSend.name());}
            else if(affairState == StateEnum.col_sent.key()){
            	model.setEdocType(EdocSummaryModel.EDOCTYPE.Sent.name());

            	//设置流程是否超期标志
				java.sql.Timestamp finishDate = summary.getCompleteTime();
				Date now = new Date(System.currentTimeMillis());
				if(summary.getDeadlineDatetime() != null){
					if(finishDate==null){
						Long expendTime = now.getTime() - summary.getDeadlineDatetime().getTime();
						if(expendTime > 0){
							summary.setWorklfowTimeout(true);
						}
					}else{
						Long expendTime = summary.getCompleteTime().getTime() - summary.getDeadlineDatetime().getTime();
						if(expendTime > 0){
							summary.setWorklfowTimeout(true);
						}
					}
				}
            }
            else if(affairState == StateEnum.col_done.key()){model.setEdocType(EdocSummaryModel.EDOCTYPE.Done.name());}
            else if(affairState == StateEnum.col_pending.key()){model.setEdocType(EdocSummaryModel.EDOCTYPE.Pending.name());}
            //公文子状态
            Integer sub_state = affair.getSubState();
            if (sub_state != null) {
                model.setState(sub_state.intValue());
            }
            //是否跟踪
            Integer isTrack = affair.getTrack();
            if (isTrack != null) {
              if(CollaborationEnum.flowState.finish.ordinal() == summary.getState()){
                model.setTrack(TrackEnum.no.ordinal());
              }else{
                model.setTrack(isTrack.intValue());
              }
            }
            //催办次数
            Integer hastenTimes = affair.getHastenTimes();
            if (hastenTimes != null) {
                model.setHastenTimes(hastenTimes);
            }
            //是否超期
            Boolean overtopTime = affair.isCoverTime();
            if(overtopTime != null){
            	model.setOvertopTime(overtopTime.booleanValue());
            }
            //提前提醒
            Long advanceRemind = 0L;
            if(affair.getRemindDate() != null){
            	advanceRemind = affair.getRemindDate();
            }
            model.setAdvanceRemindTime(advanceRemind);
            //协同处理期限
            Long deadLine = 0L;
            if(affair.getDeadlineDate() != null){
            	deadLine = affair.getDeadlineDate();
            }
            model.setDeadLine(deadLine);
            model.setDealLineDateTime(CollaborationUtils.getDeadLineName(affair.getExpectedProcessTime()));
            //流程期限
            String isNull = ResourceUtil.getString("collaboration.project.nothing.label"); //无
            Long deadline = summary.getDeadline();
            if(summary.getDeadlineDatetime()==null){
            	model.setDeadlineDisplay((deadline == null || deadline == 0)? isNull : ColUtil.getDeadLineNameForEdoc(deadline,summary.getCreateTime()));
            }else{
            	model.setDeadlineDisplay(EdocHelper.getDeadLineName(summary.getDeadlineDatetime()));
            }
            //将暂存待办提醒时间加入列数据中(用于小查询)
            if(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey().equals(listType)) {
            	if(object[object.length-1] != null) {
            		model.setZcdbTime(new java.sql.Date(((Timestamp)object[object.length-1]).getTime()));
            	}
            }
            //设置完成时间
            if(affair.getCompleteTime() != null){
				model.setDealTime(new Date(affair.getCompleteTime().getTime()));
			}
            
            //设置剩余时间
            if(condition.get("isOnlyPendingSurplusTime")!=null && (Boolean)condition.get("isOnlyPendingSurplusTime")) {
            	if(affairState == StateEnum.col_pending.key()) {//待办才显示剩余时间
            		model.setSurplusTime(this.calculateSurplusTime(affair.getReceiveTime(),affair.getExpectedProcessTime()));
            	}
            } else {
            	model.setSurplusTime(this.calculateSurplusTime(affair.getReceiveTime(),affair.getExpectedProcessTime()));
            }
            
            //是否代理
            boolean agentToFlag = false;
            boolean agentFlag = false;
    		List<AgentModel> edocAgent = new ArrayList<AgentModel>();
    		if(condition.get("edocAgent") != null) {
    			edocAgent = (List<AgentModel>)condition.get("edocAgent");
    		}
    		if(condition.get("agentToFlag") != null) {
    			agentToFlag = (Boolean)condition.get("agentToFlag");
    		}
    		if(condition.get("agentFlag") != null) {
    			agentFlag = (Boolean)condition.get("agentFlag");
    		}
    		java.util.Date early = null;
    		if(edocAgent != null && !edocAgent.isEmpty()) {
    			early = edocAgent.get(0).getStartDate();
    		}
    		if(type==EdocNavigationEnum.LIST_TYPE_PENDING) {
    			if(agentFlag && affair.getMemberId().longValue()!=user.getId().longValue()) {
    				Long proxyMemberId = affair.getMemberId();
    				try {
    					V3xOrgMember member = orgManager.getMemberById(proxyMemberId);
    					model.setProxyName(member.getName());
    					model.setProxy(true);
    				} catch (BusinessException e) {
    					log.error("组合查询抛出异常",  e);
    				}
    			} else if(agentToFlag && early != null && early.before(affair.getReceiveTime())) {
    				model.setProxy(true);
    			}
    		}
            //设置标题
            String subject = "";
            if(StateEnum.col_done.getKey() == affair.getState().intValue() || StateEnum.col_sent.getKey() == affair.getState().intValue()) {//已办、G6已分发 增加代理信息显示
               subject = EdocUtil.showSubjectOfSummary4Done(affair, -1);
            } else {
               subject = EdocUtil.showSubjectOfEdocSummary(summary, model.isProxy(), -1, model.getProxyName(),false);
            }
            summary.setSubject(TrimRight(subject));
            model.setCreateDate(EdocUtil.showDate(summary.getCreateTime()));
            model.setCreatePerson(Functions.showMemberName(summary.getStartUserId()));
            model.setAffairId(affair.getId());
            model.setAffair(affair);
            model.setSummary(summary);
            //设置当前处理人信息
            String currentNodessInfo = EdocHelper.parseCurrentNodesInfo(summary, members);
            String fullInfo = summary.getCurrentNodesInfo();
            int count = 0;
            if(Strings.isNotBlank(fullInfo)){
            	count = fullInfo.split("[;]").length;
            }
            if(count>=10){
            	currentNodessInfo += "...";
            }
            model.setCurrentNodesInfo(currentNodessInfo);

            models.add(model);
        }
        return models;
	}

	
	
	public String TrimRight(String sString){
	  String sResult = "";
	  if(Strings.isBlank(sString)){
	    return sResult;
	  }
	  sResult = sString.substring(0, sString.lastIndexOf(sString.trim())+sString.trim().length());   

	  return sResult;     
	} 



	/**
	 * 代理人处理
	 * @param condition
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unused")
	public Map<String, Object> getEdocAgentInfo(Map<String, Object> condition) {
		long userId = (Long)condition.get("userId");
		//获取代理相关信息
  		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(userId);
      	List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(userId);
  		List<AgentModel> agentModelList = null;
  		boolean agentToFlag = false;
  		boolean agentFlag = false;
  		if(_agentModelList != null && !_agentModelList.isEmpty()){
  			agentModelList = _agentModelList;
  			agentFlag = true;
  		}else if(_agentModelToList != null && !_agentModelToList.isEmpty()){
  			agentModelList = _agentModelToList;
  			agentToFlag = true;
  		}

  		List<AgentModel> edocAgent = new ArrayList<AgentModel>();
  		if(agentModelList != null && !agentModelList.isEmpty()){
  			java.util.Date now = new java.util.Date();
  	    	for(AgentModel agentModel : agentModelList){
      			if(agentModel.isHasEdoc() && agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now)){
      				edocAgent.add(agentModel);
      			}
  	    	}
  		}
      	boolean isProxy = false;
  		if(edocAgent != null && !edocAgent.isEmpty()){
  			isProxy = true;
  		}else{
  			agentFlag = false;
  			agentToFlag = false;
  		}
  		condition.put("edocAgent", edocAgent);
  		condition.put("agentToFlag", agentToFlag);
  		condition.put("agentFlag", agentFlag);
  		return condition;
	}

    /**
     * 获取剩余办理时间
     * @param date 办理时间
     * @param deadlineDate 限时办理(分钟)
     * @return
     */
    @SuppressWarnings("deprecation")
	private int[] getLastHandleTime(java.util.Date date, Long deadlineDate) {
    	int[] surplusTime = null;
    	if(deadlineDate!=null && deadlineDate!=0 && null!=date){
			try {
				date.setMinutes(date.getMinutes()+deadlineDate.intValue())/*Integer.parseInt(String.valueOf(deadlineDate)))*/;//在原来的时间上加上分钟数
				java.util.Date end = new java.util.Date();
				int between=(int) ((date.getTime()-end.getTime())/1000);//除以1000是为了转换成秒
		        int days=between/(24*3600);
		        int hours=between%(24*3600)/3600;
				int minutes=between%3600/60;
				surplusTime = new int[3];
	            surplusTime[0]=days;
	            surplusTime[1]=hours;
	            surplusTime[2]=minutes;
			} catch (Exception e) {
				log.error("", e);
			}
    	}else{
    		return null;
    	}

		return surplusTime;
    }


    /**
     * 计算节点处理剩余时间
     * @param date 流程到达时间
     * @param expecetProcessTime 节点处理期限（具体时间）
     * @return 剩余时间（不足一天单位为：小时，反之单位为：天）
     */
    @SuppressWarnings("deprecation")
    private int[] calculateSurplusTime(java.util.Date date,java.util.Date expecetProcessTime) {
        int[] surplusTime = null;

        User user = AppContext.getCurrentUser();
        try {
            if(expecetProcessTime !=null) {
                long days = 0;
                long hours = 0;
                long minutes = 0;
                // 获取系统当前时间
                java.util.Date nowTime = new java.util.Date();
                // 得到节点处理的最后时间
                //java.util.Date overTime = workTimeManager.getCompleteDate4Nature(date, deadlineDate, user.getAccountId());
                // 未超期
                if(nowTime.before(expecetProcessTime)) {
                    // 得到剩余处理时间（分钟）
                	long surplusMinu = workTimeManager.getDealWithTimeValue(nowTime, expecetProcessTime, user.getAccountId()) / (1000 * 60);
                    // 得到当前单位当月的日工作时间（分钟）
                    long dayOfMinu = workTimeManager.getEachDayWorkTime(nowTime.getYear(), user.getAccountId());
                    if(surplusMinu >= dayOfMinu) {
                        // 天数
                        days = surplusMinu / dayOfMinu;
                        long shenyufen = surplusMinu - days * dayOfMinu;
                        if(shenyufen < 60) {
                            minutes = shenyufen;
                        } else {
                            hours = shenyufen / 60;
                            minutes = shenyufen - hours * 60;
                        }
                    } else if(60 <= surplusMinu && surplusMinu < dayOfMinu) {
                        hours = surplusMinu / 60;
                        minutes = surplusMinu - hours * 60;
                    } else {
                        minutes = surplusMinu;
                    }
                }
                surplusTime = new int[3];
                surplusTime[0]=(int)days;
                surplusTime[1]=(int)hours;
                surplusTime[2]=(int)minutes;
            }
        } catch(WorkTimeSetExecption e) {
            log.error("计算节点处理剩余时间抛出异常", e);
        }
        return surplusTime;
    }

	/**
	 * 公文待办总数
	 * @param type
	 * @param condition
	 * @return
	 */
	public int findEdocPendingCount(int type, Map<String, Object> condition) throws BusinessException {
		condition = setEdocListCondition(type, condition);
		condition = getEdocAgentInfo(condition);
		return edocListDao.findEdocPendingCount(type, condition);

	}

    /**
     * 根据公文类型获取各类型待办、已办等数据条数
     * @return
     * @throws BusinessException
     */
	public List<EdocSummaryCountVO> getCountGroupByEdocType() throws BusinessException {
		
		Map<String, Object> fwbj=new HashMap<String, Object>();
		fwbj.put("isPage", false);
		fwbj.put("agentFlag", false);
		fwbj.put("edocType", 0);
		fwbj.put("stateList", new ArrayList<Integer>() {{add(4);}});
		fwbj.put("subEdocType", -1L);
		fwbj.put("type", 10);
		fwbj.put("userId", AppContext.getCurrentUser().getId());
		fwbj.put("listType", "listOver");
		fwbj.put("edocAgent", new ArrayList<AgentModel>());
		fwbj.put("agentToFlag", false);
		fwbj.put("accountId", AppContext.getCurrentUser().getAccountId());
		fwbj.put("conditionKey", "");
		fwbj.put("appList", new ArrayList<Integer>() {{add(19);}});
		fwbj.put("substateList", new ArrayList<Integer>());
		fwbj.put("isOnlyPendingSurplusTime", true);
		fwbj.put("state", 3);
		fwbj.put("track", -1);
		fwbj.put("user", AppContext.getCurrentUser().getId());
		List<Object[]> overTypeSize = edocListDao.findEdocList(EdocNavigationEnum.LIST_TYPE_OVER,fwbj);
		
		Map<String, Object> fwyb=new HashMap<String, Object>();
		fwyb.put("isPage", false);
		fwyb.put("agentFlag", false);
		fwyb.put("edocType", 0);
		fwyb.put("stateList", new ArrayList<Integer>() {{add(4);}});
		fwyb.put("subEdocType", -1L);
		fwyb.put("type", 10);
		fwyb.put("userId", AppContext.getCurrentUser().getId());
		fwyb.put("listType", "listDoneAll");
		fwyb.put("edocAgent", new ArrayList<AgentModel>());
		fwyb.put("agentToFlag", false);
		fwyb.put("accountId", AppContext.getCurrentUser().getAccountId());
		fwyb.put("conditionKey", "");
		fwyb.put("appList", new ArrayList<Integer>() {{add(19);}});
		fwyb.put("substateList", new ArrayList<Integer>());
		fwyb.put("isOnlyPendingSurplusTime", true);
		fwyb.put("state", 3);
		fwyb.put("track", -1);
		fwyb.put("user", AppContext.getCurrentUser().getId());
		List<Object[]> doneTypeSize = edocListDao.findEdocList(EdocNavigationEnum.LIST_TYPE_DONE,fwyb);
		
		Map<String, Object> swyb=new HashMap<String, Object>();
		swyb.put("isPage", false);
		swyb.put("agentFlag", false);
		swyb.put("edocType", 1);
		swyb.put("stateList", new ArrayList<Integer>() {{add(4);}});
		swyb.put("subEdocType", -1L);
		swyb.put("type", 10);
		swyb.put("userId", AppContext.getCurrentUser().getId());
		swyb.put("listType", "listDoneAll");
		swyb.put("edocAgent", new ArrayList<AgentModel>());
		swyb.put("agentToFlag", false);
		swyb.put("accountId", AppContext.getCurrentUser().getAccountId());
		swyb.put("conditionKey", "");
		swyb.put("appList", new ArrayList<Integer>() {{add(20);}});
		swyb.put("substateList", new ArrayList<Integer>());
		swyb.put("isOnlyPendingSurplusTime", true);
		swyb.put("state", 3);
		swyb.put("track", -1);
		swyb.put("user", AppContext.getCurrentUser().getId());
		List<Object[]> SWdoneTypeSize = edocListDao.findEdocList(EdocNavigationEnum.LIST_TYPE_DONE,swyb);
		
		Map<String, Object> swbj=new HashMap<String, Object>();
		swbj.put("isPage", false);
		swbj.put("agentFlag", false);
		swbj.put("edocType", 1);
		swbj.put("stateList", new ArrayList<Integer>() {{add(4);}});
		swbj.put("subEdocType", -1L);
		swbj.put("type", 10);
		swbj.put("userId", AppContext.getCurrentUser().getId());
		swbj.put("listType", "listOver");
		swbj.put("edocAgent", new ArrayList<AgentModel>());
		swbj.put("agentToFlag", false);
		swbj.put("accountId", AppContext.getCurrentUser().getAccountId());
		swbj.put("conditionKey", "");
		swbj.put("appList", new ArrayList<Integer>() {{add(20);}});
		swbj.put("substateList", new ArrayList<Integer>());
		swbj.put("isOnlyPendingSurplusTime", true);
		swbj.put("state", 3);
		swbj.put("track", -1);
		swbj.put("user", AppContext.getCurrentUser().getId());
		List<Object[]> SWoverTypeSize = edocListDao.findEdocList(EdocNavigationEnum.LIST_TYPE_OVER,swbj);
		
		Map<String, Object> condition = new HashMap<String, Object>();
		long userId = AppContext.currentUserId();
		condition.put("userId", userId);
		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(userId);//代理人
		condition.put("edocAgent", _agentModelList);
		List<Object[]> objects = edocListDao.getCountGroupByEdocType(condition);
		
		EdocSummaryCountVO sendVo = new EdocSummaryCountVO();
		sendVo.setEdocType(0);
		EdocSummaryCountVO receiveVo = new EdocSummaryCountVO();
		receiveVo.setEdocType(1);
		EdocSummaryCountVO reportVo = new EdocSummaryCountVO();
		reportVo.setEdocType(2);
		
		//待办列表substate值
		List<Integer> pendingSubState = new ArrayList<Integer>();
		pendingSubState.add(SubStateEnum.col_normal.key());
		pendingSubState.add(SubStateEnum.col_waitSend_stepBack.key());
		pendingSubState.add(SubStateEnum.col_pending_unRead.key());
		pendingSubState.add(SubStateEnum.col_pending_read.key());
		pendingSubState.add(SubStateEnum.col_pending_assign.key());
		pendingSubState.add(SubStateEnum.col_pending_specialBack.key());
		pendingSubState.add(SubStateEnum.col_pending_specialBacked.key());
		pendingSubState.add(SubStateEnum.col_pending_specialBackCenter.key());
		pendingSubState.add(SubStateEnum.col_pending_takeBack.key());
		pendingSubState.add(SubStateEnum.col_waitSend_sendBack.key());
		
		Integer app, state, subState;
		Long summaryCount, memberId;
		for(Object[] obj : objects){
			summaryCount = (Long)obj[0];
			app = (Integer)obj[1];
			state = (Integer)obj[2];
			subState = (Integer)obj[3];
			memberId = (Long)obj[4];
			if(app == 19){
				if(state == StateEnum.col_pending.key() && pendingSubState.contains(subState)){
					sendVo.setListPendingSize(sendVo.getListPendingSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_pending.key() && (subState == SubStateEnum.col_pending_ZCDB.key() || subState == SubStateEnum.col_pending_specialBackToSenderReGo.key())){
					sendVo.setListZcdbSize(sendVo.getListZcdbSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_waitSend.key() && memberId.equals(userId)){
					sendVo.setListWaitSize(sendVo.getListWaitSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_sent.key()){
					sendVo.setListSentSize(sendVo.getListSentSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_done.key()){
					sendVo.setListDoneAllSize(doneTypeSize.size());
					sendVo.setListOverSize(overTypeSize.size());
				}
			}else if(app == 20){
				if(state == StateEnum.col_pending.key() && pendingSubState.contains(subState)){
					receiveVo.setListPendingSize(receiveVo.getListPendingSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_pending.key() && (subState == SubStateEnum.col_pending_ZCDB.key() || subState == SubStateEnum.col_pending_specialBackToSenderReGo.key())){
					receiveVo.setListZcdbSize(receiveVo.getListZcdbSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_waitSend.key() && memberId.equals(userId)){
					receiveVo.setListWaitSize(receiveVo.getListWaitSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_sent.key()){
					receiveVo.setListSentSize(receiveVo.getListSentSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_done.key()){
					receiveVo.setListDoneAllSize(SWdoneTypeSize.size());
					receiveVo.setListOverSize(SWoverTypeSize.size());
				}
			}else if(app == 21){
				if(state == StateEnum.col_pending.key() && pendingSubState.contains(subState)){
					reportVo.setListPendingSize(reportVo.getListPendingSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_pending.key() && (subState == SubStateEnum.col_pending_ZCDB.key() || subState == SubStateEnum.col_pending_specialBackToSenderReGo.key())){
					reportVo.setListZcdbSize(reportVo.getListZcdbSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_waitSend.key() && memberId.equals(userId)){
					reportVo.setListWaitSize(reportVo.getListWaitSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_sent.key()){
					reportVo.setListSentSize(reportVo.getListSentSize() + summaryCount.intValue());
				}else if(state == StateEnum.col_done.key()){
					reportVo.setListDoneAllSize(reportVo.getListDoneAllSize() + summaryCount.intValue());
				}
			}
			
		}
		
		List<EdocSummaryCountVO> list = new ArrayList<EdocSummaryCountVO>();
		list.add(sendVo);
		list.add(receiveVo);
		list.add(reportVo);
		return list;
	}
	
	/**
	 * 特殊字符转化
	 * @param str
	 * @return
	 */
	public String convertSpecialChat(String str) {
		if(Strings.isNotBlank(str)) {
        	StringBuffer buffer=new StringBuffer();
        	for(int i=0;i<str.length();i++) {
        		if(str.charAt(i)=='\'') {
        			buffer.append("\\'");
        		} else {
        			buffer.append(str.charAt(i));
        		}
        	}
        	str = SQLWildcardUtil.escape(buffer.toString());
        }
		return str;
	}

	public void setEdocListDao(EdocListDao edocListDao) {
		this.edocListDao = edocListDao;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public void setWorkTimeManager(WorkTimeManager workTimeManager) {
		this.workTimeManager = workTimeManager;
	}

}
